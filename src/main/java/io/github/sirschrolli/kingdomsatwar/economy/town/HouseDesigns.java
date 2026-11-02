package io.github.sirschrolli.kingdomsatwar.economy.town;

import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.barrel;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.beam;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.bracketLantern;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.chimney;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.cross;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.facing;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.fieldstone;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.flowerBox;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.frame;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.frameWallX;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.frameWallZ;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.litCampfire;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.pane;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.planks;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.plasterWallX;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.plasterWallZ;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.s;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.seat;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.sill;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.step;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.stone;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.wallBanner;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.waterCauldron;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.window;

import io.github.sirschrolli.kingdomsatwar.economy.town.PlanBuilder.RoofShape;
import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Weitere Entwürfe für Wohnhaus, Bürgerhaus und Taverne. Wie bei {@link Blueprints}: vorn ist −z, y = 0 ist der Fußboden
 * eine Lage über dem Gelände. Jeder Entwurf legt mit "camera"/"look" fest, wohin die Vorführung innen schaut.
 */
final class HouseDesigns {
    private HouseDesigns() {}

    private static BlockState chest(Direction facing) {
        return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing);
    }

    private static BlockState candle() {
        return Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true);
    }

    // =====================================================================================
    // Wohnhaus: Steinkate
    // =====================================================================================

    /**
     * Steinkate (2 Betten): niedriges Haus aus Feldstein 9 × 7 mit Eckquadern und Fichtenholz-Rähm, Krüppelwalmdach aus
     * Holzschindeln mit Brettergiebeln, grüne Läden und Blumenkästen. Innen ein offener Kamin, dessen Schornstein in der
     * Giebelwand hochsteigt, Esstisch, Kochecke mit Kessel und Werkbank, zwei Betten mit Truhe. An der linken Giebelseite
     * liegt unter einem kleinen Dach das Brennholz, davor der Hackklotz.
     */
    static void cottage(PlanBuilder b) {
        BuildingSet roof = ModBlocks.WOODEN_SHINGLES;
        Block shutter = ModBlocks.GREEN_SHUTTERS.get();
        BlockState boards = s(Blocks.SPRUCE_PLANKS);
        b.foundation(-5, -3, 4, 3, fieldstone());
        b.fill(-4, 0, -3, 4, 0, 3, stone());
        b.fill(-3, 0, -2, 3, 0, 2, boards);
        b.ring(-4, -3, 4, 3, 1, 3, fieldstone());
        for (int x : new int[] {-4, 4}) {
            for (int z : new int[] {-3, 3}) {
                b.fill(x, 1, z, x, 3, z, stone());
            }
        }
        b.air(-3, 1, -2, 3, 3, 2);
        b.roof(RoofShape.HALF_HIP_X, -4, -3, 4, 3, 4, roof, boards);
        int top = b.roofs();
        b.fill(-4, 4, -3, 4, 4, -3, beam(Direction.Axis.X));
        b.fill(-4, 4, 3, 4, 4, 3, beam(Direction.Axis.X));
        // Tür mit Holzsturz, Fenster mit Läden, kleine Fenster in den Brettergiebeln
        b.door(0, 1, -3, Blocks.SPRUCE_DOOR, Direction.SOUTH);
        b.set(0, 3, -3, beam(Direction.Axis.X));
        window(b, -2, 2, -3, 2, Direction.NORTH, Direction.EAST, shutter);
        window(b, 2, 2, -3, 2, Direction.NORTH, Direction.EAST, shutter);
        window(b, -2, 2, 3, 2, Direction.SOUTH, Direction.EAST, shutter);
        window(b, 2, 2, 3, 2, Direction.SOUTH, Direction.EAST, shutter);
        window(b, 4, 2, -1, 2, Direction.EAST, Direction.SOUTH, shutter);
        b.set(-4, 5, 0, pane());
        b.set(4, 5, -1, pane());
        flowerBox(b, -2, 1, -3, 1, Direction.EAST, Direction.NORTH, ModBlocks.LAVENDER.get());
        flowerBox(b, 2, 1, -3, 1, Direction.EAST, Direction.NORTH, ModBlocks.GARDEN_ROSE.get());
        // offener Kamin an der rechten Giebelwand; der Schornstein steigt in der Wand hoch
        b.fill(3, 1, 0, 3, 2, 0, s(Blocks.BRICKS));
        b.fill(3, 1, 2, 3, 2, 2, s(Blocks.BRICKS));
        b.fill(3, 3, 0, 3, 3, 2, s(Blocks.BRICKS));
        b.set(3, 1, 1, litCampfire().setValue(CampfireBlock.FACING, Direction.WEST));
        chimney(b, 4, 1, 1, top + 1);
        // Schlafecke: zwei Betten, Truhe, Teppich, Kleiderfass
        b.bed(-3, 1, 1, Blocks.BED.green(), Direction.SOUTH);
        b.bed(-2, 1, 1, Blocks.BED.green(), Direction.SOUTH);
        b.chestAt(-1, 1, 2, Direction.NORTH);
        b.set(-3, 1, 0, Blocks.CARPET.green());
        b.set(-2, 1, 0, Blocks.CARPET.green());
        b.set(-3, 1, -2, barrel(Direction.UP));
        b.set(-3, 2, -2, s(Blocks.POTTED_FERN));
        // Esstisch vor dem Kamin, Kochecke mit Kessel und Werkbank
        b.set(1, 1, 1, ModBlocks.OAK_TABLE.get());
        b.set(1, 1, 0, seat(ModBlocks.OAK_CHAIR.get(), Direction.SOUTH));
        b.set(0, 1, 1, seat(ModBlocks.OAK_CHAIR.get(), Direction.EAST));
        b.set(1, 2, 1, s(Blocks.POTTED_RED_TULIP));
        b.set(1, 1, 2, barrel(Direction.UP));
        b.set(3, 1, -1, waterCauldron());
        b.set(3, 1, -2, s(Blocks.CRAFTING_TABLE));
        // Zugbalken mit zwei Laternen
        b.fill(-3, 4, 0, 3, 4, 0, beam(Direction.Axis.X));
        b.hangingLantern(-2, 3, 0, Blocks.LANTERN);
        b.hangingLantern(1, 3, 0, Blocks.LANTERN);
        // Brennholz unter einem kleinen Dach an der linken Giebelseite, davor der Hackklotz
        for (int z = -2; z <= 2; z++) {
            b.set(-5, 0, z, PlanBuilder.log(Blocks.OAK_LOG, Direction.Axis.X));
            b.set(-5, 1, z, PlanBuilder.log(z == 0 ? Blocks.BIRCH_LOG : Blocks.OAK_LOG, Direction.Axis.X));
        }
        for (int z : new int[] {-3, 3}) {
            b.fill(-5, 0, z, -5, 1, z, s(Blocks.SPRUCE_FENCE));
        }
        for (int z = -3; z <= 3; z++) {
            b.set(-5, 2, z, PlanBuilder.stairs(roof.stairs().get(), Direction.EAST, Half.BOTTOM));
        }
        b.set(-6, 0, 0, PlanBuilder.log(Blocks.STRIPPED_OAK_LOG, Direction.Axis.Y));
        step(b, 0, -4, Direction.SOUTH, ModBlocks.MEDIEVAL_STONE.stairs().get());
        b.entranceAt(0, 0, -5);
        b.anchor("camera", 2, 1, -2).anchor("look", -3, 1, 2);
        b.anchor("camera2", -1, 1, -2).anchor("look2", 3, 1, 1);
    }

    // =====================================================================================
    // Wohnhaus: Winkelhaus mit Giebelvorbau
    // =====================================================================================

    /**
     * Haus mit Giebelvorbau (2 Betten): ockerfarbener Lehmputz zwischen dunklen Balken auf einem Steinsockel, braunes
     * Ziegeldach. Vorn springt ein Giebelvorbau mit der Haustür und zwei Laternen vor; durch die Diele mit Bank geht es in
     * die Stube: links die Schlafecke, in der Mitte die lange Tafel, rechts die Küche mit dem Herd am Giebelschornstein.
     */
    static void gabledHouse(PlanBuilder b) {
        BuildingSet roof = ModBlocks.BROWN_ROOF_TILES;
        Block shutter = ModBlocks.RED_SHUTTERS.get();
        BlockState clay = s(ModBlocks.CLAY_PLASTER.base().get());
        b.foundation(-4, -2, 4, 4, stone());
        b.foundation(-4, -3, 4, -3, stone());
        b.foundation(-2, -5, 2, -4, stone());
        b.fill(-4, 0, -2, 4, 0, 4, stone());
        b.fill(-2, 0, -5, 2, 0, -3, stone());
        b.fill(-3, 0, -1, 3, 0, 3, planks());
        b.fill(-1, 0, -4, 1, 0, -2, planks());
        plasterWallX(b, -4, 4, -2, 1, 4, clay);
        plasterWallX(b, -4, 4, 4, 1, 4, clay);
        plasterWallZ(b, -2, 4, -4, 1, 4, clay);
        plasterWallZ(b, -2, 4, 4, 1, 4, clay);
        plasterWallX(b, -2, 2, -5, 1, 4, clay);
        plasterWallZ(b, -5, -2, -2, 1, 4, clay);
        plasterWallZ(b, -5, -2, 2, 1, 4, clay);
        b.air(-3, 1, -1, 3, 4, 3);
        b.air(-1, 1, -4, 1, 4, -3);
        b.air(-1, 1, -2, 1, 3, -2);
        b.roof(RoofShape.GABLE_X, -4, -2, 4, 4, 5, roof, clay);
        b.roof(RoofShape.GABLE_Z, -2, -5, 2, -2, 5, roof, clay);
        int top = b.roofs();
        // Giebelvorbau: Tür, zwei Laternen, Giebelfenster über einem Querbalken
        b.door(0, 1, -5, Blocks.SPRUCE_DOOR, Direction.SOUTH);
        b.set(0, 3, -5, beam(Direction.Axis.X));
        bracketLantern(b, -1, 2, -5, Direction.NORTH, Blocks.LANTERN);
        bracketLantern(b, 1, 2, -5, Direction.NORTH, Blocks.LANTERN);
        b.fill(-1, 5, -5, 1, 5, -5, beam(Direction.Axis.X));
        b.set(0, 6, -5, pane());
        window(b, -2, 2, -4, 2, Direction.WEST, Direction.SOUTH, shutter);
        window(b, 2, 2, -4, 2, Direction.EAST, Direction.SOUTH, shutter);
        // Haupthaus: Fenster hinten und in den Giebeln
        window(b, -2, 2, 4, 2, Direction.SOUTH, Direction.EAST, shutter);
        window(b, 2, 2, 4, 2, Direction.SOUTH, Direction.EAST, shutter);
        window(b, -4, 2, 0, 2, Direction.WEST, Direction.SOUTH, shutter);
        window(b, 4, 2, 0, 2, Direction.EAST, Direction.SOUTH, shutter);
        for (int x : new int[] {-4, 4}) {
            b.set(x, 6, 1, pane());
            b.set(x, 7, 1, pane());
        }
        // in den Winkeln neben dem Vorbau: eine Bank, Fässer mit einer Blume
        b.set(-3, 0, -3, seat(ModBlocks.OAK_BENCH.get(), Direction.NORTH));
        b.set(3, 0, -3, barrel(Direction.UP));
        b.set(3, 1, -3, s(Blocks.POTTED_POPPY));
        // Diele: Bank, Fass, Laterne an einem Querbalken
        b.set(-1, 1, -4, seat(ModBlocks.OAK_BENCH.get(), Direction.EAST));
        b.set(1, 1, -4, barrel(Direction.UP));
        b.fill(-1, 5, -3, 1, 5, -3, beam(Direction.Axis.X));
        b.hangingLantern(0, 4, -3, Blocks.LANTERN);
        // Schlafecke links
        b.bed(-3, 1, 2, Blocks.BED.orange(), Direction.SOUTH);
        b.bed(-2, 1, 2, Blocks.BED.orange(), Direction.SOUTH);
        b.chestAt(-1, 1, 3, Direction.NORTH);
        b.set(-3, 1, 1, Blocks.CARPET.orange());
        b.set(-2, 1, 1, Blocks.CARPET.orange());
        b.set(-3, 1, -1, barrel(Direction.UP));
        // Küche rechts: Herd am Schornstein in der Giebelwand
        b.set(3, 1, 2, facing(Blocks.FURNACE, Direction.WEST));
        b.fill(3, 2, 2, 3, 3, 2, s(Blocks.BRICKS));
        chimney(b, 4, 2, 1, top + 1);
        b.set(3, 1, 3, barrel(Direction.UP));
        b.set(3, 1, 1, s(Blocks.CRAFTING_TABLE));
        b.set(3, 1, -1, waterCauldron());
        // lange Tafel mit Bänken
        b.set(0, 1, 1, ModBlocks.OAK_TABLE.get());
        b.set(1, 1, 1, ModBlocks.OAK_TABLE.get());
        b.set(0, 1, 0, seat(ModBlocks.OAK_BENCH.get(), Direction.SOUTH));
        b.set(1, 1, 0, seat(ModBlocks.OAK_BENCH.get(), Direction.SOUTH));
        b.set(0, 1, 2, seat(ModBlocks.OAK_BENCH.get(), Direction.NORTH));
        b.set(1, 1, 2, seat(ModBlocks.OAK_BENCH.get(), Direction.NORTH));
        b.set(0, 2, 1, s(Blocks.POTTED_BLUE_ORCHID));
        b.set(1, 2, 1, candle());
        b.fill(-3, 5, 1, 3, 5, 1, beam(Direction.Axis.X));
        b.hangingLantern(-2, 4, 1, Blocks.LANTERN);
        b.hangingLantern(2, 4, 1, Blocks.LANTERN);
        step(b, 0, -6, Direction.SOUTH, ModBlocks.MEDIEVAL_STONE.stairs().get());
        b.entranceAt(0, 0, -7);
        b.anchor("camera", 2, 1, -1).anchor("look", -3, 1, 3);
        b.anchor("camera2", -2, 1, 0).anchor("look2", 3, 1, 2);
    }

    // =====================================================================================
    // Wohnhaus: zweigeschossig mit Walmdach
    // =====================================================================================

    /**
     * Zweigeschossiges Haus (2 Betten): unten verwittertes Mauerwerk mit Eckquadern, oben Fachwerk, das vorn auf Knaggen
     * vorkragt, darüber ein Walmdach aus dunklem Schiefer. Unten Küche und Stube mit Herd am Schornstein in der Rückwand;
     * eine Treppe mit drei Blöcken Kopffreiheit führt in die Schlafkammer mit Webstuhl und Blumenkästen vor den Fenstern.
     */
    static void twoStoreyHouse(PlanBuilder b) {
        BuildingSet roof = ModBlocks.DARK_SLATE_SHINGLES;
        Block shutter = ModBlocks.GREEN_SHUTTERS.get();
        BlockState wall = s(ModBlocks.WEATHERED_STONE_BRICKS.base().get());
        b.foundation(-3, -3, 3, 3, fieldstone());
        b.fill(-3, 0, -3, 3, 0, 3, stone());
        b.fill(-2, 0, -2, 2, 0, 2, planks());
        b.ring(-3, -3, 3, 3, 1, 3, wall);
        for (int x : new int[] {-3, 3}) {
            for (int z : new int[] {-3, 3}) {
                b.fill(x, 1, z, x, 3, z, stone());
            }
        }
        b.air(-2, 1, -2, 2, 3, 2);
        // Balkenlage: kragt vorn einen Block vor, darunter Knaggen
        b.fill(-3, 4, -4, 3, 4, 3, planks());
        for (int x : new int[] {-3, -1, 1, 3}) {
            b.set(x, 3, -4, PlanBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.TOP));
        }
        frameWallX(b, -3, 3, -4, 5, 8);
        frameWallX(b, -3, 3, 3, 5, 8);
        frameWallZ(b, -4, 3, -3, 5, 8);
        frameWallZ(b, -4, 3, 3, 5, 8);
        b.air(-2, 5, -3, 2, 8, 2);
        b.roof(RoofShape.HIP, -3, -4, 3, 3, 9, roof, frame());
        int top = b.roofs();
        // Erdgeschoss: Tür, kleine Fenster mit Läden und Fensterbänken
        b.door(0, 1, -3, Blocks.SPRUCE_DOOR, Direction.SOUTH);
        for (int x : new int[] {-2, 2}) {
            window(b, x, 2, -3, 1, Direction.NORTH, Direction.EAST, shutter);
            sill(b, x, 1, -3, Direction.NORTH, ModBlocks.WEATHERED_STONE_BRICKS.stairs().get());
        }
        window(b, -3, 2, 0, 2, Direction.WEST, Direction.SOUTH, shutter);
        window(b, 3, 2, 0, 2, Direction.EAST, Direction.SOUTH, shutter);
        window(b, -1, 2, 3, 2, Direction.SOUTH, Direction.EAST, shutter);
        // Obergeschoss: Fenster mit Blumenkästen vorn
        window(b, -2, 6, -4, 2, Direction.NORTH, Direction.EAST, shutter);
        window(b, 2, 6, -4, 2, Direction.NORTH, Direction.EAST, shutter);
        b.set(0, 6, -4, cross());
        b.set(0, 7, -4, cross());
        flowerBox(b, -2, 5, -4, 1, Direction.EAST, Direction.NORTH, Blocks.POPPY);
        flowerBox(b, 2, 5, -4, 1, Direction.EAST, Direction.NORTH, Blocks.CORNFLOWER);
        window(b, -3, 6, -1, 2, Direction.WEST, Direction.SOUTH, shutter);
        window(b, 3, 6, 0, 2, Direction.EAST, Direction.SOUTH, shutter);
        window(b, -1, 6, 3, 2, Direction.SOUTH, Direction.EAST, shutter);
        // Treppe an der linken Wand: vier Stufen bis in den Boden des Obergeschosses, Treppenloch mit Geländer
        for (int i = 1; i <= 4; i++) {
            b.set(-2, i, i - 2, PlanBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.BOTTOM));
        }
        b.air(-2, 4, -1, -2, 4, 1);
        b.set(-2, 1, 0, barrel(Direction.UP));
        b.set(-2, 1, 1, barrel(Direction.UP));
        b.fill(-2, 1, 2, -2, 2, 2, barrel(Direction.UP));
        for (int z = -1; z <= 1; z++) {
            b.set(-1, 5, z, s(Blocks.SPRUCE_FENCE));
        }
        b.set(-2, 5, -2, s(Blocks.SPRUCE_FENCE));
        // Küche: Herd am Schornstein in der Rückwand, Fass, Werkbank, Kessel; Tisch mit zwei Stühlen
        b.set(2, 1, 2, facing(Blocks.FURNACE, Direction.NORTH));
        chimney(b, 2, 3, 1, top);
        b.set(1, 1, 2, barrel(Direction.UP));
        b.set(2, 1, 1, s(Blocks.CRAFTING_TABLE));
        b.set(2, 1, -2, waterCauldron());
        b.set(0, 1, 0, ModBlocks.OAK_TABLE.get());
        b.set(0, 1, -1, seat(ModBlocks.OAK_CHAIR.get(), Direction.SOUTH));
        b.set(1, 1, 0, seat(ModBlocks.OAK_CHAIR.get(), Direction.WEST));
        b.set(0, 2, 0, s(Blocks.POTTED_DANDELION));
        b.hangingLantern(0, 3, 1, Blocks.LANTERN);
        // Schlafkammer oben
        b.bed(1, 5, 1, Blocks.BED.lightBlue(), Direction.SOUTH);
        b.bed(2, 5, 1, Blocks.BED.lightBlue(), Direction.SOUTH);
        b.chestAt(2, 5, 0, Direction.WEST);
        for (int x = 0; x <= 1; x++) {
            for (int z = -1; z <= 0; z++) {
                b.set(x, 5, z, Blocks.CARPET.lightBlue());
            }
        }
        b.set(-2, 5, -3, facing(Blocks.LOOM, Direction.SOUTH));
        b.set(1, 5, -3, ModBlocks.OAK_TABLE.get());
        b.set(1, 5, -2, seat(ModBlocks.OAK_CHAIR.get(), Direction.NORTH));
        b.set(1, 6, -3, candle());
        b.set(2, 5, -3, barrel(Direction.UP));
        b.set(2, 6, -3, s(Blocks.POTTED_AZALEA));
        b.fill(-2, 9, -1, 2, 9, -1, beam(Direction.Axis.X));
        b.hangingLantern(0, 8, -1, Blocks.LANTERN);
        step(b, 0, -4, Direction.SOUTH, ModBlocks.WEATHERED_STONE_BRICKS.stairs().get());
        b.entranceAt(0, 0, -5);
        b.anchor("camera", 2, 1, -2).anchor("look", -2, 2, 1);
        b.anchor("camera2", 0, 5, -2).anchor("look2", 2, 5, 2);
    }

    // =====================================================================================
    // Bürgerhaus: Treppengiebelhaus aus Stein
    // =====================================================================================

    /**
     * Steinernes Bürgerhaus (4 Betten): zwei hohe Geschosse aus hellem Kalkstein mit Eckquadern und einem Gurtgesims,
     * vorn ein Treppengiebel, der in Stufen über das rote Ziegeldach steigt. Ein Säulenportal führt in die Halle mit der
     * Tafel unter dem Kronleuchter, der Schreibecke und der Küche; die Treppe (mit Bücherregalen darunter) führt in die
     * Kammern mit vier Betten. Blumenkästen vor den Fenstern des Obergeschosses, der Schornstein steigt im hinteren Giebel.
     */
    static void stoneTownhouse(PlanBuilder b) {
        BuildingSet roof = ModBlocks.RED_ROOF_TILES;
        Block shutter = ModBlocks.GREEN_SHUTTERS.get();
        BlockState wall = s(ModBlocks.LIMESTONE_BRICKS.base().get());
        BlockState trim = stone();
        BlockState floor = s(Blocks.DARK_OAK_PLANKS);
        b.foundation(-4, -4, 4, 4, trim);
        b.fill(-4, 0, -4, 4, 0, 4, trim);
        b.fill(-3, 0, -3, 3, 0, 3, floor);
        b.ring(-4, -4, 4, 4, 1, 9, wall);
        for (int x : new int[] {-4, 4}) {
            for (int z : new int[] {-4, 4}) {
                b.fill(x, 1, z, x, 9, z, trim);
            }
        }
        b.ring(-4, -4, 4, 4, 5, 5, trim);
        b.air(-3, 1, -3, 3, 4, 3);
        b.fill(-3, 5, -3, 3, 5, 3, floor);
        b.air(-3, 6, -3, 3, 9, 3);
        b.roof(RoofShape.GABLE_Z, -4, -4, 4, 4, 10, roof, wall);
        b.roofs();
        // Treppengiebel vorn: kein Dachüberstand, die Giebelwand steigt in Stufen zwei Blöcke über das Dach
        for (int x = -5; x <= 5; x++) {
            b.set(x, b.roofTop(x, -5), -5, s(Blocks.AIR));
        }
        int[] steps = {13, 13, 15, 15, 17, 15, 15, 13, 13};
        for (int x = -4; x <= 4; x++) {
            int h = steps[x + 4];
            for (int y = 10; y <= h; y++) {
                b.set(x, y, -4, x == -4 || x == 4 ? trim : wall);
            }
            b.set(x, h + 1, -4, PlanBuilder.slab(ModBlocks.MEDIEVAL_STONE.slab().get(), SlabType.BOTTOM));
        }
        b.set(0, 17, -4, s(Blocks.CHISELED_STONE_BRICKS));
        b.set(0, 11, -4, pane());
        b.set(0, 12, -4, pane());
        b.set(-2, 11, -4, pane());
        b.set(2, 11, -4, pane());
        // Säulenportal mit Schlussstein
        b.door(0, 1, -4, Blocks.DARK_OAK_DOOR, Direction.SOUTH);
        for (int x : new int[] {-1, 1}) {
            b.fill(x, 1, -5, x, 2, -5, s(ModBlocks.STONE_PILLAR.get()));
        }
        b.set(-1, 3, -5, PlanBuilder.stairs(ModBlocks.MEDIEVAL_STONE.stairs().get(), Direction.EAST, Half.TOP));
        b.set(1, 3, -5, PlanBuilder.stairs(ModBlocks.MEDIEVAL_STONE.stairs().get(), Direction.WEST, Half.TOP));
        b.set(0, 3, -5, s(Blocks.CHISELED_STONE_BRICKS));
        // Fenster: unten mit Läden, Steinbank und Sturz; oben mit Blumenkästen
        for (int x : new int[] {-3, 3}) {
            window(b, x, 2, -4, 2, Direction.NORTH, Direction.EAST, shutter);
            sill(b, x, 1, -4, Direction.NORTH, ModBlocks.MEDIEVAL_STONE.stairs().get());
            b.set(x, 4, -4, trim);
        }
        Block[] flowers = {ModBlocks.GARDEN_ROSE.get(), Blocks.AZURE_BLUET, ModBlocks.LAVENDER.get()};
        int f = 0;
        for (int x : new int[] {-3, 0, 3}) {
            b.set(x, 7, -4, pane());
            b.set(x, 8, -4, pane());
            b.set(x, 9, -4, trim);
            flowerBox(b, x, 6, -4, 1, Direction.EAST, Direction.NORTH, flowers[f++]);
        }
        window(b, -4, 2, -2, 2, Direction.WEST, Direction.SOUTH, shutter);
        window(b, 4, 2, 0, 2, Direction.EAST, Direction.SOUTH, shutter);
        window(b, -4, 7, 0, 2, Direction.WEST, Direction.SOUTH, shutter);
        window(b, 4, 7, 0, 2, Direction.EAST, Direction.SOUTH, shutter);
        window(b, -1, 2, 4, 2, Direction.SOUTH, Direction.EAST, shutter);
        window(b, -2, 7, 4, 2, Direction.SOUTH, Direction.EAST, shutter);
        window(b, 0, 7, 4, 2, Direction.SOUTH, Direction.EAST, shutter);
        // Treppe an der linken Wand, darunter Bücherregale; oben ein Geländer ums Treppenloch
        for (int i = 1; i <= 5; i++) {
            b.set(-3, i, i - 3, PlanBuilder.stairs(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, Half.BOTTOM));
        }
        b.air(-3, 5, -2, -3, 5, 1);
        b.set(-3, 1, -1, barrel(Direction.UP));
        b.fill(-3, 1, 0, -3, 2, 0, s(Blocks.BOOKSHELF));
        b.fill(-3, 1, 1, -3, 3, 1, s(Blocks.BOOKSHELF));
        b.fill(-3, 1, 2, -3, 4, 2, s(Blocks.BOOKSHELF));
        for (int z = -2; z <= 1; z++) {
            b.set(-2, 6, z, s(Blocks.DARK_OAK_FENCE));
        }
        b.set(-3, 6, -3, s(Blocks.DARK_OAK_FENCE));
        // Küche hinten rechts: Herd am Schornstein im hinteren Giebel
        b.set(2, 1, 3, facing(Blocks.FURNACE, Direction.NORTH));
        chimney(b, 2, 4, 1, 16);
        b.set(1, 1, 3, facing(Blocks.SMOKER, Direction.NORTH));
        b.set(3, 1, 3, barrel(Direction.UP));
        b.set(3, 1, 2, waterCauldron());
        b.set(3, 1, 1, s(Blocks.CRAFTING_TABLE));
        // Tafel unter dem Kronleuchter
        b.set(-1, 1, 0, ModBlocks.OAK_TABLE.get());
        b.set(0, 1, 0, ModBlocks.OAK_TABLE.get());
        b.set(-1, 1, -1, seat(ModBlocks.OAK_CHAIR.get(), Direction.SOUTH));
        b.set(0, 1, -1, seat(ModBlocks.OAK_CHAIR.get(), Direction.SOUTH));
        b.set(-1, 1, 1, seat(ModBlocks.OAK_CHAIR.get(), Direction.NORTH));
        b.set(0, 1, 1, seat(ModBlocks.OAK_CHAIR.get(), Direction.NORTH));
        b.set(1, 1, 0, seat(ModBlocks.OAK_CHAIR.get(), Direction.WEST));
        b.set(-1, 2, 0, candle());
        b.set(0, 2, 0, s(Blocks.POTTED_RED_TULIP));
        b.set(0, 4, 0, s(ModBlocks.CHANDELIER.get()));
        // Schreibecke vorn rechts
        b.fill(3, 1, -3, 3, 2, -3, s(Blocks.BOOKSHELF));
        b.set(3, 1, -2, facing(Blocks.LECTERN, Direction.WEST));
        b.set(2, 1, -3, s(Blocks.CHISELED_BOOKSHELF));
        // Kammern oben: vier Betten, Truhen, Kleiderfässer, Teppich, Kronleuchter am Zugbalken
        b.bed(1, 6, -2, Blocks.BED.red(), Direction.NORTH);
        b.bed(2, 6, -2, Blocks.BED.red(), Direction.NORTH);
        b.bed(0, 6, 2, Blocks.BED.blue(), Direction.SOUTH);
        b.bed(1, 6, 2, Blocks.BED.blue(), Direction.SOUTH);
        b.chestAt(3, 6, -3, Direction.WEST);
        b.set(-1, 6, 3, chest(Direction.NORTH));
        b.fill(3, 6, 0, 3, 7, 0, barrel(Direction.WEST));
        b.set(3, 6, 3, barrel(Direction.UP));
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 0; z++) {
                b.set(x, 6, z, Blocks.CARPET.red());
            }
        }
        b.fill(-3, 10, 0, 3, 10, 0, beam(Direction.Axis.X));
        b.set(0, 9, 0, s(ModBlocks.CHANDELIER.get()));
        step(b, 0, -5, Direction.SOUTH, ModBlocks.MEDIEVAL_STONE.stairs().get());
        b.entranceAt(0, 0, -6);
        b.anchor("camera", 2, 1, -2).anchor("look", -2, 1, 1);
        b.anchor("camera2", -1, 6, -2).anchor("look2", 1, 6, 3);
    }

    // =====================================================================================
    // Bürgerhaus: Kaufmannshaus mit Doppelgiebel
    // =====================================================================================

    /**
     * Kaufmannshaus (4 Betten): unten ein Steingeschoss mit Ladenfenster, Theke und gestreifter Markise, oben Fachwerk
     * unter zwei Giebeln nebeneinander (dazwischen die Dachrinne). Links Tür und Treppe, rechts der Laden mit Fässern,
     * hinten Küche und Tafel; oben vier Betten unter den beiden Giebeln.
     */
    static void merchantHouse(PlanBuilder b) {
        BuildingSet roof = ModBlocks.BROWN_ROOF_TILES;
        Block shutter = ModBlocks.RED_SHUTTERS.get();
        b.foundation(-5, -3, 5, 4, fieldstone());
        b.fill(-5, 0, -3, 5, 0, 4, stone());
        b.fill(-4, 0, -2, 4, 0, 3, planks());
        b.ring(-5, -3, 5, 4, 1, 4, stone());
        b.air(-4, 1, -2, 4, 4, 3);
        b.fill(-5, 5, -3, 5, 5, 4, planks());
        frameWallX(b, -5, 5, -3, 6, 9);
        frameWallX(b, -5, 5, 4, 6, 9);
        frameWallZ(b, -3, 4, -5, 6, 9);
        frameWallZ(b, -3, 4, 5, 6, 9);
        b.fill(0, 6, -3, 0, 9, -3, beam(Direction.Axis.Y));
        b.fill(0, 6, 4, 0, 9, 4, beam(Direction.Axis.Y));
        b.air(-4, 6, -2, 4, 9, 3);
        b.roof(RoofShape.GABLE_Z, -5, -3, -1, 4, 10, roof, frame());
        b.roof(RoofShape.GABLE_Z, 1, -3, 5, 4, 10, roof, frame());
        int top = b.roofs();
        // Tür links, daneben ein Fenster
        b.door(-2, 1, -3, Blocks.DARK_OAK_DOOR, Direction.SOUTH);
        step(b, -2, -4, Direction.SOUTH, ModBlocks.MEDIEVAL_STONE.stairs().get());
        window(b, -4, 2, -3, 2, Direction.NORTH, Direction.EAST, shutter);
        // Ladenfenster: Öffnung über der Mauerbrüstung, außen die Theke, darüber die Markise
        b.air(1, 2, -3, 3, 3, -3);
        b.fill(1, 4, -3, 3, 4, -3, beam(Direction.Axis.X));
        for (int x = 1; x <= 3; x++) {
            b.set(x, 1, -4, PlanBuilder.slab(Blocks.SPRUCE_SLAB, SlabType.TOP));
        }
        b.set(1, 2, -4, s(Blocks.DECORATED_POT));
        b.set(3, 2, -4, s(Blocks.POTTED_RED_TULIP));
        for (int x = 0; x <= 4; x++) {
            Block wool = (x & 1) == 0 ? Blocks.WOOL_STAIRS.red() : Blocks.WOOL_STAIRS.white();
            b.set(x, 4, -4, PlanBuilder.stairs(wool, Direction.SOUTH, Half.BOTTOM));
        }
        b.set(4, 0, -4, barrel(Direction.UP));
        b.set(4, 1, -4, s(Blocks.POTTED_AZALEA));
        // Obergeschoss: ein Fenster unter jedem Giebel, Blumenkästen, Giebelfenster
        window(b, -3, 7, -3, 2, Direction.NORTH, Direction.EAST, shutter);
        window(b, 3, 7, -3, 2, Direction.NORTH, Direction.EAST, shutter);
        flowerBox(b, -3, 6, -3, 1, Direction.EAST, Direction.NORTH, Blocks.ALLIUM);
        flowerBox(b, 3, 6, -3, 1, Direction.EAST, Direction.NORTH, Blocks.OXEYE_DAISY);
        for (int x : new int[] {-3, 3}) {
            b.set(x, 11, -3, pane());
            b.set(x, 12, -3, pane());
            b.set(x, 11, 4, pane());
        }
        window(b, -5, 7, 0, 2, Direction.WEST, Direction.SOUTH, shutter);
        window(b, 5, 7, 0, 2, Direction.EAST, Direction.SOUTH, shutter);
        window(b, 5, 2, 0, 2, Direction.EAST, Direction.SOUTH, shutter);
        window(b, -2, 7, 4, 2, Direction.SOUTH, Direction.EAST, shutter);
        window(b, 1, 7, 4, 2, Direction.SOUTH, Direction.EAST, shutter);
        window(b, 0, 2, 4, 2, Direction.SOUTH, Direction.EAST, shutter);
        // Treppe an der linken Wand, darunter Fässer; Geländer oben
        for (int i = 1; i <= 5; i++) {
            b.set(-4, i, i - 2, PlanBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.BOTTOM));
        }
        b.air(-4, 5, -1, -4, 5, 2);
        b.set(-4, 1, 0, barrel(Direction.UP));
        b.fill(-4, 1, 1, -4, 2, 1, barrel(Direction.UP));
        b.fill(-4, 1, 2, -4, 3, 2, barrel(Direction.UP));
        b.fill(-4, 1, 3, -4, 4, 3, planks());
        for (int z = -1; z <= 2; z++) {
            b.set(-3, 6, z, s(Blocks.SPRUCE_FENCE));
        }
        b.set(-4, 6, -2, s(Blocks.SPRUCE_FENCE));
        // Laden: Fässer und Kisten, das Kontorpult
        b.fill(4, 1, -2, 4, 2, -2, barrel(Direction.WEST));
        b.set(4, 1, -1, barrel(Direction.WEST));
        b.set(4, 1, 0, chest(Direction.WEST));
        b.set(1, 1, 0, facing(Blocks.LECTERN, Direction.NORTH));
        b.set(2, 1, -2, s(Blocks.WOOL.white()));
        // Küche hinten rechts mit Schornstein im rechten Giebel, Tafel in der Mitte
        b.set(3, 1, 3, facing(Blocks.FURNACE, Direction.NORTH));
        chimney(b, 3, 4, 1, top + 1);
        b.set(2, 1, 3, facing(Blocks.SMOKER, Direction.NORTH));
        b.set(4, 1, 3, barrel(Direction.UP));
        b.set(4, 1, 2, waterCauldron());
        b.set(4, 1, 1, s(Blocks.CRAFTING_TABLE));
        b.set(-1, 1, 2, ModBlocks.OAK_TABLE.get());
        b.set(0, 1, 2, ModBlocks.OAK_TABLE.get());
        b.set(-1, 1, 1, seat(ModBlocks.OAK_BENCH.get(), Direction.SOUTH));
        b.set(0, 1, 1, seat(ModBlocks.OAK_BENCH.get(), Direction.SOUTH));
        b.set(-1, 1, 3, seat(ModBlocks.OAK_BENCH.get(), Direction.NORTH));
        b.set(0, 1, 3, seat(ModBlocks.OAK_BENCH.get(), Direction.NORTH));
        b.set(0, 2, 2, candle());
        b.hangingLantern(-2, 4, 0, Blocks.LANTERN);
        b.hangingLantern(2, 4, 0, Blocks.LANTERN);
        // oben: vier Betten unter den Giebeln, Truhe dazwischen, Schreibtisch vorn
        b.bed(-1, 6, 2, Blocks.BED.yellow(), Direction.SOUTH);
        b.bed(0, 6, 2, Blocks.BED.yellow(), Direction.SOUTH);
        b.bed(2, 6, 2, Blocks.BED.purple(), Direction.SOUTH);
        b.bed(3, 6, 2, Blocks.BED.purple(), Direction.SOUTH);
        b.chestAt(1, 6, 3, Direction.NORTH);
        b.set(4, 6, 3, barrel(Direction.UP));
        b.set(4, 6, -2, barrel(Direction.UP));
        b.set(4, 7, -2, barrel(Direction.UP));
        b.set(1, 6, -2, ModBlocks.OAK_TABLE.get());
        b.set(1, 6, -1, seat(ModBlocks.OAK_CHAIR.get(), Direction.NORTH));
        b.set(1, 7, -2, candle());
        b.set(-1, 6, -2, barrel(Direction.UP));
        b.set(-1, 7, -2, s(Blocks.POTTED_CORNFLOWER));
        b.hangingLantern(-1, 9, 0, Blocks.LANTERN);
        b.hangingLantern(1, 9, 0, Blocks.LANTERN);
        b.entranceAt(-2, 0, -5);
        b.anchor("camera", 3, 1, -1).anchor("look", -1, 1, 2);
        b.anchor("camera2", -2, 6, -1).anchor("look2", 2, 6, 3);
    }

    // =====================================================================================
    // Taverne: Gasthof mit Biergarten
    // =====================================================================================

    /**
     * Gasthof (Wirt, 2 Betten): unten verwittertes Mauerwerk, oben Fachwerk, rotes Dach mit einem Zwerchgiebel über der
     * Mitte; davor ein umzäunter Biergarten mit zwei Tafeln, Laternen und Blumen. In der Schankstube ein großer offener
     * Kamin, Tische mit Bänken, die Theke mit den Fässern dahinter und die Treppe zu den Gästekammern; das Wirtshausschild
     * ist ein Banner über der Tür.
     */
    static void coachingInn(PlanBuilder b) {
        BuildingSet roof = ModBlocks.RED_ROOF_TILES;
        Block shutter = ModBlocks.GREEN_SHUTTERS.get();
        BlockState wall = s(ModBlocks.WEATHERED_STONE_BRICKS.base().get());
        b.foundation(-5, -7, 5, 5, fieldstone());
        b.fill(-5, 0, -1, 5, 0, 5, stone());
        b.fill(-4, 0, 0, 4, 0, 4, s(Blocks.OAK_PLANKS));
        b.ring(-5, -1, 5, 5, 1, 4, wall);
        for (int x : new int[] {-5, 5}) {
            for (int z : new int[] {-1, 5}) {
                b.fill(x, 1, z, x, 4, z, stone());
            }
        }
        b.air(-4, 1, 0, 4, 4, 4);
        b.fill(-5, 5, -1, 5, 5, 5, planks());
        frameWallX(b, -5, 5, -1, 6, 9);
        frameWallX(b, -5, 5, 5, 6, 9);
        frameWallZ(b, -1, 5, -5, 6, 9);
        frameWallZ(b, -1, 5, 5, 6, 9);
        b.air(-4, 6, 0, 4, 9, 4);
        b.roof(RoofShape.GABLE_Z, -2, -1, 2, 2, 10, roof, frame());
        b.roof(RoofShape.GABLE_X, -5, -1, 5, 5, 10, roof, frame());
        int top = b.roofs();
        // Fassade: Tür mit Stufe, Fenster mit Läden und Blumenkästen, Banner als Schild, Fenster im Zwerchgiebel
        b.door(0, 1, -1, ModBlocks.CASTLE_DOOR.get(), Direction.SOUTH);
        b.set(0, 3, -1, beam(Direction.Axis.X));
        bracketLantern(b, -1, 2, -1, Direction.NORTH, Blocks.LANTERN);
        bracketLantern(b, 1, 2, -1, Direction.NORTH, Blocks.LANTERN);
        for (int x : new int[] {-3, 3}) {
            window(b, x, 2, -1, 2, Direction.NORTH, Direction.EAST, shutter);
            flowerBox(b, x, 1, -1, 1, Direction.EAST, Direction.NORTH, x < 0 ? ModBlocks.GARDEN_ROSE.get() : Blocks.POPPY);
            window(b, x, 7, -1, 2, Direction.NORTH, Direction.EAST, shutter);
        }
        b.set(-1, 7, -1, pane());
        b.set(1, 7, -1, pane());
        b.set(0, 8, -2, wallBanner(Blocks.WALL_BANNER.yellow(), Direction.NORTH));
        b.set(0, 10, -1, pane());
        b.set(0, 11, -1, pane());
        window(b, 5, 2, 2, 2, Direction.EAST, Direction.SOUTH, shutter);
        window(b, 5, 7, 2, 2, Direction.EAST, Direction.SOUTH, shutter);
        window(b, -5, 7, 2, 2, Direction.WEST, Direction.SOUTH, shutter);
        window(b, -2, 2, 5, 2, Direction.SOUTH, Direction.EAST, shutter);
        window(b, -2, 7, 5, 2, Direction.SOUTH, Direction.EAST, shutter);
        window(b, 2, 7, 5, 2, Direction.SOUTH, Direction.EAST, shutter);
        // großer offener Kamin an der linken Giebelwand
        b.fill(-4, 1, 1, -4, 2, 1, s(Blocks.BRICKS));
        b.fill(-4, 1, 3, -4, 2, 3, s(Blocks.BRICKS));
        b.fill(-4, 3, 1, -4, 3, 3, s(Blocks.BRICKS));
        b.set(-4, 4, 2, s(Blocks.BRICKS));
        b.set(-4, 1, 2, litCampfire().setValue(CampfireBlock.FACING, Direction.EAST));
        chimney(b, -5, 2, 1, top + 1);
        // Theke aus Fässern, dahinter Regale, die Truhe des Wirts
        for (int z = 1; z <= 3; z++) {
            b.set(2, 1, z, barrel(Direction.WEST));
        }
        b.set(2, 2, 1, s(Blocks.BREWING_STAND));
        b.set(2, 2, 3, candle());
        b.fill(4, 1, 1, 4, 2, 2, barrel(Direction.WEST));
        b.set(4, 2, 3, barrel(Direction.WEST));
        b.chestAt(4, 1, 3, Direction.WEST);
        // Tische mit Bänken
        for (int x : new int[] {-2, 0}) {
            b.set(x, 1, 2, ModBlocks.OAK_TABLE.get());
            b.set(x, 1, 1, seat(ModBlocks.OAK_BENCH.get(), Direction.SOUTH));
            b.set(x, 1, 3, seat(ModBlocks.OAK_BENCH.get(), Direction.NORTH));
            b.set(x, 2, 2, candle());
        }
        b.set(-1, 4, 2, s(ModBlocks.CHANDELIER.get()));
        b.hangingLantern(3, 4, 2, Blocks.LANTERN);
        // Treppe an der Rückwand, darunter Fässer; Geländer oben
        for (int i = 1; i <= 5; i++) {
            b.set(i - 2, i, 4, PlanBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.EAST, Half.BOTTOM));
        }
        b.air(-1, 5, 4, 2, 5, 4);
        b.set(0, 1, 4, barrel(Direction.UP));
        b.fill(1, 1, 4, 1, 2, 4, barrel(Direction.UP));
        b.fill(2, 1, 4, 2, 3, 4, barrel(Direction.UP));
        b.fill(3, 1, 4, 3, 4, 4, planks());
        for (int x = -1; x <= 2; x++) {
            b.set(x, 6, 3, s(Blocks.SPRUCE_FENCE));
        }
        b.set(-2, 6, 4, s(Blocks.SPRUCE_FENCE));
        // Gästekammern oben
        b.bed(-4, 6, 3, Blocks.BED.red(), Direction.SOUTH);
        b.bed(-3, 6, 3, Blocks.BED.green(), Direction.SOUTH);
        b.set(-4, 6, 2, barrel(Direction.UP));
        b.set(-4, 6, 0, waterCauldron());
        b.set(-2, 6, 0, ModBlocks.OAK_TABLE.get());
        b.set(-2, 6, 1, seat(ModBlocks.OAK_CHAIR.get(), Direction.NORTH));
        b.set(-2, 7, 0, candle());
        b.fill(4, 6, 0, 4, 7, 0, barrel(Direction.WEST));
        b.set(4, 6, 1, chest(Direction.WEST));
        for (int x = 0; x <= 2; x++) {
            for (int z = 0; z <= 1; z++) {
                b.set(x, 6, z, Blocks.CARPET.red());
            }
        }
        b.fill(-4, 10, 2, 4, 10, 2, beam(Direction.Axis.X));
        b.set(0, 9, 2, s(ModBlocks.CHANDELIER.get()));
        // Biergarten: Wiese, Weg, Zaun mit Tor, zwei Tafeln mit Laternen, Blumen und Sträucher
        for (int x = -4; x <= 4; x++) {
            for (int z = -6; z <= -2; z++) {
                b.set(x, -1, z, s(x == 0 && z <= -3 ? Blocks.DIRT_PATH : Blocks.GRASS_BLOCK));
            }
        }
        for (int x = -5; x <= 5; x++) {
            b.set(x, -1, -7, s(x == 0 ? Blocks.DIRT_PATH : Blocks.GRASS_BLOCK));
            if (x != 0) {
                b.set(x, 0, -7, s(Blocks.SPRUCE_FENCE));
            }
        }
        for (int z = -6; z <= -2; z++) {
            b.set(-5, -1, z, s(Blocks.GRASS_BLOCK));
            b.set(5, -1, z, s(Blocks.GRASS_BLOCK));
            b.set(-5, 0, z, s(Blocks.SPRUCE_FENCE));
            b.set(5, 0, z, s(Blocks.SPRUCE_FENCE));
        }
        b.set(0, 0, -7, Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, Direction.NORTH));
        for (int x : new int[] {-5, -1, 1, 5}) {
            b.standingLantern(x, 1, -7, Blocks.LANTERN);
        }
        for (int x0 : new int[] {-3, 2}) {
            for (int x = x0; x <= x0 + 1; x++) {
                b.set(x, 0, -4, ModBlocks.OAK_TABLE.get());
                b.set(x, 0, -5, seat(ModBlocks.OAK_BENCH.get(), Direction.SOUTH));
                b.set(x, 0, -3, seat(ModBlocks.OAK_BENCH.get(), Direction.NORTH));
            }
            b.standingLantern(x0, 1, -4, Blocks.LANTERN);
            b.set(x0 + 1, 1, -4, s(Blocks.POTTED_POPPY));
        }
        b.set(-4, 0, -6, ModBlocks.LAVENDER.get());
        b.set(-3, 0, -6, ModBlocks.GARDEN_ROSE.get());
        b.set(3, 0, -6, Blocks.CORNFLOWER);
        b.set(4, 0, -6, ModBlocks.LAVENDER.get());
        b.set(-4, 0, -2, s(Blocks.FLOWERING_AZALEA));
        b.set(4, 0, -2, s(Blocks.AZALEA));
        step(b, 0, -2, Direction.SOUTH, ModBlocks.WEATHERED_STONE_BRICKS.stairs().get());
        b.workAt(3, 1, 2);
        b.entranceAt(0, 0, -8);
        b.anchor("camera", 1, 1, 0).anchor("look", -4, 2, 2);
        b.anchor("camera2", -1, 1, 0).anchor("look2", 3, 2, 2);
    }
}
