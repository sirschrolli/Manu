package io.github.sirschrolli.kingdomsatwar.worldgen.tree;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

/**
 * Gemeinsames der eigenen Laubformen: Eine Krone wird erst als Form gesammelt und dann vom Holz des Baums aus Schicht um
 * Schicht durch das Laub hindurch gesetzt – genau so, wie das Spiel später die Entfernung jedes Blatts zum Holz zählt. Was
 * auf diesem Weg mehr als sechs Schritte braucht, bleibt weg; so verdorrt kein Blatt, auch nicht am zerfransten Rand oder
 * an der Spitze eines hängenden Zweigs.
 */
abstract class ShapedFoliagePlacer extends FoliagePlacer {
    private static final Direction[] DIRECTIONS = Direction.values();
    /** Was der Stamm des Baums, der gerade wächst, gesetzt hat (Holz, Äste, Wurzeln, Erde darunter) – in dieser Reihenfolge. */
    private static final ThreadLocal<Trunk> TRUNK = ThreadLocal.withInitial(Trunk::new);

    private static final class Trunk {
        final LongArrayList order = new LongArrayList();
        final LongOpenHashSet set = new LongOpenHashSet();
    }

    ShapedFoliagePlacer(IntProvider radius, IntProvider offset) {
        super(radius, offset);
    }

    /**
     * Merkt sich alles, was der Stamm setzt: Das Spiel zählt die Entfernung des Laubs von genau diesen Blöcken aus. Der
     * Stammsetzer ruft das zu Beginn jedes Baums auf.
     */
    static BiConsumer<BlockPos, BlockState> recordTrunk(BiConsumer<BlockPos, BlockState> trunk) {
        Trunk record = TRUNK.get();
        record.order.clear();
        record.set.clear();
        return (pos, state) -> {
            long key = pos.asLong();
            if (record.set.add(key)) {
                record.order.add(key);
            }
            trunk.accept(pos, state);
        };
    }

    /** Das Holz, an dem das Laub hängt: der Block unter dem Anhängepunkt. */
    static BlockPos anchor(FoliageAttachment attachment) {
        return attachment.pos().below();
    }

    /** Die Blätter einer Krone: erst als Form gesammelt, dann vom Holz aus gesetzt. */
    static final class Leaves {
        private final LongOpenHashSet wanted = new LongOpenHashSet();

        void add(BlockPos pos) {
            this.wanted.add(pos.asLong());
        }

        boolean has(BlockPos pos) {
            return this.wanted.contains(pos.asLong());
        }

        /**
         * Setzt die gesammelten Blätter, Schritt um Schritt vom Holz aus durch das Laub: zuerst die, die am Holz liegen, dann
         * deren Nachbarn, bis sechs Schritte. Ein Platz, der nicht frei ist, sperrt den Weg dahinter.
         */
        void place(WorldGenLevel level, FoliageSetter setter, RandomSource random, TreeFeature tree, FoliageAttachment attachment) {
            LongArrayList sources = sources(level, attachment);
            LongOpenHashSet seen = new LongOpenHashSet();
            LongArrayList frontier = new LongArrayList();
            for (int i = 0; i < sources.size(); i++) {
                long log = sources.getLong(i);
                for (Direction d : DIRECTIONS) {
                    long n = BlockPos.offset(log, d);
                    if (this.wanted.contains(n) && seen.add(n)) {
                        frontier.add(n);
                    }
                }
            }
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int distance = 1; distance <= 6 && !frontier.isEmpty(); distance++) {
                LongArrayList next = new LongArrayList();
                for (int i = 0; i < frontier.size(); i++) {
                    long p = frontier.getLong(i);
                    if (!tryPlaceLeaf(level, setter, random, tree, pos.set(p))) {
                        continue;
                    }
                    if (distance < 6) {
                        for (Direction d : DIRECTIONS) {
                            long n = BlockPos.offset(p, d);
                            if (this.wanted.contains(n) && seen.add(n)) {
                                next.add(n);
                            }
                        }
                    }
                }
                frontier = next;
            }
        }

        /**
         * Von wo aus gezählt wird: alles, was der Stamm dieses Baums gesetzt hat – oder, wenn die Krone auf einem fremden
         * Stamm sitzt, das zusammenhängende Holz um den Anhängepunkt.
         */
        private static LongArrayList sources(WorldGenLevel level, FoliageAttachment attachment) {
            Trunk trunk = TRUNK.get();
            BlockPos anchor = anchor(attachment);
            if (trunk.set.contains(anchor.asLong())) {
                return trunk.order;
            }
            LongArrayList found = new LongArrayList();
            LongOpenHashSet seen = new LongOpenHashSet();
            if (level.isStateAtPosition(anchor, s -> s.is(BlockTags.LOGS))) {
                found.add(anchor.asLong());
                seen.add(anchor.asLong());
            }
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int i = 0; i < found.size() && found.size() < 128; i++) {
                long p = found.getLong(i);
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            long n = BlockPos.offset(p, dx, dy, dz);
                            pos.set(n);
                            if (Math.abs(pos.getX() - anchor.getX()) <= 8 && Math.abs(pos.getZ() - anchor.getZ()) <= 8
                                    && Math.abs(pos.getY() - anchor.getY()) <= 12 && seen.add(n)
                                    && level.isStateAtPosition(pos, s -> s.is(BlockTags.LOGS))) {
                                found.add(n);
                            }
                        }
                    }
                }
            }
            return found;
        }
    }

    /**
     * Hängende Spitzen: unter dem untersten Laub jeder Säule (außerhalb des Halbmessers {@code inner}) hängt mit der
     * Wahrscheinlichkeit {@code chance} ein Zweig von bis zu {@code maxLength} Blättern herab.
     */
    static void hang(Leaves leaves, RandomSource random, BlockPos center, int radius, int top, int bottom, float chance, int maxLength,
            double inner, boolean doubleTrunk) {
        if (chance <= 0.0f) {
            return;
        }
        double c = doubleTrunk ? 0.5 : 0.0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius + (doubleTrunk ? 1 : 0); dx++) {
            for (int dz = -radius; dz <= radius + (doubleTrunk ? 1 : 0); dz++) {
                if (Math.sqrt((dx - c) * (dx - c) + (dz - c) * (dz - c)) < inner) {
                    continue;
                }
                for (int dy = bottom; dy <= top; dy++) {
                    pos.setWithOffset(center, dx, dy, dz);
                    if (!leaves.has(pos)) {
                        continue;
                    }
                    if (leaves.has(pos.below()) || random.nextFloat() >= chance) {
                        break;
                    }
                    int length = 1 + random.nextInt(maxLength);
                    for (int k = 1; k <= length; k++) {
                        leaves.add(pos.below(k));
                    }
                    break;
                }
            }
        }
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int dx, int y, int dz, int currentRadius, boolean doubleTrunk) {
        return false;
    }
}
