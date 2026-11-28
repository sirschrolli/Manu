package io.github.sirschrolli.kingdomsatwar.client.renderer.golem;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.CopperServantGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.HouseSpriteGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.IronSentinelGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.SnowmanGeometry;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Modell-Ebenen und Renderer der eigenen Golems und Wichtel (E4d). Jedes Modell hat die Knochen des Vanilla-Gegenstücks,
 * damit dessen Animationen (Schlag, Blumen-Reichen, Kopfdrehen, Truhen-Öffnen, Tanzen …) unverändert greifen.
 */
public final class GolemRenderers {
    public static final ModelLayerLocation IRON_GOLEM = layer("iron_golem");
    public static final ModelLayerLocation SNOW_GOLEM = layer("snow_golem");
    public static final ModelLayerLocation COPPER_GOLEM = layer("copper_golem");
    public static final ModelLayerLocation ALLAY = layer("allay");

    private GolemRenderers() {}

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(KingdomsAtWar.id(name), "main");
    }

    static Identifier texture(String folder, String name) {
        return KingdomsAtWar.id("textures/entity/" + folder + "/" + name + ".png");
    }

    static Identifier[] textures(String folder, String suffix, String... names) {
        Identifier[] out = new Identifier[names.length];
        for (int i = 0; i < names.length; i++) {
            out[i] = texture(folder, names[i] + suffix);
        }
        return out;
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(IRON_GOLEM, IronSentinelGeometry::createLayer);
        event.registerLayerDefinition(SNOW_GOLEM, SnowmanGeometry::createLayer);
        event.registerLayerDefinition(COPPER_GOLEM, CopperServantGeometry::createLayer);
        event.registerLayerDefinition(ALLAY, HouseSpriteGeometry::createLayer);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.IRON_GOLEM.get(), IronSentinelRenderer::new);
        event.registerEntityRenderer(ModEntities.SNOW_GOLEM.get(), SnowmanRenderer::new);
        event.registerEntityRenderer(ModEntities.COPPER_GOLEM.get(), CopperServantRenderer::new);
        event.registerEntityRenderer(ModEntities.ALLAY.get(), HouseSpriteRenderer::new);
        // die Statuen eines erstarrten Kupferknechts zeigen den Kupferknecht
        event.registerBlockEntityRenderer(net.minecraft.world.level.block.entity.BlockEntityTypes.COPPER_GOLEM_STATUE,
                CopperServantStatue.BlockRenderer::new);
    }

    /** Statuen als Gegenstand: eigene Art von Sondermodell (die Gegenstandsmodelle der Statuen verweisen darauf). */
    public static void registerSpecialRenderers(net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent event) {
        event.register(KingdomsAtWar.id("copper_servant_statue"), CopperServantStatue.ItemRenderer.Unbaked.MAP_CODEC);
    }
}
