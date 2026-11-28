package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Imkerei (seit 2.0): Auf einer Blumenwiese summt es. Vor dem Fachwerkhaus des Imkers stehen Bienenstöcke auf Pfählen
 * in einer Reihe, daneben das Bienenhaus mit Strohkörben auf zwei Brettern; davor Beete voller Blumen, ein kleiner Teich mit
 * Seerosen. Im Haus die Honigküche – Waben, Honig, Wachskerzen, ein Kessel –, unter dem Vordach die Metfässer.
 *
 * <p>Eigene Koordinaten: Mitte des Hauses bei (0, 0, 0), y = 0 ist der Fußboden; vorn (+z) Tür, Stöcke und Beete.
 */
final class Apiary {
    static final int X0 = -3, X1 = 3, Z0 = -3, Z1 = 2;
    static final BlockPos CHEST = new BlockPos(-2, 0, -2);

    private Apiary() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.APIARY, "imkerei", "Apiary", "Imkerei", 3, Wonders.Ground.LAND, false,
                Apiary::plan, Apiary::build, origin -> piece(origin, Rotation.NONE, 0xB33L),
                new double[][] {Wonders.view(-12, 7, 16, 0, 2, 2), Wonders.view(12, 4, 12, -4, 1, 4), Wonders.view(-14, 3, 3, -8, 1, -2),
                        Wonders.view(2, 1.6, 1, -2, 1, -2)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = Kit.land(context, terrain, 11, 4);
        return origin == null ? Optional.empty() : Kit.stub(piece(origin, Kit.rotation(seed), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        return new SitePiece(SiteKind.APIARY, 0, origin, rotation, seed, new int[0], SitePiece.box(origin, rotation, -18, -6, -18, 18, 12, 18));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Yard(b).build();
    }

    private static final class Yard {
        private final VillageBuilder b;
        private final Kit.Wood oak = Kit.oak();

        Yard(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            Kit.pad(this.b, 12.5, Kit.s(Blocks.GRASS_BLOCK), Kit.s(Blocks.DIRT), 12);
            this.house();
            this.inside();
            this.hives();
            this.beeHouse();
            this.beds();
            this.pond();
        }

        private void house() {
            Kit.rect(this.b, X0, Z0, X1, Z1, Kit.s(Blocks.COBBLESTONE), Kit.s(Blocks.COBBLESTONE), 9);
            Kit.timber(this.b, X0, Z0, X1, Z1, 0, 3, ModBlocks.WHITE_PLASTER.base().get().defaultBlockState(), this.oak, 3);
            Kit.floor(this.b, X0 + 1, Z0 + 1, X1 - 1, Z1 - 1, -1, Kit.s(Blocks.OAK_PLANKS));
            Block tiles = ModBlocks.BROWN_ROOF_TILES.stairs().get();
            BlockState ridge = ModBlocks.BROWN_ROOF_TILES.base().get().defaultBlockState();
            BlockState cap = ModBlocks.BROWN_ROOF_TILES.slab().get().defaultBlockState();
            this.b.gableAlongX(X0, X1, Z0, Z1, 4, tiles, ridge, cap, ModBlocks.WHITE_PLASTER.base().get().defaultBlockState());
            this.b.door(0, 0, Z1, Blocks.OAK_DOOR, Direction.NORTH);
            for (int x : new int[] {-2, 2}) {
                Kit.window(this.b, x, 1, Z1, 2, Kit.s(Blocks.GLASS_PANE));
                this.b.set(x, 0, Z1 + 1, Kit.s(Blocks.POTTED_ALLIUM));
            }
            for (int z : new int[] {-1}) {
                Kit.window(this.b, X0, 1, z, 2, Kit.s(Blocks.GLASS_PANE));
                Kit.window(this.b, X1, 1, z, 2, Kit.s(Blocks.GLASS_PANE));
            }
            Kit.window(this.b, 0, 5, Z0, 1, Kit.s(Blocks.GLASS_PANE));
            // das Vordach im Osten mit den Metfässern
            for (int z = -2; z <= 1; z++) {
                this.b.set(X1 + 1, 3, z, this.oak.slab(SlabType.BOTTOM));
                this.b.set(X1 + 2, 3, z, this.oak.slab(SlabType.BOTTOM));
            }
            for (int z : new int[] {-2, 1}) {
                for (int y = 0; y <= 2; y++) {
                    this.b.set(X1 + 2, y, z, this.oak.fence());
                }
            }
            Kit.barrel(this.b, X1 + 1, 0, -1, Direction.EAST);
            Kit.barrel(this.b, X1 + 1, 0, 0, Direction.EAST);
            Kit.barrel(this.b, X1 + 1, 1, -1, Direction.UP);
            Kit.candles(this.b, X1 + 1, 2, -1, Blocks.DYED_CANDLE.pick(DyeColor.YELLOW), 2, false);
        }

        /** Die Honigküche: Waben, Honig, Kessel, Wachskerzen, ein Bett unter dem Fenster, die Truhe. */
        private void inside() {
            this.b.set(-2, 0, 1, Kit.s(Blocks.HONEYCOMB_BLOCK));
            this.b.set(-1, 0, 1, this.oak.slab(SlabType.TOP));
            this.b.set(-1, 1, 1, Kit.s(Blocks.HONEY_BLOCK));
            this.b.set(-2, 1, 1, Kit.s(Blocks.HONEYCOMB_BLOCK));
            this.b.set(2, 0, -2, Kit.s(Blocks.WATER_CAULDRON));
            this.b.set(1, 0, -2, Kit.s(Blocks.OAK_SHELF));
            this.b.set(2, 0, -1, Kit.s(Blocks.OAK_SHELF));
            Kit.chest(this.b, CHEST.getX(), CHEST.getY(), CHEST.getZ(), Direction.SOUTH, Kit.loot("apiary"));
            this.b.bed(2, 0, 1, Blocks.BED.pick(DyeColor.YELLOW), Direction.NORTH);
            Kit.table(this.b, 0, 0, -1, this.oak);
            Kit.chair(this.b, -1, 0, -1, this.oak, Direction.EAST);
            Kit.candles(this.b, -1, 1, 1, Blocks.DYED_CANDLE.pick(DyeColor.YELLOW), 3, false);
            this.b.hangingLantern(0, 3, 0, Blocks.LANTERN);
            // Waben über dem Kessel zum Trocknen
            this.b.set(2, 3, -2, Kit.s(Blocks.HONEYCOMB_BLOCK));
        }

        /** Sechs Bienenstöcke auf Pfählen in einer Reihe, das Flugloch nach vorn; ein paar Bienen. */
        private void hives() {
            for (int i = 0; i < 6; i++) {
                int x = -8 + i * 3;
                int z = 6 + (i % 2);
                this.b.set(x, 0, z, this.oak.fence());
                this.b.set(x, 1, z, Blocks.BEEHIVE.defaultBlockState().setValue(BeehiveBlock.FACING, Direction.SOUTH));
                this.b.set(x, 2, z, this.oak.slab(SlabType.BOTTOM));
            }
            for (int i = 0; i < 4; i++) {
                Kit.mob(this.b, ModEntities.BEE.get(), -7 + i * 4, 2, 8, Direction.SOUTH);
            }
        }

        /** Das Bienenhaus: ein offener Unterstand mit Pultdach, darin Strohkörbe auf zwei Brettern. */
        private void beeHouse() {
            int x0 = -11;
            int x1 = -7;
            int z0 = -5;
            for (int x = x0; x <= x1; x++) {
                for (int y = 0; y <= 3; y++) {
                    this.b.set(x, y, z0, Kit.s(Blocks.OAK_PLANKS));
                }
                this.b.set(x, 4, z0, VillageBuilder.stairs(this.oak.stairs(), Direction.NORTH, Half.BOTTOM));
                this.b.set(x, 4, z0 + 1, this.oak.slab(SlabType.TOP));
                this.b.set(x, 3, z0 + 2, this.oak.slab(SlabType.TOP));
                this.b.set(x, 1, z0 + 1, this.oak.slab(SlabType.TOP));
                this.b.set(x, 0, z0 + 1, Kit.s(Blocks.HAY_BLOCK));
                this.b.set(x, 2, z0 + 1, Kit.s(Blocks.HAY_BLOCK));
            }
            for (int x : new int[] {x0, x1}) {
                for (int y = 0; y <= 2; y++) {
                    this.b.set(x, y, z0 + 2, this.oak.logY());
                }
            }
        }

        /** Beete voller Blumen vor den Stöcken, dazwischen Wege; Wiesenblumen ringsum. */
        private void beds() {
            Block[] rows = {Blocks.CORNFLOWER, Blocks.ALLIUM, Blocks.OXEYE_DAISY, Blocks.POPPY, Blocks.AZURE_BLUET, Blocks.LILY_OF_THE_VALLEY};
            for (int row = 0; row < 3; row++) {
                int z = 10 + row;
                for (int x = -10; x <= 10; x++) {
                    if (x % 4 == 0) {
                        this.b.set(x, -1, z, Kit.s(Blocks.DIRT_PATH));
                        continue;
                    }
                    Block flower = rows[Math.floorMod(x / 4 + row * 2, rows.length)];
                    Kit.plant(this.b, x, 0, z, Kit.s(flower));
                }
            }
            for (int[] p : new int[][] {{-12, 2}, {11, -3}, {9, 4}, {-6, -9}, {4, -10}, {-11, -9}, {10, -8}, {6, 3}}) {
                Kit.plant(this.b, p[0], 0, p[1], Kit.s(this.b.chance(p[0], 7, p[1], 0.5f) ? Blocks.PINK_PETALS : Blocks.WILDFLOWERS));
            }
            for (int z = Z1 + 1; z <= 9; z++) {
                this.b.set(0, -1, z, Kit.s(Blocks.DIRT_PATH));
            }
        }

        /** Ein kleiner Teich mit Seerosen hinter dem Haus. */
        private void pond() {
            for (int x = 5; x <= 8; x++) {
                for (int z = -8; z <= -6; z++) {
                    if ((x == 5 || x == 8) && (z == -8 || z == -6)) {
                        continue;
                    }
                    this.b.set(x, -1, z, Kit.s(Blocks.WATER));
                    this.b.set(x, -2, z, Kit.s(Blocks.CLAY));
                }
            }
            this.b.set(6, 0, -7, Kit.s(Blocks.LILY_PAD));
            this.b.set(7, -1, -9, Kit.s(Blocks.SAND));
            this.b.set(7, 0, -9, Kit.s(Blocks.SUGAR_CANE));
            this.b.set(7, 1, -9, Kit.s(Blocks.SUGAR_CANE));
        }
    }
}
