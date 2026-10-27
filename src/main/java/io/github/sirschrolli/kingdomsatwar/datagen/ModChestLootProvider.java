package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

/**
 * Truhen-Beute der Bauwerke: Vorräte, Stahl und Edelsteine aus der Burgruine, mit etwas Glück Runen, Waffen,
 * ein Drachenei-Fragment oder gar die Königskrone.
 */
public class ModChestLootProvider implements LootTableSubProvider {
    private final LootTableSubProvider.Context output;
    private final HolderGetter<Enchantment> enchantments;

    public ModChestLootProvider(LootTableSubProvider.Context output) {
        this.output = output;
        this.enchantments = output.lookup(Registries.ENCHANTMENT);
    }

    /**
     * Münzen in einer Truhe: Bronze und Silber (je ein eigener Pool), mit etwas Glück eine Goldmünze. Je nach Truhe
     * klein (Hütten), mittel (Ruinen, Tempel) oder groß (Gruft, Zitadelle, Grotte).
     */
    private static LootTable.Builder coins(LootTable.Builder table, int bronzeMin, int bronzeMax, int silverMin, int silverMax,
            float goldChance) {
        table.withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                .add(item(ModItems.BRONZE_COIN.get(), 1, bronzeMin, bronzeMax)));
        if (silverMax > 0) {
            table.withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                    .add(item(ModItems.SILVER_COIN.get(), 1, silverMin, silverMax)));
        }
        if (goldChance > 0.0f) {
            table.withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                    .add(LootItem.lootTableItem(ModItems.GOLD_COIN.get()))
                    .when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.randomChance(goldChance)));
        }
        return table;
    }

    private static UniformContainerBase.Builder<?> item(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item).setWeight(weight).apply(SetItemCountFunction.setCount(ContextIntProviders.between(min, max)));
    }

    /** Ein Pool mit {@code min}–{@code max} Ziehungen aus den Einträgen. */
    @SafeVarargs
    private static LootPool.Builder pool(int min, int max, UniformContainerBase.Builder<?>... entries) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(ContextIntProviders.between(min, max));
        for (UniformContainerBase.Builder<?> entry : entries) {
            pool.add(entry);
        }
        return pool;
    }

    /**
     * Die Truhen der Dörfer: in jedem Haus das, was dort gebraucht wird – Brot und Saatgut bei den Bauern, Eisen und
     * Werkzeug in der Schmiede, Bier und Wein im Gasthaus, Papier und manchmal ein verzaubertes Buch in der
     * Schreibstube –, dazu ein paar Münzen (die Dörfler bezahlen nicht mehr mit Smaragden).
     */
    private void villages() {
        this.output.accept(ModLootTables.VILLAGE_HOUSE, coins(LootTable.lootTable(), 1, 6, 0, 1, 0.0f)
                .withPool(pool(3, 6,
                        item(ModItems.BREAD_ROLL.get(), 10, 1, 4), item(Items.BREAD, 8, 1, 3), item(Items.APPLE, 8, 1, 4),
                        item(Items.POTATO, 8, 1, 5), item(Items.CARROT, 6, 1, 5), item(Items.WHEAT_SEEDS, 6, 2, 6),
                        item(ModItems.TOMATO_SEEDS.get(), 4, 1, 3), item(ModItems.CABBAGE_SEEDS.get(), 4, 1, 3),
                        item(Items.TORCH, 6, 2, 6), item(Items.STRING, 4, 1, 3), item(Items.FEATHER, 4, 1, 3),
                        item(Items.WOOL.white(), 4, 1, 2), item(Items.BOOK, 2, 1, 1), item(Items.OAK_SAPLING, 3, 1, 2),
                        item(Items.DANDELION, 3, 1, 2), item(Items.POPPY, 3, 1, 2))));
        this.output.accept(ModLootTables.VILLAGE_SMITHY, coins(LootTable.lootTable(), 2, 8, 1, 3, 0.05f)
                .withPool(pool(3, 6,
                        item(Items.IRON_INGOT, 10, 1, 5), item(Items.IRON_NUGGET, 8, 3, 9), item(ModItems.STEEL_INGOT.get(), 6, 1, 3),
                        item(Items.COAL, 8, 2, 8), item(ModItems.CARBON.get(), 6, 2, 6), item(Items.GOLD_INGOT, 3, 1, 3),
                        item(Items.IRON_SWORD, 3, 1, 1), item(Items.IRON_PICKAXE, 3, 1, 1), item(Items.IRON_SHOVEL, 3, 1, 1),
                        item(Items.IRON_HELMET, 2, 1, 1), item(Items.IRON_CHESTPLATE, 2, 1, 1), item(Items.CHAINMAIL_BOOTS, 2, 1, 1),
                        item(Items.SHIELD, 2, 1, 1), item(Items.OBSIDIAN, 3, 1, 4), item(Items.BREAD, 4, 1, 3),
                        item(Items.DIAMOND, 1, 1, 2), item(Items.SADDLE, 1, 1, 1), item(Items.IRON_HORSE_ARMOR, 1, 1, 1))));
        this.output.accept(ModLootTables.VILLAGE_BAKERY, coins(LootTable.lootTable(), 1, 6, 0, 1, 0.0f)
                .withPool(pool(3, 6,
                        item(Items.BREAD, 10, 1, 4), item(ModItems.BREAD_ROLL.get(), 10, 2, 6), item(Items.WHEAT, 8, 2, 8),
                        item(Items.SUGAR, 6, 1, 4), item(Items.EGG, 5, 1, 3), item(Items.COOKIE, 6, 2, 8), item(Items.PUMPKIN_PIE, 4, 1, 2),
                        item(ModItems.APPLE_PIE.get(), 4, 1, 2), item(ModItems.GARLIC_BREAD.get(), 4, 1, 3), item(Items.CAKE, 1, 1, 1),
                        item(Items.HONEY_BOTTLE, 2, 1, 1), item(ModItems.BARLEY.get(), 4, 1, 4))));
        this.output.accept(ModLootTables.VILLAGE_TAVERN, coins(LootTable.lootTable(), 3, 9, 1, 4, 0.08f)
                .withPool(pool(3, 6,
                        item(ModItems.ALE.get(), 10, 1, 3), item(ModItems.WINE.get(), 6, 1, 2), item(ModItems.MEAD.get(), 4, 1, 2),
                        item(ModItems.MEAT_PIE.get(), 6, 1, 3), item(ModItems.COOKED_VENISON.get(), 6, 1, 3),
                        item(ModItems.VEGETABLE_STEW.get(), 4, 1, 1), item(ModItems.BREAD_ROLL.get(), 8, 2, 5),
                        item(Items.CANDLE, 4, 1, 3), item(ModItems.HOPS.get(), 4, 2, 5), item(ModItems.GRAPES.get(), 3, 2, 5),
                        item(Items.MUSIC_DISC_CAT, 1, 1, 1), item(Items.GLASS_BOTTLE, 4, 1, 4))));
        this.output.accept(ModLootTables.VILLAGE_SCRIPTORIUM, coins(LootTable.lootTable(), 2, 6, 0, 2, 0.0f)
                .withPool(pool(3, 5,
                        item(Items.PAPER, 10, 2, 8), item(Items.BOOK, 8, 1, 3), item(Items.WRITABLE_BOOK, 3, 1, 1), item(Items.INK_SAC, 6, 1, 4),
                        item(Items.FEATHER, 6, 1, 4), item(Items.CANDLE, 4, 1, 3), item(Items.COMPASS, 2, 1, 1), item(Items.CLOCK, 1, 1, 1),
                        item(Items.GLOW_INK_SAC, 2, 1, 2)))
                .withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(1).apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                        .add(EmptyLootItem.emptyItem().setWeight(3))));
        this.output.accept(ModLootTables.VILLAGE_MAP_HOUSE, coins(LootTable.lootTable(), 1, 6, 0, 2, 0.0f)
                .withPool(pool(2, 5,
                        item(Items.MAP, 8, 1, 3), item(Items.PAPER, 10, 2, 8), item(Items.COMPASS, 4, 1, 1), item(Items.GLASS_PANE, 5, 1, 4),
                        item(Items.SPYGLASS, 1, 1, 1), item(Items.FEATHER, 4, 1, 3), item(Items.INK_SAC, 4, 1, 2))));
        this.output.accept(ModLootTables.VILLAGE_HERB_HOUSE, coins(LootTable.lootTable(), 1, 6, 0, 2, 0.0f)
                .withPool(pool(3, 6,
                        item(Items.GLASS_BOTTLE, 8, 1, 4), item(Items.REDSTONE, 6, 1, 4), item(Items.GLOWSTONE_DUST, 4, 1, 3),
                        item(Items.LAPIS_LAZULI, 4, 1, 4), item(Items.SWEET_BERRIES, 6, 2, 6), item(Items.NETHER_WART, 2, 1, 2),
                        item(ModBlocks.LAVENDER.get(), 5, 1, 3), item(ModBlocks.GARDEN_ROSE.get(), 4, 1, 2), item(ModItems.WISP_DUST.get(), 3, 1, 2),
                        item(Items.SPIDER_EYE, 3, 1, 2), item(Items.BLAZE_POWDER, 1, 1, 1)))
                .withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.POTION).setWeight(2).apply(net.minecraft.world.level.storage.loot.functions.SetPotionFunction
                                .setPotion(net.minecraft.world.item.alchemy.Potions.HEALING)))
                        .add(LootItem.lootTableItem(Items.POTION).setWeight(2).apply(net.minecraft.world.level.storage.loot.functions.SetPotionFunction
                                .setPotion(net.minecraft.world.item.alchemy.Potions.REGENERATION)))
                        .add(EmptyLootItem.emptyItem().setWeight(3))));
        this.output.accept(ModLootTables.VILLAGE_BUTCHERY, coins(LootTable.lootTable(), 1, 6, 0, 1, 0.0f)
                .withPool(pool(3, 6,
                        item(Items.BEEF, 8, 1, 3), item(Items.PORKCHOP, 8, 1, 3), item(Items.MUTTON, 6, 1, 3), item(Items.CHICKEN, 6, 1, 3),
                        item(Items.COOKED_BEEF, 4, 1, 2), item(Items.COOKED_PORKCHOP, 4, 1, 2), item(ModItems.VENISON.get(), 4, 1, 3),
                        item(Items.LEATHER, 5, 1, 3), item(Items.COAL, 5, 1, 4), item(Items.WHEAT, 4, 1, 4), item(Items.RABBIT, 3, 1, 2))));
        this.output.accept(ModLootTables.VILLAGE_TANNERY, coins(LootTable.lootTable(), 1, 6, 0, 2, 0.0f)
                .withPool(pool(2, 5,
                        item(Items.LEATHER, 10, 1, 4), item(Items.RABBIT_HIDE, 6, 1, 3), item(Items.LEATHER_CHESTPLATE, 2, 1, 1),
                        item(Items.LEATHER_BOOTS, 2, 1, 1), item(Items.LEATHER_HELMET, 2, 1, 1), item(Items.LEATHER_LEGGINGS, 2, 1, 1),
                        item(Items.SADDLE, 1, 1, 1), item(Items.STRING, 5, 1, 4), item(Items.BREAD, 4, 1, 2))));
        this.output.accept(ModLootTables.VILLAGE_MASONRY, coins(LootTable.lootTable(), 1, 6, 0, 2, 0.0f)
                .withPool(pool(2, 5,
                        item(Items.CLAY_BALL, 8, 2, 6), item(Items.STONE_BRICKS, 6, 2, 6), item(Items.TERRACOTTA, 5, 1, 4),
                        item(Items.SMOOTH_STONE, 5, 1, 4), item(Items.CHISELED_STONE_BRICKS, 3, 1, 2), item(Items.QUARTZ, 2, 1, 3),
                        item(Items.FLOWER_POT, 3, 1, 1), item(Items.BRICK, 6, 2, 6), item(Items.IRON_PICKAXE, 1, 1, 1))));
        this.output.accept(ModLootTables.VILLAGE_BOWYER, coins(LootTable.lootTable(), 1, 6, 0, 2, 0.0f)
                .withPool(pool(3, 5,
                        item(Items.ARROW, 10, 4, 14), item(Items.STICK, 8, 2, 8), item(Items.FEATHER, 8, 2, 6), item(Items.FLINT, 6, 1, 4),
                        item(Items.STRING, 6, 1, 4), item(Items.BOW, 2, 1, 1), item(Items.CROSSBOW, 1, 1, 1), item(Items.TARGET, 1, 1, 1))));
        this.output.accept(ModLootTables.VILLAGE_CHAPEL, coins(LootTable.lootTable(), 3, 9, 1, 4, 0.1f)
                .withPool(pool(2, 5,
                        item(Items.CANDLE, 10, 1, 4), item(Items.REDSTONE, 6, 1, 4), item(Items.LAPIS_LAZULI, 6, 1, 4),
                        item(Items.GOLD_NUGGET, 6, 2, 6), item(Items.BREAD, 4, 1, 3), item(Items.BOOK, 4, 1, 2),
                        item(Items.EXPERIENCE_BOTTLE, 2, 1, 2))));
        this.output.accept(ModLootTables.VILLAGE_FARM, coins(LootTable.lootTable(), 1, 5, 0, 1, 0.0f)
                .withPool(pool(3, 6,
                        item(Items.WHEAT_SEEDS, 10, 2, 8), item(Items.WHEAT, 8, 2, 8), item(Items.POTATO, 8, 1, 6), item(Items.CARROT, 8, 1, 6),
                        item(Items.BEETROOT_SEEDS, 6, 2, 6), item(Items.BONE_MEAL, 6, 1, 6), item(ModItems.TOMATO_SEEDS.get(), 4, 1, 3),
                        item(ModItems.CABBAGE_SEEDS.get(), 4, 1, 3), item(ModItems.BARLEY_SEEDS.get(), 4, 1, 3), item(ModItems.ONION.get(), 3, 1, 3),
                        item(ModItems.GARLIC.get(), 3, 1, 3), item(Items.PUMPKIN_SEEDS, 3, 1, 3), item(Items.MELON_SEEDS, 3, 1, 3),
                        item(Items.IRON_HOE, 1, 1, 1), item(Items.SHEARS, 1, 1, 1))));
        this.output.accept(ModLootTables.VILLAGE_SHEPHERD, coins(LootTable.lootTable(), 1, 5, 0, 1, 0.0f)
                .withPool(pool(2, 5,
                        item(Items.WOOL.white(), 10, 1, 6), item(Items.WOOL.gray(), 4, 1, 3), item(Items.WOOL.brown(), 4, 1, 3),
                        item(Items.WOOL.black(), 3, 1, 3), item(Items.SHEARS, 2, 1, 1), item(Items.WHEAT, 6, 1, 4),
                        item(Items.DYE.white(), 3, 1, 2), item(Items.DYE.red(), 2, 1, 2), item(Items.DYE.blue(), 2, 1, 2))));
        this.output.accept(ModLootTables.VILLAGE_FISHER, coins(LootTable.lootTable(), 1, 5, 0, 1, 0.0f)
                .withPool(pool(2, 5,
                        item(Items.COD, 10, 1, 5), item(Items.SALMON, 8, 1, 4), item(Items.COOKED_COD, 6, 1, 3), item(Items.STRING, 6, 1, 4),
                        item(Items.FISHING_ROD, 3, 1, 1), item(Items.KELP, 4, 1, 4), item(Items.LILY_PAD, 3, 1, 2),
                        item(Items.INK_SAC, 3, 1, 3), item(Items.NAUTILUS_SHELL, 1, 1, 1), item(Items.TROPICAL_FISH, 2, 1, 2))));
        this.output.accept(ModLootTables.VILLAGE_WATCHTOWER, coins(LootTable.lootTable(), 2, 7, 0, 2, 0.0f)
                .withPool(pool(2, 5,
                        item(Items.ARROW, 10, 4, 12), item(Items.BREAD, 8, 1, 3), item(Items.TORCH, 6, 2, 6), item(Items.IRON_INGOT, 4, 1, 3),
                        item(Items.SHIELD, 2, 1, 1), item(ModItems.STEEL_SWORD.get(), 1, 1, 1), item(ModItems.ALE.get(), 4, 1, 2),
                        item(Items.SPYGLASS, 1, 1, 1))));
    }

    @Override
    public void run() {
        this.mageTowers();
        this.villages();
        LootPool.Builder treasure = LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(ModItems.STEEL_SWORD.get()).setWeight(5)
                        .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.3f, 0.9f))))
                .add(LootItem.lootTableItem(ModItems.STEEL_HELMET.get()).setWeight(4)
                        .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.3f, 0.9f))))
                .add(LootItem.lootTableItem(Items.BOOK).setWeight(5).apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                .add(LootItem.lootTableItem(ModItems.DRAGON_COMPASS.get()).setWeight(2))
                .add(LootItem.lootTableItem(ModItems.GEMSTONE_CROWN.get()).setWeight(1))
                .add(LootItem.lootTableItem(ModItems.AETHERIT_STAFF.get()).setWeight(1)
                        .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.4f, 0.9f))))
                .add(EmptyLootItem.emptyItem().setWeight(8));
        this.output.accept(ModLootTables.CASTLE_RUIN_CHEST, coins(LootTable.lootTable(), 3, 9, 1, 3, 0.12f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(4, 7))
                        .add(item(ModItems.BREAD_ROLL.get(), 10, 1, 4))
                        .add(item(ModItems.COOKED_VENISON.get(), 6, 1, 3))
                        .add(item(ModItems.ALE.get(), 5, 1, 2))
                        .add(item(ModItems.WINE.get(), 3, 1, 1))
                        .add(item(Items.ARROW, 6, 4, 12))
                        .add(item(Items.IRON_INGOT, 8, 1, 4))
                        .add(item(ModItems.STEEL_INGOT.get(), 7, 1, 3))
                        .add(item(ModItems.CARBON.get(), 8, 2, 6))
                        .add(item(Items.GOLD_INGOT, 4, 1, 3))
                        .add(item(ModItems.GEMSTONE.get(), 3, 1, 2))
                        .add(item(ModItems.KONTERIT.get(), 3, 1, 2))
                        .add(item(ModItems.AETHERIT.get(), 3, 1, 2))
                        .add(item(ModItems.WISP_DUST.get(), 4, 1, 3))
                        .add(item(ModBlocks.STEEL_LANTERN.get(), 3, 1, 1))
                        .add(item(ModItems.HOPS.get(), 3, 1, 3)))
                .withPool(treasure));
        // Sturmwarte: was die Sturmwächter zurückließen – Fernrohr, Kupfer, Aufzeichnungen, mit viel Glück eine Blitz-Zaubertafel
        this.output.accept(ModLootTables.STORM_WATCHTOWER_CHEST, coins(LootTable.lootTable(), 3, 9, 1, 3, 0.15f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(4, 6))
                        .add(item(Items.COPPER_INGOT, 10, 2, 7))
                        .add(item(Items.RAW_COPPER, 6, 2, 5))
                        .add(item(Items.IRON_INGOT, 6, 1, 3))
                        .add(item(Items.PAPER, 8, 2, 6))
                        .add(item(Items.SPYGLASS, 3, 1, 1))
                        .add(item(Items.COMPASS, 3, 1, 1))
                        .add(item(Items.CANDLE, 5, 1, 3))
                        .add(item(Items.AMETHYST_SHARD, 4, 1, 3))
                        .add(item(ModBlocks.STORM_CRYSTAL.get(), 6, 1, 3))
                        .add(item(Items.LIGHTNING_ROD.weathering().unaffected(), 4, 1, 2))
                        .add(item(ModItems.BREAD_ROLL.get(), 5, 1, 3))
                        .add(item(ModItems.GEMSTONE.get(), 2, 1, 2)))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.SPELL_TABLETS.get(io.github.sirschrolli.kingdomsatwar.magic.Spell.LIGHTNING).get())
                                .setWeight(1))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(3)
                                .apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                        .add(LootItem.lootTableItem(ModItems.DRAGON_COMPASS.get()).setWeight(1))
                        .add(EmptyLootItem.emptyItem().setWeight(5))));
        // Thron der Tiefe: Schätze des versunkenen Tempels – Perlmutt, Prismarin, Gold, mit Glück ein Dreizack
        this.output.accept(ModLootTables.SUNKEN_GROTTO_CHEST, coins(LootTable.lootTable(), 4, 9, 2, 5, 0.4f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(4, 7))
                        .add(item(Items.PRISMARINE_SHARD, 10, 2, 8))
                        .add(item(Items.PRISMARINE_CRYSTALS, 8, 2, 6))
                        .add(item(Items.GOLD_INGOT, 6, 1, 4))
                        .add(item(Items.GOLD_NUGGET, 8, 3, 9))
                        .add(item(Items.NAUTILUS_SHELL, 3, 1, 2))
                        .add(item(Items.HEART_OF_THE_SEA, 1, 1, 1))
                        .add(item(ModBlocks.NACRE_BLOCK.get(), 6, 1, 3))
                        .add(item(ModBlocks.GLOW_CORAL.get(), 6, 1, 4))
                        .add(item(Items.SEA_LANTERN, 4, 1, 2))
                        .add(item(ModItems.GEMSTONE.get(), 3, 1, 2)))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.SPELL_TABLETS.get(io.github.sirschrolli.kingdomsatwar.magic.Spell.WATER).get())
                                .setWeight(1))
                        .add(LootItem.lootTableItem(Items.TRIDENT).setWeight(1)
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.3f, 0.8f))))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(3)
                                .apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                        .add(EmptyLootItem.emptyItem().setWeight(4))));

        // Dörfer der Umbrer
        this.output.accept(ModLootTables.UMBRER_HOUSE, coins(LootTable.lootTable(), 1, 6, 0, 1, 0.0f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(3, 6))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.DUSK_BREAD.get(), 10, 1, 4))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.DUSKGRAIN.get(), 8, 2, 6))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.DUSKGRAIN_SEEDS.get(), 6, 2, 5))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SOUL_BERRIES.get(), 8, 2, 6))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.MOONSILVER_NUGGET.get(), 6, 1, 5))
                        .add(item(Items.DYED_CANDLE.purple(), 4, 1, 3))
                        .add(item(Items.STRING, 4, 1, 3))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.SHADOW_SAPLING.get(), 3, 1, 2))));
        this.output.accept(ModLootTables.UMBRER_SMITHY, coins(LootTable.lootTable(), 2, 7, 0, 2, 0.0f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(3, 6))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.RAW_MOONSILVER.get(), 10, 1, 4))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.MOONSILVER_NUGGET.get(), 10, 2, 7))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.MOONSILVER_INGOT.get(), 5, 1, 2))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.RAW_NIGHTSTEEL.get(), 3, 1, 2))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.MOON_LANTERN.get(), 4, 1, 2))
                        .add(item(Items.IRON_CHAIN, 4, 1, 3)))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.MOONSILVER_BLADE.get()).setWeight(1)
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.3f, 0.8f))))
                        .add(LootItem.lootTableItem(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_INGOT.get()).setWeight(1))
                        .add(EmptyLootItem.emptyItem().setWeight(6))));
        this.output.accept(ModLootTables.UMBRER_HERBALIST, coins(LootTable.lootTable(), 1, 6, 0, 1, 0.0f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(3, 6))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SOUL_BERRIES.get(), 10, 2, 8))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.GHOSTCAP.get(), 8, 1, 4))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.GLIMMER_MOSS.get(), 6, 1, 3))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.GLIMMER_VINES.get(), 6, 1, 4))
                        .add(item(Items.GLASS_BOTTLE, 6, 1, 4))
                        .add(item(Items.GLOWSTONE_DUST, 4, 1, 3))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SHADOW_ESSENCE.get(), 2, 1, 2))));
        this.output.accept(ModLootTables.UMBRER_SEER, coins(LootTable.lootTable(), 2, 8, 0, 2, 0.05f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(2, 5))
                        .add(item(Items.BOOK, 8, 1, 3))
                        .add(item(Items.PAPER, 8, 2, 6))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SOUL_CRYSTAL.get(), 3, 1, 2))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHT_CRYSTAL_SHARD.get(), 6, 2, 5))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SHADOW_ESSENCE.get(), 4, 1, 3))
                        .add(item(Items.EXPERIENCE_BOTTLE, 3, 1, 2)))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(3)
                                .apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                        .add(LootItem.lootTableItem(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_UPGRADE_SMITHING_TEMPLATE.get())
                                .setWeight(1))
                        .add(EmptyLootItem.emptyItem().setWeight(4))));
        this.output.accept(ModLootTables.UMBRER_WORKSHOP, coins(LootTable.lootTable(), 2, 7, 0, 2, 0.0f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(3, 6))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHT_CRYSTAL_SHARD.get(), 10, 2, 6))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get(), 6, 1, 2))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.NIGHT_CRYSTAL.get(), 4, 1, 2))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.MOONSILVER_NUGGET.get(), 6, 1, 4))
                        .add(item(Items.TINTED_GLASS, 3, 1, 2))
                        .add(item(Items.GLASS_BOTTLE, 3, 1, 3)))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SOUL_CRYSTAL.get()).setWeight(2))
                        .add(LootItem.lootTableItem(Items.SPYGLASS).setWeight(1))
                        .add(EmptyLootItem.emptyItem().setWeight(5))));
        this.output.accept(ModLootTables.SHADOW_CITADEL_CHEST, coins(LootTable.lootTable(), 5, 9, 3, 7, 0.5f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(4, 7))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_INGOT.get(), 10, 1, 4))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.MOONSILVER_INGOT.get(), 8, 1, 3))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SOUL_CRYSTAL.get(), 8, 1, 3))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SHADOW_ESSENCE.get(), 8, 2, 5))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_SHARD.get(), 4, 1, 2))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.DUSK_BREAD.get(), 6, 2, 5))
                        .add(item(Items.EXPERIENCE_BOTTLE, 5, 1, 3))
                        .add(item(Items.ECHO_SHARD, 3, 1, 2)))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_UPGRADE_SMITHING_TEMPLATE.get())
                                .setWeight(2))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(3)
                                .apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                        .add(LootItem.lootTableItem(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_SWORD.get()).setWeight(2))
                        .add(EmptyLootItem.emptyItem().setWeight(3))));
        // Bauwerke der Schattenwelt
        this.output.accept(ModLootTables.MOON_TEMPLE_CHEST, coins(LootTable.lootTable(), 3, 9, 1, 4, 0.15f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(4, 7))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.MOONSILVER_INGOT.get(), 8, 1, 3))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.MOONSILVER_NUGGET.get(), 10, 2, 7))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SOUL_CRYSTAL.get(), 6, 1, 2))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHT_CRYSTAL_SHARD.get(), 10, 2, 6))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SHADOW_ESSENCE.get(), 6, 1, 3))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.DUSK_BREAD.get(), 8, 1, 4))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.MOON_LANTERN.get(), 4, 1, 2))
                        .add(item(Items.EXPERIENCE_BOTTLE, 5, 1, 3))
                        .add(item(Items.CANDLE, 6, 1, 4)))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(4)
                                .apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                        .add(LootItem.lootTableItem(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.MOONSILVER_BLADE.get()).setWeight(1))
                        .add(EmptyLootItem.emptyItem().setWeight(3))));
        this.output.accept(ModLootTables.MOON_TEMPLE_CRYPT, coins(LootTable.lootTable(), 4, 9, 2, 6, 0.45f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(4, 6))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SOUL_CRYSTAL.get(), 8, 1, 3))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_SHARD.get(), 3, 1, 1))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.MOONSILVER_INGOT.get(), 8, 2, 4))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SHADOW_ESSENCE.get(), 8, 2, 4))
                        .add(item(Items.BONE, 10, 2, 6))
                        .add(item(Items.ECHO_SHARD, 3, 1, 2))
                        .add(item(Items.GOLDEN_APPLE, 2, 1, 1)))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.MOONSILVER_BLADE.get()).setWeight(2))
                        .add(LootItem.lootTableItem(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_UPGRADE_SMITHING_TEMPLATE.get()).setWeight(2))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(3)
                                .apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))));
        this.output.accept(ModLootTables.CRYSTAL_MINE_CHEST, coins(LootTable.lootTable(), 3, 9, 1, 3, 0.1f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(4, 8))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.RAW_MOONSILVER.get(), 10, 2, 6))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.RAW_NIGHTSTEEL.get(), 8, 1, 4))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHT_CRYSTAL_SHARD.get(), 10, 3, 8))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SOUL_CRYSTAL.get(), 4, 1, 2))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get(), 5, 1, 3))
                        .add(item(Items.RAIL, 6, 4, 12))
                        .add(item(Items.SOUL_TORCH, 6, 4, 10))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.DUSK_BREAD.get(), 6, 1, 3)))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_PICKAXE.get()).setWeight(2))
                        .add(LootItem.lootTableItem(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_SHARD.get()).setWeight(1))
                        .add(EmptyLootItem.emptyItem().setWeight(4))));
        this.output.accept(ModLootTables.LIGHTHOUSE_CHEST, coins(LootTable.lootTable(), 2, 8, 0, 2, 0.05f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(3, 6))
                        .add(item(Items.COD, 8, 2, 5))
                        .add(item(Items.SALMON, 6, 1, 4))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.MOON_LANTERN.get(), 6, 1, 2))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.MOONSILVER_NUGGET.get(), 8, 2, 6))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.DUSK_BREAD.get(), 8, 1, 4))
                        .add(item(Items.GLASS_BOTTLE, 4, 1, 3))
                        .add(item(Items.LEAD, 3, 1, 2))
                        .add(item(Items.SPYGLASS, 2, 1, 1))
                        .add(item(Items.COMPASS, 2, 1, 1))));
        this.output.accept(ModLootTables.OLD_WORLD_RUIN_CHEST, coins(LootTable.lootTable(), 3, 9, 1, 3, 0.12f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(3, 6))
                        .add(item(Items.WHEAT_SEEDS, 8, 2, 6))
                        .add(item(Items.BREAD, 8, 1, 3))
                        .add(item(Items.IRON_INGOT, 6, 1, 3))
                        .add(item(Items.GOLD_NUGGET, 6, 2, 8))
                        .add(item(Items.COAL, 6, 2, 6))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ModItems.STEEL_INGOT.get(), 4, 1, 2))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ModItems.KONTERIT.get(), 2, 1, 1))
                        .add(item(Items.CARROT, 4, 1, 3))
                        .add(item(Items.POTATO, 4, 1, 3)))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.MAP).setWeight(2))
                        .add(LootItem.lootTableItem(Items.COMPASS).setWeight(2))
                        .add(LootItem.lootTableItem(Items.CLOCK).setWeight(1))
                        .add(EmptyLootItem.emptyItem().setWeight(3))));
    }

    /**
     * Die Türme der Erzmagier: in der Halle Vorräte für Besucher, in der Bibliothek Bücher und Papier (selten eine
     * Zaubertafel), im Laboratorium Tränke und Zutaten, in der Kammer Münzen und Edelsteine, im Sanktum der Schatz.
     */
    private void mageTowers() {
        LootPool.Builder anyTablet = LootPool.lootPool().setRolls(ContextIntProviders.exactly(1));
        for (io.github.sirschrolli.kingdomsatwar.magic.Spell spell : io.github.sirschrolli.kingdomsatwar.magic.Spell.values()) {
            var tablet = ModItems.SPELL_TABLETS.get(spell);
            if (tablet != null) {
                anyTablet.add(LootItem.lootTableItem(tablet.get()).setWeight(1));
            }
        }
        this.output.accept(ModLootTables.MAGE_TOWER_HALL, coins(LootTable.lootTable(), 2, 6, 0, 2, 0.0f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(3, 5))
                        .add(item(ModItems.BREAD_ROLL.get(), 8, 1, 4))
                        .add(item(ModItems.MEAD.get(), 4, 1, 2))
                        .add(item(Items.CANDLE, 6, 1, 4))
                        .add(item(Items.TORCH, 6, 2, 8))
                        .add(item(Items.ARROW, 5, 4, 10))
                        .add(item(Items.PAPER, 5, 1, 4))
                        .add(item(Items.STRING, 4, 1, 3))
                        .add(item(ModItems.WISP_DUST.get(), 3, 1, 2))
                        .add(LootItem.lootTableItem(Items.POTION).setWeight(3)
                                .apply(net.minecraft.world.level.storage.loot.functions.SetPotionFunction.setPotion(
                                        net.minecraft.world.item.alchemy.Potions.HEALING)))));
        this.output.accept(ModLootTables.MAGE_TOWER_LIBRARY, coins(LootTable.lootTable(), 1, 4, 1, 2, 0.05f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(4, 7))
                        .add(item(Items.BOOK, 10, 1, 4))
                        .add(item(Items.PAPER, 10, 2, 8))
                        .add(item(Items.INK_SAC, 6, 1, 3))
                        .add(item(Items.GLOW_INK_SAC, 3, 1, 2))
                        .add(item(Items.FEATHER, 6, 1, 4))
                        .add(item(Items.WRITABLE_BOOK, 3, 1, 1))
                        .add(item(Items.LAPIS_LAZULI, 5, 2, 6))
                        .add(item(Items.MAP, 2, 1, 1))
                        .add(item(Items.CANDLE, 4, 1, 3))
                        .add(item(ModItems.CHRONICLE.get(), 1, 1, 1)))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(1, 2))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(1)
                                .apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments))))
                .withPool(anyTablet.add(EmptyLootItem.emptyItem().setWeight(70))));
        this.output.accept(ModLootTables.MAGE_TOWER_LABORATORY, coins(LootTable.lootTable(), 1, 4, 1, 2, 0.05f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(4, 7))
                        .add(item(Items.GLASS_BOTTLE, 8, 2, 5))
                        .add(item(Items.NETHER_WART, 6, 1, 4))
                        .add(item(Items.BLAZE_POWDER, 4, 1, 3))
                        .add(item(Items.GLOWSTONE_DUST, 6, 2, 6))
                        .add(item(Items.REDSTONE, 6, 2, 6))
                        .add(item(Items.SPIDER_EYE, 4, 1, 3))
                        .add(item(Items.SUGAR, 4, 1, 4))
                        .add(item(Items.GHAST_TEAR, 1, 1, 1))
                        .add(item(ModItems.WISP_DUST.get(), 5, 1, 3))
                        .add(item(ModItems.AETHERIT.get(), 4, 1, 2))
                        .add(item(Items.AMETHYST_SHARD, 4, 1, 4)))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(1, 3))
                        .add(LootItem.lootTableItem(Items.POTION).setWeight(4)
                                .apply(net.minecraft.world.level.storage.loot.functions.SetPotionFunction.setPotion(
                                        net.minecraft.world.item.alchemy.Potions.HEALING)))
                        .add(LootItem.lootTableItem(Items.POTION).setWeight(3)
                                .apply(net.minecraft.world.level.storage.loot.functions.SetPotionFunction.setPotion(
                                        net.minecraft.world.item.alchemy.Potions.REGENERATION)))
                        .add(LootItem.lootTableItem(Items.POTION).setWeight(3)
                                .apply(net.minecraft.world.level.storage.loot.functions.SetPotionFunction.setPotion(
                                        net.minecraft.world.item.alchemy.Potions.FIRE_RESISTANCE)))
                        .add(LootItem.lootTableItem(Items.POTION).setWeight(2)
                                .apply(net.minecraft.world.level.storage.loot.functions.SetPotionFunction.setPotion(
                                        net.minecraft.world.item.alchemy.Potions.NIGHT_VISION)))
                        .add(LootItem.lootTableItem(Items.POTION).setWeight(2)
                                .apply(net.minecraft.world.level.storage.loot.functions.SetPotionFunction.setPotion(
                                        net.minecraft.world.item.alchemy.Potions.SWIFTNESS)))));
        this.output.accept(ModLootTables.MAGE_TOWER_QUARTERS, coins(LootTable.lootTable(), 3, 8, 2, 5, 0.35f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(3, 6))
                        .add(item(ModItems.GEMSTONE.get(), 6, 1, 3))
                        .add(item(ModItems.AETHERIT.get(), 6, 1, 3))
                        .add(item(Items.GOLD_INGOT, 6, 1, 4))
                        .add(item(Items.EMERALD, 5, 1, 4))
                        .add(item(Items.DIAMOND, 2, 1, 2))
                        .add(item(Items.CLOCK, 2, 1, 1))
                        .add(item(Items.COMPASS, 2, 1, 1))
                        .add(item(Items.GOLDEN_APPLE, 2, 1, 1))
                        .add(item(ModItems.WINE.get(), 4, 1, 2))
                        .add(item(Items.EXPERIENCE_BOTTLE, 4, 2, 6))));
        this.output.accept(ModLootTables.MAGE_TOWER_SANCTUM, coins(LootTable.lootTable(), 4, 10, 3, 7, 0.9f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(4, 6))
                        .add(item(ModItems.AETHERIT.get(), 8, 2, 5))
                        .add(item(ModItems.GEMSTONE.get(), 6, 2, 4))
                        .add(item(Items.DIAMOND, 4, 1, 3))
                        .add(item(Items.GOLD_BLOCK, 2, 1, 1))
                        .add(item(Items.EXPERIENCE_BOTTLE, 6, 3, 8))
                        .add(item(Items.ENDER_PEARL, 4, 1, 3))
                        .add(item(Items.ENCHANTED_GOLDEN_APPLE, 1, 1, 1)))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(1)
                                .apply(net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction.enchantWithLevels(
                                        this.enchantments, ContextIntProviders.between(20, 30)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.AETHERIT_STAFF.get()).setWeight(2))
                        .add(LootItem.lootTableItem(ModItems.DRAGON_COMPASS.get()).setWeight(2))
                        .add(LootItem.lootTableItem(Items.TOTEM_OF_UNDYING).setWeight(1))
                        .add(EmptyLootItem.emptyItem().setWeight(5))));
    }
}
