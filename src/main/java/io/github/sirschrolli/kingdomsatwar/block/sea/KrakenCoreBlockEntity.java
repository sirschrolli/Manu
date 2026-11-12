package io.github.sirschrolli.kingdomsatwar.block.sea;

import io.github.sirschrolli.kingdomsatwar.entity.sea.KrakenEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
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
import org.jspecify.annotations.Nullable;

/**
 * Wacht über den Krakenschlund (seit 0.91): Wer sich nähert, den warnt die unheimliche Stille über dem Wasser; wer den
 * Felsenring betritt (an der Oberfläche oder tauchend), weckt den Kraken – er steigt aus dem Schlund empor. Sinkt er zurück
 * (niemand mehr im Ring), dauert es eine Minute, bis er erneut emporsteigen kann. Das Herz merkt sich seinen Sieg; danach
 * ruft ihn nur das Gezeitenhorn zurück.
 */
public class KrakenCoreBlockEntity extends BlockEntity {
    /** So nah (waagerecht) an der Mitte steigt er empor – der Felsenring. */
    public static final double WAKE_RADIUS = 30.0;
    private static final double WARN_RADIUS = 72.0;

    public enum Phase { DORMANT, AWAKE, DEFEATED }

    private @Nullable BlockPos center;
    private Phase phase = Phase.DORMANT;
    private @Nullable UUID kraken;
    private int cooldown;
    private int missing;
    private final Map<UUID, Long> warned = new HashMap<>();

    public KrakenCoreBlockEntity(BlockPos pos, BlockState state) {
        super(SeaBlocks.KRAKEN_CORE_ENTITY.get(), pos, state);
    }

    /** Die Mitte des Schlunds an der Wasseroberfläche (dort ragt er empor). */
    public void setup(BlockPos center) {
        this.center = center.immutable();
        this.setChanged();
    }

    public BlockPos center() {
        return this.center != null ? this.center : this.worldPosition.above(60);
    }

    public Phase phase() {
        return this.phase;
    }

    /** Weckt ihn diese Stelle? Im Ring, von knapp über dem Grund bis hoch über das Wasser. */
    public boolean wakes(double x, double y, double z) {
        BlockPos c = this.center();
        double dx = x - (c.getX() + 0.5);
        double dz = z - (c.getZ() + 0.5);
        return dx * dx + dz * dz <= WAKE_RADIUS * WAKE_RADIUS && y > this.worldPosition.getY() - 4 && y < c.getY() + 40;
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
            if (this.wakes(player.getX(), player.getY(), player.getZ())) {
                this.awaken(level);
                return;
            }
            double dx = player.getX() - (c.getX() + 0.5);
            double dz = player.getZ() - (c.getZ() + 0.5);
            if (dx * dx + dz * dz < WARN_RADIUS * WARN_RADIUS && Math.abs(player.getY() - c.getY()) < 70) {
                long now = level.getGameTime();
                Long last = this.warned.get(player.getUUID());
                if (last == null || now - last > 900) {
                    this.warned.put(player.getUUID(), now);
                    player.sendOverlayMessage(Component.translatable("kraken.kingdomsatwar.warning").withColor(0x5AD8E8));
                    level.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, SoundSource.HOSTILE,
                            1.2f, 0.5f);
                }
            }
        }
    }

    /** Lässt ihn emporsteigen (auch für Tests, Befehle und das Gezeitenhorn). */
    public @Nullable KrakenEntity awaken(ServerLevel level) {
        KrakenEntity entity = ModEntities.KRAKEN.get().create(level, EntitySpawnReason.STRUCTURE);
        if (entity == null) {
            return null;
        }
        BlockPos c = this.center();
        entity.snapTo(c.getX() + 0.5, c.getY(), c.getZ() + 0.5, 180.0f, 0.0f);
        entity.yBodyRot = 180.0f;
        entity.yHeadRot = 180.0f;
        entity.bindTo(c, this.worldPosition);
        level.addFreshEntity(entity);
        entity.beginRising(level);
        this.kraken = entity.getUUID();
        this.phase = Phase.AWAKE;
        this.missing = 0;
        this.setChanged();
        return entity;
    }

    private void tickAwake(ServerLevel level) {
        Entity entity = this.kraken != null ? level.getEntity(this.kraken) : null;
        if (entity instanceof KrakenEntity && entity.isAlive()) {
            this.missing = 0;
            return;
        }
        // fort, ohne besiegt zu sein (etwa Chunk entladen und Welt neu geladen): nach einer Weile wieder schlafend
        if (++this.missing > 60 && entity == null) {
            this.phase = Phase.DORMANT;
            this.cooldown = 200;
            this.kraken = null;
            this.setChanged();
        }
    }

    public void onKrakenDefeated(ServerLevel level) {
        this.phase = Phase.DEFEATED;
        this.kraken = null;
        this.setChanged();
    }

    /** Niemand mehr im Ring: er ist zurück in den Schlund gesunken. */
    public void onKrakenDive(ServerLevel level) {
        if (this.phase != Phase.DEFEATED) {
            this.phase = Phase.DORMANT;
            this.cooldown = 1200;
            this.kraken = null;
            this.setChanged();
        }
    }

    /** Ruft den Besiegten mit dem Gezeitenhorn zurück (nur nach seinem Sieg). */
    public boolean recall(ServerLevel level) {
        if (this.phase != Phase.DEFEATED) {
            return false;
        }
        return this.awaken(level) != null;
    }

    /** Setzt den Schlund zurück (für Befehle und Tests). Ein wacher Kraken verschwindet. */
    public void reset(ServerLevel level) {
        if (this.kraken != null && level.getEntity(this.kraken) instanceof KrakenEntity entity) {
            entity.discard();
        }
        this.phase = Phase.DORMANT;
        this.cooldown = 0;
        this.kraken = null;
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
        if (this.kraken != null) {
            output.store("Kraken", UUIDUtil.CODEC, this.kraken);
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
        this.kraken = input.read("Kraken", UUIDUtil.CODEC).orElse(null);
    }
}
