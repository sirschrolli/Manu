package io.github.sirschrolli.kingdomsatwar.entity.shadow;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Schattenpirscher: ein hagerer Schatten mit langen Krallen. Solange sein Opfer ihn nicht ansieht, gleitet er schnell
 * und fast unsichtbar heran; sobald man ihn anblickt, wird er sichtbar und zögert. Seine Hiebe hüllen das Opfer kurz
 * in Finsternis. Er meidet helles Fackellicht.
 */
public class ShadeStalkerEntity extends Monster {
    public static final byte EVENT_ATTACK = 124;
    private static final EntityDataAccessor<Boolean> DATA_WATCHED =
            SynchedEntityData.defineId(ShadeStalkerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final Identifier STALK_SPEED = KingdomsAtWar.id("stalk_speed");

    public final AnimationState attackAnimation = new AnimationState();
    /** Sichtbarkeit (0 = fast unsichtbar, 1 = deutlich), weich nachgeführt – nur auf dem Client. */
    public float visibility = 0.55f;
    public float visibilityO = 0.55f;

    public ShadeStalkerEntity(EntityType<? extends ShadeStalkerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 36.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WATCHED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SoldierEntity.class, true));
    }

    /** Sieht das Ziel gerade her? */
    public boolean isWatched() {
        return this.entityData.get(DATA_WATCHED);
    }

    /** Blickt der Spieler (ungefähr) auf den Pirscher und sieht ihn auch? */
    public boolean isLookedAtBy(LivingEntity viewer) {
        // Spieler: Blickrichtung der Kamera; andere: Kopfrichtung
        Vec3 view = (viewer instanceof Player ? viewer.getLookAngle() : viewer.getViewVector(1.0f)).normalize();
        Vec3 dir = new Vec3(this.getX() - viewer.getX(), this.getY(0.6) - viewer.getEyeY(), this.getZ() - viewer.getZ());
        double distance = dir.length();
        if (distance < 1.0e-3) {
            return true;
        }
        return view.dot(dir.scale(1.0 / distance)) > 0.86 && viewer.hasLineOfSight(this);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            this.visibilityO = this.visibility;
            // angesehen: deutlich · auf der Jagd: fast unsichtbar · sonst: halb durchscheinend
            float wanted = this.isWatched() ? 1.0f : this.isAggressive() ? 0.16f : 0.55f;
            this.visibility += (wanted - this.visibility) * (wanted > this.visibility ? 0.25f : 0.08f);
            if (this.random.nextInt(6) == 0) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getRandomX(0.6), this.getRandomY(), this.getRandomZ(0.6), 0.0, 0.02, 0.0);
            }
            return;
        }
        if (this.tickCount % 4 != 0) {
            return;
        }
        LivingEntity target = this.getTarget();
        boolean watched = target != null && this.isLookedAtBy(target);
        if (watched != this.isWatched()) {
            this.entityData.set(DATA_WATCHED, watched);
        }
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(STALK_SPEED);
            if (target != null) {
                // unbeobachtet etwas schneller als ein rennender Spieler, angesehen nur ein Schleichen
                double bonus = watched ? -0.55 : 0.45;
                speed.addTransientModifier(new AttributeModifier(STALK_SPEED, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_ATTACK);
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 70, 0), this);
            level.sendParticles(ParticleTypes.SQUID_INK, living.getX(), living.getY(0.6), living.getZ(), 8, 0.3, 0.4, 0.3, 0.02);
        }
        return hit;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ATTACK) {
            this.attackAnimation.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    /** Erscheint nur, wo kein helles Kunstlicht brennt (das Zwielicht selbst stört ihn nicht). */
    public static boolean checkStalkerSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && level.getBrightness(LightLayer.BLOCK, pos) < 7
                && checkMobSpawnRules(type, level, reason, pos, random);
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // lautlos
    }

    @Override
    public float getVoicePitch() {
        return 0.55f + (this.random.nextFloat() - 0.5f) * 0.1f;
    }
}
