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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Moorschnapper des Nebelmoors (0.86): eine riesige Schnappschildkröte. Er liegt reglos im flachen Wasser und im Schlamm
 * auf der Lauer, nur Panzer und Kopf ragen heraus, und schnappt blitzschnell nach allem, was ihm vor die Nase kommt –
 * Spielern, Monstern, Fröschen, Hasen, Hühnern. Wer näher kommt, den warnt er mit einem Fauchen. An Land ist er langsam.
 * Getroffen zieht er sich kurz in seinen Panzer zurück (dann richtet man kaum etwas aus) und schnappt danach nach seinem
 * Angreifer. Er atmet unter Wasser und läuft am Grund entlang.
 */
public class MireSnapperEntity extends Animal {
    public static final byte EVENT_SNAP = 100;
    public static final byte EVENT_HISS = 101;
    /** Reichweite des Zuschnappens (vom Rand des Panzers). */
    public static final double SNAP_REACH = 1.6;
    public static final int SNAP_COOLDOWN = 36;
    public static final int WITHDRAW_TICKS = 60;

    private static final EntityDataAccessor<Boolean> DATA_WITHDRAWN = SynchedEntityData.defineId(MireSnapperEntity.class,
            EntityDataSerializers.BOOLEAN);

    public final AnimationState snapAnimation = new AnimationState();
    public final AnimationState hissAnimation = new AnimationState();
    private int snapCooldown;
    private int withdrawTicks;
    private int hissCooldown;

    public MireSnapperEntity(EntityType<? extends MireSnapperEntity> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.13)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.FOLLOW_RANGE, 12.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WITHDRAWN, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SnapGoal());
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true) {
            @Override
            public boolean canUse() {
                return !MireSnapperEntity.this.isWithdrawn() && super.canUse();
            }
        });
        this.goalSelector.addGoal(3, new LurkGoal());
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8, 240));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    // ------------------------------------------------------------------ Wasser

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    // ------------------------------------------------------------------ Zustand

    /** In den Panzer zurückgezogen (Kopf, Beine und Schwanz eingezogen). */
    public boolean isWithdrawn() {
        return this.entityData.get(DATA_WITHDRAWN);
    }

    public void setWithdrawn(boolean withdrawn) {
        this.entityData.set(DATA_WITHDRAWN, withdrawn);
        if (withdrawn) {
            this.getNavigation().stop();
        }
    }

    /** Wonach er schnappt: Spieler (nicht im Kreativ- oder Zuschauermodus), Monster und kleine Tiere. */
    public static boolean isSnapTarget(LivingEntity e) {
        if (e instanceof MireSnapperEntity || !e.isAlive()) {
            return false;
        }
        if (e instanceof Player p) {
            return !p.isCreative() && !p.isSpectator();
        }
        return e instanceof Enemy || e instanceof Frog || e instanceof Rabbit || e instanceof Chicken;
    }

    // ------------------------------------------------------------------ Takt

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.snapCooldown > 0) {
            this.snapCooldown--;
        }
        if (this.hissCooldown > 0) {
            this.hissCooldown--;
        }
        if (this.withdrawTicks > 0 && --this.withdrawTicks == 0) {
            this.setWithdrawn(false);
        }
        if (this.isWithdrawn()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_SNAP -> this.snapAnimation.start(this.tickCount);
            case EVENT_HISS -> this.hissAnimation.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    /** Zuschnappen: der Hals schnellt vor, die Kiefer schlagen zu. Trifft, wer in Reichweite vor ihm ist. */
    public void snap(@Nullable LivingEntity target) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        this.snapCooldown = SNAP_COOLDOWN;
        level.broadcastEntityEvent(this, EVENT_SNAP);
        this.playSound(ModSounds.MIRE_SNAPPER_SNAP.get(), 1.0f, 0.9f + this.random.nextFloat() * 0.2f);
        if (target != null && this.inSnapRange(target)) {
            float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            DamageSource source = this.damageSources().mobAttack(this);
            if (target.hurtServer(level, source, damage)) {
                Vec3 push = target.position().subtract(this.position()).multiply(1, 0, 1);
                if (push.lengthSqr() > 1.0e-4) {
                    push = push.normalize();
                    target.knockback(0.5, -push.x, -push.z, source, damage);
                }
            }
        }
    }

    /** Vor ihm (nicht mehr als 70° zur Seite) und nah genug am Kopf. */
    public boolean inSnapRange(LivingEntity target) {
        Vec3 look = Vec3.directionFromRotation(0.0f, this.yBodyRot);
        Vec3 to = target.position().subtract(this.position()).multiply(1, 0, 1);
        double dist = to.length();
        if (dist < 1.0e-3) {
            return true;
        }
        double reach = this.getBbWidth() * 0.5 + target.getBbWidth() * 0.5 + SNAP_REACH;
        return dist <= reach && look.dot(to.scale(1.0 / dist)) > Math.cos(Math.toRadians(70.0))
                && Math.abs(target.getY() - this.getY()) < 1.8;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (this.snapCooldown > 0 || this.isWithdrawn() || !(target instanceof LivingEntity living)) {
            return false;
        }
        this.getLookControl().setLookAt(target);
        this.snap(living);
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // im Panzer richtet man kaum etwas aus
        float dealt = this.isWithdrawn() ? amount * 0.35f : amount;
        boolean hurt = super.hurtServer(level, source, dealt);
        if (hurt && !this.isWithdrawn() && this.isAlive()) {
            this.setWithdrawn(true);
            this.withdrawTicks = WITHDRAW_TICKS;
        }
        return hurt;
    }

    // ------------------------------------------------------------------ Zucht (keine)

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    // ------------------------------------------------------------------ Auftreten

    /** Am Ufer, im Schlamm und auf feuchtem Boden. */
    public static boolean checkSnapperSpawnRules(EntityType<MireSnapperEntity> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(Blocks.MUD) || below.is(BlockTags.DIRT) || below.is(BlockTags.SAND) || below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || below.is(Blocks.CLAY) || below.is(Blocks.GRAVEL);
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.MIRE_SNAPPER_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MIRE_SNAPPER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MIRE_SNAPPER_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.MIRE_SNAPPER_STEP.get(), 0.4f, 1.0f);
    }

    // =====================================================================================
    // Ziele
    // =====================================================================================

    /** Wer ihm zu nahe kommt, den faucht er an; wer vor seine Nase gerät, nach dem schnappt er. */
    final class SnapGoal extends Goal {
        private @Nullable LivingEntity victim;

        SnapGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            MireSnapperEntity me = MireSnapperEntity.this;
            if (me.isWithdrawn() || me.isBaby()) {
                return false;
            }
            List<LivingEntity> near = me.level().getEntitiesOfClass(LivingEntity.class, me.getBoundingBox().inflate(4.0, 1.5, 4.0),
                    MireSnapperEntity::isSnapTarget);
            if (near.isEmpty()) {
                return false;
            }
            this.victim = near.stream().min((a, b) -> Double.compare(a.distanceToSqr(me), b.distanceToSqr(me))).orElse(null);
            return this.victim != null;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity v = this.victim;
            MireSnapperEntity me = MireSnapperEntity.this;
            return v != null && v.isAlive() && !me.isWithdrawn() && v.distanceToSqr(me) < 5.0 * 5.0 && isSnapTarget(v);
        }

        @Override
        public void stop() {
            this.victim = null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            MireSnapperEntity me = MireSnapperEntity.this;
            LivingEntity v = this.victim;
            if (v == null) {
                return;
            }
            me.getLookControl().setLookAt(v, 40.0f, 40.0f);
            // langsam zum Störenfried drehen
            float want = (float) (Mth.atan2(v.getZ() - me.getZ(), v.getX() - me.getX()) * Mth.RAD_TO_DEG) - 90.0f;
            me.setYRot(Mth.approachDegrees(me.getYRot(), want, 4.0f));
            me.yBodyRot = me.getYRot();
            if (me.inSnapRange(v)) {
                if (me.snapCooldown <= 0) {
                    me.snap(v);
                }
            } else if (me.hissCooldown <= 0 && v.distanceToSqr(me) < 4.0 * 4.0) {
                me.hissCooldown = 120;
                me.level().broadcastEntityEvent(me, EVENT_HISS);
                me.playSound(ModSounds.MIRE_SNAPPER_HISS.get(), 1.0f, 0.9f + me.getRandom().nextFloat() * 0.2f);
            }
        }
    }

    /** Im Wasser oder Schlamm liegt er lange reglos auf der Lauer. */
    final class LurkGoal extends Goal {
        private int ticks;

        LurkGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            MireSnapperEntity me = MireSnapperEntity.this;
            boolean wet = me.isInWater() || me.getBlockStateOn().is(Blocks.MUD);
            return me.getTarget() == null && (wet ? me.getRandom().nextInt(reducedTickDelay(40)) == 0
                    : me.getRandom().nextInt(reducedTickDelay(400)) == 0);
        }

        @Override
        public void start() {
            MireSnapperEntity me = MireSnapperEntity.this;
            this.ticks = (me.isInWater() ? 600 : 200) + me.getRandom().nextInt(600);
            me.getNavigation().stop();
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticks > 0 && MireSnapperEntity.this.getTarget() == null;
        }

        @Override
        public void tick() {
            this.ticks--;
        }
    }
}
