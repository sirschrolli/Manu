package io.github.sirschrolli.kingdomsatwar.worldgen.seal;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

/**
 * Grundriss des Siegelkreises – gemeinsam für den Bau ({@link SealCirclePiece}) und das Spiel ({@code SealCircle}):
 * In der Mitte liegt das Siegel (5×5 Platten), drumherum stehen auf einem Sechseck die sechs Monolithen. Gegenüber
 * stehen jeweils Gegensätze: Feuer und Eis, Blitz und Wasser, Natur und Erde. Von jedem Monolithen führt eine Reihe
 * eingelegter Zeichen im Boden zur Mitte.
 *
 * <p>„Mitte“ ist immer die mittlere Siegelplatte (Bodenhöhe der Plattform).
 */
public final class SealCircleLayout {
    /** Abstand der Monolithen von der Mitte. */
    public static final int MONOLITH_RADIUS = 13;
    /** Rand der Plattform. */
    public static final double PLATFORM_RADIUS = 21.5;
    /** Bis hierhin wird das Gelände an die Plattform angeglichen. */
    public static final double BLEND_RADIUS = 36.0;
    /** So breit ist der ebene Streifen (ein Block unter dem Boden) rund um die Plattform. */
    public static final double APRON = 2.5;
    /** Höhe der Monolithen über dem Boden (Deckstein). */
    public static final int MONOLITH_HEIGHT = 11;
    /** Reihenfolge im Uhrzeigersinn, beginnend im Norden. */
    public static final List<DragonType> RING = List.of(
            DragonType.FIRE, DragonType.LIGHTNING, DragonType.NATURE, DragonType.ICE, DragonType.WATER, DragonType.EARTH);

    private SealCircleLayout() {}

    /** Winkel des Monolithen (Bogenmaß; 0 = Osten, +90° = Süden). */
    public static double angle(DragonType element) {
        return Math.toRadians(-90.0 + 60.0 * RING.indexOf(element));
    }

    /** Mitte des Monolithen auf Bodenhöhe. */
    public static BlockPos monolith(BlockPos center, DragonType element) {
        double a = angle(element);
        return center.offset((int) Math.round(Math.cos(a) * MONOLITH_RADIUS), 0, (int) Math.round(Math.sin(a) * MONOLITH_RADIUS));
    }

    /** Die Seite des Monolithen, die zur Mitte zeigt (dort sitzt der Sockel). */
    public static Direction inward(DragonType element) {
        double a = angle(element);
        return Direction.getApproximateNearest(-Math.cos(a), 0.0, -Math.sin(a));
    }

    /** Lage des Siegelsockels (zweite Lage des Monolithen, auf der Seite zur Mitte). */
    public static BlockPos socket(BlockPos center, DragonType element) {
        Direction in = inward(element);
        return monolith(center, element).offset(in.getStepX(), 2, in.getStepZ());
    }

    /** Umgekehrt: die Mitte des Kreises aus der Lage eines Sockels. */
    public static BlockPos centerFromSocket(BlockPos socket, DragonType element) {
        BlockPos offset = socket(BlockPos.ZERO, element);
        return socket.subtract(offset);
    }

    /** Die eingelegten Zeichen im Boden vom Monolithen zur Mitte (Bodenhöhe). */
    public static List<BlockPos> inlays(BlockPos center, DragonType element) {
        double a = angle(element);
        Set<BlockPos> result = new LinkedHashSet<>();
        for (int r = 5; r <= MONOLITH_RADIUS - 2; r++) {
            result.add(center.offset((int) Math.round(Math.cos(a) * r), 0, (int) Math.round(Math.sin(a) * r)));
        }
        return new ArrayList<>(result);
    }

    /** Die 25 Siegelplatten (Zeile für Zeile von Nordwesten aus). */
    public static List<BlockPos> plates(BlockPos center) {
        List<BlockPos> result = new ArrayList<>(25);
        for (int row = -2; row <= 2; row++) {
            for (int col = -2; col <= 2; col++) {
                result.add(center.offset(col, 0, row));
            }
        }
        return result;
    }

    /** Teilstück des Siegelbildes für eine Platte (0..24) aus dem Versatz zur Mitte. */
    public static int platePart(int dx, int dz) {
        return (dz + 2) * 5 + (dx + 2);
    }

    /** Liegt (dx, dz) auf den Siegelplatten? */
    public static boolean isPlate(int dx, int dz) {
        return Math.abs(dx) <= 2 && Math.abs(dz) <= 2;
    }

    public static double distance(int dx, int dz) {
        return Mth.length(dx, dz);
    }
}
