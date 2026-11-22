package io.github.sirschrolli.kingdomsatwar.client.gui;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

/**
 * Tooltips im Stil ihres Fensters (seit 1.12): in den Fenstern des Helden und der Magie nachtblau mit Goldrand, in den
 * Pergament-Fenstern (Stadt, Handel, Heer, Chronik, Angeln) aus dunklem Leder mit Messingrand – statt im Violett des Spiels.
 * Ein Gegenstand mit eigenem Tooltip-Stil behält ihn.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class UiTheme {
    /** {@code textures/gui/sprites/tooltip/hero_background.png} und {@code …/hero_frame.png}. */
    public static final Identifier HERO = KingdomsAtWar.id("hero");
    /** {@code textures/gui/sprites/tooltip/parchment_background.png} und {@code …/parchment_frame.png}. */
    public static final Identifier PARCHMENT = KingdomsAtWar.id("parchment");

    /** Ein Fenster im Stil des Helden (Nachtblau und Gold). */
    public interface HeroStyle {}

    /** Ein Pergament-Fenster (seine Tooltips: dunkles Leder mit Messingrand). */
    public interface ParchmentStyle {}

    private UiTheme() {}

    /** Der Tooltip-Stil für dieses Fenster (null: der des Spiels). */
    public static @org.jspecify.annotations.Nullable Identifier style(Screen screen) {
        return screen instanceof HeroStyle ? HERO : screen instanceof ParchmentStyle ? PARCHMENT : null;
    }

    @SubscribeEvent
    static void onTooltipTexture(RenderTooltipEvent.Texture event) {
        if (event.getTexture() != null) {
            return;
        }
        Screen screen = Minecraft.getInstance().gui.screen();
        Identifier style = screen == null ? null : style(screen);
        if (style != null) {
            event.setTexture(style);
        }
    }
}
