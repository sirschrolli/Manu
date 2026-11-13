package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Die Karawanserei: Mitten in der Wüste steht die Ruine einer Herberge der alten Handelsstraßen – ein Geviert aus
 * Sandstein mit Ecktürmen und einem großen Tor, innen ein Hof mit Laubengang aus Spitzbögen, dahinter die Kammern der
 * Händler, das Dach zum Teil eingestürzt, überall Flugsand. Im Hof steht ein ausgetrockneter Brunnen; wer an der Leiter
 * hinabsteigt, findet unten den Schatzkeller der Karawanenmeister – und ihre Falle. In einer Ecke des Hofs haben
 * Schatzsucher zu graben begonnen (verdächtiger Sand mit Scherben).
 *
 * <p>Eigene Koordinaten: Mitte des Hofs bei (0, 0, 0), y = 0 ist der Boden, das Tor liegt im Süden (z = 11).
 */
final class Caravanserai {
    static final int HALF = 11;

    private Caravanserai() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int[] span = terrain.span(x, z, HALF, 5);
        if (terrain.water(x, z) || span[1] - span[0] > 6) {
            return Optional.empty();
        }
        // auf der Höhe der Mitte, eher etwas tiefer (Dünen ringsum werden angeschnitten)
        int y = (span[0] + span[1] + 1) / 2;
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 7), 4)];
        BlockPos origin = new BlockPos(x, y, z);
        BoundingBox box = new BoundingBox(x - HALF - 1, y - 9, z - HALF - 1, x + HALF + 1, y + 12, z + HALF + 1);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.CARAVANSERAI, 0, origin, rotation, seed, new int[0], box))));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Inn(b).build();
    }

    private static final class Inn {
        private final VillageBuilder b;

        Inn(VillageBuilder b) {
            this.b = b;
        }

        BlockState wall(int x, int y, int z) {
            float n = this.b.noise(x, y, z);
            if (y == 1 || y == 5) {
                return Blocks.CUT_SANDSTONE.defaultBlockState();
            }
            if (n < 0.62f) {
                return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            }
            if (n < 0.86f) {
                return Blocks.SANDSTONE.defaultBlockState();
            }
            return Blocks.CUT_SANDSTONE.defaultBlockState();
        }

        int ring(int x, int z) {
            return Math.max(Math.abs(x), Math.abs(z));
        }

        /** Eingestürzt? (eine Ecke des Gevierts und einzelne Stellen im Dach) */
        boolean ruined(int x, int y, int z) {
            boolean corner = x >= 6 && z <= -6 && y >= 3 + (int) (this.b.noise(x, 3, z) * 3);
            return corner || y >= 5 && this.b.chance(x, 5, z, 0.08f);
        }

        void build() {
            this.ground();
            this.walls();
            this.arcade();
            this.towers();
            this.gate();
            this.courtyard();
            this.cellar();
            this.rooms();
            this.sand();
        }

        /** Fundament bis zum Boden der Wüste, darüber alles frei (Dünen werden angeschnitten). */
        void ground() {
            for (int x = -HALF; x <= HALF; x++) {
                for (int z = -HALF; z <= HALF; z++) {
                    for (int y = -1; y >= -8; y--) {
                        BlockState s = this.b.get(x, y, z);
                        if (s.isSolidRender() && y < -1) {
                            break;
                        }
                        this.b.set(x, y, z, Blocks.SANDSTONE.defaultBlockState());
                    }
                    boolean court = this.ring(x, z) <= 4;
                    this.b.set(x, 0, z, court ? (this.b.chance(x, 0, z, 0.7f) ? Blocks.SAND.defaultBlockState()
                            : Blocks.SMOOTH_SANDSTONE.defaultBlockState())
                            : (Math.floorMod(x + z, 2) == 0 ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : Blocks.CUT_SANDSTONE.defaultBlockState()));
                    for (int y = 1; y <= 11; y++) {
                        this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        /** Außenmauer (mit Zinnen) und die Mauer zwischen Laubengang und Kammern, Dach über Kammern und Gang. */
        void walls() {
            for (int x = -HALF; x <= HALF; x++) {
                for (int z = -HALF; z <= HALF; z++) {
                    int ring = this.ring(x, z);
                    if (ring == HALF) {
                        for (int y = 1; y <= 6; y++) {
                            if (!this.ruined(x, y, z)) {
                                this.b.set(x, y, z, this.wall(x, y, z));
                            }
                        }
                        if (!this.ruined(x, 7, z) && Math.floorMod(x + z, 2) == 0) {
                            this.b.set(x, 7, z, Blocks.SANDSTONE_WALL.defaultBlockState());
                        }
                        // schmale Fenster in die Kammern
                        if ((Math.floorMod(x, 4) == 2 && Math.abs(z) == HALF || Math.floorMod(z, 4) == 2 && Math.abs(x) == HALF) && ring == HALF
                                && Math.abs(x) < 9 | Math.abs(z) < 9) {
                            this.b.set(x, 3, z, Blocks.AIR.defaultBlockState());
                        }
                    } else if (ring == 7) {
                        for (int y = 1; y <= 4; y++) {
                            this.b.set(x, y, z, this.wall(x, y, z));
                        }
                    }
                    if (ring >= 5 && ring <= HALF && !this.ruined(x, 5, z)) {
                        this.b.set(x, 5, z, ring == 5 ? Blocks.CUT_SANDSTONE.defaultBlockState() : Blocks.SMOOTH_SANDSTONE.defaultBlockState());
                    }
                    if (ring == 5 && !this.ruined(x, 6, z)) {
                        this.b.set(x, 6, z, Math.floorMod(x + z, 2) == 0 ? Blocks.SANDSTONE_WALL.defaultBlockState()
                                : Blocks.SMOOTH_SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
                    }
                }
            }
            // Türen von den Kammern zum Laubengang (in der Mitte jeder Kammer)
            for (Direction d : Direction.Plane.HORIZONTAL) {
                for (int s : new int[] {-6, 0, 6}) {
                    int x = d.getStepX() * 7 + (d.getAxis() == Direction.Axis.Z ? s : 0);
                    int z = d.getStepZ() * 7 + (d.getAxis() == Direction.Axis.X ? s : 0);
                    for (int y = 1; y <= 3; y++) {
                        this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                    this.b.set(x, 4, z, Blocks.CHISELED_SANDSTONE.defaultBlockState());
                }
            }
            // Trennwände zwischen den Kammern
            for (Direction d : Direction.Plane.HORIZONTAL) {
                for (int s : new int[] {-3, 3}) {
                    for (int r = 8; r <= 10; r++) {
                        int x = d.getStepX() * r + (d.getAxis() == Direction.Axis.Z ? s : 0);
                        int z = d.getStepZ() * r + (d.getAxis() == Direction.Axis.X ? s : 0);
                        for (int y = 1; y <= 4; y++) {
                            this.b.set(x, y, z, this.wall(x, y, z));
                        }
                    }
                }
            }
        }

        /** Laubengang: Spitzbögen auf Pfeilern rund um den Hof (Pfeiler bei −5, −2, 2, 5; dazwischen Bögen). */
        void arcade() {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                Direction along = side.getClockWise();
                for (int a = -5; a <= 5; a++) {
                    int x = side.getStepX() * 5 + along.getStepX() * a;
                    int z = side.getStepZ() * 5 + along.getStepZ() * a;
                    boolean pillar = a == -5 || a == -2 || a == 2 || a == 5;
                    if (pillar) {
                        for (int y = 1; y <= 4; y++) {
                            this.b.set(x, y, z, y == 1 ? Blocks.CUT_SANDSTONE.defaultBlockState() : y == 4
                                    ? Blocks.CHISELED_SANDSTONE.defaultBlockState() : Blocks.SMOOTH_SANDSTONE.defaultBlockState());
                        }
                        continue;
                    }
                    this.b.set(x, 4, z, Blocks.CUT_SANDSTONE.defaultBlockState());
                    if (a == 0) {
                        continue;   // Scheitel des mittleren Bogens
                    }
                    // Kämpfer: zum nächsten Pfeiler hin geneigt
                    boolean towardsNegative = a == -4 || a == -1 || a == 3;
                    Direction toPillar = towardsNegative ? along.getOpposite() : along;
                    this.b.set(x, 3, z, VillageBuilder.stairs(Blocks.SMOOTH_SANDSTONE_STAIRS, toPillar, Half.TOP));
                }
            }
        }

        /** Vier Ecktürme mit Zinnen; der nordöstliche ist zur Hälfte eingestürzt. */
        void towers() {
            for (int[] c : new int[][] {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
                boolean broken = c[0] > 0 && c[1] < 0;
                int top = broken ? 6 : 9;
                for (int dx = 0; dx <= 2; dx++) {
                    for (int dz = 0; dz <= 2; dz++) {
                        int x = c[0] * (HALF - dx);
                        int z = c[1] * (HALF - dz);
                        for (int y = 1; y <= top; y++) {
                            boolean shell = dx == 0 || dz == 0 || dx == 2 || dz == 2;
                            if (shell && !(broken && y > 4 && this.b.chance(x, y, z, 0.5f))) {
                                this.b.set(x, y, z, y == top ? Blocks.CHISELED_SANDSTONE.defaultBlockState() : this.wall(x, y, z));
                            }
                        }
                        if (!broken && (dx == 0 || dz == 0 || dx == 2 || dz == 2) && Math.floorMod(dx + dz, 2) == 0) {
                            this.b.set(x, top + 1, z, Blocks.SANDSTONE_WALL.defaultBlockState());
                        }
                        if (!broken) {
                            this.b.set(x, top, z, Blocks.SMOOTH_SANDSTONE.defaultBlockState());
                        }
                    }
                }
            }
        }

        /** Das große Tor im Süden: ein hoher Spitzbogen, dahinter ein Durchgang zum Hof, an der Seite Laternen. */
        void gate() {
            for (int z = 7; z <= HALF; z++) {
                for (int x = -2; x <= 2; x++) {
                    for (int y = 1; y <= 4; y++) {
                        this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
                for (int x : new int[] {-3, 3}) {
                    for (int y = 1; y <= 4; y++) {
                        this.b.set(x, y, z, this.wall(x, y, z));
                    }
                }
            }
            for (int x = -3; x <= 3; x++) {
                for (int y = 5; y <= 8; y++) {
                    this.b.set(x, y, HALF, y == 8 && Math.floorMod(x, 2) == 1 ? Blocks.AIR.defaultBlockState()
                            : y == 7 ? Blocks.CHISELED_SANDSTONE.defaultBlockState() : this.wall(x, y, HALF));
                }
            }
            this.b.set(-2, 4, HALF, VillageBuilder.stairs(Blocks.SMOOTH_SANDSTONE_STAIRS, Direction.EAST, Half.TOP));
            this.b.set(2, 4, HALF, VillageBuilder.stairs(Blocks.SMOOTH_SANDSTONE_STAIRS, Direction.WEST, Half.TOP));
            this.b.set(-3, 3, HALF + 1, Blocks.WALL_TORCH.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, Direction.SOUTH));
            this.b.set(3, 3, HALF + 1, Blocks.WALL_TORCH.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, Direction.SOUTH));
            // die Reste eines Torflügels
            this.b.set(-2, 1, HALF, Blocks.SPRUCE_TRAPDOOR.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.TrapDoorBlock.FACING, Direction.NORTH)
                    .setValue(net.minecraft.world.level.block.TrapDoorBlock.OPEN, true));
        }

        /**
         * Der Hof: ein ausgetrockneter Brunnen mit Haspel (an dessen Leiter der Weg in den Keller führt), ein paar Reste des
         * Markts und in der Nordwestecke die Grube der Schatzsucher mit verdächtigem Sand.
         */
        void courtyard() {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                        this.b.set(x, 1, z, Math.abs(x) == 2 && Math.abs(z) == 2 ? Blocks.CHISELED_SANDSTONE.defaultBlockState()
                                : Blocks.SMOOTH_SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
                        this.b.set(x, 0, z, Blocks.CUT_SANDSTONE.defaultBlockState());
                    }
                }
            }
            // Haspel über dem Brunnen
            for (int y = 2; y <= 4; y++) {
                this.b.set(-2, y, 0, Blocks.SPRUCE_FENCE);
                this.b.set(2, y, 0, Blocks.SPRUCE_FENCE);
            }
            for (int x = -2; x <= 2; x++) {
                this.b.set(x, 5, 0, Blocks.SPRUCE_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                        Direction.Axis.X));
            }
            this.b.set(0, 4, 0, Blocks.IRON_CHAIN);
            // Markt: Teppiche, Fässer, Krüge
            this.b.set(3, 1, 3, Blocks.CARPET.pick(DyeColor.RED));
            this.b.set(4, 1, 3, Blocks.CARPET.pick(DyeColor.ORANGE));
            this.b.set(3, 1, 4, Blocks.CARPET.pick(DyeColor.ORANGE));
            this.b.set(4, 1, 4, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.set(-4, 1, 4, Blocks.DECORATED_POT);
            this.b.set(-4, 1, 3, Blocks.DECORATED_POT);
            this.b.set(4, 1, -1, Blocks.DEAD_BUSH);
            this.b.set(-3, 1, 1, Blocks.DEAD_BUSH);
            // Grube der Schatzsucher (Nordwestecke)
            for (int x = -4; x <= -2; x++) {
                for (int z = -4; z <= -3; z++) {
                    this.b.set(x, 0, z, Blocks.AIR.defaultBlockState());
                    this.brushable(x, -1, z, x == -3 || z == -4, BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY);
                }
            }
            this.b.set(-4, 1, -2, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.set(-5 + 1, 1, -1, Blocks.LANTERN);
        }

        void brushable(int x, int y, int z, boolean suspicious, ResourceKey<LootTable> loot) {
            if (!suspicious) {
                this.b.set(x, y, z, Blocks.SAND.defaultBlockState());
                return;
            }
            BlockPos pos = this.b.world(x, y, z);
            if (!this.b.inside(pos)) {
                return;
            }
            this.b.set(x, y, z, Blocks.SUSPICIOUS_SAND.defaultBlockState());
            if (this.b.level.getBlockEntity(pos) instanceof BrushableBlockEntity brushable) {
                brushable.setLootTable(loot, pos.asLong());
            }
        }

        /**
         * Der Schatzkeller unter dem Brunnen: Leiter hinab, ein Gang nach Süden, eine Kammer mit Säulen, Krügen und zwei
         * Truhen der Karawanenmeister. In der Mitte liegt eine Druckplatte – darunter Sprengstoff.
         */
        void cellar() {
            // der Brunnenschacht (unten auf Höhe des Kellerbodens)
            for (int y = -7; y <= 0; y++) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        this.b.set(x, y, z, y == -7 ? Blocks.SAND.defaultBlockState() : Blocks.AIR.defaultBlockState());
                    }
                }
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        if ((Math.abs(x) == 2 || Math.abs(z) == 2) && y < 0) {
                            this.b.set(x, y, z, Blocks.SANDSTONE.defaultBlockState());
                        }
                    }
                }
                if (y > -7) {
                    this.b.set(0, y, -1, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
                }
            }
            this.b.set(0, 1, -1, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
            this.b.set(0, 1, -2, Blocks.CUT_SANDSTONE.defaultBlockState());
            // Kammer südlich des Schachts
            for (int x = -4; x <= 4; x++) {
                for (int z = 2; z <= 8; z++) {
                    for (int y = -7; y <= -1; y++) {
                        boolean shell = Math.abs(x) == 4 || z == 8 || y == -7 || y == -1;
                        this.b.set(x, y, z, shell ? (y == -7 ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : this.wall(x, y + 9, z))
                                : Blocks.AIR.defaultBlockState());
                    }
                }
            }
            // Boden aus Ziegeln, Säulen, Krüge, Kerzen
            for (int x = -3; x <= 3; x++) {
                for (int z = 3; z <= 7; z++) {
                    this.b.set(x, -7, z, Math.floorMod(x + z, 2) == 0 ? Blocks.CUT_SANDSTONE.defaultBlockState()
                            : Blocks.TERRACOTTA.defaultBlockState());
                }
            }
            for (int[] c : new int[][] {{-2, 4}, {2, 4}, {-2, 6}, {2, 6}}) {
                for (int y = -6; y <= -2; y++) {
                    this.b.set(c[0], y, c[1], y == -6 || y == -2 ? Blocks.CHISELED_SANDSTONE.defaultBlockState()
                            : Blocks.SMOOTH_SANDSTONE.defaultBlockState());
                }
            }
            this.b.chest(-1, -6, 7, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH),
                    ModLootTables.SITE_CARAVANSERAI_TREASURE);
            this.b.chest(1, -6, 7, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH),
                    ModLootTables.SITE_CARAVANSERAI_TREASURE);
            this.b.set(-3, -6, 7, Blocks.DECORATED_POT);
            this.b.set(3, -6, 7, Blocks.DECORATED_POT);
            this.b.set(-3, -6, 3, Blocks.DECORATED_POT);
            this.b.set(0, -6, 7, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 3));
            this.b.set(0, -2, 5, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            // die Falle: Druckplatte über Sprengstoff
            this.b.set(0, -6, 5, Blocks.STONE_PRESSURE_PLATE);
            for (int x = -1; x <= 1; x++) {
                for (int z = 4; z <= 6; z++) {
                    this.b.set(x, -8, z, Blocks.TNT);
                }
            }
            this.b.set(0, -8, 5, Blocks.TNT);
        }

        /** Die Kammern der Händler: Betten aus Teppich und Heu, Webstuhl, Truhe eines Reisenden. */
        void rooms() {
            this.b.set(8, 1, 5, Blocks.HAY_BLOCK);
            this.b.set(9, 1, 5, Blocks.CARPET.pick(DyeColor.RED));
            this.b.set(9, 1, 4, Blocks.CARPET.pick(DyeColor.RED));
            this.b.set(10, 1, 6, Blocks.LOOM.defaultBlockState().setValue(net.minecraft.world.level.block.LoomBlock.FACING, Direction.WEST));
            this.b.chest(10, 1, 4, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST),
                    ModLootTables.SITE_CARAVANSERAI_TRAVELER);
            this.b.set(-9, 1, -5, Blocks.CARPET.pick(DyeColor.ORANGE));
            this.b.set(-9, 1, -4, Blocks.CARPET.pick(DyeColor.ORANGE));
            this.b.set(-10, 1, -6, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.set(-10, 2, -6, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.set(-8, 1, 8, Blocks.CAULDRON);
            this.b.set(-9, 1, 9, Blocks.DECORATED_POT);
            this.b.set(9, 3, -9, Blocks.COBWEB);
            this.b.set(-9, 4, 9, Blocks.COBWEB);
        }

        /** Flugsand in Kammern, Gang und Hof; Schutt an der eingestürzten Ecke. */
        void sand() {
            for (int x = -HALF + 1; x <= HALF - 1; x++) {
                for (int z = -HALF + 1; z <= HALF - 1; z++) {
                    if (!this.b.get(x, 1, z).isAir()) {
                        continue;
                    }
                    if (Math.abs(x) <= 2 && Math.abs(z) <= 2) {
                        continue;
                    }
                    float n = this.b.noise(x, 41, z);
                    boolean nearWall = this.ring(x, z) >= 9 || this.ring(x, z) == 6;
                    if (n < (nearWall ? 0.3f : 0.12f)) {
                        this.b.set(x, 1, z, Blocks.SAND.defaultBlockState());
                        if (n < 0.08f && this.b.get(x, 2, z).isAir()) {
                            this.b.set(x, 2, z, Blocks.SAND.defaultBlockState());
                        }
                    }
                    if (x >= 6 && z <= -6 && this.b.chance(x, 42, z, 0.45f)) {
                        this.b.set(x, 1, z, this.b.chance(x, 43, z, 0.5f) ? Blocks.SANDSTONE.defaultBlockState()
                                : Blocks.SMOOTH_SANDSTONE_SLAB.defaultBlockState());
                    }
                }
            }
        }
    }
}
