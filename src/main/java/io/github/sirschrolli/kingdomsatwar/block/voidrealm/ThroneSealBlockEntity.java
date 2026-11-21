package io.github.sirschrolli.kingdomsatwar.block.voidrealm;

import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
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
 * Wacht über den Thron der Alten: warnt, wer sich nähert, lässt den Zerbrochenen König erwachen, sobald jemand den
 * Thronplatz betritt, und merkt sich, ob er besiegt ist. Zieht er sich zurück (niemand mehr auf dem Platz), wartet der
 * Thron eine Minute, bevor er erneut erwachen kann.
 */
public class ThroneSealBlockEntity extends BlockEntity implements io.github.sirschrolli.kingdomsatwar.arena.BossLair {
    /** So nah (waagerecht) an der Mitte des Platzes erwacht der König. */
    public static final double WAKE_RADIUS = 18.0;
    private static final double WARN_RADIUS = 70.0;

    public enum Phase { DORMANT, AWAKE, DEFEATED }

    private @Nullable BlockPos center;
    private Phase phase = Phase.DORMANT;
    private @Nullable UUID king;
    private int cooldown;
    private int missing;
    /** Ein Abbild in der Kampfdimension des Königs (der Thronwelt)? Dort reißt nach seinem Fall kein Riss auf. */
    private boolean replica;
    private final Map<UUID, Long> warned = new HashMap<>();

    public ThroneSealBlockEntity(BlockPos pos, BlockState state) {
        super(VoidBlocks.THRONE_SEAL_ENTITY.get(), pos, state);
    }

    public void setup(BlockPos center) {
        this.center = center.immutable();
        this.setChanged();
    }

    /** Mitte des Thronplatzes (ein Block über dem Boden). */
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
        return dx * dx + dz * dz <= WAKE_RADIUS * WAKE_RADIUS && dy > -3.0 && dy < 14.0;
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 10 != 0) {
            return;
        }
        switch (this.phase) {
            case DORMANT -> this.tickDormant(level);
            case AWAKE -> this.tickAwake(level);
            // besiegt: hinter dem Thron steht der Riss in den Abgrund offen (auch in Welten, in denen der König schon vor
            // 0.72 fiel)
            case DEFEATED -> {
                if (!this.replica) {
                    io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate.ensureThroneGate(level, this.center(), false);
                }
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
                // er zieht seine Gegner in die Thronwelt (nur in Tests und im Abbild kämpft er hier)
                if (!this.replica && io.github.sirschrolli.kingdomsatwar.arena.BossArenas.requestPull(level, this.worldPosition)) {
                    this.arenaAway(level);
                    return;
                }
                this.awaken(level);
                return;
            }
            double dx = player.getX() - (c.getX() + 0.5);
            double dz = player.getZ() - (c.getZ() + 0.5);
            double dy = player.getY() - c.getY();
            if (dx * dx + dz * dz < WARN_RADIUS * WARN_RADIUS && dy > -40 && dy < 60) {
                long now = level.getGameTime();
                Long last = this.warned.get(player.getUUID());
                if (last == null || now - last > 900) {
                    this.warned.put(player.getUUID(), now);
                    player.sendOverlayMessage(Component.translatable("throne.kingdomsatwar.warning").withColor(0xC8A8FF));
                    level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 1.0f, 0.5f);
                }
            }
        }
    }

    /** Lässt den König erwachen (auch für Tests und Befehle). */
    public @Nullable ShatteredKingEntity awaken(ServerLevel level) {
        ShatteredKingEntity entity = ModEntities.SHATTERED_KING.get().create(level, EntitySpawnReason.STRUCTURE);
        if (entity == null) {
            return null;
        }
        BlockPos c = this.center();
        // er erhebt sich vor dem Podest seines Throns (Norden) und blickt über den Platz nach Süden
        entity.snapTo(c.getX() + 0.5, c.getY() + 1.0, c.getZ() - 6.5, 0.0f, 0.0f);
        entity.yBodyRot = 0.0f;
        entity.yHeadRot = 0.0f;
        entity.bindTo(c, this.worldPosition);
        level.addFreshEntity(entity);
        entity.beginAwakening(level);
        this.king = entity.getUUID();
        this.phase = Phase.AWAKE;
        this.missing = 0;
        this.setChanged();
        return entity;
    }

    private void tickAwake(ServerLevel level) {
        if (!this.replica && io.github.sirschrolli.kingdomsatwar.arena.BossArenas.busy(level, this.worldPosition)) {
            // drüben in der Thronwelt wird gekämpft: wer jetzt den Platz betritt, folgt den anderen
            this.missing = 0;
            BlockPos c = this.center();
            for (ServerPlayer player : level.players()) {
                if (!player.isSpectator() && !player.isCreative() && player.isAlive() && wakes(c, player.getX(), player.getY(), player.getZ())) {
                    io.github.sirschrolli.kingdomsatwar.arena.BossArenas.join(level, this.worldPosition, player);
                }
            }
            return;
        }
        Entity entity = this.king != null ? level.getEntity(this.king) : null;
        if (entity instanceof ShatteredKingEntity && entity.isAlive()) {
            this.missing = 0;
            return;
        }
        // der König ist fort, ohne besiegt zu sein (z. B. Chunk entladen und Welt neu geladen): nach einer Weile neu
        if (++this.missing > 60 && entity == null) {
            this.phase = Phase.DORMANT;
            this.cooldown = 200;
            this.king = null;
            this.setChanged();
        }
    }

    public void onKingDefeated(ServerLevel level) {
        this.phase = Phase.DEFEATED;
        this.king = null;
        this.setChanged();
        // hinter dem Thron reißt die Welt auf: der Weg in den Abgrund (nicht im Abbild in der Thronwelt)
        if (!this.replica) {
            io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate.ensureThroneGate(level, this.center(), true);
        }
    }

    public void onKingSlumber() {
        if (this.phase != Phase.DEFEATED) {
            this.phase = Phase.DORMANT;
            this.cooldown = 1200;
            this.king = null;
            this.setChanged();
        }
    }

    /** Setzt den Thron zurück (für Befehle und Tests). Ein wacher König verschwindet. */
    public void reset(ServerLevel level) {
        if (this.king != null && level.getEntity(this.king) instanceof ShatteredKingEntity entity) {
            entity.discard();
        }
        this.phase = Phase.DORMANT;
        this.cooldown = 0;
        this.king = null;
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
        if (this.king != null) {
            output.store("King", UUIDUtil.CODEC, this.king);
        }
        if (this.replica) {
            output.putBoolean("ArenaReplica", true);
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
        this.king = input.read("King", UUIDUtil.CODEC).orElse(null);
        this.replica = input.getBooleanOr("ArenaReplica", false);
    }

    // =====================================================================================
    // Die Kampfdimension (die Thronwelt)
    // =====================================================================================

    @Override
    public io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind arenaKind() {
        return io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.KING;
    }

    /** Die Mitte des Thronplatzes. */
    @Override
    public BlockPos arenaFocus() {
        return this.center();
    }

    /** Mitten auf dem Thronplatz. */
    @Override
    public BlockPos arenaLootSpot() {
        return this.center();
    }

    @Override
    public boolean arenaDefeated() {
        return this.phase == Phase.DEFEATED;
    }

    @Override
    public net.minecraft.world.entity.@Nullable LivingEntity arenaBoss(ServerLevel level) {
        return this.king != null && level.getEntity(this.king) instanceof ShatteredKingEntity entity && entity.isAlive() ? entity : null;
    }

    @Override
    public net.minecraft.world.entity.@Nullable LivingEntity arenaAwaken(ServerLevel level) {
        return this.phase == Phase.DORMANT ? this.awaken(level) : (net.minecraft.world.entity.LivingEntity) this.arenaBoss(level);
    }

    @Override
    public void arenaAway(ServerLevel level) {
        if (this.king != null && level.getEntity(this.king) instanceof ShatteredKingEntity entity) {
            entity.discard();
        }
        this.king = null;
        this.missing = 0;
        this.cooldown = 0;
        this.phase = Phase.AWAKE;
        this.setChanged();
    }

    @Override
    public void arenaVictory(ServerLevel level) {
        this.onKingDefeated(level);
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
