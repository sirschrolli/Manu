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
 * Ein Nadelkleid in Stufen: Von der Spitze an wird das Laub in deutlich abgesetzten Etagen nach unten breiter; jede Etage
 * ist unten am weitesten und hängt an ihrem Rand ein wenig herab, darüber zieht sie sich zum Stamm ein – so bekommt die
 * Fichte ihr pagodenhaftes Profil. Unten bleibt ein Stück Stamm kahl.
 */
public class TieredFoliagePlacer extends ShapedFoliagePlacer {
    public static final MapCodec<TieredFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(i -> foliagePlacerParts(i).and(i.group(
            IntProviders.codec(0, 24).fieldOf("bare").forGetter(p -> p.bare),
            Codec.intRange(2, 4).fieldOf("spacing").forGetter(p -> p.spacing),
            Codec.floatRange(0.0f, 1.0f).fieldOf("droop").forGetter(p -> p.droop)
    )).apply(i, TieredFoliagePlacer::new));

    private final IntProvider bare;
    private final int spacing;
    private final float droop;

    public TieredFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider bare, int spacing, float droop) {
        super(radius, offset);
        this.bare = bare;
        this.spacing = spacing;
        this.droop = droop;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return ModTrees.TIERED_FOLIAGE.get();
    }

    @Override
    public int foliageHeight(RandomSource random, int treeHeight, TreeFeature tree) {
        return Math.max(4, treeHeight - this.bare.sample(random));
    }

    @Override
    protected void createFoliage(WorldGenLevel level, FoliageSetter setter, RandomSource random, TreeFeature tree, int treeHeight,
            FoliageAttachment attachment, int foliageHeight, int leafRadius, int offset) {
        boolean dbl = attachment.doubleTrunk();
        BlockPos top = attachment.pos().above(offset);
        double c = dbl ? 0.5 : 0.0;
        int maxR = Math.max(1, leafRadius + attachment.radiusOffsetXZ());
        Leaves leaves = new Leaves();
        // die Spitze
        leaves.add(top);
        if (random.nextBoolean()) {
            leaves.add(top.above());
        }
        int tiers = Math.max(1, (foliageHeight - 1) / this.spacing);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int depth = 1; depth <= foliageHeight; depth++) {
            int tier = (depth - 1) / this.spacing;
            int inTier = (depth - 1) % this.spacing;
            double tierR = 1.2 + (maxR - 0.7) * Math.min(1.0, (tier + 1) / (double) tiers);
            double r = inTier == this.spacing - 1 ? tierR : Math.max(0.6, tierR - (this.spacing - 1 - inTier) * 1.15);
            int y = -depth + 1;
            int reach = (int) Math.ceil(r) + 1;
            for (int dx = -reach; dx <= reach + (dbl ? 1 : 0); dx++) {
                for (int dz = -reach; dz <= reach + (dbl ? 1 : 0); dz++) {
                    double ex = dx - c;
                    double ez = dz - c;
                    double d = Math.sqrt(ex * ex + ez * ez);
                    if (d > r + 0.35 - random.nextFloat() * 0.45) {
                        continue;
                    }
                    leaves.add(pos.setWithOffset(top, dx, y, dz));
                    // am Rand der untersten Lage einer Etage hängen die Zweige herab
                    if (inTier == this.spacing - 1 && d > r - 1.2 && random.nextFloat() < this.droop) {
                        leaves.add(pos.below());
                    }
                }
            }
        }
        leaves.place(level, setter, random, tree, attachment);
    }
}
