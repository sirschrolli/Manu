package io.github.sirschrolli.kingdomsatwar.entity.umbrer;

import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * Wächter der Umbrer: in Mondsilber gerüstet, mit Helmkamm, Umhang und Speer. Er hält die Schattenwesen vom Dorf fern
 * und stellt sich jedem entgegen, der einen Umbrer angreift. Mit Spielern tauscht er nicht – er grüßt nur.
 */
public class UmbrerGuardEntity extends PathfinderMob implements UmbrerLike {
    public static final byte EVENT_THRUST = 126;
    private static final int GREETINGS = 5;

    public final AnimationState thrustAnimation = new AnimationState();
    /** Kampfhaltung 0..1 (Speer gesenkt), weich nachgeführt – nur auf dem Client. */
    public float stance;
    public float stanceO;
    /** Wem der Wächter nicht verzeiht (z. B. wer einen Umbrer angegriffen hat). */
    private int angerTicks;

    public UmbrerGuardEntity(EntityType<? extends UmbrerGuardEntity> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    public UmbrerProfession profession() {
        return UmbrerProfession.GUARD;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.7));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, UmbrerLike.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, 5, true, false,
                (target, level) -> target.is(ModTags.EntityTypes.SHADOW_CREATURES)));
    }

    /** Bindet den Wächter an sein Dorf. */
    public void settle(BlockPos center, int radius) {
        this.setHomeTo(center, radius);
    }

    /** Ruft die Wächter in der Nähe gegen jemanden, der einen Umbrer angegriffen hat. */
    public static void alert(ServerLevel level, LivingEntity victim, LivingEntity attacker) {
        if (attacker instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }
        List<UmbrerGuardEntity> guards = level.getEntitiesOfClass(UmbrerGuardEntity.class, new AABB(victim.blockPosition()).inflate(24.0));
        for (UmbrerGuardEntity guard : guards) {
            guard.setTarget(attacker);
            guard.angerTicks = 600;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
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
        return super.doHurtTarget(level, target);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_THRUST) {
            this.thrustAnimation.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).getItem() instanceof SpawnEggItem || this.getTarget() == player) {
            return super.mobInteract(player, hand);
        }
        if (!this.level().isClientSide()) {
            // ein kurzer Gruß in der Aktionsleiste
            int line = 1 + this.random.nextInt(GREETINGS);
            player.sendOverlayMessage(Component.translatable("entity.kingdomsatwar.umbrer_guard.greeting" + line));
            this.getLookControl().setLookAt(player);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("anger", this.angerTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.angerTicks = input.getIntOr("anger", 0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WANDERING_TRADER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.62f + this.random.nextFloat() * 0.08f;
    }
}
