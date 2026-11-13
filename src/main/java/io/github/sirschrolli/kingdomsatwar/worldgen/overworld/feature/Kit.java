package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Gemeinsame Handgriffe der Oberwelt-Features: was ersetzt werden darf, Boden finden, Laubabstände setzen. */
final class Kit {
    private Kit() {}

    /** Luft oder etwas, das ein Baum oder Fels verdrängen darf (Gras, Blumen, Schnee- und Ascheschichten). */
    static boolean open(BlockState state) {
        return state.isAir() || state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    /** Wie {@link #open}, aber auch Wasser (für Felsen in der Brandung). */
    static boolean openOrWater(BlockState state) {
        return open(state) || state.is(Blocks.WATER) || state.canBeReplaced();
    }

    /** Fester Boden, in den man etwas einlassen darf (Erde, Sand, Stein, Schnee …) – keine Bauwerke. */
    static boolean ground(BlockState state) {
        // SUBSTRATE_OVERWORLD: Erde, Grasblöcke (seit 26.x nicht mehr im Erde-Tag), Moos und Schlamm
        return state.is(BlockTags.SUBSTRATE_OVERWORLD) || state.is(BlockTags.SAND) || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(Blocks.GRAVEL) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.ICE)
                || state.is(Blocks.BLUE_ICE) || state.is(Blocks.CLAY) || state.is(Blocks.CALCITE) || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.POWDER_SNOW) || state.is(BlockTags.TERRACOTTA) || state.is(Blocks.SMOOTH_BASALT)
                || state.is(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Blocks.NATURAL_GROUND);
    }

    /** Setzt den Block, wenn dort Luft oder Verdrängbares ist. */
    static boolean setIfOpen(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (open(level.getBlockState(pos))) {
            level.setBlock(pos, state, 2);
            return true;
        }
        return false;
    }

    /** Oberkante des Bodens (ohne Laub) in dieser Spalte. */
    static int surface(WorldGenLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    static BlockState log(BlockState log, Direction.Axis axis) {
        return log.hasProperty(RotatedPillarBlock.AXIS) ? log.setValue(RotatedPillarBlock.AXIS, axis) : log;
    }

    /** Die Achse, die am besten zu einer Richtung passt (für Äste). */
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
     * Ein Ast von a nach b aus Stammblöcken (in Richtung ausgerichtet). Die Blöcke hängen immer mit einer Fläche
     * aneinander: wo die Linie schräg springt, kommt ein Zwischenblock dazu – sonst sähe der Ast zerbrochen aus.
     */
    static void branch(WorldGenLevel level, Set<BlockPos> logs, BlockState log, double ax, double ay, double az, double bx, double by, double bz) {
        double dx = bx - ax;
        double dy = by - ay;
        double dz = bz - az;
        int steps = (int) Math.ceil(Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))));
        BlockState state = log(log, axis(dx, dy, dz));
        BlockPos last = null;
        for (int s = 0; s <= steps; s++) {
            double t = steps == 0 ? 0 : s / (double) steps;
            BlockPos pos = BlockPos.containing(ax + dx * t, ay + dy * t, az + dz * t);
            if (last != null && !pos.equals(last)) {
                // erst waagrecht, dann senkrecht nachziehen, bis die Blöcke Fläche an Fläche liegen
                BlockPos step = last;
                if (step.getX() != pos.getX() && (step.getZ() != pos.getZ() || step.getY() != pos.getY())) {
                    step = new BlockPos(pos.getX(), step.getY(), step.getZ());
                    put(level, logs, step, log(log, Direction.Axis.X));
                }
                if (step.getZ() != pos.getZ() && step.getY() != pos.getY()) {
                    step = new BlockPos(step.getX(), step.getY(), pos.getZ());
                    put(level, logs, step, log(log, Direction.Axis.Z));
                }
            }
            put(level, logs, pos, state);
            last = pos;
        }
    }

    private static void put(WorldGenLevel level, Set<BlockPos> logs, BlockPos pos, BlockState state) {
        BlockState old = level.getBlockState(pos);
        if (open(old) || old.is(BlockTags.LEAVES)) {
            level.setBlock(pos, state, 2);
            logs.add(pos.immutable());
        }
    }

    /**
     * Laub bekommt seinen richtigen Abstand zum Stamm (wie beim gewachsenen Baum) – so fällt es nach dem Fällen ab, steht
     * aber, solange der Baum steht. Laub, das weiter als sechs Blöcke vom Holz entfernt ist, bleibt dauerhaft.
     */
    static void settleLeaves(WorldGenLevel level, Set<BlockPos> logs, Set<BlockPos> leaves) {
        Map<BlockPos, Integer> distance = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos log : logs) {
            distance.put(log, 0);
            queue.add(log);
        }
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            int d = distance.get(pos);
            if (d >= LeavesBlock.DECAY_DISTANCE - 1) {
                continue;
            }
            for (Direction dir : Direction.values()) {
                BlockPos next = pos.relative(dir);
                if (leaves.contains(next) && !distance.containsKey(next)) {
                    distance.put(next, d + 1);
                    queue.add(next);
                }
            }
        }
        for (BlockPos pos : leaves) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof LeavesBlock)) {
                continue;
            }
            Integer d = distance.get(pos);
            BlockState settled = d == null ? state.setValue(LeavesBlock.PERSISTENT, true)
                    : state.setValue(LeavesBlock.DISTANCE, Math.max(1, d)).setValue(LeavesBlock.PERSISTENT, false);
            level.setBlock(pos, settled, 2);
        }
    }

    /** Ein Laubballen (Ellipsoid mit ausgefranstem Rand). */
    static void leafBlob(WorldGenLevel level, Set<BlockPos> leaves, BlockState leaf, double cx, double cy, double cz, double radius, double height,
            net.minecraft.util.RandomSource random, double fray) {
        int reach = (int) Math.ceil(radius) + 1;
        int reachY = (int) Math.ceil(height) + 1;
        int bx = (int) Math.floor(cx);
        int by = (int) Math.floor(cy);
        int bz = (int) Math.floor(cz);
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                for (int dy = -reachY; dy <= reachY; dy++) {
                    double ex = (bx + dx + 0.5 - cx) / radius;
                    double ez = (bz + dz + 0.5 - cz) / radius;
                    double ey = (by + dy + 0.5 - cy) / height;
                    double e = ex * ex + ey * ey + ez * ez;
                    if (e <= 1.0 - fray * random.nextDouble()) {
                        BlockPos pos = new BlockPos(bx + dx, by + dy, bz + dz);
                        if (open(level.getBlockState(pos))) {
                            level.setBlock(pos, leaf, 2);
                            leaves.add(pos);
                        }
                    }
                }
            }
        }
    }

    /** Hat die Umgebung genug Platz (keine Bauwerke, kein Wasser auf der Fläche)? */
    static boolean dryGround(WorldGenLevel level, BlockPos origin) {
        return level.getFluidState(origin).isEmpty() && ground(level.getBlockState(origin.below()));
    }
}
