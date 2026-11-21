package io.github.sirschrolli.kingdomsatwar.block.abyss;

import io.github.sirschrolli.kingdomsatwar.item.abyss.TimewardenArmor;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssGear;
import io.github.sirschrolli.kingdomsatwar.registry.ClockTower;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
 * Das Uhrwerkherz (seit 1.1): In der Nabe des großen Zifferblatts vor dem Uhrturm sitzt ein Herz aus Uhrbronze, in dessen
 * Mitte ein Chronit glimmt. Darüber steht der Stundenwächter erstarrt, seit die Uhr eine Minute vor Mitternacht stehen blieb.
 * Solange er lebt, tickt das Herz; ist er besiegt, bleibt es stehen – ein Chronit darin zieht es wieder auf, und ein neuer
 * Wächter erwacht. Unzerstörbar; man steht darauf wie auf dem Zifferblatt.
 */
public class ClockHeartBlock extends Block implements EntityBlock {
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);

    /** Der Wächter steht erstarrt (oder ist noch nicht da), er kämpft, er ist besiegt (das Herz steht still). */
    public enum Phase implements StringRepresentable {
        SLEEPING(7), AWAKE(12), DEFEATED(0);

        public final int light;

        Phase(int light) {
            this.light = light;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public ClockHeartBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PHASE, Phase.SLEEPING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PHASE);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ClockHeartBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ClockTower.CLOCK_HEART_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof ClockHeartBlockEntity heart) {
                heart.serverTick(server);
            }
        };
    }

    /** Ein Chronit im stillstehenden Herzen: Es zieht sich auf, die Zeiger springen an, und ein neuer Wächter erwacht. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult) {
        if (!stack.is(AbyssGear.CHRONIT.get()) || state.getValue(PHASE) != Phase.DEFEATED) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof ClockHeartBlockEntity heart) {
            if (!heart.recall(server, player)) {
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
                case SLEEPING -> "block.kingdomsatwar.clock_heart.hint";
                case AWAKE -> "block.kingdomsatwar.clock_heart.awake";
                case DEFEATED -> "block.kingdomsatwar.clock_heart.defeated";
            };
            player.sendOverlayMessage(Component.translatable(key).withStyle(state.getValue(PHASE) == Phase.DEFEATED ? ChatFormatting.GRAY
                    : ChatFormatting.GOLD));
        }
        return InteractionResult.SUCCESS;
    }

    /** Aus der Nabe steigt Bernsteinstaub (wacht der Wächter, sprühen Funken; steht das Herz still, nur selten ein Stäubchen). */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Phase phase = state.getValue(PHASE);
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
        if (phase == Phase.DEFEATED) {
            if (random.nextInt(14) == 0) {
                level.addParticle(new DustParticleOptions(0x6E5A44, 0.8f), x, pos.getY() + 1.05, z, 0.0, 0.01, 0.0);
            }
            return;
        }
        if (random.nextInt(phase == Phase.AWAKE ? 2 : 5) == 0) {
            level.addParticle(TimewardenArmor.AMBER, x, pos.getY() + 1.05, z, 0.0, 0.02, 0.0);
        }
        if (phase == Phase.AWAKE && random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.WAX_ON, x, pos.getY() + 1.1, z, 0.0, 0.03, 0.0);
        }
    }
}
