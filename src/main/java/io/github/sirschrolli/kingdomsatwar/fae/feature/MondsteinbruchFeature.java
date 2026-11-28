package io.github.sirschrolli.kingdomsatwar.fae.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.block.fae.KeeperStoneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.fae.Mondsteinbruch;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.jspecify.annotations.Nullable;

/**
 * Der alte Mondsteinbruch (seit 0.96): eine verlassene Grube des alten Volks im Feenreich, an deren Grund der Steinhüter kniet.
 * <ul>
 *     <li>Eine runde, leicht unregelmäßige Grube, {@link #DEPTH} Blöcke tief, in drei Stufen abgebaut: der Grund und zwei
 *     Terrassen, darüber der Rand – Gras und Feenerde über dem Fels. Die Wände aus Feenstein, darin offene Adern aus
 *     Mondsteinerz und gemeißelte Mondsteinlampen; an den Kanten ragen Grasnarbe und Moos über, Leuchtranken hängen herab.</li>
 *     <li>Rampen aus Feensteinziegeln führen an den Wänden entlang von Stufe zu Stufe hinab.</li>
 *     <li>Am Grund der Hüterstein auf einem Sockel aus Mondsteinziegeln; ein paar Schritte nördlich kniet der Hüter.</li>
 *     <li>Zwei Gerüste aus Glimmerholz an den Wänden (Laufsteg mit Geländer, Leiter, oben ein Balken mit Leuchtranken); am
 *     Nordrand ein Kran, dessen Ausleger über die Grube ragt – an seiner Kette hängt noch ein behauener Block.</li>
 *     <li>Im Südwesten die Hütte des Vorarbeiters (Steinsäge, Werkbank, eine Truhe); überall gestapelte Quader, Geröll, lose
 *     Brocken Mondsteinerz und Lampenpilze im Moos.</li>
 * </ul>
 * Wo die Brüche liegen, bestimmt {@link Mondsteinbruch} (höchstens einer pro Feld, immer in der Mitte eines Chunks).
 */
public record MondsteinbruchFeature() implements Feature {
    public static final MapCodec<MondsteinbruchFeature> CODEC = MapCodec.unit(MondsteinbruchFeature::new);
    /** Tiefe der Grube vom Rand bis zum Grund: drei Stufen zu je {@link #STEP} Blöcken. */
    public static final int DEPTH = 12;
    public static final int STEP = 4;
    /** Radien der Stufen – Grund, erste und zweite Terrasse (die Grube ist leicht unregelmäßig). */
    static final double[] RADII = {9.5, 12.5, 15.5};
    /** Bis hierher ist der Rand eben (bei der Hütte weiter) … */
    static final double RIM = 20.5;
    /** … so breit ist der Übergang ins Gelände. */
    static final double BLEND = 3.0;
    /** Wo der Hüter kniet: so weit nördlich des Hütersteins. */
    public static final int HOME_DZ = -4;
    /** Die Hütte des Vorarbeiters: Richtung (Winkel um die Mitte) und Abstand ihrer Mitte. */
    static final double HUT_ANGLE = Math.toRadians(135.0);
    static final double HUT_DISTANCE = 21.5;
    /** So weit (je Richtung) darf gebaut werden: der eigene Chunk und seine Nachbarn. */
    static final int REACH = 24;
    private static final double RAMP_WIDTH = 2.0;

    @Override
    public MapCodec<MondsteinbruchFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int chunkX = SectionPos.blockToSectionCoord(origin.getX());
        int chunkZ = SectionPos.blockToSectionCoord(origin.getZ());
        if (!Mondsteinbruch.isQuarryChunk(level.getSeed(), chunkX, chunkZ)) {
            return false;
        }
        build(level, random, SectionPos.sectionToBlockCoord(chunkX) + 8, SectionPos.sectionToBlockCoord(chunkZ) + 8);
        return true;
    }

    /** Baut den Bruch um ({@code cx}, {@code cz}) – ohne Prüfung des Felds (für Tests). Gibt die Lage des Hütersteins zurück. */
    public static BlockPos build(WorldGenLevel level, RandomSource random, int cx, int cz) {
        int floor = MondhainFeature.floorHeight(level, cx, cz);
        Quarry quarry = new Quarry(level, random, cx, cz, floor);
        quarry.build();
        return quarry.stone;
    }

    private static final class Quarry {
        private static final int SIZE = 2 * REACH + 1;
        private final WorldGenLevel level;
        private final RandomSource random;
        private final int cx;
        private final int cz;
        /** Höhe des Rands (Oberkante des Grases) und des Grunds. */
        private final int floor;
        private final int bottom;
        private final BlockPos origin;
        private final BlockPos stone;
        private final double phase1;
        private final double phase2;
        /** Je Spalte die Stufe: 0–2 Grube, 3 Rand, 4 Übergang ins Gelände, -1 unberührt. */
        private final byte[] kinds = new byte[SIZE * SIZE];
        /** Spalten, auf denen nichts mehr hinkommt (Rampen, Gerüste, Sockel, Kran, Hütte, Stapel). */
        private final Set<Long> reserved = new HashSet<>();

        Quarry(WorldGenLevel level, RandomSource random, int cx, int cz, int floor) {
            this.level = level;
            this.random = random;
            this.cx = cx;
            this.cz = cz;
            this.floor = floor;
            this.bottom = floor - DEPTH;
            this.origin = new BlockPos(cx, floor, cz);
            this.stone = new BlockPos(cx, this.bottom + 1, cz);
            this.phase1 = random.nextDouble() * Math.PI * 2.0;
            this.phase2 = random.nextDouble() * Math.PI * 2.0;
            Arrays.fill(this.kinds, (byte) -1);
        }

        void build() {
            this.terrain();
            this.dais();
            this.ramp(2, Math.toRadians(15.0));
            this.ramp(1, Math.toRadians(200.0));
            this.ramp(0, Math.toRadians(330.0));
            this.scaffold(1, Direction.SOUTH);
            this.scaffold(0, Direction.WEST);
            this.crane();
            this.hut();
            this.lips();
            this.lamps();
            this.veins();
            this.stacks();
            this.rubble();
            this.plants();
        }

        // ------------------------------------------------------------ Hilfen

        private static int index(int dx, int dz) {
            return (dx + REACH) * SIZE + dz + REACH;
        }

        /** Stufe der Spalte: 0–2 Grube, 3 Rand, 4 Übergang ins Gelände, -1 außerhalb. */
        private int kind(int dx, int dz) {
            return Math.abs(dx) <= REACH && Math.abs(dz) <= REACH ? this.kinds[index(dx, dz)] : -1;
        }

        /** Höhe der Stufe k (0 = Grund, 3 = Rand). */
        private int levelY(int k) {
            return this.bottom + STEP * k;
        }

        private double wobble(double angle) {
            return 1.0 + 0.06 * Math.sin(2.0 * angle + this.phase1) + 0.035 * Math.sin(3.0 * angle + this.phase2);
        }

        /** Bis wohin der Rand eben ist: rundum {@link #RIM}, bei der Hütte bis zu fünf Blöcke weiter. */
        private static double rimRadius(double angle) {
            double d = Mth.wrapDegrees(Math.toDegrees(angle - HUT_ANGLE));
            if (Math.abs(d) >= 30.0) {
                return RIM;
            }
            double bump = Math.cos(Math.toRadians(d * 3.0));
            return RIM + 5.0 * bump * bump;
        }

        private BlockPos at(int dx, int y, int dz) {
            return new BlockPos(this.cx + dx, y, this.cz + dz);
        }

        private void set(int dx, int y, int dz, BlockState state) {
            FaeBuild.force(this.level, this.origin, this.at(dx, y, dz), state);
        }

        private BlockState get(int dx, int y, int dz) {
            return this.level.getBlockState(this.at(dx, y, dz));
        }

        private void reserve(int dx, int dz) {
            this.reserved.add(BlockPos.asLong(dx, 0, dz));
        }

        private boolean isReserved(int dx, int dz) {
            return this.reserved.contains(BlockPos.asLong(dx, 0, dz));
        }

        private static BlockState state(Supplier<? extends Block> block) {
            return block.get().defaultBlockState();
        }

        private static BlockState stairs(Supplier<? extends StairBlock> block, Direction facing, Half half) {
            return block.get().defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
        }

        private static BlockState slab(Supplier<? extends SlabBlock> slab, SlabType type) {
            return slab.get().defaultBlockState().setValue(SlabBlock.TYPE, type);
        }

        private static BlockState log(Direction.Axis axis) {
            return FaeBlocks.GLIMMERWOOD_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
        }

        /** Ein Zaun aus Glimmerholz, zu den genannten Seiten verbunden. */
        private static BlockState fence(Direction... sides) {
            BlockState state = FaeBlocks.GLIMMERWOOD_FENCE.get().defaultBlockState();
            for (Direction side : sides) {
                state = state.setValue(CrossCollisionBlock.PROPERTY_BY_DIRECTION.get(side), true);
            }
            return state;
        }

        private static boolean rock(BlockState state) {
            return state.is(FaeBlocks.FAESTONE.base().get()) || state.is(FaeBlocks.MOONSTONE_ORE.get());
        }

        private static double smooth(double t) {
            t = Mth.clamp(t, 0.0, 1.0);
            return t * t * (3.0 - 2.0 * t);
        }

        /** Weiches Rauschen (für Moospolster auf dem Stein): grob 0 bis 1. */
        private double patches(int x, int z) {
            return 0.5 + 0.22 * Math.sin(x * 0.43 + this.phase1) + 0.18 * Math.sin(z * 0.37 + this.phase2)
                    + 0.14 * Math.sin((x + z) * 0.29 + 1.7) + 0.12 * Math.sin((x - z) * 0.61);
        }

        /** Die erste Spalte entlang des Strahls (vom Grund aus), die höher liegt als Stufe k – die Wand dahinter. */
        private int @Nullable [] wallColumn(int k, double angle) {
            for (double d = 0.0; d <= REACH; d += 0.5) {
                int dx = (int) Math.round(Math.cos(angle) * d);
                int dz = (int) Math.round(Math.sin(angle) * d);
                int kind = this.kind(dx, dz);
                if (kind > k && kind <= 3) {
                    return new int[] {dx, dz};
                }
                if (kind < 0) {
                    return null;
                }
            }
            return null;
        }

        // ------------------------------------------------------------ Gelände

        /**
         * Hebt die Grube aus (Grund, zwei Terrassen), ebnet den Rand ein und lässt ihn sanft ins Gelände übergehen. Unter dem
         * Rand und in den Wänden liegt durchgehend Feenstein (auch wo vorher Erde, Höhlen oder Wasser waren), damit die Wände
         * fest und dicht sind.
         */
        private void terrain() {
            BlockState air = Blocks.AIR.defaultBlockState();
            BlockState faestone = state(FaeBlocks.FAESTONE.base());
            BlockState soil = state(FaeBlocks.FAE_SOIL);
            for (int dx = -REACH; dx <= REACH; dx++) {
                for (int dz = -REACH; dz <= REACH; dz++) {
                    double r = Math.sqrt(dx * dx + dz * dz);
                    double angle = Math.atan2(dz, dx);
                    double rim = rimRadius(angle);
                    if (r > rim + BLEND) {
                        continue;
                    }
                    int x = this.cx + dx;
                    int z = this.cz + dz;
                    if (!FaeBuild.inReach(this.origin, new BlockPos(x, this.floor, z))) {
                        continue;
                    }
                    double rr = r / this.wobble(angle);
                    int k = rr <= RADII[0] ? 0 : rr <= RADII[1] ? 1 : rr <= RADII[2] ? 2 : r <= rim ? 3 : 4;
                    int surfaceTop = FaeBuild.surface(this.level, x, z) - 1;
                    int ground = MondhainFeature.groundAt(this.level, x, z);
                    boolean water = surfaceTop > ground && this.level.getFluidState(new BlockPos(x, surfaceTop, z)).is(FluidTags.WATER);
                    int target = k <= 3 ? this.levelY(k) : (int) Math.round(Mth.lerp(smooth((r - rim) / BLEND), this.floor, ground));
                    this.kinds[index(dx, dz)] = (byte) k;
                    if (k == 4) {
                        this.blend(x, z, target, ground, surfaceTop, water);
                        continue;
                    }
                    int clearTo = Math.max(Math.max(ground, surfaceTop), this.floor) + 8;
                    for (int y = target + 1; y <= clearTo; y++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!this.level.getBlockState(pos).isAir()) {
                            FaeBuild.force(this.level, this.origin, pos, air);
                        }
                    }
                    int rockTop = k == 3 ? target - 3 : target - 1;
                    for (int y = this.bottom - 3; y <= rockTop; y++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!rock(this.level.getBlockState(pos))) {
                            FaeBuild.force(this.level, this.origin, pos, faestone);
                        }
                    }
                    if (k == 3) {
                        FaeBuild.force(this.level, this.origin, new BlockPos(x, target - 2, z), soil);
                        FaeBuild.force(this.level, this.origin, new BlockPos(x, target - 1, z), soil);
                        FaeBuild.force(this.level, this.origin, new BlockPos(x, target, z), this.random.nextFloat() < 0.08f
                                ? state(FaeBlocks.FAE_MOSS) : state(FaeBlocks.FAE_GRASS));
                    } else {
                        FaeBuild.force(this.level, this.origin, new BlockPos(x, target, z), this.pitSurface(k, x, z));
                    }
                }
            }
        }

        /** Boden einer Stufe: nackter Feenstein, Polster aus Moos, hier und da noch die Platten, die man gerade schnitt. */
        private BlockState pitSurface(int k, int x, int z) {
            double n = this.patches(x, z);
            if (n > (k == 0 ? 0.86 : 0.78)) {
                return state(FaeBlocks.FAE_MOSS);
            }
            if (this.random.nextFloat() < (k == 0 ? 0.2f : 0.12f)) {
                return state(FaeBlocks.FAESTONE_BRICKS.base());
            }
            return state(FaeBlocks.FAESTONE.base());
        }

        /** Der Übergang ins Gelände (wie bei der Zauberbühne): abtragen oder aufschütten, Gras obenauf; Wasser bleibt Wasser. */
        private void blend(int x, int z, int target, int ground, int surfaceTop, boolean water) {
            boolean underwater = water && target < surfaceTop;
            if (target == ground && !water) {
                return;
            }
            if (ground > target || !underwater) {
                int clearTo = water ? Math.max(surfaceTop, ground) + 1 : ground + 6;
                for (int y = target + 1; y <= clearTo; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState old = this.level.getBlockState(pos);
                    if (old.isAir() || old.is(BlockTags.LEAVES) || old.is(BlockTags.LOGS)) {
                        continue;
                    }
                    FaeBuild.force(this.level, this.origin, pos, underwater && y <= surfaceTop ? Blocks.WATER.defaultBlockState()
                            : Blocks.AIR.defaultBlockState());
                }
            }
            for (int y = ground + 1; y <= target; y++) {
                FaeBuild.force(this.level, this.origin, new BlockPos(x, y, z), y < target - 3 ? state(FaeBlocks.FAESTONE.base())
                        : state(FaeBlocks.FAE_SOIL));
            }
            if (underwater) {
                return;
            }
            FaeBuild.force(this.level, this.origin, new BlockPos(x, target, z), state(FaeBlocks.FAE_GRASS));
            for (int y = target - 1; y >= target - 2; y--) {
                BlockPos pos = new BlockPos(x, y, z);
                if (!FaeBuild.soil(this.level.getBlockState(pos))) {
                    FaeBuild.force(this.level, this.origin, pos, state(FaeBlocks.FAE_SOIL));
                }
            }
        }

        // ------------------------------------------------------------ Grund

        /** Der Sockel aus Mondsteinziegeln mit dem Hüterstein; der Platz des Hüters bleibt frei. */
        private void dais() {
            int y = this.bottom;
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    double r = Math.sqrt(dx * dx + dz * dz);
                    if (r > 3.6 || this.kind(dx, dz) != 0) {
                        continue;
                    }
                    this.reserve(dx, dz);
                    if (r <= 2.5) {
                        this.set(dx, y, dz, state(FaeBlocks.MOONSTONE_BRICKS.base()));
                    } else {
                        this.set(dx, y, dz, (dx == 0 || dz == 0) && r > 2.9 ? state(FaeBlocks.CHISELED_MOONSTONE)
                                : state(FaeBlocks.FAESTONE_BRICKS.base()));
                    }
                }
            }
            this.set(0, y + 1, 0, state(FaeBlocks.KEEPER_STONE));
            if (this.level.getBlockEntity(this.stone) instanceof KeeperStoneBlockEntity entity) {
                entity.setHome(this.at(0, y + 1, HOME_DZ));
            }
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = HOME_DZ - 2; dz <= HOME_DZ + 1; dz++) {
                    this.reserve(dx, dz);
                }
            }
        }

        // ------------------------------------------------------------ Rampen

        /**
         * Eine Rampe an der Innenseite der Wand k (zwischen Stufe k und k + 1): zwei Blöcke breit, von {@code start} an (im
         * Uhrzeigersinn) in vier Stufen hinauf; jede Stufe beginnt mit einer Treppe, damit man ohne Springen hinaufkommt.
         */
        private void ramp(int k, double start) {
            double radius = RADII[k];
            double span = 7.6 / radius;
            int low = this.levelY(k);
            BlockState bricks = state(FaeBlocks.FAESTONE_BRICKS.base());
            for (int dx = -REACH; dx <= REACH; dx++) {
                for (int dz = -REACH; dz <= REACH; dz++) {
                    if (this.kind(dx, dz) != k || this.isReserved(dx, dz)) {
                        continue;
                    }
                    double angle = Math.atan2(dz, dx);
                    double rr = Math.sqrt(dx * dx + dz * dz) / this.wobble(angle);
                    if (rr <= radius - RAMP_WIDTH) {
                        continue;
                    }
                    double t = Mth.positiveModulo(angle - start, Math.PI * 2.0) / span;
                    if (t >= 1.0) {
                        continue;
                    }
                    double steps = t * STEP;
                    int h = low + 1 + Math.min(STEP - 1, (int) steps);
                    for (int y = low + 1; y < h; y++) {
                        this.set(dx, y, dz, bricks);
                    }
                    boolean stair = h < low + STEP && steps - Math.floor(steps) < 0.5;
                    Direction up = Direction.getApproximateNearest(-Math.sin(angle), 0.0, Math.cos(angle));
                    this.set(dx, h, dz, stair ? stairs(FaeBlocks.FAESTONE_BRICKS.stairs(), up, Half.BOTTOM) : bricks);
                    this.reserve(dx, dz);
                }
            }
        }

        // ------------------------------------------------------------ Gerüste und Kran

        /**
         * Ein Gerüst aus Glimmerholz vor der Wand k (auf Stufe k), von der Mitte aus in Richtung {@code toward}: ein Laufsteg auf
         * halber Höhe mit Geländer, zwei Pfosten, oben ein Querbalken (daran Leuchtranken), daneben eine Leiter; auf dem Steg
         * liegt noch ein Werkstück.
         */
        private void scaffold(int k, Direction toward) {
            int fx = toward.getStepX();
            int fz = toward.getStepZ();
            int sx = -fz;
            int sz = fx;
            Direction along = toward.getClockWise();
            int wall = -1;
            for (int d = 1; d <= REACH; d++) {
                if (this.kind(fx * d, fz * d) > k) {
                    wall = d;
                    break;
                }
            }
            if (wall < 4) {
                return;
            }
            int low = this.levelY(k);
            int high = this.levelY(k + 1);
            int back = wall - 1;
            int front = wall - 2;
            BlockState platform = slab(FaeBlocks.GLIMMERWOOD_PLANKS.slab(), SlabType.TOP);
            BlockState post = log(Direction.Axis.Y);
            BlockState beam = log(along.getAxis());
            for (int s = -3; s <= 3; s++) {
                for (int d : new int[] {back, front}) {
                    int dx = fx * d + sx * s;
                    int dz = fz * d + sz * s;
                    if (this.kind(dx, dz) != k || this.isReserved(dx, dz)) {
                        continue;
                    }
                    this.reserve(dx, dz);
                    if (Math.abs(s) == 3 && d == front) {
                        for (int y = low + 1; y <= high + 1; y++) {
                            this.set(dx, y, dz, post);
                        }
                    } else {
                        this.set(dx, low + 2, dz, platform);
                        if (d == front) {
                            this.set(dx, low + 3, dz, fence(along, along.getOpposite()));
                        }
                    }
                    if (d == front) {
                        this.set(dx, high + 2, dz, beam);
                        if (Math.abs(s) < 3 && this.random.nextFloat() < 0.6f) {
                            FaeBuild.hangVines(this.level, this.origin, this.at(dx, high + 2, dz), 3, this.random);
                        }
                    }
                }
            }
            // ein Werkstück auf dem Steg
            int wx = fx * back + sx * -1;
            int wz = fz * back + sz * -1;
            if (this.kind(wx, wz) == k) {
                this.set(wx, low + 3, wz, state(FaeBlocks.FAESTONE_BRICKS.base()));
                this.set(wx + sx * 2, low + 3, wz + sz * 2, slab(FaeBlocks.MOONSTONE_BRICKS.slab(), SlabType.BOTTOM));
            }
            // die Leiter neben dem Gerüst
            int lx = fx * back + sx * 4;
            int lz = fz * back + sz * 4;
            if (this.kind(lx, lz) == k && this.kind(lx + fx, lz + fz) > k && !this.isReserved(lx, lz)) {
                this.reserve(lx, lz);
                BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, toward.getOpposite());
                for (int y = low + 1; y <= high; y++) {
                    this.set(lx, y, lz, ladder);
                }
            }
        }

        /**
         * Der Kran am Nordrand: ein Mast aus Glimmerholz auf einem Fuß aus Ziegeln und liegenden Stämmen, oben der Ausleger über
         * die Grube und ein kurzer Gegenausleger mit Gegengewicht, Kopfbänder darunter, eine Winde am Mast. Von der Spitze hängt
         * eine Kette bis tief in die Grube – daran ein behauener Block Mondsteinziegel.
         */
        private void crane() {
            int edge = -1;
            int tip = -1;
            for (int d = 1; d <= REACH; d++) {
                int kind = this.kind(0, -d);
                if (kind == 0) {
                    tip = d;
                }
                if (kind == 3) {
                    edge = d;
                    break;
                }
            }
            if (edge < 0 || tip < 0) {
                return;
            }
            int mast = edge + 1;
            int mz = -mast;
            for (int[] o : new int[][] {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                if (this.kind(o[0], mz + o[1]) != 3) {
                    return;
                }
            }
            int y = this.floor;
            int top = y + 11;
            for (int[] o : new int[][] {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {2, 0}, {-2, 0}}) {
                if (this.kind(o[0], mz + o[1]) == 3) {
                    this.reserve(o[0], mz + o[1]);
                    this.set(o[0], y, mz + o[1], state(FaeBlocks.FAESTONE_BRICKS.base()));
                }
            }
            this.set(1, y + 1, mz, log(Direction.Axis.X));
            this.set(-1, y + 1, mz, log(Direction.Axis.X));
            this.set(0, y + 1, mz + 1, log(Direction.Axis.Z));
            this.set(0, y + 1, mz - 1, log(Direction.Axis.Z));
            for (int yy = y + 1; yy <= top + 1; yy++) {
                this.set(0, yy, mz, log(Direction.Axis.Y));
            }
            this.set(0, top + 2, mz, slab(FaeBlocks.GLIMMERWOOD_PLANKS.slab(), SlabType.BOTTOM));
            // Ausleger (nach Süden über die Grube) und Gegenausleger mit Gegengewicht
            int tipZ = -tip;
            for (int z = mz + 1; z <= tipZ; z++) {
                this.set(0, top, z, log(Direction.Axis.Z));
            }
            for (int z = mz - 3; z <= mz - 1; z++) {
                this.set(0, top, z, log(Direction.Axis.Z));
            }
            this.set(0, top - 1, mz - 3, state(FaeBlocks.FAESTONE_BRICKS.base()));
            this.set(0, top - 2, mz - 3, state(FaeBlocks.FAESTONE_BRICKS.base()));
            this.set(0, top - 1, mz + 1, stairs(FaeBlocks.GLIMMERWOOD_PLANKS.stairs(), Direction.NORTH, Half.TOP));
            this.set(0, top - 1, mz - 1, stairs(FaeBlocks.GLIMMERWOOD_PLANKS.stairs(), Direction.SOUTH, Half.TOP));
            // die Winde mit Kurbel
            this.set(1, y + 2, mz, log(Direction.Axis.X));
            this.set(2, y + 1, mz, fence());
            this.set(2, y + 2, mz, fence(Direction.WEST));
            this.reserve(0, tipZ);
            // Kette mit Block: fünf Blöcke über dem Grund (der Hüter passt darunter durch)
            int load = this.bottom + 6;
            for (int yy = top - 1; yy > load; yy--) {
                this.set(0, yy, tipZ, Blocks.IRON_CHAIN.defaultBlockState());
            }
            this.set(0, load, tipZ, state(FaeBlocks.MOONSTONE_BRICKS.base()));
            for (int z = mz + 2; z < tipZ - 1; z++) {
                if (this.random.nextFloat() < 0.45f) {
                    FaeBuild.hangVines(this.level, this.origin, this.at(0, top, z), 3, this.random);
                }
            }
        }

        // ------------------------------------------------------------ Hütte

        /**
         * Die Hütte des Vorarbeiters auf dem Rand im Südwesten: ein Fachwerkhaus – unten ein Sockel aus Feensteinziegeln, darüber
         * Bretter aus Glimmerholz zwischen Eckpfosten und Rähm aus dunklen Stämmen, zwei Fenster, die Tür nach Norden (ein Weg
         * aus Trittsteinen führt zum Bruch); ein Satteldach aus Feensteinziegeln mit einem First aus Mondsteinziegeln und
         * Überstand (Leuchtranken an der Traufe), an der Westseite ein Schornstein, aus dem Rauch steigt. Drinnen eine Steinsäge,
         * eine Werkbank, ein Fass und die Truhe des Vorarbeiters, im Boden ein leuchtender Mondstein.
         */
        private void hut() {
            int hx = (int) Math.round(Math.cos(HUT_ANGLE) * HUT_DISTANCE);
            int hz = (int) Math.round(Math.sin(HUT_ANGLE) * HUT_DISTANCE);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -3; dz <= 2; dz++) {
                    if (this.kind(hx + dx, hz + dz) != 3) {
                        return;
                    }
                }
            }
            int y = this.floor;
            BlockState planks = state(FaeBlocks.GLIMMERWOOD_PLANKS.base());
            BlockState bricks = state(FaeBlocks.FAESTONE_BRICKS.base());
            BlockState air = Blocks.AIR.defaultBlockState();
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    this.reserve(hx + dx, hz + dz);
                }
            }
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    boolean ring = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                    boolean corner = Math.abs(dx) == 2 && Math.abs(dz) == 2;
                    this.set(hx + dx, y, hz + dz, ring || !(dx == 0 && dz == 0) ? bricks : state(FaeBlocks.CHISELED_MOONSTONE));
                    for (int yy = y + 1; yy <= y + 3; yy++) {
                        BlockState wall;
                        if (!ring) {
                            wall = air;
                        } else if (corner) {
                            wall = log(Direction.Axis.Y);
                        } else if (yy == y + 1) {
                            wall = bricks;                                           // Sockel
                        } else if (yy == y + 3) {
                            wall = log(Math.abs(dz) == 2 ? Direction.Axis.X : Direction.Axis.Z);   // Rähm
                        } else {
                            wall = planks;
                        }
                        this.set(hx + dx, yy, hz + dz, wall);
                    }
                }
            }
            // Fenster (Westen und Süden) und die Tür (Norden)
            this.set(hx - 2, y + 2, hz, Blocks.GLASS_PANE.defaultBlockState()
                    .setValue(CrossCollisionBlock.NORTH, true).setValue(CrossCollisionBlock.SOUTH, true));
            this.set(hx, y + 2, hz + 2, Blocks.GLASS_PANE.defaultBlockState()
                    .setValue(CrossCollisionBlock.EAST, true).setValue(CrossCollisionBlock.WEST, true));
            BlockState door = FaeBlocks.GLIMMERWOOD_DOOR.get().defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH);
            this.set(hx, y + 1, hz - 2, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
            this.set(hx, y + 2, hz - 2, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
            // Trittsteine von der Tür zum Bruch
            for (int[] step : new int[][] {{0, -3}, {1, -4}, {1, -5}, {2, -6}, {3, -7}}) {
                int sx = hx + step[0];
                int sz = hz + step[1];
                if (this.kind(sx, sz) == 3 && (step[1] == -3 || !this.isReserved(sx, sz))) {
                    this.reserve(sx, sz);
                    this.set(sx, y, sz, step[1] == -3 ? bricks : state(FaeBlocks.FAESTONE.base()));
                }
            }
            // Dach aus Feensteinziegeln: First von Westen nach Osten (Mondsteinziegel), Giebel aus Brettern
            for (int dx = -3; dx <= 3; dx++) {
                for (int row = 0; row < 3; row++) {
                    int yy = y + 4 + row;
                    this.set(hx + dx, yy, hz - 3 + row, stairs(FaeBlocks.FAESTONE_BRICKS.stairs(), Direction.SOUTH, Half.BOTTOM));
                    this.set(hx + dx, yy, hz + 3 - row, stairs(FaeBlocks.FAESTONE_BRICKS.stairs(), Direction.NORTH, Half.BOTTOM));
                    if (row > 0 && Math.abs(dx) <= 2) {
                        this.set(hx + dx, yy - 1, hz - 3 + row, planks);
                        this.set(hx + dx, yy - 1, hz + 3 - row, planks);
                    }
                }
                this.set(hx + dx, y + 7, hz, slab(FaeBlocks.MOONSTONE_BRICKS.slab(), SlabType.BOTTOM));
                this.set(hx + dx, y + 6, hz, Math.abs(dx) <= 2 ? planks : slab(FaeBlocks.FAESTONE_BRICKS.slab(), SlabType.TOP));
            }
            for (int sx = -2; sx <= 2; sx += 4) {
                for (int dz = -1; dz <= 1; dz++) {
                    this.set(hx + sx, y + 4, hz + dz, planks);
                    this.set(hx + sx, y + 5, hz + dz, planks);
                }
            }
            // der Schornstein an der Westseite (bis über den First), obenauf ein Lagerfeuer, dessen Rauch aufsteigt
            if (this.kind(hx - 3, hz) == 3) {
                for (int yy = y - 2; yy <= y + 8; yy++) {
                    this.set(hx - 3, yy, hz, bricks);
                }
                this.set(hx - 3, y + 9, hz, Blocks.CAMPFIRE.defaultBlockState());
            }
            // Leuchtranken an der Traufe
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz : new int[] {-3, 3}) {
                    if (this.random.nextFloat() < 0.4f) {
                        FaeBuild.hangVines(this.level, this.origin, this.at(hx + dx, y + 4, hz + dz), 2, this.random);
                    }
                }
            }
            // drinnen: Steinsäge, Werkbank, Fass, Truhe
            this.set(hx + 1, y + 1, hz + 1, Blocks.STONECUTTER.defaultBlockState().setValue(StonecutterBlock.FACING, Direction.NORTH));
            this.set(hx + 1, y + 1, hz - 1, Blocks.CRAFTING_TABLE.defaultBlockState());
            this.set(hx - 1, y + 1, hz - 1, Blocks.BARREL.defaultBlockState());
            BlockPos chest = this.at(hx - 1, y + 1, hz + 1);
            this.set(hx - 1, y + 1, hz + 1, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH));
            if (this.level.getBlockEntity(chest) instanceof ChestBlockEntity entity) {
                entity.setLootTable(ModLootTables.MOONSTONE_QUARRY_CHEST, this.random.nextLong());
            }
            // vor der Hütte: ein Stapel Quader, Lampenpilze
            this.set(hx + 3, y + 1, hz - 1, bricks);
            this.set(hx + 3, y + 1, hz, slab(FaeBlocks.FAESTONE_BRICKS.slab(), SlabType.BOTTOM));
            this.set(hx + 3, y + 2, hz - 1, slab(FaeBlocks.MOONSTONE_BRICKS.slab(), SlabType.BOTTOM));
            FaeBuild.plant(this.level, this.origin, this.at(hx - 3, y + 1, hz - 2), state(FaeBlocks.LANTERN_MUSHROOM));
            FaeBuild.plant(this.level, this.origin, this.at(hx + 2, y + 1, hz - 4), state(FaeBlocks.LANTERN_MUSHROOM));
        }

        // ------------------------------------------------------------ Wände

        /**
         * An den Kanten ragen Grasnarbe (am Rand) und Moos (an den Terrassen) einen Block über die Wand; darunter hängen oft
         * kurze Leuchtranken.
         */
        private void lips() {
            for (int dx = -REACH; dx <= REACH; dx++) {
                for (int dz = -REACH; dz <= REACH; dz++) {
                    int k = this.kind(dx, dz);
                    if (k < 0 || k > 2 || this.isReserved(dx, dz) || this.random.nextFloat() >= 0.17f) {
                        continue;
                    }
                    if (this.kind(dx + 1, dz) != k + 1 && this.kind(dx - 1, dz) != k + 1 && this.kind(dx, dz + 1) != k + 1
                            && this.kind(dx, dz - 1) != k + 1) {
                        continue;
                    }
                    int y = this.levelY(k + 1);
                    if (!this.get(dx, y, dz).isAir()) {
                        continue;
                    }
                    this.set(dx, y, dz, k == 2 ? state(FaeBlocks.FAE_GRASS) : state(FaeBlocks.FAE_MOSS));
                    if (this.random.nextFloat() < 0.65f) {
                        FaeBuild.hangVines(this.level, this.origin, this.at(dx, y, dz), 2, this.random);
                    }
                }
            }
        }

        /** Gemeißelte Mondsteinlampen in den Wänden, auf halber Höhe jeder Stufe. */
        private void lamps() {
            BlockState lamp = state(FaeBlocks.CHISELED_MOONSTONE);
            for (int k = 0; k < 3; k++) {
                int count = 5 + k;
                double offset = this.random.nextDouble() * Math.PI * 2.0;
                for (int i = 0; i < count; i++) {
                    int[] wall = this.wallColumn(k, offset + i * Math.PI * 2.0 / count);
                    if (wall != null && rock(this.get(wall[0], this.levelY(k) + 2, wall[1]))) {
                        this.set(wall[0], this.levelY(k) + 2, wall[1], lamp);
                    }
                }
            }
        }

        /** Offene Adern aus Mondsteinerz in den Wänden. */
        private void veins() {
            BlockState ore = state(FaeBlocks.MOONSTONE_ORE);
            for (int i = 0; i < 14; i++) {
                int k = this.random.nextInt(3);
                int[] wall = this.wallColumn(k, this.random.nextDouble() * Math.PI * 2.0);
                if (wall == null) {
                    continue;
                }
                int y0 = this.levelY(k) + 1 + this.random.nextInt(3);
                double radius = 1.0 + this.random.nextDouble() * 0.8;
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dy = -2; dy <= 2; dy++) {
                        for (int dz = -2; dz <= 2; dz++) {
                            if (dx * dx + dy * dy + dz * dz > radius * radius || this.random.nextFloat() > 0.85f) {
                                continue;
                            }
                            if (this.get(wall[0] + dx, y0 + dy, wall[1] + dz).is(FaeBlocks.FAESTONE.base().get())) {
                                this.set(wall[0] + dx, y0 + dy, wall[1] + dz, ore);
                            }
                        }
                    }
                }
            }
        }

        // ------------------------------------------------------------ Kleinkram

        /** Gestapelte Quader (Feensteinziegel, obenauf manchmal Mondsteinziegel oder Stufen) auf allen Stufen. */
        private void stacks() {
            double[][] spots = {{60, 3}, {250, 3}, {300, 3}, {200, 3}, {115, 2}, {300, 2}, {35, 1}, {140, 1}, {290, 1},
                    {120, 0}, {225, 0}};
            for (double[] spot : spots) {
                double angle = Math.toRadians(spot[0] + this.random.nextDouble() * 16.0 - 8.0);
                int k = (int) spot[1];
                double w = this.wobble(angle);
                double radius = k == 3 ? (RADII[2] * w + RIM) / 2.0 : k == 0 ? RADII[0] * 0.72 * w : (RADII[k - 1] + RADII[k]) / 2.0 * w;
                int px = (int) Math.round(Math.cos(angle) * radius);
                int pz = (int) Math.round(Math.sin(angle) * radius);
                boolean free = true;
                for (int dx = 0; dx <= 1 && free; dx++) {
                    for (int dz = 0; dz <= 1 && free; dz++) {
                        free = this.kind(px + dx, pz + dz) == k && !this.isReserved(px + dx, pz + dz);
                    }
                }
                if (!free) {
                    continue;
                }
                int base = this.levelY(Math.min(k, 3));
                for (int dx = 0; dx <= 1; dx++) {
                    for (int dz = 0; dz <= 1; dz++) {
                        this.reserve(px + dx, pz + dz);
                        int height = 1 + (this.random.nextFloat() < 0.55f ? 1 : 0);
                        for (int h = 1; h <= height; h++) {
                            this.set(px + dx, base + h, pz + dz, state(FaeBlocks.FAESTONE_BRICKS.base()));
                        }
                        float f = this.random.nextFloat();
                        if (f < 0.3f) {
                            this.set(px + dx, base + height + 1, pz + dz, state(FaeBlocks.MOONSTONE_BRICKS.base()));
                        } else if (f < 0.6f) {
                            this.set(px + dx, base + height + 1, pz + dz, slab(FaeBlocks.FAESTONE_BRICKS.slab(), SlabType.BOTTOM));
                        }
                    }
                }
            }
        }

        /** Geröll: lose Stufen und Treppen aus Feenstein, auf dem Grund ein paar Brocken Mondsteinerz. */
        private void rubble() {
            for (int i = 0; i < 70; i++) {
                int dx = this.random.nextInt(2 * REACH + 1) - REACH;
                int dz = this.random.nextInt(2 * REACH + 1) - REACH;
                int k = this.kind(dx, dz);
                if (k < 0 || k > 3 || this.isReserved(dx, dz)) {
                    continue;
                }
                int y = this.levelY(k) + 1;
                if (!this.get(dx, y, dz).isAir() || !this.get(dx, y + 1, dz).isAir()) {
                    continue;
                }
                float f = this.random.nextFloat();
                BlockState piece;
                if (k == 0 && f < 0.22f) {
                    piece = state(FaeBlocks.MOONSTONE_ORE);
                } else if (f < 0.6f) {
                    piece = slab(FaeBlocks.FAESTONE.slab(), SlabType.BOTTOM);
                } else if (k < 3) {
                    piece = stairs(FaeBlocks.FAESTONE.stairs(), Direction.from2DDataValue(this.random.nextInt(4)), Half.BOTTOM);
                } else {
                    continue;
                }
                this.set(dx, y, dz, piece);
            }
        }

        /** Lampenpilze im Moos der Stufen, Mondglocken und Feenrosen am Rand. */
        private void plants() {
            for (int i = 0; i < 90; i++) {
                int dx = this.random.nextInt(2 * REACH + 1) - REACH;
                int dz = this.random.nextInt(2 * REACH + 1) - REACH;
                int k = this.kind(dx, dz);
                if (k < 0 || k > 3 || this.isReserved(dx, dz)) {
                    continue;
                }
                BlockState plant = k == 3 ? (this.random.nextBoolean() ? state(FaeBlocks.MOONBELL) : state(FaeBlocks.FAE_ROSE))
                        : state(FaeBlocks.LANTERN_MUSHROOM);
                if (k == 3 && this.random.nextFloat() < 0.6f) {
                    continue;
                }
                FaeBuild.plant(this.level, this.origin, this.at(dx, this.levelY(k) + 1, dz), plant);
            }
        }
    }
}
