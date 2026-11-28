package io.github.sirschrolli.kingdomsatwar.client.renderer.castle;

/**
 * Haltungen und Abläufe von Kuno von Rabenfels, dem Raubritter: je Haltung Zusatzwinkel (Grad) und Verschiebungen (Pixel)
 * der Knochen, je Angriff eine Folge von Haltungen an Ticks (dazwischen wird überblendet). Zu Ross setzt das Modell den
 * Reiter in den Sattel (auch wenn das Ross steigt); zu Fuß verschiebt der Knochen „knight“ ihn auf den Boden.
 *
 * <p>ERZEUGT von tools/robber_knight_poses.py – nicht von Hand ändern.
 */
final class RobberKnightPoses {
    static final int KNIGHT = 0;
    static final int TORSO = 1;
    static final int HELM = 2;
    static final int ARM_R = 3;
    static final int FOREARM_R = 4;
    static final int HAND_R = 5;
    static final int ARM_L = 6;
    static final int FOREARM_L = 7;
    static final int HAND_L = 8;
    static final int THIGH_R = 9;
    static final int SHIN_R = 10;
    static final int THIGH_L = 11;
    static final int SHIN_L = 12;
    static final int LANCE = 13;
    static final int MACE = 14;
    static final int SHIELD = 15;
    static final int TABARD = 16;
    static final int TABARD_BACK = 17;
    static final int CAPE = 18;
    static final int HORSE = 19;
    static final int NECK = 20;
    static final int HEAD = 21;
    static final int LEG_FL = 22;
    static final int SHIN_FL = 23;
    static final int LEG_FR = 24;
    static final int SHIN_FR = 25;
    static final int LEG_HL = 26;
    static final int SHIN_HL = 27;
    static final int LEG_HR = 28;
    static final int SHIN_HR = 29;
    static final int TAIL = 30;
    static final int BONES = 31;
    static final float FOOT_DY = 14.7f;
    static final float HORSE_PIVOT_Y = 5f;
    static final float SEAT_Y = -3f;
    static final float SEAT_Z = 0.6f;

    static final int P_RIDE_GUARD = 0;
    static final int P_RIDE_READY = 1;
    static final int P_RIDE_CHARGE = 2;
    static final int P_RIDE_THRUST_BACK = 3;
    static final int P_RIDE_THRUST = 4;
    static final int P_RIDE_REAR = 5;
    static final int P_RIDE_STOMP = 6;
    static final int P_RIDE_HORN = 7;
    static final int P_RIDE_POINT = 8;
    static final int P_RIDE_STAGGER = 9;
    static final int P_RIDE_FALL = 10;
    static final int P_FOOT_GROUND = 11;
    static final int P_FOOT_KNEEL = 12;
    static final int P_FOOT_READY = 13;
    static final int P_FOOT_SMASH_WIND = 14;
    static final int P_FOOT_SMASH = 15;
    static final int P_FOOT_WHIRL = 16;
    static final int P_FOOT_BASH_CROUCH = 17;
    static final int P_FOOT_BASH = 18;
    static final int P_FOOT_WALL = 19;
    static final int P_FOOT_HORN = 20;
    static final int P_FOOT_POINT = 21;
    static final int P_DEATH = 22;

    /** Je Haltung: sitzt er im Sattel? */
    static final boolean[] MOUNTED = {true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false};

    /** Je Haltung und Knochen: rx, ry, rz (Grad), dx, dy, dz (Pixel). */
    static final float[][] POSES = {
            // ride_guard
            {0f, 0f, 0f, 0f, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, -8f, 0f, 10f, 0f, 0f, 0f, -82f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, -16f, 0f, 0f, 0f, -48f, 0f, 18f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -46f, 0f, 26f, -3f, 0f, 0f, 52f, 0f, -20f, 0f, 0f, 0f, -46f, 0f, -26f, 3f, 0f, 0f, 52f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -64f, 0f, 0f, 0f, 0f, 0f, 42f, 0f, 0f, 0f, 0f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // ride_ready
            {0f, 0f, 0f, 0f, 0f, 0f, 4f, -8f, 0f, 0f, 0f, 0f, 4f, 6f, 0f, 0f, 0f, 0f, -18f, 0f, 14f, 0f, 0f, 0f, -42f, 0f, 0f, 0f, 0f, 0f, 58f, 0f, 0f, 0f, 0f, 0f, -42f, 0f, -10f, 0f, 0f, 0f, -50f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -46f, 0f, 26f, -3f, 0f, 0f, 52f, 0f, -20f, 0f, 0f, 0f, -46f, 0f, -26f, 3f, 0f, 0f, 52f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -64f, 0f, 0f, 0f, 0f, 0f, 42f, 0f, 0f, 0f, 0f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // ride_charge
            {0f, 0f, 0f, 0f, 0f, 0f, 22f, -10f, 0f, 0f, 0f, 0f, 16f, 8f, 0f, 0f, 0f, 0f, -12f, 0f, 16f, 0f, 0f, 0f, -48f, 0f, 0f, 0f, 0f, 0f, 40f, 0f, -10f, 0f, 0f, 0f, -55f, 0f, -6f, 0f, 0f, 0f, -40f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -46f, 0f, 26f, -3f, 0f, 0f, 52f, 0f, -20f, 0f, 0f, 0f, -46f, 0f, -26f, 3f, 0f, 0f, 52f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -64f, 0f, 0f, 0f, 0f, 0f, 60f, 0f, 0f, 0f, 0f, 0f, 46f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -22f, 0f, 0f, 0f, 0f, 0f, 12f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // ride_thrust_back
            {0f, 0f, 0f, 0f, 0f, 0f, -4f, 28f, 0f, 0f, 0f, 0f, 2f, -12f, 0f, 0f, 0f, 0f, 24f, 0f, 22f, 0f, 0f, 0f, -60f, 0f, 0f, 0f, 0f, 0f, 42f, 0f, 0f, 0f, 0f, 0f, -40f, 0f, -12f, 0f, 0f, 0f, -50f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -46f, 0f, 26f, -3f, 0f, 0f, 52f, 0f, -20f, 0f, 0f, 0f, -46f, 0f, -26f, 3f, 0f, 0f, 52f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -64f, 0f, 0f, 0f, 0f, 0f, 42f, 0f, 0f, 0f, 0f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // ride_thrust
            {0f, 0f, 0f, 0f, 0f, 0f, 12f, -26f, 0f, 0f, 0f, 0f, 8f, 14f, 0f, 0f, 0f, 0f, -60f, 0f, 8f, 0f, 0f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, 74f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, -20f, 0f, 0f, 0f, -40f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -46f, 0f, 26f, -3f, 0f, 0f, 52f, 0f, -20f, 0f, 0f, 0f, -46f, 0f, -26f, 3f, 0f, 0f, 52f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -64f, 0f, 0f, 0f, 0f, 0f, 42f, 0f, 0f, 0f, 0f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // ride_rear
            {0f, 0f, 0f, 0f, 0f, 0f, 24f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, -70f, 0f, 18f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 0f, -60f, 0f, -30f, 0f, 0f, 0f, -40f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -46f, 0f, 26f, -3f, 0f, 0f, 52f, 0f, -20f, 0f, 0f, 0f, -46f, 0f, -26f, 3f, 0f, 0f, 52f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -64f, 0f, 0f, 0f, 0f, 0f, 42f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, -36f, 0f, 0f, 0f, -4.5f, 3f, -30f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, -70f, 0f, 0f, 0f, 0f, 0f, 96f, 0f, 0f, 0f, 0f, 0f, -52f, 0f, 0f, 0f, 0f, 0f, 84f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f},
            // ride_stomp
            {0f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 14f, 0f, 0f, 0f, -60f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -40f, 0f, -14f, 0f, 0f, 0f, -50f, 0f, 24f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -46f, 0f, 26f, -3f, 0f, 0f, 52f, 0f, -20f, 0f, 0f, 0f, -46f, 0f, -26f, 3f, 0f, 0f, 52f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -64f, 0f, 0f, 0f, 0f, 0f, 42f, 0f, 0f, 0f, 0f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0.6f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 14f, 0f, 0f, 0f, 0f, 0f, -22f, 0f, 0f, 0f, 0f, 0f, 8f, 0f, 0f, 0f, 0f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f},
            // ride_horn
            {0f, 0f, 0f, 0f, 0f, 0f, -6f, 10f, 0f, 0f, 0f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 12f, 0f, 0f, 0f, -82f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -128f, 0f, 12f, 0f, 0f, 0f, -62f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -46f, 0f, 26f, -3f, 0f, 0f, 52f, 0f, -20f, 0f, 0f, 0f, -46f, 0f, -26f, 3f, 0f, 0f, 52f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -64f, 0f, 0f, 0f, 0f, 0f, 42f, 0f, 0f, 0f, 0f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // ride_point
            {0f, 0f, 0f, 0f, 0f, 0f, -8f, -16f, 0f, 0f, 0f, 0f, -4f, 12f, 0f, 0f, 0f, 0f, -120f, 0f, 6f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 93f, 0f, 0f, 0f, 0f, 0f, -24f, 0f, -18f, 0f, 0f, 0f, -46f, 0f, 22f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -46f, 0f, 26f, -3f, 0f, 0f, 52f, 0f, -20f, 0f, 0f, 0f, -46f, 0f, -26f, 3f, 0f, 0f, 52f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -64f, 0f, 0f, 0f, 0f, 0f, 42f, 0f, 0f, 0f, 0f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // ride_stagger
            {0f, 0f, 0f, 0f, 0f, 0f, -24f, 0f, 8f, 0f, 0f, 0f, 18f, 0f, -10f, 0f, 0f, 0f, -40f, 0f, 48f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, -50f, 0f, 0f, 0f, -30f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -46f, 0f, 26f, -3f, 0f, 0f, 52f, 0f, -20f, 0f, 0f, 0f, -46f, 0f, -26f, 3f, 0f, 0f, 52f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -64f, 0f, 0f, 0f, 0f, 0f, 42f, 0f, 0f, 0f, 0f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, -24f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 18f, 0f, 0f, 0f, 0f, 0f},
            // ride_fall
            {-74f, 0f, 10f, 0f, 6f, 9f, 10f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, -160f, 0f, 30f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -150f, 0f, -30f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -80f, 0f, 16f, 0f, 0f, 0f, 40f, 0f, 0f, 0f, 0f, 0f, -70f, 0f, -16f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -40f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, -3.5f, 2.5f, -26f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -60f, 0f, 0f, 0f, 0f, 0f, 90f, 0f, 0f, 0f, 0f, 0f, -48f, 0f, 0f, 0f, 0f, 0f, 80f, 0f, 0f, 0f, 0f, 0f, 26f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // foot_ground
            {-90f, 0f, 0f, 0f, 24.3f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, -150f, 0f, 30f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -160f, 0f, -36f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 8f, 0f, 0f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, -4f, 0f, -10f, 0f, 0f, 0f, 8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // foot_kneel
            {0f, 0f, 0f, 0f, 19.5f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 16f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 20f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -40f, 0f, -10f, 0f, 0f, 0f, -40f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -88f, 0f, 0f, 0f, 0f, 0f, 88f, 0f, 0f, 0f, 0f, 0f, -2f, 0f, 0f, 0f, 0f, 0f, 86f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // foot_ready
            {0f, 0f, 0f, 0f, 14.7f, 0f, 6f, -12f, 0f, 0f, 0f, 0f, 2f, 10f, 0f, 0f, 0f, 0f, -58f, 0f, 26f, 0f, 0f, 0f, -62f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, -46f, 0f, -6f, 0f, 0f, 0f, -52f, 0f, 34f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -14f, 0f, 6f, 0f, 0f, 0f, 16f, 0f, 0f, 0f, 0f, 0f, 12f, 0f, -6f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // foot_smash_wind
            {0f, 0f, 0f, 0f, 14.7f, 0f, -14f, 6f, 0f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, -176f, 0f, 6f, 0f, 0f, 0f, -36f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, -24f, 0f, -20f, 0f, 0f, 0f, -30f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -18f, 0f, 6f, 0f, 0f, 0f, 18f, 0f, 0f, 0f, 0f, 0f, 16f, 0f, -6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // foot_smash
            {0f, 0f, 0f, 0f, 16.7f, 0f, 34f, -4f, 0f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, -72f, 0f, 4f, 0f, 0f, 0f, -8f, 0f, 0f, 0f, 0f, 0f, 40f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, -24f, 0f, 0f, 0f, -40f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -46f, 0f, 6f, 0f, 0f, 0f, 56f, 0f, 0f, 0f, 0f, 0f, 24f, 0f, -6f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // foot_whirl
            {0f, 0f, 0f, 0f, 14.7f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, 92f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -90f, 0f, 0f, 0f, -10f, 0f, -84f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -16f, 0f, 10f, 0f, 0f, 0f, 20f, 0f, 0f, 0f, 0f, 0f, 10f, 0f, -10f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 50f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // foot_bash_crouch
            {0f, 0f, 0f, 0f, 16.3f, 0f, 22f, 20f, 0f, 0f, 0f, 0f, -14f, -12f, 0f, 0f, 0f, 0f, 30f, 0f, 20f, 0f, 0f, 0f, -50f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -70f, 0f, 6f, 0f, 0f, 0f, -60f, 0f, 40f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -34f, 0f, 6f, 0f, 0f, 0f, 44f, 0f, 0f, 0f, 0f, 0f, -6f, 0f, -6f, 0f, 0f, 0f, 26f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // foot_bash
            {0f, 0f, 0f, 0f, 14.7f, 0f, 16f, -18f, 0f, 0f, 0f, 0f, -8f, 10f, 0f, 0f, 0f, 0f, 24f, 0f, 20f, 0f, 0f, 0f, -50f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -86f, 0f, 0f, 0f, 0f, 0f, -24f, 0f, 50f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 18f, 0f, 6f, 0f, 0f, 0f, 10f, 0f, 0f, 0f, 0f, 0f, -42f, 0f, -6f, 0f, 0f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // foot_wall
            {0f, 0f, 0f, 0f, 16.1f, 0f, 16f, 8f, 0f, 0f, 0f, 0f, -10f, -6f, 0f, 0f, 0f, 0f, 10f, 0f, 24f, 0f, 0f, 0f, -70f, 0f, 0f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, -88f, 0f, 12f, 0f, 0f, 0f, -44f, 0f, 58f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -28f, 0f, 8f, 0f, 0f, 0f, 38f, 0f, 0f, 0f, 0f, 0f, -4f, 0f, -8f, 0f, 0f, 0f, 22f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // foot_horn
            {0f, 0f, 0f, 0f, 14.7f, 0f, -4f, 10f, 0f, 0f, 0f, 0f, -14f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 20f, 0f, 0f, 0f, -50f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -128f, 0f, 12f, 0f, 0f, 0f, -62f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -6f, 0f, 4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, -4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // foot_point
            {0f, 0f, 0f, 0f, 14.7f, 0f, -4f, -14f, 0f, 0f, 0f, 0f, -6f, 10f, 0f, 0f, 0f, 0f, -130f, 0f, 8f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 109f, 0f, 0f, 0f, 0f, 0f, -40f, 0f, -14f, 0f, 0f, 0f, -50f, 0f, 30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
            // death
            {0f, 0f, 0f, 0f, 19.7f, 0f, 46f, 0f, 0f, 0f, 0f, 0f, 36f, 0f, 10f, 0f, 0f, 0f, -20f, 0f, 10f, 0f, 0f, 0f, -10f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -10f, 0f, -20f, 0f, 0f, 0f, -20f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -90f, 0f, 0f, 0f, 0f, 0f, 90f, 0f, 0f, 0f, 0f, 0f, -84f, 0f, 0f, 0f, 0f, 0f, 84f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, -30f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f},
    };

    /** Abläufe: je Schlüssel {Tick, Haltung, Übergang (0 weich, 1 beschleunigend, 2 abbremsend)}. */
    static final int[][] AWAKEN = {{0, P_RIDE_GUARD, 0}, {14, P_RIDE_GUARD, 0}, {26, P_RIDE_REAR, 2}, {38, P_RIDE_REAR, 0}, {44, P_RIDE_STOMP, 1}, {54, P_RIDE_READY, 2}, {70, P_RIDE_READY, 0}};
    static final int[][] CHARGE = {{0, P_RIDE_READY, 0}, {12, P_RIDE_CHARGE, 2}, {200, P_RIDE_CHARGE, 0}};
    static final int[][] HALT = {{0, P_RIDE_CHARGE, 0}, {6, P_RIDE_REAR, 2}, {14, P_RIDE_REAR, 0}, {20, P_RIDE_STOMP, 1}, {30, P_RIDE_READY, 2}};
    static final int[][] THRUST = {{0, P_RIDE_READY, 0}, {8, P_RIDE_THRUST_BACK, 2}, {12, P_RIDE_THRUST, 1}, {18, P_RIDE_THRUST, 0}, {26, P_RIDE_READY, 0}};
    static final int[][] TRAMPLE = {{0, P_RIDE_READY, 0}, {12, P_RIDE_REAR, 2}, {19, P_RIDE_REAR, 0}, {22, P_RIDE_STOMP, 1}, {32, P_RIDE_STOMP, 0}, {40, P_RIDE_READY, 0}};
    static final int[][] HORN_RIDE = {{0, P_RIDE_READY, 0}, {8, P_RIDE_HORN, 2}, {34, P_RIDE_HORN, 0}, {42, P_RIDE_READY, 0}};
    static final int[][] VOLLEY_RIDE = {{0, P_RIDE_READY, 0}, {10, P_RIDE_POINT, 2}, {40, P_RIDE_POINT, 0}, {50, P_RIDE_READY, 0}};
    static final int[][] STAGGER = {{0, P_RIDE_STAGGER, 1}, {48, P_RIDE_STAGGER, 0}, {60, P_RIDE_READY, 0}};
    static final int[][] UNHORSE = {{0, P_RIDE_READY, 0}, {10, P_RIDE_REAR, 2}, {16, P_RIDE_FALL, 1}, {22, P_FOOT_GROUND, 1}, {36, P_FOOT_GROUND, 0}, {46, P_FOOT_KNEEL, 2}, {56, P_FOOT_READY, 2}, {64, P_FOOT_READY, 0}};
    static final int[][] SMASH = {{0, P_FOOT_READY, 0}, {12, P_FOOT_SMASH_WIND, 2}, {17, P_FOOT_SMASH_WIND, 0}, {20, P_FOOT_SMASH, 1}, {30, P_FOOT_SMASH, 0}, {38, P_FOOT_READY, 0}};
    static final int[][] WHIRL = {{0, P_FOOT_READY, 0}, {8, P_FOOT_WHIRL, 2}, {40, P_FOOT_WHIRL, 0}, {48, P_FOOT_READY, 0}};
    static final int[][] BASH = {{0, P_FOOT_READY, 0}, {8, P_FOOT_BASH_CROUCH, 2}, {12, P_FOOT_BASH, 1}, {20, P_FOOT_BASH, 0}, {28, P_FOOT_READY, 0}};
    static final int[][] WALL = {{0, P_FOOT_READY, 0}, {8, P_FOOT_WALL, 2}, {62, P_FOOT_WALL, 0}, {70, P_FOOT_READY, 0}};
    static final int[][] HORN_FOOT = {{0, P_FOOT_READY, 0}, {8, P_FOOT_HORN, 2}, {34, P_FOOT_HORN, 0}, {42, P_FOOT_READY, 0}};
    static final int[][] VOLLEY_FOOT = {{0, P_FOOT_READY, 0}, {10, P_FOOT_POINT, 2}, {40, P_FOOT_POINT, 0}, {50, P_FOOT_READY, 0}};

    private RobberKnightPoses() {}
}
