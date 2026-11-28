package io.github.sirschrolli.kingdomsatwar.gametest;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Prüft, ob man ein Gebäude zu Fuß begehen kann – so, wie ein Spieler, der nur geht und nicht springt: Er steigt Stufen
 * und Platten hinauf (höchstens eine halbe Stufe auf einmal), fällt höchstens drei Blöcke tief, braucht 1,8 Blöcke
 * Kopffreiheit, öffnet Türen, Tore und Luken und klettert an Leitern. Gesucht wird vom Eingang aus; dann muss jeder
 * „Raum“ (zusammenhängende Fußböden unter einem Dach mit mindestens 2,5 Blöcken Höhe) erreichbar sein, und neben jedem
 * wichtigen Block (Bett, Truhe, Tisch …) muss man stehen können.
 */
final class WalkCheck {
    private static final double STEP = 0.6;
    private static final double HEIGHT = 1.8;
    private static final double ROOM_HEIGHT = 2.5;
    private static final int MAX_DROP = 3;
    private static final double EPS = 1.0e-4;

    private final ServerLevel level;
    private final BoundingBox box;
    private final Map<Long, List<Double>> surfaces = new HashMap<>();
    private final Set<Long> visited = new HashSet<>();

    /** Ein Standplatz: Spalte und Höhe der Füße (in Sechzehnteln, damit gleiche Höhen gleich sind). */
    private record Spot(int x, int z, int h16) {
        double h() {
            return this.h16 / 16.0;
        }

        long key() {
            return ((long) this.x & 0x3FFFFFL) << 42 | ((long) this.z & 0x3FFFFFL) << 20 | (this.h16 & 0xFFFFFL);
        }
    }

    WalkCheck(ServerLevel level, BoundingBox box) {
        this.level = level;
        this.box = box;
    }

    // =====================================================================================
    // Blöcke
    // =====================================================================================

    /** Kein Hindernis: Türen, Tore und Luken öffnet man; Leitern, Pflanzen, Fackeln und Ähnliches hat keine Kollision. */
    private VoxelShape shape(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof DoorBlock || block instanceof FenceGateBlock || block instanceof TrapDoorBlock || state.is(BlockTags.CLIMBABLE)) {
            return Shapes.empty();   // Leitern: man steht darin und klettert
        }
        return state.getCollisionShape(this.level, pos);
    }

    private boolean climbable(BlockPos pos) {
        return this.level.getBlockState(pos).is(BlockTags.CLIMBABLE);
    }

    /** Ist der Raum von {@code from} bis {@code to} (Höhen in Blöcken) in der Spalte frei? Zelle {@code skip} zählt nicht mit. */
    private boolean free(int x, int z, double from, double to, int skip) {
        for (int y = (int) Math.floor(from + EPS); y < to - EPS; y++) {
            if (y == skip) {
                continue;
            }
            BlockPos pos = new BlockPos(x, y, z);
            VoxelShape shape = this.shape(pos);
            if (shape.isEmpty()) {
                continue;
            }
            double lo = y + shape.min(Direction.Axis.Y);
            double hi = y + shape.max(Direction.Axis.Y);
            if (hi > from + EPS && lo < to - EPS) {
                return false;
            }
        }
        return true;
    }

    /** Die Höhen, auf denen man in einer Spalte stehen kann (mit 1,8 Blöcken Platz darüber). */
    private List<Double> surfaces(int x, int z) {
        long key = BlockPos.asLong(x, 0, z);
        List<Double> cached = this.surfaces.get(key);
        if (cached != null) {
            return cached;
        }
        List<Double> list = new ArrayList<>();
        for (int y = this.box.minY() - 2; y <= this.box.maxY() + 2; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = this.level.getBlockState(pos);
            VoxelShape shape = this.shape(pos);
            List<Double> tops = new ArrayList<>();
            if (state.getBlock() instanceof StairBlock && state.getValue(StairBlock.HALF) == Half.BOTTOM) {
                tops.add(y + 0.5);
                tops.add(y + 1.0);
            } else if (!shape.isEmpty()) {
                double top = shape.max(Direction.Axis.Y);
                if (top <= 1.0 + EPS) {
                    tops.add(y + top);
                }
            }
            if (this.climbable(pos) && shape.isEmpty()) {
                tops.add((double) y);   // an der Leiter hängen
            }
            for (double h : tops) {
                if (this.free(x, z, h, h + HEIGHT, y) && !list.contains(h)) {
                    list.add(h);
                }
            }
        }
        this.surfaces.put(key, list);
        return list;
    }

    // =====================================================================================
    // Gehen
    // =====================================================================================

    /** Alle Standplätze, die man von der Spalte (x, z) nahe der Höhe {@code y} aus erreicht. */
    WalkCheck walkFrom(int x, int z, double y) {
        Spot start = null;
        for (double h : this.surfaces(x, z)) {
            if (start == null || Math.abs(h - y) < Math.abs(start.h() - y)) {
                start = new Spot(x, z, (int) Math.round(h * 16));
            }
        }
        if (start == null) {
            return this;
        }
        ArrayDeque<Spot> queue = new ArrayDeque<>();
        queue.add(start);
        this.visited.add(start.key());
        while (!queue.isEmpty()) {
            Spot s = queue.poll();
            for (Spot n : this.moves(s)) {
                if (this.visited.add(n.key())) {
                    queue.add(n);
                }
            }
        }
        return this;
    }

    private boolean inside(int x, int z) {
        return x >= this.box.minX() - 4 && x <= this.box.maxX() + 4 && z >= this.box.minZ() - 4 && z <= this.box.maxZ() + 4;
    }

    private List<Spot> moves(Spot s) {
        List<Spot> out = new ArrayList<>();
        double h = s.h();
        // in derselben Spalte: halbe Stufen (Treppe) und an der Leiter hinauf und hinunter
        boolean onLadder = this.climbable(new BlockPos(s.x(), (int) Math.floor(h + EPS), s.z()));
        for (double h2 : this.surfaces(s.x(), s.z())) {
            double d = h2 - h;
            if (Math.abs(d) < EPS) {
                continue;
            }
            boolean stair = Math.abs(d) <= 0.5 + EPS;
            boolean ladder = onLadder && d > 0 && d <= 1.0 + EPS
                    || d < 0 && d >= -1.0 - EPS && this.climbable(new BlockPos(s.x(), (int) Math.floor(h2 + EPS), s.z()));
            if (stair || ladder) {
                out.add(new Spot(s.x(), s.z(), (int) Math.round(h2 * 16)));
            }
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int nx = s.x() + dir.getStepX();
            int nz = s.z() + dir.getStepZ();
            if (!this.inside(nx, nz)) {
                continue;
            }
            for (double h2 : this.surfaces(nx, nz)) {
                double d = h2 - h;
                // an der Leiter ist man irgendwo im Leiterblock: oben tritt man einen ganzen Block höher hinaus
                if (d > (onLadder ? 1.0 : STEP) + EPS || d < -MAX_DROP - EPS) {
                    continue;
                }
                // hinunter: über die Kante treten, dann fallen – der Weg dorthin muss frei sein
                if (d < -EPS && !this.free(nx, nz, h2 + 0.01, h + HEIGHT, Integer.MIN_VALUE)) {
                    continue;
                }
                // hinauf: am alten Platz muss der Kopf Platz haben, wenn die Füße die Stufe erreichen
                if (d > EPS && !this.free(s.x(), s.z(), h, h2 + HEIGHT, (int) Math.floor(h - EPS))) {
                    continue;
                }
                out.add(new Spot(nx, nz, (int) Math.round(h2 * 16)));
            }
        }
        return out;
    }

    /** Für die Fehlersuche: die Standhöhen einer Spalte, erreichte mit Stern. */
    String column(int x, int z) {
        StringBuilder b = new StringBuilder(x + "," + z + ":");
        for (double h : this.surfaces(x, z)) {
            b.append(' ').append(h).append(this.reached(x, z, h) ? "*" : "");
        }
        return b.toString();
    }

    /** Für Fehlermeldungen: wie viele Plätze erreicht wurden und die höchsten davon. */
    String explain() {
        int max = Integer.MIN_VALUE;
        List<String> top = new ArrayList<>();
        for (long key : this.visited) {
            int h16 = (int) (key & 0xFFFFFL);
            if (h16 > max) {
                max = h16;
                top.clear();
            }
            if (h16 == max && top.size() < 6) {
                int x = (int) (key >> 42);
                int z = (int) (key << 22 >> 42);
                top.add(x + "," + z);
            }
        }
        return this.visited.size() + " Plätze, höchste Füße bei y=" + (max / 16.0) + " an " + top;
    }

    boolean reached(int x, int z, double h) {
        return this.visited.contains(new Spot(x, z, (int) Math.round(h * 16)).key());
    }

    /** Kann man neben (oder auf) dem Block stehen, nicht tiefer als einen Block darunter und nicht höher als anderthalb darüber? */
    boolean reachesBlock(BlockPos target) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int x = target.getX() + dir.getStepX();
            int z = target.getZ() + dir.getStepZ();
            for (double h : this.surfaces(x, z)) {
                if (h >= target.getY() - 1.0 - EPS && h <= target.getY() + 1.5 + EPS && this.reached(x, z, h)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Steht man irgendwo in dieser Spalte mit den Füßen etwa auf Höhe {@code feet.getY()}? */
    boolean reachesSpot(BlockPos feet) {
        for (double h : this.surfaces(feet.getX(), feet.getZ())) {
            if (Math.abs(h - feet.getY()) <= 0.6 && this.reached(feet.getX(), feet.getZ(), h)) {
                return true;
            }
        }
        return false;
    }

    // =====================================================================================
    // Räume
    // =====================================================================================

    /** Ist das ein Fußboden (und kein Möbelstück, kein Fass, kein Regal)? */
    private boolean floorBlock(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        Block block = state.getBlock();
        if (this.level.getBlockEntity(pos) != null || state.is(BlockTags.WOOL) || block == Blocks.HAY_BLOCK || block == Blocks.BOOKSHELF
                || block == Blocks.CHISELED_BOOKSHELF || state.is(BlockTags.LEAVES)) {
            return false;
        }
        if (block instanceof StairBlock || block instanceof SlabBlock && state.getValue(SlabBlock.TYPE) != SlabType.TOP) {
            return true;
        }
        return Block.isShapeFullBlock(state.getCollisionShape(this.level, pos));
    }

    /**
     * Der erste Raum, den man nicht erreicht (eine seiner Stellen), oder {@code null}. Ein Raum: mindestens {@code minSize}
     * zusammenhängende Fußböden innerhalb des Gebäudes mit Dach darüber und {@link #ROOM_HEIGHT} Blöcken Höhe; berücksichtigt
     * werden nur Böden ab {@code minY}.
     */
    @Nullable BlockPos unreachedRoom(int minSize, int minY) {
        Map<Long, Spot> floors = new HashMap<>();
        for (int x = this.box.minX(); x <= this.box.maxX(); x++) {
            for (int z = this.box.minZ(); z <= this.box.maxZ(); z++) {
                for (double h : this.surfaces(x, z)) {
                    int y = (int) Math.floor(h - EPS);
                    if (h < minY - EPS || !this.floorBlock(new BlockPos(x, y, z)) || !this.free(x, z, h, h + ROOM_HEIGHT, y)
                            || !this.roofed(x, z, h + ROOM_HEIGHT)) {
                        continue;
                    }
                    Spot s = new Spot(x, z, (int) Math.round(h * 16));
                    floors.put(s.key(), s);
                }
            }
        }
        Set<Long> done = new HashSet<>();
        for (Spot seed : floors.values()) {
            if (done.contains(seed.key())) {
                continue;
            }
            // Raum zusammensuchen (ohne Fallen: Nachbarböden höchstens eine halbe Stufe höher oder tiefer)
            List<Spot> room = new ArrayList<>();
            ArrayDeque<Spot> queue = new ArrayDeque<>();
            queue.add(seed);
            done.add(seed.key());
            while (!queue.isEmpty()) {
                Spot s = queue.poll();
                room.add(s);
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    for (int dh = -8; dh <= 8; dh++) {
                        Spot n = new Spot(s.x() + dir.getStepX(), s.z() + dir.getStepZ(), s.h16() + dh);
                        if (floors.containsKey(n.key()) && done.add(n.key())) {
                            queue.add(n);
                        }
                    }
                }
            }
            if (room.size() >= minSize && square(room) && room.stream().noneMatch(s -> this.visited.contains(s.key()))) {
                Spot s = room.getFirst();
                return new BlockPos(s.x(), (int) Math.floor(s.h() + EPS), s.z());
            }
        }
        return null;
    }

    /** Ein Raum ist mindestens an einer Stelle zwei mal zwei Felder groß (ein Balken oder Sims ist kein Raum). */
    private static boolean square(List<Spot> room) {
        Set<Long> cols = new HashSet<>();
        for (Spot s : room) {
            cols.add(BlockPos.asLong(s.x(), 0, s.z()));
        }
        for (Spot s : room) {
            if (cols.contains(BlockPos.asLong(s.x() + 1, 0, s.z())) && cols.contains(BlockPos.asLong(s.x(), 0, s.z() + 1))
                    && cols.contains(BlockPos.asLong(s.x() + 1, 0, s.z() + 1))) {
                return true;
            }
        }
        return false;
    }

    /** Steht über dieser Stelle (ab {@code from}) noch etwas innerhalb des Gebäudes? */
    private boolean roofed(int x, int z, double from) {
        for (int y = (int) Math.floor(from); y <= this.box.maxY() + 1; y++) {
            if (!this.level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                return true;
            }
        }
        return false;
    }
}
