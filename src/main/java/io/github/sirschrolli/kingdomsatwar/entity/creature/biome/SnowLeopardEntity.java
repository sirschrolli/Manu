package io.github.sirschrolli.kingdomsatwar.entity.creature.biome;

import io.github.sirschrolli.kingdomsatwar.registry.BiomeBeasts;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
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
 * Schneeleopard der Frostgipfel (0.85). Scheu: Wer nicht schleicht und kein Fleisch in der Hand hält, dem weicht er aus.
 * Er jagt Ziegen und Hasen – erst schleicht er sich geduckt heran, dann setzt er zum Sprung an und fällt die Beute aus
 * mehreren Blöcken Entfernung an; danach frisst er und jagt eine Weile nicht. Über Pulverschnee läuft er, ohne
 * einzusinken, und friert nicht. Mit rohem Hammel-, Kaninchen- oder Wildfleisch lässt er sich zähmen (jedes dritte Mal):
 * Dann folgt er seinem Herrn, legt sich auf Befehl hin, kämpft an seiner Seite – mit demselben Sprung – und bekommt
 * mit Fleisch Junge.
 */
public class SnowLeopardEntity extends TamableAnimal {
    public static final byte EVENT_POUNCE = 100;
    public static final byte EVENT_SWIPE = 101;
    public static final byte EVENT_EAT = 102;
    /** Zwei Fellfarben: hell (häufiger) und rauchgrau. */
    public static final int VARIANTS = 2;
    /** Ab dieser Entfernung (Blöcke) setzt er zum Sprung an, bis hinunter zu {@link #POUNCE_MIN}. */
    public static final double POUNCE_MAX = 7.0;
    public static final double POUNCE_MIN = 3.0;
    public static final int POUNCE_COOLDOWN = 70;
    /** Nach einer erlegten Beute jagt er so lange nicht (Takte). */
    public static final int FULL_TICKS = 3600;

    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(SnowLeopardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_STALKING = SynchedEntityData.defineId(SnowLeopardEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_RESTING = SynchedEntityData.defineId(SnowLeopardEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState pounceAnimation = new AnimationState();
    public final AnimationState swipeAnimation = new AnimationState();
    public final AnimationState eatAnimation = new AnimationState();

    private int pounceCooldown;
    private int fullTicks;
    private int eatTicks;
    /** Der Sprung läuft (Server): trifft er unterwegs sein Ziel, zählt der Treffer doppelt. */
    private boolean leaping;
    private boolean leapHit;

    public SnowLeopardEntity(EntityType<? extends SnowLeopardEntity> type, Level level) {
        super(type, level);
        this.fullTicks = 600 + level.getRandom().nextInt(2400);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.SAFE_FALL_DISTANCE, 6.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_STALKING, false);
        builder.define(DATA_RESTING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TamableAnimal.TamableAnimalPanicGoal(1.5, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        // scheu: weicht Spielern aus, die nicht schleichen und kein Fleisch zeigen (nicht, wenn er gezähmt ist oder jagt)
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 9.0f, 1.0, 1.35,
                entity -> !this.isTame() && this.getTarget() == null && entity instanceof Player player && !player.isDiscrete()
                        && !this.isFood(player.getMainHandItem()) && !this.isFood(player.getOffhandItem())));
        this.goalSelector.addGoal(4, new HuntGoal());
        this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.15, 10.0f, 2.5f));
        this.goalSelector.addGoal(6, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.2));
        this.goalSelector.addGoal(7, new RestGoal());
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.85));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        // auf der Jagd: Ziegen und Hasen, wenn er hungrig ist und nicht gezähmt
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Animal.class, 20, true, false,
                (target, level) -> isPrey(target)) {
            @Override
            public boolean canUse() {
                return !SnowLeopardEntity.this.isTame() && !SnowLeopardEntity.this.isBaby() && SnowLeopardEntity.this.fullTicks <= 0
                        && super.canUse();
            }
        });
    }

    /** Beute: Ziegen und Hasen (auch die eigenen der Mod). */
    public static boolean isPrey(LivingEntity target) {
        return target instanceof Goat || target instanceof Rabbit;
    }

    // ------------------------------------------------------------------ Zustand

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, Mth.clamp(variant, 0, VARIANTS - 1));
    }

    /** Duckt sich und schleicht sich an. */
    public boolean isStalking() {
        return this.entityData.get(DATA_STALKING);
    }

    public void setStalking(boolean stalking) {
        this.entityData.set(DATA_STALKING, stalking);
    }

    /** Liegt (ein wilder Leopard ruht sich aus; ein gezähmter liegt auf Befehl). */
    public boolean isLying() {
        return this.entityData.get(DATA_RESTING) || this.isInSittingPose();
    }

    public void setResting(boolean resting) {
        this.entityData.set(DATA_RESTING, resting);
    }

    public boolean isLeaping() {
        return this.leaping;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.setVariant(this.random.nextFloat() < 0.7f ? 0 : 1);
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Variant", this.getVariant());
        output.putInt("FullTicks", this.fullTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(input.getIntOr("Variant", 0));
        this.fullTicks = input.getIntOr("FullTicks", 0);
    }

    @Override
    protected void applyTamingSideEffects() {
        if (this.isTame()) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(36.0);
            this.setHealth(36.0f);
            this.setResting(false);
        } else {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(26.0);
        }
    }

    // ------------------------------------------------------------------ Takt

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.pounceCooldown > 0) {
            this.pounceCooldown--;
        }
        if (this.fullTicks > 0) {
            this.fullTicks--;
        }
        if (this.eatTicks > 0) {
            this.eatTicks--;
            this.getNavigation().stop();
            if (this.eatTicks % 12 == 0) {
                this.playSound(ModSounds.SNOW_LEOPARD_EAT.get(), 0.7f, 0.85f + this.random.nextFloat() * 0.2f);
            }
        }
        if (this.leaping) {
            LivingEntity target = this.getTarget();
            if (!this.leapHit && target != null && target.isAlive()
                    && this.getBoundingBox().inflate(0.45).intersects(target.getBoundingBox())) {
                this.leapHit = true;
                this.strike((ServerLevel) this.level(), target, 1.6f);
            }
            if (this.onGround() && this.tickCount % 2 == 0 && this.getDeltaMovement().y <= 0.0) {
                this.leaping = false;
            }
        }
    }

    /** Ein Prankenhieb oder der Treffer am Ende des Sprungs. */
    private void strike(ServerLevel level, LivingEntity target, float factor) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * factor;
        DamageSource source = this.damageSources().mobAttack(this);
        if (target.hurtServer(level, source, damage)) {
            Vec3 push = target.position().subtract(this.position()).multiply(1, 0, 1).normalize();
            target.knockback(factor > 1.0f ? 0.6 : 0.3, -push.x, -push.z, source, damage);
        }
        level.broadcastEntityEvent(this, EVENT_SWIPE);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        level.broadcastEntityEvent(this, EVENT_SWIPE);
        return hit;
    }

    /** Der Sprung auf das Ziel. */
    void pounce(LivingEntity target) {
        Vec3 to = target.position().subtract(this.position());
        Vec3 flat = new Vec3(to.x, 0.0, to.z);
        double dist = flat.length();
        if (dist < 1.0e-3) {
            return;
        }
        Vec3 dir = flat.scale(1.0 / dist);
        double speed = 0.42 + dist * 0.085;
        this.setDeltaMovement(dir.x * speed, 0.42 + Math.max(0.0, to.y) * 0.12, dir.z * speed);
        this.setYRot((float) (Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) - 90.0f);
        this.yBodyRot = this.getYRot();
        this.leaping = true;
        this.leapHit = false;
        this.pounceCooldown = POUNCE_COOLDOWN + this.random.nextInt(30);
        this.setStalking(false);
        this.level().broadcastEntityEvent(this, EVENT_POUNCE);
        this.playSound(ModSounds.SNOW_LEOPARD_HISS.get(), 1.0f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity entity, DamageSource source) {
        if (isPrey(entity) && !this.isTame()) {
            // die Beute: fressen, satt sein
            this.eatTicks = 70;
            this.fullTicks = FULL_TICKS + this.random.nextInt(FULL_TICKS);
            this.heal(this.getMaxHealth());
            this.setTarget(null);
            level.broadcastEntityEvent(this, EVENT_EAT);
        }
        return super.killedEntity(level, entity, source);
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_POUNCE -> this.pounceAnimation.start(this.tickCount);
            case EVENT_SWIPE -> this.swipeAnimation.start(this.tickCount);
            case EVENT_EAT -> {
                this.eatAnimation.start(this.tickCount);
                for (int i = 0; i < 6; i++) {
                    this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(0.8), this.getY() + 0.4, this.getRandomZ(0.8),
                            0.0, 0.02, 0.0);
                }
            }
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            this.setResting(false);
            if (!this.isTame()) {
                this.setOrderedToSit(false);
            }
        }
        return hurt;
    }

    // ------------------------------------------------------------------ Zähmen, Füttern, Junge

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.MUTTON) || stack.is(Items.RABBIT) || stack.is(ModItems.VENISON.get());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.isTame()) {
            if (this.isFood(stack) && this.getHealth() < this.getMaxHealth()) {
                this.feed(player, hand, stack, 2.0f, 4.0f);
                return InteractionResult.SUCCESS;
            }
            InteractionResult result = super.mobInteract(player, hand);
            if (!result.consumesAction() && this.isOwnedBy(player)) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.jumping = false;
                this.navigation.stop();
                this.setTarget(null);
                return InteractionResult.SUCCESS.withoutItem();
            }
            return result;
        }
        if (this.isFood(stack) && !this.isBaby()) {
            if (!this.level().isClientSide()) {
                stack.consume(1, player);
                this.playSound(ModSounds.SNOW_LEOPARD_EAT.get(), 0.8f, 1.0f);
                if (this.random.nextInt(3) == 0 && !net.neoforged.neoforge.event.EventHooks.onAnimalTame(this, player)) {
                    this.tame(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.setOrderedToSit(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    /** Wie ein Wolf: keine Creeper, Rüstungsständer, gezähmten Pferde und Tiere, keine Spieler, die der Herr nicht angreifen darf. */
    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof net.minecraft.world.entity.monster.Creeper || target instanceof net.minecraft.world.entity.decoration.ArmorStand) {
            return false;
        }
        if (target instanceof Player player && owner instanceof Player ownerPlayer && !ownerPlayer.canHarmPlayer(player)) {
            return false;
        }
        if (target instanceof net.minecraft.world.entity.animal.equine.AbstractHorse horse && horse.isTamed()) {
            return false;
        }
        return !(target instanceof TamableAnimal animal && animal.isTame());
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        SnowLeopardEntity cub = BiomeBeasts.SNOW_LEOPARD.get().create(level, EntitySpawnReason.BREEDING);
        if (cub != null) {
            cub.setVariant(this.random.nextBoolean() || !(partner instanceof SnowLeopardEntity other) ? this.getVariant() : other.getVariant());
            if (this.getOwnerReference() != null) {
                cub.setOwnerReference(this.getOwnerReference());
                cub.setTame(true, true);
            }
        }
        return cub;
    }

    @Override
    public boolean canMate(Animal other) {
        return other != this && other instanceof SnowLeopardEntity leopard && this.isTame() && leopard.isTame()
                && this.isInLove() && leopard.isInLove() && !this.isInSittingPose() && !leopard.isInSittingPose();
    }

    // ------------------------------------------------------------------ Auftreten

    /** Auf Schnee, Eis und Fels der Gipfel, wo es hell genug ist. */
    public static boolean checkLeopardSpawnRules(EntityType<SnowLeopardEntity> type, LevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        boolean ground = below.is(BlockTags.GOATS_SPAWNABLE_ON) || below.is(BlockTags.SNOW) || below.is(BlockTags.BASE_STONE_OVERWORLD)
                || below.is(ModBlocks.GLACIER_ICE.get()) || below.is(ModBlocks.FROZEN_SOIL.get()) || below.is(BlockTags.ANIMALS_SPAWNABLE_ON);
        return ground && (EntitySpawnReason.ignoresLightRequirements(reason) || level.getRawBrightness(pos, 0) > 8);
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (this.isLying() && this.isTame()) {
            return ModSounds.SNOW_LEOPARD_PURR.get();
        }
        return this.getTarget() != null ? ModSounds.SNOW_LEOPARD_HISS.get() : ModSounds.SNOW_LEOPARD_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 280;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SNOW_LEOPARD_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SNOW_LEOPARD_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.SNOW_LEOPARD_STEP.get(), 0.12f, 1.0f);
    }

    @Override
    public float getVoicePitch() {
        return this.isBaby() ? 1.4f : 1.0f + (this.random.nextFloat() - 0.5f) * 0.1f;
    }

    // =====================================================================================
    // Jagd: anschleichen, springen, zuschlagen
    // =====================================================================================

    /** Geht auf das Ziel los: Beute beschleicht er geduckt, aus drei bis sieben Blöcken springt er; nah am Ziel schlägt er zu. */
    final class HuntGoal extends Goal {
        private int pathDelay;
        private int attackDelay;

        HuntGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = SnowLeopardEntity.this.getTarget();
            return target != null && target.isAlive() && !SnowLeopardEntity.this.isInSittingPose() && SnowLeopardEntity.this.eatTicks <= 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() && SnowLeopardEntity.this.canAttack(SnowLeopardEntity.this.getTarget());
        }

        @Override
        public void start() {
            this.pathDelay = 0;
            this.attackDelay = 10;
            SnowLeopardEntity.this.setResting(false);
        }

        @Override
        public void stop() {
            SnowLeopardEntity.this.setStalking(false);
            SnowLeopardEntity.this.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            SnowLeopardEntity leopard = SnowLeopardEntity.this;
            LivingEntity target = leopard.getTarget();
            if (target == null) {
                return;
            }
            leopard.getLookControl().setLookAt(target, 30.0f, 30.0f);
            if (leopard.leaping) {
                return;
            }
            double dist = Math.sqrt(leopard.distanceToSqr(target));
            boolean prey = !(target instanceof Player) && !leopard.isTame();
            this.attackDelay = Math.max(0, this.attackDelay - 1);
            if (dist >= POUNCE_MIN && dist <= POUNCE_MAX && leopard.onGround() && leopard.pounceCooldown <= 0
                    && leopard.hasLineOfSight(target) && Math.abs(target.getY() - leopard.getY()) < 3.0) {
                leopard.getNavigation().stop();
                leopard.pounce(target);
                return;
            }
            boolean stalk = prey && dist > POUNCE_MAX;
            leopard.setStalking(stalk);
            if (--this.pathDelay <= 0) {
                this.pathDelay = 6 + leopard.getRandom().nextInt(5);
                leopard.getNavigation().moveTo(target, stalk ? 0.7 : 1.35);
            }
            double reach = leopard.getBbWidth() * 0.5 + target.getBbWidth() * 0.5 + 1.0;
            if (dist <= reach && this.attackDelay <= 0) {
                this.attackDelay = 16;
                leopard.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT);
                leopard.doHurtTarget((ServerLevel) leopard.level(), target);
            }
        }
    }

    /** Ein wilder Leopard legt sich ab und zu hin und ruht; steht auf, wenn ihm jemand zu nahe kommt. */
    final class RestGoal extends Goal {
        private int ticks;

        RestGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            SnowLeopardEntity leopard = SnowLeopardEntity.this;
            return !leopard.isTame() && leopard.getTarget() == null && leopard.onGround() && !leopard.isInWater()
                    && leopard.getRandom().nextInt(reducedTickDelay(600)) == 0 && !this.disturbed();
        }

        private boolean disturbed() {
            SnowLeopardEntity leopard = SnowLeopardEntity.this;
            Player player = leopard.level().getNearestPlayer(leopard, 6.0);
            return player != null && !player.isDiscrete() && !player.isSpectator() && !player.isCreative();
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticks > 0 && SnowLeopardEntity.this.getTarget() == null && !this.disturbed();
        }

        @Override
        public void start() {
            this.ticks = 300 + SnowLeopardEntity.this.getRandom().nextInt(600);
            SnowLeopardEntity.this.getNavigation().stop();
            SnowLeopardEntity.this.setResting(true);
        }

        @Override
        public void tick() {
            this.ticks--;
        }

        @Override
        public void stop() {
            SnowLeopardEntity.this.setResting(false);
        }
    }
}
