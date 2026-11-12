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
            io.github.sirschrolli.kingdomsatwar.network.ModNetwork.send(player, new StartIntroPayload(false));
        }
    }

    /**
     * Befehle der Mod: {@code /kaw intro} zeigt das Erzähl-Intro noch einmal; {@code /kaw army <Art> [Anzahl]}
     * (nur für Operatoren) stellt sofort Soldaten in den Dienst des Spielers. Seit 1.1.2 nach Art geordnet:
     * {@code /kaw dim <Dimension>}, {@code /kaw boss <Boss>} und {@code /kaw structure <Bauwerk>} (siehe
     * {@link io.github.sirschrolli.kingdomsatwar.command.KawLocate}).
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
        army.then(Commands.literal("xp").then(Commands.argument("amount", IntegerArgumentType.integer(1,
                io.github.sirschrolli.kingdomsatwar.army.Veterancy.total(io.github.sirschrolli.kingdomsatwar.army.Veterancy.MAX_LEVEL)))
                .executes(context -> armyExperience(context.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(context, "amount")))));
        event.getDispatcher().register(Commands.literal("kaw")
                .then(Commands.literal("intro").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    io.github.sirschrolli.kingdomsatwar.network.ModNetwork.send(player, new StartIntroPayload(true));
                    return 1;
                }))
                .then(army)
                .then(io.github.sirschrolli.kingdomsatwar.command.KawLocate.bossCommand())
                .then(io.github.sirschrolli.kingdomsatwar.command.KawLocate.structureCommand())
                .then(io.github.sirschrolli.kingdomsatwar.command.DimensionCommand.command())
                .then(storyCommand())
                .then(sealCommand())
                .then(io.github.sirschrolli.kingdomsatwar.war.RealmWar.command())
                .then(io.github.sirschrolli.kingdomsatwar.war.RealmRaids.command())
                .then(io.github.sirschrolli.kingdomsatwar.hero.HeroLevel.command()));
    }

    /** {@code /kaw story reset|card <Kapitel>|chronicle}: Geschichte zurücksetzen, Titelkarte zeigen, Chronik geben. */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> storyCommand() {
        return Commands.literal("story").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("reset").executes(context -> {
                    io.github.sirschrolli.kingdomsatwar.story.Story.reset(context.getSource().getPlayerOrException());
                    context.getSource().sendSuccess(() -> Component.translatable("command.kingdomsatwar.story.reset"), false);
                    return 1;
                }))
                .then(Commands.literal("card").then(Commands.argument("chapter", IntegerArgumentType.integer(0, 10)).executes(context -> {
                    io.github.sirschrolli.kingdomsatwar.story.Story.sendCard(context.getSource().getPlayerOrException(),
                            io.github.sirschrolli.kingdomsatwar.story.Chapter.byIndex(IntegerArgumentType.getInteger(context, "chapter")), -1);
                    return 1;
                })))
                .then(Commands.literal("chronicle").executes(context -> {
                    io.github.sirschrolli.kingdomsatwar.story.Story.giveChronicle(context.getSource().getPlayerOrException());
                    return 1;
                }));
    }

    /**
     * {@code /kaw seal}: Zustand des Siegels und Lage des Siegelkreises (anklickbar); {@code shard <Element>} gibt einen
     * Siegelsplitter, {@code insert <Element>} setzt ihn direkt ein, {@code break} lässt das Siegel brechen,
     * {@code reset} stellt alles wieder her.
     */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> sealCommand() {
        var shard = Commands.literal("shard");
        var insert = Commands.literal("insert");
        for (DragonType type : DragonType.values()) {
            shard.then(Commands.literal(type.id()).executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(
                        io.github.sirschrolli.kingdomsatwar.registry.ModItems.SEAL_SHARDS.get(type).get());
                player.getInventory().placeItemBackInInventory(stack, net.minecraft.util.Prediction.SERVER_ONLY);
                return 1;
            }));
            insert.then(Commands.literal(type.id()).executes(context -> {
                boolean ok = io.github.sirschrolli.kingdomsatwar.world.SealCircle.insert(context.getSource().getLevel().getServer().overworld(), type);
                if (!ok) {
                    context.getSource().sendFailure(Component.translatable("command.kingdomsatwar.seal.insert_failed"));
                }
                return ok ? 1 : 0;
            }));
        }
        return Commands.literal("seal").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> sealStatus(context.getSource()))
                .then(shard)
                .then(insert)
                .then(Commands.literal("break").executes(context -> {
                    ServerLevel overworld = context.getSource().getServer().overworld();
                    io.github.sirschrolli.kingdomsatwar.world.SealState seal = io.github.sirschrolli.kingdomsatwar.world.SealState.of(overworld);
                    BlockPos center = io.github.sirschrolli.kingdomsatwar.world.SealCircle.center(overworld);
                    if (center == null || seal.isBroken() || seal.isBreaking()) {
                        context.getSource().sendFailure(Component.translatable("command.kingdomsatwar.seal.insert_failed"));
                        return 0;
                    }
                    seal.setCenter(center);
                    for (DragonType type : DragonType.values()) {
                        seal.insert(type);
                    }
                    seal.setBreakTicks(0);
                    seal.save(overworld);
                    return 1;
                }))
                .then(Commands.literal("reset").executes(context -> {
                    io.github.sirschrolli.kingdomsatwar.world.SealCircle.reset(context.getSource().getServer().overworld());
                    context.getSource().sendSuccess(() -> Component.translatable("command.kingdomsatwar.seal.reset"), false);
                    return 1;
                }));
    }

    private static int sealStatus(net.minecraft.commands.CommandSourceStack source) {
        ServerLevel overworld = source.getServer().overworld();
        BlockPos center = io.github.sirschrolli.kingdomsatwar.world.SealCircle.center(overworld);
        if (center == null) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.seal.none"));
            return 0;
        }
        io.github.sirschrolli.kingdomsatwar.world.SealState seal = io.github.sirschrolli.kingdomsatwar.world.SealState.of(overworld);
        Component coords = io.github.sirschrolli.kingdomsatwar.command.KawLocate.coordinates(overworld,
                io.github.sirschrolli.kingdomsatwar.command.KawPlaces.SEAL_CIRCLE,
                new io.github.sirschrolli.kingdomsatwar.command.KawPlaces.Spot(center, seal.center() != null, null));
        Component state = Component.translatable(seal.isBroken() ? "command.kingdomsatwar.seal.state.broken"
                : seal.isBreaking() ? "command.kingdomsatwar.seal.state.breaking" : "command.kingdomsatwar.seal.state.intact");
        source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.seal.status", coords, seal.insertedCount(), state)
                .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE), false);
        return 1;
    }

    /** {@code /kaw army xp <Menge>}: Alle eigenen Soldaten in der Nähe (128 Blöcke) bekommen so viel Kampferfahrung. */
    private static int armyExperience(ServerPlayer player, int amount) {
        ServerLevel level = player.level();
        List<io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity> soldiers = level.getEntitiesOfClass(
                io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity.class, player.getBoundingBox().inflate(128.0),
                soldier -> soldier.isAlive() && soldier.isOwnedBy(player));
        for (var soldier : soldiers) {
            soldier.gainCombatExperience(amount);
        }
        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("commands.kingdomsatwar.army.xp", amount, soldiers.size()));
        return soldiers.size();
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
            soldier.recruitIntoField(player);
            spawned++;
        }
        int result = spawned;
        player.sendSystemMessage(Component.translatable("message.kingdomsatwar.army_summoned", result,
                Component.translatable(type.translationKey())));
        return result;
    }

    /** Soldaten verletzen weder ihren Besitzer noch dessen Drachen, Tiere oder andere Soldaten (auch nicht mit Pfeilen); hungrige treffen schwächer. */
    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof SoldierEntity soldier && soldier != event.getEntity()) {
            if (soldier.isAlliedTo(event.getEntity())) {
                event.setCanceled(true);
            } else if (soldier.isHungry()) {
                // wer hungert, schlägt (und schießt) schwächer zu
                event.setAmount(event.getAmount() * io.github.sirschrolli.kingdomsatwar.army.Provisions.HUNGRY_DAMAGE);
            }
        }
    }

    /** Voller Satz Drachenschuppen-Rüstung: jede Sekunde die Kraft des Elements erneuern. */
    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % 20 != 0 || !player.isAlive()) {
            return;
        }
        DragonScaleArmorItem.applySetBonus(player, DragonScaleArmorItem.fullSet(player));
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
