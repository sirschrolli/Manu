package io.github.sirschrolli.kingdomsatwar.client.renderer.serpent;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Modell-Ebenen und Renderer des Schlangentempels (seit 1.10): Quetzara, die Gefiederte Schlange (Kopf, Glied, Schwanz), ihre
 * abgestreifte Haut, ihre Federn und ihr Gift.
 */
public final class SerpentRenderers {
    public static final ModelLayerLocation QUETZARA = layer("quetzara");
    public static final ModelLayerLocation QUETZARA_SEGMENT = layer("quetzara_segment");
    public static final ModelLayerLocation QUETZARA_TAIL = layer("quetzara_tail");
    public static final ModelLayerLocation QUETZARA_FEATHER = layer("quetzara_feather");
    public static final ModelLayerLocation VENOM_GLOB = layer("venom_glob");

    private SerpentRenderers() {}

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(KingdomsAtWar.id(name), "main");
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(QUETZARA, io.github.sirschrolli.kingdomsatwar.client.model.geom.QuetzaraGeometry::createLayer);
        event.registerLayerDefinition(QUETZARA_SEGMENT, io.github.sirschrolli.kingdomsatwar.client.model.geom.QuetzaraSegmentGeometry::createLayer);
        event.registerLayerDefinition(QUETZARA_TAIL, io.github.sirschrolli.kingdomsatwar.client.model.geom.QuetzaraTailGeometry::createLayer);
        event.registerLayerDefinition(QUETZARA_FEATHER, io.github.sirschrolli.kingdomsatwar.client.model.geom.QuetzaraFeatherGeometry::createLayer);
        event.registerLayerDefinition(VENOM_GLOB, io.github.sirschrolli.kingdomsatwar.client.model.geom.VenomGlobGeometry::createLayer);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.QUETZARA.get(), QuetzaraRenderer::new);
        event.registerEntityRenderer(ModEntities.SHED_SKIN.get(), ShedSkinRenderer::new);
        event.registerEntityRenderer(ModEntities.QUETZARA_FEATHER.get(), QuetzaraFeatherRenderer::new);
        event.registerEntityRenderer(ModEntities.VENOM_GLOB.get(), VenomGlobRenderer::new);
    }
}
