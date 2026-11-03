package io.github.sirschrolli.kingdomsatwar.worldgen.tower;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

/**
 * Die Arena eines Erzmagiers in der Arkanen Sphäre: eine schwebende Felsscheibe in den Steinen seines Turms. Im Boden
 * das Ritualmuster des Sanktums (Hexagramm im Ring, Lichtstein in der Mitte), am Rand eine niedrige Brüstung mit acht
 * Säulen und Feuerschalen; darunter läuft der Fels zu einem Zapfen mit leuchtender Spitze aus. Ringsum schweben Felsinseln
 * mit Kristallschreinen und Ruinen und vier Runenobelisken.
 */
public final class MageArenaBuilder {
    /** Halbmesser der Arena (Boden). */
    public static final int RADIUS = 18;
    /** So weit reicht alles, was zur Arena gehört (Inseln, Obelisken), um die Mitte. */
    public static final int REACH = 46;
    private static final double STAR = 9.5;

    private final ServerLevel level;
    private final BlockPos center;
    private final TowerPalette p;
    private final RandomSource random;
    private final List<BlockPos> shaped = new ArrayList<>();

    private MageArenaBuilder(ServerLevel level, BlockPos center, DragonType element) {
        this.level = level;
        this.center = center;
        this.p = TowerPalette.of(element);
        this.random = RandomSource.create(center.asLong() ^ element.ordinal() * 0x9E3779B97F4A7C15L);
    }

    /** Baut die Arena mit ihrer Mitte (Bodenblock) bei {@code center}; was dort vorher stand, wird abgeräumt. */
    public static void build(ServerLevel level, BlockPos center, DragonType element) {
        new MageArenaBuilder(level, center, element).run();
    }

    /** Die Chunks, die zur Arena gehören (zum Laden und Festhalten). */
    public static List<long[]> chunks(BlockPos center) {
        List<long[]> out = new ArrayList<>();
        for (int cx = (center.getX() - REACH) >> 4; cx <= (center.getX() + REACH) >> 4; cx++) {
            for (int cz = (center.getZ() - REACH) >> 4; cz <= (center.getZ() + REACH) >> 4; cz++) {
                out.add(new long[] {cx, cz});
            }
        }
        return out;
    }

    /** Wo die Kämpfer erscheinen: im Süden der Arena auf einem Bogen, mit Blick zur Mitte. */
    public static List<Vec3> spawnPoints(BlockPos center, int count) {
        List<Vec3> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int row = i / 7;
            int col = i % 7;
            double angle = Math.toRadians((col - 3) * 12.0 + (row % 2) * 6.0);
            double r = 12.5 + row * 1.6;
            out.add(new Vec3(center.getX() + 0.5 + Math.sin(angle) * r, center.getY() + 1.0, center.getZ() + 0.5 + Math.cos(angle) * r));
        }
        return out;
    }

    private void run() {
        this.clear();
        this.platform();
        this.underside();
        this.rim();
        this.islands();
        this.obelisks();
        for (BlockPos pos : this.shaped) {
            BlockState state = this.level.getBlockState(pos);
            BlockState shaped = Block.updateFromNeighbourShapes(state, this.level, pos);
            if (shaped != state) {
                this.level.setBlock(pos, shaped, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private void set(int x, int y, int z, BlockState state) {
        BlockPos pos = this.center.offset(x, y, z);
        this.level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        Block block = state.getBlock();
        if (block instanceof WallBlock || block instanceof CrossCollisionBlock) {
            this.shaped.add(pos);
        }
    }

    private void set(int x, int y, int z, Block block) {
        this.set(x, y, z, block.defaultBlockState());
    }

    private static BlockState lit(Block block) {
        BlockState state = block.defaultBlockState();
        return state.hasProperty(BlockStateProperties.LIT) ? state.setValue(BlockStateProperties.LIT, true) : state;
    }

    private static BlockState pillar(Block block) {
        BlockState state = block.defaultBlockState();
        return state.hasProperty(RotatedPillarBlock.AXIS) ? state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y) : state;
    }

    private static BlockState stairs(Block block, Direction facing, Half half) {
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(StairBlock.FACING)) {
            state = state.setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
        }
        return state;
    }

    private static BlockState slab(Block block, SlabType type) {
        BlockState state = block.defaultBlockState();
        return state.hasProperty(SlabBlock.TYPE) ? state.setValue(SlabBlock.TYPE, type) : state;
    }

    private static double dist(double x, double z) {
        return Math.sqrt(x * x + z * z);
    }

    /** Eine der drei Mauersteine (gestreut), nach Lage reproduzierbar. */
    private BlockState stone(int x, int y, int z) {
        long h = Mth.getSeed(this.center.getX() + x, this.center.getY() + y, this.center.getZ() + z);
        int k = (int) Math.floorMod(h >> 8, 10);
        return (k < 6 ? this.p.wall() : k < 9 ? this.p.wall2() : this.p.wall3()).defaultBlockState();
    }

    private double noise(int x, int z, int salt) {
        long h = Mth.getSeed(this.center.getX() + x * 3, salt, this.center.getZ() + z * 7);
        return (Math.floorMod(h >> 12, 1000)) / 1000.0;
    }

    private static double segmentDistance(double px, double pz, double ax, double az, double bx, double bz) {
        double dx = bx - ax;
        double dz = bz - az;
        double t = Mth.clamp(((px - ax) * dx + (pz - az) * dz) / (dx * dx + dz * dz), 0.0, 1.0);
        double cx = ax + t * dx - px;
        double cz = az + t * dz - pz;
        return Math.sqrt(cx * cx + cz * cz);
    }

    // =====================================================================================
    // Teile
    // =====================================================================================

    /** Abräumen, was von einem früheren Kampf an dieser Stelle steht. */
    private void clear() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -REACH; x <= REACH; x++) {
            for (int z = -REACH; z <= REACH; z++) {
                for (int y = -34; y <= 24; y++) {
                    pos.setWithOffset(this.center, x, y, z);
                    if (!this.level.getBlockState(pos).isAir()) {
                        this.level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    }
                }
            }
        }
    }

    /** Der Boden: Ritualmuster wie im Sanktum, Ringe, glatter Rand. */
    private void platform() {
        double[][] star = new double[6][];
        for (int k = 0; k < 6; k++) {
            double a = Math.toRadians(30 + k * 60);
            star[k] = new double[] {Math.cos(a) * STAR, Math.sin(a) * STAR};
        }
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                double d = dist(x, z);
                if (d > RADIUS + 0.5) {
                    continue;
                }
                boolean line = false;
                for (int k = 0; k < 6; k++) {
                    line |= segmentDistance(x, z, star[k][0], star[k][1], star[(k + 2) % 6][0], star[(k + 2) % 6][1]) < 0.5;
                }
                BlockState state;
                if (d <= 0.5) {
                    state = lit(this.p.glow());
                } else if (d <= 1.5 || line || Math.abs(d - (STAR + 0.9)) < 0.5) {
                    state = this.p.trim().defaultBlockState();
                } else if (d > RADIUS - 0.6) {
                    state = this.p.trim().defaultBlockState();
                } else if (Math.abs(d - 14.5) < 0.55) {
                    state = this.p.floorAccent().defaultBlockState();
                } else if (d > 15.0) {
                    state = this.noise(x, z, 11) < 0.12 ? this.p.pave2().defaultBlockState() : this.p.pave().defaultBlockState();
                } else {
                    state = this.noise(x, z, 12) < 0.06 ? this.p.floorAccent().defaultBlockState() : this.p.floor().defaultBlockState();
                }
                this.set(x, 0, z, state);
            }
        }
        // Lichtpunkte an den Spitzen des Sterns und auf dem äußeren Ring
        for (double[] point : star) {
            this.set((int) Math.round(point[0]), 0, (int) Math.round(point[1]), lit(this.p.glow()));
        }
        for (int k = 0; k < 12; k++) {
            double a = Math.toRadians(k * 30 + 15);
            this.set((int) Math.round(Math.cos(a) * 14.5), 0, (int) Math.round(Math.sin(a) * 14.5), lit(this.p.glow()));
        }
    }

    /** Der Fels unter der Scheibe: ein Zapfen, der in eine leuchtende Spitze ausläuft, mit Gesims unter dem Rand. */
    private void underside() {
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                double d = dist(x, z);
                if (d > RADIUS + 0.5) {
                    continue;
                }
                double t = Math.min(1.0, d / (RADIUS + 0.5));
                int depth = (int) Math.round(3 + 24 * Math.pow(1.0 - t, 1.6) + this.noise(x, z, 21) * 3.0);
                for (int y = -1; y >= -depth; y--) {
                    BlockState state;
                    if (y == -1 && d > RADIUS - 1.5) {
                        state = this.p.trim().defaultBlockState();
                    } else if (y == -2 && d > RADIUS - 1.6) {
                        state = stairs(this.p.trimStairs(), outward(x, z), Half.TOP);
                    } else if (y < -depth + 2 && this.noise(x, z, 22) < 0.5) {
                        state = this.p.foundation().defaultBlockState();
                    } else {
                        state = this.stone(x, y, z);
                    }
                    this.set(x, y, z, state);
                }
            }
        }
        // die leuchtende Spitze und hängende Lichter unter dem Rand
        int tip = -30;
        this.set(0, tip + 2, 0, this.p.trim().defaultBlockState());
        this.set(0, tip + 1, 0, lit(this.p.glow()));
        for (int k = 0; k < 8; k++) {
            double a = Math.toRadians(k * 45 + 22.5);
            int x = (int) Math.round(Math.cos(a) * (RADIUS - 3));
            int z = (int) Math.round(Math.sin(a) * (RADIUS - 3));
            int bottom = -1;
            while (bottom > -30 && !this.level.getBlockState(this.center.offset(x, bottom - 1, z)).isAir()) {
                bottom--;
            }
            this.set(x, bottom - 1, z, Blocks.IRON_CHAIN.defaultBlockState());
            this.set(x, bottom - 2, z, Blocks.IRON_CHAIN.defaultBlockState());
            this.set(x, bottom - 3, z, this.p.lantern().defaultBlockState().trySetValue(BlockStateProperties.HANGING, true));
        }
    }

    private static Direction outward(int x, int z) {
        if (Math.abs(x) > Math.abs(z)) {
            return x > 0 ? Direction.WEST : Direction.EAST;
        }
        return z > 0 ? Direction.NORTH : Direction.SOUTH;
    }

    /** Die Brüstung: niedrige Mauer am Rand, acht Säulen mit Feuerschalen. */
    private void rim() {
        List<int[]> pillars = new ArrayList<>();
        for (int k = 0; k < 8; k++) {
            double a = Math.toRadians(k * 45 + 22.5);
            pillars.add(new int[] {(int) Math.round(Math.cos(a) * (RADIUS - 0.8)), (int) Math.round(Math.sin(a) * (RADIUS - 0.8))});
        }
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                double d = dist(x, z);
                if (d > RADIUS - 0.6 && d <= RADIUS + 0.5) {
                    this.set(x, 1, z, this.p.trimWall().defaultBlockState());
                }
            }
        }
        for (int[] c : pillars) {
            int x = c[0];
            int z = c[1];
            for (int y = 1; y <= 4; y++) {
                this.set(x, y, z, pillar(this.p.pillar()));
            }
            this.set(x, 5, z, this.p.trim().defaultBlockState());
            this.set(x, 6, z, lit(this.p.glow()));
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                this.set(x + dir.getStepX(), 5, z + dir.getStepZ(), stairs(this.p.trimStairs(), dir.getOpposite(), Half.TOP));
            }
        }
    }

    /** Schwebende Felsinseln: abwechselnd ein Kristallschrein und eine Ruine mit zwei Säulen. */
    private void islands() {
        for (int k = 0; k < 6; k++) {
            double a = Math.toRadians(k * 60 + 30 + (this.random.nextDouble() - 0.5) * 18);
            double r = 32 + this.random.nextDouble() * 7;
            int cx = (int) Math.round(Math.cos(a) * r);
            int cz = (int) Math.round(Math.sin(a) * r);
            int top = -4 + this.random.nextInt(9);
            int radius = 3 + this.random.nextInt(3);
            this.island(cx, top, cz, radius, k % 2 == 0);
        }
    }

    private void island(int cx, int top, int cz, int radius, boolean shrine) {
        for (int x = -radius - 1; x <= radius + 1; x++) {
            for (int z = -radius - 1; z <= radius + 1; z++) {
                double d = dist(x, z) + this.noise(cx + x, cz + z, 31) * 0.8;
                if (d > radius + 0.4) {
                    continue;
                }
                int depth = (int) Math.round(2 + (radius - d) * 1.8 + this.noise(cx + x, cz + z, 32) * 2);
                this.set(cx + x, top, cz + z, d > radius - 0.8 ? this.p.edge().defaultBlockState() : this.p.pave().defaultBlockState());
                for (int y = top - 1; y >= top - depth; y--) {
                    this.set(cx + x, y, cz + z, this.stone(cx + x, y, cz + z));
                }
            }
        }
        if (shrine) {
            // Kristallschrein: Sockel, leuchtender Kristall, darüber ein Ring aus Stufen
            this.set(cx, top + 1, cz, this.p.trim().defaultBlockState());
            this.set(cx, top + 2, cz, lit(this.p.glow()));
            this.set(cx, top + 3, cz, lit(this.p.glow()));
            this.set(cx, top + 4, cz, slab(this.p.trimSlab(), SlabType.BOTTOM));
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                this.set(cx + dir.getStepX(), top + 1, cz + dir.getStepZ(), stairs(this.p.trimStairs(), dir.getOpposite(), Half.BOTTOM));
            }
        } else {
            // Ruine: zwei Säulen, eine gebrochen, ein Sturz dazwischen liegt herabgefallen daneben
            int dx = this.random.nextBoolean() ? 1 : 0;
            int dz = 1 - dx;
            for (int y = 1; y <= 4; y++) {
                this.set(cx - dx * 2, top + y, cz - dz * 2, pillar(this.p.pillar()));
            }
            this.set(cx - dx * 2, top + 5, cz - dz * 2, this.p.trim().defaultBlockState());
            for (int y = 1; y <= 2; y++) {
                this.set(cx + dx * 2, top + y, cz + dz * 2, pillar(this.p.pillar()));
            }
            this.set(cx + dx * 2, top + 3, cz + dz * 2, slab(this.p.trimSlab(), SlabType.BOTTOM));
            this.set(cx + dz, top + 1, cz + dx, this.p.trim().defaultBlockState());
            this.set(cx, top + 1, cz, this.p.lantern().defaultBlockState());
        }
    }

    /** Vier Runenobelisken, die frei im Abgrund schweben. */
    private void obelisks() {
        for (int k = 0; k < 4; k++) {
            double a = Math.toRadians(k * 90 + 15);
            int x = (int) Math.round(Math.cos(a) * 25);
            int z = (int) Math.round(Math.sin(a) * 25);
            int base = 3 + (k % 2) * 3;
            this.set(x, base, z, lit(this.p.glow()));
            for (int y = base + 1; y <= base + 6; y++) {
                this.set(x, y, z, pillar(this.p.pillar()));
            }
            this.set(x, base + 7, z, this.p.trim().defaultBlockState());
            this.set(x, base + 8, z, lit(this.p.glow()));
        }
    }
}
