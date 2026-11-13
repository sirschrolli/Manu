package io.github.sirschrolli.kingdomsatwar.client.hero;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import io.github.sirschrolli.kingdomsatwar.network.HeroLevelUpPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
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
 * Die Aufstiegs-Szene auf dem Bildschirm des Helden (gut vier Sekunden, im oberen Drittel, das Fadenkreuz bleibt frei):
 *
 * <ol>
 *   <li>ein Lichtblitz und eine Druckwelle in der Farbe des Rangs, die nach außen läuft,</li>
 *   <li>das Rangabzeichen springt herein (mit Überschwingen), dahinter drehen sich zwei Strahlenkränze gegeneinander,</li>
 *   <li>die Zahl zählt bis zur neuen Stufe hoch und hüpft, Funken sprühen nach allen Seiten und fallen,</li>
 *   <li>darunter gleiten "Heldenstufe N", der Rang und die neuen Talentpunkte herein;</li>
 *   <li>bei einem neuen Rang: eine zweite Welle, doppelt so viele Funken, "Neuer Rang", und als Legende die Belohnung.</li>
 * </ol>
 */
public final class HeroLevelUpOverlay {
    public static final Identifier LAYER = KingdomsAtWar.id("hero_level_up");
    private static final float DURATION = 4.6f;
    private static final float FADE = 0.7f;

    private static long start = -1L;
    private static int from;
    private static int to;
    private static HeroLevel.Rank rank = HeroLevel.Rank.SQUIRE;
    private static boolean rankUp;
    private static final List<Spark> SPARKS = new ArrayList<>();

    private record Spark(float angle, float speed, float life, float size, float spin, boolean white) {}

    private HeroLevelUpOverlay() {}

    static void onPayload(HeroLevelUpPayload payload, IPayloadContext context) {
        start(payload.from(), payload.to());
    }

    /** Startet die Szene (auch für Vorführungen). */
    public static void start(int fromLevel, int toLevel) {
        from = Math.max(1, fromLevel);
        to = Math.max(from, toLevel);
        rank = HeroLevel.Rank.of(to);
        rankUp = HeroLevel.Rank.of(from) != rank;
        start = Util.getMillis();
        SPARKS.clear();
        RandomSource random = RandomSource.create(start);
        int count = rankUp ? 90 : 48;
        for (int i = 0; i < count; i++) {
            float angle = random.nextFloat() * Mth.TWO_PI;
            SPARKS.add(new Spark(angle, 55.0f + random.nextFloat() * (rankUp ? 150.0f : 110.0f), 0.8f + random.nextFloat() * 1.1f,
                    6.0f + random.nextFloat() * 8.0f, (random.nextFloat() - 0.5f) * 6.0f, random.nextFloat() < 0.35f));
        }
        HeroHud.ping();
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
        float cx = g.guiWidth() / 2.0f;
        float cy = Math.max(46.0f, g.guiHeight() * 0.3f);
        float out = 1.0f - HeroGui.span(t, DURATION - FADE, DURATION);
        int color = HeroGui.rankColor(rank);
        float base = rankUp ? 1.6f : 1.4f;

        // Lichtblitz und Druckwellen
        float flash = 1.0f - HeroGui.span(t, 0.0f, 0.4f);
        HeroGui.light(g, HeroGui.GLOW, cx, cy, 120.0f + 220.0f * HeroGui.easeOutCubic(HeroGui.span(t, 0.0f, 0.4f)), 0.0f, color, 0.85f * flash);
        wave(g, cx, cy, t, 0.06f, 0.95f, 300.0f, color);
        if (rankUp) {
            wave(g, cx, cy, t, 0.4f, 1.45f, 420.0f, 0xFFFFFFFF);
            wave(g, cx, cy, t, 0.75f, 1.9f, 360.0f, color);
        }

        // das Abzeichen springt herein, die Zahl zählt hoch und hüpft
        float appear = HeroGui.span(t, 0.05f, 0.65f);
        float scale = base * (0.25f + 0.75f * HeroGui.easeOutBack(appear)) * (1.0f - 0.12f * (1.0f - out));
        float alpha = HeroGui.span(t, 0.05f, 0.25f) * out;
        float count = HeroGui.easeOutCubic(HeroGui.span(t, 0.35f, 1.0f));
        int shown = Math.round(Mth.lerp(count, (float) from, (float) to));
        float pop = t > 1.0f ? 1.0f + 0.45f * (1.0f - HeroGui.easeOutCubic(HeroGui.span(t, 1.0f, 1.35f))) : 1.0f;
        float lift = -12.0f * (1.0f - out);
        HeroGui.emblem(g, font, rank, shown, cx, cy + lift, scale, alpha, pop, true);
        if (t > 1.0f && t < 1.45f) {
            HeroGui.light(g, HeroGui.GLOW, cx, cy + lift, 90.0f * scale, 0.0f, 0xFFFFFFFF, 0.5f * (1.0f - HeroGui.span(t, 1.0f, 1.45f)));
        }

        // Funken: sprühen nach außen, werden langsamer, fallen und funkeln
        for (Spark spark : SPARKS) {
            float age = t - 0.12f;
            if (age <= 0.0f || age > spark.life()) {
                continue;
            }
            float k = age / spark.life();
            float distance = spark.speed() * (1.0f - (float) Math.exp(-age * 2.4f)) / 2.4f * 2.0f;
            float sx = cx + Mth.cos(spark.angle()) * distance;
            float sy = cy + Mth.sin(spark.angle()) * distance + 38.0f * age * age;
            float twinkle = 0.6f + 0.4f * Mth.sin(age * 22.0f + spark.angle() * 7.0f);
            HeroGui.light(g, HeroGui.SPARKLE, sx, sy, spark.size() * (1.0f - 0.5f * k), spark.spin() * age,
                    spark.white() ? 0xFFFFFFFF : color, (1.0f - k) * twinkle * out);
        }

        // Texte darunter
        float textTop = cy + 24.0f * base + 12.0f;
        float in1 = HeroGui.easeOutCubic(HeroGui.span(t, 0.45f, 0.85f));
        if (in1 > 0.0f) {
            Component title = Component.translatable("hero.kingdomsatwar.level_up.title", to);
            HeroGui.centered(g, font, title, cx, textTop + 8.0f * (1.0f - in1), ARGB.color(Math.round(255 * in1 * out), 0xFFE7A0), 2.0f);
        }
        float in2 = HeroGui.easeOutBack(HeroGui.span(t, 0.8f, 1.2f));
        if (in2 > 0.0f) {
            Component line = rankUp
                    ? Component.translatable("hero.kingdomsatwar.level_up.rank", Component.translatable(rank.translationKey()))
                    : Component.translatable(rank.translationKey());
            float s = rankUp ? 1.3f : 1.0f;
            HeroGui.centered(g, font, line, cx, textTop + 24.0f, ARGB.color(Math.round(255 * Mth.clamp(in2, 0.0f, 1.0f) * out), color & 0xFFFFFF),
                    s * (0.85f + 0.15f * in2));
        }
        float in3 = HeroGui.easeOutCubic(HeroGui.span(t, 1.05f, 1.45f));
        int points = HeroLevel.pointsFor(to) - HeroLevel.pointsFor(from);
        if (in3 > 0.0f && points > 0) {
            Component line = Component.translatable(points == 1 ? "hero.kingdomsatwar.level_up.point" : "hero.kingdomsatwar.level_up.points", points,
                    Component.keybind("key.kingdomsatwar.character").copy().withStyle(ChatFormatting.GOLD));
            HeroGui.centered(g, font, line, cx, textTop + (rankUp ? 40.0f : 38.0f) + 6.0f * (1.0f - in3),
                    ARGB.color(Math.round(255 * in3 * out), 0xEDE3C8), 1.0f);
        }
        // die Heldenkraft, die diese Stufe(n) mitbringen
        float inMight = HeroGui.easeOutCubic(HeroGui.span(t, 1.25f, 1.65f));
        Component might = HeroGui.might(from, to);
        if (inMight > 0.0f && !might.getString().isEmpty()) {
            Component line = Component.translatable("hero.kingdomsatwar.level_up.might", might);
            // (bringt die Stufe keinen Talentpunkt, rückt die Heldenkraft an seine Stelle)
            HeroGui.centered(g, font, line, cx, textTop + (points > 0 ? (rankUp ? 52.0f : 50.0f) : (rankUp ? 40.0f : 38.0f)) + 6.0f * (1.0f - inMight),
                    ARGB.color(Math.round(255 * inMight * out), 0xF2C14E), 1.0f);
        }
        boolean special = rankUp && (rank == HeroLevel.Rank.LEGEND || rank == HeroLevel.Rank.IMMORTAL);
        if (special) {
            float in4 = HeroGui.easeOutCubic(HeroGui.span(t, 1.5f, 1.95f));
            if (in4 > 0.0f) {
                boolean immortal = rank == HeroLevel.Rank.IMMORTAL;
                Component line = immortal ? Component.translatable("hero.kingdomsatwar.level_up.reward.immortal")
                        : Component.translatable("hero.kingdomsatwar.level_up.reward",
                                Component.keybind("key.kingdomsatwar.pat").copy().withStyle(ChatFormatting.GOLD));
                float y = textTop + 66.0f + 6.0f * (1.0f - in4);
                HeroGui.light(g, HeroGui.GLOW, cx, y + 4.0f, 150.0f, 0.0f, immortal ? 0xFFFFE27A : 0xFFB070FF, 0.35f * in4 * out);
                HeroGui.centered(g, font, line, cx, y, ARGB.color(Math.round(255 * in4 * out), immortal ? 0xFFF0C0 : 0xE0B8FF), 1.0f);
            }
        }
    }

    /** Eine Druckwelle: ein Lichtring, der zwischen {@code from} und {@code to} Sekunden auf {@code size} anwächst und verblasst. */
    private static void wave(GuiGraphicsExtractor g, float cx, float cy, float t, float from, float to, float size, int color) {
        float p = HeroGui.span(t, from, to);
        if (p <= 0.0f || p >= 1.0f) {
            return;
        }
        HeroGui.light(g, HeroGui.RING, cx, cy, 30.0f + size * HeroGui.easeOutCubic(p), 0.0f, color, 0.9f * (1.0f - p) * (1.0f - p));
    }
}
