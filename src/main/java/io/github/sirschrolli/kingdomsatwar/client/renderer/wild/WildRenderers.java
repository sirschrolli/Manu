package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.BearGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.FelineGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.WildArmadilloGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.WildBatGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.WildBeeGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.WildCamelGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.WildFoxGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.WildLlamaGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.WildPandaGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.WildParrotGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.WildWolfGeometry;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildLlama;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildTraderLlama;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Modell-Ebenen und Renderer der eigenen Wildtiere, dazu der eigene Papagei auf der Schulter des Spielers. */
public final class WildRenderers {
    public static final ModelLayerLocation WOLF = layer("wolf");
    public static final ModelLayerLocation FOX = layer("fox");
    public static final ModelLayerLocation BEAR = layer("bear");
    public static final ModelLayerLocation PANDA = layer("panda");
    public static final ModelLayerLocation CAT = layer("cat");
    public static final ModelLayerLocation WILDCAT = layer("wildcat");
    public static final ModelLayerLocation PARROT = layer("parrot");
    public static final ModelLayerLocation BEE = layer("bee");
    public static final ModelLayerLocation BAT = layer("bat");
    public static final ModelLayerLocation ARMADILLO = layer("armadillo");
    public static final ModelLayerLocation CAMEL = layer("camel");
    public static final ModelLayerLocation LLAMA = layer("llama");

    /** Hauskatzen sind wie bei Vanilla kleiner gebaut als Wildkatzen. */
    private static final float CAT_SCALE = 0.8f;

    private WildRenderers() {}

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(KingdomsAtWar.id(name), "main");
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WOLF, WildWolfGeometry::createLayer);
        event.registerLayerDefinition(FOX, WildFoxGeometry::createLayer);
        event.registerLayerDefinition(BEAR, BearGeometry::createLayer);
        event.registerLayerDefinition(PANDA, WildPandaGeometry::createLayer);
        event.registerLayerDefinition(CAT, () -> LayerDefinition.create(FelineGeometry.createMesh(), FelineGeometry.TEXTURE_WIDTH,
                FelineGeometry.TEXTURE_HEIGHT).apply(MeshTransformer.scaling(CAT_SCALE)));
        event.registerLayerDefinition(WILDCAT, FelineGeometry::createLayer);
        event.registerLayerDefinition(PARROT, WildParrotGeometry::createLayer);
        event.registerLayerDefinition(BEE, WildBeeGeometry::createLayer);
        event.registerLayerDefinition(BAT, WildBatGeometry::createLayer);
        event.registerLayerDefinition(ARMADILLO, WildArmadilloGeometry::createLayer);
        event.registerLayerDefinition(CAMEL, WildCamelGeometry::createLayer);
        event.registerLayerDefinition(LLAMA, WildLlamaGeometry::createLayer);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.WOLF.get(), WildWolfRenderer::new);
        event.registerEntityRenderer(ModEntities.FOX.get(), WildFoxRenderer::new);
        event.registerEntityRenderer(ModEntities.BEAR.get(), BearRenderer::new);
        event.registerEntityRenderer(ModEntities.PANDA.get(), WildPandaRenderer::new);
        event.registerEntityRenderer(ModEntities.CAT.get(), HouseCatRenderer::new);
        event.registerEntityRenderer(ModEntities.WILDCAT.get(), WildcatRenderer::new);
        event.registerEntityRenderer(ModEntities.PARROT.get(), WildParrotRenderer::new);
        event.registerEntityRenderer(ModEntities.BEE.get(), WildBeeRenderer::new);
        event.registerEntityRenderer(ModEntities.BAT.get(), WildBatRenderer::new);
        event.registerEntityRenderer(ModEntities.ARMADILLO.get(), WildArmadilloRenderer::new);
        event.registerEntityRenderer(ModEntities.CAMEL.get(), WildCamelRenderer::new);
        event.registerEntityRenderer(ModEntities.LLAMA.get(), context -> new WildLlamaRenderer<WildLlama>(context));
        event.registerEntityRenderer(ModEntities.TRADER_LLAMA.get(), context -> new WildLlamaRenderer<WildTraderLlama>(context));
    }

    /**
     * Auf der Schulter sitzt ein Papagei für den Spieler als Vanilla-Papagei (nur dessen Farbe wird übertragen). Statt der
     * Vanilla-Ebene zeichnet ihn hier die eigene Ebene mit dem eigenen Modell. Gelingt das Entfernen der Vanilla-Ebene
     * nicht, bleibt alles, wie es ist (dann eben mit dem Vanilla-Papagei).
     */
    public static void replaceShoulderParrots(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType skin : event.getSkins()) {
            AvatarRenderer<?> renderer = event.getPlayerRenderer(skin);
            if (renderer != null && removeVanillaShoulderLayer(renderer)) {
                renderer.addLayer(new WildParrotOnShoulderLayer(renderer, event.getEntityModels()));
            }
        }
    }

    private static boolean removeVanillaShoulderLayer(LivingEntityRenderer<?, ?, ?> renderer) {
        try {
            for (Field field : LivingEntityRenderer.class.getDeclaredFields()) {
                if (List.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    List<?> layers = (List<?>) field.get(renderer);
                    if (layers.stream().anyMatch(layer -> layer instanceof ParrotOnShoulderLayer)) {
                        layers.removeIf(layer -> layer instanceof ParrotOnShoulderLayer);
                        return true;
                    }
                }
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            KingdomsAtWar.LOGGER.warn("Papagei auf der Schulter: Vanilla-Ebene nicht ersetzbar ({})", e.toString());
        }
        return false;
    }
}
