package io.github.sirschrolli.kingdomsatwar.shadow.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Bäume der Schattenwelt, Block für Block gewachsen:
 * <ul>
 *     <li><b>Schattenbaum</b> – ein schiefer, knorriger Stamm mit ausladenden Ästen und violetten Laubballen;</li>
 *     <li><b>Schattenriese</b> – doppelt so dick und hoch, mit Brettwurzeln und einer gewaltigen Krone;</li>
 *     <li><b>Bleichbaum</b> – abgestorben und kahl, ein Gerippe aus geschältem Holz in der Bleichen Öde;</li>
 *     <li><b>Sumpfbaum</b> – auf Stelzwurzeln über dem dunklen Wasser, mit flacher Krone und herabhängendem Laub;</li>
 *     <li><b>Inselbaum</b> – klein und rund, auf den schwebenden Inseln.</li>
 * </ul>
 * Das Laub erhält seinen Abstand zum Holz gleich beim Wachsen, damit es nach dem Fällen richtig verwelkt.
 */
public record ShadowTreeFeature(Kind kind) implements Feature {
    public static final MapCodec<ShadowTreeFeature> CODEC = Kind.CODEC.fieldOf("kind").xmap(ShadowTreeFeature::new, ShadowTreeFeature::kind);

    public enum Kind implements StringRepresentable {
        SHADOW("shadow"),
        GIANT("giant"),
        BLEACHED("bleached"),
        MARSH("marsh"),
        ISLE("isle");

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);
        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    @Override
    public MapCodec<ShadowTreeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        BlockState ground = level.getBlockState(origin.below());
        boolean wet = this.kind == Kind.MARSH && level.getFluidState(origin).isSource();
        if (!wet && !isSoil(ground) || !wet && !level.getBlockState(origin).canBeReplaced()) {
            return false;
        }
        if (wet && !level.getFluidState(origin.above(2)).isEmpty()) {
            return false;   // höchstens zwei Blöcke tiefes Wasser
        }
        Tree tree = new Tree();
        switch (this.kind) {
            case SHADOW -> shadowTree(tree, random, origin, 7 + random.nextInt(5), 2.6);
            case ISLE -> shadowTree(tree, random, origin, 5 + random.nextInt(3), 2.2);
            case GIANT -> giantTree(tree, random, origin);
            case BLEACHED -> bleachedTree(tree, random, origin);
            case MARSH -> marshTree(tree, level, random, origin);
        }
        return tree.place(level, this.kind == Kind.BLEACHED);
    }

    private static boolean isSoil(BlockState state) {
        return state.is(ShadowBlocks.SHADOW_GRASS.get()) || state.is(ShadowBlocks.SHADOW_SOIL.get()) || state.is(ShadowBlocks.GLIMMER_MOSS.get())
                || state.is(ShadowBlocks.SHADOW_SAND.get()) || state.is(BlockTags.DIRT);
    }

    // =====================================================================================
    // Formen
    // =====================================================================================

    /** Schiefer Stamm, drei bis vier Äste nahe der Spitze, Laubballen an den Astenden und obenauf. */
    private static void shadowTree(Tree tree, RandomSource random, BlockPos origin, int height, double crown) {
        Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        double x = origin.getX() + 0.5;
        double z = origin.getZ() + 0.5;
        int y = origin.getY();
        BlockPos top = origin;
        for (int i = 0; i < height; i++) {
            if (i > 2 && random.nextInt(4) == 0) {
                x += lean.getStepX() * 0.7;
                z += lean.getStepZ() * 0.7;
            }
            top = BlockPos.containing(x, y + i, z);
            tree.log(top, Direction.Axis.Y);
        }
        roots(tree, random, origin, 1 + random.nextInt(3), 2);
        int branches = 2 + random.nextInt(3);
        for (int b = 0; b < branches; b++) {
            double angle = (b + random.nextDouble() * 0.6) * Math.PI * 2 / branches;
            int startY = height - 2 - random.nextInt(3);
            BlockPos start = new BlockPos(top.getX(), origin.getY() + startY, top.getZ());
            BlockPos tip = branch(tree, start, angle, 2 + random.nextInt(2), 1);
            tree.blob(tip, crown * (0.8 + random.nextDouble() * 0.3), crown * 0.65, random);
        }
        tree.blob(top.above(), crown, crown * 0.75, random);
    }

    /** 2×2-Stamm mit Brettwurzeln, fünf bis sieben Äste in zwei Stockwerken, große Laubballen. */
    private static void giantTree(Tree tree, RandomSource random, BlockPos origin) {
        int height = 15 + random.nextInt(8);
        for (int i = 0; i < height; i++) {
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    tree.log(origin.offset(dx, i, dz), Direction.Axis.Y);
                }
            }
        }
        // Brettwurzeln: von jeder Seite schräg in den Boden
        for (Direction side : Direction.Plane.HORIZONTAL) {
            int length = 2 + random.nextInt(3);
            BlockPos base = origin.offset(side.getStepX() > 0 ? 1 : 0, 0, side.getStepZ() > 0 ? 1 : 0);
            for (int k = 1; k <= length; k++) {
                int up = Math.max(0, 2 - k);
                tree.log(base.relative(side, k).above(up), side.getAxis());
                if (up > 0) {
                    tree.log(base.relative(side, k), Direction.Axis.Y);
                }
            }
        }
        BlockPos center = origin.offset(1, 0, 1);
        int branches = 5 + random.nextInt(3);
        for (int b = 0; b < branches; b++) {
            double angle = (b + random.nextDouble() * 0.5) * Math.PI * 2 / branches;
            int startY = height - 3 - (b % 2) * 4 - random.nextInt(2);
            BlockPos start = center.above(startY);
            BlockPos tip = branch(tree, start, angle, 3 + random.nextInt(3), 2);
            tree.blob(tip, 3.2 + random.nextDouble(), 2.2, random);
        }
        tree.blob(center.above(height), 4.2, 3.0, random);
    }

    /** Kahler, abgestorbener Baum aus geschältem Holz mit gebrochenen Ästen. */
    private static void bleachedTree(Tree tree, RandomSource random, BlockPos origin) {
        int height = 4 + random.nextInt(5);
        Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        java.util.List<BlockPos> trunk = new java.util.ArrayList<>();
        BlockPos pos = origin;
        for (int i = 0; i < height; i++) {
            if (i > 1 && random.nextInt(3) == 0) {
                pos = pos.relative(lean);
            }
            tree.wood(pos, Direction.Axis.Y);
            trunk.add(pos);
            pos = pos.above();
        }
        int branches = 2 + random.nextInt(3);
        for (int b = 0; b < branches; b++) {
            double angle = random.nextDouble() * Math.PI * 2;
            BlockPos start = trunk.get(Math.min(trunk.size() - 1, 2 + random.nextInt(Math.max(1, height - 2))));
            Direction dir = Direction.fromYRot(Math.toDegrees(angle));
            int length = 1 + random.nextInt(3);
            BlockPos at = start;
            for (int k = 0; k < length; k++) {
                at = at.relative(dir);
                if (k > 0 && random.nextBoolean()) {
                    at = at.above();
                }
                tree.wood(at, dir.getAxis());
            }
        }
    }

    /** Auf Stelzwurzeln: vier schräge Wurzeln, ein kurzer Stamm, eine flache, breite Krone mit herabhängendem Laub. */
    private static void marshTree(Tree tree, WorldGenLevel level, RandomSource random, BlockPos origin) {
        int lift = 2 + random.nextInt(2);
        int height = 4 + random.nextInt(3);
        BlockPos trunkBase = origin.above(lift);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            for (int k = 0; k <= lift; k++) {
                BlockPos root = origin.relative(side, lift - k + 1).above(k);
                tree.log(root, k == lift ? Direction.Axis.Y : side.getAxis());
                // bis zum Grund verlängern
                if (k == 0) {
                    for (int d = 1; d <= 3 && level.getBlockState(root.below(d)).canBeReplaced(); d++) {
                        tree.log(root.below(d), Direction.Axis.Y);
                    }
                }
            }
        }
        for (int i = 0; i < height; i++) {
            tree.log(trunkBase.above(i), Direction.Axis.Y);
        }
        BlockPos top = trunkBase.above(height);
        double radius = 3.5 + random.nextDouble() * 1.2;
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > radius) {
                    continue;
                }
                tree.leaf(top.offset(dx, 0, dz));
                if (d < radius - 1.4) {
                    tree.leaf(top.offset(dx, 1, dz));
                }
                if (d > radius - 1.3 && random.nextInt(3) == 0) {
                    int hang = 1 + random.nextInt(3);
                    for (int h = 1; h <= hang; h++) {
                        tree.leaf(top.offset(dx, -h, dz));
                    }
                }
            }
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            tree.log(top.relative(side), side.getAxis());
            tree.log(top.relative(side, 2), side.getAxis());
        }
    }

    // =====================================================================================
    // Bausteine
    // =====================================================================================

    /** Ein Ast vom Stamm schräg nach außen und oben; liefert das Astende. */
    private static BlockPos branch(Tree tree, BlockPos start, double angle, int length, int rise) {
        double dx = Math.cos(angle);
        double dz = Math.sin(angle);
        Direction.Axis axis = Math.abs(dx) > Math.abs(dz) ? Direction.Axis.X : Direction.Axis.Z;
        BlockPos at = start;
        for (int k = 1; k <= length; k++) {
            at = BlockPos.containing(start.getX() + 0.5 + dx * k, start.getY() + (double) k * rise / length * 1.2, start.getZ() + 0.5 + dz * k);
            tree.log(at, axis);
        }
        return at;
    }

    /** Wurzeln, die vom Stammfuß über den Boden kriechen. */
    private static void roots(Tree tree, RandomSource random, BlockPos origin, int count, int length) {
        for (int r = 0; r < count; r++) {
            Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            for (int k = 1; k <= length; k++) {
                tree.log(origin.relative(side, k), side.getAxis());
            }
        }
    }

    /** Sammelt die Blöcke eines Baumes und setzt sie gemeinsam – Laub mit dem richtigen Abstand zum Holz. */
    private static final class Tree {
        private final Map<BlockPos, BlockState> logs = new LinkedHashMap<>();
        private final Map<BlockPos, Boolean> leaves = new LinkedHashMap<>();

        void log(BlockPos pos, Direction.Axis axis) {
            this.logs.put(pos.immutable(), ShadowBlocks.SHADOW_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis));
            this.leaves.remove(pos);
        }

        void wood(BlockPos pos, Direction.Axis axis) {
            this.logs.put(pos.immutable(), ShadowBlocks.STRIPPED_SHADOW_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis));
        }

        void leaf(BlockPos pos) {
            if (!this.logs.containsKey(pos)) {
                this.leaves.put(pos.immutable(), Boolean.TRUE);
            }
        }

        /** Ein unregelmäßiger, abgeplatteter Laubballen. */
        void blob(BlockPos center, double rx, double ry, RandomSource random) {
            int ix = (int) Math.ceil(rx);
            int iy = (int) Math.ceil(ry);
            for (int dx = -ix; dx <= ix; dx++) {
                for (int dy = -iy; dy <= iy; dy++) {
                    for (int dz = -ix; dz <= ix; dz++) {
                        double d = (dx * dx + dz * dz) / (rx * rx) + (dy * dy) / (ry * ry);
                        if (d <= 1.0 && (d < 0.7 || random.nextFloat() < 0.75)) {
                            this.leaf(center.offset(dx, dy, dz));
                        }
                    }
                }
            }
        }

        boolean place(WorldGenLevel level, boolean bare) {
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
                BlockState old = level.getBlockState(entry.getKey());
                if (old.canBeReplaced() || old.is(BlockTags.LEAVES) || old.is(ShadowBlocks.SHADOW_GRASS.get()) && entry.getValue().getValue(RotatedPillarBlock.AXIS) != Direction.Axis.Y) {
                    level.setBlock(entry.getKey(), entry.getValue(), 2);
                    placed++;
                }
            }
            if (placed == 0) {
                return false;
            }
            // Erde unter dem Stamm
            for (BlockPos log : this.logs.keySet()) {
                BlockState below = level.getBlockState(log.below());
                if (below.is(ShadowBlocks.SHADOW_GRASS.get())) {
                    level.setBlock(log.below(), ShadowBlocks.SHADOW_SOIL.get().defaultBlockState(), 2);
                }
            }
            if (bare) {
                return true;
            }
            BlockState leafState = ShadowBlocks.SHADOW_LEAVES.get().defaultBlockState();
            for (BlockPos pos : this.leaves.keySet()) {
                Integer d = distance.get(pos);
                if (d == null || d < 1 || d > 6) {
                    continue;
                }
                BlockState old = level.getBlockState(pos);
                if (old.isAir() || old.is(Blocks.SHORT_GRASS) || old.canBeReplaced() && old.getFluidState().isEmpty()) {
                    level.setBlock(pos, leafState.setValue(LeavesBlock.DISTANCE, d), 2);
                }
            }
            return true;
        }
    }
}
