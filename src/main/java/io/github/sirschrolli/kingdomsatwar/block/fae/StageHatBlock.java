package io.github.sirschrolli.kingdomsatwar.block.fae;

import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks;
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
 * Der große Zauberzylinder: steht mitten auf der Zauberbühne im Feenreich – ein riesiger Zylinder aus schwarzviolettem
 * Seidenfilz mit Satinband und goldenem Stern, eineinhalb Blöcke hoch. Darin wartet MissMystik auf ihre Zuschauer; tritt
 * jemand heran, beginnt die Vorstellung. Nach ihrem Sieg ist er leer, und nur Feenstaub hinein ruft sie zurück.
 * Unzerstörbar.
 */
public class StageHatBlock extends Block implements EntityBlock {
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);

    /** Wartend (das Band leuchtet), Vorstellung läuft, leer (besiegt). */
    public enum Phase implements StringRepresentable {
        WAITING(11), SHOW(7), EMPTY(2);

        public final int light;

        Phase(int light) {
            this.light = light;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    private static final VoxelShape SHAPE = Shapes.or(Block.box(0, 0, 0, 16, 1.5, 16), Block.box(2.5, 1.5, 2.5, 13.5, 16, 13.5));

    public StageHatBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PHASE, Phase.WAITING));
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
        return new StageHatBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != MystikBlocks.STAGE_HAT_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof StageHatBlockEntity hat) {
                hat.serverTick(server);
            }
        };
    }

    /** Feenstaub in den leeren Zylinder: MissMystik kehrt zurück. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(FaeBlocks.FAE_DUST.get()) || state.getValue(PHASE) != Phase.EMPTY) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof StageHatBlockEntity hat) {
            if (!hat.refill(server, player)) {
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
                case WAITING -> "block.kingdomsatwar.stage_hat.hint";
                case SHOW -> "block.kingdomsatwar.stage_hat.show";
                case EMPTY -> "block.kingdomsatwar.stage_hat.empty";
            };
            player.sendOverlayMessage(Component.translatable(key).withStyle(state.getValue(PHASE) == Phase.EMPTY ? ChatFormatting.GRAY
                    : ChatFormatting.LIGHT_PURPLE));
        }
        return InteractionResult.SUCCESS;
    }

    /** Wartend funkelt es über dem Zylinder und um sein Band; leer steigt nur selten ein Funke auf. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Phase phase = state.getValue(PHASE);
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.45;
        double z = pos.getZ() + 0.5;
        if (phase == Phase.EMPTY) {
            if (random.nextInt(12) == 0) {
                level.addParticle(ModParticles.FAE_SPARK.get(), x + (random.nextDouble() - 0.5) * 0.5, y, z + (random.nextDouble() - 0.5) * 0.5,
                        0.0, 0.01, 0.0);
            }
            return;
        }
        if (random.nextInt(phase == Phase.WAITING ? 1 : 3) == 0) {
            level.addParticle(ModParticles.FAE_SPARK.get(), x + (random.nextDouble() - 0.5) * 0.5, y + random.nextDouble() * 0.3,
                    z + (random.nextDouble() - 0.5) * 0.5, 0.0, 0.02 + random.nextDouble() * 0.02, 0.0);
        }
        if (random.nextInt(4) == 0) {
            double a = random.nextDouble() * Math.PI * 2.0;
            level.addParticle(ParticleTypes.END_ROD, x + Math.cos(a) * 0.75, pos.getY() + 0.3 + random.nextDouble() * 0.3, z + Math.sin(a) * 0.75,
                    0.0, 0.01, 0.0);
        }
    }
}
