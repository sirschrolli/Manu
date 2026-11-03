package io.github.sirschrolli.kingdomsatwar.architect.castle;

import io.github.sirschrolli.kingdomsatwar.architect.io.Volume;
import io.github.sirschrolli.kingdomsatwar.economy.town.Blueprint;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Eine Burg, die der Dragon Architect selbst entwirft – als Volumen, das sich wie ein Gebäude vermessen lässt: eine
 * Ringmauer mit Wehrgang, Zinnen und Brustwehr auf einem Sockel aus Bruchstein, Ecktürme mit spitzen Schieferdächern,
 * Mauertürme mit Zinnenkranz, ein Torhaus mit Durchfahrt und Fallgitter zwischen zwei Tortürmen, ein Bergfried mit vier
 * Ecktürmchen, Stockwerken, Leiter, Fenstern und Laternen, gepflasterte Wege mit Laternen und im Hof die Häuser der
 * Siedlung – Palas, Kaserne, Schmiede, Taverne, Lager, Bürgerhäuser, Brunnen und Markt.
 *
 * <p>Koordinaten im Volumen: x nach rechts, z nach hinten (das Tor liegt vorn bei z = 0), y = 0 ist die Lage Gelände unter
 * der Bodenlinie (dort liegen die Wege), y = 1 die erste Lage darüber. Alles ist vorhersagbar (gleiche Größe und Saat –
 * gleiche Burg).
 */
public final class CastleGenerator {
    /** Abstand der Mauer vom Rand (dort ragen die Türme hinaus). */
    static final int INSET = 5;
    /** Dicke der Ringmauer. */
    static final int THICK = 3;
    /** Höhe der Ringmauer (Lagen über dem Boden; der Wehrgang liegt oben auf). */
    static final int WALL = 10;

    private final Volume v;
    private final int w;
    private final int d;
    private final long seed;
    private final int gateX;
    private final int keepSize;
    private final int keepHeight;
    private final int keepX;
    private final int keepZ;
    /** Was schon belegt ist (Grundfläche, mit Abstand) – dort steht kein weiteres Haus. */
    private final List<BoundingBox> taken = new ArrayList<>();

    private CastleGenerator(int w, int d, long seed) {
        this.w = w;
        this.d = d;
        this.seed = seed;
        this.keepSize = w >= 200 ? 22 : 18;
        this.keepHeight = w >= 200 ? 30 : 26;
        this.v = new Volume(w, this.keepHeight + 14, d);
        this.gateX = w / 2;
        this.keepX = w / 2;
        this.keepZ = d - INSET - THICK - 12 - this.keepSize / 2;
    }

    /** Entwirft eine Burg dieser Größe (Breite und Tiefe 96 bis 320). */
    public static Volume generate(int width, int depth, long seed) {
        CastleGenerator g = new CastleGenerator(Math.max(96, Math.min(320, width)), Math.max(96, Math.min(320, depth)), seed);
        g.paths();
        g.ringWall();
        g.towers();
        g.gatehouse();
        g.keep();
        g.buildings();
        g.lights();
        g.greenery();
        return g.v;
    }

    // =====================================================================================
    // Grundlagen
    // =====================================================================================

    /** Setzt einen Block (y über der Bodenlinie; −1 ist die Lage Gelände). */
    private void put(int x, int y, int z, BlockState state) {
        this.v.set(x, y + 1, z, state);
    }

    private BlockState at(int x, int y, int z) {
        return this.v.getBlockState(new BlockPos(x, y + 1, z));
    }

    private int hash(int x, int y, int z) {
        long h = this.seed ^ x * 0x9E3779B97F4A7C15L ^ y * 0xC2B2AE3D27D4EB4FL ^ z * 0x165667B19E3779F9L;
        h ^= h >>> 29;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 32;
        return (int) (h & 0x7FFFFFFF);
    }

    /** Mauerwerk: unten Bruchstein (teils bemoost), darüber Steinziegel mit bemoosten und rissigen dazwischen. */
    private BlockState stone(int x, int y, int z) {
        int r = this.hash(x, y, z) % 100;
        if (y <= 1) {
            return r < 30 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState();
        }
        if (r < 9) {
            return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        }
        if (r < 17) {
            return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        }
        return Blocks.STONE_BRICKS.defaultBlockState();
    }

    /** Pflaster der Wege (unter der Bodenlinie, an Stelle des Grases). */
    private BlockState paving(int x, int z) {
        int r = this.hash(x, -1, z) % 100;
        if (r < 18) {
            return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        }
        if (r < 34) {
            return Blocks.POLISHED_ANDESITE.defaultBlockState();
        }
        if (r < 44) {
            return Blocks.STONE_BRICKS.defaultBlockState();
        }
        return Blocks.COBBLESTONE.defaultBlockState();
    }

    /** Abstand einer Stelle zum nächsten Rand des Volumens. */
    private int edge(int x, int z) {
        return Math.min(Math.min(x, z), Math.min(this.w - 1 - x, this.d - 1 - z));
    }

    private static BlockState stairs(Block block, Direction facing, Half half) {
        return block.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
    }

    // =====================================================================================
    // Wege
    // =====================================================================================

    /** Gepflasterte Wege: vom Tor zum Bergfried, quer über den Hof und rund um den Platz in der Mitte. */
    private void paths() {
        int inner = INSET + THICK;
        int mid = (inner + 14 + this.keepZ - this.keepSize / 2) / 2;
        // vom Tor zum Bergfried (fünf breit)
        for (int z = inner; z <= this.keepZ - this.keepSize / 2; z++) {
            for (int x = this.gateX - 2; x <= this.gateX + 2; x++) {
                this.put(x, -1, z, this.paving(x, z));
            }
        }
        // quer über den Hof
        for (int x = inner + 4; x < this.w - inner - 4; x++) {
            for (int z = mid - 1; z <= mid + 1; z++) {
                this.put(x, -1, z, this.paving(x, z));
            }
        }
        // der Platz mit dem Brunnen
        for (int z = mid - 8; z <= mid + 8; z++) {
            for (int x = this.gateX - 8; x <= this.gateX + 8; x++) {
                this.put(x, -1, z, this.paving(x, z));
            }
        }
        this.taken.add(new BoundingBox(this.gateX - 9, 0, mid - 9, this.gateX + 9, 0, mid + 9));
        this.taken.add(new BoundingBox(this.gateX - 3, 0, inner, this.gateX + 3, 0, this.d));
        this.taken.add(new BoundingBox(inner, 0, mid - 2, this.w - inner, 0, mid + 2));
        // Brunnen auf dem Platz
        this.stamp(BuildingType.WELL, 1, this.gateX, mid, Rotation.NONE, false);
        this.plaza = mid;
    }

    private int plaza;

    // =====================================================================================
    // Ringmauer
    // =====================================================================================

    private boolean inWall(int x, int z) {
        int e = this.edge(x, z);
        return e >= INSET && e < INSET + THICK;
    }

    /**
     * Die Ringmauer: Sockel aus Bruchstein, Steinziegel, oben der Wehrgang mit Zinnen nach außen (zwei Steine, eine
     * Lücke) und einer Brustwehr nach innen; zum Wehrgang führen Treppen neben dem Tor.
     */
    private void ringWall() {
        for (int z = 0; z < this.d; z++) {
            for (int x = 0; x < this.w; x++) {
                if (!this.inWall(x, z)) {
                    continue;
                }
                int e = this.edge(x, z);
                for (int y = 0; y < WALL; y++) {
                    this.put(x, y, z, this.stone(x, y, z));
                }
                // Fundament unter der Mauer
                this.put(x, -1, z, Blocks.COBBLESTONE.defaultBlockState());
                if (e == INSET) {
                    // Sockel: außen eine Lage vorgesetzt, oben mit einer Schräge aus Stufen
                    int ox = x == INSET ? x - 1 : x == this.w - 1 - INSET ? x + 1 : x;
                    int oz = z == INSET ? z - 1 : z == this.d - 1 - INSET ? z + 1 : z;
                    if (ox != x || oz != z) {
                        this.put(ox, -1, oz, Blocks.COBBLESTONE.defaultBlockState());
                        this.put(ox, 0, oz, this.stone(ox, 0, oz));
                        this.put(ox, 1, oz, this.stone(ox, 1, oz));
                        Direction toWall = ox < x ? Direction.EAST : ox > x ? Direction.WEST : oz < z ? Direction.SOUTH : Direction.NORTH;
                        if ((ox != x) != (oz != z)) {
                            this.put(ox, 2, oz, stairs(Blocks.STONE_BRICK_STAIRS, toWall, Half.BOTTOM));
                        }
                    }
                    int along = x + z;
                    if (along % 3 != 0) {
                        this.put(x, WALL, z, this.stone(x, WALL, z));
                    }
                    // Gesims unter den Zinnen
                    if (this.hash(x, WALL - 2, z) % 3 == 0) {
                        this.put(x, WALL - 2, z, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
                    }
                } else if (e == INSET + THICK - 1) {
                    this.put(x, WALL, z, Blocks.STONE_BRICK_WALL.defaultBlockState());
                }
            }
        }
        // Treppen zum Wehrgang: rechts und links vom Tor an der Innenseite der vorderen Mauer
        int inner = INSET + THICK;
        for (int side = -1; side <= 1; side += 2) {
            int x0 = this.gateX + side * 12;
            for (int i = 0; i < WALL; i++) {
                int x = x0 + side * i;
                for (int z = inner; z < inner + 2; z++) {
                    this.put(x, i, z, stairs(Blocks.STONE_BRICK_STAIRS, side > 0 ? Direction.EAST : Direction.WEST, Half.BOTTOM));
                    for (int y = 0; y < i; y++) {
                        this.put(x, y, z, this.stone(x, y, z));
                    }
                }
            }
            // Lücke in der Brustwehr oben an der Treppe
            for (int z = inner - 1; z < inner + 1; z++) {
                this.put(x0 + side * WALL, WALL, inner - 1, Blocks.AIR.defaultBlockState());
            }
            this.taken.add(new BoundingBox(Math.min(x0, x0 + side * WALL) - 1, 0, inner, Math.max(x0, x0 + side * WALL) + 1, 0, inner + 3));
        }
    }

    // =====================================================================================
    // Türme
    // =====================================================================================

    /**
     * Ecktürme (11 × 11, mit spitzem Dach) und Mauertürme (9 × 9, mit Zinnenkranz) alle 40 Blöcke entlang der Mauer;
     * das Tor bleibt frei.
     */
    private void towers() {
        int c = INSET + 1;
        int[][] corners = {{c, c}, {this.w - 1 - c, c}, {c, this.d - 1 - c}, {this.w - 1 - c, this.d - 1 - c}};
        for (int[] t : corners) {
            this.tower(t[0], t[1], 11, WALL + 9, true);
        }
        for (int[] side : new int[][] {{0, 0}, {0, 1}, {1, 0}, {1, 1}}) {
            boolean alongX = side[0] == 0;
            int length = alongX ? this.w : this.d;
            int n = Math.max(0, (length - 2 * c) / 40 - 1);
            for (int i = 1; i <= n; i++) {
                int p = c + (length - 2 * c) * i / (n + 1);
                int x = alongX ? p : side[1] == 0 ? c : this.w - 1 - c;
                int z = alongX ? (side[1] == 0 ? c : this.d - 1 - c) : p;
                if (alongX && side[1] == 0 && Math.abs(x - this.gateX) < 22) {
                    continue; // dort steht das Torhaus
                }
                this.tower(x, z, 9, WALL + 6, false);
            }
        }
    }

    /** Ein viereckiger Turm um (cx, cz): Mauerring, Stockwerke, Schießscharten, Türen, Leiter, Dach oder Zinnen. */
    private void tower(int cx, int cz, int size, int height, boolean roof) {
        int r = size / 2;
        // zur Burgmitte: dort liegt die Tür
        Direction in = Math.abs(cx - this.w / 2) > Math.abs(cz - this.d / 2) ? (cx < this.w / 2 ? Direction.EAST : Direction.WEST)
                : (cz < this.d / 2 ? Direction.SOUTH : Direction.NORTH);
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int y = -1; y < height; y++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dx = -r; dx <= r; dx++) {
                    int x = cx + dx;
                    int z = cz + dz;
                    boolean ring = Math.abs(dx) == r || Math.abs(dz) == r;
                    if (y == -1) {
                        this.put(x, y, z, Blocks.COBBLESTONE.defaultBlockState());
                    } else if (ring) {
                        this.put(x, y, z, this.stone(x, y, z));
                    } else if (y == 0 || y == WALL - 1 || y == height - 1) {
                        this.put(x, y, z, (y == height - 1 ? Blocks.STONE_BRICKS : Blocks.SPRUCE_PLANKS).defaultBlockState());
                    } else {
                        this.put(x, y, z, air);
                    }
                }
            }
        }
        // Schießscharten in jeder Seite (unten und über dem Wehrgang)
        for (Direction f : Direction.Plane.HORIZONTAL) {
            for (int y : new int[] {3, 4, WALL + 2, WALL + 3}) {
                if (y < height - 1) {
                    this.put(cx + f.getStepX() * r, y, cz + f.getStepZ() * r, air);
                }
            }
        }
        // Tür zum Hof und Durchgänge zum Wehrgang
        int dxIn = in.getStepX() * r;
        int dzIn = in.getStepZ() * r;
        this.put(cx + dxIn, 1, cz + dzIn, Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, in));
        this.put(cx + dxIn, 2, cz + dzIn, Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, in).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        this.put(cx + dxIn + in.getStepX(), 0, cz + dzIn + in.getStepZ(), stairs(Blocks.STONE_BRICK_STAIRS, in.getOpposite(), Half.BOTTOM));
        for (Direction f : new Direction[] {in.getClockWise(), in.getCounterClockWise()}) {
            int x = cx + f.getStepX() * r;
            int z = cz + f.getStepZ() * r;
            if (this.inWall(x + f.getStepX(), z + f.getStepZ())) {
                this.put(x, WALL, z, air);
                this.put(x, WALL + 1, z, air);
            }
        }
        // Leiter innen an der Wand gegenüber der Tür
        Direction back = in.getOpposite();
        int lx = cx + back.getStepX() * (r - 1);
        int lz = cz + back.getStepZ() * (r - 1);
        for (int y = 1; y < height; y++) {
            this.put(lx, y, lz, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, in));
        }
        // Laterne unter der Decke des Erdgeschosses
        this.put(cx, WALL - 2, cz, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        if (roof) {
            this.pyramid(cx, cz, r, height, true);
        } else {
            this.crown(cx, cz, r, height);
        }
        this.taken.add(new BoundingBox(cx - r - 2, 0, cz - r - 2, cx + r + 2, 0, cz + r + 2));
    }

    /**
     * Ein Zinnenkranz auf Kragsteinen: oben ragt der Turm eine Lage vor (darunter umgedrehte Stufen), darauf die Zinnen –
     * wie ein Wehrturm aus dem Bilderbuch.
     */
    private void crown(int cx, int cz, int r, int height) {
        int k = r + 1;
        for (int dz = -k; dz <= k; dz++) {
            for (int dx = -k; dx <= k; dx++) {
                if (Math.abs(dx) != k && Math.abs(dz) != k) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                boolean corner = Math.abs(dx) == k && Math.abs(dz) == k;
                if (!corner) {
                    Direction out = Math.abs(dx) == k ? (dx > 0 ? Direction.EAST : Direction.WEST) : (dz > 0 ? Direction.SOUTH : Direction.NORTH);
                    this.put(x, height - 2, z, stairs(Blocks.STONE_BRICK_STAIRS, out.getOpposite(), Half.TOP));
                }
                this.put(x, height - 1, z, this.stone(x, height - 1, z));
                if ((dx + dz) % 2 == 0 || corner) {
                    this.put(x, height, z, this.stone(x, height, z));
                }
            }
        }
        for (int dz = -r; dz <= r; dz++) {
            for (int dx = -r; dx <= r; dx++) {
                if (!this.at(cx + dx, height - 1, cz + dz).is(Blocks.LADDER)) {
                    this.put(cx + dx, height - 1, cz + dz, Blocks.STONE_BRICKS.defaultBlockState());
                }
            }
        }
    }

    /** Ein spitzes Dach aus Tiefenschiefer-Ziegeln über einem Quadrat (Halbmesser r) ab der Lage y, mit Spitze. */
    private void pyramid(int cx, int cz, int r, int y) {
        this.pyramid(cx, cz, r, y, false);
    }

    /** Wie oben; {@code eaves}: mit einer Traufe, die eine Lage über die Mauer hinausragt. */
    private void pyramid(int cx, int cz, int r, int y, boolean eaves) {
        if (eaves) {
            int k = r + 1;
            for (int dz = -k; dz <= k; dz++) {
                for (int dx = -k; dx <= k; dx++) {
                    if (Math.abs(dx) != k && Math.abs(dz) != k) {
                        continue;
                    }
                    Direction up = Math.abs(dz) >= Math.abs(dx) ? (dz < 0 ? Direction.SOUTH : Direction.NORTH)
                            : (dx < 0 ? Direction.EAST : Direction.WEST);
                    this.put(cx + dx, y - 1, cz + dz, stairs(Blocks.DEEPSLATE_TILE_STAIRS, up, Half.BOTTOM));
                }
            }
        }
        for (int i = 0; i <= r; i++) {
            int layer = y + i;
            int k = r - i;
            if (k == 0) {
                this.put(cx, layer, cz, Blocks.DEEPSLATE_TILES.defaultBlockState());
                this.put(cx, layer + 1, cz, Blocks.DARK_OAK_FENCE.defaultBlockState());
                this.put(cx, layer + 2, cz, Blocks.DARK_OAK_FENCE.defaultBlockState());
                break;
            }
            for (int dz = -k; dz <= k; dz++) {
                for (int dx = -k; dx <= k; dx++) {
                    if (Math.abs(dx) != k && Math.abs(dz) != k) {
                        continue;
                    }
                    Direction up = Math.abs(dz) >= Math.abs(dx) ? (dz < 0 ? Direction.SOUTH : Direction.NORTH)
                            : (dx < 0 ? Direction.EAST : Direction.WEST);
                    this.put(cx + dx, layer, cz + dz, stairs(Blocks.DEEPSLATE_TILE_STAIRS, up, Half.BOTTOM));
                }
            }
            // innen gefüllt, damit das Dach dicht ist
            for (int dz = -k + 1; dz <= k - 1; dz++) {
                for (int dx = -k + 1; dx <= k - 1; dx++) {
                    this.put(cx + dx, layer, cz + dz, Blocks.DEEPSLATE_TILES.defaultBlockState());
                }
            }
        }
    }

    // =====================================================================================
    // Torhaus
    // =====================================================================================

    /**
     * Das Torhaus: ein Block über der Durchfahrt (fünf breit, sieben hoch, mit Bogen und hochgezogenem Fallgitter),
     * zwei Tortürme mit Dach, darüber eine Wachstube mit Fenstern und Zinnen, Banner über dem Tor.
     */
    private void gatehouse() {
        int x0 = this.gateX - 8;
        int x1 = this.gateX + 8;
        int z0 = INSET;
        int z1 = INSET + 10;
        int top = WALL + 4;
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int y = -1; y < top; y++) {
            for (int z = z0; z <= z1; z++) {
                for (int x = x0; x <= x1; x++) {
                    boolean ring = x == x0 || x == x1 || z == z0 || z == z1;
                    if (y == -1) {
                        this.put(x, y, z, Blocks.COBBLESTONE.defaultBlockState());
                    } else if (ring || y == 7 || y == top - 1) {
                        this.put(x, y, z, this.stone(x, y, z));
                    } else {
                        this.put(x, y, z, y < 7 ? this.stone(x, y, z) : air);
                    }
                }
            }
        }
        // Durchfahrt
        for (int z = z0; z <= z1; z++) {
            for (int x = this.gateX - 2; x <= this.gateX + 2; x++) {
                for (int y = 0; y < 7; y++) {
                    boolean corner = y == 6 && (x == this.gateX - 2 || x == this.gateX + 2);
                    this.put(x, y, z, corner ? stairs(Blocks.STONE_BRICK_STAIRS, x < this.gateX ? Direction.EAST : Direction.WEST, Half.TOP) : air);
                }
                this.put(x, -1, z, this.paving(x, z));
            }
        }
        // hochgezogenes Fallgitter
        for (int x = this.gateX - 1; x <= this.gateX + 1; x++) {
            this.put(x, 5, z0 + 1, Blocks.IRON_BARS.defaultBlockState());
            this.put(x, 6, z0 + 1, Blocks.IRON_BARS.defaultBlockState());
        }
        // Fenster der Wachstube und Zinnen
        for (int x = x0 + 2; x <= x1 - 2; x += 3) {
            if (Math.abs(x - this.gateX) > 1) {
                this.put(x, 9, z0, Blocks.GLASS_PANE.defaultBlockState());
                this.put(x, 9, z1, Blocks.GLASS_PANE.defaultBlockState());
            }
        }
        for (int z = z0; z <= z1; z++) {
            for (int x = x0; x <= x1; x++) {
                if ((x == x0 || x == x1 || z == z0 || z == z1) && (x + z) % 2 == 0) {
                    this.put(x, top, z, this.stone(x, top, z));
                }
            }
        }
        // Zugang von der Wachstube zum Wehrgang
        for (int x : new int[] {x0, x1}) {
            this.put(x, WALL, INSET + 1, air);
            this.put(x, WALL + 1, INSET + 1, air);
        }
        this.put(this.gateX, 8, z0 + 5, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        // Tortürme
        this.tower(x0 - 3, INSET + 2, 7, top + 4, true);
        this.tower(x1 + 3, INSET + 2, 7, top + 4, true);
        // Banner über dem Tor
        for (int x : new int[] {this.gateX - 3, this.gateX + 3}) {
            this.put(x, 9, z0 - 1, Blocks.WALL_BANNER.red().defaultBlockState().setValue(WallBannerBlock.FACING, Direction.NORTH));
        }
        this.taken.add(new BoundingBox(x0 - 8, 0, z0, x1 + 8, 0, z1 + 4));
    }

    // =====================================================================================
    // Bergfried
    // =====================================================================================

    /**
     * Der Bergfried hinten in der Mitte: Mauern zwei Steine dick, Stockwerke alle sechs Lagen mit Fenstern, eine Leiter
     * bis aufs Dach, hängende Laternen, eine Tür mit Stufen, oben Zinnen und vier Ecktürmchen mit spitzen Dächern,
     * in der Mitte des Dachs eine Fahne.
     */
    private void keep() {
        int s = this.keepSize;
        int r = s / 2;
        int x0 = this.keepX - r;
        int z0 = this.keepZ - r;
        int x1 = x0 + s - 1;
        int z1 = z0 + s - 1;
        int h = this.keepHeight;
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int y = -1; y < h; y++) {
            for (int z = z0; z <= z1; z++) {
                for (int x = x0; x <= x1; x++) {
                    int e = Math.min(Math.min(x - x0, x1 - x), Math.min(z - z0, z1 - z));
                    if (y == -1) {
                        this.put(x, y, z, Blocks.COBBLESTONE.defaultBlockState());
                    } else if (e <= 1) {
                        this.put(x, y, z, this.stone(x, y, z));
                    } else if (y % 6 == 0 || y == h - 1) {
                        this.put(x, y, z, (y == h - 1 ? Blocks.STONE_BRICKS : Blocks.SPRUCE_PLANKS).defaultBlockState());
                    } else {
                        this.put(x, y, z, air);
                    }
                }
            }
        }
        // Fenster: je Stockwerk in der Mitte jeder Seite zwei Scheiben (außen), innen offen
        for (int f = 0; f + 6 < h; f += 6) {
            for (int k = -1; k <= 0; k++) {
                for (int y = f + 2; y <= f + 3; y++) {
                    this.put(this.keepX + k, y, z0, Blocks.GLASS_PANE.defaultBlockState());
                    this.put(this.keepX + k, y, z0 + 1, air);
                    this.put(this.keepX + k, y, z1, Blocks.GLASS_PANE.defaultBlockState());
                    this.put(this.keepX + k, y, z1 - 1, air);
                    this.put(x0, y, this.keepZ + k, Blocks.GLASS_PANE.defaultBlockState());
                    this.put(x0 + 1, y, this.keepZ + k, air);
                    this.put(x1, y, this.keepZ + k, Blocks.GLASS_PANE.defaultBlockState());
                    this.put(x1 - 1, y, this.keepZ + k, air);
                }
            }
            if (f > 0) {
                this.put(this.keepX, f - 1, this.keepZ, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
        }
        // Tür vorn (zum Tor hin) mit Stufen
        for (int k = 0; k < 2; k++) {
            this.put(this.keepX - k, 1, z0, Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH)
                    .setValue(DoorBlock.HINGE, k == 0 ? net.minecraft.world.level.block.state.properties.DoorHingeSide.RIGHT
                            : net.minecraft.world.level.block.state.properties.DoorHingeSide.LEFT));
            this.put(this.keepX - k, 2, z0, Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH)
                    .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                    .setValue(DoorBlock.HINGE, k == 0 ? net.minecraft.world.level.block.state.properties.DoorHingeSide.RIGHT
                            : net.minecraft.world.level.block.state.properties.DoorHingeSide.LEFT));
            this.put(this.keepX - k, 1, z0 + 1, air);
            this.put(this.keepX - k, 2, z0 + 1, air);
            this.put(this.keepX - k, 0, z0 - 1, stairs(Blocks.STONE_BRICK_STAIRS, Direction.SOUTH, Half.BOTTOM));
        }
        // Leiter in der hinteren linken Ecke, durch alle Böden
        for (int y = 1; y < h; y++) {
            this.put(x0 + 2, y, z1 - 2, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
        }
        // ein paar Möbel im Erdgeschoss und im Gemach
        this.put(x0 + 3, 1, z0 + 3, Blocks.BARREL.defaultBlockState());
        this.put(x0 + 4, 1, z0 + 3, Blocks.BARREL.defaultBlockState());
        this.put(x1 - 3, 1, z0 + 3, Blocks.CRAFTING_TABLE.defaultBlockState());
        this.put(x1 - 3, 13, z1 - 4, Blocks.BED.red().defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH));
        this.put(x1 - 3, 13, z1 - 3, Blocks.BED.red().defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH)
                .setValue(BedBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.HEAD));
        this.put(x1 - 5, 13, z1 - 3, Blocks.BOOKSHELF.defaultBlockState());
        this.put(x1 - 6, 13, z1 - 3, Blocks.BOOKSHELF.defaultBlockState());
        // Zinnen
        for (int z = z0; z <= z1; z++) {
            for (int x = x0; x <= x1; x++) {
                if ((x == x0 || x == x1 || z == z0 || z == z1) && (x + z) % 2 == 0) {
                    this.put(x, h, z, this.stone(x, h, z));
                }
            }
        }
        // Ecktürmchen
        for (int[] c : new int[][] {{x0 + 2, z0 + 2}, {x1 - 2, z0 + 2}, {x0 + 2, z1 - 2}, {x1 - 2, z1 - 2}}) {
            for (int y = h - 4; y < h + 5; y++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dx = -2; dx <= 2; dx++) {
                        boolean ring = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                        if (y >= h || ring) {
                            this.put(c[0] + dx, y, c[1] + dz, ring ? this.stone(c[0] + dx, y, c[1] + dz) : air);
                        }
                    }
                }
            }
            for (Direction f : Direction.Plane.HORIZONTAL) {
                this.put(c[0] + f.getStepX() * 2, h + 2, c[1] + f.getStepZ() * 2, air);
            }
            this.pyramid(c[0], c[1], 2, h + 5);
        }
        // Fahne auf dem Dach
        for (int y = h; y < h + 6; y++) {
            this.put(this.keepX, y, this.keepZ, Blocks.DARK_OAK_FENCE.defaultBlockState());
        }
        this.put(this.keepX, h + 6, this.keepZ, Blocks.BANNER.red().defaultBlockState());
        this.taken.add(new BoundingBox(x0 - 4, 0, z0 - 6, x1 + 4, 0, z1 + 4));
    }

    // =====================================================================================
    // Häuser im Hof
    // =====================================================================================

    /** Setzt einen Bauplan der Siedlung an eine Stelle (Mitte der Grundfläche), gedreht; {@code mark}: Platz belegen. */
    private BoundingBox stamp(BuildingType type, int design, int cx, int cz, Rotation rotation, boolean mark) {
        Blueprint plan = type.blueprint(design);
        BlockPos origin = new BlockPos(cx, 0, cz);
        for (Blueprint.Entry e : plan.blocks()) {
            if (e.state().isAir() && e.pos().getY() >= 0) {
                continue;
            }
            BlockPos p = Blueprint.world(origin, rotation, e.pos());
            if (p.getY() >= -1) {
                this.put(p.getX(), p.getY(), p.getZ(), Blueprint.world(e.state(), rotation));
            }
        }
        BoundingBox box = plan.worldBounds(origin, rotation);
        if (mark) {
            this.taken.add(box.inflatedBy(2));
        }
        return box;
    }

    private boolean free(BoundingBox box) {
        int inner = INSET + THICK + 2;
        if (box.minX() < inner || box.minZ() < inner || box.maxX() > this.w - 1 - inner || box.maxZ() > this.d - 1 - inner) {
            return false;
        }
        for (BoundingBox t : this.taken) {
            if (t.intersects(box.minX(), box.minZ(), box.maxX(), box.maxZ())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Die Häuser der Siedlung im Hof: zuerst Palas, Kaserne, Markt und Taverne an die besten Stellen, dann Reihen von
     * Bürgerhäusern, Werkstätten und Lagern entlang der Mauern, jedes mit der Vorderseite zum Hof.
     */
    private void buildings() {
        // Palas und Kaserne links und rechts vom Bergfried, mit der Vorderseite zum Hof
        int r = this.keepSize / 2;
        this.beside(BuildingType.THRONE_HALL, 0, this.keepX - r - 7, this.keepZ, -1, Rotation.NONE);
        this.beside(BuildingType.BARRACKS, 0, this.keepX + r + 7, this.keepZ, 1, Rotation.NONE);
        // Markt und Taverne vor dem Platz, an der Querstraße
        this.beside(BuildingType.MARKET, 1, this.gateX - 11, this.plaza - 13, -1, Rotation.CLOCKWISE_180);
        this.beside(BuildingType.TAVERN, 1, this.gateX + 11, this.plaza - 13, 1, Rotation.CLOCKWISE_180);
        this.beside(BuildingType.TOWNHOUSE, 2, this.gateX - 11, this.plaza + 14, -1, Rotation.NONE);
        this.beside(BuildingType.SMITHY, 1, this.gateX + 11, this.plaza + 14, 1, Rotation.NONE);
        // Reihen entlang der Mauern
        BuildingType[] cycle = {BuildingType.TOWNHOUSE, BuildingType.HOUSE, BuildingType.SMITHY, BuildingType.HOUSE, BuildingType.STOREHOUSE,
                BuildingType.TOWNHOUSE, BuildingType.HOUSE, BuildingType.MAP_HOUSE, BuildingType.HOUSE, BuildingType.LUMBER_HUT,
                BuildingType.TOWNHOUSE, BuildingType.HOUSE, BuildingType.FIELD, BuildingType.HOUSE};
        int inner = INSET + THICK + 4;
        int k = 0;
        // links (Front nach Osten) und rechts (Front nach Westen)
        for (int side = 0; side < 2; side++) {
            Rotation rot = side == 0 ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;
            int z = inner + 20;
            while (z < this.d - inner - 10) {
                boolean placed = false;
                for (int tries = 0; tries < cycle.length && !placed; tries++) {
                    BuildingType type = cycle[(k + tries) % cycle.length];
                    int design = this.hash(side, z, tries) % Math.max(1, type.designs());
                    BoundingBox local = type.blueprint(design).worldBounds(BlockPos.ZERO, rot);
                    int depthX = local.getXSpan();
                    int lengthZ = local.getZSpan();
                    int cx = side == 0 ? inner + (-local.minX()) : this.w - 1 - inner - local.maxX();
                    int cz = z - local.minZ();
                    BoundingBox box = new BoundingBox(cx + local.minX(), 0, cz + local.minZ(), cx + local.maxX(), 0, cz + local.maxZ());
                    if (depthX < 24 && this.free(box)) {
                        this.stamp(type, design, cx, cz, rot, true);
                        z += lengthZ + 3;
                        k = (k + tries + 1) % cycle.length;
                        placed = true;
                    }
                }
                if (!placed) {
                    z += 2;
                }
            }
        }
        // hinten (Front nach Norden) und vorn (Front nach Süden), wo Platz ist
        for (int side = 0; side < 2; side++) {
            Rotation rot = side == 0 ? Rotation.CLOCKWISE_180 : Rotation.NONE;
            int x = inner + 18;
            while (x < this.w - inner - 18) {
                boolean placed = false;
                for (int tries = 0; tries < cycle.length && !placed; tries++) {
                    BuildingType type = cycle[(k + tries) % cycle.length];
                    int design = this.hash(x, side, tries) % Math.max(1, type.designs());
                    BoundingBox local = type.blueprint(design).worldBounds(BlockPos.ZERO, rot);
                    int cz = side == 0 ? this.d - 1 - inner - local.maxZ() : inner + 14 - local.minZ();
                    int cx = x - local.minX();
                    BoundingBox box = new BoundingBox(cx + local.minX(), 0, cz + local.minZ(), cx + local.maxX(), 0, cz + local.maxZ());
                    if (local.getZSpan() < 24 && this.free(box)) {
                        this.stamp(type, design, cx, cz, rot, true);
                        x += local.getXSpan() + 3;
                        k = (k + tries + 1) % cycle.length;
                        placed = true;
                    }
                }
                if (!placed) {
                    x += 2;
                }
            }
        }
    }

    /**
     * Setzt ein Gebäude neben eine Kante: {@code side} −1 – seine rechte Seite liegt bei x, +1 – seine linke; in der Tiefe
     * mittig um z. Ist der Platz nicht frei, bleibt die Stelle leer.
     */
    private void beside(BuildingType type, int design, int x, int z, int side, Rotation rotation) {
        BoundingBox local = type.blueprint(design).worldBounds(BlockPos.ZERO, rotation);
        int cx = side < 0 ? x - local.maxX() : x - local.minX();
        int cz = z - (local.minZ() + local.maxZ()) / 2;
        BoundingBox box = new BoundingBox(cx + local.minX(), 0, cz + local.minZ(), cx + local.maxX(), 0, cz + local.maxZ());
        if (this.free(box)) {
            this.stamp(type, design, cx, cz, rotation, true);
        }
    }

    // =====================================================================================
    // Grün
    // =====================================================================================

    /**
     * Was im Hof frei bleibt, wird Garten und Wiese: Gemüsegärten an freien Stellen, dazu Eichen und Birken in lockerem
     * Abstand (dauerhaftes Laub).
     */
    private void greenery() {
        int inner = INSET + THICK + 3;
        // Gärten
        for (int z = inner + 16; z < this.d - inner - 12; z += 9) {
            for (int x = inner + 14; x < this.w - inner - 14; x += 11) {
                if (this.hash(x, 7, z) % 4 != 0) {
                    continue;
                }
                BoundingBox local = BuildingType.FIELD.blueprint(1).worldBounds(BlockPos.ZERO, Rotation.NONE);
                BoundingBox box = new BoundingBox(x + local.minX() - 1, 0, z + local.minZ() - 1, x + local.maxX() + 1, 0, z + local.maxZ() + 1);
                if (this.free(box)) {
                    this.stamp(BuildingType.FIELD, 1, x, z, Rotation.NONE, true);
                }
            }
        }
        // Bäume
        for (int z = inner + 4; z < this.d - inner - 4; z += 7) {
            for (int x = inner + 4; x < this.w - inner - 4; x += 7) {
                int h = this.hash(x, 3, z);
                if (h % 3 == 0) {
                    continue;
                }
                int tx = x + h % 5 - 2;
                int tz = z + (h >> 4) % 5 - 2;
                if (this.free(new BoundingBox(tx - 3, 0, tz - 3, tx + 3, 0, tz + 3))) {
                    this.tree(tx, tz, h % 4 == 0);
                    this.taken.add(new BoundingBox(tx - 3, 0, tz - 3, tx + 3, 0, tz + 3));
                }
            }
        }
    }

    /** Ein Baum: Stamm vier bis sechs hoch, eine runde Krone (Eiche oder Birke). */
    private void tree(int x, int z, boolean birch) {
        int h = 4 + this.hash(x, 9, z) % 3;
        Block log = birch ? Blocks.BIRCH_LOG : Blocks.OAK_LOG;
        Block leaves = birch ? Blocks.BIRCH_LEAVES : Blocks.OAK_LEAVES;
        for (int y = 0; y < h; y++) {
            this.put(x, y, z, log.defaultBlockState());
        }
        for (int y = h - 2; y <= h + 1; y++) {
            int r = y >= h + 1 ? 1 : 2;
            for (int dz = -r; dz <= r; dz++) {
                for (int dx = -r; dx <= r; dx++) {
                    if (dx == 0 && dz == 0 && y < h) {
                        continue;
                    }
                    boolean corner = Math.abs(dx) == r && Math.abs(dz) == r;
                    if (corner && (r == 1 || this.hash(x + dx, y, z + dz) % 2 == 0)) {
                        continue;
                    }
                    this.put(x + dx, y, z + dz, leaves.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true));
                }
            }
        }
    }

    // =====================================================================================
    // Laternen
    // =====================================================================================

    /** Laternen auf Pfosten beidseits des Wegs vom Tor zum Bergfried und rund um den Platz. */
    private void lights() {
        int inner = INSET + THICK;
        for (int z = inner + 14; z < this.keepZ - this.keepSize / 2 - 4; z += 8) {
            if (Math.abs(z - this.plaza) < 10) {
                continue;
            }
            for (int x : new int[] {this.gateX - 3, this.gateX + 3}) {
                this.post(x, z);
            }
        }
        for (int[] c : new int[][] {{-9, -9}, {9, -9}, {-9, 9}, {9, 9}}) {
            this.post(this.gateX + c[0], this.plaza + c[1]);
        }
    }

    private void post(int x, int z) {
        if (!this.at(x, 0, z).isAir() && !this.at(x, 0, z).is(Blocks.STRUCTURE_VOID)) {
            return;
        }
        this.put(x, 0, z, Blocks.DARK_OAK_FENCE.defaultBlockState());
        this.put(x, 1, z, Blocks.DARK_OAK_FENCE.defaultBlockState());
        this.put(x, 2, z, Blocks.LANTERN.defaultBlockState());
    }

    /** Für die Fehlersuche: unbenutzt. */
    @SuppressWarnings("unused")
    private static BlockState slab(Block block) {
        return block.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
    }
}
