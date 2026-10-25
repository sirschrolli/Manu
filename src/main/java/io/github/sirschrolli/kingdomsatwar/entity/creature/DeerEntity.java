package io.github.sirschrolli.kingdomsatwar.entity.creature;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Rothirsch: scheu und schnell. Flieht vor Spielern, die nicht schleichen; mit Äpfeln oder Weizen lässt er sich
 * anlocken und vermehren. Männliche Tiere tragen ein Geweih, Kitze ein getupftes Fell. Grast gern.
 */
public class DeerEntity extends Animal {
    public static final byte EVENT_GRAZE = 10;
    private static final EntityDataAccessor<Boolean> DATA_STAG =
            SynchedEntityData.defineId(DeerEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState grazeAnimation = new AnimationState();

    public DeerEntity(EntityType<? extends DeerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.SAFE_FALL_DISTANCE, 5.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STAG, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, this::isFood, false));
        // Scheu: weicht Spielern aus, die nicht schleichen und kein Futter in der Hand halten
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Player.class, 10.0f, 1.5, 2.0,
                entity -> entity instanceof Player player && !player.isDiscrete()
                        && !this.isFood(player.getMainHandItem()) && !this.isFood(player.getOffhandItem())));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(6, new EatBlockGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    /** Männliches Tier mit Geweih. */
    public boolean isStag() {
        return this.entityData.get(DATA_STAG);
    }

    public void setStag(boolean stag) {
        this.entityData.set(DATA_STAG, stag);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.setStag(this.random.nextBoolean());
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Stag", this.isStag());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setStag(input.getBooleanOr("Stag", false));
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_GRAZE) {
            this.grazeAnimation.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void ate() {
        super.ate();
        if (this.isBaby()) {
            this.ageUp(60);
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.APPLE) || stack.is(Items.WHEAT) || stack.is(Items.SWEET_BERRIES);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        DeerEntity fawn = ModEntities.DEER.get().create(level, EntitySpawnReason.BREEDING);
        if (fawn != null) {
            fawn.setStag(this.random.nextBoolean());
        }
        return fawn;
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.DEER_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.DEER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DEER_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.DEER_STEP.get(), 0.15f, 1.0f);
    }
}
