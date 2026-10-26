package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

/**
 * Schattengras: Anders als das Gras der Oberwelt braucht es kein helles Licht – es breitet sich im ewigen Zwielicht
 * über die Schattenerde aus. Wird es zugedeckt, stirbt es ab. Mit Knochenmehl sprießen Schattenhalme und Geisterpilze.
 */
public class ShadowGrassBlock extends Block implements BonemealableBlock {
    public ShadowGrassBlock(BlockBehaviour.Properties properties) {
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
                level.setBlockAndUpdate(pos, ShadowBlocks.SHADOW_SOIL.get().defaultBlockState());
            }
            return;
        }
        if (!level.isAreaLoaded(pos, 3)) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
            if (level.getBlockState(target).is(ShadowBlocks.SHADOW_SOIL.get()) && canStayAlive(state, level, target)
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
            BlockState plant = random.nextInt(9) == 0 ? ShadowBlocks.GHOSTCAP.get().defaultBlockState()
                    : ShadowBlocks.SHADOW_SPROUTS.get().defaultBlockState();
            if (random.nextInt(3) != 0 && plant.canSurvive(level, target.above())) {
                level.setBlock(target.above(), plant, Block.UPDATE_ALL);
            }
        }
    }
}
