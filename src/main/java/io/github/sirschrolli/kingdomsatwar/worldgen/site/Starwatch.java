package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.StarBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Sternwarte: Auf den äußeren Inseln des Ends hat ein vergessener Orden die Sterne beobachtet, die der Sternenfresser
 * verschlingt. Auf einer schwebenden Plattform mit dem Sternenhof – einem achtzackigen Stern im Pflaster – steht ihr
 * achteckiger Turm aus Endsteinziegeln mit Purpurpfeilern: unten die Halle der Schreine, darüber die Bibliothek der
 * Sternkarten, die Instrumentenhalle mit einer Armillarsphäre und die Reliquienkammer, in der (meist) die Schwingen eines
 * gefallenen Sternenreiters hängen – die Elytren. Oben dreht sich unter einer Kuppel aus grünspanigem Kupfer ein großes
 * Fernrohr. Zwei Seitentürme mit Kristallspitzen sind über Brücken mit dem Turm verbunden; Schreinlinge halten Wache
 * (an Stelle der Endstadt und des Endschiffs).
 *
 * <p>Eigene Koordinaten: (0, 0, 0) ist die Mitte des Sternenhofs, y = 0 ist die Lage des Pflasters (man steht auf
 * y = 1); der Turm steht hinten (−z), sein Tor blickt nach vorn (+z).
 */
final class Starwatch {
    /** Mitte des Hauptturms (z) und Halbmesser seines Achtecks (bis zur Mitte einer Seite). */
    static final int TOWER_Z = -11;
    static final int TOWER_R = 6;
    /** Böden der Geschosse; auf DECK liegt die Plattform mit der Kuppel. */
    static final int[] FLOORS = {0, 8, 16, 24};
    static final int DECK = 32;
    /** Die Seitentürme: Mitte bei x = ±SIDE_X, z = TOWER_Z; Böden wie der Hauptturm, oben bei SIDE_TOP. */
    static final int SIDE_X = 16;
    static final int SIDE_R = 3;
    static final int SIDE_TOP = 24;
    /** Der Sternenhof: Mitte bei z = PLAZA_Z. */
    static final int PLAZA_Z = 4;
    static final int PLAZA_R = 12;
    /** Der Sternstein in der Mitte des Sternenhofs (auf dem Pfeiler). */
    static final int[] STARSTONE = {0, 2, PLAZA_Z};
    private static final int REACH_X = 23;
    private static final int REACH_BACK = -22;
    private static final int REACH_FRONT = 20;
    private static final int DEPTH = 26;
    private static final int TOP = 44;
    private static final int[][] PROBES = {{0, 0}, {12, 0}, {-12, 0}, {0, 14}, {0, -14}, {16, -11}, {-16, -11}, {9, 10}, {-9, 10}};

    private Starwatch() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 13), 4)];
        // der Boden unter der Plattform: alle Proben auf der Insel, die Mitte hoch genug
        int[] heights = new int[PROBES.length];
        for (int i = 0; i < PROBES.length; i++) {
            BlockPos p = new BlockPos(PROBES[i][0], 0, PROBES[i][1]).rotate(rotation);
            heights[i] = terrain.ground(x + p.getX(), z + p.getZ());
        }
        int center = heights[0];
        if (center < 52) {
            return Optional.empty();
        }
        int sum = 0;
        for (int h : heights) {
            if (h < center - 14) {
                return Optional.empty();
            }
            sum += h;
        }
        int y = Math.max(center, sum / heights.length) + 1;
        BlockPos origin = new BlockPos(x, y, z);
        SitePiece piece = piece(origin, rotation, seed);
        if (!terrain.valid(x, y, z)) {
            return Optional.empty();
        }
        return Optional.of(new Structure.GenerationStub(origin, builder -> builder.addPiece(piece)));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        return new SitePiece(SiteKind.STARWATCH, 0, origin, rotation, seed, new int[0],
                SitePiece.box(origin, rotation, -REACH_X, -DEPTH, REACH_BACK, REACH_X, TOP, REACH_FRONT));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Warte(b, piece.seed()).build();
    }

    /** Hängen in der Reliquienkammer der Warte mit diesem Samen die Elytren? */
    static boolean hasWings(long seed) {
        return Warte.wings(seed);
    }

    // =====================================================================================
    // Formen
    // =====================================================================================

    /** Im Achteck mit dem Halbmesser r (bis zur Mitte einer Seite)? */
    static boolean oct(double dx, double dz, double r) {
        return Math.abs(dx) <= r && Math.abs(dz) <= r && Math.abs(dx) + Math.abs(dz) <= r * 1.4142 + 0.01;
    }

    /** Auf dem Rand des Achtecks (eine Blockreihe)? */
    static boolean octWall(double dx, double dz, double r) {
        return oct(dx, dz, r) && !oct(dx, dz, r - 1);
    }

    /** An einer Ecke des Achtecks (dort stehen die Pfeiler)? */
    static boolean octCorner(int dx, int dz, int r) {
        if (!octWall(dx, dz, r)) {
            return false;
        }
        double a = Math.toDegrees(Math.atan2(dz, dx));
        double off = Math.abs(Math.floorMod((long) Math.round((a - 22.5) * 100), 4500L) / 100.0);
        return off < 7.0 || off > 38.0;
    }

    /** Die Mitte einer Seite des Achtecks (dort sind die Fenster). */
    static boolean octFaceCenter(int dx, int dz, int r) {
        int k = (int) Math.round(r / 1.4142);
        return (Math.abs(dx) == r && dz == 0) || (Math.abs(dz) == r && dx == 0) || (Math.abs(dx) == k && Math.abs(dz) == k);
    }

    private static final class Warte {
        private static final BlockState AIR = Blocks.AIR.defaultBlockState();
        private final VillageBuilder b;
        private final RandomSource random;
        private final boolean wings;

        Warte(VillageBuilder b, long seed) {
            this.b = b;
            this.random = RandomSource.create(seed ^ 0x510E527FADE682D1L);
            this.wings = this.random.nextFloat() < 0.6f;
        }

        /** Hängen in der Reliquienkammer der Warte mit diesem Samen die Schwingen (die Elytren)? */
        static boolean wings(long seed) {
            return RandomSource.create(seed ^ 0x510E527FADE682D1L).nextFloat() < 0.6f;
        }

        static BlockState s(Block block) {
            return block.defaultBlockState();
        }

        static BlockState pillar() {
            return Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        }

        static BlockState copper() {
            return s(Blocks.CUT_COPPER.waxed().oxidized());
        }

        static BlockState slab(Block block, SlabType type) {
            return VillageBuilder.slab(block, type);
        }

        void build() {
            this.platform();
            this.plaza();
            this.tower();
            this.dome();
            for (int sx : new int[] {-1, 1}) {
                this.sideTower(sx);
                this.bridge(sx);
            }
            this.shrines();
            this.guards();
        }

        // =====================================================================================
        // Plattform und Sternenhof
        // =====================================================================================

        /** Liegt die Stelle auf der Plattform (Sternenhof, Turmsockel, Sockel der Seitentürme)? */
        boolean onPlatform(int x, int z) {
            double edge = (this.b.noise(x, 3, z) - 0.5) * 0.8;
            return oct(x, z - PLAZA_Z, PLAZA_R + edge) || oct(x, z - TOWER_Z, TOWER_R + 3 + edge)
                    || oct(x - SIDE_X, z - TOWER_Z, SIDE_R + 2 + edge) || oct(x + SIDE_X, z - TOWER_Z, SIDE_R + 2 + edge)
                    || Math.abs(z - TOWER_Z) <= 1 && Math.abs(x) <= SIDE_X;
        }

        /** Wie weit die Plattform von ihrem Rand nach innen reicht (für die Tiefe des Unterbaus). */
        double inset(int x, int z) {
            double best = 0;
            for (double r = 1; r <= 14; r += 1) {
                if (oct(x, z - PLAZA_Z, PLAZA_R - r) || oct(x, z - TOWER_Z, TOWER_R + 3 - r)) {
                    best = r;
                } else {
                    break;
                }
            }
            return best;
        }

        /**
         * Die schwebende Plattform: oben Pflaster, darunter ein Unterbau aus Endstein, der sich nach unten verjüngt – liegt
         * die Insel tiefer, hängt er frei wie ein Zapfen. Was über dem Pflaster vom Gelände aufragt, wird abgetragen.
         */
        void platform() {
            for (int x = -REACH_X; x <= REACH_X; x++) {
                for (int z = REACH_BACK; z <= REACH_FRONT; z++) {
                    if (!this.onPlatform(x, z)) {
                        continue;
                    }
                    for (int y = 1; y <= TOP; y++) {
                        BlockState st = this.b.get(x, y, z);
                        if (!st.isAir()) {
                            this.b.set(x, y, z, AIR);
                        }
                    }
                    boolean rim = !this.onPlatform(x + 1, z) || !this.onPlatform(x - 1, z) || !this.onPlatform(x, z + 1) || !this.onPlatform(x, z - 1);
                    this.b.set(x, 0, z, rim ? s(Blocks.QUARTZ_BRICKS) : s(Blocks.END_STONE_BRICKS));
                    int depth = (int) Math.round(3 + this.inset(x, z) * 1.6 + this.b.noise(x, 5, z) * 3);
                    for (int y = -1; y >= -depth && y >= -DEPTH; y--) {
                        BlockState st = this.b.get(x, y, z);
                        if (st.is(Blocks.END_STONE) && y < -2) {
                            break;                          // die Insel trägt hier selbst
                        }
                        this.b.set(x, y, z, y >= -2 && rim ? s(Blocks.END_STONE_BRICKS) : s(Blocks.END_STONE));
                    }
                }
            }
        }

        /** Der Sternenhof: ein achtzackiger Stern im Pflaster, in der Mitte der Sternstein, ringsum Brüstung und Laternen. */
        void plaza() {
            for (int x = -8; x <= 8; x++) {
                for (int z = PLAZA_Z - 8; z <= PLAZA_Z + 8; z++) {
                    int dx = x;
                    int dz = z - PLAZA_Z;
                    boolean square = Math.max(Math.abs(dx), Math.abs(dz)) <= 5;
                    boolean diamond = Math.abs(dx) + Math.abs(dz) <= 7;
                    if (!square && !diamond) {
                        continue;
                    }
                    // der Umriss schwarz, die Zacken weiß, die Mitte Kristall in einem Ring aus weinendem Obsidian
                    boolean outline = !(Math.max(Math.abs(dx + 1), Math.abs(dz)) <= 5 || Math.abs(dx + 1) + Math.abs(dz) <= 7)
                            || !(Math.max(Math.abs(dx - 1), Math.abs(dz)) <= 5 || Math.abs(dx - 1) + Math.abs(dz) <= 7)
                            || !(Math.max(Math.abs(dx), Math.abs(dz + 1)) <= 5 || Math.abs(dx) + Math.abs(dz + 1) <= 7)
                            || !(Math.max(Math.abs(dx), Math.abs(dz - 1)) <= 5 || Math.abs(dx) + Math.abs(dz - 1) <= 7);
                    int ring = Math.max(Math.abs(dx), Math.abs(dz));
                    this.b.set(x, 0, z, ring <= 1 ? s(Blocks.AMETHYST_BLOCK) : ring == 2 ? s(Blocks.CRYING_OBSIDIAN)
                            : outline ? s(Blocks.OBSIDIAN) : s(Blocks.SMOOTH_QUARTZ));
                }
            }
            // der Sternstein auf seinem Pfeiler (ruft Asteria, die Sternenreiterin), vier Leuchtstäbe ringsum
            this.b.set(0, 1, PLAZA_Z, pillar());
            this.b.set(STARSTONE[0], STARSTONE[1], STARSTONE[2], StarBlocks.STARSTONE.get().defaultBlockState());
            for (Direction d : Direction.Plane.HORIZONTAL) {
                this.b.set(d.getStepX() * 2, 1, PLAZA_Z + d.getStepZ() * 2, s(Blocks.END_ROD));
            }
            // die Brüstung am Rand des Hofs, vorn und an den Seiten Treppen hinab; Laternen auf Pfosten
            for (int x = -PLAZA_R - 1; x <= PLAZA_R + 1; x++) {
                for (int z = PLAZA_Z - 2; z <= PLAZA_Z + PLAZA_R + 1; z++) {
                    int dz = z - PLAZA_Z;
                    if (!this.onPlatform(x, z) || this.onPlatform(x, z + 1) && this.onPlatform(x + 1, z) && this.onPlatform(x - 1, z)
                            && this.onPlatform(x, z - 1)) {
                        continue;
                    }
                    if (Math.abs(x) <= 1 && dz > 0 || Math.abs(dz) <= 1 && Math.abs(x) > 8) {
                        continue;                           // Durchgänge
                    }
                    boolean post = Math.floorMod(x * 3 + z * 5, 7) == 0;
                    this.b.set(x, 1, z, post ? pillar() : s(Blocks.END_STONE_BRICK_WALL));
                    if (post) {
                        this.b.set(x, 2, z, pillar());
                        this.b.set(x, 3, z, s(Blocks.END_ROD));
                    }
                }
            }
            // Treppe vorn hinab (ins Freie)
            for (int k = 1; k <= 3; k++) {
                for (int x = -1; x <= 1; x++) {
                    int z = PLAZA_Z + PLAZA_R + k;
                    this.b.set(x, 1 - k, z, VillageBuilder.stairs(Blocks.SMOOTH_QUARTZ_STAIRS, Direction.NORTH, Half.BOTTOM));
                    this.b.set(x, -k, z, s(Blocks.END_STONE_BRICKS));
                }
            }
        }

        // =====================================================================================
        // Der Hauptturm
        // =====================================================================================

        void tower() {
            int r = TOWER_R;
            for (int dx = -r - 1; dx <= r + 1; dx++) {
                for (int dz = -r - 1; dz <= r + 1; dz++) {
                    int z = TOWER_Z + dz;
                    if (octWall(dx, dz, r)) {
                        boolean corner = octCorner(dx, dz, r);
                        for (int y = 1; y <= DECK; y++) {
                            boolean band = y % 8 == 0;
                            this.b.set(dx, y, z, corner ? pillar() : band ? s(Blocks.SMOOTH_QUARTZ) : s(Blocks.END_STONE_BRICKS));
                        }
                        // Fenster: in der Mitte jeder Seite, je Geschoss ein schmales hohes
                        if (octFaceCenter(dx, dz, r)) {
                            for (int f = 0; f < FLOORS.length; f++) {
                                int fl = FLOORS[f];
                                if (f == 0 && dz == r && dx == 0) {
                                    continue;                   // vorn ist das Tor
                                }
                                for (int y = fl + 3; y <= fl + 5; y++) {
                                    this.b.set(dx, y, z, s(Blocks.STAINED_GLASS_PANE.purple()));
                                }
                                this.b.set(dx, fl + 6, z, s(Blocks.SMOOTH_QUARTZ));
                            }
                        }
                    } else if (oct(dx, dz, r + 1) && !oct(dx, dz, r)) {
                        // Gesimse um den Turm an jedem Geschoss
                        for (int fl : new int[] {8, 16, 24, DECK}) {
                            this.b.set(dx, fl, z, slab(Blocks.SMOOTH_QUARTZ_SLAB, SlabType.TOP));
                        }
                        // ein Sockel rings um den Fuß
                        this.b.set(dx, 1, z, s(Blocks.END_STONE_BRICKS));
                        this.b.set(dx, 2, z, slab(Blocks.END_STONE_BRICK_SLAB, SlabType.BOTTOM));
                    } else if (oct(dx, dz, r - 1)) {
                        // Böden (die Mitte jedes Bodens ist eine Lampe für das Geschoss darunter)
                        for (int fl : new int[] {8, 16, 24, DECK}) {
                            boolean lamp = dx == 0 && dz == 0;
                            this.b.set(dx, fl, z, lamp ? s(Blocks.PEARLESCENT_FROGLIGHT)
                                    : Math.floorMod(dx + dz, 2) == 0 ? s(Blocks.SMOOTH_QUARTZ) : s(Blocks.END_STONE_BRICKS));
                        }
                        this.b.set(dx, 0, z, Math.max(Math.abs(dx), Math.abs(dz)) <= 1 ? s(Blocks.AMETHYST_BLOCK)
                                : Math.floorMod(dx + dz, 2) == 0 ? s(Blocks.SMOOTH_QUARTZ) : s(Blocks.END_STONE_BRICKS));
                        for (int y = 1; y < DECK; y++) {
                            if (y % 8 != 0) {
                                this.b.set(dx, y, z, AIR);
                            }
                        }
                    }
                }
            }
            // die Gesimse an den Ecken: kleine Treppen statt der Stufen am Fuß (der Sockel bleibt glatt)
            // das Tor nach vorn: ein Spitzbogen über drei Blöcken
            int front = TOWER_Z + r;
            for (int x = -1; x <= 1; x++) {
                for (int y = 1; y <= 5; y++) {
                    this.b.set(x, y, front, AIR);
                    this.b.set(x, y, front + 1, AIR);
                }
                this.b.set(x, 0, front, s(Blocks.SMOOTH_QUARTZ));
                this.b.set(x, 0, front + 1, s(Blocks.SMOOTH_QUARTZ));
            }
            for (int sx : new int[] {-1, 1}) {
                for (int y = 1; y <= 5; y++) {
                    this.b.set(sx * 2, y, front + 1, pillar());
                }
                this.b.set(sx * 2, 6, front + 1, VillageBuilder.stairs(Blocks.SMOOTH_QUARTZ_STAIRS, sx > 0 ? Direction.WEST : Direction.EAST, Half.BOTTOM));
                this.b.set(sx, 6, front + 1, s(Blocks.SMOOTH_QUARTZ));
                this.b.set(sx, 6, front, s(Blocks.SMOOTH_QUARTZ));
            }
            this.b.set(0, 6, front + 1, s(Blocks.SMOOTH_QUARTZ));
            this.b.set(0, 7, front + 1, VillageBuilder.stairs(Blocks.SMOOTH_QUARTZ_STAIRS, Direction.SOUTH, Half.BOTTOM));
            this.b.set(0, 6, front, s(Blocks.SMOOTH_QUARTZ));
            this.spiral();
            this.hall();
            this.library();
            this.instruments();
            this.relics();
        }

        /** Die Wendeltreppe: vorn links im Turm um eine Säule, je Geschoss eine ganze Drehung (acht Stufen). */
        static final int SPIRAL_X = -2;
        static final int SPIRAL_Z = 3;
        private static final int[][] RING = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
        private static final Direction[] STEP = {Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.NORTH, Direction.NORTH,
                Direction.EAST, Direction.EAST, Direction.SOUTH};

        /**
         * Vom Boden der Halle bis hinauf in die Kuppel: Stufe um Stufe um die Säule, jede eine höher, unter jeder eine
         * Platte; in jedem Boden zwei Löcher über den letzten Stufen, damit man oben nicht mit dem Kopf anstößt.
         */
        void spiral() {
            for (int y = 1; y <= DECK; y++) {
                this.b.set(SPIRAL_X, y, TOWER_Z + SPIRAL_Z, pillar());
            }
            for (int i = 0; i < DECK; i++) {
                int p = i % 8;
                int y = 1 + i;
                int x = SPIRAL_X + RING[p][0];
                int z = TOWER_Z + SPIRAL_Z + RING[p][1];
                this.b.set(x, y, z, VillageBuilder.stairs(Blocks.SMOOTH_QUARTZ_STAIRS, STEP[p], Half.BOTTOM));
                if (y - 1 > 0 && (y - 1) % 8 != 0) {
                    this.b.set(x, y - 1, z, slab(Blocks.SMOOTH_QUARTZ_SLAB, SlabType.TOP));
                }
            }
            for (int fl : new int[] {8, 16, 24, DECK}) {
                for (int p = 5; p <= 6; p++) {
                    this.b.set(SPIRAL_X + RING[p][0], fl, TOWER_Z + SPIRAL_Z + RING[p][1], AIR);
                }
            }
        }

        /** Liegt die Stelle (Turmkoordinaten) an der Wendeltreppe? */
        static boolean nearSpiral(int dx, int dz) {
            return Math.abs(dx - SPIRAL_X) <= 2 && Math.abs(dz - SPIRAL_Z) <= 2;
        }

        /** Unten: die Halle der Schreine – Nischen mit Leuchtstäben, in der Mitte ein Kristall auf einem Sockel. */
        void hall() {
            this.b.set(0, 1, TOWER_Z, pillar());
            this.b.set(0, 2, TOWER_Z, s(Blocks.AMETHYST_BLOCK));
            this.b.set(0, 3, TOWER_Z, s(Blocks.AMETHYST_CLUSTER));
            for (int[] n : new int[][] {{-5, -2}, {-5, 2}, {5, -2}, {5, 2}}) {
                this.b.set(n[0], 4, TOWER_Z + n[1], s(Blocks.END_ROD));
            }
            // Bänke vor den Wänden
            for (int dz = -2; dz <= 0; dz++) {
                this.b.set(-3, 1, TOWER_Z + dz, VillageBuilder.stairs(Blocks.END_STONE_BRICK_STAIRS, Direction.WEST, Half.BOTTOM));
                this.b.set(3, 1, TOWER_Z + dz, VillageBuilder.stairs(Blocks.END_STONE_BRICK_STAIRS, Direction.EAST, Half.BOTTOM));
            }
        }

        /** Darüber: die Bibliothek der Sternkarten – Regale an den Wänden, zwei Lesepulte, eine Truhe. */
        void library() {
            int fl = FLOORS[1];
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    if (!octWall(dx, dz, TOWER_R - 1) || nearSpiral(dx, dz) || dz == TOWER_R - 1 && Math.abs(dx) <= 1) {
                        continue;                       // nicht an der Wendeltreppe
                    }
                    if (this.b.get(dx, fl + 1, TOWER_Z + dz).isAir() && this.b.get(dx, fl + 2, TOWER_Z + dz).isAir()
                            && !this.b.get(dx, fl, TOWER_Z + dz).isAir()) {
                        this.b.set(dx, fl + 1, TOWER_Z + dz, s(Blocks.BOOKSHELF));
                        this.b.set(dx, fl + 2, TOWER_Z + dz, s(Blocks.BOOKSHELF));
                    }
                }
            }
            this.b.set(-1, fl + 1, TOWER_Z + 1, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.SOUTH));
            this.b.set(1, fl + 1, TOWER_Z + 1, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.SOUTH));
            this.b.set(0, fl + 1, TOWER_Z - 2, s(Blocks.CARTOGRAPHY_TABLE));
            this.b.chest(-2, fl + 1, TOWER_Z - 3, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH),
                    ModLootTables.SITE_STARWATCH);
        }

        /**
         * Die Instrumentenhalle: eine Armillarsphäre aus Kupfer – zwei Ringe um eine leuchtende Mitte –, dazu Tische mit
         * Brauständen.
         */
        void instruments() {
            int fl = FLOORS[2];
            int cy = fl + 4;
            // die Sphäre steht rechts hinten (links vorn läuft die Wendeltreppe)
            int sx0 = 1;
            int sz0 = TOWER_Z - 1;
            // drei Ringe aus dünnen Kupferstäben – waagrecht und zweimal senkrecht –, jeder Stab längs des Rings gelegt
            for (int a = 0; a < 48; a++) {
                double t = a * Math.PI * 2 / 48;
                double c = Math.cos(t) * 2.6;
                double sn = Math.sin(t) * 2.6;
                boolean flatX = Math.abs(Math.sin(t)) > Math.abs(Math.cos(t));
                this.rod(sx0 + (int) Math.round(c), cy, sz0 + (int) Math.round(sn), flatX ? Direction.EAST : Direction.SOUTH);
                this.rod(sx0 + (int) Math.round(c), cy + (int) Math.round(sn), sz0, flatX ? Direction.EAST : Direction.UP);
                this.rod(sx0, cy + (int) Math.round(sn), sz0 + (int) Math.round(c), flatX ? Direction.SOUTH : Direction.UP);
            }
            this.b.set(sx0, cy, sz0, s(Blocks.COPPER_BULB.waxed().unaffected()).setValue(
                    net.minecraft.world.level.block.CopperBulbBlock.LIT, true));
            this.b.set(sx0, fl + 1, sz0, s(Blocks.POLISHED_BLACKSTONE_WALL));
            for (int sx : new int[] {-1, 1}) {
                int x = sx * 4;
                if (this.b.get(x, fl + 1, TOWER_Z - 3).isAir()) {
                    this.b.set(x, fl + 1, TOWER_Z - 3, s(Blocks.END_STONE_BRICKS));
                    this.b.set(x, fl + 2, TOWER_Z - 3, s(Blocks.BREWING_STAND));
                }
            }
        }

        /**
         * Oben: die Reliquienkammer – hinten ein Schrein mit den Schwingen eines gefallenen Sternenreiters (den Elytren) in
         * einem Rahmen, davor die Truhe mit dem Schatz der Warte.
         */
        void relics() {
            int fl = FLOORS[3];
            int back = TOWER_Z - TOWER_R + 1;
            // der Schrein: ein Podest, zwei Pfeiler mit Leuchtstäben, dazwischen die Rückwand
            for (int x = -2; x <= 2; x++) {
                this.b.set(x, fl + 1, back, s(Blocks.SMOOTH_QUARTZ));
                this.b.set(x, fl + 1, back + 1, VillageBuilder.stairs(Blocks.SMOOTH_QUARTZ_STAIRS, Direction.NORTH, Half.BOTTOM));
            }
            for (int sx : new int[] {-1, 1}) {
                for (int y = fl + 2; y <= fl + 5; y++) {
                    this.b.set(sx * 2, y, back, pillar());
                }
                this.b.set(sx * 2, fl + 6, back, s(Blocks.END_ROD));
            }
            for (int x = -1; x <= 1; x++) {
                for (int y = fl + 2; y <= fl + 5; y++) {
                    this.b.set(x, y, back - 1, s(Blocks.OBSIDIAN));
                }
            }
            // der Kopf des Sternenfressers als Trophäe an der Wand, darüber (meist) die Schwingen
            this.b.set(0, fl + 2, back, Blocks.DRAGON_WALL_HEAD.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.WallSkullBlock.FACING, Direction.SOUTH));
            if (this.wings) {
                this.frame(0, fl + 4, back, Direction.SOUTH, new ItemStack(Items.ELYTRA));
            }
            this.b.chest(0, fl + 1, back + 3, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH),
                    ModLootTables.SITE_STARWATCH);
        }

        /** Ein dünner Stab aus grünspanigem Kupfer, längs {@code facing} gelegt. */
        void rod(int x, int y, int z, Direction facing) {
            this.b.set(x, y, z, Blocks.LIGHTNING_ROD.waxed().oxidized().defaultBlockState()
                    .setValue(net.minecraft.world.level.block.LightningRodBlock.FACING, facing));
        }

        /** Ein Rahmen an der Wand (die Wand liegt hinter {@code facing}) mit einem Gegenstand. */
        void frame(int x, int y, int z, Direction facing, ItemStack item) {
            BlockPos pos = this.b.world(x, y, z);
            if (!this.b.inside(pos)) {
                return;
            }
            ItemFrame frame = new ItemFrame(this.b.level.getLevel(), pos, this.b.worldDirection(facing));
            frame.setItem(item, false);
            this.b.level.addFreshEntity(frame);
        }

        // =====================================================================================
        // Kuppel und Fernrohr
        // =====================================================================================

        /**
         * Oben auf dem Turm: eine Brüstung mit Pfosten, in der Mitte die Kuppel aus grünspanigem Kupfer mit einem Spalt nach
         * vorn, darin das große Fernrohr, das durch den Spalt in den Himmel zeigt.
         */
        void dome() {
            int r = TOWER_R;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (octWall(dx, dz, r)) {
                        boolean corner = octCorner(dx, dz, r);
                        this.b.set(dx, DECK + 1, TOWER_Z + dz, corner ? pillar() : s(Blocks.END_STONE_BRICK_WALL));
                        if (corner) {
                            this.b.set(dx, DECK + 2, TOWER_Z + dz, s(Blocks.END_ROD));
                        }
                    }
                }
            }
            // die Trommel (zwei Schichten, mit Gittern als Fenstern) und darauf die Halbkugel; nach vorn ein Spalt
            double cy = DECK + 2.5;
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    double flat = Math.sqrt(dx * dx + dz * dz);
                    for (int y = DECK + 1; y <= DECK + 8; y++) {
                        boolean drum = y <= DECK + 2;
                        double d = drum ? flat : Math.sqrt(dx * dx + (y - cy) * (y - cy) + dz * dz);
                        if (d > 4.6) {
                            continue;
                        }
                        boolean slit = dx == 0 && dz > 0 && y >= DECK + 2;
                        if (d >= 3.6 && !slit) {
                            boolean grate = drum && y == DECK + 2 && (dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz));
                            this.b.set(dx, y, TOWER_Z + dz, grate ? s(Blocks.COPPER_GRATE.waxed().oxidized())
                                    : y == DECK + 1 ? s(Blocks.SMOOTH_QUARTZ) : copper());
                        } else {
                            this.b.set(dx, y, TOWER_Z + dz, AIR);
                        }
                    }
                }
            }
            // die Spitze der Kuppel
            this.b.set(0, DECK + 8, TOWER_Z, s(Blocks.LIGHTNING_ROD.waxed().oxidized()));
            // das Fernrohr: ein dickes Rohr aus Kupfer schräg durch den Spalt hinaus, vorn ein Gitter, hinten das Okular;
            // es ruht auf einem Bock in der Mitte
            double[] from = {DECK + 2.0, -2.6};
            double[] to = {DECK + 9.0, 5.2};
            int steps = 32;
            for (int i = 0; i <= steps; i++) {
                double t = i / (double) steps;
                int y = (int) Math.round(from[0] + (to[0] - from[0]) * t);
                int dz = (int) Math.round(from[1] + (to[1] - from[1]) * t);
                BlockState tube = i >= steps - 1 ? s(Blocks.COPPER_GRATE.waxed().oxidized()) : s(Blocks.COPPER_BLOCK.waxed().oxidized());
                this.b.set(0, y, TOWER_Z + dz, tube);
                if (i > 3 && i < steps - 1) {
                    this.b.set(0, y - 1, TOWER_Z + dz, s(Blocks.CUT_COPPER.waxed().oxidized()));
                }
            }
            this.b.set(0, DECK + 1, TOWER_Z - 3, s(Blocks.LIGHTNING_ROD.waxed().oxidized()));
            for (int y = DECK + 1; y <= DECK + 3; y++) {
                this.b.set(0, y, TOWER_Z, s(Blocks.POLISHED_BLACKSTONE_WALL));
            }
            // auf das Deck führt die letzte Treppe; ein Durchgang in der Kuppel dorthin
            for (int y = DECK + 1; y <= DECK + 3; y++) {
                for (int x = 3; x <= 4; x++) {
                    this.b.set(x, y, TOWER_Z + 1, AIR);
                    this.b.set(x, y, TOWER_Z + 2, AIR);
                }
            }
        }

        // =====================================================================================
        // Seitentürme und Brücken
        // =====================================================================================

        /** Ein Seitenturm: achteckig, schmal, mit einer Leiter innen und einer Spitze aus Amethyst. */
        void sideTower(int sx) {
            int cx = sx * SIDE_X;
            int r = SIDE_R;
            for (int dx = -r - 1; dx <= r + 1; dx++) {
                for (int dz = -r - 1; dz <= r + 1; dz++) {
                    int x = cx + dx;
                    int z = TOWER_Z + dz;
                    if (octWall(dx, dz, r)) {
                        boolean corner = octCorner(dx, dz, r);
                        for (int y = 1; y <= SIDE_TOP; y++) {
                            this.b.set(x, y, z, corner ? pillar() : y % 8 == 0 ? s(Blocks.SMOOTH_QUARTZ) : s(Blocks.END_STONE_BRICKS));
                        }
                        if (octFaceCenter(dx, dz, r) && !(dx == 0 && dz == -r)) {
                            // (nicht hinten: dort hängt innen die Leiter)
                            for (int fl : new int[] {8, 16}) {
                                this.b.set(x, fl + 3, z, s(Blocks.STAINED_GLASS_PANE.purple()));
                                this.b.set(x, fl + 4, z, s(Blocks.STAINED_GLASS_PANE.purple()));
                            }
                        }
                        // Zinnen oben
                        this.b.set(x, SIDE_TOP + 1, z, corner ? pillar() : s(Blocks.END_STONE_BRICK_WALL));
                    } else if (oct(dx, dz, r - 1)) {
                        for (int y = 1; y < SIDE_TOP; y++) {
                            this.b.set(x, y, z, y % 8 == 0 ? s(Blocks.SMOOTH_QUARTZ) : AIR);
                        }
                        this.b.set(x, 0, z, s(Blocks.SMOOTH_QUARTZ));
                        this.b.set(x, SIDE_TOP, z, s(Blocks.SMOOTH_QUARTZ));
                    } else if (oct(dx, dz, r + 1)) {
                        for (int fl : new int[] {8, 16, SIDE_TOP}) {
                            this.b.set(x, fl, z, slab(Blocks.SMOOTH_QUARTZ_SLAB, SlabType.TOP));
                        }
                    }
                }
            }
            // die Tür nach vorn und eine Leiter innen an der Rückwand bis aufs Dach (durch Luken in den Böden)
            for (int y = 1; y <= 3; y++) {
                this.b.set(cx, y, TOWER_Z + r, AIR);
            }
            for (int y = 1; y <= SIDE_TOP; y++) {
                this.b.set(cx, y, TOWER_Z - r + 1, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
            }
            // die Kristallspitze: ein Kegel aus Amethyst mit Kristallen und einem Leuchtstab
            int base = SIDE_TOP + 1;
            for (int y = 0; y <= 5; y++) {
                double rr = 1.6 - y * 0.32;
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        if (Math.sqrt(dx * dx + dz * dz) <= rr && !(dz == -r + 1 && dx == 0 && y == 0)) {
                            this.b.set(cx + dx, base + y, TOWER_Z + dz, s(Blocks.AMETHYST_BLOCK));
                        }
                    }
                }
            }
            this.b.set(cx, base + 5, TOWER_Z, s(Blocks.AMETHYST_BLOCK));
            this.b.set(cx, base + 6, TOWER_Z, s(Blocks.END_ROD));
            for (Direction d : Direction.Plane.HORIZONTAL) {
                this.b.set(cx + d.getStepX() * 2, base + 1, TOWER_Z + d.getStepZ() * 2,
                        Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(net.minecraft.world.level.block.AmethystClusterBlock.FACING, d));
            }
            // die Luke für die Leiter durch die Spitze freilassen: Ausstieg auf das Dach neben dem Kegel
            this.b.set(cx, base, TOWER_Z - r + 1, AIR);
        }

        /** Eine Brücke auf Höhe des dritten Bodens vom Hauptturm zum Seitenturm, mit Bogen darunter und Geländer. */
        void bridge(int sx) {
            int y = FLOORS[2];
            int from = TOWER_R;
            int to = SIDE_X - SIDE_R;
            for (int i = from; i <= to; i++) {
                int x = sx * i;
                for (int dz = -1; dz <= 1; dz++) {
                    this.b.set(x, y, TOWER_Z + dz, dz == 0 ? s(Blocks.SMOOTH_QUARTZ) : s(Blocks.END_STONE_BRICKS));
                    if (Math.abs(dz) == 1 && i > from && i < to) {
                        this.b.set(x, y + 1, TOWER_Z + dz, s(Blocks.END_STONE_BRICK_WALL));
                    }
                }
                // darunter ein Bogen: an den Türmen tief, in der Mitte dünn
                double half = (to - from) / 2.0;
                double u = (i - (from + to) / 2.0) / half;
                int depth = (int) Math.round(3.2 * u * u);
                for (int d = 1; d <= depth; d++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        this.b.set(x, y - d, TOWER_Z + dz, d == depth && depth < 3 ? slab(Blocks.END_STONE_BRICK_SLAB, SlabType.TOP)
                                : s(Blocks.END_STONE_BRICKS));
                    }
                }
            }
            // Durchgänge in beiden Türmen
            for (int h = 1; h <= 3; h++) {
                this.b.set(sx * from, y + h, TOWER_Z, AIR);
                this.b.set(sx * to, y + h, TOWER_Z, AIR);
            }
        }

        // =====================================================================================
        // Schreine, Wachen
        // =====================================================================================

        /** Zwei kleine Schreine vorn am Hof: vier Pfeiler unter einem Dach aus Platten; im linken die Truhe. */
        void shrines() {
            for (int sx : new int[] {-1, 1}) {
                int cx = sx * 7;
                int cz = PLAZA_Z + 7;
                for (int dx = -1; dx <= 1; dx += 2) {
                    for (int dz = -1; dz <= 1; dz += 2) {
                        this.b.set(cx + dx, 1, cz + dz, pillar());
                        this.b.set(cx + dx, 2, cz + dz, pillar());
                    }
                }
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        this.b.set(cx + dx, 3, cz + dz, dx == 0 && dz == 0 ? s(Blocks.SMOOTH_QUARTZ) : slab(Blocks.SMOOTH_QUARTZ_SLAB, SlabType.BOTTOM));
                    }
                }
                this.b.set(cx, 4, cz, s(Blocks.END_ROD));
                if (sx < 0) {
                    this.b.chest(cx, 1, cz, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), ModLootTables.SITE_STARWATCH);
                }
            }
        }

        /** Schreinlinge: in der Halle, in der Reliquienkammer, auf dem Deck, auf den Seitentürmen und in einem Schrein. */
        void guards() {
            int[][] spots = {{-3, 1, TOWER_Z - 3}, {3, 1, TOWER_Z + 3}, {-3, FLOORS[3] + 1, TOWER_Z - 2}, {3, FLOORS[3] + 1, TOWER_Z - 2},
                    {-4, DECK + 1, TOWER_Z - 3}, {-3, DECK + 1, TOWER_Z + 4}, {SIDE_X, SIDE_TOP + 1, TOWER_Z + 2},
                    {-SIDE_X, SIDE_TOP + 1, TOWER_Z + 2}, {7, 1, PLAZA_Z + 7}};
            for (int[] p : spots) {
                this.spawn(p[0], p[1], p[2]);
            }
        }

        void spawn(int x, int y, int z) {
            BlockPos pos = this.b.world(x, y, z);
            if (!this.b.inside(pos)) {
                return;
            }
            Mob mob = ModEntities.SHULKER.get().create(this.b.level.getLevel(), EntitySpawnReason.STRUCTURE);
            if (mob == null) {
                return;
            }
            mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
            mob.finalizeSpawn(this.b.level, this.b.level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
            mob.setPersistenceRequired();
            this.b.level.addFreshEntity(mob);
        }
    }
}
