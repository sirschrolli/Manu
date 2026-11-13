package io.github.sirschrolli.kingdomsatwar.block.end;

import io.github.sirschrolli.kingdomsatwar.registry.PhaseGear;
import io.github.sirschrolli.kingdomsatwar.registry.PhaseSanctuary;
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
 * Der Leerenthron (seit 1.5): ein Thron aus Obsidian und Purpur mit hoher Lehne, an deren Spitze ein Phasit glimmt, an den
 * Wangen kleine Phasitsplitter. Vor ihm kniet Phasmor, der Leerwandlerkönig, und hält Wache. Solange er lebt, leuchtet der
 * Phasit; ist er besiegt, erlischt er – ein Phasit auf dem leeren Thron entzündet ihn neu und ruft den König zurück.
 * Unzerstörbar.
 */
public class VoidThroneBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);
    /** Sitz und untere Lehne (die Lehne ragt im Modell noch einen Block höher). */
    private static final VoxelShape SHAPE_NORTH = Shapes.or(Block.box(1, 0, 1, 15, 8, 15), Block.box(1, 8, 11, 15, 16, 15));

    /** Er hält Wache (oder ist noch nicht da), er kämpft, er ist besiegt (der Phasit ist erloschen). */
    public enum Phase implements StringRepresentable {
        GUARDED(10), AWAKE(15), DEFEATED(2);

        public final int light;

        Phase(int light) {
            this.light = light;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public VoidThroneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(PHASE, Phase.GUARDED));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PHASE);
    }

    /** Gedreht (etwa in einem gedrehten Bauwerk) blickt der Thron mit – sonst knie der König hinter ihm. */
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

    /** Die Lehne steht hinten (dem Blick abgewandt). */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> Shapes.or(Block.box(1, 0, 1, 15, 8, 15), Block.box(1, 8, 1, 15, 16, 5));
            case EAST -> Shapes.or(Block.box(1, 0, 1, 15, 8, 15), Block.box(1, 8, 1, 5, 16, 15));
            case WEST -> Shapes.or(Block.box(1, 0, 1, 15, 8, 15), Block.box(11, 8, 1, 15, 16, 15));
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VoidThroneBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != PhaseSanctuary.VOID_THRONE_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof VoidThroneBlockEntity throne) {
                throne.serverTick(server);
            }
        };
    }

    /** Ein Phasit auf dem leeren Thron: Der Stein in der Lehne flammt auf, und der König kehrt zurück. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(PhaseGear.PHASIT.get()) || state.getValue(PHASE) != Phase.DEFEATED) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof VoidThroneBlockEntity throne) {
            if (!throne.rekindle(server, player)) {
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
                case GUARDED -> "block.kingdomsatwar.void_throne.hint";
                case AWAKE -> "block.kingdomsatwar.void_throne.awake";
                case DEFEATED -> "block.kingdomsatwar.void_throne.defeated";
            };
            player.sendOverlayMessage(Component.translatable(key).withStyle(state.getValue(PHASE) == Phase.DEFEATED ? ChatFormatting.GRAY
                    : ChatFormatting.DARK_AQUA));
        }
        return InteractionResult.SUCCESS;
    }

    /** Um die Lehne treiben türkise Funken und Portalwirbel (im Kampf mehr; erloschen nur noch ein wenig Staub). */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Phase phase = state.getValue(PHASE);
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
        if (phase == Phase.DEFEATED) {
            if (random.nextInt(8) == 0) {
                level.addParticle(ParticleTypes.ASH, x, pos.getY() + 1.2, z, 0.0, 0.0, 0.0);
            }
            return;
        }
        if (random.nextInt(phase == Phase.AWAKE ? 1 : 3) == 0) {
            level.addParticle(new DustParticleOptions(0x3FE6CC, 0.8f), x, pos.getY() + 1.0 + random.nextDouble() * 1.0, z, 0.0, 0.02, 0.0);
        }
        if (random.nextInt(phase == Phase.AWAKE ? 2 : 5) == 0) {
            level.addParticle(ParticleTypes.PORTAL, x, pos.getY() + 1.6, z, (random.nextDouble() - 0.5) * 1.5, -random.nextDouble(),
                    (random.nextDouble() - 0.5) * 1.5);
        }
    }
}
