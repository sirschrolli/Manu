package io.github.sirschrolli.kingdomsatwar.block.castle;

import io.github.sirschrolli.kingdomsatwar.registry.CastleGear;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Das Rabenbanner (seit 1.8): ein hoher Bannerpfahl aus dunkler Eiche mit Eisenbändern und goldenem Knauf im Hof der
 * Raubritterburg; am Querholz das Banner derer von Rabenfels – Gold, darin der schwarze Rabe, unten gespalten. Unter ihm hält
 * Kuno von Rabenfels, der Raubritter, Hof. Ist er besiegt, hängt nur noch ein Fetzen am Pfahl – ein schwarzes Banner, an den
 * Pfahl gehängt, ruft ihn zurück. Unzerstörbar.
 */
public class RavenBannerBlock extends Block implements EntityBlock {
    /** Wohin das Banner blickt (zum Tor): dorthin blickt auch der Ritter. */
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);
    /** Der Pfahl (anderthalb Blöcke hoch fest; im Modell ragt er mit dem Banner zwei Blöcke auf). */
    private static final VoxelShape SHAPE = Block.box(6.5, 0.0, 6.5, 9.5, 24.0, 9.5);

    /** Er hält Hof (oder ist noch nicht da), er kämpft, er ist besiegt (das Banner ist zerrissen). */
    public enum Phase implements StringRepresentable {
        GUARDED, AWAKE, FALLEN;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public RavenBannerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(PHASE, Phase.GUARDED));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PHASE);
    }

    /** Gedreht (etwa in einer gedrehten Burg) blickt das Banner mit – sonst stünde der Ritter in der Mauer. */
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
        return new RavenBannerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != CastleGear.RAVEN_BANNER_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof RavenBannerBlockEntity banner) {
                banner.serverTick(server);
            }
        };
    }

    /** Ein schwarzes Banner am leeren Pfahl: Das Rabenbanner weht wieder, und der Raubritter kehrt zurück. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(Items.BANNER.pick(DyeColor.BLACK)) || state.getValue(PHASE) != Phase.FALLEN) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof RavenBannerBlockEntity banner) {
            if (!banner.raise(server, player)) {
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
                case GUARDED -> "block.kingdomsatwar.raven_banner.hint";
                case AWAKE -> "block.kingdomsatwar.raven_banner.awake";
                case FALLEN -> "block.kingdomsatwar.raven_banner.fallen";
            };
            player.sendOverlayMessage(Component.translatable(key).withStyle(state.getValue(PHASE) == Phase.FALLEN ? ChatFormatting.GRAY
                    : ChatFormatting.GOLD));
        }
        return InteractionResult.SUCCESS;
    }

    /** Ist das Banner zerrissen, treibt dann und wann ein Fetzen Staub vom Pfahl. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(PHASE) == Phase.FALLEN && random.nextInt(12) == 0) {
            level.addParticle(ParticleTypes.ASH, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6, pos.getY() + 1.6,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6, 0.0, 0.0, 0.0);
        }
    }
}
