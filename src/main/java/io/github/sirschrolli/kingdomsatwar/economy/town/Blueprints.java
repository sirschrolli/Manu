package io.github.sirschrolli.kingdomsatwar.economy.town;

import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Die Baupläne der Siedlung im Stil eines mittelalterlichen Städtchens: Fachwerk über einem Steinsockel, Bleiglasfenster
 * mit Fensterläden, Dächer aus roten oder braunen Ziegeln, Schiefer oder Holzschindeln. Vorn (−z) liegt immer der
 * Eingang; y = 0 ist der Fußboden (eine Lage über dem Gelände).
 */
final class Blueprints {
    private Blueprints() {}

    static Blueprint create(BuildingType type) {
        PlanBuilder b = new PlanBuilder();
        switch (type) {
            case HOUSE -> house(b);
            case FIELD -> field(b);
            case LUMBER_HUT -> lumberHut(b);
            case WELL -> well(b);
            case SMITHY -> smithy(b);
            case MINE -> mine(b);
            case MARKET -> market(b);
            case TAVERN -> tavern(b);
            case TOWNHOUSE -> townhouse(b);
            case BARRACKS -> barracks(b);
            case MAP_HOUSE -> mapHouse(b);
            case THRONE_HALL -> throneHall(b);
        }
        return b.build();
    }

    // =====================================================================================
    // Baustoffe
    // =====================================================================================

    private static BlockState s(Block block) {
        return block.defaultBlockState();
    }

    private static BlockState stone() {
        return s(ModBlocks.MEDIEVAL_STONE.base().get());
    }

    private static BlockState fieldstone() {
        return s(ModBlocks.ROUGH_FIELDSTONE.base().get());
    }

    private static BlockState frame() {
        return s(ModBlocks.TIMBER_FRAME.get());
    }

    private static BlockState diagonal() {
        return s(ModBlocks.TIMBER_FRAME_DIAGONAL.get());
    }

    private static BlockState cross() {
        return s(ModBlocks.TIMBER_FRAME_CROSS.get());
    }

    private static BlockState beam(Direction.Axis axis) {
        return PlanBuilder.log(ModBlocks.TIMBER_BEAM.get(), axis);
    }

    private static BlockState plaster() {
        return s(ModBlocks.WHITE_PLASTER.base().get());
    }

    private static BlockState pane() {
        return s(ModBlocks.LEADED_GLASS_PANE.get());
    }

    private static BlockState planks() {
        return s(Blocks.SPRUCE_PLANKS);
    }

    private static BlockState facing(Block block, Direction facing) {
        return block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
    }

    private static BlockState barrel(Direction facing) {
        return Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, facing);
    }

    private static BlockState seat(Block seat, Direction facing) {
        return facing(seat, facing);
    }

    private static BlockState roofBlock(BuildingSet roof) {
        return s(roof.base().get());
    }

    // =====================================================================================
    // Bausteine
    // =====================================================================================

    /**
     * Fachwerkwand entlang x (bei z) von x0 bis x1, Höhe y1..y4: unten Steinsockel, darüber Gefache mit Streben an den
     * Enden, oben das Rähm (waagerechter Balken). Ecken bekommen Pfosten.
     */
    private static void timberWallX(PlanBuilder b, int x0, int x1, int z, int y0, int y1) {
        for (int x = x0; x <= x1; x++) {
            boolean corner = x == x0 || x == x1;
            for (int y = y0; y <= y1; y++) {
                BlockState state;
                if (corner) {
                    state = beam(Direction.Axis.Y);
                } else if (y == y0) {
                    state = stone();
                } else if (y == y1) {
                    state = beam(Direction.Axis.X);
                } else {
                    state = (x == x0 + 1 || x == x1 - 1) && y == y1 - 1 ? diagonal() : frame();
                }
                b.set(x, y, z, state);
            }
        }
    }

    /** Fachwerkwand entlang z (bei x). */
    private static void timberWallZ(PlanBuilder b, int z0, int z1, int x, int y0, int y1) {
        for (int z = z0; z <= z1; z++) {
            boolean corner = z == z0 || z == z1;
            for (int y = y0; y <= y1; y++) {
                BlockState state;
                if (corner) {
                    state = beam(Direction.Axis.Y);
                } else if (y == y0) {
                    state = stone();
                } else if (y == y1) {
                    state = beam(Direction.Axis.Z);
                } else {
                    state = (z == z0 + 1 || z == z1 - 1) && y == y1 - 1 ? diagonal() : frame();
                }
                b.set(x, y, z, state);
            }
        }
    }

    /**
     * Fenster in einer Wand bei (x, y..y+h-1, z) mit Läden auf beiden Seiten. {@code out} zeigt aus der Wand hinaus,
     * {@code along} entlang der Wand.
     */
    private static void window(PlanBuilder b, int x, int y, int z, int h, Direction out, Direction along, Block shutter) {
        for (int i = 0; i < h; i++) {
            b.set(x, y + i, z, pane());
        }
        int ox = x + out.getStepX();
        int oz = z + out.getStepZ();
        for (int side = -1; side <= 1; side += 2) {
            int sx = ox + along.getStepX() * side;
            int sz = oz + along.getStepZ() * side;
            for (int i = 0; i < h; i++) {
                // ein offener Laden liegt an der Seite seines Blocks, die seiner Blickrichtung gegenüberliegt
                b.shutter(sx, y + i, sz, shutter, out.getOpposite(), i == 0 ? Half.BOTTOM : Half.TOP);
            }
        }
    }

    /** Kamin aus Ziegeln von y0 bis y1 (oben eine Ziegelstufe). */
    private static void chimney(PlanBuilder b, int x, int z, int y0, int y1) {
        for (int y = y0; y <= y1; y++) {
            b.set(x, y, z, Blocks.BRICKS);
        }
        b.set(x, y1 + 1, z, PlanBuilder.slab(Blocks.BRICK_SLAB, SlabType.BOTTOM));
    }

    /** Stufe vor einer Tür: Treppe, die zum Haus hin ansteigt. */
    private static void step(PlanBuilder b, int x, int z, Direction toHouse, Block stairs) {
        b.set(x, 0, z, PlanBuilder.stairs(stairs, toHouse, Half.BOTTOM));
    }

    // =====================================================================================
    // Wohnhaus
    // =====================================================================================

    /**
     * Wohnhaus (2 Betten): Fachwerkhaus 7 × 8 über Steinsockel, hohe Bleiglasfenster mit grünen Läden, rotes Satteldach mit
     * Fachwerkgiebeln, Kamin. Innen zwei Betten, Ofen, Tisch mit Stühlen, Fass und eine Laterne unter dem Zugbalken.
     */
    private static void house(PlanBuilder b) {
        BuildingSet roof = ModBlocks.RED_ROOF_TILES;
        Block shutter = ModBlocks.GREEN_SHUTTERS.get();
        b.foundation(-3, -3, 3, 4, fieldstone());
        b.fill(-3, 0, -3, 3, 0, 4, stone());
        b.fill(-2, 0, -2, 2, 0, 3, planks());
        timberWallX(b, -3, 3, -3, 1, 4);
        timberWallX(b, -3, 3, 4, 1, 4);
        timberWallZ(b, -3, 4, -3, 1, 4);
        timberWallZ(b, -3, 4, 3, 1, 4);
        b.air(-2, 1, -2, 2, 4, 3);
        // Tür und Fenster vorn
        b.door(0, 1, -3, Blocks.SPRUCE_DOOR, Direction.SOUTH);
        b.set(0, 3, -3, cross());
        window(b, -2, 2, -3, 2, Direction.NORTH, Direction.EAST, shutter);
        window(b, 2, 2, -3, 2, Direction.NORTH, Direction.EAST, shutter);
        // hinten und an den Seiten
        window(b, -1, 2, 4, 2, Direction.SOUTH, Direction.EAST, shutter);
        window(b, -3, 2, 0, 2, Direction.WEST, Direction.SOUTH, shutter);
        window(b, 3, 2, 1, 2, Direction.EAST, Direction.SOUTH, shutter);
        // Dach mit Fachwerkgiebeln, Giebelfenster vorn
        int ridge = b.gableAlongZ(-3, 3, -3, 4, 5, roof.stairs().get(), roofBlock(roof), frame());
        b.set(0, 6, -3, pane());
        b.set(0, 6, 4, pane());
        // Zugbalken unter dem Dach, daran die Laterne
        b.fill(-2, 5, 0, 2, 5, 0, beam(Direction.Axis.X));
        b.hangingLantern(0, 4, 0, Blocks.LANTERN);
        // Ofen mit Kamin in der hinteren rechten Ecke
        b.set(2, 1, 3, facing(Blocks.FURNACE, Direction.WEST));
        chimney(b, 2, 3, 2, ridge);
        // Betten, Teppich, Fass
        b.bed(-2, 1, 2, Blocks.BED.red(), Direction.SOUTH);
        b.bed(-1, 1, 2, Blocks.BED.blue(), Direction.SOUTH);
        b.set(-2, 1, 1, Blocks.CARPET.red());
        b.set(-1, 1, 1, Blocks.CARPET.red());
        b.set(0, 1, 3, barrel(Direction.UP));
        // Tisch mit zwei Stühlen und einer Blume
        b.set(1, 1, 0, ModBlocks.OAK_TABLE.get());
        b.set(1, 1, -1, seat(ModBlocks.OAK_CHAIR.get(), Direction.SOUTH));
        b.set(2, 1, 0, seat(ModBlocks.OAK_CHAIR.get(), Direction.WEST));
        b.set(1, 2, 0, Blocks.POTTED_RED_TULIP);
        b.set(-2, 1, -2, Blocks.CRAFTING_TABLE);
        // Stufe und Blumen vor dem Haus
        step(b, 0, -4, Direction.SOUTH, ModBlocks.MEDIEVAL_STONE.stairs().get());
        b.entranceAt(0, 0, -5);
        b.workAt(0, 1, 0);
    }

    // =====================================================================================
    // Feld
    // =====================================================================================

    /**
     * Feld (für einen Bauern): 9 × 9 Ackerland, in der Mitte die Wasserquelle, Reihen mit Weizen, Karotten, Kartoffeln
     * und Roter Bete, rundum ein Zaun mit Tor, eine Vogelscheuche, davor Truhe, Fass und Komposter.
     */
    private static void field(PlanBuilder b) {
        b.foundation(-5, -6, 5, 5, s(Blocks.DIRT));
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                boolean rim = Math.abs(x) == 5 || Math.abs(z) == 5;
                if (rim) {
                    b.set(x, -1, z, s(Blocks.GRASS_BLOCK));
                    b.set(x, 0, z, s(Blocks.SPRUCE_FENCE));
                    continue;
                }
                if (x == 0 && z == 0) {
                    b.set(x, -1, z, s(Blocks.WATER));
                    continue;
                }
                if (x == 2 && z == 2) {
                    continue;   // Vogelscheuche
                }
                b.set(x, -1, z, Blocks.FARMLAND.defaultBlockState().setValue(net.minecraft.world.level.block.FarmlandBlock.MOISTURE, 7));
                Block crop = z <= -3 ? Blocks.WHEAT : z <= -1 ? Blocks.CARROTS : z <= 2 ? Blocks.POTATOES : Blocks.BEETROOTS;
                b.set(x, 0, z, crop.defaultBlockState());
            }
        }
        // Tor vorn
        b.set(0, 0, -5, Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, Direction.NORTH));
        b.set(-5, 1, -5, Blocks.LANTERN.defaultBlockState());
        b.set(5, 1, -5, Blocks.LANTERN.defaultBlockState());
        // Vogelscheuche
        b.set(2, -1, 2, s(Blocks.COARSE_DIRT));
        b.set(2, 0, 2, s(Blocks.SPRUCE_FENCE));
        b.set(2, 1, 2, s(Blocks.SPRUCE_FENCE));
        b.set(1, 1, 2, s(Blocks.SPRUCE_FENCE));
        b.set(3, 1, 2, s(Blocks.SPRUCE_FENCE));
        b.set(2, 2, 2, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, Direction.NORTH));
        // vor dem Tor: Truhe, Fass, Komposter, Heuballen
        b.set(-2, -1, -6, s(Blocks.GRASS_BLOCK));
        b.chestAt(-2, 0, -6, Direction.NORTH);
        b.set(-3, 0, -6, barrel(Direction.UP));
        b.set(2, 0, -6, s(Blocks.COMPOSTER));
        b.set(3, 0, -6, s(Blocks.HAY_BLOCK));
        b.workAt(0, 0, -4);
        b.entranceAt(0, 0, -7);
    }

    // =====================================================================================
    // Holzfällerhütte
    // =====================================================================================

    /**
     * Holzfällerhütte (1 Bett): Blockhaus aus Fichtenstämmen mit Schindeldach, davor ein Hackklotz und ein Holzstapel,
     * hinten eine Setzlingsschule. Innen Bett, Truhe, Werkbank und Laterne.
     */
    private static void lumberHut(PlanBuilder b) {
        BuildingSet roof = ModBlocks.WOODEN_SHINGLES;
        b.foundation(-3, -2, 3, 3, fieldstone());
        b.fill(-3, 0, -2, 3, 0, 3, fieldstone());
        b.fill(-2, 0, -1, 2, 0, 2, s(Blocks.SPRUCE_PLANKS));
        for (int y = 1; y <= 3; y++) {
            for (int x = -3; x <= 3; x++) {
                for (int z = -2; z <= 3; z++) {
                    boolean cornerPost = (x == -3 || x == 3) && (z == -2 || z == 3);
                    boolean front = z == -2 || z == 3;
                    boolean side = x == -3 || x == 3;
                    if (cornerPost) {
                        b.set(x, y, z, PlanBuilder.log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.Y));
                    } else if (front) {
                        b.set(x, y, z, PlanBuilder.log(Blocks.SPRUCE_LOG, Direction.Axis.X));
                    } else if (side) {
                        b.set(x, y, z, PlanBuilder.log(Blocks.SPRUCE_LOG, Direction.Axis.Z));
                    }
                }
            }
        }
        b.air(-2, 1, -1, 2, 3, 2);
        b.door(0, 1, -2, Blocks.SPRUCE_DOOR, Direction.SOUTH);
        b.set(-2, 2, -2, pane());
        b.set(2, 2, -2, pane());
        b.set(-3, 2, 1, pane());
        b.set(3, 2, 0, pane());
        int ridge = b.gableAlongX(-3, 3, -2, 3, 4, roof.stairs().get(), roofBlock(roof),
                PlanBuilder.log(Blocks.SPRUCE_LOG, Direction.Axis.X));
        b.set(0, ridge + 1, 0, s(Blocks.AIR));
        b.bed(-2, 1, 1, Blocks.BED.green(), Direction.SOUTH);
        b.chestAt(2, 1, 2, Direction.WEST);
        b.set(2, 1, -1, s(Blocks.CRAFTING_TABLE));
        b.hangingLantern(0, 3, 0, Blocks.LANTERN);
        // Hackklotz und Holzstapel vor der Hütte
        b.set(2, 0, -4, PlanBuilder.log(Blocks.STRIPPED_OAK_LOG, Direction.Axis.Y));
        for (int x = -3; x <= -1; x++) {
            b.set(x, 0, -4, PlanBuilder.log(Blocks.OAK_LOG, Direction.Axis.Z));
            if (x > -3) {
                b.set(x, 1, -4, PlanBuilder.log(Blocks.OAK_LOG, Direction.Axis.Z));
            }
        }
        b.set(-2, 2, -4, PlanBuilder.log(Blocks.OAK_LOG, Direction.Axis.Z));
        // Setzlingsschule hinten
        for (int x = -2; x <= 2; x += 2) {
            b.set(x, -1, 5, s(Blocks.PODZOL));
            b.set(x, 0, 5, s(x == 0 ? Blocks.SPRUCE_SAPLING : Blocks.OAK_SAPLING));
        }
        step(b, 0, -3, Direction.SOUTH, Blocks.SPRUCE_STAIRS);
        b.workAt(1, 0, -4);
        b.entranceAt(0, 0, -4);
    }

    // =====================================================================================
    // Brunnen
    // =====================================================================================

    /** Brunnen: Steinkranz um tiefes Wasser, vier Pfosten mit einem Ziegeldach, eine Kette hängt in den Schacht. */
    private static void well(PlanBuilder b) {
        BuildingSet roof = ModBlocks.RED_ROOF_TILES;
        b.foundation(-2, -2, 2, 2, stone());
        b.fill(-2, -1, -2, 2, -1, 2, s(ModBlocks.MEDIEVAL_STONE.base().get()));
        for (int y = -4; y <= -1; y++) {
            b.fill(-1, y, -1, 1, y, 1, s(Blocks.WATER));
        }
        b.fill(-2, -5, -2, 2, -5, 2, stone());
        for (int y = -4; y <= -2; y++) {
            b.ring(-2, -2, 2, 2, y, y, stone());
        }
        b.ring(-2, -2, 2, 2, 0, 0, stone());
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 && Math.abs(z) == 2) {
                    for (int y = 1; y <= 2; y++) {
                        b.set(x, y, z, s(Blocks.SPRUCE_FENCE));
                    }
                }
            }
        }
        b.hipRoof(-2, -2, 2, 2, 3, roof.stairs().get(), roofBlock(roof));
        b.fill(-1, 3, 0, 1, 3, 0, PlanBuilder.log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.X));
        for (int y = 1; y <= 2; y++) {
            b.set(0, y, 0, s(Blocks.IRON_CHAIN));
        }
        b.set(0, 0, 0, s(Blocks.AIR));
        b.workAt(0, 1, -3);
        b.entranceAt(0, 0, -3);
    }

    // =====================================================================================
    // Schmiede
    // =====================================================================================

    /**
     * Schmiede (1 Bett): Steinhaus mit offener Werkstatt unter einem Vordach, Esse mit Glut und Schornstein, Amboss,
     * Schleifstein, Schmiedetisch und Löschwasser; hinten die Kammer des Schmieds.
     */
    private static void smithy(PlanBuilder b) {
        BuildingSet roof = ModBlocks.DARK_SLATE_SHINGLES;
        b.foundation(-4, -4, 4, 4, fieldstone());
        b.fill(-4, 0, -4, 4, 0, 4, stone());
        // Kammer hinten (z 1..4)
        b.ring(-4, 1, 4, 4, 1, 4, stone());
        b.air(-3, 1, 2, 3, 4, 3);
        for (int x : new int[] {-4, 4}) {
            for (int z : new int[] {1, 4}) {
                for (int y = 1; y <= 4; y++) {
                    b.set(x, y, z, beam(Direction.Axis.Y));
                }
            }
        }
        b.fill(-3, 4, 1, 3, 4, 1, beam(Direction.Axis.X));
        b.door(-2, 1, 1, Blocks.SPRUCE_DOOR, Direction.SOUTH);
        window(b, 2, 2, 4, 2, Direction.SOUTH, Direction.EAST, ModBlocks.RED_SHUTTERS.get());
        // offene Werkstatt vorn (z -4..0): Pfosten vorn, Vordach
        for (int x : new int[] {-4, 4}) {
            for (int y = 1; y <= 4; y++) {
                b.set(x, y, -4, beam(Direction.Axis.Y));
            }
        }
        b.fill(-3, 4, -4, 3, 4, -4, beam(Direction.Axis.X));
        b.fill(-4, 4, -3, -4, 4, 0, beam(Direction.Axis.Z));
        b.fill(4, 4, -3, 4, 4, 0, beam(Direction.Axis.Z));
        // Esse links in der Werkstatt mit Schornstein
        b.fill(-4, 1, -2, -3, 1, 0, s(Blocks.BRICKS));
        b.set(-3, 1, -1, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true).setValue(CampfireBlock.SIGNAL_FIRE, false));
        b.set(-4, 1, -1, s(Blocks.BLAST_FURNACE).setValue(FurnaceBlock.FACING, Direction.EAST));
        b.fill(-4, 2, -2, -3, 3, 0, s(Blocks.BRICKS));
        b.set(-3, 2, -1, s(Blocks.AIR));
        b.set(-3, 3, -1, s(Blocks.BRICKS));
        // Dach: Schiefer über allem, Schornstein hindurch
        int ridge = b.gableAlongX(-4, 4, -4, 4, 5, roof.stairs().get(), roofBlock(roof), stone());
        chimney(b, -4, -1, 4, ridge + 1);
        // Werkzeuge
        b.set(0, 1, -2, Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, Direction.EAST));
        b.set(2, 1, -3, s(Blocks.GRINDSTONE));
        b.set(3, 1, 0, s(Blocks.SMITHING_TABLE));
        b.set(3, 1, -1, s(Blocks.WATER_CAULDRON).setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 3));
        b.chestAt(2, 1, 0, Direction.NORTH);
        b.hangingLantern(0, 3, -2, Blocks.LANTERN);
        // Kammer: Bett, Tisch
        b.bed(2, 1, 2, Blocks.BED.brown(), Direction.SOUTH);
        b.set(-2, 1, 3, ModBlocks.OAK_TABLE.get());
        b.set(-3, 1, 3, seat(ModBlocks.OAK_CHAIR.get(), Direction.EAST));
        b.hangingLantern(0, 3, 3, Blocks.LANTERN);
        b.workAt(0, 1, -3);
        b.entranceAt(0, 0, -5);
    }

    // =====================================================================================
    // Mine
    // =====================================================================================

    /**
     * Mine (2 Bergleute): ein Stollenhaus aus Feldstein über dem Schacht, eine Leiter zehn Blöcke hinab in eine Kammer
     * mit Stützbalken, Laternen und der Erzwand, vor der die Bergleute arbeiten; oben Lore, Gleis und Truhe.
     */
    private static void mine(PlanBuilder b) {
        BuildingSet roof = ModBlocks.WOODEN_SHINGLES;
        b.foundation(-3, -3, 3, 3, fieldstone());
        b.fill(-3, 0, -3, 3, 0, 3, fieldstone());
        b.ring(-3, -3, 3, 3, 1, 3, fieldstone());
        for (int x : new int[] {-3, 3}) {
            for (int z : new int[] {-3, 3}) {
                for (int y = 1; y <= 3; y++) {
                    b.set(x, y, z, beam(Direction.Axis.Y));
                }
            }
        }
        b.air(-2, 1, -2, 2, 3, 2);
        b.air(-1, 1, -3, 1, 2, -3);
        b.fill(-1, 3, -3, 1, 3, -3, beam(Direction.Axis.X));
        b.hipRoof(-4, -4, 4, 4, 4, roof.stairs().get(), roofBlock(roof));
        int bottom = -11;
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH);
        // Kammer unten (rundum Stein, damit sie in jedem Gelände dicht ist), mit Stützbalken und Erzwand
        b.fill(-3, bottom - 1, -4, 4, bottom + 3, 2, s(Blocks.STONE));
        b.air(-2, bottom, -3, 3, bottom + 2, 1);
        b.fill(-2, bottom - 1, -3, 3, bottom - 1, 1, s(Blocks.COBBLESTONE));
        for (int x : new int[] {-2, 3}) {
            for (int y = bottom; y <= bottom + 2; y++) {
                b.set(x, y, -1, PlanBuilder.log(Blocks.SPRUCE_LOG, Direction.Axis.Y));
            }
        }
        b.fill(-2, bottom + 2, -1, 3, bottom + 2, -1, PlanBuilder.log(Blocks.SPRUCE_LOG, Direction.Axis.X));
        // Schacht 2 × 2 von der Hütte bis in die Kammer, die Leiter an der Rückwand
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
        b.set(-1, bottom + 1, -4, s(Blocks.COAL_ORE));
        b.set(1, bottom, -4, s(Blocks.IRON_ORE));
        b.set(2, bottom + 2, -4, s(Blocks.COPPER_ORE));
        b.set(0, bottom + 2, -4, s(Blocks.COAL_ORE));
        b.hangingLantern(0, bottom + 1, -1, Blocks.LANTERN);
        b.set(3, bottom, 1, Blocks.RAIL.defaultBlockState());
        b.set(2, bottom, 1, Blocks.RAIL.defaultBlockState());
        b.mark(-1, bottom, -3);
        b.workAt(1, bottom, -3);
        // Geländer um die Schachtöffnung (offen zur Leiter hin)
        for (int[] f : new int[][] {{-1, 0}, {-1, 1}, {2, 0}, {2, 1}, {0, -1}, {1, -1}}) {
            b.set(f[0], 1, f[1], s(Blocks.SPRUCE_FENCE));
        }
        // oben: Truhe, Fass und Werkbank
        b.chestAt(-2, 1, 2, Direction.EAST);
        b.set(-2, 1, 1, barrel(Direction.UP));
        b.set(2, 1, -2, s(Blocks.CRAFTING_TABLE));
        b.hangingLantern(0, 3, -1, Blocks.LANTERN);
        b.entranceAt(0, 0, -4);
    }

    // =====================================================================================
    // Markt
    // =====================================================================================

    /**
     * Markt (2 Händler): zwei Marktstände mit gestreiften Markisen aus Wolle, Theken aus Fässern und Tischen, Waren
     * (Melonen, Kürbisse, Heu, Blumen), in der Mitte ein gepflasterter Platz mit einer Laterne.
     */
    private static void market(PlanBuilder b) {
        b.foundation(-6, -3, 6, 4, stone());
        for (int x = -6; x <= 6; x++) {
            for (int z = -3; z <= 4; z++) {
                b.set(x, -1, z, (x + z & 1) == 0 ? s(Blocks.STONE_BRICKS) : s(Blocks.COBBLESTONE));
            }
        }
        stall(b, -4, Blocks.WOOL.red(), Blocks.WOOL.white(), Blocks.WOOL_STAIRS.red(), Blocks.WOOL_STAIRS.white());
        stall(b, 4, Blocks.WOOL.blue(), Blocks.WOOL.yellow(), Blocks.WOOL_STAIRS.blue(), Blocks.WOOL_STAIRS.yellow());
        // Waren
        b.set(-5, 1, 1, s(Blocks.MELON));
        b.set(-3, 1, 1, s(Blocks.PUMPKIN));
        b.set(-4, 1, 1, s(Blocks.HAY_BLOCK));
        b.set(3, 1, 1, barrel(Direction.UP));
        b.set(5, 1, 1, barrel(Direction.UP));
        b.set(4, 2, 1, s(Blocks.POTTED_POPPY));
        b.set(3, 2, 1, s(Blocks.POTTED_DANDELION));
        // Laterne in der Mitte
        b.set(0, 0, 1, s(Blocks.SPRUCE_FENCE));
        b.set(0, 1, 1, s(Blocks.SPRUCE_FENCE));
        b.set(0, 2, 1, Blocks.LANTERN.defaultBlockState());
        b.chestAt(-6, 0, 3, Direction.EAST);
        b.mark(-4, 0, -1);
        b.mark(4, 0, -1);
        b.workAt(-4, 0, 0);
        b.entranceAt(0, 0, -4);
    }

    /** Ein Marktstand bei x: Theke, vier Pfosten, Markise mit Streifen. */
    private static void stall(PlanBuilder b, int cx, Block woolA, Block woolB, Block stairsA, Block stairsB) {
        for (int x = cx - 1; x <= cx + 1; x++) {
            b.set(x, 0, 2, x == cx ? s(ModBlocks.OAK_TABLE.get()) : barrel(Direction.NORTH));
        }
        for (int x : new int[] {cx - 2, cx + 2}) {
            for (int z : new int[] {-1, 3}) {
                for (int y = 0; y <= 2; y++) {
                    b.set(x, y, z, s(Blocks.SPRUCE_FENCE));
                }
            }
        }
        for (int x = cx - 2; x <= cx + 2; x++) {
            boolean a = ((x - cx) & 1) == 0;
            b.set(x, 3, -1, PlanBuilder.stairs(a ? stairsA : stairsB, Direction.SOUTH, Half.BOTTOM));
            for (int z = 0; z <= 2; z++) {
                b.set(x, 3, z, s(a ? woolA : woolB));
            }
            b.set(x, 3, 3, PlanBuilder.stairs(a ? stairsA : stairsB, Direction.NORTH, Half.BOTTOM));
        }
    }

    // =====================================================================================
    // Taverne
    // =====================================================================================

    /**
     * Taverne (Wirt, 2 Betten): zweistöckiges Fachwerkhaus 9 × 11 mit Steinsockel, unten die Schankstube mit Theke aus
     * Fässern, Tischen und Bänken, Kamin und Kronleuchter; eine Treppe führt in die Kammern mit zwei Betten; braunes Dach.
     */
    private static void tavern(PlanBuilder b) {
        BuildingSet roof = ModBlocks.BROWN_ROOF_TILES;
        Block shutter = ModBlocks.RED_SHUTTERS.get();
        b.foundation(-4, -5, 4, 5, fieldstone());
        b.fill(-4, 0, -5, 4, 0, 5, stone());
        b.fill(-3, 0, -4, 3, 0, 4, s(Blocks.OAK_PLANKS));
        // Erdgeschoss aus Stein, Obergeschoss in Fachwerk
        b.ring(-4, -5, 4, 5, 1, 3, stone());
        timberWallX(b, -4, 4, -5, 4, 7);
        timberWallX(b, -4, 4, 5, 4, 7);
        timberWallZ(b, -5, 5, -4, 4, 7);
        timberWallZ(b, -5, 5, 4, 4, 7);
        for (int x : new int[] {-4, 4}) {
            for (int z : new int[] {-5, 5}) {
                for (int y = 1; y <= 7; y++) {
                    b.set(x, y, z, beam(Direction.Axis.Y));
                }
            }
        }
        b.air(-3, 1, -4, 3, 7, 4);
        // Zwischendecke (mit Treppenloch)
        b.fill(-3, 4, -4, 3, 4, 4, s(Blocks.SPRUCE_PLANKS));
        b.air(2, 4, -1, 3, 4, 3);
        for (int i = 0; i < 3; i++) {
            b.set(3, 1 + i, 3 - i, PlanBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, Half.BOTTOM));
            b.set(2, 1 + i, 3 - i, PlanBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, Half.BOTTOM));
        }
        b.set(2, 4, 0, PlanBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, Half.BOTTOM));
        b.set(3, 4, 0, PlanBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, Half.BOTTOM));
        // Tür und Fenster
        b.door(0, 1, -5, ModBlocks.CASTLE_DOOR.get(), Direction.SOUTH);
        b.set(-2, 2, -5, pane());
        b.set(2, 2, -5, pane());
        b.set(-4, 2, -2, pane());
        b.set(-4, 2, 1, pane());
        b.set(4, 2, -3, pane());
        window(b, -2, 5, -5, 2, Direction.NORTH, Direction.EAST, shutter);
        window(b, 2, 5, -5, 2, Direction.NORTH, Direction.EAST, shutter);
        window(b, -2, 5, 5, 2, Direction.SOUTH, Direction.EAST, shutter);
        window(b, -4, 5, 0, 2, Direction.WEST, Direction.SOUTH, shutter);
        // Dach
        int ridge = b.gableAlongZ(-4, 4, -5, 5, 8, roof.stairs().get(), roofBlock(roof), frame());
        b.set(0, 9, -5, pane());
        b.set(0, 10, -5, pane());
        // Kamin an der linken Wand
        b.fill(-3, 1, 2, -3, 1, 2, s(Blocks.BRICKS));
        b.set(-3, 1, 3, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        b.fill(-3, 2, 3, -3, 3, 3, s(Blocks.BRICKS));
        b.set(-3, 1, 4, s(Blocks.BRICKS));
        chimney(b, -3, 4, 2, ridge + 1);
        // Theke aus Fässern hinten rechts, Fässer an der Wand
        for (int x = -1; x <= 1; x++) {
            b.set(x, 1, 2, barrel(Direction.NORTH));
        }
        b.set(1, 1, 4, barrel(Direction.UP));
        b.set(0, 1, 4, barrel(Direction.UP));
        b.set(0, 2, 4, barrel(Direction.UP));
        b.set(-1, 1, 4, s(Blocks.SMOKER).setValue(FurnaceBlock.FACING, Direction.NORTH));
        b.chestAt(-2, 1, 4, Direction.NORTH);
        // Tische mit Bänken
        for (int[] t : new int[][] {{-2, -2}, {2, -2}}) {
            b.set(t[0], 1, t[1], ModBlocks.OAK_TABLE.get());
            b.set(t[0], 1, t[1] - 1, seat(ModBlocks.OAK_BENCH.get(), Direction.SOUTH));
            b.set(t[0], 1, t[1] + 1, seat(ModBlocks.OAK_BENCH.get(), Direction.NORTH));
        }
        b.set(-2, 2, -2, s(Blocks.CANDLE).setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        b.set(2, 2, -2, s(Blocks.CANDLE).setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        b.set(0, 3, -1, s(ModBlocks.CHANDELIER.get()));
        // Kammern oben
        b.bed(-2, 5, 2, Blocks.BED.red(), Direction.SOUTH);
        b.bed(-2, 5, -3, Blocks.BED.red(), Direction.NORTH);
        b.set(-3, 5, 0, barrel(Direction.UP));
        b.hangingLantern(0, 7, 0, Blocks.LANTERN);
        // Laterne am Eingang
        b.set(1, 3, -6, Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
        b.set(1, 4, -6, PlanBuilder.log(Blocks.SPRUCE_LOG, Direction.Axis.Z));
        step(b, 0, -6, Direction.SOUTH, ModBlocks.MEDIEVAL_STONE.stairs().get());
        b.workAt(0, 1, 1);
        b.entranceAt(0, 0, -7);
    }

    // =====================================================================================
    // Bürgerhaus
    // =====================================================================================

    /**
     * Bürgerhaus (4 Betten): schmales, hohes Fachwerkhaus 7 × 9, das Obergeschoss kragt vorn einen Block über, steiles
     * rotes Dach mit Gaube; unten Stube und Küche, oben vier Betten.
     */
    private static void townhouse(PlanBuilder b) {
        BuildingSet roof = ModBlocks.RED_ROOF_TILES;
        Block shutter = ModBlocks.GREEN_SHUTTERS.get();
        b.foundation(-3, -4, 3, 4, fieldstone());
        b.fill(-3, 0, -4, 3, 0, 4, stone());
        b.fill(-2, 0, -3, 2, 0, 3, planks());
        timberWallX(b, -3, 3, -4, 1, 4);
        timberWallX(b, -3, 3, 4, 1, 4);
        timberWallZ(b, -4, 4, -3, 1, 4);
        timberWallZ(b, -4, 4, 3, 1, 4);
        b.air(-2, 1, -3, 2, 4, 3);
        // Obergeschoss kragt vorn über (z = -5), getragen von Knaggen
        b.fill(-3, 5, -5, 3, 5, 4, planks());
        timberWallX(b, -3, 3, -5, 6, 9);
        timberWallX(b, -3, 3, 4, 6, 9);
        timberWallZ(b, -5, 4, -3, 6, 9);
        timberWallZ(b, -5, 4, 3, 6, 9);
        for (int x = -3; x <= 3; x++) {
            b.set(x, 6, -5, x == -3 || x == 3 ? beam(Direction.Axis.Y) : beam(Direction.Axis.X));
        }
        b.air(-2, 6, -4, 2, 9, 3);
        for (int x : new int[] {-3, 3}) {
            b.set(x, 4, -5, PlanBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.TOP));
        }
        b.set(0, 4, -5, PlanBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.TOP));
        // Tür, Fenster
        b.door(0, 1, -4, Blocks.SPRUCE_DOOR, Direction.SOUTH);
        b.set(0, 3, -4, cross());
        window(b, -2, 2, -4, 2, Direction.NORTH, Direction.EAST, shutter);
        window(b, 2, 2, -4, 2, Direction.NORTH, Direction.EAST, shutter);
        window(b, -2, 7, -5, 2, Direction.NORTH, Direction.EAST, shutter);
        window(b, 0, 7, -5, 2, Direction.NORTH, Direction.EAST, shutter);
        window(b, 2, 7, -5, 2, Direction.NORTH, Direction.EAST, shutter);
        window(b, -3, 2, 0, 2, Direction.WEST, Direction.SOUTH, shutter);
        window(b, 3, 2, 0, 2, Direction.EAST, Direction.SOUTH, shutter);
        window(b, -3, 7, 1, 2, Direction.WEST, Direction.SOUTH, shutter);
        window(b, 3, 7, -1, 2, Direction.EAST, Direction.SOUTH, shutter);
        window(b, 0, 7, 4, 2, Direction.SOUTH, Direction.EAST, shutter);
        // Treppe hinten links nach oben
        for (int i = 0; i < 4; i++) {
            b.set(-2, 1 + i, 3 - i, PlanBuilder.stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, Half.BOTTOM));
        }
        b.air(-2, 5, -1, -2, 5, 3);
        // Dach
        int ridge = b.gableAlongZ(-3, 3, -5, 4, 10, roof.stairs().get(), roofBlock(roof), frame());
        b.set(0, 11, -5, pane());
        chimney(b, 2, 3, 1, ridge);
        b.set(2, 1, 3, facing(Blocks.FURNACE, Direction.NORTH));
        // Stube
        b.set(1, 1, -1, ModBlocks.OAK_TABLE.get());
        b.set(1, 1, -2, seat(ModBlocks.OAK_CHAIR.get(), Direction.SOUTH));
        b.set(2, 1, -1, seat(ModBlocks.OAK_CHAIR.get(), Direction.WEST));
        b.set(1, 2, -1, s(Blocks.POTTED_BLUE_ORCHID));
        b.set(1, 1, 3, barrel(Direction.UP));
        b.hangingLantern(0, 4, 0, Blocks.LANTERN);
        // vier Betten oben
        b.bed(-1, 6, 2, Blocks.BED.yellow(), Direction.SOUTH);
        b.bed(0, 6, 2, Blocks.BED.yellow(), Direction.SOUTH);
        b.bed(1, 6, -3, Blocks.BED.lightBlue(), Direction.NORTH);
        b.bed(2, 6, -3, Blocks.BED.lightBlue(), Direction.NORTH);
        b.set(2, 6, 1, barrel(Direction.UP));
        b.hangingLantern(0, 9, 0, Blocks.LANTERN);
        step(b, 0, -5, Direction.SOUTH, ModBlocks.MEDIEVAL_STONE.stairs().get());
        b.workAt(0, 1, 0);
        b.entranceAt(0, 0, -6);
    }

    // =====================================================================================
    // Kaserne
    // =====================================================================================

    /**
     * Kaserne (acht Soldaten mehr): wehrhaftes Steinhaus 11 × 9 mit Zinnenkranz auf dem Flachdach, schmale Fenster,
     * innen Waffenständer, Tische, Kisten; davor ein Übungsplatz mit Strohpuppen und einer Zielscheibe.
     */
    private static void barracks(PlanBuilder b) {
        BlockState wall = s(ModBlocks.DARK_STONE_BRICKS.base().get());
        BlockState trim = s(ModBlocks.MEDIEVAL_STONE.base().get());
        Block battlement = ModBlocks.DARK_STONE_BRICK_BATTLEMENT.get();
        b.foundation(-6, -5, 6, 5, trim);
        b.fill(-6, 0, -5, 6, 0, 5, trim);
        b.fill(-4, 0, -3, 4, 0, 3, s(Blocks.SPRUCE_PLANKS));
        b.ring(-5, -4, 5, 4, 1, 5, wall);
        b.fill(-5, 1, -4, 5, 1, -4, trim);
        b.fill(-5, 1, 4, 5, 1, 4, trim);
        b.air(-4, 1, -3, 4, 5, 3);
        b.fill(-5, 6, -4, 5, 6, 4, wall);
        // Zinnenkranz auf dem Flachdach
        for (int x = -4; x <= 4; x++) {
            b.set(x, 7, -4, facing(battlement, Direction.NORTH));
            b.set(x, 7, 4, facing(battlement, Direction.SOUTH));
        }
        for (int z = -3; z <= 3; z++) {
            b.set(-5, 7, z, facing(battlement, Direction.WEST));
            b.set(5, 7, z, facing(battlement, Direction.EAST));
        }
        // vier Ecktürme, die über das Dach hinausragen
        for (int cx : new int[] {-5, 5}) {
            for (int cz : new int[] {-4, 4}) {
                for (int x = cx - 1; x <= cx + 1; x++) {
                    for (int z = cz - 1; z <= cz + 1; z++) {
                        for (int y = 0; y <= 8; y++) {
                            b.set(x, y, z, y == 0 ? trim : wall);
                        }
                    }
                }
                b.set(cx, 3, cz + (cz < 0 ? -1 : 1), s(Blocks.IRON_BARS));
                b.set(cx + (cx < 0 ? -1 : 1), 3, cz, s(Blocks.IRON_BARS));
                b.set(cx + (cx < 0 ? -1 : 1), 6, cz, s(Blocks.IRON_BARS));
                for (int x = cx - 1; x <= cx + 1; x++) {
                    b.set(x, 9, cz - 1, facing(battlement, Direction.NORTH));
                    b.set(x, 9, cz + 1, facing(battlement, Direction.SOUTH));
                }
                b.set(cx - 1, 9, cz, facing(battlement, Direction.WEST));
                b.set(cx + 1, 9, cz, facing(battlement, Direction.EAST));
                b.set(cx, 9, cz, s(Blocks.AIR));
            }
        }
        // Torbogen mit zwei Laternen
        b.door(0, 1, -4, ModBlocks.CASTLE_DOOR.get(), Direction.SOUTH);
        b.set(-1, 3, -5, PlanBuilder.stairs(ModBlocks.MEDIEVAL_STONE.stairs().get(), Direction.WEST, Half.TOP));
        b.set(1, 3, -5, PlanBuilder.stairs(ModBlocks.MEDIEVAL_STONE.stairs().get(), Direction.EAST, Half.TOP));
        b.set(0, 3, -5, s(Blocks.CHISELED_STONE_BRICKS));
        b.set(0, 3, -4, trim);
        b.set(-2, 2, -5, ModBlocks.STEEL_LANTERN.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.LanternBlock.HANGING, false));
        b.set(2, 2, -5, ModBlocks.STEEL_LANTERN.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.LanternBlock.HANGING, false));
        b.set(-2, 1, -5, PlanBuilder.slab(ModBlocks.MEDIEVAL_STONE.slab().get(), SlabType.TOP));
        b.set(2, 1, -5, PlanBuilder.slab(ModBlocks.MEDIEVAL_STONE.slab().get(), SlabType.TOP));
        b.set(0, 5, -5, Blocks.WALL_BANNER.red().defaultBlockState()
                .setValue(net.minecraft.world.level.block.WallBannerBlock.FACING, Direction.NORTH));
        // Schießscharten
        for (int x : new int[] {-3, 3}) {
            b.set(x, 2, -4, s(Blocks.IRON_BARS));
            b.set(x, 3, -4, s(Blocks.IRON_BARS));
            b.set(x, 3, 4, s(Blocks.IRON_BARS));
        }
        for (int z : new int[] {-1, 1}) {
            b.set(-5, 3, z, s(Blocks.IRON_BARS));
            b.set(5, 3, z, s(Blocks.IRON_BARS));
        }
        // Fahnenmast auf dem Dach
        for (int y = 7; y <= 10; y++) {
            b.set(0, y, 0, s(Blocks.SPRUCE_FENCE));
        }
        b.set(0, 11, 0, Blocks.BANNER.red().defaultBlockState());
        // innen: Pritschen, Speerständer, Tische, Banner
        for (int x : new int[] {-4, -2}) {
            b.set(x, 1, 2, Blocks.BED.red().defaultBlockState().setValue(net.minecraft.world.level.block.BedBlock.FACING, Direction.SOUTH)
                    .setValue(net.minecraft.world.level.block.BedBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.FOOT));
            b.set(x, 1, 3, Blocks.BED.red().defaultBlockState().setValue(net.minecraft.world.level.block.BedBlock.FACING, Direction.SOUTH)
                    .setValue(net.minecraft.world.level.block.BedBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.HEAD));
        }
        b.set(-3, 1, 3, barrel(Direction.UP));
        for (int z = -2; z <= 0; z++) {
            b.set(4, 1, z, PlanBuilder.slab(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
            b.set(4, 2, z, s(Blocks.LIGHTNING_ROD.weathering().unaffected()));
        }
        b.chestAt(4, 1, 3, Direction.WEST);
        b.set(3, 1, 3, s(Blocks.SMITHING_TABLE));
        b.set(-1, 1, -1, ModBlocks.OAK_TABLE.get());
        b.set(0, 1, -1, ModBlocks.OAK_TABLE.get());
        b.set(-1, 1, -2, seat(ModBlocks.OAK_BENCH.get(), Direction.SOUTH));
        b.set(0, 1, -2, seat(ModBlocks.OAK_BENCH.get(), Direction.SOUTH));
        b.set(-1, 1, 0, seat(ModBlocks.OAK_BENCH.get(), Direction.NORTH));
        b.set(0, 1, 0, seat(ModBlocks.OAK_BENCH.get(), Direction.NORTH));
        b.set(-4, 4, 0, Blocks.WALL_BANNER.red().defaultBlockState()
                .setValue(net.minecraft.world.level.block.WallBannerBlock.FACING, Direction.EAST));
        b.set(4, 4, 2, Blocks.WALL_BANNER.red().defaultBlockState()
                .setValue(net.minecraft.world.level.block.WallBannerBlock.FACING, Direction.WEST));
        b.set(0, 5, 1, s(ModBlocks.CHANDELIER.get()));
        b.hangingLantern(-3, 5, -2, ModBlocks.STEEL_LANTERN.get());
        b.hangingLantern(3, 5, 2, ModBlocks.STEEL_LANTERN.get());
        // Übungsplatz: zwei Strohpuppen und eine Zielscheibe
        for (int x : new int[] {-4, -1}) {
            b.set(x, 0, -8, s(Blocks.SPRUCE_FENCE));
            b.set(x, 1, -8, s(Blocks.HAY_BLOCK));
            b.set(x - 1, 1, -8, s(Blocks.SPRUCE_FENCE));
            b.set(x + 1, 1, -8, s(Blocks.SPRUCE_FENCE));
            b.set(x, 2, -8, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, Direction.SOUTH));
        }
        b.set(3, 0, -8, s(Blocks.SPRUCE_FENCE));
        b.set(3, 1, -8, s(Blocks.TARGET));
        step(b, 0, -5, Direction.SOUTH, ModBlocks.DARK_STONE_BRICKS.stairs().get());
        b.workAt(0, 1, -1);
        b.entranceAt(0, 0, -6);
    }

    // =====================================================================================
    // Kartenhaus
    // =====================================================================================

    /**
     * Kartenhaus (Kartograf, 1 Bett): schlanker Turm mit Fachwerk-Obergeschoss und kupfernem Zeltdach; unten Kartentisch,
     * Lesepult und Bücher, oben die Kammer des Kartografen und ein Fernrohr-Erker.
     */
    private static void mapHouse(PlanBuilder b) {
        BuildingSet roof = ModBlocks.COPPER_ROOF;
        b.foundation(-3, -3, 3, 3, fieldstone());
        b.fill(-3, 0, -3, 3, 0, 3, stone());
        b.fill(-2, 0, -2, 2, 0, 2, s(Blocks.DARK_OAK_PLANKS));
        b.ring(-3, -3, 3, 3, 1, 4, s(ModBlocks.LIMESTONE_BRICKS.base().get()));
        b.air(-2, 1, -2, 2, 4, 2);
        b.fill(-2, 5, -2, 2, 5, 2, s(Blocks.DARK_OAK_PLANKS));
        timberWallX(b, -3, 3, -3, 5, 8);
        timberWallX(b, -3, 3, 3, 5, 8);
        timberWallZ(b, -3, 3, -3, 5, 8);
        timberWallZ(b, -3, 3, 3, 5, 8);
        b.air(-2, 6, -2, 2, 8, 2);
        b.door(0, 1, -3, Blocks.DARK_OAK_DOOR, Direction.SOUTH);
        b.set(-2, 2, -3, pane());
        b.set(2, 2, -3, pane());
        b.set(-3, 2, 0, pane());
        b.set(3, 2, 0, pane());
        b.set(0, 2, 3, pane());
        window(b, 0, 6, -3, 2, Direction.NORTH, Direction.EAST, ModBlocks.GREEN_SHUTTERS.get());
        window(b, -3, 6, 0, 2, Direction.WEST, Direction.SOUTH, ModBlocks.GREEN_SHUTTERS.get());
        window(b, 3, 6, 0, 2, Direction.EAST, Direction.SOUTH, ModBlocks.GREEN_SHUTTERS.get());
        int top = b.hipRoof(-4, -4, 4, 4, 9, roof.stairs().get(), roofBlock(roof));
        b.set(0, top + 1, 0, s(Blocks.LIGHTNING_ROD.waxed().oxidized()));
        // Leiter nach oben
        for (int y = 1; y <= 5; y++) {
            b.set(-2, y, 2, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
        }
        // unten: Kartentisch, Lesepult, Bücher
        b.set(0, 1, 1, s(Blocks.CARTOGRAPHY_TABLE));
        b.set(2, 1, 1, s(Blocks.LECTERN).setValue(net.minecraft.world.level.block.LecternBlock.FACING, Direction.WEST));
        b.set(2, 1, -1, s(Blocks.BOOKSHELF));
        b.set(2, 2, -1, s(Blocks.BOOKSHELF));
        b.set(-2, 1, -1, s(Blocks.CHISELED_BOOKSHELF));
        b.chestAt(-2, 1, 0, Direction.EAST);
        b.hangingLantern(0, 4, 0, Blocks.LANTERN);
        // oben: Kammer
        b.bed(1, 6, 1, Blocks.BED.cyan(), Direction.SOUTH);
        b.set(-1, 6, -2, ModBlocks.OAK_TABLE.get());
        b.set(-1, 7, -2, s(Blocks.POTTED_FERN));
        b.hangingLantern(0, 8, 0, Blocks.LANTERN);
        step(b, 0, -4, Direction.SOUTH, ModBlocks.LIMESTONE_BRICKS.stairs().get());
        b.workAt(0, 1, 0);
        b.entranceAt(0, 0, -5);
    }

    // =====================================================================================
    // Thronsaal
    // =====================================================================================

    /**
     * Thronsaal (Ort der Krönung): Halle 13 × 21 aus hellem Kalkstein mit Strebepfeilern und hohen Bleiglasfenstern,
     * Säulen und Kronleuchtern, ein roter Teppich führt vom Burgtor zum Thron auf einem dreistufigen Podest; Banner an den
     * Wänden, ein steiles Schieferdach mit Zinnen am Giebel.
     */
    private static void throneHall(PlanBuilder b) {
        BuildingSet roof = ModBlocks.DARK_SLATE_SHINGLES;
        BlockState wall = s(ModBlocks.LIMESTONE_BRICKS.base().get());
        b.foundation(-6, -10, 6, 10, stone());
        b.fill(-6, 0, -10, 6, 0, 10, stone());
        b.fill(-5, 0, -9, 5, 0, 9, s(Blocks.POLISHED_DIORITE));
        b.ring(-6, -10, 6, 10, 1, 8, wall);
        b.air(-5, 1, -9, 5, 8, 9);
        // Strebepfeiler und hohe Fenster an den Langseiten
        for (int z = -8; z <= 8; z += 4) {
            for (int x : new int[] {-7, 7}) {
                for (int y = 1; y <= 7; y++) {
                    b.set(x, y, z, s(ModBlocks.MEDIEVAL_STONE.base().get()));
                }
                b.set(x, 8, z, PlanBuilder.stairs(ModBlocks.MEDIEVAL_STONE.stairs().get(), x < 0 ? Direction.EAST : Direction.WEST, Half.BOTTOM));
            }
        }
        for (int z = -6; z <= 6; z += 4) {
            for (int x : new int[] {-6, 6}) {
                for (int y = 3; y <= 6; y++) {
                    b.set(x, y, z, s(ModBlocks.LEADED_GLASS.get()));
                    b.set(x, y, z + 1, s(ModBlocks.LEADED_GLASS.get()));
                }
            }
        }
        // Burgtor mit Rundbogen-Rahmen
        b.door(-1, 1, -10, ModBlocks.CASTLE_DOOR.get(), Direction.SOUTH);
        b.door(0, 1, -10, ModBlocks.CASTLE_DOOR.get(), Direction.SOUTH);
        b.door(1, 1, -10, ModBlocks.CASTLE_DOOR.get(), Direction.SOUTH);
        b.fill(-2, 3, -10, 2, 3, -10, s(Blocks.CHISELED_STONE_BRICKS));
        b.set(0, 4, -10, s(Blocks.CHISELED_STONE_BRICKS));
        b.set(0, 6, -10, s(ModBlocks.LEADED_GLASS.get()));
        b.set(-1, 6, -10, s(ModBlocks.LEADED_GLASS.get()));
        b.set(1, 6, -10, s(ModBlocks.LEADED_GLASS.get()));
        b.set(0, 7, -10, s(ModBlocks.LEADED_GLASS.get()));
        // Säulen innen
        for (int z = -6; z <= 6; z += 4) {
            for (int x : new int[] {-3, 3}) {
                for (int y = 1; y <= 7; y++) {
                    b.set(x, y, z, s(ModBlocks.STONE_PILLAR.get()));
                }
                b.set(x, 8, z, s(Blocks.CHISELED_STONE_BRICKS));
            }
        }
        // Dach (steil) mit Steingiebeln
        int ridge = b.gableAlongZ(-6, 6, -10, 10, 9, roof.stairs().get(), roofBlock(roof), wall);
        for (int y = 10; y <= ridge; y++) {
            b.set(0, y, -11, wall);
        }
        b.set(0, ridge + 1, -11, facing(ModBlocks.LIMESTONE_BRICK_BATTLEMENT.get(), Direction.NORTH));
        // roter Teppich, Podest und Thron
        for (int z = -9; z <= 5; z++) {
            for (int x = -1; x <= 1; x++) {
                b.set(x, 1, z, x == 0 ? Blocks.CARPET.red() : Blocks.CARPET.red());
            }
        }
        for (int x = -3; x <= 3; x++) {
            b.set(x, 1, 6, PlanBuilder.stairs(Blocks.POLISHED_DIORITE_STAIRS, Direction.SOUTH, Half.BOTTOM));
            b.fill(x, 1, 7, x, 1, 9, s(Blocks.POLISHED_DIORITE));
        }
        for (int x = -2; x <= 2; x++) {
            b.set(x, 2, 7, PlanBuilder.stairs(Blocks.POLISHED_DIORITE_STAIRS, Direction.SOUTH, Half.BOTTOM));
            b.fill(x, 2, 8, x, 2, 9, s(Blocks.POLISHED_DIORITE));
        }
        b.fill(-1, 3, 8, 1, 3, 9, s(Blocks.GOLD_BLOCK));
        b.set(0, 4, 8, seat(ModBlocks.THRONE.get(), Direction.NORTH));
        b.set(0, 3, 7, Blocks.CARPET.red());
        b.set(-1, 3, 7, Blocks.CARPET.red());
        b.set(1, 3, 7, Blocks.CARPET.red());
        // Banner hinter dem Thron und an den Wänden
        for (int x : new int[] {-2, 2}) {
            b.set(x, 6, 9, Blocks.WALL_BANNER.red().defaultBlockState()
                    .setValue(net.minecraft.world.level.block.WallBannerBlock.FACING, Direction.NORTH));
        }
        b.set(0, 7, 9, Blocks.WALL_BANNER.yellow().defaultBlockState()
                .setValue(net.minecraft.world.level.block.WallBannerBlock.FACING, Direction.NORTH));
        for (int z = -8; z <= 8; z += 4) {
            b.set(-5, 6, z, Blocks.WALL_BANNER.blue().defaultBlockState()
                    .setValue(net.minecraft.world.level.block.WallBannerBlock.FACING, Direction.EAST));
            b.set(5, 6, z, Blocks.WALL_BANNER.blue().defaultBlockState()
                    .setValue(net.minecraft.world.level.block.WallBannerBlock.FACING, Direction.WEST));
        }
        // Kronleuchter
        for (int z = -6; z <= 6; z += 6) {
            b.set(0, 8, z, s(ModBlocks.CHANDELIER.get()));
        }
        // Treppe vor dem Tor
        for (int x = -2; x <= 2; x++) {
            b.set(x, 0, -11, PlanBuilder.stairs(ModBlocks.LIMESTONE_BRICKS.stairs().get(), Direction.SOUTH, Half.BOTTOM));
        }
        b.workAt(0, 4, 7);
        b.mark(0, 4, 8);
        b.entranceAt(0, 0, -12);
    }
}
