package io.github.sirschrolli.kingdomsatwar.entity.voidrealm;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Zerrling: ein Wesen, das die Leere verzerrt hat – ein Fehler im Bild. Er bewegt sich ruckhaft und <b>springt</b> in
 * kurzen Sätzen durch den Raum: mal näher an seine Beute, am liebsten in ihren Rücken; getroffen springt er oft davon.
 * Seine Krallen verlangsamen, und wo er war, flirrt noch kurz sein Nachbild.
 */
public class DistortlingEntity extends Monster {
    public static final byte EVENT_BLINK = 80;
    public static final byte EVENT_ATTACK = 81;
    private static final DustColorTransitionOptions GLITCH = new DustColorTransitionOptions(0x5AF0FF, 0xFF5AE0, 1.1f);

    public final AnimationState attackAnimation = new AnimationState();
    /** Zeitpunkt des letzten Sprungs (für das Nachbild auf dem Client) und wo er herkam. */
    public int blinkTick = -100;
    public Vec3 blinkFrom = Vec3.ZERO;
    private int blinkCooldown = 40;

    public DistortlingEntity(EntityType<? extends DistortlingEntity> type, Level level) {
        super(type, level);
        this.xpReward = 9;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SoldierEntity.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.random.nextInt(3) == 0) {
            this.level().addParticle(GLITCH, this.getRandomX(0.7), this.getRandomY(), this.getRandomZ(0.7), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.blinkCooldown > 0) {
            this.blinkCooldown--;
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        double d = this.distanceTo(target);
        if (d > 4.5 && d < 24.0 && this.random.nextInt(8) == 0) {
            // in den Rücken der Beute (oder wenigstens neben sie)
            Vec3 behind = target.position().subtract(target.getViewVector(1.0f).multiply(2.5, 0.0, 2.5));
            if (this.blinkNear(level, behind, 1.5) || this.blinkNear(level, target.position(), 3.5)) {
                this.blinkCooldown = 60 + this.random.nextInt(50);
            } else {
                this.blinkCooldown = 15;
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive() && this.random.nextFloat() < 0.45f && source.getEntity() instanceof LivingEntity attacker) {
            // weg vom Angreifer, vier bis acht Blöcke
            Vec3 away = this.position().subtract(attacker.position()).multiply(1.0, 0.0, 1.0);
            away = away.lengthSqr() < 1.0e-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
            if (this.blinkNear(level, this.position().add(away.scale(4.0 + this.random.nextDouble() * 4.0)), 2.0)) {
                this.blinkCooldown = 30 + this.random.nextInt(30);
            }
        }
        return hurt;
    }

    /** Sucht um {@code center} eine Stelle, an der er stehen kann, und springt dorthin. */
    public boolean blinkNear(ServerLevel level, Vec3 center, double spread) {
        for (int i = 0; i < 16; i++) {
            double x = center.x + (this.random.nextDouble() * 2.0 - 1.0) * spread;
            double z = center.z + (this.random.nextDouble() * 2.0 - 1.0) * spread;
            int y0 = Mth.floor(center.y);
            for (int dy = 2; dy >= -3; dy--) {
                BlockPos feet = BlockPos.containing(x, y0 + dy, z);
                if (this.canStandAt(level, feet)) {
                    Vec3 from = this.position();
                    Vec3 to = new Vec3(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
                    this.teleportTo(to.x, to.y, to.z);
                    this.getNavigation().stop();
                    this.blinkEffects(level, from, to);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canStandAt(ServerLevel level, BlockPos feet) {
        BlockState below = level.getBlockState(feet.below());
        if (!below.isFaceSturdy(level, feet.below(), net.minecraft.core.Direction.UP) || !level.getFluidState(feet).isEmpty()) {
            return false;
        }
        AABB box = this.getDimensions(this.getPose()).makeBoundingBox(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
        return level.noCollision(this, box) && !level.containsAnyLiquid(box);
    }

    private void blinkEffects(ServerLevel level, Vec3 from, Vec3 to) {
        level.broadcastEntityEvent(this, EVENT_BLINK);
        for (Vec3 p : new Vec3[] {from, to}) {
            level.sendParticles(GLITCH, p.x, p.y + 1.1, p.z, 22, 0.35, 0.8, 0.35, 0.0);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.x, p.y + 1.0, p.z, 14, 0.3, 0.7, 0.3, 0.05);
            level.playSound(null, p.x, p.y, p.z, ModSounds.DISTORTLING_BLINK.get(), SoundSource.HOSTILE, 1.0f,
                    0.85f + this.random.nextFloat() * 0.3f);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_ATTACK);
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1), this);
            this.playSound(ModSounds.DISTORTLING_ATTACK.get(), 1.0f, 0.9f + this.random.nextFloat() * 0.2f);
        }
        return hit;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_BLINK) {
            this.blinkTick = this.tickCount;
            this.blinkFrom = new Vec3(this.xOld, this.yOld, this.zOld);
        } else if (id == EVENT_ATTACK) {
            this.attackAnimation.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    /** Auf festem Boden in der Leere, nicht im Frieden und nicht im Schein von Fackeln (das Zwielicht stört ihn nicht). */
    public static boolean checkDistortlingSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && (EntitySpawnReason.isSpawner(reason)
                || level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos) < 8)
                && checkMobSpawnRules(type, level, reason, pos, random);
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.DISTORTLING_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.DISTORTLING_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DISTORTLING_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // fast lautlos: ein leises Knistern statt Schritten
        if (this.random.nextInt(3) == 0) {
            this.playSound(ModSounds.DISTORTLING_AMBIENT.get(), 0.12f, 1.8f);
        }
    }
}
