package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Ein kleines Riff im flachen, warmen Wasser der Korallenküste: bunte Korallen und Korallenfächer, Seegurken in
 * Grüppchen, dazwischen leuchtende Leuchtkorallen und hier und da ein Korallenstock.
 */
public record ReefFeature() implements Feature {
    public static final MapCodec<ReefFeature> CODEC = MapCodec.unit(ReefFeature::new);

    private static final Block[] CORALS = {Blocks.TUBE_CORAL, Blocks.BRAIN_CORAL, Blocks.BUBBLE_CORAL, Blocks.FIRE_CORAL, Blocks.HORN_CORAL};
    private static final Block[] FANS = {Blocks.TUBE_CORAL_FAN, Blocks.BRAIN_CORAL_FAN, Blocks.BUBBLE_CORAL_FAN, Blocks.FIRE_CORAL_FAN,
            Blocks.HORN_CORAL_FAN};
    private static final Block[] CORAL_BLOCKS = {Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK,
            Blocks.FIRE_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK};

    @Override
    public MapCodec<ReefFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        boolean placed = false;
        for (int i = 0; i < 28; i++) {
            int x = origin.getX() + random.nextInt(11) - 5;
            int z = origin.getZ() + random.nextInt(11) - 5;
            int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            BlockPos pos = new BlockPos(x, floor, z);
            int depth = 0;
            while (depth < 8 && level.getBlockState(pos.above(depth)).is(Blocks.WATER)) {
                depth++;
            }
            // nur im flachen Wasser über Sand oder Kies
            if (depth < 1 || depth > 6 || !level.getBlockState(pos).is(Blocks.WATER)) {
                continue;
            }
            BlockState ground = level.getBlockState(pos.below());
            if (!ground.is(BlockTags.SAND) && !ground.is(Blocks.GRAVEL) && !ground.is(BlockTags.CORAL_BLOCKS)) {
                continue;
            }
            float r = random.nextFloat();
            BlockState state;
            if (r < 0.12f && depth >= 2) {
                // ein kleiner Korallenstock, darauf eine Koralle
                Block block = CORAL_BLOCKS[random.nextInt(CORAL_BLOCKS.length)];
                level.setBlock(pos, block.defaultBlockState(), 2);
                pos = pos.above();
                if (!level.getBlockState(pos).is(Blocks.WATER)) {
                    continue;
                }
                state = waterlogged(CORALS[random.nextInt(CORALS.length)].defaultBlockState());
            } else if (r < 0.42f) {
                state = waterlogged(CORALS[random.nextInt(CORALS.length)].defaultBlockState());
            } else if (r < 0.66f) {
                state = waterlogged(FANS[random.nextInt(FANS.length)].defaultBlockState());
            } else if (r < 0.84f) {
                state = Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 1 + random.nextInt(4))
                        .setValue(BlockStateProperties.WATERLOGGED, true);
            } else {
                state = waterlogged(ModBlocks.GLOW_CORAL.get().defaultBlockState());
            }
            if (state.canSurvive(level, pos)) {
                level.setBlock(pos, state, 2);
                placed = true;
            }
        }
        return placed;
    }

    private static BlockState waterlogged(BlockState state) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) ? state.setValue(BlockStateProperties.WATERLOGGED, true) : state;
    }
}
