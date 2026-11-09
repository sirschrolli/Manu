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
     * Münzen in einer Truhe: nur Bronze – Silber und Gold gibt es allein durch Zusammenlegen (9 Bronze = 1 Silber, 9 Silber
     * = 1 Gold) und im Handel. Die alten Mengen bleiben als Maß für die Truhe: wo früher Silber lag, liegt etwas mehr
     * Bronze, insgesamt aber weit weniger Wert als zuvor.
     */
    private static LootTable.Builder coins(LootTable.Builder table, int bronzeMin, int bronzeMax, int silverMin, int silverMax,
            float goldChance) {
        // nur Bronze, und karg: die alten Silber- und Goldmünzen zählen nur noch als ein paar Bronzestücke mehr
        int min = Math.max(0, bronzeMin);
        int max = bronzeMax + silverMax + (goldChance >= 0.1f ? 1 : 0);
        if (max > 0) {
            table.withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                    .add(item(ModItems.BRONZE_COIN.get(), 1, Math.min(min, max), max)));
        }
        return table;
    }

    /**
     * Höchstens ein Herz- oder Manakristall: mit der Wahrscheinlichkeit {@code heart} bzw. {@code mana} (in Hundertsteln),
     * sonst keiner.
     */
    private static LootPool.Builder crystals(int heart, int mana) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(ContextIntProviders.exactly(1));
        if (heart > 0) {
            pool.add(LootItem.lootTableItem(ModItems.HEART_CRYSTAL.get()).setWeight(heart));
        }
        if (mana > 0) {
            pool.add(LootItem.lootTableItem(ModItems.MANA_CRYSTAL.get()).setWeight(mana));
        }
        return pool.add(EmptyLootItem.emptyItem().setWeight(100 - heart - mana));
    }

    private static UniformContainerBase.Builder<?> item(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item).setWeight(weight).apply(SetItemCountFunction.setCount(ContextIntProviders.between(min, max)));
    }

    /**
     * Ein Pool mit {@code min}–{@code max} Ziehungen aus den Einträgen – karger als angegeben: eine Ziehung weniger und
     * höchstens zwei unter dem alten Höchstwert (die Truhen sollen sich verdient anfühlen).
     */
    @SafeVarargs
    private static LootPool.Builder pool(int min, int max, UniformContainerBase.Builder<?>... entries) {
        int lean = Math.max(1, min - 1);
        LootPool.Builder pool = LootPool.lootPool().setRolls(ContextIntProviders.between(lean, Math.max(lean, max - 2)));
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

    /**
     * Die eigenen Bauwerke an Stelle der Vanilla-Bauwerke: In jeder Truhe liegt alles, was die alte Truhe enthielt (die
     * Vanilla-Tabelle wird einmal gezogen – so bleiben Schmiedevorlagen, Schallplatten, das Herz des Meeres und die
     * Schatzkarten erhältlich), dazu Münzen und manchmal etwas aus diesem Königreich.
     */
    /**
     * Die Truhen der eigenen Bauwerke der Oberwelt. Der Sternensturz und die Wasserstollen haben ganz eigene Beute;
     * im Gezeitenschrein liegt (wie im vergrabenen Schatz) immer das Herz des Meeres, und in der Truhe des Kapitäns in den
     * Gebeinen eines Seedrachen immer die Seekarte, die zu einem Gezeitenschrein führt.
     */
    private void sites() {
        // Sterndeuter: Vorräte, Schreibzeug, Kerzen, ein Fernrohr, selten eine Enderperle
        this.output.accept(ModLootTables.SITE_FALLEN_STAR_CAMP, coins(LootTable.lootTable(), 2, 8, 0, 2, 0.05f)
                .withPool(pool(3, 6, item(Items.BREAD, 8, 1, 4), item(Items.APPLE, 6, 1, 3), item(Items.TORCH, 8, 3, 10), item(Items.PAPER, 6, 2, 6),
                        item(Items.BOOK, 4, 1, 2), item(Items.CANDLE, 5, 1, 3), item(Items.COMPASS, 2, 1, 1), item(Items.SPYGLASS, 1, 1, 1),
                        item(Items.ENDER_PEARL, 2, 1, 1), item(Items.AMETHYST_SHARD, 4, 1, 4))));
        // Kern des Sterns: Enderperlen und -augen, Obsidian, Amethyst, Gold, ein verzaubertes Buch der Sterndeuter
        this.output.accept(ModLootTables.SITE_FALLEN_STAR_CORE, coins(LootTable.lootTable().withPool(crystals(10, 20)), 0, 0, 2, 6, 0.4f)
                .withPool(pool(1, 1, item(Items.ENDER_PEARL, 1, 2, 5)))
                .withPool(pool(2, 4, item(Items.ENDER_EYE, 2, 1, 2), item(Items.OBSIDIAN, 6, 2, 6), item(Items.CRYING_OBSIDIAN, 4, 1, 3),
                        item(Items.AMETHYST_SHARD, 6, 3, 8), item(Items.GOLD_INGOT, 4, 1, 4), item(ModItems.GEMSTONE.get(), 3, 1, 2),
                        item(Items.EXPERIENCE_BOTTLE, 3, 1, 4)))
                .withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.BOOK).apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                        .when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.randomChance(0.6f))));
        // Wasserwärter: Werkzeug, Eimer, Seil, Laternen, Brot, etwas Stahl
        this.output.accept(ModLootTables.SITE_AQUEDUCT_ALCOVE, coins(LootTable.lootTable(), 2, 8, 0, 2, 0.05f)
                .withPool(pool(3, 6, item(Items.BUCKET, 4, 1, 1), item(Items.WATER_BUCKET, 2, 1, 1), item(Items.LEAD, 3, 1, 2),
                        item(Items.LANTERN, 4, 1, 2), item(Items.TORCH, 6, 2, 8), item(Items.BREAD, 6, 1, 3), item(Items.IRON_INGOT, 4, 1, 3),
                        item(ModItems.STEEL_INGOT.get(), 3, 1, 2), item(Items.IRON_PICKAXE, 1, 1, 1), item(Items.IRON_SHOVEL, 2, 1, 1),
                        item(Items.STONE_BRICKS, 4, 4, 12), item(Items.CLAY_BALL, 3, 2, 6))));
        // Opfergaben auf dem Grund der Zisterne: Münzen, Gold, Edelsteine, Perlmutt (Prismarin), ein Nautilus
        this.output.accept(ModLootTables.SITE_AQUEDUCT_CISTERN, coins(LootTable.lootTable().withPool(crystals(10, 6)), 4, 12, 2, 6, 0.5f)
                .withPool(pool(2, 4, item(Items.GOLD_NUGGET, 8, 3, 12), item(Items.GOLD_INGOT, 4, 1, 3), item(ModItems.GEMSTONE.get(), 4, 1, 3),
                        item(Items.PRISMARINE_SHARD, 4, 2, 6), item(Items.NAUTILUS_SHELL, 2, 1, 1), item(Items.EMERALD, 3, 1, 3),
                        item(Items.GLOW_INK_SAC, 3, 1, 3))));
        // Gezeitenschrein: was im vergrabenen Schatz lag (immer das Herz des Meeres), dazu Münzen und Edelsteine
        this.output.accept(ModLootTables.SITE_TIDE_SHRINE, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.BURIED_TREASURE),
                0, 0, 3, 8, 0.8f)
                .withPool(pool(0, 1, item(ModItems.GEMSTONE.get(), 1, 1, 3))));
        // Versunkenes Atoll: der Schatz des Meeresvolks im Tempel – Perlmutt, Gold, Nautilusse, Edelsteine, ein Gezeitenhorn
        this.output.accept(io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollPiece.CHEST,
                coins(LootTable.lootTable().withPool(crystals(20, 15)), 2, 8, 2, 6, 0.6f)
                .withPool(pool(3, 6, item(Items.PRISMARINE_CRYSTALS, 6, 2, 6), item(Items.PRISMARINE_SHARD, 6, 3, 8),
                        item(Items.GOLD_INGOT, 4, 1, 4), item(Items.GOLD_NUGGET, 6, 4, 12), item(Items.NAUTILUS_SHELL, 3, 1, 2),
                        item(ModItems.GEMSTONE.get(), 3, 1, 3), item(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.SCALLOP_SHELLS.get(), 4, 1, 3),
                        item(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.TIDE_HORN.get(), 1, 1, 1),
                        item(Items.SEA_LANTERN, 3, 1, 2)))
                .withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.BOOK).apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                        .when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.randomChance(0.5f))));
        // Seedrache: die verschluckte Fracht eines Schiffs (Seekarte, Vorräte, Schatz)
        this.output.accept(ModLootTables.SITE_SEA_DRAGON_MAP, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.SHIPWRECK_MAP),
                1, 5, 0, 1, 0.0f));
        this.output.accept(ModLootTables.SITE_SEA_DRAGON_CARGO, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.SHIPWRECK_SUPPLY),
                1, 4, 0, 0, 0.0f)
                .withPool(pool(0, 2, item(Items.BONE, 6, 1, 4), item(Items.BONE_MEAL, 4, 2, 6))));
        this.output.accept(ModLootTables.SITE_SEA_DRAGON_HOARD, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.SHIPWRECK_TREASURE).withPool(crystals(15, 10)),
                2, 8, 2, 5, 0.3f));
        // Karawanserei: der Schatz der Karawanenmeister (wie im Wüstentempel), Habe eines Reisenden
        this.output.accept(ModLootTables.SITE_CARAVANSERAI_TREASURE, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.DESERT_PYRAMID),
                0, 4, 2, 6, 0.4f)
                .withPool(pool(0, 2, item(Items.GOLD_INGOT, 4, 1, 3), item(ModItems.GEMSTONE.get(), 2, 1, 2), item(Items.CARPET.pick(net.minecraft.world.item.DyeColor.ORANGE), 2, 1, 3))));
        this.output.accept(ModLootTables.SITE_CARAVANSERAI_TRAVELER, coins(LootTable.lootTable(), 2, 8, 0, 3, 0.05f)
                .withPool(pool(3, 6, item(Items.BREAD, 6, 1, 3), item(Items.APPLE, 5, 1, 3), item(Items.STRING, 5, 2, 6), item(Items.LEATHER, 4, 1, 3),
                        item(Items.CARPET.pick(net.minecraft.world.item.DyeColor.RED), 3, 1, 3), item(Items.DYE.pick(net.minecraft.world.item.DyeColor.ORANGE), 3, 1, 4), item(Items.CACTUS, 3, 1, 4), item(Items.SADDLE, 1, 1, 1),
                        item(Items.LEAD, 2, 1, 1), item(Items.COMPASS, 1, 1, 1), item(Items.GLASS_BOTTLE, 4, 1, 3))));
        // Schlangentempel: die Opfergaben (wie im Dschungeltempel), dazu Gold und Smaragde
        this.output.accept(ModLootTables.SITE_SERPENT_TEMPLE, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.JUNGLE_TEMPLE).withPool(crystals(10, 8)),
                0, 4, 2, 6, 0.4f)
                .withPool(pool(1, 2, item(Items.GOLD_NUGGET, 6, 3, 9), item(Items.EMERALD, 4, 1, 3), item(ModItems.GEMSTONE.get(), 3, 1, 2),
                        item(Items.COCOA_BEANS, 3, 2, 5))));
        // Hexenbaum: Zutaten der Hexe
        this.output.accept(ModLootTables.SITE_WITCH_TREE, coins(LootTable.lootTable(), 1, 5, 0, 2, 0.05f)
                .withPool(pool(3, 6, item(Items.GLASS_BOTTLE, 6, 1, 4), item(Items.REDSTONE, 5, 1, 4), item(Items.GLOWSTONE_DUST, 4, 1, 3),
                        item(Items.SUGAR, 5, 1, 4), item(Items.SPIDER_EYE, 4, 1, 3), item(Items.FERMENTED_SPIDER_EYE, 2, 1, 1),
                        item(Items.NETHER_WART, 3, 1, 3), item(Items.BROWN_MUSHROOM, 4, 1, 3), item(Items.RED_MUSHROOM, 4, 1, 3),
                        item(Items.GUNPOWDER, 3, 1, 2), item(Items.STICK, 3, 1, 3), item(Items.CANDLE, 3, 1, 2))));
        // Frostgrotte: die Stube (wie im Iglu) und der Keller des Einsiedlers
        this.output.accept(ModLootTables.SITE_FROST_GROTTO, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.IGLOO_CHEST),
                1, 4, 0, 1, 0.0f)
                .withPool(pool(1, 3, item(Items.COD, 5, 1, 4), item(Items.SALMON, 4, 1, 3), item(Items.TORCH, 5, 2, 6), item(Items.LEATHER, 3, 1, 3))));
        this.output.accept(ModLootTables.SITE_FROST_GROTTO_CELLAR, coins(LootTable.lootTable().withPool(crystals(12, 8)), 0, 3, 2, 5, 0.3f)
                .withPool(pool(1, 1, item(Items.GOLDEN_APPLE, 1, 1, 1)))
                .withPool(pool(2, 4, item(Items.GLASS_BOTTLE, 4, 1, 3), item(Items.BLAZE_POWDER, 2, 1, 2), item(Items.FERMENTED_SPIDER_EYE, 3, 1, 2),
                        item(Items.GOLD_NUGGET, 4, 2, 8), item(Items.EMERALD, 3, 1, 2), item(ModItems.GEMSTONE.get(), 2, 1, 1),
                        item(Items.BOOK, 3, 1, 2))));
        // Räuberlager: die Beute der Wegelagerer (wie im Außenposten, mit Armbrust und Wachposten-Besatz)
        this.output.accept(ModLootTables.SITE_BANDIT_CAMP, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.PILLAGER_OUTPOST),
                4, 12, 1, 4, 0.2f)
                .withPool(pool(0, 2, item(ModItems.STEEL_INGOT.get(), 3, 1, 2), item(Items.LEATHER, 4, 1, 3), item(Items.GOLD_NUGGET, 4, 2, 6))));
        // Köhlerei: Vorräte und Karten der Köhler (wie im verlassenen Lager), im Versteck die geheimen Karten
        this.output.accept(ModLootTables.SITE_CHARCOAL_CAMP, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables
                .ABANDONED_CAMP_COMMON_CHEST), 1, 5, 0, 1, 0.0f)
                .withPool(pool(1, 2, item(Items.CHARCOAL, 6, 4, 12), item(Items.STONE_AXE, 2, 1, 1), item(Items.BREAD, 3, 1, 3))));
        this.output.accept(ModLootTables.SITE_CHARCOAL_CAMP_SECRET, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables
                .ABANDONED_CAMP_SECRET_CHEST), 0, 0, 2, 6, 0.4f));
        // Sternwarte: der Schatz des Ordens (wie in der Endstadt)
        this.output.accept(ModLootTables.SITE_STARWATCH, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.END_CITY_TREASURE).withPool(crystals(5, 20)),
                0, 4, 2, 5, 0.3f));
        // Glutpforte: das Gold der Feuerdiener (wie in der Portalruine)
        this.output.accept(ModLootTables.SITE_EMBER_GATE, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.RUINED_PORTAL),
                0, 3, 1, 3, 0.1f));
        // Muschelfeld: was das Meer in die Muscheln spülte (wie in den Ozeanruinen, mit Schatzkarten), dazu Perlen (Prismarin)
        this.output.accept(ModLootTables.SITE_CLAM_BIG, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.UNDERWATER_RUIN_BIG),
                0, 4, 1, 3, 0.15f)
                .withPool(pool(1, 2, item(Items.PRISMARINE_CRYSTALS, 4, 1, 4), item(Items.NAUTILUS_SHELL, 1, 1, 1), item(ModItems.GEMSTONE.get(), 2, 1, 2))));
        this.output.accept(ModLootTables.SITE_CLAM_SMALL, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables
                .UNDERWATER_RUIN_SMALL), 1, 4, 0, 1, 0.0f)
                .withPool(pool(0, 2, item(Items.PRISMARINE_CRYSTALS, 4, 1, 3), item(Items.KELP, 3, 2, 6))));
        // Kloster: Bücher, Schreibzeug, Kerzen, Brot, ein verzaubertes Buch der Mönche
        this.output.accept(ModLootTables.SITE_BURIED_ABBEY, coins(LootTable.lootTable().withPool(crystals(12, 10)), 0, 4, 1, 4, 0.2f)
                .withPool(pool(3, 6, item(Items.BOOK, 6, 1, 3), item(Items.PAPER, 6, 2, 8), item(Items.INK_SAC, 4, 1, 3), item(Items.FEATHER, 4, 1, 3),
                        item(Items.CANDLE, 4, 1, 3), item(Items.BREAD, 3, 1, 2), item(Items.BRUSH, 2, 1, 1), item(Items.GOLD_NUGGET, 3, 2, 6)))
                .withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.BOOK).apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                        .when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.randomChance(0.5f))));
        this.greatSites();
    }

    /** Kathedrale, Arena und Burg: die Beute der Bauwerke, an deren Stelle sie stehen, dazu eigene Funde. */
    private void greatSites() {
        // Stille Kathedrale: wie in der antiken Stadt (Echosplitter, Schallplattenbruchstück, Bücher mit Huschen …)
        this.output.accept(ModLootTables.SITE_CATHEDRAL, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.ANCIENT_CITY),
                2, 8, 1, 3, 0.15f)
                .withPool(pool(0, 2, item(Items.CANDLE, 4, 1, 3), item(Items.BONE, 4, 1, 4), item(Items.ECHO_SHARD, 1, 1, 1))));
        this.output.accept(ModLootTables.SITE_CATHEDRAL_RELIQUARY, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.ANCIENT_CITY).withPool(crystals(15, 15)),
                4, 12, 2, 6, 0.4f)
                .withPool(pool(1, 2, item(Items.ECHO_SHARD, 3, 1, 3), item(ModItems.GEMSTONE.get(), 2, 1, 2), item(Items.SKELETON_SKULL, 1, 1, 1))));
        // Arena der Prüfungen: die Vorräte der Kämpfer (wie in den Prüfungskammern)
        this.output.accept(ModLootTables.SITE_TRIAL_ARENA, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.TRIAL_CHAMBERS_SUPPLY).withPool(crystals(8, 8)),
                1, 6, 0, 2, 0.05f)
                .withPool(pool(0, 2, item(Items.BREAD, 3, 1, 3), item(Items.ARROW, 3, 4, 12), item(Items.LEATHER, 2, 1, 3))));
        // Raubritterburg: Raubgut (wie im Waldanwesen), in der Kammer des Raubritters sein Hort
        this.output.accept(ModLootTables.SITE_ROBBER_CASTLE, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.WOODLAND_MANSION),
                3, 10, 1, 4, 0.15f)
                .withPool(pool(0, 2, item(ModItems.STEEL_INGOT.get(), 3, 1, 2), item(Items.LEATHER, 3, 1, 4), item(Items.WHEAT, 3, 2, 6))));
        this.output.accept(ModLootTables.SITE_ROBBER_CASTLE_HOARD, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.WOODLAND_MANSION).withPool(crystals(15, 8)),
                10, 30, 4, 10, 0.5f)
                .withPool(pool(2, 4, item(Items.GOLD_INGOT, 5, 1, 4), item(Items.EMERALD, 4, 1, 5), item(ModItems.GEMSTONE.get(), 3, 1, 3),
                        item(Items.DIAMOND, 1, 1, 2), item(Items.SADDLE, 2, 1, 1))));
        // Aschenesse: was in der Netherfestung lag (Netherwarzen, Gold, Eisen, Diamanten, Sattel, Rossharnische …),
        // dazu Schmiedegut; im Hort des Aschenkönigs mehr Gold, Edelsteine, etwas Netheritschrott und selten ein Schädel
        this.output.accept(ModLootTables.SITE_ASH_FORGE, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.NETHER_BRIDGE),
                1, 6, 0, 2, 0.08f)
                .withPool(pool(1, 3, item(Items.NETHER_WART, 5, 2, 6), item(Items.MAGMA_CREAM, 3, 1, 2), item(Items.GOLD_NUGGET, 4, 3, 9),
                        item(Items.QUARTZ, 3, 2, 6), item(Items.IRON_INGOT, 2, 1, 3))));
        this.output.accept(ModLootTables.SITE_ASH_FORGE_HOARD, coins(vanilla(net.minecraft.world.level.storage.loot.BuiltInLootTables.NETHER_BRIDGE).withPool(crystals(15, 8)),
                6, 18, 2, 6, 0.4f)
                .withPool(pool(2, 4, item(Items.GOLD_INGOT, 5, 2, 6), item(Items.GILDED_BLACKSTONE, 3, 1, 3), item(ModItems.GEMSTONE.get(), 2, 1, 2),
                        item(Items.DIAMOND, 2, 1, 2), item(Items.NETHERITE_SCRAP, 1, 1, 1)))
                .withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.WITHER_SKELETON_SKULL))
                        .when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.randomChance(0.06f))));
    }

    /**
     * Die Truhen der Stammburgen: in den Gemächern des Herrschers die Schatztruhe (erst nach dem Kampf durch die ganze Burg
     * zu erreichen, daher reich, aber nicht verschwenderisch), in der Kaserne die Waffenkammer, in Küche und Vorratshaus
     * die Vorräte.
     */
    private void realmCastles() {
        this.output.accept(ModLootTables.REALM_TREASURY, coins(LootTable.lootTable().withPool(crystals(30, 15)), 12, 30, 4, 10, 0.5f)
                .withPool(pool(3, 5, item(Items.GOLD_INGOT, 6, 2, 5), item(ModItems.GEMSTONE.get(), 4, 1, 3), item(Items.EMERALD, 4, 2, 5),
                        item(Items.GOLD_NUGGET, 4, 6, 14), item(Items.DIAMOND, 1, 1, 2), item(ModItems.STEEL_INGOT.get(), 3, 2, 4)))
                .withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(6).apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                        .add(LootItem.lootTableItem(ModItems.GEMSTONE_CROWN.get()).setWeight(1))
                        .add(LootItem.lootTableItem(ModItems.DRAGON_COMPASS.get()).setWeight(2))
                        .add(EmptyLootItem.emptyItem().setWeight(5)))
                .withPool(this.modBook(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Enchantments.LOOT_REALM, 40)));
        this.output.accept(ModLootTables.REALM_ARMORY, coins(LootTable.lootTable(), 2, 8, 0, 2, 0.0f)
                .withPool(pool(3, 5, item(Items.ARROW, 6, 8, 20), item(ModItems.STEEL_INGOT.get(), 4, 1, 3), item(Items.IRON_INGOT, 5, 2, 5),
                        item(Items.LEATHER, 3, 2, 5), item(Items.STRING, 3, 2, 6), item(Items.SHIELD, 2, 1, 1)))
                .withPool(LootPool.lootPool().setRolls(ContextIntProviders.between(1, 2))
                        .add(LootItem.lootTableItem(ModItems.STEEL_SWORD.get()).setWeight(4)
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.2f, 0.7f))))
                        .add(LootItem.lootTableItem(ModItems.STEEL_AXE.get()).setWeight(3)
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.2f, 0.7f))))
                        .add(LootItem.lootTableItem(ModItems.STEEL_HELMET.get()).setWeight(3)
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.2f, 0.7f))))
                        .add(LootItem.lootTableItem(ModItems.STEEL_CHESTPLATE.get()).setWeight(2)
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.3f, 0.8f))))
                        .add(LootItem.lootTableItem(Items.CROSSBOW).setWeight(3)
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.2f, 0.7f))))
                        .add(EmptyLootItem.emptyItem().setWeight(4)))
                .withPool(this.modBook(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Enchantments.LOOT_REALM, 15)));
        this.output.accept(ModLootTables.REALM_STORES, coins(LootTable.lootTable(), 0, 4, 0, 0, 0.0f)
                .withPool(pool(4, 7, item(ModItems.BREAD_ROLL.get(), 8, 2, 6), item(ModItems.COOKED_VENISON.get(), 5, 1, 4),
                        item(ModItems.ALE.get(), 5, 1, 3), item(ModItems.WINE.get(), 3, 1, 2), item(Items.WHEAT, 5, 3, 9),
                        item(Items.CARROT, 4, 2, 6), item(Items.POTATO, 4, 2, 6), item(Items.APPLE, 4, 1, 4), item(Items.SALMON, 2, 1, 3))));
    }

    /**
     * Ein Pool mit einem Buch der Verzauberungen dieser Mod (aus der Beute-Gruppe {@code tag}) – mit der Wahrscheinlichkeit
     * {@code chance} in Hundertsteln, sonst nichts.
     */
    private LootPool.Builder modBook(net.minecraft.tags.TagKey<Enchantment> tag, int chance) {
        return LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(Items.BOOK).setWeight(chance)
                        .apply(EnchantRandomlyFunction.randomEnchantment().withOneOf(this.enchantments.getOrThrow(tag))))
                .add(EmptyLootItem.emptyItem().setWeight(100 - chance));
    }

    /** Eine Tabelle, die einmal die Vanilla-Tabelle zieht. */
    private LootTable.Builder vanilla(net.minecraft.resources.ResourceKey<LootTable> table) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                .add(net.minecraft.world.level.storage.loot.entries.NestedLootTable.lootTableReference(
                        this.output.lookup(Registries.LOOT_TABLE).getOrThrow(table))));
    }

    @Override
    public void run() {
        this.mageTowers();
        this.villages();
        this.sites();
        this.realmCastles();
        LootPool.Builder treasure = LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(ModItems.STEEL_SWORD.get()).setWeight(5)
                        .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.3f, 0.9f))))
                .add(LootItem.lootTableItem(ModItems.STEEL_HELMET.get()).setWeight(4)
                        .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.3f, 0.9f))))
                .add(LootItem.lootTableItem(Items.BOOK).setWeight(5).apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                .add(LootItem.lootTableItem(ModItems.DRAGON_COMPASS.get()).setWeight(2))
                .add(LootItem.lootTableItem(ModItems.GEMSTONE_CROWN.get()).setWeight(1))
                .add(LootItem.lootTableItem(ModItems.AETHERIT.get()).setWeight(1)
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(2, 4))))
                .add(EmptyLootItem.emptyItem().setWeight(8));
        this.output.accept(ModLootTables.CASTLE_RUIN_CHEST, coins(LootTable.lootTable().withPool(crystals(8, 4)), 3, 9, 1, 3, 0.12f)
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
        this.output.accept(ModLootTables.STORM_WATCHTOWER_CHEST, coins(LootTable.lootTable().withPool(crystals(6, 6)), 3, 9, 1, 3, 0.15f)
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
        this.output.accept(ModLootTables.SUNKEN_GROTTO_CHEST, coins(LootTable.lootTable().withPool(crystals(10, 8)), 4, 9, 2, 5, 0.4f)
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
        this.output.accept(ModLootTables.SHADOW_CITADEL_CHEST, coins(LootTable.lootTable().withPool(crystals(20, 20)), 5, 9, 3, 7, 0.5f)
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
                        .add(EmptyLootItem.emptyItem().setWeight(3)))
                .withPool(this.modBook(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Enchantments.LOOT_DRAGON, 20)));
        // Bauwerke der Schattenwelt
        // Truhen der Alten in den schwerelosen Ruinen der Leere
        this.output.accept(ModLootTables.VOID_RUIN_CHEST, coins(LootTable.lootTable(), 4, 10, 2, 5, 0.25f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(4, 7))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_SHARD.get(), 12, 2, 6))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_CRYSTAL.get(), 4, 1, 2))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CHISELED_ASTRAL_MARBLE.get(), 4, 1, 3))
                        .add(item(Items.GOLD_INGOT, 8, 2, 5))
                        .add(item(Items.GOLD_NUGGET, 8, 4, 12))
                        .add(item(Items.ENDER_PEARL, 6, 1, 3))
                        .add(item(Items.EXPERIENCE_BOTTLE, 6, 2, 5))
                        .add(item(Items.AMETHYST_SHARD, 6, 2, 6))
                        .add(item(Items.DIAMOND, 2, 1, 2))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SOUL_CRYSTAL.get(), 4, 1, 2)))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(5)
                                .apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                        .add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE).setWeight(1))
                        .add(EmptyLootItem.emptyItem().setWeight(4))));
        // Truhen in den Ruinen des Feenreichs
        this.output.accept(ModLootTables.FAE_RUIN_CHEST, coins(LootTable.lootTable(), 3, 8, 1, 4, 0.12f)
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(4, 7))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_DUST.get(), 10, 1, 3))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MOONSTONE_BRICKS.base().get(), 6, 2, 6))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.CHISELED_MOONSTONE.get(), 4, 1, 2))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MOONBELL.get(), 6, 1, 3))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.GLIMMERWOOD_SAPLING.get(), 5, 1, 2))
                        .add(item(Items.AMETHYST_SHARD, 10, 2, 6))
                        .add(item(Items.GLOW_BERRIES, 8, 2, 6))
                        .add(item(Items.GOLD_INGOT, 6, 1, 4))
                        .add(item(Items.GOLD_NUGGET, 8, 3, 9))
                        .add(item(Items.HONEY_BOTTLE, 5, 1, 2))
                        .add(item(Items.EXPERIENCE_BOTTLE, 5, 1, 3))
                        .add(item(Items.EMERALD, 4, 1, 3)))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(5)
                                .apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                        .add(LootItem.lootTableItem(Items.GOLDEN_APPLE).setWeight(2))
                        .add(EmptyLootItem.emptyItem().setWeight(4))));
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
        this.output.accept(ModLootTables.MOON_TEMPLE_CRYPT, coins(LootTable.lootTable().withPool(crystals(12, 18)), 4, 9, 2, 6, 0.45f)
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
        this.output.accept(ModLootTables.CRYSTAL_MINE_CHEST, coins(LootTable.lootTable().withPool(crystals(12, 15)), 3, 9, 1, 3, 0.1f)
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
        this.output.accept(ModLootTables.OLD_WORLD_RUIN_CHEST, coins(LootTable.lootTable().withPool(crystals(8, 6)), 3, 9, 1, 3, 0.12f)
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
        this.output.accept(ModLootTables.MAGE_TOWER_LIBRARY, coins(LootTable.lootTable().withPool(crystals(0, 15)), 1, 4, 1, 2, 0.05f)
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
                .withPool(anyTablet.add(EmptyLootItem.emptyItem().setWeight(70)))
                .withPool(this.modBook(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Enchantments.LOOT_ARCANE, 12)));
        this.output.accept(ModLootTables.MAGE_TOWER_LABORATORY, coins(LootTable.lootTable().withPool(crystals(3, 15)), 1, 4, 1, 2, 0.05f)
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
        this.output.accept(ModLootTables.MAGE_TOWER_QUARTERS, coins(LootTable.lootTable().withPool(crystals(5, 15)), 3, 8, 2, 5, 0.35f)
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
        this.output.accept(ModLootTables.MAGE_TOWER_SANCTUM, coins(LootTable.lootTable().withPool(crystals(10, 45)), 4, 10, 3, 7, 0.9f)
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
                        .add(LootItem.lootTableItem(ModItems.WISP_DUST.get()).setWeight(2)
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(2, 4))))
                        .add(LootItem.lootTableItem(ModItems.DRAGON_COMPASS.get()).setWeight(2))
                        .add(LootItem.lootTableItem(Items.TOTEM_OF_UNDYING).setWeight(1))
                        .add(EmptyLootItem.emptyItem().setWeight(5)))
                .withPool(this.modBook(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Enchantments.LOOT_ARCANE, 35)));
    }
}
