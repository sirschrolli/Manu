package io.github.sirschrolli.kingdomsatwar.client.renderer.creator;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.DivineFeatherGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.DivineHaloGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.DivinePlanetGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.DivineSpearGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.SchrolliGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.SeraphlingGeometry;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Modell-Ebenen und Renderer Schrollis des Schöpfers, seiner Seraphlinge und seiner Geschosse. */
public final class CreatorRenderers {
    public static final ModelLayerLocation SCHROLLI = layer("schrolli");
    public static final ModelLayerLocation SERAPHLING = layer("seraphling");
    public static final ModelLayerLocation DIVINE_SPEAR = layer("divine_spear");
    public static final ModelLayerLocation DIVINE_FEATHER = layer("divine_feather");
    public static final ModelLayerLocation DIVINE_HALO = layer("divine_halo");
    public static final ModelLayerLocation DIVINE_PLANET = layer("divine_planet");

    private CreatorRenderers() {}

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(KingdomsAtWar.id(name), "main");
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SCHROLLI, SchrolliGeometry::createLayer);
        event.registerLayerDefinition(SERAPHLING, SeraphlingGeometry::createLayer);
        event.registerLayerDefinition(DIVINE_SPEAR, DivineSpearGeometry::createLayer);
        event.registerLayerDefinition(DIVINE_FEATHER, DivineFeatherGeometry::createLayer);
        event.registerLayerDefinition(DIVINE_HALO, DivineHaloGeometry::createLayer);
        event.registerLayerDefinition(DIVINE_PLANET, DivinePlanetGeometry::createLayer);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SCHROLLI.get(), SchrolliRenderer::new);
        event.registerEntityRenderer(ModEntities.SERAPHLING.get(), SeraphlingRenderer::new);
        event.registerEntityRenderer(ModEntities.DIVINE_SHOT.get(), DivineShotRenderer::new);
    }
}
