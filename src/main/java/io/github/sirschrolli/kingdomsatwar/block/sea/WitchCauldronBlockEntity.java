package io.github.sirschrolli.kingdomsatwar.block.sea;

import io.github.sirschrolli.kingdomsatwar.entity.sea.SeaWitchEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.SeaWitch;
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
 * Wacht über die Meerhexe: Kommt jemand näher als {@link #SPAWN_RADIUS} Blöcke, schwebt sie (in Trance) über dem Kessel –
 * fehlt sie, steigt sie aus dem Dampf. Ist sie besiegt, erkaltet der Kessel; eine Perle darin ruft sie zurück, und sie
 * erwacht sofort. Wer sich der Grotte nähert, hört ihr Flüstern.
 */
public class WitchCauldronBlockEntity extends BlockEntity {
    /** So nah muss jemand kommen, damit die Hexe (in Trance) über ihrem Kessel schwebt. */
    public static final double SPAWN_RADIUS = 48.0;
    private static final double WARN_RADIUS = 26.0;

    private WitchCauldronBlock.Phase phase = WitchCauldronBlock.Phase.SLEEPING;
    private @Nullable UUID witch;
    /** Wo die Hexe schwebt (relativ zum Kessel). */
    private BlockPos homeOffset = new BlockPos(0, 3, 0);
    private int missing;
    private final Map<UUID, Long> warned = new HashMap<>();

    public WitchCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(SeaWitch.WITCH_CAULDRON_ENTITY.get(), pos, state);
    }

    /** Legt fest, wo die Hexe schwebt (für das Bauwerk und Tests). */
    public void setHome(BlockPos home) {
        this.homeOffset = home.subtract(this.worldPosition);
        this.setChanged();
    }

    public BlockPos home() {
        return this.worldPosition.offset(this.homeOffset);
    }

    public WitchCauldronBlock.Phase phase() {
        return this.phase;
    }

    /** Gehört diese Hexe zu diesem Kessel? */
    public boolean owns(SeaWitchEntity entity) {
        return entity.getUUID().equals(this.witch);
    }

    public @Nullable SeaWitchEntity witch(ServerLevel level) {
        if (this.witch != null && level.getEntity(this.witch) instanceof SeaWitchEntity entity && entity.isAlive()) {
            return entity;
        }
        List<SeaWitchEntity> near = level.getEntitiesOfClass(SeaWitchEntity.class, new AABB(this.worldPosition).inflate(40.0),
                e -> e.isAlive() && this.worldPosition.equals(e.lair()));
        if (!near.isEmpty()) {
            this.witch = near.getFirst().getUUID();
            return near.getFirst();
        }
        return null;
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 20 != 11 || this.phase == WitchCauldronBlock.Phase.DEFEATED) {
            return;
        }
        Vec3 c = Vec3.atCenterOf(this.worldPosition);
        Player near = level.getNearestPlayer(c.x, c.y, c.z, SPAWN_RADIUS, e -> e instanceof Player p && !p.isSpectator() && p.isAlive());
        SeaWitchEntity entity = this.witch(level);
        if (entity == null) {
            if (near == null || level.getDifficulty() == Difficulty.PEACEFUL || !level.isPositionEntityTicking(this.home())) {
                this.missing = 0;
                return;
            }
            // erst nach drei Prüfungen: ihr Chunk könnte gerade erst laden
            if (++this.missing >= 3) {
                this.missing = 0;
                this.spawn(level, false);
            }
            return;
        }
        this.missing = 0;
        this.setPhase(level, entity.isDormant() ? WitchCauldronBlock.Phase.SLEEPING : WitchCauldronBlock.Phase.AWAKE);
        if (entity.isDormant()) {
            this.warn(level);
        }
    }

    /** Ein Flüstern aus der Grotte für alle, die sich nähern (höchstens alle 45 Sekunden je Spieler). */
    private void warn(ServerLevel level) {
        long now = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            if (player.isCreative() || player.isSpectator() || player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) > WARN_RADIUS * WARN_RADIUS) {
                continue;
            }
            Long last = this.warned.get(player.getUUID());
            if (last == null || now - last > 900) {
                this.warned.put(player.getUUID(), now);
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.witch_cauldron.warning").withColor(0x7AF0C8));
            }
        }
    }

    /** Setzt die Hexe über den Kessel: in Trance ({@code awake} = false) oder sofort erwachend. Gibt sie zurück (oder null). */
    public @Nullable SeaWitchEntity spawn(ServerLevel level, boolean awake) {
        SeaWitchEntity entity = ModEntities.SEA_WITCH.get().create(level, EntitySpawnReason.TRIGGERED);
        if (entity == null) {
            return null;
        }
        BlockPos home = this.home();
        entity.snapTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, level.getRandom().nextFloat() * 360.0f, 0.0f);
        if (awake) {
            entity.rise(home, this.worldPosition);
        } else {
            entity.bindTo(home, this.worldPosition);
        }
        level.addFreshEntity(entity);
        this.witch = entity.getUUID();
        this.setPhase(level, awake ? WitchCauldronBlock.Phase.AWAKE : WitchCauldronBlock.Phase.SLEEPING);
        if (awake) {
            level.playSound(null, home, ModSounds.SEA_WITCH_AWAKEN.get(), SoundSource.HOSTILE, 3.0f, 1.0f);
            level.sendParticles(ParticleTypes.SQUID_INK, home.getX() + 0.5, home.getY() + 1.0, home.getZ() + 0.5, 120, 1.2, 1.4, 1.2, 0.1);
            level.sendParticles(ParticleTypes.GLOW_SQUID_INK, home.getX() + 0.5, home.getY() + 1.0, home.getZ() + 0.5, 40, 1.0, 1.2, 1.0, 0.06);
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0,
                    this.worldPosition.getZ() + 0.5, 60, 0.4, 0.4, 0.4, 0.2);
        }
        this.setChanged();
        return entity;
    }

    /** Eine Perle in den erkalteten Kessel: Die Hexe steigt aus dem Dampf. */
    public boolean recall(ServerLevel level, @Nullable Player player) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.witch_cauldron.peaceful").withStyle(ChatFormatting.GRAY));
            }
            return false;
        }
        if (this.witch(level) != null) {
            return false;
        }
        return this.spawn(level, true) != null;
    }

    /** Die Hexe ist besiegt: Der Kessel erkaltet. */
    public void onWitchDefeated(ServerLevel level) {
        this.witch = null;
        this.setPhase(level, WitchCauldronBlock.Phase.DEFEATED);
        this.setChanged();
    }

    /** Für Befehle: zurück auf Anfang (die Hexe, wenn da, verschwindet; beim nächsten Besuch schwebt eine neue über dem Kessel). */
    public void reset(ServerLevel level) {
        SeaWitchEntity entity = this.witch(level);
        if (entity != null) {
            entity.discard();
        }
        this.witch = null;
        this.missing = 0;
        this.setPhase(level, WitchCauldronBlock.Phase.SLEEPING);
        this.setChanged();
    }

    private void setPhase(ServerLevel level, WitchCauldronBlock.Phase next) {
        this.phase = next;
        BlockState state = this.getBlockState();
        if (state.hasProperty(WitchCauldronBlock.PHASE) && state.getValue(WitchCauldronBlock.PHASE) != next) {
            level.setBlock(this.worldPosition, state.setValue(WitchCauldronBlock.PHASE, next), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Phase", this.phase.getSerializedName());
        if (this.witch != null) {
            output.store("Witch", UUIDUtil.CODEC, this.witch);
        }
        output.putLong("HomeOffset", this.homeOffset.asLong());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String phase = input.getStringOr("Phase", "sleeping");
        for (WitchCauldronBlock.Phase p : WitchCauldronBlock.Phase.values()) {
            if (p.getSerializedName().equals(phase)) {
                this.phase = p;
            }
        }
        this.witch = input.read("Witch", UUIDUtil.CODEC).orElse(null);
        input.getLong("HomeOffset").ifPresent(v -> this.homeOffset = BlockPos.of(v));
    }
}
