package io.github.sirschrolli.kingdomsatwar.block.end;

import io.github.sirschrolli.kingdomsatwar.entity.end.AsteriaEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.StarBlocks;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
 * Wacht über den Sternstein: Tritt jemand näher als neun Blöcke heran, stößt Asteria in einer Säule aus Sternenlicht vom
 * Himmel herab und kämpft. Findet sie niemanden mehr, kehrt sie in den Himmel zurück (der Stein ruht wieder); nach ihrem
 * Sieg erlischt er, und ein Endkristall entzündet ihn neu – sie kehrt sofort zurück.
 */
public class StarstoneBlockEntity extends BlockEntity implements io.github.sirschrolli.kingdomsatwar.arena.BossLair {
    /** So nah (waagerecht) am Sternstein stößt sie herab. */
    public static final double WAKE_RADIUS = 9.0;
    private static final double WARN_RADIUS = 40.0;

    public enum Phase { DORMANT, AWAKE, DEFEATED }

    private Phase phase = Phase.DORMANT;
    private @Nullable UUID rider;
    private int missing;
    /** Ein Abbild in Asterias Kampfdimension (dem Sternenzelt)? */
    private boolean replica;
    private final Map<UUID, Long> warned = new HashMap<>();

    public StarstoneBlockEntity(BlockPos pos, BlockState state) {
        super(StarBlocks.STARSTONE_ENTITY.get(), pos, state);
    }

    public Phase phase() {
        return this.phase;
    }

    /** Ob diese Reiterin die ist, die zu diesem Stein gehört (sonst ist sie eine verirrte Doppelgängerin). */
    public boolean owns(Entity entity) {
        return this.phase != Phase.DEFEATED && entity.getUUID().equals(this.rider);
    }

    public static boolean wakes(BlockPos stone, double x, double y, double z) {
        double dx = x - (stone.getX() + 0.5);
        double dz = z - (stone.getZ() + 0.5);
        double dy = y - stone.getY();
        return dx * dx + dz * dz <= WAKE_RADIUS * WAKE_RADIUS && dy > -5.0 && dy < 14.0;
    }

    private static boolean counts(Player player) {
        return !player.isSpectator() && !player.isCreative() && player.isAlive();
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 10 != 0) {
            return;
        }
        switch (this.phase) {
            case DORMANT -> this.tickDormant(level);
            case AWAKE -> this.tickAwake(level);
            case DEFEATED -> {
            }
        }
    }

    private @Nullable AsteriaEntity rider(ServerLevel level) {
        return this.rider != null && level.getEntity(this.rider) instanceof AsteriaEntity entity && entity.isAlive() ? entity : null;
    }

    private void tickDormant(ServerLevel level) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        BlockPos s = this.worldPosition;
        for (ServerPlayer player : level.players()) {
            if (!counts(player)) {
                continue;
            }
            if (wakes(s, player.getX(), player.getY(), player.getZ())) {
                // sie zieht ihre Gegner ins Sternenzelt (nur in Tests und im Abbild kämpft sie hier)
                if (!this.replica && io.github.sirschrolli.kingdomsatwar.arena.BossArenas.requestPull(level, this.worldPosition)) {
                    this.arenaAway(level);
                    return;
                }
                this.descend(level);
                return;
            }
            double dx = player.getX() - (s.getX() + 0.5);
            double dz = player.getZ() - (s.getZ() + 0.5);
            double dy = player.getY() - s.getY();
            if (dx * dx + dz * dz < WARN_RADIUS * WARN_RADIUS && dy > -40 && dy < 40) {
                long now = level.getGameTime();
                Long last = this.warned.get(player.getUUID());
                if (last == null || now - last > 900) {
                    this.warned.put(player.getUUID(), now);
                    player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.starstone.warning").withColor(0x9AD8FF));
                }
            }
        }
    }

    /** Ruft sie herab (für die Vorführung und Befehle) – sofern der Stein ruht. */
    public @Nullable AsteriaEntity summon(ServerLevel level) {
        if (this.phase != Phase.DORMANT) {
            return this.rider(level);
        }
        return this.descend(level);
    }

    /** Asteria stößt vom Himmel herab: hoch über dem Stein erscheint sie und kreist in weiter Spirale herunter. */
    private @Nullable AsteriaEntity descend(ServerLevel level) {
        AsteriaEntity entity = ModEntities.ASTERIA.get().create(level, EntitySpawnReason.STRUCTURE);
        if (entity == null) {
            return null;
        }
        BlockPos s = this.worldPosition;
        float angle = level.getRandom().nextFloat() * Mth.TWO_PI;
        double x = s.getX() + 0.5 + Mth.cos(angle) * 12.0;
        double z = s.getZ() + 0.5 + Mth.sin(angle) * 12.0;
        entity.snapTo(x, s.getY() + AsteriaEntity.DESCENT, z, angle * Mth.RAD_TO_DEG, 0.0f);
        entity.bindTo(s, angle);
        entity.descend();
        level.addFreshEntity(entity);
        this.rider = entity.getUUID();
        this.missing = 0;
        this.setPhase(level, Phase.AWAKE);
        level.playSound(null, s, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 3.0f, 0.6f);
        level.sendParticles(ParticleTypes.END_ROD, s.getX() + 0.5, s.getY() + 1.2, s.getZ() + 0.5, 40, 0.3, 0.6, 0.3, 0.12);
        return entity;
    }

    private void tickAwake(ServerLevel level) {
        if (!this.replica && io.github.sirschrolli.kingdomsatwar.arena.BossArenas.busy(level, this.worldPosition)) {
            // drüben im Sternenzelt wird gekämpft: wer jetzt an den Stein tritt, folgt den anderen
            this.missing = 0;
            for (ServerPlayer player : level.players()) {
                if (counts(player) && wakes(this.worldPosition, player.getX(), player.getY(), player.getZ())) {
                    io.github.sirschrolli.kingdomsatwar.arena.BossArenas.join(level, this.worldPosition, player);
                }
            }
            return;
        }
        AsteriaEntity entity = this.rider(level);
        if (entity != null) {
            this.missing = 0;
            return;
        }
        // fort, ohne besiegt zu sein (etwa Chunk entladen und Welt neu geladen): nach einer Weile ruht der Stein wieder
        if (++this.missing > 60) {
            this.rider = null;
            this.missing = 0;
            this.setPhase(level, Phase.DORMANT);
        }
    }

    /** Sie hat niemanden mehr gefunden und ist in den Himmel zurückgekehrt: der Stein ruht wieder. */
    public void onRiderDeparted(ServerLevel level) {
        if (this.phase == Phase.DEFEATED) {
            return;
        }
        this.rider = null;
        this.setPhase(level, Phase.DORMANT);
    }

    public void onRiderDefeated(ServerLevel level) {
        this.rider = null;
        this.setPhase(level, Phase.DEFEATED);
        level.playSound(null, this.worldPosition, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 2.0f, 0.5f);
        level.sendParticles(ParticleTypes.END_ROD, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0,
                this.worldPosition.getZ() + 0.5, 30, 0.3, 0.5, 0.3, 0.05);
    }

    /** Entzündet den erloschenen Stein mit einem Endkristall neu und ruft Asteria zurück. */
    public boolean rekindle(ServerLevel level, @Nullable Player player) {
        if (this.phase != Phase.DEFEATED) {
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.starstone.peaceful").withStyle(ChatFormatting.GRAY));
            }
            return false;
        }
        this.phase = Phase.DORMANT;
        this.setState(level, StarstoneBlock.Phase.DORMANT);
        level.playSound(null, this.worldPosition, ModSounds.ASTERIA_CONSTELLATION.get(), SoundSource.BLOCKS, 2.0f, 1.2f);
        level.sendParticles(ParticleTypes.END_ROD, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0,
                this.worldPosition.getZ() + 0.5, 50, 0.3, 0.8, 0.3, 0.1);
        if (!this.replica && io.github.sirschrolli.kingdomsatwar.arena.BossArenas.requestPull(level, this.worldPosition)) {
            this.arenaAway(level);
            return true;
        }
        return this.descend(level) != null;
    }

    /** Setzt den Stein zurück (für Befehle und Tests); eine Reiterin verschwindet. */
    public void reset(ServerLevel level) {
        AsteriaEntity entity = this.rider(level);
        if (entity != null) {
            entity.discard();
        }
        this.rider = null;
        this.missing = 0;
        this.setPhase(level, Phase.DORMANT);
    }

    private void setPhase(ServerLevel level, Phase phase) {
        this.phase = phase;
        this.setState(level, switch (phase) {
            case DORMANT -> StarstoneBlock.Phase.DORMANT;
            case AWAKE -> StarstoneBlock.Phase.AWAKE;
            case DEFEATED -> StarstoneBlock.Phase.DIM;
        });
        this.setChanged();
    }

    private void setState(ServerLevel level, StarstoneBlock.Phase phase) {
        BlockState state = level.getBlockState(this.worldPosition);
        if (state.hasProperty(StarstoneBlock.PHASE) && state.getValue(StarstoneBlock.PHASE) != phase) {
            level.setBlock(this.worldPosition, state.setValue(StarstoneBlock.PHASE, phase), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Phase", this.phase.name());
        if (this.rider != null) {
            output.store("Rider", UUIDUtil.CODEC, this.rider);
        }
        if (this.replica) {
            output.putBoolean("ArenaReplica", true);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        try {
            this.phase = Phase.valueOf(input.getStringOr("Phase", Phase.DORMANT.name()));
        } catch (IllegalArgumentException e) {
            this.phase = Phase.DORMANT;
        }
        this.rider = input.read("Rider", UUIDUtil.CODEC).orElse(null);
        this.replica = input.getBooleanOr("ArenaReplica", false);
    }

    // =====================================================================================
    // Die Kampfdimension (das Sternenzelt)
    // =====================================================================================

    @Override
    public io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind arenaKind() {
        return io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.ASTERIA;
    }

    @Override
    public BlockPos arenaFocus() {
        return this.worldPosition;
    }

    /** Auf dem Pflaster des Sternenhofs, drei Schritte vor dem Pfeiler des Sternsteins. */
    @Override
    public BlockPos arenaLootSpot() {
        return this.worldPosition.offset(0, -1, 3);
    }

    @Override
    public boolean arenaDefeated() {
        return this.phase == Phase.DEFEATED;
    }

    @Override
    public net.minecraft.world.entity.@Nullable LivingEntity arenaBoss(ServerLevel level) {
        return this.rider(level);
    }

    @Override
    public net.minecraft.world.entity.@Nullable LivingEntity arenaAwaken(ServerLevel level) {
        return this.phase == Phase.DORMANT ? this.descend(level) : this.rider(level);
    }

    @Override
    public void arenaAway(ServerLevel level) {
        AsteriaEntity entity = this.rider(level);
        if (entity != null) {
            entity.discard();
        }
        this.rider = null;
        this.missing = 0;
        this.setPhase(level, Phase.AWAKE);
    }

    @Override
    public void arenaVictory(ServerLevel level) {
        this.onRiderDefeated(level);
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
