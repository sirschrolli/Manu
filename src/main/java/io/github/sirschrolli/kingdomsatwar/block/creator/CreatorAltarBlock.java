package io.github.sirschrolli.kingdomsatwar.block.creator;

import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
 * Der Altar des Schöpfers: ein Sockel aus weißem Stein und Gold, auf dem eine Sonnenscheibe liegt. Legt man einen
 * Schöpferfunken hinein, schwebt er leuchtend über der Scheibe; wer den Altar dann berührt, ruft Schrolli den Schöpfer –
 * alle, die nahe stehen, werden in seinen Hof gezogen. Schleichend berührt gibt der Altar den Funken zurück. Fällt der
 * Schöpfer, vergeht der Funke; unterliegen seine Herausforderer, bleibt er im Altar (man kann es erneut wagen).
 */
public class CreatorAltarBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);

    /** Leer, mit Funken (bereit), der Schöpfer ist gerufen (erwacht – drüben wird gekämpft). */
    public enum Phase implements StringRepresentable {
        EMPTY(7), ARMED(13), AWAKE(15);

        public final int light;

        Phase(int light) {
            this.light = light;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    private static final VoxelShape SHAPE = Shapes.or(Block.box(0, 0, 0, 16, 4, 16), Block.box(3, 4, 3, 13, 12, 13), Block.box(1, 12, 1, 15, 15, 15));

    public CreatorAltarBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PHASE, Phase.EMPTY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PHASE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreatorAltarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != CreatorBlocks.CREATOR_ALTAR_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof CreatorAltarBlockEntity altar) {
                altar.serverTick(server);
            }
        };
    }

    /** Ein Schöpferfunke in den leeren Altar. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(CreatorBlocks.CREATOR_SPARK.get()) || state.getValue(PHASE) != Phase.EMPTY) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof CreatorAltarBlockEntity altar) {
            if (!altar.insertSpark(server, player)) {
                return InteractionResult.FAIL;
            }
            stack.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }

    /** Berühren: den Schöpfer rufen (mit Funken), schleichend den Funken zurücknehmen, sonst ein Hinweis. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof CreatorAltarBlockEntity altar) {
            altar.touch(server, player);
        }
        return InteractionResult.SUCCESS;
    }

    /** Leer glimmt die Scheibe; mit Funken steigen goldene Funken auf, gerufen strömt Licht empor. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Phase phase = state.getValue(PHASE);
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        if (phase == Phase.EMPTY) {
            if (random.nextInt(6) == 0) {
                level.addParticle(ModParticles.DIVINE_SPARK.get(), x + (random.nextDouble() - 0.5) * 0.8, y, z + (random.nextDouble() - 0.5) * 0.8,
                        0.0, 0.01, 0.0);
            }
            return;
        }
        for (int i = 0; i < (phase == Phase.AWAKE ? 4 : 2); i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double r = 0.3 + random.nextDouble() * 0.5;
            level.addParticle(ModParticles.DIVINE_SPARK.get(), x + Math.cos(a) * r, y + 0.5 + random.nextDouble() * 0.6, z + Math.sin(a) * r, 0.0,
                    0.02 + random.nextDouble() * 0.03, 0.0);
        }
        if (phase == Phase.AWAKE || random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.END_ROD, x + (random.nextDouble() - 0.5) * 0.3, y + 0.8, z + (random.nextDouble() - 0.5) * 0.3, 0.0,
                    phase == Phase.AWAKE ? 0.12 : 0.04, 0.0);
        }
    }
}
