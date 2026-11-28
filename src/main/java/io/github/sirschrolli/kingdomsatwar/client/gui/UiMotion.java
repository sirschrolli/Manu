package io.github.sirschrolli.kingdomsatwar.client.gui;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

/**
 * Bewegung in den Fenstern (seit 1.12): Ein Fenster geht sanft auf – es wächst in gut einer Fünftelsekunde aus etwas
 * kleiner und von etwas weiter unten an seinen Platz. Knöpfe und Karten blenden unter der Maus weich über, statt
 * umzuspringen.
 */
public final class UiMotion {
    /** So lange (Millisekunden) geht ein Fenster auf. */
    public static final float OPEN_MS = 210.0f;
    /** So lange blendet ein Knopf unter der Maus auf (und etwas langsamer wieder ab). */
    private static final float HOVER_MS = 90.0f;
    private static final Map<Long, Fade> HOVER = new HashMap<>();

    private static final class Fade {
        float value;
        long at;
    }

    private UiMotion() {}

    /** Wie weit ein Fenster, das um {@code openedAt} aufging, schon offen ist (0 bis 1, weich auslaufend). */
    public static float open(long openedAt) {
        float k = Mth.clamp((Util.getMillis() - openedAt) / OPEN_MS, 0.0f, 1.0f);
        float u = 1.0f - k;
        return 1.0f - u * u * u;
    }

    /**
     * Ab hier geht alles, was gezeichnet wird, mit dem Fenster auf (bis {@link #end}): um die Mitte (cx, cy) vergrößert, von
     * etwas weiter unten. Immer paarweise mit {@link #end} aufrufen.
     */
    public static void begin(GuiGraphicsExtractor g, long openedAt, float cx, float cy) {
        g.pose().pushMatrix();
        float e = open(openedAt);
        if (e < 1.0f) {
            float s = 0.93f + 0.07f * e;
            g.pose().translate(cx, cy + 10.0f * (1.0f - e));
            g.pose().scale(s, s);
            g.pose().translate(-cx, -cy);
        }
    }

    public static void end(GuiGraphicsExtractor g) {
        g.pose().popMatrix();
    }

    /**
     * Ein weicher Wert zwischen 0 und 1 für das Element an dieser Stelle, der {@code on} folgt – z. B. 1, solange die Maus
     * über einem Knopf ist. So blendet der Knopf über, statt umzuspringen.
     */
    public static float hover(int x, int y, int w, int h, boolean on) {
        long key = ((long) (x & 0xFFFF) << 48) | ((long) (y & 0xFFFF) << 32) | ((long) (w & 0xFFFF) << 16) | (h & 0xFFFF);
        long now = Util.getMillis();
        Fade fade = HOVER.get(key);
        if (fade == null) {
            if (HOVER.size() > 1024) {
                HOVER.clear();
            }
            fade = new Fade();
            fade.value = 0.0f;
            fade.at = now;
            HOVER.put(key, fade);
        }
        float dt = Math.min(120.0f, now - fade.at);
        fade.at = now;
        fade.value = on ? Math.min(1.0f, fade.value + dt / HOVER_MS) : Math.max(0.0f, fade.value - dt / (HOVER_MS * 1.4f));
        float v = fade.value;
        return v * v * (3.0f - 2.0f * v);
    }

    /** Eine Farbe zwischen {@code from} und {@code to} (ARGB). */
    public static int mix(int from, int to, float k) {
        return k <= 0.0f ? from : k >= 1.0f ? to : ARGB.srgbLerp(k, from, to);
    }
}
