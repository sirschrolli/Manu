package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import static io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuildings.at;
import static io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuildings.barrel;
import static io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuildings.chest;

import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/** Kapelle, Windmühle, Wachturm, Scheune, Schafweide und Felder. */
final class VillageLandmarks {
    private VillageLandmarks() {}

    // =====================================================================================
    // Kapelle (9 × 15)
    // =====================================================================================

    /**
     * Kapelle: ein Kirchenschiff mit hohen Fenstern und einem steilen Dach, davor der Glockenturm, durch den man
     * eintritt. Innen Bankreihen beiderseits des Mittelgangs, ein Altar mit Kerzen und ein Pult, Kronleuchter an den
     * Balken; oben im Turm hängt die Glocke hinter Schallöffnungen.
     */
    static void chapel(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        VillageBuildings.shell(a, 9, 15, 24);
        // Ecken vor dem Turm: Rasen bzw. Steg
        for (int x : new int[] {0, 1, 7, 8}) {
            for (int z = 0; z <= 3; z++) {
                b.set(x, 0, z, a.style == VillageStyle.STILT ? a.p.planks() : a.style == VillageStyle.CLAY ? Blocks.SAND.defaultBlockState()
                        : Blocks.GRASS_BLOCK.defaultBlockState());
            }
        }
        // Kirchenschiff
        a.walls(0, 4, 8, 14, 1, 6);
        int top = a.style == VillageStyle.NORDIC ? a.gable(0, 4, 8, 14, 6, true, true) : VillageBuildings.roof(a, 0, 4, 8, 14, 6, true);
        b.air(1, 1, 5, 7, 5, 13);
        for (int z : new int[] {7, 10, 13}) {
            a.window(0, 2, z, Direction.WEST, 3);
            a.window(8, 2, z, Direction.EAST, 3);
        }
        for (int y = 3; y <= 5; y++) {
            b.set(4, y, 14, chapelGlass(a));
        }
        // Turm aus Stein (Pfahlbau: Holz), Eingang unten
        BlockState tower = a.style == VillageStyle.STILT ? a.p.planks() : a.p.stone();
        for (int y = 1; y <= 12; y++) {
            for (int x = 2; x <= 6; x++) {
                for (int z = 0; z <= 4; z++) {
                    boolean rim = x == 2 || x == 6 || z == 0 || z == 4;
                    boolean corner = (x == 2 || x == 6) && (z == 0 || z == 4);
                    if (rim) {
                        b.set(x, y, z, corner && a.style == VillageStyle.STILT ? a.p.post(Direction.Axis.Y)
                                : corner ? a.p.stoneAlt() : tower);
                    } else {
                        b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        a.door(4, 0, Direction.SOUTH, ModBlocks.CASTLE_DOOR.get());
        VillageBuildings.entrance(a, piece, 4, 4);
        // Durchgang vom Turm ins Schiff
        b.air(3, 1, 4, 5, 3, 4);
        b.set(3, 3, 4, Arch.stairs(a.p.stoneStairs(), Direction.EAST, Half.TOP));
        b.set(5, 3, 4, Arch.stairs(a.p.stoneStairs(), Direction.WEST, Half.TOP));
        // Fenster im Turm, Glockenstube mit Schallöffnungen
        b.set(4, 6, 0, chapelGlass(a));
        b.set(4, 7, 0, chapelGlass(a));
        for (int y = 9; y <= 10; y++) {
            b.set(4, y, 0, Blocks.AIR.defaultBlockState());
            b.set(2, y, 2, Blocks.AIR.defaultBlockState());
            b.set(6, y, 2, Blocks.AIR.defaultBlockState());
        }
        b.fill(3, 8, 1, 5, 8, 3, a.p.planks());
        b.set(3, 8, 3, Blocks.AIR.defaultBlockState());
        b.ladder(3, 1, 8, 3, Direction.NORTH);
        b.fill(3, 11, 2, 5, 11, 2, a.p.post(Direction.Axis.X));
        b.set(4, 10, 2, Blocks.BELL.defaultBlockState().setValue(BellBlock.FACING, Direction.EAST).setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING));
        // Turmdach: Zeltdach (Lehm: Kuppel aus Kalkputz)
        if (a.style == VillageStyle.CLAY) {
            a.flatRoof(2, 0, 6, 4, 12);
            BlockState plaster = ModBlocks.WHITE_PLASTER.base().get().defaultBlockState();
            b.fill(3, 13, 1, 5, 13, 3, plaster);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                b.set(4 + dir.getStepX(), 14, 2 + dir.getStepZ(), Arch.stairs(ModBlocks.WHITE_PLASTER.stairs().get(), dir.getOpposite(), Half.BOTTOM));
            }
            b.set(4, 14, 2, plaster);
            b.set(4, 15, 2, Blocks.LIGHTNING_ROD.waxed().unaffected().defaultBlockState());
        } else {
            int spire = a.hip(2, 0, 6, 4, 12);
            b.set(4, spire + 1, 2, Blocks.LIGHTNING_ROD.waxed().unaffected().defaultBlockState());
        }
        // Innen: Mittelgang, Bänke, Altar, Kronleuchter
        for (int z = 5; z <= 11; z++) {
            b.set(4, 1, z, Blocks.CARPET.red().defaultBlockState());
        }
        for (int z = 6; z <= 11; z += 2) {
            for (int x : new int[] {1, 2, 3, 5, 6, 7}) {
                b.set(x, 1, z, a.bench(Direction.SOUTH));
            }
        }
        b.fill(3, 1, 13, 5, 1, 13, ModBlocks.LIMESTONE_BRICKS.base().get().defaultBlockState());
        b.set(3, 2, 13, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));
        b.set(5, 2, 13, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));
        b.set(4, 2, 13, a.flower(4, 2, 13));
        b.set(4, 1, 12, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.NORTH));
        chest(a, 1, 1, 13, Direction.EAST, ModLootTables.VILLAGE_CHAPEL);
        for (int z : new int[] {7, 11}) {
            if (a.style != VillageStyle.CLAY) {
                b.fill(1, 6, z, 7, 6, z, a.p.post(Direction.Axis.X));
            }
            b.set(4, 5, z, ModBlocks.CHANDELIER.get().defaultBlockState());
        }
    }

    private static BlockState chapelGlass(Arch a) {
        return switch (a.style) {
            case TIMBER -> ModBlocks.LEADED_GLASS_PANE.get().defaultBlockState();
            case NORDIC -> Blocks.STAINED_GLASS_PANE.lightBlue().defaultBlockState();
            case CLAY -> Blocks.STAINED_GLASS_PANE.yellow().defaultBlockState();
            case STILT -> Blocks.STAINED_GLASS_PANE.green().defaultBlockState();
        };
    }

    // =====================================================================================
    // Windmühle (9 × 9)
    // =====================================================================================

    /**
     * Windmühle: ein gemauerter Sockel mit dem Mahlwerk, darüber der hölzerne (im Süden weiß gekalkte) Turm mit einer
     * Haube, vorn das Flügelkreuz aus vier Flügeln mit Segeltuch, wie ein Windrad versetzt. Innen Mahlstein, Mehlsäcke,
     * Leitern bis unter die Haube; hier wohnt der Müller.
     */
    static void windmill(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        VillageBuildings.shell(a, 9, 9, 28);
        BlockState base = a.p.stone();
        BlockState upper = switch (a.style) {
            case CLAY -> ModBlocks.WHITE_PLASTER.base().get().defaultBlockState();
            case NORDIC -> a.p.planks();
            default -> a.p.planks();
        };
        // Sockel 7 × 7, Turm 5 × 5
        for (int y = 1; y <= 4; y++) {
            for (int x = 1; x <= 7; x++) {
                for (int z = 1; z <= 7; z++) {
                    boolean rim = x == 1 || x == 7 || z == 1 || z == 7;
                    b.set(x, y, z, rim ? (b.chance(x, y, z, 0.2f) ? a.p.stoneAlt() : base) : Blocks.AIR.defaultBlockState());
                }
            }
        }
        for (int x = 1; x <= 7; x++) {
            for (int z = 1; z <= 7; z++) {
                b.set(x, 5, z, x == 1 || x == 7 || z == 1 || z == 7 ? Arch.slab(a.p.stoneSlab(), SlabType.BOTTOM) : a.p.planks());
            }
        }
        for (int y = 5; y <= 13; y++) {
            for (int x = 2; x <= 6; x++) {
                for (int z = 2; z <= 6; z++) {
                    boolean rim = x == 2 || x == 6 || z == 2 || z == 6;
                    boolean corner = (x == 2 || x == 6) && (z == 2 || z == 6);
                    if (rim) {
                        b.set(x, y, z, corner && a.style != VillageStyle.CLAY ? a.p.post(Direction.Axis.Y) : upper);
                    } else if (y > 5) {
                        b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        b.fill(3, 9, 3, 5, 9, 5, a.p.planks());
        b.set(5, 9, 5, Blocks.AIR.defaultBlockState());
        b.set(3, 5, 5, Blocks.AIR.defaultBlockState());
        b.ladder(3, 1, 5, 6, Direction.NORTH);
        b.ladder(5, 6, 9, 6, Direction.NORTH);
        // Haube
        int top = a.gable(2, 2, 6, 6, 13, true, a.style == VillageStyle.NORDIC);
        // Tür und Fenster
        a.door(4, 1, Direction.SOUTH);
        VillageBuildings.entrance(a, piece, 4, 4);
        for (int x = 0; x <= 8; x++) {
            b.set(x, 0, 0, a.style == VillageStyle.CLAY ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : Blocks.DIRT_PATH.defaultBlockState());
        }
        b.set(4, 0, 0, a.style == VillageStyle.CLAY ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : Blocks.DIRT_PATH.defaultBlockState());
        a.window(1, 2, 4, Direction.WEST, 2);
        a.window(7, 2, 4, Direction.EAST, 2);
        a.window(4, 7, 6, Direction.SOUTH, 2);
        a.window(2, 11, 4, Direction.WEST, 1);
        a.window(6, 11, 4, Direction.EAST, 1);
        // Flügelkreuz vorn: Nabe, vier Ruten, versetztes Segeltuch
        int hubY = 12;
        int hubX = 4;
        int sz = 1;
        b.set(hubX, hubY, sz, a.p.post(Direction.Axis.Z));
        b.set(hubX, hubY, sz - 1, a.p.post(Direction.Axis.Z));
        BlockState cloth = Blocks.WOOL.white().defaultBlockState();
        int len = 8;
        for (int k = 1; k <= len; k++) {
            // Ruten
            b.set(hubX, hubY + k, sz - 1, a.p.post(Direction.Axis.Y));
            b.set(hubX + k, hubY, sz - 1, a.p.post(Direction.Axis.X));
            b.set(hubX - k, hubY, sz - 1, a.p.post(Direction.Axis.X));
            if (hubY - k >= 3) {
                b.set(hubX, hubY - k, sz - 1, a.p.post(Direction.Axis.Y));
            }
            if (k >= 3) {
                // Segeltuch, zwei Blöcke breit: oben rechts, rechts unten, unten links, links oben (wie ein Windrad)
                for (int w = 1; w <= 2; w++) {
                    b.set(hubX + w, hubY + k, sz - 1, cloth);
                    b.set(hubX + k, hubY - w, sz - 1, cloth);
                    if (hubY - k >= 3) {
                        b.set(hubX - w, hubY - k, sz - 1, cloth);
                    }
                    b.set(hubX - k, hubY + w, sz - 1, cloth);
                }
            }
        }
        b.set(hubX, hubY, sz - 2, a.p.trimPlanks());
        // Mahlwerk und Mehl
        b.set(4, 1, 4, Blocks.GRINDSTONE.defaultBlockState().setValue(net.minecraft.world.level.block.GrindstoneBlock.FACE,
                net.minecraft.world.level.block.state.properties.AttachFace.FLOOR));
        b.set(2, 1, 2, Blocks.WOOL.white().defaultBlockState());
        b.set(2, 1, 3, Blocks.WOOL.white().defaultBlockState());
        b.set(2, 2, 2, Blocks.WOOL.white().defaultBlockState());
        b.set(6, 1, 2, Blocks.HAY_BLOCK.defaultBlockState());
        b.set(6, 2, 2, Blocks.HAY_BLOCK.defaultBlockState());
        b.set(6, 1, 3, barrel(Direction.UP));
        b.bed(5, 1, 5, a.bed(true), Direction.SOUTH);
        chest(a, 2, 1, 6, Direction.EAST, ModLootTables.VILLAGE_FARM);
        a.hangingLantern(4, 4, 3);
        piece.resident(b, 4, 1, 3, VillageProfession.FARMER, at(5, 1, 6), at(3, 1, 4));
    }

    // =====================================================================================
    // Wachturm (5 × 5)
    // =====================================================================================

    /** Wachturm: ein gemauerter Schaft mit Leiter, oben eine Plattform mit Brüstung unter einem Dach; dort hält ein Bogenschütze Wache. */
    static void watchtower(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        VillageBuildings.shell(a, 5, 5, 18);
        BlockState wall = a.style == VillageStyle.STILT ? a.p.planks() : a.p.stone();
        for (int y = 1; y <= 9; y++) {
            for (int x = 0; x <= 4; x++) {
                for (int z = 0; z <= 4; z++) {
                    boolean rim = x == 0 || x == 4 || z == 0 || z == 4;
                    boolean corner = (x == 0 || x == 4) && (z == 0 || z == 4);
                    if (rim) {
                        b.set(x, y, z, corner && a.style == VillageStyle.STILT ? a.p.post(Direction.Axis.Y)
                                : corner ? a.p.stoneAlt() : wall);
                    } else {
                        b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        a.door(2, 0, Direction.SOUTH);
        VillageBuildings.entrance(a, piece, 2, 2);
        for (int y : new int[] {4, 7}) {
            b.set(2, y, 4, Blocks.IRON_BARS.defaultBlockState());
            b.set(0, y, 2, Blocks.IRON_BARS.defaultBlockState());
            b.set(4, y, 2, Blocks.IRON_BARS.defaultBlockState());
        }
        // Plattform (einen Block breiter), Leiter hinauf
        for (int x = -1; x <= 5; x++) {
            for (int z = -1; z <= 5; z++) {
                boolean rim = x == -1 || x == 5 || z == -1 || z == 5;
                b.set(x, 10, z, rim ? a.p.trimPlanks() : a.p.planks());
                if (rim) {
                    boolean corner = (x == -1 || x == 5) && (z == -1 || z == 5);
                    b.set(x, 11, z, corner ? a.p.post(Direction.Axis.Y) : a.p.fence());
                    if (corner) {
                        b.set(x, 12, z, a.p.post(Direction.Axis.Y));
                        b.set(x, 13, z, a.p.post(Direction.Axis.Y));
                    }
                }
            }
        }
        for (int x = 0; x <= 4; x++) {
            b.set(x, 9, -1, Arch.stairs(a.p.trim.stairs(), Direction.SOUTH, Half.TOP));
            b.set(x, 9, 5, Arch.stairs(a.p.trim.stairs(), Direction.NORTH, Half.TOP));
        }
        b.set(2, 10, 3, Blocks.AIR.defaultBlockState());
        b.ladder(2, 1, 10, 3, Direction.NORTH);
        a.hangingLantern(2, 13, 2);
        if (a.style == VillageStyle.CLAY) {
            a.flatRoof(-1, -1, 5, 5, 14);
        } else {
            a.hip(-1, -1, 5, 5, 14);
        }
        chest(a, 1, 1, 3, Direction.EAST, ModLootTables.VILLAGE_WATCHTOWER);
        piece.guard(b, 1, 11, 1, true);
    }

    // =====================================================================================
    // Scheune (11 × 9)
    // =====================================================================================

    /** Scheune: ein großes Tor, darin Heuballen, ein Heuboden, Fässer und ein Komposter; in der Ecke schläft der Knecht. */
    static void barn(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        VillageBuildings.shell(a, 11, 9, 16);
        a.walls(0, 0, 10, 8, 1, 5);
        int top = VillageBuildings.roof(a, 0, 0, 10, 8, 5, true);
        b.air(1, 1, 1, 9, 4, 7);
        // großes Tor
        b.air(4, 1, 0, 6, 4, 0);
        b.set(4, 4, 0, a.p.post(Direction.Axis.X));
        b.set(5, 4, 0, a.p.post(Direction.Axis.X));
        b.set(6, 4, 0, a.p.post(Direction.Axis.X));
        VillageBuildings.entrance(a, piece, 4, 6);
        for (int x = 4; x <= 6; x++) {
            b.set(x, 0, 0, a.style == VillageStyle.STILT ? a.p.planks() : a.p.stone());
        }
        a.window(2, 2, 0, Direction.NORTH, 2);
        a.window(8, 2, 0, Direction.NORTH, 2);
        a.window(0, 2, 4, Direction.WEST, 2);
        a.window(10, 2, 4, Direction.EAST, 2);
        // Heuboden hinten
        for (int x = 1; x <= 9; x++) {
            for (int z = 5; z <= 7; z++) {
                b.set(x, 4, z, a.p.planks());
                if (b.noise(x, 5, z) < 0.7f) {
                    b.set(x, 5, z, Blocks.HAY_BLOCK.defaultBlockState());
                }
            }
        }
        b.ladder(9, 1, 4, 4, Direction.NORTH);
        b.set(9, 4, 4, Blocks.AIR.defaultBlockState());
        b.ladder(9, 4, 4, 4, Direction.NORTH);
        // Heu, Fässer, Komposter
        for (int x : new int[] {1, 2, 8}) {
            b.set(x, 1, 1, Blocks.HAY_BLOCK.defaultBlockState());
        }
        b.set(1, 2, 1, Blocks.HAY_BLOCK.defaultBlockState());
        b.set(1, 1, 4, barrel(Direction.UP));
        b.set(1, 1, 5, Blocks.COMPOSTER.defaultBlockState());
        b.set(8, 1, 4, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        b.bed(1, 1, 6, a.bed(true), Direction.SOUTH);
        chest(a, 3, 1, 7, Direction.NORTH, ModLootTables.VILLAGE_FARM);
        a.hangingLantern(5, 4, 3);
        piece.resident(b, 5, 1, 3, VillageProfession.FARMER, at(1, 1, 7), at(5, 1, 4));
    }

    // =====================================================================================
    // Schafweide (13 × 11)
    // =====================================================================================

    /** Schafweide: eine umzäunte Wiese mit Tor, Futterraufe und Tränke, hinten die Hütte des Schäfers mit Webstuhl. */
    static void sheepfold(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        a.foundation(0, 0, 12, 10, 8);
        BlockState ground = a.style == VillageStyle.STILT ? a.p.planks() : a.style == VillageStyle.CLAY ? Blocks.COARSE_DIRT.defaultBlockState()
                : Blocks.GRASS_BLOCK.defaultBlockState();
        for (int x = 0; x <= 12; x++) {
            for (int z = 0; z <= 10; z++) {
                b.set(x, 0, z, ground);
                boolean rim = x == 0 || x == 12 || z == 0 || z == 10;
                if (rim) {
                    b.set(x, 1, z, a.p.fence());
                }
            }
        }
        b.set(6, 1, 0, a.p.trim.fenceGate().defaultBlockState().setValue(FenceGateBlock.FACING, Direction.NORTH));
        VillageBuildings.entrance(a, piece, 6, 6);
        // Hütte hinten rechts (x 8..12, z 6..10)
        a.floor(8, 6, 12, 10);
        a.walls(8, 6, 12, 10, 1, 4);
        VillageBuildings.roof(a, 8, 6, 12, 10, 4, false);
        b.air(9, 1, 7, 11, 3, 9);
        a.door(10, 6, Direction.SOUTH);
        // eine zweite Tür nach draußen (so kommt der Schäfer heim, ohne die Herde hinauszulassen)
        a.door(12, 8, Direction.WEST);
        b.entrance = new BlockPos(13, 1, 8);
        // ein Pfad (im Moor ein Steg) außen am Zaun entlang von vorn bis zu dieser Tür
        BlockState walk = a.style == VillageStyle.STILT ? a.p.planks() : a.style == VillageStyle.CLAY ? Blocks.COARSE_DIRT.defaultBlockState()
                : Blocks.DIRT_PATH.defaultBlockState();
        for (int z = -1; z <= 8; z++) {
            if (a.style == VillageStyle.STILT) {
                b.set(13, 0, z, walk);
                a.stilt(13, z, z % 3 == 0);
            } else {
                b.column(13, z, a.style == VillageStyle.CLAY ? Blocks.SANDSTONE.defaultBlockState() : Blocks.DIRT.defaultBlockState(), -1);
                b.set(13, 0, z, walk);
            }
            b.air(13, 1, z, 13, 3, z);
        }
        a.window(10, 2, 10, Direction.SOUTH, 2);
        b.set(11, 1, 7, Blocks.LOOM.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.WEST));
        b.bed(9, 1, 8, a.bed(true), Direction.SOUTH);
        chest(a, 11, 1, 9, Direction.WEST, ModLootTables.VILLAGE_SHEPHERD);
        a.hangingLantern(10, 3, 8);
        // Futterraufe und Tränke
        b.set(2, 1, 9, Blocks.HAY_BLOCK.defaultBlockState());
        b.set(3, 1, 9, Blocks.HAY_BLOCK.defaultBlockState());
        b.set(2, 2, 9, Blocks.HAY_BLOCK.defaultBlockState());
        b.set(5, 1, 9, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        for (int i = 0; i < 4; i++) {
            VillageResidents.animal(b, piece, 3 + i * 2, 1, 4 + (i & 1) * 2, net.minecraft.world.entity.EntityTypes.SHEEP);
        }
        // der Schäfer webt in seiner Hütte (durch die Tür zur Weide hat er die Herde im Blick)
        piece.resident(b, 10, 1, 8, VillageProfession.SHEPHERD, at(9, 1, 9), at(10, 1, 8));
    }

    // =====================================================================================
    // Feld (11 × 13)
    // =====================================================================================

    /**
     * Feld: zwei Beete beiderseits eines Wassergrabens, jedes mit seiner Frucht (Weizen, Möhren, Kartoffeln, Rote Bete,
     * Tomaten, Kohl, Zwiebeln, Gerste, Knoblauch), eine Vogelscheuche, ein Komposter; ringsum ein Zaun bzw. eine niedrige
     * Mauer mit Tor zum Weg.
     */
    static void field(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        a.foundation(0, 0, 10, 12, 4);
        Block[] crops = {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS, ModBlocks.TOMATOES.get(), ModBlocks.CABBAGES.get(),
                ModBlocks.ONIONS.get(), ModBlocks.BARLEY.get(), ModBlocks.GARLIC_CROP.get(), Blocks.WHEAT};
        Block left = crops[b.random.nextInt(crops.length)];
        Block right = crops[b.random.nextInt(crops.length)];
        BlockState rimBlock = a.style == VillageStyle.CLAY ? Blocks.SANDSTONE_WALL.defaultBlockState()
                : a.style == VillageStyle.NORDIC ? Blocks.COBBLESTONE_WALL.defaultBlockState() : a.p.fence();
        for (int x = 0; x <= 10; x++) {
            for (int z = 0; z <= 12; z++) {
                boolean rim = x == 0 || x == 10 || z == 0 || z == 12;
                if (rim) {
                    b.set(x, 0, z, a.style == VillageStyle.CLAY ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState());
                    b.set(x, 1, z, rimBlock);
                } else if (x == 5) {
                    b.set(x, -1, z, Blocks.DIRT.defaultBlockState());
                    b.set(x, 0, z, Blocks.WATER.defaultBlockState());
                } else {
                    b.set(x, 0, z, Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 7));
                    Block crop = x < 5 ? left : right;
                    b.set(x, 1, z, grown(crop, b.noise(x, 1, z)));
                }
            }
        }
        // Tor zum Weg und ein Steg über den Graben
        // das Tor zum Weg steht offen (der Bauer geht hindurch)
        b.set(5, 1, 0, a.p.trim.fenceGate().defaultBlockState().setValue(FenceGateBlock.FACING, Direction.NORTH).setValue(FenceGateBlock.OPEN, true));
        b.set(5, 0, 0, a.style == VillageStyle.CLAY ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.DIRT_PATH.defaultBlockState());
        b.set(5, 0, 1, Arch.slab(a.p.wood.slab(), SlabType.TOP));
        VillageBuildings.entrance(a, piece, 5, 5);
        // Vogelscheuche
        b.set(2, 1, 6, a.p.fence());
        b.set(2, 2, 6, Blocks.HAY_BLOCK.defaultBlockState());
        b.set(1, 2, 6, a.p.fence());
        b.set(3, 2, 6, a.p.fence());
        b.set(2, 3, 6, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
        b.set(9, 1, 11, Blocks.COMPOSTER.defaultBlockState());
        b.set(9, 1, 10, barrel(Direction.UP));
        if (!piece.behind()) {
            // ein Feld hinter einem Haus hat keinen eigenen Weg: wer dort arbeitete, fände vielleicht nicht zurück
            piece.resident(b, 4, 1, -1, VillageProfession.FARMER, null, at(4, 1, 1));
        }
    }

    // =====================================================================================
    // Fischerhütte (7 × 10)
    // =====================================================================================

    /**
     * Fischerhütte am Ufer: vorn eine kleine Hütte mit Bett, Räucherofen und Fässern voller Fisch, hinten durch die
     * zweite Tür ein Steg auf Pfählen ins Wasser – mit Geländer, einer Laterne am Ende und dem Fang des Tages.
     */
    static void fishingHut(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        VillageBuildings.shell(a, 7, 6, 10);
        a.walls(0, 0, 6, 5, 1, 4);
        VillageBuildings.frontDoor(a, piece, 3, a.p.door());
        VillageBuildings.frontWindow(a, 1, 2, 2);
        VillageBuildings.frontWindow(a, 5, 2, 2);
        VillageBuildings.sideWindow(a, 0, 2, 3, 2, true);
        VillageBuildings.sideWindow(a, 6, 2, 2, 2, false);
        int top = VillageBuildings.roof(a, 0, 0, 6, 5, 4, true);
        b.air(1, 1, 1, 5, 3, 4);
        // hinten hinaus auf den Steg
        a.door(3, 5, Direction.NORTH);
        // Steg: drei Bohlen breit auf Pfählen, mit Geländer; am Ende eine Laterne
        for (int z = 6; z <= 9; z++) {
            for (int x = 1; x <= 5; x++) {
                boolean deck = x >= 2 && x <= 4;
                if (deck) {
                    b.set(x, 0, z, x == 3 ? a.p.planks() : a.p.beam(Direction.Axis.Z));
                    a.stilt(x, z, (x == 2 || x == 4) && (z == 6 || z == 9));
                    b.air(x, 1, z, x, 3, z);
                }
            }
            if (z >= 7) {
                b.set(1, 1, z, Blocks.AIR.defaultBlockState());
                b.set(5, 1, z, Blocks.AIR.defaultBlockState());
                b.set(2, 1, z, z == 9 ? a.p.fence() : Blocks.AIR.defaultBlockState());
                b.set(4, 1, z, a.p.fence());
            }
        }
        b.set(4, 2, 9, a.p.lantern.defaultBlockState());
        b.set(2, 2, 9, Blocks.AIR.defaultBlockState());
        // der Fang: ein Fass und ein Eimer voll Fisch (Kessel mit Wasser), daneben aufgerolltes Netz
        b.set(2, 1, 7, barrel(Direction.UP));
        b.set(2, 1, 8, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 2));
        // Hütte: Bett, Räucherofen mit Kamin, Fässer, Truhe, Werkbank
        b.bed(1, 1, 3, a.bed(true), Direction.SOUTH);
        b.set(5, 1, 4, VillageBuildings.furnace(Blocks.SMOKER, Direction.WEST));
        a.chimney(5, 4, 3, top);
        b.set(5, 1, 3, barrel(Direction.WEST));
        b.set(5, 2, 3, barrel(Direction.UP));
        chest(a, 5, 1, 1, Direction.WEST, ModLootTables.VILLAGE_FISHER);
        b.set(1, 1, 1, Blocks.CRAFTING_TABLE.defaultBlockState());
        b.set(1, 2, 1, a.flower(1, 2, 1));
        a.hangingLantern(3, 3, 2);
        piece.resident(b, 3, 1, 2, VillageProfession.FISHER, at(1, 1, 4), at(3, 1, 8));
        // das Boot des Fischers liegt am Steg
        VillageResidents.boat(b, 6, 8, a.style);
    }

    /** Eine Feldfrucht in einem Reifegrad (meist reif). */
    private static BlockState grown(Block block, float noise) {
        if (block instanceof CropBlock crop) {
            int age = noise < 0.65f ? crop.getMaxAge() : (int) (noise * crop.getMaxAge());
            return crop.getStateForAge(Math.min(age, crop.getMaxAge()));
        }
        return block.defaultBlockState();
    }

    static BlockState ladder(Direction facing) {
        return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, facing);
    }
}
