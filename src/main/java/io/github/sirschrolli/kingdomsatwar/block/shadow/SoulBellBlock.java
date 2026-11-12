package io.github.sirschrolli.kingdomsatwar.block.shadow;

import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import io.github.sirschrolli.kingdomsatwar.registry.SoulShepherd;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Die Seelenglocke (seit 0.98): eine schwere, grün angelaufene Glocke im Glockenstuhl des Seelenpferchs, in der ein
 * Seelenfeuer glimmt. Unter ihr steht der Seelenhirte in Andacht; ist er besiegt, verstummt sie und das Feuer erlischt –
 * ein Seelenkristall an ihr ruft ihn zurück. Unzerstörbar.
 */
public class SoulBellBlock extends Block implements EntityBlock {
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);
    private static final VoxelShape SHAPE = Shapes.or(Block.box(4, 4, 4, 12, 13, 12), Block.box(3, 3, 3, 13, 5, 13), Block.box(6, 13, 6, 10, 16, 10));

    /** Der Hirte steht in Andacht (oder ist noch nicht da), er kämpft, er ist besiegt. */
    public enum Phase implements StringRepresentable {
        SLEEPING(9), AWAKE(13), DEFEATED(2);

        public final int light;

        Phase(int light) {
            this.light = light;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public SoulBellBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PHASE, Phase.SLEEPING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PHASE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoulBellBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != SoulShepherd.SOUL_BELL_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof SoulBellBlockEntity bell) {
                bell.serverTick(server);
            }
        };
    }

    /** Ein Seelenkristall an der verstummten Glocke: Sie klingt auf, und der Hirte steigt aus dem Nebel. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(ShadowItems.SOUL_CRYSTAL.get()) || state.getValue(PHASE) != Phase.DEFEATED) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof SoulBellBlockEntity bell) {
            if (!bell.recall(server, player)) {
                return InteractionResult.FAIL;
            }
            stack.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            String key = switch (state.getValue(PHASE)) {
                case SLEEPING -> "block.kingdomsatwar.soul_bell.hint";
                case AWAKE -> "block.kingdomsatwar.soul_bell.awake";
                case DEFEATED -> "block.kingdomsatwar.soul_bell.defeated";
            };
            player.sendOverlayMessage(Component.translatable(key).withStyle(state.getValue(PHASE) == Phase.DEFEATED ? ChatFormatting.GRAY
                    : ChatFormatting.DARK_AQUA));
        }
        return InteractionResult.SUCCESS;
    }

    /** In der Glocke glimmt Seelenfeuer; Seelen steigen unter ihr auf (verstummt nur ab und zu ein Rauchwölkchen). */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Phase phase = state.getValue(PHASE);
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
        if (phase == Phase.DEFEATED) {
            if (random.nextInt(10) == 0) {
                level.addParticle(ParticleTypes.SMOKE, x, pos.getY() + 0.25, z, 0.0, -0.01, 0.0);
            }
            return;
        }
        if (random.nextInt(2) == 0) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, pos.getY() + 0.2, z, 0.0, -0.02, 0.0);
        }
        if (random.nextInt(phase == Phase.AWAKE ? 2 : 6) == 0) {
            level.addParticle(ParticleTypes.SOUL, x, pos.getY() + 0.1, z, 0.0, -0.03, 0.0);
        }
    }
}
