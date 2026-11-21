package io.github.sirschrolli.kingdomsatwar.client.hero;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.config.ModClientConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Die Rüstungsleiste in Schichten (seit 0.78, wie die {@link HeroHearts Heldenherzen}): Minecraft zeigt höchstens zehn
 * Panzer (20 Rüstungspunkte). Hat ein Held mehr (die Ausrüstung des Schöpfers, Zähigkeit – bis 60 Punkte zählen), liegt
 * über den gewohnten silbernen Panzern eine Schicht in Gold, darüber eine in Diamantblau; links daneben steht die Zahl der
 * Rüstungspunkte. Bis 20 Punkte bleibt alles, wie es war. Abschaltbar zusammen mit den Heldenherzen ("Herzen in
 * Schichten").
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class HeroArmor {
    private static final Identifier VANILLA_FULL = Identifier.withDefaultNamespace("hud/armor_full");
    private static final Identifier FULL = KingdomsAtWar.id("hero/armor/full");
    private static final Identifier HALF = KingdomsAtWar.id("hero/armor/half");
    private static final Identifier SHINE = KingdomsAtWar.id("hero/armor/shine");
    /** Die Farben der Schichten über den silbernen Panzern: Gold, Diamantblau, Violett. */
    private static final int[] TIERS = {0xFFFFC43A, 0xFF66E8F5, 0xFFB46CFF};
    private static final int SILVER = 0xFFD8DCE6;

    private HeroArmor() {}

    @SubscribeEvent
    static void registerLayers(RegisterGuiLayersEvent event) {
        event.wrapLayer(VanillaGuiLayers.ARMOR_LEVEL, vanilla -> (GuiLayer) (g, delta) -> {
            if (!render(g, delta)) {
                vanilla.render(g, delta);
            }
        });
    }

    /** Die Farbe einer Schicht (0: silbern wie bei Minecraft). */
    public static int tierColor(int tier) {
        return tier <= 0 ? SILVER : TIERS[(tier - 1) % TIERS.length];
    }

    /** Zeichnet die Leiste in Schichten; {@code false}: Minecraft zeichnet sie wie gewohnt. */
    private static boolean render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.getCameraEntity() instanceof Player player) || !ModClientConfig.HERO_HEARTS.get() || player.getArmorValue() <= 20) {
            return false;
        }
        Hud hud = mc.gui.hud;
        if (hud.isHidden() || mc.gameMode == null || !mc.gameMode.canHurtPlayer()) {
            return true;
        }
        int armor = player.getArmorValue();
        int xLeft = g.guiWidth() / 2 - 91;
        int y = g.guiHeight() - hud.leftHeight;
        int top = (armor - 1) / 20;
        for (int i = 0; i < 10; i++) {
            int x = xLeft + i * 8;
            int here = armor - top * 20 - i * 2;
            if (here < 2) {
                icon(g, top - 1, false, x, y);
            }
            if (here >= 1) {
                icon(g, top, here == 1, x, y);
            }
        }
        String count = String.valueOf(armor);
        g.text(mc.font, count, xLeft - 3 - mc.font.width(count), y + 1, tierColor(top), true);
        hud.leftHeight += 10;
        return true;
    }

    private static void icon(GuiGraphicsExtractor g, int tier, boolean half, int x, int y) {
        if (tier <= 0) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, VANILLA_FULL, x, y, 9, 9);
            return;
        }
        g.blitSprite(RenderPipelines.GUI_TEXTURED, half ? HALF : FULL, x, y, 9, 9, tierColor(tier));
        if (!half) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, SHINE, x, y, 9, 9);
        }
    }
}
