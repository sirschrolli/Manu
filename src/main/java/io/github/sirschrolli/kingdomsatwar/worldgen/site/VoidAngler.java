package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Der Leerenangler (seit 2.0): eine Fischerhütte an der Kante einer Insel, davor ein Steg aus Fichtenbohlen weit hinaus über
 * die Leere. An seinem Ende eine Plattform mit Bank, Köderkiste und einem Galgen, von dem eine Kette mit einer Seelenlaterne
 * tief ins Nichts hängt – hier angelt man Leerensalm, Chorusfisch und Leerenrochen. Unter dem Steg hängen Laternen an den
 * Querbalken. In der Hütte Bett, Räucherofen und die Truhe des Anglers.
 *
 * <p>Eigene Koordinaten: +z zeigt ins Leere; y = 0 ist der Boden von Hütte und Steg (Bohlen y = -1).
 */
final class VoidAngler {
    static final int X0 = -3;
    static final int X1 = 3;
    static final int Z0 = -7;
    static final int Z1 = -2;
    /** Hier beginnt die Plattform am Ende des Stegs … */
    static final int DECK = 16;
    /** … und hier endet sie. */
    static final int END = 20;
    static final BlockPos CHEST = new BlockPos(2, 0, -6);
    static final BlockPos CATCH = new BlockPos(2, 0, 17);

    private VoidAngler() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.VOID_ANGLER, "leerenangler", "Void Angler", "Leerenangler", 3, Wonders.Ground.EDGE, false,
                VoidAngler::plan, VoidAngler::build, origin -> piece(origin, Rotation.NONE, 0x7A21L),
                new double[][] {Wonders.view(-15, 7, 9, 0, 0, 7), Wonders.view(7, 4, 29, 0, 2, -3), Wonders.view(0.5, 1.6, 5, 0.5, 0.5, 22),
                        Wonders.view(-1.5, 1.6, -3, 1.5, 0.5, -6), Wonders.view(9, -7, 13, 0, -2, 9)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        Kit.Edge edge = Kit.voidEdge(context, terrain, seed, 5, 3, 10);
        return edge == null ? Optional.empty() : Kit.stub(piece(edge.origin(), edge.rotation(), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        return new SitePiece(SiteKind.VOID_ANGLER, 0, origin, rotation, seed, new int[0], SitePiece.box(origin, rotation, -7, -18, -11, 7, 10, 25));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Hut(b).build();
    }

    private static final class Hut {
        private final VillageBuilder b;
        private final Kit.Wood dark = Kit.darkOak();
        private final Kit.Wood spruce = Kit.spruce();

        Hut(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            Kit.rect(this.b, X0 - 1, Z0 - 1, X1 + 1, Z1 + 1, Kit.s(Blocks.END_STONE), Kit.s(Blocks.END_STONE), 9);
            this.hut();
            this.inside();
            this.porch();
            this.jetty();
            this.deck();
            this.gallows();
            Kit.mob(this.b, ModEntities.ENDERMAN.get(), -5, 0, -1, Direction.EAST);
            Kit.mob(this.b, ModEntities.ENDERMITE.get(), 0, 0, -5, Direction.SOUTH);
        }

        /** Ein Pfosten unter dem Steg: bis zum Fels hinab, wenn er nah genug ist; sonst hängt dort eine Laterne am Balken. */
        private void post(int x, int z) {
            int ground = Integer.MIN_VALUE;
            for (int y = -2; y >= -12; y--) {
                if (VillageBuilder.isSolidGround(this.b.get(x, y, z))) {
                    ground = y;
                    break;
                }
            }
            if (ground != Integer.MIN_VALUE) {
                for (int y = -2; y > ground; y--) {
                    this.b.set(x, y, z, this.dark.logY());
                }
            } else {
                this.b.set(x, -2, z, this.dark.fence());
                this.b.set(x, -3, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
        }

        /** Die Hütte: Pfosten aus Schwarzeiche, Wände aus Fichte, ein Dach aus Purpur, Fenster aus violettem Glas. */
        private void hut() {
            for (int y = 0; y <= 4; y++) {
                for (int x = X0; x <= X1; x++) {
                    for (int z = Z0; z <= Z1; z++) {
                        boolean edgeX = x == X0 || x == X1;
                        boolean edgeZ = z == Z0 || z == Z1;
                        if (!edgeX && !edgeZ) {
                            this.b.set(x, y, z, Kit.s(Blocks.AIR));
                            continue;
                        }
                        BlockState state;
                        if (edgeX && edgeZ) {
                            state = this.dark.logY();
                        } else if (y == 4) {
                            state = this.dark.log(edgeZ ? Direction.Axis.X : Direction.Axis.Z);
                        } else {
                            state = Kit.s(Blocks.SPRUCE_PLANKS);
                        }
                        this.b.set(x, y, z, state);
                    }
                }
            }
            for (int x = X0; x <= X1; x++) {
                for (int z = Z0; z <= Z1; z++) {
                    boolean edge = x == X0 || x == X1 || z == Z0 || z == Z1;
                    this.b.set(x, -1, z, edge ? Kit.s(Blocks.END_STONE_BRICKS) : Kit.s(Blocks.SPRUCE_PLANKS));
                }
            }
            this.b.gableAlongX(X0, X1, Z0, Z1, 4, Blocks.PURPUR_STAIRS, Kit.s(Blocks.PURPUR_BLOCK), Kit.s(Blocks.PURPUR_SLAB),
                    Kit.s(Blocks.SPRUCE_PLANKS));
            this.b.door(0, 0, Z1, Blocks.SPRUCE_DOOR, Direction.NORTH);
            BlockState glass = Kit.s(Blocks.STAINED_GLASS_PANE.pick(DyeColor.PURPLE));
            for (int x : new int[] {-2, 2}) {
                Kit.window(this.b, x, 1, Z1, 2, glass);
            }
            Kit.window(this.b, X0, 1, -4, 2, glass);
            Kit.window(this.b, X1, 1, -5, 2, glass);
            Kit.window(this.b, 0, 1, Z0, 1, glass);
            // Läden neben den Fenstern vorn
            Kit.shutter(this.b, -3, 1, Z1 + 1, this.spruce.trapdoor(), Direction.SOUTH);
            Kit.shutter(this.b, 3, 1, Z1 + 1, this.spruce.trapdoor(), Direction.SOUTH);
            // der Ofenrohr-Kamin hinten rechts
            for (int y = 0; y <= 8; y++) {
                this.b.set(X1 + 1, y, Z0 + 1, y == 8 ? Kit.s(Blocks.END_STONE_BRICK_WALL) : Kit.s(Blocks.END_STONE_BRICKS));
            }
            Kit.campfire(this.b, X1 + 1, 9, Z0 + 1, Blocks.SOUL_CAMPFIRE, true);
        }

        /** Drinnen: Bett, Räucherofen, Tisch und Stuhl, die Truhe, Köderfässer, eine Laterne. */
        private void inside() {
            this.b.bed(-1, 0, -6, Blocks.BED.pick(DyeColor.PURPLE), Direction.WEST);
            Kit.chest(this.b, CHEST.getX(), CHEST.getY(), CHEST.getZ(), Direction.SOUTH, Kit.loot("void_angler"));
            Kit.barrel(this.b, 1, 0, -6, Direction.UP);
            Kit.facing(this.b, 2, 0, -4, Blocks.SMOKER, Direction.WEST);
            Kit.table(this.b, -1, 0, -4, this.spruce);
            Kit.chair(this.b, -2, 0, -4, this.spruce, Direction.EAST);
            this.b.set(-2, 0, -3, Kit.s(Blocks.CRAFTING_TABLE));
            this.b.hangingLantern(0, 4, -4, Blocks.LANTERN);
            Kit.candles(this.b, 1, 1, -6, Blocks.DYED_CANDLE.pick(DyeColor.PURPLE), 2, true);
        }

        /** Die Veranda vor der Hütte: Bohlen, ein Geländer, zwei Laternenpfosten. */
        private void porch() {
            for (int x = X0; x <= X1; x++) {
                for (int z = Z1 + 1; z <= 2; z++) {
                    this.b.set(x, -1, z, Kit.s(Blocks.DARK_OAK_PLANKS));
                    for (int y = 0; y <= 5; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                }
            }
            for (int z = Z1 + 1; z <= 2; z++) {
                this.b.set(X0, 0, z, this.dark.fence());
                this.b.set(X1, 0, z, this.dark.fence());
            }
            for (int x : new int[] {X0, X1}) {
                this.b.set(x, 0, 2, this.dark.logY());
                this.b.set(x, 1, 2, this.dark.fence());
                this.b.set(x, 2, 2, Kit.s(Blocks.LANTERN));
                this.post(x, 2);
            }
            Kit.chair(this.b, -2, 0, Z1 + 1, this.dark, Direction.SOUTH);
            Kit.chair(this.b, -1, 0, Z1 + 1, this.dark, Direction.SOUTH);
            this.b.set(2, 0, Z1 + 1, Kit.s(Blocks.COMPOSTER));
        }

        /** Der Steg: drei Bohlen breit hinaus über die Leere, ein Geländer, Laternenpfosten, darunter Querbalken. */
        private void jetty() {
            for (int z = 3; z < DECK; z++) {
                for (int x = -2; x <= 2; x++) {
                    for (int y = 0; y <= 5; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                    if (Math.abs(x) <= 1) {
                        // ein paar Bohlen sind morsch: Stufen statt ganzer Bretter
                        this.b.set(x, -1, z, this.b.chance(x, 1, z, 0.1f) ? this.spruce.slab(SlabType.TOP)
                                : Kit.s(Blocks.SPRUCE_PLANKS));
                    }
                }
                this.b.set(-2, 0, z, this.spruce.fence());
                this.b.set(2, 0, z, this.spruce.fence());
                if (z % 4 == 0) {
                    for (int x = -2; x <= 2; x++) {
                        this.b.set(x, -2, z, this.dark.log(Direction.Axis.X));
                    }
                    this.post(-2, z);
                    this.post(2, z);
                    int side = z % 8 == 0 ? 2 : -2;
                    this.b.set(side, 1, z, this.spruce.fence());
                    this.b.set(side, 2, z, Kit.s(Blocks.LANTERN));
                } else {
                    this.b.set(-1, -2, z, this.dark.log(Direction.Axis.Z));
                    this.b.set(1, -2, z, this.dark.log(Direction.Axis.Z));
                }
            }
        }

        /** Die Plattform am Ende: ein Rahmen aus Schwarzeiche, Bohlen, Geländer, eine Bank mit Blick ins Leere, die Köderkiste. */
        private void deck() {
            for (int x = -3; x <= 3; x++) {
                for (int z = DECK; z <= END; z++) {
                    boolean rim = Math.abs(x) == 3 || z == END;
                    this.b.set(x, -1, z, rim ? this.dark.log(Math.abs(x) == 3 ? Direction.Axis.Z : Direction.Axis.X) : Kit.s(Blocks.SPRUCE_PLANKS));
                    this.b.set(x, -2, z, z == DECK + 2 || z == END ? this.dark.log(Direction.Axis.X) : Kit.s(Blocks.AIR));
                    for (int y = 0; y <= 6; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                    boolean railing = rim && !(z == DECK && Math.abs(x) <= 1);
                    if (railing || z == DECK && Math.abs(x) >= 2) {
                        this.b.set(x, 0, z, this.spruce.fence());
                    }
                }
            }
            for (int[] c : new int[][] {{-3, DECK}, {3, DECK}, {-3, END}, {3, END}}) {
                this.b.set(c[0], 0, c[1], this.dark.logY());
                this.b.set(c[0], 1, c[1], this.dark.fence());
                this.b.set(c[0], 2, c[1], Kit.s(Blocks.SOUL_LANTERN));
                this.post(c[0], c[1]);
            }
            Kit.chair(this.b, -2, 0, END - 1, this.spruce, Direction.SOUTH);
            Kit.chair(this.b, -1, 0, END - 1, this.spruce, Direction.SOUTH);
            Kit.lootBarrel(this.b, CATCH.getX(), CATCH.getY(), CATCH.getZ(), Kit.loot("void_angler_catch"));
            Kit.barrel(this.b, -2, 0, DECK + 1, Direction.UP);
            this.b.set(-1, 0, DECK + 1, Kit.s(Blocks.WATER_CAULDRON));
        }

        /** Der Galgen: ein Pfosten, ein Ausleger über das Ende hinaus, die Kette tief hinab und an ihr die Köderlaterne. */
        private void gallows() {
            int px = 2;
            int pz = END - 1;
            for (int y = 0; y <= 5; y++) {
                this.b.set(px, y, pz, this.dark.logY());
            }
            for (int z = pz + 1; z <= END + 3; z++) {
                this.b.set(px, 5, z, this.dark.log(Direction.Axis.Z));
            }
            this.b.set(px, 4, pz + 1, this.dark.stair(Direction.NORTH, Half.TOP));
            BlockState chain = Kit.s(Blocks.IRON_CHAIN);
            for (int y = 4; y >= -12; y--) {
                this.b.set(px, y, END + 3, chain);
            }
            this.b.set(px, -13, END + 3, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
    }
}
