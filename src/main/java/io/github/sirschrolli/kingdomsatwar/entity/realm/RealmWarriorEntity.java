package io.github.sirschrolli.kingdomsatwar.entity.realm;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.war.Realm;
import io.github.sirschrolli.kingdomsatwar.war.RealmState;
import io.github.sirschrolli.kingdomsatwar.war.WarriorRole;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ein Krieger eines fremden Reichs: bewacht seinen Posten in der Stammburg, greift an, wer in die Burg eindringt (oder
 * mit dem Reich im Krieg liegt), schlägt Alarm und verfolgt Eindringlinge nur bis an den Rand der Burg. Schützen halten
 * ihre Stellung auf Mauer und Turm, Ritter und Herrscher verlassen den Thronsaal nicht.
 */
public class RealmWarriorEntity extends Monster implements CrossbowAttackMob {
    private static final EntityDataAccessor<Byte> DATA_REALM = SynchedEntityData.defineId(RealmWarriorEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(RealmWarriorEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_CHARGING = SynchedEntityData.defineId(RealmWarriorEntity.class,
            EntityDataSerializers.BOOLEAN);
    /** Bis wohin (Blöcke um die Burgmitte) Eindringlinge angegriffen und verfolgt werden. */
    public static final double CASTLE_RADIUS = 44.0;
    private static final double LEASH = 70.0;

    private final WarriorRole role;
    private @Nullable BlockPos post;
    private @Nullable BlockPos castle;
    private final @Nullable ServerBossEvent bossEvent;
    private int shieldTicks;
    private int shieldCooldown;
    private int lastHurtTick = -1000;
    private boolean drankPotion;

    public RealmWarriorEntity(EntityType<? extends RealmWarriorEntity> type, Level level, WarriorRole role) {
        super(type, level);
        this.role = role;
        this.setBase(Attributes.MAX_HEALTH, role.health());
        this.setBase(Attributes.ARMOR, role.armor());
        this.setBase(Attributes.ARMOR_TOUGHNESS, role.toughness());
        this.setBase(Attributes.ATTACK_DAMAGE, role.attack());
        this.setBase(Attributes.MOVEMENT_SPEED, role.speed());
        this.setBase(Attributes.KNOCKBACK_RESISTANCE, role.knockback());
        this.setHealth(this.getMaxHealth());
        this.xpReward = role.xp();
        if (this.navigation instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
        }
        if (role.ranged()) {
            this.goalSelector.addGoal(3, new HoldAndShootGoal());
        } else {
            this.goalSelector.addGoal(3, new MeleeAttackGoal(this, role == WarriorRole.LORD ? 1.1 : 1.15, true));
        }
        if (role.patrols()) {
            this.goalSelector.addGoal(6, new PatrolGoal());
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.setDropChance(slot, 0.05f);
        }
        if (role == WarriorRole.LORD) {
            this.bossEvent = new ServerBossEvent(Mth.createInsecureUUID(this.random), this.getDisplayName(), BossEvent.BossBarColor.RED,
                    BossEvent.BossBarOverlay.NOTCHED_10);
            this.setDropChance(EquipmentSlot.MAINHAND, 1.0f);
        } else {
            this.bossEvent = null;
        }
    }

    private void setBase(Holder<Attribute> attribute, double value) {
        var instance = this.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 0.0)
                .add(Attributes.ARMOR_TOUGHNESS, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(5, new ReturnToPostGoal());
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, RealmWarriorEntity.class).setAlertOthers(RealmWarriorEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> this.isIntruder(target)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SoldierEntity.class, 10, true, false,
                (target, level) -> this.isIntruder(target)));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                (target, level) -> !(target instanceof RealmWarriorEntity) && this.near(target, 14.0)));
    }

    public WarriorRole role() {
        return this.role;
    }

    // =====================================================================================
    // Daten
    // =====================================================================================

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_REALM, (byte) 0);
        builder.define(DATA_VARIANT, (byte) 0);
        builder.define(DATA_CHARGING, false);
    }

    public Realm realm() {
        return Realm.values()[Mth.clamp(this.entityData.get(DATA_REALM), 0, Realm.values().length - 1)];
    }

    public void setRealm(Realm realm) {
        this.entityData.set(DATA_REALM, (byte) realm.ordinal());
        if (this.bossEvent != null) {
            this.bossEvent.setName(this.getDisplayName());
            this.bossEvent.setColor(barColor(realm));
        }
    }

    public int variant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, (byte) Mth.clamp(variant, 0, this.role.variants() - 1));
    }

    public boolean isChargingCrossbow() {
        return this.entityData.get(DATA_CHARGING);
    }

    /** Posten (wo der Krieger steht) und Mitte seiner Burg. */
    public void setPost(BlockPos post, @Nullable BlockPos castle) {
        this.post = post.immutable();
        this.castle = castle == null ? null : castle.immutable();
        this.setHomeTo(post, (int) this.engageRadius() + 8);
    }

    public @Nullable BlockPos post() {
        return this.post;
    }

    public @Nullable BlockPos castle() {
        return this.castle;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Realm", this.realm().id());
        output.putInt("Variant", this.variant());
        if (this.post != null) {
            output.putInt("PostX", this.post.getX());
            output.putInt("PostY", this.post.getY());
            output.putInt("PostZ", this.post.getZ());
        }
        if (this.castle != null) {
            output.putInt("CastleX", this.castle.getX());
            output.putInt("CastleY", this.castle.getY());
            output.putInt("CastleZ", this.castle.getZ());
        }
        output.putBoolean("DrankPotion", this.drankPotion);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setRealm(Realm.byId(input.getStringOr("Realm", Realm.EISENMARK.id())));
        this.setVariant(input.getIntOr("Variant", 0));
        this.castle = input.getInt("CastleX").isPresent()
                ? new BlockPos(input.getIntOr("CastleX", 0), input.getIntOr("CastleY", 0), input.getIntOr("CastleZ", 0)) : null;
        if (input.getInt("PostX").isPresent()) {
            this.setPost(new BlockPos(input.getIntOr("PostX", 0), input.getIntOr("PostY", 0), input.getIntOr("PostZ", 0)), this.castle);
        }
        this.drankPotion = input.getBooleanOr("DrankPotion", false);
    }

    // =====================================================================================
    // Namen
    // =====================================================================================

    @Override
    protected Component getTypeName() {
        if (this.role == WarriorRole.LORD) {
            return this.realm().ruler();
        }
        return Component.translatable("entity.kingdomsatwar.realm_warrior.of", super.getTypeName(), this.realm().shortName());
    }

    private static BossEvent.BossBarColor barColor(Realm realm) {
        return switch (realm) {
            case EISENMARK -> BossEvent.BossBarColor.PURPLE;
            case RABENSTEIN -> BossEvent.BossBarColor.RED;
            case DORNFELS -> BossEvent.BossBarColor.YELLOW;
            case SILBERAU -> BossEvent.BossBarColor.GREEN;
        };
    }

    // =====================================================================================
    // Wer ist Feind?
    // =====================================================================================

    /** Wie weit sich der Krieger vom Posten löst, um zu kämpfen. */
    private double engageRadius() {
        return switch (this.role) {
            case KNIGHT -> 14.0;
            case LORD -> 12.0;
            case ARCHER, CROSSBOWMAN -> 4.0;
            default -> 40.0;
        };
    }

    /** Wer in die Burg eindringt (oder mit dem Reich im Krieg liegt), ist Feind. */
    public boolean isIntruder(LivingEntity target) {
        if (target instanceof RealmWarriorEntity || !target.isAlive()) {
            return false;
        }
        if (this.castle == null) {
            return this.distanceToSqr(target) < 16.0 * 16.0;
        }
        double dx = target.getX() - (this.castle.getX() + 0.5);
        double dz = target.getZ() - (this.castle.getZ() + 0.5);
        double dy = target.getY() - this.castle.getY();
        boolean inside = Math.abs(dx) < CASTLE_RADIUS && Math.abs(dz) < CASTLE_RADIUS && dy > -12 && dy < 48;
        if (this.level() instanceof ServerLevel level && target instanceof Player player) {
            RealmState state = RealmState.of(level);
            if (state.isConquered(this.realm())) {
                return false;
            }
            if (state.atWar(this.realm(), player.getUUID()) && dx * dx + dz * dz < 64.0 * 64.0) {
                return true;
            }
        }
        if (!inside) {
            return false;
        }
        // Ritter und Herrscher greifen nur an, wer in den Thronsaal kommt; Schützen, wen sie treffen können
        return this.role.ranged() ? this.distanceToSqr(target) < 26.0 * 26.0
                : this.post == null || target.distanceToSqr(Vec3.atCenterOf(this.post)) < Math.pow(this.engageRadius() + 4.0, 2);
    }

    private boolean near(LivingEntity target, double radius) {
        BlockPos home = this.post != null ? this.post : this.blockPosition();
        return target.distanceToSqr(Vec3.atCenterOf(home)) < radius * radius;
    }

    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        if (other instanceof RealmWarriorEntity warrior) {
            return warrior.realm() == this.realm();
        }
        return super.considersEntityAsAlly(other);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity before = this.getTarget();
        super.setTarget(target);
        if (before == null && target != null && (target instanceof Player || target instanceof SoldierEntity)) {
            this.raiseAlarm(target);
        }
    }

    /** Alarm: die Krieger in der Nähe greifen denselben Eindringling an, die Glocke der Burg läutet. */
    private void raiseAlarm(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        AABB box = this.getBoundingBox().inflate(24.0, 10.0, 24.0);
        for (RealmWarriorEntity other : level.getEntitiesOfClass(RealmWarriorEntity.class, box,
                w -> w != this && w.realm() == this.realm() && w.getTarget() == null && w.isIntruder(target))) {
            other.setTarget(target);
        }
        if (this.castle != null && target instanceof ServerPlayer player
                && RealmState.of(level).alarm(this.realm(), level.getGameTime())) {
            level.playSound(null, this.castle.above(8), SoundEvents.BELL_BLOCK, this.getSoundSource(), 3.0f, 0.8f);
            player.sendSystemMessage(Component.translatable("realm.kingdomsatwar.alarm", this.realm().displayName()), true);
        }
    }

    // =====================================================================================
    // Laufen, Kämpfen, Schild
    // =====================================================================================

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        LivingEntity target = this.getTarget();
        // nicht über die Burg hinaus verfolgen
        if (target != null && this.castle != null) {
            double dx = target.getX() - this.castle.getX();
            double dz = target.getZ() - this.castle.getZ();
            boolean warTarget = target instanceof Player player && RealmState.of(level).atWar(this.realm(), player.getUUID());
            double leash = warTarget ? LEASH + 20.0 : LEASH;
            if (dx * dx + dz * dz > leash * leash || this.post != null && !this.role.ranged()
                    && this.distanceToSqr(Vec3.atCenterOf(this.post)) > Math.pow(this.engageRadius() + 16.0, 2)) {
                this.setTarget(null);
            }
        }
        this.updateShield();
        if (this.role == WarriorRole.LORD) {
            this.lordTick(level);
        }
    }

    private boolean hasShield() {
        return this.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS);
    }

    /** Wer einen Schild trägt, hebt ihn gegen Schützen und auf dem Weg zum Gegner und senkt ihn zum Zuschlagen. */
    private void updateShield() {
        if (!this.hasShield()) {
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
        } else if (target != null && this.shieldCooldown <= 0 && !this.isUsingItem()) {
            double distance = this.distanceToSqr(target);
            boolean shooter = target.getMainHandItem().getItem() instanceof BowItem || target.getMainHandItem().getItem() instanceof CrossbowItem;
            if (shooter && distance < 20.0 * 20.0 && distance > 4.0 * 4.0 || distance > 5.0 * 5.0 && distance < 9.0 * 9.0
                    && this.random.nextInt(30) == 0) {
                this.startUsingItem(InteractionHand.OFF_HAND);
                this.shieldTicks = 40 + this.random.nextInt(20);
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (this.isUsingItem()) {
            this.stopUsingItem();
        }
        return super.doHurtTarget(level, target);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        this.lastHurtTick = this.tickCount;
        return super.hurtServer(level, source, amount);
    }

    /** Der Herrscher: Leiste für die Spieler in der Nähe, einmal ein Heiltrank in höchster Not, Wut bei halber Kraft. */
    private void lordTick(ServerLevel level) {
        if (this.bossEvent == null) {
            return;
        }
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.tickCount % 10 == 0) {
            for (ServerPlayer player : level.players()) {
                boolean close = player.distanceToSqr(this) < 28.0 * 28.0 && this.getTarget() != null;
                if (close) {
                    this.bossEvent.addPlayer(player);
                } else {
                    this.bossEvent.removePlayer(player);
                }
            }
        }
        if (!this.drankPotion && this.getHealth() < this.getMaxHealth() * 0.3f && this.getTarget() != null) {
            this.drankPotion = true;
            this.heal(60.0f);
            this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 400, 0));
            this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 400, 0));
            this.playSound(SoundEvents.GENERIC_DRINK.value(), 1.0f, 0.9f);
            this.playSound(SoundEvents.RAVAGER_ROAR, 0.6f, 1.4f);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (this.bossEvent != null) {
            this.bossEvent.removePlayer(player);
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.bossEvent != null) {
            this.bossEvent.removeAllPlayers();
        }
        if (this.level() instanceof ServerLevel level) {
            RealmState state = RealmState.of(level);
            if (this.role == WarriorRole.LORD) {
                state.lordFallen(this.realm());
                state.save(level);
                Component message = Component.translatable("realm.kingdomsatwar.lord_fallen", this.realm().ruler(), this.realm().displayName());
                for (ServerPlayer player : level.players()) {
                    if (player.distanceToSqr(this) < 96.0 * 96.0) {
                        player.sendSystemMessage(message);
                    }
                }
            } else {
                state.warriorFallen(this.realm());
                state.save(level);
            }
            if (source.getEntity() instanceof Player player) {
                state.declareWar(this.realm(), player.getUUID());
                state.save(level);
            }
        }
    }

    // =====================================================================================
    // Fernkampf
    // =====================================================================================

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (this.role.crossbow()) {
            this.performCrossbowAttack(this, 1.6f);
            return;
        }
        if (!(this.level() instanceof ServerLevel level)) {
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
        Projectile.spawnProjectileUsingShoot(arrow, level, arrowStack, dx, dy + distance * 0.2, dz, 1.7f, 4.0f);
        this.playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
    }

    @Override
    public void setChargingCrossbow(boolean charging) {
        this.entityData.set(DATA_CHARGING, charging);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override
    public boolean canUseNonMeleeWeapon(ItemStack stack) {
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    // =====================================================================================
    // Ausrüstung und Erscheinen
    // =====================================================================================

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        switch (this.role) {
            case FOOTMAN -> {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                this.setItemSlot(EquipmentSlot.OFFHAND, this.realmShield());
            }
            case ARCHER -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            case CROSSBOWMAN -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
            case KNIGHT -> {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.STEEL_SWORD.get()));
                this.setItemSlot(EquipmentSlot.OFFHAND, this.realmShield());
            }
            case CAPTAIN -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.STEEL_AXE.get()));
            case LORD -> {
                ItemStack sword = new ItemStack(ModItems.STEEL_SWORD.get());
                var enchantments = this.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                sword.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 2);
                sword.enchant(enchantments.getOrThrow(Enchantments.UNBREAKING), 2);
                this.setItemSlot(EquipmentSlot.MAINHAND, sword);
            }
        }
    }

    /** Ein Schild im Wappen des Reichs. */
    private ItemStack realmShield() {
        ItemStack shield = new ItemStack(Items.SHIELD);
        shield.set(DataComponents.BASE_COLOR, this.realm().field());
        shield.set(DataComponents.BANNER_PATTERNS, this.realm().heraldry(this.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
        return shield;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        if (reason == EntitySpawnReason.SPAWN_ITEM_USE || reason == EntitySpawnReason.COMMAND || reason == EntitySpawnReason.DISPENSER) {
            this.setRealm(Realm.values()[this.random.nextInt(Realm.values().length)]);
        }
        this.setVariant(this.random.nextInt(this.role.variants()));
        this.populateDefaultEquipmentSlots(this.random, difficulty);
        this.setPersistenceRequired();
        return data;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SOLDIER_HURT.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return ModSounds.SOLDIER_DEATH.get();
    }

    // =====================================================================================
    // Ziele
    // =====================================================================================

    /** Ohne Gegner zurück zum Posten. */
    private final class ReturnToPostGoal extends Goal {
        ReturnToPostGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        private double limit() {
            return RealmWarriorEntity.this.role.patrols() ? 7.0 : 1.2;
        }

        @Override
        public boolean canUse() {
            BlockPos post = RealmWarriorEntity.this.post;
            return post != null && RealmWarriorEntity.this.getTarget() == null
                    && !RealmWarriorEntity.this.blockPosition().closerThan(post, this.limit());
        }

        @Override
        public boolean canContinueToUse() {
            return RealmWarriorEntity.this.getTarget() == null && RealmWarriorEntity.this.post != null
                    && !RealmWarriorEntity.this.navigation.isDone();
        }

        @Override
        public void start() {
            BlockPos post = RealmWarriorEntity.this.post;
            if (post != null) {
                RealmWarriorEntity.this.navigation.moveTo(post.getX() + 0.5, post.getY(), post.getZ() + 0.5, 0.9);
            }
        }
    }

    /** Fußknechte und Hauptmann gehen in Ruhe gemächlich um ihren Posten. */
    private final class PatrolGoal extends Goal {
        PatrolGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return RealmWarriorEntity.this.post != null && RealmWarriorEntity.this.getTarget() == null
                    && RealmWarriorEntity.this.random.nextInt(this.reducedTickDelay(120)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return !RealmWarriorEntity.this.navigation.isDone() && RealmWarriorEntity.this.getTarget() == null;
        }

        @Override
        public void start() {
            BlockPos post = RealmWarriorEntity.this.post;
            if (post == null) {
                return;
            }
            Vec3 target = DefaultRandomPos.getPosTowards(RealmWarriorEntity.this, 6, 2, Vec3.atBottomCenterOf(post), Math.PI / 2);
            if (target != null) {
                RealmWarriorEntity.this.navigation.moveTo(target.x, target.y, target.z, 0.6);
            }
        }
    }

    /**
     * Schützen halten ihre Stellung: sie zielen, solange sie den Gegner sehen und er in Reichweite ist, spannen Bogen oder
     * Armbrust und schießen – ohne die Mauer zu verlassen.
     */
    private final class HoldAndShootGoal extends Goal {
        private static final double RANGE = 26.0;
        private int cooldown;
        private int seen;

        HoldAndShootGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = RealmWarriorEntity.this.getTarget();
            return target != null && target.isAlive() && RealmWarriorEntity.this.distanceToSqr(target) < RANGE * RANGE;
        }

        @Override
        public void start() {
            RealmWarriorEntity.this.setAggressive(true);
            RealmWarriorEntity.this.navigation.stop();
        }

        @Override
        public void stop() {
            RealmWarriorEntity.this.setAggressive(false);
            this.seen = 0;
            if (RealmWarriorEntity.this.isUsingItem()) {
                RealmWarriorEntity.this.stopUsingItem();
            }
            RealmWarriorEntity.this.setChargingCrossbow(false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            RealmWarriorEntity self = RealmWarriorEntity.this;
            LivingEntity target = self.getTarget();
            if (target == null) {
                return;
            }
            self.getLookControl().setLookAt(target, 30.0f, 30.0f);
            boolean visible = self.getSensing().hasLineOfSight(target);
            this.seen = visible ? this.seen + 1 : 0;
            if (this.cooldown > 0) {
                this.cooldown--;
            }
            if (self.role.crossbow()) {
                this.crossbow(self, target, visible);
            } else {
                this.bow(self, target, visible);
            }
        }

        private void bow(RealmWarriorEntity self, LivingEntity target, boolean visible) {
            if (self.isUsingItem()) {
                if (!visible && this.seen < -40) {
                    self.stopUsingItem();
                    return;
                }
                int ticks = self.getTicksUsingItem();
                if (visible && ticks >= 20) {
                    self.stopUsingItem();
                    self.performRangedAttack(target, BowItem.getPowerForTime(ticks));
                    this.cooldown = 20 + self.random.nextInt(15);
                }
            } else if (this.cooldown <= 0 && visible && this.seen > 5) {
                self.startUsingItem(ProjectileUtil.getWeaponHoldingHand(self, item -> item instanceof BowItem));
            }
        }

        private void crossbow(RealmWarriorEntity self, LivingEntity target, boolean visible) {
            ItemStack weapon = self.getItemInHand(ProjectileUtil.getWeaponHoldingHand(self, item -> item instanceof CrossbowItem));
            if (!(weapon.getItem() instanceof CrossbowItem)) {
                return;
            }
            if (CrossbowItem.isCharged(weapon)) {
                if (visible && this.seen > 8 && this.cooldown <= 0) {
                    self.performRangedAttack(target, 1.0f);
                    this.cooldown = 25 + self.random.nextInt(20);
                }
            } else if (self.isUsingItem()) {
                if (self.getTicksUsingItem() >= CrossbowItem.getChargeDuration(weapon, self)) {
                    self.releaseUsingItem();
                    self.setChargingCrossbow(false);
                }
            } else if (this.cooldown <= 0) {
                self.startUsingItem(ProjectileUtil.getWeaponHoldingHand(self, item -> item instanceof CrossbowItem));
                self.setChargingCrossbow(true);
            }
        }
    }
}
