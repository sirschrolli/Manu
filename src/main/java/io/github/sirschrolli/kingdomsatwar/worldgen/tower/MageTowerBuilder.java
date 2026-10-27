package io.github.sirschrolli.kingdomsatwar.worldgen.tower;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.SpeleothemThickness;
import net.minecraft.world.level.block.state.properties.StairsShape;

/**
 * Baut einen Magierturm in eigenen Koordinaten: Mitte bei (0, 0), y = 0 ist die Terrasse, die Tür zeigt nach vorn
 * (−z). Über einer gepflasterten Terrasse steht der runde Schaft (Außenradius 6½, acht Lisenen) mit vier Geschossen –
 * Eingangshalle, Bibliothek, Laboratorium und Kammer –, die eine Treppe an der Wand entlang verbindet. Darüber kragt auf
 * einer getreppten Auskragung das Sanktum aus (Radius 9½, acht hohe Buntglasfenster), in dessen Mitte der Erzmagier über
 * seinem Ritualkreis meditiert. Ein spitzes, leicht geschwungenes Kegeldach mit einer Spitze in der Art des Elements
 * schließt den Turm ab. Alles, was über mehrere Chunks reicht, wird aus festen Zahlen und dem Startwert berechnet, so dass
 * jeder Chunk für sich bauen kann und die Teile genau zusammenpassen.
 */
public final class MageTowerBuilder {
    // ------------------------------------------------------------------ Maße
    public static final double R_SHAFT = 6.5;
    public static final double R_IN = 5.0;
    public static final double R_CROWN = 9.5;
    public static final double R_HALL = 8.0;
    public static final double R_EAVE = 10.5;
    /** Fußbodenlagen der vier Geschosse (Eingangshalle, Bibliothek, Laboratorium, Kammer). */
    public static final int[] FLOOR = {1, 7, 13, 19};
    public static final int SANCTUM = 25;
    /** Radius der Sternspitzen im Ritualkreis des Sanktums. */
    private static final double STAR = 5.6;
    public static final int WALL_TOP = 33;
    public static final int EAVE = 34;
    public static final int ROOF_H = 19;
    /** Höchste Lage des Dachs (die Spitze darüber gehört zum Element). */
    public static final int PEAK = peak();
    /** Halbe Kantenlänge des Bauplatzes, Tiefe unter und Höhe über der Terrasse. */
    public static final int RADIUS = 16;
    public static final int BELOW = 26;
    public static final int ABOVE = PEAK + 8;

    private final VillageBuilder b;
    private final TowerPalette p;
    private final DragonType element;
    /** Höhe des Geländes (oberster fester Block, relativ zur Terrasse) an einer Stelle in Turmkoordinaten. */
    private final java.util.function.IntBinaryOperator ground;

    MageTowerBuilder(VillageBuilder builder, DragonType element, java.util.function.IntBinaryOperator ground) {
        this.b = builder;
        this.element = element;
        this.p = TowerPalette.of(element);
        this.ground = ground;
    }

    void build() {
        this.terrace();
        this.shaft();
        this.floors();
        this.crown();
        this.stairs();
        this.entrance();
        this.hall();
        this.library();
        this.laboratory();
        this.quarters();
        this.sanctum();
        this.roof();
        this.spire();
        this.surroundings();
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    public static double dist(double x, double z) {
        return Math.sqrt(x * x + z * z);
    }

    /** Winkel um die Turmachse in Grad (0 = Osten, 90 = Süden), 0..360. */
    static double angle(double x, double z) {
        double a = Math.toDegrees(Math.atan2(z, x));
        return a < 0 ? a + 360.0 : a;
    }

    /** Kleinster Abstand zweier Winkel in Grad. */
    static double angleDiff(double a, double b) {
        double d = Math.abs(a - b) % 360.0;
        return d > 180.0 ? 360.0 - d : d;
    }

    /** Liegt der Winkel nahe an einem Vielfachen von {@code step} (versetzt um {@code offset})? */
    static boolean nearAngle(double angle, double step, double offset, double tolerance) {
        double rel = ((angle - offset) % step + step) % step;
        return rel < tolerance || rel > step - tolerance;
    }

    /** Radius des Kegeldachs in einer Lage (leicht geschwungen: unten flach, oben steil). */
    static double roofRadius(int y) {
        if (y < EAVE) {
            return R_EAVE;
        }
        double t = (y - EAVE) / (double) ROOF_H;
        return t >= 1.0 ? -1.0 : R_EAVE * Math.pow(1.0 - t, 1.35);
    }

    private static int peak() {
        int y = EAVE;
        while (roofRadius(y + 1) >= 0.0) {
            y++;
        }
        return y;
    }

    public static boolean inRoof(int x, int y, int z) {
        return y >= EAVE && y <= PEAK && dist(x, z) <= roofRadius(y);
    }

    private void set(int x, int y, int z, BlockState state) {
        this.b.set(x, y, z, state);
    }

    private void set(int x, int y, int z, Block block) {
        this.b.set(x, y, z, block.defaultBlockState());
    }

    private void air(int x, int y, int z) {
        this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
    }

    private static BlockState s(Block block) {
        return block.defaultBlockState();
    }

    private static BlockState stairs(Block block, Direction facing, Half half) {
        return VillageBuilder.stairs(block, facing, half);
    }

    private static BlockState slab(Block block, SlabType type) {
        return VillageBuilder.slab(block, type);
    }

    private static BlockState pillar(Block block, Direction.Axis axis) {
        BlockState state = block.defaultBlockState();
        return state.hasProperty(RotatedPillarBlock.AXIS) ? state.setValue(RotatedPillarBlock.AXIS, axis) : state;
    }

    /** Leuchtende Blöcke (Kupferlampen) eingeschaltet. */
    private static BlockState lit(Block block) {
        BlockState state = block.defaultBlockState();
        return state.hasProperty(BlockStateProperties.LIT) ? state.setValue(BlockStateProperties.LIT, true) : state;
    }

    private BlockState wallStone(int x, int y, int z) {
        float n = this.b.noise(x, y, z);
        if (n < 0.13f) {
            return s(this.p.wall2());
        }
        if (n < 0.21f) {
            return s(this.p.wall3());
        }
        return s(this.p.wall());
    }

    private BlockState hanging(Block lantern) {
        BlockState state = lantern.defaultBlockState();
        return state.hasProperty(LanternBlock.HANGING) ? state.setValue(LanternBlock.HANGING, true) : state;
    }

    /** Stuhl, der in Richtung {@code facing} blickt (zum Tisch hin). */
    private static BlockState chair(Direction facing) {
        return s(ModBlocks.OAK_CHAIR.get()).setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, facing);
    }

    private BlockState candles(int count) {
        return this.p.candle().defaultBlockState().setValue(CandleBlock.CANDLES, count).setValue(CandleBlock.LIT, true);
    }

    /** Richtung von der Mitte nach außen (vier Himmelsrichtungen) für einen Winkel. */
    static Direction outward(double angle) {
        if (angle >= 315 || angle < 45) {
            return Direction.EAST;
        }
        if (angle < 135) {
            return Direction.SOUTH;
        }
        if (angle < 225) {
            return Direction.WEST;
        }
        return Direction.NORTH;
    }

    /**
     * Treppe in einem Ring um den Turm: die hohe Seite zum Turm hin. Nahe den Diagonalen als Außenecke, damit der Ring
     * rund wirkt.
     */
    static BlockState ringStair(Block block, int x, int z, Half half) {
        double a = angle(x, z);
        Direction in = outward(a).getOpposite();
        double diag = ((a % 90.0) + 90.0) % 90.0;
        if (Math.abs(diag - 45.0) < 11.0) {
            // Außenecke: das hohe Viertel zeigt zur Mitte
            boolean east = x < 0;
            boolean south = z < 0;
            Direction facing = south ? Direction.SOUTH : Direction.NORTH;
            StairsShape shape;
            if (south) {
                shape = east ? StairsShape.OUTER_LEFT : StairsShape.OUTER_RIGHT;
            } else {
                shape = east ? StairsShape.OUTER_RIGHT : StairsShape.OUTER_LEFT;
            }
            return VillageBuilder.stairs(block, facing, half, shape);
        }
        return VillageBuilder.stairs(block, in, half);
    }

    // =====================================================================================
    // Terrasse und Fundament
    // =====================================================================================

    /** Rand der Terrasse (leicht unregelmäßig). */
    private double terraceEdge(int x, int z) {
        return 11.0 + this.b.noise(x, -7, z) * 1.3;
    }

    private void terrace() {
        BlockState fill = s(this.p.foundation());
        for (int x = -RADIUS + 3; x <= RADIUS - 3; x++) {
            for (int z = -RADIUS + 3; z <= RADIUS - 3; z++) {
                double d = dist(x, z);
                double edge = this.terraceEdge(x, z);
                if (d > edge) {
                    continue;
                }
                // unter der Terrasse auffüllen; über ihr innen alles räumen (der Turm füllt den Raum selbst), am
                // ausfransenden Rand nur wenig, damit das Gelände dort natürlich anschließt
                int clear = d <= R_CROWN + 1.5 ? 64 : d > edge - 1.5 ? 3 : 12;
                BlockState natural = this.naturalGround(x, z);
                this.b.column(x, z, fill, clear);
                BlockState top;
                if (d > edge - 1.6) {
                    // der Rand geht in den Boden der Umgebung über (Sand, Gras, Schnee …)
                    float n = this.b.noise(x, 1, z);
                    top = n < 0.5f ? natural : n < 0.75f ? s(this.p.pave2()) : s(this.p.pave());
                } else if (d > 8.6) {
                    top = this.b.chance(x, 2, z, 0.25f) ? s(this.p.pave2()) : s(this.p.pave());
                } else {
                    top = Math.abs(d - 8.0) < 0.55 ? s(this.p.trim()) : s(this.p.pave());
                }
                this.set(x, 0, z, top);
            }
        }
        // Weg von der Tür hinaus
        for (int z = -8; z >= -RADIUS + 2; z--) {
            for (int x = -1; x <= 1; x++) {
                double d = dist(x, z);
                if (d > this.terraceEdge(x, z) + 2.5) {
                    continue;
                }
                if (d > this.terraceEdge(x, z)) {
                    this.b.column(x, z, fill, 3);
                }
                this.set(x, 0, z, x == 0 ? s(this.p.trim()) : s(this.p.pave()));
            }
        }
    }

    /** Oberster natürlicher Block der Säule (für den Übergang der Terrasse ins Gelände), sonst das Pflaster. */
    private BlockState naturalGround(int x, int z) {
        if (!this.b.here(x, 0, z)) {
            return s(this.p.edge());
        }
        for (int y = 4; y >= -8; y--) {
            BlockState state = this.b.get(x, y, z);
            if (state.isAir() || !state.getFluidState().isEmpty()) {
                continue;
            }
            if (VillageBuilder.isSolidGround(state) && state.isCollisionShapeFullBlock(this.b.level, this.b.world(x, y, z))) {
                return state.hasProperty(BlockStateProperties.SNOWY) ? state.setValue(BlockStateProperties.SNOWY, false) : state;
            }
        }
        return s(this.p.edge());
    }

    // =====================================================================================
    // Schaft
    // =====================================================================================

    private static boolean isBaseboard(int y) {
        return y == FLOOR[0] + 1 || y == FLOOR[1] + 1 || y == FLOOR[2] + 1 || y == FLOOR[3] + 1;
    }

    private static boolean isCornice(int y) {
        return y == FLOOR[1] - 1 || y == FLOOR[2] - 1 || y == FLOOR[3] - 1;
    }

    private boolean shaftWindow(int x, int y, int z) {
        // je Geschoss Fenster in den Himmelsrichtungen (zwei Blöcke hoch), außer über der Treppe und an der Tür
        for (int f = 0; f < 4; f++) {
            int y0 = FLOOR[f] + 2;
            if (y != y0 && y != y0 + 1) {
                continue;
            }
            if (z == 0 && Math.abs(x) == 6) {
                return x > 0 ? f != 1 : f != 3;      // Osten: nicht über Treppe B, Westen: nicht über Treppe D
            }
            if (x == 0 && Math.abs(z) == 6) {
                return z > 0 ? f != 0 : f != 0 && f != 2;   // Süden: nicht über Treppe A; Norden: Tür und Treppe C
            }
        }
        return false;
    }

    private void shaft() {
        for (int x = -7; x <= 7; x++) {
            for (int z = -7; z <= 7; z++) {
                double d = dist(x, z);
                if (d > R_SHAFT) {
                    continue;
                }
                for (int y = 0; y < SANCTUM; y++) {
                    if (d <= R_IN) {
                        // innen: Fundament, Böden und Luft (Böden und Möbel folgen später)
                        this.set(x, y, z, y == 0 ? s(this.p.foundation()) : Blocks.AIR.defaultBlockState());
                        continue;
                    }
                    BlockState state;
                    boolean innerFace = dist(x + 1, z) <= R_IN || dist(x - 1, z) <= R_IN || dist(x, z + 1) <= R_IN || dist(x, z - 1) <= R_IN;
                    if (y <= 1) {
                        state = s(this.p.trim());
                    } else if (y == FLOOR[1] || y == FLOOR[2] || y == FLOOR[3] || y == SANCTUM - 1) {
                        state = s(this.p.trim());
                    } else if (innerFace && (isBaseboard(y) || isCornice(y))) {
                        // innen: Sockelleiste über dem Boden und Gesims unter der Decke
                        state = s(this.p.trim());
                    } else if (this.shaftWindow(x, y, z)) {
                        state = s(this.p.pane());
                    } else {
                        state = this.wallStone(x, y, z);
                    }
                    this.set(x, y, z, state);
                }
            }
        }
        // Fensterbänke und Stürze außen
        for (int f = 0; f < 4; f++) {
            int y0 = FLOOR[f] + 2;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                int wx = dir.getStepX() * 6;
                int wz = dir.getStepZ() * 6;
                if (!this.shaftWindow(wx, y0, wz)) {
                    continue;
                }
                this.set(wx + dir.getStepX(), y0 - 1, wz + dir.getStepZ(), stairs(this.p.trimStairs(), dir.getOpposite(), Half.TOP));
                this.set(wx, y0 + 2, wz, s(this.p.trim()));
                this.set(wx + dir.getStepX(), y0 + 2, wz + dir.getStepZ(), slab(this.p.trimSlab(), SlabType.BOTTOM));
            }
        }
        // Sockel: schräger Ring um den Fuß, Strebepfeiler an den Diagonalen
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                double d = dist(x, z);
                double a = angle(x, z);
                boolean buttress = nearAngle(a, 90.0, 45.0, 9.0);
                if (buttress && d > R_SHAFT && d <= 8.3) {
                    int top = d <= 7.5 ? 5 : 2;
                    for (int y = 1; y <= top; y++) {
                        this.set(x, y, z, y <= 1 ? s(this.p.trim()) : this.wallStone(x, y, z));
                    }
                    this.set(x, top + 1, z, ringStair(this.p.trimStairs(), x, z, Half.BOTTOM));
                } else if (d > R_SHAFT && d <= 7.4) {
                    this.set(x, 1, z, ringStair(this.p.trimStairs(), x, z, Half.BOTTOM));
                }
            }
        }
        // Gesimse in Höhe der Geschossböden: umlaufende Kragsteine
        for (int y : new int[] {FLOOR[1], FLOOR[2], FLOOR[3]}) {
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    double d = dist(x, z);
                    if (d > R_SHAFT && d <= 7.3) {
                        this.set(x, y, z, ringStair(this.p.trimStairs(), x, z, Half.TOP));
                    }
                }
            }
        }
        // Lisenen: acht Säulen außen am Schaft, die die Auskragung der Krone tragen
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                double d = dist(x, z);
                if (d > R_SHAFT && d <= 7.2 && nearAngle(angle(x, z), 45.0, 22.5, 5.0)) {
                    this.set(x, 1, z, s(this.p.trim()));
                    for (int y = 2; y < SANCTUM - 3; y++) {
                        this.set(x, y, z, pillar(this.p.pillar(), Direction.Axis.Y));
                    }
                }
            }
        }
    }

    // =====================================================================================
    // Böden und Treppen
    // =====================================================================================

    /** Ein Treppenlauf an der Wand: Zugang {@code (x1, z1)}, sechs Stufen in Richtung {@code dir} ab Boden {@code floor}. */
    public record Flight(int x1, int z1, Direction dir, int floor) {
        public int x(int i) {
            return this.x1 + this.dir.getStepX() * i;
        }

        public int z(int i) {
            return this.z1 + this.dir.getStepZ() * i;
        }
    }

    public static final Flight[] FLIGHTS = {
            new Flight(-3, 4, Direction.EAST, FLOOR[0]),
            new Flight(4, 3, Direction.NORTH, FLOOR[1]),
            new Flight(3, -4, Direction.WEST, FLOOR[2]),
            new Flight(-4, -3, Direction.SOUTH, FLOOR[3])};

    /** Treppenlöcher im Boden darüber (je Lauf zwei) – dort bleibt der Boden offen. */
    public static boolean isHole(int x, int y, int z) {
        for (Flight f : FLIGHTS) {
            if (y == f.floor() + 6 && ((x == f.x(4) && z == f.z(4)) || (x == f.x(5) && z == f.z(5)))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTopStep(int x, int y, int z) {
        for (Flight f : FLIGHTS) {
            if (y == f.floor() + 6 && x == f.x(6) && z == f.z(6)) {
                return true;
            }
        }
        return false;
    }

    private BlockState floorTile(int f, int x, int z) {
        double d = dist(x, z);
        return switch (f) {
            case 0 -> d <= 1.5 ? s(this.p.floorAccent()) : Math.abs(d - 3.2) < 0.5 ? s(this.p.trim())
                    : this.b.chance(x, 31, z, 0.18f) ? s(this.p.floorAccent()) : s(this.p.floor());
            case 1, 3 -> s(this.p.planks());
            default -> d <= 1.5 ? s(this.p.floorAccent()) : Math.abs(d - 2.6) < 0.45 || Math.abs(d - 4.4) < 0.4 ? s(this.p.trim())
                    : s(this.p.floor());
        };
    }

    private void floors() {
        for (int f = 0; f < 4; f++) {
            int y = FLOOR[f];
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    if (dist(x, z) > R_IN) {
                        continue;
                    }
                    this.set(x, y, z, isHole(x, y, z) ? Blocks.AIR.defaultBlockState() : this.floorTile(f, x, z));
                }
            }
        }
    }

    private void stairs() {
        Block[] under = {this.p.trim(), Blocks.BOOKSHELF, this.p.planks(), this.p.wall()};
        for (int k = 0; k < FLIGHTS.length; k++) {
            Flight f = FLIGHTS[k];
            Block step = k == 1 || k == 3 ? this.p.woodStairs() : this.p.trimStairs();
            for (int i = 1; i <= 6; i++) {
                int x = f.x(i);
                int z = f.z(i);
                for (int y = f.floor() + 1; y < f.floor() + i; y++) {
                    this.set(x, y, z, k == 1 && this.b.chance(x, y, z, 0.3f) ? s(Blocks.CHISELED_BOOKSHELF) : s(under[k]));
                }
                this.set(x, f.floor() + i, z, stairs(step, f.dir(), Half.BOTTOM));
                // Kopffreiheit über den oberen Stufen
                this.air(x, f.floor() + i + 1, z);
                this.air(x, f.floor() + i + 2, z);
            }
            // Geländer um das Treppenloch im Geschoss darüber
            int top = f.floor() + 6;
            boolean sanctum = top == SANCTUM;
            Set<Long> rails = new HashSet<>();
            for (int i = 4; i <= 5; i++) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        int x = f.x(i) + dx;
                        int z = f.z(i) + dz;
                        if (isHole(x, top, z) || isTopStep(x, top, z)) {
                            continue;
                        }
                        // die Zugänge zur obersten Stufe bleiben frei
                        if (Math.abs(x - f.x(6)) + Math.abs(z - f.z(6)) == 1) {
                            continue;
                        }
                        double d = dist(x, z);
                        if (d > (sanctum ? R_HALL : R_IN)) {
                            continue;
                        }
                        rails.add(BlockPos.asLong(x, 0, z));
                    }
                }
            }
            for (long r : rails) {
                this.set(BlockPos.getX(r), top + 1, BlockPos.getZ(r), s(this.p.trimWall()));
            }
        }
    }

    // =====================================================================================
    // Eingang
    // =====================================================================================

    private void entrance() {
        // Tür in der Nordwand, Schwelle, Stufe davor
        this.set(0, 1, -6, s(this.p.trim()));
        this.set(0, 1, -5, s(this.p.floorAccent()));
        this.b.door(0, 2, -6, this.p.door(), Direction.SOUTH);
        this.set(0, 4, -6, s(this.p.trim()));
        this.set(0, 1, -7, stairs(this.p.trimStairs(), Direction.SOUTH, Half.BOTTOM));
        this.set(0, 0, -8, s(this.p.trim()));
        // Portal: Pfeiler, Bogen und Wappenstein
        for (int x : new int[] {-1, 1}) {
            this.set(x, 1, -7, s(this.p.trim()));
            for (int y = 2; y <= 4; y++) {
                this.set(x, y, -7, pillar(this.p.pillar(), Direction.Axis.Y));
            }
            this.set(x, 5, -7, s(this.p.trim()));
            this.set(x, 6, -7, s(this.p.lantern()));
        }
        this.set(0, 5, -7, stairs(this.p.trimStairs(), Direction.SOUTH, Half.TOP));
        this.set(0, 5, -6, lit(this.p.glow()));
        this.set(0, 6, -7, slab(this.p.trimSlab(), SlabType.BOTTOM));
        // Banner mit dem Zeichen des Elements neben dem Portal
        for (int x : new int[] {-2, 2}) {
            this.banner(x, 4, -7, Direction.NORTH);
        }
        // Laternenpfähle am Weg
        for (int x : new int[] {-2, 2}) {
            int z = -11;
            this.set(x, 0, z, s(this.p.trim()));
            this.set(x, 1, z, s(this.p.trimWall()));
            this.set(x, 2, z, s(this.p.trimWall()));
            this.set(x, 3, z, s(this.p.lantern()));
        }
    }

    /** Wandbanner mit dem Zeichen des Elements; {@code facing} zeigt von der Wand weg. */
    private void banner(int x, int y, int z, Direction facing) {
        this.set(x, y, z, this.p.wallBanner().defaultBlockState().setValue(WallBannerBlock.FACING, facing));
        BlockPos pos = this.b.world(x, y, z);
        if (this.b.inside(pos) && this.b.level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.BannerBlockEntity banner) {
            net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PAPER);
            stack.set(net.minecraft.core.component.DataComponents.BANNER_PATTERNS, this.bannerPatterns());
            banner.applyComponentsFromItemStack(stack);
        }
    }

    private net.minecraft.world.level.block.entity.BannerPatternLayers bannerPatterns() {
        var patterns = this.b.level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN);
        var builder = new net.minecraft.world.level.block.entity.BannerPatternLayers.Builder();
        net.minecraft.world.item.DyeColor[] colors = switch (this.element) {
            case FIRE -> new net.minecraft.world.item.DyeColor[] {net.minecraft.world.item.DyeColor.ORANGE, net.minecraft.world.item.DyeColor.YELLOW,
                    net.minecraft.world.item.DyeColor.RED, net.minecraft.world.item.DyeColor.BLACK};
            case ICE -> new net.minecraft.world.item.DyeColor[] {net.minecraft.world.item.DyeColor.WHITE, net.minecraft.world.item.DyeColor.WHITE,
                    net.minecraft.world.item.DyeColor.LIGHT_BLUE, net.minecraft.world.item.DyeColor.BLUE};
            case LIGHTNING -> new net.minecraft.world.item.DyeColor[] {net.minecraft.world.item.DyeColor.ORANGE, net.minecraft.world.item.DyeColor.WHITE,
                    net.minecraft.world.item.DyeColor.YELLOW, net.minecraft.world.item.DyeColor.GRAY};
            case NATURE -> new net.minecraft.world.item.DyeColor[] {net.minecraft.world.item.DyeColor.LIME, net.minecraft.world.item.DyeColor.LIME,
                    net.minecraft.world.item.DyeColor.GREEN, net.minecraft.world.item.DyeColor.BROWN};
            case EARTH -> new net.minecraft.world.item.DyeColor[] {net.minecraft.world.item.DyeColor.ORANGE, net.minecraft.world.item.DyeColor.YELLOW,
                    net.minecraft.world.item.DyeColor.BROWN, net.minecraft.world.item.DyeColor.BLACK};
            case WATER -> new net.minecraft.world.item.DyeColor[] {net.minecraft.world.item.DyeColor.CYAN, net.minecraft.world.item.DyeColor.LIGHT_BLUE,
                    net.minecraft.world.item.DyeColor.BLUE, net.minecraft.world.item.DyeColor.WHITE};
        };
        builder.add(patterns.getOrThrow(net.minecraft.world.level.block.entity.BannerPatterns.GRADIENT_UP), colors[0]);
        builder.add(patterns.getOrThrow(this.element == DragonType.NATURE ? net.minecraft.world.level.block.entity.BannerPatterns.FLOWER
                : net.minecraft.world.level.block.entity.BannerPatterns.RHOMBUS_MIDDLE), colors[1]);
        if (this.element != DragonType.NATURE) {
            builder.add(patterns.getOrThrow(net.minecraft.world.level.block.entity.BannerPatterns.CIRCLE_MIDDLE), colors[2]);
        }
        builder.add(patterns.getOrThrow(net.minecraft.world.level.block.entity.BannerPatterns.BORDER), colors[3]);
        return builder.build();
    }

    // =====================================================================================
    // Eingangshalle
    // =====================================================================================

    private void hall() {
        int y = FLOOR[0] + 1;
        // Teppichring um das Herzstück
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                double d = dist(x, z);
                if (d > 1.6 && d <= 2.7) {
                    this.set(x, y, z, s(Math.abs(x) + Math.abs(z) == 2 ? this.p.carpet2() : this.p.carpet()));
                }
            }
        }
        this.hallCenter(y);
        // Bänke an den Seiten, dazwischen Sockel mit Laternen
        for (int side : new int[] {-1, 1}) {
            Direction back = side > 0 ? Direction.EAST : Direction.WEST;
            this.set(4 * side, y, -2, stairs(this.p.woodStairs(), back, Half.BOTTOM));
            this.set(4 * side, y, -1, stairs(this.p.woodStairs(), back, Half.BOTTOM));
            this.set(4 * side, y, -3, s(this.p.trim()));
            this.set(4 * side, y + 1, -3, s(this.p.lantern()));
            this.set(5 * side, y, 0, s(Blocks.DECORATED_POT));
        }
        // Wandbanner neben dem Eingang (innen)
        for (int x : new int[] {-2, 2}) {
            this.banner(x, y + 2, -4, Direction.SOUTH);
        }
        // Ostseite: Truhe des Torwächters und ein Fass; Westseite: Werkbank und Fass
        this.b.chest(4, y, 2, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.MAGE_TOWER_HALL);
        this.set(4, y, 1, s(Blocks.BARREL));
        this.set(4, y + 1, 1, this.candles(2));
        this.set(-4, y, 1, s(Blocks.BARREL));
        this.set(-4, y, 2, s(Blocks.BARREL));
        this.set(-4, y + 1, 2, s(Blocks.BARREL));
        // Licht: vier Hängelaternen
        for (int x : new int[] {-2, 2}) {
            for (int z : new int[] {-2, 2}) {
                this.set(x, FLOOR[1] - 1, z, this.hanging(this.p.lantern()));
            }
        }
    }

    /** Das Herzstück der Halle – je Element anders. */
    private void hallCenter(int y) {
        switch (this.element) {
            case FIRE -> {
                // Feuerbecken: ein Kranz aus Ziegelstufen um ein Lagerfeuer, Ketten von der Decke
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    this.set(dir.getStepX(), y, dir.getStepZ(), slab(this.p.trimSlab(), SlabType.BOTTOM));
                }
                for (int x : new int[] {-1, 1}) {
                    for (int z : new int[] {-1, 1}) {
                        this.set(x, y, z, s(this.p.trimWall()));
                        this.set(x, y + 1, z, s(Blocks.LANTERN));
                    }
                }
                this.set(0, y - 1, 0, s(Blocks.NETHERRACK));
                this.set(0, y, 0, s(Blocks.FIRE));
                for (int k = y + 2; k < FLOOR[1]; k++) {
                    this.set(0, k, 0, pillar(Blocks.IRON_CHAIN, Direction.Axis.Y));
                }
                this.set(0, y + 1, 0, this.hanging(Blocks.LANTERN));
            }
            case ICE -> {
                // gefrorener Brunnen: Becken aus Blaueis, in der Mitte eine Säule mit Frostkristall
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        this.set(x, y - 1, z, s(Blocks.BLUE_ICE));
                        if (x != 0 || z != 0) {
                            this.set(x, y, z, Math.abs(x) + Math.abs(z) == 2 ? s(this.p.trimWall()) : s(ModBlocks.GLACIER_ICE.get()));
                        }
                    }
                }
                this.set(0, y, 0, s(Blocks.PACKED_ICE));
                this.set(0, y + 1, 0, s(Blocks.BLUE_ICE));
                this.set(0, y + 2, 0, s(ModBlocks.FROST_CRYSTAL.get()));
                for (int x : new int[] {-1, 1}) {
                    for (int z : new int[] {-1, 1}) {
                        this.set(x, y + 1, z, s(Blocks.SOUL_LANTERN));
                    }
                }
            }
            case LIGHTNING -> {
                // Funkenturm: Kupferrost im Boden, eine Säule aus Kupferlampe und Blitzableiter, vier kleine Ableiter
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        this.set(x, y - 1, z, s(TowerPalette.copper(Blocks.COPPER_GRATE, net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED)));
                    }
                }
                this.set(0, y - 1, 0, lit(this.p.glow()));
                this.set(0, y, 0, s(Blocks.POLISHED_DEEPSLATE));
                this.set(0, y + 1, 0, lit(this.p.glow()));
                this.set(0, y + 2, 0, s(TowerPalette.copper(Blocks.LIGHTNING_ROD, net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED)));
                this.set(0, y + 3, 0, s(Blocks.END_ROD));
                for (int x : new int[] {-1, 1}) {
                    for (int z : new int[] {-1, 1}) {
                        this.set(x, y, z, s(this.p.trimWall()));
                        this.set(x, y + 1, z, s(TowerPalette.copper(Blocks.LIGHTNING_ROD,
                                net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED)));
                    }
                }
            }
            case NATURE -> {
                // ein kleiner Baum in einem moosigen Beet, Blumen ringsum
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        this.set(x, y - 1, z, s(Blocks.MOSS_BLOCK));
                        if (x != 0 || z != 0) {
                            this.set(x, y, z, Math.abs(x) + Math.abs(z) == 2 ? s(Blocks.MOSS_CARPET)
                                    : s(new Block[] {ModBlocks.LAVENDER.get(), ModBlocks.GARDEN_ROSE.get(), ModBlocks.LIFE_BLOSSOM.get(),
                                            ModBlocks.GROVE_BELL.get()}[Math.floorMod(x * 3 + z, 4)]));
                        }
                    }
                }
                this.set(0, y - 1, 0, s(Blocks.ROOTED_DIRT));
                for (int k = y; k <= y + 2; k++) {
                    this.set(0, k, 0, pillar(ModBlocks.ANCIENT_LOG.get(), Direction.Axis.Y));
                }
                BlockState leaves = s(Blocks.FLOWERING_AZALEA_LEAVES).setValue(LeavesBlock.PERSISTENT, true);
                BlockState leaves2 = s(Blocks.AZALEA_LEAVES).setValue(LeavesBlock.PERSISTENT, true);
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        this.set(x, y + 3, z, (x + z) % 2 == 0 ? leaves : leaves2);
                        if (Math.abs(x) + Math.abs(z) <= 1) {
                            this.set(x, y + 4, z, leaves);
                        }
                    }
                }
                this.set(0, y + 3, 0, pillar(ModBlocks.ANCIENT_LOG.get(), Direction.Axis.Y));
            }
            case EARTH -> {
                // Bernstein-Obelisk auf einem Sockel aus Schiefer, rohe Erzbrocken ringsum
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        this.set(x, y - 1, z, s(ModBlocks.SHALE.base().get()));
                    }
                }
                this.set(0, y, 0, s(Blocks.CHISELED_TUFF_BRICKS));
                this.set(0, y + 1, 0, s(ModBlocks.AMBER_CRYSTAL.get()));
                this.set(0, y + 2, 0, s(ModBlocks.AMBER_CRYSTAL.get()));
                this.set(0, y + 3, 0, s(ModBlocks.AMBER_CLUSTER.get()));
                this.set(1, y, 1, s(Blocks.RAW_GOLD_BLOCK));
                this.set(-1, y, -1, s(Blocks.RAW_COPPER_BLOCK));
                this.set(1, y, -1, s(Blocks.RAW_IRON_BLOCK));
                this.set(-1, y, 1, s(Blocks.RAW_GOLD_BLOCK));
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    this.set(dir.getStepX(), y, dir.getStepZ(), slab(this.p.trimSlab(), SlabType.BOTTOM));
                }
                // Tropfsteine hängen von der Decke
                for (int x : new int[] {-2, 2}) {
                    for (int z : new int[] {-1, 1}) {
                        this.dripstone(x, FLOOR[1] - 1, z, 2);
                    }
                }
            }
            case WATER -> {
                // Quellbecken: in den Boden eingelassen, aus vier Quellen sprudelt es (Seelensand darunter), dazwischen
                // leuchten Seegurken; in der Mitte eine Perlmuttsäule mit Meereslaterne
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        double d = dist(x, z);
                        if (d <= 1.5) {
                            boolean spring = Math.abs(x) + Math.abs(z) == 1;
                            this.set(x, y - 2, z, spring ? s(Blocks.SOUL_SAND) : s(Blocks.DARK_PRISMARINE));
                            this.set(x, y - 1, z, x == 0 && z == 0 ? s(ModBlocks.NACRE_BLOCK.get())
                                    : spring ? s(Blocks.BUBBLE_COLUMN).setValue(BubbleColumnBlock.DRAG_DOWN, false) : s(Blocks.WATER));
                        } else if (d <= 2.3) {
                            this.set(x, y - 1, z, s(Blocks.DARK_PRISMARINE));
                        }
                    }
                }
                this.set(0, y, 0, s(ModBlocks.NACRE_BLOCK.get()));
                this.set(0, y + 1, 0, s(Blocks.SEA_LANTERN));
                this.set(0, y + 2, 0, slab(Blocks.DARK_PRISMARINE_SLAB, SlabType.BOTTOM));
                this.set(1, y - 1, 1, s(Blocks.SEA_PICKLE).setValue(BlockStateProperties.PICKLES, 3).setValue(BlockStateProperties.WATERLOGGED, true));
                this.set(-1, y - 1, -1, s(Blocks.SEA_PICKLE).setValue(BlockStateProperties.PICKLES, 2).setValue(BlockStateProperties.WATERLOGGED, true));
                this.set(1, y - 1, -1, s(Blocks.SEA_PICKLE).setValue(BlockStateProperties.PICKLES, 1).setValue(BlockStateProperties.WATERLOGGED, true));
            }
        }
    }

    /** Hängender Tropfstein (von oben nach unten), {@code length} Blöcke lang. */
    private void dripstone(int x, int y, int z, int length) {
        for (int i = 0; i < length; i++) {
            SpeleothemThickness thickness = i == length - 1 ? SpeleothemThickness.TIP : i == 0 && length > 2 ? SpeleothemThickness.BASE
                    : SpeleothemThickness.FRUSTUM;
            this.set(x, y - i, z, s(Blocks.POINTED_DRIPSTONE).setValue(BlockStateProperties.VERTICAL_DIRECTION, Direction.DOWN)
                    .setValue(BlockStateProperties.SPELEOTHEM_THICKNESS, thickness));
        }
    }

    /** Stehender Tropfstein (vom Boden nach oben). */
    private void stalagmite(int x, int y, int z, int length) {
        for (int i = 0; i < length; i++) {
            SpeleothemThickness thickness = i == length - 1 ? SpeleothemThickness.TIP : i == 0 && length > 2 ? SpeleothemThickness.BASE
                    : SpeleothemThickness.FRUSTUM;
            this.set(x, y + i, z, s(Blocks.POINTED_DRIPSTONE).setValue(BlockStateProperties.VERTICAL_DIRECTION, Direction.UP)
                    .setValue(BlockStateProperties.SPELEOTHEM_THICKNESS, thickness));
        }
    }

    // =====================================================================================
    // Bibliothek
    // =====================================================================================

    private void library() {
        int y = FLOOR[1] + 1;
        int[][] shelves = {
                {-4, -3}, {-4, -2}, {-4, -1}, {-4, 1}, {-4, 2}, {-4, 3},
                {-3, -4}, {-2, -4}, {-1, -4}, {1, -4}, {2, -4}, {3, -4},
                {-3, 4}, {-2, 4}, {-1, 4}};
        for (int[] c : shelves) {
            for (int k = 0; k < 3; k++) {
                this.set(c[0], y + k, c[1], this.b.chance(c[0], y + k, c[1], 0.28f) ? s(Blocks.CHISELED_BOOKSHELF) : s(Blocks.BOOKSHELF));
            }
            if (this.b.chance(c[0], 77, c[1], 0.3f)) {
                this.set(c[0], y + 3, c[1], this.candles(1 + (int) (this.b.noise(c[0], 78, c[1]) * 3)));
            }
        }
        // Lesepult vor dem Westfenster mit den Aufzeichnungen des Magiers
        MageTowerBooks.lectern(this.b, -4, y, 0, Direction.EAST, this.element);
        // Schreibtisch vor dem Nordfenster
        this.set(0, y, -4, s(ModBlocks.OAK_TABLE.get()));
        this.set(0, y + 1, -4, this.candles(3));
        this.set(0, y, -3, chair(Direction.NORTH));
        // runder Lesetisch in der Mitte mit vier Stühlen und Teppich
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                double d = dist(x, z);
                if (d > 1.2 && d <= 2.3) {
                    this.set(x, y, z, s(this.p.carpet()));
                }
            }
        }
        this.set(0, y, 0, s(ModBlocks.OAK_TABLE.get()));
        this.set(0, y + 1, 0, this.candles(4));
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            this.set(dir.getStepX(), y, dir.getStepZ(), chair(dir.getOpposite()));
        }
        this.set(0, FLOOR[2] - 1, 0, s(ModBlocks.CHANDELIER.get()));
        // Truhe in der Ecke
        this.b.chest(-3, y, -3, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), ModLootTables.MAGE_TOWER_LIBRARY);
        this.set(-3, y, 3, s(this.p.lantern()));
    }

    // =====================================================================================
    // Laboratorium
    // =====================================================================================

    private void laboratory() {
        int y = FLOOR[2] + 1;
        // der Magietisch in der Mitte, darum ein Teppichring
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                double d = dist(x, z);
                if (d > 1.1 && d <= 2.3) {
                    this.set(x, y, z, s(Math.abs(x) == 2 || Math.abs(z) == 2 ? this.p.carpet() : this.p.carpet2()));
                }
            }
        }
        this.set(0, y, 0, s(ModBlocks.MAGIC_TABLE.get()));
        // Arbeitstresen an der Westwand: Braustände, Kessel, Kräuter
        Block counter = this.p.trim();
        this.set(-4, y, -2, s(Blocks.BARREL).setValue(BlockStateProperties.FACING, Direction.UP));
        this.set(-4, y + 1, -2, s(Blocks.BREWING_STAND));
        this.set(-4, y, -1, s(counter));
        this.set(-4, y + 1, -1, s(Blocks.BREWING_STAND));
        this.set(-4, y, 0, this.cauldron());
        this.set(-4, y, 1, s(counter));
        this.set(-4, y + 1, 1, s(this.herb()));
        this.set(-4, y, 2, s(counter));
        this.set(-4, y + 1, 2, this.candles(3));
        this.set(-4, y, 3, s(Blocks.BARREL));
        this.set(-4, y + 1, 3, s(Blocks.DECORATED_POT));
        for (int z = -2; z <= 3; z++) {
            this.set(-4, y + 3, z, stairs(this.p.woodStairs(), Direction.WEST, Half.TOP));
        }
        this.set(-4, y + 4, -1, s(Blocks.DECORATED_POT));
        this.set(-4, y + 4, 2, s(this.herb()));
        // Vorräte an der Südwand
        for (int x = -3; x <= -1; x++) {
            this.set(x, y, 4, s(Blocks.BARREL).setValue(BlockStateProperties.FACING, Direction.NORTH));
            this.set(x, y + 1, 4, x == -2 ? s(Blocks.BARREL).setValue(BlockStateProperties.FACING, Direction.NORTH) : s(Blocks.DECORATED_POT));
        }
        this.b.chest(1, y, 4, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH), ModLootTables.MAGE_TOWER_LABORATORY);
        this.set(2, y, 4, s(Blocks.CHISELED_BOOKSHELF));
        this.set(2, y + 1, 4, s(Blocks.CHISELED_BOOKSHELF));
        this.set(3, y, 4, this.cauldron());
        this.set(0, y, 5, s(this.herb()));
        // Ostseite: Fass, Tontopf und eine Kartenwerkbank
        this.set(4, y, 2, s(Blocks.CARTOGRAPHY_TABLE));
        this.set(4, y, 3, s(Blocks.BARREL).setValue(BlockStateProperties.FACING, Direction.UP));
        this.set(4, y + 1, 3, s(Blocks.DECORATED_POT));
        // Licht
        this.set(-2, FLOOR[3] - 1, 2, this.hanging(this.p.lantern()));
        this.set(2, FLOOR[3] - 1, 2, this.hanging(this.p.lantern()));
        this.set(2, FLOOR[3] - 1, -2, this.hanging(this.p.lantern()));
    }

    private BlockState cauldron() {
        return switch (this.element) {
            case FIRE -> s(Blocks.LAVA_CAULDRON);
            case ICE -> s(Blocks.POWDER_SNOW_CAULDRON).setValue(LayeredCauldronBlock.LEVEL, 3);
            default -> s(Blocks.WATER_CAULDRON).setValue(LayeredCauldronBlock.LEVEL, 3);
        };
    }

    /** Topfpflanze nach Art des Elements. */
    private Block herb() {
        return switch (this.element) {
            case FIRE -> ModBlocks.POTTED_FIRE_LILY.get();
            case ICE -> ModBlocks.POTTED_FROST_FLOWER.get();
            case LIGHTNING -> Blocks.POTTED_BLUE_ORCHID;
            case NATURE -> ModBlocks.POTTED_LIFE_BLOSSOM.get();
            case EARTH -> Blocks.POTTED_DEAD_BUSH;
            case WATER -> ModBlocks.POTTED_MOONFLOWER.get();
        };
    }

    // =====================================================================================
    // Kammer
    // =====================================================================================

    private void quarters() {
        int y = FLOOR[3] + 1;
        // Bett mit dem Kopfende an der Ostwand, daneben ein Nachttisch, am Fußende die Truhe
        this.b.bed(3, y, 1, this.p.bed(), Direction.EAST);
        this.set(4, y, 2, s(Blocks.CHISELED_BOOKSHELF));
        this.set(4, y + 1, 2, this.candles(2));
        this.b.chest(2, y, 1, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.MAGE_TOWER_QUARTERS);
        this.set(5, y, 0, s(this.herb()));
        // Schrank aus Fässern
        for (int z = -3; z <= -2; z++) {
            for (int k = 0; k < 2; k++) {
                this.set(4, y + k, z, s(Blocks.BARREL).setValue(BlockStateProperties.FACING, Direction.WEST));
            }
            this.set(4, y + 2, z, slab(this.p.woodSlab(), SlabType.BOTTOM));
        }
        // Schreibtisch
        this.set(2, y, -3, s(ModBlocks.OAK_TABLE.get()));
        this.set(2, y + 1, -3, this.candles(1));
        this.set(2, y, -2, chair(Direction.NORTH));
        // Zaubertisch mit Bücherwand an der Südseite
        this.set(0, y, 2, s(Blocks.ENCHANTING_TABLE));
        for (int x : new int[] {-2, -1, 1, 2}) {
            for (int k = 0; k < 2; k++) {
                this.set(x, y + k, 4, s(Blocks.BOOKSHELF));
            }
        }
        for (int x : new int[] {-2, 2}) {
            for (int k = 0; k < 2; k++) {
                this.set(x, y + k, 3, s(Blocks.BOOKSHELF));
            }
        }
        this.set(-3, y, 4, s(this.p.lantern()));
        // Teppich in der Mitte, Kronleuchter darüber
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                this.set(x, y, z, s(x == 0 && z == 0 ? this.p.carpet2() : this.p.carpet()));
            }
        }
        this.set(0, SANCTUM - 1, 0, s(ModBlocks.CHANDELIER.get()));
    }

    // =====================================================================================
    // Krone: Konsolen und Mauern des Sanktums
    // =====================================================================================

    private void crown() {
        for (int x = -11; x <= 11; x++) {
            for (int z = -11; z <= 11; z++) {
                double d = dist(x, z);
                double a = angle(x, z);
                // nach außen getreppte Auskragung unter dem Sanktum: drei Ringe, jeder eine Handbreit weiter
                if (d > R_SHAFT && d <= 7.4) {
                    this.set(x, SANCTUM - 3, z, ringStair(this.p.trimStairs(), x, z, Half.TOP));
                }
                if (d > R_SHAFT && d <= 8.4) {
                    this.set(x, SANCTUM - 2, z, d > 7.4 ? ringStair(this.p.trimStairs(), x, z, Half.TOP) : s(this.p.trim()));
                }
                if (d > R_SHAFT && d <= 9.4) {
                    this.set(x, SANCTUM - 1, z, d > 8.4 ? ringStair(this.p.trimStairs(), x, z, Half.TOP) : this.wallStone(x, SANCTUM - 1, z));
                }
                if (d > R_CROWN) {
                    // Lisenen außen an der Krone
                    if (nearAngle(a, 45.0, 22.5, 4.0) && d <= R_CROWN + 0.8) {
                        for (int y = SANCTUM; y <= WALL_TOP; y++) {
                            this.set(x, y, z, pillar(this.p.pillar(), Direction.Axis.Y));
                        }
                    }
                    continue;
                }
                // Boden des Sanktums (innen das Ritualmuster, siehe sanctum()) und Mauern
                this.set(x, SANCTUM, z, d > R_HALL ? s(this.p.trim()) : s(this.p.floor()));
                for (int y = SANCTUM + 1; y <= WALL_TOP; y++) {
                    if (d <= R_HALL) {
                        this.air(x, y, z);
                        continue;
                    }
                    boolean window = nearAngle(a, 45.0, 0.0, 7.5) && y >= SANCTUM + 2 && y <= SANCTUM + 6;
                    BlockState state;
                    if (window) {
                        state = s(this.p.glass());
                    } else if (y == SANCTUM + 1 || y == SANCTUM + 7 || y == WALL_TOP) {
                        state = s(this.p.trim());
                    } else {
                        state = this.wallStone(x, y, z);
                    }
                    this.set(x, y, z, state);
                }
            }
        }
    }

    // =====================================================================================
    // Sanktum
    // =====================================================================================

    private void sanctum() {
        int y = SANCTUM;
        // Ritualkreis im Boden: Hexagramm in einem Ring, in der Mitte ein Lichtstein unter dem Magier
        double[][] star = new double[6][];
        for (int k = 0; k < 6; k++) {
            double a = Math.toRadians(30 + k * 60);
            star[k] = new double[] {Math.cos(a) * STAR, Math.sin(a) * STAR};
        }
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                double d = dist(x, z);
                if (d > R_HALL || isHole(x, y, z) || isTopStep(x, y, z)) {
                    continue;
                }
                boolean line = false;
                for (int k = 0; k < 6; k++) {
                    double[] from = star[k];
                    double[] to = star[(k + 2) % 6];
                    line |= segmentDistance(x, z, from[0], from[1], to[0], to[1]) < 0.5;
                }
                BlockState state;
                if (d <= 0.5) {
                    state = lit(this.p.glow());
                } else if (d <= 1.5 || line || Math.abs(d - (STAR + 0.4)) < 0.5) {
                    state = s(this.p.trim());
                } else if (d > STAR + 1.4) {
                    state = s(this.p.floorAccent());
                } else {
                    state = this.b.chance(x, 91, z, 0.05f) ? s(this.p.floorAccent()) : s(this.p.floor());
                }
                this.set(x, y, z, state);
            }
        }
        // Lichtpunkte an den Spitzen des Sterns
        for (double[] point : star) {
            int x = (int) Math.round(point[0]);
            int z = (int) Math.round(point[1]);
            if (!isHole(x, y, z) && !isTopStep(x, y, z)) {
                this.set(x, y, z, lit(this.p.glow()));
            }
        }
        // Lisenen innen zwischen den Fenstern
        for (int x = -9; x <= 9; x++) {
            for (int z = -9; z <= 9; z++) {
                double d = dist(x, z);
                if (d > R_HALL - 0.75 && d <= R_HALL && nearAngle(angle(x, z), 45.0, 22.5, 6.0)) {
                    for (int k = y + 1; k <= WALL_TOP; k++) {
                        this.set(x, k, z, k == WALL_TOP ? s(this.p.trim()) : pillar(this.p.pillar(), Direction.Axis.Y));
                    }
                    this.set(x, y + 1, z, s(this.p.trim()));
                }
            }
        }
        // Feuerschalen (Becken des Elements) an drei Seiten – nicht dort, wo die Treppe ankommt
        int[][] braziers = {{5, 5}, {5, -5}, {-5, -5}};
        for (int[] c : braziers) {
            this.brazier(c[0], y + 1, c[1]);
        }
        // Truhe des Erzmagiers hinter ihm an der Wand
        this.b.chest(7, y + 1, 0, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.MAGE_TOWER_SANCTUM);
        this.set(7, y + 1, -1, s(this.p.trim()));
        this.set(7, y + 2, -1, this.candles(3));
        this.set(7, y + 1, 1, s(this.p.trim()));
        this.set(7, y + 2, 1, this.candles(2));
        // Lichtkugel, die an einer langen Kette aus der Dachspitze hängt
        int orb = WALL_TOP + 3;
        int ceiling = EAVE;
        while (ceiling < PEAK && !roofBlock(0, ceiling, 0)) {
            ceiling++;
        }
        for (int k = orb + 2; k < ceiling; k++) {
            this.set(0, k, 0, pillar(Blocks.IRON_CHAIN, Direction.Axis.Y));
        }
        this.set(0, orb + 1, 0, s(this.p.trim()));
        this.set(0, orb, 0, lit(this.p.glow()));
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            this.set(dir.getStepX(), orb + 1, dir.getStepZ(), stairs(this.p.trimStairs(), dir.getOpposite(), Half.BOTTOM));
            this.set(dir.getStepX(), orb, dir.getStepZ(), s(this.p.trim()));
            this.set(dir.getStepX(), orb - 1, dir.getStepZ(), this.hanging(this.p.lantern()));
        }
        this.set(0, orb - 1, 0, s(ModBlocks.CHANDELIER.get()));
        // Laternen an Ketten unter dem Kegeldach, damit die Spitze des Saals nicht im Dunkeln liegt
        for (int k = 0; k < 8; k++) {
            double a = Math.toRadians(22.5 + k * 45);
            int x = (int) Math.round(Math.cos(a) * 4.6);
            int z = (int) Math.round(Math.sin(a) * 4.6);
            int top = EAVE;
            while (top < PEAK && !roofBlock(x, top, z)) {
                top++;
            }
            int chain = Math.min(3, top - EAVE - 1);
            for (int i = 1; i <= chain; i++) {
                this.set(x, top - i, z, pillar(Blocks.IRON_CHAIN, Direction.Axis.Y));
            }
            this.set(x, top - chain - 1, z, this.hanging(this.p.lantern()));
        }
    }

    /** Abstand eines Feldes (Mitte) von einer Strecke. */
    static double segmentDistance(double px, double pz, double ax, double az, double bx, double bz) {
        double dx = bx - ax;
        double dz = bz - az;
        double t = ((px - ax) * dx + (pz - az) * dz) / (dx * dx + dz * dz);
        t = Math.max(0.0, Math.min(1.0, t));
        double qx = ax + dx * t - px;
        double qz = az + dz * t - pz;
        return Math.sqrt(qx * qx + qz * qz);
    }

    /** Steht in dieser Lage des Dachs ein Block (Außenhaut oder Traufe)? */
    static boolean roofBlock(int x, int y, int z) {
        if (!inRoof(x, y, z)) {
            return false;
        }
        boolean side = !inRoof(x + 1, y, z) || !inRoof(x - 1, y, z) || !inRoof(x, y, z + 1) || !inRoof(x, y, z - 1);
        // neben einer Dachtreppe bleibt nichts hohl, sonst schiene durch ihre offene obere Hälfte der Himmel herein
        boolean besideStair = roofTopCell(x + 1, y, z) || roofTopCell(x - 1, y, z) || roofTopCell(x, y, z + 1) || roofTopCell(x, y, z - 1);
        return !inRoof(x, y + 1, z) || side || besideStair || y == EAVE && dist(x, z) > R_HALL;
    }

    /** Oberste Zelle einer Dachsäule (dort liegt eine Treppe). */
    static boolean roofTopCell(int x, int y, int z) {
        return inRoof(x, y, z) && !inRoof(x, y + 1, z);
    }

    /** Feuerschale: Sockel und darauf das Licht des Elements. */
    private void brazier(int x, int y, int z) {
        this.set(x, y, z, s(this.p.trim()));
        this.set(x, y + 1, z, s(this.p.trimWall()));
        switch (this.element) {
            case FIRE -> {
                this.set(x, y + 2, z, s(Blocks.NETHERRACK));
                this.set(x, y + 3, z, s(Blocks.FIRE));
            }
            case ICE -> {
                this.set(x, y + 2, z, s(Blocks.SOUL_SOIL));
                this.set(x, y + 3, z, s(Blocks.SOUL_FIRE));
            }
            case LIGHTNING -> {
                this.set(x, y + 2, z, lit(this.p.glow()));
                this.set(x, y + 3, z, s(TowerPalette.copper(Blocks.LIGHTNING_ROD, net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED)));
            }
            case NATURE -> {
                this.set(x, y + 2, z, s(Blocks.SHROOMLIGHT));
                this.set(x, y + 3, z, s(Blocks.FLOWERING_AZALEA));
            }
            case EARTH -> {
                this.set(x, y + 2, z, s(ModBlocks.AMBER_CRYSTAL.get()));
                this.set(x, y + 3, z, s(ModBlocks.AMBER_CLUSTER.get()));
            }
            case WATER -> {
                this.set(x, y + 2, z, s(Blocks.SEA_LANTERN));
                this.set(x, y + 3, z, s(Blocks.CONDUIT).setValue(BlockStateProperties.WATERLOGGED, false));
            }
        }
    }

    // =====================================================================================
    // Dach
    // =====================================================================================

    private void roof() {
        // Traufgesims unter dem Dachrand
        for (int x = -11; x <= 11; x++) {
            for (int z = -11; z <= 11; z++) {
                double d = dist(x, z);
                if (d > R_CROWN && d <= R_EAVE) {
                    this.set(x, WALL_TOP, z, ringStair(this.p.trimStairs(), x, z, Half.TOP));
                }
            }
        }
        int band = EAVE + 7;
        for (int y = EAVE; y <= PEAK; y++) {
            double r = roofRadius(y);
            int ri = (int) Math.ceil(r);
            for (int x = -ri; x <= ri; x++) {
                for (int z = -ri; z <= ri; z++) {
                    if (!inRoof(x, y, z)) {
                        continue;
                    }
                    boolean up = inRoof(x, y + 1, z);
                    boolean accent = y == band || y >= PEAK - 1;
                    Block full = accent ? this.p.trim() : this.p.roof();
                    Block stair = accent ? this.p.trimStairs() : this.p.roofStairs();
                    if (!up) {
                        this.set(x, y, z, this.roofTop(stair, full, x, y, z));
                    } else if (roofBlock(x, y, z)) {
                        this.set(x, y, z, s(full));
                    } else {
                        this.air(x, y, z);
                    }
                }
            }
        }
    }

    /** Oberster Block einer Dachsäule: Treppe mit der hohen Seite hangaufwärts, an den Graten als Außenecke. */
    private BlockState roofTop(Block stair, Block full, int x, int y, int z) {
        boolean n = inRoof(x, y + 1, z - 1);
        boolean so = inRoof(x, y + 1, z + 1);
        boolean e = inRoof(x + 1, y + 1, z);
        boolean w = inRoof(x - 1, y + 1, z);
        int count = (n ? 1 : 0) + (so ? 1 : 0) + (e ? 1 : 0) + (w ? 1 : 0);
        if (count == 1) {
            Direction facing = n ? Direction.NORTH : so ? Direction.SOUTH : e ? Direction.EAST : Direction.WEST;
            return stairs(stair, facing, Half.BOTTOM);
        }
        if (count == 2 && (n || so) && (e || w)) {
            Direction facing = n ? Direction.NORTH : Direction.SOUTH;
            StairsShape shape = n ? (e ? StairsShape.OUTER_RIGHT : StairsShape.OUTER_LEFT) : (e ? StairsShape.OUTER_LEFT : StairsShape.OUTER_RIGHT);
            return VillageBuilder.stairs(stair, facing, Half.BOTTOM, shape);
        }
        if (count == 0) {
            return s(full);
        }
        // eingezogene Stellen: zur Mitte hin
        return stairs(stair, outward(angle(x, z)).getOpposite(), Half.BOTTOM);
    }

    // =====================================================================================
    // Spitze
    // =====================================================================================

    private void spire() {
        int y = PEAK + 1;
        switch (this.element) {
            case FIRE -> {
                this.set(0, y, 0, s(Blocks.GOLD_BLOCK));
                this.set(0, y + 1, 0, s(Blocks.NETHERRACK));
                this.set(0, y + 2, 0, s(Blocks.FIRE));
            }
            case ICE -> {
                this.set(0, y, 0, s(Blocks.BLUE_ICE));
                this.set(0, y + 1, 0, s(Blocks.PACKED_ICE));
                this.set(0, y + 2, 0, s(ModBlocks.FROST_CRYSTAL.get()));
                this.set(0, y + 3, 0, s(Blocks.END_ROD));
            }
            case LIGHTNING -> {
                this.set(0, y, 0, s(TowerPalette.copper(Blocks.COPPER_BLOCK, net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED)));
                this.set(0, y + 1, 0, s(TowerPalette.copper(Blocks.LIGHTNING_ROD, net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED)));
                this.set(0, y + 2, 0, s(TowerPalette.copper(Blocks.LIGHTNING_ROD, net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED)));
                this.set(0, y + 3, 0, s(TowerPalette.copper(Blocks.LIGHTNING_ROD, net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED)));
            }
            case NATURE -> {
                BlockState leaves = s(Blocks.FLOWERING_AZALEA_LEAVES).setValue(LeavesBlock.PERSISTENT, true);
                this.set(0, y, 0, pillar(ModBlocks.ANCIENT_LOG.get(), Direction.Axis.Y));
                this.set(0, y + 1, 0, pillar(ModBlocks.ANCIENT_LOG.get(), Direction.Axis.Y));
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x != 0 || z != 0) {
                            this.set(x, y + 1, z, leaves);
                        }
                        if (Math.abs(x) + Math.abs(z) <= 1) {
                            this.set(x, y + 2, z, leaves);
                        }
                    }
                }
                this.set(0, y + 3, 0, leaves);
            }
            case EARTH -> {
                this.set(0, y, 0, s(Blocks.RAW_GOLD_BLOCK));
                this.set(0, y + 1, 0, s(ModBlocks.AMBER_CRYSTAL.get()));
                this.set(0, y + 2, 0, s(ModBlocks.AMBER_CLUSTER.get()));
            }
            case WATER -> {
                this.set(0, y, 0, s(ModBlocks.NACRE_BLOCK.get()));
                this.set(0, y + 1, 0, s(Blocks.SEA_LANTERN));
                this.set(0, y + 2, 0, s(Blocks.PRISMARINE_WALL));
                this.set(0, y + 3, 0, s(Blocks.END_ROD));
            }
        }
    }

    // =====================================================================================
    // Umgebung
    // =====================================================================================

    /** Vier Obelisken auf der Terrasse und je nach Element Eisnadeln, Blitzableiter, Blumen, Felsen oder Becken. */
    private void surroundings() {
        for (int k = 0; k < 4; k++) {
            double a = Math.toRadians(45 + k * 90);
            int x = (int) Math.round(Math.cos(a) * 9.6);
            int z = (int) Math.round(Math.sin(a) * 9.6);
            this.obelisk(x, z);
        }
        List<int[]> spots = new ArrayList<>();
        for (int k = 0; k < 10; k++) {
            double a = Math.toRadians(k * 36 + 18 + this.b.noise(k, 5, 5) * 14);
            if (angleDiff(Math.toDegrees(a), 270.0) < 25.0) {
                continue;                           // der Weg zur Tür bleibt frei
            }
            double r = 12.8 + this.b.noise(k, 6, 6) * 1.6;
            spots.add(new int[] {(int) Math.round(Math.cos(a) * r), (int) Math.round(Math.sin(a) * r), k});
        }
        for (int[] spot : spots) {
            int gy = this.groundY(spot[0], spot[1]);
            if (gy == Integer.MIN_VALUE || Math.abs(gy) > 6) {
                continue;
            }
            this.decoration(spot[0], gy + 1, spot[1], spot[2]);
        }
    }

    /** Höhe des Geländes (oberster fester Block) relativ zur Terrasse – in jedem Chunk gleich berechnet. */
    private int groundY(int x, int z) {
        return this.ground.applyAsInt(x, z);
    }

    private void obelisk(int x, int z) {
        this.set(x, 1, z, s(this.p.trim()));
        this.set(x, 2, z, pillar(this.p.pillar(), Direction.Axis.Y));
        this.set(x, 3, z, pillar(this.p.pillar(), Direction.Axis.Y));
        this.set(x, 4, z, s(this.p.trim()));
        switch (this.element) {
            case FIRE -> {
                this.set(x, 5, z, s(Blocks.NETHERRACK));
                this.set(x, 6, z, s(Blocks.FIRE));
            }
            case ICE -> this.set(x, 5, z, s(ModBlocks.FROST_CRYSTAL.get()));
            case LIGHTNING -> this.set(x, 5, z, s(TowerPalette.copper(Blocks.LIGHTNING_ROD,
                    net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED)));
            case NATURE -> this.set(x, 5, z, s(Blocks.FLOWERING_AZALEA));
            case EARTH -> this.set(x, 5, z, s(ModBlocks.AMBER_CLUSTER.get()));
            case WATER -> this.set(x, 5, z, s(Blocks.SEA_LANTERN));
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            this.set(x + dir.getStepX(), 1, z + dir.getStepZ(), stairs(this.p.trimStairs(), dir.getOpposite(), Half.BOTTOM));
        }
    }

    /** Eine Zier außerhalb der Terrasse, auf dem Gelände (Höhe {@code y} = erster Block über dem Boden). */
    private void decoration(int x, int y, int z, int k) {
        switch (this.element) {
            case FIRE -> {
                // Basaltsäulen wie erstarrte Lava, unterschiedlich hoch, am Fuß Lavagestein
                int[][] cols = {{0, 0}, {1, 0}, {0, 1}, {-1, 1}, {1, -1}};
                for (int i = 0; i < cols.length; i++) {
                    int h = 1 + (int) (this.b.noise(x + cols[i][0], 7, z + cols[i][1]) * (i == 0 ? 5 : 3));
                    this.set(x + cols[i][0], y - 1, z + cols[i][1], s(ModBlocks.VOLCANIC_ROCK.base().get()));
                    for (int j = 0; j < h; j++) {
                        this.set(x + cols[i][0], y + j, z + cols[i][1], pillar(j == h - 1 ? Blocks.POLISHED_BASALT : Blocks.BASALT, Direction.Axis.Y));
                    }
                }
            }
            case ICE -> {
                // Eisnadeln
                int h = 2 + (int) (this.b.noise(x, 3, z) * 4);
                for (int i = 0; i < h; i++) {
                    this.set(x, y + i, z, s(Blocks.PACKED_ICE));
                }
                this.set(x, y + h, z, s(Blocks.BLUE_ICE));
                this.set(x + 1, y, z, s(Blocks.PACKED_ICE));
                this.set(x, y, z - 1, s(Blocks.SNOW).setValue(SnowLayerBlock.LAYERS, 3));
            }
            case LIGHTNING -> {
                // Blitzfänger: Steinpfeiler mit Kupferhaube und Ableiter, daneben Fulgurit
                for (int i = 0; i < 3; i++) {
                    this.set(x, y + i, z, s(ModBlocks.STORMSTONE.base().get()));
                }
                this.set(x, y + 3, z, s(TowerPalette.copper(Blocks.CUT_COPPER, net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED)));
                this.set(x, y + 4, z, s(TowerPalette.copper(Blocks.LIGHTNING_ROD, net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED)));
                this.set(x + 1, y, z, pillar(ModBlocks.FULGURITE.get(), Direction.Axis.Y));
            }
            case NATURE -> {
                // Blumenbeete und Azaleenbüsche
                if (k % 3 == 0) {
                    this.set(x, y, z, s(Blocks.FLOWERING_AZALEA));
                } else {
                    Block[] flowers = {ModBlocks.LAVENDER.get(), ModBlocks.GARDEN_ROSE.get(), ModBlocks.GROVE_BELL.get(), ModBlocks.LIFE_BLOSSOM.get()};
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (this.b.chance(x + dx, 44, z + dz, 0.6f) && this.b.get(x + dx, y, z + dz).isAir()
                                    && this.b.get(x + dx, y - 1, z + dz).is(Blocks.GRASS_BLOCK)) {
                                this.set(x + dx, y, z + dz, s(flowers[Math.floorMod(x + dx * 7 + z + dz, 4)]));
                            }
                        }
                    }
                }
            }
            case EARTH -> {
                // Felsblock mit Bernstein und Tropfsteinen
                this.set(x, y, z, s(Blocks.TUFF));
                this.set(x + 1, y, z, s(Blocks.COBBLED_DEEPSLATE));
                this.set(x, y, z + 1, s(Blocks.TUFF));
                this.set(x, y + 1, z, s(k % 2 == 0 ? ModBlocks.AMBER_CRYSTAL.get() : Blocks.TUFF));
                if (k % 2 == 0) {
                    this.set(x, y + 2, z, s(ModBlocks.AMBER_CLUSTER.get()));
                } else {
                    this.stalagmite(x + 1, y + 1, z, 2);
                }
            }
            case WATER -> {
                // kleine Becken mit Seerosen und Korallen
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        boolean rim = Math.abs(dx) == 1 || Math.abs(dz) == 1;
                        this.set(x + dx, y - 1, z + dz, rim ? s(Blocks.PRISMARINE) : s(Blocks.WATER));
                        if (!rim) {
                            this.set(x + dx, y - 2, z + dz, s(Blocks.SAND));
                            this.set(x + dx, y, z + dz, s(Blocks.LILY_PAD));
                        }
                    }
                }
            }
        }
    }

    // =====================================================================================
    // Abfragen für den Erzmagier und die Prüfungen
    // =====================================================================================

    /** Wo der Erzmagier meditiert (Block über der Mitte des Sanktums). */
    static BlockPos home(VillageBuilder b) {
        return b.world(0, SANCTUM + 1, 0);
    }

    /** Wo die Treppe im Sanktum ankommt (zu dieser Stelle blickt der meditierende Magier). */
    static BlockPos arrival(VillageBuilder b) {
        Flight f = FLIGHTS[3];
        return b.world(f.x(6), SANCTUM + 1, f.z(6));
    }

    /** Truhen: Halle, Bibliothek, Laboratorium, Kammer, Sanktum (Turmkoordinaten). */
    public static List<int[]> chestSpots() {
        return List.of(new int[] {4, FLOOR[0] + 1, 2}, new int[] {-3, FLOOR[1] + 1, -3}, new int[] {1, FLOOR[2] + 1, 4},
                new int[] {2, FLOOR[3] + 1, 1}, new int[] {7, SANCTUM + 1, 0});
    }
}
