package io.github.sirschrolli.kingdomsatwar.economy.town;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jspecify.annotations.Nullable;

/**
 * Bauplan eines Gebäudes der Siedlung: alle Blöcke in Bau-Reihenfolge (zuerst die festen Blöcke von unten nach oben,
 * danach Türen, Betten, Laternen, Pflanzen und anderes Angehängte), dazu die Grundfläche für das Fundament und die
 * besonderen Stellen (Betten, Arbeitstruhe, Arbeitsplatz, Eingang).
 *
 * <p>Lokale Koordinaten: x nach rechts, y nach oben (0 = erste Lage über dem Gelände), z nach hinten; die Vorderseite mit
 * der Tür zeigt nach −z (Norden). In der Welt wird der Plan um seinen Ursprung gedreht ({@link #world}).
 */
public final class Blueprint {
    /** Ein Block des Plans. */
    public record Entry(BlockPos pos, BlockState state) {}

    /** Ein Bauschritt in der Welt: einen Block setzen (Luft = wegräumen) oder eine Fundamentsäule auffüllen. */
    public record Step(BlockPos pos, BlockState state, boolean foundation) {}

    private final List<Entry> blocks;
    private final BoundingBox bounds;
    private final List<BlockPos> footprint;
    private final BlockState foundation;
    private final List<BlockPos> beds;
    private final @Nullable BlockPos chest;
    private final @Nullable BlockPos workSpot;
    private final BlockPos entrance;
    private final List<BlockPos> marks;

    Blueprint(Map<BlockPos, BlockState> plan, List<BlockPos> footprint, BlockState foundation, List<BlockPos> beds, @Nullable BlockPos chest,
            @Nullable BlockPos workSpot, BlockPos entrance, List<BlockPos> marks) {
        List<Entry> solid = new ArrayList<>();
        List<Entry> attached = new ArrayList<>();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (Map.Entry<BlockPos, BlockState> e : plan.entrySet()) {
            BlockPos p = e.getKey();
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
            maxZ = Math.max(maxZ, p.getZ());
            (attached(e.getValue()) ? attached : solid).add(new Entry(p, e.getValue()));
        }
        Comparator<Entry> order = Comparator.<Entry>comparingInt(e -> e.pos().getY()).thenComparingInt(e -> e.pos().getZ())
                .thenComparingInt(e -> e.pos().getX());
        solid.sort(order);
        attached.sort(order);
        List<Entry> all = new ArrayList<>(solid);
        all.addAll(attached);
        this.blocks = Collections.unmodifiableList(all);
        this.bounds = new BoundingBox(minX, Math.min(minY, 0), minZ, maxX, maxY, maxZ);
        this.footprint = List.copyOf(footprint);
        this.foundation = foundation;
        this.beds = List.copyOf(beds);
        this.chest = chest;
        this.workSpot = workSpot;
        this.entrance = entrance;
        this.marks = List.copyOf(marks);
    }

    /** Türen, Betten, Laternen, Fackeln, Leitern, Pflanzen, Teppiche … – alles, was an anderen Blöcken hängt. */
    static boolean attached(BlockState state) {
        Block block = state.getBlock();
        return !state.isAir() && (block instanceof DoorBlock || block instanceof BedBlock
                || block instanceof net.minecraft.world.level.block.TrapDoorBlock || block instanceof net.minecraft.world.level.block.LanternBlock
                || block instanceof net.minecraft.world.level.block.LadderBlock || block instanceof net.minecraft.world.level.block.BaseTorchBlock
                || block instanceof net.minecraft.world.level.block.CropBlock || block instanceof net.minecraft.world.level.block.VegetationBlock
                || block instanceof net.minecraft.world.level.block.CarpetBlock || block instanceof net.minecraft.world.level.block.FlowerPotBlock
                || block instanceof net.minecraft.world.level.block.ChainBlock || block instanceof net.minecraft.world.level.block.BellBlock
                || block instanceof io.github.sirschrolli.kingdomsatwar.block.ChandelierBlock
                || block instanceof net.minecraft.world.level.block.WallBannerBlock || block instanceof net.minecraft.world.level.block.BannerBlock);
    }

    public List<Entry> blocks() {
        return this.blocks;
    }

    /** Ausdehnung des Plans (lokal, mit dem Boden ab y = 0). */
    public BoundingBox bounds() {
        return this.bounds;
    }

    public List<BlockPos> footprint() {
        return this.footprint;
    }

    public List<BlockPos> beds() {
        return this.beds;
    }

    public @Nullable BlockPos chest() {
        return this.chest;
    }

    public @Nullable BlockPos workSpot() {
        return this.workSpot;
    }

    /** Stelle vor der Tür (draußen). */
    public BlockPos entrance() {
        return this.entrance;
    }

    /** Weitere markierte Stellen (z. B. Arbeitsplätze am Markt). */
    public List<BlockPos> marks() {
        return this.marks;
    }

    // =====================================================================================
    // In der Welt
    // =====================================================================================

    public static BlockPos world(BlockPos origin, Rotation rotation, BlockPos local) {
        return origin.offset(local.rotate(rotation));
    }

    public static BlockState world(BlockState state, Rotation rotation) {
        return state.rotate(rotation);
    }

    /** Ausdehnung in der Welt (mit einem Block Rand). */
    public BoundingBox worldBounds(BlockPos origin, Rotation rotation) {
        BlockPos a = world(origin, rotation, new BlockPos(this.bounds.minX(), this.bounds.minY(), this.bounds.minZ()));
        BlockPos b = world(origin, rotation, new BlockPos(this.bounds.maxX(), this.bounds.maxY(), this.bounds.maxZ()));
        return BoundingBox.fromCorners(a, b);
    }

    /**
     * Alle Bauschritte in der Welt: erst wegräumen, was im Weg steht (von oben nach unten), dann das Fundament unter der
     * Grundfläche, dann der Plan selbst.
     */
    public List<Step> steps(BlockPos origin, Rotation rotation) {
        List<Step> steps = new ArrayList<>();
        java.util.Set<BlockPos> planned = new java.util.HashSet<>();
        for (Entry e : this.blocks) {
            planned.add(e.pos());
        }
        for (int y = this.bounds.maxY() + 1; y >= 0; y--) {
            for (int z = this.bounds.minZ(); z <= this.bounds.maxZ(); z++) {
                for (int x = this.bounds.minX(); x <= this.bounds.maxX(); x++) {
                    BlockPos local = new BlockPos(x, y, z);
                    if (!planned.contains(local)) {
                        steps.add(new Step(world(origin, rotation, local), Blocks.AIR.defaultBlockState(), false));
                    }
                }
            }
        }
        for (BlockPos column : this.footprint) {
            steps.add(new Step(world(origin, rotation, column.below()), this.foundation, true));
        }
        for (Entry e : this.blocks) {
            steps.add(new Step(world(origin, rotation, e.pos()), world(e.state(), rotation), false));
        }
        return steps;
    }

    /** Ist an dieser Stelle noch etwas zu tun? (Verbindungen von Zäunen, Scheiben und Treppen zählen nicht.) */
    public static boolean needsWork(ServerLevel level, Step step) {
        if (step.foundation()) {
            BlockPos.MutableBlockPos pos = step.pos().mutable();
            for (int i = 0; i < 12; i++) {
                BlockState old = level.getBlockState(pos);
                if (i > 0 && solidGround(old)) {
                    return false;
                }
                if (old.getBlock() != step.state().getBlock()) {
                    return true;
                }
                pos.move(Direction.DOWN);
            }
            return false;
        }
        BlockState old = level.getBlockState(step.pos());
        if (step.state().isAir()) {
            return !old.isAir();
        }
        return !sameShape(old, step.state());
    }

    /** Gleicher Block in gleicher Ausrichtung (Verbindungen, Wasser, Wachstum und Ähnliches zählen nicht). */
    static boolean sameShape(BlockState a, BlockState b) {
        if (a.getBlock() != b.getBlock()) {
            return false;
        }
        for (var property : b.getProperties()) {
            String name = property.getName();
            if (name.equals("facing") || name.equals("axis") || name.equals("half") || name.equals("type") || name.equals("part")
                    || name.equals("hinge") || name.equals("hanging") || name.equals("attachment")) {
                if (!a.getValue(property).equals(b.getValue(property))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Führt einen Bauschritt aus. Gibt {@code false} zurück, wenn dort schon alles stimmt (nichts zu tun war). Türen,
     * Betten und hohe Pflanzen werden mit ihrer zweiten Hälfte zusammen gesetzt.
     */
    public static boolean apply(ServerLevel level, Step step) {
        if (step.foundation()) {
            boolean changed = false;
            BlockPos.MutableBlockPos pos = step.pos().mutable();
            for (int i = 0; i < 12; i++) {
                BlockState old = level.getBlockState(pos);
                if (i > 0 && solidGround(old)) {
                    break;
                }
                if (!old.equals(step.state())) {
                    level.setBlock(pos, step.state(), Block.UPDATE_ALL);
                    changed = true;
                }
                pos.move(Direction.DOWN);
            }
            return changed;
        }
        if (!needsWork(level, step)) {
            return false;
        }
        BlockState old = level.getBlockState(step.pos());
        if (step.state().isAir() && (old.getBlock() instanceof DoorBlock || old.getBlock() instanceof BedBlock)) {
            // Türen und Betten im Weg: als Ganzes entfernen
            level.removeBlock(step.pos(), false);
            return true;
        }
        BlockState state = step.state();
        BlockPos other = partner(step.pos(), state);
        if (other != null) {
            BlockState otherState = partnerState(state);
            level.setBlock(step.pos(), state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            level.setBlock(other, otherState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            level.updateNeighborsAt(step.pos(), state.getBlock());
            level.updateNeighborsAt(other, otherState.getBlock());
            return true;
        }
        level.setBlock(step.pos(), state, Block.UPDATE_ALL);
        return true;
    }

    /** Baut den ganzen Plan auf einmal (für Tests und den Kreativmodus). */
    public void placeAll(ServerLevel level, BlockPos origin, Rotation rotation) {
        for (Step step : this.steps(origin, rotation)) {
            apply(level, step);
        }
    }

    /** Steht der Plan vollständig? (Pflanzen dürfen inzwischen gewachsen oder geerntet sein.) */
    public boolean complete(ServerLevel level, BlockPos origin, Rotation rotation) {
        for (Entry e : this.blocks) {
            BlockState want = world(e.state(), rotation);
            BlockState have = level.getBlockState(world(origin, rotation, e.pos()));
            if (have.getBlock() != want.getBlock() && !(want.getBlock() instanceof net.minecraft.world.level.block.CropBlock)) {
                return false;
            }
        }
        return true;
    }

    static boolean solidGround(BlockState state) {
        return state.isSolidRender() && !state.is(net.minecraft.tags.BlockTags.LOGS) && !state.is(net.minecraft.tags.BlockTags.LEAVES);
    }

    /** Die zweite Hälfte von Türen, Betten und hohen Pflanzen (oder {@code null}). */
    private static @Nullable BlockPos partner(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof DoorBlock || state.getBlock() instanceof DoublePlantBlock) {
            return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        }
        if (state.getBlock() instanceof BedBlock) {
            Direction facing = state.getValue(BedBlock.FACING);
            return state.getValue(BedBlock.PART) == BedPart.FOOT ? pos.relative(facing) : pos.relative(facing.getOpposite());
        }
        return null;
    }

    private static BlockState partnerState(BlockState state) {
        if (state.getBlock() instanceof BedBlock) {
            return state.setValue(BedBlock.PART, state.getValue(BedBlock.PART) == BedPart.FOOT ? BedPart.HEAD : BedPart.FOOT);
        }
        DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
        return state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, half == DoubleBlockHalf.LOWER ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER);
    }
}
