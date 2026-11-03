package io.github.sirschrolli.kingdomsatwar.entity.creature;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

/**
 * Gans: lebt an Flüssen und auf Wiesen, schwimmt gern und schnattert laut. Wer sie ärgert, wird mit
 * ausgebreiteten Flügeln gejagt und gezwickt. Verliert ab und zu eine Feder; frisst Samen.
 * Zwei Arten: Graugans und weiße Hausgans.
 */
public class GooseEntity extends Animal {
    public static final byte EVENT_HONK = 121;
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(GooseEntity.class, EntityDataSerializers.INT);
    public static final int VARIANTS = 2;

    public final AnimationState honkAnimation = new AnimationState();
    public float flap;
    public float flapSpeed;
    public float oFlap;
    public float oFlapSpeed;
    private int featherTime;
    private float nextFlap = 1.0f;

    public GooseEntity(EntityType<? extends GooseEntity> type, Level level) {
        super(type, level);
        this.featherTime = this.random.nextInt(6000) + 6000;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ATTACK_DAMAGE, 1.5)
                .add(Attributes.FOLLOW_RANGE, 12.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.35, true));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, Mth.clamp(variant, 0, VARIANTS - 1));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        // Wilde Gänse sind meist Graugänse, Hausgänse gibt es vor allem in der Nähe von Dörfern
        this.setVariant(this.random.nextInt(4) == 0 ? 1 : 0);
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Variant", this.getVariant());
        output.putInt("FeatherTime", this.featherTime);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(input.getIntOr("Variant", 0));
        this.featherTime = input.getIntOr("FeatherTime", 6000);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Flügelschlag wie bei Hühnern: in der Luft sanft gleiten
        this.oFlap = this.flap;
        this.oFlapSpeed = this.flapSpeed;
        boolean angry = this.isAggressive();
        this.flapSpeed = Mth.clamp(this.flapSpeed + (this.onGround() && !angry ? -1.0f : 4.0f) * 0.3f, 0.0f, 1.0f);
        Vec3 movement = this.getDeltaMovement();
        if (!this.onGround() && movement.y < 0.0) {
            this.setDeltaMovement(movement.multiply(1.0, 0.6, 1.0));
        }
        this.flap += this.flapSpeed * 2.0f;
        if (this.level() instanceof ServerLevel level && this.isAlive() && !this.isBaby() && --this.featherTime <= 0) {
            this.spawnAtLocation(level, new ItemStack(Items.FEATHER));
            this.gameEvent(GameEvent.ENTITY_PLACE);
            this.featherTime = this.random.nextInt(6000) + 6000;
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_HONK);
        this.playSound(ModSounds.GOOSE_HISS.get(), 1.0f, this.getVoicePitch());
        boolean hit = super.doHurtTarget(level, target);
        // Nach einem Zwicken gibt sie sich meist zufrieden
        if (hit && this.random.nextInt(3) == 0) {
            this.setTarget(null);
        }
        return hit;
    }

    @Override
    public void playAmbientSound() {
        super.playAmbientSound();
        this.level().broadcastEntityEvent(this, EVENT_HONK);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_HONK) {
            this.honkAnimation.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected boolean isFlapping() {
        return this.flyDist > this.nextFlap;
    }

    @Override
    protected void onFlap() {
        this.nextFlap = this.flyDist + this.flapSpeed / 2.0f;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Tags.Items.SEEDS) || stack.is(Items.WHEAT);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        GooseEntity gosling = ModEntities.GOOSE.get().create(level, EntitySpawnReason.BREEDING);
        if (gosling != null) {
            gosling.setVariant(this.random.nextBoolean() ? this.getVariant()
                    : partner instanceof GooseEntity other ? other.getVariant() : this.getVariant());
        }
        return gosling;
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.getTarget() != null ? ModSounds.GOOSE_HISS.get() : ModSounds.GOOSE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.GOOSE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GOOSE_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.CHICKEN_STEP.value(), 0.15f, 0.8f);
    }
}
