package io.github.sirschrolli.kingdomsatwar.block.castle;

import io.github.sirschrolli.kingdomsatwar.entity.castle.RobberKnightEntity;
import io.github.sirschrolli.kingdomsatwar.registry.CastleGear;
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
 * Wacht über das Rabenbanner: Kommt jemand näher als {@link #SPAWN_RADIUS} Blöcke, hält der Raubritter schon Hof (zu Ross,
 * unverwundbar) – fehlt er, reitet er aus dem Bergfried heran. Tritt jemand näher als {@link RobberKnightEntity#WAKE_RADIUS}
 * Blöcke an seinen Platz oder greift ihn an, erwacht er. Ist er besiegt, ist das Banner zerrissen; ein schwarzes Banner am
 * Pfahl ruft ihn zurück, und er erwacht sofort. Wer sich der Burg nähert, hört von fern ein Pferd wiehern.
 */
public class RavenBannerBlockEntity extends BlockEntity {
    /** So nah muss jemand kommen, damit der Ritter im Hof Hof hält. */
    public static final double SPAWN_RADIUS = 48.0;
    private static final double WARN_RADIUS = 34.0;

    private RavenBannerBlock.Phase phase = RavenBannerBlock.Phase.GUARDED;
    private @Nullable UUID knight;
    private int missing;
    private final Map<UUID, Long> warned = new HashMap<>();

    public RavenBannerBlockEntity(BlockPos pos, BlockState state) {
        super(CastleGear.RAVEN_BANNER_ENTITY.get(), pos, state);
    }

    public RavenBannerBlock.Phase phase() {
        return this.phase;
    }

    /** Gehört dieser Ritter zu diesem Banner? */
    public boolean owns(RobberKnightEntity entity) {
        return this.phase != RavenBannerBlock.Phase.FALLEN && entity.getUUID().equals(this.knight);
    }

    /** Wohin das Banner blickt (zum Tor). */
    public Direction facing() {
        BlockState state = this.getBlockState();
        return state.hasProperty(RavenBannerBlock.FACING) ? state.getValue(RavenBannerBlock.FACING) : Direction.SOUTH;
    }

    /** Der Platz des Ritters: zwei Schritte vor dem Banner und zwei zur Seite, auf dem Boden. */
    public BlockPos home() {
        Direction f = this.facing();
        BlockPos p = this.worldPosition.relative(f, 2).relative(f.getCounterClockWise(), 2);
        if (this.level != null) {
            for (int i = 0; i < 4 && this.level.getBlockState(p.below()).getCollisionShape(this.level, p.below()).isEmpty(); i++) {
                p = p.below();
            }
        }
        return p;
    }

    /** Liegt (x, y, z) nah genug an seinem Platz, um ihn zu wecken? */
    public boolean wakes(double x, double y, double z) {
        BlockPos home = this.home();
        double dx = x - (home.getX() + 0.5);
        double dz = z - (home.getZ() + 0.5);
        double dy = y - home.getY();
        return dx * dx + dz * dz <= RobberKnightEntity.WAKE_RADIUS * RobberKnightEntity.WAKE_RADIUS && dy > -6.0 && dy < 12.0;
    }

    private static boolean counts(Player player) {
        return !player.isSpectator() && !player.isCreative() && player.isAlive();
    }

    public @Nullable RobberKnightEntity knight(ServerLevel level) {
        if (this.knight != null && level.getEntity(this.knight) instanceof RobberKnightEntity entity && entity.isAlive()) {
            return entity;
        }
        List<RobberKnightEntity> near = level.getEntitiesOfClass(RobberKnightEntity.class, new AABB(this.worldPosition).inflate(40.0),
                e -> e.isAlive() && this.worldPosition.equals(e.banner()));
        if (!near.isEmpty() && this.phase != RavenBannerBlock.Phase.FALLEN) {
            this.knight = near.getFirst().getUUID();
            return near.getFirst();
        }
        return null;
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 10 != 5 || this.phase == RavenBannerBlock.Phase.FALLEN) {
            return;
        }
        Vec3 c = Vec3.atCenterOf(this.worldPosition);
        RobberKnightEntity entity = this.knight(level);
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
        if (entity.isHoldingCourt()) {
            this.setPhase(level, RavenBannerBlock.Phase.GUARDED);
            for (ServerPlayer player : level.players()) {
                if (counts(player) && this.wakes(player.getX(), player.getY(), player.getZ())) {
                    this.awaken(level);
                    return;
                }
            }
            this.warn(level);
        } else {
            this.setPhase(level, RavenBannerBlock.Phase.AWAKE);
        }
    }

    /** Von fern hört man ein Schlachtross wiehern (höchstens alle 45 Sekunden je Spieler eine Warnung). */
    private void warn(ServerLevel level) {
        long now = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            if (!counts(player) || player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) > WARN_RADIUS * WARN_RADIUS) {
                continue;
            }
            Long last = this.warned.get(player.getUUID());
            if (last == null || now - last > 900) {
                this.warned.put(player.getUUID(), now);
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.raven_banner.warning").withColor(RobberKnightEntity.GOLD));
                level.playSound(null, this.worldPosition, net.minecraft.sounds.SoundEvents.HORSE_ANGRY, SoundSource.HOSTILE, 2.0f, 0.7f);
            }
        }
    }

    /** Stellt den Ritter unter das Banner: Hof haltend ({@code awake} = false) oder sofort erwachend. Gibt ihn zurück (oder null). */
    public @Nullable RobberKnightEntity spawn(ServerLevel level, boolean awake) {
        RobberKnightEntity entity = ModEntities.ROBBER_KNIGHT.get().create(level, EntitySpawnReason.TRIGGERED);
        if (entity == null) {
            return null;
        }
        BlockPos home = this.home();
        float yaw = this.facing().toYRot();
        if (awake) {
            entity.rise(home, yaw, this.worldPosition);
        } else {
            entity.bindTo(home, yaw, this.worldPosition);
        }
        entity.faceHome();
        level.addFreshEntity(entity);
        this.knight = entity.getUUID();
        this.missing = 0;
        this.setPhase(level, awake ? RavenBannerBlock.Phase.AWAKE : RavenBannerBlock.Phase.GUARDED);
        level.sendParticles(ParticleTypes.CLOUD, home.getX() + 0.5, home.getY() + 1.0, home.getZ() + 0.5, 30, 0.8, 0.8, 0.8, 0.02);
        if (awake) {
            level.playSound(null, this.worldPosition, ModSounds.ROBBER_KNIGHT_HORN.get(), SoundSource.BLOCKS, 3.0f, 0.9f);
        }
        this.setChanged();
        return entity;
    }

    /** Lässt ihn erwachen (auch für Tests und Befehle). */
    public @Nullable RobberKnightEntity awaken(ServerLevel level) {
        if (this.phase == RavenBannerBlock.Phase.FALLEN) {
            return null;
        }
        RobberKnightEntity entity = this.knight(level);
        if (entity == null) {
            return this.spawn(level, true);
        }
        entity.awaken();
        this.setPhase(level, RavenBannerBlock.Phase.AWAKE);
        return entity;
    }

    /** Der Ritter hat niemanden mehr gefunden und hält wieder Hof. */
    public void onKnightReturned(ServerLevel level, RobberKnightEntity entity) {
        if (this.phase == RavenBannerBlock.Phase.FALLEN) {
            return;
        }
        this.knight = entity.getUUID();
        this.setPhase(level, RavenBannerBlock.Phase.GUARDED);
    }

    /** Er ist besiegt: Das Banner reißt vom Querholz, nur ein Fetzen bleibt. */
    public void onKnightDefeated(ServerLevel level) {
        this.knight = null;
        this.setPhase(level, RavenBannerBlock.Phase.FALLEN);
        level.playSound(null, this.worldPosition, ModSounds.RAVEN_BANNER_TEAR.get(), SoundSource.BLOCKS, 2.0f, 0.9f);
        level.sendParticles(new DustParticleOptions(RobberKnightEntity.GOLD, 1.2f), this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.4,
                this.worldPosition.getZ() + 0.5, 30, 0.3, 0.5, 0.3, 0.0);
    }

    /** Ein schwarzes Banner am leeren Pfahl: Das Rabenbanner weht wieder, und der Ritter kehrt zurück (sofort erwacht). */
    public boolean raise(ServerLevel level, @Nullable Player player) {
        if (this.phase != RavenBannerBlock.Phase.FALLEN) {
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.raven_banner.peaceful").withStyle(ChatFormatting.GRAY));
            }
            return false;
        }
        this.phase = RavenBannerBlock.Phase.GUARDED;
        level.sendParticles(new DustParticleOptions(RobberKnightEntity.GOLD, 1.4f), this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.6,
                this.worldPosition.getZ() + 0.5, 40, 0.3, 0.6, 0.3, 0.0);
        return this.spawn(level, true) != null;
    }

    /** Für Befehle: zurück auf Anfang (der Ritter, wenn da, verschwindet; beim nächsten Besuch hält ein neuer Hof). */
    public void reset(ServerLevel level) {
        RobberKnightEntity entity = this.knight(level);
        if (entity != null) {
            entity.discard();
        }
        this.knight = null;
        this.missing = 0;
        this.setPhase(level, RavenBannerBlock.Phase.GUARDED);
    }

    private void setPhase(ServerLevel level, RavenBannerBlock.Phase next) {
        this.phase = next;
        BlockState state = this.getBlockState();
        if (state.hasProperty(RavenBannerBlock.PHASE) && state.getValue(RavenBannerBlock.PHASE) != next) {
            level.setBlock(this.worldPosition, state.setValue(RavenBannerBlock.PHASE, next), 3);
        }
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Phase", this.phase.getSerializedName());
        if (this.knight != null) {
            output.store("Knight", UUIDUtil.CODEC, this.knight);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String phase = input.getStringOr("Phase", "guarded");
        for (RavenBannerBlock.Phase p : RavenBannerBlock.Phase.values()) {
            if (p.getSerializedName().equals(phase)) {
                this.phase = p;
            }
        }
        this.knight = input.read("Knight", UUIDUtil.CODEC).orElse(null);
    }
}
