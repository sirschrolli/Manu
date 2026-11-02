package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.LeermilbeGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.LeerwandlerGeometry;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Modell-Ebenen und Renderer der Wesen des Ends (E5c). Die Modelle haben die Knochen der Vanilla-Wesen, die sie
 * ersetzen, damit deren Bewegungen (Gehen, Greifen, getragener Block, aufgerissenes Maul, Schlängeln) unverändert passen.
 */
public final class EndRenderers {
    public static final ModelLayerLocation LEERWANDLER = layer("leerwandler");
    public static final ModelLayerLocation LEERMILBE = layer("leermilbe");

    private EndRenderers() {}

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(KingdomsAtWar.id(name), "main");
    }

    static Identifier texture(String folder, String name) {
        return KingdomsAtWar.id("textures/entity/" + folder + "/" + name + ".png");
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LEERWANDLER, LeerwandlerGeometry::createLayer);
        event.registerLayerDefinition(LEERMILBE, LeermilbeGeometry::createLayer);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ENDERMAN.get(), LeerwandlerRenderer::new);
        event.registerEntityRenderer(ModEntities.ENDERMITE.get(), LeermilbeRenderer::new);
    }
}
