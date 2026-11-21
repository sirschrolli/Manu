package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Ein Tangwald: auf einer runden Fläche (Radius 7 bis 11) wachsen dicht an dicht Tangstängel vom Grund bis knapp unter
 * die Oberfläche – Riesentang mit goldbraunen Wedeln (Anteil {@code giantShare}), dazwischen gewöhnlicher Seetang und am
 * Boden Seegras. Zum Rand hin lichtet sich der Wald.
 */
public record KelpForestFeature(float giantShare) implements Feature {
    public static final MapCodec<KelpForestFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.floatRange(0.0f, 1.0f).fieldOf("giant_share").forGetter(KelpForestFeature::giantShare)
    ).apply(i, KelpForestFeature::new));

    @Override
    public MapCodec<KelpForestFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int radius = 7 + random.nextInt(5);
        int placed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz) / radius;
                if (d > 1.0) {
                    continue;
                }
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
                pos.set(x, floor, z);
                if (!level.getBlockState(pos).is(Blocks.WATER)) {
                    continue;
                }
                BlockState ground = level.getBlockState(pos.below());
                if (!ground.isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP) || ground.is(BlockTags.ICE)) {
                    continue;
                }
                float density = (float) (0.62 - d * 0.45);
                if (random.nextFloat() < density) {
                    if (this.column(level, random, pos.immutable())) {
                        placed++;
                    }
                } else if (random.nextFloat() < 0.35f) {
                    this.seagrass(level, random, pos.immutable());
                }
            }
        }
        return placed > 0;
    }

    /** Ein Stängel vom Grund bis ein, zwei Blöcke unter die Oberfläche. */
    private boolean column(WorldGenLevel level, RandomSource random, BlockPos bottom) {
        int water = 0;
        while (water < 48 && level.getBlockState(bottom.above(water)).is(Blocks.WATER)
                && level.getBlockState(bottom.above(water + 1)).is(Blocks.WATER)) {
            water++;
        }
        if (water < 3) {
            return false;
        }
        boolean giant = random.nextFloat() < this.giantShare;
        int height = giant ? water - random.nextInt(2) : Math.max(2, water - 1 - random.nextInt(Math.max(1, water / 3)));
        Block head = giant ? SeaBlocks.GIANT_KELP.get() : Blocks.KELP;
        Block body = giant ? SeaBlocks.GIANT_KELP_PLANT.get() : Blocks.KELP_PLANT;
        for (int i = 0; i < height; i++) {
            BlockPos pos = bottom.above(i);
            BlockState state = i == height - 1
                    ? head.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, 20 + random.nextInt(4))
                    : body.defaultBlockState();
            level.setBlock(pos, state, 2);
        }
        return true;
    }

    private void seagrass(WorldGenLevel level, RandomSource random, BlockPos pos) {
        if (random.nextFloat() < 0.3f && level.getBlockState(pos.above()).is(Blocks.WATER)) {
            level.setBlock(pos, Blocks.TALL_SEAGRASS.defaultBlockState().setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.LOWER), 2);
            level.setBlock(pos.above(), Blocks.TALL_SEAGRASS.defaultBlockState().setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER), 2);
        } else {
            level.setBlock(pos, Blocks.SEAGRASS.defaultBlockState(), 2);
        }
    }
}
