package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

/**
 * Haltungen und Abläufe von Ignara, der Flammenpriesterin: je Haltung Zusatzwinkel (Grad) und Verschiebungen (Pixel) der
 * Knochen, je Angriff eine Folge von Haltungen an Ticks (dazwischen wird überblendet).
 *
 * <p>ERZEUGT von tools/ignara_poses.py – nicht von Hand ändern.
 */
final class IgnaraPoses {
    static final int BODY = 0;
    static final int HEAD = 1;
    static final int LEFT_ARM = 2;
    static final int LEFT_FOREARM = 3;
    static final int RIGHT_ARM = 4;
    static final int RIGHT_FOREARM = 5;
    static final int WHIP = 6;
    static final int BONES = 7;

    static final int P_READY = 0;
    static final int P_PRAY = 1;
    static final int P_AWAKEN_LOOK = 2;
    static final int P_AWAKEN_SPREAD = 3;
    static final int P_LASH_WIND = 4;
    static final int P_LASH_STRIKE = 5;
    static final int P_LASH_BACK = 6;
    static final int P_SNARE_SPIN = 7;
    static final int P_SNARE_THROW = 8;
    static final int P_SNARE_PULL = 9;
    static final int P_WAVE_RAISE = 10;
    static final int P_WAVE_SLAM = 11;
    static final int P_PILLARS_CAST = 12;
    static final int P_RAIN_RAISE = 13;
    static final int P_CALL = 14;
    static final int P_PHOENIX_GATHER = 15;
    static final int P_PHOENIX_BURST = 16;
    static final int P_DIVE = 17;
    static final int P_DEATH = 18;

    /** Je Haltung und Knochen: rx, ry, rz (Grad), dx, dy, dz (Pixel). */
    static final float[][] POSES = {
            // ready
            {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -28f, 0f, 4f, 0f, 0f, 0f, -22f, 0f, 0f, 0f, 0f, 0f, -16f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f},
            // pray
            {8f, 0f, 0f, 0f, 0.5f, 0f, 28f, 0f, 0f, 0f, 0f, 0f, -14f, 0f, 22f, 0f, 0f, 0f, -66f, 0f, 0f, 0f, 0f, 0f, -14f, 0f, -22f, 0f, 0f, 0f, -66f, 0f, 0f, 0f, 0f, 0f, 150f, 0f, 0f, 0f, 0f, 0f},
            // awaken_look
            {-4f, 0f, 0f, 0f, 0f, 0f, -22f, 0f, 0f, 0f, 0f, 0f, -24f, 0f, -20f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, -24f, 0f, 20f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 60f, 0f, 0f, 0f, 0f, 0f},
            // awaken_spread
            {-10f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, -78f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 78f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 20f, 0f, 0f, 0f, 0f, 0f},
            // lash_wind
            {-4f, 22f, 0f, 0f, 0f, 0f, -6f, -16f, 0f, 0f, 0f, 0f, -30f, 0f, -24f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, -150f, 0f, 26f, 0f, 0f, 0f, -24f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f},
            // lash_strike
            {10f, -26f, 0f, 0f, 0f, 0f, 4f, 20f, 0f, 0f, 0f, 0f, -20f, 0f, -18f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, -74f, 0f, -22f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 40f, 0f, 0f, 0f, 0f, 0f},
            // lash_back
            {6f, 24f, 0f, 0f, 0f, 0f, 0f, -18f, 0f, 0f, 0f, 0f, -20f, 0f, -18f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, -80f, 0f, 50f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f},
            // snare_spin
            {-6f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, -20f, 0f, 0f, 0f, -24f, 0f, 0f, 0f, 0f, 0f, -172f, 0f, 8f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f},
            // snare_throw
            {12f, 0f, 0f, 0f, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, -26f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, -96f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f},
            // snare_pull
            {-14f, 10f, 0f, 0f, 0f, 0f, -6f, -8f, 0f, 0f, 0f, 0f, -34f, 0f, -18f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, -36f, 0f, 18f, 0f, 0f, 0f, -40f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f},
            // wave_raise
            {-10f, 0f, 0f, 0f, -1f, 0f, -18f, 0f, 0f, 0f, 0f, 0f, -170f, 0f, -14f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, -170f, 0f, 14f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 60f, 0f, 0f, 0f, 0f, 0f},
            // wave_slam
            {28f, 0f, 0f, 0f, 1.5f, 0f, 12f, 0f, 0f, 0f, 0f, 0f, -44f, 0f, 6f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, -44f, 0f, -6f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 70f, 0f, 0f, 0f, 0f, 0f},
            // pillars_cast
            {4f, 14f, 0f, 0f, 0f, 0f, 6f, -10f, 0f, 0f, 0f, 0f, -104f, 0f, 6f, 0f, 0f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, -26f, 0f, 36f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 50f, 0f, 0f, 0f, 0f, 0f},
            // rain_raise
            {-8f, 0f, 0f, 0f, 0f, 0f, -32f, 0f, 0f, 0f, 0f, 0f, -36f, 0f, -66f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, -176f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -40f, 0f, 0f, 0f, 0f, 0f},
            // call
            {-6f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, -44f, 0f, -86f, 0f, 0f, 0f, -12f, 0f, 0f, 0f, 0f, 0f, -44f, 0f, 86f, 0f, 0f, 0f, -12f, 0f, 0f, 0f, 0f, 0f, 40f, 0f, 0f, 0f, 0f, 0f},
            // phoenix_gather
            {14f, 0f, 0f, 0f, 1f, 0f, 24f, 0f, 0f, 0f, 0f, 0f, -62f, 0f, 40f, 0f, 0f, 0f, -80f, 0f, 0f, 0f, 0f, 0f, -62f, 0f, -40f, 0f, 0f, 0f, -80f, 0f, 0f, 0f, 0f, 0f, 80f, 0f, 0f, 0f, 0f, 0f},
            // phoenix_burst
            {-16f, 0f, 0f, 0f, -1f, 0f, -32f, 0f, 0f, 0f, 0f, 0f, -40f, 0f, -112f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, -40f, 0f, 112f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f},
            // dive
            {62f, 0f, 0f, 0f, 2f, 0f, -46f, 0f, 0f, 0f, 0f, 0f, 36f, 0f, -26f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 36f, 0f, 26f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 80f, 0f, 0f, 0f, 0f, 0f},
            // death
            {30f, 0f, 0f, 0f, 3f, 0f, 44f, 0f, 8f, 0f, 0f, 0f, 16f, 0f, 6f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 16f, 0f, -6f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 90f, 0f, 0f, 0f, 0f, 0f},
    };

    /** Abläufe: je Schlüssel {Tick, Haltung, Übergang (0 weich, 1 beschleunigend, 2 abbremsend)}. */
    static final int[][] AWAKEN = {{0, P_PRAY, 0}, {14, P_PRAY, 0}, {26, P_AWAKEN_LOOK, 0}, {38, P_AWAKEN_SPREAD, 2}, {40, P_LASH_WIND, 0}, {44, P_LASH_STRIKE, 1}, {54, P_LASH_STRIKE, 0}, {70, P_READY, 0}};
    static final int[][] LASH = {{0, P_READY, 0}, {9, P_LASH_WIND, 2}, {12, P_LASH_STRIKE, 1}, {18, P_LASH_STRIKE, 0}, {22, P_LASH_BACK, 1}, {26, P_LASH_BACK, 0}, {32, P_READY, 0}};
    static final int[][] SNARE = {{0, P_READY, 0}, {6, P_SNARE_SPIN, 0}, {14, P_SNARE_SPIN, 0}, {16, P_SNARE_THROW, 1}, {20, P_SNARE_PULL, 0}, {31, P_SNARE_PULL, 0}, {34, P_LASH_STRIKE, 1}, {42, P_LASH_STRIKE, 0}, {56, P_READY, 0}};
    static final int[][] WAVE = {{0, P_READY, 0}, {14, P_WAVE_RAISE, 2}, {18, P_WAVE_SLAM, 1}, {24, P_WAVE_RAISE, 2}, {30, P_WAVE_SLAM, 1}, {40, P_WAVE_SLAM, 0}, {50, P_READY, 0}};
    static final int[][] PILLARS = {{0, P_READY, 0}, {8, P_PILLARS_CAST, 2}, {28, P_PILLARS_CAST, 0}, {32, P_WAVE_SLAM, 1}, {40, P_PILLARS_CAST, 0}, {60, P_PILLARS_CAST, 0}, {62, P_READY, 0}};
    static final int[][] OPAL_RAIN = {{0, P_READY, 0}, {10, P_RAIN_RAISE, 2}, {56, P_RAIN_RAISE, 0}, {66, P_READY, 0}};
    static final int[][] CALL = {{0, P_READY, 0}, {8, P_CALL, 2}, {18, P_CALL, 0}, {36, P_READY, 0}};
    static final int[][] PHOENIX = {{0, P_READY, 0}, {20, P_PHOENIX_GATHER, 0}, {26, P_PHOENIX_BURST, 1}, {42, P_PHOENIX_BURST, 0}, {70, P_READY, 0}};
    static final int[][] DIVE = {{0, P_READY, 0}, {10, P_RAIN_RAISE, 2}, {13, P_DIVE, 1}, {26, P_DIVE, 0}, {34, P_READY, 0}, {46, P_READY, 0}};

    private IgnaraPoses() {}
}
