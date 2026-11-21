package io.github.sirschrolli.kingdomsatwar.block.fae;

import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
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
import org.jspecify.annotations.Nullable;

/**
 * Der Hüterstein (seit 0.96): ein behauener Block aus Feenstein am Grund des alten Mondsteinbruchs, in den das alte Volk
 * das Zeichen des Hüters gemeißelt hat – ein Auge über einer Mondsichel. Solange der Steinhüter wacht oder schläft, leuchtet
 * es; ist er besiegt, erlischt es, und ein Mondstein darauf ruft einen neuen Hüter. Unzerstörbar.
 */
public class KeeperStoneBlock extends Block implements EntityBlock {
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);

    /** Der Hüter schläft (oder ist noch nicht da), er kämpft, er ist besiegt. */
    public enum Phase implements StringRepresentable {
        SLEEPING(9), AWAKE(13), DEFEATED(2);

        public final int light;

        Phase(int light) {
            this.light = light;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public KeeperStoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PHASE, Phase.SLEEPING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PHASE);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KeeperStoneBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != FaeBlocks.KEEPER_STONE_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof KeeperStoneBlockEntity stone) {
                stone.serverTick(server);
            }
        };
    }

    /** Ein Mondstein auf den erloschenen Stein: Geröll fliegt zusammen, und ein neuer Hüter erhebt sich. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(FaeBlocks.MOONSTONE.get()) || state.getValue(PHASE) != Phase.DEFEATED) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof KeeperStoneBlockEntity stone) {
            if (!stone.recall(server, player)) {
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
                case SLEEPING -> "block.kingdomsatwar.keeper_stone.hint";
                case AWAKE -> "block.kingdomsatwar.keeper_stone.awake";
                case DEFEATED -> "block.kingdomsatwar.keeper_stone.defeated";
            };
            player.sendOverlayMessage(Component.translatable(key).withStyle(state.getValue(PHASE) == Phase.DEFEATED ? ChatFormatting.GRAY
                    : ChatFormatting.AQUA));
        }
        return InteractionResult.SUCCESS;
    }

    /** Das Zeichen glimmt: Mondfunken steigen auf (erloschen nur selten einer). */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Phase phase = state.getValue(PHASE);
        if (phase == Phase.DEFEATED && random.nextInt(10) != 0) {
            return;
        }
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
        level.addParticle(ModParticles.FAE_SPARK.get(), x, pos.getY() + 1.05, z, 0.0, 0.02 + random.nextDouble() * 0.02, 0.0);
        if (phase == Phase.AWAKE && random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 0.0, 0.06, 0.0);
        }
    }
}
