package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

/**
 * Zusätzliche Loot-Tables: Beim Abbauen von Gras können neue Samen und Pflanzknollen fallen (zusammen etwa 11 %).
 * Eingebunden wird die Tabelle über einen globalen Loot-Modifier (siehe {@link ModLootModifierProvider}).
 */
public class ModExtraLootProvider implements LootTableSubProvider {
    private final LootTableSubProvider.Context output;

    public ModExtraLootProvider(LootTableSubProvider.Context output) {
        this.output = output;
    }

    @Override
    public void run() {
        output.accept(ModLootTables.GRASS_SEEDS, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.TOMATO_SEEDS.get()).setWeight(1))
                        .add(LootItem.lootTableItem(ModItems.CABBAGE_SEEDS.get()).setWeight(1))
                        .add(LootItem.lootTableItem(ModItems.BARLEY_SEEDS.get()).setWeight(1))
                        .add(LootItem.lootTableItem(ModItems.ONION.get()).setWeight(1))
                        .add(LootItem.lootTableItem(ModItems.HOPS.get()).setWeight(1))
                        .add(LootItem.lootTableItem(ModItems.GRAPES.get()).setWeight(1))
                        .add(LootItem.lootTableItem(ModItems.GARLIC.get()).setWeight(1))
                        .add(EmptyLootItem.emptyItem().setWeight(56))
                        .when(ExplosionCondition.survivesExplosion())));
    }
}
