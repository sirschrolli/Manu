package io.github.sirschrolli.kingdomsatwar.entity.voidrealm;

import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Sternenrochen: ein riesiger, friedlicher Rochen, der mit langsamen Flügelschlägen in weiten Bögen um die
 * Bruchstücke der Leere gleitet. Wird er verletzt, flieht er mit kräftigen Schlägen.
 * <p>
 * Wer ihm <b>Leerenblüten</b> füttert, gewinnt sein Vertrauen (jede Blüte mit einer Chance von eins zu drei). Danach
 * trägt er seinen Freund durch die Leere: er gleitet stets langsam voran, schneller mit „vorwärts“, hält mit „rückwärts“
 * fast an; wohin man blickt, dorthin steigt oder sinkt er, mit „springen“ steigt er auf.
 */
public class StarRayEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_TAMED = SynchedEntityData.defineId(StarRayEntity.class, EntityDataSerializers.BOOLEAN);
    /** Reisegeschwindigkeit (Blöcke je Tick) beim Gleiten, auf der Flucht und mit Reiter (höchstens). */
    public static final double CRUISE = 0.16;
    public static final double FLEE = 0.38;
    public static final float RIDDEN_ACCEL = 0.06f;

    private @Nullable UUID owner;
    private @Nullable Vec3 glideTarget;
    private @Nullable BlockPos home;
    private int retargetTicks;
    private int fleeTicks;
    private @Nullable Vec3 fleeFrom;
    /** Nur auf dem Client: Neigung in der Kurve (Grad) und Phase des Flügelschlags. */
    public float bank;
    public float bankO;
    public float flap;
    public float flapO;

    public StarRayEntity(EntityType<? extends StarRayEntity> type, Level level) {
        super(type, level);
        this.xpReward = 4;
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TAMED, false);
    }

    public boolean isTamed() {
        return this.entityData.get(DATA_TAMED);
    }

    // ------------------------------------------------------------------ Fliegen

    @Override
    public void travel(Vec3 input) {
        this.travelFlying(input, this.isVehicle() ? RIDDEN_ACCEL : 0.02f);
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public boolean isFlyingVehicle() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            Vec3 v = this.getDeltaMovement();
            double speed = v.horizontalDistance();
            this.flapO = this.flap;
            // langsam: ruhige, weite Schläge; schnell oder steigend: kräftiger
            this.flap += 0.05f + (float) Math.min(0.12, speed * 0.35) + (float) Math.max(0.0, v.y) * 0.4f;
            this.bankO = this.bank;
            float turn = Mth.wrapDegrees(this.getYRot() - this.yRotO);
            this.bank += (Mth.clamp(turn * 4.0f, -32.0f, 32.0f) - this.bank) * 0.08f;
            if (this.random.nextInt(4) == 0) {
                // Sternenstaub, der von den Schwingen rieselt
                float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
                double side = (this.random.nextBoolean() ? 1 : -1) * (1.0 + this.random.nextDouble() * 1.8);
                this.level().addParticle(ParticleTypes.END_ROD, this.getX() + Mth.cos(yaw) * side, this.getY() + 0.2,
                        this.getZ() + Mth.sin(yaw) * side, 0.0, -0.01, 0.0);
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.home == null) {
            this.home = this.blockPosition();
        }
        if (this.getControllingPassenger() != null) {
            return;
        }
        if (this.fleeTicks > 0) {
            this.fleeTicks--;
        }
        boolean fleeing = this.fleeTicks > 0 && this.fleeFrom != null;
        if (fleeing) {
            Vec3 away = this.position().subtract(this.fleeFrom).multiply(1.0, 0.0, 1.0);
            away = away.lengthSqr() < 1.0e-4 ? this.getLookAngle() : away.normalize();
            this.glideTarget = this.position().add(away.scale(14.0)).add(0.0, 3.0, 0.0);
        } else if (this.glideTarget == null || --this.retargetTicks <= 0 || this.glideTarget.distanceToSqr(this.position()) < 9.0) {
            this.pickGlideTarget(level);
        }
        Vec3 target = this.glideTarget;
        if (target == null) {
            return;
        }
        // nicht gegen Gestein: ein Stück voraus prüfen, notfalls nach oben abdrehen
        Vec3 v = this.getDeltaMovement();
        if (v.lengthSqr() > 1.0e-4) {
            Vec3 ahead = this.position().add(0.0, 0.4, 0.0).add(v.normalize().scale(5.0));
            HitResult hit = level.clip(new ClipContext(this.position().add(0.0, 0.4, 0.0), ahead, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, this));
            if (hit.getType() != HitResult.Type.MISS) {
                this.glideTarget = this.position().add(v.normalize().scale(-6.0)).add(0.0, 7.0, 0.0);
                this.retargetTicks = 60;
                target = this.glideTarget;
            }
        }
        double speed = fleeing ? FLEE : CRUISE;
        Vec3 desired = target.subtract(this.position());
        double length = desired.length();
        if (length > 1.0e-3) {
            desired = desired.scale(speed / length);
            // sanft einlenken – ein großer Rochen wendet nicht auf der Stelle
            Vec3 next = v.add(desired.subtract(v).scale(fleeing ? 0.08 : 0.035));
            this.setDeltaMovement(next);
            if (next.horizontalDistanceSqr() > 1.0e-5) {
                float yaw = -((float) Mth.atan2(next.x, next.z)) * Mth.RAD_TO_DEG;
                this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, 4.0f));
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.getYRot();
            }
        }
    }

    /** Ein neues Ziel in weitem Bogen um sein Zuhause, eher auf Höhe der Bruchstücke. */
    private void pickGlideTarget(ServerLevel level) {
        BlockPos center = this.home != null ? this.home : this.blockPosition();
        RandomSource random = this.random;
        for (int i = 0; i < 10; i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double r = 10.0 + random.nextDouble() * 26.0;
            double x = center.getX() + Math.cos(a) * r;
            double z = center.getZ() + Math.sin(a) * r;
            double y = Mth.clamp(center.getY() + (random.nextDouble() * 18.0 - 7.0), 16.0, 230.0);
            BlockPos pos = BlockPos.containing(x, y, z);
            if (level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above()) && level.isEmptyBlock(pos.below())) {
                this.glideTarget = new Vec3(x, y, z);
                this.retargetTicks = 200 + random.nextInt(200);
                return;
            }
        }
        this.glideTarget = this.position().add(0.0, 4.0, 0.0);
        this.retargetTicks = 60;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            this.fleeTicks = 120;
            Entity attacker = source.getEntity();
            this.fleeFrom = attacker != null ? attacker.position() : this.position().add(this.getLookAngle());
            if (this.getFirstPassenger() != null && attacker == this.getFirstPassenger()) {
                this.ejectPassengers();
            }
        }
        return hurt;
    }

    // ------------------------------------------------------------------ Zähmen und Reiten

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(VoidBlocks.VOID_BLOOM.asItem())) {
            if (this.isTamed()) {
                if (this.getHealth() < this.getMaxHealth()) {
                    stack.consume(1, player);
                    this.heal(8.0f);
                    this.level().addParticle(ParticleTypes.HEART, this.getX(), this.getY(1.0), this.getZ(), 0.0, 0.1, 0.0);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            }
            stack.consume(1, player);
            if (this.level() instanceof ServerLevel level) {
                if (this.random.nextInt(3) == 0) {
                    this.entityData.set(DATA_TAMED, true);
                    this.owner = player.getUUID();
                    this.setPersistenceRequired();
                    this.fleeTicks = 0;
                    level.sendParticles(ParticleTypes.HEART, this.getX(), this.getY(1.0), this.getZ(), 7, 0.8, 0.3, 0.8, 0.0);
                    this.playSound(ModSounds.STAR_RAY_AMBIENT.get(), 1.2f, 1.3f);
                } else {
                    level.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(1.0), this.getZ(), 7, 0.6, 0.3, 0.6, 0.02);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (this.isTamed() && stack.isEmpty() && !player.isSecondaryUseActive() && !this.isVehicle()
                && player.getUUID().equals(this.owner)) {
            if (!this.level().isClientSide()) {
                player.setYRot(this.getYRot());
                player.startRiding(this);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        return this.isTamed() && this.getFirstPassenger() instanceof Player player ? player : super.getControllingPassenger();
    }

    @Override
    protected Vec3 getRiddenInput(Player controller, Vec3 selfInput) {
        // er gleitet immer langsam voran; vorwärts beschleunigt, rückwärts bremst fast bis zum Stillstand
        float forward = controller.zza > 0.0f ? 1.0f : controller.zza < 0.0f ? 0.05f : 0.35f;
        float pitch = controller.getXRot() * Mth.DEG_TO_RAD;
        float up = -Mth.sin(pitch) * forward * 1.2f;
        if (controller.isJumping()) {
            up += 0.6f;
        }
        return new Vec3(controller.xxa * 0.25f, up, Mth.cos(pitch) * forward);
    }

    @Override
    protected float getRiddenSpeed(Player controller) {
        return RIDDEN_ACCEL;
    }

    @Override
    protected void tickRidden(Player controller, Vec3 riddenInput) {
        super.tickRidden(controller, riddenInput);
        float yRot = this.getYRot() + Mth.wrapDegrees(controller.getYRot() - this.getYRot()) * 0.1f;
        this.setRot(yRot, controller.getXRot() * 0.4f);
        this.yRotO = this.yBodyRot = this.yHeadRot = yRot;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        // wo man absteigt, wartet er künftig
        this.home = this.blockPosition();
        this.glideTarget = null;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }

    // ------------------------------------------------------------------ Speichern, Laichen, Geräusche

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Tamed", this.isTamed());
        if (this.owner != null) {
            output.store("Owner", UUIDUtil.CODEC, this.owner);
        }
        if (this.home != null) {
            output.putLong("Home", this.home.asLong());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_TAMED, input.getBooleanOr("Tamed", false));
        this.owner = input.read("Owner", UUIDUtil.CODEC).orElse(null);
        this.home = input.getLong("Home").map(BlockPos::of).orElse(null);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return false;
    }

    /** Frei in der Luft über Höhe 20 (unter und neben den Bruchstücken). */
    public static boolean checkRaySpawnRules(EntityType<? extends Mob> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        return EntitySpawnReason.isSpawner(reason) || pos.getY() > 20 && level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())
                && level.isEmptyBlock(pos.above(2));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.STAR_RAY_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.STAR_RAY_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.STAR_RAY_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 260;
    }

    @Override
    protected float getSoundVolume() {
        return 1.6f;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }
}
