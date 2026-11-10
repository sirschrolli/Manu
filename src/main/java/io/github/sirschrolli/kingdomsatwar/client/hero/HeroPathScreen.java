package io.github.sirschrolli.kingdomsatwar.client.hero;

import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import com.mojang.blaze3d.platform.InputConstants;

/**
 * Der Heldenweg (Taste K): Die Heldenstufe als Pfad über eine nächtliche Landschaft.
 *
 * <ul>
 *   <li>Hinten Sternenhimmel und Mond, ferne Berge und nahe Hügel mit Tannen, die beim Fahren verschieden schnell
 *       vorbeiziehen; jeder Rang färbt seinen Abschnitt und hat sein Wahrzeichen am Horizont (Dorf, Wachturm, Burg,
 *       Schrein im Wald, Festung, Gipfel mit Kronenburg – seit 0.77 dahinter ein Tempel mit schwebendem Kristall, ein
 *       Wolkenpalast, ein Koloss, eine Sternwarte und die Himmelszitadelle mit ihrem Lichtring).</li>
 *   <li>Der Weg aus Steinen schlängelt sich von Stufe 1 bis 200; jede Stufe ein Stein, jede zehnte ein großer, jeder Rang
 *       sein Abzeichen mit Band und Belohnung. Gegangen ist er golden und Licht fließt darauf zum Helden; voraus liegt
 *       er im Nebel.</li>
 *   <li>Über der eigenen Stufe schwebt der Kopf des Helden in einem Goldrahmen, das Stück bis zur nächsten Stufe leuchtet
 *       so weit, wie er gekommen ist.</li>
 *   <li>Beim Öffnen fliegt die Ansicht von Stufe 1 bis zum Helden, die Steine leuchten nacheinander auf, dann sprühen
 *       Funken. Mausrad, Ziehen oder die Pfeiltasten fahren den Weg ab; unten zeigt eine Übersicht den ganzen Weg
 *       (anklicken springt dorthin).</li>
 * </ul>
 */
public class HeroPathScreen extends Screen {
    private static final int STEP = 34;
    private static final int MARGIN = 96;
    private static final int HEADER = 46;
    private static final int FOOTER = 44;
    private static final float INTRO = 1.7f;
    private static final Identifier MOUNTAINS = sprite("ridge");
    private static final Identifier HILLS = sprite("hills_near");
    private static final Identifier STONE = sprite("stone");
    private static final Identifier STONE_LIT = sprite("stone_lit");
    private static final Identifier BANNER = sprite("banner");
    private static final Identifier MOON = sprite("moon");
    private static final Identifier FOREGROUND = sprite("foreground");

    private float scroll;
    private float target;
    private long openedAt;
    private long lastFrame;
    private boolean intro = true;
    private int lastLevel = -1;

    public HeroPathScreen() {
        super(Component.translatable("gui.kingdomsatwar.path.title"));
    }

    private static Identifier sprite(String name) {
        return io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.id("hero/path/" + name);
    }

    @Override
    protected void init() {
        if (this.openedAt == 0L) {
            this.openedAt = Util.getMillis();
            this.lastFrame = this.openedAt;
            this.scroll = 0.0f;
        }
        this.addRenderableWidget(HeroButton.text(12, this.height - 26, 120, Component.translatable("gui.kingdomsatwar.path.talents"),
                button -> this.minecraft.gui.setScreen(new CharacterScreen())));
        this.addRenderableWidget(HeroButton.text(this.width - 12 - 80, this.height - 26, 80, Component.translatable("gui.kingdomsatwar.character.done"),
                button -> this.onClose()));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ------------------------------------------------------------------------------------------------ Lage des Weges

    private static float worldX(float level) {
        return MARGIN + (level - 1.0f) * STEP;
    }

    private static float worldWidth() {
        return worldX(HeroLevel.MAX_LEVEL) + MARGIN;
    }

    private float baseY() {
        return HEADER + (this.height - HEADER - FOOTER) * 0.6f;
    }

    /** Höhe des Weges bei Stufe {@code level} (auch zwischen den Stufen): zwei Wellen übereinander. */
    private float pathY(float level) {
        return this.baseY() + 26.0f * Mth.sin(level * 0.47f) + 14.0f * Mth.sin(level * 0.17f + 1.2f);
    }

    private float maxScroll() {
        return Math.max(0.0f, worldWidth() - this.width);
    }

    /** Wohin die Ansicht schaut, damit {@code level} etwas links der Mitte steht. */
    private float scrollFor(float level) {
        return Mth.clamp(worldX(level) - this.width * 0.45f, 0.0f, this.maxScroll());
    }

    /** Wie weit der Held auf dem Weg ist: seine Stufe plus der Teil bis zur nächsten. */
    private static float position(HeroLevel.Hero hero) {
        return hero.level() >= HeroLevel.MAX_LEVEL ? HeroLevel.MAX_LEVEL : hero.level() + HeroLevel.progress(hero.experience());
    }

    /** Springt (ohne Anflug) zu einer Stufe – für Vorführungen. */
    public void jumpTo(float level) {
        this.intro = false;
        this.target = this.scrollFor(level);
        this.scroll = this.target;
    }

    // ------------------------------------------------------------------------------------------------ Eingaben

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        this.intro = false;
        this.target = Mth.clamp(this.target - (float) (scrollY + scrollX) * STEP * 1.6f, 0.0f, this.maxScroll());
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (event.button() == 0 && event.y() > HEADER && event.y() < this.height - FOOTER) {
            this.intro = false;
            this.target = Mth.clamp(this.target - (float) dx, 0.0f, this.maxScroll());
            this.scroll = this.target;
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        // die Übersicht unten: dorthin springen
        int mx0 = 150;
        int mx1 = this.width - 110;
        int my = this.height - FOOTER + 6;
        if (event.button() == 0 && event.y() >= my - 5 && event.y() <= my + 7 && event.x() >= mx0 && event.x() <= mx1) {
            this.intro = false;
            float level = 1.0f + (float) ((event.x() - mx0) / (mx1 - mx0)) * (HeroLevel.MAX_LEVEL - 1);
            this.target = this.scrollFor(level);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (io.github.sirschrolli.kingdomsatwar.client.ModKeyMappings.CHARACTER.matches(event)) {
            this.onClose();
            return true;
        }
        int key = event.key();
        if (key == InputConstants.KEY_LEFT || key == InputConstants.KEY_RIGHT || key == InputConstants.KEY_HOME || key == InputConstants.KEY_END) {
            this.intro = false;
            this.target = key == InputConstants.KEY_HOME ? 0.0f : key == InputConstants.KEY_END ? this.maxScroll()
                    : Mth.clamp(this.target + (key == InputConstants.KEY_RIGHT ? 1 : -1) * STEP * 3.0f, 0.0f, this.maxScroll());
            return true;
        }
        return super.keyPressed(event);
    }

    // ------------------------------------------------------------------------------------------------ Zeichnen

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        // eigener Hintergrund über den ganzen Bildschirm (kein Abdunkeln der Welt)
        float horizon = this.baseY() - 30.0f;
        g.fillGradient(0, 0, this.width, Math.round(horizon), 0xFF0A0918, 0xFF2B2150);
        g.fillGradient(0, Math.round(horizon), this.width, this.height, 0xFF1A1530, 0xFF07060E);
        float time = HeroGui.seconds();
        // Sterne (ganz langsam mitziehend) und Mond
        for (int i = 0; i < 150; i++) {
            float hx = hash(i * 7 + 1);
            float hy = hash(i * 13 + 5);
            float x = ((hx * (this.width + 400) - this.scroll * 0.08f) % (this.width + 400) + this.width + 400) % (this.width + 400) - 200;
            float y = HEADER - 30 + hy * (horizon - HEADER + 10);
            float twinkle = 0.5f + 0.5f * Mth.sin(time * (1.5f + hash(i) * 2.5f) + i);
            int alpha = Math.round((80 + 150 * twinkle) * (0.4f + 0.6f * hash(i * 3 + 2)));
            int size = hash(i * 5 + 3) > 0.92f ? 2 : 1;
            g.fill(Math.round(x), Math.round(y), Math.round(x) + size, Math.round(y) + size, ARGB.color(alpha, 235, 230, 255));
        }
        // Polarlicht über dem Ende des Weges (der Legende)
        float endX = worldX(HeroLevel.MAX_LEVEL) - this.scroll;
        if (endX > -360 && endX < this.width + 360) {
            for (int k = 0; k < 5; k++) {
                float sway = Mth.sin(time * 0.6f + k * 1.3f) * 18.0f;
                float glow = 0.16f + 0.08f * Mth.sin(time * 1.1f + k);
                HeroGui.lightRect(g, HeroGui.GLOW, endX - 120 + k * 60 + sway, HEADER + 34 + k * 7, 220.0f, 70.0f,
                        k % 2 == 0 ? 0xFF6EE2F5 : 0xFFB070FF, glow);
            }
        }
        float moonX = this.width * 0.78f - this.scroll * 0.03f;
        HeroGui.light(g, HeroGui.GLOW, moonX + 20, HEADER + 34, 140.0f, 0.0f, 0xFF8C86C8, 0.35f);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, MOON, Math.round(moonX), HEADER + 14, 40, 40);
        // ferne Berge und nahe Hügel (verschieden schnell)
        tile(g, MOUNTAINS, 256, 80, this.scroll * 0.25f, Math.round(horizon - 66));
        this.regionTint(g, horizon);
        tile(g, HILLS, 256, 64, this.scroll * 0.55f, Math.round(horizon - 22));
    }

    /** Ein waagerecht kachelbares Bild über die ganze Breite, um {@code offset} verschoben. */
    private void tile(GuiGraphicsExtractor g, Identifier sprite, int w, int h, float offset, int y) {
        int start = -Math.floorMod(Math.round(offset), w);
        for (int x = start; x < this.width; x += w) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, w, h);
        }
    }

    /** Jeder Rang färbt seinen Abschnitt leicht ein, und sein Wahrzeichen steht am Horizont in seinem Licht. */
    private void regionTint(GuiGraphicsExtractor g, float horizon) {
        HeroLevel.Rank[] ranks = HeroLevel.Rank.values();
        for (int i = 0; i < ranks.length; i++) {
            HeroLevel.Rank rank = ranks[i];
            float from = worldX(rank.minLevel()) - STEP * 0.5f - this.scroll;
            float to = i + 1 < ranks.length ? worldX(ranks[i + 1].minLevel()) - STEP * 0.5f - this.scroll : worldWidth() - this.scroll;
            if (to < 0 || from > this.width) {
                continue;
            }
            int x0 = Math.max(0, Math.round(from));
            int x1 = Math.min(this.width, Math.round(to));
            g.fillGradient(x0, Math.round(horizon - 70), x1, Math.round(horizon + 40), ARGB.color(0, rank.color() & 0xFFFFFF),
                    ARGB.color(34, rank.color() & 0xFFFFFF));
            // das Wahrzeichen über dem Rangstein, im Licht seiner Farbe
            float lx = worldX(rank.minLevel()) - this.scroll;
            if (lx > -80 && lx < this.width + 80) {
                HeroGui.light(g, HeroGui.GLOW, lx, horizon - 24, 170.0f, 0.0f, rank.color(), 0.32f);
                g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("landmark_" + rank.getSerializedName()), Math.round(lx - 48), Math.round(horizon - 66),
                        96, 72);
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        Player player = this.minecraft.player;
        if (player != null) {
            this.drawScene(g, player, mouseX, mouseY);
        }
        super.extractRenderState(g, mouseX, mouseY, a);
    }

    private void drawScene(GuiGraphicsExtractor g, Player player, int mouseX, int mouseY) {
        HeroLevel.Hero hero = HeroLevel.of(player);
        long now = Util.getMillis();
        float dt = Mth.clamp((now - this.lastFrame) / 1000.0f, 0.0f, 0.1f);
        this.lastFrame = now;
        float age = (now - this.openedAt) / 1000.0f;
        float here = position(hero);
        if (this.lastLevel < 0) {
            this.lastLevel = hero.level();
        }
        // die Ansicht: beim Öffnen von Stufe 1 bis zum Helden fliegen, danach weich dem Ziel folgen
        float lit;
        if (this.intro) {
            float k = HeroGui.easeInOut(HeroGui.span(age, 0.15f, 0.15f + INTRO));
            this.scroll = Mth.lerp(k, 0.0f, this.scrollFor(here));
            this.target = this.scroll;
            lit = Mth.lerp(k, 1.0f, here);
            if (age > 0.15f + INTRO) {
                this.intro = false;
            }
        } else {
            this.scroll += (this.target - this.scroll) * Math.min(1.0f, dt * 9.0f);
            lit = here;
        }
        float time = HeroGui.seconds();

        // der Weg: Steine im Abstand von gut fünf Pixeln, gegangen golden mit fließendem Licht
        float dl = 5.5f / STEP;
        float first = Math.max(1.0f, 1.0f + (this.scroll - MARGIN - 20) / STEP);
        float last = Math.min(HeroLevel.MAX_LEVEL, 1.0f + (this.scroll + this.width - MARGIN + 20) / STEP);
        for (float l = first - (first % dl); l <= last; l += dl) {
            if (l < 1.0f) {
                continue;
            }
            float sx = worldX(l) - this.scroll;
            float sy = this.pathY(l);
            boolean on = l <= lit;
            g.blitSprite(RenderPipelines.GUI_TEXTURED, on ? STONE_LIT : STONE, Math.round(sx - 3), Math.round(sy - 2), 6, 5);
            if (on) {
                HeroGui.light(g, HeroGui.GLOW, sx, sy, 12.0f, 0.0f, 0xFFF2C14E, 0.12f);
                float wave = Mth.sin((l - time * 2.2f) * 1.3f);
                if (wave > 0.6f) {
                    HeroGui.light(g, HeroGui.GLOW, sx, sy, 16.0f, 0.0f, 0xFFF2C14E, (wave - 0.6f) * 1.4f);
                }
            }
        }
        // Glühwürmchen über dem Weg
        for (int i = 0; i < 70; i++) {
            float wx = hash(i * 11 + 4) * worldWidth() + time * (3.0f + hash(i) * 5.0f);
            wx = wx % worldWidth();
            float sx = wx - this.scroll;
            if (sx < -10 || sx > this.width + 10) {
                continue;
            }
            float level = 1.0f + (wx - MARGIN) / STEP;
            float sy = this.pathY(Mth.clamp(level, 1.0f, HeroLevel.MAX_LEVEL)) - 18.0f - hash(i * 7 + 9) * 70.0f + Mth.sin(time * 1.3f + i) * 6.0f;
            float twinkle = 0.5f + 0.5f * Mth.sin(time * (2.0f + hash(i * 5) * 3.0f) + i * 2.1f);
            HeroGui.light(g, HeroGui.GLOW, sx, sy, 7.0f + 3.0f * hash(i * 13), 0.0f, i % 3 == 0 ? 0xFFB8FF8A : 0xFFFFD27A, 0.25f + 0.45f * twinkle);
        }
        // Nebel voraus: vom Helden an zunehmend dunkel
        float fogStart = worldX(lit) - this.scroll + 20;
        for (int i = 0; i < 24; i++) {
            int x0 = Math.round(fogStart + i * 6);
            if (x0 > this.width) {
                break;
            }
            int x1 = i == 23 ? this.width : Math.round(fogStart + (i + 1) * 6);
            g.fill(Math.max(0, x0), HEADER, Math.min(this.width, x1), this.height - FOOTER, ARGB.color(Math.round(7.0f * (i + 1)), 6, 5, 14));
        }

        // die Stufen
        List<Component> tooltip = null;
        for (int level = Math.max(1, (int) first - 1); level <= Math.min(HeroLevel.MAX_LEVEL, (int) last + 1); level++) {
            float sx = worldX(level) - this.scroll;
            float sy = this.pathY(level);
            boolean done = level <= lit;
            boolean current = level == hero.level() && !this.intro;
            HeroLevel.Rank rank = rankAt(level);
            if (rank != null) {
                if (done) {
                    HeroGui.lightRect(g, HeroGui.GLOW, sx, sy - 70, 30.0f, 200.0f, HeroGui.rankColor(rank), 0.3f + 0.1f * Mth.sin(time * 2.0f + level));
                }
                this.drawRankNode(g, rank, sx, sy, done, current, time);
                if (Math.abs(mouseX - sx) <= 18 && Math.abs(mouseY - sy) <= 18) {
                    tooltip = this.rankTooltip(rank, hero);
                }
            } else {
                boolean big = level % 10 == 0;
                String state = current ? "current" : done ? "done" : "locked";
                int size = big ? 19 : 13;
                if (current) {
                    HeroGui.light(g, HeroGui.GLOW, sx, sy, 44.0f + 6.0f * Mth.sin(time * 4.0f), 0.0f, 0xFFFFE58A, 0.75f);
                }
                g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("node_" + (big ? "big_" : "small_") + state), Math.round(sx - size / 2.0f),
                        Math.round(sy - size / 2.0f), size, size);
                if (big || level % 5 == 0) {
                    String number = String.valueOf(level);
                    g.pose().pushMatrix();
                    g.pose().translate(sx, big ? sy - 3 : sy + 8);
                    g.pose().scale(big ? 0.75f : 0.5f, big ? 0.75f : 0.5f);
                    g.text(this.font, number, -this.font.width(number) / 2, 0, big ? (done ? 0xFF3A2208 : 0xFFB8B0C8) : (done ? HeroGui.GOLD : HeroGui.MUTED),
                            false);
                    g.pose().popMatrix();
                }
                if (Math.abs(mouseX - sx) <= size / 2.0f + 1 && Math.abs(mouseY - sy) <= size / 2.0f + 1) {
                    tooltip = this.levelTooltip(level, hero);
                }
            }
        }

        // der Held auf seiner Stufe
        if (!this.intro) {
            float sx = worldX(Math.min(hero.level(), HeroLevel.MAX_LEVEL)) - this.scroll;
            float sy = this.pathY(hero.level()) - (rankAt(hero.level()) != null ? 30 : 22) + Mth.sin(time * 3.0f) * 2.0f;
            float burst = 1.0f - HeroGui.span(age, 0.15f + INTRO, 0.15f + INTRO + 0.9f);
            if (burst > 0.0f && burst < 1.0f) {
                for (int i = 0; i < 18; i++) {
                    float ang = i * Mth.TWO_PI / 18.0f + hash(i) * 0.4f;
                    float r = (1.0f - burst) * (40.0f + 30.0f * hash(i * 3));
                    HeroGui.light(g, HeroGui.SPARKLE, sx + Mth.cos(ang) * r, sy + 10 + Mth.sin(ang) * r, 8.0f, (1.0f - burst) * 4.0f,
                            i % 3 == 0 ? 0xFFFFFFFF : 0xFFF2C14E, burst);
                }
                HeroGui.light(g, HeroGui.RING, sx, sy + 10, 20.0f + 90.0f * (1.0f - burst), 0.0f, 0xFFF2C14E, burst * 0.8f);
            }
            HeroGui.lightRect(g, HeroGui.GLOW, sx, sy - 110, 22.0f, 260.0f, 0xFFFFE58A, 0.35f + 0.1f * Mth.sin(time * 3.0f));
            this.drawMarker(g, player, sx, sy);
        }

        // Vordergrund: Gras, Büsche und Steine ganz vorn, schneller als der Weg
        tile(g, FOREGROUND, 256, 56, this.scroll * 1.3f, this.height - FOOTER - 40);
        this.drawHeader(g, hero);
        this.drawOverview(g, hero, here);
        if (tooltip != null) {
            g.setTooltipForNextFrame(this.font, this.lines(tooltip), mouseX, mouseY);
        }
    }

    private static HeroLevel.Rank rankAt(int level) {
        for (HeroLevel.Rank rank : HeroLevel.Rank.values()) {
            if (rank.minLevel() == level) {
                return rank;
            }
        }
        return null;
    }

    /**
     * Ein Rangstein: das Abzeichen (erreicht mit Strahlen, sonst grau), darunter das Band, darüber die Belohnung – steht der
     * Held selbst auf dem Stein (sein Kopf schwebt darüber), unter dem Band.
     */
    private void drawRankNode(GuiGraphicsExtractor g, HeroLevel.Rank rank, float x, float y, boolean done, boolean here, float time) {
        int tint = done ? 0xFFFFFFFF : 0xFF77708A;
        HeroGui.emblem(g, this.font, rank, rank.minLevel(), x, y, 0.8f, 1.0f, 1.0f, done, tint);
        Component name = Component.translatable(rank.translationKey());
        int w = this.font.width(name) + 22;
        int by = Math.round(y + 20);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, BANNER, Math.round(x - w / 2.0f), by, w, 14, done ? 0xFFFFFFFF : 0xFF8A8499);
        g.text(this.font, name, Math.round(x - this.font.width(name) / 2.0f) + 1, by + 4, 0xFF0B0910, false);
        g.text(this.font, name, Math.round(x - this.font.width(name) / 2.0f), by + 3, done ? HeroGui.rankColor(rank) : 0xFFB8B0C8, false);
        // die Belohnung über dem Abzeichen
        Component reward = rank == HeroLevel.Rank.LEGEND ? Component.translatable("gui.kingdomsatwar.path.reward.pat")
                : rank == HeroLevel.Rank.IMMORTAL ? Component.translatable("gui.kingdomsatwar.path.reward.immortal")
                : rank.ordinal() > 0 ? Component.translatable("gui.kingdomsatwar.path.reward.points", HeroLevel.RANK_BONUS) : null;
        if (reward != null) {
            float bob = Mth.sin(time * 2.0f + rank.ordinal()) * 1.5f + (here ? 76.0f : 0.0f);
            int color = rank == HeroLevel.Rank.LEGEND ? 0xFFE0B8FF : rank == HeroLevel.Rank.IMMORTAL ? 0xFFFFE9A6 : done ? HeroGui.GOOD : 0xFFB8B0C8;
            if (rank == HeroLevel.Rank.LEGEND) {
                HeroGui.light(g, HeroGui.GLOW, x, y - 32 + bob, 70.0f, 0.0f, 0xFFB070FF, 0.4f + 0.2f * Mth.sin(time * 3.0f));
            }
            if (rank == HeroLevel.Rank.IMMORTAL) {
                HeroGui.light(g, HeroGui.GLOW, x, y - 32 + bob, 90.0f, 0.0f, 0xFFFFE27A, 0.45f + 0.2f * Mth.sin(time * 3.0f));
                HeroGui.light(g, HeroGui.RAYS, x, y - 32 + bob, 80.0f, time * 0.3f, 0xFFFFF4E0, 0.25f);
            }
            HeroGui.centered(g, this.font, reward, x, y - 36 + bob, color, 1.0f);
        }
    }

    /** Der Kopf des Helden in einem Goldrahmen mit einer kleinen Spitze nach unten. */
    private void drawMarker(GuiGraphicsExtractor g, Player player, float x, float y) {
        int fx = Math.round(x - 9);
        int fy = Math.round(y - 9);
        HeroGui.light(g, HeroGui.GLOW, x, y, 46.0f, 0.0f, 0xFFFFE58A, 0.5f);
        g.fill(fx - 1, fy - 1, fx + 19, fy + 19, 0xFF0B0910);
        g.fill(fx, fy, fx + 18, fy + 18, 0xFFE2B24A);
        g.fill(fx, fy, fx + 18, fy + 1, 0xFFFFE7A0);
        g.fill(fx, fy, fx + 1, fy + 18, 0xFFFFE7A0);
        if (player instanceof AbstractClientPlayer client) {
            PlayerFaceExtractor.extractRenderState(g, client.getSkin(), fx + 1, fy + 1, 16);
        }
        for (int i = 0; i < 4; i++) {
            g.fill(Math.round(x) - 3 + i, fy + 19 + i, Math.round(x) + 3 - i, fy + 20 + i, i == 0 ? 0xFFE2B24A : 0xFFB5832C);
        }
    }

    /** Oben: Titel, Stufe und Rang, die Leiste bis zur nächsten Stufe und wie weit es noch bis zur Legende ist. */
    private void drawHeader(GuiGraphicsExtractor g, HeroLevel.Hero hero) {
        g.fillGradient(0, 0, this.width, HEADER + 10, 0xF00B0910, 0x000B0910);
        g.fill(0, HEADER - 1, this.width, HEADER, 0x60B5832C);
        g.pose().pushMatrix();
        g.pose().translate(14, 10);
        g.pose().scale(1.6f, 1.6f);
        Component title = Component.translatable("gui.kingdomsatwar.path.title");
        g.text(this.font, title, 1, 1, 0xFF0B0910, false);
        g.text(this.font, title, 0, 0, 0xFFFFE7A0, false);
        g.pose().popMatrix();
        float total = HeroLevel.total(HeroLevel.MAX_LEVEL);
        int percent = Math.min(100, Math.round(hero.experience() * 100.0f / total));
        HeroGui.text(g, this.font, Component.translatable("gui.kingdomsatwar.path.total", percent, HeroGui.number(hero.experience()),
                HeroGui.number((long) total)), 14, 30, HeroGui.MUTED);
        // rechts: Abzeichen, Stufe, Rang, Leiste
        HeroLevel.Rank rank = hero.rank();
        int rx = this.width - 14 - 170;
        HeroGui.emblem(g, this.font, rank, hero.level(), rx - 20, 22, 0.62f, 1.0f, 1.0f, true);
        Component level = Component.translatable("gui.kingdomsatwar.character.level", hero.level()).append(" · ")
                .append(Component.translatable(rank.translationKey()).withColor(HeroGui.rankColor(rank) & 0xFFFFFF));
        HeroGui.text(g, this.font, level, rx, 8, 0xFFFFE7A0);
        HeroGui.bar(g, rx, 20, 170, 8, HeroLevel.progress(hero.experience()), 1.0f, 0.0f, HeroGui.rankColor(rank));
        boolean max = hero.level() >= HeroLevel.MAX_LEVEL;
        Component numbers = max ? Component.translatable("gui.kingdomsatwar.character.experience.max", HeroGui.number(hero.experience()))
                : Component.translatable("gui.kingdomsatwar.character.next", HeroGui.number(HeroLevel.total(hero.level() + 1) - hero.experience()),
                        hero.level() + 1);
        g.pose().pushMatrix();
        g.pose().translate(rx + 170, 31);
        g.pose().scale(0.75f, 0.75f);
        g.text(this.font, numbers, -this.font.width(numbers), 0, HeroGui.MUTED, false);
        g.pose().popMatrix();
    }

    /** Unten: der ganze Weg im Kleinen – Ränge, der Held, und welches Stück gerade zu sehen ist. */
    private void drawOverview(GuiGraphicsExtractor g, HeroLevel.Hero hero, float here) {
        g.fillGradient(0, this.height - FOOTER - 8, this.width, this.height, 0x000B0910, 0xF00B0910);
        int x0 = 150;
        int x1 = this.width - 110;
        int y = this.height - FOOTER + 6;
        if (x1 - x0 < 60) {
            return;
        }
        float span = x1 - x0;
        g.fill(x0, y, x1, y + 2, 0xFF2E2840);
        int heroX = Math.round(x0 + span * (here - 1.0f) / (HeroLevel.MAX_LEVEL - 1));
        g.fillGradient(x0, y, heroX, y + 2, 0xFFFFE7A0, 0xFFB5832C);
        // sichtbares Stück
        float from = 1.0f + (this.scroll - MARGIN) / STEP;
        float to = 1.0f + (this.scroll + this.width - MARGIN) / STEP;
        int vx0 = Math.round(x0 + span * Mth.clamp((from - 1.0f) / (HeroLevel.MAX_LEVEL - 1), 0.0f, 1.0f));
        int vx1 = Math.round(x0 + span * Mth.clamp((to - 1.0f) / (HeroLevel.MAX_LEVEL - 1), 0.0f, 1.0f));
        g.fill(vx0, y - 4, vx1, y - 3, 0x80EDE3C8);
        g.fill(vx0, y + 5, vx1, y + 6, 0x80EDE3C8);
        for (HeroLevel.Rank rank : HeroLevel.Rank.values()) {
            int rx = Math.round(x0 + span * (rank.minLevel() - 1.0f) / (HeroLevel.MAX_LEVEL - 1));
            g.pose().pushMatrix();
            g.pose().translate(rx, y + 1);
            g.pose().scale(0.6f, 0.6f);
            g.blitSprite(RenderPipelines.GUI_TEXTURED, HeroGui.badge(rank), -8, -8, HeroGui.BADGE, HeroGui.BADGE,
                    hero.level() >= rank.minLevel() ? 0xFFFFFFFF : 0xFF6E6880);
            g.pose().popMatrix();
        }
        HeroGui.light(g, HeroGui.GLOW, heroX, y + 1, 16.0f, 0.0f, 0xFFFFE58A, 0.9f);
        g.fill(heroX - 1, y - 2, heroX + 2, y + 4, 0xFFFFFFFF);
        Component hint = Component.translatable("gui.kingdomsatwar.path.hint");
        g.pose().pushMatrix();
        g.pose().translate((x0 + x1) / 2.0f, y + 9);
        g.pose().scale(0.75f, 0.75f);
        g.text(this.font, hint, -this.font.width(hint) / 2, 0, HeroGui.MUTED, false);
        g.pose().popMatrix();
    }

    private List<Component> levelTooltip(int level, HeroLevel.Hero hero) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gui.kingdomsatwar.character.level", level).withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("gui.kingdomsatwar.path.needs", HeroGui.number(HeroLevel.total(level))).withStyle(ChatFormatting.GRAY));
        if (hero.level() >= level) {
            lines.add(Component.translatable("gui.kingdomsatwar.character.rank.reached").withStyle(ChatFormatting.GREEN));
        } else {
            lines.add(Component.translatable("gui.kingdomsatwar.path.missing", HeroGui.number(HeroLevel.total(level) - hero.experience()))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        lines.add(Component.translatable("gui.kingdomsatwar.path.point").withStyle(ChatFormatting.YELLOW));
        Component might = HeroGui.might(level - 1, level);
        if (level > 1 && !might.getString().isEmpty()) {
            lines.add(Component.translatable("hero.kingdomsatwar.level_up.might", might).withStyle(ChatFormatting.GOLD));
        }
        return lines;
    }

    private List<Component> rankTooltip(HeroLevel.Rank rank, HeroLevel.Hero hero) {
        List<Component> lines = new ArrayList<>();
        boolean got = hero.level() >= rank.minLevel();
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
        if (rank == HeroLevel.Rank.IMMORTAL) {
            lines.add(Component.translatable("gui.kingdomsatwar.character.rank.reward.immortal", HeroLevel.IMMORTAL_MINUTES)
                    .withStyle(ChatFormatting.GOLD));
        }
        lines.add(got ? Component.translatable("gui.kingdomsatwar.character.rank.reached").withStyle(ChatFormatting.GOLD)
                : Component.translatable("gui.kingdomsatwar.path.missing", HeroGui.number(HeroLevel.total(rank.minLevel()) - hero.experience()))
                        .withStyle(ChatFormatting.DARK_GRAY));
        return lines;
    }

    private List<FormattedCharSequence> lines(List<Component> components) {
        return components.stream().flatMap(c -> this.font.split(c, 240).stream()).toList();
    }

    /** Eine feste Zufallszahl (0 bis 1) je Nummer – Sterne und Funken sehen jedes Mal gleich aus. */
    private static float hash(int i) {
        int x = i * 0x27d4eb2d + 0x165667b1;
        x ^= x >>> 15;
        x *= 0x85ebca6b;
        x ^= x >>> 13;
        return (x & 0xFFFFFF) / (float) 0xFFFFFF;
    }
}
