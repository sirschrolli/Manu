package io.github.sirschrolli.kingdomsatwar.entity.creature.biome;

import io.github.sirschrolli.kingdomsatwar.fishing.FishItem;
import io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Sturmmöwe der Sturmküste (0.85). Möwen kreisen in kleinen Schwärmen über der Küste, landen auf Strand, Fels und Wasser,
 * gehen umher, picken und kreischen. Wer einen gewöhnlichen Fisch in der Hand hält, muss aufpassen: Eine Möwe stößt
 * herab und schnappt ihn sich – einen, nie einen seltenen oder legendären – und fliegt kreischend davon. Trifft man sie,
 * bevor sie ihn gefressen hat, lässt sie ihn fallen. Fliehen, wenn man ihnen zu nahe kommt.
 */
public class StormGullEntity extends SoaringBird {
    public static final byte EVENT_CALL = 100;
    public static final byte EVENT_PECK = 101;
    public static final byte EVENT_SNATCH = 102;
    /** Zwei Arten: die helle Sturmmöwe und die dunkle Mantelmöwe. */
    public static final int VARIANTS = 2;
    public static final int STEAL_COOLDOWN = 2400;
    /** So lange trägt sie die Beute, bevor sie sie frisst. */
    public static final int CARRY_TICKS = 600;

    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(StormGullEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> DATA_CARRIED = SynchedEntityData.defineId(StormGullEntity.class,
            EntityDataSerializers.ITEM_STACK);

    public final AnimationState callAnimation = new AnimationState();
    public final AnimationState peckAnimation = new AnimationState();
    public final AnimationState snatchAnimation = new AnimationState();
    private int stealCooldown;
    private int carryTicks;

    public StormGullEntity(EntityType<? extends StormGullEntity> type, Level level) {
        super(type, level);
        this.stealCooldown = 200 + level.getRandom().nextInt(1200);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FLYING_SPEED, 0.4)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_CARRIED, ItemStack.EMPTY);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FleeGoal());
        this.goalSelector.addGoal(2, new StealGoal());
        this.goalSelector.addGoal(3, new LandGoal());
        this.goalSelector.addGoal(4, new SoarGoal());
        this.goalSelector.addGoal(5, new TakeOffGoal());
        this.goalSelector.addGoal(6, new OnGround(new PeckGoal()));
        this.goalSelector.addGoal(7, new OnGround(new WaterAvoidingRandomStrollGoal(this, 0.8)));
        this.goalSelector.addGoal(8, new OnGround(new LookAtPlayerGoal(this, Player.class, 8.0f)));
        this.goalSelector.addGoal(9, new OnGround(new RandomLookAroundGoal(this)));
    }

    // ------------------------------------------------------------------ Art und Flug

    @Override
    protected double cruiseSpeed() {
        return 0.42;
    }

    @Override
    protected double soarRadius() {
        return 10.0;
    }

    @Override
    protected double soarHeight() {
        return 9.0;
    }

    @Override
    protected int newAirBudget() {
        return 300 + this.random.nextInt(600);
    }

    @Override
    protected int newGroundBudget() {
        return 300 + this.random.nextInt(900);
    }

    @Override
    protected boolean canRestOnWater() {
        return true;
    }

    @Override
    protected boolean isGoodLandingSpot(BlockPos pos, BlockState ground) {
        return ground.is(BlockTags.SAND) || ground.is(BlockTags.BASE_STONE_OVERWORLD) || ground.is(net.minecraft.world.level.block.Blocks.GRAVEL)
                || ground.getFluidState().isSource() || ground.is(BlockTags.LOGS);
    }

    @Override
    protected double landingScore(BlockPos pos, BlockState ground) {
        // lieber erhöht: Felsen, Pfähle, Masten
        return super.landingScore(pos, ground) + (pos.getY() - this.groundY(this.getX(), this.getZ())) * 0.15;
    }

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, Mth.clamp(variant, 0, VARIANTS - 1));
    }

    /** Was sie gerade im Schnabel trägt (ein gestohlener Fisch). */
    public ItemStack getCarried() {
        return this.entityData.get(DATA_CARRIED);
    }

    public void setCarried(ItemStack stack) {
        this.entityData.set(DATA_CARRIED, stack);
    }

    /** Für Tests: wie lange sie noch wartet, bevor sie wieder einen Fisch stiehlt. */
    public void setStealCooldown(int ticks) {
        this.stealCooldown = Math.max(0, ticks);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.setVariant(this.random.nextFloat() < 0.75f ? 0 : 1);
        this.anchor = this.position();
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Variant", this.getVariant());
        output.putInt("StealCooldown", this.stealCooldown);
        if (!this.getCarried().isEmpty()) {
            output.store("Carried", ItemStack.CODEC, this.getCarried());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(input.getIntOr("Variant", 0));
        this.stealCooldown = input.getIntOr("StealCooldown", 0);
        this.setCarried(input.read("Carried", ItemStack.CODEC).orElse(ItemStack.EMPTY));
    }

    // ------------------------------------------------------------------ Takt

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.stealCooldown > 0) {
            this.stealCooldown--;
        }
        if (!this.getCarried().isEmpty() && ++this.carryTicks > CARRY_TICKS) {
            // gefressen
            ((ServerLevel) this.level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, this.getCarried().getItem()), this.getX(),
                    this.getY() + 0.4, this.getZ(), 8, 0.15, 0.1, 0.15, 0.05);
            this.playSound(ModSounds.STORM_GULL_CALL.get(), 1.0f, 1.2f);
            this.setCarried(ItemStack.EMPTY);
            this.carryTicks = 0;
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_CALL -> this.callAnimation.start(this.tickCount);
            case EVENT_PECK -> this.peckAnimation.start(this.tickCount);
            case EVENT_SNATCH -> this.snatchAnimation.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    public void playAmbientSound() {
        super.playAmbientSound();
        if (this.level() instanceof ServerLevel level) {
            level.broadcastEntityEvent(this, EVENT_CALL);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && !this.getCarried().isEmpty()) {
            // vor Schreck fallen gelassen
            this.spawnAtLocation(level, this.getCarried().copy());
            this.setCarried(ItemStack.EMPTY);
            this.carryTicks = 0;
        }
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (!this.getCarried().isEmpty()) {
            this.spawnAtLocation(level, this.getCarried().copy());
            this.setCarried(ItemStack.EMPTY);
        }
    }

    // ------------------------------------------------------------------ Was sie stiehlt

    /** Gewöhnliche Fische (und Fischfilets); seltene, epische und legendäre nie. */
    public static boolean isStealable(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof FishItem fish) {
            FishSpecies.Rarity rarity = fish.species().rarity();
            return rarity == FishSpecies.Rarity.COMMON || rarity == FishSpecies.Rarity.UNCOMMON;
        }
        return stack.is(Items.COD) || stack.is(Items.SALMON) || stack.is(Items.TROPICAL_FISH) || stack.is(Items.COOKED_COD)
                || stack.is(Items.COOKED_SALMON) || (stack.is(ItemTags.FISHES) && !stack.is(Items.PUFFERFISH));
    }

    // ------------------------------------------------------------------ Auftreten

    /** Auf Sand, Kies und Fels der Küsten, wo es hell ist. */
    public static boolean checkGullSpawnRules(EntityType<StormGullEntity> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        boolean ground = below.is(BlockTags.SAND) || below.is(BlockTags.BASE_STONE_OVERWORLD) || below.is(net.minecraft.world.level.block.Blocks.GRAVEL)
                || below.is(BlockTags.ANIMALS_SPAWNABLE_ON);
        return ground && (EntitySpawnReason.ignoresLightRequirements(reason) || level.getRawBrightness(pos, 0) > 8);
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.STORM_GULL_CALL.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.STORM_GULL_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.STORM_GULL_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.STORM_GULL_STEP.get(), 0.15f, 1.0f);
    }

    @Override
    protected void onTakeOff() {
        this.playSound(ModSounds.STORM_GULL_FLAP.get(), 0.6f, 1.0f + this.random.nextFloat() * 0.2f);
    }

    // =====================================================================================
    // Ziele
    // =====================================================================================

    /** Flieht vor Spielern, die näher als vier Blöcke kommen (außer schleichend). */
    final class FleeGoal extends Goal {
        @Override
        public boolean canUse() {
            StormGullEntity gull = StormGullEntity.this;
            if (gull.isFlying() || gull.busy) {
                return false;
            }
            Player player = gull.level().getNearestPlayer(gull, 4.0);
            return player != null && !player.isDiscrete() && !player.isSpectator();
        }

        @Override
        public void start() {
            StormGullEntity.this.takeOff();
            StormGullEntity.this.playSound(ModSounds.STORM_GULL_CALL.get(), 1.0f, 1.25f);
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    /** Stößt auf einen Spieler herab, der einen gewöhnlichen Fisch in der Hand hält, und schnappt ihn sich. */
    final class StealGoal extends Goal {
        private @Nullable Player victim;
        private int ticks;

        StealGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            StormGullEntity gull = StormGullEntity.this;
            if (gull.stealCooldown > 0 || !gull.getCarried().isEmpty() || gull.isBaby() || gull.random.nextInt(20) != 0) {
                return false;
            }
            Player player = gull.level().getNearestPlayer(gull.getX(), gull.getY(), gull.getZ(), 16.0,
                    e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() && isStealable(p.getMainHandItem()));
            if (player == null || !gull.hasLineOfSight(player)) {
                return false;
            }
            this.victim = player;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            Player p = this.victim;
            return p != null && p.isAlive() && this.ticks < 240 && isStealable(p.getMainHandItem())
                    && StormGullEntity.this.getCarried().isEmpty() && StormGullEntity.this.distanceToSqr(p) < 32.0 * 32.0;
        }

        @Override
        public void start() {
            StormGullEntity gull = StormGullEntity.this;
            this.ticks = 0;
            gull.busy = true;
            if (!gull.isFlying()) {
                gull.takeOff();
            }
            gull.playSound(ModSounds.STORM_GULL_CALL.get(), 1.2f, 1.1f);
            gull.level().broadcastEntityEvent(gull, EVENT_CALL);
        }

        @Override
        public void stop() {
            StormGullEntity gull = StormGullEntity.this;
            gull.busy = false;
            this.victim = null;
            if (gull.stealCooldown <= 0) {
                gull.stealCooldown = 400;                                    // nicht geklappt: gleich noch mal? lieber nicht
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            StormGullEntity gull = StormGullEntity.this;
            Player p = this.victim;
            if (p == null) {
                return;
            }
            this.ticks++;
            // auf die Hand zu: vorn rechts vor dem Spieler, in Brusthöhe
            Vec3 look = p.getLookAngle();
            Vec3 hand = p.position().add(look.x * 0.6, 1.1, look.z * 0.6);
            gull.flyTo(hand.x, hand.y, hand.z, 1.35);
            gull.getLookControl().setLookAt(p);
            if (gull.position().add(0, 0.3, 0).distanceToSqr(hand) < 1.4 * 1.4) {
                ItemStack stack = p.getMainHandItem();
                if (isStealable(stack)) {
                    gull.setCarried(stack.split(1));
                    gull.carryTicks = 0;
                    gull.stealCooldown = STEAL_COOLDOWN + gull.random.nextInt(STEAL_COOLDOWN);
                    gull.level().broadcastEntityEvent(gull, EVENT_SNATCH);
                    gull.playSound(ModSounds.STORM_GULL_CALL.get(), 1.4f, 1.3f);
                    // davon: hoch und weg vom Spieler
                    Vec3 away = gull.position().subtract(p.position()).multiply(1, 0, 1).normalize();
                    gull.setAnchor(gull.position().add(away.scale(24.0)).add(0, 6, 0));
                    gull.setDeltaMovement(gull.getDeltaMovement().add(away.x * 0.3, 0.35, away.z * 0.3));
                    gull.airBudget = gull.airTicks + 400;
                }
                this.ticks = 999;
            }
        }
    }

    /** Am Boden: pickt nach Muscheln und Krümeln. */
    final class PeckGoal extends Goal {
        private int ticks;

        PeckGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return StormGullEntity.this.onGround() && StormGullEntity.this.random.nextInt(reducedTickDelay(160)) == 0;
        }

        @Override
        public void start() {
            this.ticks = 20 + StormGullEntity.this.random.nextInt(30);
            StormGullEntity.this.getNavigation().stop();
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticks > 0;
        }

        @Override
        public void tick() {
            if (--this.ticks % 10 == 0 && this.ticks > 0) {
                StormGullEntity.this.level().broadcastEntityEvent(StormGullEntity.this, EVENT_PECK);
            }
        }
    }
}
