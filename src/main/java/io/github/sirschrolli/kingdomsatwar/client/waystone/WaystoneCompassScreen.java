package io.github.sirschrolli.kingdomsatwar.client.waystone;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.gui.UiMotion;
import io.github.sirschrolli.kingdomsatwar.client.gui.UiTheme;
import io.github.sirschrolli.kingdomsatwar.client.town.Parchment;
import io.github.sirschrolli.kingdomsatwar.network.WaystoneCompassPayload;
import io.github.sirschrolli.kingdomsatwar.rules.Compass;
import io.github.sirschrolli.kingdomsatwar.waystone.WaystoneCompass;
import io.github.sirschrolli.kingdomsatwar.waystone.WaystoneCompassItem;
import io.github.sirschrolli.kingdomsatwar.waystone.Waypoints;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

/**
 * Das Fenster des Wegkompasses (seit 1.12): links und rechts die acht Plätze für Wegsteine – jeder mit einem Edelstein in
 * seiner Farbe, dem Namen, der Entfernung und der Himmelsrichtung (keine Koordinaten). In der Mitte das große Zifferblatt:
 * oben ist vorne (wohin man blickt), die Himmelsrichtungen stehen, wo sie liegen, die Edelsteine der Wegsteine sitzen in
 * der Lünette an ihrer Richtung, und die Nadel schwingt zum gewählten Ziel (ohne Ziel zum Spawn).
 *
 * <p>Ein Klick auf einen Platz (oder seinen Stein am Zifferblatt, oder die Tasten 1–8) wählt den Wegstein als Ziel, noch ein
 * Klick hebt das Ziel auf. Ein Rechtsklick fragt, ob der Kompass ihn vergessen soll – ein zweiter Rechtsklick bestätigt.
 */
public class WaystoneCompassScreen extends Screen implements UiTheme.ParchmentStyle {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/gui/waystone_compass.png");
    private static final int TEX_W = 512;
    private static final int TEX_H = 256;
    private static final int W = 304;
    private static final int H = 210;
    private static final int DIAL_U = 308;
    private static final int DIAL = 97;
    private static final int GEM_U = 408;
    private static final int GEM_V = 0;
    private static final int GEM = 9;
    private static final int HALO_U = 420;
    private static final int HALO_V = 0;
    private static final int HALO = 19;
    private static final int HUB_U = 408;
    private static final int HUB_V = 12;
    private static final int HUB = 7;

    private static final int CARD_W = 86;
    private static final int CARD_H = 30;
    private static final int CARD_GAP = 5;
    private static final int CARDS_Y = 37;
    private static final int LEFT_X = 14;
    private static final int RIGHT_X = W - 14 - CARD_W;
    private static final int DIAL_X = (W - DIAL) / 2;
    private static final int DIAL_Y = 36;
    private static final float DIAL_CX = DIAL_X + 48.5f;
    private static final float DIAL_CY = DIAL_Y + 48.5f;
    private static final float MARKER_R = 43.0f;
    private static final float LETTER_R = 28.5f;
    private static final int BUTTON_W = 84;
    private static final int BUTTON_H = 14;
    private static final int BUTTON_Y = 160;

    /** Die Farben der acht Plätze: Edelstein auf der Karte und am Zifferblatt. */
    public static final int[] COLORS = {0x4FD2E0, 0xF2C14E, 0xF07C9A, 0x7BD86B, 0xB48CF0, 0xF59A4A, 0x6FA8FF, 0xE8EEF2};
    private static final int FORGET_MS = 3000;

    private final InteractionHand hand;
    private long openedAt;
    private int left;
    private int top;
    /** Die Nadel: ihr Winkel (Bogenmaß, im Uhrzeigersinn ab „vorne“) und ihre Drehgeschwindigkeit – sie schwingt nach. */
    private float needle = Float.NaN;
    private float needleSpeed;
    private long lastFrame;
    /** Der Platz, der auf den zweiten Rechtsklick wartet (vergessen), und bis wann. */
    private int forgetIndex = -1;
    private long forgetUntil;

    public WaystoneCompassScreen(InteractionHand hand) {
        super(Component.translatable("gui.kingdomsatwar.waystone_compass.title"));
        this.hand = hand;
    }

    @Override
    protected void init() {
        if (this.openedAt == 0L) {
            this.openedAt = Util.getMillis();
        }
        this.left = (this.width - W) / 2;
        this.top = (this.height - H) / 2;
    }

    private ItemStack compass() {
        ItemStack stack = this.minecraft.player == null ? ItemStack.EMPTY : this.minecraft.player.getItemInHand(this.hand);
        return stack.getItem() instanceof WaystoneCompassItem ? stack : ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        if (this.compass().isEmpty()) {
            this.onClose();
        }
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
    // Lage
    // =====================================================================================

    private int cardX(int index) {
        return this.left + (index < 4 ? LEFT_X : RIGHT_X);
    }

    private int cardY(int index) {
        return this.top + CARDS_Y + (index % 4) * (CARD_H + CARD_GAP);
    }

    private int cardAt(double mx, double my) {
        for (int i = 0; i < Waypoints.MAX; i++) {
            int x = this.cardX(i);
            int y = this.cardY(i);
            if (mx >= x && mx < x + CARD_W && my >= y && my < y + CARD_H) {
                return i;
            }
        }
        return -1;
    }

    private boolean overButton(double mx, double my) {
        int x = this.left + (W - BUTTON_W) / 2;
        int y = this.top + BUTTON_Y;
        return mx >= x && mx < x + BUTTON_W && my >= y && my < y + BUTTON_H;
    }

    /** Der Stein am Zifferblatt unter der Maus (der gewählte liegt obenauf), -1: keiner. */
    private int markerAt(double mx, double my, Waypoints waypoints) {
        int found = -1;
        for (int i = 0; i < waypoints.entries().size(); i++) {
            Float bearing = this.bearing(waypoints.entries().get(i).pos());
            if (bearing == null) {
                continue;
            }
            double x = this.left + DIAL_CX + Mth.sin(bearing) * MARKER_R;
            double y = this.top + DIAL_CY - Mth.cos(bearing) * MARKER_R;
            if (Math.abs(mx - x) <= 5 && Math.abs(my - y) <= 5 && (found < 0 || i == waypoints.selected())) {
                found = i;
            }
        }
        return found;
    }

    /**
     * Die Richtung eines Orts auf dem Zifferblatt: Bogenmaß im Uhrzeigersinn ab „vorne“ (wohin der Spieler blickt) – oder
     * {@code null}, wenn er in einer anderen Welt liegt.
     */
    private @Nullable Float bearing(GlobalPos pos) {
        LocalPlayer player = this.minecraft.player;
        if (player == null || !pos.dimension().equals(player.level().dimension())) {
            return null;
        }
        double dx = pos.pos().getX() + 0.5 - player.getX();
        double dz = pos.pos().getZ() + 0.5 - player.getZ();
        return relative((float) Math.atan2(dz, dx));
    }

    /** Ein Winkel in der Welt (0 = Osten, im Uhrzeigersinn von oben gesehen) relativ zur Blickrichtung. */
    private float relative(float worldAngle) {
        LocalPlayer player = this.minecraft.player;
        float yaw = player == null ? 0.0f : player.getYRot() * Mth.DEG_TO_RAD;
        float facing = (float) Math.atan2(Mth.cos(yaw), -Mth.sin(yaw));
        return worldAngle - facing;
    }

    // =====================================================================================
    // Eingaben
    // =====================================================================================

    private void send(int action, int index) {
        ClientPacketDistributor.sendToServer(new WaystoneCompassPayload(this.hand == InteractionHand.MAIN_HAND, action, index));
    }

    private void click() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        ItemStack stack = this.compass();
        if (stack.isEmpty()) {
            return super.mouseClicked(event, doubleClick);
        }
        Waypoints waypoints = WaystoneCompass.of(stack);
        int index = this.cardAt(event.x(), event.y());
        if (index < 0) {
            index = this.markerAt(event.x(), event.y(), waypoints);
        }
        if (index >= 0 && index < waypoints.entries().size()) {
            if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
                long now = Util.getMillis();
                if (this.forgetIndex == index && now < this.forgetUntil) {
                    this.forgetIndex = -1;
                    this.send(WaystoneCompass.REMOVE, index);
                } else {
                    this.forgetIndex = index;
                    this.forgetUntil = now + FORGET_MS;
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.2f));
                }
                return true;
            }
            this.forgetIndex = -1;
            this.click();
            this.send(WaystoneCompass.SELECT, index);
            return true;
        }
        if (this.overButton(event.x(), event.y()) && waypoints.target() != null) {
            this.click();
            this.send(WaystoneCompass.CLEAR, 0);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.minecraft.options.keyInventory.matches(event)) {
            this.onClose();
            return true;
        }
        int key = event.key();
        if (key >= InputConstants.KEY_1 && key <= InputConstants.KEY_8) {
            int index = key - InputConstants.KEY_1;
            if (index < WaystoneCompass.of(this.compass()).entries().size()) {
                this.click();
                this.send(WaystoneCompass.SELECT, index);
            }
            return true;
        }
        return super.keyPressed(event);
    }

    // =====================================================================================
    // Zeichnen
    // =====================================================================================

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractBackground(g, mouseX, mouseY, a);
        UiMotion.begin(g, this.openedAt, this.width / 2.0f, this.height / 2.0f);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.left, this.top, 0.0f, 0.0f, W, H, TEX_W, TEX_H);
        UiMotion.end(g);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        UiMotion.begin(g, this.openedAt, this.width / 2.0f, this.height / 2.0f);
        try {
            this.extractWindow(g, mouseX, mouseY, a);
        } finally {
            UiMotion.end(g);
        }
    }

    private void extractWindow(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractRenderState(g, mouseX, mouseY, a);
        ItemStack stack = this.compass();
        if (stack.isEmpty()) {
            return;
        }
        Waypoints waypoints = WaystoneCompass.of(stack);
        List<Waypoints.Waypoint> entries = waypoints.entries();
        long now = Util.getMillis();
        if (this.forgetIndex >= entries.size() || now >= this.forgetUntil) {
            this.forgetIndex = -1;
        }
        int x0 = this.left;
        int y0 = this.top;

        // Band: der Kompass, der Titel, wie viele Plätze belegt sind
        g.item(stack, x0 + 11, y0 + 10);
        Component title = this.title.copy().withStyle(ChatFormatting.BOLD);
        g.text(this.font, title, x0 + (W - this.font.width(title)) / 2, y0 + 14, Parchment.GOLD_TEXT, true);
        Component count = Component.translatable("gui.kingdomsatwar.waystone_compass.count", entries.size(), Waypoints.MAX);
        g.text(this.font, count, x0 + W - 13 - this.font.width(count), y0 + 14, Parchment.RIBBON_TEXT, false);

        int hoveredCard = this.cardAt(mouseX, mouseY);
        int hoveredMarker = this.markerAt(mouseX, mouseY, waypoints);
        int hovered = hoveredCard >= 0 ? hoveredCard : hoveredMarker;

        // die acht Plätze
        for (int i = 0; i < Waypoints.MAX; i++) {
            this.drawCard(g, i, waypoints, i == hovered, now);
        }

        // Zifferblatt
        this.drawDial(g, waypoints, hovered, now);

        // Ziel unter dem Zifferblatt
        this.drawTarget(g, waypoints);
        boolean active = waypoints.target() != null;
        Parchment.woodButton(g, this.font, x0 + (W - BUTTON_W) / 2, y0 + BUTTON_Y, BUTTON_W, BUTTON_H,
                Component.translatable("gui.kingdomsatwar.waystone_compass.clear"), active, this.overButton(mouseX, mouseY), null);

        // Hinweise unten
        this.small(g, Component.translatable("gui.kingdomsatwar.waystone_compass.hint1"), x0 + W / 2, y0 + 181, Parchment.MUTED, W - 28);
        this.small(g, Component.translatable("gui.kingdomsatwar.waystone_compass.hint2"), x0 + W / 2, y0 + 190, Parchment.MUTED, W - 28);

        // Tooltips
        if (hoveredCard >= 0 && hoveredCard >= entries.size()) {
            g.setTooltipForNextFrame(this.font, List.of(Component.translatable("gui.kingdomsatwar.waystone_compass.free").withStyle(ChatFormatting.GRAY),
                    Component.translatable("gui.kingdomsatwar.waystone_compass.free_hint").withStyle(ChatFormatting.DARK_GRAY)), Optional.empty(),
                    mouseX, mouseY);
        } else if (hovered >= 0 && hovered < entries.size()) {
            g.setTooltipForNextFrame(this.font, this.tooltip(waypoints, hovered), Optional.empty(), mouseX, mouseY);
        } else if (this.overButton(mouseX, mouseY)) {
            g.setTooltipForNextFrame(this.font, Component.translatable("gui.kingdomsatwar.waystone_compass.clear_hint"), mouseX, mouseY);
        }
    }

    private List<Component> tooltip(Waypoints waypoints, int index) {
        Waypoints.Waypoint waypoint = waypoints.entries().get(index);
        List<Component> lines = new ArrayList<>();
        lines.add(waypoint.name().copy().withColor(COLORS[index]));
        lines.add(this.where(waypoint.pos(), false).copy().withStyle(ChatFormatting.GRAY));
        boolean selected = waypoints.selected() == index;
        lines.add(Component.translatable(selected ? "gui.kingdomsatwar.waystone_compass.click_clear" : "gui.kingdomsatwar.waystone_compass.click_select")
                .withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.translatable("gui.kingdomsatwar.waystone_compass.click_forget").withStyle(ChatFormatting.DARK_GRAY));
        return lines;
    }

    /** „≈ 350 Blöcke · Nordosten“, „Du bist da!“ oder „im Nether“ ({@code brief}: kurz für die Karten). */
    private Component where(GlobalPos pos, boolean brief) {
        LocalPlayer player = this.minecraft.player;
        if (player == null) {
            return Component.empty();
        }
        if (!pos.dimension().equals(player.level().dimension())) {
            return Component.translatable("gui.kingdomsatwar.waystone_compass.dimension." + dimensionKey(pos.dimension()));
        }
        double dx = pos.pos().getX() + 0.5 - player.getX();
        double dz = pos.pos().getZ() + 0.5 - player.getZ();
        if (dx * dx + dz * dz < 36.0) {
            return Component.translatable("gui.kingdomsatwar.waystone_compass.here");
        }
        int distance = Compass.roughDistance(dx, dz);
        if (brief) {
            return Component.translatable("gui.kingdomsatwar.waystone_compass.distance_short", distance,
                    Component.translatable("direction.kingdomsatwar.short." + Compass.direction(dx, dz)));
        }
        return Component.translatable("gui.kingdomsatwar.waystone_compass.distance", distance, Compass.describe(dx, dz));
    }

    private static String dimensionKey(ResourceKey<Level> dimension) {
        if (dimension.equals(Level.OVERWORLD)) {
            return "overworld";
        }
        if (dimension.equals(Level.NETHER)) {
            return "nether";
        }
        return dimension.equals(Level.END) ? "end" : "other";
    }

    private void drawCard(GuiGraphicsExtractor g, int index, Waypoints waypoints, boolean hover, long now) {
        int x = this.cardX(index);
        int y = this.cardY(index);
        if (index >= waypoints.entries().size()) {
            // freier Platz: gestrichelter Rand, blasser Stein
            int dash = UiMotion.mix(0x66B89C69, 0xCCAE8A52, UiMotion.hover(x, y, CARD_W, CARD_H, hover));
            for (int k = 0; k < CARD_W; k += 4) {
                g.fill(x + k, y, x + Math.min(CARD_W, k + 2), y + 1, dash);
                g.fill(x + k, y + CARD_H - 1, x + Math.min(CARD_W, k + 2), y + CARD_H, dash);
            }
            for (int k = 0; k < CARD_H; k += 4) {
                g.fill(x, y + k, x + 1, y + Math.min(CARD_H, k + 2), dash);
                g.fill(x + CARD_W - 1, y + k, x + CARD_W, y + Math.min(CARD_H, k + 2), dash);
            }
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 6, y + 11, GEM_U, GEM_V, GEM, GEM, TEX_W, TEX_H, 0x40A08868);
            this.smallLeft(g, Component.translatable("gui.kingdomsatwar.waystone_compass.free"), x + 19, y + 12, 0x997A6448, CARD_W - 24);
            return;
        }
        Waypoints.Waypoint waypoint = waypoints.entries().get(index);
        boolean selected = waypoints.selected() == index;
        Parchment.card(g, x, y, CARD_W, CARD_H, hover, selected);
        boolean forgetting = this.forgetIndex == index;
        if (forgetting) {
            g.fill(x + 1, y + 1, x + CARD_W - 1, y + CARD_H - 1, 0x38A83224);
        }
        int color = COLORS[index];
        if (selected) {
            float pulse = 0.5f + 0.5f * Mth.sin(now / 260.0f);
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 1, y + 6, HALO_U, HALO_V, HALO, HALO, TEX_W, TEX_H,
                    (Math.round(120 + 100 * pulse) << 24) | color);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 6, y + 11, GEM_U, GEM_V, GEM, GEM, TEX_W, TEX_H, 0xFF000000 | color);
        // Nummer klein neben dem Stein (für die Tasten 1–8)
        this.smallLeft(g, Component.literal(String.valueOf(index + 1)), x + 2, y + 2, 0x997A6448, 8);
        Parchment.fitted(g, this.font, waypoint.name(), x + 19, y + 6, CARD_W - 23, selected ? Parchment.GOLD : Parchment.TEXT);
        Component detail = forgetting ? Component.translatable("gui.kingdomsatwar.waystone_compass.forget_confirm") : this.where(waypoint.pos(), true);
        this.smallLeft(g, detail, x + 19, y + 18, forgetting ? Parchment.BAD : Parchment.MUTED, CARD_W - 23);
    }

    private void drawDial(GuiGraphicsExtractor g, Waypoints waypoints, int hovered, long now) {
        int dx0 = this.left + DIAL_X;
        int dy0 = this.top + DIAL_Y;
        float cx = this.left + DIAL_CX;
        float cy = this.top + DIAL_CY;
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, dx0, dy0, DIAL_U, 0.0f, DIAL, DIAL, TEX_W, TEX_H);

        // Himmelsrichtungen: stehen dort, wo sie von hier aus liegen
        String[] letters = {"north", "east", "south", "west"};
        for (int i = 0; i < 4; i++) {
            float angle = this.relative((i - 1) * Mth.HALF_PI);
            Component letter = Component.translatable("gui.kingdomsatwar.waystone_compass." + letters[i]);
            int lx = Math.round(cx + Mth.sin(angle) * LETTER_R - this.font.width(letter) / 2.0f);
            int ly = Math.round(cy - Mth.cos(angle) * LETTER_R - 3.5f);
            g.text(this.font, letter, lx, ly, i == 0 ? 0xFFE8695A : 0xFF8FA9AE, false);
        }

        // die Steine der Wegsteine in der Lünette (der gewählte und der unter der Maus obenauf)
        List<Waypoints.Waypoint> entries = waypoints.entries();
        float pulse = 0.5f + 0.5f * Mth.sin(now / 260.0f);
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < entries.size(); i++) {
                boolean front = i == waypoints.selected() || i == hovered;
                if (front != (pass == 1)) {
                    continue;
                }
                Float bearing = this.bearing(entries.get(i).pos());
                if (bearing == null) {
                    continue;
                }
                int mx = Math.round(cx + Mth.sin(bearing) * MARKER_R - 0.5f);
                int my = Math.round(cy - Mth.cos(bearing) * MARKER_R - 0.5f);
                if (front) {
                    int alpha = Math.round((i == waypoints.selected() ? 130 : 90) + 110 * pulse);
                    g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, mx - HALO / 2, my - HALO / 2, HALO_U, HALO_V, HALO, HALO, TEX_W, TEX_H,
                            (alpha << 24) | COLORS[i]);
                }
                g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, mx - GEM / 2, my - GEM / 2, GEM_U, GEM_V, GEM, GEM, TEX_W, TEX_H,
                        0xFF000000 | COLORS[i]);
            }
        }

        // die Nadel: schwingt zum Ziel und federt nach
        Waypoints.Waypoint target = waypoints.target();
        Float aim = target != null ? this.bearing(target.pos()) : this.spawnBearing();
        if (aim != null && this.atTarget(target)) {
            aim = null;
        }
        float dt = this.lastFrame == 0L ? 0.0f : Mth.clamp((now - this.lastFrame) / 1000.0f, 0.0f, 0.05f);
        this.lastFrame = now;
        float goal = aim != null ? aim : this.needle + 2.6f * dt + Mth.sin(now / 900.0f) * 0.02f;
        if (Float.isNaN(this.needle)) {
            this.needle = (aim != null ? aim : 0.0f) + 1.5f;
            this.needleSpeed = 0.0f;
        }
        float diff = Mth.wrapDegrees((goal - this.needle) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
        this.needleSpeed += (55.0f * diff - 7.5f * this.needleSpeed) * dt;
        this.needle += this.needleSpeed * dt;
        this.drawNeedle(g, cx, cy, this.needle, target != null);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, Math.round(cx - 0.5f) - HUB / 2, Math.round(cy - 0.5f) - HUB / 2, HUB_U, HUB_V, HUB, HUB,
                TEX_W, TEX_H);
    }

    /** Wo der Spawn liegt (wie beim Kompass von Minecraft) – {@code null} in einer anderen Welt. */
    private @Nullable Float spawnBearing() {
        if (this.minecraft.level == null) {
            return null;
        }
        return this.bearing(this.minecraft.level.getRespawnData().globalPos());
    }

    /** Steht der Spieler schon (fast) am Ziel? Dann kreist die Nadel ruhig. */
    private boolean atTarget(Waypoints.@Nullable Waypoint target) {
        LocalPlayer player = this.minecraft.player;
        if (target == null || player == null) {
            return false;
        }
        double dx = target.pos().pos().getX() + 0.5 - player.getX();
        double dz = target.pos().pos().getZ() + 0.5 - player.getZ();
        return dx * dx + dz * dz < 2.25;
    }

    /**
     * Die Nadel Pixel für Pixel (so bleibt sie in jeder Stellung scharf wie Pixelkunst): eine Raute, die Spitze türkis
     * (ohne Ziel rot) mit hellerer linker Facette, das Ende aus Bronze; dazu ein leichter Schatten und um die Spitze ein Schein.
     */
    private void drawNeedle(GuiGraphicsExtractor g, float cx, float cy, float angle, boolean target) {
        float sx = Mth.sin(angle);
        float sy = -Mth.cos(angle);
        float px = Mth.cos(angle);
        float py = Mth.sin(angle);
        float tipLength = 31.0f;
        float tailLength = 18.0f;
        float half = 2.8f;
        int[] tipLeft = target ? new int[] {0xFF4FD2E0, 0xFF9AF0F5, 0xFFE2FDFF} : new int[] {0xFFE05A4A, 0xFFFF8A7A, 0xFFFFC8BC};
        int[] tipRight = target ? new int[] {0xFF22A3B8, 0xFF4FD2E0, 0xFF9AF0F5} : new int[] {0xFFA8322A, 0xFFE05A4A, 0xFFFF8A7A};
        int glow = target ? 0x3A6EE3F0 : 0x00000000;
        int x0 = Mth.floor(cx - 34);
        int x1 = Mth.ceil(cx + 34);
        int y0 = Mth.floor(cy - 34);
        int y1 = Mth.ceil(cy + 34);
        for (int pass = 0; pass < 3; pass++) {
            // 0: Schein, 1: Schatten, 2: Nadel
            for (int y = y0; y <= y1; y++) {
                for (int x = x0; x <= x1; x++) {
                    float vx = x + 0.5f - cx - (pass == 1 ? 1.0f : 0.0f);
                    float vy = y + 0.5f - cy - (pass == 1 ? 1.0f : 0.0f);
                    float along = vx * sx + vy * sy;
                    float across = vx * px + vy * py;
                    if (pass == 0) {
                        if (glow == 0 || along < 4.0f || along > tipLength + 1.0f) {
                            continue;
                        }
                        float limit = half * (1.0f - along / tipLength) + 1.6f;
                        float inner = half * (1.0f - along / tipLength);
                        if (Math.abs(across) <= limit && Math.abs(across) > inner) {
                            g.fill(x, y, x + 1, y + 1, glow);
                        }
                        continue;
                    }
                    float limit = along >= 0.0f ? half * (1.0f - along / tipLength) : half * (1.0f + along / tailLength);
                    if (along > tipLength || along < -tailLength || limit <= 0.0f || Math.abs(across) > limit) {
                        continue;
                    }
                    if (pass == 1) {
                        g.fill(x, y, x + 1, y + 1, 0x55000000);
                        continue;
                    }
                    int color;
                    if (along >= 0.0f) {
                        float k = along / tipLength;
                        int step = k < 0.45f ? 0 : k < 0.8f ? 1 : 2;
                        color = across < 0.0f ? tipLeft[step] : tipRight[step];
                    } else {
                        float k = -along / tailLength;
                        color = across < 0.0f ? (k < 0.55f ? 0xFFECB862 : 0xFFCF9140) : (k < 0.55f ? 0xFFAD6F2B : 0xFF86521F);
                    }
                    g.fill(x, y, x + 1, y + 1, color);
                }
            }
        }
    }

    private void drawTarget(GuiGraphicsExtractor g, Waypoints waypoints) {
        int x0 = this.left;
        int y0 = this.top;
        Waypoints.Waypoint target = waypoints.target();
        int width = 150;
        Component line1;
        Component line2;
        int color;
        if (target != null) {
            line1 = target.name();
            line2 = this.where(target.pos(), false);
            color = Parchment.TEXT;
        } else {
            line1 = Component.translatable("gui.kingdomsatwar.waystone_compass.no_target");
            line2 = Component.translatable(this.spawnBearing() != null ? "gui.kingdomsatwar.waystone_compass.to_spawn"
                    : "gui.kingdomsatwar.waystone_compass.spinning");
            color = Parchment.MUTED;
        }
        int w = Math.min(width, this.font.width(line1));
        Parchment.fitted(g, this.font, line1, x0 + W / 2 - w / 2, y0 + 136, width, color);
        this.small(g, line2, x0 + W / 2, y0 + 148, Parchment.MUTED, width);
    }

    /** Kleine Schrift (75 %), mittig – und noch kleiner, wenn sie sonst breiter als {@code maxWidth} wäre. */
    private void small(GuiGraphicsExtractor g, Component text, int centerX, int y, int color, int maxWidth) {
        float scale = Math.min(0.75f, (float) maxWidth / Math.max(1, this.font.width(text)));
        g.pose().pushMatrix();
        g.pose().translate(centerX - this.font.width(text) * scale / 2.0f, y);
        g.pose().scale(scale, scale);
        g.text(this.font, text, 0, 0, color, false);
        g.pose().popMatrix();
    }

    /** Kleine Schrift (75 %), linksbündig, notfalls noch kleiner. */
    private void smallLeft(GuiGraphicsExtractor g, Component text, int x, int y, int color, int maxWidth) {
        float scale = Math.min(0.75f, (float) maxWidth / Math.max(1, this.font.width(text)));
        g.pose().pushMatrix();
        g.pose().translate(x, y + (0.75f - scale) * 4.0f);
        g.pose().scale(scale, scale);
        g.text(this.font, text, 0, 0, color, false);
        g.pose().popMatrix();
    }
}
