package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Gebeine eines Seedrachen: Am Meeresgrund – oder an einen Strand gespült – liegt das Gerippe eines riesigen
 * Seedrachen, der einst Schiffe verschlang. Der Schädel mit offenem Maul und leuchtenden Augenhöhlen, ein geschwungener
 * Hals, darüber die Rippenbögen des Brustkorbs mit einem Kamm aus Wirbeldornen, dahinter der Schwanz, der im Sand
 * versinkt. Im Brustkorb liegt, was er verschluckt hat: Truhen eines Schiffs (mit der Seekarte des Kapitäns), Fässer,
 * ein gebrochener Mast, Planken und die Schiffsglocke.
 *
 * <p>Eigene Koordinaten: z entlang des Körpers (der Schädel vorn bei z &lt; 0), x quer dazu, y = 0 ist die Höhe direkt über
 * dem Grund.
 */
final class SeaDragon {
    /** Länge des Körpers (Hals bis Schwanzspitze). */
    static final int LENGTH = 40;
    private static final int RIBS_FROM = 9;
    private static final int RIBS_TO = 23;

    private SeaDragon() {}

    // =====================================================================================
    // Planung
    // =====================================================================================

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int cx = chunk.getMiddleBlockX();
        int cz = chunk.getMiddleBlockZ();
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 11), 4)];
        BlockPos mid = new BlockPos(0, 0, 14).rotate(rotation);
        int x = cx + mid.getX();
        int z = cz + mid.getZ();
        int[] span = terrain.span(x, z, 12, 6);
        int sea = terrain.seaLevel();
        int ground = terrain.ground(x, z);
        boolean sunken;
        if (terrain.waterDepth(x, z) >= 7) {
            if (span[1] - span[0] > 4) {
                return Optional.empty();
            }
            sunken = true;
        } else if (!terrain.water(x, z) && ground >= sea - 1 && ground <= sea + 4 && span[1] - span[0] <= 3 && nearWater(terrain, x, z)) {
            sunken = false;
        } else {
            return Optional.empty();
        }
        BlockPos origin = new BlockPos(cx, ground + 1, cz);
        BoundingBox box = SitePiece.box(origin, rotation, -16, -5, -13, 16, 13, LENGTH + 2);
        int[] data = {sunken ? 1 : 0, sea};
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.SEA_DRAGON, 0, origin, rotation, seed, data, box))));
    }

    private static boolean nearWater(SiteTerrain terrain, int x, int z) {
        for (int d = 8; d <= 24; d += 8) {
            if (terrain.water(x + d, z) || terrain.water(x - d, z) || terrain.water(x, z + d) || terrain.water(x, z - d)) {
                return true;
            }
        }
        return false;
    }

    // =====================================================================================
    // Bauen
    // =====================================================================================

    static void build(VillageBuilder b, SitePiece piece) {
        new Bones(b, piece).build();
    }

    private static final class Bones {
        private final VillageBuilder b;
        private final boolean sunken;
        private final int sea;
        private final int baseY;
        private final double phase;

        Bones(VillageBuilder b, SitePiece piece) {
            this.b = b;
            this.sunken = piece.data(0) == 1;
            this.sea = piece.data(1);
            this.baseY = piece.origin().getY();
            this.phase = (piece.seed() & 0xFF) / 255.0 * Math.PI;
        }

        // ---- Form --------------------------------------------------------------------------------

        /** Die Mittellinie des Körpers: eine sanfte S-Kurve. */
        double spineX(double z) {
            return 3.2 * Math.sin(z / LENGTH * Math.PI * 1.5 + this.phase) - 3.2 * Math.sin(this.phase);
        }

        /** Höhe der Wirbelsäule: der Hals steigt vom Schädel an, der Rücken wölbt sich über dem Brustkorb, der Schwanz sinkt. */
        double spineY(double z) {
            if (z < RIBS_FROM) {
                return 2.5 + z / RIBS_FROM * 4.5;
            }
            if (z <= RIBS_TO) {
                double t = (z - RIBS_FROM) / (double) (RIBS_TO - RIBS_FROM);
                return 7.0 + Math.sin(t * Math.PI) * 1.5;
            }
            double t = (z - RIBS_TO) / (double) (LENGTH - RIBS_TO);
            return 7.0 * (1 - t) - 1.2 * t;
        }

        /** Halbe Spannweite der Rippen bei z (in der Mitte am weitesten). */
        double ribSpan(double z) {
            double t = (z - RIBS_FROM) / (double) (RIBS_TO - RIBS_FROM);
            return 4.0 + Math.sin(t * Math.PI) * 3.0;
        }

        // ---- Setzen ------------------------------------------------------------------------------

        boolean wet(int x, int y, int z) {
            if (this.baseY + y >= this.sea) {
                return false;
            }
            return this.sunken || !this.b.get(x, y, z).getFluidState().isEmpty();
        }

        void put(int x, int y, int z, BlockState state) {
            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && this.wet(x, y, z)) {
                state = state.setValue(BlockStateProperties.WATERLOGGED, true);
            }
            this.b.set(x, y, z, state);
        }

        BlockState bone(Direction.Axis axis) {
            return Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
        }

        /** Ein Knochen; was darunter frei ist (Wasser, Luft), wird bis zum Grund mit Sand gefüllt, wenn er tief liegt. */
        void boneAt(int x, int y, int z, Direction.Axis axis) {
            this.put(x, y, z, this.bone(axis));
        }

        void build() {
            this.skull();
            this.spine();
            this.ribs();
            this.cargo();
            this.sand();
        }

        // ---- Schädel -----------------------------------------------------------------------------

        /**
         * Der Schädel liegt vorn (z von −11 bis 0), das Maul weit offen: unten der Unterkiefer mit Zähnen (Kerzen) am
         * Boden, darüber der Oberkiefer mit zwei Fangzähnen; hinten der Hirnschädel mit leuchtenden Augenhöhlen und zwei
         * nach hinten geschwungenen Hörnern.
         */
        void skull() {
            int sx = (int) Math.round(this.spineX(0));
            // Unterkiefer: zwei Äste, die vorn zusammenlaufen
            for (int z = -10; z <= -1; z++) {
                double half = 1.0 + (z + 10) * 0.28;
                for (int side : new int[] {-1, 1}) {
                    int x = sx + side * (int) Math.round(half);
                    this.boneAt(x, 0, z, Direction.Axis.Z);
                    if (z % 2 == 0 && z < -2) {
                        this.put(x, 1, z, Blocks.DYED_CANDLE.pick(DyeColor.WHITE).defaultBlockState().setValue(CandleBlock.CANDLES,
                                1 + (int) (this.b.noise(x, 1, z) * 2)));
                    }
                }
                if (z <= -9) {
                    for (int x = sx - 1; x <= sx + 1; x++) {
                        this.boneAt(x, 0, z, Direction.Axis.X);
                    }
                }
            }
            // Oberkiefer: die Schnauze, leicht angehoben
            for (int z = -11; z <= -4; z++) {
                double half = 1.2 + (z + 11) * 0.25;
                int y = 3 + (z >= -6 ? 1 : 0);
                for (int x = sx - (int) Math.round(half); x <= sx + (int) Math.round(half); x++) {
                    this.put(x, y, z, Math.abs(x - sx) == (int) Math.round(half) ? this.bone(Direction.Axis.Z) : Blocks.CALCITE.defaultBlockState());
                    if (z <= -8 || Math.abs(x - sx) < (int) Math.round(half)) {
                        this.boneAt(x, y + 1, z, Direction.Axis.Z);
                    }
                }
                if (z == -10 || z == -7) {
                    for (int side : new int[] {-1, 1}) {
                        int x = sx + side * (int) Math.round(half);
                        this.boneAt(x, y - 1, z, Direction.Axis.Y);
                    }
                }
            }
            // Hirnschädel mit Augenhöhlen
            for (int z = -3; z <= 0; z++) {
                for (int x = sx - 3; x <= sx + 3; x++) {
                    for (int y = 2; y <= 6; y++) {
                        boolean shell = Math.abs(x - sx) == 3 || y == 2 || y == 6 || z == -3 || z == 0;
                        boolean corner = Math.abs(x - sx) == 3 && (y == 2 || y == 6 || z == -3) || y == 6 && z == -3;
                        if (corner) {
                            continue;
                        }
                        if (shell) {
                            this.put(x, y, z, this.b.chance(x, y, z, 0.7f) ? this.bone(Direction.Axis.Y) : Blocks.CALCITE.defaultBlockState());
                        } else {
                            this.put(x, y, z, this.wet(x, y, z) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }
            for (int side : new int[] {-1, 1}) {
                // Augenhöhle: ein Loch in der Seite, dahinter ein Licht, darüber ein Brauenwulst
                this.put(sx + side * 3, 4, -2, this.wet(sx + side * 3, 4, -2) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
                this.put(sx + side * 2, 4, -2, Blocks.SEA_LANTERN.defaultBlockState());
                this.boneAt(sx + side * 3, 5, -3, Direction.Axis.Z);
                this.boneAt(sx + side * 2, 5, -4, Direction.Axis.Z);
                // Nüstern vorn an der Schnauze
                this.put(sx + side, 4, -10, this.wet(sx + side, 4, -10) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
                // Hörner: nach hinten und oben geschwungen, an der Spitze wieder abwärts
                double hx = sx + side * 2;
                double hy = 6;
                double hz = 0;
                for (int i = 0; i < 9; i++) {
                    this.boneAt((int) Math.round(hx), (int) Math.round(hy), (int) Math.round(hz), i < 3 ? Direction.Axis.Y : Direction.Axis.Z);
                    hx += side * 0.3;
                    hy += i < 3 ? 0.8 : i < 6 ? 0.25 : -0.3;
                    hz += 0.85;
                }
            }
        }

        // ---- Wirbelsäule und Rippen ----------------------------------------------------------------

        void spine() {
            for (int z = 0; z <= LENGTH; z++) {
                int x = (int) Math.round(this.spineX(z));
                int y = (int) Math.round(this.spineY(z));
                boolean tail = z > RIBS_TO;
                if (tail && this.b.chance(x, y, z, 0.12f)) {
                    continue;   // verlorene Wirbel
                }
                this.boneAt(x, y, z, Direction.Axis.Z);
                if (!tail && z % 2 == 0) {
                    // Wirbeldornen, über dem Brustkorb hoch
                    int h = z >= RIBS_FROM ? 2 + (z % 4 == 0 ? 1 : 0) : 1;
                    for (int d = 1; d <= h; d++) {
                        this.boneAt(x, y + d, z, Direction.Axis.Y);
                    }
                }
                if (tail && z % 3 == 0 && y >= 1) {
                    this.boneAt(x, y + 1, z, Direction.Axis.Y);
                }
                // was unter einem tief liegenden Wirbel frei ist, füllt Sand
                if (y <= 1) {
                    for (int d = y - 1; d >= -3; d--) {
                        if (this.b.get(x, d, z).isSolidRender()) {
                            break;
                        }
                        this.put(x, d, z, Blocks.SAND.defaultBlockState());
                    }
                }
            }
        }

        /** Rippenbögen: von der Wirbelsäule nach außen und unten bis in den Grund; einige gebrochen. */
        void ribs() {
            for (int z = RIBS_FROM; z <= RIBS_TO; z += 2) {
                double xc = this.spineX(z);
                double yc = this.spineY(z);
                double span = this.ribSpan(z);
                for (int side : new int[] {-1, 1}) {
                    double broken = this.b.chance(side, 0, z, 0.22f) ? 0.9 + this.b.noise(side, 1, z) * 0.5 : 9;
                    int lastX = Integer.MIN_VALUE;
                    int lastY = Integer.MIN_VALUE;
                    for (double t = 0.15; t <= 1.85; t += 0.05) {
                        if (t > broken) {
                            break;
                        }
                        int x = (int) Math.round(xc + side * span * Math.sin(t));
                        int y = (int) Math.round(yc * Math.cos(t * 0.92) - 0.4);
                        // die Rippen streichen nach hinten, je tiefer, desto weiter
                        int rz = z + (int) Math.round(t * t * 0.6);
                        if (x == lastX && y == lastY) {
                            continue;
                        }
                        lastX = x;
                        lastY = y;
                        this.boneAt(x, y, rz, t < 0.6 ? Direction.Axis.X : Direction.Axis.Y);
                        if (y <= 0) {
                            // die Spitze steckt im Grund
                            for (int d = y - 1; d >= y - 3; d--) {
                                if (this.b.get(x, d, rz).isSolidRender()) {
                                    break;
                                }
                                this.boneAt(x, d, rz, Direction.Axis.Y);
                            }
                            break;
                        }
                    }
                }
            }
        }

        // ---- Fracht --------------------------------------------------------------------------------

        /**
         * Was der Drache verschlang, liegt im Brustkorb auf einem Sandhügel: die Truhe des Kapitäns mit seiner Seekarte,
         * Vorräte in Fässern und einer Kiste, der Schatz halb im Sand, ein gebrochener Mast, Planken und die Glocke.
         */
        void cargo() {
            int x11 = (int) Math.round(this.spineX(11));
            int x15 = (int) Math.round(this.spineX(15));
            int x19 = (int) Math.round(this.spineX(19));
            // Sandhügel
            for (int z = 10; z <= 21; z++) {
                int xc = (int) Math.round(this.spineX(z));
                for (int x = xc - 3; x <= xc + 3; x++) {
                    float n = this.b.noise(x, 0, z);
                    if (n < 0.55f - Math.abs(x - xc) * 0.12f) {
                        this.put(x, 0, z, Blocks.SAND.defaultBlockState());
                    }
                }
            }
            this.b.chest(x11 - 1, 1, 11, this.waterlog(Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST), x11 - 1, 1, 11),
                    ModLootTables.SITE_SEA_DRAGON_MAP);
            this.put(x11 - 1, 0, 11, Blocks.SAND.defaultBlockState());
            this.b.chest(x15 + 1, 1, 15, this.waterlog(Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), x15 + 1, 1, 15),
                    ModLootTables.SITE_SEA_DRAGON_CARGO);
            this.put(x15 + 1, 0, 15, Blocks.SAND.defaultBlockState());
            this.put(x15 + 2, 1, 14, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.put(x15 + 2, 1, 16, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.EAST));
            this.put(x15 + 2, 0, 14, Blocks.SAND.defaultBlockState());
            this.put(x15 + 2, 0, 16, Blocks.SAND.defaultBlockState());
            // der Schatz, halb im Sand
            this.b.chest(x19, 0, 19, this.waterlog(Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH), x19, 0, 19),
                    ModLootTables.SITE_SEA_DRAGON_HOARD);
            this.put(x19 - 1, 0, 19, Blocks.SAND.defaultBlockState());
            this.put(x19 + 1, 0, 19, Blocks.SAND.defaultBlockState());
            this.put(x19, 0, 20, Blocks.SAND.defaultBlockState());
            if (this.b.chance(x19, 1, 20, 0.5f)) {
                this.put(x19, 1, 20, Blocks.SAND.defaultBlockState());
            }
            this.put(x19 - 1, -1, 19, Blocks.SANDSTONE.defaultBlockState());
            this.put(x19, -1, 19, Blocks.SANDSTONE.defaultBlockState());
            // der gebrochene Mast quer durch den Brustkorb
            for (int i = -4; i <= 3; i++) {
                int x = (int) Math.round(this.spineX(13) + i);
                int z = 12 + (i + 4) / 4;
                this.put(x, 1, z, Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            }
            // Planken, ein Laternchen, die Glocke
            for (int z = 10; z <= 21; z++) {
                int xc = (int) Math.round(this.spineX(z));
                for (int x = xc - 4; x <= xc + 4; x++) {
                    if (this.b.chance(x, 7, z, 0.06f) && this.isFree(x, 1, z)) {
                        this.put(x, 1, z, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
                    }
                }
            }
            if (this.isFree(x19 + 2, 1, 21)) {
                this.put(x19 + 2, 1, 21, Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT, BellAttachType.FLOOR)
                        .setValue(BellBlock.FACING, Direction.EAST));
                this.put(x19 + 2, 0, 21, Blocks.SAND.defaultBlockState());
            }
            this.put(x11 + 2, 1, 10, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            this.put(x11 + 2, 0, 10, Blocks.SAND.defaultBlockState());
        }

        boolean isFree(int x, int y, int z) {
            BlockState s = this.b.get(x, y, z);
            return s.isAir() || s.is(Blocks.WATER);
        }

        BlockState waterlog(BlockState state, int x, int y, int z) {
            return state.hasProperty(BlockStateProperties.WATERLOGGED) && this.wet(x, y, z) ? state.setValue(BlockStateProperties.WATERLOGGED, true)
                    : state;
        }

        /** Am Strand verweht Sand Teile des Gerippes, am Grund sammelt er sich an den Knochen. */
        void sand() {
            for (int z = -12; z <= LENGTH + 1; z++) {
                int xc = (int) Math.round(this.spineX(Math.max(0, z)));
                for (int x = xc - 9; x <= xc + 9; x++) {
                    if (!this.isFree(x, 0, z) || !this.b.get(x, -1, z).isSolidRender()) {
                        continue;
                    }
                    int near = 0;
                    for (Direction d : Direction.Plane.HORIZONTAL) {
                        if (this.b.get(x + d.getStepX(), 0, z + d.getStepZ()).is(Blocks.BONE_BLOCK)) {
                            near++;
                        }
                    }
                    if (near > 0 && this.b.chance(x, 0, z, this.sunken ? 0.35f : 0.6f)) {
                        this.put(x, 0, z, Blocks.SAND.defaultBlockState());
                    }
                }
            }
        }
    }
}
