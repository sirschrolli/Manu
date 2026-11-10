package io.github.sirschrolli.kingdomsatwar.entity.monster;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Glutwurm: eine gewaltige Schlange aus Basalt und Glut, die durch die Lavaseen des Nethers schwimmt – nur der
 * Rückenkamm zieht eine Spur durch die Lava. Kommt jemand ans Ufer, <b>taucht er auf</b> und richtet sich wie eine Kobra
 * hoch über der Lava auf: Aus der Ferne <b>spuckt</b> er Magmabatzen (sie setzen in Brand, wo sie aufschlagen), wer zu
 * nahe steht, den trifft sein <b>Biss</b> – er schnellt vor, packt zu und setzt ihn in Flammen. Dann taucht er wieder
 * ab und erscheint woanders. An Land kriecht er nur langsam zur nächsten Lava zurück; außerhalb erkaltet er nach und
 * nach (Schaden, langsamer). Feuer und Lava schaden ihm nicht.
 */
public class EmberWyrmEntity extends Monster {
    public static final byte EVENT_SURFACE = 70;
    public static final byte EVENT_SPIT = 71;
    public static final byte EVENT_BITE = 72;
    public static final byte EVENT_DIVE = 73;
    /** So lange (Ticks) bleibt er aufgerichtet; in diesen Ticks spuckt er; in diesem schnappt er zu; ab diesem taucht er ab. */
    public static final int SURFACE_TICKS = 72;
    public static final int[] SPIT_TICKS = {20, 32, 44};
    public static final int BITE_TICK = 24;
    public static final int DIVE_TICK = 60;
    /** Bis zu dieser Entfernung (waagerecht) schnappt er zu, statt zu spucken. */
    public static final double BITE_REACH = 6.5;
    private static final EntityDimensions REARED = EntityDimensions.scalable(1.8f, 3.9f).withEyeHeight(3.4f);

    private static final EntityDataAccessor<Boolean> DATA_REARING = SynchedEntityData.defineId(EmberWyrmEntity.class,
            EntityDataSerializers.BOOLEAN);

    public final AnimationState spitAnimation = new AnimationState();
    public final AnimationState biteAnimation = new AnimationState();
    private int surfaceTicks;
    private int surfaceCooldown = 40;
    private boolean biting;
    private int dry;
    private @Nullable Vec3 wander;
    private int wanderTicks;

    public EmberWyrmEntity(EntityType<? extends EmberWyrmEntity> type, Level level) {
        super(type, level);
        this.xpReward = 16;
        this.setPathfindingMalus(PathType.LAVA, 0.0f);
        this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, 0.0f);
        this.setPathfindingMalus(PathType.FIRE, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.16)
                .add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 28.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_REARING, false);
    }

    @Override
    protected void registerGoals() {
        // an Land beißt er wie andere Monster (in der Lava greift er nur aufgerichtet an)
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true) {
            @Override
            public boolean canUse() {
                return !EmberWyrmEntity.this.isInLava() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !EmberWyrmEntity.this.isInLava() && super.canContinueToUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SoldierEntity.class, false));
    }

    /** Richtet er sich gerade über der Lava auf? */
    public boolean isRearing() {
        return this.entityData.get(DATA_REARING);
    }

    /** Lässt ihn sofort auftauchen und angreifen (für Tests und die Vorführung). */
    public void forceSurface(LivingEntity target) {
        this.setTarget(target);
        this.surface();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_REARING.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isRearing() ? REARED : super.getDefaultDimensions(pose);
    }

    // ------------------------------------------------------------------ Schwimmen in der Lava

    @Override
    public boolean canStandOnFluid(net.minecraft.world.level.material.FluidState fluid) {
        return false;
    }

    @Override
    public void travel(Vec3 input) {
        if (this.isInLava()) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.86));
            return;
        }
        super.travel(input);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        boolean lava = this.isInLava();
        this.setNoGravity(lava);
        LivingEntity target = this.getTarget();
        if (this.surfaceTicks > 0) {
            this.tickSurface(level, target);
            return;
        }
        if (this.surfaceCooldown > 0) {
            this.surfaceCooldown--;
        }
        if (lava) {
            this.dry = 0;
            this.swim(level, target);
            if (target != null && target.isAlive() && this.surfaceCooldown == 0 && this.horizontalDistanceTo(target) < 14.0
                    && Math.abs(target.getY() - this.getY()) < 8.0 && this.hasLineOfSight(target)) {
                this.surface();
            }
        } else {
            // an Land erkaltet er nach und nach und sucht die Lava
            if (++this.dry % 40 == 0) {
                this.hurtServer(level, this.damageSources().dryOut(), 2.0f);
                this.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, false));
                level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.6), this.getZ(), 6, 0.5, 0.3, 0.5, 0.02);
            }
            if (target == null && this.tickCount % 40 == 0) {
                BlockPos lavaPos = this.findLava(level, 12);
                if (lavaPos != null) {
                    this.getNavigation().moveTo(lavaPos.getX() + 0.5, lavaPos.getY(), lavaPos.getZ() + 0.5, 1.2);
                }
            }
        }
    }

    private double horizontalDistanceTo(Entity other) {
        double dx = other.getX() - this.getX();
        double dz = other.getZ() - this.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Schwimmt dicht unter der Oberfläche: zu seinem Ziel (so weit die Lava reicht) oder ziellos umher. */
    private void swim(ServerLevel level, @Nullable LivingEntity target) {
        Vec3 goal = null;
        if (target != null && target.isAlive() && this.distanceToSqr(target) < 32.0 * 32.0) {
            goal = target.position();
        } else {
            if (this.wander == null || --this.wanderTicks <= 0 || this.position().distanceToSqr(this.wander) < 4.0) {
                this.wander = this.randomLavaSpot(level);
                this.wanderTicks = 120;
            }
            goal = this.wander;
        }
        Vec3 v = this.getDeltaMovement();
        if (goal != null) {
            Vec3 to = new Vec3(goal.x - this.getX(), 0.0, goal.z - this.getZ());
            if (to.lengthSqr() > 1.0) {
                Vec3 dir = to.normalize();
                // nur weiter, wo Lava ist
                BlockPos ahead = BlockPos.containing(this.getX() + dir.x * 1.8, this.getY() + 0.5, this.getZ() + dir.z * 1.8);
                if (level.getFluidState(ahead).is(FluidTags.LAVA)) {
                    v = v.add(dir.scale(0.035));
                } else {
                    v = v.multiply(0.6, 1.0, 0.6);
                    this.wander = null;
                }
                float yaw = (float) (Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) - 90.0f;
                this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, 6.0f));
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.getYRot();
            }
        }
        // die Höhe: knapp unter der Oberfläche, nur der Kamm schaut heraus
        double surface = this.lavaSurface(level);
        double wantY = surface - 0.95;
        v = new Vec3(v.x, Mth.clamp((wantY - this.getY()) * 0.2, -0.12, 0.12), v.z);
        double h = v.horizontalDistance();
        if (h > 0.24) {
            v = new Vec3(v.x / h * 0.24, v.y, v.z / h * 0.24);
        }
        this.setDeltaMovement(v);
        if (this.tickCount % 3 == 0 && h > 0.05) {
            level.sendParticles(ParticleTypes.LAVA, this.getX(), surface, this.getZ(), 1, 0.4, 0.0, 0.4, 0.0);
        }
    }

    /** Wo die Lava über ihm endet (Oberfläche). */
    private double lavaSurface(ServerLevel level) {
        BlockPos.MutableBlockPos pos = BlockPos.containing(this.getX(), this.getY() + 0.2, this.getZ()).mutable();
        int guard = 0;
        while (guard++ < 8 && level.getFluidState(pos.above()).is(FluidTags.LAVA)) {
            pos.move(0, 1, 0);
        }
        return pos.getY() + 0.9;
    }

    private @Nullable Vec3 randomLavaSpot(ServerLevel level) {
        for (int i = 0; i < 10; i++) {
            BlockPos p = BlockPos.containing(this.getX() + (this.random.nextDouble() - 0.5) * 20.0, this.getY() + 0.5,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 20.0);
            if (level.getFluidState(p).is(FluidTags.LAVA)) {
                return Vec3.atCenterOf(p);
            }
        }
        return null;
    }

    private @Nullable BlockPos findLava(ServerLevel level, int radius) {
        BlockPos origin = this.blockPosition();
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(origin.offset(-radius, -3, -radius), origin.offset(radius, 2, radius))) {
            if (level.getFluidState(p).is(FluidTags.LAVA) && level.getFluidState(p).isSource()) {
                double d = p.distSqr(origin);
                if (d < bestD) {
                    bestD = d;
                    best = p.immutable();
                }
            }
        }
        return best;
    }

    // ------------------------------------------------------------------ Auftauchen, Spucken, Biss

    private void surface() {
        this.surfaceTicks = SURFACE_TICKS;
        this.biting = false;
        this.entityData.set(DATA_REARING, true);
        this.level().broadcastEntityEvent(this, EVENT_SURFACE);
        this.playSound(ModSounds.EMBER_WYRM_SURFACE.get(), 3.0f, 0.9f + this.random.nextFloat() * 0.15f);
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.8, 0.4, 0.8, 0.0);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.5, this.getZ(), 16, 0.8, 0.8, 0.8, 0.03);
        }
    }

    private void tickSurface(ServerLevel level, @Nullable LivingEntity target) {
        int t = SURFACE_TICKS - this.surfaceTicks--;
        // aufgerichtet bleibt er an seinem Platz; der Leib hält ihn über der Lava
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.0, 0.5));
        if (this.isInLava()) {
            double wantY = this.lavaSurface(level) - 0.95;
            this.setPos(this.getX(), this.getY() + Mth.clamp(wantY - this.getY(), -0.1, 0.1), this.getZ());
        }
        if (target != null && target.isAlive()) {
            float yaw = (float) (Mth.atan2(target.getZ() - this.getZ(), target.getX() - this.getX()) * Mth.RAD_TO_DEG) - 90.0f;
            this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, 10.0f));
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
            if (t == 12) {
                this.biting = this.horizontalDistanceTo(target) < BITE_REACH;
                if (this.biting) {
                    level.broadcastEntityEvent(this, EVENT_BITE);
                }
            }
            if (this.biting && t == BITE_TICK) {
                this.bite(level, target);
            }
            if (!this.biting) {
                for (int spit : SPIT_TICKS) {
                    if (t == spit - 6) {
                        level.broadcastEntityEvent(this, EVENT_SPIT);
                    }
                    if (t == spit) {
                        this.spit(level, target);
                    }
                }
            }
        }
        if (t == DIVE_TICK) {
            this.entityData.set(DATA_REARING, false);
            level.broadcastEntityEvent(this, EVENT_DIVE);
            this.playSound(ModSounds.EMBER_WYRM_DIVE.get(), 2.0f, 1.0f);
            level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.7, 0.3, 0.7, 0.0);
        }
        if (this.surfaceTicks <= 0) {
            this.entityData.set(DATA_REARING, false);
            this.surfaceCooldown = 80 + this.random.nextInt(70);
            this.wander = null;
        }
    }

    /** Wo der Kopf ist, wenn er aufgerichtet ist. */
    public Vec3 headPosition() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return this.position().add(-Mth.sin(yaw) * 1.4, this.isRearing() ? 3.4 : 0.8, Mth.cos(yaw) * 1.4);
    }

    private void spit(ServerLevel level, LivingEntity target) {
        Vec3 mouth = this.headPosition();
        Vec3 aim = target.position().add(0.0, target.getBbHeight() * 0.4, 0.0).add(target.getDeltaMovement().multiply(6.0, 0.0, 6.0));
        int flight = 12 + (int) (Math.sqrt(mouth.distanceToSqr(aim)) * 0.8);
        MonsterShot.shoot(level, this, MonsterShot.Kind.MAGMA, mouth, MonsterShot.lob(mouth, aim, flight, 0.04), 6.0f);
        this.playSound(ModSounds.EMBER_WYRM_SPIT.get(), 2.0f, 0.9f + this.random.nextFloat() * 0.2f);
        level.sendParticles(ParticleTypes.FLAME, mouth.x, mouth.y, mouth.z, 10, 0.2, 0.2, 0.2, 0.05);
    }

    private void bite(ServerLevel level, LivingEntity target) {
        this.playSound(ModSounds.EMBER_WYRM_BITE.get(), 2.0f, 1.0f);
        Vec3 mouth = this.headPosition();
        Vec3 strike = mouth.add(target.position().subtract(mouth).normalize().scale(Math.min(5.0, mouth.distanceTo(target.position()))));
        level.sendParticles(ParticleTypes.FLAME, strike.x, strike.y + 0.5, strike.z, 20, 0.4, 0.4, 0.4, 0.06);
        if (this.horizontalDistanceTo(target) < BITE_REACH + 0.5 && target.getY() < this.getY() + 5.0) {
            if (target.hurtServer(level, this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                target.igniteForSeconds(6.0f);
                Vec3 away = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                away = away.lengthSqr() < 1.0e-4 ? Vec3.ZERO : away.normalize();
                target.push(away.x * 0.6, 0.35, away.z * 0.6);
                target.needsSync = true;
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_BITE);
        this.playSound(ModSounds.EMBER_WYRM_BITE.get(), 1.5f, 1.1f);
        boolean hit = super.doHurtTarget(level, target);
        if (hit) {
            target.igniteForSeconds(4.0f);
        }
        return hit;
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_SPIT -> this.spitAnimation.start(this.tickCount);
            case EVENT_BITE -> this.biteAnimation.start(this.tickCount);
            case EVENT_SURFACE, EVENT_DIVE -> { }
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.random.nextInt(4) == 0) {
            // Funken und Rauch aus den Fugen
            this.level().addParticle(this.random.nextInt(3) == 0 ? ParticleTypes.SMOKE : ParticleTypes.SMALL_FLAME, this.getRandomX(0.8),
                    this.getY(0.5 + this.random.nextDouble() * 0.5), this.getRandomZ(0.8), 0.0, 0.03, 0.0);
        }
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    /** In großen Lavaseen des Nethers – nie mehr als zwei in der Nähe. */
    public static boolean checkWyrmSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        if (!level.getFluidState(pos).is(FluidTags.LAVA)) {
            return false;
        }
        BlockState above = level.getBlockState(pos.above());
        if (!above.isAir() && !level.getFluidState(pos.above()).is(FluidTags.LAVA)) {
            return false;
        }
        int lava = 0;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 0, 4))) {
            if (level.getFluidState(p).is(FluidTags.LAVA)) {
                lava++;
            }
        }
        return lava >= 60 && level.getEntitiesOfClass(EmberWyrmEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(48.0)).size() < 2;
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.EMBER_WYRM_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.EMBER_WYRM_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.EMBER_WYRM_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected float getSoundVolume() {
        return 2.0f;
    }
}
