package io.github.sirschrolli.kingdomsatwar.block.nether;

import io.github.sirschrolli.kingdomsatwar.registry.FlameGear;
import io.github.sirschrolli.kingdomsatwar.registry.FlameSanctuary;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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
 * Der Flammenaltar (seit 1.2): ein Altar aus poliertem Schwarzstein mit goldenen Kanten, auf ihm eine Feuerschale, in der das
 * ewige Feuer der Feuerdiener brennt. Vor ihm schwebt Ignara und betet. Solange sie lebt, lodert das Feuer; ist sie besiegt,
 * erlischt es – ein Feueropal in der kalten Schale entzündet es neu und ruft die Priesterin zurück. Unzerstörbar.
 */
public class FlameAltarBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);
    private static final VoxelShape SHAPE = Shapes.or(Block.box(0, 0, 0, 16, 4, 16), Block.box(2, 4, 2, 14, 11, 14),
            Block.box(0.5, 11, 0.5, 15.5, 15, 15.5));

    /** Sie betet (oder ist noch nicht da), sie kämpft, sie ist besiegt (das Feuer ist erloschen). */
    public enum Phase implements StringRepresentable {
        PRAYING(13), AWAKE(15), DEFEATED(0);

        public final int light;

        Phase(int light) {
            this.light = light;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public FlameAltarBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(PHASE, Phase.PRAYING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PHASE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FlameAltarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != FlameSanctuary.FLAME_ALTAR_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof FlameAltarBlockEntity altar) {
                altar.serverTick(server);
            }
        };
    }

    /** Ein Feueropal in der kalten Schale: Das ewige Feuer lodert wieder auf, und die Priesterin kehrt zurück. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(FlameGear.FIRE_OPAL.get()) || state.getValue(PHASE) != Phase.DEFEATED) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof FlameAltarBlockEntity altar) {
            if (!altar.rekindle(server, player)) {
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
                case PRAYING -> "block.kingdomsatwar.flame_altar.hint";
                case AWAKE -> "block.kingdomsatwar.flame_altar.awake";
                case DEFEATED -> "block.kingdomsatwar.flame_altar.defeated";
            };
            player.sendOverlayMessage(Component.translatable(key).withStyle(state.getValue(PHASE) == Phase.DEFEATED ? ChatFormatting.GRAY
                    : ChatFormatting.GOLD));
        }
        return InteractionResult.SUCCESS;
    }

    /** Aus der Schale steigen Flammen und Funken (im Kampf lodert es höher; erloschen raucht nur noch die Glut). */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Phase phase = state.getValue(PHASE);
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
        if (phase == Phase.DEFEATED) {
            if (random.nextInt(6) == 0) {
                level.addParticle(ParticleTypes.SMOKE, x, pos.getY() + 1.0, z, 0.0, 0.03, 0.0);
            }
            return;
        }
        level.addParticle(ParticleTypes.FLAME, x, pos.getY() + 1.0, z, 0.0, phase == Phase.AWAKE ? 0.08 : 0.04, 0.0);
        if (random.nextInt(phase == Phase.AWAKE ? 2 : 4) == 0) {
            level.addParticle(ParticleTypes.LAVA, x, pos.getY() + 1.0, z, 0.0, 0.0, 0.0);
        }
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SMOKE, x, pos.getY() + 1.6, z, 0.0, 0.04, 0.0);
        }
    }
}
