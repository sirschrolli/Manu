package io.github.sirschrolli.kingdomsatwar.client.hero;

import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.hero.Crystals;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import io.github.sirschrolli.kingdomsatwar.magic.Mana;
import io.github.sirschrolli.kingdomsatwar.network.HeroTalentPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

/**
 * Das Charakterfenster (Taste K) im dunkelgoldenen Stil der Heldenstufe:
 *
 * <ul>
 *   <li>links der Held selbst (er folgt der Maus), sein Rang, seine Werte (Leben, Mana, Rüstung, Schaden), was er gefunden
 *       hat (Herz- und Manakristalle, Biome) und die Knöpfe;</li>
 *   <li>rechts oben das Rangabzeichen mit der Stufe (Strahlen drehen sich dahinter), die Leiste der Heldenerfahrung (sie
 *       füllt sich beim Öffnen) und der Weg zur Legende: die sechs Ränge auf einer Linie, der nächste leuchtet;</li>
 *   <li>darunter die sechs Talente als Karten: Bild, Name, was die Punkte bringen, zwanzig Kästchen in der Farbe des Talents
 *       und ein goldener Plus-Knopf, der pulsiert, solange Punkte frei sind. Ein neuer Punkt blitzt auf und sprüht Funken.</li>
 * </ul>
 * Im Hintergrund steigen langsam Glutfunken auf.
 */
public class CharacterScreen extends Screen {
    private static final int WIDTH = 324;
    private static final int HEIGHT = 246;
    private static final int LEFT_W = 90;
    private static final int RIGHT_X = 116;
    private static final int CARD_Y = 108;
    private static final int CARD_H = 20;
    private static final int CARD_GAP = 2;
    private static final Identifier HEART = Identifier.withDefaultNamespace("hud/heart/full");
    private static final Identifier ARMOR = Identifier.withDefaultNamespace("hud/armor_full");

    private int left;
    private int top;
    private long openedAt;
    private final List<HeroButton> plus = new ArrayList<>();
    private @Nullable HeroButton reset;
    /** Wann zuletzt ein Punkt in ein Talent ging (für das Aufblitzen), und welches Kästchen. */
    private final long[] spentAt = new long[HeroLevel.Talent.values().length];
    private final int[] lastPoints = new int[HeroLevel.Talent.values().length];

    public CharacterScreen() {
        super(Component.translatable("gui.kingdomsatwar.character.title"));
        java.util.Arrays.fill(this.lastPoints, -1);
    }

    @Override
    protected void init() {
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;
        if (this.openedAt == 0L) {
            this.openedAt = Util.getMillis();
        }
        this.plus.clear();
        int rw = WIDTH - RIGHT_X - 12;
        for (HeroLevel.Talent talent : HeroLevel.Talent.values()) {
            int y = this.top + CARD_Y + talent.ordinal() * (CARD_H + CARD_GAP) + 4;
            this.plus.add(this.addRenderableWidget(HeroButton.plus(this.left + RIGHT_X + rw - 16, y,
                    Component.translatable("gui.kingdomsatwar.character.talent.spend"),
                    button -> ClientPacketDistributor.sendToServer(new HeroTalentPayload(talent.ordinal())))));
        }
        int bx = this.left + 12;
        this.reset = this.addRenderableWidget(HeroButton.text(bx, this.top + HEIGHT - 58, LEFT_W,
                Component.translatable("gui.kingdomsatwar.character.reset"),
                button -> ClientPacketDistributor.sendToServer(new HeroTalentPayload(HeroTalentPayload.RESET))));
        this.addRenderableWidget(HeroButton.text(bx, this.top + HEIGHT - 34, LEFT_W, Component.translatable("gui.kingdomsatwar.character.path"),
                button -> this.minecraft.gui.setScreen(new HeroPathScreen())));
        this.updateButtons();
    }

    @Override
    public void tick() {
        this.updateButtons();
    }

    private void updateButtons() {
        Player player = this.minecraft.player;
        if (player == null || this.reset == null) {
            return;
        }
        HeroLevel.Hero hero = HeroLevel.of(player);
        for (HeroLevel.Talent talent : HeroLevel.Talent.values()) {
            HeroButton button = this.plus.get(talent.ordinal());
            boolean full = hero.points(talent) >= HeroLevel.MAX_TALENT;
            button.active = hero.unspent() > 0 && !full;
            button.setTooltip(Tooltip.create(Component.translatable(full ? "gui.kingdomsatwar.character.talent.full" : hero.unspent() > 0
                    ? "gui.kingdomsatwar.character.talent.spend" : "gui.kingdomsatwar.character.talent.no_points", HeroLevel.MAX_TALENT)));
            int points = hero.points(talent);
            int before = this.lastPoints[talent.ordinal()];
            if (before >= 0 && points > before) {
                this.spentAt[talent.ordinal()] = Util.getMillis();
            }
            this.lastPoints[talent.ordinal()] = points;
        }
        int cost = HeroLevel.resetCost(hero);
        boolean affordable = player.hasInfiniteMaterials() || Coins.canAfford(player, cost);
        this.reset.active = hero.spent() > 0 && affordable;
        this.reset.setTooltip(Tooltip.create(hero.spent() == 0 ? Component.translatable("gui.kingdomsatwar.character.reset.none")
                : Component.translatable("gui.kingdomsatwar.character.reset.hint", Coins.format(cost))
                        .withStyle(affordable ? ChatFormatting.WHITE : ChatFormatting.RED)));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.minecraft.options.keyInventory.matches(event)
                || io.github.sirschrolli.kingdomsatwar.client.ModKeyMappings.CHARACTER.matches(event)) {
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

    private float age() {
        return (Util.getMillis() - this.openedAt) / 1000.0f;
    }

    // ------------------------------------------------------------------------------------------------ Zeichnen

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractBackground(g, mouseX, mouseY, a);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, HeroGui.PANEL, this.left, this.top, WIDTH, HEIGHT);
        this.embers(g);
        // links: der Held in seinem Rahmen
        g.blitSprite(RenderPipelines.GUI_TEXTURED, HeroGui.INSET, this.left + 12, this.top + 12, LEFT_W, 98);
        // die goldene Trennlinie zwischen den Spalten, oben und unten ein Ziernagel
        int lx = this.left + RIGHT_X - 9;
        g.fillGradient(lx, this.top + 16, lx + 1, this.top + HEIGHT - 16, 0x00B5832C, 0xFFB5832C);
        g.fill(lx, this.top + HEIGHT / 2 - 40, lx + 1, this.top + HEIGHT / 2 + 40, 0xFFE2B24A);
        g.fill(lx - 1, this.top + HEIGHT / 2 - 1, lx + 2, this.top + HEIGHT / 2 + 2, 0xFFFFE7A0);
    }

    /** Glutfunken, die langsam im Fenster aufsteigen und flackern (ohne eigenen Zustand: alles aus der Uhrzeit). */
    private void embers(GuiGraphicsExtractor g) {
        float time = HeroGui.seconds();
        g.enableScissor(this.left + 10, this.top + 10, this.left + WIDTH - 10, this.top + HEIGHT - 10);
        for (int i = 0; i < 26; i++) {
            float speed = 7.0f + (i * 37 % 11);
            float offset = (i * 97.3f) % (HEIGHT - 20);
            float y = this.top + HEIGHT - 12 - ((time * speed + offset) % (HEIGHT - 24));
            float x = this.left + 14 + ((i * 71.7f) % (WIDTH - 28)) + Mth.sin(time * 0.7f + i) * 6.0f;
            float life = ((time * speed + offset) % (HEIGHT - 24)) / (HEIGHT - 24);
            float flicker = 0.6f + 0.4f * Mth.sin(time * 5.0f + i * 1.7f);
            float k = Mth.sin(life * Mth.PI) * flicker;
            HeroGui.light(g, HeroGui.GLOW, x, y, 7.0f + (i % 4), 0.0f, i % 3 == 0 ? 0xFFFF9A3C : 0xFFF2C14E, 0.22f * k);
        }
        g.disableScissor();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        Player player = this.minecraft.player;
        if (player != null) {
            this.drawContent(g, player, mouseX, mouseY);
        }
        // die Knöpfe zuletzt: über den Karten
        super.extractRenderState(g, mouseX, mouseY, a);
    }

    private void drawContent(GuiGraphicsExtractor g, Player player, int mouseX, int mouseY) {
        HeroLevel.Hero hero = HeroLevel.of(player);
        int x0 = this.left;
        int y0 = this.top;
        InventoryScreen.extractEntityInInventoryFollowsMouse(g, x0 + 13, y0 + 13, x0 + 12 + LEFT_W - 1, y0 + 109, 40, 0.0625f, mouseX, mouseY,
                player);
        this.drawLeft(g, player, hero, x0 + 12, y0 + 114, mouseX, mouseY);
        int rx = x0 + RIGHT_X;
        int rw = WIDTH - RIGHT_X - 12;
        this.drawHeader(g, player, hero, rx, y0 + 12, rw, mouseX, mouseY);
        this.drawTrack(g, hero, rx, y0 + 68, rw, mouseX, mouseY);
        int unspent = hero.unspent();
        Component points = unspent > 0 ? Component.translatable("gui.kingdomsatwar.character.points", unspent)
                : Component.translatable("gui.kingdomsatwar.character.points.none");
        HeroGui.text(g, this.font, points, rx + 2, y0 + CARD_Y - 11, unspent > 0 ? HeroGui.GOOD : HeroGui.MUTED);
        if (unspent > 0) {
            float pulse = 0.5f + 0.5f * Mth.sin(HeroGui.seconds() * 4.5f);
            int sx = rx + 2 + this.font.width(points) + 4;
            HeroGui.light(g, HeroGui.GLOW, sx + 3.5f, y0 + CARD_Y - 7.5f, 18.0f, 0.0f, HeroGui.GOLD, 0.3f + 0.4f * pulse);
            g.text(this.font, "★", sx, y0 + CARD_Y - 11, ARGB.color(Math.round(170 + 85 * pulse), 0xFFE7A0), false);
        }
        Component spent = Component.translatable("gui.kingdomsatwar.character.spent", hero.spent(), hero.earned());
        g.text(this.font, spent, rx + rw - this.font.width(spent) - 2, y0 + CARD_Y - 11, HeroGui.MUTED, false);
        for (HeroLevel.Talent talent : HeroLevel.Talent.values()) {
            this.drawTalent(g, hero, talent, rx, y0 + CARD_Y + talent.ordinal() * (CARD_H + CARD_GAP), rw, mouseX, mouseY);
        }
    }

    /** Kopf: Abzeichen mit Stufe, "Heldenstufe N", Rang und Name, die Leiste (füllt sich beim Öffnen) mit Zahlen. */
    private void drawHeader(GuiGraphicsExtractor g, Player player, HeroLevel.Hero hero, int x, int y, int width, int mouseX, int mouseY) {
        HeroLevel.Rank rank = hero.rank();
        int color = HeroGui.rankColor(rank);
        float open = HeroGui.easeOutBack(HeroGui.span(this.age(), 0.0f, 0.5f));
        HeroGui.emblem(g, this.font, rank, hero.level(), x + 25, y + 25, 0.6f + 0.4f * open, Math.min(1.0f, this.age() * 4.0f), 1.0f, true);
        int tx = x + 56;
        g.pose().pushMatrix();
        g.pose().translate(tx, y + 3);
        g.pose().scale(1.25f, 1.25f);
        Component title = Component.translatable("gui.kingdomsatwar.character.level", hero.level());
        g.text(this.font, title, 1, 1, 0xFF0B0910, false);
        g.text(this.font, title, 0, 0, 0xFFFFE7A0, false);
        g.pose().popMatrix();
        Component rankName = Component.translatable(rank.translationKey());
        HeroGui.text(g, this.font, rankName, tx, y + 17, color);
        Component name = player.getName();
        g.text(this.font, name, x + width - this.font.width(name) - 2, y + 17, HeroGui.MUTED, false);
        // Leiste
        float progress = HeroLevel.progress(hero.experience()) * HeroGui.easeOutCubic(HeroGui.span(this.age(), 0.15f, 0.9f));
        int bw = width - 56;
        HeroGui.bar(g, tx, y + 29, bw, 9, progress, progress, 0.0f, color);
        boolean max = hero.level() >= HeroLevel.MAX_LEVEL;
        Component numbers = max ? Component.translatable("gui.kingdomsatwar.character.experience.max", HeroGui.number(hero.experience()))
                : Component.translatable("gui.kingdomsatwar.character.experience", HeroGui.number(hero.experience()),
                        HeroGui.number(HeroLevel.total(hero.level() + 1)));
        g.pose().pushMatrix();
        g.pose().translate(tx + bw, y + 41);
        g.pose().scale(0.75f, 0.75f);
        g.text(this.font, numbers, -this.font.width(numbers), 0, HeroGui.MUTED, false);
        g.pose().popMatrix();
        if (mouseX >= tx && mouseX < tx + bw && mouseY >= y + 27 && mouseY < y + 48) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("gui.kingdomsatwar.character.sources.title").withStyle(ChatFormatting.GOLD));
            if (!max) {
                lines.add(Component.translatable("gui.kingdomsatwar.character.next", HeroGui.number(HeroLevel.total(hero.level() + 1) - hero.experience()),
                        hero.level() + 1).withStyle(ChatFormatting.YELLOW));
            }
            lines.add(Component.translatable("gui.kingdomsatwar.character.sources").withStyle(ChatFormatting.GRAY));
            g.setTooltipForNextFrame(this.font, this.lines(lines), mouseX, mouseY);
        }
    }

    /** Der Weg zur Legende: sechs kleine Abzeichen auf einer Linie, erreichte golden, der nächste Rang leuchtet. */
    private void drawTrack(GuiGraphicsExtractor g, HeroLevel.Hero hero, int x, int y, int width, int mouseX, int mouseY) {
        HeroLevel.Rank[] ranks = HeroLevel.Rank.values();
        int level = hero.level();
        int n = ranks.length;
        float step = (width - 22) / (float) (n - 1);
        int ly = y + 7;
        // die Linie: erreicht golden, bis zum nächsten Rang anteilig gefüllt
        int startX = x + 11;
        int endX = Math.round(x + 11 + step * (n - 1));
        g.fill(startX, ly, endX, ly + 2, 0xFF2E2840);
        float reached = 0.0f;
        for (int i = 0; i < n - 1; i++) {
            if (level >= ranks[i + 1].minLevel()) {
                reached = i + 1;
            } else if (level >= ranks[i].minLevel()) {
                int from = HeroLevel.total(ranks[i].minLevel());
                int to = HeroLevel.total(ranks[i + 1].minLevel());
                reached = i + Mth.clamp((hero.experience() - from) / (float) (to - from), 0.0f, 1.0f);
            }
        }
        reached *= HeroGui.easeOutCubic(HeroGui.span(this.age(), 0.2f, 1.0f));
        int fillTo = Math.round(startX + step * reached);
        g.fillGradient(startX, ly, fillTo, ly + 2, 0xFFFFE7A0, 0xFFB5832C);
        HeroLevel.Rank current = hero.rank();
        HeroLevel.@Nullable Rank next = current.next();
        for (int i = 0; i < n; i++) {
            HeroLevel.Rank rank = ranks[i];
            float cx = x + 11 + step * i;
            boolean got = level >= rank.minLevel();
            if (rank == next) {
                float pulse = 0.5f + 0.5f * Mth.sin(HeroGui.seconds() * 3.0f);
                HeroGui.light(g, HeroGui.GLOW, cx, ly + 1, 34.0f, 0.0f, rank.color(), 0.25f + 0.3f * pulse);
            } else if (rank == current) {
                HeroGui.light(g, HeroGui.GLOW, cx, ly + 1, 30.0f, 0.0f, HeroGui.rankColor(rank), 0.35f);
            }
            g.blitSprite(RenderPipelines.GUI_TEXTURED, HeroGui.badge(rank), Math.round(cx - 7.5f), ly - 6, HeroGui.BADGE, HeroGui.BADGE,
                    got || rank == next ? 0xFFFFFFFF : 0xFF5A5468);
            String label = String.valueOf(rank.minLevel());
            g.pose().pushMatrix();
            g.pose().translate(cx, ly + 10);
            g.pose().scale(0.75f, 0.75f);
            g.text(this.font, label, -this.font.width(label) / 2, 0, got ? HeroGui.rankColor(rank) : HeroGui.MUTED, false);
            g.pose().popMatrix();
            if (Math.abs(mouseX - cx) <= 8 && mouseY >= ly - 7 && mouseY <= ly + 16) {
                List<Component> lines = new ArrayList<>();
                lines.add(Component.translatable(rank.translationKey()).withColor(rank.color() & 0xFFFFFF).withStyle(ChatFormatting.BOLD));
                lines.add(Component.translatable("gui.kingdomsatwar.character.rank.from", rank.minLevel(), HeroGui.number(HeroLevel.total(rank.minLevel())))
                        .withStyle(ChatFormatting.GRAY));
                if (rank.ordinal() > 0) {
                    lines.add(Component.translatable("gui.kingdomsatwar.character.rank.bonus", HeroLevel.RANK_BONUS).withStyle(ChatFormatting.GREEN));
                }
                if (rank == HeroLevel.Rank.LEGEND) {
                    lines.add(Component.translatable("gui.kingdomsatwar.character.rank.reward", Component.keybind("key.kingdomsatwar.pat"))
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                }
                lines.add(Component.translatable(got ? "gui.kingdomsatwar.character.rank.reached" : "gui.kingdomsatwar.character.rank.ahead")
                        .withStyle(got ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY));
                g.setTooltipForNextFrame(this.font, this.lines(lines), mouseX, mouseY);
            }
        }
    }

    /** Eine Talentkarte: Bild, Name, was die Punkte bringen, zwanzig Kästchen und die Zahl der Punkte. */
    private void drawTalent(GuiGraphicsExtractor g, HeroLevel.Hero hero, HeroLevel.Talent talent, int x, int y, int width, int mouseX, int mouseY) {
        int points = hero.points(talent);
        boolean hover = mouseX >= x && mouseX < x + width - 18 && mouseY >= y && mouseY < y + CARD_H;
        // die Karten gleiten nacheinander herein
        float slide = HeroGui.easeOutCubic(HeroGui.span(this.age(), 0.05f + talent.ordinal() * 0.05f, 0.35f + talent.ordinal() * 0.05f));
        int dx = Math.round(14 * (1.0f - slide));
        x += dx;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, hover ? HeroGui.CARD_HOVER : HeroGui.CARD, x, y, width, CARD_H, ARGB.white(slide));
        if (slide < 0.5f) {
            return;
        }
        int color = talent.color();
        g.item(talent.icon(), x + 2, y + 2);
        int nx = x + 21;
        Component name = Component.translatable(talent.translationKey());
        HeroGui.text(g, this.font, name, nx, y + 2, points > 0 ? HeroGui.TEXT : HeroGui.MUTED);
        Component effect = effect(talent, points);
        g.text(this.font, effect, x + width - 20 - this.font.width(effect), y + 2, points > 0 ? color : HeroGui.MUTED, false);
        // zwanzig Kästchen, nach je fünf ein Spalt
        long now = Util.getMillis();
        float flash = 1.0f - Mth.clamp((now - this.spentAt[talent.ordinal()]) / 600.0f, 0.0f, 1.0f);
        int py = y + 13;
        for (int i = 0; i < HeroLevel.MAX_TALENT; i++) {
            int px = nx + i * 4 + (i / 5) * 2;
            if (i < points) {
                g.fillGradient(px, py, px + 3, py + 4, ARGB.srgbLerp(0.4f, color, 0xFFFFFFFF), ARGB.srgbLerp(0.25f, color, 0xFF000000));
                if (i == points - 1 && flash > 0.0f) {
                    g.fill(px - 1, py - 1, px + 4, py + 5, ARGB.color(Math.round(230 * flash), 255, 255, 255));
                    HeroGui.light(g, HeroGui.GLOW, px + 1.5f, py + 2.0f, 26.0f, 0.0f, color, flash);
                    for (int k = 0; k < 3; k++) {
                        float rise = 1.0f - flash;
                        HeroGui.light(g, HeroGui.SPARKLE, px + 1.5f + (k - 1) * 5.0f * rise, py - 10.0f * rise - k * 2.0f, 7.0f, rise * 3.0f,
                                0xFFFFFFFF, flash);
                    }
                }
            } else {
                g.fill(px, py, px + 3, py + 4, 0xFF15121F);
                g.fill(px, py + 3, px + 3, py + 4, 0xFF2E2840);
            }
        }
        String count = points + "/" + HeroLevel.MAX_TALENT;
        g.pose().pushMatrix();
        g.pose().translate(x + width - 20, y + 13);
        g.pose().scale(0.75f, 0.75f);
        g.text(this.font, count, -this.font.width(count), 0, points >= HeroLevel.MAX_TALENT ? HeroGui.GOLD : HeroGui.MUTED, false);
        g.pose().popMatrix();
        if (hover) {
            g.setTooltipForNextFrame(this.font, this.lines(List.of(
                    Component.translatable(talent.translationKey()).withColor(color & 0xFFFFFF)
                            .append(Component.literal("  " + points + "/" + HeroLevel.MAX_TALENT).withStyle(ChatFormatting.YELLOW)),
                    Component.translatable(talent.translationKey() + ".desc").withStyle(ChatFormatting.GRAY),
                    Component.translatable("gui.kingdomsatwar.character.talent.max", effect(talent, HeroLevel.MAX_TALENT)).withStyle(ChatFormatting.DARK_GRAY))),
                    mouseX, mouseY);
        }
    }

    /** Was die Punkte eines Talents zusammen bringen ("+1,5 Schaden"). */
    static Component effect(HeroLevel.Talent talent, int points) {
        return Component.translatable(talent.translationKey() + ".effect", HeroGui.decimal(talent.value(points)));
    }

    /** Links unter dem Bild: Rang, Werte mit kleinen Bildern, dann Kristalle und Biome. */
    private void drawLeft(GuiGraphicsExtractor g, Player player, HeroLevel.Hero hero, int x, int y, int mouseX, int mouseY) {
        int line = 11;
        int w = LEFT_W;
        HeroLevel.Rank rank = hero.rank();
        Component rankName = Component.translatable(rank.translationKey());
        HeroGui.centered(g, this.font, rankName, x + w / 2.0f, y, HeroGui.rankColor(rank), 1.0f);
        int sy = y + 13;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, HEART, x + 2, sy, 9, 9);
        HeroGui.text(g, this.font, Component.literal(HeroGui.decimal(player.getMaxHealth() / 2.0)), x + 13, sy + 1, HeroGui.TEXT);
        icon(g, new ItemStack(ModItems.MANA_CRYSTAL.get()), x + 1, sy + line - 1);
        HeroGui.text(g, this.font, Component.literal(String.valueOf(Math.round(Mana.maxFor(player)))), x + 13, sy + line + 1, HeroGui.TEXT);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, ARMOR, x + 46, sy, 9, 9);
        HeroGui.text(g, this.font, Component.literal(HeroGui.decimal(player.getAttributeValue(Attributes.ARMOR))), x + 57, sy + 1, HeroGui.TEXT);
        icon(g, new ItemStack(Items.IRON_SWORD), x + 45, sy + line - 1);
        HeroGui.text(g, this.font, Component.literal(HeroGui.decimal(damage(player, hero))), x + 57, sy + line + 1, HeroGui.TEXT);
        if (mouseX >= x && mouseX < x + w && mouseY >= sy && mouseY < sy + 2 * line) {
            g.setTooltipForNextFrame(this.font, this.lines(List.of(Component.translatable("gui.kingdomsatwar.character.stats")
                    .withStyle(ChatFormatting.GOLD), Component.translatable("gui.kingdomsatwar.character.stats.hint").withStyle(ChatFormatting.GRAY))),
                    mouseX, mouseY);
        }
        // was er gefunden hat
        Crystals.Count crystals = Crystals.of(player);
        int cy = sy + 2 * line + 5;
        g.fillGradient(x + 4, cy - 3, x + w / 2, cy - 2, 0x00B5832C, 0xFFB5832C);
        g.fillGradient(x + w / 2, cy - 3, x + w - 4, cy - 2, 0xFFB5832C, 0x00B5832C);
        icon(g, new ItemStack(ModItems.HEART_CRYSTAL.get()), x + 1, cy);
        g.text(this.font, crystals.hearts() + "/" + Crystals.MAX, x + 13, cy + 2, HeroGui.MUTED, false);
        icon(g, new ItemStack(ModItems.MANA_CRYSTAL.get()), x + 45, cy);
        g.text(this.font, crystals.mana() + "/" + Crystals.MAX, x + 57, cy + 2, HeroGui.MUTED, false);
        icon(g, new ItemStack(Items.FILLED_MAP), x + 1, cy + line);
        g.text(this.font, String.valueOf(hero.discoveries()), x + 13, cy + line + 2, HeroGui.MUTED, false);
        if (mouseX >= x && mouseX < x + w && mouseY >= cy - 1 && mouseY < cy + 2 * line + 1) {
            g.setTooltipForNextFrame(this.font, this.lines(List.of(
                    Component.translatable("gui.kingdomsatwar.character.hearts", crystals.hearts(), Crystals.MAX).withStyle(ChatFormatting.RED),
                    Component.translatable("gui.kingdomsatwar.character.mana", crystals.mana(), Crystals.MAX).withStyle(ChatFormatting.AQUA),
                    Component.translatable("gui.kingdomsatwar.character.biomes", hero.discoveries()).withStyle(ChatFormatting.GREEN))),
                    mouseX, mouseY);
        }
    }

    /**
     * Der Schaden eines Schlags: Der Wert des Spielers kommt nicht vollständig beim Spiel an (Waffe und Stärke rechnet der
     * Server), also hier selbst: Grundwert, Stärke und die Waffe in der Hand.
     */
    private static double damage(Player player, HeroLevel.Hero hero) {
        double base = player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) + HeroLevel.Talent.STRENGTH.value(hero.points(HeroLevel.Talent.STRENGTH));
        return player.getMainHandItem().getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).compute(Attributes.ATTACK_DAMAGE, base,
                net.minecraft.world.entity.EquipmentSlot.MAINHAND);
    }

    /** Ein Gegenstand als kleines Bild (11 Pixel). */
    private static void icon(GuiGraphicsExtractor g, ItemStack stack, int x, int y) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(0.6875f, 0.6875f);
        g.item(stack, 0, 0);
        g.pose().popMatrix();
    }

    private List<FormattedCharSequence> lines(List<Component> components) {
        return components.stream().flatMap(c -> this.font.split(c, 220).stream()).toList();
    }
}
