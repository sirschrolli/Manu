package io.github.sirschrolli.kingdomsatwar.entity.dragon;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ein Drache. Wächst in fünf Stufen ({@link DragonStage}), kann ab Stufe 3 fliegen und Odem speien
 * und ab Stufe 4 geritten werden. Gezähmte Drachen folgen ihrem Besitzer und verteidigen ihn.
 *
 * <p>Steuerung für Besitzer: Rechtsklick = aufsitzen (ab Stufe 4), Schleichen + Rechtsklick = Befehl
 * wechseln (Folgen → Sitzen → Umherstreifen). Füttern mit Drachenfutter heilt und lässt schneller wachsen. Mit dem
 * Drachenhorn schickt der Herr seine Drachen in den Krieg (siehe {@link DragonWar}): Sie fliegen dann Sturzangriffe
 * mit ihrem Odem.
 */
public class DragonEntity extends TamableAnimal {
    private static final Identifier STAGE_MODIFIER = KingdomsAtWar.id("dragon_stage");
    private static final EntityDataAccessor<Integer> DATA_STAGE = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_FLYING = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_BREATH = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_MODE = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BYTE);

    public static final int MODE_FOLLOW = 0;
    public static final int MODE_SIT = 1;
    public static final int MODE_WANDER = 2;
    private static final int BREATH_DURATION = 36;
    /** Eigene Entity-Ereignisse (weit oberhalb der von Minecraft benutzten Nummern). */
    private static final byte EVENT_ROAR = 110;
    private static final byte EVENT_BITE = 111;

    // Animationen (Client)
    public final AnimationState roarAnimation = new AnimationState();
    public final AnimationState biteAnimation = new AnimationState();
    public float flapPhase;
    public float flapPhaseO;
    /** 0 = am Boden (Flügel angelegt), 1 = in der Luft – weich, damit Abheben und Landen fließen. */
    public float airBlend;
    public float airBlendO;
    /** 0 = stehend, 1 = sitzend (weich). */
    public float sitBlend;
    public float sitBlendO;
    /** Schräglage im Kurvenflug (Grad, geglättet). */
    public float bank;
    public float bankO;
    /** Steigen (positiv) oder Sinken im Flug (Grad, geglättet). */
    public float flightPitch;
    public float flightPitchO;
    /** Zurückgelegter Weg in Schrittfolgen ({@link DragonGait}): der Gang passt immer zur Geschwindigkeit. */
    public float stride;
    public float strideO;
    /** Wie kräftig der Drache gerade schreitet (0 = steht, 1 = Galopp; geglättet). */
    public float strideSpeed;
    public float strideSpeedO;
    /** Beginn des laufenden Odems (Spieltick des Wesens). */
    public int breathStart;
    private boolean wasBreathing;
    /** Laune in ruhigen Momenten ({@code BossAnims.MOOD_*}), ihr Beginn und ihre Richtung (±1). */
    public int idleEvent;
    public int idleEventStart;
    public float idleSide = 1.0f;
    private int nextIdleEvent = 80;

    private final DragonType dragonType;
    private int growth;
    private int breathCooldown;
    private int flightTicks;
    private int groundTicks;
    private int roarCooldown = 200;
    private @Nullable Vec3 breathDirection;
    private @Nullable MoveControl groundMoveControl;
    private @Nullable FlyingMoveControl<DragonEntity> flyingMoveControl;
    private @Nullable PathNavigation groundNavigation;
    private @Nullable PathNavigation flyingNavigation;
    /** Kriegsbefehl (Drachenhorn): das Ziel des Angriffs bzw. der Ort des Luftangriffs, sein Umkreis und bis wann er gilt. */
    private java.util.@Nullable UUID warTarget;
    private @Nullable BlockPos strikeAt;
    private double strikeRadius = DragonWar.STRIKE_RADIUS;
    private long orderUntil;
    /** Seit wann bei einem Luftangriff kein Gegner mehr zu sehen war. */
    private int strikeIdle;

    public DragonEntity(EntityType<? extends DragonEntity> type, Level level, DragonType dragonType) {
        super(type, level);
        this.dragonType = dragonType;
        this.groundMoveControl = this.moveControl;
        this.flyingMoveControl = new FlyingMoveControl<>(this, 12, false);
        this.groundNavigation = this.navigation;
        FlyingPathNavigation flying = new FlyingPathNavigation(this, level);
        flying.setCanFloat(true);
        flying.setCanOpenDoors(false);
        this.flyingNavigation = flying;
        this.applyStage(DragonStage.HATCHLING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.animal.Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FLYING_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.ARMOR, 0.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.STEP_HEIGHT, 1.1)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4)
                .add(Attributes.CAMERA_DISTANCE, 4.0);
    }

    // =====================================================================================
    // Daten
    // =====================================================================================

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STAGE, DragonStage.HATCHLING.number());
        builder.define(DATA_FLYING, false);
        builder.define(DATA_BREATH, 0);
        builder.define(DATA_MODE, (byte) MODE_FOLLOW);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("DragonStage", this.getStage().number());
        output.putInt("DragonGrowth", this.growth);
        output.putInt("DragonMode", this.getMode());
        output.putBoolean("DragonFlying", this.isFlying());
        if (this.warTarget != null) {
            output.store("WarTarget", net.minecraft.core.UUIDUtil.CODEC, this.warTarget);
        }
        if (this.strikeAt != null) {
            output.store("StrikeAt", BlockPos.CODEC, this.strikeAt);
            output.putDouble("StrikeRadius", this.strikeRadius);
        }
        if (this.warTarget != null || this.strikeAt != null) {
            output.putLong("OrderUntil", this.orderUntil);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.applyStage(DragonStage.byNumber(input.getIntOr("DragonStage", 1)), false);
        this.growth = input.getIntOr("DragonGrowth", 0);
        this.setMode(input.getIntOr("DragonMode", MODE_FOLLOW));
        this.setFlying(input.getBooleanOr("DragonFlying", false) && this.getStage().canFly());
        this.warTarget = input.read("WarTarget", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        this.strikeAt = input.read("StrikeAt", BlockPos.CODEC).orElse(null);
        this.strikeRadius = input.getDoubleOr("StrikeRadius", DragonWar.STRIKE_RADIUS);
        this.orderUntil = input.getLongOr("OrderUntil", 0L);
    }

    public DragonType getDragonType() {
        return dragonType;
    }

    public DragonStage getStage() {
        return DragonStage.byNumber(this.entityData.get(DATA_STAGE));
    }

    public boolean isFlying() {
        return this.entityData.get(DATA_FLYING);
    }

    public boolean isBreathing() {
        return this.entityData.get(DATA_BREATH) > 0;
    }

    public int getMode() {
        return this.entityData.get(DATA_MODE);
    }

    public void setMode(int mode) {
        this.entityData.set(DATA_MODE, (byte) Mth.clamp(mode, 0, 2));
        this.setOrderedToSit(mode == MODE_SIT);
    }

    /** Wachstum bis zur nächsten Stufe (0..1). */
    public float getGrowthProgress() {
        DragonStage stage = this.getStage();
        return stage.isLast() ? 1.0f : Mth.clamp((float) this.growth / stage.growthTicks(), 0.0f, 1.0f);
    }

    // =====================================================================================
    // Wachstum
    // =====================================================================================

    /** Setzt die Wachstumsstufe und passt Größe, Leben, Schaden und Rüstung an. */
    public void applyStage(DragonStage stage, boolean effects) {
        this.entityData.set(DATA_STAGE, stage.number());
        setStageModifier(Attributes.SCALE, stage.scale() - 1.0);
        setStageModifier(Attributes.MAX_HEALTH, stage.maxHealth() - 16.0);
        setStageModifier(Attributes.ATTACK_DAMAGE, stage.attack() - 2.0);
        setStageModifier(Attributes.ARMOR, stage.armor());
        setStageModifier(Attributes.CAMERA_DISTANCE, stage.cameraDistance() - 4.0);
        setStageModifier(Attributes.FLYING_SPEED, stage.number() >= 4 ? 0.12 : 0.0);
        this.refreshDimensions();
        if (effects) {
            this.setHealth(this.getMaxHealth());
        } else if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
        if (!stage.canFly() && this.isFlying()) {
            this.setFlying(false);
        }
    }

    private void setStageModifier(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double amount) {
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(STAGE_MODIFIER);
        if (amount != 0.0) {
            instance.addPermanentModifier(new AttributeModifier(STAGE_MODIFIER, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    /** Lässt den Drachen wachsen (z. B. durch Füttern). */
    public void addGrowth(int ticks) {
        DragonStage stage = this.getStage();
        if (stage.isLast()) {
            return;
        }
        this.growth += ticks;
        if (this.growth >= stage.growthTicks()) {
            this.growth = 0;
            this.growUp(stage.next());
        }
    }

    private void growUp(DragonStage next) {
        this.applyStage(next, true);
        if (this.level() instanceof ServerLevel level) {
            this.playSound(ModSounds.DRAGON_GROW.get(), 1.5f, this.getVoicePitch());
            level.sendParticles(this.dragonType.particle(), this.getX(), this.getY() + this.getBbHeight() * 0.6, this.getZ(),
                    40, this.getBbWidth() * 0.5, this.getBbHeight() * 0.4, this.getBbWidth() * 0.5, 0.05);
            // Beim Wachsen fallen alte Schuppen ab
            int scales = 1 + this.random.nextInt(next.number());
            this.spawnAtLocation(level, new ItemStack(ModItems.DRAGON_SCALES.get(this.dragonType).get(), scales));
            if (this.getOwner() instanceof Player owner) {
                owner.sendOverlayMessage(Component.translatable("message.kingdomsatwar.dragon_grew", this.getDisplayName(),
                        Component.translatable(next.translationKey())).withStyle(ChatFormatting.GOLD));
            }
            this.roar();
        }
    }

    // =====================================================================================
    // Fliegen
    // =====================================================================================

    public void setFlying(boolean flying) {
        if (flying == this.isFlying() && this.flyingNavigation != null) {
            if (!this.level().isClientSide()) {
                this.updateMovementMode(flying);
            }
            return;
        }
        this.entityData.set(DATA_FLYING, flying);
        if (!this.level().isClientSide()) {
            this.updateMovementMode(flying);
        }
    }

    private void updateMovementMode(boolean flying) {
        if (this.flyingMoveControl == null) {
            return;
        }
        this.setNoGravity(flying);
        this.moveControl = flying ? this.flyingMoveControl : this.groundMoveControl;
        PathNavigation wanted = flying ? this.flyingNavigation : this.groundNavigation;
        if (this.navigation != wanted) {
            this.navigation.stop();
            this.navigation = wanted;
        }
        this.flightTicks = 0;
        this.groundTicks = 0;
    }

    /** Hebt ab (falls die Stufe fliegen kann). */
    public void takeOff() {
        if (this.getStage().canFly() && !this.isFlying() && !this.isInSittingPose()) {
            this.setFlying(true);
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.5, 0.0));
            this.playSound(ModSounds.DRAGON_FLAP.get(), 1.2f, this.getVoicePitch());
        }
    }

    @Override
    public void travel(Vec3 input) {
        if (this.isFlying()) {
            float speed = (float) this.getAttributeValue(Attributes.FLYING_SPEED);
            this.travelFlying(input, speed * (this.isVehicle() ? 1.0f : 0.6f));
            this.calculateEntityAnimation(true);
        } else {
            super.travel(input);
        }
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource source) {
        return this.getStage().number() >= 2 ? false : super.causeFallDamage(fallDistance, damageModifier, source);
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
        if (!this.isFlying()) {
            super.checkFallDamage(ya, onGround, onState, pos);
        }
    }

    @Override
    public boolean isFlyingVehicle() {
        return this.isFlying();
    }

    // =====================================================================================
    // Reiten
    // =====================================================================================

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof Player player && this.isOwnedBy(player) ? player : super.getControllingPassenger();
    }

    @Override
    protected Vec3 getRiddenInput(Player controller, Vec3 selfInput) {
        float strafe = controller.xxa * 0.6f;
        if (this.isFlying()) {
            float forward = 0.0f;
            float up = 0.0f;
            if (controller.zza != 0.0f) {
                float pitch = controller.getXRot() * Mth.DEG_TO_RAD;
                forward = Mth.cos(pitch);
                up = -Mth.sin(pitch);
                if (controller.zza < 0.0f) {
                    forward *= -0.4f;
                    up *= -0.4f;
                }
            }
            if (controller.isJumping()) {
                up += 0.6f;
            }
            return new Vec3(strafe, up, forward).scale(4.2);
        }
        float forward = controller.zza;
        if (forward <= 0.0f) {
            forward *= 0.35f;
        }
        return new Vec3(strafe, 0.0, forward);
    }

    @Override
    protected void tickRidden(Player controller, Vec3 riddenInput) {
        super.tickRidden(controller, riddenInput);
        Vec2 rotation = new Vec2(controller.getXRot() * 0.5f, controller.getYRot());
        float yRot = this.getYRot();
        yRot += Mth.wrapDegrees(rotation.y - yRot) * (this.isFlying() ? 0.12f : 0.25f);
        this.setRot(yRot, this.isFlying() ? rotation.x : 0.0f);
        this.yRotO = this.yBodyRot = this.yHeadRot = yRot;
        if (!this.isFlying() && controller.isJumping() && this.onGround()) {
            this.takeOff();
        }
    }

    @Override
    protected float getRiddenSpeed(Player controller) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.1f;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        return new Vec3(0.0, dimensions.height() * 0.80, -dimensions.width() * 0.22).yRot(-this.yBodyRot * Mth.DEG_TO_RAD);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 side = new Vec3(this.getBbWidth() * 0.6 + 0.6, 0.0, 0.0).yRot(-this.yBodyRot * Mth.DEG_TO_RAD);
        return this.position().add(side);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < (this.getStage() == DragonStage.ELDER ? 2 : 1);
    }

    // =====================================================================================
    // Odem
    // =====================================================================================

    /** Startet einen Odem-Angriff in Blickrichtung (bzw. Richtung {@code direction}). */
    public boolean startBreath(@Nullable Vec3 direction) {
        if (!this.getStage().canBreathe() || this.breathCooldown > 0 || this.isBreathing()) {
            return false;
        }
        this.breathDirection = direction;
        this.entityData.set(DATA_BREATH, BREATH_DURATION);
        // im Kriegsflug holt er schneller wieder Luft (ein Sturzflug dauert kaum länger als der Odem)
        this.breathCooldown = BREATH_DURATION + (this.onWarOrder() ? 20 : 60);
        this.playSound(ModSounds.DRAGON_BREATH.get(this.dragonType).get(), 2.0f, this.getVoicePitch());
        return true;
    }

    private void tickBreath(ServerLevel level) {
        int left = this.entityData.get(DATA_BREATH);
        if (left <= 0) {
            return;
        }
        this.entityData.set(DATA_BREATH, left - 1);
        Vec3 dir = this.breathDirection != null ? this.breathDirection : this.getLookAngle();
        if (this.getTarget() != null && this.getControllingPassenger() == null) {
            dir = this.getTarget().getEyePosition().subtract(this.headPosition()).normalize();
        } else if (this.getControllingPassenger() instanceof Player rider) {
            dir = rider.getLookAngle();
        }
        DragonBreath.tick(level, this, this.headPosition(), dir.normalize(), left);
    }

    /** Ungefähre Lage des Mauls (für Odem und Partikel). */
    public Vec3 headPosition() {
        float scale = this.getStage().scale();
        Vec3 forward = Vec3.directionFromRotation(0, this.yBodyRot);
        return this.position().add(forward.scale(1.9 * scale)).add(0, 2.0 * scale, 0);
    }

    public int getBreathTicks() {
        return this.entityData.get(DATA_BREATH);
    }

    // =====================================================================================
    // Krieg (Drachenhorn)
    // =====================================================================================

    /** Angriff auf {@code target}: aufsteigen und im Sturzflug mit dem Odem herabstoßen. */
    public void orderAttack(LivingEntity target) {
        this.prepareForWar();
        this.warTarget = target.getUUID();
        this.strikeAt = null;
        this.orderUntil = this.level().getGameTime() + DragonWar.ATTACK_TICKS;
        this.setTarget(target);
    }

    /** Luftangriff auf den Ort {@code at}: über ihm kreisen und auf jeden Gegner im Umkreis {@code radius} herabstoßen. */
    public void orderStrike(BlockPos at, double radius, int ticks) {
        this.prepareForWar();
        this.warTarget = null;
        this.strikeAt = at.immutable();
        this.strikeRadius = radius;
        this.strikeIdle = 0;
        this.orderUntil = this.level().getGameTime() + ticks;
        this.setTarget(null);
    }

    private void prepareForWar() {
        this.setMode(MODE_FOLLOW);
        this.setOrderedToSit(false);
        this.setInSittingPose(false);
        this.takeOff();
    }

    /** Zurück zum Herrn (Befehl aufgehoben). */
    public void recall() {
        this.warTarget = null;
        this.strikeAt = null;
        this.setTarget(null);
        this.getNavigation().stop();
    }

    /** Hat der Drache einen Kriegsbefehl? */
    public boolean onWarOrder() {
        return this.warTarget != null || this.strikeAt != null;
    }

    /**
     * Luftangriff: nach jedem Sturzflug ein anderes Ziel im Umkreis (so trifft der Odem alle Gegner dort nach und nach, statt
     * sich an einem festzubeißen).
     */
    void nextStrikeTarget() {
        if (this.strikeAt == null || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 center = Vec3.atBottomCenterOf(this.strikeAt);
        LivingEntity current = this.getTarget();
        List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(center, center).inflate(this.strikeRadius, this.strikeRadius * 0.75, this.strikeRadius),
                e -> e != current && DragonWar.isEnemy(this, e) && e.distanceToSqr(center) < this.strikeRadius * this.strikeRadius);
        if (!enemies.isEmpty()) {
            this.setTarget(enemies.get(this.random.nextInt(enemies.size())));
        }
    }

    /** Ort des Luftangriffs ({@code null}: keiner). */
    public @Nullable BlockPos strikePos() {
        return this.strikeAt;
    }

    public double strikeRadius() {
        return this.strikeRadius;
    }

    /**
     * Kriegsbefehle verwalten: abgelaufen, schwer verwundet (Rückzug), das Ziel gefallen (dann die Gegner gleich daneben),
     * beim Luftangriff zu lange kein Gegner mehr da (Rückkehr).
     */
    private void tickWarOrders(ServerLevel level) {
        if (!this.onWarOrder()) {
            return;
        }
        if (!DragonWar.canFight(this) || level.getGameTime() > this.orderUntil) {
            this.recall();
            return;
        }
        if (this.getHealth() < this.getMaxHealth() * DragonWar.RETREAT_HEALTH) {
            this.recall();
            this.roar();
            if (this.getOwner() instanceof net.minecraft.server.level.ServerPlayer owner) {
                owner.sendOverlayMessage(Component.translatable("message.kingdomsatwar.dragon_retreat", this.getDisplayName())
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }
        if (this.warTarget != null) {
            Entity target = level.getEntity(this.warTarget);
            if (target instanceof LivingEntity living && living.isAlive()) {
                if (this.getTarget() != living) {
                    this.setTarget(living);
                }
                return;
            }
            // gefallen: die Gegner gleich daneben – sonst Sieg und zurück
            Vec3 where = target != null ? target.position() : this.position();
            LivingEntity next = DragonWar.nearestEnemy(level, this, where, 16.0);
            if (next != null) {
                this.warTarget = next.getUUID();
                this.setTarget(next);
            } else {
                this.recall();
                this.roar();
            }
            return;
        }
        if ((this.tickCount + this.getId()) % 10 == 0) {
            Vec3 center = Vec3.atBottomCenterOf(this.strikeAt);
            LivingEntity target = this.getTarget();
            boolean valid = target != null && target.isAlive() && DragonWar.isEnemy(this, target)
                    && target.distanceToSqr(center) < Math.pow(this.strikeRadius + 8.0, 2);
            if (!valid) {
                LivingEntity next = DragonWar.nearestEnemy(level, this, center, this.strikeRadius);
                this.setTarget(next);
                valid = next != null;
            }
            this.strikeIdle = valid ? 0 : this.strikeIdle + 10;
            if (this.strikeIdle > DragonWar.IDLE_TICKS) {
                this.recall();
            }
        }
    }

    // =====================================================================================
    // Ticks
    // =====================================================================================

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.clientAnimTick();
        }
    }

    /**
     * Alles, was nur fürs Aussehen zählt (nur auf dem Client): Flügelschlag, weiche Übergänge zwischen Boden und Luft,
     * Stehen und Sitzen, Schräglage in Kurven, Schritte und kleine Launen in ruhigen Momenten.
     */
    private void clientAnimTick() {
        boolean flying = this.isFlying();
        float scale = this.getStage().scale();
        double dx = this.getX() - this.xo;
        double dy = this.getY() - this.yo;
        double dz = this.getZ() - this.zo;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float turn = Mth.wrapDegrees(this.yBodyRot - this.yBodyRotO);

        // Steigen und Sinken, Schräglage in Kurven
        this.flightPitchO = this.flightPitch;
        float pitch = flying ? (float) Mth.clamp(Math.atan2(dy, Math.max(horizontal, 0.08)) * Mth.RAD_TO_DEG, -40.0, 35.0) : 0.0f;
        this.flightPitch += (pitch - this.flightPitch) * 0.15f;
        this.bankO = this.bank;
        float bankWanted = flying ? Mth.clamp(-turn * 4.0f, -32.0f, 32.0f) : 0.0f;
        this.bank += (bankWanted - this.bank) * 0.12f;

        // Flügelschlag: kräftig beim Steigen und Schweben, im Sinkflug gleitet der Drache
        this.flapPhaseO = this.flapPhase;
        boolean gliding = this.flightPitch < -8.0f;
        this.flapPhase += flying ? (gliding ? 0.07f : 0.3f) : 0.0f;
        if (flying && !gliding && Mth.sin(this.flapPhase) > 0.98f && Mth.sin(this.flapPhaseO) <= 0.98f) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), ModSounds.DRAGON_FLAP.get(), this.getSoundSource(),
                    0.6f + scale * 0.6f, this.getVoicePitch(), false);
        }

        // Abheben und Landen, Hinsetzen und Aufstehen fließen ineinander
        this.airBlendO = this.airBlend;
        this.airBlend = Mth.approach(this.airBlend, flying ? 1.0f : 0.0f, 0.075f);
        this.sitBlendO = this.sitBlend;
        this.sitBlend = Mth.approach(this.sitBlend, this.isInSittingPose() ? 1.0f : 0.0f, 0.06f);

        // Schritte: der Weg in Schrittfolgen (beim Drehen auf der Stelle treten die Füße mit)
        float moved = flying || this.isPassenger() ? 0.0f
                : (float) horizontal + Math.abs(turn) * Mth.DEG_TO_RAD * 0.5f * scale;
        this.strideSpeedO = this.strideSpeed;
        this.strideSpeed += (Mth.clamp(moved / (0.26f * scale), 0.0f, 1.0f) - this.strideSpeed) * 0.3f;
        this.strideO = this.stride;
        this.stride += moved / DragonGait.length(scale, this.strideSpeed);

        boolean breathing = this.isBreathing();
        if (breathing && !this.wasBreathing) {
            this.breathStart = this.tickCount;
        }
        this.wasBreathing = breathing;
        this.tickMood();
    }

    /** Launen in ruhigen Momenten: Schnauben, Kopfschütteln, Schwanzschlagen, Flügel lockern, Umsehen, Gähnen. */
    private void tickMood() {
        int mood = this.idleEvent;
        if (mood != BossAnims.MOOD_NONE && this.tickCount - this.idleEventStart > BossAnims.MOOD_TICKS[mood]) {
            this.idleEvent = BossAnims.MOOD_NONE;
        }
        boolean roaring = this.roarAnimation.isStarted() && this.roarAnimation.getTimeInMillis(this.tickCount) < 2200L;
        boolean biting = this.biteAnimation.isStarted() && this.biteAnimation.getTimeInMillis(this.tickCount) < 700L;
        if (this.isBreathing() || this.isDeadOrDying() || roaring || biting) {
            this.idleEvent = BossAnims.MOOD_NONE;
            return;
        }
        if (this.idleEvent == BossAnims.MOOD_NONE && this.tickCount >= this.nextIdleEvent) {
            int[] choices;
            if (this.isFlying()) {
                choices = new int[] {BossAnims.MOOD_GLANCE, BossAnims.MOOD_SHAKE, BossAnims.MOOD_SNORT};
            } else if (this.isVehicle()) {
                // mit Reiter: nichts, was den Blick vom Weg nimmt
                choices = new int[] {BossAnims.MOOD_SNORT, BossAnims.MOOD_SHAKE, BossAnims.MOOD_TAIL_FLICK};
            } else if (this.isInSittingPose()) {
                choices = new int[] {BossAnims.MOOD_GLANCE, BossAnims.MOOD_GLANCE, BossAnims.MOOD_YAWN, BossAnims.MOOD_TAIL_FLICK,
                        BossAnims.MOOD_SNORT, BossAnims.MOOD_SHRUG};
            } else {
                choices = new int[] {BossAnims.MOOD_SNORT, BossAnims.MOOD_SNORT, BossAnims.MOOD_SHAKE, BossAnims.MOOD_TAIL_FLICK,
                        BossAnims.MOOD_TAIL_FLICK, BossAnims.MOOD_SHRUG, BossAnims.MOOD_GLANCE, BossAnims.MOOD_GLANCE,
                        BossAnims.MOOD_YAWN};
            }
            this.idleEvent = choices[this.random.nextInt(choices.length)];
            this.idleEventStart = this.tickCount;
            this.idleSide = this.random.nextBoolean() ? 1.0f : -1.0f;
            this.nextIdleEvent = this.tickCount + 70 + this.random.nextInt(130);
        }
        if (this.idleEvent == BossAnims.MOOD_SNORT && this.tickCount - this.idleEventStart == 4) {
            this.snortParticles();
        }
    }

    /** Rauch (und ein Hauch des Elements) aus den Nüstern beim Schnauben. */
    private void snortParticles() {
        float scale = this.getStage().scale();
        Vec3 mouth = this.headPosition();
        Vec3 dir = Vec3.directionFromRotation(15.0f, this.yHeadRot);
        net.minecraft.core.particles.ParticleOptions element = switch (this.dragonType) {
            case FIRE -> net.minecraft.core.particles.ParticleTypes.SMALL_FLAME;
            case ICE -> net.minecraft.core.particles.ParticleTypes.SNOWFLAKE;
            case LIGHTNING -> net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK;
            case NATURE -> net.minecraft.core.particles.ParticleTypes.SPORE_BLOSSOM_AIR;
            case WATER -> net.minecraft.core.particles.ParticleTypes.SPLASH;
            default -> net.minecraft.core.particles.ParticleTypes.WHITE_ASH;
        };
        for (int i = 0; i < 5; i++) {
            double spread = 0.08 * scale;
            double speed = (0.05 + this.random.nextDouble() * 0.05) * (0.6 + scale * 0.6);
            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
                    mouth.x + (this.random.nextDouble() - 0.5) * spread, mouth.y + (this.random.nextDouble() - 0.5) * spread,
                    mouth.z + (this.random.nextDouble() - 0.5) * spread, dir.x * speed, dir.y * speed + 0.01, dir.z * speed);
        }
        this.level().addParticle(element, mouth.x, mouth.y, mouth.z, dir.x * 0.06, dir.y * 0.06 + 0.02, dir.z * 0.06);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.breathCooldown > 0) {
            this.breathCooldown--;
        }
        this.tickBreath(level);
        this.tickWarOrders(level);
        if (!this.getStage().isLast() && this.tickCount % 20 == 0) {
            this.addGrowth(20);
        }
        if (this.isFlying()) {
            this.flightTicks++;
            boolean riderWantsUp = this.getControllingPassenger() instanceof Player p && p.isJumping();
            if (this.onGround() && !riderWantsUp && this.flightTicks > 20) {
                this.groundTicks++;
                if (this.groundTicks > 5) {
                    this.setFlying(false);
                }
            } else {
                this.groundTicks = 0;
            }
            if (this.isInSittingPose()) {
                this.setFlying(false);
            }
        }
        if (--this.roarCooldown <= 0) {
            this.roarCooldown = 600 + this.random.nextInt(1200);
            if (this.getStage().number() >= 3 && this.getTarget() == null && !this.isVehicle() && this.random.nextInt(3) == 0) {
                this.roar();
            }
        }
    }

    /** Brüllen mit Animation. */
    public void roar() {
        this.level().broadcastEntityEvent(this, EVENT_ROAR);
        this.playSound(ModSounds.DRAGON_ROAR.get(), 1.0f + this.getStage().scale(), this.getVoicePitch());
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ROAR) {
            this.roarAnimation.start(this.tickCount);
        } else if (id == EVENT_BITE) {
            this.biteAnimation.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_BITE);
        this.playSound(ModSounds.DRAGON_BITE.get(), 1.0f, this.getVoicePitch());
        return super.doHurtTarget(level, target);
    }

    // =====================================================================================
    // Interaktion
    // =====================================================================================

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean owner = this.isTame() && this.isOwnedBy(player);

        if (this.isFood(stack)) {
            if (!this.level().isClientSide()) {
                // ein Drache ohne Herrn (aus alten Welten) schließt sich dem an, der ihn füttert
                if (!this.isTame()) {
                    this.bondWith(player);
                }
                boolean dragonFood = stack.is(ModItems.DRAGON_FOOD.get());
                this.heal(this.getMaxHealth() * (dragonFood ? 0.25f : 0.08f));
                this.addGrowth(dragonFood ? 3000 : 600);
                this.playSound(ModSounds.DRAGON_EAT.get(), 1.0f, this.getVoicePitch());
                ((ServerLevel) this.level()).sendParticles(net.minecraft.core.particles.ParticleTypes.HEART, this.getX(),
                        this.getY() + this.getBbHeight(), this.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
                stack.consume(1, player);
            }
            return InteractionResult.SUCCESS;
        }

        // Wilde Schlüpflinge lassen sich (selten) mit Edelsteinen zähmen
        if (!this.isTame() && this.getStage().number() <= 2 && stack.is(ModItems.GEMSTONE.get())) {
            if (!this.level().isClientSide()) {
                stack.consume(1, player);
                if (this.random.nextInt(4) == 0) {
                    this.tame(player);
                    this.setMode(MODE_FOLLOW);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Drachenpanzer anlegen (nur ausgewachsene, gezähmte Drachen) oder mit dem Werkstoff ausbessern
        if (owner && stack.has(DataComponents.EQUIPPABLE) && stack.get(DataComponents.EQUIPPABLE).slot() == EquipmentSlot.BODY) {
            if (!this.isEquippableInSlot(stack, EquipmentSlot.BODY)) {
                if (!this.level().isClientSide()) {
                    player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.dragon_armor_too_young", this.getDisplayName()));
                }
                return InteractionResult.FAIL;
            }
            if (!this.isWearingBodyArmor()) {
                if (!this.level().isClientSide()) {
                    this.setItemSlotAndDropWhenKilled(EquipmentSlot.BODY, stack.copyWithCount(1));
                    stack.consume(1, player);
                }
                return InteractionResult.SUCCESS;
            }
        }
        ItemStack armor = this.getBodyArmorItem();
        if (owner && armor.isDamaged() && armor.isValidRepairItem(stack)) {
            if (!this.level().isClientSide()) {
                armor.setDamageValue(Math.max(0, armor.getDamageValue() - armor.getMaxDamage() / 4));
                stack.consume(1, player);
                this.playSound(SoundEvents.ANVIL_USE, 0.6f, 1.2f);
            }
            return InteractionResult.SUCCESS;
        }

        if (owner && stack.isEmpty()) {
            if (player.isSecondaryUseActive()) {
                if (!this.level().isClientSide()) {
                    int mode = (this.getMode() + 1) % 3;
                    this.setMode(mode);
                    this.navigation.stop();
                    this.setTarget(null);
                    player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.dragon_mode." + mode, this.getDisplayName()));
                }
                return InteractionResult.SUCCESS;
            }
            if (this.getStage().rideable() && !this.isVehicle()) {
                if (!this.level().isClientSide()) {
                    this.setMode(MODE_FOLLOW);
                    player.startRiding(this);
                    player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.dragon_ride_hint",
                            Component.keybind("key.jump"), Component.keybind("key.kingdomsatwar.dragon_breath"),
                            Component.keybind("key.sneak")));
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    // =====================================================================================
    // Drachenpanzer (Körper-Platz)
    // =====================================================================================

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.BODY ? this.isTame() && this.getStage().rideable() : super.canUseSlot(slot);
    }

    /** Nur der Besitzer kann den Panzer mit einer Schere abnehmen. */
    @Override
    protected boolean canShearEquipment(Player player) {
        return this.isOwnedBy(player) && !this.isVehicle();
    }

    /** Treffer nutzen den Panzer ab. */
    @Override
    protected void hurtArmor(DamageSource source, float damage) {
        this.doHurtEquipment(source, damage, EquipmentSlot.BODY);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.DRAGON_FOOD.get()) || stack.is(ItemTags.MEAT);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean canMate(net.minecraft.world.entity.animal.Animal other) {
        return false;
    }

    @Override
    public boolean canFallInLove() {
        return false;
    }

    // =====================================================================================
    // KI
    // =====================================================================================

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1, new WarFlightGoal(this));
        this.goalSelector.addGoal(2, new BreathAttackGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this));
        this.goalSelector.addGoal(6, new FlyAroundGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        // Drachen sind friedlich: Sie verteidigen ihren Herrn und sich selbst, greifen aber nie von sich aus Spieler an
        // (wilde Drachen gibt es nicht mehr – jeder Drache schlüpft bei einem Spieler und gehört ihm)
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
    }

    /** Nie den eigenen Herrn, seine Tiere, seine Soldaten oder andere Verbündete angreifen (auch nicht nach einem Versehen). */
    @Override
    public boolean canAttack(LivingEntity target) {
        if (this.isTame() && DragonBreath.isAlly(this, target)) {
            return false;
        }
        return super.canAttack(target);
    }

    /** Einem Spieler gehören (Schlüpfen, Spawn-Ei, Füttern): folgt ihm ab sofort. */
    public void bondWith(Player player) {
        this.tame(player);
        this.setMode(MODE_FOLLOW);
        this.setTarget(null);
        this.navigation.stop();
        this.level().broadcastEntityEvent(this, (byte) 7);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        if (reason == EntitySpawnReason.SPAWN_ITEM_USE || reason == EntitySpawnReason.COMMAND) {
            this.applyStage(DragonStage.HATCHLING, true);
        }
        // Aus dem Spawn-Ei: Der Schlüpfling gehört dem, der das Ei benutzt hat (dem nächsten Spieler daneben)
        if (reason == EntitySpawnReason.SPAWN_ITEM_USE || reason == EntitySpawnReason.DISPENSER) {
            Player player = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 12.0, false);
            if (player != null && !player.isSpectator()) {
                this.bondWith(player);
            }
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    // =====================================================================================
    // Geräusche
    // =====================================================================================

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.getStage().number() <= 2 ? ModSounds.DRAGON_BABY_AMBIENT.get() : ModSounds.DRAGON_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.DRAGON_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DRAGON_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (this.getStage().number() >= 3) {
            this.playSound(ModSounds.DRAGON_STEP.get(), 0.4f * this.getStage().scale(), this.getVoicePitch());
        } else {
            super.playStepSound(pos, state);
        }
    }

    @Override
    public float getVoicePitch() {
        return switch (this.getStage()) {
            case HATCHLING -> 1.7f;
            case JUVENILE -> 1.4f;
            case ADOLESCENT -> 1.15f;
            case ADULT -> 0.95f;
            case ELDER -> 0.8f;
        } + (this.random.nextFloat() - 0.5f) * 0.1f;
    }

    @Override
    protected float getSoundVolume() {
        return 0.6f + this.getStage().scale() * 0.6f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        int count = this.getStage().number() * 2 + this.random.nextInt(3);
        this.spawnAtLocation(level, new ItemStack(ModItems.DRAGON_SCALES.get(this.dragonType).get(), count));
    }

    // =====================================================================================
    // Ziele (Goals)
    // =====================================================================================

    /** Folgt dem Besitzer – zu Fuß oder fliegend, wenn er weit weg oder hoch oben ist. */
    static class FollowOwnerGoal extends Goal {
        private final DragonEntity dragon;
        private @Nullable LivingEntity owner;
        private int recalc;

        FollowOwnerGoal(DragonEntity dragon) {
            this.dragon = dragon;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity owner = this.dragon.getOwner();
            if (owner == null || owner.isSpectator() || this.dragon.unableToMoveToOwner() || this.dragon.getMode() != MODE_FOLLOW) {
                return false;
            }
            if (this.dragon.distanceToSqr(owner) < sq(4 + this.dragon.getBbWidth())) {
                return false;
            }
            this.owner = owner;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.owner != null && !this.dragon.unableToMoveToOwner() && this.dragon.getMode() == MODE_FOLLOW
                    && this.dragon.distanceToSqr(this.owner) > sq(2.5 + this.dragon.getBbWidth());
        }

        @Override
        public void start() {
            this.recalc = 0;
        }

        @Override
        public void stop() {
            this.owner = null;
            this.dragon.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.owner == null) {
                return;
            }
            this.dragon.getLookControl().setLookAt(this.owner, 10.0f, this.dragon.getMaxHeadXRot());
            double dist = this.dragon.distanceToSqr(this.owner);
            boolean farOrHigh = dist > 14 * 14 || this.owner.getY() - this.dragon.getY() > 4;
            if (farOrHigh && this.dragon.getStage().canFly() && !this.dragon.isFlying()) {
                this.dragon.takeOff();
            }
            if (dist > 40 * 40) {
                this.dragon.tryToTeleportToOwner();
                return;
            }
            if (--this.recalc <= 0) {
                this.recalc = this.adjustedTickDelay(10);
                if (this.dragon.isFlying()) {
                    Vec3 target = this.owner.position().add(0, dist > 10 * 10 ? 3.0 : 0.5, 0);
                    this.dragon.getMoveControl().setWantedPosition(target.x, target.y, target.z, 1.2);
                } else {
                    this.dragon.getNavigation().moveTo(this.owner, 1.1);
                }
            }
        }
    }

    /** Ab und zu eine Runde durch die Luft fliegen (wilde oder umherstreifende Drachen). */
    static class FlyAroundGoal extends Goal {
        private final DragonEntity dragon;
        private @Nullable Vec3 target;
        private int ticks;

        FlyAroundGoal(DragonEntity dragon) {
            this.dragon = dragon;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!this.dragon.getStage().canFly() || this.dragon.isInSittingPose() || this.dragon.isVehicle()
                    || this.dragon.getTarget() != null) {
                return false;
            }
            boolean free = !this.dragon.isTame() || this.dragon.getMode() == MODE_WANDER;
            return free && this.dragon.getRandom().nextInt(this.dragon.isFlying() ? 20 : 400) == 0;
        }

        @Override
        public void start() {
            this.ticks = 0;
            this.dragon.takeOff();
            this.pickTarget();
        }

        private void pickTarget() {
            var rnd = this.dragon.getRandom();
            double angle = rnd.nextDouble() * Math.PI * 2;
            double radius = 8 + rnd.nextDouble() * 16;
            double x = this.dragon.getX() + Math.cos(angle) * radius;
            double z = this.dragon.getZ() + Math.sin(angle) * radius;
            int ground = this.dragon.level().getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
            double y = ground + 6 + rnd.nextDouble() * 10;
            this.target = new Vec3(x, y, z);
        }

        @Override
        public boolean canContinueToUse() {
            return this.dragon.isFlying() && this.ticks < 600 && !this.dragon.isInSittingPose() && this.dragon.getTarget() == null
                    && !this.dragon.isVehicle();
        }

        @Override
        public void tick() {
            this.ticks++;
            if (this.target == null || this.dragon.position().distanceToSqr(this.target) < 9) {
                if (this.ticks > 300) {
                    // Landen: Ziel auf den Boden legen
                    int ground = this.dragon.level().getHeight(Heightmap.Types.MOTION_BLOCKING, this.dragon.getBlockX(), this.dragon.getBlockZ());
                    this.target = new Vec3(this.dragon.getX(), ground, this.dragon.getZ());
                } else {
                    this.pickTarget();
                }
            }
            this.dragon.getMoveControl().setWantedPosition(this.target.x, this.target.y, this.target.z, 1.0);
        }
    }

    /**
     * Kriegsflug: Anflug in der Höhe (gut zwölf Blöcke über dem Ziel, seitlich versetzt), dann der Sturzflug mit Brüllen,
     * im Vorbeiflug der Odem auf das Ziel, danach wieder hinauf und herum für den nächsten Anflug. Ohne Ziel kreist der
     * Drache über dem Ort des Luftangriffs.
     */
    static class WarFlightGoal extends Goal {
        private static final int APPROACH = 0;
        private static final int DIVE = 1;
        private static final int CLIMB = 2;
        private final DragonEntity dragon;
        private int phase;
        private int phaseTicks;
        private double side = 1.0;
        private float orbit;
        private int roarCooldown;
        /** Wohin der Drache nach dem Sturzflug steigt (einmal festgelegt, nicht mitwandernd). */
        private @Nullable Vec3 climb;
        /** Wo der Sturzflug endet (hinter dem Ziel). */
        private @Nullable Vec3 diveEnd;

        WarFlightGoal(DragonEntity dragon) {
            this.dragon = dragon;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.dragon.onWarOrder() && DragonWar.canFight(this.dragon) && !this.dragon.isInSittingPose();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void start() {
            this.dragon.takeOff();
            this.phase = APPROACH;
            this.phaseTicks = 0;
            this.side = this.dragon.getRandom().nextBoolean() ? 1.0 : -1.0;
            this.orbit = this.dragon.getRandom().nextFloat() * Mth.TWO_PI;
        }

        @Override
        public void stop() {
            this.dragon.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        private void phase(int phase) {
            this.phase = phase;
            this.phaseTicks = 0;
        }

        private double groundAt(double x, double z) {
            return this.dragon.level().getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
        }

        private void flyTo(double x, double y, double z, double speed) {
            this.dragon.getMoveControl().setWantedPosition(x, Math.max(y, this.groundAt(x, z) + 2.5), z, speed);
        }

        @Override
        public void tick() {
            DragonEntity d = this.dragon;
            if (!d.isFlying()) {
                d.takeOff();
            }
            if (this.roarCooldown > 0) {
                this.roarCooldown--;
            }
            this.phaseTicks++;
            LivingEntity target = d.getTarget();
            if (target == null || !target.isAlive()) {
                // über dem Ort des Luftangriffs kreisen (oder auf der Stelle, bis das nächste Ziel feststeht)
                Vec3 c = d.strikePos() != null ? Vec3.atBottomCenterOf(d.strikePos()) : d.position();
                this.orbit += 0.045f;
                double r = 12.0 + d.getStage().scale() * 2.0;
                double x = c.x + Mth.cos(this.orbit) * r;
                double z = c.z + Mth.sin(this.orbit) * r;
                this.flyTo(x, this.groundAt(c.x, c.z) + 15.0, z, 1.0);
                this.phase(APPROACH);
                return;
            }
            Vec3 t = target.position();
            Vec3 flat = new Vec3(d.getX() - t.x, 0.0, d.getZ() - t.z);
            double horizontal = Math.max(0.1, flat.length());
            Vec3 away = flat.scale(1.0 / horizontal);
            double height = d.getY() - t.y;
            double range = d.getStage().breathRange();
            switch (this.phase) {
                case APPROACH -> {
                    // seitlich versetzt hinauf, gut zehn Blöcke über dem Ziel und sechzehn davor
                    Vec3 offset = away.yRot((float) (this.side * 0.5));
                    Vec3 p = t.add(offset.scale(16.0)).add(0.0, 10.0 + d.getStage().scale() * 2.0, 0.0);
                    this.flyTo(p.x, p.y, p.z, 1.2);
                    d.getLookControl().setLookAt(target, 10.0f, 30.0f);
                    boolean placed = height > 7.0 && horizontal > 9.0 && horizontal < 22.0;
                    if (placed || d.position().distanceToSqr(p) < 6.0 * 6.0 || this.phaseTicks > 140) {
                        this.phase(DIVE);
                        this.diveEnd = null;
                        if (this.roarCooldown <= 0) {
                            d.roar();
                            this.roarCooldown = 200;
                        }
                    }
                }
                case DIVE -> {
                    // im Sturzflug herab und knapp über das Ziel hinweg (der Endpunkt steht fest, sobald der Drache nah ist –
                    // sonst pendelte er über dem Ziel hin und her); während er speit, gleitet er langsamer
                    boolean close = d.distanceTo(target) < range + 3.0;
                    if (this.diveEnd == null || !close) {
                        this.diveEnd = t.add(away.scale(-9.0)).add(0.0, 3.0 + d.getStage().scale(), 0.0);
                    }
                    Vec3 p = this.diveEnd;
                    this.flyTo(p.x, p.y, p.z, d.isBreathing() ? 0.8 : 1.5);
                    d.getLookControl().setLookAt(target, 30.0f, 60.0f);
                    Vec3 toTarget = target.getEyePosition().subtract(d.headPosition()).normalize();
                    Vec3 heading = Vec3.directionFromRotation(0.0f, d.getYRot());
                    if (!d.isBreathing() && d.distanceTo(target) < range + 6.0
                            && heading.dot(new Vec3(toTarget.x, 0.0, toTarget.z).normalize()) > 0.3) {
                        d.startBreath(toTarget);
                    }
                    boolean moving = d.getDeltaMovement().x * (t.x - d.getX()) + d.getDeltaMovement().z * (t.z - d.getZ()) < 0.0;
                    boolean passed = horizontal > 5.0 && moving && this.phaseTicks > 12 || d.position().distanceToSqr(p) < 3.0 * 3.0;
                    if (passed && !d.isBreathing() || this.phaseTicks > 100) {
                        this.phase(CLIMB);
                        this.side = -this.side;
                        Vec3 forward = Vec3.directionFromRotation(0.0f, d.getYRot());
                        this.climb = d.position().add(forward.scale(12.0)).add(0.0, 8.0, 0.0);
                        d.nextStrikeTarget();
                    }
                }
                default -> {
                    // ein Stück weiter und hinauf, dann wenden
                    Vec3 c = this.climb != null ? this.climb : d.position().add(0.0, 6.0, 0.0);
                    this.flyTo(c.x, c.y, c.z, 1.3);
                    if (d.position().distanceToSqr(c) < 4.0 * 4.0 || this.phaseTicks > 40) {
                        this.phase(APPROACH);
                    }
                }
            }
        }
    }

    /** Odem-Angriff auf das aktuelle Ziel, wenn es nicht zu nah und nicht zu weit weg ist. */
    static class BreathAttackGoal extends Goal {
        private final DragonEntity dragon;

        BreathAttackGoal(DragonEntity dragon) {
            this.dragon = dragon;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.dragon.getTarget();
            if (target == null || !target.isAlive() || !this.dragon.getStage().canBreathe() || this.dragon.isVehicle()
                    || this.dragon.onWarOrder()) {
                return false;
            }
            double dist = this.dragon.distanceTo(target);
            return dist > 3 && dist < this.dragon.getStage().breathRange() && this.dragon.hasLineOfSight(target)
                    && this.dragon.breathCooldown <= 0 && this.dragon.getRandom().nextInt(10) == 0;
        }

        @Override
        public void start() {
            LivingEntity target = this.dragon.getTarget();
            if (target != null) {
                this.dragon.getLookControl().setLookAt(target, 30, 30);
                this.dragon.startBreath(target.getEyePosition().subtract(this.dragon.headPosition()).normalize());
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.dragon.isBreathing();
        }

        @Override
        public void tick() {
            LivingEntity target = this.dragon.getTarget();
            if (target != null) {
                this.dragon.getLookControl().setLookAt(target, 30, 30);
            }
        }
    }

    private static double sq(double v) {
        return v * v;
    }

    /** Alle Drachen in der Nähe (für Tests und Befehle). */
    public static List<DragonEntity> nearby(Level level, Vec3 center, double radius) {
        return level.getEntitiesOfClass(DragonEntity.class, new net.minecraft.world.phys.AABB(center, center).inflate(radius));
    }
}
