package io.github.sirschrolli.kingdomsatwar.entity.voidrealm;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Leerenschlund: ein schwebender Räuber der Leere, halb Qualle, halb Maul. Er treibt pumpend zwischen den Bruchstücken;
 * sieht er Beute, nähert er sich, reißt sein Maul auf und <b>saugt</b> sie an – wer an einer Kante steht, wird leicht
 * ins Nichts gezogen. Ist die Beute nah genug, schnappen die Lippen zu. Ducken schwächt den Sog, Treffer unterbrechen
 * ihn, und ohne Sicht auf die Beute kann er nicht saugen.
 */
public class VoidMawEntity extends Monster {
    public static final byte EVENT_BITE = 61;
    /** Zustand: treiben, ansaugen oder schnappen (für Maul und Teilchen auf dem Client). */
    private static final EntityDataAccessor<Byte> DATA_PHASE = SynchedEntityData.defineId(VoidMawEntity.class, EntityDataSerializers.BYTE);
    public static final byte DRIFT = 0;
    public static final byte INHALE = 1;

    /** Dauer des Ansaugens in Ticks; Reichweite des Sogs; ab dieser Nähe schnappt er zu. */
    public static final int INHALE_TICKS = 50;
    public static final double PULL_RANGE = 11.0;
    public static final double BITE_RANGE = 2.2;

    public final AnimationState biteAnimation = new AnimationState();
    /** Wie weit das Maul offen steht (0..1, weich) – nur auf dem Client. */
    public float mouthOpen;
    public float mouthOpenO;
    private float damageWhileInhaling;

    public VoidMawEntity(EntityType<? extends VoidMawEntity> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.moveControl = new DriftMoveControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 34.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.FLYING_SPEED, 0.35)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE, DRIFT);
    }

    public byte phase() {
        return this.entityData.get(DATA_PHASE);
    }

    public void setPhase(byte phase) {
        this.entityData.set(DATA_PHASE, phase);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new HuntGoal(this));
        this.goalSelector.addGoal(5, new DriftGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, null));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SoldierEntity.class, 10, true, false, null));
    }

    // ------------------------------------------------------------------ Schweben

    @Override
    public void travel(Vec3 input) {
        this.travelFlying(input, 0.02f);
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            this.mouthOpenO = this.mouthOpen;
            float wanted = this.phase() == INHALE ? 1.0f : 0.0f;
            this.mouthOpen += (wanted - this.mouthOpen) * (wanted > this.mouthOpen ? 0.2f : 0.12f);
            if (this.phase() == INHALE) {
                this.inhaleParticles();
            } else if (this.random.nextInt(5) == 0) {
                // Leuchtfunken, die von den Fangfäden rieseln
                this.level().addParticle(ParticleTypes.END_ROD, this.getRandomX(0.8), this.getY() + this.random.nextDouble() * 0.6,
                        this.getRandomZ(0.8), 0.0, -0.02, 0.0);
            }
        }
    }

    /** Ein Strom aus Teilchen, der aus der Umgebung ins offene Maul gezogen wird. */
    private void inhaleParticles() {
        Vec3 mouth = this.mouthPosition();
        for (int i = 0; i < 3; i++) {
            double r = 2.5 + this.random.nextDouble() * 5.5;
            double yaw = this.random.nextDouble() * Math.PI * 2.0;
            double pitch = (this.random.nextDouble() - 0.6) * 1.4;
            Vec3 from = mouth.add(Math.cos(yaw) * Math.cos(pitch) * r, Math.sin(pitch) * r, Math.sin(yaw) * Math.cos(pitch) * r);
            Vec3 v = mouth.subtract(from).scale(0.09);
            this.level().addParticle(i == 0 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.PORTAL, from.x, from.y, from.z, v.x, v.y, v.z);
        }
    }

    /** Wo das Maul sitzt (unter der Glocke, gut einen Block über den Spitzen der Fangfäden). */
    public Vec3 mouthPosition() {
        return this.position().add(0.0, this.getBbHeight() * 0.52, 0.0);
    }

    // ------------------------------------------------------------------ Angriff

    public void bite(ServerLevel level, LivingEntity target) {
        level.broadcastEntityEvent(this, EVENT_BITE);
        this.playSound(ModSounds.VOID_MAW_BITE.get(), 1.4f, 0.9f + this.random.nextFloat() * 0.2f);
        if (this.doHurtTarget(level, target)) {
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 1), this);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 1), this);
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.6), target.getZ(), 10, 0.3, 0.3, 0.3, 0.01);
    }

    /** Zieht das Ziel ein Stück zum Maul (gedrückte Schleichtaste schwächt den Sog stark). */
    public void pull(LivingEntity target) {
        Vec3 mouth = this.mouthPosition();
        Vec3 dir = mouth.subtract(target.position().add(0.0, target.getBbHeight() * 0.5, 0.0));
        double distance = dir.length();
        if (distance < 0.5 || distance > PULL_RANGE) {
            return;
        }
        double strength = 0.075 * (1.15 - distance / PULL_RANGE) * (target.isShiftKeyDown() ? 0.3 : 1.0);
        Vec3 push = dir.scale(strength / distance);
        target.push(push.x, push.y * 0.8 + 0.006, push.z);
        target.needsSync = true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.phase() == INHALE) {
            this.damageWhileInhaling += amount;
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

    /** In der Leere frei schwebend über Höhe 20, nicht im Frieden. */
    public static boolean checkMawSpawnRules(EntityType<? extends Mob> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && (EntitySpawnReason.isSpawner(reason) || pos.getY() > 20
                && level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())
                && level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos) < 8);
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.VOID_MAW_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.VOID_MAW_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.VOID_MAW_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 140;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    // ------------------------------------------------------------------ Steuerung

    /** Treibt pumpend auf den Wunschpunkt zu, wie eine Qualle (Stöße statt gleichmäßiger Fahrt). */
    static class DriftMoveControl extends MoveControl<VoidMawEntity> {
        DriftMoveControl(VoidMawEntity mob) {
            super(mob);
        }

        @Override
        public void tick() {
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                // langsam ausschweben und kaum merklich sinken
                this.mob.setDeltaMovement(this.mob.getDeltaMovement().multiply(0.96, 0.9, 0.96).add(0.0, -0.0015, 0.0));
                return;
            }
            Vec3 delta = new Vec3(this.wantedX - this.mob.getX(), this.wantedY - this.mob.getY(), this.wantedZ - this.mob.getZ());
            double length = delta.length();
            if (length < 0.6) {
                this.operation = MoveControl.Operation.WAIT;
                return;
            }
            // Pumpstöße: im Takt der Glocke kräftiger, dazwischen fast nur Gleiten
            float pulse = 0.35f + 0.65f * Math.max(0.0f, Mth.sin(this.mob.tickCount * 0.16f));
            double speed = this.mob.getAttributeValue(Attributes.FLYING_SPEED) * this.speedModifier * 0.05 * pulse;
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(delta.scale(speed / length)));
            LivingEntity target = this.mob.getTarget();
            double lx = target != null ? target.getX() - this.mob.getX() : delta.x;
            double lz = target != null ? target.getZ() - this.mob.getZ() : delta.z;
            if (lx * lx + lz * lz > 1.0e-4) {
                float yaw = -((float) Mth.atan2(lx, lz)) * Mth.RAD_TO_DEG;
                this.mob.setYRot(Mth.approachDegrees(this.mob.getYRot(), yaw, 6.0f));
                this.mob.yBodyRot = this.mob.getYRot();
                this.mob.yHeadRot = this.mob.getYRot();
            }
        }
    }

    /** Ohne Beute: gemächlich in der Nähe treiben, lieber auf Höhe der Bruchstücke als tief darunter. */
    static class DriftGoal extends Goal {
        private final VoidMawEntity maw;

        DriftGoal(VoidMawEntity maw) {
            this.maw = maw;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            MoveControl<?> control = this.maw.getMoveControl();
            if (!control.hasWanted()) {
                return this.maw.random.nextInt(reducedTickDelay(20)) == 0;
            }
            double dx = control.getWantedX() - this.maw.getX();
            double dy = control.getWantedY() - this.maw.getY();
            double dz = control.getWantedZ() - this.maw.getZ();
            double d = dx * dx + dy * dy + dz * dz;
            return d < 1.0 || d > 3600.0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            RandomSource random = this.maw.random;
            for (int i = 0; i < 12; i++) {
                double x = this.maw.getX() + (random.nextDouble() * 2.0 - 1.0) * 14.0;
                double y = this.maw.getY() + (random.nextDouble() * 2.0 - 1.0) * 6.0;
                double z = this.maw.getZ() + (random.nextDouble() * 2.0 - 1.0) * 14.0;
                // nicht ins Gestein, nicht tief ins Nichts hinab
                BlockPos pos = BlockPos.containing(x, y, z);
                if (y > 12.0 && this.maw.level().isEmptyBlock(pos) && this.maw.level().isEmptyBlock(pos.above())) {
                    this.maw.getMoveControl().setWantedPosition(x, y, z, 0.7);
                    return;
                }
            }
        }
    }

    /** Jagd: nähern, ansaugen, zuschnappen, kurz zurückweichen – und von vorn. */
    static class HuntGoal extends Goal {
        private final VoidMawEntity maw;
        private int cooldown;
        private int inhale;

        HuntGoal(VoidMawEntity maw) {
            this.maw = maw;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.maw.getTarget();
            return target != null && target.isAlive() && this.maw.canAttack(target);
        }

        @Override
        public void start() {
            this.cooldown = 20;
            this.inhale = 0;
        }

        @Override
        public void stop() {
            this.maw.setPhase(DRIFT);
            this.inhale = 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.maw.getTarget();
            if (target == null) {
                return;
            }
            ServerLevel level = getServerLevel(this.maw);
            double distance = this.maw.distanceTo(target);
            boolean sees = this.maw.getSensing().hasLineOfSight(target);
            this.maw.getLookControl().setLookAt(target, 30.0f, 30.0f);
            if (this.inhale > 0) {
                this.inhale--;
                // über der Beute schweben bleiben, das Maul nach unten gewandt
                Vec3 hover = target.position().add(0.0, 1.6, 0.0);
                this.maw.getMoveControl().setWantedPosition(hover.x, hover.y, hover.z, 0.6);
                if (sees) {
                    this.maw.pull(target);
                }
                if (this.maw.mouthPosition().distanceTo(target.position().add(0.0, target.getBbHeight() * 0.5, 0.0)) < BITE_RANGE) {
                    this.maw.bite(level, target);
                    this.endInhale(70);
                } else if (this.maw.damageWhileInhaling >= 6.0f) {
                    // getroffen: das Maul schnappt leer zu, er weicht zurück
                    this.maw.playSound(ModSounds.VOID_MAW_HURT.get(), 1.0f, 1.3f);
                    this.endInhale(60);
                } else if (this.inhale == 0 || !sees && this.inhale % 10 == 0 && distance > PULL_RANGE * 0.7) {
                    this.endInhale(50);
                }
                return;
            }
            if (this.cooldown > 0) {
                this.cooldown--;
                // Abstand halten: etwas über und neben der Beute kreisen
                double a = this.maw.tickCount * 0.04 + this.maw.getId();
                Vec3 orbit = target.position().add(Math.cos(a) * 6.0, 3.5, Math.sin(a) * 6.0);
                this.maw.getMoveControl().setWantedPosition(orbit.x, orbit.y, orbit.z, 0.9);
                return;
            }
            if (distance < 7.5 && sees) {
                this.inhale = INHALE_TICKS;
                this.maw.damageWhileInhaling = 0.0f;
                this.maw.setPhase(INHALE);
                this.maw.playSound(ModSounds.VOID_MAW_INHALE.get(), 1.6f, 0.95f + this.maw.random.nextFloat() * 0.1f);
            } else {
                Vec3 approach = target.position().add(0.0, 2.6, 0.0);
                this.maw.getMoveControl().setWantedPosition(approach.x, approach.y, approach.z, 1.3);
            }
        }

        private void endInhale(int pause) {
            this.inhale = 0;
            this.cooldown = pause + this.maw.random.nextInt(30);
            this.maw.setPhase(DRIFT);
        }
    }
}
