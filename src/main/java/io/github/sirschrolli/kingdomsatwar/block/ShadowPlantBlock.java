package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Niedrige Pflanze der Schattenwelt (Schattenhalme, Bleichgras): wächst auf Schattengras, Schattenerde und Schattensand
 * – und überall dort, wo auch Gras wächst.
 */
public class ShadowPlantBlock extends VegetationBlock {
    private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 12.0);

    public ShadowPlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.SUPPORTS_VEGETATION) || state.is(ShadowBlocks.SHADOW_SAND.get()) || state.is(ShadowBlocks.ASH_SNOW_BLOCK.get());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.move(state.getOffset(pos));
    }
}
