package io.github.sirschrolli.kingdomsatwar.entity.soldier;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Prediction;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ein angeworbener Soldat. Er gehört dem Spieler, der ihn angeworben hat, und kennt drei Befehle:
 * <ul>
 *     <li><b>Folgen</b> – bleibt beim Besitzer (springt nach, wenn er zu weit zurückfällt)</li>
 *     <li><b>Stellung halten</b> – wartet an seinem Posten und verteidigt ihn</li>
 *     <li><b>Patrouille</b> – geht rund um seinen Posten auf und ab</li>
 * </ul>
 * Soldaten verteidigen ihren Besitzer, greifen Monster und Banditen an und verletzen keine Verbündeten.
 * Der Hauptmann stärkt Soldaten in seiner Nähe, Ritter decken sich mit dem Schild. Jeder Soldat hat einen
 * Namen und ein eigenes Gesicht; seine Stimme spricht die Sprache des jeweiligen Spielers.
 */
public class SoldierEntity extends PathfinderMob implements OwnableEntity, RangedAttackMob {
    private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> DATA_OWNER =
            SynchedEntityData.defineId(SoldierEntity.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
    private static final EntityDataAccessor<Byte> DATA_MODE = SynchedEntityData.defineId(SoldierEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(SoldierEntity.class, EntityDataSerializers.BYTE);

    public static final int MODE_FOLLOW = 0;
    public static final int MODE_HOLD = 1;
    public static final int MODE_PATROL = 2;
    /** Wie weit Befehle des Kommandostabs und das Sammeln von Verbündeten reichen. */
    public static final double COMMAND_RANGE = 48.0;

    /** Eigene Entity-Ereignisse (weit oberhalb der von Minecraft benutzten Nummern). */
    public static final byte EVENT_SALUTE = 112;
    public static final byte EVENT_CHEER = 113;
    public static final int GESTURE_TICKS = 40;

    private final SoldierType soldierType;
    private @Nullable BlockPos post;
    private int lastHurtTick = -1000;
    private int voiceCooldown;
    private int idleChatter = 1200;
    private int shieldTicks;
    private int shieldCooldown;

    // nur im Spiel (Client): Startzeitpunkte der Gesten für die Animation
    private int saluteStart = -1000;
    private int cheerStart = -1000;

    public SoldierEntity(EntityType<? extends SoldierEntity> type, Level level, SoldierType soldierType) {
        super(type, level);
        this.soldierType = soldierType;
        this.setBase(Attributes.MAX_HEALTH, soldierType.health());
        this.setBase(Attributes.ARMOR, soldierType.armor());
        this.setBase(Attributes.ARMOR_TOUGHNESS, soldierType.toughness());
        this.setBase(Attributes.ATTACK_DAMAGE, soldierType.attack());
        this.setBase(Attributes.MOVEMENT_SPEED, soldierType.speed());
        this.setBase(Attributes.KNOCKBACK_RESISTANCE, soldierType.knockback());
        this.setHealth(this.getMaxHealth());
        if (this.navigation instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
        }
        // Der Angriff hängt von der Soldatenart ab – darum erst hier, nachdem die Art bekannt ist
        if (soldierType.ranged()) {
            this.goalSelector.addGoal(3, new RangedBowAttackGoal<>(this, 1.0, 20, 18.0f));
        } else {
            this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.15, true));
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.setDropChance(slot, 0.0f);
        }
        this.setPersistenceRequired();
    }

    private void setBase(Holder<Attribute> attribute, double value) {
        var instance = this.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.ARMOR, 0.0)
                .add(Attributes.ARMOR_TOUGHNESS, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    public SoldierType getSoldierType() {
        return this.soldierType;
    }

    // =====================================================================================
    // Daten
    // =====================================================================================

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER, Optional.empty());
        builder.define(DATA_MODE, (byte) MODE_FOLLOW);
        builder.define(DATA_VARIANT, (byte) 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        EntityReference.store(this.getOwnerReference(), output, "Owner");
        output.putInt("SoldierMode", this.getMode());
        output.putInt("Variant", this.getVariant());
        if (this.post != null) {
            output.putInt("PostX", this.post.getX());
            output.putInt("PostY", this.post.getY());
            output.putInt("PostZ", this.post.getZ());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        EntityReference<LivingEntity> owner = EntityReference.read(input, "Owner");
        this.entityData.set(DATA_OWNER, Optional.ofNullable(owner));
        this.entityData.set(DATA_MODE, (byte) Mth.clamp(input.getIntOr("SoldierMode", MODE_FOLLOW), 0, 2));
        this.setVariant(input.getIntOr("Variant", 0));
        if (input.getIntOr("PostY", Integer.MIN_VALUE) != Integer.MIN_VALUE) {
            this.post = new BlockPos(input.getIntOr("PostX", 0), input.getIntOr("PostY", 0), input.getIntOr("PostZ", 0));
        } else {
            this.post = null;
        }
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getOwnerReference() {
        return this.entityData.get(DATA_OWNER).orElse(null);
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.entityData.set(DATA_OWNER, Optional.ofNullable(EntityReference.of(owner)));
    }

    public @Nullable UUID getOwnerId() {
        EntityReference<LivingEntity> owner = this.getOwnerReference();
        return owner == null ? null : owner.getUUID();
    }

    public boolean hasOwner() {
        return this.getOwnerReference() != null;
    }

    public boolean isOwnedBy(LivingEntity entity) {
        return entity.getUUID().equals(this.getOwnerId());
    }

    public int getMode() {
        return this.entityData.get(DATA_MODE);
    }

    public @Nullable BlockPos getPost() {
        return this.post;
    }

    /** Welches Gesicht (Hautton, Haare, Bart) der Soldat hat. */
    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, (byte) Mth.clamp(variant, 0, this.soldierType.variants() - 1));
    }

    /** Befehl geben; bei "Stellung halten" und "Patrouille" wird {@code post} (sonst die eigene Stelle) zum Posten. */
    public void setMode(int mode, @Nullable BlockPos post) {
        this.entityData.set(DATA_MODE, (byte) Mth.clamp(mode, 0, 2));
        this.post = mode == MODE_FOLLOW ? null : (post != null ? post.immutable() : this.blockPosition());
        this.navigation.stop();
    }

    // =====================================================================================
    // Anwerben
    // =====================================================================================

    /** Der Soldat tritt in den Dienst des Spielers: Er salutiert, meldet sich und folgt ihm. */
    public void recruitBy(Player player) {
        this.setOwner(player);
        this.setMode(MODE_FOLLOW, null);
        this.setTarget(null);
        this.salute();
        this.speak("recruit");
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.0, this.getZ(), 12, 0.4, 0.6, 0.4, 0.0);
        }
    }

    /** Ein zufälliger Name aus dem Namensbuch, z. B. "Ritter Konrad". */
    public void giveRandomName() {
        // Ritter tragen geschlossene Helme – hinter dem Visier kann jeder stecken
        boolean feminine = this.soldierType == SoldierType.KNIGHT ? this.random.nextInt(4) == 0 : this.soldierType.isFeminine(this.getVariant());
        String name = SoldierNames.random(this.random, feminine);
        this.setCustomName(Component.translatable("soldier.kingdomsatwar.name",
                Component.translatable(this.soldierType.translationKey()), name));
    }

    // =====================================================================================
    // Freund und Feind
    // =====================================================================================

    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        UUID owner = this.getOwnerId();
        if (owner != null) {
            if (other.getUUID().equals(owner)) {
                return true;
            }
            if (other instanceof OwnableEntity ownable && ownable.getOwnerReference() != null
                    && owner.equals(ownable.getOwnerReference().getUUID())) {
                return true;
            }
        } else if (other instanceof SoldierEntity soldier && !soldier.hasOwner()) {
            return true; // herrenlose Soldaten halten zusammen
        }
        return super.considersEntityAsAlly(other);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !this.isAlliedTo(target) && super.canAttack(target);
    }

    /** Monster (außer Creepern und "neutralen" Wesen wie Endermen) und Banditen sind Feinde. */
    private boolean isHostile(LivingEntity target) {
        if (!(target instanceof Enemy) || target instanceof Creeper || target instanceof NeutralMob || this.isAlliedTo(target)) {
            return false;
        }
        // wer Stellung hält, jagt keine Feinde weit weg vom Posten
        return this.post == null || this.getMode() == MODE_FOLLOW || target.blockPosition().closerThan(this.post, 20.0);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        Entity attacker = source.getEntity();
        if (attacker != null && this.isAlliedTo(attacker)) {
            return false; // kein Beschuss durch Verbündete und kein versehentlicher Schlag des Besitzers
        }
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt) {
            this.lastHurtTick = this.tickCount;
            if (source.getDirectEntity() instanceof Projectile && this.canRaiseShield()) {
                this.raiseShield(30);
            }
            if (attacker instanceof LivingEntity enemy && !this.isAlliedTo(enemy)) {
                this.rallyAllies(enemy);
            }
        }
        return hurt;
    }

    /** Verbündete Soldaten in der Nähe greifen den Angreifer ebenfalls an. */
    private void rallyAllies(LivingEntity enemy) {
        for (SoldierEntity ally : this.level().getEntitiesOfClass(SoldierEntity.class, this.getBoundingBox().inflate(16.0),
                s -> s != this && s.isAlliedTo(this) && s.getTarget() == null)) {
            ally.setTarget(enemy);
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity previous = this.getTarget();
        super.setTarget(target);
        if (target != null && previous == null && !this.level().isClientSide() && this.random.nextInt(3) == 0) {
            this.speak("attack");
        }
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity entity, DamageSource source) {
        if (this.random.nextInt(2) == 0) {
            this.speak("victory");
            this.cheer();
        }
        return super.killedEntity(level, entity, source);
    }

    /** Soldaten pflegen ihre Ausrüstung: Waffen und Schilde nutzen sich nicht ab, Pfeile gehen nicht aus. */
    @Override
    public boolean hasInfiniteMaterials() {
        return true;
    }

    // =====================================================================================
    // Kampf
    // =====================================================================================

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(this.level() instanceof ServerLevel level) || this.allyInLineOfFire(target)) {
            return;
        }
        ItemStack bow = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof BowItem));
        ItemStack arrowStack = new ItemStack(Items.ARROW);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, arrowStack, power, bow);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        Projectile.spawnProjectileUsingShoot(arrow, level, arrowStack, dx, dy + distance * 0.2, dz, 1.7f, 3.0f);
        this.playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
    }

    /** Steht ein Verbündeter zwischen Schütze und Ziel? Dann lieber nicht schießen. */
    private boolean allyInLineOfFire(LivingEntity target) {
        Vec3 from = this.getEyePosition();
        Vec3 to = target.getBoundingBox().getCenter();
        AABB box = new AABB(from, to).inflate(1.0);
        for (LivingEntity other : this.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e != target && this.isAlliedTo(e))) {
            if (other.getBoundingBox().inflate(0.4).clip(from, to).isPresent()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (this.isUsingItem()) {
            this.stopUsingItem(); // Schild runter zum Zuschlagen
        }
        return super.doHurtTarget(level, target);
    }

    @Override
    protected void playAttackSound() {
        this.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 0.7f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    private boolean canRaiseShield() {
        return this.soldierType == SoldierType.KNIGHT && this.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS)
                && this.shieldCooldown <= 0 && !this.isUsingItem();
    }

    private void raiseShield(int ticks) {
        this.startUsingItem(InteractionHand.OFF_HAND);
        this.shieldTicks = ticks;
    }

    /**
     * Ritter heben den Schild, wenn Pfeile fliegen oder ein Gegner auf Abstand angreift, und senken ihn,
     * sobald sie selbst zuschlagen.
     */
    private void updateShield() {
        if (this.soldierType != SoldierType.KNIGHT) {
            return;
        }
        if (this.shieldCooldown > 0) {
            this.shieldCooldown--;
        }
        LivingEntity target = this.getTarget();
        if (this.isUsingItem() && this.getUsedItemHand() == InteractionHand.OFF_HAND) {
            boolean close = target != null && this.distanceToSqr(target) < 3.2 * 3.2;
            if (--this.shieldTicks <= 0 || close || target == null && this.tickCount - this.lastHurtTick > 40) {
                this.stopUsingItem();
                this.shieldCooldown = 20 + this.random.nextInt(20);
            }
        } else if (target != null && this.canRaiseShield()) {
            double distance = this.distanceToSqr(target);
            if ((target instanceof RangedAttackMob && distance < 20.0 * 20.0) || (distance > 5.0 * 5.0 && distance < 9.0 * 9.0
                    && this.random.nextInt(30) == 0)) {
                this.raiseShield(40 + this.random.nextInt(20));
            }
        }
    }

    /** Ausrüstung je nach Soldatenart. */
    public void equip() {
        switch (this.soldierType) {
            case SWORDSMAN -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            case ARCHER -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            case KNIGHT -> {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.STEEL_SWORD.get()));
                this.setItemSlot(EquipmentSlot.OFFHAND, this.kingdomShield());
            }
            case CAPTAIN -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.STEEL_SWORD.get()));
        }
    }

    /** Ein Schild im Wappen des Königreichs: blau mit goldenem Kreuz und Rand. */
    private ItemStack kingdomShield() {
        ItemStack shield = new ItemStack(Items.SHIELD);
        var patterns = this.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);
        shield.set(DataComponents.BASE_COLOR, DyeColor.BLUE);
        shield.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers.Builder()
                .add(patterns.getOrThrow(BannerPatterns.STRAIGHT_CROSS), DyeColor.YELLOW)
                .add(patterns.getOrThrow(BannerPatterns.BORDER), DyeColor.YELLOW)
                .build());
        return shield;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        this.equip();
        this.setVariant(this.random.nextInt(this.soldierType.variants()));
        if (!this.hasCustomName()) {
            this.giveRandomName();
        }
        if (reason == EntitySpawnReason.SPAWN_ITEM_USE) {
            // aus dem Spawn-Ei: gehört dem Spieler, der das Ei benutzt hat
            Player player = level.getNearestPlayer(this, 8.0);
            if (player != null) {
                this.setOwner(player);
            }
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    // =====================================================================================
    // Ablauf
    // =====================================================================================

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.voiceCooldown > 0) {
            this.voiceCooldown--;
        }
        this.updateShield();
        // langsame Heilung außerhalb des Kampfes
        if (this.tickCount - this.lastHurtTick > 200 && this.tickCount % 80 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0f);
        }
        // der Hauptmann stärkt seine Leute
        if (this.soldierType == SoldierType.CAPTAIN && this.tickCount % 40 == 0) {
            for (SoldierEntity ally : level.getEntitiesOfClass(SoldierEntity.class, this.getBoundingBox().inflate(12.0),
                    s -> s == this || s.isAlliedTo(this))) {
                ally.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, 0, true, false), this);
                ally.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0, true, false), this);
            }
        }
        if (--this.idleChatter <= 0) {
            this.idleChatter = 1800 + this.random.nextInt(2400);
            if (this.getTarget() == null && level.getNearestPlayer(this, 8.0) != null) {
                this.speak("idle");
            }
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        super.playStepSound(pos, state);
        if (this.soldierType != SoldierType.ARCHER) {
            // leises Klirren der Rüstung
            this.playSound(ModSounds.SOLDIER_ARMOR.get(), this.soldierType == SoldierType.SWORDSMAN ? 0.15f : 0.25f,
                    1.25f + this.random.nextFloat() * 0.25f);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.hasOwner()) {
            return this.offerService(player, stack);
        }
        if (!this.isOwnedBy(player)) {
            return super.mobInteract(player, hand);
        }
        var food = stack.get(DataComponents.FOOD);
        if (food != null && this.getHealth() < this.getMaxHealth()) {
            if (!this.level().isClientSide()) {
                this.heal(Math.max(2.0f, food.nutrition() * 1.5f));
                stack.consume(1, player);
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight() + 0.2,
                        this.getZ(), 2, 0.25, 0.1, 0.25, 0.0);
                this.playSound(SoundEvents.GENERIC_EAT.value(), 0.8f, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }
        EquipmentSlot slot = this.weaponSlotFor(stack);
        if (slot != null) {
            if (!this.level().isClientSide()) {
                this.swapGear(player, hand, slot);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.isEmpty()) {
            if (!this.level().isClientSide()) {
                if (player.isSecondaryUseActive()) {
                    player.sendOverlayMessage(this.statusMessage());
                } else {
                    int mode = (this.getMode() + 1) % 3;
                    this.setMode(mode, null);
                    this.setTarget(null);
                    player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.soldier_order." + mode, this.getDisplayName())
                            .withStyle(ChatFormatting.GOLD));
                    this.salute();
                    this.speak(mode == MODE_FOLLOW ? "follow" : "hold");
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public Component statusMessage() {
        return Component.translatable("message.kingdomsatwar.soldier_status", this.getDisplayName(),
                Mth.ceil(this.getHealth()), Mth.ceil(this.getMaxHealth()),
                Component.translatable("message.kingdomsatwar.soldier_mode." + this.getMode()));
    }

    /**
     * Herrenlose Soldaten (z. B. Söldner) treten für ihren Sold in Münzen in den Dienst: Rechtsklick mit einer Münze in
     * der Hand bezahlt (mit Wechselgeld aus dem ganzen Inventar), sonst nennen sie ihren Preis.
     */
    private InteractionResult offerService(Player player, ItemStack stack) {
        int price = this.soldierType.coins();
        if (io.github.sirschrolli.kingdomsatwar.economy.Coin.of(stack.getItem()) != null
                && io.github.sirschrolli.kingdomsatwar.economy.Coins.canAfford(player, price)) {
            if (!this.level().isClientSide()) {
                io.github.sirschrolli.kingdomsatwar.economy.Coins.pay(player, price);
                this.recruitBy(player);
            }
            return InteractionResult.SUCCESS;
        }
        if (!this.level().isClientSide()) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.soldier_price", this.getDisplayName(),
                    io.github.sirschrolli.kingdomsatwar.economy.Coins.format(price)));
        }
        return InteractionResult.SUCCESS;
    }

    /** In welche Hand gehört dieser Gegenstand? Schwerter und Äxte für Nahkämpfer, Bögen für Bogenschützen, Schilde für Ritter. */
    private @Nullable EquipmentSlot weaponSlotFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (this.soldierType.ranged()) {
            return stack.getItem() instanceof BowItem ? EquipmentSlot.MAINHAND : null;
        }
        if (stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES)) {
            return EquipmentSlot.MAINHAND;
        }
        if (this.soldierType == SoldierType.KNIGHT && stack.has(DataComponents.BLOCKS_ATTACKS)) {
            return EquipmentSlot.OFFHAND;
        }
        return null;
    }

    /** Tauscht die Waffe: Der Soldat nimmt den Gegenstand und gibt seine bisherige (eigene) Ausrüstung zurück. */
    private void swapGear(Player player, InteractionHand hand, EquipmentSlot slot) {
        ItemStack given = player.getItemInHand(hand).split(1);
        ItemStack old = this.getItemBySlot(slot);
        boolean oldWasPlayers = this.getDropChances().byEquipment(slot) > 1.0f;
        this.setItemSlot(slot, given);
        this.setDropChance(slot, 2.0f); // Ausrüstung des Spielers fällt beim Tod sicher zu Boden
        if (!old.isEmpty() && oldWasPlayers) {
            player.getInventory().placeItemBackInInventory(old, Prediction.SERVER_ONLY);
        }
        this.playSound(SoundEvents.ARMOR_EQUIP_IRON.value(), 0.8f, 1.0f);
        this.salute();
        player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.soldier_equipped", this.getDisplayName(), given.getHoverName()));
    }

    /**
     * Spricht eine Zeile ("recruit", "follow", "hold", "attack", "victory", "idle"). Jeder Spieler in der Nähe
     * hört sie in seiner Sprache (Deutsch oder Englisch); jeder Soldat hat eine leicht eigene Stimmlage.
     */
    public void speak(String category) {
        if (!(this.level() instanceof ServerLevel level) || this.voiceCooldown > 0) {
            return;
        }
        this.voiceCooldown = 60;
        float pitch = this.voicePitch();
        long seed = level.getRandom().nextLong();
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) > 32.0 * 32.0) {
                continue;
            }
            String language = player.clientInformation().language().startsWith("de") ? "de" : "en";
            Holder<SoundEvent> sound = Holder.direct(SoundEvent.createVariableRangeEvent(
                    KingdomsAtWar.id("voice.soldier." + category + "." + language)));
            player.connection.send(new ClientboundSoundPacket(sound, SoundSource.NEUTRAL, this.getX(), this.getEyeY(), this.getZ(),
                    1.0f, pitch, seed));
        }
    }

    /** Jeder Soldat klingt etwas anders – abhängig von seiner Kennung, also immer gleich. */
    private float voicePitch() {
        return 0.92f + ((this.getUUID().hashCode() & 0xff) / 255.0f) * 0.16f;
    }

    // =====================================================================================
    // Gesten (Animation)
    // =====================================================================================

    /** Salutieren – beim Anwerben und bei neuen Befehlen. */
    public void salute() {
        this.level().broadcastEntityEvent(this, EVENT_SALUTE);
    }

    /** Jubeln mit erhobener Waffe – nach einem Sieg. */
    public void cheer() {
        this.level().broadcastEntityEvent(this, EVENT_CHEER);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_SALUTE) {
            this.saluteStart = this.tickCount;
        } else if (id == EVENT_CHEER) {
            this.cheerStart = this.tickCount;
        } else {
            super.handleEntityEvent(id);
        }
    }

    /** Fortschritt der Salut-Geste von 0 bis 1 (außerhalb der Geste 0). */
    public float saluteProgress(float partialTick) {
        return gestureProgress(this.saluteStart, partialTick);
    }

    /** Fortschritt der Jubel-Geste von 0 bis 1 (außerhalb der Geste 0). */
    public float cheerProgress(float partialTick) {
        return gestureProgress(this.cheerStart, partialTick);
    }

    private float gestureProgress(int start, float partialTick) {
        float t = (this.tickCount - start + partialTick) / GESTURE_TICKS;
        return t > 0.0f && t < 1.0f ? t : 0.0f;
    }

    // =====================================================================================
    // Geräusche
    // =====================================================================================

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null; // Soldaten sprechen über speak()
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SOLDIER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SOLDIER_DEATH.get();
    }

    @Override
    public float getVoicePitch() {
        return this.voicePitch();
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    // =====================================================================================
    // KI
    // =====================================================================================

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal());
        this.goalSelector.addGoal(5, new ReturnToPostGoal());
        this.goalSelector.addGoal(6, new PatrolGoal());
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new DefendOwnerGoal());
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 5, true, false,
                (target, level) -> this.isHostile(target)));
    }

    /** Folgt dem Besitzer; fällt der Soldat zu weit zurück, springt er zu ihm. */
    private final class FollowOwnerGoal extends Goal {
        private @Nullable LivingEntity owner;
        private int recalc;

        FollowOwnerGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (SoldierEntity.this.getMode() != MODE_FOLLOW) {
                return false;
            }
            LivingEntity owner = SoldierEntity.this.getOwner();
            if (owner == null || owner.isSpectator() || SoldierEntity.this.distanceToSqr(owner) < 5.0 * 5.0) {
                return false;
            }
            this.owner = owner;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.owner != null && this.owner.isAlive() && SoldierEntity.this.getMode() == MODE_FOLLOW
                    && SoldierEntity.this.distanceToSqr(this.owner) > 3.0 * 3.0;
        }

        @Override
        public void start() {
            this.recalc = 0;
        }

        @Override
        public void stop() {
            this.owner = null;
            SoldierEntity.this.navigation.stop();
        }

        @Override
        public void tick() {
            if (this.owner == null) {
                return;
            }
            SoldierEntity.this.getLookControl().setLookAt(this.owner, 10.0f, SoldierEntity.this.getMaxHeadXRot());
            if (--this.recalc > 0) {
                return;
            }
            this.recalc = this.adjustedTickDelay(10);
            if (SoldierEntity.this.distanceToSqr(this.owner) > 24.0 * 24.0 && this.owner.onGround()) {
                this.teleportNear(this.owner);
            } else {
                SoldierEntity.this.navigation.moveTo(this.owner, 1.1);
            }
        }

        private void teleportNear(LivingEntity owner) {
            BlockPos base = owner.blockPosition();
            SoldierEntity self = SoldierEntity.this;
            for (int i = 0; i < 10; i++) {
                BlockPos pos = base.offset(self.random.nextInt(5) - 2, 0, self.random.nextInt(5) - 2);
                if (self.level().getBlockState(pos.below()).isFaceSturdy(self.level(), pos.below(), net.minecraft.core.Direction.UP)
                        && self.level().noCollision(self, self.getBoundingBox().move(Vec3.atBottomCenterOf(pos).subtract(self.position())))) {
                    self.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, self.getYRot(), self.getXRot());
                    self.navigation.stop();
                    return;
                }
            }
        }
    }

    /** Wer Stellung hält oder patrouilliert, kehrt zu seinem Posten zurück, wenn er sich zu weit entfernt hat. */
    private final class ReturnToPostGoal extends Goal {
        ReturnToPostGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        private double limit() {
            return SoldierEntity.this.getMode() == MODE_PATROL ? 10.0 : 1.5;
        }

        @Override
        public boolean canUse() {
            BlockPos post = SoldierEntity.this.post;
            return SoldierEntity.this.getMode() != MODE_FOLLOW && post != null && SoldierEntity.this.getTarget() == null
                    && !SoldierEntity.this.blockPosition().closerThan(post, this.limit());
        }

        @Override
        public boolean canContinueToUse() {
            return SoldierEntity.this.getTarget() == null && SoldierEntity.this.post != null && !SoldierEntity.this.navigation.isDone();
        }

        @Override
        public void start() {
            BlockPos post = SoldierEntity.this.post;
            if (post != null) {
                SoldierEntity.this.navigation.moveTo(post.getX() + 0.5, post.getY(), post.getZ() + 0.5, 1.0);
            }
        }
    }

    /** Patrouille: in Ruhe gemütlich um den Posten herumgehen. */
    private final class PatrolGoal extends Goal {
        PatrolGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return SoldierEntity.this.getMode() == MODE_PATROL && SoldierEntity.this.post != null && SoldierEntity.this.getTarget() == null
                    && SoldierEntity.this.random.nextInt(this.reducedTickDelay(80)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return !SoldierEntity.this.navigation.isDone() && SoldierEntity.this.getTarget() == null;
        }

        @Override
        public void start() {
            BlockPos post = SoldierEntity.this.post;
            if (post == null) {
                return;
            }
            Vec3 target = DefaultRandomPos.getPosTowards(SoldierEntity.this, 8, 3, Vec3.atBottomCenterOf(post), Math.PI / 2);
            if (target != null) {
                SoldierEntity.this.navigation.moveTo(target.x, target.y, target.z, 0.7);
            }
        }
    }

    /** Greift an, wer den Besitzer verletzt hat oder wen der Besitzer angreift. */
    private final class DefendOwnerGoal extends TargetGoal {
        private int lastHurtBy;
        private int lastHurt;
        private @Nullable LivingEntity candidate;

        DefendOwnerGoal() {
            super(SoldierEntity.this, false);
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            LivingEntity owner = SoldierEntity.this.getOwner();
            if (owner == null) {
                return false;
            }
            LivingEntity attacker = owner.getLastHurtByMob();
            if (attacker != null && owner.getLastHurtByMobTimestamp() != this.lastHurtBy
                    && this.canAttack(attacker, TargetingConditions.DEFAULT) && !SoldierEntity.this.isAlliedTo(attacker)) {
                this.candidate = attacker;
                this.lastHurtBy = owner.getLastHurtByMobTimestamp();
                return true;
            }
            LivingEntity victim = owner.getLastHurtMob();
            if (victim != null && owner.getLastHurtMobTimestamp() != this.lastHurt
                    && this.canAttack(victim, TargetingConditions.DEFAULT) && !SoldierEntity.this.isAlliedTo(victim)) {
                this.candidate = victim;
                this.lastHurt = owner.getLastHurtMobTimestamp();
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            SoldierEntity.this.setTarget(this.candidate);
            super.start();
        }
    }

    /** Alle Soldaten eines Spielers in Reichweite (für Befehle). */
    public static List<SoldierEntity> soldiersOf(Player player, double range) {
        return player.level().getEntitiesOfClass(SoldierEntity.class, player.getBoundingBox().inflate(range), s -> s.isOwnedBy(player));
    }
}
