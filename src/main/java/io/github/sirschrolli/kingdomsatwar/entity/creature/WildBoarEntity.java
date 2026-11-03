package io.github.sirschrolli.kingdomsatwar.entity.creature;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Wildschwein: lebt in Rotten im Wald. Friedlich, solange man es in Ruhe lässt – wird eines angegriffen,
 * stürmt die ganze Rotte mit gesenktem Kopf auf den Angreifer los. Lässt sich mit Karotten, Kartoffeln
 * und Roter Bete anlocken und vermehren; Frischlinge tragen helle Streifen.
 */
public class WildBoarEntity extends Animal {
    public static final byte EVENT_ATTACK = 120;
    private static final EntityDataAccessor<Boolean> DATA_CHARGING =
            SynchedEntityData.defineId(WildBoarEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState attackAnimation = new AnimationState();

    public WildBoarEntity(EntityType<? extends WildBoarEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CHARGING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.45, true));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.15, this::isFood, false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    /** Stürmt gerade auf ein Ziel los (gesenkter Kopf). */
    public boolean isCharging() {
        return this.entityData.get(DATA_CHARGING);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            LivingEntity target = this.getTarget();
            boolean charging = target != null && target.isAlive() && !this.isBaby();
            if (charging != this.isCharging()) {
                this.entityData.set(DATA_CHARGING, charging);
            }
            // Frischlinge kämpfen nicht – sie fliehen nur
            if (this.isBaby() && target != null) {
                this.setTarget(null);
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_ATTACK);
        this.playSound(ModSounds.WILD_BOAR_ANGRY.get(), 1.0f, this.getVoicePitch());
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            // Hochschleudern mit den Hauern
            living.push(0.0, 0.25, 0.0);
        }
        return hit;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ATTACK) {
            this.attackAnimation.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.CARROT) || stack.is(Items.POTATO) || stack.is(Items.BEETROOT) || stack.is(Items.SWEET_BERRIES);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntities.WILD_BOAR.get().create(level, EntitySpawnReason.BREEDING);
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isCharging() ? ModSounds.WILD_BOAR_ANGRY.get() : ModSounds.WILD_BOAR_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WILD_BOAR_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WILD_BOAR_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.WILD_BOAR_STEP.get(), 0.2f, 1.0f);
    }
}
