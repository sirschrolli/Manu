package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Stille Kathedrale: Tief unter den Bergen, im Reich des Sculk, steht in einer gewaltigen Höhle eine Kathedrale aus
 * Tiefenschiefer – ein Volk, das die Stille anbetete, hat sie erbaut. Zwei Türme mit spitzen Helmen, dazwischen ein Portal
 * mit Zwillingstüren und darüber eine Fensterrose aus blauem und schwarzem Glas. Innen ein hohes Mittelschiff unter einem
 * Spitztonnengewölbe, Seitenschiffe mit Gräbern, ein erhöhter Chor mit dem Altar, auf dem der Sculk-Katalysator ruht, und
 * in der Apsis die große Orgel aus grün angelaufenen Kupferpfeifen. An ihr steht Sordin, der Kantor der Stille (seit 1.7), und
 * spielt, ohne dass ein Ton erklingt. Wer auf ihr spielt (oder in der Nähe Lärm macht), weckt, was in der Stille wohnt. Außen tragen Strebebögen das Gewölbe, auf den Fialen brennen Seelenlaternen.
 *
 * <p>Eigene Koordinaten: Mitte des Mittelschiffs bei (0, 0, 0); y = 0 ist die Schicht des Fußbodens (man steht auf y = 1).
 * Das Portal liegt im Süden (+z), die Apsis im Norden (−z).
 */
final class SilentCathedral {
    /** Halbachsen und Mitte der Höhle. */
    static final int RX = 26;
    static final int RY = 30;
    static final int RZ = 38;
    static final int CY = 10;
    static final int CZ = 2;
    /** Mitte des Halbkreises der Apsis. */
    static final int APSE_Z = -17;

    private SilentCathedral() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int floor = -43 + Math.floorMod((int) (seed >>> 13), 10);
        if (floor - 6 < terrain.minY()) {
            return Optional.empty();
        }
        // die Höhle (40 hoch) muss gut unter der Erde bleiben
        int[] span = terrain.span(x, z, 32, 16);
        if (span[0] < floor + CY + RY + 8) {
            return Optional.empty();
        }
        // und sie liegt im Reich des Sculk
        int dark = 0;
        for (int[] d : new int[][] {{0, 0}, {16, 0}, {-16, 0}, {0, 20}, {0, -20}}) {
            if (terrain.biome(x + d[0], floor + 8, z + d[1]).is(Biomes.DEEP_DARK)) {
                dark++;
            }
        }
        if (dark < 4) {
            return Optional.empty();
        }
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 7), 4)];
        BlockPos origin = new BlockPos(x, floor, z);
        BoundingBox box = box(origin, rotation);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.SILENT_CATHEDRAL, 0, origin, rotation, seed, new int[0], box))));
    }

    /** Die ganze Höhle, gedreht. */
    static BoundingBox box(BlockPos origin, Rotation rotation) {
        BlockPos a = new BlockPos(-RX - 3, 0, CZ - RZ - 3).rotate(rotation);
        BlockPos c = new BlockPos(RX + 3, 0, CZ + RZ + 3).rotate(rotation);
        return new BoundingBox(origin.getX() + Math.min(a.getX(), c.getX()), origin.getY() - 5, origin.getZ() + Math.min(a.getZ(), c.getZ()),
                origin.getX() + Math.max(a.getX(), c.getX()), origin.getY() + CY + RY + 5, origin.getZ() + Math.max(a.getZ(), c.getZ()));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Cathedral(b).build();
    }

    private static final class Cathedral {
        private static final int[] PILLARS = {14, 10, 6, 2, -2, -6, -10, -14};
        private final VillageBuilder b;

        Cathedral(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            this.cavern();
            this.floors();
            this.aisles();
            this.arcade();
            this.vault();
            this.roof();
            this.apse();
            this.buttresses();
            this.facade();
            this.towers();
            this.furnish();
            this.organ();
            this.plaza();
            this.sculk();
        }

        // =================================================================================
        // Hilfen
        // =================================================================================

        boolean column(int x, int z) {
            return SiteCave.column(this.b, x, z);
        }

        double smooth(int x, int y, int z, int salt) {
            return SiteCave.smooth(this.b, x, y, z, salt, 7);
        }

        BlockState brick(int x, int y, int z) {
            float n = this.b.noise(x, y, z);
            return n < 0.72f ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : n < 0.86f ? Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState()
                    : Blocks.DEEPSLATE_TILES.defaultBlockState();
        }

        static BlockState state(Block block) {
            return block.defaultBlockState();
        }

        static BlockState stair(Block block, Direction facing, Half half) {
            return VillageBuilder.stairs(block, facing, half);
        }

        /** Richtung von der Mitte weg (für x > 0 Osten). */
        static Direction out(int x) {
            return x > 0 ? Direction.EAST : Direction.WEST;
        }

        static boolean pillar(int z) {
            return Math.floorMod(z - 2, 4) == 0;
        }

        static double apseR(int x, int z) {
            return Math.sqrt(x * x + (z - APSE_Z) * (double) (z - APSE_Z));
        }

        /** Oberste Schicht des Fußbodens in einer Säule des Gebäudes (Chor und Apsis liegen eine Stufe höher). */
        static int floorTop(int x, int z) {
            if (z < APSE_Z && apseR(x, z) < 6.5 || Math.abs(x) <= 5 && z <= -10 && z >= APSE_Z) {
                return 1;
            }
            return 0;
        }

        void candle(int x, int y, int z, Block candle, int count) {
            this.b.set(x, y, z, candle.defaultBlockState().setValue(CandleBlock.CANDLES, count).setValue(CandleBlock.LIT, true));
        }

        void hanging(int x, int y, int z, int chainTop) {
            for (int c = y + 1; c <= chainTop; c++) {
                this.b.set(x, c, z, Blocks.IRON_CHAIN);
            }
            this.b.set(x, y, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }

        void shrieker(int x, int y, int z) {
            this.b.set(x, y, z, Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON, true));
        }

        // =================================================================================
        // Die Höhle
        // =================================================================================

        /** Die Höhle: eine weite Kuppel im Fels, unten ein ebener Grund; Wasser und Lava am Rand werden zu Fels. */
        void cavern() {
            SiteCave.dome(this.b, RX, RY, RZ, CY, CZ, 0.08, this::ground);
        }

        BlockState ground(int x, int z) {
            double s = this.smooth(x, 0, z, 2);
            float n = this.b.noise(x, 0, z);
            if (s < 0.36) {
                return n < 0.2f ? Blocks.CALCITE.defaultBlockState() : Blocks.TUFF.defaultBlockState();
            }
            return n < 0.22f ? Blocks.COBBLED_DEEPSLATE.defaultBlockState() : n < 0.28f ? Blocks.GRAVEL.defaultBlockState()
                    : Blocks.DEEPSLATE.defaultBlockState();
        }

        // =================================================================================
        // Der Bau
        // =================================================================================

        /** Die Fußböden: im Mittelschiff Fliesen mit einem Läufer, im Chor eine Stufe höher, in den Seitenschiffen Ziegel. */
        void floors() {
            for (int x = -13; x <= 13; x++) {
                for (int z = -25; z <= 23; z++) {
                    int ax = Math.abs(x);
                    boolean apse = z < APSE_Z && apseR(x, z) < 7.6;
                    boolean body = ax <= 11 && z >= -18 && z <= 19;
                    boolean tower = ax >= 7 && ax <= 13 && z >= 17 && z <= 23;
                    if (!apse && !body && !tower) {
                        continue;
                    }
                    BlockState floor;
                    if (tower) {
                        floor = state(Blocks.POLISHED_DEEPSLATE);
                    } else if (ax <= 5 || apse) {
                        floor = ax <= 1 ? (Math.floorMod(z, 4) == 0 ? state(Blocks.CHISELED_DEEPSLATE) : state(Blocks.POLISHED_DEEPSLATE))
                                : ax == 5 ? state(Blocks.POLISHED_DEEPSLATE) : state(Blocks.DEEPSLATE_TILES);
                    } else {
                        floor = this.b.chance(x, 0, z, 0.25f) ? state(Blocks.CRACKED_DEEPSLATE_BRICKS) : state(Blocks.DEEPSLATE_BRICKS);
                    }
                    this.b.set(x, 0, z, floor);
                    if (floorTop(x, z) == 1) {
                        this.b.set(x, 0, z, state(Blocks.DEEPSLATE_TILES));
                        this.b.set(x, 1, z, ax == 5 || apse && apseR(x, z) > 5.6 ? state(Blocks.POLISHED_DEEPSLATE)
                                : Math.floorMod(x + z, 2) == 0 ? state(Blocks.POLISHED_DEEPSLATE) : state(Blocks.DEEPSLATE_TILES));
                    }
                }
            }
            // die Stufe zum Chor
            for (int x = -4; x <= 4; x++) {
                this.b.set(x, 1, -9, stair(Blocks.POLISHED_DEEPSLATE_STAIRS, Direction.NORTH, Half.BOTTOM));
            }
        }

        /** Die Seitenschiffe: Außenmauern mit Fenstern, flache Decke mit Gurtbögen, darüber ein Dach mit Zinnen. */
        void aisles() {
            for (int side = -1; side <= 1; side += 2) {
                // Außenmauer
                for (int z = -18; z <= 16; z++) {
                    for (int y = 1; y <= 9; y++) {
                        boolean window = Math.floorMod(z, 4) == 0 && z >= -16 && z <= 12 && y >= 3 && y <= 5;
                        this.b.set(side * 11, y, z, window ? Blocks.STAINED_GLASS_PANE.pick(y == 5 ? DyeColor.BLACK : DyeColor.CYAN)
                                .defaultBlockState() : this.brick(side * 11, y, z));
                    }
                    this.b.set(side * 11, 2, z, Math.floorMod(z, 4) == 0 && z >= -16 && z <= 12 ? state(Blocks.POLISHED_DEEPSLATE)
                            : this.brick(side * 11, 2, z));
                }
                // Stirnwand im Norden
                for (int x = 6; x <= 11; x++) {
                    for (int y = 1; y <= 9; y++) {
                        this.b.set(side * x, y, -18, this.brick(side * x, y, -18));
                    }
                }
                // Decke mit Gurtbögen an jedem Pfeiler, Dach, Brüstung
                for (int z = -17; z <= 16; z++) {
                    for (int x = 7; x <= 10; x++) {
                        this.b.set(side * x, 8, z, pillar(z) ? (x == 7 || x == 10
                                ? stair(Blocks.POLISHED_DEEPSLATE_STAIRS, x == 7 ? (side > 0 ? Direction.WEST : Direction.EAST) : out(side), Half.TOP)
                                : VillageBuilder.slab(Blocks.POLISHED_DEEPSLATE_SLAB, SlabType.TOP)) : Blocks.AIR.defaultBlockState());
                        this.b.set(side * x, 9, z, state(Blocks.DEEPSLATE_TILES));
                    }
                    for (int x = 7; x <= 11; x++) {
                        this.b.set(side * x, 10, z, x == 11 ? this.brick(side * x, 10, z) : state(Blocks.DEEPSLATE_TILES));
                    }
                    this.b.set(side * 11, 11, z, state(Blocks.DEEPSLATE_BRICK_WALL));
                    if (Math.floorMod(z, 2) == 0) {
                        this.b.set(side * 11, 12, z, state(Blocks.DEEPSLATE_BRICK_WALL));
                    }
                }
                // Sakristei und Gruft: Zwischenwand mit Tür
                for (int x = 7; x <= 10; x++) {
                    for (int y = 1; y <= 7; y++) {
                        this.b.set(side * x, y, -11, this.brick(side * x, y, -11));
                    }
                }
                this.b.door(side * 8, 1, -11, Blocks.DARK_OAK_DOOR, Direction.NORTH);
            }
        }

        /**
         * Die Arkaden zwischen Mittel- und Seitenschiffen: Pfeiler mit Kapitellen, dazwischen Spitzbögen; darüber der Obergaden
         * mit hohen Fenstern. Am Chor sind die Bögen zugemauert (dahinter liegen Sakristei und Gruft).
         */
        void arcade() {
            for (int side = -1; side <= 1; side += 2) {
                int x = side * 6;
                for (int z = -17; z <= 17; z++) {
                    int off = Math.floorMod(z - 2, 4);
                    boolean closed = z <= -11;
                    for (int y = 1; y <= 19; y++) {
                        BlockState s;
                        if (off == 0) {
                            s = y == 1 ? state(Blocks.CHISELED_DEEPSLATE) : y == 7 ? state(Blocks.CHISELED_DEEPSLATE)
                                    : y <= 7 ? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : this.brick(x, y, z);
                        } else if (y <= 7) {
                            boolean open = off == 2 ? y <= 6 : y <= 5;
                            if (off != 2 && y == 6) {
                                s = stair(Blocks.POLISHED_DEEPSLATE_STAIRS, off == 1 ? Direction.NORTH : Direction.SOUTH, Half.TOP);
                            } else if (open) {
                                s = closed ? state(Blocks.DEEPSLATE_TILES) : Blocks.AIR.defaultBlockState();
                            } else {
                                s = this.brick(x, y, z);
                            }
                        } else {
                            boolean window = off == 2 && y >= 9 && y <= 12;
                            s = window ? Blocks.STAINED_GLASS_PANE.pick(y == 12 ? DyeColor.BLACK : y == 11 ? DyeColor.LIGHT_BLUE : DyeColor.CYAN)
                                    .defaultBlockState() : this.brick(x, y, z);
                        }
                        this.b.set(x, y, z, s);
                    }
                    // die Dienste: schlanke Säulen vom Boden bis in die Gewölberippen
                    if (off == 0) {
                        for (int y = 1 + floorTop(side * 5, z); y <= 13; y++) {
                            this.b.set(side * 5, y, z, state(Blocks.POLISHED_DEEPSLATE_WALL));
                        }
                    }
                }
            }
        }

        /** Gewölbehöhe des Mittelschiffs (erste feste Schicht) über |x|. */
        static int vaultAt(int ax) {
            return switch (ax) {
                case 5 -> 14;
                case 4 -> 15;
                case 3 -> 16;
                case 2 -> 17;
                default -> 18;
            };
        }

        /** Das Spitztonnengewölbe des Mittelschiffs mit Rippen an jedem Pfeiler und Schlusssteinen. */
        void vault() {
            for (int z = -17; z <= 17; z++) {
                boolean rib = pillar(z);
                for (int x = -5; x <= 5; x++) {
                    int ax = Math.abs(x);
                    int v = vaultAt(ax);
                    for (int y = v; y <= 19; y++) {
                        this.b.set(x, y, z, rib ? (x == 0 && y == v ? state(Blocks.CHISELED_DEEPSLATE) : state(Blocks.POLISHED_DEEPSLATE))
                                : state(Blocks.DEEPSLATE_TILES));
                    }
                    if (ax >= 1) {
                        this.b.set(x, v - 1, z, stair(rib ? Blocks.POLISHED_DEEPSLATE_STAIRS : Blocks.DEEPSLATE_TILE_STAIRS, out(x), Half.TOP));
                    }
                }
            }
        }

        /** Das steile Dach über dem Mittelschiff (Traufe über den Obergaden, First in der Mitte). */
        void roof() {
            for (int z = -17; z <= 19; z++) {
                for (int x = -7; x <= 7; x++) {
                    int k = Math.abs(x);
                    int top = 26 - k;
                    for (int y = 19; y < top; y++) {
                        if (k <= 6) {
                            this.b.set(x, y, z, state(Blocks.DEEPSLATE_TILES));
                        }
                    }
                    this.b.set(x, top, z, k == 0 ? state(Blocks.DEEPSLATE_TILES)
                            : stair(Blocks.DEEPSLATE_TILE_STAIRS, x > 0 ? Direction.WEST : Direction.EAST, Half.BOTTOM));
                    if (k == 0) {
                        this.b.set(x, top + 1, z, VillageBuilder.slab(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM));
                    }
                }
            }
        }

        /**
         * Die Apsis: ein Halbrund mit hohen Fenstern, darüber eine Halbkuppel und ein kegelförmiges Dach.
         */
        void apse() {
            for (int x = -8; x <= 8; x++) {
                for (int z = -26; z <= -18; z++) {
                    double r = apseR(x, z);
                    if (r >= 8.4) {
                        continue;
                    }
                    double angle = Math.toDegrees(Math.atan2(-(z - APSE_Z), x));
                    for (int y = 1; y <= 27; y++) {
                        boolean interior = r < 6.5 && (y <= 13 || x * x + (z - APSE_Z) * (z - APSE_Z) + Math.pow((y - 13) * 1.4, 2) < 42.25);
                        double coneTop = 26 - r * (7.0 / 7.6);
                        if (r < 7.6 && y >= 1 && y <= 13 && r >= 6.5) {
                            boolean window = y >= 3 && y <= 10 && nearWindow(angle);
                            this.b.set(x, y, z, window ? Blocks.STAINED_GLASS_PANE.pick(y % 3 == 0 ? DyeColor.BLACK : DyeColor.CYAN)
                                    .defaultBlockState() : this.brick(x, y, z));
                        } else if (interior) {
                            if (y > floorTop(x, z)) {
                                this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                            }
                        } else if (r < 7.6 && y >= 14 && y < Math.round(coneTop)) {
                            this.b.set(x, y, z, state(Blocks.DEEPSLATE_TILES));
                        } else if (y == Math.round(coneTop) && r < 8.4) {
                            Direction in = Math.abs(x) > Math.abs(z - APSE_Z) ? (x > 0 ? Direction.WEST : Direction.EAST) : Direction.SOUTH;
                            this.b.set(x, y, z, stair(Blocks.DEEPSLATE_TILE_STAIRS, in, Half.BOTTOM));
                        }
                    }
                }
            }
        }

        static boolean nearWindow(double angle) {
            for (double w : new double[] {32, 61, 90, 119, 148}) {
                if (Math.abs(angle - w) < 7) {
                    return true;
                }
            }
            return false;
        }

        /** Strebepfeiler an jedem Joch, Strebebögen hinauf zum Obergaden, Fialen mit Seelenlaternen. */
        void buttresses() {
            for (int side = -1; side <= 1; side += 2) {
                for (int z : PILLARS) {
                    // der Pfeiler überragt das Seitenschiff, oben eine Fiale mit Laterne
                    for (int y = 1; y <= 12; y++) {
                        this.b.set(side * 12, y, z, this.brick(side * 12, y, z));
                    }
                    this.b.set(side * 13, 1, z, stair(Blocks.DEEPSLATE_BRICK_STAIRS, side > 0 ? Direction.WEST : Direction.EAST, Half.BOTTOM));
                    this.b.set(side * 13, 2, z, stair(Blocks.DEEPSLATE_BRICK_STAIRS, side > 0 ? Direction.WEST : Direction.EAST, Half.BOTTOM));
                    this.b.set(side * 12, 13, z, state(Blocks.POLISHED_DEEPSLATE_WALL));
                    this.b.set(side * 12, 14, z, state(Blocks.POLISHED_DEEPSLATE_WALL));
                    this.b.set(side * 12, 15, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
                    // der Strebebogen: vom Pfeiler schräg hinauf zum Obergaden, frei über dem Dach des Seitenschiffs
                    for (int lx = 11; lx >= 7; lx--) {
                        int x = side * lx;
                        int y = 12 + (int) Math.round((11 - lx) * 0.75);
                        this.b.set(x, y, z, state(Blocks.DEEPSLATE_BRICKS));
                        if (this.b.get(x, y - 1, z).isAir()) {
                            this.b.set(x, y - 1, z, stair(Blocks.DEEPSLATE_BRICK_STAIRS, out(side), Half.TOP));
                        }
                    }
                }
            }
        }

        /**
         * Die Westfassade (hier im Süden): der Giebel des Mittelschiffs mit einem tiefen Spitzbogenportal (zwei Türen,
         * dazwischen ein Mittelpfeiler mit einer Wächterfigur) und darüber die große Fensterrose.
         */
        void facade() {
            for (int x = -6; x <= 6; x++) {
                int ax = Math.abs(x);
                int top = 27 - ax;
                for (int y = 1; y <= top; y++) {
                    for (int z = 18; z <= 19; z++) {
                        this.b.set(x, y, z, y == top ? state(Blocks.POLISHED_DEEPSLATE) : this.brick(x, y, z));
                    }
                }
                // äußerer Bogen (sieben breit, spitz), innen das Türgewände
                for (int y = 1; y <= 8; y++) {
                    if (y <= 7 - ax && ax <= 3) {
                        this.b.set(x, y, 19, Blocks.AIR.defaultBlockState());
                    } else if (ax <= 3 && y == 8 - ax) {
                        this.b.set(x, y, 19, ax == 0 ? state(Blocks.CHISELED_DEEPSLATE)
                                : stair(Blocks.POLISHED_DEEPSLATE_STAIRS, out(x), Half.TOP));
                    }
                }
            }
            for (int y = 4; y <= 6; y++) {
                for (int x = -2; x <= 2; x++) {
                    if (y <= 7 - Math.abs(x)) {
                        this.b.set(x, y, 18, (x + y) % 2 == 0 ? state(Blocks.CHISELED_DEEPSLATE) : state(Blocks.DEEPSLATE_TILES));
                    }
                }
            }
            for (int x = -1; x <= 1; x++) {
                this.b.set(x, 3, 18, state(Blocks.POLISHED_DEEPSLATE));
            }
            this.b.door(-1, 1, 18, Blocks.DARK_OAK_DOOR, Direction.NORTH);
            this.b.door(1, 1, 18, Blocks.DARK_OAK_DOOR, Direction.NORTH);
            this.b.set(0, 1, 18, state(Blocks.POLISHED_DEEPSLATE_WALL));
            this.b.set(0, 2, 18, state(Blocks.POLISHED_DEEPSLATE_WALL));
            // die Wächterfigur vor dem Mittelpfeiler
            this.b.set(0, 1, 19, state(Blocks.CHISELED_DEEPSLATE));
            this.b.set(0, 2, 19, state(Blocks.POLISHED_DEEPSLATE_WALL));
            this.b.set(0, 3, 19, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 0));
            // die Fensterrose
            for (int x = -5; x <= 5; x++) {
                for (int y = 8; y <= 18; y++) {
                    double r = Math.sqrt(x * x + (y - 13) * (y - 13));
                    if (r > 4.5) {
                        continue;
                    }
                    if (r > 3.7) {
                        this.b.set(x, y, 19, state(Blocks.POLISHED_DEEPSLATE));
                        this.b.set(x, y, 18, state(Blocks.POLISHED_DEEPSLATE));
                        continue;
                    }
                    this.b.set(x, y, 19, Blocks.AIR.defaultBlockState());
                    double angle = Math.atan2(y - 13, x);
                    double spoke = Math.abs(Math.IEEEremainder(angle, Math.PI / 4));
                    DyeColor color;
                    if (r < 1.2) {
                        color = DyeColor.LIGHT_BLUE;
                    } else if (r > 2.9) {
                        color = DyeColor.GRAY;
                    } else if (spoke < 0.22) {
                        color = DyeColor.BLACK;
                    } else {
                        int sector = Math.floorMod((int) Math.floor(angle / (Math.PI / 4)), 2);
                        color = sector == 0 ? DyeColor.CYAN : DyeColor.BLUE;
                    }
                    this.b.set(x, y, 18, Blocks.STAINED_GLASS.pick(color).defaultBlockState());
                }
            }
            // ein kleines Rundfenster im Giebel
            for (int x = -1; x <= 1; x++) {
                for (int y = 20; y <= 22; y++) {
                    if (Math.abs(x) + Math.abs(y - 21) <= 1) {
                        this.b.set(x, y, 18, Blocks.STAINED_GLASS.pick(DyeColor.BLACK).defaultBlockState());
                        this.b.set(x, y, 19, Blocks.AIR.defaultBlockState());
                    }
                }
            }
            // Fialen an den Ecken des Giebels
            for (int x : new int[] {-6, 6}) {
                for (int y = 21; y <= 23; y++) {
                    this.b.set(x, y, 19, state(Blocks.POLISHED_DEEPSLATE_WALL));
                }
                this.b.set(x, 24, 19, state(Blocks.END_ROD));
            }
        }

        /** Die beiden Fassadentürme: Glockenstube mit Schallöffnungen, Brüstung mit Eckfialen, steile Helme. */
        void towers() {
            for (int side = -1; side <= 1; side += 2) {
                int x0 = side > 0 ? 7 : -13;
                int x1 = side > 0 ? 13 : -7;
                for (int x = x0; x <= x1; x++) {
                    for (int z = 17; z <= 23; z++) {
                        boolean shell = x == x0 || x == x1 || z == 17 || z == 23;
                        boolean corner = (x == x0 || x == x1) && (z == 17 || z == 23);
                        int lx = Math.abs(x);
                        for (int y = 1; y <= 22; y++) {
                            BlockState s;
                            if (shell) {
                                boolean along = z == 17 || z == 23 ? lx == 9 || lx == 11 : z == 19 || z == 21;
                                boolean belfry = !corner && along && y >= 16 && y <= 19;
                                boolean arch = !corner && along && y == 20;
                                boolean gate = z == 17 && lx >= 8 && lx <= 10 && y <= 4;
                                if (belfry || gate) {
                                    s = Blocks.AIR.defaultBlockState();
                                } else if (arch) {
                                    s = state(Blocks.CHISELED_DEEPSLATE);
                                } else {
                                    s = corner ? state(Blocks.POLISHED_DEEPSLATE) : this.brick(x, y, z);
                                }
                            } else {
                                s = y == 15 || y == 22 ? state(Blocks.DARK_OAK_PLANKS) : Blocks.AIR.defaultBlockState();
                            }
                            this.b.set(x, y, z, s);
                        }
                        // Gesims, Brüstung, Eckfialen
                        if (shell) {
                            this.b.set(x, 23, z, corner ? state(Blocks.POLISHED_DEEPSLATE) : state(Blocks.DEEPSLATE_BRICK_WALL));
                            if (corner) {
                                this.b.set(x, 24, z, state(Blocks.POLISHED_DEEPSLATE_WALL));
                                this.b.set(x, 25, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
                            }
                        }
                    }
                }
                // Torbogen zum Seitenschiff
                for (int lx = 8; lx <= 10; lx += 2) {
                    this.b.set(side * lx, 5, 17, stair(Blocks.POLISHED_DEEPSLATE_STAIRS, lx == 8 ? (side > 0 ? Direction.WEST : Direction.EAST)
                            : out(side), Half.TOP));
                }
                // der Helm: steil, in Stufen
                int cx = side * 10;
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        int ring = Math.max(Math.abs(dx), Math.abs(dz));
                        if (ring == 2) {
                            Direction in = Math.abs(dx) == 2 ? (dx > 0 ? Direction.WEST : Direction.EAST) : (dz > 0 ? Direction.NORTH : Direction.SOUTH);
                            StairsShape shape = Math.abs(dx) == 2 && Math.abs(dz) == 2 ? StairsShape.OUTER_LEFT : StairsShape.STRAIGHT;
                            this.b.set(cx + dx, 23, 20 + dz, Math.abs(dx) == 2 && Math.abs(dz) == 2 ? state(Blocks.DEEPSLATE_TILES)
                                    : VillageBuilder.stairs(Blocks.DEEPSLATE_TILE_STAIRS, in, Half.BOTTOM, shape));
                        } else if (ring == 1) {
                            this.b.set(cx + dx, 23, 20 + dz, state(Blocks.DEEPSLATE_TILES));
                            this.b.set(cx + dx, 24, 20 + dz, state(Blocks.DEEPSLATE_TILES));
                            Direction in = Math.abs(dx) == 1 && dz == 0 ? (dx > 0 ? Direction.WEST : Direction.EAST)
                                    : dx == 0 ? (dz > 0 ? Direction.NORTH : Direction.SOUTH) : null;
                            this.b.set(cx + dx, 25, 20 + dz, in == null ? state(Blocks.DEEPSLATE_TILES)
                                    : stair(Blocks.DEEPSLATE_TILE_STAIRS, in, Half.BOTTOM));
                        } else {
                            for (int y = 23; y <= 26; y++) {
                                this.b.set(cx, y, 20, state(Blocks.DEEPSLATE_TILES));
                            }
                            this.b.set(cx, 27, 20, state(Blocks.POLISHED_DEEPSLATE_WALL));
                            this.b.set(cx, 28, 20, Blocks.LIGHTNING_ROD.waxed().oxidized().defaultBlockState());
                        }
                    }
                }
                // die Glocke in der Glockenstube
                this.b.set(cx, 21, 20, Blocks.DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
                this.b.set(cx, 20, 20, Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING)
                        .setValue(BellBlock.FACING, Direction.SOUTH));
                // unten in der Turmhalle: Truhe und Kerzen
                this.b.chest(side * 11, 1, 22, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH), ModLootTables.SITE_CATHEDRAL);
                this.candle(side * 12, 1, 22, Blocks.DYED_CANDLE.pick(DyeColor.BLACK), 3);
                this.candle(side * 8, 1, 22, Blocks.DYED_CANDLE.pick(DyeColor.WHITE), 2);
                for (int y = 2; y <= 14; y++) {
                    this.b.set(cx, y, 20, Blocks.IRON_CHAIN);
                }
            }
        }

        // =================================================================================
        // Ausstattung
        // =================================================================================

        void furnish() {
            // Bänke im Mittelschiff (der Blick geht nach Norden zum Altar)
            for (int z = -6; z <= 12; z += 2) {
                for (int x = 2; x <= 4; x++) {
                    this.b.set(x, 1, z, stair(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, Half.BOTTOM));
                    this.b.set(-x, 1, z, stair(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, Half.BOTTOM));
                }
            }
            // Chorgestühl
            for (int z = -16; z <= -11; z++) {
                if (pillar(z)) {
                    continue;
                }
                for (int side = -1; side <= 1; side += 2) {
                    this.b.set(side * 4, 2, z, stair(Blocks.DARK_OAK_STAIRS, out(side), Half.BOTTOM));
                    this.b.set(side * 5, 2, z, state(Blocks.DARK_OAK_PLANKS));
                    this.b.set(side * 5, 3, z, state(Blocks.DARK_OAK_PLANKS));
                    this.b.set(side * 5, 4, z, VillageBuilder.slab(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
                }
            }
            // der Altar: Block aus verstärktem Tiefenschiefer, darauf der Sculk-Katalysator zwischen schwarzen Kerzen
            for (int x = -1; x <= 1; x++) {
                this.b.set(x, 2, -14, state(Blocks.REINFORCED_DEEPSLATE));
            }
            this.b.set(0, 3, -14, state(Blocks.SCULK_CATALYST));
            this.candle(-1, 3, -14, Blocks.DYED_CANDLE.pick(DyeColor.BLACK), 3);
            this.candle(1, 3, -14, Blocks.DYED_CANDLE.pick(DyeColor.BLACK), 4);
            this.b.set(0, 2, -13, stair(Blocks.POLISHED_DEEPSLATE_STAIRS, Direction.NORTH, Half.BOTTOM));
            this.shrieker(-3, 2, -12);
            this.shrieker(3, 2, -12);
            // Gräber in den Seitenschiffen, Truhen an den Wänden
            for (int side = -1; side <= 1; side += 2) {
                for (int z0 : new int[] {11, 3, -5}) {
                    boolean open = z0 == 3 && side > 0;
                    for (int z = z0; z <= z0 + 2; z++) {
                        for (int x = 8; x <= 9; x++) {
                            this.b.set(side * x, 1, z, z == z0 || z == z0 + 2 ? state(Blocks.CHISELED_DEEPSLATE) : state(Blocks.POLISHED_DEEPSLATE));
                            this.b.set(side * x, 2, z, open ? Blocks.AIR.defaultBlockState()
                                    : VillageBuilder.slab(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM));
                        }
                    }
                    if (open) {
                        // ein geöffnetes Grab: der Deckel liegt daneben, drinnen Gebeine
                        this.b.set(side * 8, 1, z0 + 1, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
                        this.b.set(side * 9, 1, z0 + 1, Blocks.AIR.defaultBlockState());
                        this.b.set(side * 10, 1, z0, VillageBuilder.slab(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM));
                        this.b.set(side * 10, 1, z0 + 1, VillageBuilder.slab(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM));
                    } else {
                        // am Kopfende ein Schädel, am Fußende eine Kerze
                        this.b.set(side * 8, 2, z0, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 8));
                        this.candle(side * 9, 2, z0 + 2, Blocks.DYED_CANDLE.pick(DyeColor.WHITE), 1 + Math.floorMod(z0, 3));
                    }
                }
                this.hanging(side * 9, 8, 8, 8);
                this.hanging(side * 9, 8, 0, 8);
                this.hanging(side * 9, 8, -8, 8);
            }
            this.b.chest(10, 1, -1, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.SITE_CATHEDRAL);
            this.b.chest(-10, 1, 7, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST), ModLootTables.SITE_CATHEDRAL);
            // Sakristei (Osten): Pult, Schrank, Brauerei der Mönche
            this.b.set(10, 1, -17, Blocks.LECTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LecternBlock.FACING, Direction.WEST));
            this.b.chest(10, 1, -14, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.SITE_CATHEDRAL);
            this.b.set(10, 1, -13, Blocks.BARREL);
            this.b.set(10, 2, -13, Blocks.BARREL);
            this.b.set(7, 1, -17, Blocks.BREWING_STAND);
            this.b.set(7, 1, -16, Blocks.CAULDRON);
            this.candle(10, 3, -13, Blocks.DYED_CANDLE.pick(DyeColor.WHITE), 2);
            this.hanging(9, 8, -15, 8);
            // Gruft (Westen): Wände aus Knochen, Schädel in Nischen, die Reliquientruhe
            for (int z = -17; z <= -12; z++) {
                for (int y = 1; y <= 4; y++) {
                    this.b.set(-10, y, z, Math.floorMod(y + z, 3) == 0 ? Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 12)
                            : Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, y % 2 == 0 ? Direction.Axis.Z : Direction.Axis.Y));
                }
            }
            this.b.chest(-9, 1, -17, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), ModLootTables.SITE_CATHEDRAL_RELIQUARY);
            this.candle(-8, 1, -17, Blocks.DYED_CANDLE.pick(DyeColor.BLACK), 4);
            this.candle(-7, 1, -13, Blocks.DYED_CANDLE.pick(DyeColor.BLACK), 2);
            this.hanging(-8, 8, -15, 8);
            // Leuchter im Mittelschiff
            for (int z : new int[] {12, 4, -4, -12}) {
                this.hanging(0, 11, z, 17);
            }
            // Kreischer und Sensoren, wo die Gläubigen gingen
            this.shrieker(0, 1, 13);
            this.b.set(1, 1, 6, Blocks.SCULK_SENSOR);
            this.b.set(-1, 1, -2, Blocks.SCULK_SENSOR);
        }

        /**
         * Die Orgel in der Apsis: ein Gehäuse aus dunkler Eiche, darauf Pfeifen aus grün angelaufenem Kupfer (die Mitte am
         * höchsten), vorne eine zweite Reihe kleiner Pfeifen; davor der Spieltisch mit Notenblöcken und die Orgelbank. Zwei
         * Sensoren hören zu.
         */
        void organ() {
            for (int x = -4; x <= 4; x++) {
                int ax = Math.abs(x);
                int zc = APSE_Z - (int) Math.round(Math.sqrt(27 - x * x));
                int top = (int) Math.round(7 + (4 - ax) * 1.75);
                for (int y = 2; y <= 3; y++) {
                    this.b.set(x, y, zc, state(Blocks.DARK_OAK_PLANKS));
                    this.b.set(x, y, zc + 1, y == 3 ? state(Blocks.STRIPPED_DARK_OAK_LOG) : state(Blocks.DARK_OAK_PLANKS));
                }
                this.b.set(x, 4, zc + 1, ax % 2 == 1 ? state(Blocks.STRIPPED_DARK_OAK_LOG) : VillageBuilder.slab(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
                for (int y = 4; y <= top; y++) {
                    this.b.set(x, y, zc, y == 5 ? Blocks.COPPER_GRATE.waxed().weathered().defaultBlockState()
                            : Blocks.COPPER_BLOCK.waxed().weathered().defaultBlockState());
                }
                this.b.set(x, top + 1, zc, Blocks.LIGHTNING_ROD.waxed().weathered().defaultBlockState());
                // die vordere Reihe: kleine Pfeifen auf dem Gehäuse
                if (ax % 2 == 1) {
                    int small = 5 + (4 - ax) / 2;
                    for (int y = 5; y <= small; y++) {
                        this.b.set(x, y, zc + 1, Blocks.CUT_COPPER.waxed().exposed().defaultBlockState());
                    }
                    this.b.set(x, small + 1, zc + 1, Blocks.LIGHTNING_ROD.waxed().exposed().defaultBlockState());
                }
            }
            // Spieltisch: Notenblöcke als Manual, in der Mitte das Orgelpult (seit 1.7), vor dem Sordin, der Kantor der Stille,
            // steht und spielt; zu beiden Seiten ein Hocker (die Mitte bleibt frei für ihn)
            for (int x = -2; x <= 2; x++) {
                this.b.set(x, 2, -19, x == 0 ? io.github.sirschrolli.kingdomsatwar.registry.CathedralGear.ORGAN_DESK.get().defaultBlockState()
                        .setValue(io.github.sirschrolli.kingdomsatwar.block.deep.OrganDeskBlock.FACING, Direction.SOUTH)
                        : state(Blocks.NOTE_BLOCK));
                if (Math.abs(x) == 2) {
                    this.b.set(x, 2, -18, stair(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, Half.BOTTOM));
                }
            }
            this.b.set(-3, 2, -19, state(Blocks.DARK_OAK_PLANKS));
            this.b.set(3, 2, -19, state(Blocks.DARK_OAK_PLANKS));
            this.candle(-3, 3, -19, Blocks.DYED_CANDLE.pick(DyeColor.WHITE), 3);
            this.candle(3, 3, -19, Blocks.DYED_CANDLE.pick(DyeColor.WHITE), 3);
            this.b.set(-4, 2, -17, Blocks.SCULK_SENSOR);
            this.b.set(4, 2, -17, Blocks.SCULK_SENSOR);
        }

        /** Vor dem Portal ein Platz mit Laternen auf Säulen und zwei Wächterfiguren. */
        void plaza() {
            for (int x = -6; x <= 6; x++) {
                for (int z = 20; z <= 31; z++) {
                    int ax = Math.abs(x);
                    if (ax >= 5 && z <= 23) {
                        continue;
                    }
                    this.b.set(x, 0, z, ax <= 1 ? state(Blocks.POLISHED_DEEPSLATE) : ax == 6 ? state(Blocks.DEEPSLATE_BRICKS)
                            : Math.floorMod(z, 3) == 0 ? state(Blocks.POLISHED_DEEPSLATE) : state(Blocks.DEEPSLATE_TILES));
                }
            }
            for (int z : new int[] {24, 29}) {
                for (int x : new int[] {-6, 6}) {
                    this.b.set(x, 1, z, state(Blocks.POLISHED_DEEPSLATE));
                    this.b.set(x, 2, z, state(Blocks.POLISHED_DEEPSLATE_WALL));
                    this.b.set(x, 3, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
                }
            }
            for (int x : new int[] {-4, 4}) {
                this.b.set(x, 1, 21, state(Blocks.CHISELED_DEEPSLATE));
                this.b.set(x, 2, 21, state(Blocks.POLISHED_DEEPSLATE));
                this.b.set(x, 3, 21, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 0));
            }
            this.shrieker(-9, 1, 27);
            this.shrieker(10, 1, 25);
            this.shrieker(-17, 1, -4);
            this.shrieker(17, 1, 6);
        }

        // =================================================================================
        // Sculk
        // =================================================================================

        /**
         * Der Sculk hat sich ausgebreitet: Flecken auf dem Höhlengrund und (weniger) im Bau, Adern an den Rändern und an
         * den Mauern, hier und da ein Sensor.
         */
        void sculk() {
            for (int x = -RX; x <= RX; x++) {
                for (int z = CZ - RZ; z <= CZ + RZ; z++) {
                    if (!this.column(x, z)) {
                        continue;
                    }
                    boolean inside = Math.abs(x) <= 13 && z >= -25 && z <= 23;
                    int y = inside ? floorTop(x, z) : 0;
                    BlockState floor = this.b.get(x, y, z);
                    BlockState above = this.b.get(x, y + 1, z);
                    if (!floor.isSolidRender() || !above.isAir()) {
                        continue;
                    }
                    double s = this.smooth(x, 0, z, 3);
                    double edge = inside ? 0.68 : 0.62;
                    if (s > edge) {
                        this.b.set(x, y, z, state(Blocks.SCULK));
                        if (!inside && this.b.chance(x, 77, z, 0.05f)) {
                            this.b.set(x, y + 1, z, Blocks.SCULK_SENSOR);
                        }
                        // Adern an angrenzenden Mauern
                        for (Direction d : Direction.Plane.HORIZONTAL) {
                            BlockState wall = this.b.get(x + d.getStepX(), y + 1, z + d.getStepZ());
                            if (wall.isSolidRender() && this.b.get(x, y + 1, z).isAir()) {
                                this.b.set(x, y + 1, z, Blocks.SCULK_VEIN.defaultBlockState()
                                        .setValue(MultifaceBlock.getFaceProperty(d), true));
                                break;
                            }
                        }
                    } else if (s > edge - 0.05 && this.b.chance(x, 78, z, 0.6f)) {
                        this.b.set(x, y + 1, z, Blocks.SCULK_VEIN.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true));
                    }
                }
            }
        }
    }
}
