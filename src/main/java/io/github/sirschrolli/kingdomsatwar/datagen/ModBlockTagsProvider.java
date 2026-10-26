package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import java.util.List;
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
        motionBlocking();
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

        // --- Frostkessel ---
        add(BlockTags.MINEABLE_WITH_PICKAXE, ModBlocks.FROST_CRYSTAL, ModBlocks.GLACIER_ICE, ModBlocks.ICICLE);
        add(BlockTags.MINEABLE_WITH_SHOVEL, ModBlocks.FROZEN_SOIL);
        add(BlockTags.ICE, ModBlocks.GLACIER_ICE);
        add(BlockTags.SUPPORTS_VEGETATION, ModBlocks.FROZEN_SOIL);

        // --- Uralter Hain ---
        add(BlockTags.MINEABLE_WITH_AXE, ModBlocks.ANCIENT_LOG, ModBlocks.GNARLED_ROOTS);
        add(BlockTags.LOGS, ModBlocks.ANCIENT_LOG);
        add(net.minecraft.tags.BlockItemTags.LOGS_THAT_BURN.block(), ModBlocks.ANCIENT_LOG);
        add(BlockTags.OVERWORLD_NATURAL_LOGS, ModBlocks.ANCIENT_LOG);
        add(BlockTags.LEAVES, ModBlocks.ANCIENT_LEAVES);
        add(BlockTags.MINEABLE_WITH_HOE, ModBlocks.ANCIENT_LEAVES, ModBlocks.GLOWING_MOSS);
        add(BlockTags.SWORD_EFFICIENT, ModBlocks.ANCIENT_LEAVES, ModBlocks.BRAMBLE, ModBlocks.GLOWCAP);
        add(BlockTags.SUPPORTS_VEGETATION, ModBlocks.GLOWING_MOSS);
        add(BlockTags.MOSS_BLOCKS, ModBlocks.GLOWING_MOSS);

        // --- Zerklüftete Weiten ---
        add(BlockTags.MINEABLE_WITH_PICKAXE, ModBlocks.AMBER_CRYSTAL, ModBlocks.AMBER_CLUSTER, ModBlocks.RIFT_STONE,
                ModBlocks.CHARGED_STORMSTONE, ModBlocks.FULGURITE, ModBlocks.STORM_CRYSTAL, ModBlocks.NACRE_BLOCK);
        add(BlockTags.MINEABLE_WITH_SHOVEL, ModBlocks.ABYSSAL_SAND);
        add(BlockTags.MINEABLE_WITH_SHOVEL, ModBlocks.RUBBLE);

        // --- Siegelkreis ---
        add(BlockTags.MINEABLE_WITH_PICKAXE, ModBlocks.CRACKED_SEALSTONE_BRICKS, ModBlocks.MOSSY_SEALSTONE_BRICKS,
                ModBlocks.POLISHED_SEALSTONE, ModBlocks.CHISELED_SEALSTONE);
        add(BlockTags.DRAGON_IMMUNE, ModBlocks.SEAL_MONOLITH, ModBlocks.SEAL_RUNE, ModBlocks.SEAL_INLAY, ModBlocks.SEAL_SOCKET,
                ModBlocks.SEAL_PLATE, ModBlocks.SHADOW_PORTAL);
        add(BlockTags.WITHER_IMMUNE, ModBlocks.SEAL_MONOLITH, ModBlocks.SEAL_RUNE, ModBlocks.SEAL_INLAY, ModBlocks.SEAL_SOCKET,
                ModBlocks.SEAL_PLATE, ModBlocks.SHADOW_PORTAL);

        // --- Schattenwelt ---
        add(BlockTags.MINEABLE_WITH_PICKAXE, ShadowBlocks.CRACKED_SHADOW_STONE_BRICKS, ShadowBlocks.CHISELED_SHADOW_STONE,
                ShadowBlocks.DEEP_SHADOW_STONE, ShadowBlocks.NIGHT_CRYSTAL, ShadowBlocks.NIGHT_CRYSTAL_CLUSTER);
        add(BlockTags.MINEABLE_WITH_SHOVEL, ShadowBlocks.SHADOW_SOIL, ShadowBlocks.SHADOW_GRASS, ShadowBlocks.SHADOW_SAND,
                ShadowBlocks.ASH_SNOW_BLOCK, ShadowBlocks.ASH_SNOW);
        add(BlockTags.MINEABLE_WITH_AXE, ShadowBlocks.SHADOW_LOG, ShadowBlocks.SHADOW_WOOD, ShadowBlocks.STRIPPED_SHADOW_LOG,
                ShadowBlocks.STRIPPED_SHADOW_WOOD, ShadowBlocks.SHADOW_DOOR, ShadowBlocks.SHADOW_TRAPDOOR, ShadowBlocks.SHADOW_FENCE,
                ShadowBlocks.SHADOW_FENCE_GATE);
        add(BlockItemTags.WOODEN_DOORS.block(), ShadowBlocks.SHADOW_DOOR);
        add(BlockItemTags.WOODEN_TRAPDOORS.block(), ShadowBlocks.SHADOW_TRAPDOOR);
        add(BlockItemTags.WOODEN_FENCES.block(), ShadowBlocks.SHADOW_FENCE);
        add(BlockItemTags.FENCE_GATES.block(), ShadowBlocks.SHADOW_FENCE_GATE);
        add(BlockTags.MINEABLE_WITH_SHOVEL, ShadowBlocks.SHADOW_FARMLAND, ShadowBlocks.SHADOW_PATH);
        add(BlockTags.SUPPORTS_CROPS, ShadowBlocks.SHADOW_FARMLAND);
        add(BlockTags.CROPS, ShadowBlocks.DUSKGRAIN);
        add(BlockTags.MAINTAINS_FARMLAND, ShadowBlocks.DUSKGRAIN);
        add(BlockTags.MINEABLE_WITH_HOE, ShadowBlocks.SHADOW_LEAVES, ShadowBlocks.GLIMMER_MOSS);
        add(BlockTags.SWORD_EFFICIENT, ShadowBlocks.SHADOW_LEAVES, ShadowBlocks.SHADOW_SPROUTS, ShadowBlocks.PALE_GRASS,
                ShadowBlocks.GHOSTCAP);
        add(ModTags.Blocks.SHADOW_LOGS, ShadowBlocks.SHADOW_LOG, ShadowBlocks.SHADOW_WOOD, ShadowBlocks.STRIPPED_SHADOW_LOG,
                ShadowBlocks.STRIPPED_SHADOW_WOOD);
        tag(BlockItemTags.LOGS_THAT_BURN.block()).addTag(ModTags.Blocks.SHADOW_LOGS);
        tag(BlockTags.LOGS).addTag(ModTags.Blocks.SHADOW_LOGS);
        add(BlockItemTags.PLANKS.block(), ShadowBlocks.SHADOW_PLANKS.base());
        add(BlockItemTags.WOODEN_STAIRS.block(), ShadowBlocks.SHADOW_PLANKS.stairs());
        add(BlockItemTags.WOODEN_SLABS.block(), ShadowBlocks.SHADOW_PLANKS.slab());
        add(BlockTags.LEAVES, ShadowBlocks.SHADOW_LEAVES);
        add(BlockItemTags.DIRT.block(), ShadowBlocks.SHADOW_SOIL, ShadowBlocks.SHADOW_GRASS);
        add(BlockItemTags.SAND.block(), ShadowBlocks.SHADOW_SAND);
        add(BlockItemTags.SMELTS_TO_GLASS.block(), ShadowBlocks.SHADOW_SAND);
        add(BlockItemTags.MOSS_BLOCKS.block(), ShadowBlocks.GLIMMER_MOSS);
        add(BlockTags.SUPPORTS_VEGETATION, ShadowBlocks.SHADOW_SOIL, ShadowBlocks.SHADOW_GRASS, ShadowBlocks.GLIMMER_MOSS);
        add(BlockTags.ANIMALS_SPAWNABLE_ON, ShadowBlocks.SHADOW_GRASS, ShadowBlocks.GLIMMER_MOSS);
        add(BlockTags.SUPPORTS_DRY_VEGETATION, ShadowBlocks.SHADOW_SAND, ShadowBlocks.SHADOW_SOIL, ShadowBlocks.ASH_SNOW_BLOCK);
        add(BlockTags.REPLACEABLE_BY_TREES, ShadowBlocks.SHADOW_SPROUTS, ShadowBlocks.PALE_GRASS);
        add(BlockTags.REPLACEABLE_BY_MUSHROOMS, ShadowBlocks.SHADOW_SPROUTS, ShadowBlocks.PALE_GRASS);
        add(BlockTags.ENDERMAN_HOLDABLE, ShadowBlocks.SHADOW_SOIL, ShadowBlocks.SHADOW_GRASS, ShadowBlocks.SHADOW_SAND,
                ShadowBlocks.GHOSTCAP);
        add(BlockTags.CRYSTAL_SOUND_BLOCKS, ShadowBlocks.NIGHT_CRYSTAL);
        add(BlockTags.VIBRATION_RESONATORS, ShadowBlocks.NIGHT_CRYSTAL);
        tag(ModTags.Blocks.SHADOW_STONE_ORE_REPLACEABLES).add(ShadowBlocks.SHADOW_STONE.base().key());
        tag(ModTags.Blocks.DEEP_SHADOW_STONE_ORE_REPLACEABLES).add(ShadowBlocks.DEEP_SHADOW_STONE.key());
        add(BlockTags.BASE_STONE_OVERWORLD, ShadowBlocks.DEEP_SHADOW_STONE);
        // Schätze der Schattenwelt
        add(BlockTags.MINEABLE_WITH_PICKAXE, ShadowBlocks.MOONSILVER_ORE, ShadowBlocks.DEEP_MOONSILVER_ORE, ShadowBlocks.NIGHTSTEEL_ORE,
                ShadowBlocks.DEEP_NIGHTSTEEL_ORE, ShadowBlocks.SOUL_CRYSTAL_ORE, ShadowBlocks.ABYSSITE_ORE, ShadowBlocks.MOONSILVER_BLOCK,
                ShadowBlocks.NIGHTSTEEL_BLOCK, ShadowBlocks.SOUL_CRYSTAL_BLOCK, ShadowBlocks.ABYSSITE_BLOCK, ShadowBlocks.MOON_LANTERN);
        add(BlockTags.NEEDS_STONE_TOOL, ShadowBlocks.MOONSILVER_ORE, ShadowBlocks.DEEP_MOONSILVER_ORE, ShadowBlocks.MOONSILVER_BLOCK);
        add(BlockTags.NEEDS_IRON_TOOL, ShadowBlocks.NIGHTSTEEL_ORE, ShadowBlocks.DEEP_NIGHTSTEEL_ORE, ShadowBlocks.SOUL_CRYSTAL_ORE,
                ShadowBlocks.SOUL_CRYSTAL_BLOCK);
        add(BlockTags.NEEDS_DIAMOND_TOOL, ShadowBlocks.ABYSSITE_ORE, ShadowBlocks.NIGHTSTEEL_BLOCK, ShadowBlocks.ABYSSITE_BLOCK);
        add(ModTags.Blocks.ORES_MOONSILVER, ShadowBlocks.MOONSILVER_ORE, ShadowBlocks.DEEP_MOONSILVER_ORE);
        add(ModTags.Blocks.ORES_NIGHTSTEEL, ShadowBlocks.NIGHTSTEEL_ORE, ShadowBlocks.DEEP_NIGHTSTEEL_ORE);
        add(ModTags.Blocks.ORES_SOUL_CRYSTAL, ShadowBlocks.SOUL_CRYSTAL_ORE);
        add(ModTags.Blocks.ORES_ABYSSITE, ShadowBlocks.ABYSSITE_ORE);
        for (TagKey<Block> oreTag : List.of(ModTags.Blocks.ORES_MOONSILVER, ModTags.Blocks.ORES_NIGHTSTEEL, ModTags.Blocks.ORES_SOUL_CRYSTAL,
                ModTags.Blocks.ORES_ABYSSITE)) {
            tag(Tags.BlockItems.ORES.block()).addTag(oreTag);
            tag(BlockItemTags.ORES.block()).addTag(oreTag);
        }
        add(ModTags.Blocks.STORAGE_BLOCKS_MOONSILVER, ShadowBlocks.MOONSILVER_BLOCK);
        add(ModTags.Blocks.STORAGE_BLOCKS_NIGHTSTEEL, ShadowBlocks.NIGHTSTEEL_BLOCK);
        add(ModTags.Blocks.STORAGE_BLOCKS_SOUL_CRYSTAL, ShadowBlocks.SOUL_CRYSTAL_BLOCK);
        add(ModTags.Blocks.STORAGE_BLOCKS_ABYSSITE, ShadowBlocks.ABYSSITE_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS.block()).addTag(ModTags.Blocks.STORAGE_BLOCKS_MOONSILVER)
                .addTag(ModTags.Blocks.STORAGE_BLOCKS_NIGHTSTEEL).addTag(ModTags.Blocks.STORAGE_BLOCKS_SOUL_CRYSTAL)
                .addTag(ModTags.Blocks.STORAGE_BLOCKS_ABYSSITE);
        add(BlockTags.BEACON_BASE_BLOCKS, ShadowBlocks.MOONSILVER_BLOCK, ShadowBlocks.NIGHTSTEEL_BLOCK, ShadowBlocks.ABYSSITE_BLOCK);
        add(BlockTags.DRAGON_IMMUNE, ShadowBlocks.ABYSSITE_ORE, ShadowBlocks.ABYSSITE_BLOCK);
        add(BlockTags.WITHER_IMMUNE, ShadowBlocks.ABYSSITE_ORE, ShadowBlocks.ABYSSITE_BLOCK);
        add(BlockItemTags.SAPLINGS.block(), ShadowBlocks.SHADOW_SAPLING);
        add(BlockTags.FLOWER_POTS, ShadowBlocks.POTTED_SHADOW_SAPLING);
        add(BlockItemTags.LANTERNS.block(), ShadowBlocks.MOON_LANTERN);
        add(BlockTags.CLIMBABLE, ShadowBlocks.GLIMMER_VINES, ShadowBlocks.GLIMMER_VINES_PLANT);
        add(BlockTags.SWORD_EFFICIENT, ShadowBlocks.SOUL_BERRY_BUSH, ShadowBlocks.GLIMMER_VINES, ShadowBlocks.GLIMMER_VINES_PLANT);
        add(BlockTags.BEE_GROWABLES, ShadowBlocks.DUSKGRAIN, ShadowBlocks.SOUL_BERRY_BUSH);
        tag(BlockTags.BASE_STONE_OVERWORLD).add(ShadowBlocks.SHADOW_STONE.base().key());

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

    /**
     * Seit 26.3 entscheiden Tags, welche Blöcke Bewegung aufhalten: Höhenkarten (worauf Bäume und Pflanzen gesetzt
     * werden), Ersticken, Wasserlauf, Teleport-Ziele. Alle festen Blöcke der Mod (außer Laub, das über
     * {@code #leaves} dazugehört) kommen deshalb in {@code #blocks_motion_no_leaves} – genau die, die nach der alten
     * Regel als fest galten.
     */
    private void motionBlocking() {
        TagAppender<Block> appender = tag(BlockTags.BLOCKS_MOTION_NO_LEAVES);
        int count = 0;
        for (var entry : ModBlocks.BLOCKS.getEntries()) {
            Block block = entry.get();
            if (block instanceof net.minecraft.world.level.block.LeavesBlock) {
                continue;
            }
            net.minecraft.world.level.block.state.BlockState state = block.defaultBlockState();
            state.initCache();
            if (state.isSolid()) {
                appender.add(entry.getKey());
                count++;
            }
        }
        KingdomsAtWar.LOGGER.info("[Datagen] {} feste Blöcke in #blocks_motion_no_leaves", count);
    }

    @SafeVarargs
    private void add(TagKey<Block> tag, DeferredBlock<? extends Block>... blocks) {
        TagAppender<Block> appender = tag(tag);
        for (DeferredBlock<? extends Block> block : blocks) {
            appender.add(block.key());
        }
    }
}
