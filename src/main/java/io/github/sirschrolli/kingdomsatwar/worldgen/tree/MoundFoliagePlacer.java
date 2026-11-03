package io.github.sirschrolli.kingdomsatwar.worldgen.tree;

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
 * Ein Busch: ein Hügel aus Laub, der bis auf den Boden reicht und den kurzen Stamm ganz verdeckt – unten am breitesten,
 * oben gewölbt, mit zerfranstem Rand und hier und da einem Zweig, der oben herausschaut.
 *
 * <p>Der Hügel beginnt eine Schicht unter dem Anhängepunkt, also auf Höhe des obersten Stammblocks (bei einem Stamm aus
 * einem Block: auf dem Boden), und ist {@code height} Schichten hoch.
 */
public class MoundFoliagePlacer extends ShapedFoliagePlacer {
    public static final MapCodec<MoundFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(i -> foliagePlacerParts(i).and(
            IntProviders.codec(1, 5).fieldOf("height").forGetter(p -> p.height)
    ).apply(i, MoundFoliagePlacer::new));

    private final IntProvider height;

    public MoundFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height) {
        super(radius, offset);
        this.height = height;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return ModTrees.MOUND_FOLIAGE.get();
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
        BlockPos base = attachment.pos().above(offset).below();
        double c = dbl ? 0.5 : 0.0;
        double r = Math.max(1, leafRadius + attachment.radiusOffsetXZ()) + 0.45;
        int h = Math.max(1, foliageHeight);
        // der Hügel und ein, zwei kleinere Buckel daneben: so wird der Umriss unregelmäßig wie bei einem echten Busch
        int lobes = 1 + random.nextInt(2);
        double[][] mounds = new double[lobes + 1][];
        mounds[0] = new double[] {c, c, r, h};
        double a0 = random.nextDouble() * Math.PI * 2.0;
        for (int k = 1; k <= lobes; k++) {
            double a = a0 + k * Math.PI * (0.8 + random.nextDouble() * 0.4);
            double d = r * (0.55 + random.nextDouble() * 0.25);
            mounds[k] = new double[] {c + Math.cos(a) * d, c + Math.sin(a) * d, r * (0.55 + random.nextDouble() * 0.2), Math.max(1, h - 1)};
        }
        int reach = (int) Math.ceil(r * 1.6) + 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy < h; dy++) {
            for (int dx = -reach; dx <= reach + (dbl ? 1 : 0); dx++) {
                for (int dz = -reach; dz <= reach + (dbl ? 1 : 0); dz++) {
                    boolean inside = false;
                    for (double[] m : mounds) {
                        if (dy >= m[3]) {
                            continue;
                        }
                        double t = dy / (m[3] + 0.35);
                        double rr = m[2] * Math.sqrt(Math.max(0.0, 1.0 - t * t));
                        double ex = dx - m[0];
                        double ez = dz - m[1];
                        if (Math.sqrt(ex * ex + ez * ez) <= rr + 0.3 - random.nextFloat() * 0.65) {
                            inside = true;
                            break;
                        }
                    }
                    if (inside) {
                        leaves.add(pos.setWithOffset(base, dx, dy, dz));
                    }
                }
            }
        }
        // ein, zwei Zweige, die oben herausschauen
        int tufts = random.nextInt(3);
        for (int k = 0; k < tufts; k++) {
            int dx = random.nextInt(3) - 1;
            int dz = random.nextInt(3) - 1;
            leaves.add(pos.setWithOffset(base, dx, h, dz));
        }
        leaves.place(level, setter, random, tree, attachment);
    }
}
