package io.github.sirschrolli.kingdomsatwar.client.hero;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.ModKeyMappings;
import io.github.sirschrolli.kingdomsatwar.hero.HeroAbilities;
import io.github.sirschrolli.kingdomsatwar.hero.HeroAbility;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
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
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;

/**
 * Das Fähigkeitenrad (seit 1.11): eine dunkle Scheibe mit Goldringen und einem Achtstern aus warmem Licht, darauf die acht
 * Fähigkeiten in Fassungen (oben der Sturmlauf, im Uhrzeigersinn in der Reihenfolge ihrer Stufen – immer an derselben
 * Stelle). In der Mitte stehen Name, Beschreibung und Abklingzeit der Fähigkeit unter der Maus; noch gesperrte sind grau
 * mit Schloss und der Stufe, ab der sie kommen.
 *
 * <p>Bedienung wie beim Zauberkreis: die Fähigkeitstaste halten, Maus in Richtung der Fähigkeit, loslassen (sie liegt dann
 * auf der Taste). Ziffern 1–8 wählen direkt, Rechtsklick oder Esc bricht ab.
 */
public class AbilityWheelScreen extends Screen {
    private static final Identifier BASE = KingdomsAtWar.id("textures/gui/hero_wheel/base.png");
    private static final Identifier PARTS = KingdomsAtWar.id("textures/gui/spell_wheel/parts.png");
    private static final int TEX = 256;
    private static final int SIZE = 200;
    private static final int RADIUS = 74;
    private static final int DEAD_ZONE = 22;
    private static final int RUNES = 24;
    private static final HeroAbility[] ORDER = HeroAbility.values();
    private static final int TEXT = 0xFFF2EAD8;
    private static final int MUTED = 0xFFA898B0;
    private static final int GOLD = 0xFFF6DC96;

    private final long openedAt = Util.getMillis();
    private boolean holding = true;
    private int hovered = -1;
    private int lastHovered = -1;
    private final float[] hover = new float[ORDER.length];
    private long lastFrame = Util.getMillis();
    /** Nur für Vorführungen: diese Fähigkeit gilt als unter der Maus (-1 = echte Maus). */
    private int forcedHover = -1;

    public AbilityWheelScreen() {
        super(Component.translatable("gui.kingdomsatwar.ability_wheel"));
    }

    /** Nur für Vorführungen: eine Fähigkeit hervorheben, als läge die Maus darauf. */
    public void devHover(int index) {
        this.forcedHover = index;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    private int heroLevel() {
        return this.minecraft.player == null ? 1 : HeroLevel.of(this.minecraft.player).level();
    }

    private float openProgress() {
        float t = Mth.clamp((Util.getMillis() - this.openedAt) / 140.0f, 0.0f, 1.0f);
        return 1.0f - (1.0f - t) * (1.0f - t);
    }

    private static int pick(double dx, double dy) {
        if (dx * dx + dy * dy < DEAD_ZONE * DEAD_ZONE) {
            return -1;
        }
        double angle = Math.atan2(dx, -dy);
        if (angle < 0) {
            angle += Math.PI * 2;
        }
        return (int) Math.round(angle / (Math.PI * 2 / ORDER.length)) % ORDER.length;
    }

    // =====================================================================================
    // Zeichnen
    // =====================================================================================

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        float open = this.openProgress();
        g.fillGradient(0, 0, this.width, this.height, ARGB.color(Math.round(open * 70), 12, 8, 4), ARGB.color(Math.round(open * 120), 16, 10, 4));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        Player player = this.minecraft.player;
        if (player == null) {
            return;
        }
        long now = Util.getMillis();
        float dt = Math.min(0.1f, (now - this.lastFrame) / 1000.0f);
        this.lastFrame = now;
        int cx = this.width / 2;
        int cy = this.height / 2;
        if (this.forcedHover >= 0) {
            float angle = this.forcedHover * Mth.TWO_PI / ORDER.length;
            mouseX = cx + Math.round(Mth.sin(angle) * RADIUS);
            mouseY = cy - Math.round(Mth.cos(angle) * RADIUS);
        }
        this.hovered = pick(mouseX - cx, mouseY - cy);
        if (this.hovered != this.lastHovered) {
            if (this.hovered >= 0) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_HIT, 1.0f + this.hovered * 0.08f, 0.35f));
            }
            this.lastHovered = this.hovered;
        }
        float open = this.openProgress();
        float scale = 0.86f + 0.14f * open;
        int level = this.heroLevel();
        HeroAbilities.State state = HeroAbilities.state(player);

        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        g.pose().scale(scale, scale);
        g.blit(RenderPipelines.GUI_TEXTURED, BASE, -SIZE / 2, -SIZE / 2, 0.0f, 0.0f, SIZE, SIZE, TEX, TEX, ARGB.white(open));
        this.drawRunes(g, now, open);
        this.drawPointer(g, mouseX - cx, mouseY - cy, open, level);
        for (int i = 0; i < ORDER.length; i++) {
            this.drawSocket(g, player, i, level, state, open, dt, a);
        }
        this.drawCenter(g, player, level, state, open, a);
        g.pose().popMatrix();

        Component hint = Component.translatable(this.holding ? "gui.kingdomsatwar.ability_wheel.hint_hold" : "gui.kingdomsatwar.ability_wheel.hint_click");
        g.centeredText(this.font, hint, cx, cy + Math.round(SIZE / 2.0f * scale) + 6, ARGB.color(Math.round(open * 200), 0xE0D0B0));
    }

    private void drawRunes(GuiGraphicsExtractor g, long now, float open) {
        float spin = -(now % 120000L) / 120000.0f * Mth.TWO_PI;
        for (int i = 0; i < RUNES; i++) {
            float angle = spin + i * Mth.TWO_PI / RUNES;
            float x = Mth.sin(angle) * 91.5f;
            float y = -Mth.cos(angle) * 91.5f;
            float pulse = 0.35f + 0.5f * (0.5f + 0.5f * Mth.sin(now / 600.0f - i * 0.8f));
            g.pose().pushMatrix();
            g.pose().translate(x - 3.5f, y - 4.5f);
            g.blit(RenderPipelines.GUI_TEXTURED, PARTS, 0, 0, (i % 12) * 8, 56.0f, 7, 9, TEX, TEX, ARGB.color(Math.round(255 * pulse * open), 0xFFC860));
            g.pose().popMatrix();
        }
    }

    private void drawPointer(GuiGraphicsExtractor g, int dx, int dy, float open, int level) {
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < DEAD_ZONE) {
            return;
        }
        float x = (float) (dx / dist * 52.0);
        float y = (float) (dy / dist * 52.0);
        int color = this.hovered >= 0 && ORDER[this.hovered].unlocked(level) ? ORDER[this.hovered].color() : 0xC8B8A0;
        g.pose().pushMatrix();
        g.pose().translate(x - 3.5f, y - 3.5f);
        g.blit(RenderPipelines.GUI_TEXTURED, PARTS, 0, 0, 216.0f, 0.0f, 7, 7, TEX, TEX, ARGB.color(Math.round(255 * open), color));
        g.pose().popMatrix();
    }

    private void drawSocket(GuiGraphicsExtractor g, Player player, int i, int level, HeroAbilities.State state, float open, float dt, float partial) {
        HeroAbility ability = ORDER[i];
        boolean unlocked = ability.unlocked(level);
        boolean selected = unlocked && ability == state.selectedAbility();
        float target = i == this.hovered ? (unlocked ? 1.0f : 0.35f) : 0.0f;
        this.hover[i] += (target - this.hover[i]) * Math.min(1.0f, dt * 16.0f);
        float h = this.hover[i];
        float angle = i * Mth.TWO_PI / ORDER.length;
        float sx = Mth.sin(angle) * RADIUS;
        float sy = -Mth.cos(angle) * RADIUS;

        g.pose().pushMatrix();
        g.pose().translate(sx, sy);
        float s = 1.0f + 0.16f * h;
        g.pose().scale(s, s);
        if (unlocked) {
            float glow = Math.max(selected ? 0.45f : 0.0f, h);
            if (glow > 0.01f) {
                g.blit(RenderPipelines.GUI_TEXTURED, PARTS, -26, -26, 80.0f, 0.0f, 52, 52, TEX, TEX, ARGB.color(Math.round(255 * glow * open), ability.color()));
            }
        }
        g.blit(RenderPipelines.GUI_TEXTURED, PARTS, -18, -18, unlocked ? 0.0f : 40.0f, 0.0f, 36, 36, TEX, TEX, ARGB.white(open));
        int iconColor = unlocked ? ARGB.white(open) : ARGB.color(Math.round(150 * open), 70, 66, 84);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, ability.icon(), -12, -12, 24, 24, iconColor);
        if (unlocked) {
            long rest = HeroAbilities.cooldown(player, ability);
            if (rest > 0L) {
                int height = Mth.ceil(24.0f * Mth.clamp((rest - partial) / ability.cooldownTicks(), 0.0f, 1.0f));
                g.fill(-12, 12 - height, 12, 12, ARGB.color(Math.round(150 * open), 12, 8, 20));
            }
        } else {
            g.blit(RenderPipelines.GUI_TEXTURED, PARTS, -5, -6, 192.0f, 0.0f, 10, 12, TEX, TEX, ARGB.white(open));
            String from = String.valueOf(ability.level());
            g.pose().pushMatrix();
            g.pose().translate(0.0f, 13.0f);
            g.pose().scale(0.75f, 0.75f);
            g.text(this.font, from, -this.font.width(from) / 2, 0, ARGB.color(Math.round(230 * open), 0xC8B8A0), true);
            g.pose().popMatrix();
        }
        if (selected) {
            g.blit(RenderPipelines.GUI_TEXTURED, PARTS, -20, -20, 136.0f, 0.0f, 40, 40, TEX, TEX, ARGB.color(Math.round(230 * open), GOLD));
        }
        if (h > 0.02f && unlocked) {
            g.blit(RenderPipelines.GUI_TEXTURED, PARTS, -20, -20, 136.0f, 0.0f, 40, 40, TEX, TEX,
                    ARGB.color(Math.round(255 * h * open), ARGB.srgbLerp(0.35f, ability.color(), 0xFFFFFF)));
        }
        g.pose().popMatrix();
    }

    private void drawCenter(GuiGraphicsExtractor g, Player player, int level, HeroAbilities.State state, float open, float partial) {
        int index = this.hovered >= 0 ? this.hovered : state.selectedAbility().ordinal();
        HeroAbility ability = ORDER[index];
        boolean unlocked = ability.unlocked(level);
        int alpha = Math.round(255 * open);
        int y = -34;
        g.centeredText(this.font, Component.translatable(ability.translationKey()), 0, y, ARGB.color(alpha, unlocked ? ability.color() : 0x9A90A8));
        y += 11;
        g.centeredText(this.font, unlocked ? Component.translatable("gui.kingdomsatwar.ability_wheel.cooldown", ability.cooldownSeconds())
                : Component.translatable("gui.kingdomsatwar.ability_wheel.locked", ability.level()), 0, y, ARGB.color(alpha, unlocked ? GOLD : MUTED));
        y += 13;
        List<FormattedCharSequence> lines = this.font.split(Component.translatable(ability.translationKey() + ".desc"), 120);
        g.pose().pushMatrix();
        g.pose().scale(0.75f, 0.75f);
        int ly = Math.round(y / 0.75f);
        for (FormattedCharSequence line : lines.subList(0, Math.min(4, lines.size()))) {
            g.centeredText(this.font, line, 0, ly, ARGB.color(alpha, unlocked ? TEXT : MUTED));
            ly += 10;
        }
        g.pose().popMatrix();
        if (unlocked) {
            long rest = HeroAbilities.cooldown(player, ability);
            Component status = rest > 0L ? Component.translatable("gui.kingdomsatwar.ability_wheel.resting", (rest + 19L) / 20L)
                    : Component.translatable("gui.kingdomsatwar.ability_wheel.ready").withStyle(ChatFormatting.BOLD);
            g.centeredText(this.font, status, 0, 30, ARGB.color(alpha, rest > 0L ? 0xFFE0A0 : 0x9CF08A));
        }
    }

    // =====================================================================================
    // Bedienung
    // =====================================================================================

    private void confirm() {
        Player player = this.minecraft.player;
        if (this.hovered >= 0 && player != null) {
            HeroAbility ability = ORDER[this.hovered];
            if (ability.unlocked(this.heroLevel())) {
                AbilityClient.select(player, ability);
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.3f, 0.8f));
            } else {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.CHAIN_PLACE, 0.6f, 0.6f));
                if (!this.holding) {
                    return;
                }
            }
        }
        this.onClose();
    }

    private void release() {
        if (!this.holding) {
            return;
        }
        if (this.hovered < 0 && Util.getMillis() - this.openedAt < 250L) {
            this.holding = false;
        } else {
            this.confirm();
        }
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (ModKeyMappings.ABILITY.matches(event)) {
            this.release();
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (ModKeyMappings.ABILITY.matchesMouse(event)) {
            this.release();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!this.holding && ModKeyMappings.ABILITY.matches(event)) {
            this.confirm();
            return true;
        }
        int key = event.key();
        if (key >= InputConstants.KEY_1 && key <= InputConstants.KEY_8) {
            this.hovered = key - InputConstants.KEY_1;
            this.holding = true;
            this.confirm();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
            this.onClose();
            return true;
        }
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            this.hovered = pick(event.x() - this.width / 2.0, event.y() - this.height / 2.0);
            if (this.hovered < 0) {
                this.onClose();
            } else {
                this.confirm();
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
