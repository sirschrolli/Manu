package io.github.sirschrolli.kingdomsatwar.worldgen.umbrer;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Die Formen der kleineren Bauwerke der Schattenwelt (Koordinaten im Bauwerk-Rahmen von {@link VillageBuilder}: x nach
 * rechts, y nach oben, z nach hinten; y = 0 ist die erste Lage über dem Gelände, vorn ist −z).
 */
final class ShadowSites {
    private ShadowSites() {}

    // =====================================================================================
    // Baustoffe
    // =====================================================================================

    private static BlockState s(Block block) {
        return block.defaultBlockState();
    }

    private static BlockState bricks(VillageBuilder b, int x, int y, int z, float wear) {
        return b.chance(x, y, z, wear) ? s(ShadowBlocks.CRACKED_SHADOW_STONE_BRICKS.get()) : s(ShadowBlocks.SHADOW_STONE_BRICKS.base().get());
    }

    private static BlockState polished() {
        return s(ShadowBlocks.POLISHED_SHADOW_STONE.base().get());
    }

    private static BlockState chiseled() {
        return s(ShadowBlocks.CHISELED_SHADOW_STONE.get());
    }

    private static BlockState stone() {
        return s(ShadowBlocks.SHADOW_STONE.base().get());
    }

    private static BlockState deep(Direction.Axis axis) {
        return ShadowBlocks.DEEP_SHADOW_STONE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static BlockState log(Direction.Axis axis) {
        return ShadowBlocks.SHADOW_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static BlockState planks() {
        return s(ShadowBlocks.SHADOW_PLANKS.base().get());
    }

    private static BlockState hanging(Block lantern) {
        return lantern.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true);
    }

    /** Hängende Glimmerranke von {@code top} abwärts ({@code length} Blöcke, unten die leuchtende Spitze). */
    private static void vine(VillageBuilder b, int x, int top, int z, int length) {
        for (int i = 0; i < length; i++) {
            int y = top - i;
            if (!b.get(x, y, z).isAir()) {
                return;
            }
            boolean tip = i == length - 1 || !b.get(x, y - 1, z).isAir();
            b.set(x, y, z, tip ? ShadowBlocks.GLIMMER_VINES.get().defaultBlockState() : ShadowBlocks.GLIMMER_VINES_PLANT.get().defaultBlockState());
            if (tip) {
                return;
            }
        }
    }

    // =====================================================================================
    // Verlassener Mondtempel
    // =====================================================================================

    /** Stufen des Sockels unter einer Säule (0–3): rechteckiges Schiff vorn, halbrunde Apsis hinten. */
    private static int plinth(int x, int z) {
        int ax = Math.abs(x);
        if (z <= 12) {
            if (ax <= 9 && z >= -13) {
                return 3;
            }
            if (ax <= 10 && z >= -14) {
                return 2;
            }
            return ax <= 11 && z >= -15 ? 1 : 0;
        }
        double d = Math.sqrt(x * x + (z - 12.0) * (z - 12.0));
        return d <= 6.6 ? 3 : d <= 7.6 ? 2 : d <= 8.6 ? 1 : 0;
    }

    /**
     * Ein verlassener Mondtempel der Umbrer: auf einem dreistufigen Sockel ein Säulenportikus, das Schiff mit zwei
     * Säulenreihen, Bänken und Fenstern aus Nachtkristall, ein steiles Steindach (an drei Stellen eingestürzt, darunter
     * Trümmer, Moos und Glimmerranken) und hinten eine Apsis mit Halbkuppel, dem Mondaltar und einer Mondsichel aus
     * Mondsilber. Unter dem Schiff liegt eine Gruft mit Sarkophagen und einer zweiten Truhe.
     */
    static void temple(VillageBuilder b) {
        // eingestürzte Stellen im Dach (vorab gezogen, damit jeder Chunk dasselbe Ergebnis erhält)
        double[][] holes = new double[3][3];
        for (double[] h : holes) {
            h[0] = -5 + b.random.nextInt(11);
            h[1] = -6 + b.random.nextInt(16);
            h[2] = 1.6 + b.random.nextDouble() * 1.6;
        }
        // Gelände: Fundament unter dem Sockel, ringsum freiräumen
        for (int x = -13; x <= 13; x++) {
            for (int z = -18; z <= 22; z++) {
                int lvl = plinth(x, z);
                boolean stairs = Math.abs(x) <= 4 && z >= -16 && z <= -14;
                if (lvl > 0 || stairs) {
                    b.column(x, z, bricks(b, x, -1, z, 0.1f), 24);
                } else {
                    b.column(x, z, stone(), 14);
                }
            }
        }
        // Sockel und Fußboden
        for (int x = -11; x <= 11; x++) {
            for (int z = -15; z <= 21; z++) {
                int lvl = plinth(x, z);
                for (int k = 0; k < lvl; k++) {
                    b.set(x, k, z, k == lvl - 1 ? (lvl == 3 ? templeFloor(b, x, z) : polished()) : bricks(b, x, k, z, 0.12f));
                }
            }
        }
        // Freitreppe vorn
        for (int x = -3; x <= 3; x++) {
            for (int k = 0; k < 3; k++) {
                b.set(x, k, -16 + k, VillageBuilder.stairs(ShadowBlocks.POLISHED_SHADOW_STONE.stairs().get(), Direction.SOUTH, Half.BOTTOM));
                for (int below = 0; below < k; below++) {
                    b.set(x, below, -16 + k, bricks(b, x, below, -16 + k, 0.1f));
                }
            }
        }
        for (int sx : new int[] {-4, 4}) {
            for (int k = 0; k < 3; k++) {
                for (int y = 0; y <= k; y++) {
                    b.set(sx, y, -16 + k, y == k ? polished() : bricks(b, sx, y, -16 + k, 0.1f));
                }
            }
        }
        templeWalls(b);
        templePortico(b);
        templeRoof(b, holes);
        templeApse(b);
        templeInterior(b, holes);
        templeCrypt(b);
    }

    private static BlockState templeFloor(VillageBuilder b, int x, int z) {
        int ax = Math.abs(x);
        if (z > -9 && z <= 12 && ax <= 7) {
            if (ax <= 1) {
                if (x == 0 && (z == -3 || z == 4)) {
                    return chiseled();
                }
                return bricks(b, x, 2, z, 0.08f);
            }
            if (ax == 4 && Math.floorMod(z + 6, 4) == 0) {
                return polished();
            }
            return b.chance(x, 2, z, 0.06f) ? s(ShadowBlocks.CRACKED_SHADOW_STONE_BRICKS.get()) : polished();
        }
        return polished();
    }

    private static void templeWalls(VillageBuilder b) {
        // Seitenwände mit Strebepfeilern und hohen Fenstern aus Nachtkristall
        for (int sx : new int[] {-8, 8}) {
            for (int z = -9; z <= 12; z++) {
                for (int y = 3; y <= 10; y++) {
                    BlockState state;
                    boolean window = Math.floorMod(z + 7, 4) == 0 && z <= 9 && y >= 5 && y <= 8;
                    if (window) {
                        state = s(ShadowBlocks.NIGHT_CRYSTAL.get());
                    } else if (y == 3 || y == 10) {
                        state = polished();
                    } else if (Math.floorMod(z + 7, 4) == 0 && (y == 4 || y == 9)) {
                        state = chiseled();
                    } else {
                        state = bricks(b, sx, y, z, 0.14f);
                    }
                    b.set(sx, y, z, state);
                }
            }
            int out = sx > 0 ? 9 : -9;
            for (int z = -9; z <= 11; z += 4) {
                for (int y = 3; y <= 9; y++) {
                    b.set(out, y, z, deep(Direction.Axis.Y));
                }
                b.set(out, 10, z, VillageBuilder.stairs(ShadowBlocks.SHADOW_STONE_BRICKS.stairs().get(), sx > 0 ? Direction.WEST : Direction.EAST,
                        Half.BOTTOM));
            }
        }
        // Stirnwand mit Portal und Augenzeichen darüber
        for (int x = -7; x <= 7; x++) {
            for (int y = 3; y <= 10; y++) {
                boolean door = Math.abs(x) <= 1 && y <= 6;
                BlockState state = door ? Blocks.AIR.defaultBlockState() : y == 3 || y == 10 ? polished() : bricks(b, x, y, -9, 0.12f);
                if (Math.abs(x) <= 1 && y == 7) {
                    state = x == 0 ? chiseled() : polished();
                }
                if (y == 8 && Math.abs(x) <= 1) {
                    state = x == 0 ? s(ShadowBlocks.SOUL_CRYSTAL_BLOCK.get()) : chiseled();
                }
                b.set(x, y, -9, state);
            }
        }
        // Rückwand zwischen Schiff und Apsis (offen in der Mitte)
        for (int x = -8; x <= 8; x++) {
            if (Math.abs(x) <= 5) {
                continue;
            }
            for (int y = 3; y <= 10; y++) {
                b.set(x, y, 12, y == 3 || y == 10 ? polished() : bricks(b, x, y, 12, 0.12f));
            }
        }
    }

    private static void templePortico(VillageBuilder b) {
        for (int x : new int[] {-7, -3, 3, 7}) {
            b.set(x, 3, -12, polished());
            for (int y = 4; y <= 9; y++) {
                b.set(x, y, -12, deep(Direction.Axis.Y));
            }
            b.set(x, 10, -12, chiseled());
        }
        // Gebälk über dem Portikus
        for (int x = -8; x <= 8; x++) {
            for (int z = -13; z <= -10; z++) {
                b.set(x, 11, z, z == -13 ? polished() : bricks(b, x, 11, z, 0.08f));
            }
        }
    }

    private static boolean inHole(VillageBuilder b, double[][] holes, int x, int z) {
        if (Math.abs(x) > 7 || z < -8 || z > 10) {
            return false;
        }
        for (double[] h : holes) {
            double d = Math.sqrt((x - h[0]) * (x - h[0]) + (z - h[1]) * (z - h[1]));
            if (d < h[2] + (b.noise(x, 77, z) - 0.5) * 1.2) {
                return true;
            }
        }
        return false;
    }

    /** Steiles Satteldach (First entlang z) mit Giebeln vorn und hinten; drei Stellen sind eingestürzt. */
    private static void templeRoof(VillageBuilder b, double[][] holes) {
        Block stairs = ShadowBlocks.SHADOW_STONE_BRICKS.stairs().get();
        int z0 = -13;
        int z1 = 12;
        for (int i = 0; ; i++) {
            int left = -9 + i;
            int right = 9 - i;
            int h = 11 + i;
            if (left > right) {
                break;
            }
            if (left == right) {
                for (int z = z0 - 1; z <= z1 + 1; z++) {
                    if (!inHole(b, holes, 0, z)) {
                        b.set(0, h - 1, z, bricks(b, 0, h - 1, z, 0.1f));
                        b.set(0, h, z, VillageBuilder.slab(ShadowBlocks.POLISHED_SHADOW_STONE.slab().get(), SlabType.BOTTOM));
                    }
                }
                break;
            }
            for (int z = z0 - 1; z <= z1 + 1; z++) {
                if (!inHole(b, holes, left, z)) {
                    b.set(left, h, z, VillageBuilder.stairs(stairs, Direction.EAST, Half.BOTTOM));
                }
                if (!inHole(b, holes, right, z)) {
                    b.set(right, h, z, VillageBuilder.stairs(stairs, Direction.WEST, Half.BOTTOM));
                }
            }
            if (i >= 1) {
                for (int x = left + 1; x < right; x++) {
                    b.set(x, h, z0, bricks(b, x, h, z0, 0.1f));
                    b.set(x, h, z1, bricks(b, x, h, z1, 0.1f));
                }
            }
        }
        // Rosenfenster im vorderen Giebel
        for (int x = -2; x <= 2; x++) {
            for (int y = 12; y <= 16; y++) {
                double d = Math.sqrt(x * x + (y - 14.0) * (y - 14.0));
                if (d < 0.5) {
                    b.set(x, y, z0, s(ShadowBlocks.SOUL_CRYSTAL_BLOCK.get()));
                } else if (d < 1.9) {
                    b.set(x, y, z0, s(ShadowBlocks.NIGHT_CRYSTAL.get()));
                } else if (d < 2.6) {
                    b.set(x, y, z0, chiseled());
                }
            }
        }
    }

    /** Apsis: Halbrund mit Halbkuppel, erhöhtem Altarplatz, Mondaltar und Mondsichel aus Mondsilber. */
    private static void templeApse(VillageBuilder b) {
        for (int x = -7; x <= 7; x++) {
            for (int z = 12; z <= 19; z++) {
                double d = Math.sqrt(x * x + (z - 12.0) * (z - 12.0));
                if (d > 6.6) {
                    continue;
                }
                if (d > 5.5 && z > 12) {
                    for (int y = 3; y <= 10; y++) {
                        b.set(x, y, z, y == 3 || y == 10 ? polished() : bricks(b, x, y, z, 0.12f));
                    }
                }
                // Halbkuppel
                int top = 11 + (int) Math.floor((6.6 - d) * 0.85);
                if (z >= 13) {
                    b.set(x, top, z, deep(Direction.Axis.Y));
                    b.set(x, top - 1, z, deep(Direction.Axis.Y));
                }
                // erhöhter Altarplatz
                if (d <= 4.6 && z >= 14) {
                    b.set(x, 3, z, polished());
                } else if (z == 13 && Math.abs(x) <= 4) {
                    b.set(x, 3, z, VillageBuilder.stairs(ShadowBlocks.POLISHED_SHADOW_STONE.stairs().get(), Direction.SOUTH, Half.BOTTOM));
                }
            }
        }
        // Altar mit der Mondreliquie
        for (int x = -1; x <= 1; x++) {
            b.set(x, 4, 15, x == 0 ? chiseled() : polished());
        }
        b.set(0, 5, 15, s(ShadowBlocks.SOUL_CRYSTAL_BLOCK.get()));
        b.set(-1, 5, 15, s(Blocks.SOUL_LANTERN));
        b.set(1, 5, 15, s(Blocks.SOUL_LANTERN));
        b.set(-3, 4, 15, s(Blocks.SOUL_LANTERN));
        b.set(3, 4, 15, s(Blocks.SOUL_LANTERN));
        // Mondsichel und Sterne an der Apsiswand
        for (int x = -4; x <= 4; x++) {
            int wz = (int) Math.round(12.0 + Math.sqrt(Math.max(0.0, 36.0 - x * x)));
            for (int y = 4; y <= 11; y++) {
                double outer = Math.sqrt(x * x + (y - 7.5) * (y - 7.5));
                double inner = Math.sqrt((x - 1.2) * (x - 1.2) + (y - 8.0) * (y - 8.0));
                if (outer <= 3.0 && inner > 2.4) {
                    b.set(x, y, wz, s(ShadowBlocks.MOONSILVER_BLOCK.get()));
                }
            }
        }
        b.set(-3, 10, (int) Math.round(12.0 + Math.sqrt(36.0 - 9)), s(ShadowBlocks.NIGHT_CRYSTAL.get()));
        b.set(3, 6, (int) Math.round(12.0 + Math.sqrt(36.0 - 9)), s(ShadowBlocks.NIGHT_CRYSTAL.get()));
        b.set(2, 10, (int) Math.round(12.0 + Math.sqrt(36.0 - 4)), s(ShadowBlocks.NIGHT_CRYSTAL.get()));
        b.chest(3, 4, 14, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.MOON_TEMPLE_CHEST);
    }

    private static void templeInterior(VillageBuilder b, double[][] holes) {
        // Säulenreihen mit Gebälk
        for (int sx : new int[] {-4, 4}) {
            for (int z = -6; z <= 10; z += 4) {
                b.set(sx, 3, z, polished());
                for (int y = 4; y <= 9; y++) {
                    b.set(sx, y, z, deep(Direction.Axis.Y));
                }
                b.set(sx, 10, z, chiseled());
            }
            for (int z = -9; z <= 12; z++) {
                b.set(sx, 11, z, bricks(b, sx, 11, z, 0.08f));
            }
            for (int z = -4; z <= 8; z += 4) {
                if (!b.chance(sx, 10, z, 0.3f)) {
                    b.set(sx, 10, z, hanging(ShadowBlocks.MOON_LANTERN.get()));
                }
            }
        }
        // Bänke (Blick zum Altar), einige zerfallen
        Block pew = ShadowBlocks.SHADOW_PLANKS.stairs().get();
        for (int z = -7; z <= 7; z += 2) {
            for (int x : new int[] {-3, -2, 2, 3}) {
                float r = b.noise(x, 3, z);
                if (r < 0.18f) {
                    continue;
                }
                b.set(x, 3, z, r < 0.28f ? VillageBuilder.slab(ShadowBlocks.SHADOW_PLANKS.slab().get(), SlabType.BOTTOM)
                        : VillageBuilder.stairs(pew, Direction.NORTH, Half.BOTTOM));
            }
        }
        // Trümmer, Moos und Ranken unter den eingestürzten Stellen
        for (int x = -7; x <= 7; x++) {
            for (int z = -8; z <= 11; z++) {
                if (!inHole(b, holes, x, z)) {
                    continue;
                }
                if (Math.abs(x) <= 7 && b.get(x, 3, z).isAir()) {
                    float r = b.noise(x, 5, z);
                    if (r < 0.35f) {
                        b.set(x, 2, z, s(ShadowBlocks.GLIMMER_MOSS.get()));
                        if (r < 0.15f) {
                            b.set(x, 3, z, s(ShadowBlocks.SHADOW_SPROUTS.get()));
                        }
                    } else if (r < 0.55f) {
                        b.set(x, 3, z, VillageBuilder.slab(ShadowBlocks.SHADOW_STONE_BRICKS.slab().get(), SlabType.BOTTOM));
                    } else if (r < 0.65f) {
                        b.set(x, 3, z, s(ShadowBlocks.CRACKED_SHADOW_STONE_BRICKS.get()));
                    }
                }
            }
        }
        // Ranken hängen vom Dach am Rand der Löcher herab
        for (int x = -7; x <= 7; x++) {
            for (int z = -8; z <= 10; z++) {
                if (inHole(b, holes, x, z) || !b.chance(x, 41, z, 0.35f)) {
                    continue;
                }
                boolean edge = inHole(b, holes, x + 1, z) || inHole(b, holes, x - 1, z) || inHole(b, holes, x, z + 1) || inHole(b, holes, x, z - 1);
                if (edge) {
                    int roof = 11 + (9 - Math.abs(x));
                    vine(b, x, roof - 1, z, 2 + (int) (b.noise(x, 43, z) * 4));
                }
            }
        }
        // Seelenlaternen am Eingang
        b.set(-2, 3, -10, s(Blocks.SOUL_LANTERN));
        b.set(2, 3, -10, s(Blocks.SOUL_LANTERN));
    }

    /** Gruft unter dem hinteren Schiff: Treppe im rechten Seitenschiff, zwei Sarkophage, Laternen, Spinnweben, Truhe. */
    private static void templeCrypt(VillageBuilder b) {
        // Hülle und Raum
        for (int x = -7; x <= 8; x++) {
            for (int z = 3; z <= 12; z++) {
                for (int y = -5; y <= 0; y++) {
                    b.set(x, y, z, y == -5 ? polished() : bricks(b, x, y, z, 0.15f));
                }
            }
        }
        b.air(-6, -4, 4, 7, -1, 11);
        // Treppe hinab
        Block stairs = ShadowBlocks.SHADOW_STONE_BRICKS.stairs().get();
        for (int k = 0; k <= 5; k++) {
            int y = 1 - k;
            int z = -1 + k;
            for (int x : new int[] {6, 7}) {
                b.set(x, y, z, VillageBuilder.stairs(stairs, Direction.NORTH, Half.BOTTOM));
                for (int above = 1; above <= 3; above++) {
                    b.set(x, y + above, z, Blocks.AIR.defaultBlockState());
                }
                for (int below = y - 1; below >= -5; below--) {
                    if (b.get(x, below, z).isAir() && z < 4) {
                        b.set(x, below, z, bricks(b, x, below, z, 0.1f));
                    }
                }
            }
            for (int wx : new int[] {5, 8}) {
                for (int wy = y; wy <= Math.min(1, y + 3); wy++) {
                    if (z < 4) {
                        b.set(wx, wy, z, bricks(b, wx, wy, z, 0.1f));
                    }
                }
            }
        }
        // Sarkophage
        for (int sx : new int[] {-4, 0}) {
            for (int z = 6; z <= 8; z++) {
                b.set(sx, -4, z, deep(Direction.Axis.Z));
                b.set(sx, -3, z, z == 6 ? chiseled() : VillageBuilder.slab(ShadowBlocks.POLISHED_SHADOW_STONE.slab().get(), SlabType.BOTTOM));
            }
        }
        b.set(-6, -4, 4, s(Blocks.SOUL_LANTERN));
        b.set(7, -4, 11, s(Blocks.SOUL_LANTERN));
        b.set(2, -4, 10, s(Blocks.SOUL_LANTERN));
        for (int[] web : new int[][] {{-6, 4}, {-6, 11}, {7, 4}, {3, 11}}) {
            b.set(web[0], -1, web[1], s(Blocks.COBWEB));
        }
        for (int x = -6; x <= 7; x++) {
            for (int z = 4; z <= 11; z++) {
                if (b.chance(x, -5, z, 0.12f) && b.get(x, -4, z).isAir()) {
                    b.set(x, -5, z, s(ShadowBlocks.GLIMMER_MOSS.get()));
                }
            }
        }
        b.chest(-5, -4, 10, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST), ModLootTables.MOON_TEMPLE_CRYPT);
    }

    // =====================================================================================
    // Mondleuchtturm
    // =====================================================================================

    /**
     * Ein Mondleuchtturm an der Küste: Felssockel, runder, nach oben schmaler werdender Turm mit Streifen aus Mondsilber,
     * drei Geschosse mit Leiter und Fenstern, eine Galerie mit Brüstung, die Laterne (Meereslaternen hinter Glas) unter
     * einem spitzen Dach, dazu das Haus des Wärters mit Truhe und ein Steg aufs Meer hinaus (hinten, +z).
     */
    static void lighthouse(VillageBuilder b) {
        // Felssockel
        for (int x = -9; x <= 9; x++) {
            for (int z = -9; z <= 9; z++) {
                double d = Math.sqrt(x * x + z * z);
                double edge = 7.0 + b.noise(x, 3, z) * 1.6;
                if (d > edge) {
                    continue;
                }
                b.column(x, z, stone(), 30);
                if (d <= 5.8) {
                    b.set(x, 0, z, b.chance(x, 0, z, 0.2f) ? stone() : polished());
                } else {
                    b.set(x, 0, z, b.chance(x, 1, z, 0.5f) ? deep(Direction.Axis.Y) : stone());
                    if (b.chance(x, 2, z, 0.3f)) {
                        b.set(x, 1, z, stone());
                    }
                }
            }
        }
        // Turm
        Block planks = ShadowBlocks.SHADOW_PLANKS.base().get();
        for (int y = 1; y <= 26; y++) {
            double r = 4.6 - 0.045 * y;
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    double d = Math.sqrt(x * x + z * z);
                    if (d > r) {
                        continue;
                    }
                    BlockState state;
                    if (d > r - 1.25) {
                        if (y <= 2) {
                            state = deep(Direction.Axis.Y);
                        } else if (Math.floorMod(y, 8) == 5 || Math.floorMod(y, 8) == 6) {
                            state = s(ShadowBlocks.MOONSILVER_BLOCK.get());
                        } else {
                            state = polished();
                        }
                        boolean window = (y == 11 || y == 12 || y == 19 || y == 20) && z == 0;
                        if (window) {
                            state = s(ShadowBlocks.NIGHT_CRYSTAL.get());
                        }
                    } else {
                        state = y == 9 || y == 18 ? s(planks) : Blocks.AIR.defaultBlockState();
                    }
                    b.set(x, y, z, state);
                }
            }
        }
        // Tür (vorn), Leiter (hinten innen) mit Stütze
        b.air(0, 1, -5, 0, 2, -3);
        b.door(0, 1, -4, ShadowBlocks.SHADOW_DOOR.get(), Direction.SOUTH);
        b.set(0, 3, -4, chiseled());
        for (int y = 1; y <= 27; y++) {
            if (4.6 - 0.045 * y - 1.25 >= 3.0 || y == 27) {
                b.set(0, y, 3, polished());       // Stütze der Leiter, wo die Wand noch nicht heranreicht
            }
        }
        b.ladder(0, 1, 27, 2, Direction.NORTH);
        b.hangingLantern(0, 8, 0, ShadowBlocks.MOON_LANTERN.get());
        b.hangingLantern(0, 17, -1, ShadowBlocks.MOON_LANTERN.get());
        // Galerie mit Brüstung
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                double d = Math.sqrt(x * x + z * z);
                if (d <= 5.4 && !(x == 0 && z == 2)) {
                    b.set(x, 27, z, d > 4.4 ? bricks(b, x, 27, z, 0.05f) : polished());
                }
                if (d > 4.6 && d <= 5.4) {
                    b.set(x, 28, z, VillageBuilder.slab(ShadowBlocks.POLISHED_SHADOW_STONE.slab().get(), SlabType.BOTTOM));
                }
            }
        }
        // Laternenraum: ein Glasring (5×5 ohne Ecken) auf einer steinernen Brüstung; innen steht die Lampe frei, so dass
        // ihr Licht von allen Seiten durch das Glas fällt. Zur See hin (über der Leiter) geht es hinaus auf die Galerie.
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                boolean corner = Math.abs(x) == 2 && Math.abs(z) == 2;
                boolean ring = Math.max(Math.abs(x), Math.abs(z)) == 2;
                if (!corner) {
                    boolean doorway = x == 0 && z == 2;
                    for (int y = 28; y <= 31; y++) {
                        BlockState state;
                        if (!ring || (doorway && y <= 29)) {
                            state = Blocks.AIR.defaultBlockState();
                        } else if (y == 28) {
                            state = polished();
                        } else {
                            state = s(Blocks.GLASS);
                        }
                        b.set(x, y, z, state);
                    }
                }
                // Gesims aus Mondsilber, darüber die Decke
                b.set(x, 32, z, ring ? s(ShadowBlocks.MOONSILVER_BLOCK.get()) : polished());
            }
        }
        b.set(0, 28, 0, s(ShadowBlocks.MOONSILVER_BLOCK.get()));
        b.set(0, 29, 0, s(Blocks.SEA_LANTERN));
        b.set(0, 30, 0, s(Blocks.SEA_LANTERN));
        b.set(0, 31, 0, s(ShadowBlocks.NIGHT_CRYSTAL.get()));
        b.hangingLantern(0, 31, 1, ShadowBlocks.MOON_LANTERN.get());
        // Zeltdach aus Schattensteinziegeln, das über das Gesims hinausragt; obenauf Mondsilber und ein Kristall
        int peak = b.hipRoof(-3, -3, 3, 3, 32, ShadowBlocks.SHADOW_STONE_BRICKS.stairs().get(), s(ShadowBlocks.MOONSILVER_BLOCK.get()));
        b.set(0, peak + 1, 0, s(ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get()));
        lighthouseHut(b);
        lighthousePier(b);
    }

    private static void lighthouseHut(VillageBuilder b) {
        Block planks = ShadowBlocks.SHADOW_PLANKS.base().get();
        b.foundation(-11, -8, -6, -3, stone(), stone(), 9);
        b.fill(-11, 0, -8, -6, 0, -3, s(planks));
        for (int x = -11; x <= -6; x++) {
            for (int z = -8; z <= -3; z++) {
                boolean edge = x == -11 || x == -6 || z == -8 || z == -3;
                boolean corner = (x == -11 || x == -6) && (z == -8 || z == -3);
                if (!edge) {
                    continue;
                }
                for (int y = 1; y <= 3; y++) {
                    b.set(x, y, z, corner ? log(Direction.Axis.Y) : y == 2 && (x == -11 || x == -6) && z == -5 ? s(Blocks.GLASS) : s(planks));
                }
            }
        }
        b.door(-8, 1, -8, ShadowBlocks.SHADOW_DOOR.get(), Direction.SOUTH);
        b.set(-10, 2, -8, s(Blocks.GLASS));                                  // Fenster neben der Tür
        b.hipRoof(-12, -9, -5, -2, 4, ShadowBlocks.SHADOW_PLANKS.stairs().get(),
                VillageBuilder.slab(ShadowBlocks.SHADOW_PLANKS.slab().get(), SlabType.BOTTOM));
        // Schornstein aus Ziegeln in der Rückwand, darunter der Ofen
        for (int y = 1; y <= 8; y++) {
            b.set(-10, y, -3, s(Blocks.BRICKS));
        }
        b.set(-10, 9, -3, VillageBuilder.slab(Blocks.BRICK_SLAB, SlabType.BOTTOM));
        b.set(-10, 1, -4, Blocks.FURNACE.defaultBlockState().setValue(net.minecraft.world.level.block.FurnaceBlock.FACING, Direction.NORTH));
        b.hangingLantern(-8, 3, -5, ShadowBlocks.MOON_LANTERN.get());
        b.chest(-7, 1, -4, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.LIGHTHOUSE_CHEST);
        b.bed(-7, 1, -6, Blocks.BED.purple(), Direction.SOUTH);
        b.set(-10, 1, -5, s(Blocks.BARREL));
        b.set(-10, 1, -7, s(Blocks.CRAFTING_TABLE));
        // Stufe vor der Tür und ein Laternenpfahl am Weg
        b.column(-8, -9, stone(), 3);
        b.set(-8, 0, -9, VillageBuilder.stairs(ShadowBlocks.SHADOW_PLANKS.stairs().get(), Direction.SOUTH, Half.BOTTOM));
        b.column(-10, -10, polished(), 3);
        b.set(-10, 0, -10, s(ShadowBlocks.SHADOW_FENCE.get()));
        b.set(-10, 1, -10, s(ShadowBlocks.SHADOW_FENCE.get()));
        b.set(-10, 2, -10, s(ShadowBlocks.MOON_LANTERN.get()));
    }

    /** Steg aufs Meer: Planken knapp über dem Wasser auf Pfählen, am Ende zwei Laternen. */
    private static void lighthousePier(VillageBuilder b) {
        int deck = b.level.getSeaLevel() - b.origin.getY();      // lokale Höhe knapp über dem Wasserspiegel
        if (deck < -6 || deck > 1) {
            return;
        }
        Block planks = ShadowBlocks.SHADOW_PLANKS.base().get();
        for (int z = 6; z <= 21; z++) {
            for (int x = -1; x <= 1; x++) {
                if (b.get(x, deck, z).isSolidRender() && z < 9) {
                    continue;                                     // noch Land: der Fels reicht hier hin
                }
                b.set(x, deck, z, s(planks));
                b.set(x, deck + 1, z, Blocks.AIR.defaultBlockState());
                b.set(x, deck + 2, z, Blocks.AIR.defaultBlockState());
            }
            if (Math.floorMod(z, 3) == 0) {
                for (int x : new int[] {-2, 2}) {
                    for (int y = deck + 1; y > deck - 14; y--) {
                        BlockState old = b.get(x, y, z);
                        if (y < deck && VillageBuilder.isSolidGround(old)) {
                            break;
                        }
                        b.set(x, y, z, log(Direction.Axis.Y));
                    }
                }
            }
        }
        b.set(-2, deck + 2, 21, s(ShadowBlocks.MOON_LANTERN.get()));
        b.set(2, deck + 2, 21, s(ShadowBlocks.MOON_LANTERN.get()));
    }

    // =====================================================================================
    // Ruine der alten Welt
    // =====================================================================================

    /**
     * Ruine eines Hofs aus der alten Welt – aus der Zeit, bevor das Siegel die Welten trennte: Mauerreste aus
     * (bemoostem) Bruchstein, Eckpfosten aus Eiche, ein eingestürzter Dachbalken, ein Kamin aus Ziegeln, eine Werkbank,
     * ein beschädigter Amboss, die Truhe des Hofs, ein Brunnen mit eingefallenem Dach und Reste eines Gartenzauns.
     */
    static void ruin(VillageBuilder b) {
        b.foundation(-7, -6, 7, 7, s(Blocks.COBBLESTONE), s(Blocks.DIRT), 10);
        // Boden: alte Dielen, dazwischen Erde und Moos
        for (int x = -5; x <= 5; x++) {
            for (int z = -4; z <= 5; z++) {
                float r = b.noise(x, 0, z);
                b.set(x, 0, z, r < 0.55f ? s(Blocks.OAK_PLANKS) : r < 0.8f ? s(Blocks.COARSE_DIRT) : s(Blocks.MOSSY_COBBLESTONE));
            }
        }
        // Mauern mit unterschiedlich hohen Resten, Tür vorn, Fensterlücken
        for (int x = -6; x <= 6; x++) {
            for (int z = -5; z <= 6; z++) {
                boolean edge = Math.abs(x) == 6 || z == -5 || z == 6;
                if (!edge) {
                    continue;
                }
                boolean corner = Math.abs(x) == 6 && (z == -5 || z == 6);
                int height = corner ? (b.chance(x, 9, z, 0.5f) ? 5 : 2) : 1 + (int) (b.noise(x, 1, z) * 4.0f);
                for (int y = 0; y <= height; y++) {
                    BlockState state = corner && y >= 1 ? s(Blocks.OAK_LOG)
                            : b.chance(x, y, z, 0.4f) ? s(Blocks.MOSSY_COBBLESTONE) : s(Blocks.COBBLESTONE);
                    boolean door = z == -5 && (x == 0 || x == -1) && y >= 1 && y <= 3;
                    boolean window = Math.abs(x) == 6 && (z == 0 || z == 1) && (y == 2 || y == 3);
                    if (door || window) {
                        continue;
                    }
                    b.set(x, y, z, state);
                }
                // Reste der Dachtraufe
                if (!corner && (z == -5 || z == 6) && height >= 3 && b.chance(x, 11, z, 0.5f)) {
                    b.set(x, height + 1, z, VillageBuilder.stairs(Blocks.OAK_STAIRS, z == -5 ? Direction.SOUTH : Direction.NORTH, Half.BOTTOM));
                }
            }
        }
        // eingestürzter Dachbalken und Trümmer
        for (int x = -4; x <= 3; x++) {
            if (!b.chance(x, 12, 2, 0.15f)) {
                b.set(x, 1, 2, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            }
        }
        b.set(-2, 1, -1, VillageBuilder.slab(Blocks.OAK_SLAB, SlabType.BOTTOM));
        b.set(3, 1, 0, s(Blocks.MOSSY_COBBLESTONE));
        // Kamin mit Feuerstelle
        for (int y = 1; y <= 4 + (int) (b.noise(5, 13, 5) * 3); y++) {
            b.set(5, y, 5, s(Blocks.BRICKS));
        }
        b.set(4, 1, 5, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        // Einrichtung
        b.set(-4, 1, 4, s(Blocks.CRAFTING_TABLE));
        b.set(-5, 1, 4, s(Blocks.BARREL));
        b.set(3, 1, -3, Blocks.DAMAGED_ANVIL.defaultBlockState());
        b.set(4, 1, -4, s(Blocks.POTTED_DEAD_BUSH));
        b.chest(-5, 1, 2, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST), ModLootTables.OLD_WORLD_RUIN_CHEST);
        // Brunnen mit eingefallenem Dach
        for (int x = -10; x <= -8; x++) {
            for (int z = -1; z <= 1; z++) {
                b.column(x, z, s(Blocks.COBBLESTONE), 6);
                if (x == -9 && z == 0) {
                    for (int y = -4; y <= -1; y++) {
                        b.set(x, y, z, y == -4 ? s(Blocks.COBBLESTONE) : s(Blocks.WATER));
                    }
                    b.set(x, 0, z, Blocks.AIR.defaultBlockState());
                } else {
                    for (int y = -4; y <= 1; y++) {
                        b.set(x, y, z, b.chance(x, y, z, 0.35f) ? s(Blocks.MOSSY_COBBLESTONE) : s(Blocks.COBBLESTONE));
                    }
                }
            }
        }
        for (int x : new int[] {-10, -8}) {
            b.set(x, 2, 0, s(Blocks.OAK_FENCE));
            b.set(x, 3, 0, s(Blocks.OAK_FENCE));
        }
        b.set(-10, 4, 0, VillageBuilder.slab(Blocks.OAK_SLAB, SlabType.BOTTOM));
        b.set(-9, 4, 0, VillageBuilder.slab(Blocks.OAK_SLAB, SlabType.BOTTOM));
        // Reste eines Gartenzauns
        for (int z = -4; z <= 5; z++) {
            if (b.chance(8, 1, z, 0.6f)) {
                b.column(8, z, s(Blocks.DIRT), 4);
                b.set(8, 0, z, s(Blocks.OAK_FENCE));
            }
        }
        // Bewuchs der Schattenwelt
        for (int x = -7; x <= 9; x++) {
            for (int z = -6; z <= 7; z++) {
                if (b.get(x, 0, z).isAir() && b.chance(x, 21, z, 0.18f)
                        && b.get(x, -1, z).is(net.minecraft.tags.BlockTags.SUPPORTS_VEGETATION)) {
                    b.set(x, 0, z, s(b.chance(x, 22, z, 0.5f) ? ShadowBlocks.PALE_GRASS.get() : ShadowBlocks.SHADOW_SPROUTS.get()));
                }
            }
        }
    }

    // =====================================================================================
    // Kristallmine
    // =====================================================================================

    private static final int HALL = -26;

    /** Hohlraum ausräumen (Grenzen eingeschlossen). */
    private static void carve(VillageBuilder b, int x0, int y0, int z0, int x1, int y1, int z1) {
        b.air(x0, y0, z0, x1, y1, z1);
    }

    /** Wände rund um einen Hohlraum dicht machen (Höhlen, Wasser) und Erz-/Kristalladern einstreuen. */
    private static void walls(VillageBuilder b, int x0, int y0, int z0, int x1, int y1, int z1) {
        for (int x = x0 - 1; x <= x1 + 1; x++) {
            for (int y = y0 - 1; y <= y1 + 1; y++) {
                for (int z = z0 - 1; z <= z1 + 1; z++) {
                    boolean inside = x >= x0 && x <= x1 && y >= y0 && y <= y1 && z >= z0 && z <= z1;
                    if (inside) {
                        continue;
                    }
                    BlockState old = b.get(x, y, z);
                    if (!old.isAir() && old.getFluidState().isEmpty() && !old.is(Blocks.CHEST)) {
                        float r = b.noise(x, y, z);
                        if (r < 0.035f) {
                            b.set(x, y, z, s(ShadowBlocks.SOUL_CRYSTAL_ORE.get()));
                        } else if (r < 0.07f) {
                            b.set(x, y, z, s(ShadowBlocks.NIGHT_CRYSTAL.get()));
                        } else if (r < 0.1f) {
                            b.set(x, y, z, s(ShadowBlocks.DEEP_MOONSILVER_ORE.get()));
                        }
                        continue;
                    }
                    if (old.isAir() || !old.getFluidState().isEmpty()) {
                        b.set(x, y, z, b.chance(x, y, z, 0.5f) ? stone() : deep(Direction.Axis.Y));
                    }
                }
            }
        }
    }

    /**
     * Eine verlassene Kristallmine: oben ein Förderturm aus Schattenholz mit Plattform, Geländer, Haspel und Dach über
     * dem Schacht; eine Leiter führt 26 Blöcke hinab in eine Halle mit Stützbalken, Laternen, Truhe und Fässern. Von
     * dort gehen zwei Stollen mit Gleisen ab: nach Osten bis zu einer Lore mit Beute, nach Westen in eine Kristallkammer
     * voller Nachtkristall – in der ein Spawner Kristallkriecher hervorbringt. In den Wänden glitzern Seelenkristall-
     * und Mondsilberadern.
     */
    static void mine(VillageBuilder b) {
        Block planks = ShadowBlocks.SHADOW_PLANKS.base().get();
        // Plattform und Förderturm
        b.foundation(-4, -4, 4, 4, log(Direction.Axis.Y), s(planks), 10);
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                boolean shaft = Math.abs(x) <= 1 && Math.abs(z) <= 1;
                if (!shaft) {
                    b.set(x, 0, z, Math.abs(x) == 4 || Math.abs(z) == 4 ? log(Math.abs(x) == 4 ? Direction.Axis.Z : Direction.Axis.X) : s(planks));
                }
                if (Math.max(Math.abs(x), Math.abs(z)) == 2 && !(x == 0 && z == -2)) {
                    b.set(x, 1, z, s(ShadowBlocks.SHADOW_FENCE.get()));
                }
            }
        }
        for (int sx : new int[] {-3, 3}) {
            for (int sz : new int[] {-3, 3}) {
                for (int y = 1; y <= 7; y++) {
                    b.set(sx, y, sz, log(Direction.Axis.Y));
                }
            }
        }
        for (int i = -3; i <= 3; i++) {
            b.set(i, 8, -3, log(Direction.Axis.X));
            b.set(i, 8, 3, log(Direction.Axis.X));
            b.set(-3, 8, i, log(Direction.Axis.Z));
            b.set(3, 8, i, log(Direction.Axis.Z));
            b.set(i, 8, 0, log(Direction.Axis.X));
        }
        for (int x = -2; x <= 2; x++) {
            b.set(x, 6, 1, log(Direction.Axis.X));                   // Haspel
        }
        b.hangingLantern(0, 7, 0, Blocks.SOUL_LANTERN);
        b.hipRoof(-4, -4, 4, 4, 9, ShadowBlocks.SHADOW_PLANKS.stairs().get(),
                VillageBuilder.slab(ShadowBlocks.SHADOW_PLANKS.slab().get(), SlabType.BOTTOM));
        // Schacht mit Leiter
        carve(b, -1, HALL + 5, -1, 1, -1, 1);
        for (int y = HALL + 5; y <= -1; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) != 2) {
                        continue;
                    }
                    if (Math.floorMod(y, 4) == 0) {
                        b.set(x, y, z, s(planks));
                    } else {
                        BlockState old = b.get(x, y, z);
                        if (old.isAir() || !old.getFluidState().isEmpty()) {
                            b.set(x, y, z, stone());
                        }
                    }
                }
            }
        }
        // Halle
        carve(b, -8, HALL, -5, 8, HALL + 5, 5);
        walls(b, -8, HALL, -5, 8, HALL + 5, 5);
        for (int x = -8; x <= 8; x++) {
            for (int z = -5; z <= 5; z++) {
                b.set(x, HALL - 1, z, b.chance(x, HALL - 1, z, 0.3f) ? stone() : s(planks));
            }
        }
        for (int y = HALL; y <= -1; y++) {
            b.set(0, y, -2, y <= HALL + 5 ? log(Direction.Axis.Y) : s(planks));
        }
        b.ladder(0, HALL, 0, -1, Direction.SOUTH);
        for (int x : new int[] {-6, -3, 3, 6}) {
            for (int y = HALL; y <= HALL + 4; y++) {
                b.set(x, y, -5, log(Direction.Axis.Y));
                b.set(x, y, 5, log(Direction.Axis.Y));
            }
            for (int z = -5; z <= 5; z++) {
                b.set(x, HALL + 5, z, log(Direction.Axis.Z));
            }
            b.hangingLantern(x, HALL + 4, 0, Blocks.SOUL_LANTERN);
        }
        b.chest(7, HALL, -4, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), ModLootTables.CRYSTAL_MINE_CHEST);
        b.set(7, HALL, -3, s(Blocks.BARREL));
        b.set(-7, HALL, 4, s(Blocks.BARREL));
        b.set(-7, HALL, -4, s(Blocks.CRAFTING_TABLE));
        b.set(6, HALL, 4, s(ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get()));
        b.set(-6, HALL, -3, s(ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get()));
        // Stollen mit Gleisen
        for (int dir : new int[] {-1, 1}) {
            int from = 9 * dir;
            int to = 24 * dir;
            int x0 = Math.min(from, to);
            int x1 = Math.max(from, to);
            carve(b, x0, HALL, -1, x1, HALL + 2, 1);
            walls(b, x0, HALL, -1, x1, HALL + 2, 1);
            for (int x = x0; x <= x1; x++) {
                b.set(x, HALL - 1, 0, stone());
                if (!b.chance(x, HALL, 0, 0.15f)) {
                    b.set(x, HALL, 0, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST));
                }
                if (Math.floorMod(x, 4) == 0) {
                    for (int y = HALL; y <= HALL + 2; y++) {
                        b.set(x, y, -2, log(Direction.Axis.Y));
                        b.set(x, y, 2, log(Direction.Axis.Y));
                    }
                    for (int z = -2; z <= 2; z++) {
                        b.set(x, HALL + 3, z, log(Direction.Axis.Z));
                    }
                    if (Math.floorMod(x, 8) == 0) {
                        b.hangingLantern(x, HALL + 2, 0, Blocks.SOUL_LANTERN);
                    }
                }
            }
        }
        // Lore mit Beute am Ende des Oststollens
        BlockPos cart = b.world(23, HALL, 0);
        if (b.inside(cart)) {
            b.set(23, HALL, 0, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST));
            MinecartChest chest = EntityTypes.CHEST_MINECART.create(b.level.getLevel(), EntitySpawnReason.CHUNK_GENERATION);
            if (chest != null) {
                chest.setInitialPos(cart.getX() + 0.5, cart.getY() + 0.1, cart.getZ() + 0.5);
                chest.setLootTable(ModLootTables.CRYSTAL_MINE_CHEST, cart.asLong() ^ 0x5EEDL);
                b.level.addFreshEntity(chest);
            }
        }
        crystalChamber(b);
    }

    /** Kristallkammer am Ende des Weststollens: unregelmäßige Höhle voller Nachtkristall, in der Mitte ein Spawner. */
    private static void crystalChamber(VillageBuilder b) {
        int cx = -29;
        int cy = HALL + 2;
        for (int x = cx - 6; x <= cx + 6; x++) {
            for (int y = cy - 4; y <= cy + 4; y++) {
                for (int z = -6; z <= 6; z++) {
                    double dx = (x - cx) / 5.2;
                    double dy = (y - cy) / 3.4;
                    double dz = z / 5.0;
                    double d = Math.sqrt(dx * dx + dy * dy + dz * dz) + (b.noise(x, y, z) - 0.5) * 0.25;
                    if (d <= 1.0) {
                        b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    } else if (d <= 1.25) {
                        BlockState old = b.get(x, y, z);
                        float r = b.noise(x, y + 50, z);
                        if (r < 0.25f) {
                            b.set(x, y, z, s(ShadowBlocks.NIGHT_CRYSTAL.get()));
                        } else if (r < 0.32f) {
                            b.set(x, y, z, s(ShadowBlocks.SOUL_CRYSTAL_ORE.get()));
                        } else if (old.isAir() || !old.getFluidState().isEmpty()) {
                            b.set(x, y, z, stone());
                        }
                    }
                }
            }
        }
        // Kristalle auf dem Boden der Kammer
        for (int x = cx - 5; x <= cx + 5; x++) {
            for (int z = -5; z <= 5; z++) {
                for (int y = cy - 4; y <= cy + 2; y++) {
                    if (b.get(x, y, z).isAir() && !b.get(x, y - 1, z).isAir() && b.chance(x, y, z, 0.18f)) {
                        b.set(x, y, z, s(ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get()));
                        break;
                    }
                }
            }
        }
        // Spawner der Kristallkriecher
        BlockPos spawner = b.world(cx, HALL, 0);
        b.set(cx, HALL - 1, 0, s(ShadowBlocks.NIGHT_CRYSTAL.get()));
        b.set(cx, HALL, 0, s(Blocks.SPAWNER));
        if (b.inside(spawner) && b.level.getBlockEntity(spawner) instanceof SpawnerBlockEntity be) {
            be.setEntityId(ModEntities.CRYSTAL_CRAWLER.get(), RandomSource.create(spawner.asLong()));
        }
        // Übergang vom Stollen in die Kammer
        b.air(-24, HALL, -1, -23, HALL + 2, 1);
        for (int x = -25; x >= -26; x--) {
            for (int y = HALL; y <= HALL + 2; y++) {
                for (int z = -1; z <= 1; z++) {
                    b.set(x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }
}
