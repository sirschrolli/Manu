package io.github.sirschrolli.kingdomsatwar.client.army;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.renderer.SoldierRenderer;
import io.github.sirschrolli.kingdomsatwar.client.town.Parchment;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Gemeinsame Bausteine der Armee-Fenster (Feldherrentisch und Söldnertafel) im Pergament-Stil des Stadtfensters:
 * Fensterbild, Bilderrahmen für den Soldaten, Einträge der Liste, Fächer für Preise.
 */
final class ArmyWidgets {
    /** Atlas: Fenster (320 × 218), Bilderrahmen (68 × 96 bei 322/0), Fach (18 × 18 bei 392/0). */
    static final Identifier ATLAS = KingdomsAtWar.id("textures/gui/recruitment_table.png");
    static final int ATLAS_W = 512;
    static final int ATLAS_H = 256;
    static final int WIDTH = 320;
    static final int HEIGHT = 218;
    static final Identifier HEART = Identifier.withDefaultNamespace("hud/heart/full");
    static final Identifier ARMOR = Identifier.withDefaultNamespace("hud/armor_full");
    static final int LABEL = Parchment.TEXT;
    static final int MUTED = Parchment.MUTED;
    static final int GOOD = Parchment.GOOD;
    static final int BAD = Parchment.BAD;
    static final int GOLD = Parchment.GOLD;
    /** Rückmeldungen im blauen Band (hell genug für den dunklen Grund). */
    static final int RIBBON_GOOD = 0xFFB4EC8C;
    static final int RIBBON_BAD = 0xFFFFA08C;

    private ArmyWidgets() {}

    /** Das Fenster selbst (Rahmen, Band, Pergament). */
    static void window(GuiGraphicsExtractor g, int x, int y) {
        g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, x, y, 0.0f, 0.0f, WIDTH, HEIGHT, ATLAS_W, ATLAS_H);
    }

    /** Der Bilderrahmen für den Soldaten (68 × 96; innen 3 Pixel Leiste). */
    static void portrait(GuiGraphicsExtractor g, int x, int y) {
        g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, x, y, 322.0f, 0.0f, 68, 96, ATLAS_W, ATLAS_H);
    }

    /**
     * Titel im blauen Band: links ein Bild und der Name (golden), rechts die Armee – für ein paar Sekunden steht dort
     * stattdessen die Antwort des Servers (grün oder rot).
     */
    static void header(GuiGraphicsExtractor g, Font font, int x0, int y0, ItemStack icon, Component title, Component right,
            @Nullable Component feedback, boolean good) {
        Parchment.item(g, icon, x0 + 11, y0 + 10, 1.0f);
        Component side = feedback != null ? feedback : right;
        int sideW = font.width(side);
        int titleMax = WIDTH - 31 - 12 - Math.max(sideW, font.width(right)) - 8;
        Component shown = font.width(title) <= titleMax ? title
                : Component.literal(Parchment.ellipsize(font, title.getString(), titleMax)).withStyle(title.getStyle());
        g.text(font, shown, x0 + 31, y0 + 14, Parchment.GOLD_TEXT, true);
        g.text(font, side, x0 + WIDTH - 12 - sideW, y0 + 14, feedback == null ? Parchment.RIBBON_TEXT : good ? RIBBON_GOOD : RIBBON_BAD, true);
    }

    /** Gesicht aus der Textur (Kopf und Helm), 24 × 24 mit dunklem Rahmen. */
    static void face(GuiGraphicsExtractor g, SoldierType type, int variant, int x, int y) {
        Identifier skin = SoldierRenderer.texture(type, variant);
        g.fill(x - 1, y - 1, x + 25, y + 25, 0xFF5A4126);
        g.blit(RenderPipelines.GUI_TEXTURED, skin, x, y, 8.0f, 8.0f, 24, 24, 8, 8, 128, 64);
        g.blit(RenderPipelines.GUI_TEXTURED, skin, x, y, 40.0f, 8.0f, 24, 24, 8, 8, 128, 64);
    }

    /** Die Stufe in der Ecke des Gesichts (wie die Zahl an einem Stapel), ab Stufe 2. */
    static void levelBadge(GuiGraphicsExtractor g, Font font, int x, int y, int level) {
        if (level < 2) {
            return;
        }
        String text = String.valueOf(level);
        g.text(font, text, x + 25 - font.width(text), y + 17, level >= io.github.sirschrolli.kingdomsatwar.army.Veterancy.MAX_LEVEL
                ? 0xFFFFE08A : 0xFFF2C14E, true);
    }

    /**
     * "Stufe 7" und daneben der Weg zur nächsten Stufe als Leiste (auf der höchsten Stufe ganz gefüllt); darüber zeigt ein
     * Tooltip die Kampferfahrung in Zahlen und wie man sie sammelt.
     */
    static void levelLine(GuiGraphicsExtractor g, Font font, int x, int y, int width, int level, int experience, int mouseX, int mouseY) {
        Component text = Component.translatable("soldier.kingdomsatwar.level", level);
        g.text(font, text, x, y, LABEL, false);
        int bx = x + font.width(text) + 5;
        int bw = width - (bx - x) - 1;
        float progress = io.github.sirschrolli.kingdomsatwar.army.Veterancy.progress(experience);
        Parchment.bar(g, bx, y + 1, bw, 6, progress, 0xFFD39A2C);
        if (mouseX >= x && mouseX < x + width && mouseY >= y - 1 && mouseY < y + 9) {
            boolean max = level >= io.github.sirschrolli.kingdomsatwar.army.Veterancy.MAX_LEVEL;
            Component numbers = max ? Component.translatable("gui.kingdomsatwar.soldier.experience.max", experience)
                    : Component.translatable("gui.kingdomsatwar.soldier.experience", experience,
                            io.github.sirschrolli.kingdomsatwar.army.Veterancy.total(level + 1));
            g.setTooltipForNextFrame(font, java.util.stream.Stream.of(numbers.copy().withStyle(ChatFormatting.GOLD),
                    Component.translatable("gui.kingdomsatwar.soldier.experience.hint").withStyle(ChatFormatting.GRAY))
                    .flatMap(c -> font.split(c, 200).stream()).toList(), mouseX, mouseY);
        }
    }

    /** Kleines Waffen-Symbol (Schwert, Bogen, Armbrust, Speer, Streitkolben, Axt) für die Angriffswerte. */
    static void weaponIcon(GuiGraphicsExtractor g, SoldierType type, int x, int y) {
        Parchment.item(g, new ItemStack(type.weaponIcon()), x, y, 0.6875f);
    }

    /** Ein Text, der in {@code width} Pixel passen muss: notfalls etwas kleiner gesetzt (bis 70 %), dann gekürzt. */
    static void fitText(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int width, int color, boolean shadow) {
        int w = font.width(text);
        if (w <= width) {
            g.text(font, text, x, y, color, shadow);
            return;
        }
        float scale = Math.max(0.7f, (float) width / w);
        g.pose().pushMatrix();
        g.pose().translate(x, y + (1.0f - scale) * 4.0f);
        g.pose().scale(scale, scale);
        String shown = shorten(font, text.getString(), (int) (width / scale));
        g.text(font, Component.literal(shown).withStyle(text.getStyle()), 0, 0, color, shadow);
        g.pose().popMatrix();
    }

    /** "Rekrut", "Veteran ★☆", "Elite ★★" – der Rang in Worten und Sternen. */
    static Component rank(int rank, int max) {
        return Component.translatable("soldier.kingdomsatwar.rank." + rank).append(" " + "★".repeat(rank) + "☆".repeat(max - rank));
    }

    /**
     * Ein Eintrag der Liste: Karte auf dem Pergament (ausgewählt mit Goldrand), Gesicht, oben der Name mit den Rangsternen
     * rechtsbündig, darunter eine zweite Zeile (Art oder Rolle) über die ganze Breite; die dritte Zeile ({@link #ROW3})
     * füllt das Fenster selbst (Gesundheit und Proviant bzw. der Sold).
     */
    static void entry(GuiGraphicsExtractor g, Font font, int x, int y, int width, int height, boolean hovered, boolean selected, SoldierType type,
            int variant, String name, Component sub, int rank) {
        Parchment.card(g, x, y, width, height, hovered, selected);
        face(g, type, variant, x + 4, y + 5);
        String stars = "★".repeat(rank);
        int starsWidth = rank > 0 ? font.width(stars) + 3 : 0;
        int nameWidth = width - TEXT_X - 3 - starsWidth;
        g.text(font, shorten(font, name, nameWidth), x + TEXT_X, y + 6, selected ? 0xFF5A3608 : LABEL, false);
        if (rank > 0) {
            g.text(font, stars, x + width - 3 - font.width(stars), y + 6, 0xFFB8862A, false);
        }
        g.text(font, shorten(font, sub.getString(), width - TEXT_X - 3), x + TEXT_X, y + 16, MUTED, false);
    }

    /** Wo im Eintrag die Schrift beginnt (rechts neben dem Gesicht). */
    static final int TEXT_X = 31;
    /** Die dritte Zeile eines Eintrags (Abstand von oben). */
    static final int ROW3 = 26;

    /** Kürzt einen Text auf {@code width} Pixel – mit "…", wenn etwas fehlt. */
    static String shorten(Font font, String text, int width) {
        return Parchment.ellipsize(font, text, width);
    }

    /** Ein Wert und – wenn es einen gibt – der Wert danach in Grün ("30 → 38"). */
    static void stat(GuiGraphicsExtractor g, Font font, int x, int y, String now, @Nullable String next) {
        g.text(font, now, x, y, LABEL, false);
        if (next != null) {
            g.text(font, " → " + next, x + font.width(now), y, GOOD, false);
        }
    }

    /** Rollbalken auf dem Pergament: helle Rinne, Griff aus Holz (golden umrandet, solange man ihn anfasst). */
    static void scrollbar(GuiGraphicsExtractor g, int x, int y, int w, int h, int thumbY, int thumbH, boolean hot) {
        g.fill(x, y, x + w, y + h, 0xFF8C7650);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFC9B48A);
        g.fill(x, thumbY, x + w, thumbY + thumbH, hot ? 0xFFE0B04A : 0xFF2E1D10);
        g.fill(x + 1, thumbY + 1, x + w - 1, thumbY + thumbH - 1, hot ? 0xFF8E6038 : 0xFF74492A);
        g.fill(x + 1, thumbY + 1, x + w - 1, thumbY + 2, 0xFFA4744A);
        g.fill(x + 1, thumbY + thumbH - 2, x + w - 1, thumbY + thumbH - 1, 0xFF4A2E18);
        // Griffrillen in der Mitte
        int my = thumbY + thumbH / 2;
        for (int i = -1; i <= 1; i++) {
            g.fill(x + 1, my + i * 2, x + w - 1, my + i * 2 + 1, 0xFF4A2E18);
        }
    }

    /**
     * Eine Reihe Fächer: erst die Münzen, dann die Ausrüstung; was fehlt, ist rot hinterlegt. Mit Tooltip (Preis und
     * was man hat).
     */
    static void costRow(GuiGraphicsExtractor g, Font font, @Nullable Player player, int x, int y, int coins, List<ItemStack> items,
            List<Integer> counts, int mouseX, int mouseY) {
        List<ItemStack> shown = new ArrayList<>(Coins.stacks(coins));
        int coinStacks = shown.size();
        shown.addAll(items);
        boolean creative = player != null && player.hasInfiniteMaterials();
        int purse = player == null ? 0 : Coins.total(player);
        for (int i = 0; i < shown.size(); i++) {
            ItemStack stack = shown.get(i);
            int sx = x + i * 20;
            g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, sx, y, 392.0f, 0.0f, 18, 18, ATLAS_W, ATLAS_H);
            boolean coin = i < coinStacks;
            int have = coin ? purse : player == null ? 0 : player.getInventory().countItem(stack.getItem());
            int need = coin ? coins : counts.get(i - coinStacks);
            boolean enough = creative || have >= need;
            if (!enough) {
                g.fill(sx + 2, y + 2, sx + 16, y + 16, 0x66D03A28);
            }
            g.item(stack, sx + 1, y + 1);
            g.itemDecorations(font, stack, sx + 1, y + 1);
            if (mouseX >= sx && mouseX < sx + 18 && mouseY >= y && mouseY < y + 18) {
                List<Component> tooltip = new ArrayList<>();
                if (coin) {
                    tooltip.add(Component.translatable("gui.kingdomsatwar.recruitment.pay", Coins.format(need)));
                    tooltip.add(Component.translatable("gui.kingdomsatwar.recruitment.purse", Coins.format(have))
                            .withStyle(enough ? ChatFormatting.GREEN : ChatFormatting.RED));
                } else {
                    tooltip.add(Component.literal(need + "× ").append(stack.getHoverName()));
                    tooltip.add(Component.translatable("gui.kingdomsatwar.recruitment.have", have)
                            .withStyle(enough ? ChatFormatting.GREEN : ChatFormatting.RED));
                }
                g.setTooltipForNextFrame(font, tooltip, Optional.empty(), stack, mouseX, mouseY);
            }
        }
    }
}
