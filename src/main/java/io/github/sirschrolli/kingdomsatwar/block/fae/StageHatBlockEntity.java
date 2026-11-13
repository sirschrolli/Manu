package io.github.sirschrolli.kingdomsatwar.block.fae;

import io.github.sirschrolli.kingdomsatwar.entity.fae.MissMystikEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Wacht über den großen Zauberzylinder: Tritt jemand näher als zehn Blöcke heran, beginnt die Vorstellung – MissMystik
 * springt heraus. Findet sie keine Zuschauer mehr, verschwindet sie wieder darin (der Zylinder wartet); nach ihrem Sieg
 * ist er leer, und Feenstaub hinein ruft sie sofort zurück.
 */
public class StageHatBlockEntity extends BlockEntity implements io.github.sirschrolli.kingdomsatwar.arena.BossLair {
    /** So nah (waagerecht) am Zylinder beginnt die Vorstellung. */
    public static final double WAKE_RADIUS = 10.0;
    private static final double WARN_RADIUS = 40.0;

    public enum Phase { WAITING, SHOW, EMPTY }

    private Phase phase = Phase.WAITING;
    private @Nullable UUID performer;
    private int missing;
    /** Ein Abbild in MissMystiks Kampfdimension (der Mondbühne)? */
    private boolean replica;
    private final Map<UUID, Long> warned = new HashMap<>();

    public StageHatBlockEntity(BlockPos pos, BlockState state) {
        super(MystikBlocks.STAGE_HAT_ENTITY.get(), pos, state);
    }

    public Phase phase() {
        return this.phase;
    }

    /** Ob diese MissMystik die ist, die zu diesem Zylinder gehört (sonst ist sie eine verirrte Doppelgängerin). */
    public boolean owns(Entity entity) {
        return this.phase != Phase.EMPTY && entity.getUUID().equals(this.performer);
    }

    public static boolean wakes(BlockPos hat, double x, double y, double z) {
        double dx = x - (hat.getX() + 0.5);
        double dz = z - (hat.getZ() + 0.5);
        double dy = y - hat.getY();
        return dx * dx + dz * dz <= WAKE_RADIUS * WAKE_RADIUS && dy > -5.0 && dy < 10.0;
    }

    private static boolean counts(Player player) {
        return !player.isSpectator() && !player.isCreative() && player.isAlive();
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 10 != 0) {
            return;
        }
        switch (this.phase) {
            case WAITING -> this.tickWaiting(level);
            case SHOW -> this.tickShow(level);
            case EMPTY -> {
            }
        }
    }

    private @Nullable MissMystikEntity performer(ServerLevel level) {
        return this.performer != null && level.getEntity(this.performer) instanceof MissMystikEntity entity && entity.isAlive() ? entity : null;
    }

    private void tickWaiting(ServerLevel level) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        BlockPos s = this.worldPosition;
        for (ServerPlayer player : level.players()) {
            if (!counts(player)) {
                continue;
            }
            if (wakes(s, player.getX(), player.getY(), player.getZ())) {
                // sie holt ihr Publikum auf die Mondbühne (nur in Tests und im Abbild spielt sie hier)
                if (!this.replica && io.github.sirschrolli.kingdomsatwar.arena.BossArenas.requestPull(level, this.worldPosition)) {
                    this.arenaAway(level);
                    return;
                }
                this.begin(level);
                return;
            }
            double dx = player.getX() - (s.getX() + 0.5);
            double dz = player.getZ() - (s.getZ() + 0.5);
            double dy = player.getY() - s.getY();
            if (dx * dx + dz * dz < WARN_RADIUS * WARN_RADIUS && dy > -30 && dy < 30) {
                long now = level.getGameTime();
                Long last = this.warned.get(player.getUUID());
                if (last == null || now - last > 900) {
                    this.warned.put(player.getUUID(), now);
                    player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.stage_hat.warning").withColor(0xE8A8FF));
                }
            }
        }
    }

    /** Beginnt die Vorstellung (für die Vorführung und Befehle) – sofern der Zylinder wartet. */
    public @Nullable MissMystikEntity summon(ServerLevel level) {
        if (this.phase != Phase.WAITING) {
            return this.performer(level);
        }
        return this.begin(level);
    }

    /** Die Vorstellung beginnt: MissMystik steckt im Zylinder, Trommelwirbel – und gleich springt sie heraus. */
    private @Nullable MissMystikEntity begin(ServerLevel level) {
        MissMystikEntity entity = ModEntities.MISS_MYSTIK.get().create(level, EntitySpawnReason.STRUCTURE);
        if (entity == null) {
            return null;
        }
        BlockPos s = this.worldPosition;
        entity.snapTo(s.getX() + 0.5, s.getY() + 0.2, s.getZ() + 0.5, level.getRandom().nextFloat() * 360.0f, 0.0f);
        entity.bindTo(s);
        entity.appear();
        level.addFreshEntity(entity);
        this.performer = entity.getUUID();
        this.missing = 0;
        this.setPhase(level, Phase.SHOW);
        level.sendParticles(ModParticles.FAE_SPARK.get(), s.getX() + 0.5, s.getY() + 1.2, s.getZ() + 0.5, 30, 0.4, 0.5, 0.4, 0.05);
        return entity;
    }

    private void tickShow(ServerLevel level) {
        if (!this.replica && io.github.sirschrolli.kingdomsatwar.arena.BossArenas.busy(level, this.worldPosition)) {
            // drüben auf der Mondbühne läuft die Vorstellung: wer jetzt an den Zylinder tritt, folgt den anderen
            this.missing = 0;
            for (ServerPlayer player : level.players()) {
                if (counts(player) && wakes(this.worldPosition, player.getX(), player.getY(), player.getZ())) {
                    io.github.sirschrolli.kingdomsatwar.arena.BossArenas.join(level, this.worldPosition, player);
                }
            }
            return;
        }
        MissMystikEntity entity = this.performer(level);
        if (entity != null) {
            this.missing = 0;
            return;
        }
        // fort, ohne besiegt zu sein (etwa Chunk entladen und Welt neu geladen): nach einer Weile wartet der Zylinder wieder
        if (++this.missing > 60) {
            this.performer = null;
            this.missing = 0;
            this.setPhase(level, Phase.WAITING);
        }
    }

    /** Sie hat keine Zuschauer mehr gefunden und ist in den Zylinder zurückgekehrt: er wartet wieder. */
    public void onPerformerDeparted(ServerLevel level) {
        if (this.phase == Phase.EMPTY) {
            return;
        }
        this.performer = null;
        this.setPhase(level, Phase.WAITING);
    }

    public void onPerformerDefeated(ServerLevel level) {
        this.performer = null;
        this.setPhase(level, Phase.EMPTY);
        level.playSound(null, this.worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 2.0f, 0.6f);
    }

    /** Feenstaub in den leeren Zylinder: MissMystik kehrt sofort für eine neue Vorstellung zurück. */
    public boolean refill(ServerLevel level, @Nullable Player player) {
        if (this.phase != Phase.EMPTY) {
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.stage_hat.peaceful").withStyle(ChatFormatting.GRAY));
            }
            return false;
        }
        this.phase = Phase.WAITING;
        this.setState(level, StageHatBlock.Phase.WAITING);
        level.sendParticles(ModParticles.FAE_SPARK.get(), this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.2,
                this.worldPosition.getZ() + 0.5, 40, 0.4, 0.6, 0.4, 0.08);
        if (!this.replica && io.github.sirschrolli.kingdomsatwar.arena.BossArenas.requestPull(level, this.worldPosition)) {
            this.arenaAway(level);
            return true;
        }
        return this.begin(level) != null;
    }

    /** Setzt den Zylinder zurück (für Befehle und Tests); eine MissMystik verschwindet. */
    public void reset(ServerLevel level) {
        MissMystikEntity entity = this.performer(level);
        if (entity != null) {
            entity.discard();
        }
        this.performer = null;
        this.missing = 0;
        this.setPhase(level, Phase.WAITING);
    }

    private void setPhase(ServerLevel level, Phase phase) {
        this.phase = phase;
        this.setState(level, switch (phase) {
            case WAITING -> StageHatBlock.Phase.WAITING;
            case SHOW -> StageHatBlock.Phase.SHOW;
            case EMPTY -> StageHatBlock.Phase.EMPTY;
        });
        this.setChanged();
    }

    private void setState(ServerLevel level, StageHatBlock.Phase phase) {
        BlockState state = level.getBlockState(this.worldPosition);
        if (state.hasProperty(StageHatBlock.PHASE) && state.getValue(StageHatBlock.PHASE) != phase) {
            level.setBlock(this.worldPosition, state.setValue(StageHatBlock.PHASE, phase), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Phase", this.phase.name());
        if (this.performer != null) {
            output.store("Performer", UUIDUtil.CODEC, this.performer);
        }
        if (this.replica) {
            output.putBoolean("ArenaReplica", true);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        try {
            this.phase = Phase.valueOf(input.getStringOr("Phase", Phase.WAITING.name()));
        } catch (IllegalArgumentException e) {
            this.phase = Phase.WAITING;
        }
        this.performer = input.read("Performer", UUIDUtil.CODEC).orElse(null);
        this.replica = input.getBooleanOr("ArenaReplica", false);
    }

    // =====================================================================================
    // Die Kampfdimension (die Mondbühne)
    // =====================================================================================

    @Override
    public io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind arenaKind() {
        return io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.MYSTIK;
    }

    @Override
    public BlockPos arenaFocus() {
        return this.worldPosition;
    }

    /** Vorn auf der Bühne, drei Schritte vor dem Zylinder. */
    @Override
    public BlockPos arenaLootSpot() {
        return this.worldPosition.offset(0, 0, 3);
    }

    @Override
    public boolean arenaDefeated() {
        return this.phase == Phase.EMPTY;
    }

    @Override
    public net.minecraft.world.entity.@Nullable LivingEntity arenaBoss(ServerLevel level) {
        return this.performer(level);
    }

    @Override
    public net.minecraft.world.entity.@Nullable LivingEntity arenaAwaken(ServerLevel level) {
        return this.phase == Phase.WAITING ? this.begin(level) : this.performer(level);
    }

    @Override
    public void arenaAway(ServerLevel level) {
        MissMystikEntity entity = this.performer(level);
        if (entity != null) {
            entity.discard();
        }
        this.performer = null;
        this.missing = 0;
        this.setPhase(level, Phase.SHOW);
    }

    @Override
    public void arenaVictory(ServerLevel level) {
        this.onPerformerDefeated(level);
    }

    @Override
    public void arenaReset(ServerLevel level) {
        this.reset(level);
    }

    @Override
    public void arenaPrepareReplica(ServerLevel level) {
        this.replica = true;
        this.reset(level);
    }

    @Override
    public boolean isArenaReplica() {
        return this.replica;
    }
}
