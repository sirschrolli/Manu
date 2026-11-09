package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.block.sea.SeashellBlock;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * Eine Muschelbank: auf Sand und Kies – am Strand wie im flachen Wasser – liegen verstreut Herzmuscheln, Kammmuscheln
 * und Wellhornschnecken, je ein bis drei, mal hier, mal da.
 */
public record ShellBedFeature() implements Feature {
    public static final MapCodec<ShellBedFeature> CODEC = MapCodec.unit(ShellBedFeature::new);

    @Override
    public MapCodec<ShellBedFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        List<DeferredBlock<SeashellBlock>> kinds = SeaBlocks.shells();
        int placed = 0;
        for (int i = 0; i < 18; i++) {
            int x = origin.getX() + random.nextInt(13) - 6;
            int z = origin.getZ() + random.nextInt(13) - 6;
            int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            BlockPos pos = new BlockPos(x, floor, z);
            BlockState here = level.getBlockState(pos);
            boolean water = here.is(Blocks.WATER);
            if (!water && !here.isAir()) {
                continue;
            }
            if (water && !level.getFluidState(pos).isSource()) {
                continue;
            }
            BlockPos below = pos.below();
            BlockState ground = level.getBlockState(below);
            if (!(ground.is(BlockTags.SAND) || ground.is(Blocks.GRAVEL)) || !ground.isFaceSturdy(level, below, Direction.UP)) {
                continue;
            }
            // tief im Meer liegen keine Muschelbänke mehr
            if (water && generator.getSeaLevel() - floor > 12) {
                continue;
            }
            SeashellBlock kind = kinds.get(random.nextInt(kinds.size())).get();
            BlockState shells = kind.defaultBlockState()
                    .setValue(SeashellBlock.SHELLS, 1 + (random.nextFloat() < 0.35f ? 1 : 0) + (random.nextFloat() < 0.12f ? 1 : 0))
                    .setValue(SeashellBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random))
                    .setValue(SeashellBlock.WATERLOGGED, water && level.getFluidState(pos).is(Fluids.WATER));
            level.setBlock(pos, shells, 2);
            placed++;
        }
        return placed > 0;
    }
}
