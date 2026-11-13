package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

/**
 * Haltungen und Abläufe von Phasmor, dem Leerwandlerkönig: je Haltung Zusatzwinkel (Grad) und Verschiebungen (Pixel) der
 * Knochen, je Angriff eine Folge von Haltungen an Ticks (dazwischen wird überblendet).
 *
 * <p>ERZEUGT von tools/phasmor_poses.py – nicht von Hand ändern.
 */
final class PhasmorPoses {
    static final int BODY = 0;
    static final int HEAD = 1;
    static final int LEFT_ARM = 2;
    static final int LEFT_FOREARM = 3;
    static final int RIGHT_ARM = 4;
    static final int RIGHT_FOREARM = 5;
    static final int BLADE = 6;
    static final int LEFT_LEG = 7;
    static final int LEFT_SHIN = 8;
    static final int RIGHT_LEG = 9;
    static final int RIGHT_SHIN = 10;
    static final int BONES = 11;

    static final int P_KNEEL = 0;
    static final int P_READY = 1;
    static final int P_RISE = 2;
    static final int P_RAISE = 3;
    static final int P_SLASH_WIND = 4;
    static final int P_SLASH_STRIKE = 5;
    static final int P_THRUST = 6;
    static final int P_CAST = 7;
    static final int P_LIFT = 8;
    static final int P_THROW = 9;
    static final int P_PLUNGE = 10;
    static final int P_STORM = 11;
    static final int P_ROAR = 12;
    static final int P_STAGGER = 13;
    static final int P_DEATH = 14;

    /** Je Haltung und Knochen: rx, ry, rz (Grad), dx, dy, dz (Pixel). */
    static final float[][] POSES = {
            // kneel
            {6f, 0f, 0f, 0f, 11.5f, 0f, 26f, 0f, 0f, 0f, 0f, 0f, 17.6f, 0f, 9f, 0f, 0f, 0f, -139.7f, 25.9f, 0f, 0f, 0f, 0f, 20.2f, 0f, 6.8f, 0f, 0f, 0f, -128.7f, -30f, 0f, 0f, 0f, 0f, 193.8f, -3.6f, -14f, 0f, 0f, 0f, -84f, 0f, -4f, 0f, 0f, 0f, 86f, 0f, 0f, 0f, 0f, 0f, 4f, 0f, 4f, 0f, 0f, 0f, 86f, 0f, 0f, 0f, 0f, 0f},
            // ready
            {4f, 0f, 0f, 0f, 0f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, 25.3f, 0f, 24.5f, 0f, 0f, 0f, -92f, -39.4f, 0f, 0f, 0f, 0f, 26.8f, 0f, 19f, 0f, 0f, 0f, -107.3f, -24.3f, 0f, 0f, 0f, 0f, 24.7f, 6.8f, 9.5f, 0f, 0f, 0f, -6f, 0f, -3f, 0f, 0f, 0f, 8f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 3f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f},
            // rise
            {14f, 0f, 0f, 0f, 5f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 19.4f, 0f, -3.7f, 0f, 0f, 0f, -132.4f, 26.3f, 0f, 0f, 0f, 0f, 20.5f, 0f, -55.3f, 0f, 0f, 0f, -122.2f, 24.4f, 0f, 0f, 0f, 0f, 134.7f, -4.2f, 26.8f, 0f, 0f, 0f, -50f, 0f, -4f, 0f, 0f, 0f, 50f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 4f, 0f, 0f, 0f, 40f, 0f, 0f, 0f, 0f, 0f},
            // raise
            {-6f, 0f, 0f, 0f, 0f, 0f, -26f, 0f, 0f, 0f, 0f, 0f, 28.7f, 0f, 22.8f, 0f, 0f, 0f, -138.9f, -60f, 0f, 0f, 0f, 0f, -102.9f, 0f, 5.2f, 0f, 0f, 0f, -106.5f, -0.4f, 0f, 0f, 0f, 0f, 133.6f, -1f, 6.5f, 0f, 0f, 0f, -4f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 4f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // slash_wind
            {-4f, 26f, 0f, 0f, 0f, 0f, -4f, -18f, 0f, 0f, 0f, 0f, 22.4f, 0f, 32.4f, 0f, 0f, 0f, -111.1f, -52f, 0f, 0f, 0f, 0f, -92.4f, 0f, -16.8f, 0f, 0f, 0f, -150f, 4.1f, 0f, 0f, 0f, 0f, 88.9f, 0f, 0.5f, 0f, 0f, 0f, -14f, 0f, -4f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // slash_strike
            {12f, -30f, 0f, 0f, 0f, 0f, 4f, 22f, 0f, 0f, 0f, 0f, 24.7f, 0f, 29.7f, 0f, 0f, 0f, -97.2f, -60f, 0f, 0f, 0f, 0f, 9.8f, 0f, -13.5f, 0f, 0f, 0f, -81.6f, -6f, 0f, 0f, 0f, 0f, 90.6f, 0f, -5.4f, 0f, 0f, 0f, -20f, 0f, -4f, 0f, 0f, 0f, 16f, 0f, 0f, 0f, 0f, 0f, 14f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // thrust
            {16f, -8f, 0f, 0f, 2f, 0f, -10f, 6f, 0f, 0f, 0f, 0f, 45f, 0f, 34.1f, 0f, 0f, 0f, -107.5f, -60f, 0f, 0f, 0f, 0f, -12.1f, 0f, -10.4f, 0f, 0f, 0f, -113f, 0.3f, 0f, 0f, 0f, 0f, 110.5f, -0.4f, 5.4f, 0f, 0f, 0f, 24f, 0f, -4f, 0f, 0f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, -34f, 0f, 4f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f},
            // cast
            {2f, -10f, 0f, 0f, 0f, 0f, -8f, -8f, 0f, 0f, 0f, 0f, -61.6f, 0f, -9.5f, 0f, 0f, 0f, -129.5f, 4f, 0f, 0f, 0f, 0f, 27.7f, 0f, -2f, 0f, 0f, 0f, -62.5f, 15.3f, 0f, 0f, 0f, 0f, 79f, 0.3f, 5.2f, 0f, 0f, 0f, -6f, 0f, -3f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 3f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // lift
            {-8f, 0f, 0f, 0f, -1f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, -92.9f, 0f, 8.7f, 0f, 0f, 0f, -125.2f, -2.1f, 0f, 0f, 0f, 0f, -92.9f, 0f, -8.7f, 0f, 0f, 0f, -125.2f, 2.1f, 0f, 0f, 0f, 0f, 135.7f, -0.1f, 0.8f, 0f, 0f, 0f, -4f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 4f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // throw
            {14f, -18f, 0f, 0f, 0f, 0f, 0f, 12f, 0f, 0f, 0f, 0f, -1.3f, 0f, 32.1f, 0f, 0f, 0f, -108.7f, 13.4f, 0f, 0f, 0f, 0f, -156.6f, 0f, -23.6f, 0f, 0f, 0f, -132.1f, -3.9f, 0f, 0f, 0f, 0f, 171f, 6.9f, -25f, 0f, 0f, 0f, -22f, 0f, -4f, 0f, 0f, 0f, 16f, 0f, 0f, 0f, 0f, 0f, 14f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // plunge
            {30f, 0f, 0f, 0f, 3f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 13.8f, 0f, 18.7f, 0f, 0f, 0f, -113.7f, 6.7f, 0f, 0f, 0f, 0f, 7.5f, 0f, -32f, 0f, 0f, 0f, -98.6f, 8.7f, 0f, 0f, 0f, 0f, 137f, -4.2f, 25.8f, 0f, 0f, 0f, -26f, 0f, -5f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 5f, 0f, 0f, 0f, 24f, 0f, 0f, 0f, 0f, 0f},
            // storm
            {24f, 10f, 0f, 0f, 3f, 0f, -18f, -8f, 0f, 0f, 0f, 0f, 14.6f, 0f, -6.5f, 0f, 0f, 0f, -130.3f, -3.3f, 0f, 0f, 0f, 0f, 45f, 0f, -25.3f, 0f, 0f, 0f, -97.1f, 46.4f, 0f, 0f, 0f, 0f, 139.4f, -9.6f, 55.6f, 0f, 0f, 0f, -34f, 0f, -6f, 0f, 0f, 0f, 40f, 0f, 0f, 0f, 0f, 0f, 16f, 0f, 6f, 0f, 0f, 0f, 36f, 0f, 0f, 0f, 0f, 0f},
            // roar
            {-10f, 0f, 0f, 0f, 0f, 0f, -36f, 0f, 0f, 0f, 0f, 0f, 28.3f, 0f, 14.8f, 0f, 0f, 0f, -111.1f, -60f, 0f, 0f, 0f, 0f, 28.3f, 0f, 84.5f, 0f, 0f, 0f, -111f, -60f, 0f, 0f, 0f, 0f, 139.6f, 1.7f, -9.9f, 0f, 0f, 0f, -4f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 4f, 0f, 8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // stagger
            {26f, 6f, 0f, 0f, 2f, 0f, 30f, -10f, 0f, 0f, 0f, 0f, -78.9f, 0f, -20.4f, 0f, 0f, 0f, -150f, 5.6f, 0f, 0f, 0f, 0f, 5.2f, 0f, 3.2f, 0f, 0f, 0f, -63.8f, -1.6f, 0f, 0f, 0f, 0f, 69.3f, -0.4f, -3.9f, 0f, 0f, 0f, -16f, 0f, -6f, 0f, 0f, 0f, 22f, 0f, 0f, 0f, 0f, 0f, 8f, 0f, 6f, 0f, 0f, 0f, 12f, 0f, 0f, 0f, 0f, 0f},
            // death
            {28f, 0f, 0f, 0f, 11.5f, 0f, 44f, 0f, 8f, 0f, 0f, 0f, 22.5f, 0f, 42f, 0f, 0f, 0f, -117f, -43.9f, 0f, 0f, 0f, 0f, 25.8f, 0f, 34.2f, 0f, 0f, 0f, -121.2f, -29.1f, 0f, 0f, 0f, 0f, 92.1f, -0.2f, 19.8f, 0f, 0f, 0f, -84f, 0f, -4f, 0f, 0f, 0f, 86f, 0f, 0f, 0f, 0f, 0f, 4f, 0f, 4f, 0f, 0f, 0f, 86f, 0f, 0f, 0f, 0f, 0f},
    };

    /** Abläufe: je Schlüssel {Tick, Haltung, Übergang (0 weich, 1 beschleunigend, 2 abbremsend)}. */
    static final int[][] AWAKEN = {{0, P_KNEEL, 0}, {16, P_KNEEL, 0}, {34, P_RISE, 2}, {46, P_RAISE, 2}, {60, P_RAISE, 0}, {68, P_SLASH_STRIKE, 1}, {80, P_READY, 0}};
    static final int[][] SLASH = {{0, P_READY, 0}, {10, P_SLASH_WIND, 2}, {15, P_SLASH_STRIKE, 1}, {22, P_SLASH_STRIKE, 0}, {32, P_READY, 0}};
    static final int[][] BLINK = {{0, P_READY, 0}, {7, P_STORM, 2}, {14, P_SLASH_WIND, 0}, {19, P_SLASH_STRIKE, 1}, {28, P_SLASH_STRIKE, 0}, {40, P_READY, 0}};
    static final int[][] ILLUSIONS = {{0, P_READY, 0}, {10, P_CAST, 2}, {30, P_CAST, 0}, {44, P_READY, 0}};
    static final int[][] HURL = {{0, P_READY, 0}, {10, P_LIFT, 2}, {22, P_LIFT, 0}, {26, P_THROW, 1}, {31, P_LIFT, 2}, {36, P_THROW, 1}, {41, P_LIFT, 2}, {46, P_THROW, 1}, {54, P_THROW, 0}, {64, P_READY, 0}};
    static final int[][] RIFTS = {{0, P_READY, 0}, {8, P_RAISE, 2}, {14, P_PLUNGE, 1}, {40, P_PLUNGE, 0}, {46, P_RISE, 0}, {56, P_READY, 0}};
    static final int[][] STORM = {{0, P_READY, 0}, {4, P_STORM, 2}, {10, P_SLASH_STRIKE, 1}, {18, P_STORM, 2}, {24, P_SLASH_STRIKE, 1}, {32, P_STORM, 2}, {38, P_SLASH_STRIKE, 1}, {46, P_STORM, 2}, {52, P_SLASH_STRIKE, 1}, {60, P_STORM, 2}, {66, P_SLASH_STRIKE, 1}, {74, P_SLASH_STRIKE, 0}, {84, P_READY, 0}};
    static final int[][] CROWN = {{0, P_READY, 0}, {10, P_ROAR, 2}, {50, P_ROAR, 0}, {60, P_RAISE, 2}, {72, P_READY, 0}};
    static final int[][] STAGGER = {{0, P_STAGGER, 1}, {40, P_STAGGER, 0}, {50, P_READY, 0}};

    private PhasmorPoses() {}
}
