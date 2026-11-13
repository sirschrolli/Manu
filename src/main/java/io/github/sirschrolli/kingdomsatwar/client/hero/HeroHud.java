package io.github.sirschrolli.kingdomsatwar.client.hero;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;

/**
 * Bekommt der Held Heldenerfahrung, erscheint links über der Schnellleiste sein Rangabzeichen mit der Stufe, eine goldene
 * Leiste, die sich fließend füllt (das Neue leuchtet kurz auf), und eine Zahl "+40", die nach oben schwebt – dann
 * verblasst alles. Was schnell hintereinander kommt (im Kampf), wird zusammengezählt.
 */
public final class HeroHud {
    public static final Identifier LAYER = KingdomsAtWar.id("hero_experience");
    private static final long SHOW = 3000L;
    private static final long FADE = 650L;

    private static int lastExperience = -1;
    private static int lastLevel = -1;
    private static int gained;
    private static long shownAt = -100_000L;
    private static long gainAt = -100_000L;
    private static float shown;
    private static float fresh;
    private static long lastFrame;

    private HeroHud() {}

    /** Zeigt die Anzeige (z. B. beim Aufstieg) auch ohne neue Erfahrung. */
    static void ping() {
        shownAt = Util.getMillis();
    }

    public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            lastExperience = -1;
            return;
        }
        HeroLevel.Hero hero = HeroLevel.of(player);
        long now = Util.getMillis();
        float dt = Mth.clamp((now - lastFrame) / 1000.0f, 0.0f, 0.1f);
        lastFrame = now;
        float progress = HeroLevel.progress(hero.experience());
        if (lastExperience < 0 || hero.experience() < lastExperience) {
            lastExperience = hero.experience();
            lastLevel = hero.level();
            shown = progress;
            fresh = progress;
        } else if (hero.experience() > lastExperience) {
            gained = now - shownAt < SHOW ? gained + hero.experience() - lastExperience : hero.experience() - lastExperience;
            if (hero.level() > lastLevel) {
                shown = 0.0f;   // neue Stufe: die Leiste beginnt von vorn
                fresh = 0.0f;
            } else {
                fresh = Math.min(fresh, shown);
            }
            shownAt = now;
            gainAt = now;
            lastExperience = hero.experience();
            lastLevel = hero.level();
        }
        // die Leiste läuft dem Wert weich hinterher
        shown += (progress - shown) * Math.min(1.0f, dt * 5.0f);
        long age = now - shownAt;
        if (age > SHOW || mc.gui.hud.isHidden() || player.isSpectator()) {
            fresh = progress;
            return;
        }
        float alpha = age < SHOW - FADE ? 1.0f : 1.0f - (age - (SHOW - FADE)) / (float) FADE;
        float appear = HeroGui.easeOutCubic(Math.min(1.0f, age / 180.0f));
        alpha *= appear;
        if (alpha <= 0.02f) {
            return;
        }
        Font font = mc.font;
        HeroLevel.Rank rank = hero.rank();
        int color = HeroGui.rankColor(rank);
        int x = g.guiWidth() / 2 - 91;
        int y = g.guiHeight() - mc.gui.hud.leftHeight - 20 + Math.round(4 * (1.0f - appear));

        // Abzeichen mit Stufe
        g.blitSprite(RenderPipelines.GUI_TEXTURED, HeroGui.badge(rank), x, y, HeroGui.BADGE, HeroGui.BADGE, ARGB.white(alpha));
        String level = String.valueOf(hero.level());
        g.pose().pushMatrix();
        g.pose().translate(x + 7.5f, y + 4.5f);
        float s = level.length() >= 3 ? 0.55f : 0.75f;
        g.pose().scale(s, s);
        int lw = font.width(level);
        g.text(font, level, -lw / 2, 0, ARGB.color(Math.round(255 * alpha), color & 0xFFFFFF), false);
        g.pose().popMatrix();

        // Leiste
        int bx = x + 18;
        int bw = 74;
        float flash = 1.0f - Mth.clamp((now - gainAt) / 900.0f, 0.0f, 1.0f);
        g.pose().pushMatrix();
        drawBar(g, bx, y + 8, bw, alpha, shown, fresh, flash, color);
        g.pose().popMatrix();
        if (flash <= 0.0f) {
            fresh = shown;
        }

        // Rang und Zahl darüber
        Component name = Component.translatable(rank.translationKey());
        g.text(font, name, bx, y - 1, ARGB.color(Math.round(230 * alpha), color & 0xFFFFFF), true);
        if (gained > 0 && now - gainAt < SHOW) {
            float rise = Mth.clamp((now - gainAt) / 1200.0f, 0.0f, 1.0f);
            Component plus = Component.translatable("hud.kingdomsatwar.hero.plus", HeroGui.number(gained));
            int px = bx + bw + 4;
            int py = y + 5 - Math.round(6 * HeroGui.easeOutCubic(rise));
            g.text(font, plus, px, py, ARGB.color(Math.round(255 * alpha), 0xFFE7A0), true);
        }
    }

    private static void drawBar(GuiGraphicsExtractor g, int x, int y, int w, float alpha, float progress, float fresh, float flash, int color) {
        int a = Math.round(255 * alpha);
        g.fill(x, y, x + w, y + 5, ARGB.color(Math.round(a * 0.75f), 11, 9, 16));
        g.fill(x + 1, y + 1, x + w - 1, y + 4, ARGB.color(Math.round(a * 0.8f), 36, 31, 52));
        int fill = Math.round((w - 2) * Mth.clamp(progress, 0.0f, 1.0f));
        if (fill > 0) {
            int top = ARGB.srgbLerp(0.45f, color, 0xFFFFFFFF);
            g.fillGradient(x + 1, y + 1, x + 1 + fill, y + 4, ARGB.color(a, top & 0xFFFFFF), ARGB.color(a, color & 0xFFFFFF));
            if (flash > 0.0f && fresh < progress) {
                int from = x + 1 + Math.round((w - 2) * Mth.clamp(fresh, 0.0f, 1.0f));
                g.fill(from, y + 1, x + 1 + fill, y + 4, ARGB.color(Math.round(a * 0.75f * flash), 255, 250, 225));
            }
            g.fill(x + fill, y + 1, x + 1 + fill, y + 4, ARGB.color(a, 255, 255, 235));
        }
    }
}
