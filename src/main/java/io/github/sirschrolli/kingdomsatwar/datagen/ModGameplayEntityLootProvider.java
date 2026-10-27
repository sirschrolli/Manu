package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

/**
 * Beute, die zu Lebewesen gehört, aber keiner einzelnen Art: die Münzen besiegter Monster (eingebunden über einen globalen
 * Loot-Modifier, siehe {@link ModLootModifierProvider}).
 */
public class ModGameplayEntityLootProvider implements LootTableSubProvider {
    private final LootTableSubProvider.Context output;

    public ModGameplayEntityLootProvider(LootTableSubProvider.Context output) {
        this.output = output;
    }

    @Override
    public void run() {
        // Münzen von Monstern: oft ein paar Bronzemünzen, selten eine Silbermünze (Plünderung erhöht beides)
        var enchantments = this.output.lookup(net.minecraft.core.registries.Registries.ENCHANTMENT);
        output.accept(ModLootTables.MONSTER_COINS, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.BRONZE_COIN.get())
                                .apply(net.minecraft.world.level.storage.loot.functions.SetItemCountFunction.setCount(ContextIntProviders.between(1, 3))))
                        .when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition
                                .randomChanceAndLootingBoost(enchantments, 0.45f, 0.1f)))
                .withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.SILVER_COIN.get()))
                        .when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition
                                .randomChanceAndLootingBoost(enchantments, 0.04f, 0.02f))));
    }
}
