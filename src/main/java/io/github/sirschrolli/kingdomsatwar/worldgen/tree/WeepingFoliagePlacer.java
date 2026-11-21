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
 * Eine hängende Krone: eine gewölbte Kuppel, von deren Rand Zweige wie Vorhänge herabhängen – die Kirsche, die im
 * Frühling ihre Blüten herabregnen lässt, und die Sumpfeiche mit ihren nassen, schweren Zweigen.
 */
public class WeepingFoliagePlacer extends ShapedFoliagePlacer {
    public static final MapCodec<WeepingFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(i -> foliagePlacerParts(i).and(i.group(
            IntProviders.codec(1, 8).fieldOf("height").forGetter(p -> p.height),
            Codec.floatRange(0.0f, 1.0f).fieldOf("hang").forGetter(p -> p.hang)
    )).apply(i, WeepingFoliagePlacer::new));

    private final IntProvider height;
    private final float hang;

    public WeepingFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height, float hang) {
        super(radius, offset);
        this.height = height;
        this.hang = hang;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return ModTrees.WEEPING_FOLIAGE.get();
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
        BlockPos base = attachment.pos().above(offset);
        double c = dbl ? 0.5 : 0.0;
        double r = Math.max(1, leafRadius + attachment.radiusOffsetXZ()) + 0.5;
        int h = Math.max(1, foliageHeight);
        int reach = (int) Math.ceil(r) + 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = -1; dy <= h; dy++) {
            double k = dy < 0 ? 0.75 : Math.sqrt(Math.max(0.0, 1.0 - Math.pow(dy / (h + 0.6), 2.0)));
            double rr = r * k;
            for (int dx = -reach; dx <= reach + (dbl ? 1 : 0); dx++) {
                for (int dz = -reach; dz <= reach + (dbl ? 1 : 0); dz++) {
                    double ex = dx - c;
                    double ez = dz - c;
                    if (Math.sqrt(ex * ex + ez * ez) > rr + 0.3 - random.nextFloat() * 0.5) {
                        continue;
                    }
                    leaves.add(pos.setWithOffset(base, dx, dy, dz));
                }
            }
        }
        // die herabhängenden Zweige am Rand
        hang(leaves, random, base, reach, h, -1, this.hang, 3, r * 0.6, dbl);
        leaves.place(level, setter, random, tree, attachment);
    }
}
