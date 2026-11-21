package io.github.sirschrolli.kingdomsatwar.client.hero;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.ModKeyMappings;
import io.github.sirschrolli.kingdomsatwar.hero.HeroAbilities;
import io.github.sirschrolli.kingdomsatwar.hero.HeroAbility;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * Die Fähigkeit an der Schnellleiste (seit 1.11): auf der Seite der Nebenhand (gegenüber dem Zauber) die gewählte Fähigkeit in einer
 * goldenen Fassung, darunter klein die Taste; ruht sie, füllt sich ein dunkler Schleier von unten, und ist sie wieder bereit,
 * blitzt die Fassung auf. Beim Wechsel steht kurz ihr Name über der Schnellleiste. Erst ab der ersten Fähigkeit (Stufe 10).
 */
public final class AbilityHud {
    public static final Identifier LAYER = KingdomsAtWar.id("ability_hud");
    private static final Identifier PARTS = KingdomsAtWar.id("textures/gui/spell_wheel/parts.png");
    private static final int TEX = 256;
    private static final int NAME_TICKS = 50;

    private static @Nullable HeroAbility shown;
    private static int nameTicks;
    private static int readyFlash;
    private static int shakeTicks;
    private static boolean wasResting;

    private AbilityHud() {}

    /** Zeigt den Namen der eben gewählten Fähigkeit über der Schnellleiste. */
    public static void announce(HeroAbility ability) {
        shown = ability;
        nameTicks = NAME_TICKS;
    }

    /** Die Fassung wackelt kurz (die Fähigkeit ruht noch). */
    static void shake() {
        shakeTicks = 8;
    }

    static void tick() {
        if (nameTicks > 0) {
            nameTicks--;
        }
        if (readyFlash > 0) {
            readyFlash--;
        }
        if (shakeTicks > 0) {
            shakeTicks--;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        HeroAbility ability = HeroAbilities.state(player).selectedAbility();
        boolean resting = HeroAbilities.cooldown(player, ability) > 0L;
        if (wasResting && !resting) {
            readyFlash = 12;
        }
        wasResting = resting;
    }

    public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.gui.hud.isHidden() || player.isSpectator()) {
            return;
        }
        int level = HeroLevel.of(player).level();
        if (HeroAbility.count(level) == 0) {
            return;
        }
        HeroAbility ability = HeroAbilities.state(player).selectedAbility();
        boolean unlocked = ability.unlocked(level);
        int center = g.guiWidth() / 2;
        // auf der Seite der Nebenhand, gegenüber der Zauberanzeige – mit Platz für das Feld der Nebenhand
        boolean offhandLeft = player.getMainArm().getOpposite() == HumanoidArm.LEFT;
        int x = offhandLeft ? center - 91 - 29 - 4 - 28 : center + 91 + 29 + 4;
        int y = g.guiHeight() - 28;
        float partial = delta.getGameTimeDeltaPartialTick(true);
        if (shakeTicks > 0) {
            x += Math.round(Mth.sin((shakeTicks - partial) * 2.4f) * 2.0f);
        }
        long rest = unlocked ? HeroAbilities.cooldown(player, ability) : 0L;
        float glow = rest > 0L || !unlocked ? 0.0f : 0.35f;
        if (readyFlash > 0) {
            glow = Math.max(glow, (readyFlash - partial) / 12.0f);
        }
        if (glow > 0.01f) {
            g.blit(RenderPipelines.GUI_TEXTURED, PARTS, x - 6, y - 6, 80.0f, 0.0f, 40, 40, 52, 52, TEX, TEX, ARGB.color(Math.round(200 * glow),
                    ability.color()));
        }
        g.blit(RenderPipelines.GUI_TEXTURED, PARTS, x, y, 0.0f, 72.0f, 28, 28, TEX, TEX);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, ability.icon(), x + 2, y + 2, 24, 24, unlocked ? 0xFFFFFFFF : 0xFF4A4458);
        if (rest > 0L) {
            float part = Mth.clamp((rest - partial) / ability.cooldownTicks(), 0.0f, 1.0f);
            int h = Mth.ceil(24 * part);
            g.fill(x + 2, y + 26 - h, x + 26, y + 26, ARGB.color(150, 12, 8, 20));
            String seconds = String.valueOf((rest + 19L) / 20L);
            g.text(mc.font, seconds, x + 14 - mc.font.width(seconds) / 2, y + 10, 0xFFFFE0A0, true);
        }
        // die Taste, klein unten links
        Component key = ModKeyMappings.ABILITY.getTranslatedKeyMessage();
        String label = key.getString();
        if (label.length() <= 3) {
            g.pose().pushMatrix();
            g.pose().translate(x + 1, y + 19);
            g.pose().scale(0.75f, 0.75f);
            g.text(mc.font, label, 0, 0, 0xFFF6DC96, true);
            g.pose().popMatrix();
        }
        if (nameTicks > 0 && shown != null) {
            float t = nameTicks - partial;
            int alpha = Math.round(255 * Mth.clamp(t / 10.0f, 0.0f, 1.0f));
            if (alpha > 8) {
                Component name = Component.translatable(shown.translationKey());
                int w = mc.font.width(name);
                int ny = g.guiHeight() - 72;
                g.fill(center - w / 2 - 4, ny - 2, center + w / 2 + 4, ny + 10, ARGB.color(alpha / 3, 0, 0, 0));
                g.text(mc.font, name, center - w / 2, ny, ARGB.color(alpha, shown.color()), true);
            }
        }
    }

}
