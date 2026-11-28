package io.github.sirschrolli.kingdomsatwar.economy.town;

import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import org.jspecify.annotations.Nullable;

/**
 * Werkzeug zum Zeichnen eines Bauplans: merkt sich Blöcke in lokalen Koordinaten (x rechts, y oben, z hinten; vorn ist
 * −z) und erzeugt daraus einen {@link Blueprint}. Spätere Blöcke ersetzen frühere an derselben Stelle.
 */
final class PlanBuilder {
    private final Map<BlockPos, BlockState> plan = new LinkedHashMap<>();
    private final List<BlockPos> footprint = new ArrayList<>();
    private final List<BlockPos> beds = new ArrayList<>();
    private final List<BlockPos> marks = new ArrayList<>();
    private final Map<String, BlockPos> anchors = new LinkedHashMap<>();
    private final List<RoofPart> roofParts = new ArrayList<>();
    private final Map<Long, Integer> roofTops = new HashMap<>();
    private BlockState foundation = Blocks.COBBLESTONE.defaultBlockState();
    private @Nullable BlockPos chest;
    private @Nullable BlockPos work;
    private BlockPos entrance = new BlockPos(0, 0, -1);

    // ---------------------------------------------------------------- Grundlagen

    PlanBuilder set(int x, int y, int z, BlockState state) {
        this.plan.put(new BlockPos(x, y, z), state);
        return this;
    }

    PlanBuilder set(int x, int y, int z, Block block) {
        return this.set(x, y, z, block.defaultBlockState());
    }

    @Nullable BlockState get(int x, int y, int z) {
        return this.plan.get(new BlockPos(x, y, z));
    }

    PlanBuilder fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
            for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
                for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
                    this.set(x, y, z, state);
                }
            }
        }
        return this;
    }

    PlanBuilder fill(int x0, int y0, int z0, int x1, int y1, int z1, Block block) {
        return this.fill(x0, y0, z0, x1, y1, z1, block.defaultBlockState());
    }

    /** Ausdrücklich Luft (z. B. ein Schacht unter der Erde). */
    PlanBuilder air(int x0, int y0, int z0, int x1, int y1, int z1) {
        return this.fill(x0, y0, z0, x1, y1, z1, Blocks.AIR.defaultBlockState());
    }

    /** Nur der Rand eines Rechtecks (Wände). */
    PlanBuilder ring(int x0, int z0, int x1, int z1, int y0, int y1, BlockState state) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                this.set(x, y, z0, state);
                this.set(x, y, z1, state);
            }
            for (int z = z0; z <= z1; z++) {
                this.set(x0, y, z, state);
                this.set(x1, y, z, state);
            }
        }
        return this;
    }

    // ---------------------------------------------------------------- Gelände und Marken

    /** Grundfläche, unter der ein Fundament bis zum Gelände aufgefüllt wird (aus {@code material}). */
    PlanBuilder foundation(int x0, int z0, int x1, int z1, BlockState material) {
        this.foundation = material;
        for (int z = z0; z <= z1; z++) {
            for (int x = x0; x <= x1; x++) {
                BlockPos p = new BlockPos(x, 0, z);
                if (!this.footprint.contains(p)) {
                    this.footprint.add(p);
                }
            }
        }
        return this;
    }

    PlanBuilder chestAt(int x, int y, int z, Direction facing) {
        this.set(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
        this.chest = new BlockPos(x, y, z);
        return this;
    }

    /** Merkt eine schon gesetzte Truhe als die Truhe des Gebäudes. */
    PlanBuilder chest(int x, int y, int z) {
        this.chest = new BlockPos(x, y, z);
        return this;
    }

        PlanBuilder workAt(int x, int y, int z) {
        this.work = new BlockPos(x, y, z);
        return this;
    }

    PlanBuilder entranceAt(int x, int y, int z) {
        this.entrance = new BlockPos(x, y, z);
        return this;
    }

    PlanBuilder mark(int x, int y, int z) {
        this.marks.add(new BlockPos(x, y, z));
        return this;
    }

    /** Eine benannte Stelle, die ein Beruf braucht (z. B. der Schacht der Mine oder der Amboss der Schmiede). */
    PlanBuilder anchor(String name, int x, int y, int z) {
        this.anchors.put(name, new BlockPos(x, y, z));
        return this;
    }

    // ---------------------------------------------------------------- Bauteile

    static BlockState stairs(Block block, Direction facing, Half half) {
        return block.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
    }

    static BlockState stairs(Block block, Direction facing, Half half, StairsShape shape) {
        return stairs(block, facing, half).setValue(StairBlock.SHAPE, shape);
    }

    static BlockState slab(Block block, SlabType type) {
        return block.defaultBlockState().setValue(SlabBlock.TYPE, type);
    }

    static BlockState log(Block block, Direction.Axis axis) {
        return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    /** Tür (unten bei y, oben bei y + 1); {@code facing}: Blickrichtung beim Hineingehen. */
    PlanBuilder door(int x, int y, int z, Block door, Direction facing) {
        BlockState state = door.defaultBlockState().setValue(DoorBlock.FACING, facing);
        this.set(x, y, z, state.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        this.set(x, y + 1, z, state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        return this;
    }

    /** Bett: Fußende bei (x, y, z), Kopfende einen Block weiter in Richtung {@code facing}. */
    PlanBuilder bed(int x, int y, int z, Block bed, Direction facing) {
        BlockState state = bed.defaultBlockState().setValue(BedBlock.FACING, facing);
        this.set(x, y, z, state.setValue(BedBlock.PART, BedPart.FOOT));
        this.set(x + facing.getStepX(), y, z + facing.getStepZ(), state.setValue(BedBlock.PART, BedPart.HEAD));
        this.beds.add(new BlockPos(x + facing.getStepX(), y, z + facing.getStepZ()));
        return this;
    }

    /** Fensterladen neben einem Fenster: aufgeklappt an der Wand (zeigt nach {@code facing}). */
    PlanBuilder shutter(int x, int y, int z, Block shutter, Direction facing, Half half) {
        return this.set(x, y, z, shutter.defaultBlockState().setValue(TrapDoorBlock.FACING, facing).setValue(TrapDoorBlock.OPEN, true)
                .setValue(TrapDoorBlock.HALF, half));
    }

    PlanBuilder hangingLantern(int x, int y, int z, Block lantern) {
        return this.set(x, y, z, lantern.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

    PlanBuilder standingLantern(int x, int y, int z, Block lantern) {
        return this.set(x, y, z, lantern.defaultBlockState().setValue(LanternBlock.HANGING, false));
    }

    /**
     * Satteldach mit First entlang z (Giebel vorn bei z0 und hinten bei z1), Wände bei x0 und x1, Traufe auf Höhe y.
     * Das Dach steht vorn und hinten einen Block über; die Giebeldreiecke füllt {@code gable}, den First {@code ridge}.
     * Gibt die Höhe des Firsts zurück.
     */
    int gableAlongZ(int x0, int x1, int z0, int z1, int y, Block stairs, BlockState ridge, BlockState gable) {
        for (int i = 0; ; i++) {
            int left = x0 - 1 + i;
            int right = x1 + 1 - i;
            int h = y + i;
            if (left > right) {
                return h - 1;
            }
            if (left == right) {
                for (int z = z0 - 1; z <= z1 + 1; z++) {
                    this.set(left, h, z, ridge);
                }
                return h;
            }
            for (int z = z0 - 1; z <= z1 + 1; z++) {
                this.set(left, h, z, stairs(stairs, Direction.EAST, Half.BOTTOM));
                this.set(right, h, z, stairs(stairs, Direction.WEST, Half.BOTTOM));
            }
            // Giebeldreiecke; in der untersten Reihe auch die Traufseiten (sonst bliebe unter dem Überstand eine Lücke)
            for (int x = left + 1; x < right; x++) {
                this.set(x, h, z0, gable);
                this.set(x, h, z1, gable);
            }
            if (i == 0) {
                for (int z = z0; z <= z1; z++) {
                    this.set(x0, h, z, gable);
                    this.set(x1, h, z, gable);
                }
            }
        }
    }

    /** Satteldach mit First entlang x (Traufen vorn bei z0 und hinten bei z1, Giebel links bei x0 und rechts bei x1). */
    int gableAlongX(int x0, int x1, int z0, int z1, int y, Block stairs, BlockState ridge, BlockState gable) {
        for (int i = 0; ; i++) {
            int front = z0 - 1 + i;
            int back = z1 + 1 - i;
            int h = y + i;
            if (front > back) {
                return h - 1;
            }
            if (front == back) {
                for (int x = x0 - 1; x <= x1 + 1; x++) {
                    this.set(x, h, front, ridge);
                }
                return h;
            }
            for (int x = x0 - 1; x <= x1 + 1; x++) {
                this.set(x, h, front, stairs(stairs, Direction.SOUTH, Half.BOTTOM));
                this.set(x, h, back, stairs(stairs, Direction.NORTH, Half.BOTTOM));
            }
            for (int z = front + 1; z < back; z++) {
                this.set(x0, h, z, gable);
                this.set(x1, h, z, gable);
            }
            if (i == 0) {
                for (int x = x0; x <= x1; x++) {
                    this.set(x, h, z0, gable);
                    this.set(x, h, z1, gable);
                }
            }
        }
    }

    /** Walm- bzw. Zeltdach über dem Rechteck (x0..x1, z0..z1) ab Höhe y; gibt die Höhe der Spitze zurück. */
    int hipRoof(int x0, int z0, int x1, int z1, int y, Block stairs, BlockState top) {
        for (int k = 0; ; k++) {
            int xa = x0 + k;
            int xb = x1 - k;
            int za = z0 + k;
            int zb = z1 - k;
            int h = y + k;
            if (xa > xb || za > zb) {
                return h - 1;
            }
            if (xa == xb || za == zb) {
                this.fill(xa, h, za, xb, h, zb, top);
                return h;
            }
            for (int x = xa + 1; x < xb; x++) {
                this.set(x, h, za, stairs(stairs, Direction.SOUTH, Half.BOTTOM));
                this.set(x, h, zb, stairs(stairs, Direction.NORTH, Half.BOTTOM));
            }
            for (int z = za + 1; z < zb; z++) {
                this.set(xa, h, z, stairs(stairs, Direction.EAST, Half.BOTTOM));
                this.set(xb, h, z, stairs(stairs, Direction.WEST, Half.BOTTOM));
            }
            this.set(xa, h, za, stairs(stairs, Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_LEFT));
            this.set(xb, h, za, stairs(stairs, Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_RIGHT));
            this.set(xa, h, zb, stairs(stairs, Direction.NORTH, Half.BOTTOM, StairsShape.OUTER_RIGHT));
            this.set(xb, h, zb, stairs(stairs, Direction.NORTH, Half.BOTTOM, StairsShape.OUTER_LEFT));
        }
    }

    // ---------------------------------------------------------------- Dächer aus mehreren Dachkörpern

    /** Form eines Dachkörpers. Z: First entlang z (Giebel vorn und hinten), X: First entlang x (Giebel links und rechts). */
    enum RoofShape { GABLE_Z, GABLE_X, HIP, HALF_HIP_Z, HALF_HIP_X }

    /**
     * Ein Dachkörper über den Wänden (x0..x1, z0..z1): Traufe auf Höhe {@code y}, ein Block Überstand ringsum, je Block
     * Abstand zur Traufe {@code pitch} Blöcke höher. {@code gable} füllt Giebeldreiecke und den Kranz über den Wänden
     * ({@code null}: offenes Dach auf Pfosten). Beim Krüppelwalm steigt der Giebel {@code hipAt} Blöcke, dann ist er
     * abgewalmt.
     */
    record RoofPart(RoofShape shape, int x0, int z0, int x1, int z1, int y, int pitch, int hipAt, Block stairs, BlockState ridge,
            @Nullable BlockState gable) {
        boolean covers(int x, int z) {
            return x >= this.x0 - 1 && x <= this.x1 + 1 && z >= this.z0 - 1 && z <= this.z1 + 1;
        }

        boolean onWall(int x, int z) {
            return x >= this.x0 && x <= this.x1 && z >= this.z0 && z <= this.z1
                    && (x == this.x0 || x == this.x1 || z == this.z0 || z == this.z1);
        }

        /** Abstand zur Traufe (in Blöcken) und die Richtung, in die die Dachtreppe dort ansteigt ({@code null}: First). */
        Slope slope(int x, int z) {
            int w = x - (this.x0 - 1);
            int e = this.x1 + 1 - x;
            int n = z - (this.z0 - 1);
            int s = this.z1 + 1 - z;
            int k = this.hipAt;
            int[] d = switch (this.shape) {
                case GABLE_Z -> new int[] {w, e, Integer.MAX_VALUE, Integer.MAX_VALUE};
                case GABLE_X -> new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE, n, s};
                case HIP -> new int[] {w, e, n, s};
                case HALF_HIP_Z -> new int[] {w, e, n + k, s + k};
                case HALF_HIP_X -> new int[] {w + k, e + k, n, s};
            };
            int min = Math.min(Math.min(d[0], d[1]), Math.min(d[2], d[3]));
            boolean west = d[0] == min;
            boolean east = d[1] == min;
            boolean north = d[2] == min;
            boolean south = d[3] == min;
            Direction up;
            if (west && east || north && south) {
                up = null;
            } else if (west) {
                up = Direction.EAST;
            } else if (east) {
                up = Direction.WEST;
            } else if (north) {
                up = Direction.SOUTH;
            } else {
                up = Direction.NORTH;
            }
            return new Slope(min, up);
        }

        int height(int x, int z) {
            return this.y + this.pitch * this.slope(x, z).distance();
        }
    }

    record Slope(int distance, @Nullable Direction up) {}

    /** Ein Dachkörper (Material aus einem Dachziegel-Satz), noch nicht gesetzt – siehe {@link #roofs()}. */
    PlanBuilder roof(RoofShape shape, int x0, int z0, int x1, int z1, int y, BuildingSet set, @Nullable BlockState gable) {
        return this.roof(shape, x0, z0, x1, z1, y, 1, 2, set, gable);
    }

    PlanBuilder roof(RoofShape shape, int x0, int z0, int x1, int z1, int y, int pitch, int hipAt, BuildingSet set, @Nullable BlockState gable) {
        this.roofParts.add(new RoofPart(shape, Math.min(x0, x1), Math.min(z0, z1), Math.max(x0, x1), Math.max(z0, z1), y, pitch, hipAt,
                set.stairs().get(), set.base().get().defaultBlockState(), gable));
        return this;
    }

    /**
     * Setzt alle angemeldeten Dachkörper zusammen: an jeder Stelle gilt das höchste Dach (so entstehen Kehlen, wo zwei
     * Dächer zusammenstoßen, und die Treppen bilden beim Bauen ihre Ecken). Unter dem Dach werden Giebel und der Kranz
     * über den Wänden gefüllt. Gibt die Höhe des höchsten Firsts zurück.
     */
    int roofs() {
        Map<Long, RoofPart> owner = new HashMap<>();
        Map<Long, Integer> top = new HashMap<>();
        for (RoofPart part : this.roofParts) {
            for (int x = part.x0 - 1; x <= part.x1 + 1; x++) {
                for (int z = part.z0 - 1; z <= part.z1 + 1; z++) {
                    long key = BlockPos.asLong(x, 0, z);
                    int h = part.height(x, z);
                    Integer old = top.get(key);
                    if (old == null || h > old) {
                        top.put(key, h);
                        owner.put(key, part);
                    }
                }
            }
        }
        int max = Integer.MIN_VALUE;
        for (Map.Entry<Long, RoofPart> e : owner.entrySet()) {
            long key = e.getKey();
            RoofPart part = e.getValue();
            int x = BlockPos.getX(key);
            int z = BlockPos.getZ(key);
            int h = top.get(key);
            Slope slope = part.slope(x, z);
            int fillTo = h - 1;
            if (slope.up() == null) {
                this.set(x, h, z, part.ridge);
            } else {
                this.set(x, h, z, stairs(part.stairs, slope.up(), Half.BOTTOM));
            }
            if (part.pitch > 1 && slope.distance() > 0) {
                for (int y = h - part.pitch + 1; y < h; y++) {
                    this.set(x, y, z, part.ridge);
                }
                fillTo = h - part.pitch;
            }
            if (part.gable != null && part.onWall(x, z)) {
                for (int y = part.y; y <= fillTo; y++) {
                    this.set(x, y, z, part.gable);
                }
            }
            this.roofTops.put(key, h);
            max = Math.max(max, h);
        }
        this.roofParts.clear();
        return max;
    }

    /** Höhe des Dachs über einer Spalte (nach {@link #roofs()}), sonst {@link Integer#MIN_VALUE}. */
    int roofTop(int x, int z) {
        return this.roofTops.getOrDefault(BlockPos.asLong(x, 0, z), Integer.MIN_VALUE);
    }

    Blueprint build() {
        if (!this.roofParts.isEmpty()) {
            this.roofs();
        }
        return new Blueprint(this.plan, this.footprint, this.foundation, this.beds, this.chest, this.work, this.entrance, this.marks,
                this.anchors);
    }
}
