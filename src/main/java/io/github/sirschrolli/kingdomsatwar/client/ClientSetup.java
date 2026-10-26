package io.github.sirschrolli.kingdomsatwar.client;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.boss.BossProjectileRenderer;
import io.github.sirschrolli.kingdomsatwar.client.boss.DragonBossModel;
import io.github.sirschrolli.kingdomsatwar.client.boss.DragonBossRenderer;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.PyrrhonGeometry;
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
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import io.github.sirschrolli.kingdomsatwar.client.boss.BossParticle;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;

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
        event.registerLayerDefinition(ModModelLayers.PYRRHON, PyrrhonGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.GLACIARA, io.github.sirschrolli.kingdomsatwar.client.model.geom.GlaciaraGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.VERDANTIS, io.github.sirschrolli.kingdomsatwar.client.model.geom.VerdantisGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.TERRAGON, io.github.sirschrolli.kingdomsatwar.client.model.geom.TerragonGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.VOLTARION, io.github.sirschrolli.kingdomsatwar.client.model.geom.VoltarionGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.THALASSA, io.github.sirschrolli.kingdomsatwar.client.model.geom.ThalassaGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.VINELING, io.github.sirschrolli.kingdomsatwar.client.model.geom.VinelingGeometry::createLayer);
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
        event.registerEntityRenderer(ModEntities.SEAT.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.WILL_O_WISP.get(), WillOWispRenderer::new);
        event.registerEntityRenderer(ModEntities.CAVE_TROLL.get(), CaveTrollRenderer::new);
        event.registerEntityRenderer(ModEntities.LINDWORM.get(), LindwormRenderer::new);
        // Drachen-Bosse (Modell in Grundgröße, im Spiel ×2,1)
        event.registerEntityRenderer(ModEntities.PYRRHON.get(), context -> new DragonBossRenderer<>(context,
                new DragonBossModel(context.bakeLayer(ModModelLayers.PYRRHON), 6, 8, true, 4, 32.0f),
                KingdomsAtWar.id("textures/entity/boss/pyrrhon.png"), KingdomsAtWar.id("textures/entity/boss/pyrrhon_glow.png"),
                2.1f, 4.4f, 4.5f));
        event.registerEntityRenderer(ModEntities.GLACIARA.get(), context -> new DragonBossRenderer<>(context,
                new io.github.sirschrolli.kingdomsatwar.client.boss.GlaciaraModel(context.bakeLayer(ModModelLayers.GLACIARA)),
                KingdomsAtWar.id("textures/entity/boss/glaciara.png"), KingdomsAtWar.id("textures/entity/boss/glaciara_glow.png"),
                2.1f, 4.8f, 4.0f));
        event.registerEntityRenderer(ModEntities.VERDANTIS.get(), context -> new DragonBossRenderer<>(context,
                new io.github.sirschrolli.kingdomsatwar.client.boss.VerdantisModel(context.bakeLayer(ModModelLayers.VERDANTIS)),
                KingdomsAtWar.id("textures/entity/boss/verdantis.png"), KingdomsAtWar.id("textures/entity/boss/verdantis_glow.png"),
                2.0f, 4.2f, 4.8f));
        event.registerEntityRenderer(ModEntities.TERRAGON.get(), context -> new DragonBossRenderer<>(context,
                new io.github.sirschrolli.kingdomsatwar.client.boss.TerragonModel(context.bakeLayer(ModModelLayers.TERRAGON)),
                KingdomsAtWar.id("textures/entity/boss/terragon.png"), KingdomsAtWar.id("textures/entity/boss/terragon_glow.png"),
                2.2f, 4.3f, 5.0f));
        event.registerEntityRenderer(ModEntities.VOLTARION.get(), context -> new DragonBossRenderer<>(context,
                new io.github.sirschrolli.kingdomsatwar.client.boss.VoltarionModel(context.bakeLayer(ModModelLayers.VOLTARION)),
                KingdomsAtWar.id("textures/entity/boss/voltarion.png"), KingdomsAtWar.id("textures/entity/boss/voltarion_glow.png"),
                2.1f, 4.2f, 4.2f));
        event.registerEntityRenderer(ModEntities.THALASSA.get(), context -> new DragonBossRenderer<>(context,
                new io.github.sirschrolli.kingdomsatwar.client.boss.ThalassaModel(context.bakeLayer(ModModelLayers.THALASSA)),
                KingdomsAtWar.id("textures/entity/boss/thalassa.png"), KingdomsAtWar.id("textures/entity/boss/thalassa_glow.png"),
                2.0f, 1.5f, 4.0f));
        event.registerEntityRenderer(ModEntities.VINELING.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.creature.VinelingRenderer::new);
        event.registerEntityRenderer(ModEntities.BOSS_PROJECTILE.get(), BossProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.SEAL_BREAK.get(), io.github.sirschrolli.kingdomsatwar.client.story.SealBreakRenderer::new);
    }

    @SubscribeEvent
    static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.BOSS_FLAME.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.FLAME));
        event.registerSpriteSet(ModParticles.BOSS_FROST.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.FROST));
        event.registerSpriteSet(ModParticles.BOSS_SPARK.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.SPARK));
        event.registerSpriteSet(ModParticles.BOSS_SPORE.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.SPORE));
        event.registerSpriteSet(ModParticles.BOSS_DUST.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.DUST));
        event.registerSpriteSet(ModParticles.BOSS_WATER.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.WATER));
    }
}
