package io.github.sirschrolli.kingdomsatwar.worldgen.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

/**
 * Eine Laubwolke: ein unregelmäßiges Ellipsoid mit zwei, drei Beulen und ausgefranstem Rand – so wirken Eichen, Birken
 * und Schwarzeichen rund und weich statt würfelig. Auf Wunsch hängen unten einzelne Spitzen herab.
 */
public class CloudFoliagePlacer extends ShapedFoliagePlacer {
    public static final MapCodec<CloudFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(i -> foliagePlacerParts(i).and(i.group(
            IntProviders.codec(1, 12).fieldOf("height").forGetter(p -> p.height),
            Codec.floatRange(0.0f, 1.0f).fieldOf("hang").forGetter(p -> p.hang)
    )).apply(i, CloudFoliagePlacer::new));

    private final IntProvider height;
    private final float hang;

    public CloudFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height, float hang) {
        super(radius, offset);
        this.height = height;
        this.hang = hang;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return ModTrees.CLOUD_FOLIAGE.get();
    }

    @Override
    public int foliageHeight(RandomSource random, int treeHeight, TreeFeature tree) {
        return this.height.sample(random);
    }

    @Override
    protected void createFoliage(WorldGenLevel level, FoliageSetter setter, RandomSource random, TreeFeature tree, int treeHeight,
            FoliageAttachment attachment, int foliageHeight, int leafRadius, int offset) {
        boolean dbl = attachment.doubleTrunk();
        Leaves leaves = new Leaves();
        BlockPos center = attachment.pos().above(offset);
        double c = dbl ? 0.5 : 0.0;
        double r = Math.max(1, leafRadius + attachment.radiusOffsetXZ()) + 0.5;
        double ry = Math.max(1.2, foliageHeight / 2.0 + 0.2);
        // die Hauptwolke (ein wenig über dem Holz) und zwei, drei Beulen
        int lumps = 2 + random.nextInt(2);
        double[][] blobs = new double[lumps + 1][];
        blobs[0] = new double[] {c, ry * 0.25, c, r, ry};
        for (int k = 1; k <= lumps; k++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double d = r * (0.45 + random.nextDouble() * 0.25);
            blobs[k] = new double[] {c + Math.cos(a) * d, (random.nextDouble() - 0.35) * ry, c + Math.sin(a) * d, r * 0.62, ry * 0.78};
        }
        int reach = (int) Math.ceil(r * 1.35) + 1;
        int up = (int) Math.ceil(ry * 1.3) + 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = -up; dy <= up; dy++) {
            for (int dx = -reach; dx <= reach + (dbl ? 1 : 0); dx++) {
                for (int dz = -reach; dz <= reach + (dbl ? 1 : 0); dz++) {
                    double best = 9.0;
                    for (double[] b : blobs) {
                        double ex = (dx - b[0]) / b[3];
                        double ey = (dy - b[1]) / b[4];
                        double ez = (dz - b[2]) / b[3];
                        best = Math.min(best, ex * ex + ey * ey + ez * ez);
                    }
                    if (best <= 1.0 - random.nextFloat() * 0.24) {
                        leaves.add(pos.setWithOffset(center, dx, dy, dz));
                    }
                }
            }
        }
        hang(leaves, random, center, reach, up, -up, this.hang, 2, r * 0.45, dbl);
        leaves.place(level, setter, random, tree, attachment);
    }
}
