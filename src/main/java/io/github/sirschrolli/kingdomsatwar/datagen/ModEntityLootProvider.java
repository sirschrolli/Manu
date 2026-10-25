package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.stream.Stream;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Beute der Lebewesen. Drachen lassen ihre Schuppen über eigenen Code fallen (je nach Wachstumsstufe),
 * Soldaten nur die Ausrüstung, die ihnen der Spieler gegeben hat. Banditen tragen erbeutete Smaragde bei sich –
 * die fallen auch, wenn ein Soldat den Banditen besiegt.
 */
public class ModEntityLootProvider extends EntityLootSubProvider {
    public ModEntityLootProvider(LootTableSubProvider.Context output) {
        super(FeatureFlags.REGISTRY.allFlags(), output);
    }

    @Override
    public void generate() {
        ModEntities.DRAGONS.values().forEach(type -> this.add(type.get(), LootTable.lootTable()));
        ModEntities.SOLDIERS.values().forEach(type -> this.add(type.get(), LootTable.lootTable()));
        this.add(ModEntities.BANDIT.get(), banditLoot(false));
        this.add(ModEntities.BANDIT_ARCHER.get(), banditLoot(true));
    }

    private LootTable.Builder banditLoot(boolean archer) {
        LootTable.Builder table = LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.EMERALD)
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(0, 2)))
                                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.enchantments, ContextFloatProviders.between(0.0f, 1.0f)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(3)
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 4))))
                        .add(LootItem.lootTableItem(Items.IRON_NUGGET).setWeight(3)
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 5))))
                        .add(LootItem.lootTableItem(Items.BREAD).setWeight(2))
                        .add(LootItem.lootTableItem(Items.LEATHER).setWeight(2))
                        .add(EmptyLootItem.emptyItem().setWeight(4)));
        if (archer) {
            table.withPool(LootPool.lootPool()
                    .setRolls(ContextIntProviders.exactly(1))
                    .add(LootItem.lootTableItem(Items.ARROW)
                            .apply(SetItemCountFunction.setCount(ContextIntProviders.between(0, 3)))
                            .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.enchantments, ContextFloatProviders.between(0.0f, 1.0f)))));
        }
        return table;
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return ModEntities.ENTITIES.getEntries().stream().map(DeferredHolder::value);
    }
}
