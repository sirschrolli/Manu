package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.entity.SeatEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Sitzmöbel (Stuhl, Bank, Thron): steht mit der Vorderseite zum Spieler, der es aufstellt. Ein Klick mit leerer
 * Hand setzt einen darauf; Schleichen und Klicken setzt das Möbelstück stattdessen ganz normal ein.
 */
public class SeatBlock extends HorizontalDirectionalBlock {
    private final Map<Direction, VoxelShape> shapes;
    private final double seatHeight;

    /**
     * @param northShape Form mit der Vorderseite nach Norden (Lehne im Süden)
     * @param seatHeight Höhe der Sitzfläche in Pixeln (1/16 Block)
     */
    public SeatBlock(VoxelShape northShape, double seatHeight, Properties properties) {
        super(properties);
        this.shapes = Shapes.rotateHorizontal(northShape);
        this.seatHeight = seatHeight / 16.0;
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
        return this.shapes.get(state.getValue(FACING));
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    /** Höhe der Sitzfläche über dem Boden (in Blöcken). */
    public double seatHeight() {
        return this.seatHeight;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isSecondaryUseActive() || player.isPassenger()) {
            return InteractionResult.PASS;
        }
        if (SeatEntity.isOccupied(level, pos)) {
            return refuse(level, player, "occupied");
        }
        // Nur mit freiem Platz über der Sitzfläche (sonst stäke der Kopf in der Decke)
        if (level.getBlockState(pos.above()).isSuffocating(level, pos.above())) {
            return refuse(level, player, "obstructed");
        }
        boolean sat = SeatEntity.sit(level, pos, this.seatHeight, state.getValue(FACING), player);
        if (sat && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer && state.is(ModBlocks.THRONE.get())) {
            // auf dem Thron des eigenen Thronsaals beginnt (wenn alles erfüllt ist) die Krönung
            io.github.sirschrolli.kingdomsatwar.economy.town.Coronation.onSit(serverPlayer, pos);
        }
        return sat ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private static InteractionResult refuse(Level level, Player player, String reason) {
        if (!level.isClientSide()) {
            player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.seat." + reason));
        }
        return InteractionResult.CONSUME;
    }
}
