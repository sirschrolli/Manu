package io.github.sirschrolli.kingdomsatwar.block.serpent;

import io.github.sirschrolli.kingdomsatwar.entity.serpent.QuetzaraEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.SerpentGear;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.DustParticleOptions;
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
 * Wacht über den Schlangenaltar: Kommt jemand näher als {@link #SPAWN_RADIUS} Blöcke, liegt Quetzara schon eingerollt um den
 * Altar und schläft. Steigt jemand in die Grube hinab (näher als {@link #WAKE_RADIUS} Blöcke, ungefähr auf ihrer Höhe) oder
 * greift sie an, erwacht sie. Ist sie besiegt, ist das Schlangenbild zerbrochen; ein Goldblock auf dem Altar ruft sie zurück,
 * und sie erwacht sofort. Wer sich der Grube nähert, hört aus der Tiefe ein Zischen.
 */
public class SerpentAltarBlockEntity extends BlockEntity {
    /** So nah muss jemand kommen, damit sie um den Altar schläft. */
    public static final double SPAWN_RADIUS = 40.0;
    /** So nah (waagerecht) weckt sie, wer unten in der Grube steht … */
    public static final double WAKE_RADIUS = 12.5;
    /** … und so nah jeder, gleich auf welcher Höhe. */
    public static final double CLOSE_RADIUS = 5.0;
    private static final double WARN_RADIUS = 24.0;

    private SerpentAltarBlock.Phase phase = SerpentAltarBlock.Phase.SLEEPING;
    private @Nullable UUID serpent;
    private int missing;
    private final Map<UUID, Long> warned = new HashMap<>();

    public SerpentAltarBlockEntity(BlockPos pos, BlockState state) {
        super(SerpentGear.SERPENT_ALTAR_ENTITY.get(), pos, state);
    }

    public SerpentAltarBlock.Phase phase() {
        return this.phase;
    }

    /** Wohin das Schlangenbild blickt (zum Eingang der Grube). */
    public Direction facing() {
        BlockState state = this.getBlockState();
        return state.hasProperty(SerpentAltarBlock.FACING) ? state.getValue(SerpentAltarBlock.FACING) : Direction.NORTH;
    }

    /** Gehört diese Schlange zu diesem Altar? */
    public boolean owns(QuetzaraEntity entity) {
        return this.phase != SerpentAltarBlock.Phase.FALLEN && entity.getUUID().equals(this.serpent);
    }

    private static boolean counts(Player player) {
        return !player.isSpectator() && !player.isCreative() && player.isAlive();
    }

    /** Weckt (x, y, z) sie auf? Unten in der Grube nahe genug – oder ganz nah, gleich auf welcher Höhe. */
    public boolean wakes(double x, double y, double z) {
        double dx = x - (this.worldPosition.getX() + 0.5);
        double dz = z - (this.worldPosition.getZ() + 0.5);
        double dy = y - this.worldPosition.getY();
        double flat = dx * dx + dz * dz;
        return flat <= WAKE_RADIUS * WAKE_RADIUS && dy > -3.5 && dy < 4.5 || flat + dy * dy <= CLOSE_RADIUS * CLOSE_RADIUS;
    }

    public @Nullable QuetzaraEntity serpent(ServerLevel level) {
        if (this.serpent != null && level.getEntity(this.serpent) instanceof QuetzaraEntity entity && entity.isAlive()) {
            return entity;
        }
        List<QuetzaraEntity> near = level.getEntitiesOfClass(QuetzaraEntity.class, new AABB(this.worldPosition).inflate(48.0),
                e -> e.isAlive() && this.worldPosition.equals(e.altar()));
        if (!near.isEmpty() && this.phase != SerpentAltarBlock.Phase.FALLEN) {
            this.serpent = near.getFirst().getUUID();
            return near.getFirst();
        }
        return null;
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 10 != 7 || this.phase == SerpentAltarBlock.Phase.FALLEN) {
            return;
        }
        Vec3 c = Vec3.atCenterOf(this.worldPosition);
        QuetzaraEntity entity = this.serpent(level);
        if (entity == null) {
            Player near = level.getNearestPlayer(c.x, c.y, c.z, SPAWN_RADIUS, e -> e instanceof Player p && !p.isSpectator() && p.isAlive());
            if (near == null || level.getDifficulty() == Difficulty.PEACEFUL || !level.isPositionEntityTicking(this.worldPosition)) {
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
        if (entity.isAsleep()) {
            this.setPhase(level, SerpentAltarBlock.Phase.SLEEPING);
            for (ServerPlayer player : level.players()) {
                if (counts(player) && this.wakes(player.getX(), player.getY(), player.getZ())) {
                    this.awaken(level);
                    return;
                }
            }
            this.warn(level);
        } else {
            this.setPhase(level, SerpentAltarBlock.Phase.AWAKE);
        }
    }

    /** Aus der Tiefe zischt es (höchstens alle 45 Sekunden je Spieler eine Warnung). */
    private void warn(ServerLevel level) {
        long now = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            if (!counts(player) || player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) > WARN_RADIUS * WARN_RADIUS) {
                continue;
            }
            Long last = this.warned.get(player.getUUID());
            if (last == null || now - last > 900) {
                this.warned.put(player.getUUID(), now);
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.serpent_altar.warning").withColor(QuetzaraEntity.JADE));
                level.playSound(null, this.worldPosition, ModSounds.QUETZARA_HISS.get(), SoundSource.HOSTILE, 2.5f, 0.8f);
            }
        }
    }

    /** Legt Quetzara um den Altar: schlafend ({@code awake} = false) oder sofort erwachend. Gibt sie zurück (oder null). */
    public @Nullable QuetzaraEntity spawn(ServerLevel level, boolean awake) {
        QuetzaraEntity entity = ModEntities.QUETZARA.get().create(level, EntitySpawnReason.TRIGGERED);
        if (entity == null) {
            return null;
        }
        entity.coilAround(this.worldPosition, this.facing());
        level.addFreshEntity(entity);
        if (awake) {
            entity.awaken();
        }
        this.serpent = entity.getUUID();
        this.missing = 0;
        this.setPhase(level, awake ? SerpentAltarBlock.Phase.AWAKE : SerpentAltarBlock.Phase.SLEEPING);
        this.setChanged();
        return entity;
    }

    /** Lässt sie erwachen (auch für Tests und Befehle). */
    public @Nullable QuetzaraEntity awaken(ServerLevel level) {
        if (this.phase == SerpentAltarBlock.Phase.FALLEN) {
            return null;
        }
        QuetzaraEntity entity = this.serpent(level);
        if (entity == null) {
            return this.spawn(level, true);
        }
        entity.awaken();
        this.setPhase(level, SerpentAltarBlock.Phase.AWAKE);
        return entity;
    }

    /** Sie hat niemanden mehr gefunden und schläft wieder um den Altar. */
    public void onSerpentSlept(ServerLevel level, QuetzaraEntity entity) {
        if (this.phase == SerpentAltarBlock.Phase.FALLEN) {
            return;
        }
        this.serpent = entity.getUUID();
        this.setPhase(level, SerpentAltarBlock.Phase.SLEEPING);
    }

    /** Sie ist besiegt: Das goldene Schlangenbild zerspringt. */
    public void onSerpentDefeated(ServerLevel level) {
        this.serpent = null;
        this.setPhase(level, SerpentAltarBlock.Phase.FALLEN);
        level.playSound(null, this.worldPosition, ModSounds.SERPENT_ALTAR_SHATTER.get(), SoundSource.BLOCKS, 2.0f, 1.0f);
        level.sendParticles(new DustParticleOptions(0xE8B830, 1.3f), this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.1,
                this.worldPosition.getZ() + 0.5, 40, 0.3, 0.4, 0.3, 0.0);
    }

    /** Ein Goldblock auf dem leeren Altar: Das Schlangenbild ist wieder ganz, und Quetzara kehrt zurück (sofort erwacht). */
    public boolean offer(ServerLevel level, @Nullable Player player) {
        if (this.phase != SerpentAltarBlock.Phase.FALLEN) {
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.serpent_altar.peaceful").withStyle(ChatFormatting.GRAY));
            }
            return false;
        }
        this.phase = SerpentAltarBlock.Phase.SLEEPING;
        level.playSound(null, this.worldPosition, ModSounds.SERPENT_ALTAR_OFFER.get(), SoundSource.BLOCKS, 2.0f, 1.0f);
        level.sendParticles(new DustParticleOptions(0xE8B830, 1.4f), this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.4,
                this.worldPosition.getZ() + 0.5, 40, 0.3, 0.5, 0.3, 0.0);
        return this.spawn(level, true) != null;
    }

    /** Für Befehle: zurück auf Anfang (die Schlange, wenn da, verschwindet; beim nächsten Besuch schläft eine neue um den Altar). */
    public void reset(ServerLevel level) {
        QuetzaraEntity entity = this.serpent(level);
        if (entity != null) {
            entity.discard();
        }
        this.serpent = null;
        this.missing = 0;
        this.setPhase(level, SerpentAltarBlock.Phase.SLEEPING);
    }

    private void setPhase(ServerLevel level, SerpentAltarBlock.Phase next) {
        this.phase = next;
        BlockState state = this.getBlockState();
        if (state.hasProperty(SerpentAltarBlock.PHASE) && state.getValue(SerpentAltarBlock.PHASE) != next) {
            level.setBlock(this.worldPosition, state.setValue(SerpentAltarBlock.PHASE, next), 3);
        }
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Phase", this.phase.getSerializedName());
        if (this.serpent != null) {
            output.store("Serpent", UUIDUtil.CODEC, this.serpent);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String phase = input.getStringOr("Phase", "sleeping");
        for (SerpentAltarBlock.Phase p : SerpentAltarBlock.Phase.values()) {
            if (p.getSerializedName().equals(phase)) {
                this.phase = p;
            }
        }
        this.serpent = input.read("Serpent", UUIDUtil.CODEC).orElse(null);
    }
}
