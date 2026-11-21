package io.github.sirschrolli.kingdomsatwar.shadow.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Felsbrücke: ein natürlicher Bogen aus Schattenstein, der eine Schlucht oder Senke überspannt – oben mit Schattengras
 * bewachsen, unten hängen Glimmerranken herab. Gesucht wird von der Mitte des Chunks aus in einer zufälligen Richtung
 * nach beiden Seiten je eine Kante, die mindestens zehn Blöcke über dem Grund der Senke liegt; die Brücke spannt sich
 * dann leicht gewölbt von Kante zu Kante.
 */
public record RockBridgeFeature() implements Feature {
    public static final MapCodec<RockBridgeFeature> CODEC = MapCodec.unit(RockBridgeFeature::new);
    private static final int MAX_REACH = 20;

    @Override
    public MapCodec<RockBridgeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int cx = (origin.getX() & ~15) + 8;
        int cz = (origin.getZ() & ~15) + 8;
        int floor = height(level, cx, cz);
        double angle = random.nextDouble() * Math.PI;
        double dx = Math.cos(angle);
        double dz = Math.sin(angle);
        int reachA = -1;
        int reachB = -1;
        int topA = 0;
        int topB = 0;
        for (int s = 5; s <= MAX_REACH && (reachA < 0 || reachB < 0); s++) {
            if (reachA < 0) {
                int h = height(level, (int) Math.round(cx - dx * s), (int) Math.round(cz - dz * s));
                if (h >= floor + 10) {
                    reachA = s;
                    topA = h;
                }
            }
            if (reachB < 0) {
                int h = height(level, (int) Math.round(cx + dx * s), (int) Math.round(cz + dz * s));
                if (h >= floor + 10) {
                    reachB = s;
                    topB = h;
                }
            }
        }
        if (reachA < 0 || reachB < 0 || Math.abs(topA - topB) > 8 || reachA + reachB < 12) {
            return false;
        }
        BlockPos middle = new BlockPos(cx, floor, cz);
        if (io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillageStructure.isNearVillage(level, middle, 24)) {
            return false;
        }
        // von etwas hinter der einen Kante bis etwas hinter die andere (die Enden stecken im Fels)
        double ax = cx - dx * (reachA + 2);
        double az = cz - dz * (reachA + 2);
        double bx = cx + dx * (reachB + 2);
        double bz = cz + dz * (reachB + 2);
        int length = reachA + reachB + 4;
        int steps = length * 3;
        double px = -dz;
        double pz = dx;
        double width = 1.7 + random.nextDouble() * 0.9;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double x = Mth.lerp(t, ax, bx);
            double z = Mth.lerp(t, az, bz);
            double arch = Math.sin(Math.PI * t);
            double top = Mth.lerp(t, topA - 2, topB - 2) + 2.2 * arch;
            // in der Mitte schlanker, an den Enden dicker
            double thick = 2.2 + 2.0 * (1.0 - arch);
            for (double w = -width - 0.6; w <= width + 0.6; w += 0.5) {
                double edge = 1.0 - (w * w) / ((width + 0.6) * (width + 0.6));
                if (edge <= 0) {
                    continue;
                }
                int bxp = (int) Math.floor(x + px * w);
                int bzp = (int) Math.floor(z + pz * w);
                int yTop = (int) Math.floor(top - (1.0 - edge) * 1.2);
                int yBottom = (int) Math.floor(top - thick * Math.sqrt(edge));
                for (int y = yBottom; y <= yTop; y++) {
                    BlockState state = y == yTop ? (random.nextFloat() < 0.75f ? ShadowBlocks.SHADOW_GRASS.get().defaultBlockState()
                            : ShadowBlocks.SHADOW_STONE.base().get().defaultBlockState())
                            : y <= yBottom + 1 ? ShadowBlocks.DEEP_SHADOW_STONE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,
                                    net.minecraft.core.Direction.Axis.Y)
                            : ShadowBlocks.SHADOW_STONE.base().get().defaultBlockState();
                    set(level, new BlockPos(bxp, y, bzp), state, y == yTop);
                }
                // Glimmerranken unter der Mitte des Bogens
                if (arch > 0.5 && random.nextFloat() < 0.06f) {
                    int len = 2 + random.nextInt(5);
                    for (int k = 1; k <= len; k++) {
                        BlockPos pos = new BlockPos(bxp, yBottom - k, bzp);
                        if (!level.getBlockState(pos).isAir()) {
                            break;
                        }
                        level.setBlock(pos, (k == len ? ShadowBlocks.GLIMMER_VINES.get() : ShadowBlocks.GLIMMER_VINES_PLANT.get())
                                .defaultBlockState(), 2);
                    }
                }
            }
        }
        // ein paar Gräser und Nachtkristalle oben auf dem Bogen
        for (int i = 0; i < 6; i++) {
            double t = 0.2 + random.nextDouble() * 0.6;
            int x = (int) Math.floor(Mth.lerp(t, ax, bx));
            int z = (int) Math.floor(Mth.lerp(t, az, bz));
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos.below()).is(ShadowBlocks.SHADOW_GRASS.get()) && level.getBlockState(pos).isAir()) {
                level.setBlock(pos, (random.nextFloat() < 0.3f ? ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get() : ShadowBlocks.PALE_GRASS.get())
                        .defaultBlockState(), 2);
            }
        }
        return true;
    }

    private static int height(WorldGenLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
    }

    private static void set(WorldGenLevel level, BlockPos pos, BlockState state, boolean top) {
        BlockState old = level.getBlockState(pos);
        if (old.isAir() || old.canBeReplaced() || old.is(BlockTags.LEAVES) || (top && old.is(ShadowBlocks.SHADOW_STONE.base().get()))) {
            level.setBlock(pos, state, 2);
        } else if (old.is(Blocks.WATER)) {
            level.setBlock(pos, state, 2);
        }
    }
}
