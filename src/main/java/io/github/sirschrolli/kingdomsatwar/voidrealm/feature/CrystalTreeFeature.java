package io.github.sirschrolli.kingdomsatwar.voidrealm.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.phys.Vec3;

/**
 * Verdrehter Kristallbaum: Ein Stamm aus leuchtendem Leerenkristall schraubt sich in einer Spirale in die Höhe und teilt
 * sich oben in geschwungene Äste; an ihren Enden hängen Büschel aus Kristalllaub, von denen Sternenranken herabhängen.
 * Wurzeln aus Kristall greifen in den Boden. Der Riese ist doppelt so hoch, mit einem Stamm aus vier verdrillten Strängen.
 */
public record CrystalTreeFeature(Kind kind) implements Feature {
    public enum Kind implements StringRepresentable {
        SMALL, TWISTED, GIANT;

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public static final MapCodec<CrystalTreeFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Kind.CODEC.fieldOf("kind").forGetter(CrystalTreeFeature::kind)
    ).apply(i, CrystalTreeFeature::new));

    @Override
    public MapCodec<CrystalTreeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        BlockState ground = level.getBlockState(origin.below());
        if (!ground.isSolidRender() || !VoidBuild.open(level, origin)) {
            return false;
        }
        int height = switch (this.kind) {
            case SMALL -> 5 + random.nextInt(3);
            case TWISTED -> 8 + random.nextInt(5);
            case GIANT -> 15 + random.nextInt(7);
        };
        // Platz nach oben prüfen (nicht in die Unterseite eines anderen Bruchstücks wachsen)
        for (int y = 1; y <= height + 3; y++) {
            if (!VoidBuild.open(level, origin.above(y))) {
                return false;
            }
        }
        BlockState crystal = VoidBlocks.VOID_CRYSTAL.get().defaultBlockState();
        BlockState foliage = VoidBlocks.CRYSTAL_FOLIAGE.get().defaultBlockState();
        double twist = (0.35 + random.nextDouble() * 0.3) * (random.nextBoolean() ? 1 : -1);
        double phase = random.nextDouble() * Math.PI * 2;
        double sway = this.kind == Kind.GIANT ? 1.6 : 0.9;
        int strands = this.kind == Kind.GIANT ? 3 : 1;
        Vec3 base = Vec3.atBottomCenterOf(origin);
        Vec3 top = base;
        // Stamm: ein oder mehrere Stränge, die sich umeinander winden
        for (int s = 0; s < strands; s++) {
            double offset = s * Math.PI * 2 / strands;
            Vec3 prev = null;
            for (int y = -1; y <= height; y++) {
                double t = y / (double) height;
                double r = sway * Math.sin(Math.PI * Math.max(0, t) * 0.9) + (strands > 1 ? 0.9 * (1 - t * 0.6) : 0.0);
                double a = phase + offset + twist * y;
                Vec3 p = base.add(Math.cos(a) * r, y, Math.sin(a) * r);
                if (prev != null) {
                    VoidBuild.line(level, origin, prev, p, strands > 1 ? 0.0 : (y < height / 3 && this.kind != Kind.SMALL ? 0.7 : 0.0),
                            crystal, y < 0);
                }
                prev = p;
                if (s == 0) {
                    top = p;
                }
            }
        }
        // Wurzeln
        int roots = this.kind == Kind.SMALL ? 2 : 4;
        for (int k = 0; k < roots; k++) {
            double a = phase + k * Math.PI * 2 / roots + random.nextDouble() * 0.6;
            double len = 2 + random.nextInt(this.kind == Kind.GIANT ? 4 : 2);
            Vec3 end = base.add(Math.cos(a) * len, -1 - random.nextInt(2), Math.sin(a) * len);
            VoidBuild.line(level, origin, base.add(0, 0.5, 0), end, 0.0, crystal, true);
        }
        // Äste mit Laub
        int branches = switch (this.kind) {
            case SMALL -> 2;
            case TWISTED -> 3 + random.nextInt(2);
            case GIANT -> 5 + random.nextInt(3);
        };
        for (int k = 0; k < branches; k++) {
            double t = 0.45 + random.nextDouble() * 0.5;
            Vec3 start = base.add(top.subtract(base).scale(t)).add(0, t * height - (top.y - base.y) * t, 0);
            start = new Vec3(start.x, base.y + height * t, start.z);
            double a = phase + k * Math.PI * 2 / branches + random.nextDouble() * 0.8;
            double len = (this.kind == Kind.GIANT ? 4.5 : 2.5) + random.nextDouble() * (this.kind == Kind.GIANT ? 3.5 : 2.0);
            Vec3 mid = start.add(Math.cos(a) * len * 0.55, len * 0.25, Math.sin(a) * len * 0.55);
            Vec3 end = start.add(Math.cos(a + twist * 0.8) * len, len * 0.55 + random.nextDouble(), Math.sin(a + twist * 0.8) * len);
            VoidBuild.line(level, origin, start, mid, 0.0, crystal, false);
            VoidBuild.line(level, origin, mid, end, 0.0, crystal, false);
            leaves(level, origin, BlockPos.containing(end), random, this.kind == Kind.GIANT ? 2.6 : 1.9, foliage);
        }
        leaves(level, origin, BlockPos.containing(top).above(), random, this.kind == Kind.GIANT ? 3.2 : (this.kind == Kind.SMALL ? 1.6 : 2.3),
                foliage);
        return true;
    }

    /** Ein Büschel Kristalllaub, darunter ein paar Sternenranken. */
    private static void leaves(WorldGenLevel level, BlockPos origin, BlockPos center, RandomSource random, double r, BlockState foliage) {
        VoidBuild.blob(level, origin, center, r, foliage, random, 0.9);
        BlockState vines = VoidBlocks.STAR_VINES.get().defaultBlockState();
        BlockState vinesPlant = VoidBlocks.STAR_VINES_PLANT.get().defaultBlockState();
        for (int k = 0; k < 3; k++) {
            BlockPos start = center.offset(random.nextInt(3) - 1, -(int) Math.ceil(r * 0.6), random.nextInt(3) - 1);
            while (!level.getBlockState(start).is(foliage.getBlock()) && start.getY() < center.getY()) {
                start = start.above();
            }
            if (!level.getBlockState(start).is(foliage.getBlock())) {
                continue;
            }
            int len = 1 + random.nextInt(4);
            for (int i = 1; i <= len; i++) {
                BlockPos at = start.below(i);
                if (!VoidBuild.inReach(origin, at) || !level.isEmptyBlock(at)) {
                    break;
                }
                boolean tip = i == len || !level.isEmptyBlock(at.below());
                level.setBlock(at, tip ? vines : vinesPlant, 2);
                if (tip) {
                    break;
                }
            }
        }
        // ein paar Drusen auf dem Laub
        for (int k = 0; k < 2; k++) {
            BlockPos at = center.offset(random.nextInt(3) - 1, (int) Math.ceil(r), random.nextInt(3) - 1);
            while (at.getY() > center.getY() && level.isEmptyBlock(at.below())) {
                at = at.below();
            }
            if (level.isEmptyBlock(at) && level.getBlockState(at.below()).isFaceSturdy(level, at.below(), Direction.UP)) {
                VoidBuild.place(level, origin, at, VoidBlocks.VOID_CRYSTAL_CLUSTER.get().defaultBlockState());
            }
        }
    }
}
