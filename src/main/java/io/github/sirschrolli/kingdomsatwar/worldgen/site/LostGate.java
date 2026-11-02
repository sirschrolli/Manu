package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Verlorene Pforte: Auf dieser Seite kamen die Feuerdiener an, die einst den Weg in die Unterwelt suchten – und
 * kamen nicht mehr zurück. Ihr Tor steht noch auf einem Stufenpodest aus Schwarzstein: ein Rahmen aus Obsidian unter
 * einem Spitzbogen, vier Obelisken aus Basalt mit Seelenfeuer ringsum. Einige Steine sind herausgebrochen und liegen auf
 * den Stufen; wer sie wieder einsetzt, kann das Tor entzünden. Daneben ihr letztes Lager mit kalter Feuerstelle,
 * Schlafrollen und einer Truhe. Steht die Pforte im Lavameer, trägt sie eine Insel aus Basalt (an Stelle der Portalruine
 * des Nethers).
 *
 * <p>Eigene Koordinaten: (0, 0, 0) ist der erste freie Block über dem Boden (oder über der Lava) in der Mitte; das Tor
 * steht quer zur z-Achse, die Treppe des Lagers liegt vorn (+z).
 */
final class LostGate {
    static final int GROUND = 0;
    static final int LAVA = 1;
    /** Der Rahmen aus Obsidian: außen x = −2 … 2, y = 3 … 8, bei z = 0 (innen drei breit, vier hoch). */
    static final int FRAME_Y = 3;
    private static final int REACH = 12;
    private static final int[][] PROBES = {{7, 0}, {-7, 0}, {0, 7}, {0, -7}, {5, 5}, {-5, 5}, {5, -5}, {-5, -5}};

    private LostGate() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        RandomSource random = RandomSource.create(seed);
        int sea = terrain.seaLevel();
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 11), 4)];
        boolean preferLava = random.nextFloat() < 0.3f;
        int top = Math.min(terrain.minY() + context.heightAccessor().getHeight(), 128) - 16;
        // ein paar Stellen im Chunk; an jeder von oben nach unten jeder Höhlengrund, der Platz bietet – sonst das Lavameer
        int[][] spots = new int[5][];
        for (int i = 0; i < spots.length; i++) {
            spots[i] = new int[] {chunk.getMinBlockX() + 2 + random.nextInt(12), chunk.getMinBlockZ() + 2 + random.nextInt(12)};
        }
        for (int pass = preferLava ? 0 : 1; pass < 2; pass++) {
            for (int[] spot : spots) {
                int x = spot[0];
                int z = spot[1];
                if (pass == 0) {
                    // der Spiegel des Lavameers liegt eine Schicht unter dem Meeresspiegel des Generators
                    if (terrain.fluid(x, sea - 1, z) && terrain.valid(x, sea, z) && open(terrain, x, z, sea)) {
                        return stub(new BlockPos(x, sea, z), rotation, seed, LAVA);
                    }
                    continue;
                }
                for (int y = top; y > sea + 1; y--) {
                    if (!terrain.rock(x, y, z) && !terrain.fluid(x, y, z) && terrain.rock(x, y - 1, z)
                            && roomy(terrain, x, z, y) && terrain.valid(x, y, z)) {
                        return stub(new BlockPos(x, y, z), rotation, seed, GROUND);
                    }
                }
            }
            if (pass == 1 && !preferLava) {
                // kein Höhlengrund mit Platz: dann eben auf dem Lavameer
                for (int[] spot : spots) {
                    if (terrain.fluid(spot[0], sea - 1, spot[1]) && terrain.valid(spot[0], sea, spot[1]) && open(terrain, spot[0], spot[1], sea)) {
                        return stub(new BlockPos(spot[0], sea, spot[1]), rotation, seed, LAVA);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<Structure.GenerationStub> stub(BlockPos origin, Rotation rotation, long seed, int mode) {
        SitePiece piece = piece(origin, rotation, seed, mode);
        return Optional.of(new Structure.GenerationStub(origin, builder -> builder.addPiece(piece)));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed, int mode) {
        return new SitePiece(SiteKind.LOST_GATE, mode, origin, rotation, seed, new int[] {mode},
                SitePiece.box(origin, rotation, -REACH, mode == LAVA ? -10 : -6, -REACH, REACH, 15, REACH));
    }

    /**
     * Über der Mitte sieben Blöcke frei (den Rest der Kuppel höhlt die Pforte selbst aus), ringsum meist Boden in ähnlicher
     * Höhe mit etwas Platz darüber.
     */
    private static boolean roomy(SiteTerrain terrain, int x, int z, int floor) {
        for (int y = floor; y <= floor + 6; y++) {
            if (terrain.rock(x, y, z) || terrain.fluid(x, y, z)) {
                return false;
            }
        }
        int misses = 0;
        for (int[] p : PROBES) {
            int px = x + p[0];
            int pz = z + p[1];
            int found = Integer.MIN_VALUE;
            for (int y = floor + 3; y >= floor - 4; y--) {
                if (!terrain.rock(px, y, pz) && !terrain.fluid(px, y, pz) && terrain.rock(px, y - 1, pz)) {
                    found = y;
                    break;
                }
            }
            if (found == Integer.MIN_VALUE || terrain.rock(px, found + 3, pz)) {
                misses++;
            }
        }
        return misses <= 3;
    }

    /** Auf der Lava: ringsum Lava oder Luft, darüber vierzehn Blöcke frei. */
    private static boolean open(SiteTerrain terrain, int x, int z, int y) {
        for (int dy = 0; dy <= 13; dy++) {
            if (terrain.rock(x, y + dy, z) || terrain.fluid(x, y + dy, z)) {
                return false;
            }
        }
        for (int[] p : PROBES) {
            for (int dy = 0; dy <= 8; dy += 4) {
                if (terrain.rock(x + p[0], y + dy, z + p[1])) {
                    return false;
                }
            }
        }
        return true;
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Gate(b, piece.seed(), piece.data(0)).build();
    }

    private static final class Gate {
        private static final BlockState AIR = Blocks.AIR.defaultBlockState();
        private final VillageBuilder b;
        private final boolean lava;
        private final RandomSource random;
        /** Welche Steine aus dem Rahmen gebrochen sind (Index in {@link #frame()}), und welche nur gesprungen. */
        private final boolean[] missing;
        private final boolean[] cracked;
        private final boolean archBroken;
        private final int archSide;
        private final int brokenObelisk;
        private final int campSide;

        Gate(VillageBuilder b, long seed, int mode) {
            this.b = b;
            this.lava = mode == LAVA;
            this.random = RandomSource.create(seed ^ 0x3C6EF372FE94F82BL);
            int[][] frame = frame();
            this.missing = new boolean[frame.length];
            this.cracked = new boolean[frame.length];
            int gone = 0;
            for (int i = 0; i < frame.length; i++) {
                boolean corner = Math.abs(frame[i][0]) == 2 && (frame[i][1] == FRAME_Y || frame[i][1] == FRAME_Y + 5);
                float r = this.random.nextFloat();
                if (!corner && r < 0.18f && gone < 4) {
                    this.missing[i] = true;
                    gone++;
                } else if (r > 0.82f) {
                    this.cracked[i] = true;
                }
            }
            if (gone == 0) {
                this.missing[3] = true;                 // mindestens ein Stein fehlt immer
                this.cracked[3] = false;
            }
            this.archBroken = this.random.nextFloat() < 0.6f;
            this.archSide = this.random.nextBoolean() ? 1 : -1;
            this.brokenObelisk = this.random.nextInt(4);
            this.campSide = this.random.nextBoolean() ? 1 : -1;
        }

        /** Die Steine des Rahmens ({x, y}): die Seiten, unten und oben. */
        static int[][] frame() {
            int[][] cells = new int[18][];
            int i = 0;
            for (int y = FRAME_Y; y <= FRAME_Y + 5; y++) {
                cells[i++] = new int[] {-2, y};
                cells[i++] = new int[] {2, y};
            }
            for (int x = -1; x <= 1; x++) {
                cells[i++] = new int[] {x, FRAME_Y};
                cells[i++] = new int[] {x, FRAME_Y + 5};
            }
            return cells;
        }

        static BlockState s(Block block) {
            return block.defaultBlockState();
        }

        void build() {
            this.ground();
            this.seal();
            this.dais();
            this.obelisks();
            this.gate();
            this.camp();
        }

        // =====================================================================================
        // Grund, Podest, Obelisken
        // =====================================================================================

        /** Der Grund: verbrannter Stein rings um das Podest (auf der Lava: eine Insel aus Basalt), darüber frei. */
        void ground() {
            for (int x = -REACH + 1; x <= REACH - 1; x++) {
                for (int z = -REACH + 1; z <= REACH - 1; z++) {
                    double r = Math.sqrt(x * x + z * z) + (this.b.noise(x, 0, z) - 0.5) * 2.5;
                    if (r > 10.5) {
                        continue;
                    }
                    // frei bis in eine flache Kuppel: in der Mitte vierzehn Blöcke, am Rand nur ein paar
                    double clear = 14.0 * Math.sqrt(Math.max(0.0, 1.0 - Math.pow(r / 11.0, 2)));
                    for (int y = 0; y <= clear; y++) {
                        if (!this.b.get(x, y, z).isAir()) {
                            this.b.set(x, y, z, AIR);
                        }
                    }
                    float n = this.b.noise(x, -1, z);
                    if (this.lava) {
                        // die Insel: oben Schwarzstein, darunter Basalt bis tief in die Lava; am Rand niedriger, dort ragen
                        // Basaltsäulen aus der Lava
                        int top = r > 9 ? -2 : -1;
                        if (r > 9.6 && this.b.noise(x, 7, z) < 0.35f) {
                            top = (int) (this.b.noise(x, 8, z) * 3.5f);
                        }
                        for (int y = top; y >= -9; y--) {
                            this.b.set(x, y, z, y == top && top < 0 ? (n < 0.3f ? s(Blocks.BASALT) : s(Blocks.BLACKSTONE))
                                    : Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                        }
                    } else {
                        this.b.set(x, -1, z, n < 0.08f ? s(Blocks.MAGMA_BLOCK) : n < 0.45f ? s(Blocks.BLACKSTONE)
                                : n < 0.6f ? s(Blocks.BASALT) : this.b.get(x, -1, z).isAir() ? s(Blocks.BLACKSTONE) : this.b.get(x, -1, z));
                        for (int y = -2; y >= -4; y--) {
                            BlockState below = this.b.get(x, y, z);
                            if (below.isAir() || !below.getFluidState().isEmpty()) {
                                this.b.set(x, y, z, s(Blocks.BLACKSTONE));
                            }
                        }
                    }
                }
            }
        }

        /** Lava, die an die ausgehöhlte Kuppel grenzt, erstarrt zu Schwarzstein – sonst liefe sie hinein. */
        void seal() {
            for (int x = -REACH; x <= REACH; x++) {
                for (int z = -REACH; z <= REACH; z++) {
                    for (int y = 0; y <= 16; y++) {
                        if (this.b.get(x, y, z).getFluidState().isEmpty()) {
                            continue;
                        }
                        for (Direction d : Direction.values()) {
                            if (d != Direction.UP && this.b.get(x + d.getStepX(), y + d.getStepY(), z + d.getStepZ()).isAir()) {
                                this.b.set(x, y, z, s(Blocks.BLACKSTONE));
                                break;
                            }
                        }
                    }
                }
            }
        }

        /** Das Podest: drei Stufen aus Schwarzsteinziegeln, an den Rändern gesprungen und ausgebrochen. */
        void dais() {
            double[] radius = {6.4, 4.6, 2.9};
            for (int step = 0; step < 3; step++) {
                for (int x = -7; x <= 7; x++) {
                    for (int z = -7; z <= 7; z++) {
                        double r = Math.sqrt(x * x + z * z * 1.15);
                        if (r > radius[step]) {
                            continue;
                        }
                        boolean edge = r > radius[step] - 1.0;
                        float n = this.b.noise(x, step + 10, z);
                        if (edge && step == 0 && n < 0.16f) {
                            continue;                       // ausgebrochen
                        }
                        BlockState state;
                        if (edge && step < 2) {
                            Direction out = Math.abs(x) * 1.0 > Math.abs(z) * 1.07 ? (x > 0 ? Direction.EAST : Direction.WEST)
                                    : (z > 0 ? Direction.SOUTH : Direction.NORTH);
                            state = VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, out.getOpposite(), Half.BOTTOM);
                        } else {
                            state = n < 0.22f ? s(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS) : n < 0.27f ? s(Blocks.GILDED_BLACKSTONE)
                                    : s(Blocks.POLISHED_BLACKSTONE_BRICKS);
                        }
                        this.b.set(x, step, z, state);
                        if (step > 0) {
                            this.b.set(x, step - 1, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                        }
                    }
                }
            }
            // die oberste Stufe trägt einen Ring aus gemeißelten Platten um das Tor
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) == 2 && z != 0) {
                        this.b.set(x, 2, z, s(Blocks.CHISELED_POLISHED_BLACKSTONE));
                    }
                }
            }
            // ein Riss im Podest, aus dem es glüht
            int cx = this.campSide * -4;
            for (int k = 0; k < 4; k++) {
                this.b.set(cx + (k % 2), 0, -3 + k, s(Blocks.MAGMA_BLOCK));
            }
        }

        /** Vier Obelisken aus Basalt mit Seelenfeuer auf der Spitze; einer ist umgestürzt. */
        void obelisks() {
            int[][] spots = {{5, -4}, {-5, -4}, {5, 4}, {-5, 4}};
            for (int i = 0; i < spots.length; i++) {
                int x = spots[i][0];
                int z = spots[i][1];
                this.b.set(x, 0, z, s(Blocks.CHISELED_POLISHED_BLACKSTONE));
                if (i == this.brokenObelisk) {
                    // nur der Stumpf steht noch, der Rest liegt auf dem Boden
                    this.b.set(x, 1, z, pillar(Direction.Axis.Y));
                    int dx = Integer.signum(x);
                    for (int k = 1; k <= 3; k++) {
                        this.b.set(x + dx * (k + 1), groundY(x + dx * (k + 1), z), z, pillar(Direction.Axis.X));
                    }
                    continue;
                }
                int h = 5 + Math.floorMod(x * 7 + z * 3, 2);
                for (int y = 1; y <= h; y++) {
                    this.b.set(x, y, z, pillar(Direction.Axis.Y));
                }
                this.b.set(x, h + 1, z, s(Blocks.CHISELED_POLISHED_BLACKSTONE));
                this.b.set(x, h + 2, z, s(Blocks.SOUL_CAMPFIRE));
                // Ketten am Obelisken, als hätte man daran etwas festgemacht
                this.b.set(x - Integer.signum(x), h, z, Blocks.IRON_CHAIN.defaultBlockState());
                this.b.set(x - Integer.signum(x), h - 1, z, Blocks.IRON_CHAIN.defaultBlockState());
                this.b.set(x - Integer.signum(x), h + 1, z, s(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB));
            }
        }

        static BlockState pillar(Direction.Axis axis) {
            return Blocks.POLISHED_BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
        }

        /** Die Höhe des ersten freien Blocks über dem Boden an einer Stelle (von y = 3 abwärts gesucht). */
        int groundY(int x, int z) {
            for (int y = 3; y >= -3; y--) {
                if (this.b.get(x, y, z).isAir() && !this.b.get(x, y - 1, z).isAir()) {
                    return y;
                }
            }
            return 0;
        }

        // =====================================================================================
        // Das Tor
        // =====================================================================================

        void gate() {
            int[][] frame = frame();
            // der Rahmen aus Obsidian, einige Steine gesprungen (weinender Obsidian), einige fehlen
            int fallen = 0;
            for (int i = 0; i < frame.length; i++) {
                int x = frame[i][0];
                int y = frame[i][1];
                if (this.missing[i]) {
                    this.b.set(x, y, 0, AIR);
                    // der herausgebrochene Stein liegt auf den Stufen
                    int fx = (fallen % 2 == 0 ? 1 : -1) * (2 + fallen);
                    int fz = fallen < 2 ? 2 : -2;
                    this.b.set(fx, groundY(fx, fz), fz, s(Blocks.OBSIDIAN));
                    fallen++;
                } else {
                    this.b.set(x, y, 0, this.cracked[i] ? s(Blocks.CRYING_OBSIDIAN) : s(Blocks.OBSIDIAN));
                }
            }
            for (int x = -1; x <= 1; x++) {
                for (int y = FRAME_Y + 1; y <= FRAME_Y + 4; y++) {
                    this.b.set(x, y, 0, AIR);
                }
            }
            // der Spitzbogen aus Schwarzsteinziegeln: Pfeiler mit Fuß und Kapitell, darüber der Bogen mit Schlussstein
            for (int sx : new int[] {-1, 1}) {
                int x = sx * 3;
                for (int y = FRAME_Y; y <= FRAME_Y + 5; y++) {
                    this.b.set(x, y, 0, y == FRAME_Y + 2 ? s(Blocks.CHISELED_POLISHED_BLACKSTONE) : s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                }
                for (int z : new int[] {-1, 1}) {
                    this.b.set(x, FRAME_Y, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    this.b.set(x, FRAME_Y + 1, z, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
                            z > 0 ? Direction.NORTH : Direction.SOUTH, Half.BOTTOM));
                    this.b.set(x, FRAME_Y + 5, z, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
                            z > 0 ? Direction.NORTH : Direction.SOUTH, Half.TOP));
                }
            }
            int lintel = FRAME_Y + 6;
            for (int x = -3; x <= 3; x++) {
                if (!this.broken(x, lintel)) {
                    this.b.set(x, lintel, 0, Math.abs(x) <= 1 ? s(Blocks.POLISHED_BLACKSTONE_BRICKS) : s(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS));
                }
            }
            for (int sx : new int[] {-1, 1}) {
                if (!this.broken(sx * 2, lintel + 1)) {
                    this.b.set(sx * 2, lintel + 1, 0, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
                            sx > 0 ? Direction.WEST : Direction.EAST, Half.BOTTOM));
                }
                if (!this.broken(sx, lintel + 1)) {
                    this.b.set(sx, lintel + 1, 0, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                }
                if (!this.broken(sx, lintel + 2)) {
                    this.b.set(sx, lintel + 2, 0, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
                            sx > 0 ? Direction.WEST : Direction.EAST, Half.BOTTOM));
                }
            }
            if (!this.broken(0, lintel + 2)) {
                this.b.set(0, lintel + 1, 0, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                this.b.set(0, lintel + 2, 0, s(Blocks.GILDED_BLACKSTONE));
                this.b.set(0, lintel + 3, 0, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            } else {
                this.b.set(0, lintel + 1, 0, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
            }
            // vorn und hinten am Sturz hängt je eine Seelenlaterne
            for (int z : new int[] {-1, 1}) {
                this.b.set(0, lintel, z, VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB,
                        net.minecraft.world.level.block.state.properties.SlabType.TOP));
                this.b.set(0, lintel - 1, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
            // was vom Bogen herabgestürzt ist, liegt auf der Seite
            if (this.archBroken) {
                int x = this.archSide * 4;
                this.b.set(x, groundY(x, 1), 1, s(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS));
                this.b.set(x + this.archSide, groundY(x + this.archSide, 2), 2, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                this.b.set(x, groundY(x, -2), -2, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.EAST, Half.BOTTOM));
            }
        }

        /** Ist an dieser Stelle der Bogen eingestürzt (auf einer Seite, oberhalb des Sturzes)? */
        boolean broken(int x, int y) {
            if (!this.archBroken) {
                return false;
            }
            int side = x * this.archSide;
            return side >= 1 && y >= FRAME_Y + 7 || side >= 3 && y >= FRAME_Y + 6;
        }

        // =====================================================================================
        // Das letzte Lager
        // =====================================================================================

        /** Seitlich vor dem Podest: kalte Feuerstelle, zwei Schlafrollen, Truhe, Fass und eine Laterne an einem Pfahl. */
        void camp() {
            int cx = this.campSide * 7;
            int cz = 5;
            int y = groundY(cx, cz);
            // die Feuerstelle, mit Steinen eingefasst
            this.b.set(cx, y, cz, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
            for (Direction d : Direction.Plane.HORIZONTAL) {
                int x = cx + d.getStepX();
                int z = cz + d.getStepZ();
                this.b.set(x, groundY(x, z) - 1, z, s(Blocks.BLACKSTONE));
            }
            // zwei Schlafrollen
            for (int k = 0; k < 2; k++) {
                int x = cx + this.campSide * (k == 0 ? 2 : -2);
                for (int dz = 0; dz <= 1; dz++) {
                    int z = cz + 1 + dz + k;
                    this.b.set(x, groundY(x, z), z, k == 0 ? s(Blocks.CARPET.red()) : s(Blocks.CARPET.brown()));
                }
            }
            // Truhe und Fass hinter der Feuerstelle
            int tx = cx + this.campSide;
            int tz = cz - 2;
            this.b.chest(tx, groundY(tx, tz), tz, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH),
                    ModLootTables.SITE_EMBER_GATE);
            int bx = cx - this.campSide;
            this.b.set(bx, groundY(bx, tz), tz, Blocks.BARREL.defaultBlockState());
            // ein Pfahl mit einer Laterne
            int px = cx + this.campSide * 2;
            int pz = cz - 2;
            int py = groundY(px, pz);
            this.b.set(px, py, pz, s(Blocks.POLISHED_BLACKSTONE_WALL));
            this.b.set(px, py + 1, pz, s(Blocks.POLISHED_BLACKSTONE_WALL));
            this.b.set(px, py + 2, pz, Blocks.LANTERN.defaultBlockState());
        }
    }
}
