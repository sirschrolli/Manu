package io.github.sirschrolli.kingdomsatwar.block.end;

import io.github.sirschrolli.kingdomsatwar.entity.end.PhasmorEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.PhaseSanctuary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.DustParticleOptions;
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
 * Wacht über den Leerenthron: Kommt jemand näher als {@link #SPAWN_RADIUS} Blöcke, kniet Phasmor schon vor dem Thron und hält
 * Wache (unverwundbar, er bemerkt niemanden) – fehlt er, tritt er aus der Leere. Tritt jemand näher als
 * {@link PhasmorEntity#WAKE_RADIUS} Blöcke heran (oder greift ihn an), erwacht er. Ist er besiegt, erlischt der Phasit in der
 * Lehne; ein Phasit auf dem Thron entzündet ihn neu, und der König erwacht sofort. Wer sich dem Heiligtum nähert, hört das
 * Summen seiner Klinge.
 */
public class VoidThroneBlockEntity extends BlockEntity {
    /** So nah muss jemand kommen, damit der König (kniend) vor dem Thron wacht. */
    public static final double SPAWN_RADIUS = 48.0;
    private static final double WARN_RADIUS = 34.0;

    private VoidThroneBlock.Phase phase = VoidThroneBlock.Phase.GUARDED;
    private @Nullable UUID king;
    private int missing;
    private final Map<UUID, Long> warned = new HashMap<>();

    public VoidThroneBlockEntity(BlockPos pos, BlockState state) {
        super(PhaseSanctuary.VOID_THRONE_ENTITY.get(), pos, state);
    }

    public VoidThroneBlock.Phase phase() {
        return this.phase;
    }

    /** Gehört dieser König zu diesem Thron? */
    public boolean owns(PhasmorEntity entity) {
        return this.phase != VoidThroneBlock.Phase.DEFEATED && entity.getUUID().equals(this.king);
    }

    /** Wohin der Thron blickt: dort kniet der König. */
    public Direction facing() {
        BlockState state = this.getBlockState();
        return state.hasProperty(VoidThroneBlock.FACING) ? state.getValue(VoidThroneBlock.FACING) : Direction.SOUTH;
    }

    /** Der Platz des Königs vor dem Thron: drei Blöcke davor, auf dem Boden (der Thron steht auf Stufen). */
    public BlockPos home() {
        BlockPos p = this.worldPosition.relative(this.facing(), 3);
        if (this.level != null) {
            for (int i = 0; i < 4 && this.level.getBlockState(p.below()).getCollisionShape(this.level, p.below()).isEmpty(); i++) {
                p = p.below();
            }
        }
        return p;
    }

    public static boolean wakes(BlockPos throne, double x, double y, double z) {
        double dx = x - (throne.getX() + 0.5);
        double dz = z - (throne.getZ() + 0.5);
        double dy = y - throne.getY();
        return dx * dx + dz * dz <= PhasmorEntity.WAKE_RADIUS * PhasmorEntity.WAKE_RADIUS && dy > -6.0 && dy < 10.0;
    }

    private static boolean counts(Player player) {
        return !player.isSpectator() && !player.isCreative() && player.isAlive();
    }

    public @Nullable PhasmorEntity king(ServerLevel level) {
        if (this.king != null && level.getEntity(this.king) instanceof PhasmorEntity entity && entity.isAlive()) {
            return entity;
        }
        List<PhasmorEntity> near = level.getEntitiesOfClass(PhasmorEntity.class, new AABB(this.worldPosition).inflate(40.0),
                e -> e.isAlive() && this.worldPosition.equals(e.throne()));
        if (!near.isEmpty() && this.phase != VoidThroneBlock.Phase.DEFEATED) {
            this.king = near.getFirst().getUUID();
            return near.getFirst();
        }
        return null;
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 10 != 7 || this.phase == VoidThroneBlock.Phase.DEFEATED) {
            return;
        }
        Vec3 c = Vec3.atCenterOf(this.worldPosition);
        PhasmorEntity entity = this.king(level);
        if (entity == null) {
            Player near = level.getNearestPlayer(c.x, c.y, c.z, SPAWN_RADIUS, e -> e instanceof Player p && !p.isSpectator() && p.isAlive());
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
        if (entity.isGuarding()) {
            this.setPhase(level, VoidThroneBlock.Phase.GUARDED);
            for (ServerPlayer player : level.players()) {
                if (counts(player) && wakes(this.worldPosition, player.getX(), player.getY(), player.getZ())) {
                    this.awaken(level);
                    return;
                }
            }
            this.warn(level);
        } else {
            this.setPhase(level, VoidThroneBlock.Phase.AWAKE);
        }
    }

    /** Das Summen der Klinge hallt von fern für alle, die sich nähern (höchstens alle 45 Sekunden je Spieler). */
    private void warn(ServerLevel level) {
        long now = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            if (!counts(player) || player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) > WARN_RADIUS * WARN_RADIUS) {
                continue;
            }
            Long last = this.warned.get(player.getUUID());
            if (last == null || now - last > 900) {
                this.warned.put(player.getUUID(), now);
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.void_throne.warning").withColor(PhasmorEntity.PHASIT));
                level.playSound(null, this.worldPosition, ModSounds.PHASMOR_GUARD.get(), SoundSource.HOSTILE, 1.6f, 0.9f);
            }
        }
    }

    /** Stellt den König vor den Thron: kniend ({@code awake} = false) oder sofort erwachend. Gibt ihn zurück (oder null). */
    public @Nullable PhasmorEntity spawn(ServerLevel level, boolean awake) {
        PhasmorEntity entity = ModEntities.PHASMOR.get().create(level, EntitySpawnReason.TRIGGERED);
        if (entity == null) {
            return null;
        }
        BlockPos home = this.home();
        float yaw = this.facing().toYRot();
        entity.snapTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, yaw, 0.0f);
        entity.yBodyRot = yaw;
        entity.yHeadRot = yaw;
        if (awake) {
            entity.rise(home, this.worldPosition);
        } else {
            entity.bindTo(home, this.worldPosition);
        }
        level.addFreshEntity(entity);
        this.king = entity.getUUID();
        this.missing = 0;
        this.setPhase(level, awake ? VoidThroneBlock.Phase.AWAKE : VoidThroneBlock.Phase.GUARDED);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, home.getX() + 0.5, home.getY() + 1.5, home.getZ() + 0.5, 80, 0.5, 1.5, 0.5, 0.05);
        level.sendParticles(new DustParticleOptions(PhasmorEntity.PHASIT, 1.4f), home.getX() + 0.5, home.getY() + 1.5, home.getZ() + 0.5, 40, 0.5,
                1.5, 0.5, 0.0);
        if (awake) {
            level.playSound(null, this.worldPosition, ModSounds.VOID_THRONE_KINDLE.get(), SoundSource.BLOCKS, 2.5f, 1.0f);
        }
        this.setChanged();
        return entity;
    }

    /** Lässt ihn erwachen (auch für Tests und Befehle). */
    public @Nullable PhasmorEntity awaken(ServerLevel level) {
        if (this.phase == VoidThroneBlock.Phase.DEFEATED) {
            return null;
        }
        PhasmorEntity entity = this.king(level);
        if (entity == null) {
            return this.spawn(level, true);
        }
        entity.awaken();
        this.setPhase(level, VoidThroneBlock.Phase.AWAKE);
        return entity;
    }

    /** Der König hat niemanden mehr gefunden und kniet wieder. */
    public void onKingReturned(ServerLevel level, PhasmorEntity entity) {
        if (this.phase == VoidThroneBlock.Phase.DEFEATED) {
            return;
        }
        this.king = entity.getUUID();
        this.setPhase(level, VoidThroneBlock.Phase.GUARDED);
    }

    /** Er ist besiegt: Der Phasit in der Lehne erlischt. */
    public void onKingDefeated(ServerLevel level) {
        this.king = null;
        this.setPhase(level, VoidThroneBlock.Phase.DEFEATED);
        level.playSound(null, this.worldPosition, ModSounds.VOID_THRONE_KINDLE.get(), SoundSource.BLOCKS, 2.0f, 0.5f);
        level.sendParticles(ParticleTypes.ASH, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.6,
                this.worldPosition.getZ() + 0.5, 30, 0.3, 0.4, 0.3, 0.02);
    }

    /** Ein Phasit auf dem leeren Thron: Der Stein flammt auf, und der König kehrt zurück (sofort erwacht). */
    public boolean rekindle(ServerLevel level, @Nullable Player player) {
        if (this.phase != VoidThroneBlock.Phase.DEFEATED) {
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.void_throne.peaceful").withStyle(ChatFormatting.GRAY));
            }
            return false;
        }
        this.phase = VoidThroneBlock.Phase.GUARDED;
        level.sendParticles(new DustParticleOptions(PhasmorEntity.PHASIT, 1.6f), this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.6,
                this.worldPosition.getZ() + 0.5, 50, 0.3, 0.4, 0.3, 0.0);
        return this.spawn(level, true) != null;
    }

    /** Für Befehle: zurück auf Anfang (der König, wenn da, verschwindet; beim nächsten Besuch kniet ein neuer). */
    public void reset(ServerLevel level) {
        PhasmorEntity entity = this.king(level);
        if (entity != null) {
            entity.discard();
        }
        this.king = null;
        this.missing = 0;
        this.setPhase(level, VoidThroneBlock.Phase.GUARDED);
    }

    private void setPhase(ServerLevel level, VoidThroneBlock.Phase next) {
        this.phase = next;
        BlockState state = this.getBlockState();
        if (state.hasProperty(VoidThroneBlock.PHASE) && state.getValue(VoidThroneBlock.PHASE) != next) {
            level.setBlock(this.worldPosition, state.setValue(VoidThroneBlock.PHASE, next), 3);
        }
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Phase", this.phase.getSerializedName());
        if (this.king != null) {
            output.store("King", UUIDUtil.CODEC, this.king);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String phase = input.getStringOr("Phase", "guarded");
        for (VoidThroneBlock.Phase p : VoidThroneBlock.Phase.values()) {
            if (p.getSerializedName().equals(phase)) {
                this.phase = p;
            }
        }
        this.king = input.read("King", UUIDUtil.CODEC).orElse(null);
    }
}
