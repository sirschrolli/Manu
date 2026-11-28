package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Purpurwarte (seit 2.0): ein schlanker Turm aus Purpur auf einem Sockel aus Endsteinziegeln, fast dreißig Blöcke hoch,
 * die Kanten abgeschrägt, mit Fenstern aus magentafarbenem Glas. Innen windet sich eine Wendeltreppe um eine Säule hinauf
 * bis zur Plattform mit Zinnen und Endstäben; dort oben brennt ein Signalfeuer aus Glas und Licht, daneben die Truhe des
 * Wächters. Schreinlinge halten Wacht.
 *
 * <p>Eigene Koordinaten: y = 0 ist der Boden im Turm (Fußboden y = -1); vorn (+z) der Eingang.
 */
final class PurpurWatchtower {
    /** Die Plattform oben (Fußboden). */
    static final int TOP = 24;
    static final BlockPos CHEST = new BlockPos(-3, TOP + 1, -3);
    static final double PAD = 8.5;
    /** Die Wendeltreppe: acht Stufen je Umlauf um die Säule, jede Stufe steigt zur nächsten hin an. */
    private static final int[][] RING = {{1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}};
    private static final Direction[] UP = {Direction.WEST, Direction.WEST, Direction.NORTH, Direction.NORTH, Direction.EAST, Direction.EAST,
            Direction.SOUTH, Direction.SOUTH};

    private PurpurWatchtower() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.PURPUR_WATCHTOWER, "purpurwarte", "Purpur Watchtower", "Purpurwarte", 3, Wonders.Ground.LAND, false,
                PurpurWatchtower::plan, PurpurWatchtower::build, origin -> piece(origin, Rotation.NONE, 0x7A11L),
                new double[][] {Wonders.view(-13, 9, 17, 0, 13, 0), Wonders.view(15, TOP + 7, -13, 0, TOP, 0), Wonders.view(0.5, 1.6, 2.2, 0, 7, 0),
                        Wonders.view(1.5, TOP + 2.6, 3.5, -2.5, TOP + 1, -2.5), Wonders.view(10, 2, 10, 0, 3, 0)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = Kit.land(context, terrain, 7, 5);
        return origin == null ? Optional.empty() : Kit.stub(piece(origin, Kit.rotation(seed), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        int r = (int) PAD + 5;
        return new SitePiece(SiteKind.PURPUR_WATCHTOWER, 0, origin, rotation, seed, new int[0], SitePiece.box(origin, rotation, -r, -6, -r, r, TOP + 8, r));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Tower(b).build();
    }

    private static final class Tower {
        private final VillageBuilder b;

        Tower(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            Kit.pad(this.b, PAD, Kit.s(Blocks.END_STONE), Kit.s(Blocks.END_STONE), TOP + 6);
            this.base();
            this.shaft();
            this.stair();
            this.top();
            this.guards();
        }

        private BlockState rod(Direction facing) {
            return Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, facing);
        }

        private BlockState pillar() {
            return Blocks.PURPUR_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        }

        /** Gehört (x, z) zum Grundriss des Turms (7 × 7 mit abgeschrägten Ecken)? */
        private static boolean inside(int x, int z) {
            return Math.abs(x) <= 3 && Math.abs(z) <= 3 && Math.abs(x) + Math.abs(z) <= 5;
        }

        private static boolean wall(int x, int z) {
            return inside(x, z) && !(Math.abs(x) <= 2 && Math.abs(z) <= 2 && Math.abs(x) + Math.abs(z) <= 3);
        }

        /** Der Sockel: ein Pflaster aus Endsteinziegeln, um den Fuß des Turms ein dickes Sockelgeschoss mit geschrägter Kante. */
        private void base() {
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    if (Math.abs(x) + Math.abs(z) > 9) {
                        continue;
                    }
                    this.b.column(x, z, Kit.s(Blocks.END_STONE_BRICKS), 0);
                    this.b.set(x, -1, z, Math.abs(x) + Math.abs(z) == 9 || Math.max(Math.abs(x), Math.abs(z)) == 6
                            ? Kit.s(Blocks.PURPUR_BLOCK) : Kit.s(Blocks.END_STONE_BRICKS));
                    if (inside(x, z) || !touches(x, z) || z >= 3 && Math.abs(x) <= 1) {
                        continue;
                    }
                    for (int y = 0; y <= 2; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.END_STONE_BRICKS));
                    }
                    this.b.set(x, 3, z, VillageBuilder.stairs(Blocks.END_STONE_BRICK_STAIRS, inward(x, z), Half.BOTTOM));
                }
            }
            // Pfosten mit Endstäben an den Ecken des Pflasters
            for (int[] c : new int[][] {{-5, -3}, {5, -3}, {-5, 3}, {5, 3}, {-3, -5}, {3, -5}, {-3, 5}, {3, 5}}) {
                this.b.set(c[0], 0, c[1], Kit.s(Blocks.END_STONE_BRICK_WALL));
                this.b.set(c[0], 1, c[1], this.rod(Direction.UP));
            }
        }

        /** Der Schaft: Purpur, an den Schrägen Purpursäulen, Bänder aus Endsteinziegeln, Fenster aus Glas. */
        private void shaft() {
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    if (!inside(x, z)) {
                        continue;
                    }
                    boolean w = wall(x, z);
                    boolean edge = Math.abs(x) + Math.abs(z) == 5;
                    for (int y = 0; y <= TOP; y++) {
                        BlockState state = Kit.s(Blocks.AIR);
                        if (w) {
                            state = y % 8 == 0 && y > 0 ? Kit.s(Blocks.END_STONE_BRICKS) : edge ? this.pillar() : Kit.s(Blocks.PURPUR_BLOCK);
                        }
                        this.b.set(x, y, z, state);
                    }
                    this.b.set(x, -1, z, Kit.s(Blocks.PURPUR_BLOCK));
                }
            }
            // der Eingang vorn, ein Vordach darüber
            for (int y = 0; y <= 2; y++) {
                this.b.set(0, y, 3, Kit.s(Blocks.AIR));
            }
            for (int x = -1; x <= 1; x++) {
                this.b.set(x, 3, 4, VillageBuilder.slab(Blocks.PURPUR_SLAB, SlabType.TOP));
            }
            this.b.set(0, 2, 4, this.rod(Direction.DOWN));
            // Fenster: je Seite drei, versetzt (nie auf der Höhe der Bänder)
            BlockState glass = Kit.s(Blocks.STAINED_GLASS_PANE.pick(DyeColor.MAGENTA));
            for (int y : new int[] {5, 11, 18}) {
                Kit.window(this.b, 3, y, 0, 2, glass);
            }
            for (int y : new int[] {9, 13, 20}) {
                Kit.window(this.b, -3, y, 0, 2, glass);
            }
            for (int y : new int[] {6, 12, 19}) {
                Kit.window(this.b, 0, y, -3, 2, glass);
            }
            for (int y : new int[] {10, 21}) {
                Kit.window(this.b, 0, y, 3, 2, glass);
            }
            // Gesimse aus Endsteinziegeln rings um den Turm auf der Höhe der Bänder, unter der Plattform Konsolen aus Purpur
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    if (inside(x, z) || !touches(x, z)) {
                        continue;
                    }
                    for (int y = 8; y < TOP; y += 8) {
                        this.b.set(x, y, z, VillageBuilder.slab(Blocks.END_STONE_BRICK_SLAB, SlabType.BOTTOM));
                    }
                    if (Math.floorMod(x + z, 2) == 0) {
                        this.b.set(x, TOP - 1, z, VillageBuilder.stairs(Blocks.PURPUR_STAIRS, inward(x, z), Half.TOP));
                    }
                }
            }
        }

        /** Liegt (x, z) außen direkt am Turm (eine Seite oder Ecke berührt die Mauer)? */
        private static boolean touches(int x, int z) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (wall(x + dx, z + dz)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /** Die Richtung zur Turmmitte (für Konsolen, deren hohe Seite an der Mauer liegt). */
        private static Direction inward(int x, int z) {
            if (Math.abs(x) >= Math.abs(z)) {
                return x > 0 ? Direction.WEST : Direction.EAST;
            }
            return z > 0 ? Direction.NORTH : Direction.SOUTH;
        }

        /** Die Wendeltreppe um die Säule bis hinauf zur Plattform; Endstäbe an den Wänden leuchten den Weg. */
        private void stair() {
            for (int y = 0; y <= TOP; y++) {
                if (y < TOP) {
                    this.b.set(0, y, 0, this.pillar());
                }
                int i = y % 8;
                this.b.set(RING[i][0], y, RING[i][1], VillageBuilder.stairs(Blocks.PURPUR_STAIRS, UP[i], Half.BOTTOM));
            }
            for (int y = 3; y < TOP; y += 4) {
                boolean east = y % 8 < 4;
                this.b.set(east ? 2 : -2, y, east ? -1 : 1, this.rod(east ? Direction.WEST : Direction.EAST));
            }
        }

        /** Die Plattform: über den Turm hinaus, Zinnen aus Purpursäulen mit Endstäben, das Signalfeuer, die Truhe. */
        private void top() {
            // die Stufen unter der Plattform brauchen Kopffreiheit: dort bleibt sie offen
            int below = (TOP - 1) % 8;
            int below2 = (TOP - 2) % 8;
            int exit = TOP % 8;
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    if (Math.abs(x) + Math.abs(z) > 7) {
                        continue;
                    }
                    boolean rim = Math.abs(x) == 4 || Math.abs(z) == 4 || Math.abs(x) + Math.abs(z) == 7;
                    boolean hole = x == RING[below][0] && z == RING[below][1] || x == RING[below2][0] && z == RING[below2][1];
                    boolean step = x == RING[exit][0] && z == RING[exit][1];
                    if (hole) {
                        this.b.set(x, TOP, z, Kit.s(Blocks.AIR));
                    } else if (!step) {
                        this.b.set(x, TOP, z, rim ? VillageBuilder.slab(Blocks.PURPUR_SLAB, SlabType.TOP) : Kit.s(Blocks.PURPUR_BLOCK));
                    }
                    for (int y = TOP + 1; y <= TOP + 5; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                    if (rim) {
                        boolean merlon = Math.floorMod(x + z, 2) == 0;
                        this.b.set(x, TOP + 1, z, merlon ? this.pillar() : VillageBuilder.slab(Blocks.PURPUR_SLAB, SlabType.BOTTOM));
                        if (merlon && (Math.abs(x) + Math.abs(z) == 7 || Math.abs(x) == 4 && z == 0 || Math.abs(z) == 4 && x == 0)) {
                            this.b.set(x, TOP + 2, z, this.rod(Direction.UP));
                        }
                    }
                }
            }
            // ein Geländer an der Treppenöffnung (der Ausgang bleibt frei)
            for (int[] g : new int[][] {{2, -1}, {2, 0}, {1, -2}, {0, -1}, {0, 0}}) {
                this.b.set(g[0], TOP + 1, g[1], Kit.s(Blocks.IRON_BARS));
            }
            // das Signalfeuer: eine Säule, auf ihr violettes Glas, ringsum Endstäbe
            this.b.set(-2, TOP + 1, 2, this.pillar());
            this.b.set(-2, TOP + 2, 2, Kit.s(Blocks.STAINED_GLASS.pick(DyeColor.PURPLE)));
            this.b.set(-2, TOP + 3, 2, this.rod(Direction.UP));
            Kit.chest(this.b, CHEST.getX(), CHEST.getY(), CHEST.getZ(), Direction.SOUTH, Kit.loot("purpur_watchtower"));
            Kit.barrel(this.b, -2, TOP + 1, -3, Direction.UP);
            Kit.candles(this.b, -3, TOP + 1, -2, Blocks.DYED_CANDLE.pick(DyeColor.PURPLE), 3, true);
        }

        /** Schreinlinge wachen auf der Plattform und am Sockel. */
        private void guards() {
            Kit.mob(this.b, ModEntities.SHULKER.get(), 2, TOP + 1, 3, Direction.SOUTH);
            Kit.mob(this.b, ModEntities.SHULKER.get(), 4, 0, -4, Direction.EAST);
        }
    }
}
