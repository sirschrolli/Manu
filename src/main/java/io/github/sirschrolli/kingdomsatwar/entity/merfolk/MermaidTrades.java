package io.github.sirschrolli.kingdomsatwar.entity.merfolk;

import io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.Offer;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.SeaWoods;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;

/**
 * Die Angebote des Meervolks. Seit 0.99 handeln die Meerjungfrauen wie alle anderen Händler: im selben Handelsfenster,
 * gegen <b>Münzen</b> (Preise in Bronze, Rabatt durch Ansehen, nach einigen Handeln eine Pause – siehe {@link Trading}).
 * Was sie kaufen (Fisch, Tang, Knochenmehl, Prismarin, Tinte, Perlen …), bringt Münzen ein. Welche Korallen, Anemonen,
 * welches Holz und welche Bücher es gerade gibt, hängt am Vorrat ({@code seed}): Er wechselt nach jeder Pause. Seit 1.9.2
 * erweitert jede von ihnen auch den Fischbeutel (wie der Fischer im Dorf; jede Erweiterung ist ein Kauf).
 */
public final class MermaidTrades {
    private MermaidTrades() {}

    public static List<Offer> offers(MermaidProfession profession, HolderLookup.Provider registries, long seed) {
        RandomSource random = RandomSource.create(seed);
        List<Offer> list = new ArrayList<>();
        switch (profession) {
            case PEARL_DIVER -> {
                list.add(Offer.sell(Items.COD, 12, 6));
                list.add(Offer.sell(Items.TROPICAL_FISH, 6, 6));
                list.add(Offer.sell(SeaBlocks.SHARK_TOOTH.get(), 2, 6));
                list.add(Offer.sell(SeaBlocks.PEARL.get(), 2, 10));
                list.add(Offer.buy(SeaBlocks.PEARL.get(), 1, 8));
                list.add(Offer.buy(ModBlocks.NACRE_BLOCK.get(), 4, 18));
                list.add(Offer.buy(Items.NAUTILUS_SHELL, 1, 36));
                list.add(Offer.buy(Items.TURTLE_HELMET, 1, 80));
                // das kostbarste Stück
                list.add(Offer.buy(Items.HEART_OF_THE_SEA, 1, 240));
            }
            case CORAL_GARDENER -> {
                list.add(Offer.sell(Items.BONE_MEAL, 16, 6));
                list.add(Offer.sell(Items.KELP, 24, 6));
                list.add(Offer.sell(Items.SEAGRASS, 16, 6));
                for (SeaWoods.SeaWood wood : SeaWoods.ALL) {
                    list.add(Offer.buy(wood.sapling.get(), 2, 15));
                }
                ItemLike[] corals = {Items.TUBE_CORAL_BLOCK, Items.BRAIN_CORAL_BLOCK, Items.BUBBLE_CORAL_BLOCK, Items.FIRE_CORAL_BLOCK,
                        Items.HORN_CORAL_BLOCK};
                list.add(Offer.buy(corals[random.nextInt(corals.length)], 4, 12));
                list.add(Offer.buy(Items.SEA_PICKLE, 4, 6));
                list.add(Offer.buy(SeaBlocks.GIANT_KELP.get(), 4, 6));
                ItemLike[] anemones = {SeaBlocks.PURPLE_SEA_ANEMONE.get(), SeaBlocks.RED_SEA_ANEMONE.get(), SeaBlocks.GREEN_SEA_ANEMONE.get(),
                        SeaBlocks.ORANGE_SEA_ANEMONE.get()};
                list.add(Offer.buy(anemones[random.nextInt(anemones.length)], 2, 6));
            }
            case SHELL_BUILDER -> {
                list.add(Offer.sell(Items.PRISMARINE_SHARD, 8, 6));
                list.add(Offer.sell(Items.CLAY_BALL, 16, 6));
                SeaWoods.SeaWood wood = SeaWoods.ALL.get(random.nextInt(SeaWoods.ALL.size()));
                list.add(Offer.buy(wood.log.get(), 6, 12));
                list.add(Offer.buy(wood.planks.base().get(), 12, 6));
                list.add(Offer.buy(wood.door.get(), 1, 12));
                list.add(Offer.buy(Items.SEA_LANTERN, 2, 18));
                list.add(Offer.buy(SeaBlocks.DEEP_SEA_LANTERN.get(), 2, 18));
                list.add(Offer.buy(Items.PRISMARINE_BRICKS, 8, 12));
                list.add(Offer.buy(Items.DARK_PRISMARINE, 8, 18));
                list.add(Offer.buy(SeaBlocks.TIDE_STONE_BRICKS.base().get(), 8, 12));
            }
            case TIDE_SINGER -> {
                list.add(Offer.sell(Items.INK_SAC, 8, 6));
                list.add(Offer.sell(Items.GLOW_INK_SAC, 4, 6));
                list.add(Offer.sell(SeaBlocks.GLOW_JELLY.get(), 4, 6));
                list.add(Offer.sell(Items.PRISMARINE_CRYSTALS, 6, 6));
                list.add(Offer.buy(PotionContents.createItemStack(Items.POTION, Potions.LONG_WATER_BREATHING), 18));
                list.add(Offer.buy(PotionContents.createItemStack(Items.POTION, Potions.LONG_NIGHT_VISION), 18));
                list.add(Offer.buy(PotionContents.createItemStack(Items.POTION, Potions.HEALING), 24));
                list.add(Offer.buy(randomBook(registries, random), 70));
                list.add(Offer.buy(randomBook(registries, random), 70));
                list.add(Offer.buy(new ItemStack(Items.CONDUIT), 180));
            }
            case GUARD -> {
                // Wachen handeln nicht
            }
        }
        if (profession != MermaidProfession.GUARD) {
            // die Erweiterung des Fischbeutels – Preis und Bild richten sich beim Handel nach dem Beutel des Spielers
            list.add(Offer.special(io.github.sirschrolli.kingdomsatwar.fishing.FishBag.UPGRADE_OFFER, Trading.bagUpgradeIcon(0), 12));
        }
        return list;
    }

    private static Holder<Enchantment> enchantment(HolderLookup.Provider registries, ResourceKey<Enchantment> key) {
        return registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    /** Ein verzaubertes Buch mit einer Verzauberung des Wassers. */
    private static ItemStack randomBook(HolderLookup.Provider registries, RandomSource random) {
        List<EnchantmentInstance> choices = List.of(
                new EnchantmentInstance(enchantment(registries, Enchantments.DEPTH_STRIDER), 3),
                new EnchantmentInstance(enchantment(registries, Enchantments.RESPIRATION), 3),
                new EnchantmentInstance(enchantment(registries, Enchantments.AQUA_AFFINITY), 1),
                new EnchantmentInstance(enchantment(registries, Enchantments.RIPTIDE), 3),
                new EnchantmentInstance(enchantment(registries, Enchantments.LOYALTY), 3),
                new EnchantmentInstance(enchantment(registries, Enchantments.IMPALING), 4),
                new EnchantmentInstance(enchantment(registries, Enchantments.LUCK_OF_THE_SEA), 3),
                new EnchantmentInstance(enchantment(registries, Enchantments.LURE), 3));
        return EnchantmentHelper.createBook(choices.get(random.nextInt(choices.size())));
    }
}
