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
                .then(Commands.literal("boss").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("fire").executes(context -> summonBoss(context.getSource().getPlayerOrException(), false)))
                        .then(Commands.literal("nyxar").executes(context -> summonBoss(context.getSource().getPlayerOrException(), true))))
                .then(Commands.literal("citadel").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> citadel(context.getSource().getPlayerOrException(), false))
                        .then(Commands.literal("tp").executes(context -> citadel(context.getSource().getPlayerOrException(), true))))
                .then(Commands.literal("throne").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> throne(context.getSource().getPlayerOrException(), ""))
                        .then(Commands.literal("tp").executes(context -> throne(context.getSource().getPlayerOrException(), "tp")))
                        .then(Commands.literal("reset").executes(context -> throne(context.getSource().getPlayerOrException(), "reset"))))
                .then(Commands.literal("atoll").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> atoll(context.getSource(), ""))
                        .then(Commands.literal("tp").executes(context -> atoll(context.getSource(), "tp")))
                        .then(Commands.literal("awaken").executes(context -> atoll(context.getSource(), "awaken")))
                        .then(Commands.literal("reset").executes(context -> atoll(context.getSource(), "reset"))))
                .then(Commands.literal("mondhain").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> mondhain(context.getSource(), ""))
                        .then(Commands.literal("tp").executes(context -> mondhain(context.getSource(), "tp")))
                        .then(Commands.literal("awaken").executes(context -> mondhain(context.getSource(), "awaken"))))
                .then(Commands.literal("zauberbuehne").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> mystikStage(context.getSource(), ""))
                        .then(Commands.literal("tp").executes(context -> mystikStage(context.getSource(), "tp")))
                        .then(Commands.literal("awaken").executes(context -> mystikStage(context.getSource(), "awaken")))
                        .then(Commands.literal("reset").executes(context -> mystikStage(context.getSource(), "reset"))))
                .then(Commands.literal("fehlerzone").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> errorZone(context.getSource(), ""))
                        .then(Commands.literal("tp").executes(context -> errorZone(context.getSource(), "tp")))
                        .then(Commands.literal("awaken").executes(context -> errorZone(context.getSource(), "awaken")))
                        .then(Commands.literal("reset").executes(context -> errorZone(context.getSource(), "reset"))))
                .then(Commands.literal("lair").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(context -> listLairs(context.getSource()))
                        .then(Commands.literal("awaken").executes(context -> awakenLair(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("reset").executes(context -> resetLair(context.getSource().getPlayerOrException()))))
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
        String tp = "/tp @s " + center.getX() + " " + (center.getY() + 2) + " " + (center.getZ() + 8);
        Component coords = Component.literal(center.getX() + " " + center.getY() + " " + center.getZ()).withStyle(style -> style
                .withUnderlined(true).withClickEvent(new net.minecraft.network.chat.ClickEvent.SuggestCommand(tp)));
        Component state = Component.translatable(seal.isBroken() ? "command.kingdomsatwar.seal.state.broken"
                : seal.isBreaking() ? "command.kingdomsatwar.seal.state.breaking" : "command.kingdomsatwar.seal.state.intact");
        source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.seal.status", coords, seal.insertedCount(), state)
                .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE), false);
        return 1;
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
    /** {@code /kaw citadel [tp]}: wo die Schattenzitadelle liegt (und auf Wunsch dorthin, an den Fuß der Freitreppe). */
    private static int citadel(ServerPlayer player, boolean teleport) {
        ServerLevel shadow = player.level().getServer().getLevel(io.github.sirschrolli.kingdomsatwar.world.ShadowGate.SHADOW_WORLD);
        BlockPos center = shadow == null ? null : io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelLocator.approximateCenter(shadow);
        if (center == null) {
            player.sendSystemMessage(Component.translatable("command.kingdomsatwar.citadel.none").withStyle(net.minecraft.ChatFormatting.RED));
            return 0;
        }
        player.sendSystemMessage(Component.translatable("command.kingdomsatwar.citadel.at", center.getX(), center.getZ()));
        if (teleport) {
            shadow.getChunk(center.getX() >> 4, (center.getZ() + 76) >> 4);
            int y = shadow.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.getX(), center.getZ() + 76);
            player.teleportTo(shadow, center.getX() + 0.5, y, center.getZ() + 76.5, java.util.Set.of(), 180.0f, 0.0f, true);
        }
        return 1;
    }

    /** {@code /kaw throne [tp|reset]}: wo der Thron der Alten schwebt; hinbringen; den König wieder schlafen legen. */
    private static int throne(ServerPlayer player, String mode) {
        ServerLevel voidLevel = player.level().getServer().getLevel(io.github.sirschrolli.kingdomsatwar.voidrealm.VoidGate.THE_VOID);
        BlockPos center = voidLevel == null ? null : io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneLocator.center(voidLevel);
        if (center == null) {
            player.sendSystemMessage(Component.translatable("command.kingdomsatwar.throne.none").withStyle(net.minecraft.ChatFormatting.RED));
            return 0;
        }
        player.sendSystemMessage(Component.translatable("command.kingdomsatwar.throne.at", center.getX(), center.getY(), center.getZ()));
        if (mode.equals("tp")) {
            voidLevel.getChunk(center.getX() >> 4, (center.getZ() + 20) >> 4);
            player.teleportTo(voidLevel, center.getX() + 0.5, center.getY(), center.getZ() + 20.5, java.util.Set.of(), 180.0f, 0.0f, true);
        } else if (mode.equals("reset")) {
            BlockPos seal = io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneLocator.seal(center);
            voidLevel.getChunk(seal.getX() >> 4, seal.getZ() >> 4);
            if (voidLevel.getBlockEntity(seal) instanceof io.github.sirschrolli.kingdomsatwar.block.voidrealm.ThroneSealBlockEntity throne) {
                throne.reset(voidLevel);
                player.sendSystemMessage(Component.translatable("command.kingdomsatwar.throne.reset"));
            } else {
                player.sendSystemMessage(Component.translatable("command.kingdomsatwar.throne.no_seal").withStyle(net.minecraft.ChatFormatting.RED));
                return 0;
            }
        }
        return 1;
    }

    /**
     * {@code /kaw atoll}: wo das Versunkene Atoll liegt ({@code /locate} findet es nicht, seine Lage ergibt sich aus dem
     * Startwert); {@code tp} bringt dich an den Rand der Lagune, {@code awaken} weckt Karkinos, {@code reset} schüttet den
     * Hügel wieder auf und lässt einen wachen Koloss verschwinden.
     */
    private static int atoll(net.minecraft.commands.CommandSourceStack source, String mode) throws
            com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerLevel overworld = source.getServer().overworld();
        BlockPos center = io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollLocator.center(overworld);
        if (center == null) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.atoll.none"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.atoll.at", center.getX(), center.getY(), center.getZ()), false);
        if (mode.isEmpty()) {
            return 1;
        }
        if (mode.equals("tp")) {
            ServerPlayer player = source.getPlayerOrException();
            overworld.getChunk(center.getX() >> 4, (center.getZ() + 16) >> 4);
            player.teleportTo(overworld, center.getX() + 0.5, center.getY() + 2, center.getZ() + 16.5, java.util.Set.of(), 180.0f, 10.0f, true);
            return 1;
        }
        BlockPos corePos = io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollLocator.core(center);
        overworld.getChunk(corePos.getX() >> 4, corePos.getZ() >> 4);
        if (!(overworld.getBlockEntity(corePos) instanceof io.github.sirschrolli.kingdomsatwar.block.sea.AtollCoreBlockEntity core)) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.atoll.no_core"));
            return 0;
        }
        if (mode.equals("awaken")) {
            if (core.phase() == io.github.sirschrolli.kingdomsatwar.block.sea.AtollCoreBlockEntity.Phase.AWAKE) {
                source.sendFailure(Component.translatable("command.kingdomsatwar.atoll.awake"));
                return 0;
            }
            core.reset(overworld);
            core.awaken(overworld);
            source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.atoll.awakened"), true);
        } else {
            core.reset(overworld);
            source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.atoll.reset"), true);
        }
        return 1;
    }

    /**
     * {@code /kaw mondhain}: Lage des nächsten Mondhains im Feenreich; {@code tp} bringt einen dorthin (auch aus einer
     * anderen Welt), {@code awaken} weckt die Mottenkönigin aus ihrem Kokon.
     */
    private static int mondhain(net.minecraft.commands.CommandSourceStack source, String mode) throws
            com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerLevel fae = source.getServer().getLevel(io.github.sirschrolli.kingdomsatwar.fae.FaeGate.FAE_REALM);
        if (fae == null) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.mondhain.not_fae"));
            return 0;
        }
        boolean inRealm = source.getLevel() == fae;
        if (!inRealm && mode.isEmpty()) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.mondhain.not_fae"));
            return 0;
        }
        net.minecraft.world.phys.Vec3 from = source.getPosition();
        BlockPos center = io.github.sirschrolli.kingdomsatwar.fae.Mondhain.nearest(fae.getSeed(), (int) Math.floor(from.x), (int) Math.floor(from.z));
        int distance = (int) Math.sqrt(Math.pow(center.getX() - from.x, 2) + Math.pow(center.getZ() - from.z, 2));
        Component where = Component.literal(center.getX() + ", " + center.getZ()).withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE);
        source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.mondhain.where", where, distance), false);
        if (mode.isEmpty()) {
            return 1;
        }
        fae.getChunk(center.getX() >> 4, center.getZ() >> 4);
        if (mode.equals("tp")) {
            ServerPlayer player = source.getPlayerOrException();
            int x = center.getX();
            int z = center.getZ() + 9;
            fae.getChunk(x >> 4, z >> 4);
            int y = fae.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            player.teleportTo(fae, x + 0.5, y, z + 0.5, java.util.Set.of(), 180.0f, 10.0f, true);
            return 1;
        }
        BlockPos cocoon = findCocoon(fae, center);
        if (cocoon == null) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.mondhain.no_cocoon"));
            return 0;
        }
        if (!io.github.sirschrolli.kingdomsatwar.block.fae.MothCocoonBlock.awaken(fae, cocoon, null)) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.mondhain.no_cocoon"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.mondhain.awakened"), true);
        return 1;
    }

    /**
     * {@code /kaw zauberbuehne}: Lage der nächsten Zauberbühne im Feenreich; {@code tp} bringt einen in den Zuschauergang (auch
     * aus einer anderen Welt), {@code awaken} beginnt MissMystiks Vorstellung, {@code reset} füllt den Zylinder wieder.
     */
    private static int mystikStage(net.minecraft.commands.CommandSourceStack source, String mode) throws
            com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerLevel fae = source.getServer().getLevel(io.github.sirschrolli.kingdomsatwar.fae.FaeGate.FAE_REALM);
        if (fae == null || (source.getLevel() != fae && mode.isEmpty())) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.zauberbuehne.not_fae"));
            return 0;
        }
        net.minecraft.world.phys.Vec3 from = source.getLevel() == fae ? source.getPosition() : net.minecraft.world.phys.Vec3.ZERO;
        BlockPos center = io.github.sirschrolli.kingdomsatwar.fae.MystikStage.nearest(fae.getSeed(), (int) Math.floor(from.x),
                (int) Math.floor(from.z));
        if (center == null) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.zauberbuehne.none"));
            return 0;
        }
        int distance = (int) Math.sqrt(Math.pow(center.getX() - from.x, 2) + Math.pow(center.getZ() - from.z, 2));
        Component where = Component.literal(center.getX() + ", " + center.getZ()).withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE);
        source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.zauberbuehne.where", where, distance), false);
        if (mode.isEmpty()) {
            return 1;
        }
        fae.getChunk(center.getX() >> 4, center.getZ() >> 4);
        if (mode.equals("tp")) {
            ServerPlayer player = source.getPlayerOrException();
            int x = center.getX();
            int z = center.getZ() + 17;
            fae.getChunk(x >> 4, z >> 4);
            int y = fae.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            player.teleportTo(fae, x + 0.5, y, z + 0.5, java.util.Set.of(), 180.0f, 10.0f, true);
            return 1;
        }
        BlockPos hatPos = findStageHat(fae, center);
        if (hatPos == null || !(fae.getBlockEntity(hatPos) instanceof io.github.sirschrolli.kingdomsatwar.block.fae.StageHatBlockEntity hat)) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.zauberbuehne.no_hat"));
            return 0;
        }
        if (mode.equals("reset")) {
            hat.reset(fae);
            source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.zauberbuehne.reset"), true);
            return 1;
        }
        if (hat.summon(fae) == null) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.zauberbuehne.no_hat"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.zauberbuehne.awakened"), true);
        return 1;
    }

    /**
     * {@code /kaw fehlerzone}: Lage der nächsten Fehlerzone in der Oberwelt; {@code tp} bringt einen an ihren Rand (auch aus
     * einer anderen Welt), {@code awaken} lädt Anni den Troll aus dem Fehlerkern, {@code reset} setzt den Kern zurück.
     */
    private static int errorZone(net.minecraft.commands.CommandSourceStack source, String mode) throws
            com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerLevel overworld = source.getServer().overworld();
        BlockPos from = source.getLevel() == overworld ? BlockPos.containing(source.getPosition()) : BlockPos.ZERO;
        var structure = overworld.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .get(io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures.site(
                        io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.ERROR_ZONE));
        var found = structure.isEmpty() ? null : overworld.getChunkSource().getGenerator().findNearestMapStructure(overworld,
                net.minecraft.core.HolderSet.direct(structure.get()), from, 100, false);
        if (found == null) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.fehlerzone.none"));
            return 0;
        }
        // die Mitte der Zone liegt in der Mitte ihres Chunks
        BlockPos chunkCorner = found.getFirst();
        int cx = (chunkCorner.getX() & ~15) + 8;
        int cz = (chunkCorner.getZ() & ~15) + 8;
        int distance = (int) Math.sqrt(Math.pow(cx - from.getX(), 2) + Math.pow(cz - from.getZ(), 2));
        Component where = Component.literal(cx + ", " + cz).withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE);
        source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.fehlerzone.where", where, distance), false);
        if (mode.isEmpty()) {
            return 1;
        }
        overworld.getChunk(cx >> 4, cz >> 4);
        if (mode.equals("tp")) {
            ServerPlayer player = source.getPlayerOrException();
            int x = cx;
            int z = cz + 19;
            overworld.getChunk(x >> 4, z >> 4);
            int y = overworld.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            player.teleportTo(overworld, x + 0.5, y, z + 0.5, java.util.Set.of(), 180.0f, 10.0f, true);
            return 1;
        }
        BlockPos corePos = findErrorCore(overworld, cx, cz);
        if (corePos == null
                || !(overworld.getBlockEntity(corePos) instanceof io.github.sirschrolli.kingdomsatwar.block.glitch.ErrorCoreBlockEntity core)) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.fehlerzone.no_core"));
            return 0;
        }
        if (mode.equals("reset")) {
            core.reset(overworld);
            source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.fehlerzone.reset"), true);
            return 1;
        }
        if (core.summon(overworld) == null) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.fehlerzone.no_core"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.fehlerzone.awakened"), true);
        return 1;
    }

    /** Sucht den Fehlerkern in der Mitte einer Fehlerzone (erst in der Säule der Mitte, dann ringsum). */
    private static @org.jspecify.annotations.Nullable BlockPos findErrorCore(ServerLevel level, int cx, int cz) {
        int surface = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int r = 0; r <= 3; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                        continue;
                    }
                    for (int y = surface + 4; y >= surface - 24; y--) {
                        pos.set(cx + dx, y, cz + dz);
                        if (level.getBlockState(pos).is(io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.ERROR_CORE.get())) {
                            return pos.immutable();
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Sucht den großen Zylinder auf der Zauberbühne (von oben nach unten). */
    private static @org.jspecify.annotations.Nullable BlockPos findStageHat(ServerLevel level, BlockPos center) {
        int x = center.getX();
        int z = center.getZ() + io.github.sirschrolli.kingdomsatwar.fae.feature.MystikStageFeature.HAT_DZ;
        level.getChunk(x >> 4, z >> 4);
        int top = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        for (int y = top + 2; y > top - 16; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos).is(io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks.STAGE_HAT.get())) {
                return pos;
            }
        }
        return null;
    }

    /** Sucht den Kokon in der Mitte des Mondhains (von oben nach unten). */
    private static @org.jspecify.annotations.Nullable BlockPos findCocoon(ServerLevel level, BlockPos center) {
        int top = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.getX(), center.getZ());
        for (int y = top + 2; y > top - 12; y--) {
            BlockPos pos = new BlockPos(center.getX(), y, center.getZ());
            if (level.getBlockState(pos).is(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MOTH_COCOON.get())) {
                return pos;
            }
        }
        return null;
    }

    private static int summonBoss(ServerPlayer player, boolean nyxar) {
        ServerLevel level = player.level();
        net.minecraft.world.phys.Vec3 look = player.getLookAngle().multiply(1, 0, 1);
        look = look.lengthSqr() < 1.0e-4 ? new net.minecraft.world.phys.Vec3(0, 0, 1) : look.normalize();
        net.minecraft.world.phys.Vec3 at = player.position().add(look.scale(26));
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(at.x),
                (int) Math.floor(at.z));
        BlockPos home = BlockPos.containing(at.x, y, at.z);
        io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity boss = nyxar
                ? ModEntities.NYXAR.get().create(level, EntitySpawnReason.COMMAND)
                : ModEntities.PYRRHON.get().create(level, EntitySpawnReason.COMMAND);
        if (boss == null) {
            return 0;
        }
        boss.snapTo(home.getX() + 0.5, home.getY() - (nyxar ? 0 : 11), home.getZ() + 0.5, player.getYRot() + 180.0f, 0.0f);
        boss.yBodyRot = player.getYRot() + 180.0f;
        boss.yHeadRot = boss.yBodyRot;
        level.addFreshEntity(boss);
        boss.beginAwakening(level, home);
        player.sendSystemMessage(Component.translatable("command.kingdomsatwar.boss.summoned", boss.getDisplayName()));
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
