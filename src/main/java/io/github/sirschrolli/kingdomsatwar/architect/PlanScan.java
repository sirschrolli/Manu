package io.github.sirschrolli.kingdomsatwar.architect;

import io.github.sirschrolli.kingdomsatwar.economy.town.Blueprint;
import io.github.sirschrolli.kingdomsatwar.economy.town.Material;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ein vermessenes Gebäude: alle Blöcke eines Quaders, so gedreht, dass die Vorderseite (mit der Haupttür) nach −z zeigt
 * wie bei den Bauplänen der Siedlung, und so beschnitten, dass kein leerer Rand bleibt. Unter der <i>Bodenlinie</i>
 * liegt das Gelände: dort zählt gewachsener Boden nicht zum Plan (er bleibt am neuen Bauplatz, wie er ist), Luft wird
 * ausgehoben (Keller) und alles andere gebaut (Fundamente, Kellerwände).
 *
 * <p>Gitter: x läuft nach rechts, z nach hinten, y nach oben; Index {@code (y * sizeZ + z) * sizeX + x}. Im Bauplan
 * liegt die Mitte der Grundfläche bei x = z = 0 und die Bodenlinie bei y = 0 (wie bei den Häusern der Siedlung).
 */
public final class PlanScan {
    /** Größte Breite und Tiefe eines Bauplans. */
    public static final int MAX_WIDTH = 96;
    /** Größte Höhe eines Bauplans. */
    public static final int MAX_HEIGHT = 64;
    /** So viele Lagen sucht die Bodenlinie von unten (darüber beginnt immer das Gebäude). */
    public static final int MAX_GROUND = 12;

    public final int sizeX;
    public final int sizeY;
    public final int sizeZ;
    /** Wie viele Lagen unter der Bodenlinie liegen (Bauplan-y = Lage − ground). */
    public final int ground;
    /** Farben der Blöcke; {@code palette[0] == null}: nichts tun (gewachsener Boden unter der Bodenlinie). */
    private final BlockState[] palette;
    private final int[] data;
    /** Daten von Schildern und Bannern (Index im Gitter → gespeicherte Daten). */
    private final Map<Integer, CompoundTag> blockEntities;
    /** Was beim Vermessen weggelassen wurde (nur zur Anzeige). */
    private final Map<Block, Integer> skipped;

    private PlanScan(int sizeX, int sizeY, int sizeZ, int ground, BlockState[] palette, int[] data, Map<Integer, CompoundTag> blockEntities,
            Map<Block, Integer> skipped) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.ground = ground;
        this.palette = palette;
        this.data = data;
        this.blockEntities = blockEntities;
        this.skipped = skipped;
    }

    // =====================================================================================
    // Vermessen
    // =====================================================================================

    /** Ist ein Quader zwischen zwei Ecken klein genug für einen Bauplan? */
    public static boolean fits(BlockPos a, BlockPos b) {
        int x = Math.abs(a.getX() - b.getX()) + 1;
        int y = Math.abs(a.getY() - b.getY()) + 1;
        int z = Math.abs(a.getZ() - b.getZ()) + 1;
        return Math.max(x, z) <= MAX_WIDTH && y <= MAX_HEIGHT;
    }

    /** Die Seite des Quaders, auf der ein Betrachter steht (für die Vorderseite, wenn es keine Tür gibt). */
    public static Direction sideFacing(BlockPos a, BlockPos b, Vec3 viewer) {
        double dx = viewer.x - (Math.min(a.getX(), b.getX()) + Math.max(a.getX(), b.getX()) + 1) / 2.0;
        double dz = viewer.z - (Math.min(a.getZ(), b.getZ()) + Math.max(a.getZ(), b.getZ()) + 1) / 2.0;
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        }
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    /**
     * Die Bodenlinie, die das Vermessen von selbst wählen würde (Zahl der Lagen unter ihr): wo rings um den Bereich das
     * Gelände aufhört (die übliche Höhe der Oberfläche direkt neben dem Gebäude) – so liegt auch ein Fundament, das unter
     * dem Haus das Gras ersetzt hat, richtig unter der Linie. Gibt es ringsum kein Gelände (etwa in einer Schematik), zählen
     * die Lagen gewachsenen Bodens von unten im Bereich selbst: eine Lage, die zur Hälfte aus Gelände besteht oder deren
     * Rand es zur Hälfte aus Erde, Gras oder Sand ist (das Fundament in der Mitte, rundum Gras; Stein am Rand zählt da
     * nicht, er kann auch eine Mauer sein).
     */
    public static int autoGround(BlockGetter level, BlockPos a, BlockPos b) {
        BlockPos min = BlockPos.min(a, b);
        BlockPos max = BlockPos.max(a, b);
        int layers = max.getY() - min.getY() + 1;
        int outside = surroundingSurface(level, min, max);
        if (outside != Integer.MIN_VALUE) {
            return Math.max(0, Math.min(Math.min(MAX_GROUND, layers - 1), outside + 1 - min.getY()));
        }
        int area = (max.getX() - min.getX() + 1) * (max.getZ() - min.getZ() + 1);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int ground = 0;
        while (ground < Math.min(MAX_GROUND, layers - 1)) {
            int terrain = 0;
            int ring = 0;
            int ringTerrain = 0;
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                for (int x = min.getX(); x <= max.getX(); x++) {
                    BlockState state = level.getBlockState(pos.set(x, min.getY() + ground, z));
                    if (BuildRules.terrain(state)) {
                        terrain++;
                    }
                    if (x == min.getX() || x == max.getX() || z == min.getZ() || z == max.getZ()) {
                        ring++;
                        if (soil(state)) {
                            ringTerrain++;
                        }
                    }
                }
            }
            if (terrain * 2 < area && ringTerrain * 2 < ring) {
                break;
            }
            ground++;
        }
        return ground;
    }

    /** Erde, Gras, Sand, Kies, Schnee, Lehm, Schlamm: lockerer Boden, aus dem kaum jemand Mauern baut. */
    private static boolean soil(BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.SUBSTRATE_OVERWORLD) || state.is(net.minecraft.tags.BlockTags.SAND) || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.CLAY) || state.is(Blocks.MUD) || state.is(Blocks.DIRT_PATH) || state.is(Blocks.FARMLAND);
    }

    /**
     * Die übliche Höhe des Geländes rings um den Bereich (oberster Block gewachsenen Bodens in den Spalten direkt neben
     * ihm, der Median), oder {@link Integer#MIN_VALUE}, wenn dort kein Gelände ist.
     */
    private static int surroundingSurface(BlockGetter level, BlockPos min, BlockPos max) {
        List<Integer> heights = new ArrayList<>();
        int step = Math.max(1, (max.getX() - min.getX() + max.getZ() - min.getZ()) / 48);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = min.getX() - 1; x <= max.getX() + 1; x += step) {
            surface(level, pos, x, min.getZ() - 1, min, max, heights);
            surface(level, pos, x, max.getZ() + 1, min, max, heights);
        }
        for (int z = min.getZ(); z <= max.getZ(); z += step) {
            surface(level, pos, min.getX() - 1, z, min, max, heights);
            surface(level, pos, max.getX() + 1, z, min, max, heights);
        }
        if (heights.size() < 3) {
            return Integer.MIN_VALUE;
        }
        heights.sort(Integer::compare);
        return heights.get(heights.size() / 2);
    }

    /** Oberster Geländeblock einer Spalte zwischen Ober- und Unterkante des Bereichs (Luft, Pflanzen und Schnee darüber zählen nicht). */
    private static void surface(BlockGetter level, BlockPos.MutableBlockPos pos, int x, int z, BlockPos min, BlockPos max, List<Integer> out) {
        for (int y = max.getY(); y >= min.getY() - 1; y--) {
            BlockState state = level.getBlockState(pos.set(x, y, z));
            if (state.isAir() || state.canBeReplaced() && !state.liquid() || state.getBlock() instanceof net.minecraft.world.level.block.VegetationBlock) {
                continue;
            }
            if (BuildRules.terrain(state)) {
                out.add(y);
            }
            return; // ein anderer Block (Mauer, Weg, Zaun): diese Spalte zählt nicht
        }
    }

    /**
     * Die Seite mit der Haupttür: die Tür, die am nächsten am Rand des Quaders steht (bei Gleichstand die Seite zum
     * Betrachter hin); {@code null}, wenn es über der Bodenlinie keine Tür gibt.
     */
    public static @Nullable Direction doorSide(BlockGetter level, BlockPos a, BlockPos b, int ground, @Nullable Vec3 viewer) {
        BlockPos min = BlockPos.min(a, b);
        BlockPos max = BlockPos.max(a, b);
        Direction toViewer = viewer == null ? null : sideFacing(a, b, viewer);
        Direction best = null;
        int bestDist = Integer.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(min.getX(), min.getY() + ground, min.getZ(), max.getX(), max.getY(), max.getZ())) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof DoorBlock) || state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.LOWER) {
                continue;
            }
            int[] dist = {pos.getZ() - min.getZ(), max.getX() - pos.getX(), max.getZ() - pos.getZ(), pos.getX() - min.getX()};
            Direction[] sides = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
            for (int i = 0; i < 4; i++) {
                if (dist[i] < bestDist || dist[i] == bestDist && sides[i] == toViewer) {
                    bestDist = dist[i];
                    best = sides[i];
                }
            }
        }
        return best;
    }

    /**
     * Vermisst den Quader zwischen zwei Ecken.
     *
     * @param ground     Zahl der Lagen unter der Bodenlinie (siehe {@link #autoGround})
     * @param front      Seite des Quaders, die im Bauplan vorn liegt (dort ist der Eingang)
     * @param registries für die Daten von Schildern und Bannern ({@code null}: ohne)
     */
    public static PlanScan scan(BlockGetter level, BlockPos a, BlockPos b, int ground, Direction front, HolderLookup.@Nullable Provider registries) {
        BlockPos min = BlockPos.min(a, b);
        BlockPos max = BlockPos.max(a, b);
        int rx = max.getX() - min.getX() + 1;
        int ry = max.getY() - min.getY() + 1;
        int rz = max.getZ() - min.getZ() + 1;
        if (Math.max(rx, rz) > MAX_WIDTH || ry > MAX_HEIGHT) {
            throw new IllegalArgumentException("too large");
        }
        ground = Math.max(0, Math.min(ground, ry - 1));
        Rotation rotation = switch (front) {
            case EAST -> Rotation.COUNTERCLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.CLOCKWISE_90;
            default -> Rotation.NONE;
        };
        boolean swap = front == Direction.EAST || front == Direction.WEST;
        int nx = swap ? rz : rx;
        int nz = swap ? rx : rz;
        BlockState[] grid = new BlockState[nx * ry * nz];
        Map<Integer, CompoundTag> data = new HashMap<>();
        Map<Block, Integer> skipped = new LinkedHashMap<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 0; y < ry; y++) {
            boolean below = y < ground;
            for (int z = 0; z < rz; z++) {
                for (int x = 0; x < rx; x++) {
                    pos.set(min.getX() + x, min.getY() + y, min.getZ() + z);
                    BlockState raw = level.getBlockState(pos);
                    BlockState state = BuildRules.normalize(raw);
                    BuildRules.Kind kind = BuildRules.kind(state);
                    if (kind == BuildRules.Kind.SKIP) {
                        // fließendes Wasser und fließende Lava nicht aufzählen, nur stehende Lava und echte Blöcke
                        if ((!(raw.getBlock() instanceof net.minecraft.world.level.block.LiquidBlock) || raw.getFluidState().isSource())
                                && !raw.is(Blocks.STRUCTURE_VOID)) {
                            skipped.merge(raw.getBlock(), 1, Integer::sum);
                        }
                        state = below ? null : Blocks.AIR.defaultBlockState();
                    } else if (below && BuildRules.terrain(state)) {
                        state = null;
                    }
                    int gx;
                    int gz;
                    switch (front) {
                        case EAST -> {
                            gx = z;
                            gz = rx - 1 - x;
                        }
                        case SOUTH -> {
                            gx = rx - 1 - x;
                            gz = rz - 1 - z;
                        }
                        case WEST -> {
                            gx = rz - 1 - z;
                            gz = x;
                        }
                        default -> {
                            gx = x;
                            gz = z;
                        }
                    }
                    int index = (y * nz + gz) * nx + gx;
                    grid[index] = state == null ? null : state.rotate(rotation);
                    if (state != null && registries != null && raw.hasBlockEntity()) {
                        BlockEntity be = level.getBlockEntity(pos);
                        if (be != null && BuildRules.keepsData(be)) {
                            data.put(index, BuildRules.harmless(be.saveWithoutMetadata(registries)));
                        }
                    }
                }
            }
        }
        return trimmed(grid, nx, ry, nz, ground, data, skipped);
    }

    /** Schneidet leere Ränder ab (Luft über der Bodenlinie, Unberührtes darunter) und baut die Palette. */
    private static PlanScan trimmed(BlockState[] grid, int nx, int ny, int nz, int ground, Map<Integer, CompoundTag> data, Map<Block, Integer> skipped) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = -1;
        int maxY = -1;
        int maxZ = -1;
        for (int y = 0; y < ny; y++) {
            for (int z = 0; z < nz; z++) {
                for (int x = 0; x < nx; x++) {
                    BlockState s = grid[(y * nz + z) * nx + x];
                    if (s != null && (!s.isAir() || y < ground)) {
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        minZ = Math.min(minZ, z);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                        maxZ = Math.max(maxZ, z);
                    }
                }
            }
        }
        if (maxX < 0) {
            throw new IllegalArgumentException("empty");
        }
        // unten nur das Gelände unter der Bodenlinie abschneiden (über ihr bleibt die Höhe, wie sie ist)
        minY = Math.min(minY, ground);
        int sx = maxX - minX + 1;
        int sy = maxY - minY + 1;
        int sz = maxZ - minZ + 1;
        Map<BlockState, Integer> ids = new IdentityHashMap<>();
        List<BlockState> palette = new ArrayList<>();
        palette.add(null);
        int[] out = new int[sx * sy * sz];
        Map<Integer, CompoundTag> outData = new HashMap<>();
        for (int y = 0; y < sy; y++) {
            for (int z = 0; z < sz; z++) {
                for (int x = 0; x < sx; x++) {
                    int from = ((y + minY) * nz + (z + minZ)) * nx + (x + minX);
                    BlockState s = grid[from];
                    int to = (y * sz + z) * sx + x;
                    if (s != null) {
                        Integer id = ids.get(s);
                        if (id == null) {
                            id = palette.size();
                            ids.put(s, id);
                            palette.add(s);
                        }
                        out[to] = id;
                        CompoundTag tag = data.get(from);
                        if (tag != null) {
                            outData.put(to, tag);
                        }
                    }
                }
            }
        }
        return new PlanScan(sx, sy, sz, ground - minY, palette.toArray(new BlockState[0]), out, outData, skipped);
    }

    // =====================================================================================
    // Abfragen
    // =====================================================================================

    /** Der Block an einer Stelle des Gitters ({@code null}: nichts tun). */
    public @Nullable BlockState get(int x, int y, int z) {
        return this.palette[this.data[(y * this.sizeZ + z) * this.sizeX + x]];
    }

    /** Die Daten eines Schilds oder Banners an einer Stelle des Gitters ({@code null}: keine). */
    public @Nullable CompoundTag blockEntity(int x, int y, int z) {
        return this.blockEntities.get((y * this.sizeZ + z) * this.sizeX + x);
    }

    /** Wird an dieser Stelle etwas gebaut oder ausgehoben? */
    private boolean meaningful(int x, int y, int z) {
        BlockState s = this.get(x, y, z);
        return s != null && (!s.isAir() || y < this.ground);
    }

    /** Lokale Koordinate im Bauplan (Mitte der Grundfläche bei x = z = 0, Bodenlinie bei y = 0). */
    public BlockPos local(int x, int y, int z) {
        return new BlockPos(x - (this.sizeX - 1) / 2, y - this.ground, z - (this.sizeZ - 1) / 2);
    }

    /** Ausdehnung im Bauplan (lokal). */
    public BoundingBox localBounds() {
        return bounds(this.sizeX, this.sizeY, this.sizeZ, this.ground);
    }

    /** Ausdehnung eines Bauplans dieser Größe (lokal, mindestens bis y = 0 hinunter). */
    public static BoundingBox bounds(int sizeX, int sizeY, int sizeZ, int ground) {
        int ox = (sizeX - 1) / 2;
        int oz = (sizeZ - 1) / 2;
        return new BoundingBox(-ox, Math.min(-ground, 0), -oz, sizeX - 1 - ox, sizeY - 1 - ground, sizeZ - 1 - oz);
    }

    /** Die Haupttür (untere Hälfte) im Gitter: die vorderste, bei Gleichstand die mittigste ({@code null}: keine). */
    public @Nullable BlockPos door() {
        BlockPos best = null;
        int bestScore = Integer.MAX_VALUE;
        for (int y = this.ground; y < this.sizeY; y++) {
            for (int z = 0; z < this.sizeZ; z++) {
                for (int x = 0; x < this.sizeX; x++) {
                    BlockState s = this.get(x, y, z);
                    if (s != null && s.getBlock() instanceof DoorBlock && s.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                        int score = z * 1000 + Math.abs(2 * x - (this.sizeX - 1)) * 4 + (y - this.ground);
                        if (score < bestScore) {
                            bestScore = score;
                            best = new BlockPos(x, y, z);
                        }
                    }
                }
            }
        }
        return best;
    }

    /** Stelle vor dem Eingang (lokal, draußen vor der Vorderseite, wo man vor der Tür steht). */
    public BlockPos entrance() {
        BlockPos door = this.door();
        BlockPos front = this.local(door == null ? (this.sizeX - 1) / 2 : door.getX(), door == null ? this.ground : door.getY(), 0);
        return new BlockPos(front.getX(), Math.max(0, front.getY() - 1), front.getZ() - 1);
    }

    /** Betten (Kopfende, lokal). */
    public List<BlockPos> beds() {
        List<BlockPos> beds = new ArrayList<>();
        for (int y = 0; y < this.sizeY; y++) {
            for (int z = 0; z < this.sizeZ; z++) {
                for (int x = 0; x < this.sizeX; x++) {
                    BlockState s = this.get(x, y, z);
                    if (s != null && s.getBlock() instanceof BedBlock && s.getValue(BedBlock.PART) == BedPart.HEAD) {
                        beds.add(this.local(x, y, z));
                    }
                }
            }
        }
        return beds;
    }

    /** Was beim Vermessen weggelassen wurde (Block → Anzahl). */
    public Map<Block, Integer> skipped() {
        return this.skipped;
    }

    /** Was der Bau kostet: Blöcke, Einheiten je Baustoff, kostbare Blöcke (Gegenstand → Anzahl). */
    public record Costs(int blocks, int[] units, Map<Item, Integer> precious) {}

    public Costs costs() {
        int blocks = 0;
        int[] units = new int[Material.values().length];
        Map<Item, Integer> precious = new LinkedHashMap<>();
        for (int i = 0; i < this.data.length; i++) {
            BlockState s = this.palette[this.data[i]];
            if (s == null || s.isAir()) {
                continue;
            }
            blocks++;
            switch (BuildRules.kind(s)) {
                case MATERIAL -> {
                    Material m = Material.of(s);
                    if (m != null) {
                        units[m.ordinal()]++;
                    }
                }
                case PRECIOUS -> {
                    Item item = BuildRules.precious(s, false);
                    if (item != null) {
                        precious.merge(item, 1, Integer::sum);
                    }
                }
                default -> {
                }
            }
        }
        // Fundament: eine Einheit je Säule (wie bei den Bauplänen der Siedlung)
        Material base = Material.of(this.foundation());
        if (base != null) {
            units[base.ordinal()] += this.footprint().size();
        }
        return new Costs(blocks, units, precious);
    }

    // =====================================================================================
    // Bauplan
    // =====================================================================================

    /** Grundfläche: Säulen, deren unterster Block auf der Bodenlinie fest steht (darunter füllt das Fundament auf). */
    List<BlockPos> footprint() {
        List<BlockPos> footprint = new ArrayList<>();
        if (this.ground >= this.sizeY) {
            return footprint;
        }
        for (int z = 0; z < this.sizeZ; z++) {
            for (int x = 0; x < this.sizeX; x++) {
                BlockState s = this.get(x, this.ground, z);
                if (s == null || s.isAir() || !s.isSolidRender()) {
                    continue;
                }
                boolean basement = false;
                for (int y = 0; y < this.ground && !basement; y++) {
                    basement = this.meaningful(x, y, z);
                }
                if (!basement) {
                    footprint.add(this.local(x, this.ground, z));
                }
            }
        }
        return footprint;
    }

    /** Das Fundament: der häufigste feste Steinblock der untersten Lage, sonst Bruchstein. */
    BlockState foundation() {
        Map<BlockState, Integer> count = new HashMap<>();
        if (this.ground < this.sizeY) {
            for (int z = 0; z < this.sizeZ; z++) {
                for (int x = 0; x < this.sizeX; x++) {
                    BlockState s = this.get(x, this.ground, z);
                    if (s != null && s.isSolidRender() && s.getBlock().defaultBlockState() == s && Material.of(s) == Material.STONE
                            && BuildRules.kind(s) == BuildRules.Kind.MATERIAL) {
                        count.merge(s, 1, Integer::sum);
                    }
                }
            }
        }
        BlockState best = Blocks.COBBLESTONE.defaultBlockState();
        int most = 0;
        for (Map.Entry<BlockState, Integer> e : count.entrySet()) {
            if (e.getValue() > most) {
                most = e.getValue();
                best = e.getKey();
            }
        }
        return best;
    }

    /** Macht aus der Vermessung einen Bauplan für den Baumeister. */
    public Blueprint toBlueprint() {
        Map<BlockPos, BlockState> plan = new HashMap<>();
        Map<BlockPos, CompoundTag> tags = new HashMap<>();
        for (int y = 0; y < this.sizeY; y++) {
            for (int z = 0; z < this.sizeZ; z++) {
                for (int x = 0; x < this.sizeX; x++) {
                    int index = (y * this.sizeZ + z) * this.sizeX + x;
                    BlockState s = this.palette[this.data[index]];
                    if (s == null || s.isAir() && y >= this.ground) {
                        continue; // über der Bodenlinie räumt der Baumeister ohnehin alles frei, was nicht im Plan steht
                    }
                    BlockPos local = this.local(x, y, z);
                    plan.put(local, s);
                    CompoundTag tag = this.blockEntities.get(index);
                    if (tag != null) {
                        tags.put(local, tag);
                    }
                }
            }
        }
        return Blueprint.custom(plan, tags, this.footprint(), this.foundation(), this.beds(), this.entrance(), this.localBounds());
    }

    // =====================================================================================
    // Speichern
    // =====================================================================================

    /** Version des Dateiformats. */
    private static final int VERSION = 1;

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Version", VERSION);
        tag.putInt("DataVersion", net.minecraft.SharedConstants.getCurrentVersion().dataVersion().version());
        tag.putIntArray("Size", new int[] {this.sizeX, this.sizeY, this.sizeZ});
        tag.putInt("Ground", this.ground);
        ListTag palette = new ListTag();
        for (int i = 0; i < this.palette.length; i++) {
            palette.add(NbtUtils.writeBlockState(i == 0 || this.palette[i] == null ? Blocks.STRUCTURE_VOID.defaultBlockState() : this.palette[i]));
        }
        tag.put("Palette", palette);
        tag.put("Blocks", new IntArrayTag(this.data.clone()));
        ListTag entities = new ListTag();
        for (Map.Entry<Integer, CompoundTag> e : this.blockEntities.entrySet()) {
            CompoundTag be = new CompoundTag();
            be.putInt("Index", e.getKey());
            be.put("Data", e.getValue().copy());
            entities.add(be);
        }
        tag.put("BlockEntities", entities);
        return tag;
    }

    public static PlanScan load(CompoundTag tag) {
        int[] size = tag.getIntArray("Size").orElse(new int[] {1, 1, 1});
        int sx = Math.max(1, size[0]);
        int sy = Math.max(1, size[1]);
        int sz = Math.max(1, size[2]);
        ListTag paletteTag = tag.getListOrEmpty("Palette");
        BlockState[] palette = new BlockState[Math.max(1, paletteTag.size())];
        for (int i = 1; i < paletteTag.size(); i++) {
            palette[i] = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, paletteTag.getCompoundOrEmpty(i));
        }
        int[] data = tag.getIntArray("Blocks").orElse(new int[0]);
        if (data.length != sx * sy * sz) {
            data = java.util.Arrays.copyOf(data, sx * sy * sz);
        }
        for (int i = 0; i < data.length; i++) {
            if (data[i] < 0 || data[i] >= palette.length) {
                data[i] = 0;
            }
        }
        Map<Integer, CompoundTag> entities = new HashMap<>();
        for (Tag t : tag.getListOrEmpty("BlockEntities")) {
            if (t instanceof CompoundTag be) {
                entities.put(be.getIntOr("Index", -1), be.getCompoundOrEmpty("Data"));
            }
        }
        return new PlanScan(sx, sy, sz, Math.max(0, tag.getIntOr("Ground", 0)), palette, data, entities, Map.of());
    }

    // =====================================================================================
    // Bilder
    // =====================================================================================

    /** Ein kleines Bild in Kartenfarben (je Bildpunkt eine gepackte {@link MapColor}-Nummer, 0 = durchsichtig). */
    public record Picture(int width, int height, byte[] pixels) {
        public static final Picture EMPTY = new Picture(1, 1, new byte[1]);

        /** Farbe eines Bildpunkts (ARGB, 0 = durchsichtig). */
        public int argb(int x, int y) {
            int packed = this.pixels[y * this.width + x] & 0xFF;
            return packed < 4 ? 0 : MapColor.getColorFromPackedId(packed);
        }
    }

    private static MapColor color(@Nullable BlockState state) {
        return state == null || state.isAir() ? MapColor.NONE : state.getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }

    /**
     * Grundriss (vorn ist unten im Bild), höchstens {@code max} Bildpunkte breit und hoch: ein waagrechter Schnitt in
     * Augenhöhe über dem Erdgeschoss wie auf einer echten Bauzeichnung – Mauern, Türen und Möbel hell, darunter der
     * Fußboden dunkel; das Dach bleibt weg.
     */
    public Picture top(int max) {
        int step = Math.max(1, Math.max((this.sizeX + max - 1) / max, (this.sizeZ + max - 1) / max));
        int w = (this.sizeX + step - 1) / step;
        int h = (this.sizeZ + step - 1) / step;
        int section = Math.min(this.sizeY - 1, this.ground + 1);
        byte[] pixels = new byte[w * h];
        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                int x = Math.min(this.sizeX - 1, px * step);
                int z = Math.min(this.sizeZ - 1, (h - 1 - py) * step);
                for (int y = section; y >= 0; y--) {
                    MapColor c = color(this.get(x, y, z));
                    if (c != MapColor.NONE) {
                        MapColor.Brightness b = y == section ? MapColor.Brightness.HIGH
                                : y == section - 1 ? MapColor.Brightness.LOW : MapColor.Brightness.LOWEST;
                        pixels[py * w + px] = c.getPackedId(b);
                        break;
                    }
                }
            }
        }
        return new Picture(w, h, pixels);
    }

    /** Ansicht von vorn (unten im Bild die unterste Lage), höchstens {@code max} Bildpunkte breit und hoch. */
    public Picture front(int max) {
        int step = Math.max(1, Math.max((this.sizeX + max - 1) / max, (this.sizeY + max - 1) / max));
        int w = (this.sizeX + step - 1) / step;
        int h = (this.sizeY + step - 1) / step;
        byte[] pixels = new byte[w * h];
        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                int x = Math.min(this.sizeX - 1, px * step);
                int y = Math.min(this.sizeY - 1, (h - 1 - py) * step);
                for (int z = 0; z < this.sizeZ; z++) {
                    MapColor c = color(this.get(x, y, z));
                    if (c != MapColor.NONE) {
                        MapColor.Brightness b = z == 0 ? MapColor.Brightness.HIGH : z <= 2 ? MapColor.Brightness.NORMAL
                                : z <= 5 ? MapColor.Brightness.LOW : MapColor.Brightness.LOWEST;
                        pixels[py * w + px] = c.getPackedId(b);
                        break;
                    }
                }
            }
        }
        return new Picture(w, h, pixels);
    }
}
