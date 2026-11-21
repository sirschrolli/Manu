package io.github.sirschrolli.kingdomsatwar.entity.monster;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
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
 * Nachtwidder: ein mächtiger Widder der Schattengipfel und der Bleichen Öde mit Hörnern aus Nachtkristall. Erblickt er
 * einen Eindringling, senkt er den Kopf, scharrt zweimal mit dem Huf – und stürmt los, geradeaus, bis zu sechzehn Blöcke
 * weit: Wen er trifft, den schleudert er weit fort (an den Klippen der Gipfel oft in die Tiefe). Rennt er gegen eine Wand,
 * steht er eine Weile benommen da und ist verwundbarer. Aus der Nähe stößt er mit den Hörnern zu oder bäumt sich auf und
 * stampft: Ein Ring aus Kristallsplittern bricht aus dem Boden.
 */
public class NightRamEntity extends Monster {
    public static final byte EVENT_BUTT = 84;
    public static final byte EVENT_PREPARE = 85;
    public static final byte EVENT_CHARGE = 86;
    public static final byte EVENT_STUN = 87;
    public static final byte EVENT_STOMP = 88;
    public static final int PREPARE_TICKS = 30;
    public static final int CHARGE_TICKS = 40;
    public static final double CHARGE_SPEED = 0.62;
    public static final double CHARGE_RANGE = 16.0;
    public static final int STUN_TICKS = 50;
    public static final int STOMP_TICKS = 24;
    public static final int STOMP_HIT = 14;
    public static final double STOMP_RADIUS = 3.6;
    /** Zustände (synchronisiert, für die Haltung im Modell). */
    public static final byte IDLE = 0;
    public static final byte PREPARE = 1;
    public static final byte CHARGE = 2;
    public static final byte STUNNED = 3;
    public static final byte STOMP = 4;
    private static final EntityDataAccessor<Byte> DATA_STATE = SynchedEntityData.defineId(NightRamEntity.class, EntityDataSerializers.BYTE);

    public final AnimationState buttAnimation = new AnimationState();
    public final AnimationState prepareAnimation = new AnimationState();
    public final AnimationState stompAnimation = new AnimationState();
    public final AnimationState stunAnimation = new AnimationState();
    private int stateTicks;
    private int chargeCooldown = 60;
    private int stompCooldown = 80;
    private Vec3 chargeDir = Vec3.ZERO;
    private Vec3 chargeFrom = Vec3.ZERO;

    public NightRamEntity(EntityType<? extends NightRamEntity> type, Level level) {
        super(type, level);
        this.xpReward = 14;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.2)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, IDLE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.15, true) {
            @Override
            public boolean canUse() {
                return NightRamEntity.this.state() == IDLE && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return NightRamEntity.this.state() == IDLE && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 14.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SoldierEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity.class, false));
    }

    public byte state() {
        return this.entityData.get(DATA_STATE);
    }

    private void setState(byte state, int ticks) {
        this.entityData.set(DATA_STATE, state);
        this.stateTicks = ticks;
    }

    /** Für Tests und die Vorführung: sofort den Ansturm vorbereiten (auf das Ziel). */
    public void forceCharge() {
        this.startPrepare();
    }

    /** Für Tests und die Vorführung: sofort aufbäumen und stampfen. */
    public void forceStomp() {
        this.startStomp();
    }

    /** Für die Vorführung: eine Haltung zeigen. */
    public void forceState(byte state) {
        this.setState(state, 400);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.chargeCooldown > 0) {
            this.chargeCooldown--;
        }
        if (this.stompCooldown > 0) {
            this.stompCooldown--;
        }
        LivingEntity target = this.getTarget();
        switch (this.state()) {
            case PREPARE -> this.tickPrepare(level, target);
            case CHARGE -> this.tickCharge(level);
            case STUNNED -> {
                this.getNavigation().stop();
                if (--this.stateTicks <= 0) {
                    this.setState(IDLE, 0);
                }
            }
            case STOMP -> {
                this.getNavigation().stop();
                this.stateTicks--;
                if (this.stateTicks == STOMP_TICKS - STOMP_HIT) {
                    this.stomp(level);
                }
                if (this.stateTicks <= 0) {
                    this.setState(IDLE, 0);
                }
            }
            default -> {
                if (target == null || !target.isAlive() || this.isNoAi()) {
                    return;
                }
                double d = this.distanceTo(target);
                if (d >= 5.0 && d <= CHARGE_RANGE && this.chargeCooldown == 0 && this.onGround() && this.hasLineOfSight(target)
                        && Math.abs(target.getY() - this.getY()) < 3.0) {
                    this.startPrepare();
                } else if (d < 3.4 && this.stompCooldown == 0 && this.onGround() && this.random.nextInt(4) == 0) {
                    this.startStomp();
                }
            }
        }
    }

    private void startPrepare() {
        this.getNavigation().stop();
        this.setState(PREPARE, PREPARE_TICKS);
        this.level().broadcastEntityEvent(this, EVENT_PREPARE);
        this.playSound(ModSounds.NIGHT_RAM_BLEAT.get(), 2.2f, 0.85f);
    }

    private void tickPrepare(ServerLevel level, @Nullable LivingEntity target) {
        this.getNavigation().stop();
        if (target != null) {
            this.faceTowards(target);
        }
        this.stateTicks--;
        if (this.stateTicks == PREPARE_TICKS - 9 || this.stateTicks == PREPARE_TICKS - 21) {
            // scharren: Erde und Frost fliegen hinter ihm auf
            this.playSound(ModSounds.NIGHT_RAM_SCRAPE.get(), 1.4f, 0.9f + this.random.nextFloat() * 0.2f);
            BlockState below = level.getBlockState(this.blockPosition().below());
            if (!below.isAir()) {
                Vec3 back = this.getViewVector(1.0f).multiply(-1.0, 0.0, -1.0).normalize();
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, below), this.getX() + back.x * 0.8, this.getY() + 0.1,
                        this.getZ() + back.z * 0.8, 14, 0.25, 0.05, 0.25, 0.15);
            }
            level.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 1.1, this.getZ(), 6, 0.4, 0.3, 0.4, 0.02);
        }
        if (this.stateTicks <= 0) {
            Vec3 dir = target != null ? target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0)
                    : this.getViewVector(1.0f).multiply(1.0, 0.0, 1.0);
            this.chargeDir = dir.lengthSqr() < 1.0e-4 ? Vec3.ZERO : dir.normalize();
            this.chargeFrom = this.position();
            this.setState(CHARGE, CHARGE_TICKS);
            level.broadcastEntityEvent(this, EVENT_CHARGE);
            this.playSound(ModSounds.NIGHT_RAM_CHARGE.get(), 2.4f, 1.0f);
        }
    }

    /** Der Ansturm: geradeaus, bis er trifft, gegen eine Wand rennt oder sechzehn Blöcke weit gelaufen ist. */
    private void tickCharge(ServerLevel level) {
        this.getNavigation().stop();
        Vec3 v = this.getDeltaMovement();
        this.setDeltaMovement(this.chargeDir.x * CHARGE_SPEED, v.y, this.chargeDir.z * CHARGE_SPEED);
        float yaw = (float) (Mth.atan2(this.chargeDir.z, this.chargeDir.x) * Mth.RAD_TO_DEG) - 90.0f;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
        if (this.tickCount % 3 == 0) {
            level.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 0.3, this.getZ(), 3, 0.4, 0.1, 0.4, 0.02);
            BlockState below = level.getBlockState(this.blockPosition().below());
            if (!below.isAir()) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, below), this.getX(), this.getY() + 0.1, this.getZ(), 4, 0.4, 0.05,
                        0.4, 0.1);
            }
        }
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.5),
                e -> e != this && e.isAlive() && !(e instanceof NightRamEntity))) {
            if (e.hurtServer(level, this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) + 2.0f)) {
                e.push(this.chargeDir.x * 1.9, 0.55, this.chargeDir.z * 1.9);
                e.needsSync = true;
                level.sendParticles(ParticleTypes.CRIT, e.getX(), e.getY(0.6), e.getZ(), 16, 0.3, 0.3, 0.3, 0.3);
                this.playSound(ModSounds.NIGHT_RAM_IMPACT.get(), 2.0f, 1.1f);
                level.broadcastEntityEvent(this, EVENT_BUTT);
            }
            this.endCharge(false);
            return;
        }
        this.stateTicks--;
        if (this.horizontalCollision && this.stateTicks < CHARGE_TICKS - 2) {
            // gegen die Wand: Krachen, Splitter, benommen
            this.playSound(ModSounds.NIGHT_RAM_IMPACT.get(), 2.4f, 0.7f);
            Vec3 at = this.position().add(this.chargeDir.scale(0.9)).add(0.0, 1.0, 0.0);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ShadowBlocks.NIGHT_CRYSTAL.get().defaultBlockState()), at.x, at.y,
                    at.z, 24, 0.3, 0.4, 0.3, 0.2);
            this.endCharge(true);
            return;
        }
        if (this.stateTicks <= 0 || this.position().distanceTo(this.chargeFrom) >= CHARGE_RANGE) {
            this.endCharge(false);
        }
    }

    private void endCharge(boolean stunned) {
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.2, 1.0, 0.2));
        this.chargeCooldown = 110 + this.random.nextInt(60);
        if (stunned) {
            this.setState(STUNNED, STUN_TICKS);
            this.level().broadcastEntityEvent(this, EVENT_STUN);
        } else {
            this.setState(IDLE, 0);
        }
    }

    private void startStomp() {
        this.setState(STOMP, STOMP_TICKS);
        this.stompCooldown = 120 + this.random.nextInt(60);
        this.level().broadcastEntityEvent(this, EVENT_STOMP);
        this.playSound(ModSounds.NIGHT_RAM_BLEAT.get(), 1.8f, 1.1f);
    }

    /** Die Vorderhufe schlagen nieder: ein Ring aus Kristallsplittern bricht rings um ihn aus dem Boden. */
    private void stomp(ServerLevel level) {
        this.playSound(ModSounds.NIGHT_RAM_STOMP.get(), 2.4f, 1.0f);
        BlockParticleOption crystal = new BlockParticleOption(ParticleTypes.BLOCK, ShadowBlocks.NIGHT_CRYSTAL.get().defaultBlockState());
        for (int i = 0; i < 24; i++) {
            double a = i / 24.0 * Math.PI * 2.0;
            for (double r = 1.2; r <= STOMP_RADIUS; r += 1.2) {
                double x = this.getX() + Math.cos(a) * r;
                double z = this.getZ() + Math.sin(a) * r;
                level.sendParticles(crystal, x, this.getY() + 0.2, z, 2, 0.1, 0.25, 0.1, 0.12);
            }
            level.sendParticles(ParticleTypes.END_ROD, this.getX() + Math.cos(a) * STOMP_RADIUS, this.getY() + 0.3,
                    this.getZ() + Math.sin(a) * STOMP_RADIUS, 1, 0.0, 0.15, 0.0, 0.02);
        }
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(STOMP_RADIUS, 1.5, STOMP_RADIUS),
                e -> e != this && e.isAlive() && !(e instanceof NightRamEntity) && e.distanceToSqr(this) <= STOMP_RADIUS * STOMP_RADIUS + 1.0)) {
            if (e.hurtServer(level, this.damageSources().mobAttack(this), 5.0f)) {
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1), this);
                e.push(0.0, 0.45, 0.0);
                e.needsSync = true;
            }
        }
    }

    private void faceTowards(Entity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_BUTT);
        this.playSound(ModSounds.NIGHT_RAM_IMPACT.get(), 1.2f, 1.2f + this.random.nextFloat() * 0.2f);
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            // der Hornstoß hebt an
            living.push(0.0, 0.35, 0.0);
            living.needsSync = true;
        }
        return hit;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        // benommen nach dem Anprall: verwundbarer
        if (this.state() == STUNNED) {
            damage *= 1.5f;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_BUTT -> this.buttAnimation.start(this.tickCount);
            case EVENT_PREPARE -> this.prepareAnimation.start(this.tickCount);
            case EVENT_CHARGE -> this.prepareAnimation.stop();
            case EVENT_STUN -> this.stunAnimation.start(this.tickCount);
            case EVENT_STOMP -> this.stompAnimation.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Level level = this.level();
        if (level.isClientSide()) {
            if (this.state() == STUNNED && this.tickCount % 4 == 0) {
                // Sterne kreisen um den Kopf
                double a = this.tickCount * 0.35;
                Vec3 head = this.position().add(this.getViewVector(1.0f).multiply(1.0, 0.0, 1.0).normalize().scale(1.1)).add(0.0, 1.8, 0.0);
                level.addParticle(ParticleTypes.CRIT, head.x + Math.cos(a) * 0.5, head.y, head.z + Math.sin(a) * 0.5, 0.0, 0.0, 0.0);
            }
            if (this.random.nextInt(50) == 0) {
                // Frostatem: ein feiner, bleichblauer Hauch aus den Nüstern
                Vec3 look = this.getViewVector(1.0f);
                for (int i = 0; i < 4; i++) {
                    level.addParticle(new DustParticleOptions(0xD4E2FF, 0.45f + this.random.nextFloat() * 0.25f), this.getX() + look.x * 1.3,
                            this.getY() + 1.02, this.getZ() + look.z * 1.3, look.x * 0.05 + this.random.nextGaussian() * 0.01, 0.0,
                            look.z * 0.05 + this.random.nextGaussian() * 0.01);
                }
            }
        }
    }

    /** In der Schattenwelt auf den Gipfeln und in der Öde, wo es dunkel genug ist; nie mehr als zwei auf dreißig Blöcke. */
    public static boolean checkRamSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL || !checkMobSpawnRules(type, level, reason, pos, random)) {
            return false;
        }
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        return level.getBrightness(LightLayer.BLOCK, pos) < 7
                && level.getEntitiesOfClass(NightRamEntity.class, new AABB(pos).inflate(30.0)).size() < 2;
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.NIGHT_RAM_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.NIGHT_RAM_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.NIGHT_RAM_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.NIGHT_RAM_STEP.get(), this.state() == CHARGE ? 0.8f : 0.4f, 0.9f + this.random.nextFloat() * 0.2f);
    }
}
