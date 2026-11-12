package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import io.github.sirschrolli.kingdomsatwar.registry.StoneChains;
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
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
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
        // Herz- und Manakristalle in den Höhlen geben den Kristall zum Aufnehmen
        dropOther(ModBlocks.HEART_CRYSTAL_FORMATION.get(), ModItems.HEART_CRYSTAL.get());
        dropOther(ModBlocks.MANA_CRYSTAL_FORMATION.get(), ModItems.MANA_CRYSTAL.get());
        dropSelf(ModBlocks.TIMBER_BEAM.get());
        dropSelf(ModBlocks.TIMBER_FRAME.get());
        dropSelf(ModBlocks.TIMBER_FRAME_DIAGONAL.get());
        dropSelf(ModBlocks.TIMBER_FRAME_CROSS.get());
        dropSelf(ModBlocks.RECRUITMENT_TABLE.get());
        dropSelf(ModBlocks.MAGIC_TABLE.get());
        add(ModBlocks.TOWN_BANNER.get(), this::createDoorTable);
        // die Söldnertafel hat keinen Gegenstand: abgebaut bleiben Stöcke und ein paar Anschläge
        add(ModBlocks.MERCENARY_BOARD.get(), block -> LootTable.lootTable()
                .withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(net.minecraft.world.item.Items.STICK)
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(3))))))
                .withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(net.minecraft.world.item.Items.PAPER)
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 3)))))));

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
        // Schattenhalme: mit der Schere die Halme, sonst manchmal Dämmerkorn-Saat (wie Weizensaat aus Gras)
        add(ShadowBlocks.SHADOW_SPROUTS.get(), block -> this.createShearsDispatchTable(block,
                (net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder<?>) this.applyExplosionDecay(block,
                        LootItem.lootTableItem(ShadowItems.DUSKGRAIN_SEEDS.get())
                                .when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.randomChance(0.125f))
                                .apply(ApplyBonusCount.addUniformBonusCount(this.enchantments.getOrThrow(Enchantments.FORTUNE), 2)))));
        add(ShadowBlocks.PALE_GRASS.get(), block -> this.createShearsOnlyDrop(block));
        // Mondfarn und Seelenschilf aus der unteren Hälfte (wie hohe Blumen und Schilf), Sternblüte immer
        for (DeferredBlock<? extends Block> tall : java.util.List.of(ShadowBlocks.MOON_FERN, ShadowBlocks.SOUL_REEDS)) {
            add(tall.get(), block -> this.createSinglePropConditionTable(block, net.minecraft.world.level.block.DoublePlantBlock.HALF,
                    net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER));
        }
        dropSelf(ShadowBlocks.STAR_BLOSSOM.get());

        // Die Leere
        // das Meer: die Tiefseelaterne fällt als sie selbst
        this.dropSelf(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.DEEP_SEA_LANTERN.get());
        this.dropSelf(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.CHISELED_TIDE_STONE.get());
        sea();
        fae();
        seaWoods();
        for (DeferredBlock<? extends Block> block : java.util.List.of(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CRACKED_VOID_STONE_BRICKS,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CHISELED_VOID_STONE,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.ASTRAL_PILLAR,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CHISELED_ASTRAL_MARBLE,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.STARDUST,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_CRYSTAL,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_BLOOM)) {
            dropSelf(block.get());
        }
        // Der Abgrund
        for (DeferredBlock<? extends Block> block : java.util.List.of(io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.CRACKED_CHAOS_STONE_BRICKS,
                io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.CHISELED_CHAOS_STONE,
                io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.WORLD_SEAM,
                io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.FLOAT_CRYSTAL,
                io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.LIFT_CRYSTAL,
                io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.WORLD_EATER_TROPHY)) {
            dropSelf(block.get());
        }
        // Leerenmoos: mit Behutsamkeit das Moos, sonst der Stein darunter
        add(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_MOSS.get(), block -> createSingleItemTableWithSilkTouch(block,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_STONE.base().get()));
        // Kristalllaub: mit Schere oder Behutsamkeit das Laub, sonst manchmal ein Leerensplitter
        add(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CRYSTAL_FOLIAGE.get(), block -> createSilkTouchOrShearsDispatchTable(block,
                (net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder<?>) applyExplosionDecay(block,
                        LootItem.lootTableItem(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_SHARD.get())
                                .when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.randomChance(0.12f)))));
        add(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.STAR_VINES.get(), block -> createShearsOrSilkTouchOnlyDrop(block));
        add(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.STAR_VINES_PLANT.get(),
                block -> createShearsOrSilkTouchOnlyDrop(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.STAR_VINES.get()));
        add(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_CRYSTAL_CLUSTER.get(), block -> createSilkTouchDispatchTable(block,
                (net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder<?>) applyExplosionDecay(block,
                        LootItem.lootTableItem(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_SHARD.get())
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 3)))
                                .apply(ApplyBonusCount.addOreBonusCount(this.enchantments.getOrThrow(Enchantments.FORTUNE))))));
        // Schattenholz-Tür, -Falltür, -Zaun und -Zauntor
        add(ShadowBlocks.SHADOW_DOOR.get(), this::createDoorTable);
        for (DeferredBlock<? extends Block> block : java.util.List.of(ShadowBlocks.SHADOW_TRAPDOOR, ShadowBlocks.SHADOW_FENCE,
                ShadowBlocks.SHADOW_FENCE_GATE)) {
            dropSelf(block.get());
        }
        // Ackerland und Pfad geben Schattenerde zurück
        dropOther(ShadowBlocks.SHADOW_FARMLAND.get(), ShadowBlocks.SHADOW_SOIL.get());
        dropOther(ShadowBlocks.SHADOW_PATH.get(), ShadowBlocks.SHADOW_SOIL.get());
        // Dämmerkorn: reif Ähren und Saat (Glück bringt mehr Saat), sonst nur die Saat
        add(ShadowBlocks.DUSKGRAIN.get(), createCropDrops(ShadowBlocks.DUSKGRAIN.get(), ShadowItems.DUSKGRAIN.get(),
                ShadowItems.DUSKGRAIN_SEEDS.get(), MatchBlock.blockMatches(this.blocks, ShadowBlocks.DUSKGRAIN.get(),
                        StatePropertiesPredicate.Builder.properties().hasProperty(net.minecraft.world.level.block.CropBlock.AGE, 7))));

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

        // Eigene Oberwelt-Biome: Torf; Schilf nur aus der unteren Hälfte (wie hohe Blumen)
        dropSelf(ModBlocks.PEAT.get());
        dropSelf(ModBlocks.GOLD_LEAF_BLOCK.get());
        add(ModBlocks.REEDS.get(), block -> this.createSinglePropConditionTable(block, net.minecraft.world.level.block.DoublePlantBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER));

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

        // Gestein gibt Bruchstein (mit Behutsamkeit sich selbst) – wie Stein und Bruchstein
        for (StoneChains.Chain chain : StoneChains.all()) {
            add(chain.stone().get(), block -> createSingleItemTableWithSilkTouch(block, chain.cobble().base().get()));
        }
        // Geröll gibt wie Kies manchmal Feuerstein
        add(ModBlocks.RUBBLE.get(), block -> createSilkTouchDispatchTable(block, applyExplosionCondition(block,
                LootItem.lootTableItem(net.minecraft.world.item.Items.FLINT)
                        .when(BonusLevelTableCondition.bonusLevelFlatChance(this.enchantments.getOrThrow(Enchantments.FORTUNE),
                                0.1f, 0.14285715f, 0.25f, 1.0f))
                        .otherwise(LootItem.lootTableItem(block)))));
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

    /**
     * Das Feenreich (0.63): Gestein, Holz, Pilze und Blumen wie gewohnt; Feengras gibt Feenerde, Laub manchmal Setzlinge,
     * Halme, Leuchtranken und Dornenhecken nur mit Schere oder Behutsamkeit; die Sternspitze aus der unteren Hälfte.
     */
    /** Die Meereshölzer (0.92): alles gibt sich selbst, das Laub manchmal einen Setzling, die Tür nur aus der unteren Hälfte. */
    private void seaWoods() {
        for (io.github.sirschrolli.kingdomsatwar.registry.SeaWoods.SeaWood wood : io.github.sirschrolli.kingdomsatwar.registry.SeaWoods.ALL) {
            for (DeferredBlock<? extends Block> block : java.util.List.of(wood.log, wood.wood, wood.strippedLog, wood.strippedWood, wood.trapdoor,
                    wood.fence, wood.fenceGate, wood.sapling)) {
                dropSelf(block.get());
            }
            add(wood.leaves.get(), block -> createLeavesDrops(block, wood.sapling.get(), NORMAL_LEAVES_SAPLING_CHANCES));
            dropPottedContents(wood.pottedSapling.get());
            add(wood.door.get(), this::createDoorTable);
        }
    }

    private void fae() {
        ore(FaeBlocks.MOONSTONE_ORE, FaeBlocks.MOONSTONE.get());
        for (DeferredBlock<? extends Block> block : java.util.List.of(FaeBlocks.MOSSY_FAESTONE_BRICKS, FaeBlocks.CHISELED_MOONSTONE,
                FaeBlocks.MOONSTONE_BLOCK,
                FaeBlocks.FAE_SOIL, FaeBlocks.FAE_MOSS, FaeBlocks.GLIMMERWOOD_LOG, FaeBlocks.GLIMMERWOOD_WOOD,
                FaeBlocks.STRIPPED_GLIMMERWOOD_LOG, FaeBlocks.STRIPPED_GLIMMERWOOD_WOOD, FaeBlocks.GLIMMERWOOD_TRAPDOOR,
                FaeBlocks.GLIMMERWOOD_FENCE, FaeBlocks.GLIMMERWOOD_FENCE_GATE, FaeBlocks.GLIMMERWOOD_SAPLING, FaeBlocks.FAE_MUSHROOM_STEM,
                FaeBlocks.ROSE_MUSHROOM_CAP, FaeBlocks.AZURE_MUSHROOM_CAP, FaeBlocks.VIOLET_MUSHROOM_CAP, FaeBlocks.MOONBELL,
                FaeBlocks.FAE_ROSE, FaeBlocks.LANTERN_MUSHROOM, FaeBlocks.THORNBRIAR, FaeBlocks.MOON_LILY)) {
            dropSelf(block.get());
        }
        add(FaeBlocks.FAE_GRASS.get(), block -> createSingleItemTableWithSilkTouch(block, FaeBlocks.FAE_SOIL.get()));
        add(FaeBlocks.ROSE_GLIMMER_LEAVES.get(), block -> createLeavesDrops(block, FaeBlocks.GLIMMERWOOD_SAPLING.get(),
                NORMAL_LEAVES_SAPLING_CHANCES));
        add(FaeBlocks.AZURE_GLIMMER_LEAVES.get(), block -> createLeavesDrops(block, FaeBlocks.GLIMMERWOOD_SAPLING.get(),
                NORMAL_LEAVES_SAPLING_CHANCES));
        dropPottedContents(FaeBlocks.POTTED_GLIMMERWOOD_SAPLING.get());
        add(FaeBlocks.GLIMMERWOOD_DOOR.get(), this::createDoorTable);
        add(FaeBlocks.FAE_SPROUTS.get(), block -> this.createShearsOnlyDrop(block));
        add(FaeBlocks.STARLACE.get(), block -> this.createSinglePropConditionTable(block, net.minecraft.world.level.block.DoublePlantBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER));
        add(FaeBlocks.GLOW_VINES.get(), block -> createShearsOrSilkTouchOnlyDrop(block));
        add(FaeBlocks.GLOW_VINES_PLANT.get(), block -> createShearsOrSilkTouchOnlyDrop(FaeBlocks.GLOW_VINES.get()));
        add(FaeBlocks.THORN_HEDGE.get(), block -> createShearsOrSilkTouchOnlyDrop(block));
        dropSelf(FaeBlocks.MOTH_SILK.get());
        dropSelf(io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks.VELVET_CURTAIN.get());
        dropSelf(io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks.VELVET_CURTAIN_FRINGE.get());
        dropSelf(io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.MISSING_BLOCK.get());
        dropSelf(io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_ALTAR.get());
        dropSelf(io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.STATIC_BLOCK.get());
        add(FaeBlocks.SILK_STRANDS.get(), block -> createSilkTouchOrShearsDispatchTable(block, this.applyExplosionDecay(block,
                LootItem.lootTableItem(net.minecraft.world.item.Items.STRING)
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(0, 1))))));
    }

    /** Der Meeresgrund (0.62): Anemonen, Röhrenwürmer, Gestein; Muscheln so viele, wie liegen; Algen nur mit Schere. */
    private void sea() {
        for (var anemone : io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.anemones()) {
            dropSelf(anemone.get());
        }
        for (var shells : io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.shells()) {
            add(shells.get(), block -> LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                    .add(this.applyExplosionDecay(block, LootItem.lootTableItem(block)
                            .apply(java.util.List.of(2, 3), count -> SetItemCountFunction.setCount(ContextIntProviders.exactly(count))
                                    .when(MatchBlock.blockMatches(this.blocks, block, StatePropertiesPredicate.Builder.properties()
                                            .hasProperty(io.github.sirschrolli.kingdomsatwar.block.sea.SeashellBlock.SHELLS, count.intValue())))))))
                    // seit 0.93: in mancher Muschel steckt eine Perle (die Währung des Meervolks)
                    .withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                            .add(LootItem.lootTableItem(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.PEARL.get()))
                            .when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.randomChance(0.12f))));
        }
        dropSelf(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.TUBE_WORMS.get());
        dropSelf(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.VENT_ROCK.get());
        dropSelf(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.SULFUR_CRUST.get());
        add(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.HYDROTHERMAL_VENT.get(), block -> this.createSingleItemTableWithSilkTouch(block,
                io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.VENT_ROCK.get()));
        add(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.GLOW_ALGAE.get(), block -> this.createShearsOrSilkTouchOnlyDrop(block));
        dropSelf(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.GIANT_KELP.get());
        dropOther(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.GIANT_KELP_PLANT.get(),
                io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.GIANT_KELP.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(DeferredHolder::value).map(Block.class::cast).toList();
    }
}
