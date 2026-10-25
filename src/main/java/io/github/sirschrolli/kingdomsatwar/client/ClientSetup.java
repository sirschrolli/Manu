package io.github.sirschrolli.kingdomsatwar.client;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.CaveTrollGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.DeerGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.DragonArmorGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.DragonGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.GooseGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.LindwormGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.WildBoarGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.WillOWispGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.SoldierModel;
import io.github.sirschrolli.kingdomsatwar.client.renderer.BanditRenderer;
import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.CaveTrollRenderer;
import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.DeerRenderer;
import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.GooseRenderer;
import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.LindwormRenderer;
import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.WildBoarRenderer;
import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.WillOWispRenderer;
import io.github.sirschrolli.kingdomsatwar.client.renderer.DragonRenderer;
import io.github.sirschrolli.kingdomsatwar.client.renderer.SoldierRenderer;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Anmeldung aller Client-Dinge: Modelle und Renderer der Lebewesen. */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.DRAGON, DragonGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.DRAGON_ARMOR, DragonArmorGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.WILD_BOAR, WildBoarGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.DEER, DeerGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.GOOSE, GooseGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.WILL_O_WISP, WillOWispGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.CAVE_TROLL, CaveTrollGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.LINDWORM, LindwormGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.SOLDIER, SoldierModel::createLayer);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ModEntities.DRAGONS.values().forEach(type -> event.registerEntityRenderer(type.get(), DragonRenderer::new));
        ModEntities.SOLDIERS.values().forEach(type -> event.registerEntityRenderer(type.get(), SoldierRenderer::new));
        event.registerEntityRenderer(ModEntities.BANDIT.get(), BanditRenderer::new);
        event.registerEntityRenderer(ModEntities.BANDIT_ARCHER.get(), BanditRenderer::new);
        event.registerEntityRenderer(ModEntities.WILD_BOAR.get(), WildBoarRenderer::new);
        event.registerEntityRenderer(ModEntities.DEER.get(), DeerRenderer::new);
        event.registerEntityRenderer(ModEntities.GOOSE.get(), GooseRenderer::new);
        event.registerEntityRenderer(ModEntities.WILL_O_WISP.get(), WillOWispRenderer::new);
        event.registerEntityRenderer(ModEntities.CAVE_TROLL.get(), CaveTrollRenderer::new);
        event.registerEntityRenderer(ModEntities.LINDWORM.get(), LindwormRenderer::new);
    }
}
