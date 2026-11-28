package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

/**
 * Die Truhen des Angelns (seit 0.79): Wer im Drill eine Truhe hebt, bekommt im Wasser die Schätze von Minecraft – in Lava und
 * Leere gibt es eigene: was im Nether in die Glut gefallen ist, und was zwischen den Inseln des Ends verloren ging.
 */
public class ModFishingLootProvider implements LootTableSubProvider {
    private final LootTableSubProvider.Context output;
    private final HolderGetter<Enchantment> enchantments;

    public ModFishingLootProvider(LootTableSubProvider.Context output) {
        this.output = output;
        this.enchantments = output.lookup(Registries.ENCHANTMENT);
    }

    private static UniformContainerBase.Builder<?> item(ItemLike item, int weight, int min, int max) {
        return LootItem.lootTableItem(item).setWeight(weight).apply(SetItemCountFunction.setCount(ContextIntProviders.between(min, max)));
    }

    @Override
    public void run() {
        this.output.accept(ModLootTables.FISHING_TREASURE_LAVA, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .add(item(Items.GOLD_NUGGET, 20, 4, 12))
                .add(item(Items.QUARTZ, 16, 2, 6))
                .add(item(Items.MAGMA_CREAM, 14, 1, 3))
                .add(item(Items.BLAZE_POWDER, 10, 1, 3))
                .add(item(Items.GOLD_INGOT, 10, 1, 3))
                .add(item(Items.OBSIDIAN, 8, 1, 3))
                .add(item(Items.FIRE_CHARGE, 6, 1, 3))
                .add(LootItem.lootTableItem(Items.BOOK).setWeight(6).apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                .add(item(Items.GOLDEN_APPLE, 3, 1, 1))
                .add(item(Items.NETHERITE_SCRAP, 2, 1, 1))
                .add(item(Items.MUSIC_DISC_PIGSTEP, 1, 1, 1))
                .add(item(Items.ANCIENT_DEBRIS, 1, 1, 1))));
        this.output.accept(ModLootTables.FISHING_TREASURE_VOID, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .add(item(Items.ENDER_PEARL, 20, 1, 3))
                .add(item(Items.CHORUS_FRUIT, 14, 2, 5))
                .add(item(Items.AMETHYST_SHARD, 12, 2, 6))
                .add(item(Items.END_ROD, 6, 1, 3))
                .add(item(Items.ECHO_SHARD, 4, 1, 2))
                .add(item(Items.OBSIDIAN, 8, 1, 4))
                .add(item(Items.PHANTOM_MEMBRANE, 8, 1, 3))
                .add(LootItem.lootTableItem(Items.BOOK).setWeight(8).apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.enchantments)))
                .add(item(Items.ENDER_EYE, 6, 1, 1))
                .add(item(Items.DIAMOND, 5, 1, 2))
                .add(item(Items.DRAGON_BREATH, 4, 1, 1))
                .add(item(Items.SHULKER_SHELL, 3, 1, 1))));
    }
}
