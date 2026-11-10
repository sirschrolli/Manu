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

/** Eine <b>Weltennaht</b> über den Boden: eine zackige, glühende Fuge, die dem Gelände folgt. */
public record SeamVeinFeature() implements Feature {
    public static final MapCodec<SeamVeinFeature> CODEC = MapCodec.unit(SeamVeinFeature::new);

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
