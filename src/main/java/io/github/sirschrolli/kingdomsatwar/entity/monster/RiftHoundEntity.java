package io.github.sirschrolli.kingdomsatwar.entity.monster;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Rissjäger: hagere Jagdhunde der äußeren End-Inseln, die zu zweit jagen. Ist ihr Opfer ein Stück entfernt, zerfallen
 * sie in violette Funken und treten hinter ihm wieder aus der Leere (Phasensprung) – und beißen sofort zu; ihr Biss
 * verlangsamt. Wer sie mit Pfeilen beschießt, dem weichen sie oft auf dieselbe Weise aus. Zu Beginn der Jagd heult einer:
 * Den Spielern in der Nähe wird es schwarz vor Augen, das Rudel wird schneller.
 */
public class RiftHoundEntity extends Monster {
    public static final byte EVENT_BITE = 90;
    public static final byte EVENT_PHASE_OUT = 91;
    public static final byte EVENT_PHASE_IN = 92;
    public static final byte EVENT_HOWL = 93;
    /** So lange zerfällt er, bevor er hinter seinem Opfer erscheint. */
    public static final int PHASE_TICKS = 10;
    public static final int HOWL_TICKS = 32;
    /** Clientseitig: so lange taucht er nach dem Phasensprung aus dem Riss wieder auf. */
    private static final int APPEAR_TICKS = 5;

    public final AnimationState biteAnimation = new AnimationState();
    public final AnimationState phaseAnimation = new AnimationState();
    public final AnimationState howlAnimation = new AnimationState();
    private int phaseTicks;
    private int phaseCooldown = 60;
    private int howlTicks;
    private int howlCooldown = 40;
    private @Nullable LivingEntity lastTarget;
    /** Clientseitig: so lange nach dem Zerfall unsichtbar (bis er wieder erscheint). */
    private int fadedTicks;
    private int appearTicks;

    public RiftHoundEntity(EntityType<? extends RiftHoundEntity> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 28.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new HowlGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.25, true) {
            @Override
            public boolean canUse() {
                return RiftHoundEntity.this.phaseTicks == 0 && super.canUse();
            }
        });
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 14.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SoldierEntity.class, true));
    }

    /** Zerfällt er gerade (Phasensprung)? */
    public boolean isPhasing() {
        return this.phaseTicks > 0;
    }

    /** Für Tests und die Vorführung: sofort einen Phasensprung hinter das Ziel beginnen. */
    public void forcePhase() {
        this.startPhase();
    }

    /** Für Tests und die Vorführung: sofort heulen. */
    public void forceHowl() {
        this.startHowl();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.phaseCooldown > 0) {
            this.phaseCooldown--;
        }
        if (this.howlCooldown > 0) {
            this.howlCooldown--;
        }
        LivingEntity target = this.getTarget();
        if (this.howlTicks > 0) {
            this.howlTicks--;
            this.getNavigation().stop();
            if (this.howlTicks == HOWL_TICKS - 8) {
                this.howlEffect(level);
            }
        }
        if (this.phaseTicks > 0) {
            this.getNavigation().stop();
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
            if (--this.phaseTicks == 0) {
                this.phaseIn(level, target);
            }
            return;
        }
        if (target == null || !target.isAlive() || this.isNoAi() || this.howlTicks > 0) {
            return;
        }
        double d = this.distanceTo(target);
        if (d >= 6.0 && d <= 20.0 && this.phaseCooldown == 0 && this.random.nextInt(8) == 0) {
            this.startPhase();
        }
    }

    private void startPhase() {
        this.phaseTicks = PHASE_TICKS;
        this.phaseCooldown = 110 + this.random.nextInt(50);
        this.level().broadcastEntityEvent(this, EVENT_PHASE_OUT);
        this.playSound(ModSounds.RIFT_HOUND_PHASE.get(), 1.6f, 1.0f);
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.5), this.getZ(), 40, 0.4, 0.5, 0.4, 0.15);
        }
    }

    /** Tritt hinter dem Opfer aus der Leere (sicherer Boden, sonst seitlich, sonst wo er war) und beißt sofort zu. */
    private void phaseIn(ServerLevel level, @Nullable LivingEntity target) {
        Vec3 from = this.position();
        boolean moved = false;
        if (target != null && target.isAlive()) {
            Vec3 look = target.getViewVector(1.0f).multiply(1.0, 0.0, 1.0);
            Vec3 back = look.lengthSqr() < 1.0e-4 ? new Vec3(0.0, 0.0, 1.0) : look.normalize().scale(-1.0);
            for (int i = 0; i < 8 && !moved; i++) {
                double a = (i % 2 == 0 ? 1 : -1) * (i / 2) * 0.6;
                Vec3 dir = new Vec3(back.x * Math.cos(a) - back.z * Math.sin(a), 0.0, back.x * Math.sin(a) + back.z * Math.cos(a));
                Vec3 at = target.position().add(dir.scale(2.2));
                moved = this.randomTeleport(at.x, at.y + 1.0, at.z, false, state -> false);
            }
            if (moved) {
                this.lookAt(target, 360.0f, 60.0f);
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.getYRot();
            }
        }
        level.broadcastEntityEvent(this, EVENT_PHASE_IN);
        this.playSound(ModSounds.RIFT_HOUND_PHASE.get(), 1.6f, 0.7f);
        level.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY(0.5), this.getZ(), 40, 0.4, 0.5, 0.4, 0.4);
        if (moved) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, from.x, from.y + 0.6, from.z, 12, 0.3, 0.4, 0.3, 0.05);
        }
        if (moved && target != null && this.distanceTo(target) < 3.2) {
            this.doHurtTarget(level, target);
        }
    }

    private void startHowl() {
        this.howlTicks = HOWL_TICKS;
        this.howlCooldown = 500 + this.random.nextInt(300);
        this.level().broadcastEntityEvent(this, EVENT_HOWL);
        this.playSound(ModSounds.RIFT_HOUND_HOWL.get(), 3.0f, 0.9f + this.random.nextFloat() * 0.15f);
    }

    /** Das Heulen der Leere: Dunkelheit für die Spieler ringsum, Schnelligkeit für das Rudel. */
    private void howlEffect(ServerLevel level) {
        AABB area = this.getBoundingBox().inflate(16.0);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, area, p -> !p.isCreative() && !p.isSpectator())) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 70, 0), this);
        }
        for (RiftHoundEntity mate : level.getEntitiesOfClass(RiftHoundEntity.class, area)) {
            mate.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 0));
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.8), this.getZ(), 30, 0.6, 0.4, 0.6, 0.2);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_BITE);
        this.playSound(ModSounds.RIFT_HOUND_BITE.get(), 1.3f, 0.9f + this.random.nextFloat() * 0.2f);
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0), this);
        }
        return hit;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        // Geschossen weicht er oft durch die Leere aus
        if (source.is(DamageTypeTags.IS_PROJECTILE) && this.phaseTicks == 0 && this.phaseCooldown < 60 && this.random.nextInt(2) == 0
                && this.getTarget() != null) {
            this.startPhase();
            return false;
        }
        if (this.phaseTicks > 0) {
            return false;                                                   // zwischen den Welten trifft ihn nichts
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_BITE -> this.biteAnimation.start(this.tickCount);
            case EVENT_PHASE_OUT -> {
                this.phaseAnimation.start(this.tickCount);
                this.fadedTicks = PHASE_TICKS;
            }
            case EVENT_PHASE_IN -> {
                this.phaseAnimation.stop();
                this.fadedTicks = 0;
                this.appearTicks = APPEAR_TICKS;
            }
            case EVENT_HOWL -> this.howlAnimation.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    /** Clientseitig: 0 ganz da … 1 fort – er versinkt im Riss und taucht danach rasch wieder daraus auf. */
    public float fade(float partialTicks) {
        if (this.fadedTicks > 0) {
            return Math.min(1.0f, (PHASE_TICKS - this.fadedTicks + partialTicks) / 8.0f);
        }
        if (this.appearTicks > 0) {
            return Math.max(0.0f, (this.appearTicks - partialTicks) / APPEAR_TICKS);
        }
        return 0.0f;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Level level = this.level();
        if (level.isClientSide()) {
            if (this.appearTicks > 0) {
                this.appearTicks--;
            }
            if (this.fadedTicks > 0) {
                this.fadedTicks--;
                level.addParticle(ParticleTypes.REVERSE_PORTAL, this.getRandomX(0.6), this.getRandomY(), this.getRandomZ(0.6), 0.0, 0.05, 0.0);
            } else if (this.random.nextInt(10) == 0) {
                level.addParticle(ParticleTypes.PORTAL, this.getRandomX(0.5), this.getY() + 0.6 + this.random.nextDouble() * 0.6,
                        this.getRandomZ(0.5), (this.random.nextDouble() - 0.5) * 0.3, 0.0, (this.random.nextDouble() - 0.5) * 0.3);
            }
        }
        if (this.lastTarget != this.getTarget()) {
            this.lastTarget = this.getTarget();
        }
    }

    /** Auf den äußeren End-Inseln, zu zweit; nie mehr als vier auf vierzig Blöcke. */
    public static boolean checkHoundSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL || !checkMobSpawnRules(type, level, reason, pos, random)) {
            return false;
        }
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        return level.getEntitiesOfClass(RiftHoundEntity.class, new AABB(pos).inflate(40.0)).size() < 4;
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.RIFT_HOUND_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.RIFT_HOUND_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.RIFT_HOUND_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.RIFT_HOUND_STEP.get(), 0.3f, 1.0f + this.random.nextFloat() * 0.2f);
    }

    // ------------------------------------------------------------------ Ziele

    /** Zu Beginn einer Jagd heult er (nicht zu oft). */
    private static final class HowlGoal extends Goal {
        private final RiftHoundEntity hound;

        HowlGoal(RiftHoundEntity hound) {
            this.hound = hound;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.hound.getTarget();
            return this.hound.howlCooldown == 0 && this.hound.phaseTicks == 0 && target != null && target.isAlive()
                    && this.hound.distanceToSqr(target) > 25.0 && this.hound.onGround();
        }

        @Override
        public void start() {
            this.hound.startHowl();
        }

        @Override
        public boolean canContinueToUse() {
            return this.hound.howlTicks > 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.hound.getTarget();
            if (target != null) {
                this.hound.getLookControl().setLookAt(target.getX(), target.getEyeY() + 5.0, target.getZ());
            }
        }
    }
}
