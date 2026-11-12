package io.github.sirschrolli.kingdomsatwar.block.shadow;

import io.github.sirschrolli.kingdomsatwar.entity.shadow.SoulShepherdEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.SoulShepherd;
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
 * Wacht über den Seelenhirten: Kommt jemand näher als {@link #SPAWN_RADIUS} Blöcke, steht er (in Andacht) unter seiner
 * Glocke – fehlt er, steigt er aus dem Nebel. Ist er besiegt, verstummt die Glocke; ein Seelenkristall an ihr ruft ihn
 * zurück, und er erwacht sofort. Wer sich dem Pferch nähert, hört die Glocke von fern.
 */
public class SoulBellBlockEntity extends BlockEntity {
    /** So nah muss jemand kommen, damit der Hirte (in Andacht) unter seiner Glocke steht. */
    public static final double SPAWN_RADIUS = 48.0;
    private static final double WARN_RADIUS = 30.0;

    private SoulBellBlock.Phase phase = SoulBellBlock.Phase.SLEEPING;
    private @Nullable UUID shepherd;
    /** Wo der Hirte steht (relativ zur Glocke). */
    private BlockPos homeOffset = new BlockPos(0, -4, 2);
    private int missing;
    private final Map<UUID, Long> warned = new HashMap<>();

    public SoulBellBlockEntity(BlockPos pos, BlockState state) {
        super(SoulShepherd.SOUL_BELL_ENTITY.get(), pos, state);
    }

    /** Legt fest, wo der Hirte steht (für das Bauwerk und Tests). */
    public void setHome(BlockPos home) {
        this.homeOffset = home.subtract(this.worldPosition);
        this.setChanged();
    }

    public BlockPos home() {
        return this.worldPosition.offset(this.homeOffset);
    }

    public SoulBellBlock.Phase phase() {
        return this.phase;
    }

    /** Gehört dieser Hirte zu dieser Glocke? */
    public boolean owns(SoulShepherdEntity entity) {
        return entity.getUUID().equals(this.shepherd);
    }

    public @Nullable SoulShepherdEntity shepherd(ServerLevel level) {
        if (this.shepherd != null && level.getEntity(this.shepherd) instanceof SoulShepherdEntity entity && entity.isAlive()) {
            return entity;
        }
        List<SoulShepherdEntity> near = level.getEntitiesOfClass(SoulShepherdEntity.class, new AABB(this.worldPosition).inflate(44.0),
                e -> e.isAlive() && this.worldPosition.equals(e.lair()));
        if (!near.isEmpty()) {
            this.shepherd = near.getFirst().getUUID();
            return near.getFirst();
        }
        return null;
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 20 != 13 || this.phase == SoulBellBlock.Phase.DEFEATED) {
            return;
        }
        Vec3 c = Vec3.atCenterOf(this.worldPosition);
        Player near = level.getNearestPlayer(c.x, c.y, c.z, SPAWN_RADIUS, e -> e instanceof Player p && !p.isSpectator() && p.isAlive());
        SoulShepherdEntity entity = this.shepherd(level);
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
        this.setPhase(level, entity.isDormant() ? SoulBellBlock.Phase.SLEEPING : SoulBellBlock.Phase.AWAKE);
        if (entity.isDormant()) {
            this.warn(level);
        }
    }

    /** Die Glocke von fern für alle, die sich nähern (höchstens alle 45 Sekunden je Spieler). */
    private void warn(ServerLevel level) {
        long now = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            if (player.isCreative() || player.isSpectator() || player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) > WARN_RADIUS * WARN_RADIUS) {
                continue;
            }
            Long last = this.warned.get(player.getUUID());
            if (last == null || now - last > 900) {
                this.warned.put(player.getUUID(), now);
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.soul_bell.warning").withColor(0x7AE8DC));
                level.playSound(null, this.worldPosition, ModSounds.SOUL_SHEPHERD_BELL.get(), SoundSource.HOSTILE, 1.2f, 0.7f);
            }
        }
    }

    /** Setzt den Hirten unter die Glocke: in Andacht ({@code awake} = false) oder sofort erwachend. Gibt ihn zurück (oder null). */
    public @Nullable SoulShepherdEntity spawn(ServerLevel level, boolean awake) {
        SoulShepherdEntity entity = ModEntities.SOUL_SHEPHERD.get().create(level, EntitySpawnReason.TRIGGERED);
        if (entity == null) {
            return null;
        }
        BlockPos home = this.home();
        entity.snapTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0.0f, 0.0f);
        if (awake) {
            entity.rise(home, this.worldPosition);
        } else {
            entity.bindTo(home, this.worldPosition);
        }
        entity.faceLair();
        level.addFreshEntity(entity);
        this.shepherd = entity.getUUID();
        this.setPhase(level, awake ? SoulBellBlock.Phase.AWAKE : SoulBellBlock.Phase.SLEEPING);
        if (awake) {
            level.playSound(null, home, ModSounds.SOUL_SHEPHERD_AWAKEN.get(), SoundSource.HOSTILE, 3.0f, 1.0f);
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, home.getX() + 0.5, home.getY() + 1.2, home.getZ() + 0.5, 60, 1.0, 1.4, 1.0, 0.03);
            level.sendParticles(ParticleTypes.SOUL, home.getX() + 0.5, home.getY() + 1.5, home.getZ() + 0.5, 50, 1.0, 1.4, 1.0, 0.06);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.3,
                    this.worldPosition.getZ() + 0.5, 30, 0.3, 0.3, 0.3, 0.05);
        }
        this.setChanged();
        return entity;
    }

    /** Ein Seelenkristall an der verstummten Glocke: Der Hirte steigt aus dem Nebel. */
    public boolean recall(ServerLevel level, @Nullable Player player) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.soul_bell.peaceful").withStyle(ChatFormatting.GRAY));
            }
            return false;
        }
        if (this.shepherd(level) != null) {
            return false;
        }
        return this.spawn(level, true) != null;
    }

    /** Der Hirte ist besiegt: Die Glocke verstummt. */
    public void onShepherdDefeated(ServerLevel level) {
        this.shepherd = null;
        this.setPhase(level, SoulBellBlock.Phase.DEFEATED);
        this.setChanged();
    }

    /** Für Befehle: zurück auf Anfang (der Hirte, wenn da, verschwindet; beim nächsten Besuch steht ein neuer in Andacht). */
    public void reset(ServerLevel level) {
        SoulShepherdEntity entity = this.shepherd(level);
        if (entity != null) {
            entity.discard();
        }
        this.shepherd = null;
        this.missing = 0;
        this.setPhase(level, SoulBellBlock.Phase.SLEEPING);
        this.setChanged();
    }

    private void setPhase(ServerLevel level, SoulBellBlock.Phase next) {
        this.phase = next;
        BlockState state = this.getBlockState();
        if (state.hasProperty(SoulBellBlock.PHASE) && state.getValue(SoulBellBlock.PHASE) != next) {
            level.setBlock(this.worldPosition, state.setValue(SoulBellBlock.PHASE, next), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Phase", this.phase.getSerializedName());
        if (this.shepherd != null) {
            output.store("Shepherd", UUIDUtil.CODEC, this.shepherd);
        }
        output.putLong("HomeOffset", this.homeOffset.asLong());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String phase = input.getStringOr("Phase", "sleeping");
        for (SoulBellBlock.Phase p : SoulBellBlock.Phase.values()) {
            if (p.getSerializedName().equals(phase)) {
                this.phase = p;
            }
        }
        this.shepherd = input.read("Shepherd", UUIDUtil.CODEC).orElse(null);
        input.getLong("HomeOffset").ifPresent(v -> this.homeOffset = BlockPos.of(v));
    }
}
