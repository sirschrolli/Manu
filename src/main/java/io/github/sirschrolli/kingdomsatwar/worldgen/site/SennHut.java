package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.Cuisine;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Sennhütte (seit 2.0): Auf einer Bergwiese steht die Almhütte der Sennen – unten gemauert aus Feldstein, darüber ein
 * Blockbau aus Fichtenstämmen unter einem breiten, flachen Satteldach, dessen Giebel nach vorn zeigt; unter ihm ein Balkon
 * mit Blumen. Drinnen die Käseküche mit dem Kessel über dem Feuer, das Butterfass, Tisch und Bank, Bett und Vorräte, unter
 * dem Dach das Heulager. Unter der Stube der Käsekeller: zwei Regalreihen voller Käselaibe, Fässer und die Truhe der Sennen.
 * Draußen die Weide mit Kühen und einer Ziege, ein Brunnentrog aus Stämmen, Heu, die Glocke am Gatter und Wiesenblumen.
 *
 * <p>Eigene Koordinaten: Mitte der Hütte bei (0, 0, 0), y = 0 ist der Fußboden; vorn (+z) die Tür und der Balkon.
 */
final class SennHut {
    /** Die Hütte: x -5..5, z -4..3; der Keller darunter (Boden y -5, Decke y -1). */
    static final int X0 = -5, X1 = 5, Z0 = -4, Z1 = 3;
    static final int CELLAR = -5;
    static final BlockPos CHEST = new BlockPos(3, CELLAR, 0);
    static final double PAD = 13.5;

    private SennHut() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.SENN_HUT, "sennhuette", "Alpine Dairy Hut", "Sennhütte", 3, Wonders.Ground.LAND, false,
                SennHut::plan, SennHut::build, origin -> piece(origin, Rotation.NONE, 0x5E44L),
                new double[][] {Wonders.view(-13, 8, 16, 0, 4, 0), Wonders.view(17, 6, -6, 4, 1, -2), Wonders.view(-3, 1.6, 1.5, 2, 1, -2),
                        Wonders.view(-1, CELLAR + 1.6, 1, 3, CELLAR + 1, -2), Wonders.view(1, 6.6, 1, -3, 6, -3)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = Kit.land(context, terrain, 12, 5);
        return origin == null ? Optional.empty() : Kit.stub(piece(origin, Kit.rotation(seed), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        int r = (int) PAD + 5;
        return new SitePiece(SiteKind.SENN_HUT, 0, origin, rotation, seed, new int[0], SitePiece.box(origin, rotation, -r, CELLAR - 2, -r, r, 14, r));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Hut(b).build();
    }

    private static final class Hut {
        private final VillageBuilder b;
        private final Kit.Wood spruce = Kit.spruce();
        private final Kit.Wood dark = Kit.darkOak();

        Hut(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            Kit.pad(this.b, PAD, Kit.s(Blocks.GRASS_BLOCK), Kit.s(Blocks.DIRT), 14);
            this.path();
            this.cellar();
            this.walls();
            this.roof();
            this.inside();
            this.balcony();
            this.pasture();
            this.yard();
        }

        private BlockState stone(int x, int y, int z) {
            return Kit.weathered(this.b, x, y, z, Kit.s(Blocks.COBBLESTONE), Kit.s(Blocks.MOSSY_COBBLESTONE), 0.25f, Kit.s(Blocks.STONE), 0.2f);
        }

        private BlockState wheel(int x, int y) {
            Direction[] dirs = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
            return Cuisine.CHEESE_WHEEL.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, dirs[Math.floorMod(x * 3 + y * 7, 4)]);
        }

        /** Ein Pfad aus Kies und Trampelpfad zur Tür. */
        private void path() {
            for (int z = Z1 + 1; z <= 13; z++) {
                for (int x = -1; x <= 1; x++) {
                    if (x == 0 || this.b.chance(x, 40, z, 0.45f)) {
                        this.b.set(x, -1, z, this.b.chance(x, 41, z, 0.3f) ? Kit.s(Blocks.GRAVEL) : Kit.s(Blocks.DIRT_PATH));
                    }
                }
            }
        }

        /** Der Käsekeller unter der Stube: gemauert, zwei Regalreihen voller Käselaibe, Fässer, eine Laterne, die Truhe. */
        private void cellar() {
            for (int x = -4; x <= 4; x++) {
                for (int z = -3; z <= 2; z++) {
                    for (int y = CELLAR - 1; y <= -1; y++) {
                        boolean wall = x == -4 || x == 4 || z == -3 || z == 2 || y == CELLAR - 1 || y == -1;
                        this.b.set(x, y, z, wall ? this.stone(x, y, z) : Kit.s(Blocks.AIR));
                    }
                }
            }
            Kit.floor(this.b, -3, -2, 3, 1, CELLAR - 1, Kit.s(Blocks.STONE_BRICKS));
            for (int x = -3; x <= 3; x++) {
                for (int y : new int[] {CELLAR, CELLAR + 2}) {
                    this.b.set(x, y, -2, this.spruce.slab(SlabType.TOP));
                    this.b.set(x, y + 1, -2, this.wheel(x, y));
                }
            }
            for (int z = -1; z <= 0; z++) {
                this.b.set(-3, CELLAR, z, this.spruce.slab(SlabType.TOP));
                this.b.set(-3, CELLAR + 1, z, this.wheel(-3, z));
            }
            Kit.chest(this.b, CHEST.getX(), CHEST.getY(), CHEST.getZ(), Direction.WEST, Kit.loot("senn_hut"));
            Kit.barrel(this.b, 3, CELLAR, 1, Direction.UP);
            Kit.barrel(this.b, 2, CELLAR, 1, Direction.WEST);
            Kit.barrel(this.b, 3, CELLAR + 1, 1, Direction.UP);
            this.b.hangingLantern(0, -2, 0, Blocks.LANTERN);
        }

        /** Unten zwei Lagen Feldstein, darüber der Blockbau aus liegenden Fichtenstämmen, deren Enden an den Ecken überstehen. */
        private void walls() {
            for (int y = 0; y <= 5; y++) {
                for (int x = X0; x <= X1; x++) {
                    for (int z = Z0; z <= Z1; z++) {
                        boolean edgeX = x == X0 || x == X1;
                        boolean edgeZ = z == Z0 || z == Z1;
                        if (!edgeX && !edgeZ) {
                            if (y > 0) {
                                this.b.set(x, y, z, Kit.s(Blocks.AIR));
                            }
                            continue;
                        }
                        BlockState state = y <= 1 ? this.stone(x, y, z) : edgeX && edgeZ ? this.spruce.logY()
                                : this.spruce.log(edgeZ ? Direction.Axis.X : Direction.Axis.Z);
                        this.b.set(x, y, z, state);
                    }
                }
            }
            for (int y = 2; y <= 5; y++) {
                for (int[] c : new int[][] {{X0, Z0}, {X1, Z0}, {X0, Z1}, {X1, Z1}}) {
                    if (y % 2 == 0) {
                        this.b.set(c[0] + Integer.signum(c[0]), y, c[1], this.spruce.log(Direction.Axis.X));
                    } else {
                        this.b.set(c[0], y, c[1] + Integer.signum(c[1]), this.spruce.log(Direction.Axis.Z));
                    }
                }
            }
            Kit.floor(this.b, X0 + 1, Z0 + 1, X1 - 1, Z1 - 1, -1, Kit.s(Blocks.SPRUCE_PLANKS));
            Kit.floor(this.b, X0 + 1, Z0 + 1, X1 - 1, Z1 - 1, 0, Kit.s(Blocks.AIR));
            // die Luke zum Keller mit der Leiter
            this.b.ladder(-2, CELLAR, -1, 1, Direction.NORTH);
            this.b.set(-2, 0, 1, Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.BOTTOM));
            // Tür, Fenster mit Läden und Blumenkästen
            this.b.door(0, 0, Z1, Blocks.SPRUCE_DOOR, Direction.NORTH);
            for (int x : new int[] {-3, 3}) {
                Kit.window(this.b, x, 2, Z1, 2, Kit.s(Blocks.GLASS_PANE));
                for (int y = 2; y <= 3; y++) {
                    Kit.shutter(this.b, x - 1, y, Z1 + 1, Blocks.SPRUCE_TRAPDOOR, Direction.SOUTH);
                    Kit.shutter(this.b, x + 1, y, Z1 + 1, Blocks.SPRUCE_TRAPDOOR, Direction.SOUTH);
                }
                this.b.set(x, 1, Z1 + 1, this.spruce.slab(SlabType.TOP));
                this.b.set(x, 2, Z1 + 1, Kit.s(x < 0 ? Blocks.POTTED_RED_TULIP : Blocks.POTTED_POPPY));
            }
            for (int z : new int[] {-2, 1}) {
                Kit.window(this.b, X1, 2, z, 2, Kit.s(Blocks.GLASS_PANE));
            }
            Kit.window(this.b, X0, 2, 1, 2, Kit.s(Blocks.GLASS_PANE));
            Kit.window(this.b, 2, 2, Z0, 2, Kit.s(Blocks.GLASS_PANE));
        }

        /** Das Dach: flach (ein halber Block je Block), First entlang z, vorn und hinten zwei Blöcke Überstand. */
        private void roof() {
            int zf = Z0 - 2;
            int zb = Z1 + 2;
            for (int i = 0; i <= 6; i++) {
                int y = 6 + i / 2;
                Half half = i % 2 == 0 ? Half.BOTTOM : Half.TOP;
                int xl = X0 - 1 + i;
                int xr = X1 + 1 - i;
                for (int z = zf; z <= zb; z++) {
                    if (xl == xr) {
                        this.b.set(xl, y - 1, z, Kit.s(Blocks.DARK_OAK_PLANKS));
                        this.b.set(xl, y, z, Kit.s(Blocks.MOSSY_COBBLESTONE_SLAB));
                    } else {
                        this.b.set(xl, y, z, VillageBuilder.stairs(this.dark.stairs(), Direction.EAST, half));
                        this.b.set(xr, y, z, VillageBuilder.stairs(this.dark.stairs(), Direction.WEST, half));
                    }
                }
                // die Giebel vorn und hinten: senkrechte Bretter bis unter das Dach
                for (int x = xl + 1; x < xr; x++) {
                    for (int z : new int[] {Z0, Z1}) {
                        for (int yy = 6; yy <= y - (half == Half.BOTTOM ? 1 : 0) && yy < 9; yy++) {
                            this.b.set(x, yy, z, Kit.s(Blocks.STRIPPED_SPRUCE_LOG));
                        }
                    }
                }
            }
            // der Kamin aus Feldstein, oben raucht es
            for (int y = 0; y <= 9; y++) {
                this.b.set(X0, y, -2, this.stone(X0, y, -2));
                this.b.set(X0 - 1, y, -2, this.stone(X0 - 1, y, -2));
            }
            this.b.set(X0 - 1, 10, -2, Kit.s(Blocks.CAMPFIRE));
            this.b.set(X0, 10, -2, Kit.s(Blocks.COBBLESTONE_SLAB));
            Kit.window(this.b, 0, 6, Z0, 2, Kit.s(Blocks.GLASS_PANE));
        }

        /** Die Stube: Käseküche im Westen (Kessel über dem Feuer, Butterfass), Tisch und Bank, Bett, Regal, Räucherofen; oben Heu. */
        private void inside() {
            // die Feuerstelle: gemauert, der Kessel über dem Feuer
            for (int z = -3; z <= -1; z++) {
                this.b.set(-4, 0, z, Kit.s(Blocks.STONE_BRICKS));
                this.b.set(-4, 2, z, Kit.s(Blocks.STONE_BRICK_SLAB));
            }
            this.b.set(-4, 0, -2, Kit.s(Blocks.CAMPFIRE));
            this.b.set(-4, 1, -2, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
            this.b.set(-4, 1, -3, Kit.s(Blocks.STONE_BRICKS));
            this.b.set(-4, 1, -1, Kit.s(Blocks.STONE_BRICKS));
            // Butterfass: ein Fass mit dem Stößel
            Kit.barrel(this.b, -4, 0, 1, Direction.UP);
            this.b.set(-4, 1, 1, this.spruce.fence());
            // Tisch mit Bank, darauf ein Käselaib und eine Kerze
            for (int x = 0; x <= 2; x++) {
                this.b.set(x, 0, -2, this.dark.slab(SlabType.TOP));
                this.b.set(x, 0, -1, VillageBuilder.stairs(this.spruce.stairs(), Direction.SOUTH, Half.BOTTOM));
            }
            this.b.set(-1, 0, -2, VillageBuilder.stairs(this.spruce.stairs(), Direction.WEST, Half.BOTTOM));
            this.b.set(1, 1, -2, this.wheel(1, 1));
            Kit.candles(this.b, 2, 1, -2, Blocks.CANDLE, 2, false);
            // Bett, Regal, Räucherofen, die Vorräte
            this.b.bed(4, 0, 1, Blocks.BED.pick(net.minecraft.world.item.DyeColor.RED), Direction.NORTH);
            this.b.set(4, 0, -3, Kit.s(Blocks.SPRUCE_SHELF));
            Kit.facing(this.b, 3, 0, -3, Blocks.SMOKER, Direction.SOUTH);
            Kit.lootBarrel(this.b, 4, 0, -2, Kit.loot("senn_hut_pantry"));
            this.b.set(-3, 0, -3, Kit.s(Blocks.CRAFTING_TABLE));
            // das Heulager unter dem Dach: Bretterboden, eine Leiter hinauf, Heu
            for (int x = X0 + 1; x <= X1 - 1; x++) {
                for (int z = Z0 + 1; z <= Z1 - 1; z++) {
                    if (!(x == -1 && z == 2)) {
                        this.b.set(x, 5, z, Kit.s(Blocks.SPRUCE_PLANKS));
                    }
                }
            }
            this.b.ladder(-1, 0, 5, 2, Direction.NORTH);
            this.b.hangingLantern(1, 4, 0, Blocks.LANTERN);
            for (int[] h : new int[][] {{2, -3}, {3, -3}, {3, -2}, {-3, -3}, {-3, -2}}) {
                this.b.set(h[0], 6, h[1], Kit.s(Blocks.HAY_BLOCK));
            }
        }

        /** Der Balkon unter dem Giebel: Bretter, Geländer aus Zäunen mit Blumen darauf, Stützen; die Tür aus dem Heulager. */
        private void balcony() {
            for (int x = -3; x <= 3; x++) {
                for (int z = Z1 + 1; z <= Z1 + 2; z++) {
                    this.b.set(x, 5, z, this.spruce.slab(SlabType.TOP));
                }
                this.b.set(x, 6, Z1 + 2, this.spruce.fence());
            }
            for (int x : new int[] {-3, 3}) {
                this.b.set(x, 6, Z1 + 1, this.spruce.fence());
                for (int y = 0; y <= 4; y++) {
                    this.b.set(x, y, Z1 + 2, this.spruce.fence());
                }
            }
            for (int x = -2; x <= 2; x += 2) {
                this.b.set(x, 7, Z1 + 2, Kit.s(x == 0 ? Blocks.POTTED_BLUE_ORCHID : Blocks.POTTED_RED_TULIP));
            }
            this.b.door(0, 6, Z1, Blocks.SPRUCE_DOOR, Direction.SOUTH);
            // Bänke neben der Tür unten
            this.b.set(-2, 0, Z1 + 1, VillageBuilder.stairs(this.spruce.stairs(), Direction.NORTH, Half.BOTTOM));
            this.b.set(2, 0, Z1 + 1, VillageBuilder.stairs(this.spruce.stairs(), Direction.NORTH, Half.BOTTOM));
            this.b.set(1, 1, Z1 + 1, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            this.b.set(1, 0, Z1 + 1, this.spruce.fence());
        }

        /** Die Weide im Osten: Zaun mit Gatter, die Glocke darüber, Heuraufe, Brunnentrog, Kühe und eine Ziege. */
        private void pasture() {
            int x0 = 7;
            int x1 = 11;
            int z0 = -7;
            int z1 = 3;
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    if (x == x0 || x == x1 || z == z0 || z == z1) {
                        this.b.set(x, 0, z, this.spruce.fence());
                    }
                }
            }
            this.b.set(x0, 0, 0, this.spruce.gate().defaultBlockState().setValue(FenceGateBlock.FACING, Direction.EAST));
            for (int z : new int[] {-1, 1}) {
                this.b.set(x0, 1, z, this.spruce.logY());
                this.b.set(x0, 2, z, this.spruce.logY());
            }
            this.b.set(x0, 2, 0, this.spruce.log(Direction.Axis.Z));
            this.b.set(x0, 1, 0, Blocks.BELL.defaultBlockState().setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING)
                    .setValue(BellBlock.FACING, Direction.EAST));
            // Heuraufe
            this.b.set(10, 0, -6, Kit.s(Blocks.HAY_BLOCK));
            this.b.set(9, 0, -6, Kit.s(Blocks.HAY_BLOCK));
            this.b.set(10, 1, -6, Kit.s(Blocks.HAY_BLOCK));
            // der Brunnentrog: Wasser in einem ausgehöhlten Stamm
            for (int z = -4; z <= -2; z++) {
                this.b.set(9, -1, z, Kit.s(Blocks.WATER));
                this.b.set(9, -2, z, Kit.s(Blocks.STRIPPED_SPRUCE_LOG));
                this.b.set(8, -1, z, this.spruce.log(Direction.Axis.Z));
                this.b.set(10, -1, z, this.spruce.log(Direction.Axis.Z));
            }
            this.b.set(9, -1, -5, this.spruce.log(Direction.Axis.X));
            this.b.set(9, -1, -1, this.spruce.log(Direction.Axis.X));
            Kit.mob(this.b, ModEntities.CATTLE.get(), 8, 0, -6, Direction.WEST);
            Kit.mob(this.b, ModEntities.CATTLE.get(), 10, 0, 1, Direction.SOUTH);
            Kit.mob(this.b, ModEntities.FARM_GOAT.get(), 8, 0, 2, Direction.NORTH);
        }

        /** Der Hof: Heuhaufen, ein Hackklotz mit Scheiten, Wiesenblumen. */
        private void yard() {
            for (int[] h : new int[][] {{-8, -4}, {-8, -3}, {-9, -4}}) {
                this.b.set(h[0], 0, h[1], Kit.s(Blocks.HAY_BLOCK));
            }
            this.b.set(-8, 1, -4, Kit.s(Blocks.HAY_BLOCK));
            this.b.set(-8, 0, 2, Kit.s(Blocks.STRIPPED_SPRUCE_LOG));
            for (int z = 4; z <= 6; z++) {
                this.b.set(-9, 0, z, this.spruce.log(Direction.Axis.Z));
            }
            this.b.set(-9, 1, 5, this.spruce.log(Direction.Axis.Z));
            for (int[] f : new int[][] {{-6, 6}, {-9, 8}, {5, 8}, {-7, 10}, {3, 10}, {-11, 0}, {6, 6}, {-4, 9}, {11, 7}, {-12, -6}}) {
                Kit.plant(this.b, f[0], 0, f[1], Kit.s(this.b.chance(f[0], 2, f[1], 0.5f) ? Blocks.CORNFLOWER : Blocks.OXEYE_DAISY));
            }
            for (int[] f : new int[][] {{-11, 5}, {-5, 12}, {8, 9}, {4, 12}, {-12, -2}}) {
                Kit.plant(this.b, f[0], 0, f[1], Kit.s(Blocks.WILDFLOWERS));
            }
        }
    }
}
