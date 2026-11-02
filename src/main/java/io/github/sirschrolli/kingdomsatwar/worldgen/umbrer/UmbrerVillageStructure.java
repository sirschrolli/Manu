package io.github.sirschrolli.kingdomsatwar.worldgen.umbrer;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Ein Dorf der Umbrer: ein runder Platz mit dem Mondbrunnen in der Mitte, vier Wege mit Laternenpfählen (über Wasser
 * als Laternenbrücken), an den Wegen Wohnhäuser, Langhäuser, die Mondschmiede, die Kräuterhütte, die
 * Kristallwerkstatt, der Seherturm aus Schattenstein, Dämmerkornfelder und ein Kristallgarten; an den Wegenden
 * Wachtürme. Alle Höhen werden schon beim Planen aus dem Geländegenerator berechnet – so passen die Teile zusammen,
 * egal in welcher Reihenfolge die Chunks entstehen.
 */
public class UmbrerVillageStructure extends Structure {
    public static final MapCodec<UmbrerVillageStructure> CODEC = simpleCodec(UmbrerVillageStructure::new);

    public UmbrerVillageStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.UMBRER_VILLAGE.get();
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        // schneller Vorab-Test: ohne passendes Biom in der Säule muss das Gelände gar nicht erst geplant werden
        if (!context.couldValidBiomeExistOnTopOfChunkCenter()) {
            return Optional.empty();
        }
        if (nearCitadel(context, chunk)) {
            return Optional.empty();
        }
        Terrain terrain = new Terrain(context);
        long seed = context.random().nextLong();
        List<UmbrerVillagePiece> pieces = plan(chunk.getMiddleBlockX(), chunk.getMiddleBlockZ(), seed, terrain);
        if (pieces.isEmpty()) {
            return Optional.empty();
        }
        UmbrerVillagePiece plaza = pieces.get(0);
        return Optional.of(new Structure.GenerationStub(plaza.origin(), builder -> pieces.forEach(builder::addPiece)));
    }

    /** Kein Dorf im Schatten der Zitadelle: mindestens 160 Blöcke Abstand zu Nyxars Festung. */
    private static boolean nearCitadel(Structure.GenerationContext context, ChunkPos chunk) {
        return nearCitadelArea(context, chunk, 160);
    }

    /** Liegt der Chunk näher als {@code distance} Blöcke an Nyxars Zitadelle? */
    static boolean nearCitadelArea(Structure.GenerationContext context, ChunkPos chunk, int distance) {
        var set = context.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET)
                .get(io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures.SHADOW_CITADEL_SET);
        if (set.isEmpty() || !(set.get().value().placement()
                instanceof io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelPlacement placement)) {
            return false;
        }
        ChunkPos citadel = io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelLocator.find(context.seed(), context.biomeSource(),
                context.randomState(), placement);
        if (citadel == null) {
            return false;
        }
        double dx = citadel.getMiddleBlockX() - chunk.getMiddleBlockX();
        double dz = citadel.getMiddleBlockZ() - chunk.getMiddleBlockZ();
        return dx * dx + dz * dz < (double) distance * distance;
    }

    // =====================================================================================
    // Lichtung: Bäume, Felsnadeln und Ranken meiden das Dorf
    // =====================================================================================

    /**
     * Liegt der Punkt höchstens {@code margin} Blöcke neben einem Bauteil eines Umbrer-Dorfs? Für Merkmale, die erst nach
     * dem Dorf entstehen (Bäume, Felsnadeln, Ranken): sie lassen das Dorf frei, als stünde es auf einer Lichtung. Gelesen
     * werden nur Chunks, die beim Erzeugen ohnehin bereitstehen (Bezüge in der Nähe, der Startchunk höchstens 8 Chunks
     * entfernt).
     */
    public static boolean isNearVillage(net.minecraft.world.level.WorldGenLevel level, BlockPos pos, int margin) {
        // auch rund um Nyxars Zitadelle bleibt das Land frei (Bäume ragten sonst über die Mauern in den Thronhof)
        if (isNear(level, pos, margin, ModStructures.UMBRER_VILLAGE_STRUCTURE) || isNear(level, pos, margin, ModStructures.SHADOW_CITADEL_STRUCTURE)) {
            return true;
        }
        for (ShadowSiteStructure.Kind kind : ShadowSiteStructure.Kind.values()) {
            if (isNear(level, pos, margin, ModStructures.shadowSite(kind))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNear(net.minecraft.world.level.WorldGenLevel level, BlockPos pos, int margin,
            net.minecraft.resources.ResourceKey<Structure> structureKey) {
        Structure village = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getValue(structureKey);
        if (village == null) {
            return false;
        }
        it.unimi.dsi.fastutil.longs.LongSet starts = new it.unimi.dsi.fastutil.longs.LongOpenHashSet();
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                int cx = (pos.getX() + dx * margin) >> 4;
                int cz = (pos.getZ() + dz * margin) >> 4;
                var chunk = level.getChunk(cx, cz, net.minecraft.world.level.chunk.status.ChunkStatus.STRUCTURE_REFERENCES, false);
                if (chunk != null) {
                    starts.addAll(chunk.getReferencesForStructure(village));
                }
            }
        }
        for (long key : starts) {
            ChunkPos startPos = ChunkPos.unpack(key);
            var chunk = level.getChunk(startPos.x(), startPos.z(), net.minecraft.world.level.chunk.status.ChunkStatus.STRUCTURE_STARTS, false);
            var start = chunk != null ? chunk.getStartForStructure(village) : null;
            if (start == null || !start.isValid()) {
                continue;
            }
            for (var piece : start.getPieces()) {
                BoundingBox box = piece instanceof ShadowSitePiece site ? site.clearing() : piece.getBoundingBox();
                if (pos.getX() >= box.minX() - margin && pos.getX() <= box.maxX() + margin
                        && pos.getZ() >= box.minZ() - margin && pos.getZ() <= box.maxZ() + margin) {
                    return true;
                }
            }
        }
        return false;
    }

    // =====================================================================================
    // Gelände
    // =====================================================================================

    /** Geländehöhen aus dem Generator (mit Zwischenspeicher). */
    static final class Terrain {
        private final Structure.@org.jspecify.annotations.Nullable GenerationContext context;
        private final Map<Long, int[]> cache = new HashMap<>();
        /** Ebenes Land (Tests, Vorführungen): oberster Block und Meeresspiegel. */
        private final int flatY;
        private final int flatSea;

        Terrain(Structure.GenerationContext context) {
            this.context = context;
            this.flatY = 0;
            this.flatSea = 0;
        }

        private Terrain(int flatY, int flatSea) {
            this.context = null;
            this.flatY = flatY;
            this.flatSea = flatSea;
        }

        /** Ebenes, trockenes Land mit dem obersten Block bei {@code y}. */
        static Terrain flat(int y, int seaLevel) {
            return new Terrain(y, seaLevel);
        }

        private int[] column(int x, int z) {
            if (this.context == null) {
                return new int[] {this.flatY + 1, this.flatY + 1};
            }
            return this.cache.computeIfAbsent(BlockPos.asLong(x, 0, z), key -> new int[] {
                    this.context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, this.context.heightAccessor(),
                            this.context.randomState()),
                    this.context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, this.context.heightAccessor(),
                            this.context.randomState())});
        }

        /** Oberster fester Block (y). */
        int ground(int x, int z) {
            return this.column(x, z)[0] - 1;
        }

        /** Erste freie Höhe über einem Gewässer (sonst über dem Boden). */
        int surface(int x, int z) {
            return this.column(x, z)[1];
        }

        boolean water(int x, int z) {
            int[] c = this.column(x, z);
            return c[1] > c[0];
        }

        int seaLevel() {
            return this.context == null ? this.flatSea : this.context.chunkGenerator().getSeaLevel();
        }
    }

    /**
     * Für Tests und Vorführungen: ein Dorf der Umbrer auf ebenem Land (oberster Block bei {@code y}) planen;
     * {@code layout} und {@code centerpiece} vorgeben ({@code null} bzw. −1: nach Zufall).
     */
    public static List<UmbrerVillagePiece> planFlat(int cx, int cz, int y, long seed, @org.jspecify.annotations.Nullable Layout layout,
            int centerpiece) {
        return plan(cx, cz, seed, Terrain.flat(y, y - 8), layout, centerpiece);
    }

    // =====================================================================================
    // Planung
    // =====================================================================================

    static final int PLAZA_RADIUS = 7;
    /** Nur für die Entwicklung: erfährt, warum ein Ort abgelehnt wurde. */
    public static java.util.function.@org.jspecify.annotations.Nullable Consumer<String> debug;

    private static List<UmbrerVillagePiece> reject(String reason) {
        if (debug != null) {
            debug.accept(reason);
        }
        return List.of();
    }
    private static final int ROAD_HALF = 1;

    /** Plant das Dorf; die erste Liste ist leer, wenn der Ort nicht taugt (Wasser, zu steil). */
    static List<UmbrerVillagePiece> plan(int cx, int cz, long seed, Terrain terrain) {
        return plan(cx, cz, seed, terrain, null, -1);
    }

    static List<UmbrerVillagePiece> plan(int cx, int cz, long seed, Terrain terrain, @org.jspecify.annotations.Nullable Layout forcedLayout,
            int forcedCenterpiece) {
        RandomSource random = RandomSource.create(seed);
        if (terrain.water(cx, cz)) {
            return reject("water");
        }
        // Platz: Median der Höhen ringsum; zu steile Orte meiden
        List<Integer> heights = new ArrayList<>();
        for (int dx = -6; dx <= 6; dx += 3) {
            for (int dz = -6; dz <= 6; dz += 3) {
                heights.add(terrain.ground(cx + dx, cz + dz));
            }
        }
        Collections.sort(heights);
        int floor = heights.get(heights.size() / 2);
        if (heights.get(heights.size() - 1) - heights.get(0) > 9) {
            return reject("steep");
        }
        if (floor <= terrain.seaLevel()) {
            return reject("low");
        }
        List<UmbrerVillagePiece> pieces = new ArrayList<>();
        List<BoundingBox> taken = new ArrayList<>();
        BlockPos center = new BlockPos(cx, floor, cz);
        // der Grundriss (eigener Zufall): Stern mit vier Wegen, eine lange Gasse, drei Wege oder ein Ring um den Platz;
        // dazu das Mittelstück des Platzes (Mondbrunnen, Kristallhain, Schattenbaum oder Steinkreis)
        RandomSource shape = RandomSource.create(seed ^ 0x554D4252L);
        List<Direction> dirs = new ArrayList<>(List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST));
        Collections.shuffle(dirs, new java.util.Random(shape.nextLong()));
        Layout layout = forcedLayout != null ? forcedLayout : Layout.values()[shape.nextInt(Layout.values().length)];
        int centerpiece = forcedCenterpiece >= 0 ? forcedCenterpiece & 3 : shape.nextInt(4);
        pieces.add(UmbrerVillagePiece.plaza(center, PLAZA_RADIUS, random.nextLong(), centerpiece));
        taken.add(new BoundingBox(cx - PLAZA_RADIUS - 1, 0, cz - PLAZA_RADIUS - 1, cx + PLAZA_RADIUS + 1, 0, cz + PLAZA_RADIUS + 1));
        List<Road> roads = new ArrayList<>();
        if (layout == Layout.RING) {
            int d = RING;
            Direction[] along = {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};
            BlockPos[] corners = {center.offset(-d, 0, -d), center.offset(d, 0, -d), center.offset(d, 0, d), center.offset(-d, 0, d)};
            for (int i = 0; i < 4 && layout == Layout.RING; i++) {
                Road ring = flatRoad(terrain, corners[i], along[i], 2 * d, floor);
                if (ring == null) {
                    // der Ring passt nicht ins Gelände: dann ein Stern
                    layout = Layout.STAR;
                    roads.clear();
                } else {
                    roads.add(ring);
                }
            }
        }
        switch (layout) {
            case STAR -> {
                for (Direction dir : dirs) {
                    roads.add(trace(terrain, center, dir, 30 + random.nextInt(6), PLAZA_RADIUS, floor, true));
                }
            }
            case LANE -> {
                Direction main = shape.nextBoolean() ? Direction.NORTH : Direction.EAST;
                for (Direction dir : List.of(main, main.getOpposite())) {
                    roads.add(trace(terrain, center, dir, 40 + shape.nextInt(10), PLAZA_RADIUS, floor, true));
                }
            }
            case FORK -> {
                for (Direction dir : dirs.subList(0, 3)) {
                    roads.add(trace(terrain, center, dir, 32 + shape.nextInt(8), PLAZA_RADIUS, floor, true));
                }
            }
            case RING -> {
                for (Direction dir : dirs.subList(0, 2)) {
                    roads.add(trace(terrain, center, dir, RING + 1 + 24 + shape.nextInt(8), RING + 1, floor, true));
                }
            }
        }
        for (Road road : roads) {
            pieces.add(UmbrerVillagePiece.road(road.start, center, road.dir, road.heights, road.bridges, road.skip, random.nextLong()));
            taken.add(road.area());
        }

        // Wachtürme an (bis zu) zwei Wegenden
        List<Road> arms = new ArrayList<>(roads.stream().filter(r -> r.arm).toList());
        Collections.shuffle(arms, new java.util.Random(seed));
        int towers = 0;
        for (Road road : arms) {
            if (towers >= 2) {
                break;
            }
            // ein Block Abstand zum Wegende: dort liegt der Zugang (sonst überschneidet sich der Turm mit seinem eigenen Weg)
            int t = road.length();
            BlockPos front = new BlockPos(road.x(t) + road.dir.getStepX() * 2, 0, road.z(t) + road.dir.getStepZ() * 2);
            if (tryPlace(pieces, taken, terrain, UmbrerVillagePiece.Kind.WATCHTOWER, front, road.dir.getOpposite(), road.heights[t], center,
                    random)) {
                towers++;
            }
        }

        // Häuser an den Wegen: erst, was ein Dorf braucht (der Seherturm fehlt mancherorts), dann eine bunte Mischung
        List<UmbrerVillagePiece.Kind> wanted = new ArrayList<>();
        if (shape.nextFloat() < 0.8f) {
            wanted.add(UmbrerVillagePiece.Kind.SEER_TOWER);
        }
        wanted.addAll(List.of(UmbrerVillagePiece.Kind.SMITHY, UmbrerVillagePiece.Kind.HERB_HUT, UmbrerVillagePiece.Kind.CRYSTAL_WORKSHOP,
                UmbrerVillagePiece.Kind.FARM, UmbrerVillagePiece.Kind.HOUSE));
        List<UmbrerVillagePiece.Kind> rest = new ArrayList<>();
        int houses = 3 + shape.nextInt(3);
        int longhouses = 1 + shape.nextInt(3);
        int farms = 1 + shape.nextInt(3);
        int gardens = shape.nextInt(3);
        for (int i = 0; i < houses; i++) {
            rest.add(UmbrerVillagePiece.Kind.HOUSE);
        }
        for (int i = 0; i < longhouses; i++) {
            rest.add(UmbrerVillagePiece.Kind.LONGHOUSE);
        }
        for (int i = 0; i < farms; i++) {
            rest.add(UmbrerVillagePiece.Kind.FARM);
        }
        for (int i = 0; i < gardens; i++) {
            rest.add(UmbrerVillagePiece.Kind.CRYSTAL_GARDEN);
        }
        Collections.shuffle(rest, new java.util.Random(shape.nextLong()));
        wanted.addAll(rest);
        // Bauplätze: an jeder Wegseite reihen sich die Gebäude vom Platz nach außen aneinander (2 Blöcke Abstand);
        // wo Wasser oder ein Hang stört, rückt der Platz ein Stück weiter. Am Ring stehen die Häuser zum Platz hin.
        int[][] cursor = new int[roads.size()][2];
        List<int[]> sides = new ArrayList<>();
        for (int i = 0; i < roads.size(); i++) {
            Road road = roads.get(i);
            cursor[i][0] = cursor[i][1] = road.arm ? road.skip + 3 : 1;
            sides.add(new int[] {i, 0});
            sides.add(new int[] {i, 1});
        }
        java.util.Random order = new java.util.Random(seed ^ 0x9E3779B9L);
        int placed = 0;
        for (UmbrerVillagePiece.Kind kind : wanted) {
            Collections.shuffle(sides, order);
            if (layout == Layout.RING) {
                // die wichtigen Häuser stehen am Ring
                sides.sort(java.util.Comparator.comparingInt(lot -> roads.get(lot[0]).arm ? 1 : 0));
            }
            boolean done = false;
            for (int[] lot : sides) {
                Road road = roads.get(lot[0]);
                Direction side = lot[1] == 0 ? road.dir.getClockWise() : road.dir.getCounterClockWise();
                for (int start = cursor[lot[0]][lot[1]]; !done && start + kind.width <= road.length(); start += 3) {
                    int t = start + kind.width / 2;
                    BlockPos front = new BlockPos(road.x(t) + side.getStepX() * (ROAD_HALF + 2), 0, road.z(t) + side.getStepZ() * (ROAD_HALF + 2));
                    if (road.bridges[t] == 0
                            && tryPlace(pieces, taken, terrain, kind, front, side.getOpposite(), road.heights[t], center, random)) {
                        cursor[lot[0]][lot[1]] = start + kind.width + 2;
                        placed++;
                        done = true;
                    }
                }
                if (done) {
                    break;
                }
            }
        }
        if (placed < 6) {
            return reject("few:" + placed);
        }
        if (debug != null) {
            debug.accept("umbrer village (" + layout.name().toLowerCase(java.util.Locale.ROOT) + ") at " + center + ": " + placed + " buildings");
        }
        return pieces;
    }

    /** Die Grundrisse der Umbrer-Dörfer. */
    public enum Layout {
        /** vier Wege in die Himmelsrichtungen */
        STAR,
        /** eine lange Gasse durch den Platz */
        LANE,
        /** drei Wege */
        FORK,
        /** ein Ring um den Platz, die Häuser schauen zur Mitte; zwei Wege hinaus */
        RING
    }

    /** Abstand der Ringstraße von der Platzmitte. */
    static final int RING = PLAZA_RADIUS + 3;

    /** Ein geplanter Weg: Anfang, Richtung, Höhen und Brücken je Schritt; gebaut wird ab {@code skip}. */
    private record Road(BlockPos start, Direction dir, int[] heights, int[] bridges, int skip, boolean arm) {
        int length() {
            return this.heights.length - 1;
        }

        int x(int t) {
            return this.start.getX() + this.dir.getStepX() * t;
        }

        int z(int t) {
            return this.start.getZ() + this.dir.getStepZ() * t;
        }

        /** Wegfläche: in Wegrichtung ab {@code skip} bis zum Ende, quer dazu ± ROAD_HALF. */
        BoundingBox area() {
            int padX = this.dir.getStepX() == 0 ? ROAD_HALF : 0;
            int padZ = this.dir.getStepZ() == 0 ? ROAD_HALF : 0;
            int x0 = this.x(this.skip);
            int z0 = this.z(this.skip);
            int x1 = this.x(this.length());
            int z1 = this.z(this.length());
            return new BoundingBox(Math.min(x0, x1) - padX, 0, Math.min(z0, z1) - padZ, Math.max(x0, x1) + padX, 0, Math.max(z0, z1) + padZ);
        }
    }

    /** Ein Weg vom Platz hinaus: höchstens eine Stufe je Block, über Wasser und tiefe Senken als Brücke. */
    private static Road trace(Terrain terrain, BlockPos center, Direction dir, int length, int skip, int floor, boolean arm) {
        int[] road = new int[length + 1];
        int[] bridges = new int[length + 1];
        int previous = floor;
        for (int t = 0; t <= length; t++) {
            int x = center.getX() + dir.getStepX() * t;
            int z = center.getZ() + dir.getStepZ() * t;
            boolean water = terrain.water(x, z);
            int target = water ? Math.max(terrain.surface(x, z), terrain.seaLevel() + 1) : terrain.ground(x, z);
            if (t <= skip) {
                target = floor;
            }
            int y = Math.max(previous - 1, Math.min(previous + 1, target));
            road[t] = y;
            bridges[t] = t > skip && (water || y > terrain.ground(x, z) + 2) ? 1 : 0;
            previous = y;
        }
        return new Road(center, dir, road, bridges, skip, arm);
    }

    /** Ein ebenes Stück des Rings (ohne Wasser, das Gelände höchstens drei Blöcke höher oder tiefer), sonst {@code null}. */
    private static @org.jspecify.annotations.Nullable Road flatRoad(Terrain terrain, BlockPos start, Direction dir, int length, int y) {
        int[] heights = new int[length + 1];
        for (int t = 0; t <= length; t++) {
            int x = start.getX() + dir.getStepX() * t;
            int z = start.getZ() + dir.getStepZ() * t;
            if (terrain.water(x, z) || Math.abs(terrain.ground(x, z) - y) > 3) {
                return null;
            }
            heights[t] = y;
        }
        return new Road(start.atY(y), dir, heights, new int[length + 1], 0, false);
    }

    /**
     * Versucht, ein Gebäude so zu setzen, dass seine Vorderseite (Mitte bei {@code front}) zur Straße ({@code facing})
     * zeigt. Der Fußboden liegt höchstens eine Stufe über oder unter dem Weg ({@code roadY}) davor. Gibt {@code false}
     * zurück, wenn es sich mit etwas überschneidet, im Wasser stünde oder zu steil liegt.
     */
    private static boolean tryPlace(List<UmbrerVillagePiece> pieces, List<BoundingBox> taken, Terrain terrain, UmbrerVillagePiece.Kind kind,
            BlockPos front, Direction facing, int roadY, BlockPos center, RandomSource random) {
        Rotation rotation = switch (facing) {
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
        int w = kind.width;
        int d = kind.depth;
        BlockPos origin = front.subtract(new BlockPos(w / 2, 0, 0).rotate(rotation));
        BoundingBox rect = UmbrerVillagePiece.footprint(origin, rotation, w, d, 0, 0);
        BoundingBox padded = rect.inflatedBy(1, 0, 1);
        for (BoundingBox other : taken) {
            if (padded.intersects(other)) {
                if (debug != null) {
                    debug.accept(kind + " overlap " + other);
                }
                return false;
            }
        }
        // Höhe: Mittel aus den Ecken und der Mitte; kein Wasser, nicht zu steil
        int[] xs = {rect.minX(), rect.maxX(), rect.minX(), rect.maxX(), (rect.minX() + rect.maxX()) / 2};
        int[] zs = {rect.minZ(), rect.minZ(), rect.maxZ(), rect.maxZ(), (rect.minZ() + rect.maxZ()) / 2};
        int sum = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int k = 0; k < xs.length; k++) {
            if (terrain.water(xs[k], zs[k])) {
                if (debug != null) {
                    debug.accept(kind + " water");
                }
                return false;
            }
            int g = terrain.ground(xs[k], zs[k]);
            sum += g;
            min = Math.min(min, g);
            max = Math.max(max, g);
        }
        if (max - min > 7) {
            if (debug != null) {
                debug.accept(kind + " steep " + (max - min));
            }
            return false;
        }
        int floor = Math.clamp(Math.round(sum / (float) xs.length), roadY - 1, roadY + 1);
        pieces.add(UmbrerVillagePiece.building(kind, origin.atY(floor), rotation, random.nextLong(), center, floor - roadY));
        taken.add(rect);
        return true;
    }
}
