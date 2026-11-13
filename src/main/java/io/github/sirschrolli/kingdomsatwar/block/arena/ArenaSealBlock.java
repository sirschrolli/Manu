package io.github.sirschrolli.kingdomsatwar.block.arena;

import io.github.sirschrolli.kingdomsatwar.registry.ArenaGear;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
 * Das Arenasiegel (seit 1.9): ein Stein aus gemeißeltem Sandstein mit einer Windspirale aus Kupfer, eingelassen in das Podest
 * der Arena der Prüfungen, genau unter dem Spawner der Böen. Es zählt die Käfige, die bezwungen sind – über dem Podest
 * schweben dafür sechs Windsiegel, eines für jeden Käfig, und jedes leuchtet auf, sobald sein Käfig bezwungen ist. Sind es
 * alle, steigt Ventor, der Gladiator der Winde, aus dem Spawner, sobald jemand die Arena betritt. Unzerstörbar.
 */
public class ArenaSealBlock extends Block implements EntityBlock {
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);

    /** Noch nicht alle Käfige bezwungen · bereit (er steigt auf, sobald jemand kommt) · er kämpft · er ist besiegt. */
    public enum Phase implements StringRepresentable {
        DORMANT, READY, AWAKE, CONQUERED;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public ArenaSealBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PHASE, Phase.DORMANT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PHASE);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArenaSealBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ArenaGear.ARENA_SEAL_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof ArenaSealBlockEntity seal) {
                seal.serverTick(server);
            }
        };
    }

    /** Wer das Siegel berührt, erfährt, wie viele Käfige bezwungen sind. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof ArenaSealBlockEntity seal) {
            seal.tell(server, player);
        }
        return InteractionResult.SUCCESS;
    }

    /** Ist der Meister bereit, wirbelt über dem Podest der Wind. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(PHASE) == Phase.READY && random.nextInt(2) == 0) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double r = 0.9 + random.nextDouble() * 0.5;
            level.addParticle(ParticleTypes.SMALL_GUST, pos.getX() + 0.5 + Math.cos(a) * r, pos.getY() + 1.2 + random.nextDouble() * 1.6,
                    pos.getZ() + 0.5 + Math.sin(a) * r, 0.0, 0.0, 0.0);
        }
    }
}
