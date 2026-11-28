package io.github.sirschrolli.kingdomsatwar.block.deep;

import io.github.sirschrolli.kingdomsatwar.entity.deep.SordinEntity;
import io.github.sirschrolli.kingdomsatwar.registry.CathedralGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Wacht über das Orgelpult: Kommt jemand näher als {@link #SPAWN_RADIUS} Blöcke, steht Sordin schon an der Orgel und spielt
 * (unverwundbar, er bemerkt niemanden) – fehlt er, tritt er aus dem Sculk. Tritt jemand näher als
 * {@link SordinEntity#WAKE_RADIUS} Blöcke heran, greift ihn an, spielt auf der Orgel oder macht im Umkreis von
 * {@link SordinEntity#HEAR_RADIUS} Blöcken Lärm (siehe {@link #hear}), erwacht er. Ist er besiegt, verstummt das Pult; ein
 * Hallkristall darauf ruft ihn zurück, und er erwacht sofort. Wer sich nähert, hört von fern die Orgel.
 */
public class OrganDeskBlockEntity extends BlockEntity {
    /** So nah muss jemand kommen, damit der Kantor (spielend) an der Orgel steht. */
    public static final double SPAWN_RADIUS = 48.0;
    private static final double WARN_RADIUS = 36.0;
    /** Die Pulte, die gerade geladen sind (je Welt) – für das Gehör des Kantors. */
    private static final Map<Level, Set<BlockPos>> LOADED = new WeakHashMap<>();

    private OrganDeskBlock.Phase phase = OrganDeskBlock.Phase.GUARDED;
    private @Nullable UUID cantor;
    private int missing;
    private final Map<UUID, Long> warned = new HashMap<>();

    public OrganDeskBlockEntity(BlockPos pos, BlockState state) {
        super(CathedralGear.ORGAN_DESK_ENTITY.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.level.isClientSide()) {
            LOADED.computeIfAbsent(this.level, k -> new HashSet<>()).add(this.worldPosition.immutable());
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.forgetLoaded();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.forgetLoaded();
    }

    private void forgetLoaded() {
        if (this.level != null) {
            Set<BlockPos> desks = LOADED.get(this.level);
            if (desks != null) {
                desks.remove(this.worldPosition);
            }
        }
    }

    public OrganDeskBlock.Phase phase() {
        return this.phase;
    }

    /** Gehört dieser Kantor zu diesem Pult? */
    public boolean owns(SordinEntity entity) {
        return this.phase != OrganDeskBlock.Phase.SILENCED && entity.getUUID().equals(this.cantor);
    }

    /** Wohin das Pult blickt: dort steht der Kantor. */
    public Direction facing() {
        BlockState state = this.getBlockState();
        return state.hasProperty(OrganDeskBlock.FACING) ? state.getValue(OrganDeskBlock.FACING) : Direction.SOUTH;
    }

    /** Der Platz des Kantors: vor dem Pult, auf dem Boden. */
    public BlockPos home() {
        BlockPos p = this.worldPosition.relative(this.facing(), 1);
        if (this.level != null) {
            for (int i = 0; i < 4 && this.level.getBlockState(p.below()).getCollisionShape(this.level, p.below()).isEmpty(); i++) {
                p = p.below();
            }
        }
        return p;
    }

    public static boolean wakes(BlockPos desk, double x, double y, double z) {
        double dx = x - (desk.getX() + 0.5);
        double dz = z - (desk.getZ() + 0.5);
        double dy = y - desk.getY();
        return dx * dx + dz * dz <= SordinEntity.WAKE_RADIUS * SordinEntity.WAKE_RADIUS && dy > -6.0 && dy < 12.0;
    }

    private static boolean counts(Player player) {
        return !player.isSpectator() && !player.isCreative() && player.isAlive();
    }

    /**
     * Jemand macht Lärm (ein Schritt, ein Sprung, ein abgebauter Block …) bei {@code at}: Spielt ein Kantor in Hörweite, erwacht
     * er. Gibt zurück, ob einer erwachte.
     */
    public static boolean hear(ServerLevel level, Vec3 at, Player player) {
        if (!counts(player)) {
            return false;
        }
        Set<BlockPos> desks = LOADED.get(level);
        if (desks == null || desks.isEmpty()) {
            return false;
        }
        boolean woke = false;
        for (BlockPos pos : List.copyOf(desks)) {
            if (pos.distToCenterSqr(at) > SordinEntity.HEAR_RADIUS * SordinEntity.HEAR_RADIUS
                    || !(level.getBlockEntity(pos) instanceof OrganDeskBlockEntity desk) || desk.phase != OrganDeskBlock.Phase.GUARDED) {
                continue;
            }
            SordinEntity cantor = desk.cantor(level);
            if (cantor != null && cantor.isPlaying()) {
                desk.awaken(level);
                woke = true;
            }
        }
        return woke;
    }

    public @Nullable SordinEntity cantor(ServerLevel level) {
        if (this.cantor != null && level.getEntity(this.cantor) instanceof SordinEntity entity && entity.isAlive()) {
            return entity;
        }
        List<SordinEntity> near = level.getEntitiesOfClass(SordinEntity.class, new AABB(this.worldPosition).inflate(40.0),
                e -> e.isAlive() && this.worldPosition.equals(e.desk()));
        if (!near.isEmpty() && this.phase != OrganDeskBlock.Phase.SILENCED) {
            this.cantor = near.getFirst().getUUID();
            return near.getFirst();
        }
        return null;
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 10 != 3 || this.phase == OrganDeskBlock.Phase.SILENCED) {
            return;
        }
        Vec3 c = Vec3.atCenterOf(this.worldPosition);
        SordinEntity entity = this.cantor(level);
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
        if (entity.isPlaying()) {
            this.setPhase(level, OrganDeskBlock.Phase.GUARDED);
            for (ServerPlayer player : level.players()) {
                if (counts(player) && wakes(this.worldPosition, player.getX(), player.getY(), player.getZ())) {
                    this.awaken(level);
                    return;
                }
            }
            this.warn(level);
        } else {
            this.setPhase(level, OrganDeskBlock.Phase.AWAKE);
        }
    }

    /** Von fern hört man die Orgel (höchstens alle 45 Sekunden je Spieler eine Warnung). */
    private void warn(ServerLevel level) {
        long now = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            if (!counts(player) || player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) > WARN_RADIUS * WARN_RADIUS) {
                continue;
            }
            Long last = this.warned.get(player.getUUID());
            if (last == null || now - last > 900) {
                this.warned.put(player.getUUID(), now);
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.organ_desk.warning").withColor(SordinEntity.SCULK));
                level.playSound(null, this.worldPosition, ModSounds.SORDIN_ORGAN.get(), SoundSource.HOSTILE, 1.8f, 0.85f);
            }
        }
    }

    /** Stellt den Kantor an die Orgel: spielend ({@code awake} = false) oder sofort erwachend. Gibt ihn zurück (oder null). */
    public @Nullable SordinEntity spawn(ServerLevel level, boolean awake) {
        SordinEntity entity = ModEntities.SORDIN.get().create(level, EntitySpawnReason.TRIGGERED);
        if (entity == null) {
            return null;
        }
        BlockPos home = this.home();
        if (awake) {
            entity.rise(home, this.worldPosition);
        } else {
            entity.bindTo(home, this.worldPosition);
        }
        entity.faceOrgan();
        level.addFreshEntity(entity);
        this.cantor = entity.getUUID();
        this.missing = 0;
        this.setPhase(level, awake ? OrganDeskBlock.Phase.AWAKE : OrganDeskBlock.Phase.GUARDED);
        level.sendParticles(ParticleTypes.SCULK_SOUL, home.getX() + 0.5, home.getY() + 1.5, home.getZ() + 0.5, 60, 0.5, 1.5, 0.5, 0.05);
        level.sendParticles(new DustParticleOptions(SordinEntity.SCULK, 1.4f), home.getX() + 0.5, home.getY() + 1.5, home.getZ() + 0.5, 40, 0.5,
                1.5, 0.5, 0.0);
        if (awake) {
            level.playSound(null, this.worldPosition, ModSounds.ORGAN_DESK_KINDLE.get(), SoundSource.BLOCKS, 2.5f, 1.0f);
        }
        this.setChanged();
        return entity;
    }

    /** Lässt ihn erwachen (auch für Tests und Befehle). */
    public @Nullable SordinEntity awaken(ServerLevel level) {
        if (this.phase == OrganDeskBlock.Phase.SILENCED) {
            return null;
        }
        SordinEntity entity = this.cantor(level);
        if (entity == null) {
            return this.spawn(level, true);
        }
        entity.awaken();
        this.setPhase(level, OrganDeskBlock.Phase.AWAKE);
        return entity;
    }

    /** Der Kantor hat niemanden mehr gefunden und spielt wieder. */
    public void onCantorReturned(ServerLevel level, SordinEntity entity) {
        if (this.phase == OrganDeskBlock.Phase.SILENCED) {
            return;
        }
        this.cantor = entity.getUUID();
        this.setPhase(level, OrganDeskBlock.Phase.GUARDED);
    }

    /** Er ist besiegt: Das Pult verstummt, die Noten erlöschen. */
    public void onCantorDefeated(ServerLevel level) {
        this.cantor = null;
        this.setPhase(level, OrganDeskBlock.Phase.SILENCED);
        level.playSound(null, this.worldPosition, ModSounds.ORGAN_DESK_KINDLE.get(), SoundSource.BLOCKS, 2.0f, 0.5f);
        level.sendParticles(ParticleTypes.ASH, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.4,
                this.worldPosition.getZ() + 0.5, 30, 0.3, 0.3, 0.3, 0.02);
    }

    /** Ein Hallkristall auf dem verstummten Pult: Die Noten leuchten auf, und der Kantor kehrt zurück (sofort erwacht). */
    public boolean rekindle(ServerLevel level, @Nullable Player player) {
        if (this.phase != OrganDeskBlock.Phase.SILENCED) {
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.organ_desk.peaceful").withStyle(ChatFormatting.GRAY));
            }
            return false;
        }
        this.phase = OrganDeskBlock.Phase.GUARDED;
        level.sendParticles(new DustParticleOptions(SordinEntity.SCULK, 1.6f), this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.4,
                this.worldPosition.getZ() + 0.5, 50, 0.3, 0.4, 0.3, 0.0);
        return this.spawn(level, true) != null;
    }

    /** Für Befehle: zurück auf Anfang (der Kantor, wenn da, verschwindet; beim nächsten Besuch spielt ein neuer). */
    public void reset(ServerLevel level) {
        SordinEntity entity = this.cantor(level);
        if (entity != null) {
            entity.discard();
        }
        this.cantor = null;
        this.missing = 0;
        this.setPhase(level, OrganDeskBlock.Phase.GUARDED);
    }

    private void setPhase(ServerLevel level, OrganDeskBlock.Phase next) {
        this.phase = next;
        BlockState state = this.getBlockState();
        if (state.hasProperty(OrganDeskBlock.PHASE) && state.getValue(OrganDeskBlock.PHASE) != next) {
            level.setBlock(this.worldPosition, state.setValue(OrganDeskBlock.PHASE, next), 3);
        }
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Phase", this.phase.getSerializedName());
        if (this.cantor != null) {
            output.store("Cantor", UUIDUtil.CODEC, this.cantor);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String phase = input.getStringOr("Phase", "guarded");
        for (OrganDeskBlock.Phase p : OrganDeskBlock.Phase.values()) {
            if (p.getSerializedName().equals(phase)) {
                this.phase = p;
            }
        }
        this.cantor = input.read("Cantor", UUIDUtil.CODEC).orElse(null);
    }
}
