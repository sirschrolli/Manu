package io.github.sirschrolli.kingdomsatwar.entity.fae;

import io.github.sirschrolli.kingdomsatwar.fae.FaeBiomes;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.ai.goal.Goal;
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
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Dornwicht: der Hüter der Dornenwildnis – hager, über zwei Blöcke hoch, aus schwarzen Ranken und Rinde, mit einer
 * Dornenkrone und glühenden, violetten Augen. Er schlägt mit Dornenklauen zu (Gift und Langsamkeit) und schleudert aus
 * der Ferne Salven aus Dornen. Die Dornenhecken seiner Wildnis stechen ihn nicht. Er erscheint nur in der Dornenwildnis
 * – dort aber auch im Abendlicht.
 */
public class ThornWightEntity extends Monster {
    private static final byte EVENT_SLASH = 70;
    private static final byte EVENT_VOLLEY = 71;

    public final AnimationState slashAnimation = new AnimationState();
    public final AnimationState volleyAnimation = new AnimationState();

    public ThornWightEntity(EntityType<? extends ThornWightEntity> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.STEP_HEIGHT, 1.1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new VolleyGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.15, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_SLASH);
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0), this);
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0), this);
            this.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0f, 0.6f);
        }
        return hit;
    }

    /** Eine Salve aus drei Dornen, leicht gefächert, auf das Ziel. */
    void volley(ServerLevel level, LivingEntity target) {
        this.playSound(ModSounds.THORN_VOLLEY.get(), 1.0f, 0.9f + this.random.nextFloat() * 0.2f);
        for (int i = -1; i <= 1; i++) {
            ThornProjectile thorn = new ThornProjectile(level, this);
            Vec3 aim = target.getEyePosition().subtract(0.0, 0.4, 0.0).subtract(thorn.position());
            double flat = aim.horizontalDistance();
            Vec3 side = new Vec3(-aim.z, 0.0, aim.x).normalize().scale(i * 0.14 * flat / 10.0);
            thorn.shoot(aim.x + side.x, aim.y + flat * 0.12, aim.z + side.z, 1.5f, 3.0f);
            level.addFreshEntity(thorn);
        }
        level.sendParticles(ModParticles.FAE_SPARK.get(), this.getX(), this.getY(0.75), this.getZ(), 8, 0.4, 0.3, 0.4, 0.04);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_SLASH) {
            this.slashAnimation.start(this.tickCount);
        } else if (id == EVENT_VOLLEY) {
            this.volleyAnimation.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.random.nextInt(5) == 0) {
            this.level().addParticle(ModParticles.FAE_SPARK.get(), this.getRandomX(0.5), this.getY(0.55 + this.random.nextDouble() * 0.3),
                    this.getRandomZ(0.5), 0.0, 0.005, 0.0);
        }
    }

    /** Nur in der Dornenwildnis – dort auch im Abendlicht (nicht auf Friedlich). */
    public static boolean checkWightSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        if (EntitySpawnReason.isSpawner(reason)) {
            return level.getDifficulty() != Difficulty.PEACEFUL;
        }
        return level.getDifficulty() != Difficulty.PEACEFUL && level.getBiome(pos).is(FaeBiomes.THORNWILD) && random.nextInt(2) == 0
                && checkMobSpawnRules(type, level, reason, pos, random);
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.75f;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.CREAKING_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CREAKING_SWAY;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CREAKING_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.CREAKING_STEP, 0.4f, 0.7f);
    }

    /**
     * Dornensalve: steht das Ziel sechs bis zwanzig Blöcke entfernt in Sicht, bleibt der Wicht stehen, holt aus und
     * schleudert drei Dornen (dann eine Weile nicht mehr).
     */
    static final class VolleyGoal extends Goal {
        private final ThornWightEntity wight;
        private int cooldown = 40;
        private int windup;

        VolleyGoal(ThornWightEntity wight) {
            this.wight = wight;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }
            LivingEntity target = this.wight.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            double d = this.wight.distanceTo(target);
            return d > 6.0 && d < 20.0 && this.wight.getSensing().hasLineOfSight(target);
        }

        @Override
        public void start() {
            this.windup = 14;
            this.wight.getNavigation().stop();
            this.wight.level().broadcastEntityEvent(this.wight, EVENT_VOLLEY);
        }

        @Override
        public boolean canContinueToUse() {
            return this.windup > 0 && this.wight.getTarget() != null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.wight.getTarget();
            if (target == null) {
                return;
            }
            this.wight.getLookControl().setLookAt(target, 30.0f, 30.0f);
            if (--this.windup == 0 && this.wight.level() instanceof ServerLevel level) {
                this.wight.volley(level, target);
            }
        }

        @Override
        public void stop() {
            this.cooldown = 70 + this.wight.getRandom().nextInt(50);
        }
    }
}
