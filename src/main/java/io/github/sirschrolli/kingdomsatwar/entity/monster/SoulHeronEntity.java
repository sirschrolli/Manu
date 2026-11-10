package io.github.sirschrolli.kingdomsatwar.entity.monster;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.EnumSet;
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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Seelenreiher: watet durch die Seelensümpfe der Schattenwelt und verteidigt sein Revier. Er lauert mit eingezogenem Hals
 * und stößt dann blitzschnell mit dem Speerschnabel zu (Reichweite gut dreieinhalb Blöcke, verlangsamt); wer ihm zu nah
 * kommt, den treibt er mit einem Schlag der glimmenden Schwingen zurück (Rückstoß, Seelennebel blendet). Ist sein Opfer
 * weiter weg oder auf festem Land, hebt er ab, steigt auf und stößt im Sturzflug herab.
 */
public class SoulHeronEntity extends Monster {
    public static final byte EVENT_STAB = 80;
    public static final byte EVENT_BUFFET = 81;
    public static final byte EVENT_TAKEOFF = 82;
    public static final byte EVENT_DIVE = 83;
    /** Ein Stoß: so lange holt er aus (der Hals zieht sich zurück), dann schnellt der Schnabel vor. */
    public static final int STAB_WINDUP = 9;
    public static final int STAB_TICKS = 18;
    /** So weit reicht der Schnabel (Blöcke, Mitte zu Mitte). */
    public static final double STAB_REACH = 3.6;
    public static final int BUFFET_TICKS = 18;
    public static final int BUFFET_HIT = 10;
    /** Aufsteigen, Anflug, Sturzflug (höchstens so viele Ticks). */
    public static final int RISE_TICKS = 18;
    public static final int APPROACH_TICKS = 40;
    public static final int DIVE_TICKS = 26;
    private static final EntityDataAccessor<Boolean> DATA_FLYING = SynchedEntityData.defineId(SoulHeronEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState stabAnimation = new AnimationState();
    public final AnimationState buffetAnimation = new AnimationState();
    public final AnimationState diveAnimation = new AnimationState();
    private int stabTicks;
    private int buffetTicks;
    private int stabCooldown = 20;
    private int buffetCooldown = 60;
    private int diveCooldown = 120;
    /** 0 = am Boden, 1 = Aufsteigen, 2 = Anflug, 3 = Sturzflug. */
    private int flight;
    private int flightTicks;
    private Vec3 diveDir = Vec3.ZERO;

    public SoulHeronEntity(EntityType<? extends SoulHeronEntity> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 34.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLYING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new StalkGoal(this));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // sein Revier: wer näher als zwölf Blöcke kommt
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> target.distanceToSqr(this) < 144.0));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SoldierEntity.class, 10, true, false,
                (target, level) -> target.distanceToSqr(this) < 144.0));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, 10, true, false,
                (target, level) -> target.distanceToSqr(this) < 144.0));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity.class,
                10, true, false, (target, level) -> target.distanceToSqr(this) < 144.0));
    }

    /** Fliegt er gerade (Aufsteigen, Anflug oder Sturzflug)? */
    public boolean isFlying() {
        return this.entityData.get(DATA_FLYING);
    }

    /** Stößt er gerade zu (oder holt aus)? */
    public boolean isStabbing() {
        return this.stabTicks > 0;
    }

    /** Für Tests und die Vorführung: sofort zustoßen. */
    public void forceStab() {
        this.startStab();
    }

    /** Für Tests und die Vorführung: sofort mit den Schwingen schlagen. */
    public void forceBuffet() {
        this.startBuffet();
    }

    /** Für Tests und die Vorführung: sofort abheben (Sturzflug auf das Ziel). */
    public void forceTakeOff() {
        this.takeOff();
    }

    /** Für die Vorführung: im Flug zeigen (ohne Angriff). */
    public void forceFlying(boolean flying) {
        this.entityData.set(DATA_FLYING, flying);
        this.setNoGravity(flying);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.stabCooldown > 0) {
            this.stabCooldown--;
        }
        if (this.buffetCooldown > 0) {
            this.buffetCooldown--;
        }
        if (this.diveCooldown > 0) {
            this.diveCooldown--;
        }
        LivingEntity target = this.getTarget();
        if (this.flight > 0) {
            this.tickFlight(level, target);
            return;
        }
        if (this.stabTicks > 0) {
            this.stabTicks--;
            this.getNavigation().stop();
            if (target != null) {
                this.getLookControl().setLookAt(target, 60.0f, 60.0f);
            }
            if (this.stabTicks == STAB_TICKS - STAB_WINDUP) {
                this.strike(level, target);
            }
            return;
        }
        if (this.buffetTicks > 0) {
            this.buffetTicks--;
            this.getNavigation().stop();
            if (this.buffetTicks == BUFFET_TICKS - BUFFET_HIT) {
                this.buffet(level);
            }
            return;
        }
        if (target == null || !target.isAlive() || this.isNoAi()) {
            return;
        }
        double d = this.distanceTo(target);
        boolean sees = this.hasLineOfSight(target);
        if (d <= 2.4 && this.buffetCooldown == 0 && this.random.nextInt(3) == 0) {
            this.startBuffet();
        } else if (d <= STAB_REACH && this.stabCooldown == 0 && sees) {
            this.startStab();
        } else if (d >= 7.0 && d <= 20.0 && this.diveCooldown == 0 && this.onGround() && sees && this.random.nextInt(10) == 0) {
            this.takeOff();
        }
    }

    private void startStab() {
        this.stabTicks = STAB_TICKS;
        this.stabCooldown = 26 + this.random.nextInt(14);
        this.level().broadcastEntityEvent(this, EVENT_STAB);
    }

    /** Der Stoß: trifft nur, wer noch in Reichweite ist (wer zurückweicht, entgeht ihm). */
    private void strike(ServerLevel level, @Nullable LivingEntity target) {
        this.playSound(ModSounds.SOUL_HERON_STAB.get(), 1.4f, 0.9f + this.random.nextFloat() * 0.2f);
        if (target == null || !target.isAlive() || this.distanceTo(target) > STAB_REACH + 0.6 || !this.hasLineOfSight(target)) {
            return;
        }
        if (target.hurtServer(level, this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1), this);
            Vec3 at = target.position().add(0.0, target.getBbHeight() * 0.6, 0.0);
            level.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 10, 0.2, 0.2, 0.2, 0.25);
            level.sendParticles(ParticleTypes.SOUL, at.x, at.y, at.z, 4, 0.2, 0.2, 0.2, 0.02);
        }
    }

    private void startBuffet() {
        this.buffetTicks = BUFFET_TICKS;
        this.buffetCooldown = 100 + this.random.nextInt(60);
        this.level().broadcastEntityEvent(this, EVENT_BUFFET);
    }

    /** Schwingenschlag: wer vor ihm steht, wird zurückgetrieben, der Seelennebel blendet. */
    private void buffet(ServerLevel level) {
        this.playSound(ModSounds.SOUL_HERON_WINGS.get(), 2.0f, 0.8f);
        Vec3 look = this.getViewVector(1.0f).multiply(1.0, 0.0, 1.0).normalize();
        Vec3 c = this.position().add(look.scale(1.2)).add(0.0, 1.2, 0.0);
        level.sendParticles(ParticleTypes.SOUL, c.x, c.y, c.z, 30, 1.2, 0.6, 1.2, 0.06);
        level.sendParticles(ParticleTypes.CLOUD, c.x, c.y - 0.6, c.z, 16, 1.0, 0.2, 1.0, 0.05);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3.4, 1.5, 3.4),
                e -> e != this && e.isAlive() && !(e instanceof SoulHeronEntity))) {
            Vec3 to = e.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            if (to.lengthSqr() > 3.4 * 3.4 || (to.lengthSqr() > 0.25 && to.normalize().dot(look) < -0.2)) {
                continue;
            }
            Vec3 away = to.lengthSqr() < 1.0e-4 ? look : to.normalize();
            if (e.hurtServer(level, this.damageSources().mobAttack(this), 3.0f)) {
                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0), this);
            }
            e.push(away.x * 1.15, 0.42, away.z * 1.15);
            e.needsSync = true;
        }
    }

    // ------------------------------------------------------------------ Flug

    private void takeOff() {
        this.flight = 1;
        this.flightTicks = 0;
        this.entityData.set(DATA_FLYING, true);
        this.setNoGravity(true);
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, EVENT_TAKEOFF);
        this.playSound(ModSounds.SOUL_HERON_CALL.get(), 2.4f, 1.0f);
        this.playSound(ModSounds.SOUL_HERON_WINGS.get(), 1.6f, 1.1f);
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 0.3, this.getZ(), 16, 0.8, 0.2, 0.8, 0.04);
        }
    }

    private void tickFlight(ServerLevel level, @Nullable LivingEntity target) {
        this.flightTicks++;
        this.fallDistance = 0.0;
        if (target == null || !target.isAlive()) {
            this.land();
            return;
        }
        if (this.flightTicks % 7 == 0 && this.flight < 3) {
            this.playSound(ModSounds.SOUL_HERON_WINGS.get(), 0.9f, 1.2f + this.random.nextFloat() * 0.2f);
        }
        switch (this.flight) {
            case 1 -> {
                // steil aufsteigen, ein wenig zum Ziel hin
                Vec3 flat = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
                Vec3 side = flat.lengthSqr() < 1.0e-4 ? Vec3.ZERO : flat.normalize().scale(0.08);
                this.setDeltaMovement(side.x, 0.34, side.z);
                if (this.flightTicks >= RISE_TICKS) {
                    this.flight = 2;
                    this.flightTicks = 0;
                }
            }
            case 2 -> {
                // über das Ziel: auf einen Punkt fünf Blöcke darüber zu
                Vec3 aim = target.position().add(0.0, 5.0, 0.0).subtract(this.position());
                Vec3 v = aim.lengthSqr() < 1.0e-4 ? Vec3.ZERO : aim.normalize().scale(0.5);
                this.setDeltaMovement(this.getDeltaMovement().scale(0.6).add(v.scale(0.4)));
                this.faceTowards(target);
                double flat = target.position().subtract(this.position()).horizontalDistance();
                if (flat < 3.5 || this.flightTicks >= APPROACH_TICKS) {
                    this.flight = 3;
                    this.flightTicks = 0;
                    this.diveDir = target.getEyePosition().subtract(this.position()).normalize().scale(1.15);
                    level.broadcastEntityEvent(this, EVENT_DIVE);
                    this.playSound(ModSounds.SOUL_HERON_CALL.get(), 2.6f, 0.8f);
                }
            }
            default -> {
                // Sturzflug, ein wenig nachgeführt
                Vec3 want = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).subtract(this.position());
                if (want.lengthSqr() > 1.0e-4) {
                    this.diveDir = this.diveDir.scale(0.85).add(want.normalize().scale(1.15 * 0.15));
                }
                this.setDeltaMovement(this.diveDir);
                this.faceTowards(target);
                if (this.flightTicks % 2 == 0) {
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
                }
                if (this.getBoundingBox().inflate(0.6).intersects(target.getBoundingBox())) {
                    float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) + 3.0f;
                    if (target.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                        Vec3 push = this.diveDir.multiply(1.0, 0.0, 1.0);
                        push = push.lengthSqr() < 1.0e-4 ? Vec3.ZERO : push.normalize().scale(1.0);
                        target.push(push.x, 0.35, push.z);
                        target.needsSync = true;
                        level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.6), target.getZ(), 14, 0.3, 0.3, 0.3, 0.3);
                    }
                    this.playSound(ModSounds.SOUL_HERON_STAB.get(), 1.8f, 0.8f);
                    this.land();
                } else if (this.onGround() || this.horizontalCollision || this.flightTicks >= DIVE_TICKS) {
                    this.land();
                }
            }
        }
    }

    private void faceTowards(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    private void land() {
        this.flight = 0;
        this.flightTicks = 0;
        this.entityData.set(DATA_FLYING, false);
        this.setNoGravity(false);
        this.diveCooldown = 180 + this.random.nextInt(80);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 0.0, 0.3));
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_STAB -> this.stabAnimation.start(this.tickCount);
            case EVENT_BUFFET -> this.buffetAnimation.start(this.tickCount);
            case EVENT_TAKEOFF -> this.buffetAnimation.stop();
            case EVENT_DIVE -> this.diveAnimation.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.random.nextInt(this.isFlying() ? 2 : 14) == 0) {
            // glimmende Seelenfunken fallen aus dem Gefieder
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getRandomX(0.6), this.getY() + 1.0 + this.random.nextDouble() * 0.8,
                    this.getRandomZ(0.6), 0.0, -0.02, 0.0);
        }
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    /**
     * Im Seelensumpf, nah am Wasser (im Umkreis von vier Blöcken), wenn es dort dunkel genug ist – und nie mehr als ein
     * Reiher auf zwanzig Blöcke.
     */
    public static boolean checkHeronSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL || !checkMobSpawnRules(type, level, reason, pos, random)) {
            return false;
        }
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        if (level.getBrightness(LightLayer.BLOCK, pos) >= 7) {
            return false;
        }
        boolean water = false;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 0, 4))) {
            if (level.getFluidState(p).is(FluidTags.WATER)) {
                water = true;
                break;
            }
        }
        return water && level.getEntitiesOfClass(SoulHeronEntity.class, new AABB(pos).inflate(20.0)).isEmpty();
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.SOUL_HERON_CALL.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SOUL_HERON_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SOUL_HERON_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.SOUL_HERON_STEP.get(), 0.35f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    // ------------------------------------------------------------------ Ziele

    /** Pirschen: langsam heran bis in Stoßweite, dann still stehen und lauern. */
    private static final class StalkGoal extends Goal {
        private final SoulHeronEntity heron;
        private int repath;

        StalkGoal(SoulHeronEntity heron) {
            this.heron = heron;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.heron.getTarget();
            return target != null && target.isAlive() && this.heron.flight == 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.heron.getTarget();
            if (target == null || this.heron.flight > 0) {
                return;
            }
            this.heron.getLookControl().setLookAt(target, 30.0f, 30.0f);
            if (this.heron.stabTicks > 0 || this.heron.buffetTicks > 0) {
                return;
            }
            double d = this.heron.distanceTo(target);
            if (d > STAB_REACH - 0.6) {
                if (--this.repath <= 0) {
                    this.repath = 8;
                    this.heron.getNavigation().moveTo(target, d > 8.0 ? 1.15 : 0.85);
                }
            } else {
                this.heron.getNavigation().stop();
            }
        }
    }
}
