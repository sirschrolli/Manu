package io.github.sirschrolli.kingdomsatwar.client.intro;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.config.ModClientConfig;
import io.github.sirschrolli.kingdomsatwar.network.StartIntroPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Startet das Erzähl-Intro, sobald der Server es verlangt und kein anderes Fenster offen ist. */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class IntroController {
    private static boolean pending;

    private IntroController() {}

    @SubscribeEvent
    static void registerHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(StartIntroPayload.TYPE, IntroController::onStartIntro);
    }

    private static void onStartIntro(StartIntroPayload payload, IPayloadContext context) {
        // Bei automatischen Screenshots (Entwicklung) nur auf ausdrücklichen Wunsch
        boolean showcase = System.getProperty("kingdomsatwar.showcase") != null;
        if (payload.forced() || (ModClientConfig.SHOW_INTRO.get() && !showcase)) {
            pending = true;
        }
    }

    /** Intro sofort beim nächsten Tick öffnen (z. B. für die Screenshot-Automatik). */
    public static void request() {
        pending = true;
    }

    /** Während des Intros ist der Spieler selbst nicht zu sehen – die Kamera gehört dem Drachenflug. */
    @SubscribeEvent
    static void onRenderPlayer(RenderPlayerEvent.Pre<?> event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() instanceof IntroScreen && minecraft.player != null
                && event.getRenderState().id == minecraft.player.getId()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!pending || minecraft.player == null || minecraft.level == null || minecraft.gui.screen() != null) {
            return;
        }
        pending = false;
        IntroScript script = IntroScript.load(minecraft.getResourceManager());
        if (script != null) {
            minecraft.gui.setScreen(new IntroScreen(script, minecraft.getLanguageManager().getSelected()));
        }
    }
}
