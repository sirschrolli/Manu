package io.github.sirschrolli.kingdomsatwar.block.crop;

import io.github.sirschrolli.kingdomsatwar.block.ModCropBlock;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Reis (seit 2.0): wächst im seichten Wasser – einen Block tief, über Erde, Gras, Schlamm, Ton oder Ackerland, und nur dort, wo
 * über dem Wasser Luft ist. Die jungen Halme stehen unter Wasser, später ragen sie heraus, reif neigen sich die goldenen
 * Rispen. Das Korn ist zugleich der Samen. Er braucht Sonne (wie Weizen), aber kein Ackerland – das Wasser genügt.
 */
public class RiceBlock extends ModCropBlock implements LiquidBlockContainer {
    private static final VoxelShape[] SHAPES = Block.boxes(7, age -> Block.column(14.0, 0.0, Math.min(16, 4 + age * 2)));

    public RiceBlock(Supplier<? extends ItemLike> seed, BlockBehaviour.Properties properties) {
        super(seed, properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[this.getAge(state)];
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.SUBSTRATE_OVERWORLD) || state.is(Blocks.CLAY) || state.is(BlockTags.SUPPORTS_CROPS);
    }

    /** Auf seinem Grund, im stehenden Wasser, und darüber kein Wasser mehr (seicht). */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        FluidState fluid = level.getFluidState(pos);
        if (!fluid.is(FluidTags.WATER) || !fluid.isSource() || !level.getFluidState(pos.above()).isEmpty()) {
            return false;
        }
        BlockPos below = pos.below();
        BlockState soil = level.getBlockState(below);
        TriState decision = soil.canSustainPlant(level, below, Direction.UP, state);
        return !decision.isDefault() ? decision.isTrue() : this.mayPlaceOn(soil, level, below);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return fluid.is(FluidTags.WATER) && fluid.isFull() ? super.getStateForPlacement(context) : null;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.WATER.defaultBlockState();
        }
        ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getSource(false);
    }

    @Override
    public boolean canPlaceLiquid(@Nullable LivingEntity user, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }

    /** Im Wasser wächst er etwa so schnell wie Weizen auf feuchtem Ackerland – solange die Sonne scheint. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1) || level.getRawBrightness(pos.above(), 0) < 9) {
            return;
        }
        int age = this.getAge(state);
        if (age < this.getMaxAge() && net.neoforged.neoforge.common.CommonHooks.canCropGrow(level, pos, state, random.nextInt(5) == 0)) {
            level.setBlock(pos, this.getStateForAge(age + 1), Block.UPDATE_CLIENTS);
            net.neoforged.neoforge.common.CommonHooks.fireCropGrowPost(level, pos, state);
        }
    }
}
