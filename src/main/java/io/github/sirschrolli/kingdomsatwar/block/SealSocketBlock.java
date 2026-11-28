package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.item.SealShardItem;
import io.github.sirschrolli.kingdomsatwar.world.SealCircle;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * Siegelsockel am Fuß eines Monolithen: Hier wird der Siegelsplitter des passenden Drachen eingesetzt (Rechtsklick mit
 * dem Splitter). Dann erwacht der Monolith, seine Runen und die Einlagen im Boden leuchten. Steckt der sechste
 * Splitter, bricht das Siegel ({@link SealCircle}).
 */
public class SealSocketBlock extends HorizontalDirectionalBlock {
    public static final EnumProperty<DragonType> ELEMENT = SealMarkBlock.ELEMENT;
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    public SealSocketBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ELEMENT, DragonType.FIRE)
                .setValue(FILLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ELEMENT, FILLED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!(stack.getItem() instanceof SealShardItem shard)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        DragonType element = state.getValue(ELEMENT);
        if (state.getValue(FILLED)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (shard.element() != element) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.seal_socket.wrong_shard",
                        Component.translatable("lair.kingdomsatwar." + element.id() + ".dragon").withColor(element.color()))
                        .withStyle(ChatFormatting.GRAY));
            }
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel server && player instanceof ServerPlayer serverPlayer) {
            if (!SealCircle.insert(server, pos, state, serverPlayer)) {
                return InteractionResult.FAIL;
            }
            stack.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            DragonType element = state.getValue(ELEMENT);
            Component dragon = Component.translatable("lair.kingdomsatwar." + element.id() + ".dragon").withColor(element.color());
            player.sendOverlayMessage(state.getValue(FILLED)
                    ? Component.translatable("block.kingdomsatwar.seal_socket.filled", dragon).withStyle(ChatFormatting.GOLD)
                    : Component.translatable("block.kingdomsatwar.seal_socket.empty", dragon).withStyle(ChatFormatting.GRAY));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING);
        double x = pos.getX() + 0.5 + facing.getStepX() * 0.55;
        double y = pos.getY() + 0.45;
        double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.55;
        if (state.getValue(FILLED)) {
            if (random.nextInt(3) == 0) {
                level.addParticle(state.getValue(ELEMENT).particle(), x, y, z, facing.getStepX() * 0.02, 0.03, facing.getStepZ() * 0.02);
            }
        } else if (random.nextInt(8) == 0) {
            // ein leiser Sog: das Siegel wartet auf seinen Splitter
            level.addParticle(ParticleTypes.ENCHANT, x + facing.getStepX() * 1.2, y + 0.6, z + facing.getStepZ() * 1.2,
                    -facing.getStepX() * 0.6, -0.4, -facing.getStepZ() * 0.6);
        }
    }
}
