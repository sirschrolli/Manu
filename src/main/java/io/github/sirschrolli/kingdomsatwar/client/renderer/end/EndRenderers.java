package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.LeermilbeGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.LeerwandlerGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.SchreinfunkeGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.SchreinlingGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.SternenfresserGeometry;
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
    public static final ModelLayerLocation SCHREINLING = layer("schreinling");
    public static final ModelLayerLocation SCHREINFUNKE = layer("schreinfunke");
    public static final ModelLayerLocation STERNENFRESSER = layer("sternenfresser");

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
        event.registerLayerDefinition(SCHREINLING, SchreinlingGeometry::createLayer);
        event.registerLayerDefinition(SCHREINFUNKE, SchreinfunkeGeometry::createLayer);
        event.registerLayerDefinition(STERNENFRESSER, SternenfresserGeometry::createLayer);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ENDERMAN.get(), LeerwandlerRenderer::new);
        event.registerEntityRenderer(ModEntities.ENDERMITE.get(), LeermilbeRenderer::new);
        event.registerEntityRenderer(ModEntities.SHULKER.get(), SchreinlingRenderer::new);
        // die Geschosse der Schreinlinge sind Vanilla-Geschosse – sie bekommen das eigene Aussehen
        event.registerEntityRenderer(net.minecraft.world.entity.EntityTypes.SHULKER_BULLET, SchreinfunkeRenderer::new);
        // der Enderdrache bleibt der Vanilla-Drache (Kampf, Kristalle, Portal und Ei hängen an ihm) – mit eigenem Aussehen
        event.registerEntityRenderer(net.minecraft.world.entity.EntityTypes.ENDER_DRAGON, SternenfresserRenderer::new);
    }
}
