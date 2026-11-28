package io.github.sirschrolli.kingdomsatwar.client.renderer.arena;

/**
 * Haltungen und Abläufe von Ventor, dem Gladiator der Winde: je Haltung Zusatzwinkel (Grad) und Verschiebungen (Pixel) der
 * Knochen, je Angriff eine Folge von Haltungen an Ticks (dazwischen wird überblendet).
 *
 * <p>ERZEUGT von tools/ventor_poses.py – nicht von Hand ändern.
 */
final class VentorPoses {
    static final int WIND = 0;
    static final int BODY = 1;
    static final int HEAD = 2;
    static final int ARM_R = 3;
    static final int FOREARM_R = 4;
    static final int HAND_R = 5;
    static final int ARM_L = 6;
    static final int FOREARM_L = 7;
    static final int HAND_L = 8;
    static final int TRIDENT = 9;
    static final int NET = 10;
    static final int LOIN_FRONT = 11;
    static final int LOIN_BACK = 12;
    static final int CREST_TAIL = 13;
    static final int BONES = 14;

    static final int P_IDLE = 0;
    static final int P_RISE = 1;
    static final int P_SPREAD = 2;
    static final int P_SALUTE = 3;
    static final int P_GUST_WIND = 4;
    static final int P_GUST = 5;
    static final int P_THRUST_BACK = 6;
    static final int P_THRUST = 7;
    static final int P_NET_WIND = 8;
    static final int P_NET_THROW = 9;
    static final int P_THROW_WIND = 10;
    static final int P_THROW = 11;
    static final int P_POINT = 12;
    static final int P_SUMMON = 13;
    static final int P_DIVE_RISE = 14;
    static final int P_DIVE_HOVER = 15;
    static final int P_DIVE = 16;
    static final int P_DIVE_LAND = 17;
    static final int P_CYCLONE = 18;
    static final int P_DEATH_SINK = 19;
    static final int P_DEATH_FALL = 20;

    /** Je Haltung und Knochen: rx, ry, rz (Grad), dx, dy, dz (Pixel). */
    static final float[][] POSES = {
            // idle
            {0f, 0f, 0f, 0f, 0f, 0f, 4f, -14f, 0f, 0f, 0f, 0f, 4f, 12f, 0f, 0f, 0f, 0f, -24f, 0f, 14f, 0f, 0f, 0f, -58f, 0f, 0f, 0f, 0f, 0f, 36f, 0f, 0f, 0f, 0f, 0f, -16f, 0f, -18f, 0f, 0f, 0f, -34f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f},
            // rise
            {0f, 0f, 0f, 0f, 0f, 0f, 14f, 0f, 0f, 0f, 1f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 6f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 96f, 0f, 0f, 0f, 0f, 0f, -6f, 0f, -6f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // spread
            {0f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, -16f, 0f, 0f, 0f, 0f, 0f, -24f, 0f, 72f, 0f, 0f, 0f, -18f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, -24f, 0f, -72f, 0f, 0f, 0f, -18f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -12f, 0f, 0f, 0f, 0f, 0f, 14f, 0f, 0f, 0f, 0f, 0f, 20f, 0f, 0f, 0f, 0f, 0f},
            // salute
            {0f, 0f, 0f, 0f, 0f, 0f, -6f, 8f, 0f, 0f, 0f, 0f, -14f, -6f, 0f, 0f, 0f, 0f, -172f, 0f, 8f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 86f, 0f, 0f, 0f, 0f, 0f, -42f, 0f, 26f, 0f, 0f, 0f, -92f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // gust_wind
            {0f, 0f, 0f, 0f, 0f, 0f, 6f, -30f, 0f, 0f, 0f, 0f, 2f, 22f, 0f, 0f, 0f, 0f, 18f, 0f, 16f, 0f, 0f, 0f, -40f, 0f, 0f, 0f, 0f, 0f, 22f, 0f, 0f, 0f, 0f, 0f, -78f, 52f, 0f, 0f, 0f, 0f, -34f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // gust
            {0f, 0f, 0f, 0f, 0f, 0f, 10f, 26f, 0f, 0f, 0f, 0f, 6f, -20f, 0f, 0f, 0f, 0f, 24f, 0f, 20f, 0f, 0f, 0f, -36f, 0f, 0f, 0f, 0f, 0f, 12f, 0f, 0f, 0f, 0f, 0f, -86f, -64f, 0f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, 16f, 0f, 0f, 0f, 0f, 0f, 22f, 0f, 0f, 0f, 0f, 0f},
            // thrust_back
            {6f, 0f, 0f, 0f, 0f, 0f, -4f, 28f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 26f, 0f, 16f, 0f, 0f, 0f, -62f, 0f, 0f, 0f, 0f, 0f, 36f, 0f, 0f, 0f, 0f, 0f, -48f, 0f, -22f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // thrust
            {-12f, 0f, 0f, 0f, 0f, 0f, 18f, -24f, 0f, 0f, 0f, -1.5f, -14f, 18f, 0f, 0f, 0f, 0f, -82f, 0f, 6f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 88f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, -26f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -16f, 0f, 0f, 0f, 0f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 26f, 0f, 0f, 0f, 0f, 0f},
            // net_wind
            {0f, 0f, 0f, 0f, 0f, 0f, -8f, 18f, 0f, 0f, 0f, 0f, -10f, -10f, 0f, 0f, 0f, 0f, 14f, 0f, 16f, 0f, 0f, 0f, -44f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, -164f, 0f, -24f, 0f, 0f, 0f, -18f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // net_throw
            {0f, 0f, 0f, 0f, 0f, 0f, 12f, -22f, 0f, 0f, 0f, 0f, -4f, 14f, 0f, 0f, 0f, 0f, 20f, 0f, 16f, 0f, 0f, 0f, -40f, 0f, 0f, 0f, 0f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, -78f, 0f, -8f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -12f, 0f, 0f, 0f, 0f, 0f, 14f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // throw_wind
            {0f, 0f, 0f, 0f, 0f, 0f, -10f, 32f, 0f, 0f, 0f, 0f, 0f, -26f, 0f, 0f, 0f, 0f, -158f, 0f, 18f, 0f, 0f, 0f, 58f, 0f, 0f, 0f, 0f, 0f, 90f, 0f, 0f, 0f, 0f, 0f, -84f, 0f, -14f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // throw
            {-8f, 0f, 0f, 0f, 0f, 0f, 16f, -26f, 0f, 0f, 0f, 0f, -10f, 20f, 0f, 0f, 0f, 0f, -70f, 0f, 10f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 70f, 0f, 0f, 0f, 0f, 0f, 24f, 0f, -22f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -12f, 0f, 0f, 0f, 0f, 0f, 16f, 0f, 0f, 0f, 0f, 0f, 20f, 0f, 0f, 0f, 0f, 0f},
            // point
            {0f, 0f, 0f, 0f, 0f, 0f, 2f, -14f, 0f, 0f, 0f, 0f, 0f, 14f, 0f, 0f, 0f, 0f, -88f, 0f, 6f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 92f, 0f, 0f, 0f, 0f, 0f, -22f, 0f, -24f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // summon
            {0f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, -26f, 0f, 0f, 0f, 0f, 0f, -164f, 0f, 24f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 82f, 0f, 0f, 0f, 0f, 0f, -164f, 0f, -24f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, 16f, 0f, 0f, 0f, 0f, 0f, 28f, 0f, 0f, 0f, 0f, 0f},
            // dive_rise
            {18f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, -26f, 0f, 0f, 0f, 0f, 0f, 48f, 0f, 28f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, 48f, 0f, -28f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, 40f, 0f, 0f, 0f, 0f, 0f},
            // dive_hover
            {0f, 0f, 0f, 0f, 0f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, -160f, 0f, 16f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 80f, 0f, 0f, 0f, 0f, 0f, -28f, 0f, -58f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // dive
            {-20f, 0f, 0f, 0f, 0f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, -46f, 0f, 8f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 146f, 0f, 0f, 0f, 0f, 0f, 24f, 0f, -40f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f},
            // dive_land
            {0f, 0f, 0f, 0f, 0f, 0f, 40f, 0f, 0f, 0f, 3f, 0f, -28f, 0f, 0f, 0f, 0f, 0f, -34f, 0f, 10f, 0f, 0f, 0f, -16f, 0f, 0f, 0f, 0f, 0f, 110f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, -34f, 0f, 0f, 0f, -24f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -34f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // cyclone
            {0f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 88f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, -88f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 36f, 0f, 0f, 0f, 0f, 0f},
            // death_sink
            {0f, 0f, 0f, 0f, 0f, 0f, 24f, 0f, 0f, 0f, 9f, 0f, 26f, 0f, 0f, 0f, 0f, 0f, 8f, 0f, 10f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 8f, 0f, -10f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // death_fall
            {0f, 0f, 0f, 0f, 0f, 0f, 82f, 0f, 0f, 0f, 18f, -6f, -10f, 0f, 0f, 0f, 0f, 0f, -150f, 0f, 20f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 60f, 0f, 0f, 0f, 0f, 0f, -150f, 0f, -20f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
    };

    /** Abläufe: je Schlüssel {Tick, Haltung, Übergang (0 weich, 1 beschleunigend, 2 abbremsend)}. */
    static final int[][] AWAKEN = {{0, P_RISE, 0}, {30, P_RISE, 0}, {42, P_SPREAD, 2}, {52, P_SPREAD, 0}, {60, P_SALUTE, 2}, {72, P_SALUTE, 0}, {80, P_IDLE, 0}};
    static final int[][] RAGE = {{0, P_IDLE, 0}, {8, P_SUMMON, 2}, {34, P_SUMMON, 0}, {40, P_IDLE, 0}};
    static final int[][] GUST = {{0, P_IDLE, 0}, {10, P_GUST_WIND, 2}, {14, P_GUST, 1}, {22, P_GUST, 0}, {30, P_IDLE, 0}};
    static final int[][] THRUST = {{0, P_IDLE, 0}, {9, P_THRUST_BACK, 2}, {12, P_THRUST, 1}, {18, P_THRUST, 0}, {26, P_IDLE, 0}};
    static final int[][] NET_THROW = {{0, P_IDLE, 0}, {12, P_NET_WIND, 2}, {18, P_NET_WIND, 0}, {21, P_NET_THROW, 1}, {28, P_NET_THROW, 0}, {36, P_IDLE, 0}};
    static final int[][] THROW = {{0, P_IDLE, 0}, {14, P_THROW_WIND, 2}, {18, P_THROW, 1}, {26, P_THROW, 0}, {34, P_IDLE, 0}};
    static final int[][] CHARGES = {{0, P_IDLE, 0}, {10, P_POINT, 2}, {32, P_POINT, 0}, {40, P_IDLE, 0}};
    static final int[][] VORTEX = {{0, P_IDLE, 0}, {14, P_SUMMON, 2}, {40, P_SUMMON, 0}, {50, P_IDLE, 0}};
    static final int[][] DIVE = {{0, P_IDLE, 0}, {10, P_DIVE_RISE, 2}, {26, P_DIVE_HOVER, 2}, {42, P_DIVE_HOVER, 0}, {48, P_DIVE, 1}, {52, P_DIVE_LAND, 1}, {64, P_DIVE_LAND, 0}, {72, P_IDLE, 0}};
    static final int[][] CYCLONE = {{0, P_IDLE, 0}, {10, P_CYCLONE, 2}, {62, P_CYCLONE, 0}, {70, P_IDLE, 0}};
    static final int[][] RETREAT = {{0, P_IDLE, 0}, {12, P_RISE, 2}, {40, P_RISE, 0}};
    static final int[][] DEATH = {{0, P_IDLE, 0}, {24, P_DEATH_SINK, 2}, {40, P_DEATH_SINK, 0}, {52, P_DEATH_FALL, 1}};

    private VentorPoses() {}
}
