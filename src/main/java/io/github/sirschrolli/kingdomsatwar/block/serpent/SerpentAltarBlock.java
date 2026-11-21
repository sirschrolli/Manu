package io.github.sirschrolli.kingdomsatwar.block.serpent;

import io.github.sirschrolli.kingdomsatwar.registry.SerpentGear;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
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
 * Der Schlangenaltar (seit 1.10): ein Altarstein aus bemoostem Fels mitten in der Schlangengrube des Schlangentempels, an den
 * Seiten gemeißelte Federschlangen; obenauf windet sich ein goldenes Schlangenbild mit Smaragdaugen. Um ihn schläft Quetzara,
 * die Gefiederte Schlange – wer in die Grube hinabsteigt, weckt sie. Ist sie besiegt, ist das Schlangenbild zerbrochen; ein
 * <b>Goldblock</b> auf dem leeren Altar ruft sie zurück (sie erwacht sofort). Unzerstörbar.
 */
public class SerpentAltarBlock extends Block implements EntityBlock {
    /** Wohin das Schlangenbild blickt (zum Eingang der Grube). */
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);
    private static final VoxelShape BASE = Block.box(1.0, 0.0, 1.0, 15.0, 10.0, 15.0);
    private static final VoxelShape WHOLE = Shapes.or(BASE, Block.box(4.0, 10.0, 4.0, 12.0, 22.0, 12.0));
    private static final VoxelShape BROKEN = Shapes.or(BASE, Block.box(4.0, 10.0, 4.0, 12.0, 13.0, 12.0));

    /** Sie schläft (oder ist noch nicht da), sie kämpft, sie ist besiegt (das Schlangenbild ist zerbrochen). */
    public enum Phase implements StringRepresentable {
        SLEEPING, AWAKE, FALLEN;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public SerpentAltarBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PHASE, Phase.SLEEPING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PHASE);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(PHASE) == Phase.FALLEN ? BROKEN : WHOLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SerpentAltarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != SerpentGear.SERPENT_ALTAR_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof SerpentAltarBlockEntity altar) {
                altar.serverTick(server);
            }
        };
    }

    /** Ein Goldblock auf dem leeren Altar: Das Schlangenbild ist wieder ganz, und Quetzara kehrt zurück. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(Items.GOLD_BLOCK) || state.getValue(PHASE) != Phase.FALLEN) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof SerpentAltarBlockEntity altar) {
            if (!altar.offer(server, player)) {
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
                case SLEEPING -> "block.kingdomsatwar.serpent_altar.hint";
                case AWAKE -> "block.kingdomsatwar.serpent_altar.awake";
                case FALLEN -> "block.kingdomsatwar.serpent_altar.fallen";
            };
            player.sendOverlayMessage(Component.translatable(key).withStyle(state.getValue(PHASE) == Phase.FALLEN ? ChatFormatting.GRAY
                    : ChatFormatting.GREEN));
        }
        return InteractionResult.SUCCESS;
    }

    /** Wacht die Schlange, glimmen die Augen des Schlangenbilds und Gift tropft; zerbrochen rieselt Goldstaub. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Phase phase = state.getValue(PHASE);
        if (phase == Phase.AWAKE && random.nextInt(2) == 0) {
            level.addParticle(new DustParticleOptions(0x7CFF4A, 0.8f), pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                    pos.getY() + 1.3, pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4, 0.0, -0.02, 0.0);
        } else if (phase == Phase.SLEEPING && random.nextInt(6) == 0) {
            level.addParticle(ParticleTypes.WAX_ON, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5, pos.getY() + 1.1 + random.nextDouble() * 0.3,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5, 0.0, 0.0, 0.0);
        } else if (phase == Phase.FALLEN && random.nextInt(10) == 0) {
            level.addParticle(new DustParticleOptions(0xE8B830, 0.6f), pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6, pos.getY() + 0.9,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6, 0.0, 0.0, 0.0);
        }
    }
}
