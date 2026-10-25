package io.github.sirschrolli.kingdomsatwar.entity.creature;

import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Lindwurm: ein flügelloser Drache der Berge und Tafelländer. Kriecht schlangengleich, beißt kräftig und
 * fegt mit dem Schwanz alles in seiner Nähe von den Beinen. Jagt Spieler und Tiere – nur gezähmte Tiere und
 * echte Drachen lässt er in Ruhe. Hinterlässt Erddrachen-Schuppen.
 */
public class LindwormEntity extends Monster {
    public static final byte EVENT_BITE = 123;
    public static final byte EVENT_SWEEP = 124;
    private static final int SWEEP_WINDUP = 8;
    private static final double SWEEP_RANGE = 3.6;

    public final AnimationState biteAnimation = new AnimationState();
    public final AnimationState sweepAnimation = new AnimationState();
    private int sweepCooldown = 60;
    private int sweepTimer;

    public LindwormEntity(EntityType<? extends LindwormEntity> type, Level level) {
        super(type, level);
        this.xpReward = 25;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Animal.class, 20, true, false,
                (target, level) -> !(target instanceof DragonEntity) && !(target instanceof TamableAnimal tame && tame.isTame())));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level) || !this.isAlive()) {
            return;
        }
        if (this.sweepCooldown > 0) {
            this.sweepCooldown--;
        }
        LivingEntity target = this.getTarget();
        if (this.sweepTimer == 0 && this.sweepCooldown == 0 && target != null && target.isAlive()
                && this.distanceToSqr(target) < SWEEP_RANGE * SWEEP_RANGE && this.random.nextInt(15) == 0) {
            this.sweepTimer = SWEEP_WINDUP;
            level.broadcastEntityEvent(this, EVENT_SWEEP);
            this.playSound(ModSounds.LINDWORM_AMBIENT.get(), 1.2f, 0.7f);
        }
        if (this.sweepTimer > 0 && --this.sweepTimer == 0) {
            this.tailSweep(level);
            this.sweepCooldown = 100 + this.random.nextInt(40);
        }
    }

    /** Schwanzhieb: verletzt und schleudert alles in der Nähe weg. */
    private void tailSweep(ServerLevel level) {
        this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.4f, 0.6f);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY(0.4), this.getZ(), 6, 1.6, 0.2, 1.6, 0.0);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(SWEEP_RANGE, 1.0, SWEEP_RANGE),
                entity -> entity != this && !(entity instanceof LindwormEntity) && entity.isAlive())) {
            DamageSource source = this.damageSources().mobAttack(this);
            if (victim.hurtServer(level, source, 6.0f)) {
                Vec3 away = victim.position().subtract(this.position()).multiply(1.0, 0.0, 1.0).normalize();
                victim.knockback(1.2, -away.x, -away.z, source, 6.0f);
                victim.push(0.0, 0.3, 0.0);
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_BITE);
        this.playSound(ModSounds.LINDWORM_BITE.get(), 1.0f, this.getVoicePitch());
        return super.doHurtTarget(level, target);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_BITE) {
            this.biteAnimation.start(this.tickCount);
        } else if (id == EVENT_SWEEP) {
            this.sweepAnimation.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    /** Auch bei Tageslicht, aber nur unter freiem Himmel, selten und nie zwei nahe beieinander. */
    public static boolean checkLindwormSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        if (!Monster.checkAnyLightMonsterSpawnRules(type, level, reason, pos, random)) {
            return false;
        }
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        return random.nextInt(6) == 0 && level.canSeeSky(pos)
                && level.getEntitiesOfClass(LindwormEntity.class, new AABB(pos).inflate(64.0)).isEmpty();
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.LINDWORM_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.LINDWORM_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.LINDWORM_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.LINDWORM_SLITHER.get(), 0.35f, 1.0f);
    }

    @Override
    public float getVoicePitch() {
        return 0.8f + (this.random.nextFloat() - 0.5f) * 0.1f;
    }
}
