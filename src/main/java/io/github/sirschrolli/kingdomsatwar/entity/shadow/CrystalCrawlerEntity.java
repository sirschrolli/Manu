package io.github.sirschrolli.kingdomsatwar.entity.shadow;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
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
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;

/**
 * Kristallkriecher: ein flacher, sechsbeiniger Käfer aus den Kristallschluchten, der an Wänden hinaufklettert und
 * Beute anspringt. Auf seinem Rücken wachsen Nachtkristalle – wer ihn mit der Hand oder Klinge trifft, bekommt
 * manchmal einen Splitterhagel ab.
 */
public class CrystalCrawlerEntity extends Monster {
    public static final byte EVENT_BITE = 125;
    private static final EntityDataAccessor<Boolean> DATA_CLIMBING =
            SynchedEntityData.defineId(CrystalCrawlerEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState biteAnimation = new AnimationState();
    /** Wie weit er sich an einer Wand aufgerichtet hat (0..1, nur auf dem Client). */
    public float climbAmount;
    public float climbAmountO;

    public CrystalCrawlerEntity(EntityType<? extends CrystalCrawlerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CLIMBING, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.setClimbing(this.horizontalCollision);
        } else {
            this.climbAmountO = this.climbAmount;
            this.climbAmount += ((this.isClimbing() ? 1.0f : 0.0f) - this.climbAmount) * 0.3f;
        }
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing();
    }

    public boolean isClimbing() {
        return this.entityData.get(DATA_CLIMBING);
    }

    public void setClimbing(boolean climbing) {
        if (climbing != this.isClimbing()) {
            this.entityData.set(DATA_CLIMBING, climbing);
        }
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 speedMultiplier) {
        if (!state.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(state, speedMultiplier);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_BITE);
        return super.doHurtTarget(level, target);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && source.getDirectEntity() instanceof LivingEntity attacker && attacker.distanceToSqr(this) < 9.0
                && this.random.nextInt(4) == 0) {
            // Splitterhagel: die Rückenkristalle zerspringen ein wenig
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ShadowBlocks.NIGHT_CRYSTAL.get().defaultBlockState()),
                    this.getX(), this.getY(0.8), this.getZ(), 18, 0.4, 0.3, 0.4, 0.2);
            this.playSound(SoundEvents.AMETHYST_CLUSTER_BREAK, 1.0f, 1.2f);
            attacker.hurtServer(level, this.damageSources().thorns(this), 2.0f);
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_BITE) {
            this.biteAnimation.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    /** Unter der Erde, in Schluchten und Höhlen – nicht im hellen Kunstlicht. */
    public static boolean checkCrawlerSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && level.getBrightness(LightLayer.BLOCK, pos) < 7
                && (EntitySpawnReason.isSpawner(reason) || !level.canSeeSky(pos) || pos.getY() < 50)
                && checkMobSpawnRules(type, level, reason, pos, random);
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AMETHYST_BLOCK_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AMETHYST_BLOCK_BREAK;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15f, 1.3f);
    }

    @Override
    public float getVoicePitch() {
        return 0.7f + (this.random.nextFloat() - 0.5f) * 0.1f;
    }
}
