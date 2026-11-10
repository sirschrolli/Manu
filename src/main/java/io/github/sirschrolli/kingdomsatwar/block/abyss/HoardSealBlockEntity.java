package io.github.sirschrolli.kingdomsatwar.block.abyss;

import io.github.sirschrolli.kingdomsatwar.abyss.hoard.HoardPiece;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.WorldEaterEntity;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Wacht über den Schlund der Welten: Solange der Weltenfresser schläft, halten Lichtketten die sieben Kronensplitter mit
 * dem Schlund verbunden; wer sich nähert, spürt das Beben. Betritt jemand den Ring, erwacht er. Zieht er sich zurück
 * (niemand mehr am Ring), wartet der Schlund eine Minute, bevor er erneut erwachen kann; besiegt bleibt er still.
 */
public class HoardSealBlockEntity extends BlockEntity {
    private static final double WARN_RADIUS = 110.0;

    public enum Phase { DORMANT, AWAKE, DEFEATED }

    private @Nullable BlockPos center;
    private Phase phase = Phase.DORMANT;
    private @Nullable UUID eater;
    private int cooldown;
    private int missing;
    private final Map<UUID, Long> warned = new HashMap<>();

    public HoardSealBlockEntity(BlockPos pos, BlockState state) {
        super(AbyssBlocks.HOARD_SEAL_ENTITY.get(), pos, state);
    }

    public void setup(BlockPos center) {
        this.center = center.immutable();
        this.setChanged();
    }

    /** Mitte des Rings (ein Block über dem Boden). */
    public BlockPos center() {
        return this.center != null ? this.center : this.worldPosition.offset(0, 4, -HoardPiece.SEAL_Z);
    }

    public Phase phase() {
        return this.phase;
    }

    /** Steht man auf (oder dicht über) dem Ring? */
    public static boolean wakes(BlockPos center, double x, double y, double z) {
        double dx = x - (center.getX() + 0.5);
        double dz = z - (center.getZ() + 0.5);
        double dy = y - center.getY();
        double r2 = dx * dx + dz * dz;
        return r2 <= (HoardPiece.R_OUT + 1.0) * (HoardPiece.R_OUT + 1.0) && r2 >= (HoardPiece.R_IN - 1.0) * (HoardPiece.R_IN - 1.0) && dy > -3.0
                && dy < 16.0;
    }

    public void serverTick(ServerLevel level) {
        if (this.phase == Phase.DORMANT && level.getGameTime() % 10 == 5) {
            this.chains(level);
        }
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

    /** Lichtketten von den Kronensplittern zum Schlund (nur, wo noch ein Splitter steht). */
    private void chains(ServerLevel level) {
        BlockPos c = this.center();
        // Funken wandern die Lichtketten entlang: vom schwebenden Kristall zum Siegelstern über dem Schlund
        Vec3 hub = new Vec3(c.getX() + 0.5, c.getY() + 5.5, c.getZ() + 0.5);
        for (int k = 0; k < HoardPiece.SHARDS; k++) {
            BlockPos shard = HoardPiece.shardPosition(c, k);
            if (!level.getBlockState(shard).is(AbyssBlocks.SEAL_SHARD.get())) {
                continue;
            }
            Vec3 from = Vec3.atBottomCenterOf(shard).add(0.0, 2.5, 0.0);
            Vec3 d = hub.subtract(from);
            for (int i = 0; i < 3; i++) {
                Vec3 p = from.add(d.scale(level.getRandom().nextDouble()));
                level.sendParticles(new DustParticleOptions(0xC8A8FF, 0.9f), p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
            }
        }
        level.sendParticles(ParticleTypes.END_ROD, hub.x, hub.y, hub.z, 2, 0.4, 0.4, 0.4, 0.02);
    }

    private void tickDormant(ServerLevel level) {
        if (this.cooldown > 0) {
            this.cooldown -= 10;
            return;
        }
        BlockPos c = this.center();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isCreative() || !player.isAlive()) {
                continue;
            }
            if (wakes(c, player.getX(), player.getY(), player.getZ())) {
                this.awaken(level);
                return;
            }
            double dx = player.getX() - (c.getX() + 0.5);
            double dz = player.getZ() - (c.getZ() + 0.5);
            double dy = player.getY() - c.getY();
            if (dx * dx + dz * dz < WARN_RADIUS * WARN_RADIUS && dy > -60 && dy < 60) {
                long now = level.getGameTime();
                Long last = this.warned.get(player.getUUID());
                if (last == null || now - last > 900) {
                    this.warned.put(player.getUUID(), now);
                    player.sendOverlayMessage(Component.translatable("hoard.kingdomsatwar.warning").withColor(0xFF8A5A));
                    level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.4f, 0.4f);
                }
            }
        }
    }

    /** Lässt den Weltenfresser erwachen (auch für Tests und Befehle). */
    public @Nullable WorldEaterEntity awaken(ServerLevel level) {
        WorldEaterEntity entity = ModEntities.WORLD_EATER.get().create(level, EntitySpawnReason.STRUCTURE);
        if (entity == null) {
            return null;
        }
        BlockPos c = this.center();
        entity.snapTo(c.getX() + 0.5, c.getY() - 36.0, c.getZ() + 0.5, 0.0f, -90.0f);
        entity.bindTo(c, this.worldPosition);
        level.addFreshEntity(entity);
        entity.beginAwakening(level);
        this.eater = entity.getUUID();
        this.phase = Phase.AWAKE;
        this.missing = 0;
        this.setChanged();
        return entity;
    }

    private void tickAwake(ServerLevel level) {
        Entity entity = this.eater != null ? level.getEntity(this.eater) : null;
        if (entity instanceof WorldEaterEntity && entity.isAlive()) {
            this.missing = 0;
            return;
        }
        // er ist fort, ohne besiegt zu sein (z. B. Chunk entladen und Welt neu geladen): nach einer Weile neu
        if (++this.missing > 60 && entity == null) {
            this.phase = Phase.DORMANT;
            this.cooldown = 200;
            this.eater = null;
            this.setChanged();
        }
    }

    public void onDefeated(ServerLevel level) {
        this.phase = Phase.DEFEATED;
        this.eater = null;
        this.setChanged();
    }

    public void onSlumber() {
        if (this.phase != Phase.DEFEATED) {
            this.phase = Phase.DORMANT;
            this.cooldown = 1200;
            this.eater = null;
            this.setChanged();
        }
    }

    /** Setzt den Schlund zurück (für Befehle und Tests). Ein wacher Weltenfresser verschwindet. */
    public void reset(ServerLevel level) {
        if (this.eater != null && level.getEntity(this.eater) instanceof WorldEaterEntity entity) {
            entity.discard();
        }
        this.phase = Phase.DORMANT;
        this.cooldown = 0;
        this.eater = null;
        this.missing = 0;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.center != null) {
            output.putLong("Center", this.center.asLong());
        }
        output.putString("Phase", this.phase.name());
        if (this.eater != null) {
            output.store("Eater", UUIDUtil.CODEC, this.eater);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.center = input.getLong("Center").map(BlockPos::of).orElse(null);
        try {
            this.phase = Phase.valueOf(input.getStringOr("Phase", Phase.DORMANT.name()));
        } catch (IllegalArgumentException e) {
            this.phase = Phase.DORMANT;
        }
        this.eater = input.read("Eater", UUIDUtil.CODEC).orElse(null);
    }
}
