package io.github.sirschrolli.kingdomsatwar.client.renderer.deep;

/**
 * Haltungen und Abläufe von Sordin, dem Kantor der Stille: je Haltung Zusatzwinkel (Grad) und Verschiebungen (Pixel) der
 * Knochen, je Angriff eine Folge von Haltungen an Ticks (dazwischen wird überblendet).
 *
 * <p>ERZEUGT von tools/sordin_poses.py – nicht von Hand ändern.
 */
final class SordinPoses {
    static final int BODY = 0;
    static final int HEAD = 1;
    static final int LEFT_ARM = 2;
    static final int LEFT_FOREARM = 3;
    static final int LEFT_HAND = 4;
    static final int RIGHT_ARM = 5;
    static final int RIGHT_FOREARM = 6;
    static final int RIGHT_HAND = 7;
    static final int FORK = 8;
    static final int LEFT_LEG = 9;
    static final int LEFT_SHIN = 10;
    static final int RIGHT_LEG = 11;
    static final int RIGHT_SHIN = 12;
    static final int SKIRT = 13;
    static final int SKIRT_LOW = 14;
    static final int PIPES = 15;
    static final int BONES = 16;

    static final int P_PLAY = 0;
    static final int P_LISTEN = 1;
    static final int P_READY = 2;
    static final int P_RAISE = 3;
    static final int P_SWING_WIND = 4;
    static final int P_SWING_STRIKE = 5;
    static final int P_STRIKE = 6;
    static final int P_AIM = 7;
    static final int P_CONDUCT_UP = 8;
    static final int P_CONDUCT_DOWN = 9;
    static final int P_SING = 10;
    static final int P_HUSH = 11;
    static final int P_FORTISSIMO = 12;
    static final int P_STAGGER = 13;
    static final int P_DEATH = 14;

    /** Je Haltung und Knochen: rx, ry, rz (Grad), dx, dy, dz (Pixel). */
    static final float[][] POSES = {
            // play
            {12f, 0f, 0f, 0f, 0f, 0f, -22f, 0f, 0f, 0f, 0f, 0f, -8.9f, 4.1f, -1.7f, 0f, 0f, 0f, -94.5f, 1.6f, 0f, 0f, 0f, 0f, 46.9f, 0f, -1.7f, 0f, 0f, 0f, -8.9f, -4.1f, 1.7f, 0f, 0f, 0f, -94.5f, -1.6f, 0f, 0f, 0f, 0f, 46.9f, 0f, 1.7f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, -3f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, -4f, 0f, 3f, 0f, 0f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // listen
            {4f, 0f, 0f, 0f, 0f, 0f, -6f, 18f, 22f, 0f, 0f, 0f, 3.2f, 6.9f, -18.3f, 0f, 0f, 0f, -150f, 4.6f, 0f, 0f, 0f, 0f, -44.9f, 0f, -38.2f, 0f, 0f, 0f, -20f, 0f, 8f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // ready
            {4f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, -18f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, -28f, 0f, 6f, 0f, 0f, 0f, -58f, 0f, 0f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, -62f, 0f, 0f, 0f, 0f, 0f, -6f, 0f, -3f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 3f, 0f, 0f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // raise
            {-8f, 0f, 0f, 0f, 0f, 0f, -24f, 0f, 0f, 0f, 0f, 0f, -24f, 0f, -40f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -168f, 0f, 4f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 90f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // swing_wind
            {-6f, 24f, 0f, 0f, 0f, 0f, -6f, -16f, 0f, 0f, 0f, 0f, -40f, 0f, -24f, 0f, 0f, 0f, -40f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -160f, 0f, 12f, 0f, 0f, 0f, -40f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 60f, 0f, 0f, 0f, 0f, 0f, -12f, 0f, -3f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 3f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // swing_strike
            {16f, -28f, 0f, 0f, 1f, 0f, 8f, 20f, 0f, 0f, 0f, 0f, -10f, 0f, -30f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -50f, 0f, 6f, 0f, 0f, 0f, -12f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 90f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, -3f, 0f, 0f, 0f, 14f, 0f, 0f, 0f, 0f, 0f, 14f, 0f, 3f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // strike
            {2f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, -79.1f, -1.3f, -25.5f, 0f, 0f, 0f, -104.9f, 3.6f, 0f, 0f, 0f, 0f, 49.6f, 0f, 39f, 0f, 0f, 0f, -30f, 0f, 8f, 0f, 0f, 0f, -70f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // aim
            {6f, -10f, 0f, 0f, 0f, 0f, 2f, 8f, 0f, 0f, 0f, 0f, 1.3f, -1.3f, -3.6f, 0f, 0f, 0f, -138.6f, 30f, 0f, 0f, 0f, 0f, -51.4f, 0f, 0.1f, 0f, 0f, 0f, -88f, 0f, 2f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 90f, 0f, 0f, 0f, 0f, 0f, -14f, 0f, -3f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 12f, 0f, 3f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // conduct_up
            {-6f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, -165f, 0f, 16f, 0f, 0f, 0f, -25f, 0f, 0f, 0f, 0f, 0f, -15f, 0f, 0f, 0f, 0f, 0f, -165f, 0f, -16f, 0f, 0f, 0f, -25f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 90f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // conduct_down
            {12f, 0f, 0f, 0f, 1f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, -45f, 0f, -45f, 0f, 0f, 0f, -15f, 0f, 0f, 0f, 0f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, -45f, 0f, 45f, 0f, 0f, 0f, -15f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 90f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 8f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // sing
            {-12f, 0f, 0f, 0f, 0f, 0f, -36f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, -78f, 0f, 0f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 78f, 0f, 0f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 90f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 8f, 0f, 0f, 0f, 0f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f},
            // hush
            {10f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, -3.5f, 7.4f, -1.5f, 0f, 0f, 0f, -131.2f, 30f, 0f, 0f, 0f, 0f, -55.4f, 0f, 7.5f, 0f, 0f, 0f, -16f, 0f, 10f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -50f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // fortissimo
            {-14f, 0f, 0f, 0f, 0f, 0f, -40f, 0f, 0f, 0f, 0f, 0f, -160f, 0f, 40f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, -160f, 0f, -40f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 90f, 0f, 0f, 0f, 0f, 0f, -4f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 4f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, -12f, 0f, 0f, 0f, 0f, 0f},
            // stagger
            {28f, 6f, 0f, 0f, 2f, 0f, 24f, -10f, 0f, 0f, 0f, 0f, -16.8f, 0.8f, -1.3f, 0f, 0f, 0f, -150f, 0.8f, 0f, 0f, 0f, 0f, -44.9f, 0f, -0.5f, 0f, 0f, 0f, -18.3f, -13.4f, 1.8f, 0f, 0f, 0f, -143f, 5.1f, 0f, 0f, 0f, 0f, -49.1f, 0f, -5.5f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, -18f, 0f, -6f, 0f, 0f, 0f, 22f, 0f, 0f, 0f, 0f, 0f, 8f, 0f, 6f, 0f, 0f, 0f, 12f, 0f, 0f, 0f, 0f, 0f, -16f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // death
            {30f, 0f, 0f, 0f, 11f, 0f, 40f, 0f, 8f, 0f, 0f, 0f, 20f, 0f, -30f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 24f, 0f, 26f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, -84f, 0f, -4f, 0f, 0f, 0f, 86f, 0f, 0f, 0f, 0f, 0f, -80f, 0f, 4f, 0f, 0f, 0f, 84f, 0f, 0f, 0f, 0f, 0f, -40f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
    };

    /** Abläufe: je Schlüssel {Tick, Haltung, Übergang (0 weich, 1 beschleunigend, 2 abbremsend)}. */
    static final int[][] AWAKEN = {{0, P_PLAY, 0}, {14, P_LISTEN, 2}, {34, P_LISTEN, 0}, {50, P_RAISE, 2}, {66, P_RAISE, 0}, {72, P_SWING_STRIKE, 1}, {80, P_READY, 0}};
    static final int[][] SWING = {{0, P_READY, 0}, {9, P_SWING_WIND, 2}, {14, P_SWING_STRIKE, 1}, {20, P_SWING_STRIKE, 0}, {30, P_READY, 0}};
    static final int[][] SONIC = {{0, P_READY, 0}, {8, P_STRIKE, 2}, {12, P_STRIKE, 0}, {22, P_AIM, 2}, {40, P_AIM, 0}, {50, P_READY, 0}};
    static final int[][] CHORD = {{0, P_READY, 0}, {8, P_CONDUCT_UP, 2}, {14, P_CONDUCT_DOWN, 1}, {22, P_CONDUCT_UP, 2}, {28, P_CONDUCT_DOWN, 1}, {36, P_CONDUCT_UP, 2}, {42, P_CONDUCT_DOWN, 1}, {52, P_CONDUCT_DOWN, 0}, {60, P_READY, 0}};
    static final int[][] NOTES = {{0, P_READY, 0}, {12, P_SING, 2}, {50, P_SING, 0}, {60, P_READY, 0}};
    static final int[][] SILENCE = {{0, P_READY, 0}, {8, P_HUSH, 2}, {36, P_HUSH, 0}, {44, P_SWING_WIND, 2}, {54, P_SWING_STRIKE, 1}, {60, P_SWING_STRIKE, 0}, {70, P_READY, 0}};
    static final int[][] FORTISSIMO = {{0, P_READY, 0}, {12, P_FORTISSIMO, 2}, {56, P_FORTISSIMO, 0}, {64, P_CONDUCT_DOWN, 1}, {76, P_READY, 0}};
    static final int[][] STAGGER = {{0, P_STAGGER, 1}, {50, P_STAGGER, 0}, {60, P_READY, 0}};

    private SordinPoses() {}
}
