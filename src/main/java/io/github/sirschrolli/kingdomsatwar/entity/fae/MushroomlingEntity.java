package io.github.sirschrolli.kingdomsatwar.entity.fae;

import io.github.sirschrolli.kingdomsatwar.fae.feature.FaeBuild;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Pilzling: ein knapp kniehohes Pilzwesen aus dem Pilzhain. Es tapst zwischen den Riesenpilzen umher, und kommt jemand
 * zu nah, duckt es sich unter seinen Hut und hält ganz still – dann sieht es aus wie ein gewöhnlicher Pilz. Wer es
 * schlägt, bekommt eine Wolke Sporen ins Gesicht (Übelkeit und Langsamkeit), während der Pilzling davonwackelt.
 */
public class MushroomlingEntity extends PathfinderMob {
    public static final int VARIANTS = 3;
    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(MushroomlingEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_HIDING = SynchedEntityData.defineId(MushroomlingEntity.class,
            EntityDataSerializers.BOOLEAN);
    private int puffCooldown;

    public MushroomlingEntity(EntityType<? extends MushroomlingEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.FOLLOW_RANGE, 12.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.45));
        this.goalSelector.addGoal(2, new HideGoal(this));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8, 80));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_HIDING, false);
    }

    public int getVariant() {
        return Mth.clamp(this.entityData.get(DATA_VARIANT), 0, VARIANTS - 1);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, Mth.clamp(variant, 0, VARIANTS - 1));
    }

    public boolean isHiding() {
        return this.entityData.get(DATA_HIDING);
    }

    public void setHiding(boolean hiding) {
        this.entityData.set(DATA_HIDING, hiding);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            if (this.random.nextInt(18) == 0) {
                this.level().addParticle(ModParticles.FAE_SPARK.get(), this.getRandomX(0.6), this.getY(0.9), this.getRandomZ(0.6),
                        0.0, 0.01, 0.0);
            }
        } else if (this.puffCooldown > 0) {
            this.puffCooldown--;
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        this.setHiding(false);
        if (hurt && this.puffCooldown <= 0) {
            this.puffCooldown = 60;
            this.puff(level, source.getEntity());
        }
        return hurt;
    }

    /** Eine Wolke Sporen: wer daneben steht, dem wird schwindlig und schwer. */
    private void puff(ServerLevel level, @Nullable Entity attacker) {
        this.playSound(ModSounds.MUSHROOMLING_PUFF.get(), 0.9f, 0.9f + this.random.nextFloat() * 0.2f);
        level.sendParticles(ModParticles.BOSS_SPORE.get(), this.getX(), this.getY(0.8), this.getZ(), 12, 0.5, 0.3, 0.5, 0.02);
        level.sendParticles(ModParticles.FAE_SPARK.get(), this.getX(), this.getY(0.8), this.getZ(), 10, 0.6, 0.4, 0.6, 0.03);
        for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.5),
                e -> e != this && !(e instanceof MushroomlingEntity))) {
            near.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0), this);
            near.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1), this);
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.setVariant(level.getRandom().nextInt(VARIANTS));
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Variant", this.getVariant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(input.getIntOr("Variant", 0));
    }

    /** Auf Moos und Gras des Feenreichs, egal wie hell. */
    public static boolean checkMushroomlingSpawnRules(EntityType<? extends Mob> type, LevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        return FaeBuild.soil(level.getBlockState(pos.below()));
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isHiding() ? null : ModSounds.MUSHROOMLING_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 260;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MUSHROOMLING_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MUSHROOMLING_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(net.minecraft.sounds.SoundEvents.FUNGUS_STEP, 0.2f, 1.6f);
    }

    @Override
    protected float getSoundVolume() {
        return 0.6f;
    }

    /** Verstecken: kommt ein Spieler (nicht schleichend) zu nah, duckt sich der Pilzling eine Weile unter seinen Hut. */
    static final class HideGoal extends Goal {
        private final MushroomlingEntity mob;
        private int ticks;

        HideGoal(MushroomlingEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (this.mob.getLastHurtByMob() != null || this.mob.getRandom().nextInt(10) != 0) {
                return false;
            }
            Player player = this.mob.level().getNearestPlayer(this.mob, 5.0);
            return player != null && !player.isShiftKeyDown() && !player.isSpectator();
        }

        @Override
        public void start() {
            this.ticks = 60 + this.mob.getRandom().nextInt(100);
            this.mob.getNavigation().stop();
            this.mob.setHiding(true);
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticks > 0 && this.mob.getLastHurtByMob() == null;
        }

        @Override
        public void tick() {
            this.ticks--;
            this.mob.getNavigation().stop();
        }

        @Override
        public void stop() {
            this.mob.setHiding(false);
        }
    }
}
