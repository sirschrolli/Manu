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
    private static long levelAt = -100_000L;
    /** So viele Stufen vor einem neuen Rang, einer Fähigkeit oder einem Titel meldet die Leiste das Ziel. */
    private static final int NEAR = 3;
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
                levelAt = now;
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
        HeroGoals.Goal goal = HeroGoals.next(hero.level());
        int remaining = goal == null ? 0 : goal.level() - hero.level();
        boolean near = goal != null && remaining <= NEAR;
        int x = g.guiWidth() / 2 - 91;
        int y = g.guiHeight() - mc.gui.hud.leftHeight - 20 - (near ? 8 : 0) + Math.round(4 * (1.0f - appear));
        int bx = x + 18;
        int bw = 74;

        // ein weicher dunkler Grund: so bleibt alles auch vor hellem Himmel und Schnee lesbar
        HeroGui.shade(g, x + 50.0f, y + 7.0f + (near ? 4.0f : 0.0f), 168.0f, near ? 46.0f : 36.0f, 0.5f * alpha);

        // das Abzeichen mit der Stufe – bei einer neuen Stufe hüpft es und glüht auf
        float bump = 1.0f + 0.3f * (1.0f - HeroGui.easeOutCubic(Mth.clamp((now - levelAt) / 500.0f, 0.0f, 1.0f)));
        float pulse = 0.8f + 0.2f * Mth.sin(now / 1000.0f * 3.0f);
        HeroGui.light(g, HeroGui.GLOW, x + 7.5f, y + 7.5f, 24.0f * bump, 0.0f, color, (0.28f * pulse + 0.4f * (bump - 1.0f) / 0.3f) * alpha);
        g.pose().pushMatrix();
        g.pose().translate(x + 7.5f, y + 7.5f);
        g.pose().scale(bump, bump);
        g.pose().translate(-7.5f, -7.5f);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, HeroGui.badge(rank), 0, 0, HeroGui.BADGE, HeroGui.BADGE, ARGB.white(alpha));
        String level = String.valueOf(hero.level());
        g.pose().translate(7.5f, 4.5f);
        float s = level.length() >= 3 ? 0.55f : 0.75f;
        g.pose().scale(s, s);
        int lw = font.width(level);
        g.text(font, level, -lw / 2, 0, ARGB.color(Math.round(255 * alpha), color & 0xFFFFFF), false);
        g.pose().popMatrix();

        // Leiste
        float flash = 1.0f - Mth.clamp((now - gainAt) / 900.0f, 0.0f, 1.0f);
        drawBar(g, bx, y + 8, bw, alpha, shown, fresh, flash, color, now);
        if (flash <= 0.0f) {
            fresh = shown;
        }

        // Rang darüber, die Stufe klein dahinter
        Component name = Component.translatable(rank.translationKey());
        HeroGui.text(g, font, name, bx, y - 1, ARGB.color(Math.round(235 * alpha), color & 0xFFFFFF));
        // die neue Erfahrung springt herein, steigt und glüht kurz
        if (gained > 0 && now - gainAt < SHOW) {
            float rise = Mth.clamp((now - gainAt) / 1200.0f, 0.0f, 1.0f);
            float pop = HeroGui.easeOutBack(Mth.clamp((now - gainAt) / 280.0f, 0.0f, 1.0f));
            Component plus = Component.translatable("hud.kingdomsatwar.hero.plus", HeroGui.number(gained));
            float px = bx + bw + 5;
            float py = y + 4.5f - 5.0f * HeroGui.easeOutCubic(rise);
            float glow = 1.0f - Mth.clamp((now - gainAt) / 700.0f, 0.0f, 1.0f);
            HeroGui.light(g, HeroGui.GLOW, px + font.width(plus) / 2.0f, py + 3.5f, 22.0f + font.width(plus), 0.0f, 0xFFF2C14E, 0.45f * glow * alpha);
            g.pose().pushMatrix();
            g.pose().translate(px, py + 3.5f);
            float ps = 0.7f + 0.3f * pop;
            g.pose().scale(ps, ps);
            HeroGui.text(g, font, plus, 0, -4, ARGB.color(Math.round(255 * alpha), 0xFFE7A0));
            g.pose().popMatrix();
        }
        // ein Ziel ist nah: „Noch 2 Stufen: Rang Held“
        if (near) {
            float in = HeroGui.easeOutCubic(Math.min(1.0f, age / 400.0f));
            int a = Math.round(255 * alpha * in);
            g.blitSprite(RenderPipelines.GUI_TEXTURED, goal.icon(), bx, y + 15, 8, 8, ARGB.white(alpha * in));
            Component hint = Component.translatable(remaining == 1 ? "hud.kingdomsatwar.hero.goal.one" : "hud.kingdomsatwar.hero.goal", remaining,
                    goal.label());
            g.pose().pushMatrix();
            g.pose().translate(bx + 10, y + 16.5f);
            g.pose().scale(0.75f, 0.75f);
            HeroGui.text(g, font, hint, 0, 0, ARGB.color(a, goal.color() & 0xFFFFFF));
            g.pose().popMatrix();
        }
    }

    private static void drawBar(GuiGraphicsExtractor g, int x, int y, int w, float alpha, float progress, float fresh, float flash, int color, long now) {
        int a = Math.round(255 * alpha);
        g.fill(x, y, x + w, y + 5, ARGB.color(Math.round(a * 0.8f), 11, 9, 16));
        g.fill(x + 1, y + 1, x + w - 1, y + 4, ARGB.color(Math.round(a * 0.85f), 36, 31, 52));
        int fill = Math.round((w - 2) * Mth.clamp(progress, 0.0f, 1.0f));
        if (fill > 0) {
            int top = ARGB.srgbLerp(0.45f, color, 0xFFFFFFFF);
            g.fillGradient(x + 1, y + 1, x + 1 + fill, y + 4, ARGB.color(a, top & 0xFFFFFF), ARGB.color(a, color & 0xFFFFFF));
            // ein Schimmer wandert durch die Füllung
            float t = (now / 1000.0f * 0.6f) % 1.0f;
            int sx = x + 1 + Math.round((w + 10) * t) - 8;
            for (int i = 0; i < 6; i++) {
                int px = sx + i;
                if (px >= x + 1 && px < x + 1 + fill) {
                    float k = 1.0f - Math.abs(i - 2.5f) / 3.0f;
                    g.fill(px, y + 1, px + 1, y + 4, ARGB.color(Math.round(a * 0.35f * k), 255, 255, 255));
                }
            }
            if (flash > 0.0f && fresh < progress) {
                int from = x + 1 + Math.round((w - 2) * Mth.clamp(fresh, 0.0f, 1.0f));
                g.fill(from, y + 1, x + 1 + fill, y + 4, ARGB.color(Math.round(a * 0.75f * flash), 255, 250, 225));
            }
            g.fill(x + fill, y + 1, x + 1 + fill, y + 4, ARGB.color(a, 255, 255, 235));
        }
    }
}
