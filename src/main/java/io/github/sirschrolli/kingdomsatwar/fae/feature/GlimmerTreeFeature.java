package io.github.sirschrolli.kingdomsatwar.fae.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Die Glimmerbäume des Feenreichs – silbrige Rinde, leuchtendes Laub in Rosa oder Türkis:
 * <ul>
 *     <li><b>Glimmerbaum</b> – ein schlanker, sanft geschwungener Stamm mit zwei, drei aufstrebenden Ästen und einer
 *     wolkigen Krone, unter der einzelne Leuchtranken hängen;</li>
 *     <li><b>Glimmerriese</b> – ein mächtiger Doppelstamm mit Brettwurzeln, zwei Stockwerken ausladender Äste und einer
 *     gewaltigen Krone, aus der Schleier von Leuchtranken fallen;</li>
 *     <li><b>Tränenglimmer</b> – an den Ufern der Mondseen: ein kurzer Stamm, bogenförmige Äste und Vorhänge aus Laub,
 *     die bis fast aufs Wasser hängen.</li>
 * </ul>
 */
public record GlimmerTreeFeature(Kind kind, Palette palette) implements Feature {
    public static final MapCodec<GlimmerTreeFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Kind.CODEC.fieldOf("kind").forGetter(GlimmerTreeFeature::kind),
            Palette.CODEC.fieldOf("palette").forGetter(GlimmerTreeFeature::palette)
    ).apply(i, GlimmerTreeFeature::new));

    public enum Kind implements StringRepresentable {
        SLENDER("slender"),
        GRAND("grand"),
        WEEPING("weeping");

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

    /** Welche Laubfarbe: immer Rosa, immer Türkis oder je Baum gemischt. */
    public enum Palette implements StringRepresentable {
        ROSE("rose"),
        AZURE("azure"),
        MIXED("mixed");

        public static final Codec<Palette> CODEC = StringRepresentable.fromEnum(Palette::values);
        private final String name;

        Palette(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    @Override
    public MapCodec<GlimmerTreeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!FaeBuild.soil(level.getBlockState(origin.below())) || !FaeBuild.open(level.getBlockState(origin))) {
            return false;
        }
        // nicht in einen anderen Baum hinein wachsen
        int clear = this.kind == Kind.GRAND ? 8 : 4;
        for (int y = 1; y <= clear; y++) {
            BlockState state = level.getBlockState(origin.above(y));
            if (!FaeBuild.open(state) && !state.is(net.minecraft.tags.BlockTags.LEAVES)) {
                return false;
            }
        }
        BlockState rose = FaeBlocks.ROSE_GLIMMER_LEAVES.get().defaultBlockState();
        BlockState azure = FaeBlocks.AZURE_GLIMMER_LEAVES.get().defaultBlockState();
        BlockState main = switch (this.palette) {
            case ROSE -> rose;
            case AZURE -> azure;
            case MIXED -> random.nextFloat() < 0.55f ? rose : azure;
        };
        BlockState other = main == rose ? azure : rose;
        FaeTree tree = new FaeTree(origin);
        return switch (this.kind) {
            case SLENDER -> {
                slender(tree, random, origin, main);
                yield tree.place(level, random, 0.10f, 4);
            }
            case GRAND -> {
                grand(tree, random, origin, main, other);
                yield tree.place(level, random, 0.2f, 7);
            }
            case WEEPING -> {
                weeping(tree, random, origin, main);
                yield tree.place(level, random, 0.16f, 4);
            }
        };
    }

    private static BlockState log() {
        return FaeBlocks.GLIMMERWOOD_LOG.get().defaultBlockState();
    }

    private static BlockState wood() {
        return FaeBlocks.GLIMMERWOOD_WOOD.get().defaultBlockState();
    }

    // =====================================================================================
    // Glimmerbaum
    // =====================================================================================

    /** Schlanker, S-förmig geschwungener Stamm, zwei bis drei Äste, eine wolkige Krone aus mehreren Ballen. */
    private static void slender(FaeTree tree, RandomSource random, BlockPos origin, BlockState leaf) {
        int height = 6 + random.nextInt(4);
        double angle = random.nextDouble() * Math.PI * 2.0;
        double lx = Math.cos(angle);
        double lz = Math.sin(angle);
        double bend = 0.9 + random.nextDouble() * 0.7;
        double x0 = origin.getX() + 0.5;
        double z0 = origin.getZ() + 0.5;
        int y0 = origin.getY();
        // unten gerade, in der Mitte ausschwingend, oben wieder etwas zurück
        double mx = x0 + lx * bend;
        double mz = z0 + lz * bend;
        double tx = x0 + lx * bend * 0.45;
        double tz = z0 + lz * bend * 0.45;
        tree.branch(log(), x0, y0, z0, x0, y0 + 2, z0);
        tree.branch(log(), x0, y0 + 2, z0, mx, y0 + height * 0.6, mz);
        BlockPos top = tree.branch(log(), mx, y0 + height * 0.6, mz, tx, y0 + height, tz);
        int branches = 2 + random.nextInt(2);
        for (int b = 0; b < branches; b++) {
            double a = angle + Math.PI * (0.6 + 2.0 * b / branches) + random.nextDouble() * 0.5;
            double sy = y0 + height * (0.55 + 0.12 * b) + random.nextDouble();
            double sx = x0 + (mx - x0) * 0.8;
            double sz = z0 + (mz - z0) * 0.8;
            double length = 2.2 + random.nextDouble() * 1.4;
            BlockPos tip = tree.branch(wood(), sx, sy, sz, sx + Math.cos(a) * length, sy + 1.4 + random.nextDouble(), sz + Math.sin(a) * length);
            tree.blob(tip.above(), 2.0 + random.nextDouble() * 0.6, 1.5 + random.nextDouble() * 0.3, leaf, random, 0.3);
        }
        double crown = 2.7 + random.nextDouble() * 0.7;
        tree.blob(top.above(), crown, crown * 0.72, leaf, random, 0.25);
        tree.blob(top.offset((int) Math.round(-lx), 2, (int) Math.round(-lz)), crown * 0.65, crown * 0.5, leaf, random, 0.35);
    }

    // =====================================================================================
    // Glimmerriese
    // =====================================================================================

    /** Doppelstamm mit Brettwurzeln, Äste in zwei Stockwerken, riesige Krone; ein Teil der Ballen in der zweiten Farbe. */
    private static void grand(FaeTree tree, RandomSource random, BlockPos origin, BlockState main, BlockState other) {
        int height = 14 + random.nextInt(6);
        for (int i = 0; i < height; i++) {
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    tree.log(origin.offset(dx, i, dz), log(), Direction.Axis.Y);
                }
            }
        }
        // Brettwurzeln: von jeder Seite schräg in den Boden – je Seite eine, manchmal zwei
        for (Direction side : Direction.Plane.HORIZONTAL) {
            Direction along = side.getClockWise();
            for (int column = 0; column <= 1; column++) {
                if (column == 1 && random.nextFloat() > 0.45f) {
                    continue;
                }
                int length = 2 + random.nextInt(3);
                int bx = side.getStepX() > 0 ? 1 : side.getStepX() < 0 ? 0 : (along.getStepX() > 0 ? column : 1 - column);
                int bz = side.getStepZ() > 0 ? 1 : side.getStepZ() < 0 ? 0 : (along.getStepZ() > 0 ? column : 1 - column);
                BlockPos base = origin.offset(bx, 0, bz);
                for (int k = 1; k <= length; k++) {
                    int up = Math.max(0, 3 - k);
                    tree.log(base.relative(side, k).above(up), wood(), side.getAxis());
                    for (int u = 0; u < up; u++) {
                        tree.log(base.relative(side, k).above(u), wood(), Direction.Axis.Y);
                    }
                }
            }
        }
        double cx = origin.getX() + 1.0;
        double cz = origin.getZ() + 1.0;
        int y0 = origin.getY();
        int branches = 5 + random.nextInt(3);
        for (int b = 0; b < branches; b++) {
            double a = (b + random.nextDouble() * 0.5) * Math.PI * 2.0 / branches;
            boolean upper = b % 2 == 1;
            double sy = y0 + height * (upper ? 0.82 : 0.6) + random.nextDouble() * 2.0;
            double length = upper ? 3.0 + random.nextDouble() * 1.5 : 4.2 + random.nextDouble() * 1.8;
            double rise = upper ? 2.0 + random.nextDouble() : 2.5 + random.nextDouble() * 1.5;
            double ex = cx + Math.cos(a) * length;
            double ez = cz + Math.sin(a) * length;
            // ein Knick: erst flach hinaus, dann aufwärts
            double kx = cx + Math.cos(a) * length * 0.55;
            double kz = cz + Math.sin(a) * length * 0.55;
            tree.branch(wood(), cx + Math.cos(a) * 0.8, sy, cz + Math.sin(a) * 0.8, kx, sy + rise * 0.35, kz);
            BlockPos tip = tree.branch(wood(), kx, sy + rise * 0.35, kz, ex, sy + rise, ez);
            BlockState leaf = random.nextFloat() < 0.22f ? other : main;
            double r = upper ? 2.8 + random.nextDouble() * 0.6 : 3.2 + random.nextDouble() * 0.8;
            tree.blob(tip.above(), r, r * 0.66, leaf, random, 0.28);
        }
        BlockPos top = origin.offset(0, height, 0);
        tree.blob(top.offset(1, 1, 1), 4.2 + random.nextDouble() * 0.6, 3.0, main, random, 0.22);
    }

    // =====================================================================================
    // Tränenglimmer
    // =====================================================================================

    /** Kurzer Stamm, Bogenäste rundum, eine flache Kuppel und Laubvorhänge, die tief herabhängen. */
    private static void weeping(FaeTree tree, RandomSource random, BlockPos origin, BlockState leaf) {
        int height = 5 + random.nextInt(3);
        double x0 = origin.getX() + 0.5;
        double z0 = origin.getZ() + 0.5;
        int y0 = origin.getY();
        double lean = random.nextDouble() * Math.PI * 2.0;
        double tx = x0 + Math.cos(lean) * 0.8;
        double tz = z0 + Math.sin(lean) * 0.8;
        BlockPos top = tree.branch(log(), x0, y0, z0, tx, y0 + height, tz);
        int arms = 5 + random.nextInt(3);
        for (int b = 0; b < arms; b++) {
            double a = (b + random.nextDouble() * 0.4) * Math.PI * 2.0 / arms;
            double length = 3.0 + random.nextDouble() * 1.4;
            double ex = tx + Math.cos(a) * length;
            double ez = tz + Math.sin(a) * length;
            BlockPos tip = tree.branch(wood(), tx, y0 + height - 1, tz, ex, y0 + height + 0.5, ez);
            tree.blob(tip, 1.7 + random.nextDouble() * 0.4, 1.1, leaf, random, 0.3);
        }
        tree.blob(top.above(), 3.4 + random.nextDouble() * 0.6, 1.8, leaf, random, 0.25);
        // Vorhänge: vom Kronenrand hängt das Laub in Strähnen herab
        double cx = top.getX() + 0.5;
        double cz = top.getZ() + 0.5;
        for (BlockPos pos : tree.leafPositions()) {
            double dx = pos.getX() + 0.5 - cx;
            double dz = pos.getZ() + 0.5 - cz;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d < 2.4 || tree.hasLeaf(pos.below()) || random.nextFloat() > 0.6f) {
                continue;
            }
            int hang = 2 + random.nextInt(4);
            for (int h = 1; h <= hang; h++) {
                tree.leaf(pos.below(h), leaf);
            }
        }
    }
}
