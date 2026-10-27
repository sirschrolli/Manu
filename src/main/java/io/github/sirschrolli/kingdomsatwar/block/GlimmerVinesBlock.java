package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Glimmerranken: hängen von Laub und Höhlendecken herab und wachsen langsam nach unten; an ihren Spitzen glimmt eine
 * silberblaue Knospe. Man kann an ihnen klettern.
 */
public class GlimmerVinesBlock extends GrowingPlantHeadBlock {
    private static final VoxelShape SHAPE = Block.column(8.0, 9.0, 16.0);

    public GlimmerVinesBlock(BlockBehaviour.Properties properties) {
        super(properties, Direction.DOWN, SHAPE, false, 0.08);
    }

    @Override
    protected int getBlocksToGrowWhenBonemealed(RandomSource random) {
        return 1 + random.nextInt(3);
    }

    @Override
    protected Block getBodyBlock() {
        return ShadowBlocks.GLIMMER_VINES_PLANT.get();
    }

    @Override
    protected boolean canGrowInto(BlockState state) {
        return state.isAir();
    }
}
