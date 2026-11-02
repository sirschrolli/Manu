package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.BorstlingBabyGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.BorstlingGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.GlutkeilerBabyGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.GlutkeilerGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.LavastelzerBabyGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.LavastelzerGeometry;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Modell-Ebenen und Renderer des Nether-Volks und seiner Tiere (E5a). Die Modelle haben die Knochen der Vanilla-Wesen,
 * die sie ersetzen, damit deren Animationen (Ohrenwackeln, Gold-Bewundern, Tanzen, Armbrust, Kopfstoß, Stelzen und
 * Haarwehen), Rüstungen und Sättel unverändert passen.
 */
public final class NetherRenderers {
    public static final ModelLayerLocation BORSTLING = layer("borstling");
    public static final ModelLayerLocation BORSTLING_BABY = layer("borstling_baby");
    public static final ModelLayerLocation GLUTKEILER = layer("glutkeiler");
    public static final ModelLayerLocation GLUTKEILER_BABY = layer("glutkeiler_baby");
    public static final ModelLayerLocation LAVASTELZER = layer("lavastelzer");
    public static final ModelLayerLocation LAVASTELZER_BABY = layer("lavastelzer_baby");

    private NetherRenderers() {}

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(KingdomsAtWar.id(name), "main");
    }

    static Identifier texture(String folder, String name) {
        return KingdomsAtWar.id("textures/entity/" + folder + "/" + name + ".png");
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BORSTLING, BorstlingGeometry::createLayer);
        event.registerLayerDefinition(BORSTLING_BABY, BorstlingBabyGeometry::createLayer);
        event.registerLayerDefinition(GLUTKEILER, GlutkeilerGeometry::createLayer);
        event.registerLayerDefinition(GLUTKEILER_BABY, GlutkeilerBabyGeometry::createLayer);
        event.registerLayerDefinition(LAVASTELZER, LavastelzerGeometry::createLayer);
        event.registerLayerDefinition(LAVASTELZER_BABY, LavastelzerBabyGeometry::createLayer);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.PIGLIN.get(), context -> new BorstlingRenderer(context, BORSTLING_BABY,
                ModelLayers.PIGLIN_ARMOR, ModelLayers.PIGLIN_BABY_ARMOR));
        event.registerEntityRenderer(ModEntities.PIGLIN_BRUTE.get(), context -> new BorstlingRenderer(context, BORSTLING,
                ModelLayers.PIGLIN_BRUTE_ARMOR, ModelLayers.PIGLIN_BRUTE_ARMOR));
        event.registerEntityRenderer(ModEntities.ZOMBIFIED_PIGLIN.get(), RottenBristlingRenderer::new);
        event.registerEntityRenderer(ModEntities.HOGLIN.get(), GlutkeilerRenderer::new);
        event.registerEntityRenderer(ModEntities.ZOGLIN.get(), ModerkeilerRenderer::new);
        event.registerEntityRenderer(ModEntities.STRIDER.get(), LavastelzerRenderer::new);
    }
}
