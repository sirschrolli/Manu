package io.github.sirschrolli.kingdomsatwar.entity.sea;

import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Mantarochen: ein riesiger, friedlicher Rochen der warmen und tiefen Meere. Er gleitet mit langsamen, weiten
 * Flügelschlägen in großen Bögen durchs Wasser und springt hin und wieder aus dem Meer – er schießt schräg hinauf, dreht
 * sich in der Luft und klatscht mit einer großen Gischt zurück. Verletzt flieht er mit kräftigen Schlägen.
 * <p>
 * Wer dicht bei ihm schwimmt, gleitet in seinem Sog mit (Gunst des Delfins).
 */
public class MantaRayEntity extends WaterAnimal {
    /** Reisegeschwindigkeit, auf der Flucht und beim Anlauf zum Sprung (Blöcke je Tick). */
    public static final double CRUISE = 0.12;
    public static final double FLEE = 0.3;
    public static final double CHARGE = 0.42;
    private static final byte EVENT_LEAP = 70;

    private @Nullable Vec3 glideTarget;
    private @Nullable BlockPos home;
    private int retargetTicks;
    private int fleeTicks;
    private @Nullable Vec3 fleeFrom;
    /** Abstand zum nächsten Sprung (Ticks); 0: bereit. */
    private int breachCooldown;
    /** 0: gleiten, 1: Anlauf zur Oberfläche, 2: in der Luft. */
    private int breach;
    private int breachTicks;
    /** Nur auf dem Client: Flügelschlag, Neigung in der Kurve (Grad) und die Drehung beim Sprung. */
    public float flap;
    public float flapO;
    public float bank;
    public float bankO;
    public float spin;
    public float spinO;
    private int leapTick = -1000;

    public MantaRayEntity(EntityType<? extends MantaRayEntity> type, Level level) {
        super(type, level);
        this.xpReward = 3;
        this.breachCooldown = 400 + this.random.nextInt(800);
        // Blick und Neigung lenkt er selbst (die Vanilla-Steuerung würde die Neigung jeden Tick zurücksetzen)
        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
            }
        };
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 1.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    // ------------------------------------------------------------------ Schwimmen

    /** Im Wasser gleitet er schwerelos mit wenig Widerstand – gelenkt wird in {@link #customServerAiStep}. */
    @Override
    protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.96));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            Vec3 v = this.getDeltaMovement();
            double speed = v.length();
            this.flapO = this.flap;
            // ruhige, weite Schläge; schnell oder steigend kräftiger, in der Luft stehen die Schwingen fast still
            this.flap += this.isInWater() ? 0.045f + (float) Math.min(0.14, speed * 0.4) + (float) Math.max(0.0, v.y) * 0.25f : 0.01f;
            this.bankO = this.bank;
            float turn = Mth.wrapDegrees(this.getYRot() - this.yRotO);
            this.bank += (Mth.clamp(turn * 5.0f, -30.0f, 30.0f) - this.bank) * 0.1f;
            this.spinO = this.spin;
            // beim Sprung eine ganze Drehung um die Längsachse, so lange er in der Luft ist
            int since = this.tickCount - this.leapTick;
            this.spin = since >= 0 && since < 40 && !this.isInWater() ? Math.min(1.0f, this.spin + 0.045f) : 0.0f;
            if (this.isInWater() && this.random.nextInt(6) == 0 && speed > 0.05) {
                float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
                double side = (this.random.nextBoolean() ? 1 : -1) * (1.2 + this.random.nextDouble() * 1.4);
                this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + Mth.cos(yaw) * side, this.getY() + 0.2,
                        this.getZ() + Mth.sin(yaw) * side, 0.0, 0.02, 0.0);
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.home == null) {
            this.home = this.blockPosition();
        }
        if (this.breachCooldown > 0) {
            this.breachCooldown--;
        }
        if (this.fleeTicks > 0) {
            this.fleeTicks--;
        }
        if (this.tickCount % 20 == 0) {
            this.blessSwimmers(level);
        }
        if (!this.isInWater()) {
            if (this.breach == 2 && ++this.breachTicks > 60) {
                this.breach = 0;
            }
            return;                                       // in der Luft (oder gestrandet): keine Steuerung
        }
        if (this.breach == 2) {
            // zurück im Wasser: große Gischt
            this.breach = 0;
            this.breachCooldown = 900 + this.random.nextInt(1500);
            this.splash(level, 1.0f);
        }
        boolean fleeing = this.fleeTicks > 0 && this.fleeFrom != null;
        if (fleeing) {
            this.breach = 0;
            Vec3 away = this.position().subtract(this.fleeFrom).multiply(1.0, 0.0, 1.0);
            away = away.lengthSqr() < 1.0e-4 ? this.getLookAngle() : away.normalize();
            this.glideTarget = this.position().add(away.scale(12.0));
        } else if (this.breach == 0 && this.breachCooldown == 0 && this.random.nextInt(40) == 0) {
            this.startBreach(level);
        } else if (this.breach == 0 && (this.glideTarget == null || --this.retargetTicks <= 0
                || this.glideTarget.distanceToSqr(this.position()) < 6.0)) {
            this.pickGlideTarget(level);
        }
        if (this.breach == 1) {
            if (++this.breachTicks > 120) {
                this.breach = 0;                         // kein Platz zum Springen
            } else {
                int surface = SeaCreatures.surfaceAbove(level, this.blockPosition(), 3);
                if (surface != Integer.MIN_VALUE && this.getY() > surface - 1.6 && this.getDeltaMovement().y > 0.15) {
                    this.leap(level);
                    return;
                }
            }
        }
        Vec3 target = this.glideTarget;
        if (target == null) {
            return;
        }
        // nicht gegen Fels: ein Stück voraus prüfen, notfalls abdrehen
        Vec3 v = this.getDeltaMovement();
        if (this.breach == 0 && v.lengthSqr() > 1.0e-4) {
            Vec3 from = this.position().add(0.0, 0.3, 0.0);
            HitResult hit = level.clip(new ClipContext(from, from.add(v.normalize().scale(4.5)), ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, this));
            if (hit.getType() != HitResult.Type.MISS) {
                this.glideTarget = this.position().add(v.normalize().scale(-6.0)).add(0.0, 1.5, 0.0);
                this.retargetTicks = 50;
                target = this.glideTarget;
            }
        }
        double speed = this.breach == 1 ? CHARGE : fleeing ? FLEE : CRUISE;
        Vec3 desired = target.subtract(this.position());
        double length = desired.length();
        if (length > 1.0e-3) {
            desired = desired.scale(speed / length);
            // nie über die Oberfläche hinaus (außer beim Sprung)
            if (this.breach == 0 && this.isNearSurface(level) && desired.y > 0.0) {
                desired = new Vec3(desired.x, -0.02, desired.z);
            }
            Vec3 next = v.add(desired.subtract(v).scale(this.breach == 1 ? 0.12 : fleeing ? 0.08 : 0.03));
            this.setDeltaMovement(next);
            if (next.horizontalDistanceSqr() > 1.0e-5) {
                float yaw = -((float) Mth.atan2(next.x, next.z)) * Mth.RAD_TO_DEG;
                this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, this.breach == 1 ? 6.0f : 3.0f));
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.getYRot();
            }
            float pitch = (float) (-Mth.atan2(next.y, next.horizontalDistance()) * Mth.RAD_TO_DEG);
            this.setXRot(Mth.approachDegrees(this.getXRot(), Mth.clamp(pitch, -50.0f, 50.0f), 3.0f));
        }
    }

    private boolean isNearSurface(ServerLevel level) {
        return SeaCreatures.surfaceAbove(level, this.blockPosition().above(), 1) != Integer.MIN_VALUE
                || !SeaCreatures.isWater(level, this.blockPosition().above());
    }

    /** Ein neues Ziel in weitem Bogen um sein Zuhause – im freien Wasser, nicht zu nah am Grund. */
    private void pickGlideTarget(ServerLevel level) {
        BlockPos center = this.home != null ? this.home : this.blockPosition();
        RandomSource random = this.random;
        for (int i = 0; i < 12; i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double r = 8.0 + random.nextDouble() * 20.0;
            double x = center.getX() + Math.cos(a) * r;
            double z = center.getZ() + Math.sin(a) * r;
            double y = center.getY() + random.nextDouble() * 10.0 - 5.0;
            BlockPos pos = BlockPos.containing(x, y, z);
            if (SeaCreatures.openWater(level, pos) && SeaCreatures.isWater(level, pos.east(2)) && SeaCreatures.isWater(level, pos.west(2))
                    && SeaCreatures.isWater(level, pos.north(2)) && SeaCreatures.isWater(level, pos.south(2))) {
                this.glideTarget = new Vec3(x, y, z);
                this.retargetTicks = 200 + random.nextInt(200);
                return;
            }
        }
        this.glideTarget = this.position().add(this.getLookAngle().scale(-4.0));
        this.retargetTicks = 60;
    }

    /** Anlauf: steil hinauf zur Oberfläche, wenn sie nah genug ist. */
    private void startBreach(ServerLevel level) {
        int surface = SeaCreatures.surfaceAbove(level, this.blockPosition(), 14);
        if (surface == Integer.MIN_VALUE || surface - this.getY() < 3.0) {
            this.breachCooldown = 200;
            return;
        }
        Vec3 ahead = this.getLookAngle().multiply(1.0, 0.0, 1.0);
        ahead = ahead.lengthSqr() < 1.0e-4 ? new Vec3(1.0, 0.0, 0.0) : ahead.normalize();
        this.glideTarget = new Vec3(this.getX() + ahead.x * 6.0, surface + 1.5, this.getZ() + ahead.z * 6.0);
        this.breach = 1;
        this.breachTicks = 0;
    }

    /** Hinaus aus dem Wasser: schräg nach oben, die Schwerkraft holt ihn zurück. */
    private void leap(ServerLevel level) {
        Vec3 v = this.getDeltaMovement();
        Vec3 flat = v.multiply(1.0, 0.0, 1.0);
        flat = flat.lengthSqr() < 1.0e-4 ? this.getLookAngle().multiply(1.0, 0.0, 1.0) : flat;
        flat = flat.normalize().scale(0.32);
        this.setDeltaMovement(flat.x, 0.78 + this.random.nextDouble() * 0.12, flat.z);
        this.breach = 2;
        this.breachTicks = 0;
        level.broadcastEntityEvent(this, EVENT_LEAP);
        this.playSound(ModSounds.MANTA_RAY_LEAP.get(), 1.2f, 0.9f + this.random.nextFloat() * 0.2f);
        this.splash(level, 0.5f);
    }

    private void splash(ServerLevel level, float strength) {
        level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.6, this.getZ(), (int) (60 * strength), 1.6, 0.3, 1.6, 0.3);
        level.sendParticles(ParticleTypes.BUBBLE, this.getX(), this.getY(), this.getZ(), (int) (30 * strength), 1.4, 0.4, 1.4, 0.15);
        level.sendParticles(ParticleTypes.FISHING, this.getX(), this.getY() + 0.8, this.getZ(), (int) (24 * strength), 1.8, 0.1, 1.8, 0.05);
        if (strength >= 1.0f) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.MANTA_RAY_SPLASH.get(), SoundSource.NEUTRAL, 1.8f,
                    0.85f + this.random.nextFloat() * 0.2f);
        }
    }

    /** Im Sog des Rochens: wer nah bei ihm schwimmt, bekommt kurz die Gunst des Delfins. */
    private void blessSwimmers(ServerLevel level) {
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(4.0, 2.5, 4.0))) {
            if (player.isInWater() && !player.isSpectator()) {
                player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 50, 0, true, false, true), this);
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_LEAP) {
            this.leapTick = this.tickCount;
        } else {
            super.handleEntityEvent(id);
        }
    }

    /** Für die Vorführung und Tests: sofort zum Sprung ansetzen. */
    public void forceBreach(ServerLevel level) {
        this.breachCooldown = 0;
        this.startBreach(level);
    }

    public boolean isBreaching() {
        return this.breach != 0;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            this.fleeTicks = 100;
            Entity attacker = source.getEntity();
            this.fleeFrom = attacker != null ? attacker.position() : this.position().add(this.getLookAngle());
        }
        return hurt;
    }

    // ------------------------------------------------------------------ Speichern, Laichen, Geräusche

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.home != null) {
            output.putLong("Home", this.home.asLong());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.home = input.getLong("Home").map(BlockPos::of).orElse(null);
    }

    /** Im offenen Wasser, wenigstens sechs Blöcke tief und nicht direkt an der Oberfläche. */
    public static boolean checkMantaSpawnRules(EntityType<? extends Mob> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        int sea = level.getSeaLevel();
        return pos.getY() < sea - 3 && pos.getY() > sea - 30 && SeaCreatures.openWater(level, pos)
                && SeaCreatures.depthBelow(level, pos, 8) >= 2 && SeaCreatures.isWater(level, pos.above(2));
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MANTA_RAY_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MANTA_RAY_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.4f;
    }

    @Override
    public int getMaxHeadXRot() {
        return 1;
    }
}
