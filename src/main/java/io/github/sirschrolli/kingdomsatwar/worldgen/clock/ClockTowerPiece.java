package io.github.sirschrolli.kingdomsatwar.worldgen.clock;

import io.github.sirschrolli.kingdomsatwar.block.abyss.ClockHeartBlockEntity;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssGear;
import io.github.sirschrolli.kingdomsatwar.registry.ClockTower;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.LairContext;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.jspecify.annotations.Nullable;

/**
 * Der Uhrturm (seit 1.1), Chunk für Chunk gebaut – frei schwebend im Abgrund, in einem leer gefegten Auge des Chaos:
 * <ol>
 *     <li>Das <b>große Zifferblatt</b>: eine runde Scheibe aus Email (31 Blöcke breit) mit einem Lünettenring aus Uhrbronze,
 *     einer Minutenbahn, zwölf Stundenmarken (die vier Hauptstunden aus Chronit) und zwei eingelegten Zeigern – sie stehen
 *     eine Minute vor zwölf. In der Nabe sitzt das <b>Uhrwerkherz</b>; darüber steht der Stundenwächter erstarrt. Ringsum eine
 *     niedrige, zerbrochene Brüstung; darunter ein zerklüfteter Kegel aus Chaosgestein mit Chronit und Uhrwerk, an dem
 *     Uhrgewichte an Ketten hängen.</li>
 *     <li>Im Norden (wo die Zwölf steht) über eine Brücke der <b>Uhrturm</b>: elf Blöcke breit, Chaosziegel mit Gesimsen aus
 *     Uhrbronze, auf jeder Seite ein Zifferblatt; innen windet sich eine Treppe um einen hohlen Kern, in dem das große Pendel
 *     hängt; auf halber Höhe die <b>Uhrwerkkammer</b> (der Kern aus Uhrwerk, eine Truhe), oben die offene <b>Glockenstube</b> mit
 *     der großen Glocke (eine Truhe). Das Dach ist abgebrochen – seine Spitze schwebt ein Stück darüber.</li>
 *     <li>Ringsum schweben riesige <b>Zahnräder</b> aus Uhrbronze, schräg im Raum, mit Uhrwerknabe und Chronitkern.</li>
 * </ol>
 */
public class ClockTowerPiece extends StructurePiece {
    /** Halbmesser des Zifferblatts (man steht einen Block über {@code center.y - 1}). */
    public static final double DIAL = 15.5;
    /** Wo der Turm steht (Mitte, nach Norden versetzt) und wie weit seine Mauern reichen (halbe Breite). */
    public static final int TOWER_Z = -27;
    public static final int TOWER_HALF = 5;
    /** Höhen im Turm (über dem Zifferblatt): Boden der Uhrwerkkammer, Boden der Glockenstube, Dach. */
    public static final int GEAR_ROOM = 15;
    public static final int BELL_ROOM = 26;
    public static final int ROOF = 34;
    /** Treppe: so viele Stufen je Umlauf (eine Stufe je Höhe). */
    static final int STEPS_PER_TURN = 24;
    /** Der freigeräumte Raum: so weit reicht er (sein Rand ist unregelmäßig), so tief und so hoch. */
    static final double R_CLEAR = 46.0;
    static final int CLEAR_DOWN = 34;
    static final int CLEAR_UP = 66;
    static final int REACH = 48;

    private final BlockPos center;
    private final long seed;

    public ClockTowerPiece(BlockPos center, long seed) {
        super(ModStructures.CLOCK_TOWER_PIECE.get(), 0, makeBox(center));
        this.center = center;
        this.seed = seed;
    }

    public ClockTowerPiece(CompoundTag tag) {
        super(ModStructures.CLOCK_TOWER_PIECE.get(), tag);
        this.center = new BlockPos(tag.getIntOr("CX", 0), tag.getIntOr("CY", 112), tag.getIntOr("CZ", 0));
        this.seed = tag.getLongOr("Seed", 0L);
    }

    public static BoundingBox makeBox(BlockPos c) {
        return new BoundingBox(c.getX() - REACH, c.getY() - CLEAR_DOWN, c.getZ() - REACH, c.getX() + REACH, c.getY() + CLEAR_UP,
                c.getZ() + REACH);
    }

    public BlockPos center() {
        return this.center;
    }

    /** Das Uhrwerkherz in der Nabe des Zifferblatts (eingelassen in den Boden). */
    public static BlockPos heart(BlockPos center) {
        return center.below();
    }

    /** Die Mitte des Turms (auf Höhe des Zifferblatts). */
    public static BlockPos tower(BlockPos center) {
        return center.offset(0, 0, TOWER_Z);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CX", this.center.getX());
        tag.putInt("CY", this.center.getY());
        tag.putInt("CZ", this.center.getZ());
        tag.putLong("Seed", this.seed);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBB, net.minecraft.world.level.ChunkPos chunkPos, BlockPos referencePos) {
        build(level, chunkBB, this.center, this.seed, random, this);
    }

    /** Baut den Uhrturm (für die Welt Chunk für Chunk, für Tests in einem Zug). {@code piece} legt die Truhen an (null: direkt). */
    public static void build(WorldGenLevel level, BoundingBox box, BlockPos center, long seed, RandomSource random, @Nullable ClockTowerPiece piece) {
        new Builder(level, box, center, seed).run(random, piece);
    }

    /** Ein schwebendes Zahnrad: Mitte, Halbmesser, Ebene (Normale), Zähne, Drehung. */
    record Gear(double x, double y, double z, double r, double nx, double ny, double nz, int teeth, double turn) {}

    /** Die schwebenden Zahnräder um das Zifferblatt (relativ zur Mitte). Auch für Tests. */
    static Gear[] gears(LairContext ctx) {
        double[][] spots = {
                // Richtung (Grad, 0 = Süden, im Uhrzeigersinn von oben), Abstand, Höhe, Halbmesser, Neigung (Grad)
                {40, 27, 7, 6.5, 18}, {118, 30, -3, 5.0, 70}, {205, 26, 13, 4.5, -25}, {290, 31, 2, 6.0, 40}, {330, 24, -11, 4.0, 82}};
        Gear[] out = new Gear[spots.length];
        for (int i = 0; i < spots.length; i++) {
            double[] s = spots[i];
            double a = Math.toRadians(s[0] + (ctx.rand(i, 1, 501) - 0.5) * 18.0);
            double d = s[1] + (ctx.rand(i, 2, 503) - 0.5) * 4.0;
            double x = -Math.sin(a) * d;
            double z = Math.cos(a) * d;
            // die Scheibe steht quer zur Richtung von der Mitte (man sieht ihre Fläche vom Zifferblatt aus), geneigt um tilt
            double tilt = Math.toRadians(s[4]);
            double nx = -Math.sin(a) * Math.cos(tilt);
            double nz = Math.cos(a) * Math.cos(tilt);
            double ny = Math.sin(tilt);
            out[i] = new Gear(x, s[2] + (ctx.rand(i, 3, 505) - 0.5) * 4.0, z, s[3], nx, ny, nz, 10 + (int) (ctx.rand(i, 4, 507) * 4) * 2,
                    ctx.rand(i, 5, 509) * Math.PI);
        }
        return out;
    }

    // =====================================================================================

    private static final class Builder {
        final WorldGenLevel level;
        final BoundingBox box;
        final LairContext ctx;
        final int cx;
        final int cz;
        final int f;
        final int tx;
        final int tz;

        Builder(WorldGenLevel level, BoundingBox box, BlockPos center, long seed) {
            this.level = level;
            this.box = box;
            this.ctx = new LairContext(level, box, center, seed);
            this.cx = center.getX();
            this.cz = center.getZ();
            this.f = center.getY();
            this.tx = this.cx;
            this.tz = this.cz + TOWER_Z;
        }

        boolean in(int x, int y, int z) {
            return this.box.isInside(x, y, z) && y > this.level.getMinY() && y < this.level.getMaxY();
        }

        void set(int x, int y, int z, BlockState state) {
            if (this.in(x, y, z)) {
                this.level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
            }
        }

        void setIfAir(int x, int y, int z, BlockState state) {
            if (this.in(x, y, z) && this.level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                this.level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
            }
        }

        /** Der Block an dieser Stelle – außerhalb des Chunks, der gerade entsteht, gilt Luft (dort darf nichts gelesen werden). */
        BlockState get(int x, int y, int z) {
            if (!this.box.isInside(x, y, z)) {
                return Blocks.AIR.defaultBlockState();
            }
            return this.level.getBlockState(new BlockPos(x, y, z));
        }

        static BlockState s(Block block) {
            return block.defaultBlockState();
        }

        BlockState bronze() {
            return s(ClockTower.CLOCK_BRONZE.base().get());
        }

        BlockState brick(int x, int y, int z) {
            return this.ctx.rand(x * 7 + y, z, 31) < 0.12 ? s(AbyssBlocks.CRACKED_CHAOS_STONE_BRICKS.get()) : s(AbyssBlocks.CHAOS_STONE_BRICKS.base().get());
        }

        BlockState polished() {
            return s(AbyssBlocks.POLISHED_CHAOS_STONE.base().get());
        }

        BlockState stone() {
            return s(AbyssBlocks.CHAOS_STONE.base().get());
        }

        void run(RandomSource random, @Nullable ClockTowerPiece piece) {
            this.clear();
            this.island();
            this.dial();
            this.bridge();
            this.tower();
            this.roof();
            for (Gear gear : gears(this.ctx)) {
                this.gear(gear);
            }
            this.weights();
            this.chests(random, piece);
            this.heart();
        }

        // ------------------------------------------------------------------ das Auge im Chaos

        /** Rand des freien Raums in Richtung {@code angle} auf Höhe {@code y}: zwischen 76 % und 100 % von {@link #R_CLEAR}. */
        double clearEdge(double angle, int y) {
            double n = 0.6 * this.ctx.angleNoise(angle + y * 0.011, 4.0, 151) + 0.4 * this.ctx.noise(angle * 9.0, y * 0.11, 1.0, 153);
            return R_CLEAR * (0.76 + 0.24 * n);
        }

        /** Räumt den Raum ringsum frei: ein hoher, runder Raum mit zerklüftetem Rand (oben und unten gerundet). */
        void clear() {
            for (int x = Math.max(this.box.minX(), this.cx - REACH); x <= Math.min(this.box.maxX(), this.cx + REACH); x++) {
                for (int z = Math.max(this.box.minZ(), this.cz - REACH); z <= Math.min(this.box.maxZ(), this.cz + REACH); z++) {
                    double dx = x - this.cx;
                    double dz = z - this.cz;
                    double r = Math.sqrt(dx * dx + dz * dz);
                    if (r > R_CLEAR + 0.5) {
                        continue;
                    }
                    double angle = Math.atan2(dz, dx);
                    int y0 = Math.max(this.box.minY(), this.f - CLEAR_DOWN);
                    int y1 = Math.min(this.box.maxY(), this.f + CLEAR_UP);
                    for (int y = y0; y <= y1; y++) {
                        double edge = this.clearEdge(angle, y);
                        if (r > edge) {
                            continue;
                        }
                        double k = r / edge;
                        double k4 = k * k * k * k;
                        if (y > this.f + CLEAR_UP - k4 * 22.0 || y < this.f - CLEAR_DOWN + k4 * 14.0) {
                            continue;
                        }
                        if (!this.get(x, y, z).isAir()) {
                            this.set(x, y, z, Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }
        }

        // ------------------------------------------------------------------ der Kegel unter Zifferblatt und Turm

        void island() {
            for (int x = this.cx - 18; x <= this.cx + 18; x++) {
                for (int z = this.cz - 18; z <= this.cz + 18; z++) {
                    if (!this.ctx.near(x, z, 0)) {
                        continue;
                    }
                    double r = Math.hypot(x - this.cx, z - this.cz);
                    double edge = DIAL + 1.1 + (this.ctx.rand(x, z, 61) - 0.5) * 0.8;
                    if (r > edge) {
                        continue;
                    }
                    double t = r / (DIAL + 1.1);
                    int depth = (int) (3 + 17 * (1.0 - Math.pow(t, 1.5)) + this.ctx.noise(x, z, 0.25, 63) * 4.0);
                    this.cone(x, z, depth);
                }
            }
            // unter dem Turm und der Brücke
            for (int x = this.tx - 8; x <= this.tx + 8; x++) {
                for (int z = this.tz - 8; z <= this.cz - 14; z++) {
                    if (!this.ctx.near(x, z, 0)) {
                        continue;
                    }
                    int dx = Math.abs(x - this.tx);
                    int dz = Math.abs(z - this.tz);
                    double m = Math.max(dx, z > this.tz ? 0.0 : dz) + (this.ctx.rand(x, z, 65) - 0.5) * 0.9;
                    boolean bridge = z > this.tz + TOWER_HALF && dx <= 2;
                    if (m > 7.4 && !bridge) {
                        continue;
                    }
                    if (bridge) {
                        this.cone(x, z, 2 + (int) (this.ctx.rand(x, z, 67) * 2));
                        continue;
                    }
                    double t = Math.min(1.0, m / 7.6);
                    this.cone(x, z, (int) (3 + 13 * (1.0 - t * t) + this.ctx.noise(x, z, 0.3, 69) * 3.0));
                }
            }
        }

        /** Eine Säule des Kegels: Chaosgestein mit Weltennähten, Chronit und Stücken Uhrwerk; oben eine Lage Ziegel. */
        void cone(int x, int z, int depth) {
            for (int k = 1; k <= depth; k++) {
                int y = this.f - 1 - k;
                BlockState state;
                double n = this.ctx.rand(x * 13 + y, z, 71);
                if (k == 1) {
                    state = this.brick(x, y, z);
                } else if (n < 0.04) {
                    state = s(AbyssBlocks.WORLD_SEAM.get());
                } else if (n < 0.075) {
                    state = s(AbyssGear.CHRONIT_ORE.get());
                } else if (n < 0.11) {
                    state = s(ClockTower.CLOCKWORK.get());
                } else {
                    state = this.stone();
                }
                this.set(x, y, z, state);
            }
        }

        // ------------------------------------------------------------------ das große Zifferblatt

        /** Winkel von Norden (der Zwölf, dem Turm) im Uhrzeigersinn von oben gesehen, im Bogenmaß 0..2π. */
        double clockAngle(double dx, double dz) {
            double a = Math.atan2(dx, -dz);
            return a < 0 ? a + Mth.TWO_PI : a;
        }

        /** Abstand des Punkts zur Linie eines Zeigers (Winkel {@code hand}) und wie weit er an ihr entlang liegt. */
        static double[] alongHand(double dx, double dz, double hand) {
            double ux = Math.sin(hand);
            double uz = -Math.cos(hand);
            return new double[] {dx * ux + dz * uz, Math.abs(dx * uz - dz * ux)};
        }

        void dial() {
            double minute = Math.toRadians(-6.0);
            double hour = Math.toRadians(-0.5);
            for (int x = this.cx - 17; x <= this.cx + 17; x++) {
                for (int z = this.cz - 17; z <= this.cz + 17; z++) {
                    if (!this.ctx.near(x, z, 0)) {
                        continue;
                    }
                    double dx = x - this.cx;
                    double dz = z - this.cz;
                    double r = Math.sqrt(dx * dx + dz * dz);
                    double a = this.clockAngle(dx, dz);
                    if (r <= DIAL) {
                        this.set(x, this.f - 1, z, this.dialBlock(dx, dz, r, a, minute, hour));
                        for (int y = this.f; y <= this.f + 6; y++) {
                            if (!this.get(x, y, z).isAir()) {
                                this.set(x, y, z, Blocks.AIR.defaultBlockState());
                            }
                        }
                    } else if (r <= DIAL + 0.95) {
                        this.parapet(x, z, dx, dz, r, a);
                    }
                }
            }
        }

        BlockState dialBlock(double dx, double dz, double r, double a, double minute, double hour) {
            if (r < 0.5) {
                return s(ClockTower.CLOCK_HEART.get());
            }
            if (r < 1.6) {
                return this.bronze();
            }
            if (r < 2.7) {
                return s(ClockTower.CLOCKWORK.get());
            }
            if (r > 14.0) {
                // die Lünette, an den vier Hauptstunden ein Chronit
                double h = a / (Mth.TWO_PI / 4.0);
                if (Math.abs(h - Math.round(h)) * Mth.HALF_PI * r < 0.6 && r < 15.0) {
                    return s(AbyssGear.CHRONIT_BLOCK.get());
                }
                return this.bronze();
            }
            if (r > 12.9) {
                // die Minutenbahn: dunkel, jede Minute ein heller Strich
                double m = a / (Mth.TWO_PI / 60.0);
                return Math.abs(m - Math.round(m)) * (Mth.TWO_PI / 60.0) * r < 0.42 ? s(ClockTower.CLOCK_FACE.get()) : this.polished();
            }
            // die Zeiger: eingelegt aus poliertem Chaosgestein, die Spitze eine Raute aus Uhrbronze
            double[] mh = alongHand(dx, dz, minute);
            if (mh[0] > 1.5 && mh[0] < 12.2) {
                double half = mh[0] > 9.2 && mh[0] < 11.2 ? 1.25 - Math.abs(mh[0] - 10.2) * 0.6 : 0.6;
                if (mh[1] < half) {
                    return mh[0] > 9.2 ? this.bronze() : this.polished();
                }
            }
            double[] hh = alongHand(dx, dz, hour);
            if (hh[0] > 1.5 && hh[0] < 8.4) {
                double half = hh[0] > 5.6 && hh[0] < 7.6 ? 1.6 - Math.abs(hh[0] - 6.6) * 0.7 : 0.85;
                if (hh[1] < half) {
                    return hh[0] > 5.6 ? this.bronze() : this.polished();
                }
            }
            // die Stundenmarken: Balken (die vier Hauptstunden breiter, aus Chronit)
            if (r > 10.4) {
                double h = a / (Mth.TWO_PI / 12.0);
                long nearest = Math.round(h);
                double off = Math.abs(h - nearest) * (Mth.TWO_PI / 12.0) * r;
                boolean main = nearest % 3 == 0;
                if (off < (main ? 1.05 : 0.55) && r > (main ? 10.4 : 11.2)) {
                    return main ? s(AbyssGear.CHRONIT_BLOCK.get()) : this.polished();
                }
            }
            return s(ClockTower.CLOCK_FACE.get());
        }

        /** Die Brüstung: niedrige Chaosmauern, an der Brücke offen, an jeder Stunde eine Lücke, stellenweise zerbrochen. */
        void parapet(int x, int z, double dx, double dz, double r, double a) {
            this.set(x, this.f - 1, z, this.brick(x, this.f - 1, z));
            if (Math.abs(dx) <= 2.5 && dz < 0) {
                return;                                                     // der Durchgang zur Brücke
            }
            double h = a / (Mth.TWO_PI / 12.0);
            if (Math.abs(h - Math.round(h)) * (Mth.TWO_PI / 12.0) * r < 1.1) {
                return;
            }
            double broken = this.ctx.angleNoise(a, 6.0, 99);
            if (broken < 0.28) {
                if (this.ctx.rand(x, z, 101) < 0.45) {
                    this.set(x, this.f, z, s(AbyssBlocks.CHAOS_STONE_BRICKS.slab().get()));
                }
                return;
            }
            this.set(x, this.f, z, s(AbyssBlocks.CHAOS_STONE_BRICKS.wall().get()));
        }

        // ------------------------------------------------------------------ die Brücke zum Turm

        void bridge() {
            int z0 = this.cz - 16;
            int z1 = this.tz + TOWER_HALF + 1;
            for (int z = z1; z <= z0; z++) {
                for (int x = this.cx - 3; x <= this.cx + 3; x++) {
                    int dx = Math.abs(x - this.cx);
                    if (dx <= 2) {
                        this.set(x, this.f - 1, z, dx == 2 ? this.bronze() : this.brick(x, this.f - 1, z));
                        for (int y = this.f; y <= this.f + 4; y++) {
                            if (!this.get(x, y, z).isAir()) {
                                this.set(x, y, z, Blocks.AIR.defaultBlockState());
                            }
                        }
                    } else {
                        this.set(x, this.f - 1, z, this.polished());
                        this.set(x, this.f, z, s(ClockTower.CLOCK_BRONZE.wall().get()));
                    }
                }
            }
            // eine Laterne unter der Mitte der Brücke
            int mz = (z0 + z1) / 2;
            this.set(this.cx, this.f - 3, mz, s(Blocks.COPPER_CHAIN.waxed().unaffected()));
            this.set(this.cx, this.f - 4, mz, s(Blocks.LANTERN).setValue(LanternBlock.HANGING, true));
        }

        // ------------------------------------------------------------------ der Turm

        /** Treppenstufe in dieser Säule (relativ zur Turmmitte): Höhe über dem Zifferblatt für den ersten Umlauf (0..23). */
        static int step(int dx, int dz) {
            double a = Math.atan2(dz, dx);                                  // Osten 0, Süden +π/2
            double s = ((a + Math.PI / 2.0) % Mth.TWO_PI + Mth.TWO_PI) % Mth.TWO_PI / (Mth.TWO_PI / STEPS_PER_TURN);
            return (int) Math.floor(s);
        }

        static int lane(int dx, int dz) {
            return Math.max(Math.abs(dx), Math.abs(dz));
        }

        void tower() {
            int h = TOWER_HALF;
            for (int x = this.tx - h - 1; x <= this.tx + h + 1; x++) {
                for (int z = this.tz - h - 1; z <= this.tz + h + 1; z++) {
                    if (!this.ctx.near(x, z, 0)) {
                        continue;
                    }
                    int dx = x - this.tx;
                    int dz = z - this.tz;
                    int lane = lane(dx, dz);
                    if (lane == h + 1) {
                        // der Sockel: eine Stufe außen herum
                        this.set(x, this.f - 1, z, this.polished());
                        this.set(x, this.f, z, s(AbyssBlocks.POLISHED_CHAOS_STONE.slab().get()));
                        continue;
                    }
                    this.set(x, this.f - 1, z, lane <= 1 ? s(ClockTower.CLOCKWORK.get()) : this.polished());
                    if (lane == h) {
                        this.wall(x, z, dx, dz);
                    } else {
                        this.inside(x, z, dx, dz, lane);
                    }
                }
            }
            this.faces();
            this.bellRoom();
        }

        /** Eine Säule der Außenmauer (bis unter das Dach), mit Ecksäulen, Gesimsen, Fenstern und der Tür. */
        void wall(int x, int z, int dx, int dz) {
            boolean corner = Math.abs(dx) == TOWER_HALF && Math.abs(dz) == TOWER_HALF;
            for (int y = this.f; y < this.f + ROOF; y++) {
                int k = y - this.f;
                BlockState state = corner ? this.polished() : this.brick(x, y, z);
                if (k <= 1 && !corner) {
                    state = this.polished();
                }
                if (k == GEAR_ROOM - 1 || k == BELL_ROOM - 1 || k == ROOF - 1) {
                    state = corner ? this.bronze() : s(ClockTower.CLOCK_BRONZE.base().get());     // die Gesimse
                }
                int side = Math.abs(dx) == TOWER_HALF ? dz : dx;
                // die Glockenstube: große Bögen auf jeder Seite
                if (k >= BELL_ROOM + 1 && k <= BELL_ROOM + 5 && !corner && Math.abs(side) <= 2) {
                    if (k == BELL_ROOM + 5 && Math.abs(side) == 2) {
                        state = this.archStair(dx, dz);
                    } else {
                        state = Blocks.AIR.defaultBlockState();
                    }
                }
                // die Tür nach Süden (zum Zifferblatt)
                if (dz == TOWER_HALF && Math.abs(dx) <= 1 && k <= 3) {
                    state = k == 3 && Math.abs(dx) == 1 ? s(ClockTower.CLOCK_BRONZE.stairs().get()).setValue(StairBlock.FACING,
                            dx > 0 ? Direction.WEST : Direction.EAST).setValue(StairBlock.HALF, Half.TOP) : Blocks.AIR.defaultBlockState();
                }
                // Schießscharten: schmale Fenster mit Kupfergittern
                if (!corner && side == 0 && (k == 6 || k == 7 || k == 10 || k == 11) && !(dz == TOWER_HALF && k <= 7)) {
                    state = s(Blocks.COPPER_BARS.waxed().unaffected());
                }
                this.set(x, y, z, state);
            }
        }

        /** Der obere Abschluss eines Bogens (eine umgedrehte Treppe, die zur Mitte hin ansteigt). */
        BlockState archStair(int dx, int dz) {
            Direction facing;
            if (Math.abs(dx) == TOWER_HALF) {
                facing = dz > 0 ? Direction.NORTH : Direction.SOUTH;
            } else {
                facing = dx > 0 ? Direction.WEST : Direction.EAST;
            }
            return s(AbyssBlocks.CHAOS_STONE_BRICKS.stairs().get()).setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, Half.TOP);
        }

        /** Das Innere einer Säule: Treppe, Kern mit Pendel, die Böden der Kammern. */
        void inside(int x, int z, int dx, int dz, int lane) {
            for (int y = this.f; y < this.f + ROOF; y++) {
                if (!this.get(x, y, z).isAir()) {
                    this.set(x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
            if (lane <= 1) {
                this.shaft(x, z, dx, dz);
                return;
            }
            if (lane == 2) {
                // der Kern: unten Ziegel mit Sehschlitzen, in der Uhrwerkkammer Uhrwerk
                for (int y = this.f; y < this.f + BELL_ROOM; y++) {
                    int k = y - this.f;
                    boolean slit = (dx == 0 || dz == 0) && (k % 8 == 4 || k % 8 == 5) && k < GEAR_ROOM;
                    BlockState state = slit ? s(Blocks.COPPER_BARS.waxed().unaffected())
                            : k >= GEAR_ROOM ? s(ClockTower.CLOCKWORK.get()) : this.brick(x, y, z);
                    this.set(x, y, z, state);
                }
                this.set(x, this.f + BELL_ROOM, z, this.polished());
                return;
            }
            int first = step(dx, dz);
            // die Böden der Kammern: offen, wo die Treppe hindurchführt
            for (int floor : new int[] {GEAR_ROOM, BELL_ROOM}) {
                boolean open = false;
                for (int i = first; i <= BELL_ROOM; i += STEPS_PER_TURN) {
                    if (i >= floor - 3 && i < floor) {
                        open = true;
                    }
                }
                if (!open) {
                    this.set(x, this.f + floor, z, floor == BELL_ROOM ? this.polished() : (lane == 4 && (dx + dz) % 2 == 0 ? this.bronze() : this.polished()));
                }
            }
            // die Treppe: je Umlauf eine Stufe in dieser Säule, darunter eine Stütze
            Direction facing = tangent(dx, dz);
            for (int i = first; i <= BELL_ROOM; i += STEPS_PER_TURN) {
                int y = this.f + i;
                this.set(x, y, z, s(AbyssBlocks.CHAOS_STONE_BRICKS.stairs().get()).setValue(StairBlock.FACING, facing));
                if (i > 0) {
                    this.set(x, y - 1, z, this.polished());
                }
            }
        }

        /** In welche Richtung man die Treppe hinaufgeht (mit wachsendem Winkel: im Uhrzeigersinn von oben gesehen). */
        static Direction tangent(int dx, int dz) {
            double a = Math.atan2(dz, dx);
            double tx = -Math.sin(a);
            double tz = Math.cos(a);
            if (Math.abs(tx) > Math.abs(tz)) {
                return tx > 0 ? Direction.EAST : Direction.WEST;
            }
            return tz > 0 ? Direction.SOUTH : Direction.NORTH;
        }

        /** Der hohle Kern: oben das Lager, darin hängt das große Pendel an einer Kette bis fast auf den Boden. */
        void shaft(int x, int z, int dx, int dz) {
            int top = this.f + BELL_ROOM - 1;
            this.set(x, top, z, dx == 0 && dz == 0 ? this.bronze() : s(ClockTower.CLOCKWORK.get()));
            this.set(x, top + 1, z, this.polished());
            if (dx == 0 && dz == 0) {
                for (int y = this.f + 6; y < top; y++) {
                    this.set(x, y, z, s(Blocks.COPPER_CHAIN.waxed().unaffected()).setValue(ChainBlock.AXIS, Direction.Axis.Y));
                }
                this.set(x, this.f + 4, z, s(AbyssGear.CHRONIT_BLOCK.get()));
                this.set(x, this.f + 5, z, this.bronze());
                this.set(x, this.f + 3, z, this.bronze());
            } else if (dx == 0 || dz == 0) {
                this.set(x, this.f + 4, z, this.bronze());              // die Pendelscheibe: ein Kreuz aus Bronze um das Chronit
            }
        }

        /** Auf jeder Seite des Turms ein Zifferblatt (einen Block vor der Mauer): Email, Bronzering, Chronitmarken, Zeiger. */
        void faces() {
            int fc = this.f + 20;
            for (Direction side : Direction.Plane.HORIZONTAL) {
                int ox = side.getStepX() * (TOWER_HALF + 1);
                int oz = side.getStepZ() * (TOWER_HALF + 1);
                for (int u = -5; u <= 5; u++) {
                    for (int v = -5; v <= 5; v++) {
                        double r = Math.sqrt(u * u + v * v);
                        if (r > 4.6) {
                            continue;
                        }
                        // u läuft nach rechts (von vorn gesehen), v nach oben
                        int x = this.tx + ox + (side.getAxis() == Direction.Axis.Z ? (side == Direction.SOUTH ? u : -u) : 0);
                        int z = this.tz + oz + (side.getAxis() == Direction.Axis.X ? (side == Direction.WEST ? u : -u) : 0);
                        int y = fc + v;
                        BlockState state;
                        if (r > 3.6) {
                            state = this.bronze();
                        } else if (u == 0 && v == 0) {
                            state = this.bronze();
                        } else if (u == 0 && v > 0 && v <= 3) {
                            state = this.polished();                        // die Zeiger: eine Minute vor zwölf
                        } else if ((Math.abs(u) == 3 && v == 0) || (u == 0 && v == -3)) {
                            state = s(AbyssGear.CHRONIT_BLOCK.get());
                        } else {
                            state = s(ClockTower.CLOCK_FACE.get());
                        }
                        if (this.ctx.near(x, z, 0)) {
                            this.set(x, y, z, state);
                        }
                    }
                }
                // die Zwölf über dem Zeiger
                int x12 = this.tx + ox;
                int z12 = this.tz + oz;
                this.set(x12, fc + 4, z12, s(AbyssGear.CHRONIT_BLOCK.get()));
            }
        }

        /** Die Glockenstube: oben unter dem Dach hängt die große Glocke aus Uhrbronze mit einem Klöppel aus Chronit. */
        void bellRoom() {
            int top = this.f + ROOF - 1;
            for (int x = this.tx - 4; x <= this.tx + 4; x++) {
                for (int z = this.tz - 4; z <= this.tz + 4; z++) {
                    if (!this.ctx.near(x, z, 0)) {
                        continue;
                    }
                    int dx = x - this.tx;
                    int dz = z - this.tz;
                    int lane = lane(dx, dz);
                    this.set(x, top, z, this.polished());                   // die Decke
                    if (lane <= 1) {
                        this.set(x, top - 1, z, this.bronze());              // die Krone der Glocke
                    }
                    if (lane == 2) {
                        this.set(x, top - 2, z, this.bronze());
                        this.set(x, top - 3, z, this.bronze());
                    }
                    if (lane == 3) {
                        this.set(x, top - 4, z, s(ClockTower.CLOCK_BRONZE.slab().get()));    // der Schlagring
                    }
                    if (lane == 0) {
                        this.set(x, top - 2, z, s(Blocks.COPPER_CHAIN.waxed().unaffected()));
                        this.set(x, top - 3, z, s(Blocks.COPPER_CHAIN.waxed().unaffected()));
                        this.set(x, top - 4, z, s(AbyssGear.CHRONIT_BLOCK.get()));
                    }
                    if (lane == 4 && Math.abs(dx) == 4 && Math.abs(dz) == 4) {
                        this.set(x, top - 1, z, s(Blocks.LANTERN).setValue(LanternBlock.HANGING, true));
                    }
                }
            }
        }

        // ------------------------------------------------------------------ das Dach (abgebrochen; die Spitze schwebt)

        void roof() {
            int base = this.f + ROOF;
            for (int k = 0; k <= 9; k++) {
                int half = TOWER_HALF + 1 - k;
                boolean broken = k >= 4;
                int ox = broken ? 3 : 0;
                int oy = broken ? 9 : 0;
                int oz = broken ? 2 : 0;
                for (int dx = -Math.max(0, half); dx <= Math.max(0, half); dx++) {
                    for (int dz = -Math.max(0, half); dz <= Math.max(0, half); dz++) {
                        int lane = lane(dx, dz);
                        int x = this.tx + dx + ox;
                        int z = this.tz + dz + oz;
                        int y = base + k + oy;
                        if (!this.ctx.near(x, z, 0)) {
                            continue;
                        }
                        if (k >= 7) {
                            // die Spitze: ein Dorn aus Uhrbronze mit Chronit
                            if (dx == 0 && dz == 0) {
                                this.set(x, y, z, k == 9 ? s(AbyssGear.CHRONIT_BLOCK.get()) : s(ClockTower.CLOCK_BRONZE.wall().get()));
                            }
                            continue;
                        }
                        if (lane != Math.max(0, half)) {
                            if (k == 0 || k == 4) {
                                this.set(x, y, z, this.brick(x, y, z));
                            }
                            continue;
                        }
                        // die Bruchkante: in der obersten verbliebenen Lage fehlen Stücke
                        if (k == 3 && this.ctx.rand(x, z, 111) < 0.4) {
                            continue;
                        }
                        if (k == 4 && this.ctx.rand(x, z, 113) < 0.35) {
                            continue;
                        }
                        boolean edge = Math.abs(dx) == half && Math.abs(dz) == half;
                        this.set(x, y, z, edge ? this.bronze() : this.polished());
                    }
                }
            }
            // Trümmer zwischen Dach und Spitze
            for (int i = 0; i < 9; i++) {
                int x = this.tx + (int) Math.round((this.ctx.rand(i, 1, 121) - 0.3) * 9.0);
                int z = this.tz + (int) Math.round((this.ctx.rand(i, 2, 123) - 0.3) * 8.0);
                int y = base + 4 + (int) (this.ctx.rand(i, 3, 125) * 8.0);
                if (this.ctx.near(x, z, 0)) {
                    this.setIfAir(x, y, z, i % 3 == 0 ? this.bronze() : this.polished());
                }
            }
        }

        // ------------------------------------------------------------------ schwebende Zahnräder

        void gear(Gear g) {
            double nx = g.nx();
            double ny = g.ny();
            double nz = g.nz();
            // zwei Achsen in der Ebene der Scheibe
            double ax;
            double ay;
            double az;
            if (Math.abs(ny) < 0.9) {
                ax = -nz;
                ay = 0.0;
                az = nx;
            } else {
                ax = 1.0;
                ay = 0.0;
                az = 0.0;
            }
            double la = Math.sqrt(ax * ax + ay * ay + az * az);
            ax /= la;
            ay /= la;
            az /= la;
            double bx = ny * az - nz * ay;
            double by = nz * ax - nx * az;
            double bz = nx * ay - ny * ax;
            int r = (int) Math.ceil(g.r()) + 1;
            double gx = this.cx + g.x();
            double gy = this.f + g.y();
            double gz = this.cz + g.z();
            for (int x = (int) Math.floor(gx) - r; x <= (int) Math.floor(gx) + r; x++) {
                for (int z = (int) Math.floor(gz) - r; z <= (int) Math.floor(gz) + r; z++) {
                    if (!this.ctx.near(x, z, 0)) {
                        continue;
                    }
                    for (int y = (int) Math.floor(gy) - r; y <= (int) Math.floor(gy) + r; y++) {
                        double px = x + 0.5 - gx;
                        double py = y + 0.5 - gy;
                        double pz = z + 0.5 - gz;
                        double w = px * nx + py * ny + pz * nz;
                        if (Math.abs(w) > 0.55) {
                            continue;
                        }
                        double u = px * ax + py * ay + pz * az;
                        double v = px * bx + py * by + pz * bz;
                        double d = Math.sqrt(u * u + v * v);
                        BlockState state = this.gearBlock(g, d, Math.atan2(v, u));
                        if (state != null) {
                            this.set(x, y, z, state);
                        }
                    }
                }
            }
        }

        @Nullable BlockState gearBlock(Gear g, double d, double angle) {
            double R = g.r();
            if (d > R + 0.4) {
                return null;
            }
            double root = R - 1.0;
            double a = angle - g.turn();
            if (d > root + 0.3) {
                double k = (a / Mth.TWO_PI * g.teeth()) % 1.0;
                if (k < 0) {
                    k += 1.0;
                }
                return Math.abs(k - 0.5) < 0.26 ? this.bronze() : null;
            }
            if (d > root - 1.1) {
                return this.bronze();                                       // der Kranz
            }
            if (d < 0.75) {
                return s(AbyssGear.CHRONIT_BLOCK.get());
            }
            if (d < 1.9) {
                return s(ClockTower.CLOCKWORK.get());                       // die Nabe
            }
            for (int i = 0; i < 4; i++) {
                double sa = g.turn() + i * Mth.HALF_PI;
                double across = Math.abs(Math.sin(angle - sa)) * d;
                if (across < 0.62 && Math.cos(angle - sa) > 0) {
                    return this.bronze();                                   // eine Speiche
                }
            }
            return null;
        }

        // ------------------------------------------------------------------ Uhrgewichte unter dem Zifferblatt

        void weights() {
            for (int i = 0; i < 3; i++) {
                double a = i * Mth.TWO_PI / 3.0 + 0.6;
                int x = this.cx + (int) Math.round(Math.cos(a) * 9.0);
                int z = this.cz + (int) Math.round(Math.sin(a) * 9.0);
                if (!this.box.isInside(x, this.f, z)) {
                    continue;
                }
                // die Unterseite des Kegels an dieser Stelle (nur im eigenen Chunk lesbar – hier liegt sie sicher darin)
                int y = this.f - 2;
                while (y > this.f - 30 && !this.get(x, y - 1, z).isAir()) {
                    y--;
                }
                y--;
                int length = 5 + i * 2;
                for (int k = 0; k < length; k++) {
                    this.set(x, y - k, z, s(Blocks.COPPER_CHAIN.waxed().unaffected()));
                }
                int w = y - length;
                this.set(x, w, z, this.bronze());
                this.set(x, w - 1, z, s(ClockTower.CLOCKWORK.get()));
                this.set(x, w - 2, z, this.bronze());
                this.set(x, w - 3, z, s(ClockTower.CLOCK_BRONZE.wall().get()));
            }
        }

        // ------------------------------------------------------------------ Truhen und Herz

        /** Zwei Truhen: in der Uhrwerkkammer und in der Glockenstube (in Ecken, über denen die Treppe hoch genug ist). */
        void chests(RandomSource random, @Nullable ClockTowerPiece piece) {
            this.chest(random, piece, gearRoomChest(new BlockPos(this.cx, this.f, this.cz)));
            this.chest(random, piece, bellRoomChest(new BlockPos(this.cx, this.f, this.cz)));
        }

        void chest(RandomSource random, @Nullable ClockTowerPiece piece, BlockPos pos) {
            if (!this.box.isInside(pos)) {
                return;
            }
            Direction facing = Direction.fromYRot(Math.toDegrees(Math.atan2(this.tz - pos.getZ(), this.tx - pos.getX())) - 90.0);
            BlockState state = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing);
            if (piece != null) {
                piece.createChest(this.level, this.box, random, pos, ModLootTables.CLOCK_TOWER_CHEST, state);
            } else {
                this.level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                if (this.level.getBlockEntity(pos) instanceof ChestBlockEntity be) {
                    be.setLootTable(ModLootTables.CLOCK_TOWER_CHEST, random.nextLong());
                }
            }
        }

        void heart() {
            BlockPos pos = ClockTowerPiece.heart(new BlockPos(this.cx, this.f, this.cz));
            if (!this.box.isInside(pos)) {
                return;
            }
            this.level.setBlock(pos, s(ClockTower.CLOCK_HEART.get()), Block.UPDATE_CLIENTS);
            if (this.level.getBlockEntity(pos) instanceof ClockHeartBlockEntity be) {
                be.setHome(pos.above());
            }
        }
    }

    /** Die Truhe der Uhrwerkkammer: in einer äußeren Ecke, über der die Treppe erst weit oben vorbeiführt. */
    public static BlockPos gearRoomChest(BlockPos center) {
        return roomCorner(center, GEAR_ROOM, 21);
    }

    /** Die Truhe der Glockenstube. */
    public static BlockPos bellRoomChest(BlockPos center) {
        return roomCorner(center, BELL_ROOM, 8);
    }

    /** Eine Ecke des Turms (äußere Bahn), deren erste Treppenstufe möglichst nah an {@code wanted} liegt (für Truhen). */
    private static BlockPos roomCorner(BlockPos center, int floor, int wanted) {
        BlockPos tower = tower(center);
        int best = Integer.MAX_VALUE;
        BlockPos out = tower.offset(4, floor + 1, 4);
        for (int[] c : new int[][] {{4, 4}, {-4, 4}, {-4, -4}, {4, -4}}) {
            int diff = Math.abs(Builder.step(c[0], c[1]) - wanted);
            if (diff < best) {
                best = diff;
                out = tower.offset(c[0], floor + 1, c[1]);
            }
        }
        return out;
    }
}
