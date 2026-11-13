package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.block.nether.FlameAltarBlock;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.FlameSanctuary;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Das Flammenheiligtum (seit 1.2): Mitten im Lavameer der Unterwelt ruht auf Stelzen aus Basalt eine runde Plattform aus
 * poliertem Schwarzstein. Ihr Boden ist ein Mosaik – eine Sonne aus vergoldetem Schwarzstein, von der acht Strahlen aus
 * roten Netherziegeln ausgehen, ein goldener Ring, ein Rand aus Ziegeln mit einer niedrigen Brüstung. Acht Säulen tragen
 * Feuerschalen, in denen ewiges Feuer brennt. Auf einem Podest steht der Flammenaltar, dahinter eine Wand mit einem Fenster
 * aus Pilzlicht in der Gestalt einer Flamme; vor dem Altar schwebt Ignara, die Flammenpriesterin, und betet. Neben dem Altar
 * stehen zwei Truhen mit den Gaben der Pilger, Kerzen brennen. Stege führen über die Lava bis zum Fels und enden in Stollen.
 *
 * <p>Die Hitze hat rings um das Heiligtum eine Kuppel aus dem Gestein geschmolzen (mit einem Lavasee am Grund), so steht es
 * auch dort frei, wo das Lavameer unter Fels liegt.
 *
 * <p>Eigene Koordinaten: Mitte der Plattform bei (0, 0, 0); y = 0 ist die Höhe über dem Boden (vier Blöcke über dem
 * Lavasee). Vorn (+z) beginnt der Hauptsteg; der Altar blickt dorthin.
 */
final class FlameSanctum {
    /** Teile: das Heiligtum und die Stege (Stege bauen in ihrem eigenen Rahmen nach +z). */
    static final int SANCTUM = 0;
    static final int BRIDGE = 1;
    /** Halbmesser der Plattform. */
    static final int R = 14;
    /** So hoch über dem Lavasee steht man (y = 0). */
    static final int LIFT = 4;
    /** Die Oberfläche des Lavasees (eigene Koordinaten). */
    static final int LAVA = -LIFT - 1;
    /** Die Kuppel: mittlerer Halbmesser und Scheitelhöhe. */
    static final double DOME = 24.0;
    static final int DOME_HEIGHT = 30;
    /** Bis hierher (Halbmesser) reicht das Heiligtum. */
    static final int REACH = 28;
    /** So tief reichen Stelzen und Pfeiler höchstens. */
    static final int DEPTH = 16;
    static final int MAX_BRIDGE = 40;
    static final int MAX_TUNNEL = 14;
    /** Der Flammenaltar: blickt nach vorn (+z), die Priesterin schwebt zwei Blöcke davor. */
    static final int[] ALTAR = {0, 1, -5};

    private FlameSanctum() {}

    private static BlockState s(net.minecraft.world.level.block.Block block) {
        return block.defaultBlockState();
    }

    // =====================================================================================
    // Planung
    // =====================================================================================

    private static final int[][] PROBES = {{0, 0}, {10, 0}, {-10, 0}, {0, 10}, {0, -10}, {7, 7}, {-7, 7}, {7, -7}, {-7, -7}};

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int sea = terrain.seaLevel();
        int y = sea + LIFT;
        // unter neun Stellen die freieste: am liebsten steht das Heiligtum im offenen Lavameer
        int bestX = chunk.getMiddleBlockX();
        int bestZ = chunk.getMiddleBlockZ();
        int best = Integer.MIN_VALUE;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int x = chunk.getMiddleBlockX() + i * 12;
                int z = chunk.getMiddleBlockZ() + j * 12;
                int score = openness(terrain, x, z, sea) * 4 - Math.abs(i) - Math.abs(j);
                if (score > best) {
                    best = score;
                    bestX = x;
                    bestZ = z;
                }
            }
        }
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 7), 4)];
        BlockPos origin = new BlockPos(bestX, y, bestZ);
        // Stege nach allen vier Seiten – aber nur dorthin, wo sie auf Fels treffen; trifft keiner, führt der vordere zu einer
        // Anlegestelle über der Lava
        List<int[]> bridges = new ArrayList<>();
        int mask = 0;
        for (int d = 0; d < 4; d++) {
            Rotation r = rotation.getRotated(Rotation.values()[d]);
            int[] reach = reach(terrain, origin, r);
            if (reach != null) {
                bridges.add(new int[] {d, reach[0], reach[1]});
                mask |= 1 << d;
            }
        }
        if (bridges.isEmpty()) {
            bridges.add(new int[] {0, 24, -1});
            mask = 1;
        }
        List<SitePiece> pieces = new ArrayList<>();
        pieces.add(sanctum(origin, rotation, seed, mask));
        for (int[] bridge : bridges) {
            Rotation r = rotation.getRotated(Rotation.values()[bridge[0]]);
            pieces.add(bridgePiece(origin, r, seed + 31L * (bridge[0] + 1), bridge[1], Math.max(0, bridge[2])));
        }
        return Optional.of(new Structure.GenerationStub(origin, builder -> pieces.forEach(builder::addPiece)));
    }

    private static int openness(SiteTerrain terrain, int x, int z, int sea) {
        int score = 0;
        for (int[] p : PROBES) {
            for (int y = sea + 2; y <= sea + 22; y += 4) {
                if (!terrain.rock(x + p[0], y, z + p[1])) {
                    score++;
                }
            }
            if (terrain.fluid(x + p[0], sea - 1, z + p[1])) {
                score += 2;
            }
        }
        return score;
    }

    /**
     * Wie weit ein Steg (nach eigenem +z) reicht, bis er auf Fels trifft: {Länge, Stollen} – der Stollen sagt, wie weit man
     * durch den Fels bis zur nächsten Höhle graben muss. Null, wenn er bis {@link #MAX_BRIDGE} keinen Fels findet.
     */
    private static int[] reach(SiteTerrain terrain, BlockPos origin, Rotation r) {
        int walk = origin.getY();
        for (int t = (int) DOME + 3; t <= MAX_BRIDGE; t++) {
            BlockPos p = origin.offset(new BlockPos(0, 0, t).rotate(r));
            int solid = 0;
            for (int dy = 0; dy <= 2; dy++) {
                if (terrain.rock(p.getX(), walk + dy, p.getZ())) {
                    solid++;
                }
            }
            if (solid >= 2) {
                int tunnel = MAX_TUNNEL;
                for (int e = t + 1; e <= t + MAX_TUNNEL; e++) {
                    BlockPos q = origin.offset(new BlockPos(0, 0, e).rotate(r));
                    if (!terrain.rock(q.getX(), walk, q.getZ()) && !terrain.rock(q.getX(), walk + 1, q.getZ())
                            && !terrain.fluid(q.getX(), walk, q.getZ())) {
                        tunnel = e - t;
                        break;
                    }
                }
                return new int[] {t, tunnel};
            }
        }
        return null;
    }

    /** Das Heiligtum (ohne Stege) mit der Mitte der Plattform bei {@code origin}; {@code bridges}: Öffnungen der Brüstung. */
    static SitePiece sanctum(BlockPos origin, Rotation rotation, long seed, int bridges) {
        return new SitePiece(SiteKind.FLAME_SANCTUARY, SANCTUM, origin, rotation, seed, new int[] {bridges},
                new BoundingBox(origin.getX() - REACH, origin.getY() - DEPTH - 4, origin.getZ() - REACH, origin.getX() + REACH,
                        origin.getY() + DOME_HEIGHT + 4, origin.getZ() + REACH));
    }

    /** Ein Steg nach eigenem +z (Länge bis zum Fels, Stollen – 0: Anlegestelle). */
    static SitePiece bridgePiece(BlockPos origin, Rotation rotation, long seed, int len, int tunnel) {
        return new SitePiece(SiteKind.FLAME_SANCTUARY, BRIDGE, origin, rotation, seed, new int[] {len, tunnel},
                SitePiece.box(origin, rotation, -4, -DEPTH - 4, R - 1, 4, 6, len + Math.max(tunnel, 6) + 2));
    }

    /** Der Halbmesser der Kuppel in Richtung {@code angle}: 21 bis 27 Blöcke, unregelmäßig. */
    static double domeRadius(long seed, double angle) {
        double p1 = (seed & 0xFFF) / 4096.0 * 2.0 * Math.PI;
        double p2 = ((seed >>> 12) & 0xFFF) / 4096.0 * 2.0 * Math.PI;
        return DOME + 2.0 * Math.sin(3.0 * angle + p1) + 1.0 * Math.sin(5.0 * angle + p2);
    }

    // =====================================================================================
    // Bau
    // =====================================================================================

    static void build(VillageBuilder b, SitePiece piece) {
        if (piece.part() == SANCTUM) {
            new Sanctum(b, piece.seed(), piece.data(0)).build();
        } else {
            bridge(b, piece.data(0), piece.data(1));
        }
    }

    /** Liegt ein Punkt auf einer der acht Säulen (Halbmesser 13, zwischen den Stegen)? */
    static int[][] pillars() {
        int[][] out = new int[8][];
        for (int k = 0; k < 8; k++) {
            double a = Math.toRadians(22.5 + 45.0 * k);
            out[k] = new int[] {(int) Math.round(Math.cos(a) * 12.6), (int) Math.round(Math.sin(a) * 12.6)};
        }
        return out;
    }

    private static final class Sanctum {
        private final VillageBuilder b;
        private final long seed;
        private final int bridges;

        Sanctum(VillageBuilder b, long seed, int bridges) {
            this.b = b;
            this.seed = seed;
            this.bridges = bridges;
        }

        void build() {
            this.dome();
            this.platform();
            this.underside();
            this.stilts();
            this.rim();
            for (int[] p : pillars()) {
                this.pillar(p[0], p[1]);
            }
            this.dais();
            this.backWall();
        }

        /** Die Kuppel: das Gestein ist fortgeschmolzen (der Rand unregelmäßig), am Grund liegt Lava. */
        private void dome() {
            for (int x = -REACH; x <= REACH; x++) {
                for (int z = -REACH; z <= REACH; z++) {
                    if (!this.b.here(x, 0, z)) {
                        continue;
                    }
                    double r = Math.sqrt(x * x + z * z);
                    double edge = domeRadius(this.seed, Math.atan2(z, x)) + (this.b.noise(x, 0, z) - 0.5) * 1.6;
                    if (r > edge) {
                        continue;
                    }
                    double k = Math.min(1.0, r / edge);
                    int top = (int) Math.round((DOME_HEIGHT - LIFT) * Math.sqrt(1.0 - k * k) + (this.b.noise(x, 1, z) - 0.5) * 3.0);
                    for (int y = LAVA + 1; y <= top; y++) {
                        if (!this.b.get(x, y, z).isAir()) {
                            this.b.set(x, y, z, s(Blocks.AIR));
                        }
                    }
                    // der Lavasee: zwei Schichten tief, darunter bleibt das Gestein
                    for (int y = LAVA - 1; y <= LAVA; y++) {
                        if (this.b.get(x, y, z).getFluidState().isEmpty()) {
                            this.b.set(x, y, z, s(Blocks.LAVA));
                        }
                    }
                }
            }
        }

        /** Das Mosaik des Bodens: Sonne, Strahlen, goldener Ring, Ziegelrand. */
        private BlockState mosaic(int x, int z) {
            double r = Math.sqrt(x * x + z * z);
            if (r <= 1.5) {
                return s(Blocks.GILDED_BLACKSTONE);
            }
            if (r <= 2.6) {
                return s(Blocks.CHISELED_POLISHED_BLACKSTONE);
            }
            if (r > R - 1.6) {
                return s(Blocks.POLISHED_BLACKSTONE_BRICKS);
            }
            if (r >= 10.4 && r <= 11.5) {
                return s(Blocks.GILDED_BLACKSTONE);
            }
            double angle = Math.atan2(z, x);
            double step = Math.PI / 4.0;
            double off = angle - Math.round(angle / step) * step;
            if (r >= 3.0 && r <= 10.0 && Math.abs(Math.sin(off)) * r < 0.75) {
                return s(Blocks.RED_NETHER_BRICKS);
            }
            return s(Blocks.POLISHED_BLACKSTONE);
        }

        private void platform() {
            for (int x = -R; x <= R; x++) {
                for (int z = -R; z <= R; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r > R + 0.5) {
                        continue;
                    }
                    this.b.set(x, -1, z, this.mosaic(x, z));
                    this.b.set(x, -2, z, s(Blocks.POLISHED_BLACKSTONE));
                }
            }
        }

        /** Unter der Plattform: ein Kegel aus Schwarzstein, der in einen Pfeiler aus Basalt übergeht (bis auf festen Grund). */
        private void underside() {
            for (int x = -12; x <= 12; x++) {
                for (int z = -12; z <= 12; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r <= 11.0) {
                        this.b.set(x, -3, z, s(r > 10.0 ? Blocks.POLISHED_BLACKSTONE_BRICKS : Blocks.BLACKSTONE));
                    }
                    if (r <= 8.5) {
                        this.b.set(x, -4, z, s(Blocks.BLACKSTONE));
                    }
                    if (r <= 6.0) {
                        this.b.set(x, LAVA, z, s(Blocks.BLACKSTONE));
                    }
                    if (r <= 3.2) {
                        for (int y = LAVA - 1; y >= -DEPTH; y--) {
                            BlockState below = this.b.get(x, y, z);
                            if (below.isSolidRender() && below.getFluidState().isEmpty() && y < LAVA - 2) {
                                break;
                            }
                            this.b.set(x, y, z, Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                        }
                    }
                }
            }
        }

        /** Acht Stelzen aus Basalt am Rand, tief in die Lava. */
        private void stilts() {
            for (int[] p : pillars()) {
                for (int y = -3; y >= -DEPTH; y--) {
                    BlockState below = this.b.get(p[0], y, p[1]);
                    if (below.isSolidRender() && below.getFluidState().isEmpty() && y < LAVA - 2) {
                        break;
                    }
                    this.b.set(p[0], y, p[1], Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                }
            }
        }

        /** Die Brüstung am Rand (Mauern), offen, wo ein Steg ansetzt. */
        private void rim() {
            for (int x = -R; x <= R; x++) {
                for (int z = -R; z <= R; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r > R + 0.5 || r <= R - 0.5) {
                        continue;
                    }
                    if (this.gate(x, z)) {
                        continue;
                    }
                    this.b.set(x, 0, z, s(Blocks.POLISHED_BLACKSTONE_WALL));
                }
            }
        }

        /** Öffnung für einen Steg (eigene Richtungen: 0 = +z, 1 = -x, 2 = -z, 3 = +x – wie Rotation im Uhrzeigersinn). */
        private boolean gate(int x, int z) {
            for (int d = 0; d < 4; d++) {
                if ((this.bridges & (1 << d)) == 0) {
                    continue;
                }
                BlockPos dir = new BlockPos(0, 0, 1).rotate(Rotation.values()[d]);
                int along = x * dir.getX() + z * dir.getZ();
                int across = Math.abs(x * dir.getZ() - z * dir.getX());
                if (along > 0 && across <= 1) {
                    return true;
                }
            }
            return false;
        }

        /** Eine Säule aus poliertem Basalt mit einer Feuerschale, in der ewiges Feuer brennt. */
        private void pillar(int px, int pz) {
            for (int y = 0; y <= 4; y++) {
                this.b.set(px, y, pz, Blocks.POLISHED_BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
            }
            this.b.set(px, 5, pz, s(Blocks.CHISELED_POLISHED_BLACKSTONE));
            this.b.set(px, 6, pz, s(Blocks.GILDED_BLACKSTONE));
            this.b.set(px, 7, pz, s(Blocks.NETHERRACK));
            this.b.set(px, 8, pz, s(Blocks.FIRE));
            for (Direction d : Direction.Plane.HORIZONTAL) {
                this.b.set(px + d.getStepX(), 7, pz + d.getStepZ(), VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, d.getOpposite(), Half.TOP));
            }
        }

        /** Das Podest mit dem Flammenaltar, einem roten Läufer, den Truhen der Pilger und Kerzen. */
        private void dais() {
            for (int x = -3; x <= 3; x++) {
                for (int z = -7; z <= -1; z++) {
                    BlockState state;
                    if (z == -1) {
                        state = VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM);
                    } else if (x == -3) {
                        state = VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.EAST, Half.BOTTOM);
                    } else if (x == 3) {
                        state = VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.WEST, Half.BOTTOM);
                    } else if (Math.abs(x) <= 1) {
                        state = s(Blocks.RED_NETHER_BRICKS);
                    } else {
                        state = s(z == -7 ? Blocks.CHISELED_POLISHED_BLACKSTONE : Blocks.POLISHED_BLACKSTONE_BRICKS);
                    }
                    this.b.set(x, 0, z, state);
                }
            }
            this.b.set(ALTAR[0], ALTAR[1], ALTAR[2], FlameSanctuary.FLAME_ALTAR.get().defaultBlockState()
                    .setValue(FlameAltarBlock.FACING, Direction.SOUTH).setValue(FlameAltarBlock.PHASE, FlameAltarBlock.Phase.PRAYING));
            for (int x : new int[] {-2, 2}) {
                this.b.chest(x, 1, -6, s(Blocks.CHEST).setValue(ChestBlock.FACING, Direction.SOUTH), ModLootTables.SITE_FLAME_SANCTUARY);
            }
            for (int[] c : new int[][] {{-2, -2}, {2, -2}, {-1, -7}, {1, -7}}) {
                this.b.set(c[0], 1, c[1], Blocks.DYED_CANDLE.pick(net.minecraft.world.item.DyeColor.RED).defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));
            }
        }

        /** Hinter dem Altar eine Wand zwischen zwei Säulen, darin ein Fenster aus Pilzlicht in der Gestalt einer Flamme. */
        private void backWall() {
            int z = -8;
            for (int x = -4; x <= 4; x++) {
                for (int y = 0; y <= 7; y++) {
                    BlockState state;
                    if (Math.abs(x) == 4) {
                        state = y == 7 ? s(Blocks.CHISELED_POLISHED_BLACKSTONE)
                                : Blocks.POLISHED_BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
                    } else if (y == 7) {
                        state = x == 0 ? s(Blocks.GILDED_BLACKSTONE) : VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
                                Direction.NORTH, Half.TOP);
                    } else if (flameWindow(x, y)) {
                        state = s(Blocks.SHROOMLIGHT);
                    } else if (y == 0) {
                        state = s(Blocks.POLISHED_BLACKSTONE_BRICKS);
                    } else {
                        state = s((x + y) % 5 == 0 ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS : Blocks.POLISHED_BLACKSTONE_BRICKS);
                    }
                    this.b.set(x, y, z, state);
                }
            }
            // Laternen an den Säulen der Wand
            this.b.set(-4, 1, z + 1, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            this.b.set(4, 1, z + 1, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
        }

        private static boolean flameWindow(int x, int y) {
            return switch (y) {
                case 1 -> x == 0;
                case 2, 3 -> Math.abs(x) <= 1;
                case 4 -> x == 0 || x == 1;
                case 5 -> x == 0;
                default -> false;
            };
        }
    }

    // =====================================================================================
    // Die Stege
    // =====================================================================================

    /**
     * Ein Steg nach eigenem +z: drei Blöcke breit, mit Brüstungen und Laternen, alle acht Blöcke ein Pfeiler bis in die Lava;
     * trifft er auf Fels, führt ein Stollen mit einem Torbogen hindurch, sonst endet er an einer Anlegestelle mit Feuerschale.
     */
    private static void bridge(VillageBuilder b, int len, int tunnel) {
        int start = R - 1;
        for (int z = start; z <= len; z++) {
            for (int x = -2; x <= 2; x++) {
                b.set(x, -1, z, s(Math.abs(x) == 2 ? Blocks.POLISHED_BLACKSTONE_BRICKS
                        : (x == 0 && z % 4 == 0 ? Blocks.CHISELED_POLISHED_BLACKSTONE : Blocks.POLISHED_BLACKSTONE_BRICKS)));
                b.set(x, -2, z, s(Blocks.POLISHED_BLACKSTONE));
                for (int y = 0; y <= 3; y++) {
                    if (Math.abs(x) <= 1 || y > 0) {
                        if (!b.get(x, y, z).isAir()) {
                            b.set(x, y, z, s(Blocks.AIR));
                        }
                    }
                }
            }
            if (z > R) {
                b.set(-2, 0, z, s(Blocks.POLISHED_BLACKSTONE_WALL));
                b.set(2, 0, z, s(Blocks.POLISHED_BLACKSTONE_WALL));
                if (z % 6 == 3) {
                    b.set(-2, 1, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
                    b.set(2, 1, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
                }
            }
            if (z > R && z % 8 == 4) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = -3; y >= -DEPTH; y--) {
                        BlockState below = b.get(x, y, z);
                        if (below.isSolidRender() && below.getFluidState().isEmpty() && y < LAVA - 2) {
                            break;
                        }
                        b.set(x, y, z, Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                    }
                }
            }
        }
        if (tunnel > 0) {
            // der Torbogen am Fels und der Stollen dahinter
            for (int y = 0; y <= 3; y++) {
                b.set(-2, y, len, s(y == 3 ? Blocks.CHISELED_POLISHED_BLACKSTONE : Blocks.POLISHED_BLACKSTONE_BRICKS));
                b.set(2, y, len, s(y == 3 ? Blocks.CHISELED_POLISHED_BLACKSTONE : Blocks.POLISHED_BLACKSTONE_BRICKS));
            }
            for (int x = -1; x <= 1; x++) {
                b.set(x, 3, len, x == 0 ? s(Blocks.GILDED_BLACKSTONE)
                        : VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, x < 0 ? Direction.EAST : Direction.WEST, Half.TOP));
            }
            for (int z = len + 1; z <= len + tunnel; z++) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = 0; y <= 2; y++) {
                        b.set(x, y, z, s(Blocks.AIR));
                    }
                    if (!b.get(x, -1, z).isSolidRender()) {
                        b.set(x, -1, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    }
                }
                if ((z - len) % 5 == 2) {
                    b.set(1, 2, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
                }
            }
        } else {
            // die Anlegestelle über der Lava
            for (int z = len + 1; z <= len + 5; z++) {
                for (int x = -2; x <= 2; x++) {
                    b.set(x, -1, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    b.set(x, -2, z, s(Blocks.POLISHED_BLACKSTONE));
                    if (Math.abs(x) == 2 || z == len + 5) {
                        b.set(x, 0, z, s(Blocks.POLISHED_BLACKSTONE_WALL));
                    }
                }
            }
            b.set(0, 0, len + 4, s(Blocks.NETHERRACK));
            b.set(0, 1, len + 4, s(Blocks.FIRE));
        }
    }
}
