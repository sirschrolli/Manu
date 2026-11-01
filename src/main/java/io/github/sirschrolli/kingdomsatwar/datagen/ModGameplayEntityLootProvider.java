package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

/**
 * Beute, die zu Lebewesen gehört, aber keiner einzelnen Art: die Münzen besiegter Monster und die Froschlichter der eigenen
 * Frösche (eingebunden über globale Loot-Modifier, siehe {@link ModLootModifierProvider}).
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
        // Froschlichter wie bei Vanilla – Vanilla fragt nur nach dem Vanilla-Frosch, die eigenen Frösche sind eine eigene Art
        var types = this.output.lookup(net.minecraft.core.registries.Registries.ENTITY_TYPE);
        var variants = this.output.lookup(net.minecraft.core.registries.Registries.FROG_VARIANT);
        output.accept(ModLootTables.FROGLIGHTS, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(net.minecraft.world.item.Items.PEARLESCENT_FROGLIGHT)
                                .when(eatenByFrog(types, variants, net.minecraft.world.entity.animal.frog.FrogVariants.WARM)))
                        .add(LootItem.lootTableItem(net.minecraft.world.item.Items.VERDANT_FROGLIGHT)
                                .when(eatenByFrog(types, variants, net.minecraft.world.entity.animal.frog.FrogVariants.COLD)))
                        .add(LootItem.lootTableItem(net.minecraft.world.item.Items.OCHRE_FROGLIGHT)
                                .when(eatenByFrog(types, variants, net.minecraft.world.entity.animal.frog.FrogVariants.TEMPERATE)))));
    }

    private static net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder eatenByFrog(
            net.minecraft.core.HolderGetter<net.minecraft.world.entity.EntityType<?>> types,
            net.minecraft.core.HolderGetter<net.minecraft.world.entity.animal.frog.FrogVariant> variants,
            net.minecraft.resources.ResourceKey<net.minecraft.world.entity.animal.frog.FrogVariant> variant) {
        return net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition.hasDamageSource(
                net.minecraft.advancements.predicates.DamageSourcePredicate.Builder.damageType().source(
                        net.minecraft.advancements.predicates.entity.EntityPredicate.Builder.entity()
                                .of(types, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.FROG.get())
                                .components(net.minecraft.core.component.DataComponentExactPredicate.expect(
                                        net.minecraft.core.component.DataComponents.FROG_VARIANT, variants.getOrThrow(variant)))));
    }
}
