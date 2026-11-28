package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.block.crop.BerryBushBlock;
import io.github.sirschrolli.kingdomsatwar.registry.Cuisine;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Der Chorusgarten (seit 2.0): Auf einer Insel des End hat jemand einen Garten angelegt – eine niedrige Mauer aus
 * Endsteinziegeln mit Purpurpfosten und Endstäben, Wege aus Purpur, in den Beeten hohe Chorusgewächse und Sträucher voller
 * Schwebebeeren. In der Mitte ein Brunnen unter einem Pavillon aus Purpur und violettem Glas, hinten das Gärtnerhaus mit
 * Vorräten an Chorusmarmelade. Ein Leerwandler pflegt die Beete.
 *
 * <p>Eigene Koordinaten: y = 0 ist der Boden des Gartens (Fußboden y = -1); vorn (+z) das Tor, die Mitte bei (0, 0, 0).
 */
final class ChorusGarden {
    static final int WALL = 12;
    static final BlockPos CHEST = new BlockPos(10, 0, -10);
    /** Der Platz am Brunnen (hier kann später der Wächter des Gartens stehen). */
    static final BlockPos CENTER = new BlockPos(0, 0, 3);
    static final double PAD = 14.5;

    private ChorusGarden() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.CHORUS_GARDEN, "chorusgarten", "Chorus Garden", "Chorusgarten", 3, Wonders.Ground.LAND, false,
                ChorusGarden::plan, ChorusGarden::build, origin -> piece(origin, Rotation.NONE, 0xC0A5L),
                new double[][] {Wonders.view(0, 12, 22, 0, 1, 0), Wonders.view(-17, 9, -15, 0, 2, 0), Wonders.view(0, 1.6, 9, 0, 2, -1),
                        Wonders.view(7.5, 1.7, -7.2, 10.5, 0.8, -10.5), Wonders.view(-6, 2, 7, -7, 2, -6)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = Kit.land(context, terrain, 13, 6);
        return origin == null ? Optional.empty() : Kit.stub(piece(origin, Kit.rotation(seed), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        int r = (int) PAD + 5;
        return new SitePiece(SiteKind.CHORUS_GARDEN, 0, origin, rotation, seed, new int[0], SitePiece.box(origin, rotation, -r, -6, -r, r, 22, r));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Garden(b).build();
    }

    private static final class Garden {
        private final VillageBuilder b;

        Garden(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            Kit.pad(this.b, PAD, Kit.s(Blocks.END_STONE), Kit.s(Blocks.END_STONE), 18);
            this.walls();
            this.paths();
            this.beds();
            this.pavilion();
            this.hut();
            Kit.mob(this.b, ModEntities.ENDERMAN.get(), -5, 0, 5, Direction.EAST);
        }

        private BlockState rod(Direction up) {
            return Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, up);
        }

        /** Die Gartenmauer: Endsteinziegel mit Mauerkrone, Pfosten aus Purpur mit Endstäben, Tore vorn und hinten. */
        private void walls() {
            for (int x = -WALL; x <= WALL; x++) {
                for (int z = -WALL; z <= WALL; z++) {
                    if (Math.abs(x) != WALL && Math.abs(z) != WALL) {
                        continue;
                    }
                    boolean gate = Math.abs(x) <= 1 && Math.abs(z) == WALL;
                    boolean post = (x + WALL) % 4 == 0 && (z + WALL) % 4 == 0;
                    this.b.column(x, z, Kit.s(Blocks.END_STONE_BRICKS), 0);
                    if (gate) {
                        this.b.set(x, -1, z, Kit.s(Blocks.PURPUR_BLOCK));
                        continue;
                    }
                    if (post) {
                        for (int y = 0; y <= 2; y++) {
                            this.b.set(x, y, z, Blocks.PURPUR_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                        }
                        this.b.set(x, 3, z, this.rod(Direction.UP));
                    } else {
                        this.b.set(x, 0, z, Kit.s(Blocks.END_STONE_BRICKS));
                        this.b.set(x, 1, z, Kit.s(Blocks.END_STONE_BRICK_WALL));
                    }
                }
            }
            // die Torbögen: Purpurstufen über dem Durchgang zwischen zwei hohen Pfosten
            for (int sz : new int[] {-1, 1}) {
                int z = sz * WALL;
                for (int x : new int[] {-2, 2}) {
                    for (int y = 0; y <= 3; y++) {
                        this.b.set(x, y, z, Blocks.PURPUR_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                    }
                    this.b.set(x, 4, z, this.rod(Direction.UP));
                }
                this.b.set(-1, 3, z, VillageBuilder.stairs(Blocks.PURPUR_STAIRS, Direction.EAST, Half.TOP));
                this.b.set(1, 3, z, VillageBuilder.stairs(Blocks.PURPUR_STAIRS, Direction.WEST, Half.TOP));
                this.b.set(0, 3, z, VillageBuilder.slab(Blocks.PURPUR_SLAB, SlabType.TOP));
            }
        }

        /** Wege aus Purpur: ein Kreuz vom Tor zum Pavillon und ein Ring um ihn, eingefasst mit Stufen. */
        private void paths() {
            for (int i = -WALL + 1; i <= WALL - 1; i++) {
                for (int w = -1; w <= 1; w++) {
                    this.b.set(w, -1, i, Kit.s(Blocks.PURPUR_BLOCK));
                    this.b.set(i, -1, w, Kit.s(Blocks.PURPUR_BLOCK));
                }
            }
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    double d = Math.sqrt(x * x + z * z);
                    if (d >= 3.6 && d <= 5.4) {
                        this.b.set(x, -1, z, Kit.s(Blocks.PURPUR_BLOCK));
                    }
                }
            }
        }

        /**
         * Die Beete: in jedem Viertel ein erhöhtes Beet aus Endstein, eingefasst mit Endsteinziegeln; in zweien ein hohes
         * Chorusgewächs, in den anderen Schwebebeerensträucher; dazwischen Chorusblüten.
         */
        private void beds() {
            int[][] quads = {{-10, -10, -6, -6}, {6, -10, 10, -6}, {-10, 6, -6, 10}, {6, 6, 10, 10}};
            for (int q = 0; q < quads.length; q++) {
                int[] r = quads[q];
                for (int x = r[0]; x <= r[2]; x++) {
                    for (int z = r[1]; z <= r[3]; z++) {
                        boolean rim = x == r[0] || x == r[2] || z == r[1] || z == r[3];
                        this.b.set(x, 0, z, rim ? Kit.s(Blocks.END_STONE_BRICK_SLAB) : Kit.s(Blocks.END_STONE));
                    }
                }
                int cx = (r[0] + r[2]) / 2;
                int cz = (r[1] + r[3]) / 2;
                if (q == 1) {
                    continue;
                }
                if (q == 0 || q == 3) {
                    // ein hohes Chorusgewächs in der Mitte des Beets (wächst wie in der Natur, aus eigenem Zufall)
                    // drei Gewächse je Beet: ein hohes in der Mitte, zwei kleinere an den Ecken
                    for (int[] p : new int[][] {{cx, cz, 6}, {r[0] + 1, r[3] - 1, 2}, {r[2] - 1, r[1] + 1, 2}}) {
                        BlockPos root = this.b.world(p[0], 1, p[1]);
                        if (this.b.inside(root) && this.b.level.getBlockState(root.below()).is(Blocks.END_STONE)
                                && this.b.level.getBlockState(root).isAir()) {
                            ChorusFlowerBlock.generatePlant(this.b.level, root, RandomSource.create(root.asLong() ^ p[2]), p[2]);
                        }
                    }
                    for (int[] f : new int[][] {{r[0] + 1, r[1] + 1}, {r[2] - 1, r[3] - 1}}) {
                        this.b.set(f[0], 1, f[1], Blocks.CHORUS_FLOWER.defaultBlockState().setValue(ChorusFlowerBlock.AGE, 5));
                    }
                } else {
                    for (int x = r[0] + 1; x <= r[2] - 1; x++) {
                        for (int z = r[1] + 1; z <= r[3] - 1; z++) {
                            if ((x + z) % 2 == 0) {
                                int age = this.b.chance(x, 4, z, 0.7f) ? 3 : 2;
                                this.b.set(x, 1, z, Cuisine.FLOAT_BERRY_BUSH.get().defaultBlockState().setValue(BerryBushBlock.AGE, age));
                            }
                        }
                    }
                }
            }
        }

        /** Der Pavillon in der Mitte: sechs Purpursäulen, ein Dach aus violettem Glas, darunter der Brunnen mit Endstab. */
        private void pavilion() {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    boolean rim = Math.abs(x) == 2 || Math.abs(z) == 2;
                    if (rim) {
                        this.b.set(x, 0, z, Kit.s(Blocks.PURPUR_SLAB));
                    } else {
                        this.b.set(x, -1, z, Kit.s(Blocks.WATER));
                        this.b.set(x, -2, z, Kit.s(Blocks.PURPUR_BLOCK));
                    }
                }
            }
            for (int y = -1; y <= 1; y++) {
                this.b.set(0, y, 0, Blocks.PURPUR_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
            }
            this.b.set(0, 2, 0, this.rod(Direction.UP));
            for (int[] p : new int[][] {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}, {-3, 0}, {3, 0}}) {
                for (int y = 0; y <= 4; y++) {
                    this.b.set(p[0], y, p[1], Blocks.PURPUR_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                }
            }
            // das Dach: ein Ring aus Purpurstufen, innen violettes Glas, oben ein Endstab
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    int m = Math.max(Math.abs(x), Math.abs(z));
                    if (m == 4) {
                        this.b.set(x, 5, z, VillageBuilder.slab(Blocks.PURPUR_SLAB, SlabType.BOTTOM));
                    } else if (m == 3) {
                        this.b.set(x, 5, z, Kit.s(Blocks.PURPUR_BLOCK));
                    } else if (m == 2) {
                        this.b.set(x, 6, z, Kit.s(Blocks.STAINED_GLASS.pick(DyeColor.MAGENTA)));
                    } else if (m == 1) {
                        this.b.set(x, 7, z, Kit.s(Blocks.STAINED_GLASS.pick(DyeColor.PURPLE)));
                    }
                }
            }
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) == 2) {
                        this.b.set(x, 5, z, Kit.s(Blocks.PURPUR_BLOCK));
                    }
                }
            }
            this.b.set(0, 7, 0, Kit.s(Blocks.PURPUR_BLOCK));
            this.b.set(0, 8, 0, this.rod(Direction.UP));
            this.b.set(0, 6, 0, this.rod(Direction.DOWN));
            // Bänke zwischen den Säulen
            for (int[] s : new int[][] {{-3, -1, 1}, {-3, 1, 1}, {3, -1, 0}, {3, 1, 0}}) {
                this.b.set(s[0], 0, s[1], VillageBuilder.stairs(Blocks.PURPUR_STAIRS, s[2] == 1 ? Direction.WEST : Direction.EAST, Half.BOTTOM));
            }
        }

        /** Das Gärtnerhaus hinten rechts: Purpur und Endsteinziegel, ein flaches Dach, Werkzeug, Vorräte, die Truhe. */
        private void hut() {
            int x0 = 6, x1 = 11, z0 = -11, z1 = -6;
            // das Beet hinten rechts weicht dem Haus
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean wall = x == x0 || x == x1 || z == z0 || z == z1;
                    boolean corner = (x == x0 || x == x1) && (z == z0 || z == z1);
                    this.b.set(x, -1, z, Kit.s(Blocks.PURPUR_BLOCK));
                    for (int y = 0; y <= 3; y++) {
                        this.b.set(x, y, z, corner ? Blocks.PURPUR_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                                : wall ? (y == 3 ? Kit.s(Blocks.PURPUR_BLOCK) : Kit.s(Blocks.END_STONE_BRICKS)) : Kit.s(Blocks.AIR));
                    }
                    this.b.set(x, 4, z, wall ? Kit.s(Blocks.PURPUR_SLAB) : Kit.s(Blocks.PURPUR_BLOCK));
                }
            }
            this.b.door(x0 + 2, 0, z1, Blocks.SPRUCE_DOOR, Direction.NORTH);
            Kit.window(this.b, x0, 1, -9, 2, Kit.s(Blocks.STAINED_GLASS_PANE.pick(DyeColor.MAGENTA)));
            Kit.window(this.b, x1 - 1, 1, z1, 1, Kit.s(Blocks.STAINED_GLASS_PANE.pick(DyeColor.MAGENTA)));
            Kit.chest(this.b, CHEST.getX(), CHEST.getY(), CHEST.getZ(), Direction.WEST, Kit.loot("chorus_garden"));
            Kit.lootBarrel(this.b, 10, 0, -7, Kit.loot("chorus_garden_pantry"));
            this.b.set(7, 0, -10, Kit.s(Blocks.COMPOSTER));
            this.b.set(8, 0, -10, Kit.s(Blocks.CRAFTING_TABLE));
            this.b.bed(7, 0, -8, Blocks.BED.pick(DyeColor.PURPLE), Direction.SOUTH);
            this.b.set(9, 3, -8, this.rod(Direction.DOWN));
        }
    }
}
