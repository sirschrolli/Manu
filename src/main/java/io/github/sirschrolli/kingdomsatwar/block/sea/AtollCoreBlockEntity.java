package io.github.sirschrolli.kingdomsatwar.block.sea;

import io.github.sirschrolli.kingdomsatwar.entity.sea.ReefColossusEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollPiece;
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
 * Wacht über das Versunkene Atoll: warnt, wer sich nähert, lässt Karkinos erwachen, sobald jemand dem Hügel mit dem Speer
 * zu nahe kommt, und merkt sich, ob er besiegt ist. Gräbt er sich wieder ein (niemand mehr in der Lagune), schließt sich
 * der Hügel, und es dauert eine Minute, bis er erneut erwachen kann. Nach seinem Sieg ruft ihn nur das Gezeitenhorn
 * (mit einem Herz des Meeres) zurück.
 */
public class AtollCoreBlockEntity extends BlockEntity {
    /** So nah (waagerecht) an der Mitte des Hügels erwacht er. */
    public static final double WAKE_RADIUS = 8.0;
    private static final double WARN_RADIUS = 56.0;

    public enum Phase { DORMANT, AWAKE, DEFEATED }

    private @Nullable BlockPos center;
    private Phase phase = Phase.DORMANT;
    private @Nullable UUID colossus;
    private int cooldown;
    private int missing;
    private final Map<UUID, Long> warned = new HashMap<>();

    public AtollCoreBlockEntity(BlockPos pos, BlockState state) {
        super(SeaBlocks.ATOLL_CORE_ENTITY.get(), pos, state);
    }

    public void setup(BlockPos center) {
        this.center = center.immutable();
        this.setChanged();
    }

    /** Mitte der Lagune (auf dem Grund, wo der Hügel liegt). */
    public BlockPos center() {
        return this.center != null ? this.center : this.worldPosition.above(3);
    }

    public Phase phase() {
        return this.phase;
    }

    public static boolean wakes(BlockPos center, double x, double y, double z) {
        double dx = x - (center.getX() + 0.5);
        double dz = z - (center.getZ() + 0.5);
        double dy = y - center.getY();
        return dx * dx + dz * dz <= WAKE_RADIUS * WAKE_RADIUS && dy > -4.0 && dy < 12.0;
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
            if (dx * dx + dz * dz < WARN_RADIUS * WARN_RADIUS && dy > -30 && dy < 50) {
                long now = level.getGameTime();
                Long last = this.warned.get(player.getUUID());
                if (last == null || now - last > 900) {
                    this.warned.put(player.getUUID(), now);
                    player.sendOverlayMessage(Component.translatable("atoll.kingdomsatwar.warning").withColor(0x7AE8FF));
                    level.playSound(null, player.blockPosition(), SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.HOSTILE, 1.0f, 0.5f);
                }
            }
        }
    }

    /** Lässt ihn erwachen (auch für Tests, Befehle und das Gezeitenhorn): der Hügel öffnet sich, er bricht hervor. */
    public @Nullable ReefColossusEntity awaken(ServerLevel level) {
        ReefColossusEntity entity = ModEntities.REEF_COLOSSUS.get().create(level, EntitySpawnReason.STRUCTURE);
        if (entity == null) {
            return null;
        }
        BlockPos c = this.center();
        AtollPiece.clearMound(level, c);
        entity.snapTo(c.getX() + 0.5, c.getY(), c.getZ() + 0.5, 180.0f, 0.0f);
        entity.yBodyRot = 180.0f;
        entity.yHeadRot = 180.0f;
        entity.bindTo(c, this.worldPosition);
        level.addFreshEntity(entity);
        entity.beginAwakening(level);
        this.colossus = entity.getUUID();
        this.phase = Phase.AWAKE;
        this.missing = 0;
        this.setChanged();
        return entity;
    }

    private void tickAwake(ServerLevel level) {
        Entity entity = this.colossus != null ? level.getEntity(this.colossus) : null;
        if (entity instanceof ReefColossusEntity && entity.isAlive()) {
            this.missing = 0;
            return;
        }
        // fort, ohne besiegt zu sein (z. B. Chunk entladen und Welt neu geladen): nach einer Weile wieder schlafend
        if (++this.missing > 60 && entity == null) {
            AtollPiece.buildMound(level, this.center());
            this.phase = Phase.DORMANT;
            this.cooldown = 200;
            this.colossus = null;
            this.setChanged();
        }
    }

    public void onColossusDefeated(ServerLevel level) {
        this.phase = Phase.DEFEATED;
        this.colossus = null;
        this.setChanged();
    }

    public void onColossusSlumber(ServerLevel level) {
        if (this.phase != Phase.DEFEATED) {
            AtollPiece.buildMound(level, this.center());
            this.phase = Phase.DORMANT;
            this.cooldown = 1200;
            this.colossus = null;
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

    /** Setzt das Atoll zurück (für Befehle und Tests). Ein wacher Koloss verschwindet. */
    public void reset(ServerLevel level) {
        if (this.colossus != null && level.getEntity(this.colossus) instanceof ReefColossusEntity entity) {
            entity.discard();
        }
        AtollPiece.buildMound(level, this.center());
        this.phase = Phase.DORMANT;
        this.cooldown = 0;
        this.colossus = null;
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
        if (this.colossus != null) {
            output.store("Colossus", UUIDUtil.CODEC, this.colossus);
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
        this.colossus = input.read("Colossus", UUIDUtil.CODEC).orElse(null);
    }
}
