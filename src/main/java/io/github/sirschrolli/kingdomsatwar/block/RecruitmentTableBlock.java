package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.network.OpenRecruitScreenPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
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
 * Der Rekrutierungstisch: Ein Klick öffnet die Anwerbe-Liste, in der man Soldaten gegen Smaragde
 * (und für Ritter und Hauptleute etwas Stahl bzw. einen Edelstein) in seinen Dienst nimmt.
 */
public class RecruitmentTableBlock extends HorizontalDirectionalBlock {
    /** Tischplatte und vier Beine. */
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 13, 0, 16, 15, 16),
            Block.box(1, 0, 1, 3, 13, 3), Block.box(13, 0, 1, 15, 13, 3),
            Block.box(1, 0, 13, 3, 13, 15), Block.box(13, 0, 13, 15, 13, 15));

    public RecruitmentTableBlock(Properties properties) {
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
        return SHAPE;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer) {
            io.github.sirschrolli.kingdomsatwar.network.ModNetwork.send(serverPlayer, new OpenRecruitScreenPayload(pos, Recruitment.armyLimit(serverPlayer)));
        }
        return InteractionResult.SUCCESS;
    }
}
