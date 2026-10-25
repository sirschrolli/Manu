package io.github.sirschrolli.kingdomsatwar.entity.dragon;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
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
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
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
 * wechseln (Folgen → Sitzen → Umherstreifen). Füttern mit Drachenfutter heilt und lässt schneller wachsen.
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
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.applyStage(DragonStage.byNumber(input.getIntOr("DragonStage", 1)), false);
        this.growth = input.getIntOr("DragonGrowth", 0);
        this.setMode(input.getIntOr("DragonMode", MODE_FOLLOW));
        this.setFlying(input.getBooleanOr("DragonFlying", false) && this.getStage().canFly());
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
        this.breathCooldown = BREATH_DURATION + 60;
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
    // Ticks
    // =====================================================================================

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.flapPhaseO = this.flapPhase;
            float speed = this.isFlying() ? (this.getDeltaMovement().y < -0.05 ? 0.08f : 0.32f) : 0.0f;
            this.flapPhase += speed;
            if (this.isFlying() && Mth.sin(this.flapPhase) > 0.98f && Mth.sin(this.flapPhaseO) <= 0.98f) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), ModSounds.DRAGON_FLAP.get(), this.getSoundSource(),
                        0.6f + this.getStage().scale() * 0.6f, this.getVoicePitch(), false);
            }
        }
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
        this.goalSelector.addGoal(2, new BreathAttackGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this));
        this.goalSelector.addGoal(6, new FlyAroundGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> !this.isTame() && this.getStage().number() >= 4 && target.distanceToSqr(this) < 144));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        if (reason == EntitySpawnReason.SPAWN_ITEM_USE || reason == EntitySpawnReason.COMMAND) {
            this.applyStage(DragonStage.HATCHLING, true);
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
            if (target == null || !target.isAlive() || !this.dragon.getStage().canBreathe() || this.dragon.isVehicle()) {
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
