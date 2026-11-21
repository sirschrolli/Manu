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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Aschengeist: ein schwebender Geist aus Asche und Seelenfeuer in den Seelensandtälern und Basaltdeltas des Nethers. Er
 * hält Abstand und umkreist seine Beute: Er schleudert <b>Seelenfeuer</b> (es brennt, lähmt und schwächt), wer ihm zu nahe
 * kommt, den hüllt er in eine <b>Aschewolke</b> (blind und langsam) oder reißt ihn mit seinen Krallen. Getroffen
 * <b>verweht</b> er zu Asche – unsichtbar und unverwundbar – und setzt sich hinter seinem Gegner wieder zusammen. Wasser
 * schadet ihm.
 */
public class AshWraithEntity extends Monster {
    public static final byte EVENT_BOLT = 70;
    public static final byte EVENT_CLOUD = 71;
    public static final byte EVENT_SLASH = 72;
    public static final byte EVENT_FADE = 73;
    public static final byte EVENT_RETURN = 74;
    /** In diesem Tick des Wurfs fliegt das Seelenfeuer los; in diesem Tick der Aschewolke breitet sie sich aus. */
    public static final int BOLT_RELEASE = 10;
    public static final int CLOUD_RELEASE = 14;
    /** So lange (Ticks) bleibt er verweht. */
    public static final int FADE_TICKS = 30;

    private static final EntityDataAccessor<Boolean> DATA_FADED = SynchedEntityData.defineId(AshWraithEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState boltAnimation = new AnimationState();
    public final AnimationState cloudAnimation = new AnimationState();
    public final AnimationState slashAnimation = new AnimationState();
    private int boltTicks;
    private int boltCooldown = 40;
    private int cloudTicks;
    private int cloudCooldown = 100;
    private int slashCooldown;
    private int fadeTicks;
    private int fadeCooldown;
    private float orbit;

    public AshWraithEntity(EntityType<? extends AshWraithEntity> type, Level level) {
        super(type, level);
        this.xpReward = 12;
        this.moveControl = new FlyingMoveControl<>(this, 20, true);
        this.setNoGravity(true);
        this.orbit = this.random.nextFloat() * Mth.TWO_PI;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 34.0)
                .add(Attributes.FLYING_SPEED, 0.45)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FADED, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SoldierEntity.class, true));
    }

    /** Ist er gerade zu Asche verweht (unsichtbar und unverwundbar)? */
    public boolean isFaded() {
        return this.entityData.get(DATA_FADED);
    }

    /** Für Tests und die Vorführung: sofort Seelenfeuer, Aschewolke oder Verwehen. */
    public void forceBolt(LivingEntity target) {
        this.setTarget(target);
        this.startBolt();
    }

    public void forceCloud(LivingEntity target) {
        this.setTarget(target);
        this.startCloud();
    }

    public void forceFade() {
        this.fade();
    }

    // ------------------------------------------------------------------ Schweben

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.fadeTicks > 0) {
            this.tickFade(level);
            return;
        }
        if (this.fadeCooldown > 0) {
            this.fadeCooldown--;
        }
        if (this.boltCooldown > 0) {
            this.boltCooldown--;
        }
        if (this.cloudCooldown > 0) {
            this.cloudCooldown--;
        }
        if (this.slashCooldown > 0) {
            this.slashCooldown--;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.drift(level);
            return;
        }
        double d = this.distanceTo(target);
        this.getLookControl().setLookAt(target, 30.0f, 30.0f);
        // Abstand halten und umkreisen; schwebt etwas über Augenhöhe seines Ziels
        this.orbit += 0.025f;
        double radius = this.cloudTicks > 0 ? d : 6.5 + Mth.sin(this.tickCount * 0.02f) * 1.5;
        Vec3 want = target.position().add(Mth.cos(this.orbit) * radius, 2.2 + Mth.sin(this.tickCount * 0.05f) * 0.6, Mth.sin(this.orbit) * radius);
        if (this.boltTicks == 0 && this.cloudTicks == 0) {
            this.getMoveControl().setWantedPosition(want.x, want.y, want.z, 1.0);
        }
        this.tickBolt(level, target);
        this.tickCloud(level, target);
        if (this.boltTicks == 0 && this.cloudTicks == 0) {
            if (d < 2.8 && this.slashCooldown == 0) {
                this.slash(level, target);
            } else if (d < 7.0 && this.cloudCooldown == 0) {
                this.startCloud();
            } else if (this.boltCooldown == 0 && d < 20.0 && this.hasLineOfSight(target)) {
                this.startBolt();
            }
        }
    }

    /** Ohne Beute treibt er langsam umher, ein, zwei Blöcke über dem Boden. */
    private void drift(ServerLevel level) {
        if (this.tickCount % 60 == 0 || !this.getMoveControl().hasWanted()) {
            Vec3 p = this.position().add((this.random.nextDouble() - 0.5) * 12.0, 0.0, (this.random.nextDouble() - 0.5) * 12.0);
            int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(p.x), Mth.floor(p.z));
            double y = ground < this.getY() + 6 && ground > this.getY() - 8 ? ground + 1.5 + this.random.nextDouble() : this.getY();
            this.getMoveControl().setWantedPosition(p.x, y, p.z, 0.5);
        }
    }

    // ------------------------------------------------------------------ Seelenfeuer

    private void startBolt() {
        this.boltTicks = 20;
        this.boltCooldown = 50 + this.random.nextInt(35);
        this.level().broadcastEntityEvent(this, EVENT_BOLT);
    }

    private void tickBolt(ServerLevel level, LivingEntity target) {
        if (this.boltTicks <= 0) {
            return;
        }
        int t = 20 - this.boltTicks--;
        this.setDeltaMovement(this.getDeltaMovement().scale(0.6));
        Vec3 hand = this.handPosition();
        if (t < BOLT_RELEASE && t % 2 == 0) {
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, hand.x, hand.y, hand.z, 3, 0.1, 0.1, 0.1, 0.01);
        }
        if (t == BOLT_RELEASE) {
            Vec3 aim = target.position().add(0.0, target.getBbHeight() * 0.55, 0.0).add(target.getDeltaMovement().scale(4.0));
            Vec3 dir = aim.subtract(hand).normalize();
            MonsterShot.shoot(level, this, MonsterShot.Kind.SOUL, hand, dir.scale(0.85), 5.0f);
            this.playSound(ModSounds.ASH_WRAITH_BOLT.get(), 1.6f, 0.9f + this.random.nextFloat() * 0.2f);
        }
    }

    private Vec3 handPosition() {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        return this.position().add(-Mth.sin(yaw) * 0.7 - Mth.cos(yaw) * 0.5, 1.5, Mth.cos(yaw) * 0.7 - Mth.sin(yaw) * 0.5);
    }

    // ------------------------------------------------------------------ Aschewolke

    private void startCloud() {
        this.cloudTicks = 30;
        this.cloudCooldown = 160 + this.random.nextInt(80);
        this.level().broadcastEntityEvent(this, EVENT_CLOUD);
        this.playSound(ModSounds.ASH_WRAITH_CLOUD.get(), 1.8f, 1.0f);
    }

    private void tickCloud(ServerLevel level, LivingEntity target) {
        if (this.cloudTicks <= 0) {
            return;
        }
        int t = 30 - this.cloudTicks--;
        this.setDeltaMovement(this.getDeltaMovement().scale(0.6));
        if (t < CLOUD_RELEASE) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.6), this.getZ(), 3, 0.4, 0.5, 0.4, 0.01);
        }
        if (t == CLOUD_RELEASE) {
            Vec3 at = target.position();
            AreaEffectCloud cloud = new AreaEffectCloud(level, at.x, at.y, at.z);
            cloud.setOwner(this);
            cloud.setRadius(3.2f);
            cloud.setRadiusPerTick(-0.012f);
            cloud.setDuration(120);
            cloud.setWaitTime(0);
            cloud.setCustomParticle(ParticleTypes.WHITE_ASH);
            cloud.setPotionContents(PotionContents.EMPTY.withEffectAdded(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0))
                    .withEffectAdded(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0)));
            level.addFreshEntity(cloud);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y + 0.6, at.z, 40, 1.6, 0.6, 1.6, 0.02);
            level.sendParticles(ParticleTypes.WHITE_ASH, at.x, at.y + 1.0, at.z, 60, 1.8, 1.0, 1.8, 0.02);
        }
    }

    // ------------------------------------------------------------------ Krallen

    private void slash(ServerLevel level, LivingEntity target) {
        this.slashCooldown = 30;
        level.broadcastEntityEvent(this, EVENT_SLASH);
        this.doHurtTarget(level, target);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0), this);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, living.getX(), living.getY(0.6), living.getZ(), 8, 0.3, 0.3, 0.3, 0.02);
        }
        return hit;
    }

    // ------------------------------------------------------------------ Verwehen

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isFaded() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive() && this.fadeCooldown == 0 && this.getHealth() < this.getMaxHealth() * 0.75f && this.random.nextInt(2) == 0
                && source.getEntity() instanceof LivingEntity) {
            this.fade();
        }
        return hurt;
    }

    private void fade() {
        this.fadeTicks = FADE_TICKS;
        this.fadeCooldown = 140;
        this.entityData.set(DATA_FADED, true);
        this.setInvisible(true);
        this.boltTicks = 0;
        this.cloudTicks = 0;
        this.level().broadcastEntityEvent(this, EVENT_FADE);
        this.playSound(ModSounds.ASH_WRAITH_FADE.get(), 1.6f, 1.0f);
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.WHITE_ASH, this.getX(), this.getY(0.5), this.getZ(), 80, 0.5, 0.9, 0.5, 0.05);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(0.5), this.getZ(), 20, 0.4, 0.8, 0.4, 0.04);
        }
    }

    private void tickFade(ServerLevel level) {
        this.fadeTicks--;
        if (this.fadeTicks % 3 == 0) {
            level.sendParticles(ParticleTypes.WHITE_ASH, this.getX(), this.getY(0.5), this.getZ(), 6, 0.6, 0.8, 0.6, 0.01);
        }
        if (this.fadeTicks > 0) {
            return;
        }
        // hinter seinem Gegner setzt er sich wieder zusammen
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            Vec3 look = target.getLookAngle().multiply(1.0, 0.0, 1.0);
            look = look.lengthSqr() < 1.0e-4 ? new Vec3(0.0, 0.0, 1.0) : look.normalize();
            Vec3 behind = target.position().subtract(look.scale(3.2)).add(0.0, 1.2, 0.0);
            if (level.noCollision(this, this.getBoundingBox().move(behind.subtract(this.position())))) {
                this.teleportTo(behind.x, behind.y, behind.z);
            }
        }
        this.entityData.set(DATA_FADED, false);
        this.setInvisible(false);
        level.broadcastEntityEvent(this, EVENT_RETURN);
        this.playSound(ModSounds.ASH_WRAITH_FADE.get(), 1.4f, 1.3f);
        level.sendParticles(ParticleTypes.WHITE_ASH, this.getX(), this.getY(0.5), this.getZ(), 60, 0.4, 0.8, 0.4, 0.02);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY(0.6), this.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
        this.slashCooldown = 0;
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_BOLT -> this.boltAnimation.start(this.tickCount);
            case EVENT_CLOUD -> this.cloudAnimation.start(this.tickCount);
            case EVENT_SLASH -> this.slashAnimation.start(this.tickCount);
            case EVENT_FADE, EVENT_RETURN -> { }
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && !this.isFaded()) {
            // Asche rieselt, aus dem Saum steigt Rauch, die Krallen glimmen
            if (this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.WHITE_ASH, this.getRandomX(0.6), this.getY(0.2 + this.random.nextDouble() * 0.7),
                        this.getRandomZ(0.6), 0.0, -0.02, 0.0);
            }
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX() + (this.random.nextDouble() - 0.5) * 0.4, this.getY() + 0.1,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.4, 0.0, -0.01, 0.0);
            }
            if (this.random.nextInt(10) == 0) {
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getRandomX(0.8), this.getY(0.45), this.getRandomZ(0.8), 0.0, 0.01, 0.0);
            }
        }
    }

    @Override
    public boolean isPushable() {
        return !this.isFaded() && super.isPushable();
    }

    /** In Seelensandtälern und Basaltdeltas – nie mehr als drei in der Nähe. */
    public static boolean checkWraithSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL || !Monster.checkMobSpawnRules(type, level, reason, pos, random)) {
            return false;
        }
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        return level.getEntitiesOfClass(AshWraithEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(32.0)).size() < 3;
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isFaded() ? null : ModSounds.ASH_WRAITH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ASH_WRAITH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ASH_WRAITH_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 140;
    }
}
