package io.github.sirschrolli.kingdomsatwar.fae.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Ein Feenring: ein Kreis aus leuchtenden Laternenpilzen auf der Wiese, innen samtiges Feenmoos mit Mondglocken – und
 * manchmal in der Mitte ein bemooster Feenstein, auf dem eine Mondsichel leuchtet. Wo die Feen tanzen.
 */
public record FairyRingFeature() implements Feature {
    public static final MapCodec<FairyRingFeature> CODEC = MapCodec.unit(FairyRingFeature::new);

    @Override
    public MapCodec<FairyRingFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!level.getBlockState(origin.below()).is(FaeBlocks.FAE_GRASS.get()) || !level.getFluidState(origin).isEmpty()) {
            return false;
        }
        double radius = 3.0 + random.nextDouble() * 2.2;
        int ir = (int) Math.ceil(radius) + 1;
        // nur auf (fast) ebenem Grund
        for (int dx = -ir; dx <= ir; dx += ir) {
            for (int dz = -ir; dz <= ir; dz += ir) {
                int y = FaeBuild.surface(level, origin.getX() + dx, origin.getZ() + dz);
                if (Math.abs(y - origin.getY()) > 2) {
                    return false;
                }
            }
        }
        BlockState moss = FaeBlocks.FAE_MOSS.get().defaultBlockState();
        BlockState lantern = FaeBlocks.LANTERN_MUSHROOM.get().defaultBlockState();
        BlockState bell = FaeBlocks.MOONBELL.get().defaultBlockState();
        for (int dx = -ir; dx <= ir; dx++) {
            for (int dz = -ir; dz <= ir; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                BlockPos top = new BlockPos(x, FaeBuild.surface(level, x, z), z);
                if (Math.abs(top.getY() - origin.getY()) > 2 || !FaeBuild.inReach(origin, top)) {
                    continue;
                }
                BlockPos ground = top.below();
                BlockState under = level.getBlockState(ground);
                if (!under.is(FaeBlocks.FAE_GRASS.get()) && !under.is(FaeBlocks.FAE_SOIL.get())) {
                    continue;
                }
                if (Math.abs(d - radius) < 0.55) {
                    // der Ring selbst: dicht an dicht Laternenpilze
                    clear(level, top);
                    if (random.nextFloat() < 0.88f) {
                        FaeBuild.plant(level, origin, top, lantern);
                    }
                } else if (d < radius - 0.6) {
                    // innen: Moos statt Gras, hier und da eine Mondglocke
                    level.setBlock(ground, moss, 2);
                    clear(level, top);
                    if (d > 1.3 && random.nextFloat() < 0.12f) {
                        FaeBuild.plant(level, origin, top, bell);
                    }
                }
            }
        }
        if (random.nextFloat() < 0.4f) {
            // der Feenstein in der Mitte
            BlockPos base = new BlockPos(origin.getX(), FaeBuild.surface(level, origin.getX(), origin.getZ()), origin.getZ());
            clear(level, base);
            FaeBuild.place(level, origin, base, FaeBlocks.MOSSY_FAESTONE_BRICKS.get().defaultBlockState());
            FaeBuild.place(level, origin, base.above(), FaeBlocks.CHISELED_MOONSTONE.get().defaultBlockState());
        }
        return true;
    }

    /** Gras und Blumen weg, damit Ring und Moos frei liegen. */
    private static void clear(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.isAir() && state.canBeReplaced() && state.getFluidState().isEmpty()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
        BlockState above = level.getBlockState(pos.above());
        if (!above.isAir() && above.canBeReplaced() && above.getFluidState().isEmpty()) {
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
        }
    }
}
