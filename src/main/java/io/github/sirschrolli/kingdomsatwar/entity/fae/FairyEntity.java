package io.github.sirschrolli.kingdomsatwar.entity.fae;

import io.github.sirschrolli.kingdomsatwar.fae.FaeBiomes;
import io.github.sirschrolli.kingdomsatwar.fae.feature.FaeBuild;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Fee: ein kleines, leuchtendes Wesen mit Schmetterlingsflügeln, das über den Blumen des Feenreichs flattert und eine
 * Spur aus Funken hinter sich herzieht. Scheu – wer ihr aber eine Blume hinhält, dem kommt sie entgegen; schenkt man
 * sie ihr, tanzt sie vor Freude einen Wirbel und lässt Feenstaub fallen (danach eine Weile nicht mehr).
 * Vier Gestalten: Rosenfee, Himmelsfee, Goldfee, Veilchenfee.
 */
public class FairyEntity extends PathfinderMob {
    public static final int VARIANTS = 4;
    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(FairyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_DANCE = SynchedEntityData.defineId(FairyEntity.class, EntityDataSerializers.INT);
    /** Wie lange der Freudentanz dauert (Ticks). */
    public static final int DANCE_TICKS = 50;
    private int giftCooldown;

    public FairyEntity(EntityType<? extends FairyEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.FLYING_SPEED, 0.45)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.TEMPT_RANGE, 10.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new TemptGoal(this, 1.15, FairyEntity::isGift, false, 1.8));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, p -> !holdsGift(p), 5.0f, 1.0, 1.35,
                net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR::test));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));
    }

    private static boolean isGift(ItemStack stack) {
        return stack.is(net.minecraft.tags.BlockItemTags.SMALL_FLOWERS.item());
    }

    private static boolean holdsGift(LivingEntity entity) {
        return isGift(entity.getMainHandItem()) || isGift(entity.getOffhandItem());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_DANCE, 0);
    }

    public int getVariant() {
        return Mth.clamp(this.entityData.get(DATA_VARIANT), 0, VARIANTS - 1);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, Mth.clamp(variant, 0, VARIANTS - 1));
    }

    /** Verbleibende Ticks des Freudentanzes (0 = tanzt nicht). */
    public int getDance() {
        return this.entityData.get(DATA_DANCE);
    }

    @Override
    public void travel(Vec3 input) {
        this.travelFlying(input, this.getSpeed());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Level level = this.level();
        if (level.isClientSide()) {
            if (this.random.nextInt(4) == 0) {
                level.addParticle(ModParticles.FAE_SPARK.get(), this.getRandomX(0.5), this.getY(0.3 + this.random.nextDouble() * 0.6),
                        this.getRandomZ(0.5), 0.0, -0.01, 0.0);
            }
            if (this.getDance() > 0 && this.random.nextInt(2) == 0) {
                level.addParticle(ModParticles.FAE_SPARK.get(), this.getRandomX(1.2), this.getY(0.5), this.getRandomZ(1.2), 0.0, 0.05, 0.0);
            }
            return;
        }
        if (this.giftCooldown > 0) {
            this.giftCooldown--;
        }
        int dance = this.getDance();
        if (dance > 0) {
            this.entityData.set(DATA_DANCE, dance - 1);
            this.getNavigation().stop();
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isGift(stack)) {
            return super.mobInteract(player, hand);
        }
        if (this.giftCooldown > 0) {
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(0.6), this.getZ(), 3, 0.15, 0.15, 0.15, 0.0);
            }
            return InteractionResult.CONSUME;
        }
        if (this.level() instanceof ServerLevel server) {
            stack.consume(1, player);
            this.giftCooldown = 2400 + this.random.nextInt(1200);
            this.entityData.set(DATA_DANCE, DANCE_TICKS);
            this.playSound(ModSounds.FAIRY_GIFT.get(), 0.9f, 1.0f);
            server.sendParticles(ParticleTypes.HEART, this.getX(), this.getY(1.0), this.getZ(), 3, 0.25, 0.2, 0.25, 0.0);
            server.sendParticles(ModParticles.FAE_SPARK.get(), this.getX(), this.getY(0.5), this.getZ(), 24, 0.4, 0.4, 0.4, 0.02);
            ItemEntity dust = new ItemEntity(server, this.getX(), this.getY(0.4), this.getZ(), new ItemStack(FaeBlocks.FAE_DUST.get()));
            dust.setDeltaMovement((player.getX() - this.getX()) * 0.08, 0.15, (player.getZ() - this.getZ()) * 0.08);
            server.addFreshEntity(dust);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.setVariant(pickVariant(level.getBiome(this.blockPosition()), level.getRandom()));
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    /** Auf den Wiesen Rosen- und Goldfeen, im Glimmerwald Rosen- und Himmelsfeen, im Pilzhain Veilchenfeen, an den Seen Himmelsfeen. */
    private static int pickVariant(Holder<Biome> biome, RandomSource random) {
        if (biome.is(FaeBiomes.FAE_MEADOWS)) {
            return random.nextBoolean() ? 0 : 2;
        }
        if (biome.is(FaeBiomes.GLIMMERWOOD)) {
            return random.nextBoolean() ? 0 : 1;
        }
        if (biome.is(FaeBiomes.MUSHROOM_GROVE)) {
            return random.nextInt(4) == 0 ? 0 : 3;
        }
        if (biome.is(FaeBiomes.MOONLIT_LAKES)) {
            return random.nextInt(4) == 0 ? 3 : 1;
        }
        return random.nextInt(VARIANTS);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Variant", this.getVariant());
        output.putInt("GiftCooldown", this.giftCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(input.getIntOr("Variant", 0));
        this.giftCooldown = input.getIntOr("GiftCooldown", 0);
    }

    /** Auf Gras und Moos des Feenreichs, egal wie hell. */
    public static boolean checkFairySpawnRules(EntityType<? extends Mob> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        BlockState ground = level.getBlockState(pos.below());
        return FaeBuild.soil(ground) && level.getBlockState(pos).isAir();
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.FAIRY_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 220;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.FAIRY_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.FAIRY_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.55f;
    }
}
