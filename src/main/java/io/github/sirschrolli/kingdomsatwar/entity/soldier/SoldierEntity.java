package io.github.sirschrolli.kingdomsatwar.entity.soldier;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeCrew;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeWeaponEntity;
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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Prediction;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.SwingAnimation;
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
 * Der Hauptmann stärkt Soldaten in seiner Nähe, Ritter decken sich mit dem Schild, Schildträger ziehen die Angreifer
 * ihrer Leute auf sich, Speerträger treffen Reiter besonders hart, Waffenknechte treffen mit dem Streitkolben auch die
 * Nebenleute, Lanzenreiter reiten mit gesenkter Lanze an, Armbrustschützen spannen und schießen durchschlagende Bolzen,
 * Belagerungsknechte bessern Belagerungswaffen aus. Jeder Soldat hat einen Namen und ein eigenes Gesicht; seine Stimme
 * spricht die Sprache des jeweiligen Spielers.
 */
public class SoldierEntity extends PathfinderMob implements OwnableEntity, RangedAttackMob {
    private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> DATA_OWNER =
            SynchedEntityData.defineId(SoldierEntity.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
    private static final EntityDataAccessor<Byte> DATA_MODE = SynchedEntityData.defineId(SoldierEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(SoldierEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_RANK = SynchedEntityData.defineId(SoldierEntity.class, EntityDataSerializers.BYTE);
    /** Söldner ohne Herrn: die Söldnertafel des Söldnerhauses, zu dem er gehört (auch der Client braucht sie für die Tafel). */
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_HALL =
            SynchedEntityData.defineId(SoldierEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    /** Proviant: Tagesrationen im Gepäck, ob er gerade hungert, ob er auf einem Schlachtzug ist (für den Feldherrentisch). */
    private static final EntityDataAccessor<Byte> DATA_RATIONS = SynchedEntityData.defineId(SoldierEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_HUNGRY = SynchedEntityData.defineId(SoldierEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CAMPAIGN = SynchedEntityData.defineId(SoldierEntity.class, EntityDataSerializers.BOOLEAN);
    /** Kampferfahrung (die Stufe folgt daraus, siehe {@link io.github.sirschrolli.kingdomsatwar.army.Veterancy}). */
    private static final EntityDataAccessor<Integer> DATA_EXPERIENCE = SynchedEntityData.defineId(SoldierEntity.class, EntityDataSerializers.INT);

    /**
     * Ränge: 0 Rekrut, 1 Veteran, 2 Elite, 3 Meister, 4 Legende (am Feldherrentisch befördert, ab einer Stufe der
     * Kampferfahrung – oder als Söldner schon erfahren).
     */
    public static final int MAX_RANK = 4;
    private static final net.minecraft.resources.Identifier RANK_MODIFIER = KingdomsAtWar.id("rank");
    private static final net.minecraft.resources.Identifier LEVEL_MODIFIER = KingdomsAtWar.id("level");
    /** Für einen Schlag: der Speer gegen Reiter, Pferde und große Ungetüme. */
    private static final net.minecraft.resources.Identifier SPEAR_BONUS = KingdomsAtWar.id("spear_bonus");

    public static final int MODE_FOLLOW = 0;
    public static final int MODE_HOLD = 1;
    public static final int MODE_PATROL = 2;
    /** Wie weit Befehle des Kommandostabs und das Sammeln von Verbündeten reichen. */
    public static final double COMMAND_RANGE = 48.0;

    /** Eigene Entity-Ereignisse (weit oberhalb der von Minecraft benutzten Nummern). */
    public static final byte EVENT_SALUTE = 112;
    public static final byte EVENT_CHEER = 113;
    public static final int GESTURE_TICKS = 40;
    /** So schnell fliegt ein Armbrustbolzen (ein Pfeil vom Bogen: 1,7). */
    private static final float BOLT_SPEED = 2.6f;

    private final SoldierType soldierType;
    private @Nullable BlockPos post;
    /** Wache eines Dorfs: bewacht es, lässt sich nicht anwerben und schützt die Dorfbewohner. */
    private String village = "";
    /** Diese Dorfwache ruft beim ersten Erwachen den Eisenwächter des Dorfs herbei (einmal je Dorf). */
    private boolean callsSentinel;
    /** Der eigene Name (ohne Art und Rang), z. B. "Konrad". */
    private String personalName = "";
    private int lastHurtTick = -1000;
    /** Kampferfahrung unter einem ganzen Punkt (aus kleinen Treffern), wird beim nächsten Treffer weitergezählt. */
    private float experienceRemainder;
    /** Wann eine Legende zuletzt den zweiten Atem hatte (-1: noch nie). */
    private long lastSecondWind = -1;
    /** Der Tag (seit Weltbeginn), an dem er zuletzt eine Ration gegessen hat ({@code -1}: noch nie). */
    /** Tag der letzten Mahlzeit ({@link Long#MIN_VALUE}: noch nie – dann kommt er satt an). */
    private long fedDay = Long.MIN_VALUE;
    private int voiceCooldown;
    private int idleChatter = 1200;
    private int shieldTicks;
    private int shieldCooldown;
    /** Schildträger: bis zur nächsten Herausforderung an die Gegner. */
    private int tauntCooldown = 40;
    /** Lanzenreiter: soll ein (neues) Schlachtross bekommen; die Kennung seines Rosses. */
    private boolean steedWanted;
    private @Nullable UUID steedId;

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
        if (soldierType.crossbow()) {
            this.goalSelector.addGoal(3, new CrossbowGoal());
        } else if (soldierType.ranged()) {
            this.goalSelector.addGoal(3, new RangedBowAttackGoal<>(this, 1.0, 20, 18.0f));
        } else {
            if (soldierType.mounted()) {
                this.goalSelector.addGoal(2, new LanceChargeGoal());
            }
            if (soldierType == SoldierType.ENGINEER) {
                this.goalSelector.addGoal(3, new RepairSiegeGoal());
            }
            this.goalSelector.addGoal(3, new MeleeAttackGoal(this, soldierType.mounted() ? 1.0 : 1.15, true));
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
        builder.define(DATA_RANK, (byte) 0);
        builder.define(DATA_HALL, Optional.empty());
        builder.define(DATA_RATIONS, (byte) 0);
        builder.define(DATA_HUNGRY, false);
        builder.define(DATA_CAMPAIGN, false);
        builder.define(DATA_EXPERIENCE, 0);
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
        if (!this.village.isEmpty()) {
            output.putString("Village", this.village);
        }
        if (this.callsSentinel) {
            output.putBoolean("CallsSentinel", true);
        }
        output.putInt("Rank", this.getRank());
        output.putInt("Experience", this.getCombatExperience());
        if (!this.personalName.isEmpty()) {
            output.putString("PersonalName", this.personalName);
        }
        BlockPos hall = this.getHall();
        if (hall != null) {
            output.putInt("HallX", hall.getX());
            output.putInt("HallY", hall.getY());
            output.putInt("HallZ", hall.getZ());
        }
        output.putInt("Rations", this.getRations());
        output.putLong("FedDay", this.fedDay);
        output.putBoolean("Hungry", this.isHungry());
        output.putBoolean("Campaign", this.isOnCampaign());
        if (this.steedWanted) {
            output.putBoolean("SteedWanted", true);
        }
        if (this.steedId != null) {
            output.store("Steed", net.minecraft.core.UUIDUtil.CODEC, this.steedId);
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
        this.village = input.getStringOr("Village", "");
        this.callsSentinel = input.getBooleanOr("CallsSentinel", false);
        this.entityData.set(DATA_RANK, (byte) Mth.clamp(input.getIntOr("Rank", 0), 0, MAX_RANK));
        this.applyRank();
        // Soldaten aus älteren Spielständen haben noch keine Kampferfahrung: Sie bekommen so viel, wie ihr Rang voraussetzt
        int rankFloor = io.github.sirschrolli.kingdomsatwar.army.Veterancy.total(io.github.sirschrolli.kingdomsatwar.army.Veterancy.minLevel(this.getRank()));
        this.entityData.set(DATA_EXPERIENCE, Math.max(input.getIntOr("Experience", rankFloor), 0));
        this.applyLevel();
        this.personalName = input.getStringOr("PersonalName", "");
        this.entityData.set(DATA_HALL, input.getIntOr("HallY", Integer.MIN_VALUE) != Integer.MIN_VALUE
                ? Optional.of(new BlockPos(input.getIntOr("HallX", 0), input.getIntOr("HallY", 0), input.getIntOr("HallZ", 0)))
                : Optional.empty());
        // Soldaten aus älteren Spielständen haben noch keinen Proviant-Eintrag: Sie bekommen den Anfangsproviant
        this.entityData.set(DATA_RATIONS, (byte) Mth.clamp(input.getIntOr("Rations",
                io.github.sirschrolli.kingdomsatwar.army.Provisions.START_RATIONS), 0, io.github.sirschrolli.kingdomsatwar.army.Provisions.MAX_RATIONS));
        this.fedDay = input.getLongOr("FedDay", Long.MIN_VALUE);
        this.entityData.set(DATA_HUNGRY, input.getBooleanOr("Hungry", false));
        // ältere Soldaten mit Herrn folgten ihm: sie gelten als auf dem Schlachtzug, bis man sie heimschickt
        this.entityData.set(DATA_CAMPAIGN, input.getBooleanOr("Campaign", this.getOwnerReference() != null));
        this.steedWanted = input.getBooleanOr("SteedWanted", false);
        this.steedId = input.read("Steed", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
    }

    // =====================================================================================
    // Proviant und Schlachtzug
    // =====================================================================================

    /** Wie viele Tagesrationen er noch im Gepäck hat. */
    public int getRations() {
        return this.entityData.get(DATA_RATIONS);
    }

    public void setRations(int rations) {
        this.entityData.set(DATA_RATIONS, (byte) Mth.clamp(rations, 0, io.github.sirschrolli.kingdomsatwar.army.Provisions.MAX_RATIONS));
    }

    /** Hungert er (keine Ration mehr für heute)? Dann heilt er nicht von selbst und schlägt schwächer zu. */
    public boolean isHungry() {
        return this.entityData.get(DATA_HUNGRY);
    }

    /** Isst seine Ration für den Tag {@code day} – aus dem Gepäck ({@code fromStock}) oder frisch zugeteilt. */
    public void eatRation(long day, boolean fromStock) {
        if (fromStock) {
            this.setRations(this.getRations() - 1);
        }
        this.fedDay = day;
        this.entityData.set(DATA_HUNGRY, false);
    }

    /** Für die Vorschau: hungrig (oder satt) machen, ohne den Herrn zu rufen. */
    public void setHungry(boolean hungry) {
        this.entityData.set(DATA_HUNGRY, hungry);
    }

    /** Für Tests: so tun, als hätte er am Tag {@code day} zuletzt gegessen. */
    public void setFedDay(long day) {
        this.fedDay = day;
    }

    /** Ist er mit seinem Herrn auf einem Schlachtzug (statt in der Garnison der Siedlung)? */
    public boolean isOnCampaign() {
        return this.entityData.get(DATA_CAMPAIGN);
    }

    public void setCampaign(boolean campaign) {
        this.entityData.set(DATA_CAMPAIGN, campaign);
    }

    /**
     * Jeden Morgen isst ein Soldat mit Herrn seine Mahlzeit: in der Garnison aus dem Lager der Siedlung (wie die Bürger),
     * auf dem Schlachtzug – oder wenn das Lager leer ist – eine Ration aus seinem Gepäck. Ist nichts da, hungert er, bis
     * das Lager wieder gefüllt ist oder man ihm am Feldherrentisch Proviant zuteilt. Nebenbei trägt er sich in die
     * Stammrolle seines Herrn ein (ältere Spielstände). Läuft alle fünf Sekunden (öffentlich für die Tests).
     */
    public void rationTick(ServerLevel level) {
        UUID owner = this.getOwnerId();
        if (owner == null) {
            return;
        }
        if (!io.github.sirschrolli.kingdomsatwar.army.ArmyRoster.of(level.getServer()).contains(owner, this.getUUID())) {
            io.github.sirschrolli.kingdomsatwar.army.ArmyRoster.enlist(level.getServer(), owner, this.getUUID());
        }
        long day = io.github.sirschrolli.kingdomsatwar.army.Provisions.day(level);
        if (this.fedDay == Long.MIN_VALUE || this.fedDay > day) {
            this.fedDay = day;   // neu – oder die Zeit wurde zurückgestellt
            return;
        }
        if (this.fedDay >= day) {
            return;
        }
        if (!this.isOnCampaign() && this.eatFromTown(level)) {
            this.eatRation(day, false);
        } else if (this.getRations() > 0) {
            this.eatRation(day, true);
        } else if (!this.isHungry()) {
            this.entityData.set(DATA_HUNGRY, true);
            if (this.getOwner() instanceof ServerPlayer player && player.distanceToSqr(this) < 64.0 * 64.0) {
                player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.soldier_hungry", this.getDisplayName())
                        .withStyle(ChatFormatting.RED));
            }
        }
    }

    /** In der Garnison: eine Mahlzeit aus dem Lager der eigenen Siedlung (wenn genug Essen darin ist). */
    private boolean eatFromTown(ServerLevel level) {
        io.github.sirschrolli.kingdomsatwar.economy.town.Town town = io.github.sirschrolli.kingdomsatwar.army.Garrison.townOf(this);
        if (town == null || town.dimension() != level.dimension()) {
            return false;
        }
        int meal = io.github.sirschrolli.kingdomsatwar.economy.town.TownFood.MEAL;
        return io.github.sirschrolli.kingdomsatwar.economy.town.TownFood.pantry(level, town) >= meal
                && io.github.sirschrolli.kingdomsatwar.economy.town.TownFood.take(level, town, meal) >= meal;
    }

    // =====================================================================================
    // Rang
    // =====================================================================================

    public int getRank() {
        return this.entityData.get(DATA_RANK);
    }

    /** Setzt den Rang: mehr Leben (+25 % je Rang), Rüstung (+2), Schaden (+1) und Standfestigkeit; der Name zeigt die Sterne. */
    public void setRank(int rank) {
        this.entityData.set(DATA_RANK, (byte) Mth.clamp(rank, 0, MAX_RANK));
        float fraction = this.getHealth() / Math.max(1.0f, this.getMaxHealth());
        this.applyRank();
        this.setHealth(Math.max(1.0f, fraction * this.getMaxHealth()));
        this.refreshName();
    }

    private void applyRank() {
        int rank = this.getRank();
        this.rankModifier(Attributes.MAX_HEALTH, 0.25 * rank, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        this.rankModifier(Attributes.ARMOR, 2.0 * rank, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
        this.rankModifier(Attributes.ATTACK_DAMAGE, 1.0 * rank, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
        this.rankModifier(Attributes.KNOCKBACK_RESISTANCE, 0.05 * rank,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
    }

    private void rankModifier(Holder<Attribute> attribute, double amount, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation op) {
        this.modifier(RANK_MODIFIER, attribute, amount, op);
    }

    private void modifier(net.minecraft.resources.Identifier id, Holder<Attribute> attribute, double amount,
            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation op) {
        var instance = this.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        if (amount != 0.0) {
            instance.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(id, amount, op));
        }
    }

    // =====================================================================================
    // Kampferfahrung und Stufen
    // =====================================================================================

    /** Gesammelte Kampferfahrung. */
    public int getCombatExperience() {
        return this.entityData.get(DATA_EXPERIENCE);
    }

    /** Die Stufe (1 bis 20) aus der Kampferfahrung. */
    public int getCombatLevel() {
        return io.github.sirschrolli.kingdomsatwar.army.Veterancy.levelFor(this.getCombatExperience());
    }

    /** Setzt die Kampferfahrung (Befehle, Tests, Söldner) – Leben, Schaden und Rüstung folgen der Stufe. */
    public void setCombatExperience(int experience) {
        float missing = this.getMaxHealth() - this.getHealth();
        this.entityData.set(DATA_EXPERIENCE, Mth.clamp(experience, 0,
                io.github.sirschrolli.kingdomsatwar.army.Veterancy.total(io.github.sirschrolli.kingdomsatwar.army.Veterancy.MAX_LEVEL)));
        this.applyLevel();
        this.setHealth(Math.max(1.0f, this.getMaxHealth() - missing));
    }

    /**
     * Sammelt Kampferfahrung (Bruchteile werden aufgehoben). Steigt er dabei eine Stufe auf, wächst sein Leben mit (die
     * Wunden bleiben, wie sie sind) und es wird gefeiert.
     */
    public void gainCombatExperience(float amount) {
        if (amount <= 0.0f || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        int cap = io.github.sirschrolli.kingdomsatwar.army.Veterancy.total(io.github.sirschrolli.kingdomsatwar.army.Veterancy.MAX_LEVEL);
        if (this.getCombatExperience() >= cap) {
            return;
        }
        this.experienceRemainder += amount;
        int whole = (int) this.experienceRemainder;
        if (whole <= 0) {
            return;
        }
        this.experienceRemainder -= whole;
        int before = this.getCombatLevel();
        float missing = this.getMaxHealth() - this.getHealth();
        this.entityData.set(DATA_EXPERIENCE, Math.min(cap, this.getCombatExperience() + whole));
        int after = this.getCombatLevel();
        if (after > before) {
            this.applyLevel();
            this.setHealth(Math.max(1.0f, this.getMaxHealth() - missing));
            io.github.sirschrolli.kingdomsatwar.army.Veterancy.celebrate(level, this, before, after);
        }
    }

    /** Je Stufe über der ersten: +3 % Leben, +0,15 Schaden, +0,1 Rüstung. */
    private void applyLevel() {
        int steps = this.getCombatLevel() - 1;
        this.modifier(LEVEL_MODIFIER, Attributes.MAX_HEALTH, io.github.sirschrolli.kingdomsatwar.army.Veterancy.HEALTH_PER_LEVEL * steps,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        this.modifier(LEVEL_MODIFIER, Attributes.ATTACK_DAMAGE, io.github.sirschrolli.kingdomsatwar.army.Veterancy.DAMAGE_PER_LEVEL * steps,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
        this.modifier(LEVEL_MODIFIER, Attributes.ARMOR, io.github.sirschrolli.kingdomsatwar.army.Veterancy.ARMOR_PER_LEVEL * steps,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
    }

    /** Der eigene Name ohne Art und Rang (bei alten Soldaten aus dem gespeicherten Namen gelesen). */
    public String getPersonalName() {
        if (this.personalName.isEmpty() && this.getCustomName() != null
                && this.getCustomName().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t
                && t.getKey().startsWith("soldier.kingdomsatwar.name") && t.getArgs().length >= 2) {
            Object arg = t.getArgs()[1];
            this.personalName = arg instanceof Component c ? c.getString() : String.valueOf(arg);
        }
        return this.personalName;
    }

    /** Baut den angezeigten Namen neu: Art, Name und – ab Veteran – Sterne für den Rang ("Ritter Konrad ★★"). */
    public void refreshName() {
        String name = this.getPersonalName();
        if (name.isEmpty()) {
            return;
        }
        int rank = this.getRank();
        this.setCustomName(rank == 0
                ? Component.translatable("soldier.kingdomsatwar.name", Component.translatable(this.soldierType.translationKey()), name)
                : Component.translatable("soldier.kingdomsatwar.name.ranked", Component.translatable(this.soldierType.translationKey()), name,
                        "★".repeat(rank)));
    }

    /**
     * Welches Gesicht ein Soldat bei der Ausbildung zu einer anderen Art bekommt: eine Frau bleibt eine Frau, ein Mann ein
     * Mann (soweit die neue Art das Gesicht zeigt), sonst möglichst dieselbe Nummer.
     */
    static int faceFor(SoldierType from, int variant, SoldierType to) {
        int n = to.variants();
        boolean woman = from.isFeminine(variant);
        boolean hasWoman = to.isFeminine(n - 1);
        if (woman && hasWoman) {
            return n - 1;
        }
        return Mth.clamp(variant, 0, hasWoman ? Math.max(0, n - 2) : n - 1);
    }

    /** Übernimmt alles Persönliche von einem anderen Soldaten (bei der Beförderung zu einer anderen Art). */
    public void copyPersonFrom(SoldierEntity other) {
        this.entityData.set(DATA_OWNER, other.entityData.get(DATA_OWNER));
        this.entityData.set(DATA_MODE, (byte) other.getMode());
        this.post = other.post;
        this.village = other.village;
        this.setVariant(faceFor(other.soldierType, other.getVariant(), this.soldierType));
        this.personalName = other.getPersonalName();
        this.entityData.set(DATA_RANK, (byte) other.getRank());
        this.applyRank();
        this.entityData.set(DATA_EXPERIENCE, other.getCombatExperience());
        this.experienceRemainder = other.experienceRemainder;
        this.applyLevel();
        this.setHealth(Math.max(1.0f, other.getHealth() / Math.max(1.0f, other.getMaxHealth()) * this.getMaxHealth()));
        this.refreshName();
        this.entityData.set(DATA_RATIONS, (byte) other.getRations());
        this.entityData.set(DATA_HUNGRY, other.isHungry());
        this.entityData.set(DATA_CAMPAIGN, other.isOnCampaign());
        this.fedDay = other.fedDay;
        if (this.level() instanceof ServerLevel level && this.getOwnerId() != null) {
            io.github.sirschrolli.kingdomsatwar.army.ArmyRoster.enlist(level.getServer(), this.getOwnerId(), this.getUUID());
        }
    }

    // =====================================================================================
    // Söldner
    // =====================================================================================

    /** Macht den Soldaten zum Söldner des Söldnerhauses mit der Söldnertafel bei {@code board}; er wartet an {@code spot}. */
    public void waitForHire(BlockPos board, BlockPos spot) {
        this.entityData.set(DATA_HALL, Optional.of(board.immutable()));
        this.setMode(MODE_HOLD, spot);
    }

    /** Die Söldnertafel, an der dieser Söldner auf Arbeit wartet ({@code null}: kein Söldner). */
    public @Nullable BlockPos getHall() {
        return this.entityData.get(DATA_HALL).orElse(null);
    }

    /** Ein Söldner, der im Söldnerhaus auf einen Herrn wartet? */
    public boolean isMercenary() {
        return !this.hasOwner() && this.getHall() != null;
    }

    /** In den Dienst nehmen (mit Bezahlung schon erledigt): verlässt das Söldnerhaus und zieht in die Garnison des Spielers. */
    public void hireBy(Player player) {
        this.entityData.set(DATA_HALL, Optional.empty());
        this.recruitBy(player);
    }

    /**
     * Sold eines Söldners in Bronze: der Sold der Art, dazu der Wert der Ausrüstung, die er mitbringt (im Dorf
     * zahlt man nur mit Münzen), je Rang die Hälfte mehr – auf ganze Silber aufgerundet.
     */
    public int hirePrice() {
        int base = this.soldierType.coins() + switch (this.soldierType) {
            case KNIGHT -> 54;
            case CAPTAIN, MAN_AT_ARMS -> 81;
            case SPEARMAN -> 9;
            case CROSSBOWMAN, ENGINEER -> 18;
            case SHIELDBEARER -> 27;
            case LANCER -> 108;
            default -> 0;
        };
        int price = (int) Math.ceil(base * (1.0 + 0.5 * this.getRank()) * (1.0 + 0.04 * (this.getCombatLevel() - 1)));
        return (price + 8) / 9 * 9;
    }

    /** Macht den Soldaten zur Wache des Dorfs {@code name} (ohne Herrn, auf Patrouille um {@code post}). */
    public void guardVillage(String name, BlockPos post) {
        this.village = name;
        this.setMode(MODE_PATROL, post);
    }

    /** Diese Wache holt beim ersten Erwachen den Eisenwächter ihres Dorfs auf den Platz. */
    public void callSentinel() {
        this.callsSentinel = true;
    }

    /** Wache eines Dorfs (lässt sich nicht anwerben)? */
    public boolean isVillageGuard() {
        return !this.village.isEmpty() && !this.hasOwner();
    }

    public String getVillage() {
        return this.village;
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getOwnerReference() {
        return this.entityData.get(DATA_OWNER).orElse(null);
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.entityData.set(DATA_OWNER, Optional.ofNullable(EntityReference.of(owner)));
        if (this.level() instanceof ServerLevel level) {
            if (owner != null) {
                io.github.sirschrolli.kingdomsatwar.army.ArmyRoster.enlist(level.getServer(), owner.getUUID(), this.getUUID());
            } else {
                io.github.sirschrolli.kingdomsatwar.army.ArmyRoster.discharge(level.getServer(), this.getUUID());
            }
        }
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
        this.getNavigation().stop();
    }

    // =====================================================================================
    // Anwerben
    // =====================================================================================

    /**
     * Der Soldat tritt in den Dienst des Spielers: Er salutiert, meldet sich, bekommt Proviant für ein paar Tage und
     * zieht in die Garnison der Siedlung des Spielers. Hat der Spieler (noch) keine Siedlung, folgt er ihm.
     */
    public void recruitBy(Player player) {
        this.enlist(player);
        if (io.github.sirschrolli.kingdomsatwar.army.Garrison.station(this)) {
            io.github.sirschrolli.kingdomsatwar.economy.town.Town town = io.github.sirschrolli.kingdomsatwar.army.Garrison.townOf(this);
            player.sendSystemMessage(Component.translatable("message.kingdomsatwar.garrison.joined", this.getDisplayName(),
                    town == null ? "" : town.name()).withStyle(ChatFormatting.GOLD));
        } else {
            this.setCampaign(true);
            this.setMode(MODE_FOLLOW, null);
        }
    }

    /** Wie {@link #recruitBy}, aber er bleibt beim Spieler und folgt ihm (z. B. herbeigerufene Truppen). */
    public void recruitIntoField(Player player) {
        this.enlist(player);
        this.setCampaign(true);
        this.setMode(MODE_FOLLOW, null);
    }

    private void enlist(Player player) {
        this.setOwner(player);
        this.setTarget(null);
        this.setRations(io.github.sirschrolli.kingdomsatwar.army.Provisions.START_RATIONS);
        this.entityData.set(DATA_HUNGRY, false);
        if (this.level() instanceof ServerLevel level) {
            this.fedDay = io.github.sirschrolli.kingdomsatwar.army.Provisions.day(level);
        }
        this.salute();
        this.speak("recruit");
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.0, this.getZ(), 12, 0.4, 0.6, 0.4, 0.0);
        }
    }

    /** Ein zufälliger Name aus dem Namensbuch, z. B. "Ritter Konrad". */
    public void giveRandomName() {
        // Ritter, Waffenknechte und Lanzenreiter tragen geschlossene Helme – hinter dem Visier kann jeder stecken
        boolean feminine = this.soldierType.helmeted() ? this.random.nextInt(4) == 0 : this.soldierType.isFeminine(this.getVariant());
        this.personalName = SoldierNames.random(this.random, feminine);
        this.refreshName();
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
        if (other instanceof Warhorse horse && (other.getUUID().equals(this.steedId) || horse.rider() != null && horse.rider() != this
                && this.considersEntityAsAlly(horse.rider()))) {
            return true; // das eigene Ross und die Rosse der Kameraden
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
        if (source.is(DamageTypes.IN_WALL) && this.getVehicle() instanceof Warhorse) {
            return false; // im Sattel unter einem niedrigen Durchgang
        }
        if (this.soldierType == SoldierType.MAN_AT_ARMS && source.is(DamageTypeTags.IS_PROJECTILE)) {
            damage *= 0.7f; // am Plattenpanzer gleiten viele Pfeile ab
        }
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt) {
            this.lastHurtTick = this.tickCount;
            if (io.github.sirschrolli.kingdomsatwar.army.Veterancy.secondWind(level, this, this.lastSecondWind)) {
                this.lastSecondWind = this.tickCount;
            }
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
            if (this.soldierType == SoldierType.SHIELDBEARER) {
                this.shieldCooldown = 8 + this.random.nextInt(8);
            }
        }
        float bonus = this.spearBonus(target);
        AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (bonus > 1.0f && damage != null) {
            damage.addOrUpdateTransientModifier(new AttributeModifier(SPEAR_BONUS, bonus - 1.0f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        boolean hit;
        try {
            hit = super.doHurtTarget(level, target);
        } finally {
            if (damage != null) {
                damage.removeModifier(SPEAR_BONUS);
            }
        }
        if (hit && bonus > 1.0f) {
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.6), target.getZ(), 10, 0.3, 0.3, 0.3, 0.25);
        }
        if (hit && this.soldierType == SoldierType.MAN_AT_ARMS) {
            this.cleave(level, target);
        }
        return hit;
    }

    /**
     * Speer und Lanze: Reiter, Pferde und große Ungetüme trifft der lange Spieß besonders hart (der Speerträger ist eigens
     * dafür ausgebildet).
     */
    private float spearBonus(Entity target) {
        if (!this.soldierType.spear() || !this.getMainHandItem().is(ItemTags.SPEARS)) {
            return 1.0f;
        }
        boolean big = target.isPassenger() || target.isVehicle() || target instanceof AbstractHorse || target.getBbWidth() > 1.2f
                || target.getBbHeight() > 2.6f;
        return !big ? 1.0f : this.soldierType == SoldierType.SPEARMAN ? 1.75f : 1.3f;
    }

    /**
     * Waffenknecht: der Streitkolben trifft auch die Gegner neben dem Ziel – mit halber Wucht – und wirft sie zurück.
     */
    private void cleave(ServerLevel level, Entity target) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5f;
        DamageSource source = this.damageSources().mobAttack(this);
        boolean any = false;
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(1.75, 0.5, 1.75),
                e -> e != target && e != this && e.isAlive() && !this.isAlliedTo(e) && this.isFoe(e))) {
            if (other.hurtServer(level, source, damage)) {
                other.knockback(0.55, this.getX() - other.getX(), this.getZ() - other.getZ(), source, damage);
                any = true;
            }
        }
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.MACE_SMASH_GROUND, SoundSource.NEUTRAL, 0.55f,
                0.9f + this.random.nextFloat() * 0.2f);
        if (any) {
            double yaw = this.getYRot() * Mth.DEG_TO_RAD;
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() - Math.sin(yaw) * 1.2, this.getY(0.55), this.getZ() + Math.cos(yaw) * 1.2,
                    1, 0.0, 0.0, 0.0, 0.0);
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.9f, 0.8f);
        }
    }

    /** Wer gegen uns kämpft: Monster und Banditen – und wer gerade einen der Unseren angreift. */
    private boolean isFoe(LivingEntity entity) {
        if (entity instanceof Enemy && !(entity instanceof NeutralMob)) {
            return true;
        }
        return entity instanceof Mob mob && mob.getTarget() != null && (mob.getTarget() == this || this.isAlliedTo(mob.getTarget()));
    }

    /** Speer und Lanze treffen keine Verbündeten – auch nicht beim Anreiten durch die eigenen Reihen. */
    @Override
    public boolean stabAttack(EquipmentSlot slot, Entity target, float damage, boolean dealsDamage, boolean dealsKnockback, boolean dismounts) {
        if (this.isAlliedTo(target) || target instanceof Warhorse horse && horse.rider() != null && this.isAlliedTo(horse.rider())) {
            return false;
        }
        return super.stabAttack(slot, target, damage * this.spearBonus(target), dealsDamage, dealsKnockback, dismounts);
    }

    /**
     * Mit Speer und Lanze reicht der Stoß weit; wer dem Speerträger zu nah auf den Leib rückt, bekommt trotzdem den Schaft
     * zu spüren (anders als bei Vanilla, wo der Speer erst auf Abstand trifft).
     */
    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        var range = this.getActiveItem().get(DataComponents.ATTACK_RANGE);
        if (range != null) {
            return this.getAttackBoundingBox(range.effectiveMaxRange(this)).intersects(target.getHitbox());
        }
        return super.isWithinMeleeAttackRange(target);
    }

    @Override
    protected void playAttackSound() {
        this.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 0.7f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    private boolean canRaiseShield() {
        return this.soldierType.shielded() && this.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS)
                && this.shieldCooldown <= 0 && !this.isUsingItem();
    }

    private void raiseShield(int ticks) {
        this.startUsingItem(InteractionHand.OFF_HAND);
        this.shieldTicks = ticks;
    }

    /**
     * Ritter heben den Schild, wenn Pfeile fliegen oder ein Gegner auf Abstand angreift, und senken ihn, sobald sie selbst
     * zuschlagen. Schildträger halten ihn fast immer oben, sobald ein Gegner in Sicht ist – sie senken ihn nur kurz für
     * den eigenen Hieb.
     */
    private void updateShield() {
        if (!this.soldierType.shielded()) {
            return;
        }
        if (this.shieldCooldown > 0) {
            this.shieldCooldown--;
        }
        boolean bearer = this.soldierType == SoldierType.SHIELDBEARER;
        LivingEntity target = this.getTarget();
        if (this.isUsingItem() && this.getUsedItemHand() == InteractionHand.OFF_HAND) {
            boolean close = !bearer && target != null && this.distanceToSqr(target) < 3.2 * 3.2;
            if (--this.shieldTicks <= 0 || close || target == null && this.tickCount - this.lastHurtTick > 40) {
                this.stopUsingItem();
                this.shieldCooldown = bearer ? 6 + this.random.nextInt(8) : 20 + this.random.nextInt(20);
            }
        } else if (target != null && this.canRaiseShield()) {
            double distance = this.distanceToSqr(target);
            if (bearer ? distance < 16.0 * 16.0 : (target instanceof RangedAttackMob && distance < 20.0 * 20.0) || (distance > 5.0 * 5.0
                    && distance < 9.0 * 9.0 && this.random.nextInt(30) == 0)) {
                this.raiseShield(bearer ? 60 + this.random.nextInt(40) : 40 + this.random.nextInt(20));
            }
        }
    }

    /**
     * Schildträger: schlägt mit dem Schwert auf den Schild und zieht die Gegner in der Nähe, die seine Leute angreifen, auf
     * sich – alle paar Sekunden.
     */
    private void taunt(ServerLevel level) {
        if (--this.tauntCooldown > 0) {
            return;
        }
        this.tauntCooldown = 20;
        List<Mob> foes = level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(10.0, 4.0, 10.0),
                m -> m.isAlive() && m.getTarget() != null && m.getTarget() != this && this.isAlliedTo(m.getTarget()) && !this.isAlliedTo(m)
                        && this.hasLineOfSight(m));
        int drawn = 0;
        for (Mob foe : foes) {
            // wer nah ist, kann die Herausforderung nicht überhören; weiter weg folgt nicht jeder
            if (foe.distanceToSqr(this) < 6.0 * 6.0 || this.random.nextFloat() < 0.6f) {
                foe.setTarget(this);
                level.sendParticles(ParticleTypes.ANGRY_VILLAGER, foe.getX(), foe.getEyeY() + 0.5, foe.getZ(), 1, 0.15, 0.1, 0.15, 0.0);
                drawn++;
            }
        }
        if (drawn > 0) {
            this.tauntCooldown = 80 + this.random.nextInt(40);
            this.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.NEUTRAL, 1.0f, 0.75f);
            this.speak("attack");
            if (this.getTarget() == null) {
                this.setTarget(foes.getFirst());
            }
        }
    }

    /** Für Tests: fordert die Gegner sofort heraus. */
    public void tauntNow(ServerLevel level) {
        this.tauntCooldown = 0;
        this.taunt(level);
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
            case SPEARMAN -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SPEAR));
            case CROSSBOWMAN -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
            case SHIELDBEARER -> {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                this.setItemSlot(EquipmentSlot.OFFHAND, this.kingdomShield());
            }
            case MAN_AT_ARMS -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.MACE));
            case LANCER -> {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SPEAR));
                this.steedWanted = !this.isPassenger();
            }
            case ENGINEER -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
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
            // eine Legende umgibt ein feiner goldener Schimmer
            if (this.getRank() == MAX_RANK && this.random.nextInt(7) == 0) {
                this.level().addParticle(new net.minecraft.core.particles.DustParticleOptions(this.random.nextBoolean() ? 0xF2C14E : 0xFFE9A8, 0.55f),
                        this.getRandomX(0.6), this.getY() + 0.3 + this.random.nextDouble() * 1.5, this.getRandomZ(0.6), 0.0, 0.02, 0.0);
            }
            return;
        }
        if (this.voiceCooldown > 0) {
            this.voiceCooldown--;
        }
        if (this.callsSentinel && this.tickCount > 20 && this.post != null) {
            this.callsSentinel = false;
            io.github.sirschrolli.kingdomsatwar.entity.creature.golem.VillageSentinels.summon(level, this.post);
        }
        if (!this.isNoAi()) {
            this.updateShield();
            if (this.soldierType == SoldierType.SHIELDBEARER) {
                this.taunt(level);
            }
            if (this.soldierType.mounted()) {
                this.tendSteed(level);
            }
        }
        if ((this.tickCount + this.getId()) % 100 == 0) {
            this.rationTick(level);
        }
        // langsame Heilung außerhalb des Kampfes – nur wer satt ist; ein Meister (und eine Legende) erholt sich doppelt so schnell
        boolean master = this.getRank() >= 3;
        if (this.tickCount - this.lastHurtTick > (master ? 120 : 200) && this.tickCount % (master ? 40 : 80) == 0
                && this.getHealth() < this.getMaxHealth() && !this.isHungry()) {
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
        if (this.soldierType != SoldierType.ARCHER && this.soldierType != SoldierType.ENGINEER) {
            // leises Klirren der Rüstung – schwerer bei den Gepanzerten
            float volume = switch (this.soldierType) {
                case SWORDSMAN, SPEARMAN, CROSSBOWMAN -> 0.15f;
                case MAN_AT_ARMS -> 0.32f;
                default -> 0.25f;
            };
            this.playSound(ModSounds.SOLDIER_ARMOR.get(), volume, (this.soldierType == SoldierType.MAN_AT_ARMS ? 1.05f : 1.25f)
                    + this.random.nextFloat() * 0.25f);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.isVillageGuard()) {
            // Dorfwachen stehen nicht zum Verkauf: sie sagen, wen sie bewachen
            if (!this.level().isClientSide() && !(stack.getItem() instanceof net.minecraft.world.item.SpawnEggItem)) {
                player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.soldier.village_guard", this.getDisplayName(), this.village)
                        .withStyle(ChatFormatting.GOLD));
                this.salute();
            }
            return InteractionResult.SUCCESS;
        }
        if (!this.hasOwner()) {
            return this.offerService(player, stack);
        }
        if (!this.isOwnedBy(player)) {
            return super.mobInteract(player, hand);
        }
        var food = stack.get(DataComponents.FOOD);
        boolean feedsHunger = this.isHungry() && io.github.sirschrolli.kingdomsatwar.economy.town.TownFood.nutrition(stack) > 0;
        if (food != null && (feedsHunger || this.getHealth() < this.getMaxHealth())) {
            if (this.level() instanceof ServerLevel level) {
                if (feedsHunger) {
                    // aus der Hand gegessen: satt für heute
                    this.eatRation(io.github.sirschrolli.kingdomsatwar.army.Provisions.day(level), false);
                }
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
        return Component.translatable("message.kingdomsatwar.soldier_status", Component.translatable("soldier.kingdomsatwar.rank." + this.getRank())
                        .append(" ").append(this.getDisplayName()).append(" · ")
                        .append(Component.translatable("soldier.kingdomsatwar.level", this.getCombatLevel())),
                Mth.ceil(this.getHealth()), Mth.ceil(this.getMaxHealth()),
                Component.translatable("message.kingdomsatwar.soldier_mode." + this.getMode()),
                this.isHungry() ? Component.translatable("gui.kingdomsatwar.recruitment.rations.hungry")
                        : Component.translatable("gui.kingdomsatwar.recruitment.rations", this.getRations()));
    }

    /**
     * Herrenlose Soldaten (z. B. Söldner) treten für ihren Sold in Münzen in den Dienst: Rechtsklick mit einer Münze in
     * der Hand bezahlt (mit Wechselgeld aus dem ganzen Inventar), sonst nennen sie ihren Preis.
     */
    private InteractionResult offerService(Player player, ItemStack stack) {
        int price = this.hirePrice();
        if (this.level() instanceof ServerLevel level && io.github.sirschrolli.kingdomsatwar.economy.Coin.of(stack.getItem()) != null) {
            if (io.github.sirschrolli.kingdomsatwar.army.Recruitment.armySize(level, player.getUUID())
                    >= io.github.sirschrolli.kingdomsatwar.army.Recruitment.armyLimit(player)) {
                player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.recruit.army_full").withStyle(ChatFormatting.RED));
                return InteractionResult.SUCCESS;
            }
            if (io.github.sirschrolli.kingdomsatwar.economy.Coins.canAfford(player, price) || player.hasInfiniteMaterials()) {
                if (!player.hasInfiniteMaterials()) {
                    io.github.sirschrolli.kingdomsatwar.economy.Coins.pay(player, price);
                }
                this.hireBy(player);
                if (player instanceof ServerPlayer serverPlayer) {
                    io.github.sirschrolli.kingdomsatwar.story.Story.onRecruited(serverPlayer);
                }
                return InteractionResult.SUCCESS;
            }
        }
        if (!this.level().isClientSide()) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.soldier_price", this.getDisplayName(),
                    io.github.sirschrolli.kingdomsatwar.economy.Coins.format(price)));
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * In welche Hand gehört dieser Gegenstand? Schwerter, Äxte, Speere und Streitkolben für Nahkämpfer, Bögen für
     * Bogenschützen, Armbrüste für Armbrustschützen, Schilde für Ritter und Schildträger.
     */
    private @Nullable EquipmentSlot weaponSlotFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (this.soldierType.crossbow()) {
            return stack.getItem() instanceof CrossbowItem ? EquipmentSlot.MAINHAND : null;
        }
        if (this.soldierType.ranged()) {
            return stack.getItem() instanceof BowItem ? EquipmentSlot.MAINHAND : null;
        }
        if (stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(ItemTags.SPEARS) || stack.is(Items.MACE)) {
            return EquipmentSlot.MAINHAND;
        }
        if (this.soldierType.shielded() && stack.has(DataComponents.BLOCKS_ATTACKS)) {
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
    // Lanzenreiter und ihr Schlachtross
    // =====================================================================================

    /** Ein Lanzenreiter ohne Ross (Söldner, die noch auf einen Herrn warten, ausgenommen): am Feldherrentisch gibt es ein neues. */
    public boolean needsSteed() {
        return this.soldierType.mounted() && !this.isPassenger() && !this.isMercenary();
    }

    /** Ein neues Schlachtross aus den Ställen: es steht bereit, sobald Platz zum Aufsitzen ist. */
    public void provideSteed() {
        this.steedWanted = true;
        this.steedId = null;
    }

    /** Das eigene Ross (wenn es lebt und geladen ist). */
    public @Nullable Warhorse steed(ServerLevel level) {
        return this.steedId != null && level.getEntity(this.steedId) instanceof Warhorse horse && horse.isAlive() ? horse : null;
    }

    /**
     * Lanzenreiter: Wer kein Ross hat und eines bekommen soll, sitzt auf, sobald Platz ist; wer abgeworfen wurde (oder
     * abgestiegen ist), geht zu seinem Ross zurück und sitzt wieder auf.
     */
    private void tendSteed(ServerLevel level) {
        if ((this.tickCount + this.getId()) % 10 != 0 || this.isPassenger() || this.isMercenary() || this.isVillageGuard()) {
            return;
        }
        Warhorse steed = this.steed(level);
        if (steed == null) {
            if (this.steedWanted && this.roomToRide(level, this.position())) {
                steed = Warhorse.saddleUp(level, this);
                if (steed != null) {
                    this.steedId = steed.getUUID();
                    this.steedWanted = false;
                    level.sendParticles(ParticleTypes.POOF, steed.getX(), steed.getY() + 0.8, steed.getZ(), 10, 0.5, 0.4, 0.5, 0.02);
                    this.mount(level, steed);
                }
            }
            return;
        }
        if (steed.isVehicle() || steed.distanceToSqr(this) > 24.0 * 24.0) {
            return;
        }
        if (this.distanceToSqr(steed) < 2.4 * 2.4) {
            if (this.roomToRide(level, steed.position())) {
                this.mount(level, steed);
            }
        } else if (this.getTarget() == null) {
            this.getNavigation().moveTo(steed, 1.2);
        }
    }

    /** Sitzt auf diesem Ross auf – es ist von nun an seines. */
    public boolean mountSteed(Warhorse steed) {
        this.steedId = steed.getUUID();
        this.steedWanted = false;
        return this.startRiding(steed, true, true);
    }

    private void mount(ServerLevel level, Warhorse steed) {
        this.getNavigation().stop();
        if (this.startRiding(steed, true, true)) {
            level.playSound(null, steed.getX(), steed.getY(), steed.getZ(), SoundEvents.HORSE_SADDLE.value(), SoundSource.NEUTRAL, 0.8f, 1.0f);
        }
    }

    /** Ist hier Platz für Ross und Reiter (gut drei Blöcke hoch)? */
    private boolean roomToRide(ServerLevel level, Vec3 at) {
        AABB horse = ModEntities.WARHORSE.get().getDimensions().makeBoundingBox(at);
        AABB rider = new AABB(at.x - 0.3, at.y + 1.4, at.z - 0.3, at.x + 0.3, at.y + 3.3, at.z + 0.3);
        return level.noCollision(horse) && level.noCollision(rider);
    }

    // =====================================================================================
    // Armbrust
    // =====================================================================================

    /** Der gespannte Bolzen fliegt: schnell und flach (der Abfall bis zum Ziel ist eingerechnet), trifft härter als ein Pfeil. */
    private void shootBolt(ServerLevel level, LivingEntity target, ItemStack crossbow) {
        crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        ItemStack bolt = new ItemStack(Items.ARROW);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, bolt, 1.25f, crossbow);   // der schwere Bolzen
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        arrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.4) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        double flight = distance / BOLT_SPEED;
        Projectile.spawnProjectileUsingShoot(arrow, level, bolt, dx, dy + 0.025 * flight * flight, dz, BOLT_SPEED, 1.2f);
        this.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0f, 1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
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
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            io.github.sirschrolli.kingdomsatwar.army.ArmyRoster.discharge(level.getServer(), this.getUUID());
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason.shouldDestroy() && this.level() instanceof ServerLevel level) {
            io.github.sirschrolli.kingdomsatwar.army.ArmyRoster.discharge(level.getServer(), this.getUUID());
        }
        super.remove(reason);
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
            // wer eine Belagerungswaffe bedient oder im Turm, im Wagen mitfährt, läuft niemandem hinterher
            if (SoldierEntity.this.getMode() != MODE_FOLLOW || SiegeCrew.isCrew(SoldierEntity.this)) {
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
            SoldierEntity.this.getNavigation().stop();
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
                SoldierEntity.this.getNavigation().moveTo(this.owner, 1.1);
            }
        }

        /** Springt zum Besitzer – ein Lanzenreiter samt Ross (wer eine Belagerungswaffe bedient, bleibt, wo er ist). */
        private void teleportNear(LivingEntity owner) {
            BlockPos base = owner.blockPosition();
            SoldierEntity self = SoldierEntity.this;
            Entity body = self.getVehicle() instanceof Warhorse horse ? horse : self;
            if (body == self && self.isPassenger()) {
                return;
            }
            for (int i = 0; i < 10; i++) {
                BlockPos pos = base.offset(self.random.nextInt(5) - 2, 0, self.random.nextInt(5) - 2);
                if (self.level().getBlockState(pos.below()).isFaceSturdy(self.level(), pos.below(), net.minecraft.core.Direction.UP)
                        && self.level().noCollision(body, body.getBoundingBox().move(Vec3.atBottomCenterOf(pos).subtract(body.position())))) {
                    body.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, body.getYRot(), body.getXRot());
                    self.getNavigation().stop();
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
                    && !SiegeCrew.isCrew(SoldierEntity.this) && !SoldierEntity.this.blockPosition().closerThan(post, this.limit());
        }

        @Override
        public boolean canContinueToUse() {
            return SoldierEntity.this.getTarget() == null && SoldierEntity.this.post != null && !SoldierEntity.this.getNavigation().isDone();
        }

        @Override
        public void start() {
            BlockPos post = SoldierEntity.this.post;
            if (post != null) {
                SoldierEntity.this.getNavigation().moveTo(post.getX() + 0.5, post.getY(), post.getZ() + 0.5, 1.0);
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
                    && !SiegeCrew.isCrew(SoldierEntity.this) && SoldierEntity.this.random.nextInt(this.reducedTickDelay(80)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return !SoldierEntity.this.getNavigation().isDone() && SoldierEntity.this.getTarget() == null;
        }

        @Override
        public void start() {
            BlockPos post = SoldierEntity.this.post;
            if (post == null) {
                return;
            }
            Vec3 target = DefaultRandomPos.getPosTowards(SoldierEntity.this, 8, 3, Vec3.atBottomCenterOf(post), Math.PI / 2);
            if (target != null) {
                SoldierEntity.this.getNavigation().moveTo(target.x, target.y, target.z, 0.7);
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

    /**
     * Armbrustschütze: spannt die Armbrust (das dauert), legt an und schießt – nie, wenn ein Verbündeter in der Schusslinie
     * steht. Ist das Ziel zu weit weg oder nicht zu sehen, rückt er näher.
     */
    private final class CrossbowGoal extends Goal {
        private static final double RANGE = 24.0;
        private int seen;
        private int aim;
        private int repath;

        CrossbowGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        private ItemStack crossbow() {
            ItemStack held = SoldierEntity.this.getMainHandItem();
            return held.getItem() instanceof CrossbowItem ? held : ItemStack.EMPTY;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = SoldierEntity.this.getTarget();
            return target != null && target.isAlive() && !this.crossbow().isEmpty();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void start() {
            SoldierEntity.this.setAggressive(true);
            this.seen = 0;
            this.aim = 0;
            this.repath = 0;
        }

        @Override
        public void stop() {
            SoldierEntity self = SoldierEntity.this;
            self.setAggressive(false);
            if (self.isUsingItem()) {
                self.stopUsingItem();
            }
            self.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            SoldierEntity self = SoldierEntity.this;
            LivingEntity target = self.getTarget();
            ItemStack crossbow = this.crossbow();
            if (target == null || crossbow.isEmpty() || !(self.level() instanceof ServerLevel level)) {
                return;
            }
            boolean visible = self.getSensing().hasLineOfSight(target);
            this.seen = visible ? Math.max(0, this.seen) + 1 : Math.min(0, this.seen) - 1;
            double distance = self.distanceToSqr(target);
            boolean approach = distance > RANGE * RANGE * 0.75 || this.seen < -20;
            if (approach) {
                if (--this.repath <= 0) {
                    this.repath = this.adjustedTickDelay(20);
                    self.getNavigation().moveTo(target, self.isUsingItem() ? 0.6 : 1.0);
                }
            } else {
                self.getNavigation().stop();
            }
            self.getLookControl().setLookAt(target, 30.0f, 30.0f);
            if (CrossbowItem.isCharged(crossbow)) {
                if (visible && this.seen > 5 && --this.aim <= 0 && distance < RANGE * RANGE && !self.allyInLineOfFire(target)) {
                    self.shootBolt(level, target, crossbow);
                    this.aim = 0;
                }
            } else if (self.isUsingItem()) {
                if (self.getTicksUsingItem() >= CrossbowItem.getChargeDuration(crossbow, self)) {
                    crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(new ItemStackTemplate(Items.ARROW)));
                    self.stopUsingItem();
                    level.playSound(null, self.getX(), self.getY(), self.getZ(), SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.NEUTRAL,
                            1.0f, 1.0f / (self.random.nextFloat() * 0.5f + 1.0f) + 0.2f);
                    this.aim = 10 + self.random.nextInt(14);   // anlegen und zielen
                }
            } else {
                self.startUsingItem(InteractionHand.MAIN_HAND);
            }
        }
    }

    /**
     * Lanzenangriff (nach dem Speerangriff der Vanilla-Zombiereiter): Der Lanzenreiter reitet auf Abstand, senkt die
     * Lanze, galoppiert durch den Gegner hindurch und wendet in sicherem Abstand für den nächsten Anritt. Den Schaden macht
     * der Stoß der Lanze selbst – je schneller das Ross, desto härter.
     */
    private final class LanceChargeGoal extends Goal {
        private static final int FLEE_TIME = 50;
        private int engage = -1;
        private int fleeing = -1;
        private @Nullable Vec3 away;
        private boolean done;
        /** Neue Wege nicht jeden Tick suchen (das kostet): erst wenn diese Zeit um ist oder der alte Weg zu Ende. */
        private int repath;

        LanceChargeGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        private boolean able() {
            SoldierEntity self = SoldierEntity.this;
            LivingEntity target = self.getTarget();
            return target != null && target.isAlive() && self.getVehicle() instanceof Warhorse
                    && self.getMainHandItem().has(DataComponents.KINETIC_WEAPON);
        }

        @Override
        public boolean canUse() {
            return this.able() && !SoldierEntity.this.isUsingItem();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.done && this.able();
        }

        @Override
        public void start() {
            SoldierEntity.this.setAggressive(true);
            this.engage = -1;
            this.fleeing = -1;
            this.away = null;
            this.done = false;
            this.repath = 0;
        }

        @Override
        public void stop() {
            SoldierEntity self = SoldierEntity.this;
            self.getNavigation().stop();
            self.setAggressive(false);
            self.stopUsingItem();
            this.away = null;
        }

        /** Dem Gegner nach – der Weg wird alle paar Ticks neu gesucht (oder sofort, wenn der alte zu Ende ist). */
        private void chase(LivingEntity target, float speed) {
            if (--this.repath <= 0 || SoldierEntity.this.getNavigation().isDone()) {
                this.repath = 6;
                SoldierEntity.this.getNavigation().moveTo(target, speed);
            }
        }

        private int lanceTime() {
            var kinetic = SoldierEntity.this.getMainHandItem().get(DataComponents.KINETIC_WEAPON);
            return this.reducedTickDelay(kinetic == null ? 0 : kinetic.computeDamageUseDuration());
        }

        @Override
        public void tick() {
            SoldierEntity self = SoldierEntity.this;
            LivingEntity target = self.getTarget();
            if (target == null) {
                return;
            }
            double distance = self.distanceToSqr(target);
            float speed = self.getRootVehicle() instanceof Mob horse ? horse.chargeSpeedModifier() : 1.0f;
            self.lookAt(target, 30.0f, 30.0f);
            self.getLookControl().setLookAt(target, 30.0f, 30.0f);
            if (this.engage < 0) {
                if (distance > 11.0 * 11.0) {
                    this.chase(target, speed);
                    return;
                }
                this.engage = this.lanceTime();
                self.startUsingItem(InteractionHand.MAIN_HAND);
            }
            if (this.engage > 0 && --this.engage == 0) {
                self.stopUsingItem();
                double d = Math.sqrt(distance);
                this.away = LandRandomPos.getPosAway(self, Math.max(0.0, 11.0 - d), Math.max(1.0, 13.0 - d), 7, target.position());
                this.fleeing = 1;
                this.repath = 0;
            }
            if (this.fleeing > 0 && ++this.fleeing > FLEE_TIME) {
                this.done = true;
                return;
            }
            if (this.away != null) {
                if (--this.repath <= 0 || self.getNavigation().isDone()) {
                    this.repath = 10;
                    self.getNavigation().moveTo(this.away.x, this.away.y, this.away.z, speed);
                }
                if (self.getNavigation().isDone()) {
                    if (this.fleeing > 0) {
                        this.done = true;
                        return;
                    }
                    this.away = null;
                }
            } else {
                this.chase(target, speed * 1.1f);
                if (distance < 2.5 * 2.5 || self.getNavigation().isDone()) {
                    double d = Math.sqrt(distance);
                    this.away = LandRandomPos.getPosAway(self, 8.0 - d, 9.0 - d, 7, target.position());
                    this.repath = 0;
                }
            }
        }
    }

    /**
     * Belagerungsknecht: Ist eine eigene Belagerungswaffe in der Nähe beschädigt (und kein Feind da), geht er hin und
     * bessert sie mit Hammer und Holz aus – drei Hundertstel alle zwei Sekunden.
     */
    private final class RepairSiegeGoal extends Goal {
        private static final double SEARCH = 16.0;
        private @Nullable SiegeWeaponEntity weapon;
        private int work;

        RepairSiegeGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            SoldierEntity self = SoldierEntity.this;
            // nur ab und zu umsehen (die Ziele werden jeden zweiten Tick geprüft – darum ein Fenster von zwei Ticks)
            if (self.getTarget() != null || self.isPassenger() || (self.tickCount + self.getId()) % 20 > 1) {
                return false;
            }
            this.weapon = this.damaged();
            return this.weapon != null;
        }

        private @Nullable SiegeWeaponEntity damaged() {
            SoldierEntity self = SoldierEntity.this;
            UUID owner = self.getOwnerId();
            if (owner == null) {
                return null;
            }
            SiegeWeaponEntity best = null;
            double bestDistance = SEARCH * SEARCH;
            for (var w : self.level().getEntitiesOfClass(SiegeWeaponEntity.class,
                    self.getBoundingBox().inflate(SEARCH), w -> w.isAlive() && w.healthFraction() < 0.995f && owner.equals(w.owner()))) {
                // wer Stellung hält, entfernt sich nicht weit von seinem Posten
                if (self.post != null && self.getMode() != MODE_FOLLOW && !w.blockPosition().closerThan(self.post, 14.0)) {
                    continue;
                }
                double d = self.distanceToSqr(w);
                if (d < bestDistance) {
                    best = w;
                    bestDistance = d;
                }
            }
            return best;
        }

        @Override
        public boolean canContinueToUse() {
            SoldierEntity self = SoldierEntity.this;
            return this.weapon != null && this.weapon.isAlive() && this.weapon.healthFraction() < 0.995f && self.getTarget() == null
                    && !self.isPassenger() && self.distanceToSqr(this.weapon) < 24.0 * 24.0;
        }

        @Override
        public void start() {
            this.work = 0;
            if (this.weapon != null) {
                SoldierEntity.this.getNavigation().moveTo(this.weapon, 1.0);
            }
        }

        @Override
        public void stop() {
            if (this.weapon != null && this.weapon.healthFraction() >= 0.995f) {
                SoldierEntity.this.salute();
            }
            this.weapon = null;
            SoldierEntity.this.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            SoldierEntity self = SoldierEntity.this;
            var weapon = this.weapon;
            if (weapon == null || !(self.level() instanceof ServerLevel level)) {
                return;
            }
            self.getLookControl().setLookAt(weapon.getX(), weapon.getY() + 0.8, weapon.getZ());
            double reach = weapon.getBbWidth() * 0.5 + 1.6;
            if (self.distanceToSqr(weapon.getX(), self.getY(), weapon.getZ()) > reach * reach) {
                if (++this.work % 20 == 0 || self.getNavigation().isDone()) {
                    self.getNavigation().moveTo(weapon, 1.0);
                }
                return;
            }
            self.getNavigation().stop();
            if (++this.work % 10 != 0) {
                return;
            }
            self.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT);
            Vec3 at = weapon.position().add(self.position().subtract(weapon.position()).normalize().scale(weapon.getBbWidth() * 0.4))
                    .add(0.0, 0.7, 0.0);
            level.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,
                    net.minecraft.world.level.block.Blocks.OAK_PLANKS.defaultBlockState()), at.x, at.y, at.z, 6, 0.2, 0.2, 0.2, 0.05);
            level.playSound(null, at.x, at.y, at.z, SoundEvents.WOOD_HIT, SoundSource.NEUTRAL, 0.9f, 0.9f + self.random.nextFloat() * 0.3f);
            if (this.work % 40 == 0) {
                weapon.setHealthFraction(weapon.healthFraction() + 0.03f);
            }
        }
    }

    /** Alle Soldaten eines Spielers in Reichweite (für Befehle). */
    public static List<SoldierEntity> soldiersOf(Player player, double range) {
        return player.level().getEntitiesOfClass(SoldierEntity.class, player.getBoundingBox().inflate(range), s -> s.isOwnedBy(player));
    }
}
