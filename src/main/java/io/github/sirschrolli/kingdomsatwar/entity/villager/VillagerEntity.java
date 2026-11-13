package io.github.sirschrolli.kingdomsatwar.entity.villager;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.Reputation;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.CoinTrader;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownNames;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Ein Bewohner eines Dorfs. Er hat einen Beruf, ein Bett und einen Arbeitsplatz und lebt nach einem Tagesablauf: am
 * Morgen steht er auf, tagsüber arbeitet er an seiner Werkbank (mit Pausen), am Nachmittag trifft sich das Dorf auf dem
 * Platz, abends geht jeder heim und schläft in seinem Bett. Bei Regen stellt er sich unter, vor Monstern flieht er, und
 * wer ihn angreift, bekommt es mit den Wachen des Dorfs zu tun.
 *
 * <p>Mit Rechtsklick handelt man gegen Münzen (siehe {@link VillageTrades}); geduckt erzählt er, was man sich im Dorf
 * erzählt (siehe {@link VillageRumors}).
 */
public class VillagerEntity extends PathfinderMob implements CoinTrader {
    private static final EntityDataAccessor<Byte> DATA_PROFESSION = SynchedEntityData.defineId(VillagerEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(VillagerEntity.class, EntityDataSerializers.BYTE);
    /** Gerade bei der Arbeit (für Werkzeug und Haltung). */
    private static final EntityDataAccessor<Boolean> DATA_WORKING = SynchedEntityData.defineId(VillagerEntity.class, EntityDataSerializers.BOOLEAN);

    /** Wie weit sich ein Bewohner von der Dorfmitte entfernt, bevor er umkehrt. */
    public static final int VILLAGE_RADIUS = 48;
    /** Ansehen, das ein Spieler verliert, der einen Dorfbewohner tötet. */
    public static final int MURDER_PENALTY = 40;

    private String villageName = "";
    private @Nullable BlockPos villageCenter;
    private @Nullable BlockPos bed;
    private @Nullable BlockPos work;
    /** Vor dem eigenen Haus an der Straße: von dort aus geht es hinein (Zwischenziel auf weiten Wegen). */
    private @Nullable BlockPos frontDoor;
    /** Die Grundfläche des eigenen Hauses (wer drinnen ist, geht nicht erst zur Haustür). */
    private @Nullable BoundingBox house;
    /** Zufall für die wechselnden Waren; mit jeder Ladenpause gibt es neue ({@link #stock}). */
    private long tradeSeed;
    private int stock;
    private int tradesUsed;
    private long closedUntil;
    private boolean initialized;
    /** Spieler, der gerade das Handelsfenster offen hat (der Bewohner bleibt dann stehen und schaut ihn an). */
    private @Nullable Player customer;
    private int customerTicks;
    /** Bis zu welchem Tick der Bewohner nach einer Arbeitsschicht Pause macht. */
    int restUntil;
    /** Nur für Tests: eine feste Tageszeit statt der Uhr der Welt (−1: die Uhr der Welt). */
    long timeOverride = -1;
    /** Das nächste andere Dorf (für das, was man sich erzählt; nur einmal je Sitzung gesucht). */
    io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageLocator.@Nullable Found neighbour;
    boolean neighbourSearched;

    public VillagerEntity(EntityType<? extends VillagerEntity> type, Level level) {
        super(type, level);
        if (this.navigation instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
        }
        // Wege durch Häuser mit Treppen und Obergeschoss brauchen mehr Suche als der Weg über eine Wiese
        this.navigation.setMaxVisitedNodesMultiplier(3.0f);
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
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.35));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Mob.class, target -> target instanceof Enemy, 10.0f, 0.8, 1.25,
                EntitySelector.NO_CREATIVE_OR_SPECTATOR));
        this.goalSelector.addGoal(2, new VillagerGoals.Sleep(this));
        this.goalSelector.addGoal(3, new VillagerGoals.Serve(this));
        this.goalSelector.addGoal(4, new VillagerGoals.Shelter(this));
        this.goalSelector.addGoal(5, new VillagerGoals.Work(this));
        this.goalSelector.addGoal(6, new VillagerGoals.Gather(this));
        this.goalSelector.addGoal(7, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(8, new VillagerGoals.StayInVillage(this));
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 0.5, 0.004f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, VillagerEntity.class, 6.0f, 0.04f));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
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
        output.putString("Village", this.villageName);
        if (this.villageCenter != null) {
            output.store("VillageCenter", BlockPos.CODEC, this.villageCenter);
        }
        if (this.bed != null) {
            output.store("Bed", BlockPos.CODEC, this.bed);
        }
        if (this.work != null) {
            output.store("Work", BlockPos.CODEC, this.work);
        }
        if (this.frontDoor != null) {
            output.store("FrontDoor", BlockPos.CODEC, this.frontDoor);
        }
        if (this.house != null) {
            output.store("House", BoundingBox.CODEC, this.house);
        }
        output.putLong("TradeSeed", this.tradeSeed);
        output.putInt("Stock", this.stock);
        output.putInt("TradesUsed", this.tradesUsed);
        output.putLong("ClosedUntil", this.closedUntil);
        output.putBoolean("Initialized", this.initialized);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setProfession(VillageProfession.byId(input.getStringOr("Profession", "peasant")));
        this.setVariant(input.getIntOr("Variant", 0));
        this.villageName = input.getStringOr("Village", "");
        this.villageCenter = input.read("VillageCenter", BlockPos.CODEC).orElse(null);
        this.bed = input.read("Bed", BlockPos.CODEC).orElse(null);
        this.work = input.read("Work", BlockPos.CODEC).orElse(null);
        this.frontDoor = input.read("FrontDoor", BlockPos.CODEC).orElse(null);
        this.house = input.read("House", BoundingBox.CODEC).orElse(null);
        this.tradeSeed = input.getLongOr("TradeSeed", 0L);
        this.stock = input.getIntOr("Stock", 0);
        this.tradesUsed = input.getIntOr("TradesUsed", 0);
        this.closedUntil = input.getLongOr("ClosedUntil", 0L);
        this.initialized = input.getBooleanOr("Initialized", true);
        if (this.villageCenter != null) {
            this.setHomeTo(this.villageCenter, VILLAGE_RADIUS - 8);
        }
    }

    /**
     * Richtet einen neuen Bewohner ein: Beruf, Gesicht, Name, Dorf, Bett und Arbeitsplatz (beim Erzeugen der Welt, siehe
     * {@code worldgen/village/VillageResidents}).
     */
    public void setup(VillageProfession profession, int variant, String name, @Nullable BlockPos center, String village, @Nullable BlockPos bed,
            @Nullable BlockPos work, long seed) {
        this.setProfession(profession);
        this.setVariant(variant);
        this.setCustomName(Component.literal(name));
        this.villageName = village;
        this.villageCenter = center == null ? null : center.immutable();
        this.bed = bed == null ? null : bed.immutable();
        this.work = work == null ? null : work.immutable();
        this.tradeSeed = seed;
        this.initialized = true;
        if (center != null) {
            this.setHomeTo(center, VILLAGE_RADIUS - 8);
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData data) {
        if (!this.initialized) {
            // aus dem Spawn-Ei oder per Befehl: ein zufälliger Bewohner ohne Dorf, der hier zu Hause ist
            VillageProfession[] professions = VillageProfession.values();
            VillageProfession profession = professions[this.random.nextInt(professions.length)];
            int variant = this.random.nextInt(profession.variants());
            boolean feminine = variant == profession.variants() - 1;
            this.setup(profession, variant, TownNames.citizen(this.random, feminine), this.blockPosition(), "", null, null,
                    this.random.nextLong());
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    public VillageProfession getProfession() {
        VillageProfession[] all = VillageProfession.values();
        return all[Mth.clamp(this.entityData.get(DATA_PROFESSION), 0, all.length - 1)];
    }

    public void setProfession(VillageProfession profession) {
        this.entityData.set(DATA_PROFESSION, (byte) profession.ordinal());
    }

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, (byte) Mth.clamp(variant, 0, 7));
    }

    /** Das letzte Gesicht jedes Berufs ist eine Frau. */
    public boolean isFeminine() {
        return this.getVariant() == this.getProfession().variants() - 1;
    }

    public boolean isWorking() {
        return this.entityData.get(DATA_WORKING);
    }

    public void setWorking(boolean working) {
        this.entityData.set(DATA_WORKING, working);
    }

    /** Nur für Tests: der Bewohner lebt nach dieser Tageszeit (0–23999) statt nach der Uhr der Welt; −1 hebt das auf. */
    public void setTimeOverride(long time) {
        this.timeOverride = time;
    }

    public String getVillageName() {
        return this.villageName;
    }

    public @Nullable BlockPos getVillageCenter() {
        return this.villageCenter;
    }

    public @Nullable BlockPos getBed() {
        return this.bed;
    }

    public @Nullable BlockPos getWork() {
        return this.work;
    }

    /** Nur zur Fehlersuche: welche Ziele gerade laufen. */
    public String runningGoals() {
        return this.goalSelector.getAvailableGoals().stream().filter(net.minecraft.world.entity.ai.goal.WrappedGoal::isRunning)
                .map(g -> g.getGoal().getClass().getSimpleName()).toList().toString();
    }

    public @Nullable BlockPos getFrontDoor() {
        return this.frontDoor;
    }

    public void setFrontDoor(@Nullable BlockPos door) {
        this.frontDoor = door == null ? null : door.immutable();
    }

    /** Haustür und Grundfläche des Hauses, in dem der Bewohner lebt. */
    public void setHouse(@Nullable BlockPos door, @Nullable BoundingBox area) {
        this.setFrontDoor(door);
        this.house = area;
    }

    /** Ist der Bewohner gerade in seinem Haus (oder auf dessen Grundstück)? */
    public boolean atHome() {
        return this.house != null && this.house.isInside(this.blockPosition());
    }

    /** Dorfbewohner schieben einander nicht (sonst blockieren sich zwei in einer Tür oder auf der Treppe). */
    @Override
    protected void doPush(Entity entity) {
        if (!(entity instanceof VillagerEntity)) {
            super.doPush(entity);
        }
    }

    /** Schwingt das Werkzeug (für alle Zuschauer sichtbar). */
    public void swingArm() {
        this.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT);
    }

    // =====================================================================================
    // Handel
    // =====================================================================================

    @Override
    public boolean canTrade() {
        return this.getProfession().trader();
    }

    @Override
    public List<Trading.Offer> baseOffers() {
        return VillageTrades.offers(this.getProfession(), this.registryAccess(), this.tradeSeed * 31L + this.stock);
    }

    @Override
    public int tradesUsed() {
        return this.tradesUsed;
    }

    @Override
    public long closedUntil() {
        return this.closedUntil;
    }

    /** Nach der Pause öffnet der Laden wieder – mit neuen wechselnden Waren. */
    @Override
    public boolean isClosed() {
        if (this.closedUntil > 0 && System.currentTimeMillis() >= this.closedUntil) {
            this.closedUntil = 0;
            this.tradesUsed = 0;
            this.stock++;
        }
        return this.closedUntil > 0;
    }

    @Override
    public void recordTrade(int limit, long pauseMillis) {
        this.tradesUsed++;
        if (this.tradesUsed >= limit) {
            this.closedUntil = System.currentTimeMillis() + pauseMillis;
        }
    }

    @Override
    public @Nullable Player getCustomer() {
        return this.customer;
    }

    @Override
    public void setCustomer(@Nullable Player customer) {
        this.customer = customer;
        this.customerTicks = customer == null ? 0 : 400;
    }

    @Override
    public String skinTexture() {
        return texture(this.getProfession(), this.getVariant()).toString();
    }

    /** Die Haut eines Bewohners dieses Berufs mit diesem Gesicht. */
    public static net.minecraft.resources.Identifier texture(VillageProfession profession, int variant) {
        int v = Mth.clamp(variant, 0, profession.variants() - 1);
        return KingdomsAtWar.id("textures/entity/villager/" + profession.id() + "_" + (v + 1) + ".png");
    }

    @Override
    public String professionKey() {
        return this.getProfession().translationKey() + (this.isFeminine() ? ".female" : "");
    }

    @Override
    public Component traderName() {
        return this.getName();
    }

    // =====================================================================================
    // Verhalten
    // =====================================================================================

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.customer != null && (--this.customerTicks <= 0 || !this.customer.isAlive() || this.customer.distanceToSqr(this) > 64.0)) {
            this.customer = null;
        }
        // bei der Arbeit das Werkzeug des Berufs in der Hand, sonst nichts
        ItemStack want = this.isWorking() ? this.getProfession().workItem() : ItemStack.EMPTY;
        if (!ItemStack.isSameItem(this.getMainHandItem(), want)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, want.copy());
        }
        // wer schläft, bleibt im Bett liegen (auch wenn ihn ein Schaf oder ein anderer Bewohner anstößt)
        if (this.isSleeping()) {
            this.getSleepingPos().ifPresent(bed -> {
                double dx = this.getX() - (bed.getX() + 0.5);
                double dz = this.getZ() - (bed.getZ() + 0.5);
                if (dx * dx + dz * dz > 0.04) {
                    this.setPos(bed.getX() + 0.5, this.getY(), bed.getZ() + 0.5);
                    this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                }
            });
            return;
        }
        // eingeklemmt (z. B. von einem Block, den jemand gesetzt hat)? Dann auf den nächsten freien Platz treten
        if (this.tickCount % 20 == 0 && this.isInWall()) {
            BlockPos spot = VillagerGoals.freeSpotNear(this.level(), this.blockPosition(), 3);
            if (spot != null) {
                this.teleportTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5);
            }
        }
    }

    /** Schlafende lassen sich nicht wegschieben. */
    @Override
    public boolean isPushable() {
        return !this.isSleeping() && super.isPushable();
    }

    /** Wer in einer Wand steckt, erstickt nicht (er tritt im nächsten Augenblick hinaus). */
    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL) || super.isInvulnerableTo(level, source);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.NAME_TAG) || held.getItem() instanceof SpawnEggItem || !this.isAlive()) {
            return super.mobInteract(player, hand);
        }
        if (this.isSleeping()) {
            if (!this.level().isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.villager.sleeping", this.getName())
                        .withStyle(ChatFormatting.GRAY));
            }
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (player.isSecondaryUseActive() || !this.canTrade()) {
                this.getLookControl().setLookAt(player);
                VillageRumors.talk(serverPlayer, this);
            } else {
                Trading.open(serverPlayer, this);
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** „Agnes, Bäckerin aus Weidenbach“. */
    public Component describe() {
        Component job = Component.translatable(this.professionKey());
        if (this.villageName.isEmpty()) {
            return Component.translatable("message.kingdomsatwar.villager.who_alone", this.getName(), job);
        }
        return Component.translatable("message.kingdomsatwar.villager.who", this.getName(), job, this.villageName);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        Entity attacker = source.getEntity();
        // Pfeile der eigenen Wachen treffen keine Dorfbewohner
        if (attacker instanceof SoldierEntity soldier && soldier.isVillageGuard()) {
            return false;
        }
        if (this.isSleeping()) {
            this.stopSleeping();
        }
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && attacker instanceof LivingEntity enemy && enemy != this && !(enemy instanceof VillagerEntity)) {
            this.callGuards(enemy);
        }
        return hurt;
    }

    /** Die Wachen in der Nähe greifen den Angreifer an. */
    private void callGuards(LivingEntity enemy) {
        if (enemy instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }
        for (SoldierEntity guard : this.level().getEntitiesOfClass(SoldierEntity.class, this.getBoundingBox().inflate(32.0),
                s -> s.isVillageGuard() && s.isAlive() && s.getTarget() == null)) {
            guard.setTarget(enemy);
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (source.getEntity() instanceof ServerPlayer player && !player.isCreative()) {
            Reputation.add(player, -MURDER_PENALTY);
            player.sendSystemMessage(Component.translatable("message.kingdomsatwar.villager.murdered", this.getName(), MURDER_PENALTY)
                    .withStyle(ChatFormatting.RED));
        }
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
