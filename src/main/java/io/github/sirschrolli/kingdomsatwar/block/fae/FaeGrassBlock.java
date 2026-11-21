package io.github.sirschrolli.kingdomsatwar.block.fae;

import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

/**
 * Feengras: weiches, mintgrünes Gras, in dem winzige Blüten stecken. Es breitet sich im ewigen Abendrot über die
 * Feenerde aus; zugedeckt stirbt es ab. Mit Knochenmehl sprießen Feenhalme, Feenrosen, Mondglocken und Laternenpilze.
 */
public class FaeGrassBlock extends Block implements BonemealableBlock {
    public FaeGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    private static boolean canStayAlive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.getFluidState().isFull()) {
            return false;
        }
        return LightEngine.getLightDampeningInto(state, aboveState, Direction.UP, aboveState.getLightDampening()) < 15;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canStayAlive(state, level, pos)) {
            if (level.isAreaLoaded(pos, 1)) {
                level.setBlockAndUpdate(pos, FaeBlocks.FAE_SOIL.get().defaultBlockState());
            }
            return;
        }
        if (!level.isAreaLoaded(pos, 3)) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
            if (level.getBlockState(target).is(FaeBlocks.FAE_SOIL.get()) && canStayAlive(state, level, target)
                    && !level.getFluidState(target.above()).is(FluidTags.WATER)) {
                level.setBlockAndUpdate(target, this.defaultBlockState());
            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        for (int i = 0; i < 48; i++) {
            BlockPos target = pos.offset(random.nextInt(7) - 3, random.nextInt(3) - 1, random.nextInt(7) - 3);
            if (!level.getBlockState(target).is(this) || !level.getBlockState(target.above()).isAir()) {
                continue;
            }
            // meist Feenhalme, dazu Blumen und Laternenpilze, selten eine Sternspitze
            int roll = random.nextInt(28);
            BlockState plant = (roll == 0 ? FaeBlocks.STARLACE.get() : roll <= 2 ? FaeBlocks.MOONBELL.get() : roll <= 4 ? FaeBlocks.FAE_ROSE.get()
                    : roll <= 6 ? FaeBlocks.LANTERN_MUSHROOM.get() : FaeBlocks.FAE_SPROUTS.get()).defaultBlockState();
            if (random.nextInt(3) != 0 && plant.canSurvive(level, target.above())) {
                if (plant.getBlock() instanceof DoublePlantBlock) {
                    if (level.isEmptyBlock(target.above(2))) {
                        DoublePlantBlock.placeAt(level, plant, target.above(), Block.UPDATE_ALL);
                    }
                } else {
                    level.setBlock(target.above(), plant, Block.UPDATE_ALL);
                }
            }
        }
    }
}
