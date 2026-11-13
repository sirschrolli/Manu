package io.github.sirschrolli.kingdomsatwar.entity.creature.biome;

import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.player.Player;
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
 * Greif der Felsschluchten (0.85). Er kreist hoch über den Schluchten und landet am liebsten auf den höchsten Felsnadeln;
 * dort sitzt er aufrecht wie ein Wappentier, die Schwingen angelegt. Hat er Hunger, stößt er aus der Höhe auf Hasen,
 * Hühner und Gürteltiere herab – die Fänge voran – und frisst die Beute, sobald er wieder gelandet ist. Menschen lässt er in
 * Ruhe, solange man ihn nicht angreift; wer es tut, den greift er immer wieder im Sturzflug an, am Boden mit den Krallen.
 * Wer ihm beim Ruhen zu nahe kommt, den warnt er mit einem Schrei. Er nimmt keinen Fallschaden.
 */
public class GriffinEntity extends SoaringBird {
    public static final byte EVENT_SCREECH = 100;
    public static final byte EVENT_STRIKE = 101;
    public static final byte EVENT_SWIPE = 102;
    public static final byte EVENT_EAT = 103;
    /** Zwei Arten: der goldene Greif mit weißem Kopf und der Rostgreif der roten Schluchten. */
    public static final int VARIANTS = 2;
    /** So lange (Takte) ist er nach einer Mahlzeit satt. */
    public static final int FULL_TICKS = 4800;
    /** Schaden des Sturzflugs im Verhältnis zum Prankenhieb. */
    public static final float DIVE_FACTOR = 1.25f;

    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(GriffinEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_DIVING = SynchedEntityData.defineId(GriffinEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_RESTING = SynchedEntityData.defineId(GriffinEntity.class,
            EntityDataSerializers.BOOLEAN);

    public final AnimationState screechAnimation = new AnimationState();
    public final AnimationState strikeAnimation = new AnimationState();
    public final AnimationState swipeAnimation = new AnimationState();
    public final AnimationState eatAnimation = new AnimationState();
    private int fullTicks;
    private int eatTicks;
    /** Hat Beute geschlagen und frisst, sobald er gelandet ist. */
    private boolean mealPending;

    public GriffinEntity(EntityType<? extends GriffinEntity> type, Level level) {
        super(type, level);
        this.fullTicks = 600 + level.getRandom().nextInt(2400);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_DIVING, false);
        builder.define(DATA_RESTING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new StrikeGoal());
        this.goalSelector.addGoal(2, new OnGround(new MeleeAttackGoal(this, 1.25, true)));
        this.goalSelector.addGoal(3, new EatGoal());
        this.goalSelector.addGoal(4, new LandGoal());
        this.goalSelector.addGoal(5, new SoarGoal());
        this.goalSelector.addGoal(6, new TakeOffGoal());
        this.goalSelector.addGoal(7, new OnGround(new RestGoal()));
        this.goalSelector.addGoal(8, new OnGround(new WaterAvoidingRandomStrollGoal(this, 0.7, 0.001f) {
            {
                this.setInterval(240);
            }
        }));
        this.goalSelector.addGoal(9, new OnGround(new LookAtPlayerGoal(this, Player.class, 12.0f)));
        this.goalSelector.addGoal(10, new OnGround(new RandomLookAroundGoal(this)));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // auf der Jagd: Hasen, Hühner und Gürteltiere, wenn er hungrig ist
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Animal.class, 20, true, false,
                (target, level) -> isPrey(target)) {
            @Override
            public boolean canUse() {
                return GriffinEntity.this.fullTicks <= 0 && !GriffinEntity.this.mealPending && super.canUse();
            }
        });
    }

    /** Beute: Hasen, Hühner und Gürteltiere (auch die eigenen der Mod). */
    public static boolean isPrey(LivingEntity target) {
        return target instanceof Rabbit || target instanceof Chicken || target instanceof Armadillo;
    }

    // ------------------------------------------------------------------ Art und Flug

    @Override
    protected double cruiseSpeed() {
        return 0.5;
    }

    @Override
    protected double soarRadius() {
        return 20.0;
    }

    @Override
    protected double soarHeight() {
        return 16.0;
    }

    @Override
    protected int newAirBudget() {
        return 600 + this.random.nextInt(1200);
    }

    @Override
    protected int newGroundBudget() {
        return 900 + this.random.nextInt(2400);
    }

    @Override
    protected int landingSearchRadius() {
        return 24;
    }

    @Override
    protected boolean isGoodLandingSpot(BlockPos pos, BlockState ground) {
        if (ground.is(BlockTags.LEAVES) || !ground.isFaceSturdy(this.level(), pos.below(), Direction.UP) || !this.level().canSeeSky(pos)) {
            return false;
        }
        // Platz für den großen Leib
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    BlockState s = this.level().getBlockState(pos.offset(dx, dy, dz));
                    if (!s.isAir() && !s.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected double landingScore(BlockPos pos, BlockState ground) {
        // so hoch wie möglich: Felsnadeln, Grate, Klippenränder
        return super.landingScore(pos, ground) + pos.getY() * 0.8;
    }

    @Override
    protected boolean shouldFleeUpward() {
        return false;
    }

    // ------------------------------------------------------------------ Zustand

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, Mth.clamp(variant, 0, VARIANTS - 1));
    }

    /** Im Sturzflug (Schwingen zurückgelegt, Fänge bereit). */
    public boolean isDiving() {
        return this.entityData.get(DATA_DIVING);
    }

    public void setDiving(boolean diving) {
        this.entityData.set(DATA_DIVING, diving);
    }

    /** Sitzt aufrecht und ruht (auf seinem Felsen). */
    public boolean isResting() {
        return this.entityData.get(DATA_RESTING);
    }

    public void setResting(boolean resting) {
        this.entityData.set(DATA_RESTING, resting);
    }

    public boolean isHungry() {
        return this.fullTicks <= 0;
    }

    /** Für Tests und die Vorführung: hungrig (oder satt) machen. */
    public void setHungry(boolean hungry) {
        this.fullTicks = hungry ? 0 : FULL_TICKS;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.setVariant(this.random.nextFloat() < 0.7f ? 0 : 1);
        this.anchor = this.position();
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Variant", this.getVariant());
        output.putInt("FullTicks", this.fullTicks);
        output.putBoolean("Resting", this.isResting());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(input.getIntOr("Variant", 0));
        this.fullTicks = input.getIntOr("FullTicks", 0);
        this.setResting(input.getBooleanOr("Resting", false));
    }

    @Override
    public void setFlying(boolean flying) {
        super.setFlying(flying);
        if (flying) {
            this.setResting(false);
        } else {
            this.setDiving(false);
        }
    }

    // ------------------------------------------------------------------ Takt

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.fullTicks > 0) {
            this.fullTicks--;
        }
        if (this.eatTicks > 0) {
            this.eatTicks--;
            if (this.eatTicks % 14 == 0 && this.level() instanceof ServerLevel level) {
                Vec3 beak = this.position().add(Vec3.directionFromRotation(0.0f, this.yBodyRot).scale(1.3)).add(0.0, 0.25, 0.0);
                level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.CHICKEN), beak.x, beak.y, beak.z, 5, 0.15, 0.05, 0.15, 0.04);
                this.playSound(ModSounds.GRIFFIN_GROWL.get(), 0.6f, 1.3f + this.random.nextFloat() * 0.2f);
            }
        }
        // nach dem Schlagen der Beute: landen und fressen
        if (this.mealPending) {
            if (this.isFlying()) {
                this.airBudget = Math.min(this.airBudget, this.airTicks + 20);
            } else if (this.onGround()) {
                this.mealPending = false;
                this.eatTicks = 70;
                this.heal(this.getMaxHealth() * 0.5f);
                this.level().broadcastEntityEvent(this, EVENT_EAT);
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_SCREECH -> this.screechAnimation.start(this.tickCount);
            case EVENT_STRIKE -> this.strikeAnimation.start(this.tickCount);
            case EVENT_SWIPE -> this.swipeAnimation.start(this.tickCount);
            case EVENT_EAT -> this.eatAnimation.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    /** Ein Schrei (mit erhobenem Kopf und gelüpften Schwingen). */
    public void screech() {
        this.playSound(ModSounds.GRIFFIN_SCREECH.get(), 2.0f, 0.9f + this.random.nextFloat() * 0.2f);
        this.level().broadcastEntityEvent(this, EVENT_SCREECH);
    }

    @Override
    public void playAmbientSound() {
        super.playAmbientSound();
        if (this.isFlying() && this.level() instanceof ServerLevel level) {
            level.broadcastEntityEvent(this, EVENT_SCREECH);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        level.broadcastEntityEvent(this, EVENT_SWIPE);
        this.playSound(ModSounds.GRIFFIN_STRIKE.get(), 1.0f, 1.0f + this.random.nextFloat() * 0.15f);
        return hit;
    }

    /** Der Treffer am Ende des Sturzflugs: die Fänge voran, mit Wucht. */
    void diveStrike(ServerLevel level, LivingEntity target) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * DIVE_FACTOR;
        DamageSource source = this.damageSources().mobAttack(this);
        if (target.hurtServer(level, source, damage)) {
            Vec3 push = this.getDeltaMovement().multiply(1, 0, 1);
            if (push.lengthSqr() > 1.0e-4) {
                push = push.normalize();
                target.knockback(0.7, -push.x, -push.z, source, damage);
            }
        }
        level.broadcastEntityEvent(this, EVENT_STRIKE);
        this.playSound(ModSounds.GRIFFIN_STRIKE.get(), 1.4f, 0.85f + this.random.nextFloat() * 0.15f);
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity entity, DamageSource source) {
        if (isPrey(entity)) {
            this.fullTicks = FULL_TICKS + this.random.nextInt(FULL_TICKS);
            this.mealPending = true;
            this.setTarget(null);
        }
        return super.killedEntity(level, entity, source);
    }

    @Override
    protected void onTakeOff() {
        this.setResting(false);
        this.playSound(ModSounds.GRIFFIN_WINGS.get(), 1.4f, 0.9f + this.random.nextFloat() * 0.15f);
    }

    // ------------------------------------------------------------------ Auftreten

    /** Auf festem Fels und trockenem Boden unter freiem Himmel, wo es hell ist. */
    public static boolean checkGriffinSpawnRules(EntityType<GriffinEntity> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        boolean ground = below.is(BlockTags.BASE_STONE_OVERWORLD) || below.is(BlockTags.TERRACOTTA) || below.is(BlockTags.DIRT)
                || below.is(BlockTags.SAND) || below.is(BlockTags.ANIMALS_SPAWNABLE_ON);
        return ground && (EntitySpawnReason.ignoresLightRequirements(reason) || level.getRawBrightness(pos, 0) > 8 && level.canSeeSky(pos));
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isFlying() ? ModSounds.GRIFFIN_SCREECH.get() : ModSounds.GRIFFIN_GROWL.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 320;
    }

    @Override
    protected float getSoundVolume() {
        return this.isFlying() ? 2.2f : 1.0f;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.GRIFFIN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GRIFFIN_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.GRIFFIN_STEP.get(), 0.4f, 1.0f);
    }

    // =====================================================================================
    // Ziele
    // =====================================================================================

    /**
     * Angriff aus der Luft: über dem Ziel kreisen und Höhe gewinnen, dann im Sturzflug hinab – Schwingen zurück, Fänge
     * voran –, zuschlagen und wieder aufsteigen; das so lange, bis das Ziel erledigt oder entkommen ist.
     */
    final class StrikeGoal extends Goal {
        private static final int CLIMB = 0;
        private static final int DIVE = 1;
        private static final int RECOVER = 2;
        private int phase;
        private int phaseTicks;
        private int ticks;
        private double angle;

        StrikeGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            GriffinEntity griffin = GriffinEntity.this;
            LivingEntity target = griffin.getTarget();
            if (target == null || !target.isAlive() || griffin.eatTicks > 0 || !griffin.canAttack(target)) {
                return false;
            }
            // Beute schlägt er immer aus der Luft; Gegner am Boden nur, wenn sie weiter weg sind (sonst die Krallen)
            return griffin.isFlying() || isPrey(target) || griffin.distanceToSqr(target) > 5.0 * 5.0;
        }

        @Override
        public boolean canContinueToUse() {
            GriffinEntity griffin = GriffinEntity.this;
            LivingEntity target = griffin.getTarget();
            return target != null && target.isAlive() && griffin.canAttack(target) && this.ticks < 1200
                    && griffin.distanceToSqr(target) < 48.0 * 48.0 && !(target instanceof Player p && (p.isCreative() || p.isSpectator()));
        }

        @Override
        public void start() {
            GriffinEntity griffin = GriffinEntity.this;
            griffin.busy = true;
            griffin.setResting(false);
            this.ticks = 0;
            this.phase = CLIMB;
            this.phaseTicks = 0;
            LivingEntity target = griffin.getTarget();
            this.angle = target == null ? 0.0 : Math.atan2(griffin.getZ() - target.getZ(), griffin.getX() - target.getX());
            if (!griffin.isFlying()) {
                griffin.takeOff();
            }
            if (target instanceof Player) {
                griffin.screech();
            }
        }

        @Override
        public void stop() {
            GriffinEntity griffin = GriffinEntity.this;
            griffin.busy = false;
            griffin.setDiving(false);
            LivingEntity target = griffin.getTarget();
            if (target != null && !target.isAlive()) {
                griffin.setTarget(null);
            }
            if (griffin.isFlying()) {
                griffin.setAnchor(griffin.position());
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            GriffinEntity griffin = GriffinEntity.this;
            LivingEntity target = griffin.getTarget();
            if (target == null) {
                return;
            }
            this.ticks++;
            this.phaseTicks++;
            if (!griffin.isFlying()) {
                griffin.takeOff();
            }
            switch (this.phase) {
                case CLIMB -> {
                    // eng über dem Ziel kreisen, bis er hoch genug ist
                    this.angle += 0.07;
                    double x = target.getX() + Math.cos(this.angle) * 3.5;
                    double z = target.getZ() + Math.sin(this.angle) * 3.5;
                    double y = Math.max(target.getY() + 9.0, griffin.groundY(x, z) + 4.0);
                    griffin.flyTo(x, y, z, 1.15);
                    if (this.phaseTicks > 30 && griffin.getY() - target.getY() > 5.0 && griffin.hasLineOfSight(target)) {
                        this.switchTo(DIVE);
                        griffin.setDiving(true);
                        griffin.screech();
                    }
                }
                case DIVE -> {
                    griffin.flyTo(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 2.6);
                    griffin.getLookControl().setLookAt(target);
                    double reach = griffin.getBbWidth() * 0.5 + target.getBbWidth() * 0.5 + 0.9;
                    if (griffin.distanceToSqr(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ()) < reach * reach) {
                        griffin.diveStrike((ServerLevel) griffin.level(), target);
                        this.recover(griffin);
                    } else if (this.phaseTicks > 60 || griffin.verticalCollisionBelow || griffin.onGround()) {
                        this.recover(griffin);                                // verfehlt
                    }
                }
                default -> {
                    // wieder hinauf, ein Stück weg vom Ziel
                    Vec3 away = griffin.position().subtract(target.position()).multiply(1, 0, 1);
                    away = away.lengthSqr() < 1.0e-4 ? Vec3.directionFromRotation(0.0f, griffin.getYRot()) : away.normalize();
                    griffin.flyTo(griffin.getX() + away.x * 6.0, target.getY() + 8.0, griffin.getZ() + away.z * 6.0, 1.2);
                    if (this.phaseTicks > 34) {
                        this.switchTo(CLIMB);
                        this.angle = Math.atan2(griffin.getZ() - target.getZ(), griffin.getX() - target.getX());
                    }
                }
            }
        }

        private void recover(GriffinEntity griffin) {
            griffin.setDiving(false);
            griffin.setDeltaMovement(griffin.getDeltaMovement().multiply(0.6, 0.0, 0.6).add(0.0, 0.45, 0.0));
            griffin.playSound(ModSounds.GRIFFIN_WINGS.get(), 1.4f, 0.9f);
            this.switchTo(RECOVER);
        }

        private void switchTo(int phase) {
            this.phase = phase;
            this.phaseTicks = 0;
        }
    }

    /** Nach der Jagd: am Boden fressen (Kopf tief, reißen). */
    final class EatGoal extends Goal {
        EatGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return GriffinEntity.this.eatTicks > 0 && !GriffinEntity.this.isFlying();
        }

        @Override
        public void start() {
            GriffinEntity.this.busy = true;
            GriffinEntity.this.getNavigation().stop();
        }

        @Override
        public void stop() {
            GriffinEntity.this.busy = false;
        }
    }

    /** Sitzt aufrecht und ruht; wer ihm zu nahe kommt, den warnt er mit einem Schrei (und steht auf). */
    final class RestGoal extends Goal {
        private int ticks;

        RestGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            GriffinEntity griffin = GriffinEntity.this;
            return griffin.getTarget() == null && griffin.onGround() && !griffin.isInWater()
                    && griffin.getRandom().nextInt(reducedTickDelay(300)) == 0 && this.intruder() == null;
        }

        private @Nullable Player intruder() {
            GriffinEntity griffin = GriffinEntity.this;
            Player player = griffin.level().getNearestPlayer(griffin, 7.0);
            return player != null && !player.isDiscrete() && !player.isSpectator() && !player.isCreative() ? player : null;
        }

        @Override
        public void start() {
            this.ticks = 300 + GriffinEntity.this.getRandom().nextInt(900);
            GriffinEntity.this.getNavigation().stop();
            GriffinEntity.this.setResting(true);
        }

        @Override
        public boolean canContinueToUse() {
            GriffinEntity griffin = GriffinEntity.this;
            return this.ticks > 0 && griffin.getTarget() == null && griffin.onGround() && !griffin.isFlying();
        }

        @Override
        public void tick() {
            this.ticks--;
            Player intruder = this.intruder();
            if (intruder != null) {
                GriffinEntity.this.getLookControl().setLookAt(intruder);
                GriffinEntity.this.screech();
                this.ticks = 0;
            }
        }

        @Override
        public void stop() {
            GriffinEntity.this.setResting(false);
        }
    }
}
