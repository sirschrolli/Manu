package io.github.sirschrolli.kingdomsatwar.client.fishing;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.fishing.FishFight;
import io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies;
import io.github.sirschrolli.kingdomsatwar.fishing.Medium;
import io.github.sirschrolli.kingdomsatwar.network.FishingResultPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Der Drill ({@link FishFight}) auf dem Bildschirm: links die Karte des Fischs (sein Bild – oder nur der Schattenriss, wenn er
 * noch nicht im Fischbuch steht –, Seltenheit, Schwierigkeit), in der Mitte die Säule (Wasser, Lava oder Leere) mit dem
 * Fisch, der Fangzone und manchmal einer Truhe, rechts die Leiste des Fortschritts.
 *
 * <p>Gesteuert wird mit der Maus (seit 0.83 nur noch mit ihr): Halten hebt die Zone, jeder Klick gibt ihr einen kleinen
 * Schubs nach oben – beides mit jeder Maustaste. Esc gibt auf. Die Welt läuft weiter.
 */
public class FishingScreen extends Screen {
    private static final Identifier ATLAS = KingdomsAtWar.id("textures/gui/fishing/minigame.png");
    private static final int TEX_W = 512;
    private static final int TEX_H = 256;
    // Maße wie in tools/fishing_gui.py
    private static final int FRAME_W = 46;
    private static final int FRAME_H = 182;
    private static final int COL_X = 10;
    private static final int COL_Y = 22;
    private static final int COL_W = 26;
    private static final int COL_H = 150;
    private static final int CARD_W = 118;
    private static final int CARD_H = 74;
    private static final int TOTAL_W = CARD_W + 8 + FRAME_W + 4 + 14;
    private static final float INTRO = 0.55f;

    private static final int INK = 0xFF3A2414;
    private static final int MUTED = 0xFF7A6248;

    private final int session;
    private final FishSpecies species;
    private final Medium medium;
    private final boolean known;
    private final FishFight fight;
    private final RandomSource random = RandomSource.create();

    private long lastFrame = Util.getMillis();
    private float age;
    private float pending;
    /** Die Maustasten, die in diesem Fenster gedrückt wurden (je ein Bit) – eine schon vorher gehaltene zählt erst nach neuem Druck. */
    private int pressed;
    /** Nur für Vorführungen: gilt als gehalten. */
    private boolean devHeld;
    private boolean devAuto;
    private boolean frozen;
    private boolean sent;
    /** 0: läuft, 1: gefangen, -1: entkommen. */
    private int outcome;
    private float outcomeAge;
    private int cm;
    private int news = -1;
    private boolean gotChest;
    /** Der Fang ist gleich in den Fischbeutel gewandert. */
    private boolean bagged;
    private boolean wasInZone = true;
    private boolean hadChest;
    private float reelClock;
    private float shake;
    private float lastDart;

    public FishingScreen(int session, FishSpecies species, Medium medium, boolean known, boolean chest, float zoneBonus) {
        super(Component.translatable("gui.kingdomsatwar.fishing.title"));
        this.session = session;
        this.species = species;
        this.medium = medium;
        this.known = known;
        this.fight = new FishFight(species.behavior(), species.difficulty(), zoneBonus, chest, this.random);
    }

    /** Die Farbe einer Seltenheit auf Pergament (dunkler als im Tooltip, damit sie lesbar bleibt). */
    static int inkColor(FishSpecies.Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 0xFF5A4E3C;
            case UNCOMMON -> 0xFF2E7A2E;
            case RARE -> 0xFF2456A8;
            case EPIC -> 0xFF7A36A8;
            case LEGENDARY -> 0xFFB06A00;
        };
    }

    /** Schwierigkeit als Sterne (1–5). */
    static int stars(FishSpecies species) {
        return Mth.clamp(1 + (int) (species.difficulty() * 4.6f), 1, 5);
    }

    // =====================================================================================
    // Vom Server
    // =====================================================================================

    void caught(int session, int cm, int news, boolean chest, boolean bagged) {
        if (session != this.session) {
            return;
        }
        this.cm = cm;
        this.news = news;
        this.gotChest = chest;
        this.bagged = bagged;
        if (this.outcome != 1) {
            this.outcome = 1;
            this.outcomeAge = 0.0f;
        }
        if (news == 2 || this.species.rarity() == FishSpecies.Rarity.LEGENDARY) {
            this.play(this.species.rarity() == FishSpecies.Rarity.LEGENDARY ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE : SoundEvents.PLAYER_LEVELUP,
                    1.3f, 0.45f);
        }
    }

    void aborted(int session) {
        if (session == this.session && this.outcome == 0) {
            this.sent = true;
            this.finish(-1);
        }
    }

    // =====================================================================================
    // Ablauf
    // =====================================================================================

    private boolean held() {
        if (this.devAuto) {
            // Vorführung: drillt von selbst (hält, wenn die Zone sonst unter dem Fisch zum Stehen käme)
            FishFight f = this.fight;
            float center = f.zonePos() + f.zoneSize() / 2.0f;
            float stop = center + f.zoneVel() * Math.abs(f.zoneVel()) / (2.0f * (f.zoneVel() > 0.0f ? 2.1f : 2.5f));
            float aim = f.chestPos() >= 0.0f && f.progress() > 0.55f ? f.chestPos() : f.fishPos() + f.fishVel() * 0.1f;
            return stop < aim || (f.zonePos() <= 0.004f && f.inZone());
        }
        if (this.devHeld) {
            return true;
        }
        // nur, was hier gedrückt wurde und noch gedrückt ist (so bleibt nichts hängen, wenn das Loslassen verloren ging)
        var mouse = this.minecraft.mouseHandler;
        this.pressed &= (mouse.isLeftPressed() ? 1 : 0) | (mouse.isRightPressed() ? 2 : 0) | (mouse.isMiddlePressed() ? 4 : 0) | ~7;
        return this.pressed != 0;
    }

    private void play(SoundEvent sound, float pitch, float volume) {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    private void advance(float dt) {
        this.age += dt;
        this.shake = Math.max(0.0f, this.shake - dt * 3.0f);
        if (this.outcome != 0) {
            this.outcomeAge += dt;
            float stay = this.outcome < 0 ? 1.1f : (this.news >= 1 || this.gotChest) ? 3.0f : 1.9f;
            // ohne Antwort des Servers nicht ewig warten
            if (this.outcomeAge > stay && (this.outcome < 0 || this.news >= 0 || this.outcomeAge > 4.0f)) {
                this.minecraft.gui.setScreen(null);
            }
            return;
        }
        if (this.age < INTRO || this.frozen) {
            return;
        }
        this.pending += dt;
        boolean held = this.held();
        while (this.pending >= FishFight.STEP && this.fight.result() == 0) {
            this.pending -= FishFight.STEP;
            this.fight.step(held);
        }
        this.sounds(dt);
        if (this.fight.result() != 0) {
            this.finish(this.fight.result());
        }
    }

    private void sounds(float dt) {
        FishFight f = this.fight;
        // die Rolle klickt, solange der Fisch in der Zone ist – höher, je voller die Leiste
        if (f.reeling()) {
            this.reelClock -= dt;
            if (this.reelClock <= 0.0f) {
                this.reelClock = 0.11f;
                this.play(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.9f + f.progress() * 0.9f, 0.22f);
            }
        } else if (this.wasInZone && f.time() > 0.4f) {
            this.play(SoundEvents.NOTE_BLOCK_BASS.value(), 0.6f, 0.35f);
        }
        this.wasInZone = f.inZone();
        if (f.dart() > this.lastDart + 0.5f) {
            this.shake = 1.0f;
            switch (this.medium) {
                case WATER -> this.play(SoundEvents.FISH_SWIM, 0.9f + this.random.nextFloat() * 0.4f, 0.8f);
                case LAVA -> this.play(SoundEvents.LAVA_POP, 0.8f + this.random.nextFloat() * 0.4f, 0.9f);
                case VOID -> this.play(SoundEvents.AMETHYST_BLOCK_CHIME, 0.7f + this.random.nextFloat() * 0.5f, 0.8f);
            }
        }
        this.lastDart = f.dart();
        if (f.chestCollected() && !this.hadChest) {
            this.hadChest = true;
            this.play(SoundEvents.CHEST_OPEN, 1.3f, 0.5f);
            this.play(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.8f, 0.4f);
        }
    }

    private void finish(int result) {
        this.outcome = result;
        this.outcomeAge = 0.0f;
        if (!this.sent) {
            this.sent = true;
            ClientPacketDistributor.sendToServer(new FishingResultPayload(this.session, result > 0, this.fight.perfect(), this.fight.chestCollected()));
        }
        if (result > 0) {
            this.play(SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 1.1f, 0.35f);
            this.play(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.2f, 0.35f);
        } else {
            this.play(SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, 0.6f, 0.8f);
            this.play(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5f, 0.5f);
        }
    }

    @Override
    public void removed() {
        // geschlossen, ehe der Drill entschieden war: aufgegeben
        if (!this.sent) {
            this.sent = true;
            ClientPacketDistributor.sendToServer(new FishingResultPayload(this.session, false, false, false));
        }
        super.removed();
    }

    // =====================================================================================
    // Eingaben
    // =====================================================================================

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.outcome != 0) {
            if (this.outcomeAge > 0.45f && (this.outcome < 0 || this.news >= 0)) {
                this.minecraft.gui.setScreen(null);
            }
            return true;
        }
        this.pressed |= bit(event.button());
        if (this.age >= INTRO && !this.frozen) {
            // jeder Klick ein kleiner Schubs nach oben
            this.fight.kick();
            this.play(SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.5f + this.random.nextFloat() * 0.2f, 0.18f);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.pressed &= ~bit(event.button());
        return true;
    }

    private static int bit(int button) {
        return button >= 0 && button < 3 ? 1 << button : 8;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // =====================================================================================
    // Zeichnen
    // =====================================================================================

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        float open = Mth.clamp(this.age / 0.3f, 0.0f, 1.0f);
        g.fillGradient(0, 0, this.width, this.height, ARGB.color(Math.round(open * 46), 4, 8, 16), ARGB.color(Math.round(open * 96), 4, 8, 16));
    }

    private static float easeOut(float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        return 1.0f - (1.0f - t) * (1.0f - t) * (1.0f - t);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        long now = Util.getMillis();
        float dt = Math.min(0.1f, (now - this.lastFrame) / 1000.0f);
        this.lastFrame = now;
        this.advance(dt);

        float open = easeOut(this.age / 0.35f);
        int left = (this.width - TOTAL_W) / 2;
        int top = (this.height - FRAME_H) / 2;
        int slide = Math.round((1.0f - open) * 14.0f);
        int shakeX = this.shake > 0.0f ? Math.round(Mth.sin(this.age * 70.0f) * this.shake * 1.6f) : 0;
        int fx = left + CARD_W + 8 + shakeX;
        int fy = top + slide;

        this.drawColumn(g, fx, fy);
        this.drawMeter(g, fx + FRAME_W + 4, fy + 17);
        this.drawCard(g, left - slide, top + (FRAME_H - CARD_H) / 2 + 6);
        if (this.age < INTRO + 0.5f && this.outcome == 0) {
            this.drawBite(g, left + CARD_W / 2, top + (FRAME_H - CARD_H) / 2 - 16);
        }
        if (this.outcome != 0) {
            this.drawOutcome(g, top);
        }
    }

    private void drawColumn(GuiGraphicsExtractor g, int fx, int fy) {
        int theme = this.medium.ordinal();
        int cx = fx + COL_X;
        int cy = fy + COL_Y;
        FishFight f = this.fight;
        g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, cx, cy, 144.0f + theme * 28, 0.0f, COL_W, COL_H, TEX_W, TEX_H);
        g.enableScissor(cx, cy, cx + COL_W, cy + COL_H);
        // der ziehende Schleier: Blasen und Funken steigen, Sternenstaub sinkt langsam
        float speed = this.medium == Medium.WATER ? -14.0f : this.medium == Medium.LAVA ? -22.0f : 5.0f;
        int scroll = Math.floorMod(Math.round(this.age * speed), 64);
        for (int y = cy - 64 + scroll; y < cy + COL_H; y += 64) {
            g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, cx, y, 144.0f + theme * 28, 152.0f, COL_W, 64, TEX_W, TEX_H);
        }
        // zweite, langsamere Lage, versetzt – gibt Tiefe
        int scroll2 = Math.floorMod(Math.round(this.age * speed * 0.55f) + 23, 64);
        for (int y = cy - 64 + scroll2; y < cy + COL_H; y += 64) {
            g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, cx + 9, y, 144.0f + theme * 28, 152.0f, COL_W - 9, 64, TEX_W, TEX_H, 0x70FFFFFF);
        }

        int usable = COL_H - 6;
        // die Truhe
        if (f.chestPos() >= 0.0f) {
            int y = cy + 3 + Math.round((1.0f - f.chestPos()) * usable) - 6;
            int bob = Math.round(Mth.sin(this.age * 3.0f) * 1.2f);
            g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, cx + 6, y + bob, 310.0f, 116.0f, 14, 12, TEX_W, TEX_H);
            if (f.chestProgress() > 0.0f) {
                g.fill(cx + 6, y + bob + 13, cx + 20, y + bob + 15, 0xC0101018);
                g.fill(cx + 6, y + bob + 13, cx + 6 + Math.round(14 * f.chestProgress()), y + bob + 15, 0xFFFFD24A);
            }
        }
        // die Fangzone
        int zoneH = Math.max(12, Math.round(f.zoneSize() * usable));
        int zy = cy + 3 + Math.round((1.0f - f.zonePos() - f.zoneSize()) * usable);
        float zu = f.reeling() && this.outcome == 0 ? 282.0f : 256.0f;
        g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, cx + 1, zy, zu, 116.0f, 24, 5, TEX_W, TEX_H);
        for (int y = zy + 5; y < zy + zoneH - 5; y += 16) {
            g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, cx + 1, y, zu, 121.0f, 24, Math.min(16, zy + zoneH - 5 - y), TEX_W, TEX_H);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, cx + 1, zy + zoneH - 5, zu, 137.0f, 24, 5, TEX_W, TEX_H);

        // der Fisch: wippt mit seiner Bewegung, zuckt beim Losschießen
        if (this.outcome <= 0 || this.outcomeAge < 0.25f) {
            float fishY = cy + 3 + (1.0f - f.fishPos()) * usable;
            float wobble = Mth.sin(this.age * 9.0f) * 0.08f - Mth.clamp(f.fishVel(), -1.0f, 1.0f) * 0.5f;
            float escape = this.outcome < 0 ? this.outcomeAge * 160.0f : 0.0f;
            g.pose().pushMatrix();
            g.pose().translate(cx + COL_W / 2.0f, fishY - escape);
            g.pose().rotate(wobble);
            g.blit(RenderPipelines.GUI_TEXTURED, FishingClient.sprite(this.species), -8, -8, 0.0f, 0.0f, 16, 16, 16, 16,
                    this.known ? -1 : FishingClient.shadow(0xFF));
            g.pose().popMatrix();
            if (f.dart() > 0.3f) {
                int alpha = Math.round(f.dart() * 200.0f);
                for (int i = 0; i < 3; i++) {
                    int sx = cx + 4 + i * 8 + Math.round(Mth.sin(this.age * 31.0f + i * 2.1f) * 2.0f);
                    int sy = Math.round(fishY + (f.fishVel() > 0 ? 9 : -12) + Mth.cos(this.age * 23.0f + i) * 2.0f);
                    g.fill(sx, sy, sx + 1, sy + 3, ARGB.color(alpha, 255, 255, 255));
                }
            }
        }
        g.disableScissor();
        g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, fx, fy, theme * 48.0f, 0.0f, FRAME_W, FRAME_H, TEX_W, TEX_H);
        // gehobene Truhe: steigt aus der Säule und funkelt
        if (f.chestCollected() && this.outcome == 0) {
            int x = fx + COL_X + 6;
            int y = fy + 4;
            g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, x, y, 326.0f, 116.0f, 14, 12, TEX_W, TEX_H);
            float tw = 0.5f + 0.5f * Mth.sin(this.age * 9.0f);
            g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, x + 10, y - 5, 372.0f, 116.0f, 9, 9, TEX_W, TEX_H, ARGB.color(Math.round(120 + tw * 135), 255, 255, 255));
        }
    }

    private void drawMeter(GuiGraphicsExtractor g, int mx, int my) {
        g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, mx, my, 230.0f, 0.0f, 14, 160, TEX_W, TEX_H);
        float progress = this.outcome > 0 ? 1.0f : this.fight.progress();
        int h = Math.round(150.0f * progress);
        if (h > 0) {
            g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, mx + 4, my + 5 + 150 - h, 246.0f, 150.0f - h, 6, h, TEX_W, TEX_H);
            // heller Kopf der Füllung
            g.fill(mx + 4, my + 5 + 150 - h, mx + 10, my + 6 + 150 - h, 0xC0FFFFFF);
        }
        // fast leer: die Leiste blinkt rot
        if (this.outcome == 0 && progress < 0.16f && this.age > INTRO) {
            int alpha = Math.round((0.5f + 0.5f * Mth.sin(this.age * 14.0f)) * 110.0f);
            g.fill(mx + 4, my + 5, mx + 10, my + 155, ARGB.color(alpha, 255, 40, 30));
        }
    }

    private void drawCard(GuiGraphicsExtractor g, int x, int y) {
        g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, x, y, 256.0f, 0.0f, CARD_W, CARD_H, TEX_W, TEX_H);
        g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, x + 8, y + 8, 376.0f, 0.0f, 36, 36, TEX_W, TEX_H);
        g.blit(RenderPipelines.GUI_TEXTURED, FishingClient.sprite(this.species), x + 10, y + 10, 0.0f, 0.0f, 32, 32, 16, 16, 16, 16,
                this.known || this.outcome > 0 ? -1 : FishingClient.shadow(0xFF));

        int tx = x + 49;
        int tw = CARD_W - 49 - 8;
        Component name = this.known || this.outcome > 0 ? FishingClient.name(this.species) : Component.literal("? ? ?");
        this.fitted(g, name.copy().withStyle(ChatFormatting.BOLD), tx, y + 9, tw, INK);
        this.fitted(g, Component.translatable(this.species.rarity().key()), tx, y + 21, tw, inkColor(this.species.rarity()));
        int stars = stars(this.species);
        for (int i = 0; i < 5; i++) {
            g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, tx + i * 8, y + 33, i < stars ? 342.0f : 350.0f, 116.0f, 7, 7, TEX_W, TEX_H);
        }
        Component hint = Component.translatable(this.fight.perfect() && this.fight.time() > 0.5f && this.outcome == 0
                ? "gui.kingdomsatwar.fishing.perfect_so_far" : "gui.kingdomsatwar.fishing.hint");
        // klein gesetzt, höchstens drei Zeilen
        float scale = 0.75f;
        int line = y + 48;
        int shown = 0;
        for (var part : this.font.split(hint, Math.round((CARD_W - 16) / scale))) {
            if (shown++ >= 3) {
                break;
            }
            g.pose().pushMatrix();
            g.pose().translate(x + CARD_W / 2.0f - this.font.width(part) * scale / 2.0f, line);
            g.pose().scale(scale, scale);
            g.text(this.font, part, 0, 0, MUTED, false);
            g.pose().popMatrix();
            line += 7;
        }
    }

    /** Text, der bei Überlänge schmaler gesetzt wird (bis auf 60 %). */
    private void fitted(GuiGraphicsExtractor g, Component text, int x, int y, int maxWidth, int color) {
        int width = this.font.width(text);
        float scale = width > maxWidth ? Math.max(0.6f, (float) maxWidth / width) : 1.0f;
        g.pose().pushMatrix();
        g.pose().translate(x, y + (1.0f - scale) * 4.0f);
        g.pose().scale(scale, scale);
        g.text(this.font, text, 0, 0, color, false);
        g.pose().popMatrix();
    }

    private void drawBite(GuiGraphicsExtractor g, int cx, int y) {
        float t = this.age / (INTRO + 0.5f);
        float pop = t < 0.25f ? easeOut(t / 0.25f) : 1.0f;
        int alpha = Math.round(Mth.clamp((1.0f - t) * 3.0f, 0.0f, 1.0f) * 255.0f);
        if (alpha < 8) {
            return;
        }
        int bounce = Math.round((1.0f - pop) * 10.0f + Mth.sin(this.age * 16.0f) * 1.2f);
        Component text = Component.translatable("gui.kingdomsatwar.fishing.bite").withStyle(ChatFormatting.BOLD);
        int w = this.font.width(text) + 14;
        g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, cx - w / 2, y - 4 - bounce, 360.0f, 116.0f, 10, 16, TEX_W, TEX_H, ARGB.color(alpha, 255, 255, 255));
        g.text(this.font, text, cx - w / 2 + 14, y - bounce, ARGB.color(alpha, 255, 226, 122), true);
    }

    private void drawOutcome(GuiGraphicsExtractor g, int top) {
        float in = easeOut(this.outcomeAge / 0.3f);
        int cx = this.width / 2;
        int y = top - 22 + Math.round((1.0f - in) * -10.0f);
        int alpha = Math.round(in * 255.0f);
        g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, cx - 88, y, 256.0f, 78.0f, 176, 34, TEX_W, TEX_H, ARGB.color(alpha, 255, 255, 255));
        if (alpha < 30) {
            return;
        }
        if (this.outcome < 0) {
            Component text = Component.translatable("gui.kingdomsatwar.fishing.escaped").withStyle(ChatFormatting.BOLD);
            g.text(this.font, text, cx - this.font.width(text) / 2, y + 13, ARGB.color(alpha, 255, 196, 180), true);
            return;
        }
        Component head = Component.translatable(this.news == 2 ? "gui.kingdomsatwar.fishing.new_species"
                : this.news == 1 ? "gui.kingdomsatwar.fishing.new_record" : this.fight.perfect() ? "gui.kingdomsatwar.fishing.perfect"
                : "gui.kingdomsatwar.fishing.caught").withStyle(ChatFormatting.BOLD);
        g.text(this.font, head, cx - this.font.width(head) / 2, y + 8, ARGB.color(alpha, 255, 226, 122), true);
        Component detail = this.news < 0 ? FishingClient.name(this.species)
                : Component.translatable("gui.kingdomsatwar.fishing.size", FishingClient.name(this.species), this.cm);
        if (this.gotChest) {
            detail = detail.copy().append(Component.translatable("gui.kingdomsatwar.fishing.with_chest"));
        }
        if (this.bagged) {
            detail = detail.copy().append(Component.translatable("gui.kingdomsatwar.fishing.into_bag"));
        }
        int width = this.font.width(detail);
        float scale = width > 150 ? 150.0f / width : 1.0f;
        g.pose().pushMatrix();
        g.pose().translate(cx - width * scale / 2.0f, y + 19);
        g.pose().scale(scale, scale);
        g.text(this.font, detail, 0, 0, ARGB.color(alpha, 255, 255, 255), true);
        g.pose().popMatrix();
        // Funkeln um das Band
        for (int i = 0; i < 6; i++) {
            float phase = this.outcomeAge * 3.0f + i * 1.7f;
            float tw = Mth.sin(phase) * 0.5f + 0.5f;
            int sx = cx - 96 + (i * 37) % 190;
            int sy = y - 6 + (i * 23) % 42;
            g.blit(RenderPipelines.GUI_TEXTURED, ATLAS, sx, sy, 372.0f, 116.0f, 9, 9, TEX_W, TEX_H, ARGB.color(Math.round(tw * alpha), 255, 255, 255));
        }
    }

    // =====================================================================================
    // Nur für Vorführungen
    // =====================================================================================

    /** Hält die Rechnung an und stellt einen Stand her (Lagen 0–1). */
    public void devPose(float zonePos, float fishPos, float progress, float chestPos, float chestProgress) {
        this.frozen = true;
        this.age = Math.max(this.age, INTRO + 1.2f);
        this.fight.pose(zonePos, fishPos, progress, chestPos, chestProgress);
    }

    /** Zeigt den Abschluss (als wäre gefangen worden). */
    public void devCaught(int cm, int news, boolean chest) {
        this.sent = true;
        this.outcome = 1;
        this.outcomeAge = 0.6f;
        this.cm = cm;
        this.news = news;
        this.gotChest = chest;
        this.frozen = true;
    }

    public void devHold(boolean held) {
        this.devHeld = held;
    }

    /** Der Drill läuft von selbst. */
    public void devAuto(boolean auto) {
        this.devAuto = auto;
    }

    /** Nur für Vorführungen: der Drill selbst. */
    public FishFight devFight() {
        return this.fight;
    }

    /** 0: läuft, 1: gefangen, -1: entkommen. */
    public int outcome() {
        return this.outcome;
    }

    /** Hat der Server den Fang schon bestätigt (Größe bekannt)? */
    public boolean confirmed() {
        return this.news >= 0;
    }

    public FishSpecies species() {
        return this.species;
    }
}
