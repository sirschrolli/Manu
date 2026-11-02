package io.github.sirschrolli.kingdomsatwar.worldgen.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

/**
 * Ein Stamm, der sich in Äste teilt – die Grundform der eigenen Bäume: Der Stamm (einfach oder 2 × 2) kann sich neigen
 * und rückt dabei an ein, zwei Stellen um einen Block zur Seite; am Fuß spreizen sich Wurzelanläufe; im oberen Teil gehen
 * Äste in einer Spirale nach außen und oben, an ihren Enden (und auf Wunsch oben am Stamm) sitzt das Laub. Aus wenigen
 * Werten entstehen so ganz verschiedene Bäume: die breite Eiche, die schlanke Birke, die Schirmakazie, die Kiefer mit
 * kahlem Stamm, der Urwaldriese mit Brettwurzeln.
 */
public class BranchedTrunkPlacer extends TrunkPlacer {
    /**
     * Die Gestalt: Zahl und Länge der Äste, ab welchem Teil der Höhe sie beginnen, wie steil sie steigen (Blöcke je
     * Schritt nach außen), wie oft der Stamm zur Seite rückt, wie viele Wurzelanläufe er hat, ob oben am Stamm eine Krone
     * sitzt und ob der Stamm 2 × 2 dick ist.
     */
    public record Shape(IntProvider branches, IntProvider branchLength, float branchStart, float rise, IntProvider lean, IntProvider roots,
            boolean crown, boolean thick) {
        public static final MapCodec<Shape> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                IntProviders.codec(0, 8).fieldOf("branches").forGetter(Shape::branches),
                IntProviders.codec(1, 8).fieldOf("branch_length").forGetter(Shape::branchLength),
                Codec.floatRange(0.0f, 1.0f).fieldOf("branch_start").forGetter(Shape::branchStart),
                Codec.floatRange(0.0f, 3.0f).fieldOf("rise").forGetter(Shape::rise),
                IntProviders.codec(0, 3).fieldOf("lean").forGetter(Shape::lean),
                IntProviders.codec(0, 8).fieldOf("roots").forGetter(Shape::roots),
                Codec.BOOL.fieldOf("crown").forGetter(Shape::crown),
                Codec.BOOL.fieldOf("thick").forGetter(Shape::thick)
        ).apply(i, Shape::new));
    }

    public static final MapCodec<BranchedTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(i -> trunkPlacerParts(i)
            .and(Shape.CODEC.forGetter(p -> p.shape))
            .apply(i, BranchedTrunkPlacer::new));

    private final Shape shape;

    public BranchedTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, Shape shape) {
        super(baseHeight, heightRandA, heightRandB);
        this.shape = shape;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return ModTrees.BRANCHED_TRUNK.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunk, RandomSource random,
            int height, BlockPos origin, TreeFeature tree) {
        // was der Stamm setzt, merken sich die Kronen: von dort aus zählt das Spiel die Entfernung des Laubs
        trunk = ShapedFoliagePlacer.recordTrunk(trunk);
        int w = this.shape.thick() ? 2 : 1;
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < w; dz++) {
                placeBelowTrunkBlock(level, trunk, random, origin.offset(dx, -1, dz), tree);
            }
        }
        // der Stamm, an den Knien um einen Block zur Seite gerückt
        int lean = this.shape.lean().sample(random);
        Direction leanDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Set<Integer> knees = new HashSet<>();
        for (int k = 1; k <= lean; k++) {
            knees.add(Mth.clamp((int) Math.round(height * (0.35 + 0.45 * k / (lean + 0.5))), 2, Math.max(2, height - 2)));
        }
        int[] offX = new int[height + 1];
        int[] offZ = new int[height + 1];
        int ox = 0;
        int oz = 0;
        for (int y = 0; y < height; y++) {
            if (knees.contains(y)) {
                // das Knie: zwei liegende Stücke, so biegt sich der Stamm statt zu springen
                Direction.Axis bend = leanDir.getAxis();
                this.column(level, trunk, random, origin.offset(ox, y, oz), tree, w, bend);
                ox += leanDir.getStepX();
                oz += leanDir.getStepZ();
                offX[y] = ox;
                offZ[y] = oz;
                this.column(level, trunk, random, origin.offset(ox, y, oz), tree, w, bend);
                continue;
            }
            offX[y] = ox;
            offZ[y] = oz;
            this.column(level, trunk, random, origin.offset(ox, y, oz), tree, w, Direction.Axis.Y);
        }
        offX[height] = ox;
        offZ[height] = oz;
        this.roots(level, trunk, random, origin, tree, w);
        List<FoliagePlacer.FoliageAttachment> attachments = new ArrayList<>();
        // die Äste: in einer Spirale um den Stamm, von unten nach oben
        int count = this.shape.branches().sample(random);
        double a0 = random.nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < count; i++) {
            double angle = a0 + i * Math.PI * 2.0 / count + (random.nextDouble() - 0.5) * (Math.PI / Math.max(2, count));
            double f = count == 1 ? 0.5 : i / (double) (count - 1);
            float s0 = this.shape.branchStart();
            int start = Mth.clamp((int) Math.round(height * (s0 + (1.0 - s0) * 0.85 * f)) - random.nextInt(2), 1, height - 1);
            int length = this.shape.branchLength().sample(random);
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            double cx = offX[start] + (w - 1) * 0.5;
            double cz = offZ[start] + (w - 1) * 0.5;
            double out = (w - 1) * 0.5;
            Direction.Axis axis = this.shape.rise() > 1.0f ? Direction.Axis.Y : Math.abs(dx) > Math.abs(dz) ? Direction.Axis.X : Direction.Axis.Z;
            BlockPos end = null;
            for (int s = 1; s <= length; s++) {
                double t = out + s;
                BlockPos p = origin.offset((int) Math.round(cx + dx * t), start + (int) Math.round(this.shape.rise() * s),
                        (int) Math.round(cz + dz * t));
                if (!this.placeLog(level, trunk, random, p, tree, state -> state.trySetValue(RotatedPillarBlock.AXIS, axis))) {
                    break;
                }
                end = p;
            }
            if (end != null) {
                attachments.add(new FoliagePlacer.FoliageAttachment(end.above(), 0, false));
            }
        }
        if (this.shape.crown() || attachments.isEmpty()) {
            // über mehreren Ästen wird die Krone oben am Stamm ein wenig größer: so schließt sie sich zu einem Ganzen
            int bonus = attachments.size() >= 2 ? 1 : 0;
            attachments.add(new FoliagePlacer.FoliageAttachment(origin.offset(offX[height], height, offZ[height]), bonus, this.shape.thick()));
        }
        return attachments;
    }

    private void column(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunk, RandomSource random, BlockPos pos, TreeFeature tree, int w,
            Direction.Axis axis) {
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < w; dz++) {
                this.placeLog(level, trunk, random, pos.offset(dx, 0, dz), tree, state -> state.trySetValue(RotatedPillarBlock.AXIS, axis));
            }
        }
    }

    /** Wurzelanläufe am Fuß: liegende Stämme, die sich in den Boden spreizen (nur, wo fester Grund darunter ist). */
    private void roots(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunk, RandomSource random, BlockPos origin, TreeFeature tree, int w) {
        int n = this.shape.roots().sample(random);
        if (n <= 0) {
            return;
        }
        List<Direction> dirs = new ArrayList<>(List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST));
        for (int i = dirs.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Direction d = dirs.get(i);
            dirs.set(i, dirs.get(j));
            dirs.set(j, d);
        }
        for (int k = 0; k < n; k++) {
            Direction d = dirs.get(k % 4);
            int side = w == 2 ? (k / 4) % 2 : 0;
            int sx = w == 2 ? (d.getStepX() > 0 ? 1 : d.getStepX() < 0 ? 0 : side) : 0;
            int sz = w == 2 ? (d.getStepZ() > 0 ? 1 : d.getStepZ() < 0 ? 0 : side) : 0;
            BlockPos first = origin.offset(sx, 0, sz).relative(d);
            int length = w == 2 ? 2 + random.nextInt(2) : 1 + random.nextInt(2);
            for (int s = 0; s < length; s++) {
                BlockPos p = first.relative(d, s);
                if (TreeFeature.validTreePos(level, p.below())) {
                    break;
                }
                if (!this.placeLog(level, trunk, random, p, tree, state -> state.trySetValue(RotatedPillarBlock.AXIS, d.getAxis()))) {
                    break;
                }
                if (s == 0 && w == 2 && random.nextBoolean()) {
                    this.placeLog(level, trunk, random, p.above(), tree);
                }
            }
        }
    }
}
