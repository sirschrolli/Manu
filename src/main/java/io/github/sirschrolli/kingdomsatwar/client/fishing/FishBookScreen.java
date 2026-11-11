package io.github.sirschrolli.kingdomsatwar.client.fishing;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.ModKeyMappings;
import io.github.sirschrolli.kingdomsatwar.fishing.FishItem;
import io.github.sirschrolli.kingdomsatwar.fishing.FishLog;
import io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies;
import io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies.Chapter;
import io.github.sirschrolli.kingdomsatwar.fishing.Habitat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.platform.InputConstants;
import org.jspecify.annotations.Nullable;

/**
 * Das Fischbuch (Taste H): ein aufgeschlagener Band in Seeleder. Am linken Rand die Lesezeichen der neun Kapitel (Flüsse und
 * Seen, das Meer, die Tiefsee, der Nether, End und Leere, das Feenreich, die Schattenwelt, der Abgrund, die Legenden); links
 * die Arten des Kapitels in Fächern – gefangene mit ihrem Bild, die übrigen als Schattenriss –, rechts die gewählte Art: wo
 * und wann sie beißt (das steht auch bei noch nie gefangenen dort, als Fährte), Größe und eigener Rekord, wie oft gefangen,
 * was sie gegessen bewirkt, und eine Notiz.
 */
public class FishBookScreen extends Screen {
    private static final Identifier BOOK = KingdomsAtWar.id("textures/gui/fishing/book.png");
    private static final int TEX_W = 512;
    private static final int TEX_H = 256;
    private static final int W = 320;
    private static final int H = 200;
    private static final int LEFT_X = 20;
    private static final int RIGHT_X = 172;
    private static final int PAGE_W = 128;
    private static final int TOP = 16;
    private static final int BOTTOM = 186;
    private static final int GRID_Y = TOP + 36;
    private static final int COLUMNS = 5;
    private static final int PITCH = 25;
    private static final int BODY_Y = TOP + 58;
    private static final int TAB_Y = 14;
    private static final int TAB_PITCH = 19;

    private static final int INK = 0xFF3A2414;
    private static final int MUTED = 0xFF7A6248;
    private static final int SEA = 0xFF1C4E6A;
    private static final int GREEN = 0xFF2E6B2E;
    private static final int RED = 0xFF8A2416;
    private static final int PURPLE = 0xFF5B2A86;

    /** Eine Zeile der rechten Seite: Text, Farbe, Einzug, Zeichen (0–6 aus der Leiste der Symbole, -1 keins), Abstand davor. */
    private record Line(FormattedCharSequence text, int color, int indent, int icon, int gap) {}

    private final FishLog log;
    private Chapter chapter = Chapter.RIVERS;
    private List<FishSpecies> entries = List.of();
    private @Nullable FishSpecies selected;
    private final List<Line> lines = new ArrayList<>();
    private int contentHeight;
    private double scroll;
    private int left;
    private int top;

    public FishBookScreen(FishLog log) {
        super(Component.translatable("gui.kingdomsatwar.fish_book.title"));
        this.log = log;
        // aufgeschlagen wird das erste Kapitel, in dem schon etwas gefangen wurde
        for (Chapter c : Chapter.values()) {
            if (FishSpecies.of(c).stream().anyMatch(log::has)) {
                this.chapter = c;
                break;
            }
        }
    }

    @Override
    protected void init() {
        this.left = (this.width - W) / 2 + 8;
        this.top = (this.height - H) / 2;
        this.open(this.chapter, this.selected);
    }

    /** Schlägt ein Kapitel auf und wählt eine Art (ohne Angabe: die erste gefangene, sonst die erste). */
    public void open(Chapter chapter, @Nullable FishSpecies species) {
        this.chapter = chapter;
        this.entries = FishSpecies.of(chapter);
        if (species == null || species.chapter() != chapter) {
            species = this.entries.stream().filter(this.log::has).findFirst().orElse(this.entries.getFirst());
        }
        this.selected = species;
        this.buildPage();
    }

    private int caughtIn(Chapter chapter) {
        return (int) FishSpecies.of(chapter).stream().filter(this.log::has).count();
    }

    // =====================================================================================
    // Inhalt der rechten Seite
    // =====================================================================================

    private void buildPage() {
        this.lines.clear();
        this.scroll = 0;
        FishSpecies species = this.selected;
        if (species == null) {
            return;
        }
        boolean caught = this.log.has(species);
        // wo
        MutableComponent where = Component.empty();
        boolean first = true;
        for (Habitat habitat : species.habitats()) {
            if (!first) {
                where.append(", ");
            }
            where.append(Component.translatable(habitat.key()));
            first = false;
        }
        this.paragraph(where, INK, 12, 0, 0);
        // wann
        this.paragraph(Component.translatable("fishing.kingdomsatwar.time." + species.time().name().toLowerCase(java.util.Locale.ROOT)), INK, 12, 1, 3);
        if (species.weather() != FishSpecies.Weather.ANY) {
            this.paragraph(Component.translatable("fishing.kingdomsatwar.weather." + species.weather().name().toLowerCase(java.util.Locale.ROOT)),
                    INK, 12, 2, 3);
        }
        // Größe und Rekord
        this.paragraph(Component.translatable("fishing.kingdomsatwar.size", species.minCm(), species.maxCm()), INK, 12, 3, 3);
        if (caught) {
            this.paragraph(Component.translatable("fishing.kingdomsatwar.record", this.log.best(species)), SEA, 12, 4, 3);
        }
        // Wirkung
        List<FishItem.Effect> effects = FishItem.effects(species);
        if (caught) {
            boolean head = true;
            for (FishItem.Effect effect : effects) {
                this.paragraph(effect.text(), effect.kind() == 0 ? GREEN : effect.kind() == 1 ? RED : PURPLE, 12, head ? 6 : -1, head ? 3 : 0);
                head = false;
            }
            if (effects.isEmpty()) {
                this.paragraph(Component.translatable("fishing.kingdomsatwar.eaten.none"), MUTED, 12, 6, 3);
            }
            this.divider();
            this.paragraph(Component.translatable(species.noteKey()).withStyle(ChatFormatting.ITALIC), MUTED, 0, -1, 0);
        } else {
            this.paragraph(Component.translatable("fishing.kingdomsatwar.eaten.unknown"), MUTED, 12, 6, 3);
            this.divider();
            this.paragraph(Component.translatable("fishing.kingdomsatwar.uncaught").withStyle(ChatFormatting.ITALIC), MUTED, 0, -1, 0);
        }
        this.contentHeight = 0;
        for (Line line : this.lines) {
            this.contentHeight += line.gap() + 10;
        }
    }

    private void paragraph(Component text, int color, int indent, int icon, int gap) {
        boolean firstLine = true;
        for (FormattedCharSequence part : this.font.split(text, PAGE_W - indent)) {
            this.lines.add(new Line(part, color, indent, firstLine ? icon : -1, firstLine ? gap : 0));
            firstLine = false;
        }
    }

    private void divider() {
        this.lines.add(new Line(FormattedCharSequence.EMPTY, 0, 0, 100, 4));
        this.lines.add(new Line(FormattedCharSequence.EMPTY, 0, 0, -1, -6));
    }

    // =====================================================================================
    // Eingaben
    // =====================================================================================

    private @Nullable Chapter tabAt(double mx, double my) {
        for (Chapter c : Chapter.values()) {
            int y = this.top + TAB_Y + c.ordinal() * TAB_PITCH;
            int x = this.left - 22 + (c == this.chapter ? 0 : 6);
            if (mx >= x && mx < this.left + 2 && my >= y && my < y + 18) {
                return c;
            }
        }
        return null;
    }

    private @Nullable FishSpecies slotAt(double mx, double my) {
        int gx = this.left + LEFT_X + 3;
        int gy = this.top + GRID_Y;
        for (int i = 0; i < this.entries.size(); i++) {
            int x = gx + (i % COLUMNS) * PITCH;
            int y = gy + (i / COLUMNS) * PITCH;
            if (mx >= x && mx < x + 22 && my >= y && my < y + 22) {
                return this.entries.get(i);
            }
        }
        return null;
    }

    private void page() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0f));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        Chapter tab = this.tabAt(event.x(), event.y());
        if (tab != null && tab != this.chapter) {
            this.open(tab, null);
            this.page();
            return true;
        }
        FishSpecies slot = this.slotAt(event.x(), event.y());
        if (slot != null && slot != this.selected) {
            this.selected = slot;
            this.buildPage();
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.3f));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int visible = BOTTOM - BODY_Y;
        if (this.contentHeight > visible) {
            this.scroll = Mth.clamp(this.scroll - scrollY * 12.0, 0.0, this.contentHeight - visible);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.minecraft.options.keyInventory.matches(event) || ModKeyMappings.FISH_BOOK.matches(event)) {
            this.onClose();
            return true;
        }
        int index = this.entries.indexOf(this.selected);
        int step = event.key() == InputConstants.KEY_RIGHT ? 1 : event.key() == InputConstants.KEY_LEFT ? -1 : 0;
        if (step != 0 && index >= 0) {
            this.selected = this.entries.get(Math.floorMod(index + step, this.entries.size()));
            this.buildPage();
            return true;
        }
        int turn = event.key() == InputConstants.KEY_DOWN ? 1 : event.key() == InputConstants.KEY_UP ? -1 : 0;
        if (turn != 0) {
            Chapter[] all = Chapter.values();
            this.open(all[Math.floorMod(this.chapter.ordinal() + turn, all.length)], null);
            this.page();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    // =====================================================================================
    // Zeichnen
    // =====================================================================================

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractBackground(g, mouseX, mouseY, a);
        // die Lesezeichen stecken im Buch: erst sie, dann der Band darüber
        Chapter hovered = this.tabAt(mouseX, mouseY);
        for (Chapter c : Chapter.values()) {
            int y = this.top + TAB_Y + c.ordinal() * TAB_PITCH;
            boolean open = c == this.chapter;
            int x = this.left - 22 - (c == hovered && !open ? 2 : 0);
            g.blit(RenderPipelines.GUI_TEXTURED, BOOK, x, y, open ? 400.0f : 368.0f, 24.0f, 28, 18, TEX_W, TEX_H);
            g.blit(RenderPipelines.GUI_TEXTURED, BOOK, x + (open ? 5 : 10), y + 3, 320.0f + c.ordinal() * 14, 70.0f, 12, 12, TEX_W, TEX_H);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, BOOK, this.left, this.top, 0.0f, 0.0f, W, H, TEX_W, TEX_H);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractRenderState(g, mouseX, mouseY, a);
        this.drawLeftPage(g, mouseX, mouseY);
        this.drawRightPage(g);
        Chapter tab = this.tabAt(mouseX, mouseY);
        if (tab != null) {
            g.setTooltipForNextFrame(this.font, Component.translatable(tab.key()).append(
                    Component.literal("  " + this.caughtIn(tab) + " / " + FishSpecies.of(tab).size()).withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
        }
    }

    private void drawLeftPage(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x0 = this.left + LEFT_X;
        int y0 = this.top;
        g.text(this.font, this.title.copy().withStyle(ChatFormatting.BOLD), x0 + 2, y0 + TOP + 1, SEA, false);
        String total = this.log.speciesCaught() + " / " + FishSpecies.values().length;
        g.text(this.font, total, x0 + PAGE_W - 2 - this.font.width(total), y0 + TOP + 1, MUTED, false);
        // wie voll das Buch ist
        int bx = x0 + (PAGE_W - 102) / 2;
        g.blit(RenderPipelines.GUI_TEXTURED, BOOK, bx, y0 + TOP + 12, 320.0f, 104.0f, 102, 8, TEX_W, TEX_H);
        int fill = Math.round(100.0f * this.log.speciesCaught() / FishSpecies.values().length);
        if (fill > 0) {
            g.blit(RenderPipelines.GUI_TEXTURED, BOOK, bx + 1, y0 + TOP + 13, 320.0f, 114.0f, fill, 6, TEX_W, TEX_H);
        }
        g.text(this.font, Component.translatable(this.chapter.key()), x0 + 3, y0 + TOP + 24, INK, false);
        String part = this.caughtIn(this.chapter) + " / " + this.entries.size();
        g.text(this.font, part, x0 + PAGE_W - 2 - this.font.width(part), y0 + TOP + 24, MUTED, false);

        FishSpecies hovered = this.slotAt(mouseX, mouseY);
        int gx = x0 + 3;
        int gy = y0 + GRID_Y;
        for (int i = 0; i < this.entries.size(); i++) {
            FishSpecies species = this.entries.get(i);
            int x = gx + (i % COLUMNS) * PITCH;
            int y = gy + (i / COLUMNS) * PITCH;
            boolean caught = this.log.has(species);
            boolean legend = species.rarity() == FishSpecies.Rarity.LEGENDARY;
            g.blit(RenderPipelines.GUI_TEXTURED, BOOK, x, y, species == this.selected ? 368.0f : legend ? 392.0f : 320.0f, 0.0f, 22, 22, TEX_W, TEX_H);
            if (caught) {
                g.blit(RenderPipelines.GUI_TEXTURED, FishingClient.sprite(species), x + 3, y + 3, 0.0f, 0.0f, 16, 16, 16, 16);
                // ein Punkt in der Farbe der Seltenheit
                int color = 0xFF000000 | species.rarity().color;
                g.fill(x + 17, y + 2, x + 20, y + 5, 0xFF3A2414);
                g.fill(x + 18, y + 3, x + 19, y + 4, color);
                g.fill(x + 17, y + 3, x + 20, y + 4, color);
                g.fill(x + 18, y + 2, x + 19, y + 5, color);
            } else {
                g.blit(RenderPipelines.GUI_TEXTURED, FishingClient.silhouette(species), x + 3, y + 3, 0.0f, 0.0f, 16, 16, 16, 16, 0x90FFFFFF);
            }
            if (species == hovered && species != this.selected) {
                this.frame(g, x, y, 0xFFFFF2B0);
            }
        }
        if (hovered != null) {
            boolean caught = this.log.has(hovered);
            Component name = caught ? FishingClient.name(hovered) : Component.literal("? ? ?");
            g.setTooltipForNextFrame(this.font, name.copy().append(Component.literal("  ")).append(
                    Component.translatable(hovered.rarity().key()).withColor(hovered.rarity().color)), mouseX, mouseY);
        }
        // wo unter den Fächern Platz bleibt: ein Hinweis, wie man die Arten dieses Kapitels fängt
        int rows = (this.entries.size() + COLUMNS - 1) / COLUMNS;
        int tipY = gy + rows * PITCH + 2;
        float scale = 0.75f;
        for (FormattedCharSequence line : this.font.split(Component.translatable(this.chapter.key() + ".tip"), Math.round((PAGE_W - 6) / scale))) {
            if (tipY + 7 > y0 + BOTTOM - 10) {
                break;
            }
            g.pose().pushMatrix();
            g.pose().translate(x0 + 3, tipY);
            g.pose().scale(scale, scale);
            g.text(this.font, line, 0, 0, MUTED, false);
            g.pose().popMatrix();
            tipY += 7;
        }
        Component treasures = Component.translatable("fishing.kingdomsatwar.book.treasures", this.log.totalCaught(), this.log.treasures());
        this.small(g, treasures, x0 + PAGE_W / 2, y0 + BOTTOM - 8, MUTED);
    }

    /** Nur der Rand eines Fachs (das Bild darin bleibt sichtbar). */
    private void frame(GuiGraphicsExtractor g, int x, int y, int color) {
        g.fill(x + 1, y, x + 21, y + 1, color);
        g.fill(x + 1, y + 21, x + 21, y + 22, color);
        g.fill(x, y + 1, x + 1, y + 21, color);
        g.fill(x + 21, y + 1, x + 22, y + 21, color);
    }

    private void small(GuiGraphicsExtractor g, Component text, int centerX, int y, int color) {
        float scale = 0.75f;
        g.pose().pushMatrix();
        g.pose().translate(centerX - this.font.width(text) * scale / 2.0f, y);
        g.pose().scale(scale, scale);
        g.text(this.font, text, 0, 0, color, false);
        g.pose().popMatrix();
    }

    private void drawRightPage(GuiGraphicsExtractor g) {
        FishSpecies species = this.selected;
        if (species == null) {
            return;
        }
        int x0 = this.left + RIGHT_X;
        int y0 = this.top;
        boolean caught = this.log.has(species);
        g.blit(RenderPipelines.GUI_TEXTURED, BOOK, x0, y0 + TOP, 320.0f, 24.0f, 44, 44, TEX_W, TEX_H);
        if (caught) {
            g.blit(RenderPipelines.GUI_TEXTURED, FishingClient.sprite(species), x0 + 6, y0 + TOP + 6, 0.0f, 0.0f, 32, 32, 16, 16, 16, 16);
        } else {
            g.blit(RenderPipelines.GUI_TEXTURED, FishingClient.silhouette(species), x0 + 6, y0 + TOP + 6, 0.0f, 0.0f, 32, 32, 16, 16, 16, 16, 0xB0FFFFFF);
        }
        int tx = x0 + 49;
        int tw = PAGE_W - 49;
        Component name = (caught ? FishingClient.name(species).copy() : Component.literal("? ? ?")).withStyle(ChatFormatting.BOLD);
        int y = y0 + TOP;
        String plain = name.getString();
        if (this.font.width(name) <= tw || (!plain.contains(" ") && !plain.contains("-"))) {
            // eine Zeile – ein langes Wort wird schmaler gesetzt statt mitten im Wort umbrochen
            int width = this.font.width(name);
            float scale = width > tw ? (float) tw / width : 1.0f;
            g.pose().pushMatrix();
            g.pose().translate(tx, y + 1 + (1.0f - scale) * 4.0f);
            g.pose().scale(scale, scale);
            g.text(this.font, name, 0, 0, INK, false);
            g.pose().popMatrix();
            y += 10;
        } else {
            List<FormattedCharSequence> nameLines = this.font.split(name, tw);
            for (int i = 0; i < Math.min(2, nameLines.size()); i++) {
                g.text(this.font, nameLines.get(i), tx, y, INK, false);
                y += 9;
            }
        }
        g.text(this.font, Component.translatable(species.rarity().key()), tx, y + 1, FishingScreen.inkColor(species.rarity()), false);
        int stars = FishingScreen.stars(species);
        for (int i = 0; i < 5; i++) {
            g.blit(RenderPipelines.GUI_TEXTURED, BOOK, tx + i * 8, y + 12, i < stars ? 320.0f : 328.0f, 84.0f, 7, 7, TEX_W, TEX_H);
        }
        Component count = caught ? Component.translatable("fishing.kingdomsatwar.count", this.log.count(species))
                : Component.translatable("fishing.kingdomsatwar.count.none");
        g.pose().pushMatrix();
        g.pose().translate(tx, y + 22);
        g.pose().scale(0.75f, 0.75f);
        g.text(this.font, count, 0, 0, MUTED, false);
        g.pose().popMatrix();
        g.blit(RenderPipelines.GUI_TEXTURED, BOOK, x0 + PAGE_W / 2 - 55, y0 + TOP + 48, 320.0f, 94.0f, 110, 7, TEX_W, TEX_H);

        int areaTop = y0 + BODY_Y;
        int areaBottom = y0 + BOTTOM;
        g.enableScissor(x0 - 2, areaTop - 1, x0 + PAGE_W + 4, areaBottom);
        int ly = areaTop - (int) this.scroll;
        for (Line line : this.lines) {
            ly += line.gap();
            if (ly > areaTop - 12 && ly < areaBottom) {
                if (line.icon() == 100) {
                    g.blit(RenderPipelines.GUI_TEXTURED, BOOK, x0 + PAGE_W / 2 - 55, ly + 1, 320.0f, 94.0f, 110, 7, TEX_W, TEX_H);
                } else if (line.icon() >= 0) {
                    g.blit(RenderPipelines.GUI_TEXTURED, BOOK, x0 + 1, ly - 1, 320.0f + line.icon() * 11, 124.0f, 9, 9, TEX_W, TEX_H);
                }
                g.text(this.font, line.text(), x0 + line.indent(), ly, line.color(), false);
            }
            ly += 10;
        }
        g.disableScissor();
        int visible = BOTTOM - BODY_Y;
        if (this.contentHeight > visible) {
            if (this.scroll < this.contentHeight - visible - 1) {
                g.text(this.font, "▼", x0 + PAGE_W - 4, areaBottom + 1, MUTED, false);
            }
            if (this.scroll > 1) {
                g.text(this.font, "▲", x0 + PAGE_W - 4, areaTop - 9, MUTED, false);
            }
        }
    }
}
