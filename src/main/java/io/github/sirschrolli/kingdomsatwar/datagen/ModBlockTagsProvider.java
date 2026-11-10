package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;

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
        add(BlockTags.MINEABLE_WITH_PICKAXE, io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.DEEP_SEA_LANTERN);
        add(BlockTags.MINEABLE_WITH_PICKAXE, io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.VENT_ROCK);
        add(BlockTags.MINEABLE_WITH_PICKAXE, io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.SULFUR_CRUST);
        add(BlockTags.MINEABLE_WITH_PICKAXE, io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.HYDROTHERMAL_VENT);
        add(BlockTags.MINEABLE_WITH_PICKAXE, ModBlocks.HEART_CRYSTAL_FORMATION);
        add(BlockTags.MINEABLE_WITH_PICKAXE, ModBlocks.MANA_CRYSTAL_FORMATION);
        add(BlockTags.MINEABLE_WITH_PICKAXE, ModBlocks.GOLD_LEAF_BLOCK);
        add(BlockTags.MINEABLE_WITH_PICKAXE, ModBlocks.MAGIC_TABLE);
        add(BlockTags.MINEABLE_WITH_AXE, ModBlocks.TIMBER_BEAM, ModBlocks.TIMBER_FRAME,
                ModBlocks.TIMBER_FRAME_DIAGONAL, ModBlocks.TIMBER_FRAME_CROSS, ModBlocks.TOWN_BANNER);
        // das Stadtbanner überlebt Drachen und Wither (die Siedlung hängt an ihm)
        add(BlockTags.DRAGON_IMMUNE, ModBlocks.TOWN_BANNER);
        add(BlockTags.WITHER_IMMUNE, ModBlocks.TOWN_BANNER);
        // Belagerungswaffen zerschlagen weder Stadtbanner noch Siegelkreis, Tore der Welten oder Bannsiegel
        this.tag(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Blocks.SIEGE_PROOF).addTag(BlockTags.WITHER_IMMUNE);
        add(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Blocks.SIEGE_PROOF, ModBlocks.TOWN_BANNER, ModBlocks.SEAL_SOCKET,
                ModBlocks.SEAL_PLATE, ModBlocks.SHADOW_PORTAL, ModBlocks.DRAGON_LAIR_CORE, ModBlocks.SHADOW_THRONE_CORE, ModBlocks.ARCANE_BARRIER);

        // --- Aschelande ---
        add(BlockTags.MINEABLE_WITH_SHOVEL, ModBlocks.ASH_BLOCK, ModBlocks.ASH_LAYER, ModBlocks.CINDER_SOIL);
        add(BlockTags.MINEABLE_WITH_PICKAXE, ModBlocks.GLOWING_VOLCANIC_ROCK, ModBlocks.EMBER_VENT);
        add(BlockTags.MINEABLE_WITH_AXE, ModBlocks.CHARRED_LOG);
        add(BlockTags.LOGS, ModBlocks.CHARRED_LOG);
        add(net.minecraft.tags.BlockItemTags.LOGS_THAT_BURN.block(), ModBlocks.CHARRED_LOG);
        // Bretter der eigenen Hölzer: wie Vanilla-Bretter für Stöcke, Werkbank, Truhe & Co.
        add(BlockItemTags.PLANKS.block(), ModBlocks.ANCIENT_PLANKS.base(), ModBlocks.CHARRED_PLANKS.base());
        add(BlockItemTags.WOODEN_STAIRS.block(), ModBlocks.ANCIENT_PLANKS.stairs(), ModBlocks.CHARRED_PLANKS.stairs());
        add(BlockItemTags.WOODEN_SLABS.block(), ModBlocks.ANCIENT_PLANKS.slab(), ModBlocks.CHARRED_PLANKS.slab());
        add(BlockTags.SUPPORTS_VEGETATION, ModBlocks.CINDER_SOIL);
        add(BlockTags.SUPPORTS_DRY_VEGETATION, ModBlocks.CINDER_SOIL);
        add(BlockTags.INFINIBURN_OVERWORLD, ModBlocks.GLOWING_VOLCANIC_ROCK, ModBlocks.EMBER_VENT);
        add(BlockTags.DRAGON_IMMUNE, ModBlocks.DRAGON_LAIR_CORE);
        add(BlockTags.WITHER_IMMUNE, ModBlocks.DRAGON_LAIR_CORE);
        add(BlockTags.DRAGON_IMMUNE, ModBlocks.SHADOW_THRONE_CORE);
        add(BlockTags.WITHER_IMMUNE, ModBlocks.SHADOW_THRONE_CORE);

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

        // --- Eigene Oberwelt-Biome ---
        add(BlockTags.MINEABLE_WITH_SHOVEL, ModBlocks.PEAT);
        add(BlockTags.SWORD_EFFICIENT, ModBlocks.REEDS);
        // Schlacke, Frosterde und Torf sind Erde: Pflanzen wachsen darauf, Bäume ersetzen sie nicht durch Erde
        add(BlockTags.DIRT, ModBlocks.CINDER_SOIL, ModBlocks.FROZEN_SOIL, ModBlocks.PEAT);
        add(BlockTags.SUPPORTS_DRY_VEGETATION, ModBlocks.ASH_BLOCK);
        // Vulkangestein, Reifstein, Schiefer und Sturmstein sind gewachsener Fels wie Stein
        tag(BlockTags.BASE_STONE_OVERWORLD).add(ModBlocks.VOLCANIC_ROCK.base().key(), ModBlocks.RIME_STONE.base().key(),
                ModBlocks.SHALE.base().key(), ModBlocks.STORMSTONE.base().key());
        // natürlicher Boden: darauf dürfen Bäume, Felsen und Kristalle der eigenen Biome stehen
        tag(ModTags.Blocks.NATURAL_GROUND)
                .add(ModBlocks.VOLCANIC_ROCK.base().key(), ModBlocks.RIME_STONE.base().key(), ModBlocks.SHALE.base().key(),
                        ModBlocks.STORMSTONE.base().key(), ModBlocks.ABYSSAL_STONE.base().key())
                .add(ModBlocks.GLOWING_VOLCANIC_ROCK.key(), ModBlocks.CINDER_SOIL.key(), ModBlocks.ASH_BLOCK.key(), ModBlocks.FROZEN_SOIL.key(),
                        ModBlocks.GLACIER_ICE.key(), ModBlocks.RUBBLE.key(), ModBlocks.CHARGED_STORMSTONE.key(), ModBlocks.ABYSSAL_SAND.key(),
                        ModBlocks.NACRE_BLOCK.key(), ModBlocks.PEAT.key())
                .add(net.minecraft.world.level.block.Blocks.DRIPSTONE_BLOCK.builtInRegistryHolder().key(),
                        net.minecraft.world.level.block.Blocks.BASALT.builtInRegistryHolder().key(),
                        net.minecraft.world.level.block.Blocks.BLACKSTONE.builtInRegistryHolder().key());

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
                ShadowBlocks.GHOSTCAP, ShadowBlocks.MOON_FERN, ShadowBlocks.STAR_BLOSSOM, ShadowBlocks.SOUL_REEDS);
        add(ModTags.Blocks.SHADOW_LOGS, ShadowBlocks.SHADOW_LOG, ShadowBlocks.SHADOW_WOOD, ShadowBlocks.STRIPPED_SHADOW_LOG,
                ShadowBlocks.STRIPPED_SHADOW_WOOD);
        tag(BlockItemTags.LOGS_THAT_BURN.block()).addTag(ModTags.Blocks.SHADOW_LOGS);
        tag(BlockTags.LOGS).addTag(ModTags.Blocks.SHADOW_LOGS);
        add(BlockItemTags.PLANKS.block(), ShadowBlocks.SHADOW_PLANKS.base());
        add(BlockItemTags.WOODEN_STAIRS.block(), ShadowBlocks.SHADOW_PLANKS.stairs());
        add(BlockItemTags.WOODEN_SLABS.block(), ShadowBlocks.SHADOW_PLANKS.slab());
        add(BlockTags.LEAVES, ShadowBlocks.SHADOW_LEAVES);
        add(BlockItemTags.DIRT.block(), ShadowBlocks.SHADOW_SOIL, ShadowBlocks.SHADOW_GRASS);
        add(BlockItemTags.SAND.block(), ShadowBlocks.SHADOW_SAND, ModBlocks.ABYSSAL_SAND);
        add(BlockItemTags.SMELTS_TO_GLASS.block(), ShadowBlocks.SHADOW_SAND, ModBlocks.ABYSSAL_SAND);
        for (io.github.sirschrolli.kingdomsatwar.registry.StoneChains.Chain chain : io.github.sirschrolli.kingdomsatwar.registry.StoneChains.all()) {
            add(Tags.BlockItems.COBBLESTONES.block(), chain.cobble().base());
            add(ModTags.Blocks.COBBLED_STONES, chain.cobble().base());
        }
        add(BlockItemTags.MOSS_BLOCKS.block(), ShadowBlocks.GLIMMER_MOSS);
        add(BlockTags.SUPPORTS_VEGETATION, ShadowBlocks.SHADOW_SOIL, ShadowBlocks.SHADOW_GRASS, ShadowBlocks.GLIMMER_MOSS);

        // Die Leere
        add(BlockTags.MINEABLE_WITH_PICKAXE, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CRACKED_VOID_STONE_BRICKS,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CHISELED_VOID_STONE,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.ASTRAL_PILLAR,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CHISELED_ASTRAL_MARBLE,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_MOSS,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_CRYSTAL,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_CRYSTAL_CLUSTER,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CRYSTAL_FOLIAGE);
        add(BlockTags.MINEABLE_WITH_SHOVEL, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.STARDUST);
        add(BlockTags.SUPPORTS_VEGETATION, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_MOSS,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.STARDUST);
        add(BlockTags.SWORD_EFFICIENT, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_BLOOM,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CRYSTAL_FOLIAGE);
        add(BlockTags.SMALL_FLOWERS, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_BLOOM);
        add(BlockTags.CLIMBABLE, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.STAR_VINES,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.STAR_VINES_PLANT);
        add(BlockTags.PORTALS, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_RIFT,
                io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.ABYSS_RIFT);
        add(BlockTags.DRAGON_IMMUNE, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_RIFT,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.THRONE_SEAL, io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.ABYSS_RIFT,
                io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.SEAL_SHARD, io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.HOARD_SEAL);
        add(BlockTags.WITHER_IMMUNE, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_RIFT,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.THRONE_SEAL, io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.ABYSS_RIFT,
                io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.SEAL_SHARD, io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.HOARD_SEAL);
        // Der Abgrund
        add(BlockTags.MINEABLE_WITH_PICKAXE, io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.CRACKED_CHAOS_STONE_BRICKS,
                io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.CHISELED_CHAOS_STONE,
                io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.WORLD_SEAM,
                io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.FLOAT_CRYSTAL,
                io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.LIFT_CRYSTAL,
                io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.WORLD_EATER_TROPHY);
        add(BlockTags.NEEDS_IRON_TOOL, io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.WORLD_SEAM);
        add(BlockTags.ANIMALS_SPAWNABLE_ON, ShadowBlocks.SHADOW_GRASS, ShadowBlocks.GLIMMER_MOSS);
        add(BlockTags.SUPPORTS_DRY_VEGETATION, ShadowBlocks.SHADOW_SAND, ShadowBlocks.SHADOW_SOIL, ShadowBlocks.ASH_SNOW_BLOCK);
        add(BlockTags.REPLACEABLE_BY_TREES, ShadowBlocks.SHADOW_SPROUTS, ShadowBlocks.PALE_GRASS, ShadowBlocks.MOON_FERN);
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

        // --- Das Feenreich ---
        add(BlockTags.MINEABLE_WITH_PICKAXE, FaeBlocks.MOSSY_FAESTONE_BRICKS, FaeBlocks.CHISELED_MOONSTONE);
        add(BlockTags.MINEABLE_WITH_SHOVEL, FaeBlocks.FAE_SOIL, FaeBlocks.FAE_GRASS);
        add(BlockTags.MINEABLE_WITH_HOE, FaeBlocks.FAE_MOSS, FaeBlocks.ROSE_GLIMMER_LEAVES, FaeBlocks.AZURE_GLIMMER_LEAVES,
                FaeBlocks.THORN_HEDGE);
        add(BlockTags.MINEABLE_WITH_AXE, FaeBlocks.GLIMMERWOOD_LOG, FaeBlocks.GLIMMERWOOD_WOOD, FaeBlocks.STRIPPED_GLIMMERWOOD_LOG,
                FaeBlocks.STRIPPED_GLIMMERWOOD_WOOD, FaeBlocks.GLIMMERWOOD_DOOR, FaeBlocks.GLIMMERWOOD_TRAPDOOR, FaeBlocks.GLIMMERWOOD_FENCE,
                FaeBlocks.GLIMMERWOOD_FENCE_GATE, FaeBlocks.FAE_MUSHROOM_STEM, FaeBlocks.ROSE_MUSHROOM_CAP, FaeBlocks.AZURE_MUSHROOM_CAP,
                FaeBlocks.VIOLET_MUSHROOM_CAP);
        add(BlockTags.SWORD_EFFICIENT, FaeBlocks.ROSE_GLIMMER_LEAVES, FaeBlocks.AZURE_GLIMMER_LEAVES, FaeBlocks.FAE_SPROUTS,
                FaeBlocks.MOONBELL, FaeBlocks.FAE_ROSE, FaeBlocks.STARLACE, FaeBlocks.LANTERN_MUSHROOM, FaeBlocks.GLOW_VINES,
                FaeBlocks.GLOW_VINES_PLANT, FaeBlocks.THORNBRIAR, FaeBlocks.THORN_HEDGE, FaeBlocks.SILK_STRANDS);
        // der Mondhain: Mottenseide (wie Wolle), der Kokon ist unzerstörbar
        add(BlockItemTags.WOOL.block(), FaeBlocks.MOTH_SILK);
        add(BlockTags.DRAGON_IMMUNE, FaeBlocks.MOTH_COCOON);
        add(BlockTags.WITHER_IMMUNE, FaeBlocks.MOTH_COCOON);
        add(BlockTags.DRAGON_IMMUNE, io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks.GLUT_ANVIL);
        add(BlockTags.WITHER_IMMUNE, io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks.GLUT_ANVIL);
        add(BlockTags.DRAGON_IMMUNE, io.github.sirschrolli.kingdomsatwar.registry.StarBlocks.STARSTONE);
        add(BlockTags.WITHER_IMMUNE, io.github.sirschrolli.kingdomsatwar.registry.StarBlocks.STARSTONE);
        add(BlockTags.DRAGON_IMMUNE, io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks.STAGE_HAT);
        add(BlockTags.WITHER_IMMUNE, io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks.STAGE_HAT);
        add(BlockTags.DRAGON_IMMUNE, io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.ERROR_CORE);
        add(BlockTags.WITHER_IMMUNE, io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.ERROR_CORE);
        add(BlockTags.MINEABLE_WITH_PICKAXE, io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.MISSING_BLOCK,
                io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.STATIC_BLOCK);
        add(ModTags.Blocks.GLIMMERWOOD_LOGS, FaeBlocks.GLIMMERWOOD_LOG, FaeBlocks.GLIMMERWOOD_WOOD, FaeBlocks.STRIPPED_GLIMMERWOOD_LOG,
                FaeBlocks.STRIPPED_GLIMMERWOOD_WOOD);
        tag(BlockItemTags.LOGS_THAT_BURN.block()).addTag(ModTags.Blocks.GLIMMERWOOD_LOGS);
        tag(BlockTags.LOGS).addTag(ModTags.Blocks.GLIMMERWOOD_LOGS);
        add(BlockItemTags.PLANKS.block(), FaeBlocks.GLIMMERWOOD_PLANKS.base());
        add(BlockItemTags.WOODEN_STAIRS.block(), FaeBlocks.GLIMMERWOOD_PLANKS.stairs());
        add(BlockItemTags.WOODEN_SLABS.block(), FaeBlocks.GLIMMERWOOD_PLANKS.slab());
        add(BlockItemTags.WOODEN_DOORS.block(), FaeBlocks.GLIMMERWOOD_DOOR);
        add(BlockItemTags.WOODEN_TRAPDOORS.block(), FaeBlocks.GLIMMERWOOD_TRAPDOOR);
        add(BlockItemTags.WOODEN_FENCES.block(), FaeBlocks.GLIMMERWOOD_FENCE);
        add(BlockItemTags.FENCE_GATES.block(), FaeBlocks.GLIMMERWOOD_FENCE_GATE);
        add(BlockTags.LEAVES, FaeBlocks.ROSE_GLIMMER_LEAVES, FaeBlocks.AZURE_GLIMMER_LEAVES);
        add(BlockItemTags.SAPLINGS.block(), FaeBlocks.GLIMMERWOOD_SAPLING);
        add(BlockTags.FLOWER_POTS, FaeBlocks.POTTED_GLIMMERWOOD_SAPLING);
        add(BlockItemTags.DIRT.block(), FaeBlocks.FAE_SOIL, FaeBlocks.FAE_GRASS);
        add(BlockItemTags.MOSS_BLOCKS.block(), FaeBlocks.FAE_MOSS);
        add(BlockTags.SUPPORTS_VEGETATION, FaeBlocks.FAE_SOIL, FaeBlocks.FAE_GRASS, FaeBlocks.FAE_MOSS);
        add(BlockTags.ANIMALS_SPAWNABLE_ON, FaeBlocks.FAE_GRASS, FaeBlocks.FAE_MOSS);
        add(BlockTags.RABBITS_SPAWNABLE_ON, FaeBlocks.FAE_GRASS, FaeBlocks.FAE_MOSS);
        add(BlockTags.FOXES_SPAWNABLE_ON, FaeBlocks.FAE_GRASS, FaeBlocks.FAE_MOSS);
        add(BlockTags.FROGS_SPAWNABLE_ON, FaeBlocks.FAE_GRASS, FaeBlocks.FAE_MOSS, FaeBlocks.FAE_SOIL);
        add(BlockTags.SMALL_FLOWERS, FaeBlocks.MOONBELL, FaeBlocks.FAE_ROSE);
        add(BlockTags.FLOWERS, FaeBlocks.STARLACE);
        add(BlockTags.REPLACEABLE_BY_TREES, FaeBlocks.FAE_SPROUTS, FaeBlocks.GLOW_VINES, FaeBlocks.GLOW_VINES_PLANT);
        add(BlockTags.REPLACEABLE_BY_MUSHROOMS, FaeBlocks.FAE_SPROUTS);
        add(BlockTags.CLIMBABLE, FaeBlocks.GLOW_VINES, FaeBlocks.GLOW_VINES_PLANT);
        add(BlockTags.ENDERMAN_HOLDABLE, FaeBlocks.FAE_SOIL, FaeBlocks.FAE_GRASS, FaeBlocks.LANTERN_MUSHROOM);
        add(BlockTags.PORTALS, FaeBlocks.FAE_PORTAL);
        add(BlockTags.DRAGON_IMMUNE, FaeBlocks.FAE_PORTAL);
        add(BlockTags.WITHER_IMMUNE, FaeBlocks.FAE_PORTAL);
        add(BlockTags.BASE_STONE_OVERWORLD, FaeBlocks.FAESTONE.base());
        add(ModTags.Blocks.NATURAL_GROUND, FaeBlocks.FAESTONE.base(), FaeBlocks.FAE_SOIL, FaeBlocks.FAE_GRASS, FaeBlocks.FAE_MOSS);

        // --- Einrichtung ---
        add(Tags.BlockItems.GLASS_BLOCKS.block(), ModBlocks.LEADED_GLASS);
        add(BlockTags.IMPERMEABLE, ModBlocks.LEADED_GLASS);
        add(Tags.BlockItems.GLASS_PANES.block(), ModBlocks.LEADED_GLASS_PANE);
        add(BlockItemTags.WOODEN_DOORS.block(), ModBlocks.CASTLE_DOOR);
        add(BlockItemTags.WOODEN_TRAPDOORS.block(), ModBlocks.GREEN_SHUTTERS, ModBlocks.RED_SHUTTERS);
        add(BlockTags.MINEABLE_WITH_AXE, ModBlocks.CASTLE_DOOR, ModBlocks.GREEN_SHUTTERS, ModBlocks.RED_SHUTTERS,
                ModBlocks.OAK_CHAIR, ModBlocks.OAK_BENCH, ModBlocks.OAK_TABLE, ModBlocks.THRONE);
        add(BlockTags.MINEABLE_WITH_PICKAXE, ModBlocks.STEEL_LANTERN, ModBlocks.AETHERIT_LANTERN, ModBlocks.CHANDELIER);
        add(BlockTags.MINEABLE_WITH_AXE, ModBlocks.BUILDER_SCAFFOLD);
        add(BlockTags.MINEABLE_WITH_AXE, ModBlocks.MERCENARY_BOARD);
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
        architect();
    }

    /** Dragon Architect: was beim Vermessen nicht übernommen wird, was kostbar ist und was gewachsener Boden ist. */
    private void architect() {
        tag(ModTags.Blocks.ARCHITECT_UNBUILDABLE).addTag(ModTags.Blocks.SIEGE_PROOF)
                .add(vanilla(net.minecraft.world.level.block.Blocks.SPAWNER), vanilla(net.minecraft.world.level.block.Blocks.TRIAL_SPAWNER),
                        vanilla(net.minecraft.world.level.block.Blocks.VAULT), vanilla(net.minecraft.world.level.block.Blocks.BUDDING_AMETHYST),
                        vanilla(net.minecraft.world.level.block.Blocks.INFESTED_STONE), vanilla(net.minecraft.world.level.block.Blocks.INFESTED_COBBLESTONE),
                        vanilla(net.minecraft.world.level.block.Blocks.INFESTED_STONE_BRICKS),
                        vanilla(net.minecraft.world.level.block.Blocks.INFESTED_MOSSY_STONE_BRICKS),
                        vanilla(net.minecraft.world.level.block.Blocks.INFESTED_CRACKED_STONE_BRICKS),
                        vanilla(net.minecraft.world.level.block.Blocks.INFESTED_CHISELED_STONE_BRICKS),
                        vanilla(net.minecraft.world.level.block.Blocks.INFESTED_DEEPSLATE), vanilla(net.minecraft.world.level.block.Blocks.SUSPICIOUS_SAND),
                        vanilla(net.minecraft.world.level.block.Blocks.SUSPICIOUS_GRAVEL), vanilla(net.minecraft.world.level.block.Blocks.END_PORTAL_FRAME),
                        vanilla(net.minecraft.world.level.block.Blocks.END_PORTAL), vanilla(net.minecraft.world.level.block.Blocks.END_GATEWAY),
                        vanilla(net.minecraft.world.level.block.Blocks.NETHER_PORTAL), vanilla(net.minecraft.world.level.block.Blocks.STRUCTURE_VOID),
                        vanilla(net.minecraft.world.level.block.Blocks.BEDROCK), vanilla(net.minecraft.world.level.block.Blocks.BARRIER),
                        vanilla(net.minecraft.world.level.block.Blocks.LIGHT), vanilla(net.minecraft.world.level.block.Blocks.COMMAND_BLOCK),
                        vanilla(net.minecraft.world.level.block.Blocks.CHAIN_COMMAND_BLOCK),
                        vanilla(net.minecraft.world.level.block.Blocks.REPEATING_COMMAND_BLOCK),
                        vanilla(net.minecraft.world.level.block.Blocks.STRUCTURE_BLOCK), vanilla(net.minecraft.world.level.block.Blocks.JIGSAW),
                        vanilla(net.minecraft.world.level.block.Blocks.TEST_BLOCK), vanilla(net.minecraft.world.level.block.Blocks.TEST_INSTANCE_BLOCK),
                        vanilla(net.minecraft.world.level.block.Blocks.REINFORCED_DEEPSLATE), ModBlocks.BUILDER_SCAFFOLD.key(), ModBlocks.TOWN_BANNER.key());
        TagAppender<Block> precious = tag(ModTags.Blocks.ARCHITECT_PRECIOUS)
                .addTag(Tags.Blocks.ORES).addTag(Tags.Blocks.STORAGE_BLOCKS).addTag(Tags.Blocks.OBSIDIANS)
                .addTag(BlockTags.SHULKER_BOXES).addTag(BlockTags.ANVIL).addTag(ModTags.Blocks.DRAGON_EGGS);
        for (TagKey<Block> t : ORE_TAGS) {
            precious.addTag(t);
        }
        for (TagKey<Block> t : List.of(ModTags.Blocks.STORAGE_BLOCKS_CARBON, ModTags.Blocks.STORAGE_BLOCKS_GEMSTONE,
                ModTags.Blocks.STORAGE_BLOCKS_KONTERIT, ModTags.Blocks.STORAGE_BLOCKS_AETHERIT, ModTags.Blocks.STORAGE_BLOCKS_DRACONIUM,
                ModTags.Blocks.STORAGE_BLOCKS_WYVERNIT, ModTags.Blocks.STORAGE_BLOCKS_STEEL, ModTags.Blocks.STORAGE_BLOCKS_MOONSILVER,
                ModTags.Blocks.STORAGE_BLOCKS_NIGHTSTEEL, ModTags.Blocks.STORAGE_BLOCKS_SOUL_CRYSTAL, ModTags.Blocks.STORAGE_BLOCKS_ABYSSITE,
                ModTags.Blocks.ORES_MOONSILVER, ModTags.Blocks.ORES_NIGHTSTEEL, ModTags.Blocks.ORES_SOUL_CRYSTAL, ModTags.Blocks.ORES_ABYSSITE)) {
            precious.addTag(t);
        }
        for (Block b : List.of(net.minecraft.world.level.block.Blocks.BEACON, net.minecraft.world.level.block.Blocks.CONDUIT,
                net.minecraft.world.level.block.Blocks.ENCHANTING_TABLE, net.minecraft.world.level.block.Blocks.ENDER_CHEST,
                net.minecraft.world.level.block.Blocks.RESPAWN_ANCHOR, net.minecraft.world.level.block.Blocks.LODESTONE,
                net.minecraft.world.level.block.Blocks.TNT, net.minecraft.world.level.block.Blocks.GLOWSTONE,
                net.minecraft.world.level.block.Blocks.SEA_LANTERN, net.minecraft.world.level.block.Blocks.BREWING_STAND,
                net.minecraft.world.level.block.Blocks.JUKEBOX, net.minecraft.world.level.block.Blocks.CAULDRON,
                net.minecraft.world.level.block.Blocks.WATER_CAULDRON, net.minecraft.world.level.block.Blocks.HOPPER,
                net.minecraft.world.level.block.Blocks.BLAST_FURNACE, net.minecraft.world.level.block.Blocks.IRON_TRAPDOOR,
                net.minecraft.world.level.block.Blocks.BELL, net.minecraft.world.level.block.Blocks.POWERED_RAIL,
                net.minecraft.world.level.block.Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, net.minecraft.world.level.block.Blocks.GILDED_BLACKSTONE,
                net.minecraft.world.level.block.Blocks.SCULK_SENSOR, net.minecraft.world.level.block.Blocks.CALIBRATED_SCULK_SENSOR,
                net.minecraft.world.level.block.Blocks.SCULK_SHRIEKER, net.minecraft.world.level.block.Blocks.SCULK_CATALYST,
                net.minecraft.world.level.block.Blocks.HEAVY_CORE, net.minecraft.world.level.block.Blocks.CRAFTER,
                net.minecraft.world.level.block.Blocks.DRAGON_EGG, net.minecraft.world.level.block.Blocks.AMETHYST_BLOCK,
                net.minecraft.world.level.block.Blocks.BEEHIVE, net.minecraft.world.level.block.Blocks.BEE_NEST,
                net.minecraft.world.level.block.Blocks.CAKE, net.minecraft.world.level.block.Blocks.CREAKING_HEART)) {
            precious.add(vanilla(b));
        }
        tag(ModTags.Blocks.ARCHITECT_TERRAIN).addTag(BlockTags.SUBSTRATE_OVERWORLD).addTag(BlockTags.DIRT).addTag(BlockTags.NYLIUM)
                .addTag(BlockTags.SAND).addTag(BlockTags.SNOW).addTag(BlockTags.BASE_STONE_OVERWORLD).addTag(BlockTags.BASE_STONE_NETHER)
                .addTag(Tags.Blocks.SANDS).addTag(Tags.Blocks.GRAVELS).addTag(Tags.Blocks.NETHERRACKS).addTag(Tags.Blocks.END_STONES)
                .addTag(ModTags.Blocks.NATURAL_GROUND)
                .add(vanilla(net.minecraft.world.level.block.Blocks.SNOW_BLOCK), vanilla(net.minecraft.world.level.block.Blocks.SNOW),
                        vanilla(net.minecraft.world.level.block.Blocks.POWDER_SNOW), vanilla(net.minecraft.world.level.block.Blocks.ICE),
                        vanilla(net.minecraft.world.level.block.Blocks.PACKED_ICE), vanilla(net.minecraft.world.level.block.Blocks.BLUE_ICE),
                        vanilla(net.minecraft.world.level.block.Blocks.CLAY), vanilla(net.minecraft.world.level.block.Blocks.SOUL_SAND),
                        vanilla(net.minecraft.world.level.block.Blocks.SOUL_SOIL), vanilla(net.minecraft.world.level.block.Blocks.DIRT_PATH),
                        vanilla(net.minecraft.world.level.block.Blocks.FARMLAND), vanilla(net.minecraft.world.level.block.Blocks.CALCITE),
                        vanilla(net.minecraft.world.level.block.Blocks.SMOOTH_BASALT), vanilla(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK));
    }

    private static net.minecraft.resources.ResourceKey<Block> vanilla(Block block) {
        return block.builtInRegistryHolder().key();
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
