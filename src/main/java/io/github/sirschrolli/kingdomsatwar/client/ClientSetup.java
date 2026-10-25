package io.github.sirschrolli.kingdomsatwar.client;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.DragonGeometry;
import io.github.sirschrolli.kingdomsatwar.client.renderer.DragonRenderer;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Anmeldung aller Client-Dinge: Modelle und Renderer der Mobs. */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.DRAGON, DragonGeometry::createLayer);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ModEntities.DRAGONS.values().forEach(type -> event.registerEntityRenderer(type.get(), DragonRenderer::new));
    }
}
