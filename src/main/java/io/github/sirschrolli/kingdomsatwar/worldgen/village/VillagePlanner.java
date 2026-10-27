package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import io.github.sirschrolli.kingdomsatwar.economy.town.TownNames;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jspecify.annotations.Nullable;

/**
 * Plant ein Dorf, bevor ein einziger Block gesetzt wird: den Dorfplatz an der flachsten Stelle, drei bis vier Hauptwege
 * in die Himmelsrichtungen (sie folgen dem Gelände, über Wasser werden sie zu Brücken bzw. Stegen), hier und da einen
 * Seitenweg, und an den Wegen die Gebäude – vorn das Gasthaus, die Schmiede, die Bäckerei und die Kapelle, weiter außen
 * Felder, Mühle, Scheune und Schafweide, an den Wegenden Wachtürme. Alle Höhen stammen aus dem Geländegenerator; so
 * passen die Teile zusammen, egal in welcher Reihenfolge die Chunks entstehen.
 */
final class VillagePlanner {
    static final int ROAD_HALF = 1;
    /** Abstand der Hausfront von der Wegmitte (dazwischen liegt der Zugang zur Tür). */
    static final int FRONT = ROAD_HALF + 2;

    /** Nur für die Entwicklung: erfährt, warum ein Ort abgelehnt wurde. */
    static java.util.function.@Nullable Consumer<String> debug;

    private final VillageStyle style;
    private final VillageTerrain terrain;
    private final RandomSource random;
    private final long seed;
    private final int variant;
    private final String name;
    private final boolean stilt;
    private final List<KingdomVillagePiece> pieces = new ArrayList<>();
    private final List<BoundingBox> taken = new ArrayList<>();
    private final List<BoundingBox> roadAreas = new ArrayList<>();
    /** Grundflächen der Gebäude (für die Laternen). */
    private final List<BoundingBox> lots = new ArrayList<>();
    /** Die gesetzten Gebäude: Mitte der Vorderseite, Blickrichtung, Art und Fußboden (für Felder dahinter). */
    private final List<Lot> placedLots = new ArrayList<>();

    private record Lot(BlockPos front, Direction facing, VillageBuilding kind, int floor) {}
    private final List<Road> roads = new ArrayList<>();
    private BlockPos center = BlockPos.ZERO;
    private int radius;
    private int placed;
    /** Solange gesetzt, bleibt das äußere Stück der Hauptwege dem Dorfrand vorbehalten. */
    private boolean reserveOutskirts;

    /** Ein geplanter Weg: Anfang, Richtung, Höhe und Merkmale je Schritt; gebaut wird ab {@code skip}. */
    private static final class Road {
        final BlockPos start;
        final Direction dir;
        final int[] heights;
        final int[] flags;
        final int skip;
        final boolean arm;
        /** Belegte Schritte je Seite (für Laternen und weitere Bauplätze). */
        final boolean[][] used;

        Road(BlockPos start, Direction dir, int[] heights, int[] flags, int skip, boolean arm) {
            this.start = start;
            this.dir = dir;
            this.heights = heights;
            this.flags = flags;
            this.skip = skip;
            this.arm = arm;
            this.used = new boolean[2][heights.length];
        }

        int length() {
            return this.heights.length - 1;
        }

        int x(int t) {
            return this.start.getX() + this.dir.getStepX() * t;
        }

        int z(int t) {
            return this.start.getZ() + this.dir.getStepZ() * t;
        }
    }

    private VillagePlanner(VillageStyle style, VillageTerrain terrain, long seed) {
        this.style = style;
        this.terrain = terrain;
        this.seed = seed;
        this.random = RandomSource.create(seed);
        this.variant = this.random.nextInt(3);
        this.name = TownNames.town(this.random);
        this.stilt = style == VillageStyle.STILT;
    }

    static List<KingdomVillagePiece> plan(VillageStyle style, int cx, int cz, long seed, VillageTerrain terrain) {
        VillagePlanner planner = new VillagePlanner(style, terrain, seed);
        return planner.run(cx, cz) ? planner.pieces : List.of();
    }

    private boolean reject(String reason) {
        if (debug != null) {
            debug.accept(reason);
        }
        return false;
    }

    // =====================================================================================
    // Ablauf
    // =====================================================================================

    private boolean run(int cx, int cz) {
        if (!this.terrain.buildable(cx, cz)) {
            return this.reject("biome");
        }
        if (!this.stilt && this.terrain.water(cx, cz)) {
            return this.reject("water");
        }
        // Platz: Median der Höhen ringsum; zu steile Orte meiden
        List<Integer> heights = new ArrayList<>();
        int wet = 0;
        for (int dx = -8; dx <= 8; dx += 4) {
            for (int dz = -8; dz <= 8; dz += 4) {
                heights.add(this.level(cx + dx, cz + dz));
                if (this.terrain.water(cx + dx, cz + dz)) {
                    wet++;
                }
            }
        }
        Collections.sort(heights);
        int floor = heights.get(heights.size() / 2);
        if (heights.get(heights.size() - 1) - heights.get(0) > (this.stilt ? 5 : 7)) {
            return this.reject("steep");
        }
        int sea = this.terrain.seaLevel();
        if (!this.stilt && (floor < sea || wet > 2)) {
            return this.reject("low");
        }
        if (this.stilt) {
            if (wet > 18 || this.terrain.waterDepth(cx, cz) > 3) {
                return this.reject("deep");
            }
            // Pfahlbauten stehen zwei Blöcke über Wasser und Schlamm: man sieht die Pfähle
            floor = Math.max(floor, sea) + 2;
        }
        this.radius = this.stilt ? 6 : 7;
        this.center = new BlockPos(cx, floor, cz);
        this.taken.add(new BoundingBox(cx - this.radius - 1, 0, cz - this.radius - 1, cx + this.radius + 1, 0, cz + this.radius + 1));

        this.planRoads(floor);
        if (this.roads.size() < 2) {
            return this.reject("roads");
        }
        this.planBuildings();
        if (this.placed < 8) {
            return this.reject("few:" + this.placed);
        }
        this.planLamps();

        // Platz zuerst (er ist der Bezugspunkt), dann die Wege, dann die Gebäude
        int arms = 0;
        for (Road road : this.roads) {
            if (road.arm) {
                arms |= 1 << road.dir.get2DDataValue();
            }
        }
        int centerpiece = this.random.nextInt(4);
        List<KingdomVillagePiece> all = new ArrayList<>();
        all.add(KingdomVillagePiece.square(this.style, this.variant, this.center, this.name, this.radius, arms | centerpiece << 4,
                this.random.nextLong()));
        for (Road road : this.roads) {
            all.add(KingdomVillagePiece.road(this.style, this.variant, road.start, this.center, this.name, road.dir, road.heights, road.flags,
                    road.skip, this.random.nextLong()));
        }
        all.addAll(this.pieces);
        this.pieces.clear();
        this.pieces.addAll(all);
        if (debug != null) {
            StringBuilder roads = new StringBuilder();
            for (Road road : this.roads) {
                int bridges = 0;
                for (int t = road.skip; t <= road.length(); t++) {
                    if ((road.flags[t] & KingdomVillagePiece.BRIDGE) != 0) {
                        bridges++;
                    }
                }
                roads.append(road.dir.getSerializedName()).append(road.arm ? "" : "*").append('=').append(road.length()).append("/b").append(bridges)
                        .append(' ');
            }
            StringBuilder kinds = new StringBuilder();
            for (KingdomVillagePiece piece : this.pieces) {
                if (piece.building() != null) {
                    kinds.append(piece.building().id()).append(',');
                }
            }
            debug.accept("village " + this.name + " at " + this.center + ": " + this.placed + " buildings, roads " + roads + "kinds " + kinds
                    + " rejected " + this.rejections);
            debug.accept("map " + this.name + "\n" + this.map() + this.probe(VillageBuilding.HOUSE_SMALL));
        }
        return true;
    }

    /** Höhe, auf der hier gebaut würde: der Boden, bei Pfahlbauten über Wasser die Wasseroberfläche. */
    private int level(int x, int z) {
        return this.stilt ? Math.max(this.terrain.ground(x, z), this.terrain.surface(x, z) - 1) : this.terrain.ground(x, z);
    }

    // =====================================================================================
    // Wege
    // =====================================================================================

    private void planRoads(int floor) {
        List<Direction> dirs = new ArrayList<>(List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST));
        Collections.shuffle(dirs, new java.util.Random(this.seed));
        int count = 3 + (this.random.nextFloat() < 0.6f ? 1 : 0);
        List<Road> arms = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int length = (this.stilt ? 22 : 30) + this.random.nextInt(this.stilt ? 12 : 18);
            Road road = this.trace(this.center, dirs.get(i), length, this.radius + 1, floor, true);
            if (road != null) {
                arms.add(road);
                this.addRoad(road);
            }
        }
        // Seitengassen: von jedem Hauptweg alle dreizehn bis sechzehn Blöcke eine, abwechselnd links und rechts – so füllt
        // sich auch das Land zwischen den Hauptwegen. Die erste beginnt erst so weit vom Platz, dass zwischen ihr und dem
        // Nachbarweg (zu dem sie parallel läuft) noch eine Reihe Häuser Platz hat.
        for (Road arm : arms) {
            boolean clockwise = this.random.nextBoolean();
            float[] chance = {0.9f, 0.75f, 0.55f};
            int at = 16 + this.random.nextInt(4);
            for (int j = 0; j < chance.length && at < arm.length() - 6; j++, at += 13 + this.random.nextInt(4)) {
                if (this.random.nextFloat() > chance[j]) {
                    continue;
                }
                Direction side = (clockwise ^ (j % 2 == 1)) ? arm.dir.getClockWise() : arm.dir.getCounterClockWise();
                BlockPos start = new BlockPos(arm.x(at), arm.heights[at], arm.z(at));
                int length = (this.stilt ? 12 : 14) + this.random.nextInt(this.stilt ? 8 : 12);
                Road branch = this.trace(start, side, length, ROAD_HALF + 1, arm.heights[at], false);
                if (branch != null && !this.crossesOtherRoads(branch)) {
                    this.addRoad(branch);
                }
            }
        }
    }

    /**
     * Folgt dem Gelände von {@code start} in Richtung {@code dir}: je Block höchstens eine Stufe; über Wasser und Senken
     * eine Brücke. Bricht ab, wo das Land nicht zum Dorf gehört oder zu steil wird. {@code null}, wenn zu wenig Weg bleibt.
     */
    private @Nullable Road trace(BlockPos start, Direction dir, int length, int skip, int startY, boolean arm) {
        int[] heights = new int[length + 1];
        int[] flags = new int[length + 1];
        int previous = startY;
        int end = length;
        int waterRun = 0;
        for (int t = 0; t <= length; t++) {
            int x = start.getX() + dir.getStepX() * t;
            int z = start.getZ() + dir.getStepZ() * t;
            if (t < skip) {
                heights[t] = startY;
                continue;
            }
            boolean water = this.terrain.water(x, z);
            int ground = this.terrain.ground(x, z);
            int target;
            if (this.stilt) {
                target = Math.max(ground, this.terrain.surface(x, z) - 1) + 2;
            } else {
                target = water ? Math.max(this.terrain.surface(x, z), this.terrain.seaLevel() + 1) : ground;
            }
            boolean blocked = !this.terrain.allowed(x, z) || Math.abs(target - previous) > 4 || (!this.stilt && waterRun > 12)
                    || this.stilt && this.terrain.waterDepth(x, z) > 5;
            if (blocked) {
                end = t - 1;
                break;
            }
            waterRun = water ? waterRun + 1 : 0;
            int y = Math.max(previous - 1, Math.min(previous + 1, target));
            heights[t] = y;
            boolean bridge = this.stilt || water || y > ground + 2;
            flags[t] = bridge ? KingdomVillagePiece.BRIDGE : 0;
            previous = y;
        }
        // nicht mitten auf einer Brücke enden
        while (end > skip && (flags[end] & KingdomVillagePiece.BRIDGE) != 0 && !this.stilt) {
            end--;
        }
        if (end < skip + 8) {
            return null;
        }
        if (end < length) {
            heights = java.util.Arrays.copyOf(heights, end + 1);
            flags = java.util.Arrays.copyOf(flags, end + 1);
        }
        return new Road(start, dir, heights, flags, skip, arm);
    }

    private boolean crossesOtherRoads(Road road) {
        // erst ab dem Schritt nach dem Anschluss prüfen (dort berührt der Seitenweg seinen Hauptweg)
        Road probe = new Road(road.start, road.dir, road.heights, road.flags, road.skip + 1, false);
        BoundingBox box = roadBox(probe, 1);
        for (BoundingBox other : this.roadAreas) {
            if (box.intersects(other)) {
                return true;
            }
        }
        return false;
    }

    private void addRoad(Road road) {
        this.roads.add(road);
        BoundingBox area = roadBox(road, 0);
        this.roadAreas.add(area);
        this.taken.add(area);
    }

    /** Fläche eines Wegs (ab {@code skip}, Breite ± ROAD_HALF, zusätzlich {@code pad}). */
    private static BoundingBox roadBox(Road road, int pad) {
        int x0 = road.x(road.skip);
        int z0 = road.z(road.skip);
        int x1 = road.x(road.length());
        int z1 = road.z(road.length());
        int padX = (road.dir.getStepX() == 0 ? ROAD_HALF : 0) + pad;
        int padZ = (road.dir.getStepZ() == 0 ? ROAD_HALF : 0) + pad;
        return new BoundingBox(Math.min(x0, x1) - padX, 0, Math.min(z0, z1) - padZ, Math.max(x0, x1) + padX, 0, Math.max(z0, z1) + padZ);
    }

    // =====================================================================================
    // Gebäude
    // =====================================================================================

    private void planBuildings() {
        List<VillageBuilding> wanted = new ArrayList<>();
        // was ein Dorf ausmacht – Gasthaus, Schmiede, Bäckerei, ein erstes Wohnhaus und meist eine Kapelle
        wanted.add(VillageBuilding.TAVERN);
        wanted.add(VillageBuilding.SMITHY);
        wanted.add(VillageBuilding.BAKERY);
        wanted.add(VillageBuilding.HOUSE);
        if (this.random.nextFloat() < 0.8f) {
            wanted.add(VillageBuilding.CHAPEL);
        }
        // dazu einige Handwerker (vor den übrigen Wohnhäusern: sie sind es, mit denen man handelt)
        List<VillageBuilding> crafts = new ArrayList<>(List.of(VillageBuilding.SCRIPTORIUM, VillageBuilding.MAP_HOUSE, VillageBuilding.HERB_HOUSE,
                VillageBuilding.BUTCHERY, VillageBuilding.TANNERY, VillageBuilding.MASONRY, VillageBuilding.BOWYER));
        Collections.shuffle(crafts, new java.util.Random(this.seed ^ 0x51AFL));
        int craftCount = 4 + this.random.nextInt(3);
        wanted.addAll(crafts.subList(0, craftCount));
        wanted.addAll(List.of(VillageBuilding.HOUSE_SMALL, VillageBuilding.HOUSE_LARGE, VillageBuilding.HOUSE_SMALL, VillageBuilding.HOUSE,
                VillageBuilding.HOUSE_SMALL));
        // Dorfrand
        List<VillageBuilding> outskirts = new ArrayList<>();
        // Weide, Mühle und Scheune brauchen einen Platz am Weg, die Felder kommen zuletzt (sie passen auch hinter die Häuser)
        if (this.random.nextFloat() < 0.7f) {
            outskirts.add(VillageBuilding.SHEEPFOLD);
        }
        outskirts.add(VillageBuilding.WINDMILL);
        outskirts.add(VillageBuilding.BARN);
        int fields = 2 + this.random.nextInt(3);
        for (int i = 0; i < fields; i++) {
            outskirts.add(VillageBuilding.FIELD);
        }
        // eine Fischerhütte, wo ein Weg am Wasser endet oder das Ufer hinter einem Bauplatz liegt (im Moor überall)
        outskirts.addFirst(VillageBuilding.FISHING_HUT);
        if (this.stilt) {
            // im Moor keine Mühle und weniger Felder
            outskirts.remove(VillageBuilding.WINDMILL);
            outskirts.remove(VillageBuilding.FIELD);
        }
        // Wachtürme an bis zu zwei Wegenden
        int towers = 0;
        List<Road> ends = new ArrayList<>(this.roads);
        Collections.shuffle(ends, new java.util.Random(this.seed ^ 0x7031L));
        for (Road road : ends) {
            if (towers >= 2 || !road.arm) {
                continue;
            }
            int t = road.length();
            BlockPos front = new BlockPos(road.x(t + 2), 0, road.z(t + 2));
            if (this.tryPlace(VillageBuilding.WATCHTOWER, front, road.dir.getOpposite(), road.heights[t])) {
                towers++;
            }
        }
        // das äußere Stück der Hauptwege bleibt zunächst frei: dort liegen später Felder, Mühle und Scheune
        this.reserveOutskirts = !outskirts.isEmpty();
        List<VillageBuilding> missing = new ArrayList<>();
        for (VillageBuilding kind : wanted) {
            if (!this.placeAlongRoads(kind, false) && kind != VillageBuilding.HOUSE && kind != VillageBuilding.HOUSE_SMALL) {
                missing.add(kind);
            }
        }
        this.reserveOutskirts = false;
        // was vorhin keinen Platz fand (Kapelle, Handwerker), bekommt eine zweite Gelegenheit – jetzt auch weiter draußen
        for (VillageBuilding kind : missing) {
            this.placeAlongRoads(kind, false);
        }
        for (VillageBuilding kind : outskirts) {
            if (kind == VillageBuilding.FISHING_HUT) {
                // am liebsten dort, wo ein Weg am Ufer endet
                if (!this.placeAtRoadEnds(kind)) {
                    this.placeAlongRoads(kind, true);
                }
                continue;
            }
            // Felder brauchen keinen Weg vor dem Tor: sie liegen am liebsten hinter den Häusern am Dorfrand (die Weide nicht –
            // der Schäfer wohnt dort und muss abends heimfinden, auch wenn das Gelände hinter den Häusern steil abfällt)
            boolean behind = kind == VillageBuilding.FIELD && this.placeBehindLots(kind);
            if (!behind && !this.placeAlongRoads(kind, true) && !this.placeAtRoadEnds(kind) && kind != VillageBuilding.FIELD) {
                // zur Not auch weiter drinnen (eine Weide oder Scheune zwischen den Häusern)
                this.placeAlongRoads(kind, false);
            }
        }
        // was dann noch an Platz übrig ist, füllen Wohnhäuser – bis die Wege voll sind
        VillageBuilding[] fill = {VillageBuilding.HOUSE_SMALL, VillageBuilding.HOUSE, VillageBuilding.HOUSE_SMALL, VillageBuilding.HOUSE_LARGE};
        int failures = 0;
        for (int i = 0; i < 14 && failures < 3; i++) {
            failures = this.placeAlongRoads(fill[i % fill.length], false) ? 0 : failures + 1;
        }
    }

    /**
     * Sucht einen Bauplatz an den Wegen: innere Gebäude möglichst nah am Platz, Felder und Mühle möglichst weit draußen.
     */
    private boolean placeAlongRoads(VillageBuilding kind, boolean outer) {
        List<int[]> sides = new ArrayList<>();
        for (int i = 0; i < this.roads.size(); i++) {
            sides.add(new int[] {i, 0});
            sides.add(new int[] {i, 1});
        }
        Collections.shuffle(sides, new java.util.Random(this.random.nextLong()));
        for (int[] lot : sides) {
            Road road = this.roads.get(lot[0]);
            Direction side = lot[1] == 0 ? road.dir.getClockWise() : road.dir.getCounterClockWise();
            int first = road.skip + 2;
            int last = road.length() - kind.width / 2 - 1;
            if (outer) {
                for (int t = last; t >= Math.max(first, road.length() / 4); t -= 2) {
                    if (this.tryLot(kind, road, side, lot[1], t)) {
                        return true;
                    }
                }
            } else {
                if (this.reserveOutskirts && road.arm) {
                    // nur bis knapp zwei Drittel des Wegs, mindestens aber ein Bauplatz
                    last = Math.min(last, Math.max(first + kind.width / 2, road.length() * 5 / 8 - kind.width / 2));
                }
                for (int t = first + kind.width / 2; t <= last; t += 2) {
                    if (this.tryLot(kind, road, side, lot[1], t)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Ein Feld oder eine Weide hinter einem schon gesetzten Gebäude (gleiche Blickrichtung, einen Block Abstand) –
     * bevorzugt hinter den äußeren, so wachsen die Felder um das Dorf herum.
     */
    private boolean placeBehindLots(VillageBuilding kind) {
        List<Lot> candidates = new ArrayList<>(this.placedLots);
        Collections.shuffle(candidates, new java.util.Random(this.random.nextLong()));
        candidates.sort(java.util.Comparator.comparingDouble(lot -> -lot.front().distSqr(this.center)));
        int tried = 0;
        for (Lot lot : candidates) {
            if (lot.kind() == VillageBuilding.WATCHTOWER || lot.kind() == VillageBuilding.WINDMILL || tried++ > 8) {
                continue;
            }
            BlockPos front = lot.front().relative(lot.facing().getOpposite(), lot.kind().depth + 2);
            this.extras = KingdomVillagePiece.BEHIND;
            boolean ok = this.tryPlace(kind, front, lot.facing(), lot.floor());
            this.extras = 0;
            if (ok) {
                return true;
            }
        }
        return false;
    }

    /** Felder, Mühle und Scheune dürfen auch hinter dem Ende eines Wegs liegen (mit der Tür zum Weg). */
    private boolean placeAtRoadEnds(VillageBuilding kind) {
        for (Road road : this.roads) {
            int t = road.length();
            BlockPos front = new BlockPos(road.x(t) + road.dir.getStepX() * 2, 0, road.z(t) + road.dir.getStepZ() * 2);
            if (this.tryPlace(kind, front, road.dir.getOpposite(), road.heights[t])) {
                return true;
            }
        }
        return false;
    }

    private boolean tryLot(VillageBuilding kind, Road road, Direction side, int sideIndex, int t) {
        if ((road.flags[t] & KingdomVillagePiece.BRIDGE) != 0 && !this.stilt) {
            return this.why(kind, "bridge");
        }
        BlockPos front = new BlockPos(road.x(t) + side.getStepX() * FRONT, 0, road.z(t) + side.getStepZ() * FRONT);
        if (!this.tryPlace(kind, front, side.getOpposite(), road.heights[t])) {
            return false;
        }
        for (int k = Math.max(0, t - kind.width / 2 - 1); k <= Math.min(road.length(), t + kind.width / 2 + 1); k++) {
            road.used[sideIndex][k] = true;
        }
        return true;
    }

    /**
     * Versucht, ein Gebäude so zu setzen, dass seine Vorderseite (Mitte bei {@code front}) zum Weg ({@code facing})
     * zeigt. Der Fußboden liegt höchstens eine Stufe über oder unter dem Weg ({@code roadY}) davor.
     */
    /**
     * Ein Bauplatz am Ufer (Fischerhütte): die vorderen sechs Reihen (die Hütte) auf Land, die hinteren vier (der Steg)
     * über Wasser, das nicht zu tief ist. Der Fußboden liegt auf dem Land und mindestens einen Block über dem Wasser.
     */
    private boolean tryShore(VillageBuilding kind, BlockPos origin, Rotation rotation, BoundingBox rect, BlockPos front, Direction facing,
            int roadY) {
        int sum = 0;
        int n = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int lz = 0; lz < kind.depth; lz++) {
            for (int lx = 0; lx < kind.width; lx += 3) {
                BlockPos p = origin.offset(new BlockPos(lx, 0, lz).rotate(rotation));
                if (!this.terrain.allowed(p.getX(), p.getZ())) {
                    return this.why(kind, "biome");
                }
                boolean water = this.terrain.water(p.getX(), p.getZ());
                if (lz <= 5) {
                    // die Hütte: vorn festes Land, hinten darf das Ufer schon flach unter Wasser stehen (das Fundament füllt auf)
                    if (water && (lz <= 3 || this.terrain.waterDepth(p.getX(), p.getZ()) > 2)) {
                        return this.why(kind, "water");
                    }
                    if (!water) {
                        int g = this.terrain.ground(p.getX(), p.getZ());
                        sum += g;
                        n++;
                        min = Math.min(min, g);
                        max = Math.max(max, g);
                    }
                } else if (lz >= 8 && (!water || this.terrain.waterDepth(p.getX(), p.getZ()) > 6)) {
                    return this.why(kind, "shore");
                }
            }
        }
        if (max - min > 3) {
            return this.why(kind, "steep " + (max - min));
        }
        int floor = Math.max(Math.clamp(Math.round(sum / (float) n), roadY - 1, roadY + 1), this.terrain.seaLevel() + 1);
        if (floor - min > 4 || max - floor > 3 || Math.abs(floor - roadY) > 1) {
            return this.why(kind, "slope");
        }
        if (this.dryRun) {
            this.lastReason = "ok";
            return false;
        }
        this.pieces.add(KingdomVillagePiece.building(this.style, this.variant, kind, origin.atY(floor), rotation, this.random.nextLong(),
                this.center, this.name, floor - roadY, this.extras));
        this.placedLots.add(new Lot(front, facing, kind, floor));
        this.taken.add(rect);
        this.lots.add(rect);
        this.placed++;
        return true;
    }

    /** Nur für die Entwicklung: Trockenlauf – setzt nichts, merkt sich nur den Grund der Ablehnung. */
    private boolean dryRun;
    private String lastReason = "";
    /** Besonderheiten für das nächste gesetzte Gebäude ({@link KingdomVillagePiece#BEHIND}). */
    private int extras;

    private boolean tryPlace(VillageBuilding kind, BlockPos front, Direction facing, int roadY) {
        Rotation rotation = switch (facing) {
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
        int w = kind.width;
        int d = kind.depth;
        BlockPos origin = front.subtract(new BlockPos(w / 2, 0, 0).rotate(rotation));
        BoundingBox rect = KingdomVillagePiece.footprint(origin, rotation, w, d, 0, 0);
        BoundingBox padded = rect.inflatedBy(1, 0, 1);
        for (BoundingBox other : this.taken) {
            if (padded.intersects(other)) {
                return this.why(kind, this.roadAreas.contains(other) ? "road" : this.lots.contains(other) ? "lot" : "square");
            }
        }
        if (kind == VillageBuilding.FISHING_HUT && !this.stilt) {
            return this.tryShore(kind, origin, rotation, rect, front, facing, roadY);
        }
        // Höhe: Mittel aus Ecken, Kantenmitten und Mitte; nicht im Wasser (außer Pfahlbau), nicht zu steil
        int sum = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int n = 0;
        int maxDepth = 0;
        for (int ix = 0; ix <= 2; ix++) {
            for (int iz = 0; iz <= 2; iz++) {
                int x = ix == 0 ? rect.minX() : ix == 1 ? (rect.minX() + rect.maxX()) / 2 : rect.maxX();
                int z = iz == 0 ? rect.minZ() : iz == 1 ? (rect.minZ() + rect.maxZ()) / 2 : rect.maxZ();
                if (!this.terrain.buildable(x, z)) {
                    return this.why(kind, "biome");
                }
                if (this.terrain.water(x, z)) {
                    if (!this.stilt) {
                        return this.why(kind, "water");
                    }
                    maxDepth = Math.max(maxDepth, this.terrain.waterDepth(x, z));
                }
                int g = this.terrain.ground(x, z);
                sum += g;
                min = Math.min(min, g);
                max = Math.max(max, g);
                n++;
            }
        }
        int floor;
        if (this.stilt) {
            if (maxDepth > 4 || max - min > 5) {
                return this.why(kind, "stilts");
            }
            floor = Math.max(roadY, max + 2);
            if (floor > roadY + 1) {
                return this.why(kind, "hill");
            }
        } else {
            int spread = kind == VillageBuilding.FIELD ? 4 : 5;
            if (max - min > spread) {
                return this.why(kind, "steep " + (max - min));
            }
            floor = Math.clamp(Math.round(sum / (float) n), roadY - 1, roadY + 1);
            if (floor - min > 4 || max - floor > 3) {
                return this.why(kind, "slope");
            }
        }
        if (this.dryRun) {
            this.lastReason = "ok";
            return false;
        }
        this.pieces.add(KingdomVillagePiece.building(this.style, this.variant, kind, origin.atY(floor), rotation, this.random.nextLong(),
                this.center, this.name, floor - roadY, this.extras));
        this.placedLots.add(new Lot(front, facing, kind, floor));
        this.taken.add(rect);
        this.lots.add(rect);
        this.placed++;
        return true;
    }

    /** Nur für die Entwicklung: je Wegseite und Schritt, ob ein Gebäude dort noch Platz hätte und woran es sonst scheitert. */
    private String probe(VillageBuilding kind) {
        this.dryRun = true;
        StringBuilder out = new StringBuilder();
        for (Road road : this.roads) {
            for (int si = 0; si < 2; si++) {
                Direction side = si == 0 ? road.dir.getClockWise() : road.dir.getCounterClockWise();
                out.append(road.dir.getSerializedName()).append(road.arm ? "" : "*").append(" ").append(side.getSerializedName()).append(": ");
                for (int t = road.skip + 2 + kind.width / 2; t <= road.length() - kind.width / 2 - 1; t++) {
                    this.lastReason = "?";
                    this.tryLot(kind, road, side, si, t);
                    out.append(this.lastReason.isEmpty() ? '?' : this.lastReason.equals("ok") ? '+' : this.lastReason.charAt(0));
                }
                out.append('\n');
            }
        }
        this.dryRun = false;
        return out.toString();
    }

    /** Nur für die Entwicklung: der Plan von oben (Wege #, Platz O, Gebäude als Buchstaben, Wasser ~). */
    private String map() {
        int r = 60;
        char[][] grid = new char[2 * r + 1][2 * r + 1];
        for (int z = -r; z <= r; z++) {
            for (int x = -r; x <= r; x++) {
                int wx = this.center.getX() + x;
                int wz = this.center.getZ() + z;
                grid[z + r][x + r] = !this.terrain.allowed(wx, wz) ? ' ' : this.terrain.water(wx, wz) ? '~' : '.';
            }
        }
        for (BoundingBox box : this.roadAreas) {
            this.paint(grid, r, box, '#');
        }
        for (Road road : this.roads) {
            for (int t = road.skip; t <= road.length(); t++) {
                if ((road.flags[t] & KingdomVillagePiece.BRIDGE) != 0) {
                    this.paint(grid, r, new BoundingBox(road.x(t), 0, road.z(t), road.x(t), 0, road.z(t)), '=');
                }
            }
        }
        this.paint(grid, r, new BoundingBox(this.center.getX() - this.radius, 0, this.center.getZ() - this.radius, this.center.getX() + this.radius, 0,
                this.center.getZ() + this.radius), 'O');
        int i = 0;
        for (KingdomVillagePiece piece : this.pieces) {
            if (piece.building() != null) {
                BoundingBox box = KingdomVillagePiece.footprint(piece.origin(), piece.rotation(), piece.building().width, piece.building().depth, 0, 0);
                this.paint(grid, r, box, (char) ('A' + (i++ % 26)));
            }
        }
        StringBuilder out = new StringBuilder();
        for (char[] row : grid) {
            out.append(row).append('\n');
        }
        return out.toString();
    }

    private void paint(char[][] grid, int r, BoundingBox box, char c) {
        for (int z = box.minZ(); z <= box.maxZ(); z++) {
            for (int x = box.minX(); x <= box.maxX(); x++) {
                int gx = x - this.center.getX() + r;
                int gz = z - this.center.getZ() + r;
                if (gx >= 0 && gz >= 0 && gx < grid.length && gz < grid.length) {
                    grid[gz][gx] = c;
                }
            }
        }
    }

    /** Nur für die Entwicklung: warum Bauplätze abgelehnt wurden (Gebäude → Grund → Anzahl). */
    private final java.util.Map<String, Integer> rejections = new java.util.TreeMap<>();

    private boolean why(VillageBuilding kind, String reason) {
        this.lastReason = reason;
        if (debug != null && !this.dryRun) {
            this.rejections.merge(kind.id() + ":" + reason.split(" ")[0], 1, Integer::sum);
        }
        return false;
    }

    // =====================================================================================
    // Laternen
    // =====================================================================================

    /** Alle acht Blöcke eine Laterne am Wegrand, abwechselnd links und rechts – nie vor einer Tür oder auf einem Bauplatz. */
    private void planLamps() {
        for (Road road : this.roads) {
            int count = 0;
            for (int t = road.skip + 3; t < road.length(); t += 8) {
                int sideIndex = (count++ & 1);
                for (int attempt = 0; attempt < 2; attempt++) {
                    int si = (sideIndex + attempt) & 1;
                    Direction side = si == 0 ? road.dir.getClockWise() : road.dir.getCounterClockWise();
                    int x = road.x(t) + side.getStepX() * 2;
                    int z = road.z(t) + side.getStepZ() * 2;
                    if (this.free(x, z) && !road.used[si][t]) {
                        road.flags[t] |= si == 0 ? KingdomVillagePiece.LAMP_RIGHT : KingdomVillagePiece.LAMP_LEFT;
                        break;
                    }
                }
            }
        }
    }

    /** Steht hier nichts im Weg (kein Gebäude samt Rand, kein anderer Weg, nicht der Platz)? */
    private boolean free(int x, int z) {
        for (BoundingBox box : this.lots) {
            if (box.inflatedBy(1, 0, 1).isInside(x, 0, z)) {
                return false;
            }
        }
        for (BoundingBox box : this.roadAreas) {
            if (box.isInside(x, 0, z)) {
                return false;
            }
        }
        int dx = x - this.center.getX();
        int dz = z - this.center.getZ();
        return Math.max(Math.abs(dx), Math.abs(dz)) > this.radius + 1;
    }
}
