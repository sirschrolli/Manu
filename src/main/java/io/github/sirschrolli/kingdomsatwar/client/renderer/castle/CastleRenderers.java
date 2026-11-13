package io.github.sirschrolli.kingdomsatwar.client.renderer.castle;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Modell-Ebenen und Renderer der Raubritterburg (seit 1.8): Kuno von Rabenfels, der Raubritter, mit seinem Ross, und das Rabenbanner. */
public final class CastleRenderers {
    /** Kuno von Rabenfels, der Raubritter (1.8). */
    public static final ModelLayerLocation ROBBER_KNIGHT = layer("robber_knight");

    private CastleRenderers() {}

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(KingdomsAtWar.id(name), "main");
    }

    static Identifier texture(String folder, String name) {
        return KingdomsAtWar.id("textures/entity/" + folder + "/" + name + ".png");
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ROBBER_KNIGHT, io.github.sirschrolli.kingdomsatwar.client.model.geom.RobberKnightGeometry::createLayer);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ROBBER_KNIGHT.get(), RobberKnightRenderer::new);
        event.registerBlockEntityRenderer(io.github.sirschrolli.kingdomsatwar.registry.CastleGear.RAVEN_BANNER_ENTITY.get(), RavenBannerRenderer::new);
    }
}
