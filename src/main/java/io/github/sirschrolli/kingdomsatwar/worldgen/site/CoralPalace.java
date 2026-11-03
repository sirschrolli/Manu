package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Der Korallenpalast: Tief unten im Ozean steht der Palast eines versunkenen Meervolks – eine Halle aus Prismarin unter
 * einer gläsernen Kuppel mit dunklen Rippen, an den vier Ecken Türme aus lebenden Korallen (jeder in einer anderen
 * Farbe), gekrönt von Fächern und Seegurken. In der Halle steht der Thron des Meerkönigs auf einem Sockel aus Gold, bewacht
 * von einem alten Wächter; unter der Halle liegt die Schwammgrotte. Wächter durchstreifen den Palast.
 *
 * <p>Eigene Koordinaten: Mitte bei (0, 0, 0), y = 0 ist die Höhe direkt über dem Meeresgrund.
 */
final class CoralPalace {
    static final int HALF = 16;
    private static final Block[] CORALS = {Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK};
    private static final Block[] FANS = {Blocks.TUBE_CORAL_WALL_FAN, Blocks.BRAIN_CORAL_WALL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN,
            Blocks.FIRE_CORAL_WALL_FAN};

    private CoralPalace() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int[] span = terrain.span(x, z, 16, 8);
        int y = span[1] + 1;
        // die Kuppel (22 hoch) soll gut unter dem Meeresspiegel bleiben
        if (!terrain.water(x, z) || terrain.surface(x, z) - y < 26 || span[1] - span[0] > 6) {
            return Optional.empty();
        }
        BlockPos origin = new BlockPos(x, y, z);
        BoundingBox box = new BoundingBox(x - HALF - 1, y - 10, z - HALF - 1, x + HALF + 1, y + 22, z + HALF + 1);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.CORAL_PALACE, 0, origin, Rotation.NONE, seed, new int[0], box))));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Palace(b).build();
    }

    private static final class Palace {
        private final VillageBuilder b;

        Palace(VillageBuilder b) {
            this.b = b;
        }

        BlockState water() {
            return Blocks.WATER.defaultBlockState();
        }

        BlockState wet(BlockState s) {
            return s.hasProperty(BlockStateProperties.WATERLOGGED) ? s.setValue(BlockStateProperties.WATERLOGGED, true) : s;
        }

        void put(int x, int y, int z, BlockState s) {
            this.b.set(x, y, z, this.wet(s));
        }

        BlockState wall(int x, int y, int z) {
            float n = this.b.noise(x, y, z);
            return n < 0.6f ? Blocks.PRISMARINE_BRICKS.defaultBlockState() : n < 0.85f ? Blocks.PRISMARINE.defaultBlockState()
                    : Blocks.DARK_PRISMARINE.defaultBlockState();
        }

        void build() {
            this.platform();
            this.hall();
            this.roof();
            this.dome();
            this.towers();
            this.colonnade();
            this.throne();
            this.grotto();
        }

        /** Sockel aus dunklem Prismarin bis zum Grund, oben ein Boden mit Mustern aus Meereslaternen. */
        void platform() {
            for (int x = -HALF; x <= HALF; x++) {
                for (int z = -HALF; z <= HALF; z++) {
                    for (int y = 1; y >= -9; y--) {
                        if (y < 0 && this.b.get(x, y, z).isSolidRender()) {
                            break;
                        }
                        this.put(x, y, z, y == 1 ? this.floor(x, z) : Blocks.DARK_PRISMARINE.defaultBlockState());
                    }
                    for (int y = 2; y <= 21; y++) {
                        BlockState s = this.b.get(x, y, z);
                        if (!s.is(Blocks.WATER)) {
                            this.put(x, y, z, this.water());
                        }
                    }
                }
            }
        }

        BlockState floor(int x, int z) {
            int ring = Math.max(Math.abs(x), Math.abs(z));
            if (ring % 4 == 2 && (x == 0 || z == 0 || Math.abs(x) == Math.abs(z))) {
                return Blocks.SEA_LANTERN.defaultBlockState();
            }
            return ring % 4 == 0 ? Blocks.DARK_PRISMARINE.defaultBlockState() : Blocks.PRISMARINE_BRICKS.defaultBlockState();
        }

        /** Die Halle (19 × 19, zehn hoch) mit hohen Bogenfenstern auf jeder Seite. */
        void hall() {
            for (int x = -9; x <= 9; x++) {
                for (int z = -9; z <= 9; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) != 9) {
                        continue;
                    }
                    int along = Math.abs(x) == 9 ? z : x;
                    for (int y = 2; y <= 11; y++) {
                        boolean window = Math.abs(along) <= 2 && y >= 3 && y <= 8 || Math.abs(Math.abs(along) - 6) <= 0 && y >= 4 && y <= 7;
                        boolean pillar = Math.abs(along) == 9 || Math.abs(along) == 4;
                        if (window && !pillar) {
                            continue;
                        }
                        this.put(x, y, z, pillar ? Blocks.DARK_PRISMARINE.defaultBlockState() : y == 11 ? Blocks.DARK_PRISMARINE.defaultBlockState()
                                : this.wall(x, y, z));
                    }
                    // Bogen über den großen Fenstern
                    if (Math.abs(along) <= 2) {
                        Direction ax = Math.abs(x) == 9 ? Direction.SOUTH : Direction.EAST;
                        if (along == -2) {
                            this.put(x, 8, z, VillageBuilder.stairs(Blocks.PRISMARINE_BRICK_STAIRS, ax.getOpposite(), Half.TOP));
                        } else if (along == 2) {
                            this.put(x, 8, z, VillageBuilder.stairs(Blocks.PRISMARINE_BRICK_STAIRS, ax, Half.TOP));
                        }
                    }
                }
            }
        }

        /** Das flache Dach der Halle rings um die Kuppel, mit Meereslaternen in den Ecken. */
        void roof() {
            for (int x = -9; x <= 9; x++) {
                for (int z = -9; z <= 9; z++) {
                    if (Math.sqrt(x * x + z * z) < 8.6) {
                        continue;
                    }
                    boolean corner = Math.abs(x) >= 8 && Math.abs(z) >= 8;
                    this.put(x, 11, z, corner && Math.abs(x) == 8 && Math.abs(z) == 8 ? Blocks.SEA_LANTERN.defaultBlockState()
                            : Math.max(Math.abs(x), Math.abs(z)) == 9 ? Blocks.DARK_PRISMARINE.defaultBlockState()
                                    : Blocks.PRISMARINE_BRICKS.defaultBlockState());
                }
            }
            // Zinnen auf dem Rand
            for (int x = -9; x <= 9; x++) {
                for (int z = -9; z <= 9; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) == 9 && Math.floorMod(x + z, 2) == 0 && Math.sqrt(x * x + z * z) > 9.7) {
                        this.put(x, 12, z, Blocks.PRISMARINE_WALL.defaultBlockState());
                    }
                }
            }
        }

        /** Die Kuppel: eine Halbkugel aus hellblauem Glas mit acht Rippen aus dunklem Prismarin, oben eine Laterne. */
        void dome() {
            double r = 9.6;
            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    for (int y = 0; y <= 10; y++) {
                        double d = Math.sqrt(x * x + z * z + y * y);
                        if (d > r || d < r - 1.0) {
                            continue;
                        }
                        double phi = Math.atan2(z, x);
                        boolean rib = Math.abs(Math.IEEEremainder(phi, Math.PI / 4)) < 0.11;
                        this.put(x, 11 + y, z, rib ? Blocks.DARK_PRISMARINE.defaultBlockState()
                                : Blocks.STAINED_GLASS.pick(DyeColor.LIGHT_BLUE).defaultBlockState());
                    }
                }
            }
            this.put(0, 21, 0, Blocks.SEA_LANTERN.defaultBlockState());
            this.put(0, 22, 0, Blocks.DARK_PRISMARINE.defaultBlockState());
        }

        /** Vier Türme aus lebenden Korallen an den Ecken, mit Bändern aus Prismarin, oben Fächer und Seegurken. */
        void towers() {
            int i = 0;
            for (int[] c : new int[][] {{-13, -13}, {13, -13}, {-13, 13}, {13, 13}}) {
                Block coral = CORALS[i];
                Block fan = FANS[i];
                i++;
                int height = 13 + (i % 2) * 3;
                for (int x = -3; x <= 3; x++) {
                    for (int z = -3; z <= 3; z++) {
                        double d = Math.sqrt(x * x + z * z);
                        for (int y = 2; y <= height; y++) {
                            double rr = y > height - 3 ? 1.6 : 2.6;
                            if (d > rr) {
                                continue;
                            }
                            boolean band = y % 4 == 1;
                            boolean inner = d < rr - 1.0;
                            this.put(c[0] + x, y, c[1] + z, inner ? (y % 5 == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : this.water())
                                    : band ? Blocks.DARK_PRISMARINE.defaultBlockState() : coral.defaultBlockState());
                        }
                    }
                }
                // Krone: Fächer ringsum, Seegurken obenauf
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    for (int y = height - 2; y <= height; y++) {
                        int fx = c[0] + d.getStepX() * 2;
                        int fz = c[1] + d.getStepZ() * 2;
                        if (this.b.get(fx, y, fz).is(Blocks.WATER)) {
                            this.put(fx, y, fz, fan.defaultBlockState().setValue(BaseCoralWallFanBlock.FACING, d));
                        }
                    }
                }
                this.put(c[0], height + 1, c[1], Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 4));
                this.put(c[0] + 1, height, c[1], Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 3));
                // ein Strebebogen von der Ecke der Halle zum Turm
                int sx = Integer.signum(c[0]);
                int sz = Integer.signum(c[1]);
                for (int t = 10; t <= 11; t++) {
                    for (int u = 10; u <= 11; u++) {
                        this.put(sx * t, 9, sz * u, Blocks.DARK_PRISMARINE.defaultBlockState());
                        this.put(sx * t, 8, sz * u, t == u ? Blocks.PRISMARINE_BRICKS.defaultBlockState() : this.water());
                    }
                }
            }
        }

        /**
         * Ein Säulengang zwischen den Türmen: schlanke Säulen mit Laternen-Kapitellen, darüber ein Gebälk; in der Mitte
         * jeder Seite ein breites Tor.
         */
        void colonnade() {
            for (int side = 0; side < 4; side++) {
                for (int along = -10; along <= 10; along++) {
                    int x = side < 2 ? along : side == 2 ? -13 : 13;
                    int z = side < 2 ? (side == 0 ? -13 : 13) : along;
                    boolean pillar = Math.abs(along) % 4 == 2;
                    if (pillar) {
                        for (int y = 2; y <= 6; y++) {
                            this.put(x, y, z, y == 2 ? Blocks.DARK_PRISMARINE.defaultBlockState() : y == 6 ? Blocks.SEA_LANTERN.defaultBlockState()
                                    : Blocks.PRISMARINE_BRICKS.defaultBlockState());
                        }
                    }
                    this.put(x, 7, z, Math.abs(along) <= 1 ? Blocks.DARK_PRISMARINE.defaultBlockState()
                            : Blocks.PRISMARINE_BRICK_SLAB.defaultBlockState()
                                    .setValue(net.minecraft.world.level.block.SlabBlock.TYPE,
                                            net.minecraft.world.level.block.state.properties.SlabType.BOTTOM));
                }
            }
        }

        /**
         * Der Thron des Meerkönigs: ein Sockel aus acht Goldblöcken, darauf ein Thron aus dunklem Prismarin, gerahmt von
         * Säulen mit Meereslaternen; davor wacht ein alter Wächter.
         */
        void throne() {
            for (int x = -1; x <= 1; x++) {
                for (int z = 3; z <= 6; z++) {
                    if (x == 0 && z == 3) {
                        continue;
                    }
                    if (z <= 4 || Math.abs(x) <= 1 && z <= 5) {
                        this.put(x, 2, z, Blocks.GOLD_BLOCK.defaultBlockState());
                    }
                }
            }
            this.put(0, 2, 3, VillageBuilder.stairs(Blocks.DARK_PRISMARINE_STAIRS, Direction.SOUTH, Half.BOTTOM));
            this.put(0, 3, 5, VillageBuilder.stairs(Blocks.DARK_PRISMARINE_STAIRS, Direction.NORTH, Half.BOTTOM));
            this.put(-1, 3, 5, VillageBuilder.stairs(Blocks.DARK_PRISMARINE_STAIRS, Direction.EAST, Half.BOTTOM));
            this.put(1, 3, 5, VillageBuilder.stairs(Blocks.DARK_PRISMARINE_STAIRS, Direction.WEST, Half.BOTTOM));
            for (int x = -1; x <= 1; x++) {
                this.put(x, 2, 6, Blocks.DARK_PRISMARINE.defaultBlockState());
            }
            for (int y = 3; y <= 5; y++) {
                this.put(0, y, 6, Blocks.DARK_PRISMARINE.defaultBlockState());
            }
            this.put(0, 6, 6, Blocks.SEA_LANTERN.defaultBlockState());
            for (int x : new int[] {-3, 3}) {
                for (int y = 2; y <= 8; y++) {
                    this.put(x, y, 6, y == 8 ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.PRISMARINE_BRICKS.defaultBlockState());
                }
            }
            BlockPos pos = this.b.world(0, 3, 0);
            if (this.b.inside(pos)) {
                ElderGuardian keeper = io.github.sirschrolli.kingdomsatwar.registry.ModEntities.ELDER_GUARDIAN.get()
                        .create(this.b.level.getLevel(), EntitySpawnReason.STRUCTURE);
                if (keeper != null) {
                    keeper.setPersistenceRequired();
                    keeper.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 180.0f, 0.0f);
                    this.b.level.addFreshEntity(keeper);
                }
            }
        }

        /** Unter der Halle die Schwammgrotte: Nischen voller nasser Schwämme, Meereslaternen, ein Zugang im Boden. */
        void grotto() {
            // der Boden der Halle (y = 1) bleibt die Decke der Grotte
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    for (int y = -4; y <= 0; y++) {
                        boolean shell = Math.abs(x) == 5 || Math.abs(z) == 5 || y == -4;
                        this.put(x, y, z, shell ? Blocks.DARK_PRISMARINE.defaultBlockState() : this.water());
                    }
                }
            }
            // Zugang: eine Öffnung im Hallenboden vor dem Thron, eingefasst von Stufen
            for (int x = -1; x <= 1; x++) {
                this.put(x, 1, -4, this.water());
                this.put(x, 1, -5, Blocks.DARK_PRISMARINE.defaultBlockState());
            }
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    boolean edge = Math.abs(x) == 4 || Math.abs(z) == 4;
                    if (edge && this.b.chance(x, -2, z, 0.75f)) {
                        this.put(x, -3 + (int) (this.b.noise(x, 9, z) * 3), z, Blocks.WET_SPONGE.defaultBlockState());
                    }
                }
            }
            this.put(0, -3, 0, Blocks.SEA_LANTERN.defaultBlockState());
            this.put(2, -3, 2, Blocks.SEA_LANTERN.defaultBlockState());
            this.put(-2, -3, -2, Blocks.SEA_LANTERN.defaultBlockState());
        }
    }
}
