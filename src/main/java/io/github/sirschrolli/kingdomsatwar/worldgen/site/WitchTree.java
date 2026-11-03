package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Der Hexenbaum: Im Sumpf steht ein riesiger, abgestorbener Baum mit knorrigen Wurzeln, die ins Wasser greifen. Sein
 * Stamm ist hohl – darin wohnt eine Hexe: Kessel und Braustand, Regale mit Fläschchen, Kräuter und leuchtende Beeren an
 * Ranken von der Decke, Kerzen, Pilze, eine Truhe mit allerlei Zutaten. Eine Leiter führt hinauf in die Krone, wo zwischen
 * kahlen Ästen Seelenlaternen hängen. Hier tauchen Hexen auf, und eine schwarze Katze streicht umher.
 *
 * <p>Eigene Koordinaten: Mitte des Stamms bei (0, 0, 0), y = 0 ist der Boden, die Tür im Süden.
 */
final class WitchTree {
    private WitchTree() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int[] span = terrain.span(x, z, 4, 4);
        if (span[1] - span[0] > 3 || terrain.waterDepth(x, z) > 2) {
            return Optional.empty();
        }
        int y = Math.max(terrain.ground(x, z), terrain.seaLevel() - 1) + 1;
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 7), 4)];
        BlockPos origin = new BlockPos(x, y, z);
        BoundingBox box = new BoundingBox(x - 12, y - 4, z - 12, x + 12, y + 22, z + 12);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.WITCH_TREE, 0, origin, rotation, seed, new int[0], box))));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Tree(b).build();
    }

    private static final class Tree {
        private final VillageBuilder b;

        Tree(VillageBuilder b) {
            this.b = b;
        }

        BlockState bark(int x, int y, int z) {
            float n = this.b.noise(x, y, z);
            return n < 0.55f ? Blocks.DARK_OAK_WOOD.defaultBlockState() : n < 0.8f ? Blocks.MANGROVE_WOOD.defaultBlockState()
                    : Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState();
        }

        /** Radius des Stamms auf Höhe y (unten breit, oben schmaler). */
        double radius(int y) {
            return y < 0 ? 3.6 : y < 3 ? 3.4 - y * 0.15 : Math.max(1.2, 3.0 - (y - 3) * 0.15);
        }

        void build() {
            this.trunk();
            this.roots();
            this.lair();
            this.crown();
        }

        /** Der Stamm: unten hohl (die Hütte), oben massiv, bis 16 hoch. */
        void trunk() {
            for (int y = -3; y <= 16; y++) {
                double r = this.radius(y);
                for (int x = -4; x <= 4; x++) {
                    for (int z = -4; z <= 4; z++) {
                        double d = Math.sqrt(x * x + z * z) + this.b.noise(x, y / 3, z) * 0.5;
                        if (d > r) {
                            continue;
                        }
                        boolean hollow = y >= 0 && y <= 5 && d < r - 1.0;
                        this.b.set(x, y, z, hollow ? Blocks.AIR.defaultBlockState() : this.bark(x, y, z));
                    }
                }
            }
            // Boden der Hütte aus Brettern, Tür im Süden
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.sqrt(x * x + z * z) < 2.6) {
                        this.b.set(x, -1, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
                    }
                }
            }
            for (int y = 0; y <= 2; y++) {
                for (int z = 2; z <= 4; z++) {
                    this.b.set(0, y, z, y == 2 && z == 4 ? this.bark(0, 2, 4) : Blocks.AIR.defaultBlockState());
                }
            }
            this.b.set(0, -1, 3, Blocks.SPRUCE_PLANKS.defaultBlockState());
            this.b.set(0, -1, 4, Blocks.SPRUCE_PLANKS.defaultBlockState());
            this.b.set(0, -1, 5, Blocks.SPRUCE_SLAB.defaultBlockState());
            // Pilze am Stamm
            for (int y = 1; y <= 10; y += 3) {
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    int r = (int) Math.ceil(this.radius(y));
                    if (this.b.chance(d.ordinal(), y, 9, 0.4f)) {
                        this.b.set(d.getStepX() * r, y, d.getStepZ() * r, Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState());
                    }
                }
            }
        }

        /** Knorrige Wurzeln, die schräg in Wasser und Schlamm greifen. */
        void roots() {
            for (int i = 0; i < 7; i++) {
                double a = i * Math.PI * 2 / 7 + this.b.noise(i, 0, 3) * 0.6;
                double x = Math.cos(a) * 3.0;
                double z = Math.sin(a) * 3.0;
                double y = 1.5;
                for (int step = 0; step < 6; step++) {
                    this.b.set((int) Math.round(x), (int) Math.round(y), (int) Math.round(z), Blocks.MANGROVE_ROOTS.defaultBlockState());
                    BlockState below = this.b.get((int) Math.round(x), (int) Math.round(y) - 1, (int) Math.round(z));
                    if (below.isAir()) {
                        this.b.set((int) Math.round(x), (int) Math.round(y) - 1, (int) Math.round(z), Blocks.MANGROVE_ROOTS.defaultBlockState());
                    }
                    x += Math.cos(a) * 1.0;
                    z += Math.sin(a) * 1.0;
                    y -= 0.55;
                }
            }
        }

        /** Die Hütte der Hexe im hohlen Stamm. */
        void lair() {
            this.b.set(0, 0, 0, Blocks.CAULDRON);
            this.b.set(0, -1, 0, Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, false));
            this.b.set(-2, 0, 0, Blocks.BREWING_STAND);
            this.b.set(2, 0, -1, Blocks.CHISELED_BOOKSHELF);
            this.b.set(2, 1, -1, Blocks.FLOWER_POT.defaultBlockState());
            this.b.set(-2, 0, -1, Blocks.BARREL);
            this.b.set(-1, 0, -2, Blocks.DYED_CANDLE.pick(DyeColor.PURPLE).defaultBlockState().setValue(CandleBlock.CANDLES, 3)
                    .setValue(CandleBlock.LIT, true));
            this.b.set(1, 0, -2, Blocks.DYED_CANDLE.pick(DyeColor.BLACK).defaultBlockState().setValue(CandleBlock.CANDLES, 2)
                    .setValue(CandleBlock.LIT, true));
            this.b.chest(2, 0, 1, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.SITE_WITCH_TREE);
            this.b.set(-2, 0, 1, Blocks.RED_MUSHROOM);
            this.b.set(-1, 0, 1, Blocks.CARPET.pick(DyeColor.PURPLE));
            // Kräuter und leuchtende Beeren an Ranken von der Decke
            for (int[] c : new int[][] {{-1, -1}, {1, 1}, {-1, 1}, {1, -1}}) {
                for (int y = 5; y >= 3; y--) {
                    boolean tip = y == 3;
                    this.b.set(c[0], y, c[1], (tip ? Blocks.CAVE_VINES : Blocks.CAVE_VINES_PLANT).defaultBlockState()
                            .setValue(CaveVines.BERRIES, this.b.chance(c[0], y, c[1], 0.6f)));
                }
            }
            // Leiter an der Nordwand hinauf in die Krone (durch einen Schacht im massiven Stamm)
            for (int y = 0; y <= 16; y++) {
                if (y > 5) {
                    this.b.set(0, y, -1, Blocks.AIR.defaultBlockState());
                }
                this.b.set(0, y, -3, this.bark(0, y, -3));
                this.b.set(0, y, -2, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
            }
        }

        /** Die Krone: kahle, verdrehte Äste mit Seelenlaternen, oben eine kleine Warte aus Brettern. */
        void crown() {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) + Math.abs(z) <= 3) {
                        this.b.set(x, 16, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
                    }
                }
            }
            this.b.set(0, 16, -1, Blocks.AIR.defaultBlockState());
            this.b.set(0, 16, -2, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
            this.b.set(0, 16, -3, this.bark(0, 16, -3));
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3 + this.b.noise(i, 16, 0) * 0.5;
                double x = Math.cos(a) * 1.5;
                double z = Math.sin(a) * 1.5;
                double y = 12 + this.b.noise(i, 12, 0) * 4;
                int len = 5 + (int) (this.b.noise(i, 13, 0) * 4);
                for (int s = 0; s < len; s++) {
                    int bx = (int) Math.round(x);
                    int by = (int) Math.round(y);
                    int bz = (int) Math.round(z);
                    this.b.set(bx, by, bz, Blocks.DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,
                            Math.abs(Math.cos(a)) > Math.abs(Math.sin(a)) ? Direction.Axis.X : Direction.Axis.Z));
                    x += Math.cos(a) * 1.1;
                    z += Math.sin(a) * 1.1;
                    y += 0.5;
                    if (s == len - 2) {
                        this.b.set(bx, by - 1, bz, Blocks.IRON_CHAIN);
                        this.b.set(bx, by - 2, bz, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
                    }
                }
            }
            // ein paar Fetzen Laub und Spinnweben
            for (int i = 0; i < 6; i++) {
                int x = (int) ((this.b.noise(i, 20, 1) - 0.5) * 9);
                int z = (int) ((this.b.noise(i, 21, 1) - 0.5) * 9);
                int y = 14 + (int) (this.b.noise(i, 22, 1) * 4);
                if (this.b.get(x, y, z).isAir()) {
                    this.b.set(x, y, z, i % 2 == 0 ? Blocks.COBWEB.defaultBlockState() : Blocks.MANGROVE_LEAVES.defaultBlockState()
                            .setValue(BlockStateProperties.PERSISTENT, true));
                }
            }
        }
    }
}
