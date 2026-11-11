package io.github.sirschrolli.kingdomsatwar.client.model.creature;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;

/** Kleine Hilfen für die Animationen der Kreaturen-Modelle. */
public final class Anim {
    public static final float DEG = Mth.DEG_TO_RAD;

    /** Weiche Werte je Wesen (Wert und Zeitpunkt, bis zu acht je Wesen); verschwindet mit dem Wesen. */
    private static final Map<Object, float[]> FOLLOW = new WeakHashMap<>();

    private Anim() {}

    /**
     * Weicher Übergang je Wesen: der Wert folgt {@code target} mit {@code rate} je Sekunde (bei jeder Bildrate gleich
     * schnell), so springt eine Haltung nicht von einem Bild aufs nächste um. {@code slot} (0..7) trennt mehrere Werte
     * desselben Wesens.
     */
    public static float follow(Object entity, int slot, float target, float ageInTicks, float rate) {
        float[] v = FOLLOW.computeIfAbsent(entity, e -> new float[] {Float.NaN, 0, Float.NaN, 0, Float.NaN, 0, Float.NaN, 0, Float.NaN, 0,
                Float.NaN, 0, Float.NaN, 0, Float.NaN, 0});
        int i = slot * 2;
        float time = ageInTicks / 20.0f;
        float dt = time - v[i + 1];
        if (Float.isNaN(v[i]) || dt < 0.0f || dt > 1.0f) {
            v[i] = target;
        } else {
            v[i] += (target - v[i]) * (1.0f - (float) Math.exp(-rate * dt));
        }
        v[i + 1] = time;
        return v[i];
    }

    /** Sekunden seit Start einer Animation, oder -1, wenn sie nicht läuft. */
    public static float seconds(AnimationState state, float ageInTicks) {
        return state.isStarted() ? state.getTimeInMillis(ageInTicks) / 1000.0f : -1.0f;
    }

    /** Glatte Hüllkurve: steigt bis {@code in}, hält bis {@code hold}, fällt bis {@code end} (sonst 0). */
    public static float bump(float x, float in, float hold, float end) {
        if (x < 0.0f || x >= end) {
            return 0.0f;
        }
        if (x < in) {
            return Mth.sin(x / in * Mth.HALF_PI);
        }
        if (x < hold) {
            return 1.0f;
        }
        return Mth.cos((x - hold) / (end - hold) * Mth.HALF_PI);
    }

    /** Laufzyklus wie bei Vierbeinern: Schwung eines Beins (Phase 0 oder π). */
    public static float leg(float pos, float speed, float phase, float amplitude) {
        return Mth.cos(pos * 0.6662f + phase) * amplitude * speed;
    }
}
