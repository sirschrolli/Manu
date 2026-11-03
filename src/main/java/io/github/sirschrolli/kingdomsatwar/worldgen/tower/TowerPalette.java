package io.github.sirschrolli.kingdomsatwar.worldgen.tower;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;

/**
 * Baustoffe eines Magierturms. Jedes Element hat seinen eigenen Turm: Ignatius haust in schwarzem Basalt unter
 * ziegelrotem Dach, Isolde in weißem Kalkstein mit Eisbändern und schieferblauem Dach, Fulgor in Tiefenschiefer mit
 * Kupfer, Sylvana in bemoostem Mauerwerk mit Holzschindeln, Gorvan in Tuff mit Lehmziegeln und Marena in Prismarin mit
 * Perlmuttsäulen.
 *
 * @param wall      Hauptstein der Mauern; {@code wall2}/{@code wall3} sind eingestreute Abwandlungen
 * @param trim      Gesimse, Fensterbänke, Bänder (dazu Treppe, Stufe und Mauer desselben Steins)
 * @param pillar    Säulen und Lisenen
 * @param floor     Steinboden; {@code floorAccent} für Ringe und Muster
 * @param pave      Pflaster der Terrasse; {@code pave2} eingestreut, {@code edge} am ausfransenden Rand
 * @param roof      Dach (Vollblock, Treppe, Stufe)
 * @param planks    Holz für Böden und Möbel (dazu Treppe, Stufe, Zaun, Falltür, Tür)
 * @param glass     Buntglas (Block und Scheibe) in der Farbe des Elements
 * @param glow      leuchtender Block für Böden, Becken und Feuerschalen
 */
record TowerPalette(
        DragonType element,
        Block foundation,
        Block wall, Block wall2, Block wall3,
        Block trim, Block trimStairs, Block trimSlab, Block trimWall,
        Block pillar,
        Block floor, Block floorAccent,
        Block pave, Block pave2, Block edge,
        Block roof, Block roofStairs, Block roofSlab,
        Block planks, Block woodStairs, Block woodSlab, Block fence, Block trapdoor, Block door,
        Block glass, Block pane,
        Block lantern, Block glow,
        Block carpet, Block carpet2, Block bed, Block wallBanner, Block candle) {

    static TowerPalette of(DragonType element) {
        return switch (element) {
            case FIRE -> new TowerPalette(element,
                    ModBlocks.VOLCANIC_ROCK.base().get(),
                    Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.BLACKSTONE,
                    Blocks.RED_NETHER_BRICKS, Blocks.RED_NETHER_BRICK_STAIRS, Blocks.RED_NETHER_BRICK_SLAB, Blocks.RED_NETHER_BRICK_WALL,
                    Blocks.POLISHED_BASALT,
                    Blocks.POLISHED_BLACKSTONE, Blocks.RED_NETHER_BRICKS,
                    Blocks.POLISHED_BLACKSTONE, Blocks.BLACKSTONE, Blocks.RED_SAND,
                    set(ModBlocks.RED_ROOF_TILES),
                    ModBlocks.RED_ROOF_TILES.stairs().get(), ModBlocks.RED_ROOF_TILES.slab().get(),
                    Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_STAIRS, Blocks.CRIMSON_SLAB, Blocks.CRIMSON_FENCE, Blocks.CRIMSON_TRAPDOOR,
                    Blocks.CRIMSON_DOOR,
                    Blocks.STAINED_GLASS.red(), Blocks.STAINED_GLASS_PANE.red(),
                    Blocks.LANTERN, Blocks.SHROOMLIGHT,
                    Blocks.CARPET.red(), Blocks.CARPET.orange(), Blocks.BED.red(), Blocks.WALL_BANNER.red(), Blocks.DYED_CANDLE.orange());
            case ICE -> new TowerPalette(element,
                    ModBlocks.RIME_STONE.base().get(),
                    ModBlocks.RIME_STONE.base().get(), Blocks.PACKED_ICE, Blocks.CALCITE,
                    Blocks.PACKED_ICE, Blocks.QUARTZ_STAIRS, Blocks.QUARTZ_SLAB,
                    wall(ModBlocks.RIME_STONE),
                    Blocks.QUARTZ_PILLAR,
                    Blocks.POLISHED_DIORITE, Blocks.PACKED_ICE,
                    ModBlocks.RIME_STONE.base().get(), Blocks.PACKED_ICE, Blocks.SNOW_BLOCK,
                    set(ModBlocks.DARK_SLATE_SHINGLES),
                    ModBlocks.DARK_SLATE_SHINGLES.stairs().get(), ModBlocks.DARK_SLATE_SHINGLES.slab().get(),
                    Blocks.PALE_OAK_PLANKS, Blocks.PALE_OAK_STAIRS, Blocks.PALE_OAK_SLAB, Blocks.PALE_OAK_FENCE, Blocks.PALE_OAK_TRAPDOOR,
                    Blocks.PALE_OAK_DOOR,
                    Blocks.STAINED_GLASS.lightBlue(), Blocks.STAINED_GLASS_PANE.lightBlue(),
                    Blocks.SOUL_LANTERN, Blocks.SEA_LANTERN,
                    Blocks.CARPET.lightBlue(), Blocks.CARPET.white(), Blocks.BED.lightBlue(), Blocks.WALL_BANNER.lightBlue(),
                    Blocks.DYED_CANDLE.lightBlue());
            case LIGHTNING -> new TowerPalette(element,
                    ModBlocks.STORMSTONE.base().get(),
                    Blocks.DEEPSLATE_TILES, Blocks.POLISHED_DEEPSLATE, Blocks.CRACKED_DEEPSLATE_TILES,
                    copper(Blocks.CUT_COPPER, WeatheringCopper.WeatherState.UNAFFECTED),
                    copper(Blocks.CUT_COPPER_STAIRS, WeatheringCopper.WeatherState.UNAFFECTED),
                    copper(Blocks.CUT_COPPER_SLAB, WeatheringCopper.WeatherState.UNAFFECTED),
                    Blocks.DEEPSLATE_TILE_WALL,
                    ModBlocks.FULGURITE.get(),
                    Blocks.POLISHED_DEEPSLATE, copper(Blocks.CUT_COPPER, WeatheringCopper.WeatherState.WEATHERED),
                    ModBlocks.STORMSTONE.base().get(), Blocks.ANDESITE, Blocks.GRAVEL,
                    copper(Blocks.CUT_COPPER, WeatheringCopper.WeatherState.OXIDIZED),
                    copper(Blocks.CUT_COPPER_STAIRS, WeatheringCopper.WeatherState.OXIDIZED),
                    copper(Blocks.CUT_COPPER_SLAB, WeatheringCopper.WeatherState.OXIDIZED),
                    Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_TRAPDOOR,
                    Blocks.DARK_OAK_DOOR,
                    Blocks.STAINED_GLASS.yellow(), Blocks.STAINED_GLASS_PANE.yellow(),
                    copper(Blocks.COPPER_LANTERN, WeatheringCopper.WeatherState.UNAFFECTED),
                    copper(Blocks.COPPER_BULB, WeatheringCopper.WeatherState.UNAFFECTED),
                    Blocks.CARPET.cyan(), Blocks.CARPET.yellow(), Blocks.BED.cyan(), Blocks.WALL_BANNER.yellow(), Blocks.DYED_CANDLE.yellow());
            case NATURE -> new TowerPalette(element,
                    Blocks.MOSSY_COBBLESTONE,
                    ModBlocks.MOSSY_WALL_STONES.base().get(), Blocks.MOSSY_STONE_BRICKS, Blocks.STONE_BRICKS,
                    ModBlocks.ANCIENT_PLANKS.base().get(), ModBlocks.ANCIENT_PLANKS.stairs().get(), ModBlocks.ANCIENT_PLANKS.slab().get(),
                    Blocks.MOSSY_STONE_BRICK_WALL,
                    ModBlocks.ANCIENT_LOG.get(),
                    Blocks.MOSSY_STONE_BRICKS, Blocks.MOSS_BLOCK,
                    Blocks.MOSSY_COBBLESTONE, Blocks.COBBLESTONE, Blocks.MOSS_BLOCK,
                    set(ModBlocks.WOODEN_SHINGLES),
                    ModBlocks.WOODEN_SHINGLES.stairs().get(), ModBlocks.WOODEN_SHINGLES.slab().get(),
                    ModBlocks.ANCIENT_PLANKS.base().get(), ModBlocks.ANCIENT_PLANKS.stairs().get(), ModBlocks.ANCIENT_PLANKS.slab().get(),
                    Blocks.OAK_FENCE, Blocks.OAK_TRAPDOOR, Blocks.OAK_DOOR,
                    Blocks.STAINED_GLASS.lime(), Blocks.STAINED_GLASS_PANE.lime(),
                    Blocks.LANTERN, Blocks.SHROOMLIGHT,
                    Blocks.MOSS_CARPET, Blocks.CARPET.green(), Blocks.BED.green(), Blocks.WALL_BANNER.green(), Blocks.DYED_CANDLE.lime());
            case EARTH -> new TowerPalette(element,
                    ModBlocks.SHALE.base().get(),
                    Blocks.TUFF_BRICKS, Blocks.POLISHED_TUFF, Blocks.TUFF,
                    Blocks.MUD_BRICKS, Blocks.MUD_BRICK_STAIRS, Blocks.MUD_BRICK_SLAB, Blocks.MUD_BRICK_WALL,
                    ModBlocks.STONE_PILLAR.get(),
                    Blocks.POLISHED_TUFF, Blocks.MUD_BRICKS,
                    ModBlocks.SHALE.base().get(), Blocks.TUFF, Blocks.COARSE_DIRT,
                    set(ModBlocks.BROWN_ROOF_TILES),
                    ModBlocks.BROWN_ROOF_TILES.stairs().get(), ModBlocks.BROWN_ROOF_TILES.slab().get(),
                    Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_TRAPDOOR,
                    Blocks.SPRUCE_DOOR,
                    Blocks.STAINED_GLASS.orange(), Blocks.STAINED_GLASS_PANE.orange(),
                    Blocks.LANTERN, ModBlocks.AMBER_CRYSTAL.get(),
                    Blocks.CARPET.brown(), Blocks.CARPET.orange(), Blocks.BED.brown(), Blocks.WALL_BANNER.brown(), Blocks.DYED_CANDLE.brown());
            case WATER -> new TowerPalette(element,
                    ModBlocks.ABYSSAL_STONE.base().get(),
                    Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE, Blocks.DARK_PRISMARINE,
                    ModBlocks.NACRE_BLOCK.get(), Blocks.QUARTZ_STAIRS, Blocks.QUARTZ_SLAB, Blocks.PRISMARINE_WALL,
                    ModBlocks.NACRE_BLOCK.get(),
                    Blocks.PRISMARINE_BRICKS, Blocks.DARK_PRISMARINE,
                    ModBlocks.ABYSSAL_STONE.base().get(), Blocks.PRISMARINE, Blocks.SAND,
                    Blocks.DARK_PRISMARINE, Blocks.DARK_PRISMARINE_STAIRS, Blocks.DARK_PRISMARINE_SLAB,
                    Blocks.WARPED_PLANKS, Blocks.WARPED_STAIRS, Blocks.WARPED_SLAB, Blocks.WARPED_FENCE, Blocks.WARPED_TRAPDOOR,
                    Blocks.WARPED_DOOR,
                    Blocks.STAINED_GLASS.cyan(), Blocks.STAINED_GLASS_PANE.cyan(),
                    Blocks.SOUL_LANTERN, Blocks.SEA_LANTERN,
                    Blocks.CARPET.cyan(), Blocks.CARPET.blue(), Blocks.BED.cyan(), Blocks.WALL_BANNER.blue(), Blocks.DYED_CANDLE.cyan());
        };
    }

    private static Block set(BuildingSet set) {
        return set.base().get();
    }

    private static Block wall(BuildingSet set) {
        return set.wall() != null ? set.wall().get() : Blocks.STONE_BRICK_WALL;
    }

    /** Gewachster Kupferblock einer Alterungsstufe (bleibt so, wie er gebaut wurde). */
    static Block copper(net.minecraft.world.level.block.WeatheringCopperCollection<Block> blocks, WeatheringCopper.WeatherState state) {
        return blocks.waxed().pick(state);
    }
}
