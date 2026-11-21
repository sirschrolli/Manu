package io.github.sirschrolli.kingdomsatwar.block.nether;

import io.github.sirschrolli.kingdomsatwar.entity.nether.IgnaraEntity;
import io.github.sirschrolli.kingdomsatwar.registry.FlameSanctuary;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
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
 * Wacht über den Flammenaltar: Kommt jemand näher als {@link #SPAWN_RADIUS} Blöcke, schwebt Ignara schon vor dem Altar und
 * betet (unverwundbar, sie bemerkt niemanden) – fehlt sie, steigt sie aus dem Feuer. Tritt jemand näher als
 * {@link IgnaraEntity#WAKE_RADIUS} Blöcke heran (oder greift sie an), erwacht sie. Ist sie besiegt, erlischt das Feuer; ein
 * Feueropal entzündet es neu, und die Priesterin erwacht sofort. Wer sich dem Heiligtum nähert, hört ihren Gesang.
 */
public class FlameAltarBlockEntity extends BlockEntity {
    /** So nah muss jemand kommen, damit die Priesterin (betend) vor dem Altar schwebt. */
    public static final double SPAWN_RADIUS = 48.0;
    private static final double WARN_RADIUS = 34.0;

    private FlameAltarBlock.Phase phase = FlameAltarBlock.Phase.PRAYING;
    private @Nullable UUID priestess;
    private int missing;
    private final Map<UUID, Long> warned = new HashMap<>();

    public FlameAltarBlockEntity(BlockPos pos, BlockState state) {
        super(FlameSanctuary.FLAME_ALTAR_ENTITY.get(), pos, state);
    }

    public FlameAltarBlock.Phase phase() {
        return this.phase;
    }

    /** Gehört diese Priesterin zu diesem Altar? */
    public boolean owns(IgnaraEntity entity) {
        return this.phase != FlameAltarBlock.Phase.DEFEATED && entity.getUUID().equals(this.priestess);
    }

    /** Wohin der Altar blickt: dort schwebt die Priesterin. */
    public Direction facing() {
        BlockState state = this.getBlockState();
        return state.hasProperty(FlameAltarBlock.FACING) ? state.getValue(FlameAltarBlock.FACING) : Direction.SOUTH;
    }

    /** Der Platz der Priesterin vor dem Altar (zwei Blöcke davor, auf Höhe des Altars). */
    public BlockPos home() {
        return this.worldPosition.relative(this.facing(), 2);
    }

    public static boolean wakes(BlockPos altar, double x, double y, double z) {
        double dx = x - (altar.getX() + 0.5);
        double dz = z - (altar.getZ() + 0.5);
        double dy = y - altar.getY();
        return dx * dx + dz * dz <= IgnaraEntity.WAKE_RADIUS * IgnaraEntity.WAKE_RADIUS && dy > -4.0 && dy < 10.0;
    }

    private static boolean counts(Player player) {
        return !player.isSpectator() && !player.isCreative() && player.isAlive();
    }

    public @Nullable IgnaraEntity priestess(ServerLevel level) {
        if (this.priestess != null && level.getEntity(this.priestess) instanceof IgnaraEntity entity && entity.isAlive()) {
            return entity;
        }
        List<IgnaraEntity> near = level.getEntitiesOfClass(IgnaraEntity.class, new AABB(this.worldPosition).inflate(40.0),
                e -> e.isAlive() && this.worldPosition.equals(e.altar()));
        if (!near.isEmpty() && this.phase != FlameAltarBlock.Phase.DEFEATED) {
            this.priestess = near.getFirst().getUUID();
            return near.getFirst();
        }
        return null;
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 10 != 3 || this.phase == FlameAltarBlock.Phase.DEFEATED) {
            return;
        }
        Vec3 c = Vec3.atCenterOf(this.worldPosition);
        IgnaraEntity entity = this.priestess(level);
        if (entity == null) {
            Player near = level.getNearestPlayer(c.x, c.y, c.z, SPAWN_RADIUS, e -> e instanceof Player p && !p.isSpectator() && p.isAlive());
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
        if (entity.isPraying()) {
            this.setPhase(level, FlameAltarBlock.Phase.PRAYING);
            for (ServerPlayer player : level.players()) {
                if (counts(player) && wakes(this.worldPosition, player.getX(), player.getY(), player.getZ())) {
                    this.awaken(level);
                    return;
                }
            }
            this.warn(level);
        } else {
            this.setPhase(level, FlameAltarBlock.Phase.AWAKE);
        }
    }

    /** Ihr Gesang hallt von fern für alle, die sich nähern (höchstens alle 45 Sekunden je Spieler). */
    private void warn(ServerLevel level) {
        long now = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            if (!counts(player) || player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) > WARN_RADIUS * WARN_RADIUS) {
                continue;
            }
            Long last = this.warned.get(player.getUUID());
            if (last == null || now - last > 900) {
                this.warned.put(player.getUUID(), now);
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.flame_altar.warning").withColor(0xFF9A3A));
                level.playSound(null, this.worldPosition, ModSounds.IGNARA_PRAY.get(), SoundSource.HOSTILE, 1.6f, 1.0f);
            }
        }
    }

    /** Stellt die Priesterin vor den Altar: betend ({@code awake} = false) oder sofort erwachend. Gibt sie zurück (oder null). */
    public @Nullable IgnaraEntity spawn(ServerLevel level, boolean awake) {
        IgnaraEntity entity = ModEntities.IGNARA.get().create(level, EntitySpawnReason.TRIGGERED);
        if (entity == null) {
            return null;
        }
        BlockPos home = this.home();
        float yaw = this.facing().getOpposite().toYRot();
        entity.snapTo(home.getX() + 0.5, home.getY() + IgnaraEntity.HOVER, home.getZ() + 0.5, yaw, 0.0f);
        entity.yBodyRot = yaw;
        entity.yHeadRot = yaw;
        if (awake) {
            entity.rise(home, this.worldPosition);
        } else {
            entity.bindTo(home, this.worldPosition);
        }
        level.addFreshEntity(entity);
        this.priestess = entity.getUUID();
        this.missing = 0;
        this.setPhase(level, awake ? FlameAltarBlock.Phase.AWAKE : FlameAltarBlock.Phase.PRAYING);
        level.sendParticles(ParticleTypes.FLAME, home.getX() + 0.5, home.getY() + 1.2, home.getZ() + 0.5, 60, 0.5, 1.2, 0.5, 0.05);
        if (awake) {
            level.playSound(null, this.worldPosition, ModSounds.FLAME_ALTAR_IGNITE.get(), SoundSource.BLOCKS, 2.5f, 1.0f);
        }
        this.setChanged();
        return entity;
    }

    /** Lässt sie erwachen (auch für Tests und Befehle). */
    public @Nullable IgnaraEntity awaken(ServerLevel level) {
        if (this.phase == FlameAltarBlock.Phase.DEFEATED) {
            return null;
        }
        IgnaraEntity entity = this.priestess(level);
        if (entity == null) {
            return this.spawn(level, true);
        }
        entity.awaken();
        this.setPhase(level, FlameAltarBlock.Phase.AWAKE);
        return entity;
    }

    /** Die Priesterin hat niemanden mehr gefunden und betet wieder. */
    public void onPriestessReturned(ServerLevel level, IgnaraEntity entity) {
        if (this.phase == FlameAltarBlock.Phase.DEFEATED) {
            return;
        }
        this.priestess = entity.getUUID();
        this.setPhase(level, FlameAltarBlock.Phase.PRAYING);
    }

    /** Sie ist besiegt: Das ewige Feuer erlischt. */
    public void onPriestessDefeated(ServerLevel level) {
        this.priestess = null;
        this.setPhase(level, FlameAltarBlock.Phase.DEFEATED);
        level.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 2.0f, 0.6f);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.1,
                this.worldPosition.getZ() + 0.5, 30, 0.3, 0.3, 0.3, 0.03);
    }

    /** Ein Feueropal in der kalten Schale: Das Feuer lodert auf, und die Priesterin kehrt zurück (sofort erwacht). */
    public boolean rekindle(ServerLevel level, @Nullable Player player) {
        if (this.phase != FlameAltarBlock.Phase.DEFEATED) {
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.flame_altar.peaceful").withStyle(ChatFormatting.GRAY));
            }
            return false;
        }
        this.phase = FlameAltarBlock.Phase.PRAYING;
        level.sendParticles(ParticleTypes.FLAME, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.1,
                this.worldPosition.getZ() + 0.5, 50, 0.3, 0.4, 0.3, 0.06);
        return this.spawn(level, true) != null;
    }

    /** Für Befehle: zurück auf Anfang (die Priesterin, wenn da, verschwindet; beim nächsten Besuch betet eine neue). */
    public void reset(ServerLevel level) {
        IgnaraEntity entity = this.priestess(level);
        if (entity != null) {
            entity.discard();
        }
        this.priestess = null;
        this.missing = 0;
        this.setPhase(level, FlameAltarBlock.Phase.PRAYING);
    }

    private void setPhase(ServerLevel level, FlameAltarBlock.Phase next) {
        this.phase = next;
        BlockState state = this.getBlockState();
        if (state.hasProperty(FlameAltarBlock.PHASE) && state.getValue(FlameAltarBlock.PHASE) != next) {
            level.setBlock(this.worldPosition, state.setValue(FlameAltarBlock.PHASE, next), 3);
        }
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Phase", this.phase.getSerializedName());
        if (this.priestess != null) {
            output.store("Priestess", UUIDUtil.CODEC, this.priestess);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String phase = input.getStringOr("Phase", "praying");
        for (FlameAltarBlock.Phase p : FlameAltarBlock.Phase.values()) {
            if (p.getSerializedName().equals(phase)) {
                this.phase = p;
            }
        }
        this.priestess = input.read("Priestess", UUIDUtil.CODEC).orElse(null);
    }
}
