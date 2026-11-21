package io.github.sirschrolli.kingdomsatwar.block.abyss;

import io.github.sirschrolli.kingdomsatwar.entity.abyss.HourWardenEntity;
import io.github.sirschrolli.kingdomsatwar.item.abyss.TimewardenArmor;
import io.github.sirschrolli.kingdomsatwar.registry.ClockTower;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Wacht über den Stundenwächter: Kommt jemand näher als {@link #SPAWN_RADIUS} Blöcke, steht er erstarrt über dem Herzen –
 * fehlt er, setzt er sich aus Zahnrädern wieder zusammen. Ist er besiegt, bleibt das Herz stehen; ein Chronit zieht es wieder
 * auf, und ein neuer Wächter erwacht sofort. Wer sich dem Zifferblatt nähert, hört die Uhr ticken.
 */
public class ClockHeartBlockEntity extends BlockEntity {
    /** So nah muss jemand kommen, damit der Wächter (erstarrt) über dem Herzen steht. */
    public static final double SPAWN_RADIUS = 48.0;
    private static final double WARN_RADIUS = 32.0;

    private ClockHeartBlock.Phase phase = ClockHeartBlock.Phase.SLEEPING;
    private @Nullable UUID warden;
    /** Wo der Wächter steht (relativ zum Herzen; er steht darauf). */
    private BlockPos homeOffset = new BlockPos(0, 1, 0);
    private int missing;
    private final Map<UUID, Long> warned = new HashMap<>();

    public ClockHeartBlockEntity(BlockPos pos, BlockState state) {
        super(ClockTower.CLOCK_HEART_ENTITY.get(), pos, state);
    }

    /** Legt fest, wo der Wächter steht (für das Bauwerk und Tests). */
    public void setHome(BlockPos home) {
        this.homeOffset = home.subtract(this.worldPosition);
        this.setChanged();
    }

    public BlockPos home() {
        return this.worldPosition.offset(this.homeOffset);
    }

    public ClockHeartBlock.Phase phase() {
        return this.phase;
    }

    /** Gehört dieser Wächter zu diesem Herzen? */
    public boolean owns(HourWardenEntity entity) {
        return entity.getUUID().equals(this.warden);
    }

    public @Nullable HourWardenEntity warden(ServerLevel level) {
        if (this.warden != null && level.getEntity(this.warden) instanceof HourWardenEntity entity && entity.isAlive()) {
            return entity;
        }
        List<HourWardenEntity> near = level.getEntitiesOfClass(HourWardenEntity.class, new AABB(this.worldPosition).inflate(44.0),
                e -> e.isAlive() && this.worldPosition.equals(e.heart()));
        if (!near.isEmpty()) {
            this.warden = near.getFirst().getUUID();
            return near.getFirst();
        }
        return null;
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 20 != 11 || this.phase == ClockHeartBlock.Phase.DEFEATED) {
            return;
        }
        Vec3 c = Vec3.atCenterOf(this.worldPosition);
        Player near = level.getNearestPlayer(c.x, c.y, c.z, SPAWN_RADIUS, e -> e instanceof Player p && !p.isSpectator() && p.isAlive());
        HourWardenEntity entity = this.warden(level);
        if (entity == null) {
            if (near == null || level.getDifficulty() == Difficulty.PEACEFUL || !level.isPositionEntityTicking(this.home())) {
                this.missing = 0;
                return;
            }
            // erst nach drei Prüfungen: sein Chunk könnte gerade erst laden
            if (++this.missing >= 3) {
                this.missing = 0;
                this.spawn(level, false);
            }
            return;
        }
        this.missing = 0;
        this.setPhase(level, entity.isDormant() ? ClockHeartBlock.Phase.SLEEPING : ClockHeartBlock.Phase.AWAKE);
        if (entity.isDormant()) {
            this.warn(level);
        }
    }

    /** Die Uhr tickt von fern für alle, die sich nähern (höchstens alle 45 Sekunden je Spieler). */
    private void warn(ServerLevel level) {
        long now = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            if (player.isCreative() || player.isSpectator() || player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) > WARN_RADIUS * WARN_RADIUS) {
                continue;
            }
            Long last = this.warned.get(player.getUUID());
            if (last == null || now - last > 900) {
                this.warned.put(player.getUUID(), now);
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.clock_heart.warning").withColor(0xFFC86A));
                level.playSound(null, this.worldPosition, ModSounds.HOUR_WARDEN_AMBIENT.get(), SoundSource.HOSTILE, 1.4f, 0.8f);
            }
        }
    }

    /** Stellt den Wächter auf das Herz: erstarrt ({@code awake} = false) oder sofort erwachend. Gibt ihn zurück (oder null). */
    public @Nullable HourWardenEntity spawn(ServerLevel level, boolean awake) {
        HourWardenEntity entity = ModEntities.HOUR_WARDEN.get().create(level, EntitySpawnReason.TRIGGERED);
        if (entity == null) {
            return null;
        }
        BlockPos home = this.home();
        if (awake) {
            entity.rise(home, this.worldPosition);
        } else {
            entity.bindTo(home, this.worldPosition);
        }
        level.addFreshEntity(entity);
        this.warden = entity.getUUID();
        this.setPhase(level, awake ? ClockHeartBlock.Phase.AWAKE : ClockHeartBlock.Phase.SLEEPING);
        if (awake) {
            level.playSound(null, home, ModSounds.HOUR_WARDEN_AWAKEN.get(), SoundSource.HOSTILE, 3.0f, 1.0f);
            level.sendParticles(TimewardenArmor.AMBER, home.getX() + 0.5, home.getY() + 2.0, home.getZ() + 0.5, 60, 1.0, 1.4, 1.0, 0.0);
            level.sendParticles(ParticleTypes.WAX_ON, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.1,
                    this.worldPosition.getZ() + 0.5, 30, 0.5, 0.2, 0.5, 0.2);
        }
        this.setChanged();
        return entity;
    }

    /** Ein Chronit im stillstehenden Herzen: Ein neuer Wächter erwacht. */
    public boolean recall(ServerLevel level, @Nullable Player player) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.clock_heart.peaceful").withStyle(ChatFormatting.GRAY));
            }
            return false;
        }
        if (this.warden(level) != null) {
            return false;
        }
        return this.spawn(level, true) != null;
    }

    /** Der Wächter ist besiegt: Das Herz bleibt stehen. */
    public void onWardenDefeated(ServerLevel level) {
        this.warden = null;
        this.setPhase(level, ClockHeartBlock.Phase.DEFEATED);
        this.setChanged();
    }

    /** Für Befehle: zurück auf Anfang (der Wächter, wenn da, verschwindet; beim nächsten Besuch steht ein neuer erstarrt da). */
    public void reset(ServerLevel level) {
        HourWardenEntity entity = this.warden(level);
        if (entity != null) {
            entity.discard();
        }
        this.warden = null;
        this.missing = 0;
        this.setPhase(level, ClockHeartBlock.Phase.SLEEPING);
        this.setChanged();
    }

    private void setPhase(ServerLevel level, ClockHeartBlock.Phase next) {
        this.phase = next;
        BlockState state = this.getBlockState();
        if (state.hasProperty(ClockHeartBlock.PHASE) && state.getValue(ClockHeartBlock.PHASE) != next) {
            level.setBlock(this.worldPosition, state.setValue(ClockHeartBlock.PHASE, next), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Phase", this.phase.getSerializedName());
        if (this.warden != null) {
            output.store("Warden", UUIDUtil.CODEC, this.warden);
        }
        output.putLong("HomeOffset", this.homeOffset.asLong());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String phase = input.getStringOr("Phase", "sleeping");
        for (ClockHeartBlock.Phase p : ClockHeartBlock.Phase.values()) {
            if (p.getSerializedName().equals(phase)) {
                this.phase = p;
            }
        }
        this.warden = input.read("Warden", UUIDUtil.CODEC).orElse(null);
        input.getLong("HomeOffset").ifPresent(v -> this.homeOffset = BlockPos.of(v));
    }
}
