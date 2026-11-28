package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Goldmine der Borstlinge (seit 2.0): ein Tagebau in drei Stufen, tief in den Höhlenboden gegraben – in seinen Wänden
 * glänzen Netherrack-Golderz und vergoldeter Schwarzstein, ganz unten ein Nest aus Rohgold. Leitern verbinden die Stufen,
 * am Rand steht ein Gerüst mit einem Kran, an dessen Kette ein Kessel über der Grube hängt. Auf Schienen rollt eine Lore mit
 * Erz zur Schmelzhütte: Hochöfen, ein Amboss, Goldblöcke und die Truhe des Aufsehers. Borstlinge mit goldenen Spitzhacken
 * schuften in der Grube, ein Borstenriese wacht.
 *
 * <p>Eigene Koordinaten: y = 0 ist der Boden am Rand (Fußboden y = -1); die Grube liegt um (0, 0, 0), die Hütte im Osten.
 */
final class GoldMine {
    /** Die Stufen: bis zu welchem Abstand welche Bodenhöhe (Fußboden), innen am tiefsten. */
    static final double[] RINGS = {2.6, 4.2, 5.8, 7.3};
    static final int[] FLOORS = {-10, -7, -4, -1};
    static final BlockPos CHEST = new BlockPos(12, 0, -5);
    static final double PAD = 13.5;

    private GoldMine() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.GOLD_MINE, "goldmine", "Bristling Gold Mine", "Borstling-Goldmine", 3, Wonders.Ground.LAND, false,
                GoldMine::plan, GoldMine::build, origin -> piece(origin, Rotation.NONE, 0x60D1L),
                new double[][] {Wonders.view(-13, 11, 15, 0, -4, 0), Wonders.view(0, -8.4, 1.5, 0, -9, -2), Wonders.view(16, 6, 9, 10, 1, -3),
                        Wonders.view(-8, 4, -9, 0, -5, 0), Wonders.view(9, 1.6, 2, 12, 1, -4)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = Kit.cavern(context, terrain, seed, 12, 4, 12);
        return origin == null ? Optional.empty() : Kit.stub(piece(origin, Kit.rotation(seed), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        int r = (int) PAD + 5;
        return new SitePiece(SiteKind.GOLD_MINE, 0, origin, rotation, seed, new int[0], SitePiece.box(origin, rotation, -r, -15, -r, r, 14, r));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Mine(b).build();
    }

    private static final class Mine {
        private final VillageBuilder b;
        private final Kit.Wood crimson = Kit.Wood.vanilla("crimson", true);

        Mine(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            Kit.pad(this.b, PAD, Kit.s(Blocks.NETHERRACK), Kit.s(Blocks.NETHERRACK), 12);
            this.pit();
            this.ladders();
            this.crane();
            this.rails();
            this.shed();
            this.workers();
        }

        /** Gestein der Grubenwände: meist Netherrack, dazwischen Schwarzstein, Golderz und vergoldeter Schwarzstein. */
        private BlockState rock(int x, int y, int z) {
            float n = this.b.noise(x * 3, y * 5 + 11, z * 3);
            if (n < 0.13f) {
                return Kit.s(Blocks.NETHER_GOLD_ORE);
            }
            if (n < 0.19f) {
                return Kit.s(Blocks.GILDED_BLACKSTONE);
            }
            if (n > 0.8f) {
                return Kit.s(Blocks.BLACKSTONE);
            }
            return Kit.s(Blocks.NETHERRACK);
        }

        private double dist(int x, int z) {
            return Math.sqrt(x * x + z * z * 1.15) + (this.b.noise(x, 3, z) - 0.5) * 0.7;
        }

        /** Die Bodenhöhe (Fußboden) der Grube in der Säule (x, z); außerhalb der Rand (-1). */
        private int floorOf(int x, int z) {
            double d = this.dist(x, z);
            for (int i = 0; i < RINGS.length; i++) {
                if (d <= RINGS[i]) {
                    return FLOORS[i];
                }
            }
            return -1;
        }

        /** Die Grube: Säule für Säule ein fester Grund (mit Erz in den Wänden), darüber Luft bis zur Höhlendecke. */
        private void pit() {
            for (int x = -9; x <= 9; x++) {
                for (int z = -9; z <= 9; z++) {
                    if (this.dist(x, z) > 8.6) {
                        continue;
                    }
                    int floor = this.floorOf(x, z);
                    for (int y = -13; y <= floor; y++) {
                        this.b.set(x, y, z, this.rock(x, y, z));
                    }
                    // der Boden der Stufen: festgetretener Schwarzstein, am Rand gewöhnlicher Netherrack
                    this.b.set(x, floor, z, floor == -1 ? Kit.s(Blocks.NETHERRACK)
                            : this.b.chance(x, 22, z, 0.5f) ? Kit.s(Blocks.BLACKSTONE) : Kit.s(Blocks.NETHERRACK));
                    for (int y = floor + 1; y <= 8; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                    // auf den Stufen Kies und Schotter, unten das Rohgold
                    if (floor < -1 && this.b.chance(x, 21, z, 0.22f)) {
                        this.b.set(x, floor, z, Kit.s(Blocks.GRAVEL));
                    }
                }
            }
            for (int[] g : new int[][] {{0, -1}, {1, 0}, {-1, 1}}) {
                this.b.set(g[0], FLOORS[0], g[1], Kit.s(Blocks.RAW_GOLD_BLOCK));
            }
            this.b.set(1, FLOORS[0] + 1, -1, Kit.s(Blocks.GILDED_BLACKSTONE));
            this.b.set(-1, FLOORS[0] + 1, 0, Kit.s(Blocks.NETHER_GOLD_ORE));
            // ein Geländer aus Netherziegelzaun am Rand der Grube (Lücken an der Leiter und an den Schienen)
            for (int x = -9; x <= 9; x++) {
                for (int z = -9; z <= 9; z++) {
                    if (this.floorOf(x, z) != -1 || this.dist(x, z) > 8.6 || Math.abs(x) <= 1 && z < 0 || x >= 5 && Math.abs(z) <= 1) {
                        continue;
                    }
                    boolean edge = false;
                    for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                        edge |= this.floorOf(x + d[0], z + d[1]) < -1 && this.dist(x + d[0], z + d[1]) <= 8.6;
                    }
                    if (edge) {
                        this.b.set(x, 0, z, Kit.s(Blocks.NETHER_BRICK_FENCE));
                    }
                }
            }
        }

        /** Leitern an der Nordseite: wo der Boden zur nächsten Stufe ansteigt, hängt an der Stufenwand eine Leiter. */
        private void ladders() {
            for (int z = 0; z > -9; z--) {
                int here = this.floorOf(0, z);
                int next = this.floorOf(0, z - 1);
                if (next > here) {
                    for (int y = here + 1; y <= next; y++) {
                        this.b.set(0, y, z, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.SOUTH));
                    }
                }
            }
        }

        /** Das Gerüst am Westrand mit dem Kran: ein Ausleger über der Grube, eine Kette, daran ein Kessel. */
        private void crane() {
            int px = -9;
            int pz = 0;
            for (int[] p : new int[][] {{px, pz - 1}, {px, pz + 1}, {px - 2, pz - 1}, {px - 2, pz + 1}}) {
                for (int y = 0; y <= 6; y++) {
                    this.b.set(p[0], y, p[1], Kit.s(Blocks.STRIPPED_CRIMSON_STEM));
                }
                this.b.column(p[0], p[1], Kit.s(Blocks.STRIPPED_CRIMSON_STEM), 0);
            }
            for (int x = px - 2; x <= px; x++) {
                for (int z = pz - 1; z <= pz + 1; z++) {
                    this.b.set(x, 3, z, VillageBuilder.slab(Blocks.CRIMSON_SLAB, SlabType.TOP));
                    this.b.set(x, 7, z, Kit.s(Blocks.CRIMSON_PLANKS));
                }
            }
            this.b.ladder(px, 0, 7, pz + 2, Direction.SOUTH);
            // der Ausleger: ein Balken über die Grube, darunter die Kette mit dem Kessel
            for (int x = px + 1; x <= -2; x++) {
                this.b.set(x, 7, pz, Blocks.STRIPPED_CRIMSON_STEM.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            }
            this.b.set(-1, 7, pz, this.crimson.fence().defaultBlockState());
            for (int y = 2; y <= 6; y++) {
                this.b.set(-2, y, pz, Kit.s(Blocks.IRON_CHAIN));
            }
            this.b.set(-2, 1, pz, Kit.s(Blocks.CAULDRON));
            this.b.set(px - 1, 4, pz, Kit.s(Blocks.CRIMSON_FENCE));
            this.b.set(px - 1, 5, pz, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
        }

        /** Schienen am Ostrand vom Grubenrand zur Hütte, darauf eine Lore voller Erz. */
        private void rails() {
            for (int x = 7; x <= 10; x++) {
                this.b.set(x, -1, 0, Kit.s(Blocks.BLACKSTONE));
                this.b.set(x, 0, 0, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST));
            }
            this.b.set(11, -1, 0, Kit.s(Blocks.BLACKSTONE));
            this.b.set(11, 0, 0, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_WEST));
            for (int z = -1; z >= -2; z--) {
                this.b.set(11, -1, z, Kit.s(Blocks.BLACKSTONE));
                this.b.set(11, 0, z, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_SOUTH));
            }
            this.b.set(7, 0, -1, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            this.b.set(7, 0, 1, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICK_WALL));
            BlockPos cart = this.b.world(8, 0, 0);
            if (this.b.inside(cart)) {
                MinecartChest chest = EntityTypes.CHEST_MINECART.create(this.b.level.getLevel(), EntitySpawnReason.CHUNK_GENERATION);
                if (chest != null) {
                    chest.setInitialPos(cart.getX() + 0.5, cart.getY() + 0.1, cart.getZ() + 0.5);
                    chest.setLootTable(Kit.loot("gold_mine_cart"), cart.asLong() ^ 0x60D1L);
                    this.b.level.addFreshEntity(chest);
                }
            }
        }

        /** Die Schmelzhütte: Pfeiler aus Netherziegeln, ein Pultdach, zwei Hochöfen, Amboss, Goldblöcke, die Truhe. */
        private void shed() {
            int x0 = 9, x1 = 14, z0 = -7, z1 = -3;
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    this.b.set(x, -1, z, Kit.s(Blocks.POLISHED_BLACKSTONE_BRICKS));
                    for (int y = 0; y <= 4; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.AIR));
                    }
                }
            }
            for (int[] p : new int[][] {{x0, z0}, {x1, z0}, {x0, z1}, {x1, z1}}) {
                for (int y = 0; y <= 3; y++) {
                    this.b.set(p[0], y, p[1], Kit.s(Blocks.NETHER_BRICKS));
                }
            }
            for (int x = x0 - 1; x <= x1 + 1; x++) {
                for (int z = z0 - 1; z <= z1 + 1; z++) {
                    boolean back = z <= z0;
                    this.b.set(x, back ? 5 : 4, z, back ? VillageBuilder.slab(Blocks.NETHER_BRICK_SLAB, SlabType.BOTTOM)
                            : z == z0 + 1 ? VillageBuilder.stairs(Blocks.NETHER_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM)
                            : VillageBuilder.slab(Blocks.NETHER_BRICK_SLAB, SlabType.TOP));
                }
            }
            // die Rückwand aus Ziegeln mit den Hochöfen
            for (int x = x0 + 1; x < x1; x++) {
                for (int y = 0; y <= 4; y++) {
                    this.b.set(x, y, z0, Kit.s(Blocks.NETHER_BRICKS));
                }
            }
            for (int x : new int[] {10, 11}) {
                this.b.set(x, 0, z0 + 1, Blocks.BLAST_FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.SOUTH)
                        .setValue(AbstractFurnaceBlock.LIT, true));
            }
            this.b.set(13, 0, z0 + 1, Kit.s(Blocks.ANVIL));
            this.b.set(13, 0, z0 + 2, Kit.s(Blocks.GOLD_BLOCK));
            this.b.set(14, 0, z0 + 2, Kit.s(Blocks.GOLD_BLOCK));
            this.b.set(13, 1, z0 + 2, Kit.s(Blocks.RAW_GOLD_BLOCK));
            Kit.chest(this.b, CHEST.getX(), CHEST.getY(), CHEST.getZ(), Direction.WEST, Kit.loot("gold_mine"));
            Kit.lootBarrel(this.b, 14, 0, -4, Kit.loot("gold_mine_cart"));
            Kit.barrel(this.b, 14, 1, -4, Direction.UP);
            this.b.set(12, 3, -5, Kit.s(Blocks.SHROOMLIGHT));
            this.b.set(12, 4, -5, Kit.s(Blocks.NETHER_BRICKS));
            // Laternen auf Pfosten am Rand der Grube
            for (int[] p : new int[][] {{-6, 7}, {6, 7}, {6, -7}, {-6, -7}, {9, 3}}) {
                this.b.set(p[0], 0, p[1], Kit.s(Blocks.NETHER_BRICK_FENCE));
                this.b.set(p[0], 1, p[1], Kit.s(Blocks.NETHER_BRICK_FENCE));
                this.b.set(p[0], 2, p[1], Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            }
        }

        /** Borstlinge mit goldenen Spitzhacken in der Grube und an der Hütte, ein Borstenriese als Aufseher. */
        private void workers() {
            for (int[] w : new int[][] {{1, 1}, {-4, 2}, {3, -3}}) {
                Mob miner = Kit.mob(this.b, ModEntities.PIGLIN.get(), w[0], this.floorOf(w[0], w[1]) + 1, w[1], Direction.NORTH);
                if (miner != null) {
                    miner.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_PICKAXE));
                }
            }
            Mob boss = Kit.mob(this.b, ModEntities.PIGLIN_BRUTE.get(), 10, 0, -4, Direction.SOUTH);
            if (boss != null) {
                boss.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_AXE));
            }
        }
    }
}
