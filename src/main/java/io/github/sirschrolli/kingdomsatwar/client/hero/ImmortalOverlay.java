package io.github.sirschrolli.kingdomsatwar.client.hero;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import io.github.sirschrolli.kingdomsatwar.network.HeroImmortalPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Das Licht der Unsterblichkeit auf dem Bildschirm des Geretteten (gut vier Sekunden, über dem Fadenkreuz):
 *
 * <ol>
 *   <li>der Bildschirm wird weiß-golden und klärt sich wieder, vom unteren Rand und von den Seiten leuchtet warmes Licht,</li>
 *   <li>drei Lichtringe laufen nach außen, aus dem Licht tritt das Abzeichen des Unsterblichen, hinter ihm drehen sich
 *       Strahlenkränze in den schillernden Farben seines Rangs,</li>
 *   <li>Lichtfunken steigen vom unteren Rand auf und funkeln,</li>
 *   <li>darunter gleiten "Das Licht der Unsterblichkeit" und wie lange es nun ruht herein.</li>
 * </ol>
 */
public final class ImmortalOverlay {
    public static final Identifier LAYER = KingdomsAtWar.id("hero_immortal");
    private static final float DURATION = 4.4f;
    private static final float FADE = 0.9f;
    private static final float EMBLEM_SCALE = 1.5f;
    private static final int[] COLORS = {0xFFFFE9A6, 0xFFFFFFFF, 0xFFA8E4FF, 0xFFFFB8E8, 0xFFFFD27A};

    private static long start = -1L;
    private static int minutes = HeroLevel.IMMORTAL_MINUTES;
    private static final List<Mote> MOTES = new ArrayList<>();

    /** Ein Lichtfunke, der vom unteren Rand aufsteigt. */
    private record Mote(float x, float speed, float delay, float life, float size, float sway, int color) {}

    private ImmortalOverlay() {}

    static void onPayload(HeroImmortalPayload payload, IPayloadContext context) {
        start(payload.minutes());
    }

    /** Startet die Szene (auch für Vorführungen); das Licht ruht danach {@code restMinutes} Minuten. */
    public static void start(int restMinutes) {
        minutes = restMinutes;
        start = Util.getMillis();
        MOTES.clear();
        RandomSource random = RandomSource.create(start);
        for (int i = 0; i < 84; i++) {
            MOTES.add(new Mote(random.nextFloat(), 45.0f + random.nextFloat() * 115.0f, random.nextFloat() * 1.7f, 1.5f + random.nextFloat() * 1.5f,
                    4.0f + random.nextFloat() * 9.0f, random.nextFloat() * Mth.TWO_PI, COLORS[random.nextInt(COLORS.length)]));
        }
    }

    /** Läuft die Szene gerade? */
    public static boolean active() {
        return start >= 0L && (Util.getMillis() - start) / 1000.0f < DURATION;
    }

    public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        if (start < 0L) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        float t = (Util.getMillis() - start) / 1000.0f;
        if (t >= DURATION || mc.player == null) {
            start = -1L;
            return;
        }
        if (mc.gui.hud.isHidden()) {
            return;
        }
        Font font = mc.font;
        int w = g.guiWidth();
        int h = g.guiHeight();
        float cx = w / 2.0f;
        float cy = Math.max(44.0f, h * 0.26f);
        float out = 1.0f - HeroGui.span(t, DURATION - FADE, DURATION);
        int prism = HeroGui.rankColor(HeroLevel.Rank.IMMORTAL);

        // der Bildschirm wird weiß-golden und klärt sich; warmes Licht vom unteren Rand und von den Seiten
        float flash = 1.0f - HeroGui.easeOutCubic(HeroGui.span(t, 0.0f, 1.1f));
        if (flash > 0.02f) {
            g.fill(0, 0, w, h, ARGB.color(Math.round(215 * flash), 0xFFF6DC));
        }
        float warm = HeroGui.span(t, 0.0f, 0.25f) * out;
        HeroGui.lightRect(g, HeroGui.GLOW, cx, h + 8.0f, w * 1.6f, h * 1.1f, 0xFFFFC870, 0.5f * warm);
        HeroGui.lightRect(g, HeroGui.GLOW, -12.0f, h * 0.5f, w * 0.34f, h * 1.4f, prism, 0.24f * warm);
        HeroGui.lightRect(g, HeroGui.GLOW, w + 12.0f, h * 0.5f, w * 0.34f, h * 1.4f, prism, 0.24f * warm);

        // drei Lichtringe laufen nach außen
        ring(g, cx, cy, t, 0.04f, 1.0f, 540.0f, 0xFFFFFFFF);
        ring(g, cx, cy, t, 0.3f, 1.4f, 460.0f, 0xFFFFE27A);
        ring(g, cx, cy, t, 0.6f, 1.85f, 400.0f, prism);

        // das Abzeichen des Unsterblichen tritt aus dem Licht, Strahlenkränze in seinen schillernden Farben
        float appear = HeroGui.span(t, 0.12f, 0.75f);
        float scale = EMBLEM_SCALE * (0.3f + 0.7f * HeroGui.easeOutBack(appear));
        float alpha = HeroGui.span(t, 0.12f, 0.35f) * out;
        float ey = cy - 10.0f * (1.0f - out);
        HeroGui.light(g, HeroGui.RAYS, cx, ey, 190.0f * scale, t * 0.25f, prism, 0.45f * alpha);
        HeroGui.light(g, HeroGui.RAYS, cx, ey, 150.0f * scale, -t * 0.4f, 0xFFFFF6DC, 0.3f * alpha);
        HeroGui.emblem(g, font, HeroLevel.Rank.IMMORTAL, HeroLevel.MAX_LEVEL, cx, ey, scale, alpha, 1.0f, true);
        if (t > 0.7f && t < 1.15f) {
            HeroGui.light(g, HeroGui.GLOW, cx, ey, 100.0f * scale, 0.0f, 0xFFFFFFFF, 0.55f * (1.0f - HeroGui.span(t, 0.7f, 1.15f)));
        }

        // Lichtfunken steigen auf, schwanken sacht und funkeln
        for (Mote mote : MOTES) {
            float age = t - mote.delay();
            if (age <= 0.0f || age > mote.life()) {
                continue;
            }
            float k = age / mote.life();
            float x = mote.x() * w + Mth.sin(age * 2.2f + mote.sway()) * 9.0f;
            float y = h + 6.0f - mote.speed() * age - 18.0f * age * age;
            float twinkle = 0.65f + 0.35f * Mth.sin(age * 18.0f + mote.sway() * 5.0f);
            HeroGui.light(g, HeroGui.SPARKLE, x, y, mote.size() * (1.0f - 0.35f * k), age * 1.5f, mote.color(), Mth.sin(k * Mth.PI) * twinkle * out);
        }

        // darunter: "Das Licht der Unsterblichkeit" und wie lange es nun ruht
        float textTop = ey + 24.0f * EMBLEM_SCALE + 10.0f;
        float in1 = HeroGui.easeOutCubic(HeroGui.span(t, 0.5f, 0.95f));
        if (in1 > 0.02f) {
            Component title = Component.translatable("hero.kingdomsatwar.immortal.title");
            HeroGui.lightRect(g, HeroGui.GLOW, cx, textTop + 7.0f, font.width(title) * 2.6f + 70.0f, 44.0f, prism, 0.38f * in1 * out);
            HeroGui.centered(g, font, title, cx, textTop + 8.0f * (1.0f - in1), ARGB.color(Math.round(255 * in1 * out), 0xFFF6DC), 1.8f);
        }
        float in2 = HeroGui.easeOutCubic(HeroGui.span(t, 0.95f, 1.35f));
        if (in2 > 0.02f) {
            Component line = Component.translatable("hero.kingdomsatwar.immortal.subtitle", minutes);
            HeroGui.centered(g, font, line, cx, textTop + 22.0f + 6.0f * (1.0f - in2), ARGB.color(Math.round(255 * in2 * out), 0xEDE3C8), 1.0f);
        }
    }

    /** Ein Lichtring, der zwischen {@code from} und {@code to} Sekunden auf {@code size} anwächst und verblasst. */
    private static void ring(GuiGraphicsExtractor g, float cx, float cy, float t, float from, float to, float size, int color) {
        float p = HeroGui.span(t, from, to);
        if (p <= 0.0f || p >= 1.0f) {
            return;
        }
        HeroGui.light(g, HeroGui.RING, cx, cy, 30.0f + size * HeroGui.easeOutCubic(p), 0.0f, color, 0.9f * (1.0f - p) * (1.0f - p));
    }
}
