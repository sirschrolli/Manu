package io.github.sirschrolli.kingdomsatwar.client.renderer.arena;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ArenaGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Modell-Ebenen und Renderer der Arena der Prüfungen (seit 1.9): Ventor, der Gladiator der Winde (Leib und Wind-Ebene), sein
 * geworfener Sturmdreizack, das Wurfnetz im Flug, die Windhosen und die Windsiegel über dem Arenasiegel.
 */
public final class ArenaRenderers {
    /** Ventor, der Gladiator der Winde (1.9): sein Leib … */
    public static final ModelLayerLocation VENTOR = layer("ventor");
    /** … und der durchscheinende Dunst seines Wirbels (gleiche Knochen, eigenes rollendes Bild). */
    public static final ModelLayerLocation VENTOR_WIND = layer("ventor_wind");

    private ArenaRenderers() {}

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(KingdomsAtWar.id(name), "main");
    }

    static Identifier texture(String folder, String name) {
        return KingdomsAtWar.id("textures/entity/" + folder + "/" + name + ".png");
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(VENTOR, io.github.sirschrolli.kingdomsatwar.client.model.geom.VentorGeometry::createLayer);
        event.registerLayerDefinition(VENTOR_WIND, io.github.sirschrolli.kingdomsatwar.client.model.geom.VentorWindGeometry::createLayer);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.VENTOR.get(), VentorRenderer::new);
        event.registerEntityRenderer(ModEntities.STORM_TRIDENT.get(), StormTridentRenderer::new);
        event.registerEntityRenderer(ModEntities.GLADIATOR_NET.get(), GladiatorNetRenderer::new);
        event.registerEntityRenderer(ModEntities.WIND_VORTEX.get(), WindVortexRenderer::new);
        event.registerBlockEntityRenderer(ArenaGear.ARENA_SEAL_ENTITY.get(), ArenaSealRenderer::new);
    }
}
