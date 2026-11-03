package io.github.sirschrolli.kingdomsatwar.client.army;

import io.github.sirschrolli.kingdomsatwar.client.renderer.SoldierRenderer;
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
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/** Gemeinsame Bausteine der Armee-Fenster (Feldherrentisch und Söldnertafel). */
final class ArmyWidgets {
    static final Identifier SLOT = Identifier.withDefaultNamespace("container/slot");
    static final Identifier HEART = Identifier.withDefaultNamespace("hud/heart/full");
    static final Identifier ARMOR = Identifier.withDefaultNamespace("hud/armor_full");
    static final Identifier BUTTON = Identifier.withDefaultNamespace("widget/button");
    static final Identifier BUTTON_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/button_highlighted");
    static final int LABEL = 0xFF404040;
    static final int MUTED = 0xFF6B6B6B;
    static final int GOOD = 0xFF2E7D32;
    static final int BAD = 0xFFB3261E;
    static final int GOLD = 0xFF8A6A10;

    private ArmyWidgets() {}

    /** Gesicht aus der Textur (Kopf und Helm), 24 × 24 mit dunklem Rand. */
    static void face(GuiGraphicsExtractor g, SoldierType type, int variant, int x, int y) {
        Identifier skin = SoldierRenderer.texture(type, variant);
        g.fill(x - 1, y - 1, x + 25, y + 25, 0xFF1E1E1E);
        g.blit(RenderPipelines.GUI_TEXTURED, skin, x, y, 8.0f, 8.0f, 24, 24, 8, 8, 128, 64);
        g.blit(RenderPipelines.GUI_TEXTURED, skin, x, y, 40.0f, 8.0f, 24, 24, 8, 8, 128, 64);
    }

    /** Kleines Waffen-Symbol (Schwert, Bogen, Armbrust, Speer, Streitkolben, Axt) für die Angriffswerte. */
    static void weaponIcon(GuiGraphicsExtractor g, SoldierType type, int x, int y) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(0.6875f, 0.6875f);
        g.item(new ItemStack(type.weaponIcon()), 0, 0);
        g.pose().popMatrix();
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
     * Ein Eintrag der Liste: Knopf, Gesicht, oben der Name (ausgewählt in Gold) mit den Rangsternen rechtsbündig, darunter
     * eine zweite Zeile (Art oder Rolle).
     */
    static void entry(GuiGraphicsExtractor g, Font font, int x, int y, int width, int height, boolean hovered, boolean selected, SoldierType type,
            int variant, String name, Component sub, int rank) {
        entry(g, font, x, y, width, height, hovered, selected, type, variant, name, sub, rank, width - 34);
    }

    /** Wie oben; die zweite Zeile ist höchstens {@code subWidth} breit (sonst mit "…" gekürzt), rechts daneben ist Platz. */
    static void entry(GuiGraphicsExtractor g, Font font, int x, int y, int width, int height, boolean hovered, boolean selected, SoldierType type,
            int variant, String name, Component sub, int rank, int subWidth) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, hovered || selected ? BUTTON_HIGHLIGHTED : BUTTON, x, y, width, height);
        face(g, type, variant, x + 5, y + 5);
        String stars = "★".repeat(rank);
        int starsWidth = rank > 0 ? font.width(stars) + 3 : 0;
        int nameWidth = width - 36 - starsWidth;
        g.text(font, font.plainSubstrByWidth(name, nameWidth), x + 33, y + 7, selected ? 0xFFFFE08A : 0xFFFFFFFF, true);
        if (rank > 0) {
            g.text(font, stars, x + width - 4 - font.width(stars), y + 7, 0xFFF2C14E, true);
        }
        g.text(font, shorten(font, sub.getString(), subWidth), x + 33, y + 19, 0xFFC8C8C8, true);
    }

    /** Kürzt einen Text auf {@code width} Pixel – mit "…", wenn etwas fehlt. */
    static String shorten(Font font, String text, int width) {
        if (font.width(text) <= width) {
            return text;
        }
        return font.plainSubstrByWidth(text, Math.max(0, width - font.width("…"))) + "…";
    }

    /**
     * Beschreibung unter dem Vorschaubild: bis zu drei Zeilen der vollen Breite, eine vierte (enger gesetzt) nur, wenn
     * sie links neben den Knopf unten rechts passt.
     */
    static void description(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int width, int lastWidth, int color) {
        java.util.List<net.minecraft.util.FormattedCharSequence> lines = font.split(text, width);
        boolean four = lines.size() > 3 && font.width(lines.get(3)) <= lastWidth;
        int spacing = four ? 9 : 10;
        for (int i = 0; i < Math.min(four ? 4 : 3, lines.size()); i++) {
            g.text(font, lines.get(i), x, y + i * spacing, color, false);
        }
    }

    /** Ein Wert und – wenn es einen gibt – der Wert danach in Grün ("30 → 38"). */
    static void stat(GuiGraphicsExtractor g, Font font, int x, int y, String now, @Nullable String next) {
        g.text(font, now, x, y, LABEL, false);
        if (next != null) {
            g.text(font, " → " + next, x + font.width(now), y, GOOD, false);
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
            g.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, sx, y, 18, 18);
            g.item(stack, sx + 1, y + 1);
            g.itemDecorations(font, stack, sx + 1, y + 1);
            boolean coin = i < coinStacks;
            int have = coin ? purse : player == null ? 0 : player.getInventory().countItem(stack.getItem());
            int need = coin ? coins : counts.get(i - coinStacks);
            boolean enough = creative || have >= need;
            if (!enough) {
                g.fill(sx + 1, y + 1, sx + 17, y + 17, 0x55FF3030);
            }
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
