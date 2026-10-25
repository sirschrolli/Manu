package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
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

        // Zierblumen und Blumentöpfe
        ModBlocks.FLOWERS.forEach((flower, pot) -> {
            dropSelf(flower.get());
            dropPottedContents(pot.get());
        });
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
