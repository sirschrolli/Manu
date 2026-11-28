package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Warzenbrauerei (seit 2.0): Ein Brauhaus aus Netherziegeln mit Pfeilern aus roten Ziegeln und einem Walmdach. Unten das
 * Sudhaus – drei Kessel über glühendem Magma, Braustände auf der Theke, Regale voller Fässer –, oben die Stube des
 * Braumeisters mit seiner Truhe; darunter der Gärkeller mit langen Fassreihen. Ringsum Felder mit Netherwarzen auf
 * Seelensand, unter dem Vordach eine Schänke. Borstlinge brauen hier.
 *
 * <p>Eigene Koordinaten: y = 0 ist der Boden des Sudhauses (Fußboden y = -1); vorn (+z) die Tür und die Schänke.
 */
final class WartBrewery {
    static final int X0 = -6, X1 = 4, Z0 = -6, Z1 = 2;
    static final int CELLAR = -5;
    static final BlockPos CHEST = new BlockPos(3, 5, -5);
    static final BlockPos CELLAR_CHEST = new BlockPos(-5, CELLAR, -5);
    static final double PAD = 12.5;

    private WartBrewery() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.WART_BREWERY, "warzenbrauerei", "Wart Brewery", "Warzenbrauerei", 3, Wonders.Ground.LAND, false,
                WartBrewery::plan, WartBrewery::build, origin -> piece(origin, Rotation.NONE, 0x3AB7L),
                new double[][] {Wonders.view(-15, 10, 17, -1, 3, -2), Wonders.view(16, 7, 10, 8, 0, 0), Wonders.view(-4, 1.6, 1, 2, 1, -4),
                        Wonders.view(-2, 6.6, 1, 2, 5.5, -5), Wonders.view(0, CELLAR + 1.6, 0, -4, CELLAR + 1, -5), Wonders.view(2, 1.6, 7, -2, 1, 3)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = Kit.cavern(context, terrain, seed, 11, 4, 12);
        return origin == null ? Optional.empty() : Kit.stub(piece(origin, Kit.rotation(seed), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        int r = (int) PAD + 5;
        return new SitePiece(SiteKind.WART_BREWERY, 0, origin, rotation, seed, new int[0], SitePiece.box(origin, rotation, -r, CELLAR - 2, -r, r, 14, r));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Brewery(b).build();
    }

    private static final class Brewery {
        private final VillageBuilder b;
        private final Kit.Wood crimson = Kit.Wood.vanilla("crimson", true);

        Brewery(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            Kit.pad(this.b, PAD, Kit.s(Blocks.NETHERRACK), Kit.s(Blocks.NETHERRACK), 12);
            this.cellar();
            this.house();
            this.brewhouse();
            this.upstairs();
            this.fields();
            this.tavern();
        }

        private BlockState brick(int x, int y, int z) {
            return this.b.chance(x, y, z, 0.18f) ? Kit.s(Blocks.CRACKED_NETHER_BRICKS) : Kit.s(Blocks.NETHER_BRICKS);
        }

        /** Der Gärkeller unter dem Sudhaus: lange Reihen liegender Fässer, die Truhe, eine Seelenlaterne. */
        private void cellar() {
            for (int x = X0; x <= X1; x++) {
                for (int z = Z0; z <= Z1; z++) {
                    for (int y = CELLAR - 1; y <= -1; y++) {
                        boolean shell = x == X0 || x == X1 || z == Z0 || z == Z1 || y == CELLAR - 1 || y == -1;
                        this.b.set(x, y, z, shell ? this.brick(x, y, z) : Kit.s(Blocks.AIR));
                    }
                }
            }
            for (int z = Z0 + 1; z <= Z1 - 1; z++) {
                for (int y = CELLAR; y <= CELLAR + 1; y++) {
                    if (z != -2) {
                        Kit.barrel(this.b, X0 + 1, y, z, Direction.EAST);
                        Kit.barrel(this.b, X1 - 1, y, z, Direction.WEST);
                    }
                }
            }
            Kit.chest(this.b, CELLAR_CHEST.getX(), CELLAR_CHEST.getY(), CELLAR_CHEST.getZ(), Direction.SOUTH, Kit.loot("wart_brewery_cellar"));
            Kit.lootBarrel(this.b, X1 - 1, CELLAR, -2, Kit.loot("wart_brewery_cellar"));
            this.b.hangingLantern(-1, -2, -2, Blocks.SOUL_LANTERN);
            this.b.ladder(0, CELLAR, -1, Z1 - 1, Direction.NORTH);
        }

        /** Das Haus: Netherziegel, Pfeiler aus roten Ziegeln, zwei Geschosse, Walmdach; Tür und Fenster mit Gittern. */
        private void house() {
            for (int x = X0; x <= X1; x++) {
                for (int z = Z0; z <= Z1; z++) {
                    boolean edgeX = x == X0 || x == X1;
                    boolean edgeZ = z == Z0 || z == Z1;
                    boolean pillar = (edgeX && edgeZ) || (edgeZ && (x - X0) % 5 == 0) || (edgeX && (z - Z0) % 4 == 0);
                    for (int y = 0; y <= 8; y++) {
                        BlockState state = Kit.s(Blocks.AIR);
                        if (edgeX || edgeZ) {
                            state = pillar ? Kit.s(Blocks.RED_NETHER_BRICKS) : y == 4 ? Kit.s(Blocks.CHISELED_NETHER_BRICKS) : this.brick(x, y, z);
                        } else if (y == 4) {
                            state = Kit.s(Blocks.CRIMSON_PLANKS);
                        }
                        this.b.set(x, y, z, state);
                    }
                    if (!edgeX && !edgeZ && !(x == 0 && z == Z1 - 1)) {
                        this.b.set(x, -1, z, Kit.s(Blocks.CRIMSON_PLANKS));
                    }
                }
            }
            this.b.hipRoof(X0 - 1, Z0 - 1, X1 + 1, Z1 + 1, 9, Blocks.RED_NETHER_BRICK_STAIRS, Kit.s(Blocks.RED_NETHER_BRICK_SLAB));
            for (int x = X0; x <= X1; x++) {
                for (int z = Z0; z <= Z1; z++) {
                    this.b.set(x, 9, z, Kit.s(Blocks.NETHER_BRICKS));
                }
            }
            // Tür vorn, Fenster mit Gittern (unten) und Scheiben (oben)
            this.b.door(-1, 0, Z1, Blocks.CRIMSON_DOOR, Direction.NORTH);
            for (int x : new int[] {-4, 2}) {
                Kit.window(this.b, x, 1, Z1, 2, Kit.s(Blocks.IRON_BARS));
                Kit.window(this.b, x, 6, Z1, 1, Kit.s(Blocks.GLASS_PANE));
            }
            for (int z : new int[] {-4, 0}) {
                Kit.window(this.b, X0, 1, z, 2, Kit.s(Blocks.IRON_BARS));
                Kit.window(this.b, X1, 6, z, 1, Kit.s(Blocks.GLASS_PANE));
            }
            // der Schornstein über den Kesseln
            for (int y = 5; y <= 11; y++) {
                this.b.set(X0 + 1, y, Z0, Kit.s(Blocks.RED_NETHER_BRICKS));
            }
            Kit.campfire(this.b, X0 + 1, 12, Z0, Blocks.CAMPFIRE, true);
            // Leiter nach oben
            this.b.ladder(X1 - 1, 0, 4, Z1 - 1, Direction.NORTH);
            this.b.set(X1 - 1, 4, Z1 - 1, Kit.s(Blocks.AIR));
        }

        /** Das Sudhaus: drei Kessel über Magma, die Theke mit Brauständen, Regale, Werkbänke, eine Hängelaterne. */
        private void brewhouse() {
            for (int x : new int[] {-5, -3}) {
                this.b.set(x, -1, -5, Kit.s(Blocks.MAGMA_BLOCK));
                this.b.set(x, 0, -5, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
            }
            this.b.set(-1, -1, -5, Kit.s(Blocks.MAGMA_BLOCK));
            this.b.set(-1, 0, -5, Kit.s(Blocks.LAVA_CAULDRON));
            for (int x = -5; x <= -1; x += 2) {
                for (int y = 1; y <= 3; y++) {
                    this.b.set(x, y, -5, Kit.s(Blocks.IRON_CHAIN));
                }
            }
            // die Theke mit den Brauständen
            for (int z = -3; z <= 0; z++) {
                this.b.set(3, 0, z, VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_SLAB, SlabType.TOP));
            }
            this.b.set(3, 1, -3, Kit.s(Blocks.BREWING_STAND));
            this.b.set(3, 1, -1, Kit.s(Blocks.BREWING_STAND));
            this.b.set(3, 1, 0, Kit.s(Blocks.POTTED_CRIMSON_FUNGUS));
            // Regale voller Fässer an der Westwand, die Werkbank, ein Fass mit Beute
            for (int z = -2; z <= 0; z++) {
                Kit.barrel(this.b, -5, 0, z, Direction.EAST);
                Kit.barrel(this.b, -5, 1, z, Direction.EAST);
                this.b.set(-5, 2, z, VillageBuilder.slab(Blocks.CRIMSON_SLAB, SlabType.BOTTOM));
            }
            Kit.lootBarrel(this.b, -5, 2, -1, Kit.loot("wart_brewery_cellar"));
            this.b.set(1, 0, -5, Kit.s(Blocks.CRAFTING_TABLE));
            this.b.set(2, 0, -5, Kit.s(Blocks.CRIMSON_SHELF));
            this.b.hangingLantern(-1, 3, -2, Blocks.LANTERN);
            Kit.mob(this.b, ModEntities.PIGLIN.get(), 1, 0, -2, Direction.EAST);
        }

        /** Oben: die Stube des Braumeisters – Bett, Truhe, Schreibpult, Regale mit Büchern über das Brauen, Fässer. */
        private void upstairs() {
            this.b.bed(-5, 5, -4, Blocks.BED.pick(net.minecraft.world.item.DyeColor.RED), Direction.NORTH);
            Kit.chest(this.b, CHEST.getX(), CHEST.getY(), CHEST.getZ(), Direction.WEST, Kit.loot("wart_brewery"));
            this.b.set(-2, 5, -5, Kit.s(Blocks.BOOKSHELF));
            this.b.set(-1, 5, -5, Kit.s(Blocks.BOOKSHELF));
            this.b.set(0, 5, -5, Kit.s(Blocks.LECTERN));
            Kit.chair(this.b, 0, 5, -4, this.crimson, Direction.NORTH);
            Kit.barrel(this.b, 0, 5, 1, Direction.UP);
            Kit.barrel(this.b, -1, 5, 1, Direction.UP);
            Kit.candles(this.b, -2, 6, -5, Blocks.CANDLE, 3, true);
            this.b.hangingLantern(-1, 8, -1, Blocks.LANTERN);
        }

        /** Zwei Warzenfelder auf Seelensand (eingefasst, Seelenfackeln an den Ecken) und ein drittes, abgeerntetes. */
        private void fields() {
            int[][] fields = {{6, -6, 10, -2}, {6, 0, 10, 4}, {-11, -6, -8, 1}};
            for (int f = 0; f < fields.length; f++) {
                int[] r = fields[f];
                for (int x = r[0]; x <= r[2]; x++) {
                    for (int z = r[1]; z <= r[3]; z++) {
                        boolean rim = x == r[0] || x == r[2] || z == r[1] || z == r[3];
                        if (rim) {
                            this.b.set(x, -1, z, Kit.s(Blocks.NETHER_BRICKS));
                            this.b.set(x, 0, z, VillageBuilder.slab(Blocks.NETHER_BRICK_SLAB, SlabType.BOTTOM));
                        } else {
                            this.b.set(x, -1, z, Kit.s(Blocks.SOUL_SAND));
                            int age = f == 2 ? Math.floorMod(x + z, 2) : Math.floorMod(x * 7 + z * 3, 5) == 0 ? 2 : 3;
                            this.b.set(x, 0, z, Blocks.NETHER_WART.defaultBlockState().setValue(NetherWartBlock.AGE, age));
                        }
                    }
                }
                for (int[] c : new int[][] {{r[0], r[1]}, {r[2], r[1]}, {r[0], r[3]}, {r[2], r[3]}}) {
                    this.b.set(c[0], 0, c[1], Kit.s(Blocks.NETHER_BRICK_FENCE));
                    this.b.set(c[0], 1, c[1], Kit.s(Blocks.SOUL_TORCH));
                }
            }
        }

        /** Die Schänke unter dem Vordach: Pfosten, Tische mit Stühlen, ein Fass als Ausschank, Laternen. */
        private void tavern() {
            for (int x = X0; x <= X1; x++) {
                for (int z = Z1 + 1; z <= Z1 + 4; z++) {
                    this.b.set(x, -1, z, Kit.s(this.b.chance(x, 3, z, 0.3f) ? Blocks.POLISHED_BLACKSTONE : Blocks.POLISHED_BLACKSTONE_BRICKS));
                }
                this.b.set(x, 4, Z1 + 4, VillageBuilder.stairs(Blocks.RED_NETHER_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
                for (int z = Z1 + 1; z <= Z1 + 3; z++) {
                    this.b.set(x, 4, z, VillageBuilder.slab(Blocks.RED_NETHER_BRICK_SLAB, SlabType.TOP));
                }
            }
            for (int x : new int[] {X0, X1}) {
                for (int y = 0; y <= 3; y++) {
                    this.b.set(x, y, Z1 + 4, this.crimson.fence().defaultBlockState());
                }
            }
            for (int[] t : new int[][] {{-4, Z1 + 2}, {2, Z1 + 2}}) {
                Kit.table(this.b, t[0], 0, t[1], this.crimson);
                Kit.chair(this.b, t[0] - 1, 0, t[1], this.crimson, Direction.EAST);
                Kit.chair(this.b, t[0] + 1, 0, t[1], this.crimson, Direction.WEST);
            }
            Kit.barrel(this.b, -1, 0, Z1 + 3, Direction.SOUTH);
            this.b.set(-1, 1, Z1 + 3, Kit.s(Blocks.POTTED_WARPED_FUNGUS));
            this.b.hangingLantern(-4, 3, Z1 + 2, Blocks.LANTERN);
            this.b.hangingLantern(2, 3, Z1 + 2, Blocks.LANTERN);
            Kit.mob(this.b, ModEntities.PIGLIN.get(), 0, 0, Z1 + 2, Direction.SOUTH);
            // vor der Schänke Laternen auf Pfosten
            for (int x : new int[] {X0 - 2, X1 + 2}) {
                this.b.set(x, 0, Z1 + 5, Kit.s(Blocks.NETHER_BRICK_FENCE));
                this.b.set(x, 1, Z1 + 5, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            }
        }
    }
}
