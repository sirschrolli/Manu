package io.github.sirschrolli.kingdomsatwar.client.town;

import io.github.sirschrolli.kingdomsatwar.client.gui.UiMotion;
import io.github.sirschrolli.kingdomsatwar.economy.Coin;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Bausteine der Pergament-Fenster (Stadt, Handel, Bauplan, Feldherrentisch, Söldnertafel): Farben, Holzknöpfe, Karten,
 * Balken, Trennlinien und Münzbeträge – damit alle gleich aussehen.
 */
public final class Parchment {
    /** Schrift auf Pergament: dunkles Braun. */
    public static final int TEXT = 0xFF3B2611;
    public static final int MUTED = 0xFF7A6448;
    public static final int GOOD = 0xFF2E6B2E;
    public static final int BAD = 0xFFA83224;
    /** Gold auf Pergament (Ränge, Ausbildung). */
    public static final int GOLD = 0xFF8A5A10;
    /** Titel im blauen Band. */
    public static final int GOLD_TEXT = 0xFFF2CE6B;
    public static final int RIBBON_TEXT = 0xFFE9DDBF;
    public static final int LINE = 0xFFB89C69;
    public static final int LINE_LIGHT = 0xFFF3E6C4;
    public static final int CARD = 0x26FFF6DC;
    public static final int CARD_HOVER = 0x4DFFF3CF;
    public static final int CARD_BORDER = 0xFFC2A472;
    public static final int CARD_BORDER_HOVER = 0xFFAE8A52;

    private Parchment() {}

    /**
     * Ein Holzknopf: dunkler Rand (golden unter der Maus), Holz mit hellem Glanz oben und Schatten unten; ausgegraut, wenn
     * er nichts tun kann. Ein Bild ({@code icon}) steht links vor der Schrift; zu lange Schrift wird kleiner gesetzt
     * (bis 75 %) und notfalls gekürzt.
     */
    public static void woodButton(GuiGraphicsExtractor g, Font font, int x, int y, int w, int h, Component label, boolean active, boolean hover,
            @Nullable ItemStack icon) {
        woodButton(g, font, x, y, w, h, label, active, hover, icon, -1);
    }

    /** Wie oben; mit {@code price} ≥ 0 steht hinter der Schrift noch ein Betrag als kleine Münzen. */
    public static void woodButton(GuiGraphicsExtractor g, Font font, int x, int y, int w, int h, Component label, boolean active, boolean hover,
            @Nullable ItemStack icon, int price) {
        boolean hot = active && hover;
        // unter der Maus blendet der Knopf weich über (seit 1.12), statt umzuspringen
        float glow = UiMotion.hover(x, y, w, h, hot);
        g.fill(x, y, x + w, y + h, UiMotion.mix(0xFF2E1D10, 0xFFE0B04A, glow));
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, !active ? 0xFF8C826E : UiMotion.mix(0xFF74492A, 0xFF8E6038, glow));
        g.fill(x + 1, y + 1, x + w - 1, y + 2, !active ? 0xFFA49A86 : UiMotion.mix(0xFFA4744A, 0xFFC09060, glow));
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, !active ? 0xFF6E6656 : 0xFF4A2E18);
        int color = !active ? 0xFFE6DECB : UiMotion.mix(0xFFF3E6C4, 0xFFFFF4D6, glow);
        int iconSize = icon == null || icon.isEmpty() ? 0 : h >= 16 ? 10 : 8;
        int gap = iconSize > 0 ? 3 : 0;
        int coinsW = price >= 0 ? 4 + smallPriceWidth(font, price) : 0;
        int room = w - (w < 24 ? 2 : 6) - iconSize - gap - coinsW;
        int tw = font.width(label);
        float scale = tw <= room ? 1.0f : Math.max(0.75f, room / (float) tw);
        Component shown = tw * scale <= room ? label : Component.literal(ellipsize(font, label.getString(), Math.round(room / scale)))
                .withStyle(label.getStyle());
        float textW = Math.min(room, font.width(shown) * scale);
        float total = iconSize + gap + textW + coinsW;
        float sx = x + (w - total) / 2.0f;
        if (iconSize > 0) {
            item(g, icon, Math.round(sx), y + (h - iconSize) / 2, iconSize / 16.0f);
        }
        float tx = sx + iconSize + gap;
        float ty = y + (h - 8 * scale) / 2.0f;
        g.pose().pushMatrix();
        g.pose().translate(tx, ty);
        g.pose().scale(scale, scale);
        if (active) {
            g.text(font, shown, 1, 1, 0xFF2A1A0C, false);
        }
        g.text(font, shown, 0, 0, color, false);
        g.pose().popMatrix();
        if (price >= 0) {
            smallPrice(g, font, Math.round(tx + textW + 4), y + (h - 8) / 2, price, color, active);
        }
    }

    /** Ein Betrag als winzige Münzen (8 Pixel) mit Anzahl – für Knöpfe. */
    private static void smallPrice(GuiGraphicsExtractor g, Font font, int x, int y, int amount, int color, boolean shadow) {
        int cx = x;
        int rest = Math.max(0, amount);
        boolean any = false;
        for (int i = Coin.values().length - 1; i >= 0; i--) {
            Coin coin = Coin.values()[i];
            int n = rest / coin.value();
            rest -= n * coin.value();
            if (n > 0 || (!any && i == 0)) {
                item(g, new ItemStack(coin.item()), cx, y, 0.5f);
                String s = String.valueOf(n);
                if (shadow) {
                    g.text(font, s, cx + 10, y + 1, 0xFF2A1A0C, false);
                }
                g.text(font, s, cx + 9, y, color, false);
                cx += 9 + font.width(s) + 3;
                any = true;
            }
        }
    }

    private static int smallPriceWidth(Font font, int amount) {
        int w = 0;
        int rest = Math.max(0, amount);
        boolean any = false;
        for (int i = Coin.values().length - 1; i >= 0; i--) {
            Coin coin = Coin.values()[i];
            int n = rest / coin.value();
            rest -= n * coin.value();
            if (n > 0 || (!any && i == 0)) {
                w += 9 + font.width(String.valueOf(n)) + 3;
                any = true;
            }
        }
        return Math.max(0, w - 3);
    }

    /** Ein leicht aufgehelltes Feld mit dünnem Rand (unter der Maus heller, ausgewählt mit Goldrand). */
    public static void card(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean hover, boolean selected) {
        float glow = UiMotion.hover(x, y, w, h, hover);
        g.fill(x, y, x + w, y + h, selected ? 0x59FFF1C4 : UiMotion.mix(CARD, CARD_HOVER, glow));
        if (selected) {
            g.outline(x, y, w, h, 0xFFB8862A);
            g.outline(x + 1, y + 1, w - 2, h - 2, 0x66F7DC80);
        } else {
            g.outline(x, y, w, h, UiMotion.mix(CARD_BORDER, CARD_BORDER_HOVER, glow));
        }
    }

    /** Fortschrittsbalken (dunkle Rinne, farbige Füllung mit hellem Glanz oben). */
    public static void bar(GuiGraphicsExtractor g, int x, int y, int w, int h, float fraction, int color) {
        g.fill(x, y, x + w, y + h, 0xFF8C7650);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFC9B48A);
        int fw = Math.round((w - 2) * Mth.clamp(fraction, 0.0f, 1.0f));
        if (fw > 0) {
            g.fill(x + 1, y + 1, x + 1 + fw, y + h - 1, color);
            if (h > 3) {
                g.fill(x + 1, y + 1, x + 1 + fw, y + 2, brighten(color));
            }
        }
    }

    /** Waagerechte Trennlinie (braun mit hellem Saum darunter). */
    public static void hLine(GuiGraphicsExtractor g, int x, int y, int w) {
        g.fill(x, y, x + w, y + 1, LINE);
        g.fill(x, y + 1, x + w, y + 2, 0x80F3E6C4);
    }

    /** Senkrechte Trennlinie (braun mit hellem Saum rechts). */
    public static void vLine(GuiGraphicsExtractor g, int x, int y, int h) {
        g.fill(x, y, x + 1, y + h, LINE);
        g.fill(x + 1, y, x + 2, y + h, 0x80F3E6C4);
    }

    /** Ein Gegenstand, verkleinert oder vergrößert ({@code scale} 1 = 16 Pixel). */
    public static void item(GuiGraphicsExtractor g, ItemStack stack, int x, int y, float scale) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.item(stack, 0, 0);
        g.pose().popMatrix();
    }

    /** Eine Zeile, die höchstens {@code width} breit wird: sonst etwas kleiner (bis 80 %), notfalls gekürzt („…“). */
    public static void fitted(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int width, int color) {
        int w = font.width(text);
        if (w <= width) {
            g.text(font, text, x, y, color, false);
            return;
        }
        float scale = Math.max(0.8f, width / (float) w);
        g.pose().pushMatrix();
        g.pose().translate(x, y + 4.0f * (1.0f - scale));
        g.pose().scale(scale, scale);
        if (w * scale <= width) {
            g.text(font, text, 0, 0, color, false);
        } else {
            g.text(font, Component.literal(ellipsize(font, text.getString(), Math.round(width / scale))).withStyle(text.getStyle()), 0, 0, color,
                    false);
        }
        g.pose().popMatrix();
    }

    /** Kürzt einen Text auf {@code maxWidth} Pixel – mit „…“, wenn etwas fehlt. */
    public static String ellipsize(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        return font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width("…"))) + "…";
    }

    /** Ein Betrag als kleine Münzen mit Anzahl (z. B. Gold 1, Silber 4, Bronze 5); gibt die Breite zurück. */
    public static int price(GuiGraphicsExtractor g, Font font, int x, int y, int amount, int color) {
        int cx = x;
        int rest = Math.max(0, amount);
        boolean any = false;
        for (int i = Coin.values().length - 1; i >= 0; i--) {
            Coin coin = Coin.values()[i];
            int n = rest / coin.value();
            rest -= n * coin.value();
            if (n > 0 || (!any && i == 0)) {
                item(g, new ItemStack(coin.item()), cx, y, 0.625f);
                String s = String.valueOf(n);
                g.text(font, s, cx + 11, y + 2, color, false);
                cx += 11 + font.width(s) + 4;
                any = true;
            }
        }
        return Math.max(0, cx - x - 4);
    }

    /** Wie breit {@link #price} wird. */
    public static int priceWidth(Font font, int amount) {
        int w = 0;
        int rest = Math.max(0, amount);
        boolean any = false;
        for (int i = Coin.values().length - 1; i >= 0; i--) {
            Coin coin = Coin.values()[i];
            int n = rest / coin.value();
            rest -= n * coin.value();
            if (n > 0 || (!any && i == 0)) {
                w += 11 + font.width(String.valueOf(n)) + 4;
                any = true;
            }
        }
        return Math.max(0, w - 4);
    }

    private static int brighten(int color) {
        int r = Math.min(255, ((color >> 16) & 0xFF) + 50);
        int gr = Math.min(255, ((color >> 8) & 0xFF) + 50);
        int b = Math.min(255, (color & 0xFF) + 50);
        return (color & 0xFF000000) | (r << 16) | (gr << 8) | b;
    }
}
