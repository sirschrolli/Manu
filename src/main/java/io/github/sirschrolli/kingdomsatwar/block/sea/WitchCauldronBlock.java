package io.github.sirschrolli.kingdomsatwar.block.sea;

import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.SeaWitch;
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
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Der Hexenkessel (seit 0.97): ein schwerer, von schwarzer Koralle überwucherter Kessel in der Hexengrotte, in dem ein
 * leuchtender Trank brodelt. Über ihm schwebt die Meerhexe in ihrer Trance; ist sie besiegt, erkaltet der Kessel, und eine
 * Perle hineingeworfen ruft sie zurück. Unzerstörbar.
 */
public class WitchCauldronBlock extends Block implements EntityBlock {
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);
    private static final VoxelShape SHAPE = Shapes.join(Shapes.or(Block.box(1, 0, 1, 15, 14, 15), Block.box(0, 13, 0, 16, 15, 16)),
            Block.box(3, 5, 3, 13, 15, 13), BooleanOp.ONLY_FIRST);

    /** Die Hexe schläft (oder ist noch nicht da), sie kämpft, sie ist besiegt. */
    public enum Phase implements StringRepresentable {
        SLEEPING(10), AWAKE(13), DEFEATED(2);

        public final int light;

        Phase(int light) {
            this.light = light;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public WitchCauldronBlock(BlockBehaviour.Properties properties) {
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
        return new WitchCauldronBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != SeaWitch.WITCH_CAULDRON_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof WitchCauldronBlockEntity cauldron) {
                cauldron.serverTick(server);
            }
        };
    }

    /** Eine Perle in den erkalteten Kessel: Er brodelt auf, und die Meerhexe steigt aus dem Dampf. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(SeaBlocks.PEARL.get()) || state.getValue(PHASE) != Phase.DEFEATED) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof WitchCauldronBlockEntity cauldron) {
            if (!cauldron.recall(server, player)) {
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
                case SLEEPING -> "block.kingdomsatwar.witch_cauldron.hint";
                case AWAKE -> "block.kingdomsatwar.witch_cauldron.awake";
                case DEFEATED -> "block.kingdomsatwar.witch_cauldron.defeated";
            };
            player.sendOverlayMessage(Component.translatable(key).withStyle(state.getValue(PHASE) == Phase.DEFEATED ? ChatFormatting.GRAY
                    : ChatFormatting.DARK_AQUA));
        }
        return InteractionResult.SUCCESS;
    }

    /** Der Trank brodelt: Blasen und leuchtende Tropfen steigen auf (erkaltet nur selten ein dunkles Wölkchen). */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Phase phase = state.getValue(PHASE);
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.55;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.55;
        if (phase == Phase.DEFEATED) {
            if (random.nextInt(8) == 0) {
                level.addParticle(ParticleTypes.SQUID_INK, x, pos.getY() + 0.7, z, 0.0, 0.01, 0.0);
            }
            return;
        }
        level.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, x, pos.getY() + 0.8, z, 0.0, 0.05, 0.0);
        if (random.nextInt(2) == 0) {
            level.addParticle(ParticleTypes.GLOW, x, pos.getY() + 0.85, z, 0.0, 0.03 + random.nextDouble() * 0.03, 0.0);
        }
        if (phase == Phase.AWAKE && random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.GLOW_SQUID_INK, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0, 0.04, 0.0);
        }
    }
}
