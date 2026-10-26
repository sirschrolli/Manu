package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

/**
 * Was man mit dem Pinsel aus dem Kies (bzw. Sand) eines Drachennests freilegt: Schuppen der Art, Eierschalen-
 * Fragmente, Goldkrümel aus dem Drachenhort, Knochen – und selten die Rune des Elements.
 */
public class ModArchaeologyLootProvider implements LootTableSubProvider {
    private final LootTableSubProvider.Context output;

    public ModArchaeologyLootProvider(LootTableSubProvider.Context output) {
        this.output = output;
    }

    @Override
    public void run() {
        ModLootTables.DRAGON_NEST_ARCHAEOLOGY.forEach((type, key) -> this.output.accept(key, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.DRAGON_SCALES.get(type).get()).setWeight(5))
                        .add(LootItem.lootTableItem(ModItems.DRACONIUM.get()).setWeight(2))
                        .add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(4))
                        .add(LootItem.lootTableItem(ModItems.GEMSTONE.get()).setWeight(2))
                        .add(LootItem.lootTableItem(Items.BONE).setWeight(4))
                        .add(LootItem.lootTableItem(ModItems.RUNES.get(type).get()).setWeight(1)))));
    }
}
