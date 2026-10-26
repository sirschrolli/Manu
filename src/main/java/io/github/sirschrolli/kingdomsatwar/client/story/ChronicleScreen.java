package io.github.sirschrolli.kingdomsatwar.client.story;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.story.Chapter;
import io.github.sirschrolli.kingdomsatwar.story.ChronicleData;
import io.github.sirschrolli.kingdomsatwar.story.Story;
import io.github.sirschrolli.kingdomsatwar.story.StoryProgress.Flag;
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
import org.jspecify.annotations.Nullable;

/**
 * Die Chronik des Siegels als aufgeschlagenes Buch: links die Kapitel (erreichte mit Haken, das aktuelle markiert,
 * künftige noch verborgen) und unten der Zustand des Siegels; rechts das gewählte Kapitel mit Erzählung, Zielen und
 * Hinweisen – bei den sechs Drachen eine Liste mit Zustand und ungefährer Lage jedes Nests. Lange Seiten lassen sich
 * mit dem Mausrad blättern.
 */
public class ChronicleScreen extends Screen {
    private static final Identifier BOOK = KingdomsAtWar.id("textures/gui/chronicle.png");
    private static final int TEX_W = 512;
    private static final int TEX_H = 256;
    private static final int W = 320;
    private static final int H = 200;
    private static final int LEFT_X = 20;
    private static final int RIGHT_X = 172;
    private static final int PAGE_W = 128;
    private static final int TOP = 16;
    private static final int BOTTOM = 186;
    private static final int ENTRY_Y = TOP + 24;
    private static final int ENTRY_H = 20;
    private static final int BODY_Y = TOP + 34;

    private static final int INK = 0xFF3A2414;
    private static final int MUTED = 0xFF7A6248;
    private static final int RED = 0xFF7A1E12;
    private static final int GOLD = 0xFF94600E;
    private static final int GREEN = 0xFF2E6B2E;
    private static final int PURPLE = 0xFF5B2A86;

    /** Die Abschnitte der Chronik (die Drachen-Kapitel III–VIII stehen gemeinsam). */
    private enum Part {
        PROLOG(Chapter.PROLOG, Chapter.PROLOG, "Prolog"),
        KINGDOM(Chapter.KINGDOM, Chapter.KINGDOM, "I"),
        CHRONICLE(Chapter.CHRONICLE, Chapter.CHRONICLE, "II"),
        DRAGONS(Chapter.DRAGON_1, Chapter.DRAGON_6, "III–VIII"),
        SEAL(Chapter.SEAL, Chapter.SEAL, "IX"),
        CROWN(Chapter.CROWN, Chapter.CROWN, "Epilog");

        final Chapter first;
        final Chapter last;
        final String numeral;

        Part(Chapter first, Chapter last, String numeral) {
            this.first = first;
            this.last = last;
            this.numeral = numeral;
        }

        String id() {
            return this == DRAGONS ? "dragons" : this.first.id();
        }

        static Part of(Chapter chapter) {
            for (Part part : values()) {
                if (chapter.ordinal() >= part.first.ordinal() && chapter.ordinal() <= part.last.ordinal()) {
                    return part;
                }
            }
            return PROLOG;
        }
    }

    /** Eine Zeile der rechten Seite. */
    private record Line(FormattedCharSequence text, int color, int indent, int icon, boolean iconLit, int gap) {}

    private final ChronicleData data;
    private Part selected;
    private final List<Line> lines = new ArrayList<>();
    private int contentHeight;
    private double scroll;
    private int left;
    private int top;

    public ChronicleScreen(ChronicleData data) {
        super(Component.translatable("gui.kingdomsatwar.chronicle.title"));
        this.data = data;
        this.selected = Part.of(data.current());
    }

    @Override
    protected void init() {
        this.left = (this.width - W) / 2;
        this.top = (this.height - H) / 2;
        this.buildPage();
    }

    /** Schlägt einen Abschnitt auf (0 = Prolog … 5 = Epilog), auch für die Screenshot-Automatik. */
    public void select(int index) {
        Part part = Part.values()[Math.max(0, Math.min(Part.values().length - 1, index))];
        if (this.unlocked(part)) {
            this.selected = part;
            this.buildPage();
        }
    }

    private boolean unlocked(Part part) {
        return part.first.ordinal() <= this.data.chapter();
    }

    private boolean done(Part part) {
        return part.last.ordinal() < this.data.chapter();
    }

    // =====================================================================================
    // Inhalt der rechten Seite
    // =====================================================================================

    private void buildPage() {
        this.lines.clear();
        this.scroll = 0;
        Part part = this.selected;
        Component story = Component.translatable("chronicle.kingdomsatwar." + part.id() + ".text");
        switch (part) {
            case PROLOG -> this.paragraph(story, INK, 0);
            case KINGDOM -> {
                this.heading(false);
                this.goal(Component.translatable("chronicle.kingdomsatwar.goal.town"), this.data.has(Flag.TOWN_FOUNDED) || this.done(part));
                this.goal(Component.translatable("chronicle.kingdomsatwar.goal.recruit"), this.data.has(Flag.RECRUITED) || this.done(part));
                this.story(story);
            }
            case CHRONICLE -> {
                this.heading(false);
                this.goal(Component.translatable("chronicle.kingdomsatwar.goal.read"), true);
                boolean found = this.data.has(Flag.FOUND_CIRCLE) || this.done(part);
                this.goal(Component.translatable("chronicle.kingdomsatwar.goal.find_circle"), found);
                this.gap(4);
                this.paragraph(this.circleHint(), PURPLE, 0);
                this.story(story);
            }
            case DRAGONS -> {
                this.heading(false);
                int defeated = this.data.defeated();
                int inserted = this.data.insertedCount();
                this.goal(Component.translatable("chronicle.kingdomsatwar.goal.defeat", defeated), defeated >= 6);
                this.goal(Component.translatable("chronicle.kingdomsatwar.goal.insert", inserted), inserted >= 6);
                this.gap(4);
                for (ChronicleData.Dragon dragon : this.data.dragons()) {
                    this.dragon(dragon);
                }
                this.gap(4);
                this.paragraph(this.circleHint(), PURPLE, 0);
                this.story(story);
            }
            case SEAL -> {
                this.heading(false);
                this.goal(Component.translatable("chronicle.kingdomsatwar.goal.enter_shadow"), this.data.has(Flag.ENTERED_SHADOW));
                this.gap(4);
                this.paragraph(this.gateHint(), PURPLE, 0);
                this.story(story);
            }
            case CROWN -> {
                this.heading(false);
                this.goal(Component.translatable("chronicle.kingdomsatwar.goal.crown"), this.data.has(Flag.CROWNED));
                this.story(story);
            }
        }
        int y = 0;
        for (Line line : this.lines) {
            y += line.gap() + 10;
        }
        this.contentHeight = y;
    }

    /** Die Erzählung des Kapitels unter Zielen und Hinweisen, durch eine Zierlinie abgesetzt. */
    private void story(Component story) {
        this.gap(6);
        this.lines.add(new Line(FormattedCharSequence.EMPTY, INK, 0, 200, false, 0));
        this.gap(4);
        this.paragraph(story, INK, 0);
    }

    private void paragraph(Component text, int color, int indent) {
        List<FormattedCharSequence> split = this.font.split(text, PAGE_W - indent);
        for (int i = 0; i < split.size(); i++) {
            this.lines.add(new Line(split.get(i), color, indent, -1, false, 0));
        }
    }

    private void gap(int pixels) {
        this.lines.add(new Line(FormattedCharSequence.EMPTY, INK, 0, -1, false, pixels - 10));
    }

    private void heading(boolean spaced) {
        if (spaced) {
            this.gap(6);
        }
        this.lines.add(new Line(Component.translatable("chronicle.kingdomsatwar.goals").withStyle(ChatFormatting.BOLD).getVisualOrderText(),
                RED, 0, -1, false, 0));
    }

    /** Ein Ziel mit Kästchen (Symbol 100/101 = leer/abgehakt). */
    private void goal(Component text, boolean reached) {
        List<FormattedCharSequence> split = this.font.split(text, PAGE_W - 12);
        for (int i = 0; i < split.size(); i++) {
            this.lines.add(new Line(split.get(i), reached ? GREEN : INK, 12, i == 0 ? (reached ? 101 : 100) : -1, false, i == 0 ? 2 : 0));
        }
    }

    /** Ein Drache: Zeichen seines Elements, Name (oder Gerücht) und darunter Zustand oder Hinweis. */
    private void dragon(ChronicleData.Dragon dragon) {
        DragonType type = dragon.element();
        Component name = dragon.discovered()
                ? Component.translatable("boss.kingdomsatwar." + type.id())
                : Component.translatable("lair.kingdomsatwar." + type.id() + ".unknown");
        List<FormattedCharSequence> nameLines = this.font.split(name.copy().withStyle(ChatFormatting.BOLD), PAGE_W - 16);
        for (int i = 0; i < nameLines.size(); i++) {
            this.lines.add(new Line(nameLines.get(i), INK, 16, i == 0 ? type.ordinal() : -1, dragon.phase() == 2, i == 0 ? 3 : 0));
        }
        Component status;
        int color;
        if (dragon.inserted()) {
            status = Component.translatable("chronicle.kingdomsatwar.dragon.inserted");
            color = GREEN;
        } else if (dragon.phase() == 2) {
            status = Component.translatable("chronicle.kingdomsatwar.dragon.defeated");
            color = GOLD;
        } else if (dragon.phase() == 1) {
            status = Component.translatable("chronicle.kingdomsatwar.dragon.awake");
            color = RED;
        } else if (dragon.discovered()) {
            status = Component.translatable("chronicle.kingdomsatwar.dragon.sleeps",
                    Component.translatable("lair.kingdomsatwar." + type.id() + ".name"), this.where(dragon.place(), true));
            color = MUTED;
        } else {
            status = Component.translatable("chronicle.kingdomsatwar.dragon.rumor",
                    Component.translatable("lair.kingdomsatwar." + type.id() + ".hint"), this.where(dragon.place(), false));
            color = MUTED;
        }
        this.paragraph(status, color, 16);
    }

    private Component circleHint() {
        ChronicleData.Place place = this.data.circle();
        if (place.direction() == ChronicleData.HERE) {
            return Component.translatable("chronicle.kingdomsatwar.circle.here");
        }
        if (place.direction() == ChronicleData.UNKNOWN) {
            return Component.translatable("chronicle.kingdomsatwar.circle.unknown");
        }
        return Component.translatable("chronicle.kingdomsatwar.circle.distance", round(place.distance()),
                Component.translatable(Story.directionKey(place.direction())));
    }

    private Component gateHint() {
        ChronicleData.Place place = this.data.circle();
        if (place.direction() == ChronicleData.HERE) {
            return Component.translatable("chronicle.kingdomsatwar.gate.here");
        }
        if (place.direction() == ChronicleData.UNKNOWN) {
            return Component.translatable("chronicle.kingdomsatwar.gate.unknown");
        }
        return Component.translatable("chronicle.kingdomsatwar.gate.distance", round(place.distance()),
                Component.translatable(Story.directionKey(place.direction())));
    }

    /** Lage eines Nests: genau (wenn entdeckt) oder nur ungefähr („weit im Norden“). */
    private Component where(ChronicleData.Place place, boolean exact) {
        if (place.direction() == ChronicleData.HERE) {
            return Component.translatable("chronicle.kingdomsatwar.where.here");
        }
        if (place.direction() == ChronicleData.UNKNOWN) {
            return Component.translatable("chronicle.kingdomsatwar.where.unknown");
        }
        if (place.direction() == ChronicleData.NOWHERE) {
            return Component.translatable("chronicle.kingdomsatwar.where.nowhere");
        }
        Component dir = Component.translatable(Story.directionKey(place.direction()));
        if (exact) {
            return Component.translatable("chronicle.kingdomsatwar.where.exact", round(place.distance()), dir);
        }
        String key = place.distance() < 500 ? "near" : place.distance() < 1200 ? "some" : "far";
        return Component.translatable("chronicle.kingdomsatwar.where." + key, dir);
    }

    private static int round(int distance) {
        return Math.max(10, Math.round(distance / 10.0f) * 10);
    }

    // =====================================================================================
    // Eingaben
    // =====================================================================================

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        Part part = this.partAt(event.x(), event.y());
        if (part != null && part != this.selected && this.unlocked(part)) {
            this.selected = part;
            this.buildPage();
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0f));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private @Nullable Part partAt(double mx, double my) {
        for (Part part : Part.values()) {
            int x = this.left + LEFT_X - 4;
            int y = this.top + ENTRY_Y + part.ordinal() * ENTRY_H;
            if (mx >= x && mx < x + PAGE_W + 8 && my >= y && my < y + ENTRY_H - 1) {
                return part;
            }
        }
        return null;
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
        if (this.minecraft.options.keyInventory.matches(event)) {
            this.onClose();
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
        g.blit(RenderPipelines.GUI_TEXTURED, BOOK, this.left, this.top, 0.0f, 0.0f, W, H, TEX_W, TEX_H);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractRenderState(g, mouseX, mouseY, a);
        this.drawLeftPage(g, mouseX, mouseY);
        this.drawRightPage(g);
    }

    private void drawLeftPage(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x0 = this.left + LEFT_X;
        int y0 = this.top;
        Component title = this.title.copy().withStyle(ChatFormatting.BOLD);
        g.text(this.font, title, x0 + PAGE_W / 2 - this.font.width(title) / 2, y0 + TOP + 1, RED, false);
        g.blit(RenderPipelines.GUI_TEXTURED, BOOK, x0 + PAGE_W / 2 - 55, y0 + TOP + 12, 320.0f, 94.0f, 110, 7, TEX_W, TEX_H);

        Part hovered = this.partAt(mouseX, mouseY);
        for (Part part : Part.values()) {
            int y = y0 + ENTRY_Y + part.ordinal() * ENTRY_H;
            boolean open = this.unlocked(part);
            if (part == this.selected) {
                g.blit(RenderPipelines.GUI_TEXTURED, BOOK, x0 - 4, y, 0.0f, 200.0f, PAGE_W + 8, 18, TEX_W, TEX_H);
            } else if (part == hovered && open) {
                g.blit(RenderPipelines.GUI_TEXTURED, BOOK, x0 - 4, y, 0.0f, 220.0f, PAGE_W + 8, 18, TEX_W, TEX_H);
            }
            Component label = part == Part.PROLOG ? Component.translatable("chapter.kingdomsatwar.label.prolog")
                    : part == Part.CROWN ? Component.translatable("chapter.kingdomsatwar.label.epilog")
                    : Component.translatable("chapter.kingdomsatwar.label", part.numeral);
            g.pose().pushMatrix();
            g.pose().translate(x0 + 2, y + 2);
            g.pose().scale(0.75f, 0.75f);
            g.text(this.font, label.getString().toUpperCase(java.util.Locale.ROOT), 0, 0, open ? GOLD : MUTED, false);
            g.pose().popMatrix();
            MutableComponent name = open
                    ? Component.translatable(part == Part.DRAGONS ? "chapter.kingdomsatwar.dragons" : part.first.titleKey())
                    : Component.literal("· · ·");
            int maxW = PAGE_W - 16;
            int width = this.font.width(name);
            float scale = width > maxW ? Math.max(0.75f, (float) maxW / width) : 1.0f;
            int color = !open ? MUTED : part == this.selected ? RED : INK;
            g.pose().pushMatrix();
            g.pose().translate(x0 + 2, y + 10);
            g.pose().scale(scale, scale);
            g.text(this.font, name, 0, 0, color, false);
            g.pose().popMatrix();
            int icon = !open ? 2 : this.done(part) ? 0 : 1;
            g.blit(RenderPipelines.GUI_TEXTURED, BOOK, x0 + PAGE_W - 10, y + 6, 320.0f + icon * 10, 0.0f, 9, 9, TEX_W, TEX_H);
        }

        // Zustand des Siegels: sechs Zeichen, leuchtend, wenn der Splitter steckt
        int sy = y0 + BOTTOM - 23;
        Component label = this.data.sealBroken()
                ? Component.translatable("chronicle.kingdomsatwar.seal.broken")
                : Component.translatable("chronicle.kingdomsatwar.seal.count", this.data.insertedCount());
        g.text(this.font, label, x0 + PAGE_W / 2 - this.font.width(label) / 2, sy + 14, this.data.sealBroken() ? PURPLE : MUTED, false);
        int iconsW = 6 * 14 - 2;
        int ix = x0 + PAGE_W / 2 - iconsW / 2;
        for (DragonType type : io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLayout.RING) {
            boolean lit = this.data.sealBroken() || this.data.dragons().stream().anyMatch(d -> d.element() == type && d.inserted());
            g.blit(RenderPipelines.GUI_TEXTURED, BOOK, ix, sy, 320.0f + type.ordinal() * 13, lit ? 12.0f : 25.0f, 12, 12, TEX_W, TEX_H);
            ix += 14;
        }
    }

    private void drawRightPage(GuiGraphicsExtractor g) {
        int x0 = this.left + RIGHT_X;
        int y0 = this.top;
        Part part = this.selected;
        Component label = part == Part.PROLOG ? Component.translatable("chapter.kingdomsatwar.label.prolog")
                : part == Part.CROWN ? Component.translatable("chapter.kingdomsatwar.label.epilog")
                : Component.translatable("chapter.kingdomsatwar.label", part.numeral);
        g.text(this.font, label, x0 + PAGE_W / 2 - this.font.width(label) / 2, y0 + TOP, MUTED, false);
        Component title = Component.translatable(part == Part.DRAGONS ? "chapter.kingdomsatwar.dragons" : part.first.titleKey())
                .withStyle(ChatFormatting.BOLD);
        FormattedCharSequence t = this.font.split(title, PAGE_W).getFirst();
        g.text(this.font, t, x0 + PAGE_W / 2 - this.font.width(t) / 2, y0 + TOP + 11, RED, false);
        g.blit(RenderPipelines.GUI_TEXTURED, BOOK, x0 + PAGE_W / 2 - 55, y0 + TOP + 22, 320.0f, 94.0f, 110, 7, TEX_W, TEX_H);

        int areaTop = y0 + BODY_Y;
        int areaBottom = y0 + BOTTOM;
        g.enableScissor(x0 - 2, areaTop - 1, x0 + PAGE_W + 4, areaBottom);
        int y = areaTop - (int) this.scroll;
        for (Line line : this.lines) {
            y += line.gap();
            if (y > areaTop - 12 && y < areaBottom) {
                if (line.icon() == 200) {
                    g.blit(RenderPipelines.GUI_TEXTURED, BOOK, x0 + PAGE_W / 2 - 55, y + 1, 320.0f, 94.0f, 110, 7, TEX_W, TEX_H);
                } else if (line.icon() >= 100) {
                    g.blit(RenderPipelines.GUI_TEXTURED, BOOK, x0 + 1, y, 350.0f + (line.icon() - 100) * 10, 0.0f, 9, 9, TEX_W, TEX_H);
                } else if (line.icon() >= 0) {
                    g.blit(RenderPipelines.GUI_TEXTURED, BOOK, x0, y - 2, 320.0f + line.icon() * 13, line.iconLit() ? 12.0f : 25.0f, 12, 12,
                            TEX_W, TEX_H);
                }
                g.text(this.font, line.text(), x0 + line.indent(), y, line.color(), false);
            }
            y += 10;
        }
        g.disableScissor();
        // Blätter-Hinweis, wenn die Seite länger ist
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
