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
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * Anglerfisch: ein Schrecken der Tiefsee. Er lauert reglos in der Dunkelheit am Grund der tiefen Meere; man sieht nur
 * den blau-weiß leuchtenden Köder an seiner Angel, der sacht hin und her pendelt. Wer ihm zu nahe kommt, den schnappt er
 * blitzschnell mit seinem riesigen Maul – danach braucht er einen Augenblick, bis er wieder zuschlagen kann.
 * <p>
 * Sein Köder („Leuchtköder“) leuchtet auch nach seinem Tod weiter – daraus entsteht die Tiefseelaterne.
 */
public class AnglerfishEntity extends Monster {
    private static final byte EVENT_SNAP = 73;
    /** So nah muss die Beute sein, bevor er zuschnappt (Blöcke). */
    public static final double STRIKE_RANGE = 4.5;

    public final AnimationState snapAnimation = new AnimationState();
    /** Nur auf dem Client: Pendeln des Köders und Flossenschlag. */
    public float swim;
    public float swimO;

    public AnglerfishEntity(EntityType<? extends AnglerfishEntity> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.moveControl = new SmoothSwimmingMoveControl<>(this, 85, 8, 0.02f, 0.1f, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 12);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.85)
                .add(Attributes.FOLLOW_RANGE, 14.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SnapGoal(this));
        this.goalSelector.addGoal(6, new RandomSwimmingGoal(this, 0.5, 160));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> target.isInWater()));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
        this.moveRelative(this.getSpeed(), input);
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.88));
    }

    // ------------------------------------------------------------------ Atmen: nur im Wasser

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void baseTick() {
        int air = this.getAirSupply();
        super.baseTick();
        if (this.level() instanceof ServerLevel level && this.isAlive()) {
            if (this.isInWater()) {
                this.setAirSupply(300);
            } else {
                this.setAirSupply(air - 1);
                if (this.getAirSupply() == -20) {
                    this.setAirSupply(0);
                    this.hurtServer(level, this.damageSources().drown(), 2.0f);
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.swimO = this.swim;
            this.swim += 0.06f + (float) Math.min(0.4, this.getDeltaMovement().length() * 1.4);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit) {
            this.playSound(ModSounds.ANGLERFISH_BITE.get(), 1.1f, 0.85f + this.random.nextFloat() * 0.2f);
        }
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

    // ------------------------------------------------------------------ Laichen und Geräusche

    /** Tief und dunkel: im Wasser wenigstens 20 Blöcke unter dem Meeresspiegel, kaum Licht. */
    public static boolean checkAnglerSpawnRules(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        if (EntitySpawnReason.isSpawner(reason)) {
            return level.getDifficulty() != Difficulty.PEACEFUL;
        }
        return level.getDifficulty() != Difficulty.PEACEFUL && pos.getY() <= level.getSeaLevel() - 20
                && SeaCreatures.openWater(level, pos) && level.getRawBrightness(pos, 0) <= 5;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ANGLERFISH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ANGLERFISH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ANGLERFISH_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    public int getMaxHeadXRot() {
        return 1;
    }

    // ------------------------------------------------------------------ Lauern und Zuschnappen

    /** Er wartet, bis die Beute nah genug ist, wendet sich ihr langsam zu und schnappt dann blitzschnell zu. */
    static final class SnapGoal extends Goal {
        private final AnglerfishEntity fish;
        private int cooldown;
        private int lunge;

        SnapGoal(AnglerfishEntity fish) {
            this.fish = fish;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.fish.getTarget();
            return target != null && target.isAlive() && target.isInWater() && this.fish.isInWater();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void stop() {
            this.fish.getNavigation().stop();
            this.lunge = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.fish.getTarget();
            if (target == null) {
                return;
            }
            this.fish.getLookControl().setLookAt(target, 6.0f, 6.0f);
            if (this.cooldown > 0) {
                this.cooldown--;
            }
            double dist = this.fish.distanceTo(target);
            if (this.lunge > 0) {
                this.lunge--;
                double reach = this.fish.getBbWidth() * 0.5 + target.getBbWidth() * 0.5 + 0.9;
                if (dist < reach && this.fish.level() instanceof ServerLevel level) {
                    this.fish.doHurtTarget(level, target);
                    this.lunge = 0;
                    this.cooldown = 45 + this.fish.random.nextInt(20);
                    this.fish.setDeltaMovement(this.fish.getDeltaMovement().scale(-0.3));
                }
                return;
            }
            if (this.cooldown == 0 && dist < STRIKE_RANGE && this.fish.level() instanceof ServerLevel level) {
                // Zuschnappen: ein plötzlicher Stoß auf die Beute zu
                Vec3 to = target.getEyePosition().subtract(this.fish.position().add(0.0, this.fish.getBbHeight() * 0.5, 0.0));
                this.fish.setDeltaMovement(to.normalize().scale(0.62));
                float yaw = -((float) Mth.atan2(to.x, to.z)) * Mth.RAD_TO_DEG;
                this.fish.setYRot(yaw);
                this.fish.yBodyRot = yaw;
                this.lunge = 12;
                level.broadcastEntityEvent(this.fish, EVENT_SNAP);
                level.sendParticles(ParticleTypes.BUBBLE, this.fish.getX(), this.fish.getY(0.5), this.fish.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
                this.fish.playSound(ModSounds.ANGLERFISH_SNAP.get(), 1.0f, 1.0f);
                return;
            }
            // langsam heranpirschen, aber nie hastig
            if (dist > 3.0 && this.fish.tickCount % 10 == 0) {
                this.fish.getNavigation().moveTo(target, 0.7);
            } else if (dist <= 3.0) {
                this.fish.getNavigation().stop();
            }
        }
    }
}
