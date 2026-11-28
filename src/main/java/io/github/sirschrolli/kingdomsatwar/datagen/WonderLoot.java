package io.github.sirschrolli.kingdomsatwar.datagen;

import static io.github.sirschrolli.kingdomsatwar.datagen.ModChestLootProvider.coins;
import static io.github.sirschrolli.kingdomsatwar.datagen.ModChestLootProvider.crystals;
import static io.github.sirschrolli.kingdomsatwar.datagen.ModChestLootProvider.item;
import static io.github.sirschrolli.kingdomsatwar.datagen.ModChestLootProvider.pool;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.Cuisine;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

/**
 * Die Truhen der Bauwerke von 2.0: in jedem, was dort hingehört – Käse in der Sennhütte, Rum und Gold bei den Schmugglern,
 * Honig in der Imkerei, Grabbeigaben im Hünengrab, Tränke im Alchemistenturm, Fleisch und Felle in der Jagdhütte. Herzkristalle
 * nur selten, Münzen karg.
 */
final class WonderLoot {
    private final LootTableSubProvider.Context output;
    private final HolderGetter<Enchantment> enchantments;

    private WonderLoot(LootTableSubProvider.Context output, HolderGetter<Enchantment> enchantments) {
        this.output = output;
        this.enchantments = enchantments;
    }

    static void register(LootTableSubProvider.Context output, HolderGetter<Enchantment> enchantments) {
        WonderLoot loot = new WonderLoot(output, enchantments);
        loot.overworld();
    }

    private void accept(String name, LootTable.Builder table) {
        this.output.accept(ModLootTables.wonder(name), table);
    }

    /** Mit {@code chance} ein verzaubertes Buch. */
    private LootPool.Builder book(float chance) {
        return LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(Items.BOOK).apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                .when(LootItemRandomChanceCondition.randomChance(chance));
    }

    private static LootItem.Builder<?> potion(Holder<Potion> potion, int weight) {
        return LootItem.lootTableItem(Items.POTION).setWeight(weight).apply(SetPotionFunction.setPotion(potion));
    }

    private void overworld() {
        // Sennhütte: der Käsekeller und die Speisekammer
        this.accept("senn_hut", coins(LootTable.lootTable().withPool(crystals(5, 5)), 2, 7, 0, 2, 0.05f)
                .withPool(pool(1, 2, item(Cuisine.CHEESE_WHEEL.get(), 1, 1, 2)))
                .withPool(pool(3, 6, item(Cuisine.CHEESE.get(), 8, 2, 6), item(Cuisine.PRETZEL.get(), 6, 2, 4), item(Cuisine.SAUSAGE.get(), 5, 1, 3),
                        item(Cuisine.CHEESE_SPAETZLE.get(), 2, 1, 1), item(Items.MILK_BUCKET, 2, 1, 1), item(Items.LEATHER, 4, 1, 3),
                        item(Items.WOOL.pick(DyeColor.WHITE), 3, 1, 3), item(Items.BELL, 1, 1, 1), item(Items.EMERALD, 2, 1, 2),
                        item(Items.SPRUCE_SAPLING, 2, 1, 2))));
        this.accept("senn_hut_pantry", LootTable.lootTable()
                .withPool(pool(3, 6, item(Cuisine.FLOUR.get(), 6, 2, 6), item(Items.BREAD, 5, 1, 3), item(Cuisine.RAW_SAUSAGE.get(), 4, 1, 3),
                        item(Items.EGG, 4, 1, 4), item(Items.SUGAR, 3, 1, 3), item(Cuisine.CHEESE.get(), 5, 1, 4), item(ModItems.ONION.get(), 3, 1, 3),
                        item(Items.POTATO, 4, 2, 5))));
        // Schmugglerhöhle: das Lager und der Schatz
        this.accept("smuggler_cove", coins(LootTable.lootTable().withPool(crystals(8, 6)), 3, 10, 1, 3, 0.15f)
                .withPool(pool(3, 6, item(Cuisine.RUM.get(), 8, 1, 4), item(ModItems.WINE.get(), 4, 1, 2), item(Items.GUNPOWDER, 5, 2, 5),
                        item(Items.GOLD_NUGGET, 5, 3, 9), item(Items.STRING, 4, 2, 6), item(Items.SUGAR, 4, 2, 5), item(Items.COCOA_BEANS, 3, 2, 5),
                        item(Items.LEAD, 2, 1, 2), item(Items.COMPASS, 2, 1, 1), item(Items.MAP, 2, 1, 1), item(Items.SPYGLASS, 1, 1, 1),
                        item(Cuisine.HARDTACK.get(), 4, 2, 5))));
        this.accept("smuggler_cove_hoard", coins(LootTable.lootTable().withPool(crystals(12, 10)), 6, 16, 3, 7, 0.4f)
                .withPool(pool(3, 5, item(Items.GOLD_INGOT, 6, 2, 5), item(Items.EMERALD, 4, 1, 4), item(ModItems.GEMSTONE.get(), 3, 1, 2),
                        item(Items.DIAMOND, 1, 1, 1), item(Items.HEART_OF_THE_SEA, 1, 1, 1), item(Cuisine.RUM.get(), 4, 1, 3),
                        item(Items.NAUTILUS_SHELL, 2, 1, 1), item(Items.GOLDEN_APPLE, 1, 1, 1)))
                .withPool(this.book(0.5f)));
        // Imkerei
        this.accept("apiary", coins(LootTable.lootTable().withPool(crystals(4, 6)), 1, 6, 0, 2, 0.05f)
                .withPool(pool(3, 6, item(Items.HONEY_BOTTLE, 8, 1, 3), item(Items.HONEYCOMB, 8, 2, 6), item(ModItems.MEAD.get(), 5, 1, 2),
                        item(Cuisine.GINGERBREAD.get(), 5, 2, 4), item(Items.CANDLE, 4, 1, 3), item(Items.DYED_CANDLE.pick(DyeColor.YELLOW), 4, 1, 3),
                        item(Items.GLASS_BOTTLE, 4, 1, 4), item(Items.SUGAR, 3, 1, 3), item(Items.CORNFLOWER, 3, 1, 3), item(Items.ALLIUM, 3, 1, 3),
                        item(Items.SUNFLOWER, 2, 1, 2), item(Items.HONEY_BLOCK, 1, 1, 1))));
        // Hünengrab: was man den Toten mitgab, und der Schatz des Fürsten
        this.accept("barrow", coins(LootTable.lootTable().withPool(crystals(6, 6)), 1, 6, 0, 2, 0.05f)
                .withPool(pool(2, 5, item(Items.BONE, 6, 2, 6), item(Items.ARROW, 4, 3, 9), item(Items.IRON_NUGGET, 4, 2, 6),
                        item(Items.CANDLE, 3, 1, 3), item(Items.SKELETON_SKULL, 1, 1, 1), item(Items.GOLD_NUGGET, 4, 2, 6),
                        item(Items.ARCHER_POTTERY_SHERD, 1, 1, 1), item(Items.SKULL_POTTERY_SHERD, 1, 1, 1), item(Items.FLINT, 3, 1, 3)))
                .withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.IRON_SWORD).apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.2f, 0.6f))))
                        .add(LootItem.lootTableItem(Items.IRON_AXE).apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.2f, 0.6f))))
                        .add(EmptyLootItem.emptyItem().setWeight(3))));
        this.accept("barrow_hoard", coins(LootTable.lootTable().withPool(crystals(14, 10)), 6, 14, 2, 6, 0.4f)
                .withPool(pool(3, 5, item(Items.GOLD_INGOT, 6, 2, 5), item(Items.GOLD_NUGGET, 5, 4, 10), item(ModItems.GEMSTONE.get(), 3, 1, 2),
                        item(Items.AMETHYST_SHARD, 3, 2, 5), item(Items.EMERALD, 3, 1, 3), item(Items.GOLDEN_HELMET, 1, 1, 1),
                        item(Items.GOLDEN_APPLE, 1, 1, 1), item(Items.DIAMOND, 1, 1, 1)))
                .withPool(this.book(0.5f)));
        // Alchemistenturm: Zutaten im Labor, Tränke und Seltenes in der Truhe der Alchemistin
        this.accept("alchemist_tower", LootTable.lootTable()
                .withPool(pool(3, 6, item(Items.GLASS_BOTTLE, 6, 1, 4), item(Items.GLOWSTONE_DUST, 5, 1, 4), item(Items.REDSTONE, 5, 1, 4),
                        item(Items.NETHER_WART, 4, 1, 3), item(Items.SUGAR, 4, 1, 3), item(Items.SPIDER_EYE, 4, 1, 2),
                        item(Items.FERMENTED_SPIDER_EYE, 2, 1, 1), item(Items.BLAZE_POWDER, 2, 1, 2), item(Items.GUNPOWDER, 3, 1, 2),
                        item(Items.RABBIT_FOOT, 1, 1, 1), item(Items.PHANTOM_MEMBRANE, 2, 1, 1), item(Items.AMETHYST_SHARD, 3, 1, 3),
                        item(ModItems.WISP_DUST.get(), 2, 1, 2)))
                .withPool(LootPool.lootPool().setRolls(ContextIntProviders.between(1, 2))
                        .add(potion(Potions.HEALING, 3)).add(potion(Potions.STRENGTH, 2)).add(potion(Potions.SWIFTNESS, 2))
                        .add(potion(Potions.FIRE_RESISTANCE, 2)).add(potion(Potions.NIGHT_VISION, 2)).add(EmptyLootItem.emptyItem().setWeight(3))));
        this.accept("alchemist_hoard", coins(LootTable.lootTable().withPool(crystals(10, 16)), 3, 9, 1, 4, 0.25f)
                .withPool(pool(2, 4, item(Items.ENDER_PEARL, 4, 1, 2), item(Items.GHAST_TEAR, 2, 1, 1), item(Items.GOLDEN_CARROT, 3, 1, 3),
                        item(Items.GLISTERING_MELON_SLICE, 3, 1, 2), item(ModItems.GEMSTONE.get(), 3, 1, 2), item(Items.EXPERIENCE_BOTTLE, 4, 1, 4),
                        item(Items.BREWING_STAND, 1, 1, 1)))
                .withPool(LootPool.lootPool().setRolls(ContextIntProviders.between(1, 3))
                        .add(potion(Potions.STRONG_HEALING, 2)).add(potion(Potions.LONG_STRENGTH, 2)).add(potion(Potions.REGENERATION, 2))
                        .add(potion(Potions.LONG_INVISIBILITY, 1)).add(potion(Potions.TURTLE_MASTER, 1)))
                .withPool(this.book(0.6f)));
        // Jagdhütte und Räucherkammer
        this.accept("hunting_lodge", coins(LootTable.lootTable().withPool(crystals(5, 5)), 2, 7, 0, 2, 0.05f)
                .withPool(pool(3, 6, item(Items.LEATHER, 6, 2, 5), item(Items.RABBIT_HIDE, 4, 1, 4), item(Items.ARROW, 6, 4, 12),
                        item(Items.FEATHER, 4, 2, 6), item(Items.STRING, 4, 1, 4), item(ModItems.COOKED_VENISON.get(), 4, 1, 3),
                        item(Cuisine.SAUSAGE.get(), 3, 1, 3), item(Items.SPYGLASS, 1, 1, 1), item(Items.CROSSBOW, 1, 1, 1),
                        item(Items.LEAD, 2, 1, 1), item(Items.WOOL.pick(DyeColor.BROWN), 3, 1, 2)))
                .withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.BOW).apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.3f, 0.8f))))
                        .add(EmptyLootItem.emptyItem().setWeight(2))));
        this.accept("hunting_lodge_smokehouse", LootTable.lootTable()
                .withPool(pool(3, 6, item(ModItems.COOKED_VENISON.get(), 6, 1, 4), item(Cuisine.SAUSAGE.get(), 6, 2, 5),
                        item(Items.COOKED_PORKCHOP, 4, 1, 3), item(Items.COOKED_MUTTON, 4, 1, 3), item(Cuisine.RAW_SAUSAGE.get(), 3, 1, 3),
                        item(Items.CHARCOAL, 4, 2, 6), item(ModItems.VENISON.get(), 3, 1, 3))));
    }
}
