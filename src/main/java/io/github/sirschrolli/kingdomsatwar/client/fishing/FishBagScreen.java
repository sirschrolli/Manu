package io.github.sirschrolli.kingdomsatwar.client.fishing;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.Coin;
import io.github.sirschrolli.kingdomsatwar.economy.Reputation;
import io.github.sirschrolli.kingdomsatwar.fishing.FishBag;
import io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies;
import io.github.sirschrolli.kingdomsatwar.network.FishBagTakePayload;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

/**
 * Das Fenster des Fischbeutels (seit 0.81): eine Tafel aus Pergament in dunklem Holz. Oben der Name und die Stufe, darunter
 * die Leiste der Füllung, dann die Fische im Beutel – je Art ein Fach mit der Zahl. Ein Klick nimmt einen Stapel heraus, mit
 * Umschalt alle dieser Art, ein Rechtsklick einen einzelnen; „Alles entnehmen“ leert den Beutel, soweit das Inventar reicht.
 * Unten steht, was die nächste Stufe beim Fischer im Dorf kostet. Das Fenster zeigt immer den Beutel in der Hand – entnommen
 * wird auf dem Server.
 */
public class FishBagScreen extends Screen {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/gui/fishing/bag.png");
    private static final int TEX = 256;
    private static final int W = 196;
    private static final int H = 186;
    private static final int GRID_X = 10;
    private static final int GRID_Y = 49;
    private static final int COLUMNS = 8;
    private static final int ROWS = 4;
    private static final int SLOT = 22;
    private static final int BUTTON_Y = 152;
    private static final int BUTTON_W = 84;
    private static final int BUTTON_H = 16;

    private static final int INK = 0xFF3A2414;
    private static final int MUTED = 0xFF7A6248;
    private static final int SEA = 0xFF1C4E6A;
    private static final int RED = 0xFF8A2416;

    private final InteractionHand hand;
    private int left;
    private int top;
    private int scroll;

    public FishBagScreen(InteractionHand hand) {
        super(Component.translatable("gui.kingdomsatwar.fish_bag.title"));
        this.hand = hand;
    }

    @Override
    protected void init() {
        this.left = (this.width - W) / 2;
        this.top = (this.height - H) / 2;
    }

    /** Der Beutel in der Hand ({@code null}: Er ist nicht mehr dort). */
    private @Nullable FishBag bag() {
        ItemStack stack = this.minecraft.player == null ? ItemStack.EMPTY : this.minecraft.player.getItemInHand(this.hand);
        return FishBag.isBag(stack) ? FishBag.of(stack) : null;
    }

    @Override
    public void tick() {
        if (this.bag() == null) {
            this.onClose();
        }
    }

    private int maxScroll(List<FishBag.Entry> entries) {
        return Math.max(0, (entries.size() + COLUMNS - 1) / COLUMNS - ROWS);
    }

    /** Die Stelle (in der Liste des Inhalts) des Fachs unter der Maus, -1: keins. */
    private int slotAt(double mx, double my, int size) {
        int x = (int) Math.floor((mx - this.left - GRID_X) / SLOT);
        int y = (int) Math.floor((my - this.top - GRID_Y) / SLOT);
        if (mx < this.left + GRID_X || my < this.top + GRID_Y || x >= COLUMNS || y >= ROWS) {
            return -1;
        }
        int index = (this.scroll + y) * COLUMNS + x;
        return index < size ? index : -1;
    }

    /** 0: „Alles entnehmen“ · 1: „Fischbuch“ · -1: kein Knopf unter der Maus. */
    private int buttonAt(double mx, double my) {
        if (my < this.top + BUTTON_Y || my >= this.top + BUTTON_Y + BUTTON_H) {
            return -1;
        }
        if (mx >= this.left + 12 && mx < this.left + 12 + BUTTON_W) {
            return 0;
        }
        return mx >= this.left + 100 && mx < this.left + 100 + BUTTON_W ? 1 : -1;
    }

    private void send(int species, int mode) {
        ClientPacketDistributor.sendToServer(new FishBagTakePayload(this.hand == InteractionHand.MAIN_HAND, species, mode));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        FishBag bag = this.bag();
        if (bag == null) {
            return super.mouseClicked(event, doubleClick);
        }
        List<FishBag.Entry> entries = bag.entries();
        int slot = this.slotAt(event.x(), event.y(), entries.size());
        if (slot >= 0) {
            int mode = event.button() == 1 ? FishBag.TAKE_ONE : event.hasShiftDown() ? FishBag.TAKE_ALL : FishBag.TAKE_STACK;
            this.send(entries.get(slot).species().ordinal() + 1, mode);
            return true;
        }
        int button = this.buttonAt(event.x(), event.y());
        if (button == 0) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            if (!entries.isEmpty()) {
                this.send(0, FishBag.TAKE_ALL);
            }
            return true;
        }
        if (button == 1) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            this.send(0, FishBagTakePayload.OPEN_BOOK);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        FishBag bag = this.bag();
        if (bag != null) {
            this.scroll = Mth.clamp(this.scroll - (int) Math.signum(scrollY), 0, this.maxScroll(bag.entries()));
        }
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

    // =====================================================================================
    // Zeichnen
    // =====================================================================================

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractBackground(g, mouseX, mouseY, a);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.left, this.top, 0.0f, 0.0f, W, H, TEX, TEX);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractRenderState(g, mouseX, mouseY, a);
        FishBag bag = this.bag();
        if (bag == null) {
            return;
        }
        int x0 = this.left;
        int y0 = this.top;
        Component title = this.title.copy().withStyle(ChatFormatting.BOLD);
        g.text(this.font, title, x0 + (W - this.font.width(title)) / 2, y0 + 10, 0xFFFFE9A8, true);
        // Stufe und Füllung
        g.text(this.font, Component.translatable("gui.kingdomsatwar.fish_bag.level", bag.level() + 1), x0 + 13, y0 + 26, SEA, false);
        boolean full = bag.free() == 0;
        Component fill = Component.translatable("gui.kingdomsatwar.fish_bag.fill", bag.total(), bag.capacity());
        g.text(this.font, fill, x0 + W - 13 - this.font.width(fill), y0 + 26, full ? RED : INK, false);
        int width = Mth.clamp(Mth.ceil(170.0f * bag.total() / Math.max(1, bag.capacity())), 0, 170);
        if (width > 0) {
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x0 + 13, y0 + 38, 0.0f, full ? 198.0f : 190.0f, width, 6, TEX, TEX);
        }

        // die Fische
        List<FishBag.Entry> entries = bag.entries();
        this.scroll = Mth.clamp(this.scroll, 0, this.maxScroll(entries));
        int hovered = this.slotAt(mouseX, mouseY, entries.size());
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int index = (this.scroll + row) * COLUMNS + col;
                if (index >= entries.size()) {
                    break;
                }
                FishBag.Entry entry = entries.get(index);
                int x = x0 + GRID_X + col * SLOT;
                int y = y0 + GRID_Y + row * SLOT;
                if (entry.species().rarity() == FishSpecies.Rarity.LEGENDARY) {
                    g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 224.0f, 0.0f, 22, 22, TEX, TEX);
                }
                if (index == hovered) {
                    g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 200.0f, 0.0f, 22, 22, TEX, TEX);
                }
                ItemStack stack = new ItemStack(entry.species().item());
                g.item(stack, x + 3, y + 3);
                g.itemDecorations(this.font, stack, x + 3, y + 3, count(entry.count()));
            }
        }
        if (entries.isEmpty()) {
            int ty = y0 + GRID_Y + 30;
            for (FormattedCharSequence line : this.font.split(Component.translatable("gui.kingdomsatwar.fish_bag.empty"), 150)) {
                g.text(this.font, line, x0 + (W - this.font.width(line)) / 2, ty, MUTED, false);
                ty += 10;
            }
        }
        int max = this.maxScroll(entries);
        if (max > 0) {
            int sx = x0 + GRID_X + COLUMNS * SLOT + 2;
            int sy = y0 + GRID_Y;
            int sh = ROWS * SLOT;
            g.fill(sx, sy, sx + 3, sy + sh, 0x303A2414);
            int knob = Math.max(10, sh * ROWS / (ROWS + max));
            g.fill(sx, sy + (sh - knob) * this.scroll / max, sx + 3, sy + (sh - knob) * this.scroll / max + knob, 0xFF8A6A3E);
        }
        this.small(g, Component.translatable("gui.kingdomsatwar.fish_bag.hint"), x0 + W / 2, y0 + 141, MUTED, W - 20);

        // Knöpfe
        int over = this.buttonAt(mouseX, mouseY);
        this.button(g, x0 + 12, y0 + BUTTON_Y, Component.translatable("gui.kingdomsatwar.fish_bag.take_all"), over == 0, !entries.isEmpty());
        this.button(g, x0 + 100, y0 + BUTTON_Y, Component.translatable("gui.kingdomsatwar.fish_bag.book"), over == 1, true);

        // was die nächste Stufe kostet
        Component next = Component.translatable("gui.kingdomsatwar.fish_bag.next", FishBag.capacity(bag.level() + 1));
        float scale = 0.75f;
        int price = FishBag.upgradePrice(bag.level());
        if (this.minecraft.player != null) {
            price = Math.max(1, Math.round(price * (100 - Reputation.rank(this.minecraft.player).discount()) / 100.0f));
        }
        int textWidth = Math.round(this.font.width(next) * scale);
        int coinsWidth = this.coinsWidth(price);
        int nx = x0 + (W - textWidth - 4 - coinsWidth) / 2;
        g.pose().pushMatrix();
        g.pose().translate(nx, y0 + 173);
        g.pose().scale(scale, scale);
        g.text(this.font, next, 0, 0, MUTED, false);
        g.pose().popMatrix();
        this.coins(g, nx + textWidth + 4, y0 + 172, price);

        if (hovered >= 0) {
            g.setTooltipForNextFrame(this.font, new ItemStack(entries.get(hovered).species().item()), mouseX, mouseY);
        }
    }

    private static String count(int count) {
        if (count >= 10000) {
            return count / 1000 + "k";
        }
        return count >= 1000 ? String.format(java.util.Locale.ROOT, "%.1fk", count / 1000.0f) : String.valueOf(count);
    }

    private void button(GuiGraphicsExtractor g, int x, int y, Component label, boolean hover, boolean active) {
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, hover && active ? 88.0f : 0.0f, 208.0f, BUTTON_W, BUTTON_H, TEX, TEX);
        int width = this.font.width(label);
        float scale = width > BUTTON_W - 8 ? (BUTTON_W - 8.0f) / width : 1.0f;
        g.pose().pushMatrix();
        g.pose().translate(x + (BUTTON_W - width * scale) / 2.0f, y + 4 + (1.0f - scale) * 4.0f);
        g.pose().scale(scale, scale);
        g.text(this.font, label, 0, 0, active ? (hover ? 0xFFFFF4C8 : 0xFFF0DCA0) : 0xFF9A8A6A, true);
        g.pose().popMatrix();
    }

    /** Kleine Schrift, mittig – und noch etwas kleiner, wenn der Text sonst breiter als {@code maxWidth} wäre. */
    private void small(GuiGraphicsExtractor g, Component text, int centerX, int y, int color, int maxWidth) {
        float scale = Math.min(0.75f, (float) maxWidth / Math.max(1, this.font.width(text)));
        g.pose().pushMatrix();
        g.pose().translate(centerX - this.font.width(text) * scale / 2.0f, y);
        g.pose().scale(scale, scale);
        g.text(this.font, text, 0, 0, color, false);
        g.pose().popMatrix();
    }

    private int coinsWidth(int amount) {
        int width = 0;
        int rest = Math.max(0, amount);
        for (int i = Coin.values().length - 1; i >= 0; i--) {
            Coin coin = Coin.values()[i];
            int n = rest / coin.value();
            rest -= n * coin.value();
            if (n > 0) {
                width += 11 + this.font.width(String.valueOf(n)) + 3;
            }
        }
        return Math.max(0, width - 3);
    }

    /** Ein Münzbetrag mit kleinen Münzen (wie im Handelsfenster). */
    private void coins(GuiGraphicsExtractor g, int x, int y, int amount) {
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
                g.text(this.font, s, cx + 11, y, INK, false);
                cx += 11 + this.font.width(s) + 3;
            }
        }
    }
}
