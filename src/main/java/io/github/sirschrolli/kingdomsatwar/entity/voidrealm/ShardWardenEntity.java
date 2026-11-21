package io.github.sirschrolli.kingdomsatwar.entity.voidrealm;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Splitterwächter: ein uralter Wächter aus Sternenmarmor, der über den Ruinen der Leere wacht.
 * <ul>
 *     <li>Er <b>ruht</b>, bis jemand näher als neun Blöcke kommt oder ihn trifft – dann erwacht er: der Kopf hebt sich,
 *     das Herz in seiner offenen Brust flammt auf.</li>
 *     <li>Auf Abstand schleudert er <b>Kristallsplitter</b> (drei kurz nacheinander).</li>
 *     <li>Wer ihm zu nahe kommt, den trifft ein <b>Erdstoß</b>: beide Fäuste schlagen auf den Boden, Splitter fliegen,
 *     und alles ringsum wird fortgeschleudert. Dazwischen teilt er schwere Faustschläge aus.</li>
 *     <li>Findet er keine Feinde mehr, kehrt er an seinen Platz zurück und ruht wieder.</li>
 * </ul>
 */
public class ShardWardenEntity extends Monster {
    public static final byte EVENT_PUNCH = 70;
    public static final byte EVENT_SLAM = 71;
    public static final byte EVENT_VOLLEY = 72;
    public static final byte EVENT_AWAKEN = 73;
    private static final EntityDataAccessor<Boolean> DATA_DORMANT = SynchedEntityData.defineId(ShardWardenEntity.class,
            EntityDataSerializers.BOOLEAN);

    public static final double WAKE_RANGE = 9.0;
    public static final int AWAKEN_TICKS = 34;
    public static final int SLAM_TICKS = 26;
    public static final int SLAM_IMPACT = 15;
    public static final int VOLLEY_TICKS = 38;

    public final AnimationState punchAnimation = new AnimationState();
    public final AnimationState slamAnimation = new AnimationState();
    public final AnimationState volleyAnimation = new AnimationState();
    public final AnimationState awakenAnimation = new AnimationState();
    /** Wie hell das Herz glüht (0 ruhend … 1 wach, weich) – nur auf dem Client. */
    public float awake;
    public float awakeO;

    private enum Action { NONE, AWAKEN, SLAM, VOLLEY }

    private Action action = Action.NONE;
    private int actionTicks;
    private int slamCooldown = 40;
    private int volleyCooldown = 60;
    private int idleTicks;
    private @Nullable BlockPos home;

    public ShardWardenEntity(EntityType<? extends ShardWardenEntity> type, Level level) {
        super(type, level);
        this.xpReward = 22;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.2)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_DORMANT, true);
    }

    public boolean isDormant() {
        return this.entityData.get(DATA_DORMANT);
    }

    public void setDormant(boolean dormant) {
        this.entityData.set(DATA_DORMANT, dormant);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ActionGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true) {
            @Override
            public boolean canUse() {
                return !ShardWardenEntity.this.isDormant() && super.canUse();
            }
        });
        this.goalSelector.addGoal(5, new ReturnHomeGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0f) {
            @Override
            public boolean canUse() {
                return !ShardWardenEntity.this.isDormant() && super.canUse();
            }
        });
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !ShardWardenEntity.this.isDormant() && super.canUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // ruhend bemerkt er nur, wer ihm nahe kommt
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> !this.isDormant() || this.distanceToSqr(target) < WAKE_RANGE * WAKE_RANGE));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SoldierEntity.class, 10, true, false,
                (target, level) -> !this.isDormant() || this.distanceToSqr(target) < WAKE_RANGE * WAKE_RANGE));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.home = this.blockPosition();
        this.setDormant(true);
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    public boolean isPushable() {
        return !this.isDormant() && super.isPushable();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            this.awakeO = this.awake;
            float wanted = this.isDormant() ? 0.0f : 1.0f;
            this.awake += (wanted - this.awake) * 0.08f;
            if (!this.isDormant() && this.random.nextInt(4) == 0) {
                // Funken aus der offenen Brust
                float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
                double fx = -Mth.sin(yaw) * 0.45;
                double fz = Mth.cos(yaw) * 0.45;
                this.level().addParticle(ParticleTypes.END_ROD, this.getX() + fx, this.getY() + 2.05, this.getZ() + fz,
                        fx * 0.05, 0.02, fz * 0.05);
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.home == null) {
            this.home = this.blockPosition();
        }
        if (this.slamCooldown > 0) {
            this.slamCooldown--;
        }
        if (this.volleyCooldown > 0) {
            this.volleyCooldown--;
        }
        LivingEntity target = this.getTarget();
        if (this.isDormant()) {
            // ruhend steht er still wie eine Statue (seine Ziele laufen weiter, damit er bemerkt, wer ihm nahe kommt)
            this.getNavigation().stop();
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
            if (target != null && target.isAlive()) {
                this.awaken(level);
            }
            return;
        }
        // keine Feinde mehr: zurück an den Platz und wieder ruhen
        if (target == null && this.action == Action.NONE) {
            this.idleTicks++;
            if (this.idleTicks > 200 && this.home != null && this.home.closerToCenterThan(this.position(), 2.5)) {
                this.setDormant(true);
                this.getNavigation().stop();
                this.playSound(ModSounds.SHARD_WARDEN_AWAKEN.get(), 0.7f, 0.6f);
            }
        } else {
            this.idleTicks = 0;
        }
    }

    private void awaken(ServerLevel level) {
        this.setDormant(false);
        this.action = Action.AWAKEN;
        this.actionTicks = AWAKEN_TICKS;
        this.idleTicks = 0;
        level.broadcastEntityEvent(this, EVENT_AWAKEN);
        this.playSound(ModSounds.SHARD_WARDEN_AWAKEN.get(), 1.6f, 1.0f);
        level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(1.0), this.getZ(), 24, 0.6, 0.8, 0.6, 0.06);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, VoidBlocks.ASTRAL_MARBLE.base().get().defaultBlockState()),
                this.getX(), this.getY(0.5), this.getZ(), 30, 0.7, 1.0, 0.7, 0.1);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isDormant() && source.getEntity() instanceof LivingEntity attacker && !this.isInvulnerableTo(level, source)) {
            this.setTarget(attacker);
        }
        // ruhend ist er eine Statue aus Marmor: Treffer prallen zur Hälfte ab
        return super.hurtServer(level, source, this.isDormant() ? amount * 0.5f : amount);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_PUNCH);
        boolean hit = super.doHurtTarget(level, target);
        if (hit) {
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.35, 0.0));
            this.playSound(ModSounds.SHARD_WARDEN_SLAM.get(), 0.8f, 1.3f);
        }
        return hit;
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_PUNCH -> this.punchAnimation.start(this.tickCount);
            case EVENT_SLAM -> this.slamAnimation.start(this.tickCount);
            case EVENT_VOLLEY -> this.volleyAnimation.start(this.tickCount);
            case EVENT_AWAKEN -> this.awakenAnimation.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    // ------------------------------------------------------------------ Erdstoß und Splitter

    private void slamImpact(ServerLevel level) {
        Vec3 c = this.position().add(this.getLookAngle().multiply(1.2, 0.0, 1.2));
        this.playSound(ModSounds.SHARD_WARDEN_SLAM.get(), 2.0f, 0.75f);
        BlockState crystal = VoidBlocks.VOID_CRYSTAL.get().defaultBlockState();
        for (int i = 0; i < 24; i++) {
            double a = i / 24.0 * Math.PI * 2.0;
            for (double r : new double[] {1.5, 3.0}) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, crystal), c.x + Math.cos(a) * r, c.y + 0.1, c.z + Math.sin(a) * r,
                        2, 0.1, 0.05, 0.1, 0.15);
            }
        }
        level.sendParticles(ParticleTypes.END_ROD, c.x, c.y + 0.2, c.z, 30, 1.8, 0.2, 1.8, 0.12);
        level.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y + 0.3, c.z, 1, 0.0, 0.0, 0.0, 0.0);
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.1f;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0, 1.5, 4.0),
                e -> e != this && !(e instanceof ShardWardenEntity) && e.isAlive() && !(e instanceof Player p && (p.isCreative() || p.isSpectator())))) {
            double d = e.position().distanceTo(c);
            if (d > 4.5 || !e.onGround() && e.getY() > c.y + 1.5) {
                continue;
            }
            float f = (float) Math.max(0.4, 1.0 - d / 5.0);
            if (e.hurtServer(level, this.damageSources().mobAttack(this), damage * f)) {
                Vec3 away = e.position().subtract(c).multiply(1.0, 0.0, 1.0);
                away = away.lengthSqr() < 1.0e-4 ? this.getLookAngle().multiply(1.0, 0.0, 1.0) : away.normalize();
                e.push(away.x * 1.1 * f, 0.55 * f, away.z * 1.1 * f);
                e.needsSync = true;
            }
        }
    }

    private void fireShard(ServerLevel level, LivingEntity target, int index) {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        // aus der erhobenen rechten Hand (vom Wächter aus gesehen)
        Vec3 from = this.position().add(-Mth.cos(yaw) * 1.1 - Mth.sin(yaw) * 0.6, 3.1, -Mth.sin(yaw) * 1.1 + Mth.cos(yaw) * 0.6);
        double flight = from.distanceTo(target.getEyePosition()) / 1.15;
        Vec3 aim = target.getEyePosition().add(target.getDeltaMovement().multiply(flight * 0.8, 0.0, flight * 0.8)).subtract(0.0, 0.35, 0.0);
        Vec3 dir = aim.subtract(from);
        double spread = 0.045 * dir.length();
        dir = dir.add((this.random.nextDouble() - 0.5) * spread * (index == 0 ? 0.2 : 1.0), (this.random.nextDouble() - 0.5) * spread * 0.5,
                (this.random.nextDouble() - 0.5) * spread * (index == 0 ? 0.2 : 1.0));
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6f;
        WardenShardEntity.shoot(level, this, from, dir, 1.15, damage);
        this.playSound(ModSounds.SHARD_WARDEN_SHOOT.get(), 1.4f, 0.9f + index * 0.12f);
    }

    // ------------------------------------------------------------------ Speichern

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Dormant", this.isDormant());
        if (this.home != null) {
            output.putLong("Home", this.home.asLong());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setDormant(input.getBooleanOr("Dormant", true));
        this.home = input.getLong("Home").map(BlockPos::of).orElse(null);
    }

    // ------------------------------------------------------------------ Laichen und Geräusche

    /** Auf festem Boden in den Ruinen der Leere (selten von selbst, meist mit den Ruinen erzeugt). */
    public static boolean checkWardenSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && (EntitySpawnReason.isSpawner(reason)
                || level.getBlockState(pos.below()).isSolid() && random.nextInt(3) == 0);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isDormant() ? null : ModSounds.SHARD_WARDEN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SHARD_WARDEN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SHARD_WARDEN_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.SHARD_WARDEN_STEP.get(), 0.9f, 0.8f + this.random.nextFloat() * 0.15f);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    // ------------------------------------------------------------------ Ziele

    /** Erwachen, Erdstoß und Splittersalve: währenddessen steht er und blickt sein Ziel an. */
    static class ActionGoal extends Goal {
        private final ShardWardenEntity warden;
        private Action pending = Action.NONE;
        private int shots;

        ActionGoal(ShardWardenEntity warden) {
            this.warden = warden;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            ShardWardenEntity w = this.warden;
            this.pending = Action.NONE;
            if (w.action == Action.AWAKEN) {
                this.pending = Action.AWAKEN;
                return true;
            }
            LivingEntity target = w.getTarget();
            if (w.isDormant() || target == null || !target.isAlive()) {
                return false;
            }
            double d = w.distanceTo(target);
            if (w.slamCooldown == 0 && d < 3.6 && w.onGround()) {
                this.pending = Action.SLAM;
            } else if (w.volleyCooldown == 0 && d > 5.0 && d < 22.0 && w.getSensing().hasLineOfSight(target)) {
                this.pending = Action.VOLLEY;
            }
            return this.pending != Action.NONE;
        }

        @Override
        public boolean canContinueToUse() {
            return this.warden.action != Action.NONE && this.warden.actionTicks > 0 && this.warden.isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            ShardWardenEntity w = this.warden;
            if (this.pending != Action.AWAKEN) {
                w.action = this.pending;
                w.actionTicks = this.pending == Action.SLAM ? SLAM_TICKS : VOLLEY_TICKS;
            }
            w.getNavigation().stop();
            ServerLevel level = getServerLevel(w);
            this.shots = 0;
            if (w.action == Action.SLAM) {
                level.broadcastEntityEvent(w, EVENT_SLAM);
                w.playSound(ModSounds.SHARD_WARDEN_CHARGE.get(), 1.2f, 0.7f);
            } else if (w.action == Action.VOLLEY) {
                level.broadcastEntityEvent(w, EVENT_VOLLEY);
                w.playSound(ModSounds.SHARD_WARDEN_CHARGE.get(), 1.4f, 1.0f);
            }
        }

        @Override
        public void tick() {
            ShardWardenEntity w = this.warden;
            LivingEntity target = w.getTarget();
            if (target != null) {
                w.getLookControl().setLookAt(target, 20.0f, 30.0f);
                if (w.action != Action.AWAKEN) {
                    double dx = target.getX() - w.getX();
                    double dz = target.getZ() - w.getZ();
                    float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
                    w.setYRot(Mth.approachDegrees(w.getYRot(), yaw, 8.0f));
                    w.yBodyRot = w.getYRot();
                }
            }
            int elapsed = switch (w.action) {
                case SLAM -> SLAM_TICKS - w.actionTicks;
                case VOLLEY -> VOLLEY_TICKS - w.actionTicks;
                case AWAKEN -> AWAKEN_TICKS - w.actionTicks;
                default -> 0;
            };
            ServerLevel level = getServerLevel(w);
            if (w.action == Action.SLAM && elapsed == SLAM_IMPACT) {
                w.slamImpact(level);
            } else if (w.action == Action.VOLLEY && target != null && (elapsed == 20 || elapsed == 25 || elapsed == 30)) {
                w.fireShard(level, target, this.shots++);
            }
            w.actionTicks--;
        }

        @Override
        public void stop() {
            ShardWardenEntity w = this.warden;
            if (w.action == Action.SLAM) {
                w.slamCooldown = 70 + w.random.nextInt(40);
            } else if (w.action == Action.VOLLEY) {
                w.volleyCooldown = 80 + w.random.nextInt(40);
            }
            w.action = Action.NONE;
            w.actionTicks = 0;
        }
    }

    /** Ohne Feinde zurück an den Platz, den er bewacht. */
    static class ReturnHomeGoal extends Goal {
        private final ShardWardenEntity warden;

        ReturnHomeGoal(ShardWardenEntity warden) {
            this.warden = warden;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            ShardWardenEntity w = this.warden;
            return !w.isDormant() && w.getTarget() == null && w.home != null && !w.home.closerToCenterThan(w.position(), 2.0)
                    && w.idleTicks > 40;
        }

        @Override
        public void start() {
            BlockPos home = this.warden.home;
            if (home != null) {
                this.warden.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0.8);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.warden.getTarget() == null && !this.warden.getNavigation().isDone();
        }
    }
}
