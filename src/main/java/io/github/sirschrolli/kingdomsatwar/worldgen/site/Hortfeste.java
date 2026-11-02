package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Die Hortfeste (an Stelle der Bastion): Die Borstlinge horten ihr Gold in einer Stufenburg – drei Absätze aus
 * Schwarzstein übereinander, jeder mit einem goldenen Fries und Zinnen, an den Ecken vier Türme mit Feuerschalen. Eine
 * Freitreppe führt vom Vorhof über alle Absätze bis auf die oberste Plattform; dort steht zwischen Feuerschalen der
 * goldene Keiler, das Götzenbild der Borstlinge, mit glühenden Augen und weißen Hauern. Die Feste steht frei in einer
 * weiten Höhlenkuppel auf einem Sockel, der bis auf den Grund (oder in den Lavasee) reicht.
 *
 * <p>Innen: unten die Tauschhalle mit dem Feuerbecken, Arkaden, Vorräten und – hinten – den Ställen der Glutkeiler; im
 * mittleren Absatz der Schlafsaal und die Waffenkammer; im obersten die Hortkammer mit Haufen aus Gold, Netheritschutt
 * in den Wänden, zwei Glutschleim-Spawnern und dem Hort. Treppen im Inneren verbinden die Absätze, Türen führen auf die
 * Terrassen. Borstlinge, Borstenhünen und Glutkeiler leben darin – und die Truhen haben die Beute der Bastion (in der
 * Hortkammer die Netherit-Vorlage).
 *
 * <p>Eigene Koordinaten: Mitte des Sockels bei (0, 0, 0); y = 0 ist die Oberfläche des Sockels (man steht auf y = 1).
 * Vorn (+z) liegen der Vorhof und die Freitreppe.
 */
final class Hortfeste {
    static final int FESTE = 0;
    /** Sockel: |x|, |z| ≤ P; vorn (Vorhof) bis {@link #FRONT} für |x| ≤ {@link #COURT}. */
    static final int P = 24;
    static final int COURT = 12;
    static final int FRONT = 42;
    /** Die drei Absätze: halbe Kante der Außenmauer, Boden und Dach (beides Blockhöhen). */
    static final int[] A = {19, 13, 8};
    static final int[] FLOOR = {0, 10, 19};
    static final int[] ROOF = {10, 19, 27};
    /** Die Freitreppe: Stufe k liegt bei z = FOOT − k in der Höhe 1 + k (k = 0 … 26). */
    static final int FOOT = 35;
    static final int STEPS = 27;
    /** Die Kuppel: Mitte bei z = 8, Halbmesser um 45. */
    static final int DOME_Z = 8;
    static final double DOME = 45.0;
    static final int DOME_HEIGHT = 62;
    static final int REACH = 57;
    static final int DEPTH = 36;
    private static final int[][] PROBES = {{0, 0}, {14, 0}, {-14, 0}, {0, 14}, {0, -14}, {10, 10}, {-10, 10}, {10, -10}, {-10, -10},
            {0, 26}};

    private Hortfeste() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int base = terrain.seaLevel() + 4;
        int bestX = chunk.getMiddleBlockX();
        int bestZ = chunk.getMiddleBlockZ();
        int best = Integer.MIN_VALUE;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int x = chunk.getMiddleBlockX() + i * 12;
                int z = chunk.getMiddleBlockZ() + j * 12;
                int score = openness(terrain, x, z, base) * 3 + ground(terrain, x, z, base) - Math.abs(i) - Math.abs(j);
                if (score > best) {
                    best = score;
                    bestX = x;
                    bestZ = z;
                }
            }
        }
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 9), 4)];
        BlockPos origin = new BlockPos(bestX, base, bestZ);
        SitePiece piece = piece(origin, rotation, seed);
        return Optional.of(new Structure.GenerationStub(origin, builder -> builder.addPiece(piece)));
    }

    /** Wie viel über dem Sockel frei ist (Luft oder Lava statt Fels). */
    private static int openness(SiteTerrain terrain, int x, int z, int base) {
        int score = 0;
        for (int[] p : PROBES) {
            for (int y = base + 3; y <= base + 42; y += 5) {
                if (!terrain.rock(x + p[0], y, z + p[1])) {
                    score++;
                }
            }
        }
        return score;
    }

    /** Fester Grund nicht weit unter dem Sockel spart Fundament: je Probe ein Punkt. */
    private static int ground(SiteTerrain terrain, int x, int z, int base) {
        int score = 0;
        for (int[] p : PROBES) {
            for (int y = base - 1; y >= base - 8; y--) {
                if (terrain.rock(x + p[0], y, z + p[1])) {
                    score++;
                    break;
                }
            }
        }
        return score;
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        return new SitePiece(SiteKind.HORTFESTE, FESTE, origin, rotation, seed, new int[0],
                SitePiece.box(origin, rotation, -REACH, -DEPTH - 2, DOME_Z - REACH, REACH, DOME_HEIGHT + 2, DOME_Z + REACH));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Feste(b, piece.seed()).build();
    }

    /** Der Halbmesser der Kuppel in Richtung {@code angle}: 41 bis 49 Blöcke. */
    static double domeRadius(long seed, double angle) {
        double p1 = (seed & 0xFFF) / 4096.0 * 2.0 * Math.PI;
        double p2 = ((seed >>> 12) & 0xFFF) / 4096.0 * 2.0 * Math.PI;
        return DOME + 2.5 * Math.sin(3.0 * angle + p1) + 1.5 * Math.sin(5.0 * angle + p2);
    }

    /** Abstand im Quadrat. */
    static int cheb(int x, int z) {
        return Math.max(Math.abs(x), Math.abs(z));
    }

    /** Höhe der Freitreppe bei z (0, wo keine ist). */
    static int stairY(int z) {
        int k = FOOT - z;
        return k >= 0 && k < STEPS ? 1 + k : 0;
    }

    private static final class Feste {
        private static final BlockState AIR = Blocks.AIR.defaultBlockState();

        private final VillageBuilder b;
        private final long seed;

        Feste(VillageBuilder b, long seed) {
            this.b = b;
            this.seed = seed;
        }

        static BlockState s(Block block) {
            return block.defaultBlockState();
        }

        void build() {
            this.clearing();
            this.foundation();
            this.plinth();
            this.court();
            for (int t = 0; t < 3; t++) {
                this.tier(t);
            }
            this.towers();
            this.ground();
            this.middle();
            this.vault();
            this.inner();
            this.stair();
            this.summit();
            this.garrison();
        }

        // =====================================================================================
        // Werkstoffe
        // =====================================================================================

        /** Mauerwerk: Ziegel aus poliertem Schwarzstein, rissig, roh, hier und da ein Goldkorn. */
        BlockState wall(int x, int y, int z) {
            float n = this.b.noise(x, y, z);
            return n < 0.55f ? s(Blocks.POLISHED_BLACKSTONE_BRICKS) : n < 0.72f ? s(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)
                    : n < 0.87f ? s(Blocks.BLACKSTONE) : n < 0.96f ? s(Blocks.POLISHED_BLACKSTONE) : s(Blocks.GILDED_BLACKSTONE);
        }

        /** Grobes Gestein für Fundament und Sockel. */
        BlockState rubble(int x, int y, int z) {
            float n = this.b.noise(x, y + 700, z);
            return n < 0.5f ? s(Blocks.BLACKSTONE) : n < 0.78f ? s(Blocks.BASALT) : n < 0.93f ? s(Blocks.POLISHED_BLACKSTONE_BRICKS)
                    : s(Blocks.MAGMA_BLOCK);
        }

        BlockState pillar(Block block) {
            return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        }

        void lantern(int x, int y, int z, boolean hanging) {
            this.b.set(x, y, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, hanging));
        }

        /** Eine Laterne an einer Kette unter der Decke {@code top} (die Borstlinge fürchten Seelenfeuer: nur gelbe Flammen). */
        void hang(int x, int top, int z, int chain) {
            for (int i = 0; i < chain; i++) {
                this.b.set(x, top - i, z, Blocks.IRON_CHAIN);
            }
            this.lantern(x, top - chain, z, true);
        }

        /** Eine Feuerschale: Sockel, Netherrack, Feuer. */
        void brazier(int x, int y, int z) {
            this.b.set(x, y, z, s(Blocks.CHISELED_POLISHED_BLACKSTONE));
            this.b.set(x, y + 1, z, s(Blocks.NETHERRACK));
            this.b.set(x, y + 2, z, s(Blocks.FIRE));
        }

        void chest(int x, int y, int z, Direction facing, ResourceKey<LootTable> loot) {
            this.b.chest(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), loot);
        }

        void barrel(int x, int y, int z, Direction facing) {
            this.b.set(x, y, z, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, facing));
        }

        /** Ein Spawner, aus dem Glutschleime kommen. */
        void spawner(int x, int y, int z) {
            BlockPos pos = this.b.world(x, y, z);
            if (!this.b.inside(pos)) {
                return;
            }
            this.b.level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_CLIENTS);
            if (this.b.level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
                spawner.setEntityId(ModEntities.MAGMA_CUBE.get(), RandomSource.create(pos.asLong()));
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

        // =====================================================================================
        // Gelände: Kuppel, Fundament, Sockel, Vorhof
        // =====================================================================================

        /** Die Höhlenkuppel über der Feste (der Boden bleibt, wie er ist: Netherrack, Seelensand oder Lava). */
        private void clearing() {
            for (int x = -REACH; x <= REACH; x++) {
                for (int z = DOME_Z - REACH; z <= DOME_Z + REACH; z++) {
                    if (!this.b.here(x, 1, z)) {
                        continue;
                    }
                    int dz = z - DOME_Z;
                    double r = Math.sqrt(x * x + dz * dz);
                    double edge = domeRadius(this.seed, Math.atan2(dz, x));
                    double rough = (this.b.noise(x, 0, z) - 0.5) * 1.8;
                    if (r > edge + rough) {
                        continue;
                    }
                    double k = Math.min(1.0, r / edge);
                    int top = (int) Math.round(DOME_HEIGHT * Math.sqrt(1.0 - k * k) + (this.b.noise(x, 1, z) - 0.5) * 3.0);
                    for (int y = 1; y <= top; y++) {
                        if (!this.b.get(x, y, z).isAir()) {
                            this.b.set(x, y, z, AIR);
                        }
                    }
                }
            }
        }

        /** Auf dem Sockel? (Rechteck hinten und seitlich, vorn der schmalere Vorhof.) */
        static boolean onPlinth(int x, int z) {
            return Math.abs(x) <= P && z >= -P && z <= P || Math.abs(x) <= COURT && z > P && z <= FRONT;
        }

        /** Unter dem Sockel bis auf festen Grund gemauert, am Rand als schräg abgestufter Fuß. */
        private void foundation() {
            for (int x = -P - 4; x <= P + 4; x++) {
                for (int z = -P - 4; z <= FRONT + 4; z++) {
                    if (!this.b.here(x, 0, z)) {
                        continue;
                    }
                    // Abstand nach außen zum Sockel (0: darunter); mit jeder Tiefe rückt der Fuß einen halben Block heraus
                    int out = outside(x, z);
                    if (out > 4) {
                        continue;
                    }
                    for (int y = -1; y >= -DEPTH; y--) {
                        if (out > 0 && -y < out * 2 + 4) {
                            continue;
                        }
                        BlockState old = this.b.get(x, y, z);
                        if (y < -4 && !old.isAir() && old.getFluidState().isEmpty() && !old.is(Blocks.MAGMA_BLOCK)) {
                            break;
                        }
                        this.b.set(x, y, z, this.rubble(x, y, z));
                    }
                }
            }
        }

        /** Wie weit (Chebyshev) eine Stelle außerhalb des Sockels liegt (0: auf dem Sockel). */
        static int outside(int x, int z) {
            int best = Integer.MAX_VALUE;
            // Hauptteil
            best = Math.min(best, Math.max(Math.max(Math.abs(x) - P, 0), Math.max(Math.max(-P - z, 0), Math.max(z - P, 0))));
            // Vorhof
            best = Math.min(best, Math.max(Math.max(Math.abs(x) - COURT, 0), Math.max(Math.max(P - z, 0), Math.max(z - FRONT, 0))));
            return best;
        }

        /** Die Oberfläche des Sockels, sein Rand mit Zinnen und das Band an seiner Seite. */
        private void plinth() {
            for (int x = -P; x <= P; x++) {
                for (int z = -P; z <= FRONT; z++) {
                    if (!onPlinth(x, z) || !this.b.here(x, 0, z)) {
                        continue;
                    }
                    boolean edge = !onPlinth(x + 1, z) || !onPlinth(x - 1, z) || !onPlinth(x, z + 1) || !onPlinth(x, z - 1);
                    // Seite: ein Band aus gemeißeltem Schwarzstein, darunter grobes Mauerwerk
                    for (int y = -4; y <= -1; y++) {
                        this.b.set(x, y, z, edge ? (y == -2 ? s(Blocks.CHISELED_POLISHED_BLACKSTONE) : this.wall(x, y, z)) : this.rubble(x, y, z));
                    }
                    // Oberfläche: Bahnen aus Ziegeln im Raster, dazwischen polierter Stein
                    boolean line = Math.floorMod(x, 6) == 0 || Math.floorMod(z, 6) == 0;
                    this.b.set(x, 0, z, edge ? s(Blocks.POLISHED_BLACKSTONE_BRICKS)
                            : line ? s(Blocks.POLISHED_BLACKSTONE_BRICKS) : s(Blocks.POLISHED_BLACKSTONE));
                    if (edge) {
                        // Brüstung: Mauer, alle vier Blöcke ein Pfosten mit Goldkorn
                        boolean post = Math.floorMod(x + z, 4) == 0;
                        this.b.set(x, 1, z, post ? s(Blocks.POLISHED_BLACKSTONE_BRICKS) : s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                        if (post) {
                            this.b.set(x, 2, z, s(Blocks.GILDED_BLACKSTONE));
                        }
                    }
                }
            }
        }

        /** Der Vorhof: ein Tor zwischen zwei Pylonen am vorderen Rand, Feuerschalen, die Wachstube. */
        private void court() {
            int z = FRONT;
            // die Lücke im Rand für das Tor
            for (int x = -3; x <= 3; x++) {
                this.b.set(x, 1, z, AIR);
                this.b.set(x, 2, z, AIR);
            }
            for (int side : new int[] {-1, 1}) {
                int cx = side * 6;
                // Pylon 3 × 3, zwölf hoch, nach oben schmaler
                for (int y = 1; y <= 12; y++) {
                    int r = y <= 8 ? 1 : 0;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (Math.abs(dx) > r || Math.abs(dz) > r) {
                                continue;
                            }
                            boolean corner = Math.abs(dx) == 1 && Math.abs(dz) == 1;
                            this.b.set(cx + dx, y, z - 1 + dz, corner ? this.pillar(Blocks.POLISHED_BASALT)
                                    : y == 4 || y == 8 ? s(Blocks.GILDED_BLACKSTONE) : this.wall(cx + dx, y, z - 1 + dz));
                        }
                    }
                }
                this.b.set(cx, 13, z - 1, s(Blocks.GOLD_BLOCK));
                this.brazier(cx, 14, z - 1);
                // der Torbogen zwischen den Pylonen
                for (int x = -4; x <= 4; x++) {
                    this.b.set(x, 7, z - 1, x == 0 ? s(Blocks.GOLD_BLOCK) : s(Blocks.CHISELED_POLISHED_BLACKSTONE));
                    this.b.set(x, 8, z - 1, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                }
                for (int x = -4; x <= 4; x += 2) {
                    this.b.set(x, 9, z - 1, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                }
                // Feuerschalen vor der Treppe
                this.brazier(side * 6, 1, FOOT + 3);
            }
            // die Wachstube am Tor: eine Bank, ein Lagerfeuer, eine Truhe
            this.b.set(-9, 1, FRONT - 3, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
            this.b.set(-10, 1, FRONT - 4, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.WEST, Half.BOTTOM));
            this.b.set(-8, 1, FRONT - 4, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.EAST, Half.BOTTOM));
            this.chest(-11, 1, FRONT - 2, Direction.EAST, BuiltInLootTables.BASTION_BRIDGE);
            this.barrel(-11, 1, FRONT - 1, Direction.UP);
            this.barrel(10, 1, FRONT - 2, Direction.UP);
            this.barrel(11, 1, FRONT - 3, Direction.NORTH);
        }

        // =====================================================================================
        // Die drei Absätze
        // =====================================================================================

        private void tier(int t) {
            int a = A[t];
            int fl = FLOOR[t];
            int rf = ROOF[t];
            for (int x = -a - 1; x <= a + 1; x++) {
                for (int z = -a - 1; z <= a + 1; z++) {
                    if (!this.b.here(x, fl + 1, z)) {
                        continue;
                    }
                    int d = cheb(x, z);
                    if (d == a + 1) {
                        // die Sockelstufe vor der Mauer
                        this.b.set(x, fl + 1, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                        continue;
                    }
                    boolean corner = Math.abs(x) == a && Math.abs(z) == a;
                    if (d == a) {
                        for (int y = fl + 1; y < rf; y++) {
                            BlockState state;
                            if (corner) {
                                state = this.pillar(Blocks.POLISHED_BASALT);
                            } else if (y == rf - 2) {
                                // der Fries: gemeißelte Platten und Gold im Wechsel
                                state = Math.floorMod(x + z, 3) == 0 ? s(Blocks.GILDED_BLACKSTONE) : s(Blocks.CHISELED_POLISHED_BLACKSTONE);
                            } else if (y == rf - 1) {
                                state = s(Blocks.POLISHED_BLACKSTONE_BRICKS);
                            } else {
                                state = this.wall(x, y, z);
                            }
                            this.b.set(x, y, z, state);
                        }
                    } else if (t > 0 || d < a) {
                        // innen: Luft (die Räume richten die Stockwerke ein)
                        for (int y = fl + 1; y < rf; y++) {
                            this.b.set(x, y, z, AIR);
                        }
                    }
                    if (d <= a) {
                        // das Dach – zugleich der Boden der Terrasse darüber
                        this.b.set(x, rf, z, d == a ? s(Blocks.POLISHED_BLACKSTONE_BRICKS) : s(Blocks.POLISHED_BLACKSTONE));
                    }
                    if (d == a && t < 2) {
                        // Zinnen am Rand der Terrasse; an den Ecken Gold
                        boolean merlon = Math.floorMod(x + z, 2) == 0;
                        this.b.set(x, rf + 1, z, merlon ? s(Blocks.POLISHED_BLACKSTONE_BRICKS) : s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                        if (corner) {
                            this.b.set(x, rf + 2, z, s(Blocks.GOLD_BLOCK));
                        } else if (merlon && Math.floorMod(x + z, 8) == 0) {
                            this.b.set(x, rf + 2, z, s(Blocks.GILDED_BLACKSTONE));
                        }
                    }
                }
            }
            this.windows(t);
            this.doors(t);
        }

        /** Hohe Fenster in den Mauern der beiden unteren Absätze, mit Sturz und Brüstung. */
        private void windows(int t) {
            if (t == 2) {
                return;
            }
            int a = A[t];
            int fl = FLOOR[t];
            for (int i = -a + 3; i <= a - 3; i++) {
                if (Math.floorMod(i, 6) != 3 || Math.abs(i) <= 3) {
                    continue;
                }
                for (int[] w : new int[][] {{i, a}, {i, -a}, {a, i}, {-a, i}}) {
                    if (w[1] == a && Math.abs(w[0]) <= 4) {
                        continue;                       // vorn liegt die Freitreppe
                    }
                    for (int y = fl + 3; y <= fl + 5; y++) {
                        this.b.set(w[0], y, w[1], AIR);
                    }
                    this.b.set(w[0], fl + 6, w[1], s(Blocks.CHISELED_POLISHED_BLACKSTONE));
                    this.b.set(w[0], fl + 2, w[1], s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                }
            }
        }

        /** Tore und Türen: unten an beiden Seiten und hinten große Tore, oben Türen auf die Terrassen. */
        private void doors(int t) {
            int a = A[t];
            int fl = FLOOR[t];
            if (t == 0) {
                for (int[] g : new int[][] {{a, 0}, {-a, 0}, {0, -a}}) {
                    boolean alongX = g[0] == 0;
                    for (int s = -1; s <= 1; s++) {
                        int x = alongX ? s : g[0];
                        int z = alongX ? g[1] : s;
                        for (int y = fl + 1; y <= fl + 4; y++) {
                            this.b.set(x, y, z, AIR);
                        }
                        this.b.set(x, fl + 5, z, s == 0 ? s(Blocks.GOLD_BLOCK) : s(Blocks.CHISELED_POLISHED_BLACKSTONE));
                    }
                    // Torpfeiler
                    for (int s : new int[] {-2, 2}) {
                        int x = alongX ? s : g[0];
                        int z = alongX ? g[1] : s;
                        for (int y = fl + 1; y <= fl + 5; y++) {
                            this.b.set(x, y, z, this.pillar(Blocks.POLISHED_BASALT));
                        }
                    }
                }
            } else if (t == 1) {
                for (int[] g : new int[][] {{a, 0}, {-a, 0}, {0, -a}}) {
                    for (int y = fl + 1; y <= fl + 3; y++) {
                        this.b.set(g[0], y, g[1], AIR);
                    }
                    this.b.set(g[0], fl + 4, g[1], s(Blocks.GILDED_BLACKSTONE));
                }
            }
        }

        /** Vier Ecktürme an den Ecken des untersten Absatzes, oben mit Feuerschale; eine Leiter von der Terrasse hinauf. */
        private void towers() {
            int a = A[0];
            int top = 15;
            for (int sx : new int[] {-1, 1}) {
                for (int sz : new int[] {-1, 1}) {
                    int cx = sx * a;
                    int cz = sz * a;
                    for (int dx = -3; dx <= 3; dx++) {
                        for (int dz = -3; dz <= 3; dz++) {
                            boolean corner = Math.abs(dx) == 3 && Math.abs(dz) == 3;
                            for (int y = 1; y <= top; y++) {
                                BlockState state = corner ? this.pillar(Blocks.POLISHED_BASALT)
                                        : y == 9 || y == top - 1 ? s(Blocks.CHISELED_POLISHED_BLACKSTONE) : this.wall(cx + dx, y, cz + dz);
                                this.b.set(cx + dx, y, cz + dz, state);
                            }
                            // Schießscharten nach außen
                            boolean outer = dx == 3 * sx && Math.abs(dz) == 0 || dz == 3 * sz && Math.abs(dx) == 0;
                            if (outer) {
                                this.b.set(cx + dx, 5, cz + dz, AIR);
                                this.b.set(cx + dx, 6, cz + dz, AIR);
                                this.b.set(cx + dx, 12, cz + dz, AIR);
                            }
                            // Zinnen
                            boolean rim = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                            if (rim) {
                                boolean merlon = Math.floorMod(dx + dz, 2) == 1 || corner;
                                this.b.set(cx + dx, top + 1, cz + dz, merlon ? s(Blocks.POLISHED_BLACKSTONE_BRICKS) : AIR);
                                if (corner) {
                                    this.b.set(cx + dx, top + 2, cz + dz, s(Blocks.GOLD_BLOCK));
                                }
                            } else {
                                this.b.set(cx + dx, top + 1, cz + dz, AIR);
                                this.b.set(cx + dx, top, cz + dz, s(Blocks.POLISHED_BLACKSTONE));
                            }
                        }
                    }
                    this.brazier(cx, top + 1, cz);
                    // Leiter von der Terrasse (y = 11) an der Innenseite des Turms hinauf
                    int lx = cx - 4 * sx;
                    int lz = cz;
                    Direction face = sx > 0 ? Direction.WEST : Direction.EAST;
                    for (int y = ROOF[0] + 1; y <= top; y++) {
                        this.b.set(lx + sx, y, lz, this.wall(lx + sx, y, lz));
                        this.b.set(lx, y, lz, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, face));
                    }
                    this.b.set(lx + sx, top + 1, lz, AIR);
                }
            }
        }

        // =====================================================================================
        // Innen
        // =====================================================================================

        /** Unten: Arkaden, die Tauschhalle mit dem Feuerbecken, Vorräte an den Seiten, hinten die Ställe der Glutkeiler. */
        private void ground() {
            int a = A[0] - 1;
            // Boden: konzentrische Bänder, in der Mitte Gold
            for (int x = -a; x <= a; x++) {
                for (int z = -a; z <= a; z++) {
                    int d = cheb(x, z);
                    BlockState floor = d <= 1 ? s(Blocks.GOLD_BLOCK) : d % 4 == 0 ? s(Blocks.POLISHED_BLACKSTONE_BRICKS)
                            : d > A[1] ? s(Blocks.POLISHED_BLACKSTONE) : Math.floorMod(x + z, 2) == 0 ? s(Blocks.POLISHED_BLACKSTONE)
                            : s(Blocks.BLACKSTONE);
                    this.b.set(x, 0, z, floor);
                }
            }
            this.arcade(A[1], FLOOR[0], ROOF[0]);
            // das Feuerbecken auf dem goldenen Fleck in der Mitte (vor der Treppe nach oben)
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean mid = dx == 0 && dz == 0;
                    this.b.set(dx, 1, 4 + dz, mid ? s(Blocks.NETHERRACK) : VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_SLAB,
                            net.minecraft.world.level.block.state.properties.SlabType.BOTTOM));
                    if (mid) {
                        this.b.set(0, 2, 4, s(Blocks.FIRE));
                    }
                }
            }
            for (int[] c : new int[][] {{2, 2}, {-2, 2}, {2, 6}, {-2, 6}}) {
                this.b.set(c[0], 1, c[1], s(Blocks.GOLD_BLOCK));
                this.lantern(c[0], 2, c[1], false);
            }
            // Laternen an Ketten in der Halle und in den Gängen
            for (int[] l : new int[][] {{6, 6}, {-6, 6}, {6, -6}, {-6, -6}, {16, 0}, {-16, 0}, {16, 10}, {-16, 10}, {0, 16}, {10, 16}, {-10, 16}}) {
                this.hang(l[0], ROOF[0] - 1, l[1], 2);
            }
            // Vorräte in den Seitengängen: Fässer an der Wand, Truhen, ein paar Goldbarren-Blöcke
            for (int side : new int[] {-1, 1}) {
                int x = side * a;
                Direction in = side > 0 ? Direction.WEST : Direction.EAST;
                for (int z = 4; z <= 12; z++) {
                    if (z == 8) {
                        this.chest(x, 1, z, in, BuiltInLootTables.BASTION_OTHER);
                        continue;
                    }
                    this.barrel(x, 1, z, in);
                    if (this.b.chance(x, 2, z, 0.5f)) {
                        this.barrel(x, 2, z, Direction.UP);
                    }
                }
                for (int z = -12; z <= -5; z++) {
                    if (z == -9) {
                        this.chest(x, 1, z, in, BuiltInLootTables.BASTION_OTHER);
                    } else if (this.b.chance(x, 1, z, 0.35f)) {
                        this.b.set(x, 1, z, this.b.chance(x, 3, z, 0.4f) ? s(Blocks.GOLD_BLOCK) : s(Blocks.GILDED_BLACKSTONE));
                    } else {
                        this.b.set(x, 1, z, s(Blocks.HAY_BLOCK));
                    }
                }
            }
            // vorn hinter der Mauer die Wache: Lagerfeuer und Bänke
            this.b.set(8, 1, a, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
            this.b.set(-8, 1, a, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
            for (int x : new int[] {-10, -6, 6, 10}) {
                this.b.set(x, 1, a - 1, VillageBuilder.stairs(Blocks.CRIMSON_STAIRS, Direction.SOUTH, Half.BOTTOM));
            }
            this.stables();
        }

        /** Hinten (z ≤ −14): Koppeln aus Karmesinzaun, Heu, Futter aus Netherwarzenblöcken, eine Truhe. */
        private void stables() {
            int a = A[0] - 1;
            int front = -A[1] - 1;
            for (int x = -a; x <= a; x++) {
                for (int z = -a; z <= front; z++) {
                    // Stroh auf dem Boden
                    this.b.set(x, 0, z, this.b.chance(x, 0, z, 0.55f) ? s(Blocks.SOUL_SOIL) : s(Blocks.CRIMSON_NYLIUM));
                }
            }
            // die Koppeln: Trennzäune alle sechs Blöcke, vorn Zaun mit Tor
            for (int x = -a; x <= a; x++) {
                boolean divider = Math.floorMod(x, 6) == 3;
                for (int z = -a; z <= front; z++) {
                    if (divider) {
                        this.b.set(x, 1, z, s(Blocks.CRIMSON_FENCE));
                    }
                }
                if (Math.abs(x) <= 1) {
                    continue;                           // der Gang zum hinteren Tor
                }
                boolean gate = Math.floorMod(x, 6) == 0;
                this.b.set(x, 1, front, gate ? Blocks.CRIMSON_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, Direction.SOUTH)
                        : s(Blocks.CRIMSON_FENCE));
            }
            // der Gang in der Mitte bleibt frei bis zum hinteren Tor
            for (int z = -a; z <= front; z++) {
                for (int x = -1; x <= 1; x++) {
                    this.b.set(x, 0, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    this.b.set(x, 1, z, AIR);
                }
                this.b.set(2, 1, z, s(Blocks.CRIMSON_FENCE));
                this.b.set(-2, 1, z, s(Blocks.CRIMSON_FENCE));
            }
            // Heu und Futter in jeder Koppel
            for (int x = -a + 1; x <= a - 1; x++) {
                if (Math.abs(x) <= 2 || Math.floorMod(x, 6) == 3) {
                    continue;
                }
                int z = -a;
                this.b.set(x, 1, z, Math.floorMod(x, 3) == 0 ? s(Blocks.NETHER_WART_BLOCK) : s(Blocks.HAY_BLOCK));
                if (this.b.chance(x, 2, z, 0.5f)) {
                    this.b.set(x, 2, z, s(Blocks.HAY_BLOCK));
                }
            }
            this.chest(16, 1, -a + 1, Direction.SOUTH, BuiltInLootTables.BASTION_HOGLIN_STABLE);
            this.hang(-9, ROOF[0] - 1, -16, 2);
            this.hang(9, ROOF[0] - 1, -16, 2);
        }

        /**
         * Arkaden unter der Mauer des nächsten Absatzes ({@code ring}): Pfeiler alle vier Blöcke, dazwischen Bögen –
         * so trägt jede Mauer sichtbar auf der darunter.
         */
        private void arcade(int ring, int fl, int rf) {
            for (int x = -ring; x <= ring; x++) {
                for (int z = -ring; z <= ring; z++) {
                    if (cheb(x, z) != ring) {
                        continue;
                    }
                    int along = Math.abs(x) == ring ? z : x;
                    boolean pier = Math.floorMod(along, 4) == 0 || Math.abs(x) == ring && Math.abs(z) == ring;
                    if (pier) {
                        for (int y = fl + 1; y < rf; y++) {
                            this.b.set(x, y, z, y == fl + 1 || y == rf - 1 ? s(Blocks.CHISELED_POLISHED_BLACKSTONE)
                                    : this.pillar(Blocks.POLISHED_BASALT));
                        }
                    } else {
                        // Bogen: oben Mauerwerk, darunter eine umgedrehte Treppe zum Pfeiler hin
                        for (int y = rf - 2; y < rf; y++) {
                            this.b.set(x, y, z, this.wall(x, y, z));
                        }
                        int m = Math.floorMod(along, 4);
                        if (m == 1 || m == 3) {
                            Direction toward;
                            if (Math.abs(x) == ring) {
                                toward = m == 1 ? Direction.NORTH : Direction.SOUTH;
                            } else {
                                toward = m == 1 ? Direction.WEST : Direction.EAST;
                            }
                            this.b.set(x, rf - 3, z, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, toward, Half.TOP));
                        }
                    }
                }
            }
        }

        /** Mitte: Arkaden unter der Hortkammer, außen der Schlafsaal, innen die Waffenkammer. */
        private void middle() {
            int fl = FLOOR[1];
            int rf = ROOF[1];
            int a = A[1] - 1;
            // Dielen aus Karmesinholz mit einem Rand aus Schwarzstein
            for (int x = -a; x <= a; x++) {
                for (int z = -a; z <= a; z++) {
                    this.b.set(x, fl, z, cheb(x, z) == a ? s(Blocks.POLISHED_BLACKSTONE) : s(Blocks.CRIMSON_PLANKS));
                }
            }
            this.arcade(A[2], fl, rf);
            // Schlafsaal: Strohlager entlang der Außenmauer (zwei Heuballen mit Teppich davor)
            for (int i = -a + 1; i <= a - 1; i += 3) {
                for (int[] w : new int[][] {{i, a}, {i, -a}, {a, i}, {-a, i}}) {
                    if (cheb(w[0], w[1]) != a || Math.abs(i) <= 1) {
                        continue;                           // Türen in der Mitte
                    }
                    this.b.set(w[0], fl + 1, w[1], s(Blocks.HAY_BLOCK));
                    int ix = w[0] - Integer.signum(w[0]) * (Math.abs(w[0]) == a ? 1 : 0);
                    int iz = w[1] - Integer.signum(w[1]) * (Math.abs(w[1]) == a ? 1 : 0);
                    this.b.set(ix, fl + 1, iz, s(Blocks.CARPET.red()));
                }
            }
            for (int[] l : new int[][] {{10, 10}, {-10, 10}, {10, -10}, {-10, -10}, {0, 10}, {10, 0}, {-10, 0}}) {
                this.hang(l[0], rf - 1, l[1], 1);
            }
            this.chest(-a + 1, fl + 1, a - 2, Direction.EAST, BuiltInLootTables.BASTION_OTHER);
            // Waffenkammer innerhalb der Arkaden: Schmiede, Amboss, Schleifstein, Fässer, eine Truhe
            int w = A[2] - 1;
            this.b.set(-w, fl + 1, -w + 2, s(Blocks.SMITHING_TABLE));
            this.b.set(-w, fl + 1, -w + 3, Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, Direction.NORTH));
            this.b.set(-w, fl + 1, -w + 4, Blocks.GRINDSTONE.defaultBlockState().setValue(GrindstoneBlock.FACE, AttachFace.FLOOR)
                    .setValue(GrindstoneBlock.FACING, Direction.EAST));
            for (int z = -w; z <= -w + 1; z++) {
                this.barrel(-w, fl + 1, z, Direction.EAST);
                this.barrel(-w, fl + 2, z, Direction.UP);
            }
            this.chest(-w + 1, fl + 1, w, Direction.NORTH, BuiltInLootTables.BASTION_OTHER);
            this.b.set(-w, fl + 1, w, s(Blocks.GOLD_BLOCK));
            this.lantern(-w, fl + 2, w, false);
        }

        /** Oben: die Hortkammer – Haufen aus Gold, Netheritschutt in den Wänden, zwei Spawner, der Hort auf einem Podest. */
        private void vault() {
            int fl = FLOOR[2];
            int rf = ROOF[2];
            int a = A[2] - 1;
            for (int x = -a; x <= a; x++) {
                for (int z = -a; z <= a; z++) {
                    int d = cheb(x, z);
                    this.b.set(x, fl, z, d % 3 == 0 ? s(Blocks.GILDED_BLACKSTONE) : s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                }
            }
            // Netheritschutt in den Wänden, nah am Boden
            for (int[] d : new int[][] {{A[2], -3, 1}, {-A[2], 2, 2}, {4, -A[2], 1}, {-5, -A[2], 3}, {A[2], 5, 2}}) {
                this.b.set(d[0], fl + d[2], d[1], s(Blocks.ANCIENT_DEBRIS));
            }
            // Goldhaufen in den Ecken: Gold, Rohgold und vergoldeter Schwarzstein
            for (int x = -a; x <= a; x++) {
                for (int z = -a; z <= a; z++) {
                    int edge = Math.min(a - Math.abs(x), a - Math.abs(z));
                    if (Math.abs(x) <= 2 || z >= 0 && Math.abs(x) <= 6 && z <= 3) {
                        continue;                           // Weg in der Mitte und um die Treppe
                    }
                    int h = (int) Math.round(2.6 - edge * 0.9 + (this.b.noise(x, 50, z) - 0.5) * 1.4);
                    for (int y = 1; y <= Math.min(h, 3); y++) {
                        float n = this.b.noise(x, fl + y, z);
                        this.b.set(x, fl + y, z, n < 0.38f ? s(Blocks.GOLD_BLOCK) : n < 0.6f ? s(Blocks.RAW_GOLD_BLOCK)
                                : s(Blocks.GILDED_BLACKSTONE));
                    }
                }
            }
            // der Hort auf einem Podest hinten in der Mitte
            for (int x = -2; x <= 2; x++) {
                for (int z = -a; z <= -a + 2; z++) {
                    this.b.set(x, fl + 1, z, Math.abs(x) == 2 ? s(Blocks.GOLD_BLOCK) : s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    this.b.set(x, fl + 2, z, AIR);
                }
                this.b.set(x, fl + 1, -a + 3, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
            }
            this.chest(0, fl + 2, -a + 1, Direction.SOUTH, BuiltInLootTables.BASTION_TREASURE);
            this.chest(-a + 1, fl + 1, 3, Direction.EAST, BuiltInLootTables.BASTION_OTHER);
            this.b.set(-a + 1, fl + 2, 3, AIR);
            this.lantern(2, fl + 2, -a, false);
            this.lantern(-2, fl + 2, -a, false);
            // zwei Spawner in Käfigen aus Gittern auf Gold
            for (int sx : new int[] {-1, 1}) {
                int x = sx * (a - 1);
                int z = -a + 1;
                this.b.set(x, fl + 1, z, s(Blocks.GOLD_BLOCK));
                this.spawner(x, fl + 2, z);
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0 || cheb(x + dx, z + dz) > a) {
                            continue;
                        }
                        for (int y = fl + 1; y <= fl + 3; y++) {
                            this.b.set(x + dx, y, z + dz, s(Blocks.IRON_BARS));
                        }
                    }
                }
                this.b.set(x, fl + 3, z, s(Blocks.IRON_BARS));
            }
            for (int[] l : new int[][] {{3, 3}, {-3, 3}, {3, -3}, {-3, -3}}) {
                this.hang(l[0], rf - 1, l[1], 1);
            }
        }

        /** Die Treppen im Inneren: von der Halle hinauf in den Schlafsaal und weiter in die Hortkammer. */
        private void inner() {
            // unten → Mitte: x −1 … 1, von z = −2 (Höhe 1) nach Norden bis z = −11 (Höhe 10)
            for (int k = 0; k <= 9; k++) {
                int z = -2 - k;
                int y = 1 + k;
                for (int x = -1; x <= 1; x++) {
                    this.b.set(x, y, z, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
                    for (int f = 1; f < y; f++) {
                        this.b.set(x, f, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    }
                    for (int h = y + 1; h <= y + 3 && h <= ROOF[0] + 2; h++) {
                        if (h >= ROOF[0]) {
                            this.b.set(x, h, z, AIR);
                        }
                    }
                }
                // Geländer an der Treppe in der Halle
                this.b.set(2, y + 1, z, k < 9 ? s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL) : AIR);
                this.b.set(-2, y + 1, z, k < 9 ? s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL) : AIR);
                this.b.set(2, y, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                this.b.set(-2, y, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                for (int f = 1; f < y; f++) {
                    this.b.set(2, f, z, this.wall(2, f, z));
                    this.b.set(-2, f, z, this.wall(-2, f, z));
                }
            }
            // oben am Treppenloch ein Geländer
            for (int z = -10; z <= -8; z++) {
                this.b.set(2, FLOOR[1] + 1, z, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                this.b.set(-2, FLOOR[1] + 1, z, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            }
            for (int x = -2; x <= 2; x++) {
                this.b.set(x, FLOOR[1] + 1, -7, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            }
            // Mitte → Hortkammer: x 3 … 5, von z = −5 (Höhe 11) nach Süden bis z = 3 (Höhe 19)
            for (int k = 0; k <= 8; k++) {
                int z = -5 + k;
                int y = FLOOR[1] + 1 + k;
                for (int x = 3; x <= 5; x++) {
                    this.b.set(x, y, z, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.SOUTH, Half.BOTTOM));
                    for (int f = FLOOR[1] + 1; f < y; f++) {
                        this.b.set(x, f, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    }
                    for (int h = y + 1; h <= y + 3; h++) {
                        if (h >= FLOOR[2] && h < ROOF[2]) {
                            this.b.set(x, h, z, AIR);
                        } else if (h < FLOOR[2]) {
                            this.b.set(x, h, z, AIR);
                        }
                    }
                }
            }
            for (int z = 0; z <= 2; z++) {
                this.b.set(2, FLOOR[2] + 1, z, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                this.b.set(6, FLOOR[2] + 1, z, s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            }
        }

        /** Die Freitreppe vom Vorhof bis auf die oberste Plattform, mit Brüstung, Pfosten und Laternen. */
        private void stair() {
            for (int k = 0; k < STEPS; k++) {
                int z = FOOT - k;
                int y = 1 + k;
                for (int x = -4; x <= 4; x++) {
                    if (!this.b.here(x, y, z)) {
                        continue;
                    }
                    int ax = Math.abs(x);
                    // der Treppenkörper bis hinab auf Sockel bzw. Terrasse (unter den Absätzen liegen ihre Mauern)
                    for (int f = 1; f < y + (ax == 4 ? -1 : 0); f++) {
                        if (z <= A[0] && f <= ROOF[0] || z <= A[1] && f <= ROOF[1]) {
                            continue;
                        }
                        this.b.set(x, f, z, ax == 4 && Math.floorMod(f - y, 4) == 0 ? s(Blocks.GILDED_BLACKSTONE) : this.wall(x, f, z));
                    }
                    if (ax <= 2) {
                        this.b.set(x, y, z, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
                        for (int h = y + 1; h <= y + 3; h++) {
                            this.b.set(x, h, z, AIR);
                        }
                    } else if (ax == 3) {
                        this.b.set(x, y, z, s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                        boolean post = k % 5 == 0;
                        this.b.set(x, y + 1, z, post ? s(Blocks.CHISELED_POLISHED_BLACKSTONE) : s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                        if (post && k > 0) {
                            this.lantern(x, y + 2, z, false);
                        }
                    } else if (k > 0) {
                        this.b.set(x, y - 1, z, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, x > 0 ? Direction.WEST : Direction.EAST,
                                Half.BOTTOM));
                    }
                }
            }
        }

        /** Die oberste Plattform: Brüstung, Feuerschalen an den Ecken, in der Mitte der goldene Keiler auf seinem Sockel. */
        private void summit() {
            int top = ROOF[2];
            int a = A[2];
            for (int x = -a; x <= a; x++) {
                for (int z = -a; z <= a; z++) {
                    if (cheb(x, z) != a || z == a && Math.abs(x) <= 2) {
                        continue;
                    }
                    boolean post = Math.abs(x) == a && Math.abs(z) == a || Math.floorMod(x + z, 4) == 0;
                    this.b.set(x, top + 1, z, post ? s(Blocks.POLISHED_BLACKSTONE_BRICKS) : s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                }
            }
            for (int[] c : new int[][] {{6, 6}, {-6, 6}, {6, -6}, {-6, -6}}) {
                this.brazier(c[0], top + 1, c[1]);
            }
            // der Sockel des Götzenbilds
            for (int x = -3; x <= 3; x++) {
                for (int z = -5; z <= 4; z++) {
                    boolean rim = Math.abs(x) == 3 || z == -5 || z == 4;
                    for (int y = top + 1; y <= top + 2; y++) {
                        this.b.set(x, y, z, rim && y == top + 2 ? s(Blocks.CHISELED_POLISHED_BLACKSTONE) : s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    }
                }
            }
            for (int[] c : new int[][] {{3, -5}, {-3, -5}, {3, 4}, {-3, 4}}) {
                this.b.set(c[0], top + 3, c[1], s(Blocks.GOLD_BLOCK));
            }
            this.boar(top + 3);
        }

        /**
         * Der goldene Keiler (Blick nach vorn, +z): ein Rumpf aus Gold mit Sprenkeln aus Rohgold, vier Beine, ein schwerer
         * Kopf mit Rüssel, weißen Hauern und glühenden Augen, Ohren und ein Kamm aus dunklen Borsten auf dem Rücken.
         */
        private void boar(int y0) {
            // Rumpf: ein Ellipsoid
            double cy = y0 + 2.6;
            double cz = -0.5;
            for (int x = -3; x <= 3; x++) {
                for (int y = y0 + 1; y <= y0 + 5; y++) {
                    for (int z = -5; z <= 4; z++) {
                        double ex = x / 2.4;
                        double ey = (y - cy) / 2.0;
                        double ez = (z - cz) / 4.0;
                        if (ex * ex + ey * ey + ez * ez <= 1.0) {
                            this.b.set(x, y, z, this.b.chance(x, y, z, 0.16f) ? s(Blocks.RAW_GOLD_BLOCK) : s(Blocks.GOLD_BLOCK));
                        }
                    }
                }
            }
            // Beine
            for (int[] l : new int[][] {{1, 2}, {-1, 2}, {1, -3}, {-1, -3}}) {
                this.b.set(l[0], y0, l[1], s(Blocks.GOLD_BLOCK));
            }
            // Kopf, Rüssel, Hauer, Augen, Ohren
            for (int x = -1; x <= 1; x++) {
                for (int y = y0 + 1; y <= y0 + 3; y++) {
                    for (int z = 4; z <= 5; z++) {
                        this.b.set(x, y, z, s(Blocks.GOLD_BLOCK));
                    }
                }
                this.b.set(x, y0 + 1, 6, s(Blocks.RAW_GOLD_BLOCK));
                this.b.set(x, y0 + 2, 6, s(Blocks.GOLD_BLOCK));
            }
            for (int sx : new int[] {-1, 1}) {
                this.b.set(sx * 2, y0 + 1, 6, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                this.b.set(sx * 2, y0 + 2, 6, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                this.b.set(sx * 2, y0 + 3, 7, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
                this.b.set(sx, y0 + 3, 6, s(Blocks.SHROOMLIGHT));
                this.b.set(sx, y0 + 4, 4, s(Blocks.GOLD_BLOCK));
                this.b.set(sx * 2, y0 + 4, 3, s(Blocks.GOLD_BLOCK));
            }
            // der Kamm aus Borsten
            for (int z = -3; z <= 3; z++) {
                this.b.set(0, y0 + 5, z, s(Blocks.BLACKSTONE_WALL));
                if (z % 2 == 0) {
                    this.b.set(0, y0 + 6, z, s(Blocks.BLACKSTONE_WALL));
                }
            }
            // der Schwanz
            this.b.set(0, y0 + 3, -5, s(Blocks.GOLD_BLOCK));
            this.b.set(0, y0 + 2, -6, s(Blocks.IRON_CHAIN));
        }

        // =====================================================================================
        // Die Bewohner
        // =====================================================================================

        private void garrison() {
            // Borstlinge in der Halle, auf den Terrassen und im Schlafsaal
            for (int[] p : new int[][] {{6, 1, 9}, {-7, 1, 8}, {4, 1, -1}, {-14, 1, 2}, {14, 1, -3}, {0, 1, 14}, {-9, 11, 5}, {9, 11, -6},
                    {0, 11, 9}, {16, 11, 2}, {-16, 11, -4}}) {
                this.spawn(ModEntities.PIGLIN.get(), p[0], p[1], p[2]);
            }
            // Borstenhünen bewachen Schlafsaal, Hortkammer und den Keiler
            for (int[] p : new int[][] {{-4, 11, -9}, {5, 11, 8}, {-4, 20, -2}, {4, 20, -4}, {0, ROOF[2] + 3, 7}}) {
                this.spawn(ModEntities.PIGLIN_BRUTE.get(), p[0], p[1], p[2]);
            }
            // Glutkeiler in den Koppeln
            for (int[] p : new int[][] {{-12, 1, -16}, {-6, 1, -16}, {6, 1, -17}, {12, 1, -15}}) {
                this.spawn(ModEntities.HOGLIN.get(), p[0], p[1], p[2]);
            }
        }
    }

    /** Alle Beute-Tabellen der Feste (für Tests). */
    static List<ResourceKey<LootTable>> lootTables() {
        return List.of(BuiltInLootTables.BASTION_TREASURE, BuiltInLootTables.BASTION_OTHER, BuiltInLootTables.BASTION_HOGLIN_STABLE,
                BuiltInLootTables.BASTION_BRIDGE);
    }
}
