package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.stream.Stream;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Beute der Lebewesen. Drachen lassen ihre Schuppen über eigenen Code fallen (je nach Wachstumsstufe),
 * Soldaten nur die Ausrüstung, die ihnen der Spieler gegeben hat. Banditen tragen erbeutete Smaragde bei sich –
 * die fallen auch, wenn ein Soldat den Banditen besiegt. Wilde Kreaturen geben Fleisch, Leder, Federn,
 * Irrlichtstaub, Erddrachen-Schuppen und (selten) Edelsteine, Draconium oder eine Trollkeule.
 */
public class ModEntityLootProvider extends EntityLootSubProvider {
    public ModEntityLootProvider(LootTableSubProvider.Context output) {
        super(FeatureFlags.REGISTRY.allFlags(), output);
    }

    @Override
    public void generate() {
        ModEntities.DRAGONS.values().forEach(type -> this.add(type.get(), LootTable.lootTable()));
        ModEntities.SOLDIERS.values().forEach(type -> this.add(type.get(), LootTable.lootTable()));
        // Drachen-Bosse: viele Schuppen, Draconium und Edelsteine (das Ei erscheint gesondert auf dem Nest)
        this.add(ModEntities.PYRRHON.get(), LootTable.lootTable()
                .withPool(drop(ModItems.DRAGON_SCALES.get(DragonType.FIRE).get(), 18, 26))
                .withPool(drop(ModItems.DRACONIUM.get(), 3, 6))
                .withPool(drop(ModItems.GEMSTONE.get(), 2, 4)));
        this.add(ModEntities.GLACIARA.get(), LootTable.lootTable()
                .withPool(drop(ModItems.DRAGON_SCALES.get(DragonType.ICE).get(), 18, 26))
                .withPool(drop(ModItems.DRACONIUM.get(), 3, 6))
                .withPool(drop(ModItems.GEMSTONE.get(), 2, 4)));
        this.add(ModEntities.VERDANTIS.get(), LootTable.lootTable()
                .withPool(drop(ModItems.DRAGON_SCALES.get(DragonType.NATURE).get(), 18, 26))
                .withPool(drop(ModItems.DRACONIUM.get(), 3, 6))
                .withPool(drop(ModItems.GEMSTONE.get(), 2, 4))
                .withPool(drop(io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.LIFE_BLOSSOM.get(), 2, 4)));
        this.add(ModEntities.TERRAGON.get(), LootTable.lootTable()
                .withPool(drop(ModItems.DRAGON_SCALES.get(DragonType.EARTH).get(), 18, 26))
                .withPool(drop(ModItems.DRACONIUM.get(), 3, 6))
                .withPool(drop(ModItems.GEMSTONE.get(), 3, 6))
                .withPool(drop(io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.AMBER_CRYSTAL.get(), 4, 8)));
        this.add(ModEntities.VOLTARION.get(), LootTable.lootTable()
                .withPool(drop(ModItems.DRAGON_SCALES.get(DragonType.LIGHTNING).get(), 18, 26))
                .withPool(drop(ModItems.DRACONIUM.get(), 3, 6))
                .withPool(drop(ModItems.GEMSTONE.get(), 2, 4))
                .withPool(drop(io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.STORM_CRYSTAL.get(), 3, 6)));
        this.add(ModEntities.THALASSA.get(), LootTable.lootTable()
                .withPool(drop(ModItems.DRAGON_SCALES.get(DragonType.WATER).get(), 18, 26))
                .withPool(drop(ModItems.DRACONIUM.get(), 3, 6))
                .withPool(drop(ModItems.GEMSTONE.get(), 2, 4))
                .withPool(drop(io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.NACRE_BLOCK.get(), 3, 6))
                .withPool(drop(net.minecraft.world.item.Items.PRISMARINE_SHARD, 6, 12)));
        // Nyxar: Abyssit, Seelenkristalle und Schattenessenz (das Herz der Schatten erscheint gesondert)
        this.add(ModEntities.NYXAR.get(), LootTable.lootTable()
                .withPool(drop(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_SHARD.get(), 6, 10))
                .withPool(drop(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SOUL_CRYSTAL.get(), 8, 14))
                .withPool(drop(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SHADOW_ESSENCE.get(), 10, 16))
                .withPool(drop(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_UPGRADE_SMITHING_TEMPLATE.get(), 1, 2))
                .withPool(drop(ModItems.GEMSTONE.get(), 4, 8)));
        this.add(ModEntities.VINELING.get(), LootTable.lootTable()
                .withPool(drop(Items.VINE, 0, 2))
                .withPool(rare(io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.GLOWCAP.get(), 0.25f, 0.05f)));
        this.add(ModEntities.BANDIT.get(), banditLoot(false));
        this.add(ModEntities.BANDIT_ARCHER.get(), banditLoot(true));

        // Wilde Kreaturen
        this.add(ModEntities.WILD_BOAR.get(), LootTable.lootTable()
                .withPool(meat(Items.PORKCHOP, 1, 3))
                .withPool(drop(Items.LEATHER, 0, 2)));
        this.add(ModEntities.DEER.get(), LootTable.lootTable()
                .withPool(meat(ModItems.VENISON.get(), 1, 3))
                .withPool(drop(Items.LEATHER, 0, 2)));
        this.add(ModEntities.GOOSE.get(), LootTable.lootTable()
                .withPool(drop(Items.FEATHER, 1, 3))
                .withPool(meat(Items.CHICKEN, 1, 1)));
        this.add(ModEntities.WILL_O_WISP.get(), LootTable.lootTable()
                .withPool(drop(ModItems.WISP_DUST.get(), 1, 2)));
        this.add(ModEntities.CAVE_TROLL.get(), LootTable.lootTable()
                .withPool(drop(Items.LEATHER, 1, 3))
                .withPool(drop(Items.IRON_NUGGET, 3, 8))
                .withPool(drop(Items.BONE, 0, 2))
                .withPool(rare(ModItems.GEMSTONE.get(), 0.12f, 0.04f))
                .withPool(rare(ModItems.TROLL_CLUB.get(), 0.08f, 0.03f)));
        this.add(ModEntities.LINDWORM.get(), LootTable.lootTable()
                .withPool(drop(ModItems.DRAGON_SCALES.get(DragonType.EARTH).get(), 1, 3))
                .withPool(drop(Items.BONE, 1, 3))
                .withPool(rare(ModItems.DRACONIUM.get(), 0.1f, 0.03f)));

        // Kreaturen der Schattenwelt
        this.add(ModEntities.SHADE_STALKER.get(), LootTable.lootTable()
                .withPool(drop(ShadowItems.SHADOW_ESSENCE.get(), 0, 2))
                .withPool(rare(ShadowItems.SOUL_CRYSTAL.get(), 0.05f, 0.02f)));
        this.add(ModEntities.CRYSTAL_CRAWLER.get(), LootTable.lootTable()
                .withPool(drop(ShadowItems.NIGHT_CRYSTAL_SHARD.get(), 1, 3))
                .withPool(rare(ShadowItems.SOUL_CRYSTAL.get(), 0.04f, 0.02f)));
        this.add(ModEntities.NIGHT_WING.get(), LootTable.lootTable()
                .withPool(drop(Items.PHANTOM_MEMBRANE, 0, 2))
                .withPool(drop(ShadowItems.SHADOW_ESSENCE.get(), 0, 1)));
        this.add(ModEntities.MOON_STAG.get(), LootTable.lootTable()
                .withPool(meat(ModItems.VENISON.get(), 1, 3))
                .withPool(drop(Items.LEATHER, 0, 2))
                .withPool(drop(ShadowItems.NIGHT_CRYSTAL_SHARD.get(), 0, 1)));
        this.add(ModEntities.GLIMMER_MOTH.get(), LootTable.lootTable()
                .withPool(drop(Items.GLOWSTONE_DUST, 0, 1)));
        // Umbrer lassen nichts fallen (wie Dorfbewohner), Wächter ein wenig Mondsilber
        this.add(ModEntities.UMBRER.get(), LootTable.lootTable());
        this.add(ModEntities.UMBRER_GUARD.get(), LootTable.lootTable()
                .withPool(drop(ShadowItems.MOONSILVER_NUGGET.get(), 0, 3)));
    }

    /** Fleisch: wird gebraten, wenn das Tier brennend stirbt; Plünderung bringt mehr. */
    private LootPool.Builder meat(ItemLike item, int min, int max) {
        return LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(min, max)))
                        .apply(SmeltItemFunction.smelted().when(this.shouldSmeltLoot()))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.enchantments, ContextFloatProviders.between(0.0f, 1.0f))));
    }

    private LootPool.Builder drop(ItemLike item, int min, int max) {
        return LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(min, max)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.enchantments, ContextFloatProviders.between(0.0f, 1.0f))));
    }

    /** Seltene Beute – nur, wenn ein Spieler das Lebewesen besiegt. */
    private LootPool.Builder rare(ItemLike item, float chance, float lootingBonus) {
        return LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(item))
                .when(LootItemKilledByPlayerCondition.killedByPlayer())
                .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(this.enchantments, chance, lootingBonus));
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
