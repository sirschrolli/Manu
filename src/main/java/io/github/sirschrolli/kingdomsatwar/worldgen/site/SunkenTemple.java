package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.block.sea.TideGateBlock;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.urmeer.TideGate;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Der Versunkene Tempel (seit 0.89): Auf dem Grund der tiefen Ozeane steht das Heiligtum eines Meervolks, das vor langer
 * Zeit ins Urmeer gezogen ist – ein runder Tempel auf einem gestuften Sockel aus Gezeitenstein, ringsum zwölf Säulen
 * (einige gebrochen und umgestürzt), darüber eine halb eingestürzte Kuppel mit dunklen Rippen. In der Mitte steht auf
 * einem Podest das <b>Gezeitentor</b>: ein Ring aus Gezeitensteinziegeln, elf Blöcke hoch, in dem das Wasser leuchtend
 * wirbelt – der Weg ins Urmeer. Zu beiden Seiten des Tors stehen Truhen mit den Gaben des Meervolks; Wächter
 * durchstreifen die Ruine.
 *
 * <p>Eigene Koordinaten: Mitte bei (0, 0, 0), y = 0 ist die Höhe direkt über dem höchsten Grund unter dem Tempel.
 */
final class SunkenTemple {
    /** Halbmesser des Sockels. */
    static final int REACH = 13;
    /** Halbmesser der Säulenreihe. */
    static final int COLONNADE = 9;
    /** Höhe der Mitte des Tors (über y = 0). */
    static final int GATE_Y = 2 + 1 + TideGate.RADIUS;

    private SunkenTemple() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        if (!terrain.water(x, z)) {
            return Optional.empty();
        }
        int[] span = terrain.span(x, z, 12, 6);
        int y = span[1] + 1;
        // die Kuppel (19 hoch) soll gut unter dem Meeresspiegel bleiben, der Grund nicht zu schief sein
        if (terrain.surface(x, z) - y < 22 || span[1] - span[0] > 7) {
            return Optional.empty();
        }
        BlockPos origin = new BlockPos(x, y, z);
        int axis = (int) Math.floorMod(seed, 2L);
        int ruin = (int) Math.floorMod(seed >> 8, 8L);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.SUNKEN_TEMPLE, 0, origin, Rotation.NONE, seed, new int[] {axis, ruin},
                        box(origin)))));
    }

    static BoundingBox box(BlockPos origin) {
        return new BoundingBox(origin.getX() - REACH - 1, origin.getY() - 10, origin.getZ() - REACH - 1, origin.getX() + REACH + 1,
                origin.getY() + 20, origin.getZ() + REACH + 1);
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Temple(b, piece.data(0) == 1 ? Direction.Axis.Z : Direction.Axis.X, piece.data(1)).build();
    }

    private static final class Temple {
        private final VillageBuilder b;
        private final Direction.Axis axis;
        private final Direction along;
        private final Direction out;
        /** Wo die Kuppel eingestürzt ist (Achtel des Kreises). */
        private final int ruin;
        private final BlockState bricks = SeaBlocks.TIDE_STONE_BRICKS.base().get().defaultBlockState();
        private final BlockState stone = SeaBlocks.TIDE_STONE.base().get().defaultBlockState();
        private final BlockState chiseled = SeaBlocks.CHISELED_TIDE_STONE.get().defaultBlockState();
        private final BlockState dark = Blocks.DARK_PRISMARINE.defaultBlockState();

        Temple(VillageBuilder b, Direction.Axis axis, int ruin) {
            this.b = b;
            this.axis = axis;
            this.along = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
            this.out = TideGate.out(axis);
            this.ruin = ruin;
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

        /** Eine Stelle relativ zum Tor: {@code a} entlang des Tors, {@code o} aus ihm heraus. */
        void gatePut(int a, int y, int o, BlockState s) {
            int x = this.along.getStepX() * a + this.out.getStepX() * o;
            int z = this.along.getStepZ() * a + this.out.getStepZ() * o;
            this.put(x, y, z, s);
        }

        BlockState slab(SlabType type) {
            return SeaBlocks.TIDE_STONE_BRICKS.slab().get().defaultBlockState().setValue(SlabBlock.TYPE, type);
        }

        BlockState stairs(Direction facing, Half half) {
            return VillageBuilder.stairs(SeaBlocks.TIDE_STONE_BRICKS.stairs().get(), facing, half);
        }

        /** Richtung zur Mitte (für Stufen, die zum Tempel hinaufführen). */
        static Direction inward(int x, int z) {
            if (Math.abs(x) >= Math.abs(z)) {
                return x > 0 ? Direction.WEST : Direction.EAST;
            }
            return z > 0 ? Direction.NORTH : Direction.SOUTH;
        }

        /** Liegt die Stelle im eingestürzten Teil (ein Achtel, dazu ein weicher Rand)? */
        boolean ruined(int x, int z, float slack) {
            double angle = Math.atan2(z, x);
            double sector = (this.ruin + 0.5) * Math.PI / 4.0 - Math.PI;
            double diff = Math.abs(Math.atan2(Math.sin(angle - sector), Math.cos(angle - sector)));
            return diff < Math.PI / 6.0 + slack * this.b.noise(x, 7, z);
        }

        void build() {
            this.foundation();
            this.colonnade();
            this.architrave();
            this.dome();
            this.gate();
            this.lanterns();
            this.debris();
            this.growth();
        }

        /**
         * Der gestufte Sockel: außen eine breite Stufe, innen der Boden des Tempels mit Ringen aus dunklem Prismarin und
         * Meereslaternen; darunter Gezeitenstein bis zum Grund. Über dem Sockel nur Wasser.
         */
        void foundation() {
            for (int x = -REACH; x <= REACH; x++) {
                for (int z = -REACH; z <= REACH; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r > REACH + 0.4) {
                        continue;
                    }
                    for (int y = -1; y >= -10; y--) {
                        if (this.b.get(x, y, z).isSolidRender()) {
                            break;
                        }
                        this.put(x, y, z, this.stone);
                    }
                    if (r <= 10.5) {
                        this.put(x, 0, z, this.stone);
                        this.put(x, 1, z, this.floor(x, z, r));
                    } else if (r <= 11.5) {
                        this.put(x, 0, z, this.bricks);
                        this.put(x, 1, z, this.stairs(inward(x, z), Half.BOTTOM));
                    } else if (r <= 12.4) {
                        this.put(x, 0, z, this.bricks);
                        this.put(x, 1, z, this.water());
                    } else {
                        this.put(x, 0, z, this.slab(SlabType.BOTTOM));
                        this.put(x, 1, z, this.water());
                    }
                    for (int y = 2; y <= 19; y++) {
                        if (!this.b.get(x, y, z).is(Blocks.WATER)) {
                            this.put(x, y, z, this.water());
                        }
                    }
                }
            }
        }

        BlockState floor(int x, int z, double r) {
            if (r >= 6.5 && r < 7.5) {
                return this.dark;
            }
            if (r >= 3.5 && r < 4.5) {
                return Blocks.PRISMARINE_BRICKS.defaultBlockState();
            }
            if ((x == 0 || z == 0) && Math.abs(x + z) == 8) {
                return Blocks.SEA_LANTERN.defaultBlockState();
            }
            if ((x == 0 || z == 0 || Math.abs(x) == Math.abs(z)) && r > 7.5) {
                return Blocks.PRISMARINE_BRICKS.defaultBlockState();
            }
            return this.b.noise(x, 1, z) > 0.8f ? this.stone : this.bricks;
        }

        /** Ist die Säule {@code k} (von zwölf) gebrochen? Wie hoch steht sie noch (0: ganz)? */
        int broken(int k) {
            float n = this.b.noise(k * 13, 40, k * 7);
            boolean inRuin = this.ruined((int) Math.round(Math.cos(k * Math.PI / 6) * COLONNADE),
                    (int) Math.round(Math.sin(k * Math.PI / 6) * COLONNADE), 0.0f);
            if (inRuin || n > 0.72f) {
                return 2 + (int) (this.b.noise(k * 5, 41, k * 3) * 4);
            }
            return 0;
        }

        /** Zwölf Säulen: Fuß aus Gezeitenstein, Schaft aus Ziegeln, Kapitell aus dunklem Prismarin – manche gebrochen. */
        void colonnade() {
            for (int k = 0; k < 12; k++) {
                int x = (int) Math.round(Math.cos(k * Math.PI / 6) * COLONNADE);
                int z = (int) Math.round(Math.sin(k * Math.PI / 6) * COLONNADE);
                int stump = this.broken(k);
                int top = stump > 0 ? 1 + stump : 9;
                for (int y = 2; y <= top; y++) {
                    this.put(x, y, z, y == 2 ? this.stone : y == 9 ? this.dark : this.bricks);
                }
                if (stump > 0) {
                    // Seegurken auf dem Stumpf, der Rest der Säule liegt daneben am Boden
                    this.put(x, top + 1, z, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 1 + k % 3));
                    Direction fall = inward(x, z);
                    Direction side = fall.getClockWise();
                    int length = 7 - stump;
                    for (int i = 2; i < 2 + length; i++) {
                        int fx = x + fall.getStepX() * i + side.getStepX() * (k % 2);
                        int fz = z + fall.getStepZ() * i + side.getStepZ() * (k % 2);
                        if (!this.onDais(fx, fz)) {
                            this.put(fx, 2, fz, i == 1 + length ? this.dark : this.bricks);
                        }
                    }
                }
            }
        }

        /** Der Ring über den Säulen – wo Säulen gebrochen sind, liegt auch er am Boden. */
        void architrave() {
            for (int x = -COLONNADE - 1; x <= COLONNADE + 1; x++) {
                for (int z = -COLONNADE - 1; z <= COLONNADE + 1; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r < COLONNADE - 0.6 || r > COLONNADE + 0.6 || this.ruined(x, z, 0.3f)) {
                        continue;
                    }
                    int k = Math.floorMod((int) Math.round(Math.atan2(z, x) / (Math.PI / 6)), 12);
                    if (this.broken(k) > 0) {
                        continue;
                    }
                    this.put(x, 10, z, this.bricks);
                    this.put(x, 11, z, this.b.noise(x, 11, z) > 0.5f ? this.slab(SlabType.BOTTOM) : this.bricks);
                }
            }
        }

        /** Die Kuppel: Ziegel mit acht Rippen aus dunklem Prismarin und einem offenen Auge; ein Achtel ist eingestürzt. */
        void dome() {
            double radius = COLONNADE + 0.5;
            double rise = 7.5;
            for (int x = -COLONNADE - 1; x <= COLONNADE + 1; x++) {
                for (int z = -COLONNADE - 1; z <= COLONNADE + 1; z++) {
                    double rr = (x * x + z * z) / (radius * radius);
                    if (rr > 1.0 || x * x + z * z <= 4 || this.ruined(x, z, 0.5f)) {
                        continue;
                    }
                    for (int y = 11; y <= 19; y++) {
                        double e = Math.sqrt(rr + (y - 11) * (y - 11) / (rise * rise));
                        if (e < 0.86 || e > 1.0) {
                            continue;
                        }
                        if (y > 11 && this.b.noise(x, y, z) > 0.74f) {
                            continue;
                        }
                        boolean rib = x == 0 || z == 0 || Math.abs(x) == Math.abs(z);
                        this.put(x, y, z, rib ? this.dark : this.bricks);
                    }
                }
            }
            // das Auge: ein Kranz aus gemeißeltem Gezeitenstein
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    int d = x * x + z * z;
                    if (d > 4 && d <= 9 && !this.ruined(x, z, 0.5f)) {
                        this.put(x, 18, z, (x == 0 || z == 0) ? this.chiseled : this.bricks);
                    }
                }
            }
        }

        /**
         * Das Gezeitentor auf seinem Podest: Stufen davor und dahinter, an den Enden des Podests je eine Truhe mit den Gaben
         * des Meervolks.
         */
        void gate() {
            for (int a = -7; a <= 7; a++) {
                for (int o = -3; o <= 3; o++) {
                    BlockState s = Math.abs(o) == 3 ? this.stairs(o > 0 ? this.out.getOpposite() : this.out, Half.BOTTOM)
                            : Math.abs(a) == 7 ? this.stairs(a > 0 ? this.along.getOpposite() : this.along, Half.BOTTOM)
                            : (o == 0 && Math.abs(a) <= 3) ? this.chiseled : this.bricks;
                    if (Math.abs(o) == 3 && Math.abs(a) == 7) {
                        s = this.bricks;
                    }
                    this.gatePut(a, 2, o, s);
                }
            }
            for (int a = -TideGate.RING; a <= TideGate.RING; a++) {
                for (int h = -TideGate.RING; h <= TideGate.RING; h++) {
                    BlockState ring = TideGate.ringBlock(a, h);
                    if (ring != null) {
                        this.gatePut(a, GATE_Y + h, 0, ring);
                    } else if (TideGate.inDisc(a, h)) {
                        this.gatePut(a, GATE_Y + h, 0, TideGate.veil(this.axis, true));
                    }
                }
            }
            // die Gaben: zwei Truhen an den Enden des Podests
            for (int side : new int[] {-1, 1}) {
                BlockPos pos = this.chestPos(side);
                BlockState chest = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, this.out)
                        .setValue(BlockStateProperties.WATERLOGGED, true);
                this.b.chest(pos.getX(), pos.getY(), pos.getZ(), chest, ModLootTables.SITE_SUNKEN_TEMPLE);
            }
        }

        BlockPos chestPos(int side) {
            int a = side * 6;
            return new BlockPos(this.along.getStepX() * a, 3, this.along.getStepZ() * a);
        }

        /** Tiefseelaternen hängen unter dem Ring zwischen den Säulen (nur, wo der Ring noch steht). */
        void lanterns() {
            BlockState lantern = SeaBlocks.DEEP_SEA_LANTERN.get().defaultBlockState().setValue(LanternBlock.HANGING, true);
            for (int k = 0; k < 12; k += 2) {
                double angle = (k + 0.5) * Math.PI / 6;
                int x = (int) Math.round(Math.cos(angle) * COLONNADE);
                int z = (int) Math.round(Math.sin(angle) * COLONNADE);
                if (this.b.get(x, 10, z).is(this.bricks.getBlock())) {
                    this.put(x, 9, z, lantern);
                }
            }
        }

        /** Trümmer der eingestürzten Kuppel am Boden: Ziegel, Brocken, Sand. */
        void debris() {
            for (int x = -REACH + 1; x <= REACH - 1; x++) {
                for (int z = -REACH + 1; z <= REACH - 1; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r > REACH - 0.5 || r < 4.5 || !this.ruined(x, z, 0.6f) || this.onDais(x, z)) {
                        continue;
                    }
                    float n = this.b.noise(x, 3, z);
                    if (n > 0.62f) {
                        this.put(x, 2, z, n > 0.8f ? this.dark : this.bricks);
                        if (n > 0.86f) {
                            this.put(x, 3, z, this.slab(SlabType.BOTTOM));
                        }
                    } else if (n > 0.45f) {
                        this.put(x, 2, z, Blocks.SAND.defaultBlockState());
                    }
                }
            }
        }

        boolean onDais(int x, int z) {
            int a = this.along.getStepX() * x + this.along.getStepZ() * z;
            int o = this.out.getStepX() * x + this.out.getStepZ() * z;
            return Math.abs(a) <= 7 && Math.abs(o) <= 3;
        }

        /** Seegras und Seegurken zwischen den Platten, Tang an der äußeren Stufe, Sandwehen. */
        void growth() {
            for (int x = -REACH; x <= REACH; x++) {
                for (int z = -REACH; z <= REACH; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r > REACH + 0.4 || this.onDais(x, z)) {
                        continue;
                    }
                    int ground = r <= 10.5 ? 2 : 1;
                    // nur auf vollen Blöcken (nicht auf Stufen und Treppen) wächst etwas
                    if (!this.b.get(x, ground, z).is(Blocks.WATER) || !this.b.get(x, ground - 1, z).isSolidRender()) {
                        continue;
                    }
                    float n = this.b.noise(x, 21, z);
                    if (r > 11.5 && n > 0.86f) {
                        int height = 2 + (int) (this.b.noise(x, 22, z) * 5);
                        for (int y = ground; y < ground + height; y++) {
                            this.put(x, y, z, y == ground + height - 1 ? Blocks.KELP.defaultBlockState() : Blocks.KELP_PLANT.defaultBlockState());
                        }
                    } else if (n > 0.8f) {
                        this.put(x, ground, z, Blocks.SEAGRASS.defaultBlockState());
                    } else if (n < 0.04f && r > 5) {
                        this.put(x, ground, z, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 2));
                    }
                }
            }
        }
    }
}
