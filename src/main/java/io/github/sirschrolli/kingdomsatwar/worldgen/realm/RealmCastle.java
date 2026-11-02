package io.github.sirschrolli.kingdomsatwar.worldgen.realm;

import io.github.sirschrolli.kingdomsatwar.war.CastleStyle;
import io.github.sirschrolli.kingdomsatwar.war.Realm;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Die Stammburg eines fremden Reichs. Eigene Koordinaten: Mitte des Burghofs bei (0, 0, 0), y = 0 ist die oberste
 * Schicht des Hofs (man steht auf y = 1). Das Tor liegt im Süden (+z), der Bergfried im Norden (−z).
 *
 * <ul>
 *     <li>Ringmauer (61 × 61) mit Böschung, Wehrgang und Zinnen; vier runde Ecktürme mit überdachter Wehrplatte und
 *     spitzem Kegeldach, drei viereckige Flankentürme mit Zeltdach</li>
 *     <li>Torhaus aus zwei Türmen mit Torstube, Spitzbogen und herabgelassenem Fallgitter, davor die Banner des Reichs</li>
 *     <li>Bergfried: Halle mit Tafel und Küche, Freitreppe zum <b>Thronsaal</b> (Säulen, Teppich, Fenster in den Farben
 *     des Reichs, Thron auf einem Podest unter dem dreibahnigen Reichsbanner), darüber die Gemächer des Herrschers mit
 *     der Schatztruhe, auf der Wehrplatte vier Ecktürmchen und ein Wachturm mit Fahne</li>
 *     <li>im Hof Kaserne mit Waffenkammer, Stall, Schmiede, Kapelle mit Glockenturm, Vorratshaus, Brunnen und
 *     Übungsplatz</li>
 * </ul>
 * Die Bauweise (Stein, Dach, Holz, Farben) kommt aus dem Reich ({@link CastleStyle}). Das Gelände ebnet
 * {@link RealmCastlePiece} vorher ein.
 */
public final class RealmCastle {
    /** Halbe Kantenlänge des ebenen Bauplatzes (vom Mittelpunkt aus). */
    public static final int PLATFORM = 38;
    /** Breite des Übergangs vom Bauplatz ins umliegende Gelände. */
    public static final int BLEND = 12;
    /** Wie weit die Burg samt Übergang vom Mittelpunkt reicht. */
    public static final int REACH = PLATFORM + BLEND + 1;
    /** Wie hoch die Burg über den Hof reicht (Fahnen eingeschlossen). */
    public static final int HEIGHT = 42;

    static final int WALL = 30;
    static final int WALK = 11;
    /** Die vier runden Ecktürme. */
    private static final int[][] CORNERS = {{WALL, WALL}, {-WALL, WALL}, {-WALL, -WALL}, {WALL, -WALL}};
    private static final double TOWER_R = 6.4;
    private static final double TOWER_SHELL = 4.9;
    private static final int TOWER_TOP = 17;
    /** Torhaus: Türme von z = 27 bis 34, Wehrplatte der Türme auf y = 19. */
    private static final int GZ0 = WALL - 3;
    private static final int GZ1 = WALL + 4;
    private static final int GATE_TOP = 19;
    /** Bergfried: x −10..10, z −24..−10; Böden auf 6 (Thronsaal), 14 (Gemächer) und 20 (Wehrplatte). */
    private static final int KX = 10;
    private static final int KN = -24;
    private static final int KS = -10;
    private static final int HALL = 6;
    private static final int UPPER = 14;
    private static final int ROOF = 20;

    /** Mitte des Reichsbanners hinter dem Thron (eigene Koordinaten): hier wird die Burg erobert. */
    public static final BlockPos STANDARD = new BlockPos(0, 10, -23);
    /** Wo der Thron steht (eigene Koordinaten). */
    public static final BlockPos THRONE = new BlockPos(0, HALL + 2, -23);

    private RealmCastle() {}

    /** Ein Wächterposten (für die Besatzung): Rolle und Ort in eigenen Koordinaten (y = Standhöhe). */
    public record Post(String role, int x, int y, int z) {}

    /** Wo die Besatzung steht. */
    public static List<Post> posts() {
        List<Post> posts = new ArrayList<>();
        // Bogenschützen auf dem Wehrgang (je Mauer zwei)
        for (int i = -1; i <= 1; i += 2) {
            posts.add(new Post("archer", i * 18, WALK + 1, WALL - 1));
            posts.add(new Post("archer", i * 18, WALK + 1, -(WALL - 1)));
            posts.add(new Post("archer", WALL - 1, WALK + 1, i * 14));
            posts.add(new Post("archer", -(WALL - 1), WALK + 1, i * 14));
        }
        // Armbrustschützen auf den Wehrplatten der Ecktürme (zur Feldseite hin) und auf den Tortürmen
        for (int[] c : CORNERS) {
            posts.add(new Post("crossbowman", c[0] + Integer.signum(c[0]) * 2, TOWER_TOP + 1, c[1] + Integer.signum(c[1]) * 2));
        }
        posts.add(new Post("crossbowman", -6, GATE_TOP + 1, GZ0 + 3));
        posts.add(new Post("crossbowman", 6, GATE_TOP + 1, GZ0 + 3));
        // Wache hinter dem Tor und der Hauptmann
        posts.add(new Post("footman", -4, 1, 24));
        posts.add(new Post("footman", 4, 1, 24));
        posts.add(new Post("captain", 0, 1, 21));
        // Fußknechte im Hof und vor dem Bergfried
        posts.add(new Post("footman", -6, 1, 11));
        posts.add(new Post("footman", 8, 1, 9));
        posts.add(new Post("footman", 13, 1, -8));
        posts.add(new Post("footman", -13, 1, -8));
        posts.add(new Post("footman", 21, 1, 15));
        posts.add(new Post("footman", -21, 1, 16));
        // Ritter im Thronsaal und der Herrscher vor seinem Thron
        posts.add(new Post("knight", -3, HALL + 1, -14));
        posts.add(new Post("knight", 3, HALL + 1, -14));
        posts.add(new Post("knight", -3, HALL + 1, -19));
        posts.add(new Post("knight", 3, HALL + 1, -19));
        posts.add(new Post("lord", 0, HALL + 1, -21));
        return posts;
    }

    static void build(VillageBuilder b, Realm realm, ResourceKey<LootTable> treasury, ResourceKey<LootTable> armory,
            ResourceKey<LootTable> stores) {
        new Builder(b, realm, treasury, armory, stores).build();
    }

    private static final class Builder {
        private final VillageBuilder b;
        private final Realm realm;
        private final CastleStyle s;
        private final ResourceKey<LootTable> treasury;
        private final ResourceKey<LootTable> armory;
        private final ResourceKey<LootTable> stores;

        Builder(VillageBuilder b, Realm realm, ResourceKey<LootTable> treasury, ResourceKey<LootTable> armory, ResourceKey<LootTable> stores) {
            this.b = b;
            this.realm = realm;
            this.s = realm.style();
            this.treasury = treasury;
            this.armory = armory;
            this.stores = stores;
        }

        void build() {
            this.ground();
            this.curtain();
            for (int[] c : CORNERS) {
                this.roundTower(c[0], c[1]);
            }
            this.flankTower(WALL, 0, Direction.EAST);
            this.flankTower(-WALL, 0, Direction.WEST);
            this.flankTower(0, -WALL, Direction.NORTH);
            this.gatehouse();
            this.walkStairs();
            this.keep();
            this.barracks();
            this.stable();
            this.smithy();
            this.chapel();
            this.storehouse();
            this.well(-11, 9);
            this.yard();
            this.lamps();
        }

        // =================================================================================
        // Grund
        // =================================================================================

        /** Boden des Hofs (Wiese mit Wegen) und der Weg vom Tor hinaus. */
        private void ground() {
            for (int x = -WALL + 3; x <= WALL - 3; x++) {
                for (int z = -WALL + 3; z <= WALL - 3; z++) {
                    this.b.set(x, 0, z, this.courtyard(x, z));
                }
            }
            for (int z = GZ1 + 1; z <= PLATFORM; z++) {
                for (int x = -2; x <= 2; x++) {
                    this.b.set(x, 0, z, this.b.chance(x, 0, z, 0.2f) ? Blocks.COARSE_DIRT : this.s.path());
                }
            }
        }

        /** Boden im Hof: Wege vom Tor zum Bergfried und quer zu Kaserne und Stall, sonst Gras mit ausgetretenen Stellen. */
        private Block courtyard(int x, int z) {
            boolean mainRoad = Math.abs(x) <= 2 && z >= -3;
            boolean crossRoad = Math.abs(z - 4) <= 1 && Math.abs(x) <= 17;
            if (mainRoad || crossRoad) {
                return this.b.chance(x, 0, z, 0.12f) ? Blocks.COARSE_DIRT : this.s.path();
            }
            if (this.b.noise(x, 1, z) < 0.08f) {
                return Blocks.COARSE_DIRT;
            }
            return Blocks.GRASS_BLOCK;
        }

        // =================================================================================
        // Mauerwerk
        // =================================================================================

        /** Mauerstein mit Abwandlungen: unten öfter bemoost, sonst hier und da verwittert. */
        private BlockState stone(int x, int y, int z) {
            float n = this.b.noise(x, y, z);
            if (y < 4 && n < 0.14f) {
                return this.s.wallMossy().defaultBlockState();
            }
            if (n > 0.88f) {
                return this.s.wallWorn().defaultBlockState();
            }
            return this.s.wall().defaultBlockState();
        }

        private void wallBlock(int x, int y, int z) {
            this.b.set(x, y, z, this.stone(x, y, z));
        }

        /** Brüstung: immer ein Stein, auf jeder zweiten Stelle ein zweiter (Zinne). */
        private void merlon(int x, int y, int z, boolean high) {
            this.wallBlock(x, y, z);
            if (high) {
                this.wallBlock(x, y + 1, z);
            }
        }

        private BlockState trimStairs(Direction facing, Half half) {
            return VillageBuilder.stairs(this.s.trimStairs(), facing, half);
        }

        private static BlockState chain() {
            return Blocks.IRON_CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, Direction.Axis.Y);
        }

        // =================================================================================
        // Ringmauer
        // =================================================================================

        private void curtain() {
            for (int i = -WALL; i <= WALL; i++) {
                for (int d = 0; d <= 2; d++) {
                    this.curtainColumn(i, WALL - d, d, Direction.SOUTH);
                    this.curtainColumn(i, -(WALL - d), d, Direction.NORTH);
                    this.curtainColumn(WALL - d, i, d, Direction.EAST);
                    this.curtainColumn(-(WALL - d), i, d, Direction.WEST);
                }
                // Kragsteine unter der Brüstung außen
                this.b.set(i, WALK, WALL + 1, this.trimStairs(Direction.NORTH, Half.TOP));
                this.b.set(i, WALK, -WALL - 1, this.trimStairs(Direction.SOUTH, Half.TOP));
                this.b.set(WALL + 1, WALK, i, this.trimStairs(Direction.WEST, Half.TOP));
                this.b.set(-WALL - 1, WALK, i, this.trimStairs(Direction.EAST, Half.TOP));
                // Böschung außen am Mauerfuß
                this.b.set(i, 1, WALL + 1, VillageBuilder.stairs(this.s.baseStairs(), Direction.NORTH, Half.BOTTOM));
                this.b.set(i, 1, -WALL - 1, VillageBuilder.stairs(this.s.baseStairs(), Direction.SOUTH, Half.BOTTOM));
                this.b.set(WALL + 1, 1, i, VillageBuilder.stairs(this.s.baseStairs(), Direction.WEST, Half.BOTTOM));
                this.b.set(-WALL - 1, 1, i, VillageBuilder.stairs(this.s.baseStairs(), Direction.EAST, Half.BOTTOM));
            }
        }

        /**
         * Eine Säule der Ringmauer. {@code depth} 0 ist die Außenseite (Brüstung mit Zinnen), 1 und 2 der Wehrgang;
         * {@code out} zeigt nach außen.
         */
        private void curtainColumn(int x, int z, int depth, Direction out) {
            this.b.set(x, 0, z, this.s.base());
            for (int y = 1; y <= WALK; y++) {
                this.wallBlock(x, y, z);
            }
            int along = out.getAxis() == Direction.Axis.Z ? x : z;
            if (depth == 0) {
                this.merlon(x, WALK + 1, z, Math.floorMod(along, 2) == 0);
                // Schießscharten im Mauerwerk
                if (Math.floorMod(along, 6) == 3) {
                    this.b.set(x, 6, z, Blocks.AIR);
                    this.b.set(x, 7, z, Blocks.AIR);
                }
            } else if (depth == 2) {
                // Gesims an der Hofseite und darunter Laternen
                int ix = x - out.getStepX();
                int iz = z - out.getStepZ();
                this.b.set(ix, WALK, iz, this.trimStairs(out, Half.TOP));
                if (Math.floorMod(along, 10) == 5 && Math.abs(along) < WALL - 6) {
                    this.b.hangingLantern(ix, WALK - 1, iz, Blocks.LANTERN);
                }
            }
        }

        // =================================================================================
        // Ecktürme
        // =================================================================================

        /** Runder Eckturm: drei Geschosse, Wehrplatte mit Zinnen, darüber auf Pfosten ein spitzes Kegeldach mit Fahne. */
        private void roundTower(int cx, int cz) {
            int sx = -Integer.signum(cx);
            int sz = -Integer.signum(cz);
            for (int x = cx - 7; x <= cx + 7; x++) {
                for (int z = cz - 7; z <= cz + 7; z++) {
                    double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                    if (d > TOWER_R) {
                        continue;
                    }
                    boolean shell = d > TOWER_SHELL;
                    this.b.set(x, 0, z, shell ? this.s.base() : this.s.floor());
                    for (int y = 1; y <= TOWER_TOP; y++) {
                        if (shell) {
                            this.wallBlock(x, y, z);
                        } else {
                            this.b.set(x, y, z, y == 6 || y == WALK || y == TOWER_TOP ? this.s.planks() : Blocks.AIR);
                        }
                    }
                    if (shell) {
                        // Brüstung und Zinnen der Wehrplatte
                        this.merlon(x, TOWER_TOP + 1, z, Math.floorMod(x + z, 2) == 0);
                    } else {
                        this.b.air(x, TOWER_TOP + 1, z, x, TOWER_TOP + 4, z);
                    }
                    if (shell) {
                        this.b.air(x, TOWER_TOP + 3, z, x, TOWER_TOP + 4, z);
                    }
                }
            }
            // Fenster: unten nur zur Feldseite, ganz oben in alle Richtungen
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                boolean outward = dir.getStepX() != 0 && dir.getStepX() == -sx || dir.getStepZ() != 0 && dir.getStepZ() == -sz;
                for (int y : new int[] {3, 8, 14}) {
                    if (!outward && y < 14) {
                        continue;
                    }
                    for (int k = 5; k <= 6; k++) {
                        this.b.air(cx + dir.getStepX() * k, y, cz + dir.getStepZ() * k, cx + dir.getStepX() * k, y + 1, cz + dir.getStepZ() * k);
                    }
                }
            }
            // Durchgänge zum Wehrgang (Mauer entlang x und Mauer entlang z)
            for (int k = 5; k <= 6; k++) {
                for (int w = 1; w <= 2; w++) {
                    this.b.air(cx + sx * k, WALK + 1, cz + sz * w, cx + sx * k, WALK + 2, cz + sz * w);
                    this.b.air(cx + sx * w, WALK + 1, cz + sz * k, cx + sx * w, WALK + 2, cz + sz * k);
                }
            }
            // Tür vom Hof und Leiter durch alle Geschosse bis auf die Wehrplatte
            this.b.door(cx + sx * 3, 1, cz + sz * 5, this.s.door(), sz > 0 ? Direction.NORTH : Direction.SOUTH);
            this.b.air(cx + sx * 3, 1, cz + sz * 4, cx + sx * 3, 2, cz + sz * 4);
            this.b.ladder(cx - sx * 2, 1, TOWER_TOP, cz - sz * 4, sz > 0 ? Direction.SOUTH : Direction.NORTH);
            // Einrichtung: Fässer, Licht
            this.b.set(cx + sx * 2, 1, cz - sz * 2, Blocks.BARREL);
            this.b.set(cx + sx * 3, 1, cz - sz * 1, Blocks.BARREL);
            this.b.set(cx + sx * 2, WALK + 1, cz - sz * 3, Blocks.BARREL);
            this.b.hangingLantern(cx, 5, cz, Blocks.LANTERN);
            this.b.hangingLantern(cx, 10, cz, Blocks.LANTERN);
            this.b.hangingLantern(cx, 16, cz, Blocks.LANTERN);
            // Pfosten tragen das Dach
            int[][] postsAt = {{6, 0}, {-6, 0}, {0, 6}, {0, -6}, {4, 4}, {4, -4}, {-4, 4}, {-4, -4}};
            for (int[] p : postsAt) {
                for (int y = TOWER_TOP + 2; y <= TOWER_TOP + 4; y++) {
                    this.b.set(cx + p[0], y, cz + p[1], this.s.fence());
                }
            }
            int peak = this.cone(cx, cz, TOWER_TOP + 5, 7.2);
            this.b.set(cx, TOWER_TOP + 5, cz, chain());
            this.b.hangingLantern(cx, TOWER_TOP + 4, cz, Blocks.LANTERN);
            this.flag(cx, peak + 1, cz, 0);
        }

        /**
         * Spitzes Kegeldach aus Dachtreppen, Ring um Ring nach innen; die Stufen darunter werden mit vollen Blöcken
         * geschlossen. Gibt die Höhe der Spitze zurück.
         */
        private int cone(int cx, int cz, int y0, double r0) {
            int layer = 0;
            for (double r = r0; r > 0.4; r -= 0.8) {
                int y = y0 + layer;
                int reach = (int) Math.ceil(r);
                for (int x = cx - reach; x <= cx + reach; x++) {
                    for (int z = cz - reach; z <= cz + reach; z++) {
                        double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                        if (d > r || d <= r - 1.25 || d < 0.5) {
                            continue;
                        }
                        Direction away = facingAway(x - cx, z - cz);
                        this.b.set(x, y, z, VillageBuilder.stairs(this.s.roofStairs(), away.getOpposite(), Half.BOTTOM));
                        if (layer > 0 && this.b.get(x, y - 1, z).isAir()) {
                            this.b.set(x, y - 1, z, this.s.roof());
                        }
                    }
                }
                layer++;
            }
            int peak = y0 + layer - 1;
            this.b.set(cx, peak, cz, this.s.roof());
            this.b.set(cx, peak - 1, cz, this.s.roof());
            return peak;
        }

        /** In welche Richtung eine Stelle (relativ zur Mitte) nach außen zeigt. */
        private static Direction facingAway(int dx, int dz) {
            if (Math.abs(dx) >= Math.abs(dz)) {
                return dx >= 0 ? Direction.EAST : Direction.WEST;
            }
            return dz >= 0 ? Direction.SOUTH : Direction.NORTH;
        }

        /** Fahnenstange mit dem Banner des Reichs obendrauf. */
        private void flag(int x, int y, int z, int rotation) {
            for (int k = 0; k < 3; k++) {
                this.b.set(x, y + k, z, this.s.fence());
            }
            this.standingBanner(x, y + 3, z, rotation);
        }

        // =================================================================================
        // Flankentürme
        // =================================================================================

        /** Lage einer Stelle eines Flankenturms: {@code a} quer zur Mauer, {@code d} nach außen. */
        private static int[] flank(int cx, int cz, Direction out, int a, int d) {
            return switch (out) {
                case EAST -> new int[] {cx + d, cz + a};
                case WEST -> new int[] {cx - d, cz + a};
                case NORTH -> new int[] {cx + a, cz - d};
                default -> new int[] {cx + a, cz + d};
            };
        }

        /** Viereckiger Flankenturm mitten in der Mauer (springt nach außen vor), mit Zeltdach. */
        private void flankTower(int cx, int cz, Direction out) {
            int top = 15;
            for (int a = -3; a <= 3; a++) {
                for (int d = -3; d <= 3; d++) {
                    int[] p = flank(cx, cz, out, a, d);
                    boolean shell = Math.abs(a) == 3 || Math.abs(d) == 3;
                    boolean corner = Math.abs(a) == 3 && Math.abs(d) == 3;
                    this.b.set(p[0], 0, p[1], shell ? this.s.base() : this.s.floor());
                    for (int y = 1; y <= top; y++) {
                        if (shell) {
                            this.b.set(p[0], y, p[1], corner ? this.s.trim().defaultBlockState() : this.stone(p[0], y, p[1]));
                        } else {
                            this.b.set(p[0], y, p[1], y == 6 || y == WALK ? this.s.planks() : Blocks.AIR);
                        }
                    }
                }
            }
            // Durchgänge zum Wehrgang
            for (int a = -3; a <= 3; a += 6) {
                for (int d = -2; d <= -1; d++) {
                    int[] p = flank(cx, cz, out, a, d);
                    this.b.air(p[0], WALK + 1, p[1], p[0], WALK + 2, p[1]);
                }
            }
            // Tür vom Hof, Leiter, Schießscharten, Fenster
            int[] door = flank(cx, cz, out, 0, -3);
            this.b.door(door[0], 1, door[1], this.s.door(), out);
            int[] ladder = flank(cx, cz, out, 2, 2);
            this.b.ladder(ladder[0], 1, WALK, ladder[1], out.getOpposite());
            for (int a = -1; a <= 1; a += 2) {
                int[] slit = flank(cx, cz, out, a, 3);
                for (int y : new int[] {3, 8}) {
                    this.b.air(slit[0], y, slit[1], slit[0], y + 1, slit[1]);
                }
            }
            int[] window = flank(cx, cz, out, 0, 3);
            this.b.set(window[0], WALK + 2, window[1], this.s.window());
            int[] inner = flank(cx, cz, out, 0, 0);
            this.b.hangingLantern(inner[0], 5, inner[1], Blocks.LANTERN);
            this.b.hangingLantern(inner[0], top, inner[1], Blocks.LANTERN);
            // Zeltdach mit einem Block Überstand
            int[] lo = flank(cx, cz, out, -4, -4);
            int[] hi = flank(cx, cz, out, 4, 4);
            this.b.hipRoof(Math.min(lo[0], hi[0]), Math.min(lo[1], hi[1]), Math.max(lo[0], hi[0]), Math.max(lo[1], hi[1]), top + 1,
                    this.s.roofStairs(), this.s.roof().defaultBlockState());
            // Banner außen unter dem Dach
            int[] banner = flank(cx, cz, out, 0, 4);
            this.wallBanner(banner[0], top - 1, banner[1], out);
        }

        // =================================================================================
        // Torhaus
        // =================================================================================

        private void gatehouse() {
            // die beiden Tortürme
            for (int side = -1; side <= 1; side += 2) {
                int xa = side * 4;
                int xb = side * 9;
                for (int x = Math.min(xa, xb); x <= Math.max(xa, xb); x++) {
                    for (int z = GZ0; z <= GZ1; z++) {
                        boolean shell = x == xa || x == xb || z == GZ0 || z == GZ1;
                        boolean corner = (x == xa || x == xb) && (z == GZ0 || z == GZ1);
                        this.b.set(x, 0, z, shell ? this.s.base() : this.s.floor());
                        for (int y = 1; y <= GATE_TOP; y++) {
                            if (shell) {
                                this.b.set(x, y, z, corner ? this.s.trim().defaultBlockState() : this.stone(x, y, z));
                            } else {
                                this.b.set(x, y, z, y == 6 || y == WALK || y == 16 || y == GATE_TOP ? this.s.planks() : Blocks.AIR);
                            }
                        }
                        if (shell) {
                            this.merlon(x, GATE_TOP + 1, z, Math.floorMod(x + z, 2) == 0);
                        }
                    }
                }
                // Schießscharten nach vorn
                for (int y : new int[] {3, 8}) {
                    this.b.air(side * 6, y, GZ1, side * 7, y + 1, GZ1);
                }
                // Kragsteine unter der Wehrplatte (vorn und an der Außenseite)
                for (int x = Math.min(xa, xb); x <= Math.max(xa, xb); x++) {
                    this.b.set(x, GATE_TOP, GZ1 + 1, this.trimStairs(Direction.NORTH, Half.TOP));
                }
                for (int z = WALL + 1; z <= GZ1; z++) {
                    this.b.set(xb + side, GATE_TOP, z, this.trimStairs(side > 0 ? Direction.WEST : Direction.EAST, Half.TOP));
                }
                // Tür vom Hof, Leiter bis auf die Wehrplatte
                this.b.door(side * 6, 1, GZ0, this.s.door(), Direction.SOUTH);
                this.b.ladder(side * 8, 1, GATE_TOP, GZ0 + 2, side > 0 ? Direction.WEST : Direction.EAST);
                // Durchgänge zum Wehrgang, in die Torstube und auf ihr Dach
                this.b.air(xb, WALK + 1, WALL - 2, xb, WALK + 2, WALL - 1);
                this.b.air(xa, WALK + 1, WALL, xa - side, WALK + 2, WALL);
                this.b.air(xa, 17, WALL, xa, 18, WALL);
                this.b.hangingLantern(side * 6, 5, GZ0 + 4, Blocks.LANTERN);
                this.b.hangingLantern(side * 6, 15, GZ0 + 4, Blocks.LANTERN);
                // Banner vorn an den Türmen
                this.wallBanner(side * 6, 15, GZ1 + 1, Direction.SOUTH);
                this.wallBanner(side * 7, 15, GZ1 + 1, Direction.SOUTH);
            }
            // Torbau zwischen den Türmen: Durchfahrt (5 × 6), Torstube darüber, Dach mit Brüstung
            for (int x = -3; x <= 3; x++) {
                for (int z = GZ0; z <= GZ1; z++) {
                    this.b.set(x, 0, z, Math.abs(x) <= 2 ? this.s.path().defaultBlockState() : this.s.base().defaultBlockState());
                    boolean front = z == GZ0 || z == GZ1;
                    for (int y = 1; y <= 15; y++) {
                        boolean passage = Math.abs(x) <= 2 && y <= 6;
                        boolean room = Math.abs(x) <= 2 && y >= 12 && !front;
                        if (passage || room) {
                            this.b.set(x, y, z, Blocks.AIR);
                        } else if (y == WALK && Math.abs(x) <= 2 && !front) {
                            this.b.set(x, y, z, this.s.planks());
                        } else {
                            this.wallBlock(x, y, z);
                        }
                    }
                    if (front) {
                        this.wallBlock(x, 16, z);
                        this.merlon(x, 17, z, Math.floorMod(x, 2) == 1);
                    } else {
                        this.b.set(x, 16, z, this.s.planks());
                    }
                }
            }
            // Fenster der Torstube
            for (int x = -1; x <= 1; x += 2) {
                this.b.set(x, 13, GZ1, this.s.window());
                this.b.set(x, 13, GZ0, this.s.window());
            }
            this.b.hangingLantern(0, 15, WALL, Blocks.LANTERN);
            // Spitzbogen vorn und hinten
            for (int z : new int[] {GZ0, GZ1}) {
                for (int y = 1; y <= 7; y++) {
                    this.b.set(-3, y, z, this.s.trim());
                    this.b.set(3, y, z, this.s.trim());
                }
                for (int x = -2; x <= 2; x++) {
                    this.b.set(x, 7, z, this.s.trim());
                }
                this.b.set(-2, 6, z, this.trimStairs(Direction.WEST, Half.TOP));
                this.b.set(2, 6, z, this.trimStairs(Direction.EAST, Half.TOP));
            }
            // Fallgitter (herabgelassen)
            for (int x = -2; x <= 2; x++) {
                for (int y = 1; y <= (Math.abs(x) == 2 ? 5 : 6); y++) {
                    this.b.set(x, y, GZ1 - 1, Blocks.IRON_BARS);
                }
            }
            // Fackeln an der Hofseite der Durchfahrt und das große Banner über dem Tor
            for (int x = -3; x <= 3; x += 6) {
                this.b.set(x, 4, GZ0 - 1, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH));
            }
            this.wallBanner(0, 11, GZ1 + 1, Direction.SOUTH);
        }

        // =================================================================================
        // Treppen auf den Wehrgang
        // =================================================================================

        /** An der Ost- und Westmauer je eine gerade Treppe vom Hof hinauf (nach Norden steigend). */
        private void walkStairs() {
            for (int side = -1; side <= 1; side += 2) {
                int x = side * (WALL - 3);
                for (int k = 0; k < WALK; k++) {
                    int z = 22 - k;
                    int y = k + 1;
                    this.b.set(x, y, z, VillageBuilder.stairs(this.s.wallStairs(), Direction.NORTH, Half.BOTTOM));
                    for (int f = 1; f < y; f++) {
                        this.wallBlock(x, f, z);
                    }
                    this.b.air(x, y + 1, z, x, y + 3, z);
                }
            }
        }

        // =================================================================================
        // Bergfried mit Thronsaal
        // =================================================================================

        private void keep() {
            for (int x = -KX; x <= KX; x++) {
                for (int z = KN; z <= KS; z++) {
                    boolean shell = Math.abs(x) == KX || z == KN || z == KS;
                    boolean corner = Math.abs(x) == KX && (z == KN || z == KS);
                    boolean pilaster = shell && (Math.abs(x) == KX ? Math.floorMod(z - KN, 7) == 0 : Math.floorMod(x + KX, 5) == 0);
                    this.b.set(x, 0, z, shell ? this.s.base().defaultBlockState() : this.s.floor().defaultBlockState());
                    for (int y = 1; y <= ROOF; y++) {
                        if (shell) {
                            this.b.set(x, y, z, corner || pilaster ? this.s.trim().defaultBlockState() : this.stone(x, y, z));
                        } else {
                            this.b.set(x, y, z, y == HALL || y == UPPER ? this.s.planks().defaultBlockState()
                                    : y == ROOF ? this.s.floor().defaultBlockState() : Blocks.AIR.defaultBlockState());
                        }
                    }
                    if (shell) {
                        this.merlon(x, ROOF + 1, z, Math.floorMod(x + z, 2) == 0);
                    }
                }
            }
            // Kragsteine unter der Brüstung der Wehrplatte
            for (int x = -KX; x <= KX; x++) {
                this.b.set(x, ROOF, KS + 1, this.trimStairs(Direction.NORTH, Half.TOP));
                this.b.set(x, ROOF, KN - 1, this.trimStairs(Direction.SOUTH, Half.TOP));
            }
            for (int z = KN; z <= KS; z++) {
                this.b.set(KX + 1, ROOF, z, this.trimStairs(Direction.WEST, Half.TOP));
                this.b.set(-KX - 1, ROOF, z, this.trimStairs(Direction.EAST, Half.TOP));
            }
            // Gesims zwischen Thronsaal und Gemächern
            for (int x = -KX - 1; x <= KX + 1; x++) {
                this.b.set(x, UPPER, KS + 1, this.trimStairs(Direction.NORTH, Half.TOP));
                this.b.set(x, UPPER, KN - 1, this.trimStairs(Direction.SOUTH, Half.TOP));
            }
            for (int z = KN; z <= KS; z++) {
                this.b.set(KX + 1, UPPER, z, this.trimStairs(Direction.WEST, Half.TOP));
                this.b.set(-KX - 1, UPPER, z, this.trimStairs(Direction.EAST, Half.TOP));
            }
            // kleine Fenster im Erdgeschoss
            for (int side = -1; side <= 1; side += 2) {
                for (int z : new int[] {-21, -13}) {
                    this.b.set(side * KX, 2, z, this.s.window());
                    this.b.set(side * KX, 3, z, this.s.window());
                }
            }
            this.keepGround();
            this.grandStair();
            this.throneHall();
            this.chambers();
            this.keepRoof();
        }

        /** Erdgeschoss: Halle mit Tafel, Küche und Vorrat; zwei Türen zum Hof neben der Freitreppe. */
        private void keepGround() {
            for (int side = -1; side <= 1; side += 2) {
                this.b.door(side * 5, 1, KS, this.s.door(), Direction.NORTH);
            }
            // lange Tafel mit Bänken
            for (int z = -21; z <= -14; z++) {
                this.b.set(0, 1, z, this.s.woodSlab().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
                this.b.set(-1, 1, z, VillageBuilder.stairs(this.s.woodStairs(), Direction.WEST, Half.BOTTOM));
                this.b.set(1, 1, z, VillageBuilder.stairs(this.s.woodStairs(), Direction.EAST, Half.BOTTOM));
            }
            this.b.set(0, 2, -18, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3));
            // Küche: Räucherofen, Ofen, Kessel, Fässer und die Vorratstruhe
            this.b.set(-8, 1, -23, Blocks.SMOKER.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.SOUTH));
            this.b.set(-7, 1, -23, Blocks.FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.SOUTH));
            this.b.set(-6, 1, -23, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
            this.b.set(-9, 1, -23, Blocks.BARREL);
            this.b.set(-9, 1, -20, Blocks.BARREL);
            this.b.set(-9, 2, -20, Blocks.BARREL);
            this.b.set(-9, 1, -19, Blocks.BARREL);
            this.b.chest(-9, 1, -16, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST), this.stores);
            // Treppe hinauf in den Thronsaal (an der Ostwand, nach Norden steigend)
            for (int k = 0; k < 6; k++) {
                int z = -12 - k;
                int y = k + 1;
                this.b.set(8, y, z, VillageBuilder.stairs(this.s.woodStairs(), Direction.NORTH, Half.BOTTOM));
                for (int f = 1; f < y; f++) {
                    this.b.set(8, f, z, this.s.planks());
                }
                this.b.air(8, y + 1, z, 8, y + 3, z);
            }
            this.b.hangingLantern(-4, 5, -17, Blocks.LANTERN);
            this.b.hangingLantern(4, 5, -17, Blocks.LANTERN);
            this.b.hangingLantern(-6, 5, -12, Blocks.LANTERN);
        }

        /** Freitreppe von der Hofseite hinauf zum offenen Portal des Thronsaals. */
        private void grandStair() {
            for (int x = -2; x <= 2; x++) {
                for (int k = 0; k < 6; k++) {
                    int z = -3 - k;
                    int y = k + 1;
                    this.b.set(x, y, z, VillageBuilder.stairs(this.s.wallStairs(), Direction.NORTH, Half.BOTTOM));
                    for (int f = 1; f < y; f++) {
                        this.b.set(x, f, z, this.s.base());
                    }
                }
                // Podest vor dem Portal
                for (int f = 1; f <= HALL; f++) {
                    this.b.set(x, f, KS + 1, f == HALL ? this.s.floor().defaultBlockState() : this.s.base().defaultBlockState());
                }
            }
            // Wangen der Treppe mit Brüstung
            for (int k = 0; k <= 6; k++) {
                int z = -3 - k;
                int top = Math.min(HALL, k + 1);
                for (int side = -1; side <= 1; side += 2) {
                    for (int f = 1; f <= top; f++) {
                        this.b.set(side * 3, f, z, this.s.base());
                    }
                    this.b.set(side * 3, top + 1, z, this.s.wallWall());
                }
            }
            // Portal: drei Blöcke breit, mit Bogen
            this.b.air(-1, HALL + 1, KS, 1, HALL + 3, KS);
            this.b.set(-1, HALL + 3, KS, this.trimStairs(Direction.WEST, Half.TOP));
            this.b.set(1, HALL + 3, KS, this.trimStairs(Direction.EAST, Half.TOP));
            for (int x = -2; x <= 2; x++) {
                this.b.set(x, HALL + 4, KS + 1, this.trimStairs(Direction.NORTH, Half.TOP));
            }
            this.b.set(-2, HALL + 1, KS + 1, Blocks.LANTERN);
            this.b.set(2, HALL + 1, KS + 1, Blocks.LANTERN);
            // Banner beidseits des Portals
            this.wallBanner(-4, HALL + 5, KS + 1, Direction.SOUTH);
            this.wallBanner(4, HALL + 5, KS + 1, Direction.SOUTH);
        }

        /** Der Thronsaal: Säulen, Teppich, Fenster in den Farben des Reichs, Thron und Reichsbanner. */
        private void throneHall() {
            DyeColor field = this.realm.field();
            int floor = HALL;
            // Säulen mit Kapitellen und Kronleuchter
            for (int z = -21; z <= -12; z += 3) {
                for (int side = -1; side <= 1; side += 2) {
                    int x = side * 5;
                    for (int y = floor + 1; y < UPPER; y++) {
                        this.b.set(x, y, z, this.s.trim());
                    }
                    this.b.set(x + 1, UPPER - 1, z, this.trimStairs(Direction.WEST, Half.TOP));
                    this.b.set(x - 1, UPPER - 1, z, this.trimStairs(Direction.EAST, Half.TOP));
                }
                this.b.set(0, UPPER - 1, z, chain());
                this.b.hangingLantern(0, UPPER - 2, z, Blocks.LANTERN);
            }
            // Teppich vom Portal bis zum Podest (Farbe des Reichs, Saum in der Farbe des Wappens)
            for (int z = -21; z <= -11; z++) {
                for (int x = -2; x <= 2; x++) {
                    this.b.set(x, floor + 1, z, Blocks.CARPET.pick(Math.abs(x) == 2 ? this.realm.charge() : field));
                }
            }
            // hohe Fenster in den Seitenwänden
            for (int z = -22; z <= -13; z += 3) {
                for (int side = -1; side <= 1; side += 2) {
                    for (int y = floor + 2; y <= floor + 5; y++) {
                        this.b.set(side * KX, y, z, this.s.window());
                    }
                }
            }
            // Wandbanner zwischen den Fenstern
            for (int z = -20; z <= -14; z += 3) {
                this.wallBanner(KX - 1, floor + 5, z, Direction.WEST);
                this.wallBanner(-KX + 1, floor + 5, z, Direction.EAST);
            }
            // Podest mit Stufe und Thron
            for (int x = -4; x <= 4; x++) {
                this.b.set(x, floor + 1, -22, this.trimStairs(Direction.NORTH, Half.BOTTOM));
                this.b.set(x, floor + 1, -23, this.s.trim());
            }
            this.b.set(THRONE.getX(), THRONE.getY(), THRONE.getZ(), VillageBuilder.stairs(this.s.woodStairs(), Direction.NORTH, Half.BOTTOM));
            for (int side = -1; side <= 1; side += 2) {
                this.b.set(side, THRONE.getY(), THRONE.getZ(), this.trimStairs(side < 0 ? Direction.EAST : Direction.WEST, Half.TOP));
                this.b.set(side * 2, THRONE.getY(), THRONE.getZ(), Blocks.LANTERN);
            }
            // das Reichsbanner hinter dem Thron (drei Bahnen) und zwei Fahnen daneben
            for (int x = -1; x <= 1; x++) {
                this.wallBanner(x, STANDARD.getY() + 1, STANDARD.getZ(), Direction.SOUTH);
            }
            this.wallBanner(-4, floor + 5, -23, Direction.SOUTH);
            this.wallBanner(4, floor + 5, -23, Direction.SOUTH);
            // Geländer um die Treppenöffnung
            for (int z = -16; z <= -13; z++) {
                this.b.set(7, floor + 1, z, this.s.fence());
                this.b.set(9, floor + 1, z, this.s.fence());
            }
            this.b.set(8, floor + 1, -13, this.s.fence());
            // Leiter hinauf in die Gemächer (Nordostecke)
            this.b.ladder(9, floor + 1, UPPER, -23, Direction.SOUTH);
        }

        /** Gemächer des Herrschers: Schlafkammer mit der Schatztruhe, Bibliothek mit Kartentisch; Leiter zur Wehrplatte. */
        private void chambers() {
            int floor = UPPER;
            // Trennwand mit Tür
            for (int z = KN + 1; z < KS; z++) {
                for (int y = floor + 1; y < ROOF; y++) {
                    this.b.set(0, y, z, this.s.planks());
                }
            }
            this.b.door(0, floor + 1, -17, this.s.door(), Direction.WEST);
            // Westen: Schlafkammer des Herrschers
            this.b.bed(-6, floor + 1, -21, Blocks.BED.pick(this.realm.field()), Direction.NORTH);
            this.b.set(-8, floor + 1, -23, Blocks.BOOKSHELF);
            this.b.set(-8, floor + 2, -23, Blocks.BOOKSHELF);
            this.b.set(-9, floor + 1, -23, Blocks.BARREL);
            this.b.chest(-4, floor + 1, -23, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), this.treasury);
            this.b.set(-3, floor + 1, -23, Blocks.BARREL);
            for (int x = -8; x <= -4; x++) {
                for (int z = -19; z <= -15; z++) {
                    this.b.set(x, floor + 1, z, Blocks.CARPET.pick(this.realm.field()));
                }
            }
            this.b.hangingLantern(-5, ROOF - 1, -18, Blocks.LANTERN);
            this.wallBanner(-6, floor + 4, -23, Direction.SOUTH);
            // Osten: Bibliothek und Kartentisch
            for (int z = -22; z <= -19; z++) {
                this.b.set(9, floor + 1, z, Blocks.BOOKSHELF);
                this.b.set(9, floor + 2, z, Blocks.BOOKSHELF);
            }
            this.b.set(5, floor + 1, -18, Blocks.CARTOGRAPHY_TABLE);
            this.b.set(4, floor + 1, -18, Blocks.LECTERN);
            this.b.hangingLantern(5, ROOF - 1, -15, Blocks.LANTERN);
            // Leiter auf die Wehrplatte
            this.b.ladder(-4, floor + 1, ROOF, KS - 1, Direction.NORTH);
            // Fenster
            for (int x : new int[] {-7, 7}) {
                this.b.set(x, floor + 2, KS, this.s.window());
                this.b.set(x, floor + 3, KS, this.s.window());
            }
            for (int side = -1; side <= 1; side += 2) {
                for (int z : new int[] {-20, -14}) {
                    this.b.set(side * KX, floor + 2, z, this.s.window());
                    this.b.set(side * KX, floor + 3, z, this.s.window());
                }
            }
        }

        /** Wehrplatte: vier Ecktürmchen und in der Mitte ein Wachturm mit Fahne. */
        private void keepRoof() {
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int zEdge : new int[] {KN, KS}) {
                    int x0 = sx > 0 ? KX - 2 : -KX;
                    int z0 = zEdge == KN ? KN : KS - 2;
                    for (int x = x0; x <= x0 + 2; x++) {
                        for (int z = z0; z <= z0 + 2; z++) {
                            for (int y = ROOF + 1; y <= ROOF + 3; y++) {
                                boolean edge = x == x0 || x == x0 + 2 || z == z0 || z == z0 + 2;
                                this.b.set(x, y, z, edge ? this.stone(x, y, z) : this.s.trim().defaultBlockState());
                            }
                        }
                    }
                    this.b.hipRoof(x0 - 1, z0 - 1, x0 + 3, z0 + 3, ROOF + 4, this.s.roofStairs(), this.s.roof().defaultBlockState());
                }
            }
            // Wachturm
            int top = ROOF + 7;
            for (int x = -2; x <= 2; x++) {
                for (int z = -22; z <= -18; z++) {
                    boolean shell = Math.abs(x) == 2 || z == -22 || z == -18;
                    boolean corner = Math.abs(x) == 2 && (z == -22 || z == -18);
                    for (int y = ROOF + 1; y <= top; y++) {
                        this.b.set(x, y, z, !shell ? Blocks.AIR.defaultBlockState() : corner ? this.s.trim().defaultBlockState()
                                : this.stone(x, y, z));
                    }
                }
            }
            this.b.door(0, ROOF + 1, -18, this.s.door(), Direction.NORTH);
            for (int y = ROOF + 4; y <= ROOF + 5; y++) {
                this.b.set(-2, y, -20, Blocks.AIR);
                this.b.set(2, y, -20, Blocks.AIR);
                this.b.set(0, y, -22, Blocks.AIR);
            }
            int peak = this.b.hipRoof(-3, -23, 3, -17, top + 1, this.s.roofStairs(), this.s.roof().defaultBlockState());
            this.b.hangingLantern(0, top, -20, Blocks.LANTERN);
            this.flag(0, peak + 1, -20, 0);
        }

        // =================================================================================
        // Gebäude im Hof
        // =================================================================================

        /** Kaserne an der Ostmauer: zwei Geschosse (unten Stein, oben Fachwerk), Betten, Waffenkammer, Satteldach. */
        private void barracks() {
            int x0 = 17;
            int x1 = 25;
            int z0 = -6;
            int z1 = 12;
            int eaves = 9;
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean shell = x == x0 || x == x1 || z == z0 || z == z1;
                    boolean post = shell && ((x == x0 || x == x1) && Math.floorMod(z - z0, 6) == 0 || (z == z0 || z == z1) && (x == x0 || x == x1
                            || x == (x0 + x1) / 2));
                    this.b.set(x, 0, z, this.s.floor());
                    for (int y = 1; y < eaves; y++) {
                        BlockState state;
                        if (!shell) {
                            state = y == 4 ? this.s.planks().defaultBlockState() : Blocks.AIR.defaultBlockState();
                        } else if (y <= 3) {
                            state = this.stone(x, y, z);
                        } else if (post) {
                            state = this.s.log().defaultBlockState();
                        } else if (y == 4) {
                            state = this.s.strippedLog().defaultBlockState().setValue(RotatedPillarBlock.AXIS,
                                    x == x0 || x == x1 ? Direction.Axis.Z : Direction.Axis.X);
                        } else {
                            state = this.s.planks().defaultBlockState();
                        }
                        this.b.set(x, y, z, state);
                    }
                }
            }
            this.b.gableAlongZ(x0, x1, z0, z1, eaves, this.s.roofStairs(), this.s.roof().defaultBlockState(),
                    this.s.roofSlab().defaultBlockState(), this.s.planks().defaultBlockState());
            // Tür und Fenster
            this.b.door(x0, 1, 2, this.s.door(), Direction.EAST);
            for (int z = z0 + 2; z <= z1 - 2; z += 3) {
                if (z != 2) {
                    this.b.set(x0, 2, z, this.s.window());
                }
                this.b.set(x0, 6, z, this.s.window());
                this.b.set(x1, 6, z, this.s.window());
            }
            // Leiter ins Obergeschoss
            this.b.ladder(x0 + 1, 1, 4, z0 + 1, Direction.EAST);
            // Betten (unten drei, oben sechs)
            for (int z : new int[] {-1, 3, 7}) {
                this.b.bed(x1 - 2, 1, z, Blocks.BED.pick(this.realm.field()), Direction.EAST);
            }
            for (int z = z0 + 1; z <= z1 - 2; z += 3) {
                this.b.bed(x1 - 2, 5, z, Blocks.BED.pick(this.realm.field()), Direction.EAST);
            }
            // Waffenkammer im Süden
            this.b.chest(x1 - 1, 1, z1 - 1, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), this.armory);
            this.b.set(x1 - 1, 1, z1 - 2, Blocks.BARREL);
            this.b.set(x1 - 2, 1, z1 - 1, Blocks.BARREL);
            this.b.set(x0 + 2, 1, z1 - 1, Blocks.SMITHING_TABLE);
            this.b.set(x0 + 1, 1, z1 - 1, Blocks.GRINDSTONE.defaultBlockState().setValue(GrindstoneBlock.FACE, AttachFace.FLOOR));
            // Licht: unter der Decke und am First
            this.b.hangingLantern(21, 3, 0, Blocks.LANTERN);
            this.b.hangingLantern(21, 3, 8, Blocks.LANTERN);
            for (int z : new int[] {-2, 6}) {
                for (int y = 9; y <= 12; y++) {
                    this.b.set(21, y, z, chain());
                }
                this.b.hangingLantern(21, 8, z, Blocks.LANTERN);
            }
            this.wallBanner(x0 - 1, 7, 2, Direction.WEST);
        }

        /** Stall an der Westmauer: offen zum Hof, Boxen mit Heu und Tränke, Satteldach. */
        private void stable() {
            int x0 = -25;
            int x1 = -17;
            int z0 = -4;
            int z1 = 12;
            int eaves = 5;
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    this.b.set(x, 0, z, Blocks.PACKED_MUD);
                    boolean back = x == x0;
                    boolean end = z == z0 || z == z1;
                    boolean post = x == x1 && Math.floorMod(z - z0, 4) == 0;
                    for (int y = 1; y < eaves; y++) {
                        BlockState state = Blocks.AIR.defaultBlockState();
                        if (back || end && x != x1) {
                            state = y <= 2 ? this.stone(x, y, z) : this.s.planks().defaultBlockState();
                        } else if (post || end) {
                            state = this.s.log().defaultBlockState();
                        }
                        this.b.set(x, y, z, state);
                    }
                }
            }
            this.b.gableAlongZ(x0, x1, z0, z1, eaves, this.s.roofStairs(), this.s.roof().defaultBlockState(),
                    this.s.roofSlab().defaultBlockState(), this.s.planks().defaultBlockState());
            // Boxen
            for (int z = z0 + 1; z < z1; z++) {
                int inBox = Math.floorMod(z - z0, 4);
                if (inBox == 0) {
                    for (int x = x0 + 1; x <= x0 + 4; x++) {
                        this.b.set(x, 1, z, this.s.fence());
                    }
                } else if (inBox == 1) {
                    this.b.set(x0 + 1, 1, z, Blocks.HAY_BLOCK);
                } else if (inBox == 2) {
                    this.b.set(x0 + 1, 1, z, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
                }
            }
            this.b.set(x1 - 1, 1, z1 - 1, Blocks.HAY_BLOCK);
            this.b.set(x1 - 1, 2, z1 - 1, Blocks.HAY_BLOCK);
            this.b.set(x1 - 2, 1, z1 - 1, Blocks.HAY_BLOCK);
            this.b.hangingLantern(-21, 4, 0, Blocks.LANTERN);
            this.b.hangingLantern(-21, 4, 8, Blocks.LANTERN);
        }

        /** Schmiede: offene Halle mit Rückwand, Esse mit Kamin, Amboss, Schleifstein, Wassertrog. */
        private void smithy() {
            int x0 = 11;
            int x1 = 18;
            int z0 = 15;
            int z1 = 22;
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    this.b.set(x, 0, z, this.s.floor());
                    boolean corner = (x == x0 || x == x1) && (z == z0 || z == z1);
                    for (int y = 1; y <= 4; y++) {
                        if (corner) {
                            this.b.set(x, y, z, this.s.log());
                        } else if (z == z1) {
                            this.wallBlock(x, y, z);
                        } else {
                            this.b.set(x, y, z, Blocks.AIR);
                        }
                    }
                }
            }
            this.b.hipRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, 5, this.s.roofStairs(), this.s.roof().defaultBlockState());
            // Esse mit Kamin
            for (int y = 1; y <= 10; y++) {
                for (int x = x1 - 2; x <= x1 - 1; x++) {
                    this.b.set(x, y, z1, y <= 2 ? this.s.base().defaultBlockState() : this.stone(x, y, z1));
                }
            }
            this.b.set(x1 - 1, 11, z1, Blocks.CAMPFIRE);
            this.b.set(x1 - 2, 1, z1 - 1, Blocks.BLAST_FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.NORTH));
            this.b.set(x1 - 1, 1, z1 - 1, Blocks.FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.NORTH));
            this.b.set(x0 + 2, 1, z0 + 3, Blocks.ANVIL);
            this.b.set(x0 + 4, 1, z0 + 2, Blocks.GRINDSTONE.defaultBlockState().setValue(GrindstoneBlock.FACE, AttachFace.FLOOR));
            this.b.set(x0 + 1, 1, z1 - 1, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
            this.b.set(x0 + 2, 1, z1 - 1, Blocks.SMITHING_TABLE);
            this.b.set(x0 + 1, 1, z0 + 1, Blocks.BARREL);
            this.b.hangingLantern(x0 + 3, 4, z0 + 3, Blocks.LANTERN);
        }

        /** Kapelle: Schiff mit Bänken, Altar, Fenster in den Farben des Reichs, Glockenturm über dem Portal. */
        private void chapel() {
            int x0 = -25;
            int x1 = -15;
            int z0 = -24;
            int z1 = -14;
            int mid = (x0 + x1) / 2;
            int eaves = 6;
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean shell = x == x0 || x == x1 || z == z0 || z == z1;
                    boolean corner = (x == x0 || x == x1) && (z == z0 || z == z1);
                    this.b.set(x, 0, z, this.s.floor());
                    for (int y = 1; y <= eaves; y++) {
                        this.b.set(x, y, z, !shell ? Blocks.AIR.defaultBlockState() : corner ? this.s.trim().defaultBlockState()
                                : this.stone(x, y, z));
                    }
                }
            }
            this.b.gableAlongZ(x0, x1, z0, z1, eaves + 1, this.s.roofStairs(), this.s.roof().defaultBlockState(),
                    this.s.roofSlab().defaultBlockState(), this.s.wall().defaultBlockState());
            // Portal im Süden
            this.b.door(mid, 1, z1, this.s.door(), Direction.NORTH);
            this.b.set(mid, 3, z1, Blocks.AIR);
            this.b.set(mid, 4, z1 + 1, this.trimStairs(Direction.SOUTH, Half.TOP));
            // Fenster in den Seitenwänden
            DyeColor second = this.realm.charge() == DyeColor.BLACK ? DyeColor.GRAY : this.realm.charge();
            for (int z = z0 + 2; z <= z1 - 2; z += 3) {
                for (int y = 2; y <= 4; y++) {
                    this.b.set(x0, y, z, Blocks.STAINED_GLASS_PANE.pick(y == 3 ? second : this.realm.field()));
                    this.b.set(x1, y, z, Blocks.STAINED_GLASS_PANE.pick(y == 3 ? second : this.realm.field()));
                }
            }
            // Bänke
            for (int z = z0 + 4; z <= z1 - 2; z += 2) {
                for (int x = x0 + 2; x <= x1 - 2; x++) {
                    if (x != mid) {
                        this.b.set(x, 1, z, VillageBuilder.stairs(this.s.woodStairs(), Direction.SOUTH, Half.BOTTOM));
                    }
                }
            }
            // Altar mit Kerzen und dem Banner dahinter
            this.b.set(mid, 1, z0 + 1, this.s.trim());
            this.b.set(mid - 1, 1, z0 + 1, this.trimStairs(Direction.EAST, Half.TOP));
            this.b.set(mid + 1, 1, z0 + 1, this.trimStairs(Direction.WEST, Half.TOP));
            this.b.set(mid, 2, z0 + 1, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3));
            this.b.set(mid - 1, 2, z0 + 1, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 1));
            this.b.set(mid + 1, 2, z0 + 1, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 1));
            this.wallBanner(mid, 5, z0 + 1, Direction.SOUTH);
            this.b.hangingLantern(mid, eaves + 1, -19, Blocks.LANTERN);
            // Glockenturm über dem Portal
            int belfry = eaves + 5;
            for (int y = eaves + 1; y <= belfry + 2; y++) {
                for (int x = mid - 1; x <= mid + 1; x++) {
                    for (int z = z1 - 2; z <= z1; z++) {
                        boolean centre = x == mid && z == z1 - 1 && y <= belfry + 1;
                        boolean open = y >= belfry && y <= belfry + 1 && (x == mid || z == z1 - 1);
                        this.b.set(x, y, z, centre || open ? Blocks.AIR.defaultBlockState() : this.stone(x, y, z));
                    }
                }
            }
            this.b.set(mid, belfry + 1, z1 - 1, Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING));
            this.b.hipRoof(mid - 2, z1 - 3, mid + 2, z1 + 1, belfry + 3, this.s.roofStairs(), this.s.roof().defaultBlockState());
        }

        /** Vorratshaus in der Nordostecke: Fässer, Heu und die Vorratstruhe. */
        private void storehouse() {
            int x0 = 14;
            int x1 = 25;
            int z0 = -25;
            int z1 = -17;
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean shell = x == x0 || x == x1 || z == z0 || z == z1;
                    boolean post = (x == x0 || x == x1) && (z == z0 || z == z1);
                    this.b.set(x, 0, z, this.s.floor());
                    for (int y = 1; y <= 5; y++) {
                        BlockState state = !shell ? Blocks.AIR.defaultBlockState() : post ? this.s.log().defaultBlockState()
                                : y <= 2 ? this.stone(x, y, z) : this.s.planks().defaultBlockState();
                        this.b.set(x, y, z, state);
                    }
                }
            }
            this.b.gableAlongX(x0, x1, z0, z1, 6, this.s.roofStairs(), this.s.roof().defaultBlockState(),
                    this.s.roofSlab().defaultBlockState(), this.s.planks().defaultBlockState());
            this.b.air(19, 1, z1, 20, 3, z1);
            for (int x = x0 + 1; x < x1; x++) {
                this.b.set(x, 1, z0 + 1, Blocks.BARREL);
                if (this.b.chance(x, 2, z0, 0.5f)) {
                    this.b.set(x, 2, z0 + 1, Blocks.BARREL);
                }
            }
            this.b.set(x0 + 1, 1, z1 - 1, Blocks.HAY_BLOCK);
            this.b.set(x0 + 2, 1, z1 - 1, Blocks.HAY_BLOCK);
            this.b.set(x0 + 1, 2, z1 - 1, Blocks.HAY_BLOCK);
            this.b.set(x0 + 1, 1, z1 - 2, Blocks.HAY_BLOCK);
            this.b.chest(x1 - 1, 1, z1 - 2, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), this.stores);
            this.b.hangingLantern(19, 5, -21, Blocks.LANTERN);
            this.b.set(x0 - 1, 3, -21, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST));
        }

        /** Brunnen mit Dach auf vier Pfosten. */
        private void well(int cx, int cz) {
            for (int x = cx - 2; x <= cx + 2; x++) {
                for (int z = cz - 2; z <= cz + 2; z++) {
                    boolean rim = Math.abs(x - cx) == 2 || Math.abs(z - cz) == 2;
                    boolean corner = Math.abs(x - cx) == 2 && Math.abs(z - cz) == 2;
                    this.b.set(x, 0, z, this.s.base());
                    if (rim) {
                        this.b.set(x, 1, z, corner ? this.s.trim().defaultBlockState() : this.s.wallWall().defaultBlockState());
                    } else {
                        for (int y = -4; y <= 0; y++) {
                            this.b.set(x, y, z, Blocks.WATER);
                        }
                        this.b.set(x, -5, z, this.s.base());
                    }
                    if (corner) {
                        this.b.set(x, 2, z, this.s.fence());
                        this.b.set(x, 3, z, this.s.fence());
                    }
                }
            }
            this.b.hipRoof(cx - 3, cz - 3, cx + 3, cz + 3, 4, this.s.roofStairs(), this.s.roof().defaultBlockState());
            for (int y = 3; y <= 6; y++) {
                this.b.set(cx, y, cz, chain());
            }
        }

        /** Übungsplatz vor der Kaserne: Zielscheiben auf Strohballen. */
        private void yard() {
            for (int z : new int[] {-5, -1, 9}) {
                this.b.set(13, 1, z, Blocks.HAY_BLOCK);
                this.b.set(13, 2, z, Blocks.TARGET);
            }
        }

        /** Laternen an den Wegen. */
        private void lamps() {
            for (int[] p : new int[][] {{-4, 12}, {4, 12}, {-4, 20}, {4, 20}, {-10, 1}, {10, 1}, {-14, -12}, {14, -12}}) {
                this.b.set(p[0], 1, p[1], this.s.fence());
                this.b.set(p[0], 2, p[1], this.s.fence());
                this.b.set(p[0], 3, p[1], Blocks.LANTERN);
            }
        }

        // =================================================================================
        // Banner
        // =================================================================================

        /** Banner an einer Wand ({@code facing}: wohin es zeigt). */
        private void wallBanner(int x, int y, int z, Direction facing) {
            BlockPos pos = this.b.world(x, y, z);
            if (!this.b.inside(pos)) {
                return;
            }
            this.b.set(x, y, z, this.realm.wallBanner().defaultBlockState().setValue(WallBannerBlock.FACING, facing));
            this.paint(pos);
        }

        /** Stehendes Banner (Drehung 0..15 in Sechzehnteln). */
        private void standingBanner(int x, int y, int z, int rotation) {
            BlockPos pos = this.b.world(x, y, z);
            if (!this.b.inside(pos)) {
                return;
            }
            this.b.set(x, y, z, this.realm.standingBanner().defaultBlockState()
                    .setValue(BannerBlock.ROTATION, RotationSegment.convertToSegment(rotation * 22.5f)));
            this.paint(pos);
        }

        private void paint(BlockPos pos) {
            if (this.b.level.getBlockEntity(pos) instanceof BannerBlockEntity banner) {
                banner.applyComponentsFromItemStack(this.realm.banner(this.b.level.registryAccess()));
                banner.setChanged();
            }
        }
    }
}
