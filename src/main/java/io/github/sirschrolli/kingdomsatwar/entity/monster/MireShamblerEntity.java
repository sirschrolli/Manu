package io.github.sirschrolli.kingdomsatwar.entity.monster;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Moorschlurfer: ein Haufen lebendig gewordener Moorerde, der in Sümpfen und Mooren lauert – halb versunken, nur der
 * bemooste Buckel, die Rohrkolben und die leuchtenden Baumpilze schauen aus dem Schlamm. Kommt jemand heran (oder trifft
 * ihn), richtet er sich triefend auf. Er ist langsam, aber zäh: Aus der Ferne wirft er <b>Schlammklumpen</b> (sie
 * verlangsamen und spritzen in die Augen), aus der Nähe packt er mit seinen Wurzelfingern zu – der <b>Wurzelgriff</b> hält
 * fest und quetscht, bis man sich losreißt (ihn kräftig trifft) oder er loslässt. Im Wasser und im Regen heilt er. Findet
 * er lange niemanden, sinkt er wieder in den Schlamm.
 */
public class MireShamblerEntity extends Monster {
    public static final byte EVENT_THROW = 70;
    public static final byte EVENT_GRASP = 71;
    public static final byte EVENT_RISE = 72;
    public static final byte EVENT_SINK = 73;
    /** So lange (Ticks) dauert das Aufrichten aus dem Schlamm. */
    public static final int RISE_TICKS = 36;
    /** In diesem Tick des Wurfs verlässt der Klumpen die Hand. */
    public static final int THROW_RELEASE = 14;
    public static final int THROW_TICKS = 26;
    /** So lange (Ticks) hält der Wurzelgriff höchstens. */
    public static final int GRASP_TICKS = 60;

    private static final EntityDataAccessor<Boolean> DATA_SUBMERGED = SynchedEntityData.defineId(MireShamblerEntity.class,
            EntityDataSerializers.BOOLEAN);

    public final AnimationState throwAnimation = new AnimationState();
    public final AnimationState graspAnimation = new AnimationState();
    public final AnimationState riseAnimation = new AnimationState();
    private int riseTicks;
    private int throwTicks;
    private int throwCooldown = 40;
    private int graspTicks;
    private int graspCooldown = 60;
    private float graspDamageTaken;
    private @Nullable UUID grasped;
    private int lonely;

    public MireShamblerEntity(EntityType<? extends MireShamblerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.MOVEMENT_SPEED, 0.19)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 28.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SUBMERGED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.6, 160));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SoldierEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
    }

    /** Lauert er halb versunken im Schlamm? */
    public boolean isSubmerged() {
        return this.entityData.get(DATA_SUBMERGED);
    }

    /** Hält er gerade jemanden im Wurzelgriff? */
    public boolean isGrasping() {
        return this.graspTicks > 0;
    }

    public boolean isRising() {
        return this.riseTicks > 0;
    }

    /** Lässt ihn sofort versinken (für Tests und die Vorführung). */
    public void forceSubmerge() {
        this.sink();
    }

    /** Lässt ihn sofort einen Schlammklumpen nach {@code target} werfen (für Tests und die Vorführung). */
    public void forceThrow(LivingEntity target) {
        this.setTarget(target);
        this.startThrow();
    }

    /** Lässt ihn sofort zupacken (für Tests und die Vorführung). */
    public void forceGrasp(LivingEntity target) {
        this.setTarget(target);
        this.startGrasp(target);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        // natürlich erscheinend lauert er halb versunken
        if (reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION) {
            this.entityData.set(DATA_SUBMERGED, true);
        }
        return result;
    }

    // ------------------------------------------------------------------ Ablauf

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        LivingEntity target = this.getTarget();
        if (this.isSubmerged()) {
            this.halt();
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
            if (this.tickCount % 30 == 0) {
                level.sendParticles(ParticleTypes.BUBBLE_POP, this.getX(), this.getY() + 0.6, this.getZ(), 3, 0.5, 0.1, 0.5, 0.0);
            }
            Player near = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 6.0,
                    e -> e instanceof Player p && !p.isCreative() && !p.isSpectator());
            if (near != null || target != null && target.distanceToSqr(this) < 49.0) {
                this.rise(level);
            }
            return;
        }
        if (this.riseTicks > 0) {
            this.riseTicks--;
            this.halt();
            if (this.riseTicks % 4 == 0) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()), this.getX(), this.getY() + 0.3,
                        this.getZ(), 10, 0.6, 0.2, 0.6, 0.12);
                level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.5, this.getZ(), 8, 0.6, 0.3, 0.6, 0.1);
            }
            return;
        }
        // im Wasser oder Regen heilt er
        if (this.tickCount % 20 == 0 && this.isInWaterOrRain() && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0f);
        }
        if (this.throwCooldown > 0) {
            this.throwCooldown--;
        }
        if (this.graspCooldown > 0) {
            this.graspCooldown--;
        }
        this.tickThrow(level, target);
        this.tickGrasp(level);
        // allein: nach einer Weile sinkt er wieder in den Schlamm (wenn er auf weichem Grund oder im Wasser steht)
        if (target == null || !target.isAlive()) {
            if (++this.lonely > 400 && this.onSoftGround()) {
                this.sink();
            }
        } else {
            this.lonely = 0;
            double d = this.distanceToSqr(target);
            if (this.throwTicks == 0 && this.graspTicks == 0 && this.throwCooldown == 0 && d > 25.0 && d < 16.0 * 16.0
                    && this.hasLineOfSight(target)) {
                this.startThrow();
            }
        }
    }

    /** Bleibt stehen (der Weg wird vergessen, die Schritte enden). */
    private void halt() {
        this.getNavigation().stop();
        this.getMoveControl().setWait();
    }

    private boolean onSoftGround() {
        if (this.isInWater()) {
            return true;
        }
        BlockState below = this.level().getBlockState(this.blockPosition().below());
        return below.is(Blocks.MUD) || below.is(BlockTags.DIRT) || below.is(Blocks.CLAY) || below.is(Blocks.MUDDY_MANGROVE_ROOTS)
                || below.is(Blocks.SOUL_SOIL);
    }

    private void sink() {
        this.entityData.set(DATA_SUBMERGED, true);
        this.lonely = 0;
        this.graspTicks = 0;
        this.grasped = null;
        this.level().broadcastEntityEvent(this, EVENT_SINK);
        this.playSound(ModSounds.MIRE_SHAMBLER_RISE.get(), 1.0f, 0.7f);
    }

    private void rise(ServerLevel level) {
        this.entityData.set(DATA_SUBMERGED, false);
        this.riseTicks = RISE_TICKS;
        this.throwCooldown = Math.max(this.throwCooldown, 30);
        level.broadcastEntityEvent(this, EVENT_RISE);
        this.playSound(ModSounds.MIRE_SHAMBLER_RISE.get(), 2.0f, 1.0f);
    }

    // ------------------------------------------------------------------ Schlammwurf

    private void startThrow() {
        this.throwTicks = THROW_TICKS;
        this.throwCooldown = 70 + this.random.nextInt(50);
        this.level().broadcastEntityEvent(this, EVENT_THROW);
    }

    private void tickThrow(ServerLevel level, @Nullable LivingEntity target) {
        if (this.throwTicks <= 0) {
            return;
        }
        int t = THROW_TICKS - this.throwTicks--;
        this.halt();
        if (target != null) {
            this.getLookControl().setLookAt(target, 30.0f, 30.0f);
        }
        if (t == 2) {
            this.playSound(ModSounds.MIRE_SHAMBLER_AMBIENT.get(), 1.2f, 0.8f);
        }
        if (t == THROW_RELEASE && target != null) {
            float yaw = this.yBodyRot * net.minecraft.util.Mth.DEG_TO_RAD;
            Vec3 hand = this.position().add(-net.minecraft.util.Mth.cos(yaw) * 0.9, 2.3, -net.minecraft.util.Mth.sin(yaw) * 0.9);
            // vorhalten: wohin das Ziel läuft
            Vec3 aim = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).add(target.getDeltaMovement().multiply(8.0, 0.0, 8.0));
            int flight = 10 + (int) (Math.sqrt(hand.distanceToSqr(aim)) * 0.9);
            MonsterShot.shoot(level, this, MonsterShot.Kind.MUD, hand, MonsterShot.lob(hand, aim, flight, 0.04), 5.0f);
            this.playSound(ModSounds.MIRE_SHAMBLER_THROW.get(), 1.4f, 0.9f + this.random.nextFloat() * 0.2f);
        }
    }

    // ------------------------------------------------------------------ Wurzelgriff

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (this.graspCooldown == 0 && this.graspTicks == 0 && target instanceof LivingEntity living && this.random.nextInt(3) == 0) {
            this.startGrasp(living);
            return true;
        }
        level.broadcastEntityEvent(this, EVENT_THROW);
        this.playSound(ModSounds.MIRE_SHAMBLER_GRASP.get(), 1.0f, 1.3f);
        return super.doHurtTarget(level, target);
    }

    private void startGrasp(LivingEntity target) {
        this.graspTicks = GRASP_TICKS;
        this.graspCooldown = 160 + this.random.nextInt(80);
        this.graspDamageTaken = 0.0f;
        this.grasped = target.getUUID();
        this.level().broadcastEntityEvent(this, EVENT_GRASP);
        this.playSound(ModSounds.MIRE_SHAMBLER_GRASP.get(), 1.6f, 0.9f);
    }

    private void tickGrasp(ServerLevel level) {
        if (this.graspTicks <= 0) {
            return;
        }
        this.graspTicks--;
        this.halt();
        LivingEntity victim = this.grasped != null && level.getEntity(this.grasped) instanceof LivingEntity e && e.isAlive() ? e : null;
        if (victim == null || victim.distanceToSqr(this) > 25.0) {
            this.release();
            return;
        }
        this.getLookControl().setLookAt(victim, 30.0f, 30.0f);
        // die Wurzeln halten fest und ziehen heran
        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 10, 4, false, false), this);
        Vec3 to = this.position().subtract(victim.position()).multiply(1.0, 0.0, 1.0);
        if (to.length() > 1.8) {
            Vec3 pull = to.normalize().scale(0.08);
            victim.push(pull.x, 0.0, pull.z);
            victim.needsSync = true;
        }
        if (this.graspTicks % 12 == 0) {
            victim.hurtServer(level, this.damageSources().mobAttack(this), 2.5f);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()), victim.getX(), victim.getY(0.4),
                    victim.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
        }
        if (this.graspTicks == 0) {
            this.release();
        }
    }

    private void release() {
        this.graspTicks = 0;
        this.grasped = null;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isSubmerged() && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.rise(level);
        }
        boolean hurt = super.hurtServer(level, source, amount);
        // wer kräftig zuschlägt, reißt sich los
        if (hurt && this.graspTicks > 0) {
            this.graspDamageTaken += amount;
            if (this.graspDamageTaken >= 8.0f) {
                this.release();
                this.playSound(ModSounds.MIRE_SHAMBLER_HURT.get(), 1.2f, 1.2f);
            }
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_THROW -> this.throwAnimation.start(this.tickCount);
            case EVENT_GRASP -> this.graspAnimation.start(this.tickCount);
            case EVENT_RISE -> this.riseAnimation.start(this.tickCount);
            case EVENT_SINK -> this.riseAnimation.stop();
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && !this.isSubmerged() && this.random.nextInt(6) == 0) {
            // es tropft
            this.level().addParticle(ParticleTypes.DRIPPING_WATER, this.getRandomX(0.7), this.getY(0.3 + this.random.nextDouble() * 0.6),
                    this.getRandomZ(0.7), 0.0, 0.0, 0.0);
        }
    }

    @Override
    public boolean isPushable() {
        return !this.isSubmerged() && super.isPushable();
    }

    /** Sümpfe und Moore, nachts oder im Regen, auf weichem Grund nahe am Wasser – nie zwei dicht beieinander. */
    public static boolean checkShamblerSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        if (!Monster.checkMonsterSpawnRules(type, level, reason, pos, random)) {
            return false;
        }
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        Level world = level.getLevel();
        if (!world.isDarkOutside() && !world.isRainingAt(pos)) {
            return false;
        }
        return level.getEntitiesOfClass(MireShamblerEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(32.0)).size() < 2;
    }

    // ------------------------------------------------------------------ Speichern und Geräusche

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Submerged", this.isSubmerged());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_SUBMERGED, input.getBooleanOr("Submerged", false));
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isSubmerged() ? null : ModSounds.MIRE_SHAMBLER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MIRE_SHAMBLER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MIRE_SHAMBLER_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.MIRE_SHAMBLER_STEP.get(), 0.6f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }
}
