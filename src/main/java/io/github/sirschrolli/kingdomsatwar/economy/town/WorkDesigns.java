package io.github.sirschrolli.kingdomsatwar.economy.town;

import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.barrel;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.beam;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.bracketLantern;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.chimney;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.doubleChest;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.facing;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.fieldstone;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.frameWallX;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.frameWallZ;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.lanternPost;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.litCampfire;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.pane;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.planks;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.s;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.seat;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.step;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.stone;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.wallBanner;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.wallX;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.wallZ;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.waterCauldron;
import static io.github.sirschrolli.kingdomsatwar.economy.town.Blueprints.window;

import io.github.sirschrolli.kingdomsatwar.economy.town.PlanBuilder.RoofShape;
import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Weitere Entwürfe für Feld, Holzfällerhütte, Lagerhaus, Brunnen, Schmiede, Mine, Markt, Kaserne und Kartenhaus. Wie bei
 * {@link Blueprints}: vorn ist −z, y = 0 ist der Fußboden eine Lage über dem Gelände.
 */
final class WorkDesigns {
    private WorkDesigns() {}

    private static BlockState ladder(Direction facing) {
        return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, facing);
    }

    private static BlockState gate(Direction facing) {
        return Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, facing);
    }

    /** Strohpuppe zum Üben: Pfosten, Heuballen, Arme und ein Kürbiskopf. */
    private static void dummy(PlanBuilder b, int x, int y, int z, Direction facing) {
        b.set(x, y, z, s(Blocks.SPRUCE_FENCE));
        b.set(x, y + 1, z, s(Blocks.HAY_BLOCK));
        Direction side = facing.getClockWise();
        b.set(x + side.getStepX(), y + 1, z + side.getStepZ(), s(Blocks.SPRUCE_FENCE));
        b.set(x - side.getStepX(), y + 1, z - side.getStepZ(), s(Blocks.SPRUCE_FENCE));
        b.set(x, y + 2, z, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, facing));
    }

    // =====================================================================================
    // Feld: Gemüsegarten
    // =====================================================================================

    /**
     * Gemüsegarten (für einen Bauern): vier Beete mit Karotten, Kohl, Kartoffeln und Zwiebeln, Wege aus Erde im Kreuz,
     * an jeder Ecke der Wegkreuzung ein Wasserloch mit Seerose, das die Beete feucht hält; eine Vogelscheuche, ein Zaun mit
     * zwei Toren und Laternen. Hinten steht der Geräteschuppen mit der Truhe, Fässern, Komposter und Heu.
     */
    static void kitchenGarden(PlanBuilder b) {
        b.foundation(-5, -5, 5, 8, s(Blocks.DIRT));
        BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 7);
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                boolean rim = Math.abs(x) == 5 || Math.abs(z) == 5;
                if (rim) {
                    b.set(x, -1, z, s(x == 0 ? Blocks.DIRT_PATH : Blocks.GRASS_BLOCK));
                    if (x != 0) {
                        b.set(x, 0, z, s(Blocks.SPRUCE_FENCE));
                    }
                    continue;
                }
                if (x == 0 || z == 0) {
                    b.set(x, -1, z, s(Blocks.DIRT_PATH));
                    continue;
                }
                if (Math.abs(x) == 1 && Math.abs(z) == 1) {
                    b.set(x, -1, z, s(Blocks.WATER));
                    b.set(x, 0, z, s(Blocks.LILY_PAD));
                    continue;
                }
                b.set(x, -1, z, farmland);
                Block crop = x < 0 ? (z < 0 ? Blocks.CARROTS : Blocks.POTATOES) : (z < 0 ? ModBlocks.CABBAGES.get() : ModBlocks.ONIONS.get());
                b.set(x, 0, z, crop.defaultBlockState());
            }
        }
        // Tore vorn und hinten, Laternen an den Ecken
        b.set(0, 0, -5, gate(Direction.NORTH));
        b.set(0, 0, 5, gate(Direction.SOUTH));
        for (int x : new int[] {-5, 5}) {
            b.standingLantern(x, 1, -5, Blocks.LANTERN);
        }
        // Vogelscheuche im hinteren rechten Beet
        b.set(3, -1, 3, s(Blocks.COARSE_DIRT));
        dummy(b, 3, 0, 3, Direction.NORTH);
        // Geräteschuppen hinter dem Garten: offen zum Garten, Satteldach aus Holzschindeln
        for (int x = -5; x <= 5; x++) {
            for (int z = 6; z <= 8; z++) {
                b.set(x, -1, z, s(Math.abs(x) <= 2 ? Blocks.COARSE_DIRT : Blocks.GRASS_BLOCK));
            }
        }
        wallX(b, -2, 2, 8, 0, 2, beam(Direction.Axis.Y), s(Blocks.SPRUCE_PLANKS), s(Blocks.SPRUCE_PLANKS), beam(Direction.Axis.X));
        wallZ(b, 6, 8, -2, 0, 2, beam(Direction.Axis.Y), s(Blocks.SPRUCE_PLANKS), s(Blocks.SPRUCE_PLANKS), beam(Direction.Axis.Z));
        wallZ(b, 6, 8, 2, 0, 2, beam(Direction.Axis.Y), s(Blocks.SPRUCE_PLANKS), s(Blocks.SPRUCE_PLANKS), beam(Direction.Axis.Z));
        b.air(-1, 0, 6, 1, 2, 7);
        b.roof(RoofShape.GABLE_X, -2, 6, 2, 8, 3, ModBlocks.WOODEN_SHINGLES, s(Blocks.SPRUCE_PLANKS));
        b.roofs();
        b.fill(-1, 3, 6, 1, 3, 6, beam(Direction.Axis.X));
        b.fill(-1, 3, 7, 1, 3, 7, beam(Direction.Axis.X));
        b.chestAt(-1, 0, 7, Direction.NORTH);
        b.set(1, 0, 7, barrel(Direction.UP));
        b.set(1, 1, 7, s(Blocks.POTTED_FERN));
        b.hangingLantern(0, 2, 7, Blocks.LANTERN);
        b.set(3, 0, 7, s(Blocks.COMPOSTER));
        b.set(-3, 0, 7, s(Blocks.HAY_BLOCK));
        b.set(-3, 1, 7, s(Blocks.HAY_BLOCK));
        b.set(-4, 0, 7, s(Blocks.HAY_BLOCK));
        b.workAt(0, 0, -1);
        b.entranceAt(0, 0, -6);
    }

    // =====================================================================================
    // Holzfällerhütte: Sägehütte
    // =====================================================================================

    /**
     * Sägehütte (Holzfäller, 1 Bett): unter einem großen Schindeldach links die Bretterkammer des Holzfällers, rechts der
     * offene Sägeschuppen mit Säge, Hackklotz, Truhe und gestapelten Stämmen. Draußen Stammholz unter dem Dachüberstand und
     * hinten eine Reihe Setzlinge.
     */
    static void sawHut(PlanBuilder b) {
        BuildingSet roof = ModBlocks.WOODEN_SHINGLES;
        BlockState boards = s(Blocks.SPRUCE_PLANKS);
        BlockState post = PlanBuilder.log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.Y);
        b.foundation(-3, -2, 6, 3, fieldstone());
        b.foundation(-3, 4, 3, 5, fieldstone());
        b.fill(-3, 0, -2, 5, 0, 3, fieldstone());
        b.fill(-2, 0, -1, 0, 0, 2, boards);
        b.fill(2, 0, -2, 4, 0, 2, boards);
        // Kammer (x −3..1): Bretterwände auf Feldstein, Eckpfosten
        wallX(b, -3, 1, -2, 1, 3, post, fieldstone(), boards, beam(Direction.Axis.X));
        wallX(b, -3, 5, 3, 1, 3, post, fieldstone(), boards, beam(Direction.Axis.X));
        wallZ(b, -2, 3, -3, 1, 3, post, fieldstone(), boards, beam(Direction.Axis.Z));
        wallZ(b, -2, 3, 1, 1, 3, post, fieldstone(), boards, beam(Direction.Axis.Z));
        // Schuppen (x 2..5): vorn und rechts offen, nur Eckpfosten
        b.fill(5, 1, -2, 5, 3, -2, post);
        b.fill(5, 1, 3, 5, 3, 3, post);
        b.air(-2, 1, -1, 0, 3, 2);
        b.air(2, 1, -2, 4, 3, 2);
        b.roof(RoofShape.GABLE_Z, -3, -2, 5, 3, 4, roof, boards);
        b.roofs();
        // Giebel mit Deckleisten aus geschältem Fichtenholz, ein Balkenband über die ganze Front
        for (int z : new int[] {-2, 3}) {
            for (int x = -2; x <= 4; x += 2) {
                for (int y = 5; y <= 9; y++) {
                    if (boards.equals(b.get(x, y, z))) {
                        b.set(x, y, z, PlanBuilder.log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.Y));
                    }
                }
            }
        }
        b.fill(-3, 4, -2, 5, 4, -2, beam(Direction.Axis.X));
        b.fill(-3, 4, 3, 5, 4, 3, beam(Direction.Axis.X));
        b.fill(5, 4, -1, 5, 4, 3, beam(Direction.Axis.Z));
        b.set(1, 6, -2, pane());
        b.set(1, 7, -2, pane());
        b.set(1, 6, 3, pane());
        b.set(1, 5, -3, PlanBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.TOP));
        bracketLantern(b, -3, 2, -2, Direction.NORTH, Blocks.LANTERN);
        // Kammer: Tür, Fenster, Bett, Werkbank, Fass, Laterne; Tür hinüber in den Schuppen
        b.door(-1, 1, -2, Blocks.SPRUCE_DOOR, Direction.SOUTH);
        b.set(0, 2, -2, pane());
        window(b, -3, 2, 0, 1, Direction.WEST, Direction.SOUTH, ModBlocks.GREEN_SHUTTERS.get());
        b.door(1, 1, 0, Blocks.SPRUCE_DOOR, Direction.EAST);
        b.bed(-2, 1, 1, Blocks.BED.green(), Direction.SOUTH);
        b.set(0, 1, 2, s(Blocks.CRAFTING_TABLE));
        b.set(0, 1, -1, barrel(Direction.UP));
        b.set(-2, 1, -1, barrel(Direction.UP));
        b.set(-2, 2, -1, s(Blocks.POTTED_FERN));
        b.fill(-2, 4, 0, 1, 4, 0, beam(Direction.Axis.X));
        b.hangingLantern(-1, 3, 0, Blocks.LANTERN);
        // Schuppen: Säge, Hackklotz, Truhe, Stämme an der Rückwand
        b.set(3, 1, 0, facing(Blocks.STONECUTTER, Direction.EAST));
        b.set(4, 1, -1, PlanBuilder.log(Blocks.STRIPPED_OAK_LOG, Direction.Axis.Y));
        b.set(4, 1, 1, s(Blocks.GRINDSTONE));
        b.chestAt(2, 1, -1, Direction.EAST);
        for (int x = 2; x <= 4; x++) {
            b.set(x, 1, 2, PlanBuilder.log(Blocks.OAK_LOG, Direction.Axis.X));
            b.set(x, 2, 2, PlanBuilder.log(x == 3 ? Blocks.BIRCH_LOG : Blocks.SPRUCE_LOG, Direction.Axis.X));
        }
        b.fill(2, 4, 0, 4, 4, 0, beam(Direction.Axis.X));
        b.hangingLantern(3, 3, 0, Blocks.LANTERN);
        for (int x = 2; x <= 4; x++) {
            b.set(x, 0, -3, PlanBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.BOTTOM));
        }
        // draußen: Stammholz rechts unter dem Dach, Setzlinge hinten
        for (int z = -1; z <= 2; z++) {
            b.set(6, 0, z, PlanBuilder.log(Blocks.OAK_LOG, Direction.Axis.Z));
        }
        b.set(6, 1, 0, PlanBuilder.log(Blocks.OAK_LOG, Direction.Axis.Z));
        b.set(6, 1, 1, PlanBuilder.log(Blocks.OAK_LOG, Direction.Axis.Z));
        Blueprints.grove(b, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, Blocks.OAK_SAPLING);
        step(b, -1, -3, Direction.SOUTH, Blocks.SPRUCE_STAIRS);
        b.workAt(3, 1, -1);
        b.entranceAt(-1, 0, -4);
        b.anchor("camera", 4, 1, -3).anchor("look", 2, 1, 2);
        b.anchor("camera2", 0, 1, -1).anchor("look2", -2, 1, 2);
    }

    // =====================================================================================
    // Lagerhaus: Feldsteinscheune
    // =====================================================================================

    /**
     * Feldsteinscheune: Mauern aus Feldstein mit Eckquadern, ein helles Ziegeldach mit Brettergiebeln, vorn ein breites
     * offenes Tor mit zwei Laternen. Innen links Doppeltruhen, rechts Fässer in zwei Reihen, hinten ein Heuboden auf
     * Stützen, zu dem eine Leiter führt; darunter weitere Truhen und Heu.
     */
    static void barn(PlanBuilder b) {
        BuildingSet roof = ModBlocks.LIGHT_ROOF_TILES;
        BlockState boards = s(Blocks.SPRUCE_PLANKS);
        b.foundation(-4, -5, 4, 5, fieldstone());
        b.fill(-4, 0, -5, 4, 0, 5, stone());
        b.fill(-3, 0, -4, 3, 0, 4, s(Blocks.SPRUCE_PLANKS));
        b.ring(-4, -5, 4, 5, 1, 4, fieldstone());
        for (int x : new int[] {-4, 4}) {
            for (int z : new int[] {-5, 5}) {
                b.fill(x, 1, z, x, 4, z, stone());
            }
        }
        b.air(-3, 1, -4, 3, 4, 4);
        b.roof(RoofShape.GABLE_Z, -4, -5, 4, 5, 5, roof, boards);
        b.roofs();
        // Tor: drei Blöcke breit, mit Balken darüber und zwei Laternen
        b.air(-1, 1, -5, 1, 3, -5);
        b.fill(-1, 4, -5, 1, 4, -5, beam(Direction.Axis.X));
        bracketLantern(b, -2, 3, -5, Direction.NORTH, Blocks.LANTERN);
        bracketLantern(b, 2, 3, -5, Direction.NORTH, Blocks.LANTERN);
        for (int x = -1; x <= 1; x++) {
            b.set(x, 0, -6, PlanBuilder.stairs(ModBlocks.MEDIEVAL_STONE.stairs().get(), Direction.SOUTH, Half.BOTTOM));
        }
        window(b, 0, 6, -5, 2, Direction.NORTH, Direction.EAST, ModBlocks.RED_SHUTTERS.get());
        window(b, 0, 6, 5, 2, Direction.SOUTH, Direction.EAST, ModBlocks.RED_SHUTTERS.get());
        for (int x : new int[] {-4, 4}) {
            b.set(x, 3, -2, pane());
            b.set(x, 3, 2, pane());
        }
        // Heuboden hinten auf drei Stützen, Leiter an der mittleren Stütze, Geländer an der Kante
        b.fill(-3, 5, 1, 3, 5, 4, boards);
        for (int x : new int[] {-2, 0, 2}) {
            b.fill(x, 1, 1, x, 4, 1, PlanBuilder.log(Blocks.SPRUCE_LOG, Direction.Axis.Y));
        }
        b.fill(0, 1, 0, 0, 5, 0, ladder(Direction.NORTH));
        for (int x = -3; x <= 3; x++) {
            if (x != 0) {
                b.set(x, 6, 1, s(Blocks.SPRUCE_FENCE));
            }
        }
        b.fill(-2, 6, 3, 2, 6, 4, s(Blocks.HAY_BLOCK));
        b.fill(-1, 7, 4, 1, 7, 4, s(Blocks.HAY_BLOCK));
        b.set(-2, 6, 2, s(Blocks.HAY_BLOCK));
        b.hangingLantern(0, 8, 2, Blocks.LANTERN);
        b.fill(-1, 9, 2, 1, 9, 2, beam(Direction.Axis.X));
        // Truhen links, Fässer rechts, unter dem Heuboden Heu und eine Doppeltruhe
        doubleChest(b, -3, 1, -3, Direction.EAST);
        doubleChest(b, -3, 1, -1, Direction.EAST);
        b.chest(-3, 1, -3);
        for (int z = -3; z <= 0; z++) {
            b.fill(3, 1, z, 3, 2, z, barrel(Direction.WEST));
        }
        b.fill(-3, 1, 3, -2, 1, 4, s(Blocks.HAY_BLOCK));
        b.set(-3, 2, 4, s(Blocks.HAY_BLOCK));
        doubleChest(b, 1, 1, 4, Direction.NORTH);
        b.set(3, 1, 4, barrel(Direction.UP));
        b.set(3, 1, 3, barrel(Direction.UP));
        b.set(3, 2, 4, barrel(Direction.UP));
        b.hangingLantern(0, 4, -2, Blocks.LANTERN);
        b.fill(-3, 5, -2, 3, 5, -2, beam(Direction.Axis.X));
        // vor dem Tor: Fässer, Heu und eine Tränke
        b.set(-3, 0, -6, barrel(Direction.UP));
        b.set(-3, 1, -6, barrel(Direction.UP));
        b.set(3, 0, -6, s(Blocks.HAY_BLOCK));
        b.set(-4, 0, -6, waterCauldron());
        b.workAt(0, 1, -2);
        b.entranceAt(0, 0, -7);
        b.anchor("camera", 2, 1, -4).anchor("look", -2, 2, 3);
    }

    // =====================================================================================
    // Brunnen: Ziehbrunnen auf einem gepflasterten Platz
    // =====================================================================================

    /**
     * Ziehbrunnen: ein gepflasterter Platz mit Steinbrüstung um den tiefen Schacht, darüber die Haspel mit Welle, Kurbel und
     * Kette unter einem kleinen Schindeldach. Dazu eine Tränke, eine Bank, Laternen und blühende Sträucher.
     */
    static void drawWell(PlanBuilder b) {
        b.foundation(-4, -4, 4, 4, stone());
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                b.set(x, -1, z, ((x * 7 + z * 3) & 3) == 0 ? s(Blocks.COBBLESTONE) : stone());
            }
        }
        // Schacht 3 × 3 mit Wasser, Brüstung aus Mauersteinen
        b.fill(-2, -7, -2, 2, -7, 2, stone());
        for (int y = -6; y <= -1; y++) {
            b.ring(-2, -2, 2, 2, y, y, stone());
            b.fill(-1, y, -1, 1, y, 1, s(Blocks.WATER));
        }
        b.ring(-2, -2, 2, 2, 0, 0, s(ModBlocks.MEDIEVAL_STONE.wall().get()));
        for (int x : new int[] {-2, 2}) {
            for (int z : new int[] {-2, 2}) {
                b.set(x, 1, z, s(x < 0 ? Blocks.POTTED_RED_TULIP : Blocks.POTTED_CORNFLOWER));
            }
        }
        // Haspel: zwei Pfosten, die Welle mit Kurbel, die Kette hängt in den Schacht; kleines Dach darüber
        for (int x : new int[] {-2, 2}) {
            b.fill(x, 1, 0, x, 6, 0, PlanBuilder.log(Blocks.SPRUCE_LOG, Direction.Axis.Y));
        }
        b.fill(-1, 3, 0, 1, 3, 0, PlanBuilder.log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.X));
        b.fill(0, 0, 0, 0, 2, 0, s(Blocks.IRON_CHAIN));
        b.set(3, 3, 0, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.WALL).setValue(LeverBlock.FACING, Direction.EAST));
        b.roof(RoofShape.GABLE_X, -2, -1, 2, 1, 5, ModBlocks.WOODEN_SHINGLES, null);
        b.roofs();
        // Tränke, Bank, Laternen, Sträucher
        b.set(-4, 0, -1, waterCauldron());
        b.set(-4, 0, 0, waterCauldron());
        b.set(-4, 0, 1, waterCauldron());
        for (int z = -1; z <= 1; z++) {
            b.set(4, 0, z, seat(ModBlocks.OAK_BENCH.get(), Direction.WEST));
        }
        lanternPost(b, -4, 0, -4, 2, Blocks.LANTERN);
        lanternPost(b, 4, 0, 4, 2, Blocks.LANTERN);
        b.set(4, -1, -4, s(Blocks.GRASS_BLOCK));
        b.set(4, 0, -4, s(Blocks.FLOWERING_AZALEA));
        b.set(-4, -1, 4, s(Blocks.GRASS_BLOCK));
        b.set(-4, 0, 4, s(Blocks.AZALEA));
        b.workAt(0, 0, -3);
        b.entranceAt(0, 0, -5);
    }

    // =====================================================================================
    // Schmiede: Steinschmiede mit Kammer
    // =====================================================================================

    /**
     * Steinschmiede (Schmied, 1 Bett): eine Halle aus dunklem Stein mit braunem Ziegeldach; vorn öffnet sich ein breiter
     * Bogen zur Gasse. Hinten die Esse mit Glut unter einer Ziegelhaube, deren Schornstein hoch übers Dach steigt, daneben
     * der Hochofen und das Löschwasser; in der Mitte der Amboss, vorn Schleifstein und Waffenständer. An der rechten Seite
     * liegt unter einem eigenen Dach die Kammer des Schmieds.
     */
    static void stoneForge(PlanBuilder b) {
        BuildingSet roof = ModBlocks.BROWN_ROOF_TILES;
        BlockState wall = s(ModBlocks.DARK_STONE_BRICKS.base().get());
        BlockState trim = stone();
        Block shutter = ModBlocks.RED_SHUTTERS.get();
        b.foundation(-4, -3, 4, 4, fieldstone());
        b.foundation(5, -1, 8, 3, fieldstone());
        b.fill(-4, 0, -3, 4, 0, 4, trim);
        b.fill(-3, 0, -2, 3, 0, 3, s(Blocks.COBBLESTONE));
        b.fill(4, 0, -1, 8, 0, 3, trim);
        b.fill(5, 0, 0, 7, 0, 2, planks());
        b.ring(-4, -3, 4, 4, 1, 4, wall);
        b.ring(4, -1, 8, 3, 1, 3, wall);
        for (int x : new int[] {-4, 4}) {
            for (int z : new int[] {-3, 4}) {
                b.fill(x, 1, z, x, 4, z, trim);
            }
        }
        b.air(-3, 1, -2, 3, 4, 3);
        b.air(5, 1, 0, 7, 3, 2);
        b.roof(RoofShape.GABLE_X, 4, -1, 8, 3, 4, roof, wall);
        b.roof(RoofShape.GABLE_Z, -4, -3, 4, 4, 5, roof, wall);
        int top = b.roofs();
        // breiter Bogen vorn mit Schlussstein, Stufen davor, zwei Laternen
        b.air(-2, 1, -3, 2, 3, -3);
        b.set(-2, 3, -3, PlanBuilder.stairs(ModBlocks.DARK_STONE_BRICKS.stairs().get(), Direction.WEST, Half.TOP));
        b.set(2, 3, -3, PlanBuilder.stairs(ModBlocks.DARK_STONE_BRICKS.stairs().get(), Direction.EAST, Half.TOP));
        b.set(0, 4, -3, s(Blocks.CHISELED_STONE_BRICKS));
        for (int x = -2; x <= 2; x++) {
            b.set(x, 0, -4, PlanBuilder.stairs(ModBlocks.DARK_STONE_BRICKS.stairs().get(), Direction.SOUTH, Half.BOTTOM));
        }
        bracketLantern(b, -3, 2, -3, Direction.NORTH, Blocks.LANTERN);
        bracketLantern(b, 3, 2, -3, Direction.NORTH, Blocks.LANTERN);
        b.fill(-3, 5, -3, 3, 5, -3, trim);
        b.set(0, 6, -3, s(Blocks.IRON_BARS));
        b.set(0, 7, -3, s(Blocks.IRON_BARS));
        b.set(-2, 6, -3, s(Blocks.IRON_BARS));
        b.set(2, 6, -3, s(Blocks.IRON_BARS));
        b.set(0, 8, -3, s(Blocks.CHISELED_STONE_BRICKS));
        // Esse mit Ziegelhaube und hohem Schornstein, Hochofen, Löschwasser
        b.set(-3, 1, 3, s(Blocks.BRICKS));
        b.set(-1, 1, 3, s(Blocks.BRICKS));
        b.set(-2, 1, 3, litCampfire().setValue(CampfireBlock.FACING, Direction.NORTH));
        b.fill(-3, 2, 3, -1, 4, 3, s(Blocks.BRICKS));
        chimney(b, -2, 4, 1, top + 2);
        b.set(-3, 1, 2, facing(Blocks.BLAST_FURNACE, Direction.EAST));
        b.set(-1, 1, 2, waterCauldron());
        // Amboss, Schleifstein, Schmiedetisch, Waffenständer, Truhe und Fässer
        b.set(0, 1, 0, Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, Direction.EAST));
        b.set(2, 1, -2, s(Blocks.GRINDSTONE));
        b.set(3, 1, -1, s(Blocks.SMITHING_TABLE));
        b.set(-3, 1, -2, PlanBuilder.slab(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
        b.set(-3, 2, -2, s(Blocks.LIGHTNING_ROD.weathering().unaffected()));
        b.set(-3, 1, -1, PlanBuilder.slab(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
        b.set(-3, 2, -1, s(Blocks.LIGHTNING_ROD.weathering().unaffected()));
        b.set(3, 1, 2, barrel(Direction.WEST));
        b.chestAt(3, 1, 3, Direction.WEST);
        b.fill(-3, 5, 0, 3, 5, 0, beam(Direction.Axis.X));
        b.hangingLantern(-2, 4, 0, Blocks.LANTERN);
        b.hangingLantern(2, 4, 0, Blocks.LANTERN);
        for (int z : new int[] {-1, 1}) {
            b.set(-4, 2, z, s(Blocks.IRON_BARS));
            b.set(-4, 3, z, s(Blocks.IRON_BARS));
        }
        b.set(1, 2, 4, s(Blocks.IRON_BARS));
        b.set(1, 3, 4, s(Blocks.IRON_BARS));
        // Kammer des Schmieds im Anbau
        b.door(4, 1, 1, Blocks.SPRUCE_DOOR, Direction.EAST);
        b.bed(5, 1, 2, Blocks.BED.brown(), Direction.EAST);
        b.set(7, 1, 2, facing(Blocks.CHEST, Direction.WEST));
        b.set(7, 1, 0, ModBlocks.OAK_TABLE.get());
        b.set(6, 1, 0, seat(ModBlocks.OAK_CHAIR.get(), Direction.EAST));
        b.set(7, 2, 0, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        b.fill(5, 4, 1, 7, 4, 1, beam(Direction.Axis.X));
        b.hangingLantern(6, 3, 1, Blocks.LANTERN);
        window(b, 8, 2, 1, 2, Direction.EAST, Direction.SOUTH, shutter);
        window(b, 6, 2, -1, 2, Direction.NORTH, Direction.EAST, shutter);
        b.anchor("anvil", 0, 1, 0);
        b.workAt(0, 1, -1);
        b.entranceAt(0, 0, -5);
        b.anchor("camera", 2, 1, -2).anchor("look", -2, 1, 3);
        b.anchor("camera2", 5, 1, 0).anchor("look2", 6, 1, 2);
    }

    // =====================================================================================
    // Mine: Förderhaus mit Haspel und Stollen
    // =====================================================================================

    /**
     * Mine mit Förderhaus (2 Bergleute): ein Fachwerkhaus auf Feldstein mit Schieferdach über dem Schacht; im Haus steht
     * das Fördergerüst aus vier Stämmen, an dessen Welle die Kette bis auf die Sohle hängt. Unten führt ein Stollen mit
     * Türstöcken, Laternen und Gleis zur Erzwand. Neben dem Haus eine Halde aus Gestein und eine Kiste mit Erz.
     */
    static void headframeMine(PlanBuilder b) {
        BuildingSet roof = ModBlocks.DARK_SLATE_SHINGLES;
        b.foundation(-4, -3, 4, 4, fieldstone());
        b.foundation(5, 0, 7, 3, fieldstone());
        b.foundation(-5, -1, -5, 0, fieldstone());
        b.fill(-4, 0, -3, 4, 0, 4, fieldstone());
        frameWallX(b, -4, 4, -3, 1, 4);
        frameWallX(b, -4, 4, 4, 1, 4);
        frameWallZ(b, -3, 4, -4, 1, 4);
        frameWallZ(b, -3, 4, 4, 1, 4);
        b.air(-3, 1, -2, 3, 4, 3);
        b.roof(RoofShape.GABLE_Z, -4, -3, 4, 4, 5, roof, s(Blocks.SPRUCE_PLANKS));
        b.roofs();
        // breiter Eingang
        b.air(-1, 1, -3, 1, 3, -3);
        for (int x = -1; x <= 1; x++) {
            b.set(x, 0, -4, PlanBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.BOTTOM));
        }
        bracketLantern(b, -2, 2, -3, Direction.NORTH, Blocks.LANTERN);
        window(b, -4, 2, 1, 2, Direction.WEST, Direction.SOUTH, ModBlocks.GREEN_SHUTTERS.get());
        window(b, 4, 2, 1, 2, Direction.EAST, Direction.SOUTH, ModBlocks.GREEN_SHUTTERS.get());
        int bottom = -11;
        // Stollen unten (rundum Stein, damit er in jedem Gelände dicht ist) mit Türstöcken, Gleis und Erzwand
        b.fill(-2, bottom - 1, -8, 3, bottom + 4, 2, s(Blocks.STONE));
        b.air(0, bottom, -6, 1, bottom + 2, 1);
        b.fill(0, bottom - 1, -6, 1, bottom - 1, 1, s(Blocks.COBBLESTONE));
        for (int z : new int[] {-1, -4}) {
            b.fill(-1, bottom, z, -1, bottom + 2, z, PlanBuilder.log(Blocks.SPRUCE_LOG, Direction.Axis.Y));
            b.fill(2, bottom, z, 2, bottom + 2, z, PlanBuilder.log(Blocks.SPRUCE_LOG, Direction.Axis.Y));
            b.fill(-1, bottom + 3, z, 2, bottom + 3, z, PlanBuilder.log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.X));
            b.hangingLantern(0, bottom + 2, z, Blocks.LANTERN);
        }
        for (int z = -5; z <= -2; z++) {
            b.set(1, bottom, z, Blocks.RAIL.defaultBlockState());
        }
        b.set(0, bottom, -7, s(Blocks.IRON_ORE));
        b.set(1, bottom + 1, -7, s(Blocks.COAL_ORE));
        b.set(0, bottom + 2, -7, s(Blocks.COPPER_ORE));
        b.set(1, bottom, -7, s(Blocks.COAL_ORE));
        b.mark(0, bottom, -6);
        b.workAt(1, bottom, -6);
        // Schacht 2 × 2 vom Haus bis in den Stollen, die Leiter an der Rückwand
        BlockState ladder = ladder(Direction.NORTH);
        for (int y = bottom + 3; y <= 0; y++) {
            b.set(0, y, 2, stone());
            b.set(1, y, 2, stone());
            b.set(-1, y, 0, stone());
            b.set(-1, y, 1, stone());
            b.set(2, y, 0, stone());
            b.set(2, y, 1, stone());
            b.set(0, y, -1, stone());
            b.set(1, y, -1, stone());
            b.set(0, y, 0, s(Blocks.AIR));
            b.set(1, y, 0, s(Blocks.AIR));
            b.set(1, y, 1, s(Blocks.AIR));
        }
        for (int y = bottom; y <= 0; y++) {
            b.set(0, y, 1, ladder);
        }
        // Fördergerüst: vier Stämme um den Schacht, oben ein Rahmen mit der Welle; die Kette hängt bis auf die Sohle
        for (int x : new int[] {-1, 2}) {
            for (int z : new int[] {-1, 2}) {
                b.fill(x, 1, z, x, 5, z, PlanBuilder.log(Blocks.SPRUCE_LOG, Direction.Axis.Y));
            }
        }
        b.fill(-1, 6, -1, -1, 6, 2, beam(Direction.Axis.Z));
        b.fill(2, 6, -1, 2, 6, 2, beam(Direction.Axis.Z));
        // das Fördergerüst steigt als Turm durch das Dach: oben die Seilscheibe unter einem eigenen Dach
        for (int x : new int[] {-1, 2}) {
            for (int z : new int[] {-1, 2}) {
                b.fill(x, 7, z, x, 14, z, PlanBuilder.log(Blocks.SPRUCE_LOG, Direction.Axis.Y));
            }
        }
        for (int y = 7; y <= 14; y++) {
            for (int x = 0; x <= 1; x++) {
                for (int z = 0; z <= 1; z++) {
                    b.set(x, y, z, s(Blocks.AIR));
                }
            }
        }
        for (int x = 0; x <= 1; x++) {
            b.set(x, 11, -1, s(Blocks.SPRUCE_FENCE));
            b.set(x, 11, 2, s(Blocks.SPRUCE_FENCE));
        }
        for (int z = 0; z <= 1; z++) {
            b.set(-1, 11, z, s(Blocks.SPRUCE_FENCE));
            b.set(2, 11, z, s(Blocks.SPRUCE_FENCE));
        }
        b.fill(-1, 14, -1, 2, 14, -1, beam(Direction.Axis.X));
        b.fill(-1, 14, 2, 2, 14, 2, beam(Direction.Axis.X));
        b.fill(-1, 13, 0, -1, 13, 1, beam(Direction.Axis.Z));
        b.fill(2, 13, 0, 2, 13, 1, beam(Direction.Axis.Z));
        b.fill(0, 13, 0, 1, 13, 0, PlanBuilder.log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.X));
        b.roof(RoofShape.HIP, 0, 0, 1, 1, 15, ModBlocks.WOODEN_SHINGLES, s(Blocks.SPRUCE_PLANKS));
        b.roofs();
        b.fill(1, bottom + 3, 0, 1, 12, 0, s(Blocks.IRON_CHAIN));
        b.set(1, bottom + 2, 0, barrel(Direction.UP));
        for (int[] f : new int[][] {{-1, 0}, {-1, 1}, {2, 0}, {2, 1}, {0, -1}, {1, -1}}) {
            b.set(f[0], 1, f[1], s(Blocks.SPRUCE_FENCE));
        }
        b.hangingLantern(-1, 5, 0, Blocks.LANTERN);
        b.fill(-3, 6, 3, 3, 6, 3, beam(Direction.Axis.X));
        b.hangingLantern(0, 5, 3, Blocks.LANTERN);
        // im Haus: Truhe, Fässer, Werkbank, Spitzhacken-Ständer
        b.chestAt(-3, 1, 3, Direction.EAST);
        b.set(-3, 1, -2, barrel(Direction.UP));
        b.set(3, 1, 3, barrel(Direction.UP));
        b.set(3, 2, 3, barrel(Direction.UP));
        b.set(3, 1, -2, s(Blocks.CRAFTING_TABLE));
        // draußen: Halde aus Gestein, eine Kiste mit Erz
        for (int x = 5; x <= 7; x++) {
            for (int z = 0; z <= 3; z++) {
                b.set(x, 0, z, s(((x + z) & 1) == 0 ? Blocks.COBBLESTONE : Blocks.ANDESITE));
            }
        }
        b.fill(5, 1, 1, 6, 1, 2, s(Blocks.COBBLESTONE));
        b.set(6, 1, 1, s(Blocks.COARSE_DIRT));
        b.set(5, 2, 2, s(Blocks.ANDESITE));
        b.set(-5, 0, -1, s(Blocks.COAL_ORE));
        b.set(-5, 0, 0, s(Blocks.IRON_ORE));
        b.set(-5, 1, 0, s(Blocks.COAL_ORE));
        b.anchor("shaft_top", 0, 1, 1).anchor("shaft_bottom", 0, bottom, 1).anchor("shaft_entry", 0, 1, 2);
        b.entranceAt(0, 0, -5);
        b.anchor("camera", 2, 1, -2).anchor("look", 0, 4, 1);
        b.anchor("camera2", 0, bottom, 1).anchor("look2", 0, bottom + 1, -7);
    }

    // =====================================================================================
    // Markt: Markthalle
    // =====================================================================================

    /**
     * Markthalle (2 Händler): ein offenes Walmdach aus roten Ziegeln auf acht Steinsäulen und Balken über einem
     * gepflasterten Platz. Unter dem Dach zwei Stände mit Theken und Waren, in der Mitte das Marktkreuz mit Laterne, vorn
     * die Marktglocke. Die Truhe steht hinter dem linken Stand.
     */
    static void marketHall(PlanBuilder b) {
        BuildingSet roof = ModBlocks.RED_ROOF_TILES;
        b.foundation(-6, -4, 6, 4, stone());
        for (int x = -6; x <= 6; x++) {
            for (int z = -4; z <= 4; z++) {
                b.set(x, -1, z, ((x + z) & 1) == 0 ? s(Blocks.STONE_BRICKS) : stone());
            }
        }
        for (int x : new int[] {-5, -2, 2, 5}) {
            for (int z : new int[] {-3, 3}) {
                b.fill(x, 0, z, x, 2, z, s(ModBlocks.STONE_PILLAR.get()));
                b.set(x, 3, z, s(Blocks.CHISELED_STONE_BRICKS));
            }
        }
        for (int z = -3; z <= 3; z += 3) {
            if (z != 0) {
                b.fill(-5, 4, z, 5, 4, z, beam(Direction.Axis.X));
            }
        }
        b.fill(-5, 4, -2, -5, 4, 2, beam(Direction.Axis.Z));
        b.fill(5, 4, -2, 5, 4, 2, beam(Direction.Axis.Z));
        b.roof(RoofShape.HIP, -5, -3, 5, 3, 5, roof, s(Blocks.SPRUCE_PLANKS));
        b.roofs();
        b.fill(-4, 5, 0, 4, 5, 0, beam(Direction.Axis.X));
        // Marktkreuz mit Laterne, Glocke vorn
        b.fill(0, 0, 0, 0, 2, 0, s(ModBlocks.MEDIEVAL_STONE.wall().get()));
        b.standingLantern(0, 3, 0, Blocks.LANTERN);
        b.set(0, 3, -3, Blocks.BELL.defaultBlockState().setValue(net.minecraft.world.level.block.BellBlock.ATTACHMENT, BellAttachType.CEILING)
                .setValue(net.minecraft.world.level.block.BellBlock.FACING, Direction.NORTH));
        // zwei Stände: Theke vorn, Händler dahinter, Waren an der Rückseite
        stallCounter(b, -3, Blocks.WOOL.red());
        stallCounter(b, 3, Blocks.WOOL.blue());
        b.set(-4, 0, 2, s(Blocks.MELON));
        b.set(-3, 0, 2, s(Blocks.PUMPKIN));
        b.chestAt(-2, 0, 2, Direction.NORTH);
        b.set(2, 0, 2, barrel(Direction.UP));
        b.set(2, 1, 2, s(Blocks.POTTED_POPPY));
        b.set(3, 0, 2, s(Blocks.WOOL.white()));
        b.set(4, 0, 2, s(Blocks.HAY_BLOCK));
        b.set(4, 1, 2, s(Blocks.WOOL.white()));
        b.hangingLantern(-3, 4, 0, Blocks.LANTERN);
        b.hangingLantern(3, 4, 0, Blocks.LANTERN);
        for (int x : new int[] {-6, 6}) {
            b.set(x, -1, -4, s(Blocks.GRASS_BLOCK));
            b.set(x, 0, -4, s(Blocks.FLOWERING_AZALEA));
        }
        b.workAt(-3, 0, 1);
        b.mark(3, 0, 1);
        b.entranceAt(0, 0, -5);
        b.anchor("camera", 1, 0, -2).anchor("look", -4, 1, 2);
    }

    /** Eine Theke (Tisch zwischen zwei Fässern) mit einem Ballen Stoff darauf. */
    private static void stallCounter(PlanBuilder b, int cx, Block wool) {
        b.set(cx - 1, 0, 0, barrel(Direction.NORTH));
        b.set(cx, 0, 0, ModBlocks.OAK_TABLE.get());
        b.set(cx + 1, 0, 0, barrel(Direction.NORTH));
        b.set(cx - 1, 1, 0, s(wool));
        b.set(cx + 1, 1, 0, s(Blocks.POTTED_DANDELION));
    }

    // =====================================================================================
    // Kaserne: Wachhaus mit Turm
    // =====================================================================================

    /**
     * Wachhaus mit Turm (acht Soldaten mehr): ein Wachhaus aus dunklem Stein mit Schieferdach, an der vorderen rechten Ecke
     * ein zwölf Blöcke hoher Wachturm mit Schießscharten, innen eine Leiter zur Plattform mit Zinnen und Fahne. Im Wachhaus
     * Pritschen, Tisch mit Bänken, Waffenständer und Truhe; davor ein Übungsplatz mit Strohpuppen und einer Zielscheibe.
     */
    static void guardTower(PlanBuilder b) {
        BuildingSet roof = ModBlocks.DARK_SLATE_SHINGLES;
        BlockState wall = s(ModBlocks.DARK_STONE_BRICKS.base().get());
        BlockState trim = stone();
        Block battlement = ModBlocks.DARK_STONE_BRICK_BATTLEMENT.get();
        b.foundation(-4, -4, 6, 3, trim);
        b.foundation(-4, -8, 5, -5, trim);
        b.fill(-4, 0, -3, 3, 0, 3, trim);
        b.fill(-3, 0, -2, 2, 0, 2, s(Blocks.SPRUCE_PLANKS));
        b.ring(-4, -3, 3, 3, 1, 4, wall);
        b.air(-3, 1, -2, 2, 4, 2);
        b.roof(RoofShape.GABLE_X, -4, -3, 3, 3, 5, roof, wall);
        b.roofs();
        // Wachturm (x 3..6, z −4..−1): Mauern bis y = 11, Plattform, Zinnen, Leiter innen
        b.fill(3, 0, -4, 6, 0, -1, trim);
        for (int y = 1; y <= 11; y++) {
            b.ring(3, -4, 6, -1, y, y, wall);
        }
        for (int x : new int[] {3, 6}) {
            for (int z : new int[] {-4, -1}) {
                b.fill(x, 1, z, x, 11, z, trim);
            }
        }
        b.air(4, 1, -3, 5, 15, -2);
        b.fill(3, 12, -4, 6, 12, -1, trim);
        b.fill(5, 1, -2, 5, 12, -2, ladder(Direction.WEST));
        for (int x = 3; x <= 6; x++) {
            b.set(x, 13, -4, facing(battlement, Direction.NORTH));
            b.set(x, 13, -1, facing(battlement, Direction.SOUTH));
        }
        for (int z = -3; z <= -2; z++) {
            b.set(3, 13, z, facing(battlement, Direction.WEST));
            b.set(6, 13, z, facing(battlement, Direction.EAST));
        }
        b.fill(4, 13, -3, 4, 14, -3, s(Blocks.SPRUCE_FENCE));
        b.set(4, 15, -3, Blocks.BANNER.red().defaultBlockState());
        for (int[] slit : new int[][] {{6, 5, -3}, {6, 6, -3}, {4, 8, -4}, {4, 9, -4}, {3, 9, -3}}) {
            b.set(slit[0], slit[1], slit[2], s(Blocks.IRON_BARS));
        }
        b.door(3, 1, -2, Blocks.SPRUCE_DOOR, Direction.EAST);
        b.hangingLantern(4, 11, -2, ModBlocks.STEEL_LANTERN.get());
        // Wachhaus: Tor mit Laternen, Schießscharten, Banner
        b.door(-1, 1, -3, ModBlocks.CASTLE_DOOR.get(), Direction.SOUTH);
        bracketLantern(b, -2, 2, -3, Direction.NORTH, ModBlocks.STEEL_LANTERN.get());
        bracketLantern(b, 0, 2, -3, Direction.NORTH, ModBlocks.STEEL_LANTERN.get());
        for (int[] slit : new int[][] {{-3, -3}, {1, -3}, {-4, 0}, {-1, 3}, {1, 3}}) {
            b.set(slit[0], 2, slit[1], s(Blocks.IRON_BARS));
            b.set(slit[0], 3, slit[1], s(Blocks.IRON_BARS));
        }
        b.set(-1, 5, -4, wallBanner(Blocks.WALL_BANNER.red(), Direction.NORTH));
        // innen: zwei Pritschen, Tisch mit Bänken, Waffenständer, Truhe, Banner, Laternen
        for (int x : new int[] {-3, -2}) {
            BlockState bed = Blocks.BED.red().defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH);
            b.set(x, 1, 1, bed.setValue(BedBlock.PART, BedPart.FOOT));
            b.set(x, 1, 2, bed.setValue(BedBlock.PART, BedPart.HEAD));
        }
        b.set(-3, 1, -2, barrel(Direction.UP));
        b.set(0, 1, 0, ModBlocks.OAK_TABLE.get());
        b.set(1, 1, 0, ModBlocks.OAK_TABLE.get());
        b.set(0, 1, -1, seat(ModBlocks.OAK_BENCH.get(), Direction.SOUTH));
        b.set(1, 1, -1, seat(ModBlocks.OAK_BENCH.get(), Direction.SOUTH));
        b.set(0, 1, 1, seat(ModBlocks.OAK_BENCH.get(), Direction.NORTH));
        b.set(1, 1, 1, seat(ModBlocks.OAK_BENCH.get(), Direction.NORTH));
        b.set(0, 2, 0, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        for (int x = -1; x <= 0; x++) {
            b.set(x, 1, 2, PlanBuilder.slab(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
            b.set(x, 2, 2, s(Blocks.LIGHTNING_ROD.weathering().unaffected()));
        }
        b.chestAt(2, 1, 2, Direction.WEST);
        b.set(-3, 3, -1, wallBanner(Blocks.WALL_BANNER.red(), Direction.EAST));
        b.fill(-3, 5, 0, 2, 5, 0, beam(Direction.Axis.X));
        b.hangingLantern(-2, 4, 0, ModBlocks.STEEL_LANTERN.get());
        b.hangingLantern(1, 4, 0, ModBlocks.STEEL_LANTERN.get());
        // Übungsplatz aus gestampfter Erde: zwei Strohpuppen, eine Zielscheibe
        for (int x = -4; x <= 5; x++) {
            for (int z = -8; z <= -5; z++) {
                b.set(x, -1, z, s(Blocks.COARSE_DIRT));
            }
        }
        dummy(b, -3, 0, -7, Direction.SOUTH);
        dummy(b, 1, 0, -7, Direction.SOUTH);
        b.set(4, 0, -7, s(Blocks.SPRUCE_FENCE));
        b.set(4, 1, -7, s(Blocks.TARGET));
        step(b, -1, -4, Direction.SOUTH, ModBlocks.DARK_STONE_BRICKS.stairs().get());
        b.workAt(-1, 1, -1);
        b.entranceAt(-1, 0, -5);
        b.anchor("camera", 1, 1, -2).anchor("look", -3, 1, 2);
        b.anchor("camera2", 4, 13, -2).anchor("look2", 0, 10, -16);
    }

    // =====================================================================================
    // Kartenhaus: Gelehrtenhaus mit Aussichtsturm
    // =====================================================================================

    /**
     * Gelehrtenhaus mit Aussichtsturm (Kartograf, 1 Bett): ein Haus aus hellem Kalkstein mit Fachwerkgiebeln und hellem
     * Ziegeldach; innen Kartentisch, Lesepult, Bücherwand und die Schlafecke. Daneben steht ein schlanker Turm, in dem eine
     * Leiter zur offenen Aussichtsstube unter einem kupfernen Zeltdach führt – dort steht das Fernrohr.
     */
    static void observatory(PlanBuilder b) {
        BuildingSet roof = ModBlocks.LIGHT_ROOF_TILES;
        BlockState wall = s(ModBlocks.LIMESTONE_BRICKS.base().get());
        BlockState trim = stone();
        Block shutter = ModBlocks.GREEN_SHUTTERS.get();
        b.foundation(-4, -2, 2, 4, trim);
        b.foundation(3, -3, 6, 0, trim);
        b.fill(-4, 0, -2, 2, 0, 4, trim);
        b.fill(-3, 0, -1, 1, 0, 3, s(Blocks.DARK_OAK_PLANKS));
        b.ring(-4, -2, 2, 4, 1, 4, wall);
        for (int x : new int[] {-4, 2}) {
            for (int z : new int[] {-2, 4}) {
                b.fill(x, 1, z, x, 4, z, trim);
            }
        }
        b.air(-3, 1, -1, 1, 4, 3);
        b.roof(RoofShape.GABLE_Z, -4, -2, 2, 4, 5, roof, Blueprints.frame());
        int top = b.roofs();
        // Turm: Mauern bis y = 9, Boden der Aussichtsstube bei y = 10, vier Eckpfosten, kupfernes Zeltdach
        b.fill(3, 0, -3, 6, 0, 0, trim);
        for (int y = 1; y <= 9; y++) {
            b.ring(3, -3, 6, 0, y, y, wall);
        }
        b.air(4, 1, -2, 5, 12, -1);
        b.fill(3, 10, -3, 6, 10, 0, s(Blocks.DARK_OAK_PLANKS));
        for (int x : new int[] {3, 6}) {
            for (int z : new int[] {-3, 0}) {
                b.fill(x, 1, z, x, 12, z, trim);
            }
        }
        b.fill(5, 1, -1, 5, 10, -1, ladder(Direction.NORTH));
        for (int x = 4; x <= 5; x++) {
            b.set(x, 11, -3, s(Blocks.DARK_OAK_FENCE));
            b.set(x, 11, 0, s(Blocks.DARK_OAK_FENCE));
        }
        for (int z = -2; z <= -1; z++) {
            b.set(3, 11, z, s(Blocks.DARK_OAK_FENCE));
            b.set(6, 11, z, s(Blocks.DARK_OAK_FENCE));
        }
        b.roof(RoofShape.HIP, 3, -3, 6, 0, 13, ModBlocks.COPPER_ROOF, trim);
        int spire = b.roofs();
        b.set(4, spire + 1, -2, s(Blocks.LIGHTNING_ROD.waxed().oxidized()));
        // Fernrohr auf einem Pfosten, ein Lesepult in der Aussichtsstube
        b.set(4, 11, -2, s(Blocks.DARK_OAK_FENCE));
        b.set(4, 12, -2, Blocks.LIGHTNING_ROD.waxed().unaffected().defaultBlockState()
                .setValue(net.minecraft.world.level.block.LightningRodBlock.FACING, Direction.NORTH));
        // Fenster am Turm, Tür vom Haus hinüber
        for (int y : new int[] {3, 6}) {
            b.set(4, y, -3, pane());
            b.set(6, y, -2, pane());
        }
        b.door(2, 1, -1, Blocks.DARK_OAK_DOOR, Direction.EAST);
        b.air(3, 1, -1, 3, 2, -1);
        // Haus: Tür, Fenster, Giebelfenster
        b.door(-1, 1, -2, Blocks.DARK_OAK_DOOR, Direction.SOUTH);
        window(b, -3, 2, -2, 2, Direction.NORTH, Direction.EAST, shutter);
        window(b, 1, 2, -2, 2, Direction.NORTH, Direction.EAST, shutter);
        window(b, -4, 2, 1, 2, Direction.WEST, Direction.SOUTH, shutter);
        window(b, 2, 2, 2, 2, Direction.EAST, Direction.SOUTH, shutter);
        window(b, -1, 2, 4, 2, Direction.SOUTH, Direction.EAST, shutter);
        b.set(-1, 6, -2, pane());
        b.set(-1, 7, -2, pane());
        b.set(-1, 6, 4, pane());
        // Kartentisch, Lesepult, Bücherwand, Truhe, Schlafecke
        b.set(-1, 1, 1, s(Blocks.CARTOGRAPHY_TABLE));
        b.set(0, 1, 1, facing(Blocks.LECTERN, Direction.WEST));
        for (int x = -3; x <= 1; x++) {
            if (x != -1) {
                b.fill(x, 1, 3, x, 2, 3, s(x == 0 ? Blocks.CHISELED_BOOKSHELF : Blocks.BOOKSHELF));
            }
        }
        b.bed(-3, 1, 1, Blocks.BED.cyan(), Direction.NORTH);
        b.chestAt(-3, 1, -1, Direction.EAST);
        b.set(1, 1, 0, ModBlocks.OAK_TABLE.get());
        b.set(1, 2, 0, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        b.set(0, 1, 0, seat(ModBlocks.OAK_CHAIR.get(), Direction.EAST));
        b.set(-1, 1, 3, s(Blocks.POTTED_FERN));
        b.fill(-3, 5, 1, 1, 5, 1, beam(Direction.Axis.X));
        b.hangingLantern(-1, 4, 1, Blocks.LANTERN);
        step(b, -1, -3, Direction.SOUTH, ModBlocks.LIMESTONE_BRICKS.stairs().get());
        b.workAt(-1, 1, 0);
        b.entranceAt(-1, 0, -4);
        b.anchor("camera", 1, 1, -1).anchor("look", -2, 1, 3);
        b.anchor("camera2", 5, 11, -1).anchor("look2", 2, 11, -9);
    }
}
