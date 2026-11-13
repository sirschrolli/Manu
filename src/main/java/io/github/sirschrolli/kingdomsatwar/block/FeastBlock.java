package io.github.sirschrolli.kingdomsatwar.block;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Festmahl: ein Braten mit Beilagen auf einem Holzbrett – zum Aufstellen auf der Tafel. Wie ein Kuchen isst man
 * direkt vom Block; vier üppige Portionen, dann ist nur noch das Brett leer und verschwindet.
 */
public class FeastBlock extends HorizontalDirectionalBlock {
    public static final int SERVINGS = 4;
    public static final IntegerProperty EATEN = IntegerProperty.create("servings_eaten", 0, SERVINGS - 1);
    /** Sättigung je Portion (wie ein gebratenes Kotelett und etwas mehr). */
    public static final int NUTRITION = 6;
    public static final float SATURATION = 0.8f;
    private static final VoxelShape BOARD = Block.box(1, 0, 1, 15, 1, 15);
    @SuppressWarnings("unchecked")
    private static final Map<Direction, VoxelShape>[] SHAPES = new Map[SERVINGS];

    static {
        for (int eaten = 0; eaten < SERVINGS; eaten++) {
            // der Braten wird von Westen her angeschnitten (Modell mit Blick nach Norden)
            SHAPES[eaten] = Shapes.rotateHorizontal(Shapes.or(BOARD, Block.box(4 + eaten * 2, 1, 5, 14.5, 6, 11)));
        }
    }

    public FeastBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(EATEN, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, EATEN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(EATEN)].get(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            if (eat(level, pos, state, player).consumesAction()) {
                return InteractionResult.SUCCESS;
            }
            if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                return InteractionResult.CONSUME;
            }
        }
        return eat(level, pos, state, player);
    }

    /** Eine Portion essen; die letzte räumt das Brett ab. */
    protected static InteractionResult eat(LevelAccessor level, BlockPos pos, BlockState state, Player player) {
        if (!player.canEat(false)) {
            return InteractionResult.PASS;
        }
        player.getFoodData().eat(NUTRITION, SATURATION);
        RandomSource random = level.getRandom();
        level.playSound(player, pos, SoundEvents.GENERIC_EAT.value(), SoundSource.PLAYERS, 1.0f, 0.9f + random.nextFloat() * 0.2f);
        level.gameEvent(player, GameEvent.EAT, pos);
        int eaten = state.getValue(EATEN);
        if (eaten < SERVINGS - 1) {
            level.setBlock(pos, state.setValue(EATEN, eaten + 1), Block.UPDATE_ALL);
        } else {
            level.playSound(player, pos, SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.6f, 0.9f + random.nextFloat() * 0.2f);
            level.removeBlock(pos, false);
            level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        }
        return InteractionResult.SUCCESS;
    }

    /** Steht auf allem mit fester Oberseite – auch auf Tischen. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return directionToNeighbour == Direction.DOWN && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return (SERVINGS - state.getValue(EATEN)) * 3;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}
