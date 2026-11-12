package io.github.sirschrolli.kingdomsatwar.urmeer.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.block.sea.SeaSaplingBlock;
import io.github.sirschrolli.kingdomsatwar.registry.SeaWoods;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluids;

/**
 * Die Bäume der Meereshölzer (seit 0.92) – sie wachsen am Grund des Meeres, im Wasser (das Laub hält es) und bleiben immer
 * ein paar Blöcke unter der Oberfläche:
 * <ul>
 *     <li><b>Tangbaum</b> – ein hoher, schlanker Stamm, der sich in der Strömung wiegt; an ihm entlang und oben in einem
 *     Fächer lange, aufwärts treibende Tangwedel;</li>
 *     <li><b>Korallenbaum</b> – ein kurzer Stamm, der sich wie ein Hirschhorn in Äste und Zweige teilt; an jedem Zweigende ein
 *     Büschel Korallenfächer;</li>
 *     <li><b>Perlbaum</b> – ein heller, leicht geneigter Stamm mit einer runden Krone aus schimmernden Perlblättern, von deren
 *     Rand einzelne Perlschnüre hängen;</li>
 *     <li><b>Tiefseebaum</b> – ein gedrehter, dunkler Stamm mit Wurzeln, oben bogenförmige Äste, von denen leuchtende Wedel
 *     wie Vorhänge herabhängen.</li>
 * </ul>
 */
public record SeaTreeFeature(Kind kind) implements Feature {
    public static final MapCodec<SeaTreeFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Kind.CODEC.fieldOf("kind").forGetter(SeaTreeFeature::kind)
    ).apply(i, SeaTreeFeature::new));

    public enum Kind implements StringRepresentable {
        KELP("kelp"),
        CORAL("coral"),
        PEARL("pearl"),
        ABYSS("abyss");

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);
        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        SeaWoods.SeaWood wood() {
            return switch (this) {
                case KELP -> SeaWoods.KELP;
                case CORAL -> SeaWoods.CORAL;
                case PEARL -> SeaWoods.PEARL;
                case ABYSS -> SeaWoods.ABYSS;
            };
        }
    }

    @Override
    public MapCodec<SeaTreeFeature> codec() {
        return CODEC;
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Darf der Baum hier wachsen (Wasser, Luft, Seegras, Tang …)? */
    static boolean open(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(BlockTags.REPLACEABLE_BY_TREES) || state.is(Blocks.SEAGRASS)
                || state.is(Blocks.TALL_SEAGRASS) || state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT)
                || state.is(BlockTags.LEAVES) && !state.is(Blocks.AIR) || state.getBlock() instanceof SeaSaplingBlock
                || state.canBeReplaced() && !state.getFluidState().isEmpty();
    }

    /** Fester Grund für einen Meeresbaum. */
    static boolean ground(BlockState state) {
        return SeaSaplingBlock.soil(state) || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.TIDE_STONE.base().get())
                || state.is(io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.ABYSSAL_STONE.base().get());
    }

    /** In der Schreibzone der Erzeugung (der Chunk der Mitte und seine Nachbarn)? */
    static boolean inReach(BlockPos origin, BlockPos pos) {
        return Math.abs(SectionPos.blockToSectionCoord(pos.getX()) - SectionPos.blockToSectionCoord(origin.getX())) <= 1
                && Math.abs(SectionPos.blockToSectionCoord(pos.getZ()) - SectionPos.blockToSectionCoord(origin.getZ())) <= 1;
    }

    /** Wie viel Wasser (Blöcke) steht über dem Grund, bevor Luft kommt (höchstens {@code max})? */
    static int water(WorldGenLevel level, BlockPos origin, int max) {
        int n = 0;
        while (n < max) {
            BlockState state = level.getBlockState(origin.above(n));
            if (state.getFluidState().isEmpty() && !(state.getBlock() instanceof SeaSaplingBlock)) {
                break;
            }
            n++;
        }
        return n;
    }

    /** Sammelt Holz und Laub und setzt sie zusammen (Laub mit Abstand zum Holz, im Wasser geflutet). */
    static final class Tree {
        private final BlockPos origin;
        private final BlockState log;
        /** Holz mit Rinde ringsum: für Äste, Wurzeln und Versätze (keine Jahresringe an den Seiten). */
        private final BlockState bark;
        private final BlockState leaf;
        private final Map<BlockPos, BlockState> logs = new LinkedHashMap<>();
        private final Map<BlockPos, BlockState> leaves = new LinkedHashMap<>();
        private final int ceiling;

        Tree(BlockPos origin, SeaWoods.SeaWood wood, int ceiling) {
            this.origin = origin;
            this.log = wood.log.get().defaultBlockState();
            this.bark = wood.wood.get().defaultBlockState();
            this.leaf = wood.leaves.get().defaultBlockState();
            this.ceiling = ceiling;
        }

        void log(BlockPos pos, Direction.Axis axis) {
            if (pos.getY() > this.ceiling) {
                return;
            }
            BlockPos key = pos.immutable();
            // was zuerst da ist, bleibt (der Stamm bleibt Stamm, wo Äste und Wurzeln ansetzen); senkrechte Stücke sind Stamm,
            // alles Schräge und Liegende Holz mit Rinde ringsum
            if (this.logs.containsKey(key)) {
                return;
            }
            this.logs.put(key, (axis == Direction.Axis.Y ? this.log : this.bark).setValue(RotatedPillarBlock.AXIS, axis));
            this.leaves.remove(key);
        }

        void leaf(BlockPos pos) {
            if (pos.getY() > this.ceiling || this.logs.containsKey(pos)) {
                return;
            }
            this.leaves.put(pos.immutable(), this.leaf);
        }

        boolean hasLog(BlockPos pos) {
            return this.logs.containsKey(pos);
        }

        /** Ein Ast von a nach b, Block an Block mit einer Fläche verbunden; liefert den letzten Block. */
        BlockPos branch(double ax, double ay, double az, double bx, double by, double bz) {
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
                        this.log(step, Direction.Axis.X);
                    }
                    if (step.getZ() != pos.getZ() && step.getY() != pos.getY()) {
                        step = new BlockPos(step.getX(), step.getY(), pos.getZ());
                        this.log(step, Direction.Axis.Z);
                    }
                }
                this.log(pos, axis);
                last = pos;
            }
            return last;
        }

        /** Ein Laubballen (Ränder ausgefranst). */
        void blob(BlockPos center, double rx, double ry, RandomSource random, double fray) {
            int ix = (int) Math.ceil(rx);
            int iy = (int) Math.ceil(ry);
            for (int dx = -ix; dx <= ix; dx++) {
                for (int dy = -iy; dy <= iy; dy++) {
                    for (int dz = -ix; dz <= ix; dz++) {
                        double d = (dx * dx + dz * dz) / (rx * rx) + (dy * dy) / (ry * ry);
                        if (d <= 1.0 && (d < 1.0 - fray || random.nextFloat() < 0.55f)) {
                            this.leaf(center.offset(dx, dy, dz));
                        }
                    }
                }
            }
        }

        /**
         * Ein Wedel: eine Linie aus Laub von a nach b, Block an Block mit einer Fläche verbunden (sonst fände das Laub den
         * Weg zum Holz nicht und welkte – solche Blätter setzt {@link #place} gar nicht erst).
         */
        void frond(double ax, double ay, double az, double bx, double by, double bz) {
            double dx = bx - ax;
            double dy = by - ay;
            double dz = bz - az;
            int steps = (int) Math.ceil(Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)))) * 2;
            BlockPos last = null;
            for (int s = 0; s <= steps; s++) {
                double t = steps == 0 ? 0 : s / (double) steps;
                BlockPos pos = BlockPos.containing(ax + dx * t, ay + dy * t, az + dz * t);
                if (last != null && !pos.equals(last)) {
                    BlockPos step = last;
                    if (step.getX() != pos.getX() && (step.getY() != pos.getY() || step.getZ() != pos.getZ())) {
                        step = new BlockPos(pos.getX(), step.getY(), step.getZ());
                        this.leaf(step);
                    }
                    if (step.getZ() != pos.getZ() && step.getY() != pos.getY()) {
                        step = new BlockPos(step.getX(), step.getY(), pos.getZ());
                        this.leaf(step);
                    }
                }
                this.leaf(pos);
                last = pos;
            }
        }

        /** Hängt Laub von {@code from} senkrecht hinab (höchstens {@code length} Blöcke, nur durch Wasser und Luft). */
        void hang(WorldGenLevel level, BlockPos from, int length) {
            for (int i = 1; i <= length; i++) {
                BlockPos pos = from.below(i);
                if (this.logs.containsKey(pos) || !open(level.getBlockState(pos))) {
                    return;
                }
                this.leaf(pos);
            }
        }

        static Direction.Axis axis(double dx, double dy, double dz) {
            double ax = Math.abs(dx);
            double ay = Math.abs(dy);
            double az = Math.abs(dz);
            if (ay >= ax && ay >= az) {
                return Direction.Axis.Y;
            }
            return ax >= az ? Direction.Axis.X : Direction.Axis.Z;
        }

        /** Setzt den Baum; false, wenn der Stamm keinen Platz fand. */
        boolean place(WorldGenLevel level) {
            // der Stamm muss frei stehen (die unteren drei Blöcke), sonst wächst hier nichts
            for (int y = 0; y < 3; y++) {
                BlockPos pos = this.origin.above(y);
                if (this.logs.containsKey(pos) && !open(level.getBlockState(pos))) {
                    return false;
                }
            }
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
                if (!inReach(this.origin, pos)) {
                    continue;
                }
                BlockState old = level.getBlockState(pos);
                // Wurzeln dürfen in den Sand greifen
                boolean root = entry.getValue().getValue(RotatedPillarBlock.AXIS) != Direction.Axis.Y && SeaSaplingBlock.soil(old);
                if (open(old) || root) {
                    level.setBlock(pos, entry.getValue(), 2);
                    placed++;
                }
            }
            if (placed == 0) {
                return false;
            }
            for (Map.Entry<BlockPos, BlockState> entry : this.leaves.entrySet()) {
                BlockPos pos = entry.getKey();
                Integer d = distance.get(pos);
                if (d == null || d < 1 || d > 6 || !inReach(this.origin, pos)) {
                    continue;
                }
                BlockState old = level.getBlockState(pos);
                if (open(old) && !old.is(BlockTags.LEAVES)) {
                    BlockState leaf = entry.getValue().setValue(LeavesBlock.DISTANCE, d)
                            .setValue(LeavesBlock.WATERLOGGED, old.getFluidState().is(Fluids.WATER));
                    level.setBlock(pos, leaf, 2);
                }
            }
            return true;
        }
    }

    // =====================================================================================
    // Die Bäume
    // =====================================================================================

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!ground(level.getBlockState(origin.below())) || !open(level.getBlockState(origin))) {
            return false;
        }
        int water = water(level, origin, 64);
        // nie bis an die Oberfläche: drei Blöcke Wasser bleiben darüber (an Land wächst er wie ein Baum, bis 20 hoch)
        int room = water > 0 ? water - 3 : 20;
        int need = switch (this.kind) {
            case KELP -> 8;
            case CORAL -> 6;
            case PEARL -> 9;
            case ABYSS -> 9;
        };
        if (room < need) {
            return false;
        }
        // nicht in einen anderen Baum hinein
        for (int y = 1; y <= 4; y++) {
            if (level.getBlockState(origin.above(y)).is(BlockTags.LOGS)) {
                return false;
            }
        }
        Tree tree = new Tree(origin, this.kind.wood(), origin.getY() + room - 1);
        switch (this.kind) {
            case KELP -> kelp(tree, level, random, origin, room);
            case CORAL -> coral(tree, random, origin, room);
            case PEARL -> pearl(tree, level, random, origin, room);
            case ABYSS -> abyss(tree, level, random, origin, room);
        }
        return tree.place(level);
    }

    /** Tangbaum: hoch und schlank, wiegt sich; Wedel am Stamm entlang und ein Fächer oben. */
    private static void kelp(Tree tree, WorldGenLevel level, RandomSource random, BlockPos o, int room) {
        int height = Math.min(room - 2, 12 + random.nextInt(9));
        double phase = random.nextDouble() * Math.PI * 2.0;
        double sway = 0.8 + random.nextDouble() * 0.8;
        double dirX = random.nextDouble() * 2.0 - 1.0;
        double dirZ = random.nextDouble() * 2.0 - 1.0;
        double len = Math.max(0.2, Math.sqrt(dirX * dirX + dirZ * dirZ));
        dirX /= len;
        dirZ /= len;
        double px = o.getX() + 0.5;
        double pz = o.getZ() + 0.5;
        double lastX = px;
        double lastZ = pz;
        List<double[]> joints = new ArrayList<>();
        for (int y = 0; y <= height; y++) {
            double s = Math.sin(y * 0.33 + phase) * sway * Math.min(1.0, y / 4.0);
            double x = px + dirX * s;
            double z = pz + dirZ * s;
            tree.branch(lastX, o.getY() + Math.max(0, y - 1) + 0.5, lastZ, x, o.getY() + y + 0.5, z);
            lastX = x;
            lastZ = z;
            joints.add(new double[] {x, o.getY() + y + 0.5, z});
        }
        // Blätter am Stamm entlang wie beim Riesentang: alle ein, zwei Blöcke ein Wedel, schraubig um den Stamm versetzt,
        // erst ein Stück hinaus, dann aufwärts treibend (oben länger)
        double spiral = random.nextDouble() * Math.PI * 2.0;
        for (int y = 3; y < height - 1; y += 1 + random.nextInt(2)) {
            double[] j = joints.get(y);
            spiral += 2.2 + (random.nextDouble() - 0.5) * 0.9;
            double ox = Math.cos(spiral);
            double oz = Math.sin(spiral);
            double l = 1.8 + random.nextDouble() * 1.5 + 1.0 * y / height;
            double mx = j[0] + ox * l * 0.6;
            double my = j[1] + l * 0.45;
            double mz = j[2] + oz * l * 0.6;
            tree.frond(j[0] + ox * 0.7, j[1], j[2] + oz * 0.7, mx, my, mz);
            tree.frond(mx, my, mz, j[0] + ox * l * 0.85, j[1] + l * 1.15, j[2] + oz * l * 0.85);
        }
        // oben ein Fächer aus langen Wedeln
        double[] top = joints.get(height);
        int fronds = 5 + random.nextInt(3);
        for (int k = 0; k < fronds; k++) {
            double a = k * Math.PI * 2.0 / fronds + random.nextDouble() * 0.5;
            double l = 3.0 + random.nextDouble() * 2.0;
            double rise = 1.0 + random.nextDouble() * 2.0;
            double mx = top[0] + Math.cos(a) * l * 0.5;
            double mz = top[2] + Math.sin(a) * l * 0.5;
            tree.frond(top[0], top[1], top[2], mx, top[1] + rise, mz);
            tree.frond(mx, top[1] + rise, mz, top[0] + Math.cos(a) * l, top[1] + rise * 0.6, top[2] + Math.sin(a) * l);
        }
        tree.blob(BlockPos.containing(top[0], top[1] + 1, top[2]), 1.4, 1.2, random, 0.3);
    }

    /** Korallenbaum: kurzer Stamm, verzweigt wie ein Hirschhorn; an jedem Zweigende Korallenfächer. */
    private static void coral(Tree tree, RandomSource random, BlockPos o, int room) {
        int trunk = 2 + random.nextInt(3);
        double cx = o.getX() + 0.5;
        double cz = o.getZ() + 0.5;
        tree.branch(cx, o.getY() + 0.5, cz, cx, o.getY() + trunk + 0.5, cz);
        int branches = 3 + random.nextInt(3);
        double a0 = random.nextDouble() * Math.PI * 2.0;
        double maxUp = Math.max(3.0, room - trunk - 3.0);
        for (int b = 0; b < branches; b++) {
            double a = a0 + b * Math.PI * 2.0 / branches + (random.nextDouble() - 0.5) * 0.5;
            double out = 2.5 + random.nextDouble() * 1.8;
            double up = Math.min(maxUp * 0.6, 2.0 + random.nextDouble() * 1.8);
            double bx = cx + Math.cos(a) * out;
            double by = o.getY() + trunk + 0.5 + up;
            double bz = cz + Math.sin(a) * out;
            tree.branch(cx, o.getY() + trunk + 0.5, cz, bx, by, bz);
            // jeder Ast teilt sich in zwei Zweige
            for (int t = 0; t < 2; t++) {
                double ta = a + (t == 0 ? -0.6 : 0.6) + (random.nextDouble() - 0.5) * 0.3;
                double tl = 1.5 + random.nextDouble() * 1.4;
                double tu = Math.min(maxUp - up, 1.5 + random.nextDouble() * 1.5);
                double ex = bx + Math.cos(ta) * tl;
                double ey = by + Math.max(0.5, tu);
                double ez = bz + Math.sin(ta) * tl;
                BlockPos end = tree.branch(bx, by, bz, ex, ey, ez);
                tree.blob(end.above(), 1.6 + random.nextDouble() * 0.6, 1.2, random, 0.35);
            }
        }
    }

    /** Perlbaum: heller, leicht geschwungener Stamm, eine flache Schirmkrone mit einer kleineren darüber, Perlschnüre am Rand. */
    private static void pearl(Tree tree, WorldGenLevel level, RandomSource random, BlockPos o, int room) {
        int height = Math.min(room - 5, 5 + random.nextInt(3));
        double lean = random.nextDouble() * Math.PI * 2.0;
        double lx = Math.cos(lean) * 1.4;
        double lz = Math.sin(lean) * 1.4;
        double cx = o.getX() + 0.5;
        double cz = o.getZ() + 0.5;
        // erst gerade, dann zur Seite geneigt: ein sanfter Schwung
        double midY = o.getY() + height * 0.45 + 0.5;
        tree.branch(cx, o.getY() + 0.5, cz, cx - lx * 0.15, midY, cz - lz * 0.15);
        BlockPos top = tree.branch(cx - lx * 0.15, midY, cz - lz * 0.15, cx + lx, o.getY() + height + 0.5, cz + lz);
        // zwei, drei kurze Äste in den Schirm
        int arms = 2 + random.nextInt(2);
        double arm0 = random.nextDouble() * Math.PI * 2.0;
        for (int k = 0; k < arms; k++) {
            double a = arm0 + k * Math.PI * 2.0 / arms + (random.nextDouble() - 0.5) * 0.6;
            double y0 = top.getY() - 1 + 0.5;
            tree.branch(top.getX() + 0.5, y0, top.getZ() + 0.5, top.getX() + 0.5 + Math.cos(a) * 2.4, y0 + 1.2, top.getZ() + 0.5 + Math.sin(a) * 2.4);
        }
        // der Schirm: breit und flach, darüber eine kleinere Kuppel
        double r = 3.1 + random.nextDouble() * 0.7;
        tree.blob(top.above(), r, 1.5, random, 0.22);
        BlockPos crest = top.offset(Mth.floor(Math.cos(lean) * 0.8 + 0.5), 2, Mth.floor(Math.sin(lean) * 0.8 + 0.5));
        tree.blob(crest, r * 0.62, 1.3, random, 0.3);
        // Perlschnüre: rings um den Schirmrand hängt Laub herab, mal kurz, mal lang
        int strands = 8 + random.nextInt(4);
        for (int k = 0; k < strands; k++) {
            double a = k * Math.PI * 2.0 / strands + (random.nextDouble() - 0.5) * 0.4;
            double rr = r - 0.4 - random.nextDouble() * 0.8;
            BlockPos rim = BlockPos.containing(top.getX() + 0.5 + Math.cos(a) * rr, top.getY() + 0.5, top.getZ() + 0.5 + Math.sin(a) * rr);
            tree.leaf(rim);
            tree.hang(level, rim, 1 + random.nextInt(4));
        }
    }

    /** Tiefseebaum: gedreht, mit Wurzeln; bogenförmige Äste, von denen leuchtende Wedel herabhängen. */
    private static void abyss(Tree tree, WorldGenLevel level, RandomSource random, BlockPos o, int room) {
        int height = Math.min(room - 3, 9 + random.nextInt(6));
        double cx = o.getX() + 0.5;
        double cz = o.getZ() + 0.5;
        double phase = random.nextDouble() * Math.PI * 2.0;
        double lastX = cx;
        double lastZ = cz;
        for (int y = 0; y <= height; y++) {
            double a = phase + y * 0.45;
            double rr = 0.6 * Math.min(1.0, y / 3.0);
            double x = cx + Math.cos(a) * rr;
            double z = cz + Math.sin(a) * rr;
            tree.branch(lastX, o.getY() + Math.max(0, y - 1) + 0.5, lastZ, x, o.getY() + y + 0.5, z);
            lastX = x;
            lastZ = z;
        }
        // Wurzeln: vier, fünf flache Ausläufer am Fuß
        int roots = 4 + random.nextInt(2);
        for (int k = 0; k < roots; k++) {
            double a = k * Math.PI * 2.0 / roots + random.nextDouble() * 0.5;
            double l = 1.6 + random.nextDouble() * 1.2;
            tree.branch(cx, o.getY() + 0.5, cz, cx + Math.cos(a) * l, o.getY() + 0.5, cz + Math.sin(a) * l);
        }
        // oben bogenförmige Äste, von deren Enden Wedel hängen
        double topY = o.getY() + height + 0.5;
        int arms = 4 + random.nextInt(3);
        for (int k = 0; k < arms; k++) {
            double a = k * Math.PI * 2.0 / arms + random.nextDouble() * 0.6;
            double l = 2.6 + random.nextDouble() * 1.4;
            double mx = lastX + Math.cos(a) * l * 0.55;
            double mz = lastZ + Math.sin(a) * l * 0.55;
            tree.branch(lastX, topY - 1.0, lastZ, mx, topY + 1.0, mz);
            BlockPos end = tree.branch(mx, topY + 1.0, mz, lastX + Math.cos(a) * l, topY + 0.2, lastZ + Math.sin(a) * l);
            tree.blob(end, 1.3, 0.9, random, 0.3);
            // der Vorhang: Wedel hängen vom Astende und daneben herab
            for (int h = 0; h < 3; h++) {
                BlockPos from = end.offset(Mth.floor((random.nextDouble() - 0.5) * 3.0), 0, Mth.floor((random.nextDouble() - 0.5) * 3.0));
                // mit dem Astende verbunden, damit der Vorhang nicht welkt
                tree.frond(end.getX() + 0.5, end.getY() + 0.5, end.getZ() + 0.5, from.getX() + 0.5, from.getY() + 0.5, from.getZ() + 0.5);
                tree.hang(level, from, 2 + random.nextInt(5));
            }
        }
        tree.blob(BlockPos.containing(lastX, topY + 1.5, lastZ), 2.0, 1.4, random, 0.3);
    }
}
