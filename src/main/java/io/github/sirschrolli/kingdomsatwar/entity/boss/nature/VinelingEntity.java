package io.github.sirschrolli.kingdomsatwar.entity.boss.nature;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rankling: ein Diener des Hains, den Verdantis aus dem Boden ruft. Wächst in einer Sekunde aus der Erde, jagt
 * Spieler und Soldaten, schnappt mit seiner Knospe zu (die Dornen verlangsamen) und verwelkt nach einer halben Minute
 * wieder zu Laub. Wird nicht gespeichert.
 */
public class VinelingEntity extends Monster {
    public static final byte EVENT_SNAP = 61;
    public static final int SPROUT_TICKS = 20;
    private static final int LIFETIME = 600;

    public final AnimationState snapAnimation = new AnimationState();
    private int age;

    public VinelingEntity(EntityType<? extends VinelingEntity> type, Level level) {
        super(type, level);
        this.xpReward = 2;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.31)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, VinelingEntity.class, DragonBossEntity.class, io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SoldierEntity.class, true));
    }

    /** Wie weit der Rankling schon aus dem Boden gewachsen ist (0..1). */
    public float sprout(float partialTicks) {
        return Math.min(1.0f, (this.tickCount + partialTicks) / SPROUT_TICKS);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        this.age++;
        if (this.age <= SPROUT_TICKS) {
            // wächst aus dem Boden: steht still, Erdbrocken fliegen
            this.getNavigation().stop();
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
            if (this.age % 3 == 0) {
                BlockState ground = level.getBlockState(this.blockPosition().below());
                if (!ground.isAir()) {
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), this.getX(), this.getY() + 0.1, this.getZ(),
                            6, 0.3, 0.1, 0.3, 0.1);
                }
            }
        }
        if (this.age > LIFETIME) {
            this.wither(level);
        }
    }

    /** Verwelkt zu Laub (ohne Beute). */
    private void wither(ServerLevel level) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AZALEA_LEAVES.defaultBlockState()), this.getX(),
                this.getY() + 0.8, this.getZ(), 30, 0.3, 0.5, 0.3, 0.1);
        level.sendParticles(ParticleTypes.COMPOSTER, this.getX(), this.getY() + 0.8, this.getZ(), 10, 0.3, 0.5, 0.3, 0.02);
        this.playSound(SoundEvents.AZALEA_LEAVES_BREAK, 1.0f, 0.8f);
        this.discard();
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_SNAP);
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(BossEffects.debuff(MobEffects.SLOWNESS, 40, 1));
        }
        this.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0f, 0.6f);
        return hit;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_SNAP) {
            this.snapAnimation.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected boolean considersEntityAsAlly(Entity entity) {
        return entity instanceof VinelingEntity || entity instanceof DragonBossEntity || super.considersEntityAsAlly(entity);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof DragonBossEntity) && !(target instanceof VinelingEntity) && super.canAttack(target);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CAVE_VINES_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AZALEA_LEAVES_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SWEET_BERRY_BUSH_BREAK;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.MOSS_STEP, 0.4f, 1.2f);
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return true;
    }
}
