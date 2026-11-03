package io.github.sirschrolli.kingdomsatwar.economy.town;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jspecify.annotations.Nullable;

/**
 * Bauplan eines Gebäudes der Siedlung: alle Blöcke in Bau-Reihenfolge (zuerst die festen Blöcke von unten nach oben,
 * danach Türen, Betten, Laternen, Pflanzen und anderes Angehängte), dazu die Grundfläche für das Fundament und die
 * besonderen Stellen (Betten, Arbeitstruhe, Arbeitsplatz, Eingang).
 *
 * <p>Lokale Koordinaten: x nach rechts, y nach oben (0 = erste Lage über dem Gelände), z nach hinten; die Vorderseite mit
 * der Tür zeigt nach −z (Norden). In der Welt wird der Plan um seinen Ursprung gedreht ({@link #world}).
 */
public final class Blueprint {
    /** Ein Block des Plans. */
    public record Entry(BlockPos pos, BlockState state) {}

    /**
     * Ein Bauschritt in der Welt: einen Block setzen (Luft = wegräumen) oder eine Fundamentsäule auffüllen; {@code data}
     * sind die Daten eines Schilds oder Banners aus einem vermessenen Gebäude (sonst {@code null}).
     */
    public record Step(BlockPos pos, BlockState state, boolean foundation, net.minecraft.nbt.@Nullable CompoundTag data) {
        public Step(BlockPos pos, BlockState state, boolean foundation) {
            this(pos, state, foundation, null);
        }
    }

    private final List<Entry> blocks;
    private final BoundingBox bounds;
    private final List<BlockPos> footprint;
    private final BlockState foundation;
    private final List<BlockPos> beds;
    private final @Nullable BlockPos chest;
    private final @Nullable BlockPos workSpot;
    private final BlockPos entrance;
    private final List<BlockPos> marks;
    private final Map<String, BlockPos> anchors;
    /** Daten von Schildern und Bannern (lokal; nur bei vermessenen Gebäuden). */
    private final Map<BlockPos, net.minecraft.nbt.CompoundTag> data;

    /**
     * Ein Bauplan aus einem vermessenen Gebäude (Dragon Architect): mit den Daten der Schilder und Banner und der
     * vermessenen Ausdehnung (auch wenn der Rand nur aus Luft besteht).
     */
    public static Blueprint custom(Map<BlockPos, BlockState> plan, Map<BlockPos, net.minecraft.nbt.CompoundTag> data, List<BlockPos> footprint,
            BlockState foundation, List<BlockPos> beds, BlockPos entrance, BoundingBox bounds) {
        return new Blueprint(plan, footprint, foundation, beds, null, null, entrance, List.of(), Map.of(), data, bounds);
    }

    Blueprint(Map<BlockPos, BlockState> plan, List<BlockPos> footprint, BlockState foundation, List<BlockPos> beds, @Nullable BlockPos chest,
            @Nullable BlockPos workSpot, BlockPos entrance, List<BlockPos> marks, Map<String, BlockPos> anchors) {
        this(plan, footprint, foundation, beds, chest, workSpot, entrance, marks, anchors, Map.of(), null);
    }

    private Blueprint(Map<BlockPos, BlockState> plan, List<BlockPos> footprint, BlockState foundation, List<BlockPos> beds, @Nullable BlockPos chest,
            @Nullable BlockPos workSpot, BlockPos entrance, List<BlockPos> marks, Map<String, BlockPos> anchors,
            Map<BlockPos, net.minecraft.nbt.CompoundTag> data, @Nullable BoundingBox fixedBounds) {
        List<Entry> solid = new ArrayList<>();
        List<Entry> attached = new ArrayList<>();
        List<Entry> dig = new ArrayList<>();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (Map.Entry<BlockPos, BlockState> e : plan.entrySet()) {
            BlockPos p = e.getKey();
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
            maxZ = Math.max(maxZ, p.getZ());
            if (fixedBounds != null && e.getValue().isAir() && p.getY() < 0) {
                dig.add(new Entry(p, e.getValue())); // Keller eines vermessenen Gebäudes
            } else {
                (attached(e.getValue()) ? attached : solid).add(new Entry(p, e.getValue()));
            }
        }
        // Lage für Lage von unten nach oben, innerhalb der Lage immer zum nächstgelegenen Block: so zieht der Baumeister
        // die Mauern im Kreis hoch, statt zwischen den Seiten hin und her zu laufen (einen Keller hebt er vorher von oben
        // nach unten aus)
        BlockPos corner = new BlockPos(minX, 0, minZ);
        List<Entry> all = new ArrayList<>();
        if (!dig.isEmpty()) {
            List<Entry> down = new ArrayList<>(sweep(dig, Entry::pos, corner));
            java.util.Map<Integer, List<Entry>> byLayer = new java.util.TreeMap<>(Comparator.reverseOrder());
            for (Entry e : down) {
                byLayer.computeIfAbsent(e.pos().getY(), y -> new ArrayList<>()).add(e);
            }
            byLayer.values().forEach(all::addAll);
        }
        all.addAll(sweep(solid, Entry::pos, all.isEmpty() ? corner : all.getLast().pos()));
        all.addAll(sweep(attached, Entry::pos, all.isEmpty() ? corner : all.getLast().pos()));
        this.blocks = Collections.unmodifiableList(all);
        this.bounds = fixedBounds != null ? fixedBounds
                : plan.isEmpty() ? new BoundingBox(0, 0, 0, 0, 0, 0) : new BoundingBox(minX, Math.min(minY, 0), minZ, maxX, maxY, maxZ);
        this.data = Map.copyOf(data);
        this.footprint = List.copyOf(sweep(new ArrayList<>(footprint), p -> p, corner));
        this.foundation = foundation;
        this.beds = List.copyOf(beds);
        this.chest = chest;
        this.workSpot = workSpot;
        this.entrance = entrance;
        this.marks = List.copyOf(marks);
        this.anchors = Map.copyOf(anchors);
    }

    /**
     * Ordnet Stellen Lage für Lage (y aufsteigend); innerhalb einer Lage folgt auf jede Stelle die nächstgelegene noch
     * offene (bei Gleichstand die vordere, dann die linke). Jede Lage beginnt dort, wo die vorige aufgehört hat.
     */
    static <T> List<T> sweep(List<T> items, java.util.function.Function<T, BlockPos> pos, BlockPos start) {
        java.util.TreeMap<Integer, List<T>> layers = new java.util.TreeMap<>();
        for (T item : items) {
            layers.computeIfAbsent(pos.apply(item).getY(), y -> new ArrayList<>()).add(item);
        }
        List<T> out = new ArrayList<>(items.size());
        int cx = start.getX();
        int cz = start.getZ();
        for (List<T> layer : layers.values()) {
            layer.sort(Comparator.<T>comparingInt(i -> pos.apply(i).getZ()).thenComparingInt(i -> pos.apply(i).getX()));
            if (layer.size() > LARGE_LAYER) {
                // große Lagen (vermessene Burgen): dieselbe Reihenfolge, aber mit Suche in Ringen statt über alle Blöcke
                long last = sweepLarge(layer, pos, cx, cz, out);
                cx = (int) (last >> 32);
                cz = (int) last;
                continue;
            }
            boolean[] used = new boolean[layer.size()];
            for (int n = 0; n < layer.size(); n++) {
                int best = -1;
                int bestDist = Integer.MAX_VALUE;
                for (int i = 0; i < layer.size(); i++) {
                    if (!used[i]) {
                        BlockPos p = pos.apply(layer.get(i));
                        int d = Math.abs(p.getX() - cx) + Math.abs(p.getZ() - cz);
                        if (d < bestDist) {
                            bestDist = d;
                            best = i;
                        }
                    }
                }
                used[best] = true;
                T item = layer.get(best);
                out.add(item);
                cx = pos.apply(item).getX();
                cz = pos.apply(item).getZ();
            }
        }
        return out;
    }

    /** Ab so vielen Blöcken in einer Lage sucht {@link #sweep} in Ringen (die Pläne der Siedlung bleiben darunter). */
    private static final int LARGE_LAYER = 1200;

    /**
     * Wie die Suche in {@link #sweep}, aber für große Lagen: die Blöcke liegen in einem Raster, gesucht wird in Ringen
     * wachsender Entfernung (bei Gleichstand die vordere, dann die linke Stelle – wie oben). Gibt die letzte Stelle zurück
     * (x in den oberen, z in den unteren 32 Bit).
     */
    private static <T> long sweepLarge(List<T> layer, java.util.function.Function<T, BlockPos> pos, int cx, int cz, List<T> out) {
        int minX = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (T item : layer) {
            BlockPos p = pos.apply(item);
            minX = Math.min(minX, p.getX());
            minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX());
            maxZ = Math.max(maxZ, p.getZ());
        }
        int w = maxX - minX + 1;
        int d = maxZ - minZ + 1;
        int[] cell = new int[w * d];
        java.util.Arrays.fill(cell, -1);
        for (int i = 0; i < layer.size(); i++) {
            BlockPos p = pos.apply(layer.get(i));
            int c = (p.getZ() - minZ) * w + (p.getX() - minX);
            if (cell[c] < 0) {
                cell[c] = i;
            } else {
                out.add(layer.get(i)); // doppelt (sollte nicht vorkommen)
            }
        }
        int left = 0;
        for (int c : cell) {
            if (c >= 0) {
                left++;
            }
        }
        int maxR = w + d + Math.abs(cx - minX) + Math.abs(cz - minZ) + 2;
        while (left > 0) {
            int found = -1;
            for (int r = 0; r <= maxR && found < 0; r++) {
                for (int dz = -r; dz <= r && found < 0; dz++) {
                    int z = cz + dz - minZ;
                    if (z < 0 || z >= d) {
                        continue;
                    }
                    int rest = r - Math.abs(dz);
                    int x1 = cx - rest - minX;
                    int x2 = cx + rest - minX;
                    if (x1 >= 0 && x1 < w && cell[z * w + x1] >= 0) {
                        found = z * w + x1;
                    } else if (rest > 0 && x2 >= 0 && x2 < w && cell[z * w + x2] >= 0) {
                        found = z * w + x2;
                    }
                }
            }
            if (found < 0) {
                break;
            }
            T item = layer.get(cell[found]);
            cell[found] = -1;
            left--;
            out.add(item);
            cx = pos.apply(item).getX();
            cz = pos.apply(item).getZ();
        }
        return ((long) cx << 32) | (cz & 0xFFFFFFFFL);
    }

    /** Türen, Betten, Laternen, Fackeln, Leitern, Pflanzen, Teppiche … – alles, was an anderen Blöcken hängt. */
    static boolean attached(BlockState state) {
        Block block = state.getBlock();
        return !state.isAir() && (block instanceof DoorBlock || block instanceof BedBlock
                || block instanceof net.minecraft.world.level.block.TrapDoorBlock || block instanceof net.minecraft.world.level.block.LanternBlock
                || block instanceof net.minecraft.world.level.block.LadderBlock || block instanceof net.minecraft.world.level.block.BaseTorchBlock
                || block instanceof net.minecraft.world.level.block.CropBlock || block instanceof net.minecraft.world.level.block.VegetationBlock
                || block instanceof net.minecraft.world.level.block.CarpetBlock || block instanceof net.minecraft.world.level.block.FlowerPotBlock
                || block instanceof net.minecraft.world.level.block.ChainBlock || block instanceof net.minecraft.world.level.block.BellBlock
                || block instanceof io.github.sirschrolli.kingdomsatwar.block.ChandelierBlock
                || block instanceof net.minecraft.world.level.block.WallBannerBlock || block instanceof net.minecraft.world.level.block.BannerBlock);
    }

    public List<Entry> blocks() {
        return this.blocks;
    }

    private int @Nullable [] costs;

    /**
     * Was der Plan an Baustoffen kostet: eine Einheit je Block (siehe {@link Material#of(BlockState)}) und eine Einheit
     * Stein je Säule des Fundaments.
     */
    public int[] costs() {
        if (this.costs == null) {
            int[] c = new int[Material.values().length];
            for (Entry e : this.blocks) {
                Material m = Material.of(e.state());
                if (m != null) {
                    c[m.ordinal()]++;
                }
            }
            Material base = Material.of(this.foundation);
            if (base != null) {
                c[base.ordinal()] += this.footprint.size();
            }
            this.costs = c;
        }
        return this.costs.clone();
    }

    /** Was ein Bauschritt kostet ({@code null}: nichts, z. B. Wegräumen oder Pflanzen). */
    public static @Nullable Material cost(Step step) {
        return Material.of(step.state());
    }

    /** Ausdehnung des Plans (lokal, mit dem Boden ab y = 0). */
    public BoundingBox bounds() {
        return this.bounds;
    }

    public List<BlockPos> footprint() {
        return this.footprint;
    }

    public List<BlockPos> beds() {
        return this.beds;
    }

    public @Nullable BlockPos chest() {
        return this.chest;
    }

    public @Nullable BlockPos workSpot() {
        return this.workSpot;
    }

    /** Stelle vor der Tür (draußen). */
    public BlockPos entrance() {
        return this.entrance;
    }

    /** Weitere markierte Stellen (z. B. Arbeitsplätze am Markt). */
    public List<BlockPos> marks() {
        return this.marks;
    }

    /** Eine benannte Stelle (z. B. "shaft_top" der Mine, "anvil" der Schmiede) oder {@code fallback}. */
    public BlockPos anchor(String name, BlockPos fallback) {
        return this.anchors.getOrDefault(name, fallback);
    }

    // =====================================================================================
    // In der Welt
    // =====================================================================================

    public static BlockPos world(BlockPos origin, Rotation rotation, BlockPos local) {
        return origin.offset(local.rotate(rotation));
    }

    public static BlockState world(BlockState state, Rotation rotation) {
        return state.rotate(rotation);
    }

    /** Ausdehnung in der Welt (mit einem Block Rand). */
    public BoundingBox worldBounds(BlockPos origin, Rotation rotation) {
        BlockPos a = world(origin, rotation, new BlockPos(this.bounds.minX(), this.bounds.minY(), this.bounds.minZ()));
        BlockPos b = world(origin, rotation, new BlockPos(this.bounds.maxX(), this.bounds.maxY(), this.bounds.maxZ()));
        return BoundingBox.fromCorners(a, b);
    }

    /**
     * Alle Bauschritte in der Welt: erst wegräumen, was im Weg steht (von oben nach unten), dann das Fundament unter der
     * Grundfläche, dann der Plan selbst.
     */
    public List<Step> steps(BlockPos origin, Rotation rotation) {
        List<Step> steps = new ArrayList<>();
        java.util.Set<BlockPos> planned = new java.util.HashSet<>();
        for (Entry e : this.blocks) {
            planned.add(e.pos());
        }
        // Räumen von oben nach unten, Reihe für Reihe in Schlangenlinien
        for (int y = this.bounds.maxY() + 1; y >= 0; y--) {
            for (int z = this.bounds.minZ(); z <= this.bounds.maxZ(); z++) {
                for (int i = 0; i <= this.bounds.maxX() - this.bounds.minX(); i++) {
                    int x = (z - this.bounds.minZ()) % 2 == 0 ? this.bounds.minX() + i : this.bounds.maxX() - i;
                    BlockPos local = new BlockPos(x, y, z);
                    if (!planned.contains(local)) {
                        steps.add(new Step(world(origin, rotation, local), Blocks.AIR.defaultBlockState(), false));
                    }
                }
            }
        }
        for (BlockPos column : this.footprint) {
            steps.add(new Step(world(origin, rotation, column.below()), this.foundation, true));
        }
        for (Entry e : this.blocks) {
            steps.add(new Step(world(origin, rotation, e.pos()), world(e.state(), rotation), false, this.data.get(e.pos())));
        }
        return steps;
    }

    /** Ausdehnung eines lokalen Bereichs in der Welt. */
    public static BoundingBox worldBounds(BoundingBox local, BlockPos origin, Rotation rotation) {
        BlockPos a = world(origin, rotation, new BlockPos(local.minX(), local.minY(), local.minZ()));
        BlockPos b = world(origin, rotation, new BlockPos(local.maxX(), local.maxY(), local.maxZ()));
        return BoundingBox.fromCorners(a, b);
    }

    /** Ist an dieser Stelle noch etwas zu tun? (Verbindungen von Zäunen, Scheiben und Treppen zählen nicht.) */
    public static boolean needsWork(ServerLevel level, Step step) {
        if (step.foundation()) {
            BlockPos.MutableBlockPos pos = step.pos().mutable();
            for (int i = 0; i < 12; i++) {
                BlockState old = level.getBlockState(pos);
                if (i > 0 && solidGround(old)) {
                    return false;
                }
                if (old.getBlock() != step.state().getBlock()) {
                    return true;
                }
                pos.move(Direction.DOWN);
            }
            return false;
        }
        BlockState old = level.getBlockState(step.pos());
        if (step.state().isAir()) {
            return !old.isAir();
        }
        return !sameShape(old, step.state());
    }

    /** Gleicher Block in gleicher Ausrichtung (Verbindungen, Wasser, Wachstum und Ähnliches zählen nicht). */
    static boolean sameShape(BlockState a, BlockState b) {
        if (a.getBlock() != b.getBlock()) {
            return false;
        }
        for (var property : b.getProperties()) {
            String name = property.getName();
            if (name.equals("facing") || name.equals("axis") || name.equals("half") || name.equals("type") || name.equals("part")
                    || name.equals("hinge") || name.equals("hanging") || name.equals("attachment")) {
                if (!a.getValue(property).equals(b.getValue(property))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Führt einen Bauschritt aus. Gibt {@code false} zurück, wenn dort schon alles stimmt (nichts zu tun war). Türen,
     * Betten und hohe Pflanzen werden mit ihrer zweiten Hälfte zusammen gesetzt.
     */
    public static boolean apply(ServerLevel level, Step step) {
        if (step.foundation()) {
            boolean changed = false;
            BlockPos.MutableBlockPos pos = step.pos().mutable();
            for (int i = 0; i < 12; i++) {
                BlockState old = level.getBlockState(pos);
                if (i > 0 && solidGround(old)) {
                    break;
                }
                if (!old.equals(step.state())) {
                    level.setBlock(pos, step.state(), Block.UPDATE_ALL);
                    changed = true;
                }
                pos.move(Direction.DOWN);
            }
            return changed;
        }
        if (!needsWork(level, step)) {
            return false;
        }
        BlockState old = level.getBlockState(step.pos());
        if (step.state().isAir() && (old.getBlock() instanceof DoorBlock || old.getBlock() instanceof BedBlock)) {
            // Türen und Betten im Weg: als Ganzes entfernen
            level.removeBlock(step.pos(), false);
            return true;
        }
        BlockState state = step.state();
        BlockPos other = partner(step.pos(), state);
        if (other != null) {
            BlockState otherState = partnerState(state);
            level.setBlock(step.pos(), state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            level.setBlock(other, otherState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            level.updateNeighborsAt(step.pos(), state.getBlock());
            level.updateNeighborsAt(other, otherState.getBlock());
            return true;
        }
        // an die schon stehenden Nachbarn anschließen: Scheiben und Zäune verbinden sich mit der Wand daneben, Treppen
        // bilden Ecken (die Nachbarn, die erst später kommen, passen sich dann selbst an)
        BlockState shaped = Block.updateFromNeighbourShapes(state, level, step.pos());
        level.setBlock(step.pos(), shaped.isAir() ? state : shaped, Block.UPDATE_ALL);
        loadData(level, step);
        return true;
    }

    /** Schreibt die Daten eines Schilds oder Banners aus dem Bauplan in den gesetzten Block. */
    private static void loadData(ServerLevel level, Step step) {
        if (step.data() == null) {
            return;
        }
        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(step.pos());
        if (be != null) {
            be.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,
                    level.registryAccess(), step.data()));
            be.setChanged();
            level.sendBlockUpdated(step.pos(), step.state(), level.getBlockState(step.pos()), Block.UPDATE_CLIENTS);
        }
    }

    /** Baut den ganzen Plan auf einmal (für Tests und den Kreativmodus). */
    public void placeAll(ServerLevel level, BlockPos origin, Rotation rotation) {
        for (Step step : this.steps(origin, rotation)) {
            apply(level, step);
        }
    }

    /** Steht der Plan vollständig? (Pflanzen dürfen inzwischen gewachsen oder geerntet sein.) */
    public boolean complete(ServerLevel level, BlockPos origin, Rotation rotation) {
        for (Entry e : this.blocks) {
            BlockState want = world(e.state(), rotation);
            BlockState have = level.getBlockState(world(origin, rotation, e.pos()));
            if (have.getBlock() != want.getBlock() && !(want.getBlock() instanceof net.minecraft.world.level.block.CropBlock)) {
                return false;
            }
        }
        return true;
    }

    static boolean solidGround(BlockState state) {
        return state.isSolidRender() && !state.is(net.minecraft.tags.BlockTags.LOGS) && !state.is(net.minecraft.tags.BlockTags.LEAVES);
    }

    /** Die zweite Hälfte von Türen, Betten und hohen Pflanzen (oder {@code null}). */
    private static @Nullable BlockPos partner(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof DoorBlock || state.getBlock() instanceof DoublePlantBlock) {
            return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        }
        if (state.getBlock() instanceof BedBlock) {
            Direction facing = state.getValue(BedBlock.FACING);
            return state.getValue(BedBlock.PART) == BedPart.FOOT ? pos.relative(facing) : pos.relative(facing.getOpposite());
        }
        return null;
    }

    private static BlockState partnerState(BlockState state) {
        if (state.getBlock() instanceof BedBlock) {
            return state.setValue(BedBlock.PART, state.getValue(BedBlock.PART) == BedPart.FOOT ? BedPart.HEAD : BedPart.FOOT);
        }
        DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
        return state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, half == DoubleBlockHalf.LOWER ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER);
    }
}
