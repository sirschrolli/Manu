package io.github.sirschrolli.kingdomsatwar.block.entity;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Wacht über ein Drachennest: warnt näherkommende Spieler, lässt den Boss erwachen, wenn jemand das Nest betritt,
 * und merkt sich, ob der Drache besiegt ist (dann bleibt das Nest still). Schläft der Boss wieder ein, wartet das
 * Nest eine halbe Minute, bevor es erneut erwacht.
 */
public class DragonLairCoreBlockEntity extends BlockEntity {
    /** Betritt ein Spieler diesen Umkreis (waagerecht) um die Nestmitte, erwacht der Boss. */
    public static final double AWAKEN_RADIUS = 26.0;
    private static final double WARN_RADIUS = 64.0;

    private DragonType element = DragonType.FIRE;
    private @Nullable BlockPos center;
    private LairRecords.Phase phase = LairRecords.Phase.DORMANT;
    private @Nullable UUID boss;
    private int cooldown;
    private int missingBoss;
    private boolean registered;
    private final Map<UUID, Long> warned = new HashMap<>();

    public DragonLairCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRAGON_LAIR_CORE.get(), pos, state);
    }

    public void setup(DragonType element, BlockPos center) {
        this.element = element;
        this.center = center;
        this.setChanged();
    }

    public DragonType element() {
        return this.element;
    }

    public LairRecords.Phase phase() {
        return this.phase;
    }

    public BlockPos center() {
        return this.center != null ? this.center : this.worldPosition.above(7);
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 10 != 0) {
            return;
        }
        if (!this.registered) {
            this.registered = true;
            LairRecords.update(level, this.element, this.center(), this.phase);
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
            double dx = player.getX() - (c.getX() + 0.5);
            double dz = player.getZ() - (c.getZ() + 0.5);
            double dy = player.getY() - c.getY();
            double dist = Math.sqrt(dx * dx + dz * dz);
            // Thalassa erwacht erst, wenn jemand aus dem See ans Ufer der Grotte steigt
            double minDy = this.element == DragonType.WATER ? -2.5 : -16;
            if (dist < AWAKEN_RADIUS && dy > minDy && dy < 40) {
                this.awaken(level);
                return;
            }
            if (dist < WARN_RADIUS && dy > -32 && dy < 64) {
                long now = level.getGameTime();
                Long last = this.warned.get(player.getUUID());
                if (last == null || now - last > 600) {
                    this.warned.put(player.getUUID(), now);
                    player.sendOverlayMessage(Component.translatable("lair.kingdomsatwar." + this.element.id() + ".warning")
                            .withColor(this.element.color()));
                }
            }
        }
    }

    /** Lässt den Boss erwachen (auch für Tests und Befehle). */
    public @Nullable DragonBossEntity awaken(ServerLevel level) {
        EntityType<? extends DragonBossEntity> type = ModEntities.bossType(this.element);
        if (type == null) {
            return null;
        }
        DragonBossEntity entity = type.create(level, EntitySpawnReason.STRUCTURE);
        if (entity == null) {
            return null;
        }
        BlockPos c = this.center();
        float yaw = level.getRandom().nextFloat() * 360.0f;
        entity.snapTo(c.getX() + 0.5, c.getY() - 11, c.getZ() + 0.5, yaw, 0.0f);
        entity.yBodyRot = yaw;
        entity.yHeadRot = yaw;
        entity.setLairCore(this.worldPosition);
        level.addFreshEntity(entity);
        entity.beginAwakening(level, c);
        this.boss = entity.getUUID();
        this.phase = LairRecords.Phase.AWAKE;
        this.missingBoss = 0;
        LairRecords.update(level, this.element, c, this.phase);
        this.setChanged();
        return entity;
    }

    private void tickAwake(ServerLevel level) {
        Entity entity = this.boss != null ? level.getEntity(this.boss) : null;
        if (entity instanceof DragonBossEntity && entity.isAlive()) {
            this.missingBoss = 0;
            return;
        }
        // Boss verschwunden (z. B. per Befehl entfernt) – nach einer Weile wieder schlafen legen
        if (++this.missingBoss > 60 && entity == null) {
            this.phase = LairRecords.Phase.DORMANT;
            this.cooldown = 200;
            this.boss = null;
            this.setChanged();
        }
    }

    /** Der Boss wurde besiegt: Das Nest bleibt still. */
    public void onBossDefeated(ServerLevel level) {
        this.phase = LairRecords.Phase.DEFEATED;
        this.boss = null;
        LairRecords.update(level, this.element, this.center(), this.phase);
        this.setChanged();
    }

    /** Der Boss ist wieder eingeschlafen: Nach einer halben Minute kann er erneut erwachen. */
    public void onBossSlumber(ServerLevel level) {
        if (this.phase != LairRecords.Phase.DEFEATED) {
            this.phase = LairRecords.Phase.DORMANT;
            this.cooldown = 600;
            this.boss = null;
            LairRecords.update(level, this.element, this.center(), this.phase);
            this.setChanged();
        }
    }

    /** Setzt das Nest zurück in den Schlaf – auch ein besiegtes (für Befehle und Tests). Ein wacher Boss verschwindet. */
    public void reset(ServerLevel level) {
        if (this.boss != null && level.getEntity(this.boss) instanceof DragonBossEntity entity) {
            entity.discard();
        }
        this.phase = LairRecords.Phase.DORMANT;
        this.cooldown = 0;
        this.boss = null;
        this.missingBoss = 0;
        this.warned.clear();
        LairRecords.reset(level, this.element, this.center());
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Element", this.element.id());
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
        this.element = DragonType.byId(input.getStringOr("Element", "fire"));
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
