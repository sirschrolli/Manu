package io.github.sirschrolli.kingdomsatwar.entity.villager;

import io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.Offer;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ItemLike;

/**
 * Was die Dorfbewohner kaufen und verkaufen – alles, was es früher bei den Dorfbewohnern gab, jetzt gegen Münzen (1
 * Silber = 10 Bronze, 1 Gold = 100 Bronze). Feste Waren hat jeder Händler seines Berufs; dazu kommen einige wechselnde
 * (verzauberte Bücher, verzauberte Ausrüstung, Tränke, Farben, Waren aus fernen Ländern), die jeder Bewohner nach
 * seinem eigenen Zufall wählt – und nach jeder Ladenpause neu.
 */
public final class VillageTrades {
    private VillageTrades() {}

    /**
     * Die Angebote (vor Rabatt) eines Bewohners dieses Berufs; {@code seed} bestimmt die wechselnden Waren, so bleibt
     * die Liste zwischen Anzeigen und Handeln gleich.
     */
    public static List<Offer> offers(VillageProfession profession, RegistryAccess registries, long seed) {
        RandomSource random = RandomSource.create(seed);
        List<Offer> list = new ArrayList<>();
        switch (profession) {
            case PEASANT -> {
            }
            case FARMER -> {
                list.add(Offer.sell(Items.WHEAT, 20, 5));
                list.add(Offer.sell(Items.POTATO, 24, 5));
                list.add(Offer.sell(Items.CARROT, 22, 5));
                list.add(Offer.sell(Items.BEETROOT, 15, 5));
                list.add(Offer.sell(Items.PUMPKIN, 6, 6));
                list.add(Offer.sell(Items.MELON, 4, 6));
                list.add(Offer.buy(Items.BREAD, 6, 6));
                list.add(Offer.buy(Items.APPLE, 4, 6));
                list.add(Offer.buy(Items.PUMPKIN_PIE, 4, 8));
                list.add(Offer.buy(Items.COOKIE, 18, 10));
                list.add(Offer.buy(Items.CAKE, 1, 10));
                list.add(Offer.buy(Items.GOLDEN_CARROT, 3, 25));
                list.add(Offer.buy(Items.GLISTERING_MELON_SLICE, 3, 30));
                list.add(Offer.buy(Items.BONE_MEAL, 12, 6));
                list.add(Offer.buy(Items.WHEAT_SEEDS, 16, 3));
                list.add(Offer.buy(Items.PUMPKIN_SEEDS, 8, 4));
                list.add(Offer.buy(Items.MELON_SEEDS, 8, 4));
                list.add(Offer.buy(ModItems.TOMATO_SEEDS.get(), 6, 5));
                list.add(Offer.buy(ModItems.CABBAGE_SEEDS.get(), 6, 5));
                list.add(Offer.buy(ModItems.BARLEY_SEEDS.get(), 6, 5));
                list.add(Offer.buy(ModItems.ONION.get(), 4, 5));
                list.add(Offer.buy(ModItems.GARLIC.get(), 4, 5));
            }
            case FISHER -> {
                list.add(Offer.sell(Items.STRING, 20, 8));
                list.add(Offer.sell(Items.COAL, 10, 8));
                list.add(Offer.sell(Items.COD, 15, 8));
                list.add(Offer.sell(Items.SALMON, 13, 9));
                list.add(Offer.sell(Items.TROPICAL_FISH, 6, 8));
                list.add(Offer.sell(Items.PUFFERFISH, 4, 8));
                list.add(Offer.buy(Items.COOKED_COD, 6, 8));
                list.add(Offer.buy(Items.COOKED_SALMON, 6, 10));
                list.add(Offer.buy(Items.COD_BUCKET, 1, 15));
                list.add(Offer.buy(Items.SALMON_BUCKET, 1, 15));
                list.add(Offer.buy(Items.CAMPFIRE, 1, 10));
                list.add(Offer.buy(Items.LILY_PAD, 4, 5));
                list.add(Offer.buy(Items.KELP, 8, 4));
                list.add(Offer.buy(boat(random), 1, 10));
                list.add(Offer.buy(enchanted(registries, random, Items.FISHING_ROD, 10), 45));
            }
            case SHEPHERD -> {
                list.add(Offer.sell(Items.WOOL.white(), 18, 8));
                list.add(Offer.sell(Items.WOOL.brown(), 18, 8));
                list.add(Offer.sell(Items.WOOL.black(), 18, 8));
                list.add(Offer.sell(Items.WOOL.gray(), 18, 8));
                list.add(Offer.sell(Items.DYE.pick(color(random)), 12, 6));
                list.add(Offer.buy(Items.SHEARS, 1, 12));
                for (int i = 0; i < 3; i++) {
                    list.add(Offer.buy(Items.WOOL.pick(color(random)), 8, 8));
                }
                list.add(Offer.buy(Items.CARPET.pick(color(random)), 8, 6));
                list.add(Offer.buy(Items.BED.pick(color(random)), 1, 15));
                list.add(Offer.buy(Items.BANNER.pick(color(random)), 1, 15));
                list.add(Offer.buy(Items.PAINTING, 3, 15));
                list.add(Offer.buy(Items.LOOM, 1, 8));
            }
            case FLETCHER -> {
                list.add(Offer.sell(Items.STICK, 32, 6));
                list.add(Offer.sell(Items.FLINT, 26, 8));
                list.add(Offer.sell(Items.STRING, 14, 8));
                list.add(Offer.sell(Items.FEATHER, 24, 8));
                list.add(Offer.sell(Items.TRIPWIRE_HOOK, 8, 8));
                list.add(Offer.buy(Items.ARROW, 16, 8));
                list.add(Offer.buy(Items.SPECTRAL_ARROW, 8, 15));
                list.add(Offer.buy(Items.BOW, 1, 15));
                list.add(Offer.buy(Items.CROSSBOW, 1, 25));
                list.add(Offer.buy(Items.TARGET, 1, 6));
                list.add(Offer.buy(enchanted(registries, random, Items.BOW, 15), 60));
                list.add(Offer.buy(enchanted(registries, random, Items.CROSSBOW, 15), 70));
                for (Holder<Potion> potion : pick(random, 3, Potions.SLOWNESS, Potions.POISON, Potions.WEAKNESS, Potions.HARMING, Potions.HEALING,
                        Potions.SWIFTNESS, Potions.STRENGTH, Potions.LEAPING, Potions.SLOW_FALLING, Potions.NIGHT_VISION)) {
                    ItemStack arrows = PotionContents.createItemStack(Items.TIPPED_ARROW, potion);
                    arrows.setCount(5);
                    list.add(Offer.buy(arrows, 15));
                }
            }
            case SCRIBE -> {
                list.add(Offer.sell(Items.PAPER, 24, 8));
                list.add(Offer.sell(Items.BOOK, 4, 8));
                list.add(Offer.sell(Items.INK_SAC, 5, 8));
                list.add(Offer.sell(Items.WRITABLE_BOOK, 2, 8));
                list.add(Offer.buy(Items.BOOKSHELF, 1, 10));
                list.add(Offer.buy(Items.LANTERN, 1, 4));
                list.add(Offer.buy(Items.GLASS, 4, 4));
                list.add(Offer.buy(Items.WRITABLE_BOOK, 1, 8));
                list.add(Offer.buy(Items.CLOCK, 1, 25));
                list.add(Offer.buy(Items.COMPASS, 1, 20));
                list.add(Offer.buy(Items.NAME_TAG, 1, 40));
                for (int i = 0; i < 4; i++) {
                    Offer book = book(registries, random);
                    if (book != null && list.stream().noneMatch(o -> ItemStack.isSameItemSameComponents(o.get(), book.get()))) {
                        list.add(book);
                    }
                }
            }
            case CARTOGRAPHER -> {
                list.add(Offer.sell(Items.PAPER, 24, 8));
                list.add(Offer.sell(Items.GLASS_PANE, 11, 8));
                list.add(Offer.sell(Items.COMPASS, 1, 10));
                list.add(Offer.buy(Items.MAP, 1, 7));
                list.add(Offer.special("village_map", Trading.named(Items.FILLED_MAP, "item.kingdomsatwar.map.village"), 40));
                list.add(Offer.special("seal_map", Trading.named(Items.FILLED_MAP, "item.kingdomsatwar.map.seal_circle"), 50));
                list.add(Offer.special("tower_map", Trading.named(Items.FILLED_MAP, "item.kingdomsatwar.map.mage_tower"), 80));
                list.add(Offer.special("nest_map", Trading.named(Items.FILLED_MAP, "item.kingdomsatwar.map.nest"), 120));
                list.add(Offer.buy(Items.ITEM_FRAME, 1, 7));
                list.add(Offer.buy(Items.GLOBE_BANNER_PATTERN, 1, 60));
                list.add(Offer.buy(Items.BANNER.pick(color(random)), 1, 15));
                list.add(Offer.buy(Items.SPYGLASS, 1, 25));
                list.add(Offer.buy(ModItems.DRAGON_COMPASS.get(), 1, 300));
            }
            case HERBALIST -> {
                list.add(Offer.sell(Items.ROTTEN_FLESH, 32, 8));
                list.add(Offer.sell(Items.GOLD_INGOT, 3, 10));
                list.add(Offer.sell(Items.RABBIT_FOOT, 2, 10));
                list.add(Offer.sell(Items.GLASS_BOTTLE, 9, 8));
                list.add(Offer.sell(Items.NETHER_WART, 22, 10));
                list.add(Offer.sell(Items.TURTLE_SCUTE, 4, 10));
                list.add(Offer.sell(Items.SPIDER_EYE, 8, 6));
                list.add(Offer.buy(Items.REDSTONE, 4, 8));
                list.add(Offer.buy(Items.LAPIS_LAZULI, 2, 8));
                list.add(Offer.buy(Items.GLOWSTONE, 1, 8));
                list.add(Offer.buy(Items.ENDER_PEARL, 1, 30));
                list.add(Offer.buy(Items.EXPERIENCE_BOTTLE, 1, 25));
                list.add(Offer.buy(Items.BREWING_STAND, 1, 20));
                for (Holder<Potion> potion : pick(random, 3, Potions.HEALING, Potions.REGENERATION, Potions.SWIFTNESS, Potions.FIRE_RESISTANCE,
                        Potions.NIGHT_VISION, Potions.WATER_BREATHING, Potions.STRENGTH, Potions.LEAPING, Potions.SLOW_FALLING,
                        Potions.INVISIBILITY)) {
                    list.add(Offer.buy(PotionContents.createItemStack(Items.POTION, potion), 15 + random.nextInt(11)));
                }
            }
            case ARMORER -> {
                list.add(Offer.sell(Items.COAL, 15, 8));
                list.add(Offer.sell(Items.IRON_INGOT, 4, 10));
                list.add(Offer.sell(Items.LAVA_BUCKET, 1, 8));
                list.add(Offer.sell(Items.DIAMOND, 1, 12));
                list.add(Offer.buy(Items.IRON_HELMET, 1, 25));
                list.add(Offer.buy(Items.IRON_CHESTPLATE, 1, 40));
                list.add(Offer.buy(Items.IRON_LEGGINGS, 1, 35));
                list.add(Offer.buy(Items.IRON_BOOTS, 1, 20));
                list.add(Offer.buy(Items.CHAINMAIL_HELMET, 1, 15));
                list.add(Offer.buy(Items.CHAINMAIL_CHESTPLATE, 1, 30));
                list.add(Offer.buy(Items.CHAINMAIL_LEGGINGS, 1, 25));
                list.add(Offer.buy(Items.CHAINMAIL_BOOTS, 1, 12));
                list.add(Offer.buy(Items.SHIELD, 1, 20));
                list.add(Offer.buy(Items.BELL, 1, 40));
                list.add(Offer.buy(ModItems.STEEL_HELMET.get(), 1, 50));
                list.add(Offer.buy(ModItems.STEEL_CHESTPLATE.get(), 1, 80));
                list.add(Offer.buy(ModItems.STEEL_LEGGINGS.get(), 1, 70));
                list.add(Offer.buy(ModItems.STEEL_BOOTS.get(), 1, 45));
                Item diamond = pickItem(random, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
                list.add(Offer.buy(enchanted(registries, random, diamond, 15), 260 + random.nextInt(140)));
            }
            case WEAPONSMITH -> {
                list.add(Offer.sell(Items.COAL, 15, 8));
                list.add(Offer.sell(Items.IRON_INGOT, 4, 10));
                list.add(Offer.sell(Items.FLINT, 24, 8));
                list.add(Offer.sell(Items.DIAMOND, 1, 12));
                list.add(Offer.buy(Items.IRON_SWORD, 1, 18));
                list.add(Offer.buy(Items.IRON_AXE, 1, 20));
                list.add(Offer.buy(Items.IRON_SPEAR, 1, 18));
                list.add(Offer.buy(ModItems.STEEL_SWORD.get(), 1, 40));
                list.add(Offer.buy(ModItems.STEEL_AXE.get(), 1, 40));
                list.add(Offer.buy(Items.BELL, 1, 40));
                list.add(Offer.buy(enchanted(registries, random, Items.IRON_SWORD, 10), 45));
                Item diamond = pickItem(random, Items.DIAMOND_SWORD, Items.DIAMOND_AXE);
                list.add(Offer.buy(enchanted(registries, random, diamond, 15), 260 + random.nextInt(100)));
            }
            case TOOLSMITH -> {
                list.add(Offer.sell(Items.COAL, 15, 8));
                list.add(Offer.sell(Items.IRON_INGOT, 4, 10));
                list.add(Offer.sell(Items.FLINT, 30, 8));
                list.add(Offer.sell(Items.DIAMOND, 1, 12));
                list.add(Offer.buy(Items.STONE_PICKAXE, 1, 4));
                list.add(Offer.buy(Items.STONE_AXE, 1, 4));
                list.add(Offer.buy(Items.STONE_SHOVEL, 1, 3));
                list.add(Offer.buy(Items.STONE_HOE, 1, 3));
                list.add(Offer.buy(Items.IRON_PICKAXE, 1, 25));
                list.add(Offer.buy(Items.IRON_SHOVEL, 1, 12));
                list.add(Offer.buy(Items.IRON_HOE, 1, 12));
                list.add(Offer.buy(ModItems.STEEL_PICKAXE.get(), 1, 45));
                list.add(Offer.buy(ModItems.STEEL_SHOVEL.get(), 1, 30));
                list.add(Offer.buy(Items.BELL, 1, 40));
                list.add(Offer.buy(enchanted(registries, random, Items.IRON_PICKAXE, 10), 45));
                Item diamond = pickItem(random, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_AXE, Items.DIAMOND_HOE);
                list.add(Offer.buy(enchanted(registries, random, diamond, 15), 220 + random.nextInt(100)));
            }
            case BUTCHER -> {
                list.add(Offer.sell(Items.CHICKEN, 14, 8));
                list.add(Offer.sell(Items.PORKCHOP, 7, 8));
                list.add(Offer.sell(Items.RABBIT, 4, 8));
                list.add(Offer.sell(Items.MUTTON, 7, 8));
                list.add(Offer.sell(Items.BEEF, 10, 8));
                list.add(Offer.sell(ModItems.VENISON.get(), 6, 8));
                list.add(Offer.sell(Items.DRIED_KELP_BLOCK, 10, 8));
                list.add(Offer.sell(Items.SWEET_BERRIES, 10, 8));
                list.add(Offer.buy(Items.COOKED_CHICKEN, 8, 8));
                list.add(Offer.buy(Items.COOKED_PORKCHOP, 5, 8));
                list.add(Offer.buy(Items.COOKED_MUTTON, 5, 8));
                list.add(Offer.buy(Items.COOKED_BEEF, 5, 9));
                list.add(Offer.buy(ModItems.COOKED_VENISON.get(), 4, 9));
                list.add(Offer.buy(Items.RABBIT_STEW, 1, 6));
                list.add(Offer.buy(ModItems.MEAT_PIE.get(), 2, 8));
            }
            case TANNER -> {
                list.add(Offer.sell(Items.LEATHER, 6, 8));
                list.add(Offer.sell(Items.FLINT, 26, 8));
                list.add(Offer.sell(Items.RABBIT_HIDE, 9, 8));
                list.add(Offer.sell(Items.TURTLE_SCUTE, 4, 10));
                list.add(Offer.sell(Items.ARMADILLO_SCUTE, 4, 10));
                list.add(Offer.buy(dyed(Items.LEATHER_HELMET, random), 6));
                list.add(Offer.buy(dyed(Items.LEATHER_CHESTPLATE, random), 10));
                list.add(Offer.buy(dyed(Items.LEATHER_LEGGINGS, random), 8));
                list.add(Offer.buy(dyed(Items.LEATHER_BOOTS, random), 6));
                list.add(Offer.buy(dyed(Items.LEATHER_HORSE_ARMOR, random), 15));
                list.add(Offer.buy(Items.SADDLE, 1, 50));
                list.add(Offer.buy(Items.BUNDLE, 1, 10));
                list.add(Offer.buy(Items.LEAD, 1, 8));
            }
            case MASON -> {
                list.add(Offer.sell(Items.CLAY_BALL, 10, 8));
                list.add(Offer.sell(Items.STONE, 20, 8));
                list.add(Offer.sell(Items.GRANITE, 16, 8));
                list.add(Offer.sell(Items.ANDESITE, 16, 8));
                list.add(Offer.sell(Items.DIORITE, 16, 8));
                list.add(Offer.sell(Items.QUARTZ, 12, 8));
                list.add(Offer.buy(Items.BRICK, 10, 8));
                list.add(Offer.buy(Items.CHISELED_STONE_BRICKS, 4, 8));
                list.add(Offer.buy(Items.POLISHED_ANDESITE, 4, 8));
                list.add(Offer.buy(Items.POLISHED_GRANITE, 4, 8));
                list.add(Offer.buy(Items.POLISHED_DIORITE, 4, 8));
                list.add(Offer.buy(Items.DRIPSTONE_BLOCK, 4, 8));
                list.add(Offer.buy(Items.QUARTZ_PILLAR, 1, 3));
                list.add(Offer.buy(Items.QUARTZ_BLOCK, 1, 3));
                for (DyeColor c : List.of(color(random), color(random))) {
                    list.add(Offer.buy(Items.DYED_TERRACOTTA.pick(c), 4, 6));
                    list.add(Offer.buy(Items.GLAZED_TERRACOTTA.pick(c), 4, 6));
                }
                list.add(Offer.buy(ModBlocks.MEDIEVAL_STONE.base().get(), 16, 8));
                list.add(Offer.buy(ModBlocks.ROUGH_FIELDSTONE.base().get(), 16, 8));
                list.add(Offer.buy(ModBlocks.RED_ROOF_TILES.base().get(), 16, 10));
                list.add(Offer.buy(ModBlocks.BROWN_ROOF_TILES.base().get(), 16, 10));
            }
            case BAKER -> {
                list.add(Offer.sell(Items.WHEAT, 20, 5));
                list.add(Offer.sell(Items.SUGAR, 10, 6));
                list.add(Offer.sell(Items.EGG, 12, 6));
                list.add(Offer.sell(ModItems.BARLEY.get(), 16, 6));
                list.add(Offer.buy(Items.BREAD, 6, 6));
                list.add(Offer.buy(ModItems.BREAD_ROLL.get(), 4, 4));
                list.add(Offer.buy(ModItems.GARLIC_BREAD.get(), 2, 6));
                list.add(Offer.buy(ModItems.APPLE_PIE.get(), 2, 7));
                list.add(Offer.buy(Items.PUMPKIN_PIE, 3, 6));
                list.add(Offer.buy(Items.COOKIE, 12, 6));
                list.add(Offer.buy(Items.CAKE, 1, 10));
            }
            case INNKEEPER -> list.addAll(Trading.baseOffers(io.github.sirschrolli.kingdomsatwar.economy.town.Profession.INNKEEPER));
            case MERCHANT -> {
                list.add(Offer.sell(Items.WHEAT, 20, 5));
                list.add(Offer.sell(Items.OAK_LOG, 16, 6));
                list.add(Offer.sell(Items.COBBLESTONE, 32, 4));
                list.add(Offer.sell(Items.LEATHER, 6, 8));
                list.add(Offer.sell(Items.EMERALD, 1, 6));
                list.add(Offer.buy(Items.EMERALD, 1, 12));
                list.add(Offer.buy(Items.TORCH, 16, 4));
                list.add(Offer.buy(Items.LANTERN, 2, 8));
                list.add(Offer.buy(Items.NAME_TAG, 1, 40));
                list.add(Offer.buy(Items.LEAD, 1, 8));
                // Waren aus fernen Ländern: jeder Händler hat andere
                for (ItemStack exotic : exotics(random, 6)) {
                    list.add(Offer.buy(exotic, 3 + random.nextInt(6) + exotic.getCount() / 4));
                }
            }
        }
        return list;
    }

    // =====================================================================================
    // Wechselnde Waren
    // =====================================================================================

    private static DyeColor color(RandomSource random) {
        DyeColor[] colors = DyeColor.values();
        return colors[random.nextInt(colors.length)];
    }

    @SafeVarargs
    private static List<Holder<Potion>> pick(RandomSource random, int count, Holder<Potion>... potions) {
        List<Holder<Potion>> pool = new ArrayList<>(List.of(potions));
        List<Holder<Potion>> chosen = new ArrayList<>();
        for (int i = 0; i < count && !pool.isEmpty(); i++) {
            chosen.add(pool.remove(random.nextInt(pool.size())));
        }
        return chosen;
    }

    private static Item pickItem(RandomSource random, Item... items) {
        return items[random.nextInt(items.length)];
    }

    private static Item boat(RandomSource random) {
        return pickItem(random, Items.OAK_BOAT, Items.SPRUCE_BOAT, Items.BIRCH_BOAT, Items.DARK_OAK_BOAT);
    }

    /** Ausrüstung mit Verzauberungen, wie sie Händler anbieten ({@code power}: Stärke der Verzauberung, etwa 5–20). */
    private static ItemStack enchanted(RegistryAccess registries, RandomSource random, Item item, int power) {
        var tradeable = registries.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.ON_TRADED_EQUIPMENT);
        return EnchantmentHelper.enchantItem(random, new ItemStack(item), power + random.nextInt(6), registries, tradeable);
    }

    /** Ein verzaubertes Buch (alles, was Händler kennen; Schätze wie Reparatur kosten das Doppelte). */
    private static Offer book(RegistryAccess registries, RandomSource random) {
        var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        Optional<Holder<Enchantment>> found = enchantments.getRandomElementOf(EnchantmentTags.TRADEABLE, random);
        if (found.isEmpty()) {
            return null;
        }
        Holder<Enchantment> enchantment = found.get();
        int min = enchantment.value().getMinLevel();
        int max = enchantment.value().getMaxLevel();
        int level = min + random.nextInt(max - min + 1);
        int price = 20 + level * 25 + random.nextInt(level * 15 + 1);
        if (enchantment.is(EnchantmentTags.DOUBLE_TRADE_PRICE)) {
            price *= 2;
        }
        return Offer.buy(EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, level)), Math.min(price, 400));
    }

    /** Lederzeug in einer zufälligen Farbe. */
    private static ItemStack dyed(Item item, RandomSource random) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color(random).getTextureDiffuseColor() & 0xFFFFFF));
        return stack;
    }

    /** Waren aus fernen Ländern (wie beim fahrenden Händler früher). */
    private static List<ItemStack> exotics(RandomSource random, int count) {
        List<ItemStack> pool = new ArrayList<>(List.of(
                stack(Items.COCOA_BEANS, 3), stack(Items.SUGAR_CANE, 3), stack(Items.CACTUS, 2), stack(Items.BAMBOO, 4), stack(Items.VINE, 3),
                stack(Items.LILY_PAD, 2), stack(Items.SLIME_BALL, 4), stack(Items.PACKED_ICE, 2), stack(Items.BLUE_ICE, 1),
                stack(Items.GUNPOWDER, 2), stack(Items.PODZOL, 3), stack(Items.RED_SAND, 4), stack(Items.NAUTILUS_SHELL, 1),
                stack(Items.SEA_PICKLE, 2), stack(Items.GLOW_BERRIES, 3), stack(Items.MOSS_BLOCK, 2), stack(Items.POINTED_DRIPSTONE, 2),
                stack(Items.SMALL_DRIPLEAF, 2), stack(Items.GLOW_LICHEN, 3), stack(Items.PUFFERFISH_BUCKET, 1),
                stack(Items.TROPICAL_FISH_BUCKET, 1), stack(Items.OAK_SAPLING, 1), stack(Items.SPRUCE_SAPLING, 1),
                stack(Items.BIRCH_SAPLING, 1), stack(Items.JUNGLE_SAPLING, 1), stack(Items.ACACIA_SAPLING, 1),
                stack(Items.DARK_OAK_SAPLING, 1), stack(Items.CHERRY_SAPLING, 1), stack(Items.PALE_OAK_SAPLING, 1),
                stack(Items.MANGROVE_PROPAGULE, 1), stack(Items.BROWN_MUSHROOM, 2), stack(Items.RED_MUSHROOM, 2),
                stack(Items.FERN, 2), stack(Items.DYE.pick(color(random)), 3), stack(Items.DYE.pick(color(random)), 3)));
        List<ItemStack> chosen = new ArrayList<>();
        for (int i = 0; i < count && !pool.isEmpty(); i++) {
            chosen.add(pool.remove(random.nextInt(pool.size())));
        }
        return chosen;
    }

    private static ItemStack stack(ItemLike item, int count) {
        return new ItemStack(item, count);
    }
}
