package io.github.sirschrolli.kingdomsatwar.entity.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Werwolf: streift nachts durch die Wälder der Oberwelt, bei Vollmond besonders oft. Er geht gebeugt auf zwei Beinen –
 * jagt er aber, fällt er auf alle viere und rennt schneller als ein Mensch. Erblickt er nachts sein Opfer, reckt er den
 * Kopf und heult (danach ist er schneller und stärker); aus einigen Blöcken Entfernung springt er es an, aus der Nähe
 * zerfetzt er es mit den Krallen. Feuer fürchtet er (brennend flieht er), Mondsilber trifft ihn besonders hart (er gehört
 * zu den Werwesen, siehe MoonsilverBladeItem), und im Tageslicht wird er matt.
 */
public class WerewolfEntity extends Monster {
    public static final byte EVENT_SWIPE = 70;
    public static final byte EVENT_HOWL = 71;
    public static final byte EVENT_POUNCE = 72;
    /** So lange (Ticks) heult er. */
    public static final int HOWL_TICKS = 34;
    private static final Identifier RUN_SPEED = KingdomsAtWar.id("werewolf_run");
    private static final EntityDataAccessor<Boolean> DATA_RUNNING = SynchedEntityData.defineId(WerewolfEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState swipeAnimation = new AnimationState();
    public final AnimationState howlAnimation = new AnimationState();
    public final AnimationState pounceAnimation = new AnimationState();
    private int howlTicks;
    private int howlCooldown = 100;
    private int pounceCooldown = 40;
    private @Nullable LivingEntity lastTarget;

    public WerewolfEntity(EntityType<? extends WerewolfEntity> type, Level level) {
        super(type, level);
        this.xpReward = 14;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.6)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_RUNNING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // brennend flieht er (Feuer fürchtet er), sonst jagt er
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5, DamageTypeTags.IS_FIRE));
        this.goalSelector.addGoal(2, new HowlGoal(this));
        this.goalSelector.addGoal(3, new PounceGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.75));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SoldierEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity.class, false));
    }

    /** Rennt er gerade auf allen vieren (jagend)? */
    public boolean isRunning() {
        return this.entityData.get(DATA_RUNNING);
    }

    /** Heult er gerade? */
    public boolean isHowling() {
        return this.howlTicks > 0;
    }

    /** Lässt ihn sofort heulen (für Tests und die Vorführung). */
    public void forceHowl() {
        this.startHowl();
    }

    /** Für die Vorführung: auf allen vieren, auch ohne Jagd. */
    public void forceRunning(boolean running) {
        this.entityData.set(DATA_RUNNING, running);
    }

    /** Lässt ihn sofort anspringen (für Tests und die Vorführung). */
    public void forcePounce(LivingEntity target) {
        this.pounceAt(target);
    }

    private boolean night() {
        return this.level().isDarkOutside();
    }

    private boolean inSunlight() {
        Level level = this.level();
        if (!level.environmentAttributes().getValue(EnvironmentAttributes.MONSTERS_BURN, this.position()) || this.isInWaterOrRain()) {
            return false;
        }
        return level.canSeeSky(BlockPos.containing(this.getX(), this.getEyeY(), this.getZ()));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.howlTicks > 0) {
            this.howlTicks--;
            this.getNavigation().stop();
            if (this.howlTicks == HOWL_TICKS - 6) {
                this.playSound(ModSounds.WEREWOLF_HOWL.get(), 3.0f, 0.9f + this.random.nextFloat() * 0.15f);
            }
            if (this.howlTicks == 0) {
                // nach dem Heulen: schneller und stärker
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 240, 0));
                this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 240, 0));
            }
        }
        if (this.howlCooldown > 0) {
            this.howlCooldown--;
        }
        if (this.pounceCooldown > 0) {
            this.pounceCooldown--;
        }
        LivingEntity target = this.getTarget();
        if (target != this.lastTarget) {
            this.lastTarget = target;
        }
        // auf allen vieren, wenn er jagt und das Ziel noch ein Stück entfernt ist
        boolean running = target != null && target.isAlive() && this.distanceToSqr(target) > 9.0 && this.howlTicks == 0
                && this.getDeltaMovement().horizontalDistanceSqr() > 0.004;
        if (running != this.isRunning()) {
            this.entityData.set(DATA_RUNNING, running);
            AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.removeModifier(RUN_SPEED);
                if (running) {
                    speed.addTransientModifier(new AttributeModifier(RUN_SPEED, 0.45, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
            }
        }
        // im Tageslicht wird er matt
        if (this.tickCount % 40 == 0 && !this.isNoAi() && this.inSunlight()) {
            this.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, false));
            this.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0, true, false));
        }
    }

    private void startHowl() {
        this.howlTicks = HOWL_TICKS;
        this.howlCooldown = 600 + this.random.nextInt(400);
        this.level().broadcastEntityEvent(this, EVENT_HOWL);
    }

    private void pounceAt(LivingEntity target) {
        Vec3 to = target.position().subtract(this.position());
        Vec3 flat = new Vec3(to.x, 0.0, to.z);
        double d = flat.length();
        Vec3 dir = d < 1.0e-3 ? Vec3.ZERO : flat.scale(1.0 / d);
        this.setDeltaMovement(dir.scale(Math.min(1.25, 0.35 + d * 0.13)).add(0.0, 0.48 + Math.max(0.0, to.y) * 0.08, 0.0));
        this.needsSync = true;
        this.lookAt(target, 360.0f, 30.0f);
        this.pounceCooldown = 70 + this.random.nextInt(40);
        this.level().broadcastEntityEvent(this, EVENT_POUNCE);
        this.playSound(ModSounds.WEREWOLF_SNARL.get(), 1.6f, 1.1f);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_SWIPE);
        this.playSound(ModSounds.WEREWOLF_SNARL.get(), 1.2f, 0.9f + this.random.nextFloat() * 0.2f);
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, living.getX(), living.getY(0.6), living.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
        return hit;
    }

    @Override
    public void tick() {
        super.tick();
        // beim Ansprung: wen er im Flug berührt, den reißt er um
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel level && !this.onGround() && this.pounceCooldown > 55
                && this.getTarget() != null && this.getTarget().isAlive() && this.getBoundingBox().inflate(0.4).intersects(this.getTarget().getBoundingBox())) {
            LivingEntity target = this.getTarget();
            if (target.hurtServer(level, this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) + 2.0f)) {
                Vec3 push = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(0.9);
                target.push(push.x, 0.35, push.z);
                target.needsSync = true;
                level.broadcastEntityEvent(this, EVENT_SWIPE);
            }
            this.pounceCooldown = 55;
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_SWIPE -> this.swipeAnimation.start(this.tickCount);
            case EVENT_HOWL -> {
                this.howlAnimation.start(this.tickCount);
                this.howlTicks = HOWL_TICKS;
            }
            case EVENT_POUNCE -> this.pounceAnimation.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.howlTicks > 0) {
            this.howlTicks--;
            if (this.random.nextInt(6) == 0) {
                // sein Atem steigt in der kalten Nachtluft auf
                Vec3 look = this.getViewVector(1.0f);
                this.level().addParticle(ParticleTypes.WHITE_SMOKE, this.getX() + look.x * 1.1, this.getEyeY() + 0.7, this.getZ() + look.z * 1.1,
                        look.x * 0.01, 0.03, look.z * 0.01);
            }
        }
    }

    /**
     * Nachts im Wald, auf freiem Himmel; bei Vollmond kommt jeder, sonst nur jeder zweite – und nie zwei Werwölfe dicht
     * beieinander.
     */
    public static boolean checkWerewolfSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        if (!Monster.checkMonsterSpawnRules(type, level, reason, pos, random)) {
            return false;
        }
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        Level world = level.getLevel();
        if (!world.isDarkOutside() || !level.canSeeSky(pos)) {
            return false;
        }
        MoonPhase moon = world.environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, Vec3.atCenterOf(pos));
        if (moon != MoonPhase.FULL_MOON && random.nextBoolean()) {
            return false;
        }
        return level.getEntitiesOfClass(WerewolfEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(40.0)).isEmpty();
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.WEREWOLF_GROWL.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WEREWOLF_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WEREWOLF_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.WEREWOLF_STEP.get(), this.isRunning() ? 0.5f : 0.35f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    // ------------------------------------------------------------------ Ziele

    /** Erblickt er nachts ein neues Opfer, bleibt er stehen und heult (nicht zu oft). */
    private static final class HowlGoal extends Goal {
        private final WerewolfEntity wolf;

        HowlGoal(WerewolfEntity wolf) {
            this.wolf = wolf;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.wolf.getTarget();
            return this.wolf.howlTicks == 0 && this.wolf.howlCooldown == 0 && target != null && target.isAlive() && this.wolf.night()
                    && this.wolf.distanceToSqr(target) > 36.0 && this.wolf.distanceToSqr(target) < 24.0 * 24.0 && this.wolf.onGround();
        }

        @Override
        public void start() {
            this.wolf.startHowl();
        }

        @Override
        public boolean canContinueToUse() {
            return this.wolf.howlTicks > 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.wolf.getTarget();
            if (target != null) {
                this.wolf.getLookControl().setLookAt(target.getX(), target.getEyeY() + 4.0, target.getZ());
            }
        }
    }

    /** Aus vier bis neun Blöcken springt er sein Opfer an. */
    private static final class PounceGoal extends Goal {
        private final WerewolfEntity wolf;

        PounceGoal(WerewolfEntity wolf) {
            this.wolf = wolf;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.wolf.getTarget();
            if (target == null || !target.isAlive() || this.wolf.pounceCooldown > 0 || !this.wolf.onGround() || this.wolf.howlTicks > 0) {
                return false;
            }
            double d = this.wolf.distanceToSqr(target);
            return d > 16.0 && d < 81.0 && Math.abs(target.getY() - this.wolf.getY()) < 3.0 && this.wolf.hasLineOfSight(target)
                    && this.wolf.random.nextInt(4) == 0;
        }

        @Override
        public void start() {
            LivingEntity target = this.wolf.getTarget();
            if (target != null) {
                this.wolf.getNavigation().stop();
                this.wolf.pounceAt(target);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

}
