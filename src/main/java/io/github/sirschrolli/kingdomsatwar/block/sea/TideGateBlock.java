package io.github.sirschrolli.kingdomsatwar.block.sea;

import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.urmeer.TideGate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Der Schleier eines Gezeitentors (seit 0.89): wirbelndes, türkis leuchtendes Wasser in einem Ring oder Rahmen aus
 * Gezeitensteinziegeln. Wer eine Weile darin schwimmt oder steht, gelangt ins Urmeer – und von dort zurück. Unter Wasser
 * bleibt das Wasser im Tor stehen (der Schleier ist geflutet). Bricht der Rahmen, erlischt das Tor.
 */
public class TideGateBlock extends Block implements Portal, SimpleWaterloggedBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape X_SHAPE = Block.column(16.0, 4.0, 0.0, 16.0);
    private static final VoxelShape Z_SHAPE = Block.column(4.0, 16.0, 0.0, 16.0);

    public TideGateBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.Z ? Z_SHAPE : X_SHAPE;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        Direction.Axis axis = state.getValue(AXIS);
        boolean inPlane = direction.getAxis() == axis || direction.getAxis() == Direction.Axis.Y;
        if (inPlane && !neighbourState.is(this) && !TideGate.isFrame(neighbourState)) {
            return state.getValue(WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier,
            boolean isPrecise) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        if (entity instanceof Player player) {
            return player.getAbilities().invulnerable ? 1 : TideGate.TRAVEL_TICKS;
        }
        return 0;
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        return TideGate.destination(level, entity, pos);
    }

    @Override
    public Portal.Transition getLocalTransition() {
        // kein violetter Netherwirbel: den türkisen Strudel zeichnet client.urmeer.TideGateOverlay
        return Portal.Transition.NONE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        boolean x = state.getValue(AXIS) == Direction.Axis.X;
        boolean wet = state.getValue(WATERLOGGED);
        double px = pos.getX() + (x ? random.nextDouble() : 0.5 + (random.nextDouble() - 0.5) * 0.6);
        double pz = pos.getZ() + (x ? 0.5 + (random.nextDouble() - 0.5) * 0.6 : random.nextDouble());
        double py = pos.getY() + random.nextDouble();
        level.addParticle(ModParticles.SEA_GLOW.get(), px, py, pz, (random.nextDouble() - 0.5) * 0.02, 0.01 + random.nextDouble() * 0.02,
                (random.nextDouble() - 0.5) * 0.02);
        if (random.nextInt(3) == 0) {
            // Wirbel aus dem Tor heraus, wie beim Aquisitor
            double dx = x ? 0.0 : (random.nextBoolean() ? 1.0 : -1.0);
            double dz = x ? (random.nextBoolean() ? 1.0 : -1.0) : 0.0;
            level.addParticle(ParticleTypes.NAUTILUS, px + dx * 1.2, py + (random.nextDouble() - 0.5), pz + dz * 1.2, -dx * 1.2, 0.0, -dz * 1.2);
        }
        if (wet && random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.BUBBLE, px, py, pz, 0.0, 0.05, 0.0);
        } else if (!wet && random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.SPLASH, px, py, pz, 0.0, 0.0, 0.0);
        }
        if (random.nextInt(110) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.CONDUIT_AMBIENT_SHORT, SoundSource.BLOCKS,
                    0.6f, 0.8f + random.nextFloat() * 0.2f, false);
        }
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return switch (rotation) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> state.setValue(AXIS, state.getValue(AXIS) == Direction.Axis.Z ? Direction.Axis.X
                    : Direction.Axis.Z);
            default -> state;
        };
    }
}
