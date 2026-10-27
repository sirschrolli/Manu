package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Der Dorfplatz: gepflastert (im Pfahldorf eine Plattform auf Pfählen), in der Mitte ein Brunnen mit Dach und Glocke,
 * eine Dorflinde mit Bank, ein Maibaum, ein Springbrunnen oder eine Feuerstelle; ringsum Bänke, Laternen und zwei, drei
 * Marktstände zwischen den Wegen.
 */
final class VillageSquare {
    private VillageSquare() {}

    static void build(VillageBuilder b, VillagePalette p, KingdomVillagePiece piece) {
        int r = piece.radius();
        int arms = piece.feature() & 15;
        int centerpiece = (piece.feature() >> 4) & 3;
        Arch a = new Arch(b, p);
        boolean stilt = p.style == VillageStyle.STILT;
        // Pflaster bzw. Plattform
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                double d = Math.sqrt(x * x + z * z);
                if (d > r + 0.5) {
                    continue;
                }
                if (stilt) {
                    boolean post = Math.floorMod(x, 3) == 0 && Math.floorMod(z, 3) == 0 || d > r - 0.6 && Math.floorMod(x + z, 2) == 0;
                    a.stilt(x, z, post);
                    clear(b, x, z, 12);
                } else {
                    b.column(x, z, p.stone(), 12);
                }
                b.set(x, 0, z, pavement(b, p, x, z, d, r));
            }
        }
        // Mittelstück
        switch (p.style) {
            case TIMBER -> {
                if (centerpiece % 2 == 0) {
                    well(b, a, p);
                } else {
                    linden(b, a, p);
                }
            }
            case NORDIC -> {
                if (centerpiece % 2 == 0) {
                    well(b, a, p);
                } else {
                    maypole(b, a, p);
                }
            }
            case CLAY -> fountain(b, a, p, centerpiece % 2 == 0);
            case STILT -> firePit(b, a, p);
        }
        // Laternen an den Diagonalen
        int lamp = Math.max(3, r - 2);
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                lampPost(b, p, sx * lamp, sz * lamp);
            }
        }
        // Marktstände und Bänke zwischen den Wegen
        int stalls = 0;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if ((arms & (1 << dir.get2DDataValue())) != 0) {
                continue;
            }
            // diese Seite ist frei: ein Marktstand (höchstens drei), sonst Bänke
            if (stalls < 3) {
                stall(b, a, p, piece, dir, r, stalls);
                stalls++;
            }
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            Direction side = dir.getClockWise();
            int bx = dir.getStepX() * (r - 2) + side.getStepX() * 3;
            int bz = dir.getStepZ() * (r - 2) + side.getStepZ() * 3;
            if (b.get(bx, 1, bz).isAir()) {
                b.set(bx, 1, bz, a.bench(dir.getOpposite()));
            }
        }
        // zwei Wächter auf dem Platz
        piece.guard(b, 2, 1, -r + 2, false);
        piece.guard(b, -2, 1, r - 2, b.random.nextBoolean());
    }

    private static void clear(VillageBuilder b, int x, int z, int height) {
        for (int y = 1; y < height; y++) {
            if (!b.get(x, y, z).isAir()) {
                b.set(x, y, z, Blocks.AIR.defaultBlockState());
            }
        }
    }

    /** Pflaster: Ringe und ein Mittelfeld, aus zwei Steinen gemischt (Lehm: Sandstein mit Terrakotta). */
    private static BlockState pavement(VillageBuilder b, VillagePalette p, int x, int z, double d, int r) {
        float n = b.noise(x, 0, z);
        return switch (p.style) {
            case TIMBER -> d > r - 0.7 ? p.stone() : (d > 3.5 && d < 4.5) ? ModBlocks.ROUGH_FIELDSTONE.base().get().defaultBlockState()
                    : n < 0.18f ? Blocks.COBBLESTONE.defaultBlockState() : n < 0.32f ? p.stoneAlt() : p.stone();
            case NORDIC -> d > r - 0.7 ? p.stone() : n < 0.4f ? Blocks.COBBLESTONE.defaultBlockState()
                    : n < 0.7f ? Blocks.GRAVEL.defaultBlockState() : n < 0.85f ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : p.stone();
            case CLAY -> d > r - 0.7 ? Blocks.CUT_SANDSTONE.defaultBlockState()
                    : (d > 3.5 && d < 4.5) ? Blocks.DYED_TERRACOTTA.orange().defaultBlockState()
                    : (x + z & 1) == 0 ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : Blocks.CUT_SANDSTONE.defaultBlockState();
            case STILT -> d > r - 0.7 ? p.trimPlanks() : p.planks();
        };
    }

    private static void lampPost(VillageBuilder b, VillagePalette p, int x, int z) {
        b.set(x, 1, z, p.style == VillageStyle.STILT || p.style == VillageStyle.NORDIC ? p.post(Direction.Axis.Y) : p.stone());
        b.set(x, 2, z, p.fence());
        b.set(x, 3, z, p.fence());
        b.set(x, 4, z, p.lantern.defaultBlockState().setValue(LanternBlock.HANGING, false));
    }

    // =====================================================================================
    // Mittelstücke
    // =====================================================================================

    /** Brunnen: Steinkranz um ein tiefes Becken, vier Pfosten mit einem kleinen Dach, darunter die Dorfglocke. */
    private static void well(VillageBuilder b, Arch a, VillagePalette p) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                boolean rim = Math.abs(x) == 2 || Math.abs(z) == 2;
                if (rim) {
                    b.set(x, 0, z, p.stone());
                    b.set(x, 1, z, (Math.abs(x) == 2 && Math.abs(z) == 2) ? p.stone() : Arch.slab(p.stoneSlab(), SlabType.BOTTOM));
                } else {
                    for (int y = -6; y <= 0; y++) {
                        b.set(x, y, z, Blocks.WATER.defaultBlockState());
                    }
                    b.set(x, -7, z, p.stone());
                    for (int sx : new int[] {-2, 2}) {
                        for (int y = -6; y <= 0; y++) {
                            b.set(sx, y, z, p.stone());
                            b.set(z, y, sx, p.stone());
                        }
                    }
                }
            }
        }
        // Pfosten an den Ecken und Dach
        for (int sx = -2; sx <= 2; sx += 4) {
            for (int sz = -2; sz <= 2; sz += 4) {
                b.set(sx, 2, sz, p.fence());
                b.set(sx, 3, sz, p.fence());
                b.set(sx, 4, sz, p.post(Direction.Axis.Y));
            }
        }
        for (int x = -2; x <= 2; x++) {
            b.set(x, 4, -2, p.post(Direction.Axis.X));
            b.set(x, 4, 2, p.post(Direction.Axis.X));
        }
        b.set(-2, 4, 0, p.post(Direction.Axis.Z));
        b.set(2, 4, 0, p.post(Direction.Axis.Z));
        // Satteldach entlang x
        Block stairs = a.roofStairs();
        for (int x = -3; x <= 3; x++) {
            b.set(x, 5, -3, Arch.stairs(stairs, Direction.SOUTH, Half.BOTTOM));
            b.set(x, 5, 3, Arch.stairs(stairs, Direction.NORTH, Half.BOTTOM));
            b.set(x, 6, -2, Arch.stairs(stairs, Direction.SOUTH, Half.BOTTOM));
            b.set(x, 6, 2, Arch.stairs(stairs, Direction.NORTH, Half.BOTTOM));
            b.set(x, 7, -1, Arch.stairs(stairs, Direction.SOUTH, Half.BOTTOM));
            b.set(x, 7, 1, Arch.stairs(stairs, Direction.NORTH, Half.BOTTOM));
            b.set(x, 7, 0, a.roofBlock());
            b.set(x, 8, 0, a.roofSlab(SlabType.BOTTOM));
        }
        // Giebelfelder
        for (int sx = -2; sx <= 2; sx += 4) {
            b.set(sx, 5, -1, p.trimPlanks());
            b.set(sx, 5, 0, p.trimPlanks());
            b.set(sx, 5, 1, p.trimPlanks());
            b.set(sx, 6, 0, p.trimPlanks());
            b.set(sx, 6, -1, p.trimPlanks());
            b.set(sx, 6, 1, p.trimPlanks());
        }
        // Querbalken mit der Glocke darunter
        b.fill(-1, 5, 0, 1, 5, 0, p.post(Direction.Axis.X));
        b.set(0, 4, 0, Blocks.BELL.defaultBlockState().setValue(BellBlock.FACING, Direction.EAST).setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING));
        // Eimer und Fass neben dem Brunnen
        b.set(3, 1, -1, Blocks.BARREL.defaultBlockState());
        b.set(3, 1, 1, Blocks.WATER_CAULDRON.defaultBlockState().setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 3));
    }

    /**
     * Dorflinde: ein mächtiger Laubbaum, um den Stamm ein Kranz aus Steinstufen zum Sitzen, an einem Pfosten daneben die
     * Glocke.
     */
    private static void linden(VillageBuilder b, Arch a, VillagePalette p) {
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true);
        // Sitzkranz
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    Direction out = Math.abs(x) == 2 && Math.abs(z) < 2 ? (x > 0 ? Direction.EAST : Direction.WEST)
                            : Math.abs(z) == 2 && Math.abs(x) < 2 ? (z > 0 ? Direction.SOUTH : Direction.NORTH) : null;
                    b.set(x, 1, z, out == null ? p.stone() : Arch.stairs(p.stoneStairs(), out.getOpposite(), Half.BOTTOM));
                } else {
                    b.set(x, 1, z, Blocks.GRASS_BLOCK.defaultBlockState());
                    b.set(x, 0, z, Blocks.DIRT.defaultBlockState());
                }
            }
        }
        // Stamm (2×2 unten, oben einer) und eine breite, runde Krone
        for (int y = 2; y <= 7; y++) {
            b.set(0, y, 0, log);
            if (y <= 3) {
                b.set(1, y, 0, log);
                b.set(0, y, 1, log);
            }
        }
        int[][] arms = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] arm : arms) {
            b.set(arm[0], 6, arm[1], log.setValue(RotatedPillarBlock.AXIS, arm[0] != 0 ? Direction.Axis.X : Direction.Axis.Z));
            b.set(arm[0] * 2, 7, arm[1] * 2, log.setValue(RotatedPillarBlock.AXIS, arm[0] != 0 ? Direction.Axis.X : Direction.Axis.Z));
        }
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                for (int y = 6; y <= 11; y++) {
                    double ex = x / 5.2;
                    double ez = z / 5.2;
                    double ey = (y - 8.6) / 3.0;
                    double e = ex * ex + ey * ey + ez * ez;
                    if (e <= 1.0 - 0.25 * b.noise(x, y, z) && b.get(x, y, z).isAir()) {
                        b.set(x, y, z, leaves);
                    }
                }
            }
        }
        // Glocke an einem Pfosten neben dem Baum
        b.set(4, 1, -4, p.stone());
        b.set(4, 2, -4, p.fence());
        b.set(4, 3, -4, p.post(Direction.Axis.Y));
        b.set(3, 3, -4, p.post(Direction.Axis.X));
        b.set(3, 2, -4, Blocks.BELL.defaultBlockState().setValue(BellBlock.FACING, Direction.NORTH).setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING));
    }

    /** Maibaum: ein hoher, geschälter Stamm mit einem Kranz aus Laub und bunten Bändern; ringsum ein Steinkreis. */
    private static void maypole(VillageBuilder b, Arch a, VillagePalette p) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                b.set(x, 0, z, p.stone());
            }
        }
        for (int y = 1; y <= 12; y++) {
            b.set(0, y, 0, p.beam(Direction.Axis.Y));
        }
        BlockState wreath = Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            b.set(dir.getStepX(), 10, dir.getStepZ(), wreath);
            b.set(dir.getStepX(), 11, dir.getStepZ(), dir.getAxis() == Direction.Axis.X ? wreath : Blocks.AIR.defaultBlockState());
        }
        b.set(1, 10, 1, wreath);
        b.set(-1, 10, -1, wreath);
        b.set(1, 10, -1, wreath);
        b.set(-1, 10, 1, wreath);
        b.set(0, 13, 0, Blocks.LANTERN.defaultBlockState());
        // Bänder: Wolle in Rot und Weiß an langen Ketten bis zum Boden
        Block[] wool = {Blocks.WOOL.red(), Blocks.WOOL.white(), Blocks.WOOL.yellow(), Blocks.WOOL.blue()};
        int i = 0;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int x = dir.getStepX() * 2;
            int z = dir.getStepZ() * 2;
            b.set(x, 9, z, wool[i++ % wool.length].defaultBlockState());
            for (int y = 2; y <= 8; y++) {
                b.set(x, y, z, Blocks.IRON_CHAIN.defaultBlockState());
            }
            b.set(x, 1, z, p.stone());
        }
        // die Glocke an einem Gestell
        b.set(4, 1, 3, p.post(Direction.Axis.Y));
        b.set(4, 2, 3, p.post(Direction.Axis.Y));
        b.set(4, 3, 3, p.post(Direction.Axis.Y));
        b.set(4, 1, 5, p.post(Direction.Axis.Y));
        b.set(4, 2, 5, p.post(Direction.Axis.Y));
        b.set(4, 3, 5, p.post(Direction.Axis.Y));
        b.set(4, 4, 3, a.roofSlab(SlabType.BOTTOM));
        b.set(4, 4, 4, p.post(Direction.Axis.Z));
        b.set(4, 4, 5, a.roofSlab(SlabType.BOTTOM));
        b.set(4, 3, 4, Blocks.BELL.defaultBlockState().setValue(BellBlock.FACING, Direction.EAST).setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING));
    }

    /** Springbrunnen im Lehmdorf: ein achteckiges Becken, in der Mitte eine Säule mit Schale; oder ein Brunnen mit Kuppel. */
    private static void fountain(VillageBuilder b, Arch a, VillagePalette p, boolean basin) {
        BlockState rim = Blocks.CUT_SANDSTONE.defaultBlockState();
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                int ax = Math.abs(x);
                int az = Math.abs(z);
                if (ax + az > 5) {
                    continue;
                }
                boolean edge = ax == 3 || az == 3 || ax + az == 5;
                if (edge) {
                    b.set(x, 0, z, rim);
                    b.set(x, 1, z, Arch.slab(Blocks.SMOOTH_SANDSTONE_SLAB, SlabType.BOTTOM));
                } else {
                    b.set(x, -1, z, Blocks.SMOOTH_SANDSTONE.defaultBlockState());
                    b.set(x, 0, z, Blocks.WATER.defaultBlockState());
                    b.set(x, 1, z, Blocks.AIR.defaultBlockState());
                }
            }
        }
        if (basin) {
            b.set(0, 0, 0, Blocks.CHISELED_SANDSTONE.defaultBlockState());
            b.set(0, 1, 0, Blocks.SANDSTONE_WALL.defaultBlockState());
            b.set(0, 2, 0, Blocks.SANDSTONE_WALL.defaultBlockState());
            b.set(0, 3, 0, Blocks.CHISELED_SANDSTONE.defaultBlockState());
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                b.set(dir.getStepX(), 3, dir.getStepZ(), Arch.stairs(Blocks.SANDSTONE_STAIRS, dir.getOpposite(), Half.TOP));
            }
            b.set(0, 4, 0, Blocks.WATER.defaultBlockState());
        } else {
            // Brunnenhaus mit Kuppel aus Kalkputz über einem kleinen Schacht
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    b.set(sx, 1, sz, Blocks.SANDSTONE_WALL.defaultBlockState());
                    b.set(sx, 2, sz, Blocks.SANDSTONE_WALL.defaultBlockState());
                    b.set(sx, 3, sz, Blocks.CUT_SANDSTONE.defaultBlockState());
                }
            }
            BlockState plaster = ModBlocks.WHITE_PLASTER.base().get().defaultBlockState();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) {
                        b.set(0, 4, 0, plaster);
                        b.set(0, 5, 0, Arch.slab(ModBlocks.WHITE_PLASTER.slab().get(), SlabType.BOTTOM));
                    } else if (Math.abs(x) + Math.abs(z) == 1) {
                        b.set(x, 3, z, Blocks.CUT_SANDSTONE.defaultBlockState());
                        b.set(x, 4, z, Arch.stairs(ModBlocks.WHITE_PLASTER.stairs().get(), Direction.getApproximateNearest(-x, 0, -z), Half.BOTTOM));
                    }
                }
            }
            b.set(0, 3, 0, Blocks.BELL.defaultBlockState().setValue(BellBlock.FACING, Direction.EAST).setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING));
        }
        // Palmen-Kübel an zwei Ecken
        b.set(5, 1, 5, Blocks.POTTED_CACTUS.defaultBlockState());
        b.set(-5, 1, -5, Blocks.POTTED_ACACIA_SAPLING.defaultBlockState());
    }

    /** Feuerstelle im Pfahldorf: ein Ring aus Lehmziegeln mit Lagerfeuer, Bänke ringsum, eine Glocke an einem Pfahl. */
    private static void firePit(VillageBuilder b, Arch a, VillagePalette p) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                b.set(x, 0, z, Blocks.MUD_BRICKS.defaultBlockState());
            }
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            b.set(dir.getStepX(), 1, dir.getStepZ(), Arch.slab(Blocks.MUD_BRICK_SLAB, SlabType.BOTTOM));
            Direction side = dir.getClockWise();
            b.set(dir.getStepX() + side.getStepX(), 1, dir.getStepZ() + side.getStepZ(), Blocks.MUD_BRICKS.defaultBlockState());
            b.set(dir.getStepX() * 3, 1, dir.getStepZ() * 3, a.bench(dir.getOpposite()));
        }
        b.set(0, 1, 0, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        // Glockenpfahl
        b.set(-4, 1, 3, p.post(Direction.Axis.Y));
        b.set(-4, 2, 3, p.post(Direction.Axis.Y));
        b.set(-4, 3, 3, p.post(Direction.Axis.Y));
        b.set(-3, 3, 3, p.fence());
        b.set(-3, 2, 3, Blocks.BELL.defaultBlockState().setValue(BellBlock.FACING, Direction.NORTH).setValue(BellBlock.ATTACHMENT, BellAttachType.CEILING));
    }

    // =====================================================================================
    // Marktstände
    // =====================================================================================

    /**
     * Ein Marktstand am Platzrand (Seite {@code dir}, ohne Weg): Theke aus Fässern und Brettern, zwei Pfosten mit einem
     * Sonnensegel aus Wolle, Waren auf der Theke; dahinter steht der Händler.
     */
    private static void stall(VillageBuilder b, Arch a, VillagePalette p, KingdomVillagePiece piece, Direction dir, int r, int index) {
        Direction side = dir.getClockWise();
        int depth = r - 2;
        int[] colors = {0, 1, 2};
        Block[] awnings = switch (p.style) {
            case TIMBER -> new Block[] {Blocks.WOOL.red(), Blocks.WOOL.blue(), Blocks.WOOL.green()};
            case NORDIC -> new Block[] {Blocks.WOOL.red(), Blocks.WOOL.gray(), Blocks.WOOL.white()};
            case CLAY -> new Block[] {Blocks.WOOL.orange(), Blocks.WOOL.cyan(), Blocks.WOOL.yellow()};
            case STILT -> new Block[] {Blocks.WOOL.green(), Blocks.WOOL.brown(), Blocks.WOOL.lime()};
        };
        Block awning = awnings[colors[index % 3]];
        Block stripe = Blocks.WOOL.white();
        for (int w = -1; w <= 1; w++) {
            int x = dir.getStepX() * depth + side.getStepX() * w;
            int z = dir.getStepZ() * depth + side.getStepZ() * w;
            // Theke
            b.set(x, 1, z, w == 0 ? Arch.slab(p.trim.slab(), SlabType.TOP) : Blocks.BARREL.defaultBlockState());
            // Rückseite: Pfosten und Segel
            int bx = x + dir.getStepX();
            int bz = z + dir.getStepZ();
            if (w != 0) {
                b.set(bx, 1, bz, p.fence());
                b.set(bx, 2, bz, p.fence());
                b.set(x - dir.getStepX(), 1, z - dir.getStepZ(), Blocks.AIR.defaultBlockState());
            }
            b.set(bx, 3, bz, (w == 0 ? stripe : awning).defaultBlockState());
            b.set(x, 3, z, (w == 0 ? awning : stripe).defaultBlockState());
            b.set(x - dir.getStepX(), 3, z - dir.getStepZ(), Arch.slab(p.trim.slab(), SlabType.BOTTOM));
            if (w != 0) {
                b.set(x - dir.getStepX(), 2, z - dir.getStepZ(), p.fence());
                b.set(x - dir.getStepX(), 1, z - dir.getStepZ(), p.fence());
            }
        }
        // Waren auf der Theke
        int cx = dir.getStepX() * depth;
        int cz = dir.getStepZ() * depth;
        BlockState goods = switch (index % 3) {
            case 0 -> Blocks.CARVED_PUMPKIN.defaultBlockState();
            case 1 -> Blocks.MELON.defaultBlockState();
            default -> Blocks.HAY_BLOCK.defaultBlockState();
        };
        b.set(cx, 2, cz, a.flower(cx, 2, cz));
        b.set(cx + side.getStepX(), 2, cz + side.getStepZ(), goods);
        // der Händler hinter der Theke
        piece.resident(b, cx + dir.getStepX(), 1, cz + dir.getStepZ(), VillageProfession.MERCHANT, null,
                new BlockPos(cx + dir.getStepX(), 1, cz + dir.getStepZ()));
    }
}
