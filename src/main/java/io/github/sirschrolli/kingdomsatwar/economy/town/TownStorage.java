package io.github.sirschrolli.kingdomsatwar.economy.town;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jspecify.annotations.Nullable;

/**
 * Das Lager der Siedlung: alle Truhen und Fässer in den Lagerhäusern und – solange es noch keins gibt, aber auch danach –
 * die Truhen und Fässer, die bis zu {@link #BANNER_RADIUS} Blöcke neben dem Stadtbanner stehen. Hierher bringen die
 * Bürger abends, was sie gesammelt haben, und hier holt der Baumeister seine Baustoffe.
 */
public final class TownStorage {
    /** So weit um das Banner zählen Truhen und Fässer zum Lager. */
    public static final int BANNER_RADIUS = 4;

    private TownStorage() {}

    /** Alle Behälter des Lagers (nur geladene). */
    public static List<BlockPos> containers(ServerLevel level, Town town) {
        return scanAll(level, town);
    }

    private static List<BlockPos> scanAll(ServerLevel level, Town town) {
        List<BlockPos> list = new ArrayList<>();
        if (!level.dimension().equals(town.dimension())) {
            return list;
        }
        for (Town.Building building : town.buildings()) {
            if (building.type() == BuildingType.STOREHOUSE) {
                scan(level, building.bounds(), list);
            }
        }
        if (town.hasBanner()) {
            BlockPos b = town.banner();
            scan(level, new BoundingBox(b.getX() - BANNER_RADIUS, b.getY() - 2, b.getZ() - BANNER_RADIUS,
                    b.getX() + BANNER_RADIUS, b.getY() + 3, b.getZ() + BANNER_RADIUS), list);
        }
        return list;
    }

    private static void scan(ServerLevel level, BoundingBox box, List<BlockPos> into) {
        for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            if (!level.isLoaded(pos)) {
                continue;
            }
            BlockEntity be = level.getBlockEntity(pos);
            if ((be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity) && !into.contains(pos)) {
                into.add(pos.immutable());
            }
        }
    }

    private static @Nullable Container container(ServerLevel level, BlockPos pos) {
        return level.isLoaded(pos) && level.getBlockEntity(pos) instanceof Container c ? c : null;
    }

    /** Gibt es überhaupt ein Lager? */
    public static boolean exists(ServerLevel level, Town town) {
        return !containers(level, town).isEmpty();
    }

    /** Wie viele Einheiten jedes Baustoffs im Lager liegen. */
    public static int[] counts(ServerLevel level, Town town) {
        int[] counts = new int[Material.values().length];
        for (BlockPos pos : containers(level, town)) {
            Container c = container(level, pos);
            if (c == null) {
                continue;
            }
            for (int i = 0; i < c.getContainerSize(); i++) {
                ItemStack stack = c.getItem(i);
                for (Material m : Material.values()) {
                    counts[m.ordinal()] += m.units(stack) * stack.getCount();
                }
            }
        }
        return counts;
    }

    /**
     * Nimmt Gegenstände für (mindestens) {@code units} Einheiten des Baustoffs aus dem Lager – erst Bretter und Bruchstein,
     * dann Stämme. Gibt zurück, wie viele Einheiten es wirklich waren (ein Stamm bringt vier, auch wenn nur eine fehlte).
     */
    public static int take(ServerLevel level, Town town, Material material, int units) {
        int got = 0;
        List<BlockPos> containers = containers(level, town);
        for (int pass = 1; pass <= 4 && got < units; pass++) {
            for (BlockPos pos : containers) {
                Container c = container(level, pos);
                if (c == null) {
                    continue;
                }
                for (int i = 0; i < c.getContainerSize() && got < units; i++) {
                    ItemStack stack = c.getItem(i);
                    int each = material.units(stack);
                    if (each != pass) {
                        continue;
                    }
                    int n = Math.min(stack.getCount(), (units - got + each - 1) / each);
                    stack.shrink(n);
                    got += n * each;
                    c.setChanged();
                }
            }
        }
        return got;
    }

    /** Legt einen Stapel ins Lager (in den Behälter, der ihm am nächsten steht, dann in die anderen); gibt den Rest zurück. */
    public static ItemStack store(ServerLevel level, Town town, ItemStack stack, BlockPos near) {
        ItemStack rest = stack.copy();
        List<BlockPos> containers = containers(level, town);
        containers.sort((a, b) -> Double.compare(a.distSqr(near), b.distSqr(near)));
        for (BlockPos pos : containers) {
            Container c = container(level, pos);
            if (c == null || rest.isEmpty()) {
                continue;
            }
            rest = insert(c, rest);
            c.setChanged();
        }
        return rest;
    }

    /** Legt einen Stapel in einen Behälter (erst zu gleichen Stapeln, dann in freie Fächer); gibt den Rest zurück. */
    public static ItemStack insert(Container container, ItemStack stack) {
        ItemStack rest = stack.copy();
        for (int i = 0; i < container.getContainerSize() && !rest.isEmpty(); i++) {
            ItemStack slot = container.getItem(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, rest) && slot.getCount() < slot.getMaxStackSize()) {
                int move = Math.min(rest.getCount(), slot.getMaxStackSize() - slot.getCount());
                slot.grow(move);
                rest.shrink(move);
            }
        }
        for (int i = 0; i < container.getContainerSize() && !rest.isEmpty(); i++) {
            if (container.getItem(i).isEmpty()) {
                container.setItem(i, rest.copy());
                rest = ItemStack.EMPTY;
            }
        }
        return rest;
    }

    /** Der Behälter des Lagers, der einer Stelle am nächsten steht (oder {@code null}, wenn es kein Lager gibt). */
    public static @Nullable BlockPos nearest(ServerLevel level, Town town, BlockPos from) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : containers(level, town)) {
            double d = pos.distSqr(from);
            if (d < bestDist) {
                bestDist = d;
                best = pos;
            }
        }
        return best;
    }

    /**
     * Für Tests und Vorführungen: stellt bei {@code pos} eine Truhe auf (und, wenn eine nicht reicht, weitere nach Osten) und
     * füllt sie mit Stämmen, Bruchstein und Eisen für die angegebenen Einheiten.
     */
    public static void supply(ServerLevel level, BlockPos pos, int[] units) {
        List<ItemStack> stacks = new ArrayList<>();
        addStacks(stacks, new ItemStack(net.minecraft.world.item.Items.OAK_LOG), (units[Material.WOOD.ordinal()] + 3) / 4);
        addStacks(stacks, new ItemStack(net.minecraft.world.item.Items.COBBLESTONE), units[Material.STONE.ordinal()]);
        addStacks(stacks, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT), units[Material.METAL.ordinal()]);
        BlockPos chest = pos;
        int slot = 27;
        Container container = null;
        for (ItemStack stack : stacks) {
            if (container == null || slot >= container.getContainerSize()) {
                level.setBlock(chest, net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
                container = container(level, chest);
                chest = chest.east(2);
                slot = 0;
                if (container == null) {
                    return;
                }
            }
            container.setItem(slot++, stack);
        }
    }

    private static void addStacks(List<ItemStack> into, ItemStack item, int count) {
        while (count > 0) {
            int n = Math.min(count, item.getMaxStackSize());
            into.add(item.copyWithCount(n));
            count -= n;
        }
    }

    /** Ist im Lager noch Platz für diesen Stapel (ganz oder teilweise)? */
    public static boolean hasRoom(ServerLevel level, Town town, ItemStack stack) {
        for (BlockPos pos : containers(level, town)) {
            Container c = container(level, pos);
            if (c == null) {
                continue;
            }
            for (int i = 0; i < c.getContainerSize(); i++) {
                ItemStack slot = c.getItem(i);
                if (slot.isEmpty() || ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
                    return true;
                }
            }
        }
        return false;
    }
}
