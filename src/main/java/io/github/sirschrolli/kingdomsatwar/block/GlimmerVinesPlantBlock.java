package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Der Strang der Glimmerranken (zwischen Decke und glimmender Spitze). */
public class GlimmerVinesPlantBlock extends GrowingPlantBodyBlock {
    private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 16.0);

    public GlimmerVinesPlantBlock(BlockBehaviour.Properties properties) {
        super(properties, Direction.DOWN, SHAPE, false);
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return ShadowBlocks.GLIMMER_VINES.get();
    }
}
