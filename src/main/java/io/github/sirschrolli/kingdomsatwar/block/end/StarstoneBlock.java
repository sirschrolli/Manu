package io.github.sirschrolli.kingdomsatwar.block.end;

import io.github.sirschrolli.kingdomsatwar.registry.StarBlocks;
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
import net.minecraft.world.item.Items;
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
 * Der Sternstein: ein Kristall des Sternenordens auf dem Pfeiler in der Mitte des Sternenhofs – ein Sockel aus Quarz und
 * Gold, darauf ein hoher, leuchtender Sternkristall mit vier kleinen Spitzen. Tritt jemand an ihn heran, stößt Asteria, die
 * Sternenreiterin, vom Himmel herab; nach ihrem Sieg erlischt er, und nur ein Endkristall entzündet ihn wieder (und ruft sie
 * zurück). Unzerstörbar.
 */
public class StarstoneBlock extends Block implements EntityBlock {
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);

    /** Ruhend und hell leuchtend, wach (sie ist da), erloschen (besiegt). */
    public enum Phase implements StringRepresentable {
        DORMANT(15), AWAKE(12), DIM(4);

        public final int light;

        Phase(int light) {
            this.light = light;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    private static final VoxelShape SHAPE = Shapes.or(Block.box(2, 0, 2, 14, 3, 14), Block.box(5, 3, 5, 11, 16, 11));

    public StarstoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PHASE, Phase.DORMANT));
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
        return new StarstoneBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != StarBlocks.STARSTONE_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof StarstoneBlockEntity stone) {
                stone.serverTick(server);
            }
        };
    }

    /** Ein Endkristall am erloschenen Sternstein entzündet ihn neu – und ruft Asteria zurück. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(Items.END_CRYSTAL) || state.getValue(PHASE) != Phase.DIM) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof StarstoneBlockEntity stone) {
            if (!stone.rekindle(server, player)) {
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
                case DORMANT -> "block.kingdomsatwar.starstone.hint";
                case AWAKE -> "block.kingdomsatwar.starstone.awake";
                case DIM -> "block.kingdomsatwar.starstone.dim";
            };
            player.sendOverlayMessage(Component.translatable(key).withStyle(state.getValue(PHASE) == Phase.DIM ? ChatFormatting.GRAY
                    : ChatFormatting.AQUA));
        }
        return InteractionResult.SUCCESS;
    }

    /** Licht steigt vom Kristall auf und funkelt um ihn; erloschen glimmt er nur noch selten. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Phase phase = state.getValue(PHASE);
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.9;
        double z = pos.getZ() + 0.5;
        if (phase == Phase.DIM) {
            if (random.nextInt(10) == 0) {
                level.addParticle(ParticleTypes.END_ROD, x + (random.nextDouble() - 0.5) * 0.5, y + 0.2, z + (random.nextDouble() - 0.5) * 0.5,
                        0.0, 0.01, 0.0);
            }
            return;
        }
        if (random.nextInt(phase == Phase.AWAKE ? 1 : 2) == 0) {
            level.addParticle(ParticleTypes.END_ROD, x + (random.nextDouble() - 0.5) * 0.4, y + random.nextDouble() * 0.3,
                    z + (random.nextDouble() - 0.5) * 0.4, 0.0, 0.03 + random.nextDouble() * 0.04, 0.0);
        }
        if (random.nextInt(3) == 0) {
            double a = random.nextDouble() * Math.PI * 2.0;
            level.addParticle(ParticleTypes.FIREWORK, x + Math.cos(a) * 0.8, y - 0.3 + random.nextDouble() * 0.8, z + Math.sin(a) * 0.8,
                    0.0, 0.0, 0.0);
        }
    }
}
