package io.github.sirschrolli.kingdomsatwar.entity.voidrealm;

import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Ein Sternspinnling (seit 0.99): die Brut der Sternenweberin – eine Spinne kaum größer als eine Katze, nachtblau mit einem
 * Funken Sternenlicht auf dem Rücken. Er springt seine Beute an und beißt (macht langsam); nach einer halben Minute zerfällt er
 * zu Sternenstaub, ebenso, wenn die Weberin fällt. Ihr Netz hält ihn nicht.
 */
public class StarSpiderlingEntity extends Monster {
    /** So lange lebt ein Spinnling (Ticks). */
    public static final int LIFETIME = 600;

    public StarSpiderlingEntity(EntityType<? extends StarSpiderlingEntity> type, Level level) {
        super(type, level);
        this.xpReward = 1;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.42f));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.15, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, StarWeaverEntity.class, StarSpiderlingEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level && this.tickCount > LIFETIME && this.isAlive()) {
            this.vanish();
        } else if (this.level().isClientSide() && this.random.nextInt(6) == 0) {
            this.level().addParticle(new DustParticleOptions(0xB8D4FF, 0.6f), this.getRandomX(0.6), this.getY(0.6), this.getRandomZ(0.6), 0.0, 0.0,
                    0.0);
        }
    }

    /** Er zerfällt zu Sternenstaub (ohne Beute). */
    public void vanish() {
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(new DustParticleOptions(0xB8D4FF, 1.0f), this.getX(), this.getY(0.5), this.getZ(), 14, 0.3, 0.2, 0.3, 0.0);
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.5), this.getZ(), 4, 0.2, 0.2, 0.2, 0.03);
        }
        this.discard();
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0), this);
        }
        return hit;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() instanceof StarWeaverEntity || source.getEntity() instanceof StarSpiderlingEntity) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 speedMultiplier) {
        // das Netz der Weberin hält ihre Brut nicht
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.STAR_WEAVER_SPIDERLING.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15f, 1.6f);
    }

    @Override
    public float getVoicePitch() {
        return 1.5f + (this.random.nextFloat() - 0.5f) * 0.2f;
    }
}
