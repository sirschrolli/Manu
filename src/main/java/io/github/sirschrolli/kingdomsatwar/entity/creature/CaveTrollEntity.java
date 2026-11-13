package io.github.sirschrolli.kingdomsatwar.entity.creature;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
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
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * Höhlentroll: haust tief unter der Erde. Groß, langsam und gewaltig stark – ein Hieb mit der Keule schleudert
 * selbst Ritter durch die Luft. Sonnenlicht lässt Trolle zu Stein erstarren: Im Freien werden sie langsam,
 * nehmen Schaden und suchen eilig den Schatten.
 */
public class CaveTrollEntity extends Monster {
    public static final byte EVENT_SMASH = 122;
    private static final EntityDataAccessor<Boolean> DATA_PETRIFYING =
            SynchedEntityData.defineId(CaveTrollEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState smashAnimation = new AnimationState();

    public CaveTrollEntity(EntityType<? extends CaveTrollEntity> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 70.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.8)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PETRIFYING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SeekShadeGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SoldierEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    /** Steht gerade im Sonnenlicht und erstarrt zu Stein. */
    public boolean isPetrifying() {
        return this.entityData.get(DATA_PETRIFYING);
    }

    private boolean inSunlight() {
        Level level = this.level();
        if (!level.environmentAttributes().getValue(EnvironmentAttributes.MONSTERS_BURN, this.position())
                || this.isInWaterOrRain() || this.getLightLevelDependentMagicValue() <= 0.5f) {
            return false;
        }
        return level.canSeeSky(BlockPos.containing(this.getX(), this.getEyeY(), this.getZ()));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level) || !this.isAlive()) {
            return;
        }
        boolean sun = !this.isNoAi() && this.inSunlight();       // Statuen (ohne KI) erstarren nicht
        if (sun != this.isPetrifying()) {
            this.entityData.set(DATA_PETRIFYING, sun);
        }
        if (sun && this.tickCount % 20 == 0) {
            this.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2, true, false));
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                    this.getX(), this.getY(0.6), this.getZ(), 12, 0.5, 0.8, 0.5, 0.05);
            this.playSound(ModSounds.TROLL_PETRIFY.get(), 0.8f, 0.7f + this.random.nextFloat() * 0.2f);
            this.hurtServer(level, level.damageSources().generic(), 3.0f);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_SMASH);
        this.playSound(ModSounds.TROLL_SMASH.get(), 1.2f, 0.8f + this.random.nextFloat() * 0.2f);
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            living.push(0.0, 0.45, 0.0);
            BlockState ground = level.getBlockState(living.blockPosition().below());
            if (!ground.isAir()) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), living.getX(), living.getY(), living.getZ(),
                        24, 0.6, 0.1, 0.6, 0.15);
            }
        }
        return hit;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_SMASH) {
            this.smashAnimation.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    /** Tief unter der Erde, im Dunkeln und ohne Himmel über sich – und nie zwei Trolle nahe beieinander. */
    public static boolean checkTrollSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        if (!Monster.checkMonsterSpawnRules(type, level, reason, pos, random)) {
            return false;
        }
        return EntitySpawnReason.isSpawner(reason) || (pos.getY() < 32 && !level.canSeeSky(pos)
                && level.getEntitiesOfClass(CaveTrollEntity.class, new AABB(pos).inflate(32.0)).isEmpty());
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.TROLL_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.TROLL_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TROLL_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.TROLL_STEP.get(), 0.7f, 0.8f);
    }

    @Override
    public float getVoicePitch() {
        return 0.85f + (this.random.nextFloat() - 0.5f) * 0.1f;
    }

    /** Sucht im Sonnenlicht eilig Schatten (auch mitten im Kampf). */
    private static final class SeekShadeGoal extends FleeSunGoal {
        private final CaveTrollEntity troll;

        SeekShadeGoal(CaveTrollEntity troll) {
            super(troll, 1.25);
            this.troll = troll;
        }

        @Override
        public boolean canUse() {
            return this.troll.isPetrifying() && this.setWantedPos();
        }
    }
}
