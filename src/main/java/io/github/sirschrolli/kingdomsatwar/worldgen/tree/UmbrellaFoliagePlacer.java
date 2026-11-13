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
 * Ein Laubschirm: flach und weit, unten am breitesten, oben zu einer niedrigen Kuppel gewölbt, mit ausgefranstem Rand –
 * die Krone der Akazie, die Polster der Kiefer und die Etagen des Urwaldbaums.
 */
public class UmbrellaFoliagePlacer extends ShapedFoliagePlacer {
    public static final MapCodec<UmbrellaFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(i -> foliagePlacerParts(i).and(
            IntProviders.codec(1, 6).fieldOf("height").forGetter(p -> p.height)
    ).apply(i, UmbrellaFoliagePlacer::new));

    private final IntProvider height;

    public UmbrellaFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height) {
        super(radius, offset);
        this.height = height;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return ModTrees.UMBRELLA_FOLIAGE.get();
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
        double r = Math.max(1, leafRadius + attachment.radiusOffsetXZ()) + 0.6;
        int h = Math.max(1, foliageHeight);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        // unten ein kleiner Kranz um das Holzende
        for (int dx = -1; dx <= 1 + (dbl ? 1 : 0); dx++) {
            for (int dz = -1; dz <= 1 + (dbl ? 1 : 0); dz++) {
                if (Math.abs(dx - c) + Math.abs(dz - c) <= 1.5) {
                    leaves.add(pos.setWithOffset(base, dx, -1, dz));
                }
            }
        }
        for (int dy = 0; dy < h; dy++) {
            double rr = dy == 0 ? r : r - dy * (r / h) * 0.85 - 0.3;
            int reach = (int) Math.ceil(rr) + 1;
            for (int dx = -reach; dx <= reach + (dbl ? 1 : 0); dx++) {
                for (int dz = -reach; dz <= reach + (dbl ? 1 : 0); dz++) {
                    double ex = dx - c;
                    double ez = dz - c;
                    if (Math.sqrt(ex * ex + ez * ez) > rr + 0.3 - random.nextFloat() * 0.6) {
                        continue;
                    }
                    leaves.add(pos.setWithOffset(base, dx, dy, dz));
                }
            }
        }
        leaves.place(level, setter, random, tree, attachment);
    }
}
