package io.github.sirschrolli.kingdomsatwar.entity.umbrer;

import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;

/**
 * Die Tauschangebote der Umbrer. Bezahlt wird in Mondsilber: kleine Preise in Mondsilberklümpchen, größere in Barren
 * (ein Barren = neun Klümpchen). Was die Umbrer kaufen, bringt Klümpchen ein – so finanziert sich, wer in der
 * Schattenwelt jagt, erntet und schürft.
 *
 * <p>Die Angebote erneuern sich von selbst (siehe {@link UmbrerEntity}); seltene Waren gibt es nur einmal.
 */
public final class UmbrerTrades {
    private UmbrerTrades() {}

    /** Anzahl der Chronik-Seiten (Übersetzungsschlüssel {@code book.kingdomsatwar.umbrer_chronicle.pageN}). */
    public static final int CHRONICLE_PAGES = 5;

    public static MerchantOffers create(UmbrerProfession profession, ServerLevel level, RandomSource random) {
        List<MerchantOffer> offers = new ArrayList<>();
        switch (profession) {
            case MOONSMITH -> {
                offers.add(buy(ShadowBlocks.SHADOW_LOG.get(), 8, 2, 12));
                offers.add(buy(ShadowItems.NIGHT_CRYSTAL_SHARD.get(), 3, 1, 16));
                offers.add(sell(new ItemStack(ShadowBlocks.MOON_LANTERN.get(), 2), 5, 12));
                offers.add(sell(new ItemStack(Items.IRON_CHAIN, 6), 3, 12));
                offers.add(sell(new ItemStack(ShadowItems.MOONSILVER_BLADE.get()), 22, 3));
                offers.add(service(level, ShadowItems.NIGHTSTEEL_PICKAXE.get(), 27, Enchantments.EFFICIENCY, 3, Enchantments.UNBREAKING, 2));
                offers.add(service(level, ShadowItems.NIGHTSTEEL_SWORD.get(), 27, Enchantments.SHARPNESS, 3, Enchantments.UNBREAKING, 2));
            }
            case HERBALIST -> {
                offers.add(buy(ShadowItems.SOUL_BERRIES.get(), 8, 1, 16));
                offers.add(buy(ShadowBlocks.GHOSTCAP.get(), 4, 1, 16));
                offers.add(sell(PotionContents.createItemStack(Items.POTION, Potions.NIGHT_VISION), 4, 8));
                offers.add(sell(PotionContents.createItemStack(Items.POTION, Potions.HEALING), 5, 8));
                offers.add(sell(PotionContents.createItemStack(Items.POTION, Potions.REGENERATION), 7, 6));
                offers.add(sell(new ItemStack(ShadowBlocks.GLIMMER_MOSS.get(), 4), 2, 12));
                offers.add(sell(new ItemStack(ShadowBlocks.GLIMMER_VINES.get(), 4), 2, 12));
            }
            case CRYSTAL_CUTTER -> {
                offers.add(buy(ShadowItems.NIGHT_CRYSTAL_SHARD.get(), 6, 2, 16));
                offers.add(buy(ShadowItems.SOUL_CRYSTAL.get(), 1, 5, 8));
                offers.add(sell(new ItemStack(ShadowBlocks.NIGHT_CRYSTAL.get(), 4), 3, 12));
                offers.add(sell(new ItemStack(ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get(), 2), 2, 12));
                offers.add(sell(new ItemStack(ShadowItems.SOUL_CRYSTAL.get()), 9, 6));
                offers.add(sell(new ItemStack(Items.TINTED_GLASS, 8), 3, 12));
                offers.add(sell(new ItemStack(Items.SPYGLASS), 5, 4));
            }
            case SEER -> {
                offers.add(buy(ShadowItems.SHADOW_ESSENCE.get(), 3, 2, 16));
                offers.add(buy(ShadowItems.SOUL_CRYSTAL.get(), 1, 5, 8));
                offers.add(sell(chronicle(), 1, 4));
                offers.add(sell(randomBook(level, random), 18, 2));
                offers.add(sell(randomBook(level, random), 18, 2));
                offers.add(sell(new ItemStack(Items.EXPERIENCE_BOTTLE, 3), 4, 8));
                // die seltenste Ware: nur einmal, für vier Barren und zwei Seelenkristalle
                offers.add(new MerchantOffer(new ItemCost(ShadowItems.MOONSILVER_INGOT.get(), 4),
                        Optional.of(new ItemCost(ShadowItems.SOUL_CRYSTAL.get(), 2)),
                        new ItemStack(ShadowItems.ABYSSITE_UPGRADE_SMITHING_TEMPLATE.get()), 1, 30, 0.0f));
            }
            case FARMER -> {
                offers.add(buy(ShadowItems.DUSKGRAIN.get(), 16, 1, 16));
                offers.add(buy(ShadowItems.SOUL_BERRIES.get(), 6, 1, 16));
                offers.add(sell(new ItemStack(ShadowItems.DUSKGRAIN_SEEDS.get(), 4), 1, 16));
                offers.add(sell(new ItemStack(ShadowItems.DUSK_BREAD.get(), 3), 1, 16));
                offers.add(sell(new ItemStack(ModItems.COOKED_VENISON.get(), 4), 2, 12));
                offers.add(sell(new ItemStack(ShadowBlocks.SHADOW_SAPLING.get(), 2), 1, 8));
            }
            case GUARD -> {
                // Wächter tauschen nicht
            }
        }
        MerchantOffers result = new MerchantOffers();
        result.addAll(offers);
        return result;
    }

    // =====================================================================================
    // Bausteine
    // =====================================================================================

    /** Die Umbrer kaufen: {@code count} Stück gegen {@code nuggets} Mondsilberklümpchen. */
    private static MerchantOffer buy(ItemLike item, int count, int nuggets, int maxUses) {
        return new MerchantOffer(new ItemCost(item, count), new ItemStack(ShadowItems.MOONSILVER_NUGGET.get(), nuggets), maxUses, 2, 0.05f);
    }

    /** Die Umbrer verkaufen: Ware gegen einen Preis in Klümpchen (ab neun in Barren plus Rest-Klümpchen). */
    private static MerchantOffer sell(ItemStack result, int nuggets, int maxUses) {
        int ingots = nuggets / 9;
        int rest = nuggets % 9;
        ItemCost first = ingots > 0 ? new ItemCost(ShadowItems.MOONSILVER_INGOT.get(), ingots)
                : new ItemCost(ShadowItems.MOONSILVER_NUGGET.get(), rest);
        Optional<ItemCost> second = ingots > 0 && rest > 0 ? Optional.of(new ItemCost(ShadowItems.MOONSILVER_NUGGET.get(), rest))
                : Optional.empty();
        return new MerchantOffer(first, second, result, maxUses, 3 + nuggets / 4, 0.05f);
    }

    /** Dienst des Mondschmieds: ein Werkzeug aus Nachtstahl samt Mondsilber gegen dasselbe Werkzeug, verzaubert. */
    private static MerchantOffer service(ServerLevel level, Item tool, int nuggets, ResourceKey<Enchantment> first, int firstLevel,
            ResourceKey<Enchantment> second, int secondLevel) {
        ItemStack result = new ItemStack(tool);
        result.enchant(enchantment(level, first), firstLevel);
        result.enchant(enchantment(level, second), secondLevel);
        return new MerchantOffer(new ItemCost(ShadowItems.MOONSILVER_INGOT.get(), nuggets / 9), Optional.of(new ItemCost(tool)), result,
                3, 15, 0.0f);
    }

    private static Holder<Enchantment> enchantment(ServerLevel level, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    /** Ein verzaubertes Buch aus dem Wissen der Seher. */
    private static ItemStack randomBook(ServerLevel level, RandomSource random) {
        List<EnchantmentInstance> choices = List.of(
                new EnchantmentInstance(enchantment(level, Enchantments.PROTECTION), 3),
                new EnchantmentInstance(enchantment(level, Enchantments.EFFICIENCY), 3),
                new EnchantmentInstance(enchantment(level, Enchantments.FEATHER_FALLING), 4),
                new EnchantmentInstance(enchantment(level, Enchantments.UNBREAKING), 3),
                new EnchantmentInstance(enchantment(level, Enchantments.LOOTING), 2),
                new EnchantmentInstance(enchantment(level, Enchantments.MENDING), 1));
        return EnchantmentHelper.createBook(choices.get(random.nextInt(choices.size())));
    }

    /** „Die Chronik der Umbrer“: die Geschichte der Schattenwelt, des Siegels und Nyxars (in der Sprache des Lesers). */
    public static ItemStack chronicle() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        List<Filterable<Component>> pages = new ArrayList<>();
        for (int i = 1; i <= CHRONICLE_PAGES; i++) {
            pages.add(Filterable.passThrough(Component.translatable("book.kingdomsatwar.umbrer_chronicle.page" + i)));
        }
        // ohne eigenen Titel: so gilt der (übersetzte) Gegenstandsname, nicht kursiv wie ein selbst vergebener Name
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough(""), "Ilvaren", 0, pages, true));
        book.set(DataComponents.ITEM_NAME, Component.translatable("book.kingdomsatwar.umbrer_chronicle.title"));
        return book;
    }
}
