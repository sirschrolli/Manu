package io.github.sirschrolli.kingdomsatwar.client.architect;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.architect.Architect;
import io.github.sirschrolli.kingdomsatwar.architect.PlanInfo;
import io.github.sirschrolli.kingdomsatwar.architect.PlanScan;
import io.github.sirschrolli.kingdomsatwar.economy.Coin;
import io.github.sirschrolli.kingdomsatwar.economy.town.Material;
import io.github.sirschrolli.kingdomsatwar.network.SavePlanPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

/**
 * „Bauplan zeichnen“: das Fenster des Vermessungsstabs. Links Ansicht und Grundriss des vermessenen Gebäudes auf
 * Blaupausen-Papier (mit Bodenlinie und Eingang), rechts Name, Vorderseite, Bodenlinie, was der Bau kosten wird und was
 * nicht übernommen wird. Die Vorschau vermisst das Spiel selbst; gezeichnet wird auf dem Server.
 */
public class SavePlanScreen extends Screen {
    private static final Identifier BACKGROUND = KingdomsAtWar.id("textures/gui/town.png");
    private static final int WIDTH = 320;
    private static final int HEIGHT = 210;
    private static final int TEXT = 0xFF3B2611;
    private static final int MUTED = 0xFF7A6448;
    private static final int GOOD = 0xFF2E6B2E;
    private static final int BAD = 0xFFA83224;
    private static final int GOLD_TEXT = 0xFFF2CE6B;
    private static final int RIBBON_TEXT = 0xFFE9DDBF;
    private static final int GROUND = 0xFF6EDB7C;
    private static final int PREVIEW = 96;
    private static final Direction[] SIDES = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private final BlockPos first;
    private final BlockPos second;
    private int left;
    private int top;
    private @Nullable EditBox nameBox;
    private int ground;
    private Direction front = Direction.NORTH;
    private boolean doorFound;
    private int maxGround;
    private @Nullable PlanScan scan;
    /** Vermisst gerade (große Bereiche Lage für Lage, damit das Bild nicht stockt). */
    private PlanScan.@Nullable Scanner scanner;
    private PlanScan.@Nullable Costs costs;
    private @Nullable BlockPos door;
    private int beds;
    private PlanScan.Picture frontPicture = PlanScan.Picture.EMPTY;
    private PlanScan.Picture topPicture = PlanScan.Picture.EMPTY;
    private int version;
    private final List<Hotspot> hotspots = new ArrayList<>();

    private record Hotspot(int x, int y, int w, int h, boolean active, Runnable action) {
        boolean contains(double mx, double my) {
            return mx >= this.x && mx < this.x + this.w && my >= this.y && my < this.y + this.h;
        }
    }

    public SavePlanScreen(BlockPos first, BlockPos second) {
        super(Component.translatable("gui.kingdomsatwar.architect.title"));
        this.first = first;
        this.second = second;
    }

    @Override
    protected void init() {
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;
        String name = this.nameBox != null ? this.nameBox.getValue() : Component.translatable("gui.kingdomsatwar.architect.default_name").getString();
        this.nameBox = new EditBox(this.font, this.left + 170, this.top + 47, 140, 15, Component.translatable("gui.kingdomsatwar.architect.name"));
        this.nameBox.setMaxLength(PlanInfo.MAX_NAME);
        this.nameBox.setValue(name);
        this.nameBox.setHint(Component.translatable("gui.kingdomsatwar.architect.name"));
        this.addRenderableWidget(this.nameBox);
        this.setInitialFocus(this.nameBox);
        if (this.scan == null && this.minecraft.level != null) {
            this.maxGround = Math.abs(this.first.getY() - this.second.getY());
            this.ground = PlanScan.autoGround(this.minecraft.level, this.first, this.second);
            Direction door = PlanScan.doorSide(this.minecraft.level, this.first, this.second, this.ground,
                    this.minecraft.player == null ? null : this.minecraft.player.position());
            this.doorFound = door != null;
            this.front = door != null ? door : this.minecraft.player == null ? Direction.NORTH
                    : PlanScan.sideFacing(this.first, this.second, this.minecraft.player.position());
            this.rescan();
        }
    }

    /** Für Vorführungen: den Namen vorgeben. */
    public void setName(String name) {
        if (this.nameBox != null) {
            this.nameBox.setValue(name);
            this.nameBox.setFocused(false);
        }
    }

    /** So viele Blöcke vermisst das Fenster je Tick. */
    private static final int CELLS_PER_TICK = 250_000;

    /** Vermisst den Bereich neu (nach einer Änderung von Vorderseite oder Bodenlinie). */
    private void rescan() {
        if (this.minecraft.level == null) {
            return;
        }
        PlanTextures.forget("save/front/" + this.version);
        PlanTextures.forget("save/top/" + this.version);
        this.version++;
        this.scan = null;
        this.costs = null;
        this.door = null;
        this.beds = 0;
        this.frontPicture = PlanScan.Picture.EMPTY;
        this.topPicture = PlanScan.Picture.EMPTY;
        try {
            this.scanner = new PlanScan.Scanner(this.minecraft.level, this.first, this.second, this.ground, this.front, null);
        } catch (IllegalArgumentException e) {
            this.scanner = null;
            return;
        }
        this.advanceScan();
    }

    /** Vermisst ein Stück weiter; ist alles vermessen, entstehen Bilder und Kosten. */
    private void advanceScan() {
        PlanScan.Scanner scanner = this.scanner;
        if (scanner == null || !scanner.step(CELLS_PER_TICK)) {
            return;
        }
        this.scanner = null;
        try {
            PlanScan scan = scanner.finish();
            this.costs = scan.costs();
            this.frontPicture = scan.front(PREVIEW);
            this.topPicture = scan.top(PREVIEW);
            this.door = scan.door();
            this.beds = scan.beds().size();
            this.scan = scan;
        } catch (IllegalArgumentException e) {
            this.scan = null;
            this.costs = null;
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.advanceScan();
    }

    @Override
    public void removed() {
        PlanTextures.forget("save/front/" + this.version);
        PlanTextures.forget("save/top/" + this.version);
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // =====================================================================================
    // Eingaben
    // =====================================================================================

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
    public boolean keyPressed(KeyEvent event) {
        if (event.isConfirmation() && this.canSave()) {
            this.save();
            return true;
        }
        return super.keyPressed(event);
    }

    private boolean hasPaper() {
        return this.minecraft.player != null && (this.minecraft.player.hasInfiniteMaterials()
                || this.minecraft.player.getInventory().countItem(Items.PAPER) > 0);
    }

    private boolean canSave() {
        return this.scan != null && this.hasPaper() && this.nameBox != null && !this.nameBox.getValue().isBlank();
    }

    private void save() {
        if (this.nameBox == null) {
            return;
        }
        ClientPacketDistributor.sendToServer(new SavePlanPayload(this.first, this.second, this.nameBox.getValue().strip(), this.ground, this.front));
        this.onClose();
    }

    private void turn(int delta) {
        int i = 0;
        for (int k = 0; k < SIDES.length; k++) {
            if (SIDES[k] == this.front) {
                i = k;
            }
        }
        this.front = SIDES[Math.floorMod(i + delta, SIDES.length)];
        this.doorFound = false;
        this.rescan();
    }

    private void lift(int delta) {
        int g = Math.max(0, Math.min(this.maxGround, this.ground + delta));
        if (g != this.ground) {
            this.ground = g;
            this.rescan();
        }
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
        int x0 = this.left;
        int y0 = this.top;
        // Kopf: Stab, Titel, Maße
        this.item(g, new ItemStack(ModItems.SURVEYORS_ROD.get()), x0 + 11, y0 + 10, 1.0f);
        g.text(this.font, this.title.copy().withStyle(ChatFormatting.BOLD), x0 + 31, y0 + 14, GOLD_TEXT, true);
        String size = this.scan == null ? "–" : this.scan.sizeX + " × " + (this.scan.sizeY - this.scan.ground) + " × " + this.scan.sizeZ;
        g.text(this.font, size, x0 + WIDTH - 12 - this.font.width(size), y0 + 14, RIBBON_TEXT, true);

        this.drawPictures(g, x0 + 10, y0 + 36, mouseX, mouseY);
        this.drawSettings(g, x0 + 170, y0 + 36, mouseX, mouseY);
        super.extractRenderState(g, mouseX, mouseY, a);
    }

    /** Links: Ansicht (mit Bodenlinie) und Grundriss (mit Eingang). */
    private void drawPictures(GuiGraphicsExtractor g, int x, int y, int mouseX, int mouseY) {
        int w = 150;
        int h = 76;
        int[] f = PlanTextures.drawPanel(g, "save/front/" + this.version, this.frontPicture, x, y, w, h, 8);
        g.text(this.font, Component.translatable("gui.kingdomsatwar.plan.front"), x + 3, y + 3, 0xFFBFD5F2, false);
        PlanScan scan = this.scan;
        if (scan != null && f[3] > 0) {
            // Bodenlinie: gestrichelt in Grün über die ganze Tafel
            int ly = f[1] + Math.round(f[3] * (scan.sizeY - scan.ground) / (float) scan.sizeY);
            for (int dx = x + 2; dx < x + w - 2; dx += 4) {
                g.fill(dx, ly, Math.min(dx + 2, x + w - 2), ly + 1, GROUND);
            }
            Component label = Component.translatable("gui.kingdomsatwar.architect.ground_short");
            g.text(this.font, label, x + w - 3 - this.font.width(label), Math.min(ly + 2, y + h - 10), GROUND, false);
        }
        int ty = y + h + 4;
        int[] t = PlanTextures.drawPanel(g, "save/top/" + this.version, this.topPicture, x, ty, w, h, 8);
        g.text(this.font, Component.translatable("gui.kingdomsatwar.plan.top"), x + 3, ty + 3, 0xFFBFD5F2, false);
        if (scan != null && t[2] > 0) {
            // Eingang: ein grüner Pfeil unter der Vorderseite
            BlockPos door = this.door;
            int doorX = door == null ? scan.sizeX / 2 : door.getX();
            int ax = t[0] + Math.round((doorX + 0.5f) * t[2] / scan.sizeX);
            int ay = Math.min(t[1] + t[3] + 1, ty + h - 6);
            for (int i = 0; i < 4; i++) {
                g.fill(ax - i, ay + i, ax + i + 1, ay + i + 1, GROUND);
            }
            g.fill(ax, ay + 4, ax + 1, ay + 6, GROUND);
        }
    }

    /** Rechts: Name, Vorderseite, Bodenlinie, Kosten, Knöpfe. */
    private void drawSettings(GuiGraphicsExtractor g, int x, int y, int mouseX, int mouseY) {
        int w = 140;
        g.text(this.font, Component.translatable("gui.kingdomsatwar.architect.name").withStyle(ChatFormatting.ITALIC), x, y, MUTED, false);
        // (Eingabefeld bei y + 11)
        int row = y + 31;
        g.text(this.font, Component.translatable("gui.kingdomsatwar.architect.front_label"), x, row + 3, TEXT, false);
        Component side = Component.translatable("gui.kingdomsatwar.architect.side." + this.front.getName());
        this.stepper(g, x + w - 70, row, 70, side, mouseX, mouseY, () -> this.turn(-1), () -> this.turn(1),
                List.of(Component.translatable(this.doorFound ? "gui.kingdomsatwar.architect.front_door_hint" : "gui.kingdomsatwar.architect.front_hint")));
        row += 16;
        g.text(this.font, Component.translatable("gui.kingdomsatwar.architect.ground_label"), x, row + 3, TEXT, false);
        Component layers = Component.translatable(this.ground == 1 ? "gui.kingdomsatwar.architect.layer" : "gui.kingdomsatwar.architect.layers", this.ground);
        this.stepper(g, x + w - 70, row, 70, layers, mouseX, mouseY, () -> this.lift(-1), () -> this.lift(1),
                List.of(Component.translatable("gui.kingdomsatwar.architect.ground_hint")));
        row += 18;
        g.fill(x, row, x + w, row + 1, 0x40765A34);
        row += 4;
        PlanScan scan = this.scan;
        PlanScan.Costs costs = this.costs;
        PlanScan.Scanner scanner = this.scanner;
        if (scanner != null) {
            // große Bereiche: Fortschritt des Vermessens
            g.text(this.font, Component.translatable("gui.kingdomsatwar.architect.scanning", Math.round(scanner.progress() * 100.0f)), x, row, TEXT, false);
            g.fill(x, row + 12, x + w, row + 17, 0xFF8C7650);
            g.fill(x + 1, row + 13, x + 1 + Math.round((w - 2) * scanner.progress()), row + 16, 0xFF3F7FBF);
        } else if (scan == null || costs == null) {
            for (FormattedCharSequence line : this.font.split(Component.translatable("gui.kingdomsatwar.architect.empty"), w)) {
                g.text(this.font, line, x, row, BAD, false);
                row += 10;
            }
        } else {
            // Blöcke und Betten
            g.text(this.font, Component.translatable("gui.kingdomsatwar.architect.blocks", costs.blocks()), x, row, TEXT, false);
            int beds = this.beds;
            if (beds > 0) {
                Component bedText = Component.literal("× " + beds);
                g.text(this.font, bedText, x + w - this.font.width(bedText), row, TEXT, false);
                this.item(g, new ItemStack(Items.BED.red()), x + w - this.font.width(bedText) - 12, row - 2, 0.625f);
            }
            row += 12;
            // Baustoffe
            int ix = x;
            for (Material m : Material.values()) {
                this.item(g, m.icon(), ix, row - 2, 0.625f);
                String n = String.valueOf(costs.units()[m.ordinal()]);
                g.text(this.font, n, ix + 12, row, TEXT, false);
                ix += 12 + this.font.width(n) + 8;
            }
            if (mouseX >= x && mouseX < x + w && mouseY >= row - 2 && mouseY < row + 9) {
                g.setComponentTooltipForNextFrame(this.font, List.of(Component.translatable("gui.kingdomsatwar.architect.materials_hint")
                        .withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            }
            row += 13;
            // kostbare Blöcke
            if (costs.precious().isEmpty()) {
                g.text(this.font, Component.translatable("gui.kingdomsatwar.architect.no_precious"), x, row, MUTED, false);
            } else {
                int px = x;
                int shown = 0;
                for (Map.Entry<Item, Integer> e : costs.precious().entrySet()) {
                    if (shown++ == 5) {
                        g.text(this.font, "…", px, row, TEXT, false);
                        break;
                    }
                    this.item(g, new ItemStack(e.getKey()), px, row - 3, 0.75f);
                    String n = "×" + e.getValue();
                    g.text(this.font, n, px + 13, row, 0xFF2A5C7A, false);
                    px += 14 + this.font.width(n) + 4;
                }
                if (mouseX >= x && mouseX < x + w && mouseY >= row - 3 && mouseY < row + 10) {
                    List<Component> tip = new ArrayList<>();
                    tip.add(Component.translatable("gui.kingdomsatwar.architect.precious_title").withStyle(ChatFormatting.AQUA));
                    for (Map.Entry<Item, Integer> e : costs.precious().entrySet()) {
                        tip.add(Component.translatable("message.kingdomsatwar.architect.count", e.getValue(), new ItemStack(e.getKey()).getHoverName())
                                .withStyle(ChatFormatting.GRAY));
                    }
                    tip.add(Component.translatable("gui.kingdomsatwar.architect.precious_hint").withStyle(ChatFormatting.DARK_GRAY));
                    g.setComponentTooltipForNextFrame(this.font, tip, mouseX, mouseY);
                }
            }
            row += 13;
            // nicht übernommen
            Map<Block, Integer> skipped = scan.skipped();
            if (!skipped.isEmpty()) {
                Component text = Component.translatable("gui.kingdomsatwar.architect.skipped", Architect.skippedList(skipped, 2));
                List<FormattedCharSequence> lines = this.font.split(text, w);
                for (int i = 0; i < lines.size() && i < 2; i++) {
                    g.text(this.font, lines.get(i), x, row, BAD, false);
                    row += 10;
                }
            }
            // Preis
            int price = 9 + costs.blocks() / 8;
            int py = this.top + 160;
            Component label = Component.translatable("gui.kingdomsatwar.architect.price");
            g.text(this.font, label, x, py + 2, MUTED, false);
            this.price(g, x + this.font.width(label) + 4, py, price);
        }
        // Papier und Knöpfe
        int by = this.top + 180;
        boolean paper = this.hasPaper();
        boolean creative = this.minecraft.player != null && this.minecraft.player.hasInfiniteMaterials();
        this.button(g, x, by, 54, 18, Component.translatable("gui.kingdomsatwar.architect.cancel"), true, mouseX, mouseY, this::onClose, null);
        List<Component> why = new ArrayList<>();
        if (this.scanner != null) {
            why.add(Component.translatable("gui.kingdomsatwar.architect.scanning", Math.round(this.scanner.progress() * 100.0f)));
        } else if (this.scan == null) {
            why.add(Component.translatable("gui.kingdomsatwar.architect.empty"));
        } else if (!paper) {
            why.add(Component.translatable("message.kingdomsatwar.architect.no_paper"));
        } else {
            why.add(Component.translatable(creative ? "gui.kingdomsatwar.architect.paper_free" : "gui.kingdomsatwar.architect.paper"));
        }
        this.button(g, x + 58, by, 82, 18, Component.translatable("gui.kingdomsatwar.architect.save"), this.canSave(), mouseX, mouseY,
                this::save, why, creative ? null : new ItemStack(Items.PAPER));
    }

    // ---------------------------------------------------------------- Bausteine

    private void item(GuiGraphicsExtractor g, ItemStack stack, int x, int y, float scale) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.item(stack, 0, 0);
        g.pose().popMatrix();
    }

    /** „◀ Wert ▶“ mit zwei kleinen Holzknöpfen. */
    private void stepper(GuiGraphicsExtractor g, int x, int y, int w, Component value, int mouseX, int mouseY, Runnable down, Runnable up,
            List<Component> tooltip) {
        this.button(g, x, y, 12, 13, Component.literal("◀"), true, mouseX, mouseY, down, null);
        this.button(g, x + w - 12, y, 12, 13, Component.literal("▶"), true, mouseX, mouseY, up, null);
        int tw = this.font.width(value);
        g.text(this.font, value, x + (w - tw) / 2, y + 3, TEXT, false);
        if (mouseX >= x + 12 && mouseX < x + w - 12 && mouseY >= y && mouseY < y + 13) {
            g.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
        }
    }

    private void button(GuiGraphicsExtractor g, int x, int y, int w, int h, Component label, boolean active, int mouseX, int mouseY, Runnable action,
            @Nullable List<Component> tooltip) {
        this.button(g, x, y, w, h, label, active, mouseX, mouseY, action, tooltip, null);
    }

    /** Hölzerner Knopf wie im Stadtfenster (mit kleinem Bild links, wenn {@code icon} gesetzt ist). */
    private void button(GuiGraphicsExtractor g, int x, int y, int w, int h, Component label, boolean active, int mouseX, int mouseY, Runnable action,
            @Nullable List<Component> tooltip, @Nullable ItemStack icon) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int border = active && hover ? 0xFFE0B04A : 0xFF2E1D10;
        int fill = !active ? 0xFF8C826E : hover ? 0xFF8E6038 : 0xFF74492A;
        g.fill(x, y, x + w, y + h, border);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, !active ? 0xFFA49A86 : 0xFFA4744A);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, !active ? 0xFF6E6656 : 0xFF4A2E18);
        int tw = this.font.width(label);
        int lx = x + (w - tw) / 2;
        if (icon != null) {
            this.item(g, icon, x + 4, y + (h - 12) / 2, 0.75f);
            lx = x + 16 + (w - 16 - tw) / 2;
        }
        g.text(this.font, label, lx, y + (h - 8) / 2, active ? 0xFFF3E6C4 : 0xFFD5CCB6, active);
        this.hotspots.add(new Hotspot(x, y, w, h, active, action));
        if (hover && tooltip != null && !tooltip.isEmpty()) {
            g.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
        }
    }

    /** Ein Betrag als kleine Münzen (wie im Stadtfenster). */
    private void price(GuiGraphicsExtractor g, int x, int y, int amount) {
        int cx = x;
        int rest = Math.max(0, amount);
        boolean any = false;
        for (int i = Coin.values().length - 1; i >= 0; i--) {
            Coin coin = Coin.values()[i];
            int n = rest / coin.value();
            rest -= n * coin.value();
            if (n > 0 || (!any && i == 0)) {
                this.item(g, new ItemStack(coin.item()), cx, y, 0.625f);
                String s = String.valueOf(n);
                g.text(this.font, s, cx + 11, y + 2, TEXT, false);
                cx += 11 + this.font.width(s) + 4;
                any = true;
            }
        }
    }
}
