package io.github.sirschrolli.kingdomsatwar.block.entity;

import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.shadow.NyxarEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlockEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.world.LairRecords;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
 * Wacht über Nyxars Thronhof in der Schattenzitadelle: warnt näherkommende Spieler, lässt Nyxar Gestalt annehmen,
 * sobald jemand den Thronhof betritt, und merkt sich, ob er besiegt ist. Zieht sich Nyxar zurück (niemand mehr im Hof),
 * wartet der Thron eine Minute, bevor die Schatten erneut erwachen.
 */
public class ShadowThroneCoreBlockEntity extends BlockEntity {
    /** So nah (waagerecht) an der Hofmitte erwacht Nyxar – das ist der Thronhof innerhalb der Mauern. */
    public static final double WAKE_RADIUS = 24.0;
    private static final double WARN_RADIUS = 110.0;

    private @Nullable BlockPos center;
    private LairRecords.Phase phase = LairRecords.Phase.DORMANT;
    private @Nullable UUID boss;
    private int cooldown;
    private int missingBoss;
    private final Map<UUID, Long> warned = new HashMap<>();

    public ShadowThroneCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHADOW_THRONE_CORE.get(), pos, state);
    }

    public void setup(BlockPos center) {
        this.center = center.immutable();
        this.setChanged();
    }

    /** Mitte des Thronhofs (dort erwacht Nyxar). */
    public BlockPos center() {
        // ohne Angabe: so, wie die Zitadelle ihn setzt (drei Blöcke unter dem Podest, 25 Blöcke nördlich der Hofmitte)
        return this.center != null ? this.center : this.worldPosition.offset(0, 4, 25);
    }

    public LairRecords.Phase phase() {
        return this.phase;
    }

    /** Steht jemand im Thronhof? */
    public static boolean wakes(BlockPos center, double x, double y, double z) {
        double dx = x - (center.getX() + 0.5);
        double dz = z - (center.getZ() + 0.5);
        double dy = y - center.getY();
        return dx * dx + dz * dz <= WAKE_RADIUS * WAKE_RADIUS && dy > -4.0 && dy < 18.0;
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
            if (wakes(c, player.getX(), player.getY(), player.getZ())) {
                this.awaken(level);
                return;
            }
            double dx = player.getX() - (c.getX() + 0.5);
            double dz = player.getZ() - (c.getZ() + 0.5);
            double dy = player.getY() - c.getY();
            if (dx * dx + dz * dz < WARN_RADIUS * WARN_RADIUS && dy > -40 && dy < 80) {
                long now = level.getGameTime();
                Long last = this.warned.get(player.getUUID());
                if (last == null || now - last > 900) {
                    this.warned.put(player.getUUID(), now);
                    player.sendOverlayMessage(Component.translatable("citadel.kingdomsatwar.warning").withColor(0xB46CFF));
                    level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.0f, 0.5f);
                }
            }
        }
    }

    /** Lässt Nyxar erwachen (auch für Tests und Befehle). */
    public @Nullable NyxarEntity awaken(ServerLevel level) {
        NyxarEntity entity = ModEntities.NYXAR.get().create(level, EntitySpawnReason.STRUCTURE);
        if (entity == null) {
            return null;
        }
        BlockPos c = this.center();
        // er blickt vom Thron (Norden) aus zum Tor (Süden)
        float yaw = 0.0f;
        entity.snapTo(c.getX() + 0.5, c.getY(), c.getZ() + 0.5, yaw, 0.0f);
        entity.yBodyRot = yaw;
        entity.yHeadRot = yaw;
        entity.setLairCore(this.worldPosition);
        level.addFreshEntity(entity);
        entity.beginAwakening(level, c);
        this.boss = entity.getUUID();
        this.phase = LairRecords.Phase.AWAKE;
        this.missingBoss = 0;
        this.setChanged();
        return entity;
    }

    private void tickAwake(ServerLevel level) {
        Entity entity = this.boss != null ? level.getEntity(this.boss) : null;
        if (entity instanceof DragonBossEntity && entity.isAlive()) {
            this.missingBoss = 0;
            return;
        }
        if (++this.missingBoss > 60 && entity == null) {
            this.phase = LairRecords.Phase.DORMANT;
            this.cooldown = 200;
            this.boss = null;
            this.setChanged();
        }
    }

    /** Nyxar ist besiegt: Der Thron bleibt leer. */
    public void onBossDefeated(ServerLevel level) {
        this.phase = LairRecords.Phase.DEFEATED;
        this.boss = null;
        this.setChanged();
    }

    /** Nyxar hat sich zurückgezogen: Nach einer Minute kann er erneut erwachen. */
    public void onBossSlumber(ServerLevel level) {
        if (this.phase != LairRecords.Phase.DEFEATED) {
            this.phase = LairRecords.Phase.DORMANT;
            this.cooldown = 1200;
            this.boss = null;
            this.setChanged();
        }
    }

    /** Setzt den Thron zurück (für Befehle und Tests). Ein wacher Nyxar verschwindet. */
    public void reset(ServerLevel level) {
        if (this.boss != null && level.getEntity(this.boss) instanceof DragonBossEntity entity) {
            entity.discard();
        }
        this.phase = LairRecords.Phase.DORMANT;
        this.cooldown = 0;
        this.boss = null;
        this.missingBoss = 0;
        this.warned.clear();
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.center != null) {
            output.store("Center", BlockPos.CODEC, this.center);
        }
        output.putString("Phase", this.phase.name());
        if (this.boss != null) {
            output.store("Boss", UUIDUtil.CODEC, this.boss);
        }
        output.putInt("Cooldown", this.cooldown);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.center = input.read("Center", BlockPos.CODEC).orElse(null);
        try {
            this.phase = LairRecords.Phase.valueOf(input.getStringOr("Phase", "DORMANT"));
        } catch (IllegalArgumentException e) {
            this.phase = LairRecords.Phase.DORMANT;
        }
        Optional<UUID> id = input.read("Boss", UUIDUtil.CODEC);
        this.boss = id.orElse(null);
        this.cooldown = input.getIntOr("Cooldown", 0);
    }
}
