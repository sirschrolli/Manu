package io.github.sirschrolli.kingdomsatwar.client.boss;

import net.minecraft.util.Mth;

/** Kleine Helfer für die Boss-Animationen: Hüllkurven und weiche Übergänge (alle Zeiten in Sekunden). */
public final class BossAnim {
    private BossAnim() {}

    /** Weicher Übergang 0..1 (Smoothstep). */
    public static float smooth(float x) {
        x = Mth.clamp(x, 0.0f, 1.0f);
        return x * x * (3.0f - 2.0f * x);
    }

    /** Steigt von a bis b auf 1 (weich). */
    public static float rise(float t, float a, float b) {
        return b <= a ? (t >= a ? 1 : 0) : smooth((t - a) / (b - a));
    }

    /** Trapez: steigt a→b auf 1, hält bis c, fällt bis d auf 0 (weich). */
    public static float env(float t, float a, float b, float c, float d) {
        if (t <= a || t >= d) {
            return 0.0f;
        }
        if (t < b) {
            return smooth((t - a) / (b - a));
        }
        if (t <= c) {
            return 1.0f;
        }
        return 1.0f - smooth((t - c) / (d - c));
    }

    /** Schneller Schlag: sehr kurzer Anstieg bei t0, Abklingen bis t1. */
    public static float strike(float t, float t0, float t1) {
        if (t < t0 - 0.05f || t > t1) {
            return 0.0f;
        }
        if (t < t0) {
            return (t - (t0 - 0.05f)) / 0.05f;
        }
        return 1.0f - smooth((t - t0) / (t1 - t0));
    }

    /** Anteil des Abschlags am ganzen Flügelschlag. */
    public static final float DOWNSTROKE = 0.42f;

    private static float strokeU(float phase) {
        float u = phase / Mth.TWO_PI;
        return u - Mth.floor(u);
    }

    /** Stellung des Flügels im Schlag: 1 = ganz oben, -1 = ganz unten. Der Abschlag ist schneller als der Aufschlag. */
    public static float stroke(float phase) {
        float u = strokeU(phase);
        return u < DOWNSTROKE ? Mth.cos(Mth.PI * u / DOWNSTROKE) : -Mth.cos(Mth.PI * (u - DOWNSTROKE) / (1.0f - DOWNSTROKE));
    }

    /** Kraft des Abschlags (0..1..0, nur im Abschlag). */
    public static float downstroke(float phase) {
        float u = strokeU(phase);
        return u < DOWNSTROKE ? Mth.sin(Mth.PI * u / DOWNSTROKE) : 0.0f;
    }

    /** Aufschlag (0..1..0): dann knickt der Flügel im Handgelenk ein, die Finger legen sich zusammen. */
    public static float upstroke(float phase) {
        float u = strokeU(phase);
        return u < DOWNSTROKE ? 0.0f : Mth.sin(Mth.PI * (u - DOWNSTROKE) / (1.0f - DOWNSTROKE));
    }

    /** Gedämpfte Schwingung nach einem Ereignis (z. B. Nachwippen nach dem Aufprall). */
    public static float wobble(float t, float t0, float freq, float decay) {
        if (t < t0) {
            return 0.0f;
        }
        float x = t - t0;
        return Mth.sin(x * freq * Mth.TWO_PI) * (float) Math.exp(-x * decay);
    }
}
