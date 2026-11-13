package io.github.sirschrolli.kingdomsatwar.siege;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Die Bewegungen der Belagerungswaffen als Funktion der Zeit seit dem Schuss – gemeinsam für das Bild (Modell) und den
 * Server (der Stein verlässt Löffel bzw. Schlinge genau dort, wo das Modell ihn gerade zeigt). Winkel in Bogenmaß,
 * Modellraum wie in der Geometrie: y nach unten, vorn = −z.
 */
public final class SiegePoses {
    private static final float DEG = Mth.DEG_TO_RAD;

    // --------------------------------------------------------------------- Katapult
    /** Wurfarm gespannt (nach hinten gelegt). */
    public static final float CATAPULT_COCKED = -80.0f * DEG;
    /** In diesem Tick nach dem Abzug verlässt der Stein den Löffel (Arm senkrecht). */
    public static final int CATAPULT_RELEASE = 4;
    private static final int CATAPULT_SETTLE = 10;

    // --------------------------------------------------------------------- Tribok
    /** Wurfarm gespannt: das lange Ende unten, die Schlinge liegt am Boden. */
    public static final float TREBUCHET_COCKED = -60.0f * DEG;
    /** Wurfarm nach dem Wurf in Ruhe (Gegengewicht hängt unten). */
    public static final float TREBUCHET_FIRED = 110.0f * DEG;
    private static final float TREBUCHET_PEAK = 135.0f * DEG;
    private static final int TREBUCHET_SWING = 12;
    /** In diesem Tick reißt die Schlinge auf und der Brocken fliegt. */
    public static final int TREBUCHET_RELEASE = 9;
    private static final int TREBUCHET_SETTLE = 40;
    /** Drehpunkt des Wurfarms (Modell) und Länge bis zur Schlinge bzw. zum Stein in der Schlinge. */
    private static final double TREBUCHET_PIVOT_Y = -44.0;
    private static final double TREBUCHET_ARM = 55.0;
    /** Mitte des Brockens unter dem Aufhängepunkt der Schlinge (er liegt tief im Beutel). */
    public static final float TREBUCHET_STONE = 15.4f;
    private static final double TREBUCHET_SLING = TREBUCHET_STONE;

    // --------------------------------------------------------------------- Balliste
    /** So weit schnellen die Bogenarme beim Schuss nach vorn (gespannt: 0). */
    public static final float BALLISTA_RELEASED = 28.0f * DEG;
    public static final int BALLISTA_RELEASE = 1;
    private static final int BALLISTA_SNAP = 2;
    private static final int BALLISTA_RELOAD_START = 12;

    // --------------------------------------------------------------------- Rammbock
    /** Der Widderkopf trifft in diesem Tick. */
    public static final int RAM_STRIKE = 5;

    private SiegePoses() {}

    /** In diesem Tick nach dem Abzug verlässt das Geschoss die Waffe bzw. trifft der Widder. */
    public static int releaseTick(SiegeType siege) {
        return switch (siege) {
            case CATAPULT -> CATAPULT_RELEASE;
            case TREBUCHET -> TREBUCHET_RELEASE;
            case BALLISTA -> BALLISTA_RELEASE;
            case RAM -> RAM_STRIKE;
            case SIEGE_TOWER, WAGON -> 0;
        };
    }

    // =====================================================================================
    // Katapult
    // =====================================================================================

    /** Neigung des Wurfarms (0 = senkrecht, negativ = nach hinten gelegt). */
    public static float catapultArm(float t, int reload) {
        if (t >= reload) {
            return CATAPULT_COCKED;
        }
        if (t < CATAPULT_RELEASE) {
            float x = t / CATAPULT_RELEASE;
            return CATAPULT_COCKED * (1.0f - x * x);
        }
        if (t < CATAPULT_SETTLE) {
            float u = t - CATAPULT_RELEASE;
            // federt am Prellbalken nach
            return 7.0f * DEG * Mth.sin(u / (CATAPULT_SETTLE - CATAPULT_RELEASE) * Mth.PI) * (float) Math.exp(-u / 4.0);
        }
        // die Winde zieht den Arm wieder nach hinten
        return CATAPULT_COCKED * smooth((t - CATAPULT_SETTLE) / Math.max(1.0f, reload - CATAPULT_SETTLE));
    }

    // =====================================================================================
    // Tribok
    // =====================================================================================

    /** Stellung des Wurfarms (negativ: langes Ende gesenkt; über 90°: über die Senkrechte nach vorn geschwungen). */
    public static float trebuchetArm(float t, int reload) {
        if (t >= reload) {
            return TREBUCHET_COCKED;
        }
        if (t < TREBUCHET_SWING) {
            return TREBUCHET_COCKED + (TREBUCHET_PEAK - TREBUCHET_COCKED) * smooth(t / TREBUCHET_SWING);
        }
        if (t < TREBUCHET_SETTLE) {
            float u = t - TREBUCHET_SWING;
            return TREBUCHET_FIRED + (TREBUCHET_PEAK - TREBUCHET_FIRED) * Mth.cos(u / 10.0f * Mth.PI) * (float) Math.exp(-u / 8.0);
        }
        return TREBUCHET_FIRED + (TREBUCHET_COCKED - TREBUCHET_FIRED) * smooth((t - TREBUCHET_SETTLE) / Math.max(1.0f, reload - TREBUCHET_SETTLE));
    }

    /** Lage der Schlinge in der Welt (0 = hängt senkrecht herab); sie schwingt mit und schnellt zum Abwurf hoch. */
    public static float trebuchetSling(float t, int reload) {
        if (t >= reload || t <= 0.0f) {
            return 0.0f;
        }
        if (t <= TREBUCHET_RELEASE) {
            float x = t / TREBUCHET_RELEASE;
            return (trebuchetArm(t, reload) - TREBUCHET_COCKED + 30.0f * DEG) * x * x;
        }
        float atRelease = trebuchetArm(TREBUCHET_RELEASE, reload) - TREBUCHET_COCKED + 30.0f * DEG;
        float u = t - TREBUCHET_RELEASE;
        return atRelease * Mth.cos(u / 14.0f * Mth.PI) * (float) Math.exp(-u / 10.0);
    }

    /** Wo der Brocken in der Schlinge gerade ist: (vorwärts, Höhe) in Blöcken relativ zur Waffe. */
    public static Vec3 trebuchetStone(float t, int reload) {
        double arm = trebuchetArm(t, reload);
        double sling = trebuchetSling(t, reload);
        double y = TREBUCHET_PIVOT_Y - TREBUCHET_ARM * Math.sin(arm) + TREBUCHET_SLING * Math.cos(sling);
        double z = TREBUCHET_ARM * Math.cos(arm) + TREBUCHET_SLING * Math.sin(sling);
        return new Vec3(0.0, (24.0 - y) / 16.0, -z / 16.0);
    }

    // =====================================================================================
    // Balliste
    // =====================================================================================

    /** Wie weit die Bogenarme vorgeschnellt sind (0 = gespannt). */
    public static float ballistaArms(float t, int reload) {
        if (t >= reload) {
            return 0.0f;
        }
        if (t < BALLISTA_SNAP) {
            return BALLISTA_RELEASED * t / BALLISTA_SNAP;
        }
        if (t < BALLISTA_RELOAD_START) {
            float u = t - BALLISTA_SNAP;
            return BALLISTA_RELEASED + 6.0f * DEG * Mth.sin(u / 2.0f * Mth.PI) * (float) Math.exp(-u / 2.5);
        }
        return BALLISTA_RELEASED * (1.0f - smooth(ballistaDraw(t, reload)));
    }

    /** Wie weit die Klaue die Sehne schon zurückgezogen hat (0 = Sehne gerade, 1 = ganz gespannt). */
    public static float ballistaDraw(float t, int reload) {
        if (t >= reload) {
            return 1.0f;
        }
        if (t < BALLISTA_SNAP) {
            return 1.0f - t / BALLISTA_SNAP;
        }
        if (t < BALLISTA_RELOAD_START) {
            return 0.0f;
        }
        return Mth.clamp((t - BALLISTA_RELOAD_START) / Math.max(1.0f, reload - BALLISTA_RELOAD_START), 0.0f, 1.0f);
    }

    // =====================================================================================
    // Rammbock
    // =====================================================================================

    /** Pendel des Widders (negativ: Kopf nach vorn und oben, positiv: zurück). */
    public static float ramLog(float t) {
        if (t < 0.0f) {
            return 0.0f;
        }
        if (t < 3.0f) {
            float x = t / 3.0f;
            return 16.0f * DEG * (1.0f - (1.0f - x) * (1.0f - x));
        }
        if (t < RAM_STRIKE) {
            float x = (t - 3.0f) / (RAM_STRIKE - 3.0f);
            return 16.0f * DEG + (-14.0f - 16.0f) * DEG * x * x;
        }
        float u = t - RAM_STRIKE;
        if (u > 30.0f) {
            return 0.0f;
        }
        return -14.0f * DEG * Mth.cos(u / 6.0f * Mth.PI) * (float) Math.exp(-u / 4.0);
    }

    // =====================================================================================

    /** Weicher Übergang 0 → 1. */
    public static float smooth(float x) {
        x = Mth.clamp(x, 0.0f, 1.0f);
        return x * x * (3.0f - 2.0f * x);
    }
}
