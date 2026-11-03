package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import static io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuildings.at;
import static io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuildings.barrel;
import static io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuildings.chest;
import static io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuildings.facing;
import static io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuildings.furnace;

import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/** Die Werkstätten der Dörfer: Schmiede, Bäckerei, Gasthaus, Schreibstube, Kartenhaus, Kräuterhaus, Metzgerei, Gerberei, Steinmetz, Bogner. */
final class VillageWorkshops {
    private VillageWorkshops() {}

    // =====================================================================================
    // Schmiede (11 × 9)
    // =====================================================================================

    /**
     * Schmiede: vorn eine offene Werkstatt unter einem Vordach – Esse mit glühender Glut und Rauchfang, Amboss,
     * Schmiedetisch und Schleifstein –, dahinter das Haus der Schmiede mit zwei Betten und der Werkzeugtruhe.
     */
    static void smithy(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        VillageBuildings.shell(a, 11, 9, 14);
        a.walls(0, 3, 10, 8, 1, 4);
        int top = VillageBuildings.roof(a, 0, 3, 10, 8, 4, false);
        b.air(1, 1, 4, 9, 3, 7);
        // Vordach auf Pfosten über der Werkstatt
        for (int x : new int[] {0, 3, 6}) {
            for (int y = 1; y <= 3; y++) {
                b.set(x, y, 0, a.p.post(Direction.Axis.Y));
            }
        }
        for (int x = 0; x <= 6; x++) {
            for (int z = 0; z <= 1; z++) {
                b.set(x, 4, z, a.style == VillageStyle.CLAY ? a.p.stone() : a.roofSlab(SlabType.BOTTOM));
            }
            b.set(x, 3, 0, a.p.beam(Direction.Axis.X));
        }
        if (a.style == VillageStyle.CLAY) {
            for (int x = 0; x <= 6; x++) {
                b.set(x, 4, 2, a.p.stone());
            }
        }
        // Esse: Glut im Kessel, Steinmantel, Rauchfang bis über das Dach
        BlockState brick = a.style == VillageStyle.NORDIC ? a.p.stone() : Blocks.BRICKS.defaultBlockState();
        b.set(0, 1, 2, brick);
        b.set(2, 1, 2, brick);
        b.set(1, 1, 2, Blocks.LAVA_CAULDRON.defaultBlockState());
        b.set(0, 2, 2, brick);
        b.set(2, 2, 2, brick);
        b.set(1, 2, 2, Blocks.IRON_BARS.defaultBlockState());
        b.fill(0, 3, 2, 2, 3, 2, brick);
        a.chimney(1, 2, 4, top);
        b.set(0, 1, 1, furnace(Blocks.BLAST_FURNACE, Direction.EAST));
        // Amboss, Schmiedetisch, Schleifstein, Wasserbottich
        b.set(3, 1, 1, Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, Direction.EAST));
        b.set(4, 1, 2, Blocks.SMITHING_TABLE.defaultBlockState());
        b.set(5, 1, 2, Blocks.GRINDSTONE.defaultBlockState().setValue(GrindstoneBlock.FACE, AttachFace.FLOOR)
                .setValue(GrindstoneBlock.FACING, Direction.EAST));
        b.set(6, 1, 2, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        // Haus: Tür hinter der Werkstatt, Fenster, Betten, Truhe
        a.door(8, 3, Direction.SOUTH);
        for (int x = 7; x <= 9; x++) {
            for (int z = 0; z <= 2; z++) {
                b.set(x, 0, z, a.style == VillageStyle.STILT ? a.p.planks() : Blocks.DIRT_PATH.defaultBlockState());
            }
        }
        if (a.style == VillageStyle.STILT) {
            for (int x = 7; x <= 9; x++) {
                for (int z = 0; z <= 2; z++) {
                    b.set(x, 0, z, a.p.planks());
                }
            }
        }
        VillageBuildings.entrance(a, piece, 7, 9);
        VillageBuildings.entrance(a, piece, 1, 5);
        a.window(4, 2, 3, Direction.NORTH, 2);
        a.window(0, 2, 6, Direction.WEST, 2);
        a.window(10, 2, 6, Direction.EAST, 2);
        a.window(3, 2, 8, Direction.SOUTH, 2);
        a.window(7, 2, 8, Direction.SOUTH, 2);
        b.bed(1, 1, 6, a.bed(false), Direction.SOUTH);
        b.bed(3, 1, 6, a.bed(false), Direction.SOUTH);
        chest(a, 2, 1, 7, Direction.NORTH, ModLootTables.VILLAGE_SMITHY);
        b.set(9, 1, 7, barrel(Direction.UP));
        // Tisch und Stuhl neben (nicht vor) der Tür
        b.set(9, 1, 5, a.table());
        b.set(8, 1, 5, a.chair(Direction.EAST));
        b.set(9, 2, 5, Blocks.LANTERN.defaultBlockState());
        a.hangingLantern(5, 4, 5);
        b.set(5, 1, 7, Blocks.CRAFTING_TABLE.defaultBlockState());
        VillageProfession[] smiths = {VillageProfession.ARMORER, VillageProfession.WEAPONSMITH, VillageProfession.TOOLSMITH};
        int first = b.random.nextInt(3);
        piece.resident(b, 4, 1, 1, smiths[first], at(1, 1, 7), at(4, 1, 1));
        piece.resident(b, 5, 1, 1, smiths[(first + 1 + b.random.nextInt(2)) % 3], at(3, 1, 7), at(5, 1, 1));
    }

    // =====================================================================================
    // Bäckerei (9 × 9)
    // =====================================================================================

    /**
     * Bäckerei: hinten der gemauerte Backofen mit zwei Öfen und einem mächtigen Kamin, vorn die Theke mit Kuchen und
     * Brot, Mehlsäcke und Getreide; in der Ecke das Bett des Bäckers.
     */
    static void bakery(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        VillageBuildings.shell(a, 9, 9, 15);
        a.walls(0, 0, 8, 8, 1, 4);
        VillageBuildings.frontDoor(a, piece, 4, a.p.door());
        VillageBuildings.frontWindow(a, 2, 2, 2);
        VillageBuildings.frontWindow(a, 6, 2, 2);
        VillageBuildings.sideWindow(a, 0, 2, 4, 2, true);
        VillageBuildings.sideWindow(a, 8, 2, 3, 2, false);
        int top = VillageBuildings.roof(a, 0, 0, 8, 8, 4, true);
        b.air(1, 1, 1, 7, 3, 7);
        // Backofen
        for (int x = 2; x <= 6; x++) {
            for (int y = 1; y <= 3; y++) {
                b.set(x, y, 7, Blocks.BRICKS.defaultBlockState());
            }
        }
        b.set(3, 1, 7, furnace(Blocks.SMOKER, Direction.NORTH));
        b.set(5, 1, 7, furnace(Blocks.FURNACE, Direction.NORTH));
        b.set(4, 1, 7, Blocks.CAMPFIRE.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true)
                .setValue(net.minecraft.world.level.block.CampfireBlock.SIGNAL_FIRE, false));
        b.set(4, 2, 7, Blocks.BRICKS.defaultBlockState());
        b.fill(3, 4, 7, 5, 4, 7, Blocks.BRICKS.defaultBlockState());
        a.chimney(4, 7, 5, top);
        // Theke vorn
        for (int x = 1; x <= 3; x++) {
            b.set(x, 1, 3, Arch.slab(a.p.trim.slab(), SlabType.TOP));
        }
        b.set(1, 2, 3, Blocks.CAKE.defaultBlockState());
        b.set(3, 2, 3, Blocks.HAY_BLOCK.defaultBlockState());
        // Mehl und Korn
        b.set(7, 1, 7, Blocks.WOOL.white().defaultBlockState());
        b.set(7, 1, 6, Blocks.WOOL.white().defaultBlockState());
        b.set(7, 2, 7, Blocks.HAY_BLOCK.defaultBlockState());
        b.set(1, 1, 7, barrel(Direction.UP));
        b.set(1, 2, 7, barrel(Direction.UP));
        b.set(1, 1, 6, Blocks.COMPOSTER.defaultBlockState());
        chest(a, 7, 1, 1, Direction.WEST, ModLootTables.VILLAGE_BAKERY);
        b.bed(7, 1, 3, a.bed(false), Direction.SOUTH);
        a.hangingLantern(4, a.style == VillageStyle.CLAY ? 4 : 5, 4);
        if (a.style != VillageStyle.CLAY) {
            VillageBuildings.tieBeam(a, 1, 7, 4, 5);
        }
        piece.resident(b, 4, 1, 5, VillageProfession.BAKER, at(7, 1, 4), at(4, 1, 6));
    }

    // =====================================================================================
    // Gasthaus (13 × 11)
    // =====================================================================================

    /**
     * Gasthaus: im Erdgeschoss die Wirtsstube mit langen Tafeln, Kronleuchter, Kamin und dem Schanktisch vor den
     * Fässern; oben die Kammern für Gäste. Über der Tür ein Wirtshausschild.
     */
    static void tavern(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        boolean jetty = a.style == VillageStyle.TIMBER;
        VillageBuildings.shell(a, 13, 11, 19);
        int fz = jetty ? 1 : 0;
        a.walls(0, fz, 12, 10, 1, 4);
        a.walls(0, 0, 12, 10, 5, 8, true);
        if (jetty) {
            for (int x = 0; x <= 12; x++) {
                b.set(x, 0, 0, a.stone(x, 0, 0));
                b.air(x, 1, 0, x, 4, 0);
            }
            for (int x = 0; x <= 12; x += 2) {
                b.set(x, 4, 0, Arch.stairs(a.p.trim.stairs(), Direction.SOUTH, Half.TOP));
            }
            b.fill(0, 5, 0, 12, 5, 0, a.p.post(Direction.Axis.X));
        }
        a.door(6, fz, Direction.SOUTH, ModBlocks.CASTLE_DOOR.get());
        VillageBuildings.entrance(a, piece, 5, 7);
        a.window(3, 2, fz, Direction.NORTH, 2);
        a.window(9, 2, fz, Direction.NORTH, 2);
        for (int x : new int[] {2, 6, 10}) {
            a.window(x, 6, 0, Direction.NORTH, 2);
        }
        for (int z : new int[] {4, 7}) {
            a.window(0, 2, z, Direction.WEST, 2);
            a.window(12, 2, z, Direction.EAST, 2);
            a.window(0, 6, z, Direction.WEST, 2);
            a.window(12, 6, z, Direction.EAST, 2);
        }
        a.window(3, 6, 10, Direction.SOUTH, 2);
        a.window(9, 6, 10, Direction.SOUTH, 2);
        int top = VillageBuildings.roof(a, 0, 0, 12, 10, 8, false);
        b.air(1, 1, fz + 1, 11, 4, 9);
        b.air(1, 6, 1, 11, 7, 9);
        VillageBuildings.upperFloor(a, 1, 1, 11, 9, 5);
        VillageBuildings.staircase(a, 11, 9, 5);
        for (int z = 3; z <= 9; z++) {
            b.set(10, 6, z, a.p.trim.fence().defaultBlockState());
        }
        // Schild über der Tür: ein Ausleger mit einem Fass
        b.set(8, 3, -1, a.p.trim.fence().defaultBlockState());
        b.set(8, 4, -1, a.p.post(Direction.Axis.Z));
        b.set(8, 2, -1, Blocks.BARREL.defaultBlockState());
        // Schanktisch und Fässer hinten
        for (int x = 2; x <= 8; x++) {
            b.set(x, 1, 7, Arch.slab(a.p.trim.slab(), SlabType.TOP));
            b.set(x, 1, 9, barrel(Direction.NORTH));
            if (x % 2 == 0) {
                b.set(x, 2, 9, barrel(Direction.NORTH));
            }
        }
        b.set(2, 1, 7, a.p.trimPlanks());
        b.set(5, 2, 7, a.flower(5, 2, 7));
        // Kamin an der linken Wand
        b.set(1, 1, 4, Blocks.CAMPFIRE.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true));
        b.set(1, 1, 3, a.p.stone());
        b.set(1, 1, 5, a.p.stone());
        b.fill(1, 2, 3, 1, 2, 5, a.p.stone());
        b.set(1, 2, 4, a.p.stone());
        a.chimney(1, 4, 3, top);
        // Tafeln mit Bänken und ein Kronleuchter
        for (int x = 3; x <= 5; x++) {
            b.set(x, 1, 4, a.table());
            b.set(x, 1, 3, a.bench(Direction.SOUTH));
            b.set(x, 1, 5, a.bench(Direction.NORTH));
        }
        for (int x = 7; x <= 9; x++) {
            b.set(x, 1, 4, a.table());
            b.set(x, 1, 3, a.bench(Direction.SOUTH));
            b.set(x, 1, 5, a.bench(Direction.NORTH));
        }
        b.set(4, 2, 4, a.flower(4, 2, 4));
        b.set(8, 2, 4, ModBlocks.FEAST.get().defaultBlockState());
        b.set(6, 4, 4, ModBlocks.CHANDELIER.get().defaultBlockState());
        // Kammern oben
        b.bed(2, 6, 8, a.bed(false), Direction.SOUTH);
        b.bed(4, 6, 8, a.bed(false), Direction.SOUTH);
        b.bed(7, 6, 8, a.bed(false), Direction.SOUTH);
        b.bed(2, 6, 2, a.bed(false), Direction.NORTH);
        b.bed(4, 6, 2, a.bed(false), Direction.NORTH);
        b.set(3, 6, 9, a.p.wood.shelf().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
        chest(a, 8, 6, 9, Direction.NORTH, ModLootTables.VILLAGE_TAVERN);
        for (int x = 2; x <= 8; x++) {
            b.set(x, 6, 5, a.carpet());
        }
        a.hangingLantern(4, 8, 5);
        a.hangingLantern(8, 8, 5);
        piece.resident(b, 5, 1, 8, VillageProfession.INNKEEPER, at(7, 6, 9), at(5, 1, 8));
        piece.resident(b, 4, 1, 2, VillageProfession.PEASANT, at(2, 6, 9), at(6, 1, 3));
    }

    // =====================================================================================
    // Schreibstube (9 × 9)
    // =====================================================================================

    /**
     * Schreibstube: Bücherwände, ein Lesepult unter dem Fenster, ein Schreibtisch mit Kerzen; oben die Kammer des
     * Schreibers mit noch mehr Büchern.
     */
    static void scriptorium(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        VillageBuildings.shell(a, 9, 9, 16);
        a.walls(0, 0, 8, 8, 1, 4);
        a.walls(0, 0, 8, 8, 5, 8, true);
        VillageBuildings.frontDoor(a, piece, 4, a.p.door());
        VillageBuildings.frontWindow(a, 2, 2, 2);
        VillageBuildings.frontWindow(a, 6, 2, 2);
        VillageBuildings.frontWindow(a, 4, 6, 2);
        VillageBuildings.sideWindow(a, 8, 6, 4, 2, false);
        VillageBuildings.backWindow(a, 4, 2, 8, 2);
        VillageBuildings.backWindow(a, 4, 6, 8, 2);
        int top = VillageBuildings.roof(a, 0, 0, 8, 8, 8, true);
        b.air(1, 1, 1, 7, 4, 7);
        b.air(1, 6, 1, 7, 7, 7);
        VillageBuildings.upperFloor(a, 1, 1, 7, 7, 5);
        VillageBuildings.staircase(a, 7, 7, 5);
        for (int z = 3; z <= 7; z++) {
            b.set(6, 6, z, a.p.trim.fence().defaultBlockState());
        }
        // Bücherwand links (zwei Lagen hoch)
        for (int z = 1; z <= 7; z++) {
            for (int y = 1; y <= 3; y++) {
                b.set(1, y, z, z % 3 == 0 && y == 2 ? Blocks.CHISELED_BOOKSHELF.defaultBlockState()
                        .setValue(HorizontalDirectionalBlock.FACING, Direction.EAST) : Blocks.BOOKSHELF.defaultBlockState());
            }
        }
        // Lesepult am Fenster, Schreibtisch mit Kerzen
        b.set(4, 1, 6, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.NORTH));
        b.set(4, 1, 3, a.table());
        b.set(4, 1, 2, a.chair(Direction.SOUTH));
        b.set(4, 2, 3, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));
        b.set(5, 1, 3, a.table());
        b.set(5, 2, 3, Blocks.CHISELED_BOOKSHELF.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
        a.hangingLantern(4, 4, 4);
        // Kammer
        b.bed(2, 6, 6, a.bed(false), Direction.SOUTH);
        for (int x = 1; x <= 5; x++) {
            b.set(x, 6, 1, Blocks.BOOKSHELF.defaultBlockState());
        }
        chest(a, 4, 6, 7, Direction.NORTH, ModLootTables.VILLAGE_SCRIPTORIUM);
        b.set(3, 6, 4, a.carpet());
        b.set(2, 6, 4, a.carpet());
        a.hangingLantern(4, 8, 4);
        piece.resident(b, 4, 1, 4, VillageProfession.SCRIBE, at(2, 6, 7), at(4, 1, 5));
    }

    // =====================================================================================
    // Kartenhaus (7 × 9)
    // =====================================================================================

    /** Kartenhaus des Kartografen: Kartentisch, Pult, Truhe mit Papier, ein Fernrohr-Erker; hinten das Bett. */
    static void mapHouse(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        VillageBuildings.shell(a, 7, 9, 15);
        a.walls(0, 0, 6, 8, 1, 5);
        VillageBuildings.frontDoor(a, piece, 3, a.p.door());
        VillageBuildings.frontWindow(a, 1, 2, 3);
        VillageBuildings.frontWindow(a, 5, 2, 3);
        VillageBuildings.sideWindow(a, 0, 2, 4, 3, true);
        VillageBuildings.sideWindow(a, 6, 2, 4, 3, false);
        int top = VillageBuildings.roof(a, 0, 0, 6, 8, 5, true);
        b.air(1, 1, 1, 5, 4, 7);
        b.set(3, 1, 4, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
        b.set(2, 1, 4, a.chair(Direction.EAST));
        b.set(5, 1, 2, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.WEST));
        b.set(1, 1, 2, Blocks.BOOKSHELF.defaultBlockState());
        b.set(1, 2, 2, Blocks.BOOKSHELF.defaultBlockState());
        b.set(5, 1, 5, barrel(Direction.UP));
        b.set(5, 2, 5, Blocks.FLOWER_POT.defaultBlockState());
        chest(a, 5, 1, 7, Direction.WEST, ModLootTables.VILLAGE_MAP_HOUSE);
        b.bed(1, 1, 6, a.bed(false), Direction.SOUTH);
        b.set(3, 1, 6, a.carpet());
        b.set(3, 1, 7, a.carpet());
        a.hangingLantern(3, 5, 4);
        if (a.style != VillageStyle.CLAY) {
            VillageBuildings.tieBeam(a, 1, 5, 4, 6);
        }
        piece.resident(b, 3, 1, 3, VillageProfession.CARTOGRAPHER, at(1, 1, 7), at(3, 1, 3));
    }

    // =====================================================================================
    // Kräuterhaus (9 × 9)
    // =====================================================================================

    /**
     * Kräuterhaus: vorn ein umzäunter Kräutergarten mit Beerensträuchern und Blumen, im Haus Braustand, Kessel, Töpfe
     * mit Pflanzen und getrocknete Kräuter unter der Decke.
     */
    static void herbHouse(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        VillageBuildings.shell(a, 9, 9, 13);
        a.walls(0, 3, 8, 8, 1, 4);
        int top = VillageBuildings.roof(a, 0, 3, 8, 8, 4, true);
        b.air(1, 1, 4, 7, 3, 7);
        // Kräutergarten vorn (z 0..2), der Weg zur Tür in der Mitte
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 2; z++) {
                boolean path = x == 4;
                if (path) {
                    b.set(x, 0, z, a.style == VillageStyle.STILT ? a.p.planks() : Blocks.DIRT_PATH.defaultBlockState());
                    continue;
                }
                if (a.style == VillageStyle.STILT) {
                    b.set(x, 0, z, a.p.planks());
                    if (z == 0 || x == 0 || x == 8) {
                        b.set(x, 1, z, a.p.fence());
                    }
                    continue;
                }
                boolean rim = z == 0 || x == 0 || x == 8;
                b.set(x, 0, z, rim ? a.p.stone() : Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 7));
                if (rim) {
                    b.set(x, 1, z, a.p.fence());
                } else {
                    b.set(x, 1, z, herb(a, x, z));
                }
            }
        }
        // das Gartentor steht offen (der Weg zur Tür führt hindurch)
        b.set(4, 1, 0, a.p.trim.fenceGate().defaultBlockState().setValue(net.minecraft.world.level.block.FenceGateBlock.FACING, Direction.NORTH)
                .setValue(net.minecraft.world.level.block.FenceGateBlock.OPEN, true));
        a.door(4, 3, Direction.SOUTH);
        VillageBuildings.entrance(a, piece, 4, 4);
        a.window(2, 2, 3, Direction.NORTH, 2);
        a.window(6, 2, 3, Direction.NORTH, 2);
        a.window(0, 2, 6, Direction.WEST, 2);
        a.window(8, 2, 6, Direction.EAST, 2);
        // Braustand, Kessel, Töpfe
        b.set(2, 1, 7, Blocks.BREWING_STAND.defaultBlockState());
        b.set(3, 1, 7, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 2));
        b.set(1, 1, 7, a.table());
        b.set(1, 2, 7, a.flower(1, 2, 7));
        for (int x = 4; x <= 6; x++) {
            b.set(x, 1, 7, Arch.slab(a.p.trim.slab(), SlabType.TOP));
            b.set(x, 2, 7, a.flower(x, 2, 7));
        }
        b.bed(7, 1, 5, a.bed(false), Direction.SOUTH);
        chest(a, 1, 1, 4, Direction.EAST, ModLootTables.VILLAGE_HERB_HOUSE);
        // getrocknete Kräuter an einer Stange unter der Decke
        for (int x = 2; x <= 6; x++) {
            b.set(x, 3, 5, x % 2 == 0 ? Blocks.SPORE_BLOSSOM.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }
        a.hangingLantern(4, 3, 6);
        piece.resident(b, 3, 1, 5, VillageProfession.HERBALIST, at(7, 1, 6), at(2, 1, 6));
    }

    /** Eine Pflanze im Kräutergarten (je nach Land). */
    private static BlockState herb(Arch a, int x, int z) {
        float n = a.b.noise(x, 7, z);
        if (n < 0.3f) {
            return Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3);
        }
        Block[] flowers = switch (a.style) {
            case TIMBER -> new Block[] {ModBlocks.LAVENDER.get(), ModBlocks.GARDEN_ROSE.get(), Blocks.CORNFLOWER, Blocks.ALLIUM};
            case NORDIC -> new Block[] {ModBlocks.FROST_FLOWER.get(), Blocks.LILY_OF_THE_VALLEY, Blocks.BLUE_ORCHID, Blocks.FERN};
            case CLAY -> new Block[] {ModBlocks.FIRE_LILY.get(), Blocks.ORANGE_TULIP, Blocks.DEAD_BUSH, Blocks.TORCHFLOWER};
            case STILT -> new Block[] {Blocks.BLUE_ORCHID, ModBlocks.MOONFLOWER.get(), Blocks.FERN, Blocks.BROWN_MUSHROOM};
        };
        return flowers[(int) (n * 97) % flowers.length].defaultBlockState();
    }

    // =====================================================================================
    // Metzgerei (9 × 7)
    // =====================================================================================

    /** Metzgerei: Räucherofen, Hackklotz, Fässer und Heu; hinten das Bett des Metzgers. */
    static void butchery(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        VillageBuildings.shell(a, 9, 7, 13);
        a.walls(0, 0, 8, 6, 1, 4);
        VillageBuildings.frontDoor(a, piece, 2, a.p.door());
        // breites Verkaufsfenster mit Ladenbrett
        for (int x = 4; x <= 7; x++) {
            b.set(x, 2, 0, Blocks.AIR.defaultBlockState());
            b.set(x, 1, 0, Arch.slab(a.p.trim.slab(), SlabType.TOP));
            b.set(x, 3, -1, a.shutterAwning());
        }
        VillageBuildings.sideWindow(a, 0, 2, 3, 2, true);
        VillageBuildings.backWindow(a, 4, 2, 6, 2);
        int top = VillageBuildings.roof(a, 0, 0, 8, 6, 4, true);
        b.air(1, 1, 1, 7, 3, 5);
        b.set(7, 1, 5, furnace(Blocks.SMOKER, Direction.NORTH));
        a.chimney(7, 5, 2, top);
        b.set(5, 1, 3, Blocks.STRIPPED_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
        b.set(6, 1, 5, barrel(Direction.UP));
        b.set(5, 1, 5, Blocks.HAY_BLOCK.defaultBlockState());
        b.bed(1, 1, 3, a.bed(false), Direction.SOUTH);
        chest(a, 3, 1, 5, Direction.NORTH, ModLootTables.VILLAGE_BUTCHERY);
        a.hangingLantern(4, 3, 3);
        piece.resident(b, 5, 1, 2, VillageProfession.BUTCHER, at(1, 1, 4), at(5, 1, 2));
    }

    // =====================================================================================
    // Gerberei (9 × 9)
    // =====================================================================================

    /** Gerberei: im Hof die Gerbbottiche und Gestelle mit trocknenden Häuten, im Haus Werkbank, Kessel und Bett. */
    static void tannery(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        VillageBuildings.shell(a, 9, 9, 12);
        a.walls(0, 4, 8, 8, 1, 4);
        int top = VillageBuildings.roof(a, 0, 4, 8, 8, 4, true);
        b.air(1, 1, 5, 7, 3, 7);
        // Hof: Bottiche und Trockengestelle
        for (int x : new int[] {1, 3}) {
            b.set(x, 1, 1, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        }
        b.set(2, 1, 1, Blocks.CAULDRON.defaultBlockState());
        for (int x = 5; x <= 8; x++) {
            b.set(x, 1, 1, a.p.fence());
            b.set(x, 2, 1, x == 5 || x == 8 ? a.p.fence() : Blocks.CARPET.brown().defaultBlockState());
        }
        b.set(5, 3, 1, a.p.post(Direction.Axis.X));
        b.set(6, 3, 1, a.p.post(Direction.Axis.X));
        b.set(7, 3, 1, a.p.post(Direction.Axis.X));
        b.set(8, 3, 1, a.p.post(Direction.Axis.X));
        b.set(6, 2, 1, Blocks.WOOL.brown().defaultBlockState());
        b.set(7, 2, 1, Blocks.WOOL.orange().defaultBlockState());
        for (int x = 0; x <= 8; x++) {
            b.set(x, 0, 0, a.style == VillageStyle.STILT ? a.p.planks() : Blocks.DIRT_PATH.defaultBlockState());
        }
        a.door(4, 4, Direction.SOUTH);
        VillageBuildings.entrance(a, piece, 3, 5);
        a.window(2, 2, 4, Direction.NORTH, 2);
        a.window(6, 2, 4, Direction.NORTH, 2);
        a.window(0, 2, 6, Direction.WEST, 2);
        b.set(7, 1, 7, Blocks.CRAFTING_TABLE.defaultBlockState());
        b.set(6, 1, 7, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1));
        b.bed(1, 1, 5, a.bed(false), Direction.SOUTH);
        chest(a, 3, 1, 7, Direction.NORTH, ModLootTables.VILLAGE_TANNERY);
        a.hangingLantern(4, 3, 6);
        piece.resident(b, 2, 1, 2, VillageProfession.TANNER, at(1, 1, 6), at(2, 1, 2));
    }

    // =====================================================================================
    // Steinmetz (11 × 9)
    // =====================================================================================

    /** Steinmetz: im Hof Steinsäge, gestapelte Quader und ein halbfertiges Standbild; dahinter die Hütte. */
    static void masonry(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        VillageBuildings.shell(a, 11, 9, 12);
        a.walls(0, 4, 6, 8, 1, 4);
        int top = VillageBuildings.roof(a, 0, 4, 6, 8, 4, true);
        b.air(1, 1, 5, 5, 3, 7);
        // Hof
        for (int x = 0; x <= 10; x++) {
            for (int z = 0; z <= 8; z++) {
                if (x >= 7 || z <= 3) {
                    b.set(x, 0, z, b.noise(x, 0, z) < 0.5f ? Blocks.GRAVEL.defaultBlockState() : a.p.stone());
                }
            }
        }
        b.set(2, 1, 1, Blocks.STONECUTTER.defaultBlockState().setValue(net.minecraft.world.level.block.StonecutterBlock.FACING, Direction.NORTH));
        // Quaderstapel
        BlockState[] blocks = {Blocks.STONE_BRICKS.defaultBlockState(), Blocks.POLISHED_ANDESITE.defaultBlockState(),
                Blocks.SMOOTH_STONE.defaultBlockState(), a.p.stone(), Blocks.COBBLESTONE.defaultBlockState()};
        for (int x = 8; x <= 10; x++) {
            for (int z = 5; z <= 7; z++) {
                int h = 1 + (int) (b.noise(x, 1, z) * 3);
                for (int y = 1; y <= h; y++) {
                    b.set(x, y, z, blocks[(int) (b.noise(x, y, z) * blocks.length)]);
                }
            }
        }
        // halbfertiges Standbild auf einem Sockel
        b.set(9, 1, 2, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        b.set(9, 2, 2, Blocks.STONE_BRICK_WALL.defaultBlockState());
        b.set(9, 3, 2, Blocks.SMOOTH_STONE.defaultBlockState());
        b.set(7, 1, 1, Blocks.ANDESITE_STAIRS.defaultBlockState());
        a.door(3, 4, Direction.SOUTH);
        VillageBuildings.entrance(a, piece, 2, 4);
        a.window(1, 2, 4, Direction.NORTH, 2);
        a.window(5, 2, 4, Direction.NORTH, 2);
        a.window(0, 2, 6, Direction.WEST, 2);
        b.bed(1, 1, 6, a.bed(false), Direction.SOUTH);
        chest(a, 5, 1, 7, Direction.NORTH, ModLootTables.VILLAGE_MASONRY);
        b.set(5, 1, 5, Blocks.CRAFTING_TABLE.defaultBlockState());
        a.hangingLantern(3, 3, 6);
        piece.resident(b, 3, 1, 2, VillageProfession.MASON, at(1, 1, 7), at(3, 1, 1));
    }

    // =====================================================================================
    // Bogner (9 × 9)
    // =====================================================================================

    /** Bogner: Pfeilmachertisch, Fässer voller Pfeile, eine Schießbahn mit Zielscheiben neben dem Haus. */
    static void bowyer(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        VillageBuildings.shell(a, 9, 9, 13);
        a.walls(0, 0, 5, 6, 1, 4);
        VillageBuildings.frontDoor(a, piece, 2, a.p.door());
        VillageBuildings.frontWindow(a, 4, 2, 2);
        a.window(0, 2, 3, Direction.WEST, 2);
        int top = VillageBuildings.roof(a, 0, 0, 5, 6, 4, true);
        b.air(1, 1, 1, 4, 3, 5);
        b.set(4, 1, 3, Blocks.FLETCHING_TABLE.defaultBlockState());
        b.set(4, 1, 5, barrel(Direction.UP));
        b.set(3, 1, 5, barrel(Direction.UP));
        b.bed(1, 1, 4, a.bed(false), Direction.SOUTH);
        chest(a, 1, 1, 2, Direction.EAST, ModLootTables.VILLAGE_BOWYER);
        a.hangingLantern(2, 3, 3);
        // Schießbahn: Zielscheiben auf Heu, ein Zaun drumherum
        for (int z = 0; z <= 8; z++) {
            for (int x = 6; x <= 8; x++) {
                b.set(x, 0, z, a.style == VillageStyle.STILT ? a.p.planks() : Blocks.GRASS_BLOCK.defaultBlockState());
            }
            b.set(8, 1, z, a.p.fence());
        }
        for (int x = 0; x <= 8; x++) {
            b.set(x, 1, 8, a.p.fence());
            if (x <= 5) {
                b.set(x, 0, 7, a.style == VillageStyle.STILT ? a.p.planks() : Blocks.GRASS_BLOCK.defaultBlockState());
                b.set(x, 0, 8, a.style == VillageStyle.STILT ? a.p.planks() : Blocks.GRASS_BLOCK.defaultBlockState());
            }
        }
        b.set(7, 1, 7, Blocks.HAY_BLOCK.defaultBlockState());
        b.set(7, 2, 7, Blocks.TARGET.defaultBlockState());
        b.set(6, 1, 7, Blocks.TARGET.defaultBlockState());
        b.set(7, 1, 1, Blocks.HAY_BLOCK.defaultBlockState());
        piece.resident(b, 3, 1, 3, VillageProfession.FLETCHER, at(1, 1, 5), at(3, 1, 3));
    }

    /** Hilfe für Werkstätten ohne eigenes Holz: eine Holzstufe. */
    static BlockState plankSlab(Arch a) {
        return Arch.slab(a.p.wood.slab(), SlabType.BOTTOM);
    }

    static BlockState crop(Block crop) {
        return crop instanceof CropBlock c ? c.getStateForAge(c.getMaxAge()) : crop.defaultBlockState();
    }

    static BlockState facingState(Block block, Direction dir) {
        return facing(block, dir);
    }
}
