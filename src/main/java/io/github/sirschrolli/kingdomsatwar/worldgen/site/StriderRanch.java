package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.Cuisine;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Der Schreiterhof (seit 2.0): Draußen im Lavameer liegt eine Insel aus Basalt, und auf ihr der Hof der Schreiterhirten – ein
 * Wohnhaus aus Karmesinholz unter einem Dach aus Netherziegeln, daneben der offene Stall mit Raufen voller Warzenblöcke und
 * einer Lavatränke. Vor der Insel liegen zwei Koppeln mitten in der Lava, eingezäunt auf Basaltpfosten, darin grasen
 * Lavastelzer; ein Steg führt zu ihren Gattern. Auf der Insel Beete mit Glutschoten auf Seelenerde und ein Garten aus
 * Wirrpilzen – das Futter der Schreiter.
 *
 * <p>Eigene Koordinaten: y = 0 ist die Höhe über der Lava (die Lava steht bis y = -1); die Insel trägt ihren Boden auf
 * y = 0, man geht auf y = 1. Hinten (-z) das Wohnhaus, vorn (+z) die Koppeln.
 */
final class StriderRanch {
    static final BlockPos CHEST = new BlockPos(-6, 1, -8);
    static final double ISLAND = 10.5;

    private StriderRanch() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.STRIDER_RANCH, "schreiterhof", "Strider Ranch", "Schreiterhof", 3, Wonders.Ground.LAVA, false,
                StriderRanch::plan, StriderRanch::build, origin -> piece(origin, Rotation.NONE, 0x57D1L),
                new double[][] {Wonders.view(-16, 11, 22, 0, 1, 2), Wonders.view(0, 2.6, 4, -3, 2, -6), Wonders.view(-4, 2.6, -5, -6, 1.5, -8),
                        Wonders.view(12, 7, 16, 4, 0, 6), Wonders.view(7, 2.6, -1, 4, 1.5, -6), Wonders.view(18, 8, -14, 0, 1, -3)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = Kit.lavaSea(context, terrain, 14, 9);
        return origin == null ? Optional.empty() : Kit.stub(piece(origin, Kit.rotation(seed), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        return new SitePiece(SiteKind.STRIDER_RANCH, 0, origin, rotation, seed, new int[0], SitePiece.box(origin, rotation, -16, -26, -16, 16, 12, 16));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Ranch(b).build();
    }

    private static final class Ranch {
        private final VillageBuilder b;
        private final Kit.Wood crimson = Kit.Wood.vanilla("crimson", true);

        Ranch(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            this.island();
            this.house();
            this.stable();
            this.gardens();
            this.corrals();
            this.lights();
        }

        private BlockState basalt() {
            return Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        }

        /** Die Insel: rund und unregelmäßig, oben Schwarzstein mit Basalt am Rand, darunter Basalt bis zum Grund der Lava. */
        private void island() {
            for (int x = -13; x <= 13; x++) {
                for (int z = -14; z <= 4; z++) {
                    double d = Math.sqrt(x * x * 0.6 + (z + 4) * (z + 4) * 1.1) + (this.b.noise(x, 5, z) - 0.5) * 1.8;
                    if (d > ISLAND) {
                        continue;
                    }
                    this.b.column(x, z, this.basalt(), 0);
                    boolean rim = d > ISLAND - 1.6;
                    this.b.set(x, 0, z, rim ? Kit.s(Blocks.SMOOTH_BASALT) : Kit.s(this.b.chance(x, 6, z, 0.3f) ? Blocks.POLISHED_BLACKSTONE
                            : Blocks.BLACKSTONE));
                    for (int y = 1; y <= 9; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                    // hier und da ein Basaltbrocken am Ufer
                    if (rim && this.b.chance(x, 7, z, 0.18f)) {
                        this.b.set(x, 1, z, this.basalt());
                    }
                }
            }
            // der Steg zu den Koppeln
            for (int z = 4; z <= 9; z++) {
                for (int x = -1; x <= 1; x++) {
                    this.b.column(x, z, this.basalt(), 0);
                    this.b.set(x, 0, z, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    this.b.set(x, 1, z, Kit.s(Blocks.AIR));
                }
                if (z % 3 == 0) {
                    this.b.set(-2, 0, z, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                    this.b.set(2, 0, z, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
                }
            }
        }

        /** Das Wohnhaus: Fachwerk aus geschälten Karmesinstielen, Wände aus Karmesinbrettern, Satteldach aus Netherziegeln. */
        private void house() {
            int x0 = -8, x1 = -2, z0 = -10, z1 = -5;
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean edgeX = x == x0 || x == x1;
                    boolean edgeZ = z == z0 || z == z1;
                    this.b.set(x, 0, z, Kit.s(Blocks.CRIMSON_PLANKS));
                    for (int y = 1; y <= 4; y++) {
                        BlockState state = Kit.s(Blocks.AIR);
                        if (edgeX && edgeZ) {
                            state = Kit.s(Blocks.STRIPPED_CRIMSON_STEM);
                        } else if (edgeX || edgeZ) {
                            state = y == 4 ? this.crimson.log(edgeZ ? Direction.Axis.X : Direction.Axis.Z) : Kit.s(Blocks.CRIMSON_PLANKS);
                        }
                        this.b.set(x, y, z, state);
                    }
                }
            }
            this.b.gableAlongX(x0, x1, z0, z1, 5, Blocks.NETHER_BRICK_STAIRS, Kit.s(Blocks.NETHER_BRICKS), Kit.s(Blocks.NETHER_BRICK_SLAB),
                    Kit.s(Blocks.CRIMSON_PLANKS));
            // Tür nach vorn, Fenster mit Läden
            this.b.door(-5, 1, z1, Blocks.CRIMSON_DOOR, Direction.NORTH);
            this.b.set(-5, 0, z1 + 1, VillageBuilder.stairs(Blocks.POLISHED_BLACKSTONE_STAIRS, Direction.NORTH, Half.BOTTOM));
            for (int x : new int[] {-7, -3}) {
                Kit.window(this.b, x, 2, z1, 1, Kit.s(Blocks.GLASS_PANE));
                Kit.shutter(this.b, x - 1, 2, z1 + 1, Blocks.CRIMSON_TRAPDOOR, Direction.SOUTH);
                Kit.shutter(this.b, x + 1, 2, z1 + 1, Blocks.CRIMSON_TRAPDOOR, Direction.SOUTH);
            }
            Kit.window(this.b, x0, 2, -8, 2, Kit.s(Blocks.GLASS_PANE));
            Kit.window(this.b, x1, 2, -8, 1, Kit.s(Blocks.GLASS_PANE));
            // drinnen: Bett, Truhe, Räucherofen (für das Glutchili), Werkbank, ein Tisch, Fässer
            this.b.bed(-7, 1, -6, Blocks.BED.pick(net.minecraft.world.item.DyeColor.ORANGE), Direction.NORTH);
            Kit.chest(this.b, CHEST.getX(), CHEST.getY(), CHEST.getZ(), Direction.EAST, Kit.loot("strider_ranch"));
            Kit.facing(this.b, -3, 1, -9, Blocks.SMOKER, Direction.SOUTH);
            this.b.set(-4, 1, -9, Kit.s(Blocks.CRAFTING_TABLE));
            Kit.lootBarrel(this.b, -3, 1, -8, Kit.loot("strider_ranch_pantry"));
            Kit.table(this.b, -4, 1, -7, this.crimson);
            Kit.chair(this.b, -4, 1, -6, this.crimson, Direction.NORTH);
            this.b.hangingLantern(-5, 4, -7, Blocks.SOUL_LANTERN);
            this.b.set(-7, 1, -9, Kit.s(Blocks.CRIMSON_SHELF));
            // der Kamin an der Westseite
            for (int y = 0; y <= 8; y++) {
                this.b.set(x0 - 1, y, -8, Kit.s(Blocks.NETHER_BRICKS));
            }
            Kit.campfire(this.b, x0 - 1, 9, -8, Blocks.CAMPFIRE, true);
        }

        /** Der offene Stall: Pfosten, ein Pultdach, zwei Boxen mit Raufen voller Warzenblöcke und eine Lavatränke. */
        private void stable() {
            int x0 = 0, x1 = 7, z0 = -10, z1 = -5;
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    this.b.set(x, 0, z, Kit.s(this.b.chance(x, 9, z, 0.4f) ? Blocks.SOUL_SOIL : Blocks.BLACKSTONE));
                }
            }
            for (int[] p : new int[][] {{x0, z0}, {x1, z0}, {x0, z1}, {x1, z1}, {4, z1}}) {
                for (int y = 1; y <= 4; y++) {
                    this.b.set(p[0], y, p[1], Kit.s(Blocks.STRIPPED_CRIMSON_STEM));
                }
            }
            // das Pultdach: hinten höher (y 5), vorn tiefer
            for (int x = x0 - 1; x <= x1 + 1; x++) {
                for (int z = z0 - 1; z <= z1 + 1; z++) {
                    int y = z <= -8 ? 5 : 4;
                    BlockState roof = z == -8 ? VillageBuilder.stairs(Blocks.NETHER_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM)
                            : z < -8 ? VillageBuilder.slab(Blocks.NETHER_BRICK_SLAB, SlabType.BOTTOM)
                            : VillageBuilder.slab(Blocks.NETHER_BRICK_SLAB, SlabType.TOP);
                    this.b.set(x, y, z, roof);
                }
            }
            // die Rückwand und die Seiten halb offen
            for (int x = x0 + 1; x < x1; x++) {
                for (int y = 1; y <= 4; y++) {
                    this.b.set(x, y, z0, Kit.s(Blocks.CRIMSON_PLANKS));
                }
            }
            for (int z = z0 + 1; z < z1; z++) {
                this.b.set(x1, 1, z, this.crimson.fence().defaultBlockState());
            }
            // zwei Boxen, getrennt durch einen Zaun; Raufen aus Warzenblöcken, dazwischen die Lavatränke
            for (int z = z0 + 1; z <= z1 - 2; z++) {
                this.b.set(4, 1, z, this.crimson.fence().defaultBlockState());
            }
            this.b.set(1, 1, z0 + 1, Kit.s(Blocks.NETHER_WART_BLOCK));
            this.b.set(2, 1, z0 + 1, Kit.s(Blocks.NETHER_WART_BLOCK));
            this.b.set(1, 2, z0 + 1, Kit.s(Blocks.NETHER_WART_BLOCK));
            this.b.set(5, 1, z0 + 1, Kit.s(Blocks.WARPED_WART_BLOCK));
            this.b.set(6, 1, z0 + 1, Kit.s(Blocks.NETHER_WART_BLOCK));
            this.b.set(6, 2, z0 + 1, Kit.s(Blocks.WARPED_WART_BLOCK));
            this.b.set(3, 1, z0 + 1, Kit.s(Blocks.LAVA_CAULDRON));
            this.b.set(3, 1, z0 + 2, Kit.s(Blocks.POTTED_WARPED_FUNGUS));
            this.b.set(6, 1, -7, Kit.s(Blocks.POTTED_WARPED_FUNGUS));
            this.b.set(1, 4, -7, Kit.s(Blocks.SHROOMLIGHT));
            this.b.set(5, 4, -7, Kit.s(Blocks.SHROOMLIGHT));
        }

        /** Beete mit Glutschoten auf Seelenerde (eingefasst) und ein kleiner Garten aus Wirrpilzen auf Wirrnyzel. */
        private void gardens() {
            for (int x = -7; x <= -2; x++) {
                for (int z = -3; z <= 0; z++) {
                    boolean rim = x == -7 || x == -2 || z == -3 || z == 0;
                    if (rim) {
                        this.b.set(x, 1, z, VillageBuilder.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, SlabType.BOTTOM));
                    } else {
                        this.b.set(x, 0, z, Kit.s(Blocks.SOUL_SOIL));
                        int age = Math.floorMod(x * 5 + z * 3, 7) < 5 ? 3 : 2;
                        this.b.set(x, 1, z, Cuisine.EMBER_PEPPERS.get().defaultBlockState().setValue(NetherWartBlock.AGE, age));
                    }
                }
            }
            for (int[] f : new int[][] {{3, -2}, {4, -1}, {5, -3}, {6, -2}, {4, -3}}) {
                this.b.set(f[0], 0, f[1], Kit.s(Blocks.WARPED_NYLIUM));
                Kit.plant(this.b, f[0], 1, f[1], Kit.s(Blocks.WARPED_FUNGUS));
            }
            this.b.set(5, 0, -1, Kit.s(Blocks.WARPED_NYLIUM));
            Kit.plant(this.b, 5, 1, -1, Kit.s(Blocks.WARPED_ROOTS));
            this.b.set(3, 0, -1, Kit.s(Blocks.WARPED_NYLIUM));
            Kit.plant(this.b, 3, 1, -1, Kit.s(Blocks.WARPED_ROOTS));
        }

        /**
         * Zwei Koppeln in der Lava (x -9..-2 und 2..9, z 6..13): Pfosten aus Basalt vom Grund der Lava, darauf Zäune aus
         * Netherziegeln, je ein Gatter zum Steg; darin Lavastelzer.
         */
        private void corrals() {
            for (int side : new int[] {-1, 1}) {
                int xa = side < 0 ? -9 : 2;
                int xb = side < 0 ? -2 : 9;
                int za = 6;
                int zb = 13;
                for (int x = xa; x <= xb; x++) {
                    for (int z = za; z <= zb; z++) {
                        if (x != xa && x != xb && z != za && z != zb) {
                            continue;
                        }
                        boolean post = (x - xa) % 3 == 0 && (z - za) % 3 == 0 || (x == xb || x == xa) && (z == za || z == zb);
                        if (post) {
                            this.b.column(x, z, this.basalt(), 0);
                            this.b.set(x, -1, z, this.basalt());
                        }
                        this.b.set(x, 0, z, Kit.s(Blocks.NETHER_BRICK_FENCE));
                        if (post) {
                            this.b.set(x, 1, z, Kit.s(Blocks.NETHER_BRICK_FENCE));
                        }
                    }
                }
                // das Gatter am Steg
                int gx = side < 0 ? xb : xa;
                this.b.set(gx, 0, 8, this.crimson.gate().defaultBlockState().setValue(FenceGateBlock.FACING, side < 0 ? Direction.WEST : Direction.EAST));
                Kit.mob(this.b, ModEntities.STRIDER.get(), side * 5, 0, 9, side < 0 ? Direction.WEST : Direction.EAST);
                Kit.mob(this.b, ModEntities.STRIDER.get(), side * 7, 0, 12, Direction.SOUTH);
            }
            // der Steg reicht bis zwischen die Gatter
            for (int x = -1; x <= 1; x++) {
                for (int z = 10; z <= 11; z++) {
                    this.b.column(x, z, this.basalt(), 0);
                    this.b.set(x, 0, z, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                }
            }
        }

        /** Laternen auf Pfosten an Steg und Beeten, der Hirte vor dem Haus. */
        private void lights() {
            for (int[] p : new int[][] {{-2, 4}, {2, 4}, {-2, 11}, {2, 11}, {-8, -2}, {8, -3}}) {
                this.b.set(p[0], 1, p[1], Kit.s(Blocks.NETHER_BRICK_FENCE));
                this.b.set(p[0], 2, p[1], Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            }
            Mob herder = Kit.mob(this.b, ModEntities.PIGLIN.get(), 0, 1, -2, Direction.SOUTH);
            if (herder != null) {
                herder.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
            }
        }
    }
}
