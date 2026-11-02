package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Aschenesse (an Stelle der Netherfestung): Mitten im Lavameer haben die Brandgebeine ihrem König eine Esse
 * errichtet – einen achteckigen Turm aus Netherziegeln auf einem Sockel aus Schwarzstein, gestützt von Strebepfeilern,
 * mit roten Bändern an jedem Boden. In seinem Kern steigt hinter Gittern eine Säule aus Lava auf; um sie windet sich die
 * Treppe. Oben auf dem Dach trägt ein schlanker Schlot die Glutkrone, eine Feuerschale, die man weithin glühen sieht.
 *
 * <p>Fünf Böden übereinander: die Schlackenhalle am Lavasee (mit einem Anleger für Lavastelzer), die Schmiede, in der
 * zwei Glutgeister in Käfigen gebunden sind, der Warzengarten mit Netherwarzen auf Seelensand, die Gebeinkammer mit
 * Sarkophagen und Nischen und ganz oben der Thronsaal des Aschenkönigs mit seinem Hort. Von der Schmiede führen vier
 * Brücken nach allen Seiten: Wo sie auf Fels treffen, enden sie in Stuben im Gestein (eine Glutkammer, ein Vorrat, eine
 * Wachstube, ein Kerker – oft mit einem Stollen in die nächste Höhle), sonst auf Wachtürmen über der Lava.
 *
 * <p>Eigene Koordinaten: Mitte des Turms bei (0, 0, 0); y = 0 ist der Boden der Schlackenhalle, eine Schicht über dem
 * Lavasee (man steht auf y = 1). Vorn (+z) liegt unten der Anleger, auf derselben Seite steht oben der Thron (mit dem
 * Blick in den Saal).
 */
final class AshForge {
    /** Teile: der Turm und die Brücken (Brücken bauen in ihrem eigenen Rahmen nach +z). */
    static final int TOWER = 0;
    static final int BRIDGE = 1;
    /** Außenkante der Mauer (Achteck, gemessen mit {@link #m}). */
    static final int A = 12;
    /** Innenraum. */
    static final int IN = 10;
    /**
     * Der Glutdom: Die Hitze der Esse hat rings um den Turm eine gewölbte Höhle aus dem Gestein geschmolzen, mit einem
     * Lavasee am Grund – mittlerer Halbmesser und Scheitelhöhe (der Rand ist unregelmäßig, siehe {@link #domeRadius}).
     */
    static final double DOME = 26.0;
    static final int DOME_HEIGHT = 70;
    /** Bis hierher (Halbmesser) reicht der Bereich des Turms. */
    static final int REACH = 32;
    /** Die Böden: Schlackenhalle, Schmiede, Warzengarten, Gebeinkammer, Thronsaal; darüber das Dach. */
    static final int[] LEVELS = {0, 9, 18, 27, 36, 46};
    static final int ROOF = 46;
    /** Höhe der Brückendecks (= Boden der Schmiede). */
    static final int DECK = 9;
    /** Oberkante der Glutkrone. */
    static final int CROWN = 59;
    /** So tief reichen Fundament und Pfeiler höchstens. */
    static final int DEPTH = 36;
    static final int MAX_BRIDGE = 44;
    static final int MAX_EXIT = 20;
    static final int END_TOWER = 0;
    static final int END_ROCK = 1;
    /** Die Brücken-Arten (je Richtung eine): Glutkammer, Vorrat, Wachstube, Kerker. */
    static final int GLOW = 0;
    static final int STORE = 1;
    static final int GUARD = 2;
    static final int JAIL = 3;

    private AshForge() {}

    // =====================================================================================
    // Planung
    // =====================================================================================

    /** Proben für die Lage: Mitte, Innenraum und Mauerring. */
    private static final int[][] PROBES = {{0, 0}, {9, 0}, {-9, 0}, {0, 9}, {0, -9}, {7, 7}, {-7, 7}, {7, -7}, {-7, -7}};

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int base = terrain.seaLevel();
        // unter neun Stellen die freieste: am liebsten steht die Esse im offenen Lavameer
        int bestX = chunk.getMiddleBlockX();
        int bestZ = chunk.getMiddleBlockZ();
        int best = Integer.MIN_VALUE;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int x = chunk.getMiddleBlockX() + i * 12;
                int z = chunk.getMiddleBlockZ() + j * 12;
                int score = openness(terrain, x, z, base) * 4 - Math.abs(i) - Math.abs(j);
                if (score > best) {
                    best = score;
                    bestX = x;
                    bestZ = z;
                }
            }
        }
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 7), 4)];
        BlockPos origin = new BlockPos(bestX, base, bestZ);
        List<SitePiece> pieces = new ArrayList<>();
        pieces.add(tower(origin, rotation, seed));
        for (int d = 0; d < 4; d++) {
            Rotation r = rotation.getRotated(Rotation.values()[d]);
            BlockPos dir = new BlockPos(0, 0, 1).rotate(Rotation.values()[d]);
            int start = (int) Math.floor(domeRadius(seed, Math.atan2(dir.getZ(), dir.getX())) * 0.97);
            int[] wing = reach(terrain, origin, r, start);
            pieces.add(bridge(origin, r, seed + 31L * (d + 1), wing[0], wing[1], wing[2], d));
        }
        return Optional.of(new Structure.GenerationStub(origin, builder -> pieces.forEach(builder::addPiece)));
    }

    private static int openness(SiteTerrain terrain, int x, int z, int base) {
        int score = 0;
        for (int[] p : PROBES) {
            for (int y = base + 3; y <= base + ROOF; y += 4) {
                if (!terrain.rock(x + p[0], y, z + p[1])) {
                    score++;
                }
            }
        }
        if (terrain.fluid(x, base - 1, z)) {
            score += 8;
        }
        return score;
    }

    /**
     * Wie weit eine Brücke (nach eigenem +z) reicht: bis sie auf Fels trifft – dann {Länge, Fels, Ausgang}, wobei der
     * Ausgang sagt, wie lang der Stollen hinter der Felsstube bis in die nächste Höhle ist (0: keiner) – oder bis zum
     * Wachturm über der Lava.
     */
    private static int[] reach(SiteTerrain terrain, BlockPos origin, Rotation r, int start) {
        int deck = origin.getY() + DECK;
        for (int t = start; t <= MAX_BRIDGE; t++) {
            BlockPos p = origin.offset(new BlockPos(0, 0, t).rotate(r));
            int solid = 0;
            for (int dy = 1; dy <= 3; dy++) {
                if (terrain.rock(p.getX(), deck + dy, p.getZ())) {
                    solid++;
                }
            }
            if (solid >= 2) {
                int exit = 0;
                for (int e = t + 18; e <= t + 17 + MAX_EXIT; e++) {
                    BlockPos q = origin.offset(new BlockPos(0, 0, e).rotate(r));
                    if (!terrain.rock(q.getX(), deck + 1, q.getZ()) && !terrain.rock(q.getX(), deck + 2, q.getZ())
                            && !terrain.fluid(q.getX(), deck + 1, q.getZ())) {
                        exit = e - (t + 17);
                        break;
                    }
                }
                return new int[] {t, END_ROCK, exit};
            }
        }
        return new int[] {Math.min(start + 6, MAX_BRIDGE - 8), END_TOWER, 0};
    }

    /** Der Halbmesser des Glutdoms in Richtung {@code angle} (Bogenmaß in eigenen Koordinaten): 22 bis 30 Blöcke. */
    static double domeRadius(long seed, double angle) {
        double p1 = (seed & 0xFFF) / 4096.0 * 2.0 * Math.PI;
        double p2 = ((seed >>> 12) & 0xFFF) / 4096.0 * 2.0 * Math.PI;
        double p3 = ((seed >>> 24) & 0xFFF) / 4096.0 * 2.0 * Math.PI;
        return DOME + 2.2 * Math.sin(3.0 * angle + p1) + 1.3 * Math.sin(5.0 * angle + p2) + 0.8 * Math.sin(7.0 * angle + p3);
    }

    static SitePiece tower(BlockPos origin, Rotation rotation, long seed) {
        int r = REACH;
        return new SitePiece(SiteKind.ASH_FORGE, TOWER, origin, rotation, seed, new int[0], new BoundingBox(origin.getX() - r,
                origin.getY() - DEPTH, origin.getZ() - r, origin.getX() + r, origin.getY() + CROWN + 4, origin.getZ() + r));
    }

    static SitePiece bridge(BlockPos origin, Rotation rotation, long seed, int len, int end, int exit, int variant) {
        int far = end == END_ROCK ? len + 17 + Math.max(exit, 1) : len + 12;
        return new SitePiece(SiteKind.ASH_FORGE, BRIDGE, origin, rotation, seed, new int[] {len, end, exit, variant},
                SitePiece.box(origin, rotation, -7, -DEPTH - 6, 13, 7, DECK + 9, far));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        if (piece.part() == TOWER) {
            new Tower(b, piece.seed()).build();
        } else {
            new Bridge(b, piece).build();
        }
    }

    // =====================================================================================
    // Maße
    // =====================================================================================

    /** Abstand im Achteck: ein regelmäßiges Achteck mit der Kante bei {@code m = r}. */
    static double m(int x, int z) {
        int ax = Math.abs(x);
        int az = Math.abs(z);
        return Math.max(Math.max(ax, az), (ax + az) / 1.4);
    }

    /** Abstand im Quadrat (für Kern, Gitter und Treppe). */
    static int cheb(int x, int z) {
        return Math.max(Math.abs(x), Math.abs(z));
    }

    static boolean isLevel(int y) {
        for (int level : LEVELS) {
            if (level == y) {
                return true;
            }
        }
        return false;
    }

    /** Der höchste Boden unter der Höhe {@code y}. */
    static int levelBelow(int y) {
        int below = 0;
        for (int level : LEVELS) {
            if (level < y) {
                below = level;
            }
        }
        return below;
    }

    /** Eine Stelle an Seite {@code f} (0 vorn/+z, 1 rechts/+x, 2 hinten/−z, 3 links/−x): quer {@code a}, nach außen {@code d}. */
    static int[] face(int f, int a, int d) {
        return switch (f) {
            case 0 -> new int[] {a, d};
            case 1 -> new int[] {d, -a};
            case 2 -> new int[] {-a, -d};
            default -> new int[] {-d, a};
        };
    }

    /** Die Richtung nach außen an Seite {@code f}. */
    static Direction out(int f) {
        return switch (f) {
            case 0 -> Direction.SOUTH;
            case 1 -> Direction.EAST;
            case 2 -> Direction.NORTH;
            default -> Direction.WEST;
        };
    }

    /** Die Richtung, in der {@code a} an Seite {@code f} wächst. */
    static Direction across(int f) {
        return switch (f) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.NORTH;
            case 2 -> Direction.WEST;
            default -> Direction.SOUTH;
        };
    }

    // =====================================================================================
    // Gemeinsames
    // =====================================================================================

    private abstract static class Parts {
        static final BlockState AIR = Blocks.AIR.defaultBlockState();
        final VillageBuilder b;

        Parts(VillageBuilder b) {
            this.b = b;
        }

        static BlockState s(Block block) {
            return block.defaultBlockState();
        }

        /** Netherziegel, hier und da gesprungen. */
        BlockState brick(int x, int y, int z) {
            return this.b.noise(x, y, z) < 0.8f ? s(Blocks.NETHER_BRICKS) : s(Blocks.CRACKED_NETHER_BRICKS);
        }

        /** Schwarzstein für Sockel, Rahmen und Pfeiler; selten ein goldgeäderter Stein. */
        BlockState black(int x, int y, int z) {
            float n = this.b.noise(x, y + 500, z);
            return n < 0.6f ? s(Blocks.POLISHED_BLACKSTONE_BRICKS) : n < 0.84f ? s(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)
                    : n < 0.96f ? s(Blocks.BLACKSTONE) : s(Blocks.GILDED_BLACKSTONE);
        }

        /** Grobes Gestein für Fundamente und Pfeiler im Lavasee. */
        BlockState rubble(int x, int y, int z) {
            float n = this.b.noise(x, y + 900, z);
            return n < 0.48f ? s(Blocks.BLACKSTONE) : n < 0.76f ? s(Blocks.BASALT) : n < 0.94f ? s(Blocks.POLISHED_BLACKSTONE_BRICKS)
                    : s(Blocks.MAGMA_BLOCK);
        }

        /** Schlacke: Schwarzstein, Basalt, Magma, ein wenig Gold. */
        BlockState slag(int x, int y, int z) {
            float n = this.b.noise(x, y + 300, z);
            return n < 0.4f ? s(Blocks.BLACKSTONE) : n < 0.65f ? s(Blocks.BASALT) : n < 0.8f ? s(Blocks.SMOOTH_BASALT)
                    : n < 0.93f ? s(Blocks.MAGMA_BLOCK) : s(Blocks.GILDED_BLACKSTONE);
        }

        void lantern(int x, int y, int z, boolean hanging) {
            this.b.set(x, y, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, hanging));
        }

        /** Eine Laterne an einer Kette, die von der Decke {@code top} herabhängt. */
        void hang(int x, int top, int z, int chain) {
            for (int i = 0; i < chain; i++) {
                this.b.set(x, top - i, z, Blocks.IRON_CHAIN);
            }
            this.lantern(x, top - chain, z, true);
        }

        void chest(int x, int y, int z, Direction facing, boolean hoard) {
            this.b.chest(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing),
                    hoard ? ModLootTables.SITE_ASH_FORGE_HOARD : ModLootTables.SITE_ASH_FORGE);
        }

        /** Ein Spawner, aus dem Glutgeister kommen. */
        void spawner(int x, int y, int z) {
            BlockPos pos = this.b.world(x, y, z);
            if (!this.b.inside(pos)) {
                return;
            }
            this.b.level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_CLIENTS);
            if (this.b.level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
                spawner.setEntityId(ModEntities.BLAZE.get(), RandomSource.create(pos.asLong()));
            }
        }

        void spawn(EntityType<? extends Mob> type, int x, int y, int z) {
            BlockPos pos = this.b.world(x, y, z);
            if (!this.b.inside(pos)) {
                return;
            }
            Mob mob = type.create(this.b.level.getLevel(), EntitySpawnReason.STRUCTURE);
            if (mob == null) {
                return;
            }
            mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.b.noise(x, y, z) * 360.0f, 0.0f);
            mob.finalizeSpawn(this.b.level, this.b.level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
            mob.setPersistenceRequired();
            this.b.level.addFreshEntityWithPassengers(mob);
        }

        /** Füllt von {@code top} abwärts, bis fester Grund kommt (durch Lava und Luft hindurch). */
        void down(int x, int top, int z, boolean edge) {
            for (int y = top; y >= top - DEPTH; y--) {
                BlockState old = this.b.get(x, y, z);
                if (y < top && !old.isAir() && old.getFluidState().isEmpty() && !old.is(Blocks.MAGMA_BLOCK)) {
                    break;
                }
                this.b.set(x, y, z, edge ? this.black(x, y, z) : this.rubble(x, y, z));
            }
        }

        /** Eine Feuerschale: Sockel und darauf ein Seelenlagerfeuer. */
        void brazier(int x, int y, int z) {
            this.b.set(x, y, z, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            this.b.set(x, y + 1, z, s(Blocks.SOUL_CAMPFIRE));
        }
    }

    // =====================================================================================
    // Der Turm
    // =====================================================================================

    private static final class Tower extends Parts {
        /** Die Wendeltreppe um den Kern: Stufen {x, y, z, Richtung} (Richtung −1: Absatz in der Ecke). */
        private final List<int[]> steps = new ArrayList<>();
        private final long seed;

        Tower(VillageBuilder b, long seed) {
            super(b);
            this.seed = seed;
            this.planStairs();
        }

        void build() {
            this.clearing();
            this.foundation();
            this.shell();
            this.openings();
            this.core();
            this.stairs();
            this.slagHall();
            this.forge();
            this.garden();
            this.crypt();
            this.throne();
            this.roof();
            this.dock();
            this.garrison();
        }

        // ---------------------------------------------------------------------------------
        // Gelände
        // ---------------------------------------------------------------------------------

        /**
         * Der Glutdom: rings um den Turm ist das Gestein zu einer gewölbten Höhle fortgeschmolzen (der Rand unregelmäßig, die
         * Wände rau), am Grund liegt ein See aus Lava.
         */
        private void clearing() {
            for (int x = -REACH; x <= REACH; x++) {
                for (int z = -REACH; z <= REACH; z++) {
                    if (!this.b.here(x, 1, z)) {
                        continue;
                    }
                    double r = Math.sqrt(x * x + z * z);
                    double edge = domeRadius(this.seed, Math.atan2(z, x));
                    double rough = (this.b.noise(x, 0, z) - 0.5) * 1.6;
                    if (r > edge + rough) {
                        continue;
                    }
                    double k = Math.min(1.0, r / edge);
                    int top = (int) Math.round(DOME_HEIGHT * Math.sqrt(1.0 - k * k) + (this.b.noise(x, 1, z) - 0.5) * 3.0);
                    for (int y = 0; y <= top; y++) {
                        if (!this.b.get(x, y, z).isAir()) {
                            this.b.set(x, y, z, AIR);
                        }
                    }
                    if (r > A + 1.5) {
                        BlockState below = this.b.get(x, -1, z);
                        if (!below.isAir() && below.getFluidState().isEmpty() && !this.b.get(x, -2, z).isAir()) {
                            this.b.set(x, -1, z, s(Blocks.LAVA));
                        }
                    }
                }
            }
        }

        /** Unter dem Turm (und den Strebepfeilern) bis auf festen Grund gemauert. */
        private void foundation() {
            for (int x = -A - 8; x <= A + 8; x++) {
                for (int z = -A - 8; z <= A + 8; z++) {
                    if (!this.b.here(x, 1, z) || (m(x, z) > A + 1 && !buttress(x, z, 0))) {
                        continue;
                    }
                    this.down(x, -1, z, m(x, z) > A - 1);
                }
            }
        }

        // ---------------------------------------------------------------------------------
        // Mauern, Böden, Strebepfeiler
        // ---------------------------------------------------------------------------------

        /** Wie weit die Strebepfeiler an den Schrägseiten vorspringen (in Schritten von |x| + |z|). */
        private static int reachOf(int y) {
            return y <= 12 ? 6 : y <= 28 ? 4 : y <= 44 ? 2 : 0;
        }

        static boolean buttress(int x, int z, int y) {
            int ax = Math.abs(x);
            int az = Math.abs(z);
            int u = ax + az;
            return Math.abs(ax - az) <= 1 && u >= 17 && u <= 16 + reachOf(y);
        }

        /** Ecksteine an den Kanten des Achtecks. */
        private static boolean quoin(int x, int z) {
            int ax = Math.abs(x);
            int az = Math.abs(z);
            return Math.abs(Math.max(ax, az) - (ax + az) / 1.4) < 0.75;
        }

        private void shell() {
            for (int x = -A - 8; x <= A + 8; x++) {
                for (int z = -A - 8; z <= A + 8; z++) {
                    if (!this.b.here(x, 1, z)) {
                        continue;
                    }
                    double mm = m(x, z);
                    for (int y = 0; y <= ROOF; y++) {
                        BlockState state = this.cell(x, y, z, mm);
                        if (state != null) {
                            this.b.set(x, y, z, state);
                        }
                    }
                }
            }
        }

        private BlockState cell(int x, int y, int z, double mm) {
            if (mm <= IN) {
                return isLevel(y) ? this.floor(x, y, z) : AIR;
            }
            if (mm <= A) {
                return y == ROOF ? s(Blocks.POLISHED_BLACKSTONE_BRICKS) : this.wall(x, y, z, mm);
            }
            if (buttress(x, z, y)) {
                return this.black(x, y, z);
            }
            // die Kappen der Strebepfeiler, wo sie zurückspringen
            if (y > 0 && buttress(x, z, y - 1)) {
                return VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.BOTTOM);
            }
            if (mm <= A + 1) {
                if (y == 0) {
                    return this.black(x, y, z);
                }
                if (y == 1) {
                    return VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.BOTTOM);
                }
                if (y == ROOF) {
                    return s(Blocks.POLISHED_BLACKSTONE_BRICKS);
                }
                if (y == ROOF - 1 || (isLevel(y) && y > 0)) {
                    return VillageBuilder.slab(Blocks.NETHER_BRICK_SLAB, SlabType.TOP);
                }
            }
            return null;
        }

        private BlockState wall(int x, int y, int z, double mm) {
            boolean outer = mm > A - 1;
            if (outer && isLevel(y) && y > 0) {
                return s(Blocks.RED_NETHER_BRICKS);
            }
            if (outer && quoin(x, z) && y > 2) {
                return (y / 2) % 2 == 0 ? s(Blocks.POLISHED_BLACKSTONE_BRICKS) : s(Blocks.NETHER_BRICKS);
            }
            if (y < DECK) {
                if (outer && y > 1 && this.b.noise(x, y + 100, z) < 0.05f) {
                    return s(Blocks.MAGMA_BLOCK);
                }
                return this.black(x, y, z);
            }
            return this.brick(x, y, z);
        }

        private BlockState floor(int x, int y, int z) {
            double r = Math.sqrt(x * x + z * z);
            float n = this.b.noise(x, y, z);
            return switch (y) {
                case 0 -> n < 0.12f ? s(Blocks.MAGMA_BLOCK) : n < 0.3f ? s(Blocks.SMOOTH_BASALT) : n < 0.75f ? s(Blocks.BLACKSTONE)
                        : s(Blocks.POLISHED_BLACKSTONE);
                case 9 -> Math.abs(r - 7.5) < 0.5 ? s(Blocks.RED_NETHER_BRICKS)
                        : (x == 0 || z == 0) && r > 5 ? s(Blocks.CHISELED_POLISHED_BLACKSTONE)
                        : n < 0.8f ? s(Blocks.POLISHED_BLACKSTONE_BRICKS) : s(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
                case 18 -> this.brick(x, y, z);
                case 27 -> n < 0.7f ? s(Blocks.POLISHED_BLACKSTONE) : s(Blocks.POLISHED_BLACKSTONE_BRICKS);
                case 36 -> Math.abs(r - 7.0) < 0.5 ? s(Blocks.RED_NETHER_BRICKS)
                        : Math.abs(x) == Math.abs(z) && r > 7.5 ? s(Blocks.GILDED_BLACKSTONE)
                        : s(Blocks.POLISHED_BLACKSTONE_BRICKS);
                default -> s(Blocks.POLISHED_BLACKSTONE_BRICKS);
            };
        }

        // ---------------------------------------------------------------------------------
        // Tore, Fenster, Nischen
        // ---------------------------------------------------------------------------------

        private void openings() {
            for (int f = 0; f < 4; f++) {
                this.gate(f, DECK);
                if (f == 0) {
                    this.gate(f, 0);
                } else {
                    this.slits(f, 0);
                }
                this.windows(f, 18, 3);
                this.windows(f, 36, 4);
                this.niches(f, 27);
            }
        }

        /** Ein Tor mit Spitzbogen, Rahmen aus Schwarzstein und Schlussstein. */
        private void gate(int f, int level) {
            Direction side = across(f);
            for (int a = -2; a <= 2; a++) {
                for (int d = 11; d <= 12; d++) {
                    for (int y = level + 1; y <= level + 5; y++) {
                        int[] c = face(f, a, d);
                        boolean inner = Math.abs(a) <= 1 && y <= level + 4;
                        if (inner && Math.abs(a) == 1 && y == level + 4) {
                            this.b.set(c[0], y, c[1], VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
                                    a < 0 ? side.getOpposite() : side, Half.TOP));
                        } else if (inner) {
                            this.b.set(c[0], y, c[1], AIR);
                        } else {
                            this.b.set(c[0], y, c[1], s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                        }
                    }
                }
            }
            int[] key = face(f, 0, 12);
            this.b.set(key[0], level + 6, key[1], s(Blocks.CHISELED_POLISHED_BLACKSTONE));
            int[] teeth = face(f, 0, 12);
            this.b.set(teeth[0], level + 4, teeth[1], s(Blocks.IRON_BARS));
        }

        /** Schmale Scharten im Sockel. */
        private void slits(int f, int level) {
            for (int a : new int[] {-3, 3}) {
                for (int y = level + 3; y <= level + 4; y++) {
                    int[] in = face(f, a, 11);
                    int[] outside = face(f, a, 12);
                    this.b.set(in[0], y, in[1], AIR);
                    this.b.set(outside[0], y, outside[1], s(Blocks.IRON_BARS));
                }
            }
        }

        /** Zwei hohe Gitterfenster je Seite, mit Sims und Sturz. */
        private void windows(int f, int level, int height) {
            for (int a : new int[] {-3, 3}) {
                for (int y = level + 3; y < level + 3 + height; y++) {
                    int[] in = face(f, a, 11);
                    int[] outside = face(f, a, 12);
                    this.b.set(in[0], y, in[1], AIR);
                    this.b.set(outside[0], y, outside[1], s(Blocks.IRON_BARS));
                }
                int[] lintel = face(f, a, 12);
                this.b.set(lintel[0], level + 3 + height, lintel[1], s(Blocks.CHISELED_NETHER_BRICKS));
                int[] sill = face(f, a, 13);
                this.b.set(sill[0], level + 2, sill[1], VillageBuilder.slab(Blocks.NETHER_BRICK_SLAB, SlabType.TOP));
            }
        }

        /** Nischen in der Gebeinkammer: ein Gebein, darauf ein Schädel oder eine Seelenlaterne. */
        private void niches(int f, int level) {
            Direction.Axis axis = across(f).getAxis();
            for (int a : new int[] {-3, 0, 3}) {
                int[] c = face(f, a, 11);
                this.b.set(c[0], level + 1, c[1], Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis));
                if (a == 0) {
                    this.lantern(c[0], level + 2, c[1], false);
                } else {
                    this.b.set(c[0], level + 2, c[1], Blocks.SKELETON_WALL_SKULL.defaultBlockState()
                            .setValue(WallSkullBlock.FACING, out(f).getOpposite()));
                }
                this.b.set(c[0], level + 3, c[1], AIR);
                this.b.set(c[0], level + 4, c[1], s(Blocks.CHISELED_NETHER_BRICKS));
            }
        }

        // ---------------------------------------------------------------------------------
        // Kern und Treppe
        // ---------------------------------------------------------------------------------

        /** Die Lavasäule im Kern, eingefasst von Gittern, an jedem Boden ein Kranz aus gemeißeltem Stein. */
        private void core() {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    boolean lava = cheb(x, z) <= 1;
                    for (int y = -1; y <= ROOF; y++) {
                        if (lava) {
                            this.b.set(x, y, z, s(Blocks.LAVA));
                        } else if (y >= 0) {
                            this.b.set(x, y, z, isLevel(y) ? s(Blocks.CHISELED_NETHER_BRICKS) : s(Blocks.IRON_BARS));
                        }
                    }
                }
            }
        }

        private void planStairs() {
            Direction[] climb = {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};
            for (int s = 0; ; s++) {
                int dir = s % 4;
                for (int i = 0; i < 5; i++) {
                    int y = 1 + 5 * s + i;
                    if (y > ROOF) {
                        return;
                    }
                    for (int w = 0; w < 2; w++) {
                        int[] c = tread(dir, i, w);
                        this.steps.add(new int[] {c[0], y, c[1], climb[dir].ordinal()});
                    }
                }
                int landing = 5 * s + 5;
                for (int[] c : corner(dir)) {
                    this.steps.add(new int[] {c[0], landing, c[1], -1});
                }
            }
        }

        /** Zelle {@code w} (0 innen, 1 außen) der Stufe {@code i} an der Seite {@code dir} (0 Nord, 1 Ost, 2 Süd, 3 West). */
        private static int[] tread(int dir, int i, int w) {
            return switch (dir) {
                case 0 -> new int[] {-2 + i, -3 - w};
                case 1 -> new int[] {3 + w, -2 + i};
                case 2 -> new int[] {2 - i, 3 + w};
                default -> new int[] {-3 - w, 2 - i};
            };
        }

        /** Der Absatz in der Ecke nach der Seite {@code dir}. */
        private static int[][] corner(int dir) {
            int sx = dir == 0 || dir == 1 ? 1 : -1;
            int sz = dir == 1 || dir == 2 ? 1 : -1;
            return new int[][] {{3 * sx, 3 * sz}, {4 * sx, 3 * sz}, {3 * sx, 4 * sz}, {4 * sx, 4 * sz}};
        }

        private static long key(int x, int y, int z) {
            return BlockPos.asLong(x, y, z);
        }

        /** Die Wendeltreppe: Stufen auf gemauerten Keilen, Löcher in den Böden darüber, Geländer an den Löchern. */
        private void stairs() {
            Set<Long> holes = new HashSet<>();
            Set<Long> treads = new HashSet<>();
            for (int[] st : this.steps) {
                int x = st[0];
                int y = st[1];
                int z = st[2];
                treads.add(key(x, y, z));
                for (int yy = levelBelow(y) + 1; yy < y; yy++) {
                    this.b.set(x, yy, z, this.brick(x, yy, z));
                }
                if (st[3] < 0) {
                    this.b.set(x, y, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                } else {
                    this.b.set(x, y, z, VillageBuilder.stairs(Blocks.NETHER_BRICK_STAIRS, Direction.values()[st[3]], Half.BOTTOM));
                }
                for (int level : LEVELS) {
                    if (level > y && level <= y + 3) {
                        this.b.set(x, level, z, AIR);
                        holes.add(key(x, level, z));
                    }
                }
            }
            for (long h : holes) {
                BlockPos p = BlockPos.of(h);
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    int nx = p.getX() + dir.getStepX();
                    int nz = p.getZ() + dir.getStepZ();
                    if (cheb(nx, nz) < 3 || m(nx, nz) > IN || holes.contains(key(nx, p.getY(), nz)) || treads.contains(key(nx, p.getY(), nz))) {
                        continue;
                    }
                    this.b.set(nx, p.getY() + 1, nz, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                }
            }
        }

        // ---------------------------------------------------------------------------------
        // Die Böden
        // ---------------------------------------------------------------------------------

        /** In den vier Ecktaschen eines Bodens (außerhalb der Treppe). */
        private static boolean pocket(int x, int z) {
            return Math.abs(x) >= 5 && Math.abs(z) >= 5 && m(x, z) <= IN;
        }

        /** Die Schlackenhalle: Haufen von Schlacke, Kessel voll Glut, Ketten von der Decke. */
        private void slagHall() {
            for (int x = -IN; x <= IN; x++) {
                for (int z = -IN; z <= IN; z++) {
                    if (!pocket(x, z)) {
                        continue;
                    }
                    float n = this.b.noise(x, 1, z);
                    int h = (n < 0.35f ? 0 : n < 0.8f ? 1 : 2) + (m(x, z) > IN - 1.5 ? 1 : 0);
                    for (int y = 1; y <= h; y++) {
                        this.b.set(x, y, z, this.slag(x, y, z));
                    }
                }
            }
            for (int f = 1; f < 4; f++) {
                int[] c = face(f, 0, 9);
                this.b.set(c[0], 1, c[1], s(Blocks.LAVA_CAULDRON));
                int[] side = face(f, 1, 9);
                this.b.set(side[0], 1, side[1], s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                int[] other = face(f, -1, 9);
                this.b.set(other[0], 1, other[1], s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            }
            for (int[] c : new int[][] {{6, 6}, {-6, 6}, {6, -6}, {-6, -6}}) {
                this.hang(c[0], 8, c[1], 2);
            }
        }

        /** Die Schmiede: zwei Glutkäfige, Essen mit einer Glutrinne, Ambosse und eine Werkbank des Schmieds. */
        private void forge() {
            this.cage(6, -6);
            this.cage(-6, 6);
            // Essen im Nordwesten
            for (int[] c : new int[][] {{-6, -6}, {-7, -6}, {-6, -7}, {-7, -7}}) {
                this.b.set(c[0], DECK - 1, c[1], s(Blocks.BLACKSTONE));
                this.b.set(c[0], DECK, c[1], s(Blocks.LAVA));
                this.b.set(c[0], DECK + 1, c[1], s(Blocks.IRON_BARS));
            }
            this.b.set(-8, DECK + 1, -5, s(Blocks.BLAST_FURNACE).setValue(AbstractFurnaceBlock.FACING, Direction.EAST));
            this.b.set(-5, DECK + 1, -8, s(Blocks.BLAST_FURNACE).setValue(AbstractFurnaceBlock.FACING, Direction.SOUTH));
            this.b.set(-8, DECK + 2, -5, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
            this.b.set(-5, DECK + 2, -8, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
            this.b.set(-5, DECK + 1, -6, s(Blocks.DAMAGED_ANVIL).setValue(AnvilBlock.FACING, Direction.NORTH));
            this.b.set(-8, DECK + 1, -3, s(Blocks.LAVA_CAULDRON));
            // Werkplatz im Südosten
            this.b.set(6, DECK + 1, 6, s(Blocks.SMITHING_TABLE));
            this.b.set(8, DECK + 1, 5, s(Blocks.GRINDSTONE).setValue(GrindstoneBlock.FACE, AttachFace.FLOOR)
                    .setValue(GrindstoneBlock.FACING, Direction.NORTH));
            this.b.set(5, DECK + 1, 8, s(Blocks.CHIPPED_ANVIL).setValue(AnvilBlock.FACING, Direction.WEST));
            this.chest(7, DECK + 1, 7, Direction.NORTH, false);
            for (int f = 0; f < 4; f++) {
                int[] c = face(f, 0, 7);
                this.hang(c[0], DECK + 8, c[1], 1);
            }
        }

        /** Ein Glutkäfig: auf einem Sockel mit Magma schwebt ein gebundener Glutgeist (ein Spawner) hinter Gittern. */
        private void cage(int cx, int cz) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int x = cx + dx;
                    int z = cz + dz;
                    boolean middle = dx == 0 && dz == 0;
                    this.b.set(x, DECK + 1, z, middle ? s(Blocks.MAGMA_BLOCK) : s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    for (int y = DECK + 2; y <= DECK + 3; y++) {
                        this.b.set(x, y, z, middle ? AIR : s(Blocks.IRON_BARS));
                    }
                    this.b.set(x, DECK + 4, z, s(Blocks.IRON_BARS));
                }
            }
            this.spawner(cx, DECK + 2, cz);
            for (int y = DECK + 5; y <= DECK + 8; y++) {
                this.b.set(cx, y, cz, Blocks.IRON_CHAIN);
            }
        }

        /** Der Warzengarten: Netherwarzen auf Seelensand in den Ecktaschen und an den Seitenwänden. */
        private void garden() {
            int level = 18;
            for (int x = -IN; x <= IN; x++) {
                for (int z = -IN; z <= IN; z++) {
                    if (m(x, z) > IN) {
                        continue;
                    }
                    int ax = Math.abs(x);
                    int az = Math.abs(z);
                    boolean pocket = pocket(x, z);
                    boolean bed = ax >= 9 && az <= 4;
                    boolean rim = pocket ? ax == 5 || az == 5 : ax == 8 && az <= 4;
                    if (rim) {
                        this.b.set(x, level + 1, z, s(Blocks.NETHER_BRICKS));
                    } else if (pocket || bed) {
                        this.b.set(x, level + 1, z, s(Blocks.SOUL_SAND));
                        float n = this.b.noise(x, level + 2, z);
                        int age = n < 0.45f ? 3 : n < 0.75f ? 2 : 1;
                        this.b.set(x, level + 2, z, Blocks.NETHER_WART.defaultBlockState().setValue(NetherWartBlock.AGE, age));
                    }
                }
            }
            // vorn eine Feuerschale mit zwei Bänken, hinten die Vorräte
            this.b.set(0, level + 1, 9, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
            this.b.set(0, level + 2, 9, s(Blocks.SOUL_SOIL));
            this.b.set(0, level + 3, 9, s(Blocks.SOUL_FIRE));
            this.b.set(-2, level + 1, 9, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.WEST, Half.BOTTOM));
            this.b.set(2, level + 1, 9, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.EAST, Half.BOTTOM));
            this.chest(0, level + 1, -9, Direction.SOUTH, false);
            this.b.set(-1, level + 1, -9, s(Blocks.BARREL).setValue(BarrelBlock.FACING, Direction.UP));
            this.b.set(1, level + 1, -9, s(Blocks.BARREL).setValue(BarrelBlock.FACING, Direction.UP));
            for (int f = 0; f < 4; f++) {
                int[] c = face(f, 0, 6);
                this.hang(c[0], level + 8, c[1], 2);
            }
        }

        /** Die Gebeinkammer: Sarkophage in den Ecktaschen, Gebeine, zwei Truhen. */
        private void crypt() {
            int level = 27;
            int[][] coffins = {{1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
            for (int[] q : coffins) {
                int sx = q[0];
                int sz = q[1];
                for (int i = 5; i <= 7; i++) {
                    this.b.set(sx * i, level + 1, sz * 6, s(Blocks.POLISHED_BLACKSTONE));
                    this.b.set(sx * i, level + 2, sz * 6, i == 7 ? s(Blocks.CHISELED_POLISHED_BLACKSTONE)
                            : VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.BOTTOM));
                }
                this.lantern(sx * 7, level + 3, sz * 6, false);
                for (int[] c : new int[][] {{6, 8}, {7, 7}, {5, 8}, {8, 6}}) {
                    int x = sx * c[0];
                    int z = sz * c[1];
                    if (m(x, z) <= IN && this.b.noise(x, level, z) < 0.6f) {
                        this.b.set(x, level + 1, z, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS,
                                this.b.noise(x, level + 1, z) < 0.5f ? Direction.Axis.X : Direction.Axis.Z));
                    }
                }
            }
            this.chest(0, level + 1, -9, Direction.SOUTH, false);
            this.chest(0, level + 1, 9, Direction.NORTH, false);
            for (int f = 0; f < 4; f++) {
                int[] c = face(f, 0, 7);
                this.hang(c[0], level + 8, c[1], 1);
            }
        }

        /**
         * Der Thronsaal: vorn (Süden) der Thron auf einem Podest zwischen zwei hohen Fenstern, zu beiden Seiten der Hort des
         * Königs und Feuerschalen; vier Säulen aus Basalt mit goldenen Ringen.
         */
        private void throne() {
            int level = 36;
            for (int x = -4; x <= 4; x++) {
                for (int z = 6; z <= 10; z++) {
                    if (m(x, z) > IN) {
                        continue;
                    }
                    if (z == 6) {
                        if (Math.abs(x) <= 3) {
                            this.b.set(x, level + 1, z, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.SOUTH, Half.BOTTOM));
                        }
                    } else {
                        this.b.set(x, level + 1, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    }
                }
            }
            this.b.set(0, level + 2, 9, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.SOUTH, Half.BOTTOM));
            this.b.set(-1, level + 2, 9, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            this.b.set(1, level + 2, 9, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            this.b.set(0, level + 2, 10, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
            this.b.set(0, level + 3, 10, s(Blocks.CHISELED_POLISHED_BLACKSTONE));
            this.b.set(0, level + 4, 10, s(Blocks.GILDED_BLACKSTONE));
            this.b.set(0, level + 5, 10, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
            this.b.set(0, level + 6, 10, Blocks.WITHER_SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 8));
            for (int x : new int[] {-2, 2}) {
                for (int y = level + 2; y <= level + 5; y++) {
                    this.b.set(x, y, 10, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                }
                this.lantern(x, level + 6, 10, false);
            }
            this.chest(-3, level + 2, 9, Direction.NORTH, true);
            this.chest(3, level + 2, 9, Direction.NORTH, true);
            this.b.set(-4, level + 2, 7, s(Blocks.SOUL_CAMPFIRE));
            this.b.set(4, level + 2, 7, s(Blocks.SOUL_CAMPFIRE));
            for (int[] c : new int[][] {{7, 3}, {-7, 3}, {7, -3}, {-7, -3}}) {
                for (int y = level + 1; y <= level + 9; y++) {
                    this.b.set(c[0], y, c[1], y == level + 5 ? s(Blocks.GILDED_BLACKSTONE) : s(Blocks.POLISHED_BASALT));
                }
            }
            for (int[] c : new int[][] {{5, 7}, {-5, 7}, {0, -7}}) {
                this.hang(c[0], level + 9, c[1], 2);
            }
        }

        // ---------------------------------------------------------------------------------
        // Dach, Schlot und Glutkrone
        // ---------------------------------------------------------------------------------

        private static boolean merlon(int x, int z) {
            int k = (int) Math.floor((Math.atan2(z, x) + Math.PI) / (2.0 * Math.PI) * 32.0);
            return (k & 1) == 0;
        }

        private void roof() {
            for (int x = -A - 2; x <= A + 2; x++) {
                for (int z = -A - 2; z <= A + 2; z++) {
                    if (!this.b.here(x, 1, z)) {
                        continue;
                    }
                    double mm = m(x, z);
                    int c = cheb(x, z);
                    if (mm > A && mm <= A + 1) {
                        this.b.set(x, ROOF + 1, z, s(Blocks.NETHER_BRICKS));
                        if (merlon(x, z)) {
                            this.b.set(x, ROOF + 2, z, s(Blocks.NETHER_BRICKS));
                        }
                    }
                    // der Schlot
                    if (mm <= 2.5 && c > 1) {
                        for (int y = ROOF + 1; y <= ROOF + 7; y++) {
                            this.b.set(x, y, z, y == ROOF + 3 || y == ROOF + 6 ? s(Blocks.RED_NETHER_BRICKS) : this.brick(x, y, z));
                        }
                    }
                    if (c <= 1) {
                        for (int y = ROOF + 1; y <= ROOF + 10; y++) {
                            this.b.set(x, y, z, s(Blocks.LAVA));
                        }
                        continue;
                    }
                    // die Glutkrone: eine Feuerschale, nach unten getreppt
                    if (mm <= 3.5) {
                        this.b.set(x, ROOF + 8, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    }
                    if (mm <= 4.5) {
                        this.b.set(x, ROOF + 9, z, this.black(x, ROOF + 9, z));
                        this.b.set(x, ROOF + 10, z, s(Blocks.NETHERRACK));
                        this.b.set(x, ROOF + 11, z, s(Blocks.FIRE));
                    } else if (mm <= 5.5) {
                        this.b.set(x, ROOF + 9, z, VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.TOP));
                        this.b.set(x, ROOF + 10, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                        this.b.set(x, ROOF + 11, z, s(Blocks.BLACKSTONE_WALL));
                    }
                }
            }
            // die Zacken der Krone
            for (int[] c : new int[][] {{5, 2}, {2, 5}, {-2, 5}, {-5, 2}, {-5, -2}, {-2, -5}, {2, -5}, {5, -2}}) {
                this.b.set(c[0], ROOF + 12, c[1], s(Blocks.BLACKSTONE_WALL));
                this.b.set(c[0], ROOF + 13, c[1], s(Blocks.POLISHED_BLACKSTONE_WALL));
            }
            // Fialen über den Strebepfeilern, jede mit einem Feuer
            for (int[] q : new int[][] {{1, 1}, {-1, 1}, {1, -1}, {-1, -1}}) {
                int x = q[0] * 9;
                int z = q[1] * 9;
                for (int y = ROOF + 1; y <= ROOF + 3; y++) {
                    this.b.set(x, y, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                }
                this.b.set(x, ROOF + 4, z, s(Blocks.CHISELED_POLISHED_BLACKSTONE));
                this.b.set(x, ROOF + 5, z, s(Blocks.SOUL_CAMPFIRE));
            }
        }

        /** Der Anleger vor dem unteren Tor: eine Plattform im Lavagraben, mit Pollern und Laternen. */
        private void dock() {
            for (int x = -4; x <= 4; x++) {
                for (int z = 13; z <= 18; z++) {
                    if (!this.b.here(x, 0, z)) {
                        continue;
                    }
                    boolean rim = Math.abs(x) == 4 || z == 18;
                    this.b.set(x, 0, z, rim ? s(Blocks.POLISHED_BLACKSTONE_BRICKS) : this.black(x, 0, z));
                    this.down(x, -1, z, rim);
                    for (int y = 1; y <= 5; y++) {
                        if (!this.b.get(x, y, z).isAir()) {
                            this.b.set(x, y, z, AIR);
                        }
                    }
                }
            }
            for (int x : new int[] {-4, 4}) {
                this.b.set(x, 1, 18, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                this.b.set(x, 2, 18, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                this.lantern(x, 3, 18, false);
                this.b.set(x, 1, 15, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            }
        }

        /** Die Wache der Esse: Brandgebeine in der Schmiede, in der Gebeinkammer und vor dem Thron. */
        private void garrison() {
            this.spawn(ModEntities.WITHER_SKELETON.get(), 7, DECK + 1, 2);
            this.spawn(ModEntities.WITHER_SKELETON.get(), -7, DECK + 1, -2);
            this.spawn(ModEntities.WITHER_SKELETON.get(), -7, 28, 2);
            this.spawn(ModEntities.WITHER_SKELETON.get(), 0, 37, 5);
        }
    }

    // =====================================================================================
    // Die Brücken
    // =====================================================================================

    private static final class Bridge extends Parts {
        private final int len;
        private final int end;
        private final int exit;
        private final int variant;

        Bridge(VillageBuilder b, SitePiece piece) {
            super(b);
            this.len = piece.data(0);
            this.end = piece.data(1);
            this.exit = piece.data(2);
            this.variant = piece.data(3);
        }

        void build() {
            int last = this.end == END_ROCK ? this.len - 1 : this.len;
            List<Integer> piers = new ArrayList<>();
            for (int p = 22; p + 1 <= last - 2; p += 8) {
                piers.add(p);
            }
            for (int z = 13; z <= last; z++) {
                this.span(z, piers);
            }
            for (int p : piers) {
                this.pier(p);
            }
            if (this.end == END_ROCK) {
                this.portal(this.len);
                this.tunnel(this.len + 1, this.len + 4);
                this.chamber(this.len + 5);
                if (this.exit > 0) {
                    this.passage(this.len + 18, this.len + 17 + this.exit);
                }
            } else {
                this.watchtower(this.len + 6);
            }
        }

        /** Ein Querschnitt der Brücke: Deck mit Mittellinie, Brüstung mit Laternenpfosten, darunter die Bögen. */
        private void span(int z, List<Integer> piers) {
            int support = this.end == END_ROCK ? this.len : this.len + 2;
            int dist = Math.min(z - 12, support - z);
            for (int p : piers) {
                dist = Math.min(dist, z < p ? p - z : z > p + 1 ? z - p - 1 : 0);
            }
            for (int x = -4; x <= 4; x++) {
                if (!this.b.here(x, DECK, z)) {
                    continue;
                }
                int ax = Math.abs(x);
                if (ax == 4) {
                    this.b.set(x, DECK - 1, z, VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.TOP));
                    continue;
                }
                this.b.set(x, DECK - 1, z, s(Blocks.NETHER_BRICKS));
                this.b.set(x, DECK, z, ax == 3 ? s(Blocks.POLISHED_BLACKSTONE_BRICKS)
                        : x == 0 && z % 4 == 0 ? s(Blocks.CHISELED_NETHER_BRICKS) : this.brick(x, DECK, z));
                for (int y = DECK + 1; y <= DECK + 4; y++) {
                    if (!this.b.get(x, y, z).isAir()) {
                        this.b.set(x, y, z, AIR);
                    }
                }
                if (ax == 3) {
                    this.b.set(x, DECK + 1, z, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                    if (z % 6 == 0) {
                        this.b.set(x, DECK + 2, z, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                        this.lantern(x, DECK + 3, z, false);
                    }
                }
                // die Bögen unter dem Deck
                if (dist == 1) {
                    this.b.set(x, DECK - 2, z, s(Blocks.NETHER_BRICKS));
                    this.b.set(x, DECK - 3, z, s(Blocks.NETHER_BRICKS));
                } else if (dist == 2) {
                    this.b.set(x, DECK - 2, z, s(Blocks.NETHER_BRICKS));
                    this.b.set(x, DECK - 3, z, VillageBuilder.slab(Blocks.NETHER_BRICK_SLAB, SlabType.TOP));
                } else if (dist == 3) {
                    this.b.set(x, DECK - 2, z, VillageBuilder.slab(Blocks.NETHER_BRICK_SLAB, SlabType.TOP));
                }
            }
        }

        /** Ein Pfeiler bis auf den Grund des Lavasees. */
        private void pier(int p) {
            for (int z = p; z <= p + 1; z++) {
                for (int x = -3; x <= 3; x++) {
                    if (this.b.here(x, DECK, z)) {
                        this.down(x, DECK - 2, z, Math.abs(x) == 3);
                    }
                }
            }
        }

        /** Wo die Brücke auf Fels trifft: ein Tor mit Rahmen, Schlussstein und zwei Feuerschalen. */
        private void portal(int z) {
            for (int x = -3; x <= 3; x++) {
                for (int y = DECK - 1; y <= DECK + 6; y++) {
                    int ax = Math.abs(x);
                    boolean opening = ax <= 2 && y >= DECK + 1 && y <= DECK + 4;
                    if (opening && ax == 2 && y == DECK + 4) {
                        this.b.set(x, y, z, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
                                x < 0 ? Direction.WEST : Direction.EAST, Half.TOP));
                    } else if (opening) {
                        this.b.set(x, y, z, AIR);
                    } else if (y == DECK) {
                        this.b.set(x, y, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    } else {
                        this.b.set(x, y, z, this.black(x, y, z));
                    }
                }
            }
            this.b.set(0, DECK + 6, z, s(Blocks.CHISELED_POLISHED_BLACKSTONE));
            for (int x : new int[] {-4, 4}) {
                this.b.set(x, DECK, z - 1, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                this.brazier(x, DECK + 1, z - 1);
            }
        }

        /** Ein gemauerter Stollen ins Gestein. */
        private void tunnel(int z0, int z1) {
            for (int z = z0; z <= z1; z++) {
                for (int x = -3; x <= 3; x++) {
                    int ax = Math.abs(x);
                    for (int y = DECK - 1; y <= DECK + 5; y++) {
                        if (y == DECK && ax <= 2) {
                            this.b.set(x, y, z, s(Blocks.POLISHED_BLACKSTONE));
                        } else if (ax == 3 || y == DECK + 5 || y == DECK - 1 || y == DECK) {
                            this.b.set(x, y, z, this.brick(x, y, z));
                        } else if (ax == 2 && y == DECK + 4) {
                            this.b.set(x, y, z, VillageBuilder.stairs(Blocks.NETHER_BRICK_STAIRS, x < 0 ? Direction.WEST : Direction.EAST, Half.TOP));
                        } else {
                            this.b.set(x, y, z, AIR);
                        }
                    }
                }
            }
            this.hang(0, DECK + 4, (z0 + z1) / 2, 0);
        }

        /** Die Stube im Fels: ein gewölbter Raum mit vier Basaltsäulen; was darin ist, hängt von der Richtung ab. */
        private void chamber(int c0) {
            int c1 = c0 + 12;
            for (int x = -6; x <= 6; x++) {
                for (int z = c0; z <= c1; z++) {
                    if (!this.b.here(x, DECK, z)) {
                        continue;
                    }
                    for (int y = DECK - 1; y <= DECK + 7; y++) {
                        boolean shell = Math.abs(x) == 6 || z == c0 || z == c1 || y == DECK - 1 || y == DECK + 7;
                        if (shell) {
                            this.b.set(x, y, z, y == DECK + 7 || y == DECK - 1 ? this.black(x, y, z) : this.brick(x, y, z));
                        } else if (y == DECK) {
                            this.b.set(x, y, z, (x + z) % 4 == 0 && Math.abs(x) <= 3 ? s(Blocks.POLISHED_BLACKSTONE_BRICKS)
                                    : s(Blocks.POLISHED_BLACKSTONE));
                        } else if (y == DECK + 6 && (Math.abs(x) == 5 || z == c0 + 1 || z == c1 - 1)) {
                            this.b.set(x, y, z, VillageBuilder.slab(Blocks.NETHER_BRICK_SLAB, SlabType.TOP));
                        } else {
                            this.b.set(x, y, z, AIR);
                        }
                    }
                }
            }
            // der Eingang vom Stollen
            for (int x = -2; x <= 2; x++) {
                for (int y = DECK + 1; y <= DECK + 4; y++) {
                    if (Math.abs(x) == 2 && y == DECK + 4) {
                        continue;
                    }
                    this.b.set(x, y, c0, AIR);
                }
            }
            if (this.variant != JAIL) {
                for (int px : new int[] {-4, 4}) {
                    for (int pz : new int[] {c0 + 3, c0 + 9}) {
                        for (int y = DECK + 1; y <= DECK + 5; y++) {
                            this.b.set(px, y, pz, s(Blocks.POLISHED_BASALT));
                        }
                        this.b.set(px, DECK + 6, pz, s(Blocks.CHISELED_POLISHED_BLACKSTONE));
                    }
                }
            }
            if (this.exit > 0) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = DECK + 1; y <= DECK + 3; y++) {
                        this.b.set(x, y, c1, AIR);
                    }
                }
            }
            switch (this.variant) {
                case GLOW -> {
                    // Glutkammer: der dritte gebundene Glutgeist, umgeben von Feuerschalen
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            this.b.set(dx, DECK, c0 + 6 + dz, dx == 0 && dz == 0 ? s(Blocks.MAGMA_BLOCK) : s(Blocks.GILDED_BLACKSTONE));
                        }
                    }
                    this.spawner(0, DECK + 1, c0 + 6);
                    this.brazier(-3, DECK + 1, c0 + 6);
                    this.brazier(3, DECK + 1, c0 + 6);
                    this.chest(5, DECK + 1, c1 - 1, Direction.WEST, false);
                    this.hang(0, DECK + 6, c0 + 2, 1);
                    this.hang(0, DECK + 6, c1 - 2, 1);
                }
                case STORE -> {
                    // Vorratskammer: Fässer an den Wänden, zwei Truhen, ein Leuchtpilz an der Kette
                    for (int x : new int[] {-5, 5}) {
                        for (int z = c0 + 1; z <= c1 - 1; z++) {
                            if (z == c0 + 3 || z == c0 + 9) {
                                continue;
                            }
                            if (z == c0 + 6) {
                                this.chest(x, DECK + 1, z, x < 0 ? Direction.EAST : Direction.WEST, false);
                                continue;
                            }
                            this.b.set(x, DECK + 1, z, s(Blocks.BARREL).setValue(BarrelBlock.FACING, Direction.UP));
                            if (this.b.noise(x, DECK + 2, z) < 0.5f) {
                                this.b.set(x, DECK + 2, z, s(Blocks.BARREL).setValue(BarrelBlock.FACING, x < 0 ? Direction.EAST : Direction.WEST));
                            }
                        }
                    }
                    this.b.set(0, DECK + 6, c0 + 6, Blocks.IRON_CHAIN);
                    this.b.set(0, DECK + 5, c0 + 6, s(Blocks.SHROOMLIGHT));
                }
                case GUARD -> {
                    // Wachstube: ein Feuer mit Bänken, ein Schleifstein, eine Truhe – und zwei Wachen
                    this.b.set(0, DECK + 1, c0 + 6, s(Blocks.SOUL_CAMPFIRE));
                    this.b.set(0, DECK + 1, c0 + 4, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
                    this.b.set(0, DECK + 1, c0 + 8, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.SOUTH, Half.BOTTOM));
                    this.b.set(-2, DECK + 1, c0 + 6, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.WEST, Half.BOTTOM));
                    this.b.set(2, DECK + 1, c0 + 6, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.EAST, Half.BOTTOM));
                    this.b.set(4, DECK + 1, c0 + 1, s(Blocks.GRINDSTONE).setValue(GrindstoneBlock.FACE, AttachFace.FLOOR)
                            .setValue(GrindstoneBlock.FACING, Direction.WEST));
                    this.chest(-5, DECK + 1, c1 - 1, Direction.EAST, false);
                    this.hang(0, DECK + 6, c0 + 2, 1);
                    this.spawn(ModEntities.WITHER_SKELETON.get(), 3, DECK + 1, c0 + 7);
                    this.spawn(ModEntities.WITHER_SKELETON.get(), -3, DECK + 1, c0 + 5);
                }
                default -> {
                    // Kerker: drei Zellen hinter Gittern (eine Tür ist aufgebrochen), Gebeine und Ketten, die Truhe des Wärters
                    for (int z = c0 + 1; z <= c1 - 1; z++) {
                        for (int y = DECK + 1; y <= DECK + 3; y++) {
                            this.b.set(2, y, z, s(Blocks.IRON_BARS));
                        }
                    }
                    for (int z : new int[] {c0 + 4, c0 + 8}) {
                        for (int x = 3; x <= 5; x++) {
                            for (int y = DECK + 1; y <= DECK + 5; y++) {
                                this.b.set(x, y, z, this.brick(x, y, z));
                            }
                        }
                    }
                    this.b.set(2, DECK + 1, c0 + 6, AIR);
                    this.b.set(2, DECK + 2, c0 + 6, AIR);
                    for (int z : new int[] {c0 + 2, c0 + 6, c0 + 10}) {
                        this.b.set(4, DECK + 1, z, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
                        for (int y = DECK + 3; y <= DECK + 5; y++) {
                            this.b.set(5, y, z, Blocks.IRON_CHAIN);
                        }
                    }
                    this.b.set(4, DECK + 2, c0 + 10, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 4));
                    this.chest(-5, DECK + 1, c0 + 2, Direction.EAST, false);
                    this.b.set(-5, DECK + 1, c0 + 4, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                    this.lantern(-5, DECK + 2, c0 + 4, false);
                    this.hang(-2, DECK + 6, c0 + 8, 1);
                }
            }
        }

        /** Der Stollen hinter der Felsstube bis in die nächste Höhle: nur wo Fels ist, wird er ausgemauert. */
        private void passage(int z0, int z1) {
            for (int z = z0; z <= z1; z++) {
                for (int x = -2; x <= 2; x++) {
                    if (!this.b.here(x, DECK, z)) {
                        continue;
                    }
                    int ax = Math.abs(x);
                    for (int y = DECK; y <= DECK + 4; y++) {
                        BlockState old = this.b.get(x, y, z);
                        boolean rock = !old.isAir() && old.getFluidState().isEmpty();
                        if (y == DECK && ax <= 1) {
                            this.b.set(x, y, z, s(Blocks.POLISHED_BLACKSTONE));
                        } else if (ax <= 1 && y <= DECK + 3) {
                            this.b.set(x, y, z, AIR);
                        } else if (rock) {
                            this.b.set(x, y, z, this.brick(x, y, z));
                        }
                    }
                }
                if ((z - z0) % 6 == 3 && this.b.get(-2, DECK + 2, z).isSolidRender()) {
                    this.b.set(-1, DECK + 2, z, Blocks.SOUL_WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST));
                }
            }
        }

        /** Der Wachturm über der Lava: eine achteckige Plattform auf einem Pfeiler, mit Zinnen und einem Feuer. */
        private void watchtower(int c) {
            for (int x = -6; x <= 6; x++) {
                for (int z = c - 6; z <= c + 6; z++) {
                    if (!this.b.here(x, DECK, z)) {
                        continue;
                    }
                    double mm = m(x, z - c);
                    if (mm > 5) {
                        continue;
                    }
                    if (mm <= 4) {
                        this.down(x, DECK - 2, z, mm > 3);
                    } else {
                        this.b.set(x, DECK - 2, z, VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.TOP));
                    }
                    this.b.set(x, DECK - 1, z, s(Blocks.NETHER_BRICKS));
                    this.b.set(x, DECK, z, mm <= 1.5 ? s(Blocks.CHISELED_POLISHED_BLACKSTONE) : s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    for (int y = DECK + 1; y <= DECK + 5; y++) {
                        if (!this.b.get(x, y, z).isAir()) {
                            this.b.set(x, y, z, AIR);
                        }
                    }
                    if (mm > 4) {
                        boolean gate = z - c <= -4 && Math.abs(x) <= 2;
                        if (!gate) {
                            this.b.set(x, DECK + 1, z, s(Blocks.NETHER_BRICKS));
                            if (Tower.merlon(x, z - c)) {
                                this.b.set(x, DECK + 2, z, s(Blocks.NETHER_BRICKS));
                            }
                        }
                    }
                }
            }
            for (int[] q : new int[][] {{3, 3}, {-3, 3}, {3, -3}, {-3, -3}}) {
                this.lantern(q[0], DECK + 2, c + q[1], false);
            }
            if (this.variant == GLOW) {
                this.b.set(0, DECK, c, s(Blocks.MAGMA_BLOCK));
                this.spawner(0, DECK + 1, c);
            } else {
                this.brazier(0, DECK + 1, c);
            }
            if (this.variant == STORE || this.variant == JAIL) {
                this.chest(3, DECK + 1, c + 1, Direction.WEST, false);
            }
        }
    }
}
