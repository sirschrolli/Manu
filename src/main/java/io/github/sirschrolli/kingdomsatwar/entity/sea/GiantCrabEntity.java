package io.github.sirschrolli.kingdomsatwar.entity.sea;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;

/**
 * Riesenkrabbe: eine Strandkrabbe so groß wie ein Hund. Sie läuft seitwärts über Strände, Felsküsten und den Grund
 * flacher Meere, klappert mit den Scheren und lässt sich mit Seetang oder Seegras anlocken und vermehren. Wird sie
 * gereizt, kneift sie mit ihrer großen Knackschere kräftig zu – und ihre Artgenossen kommen ihr zu Hilfe.
 */
public class GiantCrabEntity extends Animal {
    private static final byte EVENT_PINCH = 74;
    private static final byte EVENT_CLACK = 75;

    public final AnimationState pinchAnimation = new AnimationState();
    public final AnimationState clackAnimation = new AnimationState();
    private int clackIn;

    public GiantCrabEntity(EntityType<? extends GiantCrabEntity> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.clackIn = 200 + this.random.nextInt(400);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4)
                .add(Attributes.FOLLOW_RANGE, 12.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.35, true));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, this::isFood, false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.9, 60));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new AmphibiousPathNavigation(this, level);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.KELP) || stack.is(Items.DRIED_KELP) || stack.is(Items.SEAGRASS);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntities.GIANT_CRAB.get().create(level, EntitySpawnReason.BREEDING);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        // ab und zu klappert sie mit den Scheren
        if (--this.clackIn <= 0) {
            this.clackIn = 240 + this.random.nextInt(500);
            if (this.getTarget() == null) {
                level.broadcastEntityEvent(this, EVENT_CLACK);
                this.playSound(ModSounds.GIANT_CRAB_CLACK.get(), 0.7f, this.isBaby() ? 1.5f : 0.95f + this.random.nextFloat() * 0.15f);
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_PINCH);
        boolean hit = super.doHurtTarget(level, target);
        if (hit) {
            this.playSound(ModSounds.GIANT_CRAB_PINCH.get(), 1.0f, 0.9f + this.random.nextFloat() * 0.2f);
        }
        return hit;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_PINCH) {
            this.pinchAnimation.start(this.tickCount);
        } else if (id == EVENT_CLACK) {
            this.clackAnimation.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    /** Am Strand, an Felsküsten und am Grund flacher Meere – auf Sand, Kies oder Fels, nicht weit vom Meeresspiegel. */
    public static boolean checkCrabSpawnRules(EntityType<? extends Mob> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        int sea = level.getSeaLevel();
        BlockState ground = level.getBlockState(pos.below());
        boolean shore = ground.is(BlockTags.SAND) || ground.is(net.minecraft.world.level.block.Blocks.GRAVEL)
                || ground.is(BlockTags.BASE_STONE_OVERWORLD) || ground.is(net.minecraft.world.level.block.Blocks.MUD);
        return shore && pos.getY() >= sea - 8 && pos.getY() <= sea + 4 && (level.getRawBrightness(pos, 0) > 7 || SeaCreatures.isWater(level, pos));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.GIANT_CRAB_CLACK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.GIANT_CRAB_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GIANT_CRAB_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.GIANT_CRAB_STEP.get(), 0.25f, this.isBaby() ? 1.6f : 1.2f);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }

    @Override
    protected float getSoundVolume() {
        return 0.6f;
    }
}
