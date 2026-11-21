package io.github.sirschrolli.kingdomsatwar.client.renderer.rare;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.CloudJellyGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.PrimevalDiggerBabyGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.PrimevalDiggerGeometry;
import io.github.sirschrolli.kingdomsatwar.client.renderer.TravelingMerchantRenderer;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.client.model.animal.ghast.HappyGhastModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Modell-Ebenen und Renderer der seltenen Wesen und des fahrenden Händlers (E4d-2). Urwühler und Wolkenqualle haben die
 * Knochen ihrer Vanilla-Gegenstücke, damit deren Animationen (Schnuppern, Graben, Aufstehen, Freudentanz; das Wehen
 * der Fangarme) und Zubehör (Geschirr, Schutzbrille, Seile) unverändert passen.
 */
public final class RareRenderers {
    public static final ModelLayerLocation SNIFFER = layer("sniffer");
    public static final ModelLayerLocation SNIFFER_BABY = layer("sniffer_baby");
    public static final ModelLayerLocation HAPPY_GHAST = layer("happy_ghast");
    public static final ModelLayerLocation HAPPY_GHAST_BABY = layer("happy_ghast_baby");

    private RareRenderers() {}

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(KingdomsAtWar.id(name), "main");
    }

    static Identifier texture(String folder, String name) {
        return KingdomsAtWar.id("textures/entity/" + folder + "/" + name + ".png");
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SNIFFER, PrimevalDiggerGeometry::createLayer);
        event.registerLayerDefinition(SNIFFER_BABY, PrimevalDiggerBabyGeometry::createLayer);
        // wie bei Vanilla: das Modell ist in Einheiten eines 16er-Würfels beschrieben und wird vierfach vergrößert,
        // das Wölkchen ist knapp einen Block groß
        event.registerLayerDefinition(HAPPY_GHAST, () -> CloudJellyGeometry.createLayer().apply(MeshTransformer.scaling(4.0f)));
        event.registerLayerDefinition(HAPPY_GHAST_BABY, () -> CloudJellyGeometry.createLayer().apply(MeshTransformer.scaling(4.0f))
                .apply(HappyGhastModel.BABY_TRANSFORMER));
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SNIFFER.get(), PrimevalDiggerRenderer::new);
        event.registerEntityRenderer(ModEntities.HAPPY_GHAST.get(), CloudJellyRenderer::new);
        event.registerEntityRenderer(ModEntities.WANDERING_TRADER.get(), TravelingMerchantRenderer::new);
    }
}
