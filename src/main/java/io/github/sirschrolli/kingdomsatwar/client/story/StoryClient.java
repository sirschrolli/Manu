package io.github.sirschrolli.kingdomsatwar.client.story;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.network.ChapterCardPayload;
import io.github.sirschrolli.kingdomsatwar.network.OpenChroniclePayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Die Geschichte auf dem eigenen Bildschirm: Chronik öffnen, Kapitelkarten einblenden. */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class StoryClient {
    private StoryClient() {}

    @SubscribeEvent
    static void registerHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(OpenChroniclePayload.TYPE, StoryClient::onOpenChronicle);
        event.register(ChapterCardPayload.TYPE, StoryClient::onChapterCard);
    }

    @SubscribeEvent
    static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.TITLE, ChapterCardOverlay.LAYER, ChapterCardOverlay::render);
    }

    private static void onOpenChronicle(OpenChroniclePayload payload, IPayloadContext context) {
        Minecraft.getInstance().gui.setScreen(new ChronicleScreen(payload.data()));
    }

    private static void onChapterCard(ChapterCardPayload payload, IPayloadContext context) {
        ChapterCardOverlay.queue(payload.chapter(), payload.fallen());
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        ChapterCardOverlay.tick();
    }
}
