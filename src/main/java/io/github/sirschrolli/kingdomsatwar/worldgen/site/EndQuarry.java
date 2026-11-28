package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.PhaseGear;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Der Endsteinbruch (seit 2.0): eine rechteckige Grube in vier Stufen, mit glatten Schnittkanten aus dem Endstein gebrochen.
 * In den Wänden leuchten Phasitadern und Linsen aus Obsidian, auf den Stufen liegen halb behauene Blöcke. Leitern führen
 * hinab, ein Kran aus Purpur hebt an seiner Kette einen Ziegelblock aus der Tiefe. Am Rand gestapelte Endsteinziegel,
 * Schienen mit einer beladenen Lore und die Hütte der Steinmetze mit Steinsäge, Schleifstein und ihrer Truhe. Leerwandler
 * schleppen Blöcke umher.
 *
 * <p>Eigene Koordinaten: y = 0 ist der Boden am Rand (Fußboden y = -1); die Grube liegt um (0, 0, 0), die Hütte im Osten.
 */
final class EndQuarry {
    /** Die Grube: Halbmaße außen und wie tief jede Stufe liegt (Fußboden). */
    static final int HX = 10;
    static final int HZ = 8;
    static final int[] FLOORS = {-4, -7, -10, -13};
    static final BlockPos CHEST = new BlockPos(16, 0, -7);
    static final double PAD = 19.5;

    private EndQuarry() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.END_QUARRY, "endsteinbruch", "End Quarry", "Endsteinbruch", 3, Wonders.Ground.LAND, false,
                EndQuarry::plan, EndQuarry::build, origin -> piece(origin, Rotation.NONE, 0x7A31L),
                new double[][] {Wonders.view(-17, 13, 17, 0, -6, 0), Wonders.view(3.5, -10.4, 1.5, -3, -5, -6), Wonders.view(21, 7, 7, 15, 1, -4),
                        Wonders.view(-7, 4, -13, -1, -6, 1), Wonders.view(15.5, 1.7, -2.5, 16, 0.8, -7)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = Kit.land(context, terrain, 14, 5);
        return origin == null ? Optional.empty() : Kit.stub(piece(origin, Kit.rotation(seed), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        int r = (int) PAD + 5;
        return new SitePiece(SiteKind.END_QUARRY, 0, origin, rotation, seed, new int[0], SitePiece.box(origin, rotation, -r, -18, -r, r, 14, r));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Quarry(b).build();
    }

    private static final class Quarry {
        private final VillageBuilder b;

        Quarry(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            Kit.pad(this.b, PAD, Kit.s(Blocks.END_STONE), Kit.s(Blocks.END_STONE), 12);
            this.pit();
            this.ladders();
            this.crane();
            this.stacks();
            this.rails();
            this.shed();
            this.workers();
        }

        private static boolean inPit(int x, int z) {
            return Math.abs(x) <= HX && Math.abs(z) <= HZ;
        }

        /** Die Stufe in der Säule (x, z): 0 = oberste … 3 = Sohle; außerhalb der Grube -1. */
        private static int level(int x, int z) {
            if (!inPit(x, z)) {
                return -1;
            }
            return Math.min(3, Math.min((HX - Math.abs(x)) / 2, (HZ - Math.abs(z)) / 2));
        }

        /** Der Fußboden der Grube in der Säule (x, z); außerhalb der Rand (-1). */
        private static int floorOf(int x, int z) {
            int level = level(x, z);
            return level < 0 ? -1 : FLOORS[level];
        }

        /** Gestein der Grubenwände: Endstein, darin Phasitadern, Obsidianlinsen und selten weinender Obsidian. */
        private BlockState rock(int x, int y, int z) {
            float n = this.b.noise(x * 4 + 7, y * 4, z * 4 - 3);
            if (n < 0.06f) {
                return PhaseGear.PHASIT_ORE.get().defaultBlockState();
            }
            if (n > 0.95f) {
                return Kit.s(Blocks.CRYING_OBSIDIAN);
            }
            if (n > 0.88f) {
                return Kit.s(Blocks.OBSIDIAN);
            }
            return Kit.s(Blocks.END_STONE);
        }

        /** Die Grube: Säule für Säule fester Grund bis zum Fußboden der Stufe, darüber Luft. */
        private void pit() {
            for (int x = -HX; x <= HX; x++) {
                for (int z = -HZ; z <= HZ; z++) {
                    int floor = floorOf(x, z);
                    for (int y = -16; y < floor; y++) {
                        this.b.set(x, y, z, this.rock(x, y, z));
                    }
                    // die Schnittflächen der Stufen: glatt gesägt, hier und da Splitt
                    this.b.set(x, floor, z, this.b.chance(x, 31, z, 0.15f) ? Kit.s(Blocks.GRAVEL) : Kit.s(Blocks.END_STONE));
                    for (int y = floor + 1; y <= 10; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                    // halb behauene Blöcke auf den Stufen (nicht an den Leitern)
                    if (x != 0 && floor < -1 && this.b.chance(x, 33, z, 0.05f)) {
                        this.b.set(x, floor + 1, z, this.b.chance(x, 34, z, 0.5f) ? VillageBuilder.slab(Blocks.END_STONE_BRICK_SLAB, SlabType.BOTTOM)
                                : VillageBuilder.stairs(Blocks.END_STONE_BRICK_STAIRS, Direction.from2DDataValue(Math.floorMod(x * 3 + z, 4)), Half.BOTTOM));
                    }
                }
            }
            // auf der Sohle ein großer Phasitbrocken, halb freigelegt
            this.b.set(0, FLOORS[3], 0, PhaseGear.PHASIT_ORE.get().defaultBlockState());
            this.b.set(1, FLOORS[3] + 1, 0, PhaseGear.PHASIT_ORE.get().defaultBlockState());
            this.b.set(-1, FLOORS[3] + 1, 1, Kit.s(Blocks.OBSIDIAN));
            // ein Geländer aus Endsteinziegelmauern am Rand (Lücken an der Leiter und an den Schienen)
            for (int x = -HX - 1; x <= HX + 1; x++) {
                for (int z = -HZ - 1; z <= HZ + 1; z++) {
                    boolean rim = Math.abs(x) == HX + 1 && Math.abs(z) <= HZ + 1 || Math.abs(z) == HZ + 1 && Math.abs(x) <= HX + 1;
                    boolean gap = Math.abs(x) <= 1 && z < 0 || x > 0 && Math.abs(z) <= 1;
                    if (rim && !gap && this.b.chance(x, 35, z, 0.85f)) {
                        this.b.set(x, 0, z, Kit.s(Blocks.END_STONE_BRICK_WALL));
                    }
                }
            }
        }

        /** Leitern an der Nordwand: wo der Boden zur nächsten Stufe ansteigt, hängt an der Stufenwand eine Leiter. */
        private void ladders() {
            for (int z = 0; z >= -HZ - 1; z--) {
                int here = floorOf(0, z);
                int next = floorOf(0, z - 1);
                if (next > here) {
                    this.b.ladder(0, here + 1, next, z, Direction.SOUTH);
                }
            }
        }

        private BlockState pillar(Direction.Axis axis) {
            return Blocks.PURPUR_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
        }

        /** Der Kran am Westrand: ein Mast aus Purpur, ein Ausleger über die Grube, Gegengewicht aus Obsidian, die Last an der Kette. */
        private void crane() {
            int mx = -HX - 3;
            for (int y = -1; y <= 10; y++) {
                this.b.set(mx, y, 0, this.pillar(Direction.Axis.Y));
            }
            this.b.column(mx, 0, Kit.s(Blocks.END_STONE_BRICKS), 0);
            for (int[] f : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                this.b.set(mx + f[0], 0, f[1], VillageBuilder.stairs(Blocks.PURPUR_STAIRS, dir(-f[0], -f[1]), Half.BOTTOM));
            }
            for (int x = mx - 3; x <= -2; x++) {
                this.b.set(x, 11, 0, this.pillar(Direction.Axis.X));
            }
            this.b.set(mx, 12, 0, Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP));
            // das Gegengewicht hinten am Ausleger, eine Strebe am Mast
            this.b.set(mx - 3, 10, 0, Kit.s(Blocks.IRON_CHAIN));
            this.b.set(mx - 3, 9, 0, Kit.s(Blocks.OBSIDIAN));
            this.b.set(mx - 3, 8, 0, Kit.s(Blocks.OBSIDIAN));
            this.b.set(mx + 1, 10, 0, VillageBuilder.stairs(Blocks.PURPUR_STAIRS, Direction.WEST, Half.TOP));
            // die Last: an der Kette über der Grube ein Block Endsteinziegel
            for (int y = 10; y >= -1; y--) {
                this.b.set(-2, y, 0, Kit.s(Blocks.IRON_CHAIN));
            }
            this.b.set(-2, -2, 0, Kit.s(Blocks.END_STONE_BRICKS));
        }

        private static Direction dir(int dx, int dz) {
            return dx > 0 ? Direction.EAST : dx < 0 ? Direction.WEST : dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }

        /** Stapel fertiger Endsteinziegel am Rand, dazu ein paar lose Blöcke. */
        private void stacks() {
            for (int[] s : new int[][] {{-7, 11, 2}, {-4, 11, 1}, {6, 11, 2}, {-14, -6, 2}, {-14, 6, 1}}) {
                for (int dx = 0; dx <= 1; dx++) {
                    for (int dz = 0; dz <= 1; dz++) {
                        for (int y = 0; y < s[2]; y++) {
                            if (y == s[2] - 1 && dx + dz == 2) {
                                this.b.set(s[0] + dx, y, s[1] + dz, VillageBuilder.slab(Blocks.END_STONE_BRICK_SLAB, SlabType.BOTTOM));
                            } else {
                                this.b.set(s[0] + dx, y, s[1] + dz, Kit.s(Blocks.END_STONE_BRICKS));
                            }
                        }
                    }
                }
            }
            this.b.set(1, 0, 11, Kit.s(Blocks.END_STONE));
            this.b.set(-10, 0, 12, Kit.s(Blocks.OBSIDIAN));
            // Laternen auf Pfosten an den Ecken der Grube
            for (int[] p : new int[][] {{-HX - 2, -HZ - 2}, {HX + 2, -HZ - 2}, {-HX - 2, HZ + 2}, {HX + 2, HZ + 2}}) {
                this.b.set(p[0], 0, p[1], Kit.s(Blocks.END_STONE_BRICK_WALL));
                this.b.set(p[0], 1, p[1], Kit.s(Blocks.END_STONE_BRICK_WALL));
                this.b.set(p[0], 2, p[1], Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP));
            }
        }

        /** Schienen vom Ostrand zur Hütte, darauf eine Lore voller Phasit und Steine. */
        private void rails() {
            for (int x = HX + 1; x <= 14; x++) {
                this.b.set(x, -1, 0, Kit.s(Blocks.END_STONE_BRICKS));
                this.b.set(x, 0, 0, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST));
            }
            this.b.set(15, -1, 0, Kit.s(Blocks.END_STONE_BRICKS));
            this.b.set(15, 0, 0, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_WEST));
            for (int z = -1; z >= -2; z--) {
                this.b.set(15, -1, z, Kit.s(Blocks.END_STONE_BRICKS));
                this.b.set(15, 0, z, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_SOUTH));
            }
            this.b.set(HX + 1, 0, -1, Kit.s(Blocks.END_STONE_BRICK_WALL));
            this.b.set(HX + 1, 0, 1, Kit.s(Blocks.END_STONE_BRICK_WALL));
            BlockPos cart = this.b.world(12, 0, 0);
            if (this.b.inside(cart)) {
                MinecartChest chest = EntityTypes.CHEST_MINECART.create(this.b.level.getLevel(), EntitySpawnReason.CHUNK_GENERATION);
                if (chest != null) {
                    chest.setInitialPos(cart.getX() + 0.5, cart.getY() + 0.1, cart.getZ() + 0.5);
                    chest.setLootTable(Kit.loot("end_quarry_cart"), cart.asLong() ^ 0x7A31L);
                    this.b.level.addFreshEntity(chest);
                }
            }
        }

        /** Die Hütte der Steinmetze: Pfeiler aus Purpur, Wände aus Endsteinziegeln, ein Pultdach; Steinsäge, Schleifstein, Truhe. */
        private void shed() {
            int x0 = 13, x1 = 18, z0 = -9, z1 = -4;
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    this.b.set(x, -1, z, Kit.s(Blocks.END_STONE_BRICKS));
                    for (int y = 0; y <= 5; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                    boolean edgeX = x == x0 || x == x1;
                    boolean edgeZ = z == z0 || z == z1;
                    if (edgeX && edgeZ) {
                        for (int y = 0; y <= 3; y++) {
                            this.b.set(x, y, z, this.pillar(Direction.Axis.Y));
                        }
                    } else if (z == z0 || x == x1) {
                        // Rück- und Ostwand geschlossen, die Front und die Westseite offen (eine Werkhalle)
                        for (int y = 0; y <= 3; y++) {
                            this.b.set(x, y, z, y == 3 ? Kit.s(Blocks.PURPUR_BLOCK) : Kit.s(Blocks.END_STONE_BRICKS));
                        }
                    }
                }
            }
            // ein flaches Dach aus Purpur mit Überstand, auf den Ecken Endstäbe
            for (int x = x0 - 1; x <= x1 + 1; x++) {
                for (int z = z0 - 1; z <= z1 + 1; z++) {
                    boolean over = x < x0 || x > x1 || z < z0 || z > z1;
                    this.b.set(x, 4, z, over ? VillageBuilder.slab(Blocks.PURPUR_SLAB, SlabType.TOP) : Kit.s(Blocks.PURPUR_BLOCK));
                }
            }
            for (int[] c : new int[][] {{x0, z0}, {x1, z0}, {x0, z1}, {x1, z1}}) {
                this.b.set(c[0], 5, c[1], Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, Direction.UP));
            }
            Kit.facing(this.b, x0 + 1, 0, z0 + 1, Blocks.STONECUTTER, Direction.SOUTH);
            Kit.facing(this.b, x0 + 2, 0, z0 + 1, Blocks.STONECUTTER, Direction.SOUTH);
            this.b.set(x1 - 1, 0, z0 + 3, Kit.s(Blocks.GRINDSTONE));
            this.b.set(x1 - 1, 0, z0 + 4, Kit.s(Blocks.SMITHING_TABLE));
            Kit.chest(this.b, CHEST.getX(), CHEST.getY(), CHEST.getZ(), Direction.SOUTH, Kit.loot("end_quarry"));
            Kit.barrel(this.b, x1 - 1, 0, z0 + 1, Direction.UP);
            Kit.barrel(this.b, x1 - 1, 1, z0 + 1, Direction.UP);
            this.b.hangingLantern(15, 3, -6, Blocks.LANTERN);
            // fertige Werkstücke vor der Hütte
            this.b.set(x0, 0, z1 + 2, Kit.s(Blocks.PURPUR_PILLAR));
            this.b.set(x0 + 2, 0, z1 + 2, VillageBuilder.stairs(Blocks.PURPUR_STAIRS, Direction.WEST, Half.BOTTOM));
        }

        /** Leerwandler schleppen Blöcke: einer am Rand, einer in der Grube, eine Milbe in den Stapeln. */
        private void workers() {
            Mob a = Kit.mob(this.b, ModEntities.ENDERMAN.get(), -3, floorOf(-3, 3) + 1, 3, Direction.EAST);
            if (a instanceof Enderman man) {
                man.setCarriedBlock(Kit.s(Blocks.END_STONE));
            }
            Mob c = Kit.mob(this.b, ModEntities.ENDERMAN.get(), 13, 0, 5, Direction.WEST);
            if (c instanceof Enderman man) {
                man.setCarriedBlock(PhaseGear.PHASIT_ORE.get().defaultBlockState());
            }
            Kit.mob(this.b, ModEntities.ENDERMITE.get(), 4, floorOf(4, -3) + 1, -3, Direction.NORTH);
        }
    }
}
