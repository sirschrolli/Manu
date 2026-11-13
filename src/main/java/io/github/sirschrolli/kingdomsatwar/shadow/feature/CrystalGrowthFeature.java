package io.github.sirschrolli.kingdomsatwar.shadow.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Nachtkristalle an Fels: Rund um den Ausgangspunkt sucht das Feature freie Stellen an Böden, Wänden und Decken von
 * Schluchten und Höhlen und lässt dort Kristalle wachsen – Drusen am Boden, spitze Kristallsäulen aus dem Boden, aus
 * den Wänden ragende Kristallzapfen und von der Decke hängende Spitzen.
 */
public record CrystalGrowthFeature(int tries, int spread) implements Feature {
    public static final MapCodec<CrystalGrowthFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.intRange(1, 512).fieldOf("tries").forGetter(CrystalGrowthFeature::tries),
            Codec.intRange(1, 15).fieldOf("spread").forGetter(CrystalGrowthFeature::spread)
    ).apply(i, CrystalGrowthFeature::new));

    @Override
    public MapCodec<CrystalGrowthFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        boolean any = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < this.tries; i++) {
            pos.setWithOffset(origin, random.nextInt(this.spread * 2 + 1) - this.spread, random.nextInt(this.spread * 2 + 1) - this.spread,
                    random.nextInt(this.spread * 2 + 1) - this.spread);
            if (!level.isEmptyBlock(pos)) {
                continue;
            }
            if (isRock(level.getBlockState(pos.below()))) {
                any |= this.floor(level, random, pos.immutable());
            } else if (isRock(level.getBlockState(pos.above()))) {
                any |= this.ceiling(level, random, pos.immutable());
            } else {
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    if (isRock(level.getBlockState(pos.relative(side)))) {
                        any |= this.wall(level, random, pos.immutable(), side.getOpposite());
                        break;
                    }
                }
            }
        }
        return any;
    }

    private static boolean isRock(BlockState state) {
        return state.is(ShadowBlocks.SHADOW_STONE.base().get()) || state.is(ShadowBlocks.DEEP_SHADOW_STONE.get())
                || state.is(ShadowBlocks.NIGHT_CRYSTAL.get());
    }

    private boolean floor(WorldGenLevel level, RandomSource random, BlockPos pos) {
        if (random.nextInt(5) != 0) {
            level.setBlock(pos, ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get().defaultBlockState(), 2);
            return true;
        }
        int height = 2 + random.nextInt(4);
        for (int k = 0; k < height && level.isEmptyBlock(pos.above(k)); k++) {
            level.setBlock(pos.above(k), ShadowBlocks.NIGHT_CRYSTAL.get().defaultBlockState(), 2);
            if (k == 0) {
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    if (random.nextInt(3) == 0 && level.isEmptyBlock(pos.relative(side))) {
                        level.setBlock(pos.relative(side), ShadowBlocks.NIGHT_CRYSTAL.get().defaultBlockState(), 2);
                    }
                }
            }
        }
        if (level.isEmptyBlock(pos.above(height))) {
            level.setBlock(pos.above(height), ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get().defaultBlockState(), 2);
        }
        return true;
    }

    private boolean ceiling(WorldGenLevel level, RandomSource random, BlockPos pos) {
        int length = 1 + random.nextInt(3);
        for (int k = 0; k < length && level.isEmptyBlock(pos.below(k)); k++) {
            level.setBlock(pos.below(k), ShadowBlocks.NIGHT_CRYSTAL.get().defaultBlockState(), 2);
        }
        return true;
    }

    private boolean wall(WorldGenLevel level, RandomSource random, BlockPos pos, Direction out) {
        int length = 1 + random.nextInt(3);
        BlockPos at = pos;
        for (int k = 0; k < length; k++) {
            if (!level.isEmptyBlock(at)) {
                break;
            }
            level.setBlock(at, ShadowBlocks.NIGHT_CRYSTAL.get().defaultBlockState(), 2);
            at = at.relative(out);
            if (random.nextBoolean()) {
                at = at.above();
            }
        }
        return true;
    }
}
