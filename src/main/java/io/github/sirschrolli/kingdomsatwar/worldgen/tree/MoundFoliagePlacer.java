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
        // etwas schief: der Hügel lehnt sich ein wenig zu einer Seite
        double sx = (random.nextDouble() - 0.5) * 0.6;
        double sz = (random.nextDouble() - 0.5) * 0.6;
        int reach = (int) Math.ceil(r) + 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy < h; dy++) {
            double t = dy / (h + 0.35);
            double rr = r * Math.sqrt(Math.max(0.0, 1.0 - t * t));
            for (int dx = -reach; dx <= reach + (dbl ? 1 : 0); dx++) {
                for (int dz = -reach; dz <= reach + (dbl ? 1 : 0); dz++) {
                    double ex = dx - c - sx * dy;
                    double ez = dz - c - sz * dy;
                    if (Math.sqrt(ex * ex + ez * ez) > rr + 0.25 - random.nextFloat() * 0.5) {
                        continue;
                    }
                    leaves.add(pos.setWithOffset(base, dx, dy, dz));
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
