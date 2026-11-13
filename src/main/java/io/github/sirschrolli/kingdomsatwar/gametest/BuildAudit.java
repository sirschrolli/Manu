package io.github.sirschrolli.kingdomsatwar.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Bauprüfung: sucht in einem fertig gebauten Gebäude nach handwerklichen Fehlern, die man beim Hineingehen sofort sieht –
 * Betten, die halb in der Wand stecken, nach draußen ragen oder unter der Decke klemmen, halbe Betten und Türen,
 * zugestellte Türen, Truhen, die sich nicht öffnen lassen, Öfen, die in die Wand schauen, und alles, was keinen Halt hat
 * (Fackeln, Laternen, Teppiche …).
 */
final class BuildAudit {
    private BuildAudit() {}

    /**
     * Prüft alles in {@code box}; {@code where} beschreibt eine Weltposition für die Meldung (z. B. in Gebäude-Koordinaten).
     */
    static List<String> check(ServerLevel level, BoundingBox box, Function<BlockPos, String> where) {
        List<String> problems = new ArrayList<>();
        BoundingBox outer = box.inflatedBy(1);
        for (BlockPos p : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            BlockPos pos = p.immutable();
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            if (state.getBlock() instanceof AbstractBedBlock) {
                bed(level, pos, state, outer, where, problems);
            } else if (state.getBlock() instanceof DoorBlock) {
                door(level, pos, state, where, problems);
            } else if (state.getBlock() instanceof ChestBlock && ChestBlock.isChestBlockedAt(level, pos)) {
                problems.add("Truhe lässt sich nicht öffnen (Block darüber) bei " + where.apply(pos));
            } else if (state.getBlock() instanceof AbstractFurnaceBlock) {
                BlockPos front = pos.relative(state.getValue(HorizontalDirectionalBlock.FACING));
                if (level.getBlockState(front).isCollisionShapeFullBlock(level, front)) {
                    problems.add(name(state) + " schaut in die Wand bei " + where.apply(pos));
                }
            }
            if (!(state.getBlock() instanceof CropBlock) && !(state.getBlock() instanceof LiquidBlock) && !state.canSurvive(level, pos)) {
                problems.add(name(state) + " ohne Halt bei " + where.apply(pos) + " (darüber " + name(level.getBlockState(pos.above()))
                        + ", darunter " + name(level.getBlockState(pos.below())) + ")");
            }
        }
        return problems;
    }

    private static void bed(ServerLevel level, BlockPos pos, BlockState state, BoundingBox outer, Function<BlockPos, String> where,
            List<String> problems) {
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        BedPart part = state.getValue(AbstractBedBlock.PART);
        BlockPos partner = pos.relative(part == BedPart.FOOT ? facing : facing.getOpposite());
        BlockState other = level.getBlockState(partner);
        if (!other.is(state.getBlock()) || other.getValue(AbstractBedBlock.PART) == part || other.getValue(HorizontalDirectionalBlock.FACING) != facing) {
            problems.add("halbes Bett (" + (part == BedPart.FOOT ? "Kopfende" : "Fußende") + " fehlt) bei " + where.apply(pos));
            return;
        }
        if (level.getBlockState(pos.above()).isSuffocating(level, pos.above())) {
            problems.add("Bett unter der Decke eingeklemmt (" + (part == BedPart.HEAD ? "Kopfende" : "Fußende") + ") bei " + where.apply(pos));
        }
        // nach draußen offen? Von jeder Seite (außer zur anderen Hälfte) geradeaus: trifft man auf nichts, bevor man das
        // Gebäude verlässt, steckt das Bett in der Außenwand (oder steht im Freien)
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos n = pos.relative(side);
            if (n.equals(partner)) {
                continue;
            }
            if (openToOutside(level, n, side, outer)) {
                problems.add("Bett ragt nach draußen (" + (part == BedPart.HEAD ? "Kopfende" : "Fußende") + ", Seite " + side.getSerializedName()
                        + ") bei " + where.apply(pos));
                break;
            }
        }
    }

    /** Geradeaus von {@code start} in Richtung {@code dir}: nur Luft (ohne Kollision) bis aus dem Gebäude heraus? */
    private static boolean openToOutside(ServerLevel level, BlockPos start, Direction dir, BoundingBox outer) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        for (int i = 0; i < 64; i++) {
            if (!outer.isInside(cursor)) {
                return true;
            }
            BlockState s = level.getBlockState(cursor);
            if (!s.getCollisionShape(level, cursor).isEmpty()) {
                return false;
            }
            cursor.move(dir);
        }
        return false;
    }

    private static void door(ServerLevel level, BlockPos pos, BlockState state, Function<BlockPos, String> where, List<String> problems) {
        if (state.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) {
            BlockState below = level.getBlockState(pos.below());
            if (!below.is(state.getBlock()) || below.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) {
                problems.add("halbe Tür (unten fehlt) bei " + where.apply(pos));
            }
            return;
        }
        BlockState above = level.getBlockState(pos.above());
        if (!above.is(state.getBlock()) || above.getValue(DoorBlock.HALF) != DoubleBlockHalf.UPPER) {
            problems.add("halbe Tür (oben fehlt) bei " + where.apply(pos));
            return;
        }
        Direction facing = state.getValue(DoorBlock.FACING);
        for (Direction side : new Direction[] {facing, facing.getOpposite()}) {
            for (int dy = 0; dy <= 1; dy++) {
                BlockPos n = pos.relative(side).above(dy);
                BlockState s = level.getBlockState(n);
                var shape = s.getCollisionShape(level, n);
                // Teppich, Druckplatte, Schneeschicht stören nicht; alles Höhere schon
                boolean blocked = !shape.isEmpty() && (dy == 1 || shape.max(Direction.Axis.Y) > 0.2);
                if (blocked) {
                    problems.add("Tür zugestellt (Seite " + side.getSerializedName() + ": " + name(s) + ") bei " + where.apply(pos));
                    return;
                }
            }
        }
    }

    /** Prüft {@code box} und schreibt jeden Fehler ins Protokoll (Stellen relativ zu {@code origin}); gibt die Fehler zurück. */
    static List<String> log(ServerLevel level, BoundingBox box, BlockPos origin, String name) {
        List<String> problems = check(level, box, pos -> {
            BlockPos l = pos.subtract(origin);
            return "(" + l.getX() + ", " + l.getY() + ", " + l.getZ() + ")";
        });
        for (String p : problems) {
            io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.warn("[Bauprüfung] {}: {}", name, p);
        }
        return problems;
    }

    static String name(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
    }
}
