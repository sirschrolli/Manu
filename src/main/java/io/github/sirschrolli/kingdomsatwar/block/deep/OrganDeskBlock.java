package io.github.sirschrolli.kingdomsatwar.block.deep;

import io.github.sirschrolli.kingdomsatwar.entity.deep.SordinEntity;
import io.github.sirschrolli.kingdomsatwar.registry.CathedralGear;
import io.github.sirschrolli.kingdomsatwar.registry.EchoGear;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
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
 * Das Orgelpult (seit 1.7): die Mitte des Spieltischs der Orgel in der Stillen Kathedrale – ein Kasten aus dunkler Eiche mit
 * Registerknöpfen, darauf ein schräges Notenpult aus Kupfer, auf dem ein Notenblatt liegt. Vor ihm steht Sordin, der Kantor
 * der Stille, und spielt; solange er lebt, leuchten die Noten auf dem Blatt. Ist er besiegt, verstummt das Pult (die Noten
 * erlöschen) – ein Hallkristall auf dem Pult ruft ihn zurück. Unzerstörbar.
 */
public class OrganDeskBlock extends Block implements EntityBlock {
    /** Wohin das Pult blickt: dort steht der Kantor. */
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);
    /** Der Kasten (ein voller Block); das Notenpult ragt im Modell darüber hinaus. */
    private static final VoxelShape SHAPE = Shapes.block();

    /** Er spielt (oder ist noch nicht da), er kämpft, er ist besiegt (das Pult ist verstummt). */
    public enum Phase implements StringRepresentable {
        GUARDED(6), AWAKE(11), SILENCED(0);

        public final int light;

        Phase(int light) {
            this.light = light;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public OrganDeskBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(PHASE, Phase.GUARDED));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PHASE);
    }

    /** Gedreht (etwa in einer gedrehten Kathedrale) blickt das Pult mit – sonst stünde der Kantor in der Orgel. */
    @Override
    protected BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
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
        return new OrganDeskBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != CathedralGear.ORGAN_DESK_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof OrganDeskBlockEntity desk) {
                desk.serverTick(server);
            }
        };
    }

    /** Ein Hallkristall auf dem verstummten Pult: Die Noten leuchten wieder auf, und der Kantor kehrt zurück. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(EchoGear.ECHO_CRYSTAL.get()) || state.getValue(PHASE) != Phase.SILENCED) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof OrganDeskBlockEntity desk) {
            if (!desk.rekindle(server, player)) {
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
                case GUARDED -> "block.kingdomsatwar.organ_desk.hint";
                case AWAKE -> "block.kingdomsatwar.organ_desk.awake";
                case SILENCED -> "block.kingdomsatwar.organ_desk.silenced";
            };
            player.sendOverlayMessage(Component.translatable(key).withStyle(state.getValue(PHASE) == Phase.SILENCED ? ChatFormatting.GRAY
                    : ChatFormatting.DARK_AQUA));
        }
        return InteractionResult.SUCCESS;
    }

    /** Über dem Pult treiben Sculk-Funken und dann und wann eine Note (im Kampf mehr; verstummt nur ein wenig Staub). */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Phase phase = state.getValue(PHASE);
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.7;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.7;
        if (phase == Phase.SILENCED) {
            if (random.nextInt(10) == 0) {
                level.addParticle(ParticleTypes.ASH, x, pos.getY() + 1.4, z, 0.0, 0.0, 0.0);
            }
            return;
        }
        if (random.nextInt(phase == Phase.AWAKE ? 2 : 5) == 0) {
            level.addParticle(new DustParticleOptions(SordinEntity.SCULK, 0.7f), x, pos.getY() + 1.2 + random.nextDouble() * 0.6, z, 0.0, 0.02,
                    0.0);
        }
        if (random.nextInt(phase == Phase.AWAKE ? 6 : 16) == 0) {
            level.addParticle(ParticleTypes.NOTE, x, pos.getY() + 1.5, z, 0.66 + random.nextDouble() * 0.12, 0.0, 0.0);
        }
    }
}
