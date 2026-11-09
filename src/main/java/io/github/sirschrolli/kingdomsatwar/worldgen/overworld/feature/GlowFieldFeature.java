package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

/**
 * Ein Leuchtfeld in der Tiefsee: Leuchtalgen überziehen den Grund in fleckigen Teppichen (Rauschen), dazwischen stehen
 * Seegurken und einzelne Leuchtkorallen – im Dunkel der Tiefe schimmert der ganze Grund türkis.
 */
public record GlowFieldFeature() implements Feature {
    public static final MapCodec<GlowFieldFeature> CODEC = MapCodec.unit(GlowFieldFeature::new);

    @Override
    public MapCodec<GlowFieldFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int floor0 = level.getHeight(Heightmap.Types.OCEAN_FLOOR, origin.getX(), origin.getZ());
        if (SeaLevel.of(generator) - floor0 < 12) {
            return false;
        }
        SimplexNoise noise = new SimplexNoise(new WorldgenRandom(new LegacyRandomSource(random.nextLong())));
        int radius = 7 + random.nextInt(5);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState algae = SeaBlocks.GLOW_ALGAE.get().defaultBlockState();
        int placed = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz) / radius;
                if (d > 1.0) {
                    continue;
                }
                double n = noise.get(dx * 0.18, dz * 0.18) * 0.5 + 0.5 - d * 0.55;
                if (n < 0.25) {
                    continue;
                }
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
                pos.set(x, floor, z);
                BlockPos below = pos.below();
                if (!level.getBlockState(pos).is(Blocks.WATER) || !level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
                    continue;
                }
                float r = random.nextFloat();
                if (r < 0.04f) {
                    level.setBlock(pos, ModBlocks.GLOW_CORAL.get().defaultBlockState()
                            .setValue(io.github.sirschrolli.kingdomsatwar.block.GlowCoralBlock.WATERLOGGED, true), 2);
                } else if (r < 0.08f) {
                    level.setBlock(pos, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 1 + random.nextInt(4)), 2);
                } else {
                    level.setBlock(pos, algae, 2);
                }
                placed++;
            }
        }
        return placed > 0;
    }
}
