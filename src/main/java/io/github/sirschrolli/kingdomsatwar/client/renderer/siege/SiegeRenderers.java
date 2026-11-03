package io.github.sirschrolli.kingdomsatwar.client.renderer.siege;

import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.BallistaBoltGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.BallistaGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.BatteringRamGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.CatapultGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.SiegeStoneGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.TrebuchetGeometry;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Anmeldung der Modelle und Renderer der Belagerungswaffen. */
public final class SiegeRenderers {
    private SiegeRenderers() {}

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.CATAPULT, CatapultGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.TREBUCHET, TrebuchetGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.BALLISTA, BallistaGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.BATTERING_RAM, BatteringRamGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.SIEGE_STONE, SiegeStoneGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.BALLISTA_BOLT, BallistaBoltGeometry::createLayer);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (SiegeType siege : SiegeType.values()) {
            event.registerEntityRenderer(ModEntities.siegeWeapon(siege), context -> new SiegeWeaponRenderer(context, siege));
        }
        event.registerEntityRenderer(ModEntities.SIEGE_PROJECTILE.get(), SiegeProjectileRenderer::new);
    }
}
