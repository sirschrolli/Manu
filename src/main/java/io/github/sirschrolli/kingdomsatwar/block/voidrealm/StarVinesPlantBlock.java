package io.github.sirschrolli.kingdomsatwar.block.voidrealm;

import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Der Stängel der Sternenranke (zwischen Ansatz und Spitze). */
public class StarVinesPlantBlock extends GrowingPlantBodyBlock {
    private static final VoxelShape SHAPE = Block.column(2.0, 0.0, 16.0);

    public StarVinesPlantBlock(BlockBehaviour.Properties properties) {
        super(properties, Direction.DOWN, SHAPE, false);
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return VoidBlocks.STAR_VINES.get();
    }
}
