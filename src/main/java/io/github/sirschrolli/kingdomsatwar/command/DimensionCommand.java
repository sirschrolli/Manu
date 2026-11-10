package io.github.sirschrolli.kingdomsatwar.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidGate;
import io.github.sirschrolli.kingdomsatwar.world.ShadowGate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * {@code /kaw dim <oberwelt|nether|end|schattenwelt|leere|abgrund>} (oder eine beliebige Dimension als ID): wechselt sofort in
 * eine andere Dimension – für den Kreativmodus (und für alle mit Cheats). Man landet an derselben Stelle (im Nether an
 * einem Achtel der Koordinaten, wie bei einem Portal) auf sicherem Boden: im Nether in einer Höhle mit festem Grund, im
 * End auf der Obsidianplattform, in der Leere auf dem nächsten Bruchstück (sonst auf einer kleinen Plattform).
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class DimensionCommand {
    /** Kurze Namen (deutsch) und ihre Dimensionen, in der Reihenfolge der Vorschläge. */
    private static final Map<String, ResourceKey<Level>> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put("oberwelt", Level.OVERWORLD);
        NAMES.put("nether", Level.NETHER);
        NAMES.put("end", Level.END);
        NAMES.put("schattenwelt", ShadowGate.SHADOW_WORLD);
        NAMES.put("leere", VoidGate.THE_VOID);
        NAMES.put("abgrund", io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate.THE_ABYSS);
    }

    private DimensionCommand() {}

    /** Wer den Befehl nutzen darf: Spieler im Kreativmodus oder mit Cheats (Spielleiter-Rechten). */
    private static boolean allowed(CommandSourceStack source) {
        if (Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source)) {
            return true;
        }
        ServerPlayer player = source.getPlayer();
        return player != null && player.isCreative();
    }

    public static LiteralArgumentBuilder<CommandSourceStack> command() {
        LiteralArgumentBuilder<CommandSourceStack> dim = Commands.literal("dim").requires(DimensionCommand::allowed);
        for (Map.Entry<String, ResourceKey<Level>> entry : NAMES.entrySet()) {
            dim.then(Commands.literal(entry.getKey()).executes(context -> travel(context.getSource().getPlayerOrException(), entry.getValue())));
        }
        dim.then(Commands.argument("dimension", DimensionArgument.dimension()).executes(context ->
                travel(context.getSource().getPlayerOrException(), DimensionArgument.getDimension(context, "dimension").dimension())));
        return dim;
    }

    /** Nach einem Wechsel des Spielmodus kennt der Spieler die Befehle neu (der Dimensionswechsel erscheint im Kreativmodus). */
    @SubscribeEvent
    static void onGameModeChange(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MinecraftServer server = player.level().getServer();
            server.execute(() -> server.getCommands().sendCommands(player));
        }
    }

    private static int travel(ServerPlayer player, ResourceKey<Level> key) {
        MinecraftServer server = player.level().getServer();
        ServerLevel target = server.getLevel(key);
        if (target == null) {
            player.sendSystemMessage(Component.translatable("command.kingdomsatwar.dim.missing").withStyle(ChatFormatting.RED));
            return 0;
        }
        if (target == player.level()) {
            player.sendSystemMessage(Component.translatable("command.kingdomsatwar.dim.already", name(key)).withStyle(ChatFormatting.GRAY));
            return 0;
        }
        double scale = DimensionType.getTeleportationScale(player.level().dimensionType(), target.dimensionType());
        var border = target.getWorldBorder();
        int x = Mth.floor(Mth.clamp(player.getX() * scale, border.getMinX() + 16, border.getMaxX() - 16));
        int z = Mth.floor(Mth.clamp(player.getZ() * scale, border.getMinZ() + 16, border.getMaxZ() - 16));
        BlockPos spot = safeSpot(target, x, z);
        player.teleportTo(target, spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot(), true);
        player.resetFallDistance();
        target.playSound(null, spot, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.2f);
        player.sendSystemMessage(Component.translatable("command.kingdomsatwar.dim.done", name(key), spot.getX(), spot.getY(), spot.getZ())
                .withStyle(ChatFormatting.GOLD));
        return 1;
    }

    private static Component name(ResourceKey<Level> key) {
        for (Map.Entry<String, ResourceKey<Level>> entry : NAMES.entrySet()) {
            if (entry.getValue() == key || entry.getValue().equals(key)) {
                return Component.translatable("command.kingdomsatwar.dim.name." + entry.getKey());
            }
        }
        if (key.identifier().equals(KingdomsAtWar.id("arcane_sphere"))) {
            return Component.translatable("command.kingdomsatwar.dim.name.sphaere");
        }
        return Component.translatable("command.kingdomsatwar.dim.name.other", key.identifier().toString());
    }

    /** Ein sicherer Platz zum Landen nahe (x, z): fester Grund, zwei Blöcke Luft darüber, keine Lava. */
    public static BlockPos safeSpot(ServerLevel level, int x, int z) {
        if (level.dimension() == Level.END) {
            // wie beim Endportal: die Obsidianplattform (Boden eine Ebene unter den Füßen)
            BlockPos feet = ServerLevel.END_SPAWN_POINT.below();
            EndPlatformFeature.createEndPlatform(level, feet, true);
            return feet;
        }
        level.getChunk(x >> 4, z >> 4);
        if (level.dimensionType().hasCeiling()) {
            BlockPos cave = cave(level, x, z);
            if (cave != null) {
                return cave;
            }
            for (int r = 8; r <= 96; r += 8) {
                for (int k = 0; k < 12; k++) {
                    double a = k * Math.PI / 6.0 + r * 0.1;
                    int cx = x + (int) Math.round(Math.cos(a) * r);
                    int cz = z + (int) Math.round(Math.sin(a) * r);
                    level.getChunk(cx >> 4, cz >> 4);
                    cave = cave(level, cx, cz);
                    if (cave != null) {
                        return cave;
                    }
                }
            }
            return platform(level, x, 70, z, Blocks.NETHERRACK.defaultBlockState());
        }
        BlockPos ground = surface(level, x, z);
        if (ground != null) {
            return ground;
        }
        // z. B. in der Leere: das nächste Bruchstück in der Nähe suchen
        for (int r = 8; r <= 120; r += 8) {
            int steps = Math.max(8, (int) (2 * Math.PI * r / 12));
            for (int k = 0; k < steps; k++) {
                double a = k * 2 * Math.PI / steps;
                int cx = x + (int) Math.round(Math.cos(a) * r);
                int cz = z + (int) Math.round(Math.sin(a) * r);
                level.getChunk(cx >> 4, cz >> 4);
                ground = surface(level, cx, cz);
                if (ground != null) {
                    return ground;
                }
            }
        }
        boolean abyss = level.dimension() == io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate.THE_ABYSS;
        BlockState floor = level.dimension() == VoidGate.THE_VOID ? VoidBlocks.VOID_STONE.base().get().defaultBlockState()
                : abyss ? io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.CHAOS_STONE.base().get().defaultBlockState()
                : Blocks.STONE.defaultBlockState();
        return platform(level, x, level.dimension() == VoidGate.THE_VOID || abyss ? 120 : Math.max(level.getSeaLevel() + 2, 70), z, floor);
    }

    /** Oberfläche einer Säule (null, wenn dort nichts ist oder Lava liegt). */
    private static BlockPos surface(ServerLevel level, int x, int z) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y <= level.getMinY() + 1) {
            return null;
        }
        BlockState ground = level.getBlockState(new BlockPos(x, y - 1, z));
        if (ground.getFluidState().is(FluidTags.LAVA)) {
            return null;
        }
        return new BlockPos(x, y, z);
    }

    /** Eine Höhle unter der Decke (Nether): fester Grund ohne Lava, zwei Blöcke Luft. */
    private static BlockPos cave(ServerLevel level, int x, int z) {
        int top = level.getMinY() + level.dimensionType().logicalHeight() - 4;
        for (int y = Math.max(level.getMinY() + 6, 32); y < top; y++) {
            BlockPos feet = new BlockPos(x, y, z);
            BlockState below = level.getBlockState(feet.below());
            if (below.isSolidRender() && !below.getFluidState().is(FluidTags.LAVA) && level.getBlockState(feet).isAir()
                    && level.getBlockState(feet.above()).isAir()) {
                return feet;
            }
        }
        return null;
    }

    /** Eine kleine Plattform (5 × 5) mit freiem Raum darüber, falls nirgends fester Grund ist. */
    private static BlockPos platform(ServerLevel level, int x, int y, int z, BlockState floor) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                level.setBlockAndUpdate(new BlockPos(x + dx, y - 1, z + dz), floor);
                for (int dy = 0; dy <= 2; dy++) {
                    level.setBlockAndUpdate(new BlockPos(x + dx, y + dy, z + dz), Blocks.AIR.defaultBlockState());
                }
            }
        }
        return new BlockPos(x, y, z);
    }
}
