package io.github.sirschrolli.kingdomsatwar.fae.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.fae.FaeTerrain;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Der Mondhain: eine runde Lichtung im Feenreich, auf der Noctua, die Mottenkönigin, in ihrem Kokon schläft.
 * <ul>
 *     <li>Der Boden wird eingeebnet (Moos und Feengras) und geht am Rand sanft ins Gelände über – auch über Wasser.</li>
 *     <li>Ein Kreis aus fünf Mondstein-Trilithen (zwei Säulen mit leuchtenden Kapitellen und einem Deckstein, von dem
 *     Leuchtranken hängen); einer ist eingestürzt.</li>
 *     <li>Ein Pfadring aus Feensteinziegeln mit vier Wegen zur Mitte, ein Ring aus Laternenpilzen.</li>
 *     <li>In der Mitte ein zweistufiger Altar mit gemeißeltem Mondstein und vier Lichtsäulen, darauf der Kokon.</li>
 *     <li>Ringsum Glimmerbäume, behängt mit Mottenseide und langen Seidenfäden.</li>
 * </ul>
 * Wo die Mondhaine liegen, bestimmt {@link io.github.sirschrolli.kingdomsatwar.fae.Mondhain} (einer pro Feld, immer
 * in der Mitte eines Chunks).
 */
public record MondhainFeature() implements Feature {
    public static final MapCodec<MondhainFeature> CODEC = MapCodec.unit(MondhainFeature::new);
    /** Bis hierher ist die Lichtung eben … */
    static final double INNER = 14.5;
    /** … bis hierher geht sie in das Gelände über. */
    static final double OUTER = 21.5;
    /** Radius des Steinkreises. */
    static final double HENGE = 12.5;
    /** Höhe des Kokons über dem Boden der Lichtung. */
    public static final int COCOON_HEIGHT = 3;

    @Override
    public MapCodec<MondhainFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int cx = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(origin.getX())) + 8;
        int cz = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(origin.getZ())) + 8;
        int floor = floorHeight(level, cx, cz);
        new Grove(level, generator, random, cx, cz, floor).build();
        return true;
    }

    /** Höhe der Lichtung: der Median der Bodenhöhen ringsum, aber mindestens zwei Blöcke über dem Wasser. */
    static int floorHeight(WorldGenLevel level, int cx, int cz) {
        List<Integer> heights = new ArrayList<>();
        for (int dx = -12; dx <= 12; dx += 3) {
            for (int dz = -12; dz <= 12; dz += 3) {
                if (dx * dx + dz * dz <= 144) {
                    heights.add(groundAt(level, cx + dx, cz + dz));
                }
            }
        }
        Collections.sort(heights);
        return Math.max(FaeTerrain.SEA_LEVEL + 1, heights.get(heights.size() / 2));
    }

    /** Oberster Boden-Block der Spalte (unter Wasser, Pflanzen, Pilzen und Laub). */
    static int groundAt(WorldGenLevel level, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, FaeBuild.surface(level, x, z) - 1, z);
        while (pos.getY() > level.getMinY() + 2 && !FaeBuild.ground(level.getBlockState(pos))) {
            pos.move(0, -1, 0);
        }
        return pos.getY();
    }

    private static final class Grove {
        private final WorldGenLevel level;
        private final ChunkGenerator generator;
        private final RandomSource random;
        private final int cx;
        private final int cz;
        private final int floor;
        private final BlockPos origin;
        /** Spalten, auf denen nichts wachsen soll (Wege, Altar, Säulen). */
        private final Set<Long> reserved = new HashSet<>();

        Grove(WorldGenLevel level, ChunkGenerator generator, RandomSource random, int cx, int cz, int floor) {
            this.level = level;
            this.generator = generator;
            this.random = random;
            this.cx = cx;
            this.cz = cz;
            this.floor = floor;
            this.origin = new BlockPos(cx, floor, cz);
        }

        void build() {
            this.terrain();
            this.paths();
            this.altar();
            this.henge();
            this.mushroomRing();
            this.trees();
            this.silk();
            this.plants();
            this.cocoon();
        }

        // ------------------------------------------------------------ Hilfen

        private BlockPos at(int dx, int y, int dz) {
            return new BlockPos(this.cx + dx, y, this.cz + dz);
        }

        private void set(BlockPos pos, BlockState state) {
            FaeBuild.force(this.level, this.origin, pos, state);
        }

        private void reserve(int dx, int dz) {
            this.reserved.add(BlockPos.asLong(this.cx + dx, 0, this.cz + dz));
        }

        private boolean isReserved(int dx, int dz) {
            return this.reserved.contains(BlockPos.asLong(this.cx + dx, 0, this.cz + dz));
        }

        private static BlockState state(java.util.function.Supplier<? extends net.minecraft.world.level.block.Block> block) {
            return block.get().defaultBlockState();
        }

        private BlockState weatheredBrick() {
            float r = this.random.nextFloat();
            return r < 0.4f ? state(FaeBlocks.MOSSY_FAESTONE_BRICKS) : r < 0.85f ? state(FaeBlocks.FAESTONE_BRICKS.base())
                    : state(FaeBlocks.FAESTONE.base());
        }

        private BlockState moonstone() {
            return this.random.nextFloat() < 0.12f ? state(FaeBlocks.MOSSY_FAESTONE_BRICKS) : state(FaeBlocks.MOONSTONE_BRICKS.base());
        }

        private static BlockState slab(java.util.function.Supplier<? extends SlabBlock> slab) {
            return slab.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        }

        // ------------------------------------------------------------ Gelände

        /** Ebnet die Lichtung ein (abtragen oder aufschütten) und lässt sie am Rand sanft ins Gelände übergehen. */
        private void terrain() {
            int reach = (int) Math.ceil(OUTER);
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    double r = Math.sqrt(dx * dx + dz * dz);
                    if (r > OUTER) {
                        continue;
                    }
                    int x = this.cx + dx;
                    int z = this.cz + dz;
                    if (!FaeBuild.inReach(this.origin, new BlockPos(x, this.floor, z))) {
                        continue;
                    }
                    int top = FaeBuild.surface(this.level, x, z) - 1;
                    int ground = groundAt(this.level, x, z);
                    double t = r <= INNER ? 0.0 : smooth((r - INNER) / (OUTER - INNER));
                    int target = (int) Math.round(Mth.lerp(t, this.floor, ground));
                    boolean water = top > ground && this.level.getFluidState(new BlockPos(x, top, z)).is(FluidTags.WATER);
                    boolean underwater = water && target < top;
                    if (target == ground && !water && t > 0.0) {
                        continue;                                   // hier bleibt das Gelände, wie es ist
                    }
                    // abtragen (alles über der neuen Oberkante, auch Felsen und Pflanzen der alten)
                    if (ground > target || !underwater) {
                        // Wasser über dem alten Grund verschwindet mit; Pilzhüte und Laub darüber bleiben, wie sie sind
                        int clearTo = water ? Math.max(top, ground) + 1 : ground + 4;
                        for (int y = target + 1; y <= clearTo; y++) {
                            BlockPos pos = new BlockPos(x, y, z);
                            BlockState old = this.level.getBlockState(pos);
                            if (old.isAir() || old.is(BlockTags.LEAVES) || old.is(BlockTags.LOGS)) {
                                continue;
                            }
                            if (underwater && y <= top) {
                                this.set(pos, Blocks.WATER.defaultBlockState());
                            } else {
                                this.set(pos, Blocks.AIR.defaultBlockState());
                            }
                        }
                    }
                    // aufschütten
                    for (int y = ground + 1; y <= target; y++) {
                        this.set(new BlockPos(x, y, z), y < target - 3 ? state(FaeBlocks.FAESTONE.base()) : state(FaeBlocks.FAE_SOIL));
                    }
                    if (underwater) {
                        continue;                                   // Seegrund: die alte Oberfläche bleibt
                    }
                    // Oberfläche: Moos und Feengras, darunter Feenerde
                    BlockState surface = r <= INNER && this.random.nextFloat() < 0.32f ? state(FaeBlocks.FAE_MOSS) : state(FaeBlocks.FAE_GRASS);
                    this.set(new BlockPos(x, target, z), surface);
                    for (int y = target - 1; y >= target - 2; y--) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!FaeBuild.soil(this.level.getBlockState(pos))) {
                            this.set(pos, state(FaeBlocks.FAE_SOIL));
                        }
                    }
                }
            }
        }

        private static double smooth(double t) {
            t = Mth.clamp(t, 0.0, 1.0);
            return t * t * (3.0 - 2.0 * t);
        }

        // ------------------------------------------------------------ Wege

        /** Ein Pfadring aus alten Ziegeln und vier Wege zur Mitte. */
        private void paths() {
            for (int dx = -11; dx <= 11; dx++) {
                for (int dz = -11; dz <= 11; dz++) {
                    double r = Math.sqrt(dx * dx + dz * dz);
                    boolean ring = r >= 9.0 && r < 10.4;
                    boolean spoke = r >= 4.4 && r < 9.0 && (Math.abs(dx) <= 1 || Math.abs(dz) <= 1);
                    if (!ring && !spoke) {
                        continue;
                    }
                    this.reserve(dx, dz);
                    if (this.random.nextFloat() < (ring ? 0.86f : 0.78f)) {
                        this.set(this.at(dx, this.floor, dz), this.weatheredBrick());
                    }
                }
            }
        }

        // ------------------------------------------------------------ Altar

        /** Zweistufiger Altar: unten Feensteinziegel mit Stufenkranz, oben Mondstein, in der Mitte gemeißelter Mondstein. */
        private void altar() {
            int y1 = this.floor + 1;
            int y2 = this.floor + 2;
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    double r = Math.sqrt(dx * dx + dz * dz);
                    if (r > 4.5) {
                        continue;
                    }
                    this.reserve(dx, dz);
                    this.set(this.at(dx, this.floor, dz), this.weatheredBrick());
                    if (r <= 3.4) {
                        this.set(this.at(dx, y1, dz), state(FaeBlocks.FAESTONE_BRICKS.base()));
                    } else {
                        this.set(this.at(dx, y1, dz), slab(FaeBlocks.FAESTONE_BRICKS.slab()));
                    }
                    if (r <= 2.2) {
                        this.set(this.at(dx, y2, dz), state(FaeBlocks.MOONSTONE_BRICKS.base()));
                    } else if (r <= 2.95) {
                        this.set(this.at(dx, y2, dz), slab(FaeBlocks.MOONSTONE_BRICKS.slab()));
                    }
                }
            }
            this.set(this.at(0, y2, 0), state(FaeBlocks.CHISELED_MOONSTONE));
            // vier Lichtsäulen an den Ecken
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    this.set(this.at(3 * sx, y1, 3 * sz), state(FaeBlocks.FAESTONE_BRICKS.base()));
                    BlockState post = FaeBlocks.MOONSTONE_BRICKS.wall() != null ? state(FaeBlocks.MOONSTONE_BRICKS.wall())
                            : state(FaeBlocks.MOONSTONE_BRICKS.base());
                    this.set(this.at(3 * sx, y2, 3 * sz), post);
                    this.set(this.at(3 * sx, y2 + 1, 3 * sz), state(FaeBlocks.CHISELED_MOONSTONE));
                }
            }
        }

        /** Der Kokon auf dem Altar, daneben ein, zwei Ballen Mottenseide. */
        private void cocoon() {
            BlockPos pos = this.at(0, this.floor + COCOON_HEIGHT, 0);
            this.set(pos, state(FaeBlocks.MOTH_COCOON));
            int[][] corners = {{1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
            int silk = 1 + this.random.nextInt(2);
            for (int i = 0; i < silk; i++) {
                int[] c = corners[this.random.nextInt(corners.length)];
                this.set(this.at(c[0], this.floor + COCOON_HEIGHT, c[1]), state(FaeBlocks.MOTH_SILK));
            }
        }

        // ------------------------------------------------------------ Steinkreis

        /** Fünf Trilithen aus Mondstein (je zwei Säulen und ein Deckstein); einer ist eingestürzt. */
        private void henge() {
            double phase = this.random.nextDouble() * Math.PI * 2.0;
            int fallen = this.random.nextInt(5);
            for (int pair = 0; pair < 5; pair++) {
                double a0 = phase + pair * Math.PI * 2.0 / 5.0;
                double a1 = a0 + Math.PI * 2.0 / 10.0 * 0.9;
                int ax = (int) Math.round(Math.cos(a0) * HENGE);
                int az = (int) Math.round(Math.sin(a0) * HENGE);
                int bx = (int) Math.round(Math.cos(a1) * HENGE);
                int bz = (int) Math.round(Math.sin(a1) * HENGE);
                if (pair == fallen) {
                    this.ruinedTrilithon(ax, az, bx, bz);
                } else {
                    int height = 5 + this.random.nextInt(2);
                    this.pillar(ax, az, height, true);
                    this.pillar(bx, bz, height, true);
                    this.lintel(ax, az, bx, bz, this.floor + height + 2);
                }
            }
        }

        /** Eine Säule: bemooster Fuß, Mondsteinschaft, leuchtendes Kapitell. */
        private void pillar(int dx, int dz, int height, boolean capital) {
            this.reserve(dx, dz);
            this.set(this.at(dx, this.floor, dz), state(FaeBlocks.MOSSY_FAESTONE_BRICKS));
            this.set(this.at(dx, this.floor + 1, dz), state(FaeBlocks.MOSSY_FAESTONE_BRICKS));
            for (int y = 2; y <= height; y++) {
                this.set(this.at(dx, this.floor + y, dz), this.random.nextFloat() < 0.1f ? state(FaeBlocks.MOTH_SILK) : this.moonstone());
            }
            if (capital) {
                this.set(this.at(dx, this.floor + height + 1, dz), state(FaeBlocks.CHISELED_MOONSTONE));
            }
        }

        /** Deckstein zwischen zwei Säulen, ohne Lücken (Schritte nur entlang der Achsen); darunter hängen Leuchtranken. */
        private void lintel(int ax, int az, int bx, int bz, int y) {
            boolean gap = this.random.nextFloat() < 0.25f;
            List<int[]> line = line(ax, az, bx, bz);
            int broken = gap ? 2 + this.random.nextInt(Math.max(1, line.size() - 4)) : -1;
            for (int i = 0; i < line.size(); i++) {
                if (i == broken) {
                    continue;
                }
                int[] p = line.get(i);
                BlockPos pos = this.at(p[0], y, p[1]);
                this.set(pos, state(FaeBlocks.MOONSTONE_BRICKS.base()));
                boolean overPillar = (p[0] == ax && p[1] == az) || (p[0] == bx && p[1] == bz);
                if (!overPillar && this.random.nextFloat() < 0.35f) {
                    FaeBuild.hangVines(this.level, this.origin, pos, 3, this.random);
                }
            }
        }

        /** Eingestürzt: zwei Säulenstümpfe, der Deckstein liegt in Brocken im Gras, ein Kapitell daneben. */
        private void ruinedTrilithon(int ax, int az, int bx, int bz) {
            this.pillar(ax, az, 2 + this.random.nextInt(2), false);
            this.pillar(bx, bz, 1, false);
            // der Deckstein ist nach innen gekippt
            double mx = (ax + bx) / 2.0;
            double mz = (az + bz) / 2.0;
            double len = Math.sqrt(mx * mx + mz * mz);
            int ox = (int) Math.round(-mx / len * 2.5);
            int oz = (int) Math.round(-mz / len * 2.5);
            for (int[] p : line(ax + ox, az + oz, bx + ox, bz + oz)) {
                if (this.random.nextFloat() < 0.7f) {
                    BlockPos pos = this.at(p[0], this.floor + 1, p[1]);
                    this.set(pos, this.random.nextFloat() < 0.35f ? slab(FaeBlocks.MOONSTONE_BRICKS.slab()) : state(FaeBlocks.MOONSTONE_BRICKS.base()));
                    this.reserve(p[0], p[1]);
                }
            }
            this.set(this.at((int) Math.round(mx * 0.75), this.floor + 1, (int) Math.round(mz * 0.75)), state(FaeBlocks.CHISELED_MOONSTONE));
        }

        /** Linie in der Ebene, deren Felder sich immer an einer Seite berühren (Schritte nur entlang der Achsen). */
        private static List<int[]> line(int x0, int z0, int x1, int z1) {
            List<int[]> out = new ArrayList<>();
            int dx = Math.abs(x1 - x0);
            int dz = Math.abs(z1 - z0);
            int sx = Integer.signum(x1 - x0);
            int sz = Integer.signum(z1 - z0);
            int x = x0;
            int z = z0;
            out.add(new int[] {x, z});
            for (int ix = 0, iz = 0; ix < dx || iz < dz; ) {
                if ((0.5 + ix) * dz < (0.5 + iz) * dx) {
                    x += sx;
                    ix++;
                } else {
                    z += sz;
                    iz++;
                }
                out.add(new int[] {x, z});
            }
            return out;
        }

        // ------------------------------------------------------------ Pflanzen

        /** Ein Kreis aus Laternenpilzen um den Altar. */
        private void mushroomRing() {
            for (int dx = -8; dx <= 8; dx++) {
                for (int dz = -8; dz <= 8; dz++) {
                    double r = Math.sqrt(dx * dx + dz * dz);
                    if (r < 6.3 || r >= 7.2 || this.isReserved(dx, dz) || this.random.nextFloat() > 0.72f) {
                        continue;
                    }
                    if (FaeBuild.plant(this.level, this.origin, this.at(dx, this.floor + 1, dz), state(FaeBlocks.LANTERN_MUSHROOM))) {
                        this.reserve(dx, dz);
                    }
                }
            }
        }

        /** Feenhalme, Blumen und ein paar Sternspitzen auf der Lichtung. */
        private void plants() {
            for (int dx = -14; dx <= 14; dx++) {
                for (int dz = -14; dz <= 14; dz++) {
                    double r = Math.sqrt(dx * dx + dz * dz);
                    if (r > INNER - 0.5 || this.isReserved(dx, dz)) {
                        continue;
                    }
                    BlockPos pos = this.at(dx, this.floor + 1, dz);
                    float roll = this.random.nextFloat();
                    if (roll < 0.17f) {
                        FaeBuild.plant(this.level, this.origin, pos, state(FaeBlocks.FAE_SPROUTS));
                    } else if (roll < 0.21f) {
                        FaeBuild.plant(this.level, this.origin, pos, state(FaeBlocks.MOONBELL));
                    } else if (roll < 0.24f) {
                        FaeBuild.plant(this.level, this.origin, pos, state(FaeBlocks.FAE_ROSE));
                    } else if (roll < 0.25f) {
                        FaeBuild.plant(this.level, this.origin, pos, state(FaeBlocks.LANTERN_MUSHROOM));
                    } else if (roll < 0.262f && this.level.getBlockState(pos.above()).isAir()
                            && FaeBuild.inReach(this.origin, pos.above())) {
                        BlockState starlace = state(FaeBlocks.STARLACE);
                        if (this.level.getBlockState(pos).isAir() && starlace.canSurvive(this.level, pos)) {
                            DoublePlantBlock.placeAt(this.level, starlace, pos, 2);
                        }
                    }
                }
            }
        }

        // ------------------------------------------------------------ Bäume und Seide

        /** Fünf Glimmerbäume rings um den Steinkreis (Trauerglimmer und schlanke Glimmerbäume im Wechsel). */
        private void trees() {
            double phase = this.random.nextDouble() * Math.PI * 2.0;
            for (int k = 0; k < 5; k++) {
                double a = phase + k * Math.PI * 2.0 / 5.0 + (this.random.nextDouble() - 0.5) * 0.35;
                double r = 15.2 + this.random.nextDouble() * 0.8;
                int tx = this.cx + (int) Math.round(Math.cos(a) * r);
                int tz = this.cz + (int) Math.round(Math.sin(a) * r);
                BlockPos base = new BlockPos(tx, FaeBuild.surface(this.level, tx, tz), tz);
                GlimmerTreeFeature tree = k % 2 == 0
                        ? new GlimmerTreeFeature(GlimmerTreeFeature.Kind.WEEPING, k == 0 ? GlimmerTreeFeature.Palette.AZURE : GlimmerTreeFeature.Palette.MIXED)
                        : new GlimmerTreeFeature(GlimmerTreeFeature.Kind.SLENDER, GlimmerTreeFeature.Palette.ROSE);
                tree.place(this.level, this.generator, this.random, base);
            }
        }

        /** Mottenseide im Laub und lange Seidenfäden, die zur Lichtung hin von Laub und Ästen hängen. */
        private void silk() {
            int reach = 23;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    double r = Math.sqrt(dx * dx + dz * dz);
                    if (r < 6.0 || r > reach) {
                        continue;
                    }
                    float chance = r < 18.0 ? 0.24f : 0.1f;
                    for (int y = this.floor + 2; y <= this.floor + 24; y++) {
                        pos.set(this.cx + dx, y, this.cz + dz);
                        if (!FaeBuild.inReach(this.origin, pos)) {
                            continue;
                        }
                        BlockState state = this.level.getBlockState(pos);
                        boolean leaves = state.is(BlockTags.LEAVES);
                        if (!leaves && !state.is(BlockTags.LOGS) || !this.level.getBlockState(pos.below()).isAir()) {
                            continue;
                        }
                        if (leaves && r < 19.0 && this.random.nextFloat() < 0.05f) {
                            this.set(pos.immutable(), state(FaeBlocks.MOTH_SILK));
                        } else if (this.random.nextFloat() < chance) {
                            this.hangStrands(pos.immutable(), 1 + this.random.nextInt(4));
                        }
                    }
                }
            }
        }

        /** Seidenfäden unter {@code from}, höchstens {@code length} lang und nie bis auf den Boden. */
        private void hangStrands(BlockPos from, int length) {
            BlockState strands = state(FaeBlocks.SILK_STRANDS);
            BlockPos.MutableBlockPos pos = from.mutable();
            for (int i = 0; i < length; i++) {
                pos.move(0, -1, 0);
                if (!FaeBuild.inReach(this.origin, pos) || !this.level.getBlockState(pos).isAir()
                        || !this.level.getBlockState(pos.below()).isAir() || !this.level.getBlockState(pos.below(2)).isAir()) {
                    return;
                }
                this.level.setBlock(pos, strands, 2);
            }
        }
    }
}
