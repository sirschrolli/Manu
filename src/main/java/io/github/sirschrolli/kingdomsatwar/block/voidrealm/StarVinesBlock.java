package io.github.sirschrolli.kingdomsatwar.block.voidrealm;

import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Sternenranke: hängt in langen, leuchtenden Strähnen unter den Bruchstücken der Leere; ihre Spitze trägt einen kleinen Stern. */
public class StarVinesBlock extends GrowingPlantHeadBlock {
    private static final VoxelShape SHAPE = Block.column(8.0, 9.0, 16.0);

    public StarVinesBlock(BlockBehaviour.Properties properties) {
        super(properties, Direction.DOWN, SHAPE, false, 0.06);
    }

    @Override
    protected int getBlocksToGrowWhenBonemealed(RandomSource random) {
        return 1 + random.nextInt(3);
    }

    @Override
    protected Block getBodyBlock() {
        return VoidBlocks.STAR_VINES_PLANT.get();
    }

    @Override
    protected boolean canGrowInto(BlockState state) {
        return state.isAir();
    }
}
