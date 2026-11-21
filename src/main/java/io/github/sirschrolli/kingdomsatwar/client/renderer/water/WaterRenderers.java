package io.github.sirschrolli.kingdomsatwar.client.renderer.water;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.CodfishGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.InkfishGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.NewtGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.PolliwogGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.PufferBigGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.PufferMidGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.PufferSmallGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.ReefFishLargeGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.ReefFishLargePatternGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.ReefFishSmallGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.ReefFishSmallPatternGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.SalmonidGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.SeaDolphinGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.SeaNautilusGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.SeaTurtleGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.ToadGeometry;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.client.model.animal.fish.SalmonModel;
import net.minecraft.client.model.animal.squid.SquidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Modell-Ebenen und Renderer der eigenen Wassertiere. Jedes Modell hat die Knochen des Vanilla-Gegenstücks, damit die
 * Vanilla-Animationen (Flossenschlag, Fangarme, Aufblasen, Quaken, Totstellen …) unverändert greifen.
 */
public final class WaterRenderers {
    public static final ModelLayerLocation COD = layer("cod");
    public static final ModelLayerLocation SALMON = layer("salmon");
    public static final ModelLayerLocation SALMON_SMALL = layer("salmon_small");
    public static final ModelLayerLocation SALMON_LARGE = layer("salmon_large");
    public static final ModelLayerLocation PUFFER_SMALL = layer("pufferfish_small");
    public static final ModelLayerLocation PUFFER_MID = layer("pufferfish_mid");
    public static final ModelLayerLocation PUFFER_BIG = layer("pufferfish_big");
    public static final ModelLayerLocation REEF_SMALL = layer("tropical_fish_small");
    public static final ModelLayerLocation REEF_SMALL_PATTERN = layer("tropical_fish_small_pattern");
    public static final ModelLayerLocation REEF_LARGE = layer("tropical_fish_large");
    public static final ModelLayerLocation REEF_LARGE_PATTERN = layer("tropical_fish_large_pattern");
    public static final ModelLayerLocation SQUID = layer("squid");
    public static final ModelLayerLocation SQUID_BABY = layer("squid_baby");
    public static final ModelLayerLocation DOLPHIN = layer("dolphin");
    public static final ModelLayerLocation TURTLE = layer("turtle");
    public static final ModelLayerLocation NAUTILUS = layer("nautilus");
    public static final ModelLayerLocation AXOLOTL = layer("axolotl");
    public static final ModelLayerLocation FROG = layer("frog");
    public static final ModelLayerLocation TADPOLE = layer("tadpole");

    private WaterRenderers() {}

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(KingdomsAtWar.id(name), "main");
    }

    static net.minecraft.resources.Identifier texture(String folder, String name) {
        return KingdomsAtWar.id("textures/entity/" + folder + "/" + name + ".png");
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(COD, CodfishGeometry::createLayer);
        event.registerLayerDefinition(SALMON, SalmonidGeometry::createLayer);
        event.registerLayerDefinition(SALMON_SMALL, () -> SalmonidGeometry.createLayer().apply(SalmonModel.SMALL_TRANSFORMER));
        event.registerLayerDefinition(SALMON_LARGE, () -> SalmonidGeometry.createLayer().apply(SalmonModel.LARGE_TRANSFORMER));
        event.registerLayerDefinition(PUFFER_SMALL, PufferSmallGeometry::createLayer);
        event.registerLayerDefinition(PUFFER_MID, PufferMidGeometry::createLayer);
        event.registerLayerDefinition(PUFFER_BIG, PufferBigGeometry::createLayer);
        event.registerLayerDefinition(REEF_SMALL, ReefFishSmallGeometry::createLayer);
        event.registerLayerDefinition(REEF_SMALL_PATTERN, ReefFishSmallPatternGeometry::createLayer);
        event.registerLayerDefinition(REEF_LARGE, ReefFishLargeGeometry::createLayer);
        event.registerLayerDefinition(REEF_LARGE_PATTERN, ReefFishLargePatternGeometry::createLayer);
        event.registerLayerDefinition(SQUID, InkfishGeometry::createLayer);
        event.registerLayerDefinition(SQUID_BABY, () -> InkfishGeometry.createLayer().apply(SquidModel.BABY_TRANSFORMER));
        event.registerLayerDefinition(DOLPHIN, SeaDolphinGeometry::createLayer);
        event.registerLayerDefinition(TURTLE, SeaTurtleGeometry::createLayer);
        event.registerLayerDefinition(NAUTILUS, SeaNautilusGeometry::createLayer);
        event.registerLayerDefinition(AXOLOTL, NewtGeometry::createLayer);
        event.registerLayerDefinition(FROG, ToadGeometry::createLayer);
        event.registerLayerDefinition(TADPOLE, PolliwogGeometry::createLayer);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.COD.get(), CodfishRenderer::new);
        event.registerEntityRenderer(ModEntities.SALMON.get(), SalmonidRenderer::new);
        event.registerEntityRenderer(ModEntities.PUFFERFISH.get(), PufferRenderer::new);
        event.registerEntityRenderer(ModEntities.TROPICAL_FISH.get(), ReefFishRenderer::new);
        event.registerEntityRenderer(ModEntities.SQUID.get(), InkfishRenderer::new);
        event.registerEntityRenderer(ModEntities.GLOW_SQUID.get(), DeepKrakenRenderer::new);
        event.registerEntityRenderer(ModEntities.DOLPHIN.get(), SeaDolphinRenderer::new);
        event.registerEntityRenderer(ModEntities.TURTLE.get(), SeaTurtleRenderer::new);
        event.registerEntityRenderer(ModEntities.NAUTILUS.get(), SeaNautilusRenderer::new);
        event.registerEntityRenderer(ModEntities.ZOMBIE_NAUTILUS.get(), GhostNautilusRenderer::new);
        event.registerEntityRenderer(ModEntities.AXOLOTL.get(), NewtRenderer::new);
        event.registerEntityRenderer(ModEntities.FROG.get(), ToadRenderer::new);
        event.registerEntityRenderer(ModEntities.TADPOLE.get(), PolliwogRenderer::new);
    }
}
