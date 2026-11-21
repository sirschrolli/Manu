package io.github.sirschrolli.kingdomsatwar.client.story;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.story.Chapter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * Titelkarte beim Beginn eines Kapitels: „KAPITEL III“, darunter groß der Titel und – während der Erzähler spricht –
 * seine Worte, auf einem dunklen Band mit Zierlinien. Blendet sich weich ein und aus und bleibt so lange stehen, wie der
 * Erzähler spricht. Ohne Kapitel (z. B. nach dem letzten Drachen oder bei der Krönung) spricht nur der Erzähler, sein
 * Text steht dann unten wie ein Untertitel. Die Karte wartet, bis kein Fenster (z. B. das Intro) mehr offen ist und die
 * Anzeige sichtbar ist; mehrere Karten kommen nacheinander.
 */
public final class ChapterCardOverlay {
    public static final Identifier LAYER = KingdomsAtWar.id("chapter_card");
    private static final Identifier ORNAMENT = KingdomsAtWar.id("textures/gui/chronicle.png");
    private static final int FADE_IN = 25;
    private static final int HOLD = 150;
    private static final int FADE_OUT = 35;
    /** So lange nach dem Schließen eines Fensters warten, bevor eine Karte erscheint. */
    private static final int SETTLE = 30;
    /** Pause zwischen zwei Zeilen des Erzählers (Ticks). */
    private static final int GAP = 10;

    /** Eine Karte (oder nur der Erzähler, wenn {@code chapter} fehlt) mit den Zeilen des Erzählers. */
    private record Card(@Nullable Chapter chapter, @Nullable DragonType fallen, List<String> lines) {}

    private static final Deque<Card> QUEUE = new ArrayDeque<>();
    private static @Nullable Card current;
    private static int age;
    private static int settle;
    private static int total;
    /** Beginn jeder Zeile des Erzählers (Ticks seit Beginn der Karte) und Ende der letzten. */
    private static int[] lineStart = new int[0];
    private static int[] lineEnd = new int[0];
    private static int spoken;
    private static final List<SoundInstance> VOICES = new ArrayList<>();

    private ChapterCardOverlay() {}

    public static void queue(int chapter, int fallen, String narration) {
        DragonType element = fallen >= 0 && fallen < DragonType.values().length ? DragonType.values()[fallen] : null;
        List<String> lines = narration == null || narration.isBlank() ? List.of() : List.of(narration.split(","));
        if (chapter < 0 && lines.isEmpty()) {
            return;
        }
        QUEUE.add(new Card(chapter < 0 ? null : Chapter.byIndex(chapter), element, lines));
    }

    /** Alle wartenden Karten verwerfen (für die Screenshot-Automatik). */
    public static void clear() {
        QUEUE.clear();
        current = null;
        stopVoices();
    }

    public static boolean isShowing() {
        return current != null;
    }

    private static void stopVoices() {
        for (SoundInstance voice : VOICES) {
            Minecraft.getInstance().getSoundManager().stop(voice);
        }
        VOICES.clear();
    }

    /** Zeitplan der Karte: wann jede Zeile beginnt und endet, wie lange die Karte stehen bleibt. */
    private static void plan(Card card) {
        int n = card.lines().size();
        lineStart = new int[n];
        lineEnd = new int[n];
        int t = card.chapter() == null ? 8 : FADE_IN + 8;
        for (int i = 0; i < n; i++) {
            lineStart[i] = t;
            t += (int) Math.ceil(StoryNarration.duration(card.lines().get(i)) * 20.0);
            lineEnd[i] = t;
            t += GAP;
        }
        int voiceEnd = n == 0 ? 0 : lineEnd[n - 1] + 30;
        total = Math.max(card.chapter() == null ? 0 : FADE_IN + HOLD, voiceEnd) + FADE_OUT;
        spoken = 0;
    }

    /** Einmal pro Tick (vom Client-Takt). */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            QUEUE.clear();
            current = null;
            stopVoices();
            return;
        }
        boolean blocked = mc.gui.screen() != null || mc.gui.hud.isHidden();
        if (current == null) {
            if (blocked) {
                settle = SETTLE;
                return;
            }
            if (settle > 0) {
                settle--;
                return;
            }
            current = QUEUE.poll();
            age = 0;
            if (current != null) {
                plan(current);
                if (current.chapter() != null) {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BELL_BLOCK, 0.5f, 0.6f));
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_RESONATE, 0.7f, 0.5f));
                }
            }
            return;
        }
        // einmal begonnen, läuft der Erzähler weiter – auch wenn kurz ein Fenster offen ist
        if (!blocked || age < FADE_IN || spoken > 0) {
            age++;
        }
        while (spoken < lineStart.length && age >= lineStart[spoken]) {
            SoundInstance voice = StoryNarration.play(current.lines().get(spoken));
            if (voice != null) {
                VOICES.add(voice);
            }
            spoken++;
        }
        if (age >= total) {
            current = null;
            VOICES.clear();
            settle = 20;
        }
    }

    /** Die Zeile des Erzählers, die gerade zu sehen ist (Index) – auch kurz vor und nach dem Sprechen. */
    private static int shownLine(float t) {
        for (int i = lineStart.length - 1; i >= 0; i--) {
            if (t >= lineStart[i] - 6) {
                return i;
            }
        }
        return lineStart.length > 0 ? 0 : -1;
    }

    public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Card card = current;
        Minecraft mc = Minecraft.getInstance();
        if (card == null || mc.gui.hud.isHidden()) {
            return;
        }
        float t = age + delta.getGameTimeDeltaPartialTick(false);
        float alpha;
        if (t < FADE_IN) {
            alpha = t / FADE_IN;
        } else if (t < total - FADE_OUT) {
            alpha = 1.0f;
        } else {
            alpha = 1.0f - (t - (total - FADE_OUT)) / FADE_OUT;
        }
        alpha = Mth.clamp(alpha, 0.0f, 1.0f);
        if (alpha <= 0.01f) {
            return;
        }
        if (card.chapter() == null) {
            renderSubtitle(g, mc.font, card, t, alpha);
            return;
        }
        Font font = mc.font;
        int w = g.guiWidth();
        int cy = g.guiHeight() / 4 + 4;
        int a = (int) (alpha * 255);
        // Text unter dem Titel: die Worte des Erzählers (Zeile für Zeile, weich überblendet) oder die kurze Zeile
        int shown = shownLine(t);
        float lineAlpha = 1.0f;
        Component line;
        if (shown >= 0) {
            line = StoryNarration.text(card.lines().get(shown));
            if (shown + 1 < lineStart.length) {
                lineAlpha = Mth.clamp((lineStart[shown + 1] - 6 - t) / 6.0f, 0.0f, 1.0f);
            }
            if (shown > 0) {
                lineAlpha = Math.min(lineAlpha, Mth.clamp((t - (lineStart[shown] - 6)) / 6.0f, 0.0f, 1.0f));
            }
        } else if (card.fallen() != null) {
            line = Component.translatable("chapter.kingdomsatwar.card.fallen",
                    Component.translatable("lair.kingdomsatwar." + card.fallen().id() + ".dragon").withColor(card.fallen().color()));
        } else {
            line = Component.translatable(card.chapter().titleKey() + ".card");
        }
        List<FormattedCharSequence> rows = font.split(line.copy().withStyle(ChatFormatting.ITALIC), Math.min(360, w - 40));
        int count = Math.min(5, rows.size());
        int bottom = cy + 26 + count * 11;
        // dunkles Band
        int band = (int) (alpha * 150);
        g.fillGradient(0, cy - 30, w, cy - 6, ARGB.color(0, 6, 4, 10), ARGB.color(band, 6, 4, 10));
        g.fill(0, cy - 6, w, bottom, ARGB.color(band, 6, 4, 10));
        g.fillGradient(0, bottom, w, bottom + 24, ARGB.color(band, 6, 4, 10), ARGB.color(0, 6, 4, 10));

        // „KAPITEL III“ (gesperrt) zwischen zwei Zierlinien
        Component label = label(card.chapter());
        int lw = font.width(label);
        int gold = ARGB.color(a, 0xE8, 0xC2, 0x6A);
        g.text(font, label, w / 2 - lw / 2, cy - 20, gold, false);
        int lineW = 46;
        g.blit(RenderPipelines.GUI_TEXTURED, ORNAMENT, w / 2 - lw / 2 - 8 - lineW, cy - 18, 400.0f, 80.0f, lineW, 5, 512, 256, ARGB.white(alpha));
        g.blit(RenderPipelines.GUI_TEXTURED, ORNAMENT, w / 2 + lw / 2 + 8, cy - 18, 400.0f, 86.0f, lineW, 5, 512, 256, ARGB.white(alpha));

        // Titel, doppelt so groß
        Component title = Component.translatable(card.chapter().titleKey());
        g.pose().pushMatrix();
        g.pose().translate(w / 2.0f, cy - 4);
        g.pose().scale(2.0f, 2.0f);
        g.text(font, title, -font.width(title) / 2, 0, ARGB.color(a, 0xF6, 0xEC, 0xD2), true);
        g.pose().popMatrix();

        int textAlpha = (int) (alpha * lineAlpha * 255);
        if (textAlpha > 4) {
            for (int i = 0; i < count; i++) {
                FormattedCharSequence seq = rows.get(i);
                g.text(font, seq, w / 2 - font.width(seq) / 2, cy + 22 + i * 11, ARGB.color(textAlpha, 0xD8, 0xCC, 0xB8), true);
            }
        }
    }

    /** Nur der Erzähler: sein Text unten in der Mitte, wie ein Untertitel. */
    private static void renderSubtitle(GuiGraphicsExtractor g, Font font, Card card, float t, float alpha) {
        int shown = shownLine(t);
        if (shown < 0) {
            return;
        }
        float lineAlpha = alpha;
        if (shown + 1 < lineStart.length) {
            lineAlpha *= Mth.clamp((lineStart[shown + 1] - 6 - t) / 6.0f, 0.0f, 1.0f);
        }
        if (shown > 0) {
            lineAlpha *= Mth.clamp((t - (lineStart[shown] - 6)) / 6.0f, 0.0f, 1.0f);
        }
        if (lineAlpha <= 0.02f) {
            return;
        }
        int w = g.guiWidth();
        int h = g.guiHeight();
        List<FormattedCharSequence> rows = font.split(StoryNarration.text(card.lines().get(shown)).copy().withStyle(ChatFormatting.ITALIC),
                Math.min(360, w - 40));
        int count = Math.min(5, rows.size());
        int widest = 0;
        for (int i = 0; i < count; i++) {
            widest = Math.max(widest, font.width(rows.get(i)));
        }
        int top = h - 74 - count * 11;
        g.fill(w / 2 - widest / 2 - 10, top - 6, w / 2 + widest / 2 + 10, top + count * 11 + 4, ARGB.color((int) (lineAlpha * 120), 6, 4, 10));
        for (int i = 0; i < count; i++) {
            FormattedCharSequence seq = rows.get(i);
            g.text(font, seq, w / 2 - font.width(seq) / 2, top + i * 11, ARGB.color((int) (lineAlpha * 255), 0xEE, 0xE2, 0xC8), true);
        }
    }

    /** „KAPITEL III“, „PROLOG“ oder „EPILOG“ – in gesperrten Großbuchstaben. */
    public static Component label(Chapter chapter) {
        String text;
        if (chapter == Chapter.PROLOG || chapter == Chapter.CROWN) {
            text = Component.translatable(chapter == Chapter.PROLOG ? "chapter.kingdomsatwar.label.prolog" : "chapter.kingdomsatwar.label.epilog").getString();
        } else {
            text = Component.translatable("chapter.kingdomsatwar.label", chapter.numeral()).getString();
        }
        StringBuilder spaced = new StringBuilder();
        String upper = text.toUpperCase(java.util.Locale.ROOT);
        for (int i = 0; i < upper.length(); i++) {
            if (i > 0) {
                spaced.append(' ');
            }
            spaced.append(upper.charAt(i));
        }
        return Component.literal(spaced.toString());
    }
}
