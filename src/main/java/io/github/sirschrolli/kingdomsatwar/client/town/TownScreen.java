package io.github.sirschrolli.kingdomsatwar.client.town;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.renderer.CitizenRenderer;
import io.github.sirschrolli.kingdomsatwar.economy.Coin;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.economy.Reputation;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import io.github.sirschrolli.kingdomsatwar.economy.town.Material;
import io.github.sirschrolli.kingdomsatwar.economy.town.Profession;
import io.github.sirschrolli.kingdomsatwar.economy.town.SiteSections;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownActions;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownTier;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownView;
import io.github.sirschrolli.kingdomsatwar.network.TownActionPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

/**
 * Das Stadtfenster am Stadtbanner: ein Pergament im Eichenrahmen mit fünf Reitern – Übersicht (Stufe, Betten, Ansehen,
 * Baustellen, Name), Anheuern (die acht Berufe mit Lohn), Bewohner (wer hier wohnt und was er gerade tut), Bauen (die
 * Baupläne) und Krone (die Voraussetzungen der Krönung). Alles, was hier angeklickt wird, prüft der Server noch einmal.
 */
public class TownScreen extends Screen {
    private static final Identifier BACKGROUND = KingdomsAtWar.id("textures/gui/town.png");
    static final int WIDTH = 320;
    static final int HEIGHT = 210;

    // Farben auf dem Pergament
    static final int TEXT = 0xFF3B2611;
    static final int MUTED = 0xFF7A6448;
    static final int GOOD = 0xFF2E6B2E;
    static final int BAD = 0xFFA83224;
    static final int GOLD_TEXT = 0xFFF2CE6B;
    static final int RIBBON_TEXT = 0xFFE9DDBF;
    static final int LINE = 0xFFB89C69;
    static final int LINE_LIGHT = 0xFFF3E6C4;
    static final int CARD = 0x26FFF6DC;
    static final int CARD_HOVER = 0x4DFFF3CF;
    static final int CARD_BORDER = 0xFFC2A472;

    /** Inhalt: unter den Reitern bis zum unteren Rahmen. */
    private static final int CONTENT_X = 9;
    private static final int CONTENT_Y = 52;
    private static final int CONTENT_W = 302;
    private static final int CONTENT_H = 149;
    private static final int ROW_H = 17;
    private static final int VISIBLE_ROWS = 7;

    enum Tab {
        OVERVIEW("overview"), HIRE("hire"), RESIDENTS("residents"), BUILD("build"), CROWN("crown");

        final String id;

        Tab(String id) {
            this.id = id;
        }

        Component title() {
            return Component.translatable("gui.kingdomsatwar.town.tab." + this.id);
        }

        ItemStack icon() {
            return switch (this) {
                case OVERVIEW -> new ItemStack(ModBlocks.TOWN_BANNER.get());
                case HIRE -> new ItemStack(ModItems.GOLD_COIN.get());
                case RESIDENTS -> new ItemStack(Items.BED.red());
                case BUILD -> new ItemStack(ModItems.BLUEPRINT.get());
                case CROWN -> new ItemStack(ModItems.GEMSTONE_CROWN.get());
            };
        }
    }

    /** Eine anklickbare Fläche (wird bei jedem Bild neu eingetragen). */
    private record Hotspot(int x, int y, int w, int h, boolean active, Runnable action) {
        boolean contains(double mx, double my) {
            return mx >= this.x && mx < this.x + this.w && my >= this.y && my < this.y + this.h;
        }
    }

    private final BlockPos banner;
    private TownView view;
    private Tab tab = Tab.OVERVIEW;
    private int left;
    private int top;
    private @Nullable EditBox nameBox;
    /** Wird gerade der Name der Siedlung bearbeitet (Eingabefeld im blauen Band)? */
    private boolean editingName;
    private final List<Hotspot> hotspots = new ArrayList<>();
    private int residentScroll;
    /** Bürger, dessen Entlassung gerade bestätigt werden soll (zweiter Klick), und wie lange noch. */
    private int confirmDismiss = -1;
    private int confirmTicks;
    /** Dragon Architect: eigene Baupläne statt der Baupläne der Siedlung zeigen (im Reiter Bauen). */
    private boolean showPlans;
    private List<io.github.sirschrolli.kingdomsatwar.architect.PlanInfo> plans =
            io.github.sirschrolli.kingdomsatwar.client.architect.ArchitectClient.plans();
    private int planScroll;
    /** Dragon Architect: die Schematiken des Servers zum Einlesen zeigen (statt der eigenen Pläne). */
    private boolean showImport;
    private List<io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles.Entry> imports = List.of();
    private boolean importsLoaded;
    private int importScroll;
    /** Plan, dessen Löschen gerade bestätigt werden soll (zweiter Klick). */
    private @Nullable String confirmDelete;
    private @Nullable Component notice;
    private boolean noticeGood;
    private int noticeTicks;
    private int ticks;

    public TownScreen(BlockPos banner, TownView view) {
        super(Component.literal(view.name()));
        this.banner = banner;
        this.view = view;
    }

    public BlockPos banner() {
        return this.banner;
    }

    /** Für Vorführungen: einen Reiter auswählen (0 = Übersicht … 4 = Krone). */
    public void selectTab(int index) {
        this.tab = Tab.values()[Mth.clamp(index, 0, Tab.values().length - 1)];
        this.showPlans = false;
        this.updateWidgets();
    }

    /** Für Vorführungen: im Reiter Bauen die eigenen Baupläne zeigen. */
    public void showPlans() {
        this.tab = Tab.BUILD;
        this.showPlans = true;
        this.showImport = false;
        this.send(TownActions.PLANS, 0, "");
        this.updateWidgets();
    }

    /** Neue Liste der Schematiken zum Einlesen vom Server. */
    public void updateImports(List<io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles.Entry> files) {
        this.imports = files;
        this.importsLoaded = true;
        this.importScroll = Mth.clamp(this.importScroll, 0, Math.max(0, files.size() - IMPORT_ROWS));
    }

    /** Für Vorführungen: die Schematiken zum Einlesen zeigen. */
    public void showImport() {
        this.tab = Tab.BUILD;
        this.showPlans = true;
        this.showImport = true;
        this.send(TownActions.IMPORT_LIST, 0, "");
        this.updateWidgets();
    }

    /** Neue Liste der eigenen Baupläne vom Server. */
    public void updatePlans(List<io.github.sirschrolli.kingdomsatwar.architect.PlanInfo> plans) {
        this.plans = plans;
        this.planScroll = Mth.clamp(this.planScroll, 0, Math.max(0, plans.size() - PLAN_ROWS));
    }

    /** Neue Daten vom Server (nach einer Handlung); Reiter und Bildlauf bleiben. */
    public void update(TownView view, @Nullable Component notice, boolean good) {
        this.view = view;
        if (notice != null) {
            this.notice = notice;
            this.noticeGood = good;
            this.noticeTicks = 80;
        }
        if (this.nameBox != null && !this.nameBox.isFocused()) {
            this.nameBox.setValue(view.name());
        }
        this.residentScroll = Mth.clamp(this.residentScroll, 0, Math.max(0, view.residents().size() - VISIBLE_ROWS));
    }

    @Override
    protected void init() {
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;
        this.nameBox = new EditBox(this.font, this.left + 30, this.top + 11, 170, 14, Component.translatable("gui.kingdomsatwar.town.name_hint"));
        this.nameBox.setMaxLength(32);
        this.nameBox.setValue(this.view.name());
        this.nameBox.setHint(Component.translatable("gui.kingdomsatwar.town.name_hint"));
        this.addRenderableWidget(this.nameBox);
        this.updateWidgets();
        // die eigenen Baupläne frisch vom Server (für die Karte „Eigene Pläne“)
        this.send(TownActions.PLANS, 0, "");
    }

    private void updateWidgets() {
        if (this.nameBox != null) {
            this.nameBox.visible = this.editingName && this.view.isOwner();
            if (!this.nameBox.visible) {
                this.nameBox.setFocused(false);
            }
        }
    }

    // =====================================================================================
    // Ablauf
    // =====================================================================================

    @Override
    public void tick() {
        this.ticks++;
        if (this.noticeTicks > 0) {
            this.noticeTicks--;
        }
        if (this.confirmTicks > 0 && --this.confirmTicks == 0) {
            this.confirmDismiss = -1;
            this.confirmDelete = null;
        }
        // alle zwei Sekunden auffrischen (Bewohner wandern, Baustellen wachsen)
        if (this.ticks % 40 == 0) {
            this.send(TownActions.REFRESH, 0, "");
        }
        if (this.minecraft.player == null || this.minecraft.level == null
                || this.minecraft.player.distanceToSqr(Vec3.atCenterOf(this.banner)) > TownActions.RANGE * TownActions.RANGE
                || !this.minecraft.level.getBlockState(this.banner).is(ModBlocks.TOWN_BANNER.get())) {
            this.onClose();
        }
    }

    private void send(int action, int arg, String text) {
        ClientPacketDistributor.sendToServer(new TownActionPayload(this.banner, action, arg, text));
    }

    private void click() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        for (Hotspot spot : List.copyOf(this.hotspots)) {
            if (spot.contains(event.x(), event.y())) {
                if (spot.active()) {
                    this.click();
                    spot.action().run();
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.tab == Tab.RESIDENTS) {
            int max = Math.max(0, this.view.residents().size() - VISIBLE_ROWS);
            this.residentScroll = Mth.clamp(this.residentScroll - (int) Math.signum(scrollY), 0, max);
            return true;
        }
        if (this.tab == Tab.BUILD && this.showPlans && this.showImport) {
            int max = Math.max(0, this.imports.size() - IMPORT_ROWS);
            this.importScroll = Mth.clamp(this.importScroll - (int) Math.signum(scrollY), 0, max);
            return true;
        }
        if (this.tab == Tab.BUILD && this.showPlans) {
            int max = Math.max(0, this.plans.size() - PLAN_ROWS);
            this.planScroll = Mth.clamp(this.planScroll - (int) Math.signum(scrollY), 0, max);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.editingName && this.nameBox != null) {
            if (event.isConfirmation()) {
                this.rename();
                return true;
            }
            if (event.isEscape()) {
                this.editingName = false;
                this.updateWidgets();
                return true;
            }
            return this.nameBox.keyPressed(event) || this.nameBox.canConsumeInput() || super.keyPressed(event);
        }
        if (this.minecraft.options.keyInventory.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    private void startRename() {
        if (this.nameBox != null) {
            this.editingName = true;
            this.updateWidgets();
            this.nameBox.setValue(this.view.name());
            this.setFocused(this.nameBox);
            this.nameBox.setFocused(true);
        }
    }

    private void rename() {
        if (this.nameBox != null) {
            String name = this.nameBox.getValue().strip();
            if (!name.isEmpty() && !name.equals(this.view.name())) {
                this.send(TownActions.RENAME, 0, name);
            }
            this.nameBox.setFocused(false);
        }
        this.editingName = false;
        this.updateWidgets();
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
        g.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.left, this.top, 0.0f, 0.0f, WIDTH, HEIGHT, WIDTH, HEIGHT);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        this.hotspots.clear();
        this.updateWidgets();
        this.drawHeader(g, mouseX, mouseY);
        this.drawTabs(g, mouseX, mouseY);
        int x = this.left + CONTENT_X;
        int y = this.top + CONTENT_Y;
        switch (this.tab) {
            case OVERVIEW -> this.drawOverview(g, x, y, mouseX, mouseY);
            case HIRE -> this.drawHire(g, x, y, mouseX, mouseY);
            case RESIDENTS -> this.drawResidents(g, x, y, mouseX, mouseY);
            case BUILD -> {
                if (this.showPlans && this.showImport) {
                    this.drawImports(g, x, y, mouseX, mouseY);
                } else if (this.showPlans) {
                    this.drawPlans(g, x, y, mouseX, mouseY);
                } else {
                    this.drawBuild(g, x, y, mouseX, mouseY);
                }
            }
            case CROWN -> this.drawCrown(g, x, y, mouseX, mouseY);
        }
        Runnable overlay = this.overlay;
        this.overlay = null;
        if (overlay != null) {
            overlay.run();
        }
        if (this.notice != null && this.noticeTicks > 0) {
            int w = this.font.width(this.notice) + 12;
            int nx = this.left + (WIDTH - w) / 2;
            int ny = this.top + HEIGHT - 22;
            int alpha = Math.min(255, this.noticeTicks * 12);
            g.fill(nx, ny, nx + w, ny + 13, (Math.min(alpha, 0xE0) << 24) | (this.noticeGood ? 0x1E3A1E : 0x4A1612));
            g.outline(nx, ny, w, 13, (alpha << 24) | (this.noticeGood ? 0x6FA35F : 0xD0704F));
            g.text(this.font, this.notice, nx + 6, ny + 3, (alpha << 24) | 0xF3E6C4, false);
        }
        super.extractRenderState(g, mouseX, mouseY, a);
    }

    /** Wird über allem anderen gezeichnet (die Karte einer Großbaustelle). */
    private @Nullable Runnable overlay;

    /**
     * Die Karte einer Großbaustelle: jeder Abschnitt ein Feld (Norden oben) – offen, Rohbau, fertig oder gerade in Arbeit –,
     * dazu wie viele fertig sind und wie viele Baumeister daran arbeiten. Auf Blaupausen-Papier wie die Baupläne.
     */
    private void drawSiteMap(GuiGraphicsExtractor g, TownView.SiteView site, int mouseX, int mouseY) {
        int w = Math.max(1, site.width());
        int d = Math.max(1, site.map().length / w);
        int cell = Mth.clamp(112 / Math.max(w, d), 2, 8);
        int mapW = w * cell;
        int mapH = d * cell;
        Component title = Component.translatable("gui.kingdomsatwar.town.site.title").withStyle(ChatFormatting.BOLD);
        Component done = Component.translatable("gui.kingdomsatwar.town.site.done", site.done(), site.map().length);
        Component crew = Component.translatable(site.builders() == 1 ? "gui.kingdomsatwar.town.site.builder" : "gui.kingdomsatwar.town.site.builders",
                site.builders());
        int textW = Math.max(this.font.width(title), Math.max(this.font.width(done), this.font.width(crew)));
        int pw = Math.max(Math.max(mapW, textW), 150) + 14;
        int ph = 38 + mapH + 18;
        int px = Math.min(mouseX + 14, this.width - pw - 4);
        int py = Mth.clamp(mouseY - ph / 2, 4, this.height - ph - 4);
        // Blaupausen-Papier
        g.fill(px, py, px + pw, py + ph, 0xF21D3B5C);
        for (int gx = px + 6; gx < px + pw; gx += 6) {
            g.fill(gx, py + 1, gx + 1, py + ph - 1, 0x1EA9C8F0);
        }
        for (int gy = py + 6; gy < py + ph; gy += 6) {
            g.fill(px + 1, gy, px + pw - 1, gy + 1, 0x1EA9C8F0);
        }
        g.outline(px, py, pw, ph, 0xFF9DBBE4);
        g.outline(px + 2, py + 2, pw - 4, ph - 4, 0x509DBBE4);
        g.text(this.font, title, px + 7, py + 6, 0xFFF2C23A, false);
        g.text(this.font, done, px + 7, py + 17, 0xFFF3F7FF, false);
        g.text(this.font, crew, px + 7, py + 27, 0xFFBFD5F2, false);
        int mx = px + (pw - mapW) / 2;
        int my = py + 38;
        g.fill(mx - 1, my - 1, mx + mapW + 1, my + mapH + 1, 0xFF0F2238);
        boolean blink = (System.currentTimeMillis() / 400L) % 2L == 0L;
        for (int i = 0; i < site.map().length; i++) {
            int cx = mx + (i % w) * cell;
            int cy = my + (i / w) * cell;
            int color = switch (site.map()[i]) {
                case SiteSections.FRAME -> 0xFFD9A441;
                case SiteSections.DONE -> 0xFF6FBF5A;
                case 3 -> blink ? 0xFFF3F7FF : 0xFFBFD5F2;
                default -> 0xFF3A5A7E;
            };
            g.fill(cx, cy, cx + Math.max(1, cell - (cell > 3 ? 1 : 0)), cy + Math.max(1, cell - (cell > 3 ? 1 : 0)), color);
        }
        // Zeichenerklärung
        int ly = my + mapH + 6;
        int lx = px + 7;
        int[] colors = {0xFF3A5A7E, 0xFFD9A441, 0xFF6FBF5A, 0xFFF3F7FF};
        String[] keys = {"open", "frame", "done_key", "busy"};
        for (int k = 0; k < 4; k++) {
            g.fill(lx, ly + 1, lx + 6, ly + 7, colors[k]);
            Component label = Component.translatable("gui.kingdomsatwar.town.site." + keys[k]);
            g.text(this.font, label, lx + 8, ly, 0xFFBFD5F2, false);
            lx += 8 + this.font.width(label) + 6;
        }
    }

    private void drawHeader(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x0 = this.left;
        int y0 = this.top;
        this.itemScaled(g, new ItemStack(ModBlocks.TOWN_BANNER.get()), x0 + 11, y0 + 10, 1.0f);
        if (this.editingName) {
            this.woodButton(g, x0 + 204, y0 + 11, 16, 14, Component.literal("✔"), true, mouseX, mouseY, this::rename,
                    List.of(Component.translatable("gui.kingdomsatwar.town.rename")));
        } else {
            MutableComponent name = Component.literal(this.view.name()).withStyle(ChatFormatting.BOLD);
            g.text(this.font, name, x0 + 31, y0 + 14, GOLD_TEXT, true);
            int nx = x0 + 35 + this.font.width(name);
            if (this.view.crowned()) {
                this.itemScaled(g, new ItemStack(ModItems.GEMSTONE_CROWN.get()), nx, y0 + 11, 0.75f);
                nx += 15;
            }
            if (this.view.isOwner()) {
                this.woodButton(g, nx, y0 + 11, 14, 14, Component.literal("✎"), true, mouseX, mouseY, this::startRename,
                        List.of(Component.translatable("gui.kingdomsatwar.town.rename")));
            }
        }
        Component tier = Component.translatable(this.view.tierValue().translationKey());
        g.text(this.font, tier, x0 + WIDTH - 12 - this.font.width(tier), y0 + 14, RIBBON_TEXT, true);
    }

    private void drawTabs(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        Tab[] tabs = this.view.isOwner() ? Tab.values() : new Tab[] {Tab.OVERVIEW, Tab.RESIDENTS, Tab.BUILD};
        int[] widths = new int[tabs.length];
        int total = 0;
        for (int i = 0; i < tabs.length; i++) {
            widths[i] = 4 + 10 + 3 + this.font.width(tabs[i].title()) + 4;
            total += widths[i];
        }
        int gap = 2;
        int extra = Math.max(0, CONTENT_W - total - gap * (tabs.length - 1)) / tabs.length;
        int x = this.left + CONTENT_X;
        int y = this.top + 32;
        for (int i = 0; i < tabs.length; i++) {
            Tab t = tabs[i];
            int w = widths[i] + extra;
            boolean active = t == this.tab;
            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 17;
            int fill = active ? 0xFFF1E2BC : hover ? 0xFFE3CE9E : 0xFFD6BF8C;
            g.fill(x, y, x + w, y + 17, fill);
            g.fill(x, y, x + w, y + 1, active ? 0xFF8A6A3E : 0xFF9C7F52);
            g.fill(x, y, x + 1, y + 17, active ? 0xFF8A6A3E : 0xFF9C7F52);
            g.fill(x + w - 1, y, x + w, y + 17, active ? 0xFF8A6A3E : 0xFF9C7F52);
            if (active) {
                g.fill(x + 1, y + 1, x + w - 1, y + 2, 0xFFFFF6DA);
            } else {
                g.fill(x, y + 16, x + w, y + 17, 0xFF9C7F52);
            }
            int inner = 10 + 3 + this.font.width(t.title());
            int ix = x + (w - inner) / 2;
            this.itemScaled(g, t.icon(), ix, y + 3, 0.625f);
            g.text(this.font, t.title(), ix + 13, y + 5, active ? TEXT : MUTED, false);
            if (!active) {
                this.hotspots.add(new Hotspot(x, y, w, 17, true, () -> {
                    this.tab = t;
                    this.confirmDismiss = -1;
                    this.showPlans = false;
                    this.showImport = false;
                    this.updateWidgets();
                }));
            }
            x += w + gap;
        }
        // Linie unter den Reitern (unterbrochen beim aktiven)
        g.fill(this.left + CONTENT_X, y + 17, this.left + CONTENT_X + CONTENT_W, y + 18, 0x40765A34);
    }

    // ---------------------------------------------------------------- Übersicht

    private void drawOverview(GuiGraphicsExtractor g, int x0, int y0, int mouseX, int mouseY) {
        TownView v = this.view;
        int x = x0 + 5;
        int y = y0 + 4;
        if (!v.isOwner()) {
            g.text(this.font, Component.translatable("gui.kingdomsatwar.town.foreign", v.owner()), x, y, MUTED, false);
            y += 13;
        }
        // Stufe mit Fortschritt zur nächsten
        TownTier tier = v.tierValue();
        g.text(this.font, Component.translatable("gui.kingdomsatwar.town.tier").withStyle(ChatFormatting.ITALIC), x, y, MUTED, false);
        g.text(this.font, Component.translatable(tier.translationKey()).withStyle(ChatFormatting.BOLD), x + 46, y, TEXT, false);
        y += 12;
        int residents = v.residents().size();
        if (tier == TownTier.CITY) {
            this.bar(g, x, y, 136, 6, 1.0f, 0xFFC89A2E);
            y += 9;
            y = this.wrapped(g, Component.translatable("gui.kingdomsatwar.town.max_tier", residents), x, y, 140, MUTED);
        } else {
            TownTier next = tier.next();
            float f = (residents - tier.residents()) / (float) Math.max(1, next.residents() - tier.residents());
            this.bar(g, x, y, 136, 6, f, 0xFF5E8C3A);
            y += 9;
            y = this.wrapped(g, Component.translatable("gui.kingdomsatwar.town.next_tier", residents, next.residents() - residents,
                    Component.translatable(next.translationKey())), x, y, 140, MUTED);
        }
        y += 5;
        // Betten, Gebiet, Herr
        this.itemScaled(g, new ItemStack(Items.BED.red()), x, y - 3, 0.75f);
        g.text(this.font, Component.translatable("gui.kingdomsatwar.town.beds", residents, v.capacity()), x + 15, y, TEXT, false);
        y += 13;
        this.itemScaled(g, new ItemStack(Items.FILLED_MAP), x, y - 3, 0.75f);
        g.text(this.font, Component.translatable("gui.kingdomsatwar.town.radius", v.radius()), x + 15, y, TEXT, false);
        y += 13;
        this.itemScaled(g, new ItemStack(ModBlocks.THRONE.get()), x, y - 3, 0.75f);
        g.text(this.font, Component.translatable("gui.kingdomsatwar.town.owner", v.owner()), x + 15, y, TEXT, false);
        y += 13;
        // Essen im Lager: für wie viele Tage es die Bürger satt macht
        this.itemScaled(g, new ItemStack(Items.BREAD), x, y - 3, 0.75f);
        int days = v.foodDays();
        boolean hungry = v.residents().stream().anyMatch(r -> r.state() == 4);
        Component food = v.residents().isEmpty() ? Component.translatable("gui.kingdomsatwar.town.food.none_needed")
                : Component.translatable(days == 1 ? "gui.kingdomsatwar.town.food.day" : "gui.kingdomsatwar.town.food.days", days);
        g.text(this.font, food, x + 15, y, hungry || days < 1 && !v.residents().isEmpty() ? BAD : days < 3 ? 0xFF8A6A10 : TEXT, false);
        if (mouseX >= x && mouseX < x + 140 && mouseY >= y - 3 && mouseY < y + 10) {
            List<FormattedCharSequence> tip = new ArrayList<>(this.font.split(Component.translatable("gui.kingdomsatwar.town.food.hint",
                    v.food() / io.github.sirschrolli.kingdomsatwar.economy.town.TownFood.MEAL), 220));
            g.setTooltipForNextFrame(this.font, tip, mouseX, mouseY);
        }
        y += 15;
        // Ansehen
        Reputation.Rank rank = v.rank();
        g.text(this.font, Component.translatable("gui.kingdomsatwar.town.reputation").withStyle(ChatFormatting.ITALIC), x, y, MUTED, false);
        g.text(this.font, Component.translatable(rank.translationKey()).withStyle(ChatFormatting.BOLD), x + 46, y, TEXT, false);
        y += 12;
        if (rank == Reputation.Rank.LEGENDARY) {
            this.bar(g, x, y, 136, 6, 1.0f, 0xFFC89A2E);
            y += 9;
            y = this.wrapped(g, Component.translatable("gui.kingdomsatwar.town.reputation_max", v.reputation()), x, y, 140, MUTED);
        } else {
            Reputation.Rank next = rank.next();
            float f = (v.reputation() - rank.points()) / (float) Math.max(1, next.points() - rank.points());
            this.bar(g, x, y, 136, 6, f, 0xFFB0802A);
            y += 9;
            y = this.wrapped(g, Component.translatable("gui.kingdomsatwar.town.reputation_next", v.reputation(), next.points() - v.reputation(),
                    Component.translatable(next.translationKey())), x, y, 140, MUTED);
        }
        y += 1;
        Component discount = v.crowned() && v.isOwner() ? Component.translatable("gui.kingdomsatwar.town.all_free")
                : Component.translatable("gui.kingdomsatwar.town.discount", rank.discount());
        this.wrapped(g, discount, x, y, 140, v.crowned() && v.isOwner() ? GOOD : TEXT);

        // rechte Spalte: Baustellen
        int rx = x0 + 158;
        int ry = y0 + 4;
        g.fill(rx - 7, y0 + 2, rx - 6, y0 + CONTENT_H - 4, 0x40765A34);
        g.text(this.font, Component.translatable("gui.kingdomsatwar.town.projects").withStyle(ChatFormatting.BOLD), rx, ry, TEXT, false);
        Component builders = Component.translatable("gui.kingdomsatwar.town.builders", this.count(Profession.BUILDER));
        g.text(this.font, builders, rx + 140 - this.font.width(builders), ry, MUTED, false);
        ry += 13;
        if (this.count(Profession.BUILDER) == 0 && v.isOwner()) {
            for (FormattedCharSequence line : this.font.split(Component.translatable("gui.kingdomsatwar.town.no_builder"), 138)) {
                g.text(this.font, line, rx, ry, BAD, false);
                ry += 10;
            }
            ry += 3;
        }
        if (v.projects().isEmpty()) {
            for (FormattedCharSequence line : this.font.split(Component.translatable("gui.kingdomsatwar.town.no_projects"), 138)) {
                g.text(this.font, line, rx, ry, MUTED, false);
                ry += 10;
            }
        }
        int storageY = y0 + CONTENT_H - (v.storage() ? 16 : 26);
        for (int i = 0; i < v.projects().size(); i++) {
            TownView.ProjectView p = v.projects().get(i);
            if (ry + 30 > storageY - 4) {
                Component more = Component.translatable("gui.kingdomsatwar.town.more_projects", v.projects().size() - i);
                g.text(this.font, more, rx, ry, MUTED, false);
                break;
            }
            BuildingType type = BuildingType.values()[Mth.clamp(p.type(), 0, BuildingType.values().length - 1)];
            this.card(g, rx, ry, 140, 30, false);
            this.itemScaled(g, type.custom() ? new ItemStack(ModItems.BLUEPRINT.get()) : new ItemStack(type.icon()), rx + 4, ry + 4, 1.0f);
            String pct = (p.permille() / 10) + " %";
            Component title = type.custom() && !p.name().isEmpty() ? Component.literal(p.name()) : Component.translatable(type.translationKey());
            g.text(this.font, this.ellipsize(title.getString(), 108 - this.font.width(pct)), rx + 24, ry + 4, TEXT, false);
            g.text(this.font, pct, rx + 136 - this.font.width(pct), ry + 4, MUTED, false);
            if (!p.waitingItem().isEmpty()) {
                // wartet auf einen kostbaren Block: welcher fehlt
                this.itemScaled(g, p.waitingItem(), rx + 24, ry + 14, 0.625f);
                Component waiting = Component.translatable("gui.kingdomsatwar.town.waiting", p.waitingItem().getHoverName());
                g.text(this.font, this.ellipsize(waiting.getString(), 60), rx + 36, ry + 16, BAD, false);
                if (mouseX >= rx && mouseX < rx + 96 && mouseY >= ry && mouseY < ry + 30) {
                    List<FormattedCharSequence> tip = new ArrayList<>(this.font.split(Component.translatable(
                            v.storage() ? "gui.kingdomsatwar.town.waiting.precious" : "gui.kingdomsatwar.town.storage.none",
                            p.waitingItem().getHoverName()).withStyle(ChatFormatting.GRAY), 200));
                    g.setTooltipForNextFrame(this.font, tip, mouseX, mouseY);
                }
            } else if (p.waiting() >= 0 && p.waiting() < Material.values().length) {
                // wartet auf einen Baustoff: statt des Balkens rot, was fehlt
                Material m = Material.values()[p.waiting()];
                this.itemScaled(g, m.icon(), rx + 24, ry + 14, 0.625f);
                Component waiting = Component.translatable("gui.kingdomsatwar.town.waiting", Component.translatable(m.translationKey()));
                g.text(this.font, this.ellipsize(waiting.getString(), 60), rx + 36, ry + 16, BAD, false);
                if (mouseX >= rx && mouseX < rx + 96 && mouseY >= ry && mouseY < ry + 30) {
                    List<FormattedCharSequence> tip = new ArrayList<>(this.font.split(Component.translatable(
                            v.storage() ? "gui.kingdomsatwar.town.waiting.hint" : "gui.kingdomsatwar.town.storage.none",
                            Component.translatable(m.translationKey())).withStyle(ChatFormatting.GRAY), 200));
                    g.setTooltipForNextFrame(this.font, tip, mouseX, mouseY);
                }
            } else {
                this.bar(g, rx + 24, ry + 16, 72, 6, p.permille() / 1000.0f, 0xFF5E8C3A);
                if (p.site().isPresent()) {
                    // Großbaustelle: die Abschnitte als feine Teilung im Balken, die Karte beim Darüberfahren
                    TownView.SiteView site = p.site().get();
                    int n = site.map().length;
                    if (n > 1 && n <= 36) {
                        for (int k = 1; k < n; k++) {
                            int tx = rx + 25 + Math.round(70.0f * k / n);
                            g.fill(tx, ry + 17, tx + 1, ry + 21, 0x40000000);
                        }
                    }
                    if (mouseX >= rx && mouseX < rx + 98 && mouseY >= ry && mouseY < ry + 30) {
                        this.overlay = () -> this.drawSiteMap(g, site, mouseX, mouseY);
                    }
                }
            }
            if (v.isOwner()) {
                final int index = i;
                // Bauweise: ein Ziegel (Block für Block) oder eine Ziegelmauer (abschnittsweise)
                this.smallButton(g, rx + 101, ry + 13, 16, 14, Component.empty(), true, mouseX, mouseY,
                        () -> this.send(TownActions.FAST, index, ""),
                        List.of(Component.translatable(p.fast() ? "gui.kingdomsatwar.town.mode.fast" : "gui.kingdomsatwar.town.mode.slow")
                                        .withStyle(ChatFormatting.GOLD),
                                Component.translatable(p.fast() ? "gui.kingdomsatwar.town.mode.fast.hint" : "gui.kingdomsatwar.town.mode.slow.hint")
                                        .withStyle(ChatFormatting.GRAY),
                                Component.translatable("gui.kingdomsatwar.town.mode.switch").withStyle(ChatFormatting.DARK_GRAY)));
                this.itemScaled(g, new ItemStack(p.fast() ? Items.BRICKS : Items.BRICK), rx + 104, ry + 16, 0.625f);
                this.smallButton(g, rx + 120, ry + 13, 16, 14, Component.literal("✘"), true, mouseX, mouseY,
                        () -> this.send(TownActions.CANCEL, index, ""),
                        List.of(Component.translatable("gui.kingdomsatwar.town.cancel"),
                                Component.translatable("gui.kingdomsatwar.town.cancel.refund").withStyle(ChatFormatting.GRAY)));
            }
            ry += 33;
        }
        this.drawStorage(g, rx, storageY, mouseX, mouseY);
    }

    /** Unten in der rechten Spalte: was im Lager liegt (oder der Hinweis, wie man eins anlegt). */
    private void drawStorage(GuiGraphicsExtractor g, int x, int y, int mouseX, int mouseY) {
        TownView v = this.view;
        g.fill(x - 2, y - 3, x + 142, y - 2, 0x40765A34);
        if (!v.storage()) {
            this.wrapped(g, Component.translatable("gui.kingdomsatwar.town.storage.none"), x, y, 140, BAD);
            return;
        }
        Component label = Component.translatable("gui.kingdomsatwar.town.storage");
        g.text(this.font, label, x, y + 3, MUTED, false);
        int ix = x + this.font.width(label) + 5;
        for (Material m : Material.values()) {
            this.itemScaled(g, m.icon(), ix, y, 0.75f);
            String n = String.valueOf(v.stock(m));
            g.text(this.font, n, ix + 13, y + 3, TEXT, false);
            ix += 13 + this.font.width(n) + 6;
        }
        if (mouseX >= x && mouseX < x + 140 && mouseY >= y - 2 && mouseY < y + 12) {
            List<FormattedCharSequence> tip = new ArrayList<>();
            tip.add(Component.translatable("gui.kingdomsatwar.town.storage.title").withStyle(ChatFormatting.GOLD).getVisualOrderText());
            for (Material m : Material.values()) {
                tip.add(Component.translatable("gui.kingdomsatwar.town.storage.line", v.stock(m), Component.translatable(m.translationKey()))
                        .withStyle(ChatFormatting.GRAY).getVisualOrderText());
            }
            tip.addAll(this.font.split(Component.translatable("gui.kingdomsatwar.town.storage.hint").withStyle(ChatFormatting.DARK_GRAY), 200));
            g.setTooltipForNextFrame(this.font, tip, mouseX, mouseY);
        }
    }

    private int count(Profession profession) {
        int n = 0;
        for (TownView.ResidentView r : this.view.residents()) {
            if (r.profession() == profession.ordinal()) {
                n++;
            }
        }
        return n;
    }

    // ---------------------------------------------------------------- Anheuern

    private void drawHire(GuiGraphicsExtractor g, int x0, int y0, int mouseX, int mouseY) {
        TownView v = this.view;
        var player = this.minecraft.player;
        int purse = player == null ? 0 : Coins.total(player);
        boolean creative = player != null && player.hasInfiniteMaterials();
        Component purseText = Component.translatable("gui.kingdomsatwar.town.purse");
        g.text(this.font, purseText, x0 + 5, y0 + 3, MUTED, false);
        this.price(g, x0 + 9 + this.font.width(purseText), y0 + 2, purse, TEXT);
        Component beds = Component.translatable("gui.kingdomsatwar.town.free_beds", Math.max(0, v.capacity() - v.residents().size()));
        g.text(this.font, beds, x0 + CONTENT_W - 5 - this.font.width(beds), y0 + 3, MUTED, false);
        int cw = 148;
        int ch = 31;
        for (Profession p : Profession.values()) {
            int col = p.ordinal() % 2;
            int row = p.ordinal() / 2;
            int x = x0 + 2 + col * (cw + 2);
            int y = y0 + 15 + row * (ch + 2);
            TownView.HireView h = v.hires().get(p.ordinal());
            boolean hover = mouseX >= x && mouseX < x + cw && mouseY >= y && mouseY < y + ch;
            this.card(g, x, y, cw, ch, hover);
            this.face(g, x + 4, y + 4, 16, p, 0);
            this.itemScaled(g, new ItemStack(p.tool()), x + 13, y + 15, 0.5f);
            g.text(this.font, Component.translatable(p.translationKey()).withStyle(ChatFormatting.BOLD), x + 25, y + 4, TEXT, false);
            if (h.price() <= 0) {
                g.text(this.font, Component.translatable("gui.kingdomsatwar.town.free"), x + 25, y + 17, GOOD, false);
            } else {
                this.price(g, x + 25, y + 16, h.price(), creative || purse >= h.price() ? TEXT : BAD);
            }
            String count = "×" + h.count();
            g.text(this.font, count, x + cw - 52 - this.font.width(count), y + 17, MUTED, false);
            boolean affordable = creative || purse >= h.price();
            boolean ok = h.blocked() == 0 && affordable;
            List<Component> why = new ArrayList<>();
            if (h.blocked() == 1) {
                BuildingType work = p.workplace();
                why.add(work == null ? Component.translatable("gui.kingdomsatwar.town.hire.builder_limit")
                        : Component.translatable("gui.kingdomsatwar.town.hire.needs", Component.translatable(work.translationKey())));
            } else if (h.blocked() == 2) {
                why.add(Component.translatable("gui.kingdomsatwar.town.hire.no_bed"));
            } else if (!affordable) {
                why.add(Component.translatable("gui.kingdomsatwar.town.hire.not_enough"));
            }
            this.woodButton(g, x + cw - 48, y + 8, 44, 16, Component.translatable("gui.kingdomsatwar.town.hire"), ok, mouseX, mouseY,
                    () -> this.send(TownActions.HIRE, p.ordinal(), ""), why.isEmpty() ? null : why);
            if (hover && !(mouseX >= x + cw - 48 && mouseX < x + cw - 4 && mouseY >= y + 8 && mouseY < y + 24)) {
                List<FormattedCharSequence> tip = new ArrayList<>();
                tip.add(Component.translatable(p.translationKey()).withStyle(ChatFormatting.GOLD).getVisualOrderText());
                tip.addAll(this.font.split(Component.translatable(p.translationKey() + ".description").withStyle(ChatFormatting.GRAY), 200));
                if (p.workplace() != null) {
                    tip.add(Component.translatable("gui.kingdomsatwar.town.hire.workplace", Component.translatable(p.workplace().translationKey()))
                            .withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
                }
                if (p.trader()) {
                    tip.add(Component.translatable("gui.kingdomsatwar.town.hire.trader").withStyle(ChatFormatting.DARK_AQUA).getVisualOrderText());
                }
                g.setTooltipForNextFrame(this.font, tip, mouseX, mouseY);
            }
        }
    }

    // ---------------------------------------------------------------- Bewohner

    private void drawResidents(GuiGraphicsExtractor g, int x0, int y0, int mouseX, int mouseY) {
        List<TownView.ResidentView> list = this.view.residents();
        int x = x0 + 4;
        int y = y0 + 3;
        g.text(this.font, Component.translatable("gui.kingdomsatwar.town.residents.name"), x + 18, y, MUTED, false);
        g.text(this.font, Component.translatable("gui.kingdomsatwar.town.residents.profession"), x + 100, y, MUTED, false);
        g.text(this.font, Component.translatable("gui.kingdomsatwar.town.residents.state"), x + 172, y, MUTED, false);
        g.fill(x, y + 10, x + CONTENT_W - 10, y + 11, 0x40765A34);
        y += 13;
        if (list.isEmpty()) {
            for (FormattedCharSequence line : this.font.split(Component.translatable("gui.kingdomsatwar.town.residents.empty"), 280)) {
                g.text(this.font, line, x + 4, y + 6, MUTED, false);
                y += 10;
            }
            return;
        }
        int max = Math.max(0, list.size() - VISIBLE_ROWS);
        this.residentScroll = Mth.clamp(this.residentScroll, 0, max);
        for (int i = 0; i < VISIBLE_ROWS && this.residentScroll + i < list.size(); i++) {
            int index = this.residentScroll + i;
            TownView.ResidentView r = list.get(index);
            Profession p = Profession.values()[Mth.clamp(r.profession(), 0, Profession.values().length - 1)];
            int ry = y + i * ROW_H;
            boolean hover = mouseX >= x && mouseX < x + CONTENT_W - 14 && mouseY >= ry && mouseY < ry + ROW_H - 1;
            if (hover || index % 2 == 0) {
                g.fill(x, ry, x + CONTENT_W - 14, ry + ROW_H - 1, hover ? CARD_HOVER : 0x14FFF6DC);
            }
            this.face(g, x + 2, ry + 2, 12, p, r.variant());
            g.text(this.font, this.ellipsize(r.name(), 78), x + 18, ry + 4, TEXT, false);
            this.itemScaled(g, new ItemStack(p.tool()), x + 100, ry + 2, 0.625f);
            g.text(this.font, this.ellipsize(Component.translatable(p.translationKey()).getString(), 56), x + 113, ry + 4, TEXT, false);
            int stateColor = switch (r.state()) {
                case 1 -> GOOD;
                case 2 -> 0xFF3F5A8A;
                case 3 -> MUTED;
                case 4 -> BAD;
                default -> TEXT;
            };
            g.text(this.font, this.ellipsize(Component.translatable("gui.kingdomsatwar.town.state." + r.state()).getString(), 52), x + 172,
                    ry + 4, stateColor, false);
            // Bett und Werkstatt
            int ix = x + 228;
            this.itemScaled(g, new ItemStack(Items.BED.red()), ix, ry + 2, 0.625f);
            g.text(this.font, r.bed() ? "✔" : "✘", ix + 10, ry + 4, r.bed() ? GOOD : BAD, false);
            this.itemScaled(g, new ItemStack(Items.CRAFTING_TABLE), ix + 19, ry + 2, 0.625f);
            g.text(this.font, r.work() ? "✔" : "✘", ix + 29, ry + 4, r.work() ? GOOD : BAD, false);
            if (mouseX >= ix && mouseX < ix + 38 && mouseY >= ry && mouseY < ry + ROW_H - 1) {
                g.setComponentTooltipForNextFrame(this.font, List.of(
                        Component.translatable(r.bed() ? "gui.kingdomsatwar.town.residents.bed" : "gui.kingdomsatwar.town.residents.no_bed"),
                        Component.translatable(r.work() ? "gui.kingdomsatwar.town.residents.work" : "gui.kingdomsatwar.town.residents.no_work",
                                p.workplace() == null ? Component.literal("–") : Component.translatable(p.workplace().translationKey()))),
                        mouseX, mouseY);
            }
            if (this.view.isOwner()) {
                boolean confirm = this.confirmDismiss == index;
                this.smallButton(g, x + CONTENT_W - 32, ry + 1, 16, 14, Component.literal(confirm ? "!" : "✘"), true, mouseX, mouseY, () -> {
                    if (this.confirmDismiss == index) {
                        this.send(TownActions.DISMISS, index, "");
                        this.confirmDismiss = -1;
                    } else {
                        this.confirmDismiss = index;
                        this.confirmTicks = 60;
                    }
                }, List.of(Component.translatable(confirm ? "gui.kingdomsatwar.town.dismiss_confirm" : "gui.kingdomsatwar.town.dismiss",
                        Component.literal(r.name()))));
            }
        }
        // Bildlaufleiste
        if (max > 0) {
            int sx = x + CONTENT_W - 12;
            int sh = VISIBLE_ROWS * ROW_H - 2;
            g.fill(sx, y, sx + 3, y + sh, 0x40765A34);
            int knob = Math.max(10, sh * VISIBLE_ROWS / list.size());
            int ky = y + (sh - knob) * this.residentScroll / max;
            g.fill(sx, ky, sx + 3, ky + knob, 0xFF8A6A3E);
        }
    }

    // ---------------------------------------------------------------- Bauen

    private void drawBuild(GuiGraphicsExtractor g, int x0, int y0, int mouseX, int mouseY) {
        TownView v = this.view;
        var player = this.minecraft.player;
        int purse = player == null ? 0 : Coins.total(player);
        boolean creative = player != null && player.hasInfiniteMaterials();
        Component hint = Component.translatable(v.isOwner() ? "gui.kingdomsatwar.town.build.hint" : "gui.kingdomsatwar.town.build.foreign");
        g.text(this.font, hint, x0 + 5, y0 + 3, MUTED, false);
        int cw = 58;
        int ch = 42;
        for (BuildingType type : BuildingType.values()) {
            int col = type.ordinal() % 5;
            int row = type.ordinal() / 5;
            int x = x0 + 2 + col * (cw + 2);
            int y = y0 + 15 + row * (ch + 2);
            if (type.custom()) {
                this.drawPlansCard(g, x, y, cw, ch, mouseX, mouseY, type.ordinal() < v.builds().size() ? v.builds().get(type.ordinal()).count() : 0);
                continue;
            }
            TownView.BuildView b = v.builds().get(type.ordinal());
            boolean hover = mouseX >= x && mouseX < x + cw && mouseY >= y && mouseY < y + ch;
            boolean usable = v.isOwner() && b.allowed();
            this.card(g, x, y, cw, ch, hover && usable);
            this.itemScaled(g, new ItemStack(type.icon()), x + (cw - 16) / 2, y + 3, 1.0f);
            Component name = Component.translatable(type.translationKey());
            this.centeredFit(g, name, x + cw / 2, y + 21, cw - 4, b.allowed() ? TEXT : MUTED);
            if (!b.allowed()) {
                this.centeredFit(g, Component.translatable("gui.kingdomsatwar.town.build.locked", Component.translatable(type.tier().translationKey())),
                        x + cw / 2, y + 31, cw - 4, BAD);
            } else if (b.price() <= 0) {
                this.centeredFit(g, Component.translatable("gui.kingdomsatwar.town.free"), x + cw / 2, y + 31, cw - 4, GOOD);
            } else {
                int pw = this.priceWidth(b.price());
                this.price(g, x + (cw - pw) / 2, y + 30, b.price(), creative || purse >= b.price() ? TEXT : BAD);
            }
            if (b.count() > 0) {
                String n = "×" + b.count();
                g.text(this.font, n, x + cw - 3 - this.font.width(n), y + 3, MUTED, false);
            }
            if (!b.allowed()) {
                g.fill(x + 1, y + 1, x + cw - 1, y + ch - 1, 0x40B8A27A);
            }
            if (usable) {
                this.hotspots.add(new Hotspot(x, y, cw, ch, true, () -> this.send(TownActions.BLUEPRINT, type.ordinal(), "")));
            }
            if (hover) {
                List<FormattedCharSequence> tip = new ArrayList<>();
                tip.add(name.copy().withStyle(ChatFormatting.GOLD).getVisualOrderText());
                tip.addAll(this.font.split(Component.translatable(type.descriptionKey()).withStyle(ChatFormatting.GRAY), 210));
                int[] size = type.maxSize();
                if (type.designs() > 1) {
                    tip.add(Component.translatable("gui.kingdomsatwar.town.build.designs", type.designs()).withStyle(ChatFormatting.GOLD)
                            .getVisualOrderText());
                    tip.add(Component.translatable("gui.kingdomsatwar.town.build.size_max", size[0], size[1]).withStyle(ChatFormatting.DARK_GRAY)
                            .getVisualOrderText());
                } else {
                    tip.add(Component.translatable("gui.kingdomsatwar.town.build.size", size[0], size[1]).withStyle(ChatFormatting.DARK_GRAY)
                            .getVisualOrderText());
                }
                if (type.beds() > 0) {
                    tip.add(Component.translatable("gui.kingdomsatwar.town.build.beds", type.beds()).withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
                }
                // Baustoffe (grün: liegt im Lager, rot: fehlt noch)
                if (!type.costsMaterials()) {
                    tip.add(Component.translatable("gui.kingdomsatwar.town.build.no_materials").withStyle(ChatFormatting.GREEN).getVisualOrderText());
                } else {
                    tip.add(Component.translatable("gui.kingdomsatwar.town.build.materials").withStyle(ChatFormatting.GRAY).getVisualOrderText());
                    for (Material m : Material.values()) {
                        int need = b.cost(m);
                        int least = b.minCost(m);
                        if (need > 0) {
                            int have = v.stock(m);
                            ChatFormatting color = have >= need ? ChatFormatting.DARK_GREEN : have >= least ? ChatFormatting.GOLD : ChatFormatting.RED;
                            Component amount = least == need ? Component.literal(String.valueOf(need)) : Component.literal(least + "–" + need);
                            tip.add(Component.translatable("gui.kingdomsatwar.town.build.material", amount, Component.translatable(m.translationKey()),
                                    have).withStyle(color).getVisualOrderText());
                        }
                    }
                }
                for (Profession p : Profession.values()) {
                    if (p.workplace() == type) {
                        tip.add(Component.translatable("gui.kingdomsatwar.town.build.jobs", type.jobs(), Component.translatable(p.translationKey()))
                                .withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
                    }
                }
                if (!b.allowed()) {
                    tip.add(Component.translatable("gui.kingdomsatwar.town.build.needs_tier", Component.translatable(type.tier().translationKey()))
                            .withStyle(ChatFormatting.RED).getVisualOrderText());
                } else if (usable) {
                    tip.add(Component.translatable("gui.kingdomsatwar.town.build.take").withStyle(ChatFormatting.GREEN).getVisualOrderText());
                }
                g.setTooltipForNextFrame(this.font, tip, mouseX, mouseY);
            }
        }
    }

    // ---------------------------------------------------------------- Eigene Baupläne (Dragon Architect)

    /** So viele eigene Pläne passen untereinander. */
    private static final int PLAN_ROWS = 4;
    private static final int PLAN_ROW_H = 32;

    /** Die Karte „Eigene Pläne“ im Reiter Bauen. */
    private void drawPlansCard(GuiGraphicsExtractor g, int x, int y, int cw, int ch, int mouseX, int mouseY, int built) {
        boolean hover = mouseX >= x && mouseX < x + cw && mouseY >= y && mouseY < y + ch;
        g.fill(x, y, x + cw, y + ch, hover ? 0xFF2B4E80 : 0xFF203F6C);
        for (int gx = x + 6; gx < x + cw; gx += 6) {
            g.fill(gx, y + 1, gx + 1, y + ch - 1, 0x22A9C8F0);
        }
        for (int gy = y + 6; gy < y + ch; gy += 6) {
            g.fill(x + 1, gy, x + cw - 1, gy + 1, 0x22A9C8F0);
        }
        g.outline(x, y, cw, ch, hover ? 0xFFF2C23A : 0xFF9DBBE4);
        this.itemScaled(g, new ItemStack(ModItems.SURVEYORS_ROD.get()), x + (cw - 16) / 2, y + 3, 1.0f);
        this.centeredFit(g, Component.translatable("gui.kingdomsatwar.town.plans"), x + cw / 2, y + 21, cw - 4, 0xFFF3F7FF);
        Component sub = this.plans.isEmpty() ? Component.translatable("gui.kingdomsatwar.town.plans.scan")
                : Component.translatable("gui.kingdomsatwar.town.plans.count", this.plans.size());
        this.centeredFit(g, sub, x + cw / 2, y + 31, cw - 4, 0xFFBFD5F2);
        if (built > 0) {
            String n = "×" + built;
            g.text(this.font, n, x + cw - 3 - this.font.width(n), y + 3, 0xFFBFD5F2, false);
        }
        this.hotspots.add(new Hotspot(x, y, cw, ch, true, () -> {
            this.showPlans = true;
            this.planScroll = 0;
            this.send(TownActions.PLANS, 0, "");
        }));
        if (hover) {
            List<FormattedCharSequence> tip = new ArrayList<>();
            tip.add(Component.translatable("gui.kingdomsatwar.town.plans").withStyle(ChatFormatting.GOLD).getVisualOrderText());
            tip.addAll(this.font.split(Component.translatable("gui.kingdomsatwar.town.plans.description").withStyle(ChatFormatting.GRAY), 210));
            g.setTooltipForNextFrame(this.font, tip, mouseX, mouseY);
        }
    }

    /** Die eigenen Baupläne: Ansicht, Name, Größe, Baustoffe, Preis; Abschrift nehmen oder löschen. */
    private void drawPlans(GuiGraphicsExtractor g, int x0, int y0, int mouseX, int mouseY) {
        TownView v = this.view;
        this.woodButton(g, x0 + 2, y0 + 1, 16, 14, Component.literal("◀"), true, mouseX, mouseY, () -> this.showPlans = false,
                List.of(Component.translatable("gui.kingdomsatwar.town.plans.back")));
        Component title = Component.translatable("gui.kingdomsatwar.town.plans.title", this.plans.size()).withStyle(ChatFormatting.BOLD);
        g.text(this.font, title, x0 + 23, y0 + 4, TEXT, false);
        this.woodButton(g, x0 + CONTENT_W - 120, y0 + 1, 96, 14, Component.translatable("gui.kingdomsatwar.town.import"), true, mouseX, mouseY,
                () -> {
                    this.showImport = true;
                    this.importScroll = 0;
                    this.send(TownActions.IMPORT_LIST, 0, "");
                }, List.of(Component.translatable("gui.kingdomsatwar.town.import.hint").withStyle(ChatFormatting.GRAY)));
        if (v.isOwner()) {
            this.woodButton(g, x0 + CONTENT_W - 22, y0 + 1, 20, 14, Component.empty(), true, mouseX, mouseY,
                    () -> this.send(TownActions.TAKE_ROD, 0, ""), List.of(Component.translatable("gui.kingdomsatwar.town.plans.rod"),
                            Component.translatable("gui.kingdomsatwar.town.plans.rod.hint").withStyle(ChatFormatting.GRAY)));
            this.itemScaled(g, new ItemStack(ModItems.SURVEYORS_ROD.get()), x0 + CONTENT_W - 17, y0 + 4, 0.625f);
        }
        int y = y0 + 19;
        if (this.plans.isEmpty()) {
            this.itemScaled(g, new ItemStack(ModItems.SURVEYORS_ROD.get()), x0 + CONTENT_W / 2 - 16, y + 6, 2.0f);
            int ty = y + 42;
            for (FormattedCharSequence line : this.font.split(Component.translatable("gui.kingdomsatwar.town.plans.empty"), 270)) {
                g.text(this.font, line, x0 + (CONTENT_W - this.font.width(line)) / 2, ty, MUTED, false);
                ty += 10;
            }
            return;
        }
        var player = this.minecraft.player;
        int purse = player == null ? 0 : Coins.total(player);
        boolean creative = player != null && player.hasInfiniteMaterials();
        int max = Math.max(0, this.plans.size() - PLAN_ROWS);
        this.planScroll = Mth.clamp(this.planScroll, 0, max);
        int rowW = CONTENT_W - (max > 0 ? 10 : 4);
        for (int i = 0; i < PLAN_ROWS && this.planScroll + i < this.plans.size(); i++) {
            io.github.sirschrolli.kingdomsatwar.architect.PlanInfo plan = this.plans.get(this.planScroll + i);
            int ry = y + i * PLAN_ROW_H;
            int rx = x0 + 2;
            boolean hover = mouseX >= rx && mouseX < rx + rowW && mouseY >= ry && mouseY < ry + PLAN_ROW_H - 2;
            this.card(g, rx, ry, rowW, PLAN_ROW_H - 2, hover);
            // Ansicht auf Blaupausen-Papier
            io.github.sirschrolli.kingdomsatwar.client.architect.PlanTextures.drawPanel(g, plan.id() + "/front", plan.pictures().frontPicture(),
                    rx + 2, ry + 2, 44, PLAN_ROW_H - 6, 4);
            g.text(this.font, this.ellipsize(plan.name(), 120), rx + 50, ry + 3, TEXT, false);
            Component size = Component.translatable("gui.kingdomsatwar.town.plans.size", plan.shape().sizeX(), plan.height(), plan.shape().sizeZ(),
                    plan.costs().blocks());
            g.text(this.font, this.ellipsize(size.getString(), 120), rx + 50, ry + 13, MUTED, false);
            // Baustoffe (und kostbare Blöcke) klein darunter
            int ix = rx + 50;
            for (Material m : Material.values()) {
                int need = plan.costs().units(m);
                if (need <= 0) {
                    continue;
                }
                this.itemScaled(g, m.icon(), ix, ry + 21, 0.5f);
                String n = String.valueOf(need);
                g.text(this.font, n, ix + 9, ry + 22, v.stock(m) >= need ? GOOD : BAD, false);
                ix += 9 + this.font.width(n) + 5;
            }
            if (!plan.costs().precious().isEmpty()) {
                this.itemScaled(g, new ItemStack(plan.costs().precious().getFirst().item()), ix, ry + 21, 0.5f);
                g.text(this.font, "+" + plan.costs().precious().size(), ix + 9, ry + 22, 0xFF2A5C7A, false);
            }
            // Preis und Knöpfe
            int bx = rx + rowW - 86;
            int price = plan.price();
            int pw = this.priceWidth(price);
            this.price(g, bx - 6 - pw, ry + 4, price, creative || purse >= price ? TEXT : BAD);
            if (v.isOwner()) {
                this.woodButton(g, bx, ry + 3, 46, 14, Component.translatable("gui.kingdomsatwar.town.plans.take"), true, mouseX, mouseY,
                        () -> this.send(TownActions.TAKE_PLAN, 0, plan.id()),
                        List.of(Component.translatable(creative ? "gui.kingdomsatwar.town.plans.take.free" : "gui.kingdomsatwar.town.plans.take.hint")
                                .withStyle(ChatFormatting.GRAY)));
            }
            this.smallButton(g, bx + 48, ry + 3, 16, 14, Component.literal("↓"), true, mouseX, mouseY,
                    () -> this.send(TownActions.EXPORT, 0, plan.id()),
                    List.of(Component.translatable("gui.kingdomsatwar.town.export"),
                            Component.translatable("gui.kingdomsatwar.town.export.hint").withStyle(ChatFormatting.GRAY)));
            boolean confirm = plan.id().equals(this.confirmDelete);
            this.smallButton(g, bx + 66, ry + 3, 16, 14, Component.literal(confirm ? "!" : "✘"), true, mouseX, mouseY, () -> {
                if (plan.id().equals(this.confirmDelete)) {
                    this.send(TownActions.DELETE_PLAN, 0, plan.id());
                    this.confirmDelete = null;
                } else {
                    this.confirmDelete = plan.id();
                    this.confirmTicks = 60;
                }
            }, List.of(Component.translatable(confirm ? "gui.kingdomsatwar.town.plans.delete_confirm" : "gui.kingdomsatwar.town.plans.delete",
                    Component.literal(plan.name()))));
            // über der Ansicht: der ganze Bauplan als Tooltip (mit Grundriss)
            if (mouseX >= rx + 2 && mouseX < rx + 46 && mouseY >= ry + 2 && mouseY < ry + PLAN_ROW_H - 4) {
                g.setTooltipForNextFrame(this.font, io.github.sirschrolli.kingdomsatwar.item.BlueprintItem.of(plan), mouseX, mouseY);
            }
        }
        if (max > 0) {
            int sx = x0 + CONTENT_W - 6;
            int sh = PLAN_ROWS * PLAN_ROW_H - 4;
            g.fill(sx, y, sx + 3, y + sh, 0x40765A34);
            int knob = Math.max(10, sh * PLAN_ROWS / this.plans.size());
            int ky = y + (sh - knob) * this.planScroll / max;
            g.fill(sx, ky, sx + 3, ky + knob, 0xFF8A6A3E);
        }
    }

    /** Ein kreisender Pfeil (neu einlesen) – die Schrift des Spiels kennt kein solches Zeichen. */
    private static final String[] REFRESH_ICON = {
            "..####..",
            ".#....#.",
            "#....###",
            "#.....#.",
            "#.......",
            "#......#",
            ".#....#.",
            "..####.."};

    /** Zeichnet ein kleines Pixelbild (Zeilen aus '#' und '.') mit Schatten wie die Schrift. */
    private void pixels(GuiGraphicsExtractor g, int x, int y, String[] rows, int color) {
        int shadow = 0xFF000000 | ((color >> 2) & 0x3F3F3F);
        for (int pass = 0; pass < 2; pass++) {
            int off = pass == 0 ? 1 : 0;
            for (int r = 0; r < rows.length; r++) {
                String row = rows[r];
                for (int c = 0; c < row.length(); c++) {
                    if (row.charAt(c) == '#') {
                        g.fill(x + c + off, y + r + off, x + c + off + 1, y + r + off + 1, pass == 0 ? shadow : color);
                    }
                }
            }
        }
    }

    /** So viele Schematiken passen untereinander. */
    private static final int IMPORT_ROWS = 6;
    private static final int IMPORT_ROW_H = 21;

    /** Die Schematiken in den Ordnern des Servers: Art, Name, Ordner, Größe; Einlesen als eigenen Plan. */
    private void drawImports(GuiGraphicsExtractor g, int x0, int y0, int mouseX, int mouseY) {
        this.woodButton(g, x0 + 2, y0 + 1, 16, 14, Component.literal("◀"), true, mouseX, mouseY, () -> this.showImport = false,
                List.of(Component.translatable("gui.kingdomsatwar.town.plans.back")));
        Component title = Component.translatable("gui.kingdomsatwar.town.import.title", this.imports.size()).withStyle(ChatFormatting.BOLD);
        g.text(this.font, title, x0 + 23, y0 + 4, TEXT, false);
        this.woodButton(g, x0 + CONTENT_W - 22, y0 + 1, 20, 14, Component.empty(), true, mouseX, mouseY,
                () -> this.send(TownActions.IMPORT_LIST, 0, ""), List.of(Component.translatable("gui.kingdomsatwar.town.import.refresh")));
        this.pixels(g, x0 + CONTENT_W - 16, y0 + 4, REFRESH_ICON, 0xFFF3E6C4);
        int y = y0 + 19;
        if (this.imports.isEmpty()) {
            Component text = Component.translatable(this.importsLoaded ? "gui.kingdomsatwar.town.import.empty" : "gui.kingdomsatwar.town.import.loading");
            int ty = y + 8;
            for (FormattedCharSequence line : this.font.split(text, 280)) {
                g.text(this.font, line, x0 + (CONTENT_W - this.font.width(line)) / 2, ty, MUTED, false);
                ty += 10;
            }
            return;
        }
        int max = Math.max(0, this.imports.size() - IMPORT_ROWS);
        this.importScroll = Mth.clamp(this.importScroll, 0, max);
        int rowW = CONTENT_W - (max > 0 ? 10 : 4);
        for (int i = 0; i < IMPORT_ROWS && this.importScroll + i < this.imports.size(); i++) {
            var file = this.imports.get(this.importScroll + i);
            int ry = y + i * IMPORT_ROW_H;
            int rx = x0 + 2;
            boolean hover = mouseX >= rx && mouseX < rx + rowW && mouseY >= ry && mouseY < ry + IMPORT_ROW_H - 2;
            this.card(g, rx, ry, rowW, IMPORT_ROW_H - 2, hover);
            // Art der Datei als farbiges Schild
            String badge = switch (file.format()) {
                case "litematic" -> "LITE";
                case "schem" -> "SCHEM";
                default -> "NBT";
            };
            int color = switch (file.format()) {
                case "litematic" -> 0xFF2F6E9E;
                case "schem" -> 0xFF8A4FB0;
                default -> 0xFF5E7E36;
            };
            g.fill(rx + 3, ry + 4, rx + 37, ry + 15, color);
            g.outline(rx + 3, ry + 4, 34, 11, 0x60000000);
            g.text(this.font, badge, rx + 20 - this.font.width(badge) / 2, ry + 6, 0xFFF3F7FF, false);
            g.text(this.font, this.ellipsize(file.name(), 150), rx + 42, ry + 2, TEXT, false);
            String info = file.folder() + " · " + (file.bytes() >= 1024 ? (file.bytes() + 512) / 1024 + " KB" : file.bytes() + " B");
            g.text(this.font, info, rx + 42, ry + 11, MUTED, false);
            this.woodButton(g, rx + rowW - 58, ry + 3, 54, 14, Component.translatable("gui.kingdomsatwar.town.import.read"), true, mouseX, mouseY,
                    () -> this.send(TownActions.IMPORT, 0, file.id()),
                    List.of(Component.translatable("gui.kingdomsatwar.town.import.read.hint", file.name()).withStyle(ChatFormatting.GRAY)));
        }
        if (max > 0) {
            int sx = x0 + CONTENT_W - 6;
            int sh = IMPORT_ROWS * IMPORT_ROW_H - 4;
            g.fill(sx, y, sx + 3, y + sh, 0x40765A34);
            int knob = Math.max(10, sh * IMPORT_ROWS / this.imports.size());
            int ky = y + (sh - knob) * this.importScroll / max;
            g.fill(sx, ky, sx + 3, ky + knob, 0xFF8A6A3E);
        }
    }

    // ---------------------------------------------------------------- Krone

    private void drawCrown(GuiGraphicsExtractor g, int x0, int y0, int mouseX, int mouseY) {
        TownView v = this.view;
        int x = x0 + 6;
        int y = y0 + 4;
        g.text(this.font, Component.translatable("gui.kingdomsatwar.town.crown.title").withStyle(ChatFormatting.BOLD), x, y, TEXT, false);
        y += 13;
        for (FormattedCharSequence line : this.font.split(Component.translatable("gui.kingdomsatwar.town.crown.text"), 206)) {
            g.text(this.font, line, x, y, MUTED, false);
            y += 10;
        }
        y += 4;
        int[] bits = {TownView.CROWN_SEAL, TownView.CROWN_CITY, TownView.CROWN_REPUTATION, TownView.CROWN_HALL, TownView.CROWN_CROWN};
        for (int bit : bits) {
            boolean ok = (v.crown() & bit) != 0;
            g.text(this.font, ok ? "✔" : "✘", x, y, ok ? GOOD : BAD, false);
            g.text(this.font, Component.translatable("gui.kingdomsatwar.town.crown." + Integer.numberOfTrailingZeros(bit)), x + 12, y,
                    ok ? MUTED : TEXT, false);
            y += 12;
        }
        // rechts: die Krone, groß
        int cx = x0 + 250;
        this.itemScaled(g, new ItemStack(ModItems.GEMSTONE_CROWN.get()), cx - 24, y0 + 10, 3.0f);
        if (v.crowned()) {
            Component title = Component.translatable(v.queen() ? "message.kingdomsatwar.crown.queen" : "message.kingdomsatwar.crown.king");
            this.centeredFit(g, Component.translatable("gui.kingdomsatwar.town.crown.done", title).withStyle(ChatFormatting.BOLD), cx, y0 + 64,
                    100, 0xFF8C5F16);
        }
        if (v.isOwner()) {
            g.text(this.font, Component.translatable("gui.kingdomsatwar.town.crown.address"), cx - 46, y0 + 84, MUTED, false);
            this.choice(g, cx - 46, y0 + 96, 44, 16, Component.translatable("message.kingdomsatwar.crown.king"), !v.queen(), mouseX, mouseY,
                    () -> this.send(TownActions.QUEEN, 0, ""));
            this.choice(g, cx + 2, y0 + 96, 44, 16, Component.translatable("message.kingdomsatwar.crown.queen"), v.queen(), mouseX, mouseY,
                    () -> this.send(TownActions.QUEEN, 1, ""));
        }
        if ((v.crown() & TownView.CROWN_DONE) == 0 && (v.crown() & 31) == 31) {
            for (FormattedCharSequence line : this.font.split(Component.translatable("gui.kingdomsatwar.town.crown.ready"), 206)) {
                g.text(this.font, line, x, y + 4, GOOD, false);
                y += 10;
            }
        }
    }

    // =====================================================================================
    // Bausteine
    // =====================================================================================

    /** Ein Gegenstand, verkleinert oder vergrößert ({@code scale} 1 = 16 Pixel). */
    private void itemScaled(GuiGraphicsExtractor g, ItemStack stack, int x, int y, float scale) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.item(stack, 0, 0);
        g.pose().popMatrix();
    }

    /** Das Gesicht eines Bürgers (Kopf und zweite Hautschicht aus seiner Textur). */
    private void face(GuiGraphicsExtractor g, int x, int y, int size, Profession profession, int variant) {
        Identifier skin = CitizenRenderer.texture(profession, Math.min(variant, profession.variants() - 1));
        g.fill(x - 1, y - 1, x + size + 1, y + size + 1, 0xFF5A4126);
        g.blit(RenderPipelines.GUI_TEXTURED, skin, x, y, 8.0f, 8.0f, size, size, 8, 8, 128, 64);
        g.blit(RenderPipelines.GUI_TEXTURED, skin, x, y, 40.0f, 8.0f, size, size, 8, 8, 128, 64);
    }

    /** Ein leicht aufgehelltes Feld mit dünnem Rand. */
    private void card(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean hover) {
        g.fill(x, y, x + w, y + h, hover ? CARD_HOVER : CARD);
        g.outline(x, y, w, h, hover ? 0xFFAE8A52 : CARD_BORDER);
    }

    /** Fortschrittsbalken (dunkle Rinne, farbige Füllung mit hellem Glanz oben). */
    private void bar(GuiGraphicsExtractor g, int x, int y, int w, int h, float fraction, int color) {
        g.fill(x, y, x + w, y + h, 0xFF8C7650);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFC9B48A);
        int fw = Math.round((w - 2) * Mth.clamp(fraction, 0.0f, 1.0f));
        if (fw > 0) {
            g.fill(x + 1, y + 1, x + 1 + fw, y + h - 1, color);
            g.fill(x + 1, y + 1, x + 1 + fw, y + 2, (color & 0xFF000000) | (brighten(color) & 0xFFFFFF));
        }
    }

    private static int brighten(int color) {
        int r = Math.min(255, ((color >> 16) & 0xFF) + 50);
        int gr = Math.min(255, ((color >> 8) & 0xFF) + 50);
        int b = Math.min(255, (color & 0xFF) + 50);
        return (r << 16) | (gr << 8) | b;
    }

    /** Ein Betrag als kleine Münzen mit Anzahl (z. B. Gold 1, Silber 4, Bronze 5). */
    void price(GuiGraphicsExtractor g, int x, int y, int amount, int color) {
        int cx = x;
        int rest = Math.max(0, amount);
        boolean any = false;
        for (int i = Coin.values().length - 1; i >= 0; i--) {
            Coin coin = Coin.values()[i];
            int n = rest / coin.value();
            rest -= n * coin.value();
            if (n > 0 || (!any && i == 0)) {
                this.itemScaled(g, new ItemStack(coin.item()), cx, y, 0.625f);
                String s = String.valueOf(n);
                g.text(this.font, s, cx + 11, y + 2, color, false);
                cx += 11 + this.font.width(s) + 4;
                any = true;
            }
        }
    }

    int priceWidth(int amount) {
        int w = 0;
        int rest = Math.max(0, amount);
        boolean any = false;
        for (int i = Coin.values().length - 1; i >= 0; i--) {
            Coin coin = Coin.values()[i];
            int n = rest / coin.value();
            rest -= n * coin.value();
            if (n > 0 || (!any && i == 0)) {
                w += 11 + this.font.width(String.valueOf(n)) + 4;
                any = true;
            }
        }
        return Math.max(0, w - 4);
    }

    /** Text mittig; wird verkleinert, wenn er nicht in {@code maxWidth} passt. */
    private void centeredFit(GuiGraphicsExtractor g, Component text, int cx, int y, int maxWidth, int color) {
        int w = this.font.width(text);
        if (w <= maxWidth) {
            g.text(this.font, text, cx - w / 2, y, color, false);
            return;
        }
        float s = maxWidth / (float) w;
        g.pose().pushMatrix();
        g.pose().translate(cx - maxWidth / 2.0f, y + (1.0f - s) * 4.0f);
        g.pose().scale(s, s);
        g.text(this.font, text, 0, 0, color, false);
        g.pose().popMatrix();
    }

    /** Text mit Zeilenumbruch; gibt die Zeile unter dem Text zurück. */
    private int wrapped(GuiGraphicsExtractor g, Component text, int x, int y, int width, int color) {
        for (FormattedCharSequence line : this.font.split(text, width)) {
            g.text(this.font, line, x, y, color, false);
            y += 10;
        }
        return y;
    }

    private String ellipsize(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) {
            return text;
        }
        return this.font.plainSubstrByWidth(text, maxWidth - this.font.width("…")) + "…";
    }

    /** Hölzerner Knopf (dunkles Holz, beim Darüberfahren mit Goldrand). */
    private void woodButton(GuiGraphicsExtractor g, int x, int y, int w, int h, Component label, boolean active, int mouseX, int mouseY,
            Runnable action, @Nullable List<Component> tooltip) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int border = active && hover ? 0xFFE0B04A : 0xFF2E1D10;
        int fill = !active ? 0xFF8C826E : hover ? 0xFF8E6038 : 0xFF74492A;
        g.fill(x, y, x + w, y + h, border);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, !active ? 0xFFA49A86 : 0xFFA4744A);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, !active ? 0xFF6E6656 : 0xFF4A2E18);
        int tw = this.font.width(label);
        int color = active ? 0xFFF3E6C4 : 0xFFD5CCB6;
        if (tw > w - 4) {
            this.centeredFit(g, label, x + w / 2, y + (h - 8) / 2, w - 4, color);
        } else {
            g.text(this.font, label, x + (w - tw) / 2, y + (h - 8) / 2, color, active);
        }
        this.hotspots.add(new Hotspot(x, y, w, h, active, action));
        if (hover && tooltip != null && !tooltip.isEmpty()) {
            g.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
        }
    }

    /** Auswahlknopf: der gewählte leuchtet golden, der andere ist ein gewöhnlicher Holzknopf. */
    private void choice(GuiGraphicsExtractor g, int x, int y, int w, int h, Component label, boolean selected, int mouseX, int mouseY,
            Runnable action) {
        if (!selected) {
            this.woodButton(g, x, y, w, h, label, true, mouseX, mouseY, action, null);
            return;
        }
        g.fill(x, y, x + w, y + h, 0xFF8C5F16);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFE8BD52);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0xFFF7DC80);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, 0xFFC8932C);
        int tw = this.font.width(label);
        g.text(this.font, label, x + (w - tw) / 2, y + (h - 8) / 2, 0xFF3B2611, false);
    }

    private void smallButton(GuiGraphicsExtractor g, int x, int y, int w, int h, Component label, boolean active, int mouseX, int mouseY,
            Runnable action, @Nullable List<Component> tooltip) {
        this.woodButton(g, x, y, w, h, label, active, mouseX, mouseY, action, tooltip);
    }
}
