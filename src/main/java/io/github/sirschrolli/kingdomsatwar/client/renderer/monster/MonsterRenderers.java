package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.CitizenModel;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.BonesGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.CamelHuskGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.CreeperGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.PhantomGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.SilverfishGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.SlimeGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.SpiderGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.SulfurCubeGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.UndeadGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.UndeadHorseGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.WitchGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.BreezeGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.BreezeWindGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.CreakingGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.GuardianGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.IllagerGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.RavagerGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.VexGeometry;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.WardenGeometry;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.BoneHorse;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Boneman;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DrownedCorpse;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Mummy;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Revenant;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.UndeadHorse;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.WildCaveSpider;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.WildSpider;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Set;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jspecify.annotations.Nullable;

/**
 * Modell-Ebenen und Renderer der eigenen Monster (E4a und E4b). Die Zombie-Familie teilt sich ein Modell, ebenso die
 * Skelett-Familie und die Gesetzlosen; Jungtiere sind das erwachsene Modell mit großem Kopf (wie früher bei Vanilla),
 * samt passender Rüstung.
 */
public final class MonsterRenderers {
    public static final ModelLayerLocation UNDEAD = layer("undead");
    public static final ModelLayerLocation UNDEAD_BABY = layer("undead_baby");
    public static final ModelLayerLocation UNDEAD_CITIZEN_BABY = layer("undead_citizen_baby");
    public static final ArmorModelSet<ModelLayerLocation> HUMANOID_BABY_ARMOR = new ArmorModelSet<>(
            new ModelLayerLocation(KingdomsAtWar.id("humanoid_baby_armor"), "helmet"),
            new ModelLayerLocation(KingdomsAtWar.id("humanoid_baby_armor"), "chestplate"),
            new ModelLayerLocation(KingdomsAtWar.id("humanoid_baby_armor"), "leggings"),
            new ModelLayerLocation(KingdomsAtWar.id("humanoid_baby_armor"), "boots"));
    public static final ModelLayerLocation BONES = layer("bones");
    public static final ModelLayerLocation CREEPER = layer("creeper");
    public static final ModelLayerLocation SPIDER = layer("spider");
    public static final ModelLayerLocation CAVE_SPIDER = layer("cave_spider");
    public static final ModelLayerLocation SLIME = layer("slime");
    public static final ModelLayerLocation SLIME_OUTER = new ModelLayerLocation(KingdomsAtWar.id("slime"), "outer");
    public static final ModelLayerLocation SULFUR_CUBE = layer("sulfur_cube");
    public static final ModelLayerLocation SULFUR_CUBE_INNER = new ModelLayerLocation(KingdomsAtWar.id("sulfur_cube"), "inner");
    public static final ModelLayerLocation SULFUR_CUBE_SMALL = new ModelLayerLocation(KingdomsAtWar.id("sulfur_cube_small"), "main");
    public static final ModelLayerLocation SULFUR_CUBE_SMALL_INNER = new ModelLayerLocation(KingdomsAtWar.id("sulfur_cube_small"), "inner");
    public static final ModelLayerLocation WITCH = layer("witch");
    public static final ModelLayerLocation PHANTOM = layer("phantom");
    public static final ModelLayerLocation SILVERFISH = layer("silverfish");
    public static final ModelLayerLocation UNDEAD_HORSE = layer("undead_horse");
    public static final ModelLayerLocation CAMEL_HUSK = layer("camel_husk");
    // übrige Monster (E4b)
    public static final ModelLayerLocation ILLAGER = layer("illager");
    public static final ModelLayerLocation VEX = layer("vex");
    public static final ModelLayerLocation RAVAGER = layer("ravager");
    public static final ModelLayerLocation GUARDIAN = layer("guardian");
    public static final ModelLayerLocation ELDER_GUARDIAN = layer("elder_guardian");
    public static final ModelLayerLocation BREEZE = layer("breeze");
    public static final ModelLayerLocation BREEZE_WIND = new ModelLayerLocation(KingdomsAtWar.id("breeze"), "wind");
    public static final ModelLayerLocation CREAKING = layer("creaking");
    public static final ModelLayerLocation WARDEN = layer("warden");

    /** Der uralte Riffwächter ist der Riffwächter in groß (wie bei Vanilla). */
    private static final float ELDER_GUARDIAN_SCALE = 2.35f;

    /** Pferde der Mod sind wie die lebenden Vanilla-Pferde etwas größer gezeichnet als ihr Kasten. */
    private static final float HORSE_SCALE = 1.1f;
    private static final float CAVE_SPIDER_SCALE = 0.7f;

    static final String[] ZOMBIE_NAMES = {"zombie_bauer", "zombie_soeldner", "zombie_moench"};
    static final String[] HUSK_NAMES = {"husk_mumie", "husk_wuestling"};
    static final String[] DROWNED_NAMES = {"drowned_matrose", "drowned_fischer"};
    static final String[] SKELETON_NAMES = {"gerippe_knochen", "gerippe_ritter", "gerippe_jaeger"};
    static final String[] SPIDER_NAMES = {"spinne_wald", "spinne_wueste", "spinne_dschungel", "spinne_schnee"};

    private MonsterRenderers() {}

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(KingdomsAtWar.id(name), "main");
    }

    /** Bilder {@code textures/entity/<ordner>/<name>.png}. */
    static Identifier[] textures(String folder, String[] names) {
        Identifier[] out = new Identifier[names.length];
        for (int i = 0; i < names.length; i++) {
            out[i] = KingdomsAtWar.id("textures/entity/" + folder + "/" + names[i] + ".png");
        }
        return out;
    }

    /** Leucht-Bilder {@code <name>_glow.png}, soweit es sie gibt (sonst {@code null}). */
    static @Nullable RenderType[] glows(String folder, String[] names) {
        RenderType[] out = new RenderType[names.length];
        for (int i = 0; i < names.length; i++) {
            String path = "textures/entity/" + folder + "/" + names[i] + "_glow.png";
            if (MonsterRenderers.class.getResource("/assets/" + KingdomsAtWar.MODID + "/" + path) != null) {
                out[i] = RenderTypes.eyes(KingdomsAtWar.id(path));
            }
        }
        return out;
    }

    private static LayerDefinition parts(MeshDefinition mesh, int width, int height, String... keep) {
        mesh.getRoot().retainPartsAndChildren(Set.of(keep));
        return LayerDefinition.create(mesh, width, height);
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(UNDEAD, UndeadGeometry::createLayer);
        event.registerLayerDefinition(UNDEAD_BABY, () -> UndeadGeometry.createLayer().apply(HumanoidModel.BABY_TRANSFORMER));
        event.registerLayerDefinition(UNDEAD_CITIZEN_BABY, () -> CitizenModel.createLayer().apply(HumanoidModel.BABY_TRANSFORMER));
        ArmorModelSet<LayerDefinition> babyArmor = HumanoidModel.createArmorMeshSet(LayerDefinitions.INNER_ARMOR_DEFORMATION,
                LayerDefinitions.OUTER_ARMOR_DEFORMATION).map(mesh -> LayerDefinition.create(mesh, 64, 32).apply(HumanoidModel.BABY_TRANSFORMER));
        event.registerLayerDefinition(HUMANOID_BABY_ARMOR.head(), babyArmor::head);
        event.registerLayerDefinition(HUMANOID_BABY_ARMOR.chest(), babyArmor::chest);
        event.registerLayerDefinition(HUMANOID_BABY_ARMOR.legs(), babyArmor::legs);
        event.registerLayerDefinition(HUMANOID_BABY_ARMOR.feet(), babyArmor::feet);
        event.registerLayerDefinition(BONES, BonesGeometry::createLayer);
        event.registerLayerDefinition(CREEPER, CreeperGeometry::createLayer);
        event.registerLayerDefinition(SPIDER, SpiderGeometry::createLayer);
        event.registerLayerDefinition(CAVE_SPIDER, () -> SpiderGeometry.createLayer().apply(MeshTransformer.scaling(CAVE_SPIDER_SCALE)));
        event.registerLayerDefinition(SLIME, () -> parts(SlimeGeometry.createMesh(), SlimeGeometry.TEXTURE_WIDTH, SlimeGeometry.TEXTURE_HEIGHT,
                "cube", "right_eye", "left_eye", "mouth"));
        event.registerLayerDefinition(SLIME_OUTER, () -> parts(SlimeGeometry.createMesh(), SlimeGeometry.TEXTURE_WIDTH,
                SlimeGeometry.TEXTURE_HEIGHT, "outer"));
        int sw = SulfurCubeGeometry.TEXTURE_WIDTH;
        int sh = SulfurCubeGeometry.TEXTURE_HEIGHT;
        event.registerLayerDefinition(SULFUR_CUBE, () -> parts(SulfurCubeGeometry.createMesh(), sw, sh, "cube"));
        event.registerLayerDefinition(SULFUR_CUBE_INNER, () -> parts(SulfurCubeGeometry.createMesh(), sw, sh, "inner"));
        event.registerLayerDefinition(SULFUR_CUBE_SMALL, () -> parts(SulfurCubeGeometry.createMesh(), sw, sh, "small"));
        event.registerLayerDefinition(SULFUR_CUBE_SMALL_INNER, () -> parts(SulfurCubeGeometry.createMesh(), sw, sh, "small_inner"));
        event.registerLayerDefinition(WITCH, WitchGeometry::createLayer);
        event.registerLayerDefinition(PHANTOM, PhantomGeometry::createLayer);
        event.registerLayerDefinition(SILVERFISH, SilverfishGeometry::createLayer);
        event.registerLayerDefinition(UNDEAD_HORSE, () -> UndeadHorseGeometry.createLayer().apply(MeshTransformer.scaling(HORSE_SCALE)));
        event.registerLayerDefinition(CAMEL_HUSK, CamelHuskGeometry::createLayer);
        event.registerLayerDefinition(ILLAGER, IllagerGeometry::createLayer);
        event.registerLayerDefinition(VEX, VexGeometry::createLayer);
        event.registerLayerDefinition(RAVAGER, RavagerGeometry::createLayer);
        event.registerLayerDefinition(GUARDIAN, GuardianGeometry::createLayer);
        event.registerLayerDefinition(ELDER_GUARDIAN, () -> GuardianGeometry.createLayer().apply(MeshTransformer.scaling(ELDER_GUARDIAN_SCALE)));
        event.registerLayerDefinition(BREEZE, BreezeGeometry::createLayer);
        event.registerLayerDefinition(BREEZE_WIND, BreezeWindGeometry::createLayer);
        event.registerLayerDefinition(CREAKING, CreakingGeometry::createLayer);
        event.registerLayerDefinition(WARDEN, WardenGeometry::createLayer);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ZOMBIE.get(),
                context -> new RevenantRenderer<Revenant>(context, ZOMBIE_NAMES, z -> z.getLook().ordinal(), 1.0f, false));
        event.registerEntityRenderer(ModEntities.HUSK.get(),
                context -> new RevenantRenderer<Mummy>(context, HUSK_NAMES, z -> z.getLook().ordinal(), 1.0625f, false));
        event.registerEntityRenderer(ModEntities.DROWNED.get(),
                context -> new RevenantRenderer<DrownedCorpse>(context, DROWNED_NAMES, z -> z.getLook().ordinal(), 1.0f, true));
        event.registerEntityRenderer(ModEntities.ZOMBIE_VILLAGER.get(), UndeadVillagerRenderer::new);
        event.registerEntityRenderer(ModEntities.SKELETON.get(),
                context -> new BonesRenderer<Boneman>(context, SKELETON_NAMES, s -> s.getLook().ordinal()));
        event.registerEntityRenderer(ModEntities.STRAY.get(), context -> new BonesRenderer<>(context, new String[] {"frostgebein"}, s -> 0));
        event.registerEntityRenderer(ModEntities.BOGGED.get(), context -> new BonesRenderer<>(context, new String[] {"moorgebein"}, s -> 0));
        event.registerEntityRenderer(ModEntities.PARCHED.get(), context -> new BonesRenderer<>(context, new String[] {"doerrgebein"}, s -> 0));
        event.registerEntityRenderer(ModEntities.CREEPER.get(), PlantCreeperRenderer::new);
        event.registerEntityRenderer(ModEntities.SPIDER.get(),
                context -> new WildSpiderRenderer<WildSpider>(context, SPIDER, 0.8f, SPIDER_NAMES, s -> s.getLook().ordinal()));
        event.registerEntityRenderer(ModEntities.CAVE_SPIDER.get(), context -> new WildSpiderRenderer<WildCaveSpider>(context, CAVE_SPIDER,
                0.8f * CAVE_SPIDER_SCALE, new String[] {"hoehlenspinne"}, s -> 0));
        event.registerEntityRenderer(ModEntities.SLIME.get(), OozeRenderer::new);
        event.registerEntityRenderer(ModEntities.SULFUR_CUBE.get(), BrimstoneCubeRenderer::new);
        event.registerEntityRenderer(ModEntities.WITCH.get(), HagRenderer::new);
        event.registerEntityRenderer(ModEntities.PHANTOM.get(), NightmareRenderer::new);
        event.registerEntityRenderer(ModEntities.SILVERFISH.get(), SilverlingRenderer::new);
        event.registerEntityRenderer(ModEntities.ZOMBIE_HORSE.get(), context -> new UndeadEquineRenderer<UndeadHorse>(context, "zombiepferd_",
                UndeadHorse.Look.values(), horse -> horse.getLook().ordinal(), EquipmentClientInfo.LayerType.ZOMBIE_HORSE_SADDLE, true));
        event.registerEntityRenderer(ModEntities.SKELETON_HORSE.get(), context -> new UndeadEquineRenderer<BoneHorse>(context, "skelettpferd_",
                BoneHorse.Look.values(), horse -> horse.getLook().ordinal(), EquipmentClientInfo.LayerType.SKELETON_HORSE_SADDLE, false));
        event.registerEntityRenderer(ModEntities.CAMEL_HUSK.get(), DryCamelRenderer::new);
        // übrige Monster (E4b)
        event.registerEntityRenderer(ModEntities.PILLAGER.get(), HighwaymanRenderer::new);
        event.registerEntityRenderer(ModEntities.VINDICATOR.get(), ButcherRenderer::new);
        event.registerEntityRenderer(ModEntities.EVOKER.get(), ConjurerRenderer::new);
        event.registerEntityRenderer(ModEntities.ILLUSIONER.get(), TricksterRenderer::new);
        event.registerEntityRenderer(ModEntities.VEX.get(), TormentorRenderer::new);
        event.registerEntityRenderer(ModEntities.RAVAGER.get(), DevastatorRenderer::new);
        event.registerEntityRenderer(ModEntities.GUARDIAN.get(), context -> new ReefGuardianRenderer(context, 0.5f, GUARDIAN, "riffwaechter"));
        event.registerEntityRenderer(ModEntities.ELDER_GUARDIAN.get(),
                context -> new ReefGuardianRenderer(context, 1.2f, ELDER_GUARDIAN, "riffwaechter_alt"));
        event.registerEntityRenderer(ModEntities.BREEZE.get(), WindSpiritRenderer::new);
        event.registerEntityRenderer(ModEntities.CREAKING.get(), GnarlRenderer::new);
        event.registerEntityRenderer(ModEntities.WARDEN.get(), DeepWardenRenderer::new);
    }
}
