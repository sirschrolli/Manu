package io.github.sirschrolli.kingdomsatwar.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Holztisch, der sich mit Nachbartischen zu einer langen Tafel verbindet: Beine gibt es nur an den äußeren Ecken.
 */
public class TableBlock extends Block {
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;

    private static final VoxelShape TOP = Block.box(0, 14, 0, 16, 16, 16);
    private static final VoxelShape LEG_NW = Block.box(1, 0, 1, 3, 14, 3);
    private static final VoxelShape LEG_NE = Block.box(13, 0, 1, 15, 14, 3);
    private static final VoxelShape LEG_SW = Block.box(1, 0, 13, 3, 14, 15);
    private static final VoxelShape LEG_SE = Block.box(13, 0, 13, 15, 14, 15);
    /** Form je Verbindungsmuster (Bit 0 Nord, 1 Ost, 2 Süd, 3 West). */
    private static final VoxelShape[] SHAPES = new VoxelShape[16];

    static {
        for (int mask = 0; mask < 16; mask++) {
            boolean n = (mask & 1) != 0, e = (mask & 2) != 0, s = (mask & 4) != 0, w = (mask & 8) != 0;
            VoxelShape shape = TOP;
            if (!n && !w) shape = Shapes.or(shape, LEG_NW);
            if (!n && !e) shape = Shapes.or(shape, LEG_NE);
            if (!s && !w) shape = Shapes.or(shape, LEG_SW);
            if (!s && !e) shape = Shapes.or(shape, LEG_SE);
            SHAPES[mask] = shape.optimize();
        }
    }

    public TableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState neighbour = context.getLevel().getBlockState(context.getClickedPos().relative(side));
            state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side), this.connectsTo(neighbour));
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (directionToNeighbour.getAxis().isHorizontal()) {
            return state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(directionToNeighbour), this.connectsTo(neighbourState));
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    private boolean connectsTo(BlockState neighbour) {
        return neighbour.is(this);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int mask = (state.getValue(NORTH) ? 1 : 0) | (state.getValue(EAST) ? 2 : 0)
                | (state.getValue(SOUTH) ? 4 : 0) | (state.getValue(WEST) ? 8 : 0);
        return SHAPES[mask];
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}
