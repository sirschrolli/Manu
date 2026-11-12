package io.github.sirschrolli.kingdomsatwar.abyss.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Eine <b>Weltennaht</b> über den Boden: eine zackige, glühende Fuge, die dem Gelände folgt. Seit 1.01 gerinnt an ihr die
 * Zeit zu <b>Chronit</b>: unter manchem Stück der Naht und hier und da daneben steckt Chronit-Erz.
 */
public record SeamVeinFeature() implements Feature {
    public static final MapCodec<SeamVeinFeature> CODEC = MapCodec.unit(SeamVeinFeature::new);

    /** So oft steckt unter einem Stück der Naht Chronit, so oft daneben. */
    public static final float CHRONIT_BELOW = 0.14f;
    public static final float CHRONIT_BESIDE = 0.05f;

    private static void chronit(WorldGenLevel level, BlockPos origin, BlockPos pos) {
        if (AbyssBuild.inReach(origin, pos) && level.getBlockState(pos).isSolidRender()
                && !level.getBlockState(pos).is(AbyssBlocks.WORLD_SEAM.get())) {
            level.setBlock(pos, io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.CHRONIT_ORE.get().defaultBlockState(), 2);
        }
    }

    @Override
    public MapCodec<SeamVeinFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        BlockState seam = AbyssBlocks.WORLD_SEAM.get().defaultBlockState();
        BlockPos pos = origin.below();
        if (!level.getBlockState(pos).isSolidRender()) {
            return false;
        }
        Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int length = 6 + random.nextInt(12);
        int placed = 0;
        for (int i = 0; i < length; i++) {
            if (AbyssBuild.inReach(origin, pos) && level.getBlockState(pos).isSolidRender()) {
                level.setBlock(pos, seam, 2);
                placed++;
                // geronnene Zeit: Chronit unter der Naht und ab und zu daneben
                if (random.nextFloat() < CHRONIT_BELOW) {
                    chronit(level, origin, pos.below());
                }
                if (random.nextFloat() < CHRONIT_BESIDE) {
                    chronit(level, origin, pos.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random)));
                }
            }
            if (random.nextInt(3) == 0) {
                dir = random.nextBoolean() ? dir.getClockWise() : dir.getCounterClockWise();
            }
            BlockPos next = pos.relative(dir);
            // dem Gelände folgen: ein Block höher oder tiefer, wenn nötig
            if (!level.getBlockState(next).isSolidRender()) {
                next = next.below();
            } else if (level.getBlockState(next.above()).isSolidRender()) {
                next = next.above();
            }
            if (!level.getBlockState(next).isSolidRender()) {
                break;
            }
            pos = next;
        }
        return placed > 0;
    }
}
