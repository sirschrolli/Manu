package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Die Gebäude der Dörfer. Jedes wird in eigenen Koordinaten gebaut (siehe {@link VillageBuilder}: Tür vorn bei z = 0,
 * Fußboden bei y = 0) und nimmt seine Baustoffe aus dem {@link Arch} – so steht dasselbe Haus im Fachwerkdorf mit
 * Kalkputz und Ziegeldach, im Norden als Blockhaus mit Schieferdach, im Süden mit Lehmputz und Flachdach und im Moor auf
 * Pfählen. Hier: das Verteilen auf die Bauten, gemeinsame Teile und die Wohnhäuser; die Werkstätten stehen in
 * {@link VillageWorkshops}, Kapelle, Mühle, Felder und Türme in {@link VillageLandmarks}.
 */
final class VillageBuildings {
    private VillageBuildings() {}

    static void build(Arch a, KingdomVillagePiece piece) {
        VillageBuilding kind = piece.building();
        if (kind == null) {
            return;
        }
        switch (kind) {
            case HOUSE_SMALL -> smallHouse(a, piece);
            case HOUSE -> house(a, piece);
            case HOUSE_LARGE -> largeHouse(a, piece);
            case SMITHY -> VillageWorkshops.smithy(a, piece);
            case BAKERY -> VillageWorkshops.bakery(a, piece);
            case TAVERN -> VillageWorkshops.tavern(a, piece);
            case SCRIPTORIUM -> VillageWorkshops.scriptorium(a, piece);
            case MAP_HOUSE -> VillageWorkshops.mapHouse(a, piece);
            case HERB_HOUSE -> VillageWorkshops.herbHouse(a, piece);
            case BUTCHERY -> VillageWorkshops.butchery(a, piece);
            case TANNERY -> VillageWorkshops.tannery(a, piece);
            case MASONRY -> VillageWorkshops.masonry(a, piece);
            case BOWYER -> VillageWorkshops.bowyer(a, piece);
            case CHAPEL -> VillageLandmarks.chapel(a, piece);
            case WINDMILL -> VillageLandmarks.windmill(a, piece);
            case WATCHTOWER -> VillageLandmarks.watchtower(a, piece);
            case BARN -> VillageLandmarks.barn(a, piece);
            case SHEEPFOLD -> VillageLandmarks.sheepfold(a, piece);
            case FIELD -> VillageLandmarks.field(a, piece);
            case FISHING_HUT -> VillageLandmarks.fishingHut(a, piece);
            case MARKET_STALL -> {
            }
        }
    }

    // =====================================================================================
    // Gemeinsame Teile
    // =====================================================================================

    /** Unterbau und Fußboden eines Rechtecks (0..w-1 × 0..d-1), darüber {@code clear} Blöcke freiräumen. */
    static void shell(Arch a, int w, int d, int clear) {
        a.foundation(0, 0, w - 1, d - 1, clear);
        a.floor(0, 0, w - 1, d - 1);
    }

    /**
     * Das Dach, wie es die Bauweise verlangt: Satteldach (nordisch doppelt so steil), im Lehmdorf ein Flachdach mit
     * Brüstung. Mauerkrone auf Höhe {@code y}; gibt die höchste Stelle zurück.
     */
    static int roof(Arch a, int x0, int z0, int x1, int z1, int y, boolean alongZ) {
        return switch (a.style) {
            case CLAY -> {
                a.flatRoof(x0, z0, x1, z1, y + 1);
                yield y + 2;
            }
            default -> a.gable(x0, z0, x1, z1, y, alongZ, false);
        };
    }

    /**
     * Zugang von der Straße zur Tür (Spalten x0..x1 vor der Front, bei z = −1): ein Stück Weg, oder eine Stufe hinauf
     * bzw. hinab, je nachdem, wie der Fußboden zur Straße liegt. Im Pfahldorf ein Steg.
     */
    static void entrance(Arch a, KingdomVillagePiece piece, int x0, int x1) {
        VillageBuilder b = a.b;
        if (b.entrance == null) {
            b.entrance = new BlockPos((x0 + x1) / 2, 1, -1);
        }
        for (int x = x0; x <= x1; x++) {
            if (a.style == VillageStyle.STILT) {
                b.set(x, 0, -1, a.p.planks());
                a.stilt(x, -1, x == x0 || x == x1);
                b.air(x, 1, -1, x, 3, -1);
                if (piece.step() > 0) {
                    b.set(x, 0, -1, Arch.stairs(a.p.wood.stairs(), Direction.SOUTH, Half.BOTTOM));
                } else if (piece.step() < 0) {
                    b.set(x, 1, -1, Arch.stairs(a.p.wood.stairs(), Direction.NORTH, Half.BOTTOM));
                }
                continue;
            }
            switch (piece.step()) {
                case 1 -> {
                    b.column(x, -1, a.p.stone(), 0);
                    b.set(x, 0, -1, Arch.stairs(a.p.stoneStairs(), Direction.SOUTH, Half.BOTTOM));
                    b.air(x, 1, -1, x, 3, -1);
                }
                case -1 -> {
                    b.set(x, 0, -1, a.p.stone());
                    b.set(x, 1, -1, Arch.stairs(a.p.stoneStairs(), Direction.NORTH, Half.BOTTOM));
                    b.air(x, 2, -1, x, 4, -1);
                }
                default -> {
                    b.set(x, -1, -1, a.style == VillageStyle.CLAY ? Blocks.SANDSTONE.defaultBlockState() : Blocks.DIRT.defaultBlockState());
                    b.set(x, 0, -1, a.style == VillageStyle.CLAY ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : Blocks.DIRT_PATH.defaultBlockState());
                    b.air(x, 1, -1, x, 3, -1);
                }
            }
        }
    }

    /** Haustür vorn bei x (Blick ins Haus nach Süden) samt Zugang und einer Laterne an einem Ausleger daneben. */
    static void frontDoor(Arch a, KingdomVillagePiece piece, int x, Block door) {
        a.door(x, 0, Direction.SOUTH, door);
        entrance(a, piece, x, x);
        if (a.style != VillageStyle.CLAY) {
            // im Lehmdorf hängt dort das Sonnensegel
            a.bracketLantern(x + 1, 3, -1);
        }
    }

    /** Quer liegender Balken bei z (von x0 bis x1) auf Höhe y, mit einer Laterne darunter in der Mitte. */
    static void tieBeam(Arch a, int x0, int x1, int z, int y) {
        for (int x = x0; x <= x1; x++) {
            a.b.set(x, y, z, a.p.post(Direction.Axis.X));
        }
        a.hangingLantern((x0 + x1) / 2, y - 1, z);
    }

    /** Schornstein in der Ecke (x, z) vom Ofen (y = 2) bis über das Dach ({@code top}). */
    static void chimney(Arch a, int x, int z, int top) {
        a.chimney(x, z, 2, top);
    }

    static BlockState facing(Block block, Direction facing) {
        return block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
    }

    static BlockState furnace(Block block, Direction facing) {
        return block.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, facing);
    }

    static BlockState barrel(Direction facing) {
        return Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, facing);
    }

    /** Truhe mit Dorf-Beute. */
    static void chest(Arch a, int x, int y, int z, Direction facing) {
        chest(a, x, y, z, facing, ModLootTables.VILLAGE_HOUSE);
    }

    /** Truhe mit der Beute {@code loot}. */
    static void chest(Arch a, int x, int y, int z, Direction facing, net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot) {
        a.b.chest(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), loot);
    }

    static BlockPos at(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }

    /** Fenster in der Vorderwand (z = 0) bzw. Rückwand (z = d−1) und in den Seitenwänden. */
    static void frontWindow(Arch a, int x, int y, int h) {
        a.window(x, y, 0, Direction.NORTH, h);
    }

    static void backWindow(Arch a, int x, int y, int z, int h) {
        a.window(x, y, z, Direction.SOUTH, h);
    }

    static void sideWindow(Arch a, int x, int y, int z, int h, boolean left) {
        a.window(x, y, z, left ? Direction.WEST : Direction.EAST, h);
    }

    /** Einfache Holztreppe im Haus von (x, 1, zFrom) hinauf nach Norden bis auf das Obergeschoss (Boden bei y = top). */
    static void staircase(Arch a, int x, int zFrom, int top) {
        for (int k = 0; k < top; k++) {
            int y = 1 + k;
            int z = zFrom - k;
            a.b.set(x, y, z, Arch.stairs(a.p.wood.stairs(), Direction.NORTH, Half.BOTTOM));
            // darunter massiv, darüber frei
            for (int below = 1; below < y; below++) {
                a.b.set(x, below, z, a.p.planks());
            }
            a.b.air(x, y + 1, z, x, top + 2, z);
        }
    }

    /** Obergeschoss-Boden (Dielen) über dem Rechteck bei Höhe y, mit Balken an den Rändern. */
    static void upperFloor(Arch a, int x0, int z0, int x1, int z1, int y) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                a.b.set(x, y, z, a.p.planks());
            }
        }
    }

    // =====================================================================================
    // Kleines Wohnhaus (7 × 7)
    // =====================================================================================

    /**
     * Kleines Wohnhaus: ein Raum unter dem offenen Dachstuhl, Ofen mit Kamin, zwei Betten, Tisch mit Stühlen, Truhe und
     * Fass, eine Laterne unter dem Zugbalken. Hier wohnen einfache Dörfler.
     */
    static void smallHouse(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        boolean mirrored = b.random.nextBoolean();
        shell(a, 7, 7, 12);
        a.walls(0, 0, 6, 6, 1, 4);
        frontDoor(a, piece, 3, a.p.door());
        frontWindow(a, 1, 2, 2);
        frontWindow(a, 5, 2, 2);
        sideWindow(a, 0, 2, 3, 2, true);
        sideWindow(a, 6, 2, 3, 2, false);
        backWindow(a, 3, 2, 6, 2);
        int top = roof(a, 0, 0, 6, 6, 4, true);
        b.air(1, 1, 1, 5, 3, 5);
        if (a.style != VillageStyle.CLAY) {
            tieBeam(a, 1, 5, 3, 4);
        } else {
            a.hangingLantern(3, 4, 3);
        }
        int bed = mirrored ? 5 : 1;
        int side = mirrored ? 1 : 5;
        b.bed(bed, 1, 4, a.bed(true), Direction.SOUTH);
        b.bed(bed, 1, 2, a.bed(true), Direction.NORTH);
        b.set(side, 1, 5, furnace(Blocks.FURNACE, Direction.NORTH));
        chimney(a, side, 5, top);
        b.set(side, 1, 4, barrel(Direction.UP));
        b.set(side, 2, 4, a.flower(side, 2, 4));
        chest(a, 3, 1, 5, Direction.NORTH);
        b.set(side, 1, 2, a.table());
        b.set(side, 1, 1, a.chair(Direction.SOUTH));
        b.set(3, 1, 3, a.carpet());
        b.set(3, 1, 2, a.carpet());
        piece.resident(b, 3, 1, 3, VillageProfession.PEASANT, at(bed, 1, 5), at(3, 1, 3));
        piece.resident(b, 3, 1, 2, VillageProfession.PEASANT, at(bed, 1, 1), at(side == 5 ? 4 : 2, 1, 1));
    }

    // =====================================================================================
    // Wohnhaus mit Obergeschoss (9 × 9)
    // =====================================================================================

    /**
     * Wohnhaus mit zwei Geschossen: unten Stube und Küche mit Herd, Tisch und Bänken, oben die Schlafkammer unter dem
     * Dach. Eine Holztreppe an der linken Wand verbindet beide.
     */
    static void house(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        shell(a, 9, 9, 16);
        a.walls(0, 0, 8, 8, 1, 4);
        a.walls(0, 0, 8, 8, 5, 8, true);
        frontDoor(a, piece, 4, a.p.door());
        frontWindow(a, 2, 2, 2);
        frontWindow(a, 6, 2, 2);
        frontWindow(a, 2, 6, 2);
        frontWindow(a, 6, 6, 2);
        sideWindow(a, 0, 6, 4, 2, true);
        sideWindow(a, 8, 2, 3, 2, false);
        sideWindow(a, 8, 6, 5, 2, false);
        backWindow(a, 4, 2, 8, 2);
        backWindow(a, 4, 6, 8, 2);
        int top = roof(a, 0, 0, 8, 8, 8, true);
        b.air(1, 1, 1, 7, 4, 7);
        b.air(1, 6, 1, 7, 7, 7);
        upperFloor(a, 1, 1, 7, 7, 5);
        staircase(a, 1, 7, 5);
        // Geländer am Treppenloch
        for (int z = 3; z <= 7; z++) {
            b.set(2, 6, z, a.p.trim.fence().defaultBlockState());
        }
        b.set(1, 6, 7, a.p.trim.fence().defaultBlockState());
        // Erdgeschoss: Herd mit Kamin, Tisch, Bänke, Fass, Werkbank
        b.set(7, 1, 7, furnace(Blocks.SMOKER, Direction.NORTH));
        chimney(a, 7, 7, top);
        b.set(6, 1, 7, barrel(Direction.UP));
        b.set(7, 1, 6, Blocks.CRAFTING_TABLE.defaultBlockState());
        b.set(5, 1, 3, a.table());
        b.set(5, 1, 4, a.table());
        b.set(4, 1, 3, a.chair(Direction.EAST));
        b.set(4, 1, 4, a.chair(Direction.EAST));
        b.set(6, 1, 3, a.chair(Direction.WEST));
        b.set(6, 1, 4, a.chair(Direction.WEST));
        b.set(5, 2, 3, a.flower(5, 2, 3));
        a.hangingLantern(5, 4, 4);
        b.set(3, 1, 1, a.p.wood.shelf().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH));
        // Obergeschoss: drei Betten, Truhe, Teppich, Laterne
        b.bed(4, 6, 7, a.bed(false), Direction.SOUTH);
        b.bed(6, 6, 7, a.bed(false), Direction.SOUTH);
        b.bed(7, 6, 2, a.bed(false), Direction.NORTH);
        chest(a, 5, 6, 7, Direction.NORTH);
        b.set(5, 6, 4, a.carpet());
        b.set(5, 6, 5, a.carpet());
        b.set(4, 6, 4, a.carpet());
        b.set(4, 6, 5, a.carpet());
        if (a.style != VillageStyle.CLAY) {
            a.hangingLantern(5, 8, 4);
        } else {
            a.hangingLantern(5, 8, 4);
        }
        piece.resident(b, 5, 1, 2, VillageProfession.PEASANT, at(4, 6, 8), at(5, 1, 2));
        piece.resident(b, 3, 1, 5, VillageProfession.PEASANT, at(6, 6, 8), at(4, 1, 5));
        piece.resident(b, 6, 1, 5, VillageProfession.PEASANT, at(7, 6, 1), at(6, 1, 5));
    }

    // =====================================================================================
    // Großes Wohnhaus (11 × 9)
    // =====================================================================================

    /**
     * Großes Wohnhaus eines wohlhabenden Bauern: zwei Geschosse, im Fachwerkdorf mit einem vorkragenden Obergeschoss über
     * geschnitzten Knaggen, die Tür unter einem kleinen Vordach. Unten Stube mit Kamin und lange Tafel, oben vier Betten.
     */
    static void largeHouse(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        boolean jetty = a.style == VillageStyle.TIMBER || a.style == VillageStyle.STILT;
        shell(a, 11, 9, 18);
        a.walls(0, 1, 10, 8, 1, 4);
        // das Obergeschoss kragt vorn einen Block über (Fachwerk, Pfahlbau); sonst bündig
        int uz0 = jetty ? 0 : 1;
        a.walls(0, uz0, 10, 8, 5, 8, true);
        // die Front steht einen Block zurück: davor ein kleiner Vorplatz unter der Vorkragung
        for (int x = 0; x <= 10; x++) {
            b.set(x, 0, 0, a.stone(x, 0, 0));
            b.air(x, 1, 0, x, 4, 0);
        }
        if (jetty) {
            // Knaggen unter der Vorkragung
            for (int x = 0; x <= 10; x += 2) {
                b.set(x, 4, 0, Arch.stairs(a.p.trim.stairs(), Direction.SOUTH, Half.TOP));
            }
            b.fill(0, 5, 0, 10, 5, 0, a.p.post(Direction.Axis.X));
        }
        a.door(5, 1, Direction.SOUTH, a.p.door());
        entrance(a, piece, 4, 6);
        b.set(4, 1, 0, Arch.stairs(a.p.stoneStairs(), Direction.EAST, Half.BOTTOM));
        b.set(6, 1, 0, Arch.stairs(a.p.stoneStairs(), Direction.WEST, Half.BOTTOM));
        a.hangingLantern(5, 4, 0);
        a.window(2, 2, 1, Direction.NORTH, 2);
        a.window(8, 2, 1, Direction.NORTH, 2);
        a.window(2, 6, uz0, Direction.NORTH, 2);
        a.window(5, 6, uz0, Direction.NORTH, 2);
        a.window(8, 6, uz0, Direction.NORTH, 2);
        sideWindow(a, 0, 2, 5, 2, true);
        sideWindow(a, 0, 6, 5, 2, true);
        sideWindow(a, 10, 2, 5, 2, false);
        sideWindow(a, 10, 6, 5, 2, false);
        backWindow(a, 3, 2, 8, 2);
        backWindow(a, 7, 2, 8, 2);
        backWindow(a, 5, 6, 8, 2);
        int top = roof(a, 0, uz0, 10, 8, 8, true);
        b.air(1, 1, 2, 9, 4, 7);
        b.air(1, 6, uz0 + 1, 9, 7, 7);
        upperFloor(a, 1, uz0 + 1, 9, 7, 5);
        staircase(a, 9, 7, 5);
        for (int z = 3; z <= 7; z++) {
            b.set(8, 6, z, a.p.trim.fence().defaultBlockState());
        }
        // Stube: Kamin an der linken Wand, lange Tafel mit Bänken
        b.set(1, 1, 5, furnace(Blocks.FURNACE, Direction.EAST));
        chimney(a, 1, 5, top);
        b.set(1, 1, 4, barrel(Direction.EAST));
        b.set(1, 1, 6, barrel(Direction.EAST));
        for (int x = 3; x <= 6; x++) {
            b.set(x, 1, 5, a.table());
            b.set(x, 1, 4, a.bench(Direction.SOUTH));
            b.set(x, 1, 6, a.bench(Direction.NORTH));
        }
        b.set(4, 2, 5, a.flower(4, 2, 5));
        b.set(7, 1, 7, Blocks.CRAFTING_TABLE.defaultBlockState());
        b.set(6, 1, 7, a.p.wood.shelf().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
        a.hangingLantern(5, 4, 3);
        // Schlafkammer
        b.bed(1, 6, 6, a.bed(false), Direction.SOUTH);
        b.bed(3, 6, 6, a.bed(false), Direction.SOUTH);
        b.bed(5, 6, 6, a.bed(false), Direction.SOUTH);
        b.bed(1, 6, uz0 + 2, a.bed(false), Direction.NORTH);
        chest(a, 7, 6, 7, Direction.NORTH);
        for (int x = 2; x <= 5; x++) {
            b.set(x, 6, uz0 + 3, a.carpet());
        }
        a.hangingLantern(5, 8, 4);
        piece.resident(b, 5, 1, 3, VillageProfession.PEASANT, at(1, 6, 7), at(4, 1, 3));
        piece.resident(b, 3, 1, 3, VillageProfession.FARMER, at(3, 6, 7), at(3, 1, 3));
        piece.resident(b, 7, 1, 3, VillageProfession.PEASANT, at(5, 6, 7), at(7, 1, 3));
    }

    /** Hilfe: eine Stufe aus dem Holz des Dorfs als Sitz oder Ablage. */
    static BlockState woodSlab(Arch a, SlabType type) {
        return Arch.slab(a.p.wood.slab(), type);
    }

    static BlockState modBlock(net.neoforged.neoforge.registries.DeferredBlock<? extends Block> block) {
        return block.get().defaultBlockState();
    }

    static Block lanternBlock() {
        return ModBlocks.STEEL_LANTERN.get();
    }
}
