package io.github.sirschrolli.kingdomsatwar.client.boss;

import java.util.Arrays;

/**
 * Zielhaltung eines Drachen-Bosses in einem Bild: was Gang, Flug, Angriff, Zustand und Laune gerade verlangen –
 * als Zusatz zur Grundhaltung der Geometrie (Winkel im Bogenmaß, Verschiebungen in Modell-Einheiten).
 *
 * <p>Die {@link DragonRig}-Federn glätten diese Ziele, bevor das Modell sie anwendet: Hals und Schwanz schwingen
 * nach, Kopf und Kiefer federn, der Rumpf wippt.
 */
public final class DragonPose {
    public static final int FRONT_L = 0;
    public static final int FRONT_R = 1;
    public static final int HIND_L = 2;
    public static final int HIND_R = 3;
    /** Indizes der Beinwerte. */
    public static final int UPPER_X = 0;
    public static final int UPPER_Z = 1;
    public static final int SHIN_X = 2;
    public static final int FOOT_X = 3;

    // Rumpf
    public float bodyY;
    public float bodyPitch;
    public float bodyRoll;
    public float bodyYaw;
    /** Brustkorb (1 = normal; Atmen, Aufladen vor dem Odem). */
    public float chest;

    // Hals (Beugen: positiv = nach vorn/unten; wird auf die Glieder verteilt) und Kopf
    public float neckBend;
    public float neckYaw;
    public float neckRoll;
    /** Nur das letzte Halsglied (S-Kurve: oben wieder nach vorn). */
    public float neckTip;
    public float headPitch;
    public float headYaw;
    public float headRoll;
    public float jaw;

    // Schwanz: Zusatz je Glied (Gier und Nicken)
    public final float[] tailYaw;
    public final float[] tailPitch;

    // Flügel
    /** 1 = angelegt, 0 = ausgebreitet. */
    public float fold;
    public float flapAmp;
    /** Phase des Flügelschlags (Bogenmaß, läuft weiter). */
    public float flap;
    public float raise;
    public float sweep;
    public float spread;

    /** Beine: je Bein {@link #UPPER_X}, {@link #UPPER_Z}, {@link #SHIN_X}, {@link #FOOT_X}. */
    public final float[][] legs = new float[4][4];

    public DragonPose(int tails) {
        this.tailYaw = new float[tails];
        this.tailPitch = new float[tails];
        this.reset();
    }

    public void reset() {
        this.bodyY = 0;
        this.bodyPitch = 0;
        this.bodyRoll = 0;
        this.bodyYaw = 0;
        this.chest = 1;
        this.neckBend = 0;
        this.neckYaw = 0;
        this.neckRoll = 0;
        this.neckTip = 0;
        this.headPitch = 0;
        this.headYaw = 0;
        this.headRoll = 0;
        this.jaw = 0;
        Arrays.fill(this.tailYaw, 0);
        Arrays.fill(this.tailPitch, 0);
        this.fold = 1;
        this.flapAmp = 0;
        this.flap = 0;
        this.raise = 0;
        this.sweep = 0;
        this.spread = 0;
        for (float[] leg : this.legs) {
            Arrays.fill(leg, 0);
        }
    }

    public float[] leg(int index) {
        return this.legs[index];
    }
}
