package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Jagdhütte (seit 2.0): Im Wald, auf einer Lichtung aus Moos und Nadelstreu, ein Blockhaus aus Fichtenstämmen unter
 * steilem Dach, vorn eine überdachte Veranda mit Bank. Drinnen der Kamin mit einem Schädel darüber und Fellen zu beiden
 * Seiten, ein Bärenfell auf dem Boden, Tisch, Strohlager, der Pfeilmacher-Tisch und die Truhe des Jägers. Daneben die
 * Räucherkammer aus Bruchstein (sie raucht noch), ein Holzstoß mit Hackklotz, ein Gestell mit Fellen und, am Rand der
 * Lichtung, der Hochsitz.
 *
 * <p>Eigene Koordinaten: Mitte der Hütte bei (0, 0, 0), y = 0 ist der Fußboden; vorn (+z) Tür und Veranda.
 */
final class HuntingLodge {
    static final int X0 = -4, X1 = 4, Z0 = -3, Z1 = 3;
    static final BlockPos CHEST = new BlockPos(3, 0, -2);

    private HuntingLodge() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.HUNTING_LODGE, "jagdhuette", "Hunting Lodge", "Jagdhütte", 3, Wonders.Ground.LAND, false,
                HuntingLodge::plan, HuntingLodge::build, origin -> piece(origin, Rotation.NONE, 0x7A6DL),
                new double[][] {Wonders.view(-12, 7, 15, 0, 2, 0), Wonders.view(14, 5, -8, 5, 1, -3), Wonders.view(-2, 1.6, 2, 0, 1.5, -3),
                        Wonders.view(-4, 6.6, -2, -8, 5.5, -6)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = Kit.land(context, terrain, 10, 4);
        return origin == null ? Optional.empty() : Kit.stub(piece(origin, Kit.rotation(seed), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        return new SitePiece(SiteKind.HUNTING_LODGE, 0, origin, rotation, seed, new int[0], SitePiece.box(origin, rotation, -17, -4, -17, 17, 14, 17));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Lodge(b).build();
    }

    private static final class Lodge {
        private final VillageBuilder b;
        private final Kit.Wood spruce = Kit.spruce();
        private final Kit.Wood dark = Kit.darkOak();

        Lodge(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            Kit.pad(this.b, 11.5, Kit.s(Blocks.GRASS_BLOCK), Kit.s(Blocks.DIRT), 16);
            this.floorCover();
            this.cabin();
            this.inside();
            this.porch();
            this.smokehouse();
            this.hide();
            this.yard();
        }

        /** Der Waldboden der Lichtung: Gras, Moos, Nadelstreu. */
        private void floorCover() {
            for (int x = -11; x <= 11; x++) {
                for (int z = -11; z <= 11; z++) {
                    if (x * x + z * z > 121) {
                        continue;
                    }
                    float n = this.b.noise(x, 33, z);
                    if (n < 0.18f) {
                        this.b.set(x, -1, z, Kit.s(Blocks.PODZOL));
                    } else if (n < 0.28f) {
                        this.b.set(x, -1, z, Kit.s(Blocks.COARSE_DIRT));
                    } else if (n < 0.36f) {
                        this.b.set(x, -1, z, Kit.s(Blocks.MOSS_BLOCK));
                    }
                }
            }
        }

        /** Das Blockhaus: liegende Stämme, an den Ecken überstehend, ein steiles Dach mit Kamin. */
        private void cabin() {
            Kit.rect(this.b, X0, Z0, X1, Z1, Kit.s(Blocks.COBBLESTONE), Kit.s(Blocks.COBBLESTONE), 10);
            for (int y = 0; y <= 3; y++) {
                for (int x = X0; x <= X1; x++) {
                    for (int z = Z0; z <= Z1; z++) {
                        boolean edgeX = x == X0 || x == X1;
                        boolean edgeZ = z == Z0 || z == Z1;
                        if (edgeX || edgeZ) {
                            this.b.set(x, y, z, edgeX && edgeZ ? this.spruce.logY() : this.spruce.log(edgeZ ? Direction.Axis.X : Direction.Axis.Z));
                        }
                    }
                }
                for (int[] c : new int[][] {{X0, Z0}, {X1, Z0}, {X0, Z1}, {X1, Z1}}) {
                    if (y % 2 == 0) {
                        this.b.set(c[0] + Integer.signum(c[0]), y, c[1], this.spruce.log(Direction.Axis.X));
                    } else {
                        this.b.set(c[0], y, c[1] + Integer.signum(c[1]), this.spruce.log(Direction.Axis.Z));
                    }
                }
            }
            Kit.floor(this.b, X0 + 1, Z0 + 1, X1 - 1, Z1 - 1, -1, Kit.s(Blocks.SPRUCE_PLANKS));
            this.b.gableAlongX(X0, X1, Z0, Z1, 4, this.dark.stairs(), Kit.s(Blocks.DARK_OAK_PLANKS), Kit.s(Blocks.DARK_OAK_SLAB),
                    Kit.s(Blocks.SPRUCE_PLANKS));
            this.b.door(0, 0, Z1, Blocks.SPRUCE_DOOR, Direction.NORTH);
            for (int x : new int[] {-2, 2}) {
                Kit.window(this.b, x, 1, Z1, 2, Kit.s(Blocks.GLASS_PANE));
            }
            Kit.window(this.b, X0, 1, 0, 2, Kit.s(Blocks.GLASS_PANE));
            Kit.window(this.b, X1, 1, 0, 2, Kit.s(Blocks.GLASS_PANE));
            Kit.window(this.b, X0, 5, 0, 1, Kit.s(Blocks.GLASS_PANE));
            // der Kamin an der Rückwand, über das Dach hinaus
            for (int y = 0; y <= 9; y++) {
                for (int x = -1; x <= 1; x++) {
                    this.b.set(x, y, Z0 - 1, Kit.weathered(this.b, x, y, Z0 - 1, Kit.s(Blocks.COBBLESTONE), Kit.s(Blocks.MOSSY_COBBLESTONE), 0.3f,
                            Kit.s(Blocks.STONE), 0.2f));
                }
            }
            this.b.set(0, 10, Z0 - 1, Kit.s(Blocks.CAMPFIRE));
            this.b.set(-1, 10, Z0 - 1, Kit.s(Blocks.COBBLESTONE_SLAB));
            this.b.set(1, 10, Z0 - 1, Kit.s(Blocks.COBBLESTONE_SLAB));
        }

        /** Drinnen: der Kamin mit Schädel und Fellen, Bärenfell, Tisch, Strohlager, Pfeilmacher-Tisch, Truhe, Fässer. */
        private void inside() {
            // der Kamin: ein Feuer in der Rückwand
            for (int x = -1; x <= 1; x++) {
                this.b.set(x, 0, Z0, Kit.s(Blocks.COBBLESTONE));
                this.b.set(x, 1, Z0, x == 0 ? Kit.s(Blocks.AIR) : Kit.s(Blocks.COBBLESTONE));
                this.b.set(x, 2, Z0, Kit.s(Blocks.STONE_BRICKS));
            }
            this.b.set(0, 0, Z0, Kit.s(Blocks.CAMPFIRE));
            this.b.set(0, 1, Z0, Kit.s(Blocks.AIR));
            this.b.set(0, 3, Z0 + 1, Blocks.SKELETON_WALL_SKULL.defaultBlockState().setValue(WallSkullBlock.FACING, Direction.SOUTH));
            for (int x : new int[] {-2, 2}) {
                this.b.set(x, 2, Z0 + 1, Blocks.WALL_BANNER.pick(DyeColor.BROWN).defaultBlockState().setValue(WallBannerBlock.FACING, Direction.SOUTH));
            }
            // das Bärenfell vor dem Kamin
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 0; z++) {
                    this.b.set(x, 0, z, Kit.s(Blocks.CARPET.pick(DyeColor.BROWN)));
                }
            }
            this.b.set(0, 0, 1, Kit.s(Blocks.CARPET.pick(DyeColor.BLACK)));
            // Tisch und Stühle, Strohlager, Pfeilmacher-Tisch, die Truhe
            Kit.table(this.b, -3, 0, 1, this.spruce);
            Kit.chair(this.b, -3, 0, 2, this.spruce, Direction.NORTH);
            Kit.chair(this.b, -2, 0, 1, this.spruce, Direction.WEST);
            this.b.bed(-3, 0, -1, Blocks.STRAW_BED, Direction.NORTH);
            this.b.bed(3, 0, 0, Blocks.STRAW_BED, Direction.SOUTH);
            this.b.set(3, 0, 2, Kit.s(Blocks.FLETCHING_TABLE));
            Kit.chest(this.b, CHEST.getX(), CHEST.getY(), CHEST.getZ(), Direction.WEST, Kit.loot("hunting_lodge"));
            Kit.barrel(this.b, 2, 0, -2, Direction.UP);
            // Dachboden: ein paar Bretter mit Heu
            for (int x = X0 + 1; x <= X1 - 1; x++) {
                this.b.set(x, 4, -1, this.spruce.slab(SlabType.TOP));
                this.b.set(x, 4, 0, this.spruce.slab(SlabType.TOP));
            }
            this.b.set(-2, 5, 0, Kit.s(Blocks.HAY_BLOCK));
            this.b.set(2, 5, -1, Kit.s(Blocks.HAY_BLOCK));
            this.b.hangingLantern(0, 3, 1, Blocks.LANTERN);
        }

        /** Die Veranda: Bretterboden, ein Pultdach auf zwei Pfosten, eine Bank, eine Laterne. */
        private void porch() {
            for (int x = X0; x <= X1; x++) {
                for (int z = Z1 + 1; z <= Z1 + 2; z++) {
                    this.b.set(x, -1, z, Kit.s(Blocks.SPRUCE_PLANKS));
                    this.b.set(x, 3, z, z == Z1 + 2 ? this.spruce.slab(SlabType.BOTTOM) : this.spruce.slab(SlabType.TOP));
                }
            }
            for (int x : new int[] {X0, X1}) {
                for (int y = 0; y <= 2; y++) {
                    this.b.set(x, y, Z1 + 2, this.spruce.log(Direction.Axis.Y));
                }
            }
            this.b.set(-2, 0, Z1 + 1, VillageBuilder.stairs(this.spruce.stairs(), Direction.NORTH, Half.BOTTOM));
            this.b.set(-3, 0, Z1 + 1, VillageBuilder.stairs(this.spruce.stairs(), Direction.NORTH, Half.BOTTOM));
            this.b.set(2, 0, Z1 + 1, Kit.s(Blocks.BARREL));
            this.b.set(2, 1, Z1 + 1, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
        }

        /** Die Räucherkammer: Bruchstein, ein flaches Dach mit rauchendem Schlot, darin Räucheröfen und Fässer. */
        private void smokehouse() {
            int x0 = 7;
            int x1 = 11;
            int z0 = -5;
            int z1 = -2;
            Kit.rect(this.b, x0, z0, x1, z1, Kit.s(Blocks.COBBLESTONE), Kit.s(Blocks.COBBLESTONE), 6);
            for (int y = 0; y <= 2; y++) {
                for (int x = x0; x <= x1; x++) {
                    for (int z = z0; z <= z1; z++) {
                        boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                        this.b.set(x, y, z, edge ? Kit.weathered(this.b, x, y, z, Kit.s(Blocks.COBBLESTONE), Kit.s(Blocks.MOSSY_COBBLESTONE), 0.3f,
                                Kit.s(Blocks.STONE), 0.1f) : Kit.s(Blocks.AIR));
                    }
                }
            }
            this.b.gableAlongX(x0, x1, z0, z1, 3, this.spruce.stairs(), Kit.s(Blocks.SPRUCE_PLANKS), Kit.s(Blocks.SPRUCE_SLAB),
                    Kit.s(Blocks.COBBLESTONE));
            for (int y = 3; y <= 5; y++) {
                this.b.set(10, y, -4, Kit.s(Blocks.COBBLESTONE));
            }
            this.b.set(10, 6, -4, Kit.s(Blocks.CAMPFIRE));
            this.b.door(9, 0, z1, Blocks.SPRUCE_DOOR, Direction.NORTH);
            Kit.facing(this.b, 8, 0, -4, Blocks.SMOKER, Direction.SOUTH);
            Kit.facing(this.b, 10, 0, -4, Blocks.SMOKER, Direction.SOUTH);
            this.b.set(9, 0, -4, Kit.s(Blocks.CAMPFIRE));
            Kit.lootBarrel(this.b, 10, 0, -3, Kit.loot("hunting_lodge_smokehouse"));
            this.b.hangingLantern(8, 2, -3, Blocks.LANTERN);
        }

        /**
         * Der Hochsitz am Rand der Lichtung: vier schlanke Pfosten, in der Mitte der Leiterbaum, eine Kanzel mit Brüstung aus
         * Klappen, Eckpfosten und einem Pultdach.
         */
        private void hide() {
            int cx = -8;
            int cz = -6;
            for (int[] c : new int[][] {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
                for (int y = 0; y <= 3; y++) {
                    this.b.set(cx + c[0], y, cz + c[1], this.spruce.fence());
                }
                for (int y = 5; y <= 6; y++) {
                    this.b.set(cx + c[0], y, cz + c[1], this.spruce.fence());
                }
            }
            for (int y = 0; y <= 3; y++) {
                this.b.set(cx, y, cz + 1, this.spruce.log(Direction.Axis.Y));
            }
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    this.b.set(cx + dx, 4, cz + dz, Kit.s(Blocks.SPRUCE_PLANKS));
                }
                this.b.set(cx + dx, 7, cz - 2, VillageBuilder.stairs(this.spruce.stairs(), Direction.SOUTH, Half.BOTTOM));
                this.b.set(cx + dx, 7, cz - 1, this.spruce.slab(SlabType.TOP));
                this.b.set(cx + dx, 7, cz, this.spruce.slab(SlabType.TOP));
                this.b.set(cx + dx, 7, cz + 1, this.spruce.slab(SlabType.BOTTOM));
                this.b.set(cx + dx, 7, cz + 2, VillageBuilder.stairs(this.spruce.stairs(), Direction.NORTH, Half.BOTTOM));
            }
            // die Brüstung: Klappen zwischen den Eckpfosten, vorn (Süden) offen
            Kit.shutter(this.b, cx, 5, cz - 1, Blocks.SPRUCE_TRAPDOOR, Direction.SOUTH);
            Kit.shutter(this.b, cx - 1, 5, cz, Blocks.SPRUCE_TRAPDOOR, Direction.EAST);
            Kit.shutter(this.b, cx + 1, 5, cz, Blocks.SPRUCE_TRAPDOOR, Direction.WEST);
            for (int y = 0; y <= 4; y++) {
                this.b.set(cx, y, cz + 2, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.SOUTH));
            }
            Kit.chair(this.b, cx, 5, cz, this.spruce, Direction.NORTH);
        }

        /** Holzstoß und Hackklotz, ein Gestell mit Fellen, eine Feuerstelle mit Stammbänken. */
        private void yard() {
            for (int z = 1; z <= 4; z++) {
                for (int y = 0; y <= 1; y++) {
                    if (!(y == 1 && z == 4)) {
                        this.b.set(7, y, z, this.spruce.log(Direction.Axis.X));
                    }
                }
            }
            this.b.set(9, 0, 3, Kit.s(Blocks.STRIPPED_SPRUCE_LOG));
            // das Fellgestell
            for (int x : new int[] {-8, -5}) {
                for (int y = 0; y <= 2; y++) {
                    this.b.set(x, y, 5, this.dark.fence());
                }
            }
            for (int x = -7; x <= -6; x++) {
                this.b.set(x, 2, 5, this.dark.fence());
                this.b.set(x, 1, 5, Kit.s(Blocks.WOOL.pick(x == -7 ? DyeColor.BROWN : DyeColor.LIGHT_GRAY)));
            }
            // die Feuerstelle mit Stammbänken
            this.b.set(-6, -1, 9, Kit.s(Blocks.COBBLESTONE));
            this.b.set(-6, 0, 9, Blocks.CAMPFIRE.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false));
            this.b.set(-8, 0, 9, this.spruce.log(Direction.Axis.Z));
            this.b.set(-6, 0, 7, this.spruce.log(Direction.Axis.X));
            this.b.set(-4, 0, 9, this.spruce.log(Direction.Axis.Z));
            for (int z = Z1 + 3; z <= 11; z++) {
                if (this.b.chance(0, 50, z, 0.8f)) {
                    this.b.set(0, -1, z, Kit.s(Blocks.DIRT_PATH));
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private static BlockState unused() {
        return Blocks.AIR.defaultBlockState();
    }
}
