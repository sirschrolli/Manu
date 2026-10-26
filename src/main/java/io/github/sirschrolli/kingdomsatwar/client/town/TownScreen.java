package io.github.sirschrolli.kingdomsatwar.client.town;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.renderer.CitizenRenderer;
import io.github.sirschrolli.kingdomsatwar.economy.Coin;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.economy.Reputation;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import io.github.sirschrolli.kingdomsatwar.economy.town.Profession;
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
        this.updateWidgets();
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
            case BUILD -> this.drawBuild(g, x, y, mouseX, mouseY);
            case CROWN -> this.drawCrown(g, x, y, mouseX, mouseY);
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
        for (int i = 0; i < v.projects().size(); i++) {
            TownView.ProjectView p = v.projects().get(i);
            BuildingType type = BuildingType.values()[Mth.clamp(p.type(), 0, BuildingType.values().length - 1)];
            this.card(g, rx, ry, 140, 30, false);
            this.itemScaled(g, new ItemStack(type.icon()), rx + 4, ry + 4, 1.0f);
            g.text(this.font, Component.translatable(type.translationKey()), rx + 24, ry + 4, TEXT, false);
            String pct = (p.permille() / 10) + " %";
            g.text(this.font, pct, rx + 136 - this.font.width(pct), ry + 4, MUTED, false);
            this.bar(g, rx + 24, ry + 16, 90, 6, p.permille() / 1000.0f, 0xFF5E8C3A);
            if (v.isOwner()) {
                final int index = i;
                this.smallButton(g, rx + 120, ry + 13, 16, 14, Component.literal("✘"), true, mouseX, mouseY,
                        () -> this.send(TownActions.CANCEL, index, ""),
                        List.of(Component.translatable("gui.kingdomsatwar.town.cancel"),
                                Component.translatable("gui.kingdomsatwar.town.cancel.refund").withStyle(ChatFormatting.GRAY)));
            }
            ry += 33;
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
        int cw = 73;
        int ch = 42;
        for (BuildingType type : BuildingType.values()) {
            int col = type.ordinal() % 4;
            int row = type.ordinal() / 4;
            int x = x0 + 2 + col * (cw + 2);
            int y = y0 + 15 + row * (ch + 2);
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
                tip.add(Component.translatable("gui.kingdomsatwar.town.build.size", type.blueprint().bounds().getXSpan(),
                        type.blueprint().bounds().getZSpan()).withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
                if (type.beds() > 0) {
                    tip.add(Component.translatable("gui.kingdomsatwar.town.build.beds", type.beds()).withStyle(ChatFormatting.DARK_GRAY).getVisualOrderText());
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
