package io.github.sirschrolli.kingdomsatwar.economy.citizen;

import io.github.sirschrolli.kingdomsatwar.economy.town.Profession;
import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownManager;
import io.github.sirschrolli.kingdomsatwar.economy.town.Towns;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Ein Bürger der Siedlung. Er wird am Stadtbanner mit Münzen angeheuert, wohnt in einem Bett der Siedlung, geht tagsüber
 * seinem Beruf nach (bauen, ernten, Holz fällen, Erz hauen, handeln) und schläft nachts. Händler, Schmied, Wirt und
 * Kartograf handeln mit Spielern; die Handelsgrenze läuft in Echtzeit ab.
 */
public class CitizenEntity extends PathfinderMob {
    private static final EntityDataAccessor<Byte> DATA_PROFESSION = SynchedEntityData.defineId(CitizenEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(CitizenEntity.class, EntityDataSerializers.BYTE);
    /** Gerade bei der Arbeit (für die Haltung im Bild). */
    private static final EntityDataAccessor<Boolean> DATA_WORKING = SynchedEntityData.defineId(CitizenEntity.class, EntityDataSerializers.BOOLEAN);

    /** Waren, die der Bürger bei sich trägt, bis er sie in die Truhe seiner Werkstatt bringt. */
    private final SimpleContainer carried = new SimpleContainer(12);
    private @Nullable UUID townId;
    private @Nullable BlockPos bed;
    private @Nullable BlockPos work;
    /** Handel: wie oft seit der letzten Pause gehandelt wurde und bis wann (Echtzeit, ms) der Laden zu ist. */
    private int tradesUsed;
    private long closedUntil;
    /** Spieler, der gerade das Handelsfenster offen hat (der Bürger bleibt dann stehen und schaut ihn an). */
    private @Nullable Player customer;
    private int customerTicks;

    public CitizenEntity(EntityType<? extends CitizenEntity> type, Level level) {
        super(type, level);
        if (this.navigation instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
        }
        this.setPersistenceRequired();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.setDropChance(slot, 0.0f);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.3));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Monster.class, 10.0f, 1.0, 1.3));
        this.goalSelector.addGoal(2, new CitizenGoals.Sleep(this));
        this.goalSelector.addGoal(3, new CitizenGoals.Serve(this));
        this.goalSelector.addGoal(4, new CitizenGoals.Work(this));
        this.goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(6, new CitizenGoals.StayInTown(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    // =====================================================================================
    // Daten
    // =====================================================================================

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PROFESSION, (byte) 0);
        builder.define(DATA_VARIANT, (byte) 0);
        builder.define(DATA_WORKING, false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Profession", this.getProfession().id());
        output.putInt("Variant", this.getVariant());
        if (this.townId != null) {
            output.store("Town", UUIDUtil.CODEC, this.townId);
        }
        if (this.bed != null) {
            output.store("Bed", BlockPos.CODEC, this.bed);
        }
        if (this.work != null) {
            output.store("Work", BlockPos.CODEC, this.work);
        }
        output.putInt("TradesUsed", this.tradesUsed);
        output.putLong("ClosedUntil", this.closedUntil);
        this.carried.storeAsItemList(output.list("Carried", ItemStack.OPTIONAL_CODEC));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        Profession profession = Profession.BUILDER;
        String id = input.getStringOr("Profession", "builder");
        for (Profession p : Profession.values()) {
            if (p.id().equals(id)) {
                profession = p;
            }
        }
        this.setProfession(profession);
        this.setVariant(input.getIntOr("Variant", 0));
        this.townId = input.read("Town", UUIDUtil.CODEC).orElse(null);
        this.bed = input.read("Bed", BlockPos.CODEC).orElse(null);
        this.work = input.read("Work", BlockPos.CODEC).orElse(null);
        this.tradesUsed = input.getIntOr("TradesUsed", 0);
        this.closedUntil = input.getLongOr("ClosedUntil", 0L);
        this.carried.fromItemList(input.listOrEmpty("Carried", ItemStack.OPTIONAL_CODEC));
    }

    public Profession getProfession() {
        return Profession.values()[Mth.clamp(this.entityData.get(DATA_PROFESSION), 0, Profession.values().length - 1)];
    }

    public void setProfession(Profession profession) {
        this.entityData.set(DATA_PROFESSION, (byte) profession.ordinal());
    }

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, (byte) Mth.clamp(variant, 0, 7));
    }

    /** Die letzte Gesichtsvariante jedes Berufs ist eine Frau. */
    public boolean isFeminine() {
        return this.getVariant() == this.getProfession().variants() - 1;
    }

    public boolean isWorking() {
        return this.entityData.get(DATA_WORKING);
    }

    public void setWorking(boolean working) {
        this.entityData.set(DATA_WORKING, working);
    }

    public @Nullable UUID getTownId() {
        return this.townId;
    }

    public @Nullable BlockPos getBed() {
        return this.bed;
    }

    public @Nullable BlockPos getWork() {
        return this.work;
    }

    /** Wird von der Siedlung gesetzt, wenn der Bürger angeheuert wird oder ein Bett bzw. eine Werkstatt bekommt. */
    public void assign(UUID town, @Nullable BlockPos bed, @Nullable BlockPos work) {
        this.townId = town;
        this.bed = bed;
        this.work = work;
    }

    public SimpleContainer carried() {
        return this.carried;
    }

    /** Schwingt das Werkzeug (für alle Zuschauer sichtbar). */
    public void swingArm() {
        this.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT);
    }

    /** Die Siedlung des Bürgers (nur auf dem Server). */
    public @Nullable Town town() {
        if (this.townId == null || !(this.level() instanceof ServerLevel level)) {
            return null;
        }
        return Towns.of(level.getServer()).byId(this.townId);
    }

    // =====================================================================================
    // Handel
    // =====================================================================================

    public int tradesUsed() {
        return this.tradesUsed;
    }

    public long closedUntil() {
        return this.closedUntil;
    }

    /** Ist der Laden gerade geschlossen (Handelsgrenze erreicht, Pause läuft in Echtzeit)? */
    public boolean isClosed() {
        if (this.closedUntil > 0 && System.currentTimeMillis() >= this.closedUntil) {
            this.closedUntil = 0;
            this.tradesUsed = 0;
        }
        return this.closedUntil > 0;
    }

    public void recordTrade(int limit, long pauseMillis) {
        this.tradesUsed++;
        if (this.tradesUsed >= limit) {
            this.closedUntil = System.currentTimeMillis() + pauseMillis;
        }
    }

    public @Nullable Player getCustomer() {
        return this.customer;
    }

    public void setCustomer(@Nullable Player customer) {
        this.customer = customer;
        this.customerTicks = customer == null ? 0 : 400;
    }

    // =====================================================================================
    // Verhalten
    // =====================================================================================

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            if (this.customer != null && (--this.customerTicks <= 0 || !this.customer.isAlive() || this.customer.distanceToSqr(this) > 64.0)) {
                this.customer = null;
            }
            // Werkzeug in der Hand: bei der Arbeit das Werkzeug des Berufs, sonst leer (Händler immer leer)
            ItemStack want = this.isWorking() ? this.getProfession().workItem() : ItemStack.EMPTY;
            if (!ItemStack.isSameItem(this.getMainHandItem(), want)) {
                this.setItemSlot(EquipmentSlot.MAINHAND, want.copy());
            }
            // eingemauert (z. B. von einer Baustelle)? Dann auf den nächsten freien Platz treten
            if (this.tickCount % 10 == 0 && this.isInWall() && this.level() instanceof ServerLevel level) {
                BlockPos spot = Jobs.standingSpot(level, this.blockPosition());
                if (spot == null) {
                    spot = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            this.blockPosition());
                }
                this.teleportTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5);
            }
        }
    }

    /** Wer in einer Wand steckt, erstickt nicht (er tritt im nächsten Augenblick hinaus). */
    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL) || super.isInvulnerableTo(level, source);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.NAME_TAG)) {
            return super.mobInteract(player, hand);
        }
        if (this.isSleeping()) {
            if (!this.level().isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.citizen.sleeping", this.getName()).withStyle(ChatFormatting.GRAY));
            }
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (this.getProfession().trader()) {
                Trading.open(serverPlayer, this);
            } else {
                serverPlayer.sendOverlayMessage(this.statusMessage());
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** „Hans, Bauer von Lindenau – bei der Arbeit“. */
    public Component statusMessage() {
        Town town = this.town();
        Component activity = Component.translatable("citizen.kingdomsatwar.activity." + (this.isSleeping() ? "sleeping"
                : this.isWorking() ? "working" : "idle"));
        return Component.translatable("message.kingdomsatwar.citizen.status", this.getName(),
                Component.translatable(this.getProfession().translationKey()),
                town == null ? Component.literal("–") : Component.literal(town.name()), activity).withStyle(ChatFormatting.GOLD);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (this.isSleeping()) {
            this.stopSleeping();
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            TownManager.onCitizenDied(level, this, source);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason.shouldDestroy() && !this.isDeadOrDying() && this.level() instanceof ServerLevel level) {
            TownManager.onCitizenDied(level, this, null);
        }
        super.remove(reason);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return this.isFeminine() ? 1.35f : 0.95f;
    }

}
