package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

/**
 * Haltungen und Abläufe des Steinhüters: je Haltung Zusatzwinkel (Grad) und Verschiebungen (Pixel) der Knochen, je
 * Angriff eine Folge von Haltungen an Ticks (dazwischen wird überblendet).
 *
 * <p>ERZEUGT von tools/stone_keeper_poses.py – nicht von Hand ändern.
 */
final class StoneKeeperPoses {
    static final int BODY = 0;
    static final int HEAD = 1;
    static final int JAW = 2;
    static final int LEFT_ARM = 3;
    static final int LEFT_FOREARM = 4;
    static final int RIGHT_ARM = 5;
    static final int RIGHT_FOREARM = 6;
    static final int LEFT_LEG = 7;
    static final int LEFT_SHIN = 8;
    static final int RIGHT_LEG = 9;
    static final int RIGHT_SHIN = 10;
    static final int BOULDER = 11;
    static final int BONES = 12;

    static final int P_READY = 0;
    static final int P_DORMANT = 1;
    static final int P_WAKE_LOOK = 2;
    static final int P_WAKE_PUSH = 3;
    static final int P_WAKE_ROAR = 4;
    static final int P_SMASH_RAISE = 5;
    static final int P_SMASH_HIT = 6;
    static final int P_STOMP_LIFT = 7;
    static final int P_STOMP_HIT = 8;
    static final int P_BOULDER_DIG = 9;
    static final int P_BOULDER_LIFT = 10;
    static final int P_BOULDER_THROW = 11;
    static final int P_CRYSTALS_SLAM = 12;
    static final int P_BEAM_CHARGE = 13;
    static final int P_BEAM_FIRE = 14;
    static final int P_LEAP_CROUCH = 15;
    static final int P_LEAP_AIR = 16;
    static final int P_LEAP_LAND = 17;
    static final int P_ARMOR_FLEX = 18;
    static final int P_STAGGER = 19;
    static final int P_DEATH = 20;

    /** Je Haltung und Knochen: rx, ry, rz (Grad), dx, dy, dz (Pixel). */
    static final float[][] POSES = {
            // ready
            {6f, 0f, 0f, 0f, 0.6f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -24f, 0f, -4f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, -24f, 0f, 4f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, -3f, 0f, 0.6f, 0f, 12f, 0f, 0f, 0f, 0f, 0f, 4f, 0f, 3f, 0f, 0.6f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // dormant
            {30f, 0f, 0f, 0f, 8.5f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -76f, 0f, -8f, 0f, 0f, 0f, 46f, 0f, 0f, 0f, 0f, 0f, -84f, 0f, 6f, 0f, 0f, 0f, 56f, 0f, 0f, 0f, 0f, 0f, -78f, 0f, -6f, 0f, 8.5f, 0f, 74f, 0f, 0f, 0f, 0f, 0f, 16f, 0f, 6f, 0f, 8.5f, 0f, 80f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // wake_look
            {26f, 0f, 0f, 0f, 8.5f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -72f, 0f, -8f, 0f, 0f, 0f, 46f, 0f, 0f, 0f, 0f, 0f, -80f, 0f, 6f, 0f, 0f, 0f, 56f, 0f, 0f, 0f, 0f, 0f, -78f, 0f, -6f, 0f, 8.5f, 0f, 74f, 0f, 0f, 0f, 0f, 0f, 16f, 0f, 6f, 0f, 8.5f, 0f, 80f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // wake_push
            {22f, 0f, 0f, 0f, 4.5f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -58f, 0f, -10f, 0f, 0f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, -62f, 0f, 10f, 0f, 0f, 0f, 36f, 0f, 0f, 0f, 0f, 0f, -54f, 0f, -5f, 0f, 4.5f, 0f, 60f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 5f, 0f, 4.5f, 0f, 38f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // wake_roar
            {-16f, 0f, 0f, 0f, 0f, 0f, -34f, 0f, 0f, 0f, 0f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, -112f, 0f, -34f, 0f, 0f, 0f, -28f, 0f, 0f, 0f, 0f, 0f, -112f, 0f, 34f, 0f, 0f, 0f, -28f, 0f, 0f, 0f, 0f, 0f, -12f, 0f, -7f, 0f, 0f, 0f, 12f, 0f, 0f, 0f, 0f, 0f, 12f, 0f, 7f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // smash_raise
            {-14f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, -164f, 0f, 6f, 0f, 0f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, -164f, 0f, -6f, 0f, 0f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, -14f, 0f, -5f, 0f, 0f, 0f, 14f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 5f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // smash_hit
            {42f, 0f, 0f, 0f, 5.5f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 18f, 0f, 0f, 0f, 0f, 0f, -102f, 0f, 10f, 0f, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 0f, -102f, 0f, -10f, 0f, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 0f, -40f, 0f, -6f, 0f, 5.5f, 0f, 50f, 0f, 0f, 0f, 0f, 0f, 20f, 0f, 6f, 0f, 5.5f, 0f, 22f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // stomp_lift
            {-6f, 6f, 0f, 0f, 0f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -34f, 0f, -30f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, -44f, 0f, 26f, 0f, 0f, 0f, -16f, 0f, 0f, 0f, 0f, 0f, 8f, 0f, -4f, 0f, 0f, 0f, -2f, 0f, 0f, 0f, 0f, 0f, -74f, 0f, 4f, 0f, 0f, 0f, 76f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // stomp_hit
            {22f, 0f, 0f, 0f, 3f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 16f, 0f, 0f, 0f, 0f, 0f, -48f, 0f, -22f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, -54f, 0f, 22f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 14f, 0f, -6f, 0f, 3f, 0f, 16f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 6f, 0f, 3f, 0f, 36f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // boulder_dig
            {34f, 0f, 0f, 0f, 5f, 0f, 14f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -74f, 0f, -14f, 0f, 0f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, -106f, 0f, 2f, 0f, 0f, 0f, 50f, 0f, 0f, 0f, 0f, 0f, -42f, 0f, -6f, 0f, 5f, 0f, 50f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 6f, 0f, 5f, 0f, 36f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 30f, -14f},
            // boulder_lift
            {-14f, 0f, 0f, 0f, 0f, 0f, -22f, 0f, 0f, 0f, 0f, 0f, 12f, 0f, 0f, 0f, 0f, 0f, -154f, 0f, 14f, 0f, 0f, 0f, -16f, 0f, 0f, 0f, 0f, 0f, -154f, 0f, -14f, 0f, 0f, 0f, -16f, 0f, 0f, 0f, 0f, 0f, -12f, 0f, -6f, 0f, 0f, 0f, 12f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 6f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // boulder_throw
            {30f, 0f, 0f, 0f, 2f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 22f, 0f, 0f, 0f, 0f, 0f, -138f, 0f, 8f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, -138f, 0f, -8f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, -6f, 0f, 2f, 0f, 36f, 0f, 0f, 0f, 0f, 0f, 24f, 0f, 6f, 0f, 2f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // crystals_slam
            {44f, 0f, 0f, 0f, 6f, 0f, 2f, 0f, 0f, 0f, 0f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, -94f, 0f, -2f, 0f, 0f, 0f, 48f, 0f, 0f, 0f, 0f, 0f, -94f, 0f, 2f, 0f, 0f, 0f, 48f, 0f, 0f, 0f, 0f, 0f, -46f, 0f, -7f, 0f, 6f, 0f, 52f, 0f, 0f, 0f, 0f, 0f, 18f, 0f, 7f, 0f, 6f, 0f, 32f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // beam_charge
            {-18f, 0f, 0f, 0f, 0f, 0f, -24f, 0f, 0f, 0f, 0f, 0f, 8f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, -36f, 0f, 0f, 0f, -2f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, 36f, 0f, 0f, 0f, -2f, 0f, 0f, 0f, 0f, 0f, -14f, 0f, -7f, 0f, 0f, 0f, 14f, 0f, 0f, 0f, 0f, 0f, 12f, 0f, 7f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // beam_fire
            {2f, 0f, 0f, 0f, 1.5f, 0f, -12f, 0f, 0f, 0f, 0f, 0f, 26f, 0f, 0f, 0f, 0f, 0f, 16f, 0f, -40f, 0f, 0f, 0f, -2f, 0f, 0f, 0f, 0f, 0f, 16f, 0f, 40f, 0f, 0f, 0f, -2f, 0f, 0f, 0f, 0f, 0f, -22f, 0f, -8f, 0f, 1.5f, 0f, 28f, 0f, 0f, 0f, 0f, 0f, 18f, 0f, 8f, 0f, 1.5f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // leap_crouch
            {34f, 0f, 0f, 0f, 6.5f, 0f, -12f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, -10f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 10f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, -56f, 0f, -6f, 0f, 6.5f, 0f, 86f, 0f, 0f, 0f, 0f, 0f, -50f, 0f, 6f, 0f, 6.5f, 0f, 86f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // leap_air
            {4f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 14f, 0f, 0f, 0f, 0f, 0f, -164f, 0f, -18f, 0f, 0f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, -164f, 0f, 18f, 0f, 0f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, -60f, 0f, -8f, 0f, 0f, 0f, 84f, 0f, 0f, 0f, 0f, 0f, -46f, 0f, 8f, 0f, 0f, 0f, 86f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // leap_land
            {40f, 0f, 0f, 0f, 6f, 0f, 8f, 0f, 0f, 0f, 0f, 0f, 22f, 0f, 0f, 0f, 0f, 0f, -96f, 0f, -24f, 0f, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 0f, -96f, 0f, 24f, 0f, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 0f, -48f, 0f, -8f, 0f, 6f, 0f, 58f, 0f, 0f, 0f, 0f, 0f, 16f, 0f, 8f, 0f, 6f, 0f, 38f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // armor_flex
            {-6f, 0f, 0f, 0f, 0f, 0f, -28f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, -66f, 0f, -40f, 0f, 0f, 0f, -74f, 0f, 0f, 0f, 0f, 0f, -66f, 0f, 40f, 0f, 0f, 0f, -74f, 0f, 0f, 0f, 0f, 0f, -14f, 0f, -8f, 0f, 0f, 0f, 14f, 0f, 0f, 0f, 0f, 0f, 12f, 0f, 8f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // stagger
            {24f, 0f, 0f, 0f, 5f, 0f, 30f, 0f, 10f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, -52f, 0f, -12f, 0f, 0f, 0f, 26f, 0f, 0f, 0f, 0f, 0f, -60f, 0f, 8f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, -52f, 0f, -6f, 0f, 5f, 0f, 60f, 0f, 0f, 0f, 0f, 0f, 12f, 0f, 6f, 0f, 5f, 0f, 44f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // death
            {40f, 0f, 0f, 0f, 9.5f, 0f, 42f, 0f, 14f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -62f, 0f, -18f, 0f, 0f, 0f, 26f, 0f, 0f, 0f, 0f, 0f, -66f, 0f, 16f, 0f, 0f, 0f, 26f, 0f, 0f, 0f, 0f, 0f, -12f, 0f, -7f, 0f, 9.5f, 0f, 96f, 0f, 0f, 0f, 0f, 0f, -4f, 0f, 7f, 0f, 9.5f, 0f, 92f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
    };

    /** Abläufe: je Schlüssel {Tick, Haltung, Übergang (0 weich, 1 beschleunigend, 2 abbremsend)}. */
    static final int[][] DORMANT = {{0, P_DORMANT, 0}};
    static final int[][] AWAKEN = {{0, P_DORMANT, 0}, {10, P_WAKE_LOOK, 0}, {26, P_WAKE_PUSH, 0}, {38, P_WAKE_ROAR, 2}, {56, P_WAKE_ROAR, 0}, {70, P_READY, 0}};
    static final int[][] SMASH = {{0, P_READY, 0}, {12, P_SMASH_RAISE, 2}, {16, P_SMASH_HIT, 1}, {26, P_SMASH_HIT, 0}, {36, P_READY, 0}};
    static final int[][] STOMP = {{0, P_READY, 0}, {12, P_STOMP_LIFT, 2}, {16, P_STOMP_HIT, 1}, {26, P_STOMP_HIT, 0}, {36, P_READY, 0}};
    static final int[][] HURL = {{0, P_READY, 0}, {10, P_BOULDER_DIG, 0}, {16, P_BOULDER_DIG, 0}, {26, P_BOULDER_LIFT, 2}, {32, P_BOULDER_LIFT, 0}, {36, P_BOULDER_THROW, 1}, {44, P_BOULDER_THROW, 0}, {54, P_READY, 0}};
    static final int[][] CRYSTALS = {{0, P_READY, 0}, {12, P_SMASH_RAISE, 2}, {16, P_CRYSTALS_SLAM, 1}, {40, P_CRYSTALS_SLAM, 0}, {52, P_READY, 0}};
    static final int[][] BEAM = {{0, P_READY, 0}, {16, P_BEAM_CHARGE, 2}, {30, P_BEAM_CHARGE, 0}, {34, P_BEAM_FIRE, 1}, {70, P_BEAM_FIRE, 0}, {82, P_READY, 0}};
    static final int[][] LEAP = {{0, P_READY, 0}, {12, P_LEAP_CROUCH, 2}, {16, P_LEAP_AIR, 1}, {40, P_LEAP_AIR, 0}};
    static final int[][] LAND = {{0, P_LEAP_AIR, 0}, {3, P_LEAP_LAND, 1}, {14, P_LEAP_LAND, 0}, {26, P_READY, 0}};
    static final int[][] ARMOR = {{0, P_READY, 0}, {12, P_ARMOR_FLEX, 2}, {40, P_ARMOR_FLEX, 0}, {52, P_READY, 0}};
    static final int[][] STAGGER = {{0, P_READY, 0}, {6, P_STAGGER, 1}, {54, P_STAGGER, 0}, {64, P_READY, 0}};

    private StoneKeeperPoses() {}
}
