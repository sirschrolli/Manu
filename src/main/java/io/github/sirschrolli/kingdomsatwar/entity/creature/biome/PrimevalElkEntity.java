package io.github.sirschrolli.kingdomsatwar.entity.creature.biome;

import io.github.sirschrolli.kingdomsatwar.registry.BiomeBeasts;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Urelch des Urhains (0.85): riesig, ruhig, grast und knabbert Leuchtbeeren. Die Bullen tragen bemooste Schaufeln mit
 * leuchtenden Ranken – greift man einen Bullen an, senkt er die Schaufeln und stürmt los (wirft Angreifer weit fort);
 * Kühe und Kälber fliehen. Zähmen wie ein Pferd (aufsitzen, bis er sich fügt; Äpfel, Weizen und Beeren helfen), dann mit
 * Sattel reiten und mit einer Truhe als Lasttier nutzen. Er hält mehr aus und springt höher als ein Pferd, ist aber
 * etwas langsamer.
 */
public class PrimevalElkEntity extends AbstractChestedHorse {
    public static final byte EVENT_CHARGE = 100;
    public static final byte EVENT_IMPACT = 101;
    public static final byte EVENT_BUGLE = 102;
    /** Werte der Elche (eigene Grenzen, nicht die der Pferde). */
    private static final float MIN_HEALTH = 40.0f;
    private static final float MAX_HEALTH = 56.0f;
    private static final double MIN_SPEED = 0.2;
    private static final double MAX_SPEED = 0.27;
    private static final double MIN_JUMP = 0.65;
    private static final double MAX_JUMP = 0.9;

    private static final EntityDataAccessor<Boolean> DATA_BULL = SynchedEntityData.defineId(PrimevalElkEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CHARGING = SynchedEntityData.defineId(PrimevalElkEntity.class,
            EntityDataSerializers.BOOLEAN);

    public final AnimationState bugleAnimation = new AnimationState();
    public final AnimationState impactAnimation = new AnimationState();
    private int chargeCooldown;

    public PrimevalElkEntity(EntityType<? extends PrimevalElkEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseChestedHorseAttributes()
                .add(Attributes.MAX_HEALTH, 48.0)
                .add(Attributes.MOVEMENT_SPEED, 0.235)
                .add(Attributes.JUMP_STRENGTH, 0.78)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BULL, false);
        builder.define(DATA_CHARGING, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new ChargeGoal());
        // Kühe und Kälber fliehen, wenn man sie verletzt; Bullen nicht
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6) {
            @Override
            public boolean canUse() {
                return !PrimevalElkEntity.this.isBull() || PrimevalElkEntity.this.isBaby() ? super.canUse() : false;
            }
        });
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.2, this::isFood, false));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return PrimevalElkEntity.this.isBull() && !PrimevalElkEntity.this.isBaby() && !PrimevalElkEntity.this.isTamed()
                        && super.canUse();
            }
        });
    }

    @Override
    protected void randomizeAttributes(RandomSource random) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(MIN_HEALTH + random.nextFloat() * (MAX_HEALTH - MIN_HEALTH));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MIN_SPEED + random.nextDouble() * (MAX_SPEED - MIN_SPEED));
        this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(MIN_JUMP + random.nextDouble() * (MAX_JUMP - MIN_JUMP));
    }

    @Override
    protected void setOffspringAttributes(AgeableMob partner, AbstractHorse baby) {
        RandomSource random = this.random;
        baby.getAttribute(Attributes.MAX_HEALTH).setBaseValue(createOffspringAttribute(this.getAttributeBaseValue(Attributes.MAX_HEALTH),
                partner.getAttributeBaseValue(Attributes.MAX_HEALTH), MIN_HEALTH, MAX_HEALTH, random));
        baby.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(createOffspringAttribute(this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED),
                partner.getAttributeBaseValue(Attributes.MOVEMENT_SPEED), MIN_SPEED, MAX_SPEED, random));
        baby.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(createOffspringAttribute(this.getAttributeBaseValue(Attributes.JUMP_STRENGTH),
                partner.getAttributeBaseValue(Attributes.JUMP_STRENGTH), MIN_JUMP, MAX_JUMP, random));
    }

    // ------------------------------------------------------------------ Zustand

    /** Bulle: trägt Schaufeln und verteidigt sich. */
    public boolean isBull() {
        return this.entityData.get(DATA_BULL);
    }

    public void setBull(boolean bull) {
        this.entityData.set(DATA_BULL, bull);
    }

    /** Stürmt mit gesenkten Schaufeln los. */
    public boolean isCharging() {
        return this.entityData.get(DATA_CHARGING);
    }

    public void setCharging(boolean charging) {
        this.entityData.set(DATA_CHARGING, charging);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.setBull(this.random.nextFloat() < 0.4f);
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Bull", this.isBull());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setBull(input.getBooleanOr("Bull", false));
    }

    // ------------------------------------------------------------------ Takt

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            if (this.chargeCooldown > 0) {
                this.chargeCooldown--;
            }
        } else if (this.isCharging() && this.onGround() && this.tickCount % 3 == 0) {
            BlockState below = this.level().getBlockState(this.blockPosition().below());
            if (!below.isAir()) {
                for (int i = 0; i < 2; i++) {
                    this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, below), this.getRandomX(0.6), this.getY() + 0.1,
                            this.getRandomZ(0.6), 0.0, 0.12, 0.0);
                }
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_BUGLE -> this.bugleAnimation.start(this.tickCount);
            case EVENT_IMPACT -> this.impactAnimation.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    /** Ein Bulle ruft (der tiefe Brunftruf): Kopf hoch, Maul auf. */
    public void bugle() {
        if (this.level() instanceof ServerLevel level) {
            level.broadcastEntityEvent(this, EVENT_BUGLE);
            this.playSound(ModSounds.PRIMEVAL_ELK_BUGLE.get(), 1.6f, 0.9f + this.random.nextFloat() * 0.15f);
        }
    }

    @Override
    public void playAmbientSound() {
        if (this.isBull() && !this.isBaby() && this.random.nextInt(4) == 0) {
            this.bugle();
            return;
        }
        super.playAmbientSound();
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            Vec3 push = living.position().subtract(this.position()).multiply(1, 0, 1).normalize();
            living.push(push.x * 0.9, 0.45, push.z * 0.9);
        }
        level.broadcastEntityEvent(this, EVENT_IMPACT);
        this.playSound(ModSounds.PRIMEVAL_ELK_IMPACT.get(), 1.0f, 0.9f + this.random.nextFloat() * 0.2f);
        return hit;
    }

    // ------------------------------------------------------------------ Futter, Zähmen, Junge

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT) || stack.is(Items.APPLE) || stack.is(Items.GLOW_BERRIES) || stack.is(Items.SWEET_BERRIES)
                || stack.is(Items.HAY_BLOCK) || stack.is(Items.GOLDEN_APPLE) || stack.is(Items.GOLDEN_CARROT);
    }

    @Override
    protected boolean handleEating(Player player, ItemStack stack) {
        // Beeren sind Leckerbissen: etwas Heilung, gutes Zureden
        if (stack.is(Items.GLOW_BERRIES) || stack.is(Items.SWEET_BERRIES)) {
            boolean used = false;
            if (this.getHealth() < this.getMaxHealth()) {
                this.heal(2.0f);
                used = true;
            }
            if (this.isBaby()) {
                this.ageUp(20);
                used = true;
            }
            if (!this.isTamed() && this.getTemper() < this.getMaxTemper()) {
                this.modifyTemper(4);
                used = true;
            }
            if (used) {
                this.playSound(ModSounds.PRIMEVAL_ELK_EAT.get(), 0.9f, 1.0f);
                this.gameEvent(net.minecraft.world.level.gameevent.GameEvent.EAT);
            }
            return used;
        }
        return super.handleEating(player, stack);
    }

    @Override
    public boolean canMate(Animal partner) {
        return partner != this && partner instanceof PrimevalElkEntity other && this.canParent() && other.canParent();
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        PrimevalElkEntity calf = BiomeBeasts.PRIMEVAL_ELK.get().create(level, EntitySpawnReason.BREEDING);
        if (calf != null) {
            calf.setBull(this.random.nextBoolean());
            this.setOffspringAttributes(partner, calf);
        }
        return calf;
    }

    /** Auf Gras, Podsol, Moos und Erde des Waldbodens, wo es hell genug ist. */
    public static boolean checkElkSpawnRules(EntityType<PrimevalElkEntity> type, net.minecraft.world.level.LevelAccessor level,
            EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        boolean ground = below.is(net.minecraft.tags.BlockTags.DIRT) || below.is(net.minecraft.tags.BlockTags.ANIMALS_SPAWNABLE_ON);
        return ground && (EntitySpawnReason.ignoresLightRequirements(reason) || level.getRawBrightness(pos, 0) > 8);
    }

    @Override
    protected boolean canPerformRearing() {
        return true;
    }

    @Override
    public int getMaxTemper() {
        return 120;
    }

    @Override
    public int getInventoryColumns() {
        return this.hasChest() ? 5 : 0;
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.PRIMEVAL_ELK_AMBIENT.get();
    }

    @Override
    protected @Nullable SoundEvent getAngrySound() {
        return ModSounds.PRIMEVAL_ELK_ANGRY.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.PRIMEVAL_ELK_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.PRIMEVAL_ELK_DEATH.get();
    }

    @Override
    protected @Nullable SoundEvent getEatingSound() {
        return ModSounds.PRIMEVAL_ELK_EAT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.PRIMEVAL_ELK_STEP.get(), 0.35f, 1.0f);
    }

    @Override
    public float getVoicePitch() {
        return this.isBaby() ? 1.5f : (this.isBull() ? 0.85f : 1.0f) + (this.random.nextFloat() - 0.5f) * 0.1f;
    }

    // =====================================================================================
    // Der Ansturm des Bullen
    // =====================================================================================

    /** Ein wilder Bulle, der angegriffen wurde: senkt die Schaufeln, stürmt los und schleudert den Angreifer fort. */
    final class ChargeGoal extends Goal {
        private int pathDelay;
        private int ticks;

        ChargeGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            PrimevalElkEntity elk = PrimevalElkEntity.this;
            LivingEntity target = elk.getTarget();
            return elk.isBull() && !elk.isBaby() && !elk.isVehicle() && target != null && target.isAlive() && elk.chargeCooldown <= 0;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = PrimevalElkEntity.this.getTarget();
            return target != null && target.isAlive() && this.ticks < 200 && !PrimevalElkEntity.this.isVehicle()
                    && PrimevalElkEntity.this.distanceToSqr(target) < 24.0 * 24.0;
        }

        @Override
        public void start() {
            this.pathDelay = 0;
            this.ticks = 0;
            PrimevalElkEntity.this.setCharging(true);
            PrimevalElkEntity.this.playSound(ModSounds.PRIMEVAL_ELK_ANGRY.get(), 1.2f, 0.8f);
        }

        @Override
        public void stop() {
            PrimevalElkEntity.this.setCharging(false);
            PrimevalElkEntity.this.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            PrimevalElkEntity elk = PrimevalElkEntity.this;
            LivingEntity target = elk.getTarget();
            if (target == null) {
                return;
            }
            this.ticks++;
            elk.getLookControl().setLookAt(target, 30.0f, 30.0f);
            if (--this.pathDelay <= 0) {
                this.pathDelay = 5;
                elk.getNavigation().moveTo(target, 2.1);
            }
            double reach = elk.getBbWidth() * 0.5 + target.getBbWidth() * 0.5 + 0.9;
            if (elk.distanceToSqr(target) <= reach * reach) {
                elk.doHurtTarget((ServerLevel) elk.level(), target);
                elk.chargeCooldown = 50 + elk.getRandom().nextInt(30);
                // nach dem Stoß ein paar Schritte zurück, dann von vorn
                Vec3 back = elk.position().subtract(target.position()).multiply(1, 0, 1).normalize().scale(0.35);
                elk.setDeltaMovement(elk.getDeltaMovement().add(back.x, 0.0, back.z));
                elk.setYRot(elk.getYRot() + Mth.wrapDegrees(elk.getRandom().nextFloat() * 40.0f - 20.0f));
                this.ticks = 200;
            }
        }
    }
}
