package io.github.sirschrolli.kingdomsatwar.client.town;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.renderer.CitizenRenderer;
import io.github.sirschrolli.kingdomsatwar.economy.Coin;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.economy.Reputation;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.CitizenEntity;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading;
import io.github.sirschrolli.kingdomsatwar.economy.town.Profession;
import io.github.sirschrolli.kingdomsatwar.network.OpenTradePayload;
import io.github.sirschrolli.kingdomsatwar.network.TradePayload;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/**
 * Handel mit einem Bürger: links die Angebote (was man gibt → was man bekommt; ein Klick handelt einmal), rechts der
 * eigene Geldbeutel, wie oft noch gehandelt werden kann, bevor der Laden Pause macht (in Echtzeit), und der Rabatt durch
 * das Ansehen.
 */
public class TradeScreen extends Screen {
    private static final Identifier BACKGROUND = KingdomsAtWar.id("textures/gui/trade.png");
    private static final int WIDTH = 300;
    private static final int HEIGHT = 186;
    private static final int LIST_X = 9;
    private static final int LIST_Y = 33;
    private static final int LIST_W = 176;
    private static final int ROW_H = 20;
    private static final int ROWS = 7;
    private static final int PANEL_X = 194;

    private OpenTradePayload data;
    /** Wann (Echtzeit) die Daten kamen – für den Countdown der Pause. */
    private long receivedAt;
    private int left;
    private int top;
    private int scroll;
    private int hovered = -1;

    public TradeScreen(OpenTradePayload data) {
        super(Component.translatable("gui.kingdomsatwar.trade.title"));
        this.update(data);
    }

    public int entityId() {
        return this.data.entity();
    }

    public void update(OpenTradePayload data) {
        this.data = data;
        this.receivedAt = System.currentTimeMillis();
        this.scroll = Mth.clamp(this.scroll, 0, Math.max(0, data.offers().size() - ROWS));
    }

    @Override
    protected void init() {
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;
    }

    private Profession profession() {
        return Profession.values()[Mth.clamp(this.data.profession(), 0, Profession.values().length - 1)];
    }

    private long closedRemaining() {
        if (this.data.closedMillis() <= 0) {
            return 0;
        }
        return Math.max(0, this.data.closedMillis() - (System.currentTimeMillis() - this.receivedAt));
    }

    private boolean closed() {
        return this.closedRemaining() > 0;
    }

    @Override
    public void tick() {
        Entity e = this.minecraft.level == null ? null : this.minecraft.level.getEntity(this.data.entity());
        if (!(e instanceof CitizenEntity citizen) || !citizen.isAlive() || this.minecraft.player == null
                || this.minecraft.player.distanceToSqr(citizen) > 8.0 * 8.0) {
            this.onClose();
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int index = this.rowAt(event.x(), event.y());
        if (index >= 0 && !this.closed()) {
            Trading.Offer offer = this.data.offers().get(index);
            if (this.canDo(offer)) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new TradePayload(this.data.entity(), index));
            } else {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0f));
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll = Mth.clamp(this.scroll - (int) Math.signum(scrollY), 0, Math.max(0, this.data.offers().size() - ROWS));
        return true;
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

    private int rowAt(double mx, double my) {
        int x = this.left + LIST_X;
        int y = this.top + LIST_Y;
        if (mx < x || mx >= x + LIST_W || my < y || my >= y + ROWS * ROW_H) {
            return -1;
        }
        int index = this.scroll + (int) ((my - y) / ROW_H);
        return index < this.data.offers().size() ? index : -1;
    }

    /** Hat der Spieler, was das Angebot verlangt (Ware und Münzen)? */
    private boolean canDo(Trading.Offer offer) {
        var player = this.minecraft.player;
        if (player == null) {
            return false;
        }
        if (player.hasInfiniteMaterials()) {
            return true;
        }
        if (!offer.give().isEmpty() && player.getInventory().countItem(offer.give().getItem()) < offer.give().getCount()) {
            return false;
        }
        return Coins.total(player) >= offer.giveCoins();
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
        int x0 = this.left;
        int y0 = this.top;
        Profession profession = this.profession();
        Entity e = this.minecraft.level == null ? null : this.minecraft.level.getEntity(this.data.entity());
        int variant = e instanceof CitizenEntity c ? c.getVariant() : 0;
        Component name = e != null ? e.getName() : Component.translatable(profession.translationKey());
        // Kopfzeile: Gesicht, Name, Beruf
        Identifier skin = CitizenRenderer.texture(profession, Math.min(variant, profession.variants() - 1));
        g.fill(x0 + 10, y0 + 9, x0 + 28, y0 + 27, 0xFF0D1533);
        g.blit(RenderPipelines.GUI_TEXTURED, skin, x0 + 11, y0 + 10, 8.0f, 8.0f, 16, 16, 8, 8, 128, 64);
        g.blit(RenderPipelines.GUI_TEXTURED, skin, x0 + 11, y0 + 10, 40.0f, 8.0f, 16, 16, 8, 8, 128, 64);
        g.text(this.font, name.copy().withStyle(ChatFormatting.BOLD), x0 + 33, y0 + 14, TownScreen.GOLD_TEXT, true);
        Component job = Component.translatable(profession.translationKey());
        g.text(this.font, job, x0 + WIDTH - 12 - this.font.width(job), y0 + 14, TownScreen.RIBBON_TEXT, true);

        this.drawOffers(g, x0 + LIST_X, y0 + LIST_Y, mouseX, mouseY);
        this.drawPanel(g, x0 + PANEL_X, y0 + LIST_Y, mouseX, mouseY);
        super.extractRenderState(g, mouseX, mouseY, a);
    }

    private void drawOffers(GuiGraphicsExtractor g, int x, int y, int mouseX, int mouseY) {
        List<Trading.Offer> offers = this.data.offers();
        boolean closed = this.closed();
        this.hovered = this.rowAt(mouseX, mouseY);
        for (int i = 0; i < ROWS && this.scroll + i < offers.size(); i++) {
            int index = this.scroll + i;
            Trading.Offer offer = offers.get(index);
            int ry = y + i * ROW_H;
            boolean can = !closed && this.canDo(offer);
            boolean hover = index == this.hovered;
            g.fill(x, ry, x + LIST_W, ry + ROW_H - 2, hover && can ? TownScreen.CARD_HOVER : TownScreen.CARD);
            g.outline(x, ry, LIST_W, ROW_H - 2, hover && can ? 0xFFAE8A52 : TownScreen.CARD_BORDER);
            int textColor = can ? TownScreen.TEXT : TownScreen.BAD;
            // was man gibt
            int gx = x + 4;
            if (!offer.give().isEmpty()) {
                g.item(offer.give(), gx, ry + 1);
                g.itemDecorations(this.font, offer.give(), gx, ry + 1);
                gx += 20;
            }
            if (offer.giveCoins() > 0) {
                gx += this.coins(g, gx, ry + 5, offer.giveCoins(), textColor) + 2;
            } else if (offer.give().isEmpty()) {
                g.text(this.font, Component.translatable("gui.kingdomsatwar.town.free"), gx, ry + 5, TownScreen.GOOD, false);
            }
            // Pfeil
            g.text(this.font, "→", x + 92, ry + 5, TownScreen.MUTED, false);
            // was man bekommt
            int bx = x + 106;
            if (!offer.get().isEmpty()) {
                g.item(offer.get(), bx, ry + 1);
                g.itemDecorations(this.font, offer.get(), bx, ry + 1);
                bx += 20;
            }
            if (offer.getCoins() > 0) {
                this.coins(g, bx, ry + 5, offer.getCoins(), TownScreen.TEXT);
            }
            if (hover) {
                ItemStack shown = mouseX < x + 92 ? offer.give() : offer.get();
                if (!shown.isEmpty() && (mouseX >= x + 4 && mouseX < x + 20 || mouseX >= x + 106 && mouseX < x + 122)) {
                    g.setTooltipForNextFrame(this.font, shown, mouseX, mouseY);
                } else if (!can && !closed) {
                    g.setTooltipForNextFrame(this.font, Component.translatable(!offer.give().isEmpty()
                            ? "gui.kingdomsatwar.trade.missing_items" : "gui.kingdomsatwar.trade.missing_coins"), mouseX, mouseY);
                }
            }
        }
        if (closed) {
            g.fill(x, y, x + LIST_W, y + ROWS * ROW_H - 2, 0x8CE6D3A8);
            Component text = Component.translatable("gui.kingdomsatwar.trade.closed").withStyle(ChatFormatting.BOLD);
            g.text(this.font, text, x + (LIST_W - this.font.width(text)) / 2, y + 56, TownScreen.BAD, false);
            Component time = Component.translatable("gui.kingdomsatwar.trade.opens_in", formatTime(this.closedRemaining()));
            g.text(this.font, time, x + (LIST_W - this.font.width(time)) / 2, y + 70, TownScreen.TEXT, false);
        }
        int max = Math.max(0, offers.size() - ROWS);
        if (max > 0) {
            int sx = x + LIST_W + 2;
            int sh = ROWS * ROW_H - 2;
            g.fill(sx, y, sx + 3, y + sh, 0x40765A34);
            int knob = Math.max(10, sh * ROWS / offers.size());
            int ky = y + (sh - knob) * this.scroll / max;
            g.fill(sx, ky, sx + 3, ky + knob, 0xFF8A6A3E);
        }
    }

    private void drawPanel(GuiGraphicsExtractor g, int x, int y, int mouseX, int mouseY) {
        var player = this.minecraft.player;
        int w = WIDTH - PANEL_X - 10;
        g.fill(x - 2, y, x - 1, y + ROWS * ROW_H - 2, 0x40765A34);
        // Geldbeutel: drei Münzen nebeneinander
        g.text(this.font, Component.translatable("gui.kingdomsatwar.town.purse").withStyle(ChatFormatting.BOLD), x, y + 1, TownScreen.TEXT, false);
        int px = x;
        for (int i = Coin.values().length - 1; i >= 0; i--) {
            Coin coin = Coin.values()[i];
            int n = player == null ? 0 : Coins.count(player, coin);
            g.item(new ItemStack(coin.item()), px, y + 11);
            String count = String.valueOf(n);
            g.text(this.font, count, px + 8 - this.font.width(count) / 2, y + 28, TownScreen.TEXT, false);
            px += 32;
        }
        int py = y + 40;
        g.fill(x, py, x + w, py + 1, 0x40765A34);
        py += 5;
        if (this.data.free()) {
            py = this.wrapped(g, Component.translatable("gui.kingdomsatwar.trade.free"), x, py, w, TownScreen.GOOD) + 3;
        }
        // Handel bis zur Pause
        int limit = this.data.limit();
        int left = this.closed() ? 0 : this.data.tradesLeft();
        py = this.wrapped(g, Component.translatable("gui.kingdomsatwar.trade.left", left), x, py, w, TownScreen.TEXT) + 1;
        int bw = w;
        g.fill(x, py, x + bw, py + 5, 0xFF8C7650);
        g.fill(x + 1, py + 1, x + bw - 1, py + 4, 0xFFC9B48A);
        int fw = Math.round((bw - 2) * Mth.clamp(left / (float) Math.max(1, limit), 0.0f, 1.0f));
        if (fw > 0) {
            g.fill(x + 1, py + 1, x + 1 + fw, py + 4, 0xFFB0802A);
        }
        py += 9;
        int minutes = Math.max(1, this.data.pauseMinutes());
        Component duration = minutes % 60 == 0 ? Component.translatable("gui.kingdomsatwar.trade.hours", minutes / 60)
                : Component.translatable("gui.kingdomsatwar.trade.minutes", minutes);
        py = this.wrapped(g, Component.translatable("gui.kingdomsatwar.trade.pause_hint", duration).withStyle(ChatFormatting.ITALIC), x, py, w,
                TownScreen.MUTED) + 4;
        // Ansehen und Rabatt
        if (player != null) {
            Reputation.Rank rank = Reputation.rank(player);
            g.text(this.font, Component.translatable("gui.kingdomsatwar.town.reputation").withStyle(ChatFormatting.ITALIC), x, py,
                    TownScreen.MUTED, false);
            py += 10;
            g.text(this.font, Component.translatable(rank.translationKey()).withStyle(ChatFormatting.BOLD), x, py, TownScreen.TEXT, false);
            py += 10;
            this.wrapped(g, Component.translatable("gui.kingdomsatwar.trade.discount", this.data.discount()), x, py, w, TownScreen.MUTED);
        }
    }

    private int wrapped(GuiGraphicsExtractor g, Component text, int x, int y, int width, int color) {
        for (FormattedCharSequence line : this.font.split(text, width)) {
            g.text(this.font, line, x, y, color, false);
            y += 9;
        }
        return y;
    }

    /** Münzbetrag mit kleinen Münzen; gibt die Breite zurück. */
    private int coins(GuiGraphicsExtractor g, int x, int y, int amount, int color) {
        int cx = x;
        int rest = Math.max(0, amount);
        for (int i = Coin.values().length - 1; i >= 0; i--) {
            Coin coin = Coin.values()[i];
            int n = rest / coin.value();
            rest -= n * coin.value();
            if (n > 0) {
                g.pose().pushMatrix();
                g.pose().translate(cx, y - 1);
                g.pose().scale(0.625f, 0.625f);
                g.item(new ItemStack(coin.item()), 0, 0);
                g.pose().popMatrix();
                String s = String.valueOf(n);
                g.text(this.font, s, cx + 11, y, color, false);
                cx += 11 + this.font.width(s) + 3;
            }
        }
        return cx - x;
    }

    static String formatTime(long millis) {
        long s = Math.max(0, millis / 1000);
        long h = s / 3600;
        long m = (s / 60) % 60;
        long sec = s % 60;
        return h > 0 ? String.format("%d:%02d:%02d", h, m, sec) : String.format("%d:%02d", m, sec);
    }
}
