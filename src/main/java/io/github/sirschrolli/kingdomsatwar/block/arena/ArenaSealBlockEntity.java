package io.github.sirschrolli.kingdomsatwar.block.arena;

import io.github.sirschrolli.kingdomsatwar.entity.arena.VentorEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ArenaGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Das Arenasiegel zählt die Käfige: Es sucht einmal die Prüfungsspawner rings um das Podest (nicht den Spawner der Böen über
 * sich) und sieht alle Viertelsekunde nach, ob einer bezwungen ist (er wirft gerade seine Belohnung aus – vor dem ersten
 * Kampf zählt auch einer, der schon abkühlt). Jeder bezwungene Käfig schickt einen Windstoß zum Podest und entzündet sein
 * Windsiegel; sind alle bezwungen, ist der Meister bereit und steigt aus dem Spawner der Böen, sobald jemand die Arena
 * betritt. Findet er niemanden mehr, sinkt er zurück (und steigt beim nächsten Besuch wieder auf). Ist er besiegt, muss man
 * die Käfige erneut bezwingen (sie kühlen eine halbe Stunde ab), um ihn noch einmal zu fordern.
 */
public class ArenaSealBlockEntity extends BlockEntity {
    /** So weit (waagerecht) sucht das Siegel nach Käfigen. */
    public static final int SCAN_RADIUS = 18;
    /** So nah an der Mitte (waagerecht) muss jemand stehen, damit der Meister aufsteigt (die Arena samt Brüstung). */
    public static final double ARENA_RADIUS = 13.5;
    /** Höchstens so viele Käfige zählt es (mehr Siegel schweben nicht über dem Podest). */
    public static final int MAX_CAGES = 8;

    private final List<BlockPos> cages = new ArrayList<>();
    private int conquered;
    private boolean scanned;
    /** Ist er schon einmal besiegt worden? Dann zählt nur ein Käfig, der gerade (wieder) bezwungen wurde. */
    private boolean fought;
    private @Nullable UUID ventor;
    private int missing;
    private final Map<UUID, Long> told = new HashMap<>();

    public ArenaSealBlockEntity(BlockPos pos, BlockState state) {
        super(ArenaGear.ARENA_SEAL_ENTITY.get(), pos, state);
    }

    public ArenaSealBlock.Phase phase() {
        BlockState state = this.getBlockState();
        return state.hasProperty(ArenaSealBlock.PHASE) ? state.getValue(ArenaSealBlock.PHASE) : ArenaSealBlock.Phase.DORMANT;
    }

    /** Die Käfige (ihre Prüfungsspawner), im Kreis um das Podest sortiert. */
    public List<BlockPos> cages() {
        return List.copyOf(this.cages);
    }

    /** Welche Käfige bezwungen sind (Bit i: Käfig i). */
    public int conqueredMask() {
        return this.conquered;
    }

    public int conqueredCount() {
        return Integer.bitCount(this.conquered);
    }

    public boolean allConquered() {
        return !this.cages.isEmpty() && this.conqueredCount() >= this.cages.size();
    }

    /** Der Spawner der Böen über dem Siegel – auf ihm steigt der Meister auf. */
    public BlockPos spawner() {
        return this.worldPosition.above();
    }

    /** Die Mitte der Arena (auf Höhe des Sandes, auf dem man steht). */
    public Vec3 center() {
        return new Vec3(this.worldPosition.getX() + 0.5, this.worldPosition.getY() - 1.0, this.worldPosition.getZ() + 0.5);
    }

    /** Steht (x, y, z) in der Arena (im Sand oder auf der Brüstung)? */
    public boolean inArena(double x, double y, double z) {
        Vec3 c = this.center();
        double dx = x - c.x;
        double dz = z - c.z;
        return dx * dx + dz * dz <= ARENA_RADIUS * ARENA_RADIUS && y > c.y - 3.0 && y < c.y + 8.0;
    }

    private static boolean counts(Player player) {
        return !player.isSpectator() && !player.isCreative() && player.isAlive();
    }

    public @Nullable VentorEntity ventor(ServerLevel level) {
        if (this.ventor != null && level.getEntity(this.ventor) instanceof VentorEntity entity && entity.isAlive()) {
            return entity;
        }
        List<VentorEntity> near = level.getEntitiesOfClass(VentorEntity.class, new AABB(this.worldPosition).inflate(40.0),
                e -> e.isAlive() && this.worldPosition.equals(e.seal()));
        if (!near.isEmpty()) {
            this.ventor = near.getFirst().getUUID();
            return near.getFirst();
        }
        return null;
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 5 != 2) {
            return;
        }
        if (!this.scanned) {
            this.scan(level);
        }
        switch (this.phase()) {
            case DORMANT, CONQUERED -> {
                this.checkCages(level);
                if (this.allConquered()) {
                    this.ready(level);
                }
            }
            case READY -> {
                if (level.getDifficulty() == Difficulty.PEACEFUL) {
                    return;
                }
                for (ServerPlayer player : level.players()) {
                    if (counts(player) && this.inArena(player.getX(), player.getY(), player.getZ())) {
                        this.summon(level);
                        return;
                    }
                }
            }
            case AWAKE -> {
                if (this.ventor(level) != null) {
                    this.missing = 0;
                } else if (++this.missing >= 8) {
                    // er ist fort, ohne besiegt zu sein (etwa mit seinem Chunk entladen): beim nächsten Besuch steigt er wieder auf
                    this.missing = 0;
                    this.setPhase(level, ArenaSealBlock.Phase.READY);
                }
            }
        }
    }

    /** Sucht die Prüfungsspawner rings um das Podest (einmal; nicht den Spawner der Böen über dem Siegel). */
    public void scan(ServerLevel level) {
        this.cages.clear();
        BlockPos above = this.spawner();
        for (BlockPos p : BlockPos.betweenClosed(this.worldPosition.offset(-SCAN_RADIUS, -3, -SCAN_RADIUS),
                this.worldPosition.offset(SCAN_RADIUS, 1, SCAN_RADIUS))) {
            if (!p.equals(above) && level.getBlockState(p).is(Blocks.TRIAL_SPAWNER)) {
                this.cages.add(p.immutable());
            }
        }
        double cx = this.worldPosition.getX();
        double cz = this.worldPosition.getZ();
        this.cages.sort(Comparator.comparingDouble(p -> Math.atan2(p.getZ() - cz, p.getX() - cx)));
        while (this.cages.size() > MAX_CAGES) {
            this.cages.removeLast();
        }
        this.conquered &= (1 << this.cages.size()) - 1;
        this.scanned = true;
        this.sync(level);
    }

    /** Für Tests: diese Käfige zählen (statt zu suchen). */
    public void setCages(ServerLevel level, List<BlockPos> cages) {
        this.cages.clear();
        cages.forEach(p -> this.cages.add(p.immutable()));
        this.conquered = 0;
        this.scanned = true;
        this.sync(level);
    }

    /** Ist ein Käfig bezwungen? Dann zündet sein Windsiegel, und ein Windstoß fährt von ihm zum Podest. */
    private void checkCages(ServerLevel level) {
        for (int i = 0; i < this.cages.size(); i++) {
            if ((this.conquered & (1 << i)) != 0) {
                continue;
            }
            BlockPos cage = this.cages.get(i);
            if (!level.isLoaded(cage)) {
                continue;
            }
            BlockState state = level.getBlockState(cage);
            if (!state.is(Blocks.TRIAL_SPAWNER)) {
                continue;
            }
            TrialSpawnerState s = state.getValue(TrialSpawnerBlock.STATE);
            boolean won = s == TrialSpawnerState.WAITING_FOR_REWARD_EJECTION || s == TrialSpawnerState.EJECTING_REWARD
                    || !this.fought && s == TrialSpawnerState.COOLDOWN;
            if (won) {
                this.conquer(level, i);
            }
        }
    }

    private void conquer(ServerLevel level, int i) {
        this.conquered |= 1 << i;
        BlockPos cage = this.cages.get(i);
        Vec3 from = Vec3.atCenterOf(cage);
        Vec3 to = Vec3.atCenterOf(this.spawner()).add(0.0, 0.3, 0.0);
        double len = from.distanceTo(to);
        for (double s = 0.0; s < len; s += 0.7) {
            Vec3 p = from.add(to.subtract(from).scale(s / len));
            level.sendParticles(ParticleTypes.SMALL_GUST, p.x, p.y + Math.sin(s * 0.8) * 0.3, p.z, 1, 0.05, 0.05, 0.05, 0.0);
        }
        level.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, to.x, to.y, to.z, 1, 0.0, 0.0, 0.0, 0.0);
        level.playSound(null, this.spawner(), ModSounds.ARENA_SEAL_CAGE.get(), SoundSource.BLOCKS, 2.5f, 0.9f + this.conqueredCount() * 0.05f);
        this.announce(level, false);
        this.sync(level);
        this.setChanged();
    }

    /** Alle Käfige sind bezwungen: Die Ränge jubeln, der Wind wirbelt über dem Podest – der Meister ist bereit. */
    private void ready(ServerLevel level) {
        this.setPhase(level, ArenaSealBlock.Phase.READY);
        Vec3 c = Vec3.atCenterOf(this.spawner());
        level.playSound(null, this.spawner(), ModSounds.ARENA_SEAL_READY.get(), SoundSource.BLOCKS, 4.0f, 1.0f);
        level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, c.x, c.y + 0.5, c.z, 1, 0.0, 0.0, 0.0, 0.0);
        this.announce(level, true);
    }

    /** Sagt allen in und an der Arena, wie viele Käfige bezwungen sind (oder dass der Meister bereit ist). */
    private void announce(ServerLevel level, boolean ready) {
        Vec3 c = this.center();
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(c) < 40.0 * 40.0) {
                this.tell(level, player, ready);
            }
        }
    }

    public void tell(ServerLevel level, Player player) {
        this.tell(level, player, this.phase() == ArenaSealBlock.Phase.READY);
    }

    private void tell(ServerLevel level, Player player, boolean ready) {
        Component text = switch (this.phase()) {
            case AWAKE -> Component.translatable("block.kingdomsatwar.arena_seal.awake").withColor(VentorEntity.WIND);
            case CONQUERED -> this.conqueredCount() == 0
                    ? Component.translatable("block.kingdomsatwar.arena_seal.conquered").withStyle(ChatFormatting.GRAY)
                    : progress();
            default -> ready ? Component.translatable("block.kingdomsatwar.arena_seal.ready").withColor(VentorEntity.WIND) : progress();
        };
        player.sendOverlayMessage(text);
        this.told.put(player.getUUID(), level.getGameTime());
    }

    private Component progress() {
        return Component.translatable("block.kingdomsatwar.arena_seal.progress", this.conqueredCount(), this.cages.size())
                .withColor(VentorEntity.BRONZE);
    }

    /** Lässt den Meister aufsteigen (auch für Tests und Befehle: dann gelten alle Käfige als bezwungen). */
    public @Nullable VentorEntity summon(ServerLevel level) {
        VentorEntity existing = this.ventor(level);
        if (existing != null) {
            return existing;
        }
        VentorEntity entity = ModEntities.VENTOR.get().create(level, EntitySpawnReason.TRIGGERED);
        if (entity == null) {
            return null;
        }
        BlockPos top = this.spawner().above();
        entity.snapTo(top.getX() + 0.5, top.getY(), top.getZ() + 0.5, 180.0f, 0.0f);
        entity.bindTo(top, this.worldPosition);
        entity.rise();
        Player near = level.getNearestPlayer(top.getX() + 0.5, top.getY(), top.getZ() + 0.5, 40.0, false);
        if (near != null) {
            float yaw = (float) Math.toDegrees(Math.atan2(near.getZ() - (top.getZ() + 0.5), near.getX() - (top.getX() + 0.5))) - 90.0f;
            entity.snapTo(top.getX() + 0.5, top.getY(), top.getZ() + 0.5, yaw, 0.0f);
            entity.yBodyRot = yaw;
            entity.yHeadRot = yaw;
        }
        level.addFreshEntity(entity);
        this.ventor = entity.getUUID();
        this.missing = 0;
        this.setPhase(level, ArenaSealBlock.Phase.AWAKE);
        return entity;
    }

    /** Für Befehle: alle Käfige gelten als bezwungen, und der Meister steigt sofort auf. */
    public @Nullable VentorEntity awaken(ServerLevel level) {
        if (!this.scanned) {
            this.scan(level);
        }
        this.conquered = (1 << this.cages.size()) - 1;
        this.sync(level);
        return this.summon(level);
    }

    /** Er fand niemanden mehr und sank in den Spawner zurück: Beim nächsten Besuch steigt er wieder auf. */
    public void onVentorRetreated(ServerLevel level) {
        this.ventor = null;
        this.setPhase(level, ArenaSealBlock.Phase.READY);
    }

    /** Er ist besiegt: Die Ränge jubeln; wer ihn erneut fordern will, muss die Käfige noch einmal bezwingen. */
    public void onVentorDefeated(ServerLevel level) {
        this.ventor = null;
        this.fought = true;
        this.conquered = 0;
        this.setPhase(level, ArenaSealBlock.Phase.CONQUERED);
        level.playSound(null, this.spawner(), ModSounds.VENTOR_CROWD.get(), SoundSource.BLOCKS, 4.0f, 1.0f);
    }

    /** Für Befehle: zurück auf Anfang (der Meister, wenn da, verschwindet; die Käfige zählen von vorn). */
    public void reset(ServerLevel level) {
        VentorEntity entity = this.ventor(level);
        if (entity != null) {
            entity.discard();
        }
        this.ventor = null;
        this.missing = 0;
        this.fought = false;
        this.conquered = 0;
        this.scanned = false;
        this.setPhase(level, ArenaSealBlock.Phase.DORMANT);
    }

    private void setPhase(ServerLevel level, ArenaSealBlock.Phase next) {
        BlockState state = this.getBlockState();
        if (state.hasProperty(ArenaSealBlock.PHASE) && state.getValue(ArenaSealBlock.PHASE) != next) {
            level.setBlock(this.worldPosition, state.setValue(ArenaSealBlock.PHASE, next), Block.UPDATE_ALL);
        }
        this.sync(level);
        this.setChanged();
    }

    private void sync(ServerLevel level) {
        BlockState state = level.getBlockState(this.worldPosition);
        level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.writeShared(output);
        output.putBoolean("Scanned", this.scanned);
        output.putBoolean("Fought", this.fought);
        if (this.ventor != null) {
            output.store("Ventor", UUIDUtil.CODEC, this.ventor);
        }
    }

    /** Was auch der Client wissen muss: die Käfige und welche bezwungen sind. */
    private void writeShared(ValueOutput output) {
        output.store("Cages", BlockPos.CODEC.listOf(), List.copyOf(this.cages));
        output.putInt("Conquered", this.conquered);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.cages.clear();
        input.read("Cages", BlockPos.CODEC.listOf()).ifPresent(this.cages::addAll);
        this.conquered = input.getIntOr("Conquered", 0);
        this.scanned = input.getBooleanOr("Scanned", this.scanned || !this.cages.isEmpty());
        this.fought = input.getBooleanOr("Fought", this.fought);
        input.read("Ventor", UUIDUtil.CODEC).ifPresent(id -> this.ventor = id);
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(),
                org.slf4j.LoggerFactory.getLogger(ArenaSealBlockEntity.class))) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            this.writeShared(output);
            return output.buildResult();
        }
    }
}
