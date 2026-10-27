package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownActions;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownManager;
import io.github.sirschrolli.kingdomsatwar.economy.town.Towns;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Das Stadtbanner: ein zwei Blöcke hohes Banner an einem Mast auf einem Steinsockel. Wer es aufstellt, gründet seine
 * Siedlung (jeder Spieler hat eine); Rechtsklick öffnet das Stadtfenster. Nur der Gründer kann es abbauen – die
 * Siedlung bleibt dann bestehen, bis wieder ein Banner steht.
 */
public class TownBannerBlock extends HorizontalDirectionalBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    /** Formen mit dem Tuch nach Norden: Sockel, Mast, Querstange und Tuch (vor dem Mast). */
    private static final Map<Direction, VoxelShape> LOWER = Shapes.rotateHorizontal(Shapes.or(
            Block.box(3, 0, 3, 13, 2, 13), Block.box(4, 2, 4, 12, 4, 12), Block.box(7, 4, 7, 9, 16, 9),
            Block.box(2, 6, 6, 14, 16, 6.75)));
    private static final Map<Direction, VoxelShape> UPPER = Shapes.rotateHorizontal(Shapes.or(
            Block.box(7, 0, 7, 9, 16, 9), Block.box(1, 11.75, 5.5, 15, 13.5, 7.25), Block.box(2, 0, 6, 14, 12, 6.75)));
    /** Man stößt sich nur am Sockel und am Mast, nicht am Tuch. */
    private static final VoxelShape LOWER_COLLISION = Shapes.or(Block.box(3, 0, 3, 13, 2, 13), Block.box(4, 2, 4, 12, 4, 12),
            Block.box(7, 4, 7, 9, 16, 9));
    private static final VoxelShape UPPER_COLLISION = Block.box(7, 0, 7, 9, 16, 9);

    public TownBannerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER : UPPER).get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_COLLISION : UPPER_COLLISION;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxY() || !level.getBlockState(pos.above()).canBeReplaced(context)) {
            return null;
        }
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
        if (level instanceof ServerLevel server && placer instanceof ServerPlayer player) {
            TownManager.FoundResult result = TownManager.placeBanner(player, server, pos);
            switch (result) {
                case HAS_BANNER, TOO_CLOSE -> {
                    // nicht erlaubt: Banner wieder abbauen und zurückgeben
                    level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                    if (!player.hasInfiniteMaterials()) {
                        player.getInventory().placeItemBackInInventory(new ItemStack(this), Prediction.SERVER_ONLY);
                    }
                    Town own = Towns.of(server.getServer()).byOwner(player.getUUID());
                    if (result == TownManager.FoundResult.HAS_BANNER && own != null) {
                        player.sendSystemMessage(Component.translatable(result.key(), Component.literal(own.name()),
                                own.banner().getX(), own.banner().getY(), own.banner().getZ()).withStyle(ChatFormatting.RED));
                    } else {
                        player.sendSystemMessage(Component.translatable(result.key()).withStyle(ChatFormatting.RED));
                    }
                }
                case MOVED -> {
                    Town own = Towns.of(server.getServer()).byOwner(player.getUUID());
                    if (own != null) {
                        player.sendSystemMessage(Component.translatable(result.key(), Component.literal(own.name())).withStyle(ChatFormatting.GREEN));
                    }
                }
                default -> {
                }
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        if (level instanceof ServerLevel server && player instanceof ServerPlayer serverPlayer) {
            Town town = Towns.of(server.getServer()).byBanner(server.dimension(), lower);
            if (town == null) {
                player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.town.no_town").withStyle(ChatFormatting.GRAY));
            } else {
                TownActions.open(serverPlayer, town, lower);
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** Nur der Gründer der Siedlung kann ihr Banner abbauen. */
    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter getter, BlockPos pos) {
        if (getter instanceof ServerLevel server) {
            BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
            Town town = Towns.of(server.getServer()).byBanner(server.dimension(), lower);
            if (town != null && !town.owner().equals(player.getUUID()) && !player.hasInfiniteMaterials()) {
                return 0.0f;
            }
        }
        return super.getDestroyProgress(state, player, getter, pos);
    }

    /** Im Kreativmodus fällt beim Abbau der oberen Hälfte nichts heraus (wie bei Türen). */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.preventsBlockDrops() && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos below = pos.below();
            BlockState lower = level.getBlockState(below);
            if (lower.is(this) && lower.getValue(HALF) == DoubleBlockHalf.LOWER) {
                level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, LevelEvent.PARTICLES_AND_SOUND_DESTROY_BLOCK, below, Block.getId(lower));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** Ist das Banner weg (abgebaut, gesprengt, ...), bleibt die Siedlung bestehen, bis wieder eines steht. */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            TownManager.bannerRemoved(level, pos);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP)) {
            return neighbor.is(this) && neighbor.getValue(HALF) != half ? state : Blocks.AIR.defaultBlockState();
        }
        if (half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !this.canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }
}
