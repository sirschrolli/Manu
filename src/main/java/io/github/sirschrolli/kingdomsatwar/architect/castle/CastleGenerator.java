package io.github.sirschrolli.kingdomsatwar.architect.castle;

import io.github.sirschrolli.kingdomsatwar.architect.io.Volume;
import io.github.sirschrolli.kingdomsatwar.economy.town.Blueprint;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jspecify.annotations.Nullable;

/**
 * Eine Burg, die der Dragon Architect selbst entwirft – als Volumen, das sich wie ein Gebäude vermessen lässt: eine
 * Ringmauer mit Wehrgang, Zinnen und Brustwehr auf einem Sockel aus Bruchstein, Ecktürme mit spitzen Schieferdächern,
 * Mauertürme mit Zinnenkranz, ein Torhaus mit Durchfahrt und Fallgitter zwischen zwei Tortürmen, ein Bergfried mit vier
 * Ecktürmchen, Stockwerken, Leiter, Fenstern und Laternen, gepflasterte Wege mit Laternen und im Hof die Häuser der
 * Siedlung.
 *
 * <p>Ab 200 × 200 Blöcken wird es eine <b>Königsburg</b>: in der Mitte eine Kernburg mit höherer Mauer, eigenen Türmen
 * und Torhaus, rundherum ein gemauerter Graben mit Steinbrücke und eine Ringstraße; im Burghof ein Prunkbrunnen, der
 * Palas mit dem Thronsaal, eine Kapelle mit spitzem Turm und hinter dem Bergfried der Königsgarten. Vor der Kernburg liegt
 * die Stadt mit Marktplatz, Hauptstraße, Querstraße und Gassen, an denen die Häuser stehen.
 *
 * <p>Koordinaten im Volumen: x nach rechts, z nach hinten (das Tor liegt vorn bei z = 0); die untersten Lagen liegen unter
 * der Bodenlinie (eine, bei der Königsburg vier für den Graben). Alles ist vorhersagbar (gleiche Größe und Saat – gleiche
 * Burg).
 */
public final class CastleGenerator {
    /** Abstand der Mauer vom Rand (dort ragen die Türme hinaus). */
    static final int INSET = 5;
    /** Dicke der Ringmauer. */
    static final int THICK = 3;
    /** Höhe der Ringmauer (Lagen über dem Boden; der Wehrgang liegt oben auf). */
    static final int WALL = 10;
    /** Ab dieser Breite und Tiefe entwirft der Architekt eine Königsburg. */
    public static final int ROYAL = 200;
    /** Größe der eingebauten Königsburg. */
    public static final int ROYAL_SIZE = 300;
    /** Saat der eingebauten Königsburg. */
    public static final long ROYAL_SEED = 1271L;
    /** Höhe der Mauer der Kernburg. */
    static final int INNER_WALL = 14;
    /** Abstand der Futtermauer des Grabens von der Kernburg (dazwischen die Berme mit den Türmen). */
    static final int ESCARP = 5;
    /** Abstand der Gegenmauer des Grabens. */
    static final int COUNTERSCARP = 11;
    /** Bis hierhin reicht die Ringstraße um den Graben. */
    static final int RING_ROAD = 14;
    /** Halbe Seitenlänge des Marktplatzes. */
    static final int MARKET = 13;

    /** Eine entworfene Burg: das Volumen und wie viele Lagen darin unter der Bodenlinie liegen. */
    public record Castle(Volume volume, int ground) {}

    /** Ein Mauerring: Außenkanten (x0, z0) bis (x1, z1), Dicke und Höhe (Lagen über dem Boden). */
    private record Ring(int x0, int z0, int x1, int z1, int thick, int height) {
        /** Abstand von der Außenkante nach innen (negativ: außerhalb). */
        int edge(int x, int z) {
            return Math.min(Math.min(x - this.x0, z - this.z0), Math.min(this.x1 - x, this.z1 - z));
        }

        boolean wall(int x, int z) {
            int e = this.edge(x, z);
            return e >= 0 && e < this.thick;
        }

        /** Abstand außerhalb des Rings (größte Achse; 0 oder weniger: innerhalb). */
        int outside(int x, int z) {
            return Math.max(Math.max(this.x0 - x, x - this.x1), Math.max(this.z0 - z, z - this.z1));
        }

        int midX() {
            return (this.x0 + this.x1) / 2;
        }

        int midZ() {
            return (this.z0 + this.z1) / 2;
        }
    }

    /** Wo Gebäude stehen dürfen: ein Rechteck, ohne das Loch (die Kernburg mit Graben), falls es eines gibt. */
    private record Ward(int x0, int z0, int x1, int z1, @Nullable BoundingBox hole) {
        boolean holds(BoundingBox b) {
            return b.minX() >= this.x0 && b.minZ() >= this.z0 && b.maxX() <= this.x1 && b.maxZ() <= this.z1
                    && (this.hole == null || !this.hole.intersects(b.minX(), b.minZ(), b.maxX(), b.maxZ()));
        }
    }

    /** Ein Gebäude im Zyklus einer Häuserzeile: Art und Entwurf (−1: einer nach der Saat). */
    private record Kind(BuildingType type, int design) {}

    /** Häuser an den großen Straßen und am Markt. */
    private static final Kind[] MAIN = {new Kind(BuildingType.TOWNHOUSE, -1), new Kind(BuildingType.TAVERN, -1),
            new Kind(BuildingType.TOWNHOUSE, -1), new Kind(BuildingType.MARKET, 1), new Kind(BuildingType.TOWNHOUSE, -1),
            new Kind(BuildingType.SMITHY, -1), new Kind(BuildingType.TOWNHOUSE, -1), new Kind(BuildingType.MAP_HOUSE, 0)};
    /** Häuser an den Gassen und an der Mauer. */
    private static final Kind[] TOWN = {new Kind(BuildingType.HOUSE, -1), new Kind(BuildingType.TOWNHOUSE, -1),
            new Kind(BuildingType.HOUSE, -1), new Kind(BuildingType.STOREHOUSE, -1), new Kind(BuildingType.HOUSE, -1),
            new Kind(BuildingType.SMITHY, -1), new Kind(BuildingType.TOWNHOUSE, -1), new Kind(BuildingType.LUMBER_HUT, -1),
            new Kind(BuildingType.HOUSE, -1), new Kind(BuildingType.TAVERN, -1)};
    /** Häuser im Hof der Kernburg. */
    private static final Kind[] COURT = {new Kind(BuildingType.BARRACKS, 0), new Kind(BuildingType.STOREHOUSE, 1),
            new Kind(BuildingType.SMITHY, 1), new Kind(BuildingType.TOWNHOUSE, 1), new Kind(BuildingType.MAP_HOUSE, 1),
            new Kind(BuildingType.TAVERN, 1), new Kind(BuildingType.BARRACKS, 1), new Kind(BuildingType.TOWNHOUSE, 2),
            new Kind(BuildingType.STOREHOUSE, 0)};

    private final Volume v;
    private final int w;
    private final int d;
    private final long seed;
    /** Lagen unter der Bodenlinie im Volumen. */
    private final int depth;
    private final int gateX;
    private final int keepSize;
    private final int keepHeight;
    private final int keepX;
    private final int keepZ;
    private final Ring outer;
    /** Die Kernburg (nur bei der Königsburg). */
    private final @Nullable Ring inner;
    private final Ward outerWard;
    private final @Nullable Ward innerWard;
    /** Was schon belegt ist (Grundfläche, mit Abstand) – dort steht kein weiteres Haus. */
    private final List<BoundingBox> taken = new ArrayList<>();
    /** Die Straßen der Stadt (ohne Gehweg), an denen Häuser stehen. */
    private final List<BoundingBox> streets = new ArrayList<>();
    /** Mitte des Platzes (z). */
    private int plaza;

    private CastleGenerator(int w, int d, long seed) {
        this.w = w;
        this.d = d;
        this.seed = seed;
        boolean royal = w >= ROYAL && d >= ROYAL;
        this.depth = royal ? 4 : 1;
        this.keepSize = royal ? 24 : 18;
        this.keepHeight = royal ? 34 : 26;
        this.v = new Volume(w, this.depth + this.keepHeight + 14, d);
        this.gateX = w / 2;
        this.keepX = w / 2;
        this.outer = new Ring(INSET, INSET, w - 1 - INSET, d - 1 - INSET, THICK, WALL);
        int yard = INSET + THICK + 2;
        if (royal) {
            int iw = (w * 48 / 100) | 1;
            int id = (d * 42 / 100) | 1;
            int z1 = d - 1 - INSET - THICK - 20;
            Ring in = new Ring(this.gateX - iw / 2, z1 - id + 1, this.gateX + iw / 2, z1, THICK, INNER_WALL);
            this.inner = in;
            this.keepZ = in.z0() + id / 2 + 4;
            BoundingBox complex = new BoundingBox(in.x0() - RING_ROAD, 0, in.z0() - RING_ROAD, in.x1() + RING_ROAD, 0, in.z1() + RING_ROAD);
            this.outerWard = new Ward(yard, yard, w - 1 - yard, d - 1 - yard, complex);
            this.innerWard = new Ward(in.x0() + THICK + 2, in.z0() + THICK + 2, in.x1() - THICK - 2, in.z1() - THICK - 2, null);
        } else {
            this.inner = null;
            this.innerWard = null;
            this.keepZ = d - INSET - THICK - 12 - this.keepSize / 2;
            this.outerWard = new Ward(yard, yard, w - 1 - yard, d - 1 - yard, null);
        }
    }

    /** Entwirft eine Burg dieser Größe (Breite und Tiefe 96 bis 320; ab 200 eine Königsburg). */
    public static Castle generate(int width, int depth, long seed) {
        CastleGenerator g = new CastleGenerator(Math.max(96, Math.min(320, width)), Math.max(96, Math.min(320, depth)), seed);
        if (g.inner != null) {
            g.royal();
        } else {
            g.paths();
            g.ringWall(g.outer);
            g.towers(g.outer, 11, WALL + 9, 9, WALL + 6, 40, 33, -1, false);
            g.gatehouse(g.outer, g.gateX, 8, 11, 7, true);
            g.keep();
            g.buildings();
            g.lights();
            g.greenery(g.outerWard, false);
        }
        Shapes.apply(g.v);
        return new Castle(g.v, g.depth);
    }

    /** Die eingebaute Königsburg (300 × 300 Blöcke). */
    public static Castle royalCastle() {
        return generate(ROYAL_SIZE, ROYAL_SIZE, ROYAL_SEED);
    }

    // =====================================================================================
    // Grundlagen
    // =====================================================================================

    /** Setzt einen Block (y über der Bodenlinie; −1 ist die oberste Lage Gelände). */
    private void put(int x, int y, int z, BlockState state) {
        this.v.set(x, y + this.depth, z, state);
    }

    private BlockState at(int x, int y, int z) {
        return this.v.getBlockState(new BlockPos(x, y + this.depth, z));
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

    private static BlockState stairs(Block block, Direction facing, Half half) {
        return block.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
    }

    private static BlockState hanging() {
        return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
    }

    private static BlockState leaves(Block block) {
        return block.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    }

    // =====================================================================================
    // Wege (kleine Burg)
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

    // =====================================================================================
    // Mauern
    // =====================================================================================

    /**
     * Ein Mauerring: Sockel aus Bruchstein, Steinziegel, oben der Wehrgang mit Zinnen nach außen (zwei Steine, eine Lücke)
     * und einer Brustwehr nach innen.
     */
    private void ringWall(Ring r) {
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        for (int z = r.z0(); z <= r.z1(); z++) {
            for (int x = r.x0(); x <= r.x1(); x++) {
                if (!r.wall(x, z)) {
                    continue;
                }
                int e = r.edge(x, z);
                for (int y = 0; y < r.height(); y++) {
                    this.put(x, y, z, this.stone(x, y, z));
                }
                this.put(x, -1, z, cobble);
                if (e == 0) {
                    // Sockel: außen eine Lage vorgesetzt, oben mit einer Schräge aus Stufen
                    int ox = x == r.x0() ? x - 1 : x == r.x1() ? x + 1 : x;
                    int oz = z == r.z0() ? z - 1 : z == r.z1() ? z + 1 : z;
                    if (ox != x || oz != z) {
                        this.put(ox, -1, oz, cobble);
                        this.put(ox, 0, oz, this.stone(ox, 0, oz));
                        this.put(ox, 1, oz, this.stone(ox, 1, oz));
                        Direction toWall = ox < x ? Direction.EAST : ox > x ? Direction.WEST : oz < z ? Direction.SOUTH : Direction.NORTH;
                        if ((ox != x) != (oz != z)) {
                            this.put(ox, 2, oz, stairs(Blocks.STONE_BRICK_STAIRS, toWall, Half.BOTTOM));
                        }
                    }
                    if ((x + z) % 3 != 0) {
                        this.put(x, r.height(), z, this.stone(x, r.height(), z));
                    }
                    // Gesims unter den Zinnen
                    if (this.hash(x, r.height() - 2, z) % 3 == 0) {
                        this.put(x, r.height() - 2, z, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
                    }
                } else if (e == r.thick() - 1) {
                    this.put(x, r.height(), z, Blocks.STONE_BRICK_WALL.defaultBlockState());
                }
            }
        }
    }

    /** Treppen zum Wehrgang an der Innenseite der vorderen Mauer, links und rechts des Tors (ab {@code offset}). */
    private void wallStairs(Ring r, int gx, int offset) {
        int inner = r.z0() + r.thick();
        for (int side = -1; side <= 1; side += 2) {
            int x0 = gx + side * offset;
            for (int i = 0; i < r.height(); i++) {
                int x = x0 + side * i;
                for (int z = inner; z < inner + 2; z++) {
                    this.put(x, i, z, stairs(Blocks.STONE_BRICK_STAIRS, side > 0 ? Direction.EAST : Direction.WEST, Half.BOTTOM));
                    for (int y = 0; y < i; y++) {
                        this.put(x, y, z, this.stone(x, y, z));
                    }
                }
            }
            // Lücke in der Brustwehr oben an der Treppe
            int top = x0 + side * (r.height() - 1);
            this.put(top, r.height(), inner - 1, Blocks.AIR.defaultBlockState());
            this.put(top + side, r.height(), inner - 1, Blocks.AIR.defaultBlockState());
            this.taken.add(new BoundingBox(Math.min(x0, top) - 1, 0, inner, Math.max(x0, top) + 1, 0, inner + 3));
        }
    }

    // =====================================================================================
    // Türme
    // =====================================================================================

    /**
     * Ecktürme (mit spitzem Dach) und Mauertürme (mit Zinnenkranz) in gleichen Abständen entlang eines Rings, links und
     * rechts gespiegelt; vor dem Tor bleibt Platz. {@code flags}: Fahnen auf den Ecktürmen.
     */
    private void towers(Ring r, int cs, int ch, int ms, int mh, int spacing, int gateClear, int cornerFoot, boolean flags) {
        int ax = r.x0() + 1;
        int bx = r.x1() - 1;
        int az = r.z0() + 1;
        int bz = r.z1() - 1;
        for (int[] c : new int[][] {{ax, az}, {bx, az}, {ax, bz}, {bx, bz}}) {
            this.tower(c[0], c[1], cs, ch, true, r, cornerFoot);
            if (flags) {
                this.put(c[0], ch + cs / 2 + 3, c[1], Blocks.BANNER.red().defaultBlockState());
            }
        }
        for (int s = 0; s < 4; s++) {
            boolean alongX = s < 2;
            int a = alongX ? ax : az;
            int b = alongX ? bx : bz;
            int n = Math.max(0, (b - a) / spacing - 1);
            for (int i = 1; i <= n; i++) {
                int p = i * 2 <= n + 1 ? a + (b - a) * i / (n + 1) : a + b - (a + (b - a) * (n + 1 - i) / (n + 1));
                int x = alongX ? p : s == 2 ? ax : bx;
                int z = alongX ? s == 0 ? az : bz : p;
                if (s == 0 && Math.abs(x - this.gateX) < gateClear) {
                    continue; // dort steht das Torhaus
                }
                this.tower(x, z, ms, mh, false, r, -1);
            }
        }
    }

    /**
     * Ein viereckiger Turm um (cx, cz): Mauerring, Stockwerke, Schießscharten, eine Tür zum Hof, Durchgänge zum Wehrgang,
     * Leiter, Dach oder Zinnen. {@code foot}: so tief reicht das Mauerwerk (unter der Bodenlinie, etwa in den Graben).
     */
    private void tower(int cx, int cz, int size, int height, boolean roof, Ring r, int foot) {
        int rad = size / 2;
        // zur Mitte des Rings: dort liegt die Tür
        Direction in = Math.abs(cx - r.midX()) > Math.abs(cz - r.midZ()) ? (cx < r.midX() ? Direction.EAST : Direction.WEST)
                : (cz < r.midZ() ? Direction.SOUTH : Direction.NORTH);
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int y = foot; y < height; y++) {
            for (int dz = -rad; dz <= rad; dz++) {
                for (int dx = -rad; dx <= rad; dx++) {
                    int x = cx + dx;
                    int z = cz + dz;
                    boolean ring = Math.abs(dx) == rad || Math.abs(dz) == rad;
                    if (y < 0) {
                        this.put(x, y, z, ring ? this.stone(x, y, z) : Blocks.COBBLESTONE.defaultBlockState());
                    } else if (ring) {
                        this.put(x, y, z, this.stone(x, y, z));
                    } else if (y == 0 || y == r.height() - 1 || y == height - 1) {
                        this.put(x, y, z, (y == height - 1 ? Blocks.STONE_BRICKS : Blocks.SPRUCE_PLANKS).defaultBlockState());
                    } else {
                        this.put(x, y, z, air);
                    }
                }
            }
        }
        // Schießscharten in jeder Seite (unten und über dem Wehrgang)
        for (Direction f : Direction.Plane.HORIZONTAL) {
            for (int y : new int[] {3, 4, r.height() + 2, r.height() + 3}) {
                if (y < height - 1) {
                    this.put(cx + f.getStepX() * rad, y, cz + f.getStepZ() * rad, air);
                }
            }
        }
        // Tür zum Hof: mitten in der Hofseite – bei Ecktürmen so weit zur Seite gerückt, dass sie nicht in die Mauer führt
        int sx = in.getAxis() == Direction.Axis.Z ? (cx < r.midX() ? 1 : -1) : 0;
        int sz = in.getAxis() == Direction.Axis.X ? (cz < r.midZ() ? 1 : -1) : 0;
        int k = 0;
        while (k < rad - 1 && r.edge(cx + in.getStepX() * (rad + 1) + sx * k, cz + in.getStepZ() * (rad + 1) + sz * k) < r.thick()) {
            k++;
        }
        int doorX = cx + in.getStepX() * rad + sx * k;
        int doorZ = cz + in.getStepZ() * rad + sz * k;
        this.put(doorX, 1, doorZ, Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, in));
        this.put(doorX, 2, doorZ, Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, in).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        this.put(doorX + in.getStepX(), 0, doorZ + in.getStepZ(), stairs(Blocks.STONE_BRICK_STAIRS, in.getOpposite(), Half.BOTTOM));
        // Durchgänge zum Wehrgang, wo die Mauer anschließt
        for (Direction f : Direction.Plane.HORIZONTAL) {
            if (r.wall(cx + f.getStepX() * (rad + 1), cz + f.getStepZ() * (rad + 1))) {
                int x = cx + f.getStepX() * rad;
                int z = cz + f.getStepZ() * rad;
                this.put(x, r.height(), z, air);
                this.put(x, r.height() + 1, z, air);
            }
        }
        // Leiter innen an der Wand gegenüber der Tür
        Direction back = in.getOpposite();
        int lx = cx + back.getStepX() * (rad - 1);
        int lz = cz + back.getStepZ() * (rad - 1);
        for (int y = 1; y < height; y++) {
            this.put(lx, y, lz, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, in));
        }
        // Laterne unter der Decke des Erdgeschosses
        this.put(cx, r.height() - 2, cz, hanging());
        if (roof) {
            this.pyramid(cx, cz, rad, height, true);
        } else {
            this.crown(cx, cz, rad, height);
        }
        this.taken.add(new BoundingBox(cx - rad - 2, 0, cz - rad - 2, cx + rad + 2, 0, cz + rad + 2));
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
     * Ein Torhaus in der vorderen Mauer eines Rings: ein Block über der Durchfahrt (fünf breit, sieben hoch, mit Bogen und
     * hochgezogenem Fallgitter), zwei Tortürme mit Dach, darüber eine Wachstube mit Fenstern und Zinnen, Banner über dem
     * Tor; {@code stairs}: dazu Treppen zum Wehrgang hinter den Tortürmen.
     */
    private void gatehouse(Ring r, int gx, int half, int deep, int ts, boolean stairs) {
        int x0 = gx - half;
        int x1 = gx + half;
        int z0 = r.z0();
        int z1 = r.z0() + deep - 1;
        int floor = r.height() - 3;
        int top = r.height() + 4;
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int y = -1; y < top; y++) {
            for (int z = z0; z <= z1; z++) {
                for (int x = x0; x <= x1; x++) {
                    boolean ring = x == x0 || x == x1 || z == z0 || z == z1;
                    if (y == -1) {
                        this.put(x, y, z, Blocks.COBBLESTONE.defaultBlockState());
                    } else if (ring || y <= floor || y == top - 1) {
                        this.put(x, y, z, this.stone(x, y, z));
                    } else {
                        this.put(x, y, z, air);
                    }
                }
            }
        }
        // Durchfahrt; auch der Sockel davor wird frei
        for (int z = z0 - 1; z <= z1; z++) {
            for (int x = gx - 2; x <= gx + 2; x++) {
                for (int y = 0; y < 7; y++) {
                    boolean corner = y == 6 && z >= z0 && (x == gx - 2 || x == gx + 2);
                    this.put(x, y, z, corner ? stairs(Blocks.STONE_BRICK_STAIRS, x < gx ? Direction.EAST : Direction.WEST, Half.TOP) : air);
                }
                this.put(x, -1, z, this.paving(x, z));
            }
        }
        // hochgezogenes Fallgitter
        for (int x = gx - 1; x <= gx + 1; x++) {
            this.put(x, 5, z0 + 1, Blocks.IRON_BARS.defaultBlockState());
            this.put(x, 6, z0 + 1, Blocks.IRON_BARS.defaultBlockState());
        }
        // Fenster der Wachstube, Laternen in Wachstube und Durchfahrt
        for (int x = x0 + 2; x <= x1 - 2; x += 3) {
            if (Math.abs(x - gx) > 1) {
                this.put(x, floor + 2, z0, Blocks.GLASS_PANE.defaultBlockState());
                this.put(x, floor + 2, z1, Blocks.GLASS_PANE.defaultBlockState());
            }
        }
        this.put(gx, top - 2, z0 + deep / 2, hanging());
        this.put(gx, 6, z0 + deep / 2, hanging());
        // Zinnen
        for (int z = z0; z <= z1; z++) {
            for (int x = x0; x <= x1; x++) {
                if ((x == x0 || x == x1 || z == z0 || z == z1) && (x + z) % 2 == 0) {
                    this.put(x, top, z, this.stone(x, top, z));
                }
            }
        }
        // Zugang von der Wachstube zum Wehrgang
        for (int x : new int[] {x0, x1}) {
            this.put(x, r.height(), z0 + 1, air);
            this.put(x, r.height() + 1, z0 + 1, air);
        }
        // Tortürme
        int tr = ts / 2;
        this.tower(x0 - tr, z0 + 2, ts, top + 4, true, r, -1);
        this.tower(x1 + tr, z0 + 2, ts, top + 4, true, r, -1);
        // Banner über dem Tor
        for (int x : new int[] {gx - 3, gx + 3}) {
            this.put(x, 8, z0 - 1, Blocks.WALL_BANNER.red().defaultBlockState().setValue(WallBannerBlock.FACING, Direction.NORTH));
        }
        if (stairs) {
            this.wallStairs(r, gx, half + ts + 2);
        }
        this.taken.add(new BoundingBox(x0 - ts - 1, 0, z0 - 1, x1 + ts + 1, 0, z1 + 4));
    }

    // =====================================================================================
    // Bergfried
    // =====================================================================================

    /**
     * Der Bergfried: Mauern zwei Steine dick auf einem Sockel mit Schräge, an jedem Stockwerk ein Gurtgesims, je Seite drei
     * Fenster mit Sturz, ein Portal mit Rahmen, Stufen und zwei Bannern, innen Böden, Leiter, Laternen und ein paar Möbel;
     * oben ein vorgekragter Wehrgang mit Brustwehr und Zinnen, vier Ecktürmchen mit spitzen Dächern und in der Mitte eine
     * Fahne.
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
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        for (int y = -1; y < h; y++) {
            for (int z = z0; z <= z1; z++) {
                for (int x = x0; x <= x1; x++) {
                    int e = Math.min(Math.min(x - x0, x1 - x), Math.min(z - z0, z1 - z));
                    if (y == -1) {
                        this.put(x, y, z, cobble);
                    } else if (e <= 1) {
                        this.put(x, y, z, e == 0 && y > 0 && y % 6 == 0 && y < h - 1 ? Blocks.POLISHED_ANDESITE.defaultBlockState() : this.stone(x, y, z));
                    } else if (y % 6 == 0 || y == h - 1) {
                        this.put(x, y, z, (y == h - 1 ? Blocks.STONE_BRICKS : Blocks.SPRUCE_PLANKS).defaultBlockState());
                    } else {
                        this.put(x, y, z, air);
                    }
                }
            }
        }
        // Sockel: ringsum eine Lage vorgesetzt, oben eine Schräge aus Stufen (vor der Tür die Stufen hinauf)
        for (int z = z0 - 1; z <= z1 + 1; z++) {
            for (int x = x0 - 1; x <= x1 + 1; x++) {
                boolean edgeX = x == x0 - 1 || x == x1 + 1;
                boolean edgeZ = z == z0 - 1 || z == z1 + 1;
                if (!edgeX && !edgeZ || z == z0 - 1 && (x == this.keepX - 1 || x == this.keepX)) {
                    continue;
                }
                this.put(x, -1, z, cobble);
                this.put(x, 0, z, this.stone(x, 0, z));
                this.put(x, 1, z, this.stone(x, 1, z));
                if (edgeX != edgeZ) {
                    Direction in = edgeX ? (x < this.keepX ? Direction.EAST : Direction.WEST) : (z < this.keepZ ? Direction.SOUTH : Direction.NORTH);
                    this.put(x, 2, z, stairs(Blocks.STONE_BRICK_STAIRS, in, Half.BOTTOM));
                }
            }
        }
        // Fenster: je Stockwerk und Seite drei – in der Mitte zwei Scheiben breit, seitlich schmal –, drei hoch, mit Sturz
        int q = r / 2;
        int[] offsets = {-1 - q, -1, 0, q};
        for (int f = 6; f + 6 < h; f += 6) {
            for (int o : offsets) {
                for (int y = f + 2; y <= f + 4; y++) {
                    this.window(this.keepX + o, y, z0, 0, 1);
                    this.window(this.keepX + o, y, z1, 0, -1);
                    this.window(x0, y, this.keepZ + o, 1, 0);
                    this.window(x1, y, this.keepZ + o, -1, 0);
                }
                BlockState lintel = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
                this.put(this.keepX + o, f + 5, z0, lintel);
                this.put(this.keepX + o, f + 5, z1, lintel);
                this.put(x0, f + 5, this.keepZ + o, lintel);
                this.put(x1, f + 5, this.keepZ + o, lintel);
            }
            this.put(this.keepX, f - 1, this.keepZ, hanging());
        }
        // Schießscharten im Erdgeschoss (nicht vorn)
        for (int o : new int[] {-1 - q, q}) {
            for (int y = 2; y <= 3; y++) {
                this.put(this.keepX + o, y, z1, air);
                this.put(x0, y, this.keepZ + o, air);
                this.put(x1, y, this.keepZ + o, air);
            }
        }
        // Portal vorn (zum Tor hin): Doppeltür mit Rahmen, Stufen davor, darüber zwei Banner
        for (int k = 0; k < 2; k++) {
            DoorHingeSide hinge = k == 0 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
            this.put(this.keepX - k, 1, z0, Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HINGE, hinge));
            this.put(this.keepX - k, 2, z0, Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH)
                    .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER).setValue(DoorBlock.HINGE, hinge));
            this.put(this.keepX - k, 1, z0 + 1, air);
            this.put(this.keepX - k, 2, z0 + 1, air);
            this.put(this.keepX - k, 0, z0 - 1, stairs(Blocks.STONE_BRICK_STAIRS, Direction.SOUTH, Half.BOTTOM));
            this.put(this.keepX - k, -1, z0 - 1, cobble);
            this.put(this.keepX - k, 4, z0, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        }
        BlockState frame = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
        for (int y = 1; y <= 3; y++) {
            this.put(this.keepX - 2, y, z0, frame);
            this.put(this.keepX + 1, y, z0, frame);
        }
        this.put(this.keepX - 1, 3, z0, frame);
        this.put(this.keepX, 3, z0, frame);
        for (int x : new int[] {this.keepX - 3 - q / 2, this.keepX + 2 + q / 2}) {
            this.put(x, 9, z0 - 1, Blocks.WALL_BANNER.red().defaultBlockState().setValue(WallBannerBlock.FACING, Direction.NORTH));
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
        this.put(x1 - 3, 13, z1 - 3, Blocks.BED.red().defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH).setValue(BedBlock.PART, BedPart.HEAD));
        this.put(x1 - 5, 13, z1 - 3, Blocks.BOOKSHELF.defaultBlockState());
        this.put(x1 - 6, 13, z1 - 3, Blocks.BOOKSHELF.defaultBlockState());
        // Wehrgang: ringsum eine Lage vorgekragt (auf umgedrehten Stufen), darauf Brustwehr und Zinnen
        for (int z = z0 - 1; z <= z1 + 1; z++) {
            for (int x = x0 - 1; x <= x1 + 1; x++) {
                boolean edgeX = x == x0 - 1 || x == x1 + 1;
                boolean edgeZ = z == z0 - 1 || z == z1 + 1;
                if (!edgeX && !edgeZ) {
                    continue;
                }
                if (edgeX != edgeZ) {
                    Direction in = edgeX ? (x < this.keepX ? Direction.EAST : Direction.WEST) : (z < this.keepZ ? Direction.SOUTH : Direction.NORTH);
                    this.put(x, h - 2, z, stairs(Blocks.STONE_BRICK_STAIRS, in, Half.TOP));
                }
                this.put(x, h - 1, z, this.stone(x, h - 1, z));
                this.put(x, h, z, this.stone(x, h, z));
                if ((x + z) % 2 == 0) {
                    this.put(x, h + 1, z, this.stone(x, h + 1, z));
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

    /** Ein Fenster in einer zwei Steine dicken Mauer: außen eine Scheibe, dahinter (Schritt dx, dz nach innen) offen. */
    private void window(int x, int y, int z, int dx, int dz) {
        this.put(x, y, z, Blocks.GLASS_PANE.defaultBlockState());
        this.put(x + dx, y, z + dz, Blocks.AIR.defaultBlockState());
    }

    // =====================================================================================
    // Häuser
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
                this.put(p.getX(), p.getY(), p.getZ(), common(Blueprint.world(e.state(), rotation)));
            }
        }
        BoundingBox box = plan.worldBounds(origin, rotation);
        if (mark) {
            this.taken.add(box.inflatedBy(2));
        }
        return box;
    }

    /**
     * Was in den Häusern der Siedlung kostbar wäre (Ambosse, Kessel, Hochöfen, Braustände, Glocken, Goldblöcke), wird für
     * die Burg durch Ähnliches aus gewöhnlichen Baustoffen ersetzt – so bauen die Baumeister alles aus Holz, Stein und
     * Metall; nur der Thron im Palas muss im Lager liegen.
     */
    private static BlockState common(BlockState state) {
        Block b = state.getBlock();
        if (state.is(net.minecraft.tags.BlockTags.ANVIL)) {
            return Blocks.GRINDSTONE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.GrindstoneBlock.FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING))
                    .setValue(net.minecraft.world.level.block.GrindstoneBlock.FACE, net.minecraft.world.level.block.state.properties.AttachFace.FLOOR);
        }
        if (b instanceof net.minecraft.world.level.block.AbstractCauldronBlock) {
            return Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING, Direction.UP);
        }
        if (b == Blocks.BLAST_FURNACE) {
            return Blocks.FURNACE.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
        if (b == Blocks.BREWING_STAND) {
            return Blocks.POTTED_RED_TULIP.defaultBlockState();
        }
        if (b == Blocks.BELL) {
            return state.getValue(BellBlock.ATTACHMENT) == BellAttachType.FLOOR ? Blocks.LANTERN.defaultBlockState() : hanging();
        }
        if (b == Blocks.GOLD_BLOCK) {
            return Blocks.CUT_COPPER.waxed().unaffected().defaultBlockState();
        }
        return state;
    }

    private boolean free(BoundingBox box, Ward ward) {
        if (!ward.holds(box)) {
            return false;
        }
        for (BoundingBox t : this.taken) {
            if (t.intersects(box.minX(), box.minZ(), box.maxX(), box.maxZ())) {
                return false;
            }
        }
        return true;
    }

    private static Rotation facing(Direction front) {
        return switch (front) {
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    /**
     * Eine Häuserzeile: Gebäude aus dem Zyklus nebeneinander, die Vorderseite in Richtung {@code facing}. {@code line} ist
     * die Linie der Vorderseiten ({@code front}) oder der Rückseiten; entlang geht es von {@code from} bis {@code to}.
     * Nur, wo Platz ist; {@code k} merkt sich die Stelle im Zyklus.
     */
    private void row(Kind[] cycle, int[] k, Direction facing, int line, boolean front, int from, int to, int maxDepth, Ward ward) {
        Rotation rot = facing(facing);
        boolean alongX = facing.getAxis() == Direction.Axis.Z;
        int pos = from;
        while (pos + 4 <= to) {
            boolean placed = false;
            for (int tries = 0; tries < cycle.length && !placed; tries++) {
                Kind kind = cycle[(k[0] + tries) % cycle.length];
                int design = kind.design() >= 0 ? kind.design() : this.hash(pos, line, tries) % Math.max(1, kind.type().designs());
                BoundingBox local = kind.type().blueprint(design).worldBounds(BlockPos.ZERO, rot);
                int across = alongX ? local.getZSpan() : local.getXSpan();
                int length = alongX ? local.getXSpan() : local.getZSpan();
                if (across > maxDepth || pos + length - 1 > to) {
                    continue;
                }
                int c = switch (facing) {
                    case NORTH -> front ? line - local.minZ() : line - local.maxZ();
                    case SOUTH -> front ? line - local.maxZ() : line - local.minZ();
                    case EAST -> front ? line - local.maxX() : line - local.minX();
                    default -> front ? line - local.minX() : line - local.maxX();
                };
                int cx = alongX ? pos - local.minX() : c;
                int cz = alongX ? c : pos - local.minZ();
                BoundingBox box = new BoundingBox(cx + local.minX(), 0, cz + local.minZ(), cx + local.maxX(), 0, cz + local.maxZ());
                if (this.free(box, ward)) {
                    this.stamp(kind.type(), design, cx, cz, rot, true);
                    pos += length + 3;
                    k[0] = (k[0] + tries + 1) % cycle.length;
                    placed = true;
                }
            }
            if (!placed) {
                pos += 2;
            }
        }
    }

    /** Häuserzeilen beidseits einer Straße, die Vorderseiten zur Straße. */
    private void alongStreet(BoundingBox s, Kind[] cycle, int[] k, int maxDepth, Ward ward) {
        if (s.getXSpan() > s.getZSpan()) {
            this.row(cycle, k, Direction.SOUTH, s.minZ() - 2, true, s.minX(), s.maxX(), maxDepth, ward);
            this.row(cycle, k, Direction.NORTH, s.maxZ() + 2, true, s.minX(), s.maxX(), maxDepth, ward);
        } else {
            this.row(cycle, k, Direction.EAST, s.minX() - 2, true, s.minZ(), s.maxZ(), maxDepth, ward);
            this.row(cycle, k, Direction.WEST, s.maxX() + 2, true, s.minZ(), s.maxZ(), maxDepth, ward);
        }
    }

    /**
     * Die Häuser der kleinen Burg: zuerst Palas, Kaserne, Markt und Taverne an die besten Stellen, dann Reihen von
     * Bürgerhäusern, Werkstätten und Lagern entlang der Mauern, jedes mit der Vorderseite zum Hof.
     */
    private void buildings() {
        Ward ward = this.outerWard;
        int r = this.keepSize / 2;
        this.beside(BuildingType.THRONE_HALL, 0, this.keepX - r - 7, this.keepZ, -1, Rotation.NONE, ward);
        this.beside(BuildingType.BARRACKS, 0, this.keepX + r + 7, this.keepZ, 1, Rotation.NONE, ward);
        this.beside(BuildingType.MARKET, 1, this.gateX - 11, this.plaza - 13, -1, Rotation.CLOCKWISE_180, ward);
        this.beside(BuildingType.TAVERN, 1, this.gateX + 11, this.plaza - 13, 1, Rotation.CLOCKWISE_180, ward);
        this.beside(BuildingType.TOWNHOUSE, 2, this.gateX - 11, this.plaza + 14, -1, Rotation.NONE, ward);
        this.beside(BuildingType.SMITHY, 1, this.gateX + 11, this.plaza + 14, 1, Rotation.NONE, ward);
        Kind[] cycle = {new Kind(BuildingType.TOWNHOUSE, -1), new Kind(BuildingType.HOUSE, -1), new Kind(BuildingType.SMITHY, -1),
                new Kind(BuildingType.HOUSE, -1), new Kind(BuildingType.STOREHOUSE, -1), new Kind(BuildingType.TOWNHOUSE, -1),
                new Kind(BuildingType.HOUSE, -1), new Kind(BuildingType.MAP_HOUSE, -1), new Kind(BuildingType.HOUSE, -1),
                new Kind(BuildingType.LUMBER_HUT, -1), new Kind(BuildingType.TOWNHOUSE, -1), new Kind(BuildingType.HOUSE, -1),
                new Kind(BuildingType.FIELD, -1), new Kind(BuildingType.HOUSE, -1)};
        int inner = INSET + THICK + 4;
        int[] k = {0};
        this.row(cycle, k, Direction.EAST, inner, false, inner + 20, this.d - inner - 10, 23, ward);
        this.row(cycle, k, Direction.WEST, this.w - 1 - inner, false, inner + 20, this.d - inner - 10, 23, ward);
        this.row(cycle, k, Direction.NORTH, this.d - 1 - inner, false, inner + 18, this.w - inner - 18, 23, ward);
        this.row(cycle, k, Direction.SOUTH, inner + 14, false, inner + 18, this.w - inner - 18, 23, ward);
    }

    /**
     * Setzt ein Gebäude neben eine Kante: {@code side} −1 – seine rechte Seite liegt bei x, +1 – seine linke; in der Tiefe
     * mittig um z. Ist der Platz nicht frei, bleibt die Stelle leer.
     */
    private void beside(BuildingType type, int design, int x, int z, int side, Rotation rotation, Ward ward) {
        BoundingBox local = type.blueprint(design).worldBounds(BlockPos.ZERO, rotation);
        int cx = side < 0 ? x - local.maxX() : x - local.minX();
        int cz = z - (local.minZ() + local.maxZ()) / 2;
        BoundingBox box = new BoundingBox(cx + local.minX(), 0, cz + local.minZ(), cx + local.maxX(), 0, cz + local.maxZ());
        if (this.free(box, ward)) {
            this.stamp(type, design, cx, cz, rotation, true);
        }
    }

    // =====================================================================================
    // Königsburg
    // =====================================================================================

    /** Die Königsburg: Stadt, Graben, zwei Mauerringe, Bergfried, Burghof, Kapelle, Garten, Laternen und Grün. */
    private void royal() {
        Ring in = this.inner;
        if (in == null) {
            return;
        }
        this.streets(in);
        this.moat(in);
        this.ringWall(this.outer);
        this.towers(this.outer, 11, WALL + 9, 9, WALL + 6, 40, 36, -1, false);
        this.gatehouse(this.outer, this.gateX, 9, 11, 9, true);
        this.ringWall(in);
        this.towers(in, 13, INNER_WALL + 11, 11, INNER_WALL + 7, 34, 26, -3, true);
        this.gatehouse(in, this.gateX, 10, 12, 9, false);
        this.bridge(in);
        this.keep();
        this.court(in);
        this.town(in);
        this.royalLights(in);
        this.greenery(this.outerWard, false);
        if (this.innerWard != null) {
            this.greenery(this.innerWard, true);
        }
    }

    /** Pflastert ein Rechteck (unter der Bodenlinie, an Stelle des Grases) und hält es mit einem Rand frei. */
    private void pave(int x0, int z0, int x1, int z1) {
        for (int z = z0; z <= z1; z++) {
            for (int x = x0; x <= x1; x++) {
                this.put(x, -1, z, this.paving(x, z));
            }
        }
        this.taken.add(new BoundingBox(x0 - 1, 0, z0 - 1, x1 + 1, 0, z1 + 1));
    }

    /** Eine Straße der Stadt: gepflastert, und später stehen Häuser daran. */
    private void street(int x0, int z0, int x1, int z1) {
        this.pave(x0, z0, x1, z1);
        this.streets.add(new BoundingBox(x0, 0, z0, x1, 0, z1));
    }

    /**
     * Die Straßen der Stadt vor und neben der Kernburg: die Hauptstraße vom Tor zur Kernburg, die Querstraße mit zwei Gassen
     * parallel dazu, zwei Gassen längs auf jeder Seite (die äußeren reichen bis hinter die Kernburg) und der Marktplatz mit
     * seinem Brunnen.
     */
    private void streets(Ring in) {
        int top = INSET + THICK;
        int front = in.z0() - RING_ROAD;
        this.plaza = (top + 11 + front) / 2;
        int near = this.w * 16 / 100;
        int far = this.w * 33 / 100;
        this.street(this.gateX - 2, 0, this.gateX + 2, front - 1);
        this.street(top + 3, this.plaza - 1, this.w - 1 - top - 3, this.plaza + 1);
        // zwei Gassen quer: zwischen Mauer und Querstraße und zwischen Querstraße und Ringstraße
        int north = (top + 12 + this.plaza) / 2;
        int south = (this.plaza + front) / 2;
        this.street(top + 3, north - 1, this.w - 1 - top - 3, north + 1);
        this.street(top + 3, south - 1, this.w - 1 - top - 3, south + 1);
        for (int s = -1; s <= 1; s += 2) {
            this.street(this.gateX + s * near - 1, top + 3, this.gateX + s * near + 1, front - 1);
            this.street(this.gateX + s * far - 1, top + 3, this.gateX + s * far + 1, in.z1() + RING_ROAD);
        }
        // hinter der Kernburg verbindet die Ringstraße die äußeren Gassen
        this.pave(this.gateX - far - 1, in.z1() + COUNTERSCARP + 1, this.gateX + far + 1, in.z1() + RING_ROAD);
        this.street(this.gateX - MARKET, this.plaza - MARKET, this.gateX + MARKET, this.plaza + MARKET);
        this.fountain(this.gateX, this.plaza, 5);
    }

    /**
     * Der Graben um die Kernburg: zwischen gemauerter Futtermauer und Gegenmauer drei Lagen tief ausgehoben, unten
     * Stampflehm; auf der Gegenmauer ein Geländer mit Laternen; außen die gepflasterte Ringstraße.
     */
    private void moat(Ring in) {
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int z = in.z0() - RING_ROAD; z <= in.z1() + RING_ROAD; z++) {
            for (int x = in.x0() - RING_ROAD; x <= in.x1() + RING_ROAD; x++) {
                int o = in.outside(x, z);
                if (o == ESCARP || o == COUNTERSCARP) {
                    this.put(x, -4, z, Blocks.COBBLESTONE.defaultBlockState());
                    for (int y = -3; y <= -2; y++) {
                        this.put(x, y, z, this.stone(x, y, z));
                    }
                    this.put(x, -1, z, this.stone(x, 2, z));
                    if (o == COUNTERSCARP) {
                        this.put(x, 0, z, Blocks.STONE_BRICK_WALL.defaultBlockState());
                        if ((x + z) % 12 == 0) {
                            this.put(x, 1, z, Blocks.LANTERN.defaultBlockState());
                        }
                    }
                } else if (o > ESCARP && o < COUNTERSCARP) {
                    for (int y = -3; y <= -1; y++) {
                        this.put(x, y, z, air);
                    }
                    this.put(x, -4, z, Blocks.PACKED_MUD.defaultBlockState());
                } else if (o > COUNTERSCARP) {
                    this.put(x, -1, z, this.paving(x, z));
                }
            }
        }
    }

    /**
     * Die Steinbrücke über den Graben zum inneren Tor: Fahrbahn mit Brüstung, darunter ein Bogen, an den Enden Pfosten mit
     * Laternen; über die Berme ein gepflasterter Weg zur Durchfahrt.
     */
    private void bridge(Ring in) {
        int z0 = in.z0() - COUNTERSCARP;
        int z1 = in.z0() - ESCARP;
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState bricks = Blocks.STONE_BRICKS.defaultBlockState();
        for (int z = z0; z <= z1; z++) {
            int k = z - z0;
            for (int x = this.gateX - 3; x <= this.gateX + 3; x++) {
                boolean rail = Math.abs(x - this.gateX) == 3;
                this.put(x, -1, z, rail ? bricks : this.paving(x, z));
                this.put(x, 0, z, rail ? Blocks.STONE_BRICK_WALL.defaultBlockState() : air);
                this.put(x, 1, z, air);
                if (k >= 1 && k <= 5) {
                    // Bogen: Pfeiler an den Ufern, darüber Stufen, in der Mitte der Scheitel
                    BlockState arch = switch (k) {
                        case 1, 5 -> bricks;
                        case 2 -> stairs(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, Half.TOP);
                        case 4 -> stairs(Blocks.STONE_BRICK_STAIRS, Direction.SOUTH, Half.TOP);
                        default -> air;
                    };
                    this.put(x, -2, z, arch);
                    this.put(x, -3, z, k == 1 || k == 5 ? bricks : air);
                }
            }
        }
        for (int x : new int[] {this.gateX - 3, this.gateX + 3}) {
            for (int z : new int[] {z0, z1}) {
                this.put(x, 0, z, Blocks.STONE_BRICK_WALL.defaultBlockState());
                this.put(x, 1, z, Blocks.STONE_BRICK_WALL.defaultBlockState());
                this.put(x, 2, z, Blocks.LANTERN.defaultBlockState());
            }
        }
        // über die Berme zum Tor
        for (int z = z1 + 1; z < in.z0(); z++) {
            for (int x = this.gateX - 2; x <= this.gateX + 2; x++) {
                this.put(x, -1, z, this.paving(x, z));
            }
        }
    }

    /**
     * Der Hof der Kernburg: vom Torhaus ein gepflasterter Burghof mit Prunkbrunnen bis vor Bergfried, Palas und Kapelle,
     * hinter dem Bergfried der Königsgarten, an der linken Mauer der Marstall, an den anderen Mauern Kaserne, Lager,
     * Schmiede, Sternwarte und Gesindehäuser.
     */
    private void court(Ring in) {
        Ward ward = this.innerWard;
        int s = this.keepSize / 2;
        int keepFront = this.keepZ - s;
        int gateEnd = in.z0() + 12;
        int hx = 24;
        int cz1 = keepFront - 3;
        int fz = (gateEnd + cz1) / 2;
        // Burghof: Platten aus poliertem Andesit in einem Raster aus Steinziegeln, am Rand ein Band aus gemeißelten Steinen
        for (int z = gateEnd; z <= cz1; z++) {
            for (int x = this.gateX - hx; x <= this.gateX + hx; x++) {
                boolean border = Math.abs(x - this.gateX) == hx || z == gateEnd || z == cz1;
                boolean line = Math.floorMod(x - this.gateX, 6) == 3 || Math.floorMod(z - fz, 6) == 3;
                this.put(x, -1, z, (border ? Blocks.CHISELED_STONE_BRICKS : line ? Blocks.STONE_BRICKS : Blocks.POLISHED_ANDESITE).defaultBlockState());
            }
        }
        this.taken.add(new BoundingBox(this.gateX - hx - 1, 0, gateEnd - 1, this.gateX + hx + 1, 0, cz1 + 1));
        this.fountain(this.gateX, fz, 5);
        for (int[] c : new int[][] {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
            this.post(this.gateX + c[0] * 9, fz + c[1] * 9);
        }
        // Palas links, Kapelle rechts vom Bergfried, beide mit dem Eingang zum Hof
        this.beside(BuildingType.THRONE_HALL, 0, this.keepX - s - 5, this.keepZ, -1, Rotation.NONE, ward);
        this.chapel(this.keepX + s + 12, this.keepZ, Rotation.NONE);
        // Königsgarten
        this.garden(this.gateX, (this.keepZ + s + 4 + in.z1() - in.thick()) / 2);
        // Marstall an der linken Mauer, die Boxen zum Hof
        this.stable(ward.x0() + 6, this.keepZ, Rotation.CLOCKWISE_90);
        // Bäume rechts und links des Burghofs
        for (int z = gateEnd + 4; z <= keepFront - 8; z += 9) {
            for (int x : new int[] {this.gateX - hx - 4, this.gateX + hx + 4}) {
                BoundingBox box = new BoundingBox(x - 2, 0, z - 2, x + 2, 0, z + 2);
                if (this.free(box, ward)) {
                    this.tree(x, z, false);
                    this.taken.add(box);
                }
            }
        }
        // Häuser an den Mauern, die Vorderseite zum Hof
        int[] k = {0};
        this.row(COURT, k, Direction.EAST, ward.x0() + 1, false, ward.z0() + 6, ward.z1() - 1, 18, ward);
        this.row(COURT, k, Direction.WEST, ward.x1() - 1, false, ward.z0() + 6, ward.z1() - 1, 18, ward);
        this.row(COURT, k, Direction.NORTH, ward.z1() - 1, false, ward.x0() + 16, ward.x1() - 16, 18, ward);
        this.row(COURT, k, Direction.SOUTH, ward.z0() + 1, false, ward.x0() + 16, ward.x1() - 16, 18, ward);
    }

    /**
     * Die Häuser der Stadt: rund um den Marktplatz und an der Hauptstraße Bürgerhäuser, Taverne, Markthalle und Schmiede;
     * an Querstraße und Gassen Wohnhäuser, Werkstätten und Lager; an der Ringstraße mit Blick auf den Graben und zuletzt
     * an den Mauern, wo noch Platz ist.
     */
    private void town(Ring in) {
        Ward ward = this.outerWard;
        int[] k = {0};
        // um den Marktplatz und an der Hauptstraße
        BoundingBox market = this.streets.getLast();
        this.row(MAIN, k, Direction.SOUTH, market.minZ() - 2, true, market.minX(), market.maxX(), 18, ward);
        this.row(MAIN, k, Direction.NORTH, market.maxZ() + 2, true, market.minX(), market.maxX(), 18, ward);
        this.row(MAIN, k, Direction.EAST, market.minX() - 2, true, market.minZ(), market.maxZ(), 18, ward);
        this.row(MAIN, k, Direction.WEST, market.maxX() + 2, true, market.minZ(), market.maxZ(), 18, ward);
        this.alongStreet(this.streets.getFirst(), MAIN, k, 18, ward);
        // Querstraße und Gassen
        int[] j = {0};
        for (BoundingBox s : this.streets.subList(1, this.streets.size() - 1)) {
            this.alongStreet(s, TOWN, j, 18, ward);
        }
        // an der Ringstraße, mit Blick auf den Graben
        this.row(TOWN, j, Direction.SOUTH, in.z0() - RING_ROAD - 2, true, in.x0() - RING_ROAD, in.x1() + RING_ROAD, 16, ward);
        this.row(TOWN, j, Direction.EAST, in.x0() - RING_ROAD - 2, true, in.z0() - RING_ROAD, in.z1() + RING_ROAD, 16, ward);
        this.row(TOWN, j, Direction.WEST, in.x1() + RING_ROAD + 2, true, in.z0() - RING_ROAD, in.z1() + RING_ROAD, 16, ward);
        // an den Mauern, die Rückseite zur Mauer
        this.row(TOWN, j, Direction.EAST, ward.x0() + 1, false, ward.z0() + 4, ward.z1() - 1, 18, ward);
        this.row(TOWN, j, Direction.WEST, ward.x1() - 1, false, ward.z0() + 4, ward.z1() - 1, 18, ward);
        this.row(TOWN, j, Direction.SOUTH, ward.z0() + 1, false, ward.x0() + 4, ward.x1() - 4, 18, ward);
        this.row(TOWN, j, Direction.NORTH, ward.z1() - 1, false, ward.x0() + 4, ward.x1() - 4, 18, ward);
    }

    /** Laternen auf Pfosten an Hauptstraße, Marktplatz, Querstraße, Gassen und im Burghof. */
    private void royalLights(Ring in) {
        for (int z = INSET + 14; z < in.z0() - RING_ROAD - 2; z += 10) {
            if (Math.abs(z - this.plaza) <= MARKET + 2) {
                continue;
            }
            for (int x : new int[] {this.gateX - 3, this.gateX + 3}) {
                this.post(x, z);
            }
        }
        for (int[] c : new int[][] {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
            this.post(this.gateX + c[0] * (MARKET - 1), this.plaza + c[1] * (MARKET - 1));
        }
        for (BoundingBox s : this.streets.subList(1, this.streets.size() - 1)) {
            if (s.getXSpan() > s.getZSpan()) {
                for (int x = s.minX() + 6; x < s.maxX(); x += 16) {
                    this.post(x, s.minZ() - 1);
                }
            } else {
                for (int z = s.minZ() + 6; z < s.maxZ(); z += 16) {
                    this.post(s.minX() - 1, z);
                }
            }
        }
        // Burghof: an den Ecken und am Weg vom Tor
        int gateEnd = in.z0() + 12;
        int keepFront = this.keepZ - this.keepSize / 2;
        for (int x : new int[] {this.gateX - 23, this.gateX + 23}) {
            for (int z : new int[] {gateEnd + 1, keepFront - 4}) {
                this.post(x, z);
            }
        }
    }

    // =====================================================================================
    // Brunnen, Kapelle, Garten
    // =====================================================================================

    /**
     * Ein Prunkbrunnen: achteckiges Becken mit Wasser und gestuftem Rand, in der Mitte eine Säule mit einer Schale, aus der
     * Wasser fließt (das fließende Wasser entsteht im Spiel von selbst).
     */
    private void fountain(int cx, int cz, int r) {
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState bricks = Blocks.STONE_BRICKS.defaultBlockState();
        for (int dz = -r; dz <= r; dz++) {
            for (int dx = -r; dx <= r; dx++) {
                int ax = Math.abs(dx);
                int az = Math.abs(dz);
                if (ax + az > r + 2) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                this.put(x, -1, z, bricks);
                if (ax == r || az == r || ax + az == r + 2) {
                    this.put(x, 0, z, bricks);
                    BlockState rim;
                    if (ax == r) {
                        rim = stairs(Blocks.STONE_BRICK_STAIRS, dx > 0 ? Direction.WEST : Direction.EAST, Half.BOTTOM);
                    } else if (az == r) {
                        rim = stairs(Blocks.STONE_BRICK_STAIRS, dz > 0 ? Direction.NORTH : Direction.SOUTH, Half.BOTTOM);
                    } else {
                        rim = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
                    }
                    this.put(x, 1, z, rim);
                } else {
                    this.put(x, 0, z, Blocks.POLISHED_ANDESITE.defaultBlockState());
                    this.put(x, 1, z, water);
                }
            }
        }
        int top = r >= 5 ? 5 : 4;
        this.put(cx, 1, cz, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        for (int y = 2; y < top; y++) {
            this.put(cx, y, cz, Blocks.STONE_BRICK_WALL.defaultBlockState());
        }
        this.put(cx, top, cz, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        for (Direction f : Direction.Plane.HORIZONTAL) {
            this.put(cx + f.getStepX(), top, cz + f.getStepZ(), stairs(Blocks.STONE_BRICK_STAIRS, f.getOpposite(), Half.TOP));
        }
        for (int[] c : new int[][] {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
            this.put(cx + c[0], top, cz + c[1], Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        }
        this.put(cx, top + 1, cz, water);
        this.taken.add(new BoundingBox(cx - r - 1, 0, cz - r - 1, cx + r + 1, 0, cz + r + 1));
    }

    /** Setzt einen Block eines Bauwerks in Ortskoordinaten (Mitte bei (0, 0), vorn −z) gedreht an seine Stelle. */
    private void local(BlockPos origin, Rotation rot, int x, int y, int z, BlockState state) {
        BlockPos p = Blueprint.world(origin, rot, new BlockPos(x, y, z));
        this.put(p.getX(), p.getY(), p.getZ(), Blueprint.world(state, rot));
    }

    /**
     * Die Kapelle: ein hohes Schiff mit Strebepfeilern und bunten Fenstern, ein Chor mit Rosette und Altar, vorn der
     * Turm mit offener Stube, Laterne und schlankem Helm mit Kreuz; innen Bänke, ein roter Läufer, Kerzen, Lesepult
     * und Kronleuchter.
     */
    private void chapel(int cx, int cz, Rotation rot) {
        BlockPos o = new BlockPos(cx, 0, cz);
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        // Mauern, Böden und Fundament: Schiff (x −5..5, z −9..8), Chor (x −4..4, z 8..14), Turm (x −3..3, z −15..−9)
        for (int z = -15; z <= 14; z++) {
            for (int x = -5; x <= 5; x++) {
                boolean nave = z >= -9 && z <= 8;
                boolean choir = Math.abs(x) <= 4 && z >= 8;
                boolean tower = Math.abs(x) <= 3 && z <= -9;
                if (!nave && !choir && !tower) {
                    continue;
                }
                boolean wall = tower && (Math.abs(x) == 3 || z == -15 || z == -9)
                        || nave && (Math.abs(x) == 5 || z == -9 || z == 8 && Math.abs(x) >= 4)
                        || choir && (Math.abs(x) == 4 || z == 14);
                int h = tower ? 26 : nave ? 9 : 8;
                this.local(o, rot, x, -1, z, cobble);
                for (int y = 0; y <= h; y++) {
                    BlockState st;
                    if (wall) {
                        st = this.stone(cx + x, y + 2, cz + z);
                    } else if (y == 0) {
                        st = ((x + z) & 1) == 0 ? Blocks.POLISHED_ANDESITE.defaultBlockState() : Blocks.POLISHED_DIORITE.defaultBlockState();
                    } else {
                        st = air;
                    }
                    this.local(o, rot, x, y, z, st);
                }
            }
        }
        // Chorbogen: oben geschlossen, die Ecken gerundet
        for (int x = -3; x <= 3; x++) {
            for (int y = 7; y <= 9; y++) {
                this.local(o, rot, x, y, 8, this.stone(cx + x, y + 2, cz + 8));
            }
        }
        this.local(o, rot, -3, 6, 8, stairs(Blocks.STONE_BRICK_STAIRS, Direction.EAST, Half.TOP));
        this.local(o, rot, 3, 6, 8, stairs(Blocks.STONE_BRICK_STAIRS, Direction.WEST, Half.TOP));
        // Strebepfeiler
        for (int z : new int[] {-7, -2, 3}) {
            for (int sd = -1; sd <= 1; sd += 2) {
                this.local(o, rot, 6 * sd, -1, z, cobble);
                for (int y = 0; y <= 6; y++) {
                    this.local(o, rot, 6 * sd, y, z, Blocks.STONE_BRICKS.defaultBlockState());
                }
                this.local(o, rot, 6 * sd, 7, z, stairs(Blocks.STONE_BRICK_STAIRS, sd < 0 ? Direction.EAST : Direction.WEST, Half.BOTTOM));
            }
        }
        // bunte Fenster: im Schiff je drei (zwei breit), im Chor je eins, hinten eine Rosette
        DyeColor[] colors = {DyeColor.BLUE, DyeColor.RED, DyeColor.YELLOW, DyeColor.BLUE, DyeColor.PURPLE, DyeColor.RED};
        for (int z : new int[] {-5, 0, 5}) {
            for (int sd = -1; sd <= 1; sd += 2) {
                for (int y = 2; y <= 7; y++) {
                    for (int k = 0; k < 2; k++) {
                        this.local(o, rot, 5 * sd, y, z + k, Blocks.STAINED_GLASS_PANE.pick(colors[(y - 2 + k) % colors.length]).defaultBlockState());
                    }
                }
                this.local(o, rot, 5 * sd, 8, z, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
                this.local(o, rot, 5 * sd, 8, z + 1, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
            }
        }
        for (int sd = -1; sd <= 1; sd += 2) {
            for (int z = 10; z <= 11; z++) {
                for (int y = 2; y <= 6; y++) {
                    this.local(o, rot, 4 * sd, y, z, Blocks.STAINED_GLASS_PANE.pick(colors[(y + z) % colors.length]).defaultBlockState());
                }
            }
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                int a = Math.abs(dx) + Math.abs(dy);
                if (a > 3) {
                    continue;
                }
                DyeColor c = a == 0 ? DyeColor.YELLOW : a == 1 ? DyeColor.RED : a == 2 ? DyeColor.BLUE : DyeColor.PURPLE;
                this.local(o, rot, dx, 5 + dy, 14, Blocks.STAINED_GLASS_PANE.pick(c).defaultBlockState());
            }
        }
        // Dach des Schiffs (steil) mit Giebeln vorn und über dem Chor
        BlockState tiles = Blocks.DEEPSLATE_TILES.defaultBlockState();
        for (int i = 0; i <= 6; i++) {
            int y = 10 + i;
            for (int z = -10; z <= 8; z++) {
                if (z == -10 && Math.abs(6 - i) <= 3) {
                    continue; // dort steht der Turm
                }
                if (i < 6) {
                    this.local(o, rot, -6 + i, y, z, stairs(Blocks.DEEPSLATE_TILE_STAIRS, Direction.EAST, Half.BOTTOM));
                    this.local(o, rot, 6 - i, y, z, stairs(Blocks.DEEPSLATE_TILE_STAIRS, Direction.WEST, Half.BOTTOM));
                } else {
                    this.local(o, rot, 0, y, z, tiles);
                }
            }
            for (int x = -5 + i; x <= 5 - i; x++) {
                this.local(o, rot, x, y, -9, this.stone(cx + x, y, cz - 9));
                this.local(o, rot, x, y, 8, this.stone(cx + x, y, cz + 8));
            }
        }
        // Dach des Chors (niedriger) mit Giebel hinten
        for (int i = 0; i <= 5; i++) {
            int y = 9 + i;
            for (int z = 9; z <= 15; z++) {
                if (i < 5) {
                    this.local(o, rot, -5 + i, y, z, stairs(Blocks.DEEPSLATE_TILE_STAIRS, Direction.EAST, Half.BOTTOM));
                    this.local(o, rot, 5 - i, y, z, stairs(Blocks.DEEPSLATE_TILE_STAIRS, Direction.WEST, Half.BOTTOM));
                } else {
                    this.local(o, rot, 0, y, z, tiles);
                }
            }
            for (int x = -4 + i; x <= 4 - i; x++) {
                this.local(o, rot, x, y, 14, this.stone(cx + x, y, cz + 14));
            }
        }
        // Turm: Portal mit Stufen, Durchgang ins Schiff, Böden, Leiter, offene Stube mit Laterne, Helm
        this.local(o, rot, 0, 1, -15, Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH));
        this.local(o, rot, 0, 2, -15, Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        for (int x = -1; x <= 1; x++) {
            this.local(o, rot, x, 0, -16, stairs(Blocks.STONE_BRICK_STAIRS, Direction.SOUTH, Half.BOTTOM));
            this.local(o, rot, x, -1, -16, cobble);
            this.local(o, rot, x, 3, -15, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        }
        for (int y = 1; y <= 3; y++) {
            this.local(o, rot, -1, y, -15, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
            this.local(o, rot, 1, y, -15, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        }
        this.local(o, rot, 0, 4, -15, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        for (int y = 1; y <= 3; y++) {
            this.local(o, rot, 0, y, -9, air);
        }
        for (int y : new int[] {8, 16, 20, 25}) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -14; z <= -10; z++) {
                    this.local(o, rot, x, y, z, (y == 25 ? Blocks.STONE_BRICKS : Blocks.SPRUCE_PLANKS).defaultBlockState());
                }
            }
        }
        for (int y = 1; y <= 21; y++) {
            this.local(o, rot, -2, y, -14, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
        }
        for (int y = 9; y <= 13; y++) {
            this.local(o, rot, 0, y, -15, y == 13 ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : Blocks.STAINED_GLASS_PANE.red().defaultBlockState());
        }
        for (int y = 21; y <= 24; y++) {
            for (int k = -1; k <= 1; k++) {
                this.local(o, rot, k, y, -15, air);
                this.local(o, rot, k, y, -9, air);
                this.local(o, rot, -3, y, -12 + k, air);
                this.local(o, rot, 3, y, -12 + k, air);
            }
        }
        this.local(o, rot, 0, 24, -12, hanging());
        BlockPos top = Blueprint.world(o, rot, new BlockPos(0, 0, -12));
        this.spire(top.getX(), top.getZ(), 27, 3);
        // innen: Bänke, Läufer, Altar mit Kerzen, Lesepult, Kronleuchter
        for (int z = -7; z <= 5; z += 2) {
            for (int x : new int[] {-4, -3, -2, -1, 1, 2, 3, 4}) {
                this.local(o, rot, x, 1, z, stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, Half.BOTTOM));
            }
        }
        for (int z = -8; z <= 11; z++) {
            this.local(o, rot, 0, 1, z, Blocks.CARPET.red().defaultBlockState());
        }
        for (int x = -1; x <= 1; x++) {
            this.local(o, rot, x, 1, 12, Blocks.POLISHED_DIORITE.defaultBlockState());
        }
        for (int x : new int[] {-1, 1}) {
            this.local(o, rot, x, 2, 12, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3));
        }
        this.local(o, rot, 2, 1, 10, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.NORTH));
        for (int z : new int[] {-5, 1, 6}) {
            for (int y = 10; y <= 15; y++) {
                this.local(o, rot, 0, y, z, Blocks.IRON_CHAIN.defaultBlockState());
            }
            this.local(o, rot, 0, 9, z, hanging());
        }
        BlockPos a = Blueprint.world(o, rot, new BlockPos(-7, 0, -17));
        BlockPos b = Blueprint.world(o, rot, new BlockPos(7, 0, 16));
        this.taken.add(BoundingBox.fromCorners(a, b));
    }

    /**
     * Der Marstall: ein langer Stall mit Steinmauern und Holzdach, vorn offen mit Pfosten und einem Sturzbalken, innen
     * Boxen mit Zäunen, Heu und Fässern, Laternen unter dem Balken. Ortskoordinaten: 25 lang (x), 9 tief (z), vorn −z.
     */
    private void stable(int cx, int cz, Rotation rot) {
        BlockPos o = new BlockPos(cx, 0, cz);
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        BlockState post = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
        for (int z = -4; z <= 4; z++) {
            for (int x = -12; x <= 12; x++) {
                boolean back = z == 4;
                boolean side = Math.abs(x) == 12;
                this.local(o, rot, x, -1, z, cobble);
                for (int y = 0; y <= 4; y++) {
                    BlockState st;
                    if (y == 0) {
                        st = back || side ? this.stone(cx + x, 0, cz + z) : Blocks.COARSE_DIRT.defaultBlockState();
                    } else if (back || side) {
                        st = this.stone(cx + x, y + 2, cz + z);
                    } else if (z == -4) {
                        st = y == 4 ? Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                                Direction.Axis.X) : (Math.floorMod(x, 4) == 0 ? post : air);
                    } else {
                        st = air;
                    }
                    this.local(o, rot, x, y, z, st);
                }
            }
        }
        // Boxen: Trennzäune, Heu und ein Fass je Box, Laternen unter dem Balken
        for (int x = -12; x <= 12; x += 4) {
            if (Math.abs(x) == 12) {
                continue;
            }
            for (int z = -2; z <= 3; z++) {
                this.local(o, rot, x, 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
            }
        }
        for (int x = -10; x <= 10; x += 4) {
            this.local(o, rot, x, 1, 3, Blocks.HAY_BLOCK.defaultBlockState());
            this.local(o, rot, x + 1, 1, 3, Blocks.HAY_BLOCK.defaultBlockState());
            this.local(o, rot, x - 1, 1, 3, Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING, Direction.UP));
            this.local(o, rot, x, 3, -3, hanging());
        }
        // Dach aus Fichtenholz, First längs, Giebel aus Stein
        for (int i = 0; i <= 5; i++) {
            int y = 5 + i;
            for (int x = -13; x <= 13; x++) {
                if (i < 5) {
                    this.local(o, rot, x, y, -5 + i, stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.BOTTOM));
                    this.local(o, rot, x, y, 5 - i, stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, Half.BOTTOM));
                } else {
                    this.local(o, rot, x, y, 0, Blocks.SPRUCE_PLANKS.defaultBlockState());
                }
            }
            for (int z = -4 + i; z <= 4 - i; z++) {
                this.local(o, rot, -12, y, z, this.stone(cx - 12, y, cz + z));
                this.local(o, rot, 12, y, z, this.stone(cx + 12, y, cz + z));
            }
        }
        BlockPos a = Blueprint.world(o, rot, new BlockPos(-14, 0, -7));
        BlockPos b = Blueprint.world(o, rot, new BlockPos(14, 0, 6));
        this.taken.add(BoundingBox.fromCorners(a, b));
    }

    /** Ein schlanker Turmhelm über einem Turm mit Halbmesser {@code base}: Traufe, Stufen aus Ziegeln, oben ein Kreuz. */
    private void spire(int cx, int cz, int y, int base) {
        for (int dz = -base - 1; dz <= base + 1; dz++) {
            for (int dx = -base - 1; dx <= base + 1; dx++) {
                if (Math.abs(dx) != base + 1 && Math.abs(dz) != base + 1) {
                    continue;
                }
                Direction up = Math.abs(dz) >= Math.abs(dx) ? (dz < 0 ? Direction.SOUTH : Direction.NORTH) : (dx < 0 ? Direction.EAST : Direction.WEST);
                this.put(cx + dx, y - 1, cz + dz, stairs(Blocks.DEEPSLATE_TILE_STAIRS, up, Half.BOTTOM));
            }
        }
        BlockState tiles = Blocks.DEEPSLATE_TILES.defaultBlockState();
        int layer = y;
        for (int r = base; r >= 1; r--) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dx = -r; dx <= r; dx++) {
                    boolean edge = Math.abs(dx) == r || Math.abs(dz) == r;
                    this.put(cx + dx, layer, cz + dz, tiles);
                    if (edge) {
                        Direction up = Math.abs(dz) >= Math.abs(dx) ? (dz < 0 ? Direction.SOUTH : Direction.NORTH)
                                : (dx < 0 ? Direction.EAST : Direction.WEST);
                        this.put(cx + dx, layer + 1, cz + dz, stairs(Blocks.DEEPSLATE_TILE_STAIRS, up, Half.BOTTOM));
                    } else {
                        this.put(cx + dx, layer + 1, cz + dz, tiles);
                    }
                }
            }
            layer += 2;
        }
        for (int i = 0; i < 3; i++) {
            this.put(cx, layer + i, cz, tiles);
        }
        layer += 3;
        this.put(cx, layer, cz, Blocks.DARK_OAK_FENCE.defaultBlockState());
        this.put(cx, layer + 1, cz, Blocks.DARK_OAK_FENCE.defaultBlockState());
        this.put(cx, layer + 2, cz, Blocks.DARK_OAK_FENCE.defaultBlockState());
        this.put(cx - 1, layer + 1, cz, Blocks.DARK_OAK_FENCE.defaultBlockState());
        this.put(cx + 1, layer + 1, cz, Blocks.DARK_OAK_FENCE.defaultBlockState());
    }

    /**
     * Der Königsgarten: außen eine Hecke mit vier Durchgängen, Wegkreuz aus hellem Stein, Beete mit Blumen in Streifen und
     * niedrigen Hecken, in der Mitte ein Brunnen, in den Ecken Bäume.
     */
    private void garden(int cx, int cz) {
        int hx = 22;
        int hz = 16;
        BlockState hedge = leaves(Blocks.OAK_LEAVES);
        BlockState path = Blocks.POLISHED_DIORITE.defaultBlockState();
        Block[][] flowers = {{Blocks.RED_TULIP, Blocks.WHITE_TULIP}, {Blocks.CORNFLOWER, Blocks.AZURE_BLUET}, {Blocks.ALLIUM, Blocks.PINK_TULIP},
                {Blocks.POPPY, Blocks.OXEYE_DAISY}};
        for (int dz = -hz; dz <= hz; dz++) {
            for (int dx = -hx; dx <= hx; dx++) {
                int x = cx + dx;
                int z = cz + dz;
                int ax = Math.abs(dx);
                int az = Math.abs(dz);
                if (ax == hx || az == hz) {
                    if (ax <= 1 || az <= 1) {
                        this.put(x, -1, z, path);
                    } else {
                        this.put(x, 0, z, hedge);
                        this.put(x, 1, z, hedge);
                    }
                    continue;
                }
                if (ax <= 1 || az <= 1 || ax == hx - 1 || az == hz - 1) {
                    this.put(x, -1, z, path);
                    continue;
                }
                if (ax <= 6 && az <= 6) {
                    if (ax == 6 || az == 6) {
                        this.put(x, -1, z, path);
                    }
                    continue;
                }
                if (ax == 2 || az == 2 || ax == hx - 2 || az == hz - 2) {
                    this.put(x, 0, z, hedge);
                    continue;
                }
                Block[] pair = flowers[(dx < 0 ? 0 : 1) + (dz < 0 ? 0 : 2)];
                this.put(x, 0, z, pair[((ax + az) / 2) % 2].defaultBlockState());
            }
        }
        this.fountain(cx, cz, 4);
        for (int[] c : new int[][] {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
            this.tree(cx + c[0] * (hx - 5), cz + c[1] * (hz - 5), true);
        }
        this.taken.add(new BoundingBox(cx - hx - 1, 0, cz - hz - 1, cx + hx + 1, 0, cz + hz + 1));
    }

    // =====================================================================================
    // Grün
    // =====================================================================================

    /**
     * Was frei bleibt, wird Garten und Wiese: Gemüsegärten an freien Stellen, dazu Eichen und Birken in lockerem Abstand
     * (dauerhaftes Laub); {@code sparse}: nur wenige Bäume (im Hof der Kernburg).
     */
    private void greenery(Ward ward, boolean sparse) {
        BoundingBox plot = BuildingType.FIELD.blueprint(1).worldBounds(BlockPos.ZERO, Rotation.NONE);
        for (int z = ward.z0() + 8; z < ward.z1() - 6; z += 9) {
            for (int x = ward.x0() + 8; x < ward.x1() - 8; x += 11) {
                if (this.hash(x, 7, z) % 4 != 0) {
                    continue;
                }
                BoundingBox box = new BoundingBox(x + plot.minX() - 1, 0, z + plot.minZ() - 1, x + plot.maxX() + 1, 0, z + plot.maxZ() + 1);
                if (this.free(box, ward)) {
                    this.stamp(BuildingType.FIELD, 1, x, z, Rotation.NONE, true);
                }
            }
        }
        for (int z = ward.z0() + 2; z < ward.z1() - 2; z += 7) {
            for (int x = ward.x0() + 2; x < ward.x1() - 2; x += 7) {
                int h = this.hash(x, 3, z);
                if (sparse ? h % 4 != 1 : h % 3 == 0) {
                    continue;
                }
                int tx = x + h % 5 - 2;
                int tz = z + (h >> 4) % 5 - 2;
                BoundingBox box = new BoundingBox(tx - 3, 0, tz - 3, tx + 3, 0, tz + 3);
                if (this.free(box, ward)) {
                    this.tree(tx, tz, h % 4 == 0);
                    this.taken.add(box);
                }
            }
        }
    }

    /** Ein Baum: Stamm vier bis sechs hoch, eine runde Krone (Eiche oder Birke). */
    private void tree(int x, int z, boolean birch) {
        int h = 4 + this.hash(x, 9, z) % 3;
        Block log = birch ? Blocks.BIRCH_LOG : Blocks.OAK_LOG;
        BlockState crown = leaves(birch ? Blocks.BIRCH_LEAVES : Blocks.OAK_LEAVES);
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
                    this.put(x + dx, y, z + dz, crown);
                }
            }
        }
    }

    // =====================================================================================
    // Laternen (kleine Burg)
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
}
