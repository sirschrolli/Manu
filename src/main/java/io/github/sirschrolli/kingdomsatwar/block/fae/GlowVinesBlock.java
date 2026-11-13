package io.github.sirschrolli.kingdomsatwar.block.fae;

import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Leuchtranke: hängt in Schleiern aus den Kronen der Glimmerbäume; ihre Spitze trägt eine leuchtende Knospe. */
public class GlowVinesBlock extends GrowingPlantHeadBlock {
    private static final VoxelShape SHAPE = Block.column(8.0, 9.0, 16.0);

    public GlowVinesBlock(BlockBehaviour.Properties properties) {
        super(properties, Direction.DOWN, SHAPE, false, 0.08);
    }

    @Override
    protected int getBlocksToGrowWhenBonemealed(RandomSource random) {
        return 1 + random.nextInt(3);
    }

    @Override
    protected Block getBodyBlock() {
        return FaeBlocks.GLOW_VINES_PLANT.get();
    }

    @Override
    protected boolean canGrowInto(BlockState state) {
        return state.isAir();
    }
}
