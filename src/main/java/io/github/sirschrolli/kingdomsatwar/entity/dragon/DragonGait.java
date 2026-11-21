package io.github.sirschrolli.kingdomsatwar.entity.dragon;

import net.minecraft.util.Mth;

/**
 * Gangarten der Drachen: langsam ein ruhiger Schritt (die Beine setzen nacheinander auf, links hinten, links vorn, rechts
 * hinten, rechts vorn), schnell ein Galopp (erst beide Hinterbeine, dann beide Vorderbeine, kurze Standphase, weite
 * Sprünge). Dazwischen gehen die Gangarten fließend ineinander über.
 *
 * <p>Der Weg einer ganzen Schrittfolge ergibt sich aus der Beinlänge und wie weit die Beine ausgreifen – so schieben die
 * Füße in der Standphase genau so weit zurück, wie der Drache vorankommt, und nichts rutscht über den Boden. Das Wesen
 * zählt seinen Weg in Schrittfolgen ({@link DragonEntity#stride}), das Modell bewegt die Beine danach.
 */
public final class DragonGait {
    /** Höhe der Hüfte über dem Boden bei voller Größe (Blöcke). */
    private static final float HIP = 1.0f;
    /** Ausschlag des Oberschenkels bei vollem Schritt (Bogenmaß). */
    public static final float SWING = 0.42f;

    private DragonGait() {}

    /** Anteil Galopp (0 = Schritt, 1 = Galopp). */
    public static float run(float speed) {
        float x = Mth.clamp((speed - 0.55f) / 0.35f, 0.0f, 1.0f);
        return x * x * (3.0f - 2.0f * x);
    }

    /** Anteil der Standphase an der Schrittfolge (im Galopp kurz, dazwischen fliegt der Drache). */
    public static float stance(float run) {
        return Mth.lerp(run, 0.62f, 0.4f);
    }

    /** Wie weit die Beine ausgreifen (Anteil des vollen Schritts; im Galopp weiter als voll). */
    public static float reach(float speed, float run) {
        return Mth.lerp(run, 0.35f + 0.65f * Math.min(1.0f, speed / 0.55f), 1.45f);
    }

    /** Weg einer ganzen Schrittfolge in Blöcken. */
    public static float length(float scale, float speed) {
        float run = run(speed);
        float travel = 2.0f * HIP * (float) Math.tan(SWING * reach(speed, run));
        return Math.max(0.3f, travel / stance(run)) * scale;
    }
}
