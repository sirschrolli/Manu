package io.github.sirschrolli.kingdomsatwar.entity.creature.biome;

import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Flamingo der Korallenküste (0.86). Flamingos leben in Scharen und waten im flachen Wasser; sie seihen mit dem Kopf nach
 * unten das Wasser durch, stehen beim Ruhen auf einem Bein und stecken nachts den Kopf ins Gefieder. Kommt man ihnen zu
 * nahe, laufen sie davon – und flattern auf, wenn man ihnen dicht auf den Fersen ist: mit vorgestrecktem Hals und nach
 * hinten gestreckten Beinen, rote und schwarze Flügel. Sie landen bald wieder, am liebsten im flachen Wasser.
 */
public class FlamingoEntity extends SoaringBird {
    public static final byte EVENT_CALL = 100;
    /** Zwei Arten: der Rosaflamingo und der kräftig rotrosa Kubaflamingo. */
    public static final int VARIANTS = 2;

    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(FlamingoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_FEEDING = SynchedEntityData.defineId(FlamingoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_RESTING = SynchedEntityData.defineId(FlamingoEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState callAnimation = new AnimationState();

    public FlamingoEntity(EntityType<? extends FlamingoEntity> type, Level level) {
        super(type, level);
        // watet durch flaches Wasser
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.FLYING_SPEED, 0.4)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_FEEDING, false);
        builder.define(DATA_RESTING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new StartleGoal());
        this.goalSelector.addGoal(2, new OnGround(new AvoidEntityGoal<>(this, Player.class, 7.0f, 1.1, 1.45,
                e -> !e.isDiscrete() && !(e instanceof Player p && (p.isSpectator() || p.isCreative())))));
        this.goalSelector.addGoal(3, new LandGoal());
        this.goalSelector.addGoal(4, new SoarGoal());
        this.goalSelector.addGoal(5, new TakeOffGoal());
        this.goalSelector.addGoal(6, new OnGround(new FeedGoal()));
        this.goalSelector.addGoal(6, new OnGround(new RestGoal()));
        this.goalSelector.addGoal(7, new OnGround(new FlockGoal()));
        this.goalSelector.addGoal(8, new OnGround(new RandomStrollGoal(this, 0.75, 160)));
        this.goalSelector.addGoal(9, new OnGround(new LookAtPlayerGoal(this, Player.class, 8.0f)));
        this.goalSelector.addGoal(10, new OnGround(new RandomLookAroundGoal(this)));
    }

    // ------------------------------------------------------------------ Art und Flug

    @Override
    protected double cruiseSpeed() {
        return 0.42;
    }

    @Override
    protected double soarRadius() {
        return 7.0;
    }

    @Override
    protected double soarHeight() {
        return 6.0;
    }

    @Override
    protected int newAirBudget() {
        return 80 + this.random.nextInt(140);
    }

    @Override
    protected int newGroundBudget() {
        return 4000 + this.random.nextInt(8000);
    }

    @Override
    protected boolean canRestOnWater() {
        return true;
    }

    @Override
    protected boolean isGoodLandingSpot(BlockPos pos, BlockState ground) {
        if (ground.getFluidState().isSource()) {
            // nur flaches Wasser (darunter fester Grund)
            return this.level().getBlockState(pos.below(2)).isSolid();
        }
        return ground.is(BlockTags.SAND) || ground.is(BlockTags.DIRT) || ground.is(Blocks.MUD) || ground.is(Blocks.GRAVEL);
    }

    @Override
    protected double landingScore(BlockPos pos, BlockState ground) {
        return super.landingScore(pos, ground) + (ground.getFluidState().isSource() ? 3.0 : 0.0);
    }

    /** Erst ab 1,1 Blöcken Tiefe schwimmt er – sonst steht er im Wasser. */
    @Override
    public double getFluidJumpThreshold() {
        return 1.1;
    }

    // ------------------------------------------------------------------ Zustand

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, Mth.clamp(variant, 0, VARIANTS - 1));
    }

    /** Seiht mit dem Kopf nach unten das Wasser durch. */
    public boolean isFeeding() {
        return this.entityData.get(DATA_FEEDING);
    }

    public void setFeeding(boolean feeding) {
        this.entityData.set(DATA_FEEDING, feeding);
    }

    /** Steht auf einem Bein (nachts mit dem Kopf im Gefieder). */
    public boolean isResting() {
        return this.entityData.get(DATA_RESTING);
    }

    public void setResting(boolean resting) {
        this.entityData.set(DATA_RESTING, resting);
    }

    @Override
    public void setFlying(boolean flying) {
        super.setFlying(flying);
        if (flying) {
            this.setFeeding(false);
            this.setResting(false);
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        // eine Schar ist eine Art
        if (groupData instanceof FlockData flock) {
            this.setVariant(flock.variant);
        } else {
            int variant = this.random.nextFloat() < 0.75f ? 0 : 1;
            this.setVariant(variant);
            groupData = new FlockData(variant);
        }
        this.anchor = this.position();
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    /** Eine Schar ist eine Art; als Altersgruppe, weil {@link AgeableMob} die Gruppendaten als solche erwartet. */
    private static final class FlockData extends AgeableMob.AgeableMobGroupData {
        private final int variant;

        private FlockData(int variant) {
            super(false);
            this.variant = variant;
        }
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

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_CALL) {
            this.callAnimation.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void playAmbientSound() {
        super.playAmbientSound();
        if (this.level() instanceof ServerLevel level && !this.isFeeding()) {
            level.broadcastEntityEvent(this, EVENT_CALL);
        }
    }

    @Override
    protected void onTakeOff() {
        this.playSound(ModSounds.FLAMINGO_FLAP.get(), 0.8f, 0.95f + this.random.nextFloat() * 0.1f);
    }

    // ------------------------------------------------------------------ Auftreten

    /** An Ufern und im flachen Wasser, wo es hell ist. */
    public static boolean checkFlamingoSpawnRules(EntityType<FlamingoEntity> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        boolean ground = below.is(BlockTags.SAND) || below.is(BlockTags.DIRT) || below.is(Blocks.MUD) || below.is(BlockTags.ANIMALS_SPAWNABLE_ON);
        return ground && (EntitySpawnReason.ignoresLightRequirements(reason) || level.getRawBrightness(pos, 0) > 8);
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.FLAMINGO_CALL.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.FLAMINGO_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.FLAMINGO_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.FLAMINGO_STEP.get(), this.isInWater() ? 0.35f : 0.15f, 1.0f);
    }

    // =====================================================================================
    // Ziele
    // =====================================================================================

    /** Wer ihm dicht auf den Fersen ist (unter vier Blöcken, ohne zu schleichen), vor dem flattert er auf. */
    final class StartleGoal extends Goal {
        @Override
        public boolean canUse() {
            FlamingoEntity bird = FlamingoEntity.this;
            if (bird.isFlying() || bird.busy || bird.isBaby()) {
                return false;
            }
            Player player = bird.level().getNearestPlayer(bird, 3.5);
            return player != null && !player.isDiscrete() && !player.isSpectator() && !player.isCreative();
        }

        @Override
        public void start() {
            FlamingoEntity bird = FlamingoEntity.this;
            bird.takeOff();
            bird.playSound(ModSounds.FLAMINGO_CALL.get(), 1.0f, 1.15f);
            bird.level().broadcastEntityEvent(bird, EVENT_CALL);
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    /** Im flachen Wasser: Kopf nach unten, das Wasser durchseihen. */
    final class FeedGoal extends Goal {
        private int ticks;

        FeedGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            FlamingoEntity bird = FlamingoEntity.this;
            return bird.isInWater() && !bird.isResting() && bird.getRandom().nextInt(reducedTickDelay(120)) == 0;
        }

        @Override
        public void start() {
            this.ticks = 80 + FlamingoEntity.this.getRandom().nextInt(160);
            FlamingoEntity.this.getNavigation().stop();
            FlamingoEntity.this.setFeeding(true);
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticks > 0 && FlamingoEntity.this.isInWater();
        }

        @Override
        public void tick() {
            this.ticks--;
        }

        @Override
        public void stop() {
            FlamingoEntity.this.setFeeding(false);
        }
    }

    /** Auf einem Bein ruhen (nachts lange, mit dem Kopf im Gefieder). */
    final class RestGoal extends Goal {
        private int ticks;

        RestGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            FlamingoEntity bird = FlamingoEntity.this;
            boolean night = !bird.level().isBrightOutside();
            return !bird.isFeeding() && (night || bird.getRandom().nextInt(reducedTickDelay(400)) == 0);
        }

        @Override
        public void start() {
            FlamingoEntity bird = FlamingoEntity.this;
            this.ticks = bird.level().isBrightOutside() ? 200 + bird.getRandom().nextInt(400) : 1200;
            bird.getNavigation().stop();
            bird.setResting(true);
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticks > 0;
        }

        @Override
        public void tick() {
            this.ticks--;
        }

        @Override
        public void stop() {
            FlamingoEntity.this.setResting(false);
        }
    }

    /** Allein? Dann zur nächsten Schar hin. */
    final class FlockGoal extends Goal {
        private @Nullable FlamingoEntity mate;

        FlockGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            FlamingoEntity bird = FlamingoEntity.this;
            if (bird.getRandom().nextInt(reducedTickDelay(80)) != 0 || bird.isResting() || bird.isFeeding()) {
                return false;
            }
            List<FlamingoEntity> near = bird.level().getEntitiesOfClass(FlamingoEntity.class, bird.getBoundingBox().inflate(20.0, 6.0, 20.0),
                    e -> e != bird);
            if (near.isEmpty() || near.stream().anyMatch(e -> e.distanceToSqr(bird) < 5.0 * 5.0)) {
                return false;
            }
            this.mate = near.stream().min((a, b) -> Double.compare(a.distanceToSqr(bird), b.distanceToSqr(bird))).orElse(null);
            return this.mate != null;
        }

        @Override
        public void start() {
            if (this.mate != null) {
                FlamingoEntity.this.getNavigation().moveTo(this.mate, 0.9);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.mate != null && this.mate.isAlive() && !FlamingoEntity.this.getNavigation().isDone()
                    && FlamingoEntity.this.distanceToSqr(this.mate) > 3.0 * 3.0;
        }

        @Override
        public void stop() {
            this.mate = null;
            FlamingoEntity.this.getNavigation().stop();
        }
    }
}
