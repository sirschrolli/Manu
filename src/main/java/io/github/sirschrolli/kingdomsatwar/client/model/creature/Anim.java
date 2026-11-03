package io.github.sirschrolli.kingdomsatwar.client.model.creature;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;

/** Kleine Hilfen für die Animationen der Kreaturen-Modelle. */
public final class Anim {
    public static final float DEG = Mth.DEG_TO_RAD;

    private Anim() {}

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
