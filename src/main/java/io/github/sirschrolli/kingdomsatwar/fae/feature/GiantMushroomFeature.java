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
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Die Riesenpilze des Pilzhains, mit leuchtenden Hüten (die Unterseite zeigt die Lamellen):
 * <ul>
 *     <li><b>Rosenschirmling</b> – hoch und schlank, ein weiter, flacher Schirm mit herabgebogenem Rand;</li>
 *     <li><b>Himmelskuppe</b> – gedrungen, mit einer runden, hohlen Kuppel;</li>
 *     <li><b>Violettglocke</b> – sehr hoch, mit einem spitzen Glockenhut und ausgestelltem Saum.</li>
 * </ul>
 * Oft wachsen zwei, drei Pilze verschiedener Arten zu einer Gruppe zusammen ({@code cluster}); am Fuß stehen
 * Laternenpilze, vom Hutrand hängen manchmal Leuchtranken.
 */
public record GiantMushroomFeature(Kind kind, boolean cluster) implements Feature {
    public static final MapCodec<GiantMushroomFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Kind.CODEC.fieldOf("kind").forGetter(GiantMushroomFeature::kind),
            Codec.BOOL.fieldOf("cluster").forGetter(GiantMushroomFeature::cluster)
    ).apply(i, GiantMushroomFeature::new));

    public enum Kind implements StringRepresentable {
        ROSE("rose"),
        AZURE("azure"),
        VIOLET("violet"),
        RANDOM("random");

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
    public MapCodec<GiantMushroomFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        Kind first = this.kind == Kind.RANDOM ? Kind.values()[random.nextInt(3)] : this.kind;
        if (!grow(level, random, origin, origin, first, 1.0)) {
            return false;
        }
        if (this.cluster) {
            int more = 1 + random.nextInt(2);
            for (int i = 0; i < more; i++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double d = 4.0 + random.nextDouble() * 2.5;
                int x = origin.getX() + (int) Math.round(Math.cos(a) * d);
                int z = origin.getZ() + (int) Math.round(Math.sin(a) * d);
                BlockPos at = new BlockPos(x, FaeBuild.surface(level, x, z), z);
                if (Math.abs(at.getY() - origin.getY()) <= 3) {
                    Kind kind = Kind.values()[(first.ordinal() + 1 + random.nextInt(2)) % 3];
                    grow(level, random, origin, at, kind, 0.55 + random.nextDouble() * 0.2);
                }
            }
        }
        return true;
    }

    private static BlockState cap(Kind kind) {
        return switch (kind) {
            case AZURE -> FaeBlocks.AZURE_MUSHROOM_CAP.get().defaultBlockState();
            case VIOLET -> FaeBlocks.VIOLET_MUSHROOM_CAP.get().defaultBlockState();
            default -> FaeBlocks.ROSE_MUSHROOM_CAP.get().defaultBlockState();
        };
    }

    /** Ein Pilz an {@code at}; {@code scale} &lt; 1 für die kleineren Pilze einer Gruppe. */
    private static boolean grow(WorldGenLevel level, RandomSource random, BlockPos origin, BlockPos at, Kind kind, double scale) {
        if (!FaeBuild.soil(level.getBlockState(at.below())) || !FaeBuild.open(level.getBlockState(at))) {
            return false;
        }
        int height = switch (kind) {
            case ROSE -> 8 + random.nextInt(5);
            case AZURE -> 5 + random.nextInt(3);
            default -> 10 + random.nextInt(5);
        };
        height = Math.max(3, (int) Math.round(height * scale));
        for (int y = 1; y <= height + 2; y++) {
            if (!FaeBuild.open(level.getBlockState(at.above(y)))) {
                return false;
            }
        }
        BlockState stem = FaeBlocks.FAE_MUSHROOM_STEM.get().defaultBlockState();
        boolean thick = scale >= 1.0 && height >= 11;
        // der Stiel: leicht geneigt, bei großen Pilzen zwei mal zwei Blöcke dick
        double angle = random.nextDouble() * Math.PI * 2.0;
        double lean = thick ? 0.0 : 0.6 + random.nextDouble() * 0.6;
        BlockPos top = at;
        for (int y = 0; y < height; y++) {
            double t = y / (double) height;
            int ox = (int) Math.round(Math.cos(angle) * lean * t * t);
            int oz = (int) Math.round(Math.sin(angle) * lean * t * t);
            top = at.offset(ox, y, oz);
            FaeBuild.place(level, origin, top, stem);
            if (thick) {
                FaeBuild.place(level, origin, top.east(), stem);
                FaeBuild.place(level, origin, top.south(), stem);
                FaeBuild.place(level, origin, top.east().south(), stem);
            }
        }
        // Wurzelknollen am Fuß
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (random.nextFloat() < (thick ? 0.9f : 0.5f)) {
                BlockPos foot = (thick && (side == Direction.EAST || side == Direction.SOUTH) ? at.relative(side) : at).relative(side);
                FaeBuild.place(level, origin, foot, stem.setValue(RotatedPillarBlock.AXIS, side.getAxis()));
            }
        }
        double cx = top.getX() + (thick ? 1.0 : 0.5);
        double cz = top.getZ() + (thick ? 1.0 : 0.5);
        int capY = top.getY() + 1;
        BlockState cap = cap(kind);
        java.util.List<BlockPos> rim = new java.util.ArrayList<>();
        switch (kind) {
            case ROSE -> parasol(level, origin, cx, capY, cz, (4.0 + random.nextDouble() * 1.2) * Math.max(0.7, scale), cap, rim);
            case AZURE -> dome(level, origin, cx, capY - 1, cz, (3.0 + random.nextDouble() * 0.9) * Math.max(0.75, scale), cap, rim);
            default -> bell(level, origin, cx, capY, cz, (2.8 + random.nextDouble() * 0.6) * Math.max(0.75, scale), cap, rim);
        }
        for (BlockPos pos : rim) {
            if (random.nextFloat() < 0.14f) {
                FaeBuild.hangVines(level, origin, pos, 3, random);
            }
        }
        // Laternenpilze am Fuß
        int lanterns = 2 + random.nextInt(3);
        for (int i = 0; i < lanterns; i++) {
            int x = at.getX() + random.nextInt(7) - 3;
            int z = at.getZ() + random.nextInt(7) - 3;
            BlockPos pos = new BlockPos(x, FaeBuild.surface(level, x, z), z);
            if (Math.abs(pos.getY() - at.getY()) <= 2) {
                FaeBuild.plant(level, origin, pos, FaeBlocks.LANTERN_MUSHROOM.get().defaultBlockState());
            }
        }
        return true;
    }

    /** Ein flacher Schirm: innen zwei Lagen, außen eine, der äußerste Ring eine Stufe tiefer. */
    private static void parasol(WorldGenLevel level, BlockPos origin, double cx, int y, double cz, double r, BlockState cap,
            java.util.List<BlockPos> rim) {
        int ir = (int) Math.ceil(r);
        for (int dx = -ir; dx <= ir; dx++) {
            for (int dz = -ir; dz <= ir; dz++) {
                double x = Math.floor(cx) + dx + 0.5;
                double z = Math.floor(cz) + dz + 0.5;
                double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                if (d > r) {
                    continue;
                }
                BlockPos pos = BlockPos.containing(x, y, z);
                if (d > r - 1.1) {
                    FaeBuild.place(level, origin, pos.below(), cap);
                    rim.add(pos.below());
                } else {
                    FaeBuild.place(level, origin, pos, cap);
                    if (d < r * 0.55) {
                        FaeBuild.place(level, origin, pos.above(), cap);
                    }
                }
            }
        }
    }

    /** Eine runde Kuppel aus einer Lage; ihr Rand liegt auf Höhe des Stielendes. */
    private static void dome(WorldGenLevel level, BlockPos origin, double cx, int y, double cz, double r, BlockState cap,
            java.util.List<BlockPos> rim) {
        int ir = (int) Math.ceil(r);
        for (int dx = -ir; dx <= ir; dx++) {
            for (int dz = -ir; dz <= ir; dz++) {
                for (int dy = 0; dy <= ir; dy++) {
                    double x = Math.floor(cx) + dx + 0.5;
                    double z = Math.floor(cz) + dz + 0.5;
                    double h = (x - cx) * (x - cx) + (z - cz) * (z - cz);
                    double d = Math.sqrt(h + (dy + 0.5) * (dy + 0.5) * 1.25);
                    if (d > r || d < r - 1.25 && dy < ir - 1) {
                        continue;
                    }
                    BlockPos pos = BlockPos.containing(x, y + dy, z);
                    FaeBuild.place(level, origin, pos, cap);
                    if (dy == 0) {
                        rim.add(pos);
                    }
                }
            }
        }
    }

    /** Ein spitzer Glockenhut: Lage um Lage schmaler, unten ein ausgestellter Saum. */
    private static void bell(WorldGenLevel level, BlockPos origin, double cx, int y, double cz, double r, BlockState cap,
            java.util.List<BlockPos> rim) {
        int layers = 4 + (int) Math.round(r);
        for (int k = -1; k <= layers; k++) {
            double t = Math.max(0.0, k) / (double) layers;
            double radius = k < 0 ? r + 0.7 : r * Math.pow(1.0 - t, 0.75) + 0.35;
            int ir = (int) Math.ceil(radius);
            for (int dx = -ir; dx <= ir; dx++) {
                for (int dz = -ir; dz <= ir; dz++) {
                    double x = Math.floor(cx) + dx + 0.5;
                    double z = Math.floor(cz) + dz + 0.5;
                    double d = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                    boolean solidTop = k >= layers - 1;
                    if (d > radius || !solidTop && d < radius - 1.15 && k >= 0) {
                        continue;
                    }
                    if (k < 0 && d < r - 0.4) {
                        continue;   // der Saum ist nur ein Ring
                    }
                    BlockPos pos = BlockPos.containing(x, y + k, z);
                    FaeBuild.place(level, origin, pos, cap);
                    if (k < 0) {
                        rim.add(pos);
                    }
                }
            }
        }
    }
}
