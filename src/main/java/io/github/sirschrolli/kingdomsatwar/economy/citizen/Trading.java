package io.github.sirschrolli.kingdomsatwar.economy.citizen;

import io.github.sirschrolli.kingdomsatwar.config.ModServerConfig;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.economy.Reputation;
import io.github.sirschrolli.kingdomsatwar.economy.town.Profession;
import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import io.github.sirschrolli.kingdomsatwar.fishing.FishBag;
import io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies;
import io.github.sirschrolli.kingdomsatwar.network.OpenTradePayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

/**
 * Handel mit den Bürgern: Schmied, Händler, Wirt und Kartograf kaufen und verkaufen gegen Münzen. Je höher das Ansehen
 * des Spielers, desto günstiger der Einkauf und desto besser der Verkauf; dem gekrönten Herrscher geben die eigenen
 * Bürger alles umsonst. Nach einer gewissen Zahl von Handeln macht der Laden Pause – in Echtzeit (Standard drei Stunden).
 */
public final class Trading {
    /**
     * Ein Angebot: was der Spieler gibt (Gegenstand und/oder Münzen) und was er bekommt. {@code special} kennzeichnet
     * Waren, die erst beim Handel erzeugt werden (Karten zum Siegelkreis oder zu einem Drachennest).
     */
    public record Offer(ItemStack give, int giveCoins, ItemStack get, int getCoins, String special) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Offer> STREAM_CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_STREAM_CODEC, Offer::give,
                ByteBufCodecs.VAR_INT, Offer::giveCoins,
                ItemStack.OPTIONAL_STREAM_CODEC, Offer::get,
                ByteBufCodecs.VAR_INT, Offer::getCoins,
                ByteBufCodecs.STRING_UTF8, Offer::special,
                Offer::new);

        /** Der Spieler kauft etwas für Münzen. */
        public static Offer buy(ItemLike item, int count, int price) {
            return new Offer(ItemStack.EMPTY, price, new ItemStack(item, count), 0, "");
        }

        /** Der Spieler kauft eine besondere Ware (verzaubert, gefärbt, ein Trank …) für Münzen. */
        public static Offer buy(ItemStack goods, int price) {
            return new Offer(ItemStack.EMPTY, price, goods, 0, "");
        }

        /** Der Spieler verkauft etwas und bekommt Münzen. */
        public static Offer sell(ItemLike item, int count, int price) {
            return new Offer(new ItemStack(item, count), 0, ItemStack.EMPTY, price, "");
        }

        /** Ware, die erst beim Handel entsteht (z. B. eine Karte); {@code shown} ist nur das Bild im Fenster. */
        public static Offer special(String id, ItemStack shown, int price) {
            return new Offer(ItemStack.EMPTY, price, shown, 0, id);
        }

        public boolean selling() {
            return !this.give.isEmpty();
        }
    }

    private Trading() {}

    public static int limit() {
        return ModServerConfig.SPEC.isLoaded() ? ModServerConfig.TRADES_BEFORE_PAUSE.get() : ModServerConfig.TRADES_BEFORE_PAUSE.getDefault();
    }

    public static long pauseMillis() {
        int minutes = ModServerConfig.SPEC.isLoaded() ? ModServerConfig.TRADE_PAUSE_MINUTES.get() : ModServerConfig.TRADE_PAUSE_MINUTES.getDefault();
        return minutes * 60_000L;
    }

    // =====================================================================================
    // Angebote
    // =====================================================================================

    /** Grundangebote eines Berufs (Preise in Bronze, vor Rabatt). */
    public static List<Offer> baseOffers(Profession profession) {
        List<Offer> list = new ArrayList<>();
        switch (profession) {
            case SMITH -> {
                list.add(Offer.buy(ModItems.STEEL_SWORD.get(), 1, 40));
                list.add(Offer.buy(ModItems.STEEL_PICKAXE.get(), 1, 45));
                list.add(Offer.buy(ModItems.STEEL_AXE.get(), 1, 40));
                list.add(Offer.buy(ModItems.STEEL_HELMET.get(), 1, 50));
                list.add(Offer.buy(ModItems.STEEL_CHESTPLATE.get(), 1, 80));
                list.add(Offer.buy(ModItems.STEEL_LEGGINGS.get(), 1, 70));
                list.add(Offer.buy(ModItems.STEEL_BOOTS.get(), 1, 45));
                list.add(Offer.buy(Items.SHIELD, 1, 20));
                list.add(Offer.buy(ModItems.STEEL_INGOT.get(), 2, 18));
                list.add(Offer.sell(Items.IRON_INGOT, 4, 10));
                list.add(Offer.sell(Items.COAL, 16, 6));
                list.add(Offer.sell(Items.RAW_IRON, 6, 10));
                list.add(Offer.sell(Items.GOLD_INGOT, 2, 12));
            }
            case MERCHANT -> {
                list.add(Offer.sell(Items.WHEAT, 20, 5));
                list.add(Offer.sell(Items.CARROT, 20, 5));
                list.add(Offer.sell(Items.POTATO, 20, 5));
                list.add(Offer.sell(Items.BEETROOT, 16, 4));
                list.add(Offer.sell(Items.OAK_LOG, 16, 6));
                list.add(Offer.sell(Items.SPRUCE_LOG, 16, 6));
                list.add(Offer.sell(Items.COBBLESTONE, 32, 4));
                list.add(Offer.sell(Items.LEATHER, 6, 8));
                list.add(Offer.sell(Items.WOOL.white(), 8, 6));
                list.add(Offer.sell(Items.EMERALD, 1, 6));
                list.add(Offer.buy(Items.EMERALD, 1, 12));
                list.add(Offer.buy(Items.GLASS, 8, 10));
                list.add(Offer.buy(Items.BRICKS, 16, 12));
                list.add(Offer.buy(Items.LANTERN, 2, 8));
                list.add(Offer.buy(Items.TORCH, 16, 4));
                list.add(Offer.buy(ModItems.TOMATO_SEEDS.get(), 4, 5));
                list.add(Offer.buy(ModItems.CABBAGE_SEEDS.get(), 4, 5));
                list.add(Offer.buy(Items.NAME_TAG, 1, 40));
                list.add(Offer.buy(Items.SADDLE, 1, 60));
            }
            case INNKEEPER -> {
                list.add(Offer.buy(ModItems.BREAD_ROLL.get(), 4, 4));
                list.add(Offer.buy(ModItems.VEGETABLE_STEW.get(), 1, 4));
                list.add(Offer.buy(ModItems.MEAT_PIE.get(), 2, 8));
                list.add(Offer.buy(ModItems.APPLE_PIE.get(), 2, 7));
                list.add(Offer.buy(ModItems.GARLIC_BREAD.get(), 2, 6));
                list.add(Offer.buy(ModItems.COOKED_VENISON.get(), 3, 9));
                list.add(Offer.buy(ModItems.ALE.get(), 1, 5));
                list.add(Offer.buy(ModItems.WINE.get(), 1, 7));
                list.add(Offer.buy(ModItems.MEAD.get(), 1, 9));
                list.add(Offer.buy(ModBlocks.FEAST.get(), 1, 40));
                list.add(Offer.sell(Items.WHEAT, 20, 5));
                list.add(Offer.sell(ModItems.BARLEY.get(), 16, 6));
                list.add(Offer.sell(ModItems.HOPS.get(), 16, 6));
                list.add(Offer.sell(ModItems.GRAPES.get(), 16, 6));
                list.add(Offer.sell(Items.HONEY_BOTTLE, 4, 8));
            }
            case CARTOGRAPHER -> {
                list.add(Offer.buy(Items.MAP, 1, 8));
                list.add(Offer.buy(Items.COMPASS, 1, 15));
                list.add(Offer.special("seal_map", named(Items.FILLED_MAP, "item.kingdomsatwar.map.seal_circle"), 50));
                list.add(Offer.special("nest_map", named(Items.FILLED_MAP, "item.kingdomsatwar.map.nest"), 120));
                list.add(Offer.special("realm_map", named(Items.FILLED_MAP, "item.kingdomsatwar.map.realm"), 150));
                list.add(Offer.buy(ModItems.DRAGON_COMPASS.get(), 1, 300));
                list.add(Offer.buy(Items.SPYGLASS, 1, 25));
                list.add(Offer.buy(Items.WRITABLE_BOOK, 1, 10));
                list.add(Offer.sell(Items.PAPER, 24, 5));
                list.add(Offer.sell(Items.FEATHER, 16, 4));
            }
            default -> {
            }
        }
        return list;
    }

    public static ItemStack named(Item item, String key) {
        ItemStack stack = new ItemStack(item);
        stack.set(net.minecraft.core.component.DataComponents.ITEM_NAME, Component.translatable(key));
        return stack;
    }

    /** Wie viel günstiger bzw. besser es für diesen Spieler ist (in Prozent). */
    public static int discount(Player player, CoinTrader trader) {
        return Reputation.rank(player).discount();
    }

    /** Der gekrönte Herrscher bekommt bei den eigenen Bürgern alles umsonst. */
    public static boolean free(Player player, CitizenEntity citizen) {
        Town town = citizen.town();
        return town != null && town.crowned() && town.owner().equals(player.getUUID());
    }

    /** Die Angebote mit den Preisen für diesen Spieler. */
    public static List<Offer> offers(Player player, CoinTrader trader) {
        int discount = discount(player, trader);
        boolean free = trader.givesFreely(player);
        List<Offer> priced = new ArrayList<>();
        for (Offer o : trader.baseOffers()) {
            if (o.special().equals(FishBag.UPGRADE_OFFER)) {
                priced.add(bagUpgrade(player, discount, free));
                continue;
            }
            int pay = free ? 0 : o.giveCoins() <= 0 ? 0 : Math.max(1, Math.round(o.giveCoins() * (100 - discount) / 100.0f));
            int earn = o.getCoins() <= 0 ? 0 : Math.round(o.getCoins() * (100 + discount / 2) / 100.0f);
            priced.add(new Offer(o.give(), pay, o.get(), earn, o.special()));
        }
        return priced;
    }

    /**
     * Die Erweiterung des Fischbeutels (beim Fischer): Preis und Bild richten sich nach dem Beutel, den dieser Spieler bei sich
     * trägt – jede Stufe kostet mehr als die vorige ({@link FishBag#upgradePrice}).
     */
    private static Offer bagUpgrade(Player player, int discount, boolean free) {
        ItemStack bag = FishBag.forUpgrade(player);
        int level = bag.isEmpty() ? 0 : FishBag.of(bag).level();
        int pay = free ? 0 : Math.max(1, Math.round(FishBag.upgradePrice(level) * (100 - discount) / 100.0f));
        return new Offer(ItemStack.EMPTY, pay, bagUpgradeIcon(level), 0, FishBag.UPGRADE_OFFER);
    }

    /** Das Bild der Erweiterung von {@code level} auf die nächste Stufe. */
    public static ItemStack bagUpgradeIcon(int level) {
        ItemStack shown = new ItemStack(io.github.sirschrolli.kingdomsatwar.registry.FishingItems.FISH_BAG.get());
        shown.set(io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents.FISH_BAG.get(), new FishBag(level + 1, java.util.Map.of()));
        shown.set(net.minecraft.core.component.DataComponents.ITEM_NAME,
                Component.translatable("trade.kingdomsatwar.fish_bag_upgrade", level + 2, FishBag.capacity(level + 1)));
        return shown;
    }

    /** Der Fischer kauft Fänge auf: {@code count} beliebige Fische dieser Seltenheit (aus Inventar oder Fischbeutel) für {@code price}. */
    public static Offer fishSale(FishSpecies.Rarity rarity, FishSpecies icon, int count, int price) {
        String key = rarity.name().toLowerCase(java.util.Locale.ROOT);
        ItemStack shown = new ItemStack(icon.item(), count);
        shown.set(net.minecraft.core.component.DataComponents.ITEM_NAME, Component.translatable("trade.kingdomsatwar.fish." + key).withColor(rarity.color));
        shown.set(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(List.of(
                Component.translatable("trade.kingdomsatwar.fish.any").withStyle(style -> style.withItalic(false).withColor(ChatFormatting.GRAY)),
                Component.translatable("trade.kingdomsatwar.fish.from").withStyle(style -> style.withItalic(false).withColor(ChatFormatting.DARK_GRAY)))));
        return new Offer(shown, 0, ItemStack.EMPTY, price, FishBag.SELL_OFFER + key);
    }

    // =====================================================================================
    // Handel
    // =====================================================================================

    /** Öffnet das Handelsfenster (auf dem Server). */
    public static <T extends LivingEntity & CoinTrader> void open(ServerPlayer player, T trader) {
        trader.setCustomer(player);
        trader.level().playSound(null, trader.getX(), trader.getY(), trader.getZ(), SoundEvents.VILLAGER_TRADE, SoundSource.NEUTRAL, 0.6f, 1.0f);
        send(player, trader);
    }

    static <T extends LivingEntity & CoinTrader> void send(ServerPlayer player, T trader) {
        boolean closed = trader.isClosed();
        long remaining = closed ? Math.max(0, trader.closedUntil() - System.currentTimeMillis()) : 0;
        io.github.sirschrolli.kingdomsatwar.network.ModNetwork.send(player, new OpenTradePayload(trader.getId(), trader.skinTexture(),
                trader.professionKey(), trader.traderName(), offers(player, trader), Math.max(0, limit() - trader.tradesUsed()), remaining,
                discount(player, trader), trader.givesFreely(player), limit(), (int) (pauseMillis() / 60_000L)));
    }

    public enum Result { DONE, TOO_FAR, CLOSED, MISSING_ITEMS, MISSING_COINS, NO_SPACE, NOTHING }

    /** Führt einen Handel aus (auf dem Server; alles wird dort noch einmal geprüft). */
    public static Result trade(ServerPlayer player, int entityId, int index) {
        if (!(player.level().getEntity(entityId) instanceof LivingEntity entity) || !(entity instanceof CoinTrader trader) || !trader.canTrade()) {
            return Result.TOO_FAR;
        }
        return trade(player, (LivingEntity & CoinTrader) entity, index);
    }

    private static <T extends LivingEntity & CoinTrader> Result trade(ServerPlayer player, T citizen, int index) {
        if (citizen.distanceToSqr(player) > 8.0 * 8.0 || !citizen.isAlive()) {
            return Result.TOO_FAR;
        }
        if (citizen.isClosed()) {
            send(player, citizen);
            return Result.CLOSED;
        }
        List<Offer> offers = offers(player, citizen);
        if (index < 0 || index >= offers.size()) {
            return Result.NOTHING;
        }
        Offer offer = offers.get(index);
        boolean bagUpgrade = offer.special().equals(FishBag.UPGRADE_OFFER);
        FishSpecies.Rarity fishSale = FishBag.sellRarity(offer.special());
        if (fishSale != null) {
            // „beliebige Fische dieser Seltenheit“: lose im Inventar oder im Fischbeutel
            if (!player.hasInfiniteMaterials() && FishBag.countFish(player, fishSale) < offer.give().getCount()) {
                return Result.MISSING_ITEMS;
            }
        } else if (!offer.give().isEmpty() && !player.hasInfiniteMaterials()
                && player.getInventory().countItem(offer.give().getItem()) < offer.give().getCount()) {
            return Result.MISSING_ITEMS;
        }
        if (!Coins.canAfford(player, offer.giveCoins())) {
            return Result.MISSING_COINS;
        }
        ItemStack goods = offer.get().copy();
        if (bagUpgrade) {
            if (FishBag.forUpgrade(player).isEmpty()) {
                player.sendOverlayMessage(Component.translatable("gui.kingdomsatwar.trade.missing_bag").withStyle(ChatFormatting.GRAY));
                return Result.NOTHING;
            }
            goods = ItemStack.EMPTY;
        } else if (fishSale != null) {
            goods = ItemStack.EMPTY;
        } else if (!offer.special().isEmpty()) {
            goods = special(player, citizen, offer.special());
            if (goods.isEmpty()) {
                return Result.NOTHING;
            }
        }
        // bezahlen
        if (fishSale != null) {
            if (!player.hasInfiniteMaterials()) {
                FishBag.removeFish(player, fishSale, offer.give().getCount());
            }
        } else if (!offer.give().isEmpty() && !player.hasInfiniteMaterials()) {
            player.getInventory().clearOrCountMatchingItems(stack -> stack.is(offer.give().getItem()), false, offer.give().getCount(),
                    player.inventoryMenu.getCraftSlots());
        }
        Coins.pay(player, offer.giveCoins());
        if (bagUpgrade) {
            // der Beutel wächst, sein Inhalt bleibt
            FishBag.upgrade(player);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 0.9f, 0.8f);
        }
        // bekommen
        if (!goods.isEmpty()) {
            player.getInventory().placeItemBackInInventory(goods, Prediction.SERVER_ONLY);
        }
        if (offer.getCoins() > 0) {
            Coins.give(player, offer.getCoins());
        }
        if (!bagUpgrade) {
            // die Erweiterung des Fischbeutels ist kein Warenhandel: Sie zählt nicht zur Handelsgrenze des Ladens
            citizen.recordTrade(limit(), pauseMillis());
        }
        Reputation.add(player, Reputation.TRADE);
        citizen.level().playSound(null, citizen.getX(), citizen.getY(), citizen.getZ(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.6f, 1.0f);
        citizen.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CHAIN_HIT, SoundSource.PLAYERS, 0.5f, 1.7f);
        if (citizen.isClosed()) {
            player.sendSystemMessage(Component.translatable("message.kingdomsatwar.trade.closing", citizen.getName(),
                    Component.literal(String.valueOf(pauseMillis() / 60_000L))).withStyle(ChatFormatting.GOLD));
        }
        citizen.setCustomer(player);
        send(player, citizen);
        return Result.DONE;
    }

    /**
     * Waren, die erst beim Handel entstehen: Karten zum Siegelkreis, zu einem noch unbesiegten Drachennest, zum
     * nächsten anderen Dorf, zum nächsten Magierturm und zur nächsten noch freien Stammburg eines fremden Reichs.
     */
    private static ItemStack special(ServerPlayer player, LivingEntity trader, String id) {
        ServerLevel level = player.level();
        ServerLevel overworld = level.getServer().overworld();
        if (id.equals("realm_map")) {
            ItemStack map = level == overworld ? Maps.realmMap(overworld, player.blockPosition()) : null;
            if (map == null) {
                player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.trade.no_target").withStyle(ChatFormatting.GRAY));
                return ItemStack.EMPTY;
            }
            return map;
        }
        BlockPos target = switch (id) {
            case "seal_map" -> io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLocator.approximateCenter(overworld);
            case "nest_map" -> nearestNest(overworld, player.blockPosition());
            case "village_map" -> level == overworld ? Maps.nearestOtherVillage(overworld, trader) : null;
            case "tower_map" -> level == overworld ? Maps.nearestMageTower(overworld, trader.blockPosition()) : null;
            default -> null;
        };
        if (target == null) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.trade.no_target").withStyle(ChatFormatting.GRAY));
            return ItemStack.EMPTY;
        }
        return Maps.explorerMap(overworld, target, "item.kingdomsatwar.map." + switch (id) {
            case "seal_map" -> "seal_circle";
            case "nest_map" -> "nest";
            case "village_map" -> "village";
            default -> "mage_tower";
        });
    }

    /** Das nächste Nest eines noch unbesiegten Drachen (von {@code from} aus). */
    public static @Nullable BlockPos nearestNest(ServerLevel overworld, BlockPos from) {
        var records = io.github.sirschrolli.kingdomsatwar.world.LairRecords.of(overworld);
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (var entry : io.github.sirschrolli.kingdomsatwar.worldgen.lair.NestLocator.all(overworld).entrySet()) {
            if (records.isDefeated(entry.getKey())) {
                continue;
            }
            BlockPos pos = new BlockPos(entry.getValue().getMiddleBlockX(), overworld.getSeaLevel(), entry.getValue().getMiddleBlockZ());
            double d = pos.distSqr(from);
            if (d < bestDist) {
                best = pos;
                bestDist = d;
            }
        }
        return best;
    }
}
