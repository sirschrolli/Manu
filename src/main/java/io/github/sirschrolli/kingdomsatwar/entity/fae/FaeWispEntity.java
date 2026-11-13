package io.github.sirschrolli.kingdomsatwar.entity.fae;

import io.github.sirschrolli.kingdomsatwar.entity.creature.WillOWispEntity;
import io.github.sirschrolli.kingdomsatwar.fae.feature.FaeBuild;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Feen-Irrlicht: ein goldenes Licht mit rosa Schein, das über das Feenreich gaukelt. Nähert sich ein Wanderer, zieht es
 * vor ihm her – zur nächsten Ruine, deren Truhe noch niemand geöffnet hat. Es wartet, wenn man zurückbleibt, und
 * verlischt in einem Funkenregen, sobald der Schatz erreicht ist. Wer es angreift, verliert seinen Führer.
 */
public class FaeWispEntity extends WillOWispEntity {
    /** So weit sucht das Licht nach einer unberührten Truhe (in Chunks). */
    private static final int SEARCH_CHUNKS = 7;
    private @Nullable BlockPos treasure;
    private int searchIn;

    public FaeWispEntity(EntityType<? extends FaeWispEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new GuideGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 0.8));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0f));
    }

    @Override
    protected ParticleOptions glowParticle() {
        return ModParticles.FAE_SPARK.get();
    }

    @Override
    protected ParticleOptions sparkParticle() {
        return ModParticles.FAE_SPARK.get();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (--this.searchIn <= 0) {
            this.searchIn = 100 + this.random.nextInt(60);
            if (this.treasure == null || !isTreasure(level, this.treasure)) {
                this.treasure = findTreasure(level, this.blockPosition());
            }
        }
    }

    /** Die Truhe, zu der das Licht führt (oder null). */
    public @Nullable BlockPos treasure() {
        return this.treasure;
    }

    static boolean isTreasure(ServerLevel level, BlockPos pos) {
        return level.isLoaded(pos) && level.getBlockEntity(pos) instanceof ChestBlockEntity chest
                && ModLootTables.FAE_RUIN_CHEST.equals(chest.getLootTable());
    }

    /** Die nächste noch verschlossene Ruinentruhe in den geladenen Chunks ringsum. */
    static @Nullable BlockPos findTreasure(ServerLevel level, BlockPos from) {
        ChunkPos center = ChunkPos.containing(from);
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dx = -SEARCH_CHUNKS; dx <= SEARCH_CHUNKS; dx++) {
            for (int dz = -SEARCH_CHUNKS; dz <= SEARCH_CHUNKS; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(center.x() + dx, center.z() + dz);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity entity : chunk.getBlockEntities().values()) {
                    if (entity instanceof ChestBlockEntity chest && ModLootTables.FAE_RUIN_CHEST.equals(chest.getLootTable())) {
                        double d = entity.getBlockPos().distSqr(from);
                        if (d < bestDistance) {
                            bestDistance = d;
                            best = entity.getBlockPos();
                        }
                    }
                }
            }
        }
        return best;
    }

    /** Am Ziel: in einem Funkenregen verlöschen. */
    void arrive(ServerLevel level) {
        level.sendParticles(ModParticles.FAE_SPARK.get(), this.getX(), this.getY(0.5), this.getZ(), 40, 0.6, 0.6, 0.6, 0.04);
        level.playSound(null, this.blockPosition(), ModSounds.FAIRY_GIFT.get(), SoundSource.NEUTRAL, 0.8f, 1.3f);
        this.discard();
    }

    @Override
    public boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) {
        // angegriffen: es führt nicht mehr
        this.treasure = null;
        this.searchIn = 1200;
        return super.hurtServer(level, source, amount);
    }

    /** Auf Gras und Moos des Feenreichs, egal wie hell, eher selten. */
    public static boolean checkFaeWispSpawnRules(EntityType<? extends Mob> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        return FaeBuild.soil(level.getBlockState(pos.below())) && random.nextInt(3) == 0;
    }

    /**
     * Führen: Ist ein Spieler in der Nähe und gibt es einen Schatz, fliegt das Licht ein Stück vor ihm her in Richtung der
     * Truhe – nie mehr als zwölf Blöcke voraus – und wartet, wenn er zurückbleibt.
     */
    static final class GuideGoal extends Goal {
        private final FaeWispEntity wisp;
        private @Nullable Player follower;

        GuideGoal(FaeWispEntity wisp) {
            this.wisp = wisp;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.wisp.treasure == null) {
                return false;
            }
            this.follower = this.wisp.level().getNearestPlayer(this.wisp, 18.0);
            return this.follower != null && !this.follower.isSpectator();
        }

        @Override
        public boolean canContinueToUse() {
            return this.wisp.treasure != null && this.follower != null && this.follower.isAlive()
                    && this.follower.distanceToSqr(this.wisp) < 40.0 * 40.0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            BlockPos treasure = this.wisp.treasure;
            Player player = this.follower;
            if (treasure == null || player == null || !(this.wisp.level() instanceof ServerLevel level)) {
                return;
            }
            Vec3 goal = Vec3.atCenterOf(treasure).add(0.0, 1.5, 0.0);
            double toGoal = this.wisp.position().distanceTo(goal);
            if (toGoal < 3.0 && player.distanceTo(this.wisp) < 9.0) {
                this.wisp.arrive(level);
                return;
            }
            this.wisp.getLookControl().setLookAt(player);
            double gap = player.distanceTo(this.wisp);
            if (gap > 12.0) {
                // warten (sanft auf der Stelle schweben)
                this.wisp.getNavigation().stop();
                this.wisp.setDeltaMovement(this.wisp.getDeltaMovement().scale(0.7));
                return;
            }
            // ein Stück voraus, Richtung Schatz, knapp über dem Boden
            Vec3 dir = goal.subtract(this.wisp.position());
            double step = Math.min(toGoal, 5.0);
            Vec3 next = this.wisp.position().add(dir.normalize().scale(step));
            int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) Math.floor(next.x), (int) Math.floor(next.z));
            double y = Math.max(next.y, ground + 1.8);
            this.wisp.getMoveControl().setWantedPosition(next.x, Math.min(y, ground + 3.5), next.z, gap < 6.0 ? 1.1 : 0.7);
        }
    }
}
