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
        private final Structure.GenerationContext context;
        private final Map<Long, int[]> cache = new HashMap<>();

        Terrain(Structure.GenerationContext context) {
            this.context = context;
        }

        private int[] column(int x, int z) {
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
            return this.context.chunkGenerator().getSeaLevel();
        }
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
        pieces.add(UmbrerVillagePiece.plaza(center, PLAZA_RADIUS, random.nextLong()));
        taken.add(new BoundingBox(cx - PLAZA_RADIUS - 1, 0, cz - PLAZA_RADIUS - 1, cx + PLAZA_RADIUS + 1, 0, cz + PLAZA_RADIUS + 1));

        // Wege in die vier Himmelsrichtungen (Länge je Weg leicht verschieden)
        Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        int[] lengths = new int[4];
        int[][] roads = new int[4][];
        for (int i = 0; i < 4; i++) {
            Direction dir = directions[i];
            int length = 30 + random.nextInt(6);
            int[] road = new int[length + 1];
            int[] bridges = new int[length + 1];
            int previous = floor;
            for (int t = 0; t <= length; t++) {
                int x = cx + dir.getStepX() * t;
                int z = cz + dir.getStepZ() * t;
                boolean water = terrain.water(x, z);
                int target = water ? Math.max(terrain.surface(x, z), terrain.seaLevel() + 1) : terrain.ground(x, z);
                if (t <= PLAZA_RADIUS) {
                    target = floor;
                }
                // höchstens eine Stufe je Block
                int y = Math.max(previous - 1, Math.min(previous + 1, target));
                road[t] = y;
                // über Wasser und tiefe Senken führt eine Brücke
                bridges[t] = t > PLAZA_RADIUS && (water || y > terrain.ground(x, z) + 2) ? 1 : 0;
                previous = y;
            }
            lengths[i] = length;
            roads[i] = road;
            pieces.add(UmbrerVillagePiece.road(center, dir, road, bridges, random.nextLong()));
            // Wegfläche: in Wegrichtung bis zum Ende, quer dazu ± ROAD_HALF
            int padX = dir.getStepX() == 0 ? ROAD_HALF : 0;
            int padZ = dir.getStepZ() == 0 ? ROAD_HALF : 0;
            int minX = Math.min(cx, cx + dir.getStepX() * length) - padX;
            int maxX = Math.max(cx, cx + dir.getStepX() * length) + padX;
            int minZ = Math.min(cz, cz + dir.getStepZ() * length) - padZ;
            int maxZ = Math.max(cz, cz + dir.getStepZ() * length) + padZ;
            taken.add(new BoundingBox(minX, 0, minZ, maxX, 0, maxZ));
        }

        // Wachtürme an (bis zu) zwei Wegenden
        List<Integer> roadOrder = new ArrayList<>(List.of(0, 1, 2, 3));
        Collections.shuffle(roadOrder, new java.util.Random(seed));
        int towers = 0;
        for (int k = 0; k < 4 && towers < 2; k++) {
            int i = roadOrder.get(k);
            Direction dir = directions[i];
            // ein Block Abstand zum Wegende: dort liegt der Zugang (sonst überschneidet sich der Turm mit seinem eigenen Weg)
            BlockPos front = new BlockPos(cx + dir.getStepX() * (lengths[i] + 2), 0, cz + dir.getStepZ() * (lengths[i] + 2));
            if (tryPlace(pieces, taken, terrain, UmbrerVillagePiece.Kind.WATCHTOWER, front, dir.getOpposite(), roads[i][lengths[i]], center,
                    random)) {
                towers++;
            }
        }

        // Häuser an den Wegen: erst die Pflicht-Gebäude, dann was noch Platz hat
        List<UmbrerVillagePiece.Kind> wanted = new ArrayList<>(List.of(UmbrerVillagePiece.Kind.SEER_TOWER, UmbrerVillagePiece.Kind.SMITHY,
                UmbrerVillagePiece.Kind.HERB_HUT, UmbrerVillagePiece.Kind.CRYSTAL_WORKSHOP, UmbrerVillagePiece.Kind.FARM,
                UmbrerVillagePiece.Kind.HOUSE, UmbrerVillagePiece.Kind.LONGHOUSE, UmbrerVillagePiece.Kind.FARM,
                UmbrerVillagePiece.Kind.CRYSTAL_GARDEN, UmbrerVillagePiece.Kind.HOUSE, UmbrerVillagePiece.Kind.HOUSE,
                UmbrerVillagePiece.Kind.FARM, UmbrerVillagePiece.Kind.LONGHOUSE, UmbrerVillagePiece.Kind.HOUSE));
        // Bauplätze: an jeder Wegseite reihen sich die Gebäude vom Platz nach außen aneinander (2 Blöcke Abstand);
        // wo Wasser oder ein Hang stört, rückt der Platz ein Stück weiter
        int[][] cursor = new int[4][2];
        for (int[] row : cursor) {
            java.util.Arrays.fill(row, PLAZA_RADIUS + 3);
        }
        List<int[]> sides = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            sides.add(new int[] {i, 0});
            sides.add(new int[] {i, 1});
        }
        java.util.Random order = new java.util.Random(seed ^ 0x9E3779B9L);
        int placed = 0;
        for (UmbrerVillagePiece.Kind kind : wanted) {
            Collections.shuffle(sides, order);
            boolean done = false;
            for (int[] lot : sides) {
                int i = lot[0];
                Direction dir = directions[i];
                Direction side = lot[1] == 0 ? dir.getClockWise() : dir.getCounterClockWise();
                for (int start = cursor[i][lot[1]]; !done && start + kind.width <= lengths[i]; start += 3) {
                    int t = start + kind.width / 2;
                    BlockPos front = new BlockPos(cx + dir.getStepX() * t + side.getStepX() * (ROAD_HALF + 2), 0,
                            cz + dir.getStepZ() * t + side.getStepZ() * (ROAD_HALF + 2));
                    if (tryPlace(pieces, taken, terrain, kind, front, side.getOpposite(), roads[i][t], center, random)) {
                        cursor[i][lot[1]] = start + kind.width + 2;
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
        return pieces;
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
