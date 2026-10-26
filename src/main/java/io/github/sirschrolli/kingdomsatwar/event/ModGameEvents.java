package io.github.sirschrolli.kingdomsatwar.event;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Formation;
import io.github.sirschrolli.kingdomsatwar.block.ElementalDragonEggBlock;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.DragonScaleArmorItem;
import io.github.sirschrolli.kingdomsatwar.network.StartIntroPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Spielereignisse auf dem Server. */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class ModGameEvents {
    /** Wie weit ein Blitzeinschlag Blitzdrachen-Eier erreicht. */
    private static final int LIGHTNING_EGG_RADIUS = 6;

    private ModGameEvents() {}

    /** Beim ersten Betreten der Welt bekommt der Spieler das Erzähl-Intro zu sehen. */
    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.getData(ModAttachments.INTRO_SEEN)) {
            player.setData(ModAttachments.INTRO_SEEN, true);
            PacketDistributor.sendToPlayer(player, new StartIntroPayload(false));
        }
    }

    /**
     * Befehle der Mod: {@code /kaw intro} zeigt das Erzähl-Intro noch einmal; {@code /kaw army <Art> [Anzahl]}
     * (nur für Operatoren) stellt sofort Soldaten in den Dienst des Spielers.
     */
    @SubscribeEvent
    static void onRegisterCommands(RegisterCommandsEvent event) {
        var army = Commands.literal("army").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (SoldierType type : SoldierType.values()) {
            army.then(Commands.literal(type.id())
                    .executes(context -> summonArmy(context.getSource().getPlayerOrException(), type, 1))
                    .then(Commands.argument("count", IntegerArgumentType.integer(1, 32))
                            .executes(context -> summonArmy(context.getSource().getPlayerOrException(), type,
                                    IntegerArgumentType.getInteger(context, "count")))));
        }
        event.getDispatcher().register(Commands.literal("kaw")
                .then(Commands.literal("intro").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    PacketDistributor.sendToPlayer(player, new StartIntroPayload(true));
                    return 1;
                }))
                .then(army)
                .then(Commands.literal("boss").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("fire").executes(context -> summonBoss(context.getSource().getPlayerOrException()))))
                .then(Commands.literal("lair").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> listLairs(context.getSource()))
                        .then(Commands.literal("awaken").executes(context -> awakenLair(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("reset").executes(context -> resetLair(context.getSource().getPlayerOrException())))));
    }

    /** {@code /kaw lair}: alle Nester der Welt mit Lage und Zustand; die Koordinaten lassen sich anklicken (Teleport). */
    private static int listLairs(net.minecraft.commands.CommandSourceStack source) {
        ServerLevel overworld = source.getServer().overworld();
        java.util.Map<DragonType, net.minecraft.world.level.ChunkPos> nests =
                io.github.sirschrolli.kingdomsatwar.worldgen.lair.NestLocator.all(overworld);
        if (nests.isEmpty()) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.lair.none"));
            return 0;
        }
        io.github.sirschrolli.kingdomsatwar.world.LairRecords records = io.github.sirschrolli.kingdomsatwar.world.LairRecords.of(overworld);
        source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.lair.header").withStyle(net.minecraft.ChatFormatting.GOLD), false);
        for (DragonType type : io.github.sirschrolli.kingdomsatwar.item.DragonCompassItem.ORDER) {
            net.minecraft.world.level.ChunkPos chunk = nests.get(type);
            if (chunk == null) {
                continue;
            }
            BlockPos center = io.github.sirschrolli.kingdomsatwar.item.DragonCompassItem.nestCenter(overworld, type, chunk);
            var entry = records.get(type);
            Component state = entry == null
                    ? Component.translatable("command.kingdomsatwar.lair.unexplored").withStyle(net.minecraft.ChatFormatting.GRAY)
                    : Component.translatable("command.kingdomsatwar.lair.phase." + entry.phase().name().toLowerCase(java.util.Locale.ROOT));
            String tp = "/tp @s " + center.getX() + " " + (center.getY() + 30) + " " + center.getZ();
            Component x = Component.literal(String.valueOf(center.getX())).withStyle(style -> style.withUnderlined(true)
                    .withClickEvent(new net.minecraft.network.chat.ClickEvent.SuggestCommand(tp)));
            Component z = Component.literal(String.valueOf(center.getZ())).withStyle(style -> style.withUnderlined(true)
                    .withClickEvent(new net.minecraft.network.chat.ClickEvent.SuggestCommand(tp)));
            Component line = Component.translatable("command.kingdomsatwar.lair.entry",
                    Component.translatable("lair.kingdomsatwar." + type.id() + ".name").withColor(type.color()), x, z, state);
            source.sendSuccess(() -> line, false);
        }
        return nests.size();
    }

    /** Nestkern des nächstgelegenen (bereits erzeugten) Nests im Umkreis von 160 Blöcken. */
    private static io.github.sirschrolli.kingdomsatwar.block.entity.@org.jspecify.annotations.Nullable DragonLairCoreBlockEntity nearestCore(
            ServerPlayer player) {
        ServerLevel level = player.level();
        if (level.dimension() != net.minecraft.world.level.Level.OVERWORLD) {
            return null;
        }
        io.github.sirschrolli.kingdomsatwar.world.LairRecords records = io.github.sirschrolli.kingdomsatwar.world.LairRecords.of(level);
        io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity best = null;
        double bestDist = 160.0 * 160.0;
        for (DragonType type : DragonType.values()) {
            var entry = records.get(type);
            if (entry == null) {
                continue;
            }
            BlockPos core = io.github.sirschrolli.kingdomsatwar.worldgen.lair.LairDesigns.of(type).corePos(entry.center());
            double d = player.distanceToSqr(core.getX() + 0.5, player.getY(), core.getZ() + 0.5);
            if (d < bestDist && level.getBlockEntity(core) instanceof io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity be) {
                best = be;
                bestDist = d;
            }
        }
        return best;
    }

    private static int awakenLair(ServerPlayer player) {
        var core = nearestCore(player);
        if (core == null) {
            player.sendSystemMessage(Component.translatable("command.kingdomsatwar.lair.no_core").withStyle(net.minecraft.ChatFormatting.RED));
            return 0;
        }
        if (core.phase() == io.github.sirschrolli.kingdomsatwar.world.LairRecords.Phase.DEFEATED) {
            core.reset(player.level());
        }
        if (core.phase() == io.github.sirschrolli.kingdomsatwar.world.LairRecords.Phase.AWAKE || core.awaken(player.level()) == null) {
            player.sendSystemMessage(Component.translatable("command.kingdomsatwar.lair.cannot_awaken").withStyle(net.minecraft.ChatFormatting.RED));
            return 0;
        }
        player.sendSystemMessage(Component.translatable("command.kingdomsatwar.lair.awakened",
                Component.translatable("lair.kingdomsatwar." + core.element().id() + ".name").withColor(core.element().color())));
        return 1;
    }

    private static int resetLair(ServerPlayer player) {
        var core = nearestCore(player);
        if (core == null) {
            player.sendSystemMessage(Component.translatable("command.kingdomsatwar.lair.no_core").withStyle(net.minecraft.ChatFormatting.RED));
            return 0;
        }
        core.reset(player.level());
        player.sendSystemMessage(Component.translatable("command.kingdomsatwar.lair.reset",
                Component.translatable("lair.kingdomsatwar." + core.element().id() + ".name").withColor(core.element().color())));
        return 1;
    }

    /** Lässt einen Drachen-Boss vor dem Spieler erwachen (zum Testen; das Nest ist dort, wo er erwacht). */
    private static int summonBoss(ServerPlayer player) {
        ServerLevel level = player.level();
        net.minecraft.world.phys.Vec3 look = player.getLookAngle().multiply(1, 0, 1);
        look = look.lengthSqr() < 1.0e-4 ? new net.minecraft.world.phys.Vec3(0, 0, 1) : look.normalize();
        net.minecraft.world.phys.Vec3 at = player.position().add(look.scale(26));
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(at.x),
                (int) Math.floor(at.z));
        BlockPos home = BlockPos.containing(at.x, y, at.z);
        var boss = ModEntities.PYRRHON.get().create(level, EntitySpawnReason.COMMAND);
        if (boss == null) {
            return 0;
        }
        boss.snapTo(home.getX() + 0.5, home.getY() - 11, home.getZ() + 0.5, player.getYRot() + 180.0f, 0.0f);
        boss.yBodyRot = player.getYRot() + 180.0f;
        boss.yHeadRot = boss.yBodyRot;
        level.addFreshEntity(boss);
        boss.beginAwakening(level, home);
        player.sendSystemMessage(Component.translatable("command.kingdomsatwar.boss.summoned", boss.getDisplayName()));
        return 1;
    }

    private static int summonArmy(ServerPlayer player, SoldierType type, int count) {
        ServerLevel level = player.level();
        List<BlockPos> posts = Formation.around(level, player.blockPosition(), count + 1);
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            BlockPos pos = posts.get(Math.min(i + 1, posts.size() - 1));
            SoldierEntity soldier = ModEntities.SOLDIERS.get(type).get().create(level, EntitySpawnReason.COMMAND);
            if (soldier == null) {
                continue;
            }
            soldier.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), 0.0f);
            EventHooks.finalizeMobSpawn(soldier, level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.COMMAND, null);
            level.addFreshEntity(soldier);
            soldier.recruitBy(player);
            spawned++;
        }
        int result = spawned;
        player.sendSystemMessage(Component.translatable("message.kingdomsatwar.army_summoned", result,
                Component.translatable(type.translationKey())));
        return result;
    }

    /** Soldaten verletzen weder ihren Besitzer noch dessen Drachen, Tiere oder andere Soldaten (auch nicht mit Pfeilen). */
    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof SoldierEntity soldier && soldier != event.getEntity()
                && soldier.isAlliedTo(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    /** Voller Satz Drachenschuppen-Rüstung: jede Sekunde die Kraft des Elements erneuern. */
    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % 20 != 0 || !player.isAlive()) {
            return;
        }
        DragonType element = DragonScaleArmorItem.fullSet(player);
        if (element != null) {
            DragonScaleArmorItem.applySetBonus(player, element);
        }
    }

    /** Blitzeinschläge in der Nähe lassen Blitzdrachen-Eier schneller schlüpfen. */
    @SubscribeEvent
    static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LightningBolt bolt) || event.loadedFromDisk()
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos center = bolt.blockPosition();
        int r = LIGHTNING_EGG_RADIUS;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof ElementalDragonEggBlock egg && egg.getDragonType() == DragonType.LIGHTNING) {
                egg.advance(level, pos.immutable(), state, 1);
            }
        }
    }
}
