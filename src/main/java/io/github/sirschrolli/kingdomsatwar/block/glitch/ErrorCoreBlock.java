package io.github.sirschrolli.kingdomsatwar.block.glitch;

import io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
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
 * Der Fehlerkern: das Herz einer Fehlerzone – ein zerbrochener Würfel, dessen Ecken fehlen, in der Mitte ein flimmernder
 * Kern aus Bildrauschen in Rot, Grün und Blau. Darin wartet Anni der Troll; tritt jemand heran, wird sie geladen. Nach
 * ihrem Absturz ist der Kern erloschen, und nur ein Redstoneblock startet ihn neu. Unzerstörbar.
 */
public class ErrorCoreBlock extends Block implements EntityBlock {
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);

    /** Wartend (hell flimmernd), Anni läuft, abgestürzt (erloschen). */
    public enum Phase implements StringRepresentable {
        WAITING(13), RUNNING(9), CRASHED(2);

        public final int light;

        Phase(int light) {
            this.light = light;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    private static final VoxelShape SHAPE = Shapes.or(Block.box(1, 1, 1, 15, 15, 15));

    public ErrorCoreBlock(BlockBehaviour.Properties properties) {
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
        return new ErrorCoreBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != AnniBlocks.ERROR_CORE_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof ErrorCoreBlockEntity core) {
                core.serverTick(server);
            }
        };
    }

    /** Ein Redstoneblock an den erloschenen Kern: Er startet neu. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(Items.REDSTONE_BLOCK) || state.getValue(PHASE) != Phase.CRASHED) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof ErrorCoreBlockEntity core) {
            if (!core.restart(server, player)) {
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
                case WAITING -> "block.kingdomsatwar.error_core.hint";
                case RUNNING -> "block.kingdomsatwar.error_core.running";
                case CRASHED -> "block.kingdomsatwar.error_core.crashed";
            };
            player.sendOverlayMessage(Component.translatable(key).withStyle(state.getValue(PHASE) == Phase.CRASHED ? ChatFormatting.GRAY
                    : ChatFormatting.AQUA));
        }
        return InteractionResult.SUCCESS;
    }

    /** Wartend stieben Pixel aus dem Kern, und manchmal knistert er; erloschen flackert er nur selten auf. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Phase phase = state.getValue(PHASE);
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        if (phase == Phase.CRASHED) {
            if (random.nextInt(14) == 0) {
                level.addParticle(ModParticles.GLITCH_PIXEL.get(), x + (random.nextDouble() - 0.5) * 0.6, y, z + (random.nextDouble() - 0.5) * 0.6,
                        0.0, 0.01, 0.0);
            }
            return;
        }
        for (int i = 0; i < (phase == Phase.WAITING ? 2 : 1); i++) {
            level.addParticle(ModParticles.GLITCH_PIXEL.get(), x + (random.nextDouble() - 0.5) * 0.9, y + (random.nextDouble() - 0.5) * 0.9,
                    z + (random.nextDouble() - 0.5) * 0.9, (random.nextDouble() - 0.5) * 0.04, 0.03, (random.nextDouble() - 0.5) * 0.04);
        }
        if (phase == Phase.WAITING && random.nextInt(30) == 0) {
            level.playLocalSound(x, y, z, ModSounds.ERROR_CORE_HUM.get(), SoundSource.BLOCKS, 0.8f, 0.8f + random.nextFloat() * 0.4f, false);
        }
    }
}
