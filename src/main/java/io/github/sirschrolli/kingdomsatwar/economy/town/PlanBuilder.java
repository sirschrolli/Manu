package io.github.sirschrolli.kingdomsatwar.economy.town;

import java.util.ArrayList;
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

    Blueprint build() {
        return new Blueprint(this.plan, this.footprint, this.foundation, this.beds, this.chest, this.work, this.entrance, this.marks);
    }
}
