package io.github.sirschrolli.kingdomsatwar.client.waystone;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.network.OpenWaystoneCompassPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

/** Wegkompass und Wegsteine auf dem eigenen Bildschirm (seit 1.12): das Kompassfenster und der schwebende Kristall. */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class WaystoneClient {
    private WaystoneClient() {}

    @SubscribeEvent
    static void registerHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(OpenWaystoneCompassPayload.TYPE, (payload, context) -> Minecraft.getInstance().gui.setScreen(
                new WaystoneCompassScreen(payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND)));
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.WAYSTONE.get(), WaystoneRenderer::new);
    }
}
