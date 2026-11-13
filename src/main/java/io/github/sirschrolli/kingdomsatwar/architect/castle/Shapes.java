package io.github.sirschrolli.kingdomsatwar.architect.castle;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.architect.io.Volume;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Passt Mauern, Zäune, Scheiben, Gitter und Treppen in einem entworfenen Volumen an ihre Nachbarn an – genau so, wie das
 * Spiel sie beim Setzen in der Welt verbindet. So sieht die Burg auch als Schematik (etwa in Litematica) richtig aus, und
 * der Bauplan stimmt mit dem überein, was die Baumeister am Ende hinstellen.
 */
final class Shapes {
    /** Dieselbe Reihenfolge wie im Spiel ({@code Block.UPDATE_SHAPE_ORDER}). */
    private static final Direction[] ORDER = {Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP};

    private Shapes() {}

    /** Verbindet alle betroffenen Blöcke des Volumens mit ihren Nachbarn. */
    static void apply(Volume v) {
        Object view = view(v);
        if (view == null) {
            return;
        }
        LevelReader level = (LevelReader) view;
        ScheduledTickAccess ticks = (ScheduledTickAccess) view;
        RandomSource random = RandomSource.create(0L);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos next = new BlockPos.MutableBlockPos();
        boolean warned = false;
        for (int y = 0; y < v.sizeY; y++) {
            for (int z = 0; z < v.sizeZ; z++) {
                for (int x = 0; x < v.sizeX; x++) {
                    BlockState state = v.getBlockState(pos.set(x, y, z));
                    if (!shaped(state.getBlock())) {
                        continue;
                    }
                    try {
                        BlockState out = state;
                        for (Direction d : ORDER) {
                            next.setWithOffset(pos, d);
                            out = out.updateShape(level, ticks, pos, d, next, level.getBlockState(next), random);
                        }
                        if (out != state && !out.isAir()) {
                            v.set(x, y, z, out);
                        }
                    } catch (RuntimeException e) {
                        if (!warned) {
                            KingdomsAtWar.LOGGER.warn("Burg: {} lässt sich nicht verbinden", state, e);
                            warned = true;
                        }
                    }
                }
            }
        }
    }

    private static boolean shaped(Block block) {
        return block instanceof WallBlock || block instanceof CrossCollisionBlock || block instanceof StairBlock || block instanceof FenceGateBlock;
    }

    /**
     * Eine Sicht auf das Volumen, wie sie die Formregeln der Blöcke erwarten (lesen genügt; Strukturleere zählt als Luft).
     * Geplante Ticks (für Wasser in Blöcken) fallen weg.
     */
    private static @Nullable Object view(Volume v) {
        BlockState air = Blocks.AIR.defaultBlockState();
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            int n = args == null ? 0 : args.length;
            switch (name) {
                case "getBlockState" -> {
                    if (n == 1 && args[0] instanceof BlockPos p) {
                        BlockState s = v.getBlockState(p);
                        return s.is(Blocks.STRUCTURE_VOID) ? air : s;
                    }
                }
                case "getFluidState" -> {
                    if (n == 1 && args[0] instanceof BlockPos p) {
                        return v.getBlockState(p).getFluidState();
                    }
                }
                case "getBlockEntity" -> {
                    if (n == 1) {
                        return null;
                    }
                }
                case "getMinY" -> {
                    return 0;
                }
                case "getHeight" -> {
                    if (n == 0) {
                        return v.sizeY;
                    }
                }
                case "isClientSide" -> {
                    return false;
                }
                case "scheduleTick" -> {
                    return null;
                }
                case "hashCode" -> {
                    return System.identityHashCode(proxy);
                }
                case "equals" -> {
                    return proxy == args[0];
                }
                case "toString" -> {
                    return "Burg-Volumen";
                }
                default -> {
                }
            }
            if (method.isDefault()) {
                return InvocationHandler.invokeDefault(proxy, method, args);
            }
            throw new UnsupportedOperationException(name);
        };
        try {
            return Proxy.newProxyInstance(Shapes.class.getClassLoader(), new Class<?>[] {LevelReader.class, ScheduledTickAccess.class}, handler);
        } catch (RuntimeException | LinkageError e) {
            KingdomsAtWar.LOGGER.warn("Burg: Formen lassen sich nicht anpassen", e);
            return null;
        }
    }
}
