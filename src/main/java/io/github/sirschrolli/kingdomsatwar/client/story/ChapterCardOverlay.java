package io.github.sirschrolli.kingdomsatwar.client.story;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.story.Chapter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * Titelkarte beim Beginn eines Kapitels: „KAPITEL III“, darunter groß der Titel und eine Zeile des Erzählers, auf
 * einem dunklen Band mit Zierlinien. Blendet sich weich ein und aus. Die Karte wartet, bis kein Fenster (z. B. das
 * Intro) mehr offen ist und die Anzeige sichtbar ist; mehrere Karten kommen nacheinander.
 */
public final class ChapterCardOverlay {
    public static final Identifier LAYER = KingdomsAtWar.id("chapter_card");
    private static final Identifier ORNAMENT = KingdomsAtWar.id("textures/gui/chronicle.png");
    private static final int FADE_IN = 25;
    private static final int HOLD = 150;
    private static final int FADE_OUT = 35;
    private static final int TOTAL = FADE_IN + HOLD + FADE_OUT;
    /** So lange nach dem Schließen eines Fensters warten, bevor eine Karte erscheint. */
    private static final int SETTLE = 30;

    private record Card(Chapter chapter, @Nullable DragonType fallen) {}

    private static final Deque<Card> QUEUE = new ArrayDeque<>();
    private static @Nullable Card current;
    private static int age;
    private static int settle;

    private ChapterCardOverlay() {}

    public static void queue(int chapter, int fallen) {
        DragonType element = fallen >= 0 && fallen < DragonType.values().length ? DragonType.values()[fallen] : null;
        QUEUE.add(new Card(Chapter.byIndex(chapter), element));
    }

    /** Alle wartenden Karten verwerfen (für die Screenshot-Automatik). */
    public static void clear() {
        QUEUE.clear();
        current = null;
    }

    public static boolean isShowing() {
        return current != null;
    }

    /** Einmal pro Tick (vom Client-Takt). */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            QUEUE.clear();
            current = null;
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
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BELL_BLOCK, 0.5f, 0.6f));
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_RESONATE, 0.7f, 0.5f));
            }
            return;
        }
        if (!blocked || age < FADE_IN) {
            age++;
        }
        if (age >= TOTAL) {
            current = null;
            settle = 20;
        }
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
        } else if (t < FADE_IN + HOLD) {
            alpha = 1.0f;
        } else {
            alpha = 1.0f - (t - FADE_IN - HOLD) / FADE_OUT;
        }
        alpha = Mth.clamp(alpha, 0.0f, 1.0f);
        if (alpha <= 0.01f) {
            return;
        }
        Font font = mc.font;
        int w = g.guiWidth();
        int cy = g.guiHeight() / 4 + 4;
        int a = (int) (alpha * 255);
        // dunkles Band
        int band = (int) (alpha * 150);
        g.fillGradient(0, cy - 30, w, cy - 6, ARGB.color(0, 0, 0, 0), ARGB.color(band, 6, 4, 10));
        g.fill(0, cy - 6, w, cy + 40, ARGB.color(band, 6, 4, 10));
        g.fillGradient(0, cy + 40, w, cy + 64, ARGB.color(band, 6, 4, 10), ARGB.color(0, 0, 0, 0));

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

        // Zeile des Erzählers
        Component line = card.fallen() != null
                ? Component.translatable("chapter.kingdomsatwar.card.fallen",
                        Component.translatable("lair.kingdomsatwar." + card.fallen().id() + ".dragon").withColor(card.fallen().color()))
                : Component.translatable(card.chapter().titleKey() + ".card");
        List<FormattedCharSequence> lines = font.split(line.copy().withStyle(ChatFormatting.ITALIC), Math.min(320, w - 40));
        for (int i = 0; i < Math.min(3, lines.size()); i++) {
            FormattedCharSequence seq = lines.get(i);
            g.text(font, seq, w / 2 - font.width(seq) / 2, cy + 20 + i * 11, ARGB.color(a, 0xD8, 0xCC, 0xB8), true);
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
