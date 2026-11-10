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
import io.github.sirschrolli.kingdomsatwar.client.model.geom.CrystalCrawlerGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.GlimmerMothGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.NightWingGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.ShadeStalkerGeometry;
import io.github.sirschrolli.kingdomsatwar.client.renderer.shadow.CrystalCrawlerRenderer;
import io.github.sirschrolli.kingdomsatwar.client.renderer.shadow.GlimmerMothRenderer;
import io.github.sirschrolli.kingdomsatwar.client.renderer.shadow.MoonStagRenderer;
import io.github.sirschrolli.kingdomsatwar.client.renderer.shadow.NightWingRenderer;
import io.github.sirschrolli.kingdomsatwar.client.renderer.shadow.ShadeStalkerRenderer;
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
        io.github.sirschrolli.kingdomsatwar.client.renderer.farm.FarmRenderers.registerLayers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderers.registerLayers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.monster.MonsterRenderers.registerLayers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.water.WaterRenderers.registerLayers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.golem.GolemRenderers.registerLayers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.rare.RareRenderers.registerLayers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.nether.NetherRenderers.registerLayers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.end.EndRenderers.registerLayers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.creator.CreatorRenderers.registerLayers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.siege.SiegeRenderers.registerLayers(event);
        event.registerLayerDefinition(io.github.sirschrolli.kingdomsatwar.client.armor.MonsterArmorLayer.LAYER,
                io.github.sirschrolli.kingdomsatwar.client.armor.MonsterArmorLayer::createLayer);
        // die Köpfe von Vanilla bekommen die Texturen der eigenen Wesen (die Modelle setzt SkullBlockRendererMixin ein)
        io.github.sirschrolli.kingdomsatwar.client.renderer.OwnHeads.install();
        event.registerLayerDefinition(ModModelLayers.WILD_BOAR, WildBoarGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.DEER, DeerGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.GOOSE, GooseGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.WILL_O_WISP, WillOWispGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.FAIRY, io.github.sirschrolli.kingdomsatwar.client.model.geom.FairyGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.MUSHROOMLING,
                io.github.sirschrolli.kingdomsatwar.client.model.geom.MushroomlingGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.THORN_WIGHT,
                io.github.sirschrolli.kingdomsatwar.client.model.geom.ThornWightGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.MOTH_QUEEN,
                io.github.sirschrolli.kingdomsatwar.client.model.geom.MothQueenGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.GLUTSCHMIED,
                io.github.sirschrolli.kingdomsatwar.client.model.geom.GlutschmiedGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.MISS_MYSTIK,
                io.github.sirschrolli.kingdomsatwar.client.model.geom.MissMystikGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.LIGHT_DOVE,
                io.github.sirschrolli.kingdomsatwar.client.model.geom.LightDoveGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.MAGIC_SWORD,
                io.github.sirschrolli.kingdomsatwar.client.model.geom.MagicSwordGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.ANNI_TROLL,
                io.github.sirschrolli.kingdomsatwar.client.model.geom.AnniTrollGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.ASTERIA,
                io.github.sirschrolli.kingdomsatwar.client.model.geom.AsteriaGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.CAVE_TROLL, CaveTrollGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.LINDWORM, LindwormGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.WEREWOLF, io.github.sirschrolli.kingdomsatwar.client.model.geom.WerewolfGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.MIRE_SHAMBLER, io.github.sirschrolli.kingdomsatwar.client.model.geom.MireShamblerGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.EMBER_WYRM, io.github.sirschrolli.kingdomsatwar.client.model.geom.EmberWyrmGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.ASH_WRAITH, io.github.sirschrolli.kingdomsatwar.client.model.geom.AshWraithGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.SOUL_HERON, io.github.sirschrolli.kingdomsatwar.client.model.geom.SoulHeronGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.NIGHT_RAM, io.github.sirschrolli.kingdomsatwar.client.model.geom.NightRamGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.RIFT_HOUND, io.github.sirschrolli.kingdomsatwar.client.model.geom.RiftHoundGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.CHORUS_MAW, io.github.sirschrolli.kingdomsatwar.client.model.geom.ChorusMawGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.SOLDIER, SoldierModel::createLayer);
        event.registerLayerDefinition(ModModelLayers.SOLDIER_RANK,
                () -> SoldierModel.createLayer(new net.minecraft.client.model.geom.builders.CubeDeformation(0.3f)));
        event.registerLayerDefinition(ModModelLayers.CITIZEN, io.github.sirschrolli.kingdomsatwar.client.model.CitizenModel::createLayer);
        event.registerLayerDefinition(ModModelLayers.PYRRHON, PyrrhonGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.NYXAR, io.github.sirschrolli.kingdomsatwar.client.model.geom.NyxarGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.GLACIARA, io.github.sirschrolli.kingdomsatwar.client.model.geom.GlaciaraGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.VERDANTIS, io.github.sirschrolli.kingdomsatwar.client.model.geom.VerdantisGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.TERRAGON, io.github.sirschrolli.kingdomsatwar.client.model.geom.TerragonGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.VOLTARION, io.github.sirschrolli.kingdomsatwar.client.model.geom.VoltarionGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.THALASSA, io.github.sirschrolli.kingdomsatwar.client.model.geom.ThalassaGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.VINELING, io.github.sirschrolli.kingdomsatwar.client.model.geom.VinelingGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.SHADE_STALKER, ShadeStalkerGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.CRYSTAL_CRAWLER, CrystalCrawlerGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.NIGHT_WING, NightWingGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.GLIMMER_MOTH, GlimmerMothGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.VOID_MAW, io.github.sirschrolli.kingdomsatwar.client.model.geom.VoidMawGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.SHARD_WARDEN, io.github.sirschrolli.kingdomsatwar.client.model.geom.ShardWardenGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.DISTORTLING, io.github.sirschrolli.kingdomsatwar.client.model.geom.DistortlingGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.STAR_RAY, io.github.sirschrolli.kingdomsatwar.client.model.geom.StarRayGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.WARDEN_SHARD, io.github.sirschrolli.kingdomsatwar.client.model.geom.WardenShardGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.SHATTERED_KING, io.github.sirschrolli.kingdomsatwar.client.model.geom.ShatteredKingGeometry::createLayer);
        // der Weltenfresser
        event.registerLayerDefinition(ModModelLayers.WORLD_EATER, io.github.sirschrolli.kingdomsatwar.client.model.geom.WorldEaterGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.WORLD_EATER_SEGMENT, io.github.sirschrolli.kingdomsatwar.client.model.geom.WorldEaterSegmentGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.WORLD_EATER_TAIL, io.github.sirschrolli.kingdomsatwar.client.model.geom.WorldEaterTailGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.WORLD_CHUNK, io.github.sirschrolli.kingdomsatwar.client.model.geom.WorldChunkGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.MANTA_RAY, io.github.sirschrolli.kingdomsatwar.client.model.geom.MantaRayGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.GLOW_JELLYFISH, io.github.sirschrolli.kingdomsatwar.client.model.geom.GlowJellyfishGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.SHARK, io.github.sirschrolli.kingdomsatwar.client.model.geom.SharkGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.ANGLERFISH, io.github.sirschrolli.kingdomsatwar.client.model.geom.AnglerfishGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.GIANT_CRAB, io.github.sirschrolli.kingdomsatwar.client.model.geom.GiantCrabGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.REEF_COLOSSUS, io.github.sirschrolli.kingdomsatwar.client.model.geom.ReefColossusGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.SHATTERED_CROWN, io.github.sirschrolli.kingdomsatwar.client.model.geom.ShatteredCrownGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.UMBRER, io.github.sirschrolli.kingdomsatwar.client.model.geom.UmbrerGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.MAGE, io.github.sirschrolli.kingdomsatwar.client.model.geom.MageGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.PHOENIX, io.github.sirschrolli.kingdomsatwar.client.model.geom.PhoenixGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.ICE_LANCE, io.github.sirschrolli.kingdomsatwar.client.model.geom.IceLanceGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.ICE_PRISON, io.github.sirschrolli.kingdomsatwar.client.model.geom.IcePrisonGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.STONE_SPIKE, io.github.sirschrolli.kingdomsatwar.client.model.geom.StoneSpikeGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.STONE_FIST, io.github.sirschrolli.kingdomsatwar.client.model.geom.StoneFistGeometry::createLayer);
        event.registerLayerDefinition(ModModelLayers.THORN_VINE, io.github.sirschrolli.kingdomsatwar.client.model.geom.ThornVineGeometry::createLayer);
        event.registerLayerDefinition(io.github.sirschrolli.kingdomsatwar.client.creator.CreatorGearLayer.LAYER,
                io.github.sirschrolli.kingdomsatwar.client.creator.CreatorGearLayer::createLayer);
        event.registerLayerDefinition(ModModelLayers.POISON_FLOWER,
                io.github.sirschrolli.kingdomsatwar.client.model.geom.PoisonFlowerGeometry::createLayer);
    }

    /** Der eigene Papagei auf der Schulter des Spielers. */
    @SubscribeEvent
    static void addLayers(EntityRenderersEvent.AddLayers event) {
        io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderers.replaceShoulderParrots(event);
        // die 3D-Teile der Monsterrüstungen (Spieler und Rüstungsständer)
        var parts = event.getEntityModels().bakeLayer(io.github.sirschrolli.kingdomsatwar.client.armor.MonsterArmorLayer.LAYER);
        // die Ausrüstung des Schöpfers: Schwingen aus Federn, Heiligenschein, Räder (seit 0.78)
        var gear = event.getEntityModels().bakeLayer(io.github.sirschrolli.kingdomsatwar.client.creator.CreatorGearLayer.LAYER);
        for (var skin : event.getSkins()) {
            var player = event.getPlayerRenderer(skin);
            if (player != null) {
                player.addLayer(new io.github.sirschrolli.kingdomsatwar.client.armor.MonsterArmorLayer<>(player, parts));
                player.addLayer(new io.github.sirschrolli.kingdomsatwar.client.creator.CreatorGearLayer<>(player, gear));
            }
        }
        if (event.getRenderer(net.minecraft.world.entity.EntityTypes.ARMOR_STAND)
                instanceof net.minecraft.client.renderer.entity.ArmorStandRenderer stand) {
            stand.addLayer(new io.github.sirschrolli.kingdomsatwar.client.armor.MonsterArmorLayer<>(stand, parts));
            stand.addLayer(new io.github.sirschrolli.kingdomsatwar.client.creator.CreatorGearLayer<>(stand, gear));
        }
    }

    @SubscribeEvent
    static void registerSpecialRenderers(net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent event) {
        io.github.sirschrolli.kingdomsatwar.client.renderer.golem.GolemRenderers.registerSpecialRenderers(event);
        event.register(io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.id("world_eater_trophy"),
                io.github.sirschrolli.kingdomsatwar.client.renderer.abyss.WorldEaterTrophyRenderer.ItemRenderer.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ModEntities.DRAGONS.values().forEach(type -> event.registerEntityRenderer(type.get(), DragonRenderer::new));
        ModEntities.SOLDIERS.values().forEach(type -> event.registerEntityRenderer(type.get(), SoldierRenderer::new));
        event.registerEntityRenderer(ModEntities.BANDIT.get(), BanditRenderer::new);
        ModEntities.REALM_WARRIORS.values().forEach(type -> event.registerEntityRenderer(type.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.RealmWarriorRenderer::new));
        ModEntities.MAGES.values().forEach(type -> event.registerEntityRenderer(type.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.mage.MageRenderer::new));
        event.registerEntityRenderer(ModEntities.MAGE_IMAGE.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.mage.MageRenderer::new);
        event.registerEntityRenderer(ModEntities.MAGIC_MISSILE.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.mage.MagicMissileRenderer::new);
        event.registerEntityRenderer(ModEntities.SPELL_FX.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.spell.SpellFxRenderer::new);
        event.registerEntityRenderer(ModEntities.SPELL_PROJECTILE.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.spell.SpellProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.CITIZEN.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.CitizenRenderer::new);
        event.registerEntityRenderer(ModEntities.VILLAGER.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.VillagerRenderer::new);
        event.registerEntityRenderer(ModEntities.BANDIT_ARCHER.get(), BanditRenderer::new);
        io.github.sirschrolli.kingdomsatwar.client.renderer.farm.FarmRenderers.registerRenderers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderers.registerRenderers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.monster.MonsterRenderers.registerRenderers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.water.WaterRenderers.registerRenderers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.golem.GolemRenderers.registerRenderers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.rare.RareRenderers.registerRenderers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.nether.NetherRenderers.registerRenderers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.end.EndRenderers.registerRenderers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.creator.CreatorRenderers.registerRenderers(event);
        io.github.sirschrolli.kingdomsatwar.client.renderer.siege.SiegeRenderers.registerRenderers(event);
        event.registerEntityRenderer(ModEntities.WILD_BOAR.get(), WildBoarRenderer::new);
        event.registerEntityRenderer(ModEntities.DEER.get(), DeerRenderer::new);
        event.registerEntityRenderer(ModEntities.GOOSE.get(), GooseRenderer::new);
        event.registerEntityRenderer(ModEntities.SEAT.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.WILL_O_WISP.get(), WillOWispRenderer::new);
        event.registerEntityRenderer(ModEntities.FAE_WISP.get(), context -> new WillOWispRenderer(context, WillOWispRenderer.FAE_TEXTURE));
        event.registerEntityRenderer(ModEntities.FAIRY.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.fae.FairyRenderer::new);
        event.registerEntityRenderer(ModEntities.MUSHROOMLING.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.fae.MushroomlingRenderer::new);
        event.registerEntityRenderer(ModEntities.THORN_WIGHT.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.fae.ThornWightRenderer::new);
        event.registerEntityRenderer(ModEntities.THORN.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.fae.ThornProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.MOTH_QUEEN.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.fae.MothQueenRenderer::new);
        event.registerEntityRenderer(ModEntities.MOON_MOTH.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.fae.MoonMothRenderer::new);
        event.registerEntityRenderer(ModEntities.GLUTSCHMIED.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.nether.GlutschmiedRenderer::new);
        event.registerEntityRenderer(ModEntities.GLUTKLUMPEN.get(),
                context -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(context, 1.6f, true));
        event.registerEntityRenderer(ModEntities.MISS_MYSTIK.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.fae.MissMystikRenderer::new);
        event.registerEntityRenderer(ModEntities.MYSTIK_IMAGE.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.fae.MystikImageRenderer::new);
        event.registerEntityRenderer(ModEntities.MAGIC_CARD.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.fae.MagicCardRenderer::new);
        event.registerEntityRenderer(ModEntities.LIGHT_DOVE.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.fae.LightDoveRenderer::new);
        event.registerEntityRenderer(ModEntities.MAGIC_HAT.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.fae.MagicHatRenderer::new);
        event.registerEntityRenderer(ModEntities.MAGIC_SWORD.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.fae.MagicSwordRenderer::new);
        event.registerEntityRenderer(ModEntities.ANNI_TROLL.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.glitch.AnniTrollRenderer::new);
        event.registerEntityRenderer(ModEntities.GLITCH_CLONE.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.glitch.GlitchCloneRenderer::new);
        event.registerEntityRenderer(ModEntities.PIXEL_CUBE.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.glitch.PixelCubeRenderer::new);
        event.registerEntityRenderer(ModEntities.MISSING_ZONE.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.glitch.MissingZoneRenderer::new);
        event.registerEntityRenderer(ModEntities.SCANLINE.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.glitch.ScanlineRenderer::new);
        event.registerEntityRenderer(ModEntities.AFTERIMAGE.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.glitch.AfterimageRenderer::new);
        event.registerEntityRenderer(ModEntities.ASTERIA.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.end.AsteriaRenderer::new);
        event.registerEntityRenderer(ModEntities.STARFALL.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.end.StarfallRenderer::new);
        event.registerEntityRenderer(ModEntities.COMET_LANCE.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.end.CometLanceRenderer::new);
        event.registerEntityRenderer(ModEntities.CAVE_TROLL.get(), CaveTrollRenderer::new);
        event.registerEntityRenderer(ModEntities.LINDWORM.get(), LindwormRenderer::new);
        event.registerEntityRenderer(ModEntities.WEREWOLF.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.monster.WerewolfRenderer::new);
        event.registerEntityRenderer(ModEntities.MIRE_SHAMBLER.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.monster.MireShamblerRenderer::new);
        event.registerEntityRenderer(ModEntities.EMBER_WYRM.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.monster.EmberWyrmRenderer::new);
        event.registerEntityRenderer(ModEntities.ASH_WRAITH.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.monster.AshWraithRenderer::new);
        event.registerEntityRenderer(ModEntities.SOUL_HERON.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.monster.SoulHeronRenderer::new);
        event.registerEntityRenderer(ModEntities.NIGHT_RAM.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.monster.NightRamRenderer::new);
        event.registerEntityRenderer(ModEntities.RIFT_HOUND.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.monster.RiftHoundRenderer::new);
        event.registerEntityRenderer(ModEntities.CHORUS_MAW.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.monster.ChorusMawRenderer::new);
        event.registerEntityRenderer(ModEntities.MONSTER_SHOT.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
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
        event.registerEntityRenderer(ModEntities.NYXAR.get(), context -> new DragonBossRenderer<>(context,
                new io.github.sirschrolli.kingdomsatwar.client.boss.NyxarModel(context.bakeLayer(ModModelLayers.NYXAR)),
                KingdomsAtWar.id("textures/entity/boss/nyxar.png"), KingdomsAtWar.id("textures/entity/boss/nyxar_glow.png"),
                2.3f, 4.8f, 5.0f));
        event.registerEntityRenderer(ModEntities.VINELING.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.creature.VinelingRenderer::new);
        event.registerEntityRenderer(ModEntities.BOSS_PROJECTILE.get(), BossProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.SEAL_BREAK.get(), io.github.sirschrolli.kingdomsatwar.client.story.SealBreakRenderer::new);
        event.registerEntityRenderer(ModEntities.INTRO_VISION.get(), io.github.sirschrolli.kingdomsatwar.client.intro.IntroVisionRenderer::new);
        // Kreaturen der Schattenwelt
        event.registerEntityRenderer(ModEntities.SHADE_STALKER.get(), ShadeStalkerRenderer::new);
        event.registerEntityRenderer(ModEntities.CRYSTAL_CRAWLER.get(), CrystalCrawlerRenderer::new);
        event.registerEntityRenderer(ModEntities.NIGHT_WING.get(), NightWingRenderer::new);
        event.registerEntityRenderer(ModEntities.MOON_STAG.get(), MoonStagRenderer::new);
        event.registerEntityRenderer(ModEntities.GLIMMER_MOTH.get(), GlimmerMothRenderer::new);
        // Wesen der Leere
        event.registerEntityRenderer(ModEntities.VOID_MAW.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm.VoidMawRenderer::new);
        event.registerEntityRenderer(ModEntities.SHARD_WARDEN.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm.ShardWardenRenderer::new);
        event.registerEntityRenderer(ModEntities.DISTORTLING.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm.DistortlingRenderer::new);
        event.registerEntityRenderer(ModEntities.STAR_RAY.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm.StarRayRenderer::new);
        event.registerEntityRenderer(ModEntities.WARDEN_SHARD.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm.WardenShardRenderer::new);
        event.registerEntityRenderer(ModEntities.SHATTERED_KING.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm.ShatteredKingRenderer::new);
        event.registerEntityRenderer(ModEntities.WORLD_EATER.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.abyss.WorldEaterRenderer::new);
        event.registerEntityRenderer(ModEntities.WORLD_CHUNK.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.abyss.WorldChunkRenderer::new);
        event.registerBlockEntityRenderer(io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.WORLD_EATER_TROPHY_ENTITY.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.abyss.WorldEaterTrophyRenderer::new);
        event.registerBlockEntityRenderer(io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.SEAL_SHARD_ENTITY.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.abyss.SealShardRenderer::new);
        event.registerEntityRenderer(ModEntities.MANTA_RAY.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.sea.MantaRayRenderer::new);
        event.registerEntityRenderer(ModEntities.GLOW_JELLYFISH.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.sea.GlowJellyfishRenderer::new);
        event.registerEntityRenderer(ModEntities.SHARK.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.sea.SharkRenderer::new);
        event.registerEntityRenderer(ModEntities.ANGLERFISH.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.sea.AnglerfishRenderer::new);
        event.registerEntityRenderer(ModEntities.GIANT_CRAB.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.sea.GiantCrabRenderer::new);
        event.registerEntityRenderer(ModEntities.REEF_COLOSSUS.get(), io.github.sirschrolli.kingdomsatwar.client.renderer.sea.ReefColossusRenderer::new);
        // die Umbrer
        event.registerEntityRenderer(ModEntities.UMBRER.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.umbrer.UmbrerRenderer<io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerEntity>::new);
        event.registerEntityRenderer(ModEntities.UMBRER_GUARD.get(),
                io.github.sirschrolli.kingdomsatwar.client.renderer.umbrer.UmbrerRenderer<io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerGuardEntity>::new);
    }

    @SubscribeEvent
    static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.BOSS_FLAME.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.FLAME));
        event.registerSpriteSet(ModParticles.BOSS_FROST.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.FROST));
        event.registerSpriteSet(ModParticles.BOSS_SPARK.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.SPARK));
        event.registerSpriteSet(ModParticles.BOSS_SPORE.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.SPORE));
        event.registerSpriteSet(ModParticles.BOSS_DUST.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.DUST));
        event.registerSpriteSet(ModParticles.BOSS_WATER.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.WATER));
        event.registerSpriteSet(ModParticles.BOSS_SHADOW.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.SHADOW));
        event.registerSpriteSet(ModParticles.PAT_HAND.get(), io.github.sirschrolli.kingdomsatwar.client.hero.PatHandParticle.Provider::new);
        event.registerSpriteSet(ModParticles.VENT_SMOKE.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.VENT_SMOKE));
        event.registerSpriteSet(ModParticles.SEA_GLOW.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.SEA_GLOW));
        event.registerSpriteSet(ModParticles.FAE_SPARK.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.FAE_SPARK));
        event.registerSpriteSet(ModParticles.CONFETTI.get(), io.github.sirschrolli.kingdomsatwar.client.fae.ConfettiParticle.Provider::new);
        event.registerSpriteSet(ModParticles.GLITCH_PIXEL.get(), io.github.sirschrolli.kingdomsatwar.client.glitch.GlitchPixelParticle.Provider::new);
        event.registerSpriteSet(ModParticles.DIVINE_SPARK.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.DIVINE_SPARK));
        event.registerSpriteSet(ModParticles.DIVINE_FEATHER.get(), sprites -> new BossParticle.Provider(sprites, BossParticle.DIVINE_FEATHER));
    }
}
