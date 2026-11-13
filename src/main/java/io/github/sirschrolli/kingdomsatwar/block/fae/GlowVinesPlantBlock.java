package io.github.sirschrolli.kingdomsatwar.block.fae;

import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Leuchtranke (Stängel): die Glieder über der Spitze. */
public class GlowVinesPlantBlock extends GrowingPlantBodyBlock {
    private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 16.0);

    public GlowVinesPlantBlock(BlockBehaviour.Properties properties) {
        super(properties, Direction.DOWN, SHAPE, false);
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return FaeBlocks.GLOW_VINES.get();
    }
}
