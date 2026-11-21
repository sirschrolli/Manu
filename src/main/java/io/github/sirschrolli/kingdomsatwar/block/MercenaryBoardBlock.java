package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.block.entity.MercenaryBoardBlockEntity;
import io.github.sirschrolli.kingdomsatwar.network.OpenMercenaryBoardPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Die Söldnertafel im Söldnerhaus eines Dorfs: zwei Pfosten mit einem Brett voller Anschläge. Ein Klick zeigt, welche
 * Söldner heute da sind, was sie können und was sie kosten; jeden Morgen kommen neue, bis wieder drei warten.
 */
public class MercenaryBoardBlock extends HorizontalDirectionalBlock implements EntityBlock {
    /** Pfosten und Brett (Blick nach Norden: die Anschläge zeigen nach Norden). */
    private static final VoxelShape NORTH_SOUTH = Shapes.or(
            Block.box(0, 0, 7, 2, 15, 9), Block.box(14, 0, 7, 16, 15, 9), Block.box(2, 3, 7.5, 14, 15, 8.5), Block.box(0, 15, 6.5, 16, 16, 9.5));
    private static final VoxelShape EAST_WEST = Shapes.or(
            Block.box(7, 0, 0, 9, 15, 2), Block.box(7, 0, 14, 9, 15, 16), Block.box(7.5, 3, 2, 8.5, 15, 14), Block.box(6.5, 15, 0, 9.5, 16, 16));

    public MercenaryBoardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? NORTH_SOUTH : EAST_WEST;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel server
                && level.getBlockEntity(pos) instanceof MercenaryBoardBlockEntity board) {
            // wer zum ersten Mal vorbeischaut, soll nicht vor einer leeren Tafel stehen
            board.refreshIfDue(server);
            io.github.sirschrolli.kingdomsatwar.network.ModNetwork.send(serverPlayer,
                    new OpenMercenaryBoardPayload(pos, Recruitment.armyLimit(serverPlayer), board.village()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MercenaryBoardBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.MERCENARY_BOARD.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof MercenaryBoardBlockEntity board) {
                board.serverTick(server);
            }
        };
    }
}
