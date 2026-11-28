package io.github.sirschrolli.kingdomsatwar.war;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;

/**
 * Die Bauweise einer Stammburg: Mauerwerk mit Abwandlungen, Sockel, Zierstein, Dach und Holz. Jedes Reich baut anders –
 * grauer Stein mit Schiefer (Eisenmark), dunkler Stein mit roten Dächern (Rabenstein), Sandstein mit Ziegeldächern
 * (Dornfels), heller Stein mit grünen Kupferdächern (Silberau).
 */
public record CastleStyle(
        Block wall, Block wallWorn, Block wallMossy, Block wallStairs, Block wallSlab, Block wallWall,
        Block base, Block baseStairs,
        Block trim, Block trimStairs, Block trimSlab,
        Block roof, Block roofStairs, Block roofSlab,
        Block planks, Block log, Block strippedLog, Block woodStairs, Block woodSlab, Block fence, Block door, Block trapdoor,
        Block floor, Block path, Block window) {

    public static final CastleStyle EISENMARK = new CastleStyle(
            Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.STONE_BRICK_STAIRS,
            Blocks.STONE_BRICK_SLAB, Blocks.STONE_BRICK_WALL,
            Blocks.COBBLESTONE, Blocks.COBBLESTONE_STAIRS,
            Blocks.POLISHED_ANDESITE, Blocks.POLISHED_ANDESITE_STAIRS, Blocks.POLISHED_ANDESITE_SLAB,
            Blocks.DEEPSLATE_TILES, Blocks.DEEPSLATE_TILE_STAIRS, Blocks.DEEPSLATE_TILE_SLAB,
            Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB,
            Blocks.SPRUCE_FENCE, Blocks.SPRUCE_DOOR, Blocks.SPRUCE_TRAPDOOR,
            Blocks.STONE_BRICKS, Blocks.GRAVEL, Blocks.GLASS_PANE);

    public static final CastleStyle RABENSTEIN = new CastleStyle(
            Blocks.DEEPSLATE_BRICKS, Blocks.CRACKED_DEEPSLATE_BRICKS, Blocks.DEEPSLATE_TILES, Blocks.DEEPSLATE_BRICK_STAIRS,
            Blocks.DEEPSLATE_BRICK_SLAB, Blocks.DEEPSLATE_BRICK_WALL,
            Blocks.COBBLED_DEEPSLATE, Blocks.COBBLED_DEEPSLATE_STAIRS,
            Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB,
            Blocks.RED_NETHER_BRICKS, Blocks.RED_NETHER_BRICK_STAIRS, Blocks.RED_NETHER_BRICK_SLAB,
            Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_SLAB,
            Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_DOOR, Blocks.DARK_OAK_TRAPDOOR,
            Blocks.POLISHED_DEEPSLATE, Blocks.COARSE_DIRT, Blocks.STAINED_GLASS_PANE.pick(DyeColor.RED));

    public static final CastleStyle DORNFELS = new CastleStyle(
            Blocks.SMOOTH_SANDSTONE, Blocks.SANDSTONE, Blocks.SANDSTONE, Blocks.SMOOTH_SANDSTONE_STAIRS,
            Blocks.SMOOTH_SANDSTONE_SLAB, Blocks.SANDSTONE_WALL,
            Blocks.SANDSTONE, Blocks.SANDSTONE_STAIRS,
            Blocks.CUT_SANDSTONE, Blocks.SANDSTONE_STAIRS, Blocks.CUT_SANDSTONE_SLAB,
            Blocks.BRICKS, Blocks.BRICK_STAIRS, Blocks.BRICK_SLAB,
            Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_STAIRS, Blocks.JUNGLE_SLAB,
            Blocks.JUNGLE_FENCE, Blocks.JUNGLE_DOOR, Blocks.JUNGLE_TRAPDOOR,
            Blocks.CUT_SANDSTONE, Blocks.PACKED_MUD, Blocks.STAINED_GLASS_PANE.pick(DyeColor.ORANGE));

    public static final CastleStyle SILBERAU = new CastleStyle(
            Blocks.POLISHED_DIORITE, Blocks.DIORITE, Blocks.CALCITE, Blocks.POLISHED_DIORITE_STAIRS,
            Blocks.POLISHED_DIORITE_SLAB, Blocks.DIORITE_WALL,
            Blocks.POLISHED_ANDESITE, Blocks.POLISHED_ANDESITE_STAIRS,
            Blocks.SMOOTH_QUARTZ, Blocks.SMOOTH_QUARTZ_STAIRS, Blocks.SMOOTH_QUARTZ_SLAB,
            Blocks.CUT_COPPER.waxed().pick(WeatheringCopper.WeatherState.OXIDIZED), Blocks.CUT_COPPER_STAIRS.waxed().pick(WeatheringCopper.WeatherState.OXIDIZED), Blocks.CUT_COPPER_SLAB.waxed().pick(WeatheringCopper.WeatherState.OXIDIZED),
            Blocks.BIRCH_PLANKS, Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_STAIRS, Blocks.BIRCH_SLAB,
            Blocks.BIRCH_FENCE, Blocks.BIRCH_DOOR, Blocks.BIRCH_TRAPDOOR,
            Blocks.SMOOTH_STONE, Blocks.GRAVEL, Blocks.STAINED_GLASS_PANE.pick(DyeColor.LIME));

    public static CastleStyle of(Realm realm) {
        return switch (realm) {
            case EISENMARK -> EISENMARK;
            case RABENSTEIN -> RABENSTEIN;
            case DORNFELS -> DORNFELS;
            case SILBERAU -> SILBERAU;
        };
    }
}
