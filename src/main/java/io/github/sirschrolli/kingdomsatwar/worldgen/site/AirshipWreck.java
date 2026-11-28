package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Das Luftschiffwrack (seit 2.0): Ein Luftschiff aus Schwarzeichenholz ist auf einer Insel des End zerschellt – der Bug hat
 * sich in den Endstein gebohrt, das Heck ragt in die Höhe, mittschiffs ist der Rumpf geborsten. Ein Mast steht noch mit
 * seinem Krähennest, der andere liegt quer über der Insel; daneben die erschlaffte Hülle des Ballons, deren Taue noch an der
 * Reling hängen. Am Heck die Kajüte des Kapitäns mit Karte, Laterne und Truhe, die Propeller zerbrochen; ringsum verstreute
 * Fracht. Leerwandler streifen um das Wrack, im Rumpf nisten Leermilben.
 *
 * <p>Eigene Koordinaten: y = 0 ist der Boden der Insel (Fußboden y = -1); der Rumpf liegt entlang z, der Bug vorn (+z).
 */
final class AirshipWreck {
    static final int LEN = 12;
    static final BlockPos CHEST = new BlockPos(0, 0, -9);
    static final double PAD = 12.5;

    private AirshipWreck() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.AIRSHIP_WRECK, "luftschiffwrack", "Airship Wreck", "Luftschiffwrack", 3, Wonders.Ground.LAND, false,
                AirshipWreck::plan, AirshipWreck::build, origin -> piece(origin, Rotation.NONE, 0xA15DL),
                new double[][] {Wonders.view(16, 10, 14, 0, 3, 0), Wonders.view(-16, 9, -12, 0, 4, 0), Wonders.view(1.4, 10.2, -7.2, -0.5, 9.2, -10),
                        Wonders.view(7, 3, 19, 0, 2, 6), Wonders.view(-14, 4, 9, -6, 1, 0)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = Kit.land(context, terrain, 12, 7);
        return origin == null ? Optional.empty() : Kit.stub(piece(origin, Kit.rotation(seed), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        int r = (int) PAD + 5;
        return new SitePiece(SiteKind.AIRSHIP_WRECK, 0, origin, rotation, seed, new int[0], SitePiece.box(origin, rotation, -r, -8, -r, r, 20, r));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Wreck(b).build();
    }

    private static final class Wreck {
        private final VillageBuilder b;

        Wreck(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            Kit.pad(this.b, PAD, Kit.s(Blocks.END_STONE), Kit.s(Blocks.END_STONE), 14);
            this.crater();
            this.hull();
            this.cabin();
            this.masts();
            this.balloon();
            this.cargo();
        }

        /** Wie hoch der Kiel bei z liegt: das Heck (hinten) ragt auf, der Bug (vorn) steckt im Boden. */
        private int keel(int z) {
            if (z < 0) {
                return 1 + (int) Math.round(-z * 0.3);
            }
            return z <= 4 ? 1 : 1 - (int) Math.round((z - 4) * 0.7);
        }

        private double halfWidth(int z) {
            double t = z / (LEN + 1.0);
            return Math.max(0.9, 3.7 * Math.sqrt(Math.max(0.0, 1.0 - t * t)));
        }

        /** Mittschiffs geborsten: hier fehlt der Rumpf, die Ränder sind ausgefranst. */
        private boolean breach(int x, int y, int z) {
            return z >= 1 && z <= 2 || z == 0 && this.b.chance(x, y + 5, z, 0.5f) || z == 3 && this.b.chance(x, y + 7, z, 0.4f);
        }

        /** Der Krater um den Bug: aufgewühlter Endstein, ein Graben, in dem der Bug steckt. */
        private void crater() {
            for (int x = -6; x <= 6; x++) {
                for (int z = 4; z <= LEN + 3; z++) {
                    double d = Math.sqrt(x * x * 0.8 + (z - 9) * (z - 9) * 0.35);
                    if (d > 5.0) {
                        continue;
                    }
                    int depth = (int) Math.round(3.5 - d * 0.6);
                    for (int y = -1; y > -1 - depth; y--) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                    if (d > 3.6 && this.b.chance(x, 3, z, 0.5f)) {
                        this.b.set(x, 0, z, Kit.s(Blocks.END_STONE));
                    }
                }
            }
        }

        /** Der Rumpf: Spanten und Planken aus Schwarzeichenholz, ein Deck aus Fichte mit Löchern, eine Reling. */
        private void hull() {
            for (int z = -LEN; z <= LEN; z++) {
                int keel = this.keel(z);
                double w = this.halfWidth(z);
                int deck = keel + 4;
                for (int x = -4; x <= 4; x++) {
                    if (Math.abs(x) > w + 0.4) {
                        continue;
                    }
                    int bottom = keel + (int) Math.floor(Math.abs(x) * 0.7);
                    boolean side = Math.abs(x) >= w - 0.6;
                    for (int y = bottom; y <= deck; y++) {
                        if (this.breach(x, y, z)) {
                            continue;
                        }
                        BlockState state;
                        if (y == deck) {
                            state = this.b.chance(x, y, z, 0.12f) ? Kit.s(Blocks.AIR) : Kit.s(Blocks.SPRUCE_PLANKS);
                        } else if (y == bottom || side) {
                            state = this.b.chance(x, y + 40, z, 0.07f) ? Kit.s(Blocks.AIR) : Kit.s(Blocks.DARK_OAK_PLANKS);
                        } else {
                            // die Spanten: alle drei Blöcke ein Ring aus Holz, sonst hohl
                            state = Math.floorMod(z, 3) == 0 && y < deck - 1 ? Kit.s(Blocks.STRIPPED_DARK_OAK_LOG) : Kit.s(Blocks.AIR);
                        }
                        this.b.set(x, y, z, state);
                    }
                    for (int y = deck + 1; y <= deck + 4; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                    if (side && !this.breach(x, deck + 1, z) && this.b.chance(x, 9, z, 0.85f)) {
                        this.b.set(x, deck + 1, z, Kit.s(Blocks.DARK_OAK_FENCE));
                    }
                }
                // der Kiel unten in der Mitte
                if (!this.breach(0, keel - 1, z)) {
                    this.b.set(0, keel - 1, z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
                }
            }
            // die Propeller am Heck: ein Kreuz aus Falltüren an der Achse
            int stern = -LEN - 1;
            int hub = this.keel(-LEN) + 2;
            this.b.set(0, hub, stern, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
            for (int[] blade : new int[][] {{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {0, 2}}) {
                if (blade[1] == 2) {
                    continue;
                }
                this.b.set(blade[0], hub + blade[1], stern - 1, Blocks.DARK_OAK_TRAPDOOR.defaultBlockState()
                        .setValue(TrapDoorBlock.FACING, Direction.NORTH).setValue(TrapDoorBlock.OPEN, true));
            }
            // Gebeine der Mannschaft auf dem Deck
            this.b.set(-1, this.keel(-3) + 5, -3, Kit.s(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, 3));
            this.b.set(2, this.keel(6) + 5, 6, Kit.s(Blocks.SKELETON_SKULL).setValue(SkullBlock.ROTATION, 11));
            Kit.mob(this.b, ModEntities.ENDERMITE.get(), 0, this.keel(-5) + 1, -5, Direction.SOUTH);
            Kit.mob(this.b, ModEntities.ENDERMITE.get(), 1, this.keel(-8) + 1, -8, Direction.NORTH);
        }

        /** Die Kajüte des Kapitäns auf dem Heck: Holzwände, Fenster (zum Teil zersprungen), Tisch mit Karte, Laterne, Truhe. */
        private void cabin() {
            int z0 = -LEN + 1;
            int z1 = -6;
            int floor = this.keel(z0) + 4;
            for (int x = -2; x <= 2; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean wall = Math.abs(x) == 2 || z == z0 || z == z1;
                    int base = this.keel(z) + 4;
                    this.b.set(x, base, z, Kit.s(Blocks.SPRUCE_PLANKS));
                    for (int y = base + 1; y <= floor + 3; y++) {
                        this.b.set(x, y, z, wall ? (Math.abs(x) == 2 && (z == z0 || z == z1) ? Kit.s(Blocks.STRIPPED_DARK_OAK_LOG)
                                : Kit.s(Blocks.DARK_OAK_PLANKS)) : Kit.s(Blocks.AIR));
                    }
                    this.b.set(x, floor + 4, z, VillageBuilder.slab(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
                }
            }
            // Tür nach vorn, Fenster seitlich
            int doorBase = this.keel(z1) + 4;
            this.b.door(0, doorBase + 1, z1, Blocks.DARK_OAK_DOOR, Direction.NORTH);
            for (int z = z0 + 1; z <= z1 - 1; z += 2) {
                int base = this.keel(z) + 4;
                this.b.set(-2, base + 2, z, this.b.chance(-2, 1, z, 0.4f) ? Kit.s(Blocks.AIR) : Kit.s(Blocks.GLASS_PANE));
                this.b.set(2, base + 2, z, this.b.chance(2, 1, z, 0.4f) ? Kit.s(Blocks.AIR) : Kit.s(Blocks.GLASS_PANE));
            }
            // drinnen: der Kartentisch, die Truhe, eine Laterne, eine umgestürzte Kiste
            int y = this.keel(-9) + 5;
            Kit.chest(this.b, CHEST.getX(), y, CHEST.getZ(), Direction.SOUTH, Kit.loot("airship_wreck"));
            this.b.set(-1, this.keel(-8) + 5, -8, Kit.s(Blocks.CARTOGRAPHY_TABLE));
            this.b.set(1, this.keel(-7) + 5, -7, Kit.s(Blocks.BARREL));
            this.b.hangingLantern(0, floor + 3, -8, Blocks.LANTERN);
        }

        /** Ein Mast steht noch mit seinem Krähennest, der andere liegt quer über der Insel. */
        private void masts() {
            int deck = this.keel(-2) + 4;
            for (int y = deck + 1; y <= deck + 10; y++) {
                this.b.set(0, y, -2, Kit.s(Blocks.STRIPPED_DARK_OAK_LOG));
            }
            for (int x = -1; x <= 1; x++) {
                for (int z = -3; z <= -1; z++) {
                    if (x != 0 || z != -2) {
                        this.b.set(x, deck + 8, z, VillageBuilder.slab(Blocks.SPRUCE_SLAB, SlabType.TOP));
                        this.b.set(x, deck + 9, z, Kit.s(Blocks.DARK_OAK_FENCE));
                    }
                }
            }
            // eine Rah mit zerrissenem Segel
            for (int x = -3; x <= 3; x++) {
                this.b.set(x, deck + 6, -2, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
                if (Math.abs(x) >= 1 && this.b.chance(x, 2, -2, 0.7f)) {
                    this.b.set(x, deck + 5, -1, Kit.s(Blocks.WOOL.pick(DyeColor.WHITE)));
                    if (this.b.chance(x, 3, -2, 0.5f)) {
                        this.b.set(x, deck + 4, -1, Kit.s(Blocks.WOOL.pick(DyeColor.LIGHT_GRAY)));
                    }
                }
            }
            // der umgestürzte Mast: liegt vom Deck schräg über die Insel nach rechts
            for (int x = 4; x <= 12; x++) {
                this.b.set(x, 0, 5 + (x - 4) / 3, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            }
            this.b.set(12, 1, 7, Kit.s(Blocks.DARK_OAK_FENCE));
            this.b.set(11, 1, 7, Kit.s(Blocks.DARK_OAK_FENCE));
        }

        /** Die Hülle des Ballons: erschlafft neben dem Wrack, rot-weiß gestreift, in weichen Falten, mit Tauen zur Reling. */
        private void balloon() {
            for (int x = -14; x <= -4; x++) {
                for (int z = -11; z <= 9; z++) {
                    double dx = (x + 8.8) / 4.6;
                    double dz = (z + 1.0) / 9.6;
                    double d = dx * dx + dz * dz + (this.b.noise(x, 31, z) - 0.5) * 0.18;
                    if (d > 1.0) {
                        continue;
                    }
                    // eine weiche Wölbung mit Falten quer zur Länge; die Bahnen der Hülle abwechselnd rot und weiß
                    double fold = Math.sin(z * 0.9 + x * 0.3) * 0.45;
                    int h = (int) Math.round(2.4 * (1.0 - d) + fold);
                    DyeColor color = Math.floorMod(z, 4) < 2 ? DyeColor.RED : DyeColor.WHITE;
                    for (int y = 0; y < h; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.WOOL.pick(color)));
                    }
                    this.b.set(x, Math.max(0, h), z, Kit.s(Blocks.CARPET.pick(color)));
                }
            }
            // die Gondelseile: Ketten quer über die Hülle und von ihr hinüber zur Reling
            for (int z : new int[] {-6, 0, 6}) {
                for (int x = -13; x <= -5; x++) {
                    int top = this.top(x, z);
                    if (top > 0) {
                        this.b.set(x, top, z, Kit.s(Blocks.IRON_CHAIN).setValue(net.minecraft.world.level.block.ChainBlock.AXIS, Direction.Axis.X));
                    }
                }
            }
            for (int[] rope : new int[][] {{-4, -4}, {-4, 2}}) {
                int deck = this.keel(rope[1]) + 4;
                for (int y = 1; y <= deck; y++) {
                    int x = rope[0] + (y * 2) / Math.max(1, deck);
                    this.b.set(x, y, rope[1], Kit.s(Blocks.IRON_CHAIN));
                }
            }
        }

        /** Die Höhe des ersten freien Blocks über der Hülle in der Säule (x, z) (0, wenn dort keine Hülle liegt). */
        private int top(int x, int z) {
            for (int y = 4; y >= 0; y--) {
                if (this.b.get(x, y, z).is(net.minecraft.tags.BlockTags.WOOL_CARPETS)) {
                    return y;
                }
            }
            return 0;
        }

        /** Verstreute Fracht: Fässer, eine Kiste, Laternen; ein Leerwandler am Wrack. */
        private void cargo() {
            for (int[] c : new int[][] {{5, 11}, {-5, 12}, {6, -4}, {4, -7}, {-3, 13}}) {
                Kit.barrel(this.b, c[0], 0, c[1], c[0] > 0 ? Direction.EAST : Direction.WEST);
            }
            Kit.lootBarrel(this.b, 5, 0, 10, Kit.loot("airship_wreck_cargo"));
            Kit.barrel(this.b, 5, 1, 11, Direction.UP);
            this.b.set(-6, 0, -6, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            this.b.set(7, 0, 2, Kit.s(Blocks.CARPET.pick(DyeColor.WHITE)));
            Kit.mob(this.b, ModEntities.ENDERMAN.get(), 8, 0, -2, Direction.WEST);
            Kit.mob(this.b, ModEntities.ENDERMAN.get(), -9, 0, 8, Direction.EAST);
        }
    }
}
