package io.github.sirschrolli.kingdomsatwar.client.fishing;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.ModKeyMappings;
import io.github.sirschrolli.kingdomsatwar.fishing.FishItem;
import io.github.sirschrolli.kingdomsatwar.fishing.FishLog;
import io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies;
import io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies.Chapter;
import io.github.sirschrolli.kingdomsatwar.fishing.Habitat;
import io.github.sirschrolli.kingdomsatwar.fishing.Medium;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
import org.jspecify.annotations.Nullable;

/**
 * Das Fischbuch (Taste H): ein aufgeschlagener Band in Seeleder, seit 0.81 mit <b>1000 Arten</b>. Am linken Rand die
 * Lesezeichen der zehn Kapitel (Flüsse und Seen, das Meer, die Tiefsee, die Höhlen, der Nether, End und Leere, das Feenreich,
 * die Schattenwelt, der Abgrund, die Legenden). Die linke Seite zeigt erst die <b>Gewässer</b> des Kapitels – „Überall“ für die
 * Allerweltsfische und darunter jedes Biom mit seinen eigenen Arten, je mit dem Stand (gefangen / vorhanden) –, nach einem
 * Klick die Arten dieses Gewässers in Fächern: gefangene mit ihrem Bild, die übrigen als Schattenriss. Rechts steht die
 * gewählte Art: wo und wann sie beißt (auch bei noch nie gefangenen, als Fährte), Größe und eigener Rekord, wie oft gefangen,
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
    private static final int LIST_Y = TOP + 36;
    private static final int ROW_H = 13;
    private static final int ROWS = 9;
    private static final int GRID_Y = LIST_Y + 15;
    private static final int COLUMNS = 5;
    private static final int PITCH = 25;
    private static final int GRID_ROWS = 4;
    private static final int BODY_Y = TOP + 58;
    private static final int TAB_Y = 10;
    private static final int TAB_PITCH = 18;

    private static final int INK = 0xFF3A2414;
    private static final int MUTED = 0xFF7A6248;
    private static final int SEA = 0xFF1C4E6A;
    private static final int GREEN = 0xFF2E6B2E;
    private static final int RED = 0xFF8A2416;
    private static final int PURPLE = 0xFF5B2A86;
    private static final int GOLD = 0xFF9A6A0C;

    /** Ein Gewässer eines Kapitels: „Überall“ (die Allerweltsfische, {@code biome} leer) oder ein Biom mit seinen eigenen Arten. */
    public record Water(String biome, List<FishSpecies> species) {
        public Component title() {
            if (this.biome.isEmpty()) {
                boolean legends = !this.species.isEmpty() && this.species.getFirst().chapter() == Chapter.LEGENDS;
                return Component.translatable(legends ? "fishing.kingdomsatwar.book.all_worlds" : "fishing.kingdomsatwar.book.everywhere");
            }
            Identifier id = Identifier.parse(this.biome);
            return Component.translatable("biome." + id.getNamespace() + "." + id.getPath());
        }
    }

    private static final Map<Chapter, List<Water>> WATERS = new EnumMap<>(Chapter.class);

    /** Die Gewässer eines Kapitels: erst „Überall“, dann die Biome (die eigenen der Mod zuerst). */
    public static List<Water> waters(Chapter chapter) {
        if (WATERS.isEmpty()) {
            for (Chapter c : Chapter.values()) {
                WATERS.put(c, new ArrayList<>());
            }
            Map<Chapter, List<FishSpecies>> general = new EnumMap<>(Chapter.class);
            for (FishSpecies species : FishSpecies.values()) {
                if (species.biome() == null || species.chapter() == Chapter.LEGENDS) {
                    general.computeIfAbsent(species.chapter(), key -> new ArrayList<>()).add(species);
                }
            }
            general.forEach((c, list) -> WATERS.get(c).add(new Water("", List.copyOf(list))));
            for (String biome : FishSpecies.biomes()) {
                List<FishSpecies> list = FishSpecies.ofBiome(biome);
                WATERS.get(list.getFirst().chapter()).add(new Water(biome, list));
            }
        }
        return WATERS.get(chapter);
    }

    /** Eine Zeile der rechten Seite: Text, Farbe, Einzug, Zeichen (0–6 aus der Leiste der Symbole, -1 keins), Abstand davor. */
    private record Line(FormattedCharSequence text, int color, int indent, int icon, int gap) {}

    private final FishLog log;
    private Chapter chapter = Chapter.RIVERS;
    private List<Water> waters = List.of();
    /** Das aufgeschlagene Gewässer ({@code null}: die Liste der Gewässer ist zu sehen). */
    private @Nullable Water water;
    private List<FishSpecies> entries = List.of();
    private @Nullable FishSpecies selected;
    private final List<Line> lines = new ArrayList<>();
    private int contentHeight;
    private double scroll;
    private int listScroll;
    private int gridScroll;
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
        if (this.waters.isEmpty()) {
            this.open(this.chapter, this.selected);
        }
    }

    /**
     * Schlägt ein Kapitel auf. Mit einer Art: gleich ihr Gewässer und sie selbst; ohne: die Liste der Gewässer (ein Kapitel mit
     * nur einem Gewässer – die Legenden – zeigt gleich dessen Arten).
     */
    public void open(Chapter chapter, @Nullable FishSpecies species) {
        this.chapter = chapter;
        this.waters = waters(chapter);
        this.listScroll = 0;
        this.water = null;
        this.entries = List.of();
        if (species != null && species.chapter() == chapter) {
            for (Water w : this.waters) {
                if (w.species().contains(species)) {
                    this.openWater(w, species);
                    return;
                }
            }
        }
        if (this.waters.size() == 1) {
            this.openWater(this.waters.getFirst(), null);
            return;
        }
        // noch kein Gewässer gewählt: rechts steht die erste gefangene Art des Kapitels (sonst die erste überhaupt)
        List<FishSpecies> all = FishSpecies.of(chapter);
        this.selected = all.stream().filter(this.log::has).findFirst().orElse(all.isEmpty() ? null : all.getFirst());
        this.buildPage();
    }

    /** Schlägt ein Gewässer auf und wählt eine Art (ohne Angabe: die erste gefangene, sonst die erste). */
    public void openWater(Water water, @Nullable FishSpecies species) {
        this.water = water;
        this.entries = water.species();
        this.gridScroll = 0;
        if (species == null || !this.entries.contains(species)) {
            species = this.entries.stream().filter(this.log::has).findFirst().orElse(this.entries.getFirst());
        }
        this.select(species);
    }

    private void select(FishSpecies species) {
        this.selected = species;
        int row = this.entries.indexOf(species) / COLUMNS;
        this.gridScroll = Mth.clamp(this.gridScroll, Math.max(0, row - GRID_ROWS + 1), row);
        this.buildPage();
    }

    /** Zurück zur Liste der Gewässer. */
    private void closeWater() {
        if (this.water != null && this.waters.size() > 1) {
            int index = this.waters.indexOf(this.water);
            this.water = null;
            this.entries = List.of();
            this.listScroll = Mth.clamp(index - ROWS / 2, 0, this.maxListScroll());
        }
    }

    private int caughtIn(List<FishSpecies> species) {
        int n = 0;
        for (FishSpecies s : species) {
            if (this.log.has(s)) {
                n++;
            }
        }
        return n;
    }

    private int maxListScroll() {
        return Math.max(0, this.waters.size() - ROWS);
    }

    private int maxGridScroll() {
        return Math.max(0, (this.entries.size() + COLUMNS - 1) / COLUMNS - GRID_ROWS);
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
        if (species.biome() != null) {
            where.append(new Water(species.biome(), List.of()).title());
            if (species.media().contains(Medium.LAVA) && !species.media().contains(Medium.WATER)) {
                where.append(Component.translatable("fishing.kingdomsatwar.medium.lava"));
            } else if (species.media().contains(Medium.VOID)) {
                where.append(Component.translatable("fishing.kingdomsatwar.medium.void"));
            }
        } else {
            boolean first = true;
            for (Habitat habitat : species.habitats()) {
                if (!first) {
                    where.append(", ");
                }
                where.append(Component.translatable(habitat.key()));
                first = false;
            }
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

    /** Die Zeile der Gewässerliste unter der Maus (Index in {@link #waters}), -1: keine. */
    private int rowAt(double mx, double my) {
        if (this.water != null) {
            return -1;
        }
        int x = this.left + LEFT_X + 1;
        int y = this.top + LIST_Y;
        if (mx < x || mx >= x + PAGE_W - 6 || my < y || my >= y + ROWS * ROW_H) {
            return -1;
        }
        int index = this.listScroll + (int) ((my - y) / ROW_H);
        return index < this.waters.size() ? index : -1;
    }

    private boolean backAt(double mx, double my) {
        int x = this.left + LEFT_X + 1;
        int y = this.top + LIST_Y;
        return this.water != null && this.waters.size() > 1 && mx >= x && mx < x + PAGE_W - 2 && my >= y - 1 && my < y + 12;
    }

    private @Nullable FishSpecies slotAt(double mx, double my) {
        if (this.water == null) {
            return null;
        }
        int gx = this.left + LEFT_X + 3;
        int gy = this.top + GRID_Y;
        for (int i = this.gridScroll * COLUMNS; i < Math.min(this.entries.size(), (this.gridScroll + GRID_ROWS) * COLUMNS); i++) {
            int x = gx + (i % COLUMNS) * PITCH;
            int y = gy + (i / COLUMNS - this.gridScroll) * PITCH;
            if (mx >= x && mx < x + 22 && my >= y && my < y + 22) {
                return this.entries.get(i);
            }
        }
        return null;
    }

    private void page() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0f));
    }

    private void click() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.3f));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        Chapter tab = this.tabAt(event.x(), event.y());
        if (tab != null) {
            if (tab != this.chapter) {
                this.open(tab, null);
                this.page();
            } else if (this.water != null && this.waters.size() > 1) {
                this.closeWater();
                this.page();
            }
            return true;
        }
        if (this.backAt(event.x(), event.y()) || (event.button() == 1 && this.water != null)) {
            this.closeWater();
            this.page();
            return true;
        }
        int row = this.rowAt(event.x(), event.y());
        if (row >= 0) {
            this.openWater(this.waters.get(row), null);
            this.page();
            return true;
        }
        FishSpecies slot = this.slotAt(event.x(), event.y());
        if (slot != null && slot != this.selected) {
            this.select(slot);
            this.click();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX < this.left + RIGHT_X - 8) {
            int step = (int) -Math.signum(scrollY);
            if (this.water == null) {
                this.listScroll = Mth.clamp(this.listScroll + step * 3, 0, this.maxListScroll());
            } else {
                this.gridScroll = Mth.clamp(this.gridScroll + step, 0, this.maxGridScroll());
            }
            return true;
        }
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
        if (event.key() == InputConstants.KEY_BACKSPACE && this.water != null && this.waters.size() > 1) {
            this.closeWater();
            this.page();
            return true;
        }
        int index = this.entries.indexOf(this.selected);
        int step = event.key() == InputConstants.KEY_RIGHT ? 1 : event.key() == InputConstants.KEY_LEFT ? -1 : 0;
        if (step != 0 && index >= 0) {
            this.select(this.entries.get(Math.floorMod(index + step, this.entries.size())));
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
            List<FishSpecies> all = FishSpecies.of(tab);
            List<FormattedCharSequence> tip = new ArrayList<>();
            tip.add(Component.translatable(tab.key()).append(
                    Component.literal("  " + this.caughtIn(all) + " / " + all.size()).withStyle(ChatFormatting.GRAY)).getVisualOrderText());
            tip.addAll(this.font.split(Component.translatable(tab.key() + ".tip").withStyle(ChatFormatting.DARK_GRAY), 190));
            g.setTooltipForNextFrame(this.font, tip, mouseX, mouseY);
        }
    }

    private void drawLeftPage(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x0 = this.left + LEFT_X;
        int y0 = this.top;
        g.text(this.font, this.title.copy().withStyle(ChatFormatting.BOLD), x0 + 2, y0 + TOP + 1, SEA, false);
        String total = this.log.speciesCaught() + " / " + FishSpecies.count();
        g.text(this.font, total, x0 + PAGE_W - 2 - this.font.width(total), y0 + TOP + 1, MUTED, false);
        // wie voll das Buch ist
        int bx = x0 + (PAGE_W - 102) / 2;
        g.blit(RenderPipelines.GUI_TEXTURED, BOOK, bx, y0 + TOP + 12, 320.0f, 104.0f, 102, 8, TEX_W, TEX_H);
        int fill = Mth.ceil(100.0f * this.log.speciesCaught() / FishSpecies.count());
        if (fill > 0) {
            g.blit(RenderPipelines.GUI_TEXTURED, BOOK, bx + 1, y0 + TOP + 13, 320.0f, 114.0f, Math.min(100, fill), 6, TEX_W, TEX_H);
        }
        List<FishSpecies> all = FishSpecies.of(this.chapter);
        String part = this.caughtIn(all) + " / " + all.size();
        int partWidth = this.font.width(part);
        this.fitted(g, Component.translatable(this.chapter.key()), x0 + 3, y0 + TOP + 24, PAGE_W - 5 - partWidth - 6, INK);
        g.text(this.font, part, x0 + PAGE_W - 2 - partWidth, y0 + TOP + 24, MUTED, false);
        if (this.water == null) {
            this.drawWaters(g, x0, y0, mouseX, mouseY);
        } else {
            this.drawSpecies(g, x0, y0, mouseX, mouseY);
        }
        Component treasures = Component.translatable("fishing.kingdomsatwar.book.treasures", this.log.totalCaught(), this.log.treasures());
        this.small(g, treasures, x0 + PAGE_W / 2, y0 + BOTTOM - 8, MUTED);
    }

    /** Die Liste der Gewässer: je Zeile der Name und der Stand; vollständige tragen einen Stern. */
    private void drawWaters(GuiGraphicsExtractor g, int x0, int y0, int mouseX, int mouseY) {
        int hovered = this.rowAt(mouseX, mouseY);
        int x = x0 + 1;
        int w = PAGE_W - 7;
        for (int i = 0; i < ROWS && this.listScroll + i < this.waters.size(); i++) {
            int index = this.listScroll + i;
            Water water = this.waters.get(index);
            int y = y0 + LIST_Y + i * ROW_H;
            int caught = this.caughtIn(water.species());
            boolean done = caught == water.species().size();
            boolean hover = index == hovered;
            g.fill(x, y, x + w, y + ROW_H - 1, hover ? 0x50FFF2B0 : index % 2 == 0 ? 0x1C3A2414 : 0x0C3A2414);
            if (hover) {
                g.outline(x, y, w, ROW_H - 1, 0xFF9C8660);
            }
            String count = caught + "/" + water.species().size();
            int cw = this.font.width(count);
            int nameX = x + 3;
            if (done) {
                g.blit(RenderPipelines.GUI_TEXTURED, BOOK, nameX, y + 3, 320.0f, 84.0f, 7, 7, TEX_W, TEX_H);
                nameX += 9;
            }
            Component name = water.title();
            int room = w - (nameX - x) - cw - 6;
            int nw = this.font.width(name);
            float scale = nw > room ? (float) room / nw : 1.0f;
            g.pose().pushMatrix();
            g.pose().translate(nameX, y + 2 + (1.0f - scale) * 4.0f);
            g.pose().scale(scale, scale);
            g.text(this.font, name, 0, 0, done ? GOLD : caught > 0 ? INK : MUTED, false);
            g.pose().popMatrix();
            g.text(this.font, count, x + w - cw - 2, y + 2, done ? GOLD : caught > 0 ? SEA : MUTED, false);
        }
        int max = this.maxListScroll();
        if (max > 0) {
            int sx = x0 + PAGE_W - 4;
            int sy = y0 + LIST_Y;
            int sh = ROWS * ROW_H - 1;
            g.fill(sx, sy, sx + 3, sy + sh, 0x303A2414);
            int knob = Math.max(10, sh * ROWS / this.waters.size());
            int ky = sy + (sh - knob) * this.listScroll / max;
            g.fill(sx, ky, sx + 3, ky + knob, 0xFF8A6A3E);
        }
    }

    /** Die Arten des aufgeschlagenen Gewässers in Fächern. */
    private void drawSpecies(GuiGraphicsExtractor g, int x0, int y0, int mouseX, int mouseY) {
        Water water = this.water;
        int hy = y0 + LIST_Y;
        boolean many = this.waters.size() > 1;
        boolean backHover = this.backAt(mouseX, mouseY);
        if (many) {
            g.fill(x0 + 1, hy - 1, x0 + PAGE_W - 1, hy + 11, backHover ? 0x50FFF2B0 : 0x1C3A2414);
            g.text(this.font, "◀", x0 + 4, hy + 1, backHover ? RED : MUTED, false);
        }
        int caught = this.caughtIn(water.species());
        String count = caught + "/" + water.species().size();
        int cw = this.font.width(count);
        int nameX = x0 + (many ? 14 : 4);
        Component name = water.title();
        int room = PAGE_W - (nameX - x0) - cw - 8;
        int nw = this.font.width(name);
        float nameScale = nw > room ? (float) room / nw : 1.0f;
        g.pose().pushMatrix();
        g.pose().translate(nameX, hy + 1 + (1.0f - nameScale) * 4.0f);
        g.pose().scale(nameScale, nameScale);
        g.text(this.font, name, 0, 0, caught == water.species().size() ? GOLD : INK, false);
        g.pose().popMatrix();
        g.text(this.font, count, x0 + PAGE_W - 4 - cw, hy + 1, caught == water.species().size() ? GOLD : SEA, false);

        FishSpecies hovered = this.slotAt(mouseX, mouseY);
        int gx = x0 + 3;
        int gy = y0 + GRID_Y;
        for (int i = this.gridScroll * COLUMNS; i < Math.min(this.entries.size(), (this.gridScroll + GRID_ROWS) * COLUMNS); i++) {
            FishSpecies species = this.entries.get(i);
            int x = gx + (i % COLUMNS) * PITCH;
            int y = gy + (i / COLUMNS - this.gridScroll) * PITCH;
            boolean has = this.log.has(species);
            boolean legend = species.rarity() == FishSpecies.Rarity.LEGENDARY;
            g.blit(RenderPipelines.GUI_TEXTURED, BOOK, x, y, species == this.selected ? 368.0f : legend ? 392.0f : 320.0f, 0.0f, 22, 22, TEX_W, TEX_H);
            if (has) {
                g.blit(RenderPipelines.GUI_TEXTURED, FishingClient.sprite(species), x + 3, y + 3, 0.0f, 0.0f, 16, 16, 16, 16);
            } else {
                g.blit(RenderPipelines.GUI_TEXTURED, FishingClient.sprite(species), x + 3, y + 3, 0.0f, 0.0f, 16, 16, 16, 16, FishingClient.shadow(0x90));
            }
            // ein Punkt in der Farbe der Seltenheit (auch bei noch nie gefangenen: Er verrät, wie selten die Fährte ist)
            int color = 0xFF000000 | species.rarity().color;
            g.fill(x + 17, y + 2, x + 20, y + 5, 0xFF3A2414);
            g.fill(x + 18, y + 3, x + 19, y + 4, color);
            g.fill(x + 17, y + 3, x + 20, y + 4, color);
            g.fill(x + 18, y + 2, x + 19, y + 5, color);
            if (species == hovered && species != this.selected) {
                this.frame(g, x, y, 0xFFFFF2B0);
            }
        }
        if (this.maxGridScroll() > 0) {
            if (this.gridScroll > 0) {
                g.text(this.font, "▲", x0 + PAGE_W - 5, gy - 2, MUTED, false);
            }
            if (this.gridScroll < this.maxGridScroll()) {
                g.text(this.font, "▼", x0 + PAGE_W - 5, gy + GRID_ROWS * PITCH - 8, MUTED, false);
            }
        }
        if (hovered != null) {
            Component fish = this.log.has(hovered) ? FishingClient.name(hovered) : Component.literal("? ? ?");
            g.setTooltipForNextFrame(this.font, fish.copy().append(Component.literal("  ")).append(
                    Component.translatable(hovered.rarity().key()).withColor(hovered.rarity().color)), mouseX, mouseY);
        }
    }

    /** Text in einer Zeile – schmaler gesetzt, wenn er sonst breiter als {@code room} wäre. */
    private void fitted(GuiGraphicsExtractor g, Component text, int x, int y, int room, int color) {
        int width = this.font.width(text);
        float scale = width > room ? (float) room / width : 1.0f;
        g.pose().pushMatrix();
        g.pose().translate(x, y + (1.0f - scale) * 4.0f);
        g.pose().scale(scale, scale);
        g.text(this.font, text, 0, 0, color, false);
        g.pose().popMatrix();
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
            g.blit(RenderPipelines.GUI_TEXTURED, FishingClient.sprite(species), x0 + 6, y0 + TOP + 6, 0.0f, 0.0f, 32, 32, 16, 16, 16, 16,
                    FishingClient.shadow(0xB0));
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
