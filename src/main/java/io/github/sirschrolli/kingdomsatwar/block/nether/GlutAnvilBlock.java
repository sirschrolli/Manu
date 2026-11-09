package io.github.sirschrolli.kingdomsatwar.block.nether;

import io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Der Glutamboss: der Amboss des Glutschmieds Malleus auf dem Dach der Aschenesse – ein massiger Block aus geschwärztem
 * Eisen, in dessen Rissen die Glut der Esse glimmt. Darauf liegt sein Meisterstück, der Glutspeer, noch rotglühend vom
 * letzten Schlag. Wer dem Amboss zu nahe kommt, weckt den Schmied; nach seinem Sieg erkaltet der Amboss, und nur ein
 * Netheritbarren facht die Esse wieder an (und ruft ihn zurück). Unzerstörbar.
 */
public class GlutAnvilBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);

    /** Glühend mit dem Speer darauf, glühend und leer (der Schmied ist wach), erkaltet (besiegt). */
    public enum Phase implements StringRepresentable {
        DORMANT(11), AWAKE(13), COLD(3);

        public final int light;

        Phase(int light) {
            this.light = light;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    private static final VoxelShape SHAPE_X = shape(Direction.Axis.X);
    private static final VoxelShape SHAPE_Z = shape(Direction.Axis.Z);

    public GlutAnvilBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PHASE, Phase.DORMANT));
    }

    /** Fuß, Hals und Bahn wie bei einem Amboss; die Bahn liegt quer zur Blickrichtung. */
    private static VoxelShape shape(Direction.Axis axis) {
        if (axis == Direction.Axis.X) {
            return Shapes.or(Block.box(1, 0, 2, 15, 4, 14), Block.box(3, 4, 4, 13, 5, 12), Block.box(4, 5, 5.5, 12, 10, 10.5),
                    Block.box(0, 10, 3, 16, 16, 13));
        }
        return Shapes.or(Block.box(2, 0, 1, 14, 4, 15), Block.box(4, 4, 3, 12, 5, 13), Block.box(5.5, 5, 4, 10.5, 10, 12),
                Block.box(3, 10, 0, 13, 16, 16));
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
        // die Bahn liegt quer: schaut der Amboss nach Norden oder Süden, läuft sie von Westen nach Osten
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? SHAPE_X : SHAPE_Z;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GlutAnvilBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ForgeBlocks.GLUT_ANVIL_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof GlutAnvilBlockEntity forge) {
                forge.serverTick(server);
            }
        };
    }

    /** Ein Netheritbarren auf dem erkalteten Amboss facht die Esse neu an – und ruft den Schmied zurück. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(Items.NETHERITE_INGOT) || state.getValue(PHASE) != Phase.COLD) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof GlutAnvilBlockEntity forge) {
            if (!forge.rekindle(server, player)) {
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
                case DORMANT -> "block.kingdomsatwar.glut_anvil.hint";
                case AWAKE -> "block.kingdomsatwar.glut_anvil.awake";
                case COLD -> "block.kingdomsatwar.glut_anvil.cold";
            };
            player.sendOverlayMessage(Component.translatable(key).withStyle(state.getValue(PHASE) == Phase.COLD ? ChatFormatting.GRAY
                    : ChatFormatting.GOLD));
        }
        return InteractionResult.SUCCESS;
    }

    /** Glut steigt auf, Funken springen vom Werkstück; der erkaltete Amboss raucht nur noch ab und zu. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Phase phase = state.getValue(PHASE);
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        if (phase == Phase.COLD) {
            if (random.nextInt(6) == 0) {
                level.addParticle(ParticleTypes.SMOKE, x + (random.nextDouble() - 0.5) * 0.8, y + 0.05, z + (random.nextDouble() - 0.5) * 0.8,
                        0.0, 0.02, 0.0);
            }
            return;
        }
        if (random.nextInt(2) == 0) {
            level.addParticle(ParticleTypes.SMALL_FLAME, x + (random.nextDouble() - 0.5) * 0.9, y + 0.05, z + (random.nextDouble() - 0.5) * 0.9,
                    0.0, 0.015, 0.0);
        }
        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.LAVA, x, y + 0.1, z, 0.0, 0.0, 0.0);
        }
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.SMOKE, x + (random.nextDouble() - 0.5) * 0.6, y + 0.15, z + (random.nextDouble() - 0.5) * 0.6, 0.0,
                    0.03, 0.0);
        }
    }
}
