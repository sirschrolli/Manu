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

    private static UniformContainerBase.Builder<?> item(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item).setWeight(weight).apply(SetItemCountFunction.setCount(ContextIntProviders.between(min, max)));
    }

    @Override
    public void run() {
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
        ModItems.RUNES.values().forEach(rune -> treasure.add(LootItem.lootTableItem(rune.get()).setWeight(1)));
        this.output.accept(ModLootTables.CASTLE_RUIN_CHEST, LootTable.lootTable()
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
        // Sturmwarte: was die Sturmwächter zurückließen – Fernrohr, Kupfer, Aufzeichnungen, mit Glück Blitzrunen
        this.output.accept(ModLootTables.STORM_WATCHTOWER_CHEST, LootTable.lootTable()
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
                        .add(LootItem.lootTableItem(ModItems.RUNES.get(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.LIGHTNING).get())
                                .setWeight(3))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(3)
                                .apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                        .add(LootItem.lootTableItem(ModItems.DRAGON_COMPASS.get()).setWeight(1))
                        .add(EmptyLootItem.emptyItem().setWeight(5))));
        // Thron der Tiefe: Schätze des versunkenen Tempels – Perlmutt, Prismarin, Gold, mit Glück ein Dreizack
        this.output.accept(ModLootTables.SUNKEN_GROTTO_CHEST, LootTable.lootTable()
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
                        .add(LootItem.lootTableItem(ModItems.RUNES.get(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.WATER).get())
                                .setWeight(3))
                        .add(LootItem.lootTableItem(Items.TRIDENT).setWeight(1)
                                .apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(0.3f, 0.8f))))
                        .add(LootItem.lootTableItem(Items.BOOK).setWeight(3)
                                .apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                        .add(EmptyLootItem.emptyItem().setWeight(4))));

        // Dörfer der Umbrer
        this.output.accept(ModLootTables.UMBRER_HOUSE, LootTable.lootTable()
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
        this.output.accept(ModLootTables.UMBRER_SMITHY, LootTable.lootTable()
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
        this.output.accept(ModLootTables.UMBRER_HERBALIST, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.between(3, 6))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SOUL_BERRIES.get(), 10, 2, 8))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.GHOSTCAP.get(), 8, 1, 4))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.GLIMMER_MOSS.get(), 6, 1, 3))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.GLIMMER_VINES.get(), 6, 1, 4))
                        .add(item(Items.GLASS_BOTTLE, 6, 1, 4))
                        .add(item(Items.GLOWSTONE_DUST, 4, 1, 3))
                        .add(item(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SHADOW_ESSENCE.get(), 2, 1, 2))));
        this.output.accept(ModLootTables.UMBRER_SEER, LootTable.lootTable()
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
        this.output.accept(ModLootTables.UMBRER_WORKSHOP, LootTable.lootTable()
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
    }
}
