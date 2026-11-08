package io.github.sirschrolli.kingdomsatwar.client.hero;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;

/**
 * Bekommt der Held Heldenerfahrung, steht links über der Schnellleiste kurz seine Heldenstufe, wie viel dazukam und eine
 * schmale Leiste bis zur nächsten Stufe – dann verblasst es. Was schnell hintereinander kommt (im Kampf), wird
 * zusammengezählt.
 */
public final class HeroHud {
    public static final Identifier LAYER = KingdomsAtWar.id("hero_experience");
    private static final long SHOW = 2600L;
    private static final long FADE = 600L;

    private static int lastExperience = -1;
    private static int gained;
    private static long shownAt;

    private HeroHud() {}

    public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            lastExperience = -1;
            return;
        }
        HeroLevel.Hero hero = HeroLevel.of(player);
        long now = Util.getMillis();
        if (lastExperience < 0 || hero.experience() < lastExperience) {
            lastExperience = hero.experience();
        } else if (hero.experience() > lastExperience) {
            gained = now - shownAt < SHOW ? gained + hero.experience() - lastExperience : hero.experience() - lastExperience;
            shownAt = now;
            lastExperience = hero.experience();
        }
        long age = now - shownAt;
        if (gained <= 0 || age > SHOW || mc.gui.hud.isHidden() || player.isSpectator()) {
            return;
        }
        float alpha = age < SHOW - FADE ? 1.0f : 1.0f - (age - (SHOW - FADE)) / (float) FADE;
        int a = Mth.clamp(Math.round(alpha * 255), 8, 255);
        int x = g.guiWidth() / 2 - 91;
        int y = g.guiHeight() - mc.gui.hud.leftHeight - 14;
        Component badge = Component.literal("★" + hero.level());
        Component text = Component.translatable("hud.kingdomsatwar.hero.gained", gained);
        g.text(mc.font, badge, x, y, ARGB.color(a, 246, 210, 122), true);
        int tx = x + mc.font.width(badge) + 4;
        g.text(mc.font, text, tx, y, ARGB.color(a, 232, 224, 208), true);
        int bw = 60;
        int by = y + 10;
        float progress = HeroLevel.progress(hero.experience());
        g.fill(x, by, x + bw, by + 2, ARGB.color(Math.round(a * 0.7f), 30, 26, 20));
        g.fill(x, by, x + Math.round(bw * progress), by + 2, ARGB.color(a, 224, 169, 58));
    }
}
