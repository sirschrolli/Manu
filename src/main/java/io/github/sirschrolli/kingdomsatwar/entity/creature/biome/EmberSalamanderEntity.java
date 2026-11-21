package io.github.sirschrolli.kingdomsatwar.entity.creature.biome;

import io.github.sirschrolli.kingdomsatwar.registry.BiomeBeasts;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Glutsalamander der Aschefelder (0.86). Er ist feuerfest, läuft über Lava wie über festen Boden und sonnt sich gern an
 * Lava und Magma – dann glüht er heller. Er ist scheu und huscht davon, wenn man sich nähert (außer man schleicht); wer
 * ihn trotzdem schlägt, verbrennt sich an seiner glühenden Haut. Wasser verträgt er nicht. Mit Magmacreme lockt man ihn
 * an und zieht Junge auf.
 */
public class EmberSalamanderEntity extends Animal {
    /** Zwei Arten: der orangerot glühende Glutsalamander und der gelb glühende Goldsalamander. */
    public static final int VARIANTS = 2;

    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(EmberSalamanderEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_BASKING = SynchedEntityData.defineId(EmberSalamanderEntity.class,
            EntityDataSerializers.BOOLEAN);

    public EmberSalamanderEntity(EntityType<? extends EmberSalamanderEntity> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.LAVA, 0.0f);
        this.setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, 0.0f);
        this.setPathfindingMalus(PathType.FIRE, 0.0f);
        this.setPathfindingMalus(PathType.WATER, -1.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_BASKING, false);
    }

    @Override
    protected void registerGoals() {
        // schwimmen nur im Wasser (auf Lava läuft er)
        this.goalSelector.addGoal(0, new FloatGoal(this) {
            @Override
            public boolean canUse() {
                return EmberSalamanderEntity.this.isInWater() && super.canUse();
            }
        });
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, stack -> stack.is(Items.MAGMA_CREAM), false));
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Player.class, 6.0f, 1.1, 1.5,
                e -> !e.isDiscrete() && !(e instanceof Player p && (p.isSpectator() || p.isCreative()))));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(6, new BaskGoal());
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.85, 100));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    /** Läuft über Lava (wie ein Schreiter): ein Wegfinder, für den Lava fester Boden ist. */
    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level) {
            @Override
            protected PathFinder createPathFinder(int maxVisitedNodes) {
                this.nodeEvaluator = new WalkNodeEvaluator();
                return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
            }

            @Override
            public boolean isStableDestination(BlockPos pos) {
                return this.level.getBlockState(pos).is(Blocks.LAVA) || super.isStableDestination(pos);
            }
        };
    }

    @Override
    public boolean canStandOnFluid(FluidState fluid) {
        return fluid.is(FluidTags.LAVA);
    }

    @Override
    public void tick() {
        super.tick();
        this.floatOnLava();
    }

    /** Auf Lava steht er einen halben Block tief (wie ein Schreiter). */
    @Override
    public VoxelShape getLiquidCollisionShape() {
        return Block.column(16.0, 0.0, 8.0);
    }

    /** Wie ein Schreiter: in der Lava treibt er nach oben und steht dann auf ihr. */
    private void floatOnLava() {
        if (this.isInLava()) {
            CollisionContext context = CollisionContext.of(this);
            if (context.isAbove(this.getLiquidCollisionShape(), this.blockPosition(), true)
                    && !this.level().getFluidState(this.blockPosition().above()).is(FluidTags.LAVA)) {
                this.setOnGround(true);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5).add(0.0, 0.05, 0.0));
            }
        }
    }

    // ------------------------------------------------------------------ Zustand

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, Mth.clamp(variant, 0, VARIANTS - 1));
    }

    /** Liegt flach und sonnt sich an der Glut (glüht heller). */
    public boolean isBasking() {
        return this.entityData.get(DATA_BASKING);
    }

    public void setBasking(boolean basking) {
        this.entityData.set(DATA_BASKING, basking);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.setVariant(this.random.nextFloat() < 0.75f ? 0 : 1);
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

    // ------------------------------------------------------------------ Takt

    @Override
    public void aiStep() {
        super.aiStep();
        Level level = this.level();
        if (level.isClientSide()) {
            // Funken und Rauch aus der glühenden Haut, im Sonnen mehr
            float chance = this.isBasking() ? 0.18f : 0.06f;
            if (this.random.nextFloat() < chance) {
                level.addParticle(this.random.nextInt(3) == 0 ? ParticleTypes.SMOKE : ParticleTypes.SMALL_FLAME,
                        this.getRandomX(0.4), this.getY() + 0.3, this.getRandomZ(0.4), 0.0, 0.02, 0.0);
            }
            if (this.isInLava() && this.random.nextInt(40) == 0) {
                level.addParticle(ParticleTypes.LAVA, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
            return;
        }
        // Wasser löscht seine Glut: es zischt und schmerzt
        if (this.isInWater() && this.tickCount % 20 == 0 && level instanceof ServerLevel server) {
            this.hurtServer(server, this.damageSources().drown(), 1.0f);
            server.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.3, this.getZ(), 4, 0.2, 0.1, 0.2, 0.01);
            this.playSound(net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, 0.5f, 1.4f);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        // wer ihn mit der Hand (oder einer Waffe) schlägt, verbrennt sich
        if (hurt && source.getDirectEntity() instanceof LivingEntity attacker && attacker.distanceToSqr(this) < 4.0 * 4.0
                && !attacker.fireImmune()) {
            attacker.igniteForSeconds(4.0f);
            this.playSound(ModSounds.EMBER_SALAMANDER_IGNITE.get(), 0.9f, 1.0f + this.random.nextFloat() * 0.2f);
            level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.25, this.getZ(), 10, 0.3, 0.15, 0.3, 0.03);
        }
        if (hurt) {
            this.setBasking(false);
        }
        return hurt;
    }

    // ------------------------------------------------------------------ Zucht

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.MAGMA_CREAM);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        EmberSalamanderEntity baby = BiomeBeasts.EMBER_SALAMANDER.get().create(level, EntitySpawnReason.BREEDING);
        if (baby != null) {
            baby.setVariant(this.random.nextBoolean() ? this.getVariant()
                    : partner instanceof EmberSalamanderEntity other ? other.getVariant() : this.getVariant());
        }
        return baby;
    }

    // ------------------------------------------------------------------ Auftreten

    /** Auf Fels, Asche und Magma der Aschefelder (auch im Dunkeln). */
    public static boolean checkSalamanderSpawnRules(EntityType<EmberSalamanderEntity> type, LevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.BASE_STONE_OVERWORLD) || below.is(Blocks.MAGMA_BLOCK) || below.is(BlockTags.DIRT) || below.is(Blocks.GRAVEL)
                || below.is(Blocks.BASALT) || below.is(Blocks.SMOOTH_BASALT) || below.is(Blocks.BLACKSTONE) || below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || below.isSolidRender();
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.EMBER_SALAMANDER_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.EMBER_SALAMANDER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.EMBER_SALAMANDER_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.EMBER_SALAMANDER_STEP.get(), 0.25f, 1.0f);
    }

    // =====================================================================================
    // Ziele
    // =====================================================================================

    /** An Lava, Magma oder Feuer liegt er flach und sonnt sich; dabei glüht er heller. */
    final class BaskGoal extends Goal {
        private int ticks;

        BaskGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            EmberSalamanderEntity me = EmberSalamanderEntity.this;
            return me.onGround() && !me.isBaby() && me.getRandom().nextInt(reducedTickDelay(200)) == 0 && this.nearHeat();
        }

        private boolean nearHeat() {
            EmberSalamanderEntity me = EmberSalamanderEntity.this;
            if (me.isInLava() || me.getInBlockState().is(Blocks.LAVA)) {
                return true;
            }
            return BlockPos.betweenClosedStream(me.blockPosition().offset(-3, -2, -3), me.blockPosition().offset(3, 1, 3))
                    .anyMatch(p -> {
                        BlockState s = me.level().getBlockState(p);
                        return s.is(Blocks.LAVA) || s.is(Blocks.MAGMA_BLOCK) || s.is(BlockTags.FIRE) || s.is(BlockTags.CAMPFIRES);
                    });
        }

        @Override
        public void start() {
            this.ticks = 200 + EmberSalamanderEntity.this.getRandom().nextInt(500);
            EmberSalamanderEntity.this.getNavigation().stop();
            EmberSalamanderEntity.this.setBasking(true);
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticks > 0 && EmberSalamanderEntity.this.getLastHurtByMob() == null;
        }

        @Override
        public void tick() {
            this.ticks--;
        }

        @Override
        public void stop() {
            EmberSalamanderEntity.this.setBasking(false);
        }
    }
}
