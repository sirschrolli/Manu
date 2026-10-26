package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Loot-Tables aller Blöcke: was beim Abbauen fallen gelassen wird.
 */
public class ModBlockLootProvider extends BlockLootSubProvider {
    public ModBlockLootProvider(LootTableSubProvider.Context context) {
        // Dracheneier fallen auch nach einer Explosion immer als Gegenstand heraus (wie das Vanilla-Drachenei).
        super(explosionResistantItems(), FeatureFlags.REGISTRY.allFlags(), context);
    }

    private static Set<Item> explosionResistantItems() {
        return ModBlocks.DRAGON_EGGS.values().stream()
                .map(egg -> egg.get().asItem())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected void generate() {
        // Baublöcke
        for (BuildingSet set : ModBlocks.BUILDING_SETS) {
            dropSelf(set.base().get());
            dropSelf(set.stairs().get());
            add(set.slab().get(), this::createSlabItemTable);
            if (set.wall() != null) {
                dropSelf(set.wall().get());
            }
        }
        dropSelf(ModBlocks.STONE_PILLAR.get());
        dropSelf(ModBlocks.TIMBER_BEAM.get());
        dropSelf(ModBlocks.TIMBER_FRAME.get());
        dropSelf(ModBlocks.TIMBER_FRAME_DIAGONAL.get());
        dropSelf(ModBlocks.TIMBER_FRAME_CROSS.get());
        dropSelf(ModBlocks.RECRUITMENT_TABLE.get());

        // Aschelande (eine Ascheschicht gibt so viele Schichten zurück, wie sie hoch ist)
        for (DeferredBlock<? extends Block> block : java.util.List.of(ModBlocks.ASH_BLOCK, ModBlocks.CINDER_SOIL,
                ModBlocks.GLOWING_VOLCANIC_ROCK, ModBlocks.CHARRED_LOG, ModBlocks.EMBER_VENT)) {
            dropSelf(block.get());
        }
        layers(ModBlocks.ASH_LAYER.get());

        // Schattenwelt
        for (DeferredBlock<? extends Block> block : java.util.List.of(ShadowBlocks.CRACKED_SHADOW_STONE_BRICKS,
                ShadowBlocks.CHISELED_SHADOW_STONE, ShadowBlocks.DEEP_SHADOW_STONE, ShadowBlocks.SHADOW_SOIL, ShadowBlocks.SHADOW_SAND,
                ShadowBlocks.ASH_SNOW_BLOCK, ShadowBlocks.SHADOW_LOG, ShadowBlocks.SHADOW_WOOD, ShadowBlocks.STRIPPED_SHADOW_LOG,
                ShadowBlocks.STRIPPED_SHADOW_WOOD, ShadowBlocks.NIGHT_CRYSTAL, ShadowBlocks.GLIMMER_MOSS, ShadowBlocks.GHOSTCAP)) {
            dropSelf(block.get());
        }
        layers(ShadowBlocks.ASH_SNOW.get());
        add(ShadowBlocks.SHADOW_GRASS.get(), block -> createSingleItemTableWithSilkTouch(block, ShadowBlocks.SHADOW_SOIL.get()));
        add(ShadowBlocks.SHADOW_LEAVES.get(), block -> createLeavesDrops(block, ShadowBlocks.SHADOW_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
        // Schätze der Schattenwelt
        ore(ShadowBlocks.MOONSILVER_ORE, ShadowItems.RAW_MOONSILVER.get());
        ore(ShadowBlocks.DEEP_MOONSILVER_ORE, ShadowItems.RAW_MOONSILVER.get());
        ore(ShadowBlocks.NIGHTSTEEL_ORE, ShadowItems.RAW_NIGHTSTEEL.get());
        ore(ShadowBlocks.DEEP_NIGHTSTEEL_ORE, ShadowItems.RAW_NIGHTSTEEL.get());
        ore(ShadowBlocks.SOUL_CRYSTAL_ORE, ShadowItems.SOUL_CRYSTAL.get());
        for (DeferredBlock<? extends Block> block : java.util.List.of(ShadowBlocks.ABYSSITE_ORE, ShadowBlocks.MOONSILVER_BLOCK,
                ShadowBlocks.NIGHTSTEEL_BLOCK, ShadowBlocks.SOUL_CRYSTAL_BLOCK, ShadowBlocks.ABYSSITE_BLOCK, ShadowBlocks.MOON_LANTERN,
                ShadowBlocks.SHADOW_SAPLING)) {
            dropSelf(block.get());
        }
        dropPottedContents(ShadowBlocks.POTTED_SHADOW_SAPLING.get());
        // Seelenbeerenstrauch: reif zwei bis drei Beeren, halbreif eine bis zwei, sonst eine (zum Umpflanzen)
        add(ShadowBlocks.SOUL_BERRY_BUSH.get(), block -> LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .add(AlternativesEntry.alternatives(io.github.sirschrolli.kingdomsatwar.block.SoulBerryBushBlock.AGE.getPossibleValues(),
                        age -> LootItem.lootTableItem(ShadowItems.SOUL_BERRIES.get())
                                .when(MatchBlock.blockMatches(this.blocks, block, StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(io.github.sirschrolli.kingdomsatwar.block.SoulBerryBushBlock.AGE, age.intValue())))
                                .apply(SetItemCountFunction.setCount(age == 3 ? ContextIntProviders.between(2, 3)
                                        : age == 2 ? ContextIntProviders.between(1, 2) : ContextIntProviders.exactly(1))))))));
        // Glimmerranken: nur mit Schere oder Behutsamkeit (wie Trauerranken)
        add(ShadowBlocks.GLIMMER_VINES.get(), block -> createShearsOrSilkTouchOnlyDrop(block));
        add(ShadowBlocks.GLIMMER_VINES_PLANT.get(), block -> createShearsOrSilkTouchOnlyDrop(ShadowBlocks.GLIMMER_VINES.get()));
        // Drusen: mit Behutsamkeit die Druse, sonst zwei bis vier Splitter (Glück hilft)
        add(ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get(), block -> createSilkTouchDispatchTable(block,
                (net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder<?>) applyExplosionDecay(block,
                        LootItem.lootTableItem(ShadowItems.NIGHT_CRYSTAL_SHARD.get())
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(2, 4)))
                                .apply(ApplyBonusCount.addOreBonusCount(this.enchantments.getOrThrow(Enchantments.FORTUNE))))));
        add(ShadowBlocks.SHADOW_SPROUTS.get(), block -> this.createShearsOnlyDrop(block));
        add(ShadowBlocks.PALE_GRASS.get(), block -> this.createShearsOnlyDrop(block));

        // Frostkessel (Gletschereis nur mit Behutsamkeit, wie Blaueis)
        for (DeferredBlock<? extends Block> block : java.util.List.of(ModBlocks.FROST_CRYSTAL, ModBlocks.FROZEN_SOIL, ModBlocks.ICICLE)) {
            dropSelf(block.get());
        }
        dropWhenSilkTouch(ModBlocks.GLACIER_ICE.get());

        // Uralter Hain (Laub wie Mangrovenlaub: Stöcke, mit Schere oder Behutsamkeit das Laub selbst)
        for (DeferredBlock<? extends Block> block : java.util.List.of(ModBlocks.ANCIENT_LOG, ModBlocks.GLOWING_MOSS, ModBlocks.GLOWCAP,
                ModBlocks.GNARLED_ROOTS)) {
            dropSelf(block.get());
        }
        add(ModBlocks.ANCIENT_LEAVES.get(), this::createMangroveLeavesDrops);
        add(ModBlocks.BRAMBLE.get(), block -> this.createShearsOnlyDrop(block));

        // Zerklüftete Weiten
        for (DeferredBlock<? extends Block> block : java.util.List.of(ModBlocks.AMBER_CRYSTAL, ModBlocks.AMBER_CLUSTER, ModBlocks.RUBBLE,
                ModBlocks.RIFT_STONE, ModBlocks.CHARGED_STORMSTONE, ModBlocks.FULGURITE, ModBlocks.STORM_CRYSTAL, ModBlocks.ABYSSAL_SAND,
                ModBlocks.GLOW_CORAL, ModBlocks.NACRE_BLOCK)) {
            dropSelf(block.get());
        }

        // Siegelstein (die heiligen Teile des Siegelkreises sind unzerstörbar und haben keine Beute)
        for (DeferredBlock<? extends Block> block : java.util.List.of(ModBlocks.CRACKED_SEALSTONE_BRICKS, ModBlocks.MOSSY_SEALSTONE_BRICKS,
                ModBlocks.POLISHED_SEALSTONE, ModBlocks.CHISELED_SEALSTONE)) {
            dropSelf(block.get());
        }

        // Einrichtung: Glas nur mit Behutsamkeit (wie Vanilla-Glas), Türen nur aus der unteren Hälfte
        dropWhenSilkTouch(ModBlocks.LEADED_GLASS.get());
        dropWhenSilkTouch(ModBlocks.LEADED_GLASS_PANE.get());
        add(ModBlocks.CASTLE_DOOR.get(), this::createDoorTable);
        for (DeferredBlock<? extends Block> block : java.util.List.of(ModBlocks.GREEN_SHUTTERS, ModBlocks.RED_SHUTTERS,
                ModBlocks.STEEL_LANTERN, ModBlocks.AETHERIT_LANTERN, ModBlocks.CHANDELIER, ModBlocks.OAK_CHAIR, ModBlocks.OAK_BENCH,
                ModBlocks.OAK_TABLE, ModBlocks.THRONE)) {
            dropSelf(block.get());
        }
        ModBlocks.BATTLEMENTS.keySet().forEach(battlement -> dropSelf(battlement.get()));

        // Erze: mit Behutsamkeit der Erzblock, sonst der Rohstoff (Glück erhöht die Menge)
        ore(ModBlocks.CARBON_ORE, ModItems.CARBON.get());
        ore(ModBlocks.DEEPSLATE_CARBON_ORE, ModItems.CARBON.get());
        ore(ModBlocks.GEMSTONE_ORE, ModItems.GEMSTONE.get());
        ore(ModBlocks.DEEPSLATE_GEMSTONE_ORE, ModItems.GEMSTONE.get());
        ore(ModBlocks.KONTERIT_ORE, ModItems.KONTERIT.get());
        ore(ModBlocks.DEEPSLATE_KONTERIT_ORE, ModItems.KONTERIT.get());
        ore(ModBlocks.AETHERIT_ORE, ModItems.AETHERIT.get());
        ore(ModBlocks.DEEPSLATE_AETHERIT_ORE, ModItems.AETHERIT.get());
        ore(ModBlocks.DRACONIUM_ORE, ModItems.DRACONIUM.get());
        ore(ModBlocks.DEEPSLATE_DRACONIUM_ORE, ModItems.DRACONIUM.get());
        ore(ModBlocks.WYVERNIT_ORE, ModItems.WYVERNIT.get());

        // Speicherblöcke
        dropSelf(ModBlocks.CARBON_BLOCK.get());
        dropSelf(ModBlocks.GEMSTONE_BLOCK.get());
        dropSelf(ModBlocks.KONTERIT_BLOCK.get());
        dropSelf(ModBlocks.AETHERIT_BLOCK.get());
        dropSelf(ModBlocks.DRACONIUM_BLOCK.get());
        dropSelf(ModBlocks.WYVERNIT_BLOCK.get());
        dropSelf(ModBlocks.STEEL_BLOCK.get());

        // Dracheneier
        ModBlocks.DRAGON_EGGS.values().forEach(egg -> dropSelf(egg.get()));

        // Feldfrüchte: reif = Frucht + zusätzliche Samen, unreif = nur Samen
        crop(ModBlocks.TOMATOES, ModItems.TOMATO.get(), ModItems.TOMATO_SEEDS.get());
        crop(ModBlocks.CABBAGES, ModItems.CABBAGE.get(), ModItems.CABBAGE_SEEDS.get());
        crop(ModBlocks.ONIONS, ModItems.ONION.get(), ModItems.ONION.get());
        crop(ModBlocks.BARLEY, ModItems.BARLEY.get(), ModItems.BARLEY_SEEDS.get());
        crop(ModBlocks.HOPS, ModItems.HOPS.get(), ModItems.HOPS.get());
        crop(ModBlocks.GRAPES, ModItems.GRAPES.get(), ModItems.GRAPES.get());
        crop(ModBlocks.GARLIC_CROP, ModItems.GARLIC.get(), ModItems.GARLIC.get());
        // Das Festmahl isst man auf, abgebaut bleibt nichts übrig (wie beim Kuchen)
        add(ModBlocks.FEAST.get(), noDrop());

        // Zierblumen und Blumentöpfe
        ModBlocks.FLOWERS.forEach((flower, pot) -> {
            dropSelf(flower.get());
            dropPottedContents(pot.get());
        });
    }

    /** Schichtblock (wie Schnee): so viele Schichten als Gegenstand, wie der Block hoch war. */
    private void layers(Block block) {
        add(block, LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .add(AlternativesEntry.alternatives(SnowLayerBlock.LAYERS.getPossibleValues(),
                        layers -> LootItem.lootTableItem(block)
                                .when(MatchBlock.blockMatches(this.blocks, block,
                                        StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, layers.intValue())))
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(layers))))))));
    }

    private void ore(DeferredBlock<? extends Block> ore, Item drop) {
        add(ore.get(), block -> createOreDrop(block, drop));
    }

    private void crop(DeferredBlock<? extends CropBlock> crop, Item produce, Item seeds) {
        add(crop.get(), createCropDrops(crop.get(), produce, seeds,
                MatchBlock.blockMatches(this.blocks, crop.get(),
                        StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, CropBlock.MAX_AGE))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(DeferredHolder::value).map(Block.class::cast).toList();
    }
}
