package io.github.sirschrolli.kingdomsatwar.block.fae;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Seidenfäden: lange, glitzernde Fäden aus Mottenseide, die von Ästen und Laub herabhängen – auch mehrere übereinander.
 * Sie halten sich an jeder festen Unterseite (auch an Laub) und reißen ab, wenn ihr Halt verschwindet.
 */
public class SilkStrandsBlock extends Block {
    private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 16.0);

    public SilkStrandsBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState holder = level.getBlockState(above);
        return holder.is(this) || holder.isFaceSturdy(level, above, Direction.DOWN) || holder.is(net.minecraft.tags.BlockTags.LEAVES);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == Direction.UP && !this.canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }
}
