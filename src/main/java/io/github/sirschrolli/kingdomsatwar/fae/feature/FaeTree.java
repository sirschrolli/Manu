package io.github.sirschrolli.kingdomsatwar.fae.feature;

import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Sammelt die Blöcke eines Baumes und setzt sie gemeinsam: Holz zuerst (es verdrängt Gras und Blumen, nie Fels), dann das
 * Laub mit dem richtigen Abstand zum Holz (sonst verwelkte es oder bliebe nach dem Fällen ewig hängen), zuletzt die
 * Leuchtranken unter den Kronen.
 */
final class FaeTree {
    private final BlockPos origin;
    private final Map<BlockPos, BlockState> logs = new LinkedHashMap<>();
    private final Map<BlockPos, BlockState> leaves = new LinkedHashMap<>();

    FaeTree(BlockPos origin) {
        this.origin = origin;
    }

    void log(BlockPos pos, BlockState log, Direction.Axis axis) {
        BlockPos key = pos.immutable();
        this.logs.put(key, log.hasProperty(RotatedPillarBlock.AXIS) ? log.setValue(RotatedPillarBlock.AXIS, axis) : log);
        this.leaves.remove(key);
    }

    boolean hasLog(BlockPos pos) {
        return this.logs.containsKey(pos);
    }

    void leaf(BlockPos pos, BlockState leaf) {
        if (!this.logs.containsKey(pos)) {
            this.leaves.put(pos.immutable(), leaf);
        }
    }

    boolean hasLeaf(BlockPos pos) {
        return this.leaves.containsKey(pos);
    }

    Iterable<BlockPos> leafPositions() {
        return new java.util.ArrayList<>(this.leaves.keySet());
    }

    /** Die Achse, die am besten zu einer Richtung passt. */
    static Direction.Axis axis(double dx, double dy, double dz) {
        double ax = Math.abs(dx);
        double ay = Math.abs(dy);
        double az = Math.abs(dz);
        if (ay >= ax && ay >= az) {
            return Direction.Axis.Y;
        }
        return ax >= az ? Direction.Axis.X : Direction.Axis.Z;
    }

    /**
     * Ein Ast (oder Stammstück) von a nach b. Die Blöcke hängen immer mit einer Fläche aneinander: wo die Linie schräg
     * springt, kommt ein Zwischenblock dazu – sonst sähe der Ast zerbrochen aus. Liefert den letzten Block.
     */
    BlockPos branch(BlockState log, double ax, double ay, double az, double bx, double by, double bz) {
        double dx = bx - ax;
        double dy = by - ay;
        double dz = bz - az;
        int steps = (int) Math.ceil(Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))));
        Direction.Axis axis = axis(dx, dy, dz);
        BlockPos last = null;
        for (int s = 0; s <= steps; s++) {
            double t = steps == 0 ? 0 : s / (double) steps;
            BlockPos pos = BlockPos.containing(ax + dx * t, ay + dy * t, az + dz * t);
            if (last != null && !pos.equals(last)) {
                BlockPos step = last;
                if (step.getX() != pos.getX() && (step.getZ() != pos.getZ() || step.getY() != pos.getY())) {
                    step = new BlockPos(pos.getX(), step.getY(), step.getZ());
                    this.log(step, log, Direction.Axis.X);
                }
                if (step.getZ() != pos.getZ() && step.getY() != pos.getY()) {
                    step = new BlockPos(step.getX(), step.getY(), pos.getZ());
                    this.log(step, log, Direction.Axis.Z);
                }
            }
            this.log(pos, log, axis);
            last = pos;
        }
        return last;
    }

    /** Ein unregelmäßiger, abgeplatteter Laubballen (die Ränder zufällig ausgefranst). */
    void blob(BlockPos center, double rx, double ry, BlockState leaf, RandomSource random, double fray) {
        int ix = (int) Math.ceil(rx);
        int iy = (int) Math.ceil(ry);
        for (int dx = -ix; dx <= ix; dx++) {
            for (int dy = -iy; dy <= iy; dy++) {
                for (int dz = -ix; dz <= ix; dz++) {
                    double d = (dx * dx + dz * dz) / (rx * rx) + (dy * dy) / (ry * ry);
                    if (d <= 1.0 && (d < 1.0 - fray || random.nextFloat() < 0.6f)) {
                        this.leaf(center.offset(dx, dy, dz), leaf);
                    }
                }
            }
        }
    }

    /**
     * Setzt den Baum. {@code vineChance}: wie oft unter einem Laubblock am Kronenrand eine Leuchtranke hängt, höchstens
     * {@code vineLength} Blöcke lang. Liefert false, wenn kein einziger Stammblock Platz fand.
     */
    boolean place(WorldGenLevel level, RandomSource random, float vineChance, int vineLength) {
        // Abstand jedes Laubblocks zum nächsten Holz (höchstens 6, sonst fällt das Blatt weg)
        Map<BlockPos, Integer> distance = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos log : this.logs.keySet()) {
            distance.put(log, 0);
            queue.add(log);
        }
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            int d = distance.get(pos);
            if (d >= 6) {
                continue;
            }
            for (Direction dir : Direction.values()) {
                BlockPos next = pos.relative(dir);
                if (this.leaves.containsKey(next) && !distance.containsKey(next)) {
                    distance.put(next, d + 1);
                    queue.add(next);
                }
            }
        }
        int placed = 0;
        for (Map.Entry<BlockPos, BlockState> entry : this.logs.entrySet()) {
            BlockPos pos = entry.getKey();
            if (!FaeBuild.inReach(this.origin, pos)) {
                continue;
            }
            BlockState old = level.getBlockState(pos);
            boolean root = entry.getValue().hasProperty(RotatedPillarBlock.AXIS)
                    && entry.getValue().getValue(RotatedPillarBlock.AXIS) != Direction.Axis.Y && FaeBuild.soil(old);
            if (FaeBuild.open(old) || old.is(BlockTags.LEAVES) || root) {
                level.setBlock(pos, entry.getValue(), 2);
                placed++;
            }
        }
        if (placed == 0) {
            return false;
        }
        // unter dem Stamm wird das Gras zu Erde
        for (BlockPos log : this.logs.keySet()) {
            if (FaeBuild.inReach(this.origin, log) && level.getBlockState(log.below()).is(FaeBlocks.FAE_GRASS.get())) {
                level.setBlock(log.below(), FaeBlocks.FAE_SOIL.get().defaultBlockState(), 2);
            }
        }
        java.util.List<BlockPos> rim = new java.util.ArrayList<>();
        for (Map.Entry<BlockPos, BlockState> entry : this.leaves.entrySet()) {
            BlockPos pos = entry.getKey();
            Integer d = distance.get(pos);
            if (d == null || d < 1 || d > 6 || !FaeBuild.inReach(this.origin, pos)) {
                continue;
            }
            BlockState old = level.getBlockState(pos);
            if (FaeBuild.open(old)) {
                BlockState leaf = entry.getValue();
                if (leaf.hasProperty(LeavesBlock.DISTANCE)) {
                    leaf = leaf.setValue(LeavesBlock.DISTANCE, d);
                }
                level.setBlock(pos, leaf, 2);
                if (!this.leaves.containsKey(pos.below()) && !this.logs.containsKey(pos.below())) {
                    rim.add(pos);
                }
            }
        }
        if (vineChance > 0.0f) {
            for (BlockPos pos : rim) {
                if (random.nextFloat() < vineChance) {
                    FaeBuild.hangVines(level, this.origin, pos, vineLength, random);
                }
            }
        }
        return true;
    }
}
