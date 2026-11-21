package io.github.sirschrolli.kingdomsatwar.entity.merfolk;

import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Eine Tritonwache (seit 0.93): ein Krieger des Meervolks in Bronze, mit Flossenhelm und Dreizack. Sie hält Haie,
 * Ertrunkene und Wächter vom Meeresdorf fern und stellt sich jedem entgegen, der eine Meerjungfrau angreift – aus der Nähe
 * mit Stößen, auf Abstand wirft sie den Dreizack. Mit Spielern tauscht sie nicht, sie grüßt nur.
 */
public class TritonEntity extends PathfinderMob implements MerfolkLike {
    public static final byte EVENT_THRUST = 126;
    public static final byte EVENT_THROW = 125;
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(TritonEntity.class, EntityDataSerializers.INT);
    private static final int GREETINGS = 5;

    public final Merfolk.SwimMotion motion = new Merfolk.SwimMotion();
    public final AnimationState thrustAnimation = new AnimationState();
    public final AnimationState throwAnimation = new AnimationState();
    /** Kampfhaltung 0..1 (Dreizack gesenkt), weich nachgeführt – nur auf dem Client. */
    public float stance;
    public float stanceO;
    /** So lange (Ticks) verfolgt die Wache einen Spieler, der das Meervolk angegriffen hat. */
    private int angerTicks;
    private boolean lookChosen;

    public TritonEntity(EntityType<? extends TritonEntity> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.moveControl = new SmoothSwimmingMoveControl<>(this, 85, 10, 0.02f, 0.1f, false);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 44.0)
                .add(Attributes.MOVEMENT_SPEED, 1.15)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4)
                .add(Attributes.FOLLOW_RANGE, 28.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_LOOK, MermaidLook.LAGOON.ordinal());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FightGoal(this));
        this.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.9));
        this.goalSelector.addGoal(5, new RandomSwimmingGoal(this, 0.6, 60));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, MermaidEntity.class, TritonEntity.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, 5, true, false,
                (target, level) -> target.is(ModTags.EntityTypes.MERFOLK_THREATS) && target.isInWater()));
    }

    @Override
    public MermaidProfession profession() {
        return MermaidProfession.GUARD;
    }

    @Override
    public MermaidLook look() {
        return MermaidLook.byId(this.entityData.get(DATA_LOOK));
    }

    public void setLook(MermaidLook look) {
        this.entityData.set(DATA_LOOK, look.ordinal());
        this.lookChosen = true;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        if (!this.lookChosen) {
            this.setLook(MermaidLook.forBiome(level.getBiome(this.blockPosition()), this.random));
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    /** Bindet die Wache an ihr Dorf. */
    public void settle(BlockPos center, int radius) {
        this.setHomeTo(center, radius);
    }

    /** Stellt sich jemandem entgegen, der das Meervolk angegriffen hat. */
    public void defend(LivingEntity attacker) {
        this.setTarget(attacker);
        this.angerTicks = 600;
    }

    // =====================================================================================
    // Schwimmen
    // =====================================================================================

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
        Merfolk.swim(this, input);
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
    public float swimPose(float partialTicks) {
        return this.motion.pose(partialTicks);
    }

    @Override
    public float swimPhase(float partialTicks) {
        return this.motion.phase(partialTicks);
    }

    // =====================================================================================
    // Kampf
    // =====================================================================================

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            this.motion.tick(this);
            this.stanceO = this.stance;
            this.stance += ((this.isAggressive() ? 1.0f : 0.0f) - this.stance) * 0.2f;
            return;
        }
        if (this.angerTicks > 0 && --this.angerTicks == 0 && this.getTarget() instanceof Player) {
            this.setTarget(null);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_THRUST);
        boolean hit = super.doHurtTarget(level, target);
        if (hit) {
            this.playSound(SoundEvents.TRIDENT_HIT, 1.0f, 0.9f + this.random.nextFloat() * 0.2f);
        }
        return hit;
    }

    /** Wirft einen Dreizack nach dem Ziel (wie die Ertrunkenen; man kann ihn nicht aufheben). */
    void throwTrident(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        level.broadcastEntityEvent(this, EVENT_THROW);
        ThrownTrident trident = new ThrownTrident(level, this, new ItemStack(Items.TRIDENT));
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.33) - trident.getY();
        double dz = target.getZ() - this.getZ();
        double flat = Math.sqrt(dx * dx + dz * dz);
        trident.shoot(dx, dy + flat * 0.12, dz, 1.7f, 4.0f);
        this.playSound(SoundEvents.DROWNED_SHOOT, 1.0f, 0.8f + this.random.nextFloat() * 0.2f);
        level.addFreshEntity(trident);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_THRUST) {
            this.thrustAnimation.start(this.tickCount);
        } else if (id == EVENT_THROW) {
            this.throwAnimation.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && source.getEntity() instanceof LivingEntity attacker) {
            Merfolk.alert(level, this, attacker);
        }
        return hurt;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).getItem() instanceof SpawnEggItem || this.getTarget() == player) {
            return super.mobInteract(player, hand);
        }
        if (!this.level().isClientSide()) {
            // ein kurzer Gruß in der Aktionsleiste
            int line = 1 + this.random.nextInt(GREETINGS);
            player.sendOverlayMessage(Component.translatable("entity.kingdomsatwar.triton.greeting" + line));
            this.getLookControl().setLookAt(player);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("anger", this.angerTicks);
        output.putString("look", this.look().id());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.angerTicks = input.getIntOr("anger", 0);
        String look = input.getStringOr("look", "");
        if (!look.isEmpty()) {
            this.entityData.set(DATA_LOOK, MermaidLook.byName(look).ordinal());
            this.lookChosen = true;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.TRITON_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.TRITON_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TRITON_DEATH.get();
    }

    @Override
    public float getVoicePitch() {
        return 0.9f + this.random.nextFloat() * 0.15f;
    }

    /**
     * Kampf im Wasser: hinschwimmen und mit dem Dreizack zustoßen; ist das Ziel weiter weg (6 bis 18 Blöcke) und frei zu
     * sehen, wirft die Wache ab und zu den Dreizack. Weiter als 40 Blöcke vom Dorf verfolgt sie niemanden.
     */
    static final class FightGoal extends Goal {
        private final TritonEntity triton;
        private int attackCooldown;
        private int throwCooldown = 40;
        private int repath;

        FightGoal(TritonEntity triton) {
            this.triton = triton;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.triton.getTarget();
            return target != null && target.isAlive() && this.inRange(target);
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        private boolean inRange(LivingEntity target) {
            if (!this.triton.hasHome()) {
                return true;
            }
            return target.blockPosition().distSqr(this.triton.getHomePosition()) < 40.0 * 40.0;
        }

        @Override
        public void start() {
            this.triton.setAggressive(true);
            this.repath = 0;
        }

        @Override
        public void stop() {
            this.triton.setAggressive(false);
            this.triton.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.triton.getTarget();
            if (target == null) {
                return;
            }
            this.triton.getLookControl().setLookAt(target, 30.0f, 30.0f);
            double dist = this.triton.distanceToSqr(target);
            if (--this.repath <= 0) {
                this.repath = 8 + this.triton.getRandom().nextInt(6);
                this.triton.getNavigation().moveTo(target, 1.25);
            }
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }
            if (this.throwCooldown > 0) {
                this.throwCooldown--;
            }
            double reach = this.triton.getBbWidth() * 2.0 + target.getBbWidth() + 1.2;
            if (dist <= reach * reach && this.attackCooldown <= 0 && this.triton.level() instanceof ServerLevel level) {
                this.attackCooldown = 20;
                this.triton.swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT);
                this.triton.doHurtTarget(level, target);
            } else if (dist > 36.0 && dist < 18.0 * 18.0 && this.throwCooldown <= 0 && this.triton.hasLineOfSight(target)) {
                this.throwCooldown = 70 + this.triton.getRandom().nextInt(40);
                this.triton.throwTrident(target);
            }
        }
    }
}
