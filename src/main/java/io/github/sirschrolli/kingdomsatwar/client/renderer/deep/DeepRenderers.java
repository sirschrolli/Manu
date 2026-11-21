package io.github.sirschrolli.kingdomsatwar.client.renderer.deep;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Modell-Ebenen und Renderer der Wesen der Tiefe (seit 1.7): Sordin, der Kantor der Stille. */
public final class DeepRenderers {
    /** Sordin, der Kantor der Stille (1.7). */
    public static final ModelLayerLocation SORDIN = layer("sordin");

    private DeepRenderers() {}

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(KingdomsAtWar.id(name), "main");
    }

    static Identifier texture(String folder, String name) {
        return KingdomsAtWar.id("textures/entity/" + folder + "/" + name + ".png");
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SORDIN, io.github.sirschrolli.kingdomsatwar.client.model.geom.SordinGeometry::createLayer);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SORDIN.get(), SordinRenderer::new);
    }
}
