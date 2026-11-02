package io.github.sirschrolli.kingdomsatwar.entity.villager;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.CoinTrader;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownNames;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Fahrender Händler (statt Vanilla-Händler): ein Reisender mit breitkrempigem Hut, langem Mantel und einer Kiepe auf
 * dem Rücken (Weidenkorb mit Deckenrolle, Kupferkessel und Laterne). Er kommt wie der Vanilla-Händler von Zeit zu Zeit
 * mit seinen Lamas vorbei, trinkt nachts einen Unsichtbarkeitstrank und morgens Milch und zieht nach ein paar Tagen
 * weiter – gehandelt wird aber gegen Münzen (siehe {@link VillageTrades#traveler}), mit Ansehen-Rabatt und Ladenpause
 * wie bei den Dorfbewohnern.
 *
 * <p>Drei Gesichter: ein alter Mann mit grauem Vollbart, ein jüngerer mit Schnurrbart und eine rothaarige Händlerin.
 */
public class TravelingMerchant extends WanderingTrader implements CoinTrader {
    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(TravelingMerchant.class, EntityDataSerializers.BYTE);

    /** Anzahl der Gesichter (das letzte ist eine Frau). */
    public static final int VARIANTS = 3;

    private long tradeSeed;
    private int stock;
    private int tradesUsed;
    private long closedUntil;
    private boolean initialized;
    private @Nullable Player customer;
    private int customerTicks;

    public TravelingMerchant(EntityType<? extends TravelingMerchant> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, (byte) 0);
    }

    /** Richtet einen neuen Händler ein: Gesicht, Name und der Zufall für seine Waren. */
    public void setup(int variant, String name, long seed) {
        this.setVariant(variant);
        this.setCustomName(Component.literal(name));
        this.tradeSeed = seed;
        this.initialized = true;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    /** Ein zufälliger Händler (aus dem Spawn-Ei, per Befehl oder beim Umwandeln eines Vanilla-Händlers). */
    public void setupRandom() {
        int variant = this.random.nextInt(VARIANTS);
        this.setup(variant, TownNames.citizen(this.random, variant == VARIANTS - 1), this.random.nextLong());
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData data) {
        if (!this.initialized) {
            this.setupRandom();
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    public int getVariant() {
        return Mth.clamp(this.entityData.get(DATA_VARIANT), 0, VARIANTS - 1);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, (byte) Mth.clamp(variant, 0, VARIANTS - 1));
    }

    public boolean isFeminine() {
        return this.getVariant() == VARIANTS - 1;
    }

    /** Die Haut eines Händlers mit diesem Gesicht. */
    public static Identifier texture(int variant) {
        return KingdomsAtWar.id("textures/entity/villager/traveler_" + (Mth.clamp(variant, 0, VARIANTS - 1) + 1) + ".png");
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Variant", this.getVariant());
        output.putLong("TradeSeed", this.tradeSeed);
        output.putInt("Stock", this.stock);
        output.putInt("TradesUsed", this.tradesUsed);
        output.putLong("ClosedUntil", this.closedUntil);
        output.putBoolean("Initialized", this.initialized);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(input.getIntOr("Variant", 0));
        this.tradeSeed = input.getLongOr("TradeSeed", 0L);
        this.stock = input.getIntOr("Stock", 0);
        this.tradesUsed = input.getIntOr("TradesUsed", 0);
        this.closedUntil = input.getLongOr("ClosedUntil", 0L);
        this.initialized = input.getBooleanOr("Initialized", false);
    }

    // =====================================================================================
    // Handel gegen Münzen
    // =====================================================================================

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.NAME_TAG) || held.is(Items.LEAD) || held.getItem() instanceof SpawnEggItem || !this.isAlive()) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (hand == InteractionHand.MAIN_HAND) {
                player.awardStat(Stats.TALKED_TO_VILLAGER);
            }
            Trading.open(serverPlayer, this);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canTrade() {
        return true;
    }

    @Override
    public List<Trading.Offer> baseOffers() {
        return VillageTrades.traveler(this.registryAccess(), this.tradeSeed * 31L + this.stock);
    }

    @Override
    public int tradesUsed() {
        return this.tradesUsed;
    }

    @Override
    public long closedUntil() {
        return this.closedUntil;
    }

    /** Nach der Pause öffnet der Händler seine Kiepe wieder – mit neuen Waren. */
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

    /** Solange jemand handelt, bleibt der Händler stehen und schaut ihn an (wie beim Vanilla-Handel). */
    @Override
    public void setCustomer(@Nullable Player customer) {
        this.customer = customer;
        this.customerTicks = customer == null ? 0 : 400;
        this.setTradingPlayer(customer);
    }

    @Override
    public String skinTexture() {
        return texture(this.getVariant()).toString();
    }

    @Override
    public String professionKey() {
        return "villager.kingdomsatwar.profession.traveler" + (this.isFeminine() ? ".female" : "");
    }

    @Override
    public Component traderName() {
        return this.getName();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && this.customer != null
                && (--this.customerTicks <= 0 || !this.customer.isAlive() || this.customer.distanceToSqr(this) > 64.0)) {
            this.setCustomer(null);
        }
    }
}
