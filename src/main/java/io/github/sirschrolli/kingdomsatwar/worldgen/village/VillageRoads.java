package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

/**
 * Die Wege eines Dorfs: drei Blöcke breit, je nach Bauweise ein Trampelpfad mit Kies (Fachwerk), Kies und Kopfsteine
 * (nordisch), Sandsteinplatten (Lehm) oder ein Bohlensteg (Pfahlbau). Wo der Weg eine Stufe nimmt, liegen Steinstufen;
 * über Wasser und Senken führt eine Brücke mit Geländer, alle paar Blöcke steht ein Laternenpfahl am Rand.
 */
final class VillageRoads {
    private VillageRoads() {}

    static void build(VillageBuilder b, VillagePalette p, KingdomVillagePiece piece) {
        int[] heights = piece.heights();
        int[] flags = piece.flags();
        Direction dir = piece.direction();
        Direction side = dir.getClockWise();
        BlockPos start = piece.origin();
        boolean stilt = p.style == VillageStyle.STILT;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int length = heights.length - 1;
        for (int t = piece.skip(); t <= length; t++) {
            int y = heights[t];
            int cx = start.getX() + dir.getStepX() * t;
            int cz = start.getZ() + dir.getStepZ() * t;
            boolean bridge = (flags[t] & KingdomVillagePiece.BRIDGE) != 0;
            boolean railings = bridge && (!stilt || overWater(b, cx, y, cz));
            // Stufe: der höhere Block bekommt eine Treppe zum niedrigeren Nachbarn hin
            int prev = t > 0 ? heights[t - 1] : y;
            int next = t < length ? heights[t + 1] : y;
            Direction stairs = null;
            if (!bridge && !stilt) {
                if (prev == y - 1 && next >= y) {
                    stairs = dir;
                } else if (next == y - 1 && prev >= y) {
                    stairs = dir.getOpposite();
                }
            }
            int reach = railings ? 2 : 1;
            for (int w = -reach; w <= reach; w++) {
                int x = cx + side.getStepX() * w;
                int z = cz + side.getStepZ() * w;
                if (!b.chunkBox.isInside(x, y, z)) {
                    continue;
                }
                pos.set(x, y, z);
                boolean edge = Math.abs(w) == 2;
                if (bridge || stilt) {
                    b.setWorld(pos, edge ? p.trimPlanks() : p.planks());
                    // Pfosten an den Rändern bis zum Grund
                    if (Math.abs(w) == reach && t % (stilt ? 3 : 4) == 0 && (bridge || stilt && y > ground(b, x, y, z) + 1)) {
                        post(b, p, x, y - 1, z);
                    }
                    if (edge) {
                        b.setWorld(pos.set(x, y + 1, z), p.fence());
                    }
                } else {
                    fillBelow(b, p, x, y - 1, z, 6);
                    b.setWorld(pos.set(x, y, z), stairs != null ? stoneStairs(p, stairs) : surface(b, p, x, y, z, w));
                }
                if (!edge || !railings) {
                    clearAbove(b, x, y, z, 4);
                }
            }
            // Laternen
            if ((flags[t] & (KingdomVillagePiece.LAMP_LEFT | KingdomVillagePiece.LAMP_RIGHT)) != 0) {
                int w = (flags[t] & KingdomVillagePiece.LAMP_RIGHT) != 0 ? 2 : -2;
                lampPost(b, p, cx + side.getStepX() * w, y, cz + side.getStepZ() * w, bridge || stilt);
            }
        }
    }

    /** Oberfläche des Wegs: ein Gemisch, damit er nicht wie gegossen aussieht. */
    private static BlockState surface(VillageBuilder b, VillagePalette p, int x, int y, int z, int w) {
        float n = noise(b, x, z);
        boolean middle = w == 0;
        return switch (p.style) {
            case TIMBER -> (middle ? n < 0.86f : n < 0.62f) ? Blocks.DIRT_PATH.defaultBlockState()
                    : n < 0.84f ? Blocks.GRAVEL.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
            case NORDIC -> n < 0.42f ? Blocks.GRAVEL.defaultBlockState() : n < 0.78f ? Blocks.DIRT_PATH.defaultBlockState()
                    : n < 0.93f ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            case CLAY -> (middle ? n < 0.55f : n < 0.35f) ? Blocks.SMOOTH_SANDSTONE.defaultBlockState()
                    : n < 0.72f ? Blocks.CUT_SANDSTONE.defaultBlockState() : n < 0.9f ? Blocks.SANDSTONE.defaultBlockState()
                    : Blocks.COARSE_DIRT.defaultBlockState();
            case STILT -> p.planks();
        };
    }

    private static float noise(VillageBuilder b, int x, int z) {
        // in Weltkoordinaten (die Wege kennen keine Drehung)
        long h = x * 0x9E3779B97F4A7C15L ^ z * 0xC2B2AE3D27D4EB4FL ^ 0x5DEECE66DL;
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return (h >>> 40) / (float) (1L << 24);
    }

    static BlockState stoneStairs(VillagePalette p, Direction facing) {
        return p.stoneStairs().defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, Half.BOTTOM);
    }

    /** Unter dem Weg bis zum festen Grund auffüllen (Erde, im Lehmdorf Sandstein). */
    static void fillBelow(VillageBuilder b, VillagePalette p, int x, int y, int z, int depth) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState fill = p.style == VillageStyle.CLAY ? Blocks.SANDSTONE.defaultBlockState() : Blocks.DIRT.defaultBlockState();
        for (int k = 0; k < depth; k++) {
            pos.set(x, y - k, z);
            if (!b.inside(pos)) {
                return;
            }
            if (VillageBuilder.isSolidGround(b.level.getBlockState(pos)) && k > 0) {
                return;
            }
            b.setWorld(pos, fill);
        }
    }

    static void clearAbove(VillageBuilder b, int x, int y, int z, int height) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int k = 1; k <= height; k++) {
            pos.set(x, y + k, z);
            if (b.inside(pos) && !b.level.getBlockState(pos).isAir()) {
                b.setWorld(pos, Blocks.AIR.defaultBlockState());
            }
        }
    }

    /** Ein Pfosten von (x, y, z) abwärts bis auf den Grund. */
    static void post(VillageBuilder b, VillagePalette p, int x, int y, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int k = 0; k < 24; k++) {
            pos.set(x, y - k, z);
            if (!b.inside(pos)) {
                return;
            }
            if (VillageBuilder.isSolidGround(b.level.getBlockState(pos))) {
                b.setWorld(pos, p.post(Direction.Axis.Y));
                return;
            }
            b.setWorld(pos, p.post(Direction.Axis.Y));
        }
    }

    private static int ground(VillageBuilder b, int x, int y, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y - 1, z);
        for (int k = 0; k < 24 && b.inside(pos); k++) {
            if (VillageBuilder.isSolidGround(b.level.getBlockState(pos))) {
                return pos.getY();
            }
            pos.move(Direction.DOWN);
        }
        return y - 24;
    }

    private static boolean overWater(VillageBuilder b, int x, int y, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y - 1, z);
        for (int k = 0; k < 3 && b.inside(pos); k++) {
            if (!b.level.getFluidState(pos).isEmpty()) {
                return true;
            }
            pos.move(Direction.DOWN);
        }
        return false;
    }

    /**
     * Laternenpfahl am Wegrand: ein Sockel aus Stein (auf Stegen ein Pfosten), darüber zwei Zaunpfähle und die Laterne.
     */
    static void lampPost(VillageBuilder b, VillagePalette p, int x, int y, int z, boolean onDeck) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        if (!b.chunkBox.isInside(x, y, z)) {
            return;
        }
        if (onDeck) {
            b.setWorld(pos.set(x, y, z), p.trimPlanks());
            post(b, p, x, y - 1, z);
            b.setWorld(pos.set(x, y + 1, z), p.post(Direction.Axis.Y));
        } else {
            fillBelow(b, p, x, y, z, 6);
            b.setWorld(pos.set(x, y + 1, z), p.style == VillageStyle.TIMBER || p.style == VillageStyle.CLAY ? p.stone() : p.post(Direction.Axis.Y));
        }
        b.setWorld(pos.set(x, y + 2, z), p.fence());
        b.setWorld(pos.set(x, y + 3, z), p.fence());
        b.setWorld(pos.set(x, y + 4, z), p.lantern.defaultBlockState().setValue(LanternBlock.HANGING, false));
        clearAbove(b, x, y + 4, z, 1);
    }

    static Block planksOf(VillagePalette p) {
        return p.wood.planks();
    }
}
