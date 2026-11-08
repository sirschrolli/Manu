package io.github.sirschrolli.kingdomsatwar.client.hero;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

/**
 * Was Charakterfenster, Anzeige und Aufstiegs-Szene gemeinsam zeichnen: die Grafiken (aus tools/hero_gui_textures.py),
 * Farben des dunkelgoldenen Stils, das Rangabzeichen mit Zahl und Licht dahinter und ein paar Bewegungskurven.
 */
public final class HeroGui {
    public static final Identifier PANEL = sprite("panel");
    public static final Identifier INSET = sprite("inset");
    public static final Identifier CARD = sprite("card");
    public static final Identifier CARD_HOVER = sprite("card_hover");
    public static final Identifier BUTTON = sprite("button");
    public static final Identifier BUTTON_HIGHLIGHTED = sprite("button_highlighted");
    public static final Identifier BUTTON_DISABLED = sprite("button_disabled");
    public static final Identifier PLUS = sprite("plus");
    public static final Identifier PLUS_HIGHLIGHTED = sprite("plus_highlighted");
    public static final Identifier PLUS_DISABLED = sprite("plus_disabled");
    public static final Identifier RAYS = sprite("rays");
    public static final Identifier GLOW = sprite("glow");
    public static final Identifier RING = sprite("ring");
    public static final Identifier SPARKLE = sprite("sparkle");

    /** Schrift: helles Pergament, gedämpft, Gold, Grün für Gewinne. */
    public static final int TEXT = 0xFFEDE3C8;
    public static final int MUTED = 0xFF8F86A3;
    public static final int GOLD = 0xFFF2C14E;
    public static final int GOLD_DARK = 0xFFB5832C;
    public static final int GOOD = 0xFF8FE07A;
    public static final int SHADOW = 0xFF0B0910;

    /** Größe der Abzeichen (Pixel der Grafik) und Radius der dunklen Scheibe für die Zahl. */
    public static final int EMBLEM = 48;
    public static final int BADGE = 15;

    private HeroGui() {}

    private static Identifier sprite(String name) {
        return KingdomsAtWar.id("hero/" + name);
    }

    public static Identifier emblem(HeroLevel.Rank rank) {
        return sprite("emblem/" + rank.getSerializedName());
    }

    public static Identifier badge(HeroLevel.Rank rank) {
        return sprite("badge/" + rank.getSerializedName());
    }

    /** Sekunden seit Spielstart (für gleichmäßige Bewegungen). */
    public static float seconds() {
        return (Util.getMillis() % 3_600_000L) / 1000.0f;
    }

    /**
     * Die Farbe eines Rangs; die Legende schimmert langsam zwischen Feuergold und Rosé.
     */
    public static int rankColor(HeroLevel.Rank rank) {
        if (rank != HeroLevel.Rank.LEGEND) {
            return rank.color();
        }
        float t = 0.5f + 0.5f * Mth.sin(seconds() * 2.2f);
        return ARGB.srgbLerp(t, 0xFFFF9A3C, 0xFFFFD27A);
    }

    // ------------------------------------------------------------------------------------------------ Licht

    /**
     * Ein Lichtbild (Strahlen, Glühen, Ring, Funke) additiv, mittig bei (cx, cy), gedreht und in der Farbe {@code rgb} mit
     * der Stärke {@code intensity} (0 bis 1). Additiv heißt: Es hellt auf und verdeckt nichts.
     */
    public static void light(GuiGraphicsExtractor g, Identifier sprite, float cx, float cy, float size, float rotation, int rgb, float intensity) {
        if (intensity <= 0.003f || size < 1.0f) {
            return;
        }
        float k = Mth.clamp(intensity, 0.0f, 1.0f);
        int color = ARGB.color(255, Math.round(ARGB.red(rgb) * k), Math.round(ARGB.green(rgb) * k), Math.round(ARGB.blue(rgb) * k));
        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        if (rotation != 0.0f) {
            g.pose().rotate(rotation);
        }
        float scale = size / 64.0f;
        g.pose().scale(scale, scale);
        g.blitSprite(RenderPipelines.GUI_NAUSEA_OVERLAY, sprite, -32, -32, 64, 64, color);
        g.pose().popMatrix();
    }

    /** Ein Lichtbild verzerrt auf {@code w} × {@code h} Pixel (z. B. eine Lichtsäule aus dem Glühen). */
    public static void lightRect(GuiGraphicsExtractor g, Identifier sprite, float cx, float cy, float w, float h, int rgb, float intensity) {
        if (intensity <= 0.003f || w < 1.0f || h < 1.0f) {
            return;
        }
        float k = Mth.clamp(intensity, 0.0f, 1.0f);
        int color = ARGB.color(255, Math.round(ARGB.red(rgb) * k), Math.round(ARGB.green(rgb) * k), Math.round(ARGB.blue(rgb) * k));
        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        g.pose().scale(w / 64.0f, h / 64.0f);
        g.blitSprite(RenderPipelines.GUI_NAUSEA_OVERLAY, sprite, -32, -32, 64, 64, color);
        g.pose().popMatrix();
    }

    // ------------------------------------------------------------------------------------------------ Abzeichen

    /**
     * Das Rangabzeichen mit der Heldenstufe in der Mitte, mittig bei (cx, cy) in {@code scale}-facher Größe (48 Pixel bei
     * 1), dahinter langsam drehende Strahlen und ein Glühen in der Farbe des Rangs. {@code alpha} blendet alles ein und
     * aus, {@code numberScale} lässt die Zahl hüpfen.
     */
    public static void emblem(GuiGraphicsExtractor g, Font font, HeroLevel.Rank rank, int level, float cx, float cy, float scale, float alpha,
            float numberScale, boolean light) {
        emblem(g, font, rank, level, cx, cy, scale, alpha, numberScale, light, 0xFFFFFFFF);
    }

    /** Wie oben, das Abzeichen in {@code tint} getönt (z. B. grau für noch nicht erreichte Ränge). */
    public static void emblem(GuiGraphicsExtractor g, Font font, HeroLevel.Rank rank, int level, float cx, float cy, float scale, float alpha,
            float numberScale, boolean light, int tint) {
        if (alpha <= 0.01f) {
            return;
        }
        int color = tint == 0xFFFFFFFF ? rankColor(rank) : ARGB.multiply(rankColor(rank), tint);
        if (light) {
            float time = seconds();
            float pulse = 0.85f + 0.15f * Mth.sin(time * 2.4f);
            light(g, GLOW, cx, cy, 70.0f * scale, 0.0f, color, 0.55f * alpha * pulse);
            light(g, RAYS, cx, cy, 92.0f * scale, time * 0.35f, color, 0.42f * alpha);
            light(g, RAYS, cx, cy, 74.0f * scale, -time * 0.22f, 0xFFFFFFFF, 0.16f * alpha);
        }
        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        g.pose().scale(scale, scale);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, emblem(rank), -EMBLEM / 2, -EMBLEM / 2, EMBLEM, EMBLEM, ARGB.multiplyAlpha(tint, alpha));
        // die Zahl in der Scheibe (die Scheibe sitzt einen Pixel unter der Mitte)
        String text = String.valueOf(level);
        float fit = Math.min(1.0f, 19.0f / Math.max(1, font.width(text)));
        float s = (level >= 100 ? 1.0f : 1.35f) * fit * numberScale;
        g.pose().translate(0.0f, 1.0f);
        g.pose().scale(s, s);
        int a = Math.round(255 * alpha);
        int w = font.width(text);
        g.text(font, text, -w / 2 + 1, -3, ARGB.color(a, 11, 9, 16), false);
        g.text(font, text, -w / 2, -4, ARGB.color(a, ARGB.red(color), ARGB.green(color), ARGB.blue(color)), false);
        g.pose().popMatrix();
    }

    // ------------------------------------------------------------------------------------------------ Text

    /** Text mit dunklem Schatten, mittig bei x. */
    public static void centered(GuiGraphicsExtractor g, Font font, Component text, float x, float y, int color, float scale) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        int w = font.width(text);
        int alpha = ARGB.alpha(color);
        g.text(font, text, -w / 2 + 1, 1, ARGB.color(alpha, 11, 9, 16), false);
        g.text(font, text, -w / 2, 0, color, false);
        g.pose().popMatrix();
    }

    /** Text mit dunklem Schatten links bündig. */
    public static void text(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int color) {
        g.text(font, text, x + 1, y + 1, ARGB.color(ARGB.alpha(color), 11, 9, 16), false);
        g.text(font, text, x, y, color, false);
    }

    /** Große Zahlen mit Tausenderpunkten (im Deutschen) oder -kommas. */
    public static String number(long value) {
        String text = String.format(Locale.ROOT, "%,d", value);
        String language = Minecraft.getInstance().options.languageCode;
        return language.startsWith("de") ? text.replace(',', '.') : text;
    }

    /** Eine Zahl mit höchstens einer Nachkommastelle, im Deutschen mit Komma. */
    public static String decimal(double value) {
        String text = Math.abs(value - Math.round(value)) < 0.05 ? String.valueOf(Math.round(value)) : String.format(Locale.ROOT, "%.1f", value);
        String language = Minecraft.getInstance().options.languageCode;
        return language.startsWith("de") ? text.replace('.', ',') : text;
    }

    // ------------------------------------------------------------------------------------------------ Leiste

    /**
     * Eine goldene Leiste in einem vertieften Feld: {@code progress} gefüllt, ein heller Schimmer wandert hindurch, und
     * frisch Dazugekommenes ({@code fresh} bis {@code progress}) leuchtet weiß auf.
     */
    public static void bar(GuiGraphicsExtractor g, int x, int y, int w, int h, float progress, float fresh, float flash, int color) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, INSET, x, y, w, h);
        int inner = w - 2;
        int fill = Math.round(inner * Mth.clamp(progress, 0.0f, 1.0f));
        if (fill <= 0) {
            return;
        }
        int top = ARGB.srgbLerp(0.45f, color, 0xFFFFFFFF);
        int bottom = ARGB.srgbLerp(0.45f, color, 0xFF000000);
        g.fillGradient(x + 1, y + 1, x + 1 + fill, y + h - 1, top, bottom);
        g.fill(x + 1, y + 1, x + 1 + fill, y + 2, ARGB.srgbLerp(0.7f, color, 0xFFFFFFFF));
        // wandernder Schimmer
        float t = (seconds() * 0.55f) % 1.0f;
        int sx = x + 1 + Math.round((inner + 24) * t) - 12;
        for (int i = 0; i < 10; i++) {
            int px = sx + i;
            if (px >= x + 1 && px < x + 1 + fill) {
                float k = 1.0f - Math.abs(i - 4.5f) / 5.0f;
                g.fill(px, y + 1, px + 1, y + h - 1, ARGB.color(Math.round(90 * k), 255, 255, 255));
            }
        }
        // frisch dazugekommen: leuchtet kurz
        if (flash > 0.01f && fresh < progress) {
            int from = x + 1 + Math.round(inner * Mth.clamp(fresh, 0.0f, 1.0f));
            g.fill(from, y + 1, x + 1 + fill, y + h - 1, ARGB.color(Math.round(170 * flash), 255, 250, 225));
        }
        // die Spitze der Füllung glüht
        g.fill(x + fill, y + 1, x + 1 + fill, y + h - 1, ARGB.color(200, 255, 255, 235));
    }

    // ------------------------------------------------------------------------------------------------ Bewegung

    public static float easeOutCubic(float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        float u = 1.0f - t;
        return 1.0f - u * u * u;
    }

    public static float easeOutBack(float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        float c1 = 1.70158f;
        float c3 = c1 + 1.0f;
        float u = t - 1.0f;
        return 1.0f + c3 * u * u * u + c1 * u * u;
    }

    public static float easeInOut(float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    /** 0 vor {@code from}, 1 nach {@code to}, dazwischen gleichmäßig. */
    public static float span(float t, float from, float to) {
        return Mth.clamp((t - from) / (to - from), 0.0f, 1.0f);
    }
}
