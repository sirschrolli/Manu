package io.github.sirschrolli.kingdomsatwar.rules;

import net.minecraft.network.chat.Component;

/**
 * Richtungsangaben statt Koordinaten: „etwa 120 Blöcke Richtung Nordosten“. Koordinaten bekommt kein Spieler zu sehen – wohin
 * er muss, sagt ihm die Himmelsrichtung.
 */
public final class Compass {
    private static final String[] DIRECTIONS = {"south", "southwest", "west", "northwest", "north", "northeast", "east", "southeast"};

    private Compass() {}

    /** Die Himmelsrichtung (acht Striche) eines Versatzes in der Welt (Norden = −z). */
    public static String direction(double dx, double dz) {
        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        int index = Math.floorMod((int) Math.round(angle / 45.0), 8);
        return DIRECTIONS[index];
    }

    /** „Nordosten“ (als übersetzbarer Text). */
    public static Component describe(double dx, double dz) {
        return Component.translatable("direction.kingdomsatwar." + direction(dx, dz));
    }

    /** Gerundete Entfernung in Blöcken (auf zehn Blöcke genau, ab 100 Blöcken auf fünfzig). */
    public static int roughDistance(double dx, double dz) {
        double distance = Math.sqrt(dx * dx + dz * dz);
        int step = distance >= 100.0 ? 50 : 10;
        return Math.max(step, (int) Math.round(distance / step) * step);
    }
}
