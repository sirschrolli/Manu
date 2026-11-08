package io.github.sirschrolli.kingdomsatwar.entity.sea;

import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Hai: ein schlanker Jäger der warmen und tiefen Meere. Er zieht dicht unter der Oberfläche seine Bahnen – oft sieht man
 * nur die Rückenflosse durchs Wasser schneiden. Wer im Wasser schwimmt, wird umkreist: erst in weiten Runden, dann stößt
 * er plötzlich zu, beißt und dreht wieder ab. Blut wittert er von Weitem (verletzte Schwimmer findet er auf doppelte
 * Entfernung). An Land kann er nicht atmen.
 */
public class SharkEntity extends WaterAnimal {
    private static final byte EVENT_BITE = 72;
    /** Ab dieser Entfernung (Blöcke) bemerkt er unverletzte Schwimmer; verletzte (unter halber Gesundheit) auf doppelte. */
    public static final double NOTICE = 14.0;

    public final AnimationState biteAnimation = new AnimationState();
    /** Nur auf dem Client: Phase des Schwanzschlags. */
    public float swim;
    public float swimO;

    public SharkEntity(EntityType<? extends SharkEntity> type, Level level) {
        super(type, level);
        this.xpReward = 8;
        this.moveControl = new SmoothSwimmingMoveControl<>(this, 85, 10, 0.02f, 0.1f, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 36.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 1.15)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new HuntGoal(this));
        this.goalSelector.addGoal(4, new CruiseGoal(this));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> this.isPrey(target, level)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractFish.class, 80, true, false,
                (target, level) -> target.isInWater()));
    }

    /** Schwimmer im Wasser – unverletzte bis {@link #NOTICE}, verletzte (Blutgeruch) auf doppelte Entfernung. */
    private boolean isPrey(LivingEntity target, ServerLevel level) {
        if (level.getDifficulty() == Difficulty.PEACEFUL || !target.isInWater()) {
            return false;
        }
        double range = target.getHealth() < target.getMaxHealth() * 0.5f ? NOTICE * 2.0 : NOTICE;
        return target.distanceToSqr(this) < range * range;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
        this.moveRelative(this.getSpeed(), input);
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
        if (this.getTarget() == null) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.swimO = this.swim;
            double speed = this.getDeltaMovement().length();
            this.swim += 0.12f + (float) Math.min(0.5, speed * 1.6);
            // die Rückenflosse zieht eine Spur über die Oberfläche
            if (this.isInWater() && speed > 0.05 && !this.level().getFluidState(BlockPos.containing(this.getX(), this.getY() + 1.15,
                    this.getZ())).isSource() && this.level().getBlockState(BlockPos.containing(this.getX(), this.getY() + 1.15, this.getZ())).isAir()
                    && this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.SPLASH, this.getX(), this.getY() + 1.0, this.getZ(), 0.0, 0.05, 0.0);
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit) {
            this.playSound(ModSounds.SHARK_BITE.get(), 1.2f, 0.9f + this.random.nextFloat() * 0.2f);
            level.sendParticles(ParticleTypes.BUBBLE, target.getX(), target.getY(0.5), target.getZ(), 12, 0.3, 0.3, 0.3, 0.1);
        }
        level.broadcastEntityEvent(this, EVENT_BITE);
        return hit;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_BITE) {
            this.biteAnimation.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    // ------------------------------------------------------------------ Laichen und Geräusche

    /** Im offenen Wasser wenigstens vier Blöcke unter dem Meeresspiegel. */
    public static boolean checkSharkSpawnRules(EntityType<? extends Mob> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        int sea = level.getSeaLevel();
        return pos.getY() < sea - 3 && pos.getY() > sea - 32 && SeaCreatures.openWater(level, pos) && SeaCreatures.isWater(level, pos.above(2));
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SHARK_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SHARK_DEATH.get();
    }

    @Override
    public int getMaxHeadXRot() {
        return 1;
    }

    @Override
    public int getMaxHeadYRot() {
        return 1;
    }

    // ------------------------------------------------------------------ Jagd

    /** Kreisen, Zustoßen, Abdrehen – und wieder von vorn. */
    static final class HuntGoal extends Goal {
        private enum Phase { CIRCLE, STRIKE, RETREAT }

        private final SharkEntity shark;
        private Phase phase = Phase.CIRCLE;
        private int phaseTicks;
        private double angle;
        private int dir = 1;
        private double radius;
        private int repath;

        HuntGoal(SharkEntity shark) {
            this.shark = shark;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.shark.getTarget();
            return target != null && target.isAlive() && target.isInWater() && this.shark.isInWater();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() && this.shark.distanceToSqr(this.shark.getTarget()) < 40.0 * 40.0;
        }

        @Override
        public void start() {
            LivingEntity target = this.shark.getTarget();
            this.angle = Math.atan2(this.shark.getZ() - target.getZ(), this.shark.getX() - target.getX());
            this.dir = this.shark.random.nextBoolean() ? 1 : -1;
            this.circle(target instanceof Player ? 60 + this.shark.random.nextInt(60) : 20);
        }

        private void circle(int ticks) {
            this.phase = Phase.CIRCLE;
            this.phaseTicks = ticks;
            this.radius = 4.5 + this.shark.random.nextDouble() * 2.5;
            this.repath = 0;
        }

        @Override
        public void stop() {
            this.shark.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.shark.getTarget();
            if (target == null) {
                return;
            }
            this.shark.getLookControl().setLookAt(target, 10.0f, 10.0f);
            switch (this.phase) {
                case CIRCLE -> {
                    this.angle += this.dir * 0.05;
                    if (--this.repath <= 0) {
                        double x = target.getX() + Math.cos(this.angle) * this.radius;
                        double z = target.getZ() + Math.sin(this.angle) * this.radius;
                        double y = target.getY() + 0.2;
                        BlockPos at = BlockPos.containing(x, y, z);
                        if (!SeaCreatures.isWater(this.shark.level(), at)) {
                            this.dir = -this.dir;              // Ufer oder Fels: andersherum kreisen
                            this.angle += this.dir * 0.3;
                        }
                        this.shark.getNavigation().moveTo(x, y, z, 1.0);
                        this.repath = 5;
                    }
                    if (--this.phaseTicks <= 0) {
                        this.phase = Phase.STRIKE;
                        this.phaseTicks = 60;
                        this.repath = 0;
                    }
                }
                case STRIKE -> {
                    if (--this.repath <= 0) {
                        this.shark.getNavigation().moveTo(target, 2.0);
                        this.repath = 4;
                    }
                    double reach = this.shark.getBbWidth() * 0.5 + target.getBbWidth() * 0.5 + 1.1;
                    if (this.shark.distanceToSqr(target) < reach * reach && this.shark.level() instanceof ServerLevel level) {
                        this.shark.doHurtTarget(level, target);
                        this.phase = Phase.RETREAT;
                        this.phaseTicks = 25;
                        this.repath = 0;
                    } else if (--this.phaseTicks <= 0) {
                        this.circle(40);
                    }
                }
                case RETREAT -> {
                    if (--this.repath <= 0) {
                        Vec3 away = this.shark.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
                        away = away.lengthSqr() < 1.0e-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
                        Vec3 to = this.shark.position().add(away.scale(7.0));
                        this.shark.getNavigation().moveTo(to.x, target.getY(), to.z, 1.4);
                        this.repath = 8;
                    }
                    if (--this.phaseTicks <= 0) {
                        this.angle = Math.atan2(this.shark.getZ() - target.getZ(), this.shark.getX() - target.getX());
                        this.circle(target instanceof Player ? 40 + this.shark.random.nextInt(50) : 15);
                    }
                }
            }
        }
    }

    /** Ohne Beute zieht er Bahnen – oft dicht unter der Oberfläche, sodass die Flosse herausragt. */
    static final class CruiseGoal extends Goal {
        private final SharkEntity shark;
        private int cooldown;

        CruiseGoal(SharkEntity shark) {
            this.shark = shark;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.shark.getTarget() != null || !this.shark.isInWater()) {
                return false;
            }
            if (this.cooldown > 0) {
                this.cooldown--;
                return false;
            }
            Vec3 to = this.pick();
            if (to == null) {
                return false;
            }
            this.shark.getNavigation().moveTo(to.x, to.y, to.z, 0.85);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.shark.getTarget() == null && !this.shark.getNavigation().isDone();
        }

        @Override
        public void stop() {
            this.cooldown = 20 + this.shark.random.nextInt(60);
        }

        private @Nullable Vec3 pick() {
            RandomSource random = this.shark.random;
            Level level = this.shark.level();
            for (int i = 0; i < 10; i++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double r = 8.0 + random.nextDouble() * 14.0;
                BlockPos pos = BlockPos.containing(this.shark.getX() + Math.cos(a) * r, this.shark.getY() + random.nextInt(9) - 4,
                        this.shark.getZ() + Math.sin(a) * r);
                if (!SeaCreatures.isWater(level, pos)) {
                    continue;
                }
                int surface = SeaCreatures.surfaceAbove(level, pos, 16);
                if (surface != Integer.MIN_VALUE && random.nextInt(10) < 4) {
                    // dicht unter der Oberfläche: die Flosse schneidet durchs Wasser
                    return new Vec3(pos.getX() + 0.5, surface - 1.05, pos.getZ() + 0.5);
                }
                if (SeaCreatures.openWater(level, pos)) {
                    return Vec3.atCenterOf(pos);
                }
            }
            return null;
        }
    }
}
