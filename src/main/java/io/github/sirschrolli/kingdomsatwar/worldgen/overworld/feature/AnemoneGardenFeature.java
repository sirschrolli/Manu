package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.block.sea.SeaAnemoneBlock;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * Ein Anemonengarten am Grund warmer Meere: ein paar flache Felsbrocken, auf und zwischen ihnen Seeanemonen in ein, zwei
 * Farben – dichter in der Mitte, lockerer zum Rand –, dazu Seegurken und Seegras. Nachts schimmert der Garten.
 */
public record AnemoneGardenFeature() implements Feature {
    public static final MapCodec<AnemoneGardenFeature> CODEC = MapCodec.unit(AnemoneGardenFeature::new);

    @Override
    public MapCodec<AnemoneGardenFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        List<DeferredBlock<SeaAnemoneBlock>> all = SeaBlocks.anemones();
        BlockState main = all.get(random.nextInt(all.size())).get().defaultBlockState();
        BlockState second = all.get(random.nextInt(all.size())).get().defaultBlockState();
        int radius = 5 + random.nextInt(4);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        // ein paar flache Brocken als Inseln des Gartens
        int rocks = 1 + random.nextInt(3);
        for (int i = 0; i < rocks; i++) {
            int x = origin.getX() + random.nextInt(radius) - radius / 2;
            int z = origin.getZ() + random.nextInt(radius) - radius / 2;
            int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            this.rock(level, random, new BlockPos(x, floor, z));
        }
        int placed = 0;
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
                if (!level.getBlockState(pos).is(Blocks.WATER) || !level.getBlockState(pos.above()).is(Blocks.WATER)) {
                    continue;
                }
                BlockPos below = pos.below();
                if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
                    continue;
                }
                float r = random.nextFloat();
                if (r < 0.42f * (1.0 - d * 0.6)) {
                    level.setBlock(pos, random.nextFloat() < 0.7f ? main : second, 2);
                    placed++;
                } else if (r < 0.5f && level.getBlockState(below).is(BlockTags.SAND)) {
                    level.setBlock(pos, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 1 + random.nextInt(3)), 2);
                } else if (r < 0.62f) {
                    level.setBlock(pos, Blocks.SEAGRASS.defaultBlockState(), 2);
                }
            }
        }
        return placed > 0;
    }

    /** Ein flacher Brocken aus Stein und Andesit (zwei Blöcke hoch, am Rand einer). */
    private void rock(WorldGenLevel level, RandomSource random, BlockPos base) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        double r = 1.4 + random.nextDouble() * 0.9;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > r) {
                    continue;
                }
                int h = d < r * 0.5 ? 2 : 1;
                for (int y = 0; y < h; y++) {
                    pos.set(base.getX() + dx, base.getY() + y, base.getZ() + dz);
                    if (level.getBlockState(pos).is(Blocks.WATER)) {
                        level.setBlock(pos, random.nextFloat() < 0.6f ? Blocks.STONE.defaultBlockState() : Blocks.ANDESITE.defaultBlockState(), 2);
                    }
                }
            }
        }
    }
}
