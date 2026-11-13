package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.block.end.VoidThroneBlock;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.PhaseGear;
import io.github.sirschrolli.kingdomsatwar.registry.PhaseSanctuary;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Das Phasenheiligtum (seit 1.5): Über einer der äußeren Inseln des Ends schwebt eine runde Plattform aus Endsteinziegeln,
 * unter der ein Zapfen aus Endstein und Obsidian hängt, durchsetzt von Phasiterz. Ihr Boden ist ein Mosaik – in der Mitte ein
 * Phasitblock in einem Ring aus Purpur, von dem acht Strahlen aus Purpur ausgehen, ein Ring aus Obsidian mit Phasit an den
 * Enden der Strahlen, ein Rand aus Ziegeln mit einer niedrigen Mauer. An vier Stellen ist der Boden eingebrochen: Man sieht
 * hinab auf die Insel. Acht Purpursäulen tragen Kristalle aus Phasit. Hinten erhebt sich auf drei Stufen der Leerenthron
 * vor einer Wand aus Obsidian mit einem Auge aus Phasit; zu beiden Seiten des Throns stehen die Truhen. Vor den Stufen kniet
 * Phasmor, der Leerwandlerkönig, und hält Wache. Vorn führen schwebende Stufen hinab auf die Insel.
 *
 * <p>Eigene Koordinaten: Mitte der Plattform bei (0, 0, 0); y = 0 ist die Höhe, auf der man steht. Vorn (+z) liegt der
 * Zugang mit den schwebenden Stufen; der Thron steht hinten (−z) und blickt nach vorn.
 */
final class PhaseSanctum {
    /** Halbmesser der Plattform. */
    static final int R = 15;
    /** So hoch schwebt die Plattform über der Insel. */
    static final int LIFT = 9;
    /** So tief hängt der Zapfen unter der Plattform. */
    static final int DEPTH = 14;
    /** Höchstens so viele schwebende Stufen. */
    static final int MAX_STEPS = 16;
    /** Der Leerenthron (auf drei Stufen, blickt nach vorn). */
    static final int[] THRONE = {0, 3, -11};
    /** Die eingebrochenen Stellen im Boden: Winkel (Grad), Abstand von der Mitte, Halbmesser. */
    private static final double[][] HOLES = {{28, 11.5, 1.8}, {148, 11.0, 2.0}, {212, 11.5, 1.6}, {330, 10.5, 1.9}};

    private PhaseSanctum() {}

    private static BlockState s(net.minecraft.world.level.block.Block block) {
        return block.defaultBlockState();
    }

    // =====================================================================================
    // Planung
    // =====================================================================================

    private static final int[][] PROBES = {{0, 0}, {10, 0}, {-10, 0}, {0, 10}, {0, -10}};

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 11), 4)];
        // eine Insel darunter: die Mitte hoch genug, die Proben nicht zu tief darunter (das Heiligtum schwebt darüber)
        int center = terrain.ground(x, z);
        if (center < 50) {
            return Optional.empty();
        }
        int top = center;
        for (int[] p : PROBES) {
            BlockPos q = new BlockPos(p[0], 0, p[1]).rotate(rotation);
            int h = terrain.ground(x + q.getX(), z + q.getZ());
            if (h < center - 16) {
                return Optional.empty();
            }
            top = Math.max(top, h);
        }
        int y = top + LIFT;
        if (!terrain.valid(x, center, z)) {
            return Optional.empty();
        }
        BlockPos origin = new BlockPos(x, y, z);
        // die schwebenden Stufen: so viele, bis eine die Insel erreicht
        int steps = MAX_STEPS;
        for (int i = 0; i < MAX_STEPS; i++) {
            BlockPos q = origin.offset(new BlockPos(0, 0, R + 2 + i * 2).rotate(rotation));
            if (terrain.ground(q.getX(), q.getZ()) >= y - 2 - i) {
                steps = i;
                break;
            }
        }
        SitePiece piece = piece(origin, rotation, seed, steps);
        return Optional.of(new Structure.GenerationStub(origin, builder -> builder.addPiece(piece)));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed, int steps) {
        return new SitePiece(SiteKind.PHASE_SANCTUARY, 0, origin, rotation, seed, new int[] {steps},
                SitePiece.box(origin, rotation, -R - 3, -DEPTH - 4, -R - 3, R + 3, 14, R + 4 + MAX_STEPS * 2));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Sanctum(b, piece.seed(), piece.data(0)).build();
    }

    /** Der Platz des Throns in eigenen Koordinaten (für Tests und Befehle). */
    static BlockPos throne() {
        return new BlockPos(THRONE[0], THRONE[1], THRONE[2]);
    }

    /** Die acht Säulen (Halbmesser 13, versetzt zu den Strahlen). */
    static int[][] pillars() {
        int[][] out = new int[8][];
        for (int k = 0; k < 8; k++) {
            double a = Math.toRadians(22.5 + 45.0 * k);
            out[k] = new int[] {(int) Math.round(Math.cos(a) * 13.0), (int) Math.round(Math.sin(a) * 13.0)};
        }
        return out;
    }

    /** Liegt die Stelle in einem der eingebrochenen Löcher? */
    static boolean hole(double x, double z) {
        for (double[] h : HOLES) {
            double a = Math.toRadians(h[0]);
            double dx = x - Math.cos(a) * h[1];
            double dz = z - Math.sin(a) * h[1];
            if (dx * dx + dz * dz <= h[2] * h[2]) {
                return true;
            }
        }
        return false;
    }

    private static final class Sanctum {
        private final VillageBuilder b;
        private final long seed;
        private final int steps;

        Sanctum(VillageBuilder b, long seed, int steps) {
            this.b = b;
            this.seed = seed;
            this.steps = steps;
        }

        void build() {
            this.clear();
            this.underside();
            this.platform();
            this.rim();
            for (int[] p : pillars()) {
                this.pillar(p[0], p[1]);
            }
            this.dais();
            this.backWall();
            this.stepsDown();
        }

        /** Über der Plattform ist alles frei (wo eine Insel hineinragt, ist sie fortgerissen). */
        private void clear() {
            for (int x = -R - 1; x <= R + 1; x++) {
                for (int z = -R - 1; z <= R + 1; z++) {
                    if (x * x + z * z > (R + 1.5) * (R + 1.5)) {
                        continue;
                    }
                    for (int y = 0; y <= 12; y++) {
                        if (!this.b.get(x, y, z).isAir()) {
                            this.b.set(x, y, z, s(Blocks.AIR));
                        }
                    }
                }
            }
        }

        /** Der Zapfen unter der Plattform: Endstein, nach unten spitz, mit Adern aus Obsidian und Phasiterz. */
        private void underside() {
            for (int x = -R; x <= R; x++) {
                for (int z = -R; z <= R; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r > R + 0.5) {
                        continue;
                    }
                    double k = r / (R + 0.5);
                    int depth = 2 + (int) Math.round((DEPTH - 2) * Math.pow(1.0 - k, 1.4) + (this.b.noise(x, 3, z) - 0.5) * 3.0);
                    for (int dy = 2; dy <= depth; dy++) {
                        int y = -dy;
                        float n = this.b.noise(x, y, z);
                        BlockState state;
                        if (n > 0.78 && dy > 3) {
                            state = s(Blocks.OBSIDIAN);
                        } else if (n < 0.06 && dy > 2) {
                            state = PhaseGear.PHASIT_ORE.get().defaultBlockState();
                        } else {
                            state = s(Blocks.END_STONE);
                        }
                        this.b.set(x, y, z, state);
                    }
                }
            }
        }

        /** Das Mosaik des Bodens: Phasit in der Mitte, ein Ring aus Purpur, acht Strahlen, ein Ring aus Obsidian, der Rand. */
        private BlockState mosaic(int x, int z) {
            double r = Math.sqrt(x * x + z * z);
            if (r <= 1.0) {
                return PhaseGear.PHASIT_BLOCK.get().defaultBlockState();
            }
            if (r <= 2.6) {
                return Blocks.PURPUR_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
            }
            if (r > R - 1.6) {
                return s(Blocks.END_STONE_BRICKS);
            }
            double angle = Math.atan2(z, x);
            double step = Math.PI / 4.0;
            double off = angle - Math.round(angle / step) * step;
            boolean ray = Math.abs(Math.sin(off)) * r < 0.75;
            if (r >= 10.6 && r <= 11.6) {
                return ray ? PhaseGear.PHASIT_BLOCK.get().defaultBlockState() : s(Blocks.OBSIDIAN);
            }
            if (r >= 3.2 && r <= 10.2 && ray) {
                return s(Blocks.PURPUR_BLOCK);
            }
            return s((x * 7 + z * 13) % 11 == 0 ? Blocks.CHISELED_STONE_BRICKS : Blocks.END_STONE_BRICKS);
        }

        private void platform() {
            for (int x = -R; x <= R; x++) {
                for (int z = -R; z <= R; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r > R + 0.5) {
                        continue;
                    }
                    if (hole(x, z)) {
                        // eingebrochen: bis hinab durch den Zapfen
                        for (int y = -1; y >= -DEPTH - 2; y--) {
                            if (!this.b.get(x, y, z).isAir()) {
                                this.b.set(x, y, z, s(Blocks.AIR));
                            }
                        }
                        continue;
                    }
                    this.b.set(x, -1, z, this.mosaic(x, z));
                    if (hole(x + 1, z) || hole(x - 1, z) || hole(x, z + 1) || hole(x, z - 1)) {
                        // die Bruchkante: Risse und Purpursplitter
                        this.b.set(x, -1, z, s(this.b.noise(x, -1, z) > 0.5 ? Blocks.CRACKED_STONE_BRICKS : Blocks.END_STONE));
                    }
                }
            }
        }

        /** Die Mauer am Rand, offen am Zugang (vorn) und wo der Boden eingebrochen ist; dazwischen Endstäbe. */
        private void rim() {
            for (int x = -R; x <= R; x++) {
                for (int z = -R; z <= R; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r > R + 0.5 || r <= R - 0.5 || hole(x, z) || z > 0 && Math.abs(x) <= 1) {
                        continue;
                    }
                    this.b.set(x, 0, z, s(Blocks.END_STONE_BRICK_WALL));
                }
            }
        }

        /** Eine Säule aus Purpur, oben ein Kristall aus Phasit, um den Endstäbe ragen. */
        private void pillar(int px, int pz) {
            for (int y = 0; y <= 5; y++) {
                this.b.set(px, y, pz, Blocks.PURPUR_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
            }
            this.b.set(px, 6, pz, s(Blocks.PURPUR_BLOCK));
            this.b.set(px, 7, pz, PhaseGear.PHASIT_BLOCK.get().defaultBlockState());
            this.b.set(px, 8, pz, PhaseGear.PHASIT_BLOCK.get().defaultBlockState());
            this.b.set(px, 9, pz, Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP));
            for (Direction d : Direction.Plane.HORIZONTAL) {
                this.b.set(px + d.getStepX(), 6, pz + d.getStepZ(), VillageBuilder.stairs(Blocks.PURPUR_STAIRS, d.getOpposite(), Half.TOP));
                this.b.set(px + d.getStepX(), 7, pz + d.getStepZ(), Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, d));
            }
        }

        /**
         * Drei Stufen hinauf zum Leerenthron: vorn (z = -8) die erste, dahinter die zweite, dann der Absatz (z = -10 bis -13,
         * drei Blöcke hoch) mit dem Thron in der Mitte, zu beiden Seiten die Truhen, an den Ecken Endstäbe.
         */
        private void dais() {
            for (int x = -5; x <= 5; x++) {
                for (int z = -13; z <= -8; z++) {
                    int height = Math.abs(x) == 5 ? 3 : z == -8 ? 1 : z == -9 ? 2 : 3;
                    for (int y = 0; y < height; y++) {
                        BlockState state;
                        boolean top = y == height - 1;
                        if (top && Math.abs(x) < 5 && z == -8) {
                            state = VillageBuilder.stairs(Blocks.END_STONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM);
                        } else if (top && Math.abs(x) < 5 && z == -9) {
                            state = VillageBuilder.stairs(Blocks.PURPUR_STAIRS, Direction.NORTH, Half.BOTTOM);
                        } else if (top && Math.abs(x) <= 1) {
                            state = s(Blocks.PURPUR_BLOCK);
                        } else if (Math.abs(x) == 5) {
                            state = top ? s(Blocks.PURPUR_BLOCK) : Blocks.PURPUR_PILLAR.defaultBlockState()
                                    .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
                        } else {
                            state = s(Blocks.END_STONE_BRICKS);
                        }
                        this.b.set(x, y, z, state);
                    }
                }
            }
            this.b.set(THRONE[0], THRONE[1], THRONE[2], PhaseSanctuary.VOID_THRONE.get().defaultBlockState()
                    .setValue(VoidThroneBlock.FACING, Direction.SOUTH).setValue(VoidThroneBlock.PHASE, VoidThroneBlock.Phase.GUARDED));
            for (int x : new int[] {-3, 3}) {
                this.b.chest(x, 3, -12, s(Blocks.CHEST).setValue(ChestBlock.FACING, Direction.SOUTH), ModLootTables.SITE_PHASE_SANCTUARY);
            }
            for (int[] c : new int[][] {{-4, -10}, {4, -10}, {-4, -13}, {4, -13}}) {
                this.b.set(c[0], 3, c[1], Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP));
            }
        }

        /** Hinter dem Thron eine Wand aus Obsidian zwischen zwei Säulen, darin ein Auge aus Phasit; oben eine Zinne aus Purpur. */
        private void backWall() {
            int z = -14;
            for (int x = -5; x <= 5; x++) {
                for (int y = 0; y <= 11; y++) {
                    BlockState state;
                    if (Math.abs(x) == 5) {
                        state = y == 11 ? s(Blocks.PURPUR_BLOCK)
                                : Blocks.PURPUR_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
                    } else if (y == 11) {
                        state = Math.abs(x) % 2 == 0 ? s(Blocks.PURPUR_BLOCK)
                                : Blocks.PURPUR_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
                    } else if (y > 10 - Math.abs(x) / 2 && y < 11) {
                        state = s(Blocks.PURPUR_BLOCK);
                    } else if (eye(x, y)) {
                        state = PhaseGear.PHASIT_BLOCK.get().defaultBlockState();
                    } else if (y <= 2) {
                        state = s(Blocks.END_STONE_BRICKS);
                    } else {
                        state = s(this.b.noise(x, y, z) > 0.82 ? Blocks.CRYING_OBSIDIAN : Blocks.OBSIDIAN);
                    }
                    this.b.set(x, y, z, state);
                }
            }
        }

        /** Das Auge in der Wand: eine liegende Mandel aus Phasit mit einer Pupille aus Obsidian. */
        private static boolean eye(int x, int y) {
            double dx = x / 3.6;
            double dy = (y - 7.0) / 1.7;
            return dx * dx + dy * dy <= 1.0 && !(x == 0 && y == 7);
        }

        /** Vorn schweben Stufen aus Endsteinziegeln hinab auf die Insel (jede eine Höhe tiefer, mit Abstand). */
        private void stepsDown() {
            for (int i = 0; i < this.steps; i++) {
                int z = R + 2 + i * 2;
                int y = -2 - i;
                for (int x = -1; x <= 1; x++) {
                    for (int dz = 0; dz <= 1; dz++) {
                        boolean edge = x != 0 || dz == 1;
                        this.b.set(x, y, z + dz, s(edge && (i + x) % 3 == 0 ? Blocks.PURPUR_BLOCK : Blocks.END_STONE_BRICKS));
                    }
                }
                if (i % 4 == 1) {
                    this.b.set(-1, y + 1, z, Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP));
                }
            }
        }
    }
}
