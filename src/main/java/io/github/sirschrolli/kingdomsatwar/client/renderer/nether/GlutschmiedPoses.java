package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

/**
 * Haltungen und Abläufe von Malleus, dem Glutschmied: je Haltung Zusatzwinkel (Grad) und Verschiebungen (Pixel) der
 * Knochen, je Angriff eine Folge von Haltungen an Ticks (dazwischen wird überblendet).
 *
 * <p>ERZEUGT von tools/glutschmied_poses.py – nicht von Hand ändern.
 */
final class GlutschmiedPoses {
    static final int BODY = 0;
    static final int HEAD = 1;
    static final int JAW = 2;
    static final int LEFT_ARM = 3;
    static final int LEFT_FOREARM = 4;
    static final int HAMMER = 5;
    static final int RIGHT_ARM = 6;
    static final int RIGHT_FOREARM = 7;
    static final int SPEAR = 8;
    static final int LEFT_LEG = 9;
    static final int RIGHT_LEG = 10;
    static final int BONES = 11;

    static final int P_READY = 0;
    static final int P_FORGE_IMPACT = 1;
    static final int P_FORGE_LIFT = 2;
    static final int P_FORGE_RAISED = 3;
    static final int P_AWAKEN_LOOK = 4;
    static final int P_AWAKEN_RISE = 5;
    static final int P_AWAKEN_REACH = 6;
    static final int P_AWAKEN_RAISE = 7;
    static final int P_AWAKEN_ROAR = 8;
    static final int P_THRUST_DRAW = 9;
    static final int P_THRUST = 10;
    static final int P_SLAM_RAISE = 11;
    static final int P_SLAM_HIT = 12;
    static final int P_SPARKS_UP = 13;
    static final int P_SPARKS_HIT = 14;
    static final int P_GLOBS_BACK = 15;
    static final int P_GLOBS_FLING = 16;
    static final int P_CALL_RAISE = 17;
    static final int P_CALL_HIGH = 18;
    static final int P_CALL_STAMP = 19;
    static final int P_STOKE_RAISE = 20;
    static final int P_STOKE_PLANT = 21;
    static final int P_DEATH = 22;

    /** Je Haltung und Knochen: rx, ry, rz (Grad), dx, dy, dz (Pixel). */
    static final float[][] POSES = {
            // ready
            {8f, 0f, 0f, 0f, 1.5f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -22f, 0f, 4f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, -28f, 0f, -6f, 0f, 0f, 0f, -34f, 0f, 0f, 0f, 0f, 0f, 50f, 0f, 0f, 0f, 0f, 0f, -14f, 0f, -5f, 0f, 1.5f, 0f, 14f, 0f, 5f, 0f, 1.5f, 0f},
            // forge_impact
            {14f, 0f, 0f, 0f, 5.5f, 0f, 16f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 15f, 0f, 0f, 0f, -70f, 0f, 0f, 0f, 0f, 0f, -28f, 0f, 0f, 0f, 0f, 0f, -45f, 0f, -26f, 0f, 0f, 0f, -23f, 0f, 0f, 0f, 0f, 0f, 150f, 0f, 0f, 0f, 0f, 0f, 55f, 0f, 0f, 0f, 5.5f, 0f, -40f, 0f, 0f, 0f, 5.5f, 0f},
            // forge_lift
            {13f, 0f, 0f, 0f, 5.5f, 0f, 14f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -11f, 0f, 14f, 0f, 0f, 0f, -76f, 0f, 0f, 0f, 0f, 0f, -38f, 0f, 0f, 0f, 0f, 0f, -44f, 0f, -26f, 0f, 0f, 0f, -23f, 0f, 0f, 0f, 0f, 0f, 150f, 0f, 0f, 0f, 0f, 0f, 55f, 0f, 0f, 0f, 5.5f, 0f, -40f, 0f, 0f, 0f, 5.5f, 0f},
            // forge_raised
            {10f, 0f, 0f, 0f, 5.5f, 0f, 8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -190f, 0f, 10f, 0f, 0f, 0f, -28f, 0f, 0f, 0f, 0f, 0f, -40f, 0f, 0f, 0f, 0f, 0f, -41f, 0f, -26f, 0f, 0f, 0f, -23f, 0f, 0f, 0f, 0f, 0f, 150f, 0f, 0f, 0f, 0f, 0f, 55f, 0f, 0f, 0f, 5.5f, 0f, -40f, 0f, 0f, 0f, 5.5f, 0f},
            // awaken_look
            {14f, 0f, 0f, 0f, 5.5f, 0f, -22f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 15f, 0f, 0f, 0f, -70f, 0f, 0f, 0f, 0f, 0f, -28f, 0f, 0f, 0f, 0f, 0f, -45f, 0f, -26f, 0f, 0f, 0f, -23f, 0f, 0f, 0f, 0f, 0f, 150f, 0f, 0f, 0f, 0f, 0f, 55f, 0f, 0f, 0f, 5.5f, 0f, -40f, 0f, 0f, 0f, 5.5f, 0f},
            // awaken_rise
            {4f, 0f, 0f, 0f, 1f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -14f, 0f, 4f, 0f, 0f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, 14f, 0f, 0f, 0f, 0f, 0f, -18f, 0f, -6f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 114f, 0f, 0f, 0f, 0f, 0f, -12f, 0f, -4f, 0f, 1f, 0f, 12f, 0f, 4f, 0f, 1f, 0f},
            // awaken_reach
            {30f, 0f, 0f, 0f, 3f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -46f, 0f, 4f, 0f, 0f, 0f, -18f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, -88f, 0f, -24f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 74f, 70f, 0f, 0f, 0f, 0f, -24f, 0f, -4f, 0f, 3f, 0f, 26f, 0f, 4f, 0f, 3f, 0f},
            // awaken_raise
            {-8f, 0f, 0f, 0f, 0f, 0f, -18f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -148f, 0f, 2f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, -33f, 0f, 0f, 0f, 0f, 0f, -148f, 0f, 8f, 0f, 0f, 0f, 44f, 0f, 0f, 0f, 0f, 0f, 28f, 0f, 0f, 0f, 0f, 0f, -12f, 0f, -6f, 0f, 0f, 0f, 12f, 0f, 6f, 0f, 0f, 0f},
            // awaken_roar
            {-14f, 0f, 0f, 0f, 0f, 0f, -32f, 0f, 0f, 0f, 0f, 0f, 36f, 0f, 0f, 0f, 0f, 0f, -122f, 0f, -26f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, -40f, 0f, 0f, 0f, 0f, 0f, -122f, 0f, 26f, 0f, 0f, 0f, 42f, 0f, 0f, 0f, 0f, 0f, 18f, 0f, 0f, 0f, 0f, 0f, -14f, 0f, -8f, 0f, 0f, 0f, 14f, 0f, 8f, 0f, 0f, 0f},
            // thrust_draw
            {2f, -18f, 0f, 0f, 1.5f, 0f, -6f, 14f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -18f, 0f, 8f, 0f, 0f, 0f, -18f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 32f, 0f, -12f, 0f, 0f, 0f, -68f, 0f, 0f, 0f, 0f, 0f, 38f, 0f, 0f, 0f, 0f, 0f, -18f, 0f, -5f, 0f, 1.5f, 0f, 18f, 0f, 5f, 0f, 1.5f, 0f},
            // thrust
            {18f, 22f, 0f, 0f, 2.5f, 0f, -14f, -18f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 14f, 0f, 0f, 0f, -28f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -86f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 86f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, -5f, 0f, 2.5f, 0f, 24f, 0f, 5f, 0f, 2.5f, 0f},
            // slam_raise
            {-12f, 0f, 0f, 0f, 0f, 0f, -16f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -190f, 0f, 6f, 0f, 0f, 0f, -22f, 0f, 0f, 0f, 0f, 0f, -40f, 0f, 0f, 0f, 0f, 0f, -14f, 0f, -24f, 0f, 0f, 0f, -18f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, -5f, 0f, 0f, 0f, 10f, 0f, 5f, 0f, 0f, 0f},
            // slam_hit
            {34f, 0f, 0f, 0f, 4f, 0f, 8f, 0f, 0f, 0f, 0f, 0f, 14f, 0f, 0f, 0f, 0f, 0f, -64f, 0f, 10f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, -18f, 0f, 0f, 0f, 0f, 0f, -16f, 0f, -40f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -32f, 0f, 0f, 0f, 0f, 0f, -36f, 0f, -6f, 0f, 4f, 0f, 40f, 0f, 6f, 0f, 4f, 0f},
            // sparks_up
            {6f, 0f, 0f, 0f, 1.5f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -132f, 0f, 18f, 0f, 0f, 0f, -33f, 0f, 0f, 0f, 0f, 0f, -40f, 0f, 0f, 0f, 0f, 0f, -52f, 0f, -14f, 0f, 0f, 0f, -26f, 0f, 0f, 0f, 0f, 0f, 72f, -28f, 0f, 0f, 0f, 0f, -14f, 0f, -5f, 0f, 1.5f, 0f, 14f, 0f, 5f, 0f, 1.5f, 0f},
            // sparks_hit
            {14f, 0f, 0f, 0f, 2f, 0f, 12f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -78f, 0f, 26f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, -60f, 0f, -14f, 0f, 0f, 0f, -26f, 0f, 0f, 0f, 0f, 0f, 72f, -28f, 0f, 0f, 0f, 0f, -14f, 0f, -5f, 0f, 2f, 0f, 14f, 0f, 5f, 0f, 2f, 0f},
            // globs_back
            {4f, 14f, 0f, 0f, 2f, 0f, -6f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 35f, 0f, 2f, 0f, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, -8f, 0f, 0f, 0f, -18f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, -5f, 0f, 2f, 0f, 16f, 0f, 5f, 0f, 2f, 0f},
            // globs_fling
            {10f, -16f, 0f, 0f, 1f, 0f, -10f, 10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -136f, 0f, 10f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, -25f, 0f, 0f, 0f, 0f, 0f, -6f, 0f, -8f, 0f, 0f, 0f, -18f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, -5f, 0f, 1f, 0f, -18f, 0f, 5f, 0f, 1f, 0f},
            // call_raise
            {-10f, 0f, 0f, 0f, 0f, 0f, -24f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, -26f, 0f, -20f, 0f, 0f, 0f, -18f, 0f, 0f, 0f, 0f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, -116f, 0f, 10f, 0f, 0f, 0f, 32f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, -6f, 0f, 0f, 0f, 10f, 0f, 6f, 0f, 0f, 0f},
            // call_high
            {-12f, 0f, 0f, 0f, 0f, 0f, -28f, 0f, 0f, 0f, 0f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, -24f, 0f, -22f, 0f, 0f, 0f, -18f, 0f, 0f, 0f, 0f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, -134f, 0f, 10f, 0f, 0f, 0f, 32f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, -6f, 0f, 0f, 0f, 10f, 0f, 6f, 0f, 0f, 0f},
            // call_stamp
            {10f, 0f, 0f, 0f, 2.5f, 0f, 2f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, -36f, 0f, -10f, 0f, 0f, 0f, -18f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, -76f, 0f, -4f, 0f, 0f, 0f, 32f, 0f, 0f, 0f, 0f, 0f, -50f, 0f, 0f, 0f, 0f, 0f, -16f, 0f, -7f, 0f, 2.5f, 0f, 16f, 0f, 7f, 0f, 2.5f, 0f},
            // stoke_raise
            {-8f, 0f, 0f, 0f, 0f, 0f, -18f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -38f, 0f, -8f, 0f, 0f, 0f, -18f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, -158f, 0f, -6f, 0f, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 0f, 257f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, -6f, 0f, 0f, 0f, 10f, 0f, 6f, 0f, 0f, 0f},
            // stoke_plant
            {30f, 0f, 0f, 0f, 4.5f, 0f, -26f, 0f, 0f, 0f, 0f, 0f, 32f, 0f, 0f, 0f, 0f, 0f, -186f, 0f, -20f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, -106f, 0f, -4f, 0f, 0f, 0f, 42f, 0f, 0f, 0f, 0f, 0f, 108f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, -7f, 0f, 4.5f, 0f, 34f, 0f, 7f, 0f, 4.5f, 0f},
            // death
            {40f, 0f, 0f, 0f, 6.5f, 0f, 30f, 0f, 8f, 0f, 0f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, -36f, 0f, 4f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, -40f, 0f, -4f, 0f, 0f, 0f, -2f, 0f, 0f, 0f, 0f, 0f, 58f, 0f, 0f, 0f, 0f, 0f, 60f, 0f, -4f, 0f, 6.5f, 0f, -45f, 0f, 4f, 0f, 6.5f, 0f},
    };

    /** Abläufe: je Schlüssel {Tick, Haltung, Übergang (0 weich, 1 beschleunigend, 2 abbremsend)}. */
    static final int[][] FORGE = {{0, P_FORGE_IMPACT, 0}, {4, P_FORGE_LIFT, 2}, {12, P_FORGE_RAISED, 0}, {15, P_FORGE_IMPACT, 1}, {26, P_FORGE_IMPACT, 0}};
    static final int[][] AWAKEN = {{0, P_FORGE_IMPACT, 0}, {8, P_AWAKEN_LOOK, 0}, {22, P_AWAKEN_RISE, 0}, {30, P_AWAKEN_REACH, 0}, {34, P_AWAKEN_REACH, 0}, {42, P_AWAKEN_RAISE, 2}, {48, P_AWAKEN_ROAR, 0}, {56, P_AWAKEN_ROAR, 0}, {60, P_READY, 0}};
    static final int[][] THRUST = {{0, P_READY, 0}, {6, P_THRUST_DRAW, 0}, {9, P_THRUST, 1}, {13, P_THRUST, 0}, {16, P_THRUST_DRAW, 0}, {19, P_THRUST, 1}, {23, P_THRUST, 0}, {26, P_THRUST_DRAW, 0}, {29, P_THRUST, 1}, {34, P_THRUST, 0}, {40, P_READY, 0}};
    static final int[][] SLAM = {{0, P_READY, 0}, {13, P_SLAM_RAISE, 2}, {17, P_SLAM_HIT, 1}, {25, P_SLAM_HIT, 0}, {36, P_READY, 0}};
    static final int[][] SPARKS = {{0, P_READY, 0}, {6, P_SPARKS_UP, 0}, {10, P_SPARKS_HIT, 1}, {16, P_SPARKS_UP, 2}, {22, P_SPARKS_HIT, 1}, {28, P_SPARKS_UP, 2}, {34, P_SPARKS_HIT, 1}, {40, P_SPARKS_HIT, 0}, {50, P_READY, 0}};
    static final int[][] GLOBS = {{0, P_READY, 0}, {8, P_GLOBS_BACK, 0}, {12, P_GLOBS_FLING, 1}, {16, P_GLOBS_BACK, 0}, {20, P_GLOBS_FLING, 1}, {24, P_GLOBS_BACK, 0}, {28, P_GLOBS_FLING, 1}, {32, P_GLOBS_BACK, 0}, {36, P_GLOBS_FLING, 1}, {42, P_READY, 0}};
    static final int[][] CALL = {{0, P_READY, 0}, {8, P_CALL_RAISE, 0}, {12, P_CALL_HIGH, 0}, {15, P_CALL_STAMP, 1}, {22, P_CALL_STAMP, 0}, {30, P_READY, 0}};
    static final int[][] STOKE = {{0, P_READY, 0}, {12, P_STOKE_RAISE, 2}, {18, P_STOKE_PLANT, 1}, {40, P_STOKE_PLANT, 0}, {50, P_READY, 0}};
    /** Glutkette: ausholen (die Kette kreist), schleudern, heranziehen – und der Hammer empfängt das Opfer. */
    static final int[][] CHAIN = {{0, P_READY, 0}, {6, P_GLOBS_BACK, 0}, {12, P_GLOBS_BACK, 0}, {14, P_GLOBS_FLING, 1}, {20, P_GLOBS_BACK, 0},
            {24, P_SLAM_RAISE, 2}, {28, P_SLAM_HIT, 1}, {36, P_SLAM_HIT, 0}, {50, P_READY, 0}};
    /** Schmelzfluss: der Hammer hoch über den Kopf, dann auf das Dach – und er hält inne, während die Rinnen laufen. */
    static final int[][] MOLTEN = {{0, P_READY, 0}, {12, P_SLAM_RAISE, 2}, {17, P_SLAM_HIT, 1}, {40, P_SLAM_HIT, 0}, {58, P_READY, 0}};

    private GlutschmiedPoses() {}
}
