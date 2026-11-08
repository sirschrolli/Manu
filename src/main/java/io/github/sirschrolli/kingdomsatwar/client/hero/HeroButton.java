package io.github.sirschrolli.kingdomsatwar.client.hero;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Knöpfe im Stil des Charakterfensters: dunkles Violett mit Goldrand (Text hell, beim Darüberfahren golden), und der
 * runde goldene Plus-Knopf der Talente, der sanft pulsiert, solange er etwas tun kann.
 */
public class HeroButton extends Button {
    private final boolean round;

    protected HeroButton(int x, int y, int width, int height, Component message, OnPress onPress, boolean round) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.round = round;
    }

    /** Ein breiter Knopf mit Text. */
    public static HeroButton text(int x, int y, int width, Component message, OnPress onPress) {
        return new HeroButton(x, y, width, 20, message, onPress, false);
    }

    /** Der runde Plus-Knopf (13 × 13). */
    public static HeroButton plus(int x, int y, Component narration, OnPress onPress) {
        return new HeroButton(x, y, 13, 13, narration, onPress, true);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        boolean hover = this.isHoveredOrFocused();
        if (this.round) {
            float pulse = this.active ? 0.5f + 0.5f * Mth.sin(HeroGui.seconds() * 4.0f) : 0.0f;
            if (this.active) {
                HeroGui.light(g, HeroGui.GLOW, this.getX() + 6.5f, this.getY() + 6.5f, 30.0f, 0.0f, HeroGui.GOLD, 0.25f + 0.35f * pulse);
            }
            g.blitSprite(RenderPipelines.GUI_TEXTURED, !this.active ? HeroGui.PLUS_DISABLED : hover ? HeroGui.PLUS_HIGHLIGHTED : HeroGui.PLUS,
                    this.getX(), this.getY(), 13, 13, ARGB.white(this.alpha));
            return;
        }
        g.blitSprite(RenderPipelines.GUI_TEXTURED, !this.active ? HeroGui.BUTTON_DISABLED : hover ? HeroGui.BUTTON_HIGHLIGHTED : HeroGui.BUTTON,
                this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white(this.alpha));
        Font font = Minecraft.getInstance().font;
        Component label = this.getMessage();
        int color = !this.active ? 0xFF6E6880 : hover ? 0xFFFFE7A0 : HeroGui.TEXT;
        int x = this.getX() + (this.getWidth() - font.width(label)) / 2;
        int y = this.getY() + (this.getHeight() - 8) / 2;
        g.text(font, label, x + 1, y + 1, 0xFF0B0910, false);
        g.text(font, label, x, y, color, false);
    }
}
