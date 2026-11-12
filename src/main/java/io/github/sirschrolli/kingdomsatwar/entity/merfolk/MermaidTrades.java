package io.github.sirschrolli.kingdomsatwar.entity.merfolk;

import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.SeaWoods;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;

/**
 * Die Tauschangebote des Meervolks (seit 0.93). Bezahlt wird in <b>Perlen</b>: Was die Meerjungfrauen kaufen (Fisch,
 * Tang, Knochenmehl, Prismarin, Tinte …), bringt Perlen ein; Perlen findet man außerdem in Muscheln und in den Truhen der
 * Meeresdörfer. Die Angebote erneuern sich von selbst (siehe {@link MermaidEntity}); seltene Waren gibt es nur einmal.
 */
public final class MermaidTrades {
    private MermaidTrades() {}

    public static MerchantOffers create(MermaidProfession profession, ServerLevel level, RandomSource random) {
        List<MerchantOffer> offers = new ArrayList<>();
        switch (profession) {
            case PEARL_DIVER -> {
                offers.add(buy(Items.COD, 12, 1, 16));
                offers.add(buy(Items.TROPICAL_FISH, 6, 1, 16));
                offers.add(buy(SeaBlocks.SHARK_TOOTH.get(), 2, 1, 12));
                offers.add(sell(new ItemStack(ModBlocks.NACRE_BLOCK.get(), 4), 3, 12));
                offers.add(sell(new ItemStack(Items.NAUTILUS_SHELL), 6, 6));
                offers.add(sell(new ItemStack(Items.TURTLE_HELMET), 16, 2));
                // das kostbarste Stück: nur einmal
                offers.add(sell(new ItemStack(Items.HEART_OF_THE_SEA), 40, 1));
            }
            case CORAL_GARDENER -> {
                offers.add(buy(Items.BONE_MEAL, 16, 1, 16));
                offers.add(buy(Items.KELP, 24, 1, 16));
                offers.add(buy(Items.SEAGRASS, 16, 1, 16));
                for (SeaWoods.SeaWood wood : SeaWoods.ALL) {
                    offers.add(sell(new ItemStack(wood.sapling.get(), 2), 3, 8));
                }
                ItemLike[] corals = {Items.TUBE_CORAL_BLOCK, Items.BRAIN_CORAL_BLOCK, Items.BUBBLE_CORAL_BLOCK, Items.FIRE_CORAL_BLOCK,
                        Items.HORN_CORAL_BLOCK};
                offers.add(sell(new ItemStack(corals[random.nextInt(corals.length)], 4), 2, 12));
                offers.add(sell(new ItemStack(Items.SEA_PICKLE, 4), 1, 12));
                offers.add(sell(new ItemStack(SeaBlocks.GIANT_KELP.get(), 4), 1, 12));
                ItemLike[] anemones = {SeaBlocks.PURPLE_SEA_ANEMONE.get(), SeaBlocks.RED_SEA_ANEMONE.get(), SeaBlocks.GREEN_SEA_ANEMONE.get(),
                        SeaBlocks.ORANGE_SEA_ANEMONE.get()};
                offers.add(sell(new ItemStack(anemones[random.nextInt(anemones.length)], 2), 1, 12));
            }
            case SHELL_BUILDER -> {
                offers.add(buy(Items.PRISMARINE_SHARD, 8, 1, 16));
                offers.add(buy(Items.CLAY_BALL, 16, 1, 16));
                SeaWoods.SeaWood wood = SeaWoods.ALL.get(random.nextInt(SeaWoods.ALL.size()));
                offers.add(sell(new ItemStack(wood.log.get(), 6), 2, 12));
                offers.add(sell(new ItemStack(wood.planks.base().get(), 12), 1, 16));
                offers.add(sell(new ItemStack(wood.door.get(), 1), 2, 8));
                offers.add(sell(new ItemStack(Items.SEA_LANTERN, 2), 3, 12));
                offers.add(sell(new ItemStack(SeaBlocks.DEEP_SEA_LANTERN.get(), 2), 3, 12));
                offers.add(sell(new ItemStack(Items.PRISMARINE_BRICKS, 8), 2, 12));
                offers.add(sell(new ItemStack(Items.DARK_PRISMARINE, 8), 3, 12));
                offers.add(sell(new ItemStack(SeaBlocks.TIDE_STONE_BRICKS.base().get(), 8), 2, 12));
            }
            case TIDE_SINGER -> {
                offers.add(buy(Items.INK_SAC, 8, 1, 16));
                offers.add(buy(Items.GLOW_INK_SAC, 4, 1, 16));
                offers.add(buy(SeaBlocks.GLOW_JELLY.get(), 4, 1, 16));
                offers.add(buy(Items.PRISMARINE_CRYSTALS, 6, 1, 16));
                offers.add(sell(PotionContents.createItemStack(Items.POTION, Potions.LONG_WATER_BREATHING), 3, 8));
                offers.add(sell(PotionContents.createItemStack(Items.POTION, Potions.LONG_NIGHT_VISION), 3, 8));
                offers.add(sell(PotionContents.createItemStack(Items.POTION, Potions.HEALING), 4, 8));
                offers.add(sell(randomBook(level, random), 14, 2));
                offers.add(sell(randomBook(level, random), 14, 2));
                offers.add(sell(new ItemStack(Items.CONDUIT), 32, 1));
            }
            case GUARD -> {
                // Wachen tauschen nicht
            }
        }
        MerchantOffers result = new MerchantOffers();
        result.addAll(offers);
        return result;
    }

    /** Das Meervolk kauft: {@code count} Stück gegen {@code pearls} Perlen. */
    private static MerchantOffer buy(ItemLike item, int count, int pearls, int maxUses) {
        return new MerchantOffer(new ItemCost(item, count), new ItemStack(SeaBlocks.PEARL.get(), pearls), maxUses, 2, 0.05f);
    }

    /** Das Meervolk verkauft: Ware gegen Perlen. */
    private static MerchantOffer sell(ItemStack result, int pearls, int maxUses) {
        return new MerchantOffer(new ItemCost(SeaBlocks.PEARL.get(), pearls), result, maxUses, 3 + pearls / 4, 0.05f);
    }

    private static Holder<Enchantment> enchantment(ServerLevel level, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    /** Ein verzaubertes Buch mit einer Verzauberung des Wassers. */
    private static ItemStack randomBook(ServerLevel level, RandomSource random) {
        List<EnchantmentInstance> choices = List.of(
                new EnchantmentInstance(enchantment(level, Enchantments.DEPTH_STRIDER), 3),
                new EnchantmentInstance(enchantment(level, Enchantments.RESPIRATION), 3),
                new EnchantmentInstance(enchantment(level, Enchantments.AQUA_AFFINITY), 1),
                new EnchantmentInstance(enchantment(level, Enchantments.RIPTIDE), 3),
                new EnchantmentInstance(enchantment(level, Enchantments.LOYALTY), 3),
                new EnchantmentInstance(enchantment(level, Enchantments.IMPALING), 4),
                new EnchantmentInstance(enchantment(level, Enchantments.LUCK_OF_THE_SEA), 3),
                new EnchantmentInstance(enchantment(level, Enchantments.LURE), 3));
        return EnchantmentHelper.createBook(choices.get(random.nextInt(choices.size())));
    }
}
