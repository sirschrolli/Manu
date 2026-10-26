package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * Block-Tags: Werkzeug zum Abbauen, benötigte Werkzeugstufe, Treppen/Stufen/Mauern, Erze usw.
 */
public class ModBlockTagsProvider extends BlockTagsProvider {
    private static final java.util.List<TagKey<Block>> ORE_TAGS = java.util.List.of(
            ModTags.Blocks.ORES_CARBON, ModTags.Blocks.ORES_GEMSTONE, ModTags.Blocks.ORES_KONTERIT,
            ModTags.Blocks.ORES_AETHERIT, ModTags.Blocks.ORES_DRACONIUM, ModTags.Blocks.ORES_WYVERNIT);

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, KingdomsAtWar.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        // --- Baublöcke ---
        for (BuildingSet set : ModBlocks.BUILDING_SETS) {
            TagKey<Block> tool = set.tool() == BuildingSet.Tool.AXE ? BlockTags.MINEABLE_WITH_AXE : BlockTags.MINEABLE_WITH_PICKAXE;
            set.all().forEach(block -> add(tool, block));
            add(BlockItemTags.STAIRS.block(), set.stairs());
            add(BlockItemTags.SLABS.block(), set.slab());
            if (set.wall() != null) {
                add(BlockItemTags.WALLS.block(), set.wall());
            }
        }
        ModBlocks.COPPER_ROOF.all().forEach(block -> add(BlockTags.NEEDS_STONE_TOOL, block));
        add(BlockTags.MINEABLE_WITH_PICKAXE, ModBlocks.STONE_PILLAR);
        add(BlockTags.MINEABLE_WITH_AXE, ModBlocks.TIMBER_BEAM, ModBlocks.TIMBER_FRAME,
                ModBlocks.TIMBER_FRAME_DIAGONAL, ModBlocks.TIMBER_FRAME_CROSS);

        // --- Aschelande ---
        add(BlockTags.MINEABLE_WITH_SHOVEL, ModBlocks.ASH_BLOCK, ModBlocks.ASH_LAYER, ModBlocks.CINDER_SOIL);
        add(BlockTags.MINEABLE_WITH_PICKAXE, ModBlocks.GLOWING_VOLCANIC_ROCK, ModBlocks.EMBER_VENT);
        add(BlockTags.MINEABLE_WITH_AXE, ModBlocks.CHARRED_LOG);
        add(BlockTags.SUPPORTS_VEGETATION, ModBlocks.CINDER_SOIL);
        add(BlockTags.SUPPORTS_DRY_VEGETATION, ModBlocks.CINDER_SOIL);
        add(BlockTags.INFINIBURN_OVERWORLD, ModBlocks.GLOWING_VOLCANIC_ROCK, ModBlocks.EMBER_VENT);
        add(BlockTags.DRAGON_IMMUNE, ModBlocks.DRAGON_LAIR_CORE);
        add(BlockTags.WITHER_IMMUNE, ModBlocks.DRAGON_LAIR_CORE);

        // --- Siegelkreis ---
        add(BlockTags.MINEABLE_WITH_PICKAXE, ModBlocks.CRACKED_SEALSTONE_BRICKS, ModBlocks.MOSSY_SEALSTONE_BRICKS,
                ModBlocks.POLISHED_SEALSTONE, ModBlocks.CHISELED_SEALSTONE);
        add(BlockTags.DRAGON_IMMUNE, ModBlocks.SEAL_MONOLITH, ModBlocks.SEAL_RUNE, ModBlocks.SEAL_INLAY, ModBlocks.SEAL_SOCKET,
                ModBlocks.SEAL_PLATE, ModBlocks.SHADOW_PORTAL);
        add(BlockTags.WITHER_IMMUNE, ModBlocks.SEAL_MONOLITH, ModBlocks.SEAL_RUNE, ModBlocks.SEAL_INLAY, ModBlocks.SEAL_SOCKET,
                ModBlocks.SEAL_PLATE, ModBlocks.SHADOW_PORTAL);

        // --- Einrichtung ---
        add(Tags.BlockItems.GLASS_BLOCKS.block(), ModBlocks.LEADED_GLASS);
        add(BlockTags.IMPERMEABLE, ModBlocks.LEADED_GLASS);
        add(Tags.BlockItems.GLASS_PANES.block(), ModBlocks.LEADED_GLASS_PANE);
        add(BlockItemTags.WOODEN_DOORS.block(), ModBlocks.CASTLE_DOOR);
        add(BlockItemTags.WOODEN_TRAPDOORS.block(), ModBlocks.GREEN_SHUTTERS, ModBlocks.RED_SHUTTERS);
        add(BlockTags.MINEABLE_WITH_AXE, ModBlocks.CASTLE_DOOR, ModBlocks.GREEN_SHUTTERS, ModBlocks.RED_SHUTTERS,
                ModBlocks.OAK_CHAIR, ModBlocks.OAK_BENCH, ModBlocks.OAK_TABLE, ModBlocks.THRONE);
        add(BlockTags.MINEABLE_WITH_PICKAXE, ModBlocks.STEEL_LANTERN, ModBlocks.AETHERIT_LANTERN, ModBlocks.CHANDELIER);
        ModBlocks.BATTLEMENTS.keySet().forEach(battlement -> add(BlockTags.MINEABLE_WITH_PICKAXE, battlement));

        // --- Erze ---
        add(BlockTags.MINEABLE_WITH_PICKAXE,
                ModBlocks.CARBON_ORE, ModBlocks.DEEPSLATE_CARBON_ORE,
                ModBlocks.GEMSTONE_ORE, ModBlocks.DEEPSLATE_GEMSTONE_ORE,
                ModBlocks.KONTERIT_ORE, ModBlocks.DEEPSLATE_KONTERIT_ORE,
                ModBlocks.AETHERIT_ORE, ModBlocks.DEEPSLATE_AETHERIT_ORE,
                ModBlocks.DRACONIUM_ORE, ModBlocks.DEEPSLATE_DRACONIUM_ORE,
                ModBlocks.WYVERNIT_ORE);
        add(BlockTags.NEEDS_IRON_TOOL,
                ModBlocks.GEMSTONE_ORE, ModBlocks.DEEPSLATE_GEMSTONE_ORE,
                ModBlocks.KONTERIT_ORE, ModBlocks.DEEPSLATE_KONTERIT_ORE,
                ModBlocks.AETHERIT_ORE, ModBlocks.DEEPSLATE_AETHERIT_ORE,
                ModBlocks.WYVERNIT_ORE);
        add(BlockTags.NEEDS_DIAMOND_TOOL, ModBlocks.DRACONIUM_ORE, ModBlocks.DEEPSLATE_DRACONIUM_ORE);

        add(ModTags.Blocks.ORES_CARBON, ModBlocks.CARBON_ORE, ModBlocks.DEEPSLATE_CARBON_ORE);
        add(ModTags.Blocks.ORES_GEMSTONE, ModBlocks.GEMSTONE_ORE, ModBlocks.DEEPSLATE_GEMSTONE_ORE);
        add(ModTags.Blocks.ORES_KONTERIT, ModBlocks.KONTERIT_ORE, ModBlocks.DEEPSLATE_KONTERIT_ORE);
        add(ModTags.Blocks.ORES_AETHERIT, ModBlocks.AETHERIT_ORE, ModBlocks.DEEPSLATE_AETHERIT_ORE);
        add(ModTags.Blocks.ORES_DRACONIUM, ModBlocks.DRACONIUM_ORE, ModBlocks.DEEPSLATE_DRACONIUM_ORE);
        add(ModTags.Blocks.ORES_WYVERNIT, ModBlocks.WYVERNIT_ORE);
        for (TagKey<Block> oreTag : ORE_TAGS) {
            tag(Tags.BlockItems.ORES.block()).addTag(oreTag);
            tag(BlockItemTags.ORES.block()).addTag(oreTag);
            tag(Tags.BlockItems.ORE_RATES_SINGULAR.block()).addTag(oreTag);
        }
        add(Tags.BlockItems.ORES_IN_GROUND_STONE.block(), ModBlocks.CARBON_ORE, ModBlocks.GEMSTONE_ORE, ModBlocks.KONTERIT_ORE,
                ModBlocks.AETHERIT_ORE, ModBlocks.DRACONIUM_ORE, ModBlocks.WYVERNIT_ORE);
        add(Tags.BlockItems.ORES_IN_GROUND_DEEPSLATE.block(), ModBlocks.DEEPSLATE_CARBON_ORE, ModBlocks.DEEPSLATE_GEMSTONE_ORE,
                ModBlocks.DEEPSLATE_KONTERIT_ORE, ModBlocks.DEEPSLATE_AETHERIT_ORE, ModBlocks.DEEPSLATE_DRACONIUM_ORE);

        // --- Speicherblöcke ---
        add(BlockTags.MINEABLE_WITH_PICKAXE, ModBlocks.CARBON_BLOCK, ModBlocks.GEMSTONE_BLOCK, ModBlocks.KONTERIT_BLOCK,
                ModBlocks.AETHERIT_BLOCK, ModBlocks.DRACONIUM_BLOCK, ModBlocks.WYVERNIT_BLOCK, ModBlocks.STEEL_BLOCK);
        add(BlockTags.NEEDS_STONE_TOOL, ModBlocks.STEEL_BLOCK);
        add(BlockTags.NEEDS_IRON_TOOL, ModBlocks.GEMSTONE_BLOCK, ModBlocks.KONTERIT_BLOCK, ModBlocks.AETHERIT_BLOCK,
                ModBlocks.WYVERNIT_BLOCK);
        add(BlockTags.NEEDS_DIAMOND_TOOL, ModBlocks.DRACONIUM_BLOCK);
        add(BlockTags.BEACON_BASE_BLOCKS, ModBlocks.STEEL_BLOCK, ModBlocks.GEMSTONE_BLOCK, ModBlocks.DRACONIUM_BLOCK);

        add(ModTags.Blocks.STORAGE_BLOCKS_CARBON, ModBlocks.CARBON_BLOCK);
        add(ModTags.Blocks.STORAGE_BLOCKS_GEMSTONE, ModBlocks.GEMSTONE_BLOCK);
        add(ModTags.Blocks.STORAGE_BLOCKS_KONTERIT, ModBlocks.KONTERIT_BLOCK);
        add(ModTags.Blocks.STORAGE_BLOCKS_AETHERIT, ModBlocks.AETHERIT_BLOCK);
        add(ModTags.Blocks.STORAGE_BLOCKS_DRACONIUM, ModBlocks.DRACONIUM_BLOCK);
        add(ModTags.Blocks.STORAGE_BLOCKS_WYVERNIT, ModBlocks.WYVERNIT_BLOCK);
        add(ModTags.Blocks.STORAGE_BLOCKS_STEEL, ModBlocks.STEEL_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS.block()).addTag(ModTags.Blocks.STORAGE_BLOCKS_CARBON)
                .addTag(ModTags.Blocks.STORAGE_BLOCKS_GEMSTONE).addTag(ModTags.Blocks.STORAGE_BLOCKS_KONTERIT)
                .addTag(ModTags.Blocks.STORAGE_BLOCKS_AETHERIT).addTag(ModTags.Blocks.STORAGE_BLOCKS_DRACONIUM)
                .addTag(ModTags.Blocks.STORAGE_BLOCKS_WYVERNIT).addTag(ModTags.Blocks.STORAGE_BLOCKS_STEEL);

        // --- Dracheneier ---
        ModBlocks.DRAGON_EGGS.values().forEach(egg -> {
            add(ModTags.Blocks.DRAGON_EGGS, egg);
            add(BlockTags.MINEABLE_WITH_PICKAXE, egg);
        });
        tag(BlockTags.DRAGON_IMMUNE).addTag(ModTags.Blocks.DRAGON_EGGS);

        // --- Pflanzen ---
        ModBlocks.CROPS.forEach(crop -> {
            add(BlockTags.CROPS, crop);
            add(BlockTags.MAINTAINS_FARMLAND, crop);
            add(BlockTags.BEE_GROWABLES, crop);
        });
        ModBlocks.FLOWERS.forEach((flower, pot) -> {
            add(BlockItemTags.SMALL_FLOWERS.block(), flower);
            add(BlockTags.FLOWER_POTS, pot);
        });
    }

    @SafeVarargs
    private void add(TagKey<Block> tag, DeferredBlock<? extends Block>... blocks) {
        TagAppender<Block> appender = tag(tag);
        for (DeferredBlock<? extends Block> block : blocks) {
            appender.add(block.key());
        }
    }
}
