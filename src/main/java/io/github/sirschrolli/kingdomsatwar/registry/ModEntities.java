package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.entity.shadow.CrystalCrawlerEntity;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.GlimmerMothEntity;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.MoonStagEntity;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.NightWingEntity;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.ShadeStalkerEntity;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.SeatEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile;
import io.github.sirschrolli.kingdomsatwar.entity.boss.fire.PyrrhonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.CaveTrollEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.DeerEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.GooseEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.LindwormEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.WildBoarEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.WillOWispEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.Cattle;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmChicken;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmDonkey;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmGoat;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmHorse;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmMule;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmPig;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmSheep;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.Hare;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.Bear;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.HouseCat;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildAnimals;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildArmadillo;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildBat;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildBee;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildCamel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildFox;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildLlama;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildPanda;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildParrot;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildTraderLlama;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildWolf;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.Wildcat;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.BanditEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Alle Lebewesen der Mod.
 */
public final class ModEntities {
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(KingdomsAtWar.MODID);

    /** Ein Drachen-Typ je Drachenart (gleiche Klasse, eigene Texturen und Fähigkeiten). */
    public static final Map<DragonType, DeferredHolder<EntityType<?>, EntityType<DragonEntity>>> DRAGONS = new EnumMap<>(DragonType.class);

    /** Ein Typ je Soldatenart (gleiche Klasse, eigene Werte, Ausrüstung und Aussehen). */
    public static final Map<SoldierType, DeferredHolder<EntityType<?>, EntityType<SoldierEntity>>> SOLDIERS = new EnumMap<>(SoldierType.class);

    static {
        for (DragonType type : DragonType.values()) {
            DRAGONS.put(type, ENTITIES.registerEntityType(type.id() + "_dragon",
                    (entityType, level) -> new DragonEntity(entityType, level, type), MobCategory.CREATURE,
                    builder -> {
                        builder.sized(2.2f, 2.1f).eyeHeight(1.9f).clientTrackingRange(12);
                        return type == DragonType.FIRE ? builder.fireImmune() : builder;
                    }));
        }
        for (SoldierType type : SoldierType.values()) {
            SOLDIERS.put(type, ENTITIES.registerEntityType(type.id(),
                    (entityType, level) -> new SoldierEntity(entityType, level, type), MobCategory.MISC,
                    builder -> builder.sized(0.6f, 1.95f).eyeHeight(1.62f).ridingOffset(-0.7f).clientTrackingRange(10)));
        }
    }

    public static final DeferredHolder<EntityType<?>, EntityType<BanditEntity>> BANDIT = ENTITIES.registerEntityType("bandit",
            (entityType, level) -> new BanditEntity(entityType, level, false), MobCategory.MONSTER,
            builder -> builder.sized(0.6f, 1.95f).eyeHeight(1.62f).ridingOffset(-0.7f).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<BanditEntity>> BANDIT_ARCHER = ENTITIES.registerEntityType("bandit_archer",
            (entityType, level) -> new BanditEntity(entityType, level, true), MobCategory.MONSTER,
            builder -> builder.sized(0.6f, 1.95f).eyeHeight(1.62f).ridingOffset(-0.7f).clientTrackingRange(8));

    // ---------------------------------------------------------------- Bürger der eigenen Siedlung
    /** Bürger: am Stadtbanner angeheuert, mit Beruf, Bett und Werkstatt. */
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.economy.citizen.CitizenEntity>> CITIZEN =
            ENTITIES.registerEntityType("citizen", io.github.sirschrolli.kingdomsatwar.economy.citizen.CitizenEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.6f, 1.95f).eyeHeight(1.62f).ridingOffset(-0.7f).clientTrackingRange(10));
    /** Dorfbewohner: leben in den Dörfern der Welt, mit Beruf, Bett, Arbeitsplatz und Tagesablauf. */
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity>> VILLAGER =
            ENTITIES.registerEntityType("villager", io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.6f, 1.95f).eyeHeight(1.62f).ridingOffset(-0.7f).clientTrackingRange(10));

    // ---------------------------------------------------------------- Eigene Hof- und Weidetiere (ersetzen Vanilla)
    public static final DeferredHolder<EntityType<?>, EntityType<Cattle>> CATTLE = ENTITIES.registerEntityType("cattle",
            Cattle::new, MobCategory.CREATURE,
            builder -> builder.sized(0.9f, 1.4f).eyeHeight(1.3f).passengerAttachments(1.36875f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<FarmPig>> FARM_PIG = ENTITIES.registerEntityType("pig",
            FarmPig::new, MobCategory.CREATURE, builder -> builder.sized(0.9f, 0.9f).passengerAttachments(0.86875f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<FarmSheep>> FARM_SHEEP = ENTITIES.registerEntityType("sheep",
            FarmSheep::new, MobCategory.CREATURE,
            builder -> builder.sized(0.9f, 1.3f).eyeHeight(1.235f).passengerAttachments(1.2375f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<FarmChicken>> FARM_CHICKEN = ENTITIES.registerEntityType("chicken",
            FarmChicken::new, MobCategory.CREATURE,
            builder -> builder.sized(0.4f, 0.7f).eyeHeight(0.644f).passengerAttachments(new Vec3(0.0, 0.7, -0.1)).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<FarmHorse>> FARM_HORSE = ENTITIES.registerEntityType("horse",
            FarmHorse::new, MobCategory.CREATURE,
            builder -> builder.sized(1.3964844f, 1.6f).eyeHeight(1.52f).passengerAttachments(1.44375f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<FarmDonkey>> FARM_DONKEY = ENTITIES.registerEntityType("donkey",
            FarmDonkey::new, MobCategory.CREATURE,
            builder -> builder.sized(1.3964844f, 1.5f).eyeHeight(1.425f).passengerAttachments(1.1125f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<FarmMule>> FARM_MULE = ENTITIES.registerEntityType("mule",
            FarmMule::new, MobCategory.CREATURE,
            builder -> builder.sized(1.3964844f, 1.6f).eyeHeight(1.52f).passengerAttachments(1.2125f).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<FarmGoat>> FARM_GOAT = ENTITIES.registerEntityType("goat",
            FarmGoat::new, MobCategory.CREATURE, builder -> builder.sized(0.9f, 1.3f).passengerAttachments(1.1125f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<Hare>> HARE = ENTITIES.registerEntityType("hare",
            Hare::new, MobCategory.CREATURE, builder -> builder.sized(0.5f, 0.6f).eyeHeight(0.59f).clientTrackingRange(8));

    // ---------------------------------------------------------------- Eigene Wildtiere (ersetzen die Vanilla-Wildtiere)
    public static final DeferredHolder<EntityType<?>, EntityType<WildWolf>> WOLF = ENTITIES.registerEntityType("wolf",
            WildWolf::new, MobCategory.CREATURE,
            builder -> builder.sized(0.6f, 0.85f).eyeHeight(0.68f).passengerAttachments(new Vec3(0.0, 0.81875, -0.0625)).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<WildFox>> FOX = ENTITIES.registerEntityType("fox",
            WildFox::new, MobCategory.CREATURE, builder -> builder.sized(0.6f, 0.7f).eyeHeight(0.4f)
                    .passengerAttachments(new Vec3(0.0, 0.6375, -0.25)).clientTrackingRange(8).immuneTo(BlockTags.FOX_IMMUNE_TO));
    public static final DeferredHolder<EntityType<?>, EntityType<Bear>> BEAR = ENTITIES.registerEntityType("bear",
            Bear::new, MobCategory.CREATURE, builder -> builder.immuneTo(BlockTags.POLAR_BEAR_IMMUNE_TO).sized(1.4f, 1.4f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<WildPanda>> PANDA = ENTITIES.registerEntityType("panda",
            WildPanda::new, MobCategory.CREATURE, builder -> builder.sized(1.3f, 1.25f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<HouseCat>> CAT = ENTITIES.registerEntityType("cat",
            HouseCat::new, MobCategory.CREATURE,
            builder -> builder.sized(0.6f, 0.7f).eyeHeight(0.35f).passengerAttachments(0.5125f).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<Wildcat>> WILDCAT = ENTITIES.registerEntityType("wildcat",
            Wildcat::new, MobCategory.CREATURE, builder -> builder.sized(0.6f, 0.7f).passengerAttachments(0.6375f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<WildParrot>> PARROT = ENTITIES.registerEntityType("parrot",
            WildParrot::new, MobCategory.CREATURE,
            builder -> builder.sized(0.5f, 0.9f).eyeHeight(0.54f).passengerAttachments(0.4625f).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<WildBee>> BEE = ENTITIES.registerEntityType("bee",
            WildBee::new, MobCategory.CREATURE, builder -> builder.sized(0.55f, 0.5f).eyeHeight(0.3f).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<WildBat>> BAT = ENTITIES.registerEntityType("bat",
            WildBat::new, MobCategory.AMBIENT, builder -> builder.sized(0.5f, 0.9f).eyeHeight(0.45f).clientTrackingRange(5).dontTrackDeltas());
    public static final DeferredHolder<EntityType<?>, EntityType<WildArmadillo>> ARMADILLO = ENTITIES.registerEntityType("armadillo",
            WildArmadillo::new, MobCategory.CREATURE, builder -> builder.sized(0.7f, 0.65f).eyeHeight(0.26f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<WildCamel>> CAMEL = ENTITIES.registerEntityType("camel",
            WildCamel::new, MobCategory.CREATURE, builder -> builder.sized(1.7f, 2.375f).eyeHeight(2.275f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<WildLlama>> LLAMA = ENTITIES.registerEntityType("llama",
            WildLlama::new, MobCategory.CREATURE, builder -> builder.sized(0.9f, 1.87f).eyeHeight(1.7765f)
                    .passengerAttachments(new Vec3(0.0, 1.37, -0.3)).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<WildTraderLlama>> TRADER_LLAMA = ENTITIES.registerEntityType("trader_llama",
            WildTraderLlama::new, MobCategory.CREATURE, builder -> builder.sized(0.9f, 1.87f).eyeHeight(1.7765f)
                    .passengerAttachments(new Vec3(0.0, 1.37, -0.3)).clientTrackingRange(10));

    // ---------------------------------------------------------------- Eigene Monster (ersetzen die Vanilla-Monster, E4a)
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Revenant>> ZOMBIE = ENTITIES.registerEntityType("zombie",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Revenant::new, MobCategory.MONSTER, builder -> builder.sized(0.6f, 1.95f).eyeHeight(1.74f).passengerAttachments(2.0125f)
                    .ridingOffset(-0.7f).clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Mummy>> HUSK = ENTITIES.registerEntityType("husk",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Mummy::new, MobCategory.MONSTER, builder -> builder.sized(0.6f, 1.95f).eyeHeight(1.74f).passengerAttachments(2.075f)
                    .ridingOffset(-0.7f).clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DrownedCorpse>> DROWNED = ENTITIES.registerEntityType("drowned",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DrownedCorpse::new, MobCategory.MONSTER, builder -> builder.sized(0.6f, 1.95f).eyeHeight(1.74f).passengerAttachments(2.0125f)
                    .ridingOffset(-0.7f).clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.UndeadVillager>> ZOMBIE_VILLAGER = ENTITIES.registerEntityType(
            "zombie_villager", io.github.sirschrolli.kingdomsatwar.entity.creature.monster.UndeadVillager::new, MobCategory.MONSTER, builder -> builder.sized(0.6f, 1.95f).passengerAttachments(2.125f)
                    .ridingOffset(-0.7f).eyeHeight(1.74f).clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Boneman>> SKELETON = ENTITIES.registerEntityType("skeleton",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Boneman::new, MobCategory.MONSTER, builder -> builder.sized(0.6f, 1.99f).eyeHeight(1.74f).ridingOffset(-0.7f)
                    .clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.FrostBones>> STRAY = ENTITIES.registerEntityType("stray",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.FrostBones::new, MobCategory.MONSTER, builder -> builder.sized(0.6f, 1.99f).eyeHeight(1.74f).ridingOffset(-0.7f)
                    .immuneTo(BlockTags.STRAY_IMMUNE_TO).clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.BogBones>> BOGGED = ENTITIES.registerEntityType("bogged",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.BogBones::new, MobCategory.MONSTER, builder -> builder.sized(0.6f, 1.99f).eyeHeight(1.74f).ridingOffset(-0.7f)
                    .clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DryBones>> PARCHED = ENTITIES.registerEntityType("parched",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DryBones::new, MobCategory.MONSTER, builder -> builder.sized(0.6f, 1.99f).eyeHeight(1.74f).ridingOffset(-0.7f)
                    .clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.PlantCreeper>> CREEPER = ENTITIES.registerEntityType("creeper",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.PlantCreeper::new, MobCategory.MONSTER, builder -> builder.sized(0.6f, 1.7f).clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.WildSpider>> SPIDER = ENTITIES.registerEntityType("spider",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.WildSpider::new, MobCategory.MONSTER, builder -> builder.sized(1.4f, 0.9f).eyeHeight(0.65f).passengerAttachments(0.765f)
                    .clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.WildCaveSpider>> CAVE_SPIDER = ENTITIES.registerEntityType(
            "cave_spider", io.github.sirschrolli.kingdomsatwar.entity.creature.monster.WildCaveSpider::new, MobCategory.MONSTER, builder -> builder.sized(0.7f, 0.5f).eyeHeight(0.45f)
                    .clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Ooze>> SLIME = ENTITIES.registerEntityType("slime",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Ooze::new, MobCategory.MONSTER, builder -> builder.sized(0.52f, 0.52f).eyeHeight(0.325f).spawnDimensionsScale(4.0f)
                    .clientTrackingRange(10).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.BrimstoneCube>> SULFUR_CUBE = ENTITIES.registerEntityType(
            "sulfur_cube", io.github.sirschrolli.kingdomsatwar.entity.creature.monster.BrimstoneCube::new, MobCategory.MONSTER, builder -> builder.sized(0.49f, 0.49f).eyeHeight(0.175f)
                    .spawnDimensionsScale(2.0f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Hag>> WITCH = ENTITIES.registerEntityType("witch",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Hag::new, MobCategory.MONSTER, builder -> builder.sized(0.6f, 1.95f).eyeHeight(1.62f).passengerAttachments(2.2625f)
                    .clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Nightmare>> PHANTOM = ENTITIES.registerEntityType("phantom",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Nightmare::new, MobCategory.MONSTER, builder -> builder.sized(0.9f, 0.5f).eyeHeight(0.175f).passengerAttachments(0.3375f)
                    .ridingOffset(-0.125f).clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Silverling>> SILVERFISH = ENTITIES.registerEntityType("silverfish",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Silverling::new, MobCategory.MONSTER, builder -> builder.sized(0.4f, 0.3f).eyeHeight(0.13f).passengerAttachments(0.2375f)
                    .clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.UndeadHorse>> ZOMBIE_HORSE = ENTITIES.registerEntityType("zombie_horse",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.UndeadHorse::new, MobCategory.MONSTER, builder -> builder.sized(1.3964844f, 1.6f).eyeHeight(1.52f).passengerAttachments(1.31875f)
                    .clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.BoneHorse>> SKELETON_HORSE = ENTITIES.registerEntityType(
            "skeleton_horse", io.github.sirschrolli.kingdomsatwar.entity.creature.monster.BoneHorse::new, MobCategory.CREATURE, builder -> builder.sized(1.3964844f, 1.6f).eyeHeight(1.52f)
                    .passengerAttachments(1.31875f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DryCamel>> CAMEL_HUSK = ENTITIES.registerEntityType("camel_husk",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DryCamel::new, MobCategory.MONSTER, builder -> builder.sized(1.7f, 2.375f).eyeHeight(2.275f).clientTrackingRange(10));

    // ---------------------------------------------------------------- Übrige Monster (E4b): Gesetzlose, Verwüster, Wächter, Windgeist, Knorz, Tiefenhüter
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Highwayman>> PILLAGER = ENTITIES.registerEntityType("pillager",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Highwayman::new, MobCategory.MONSTER, builder -> builder.canSpawnFarFromPlayer().sized(0.6f, 1.95f)
                    .passengerAttachments(2.0f).ridingOffset(-0.6f).clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Butcher>> VINDICATOR = ENTITIES.registerEntityType("vindicator",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Butcher::new, MobCategory.MONSTER, builder -> builder.sized(0.6f, 1.95f).passengerAttachments(2.0f).ridingOffset(-0.6f)
                    .clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Conjurer>> EVOKER = ENTITIES.registerEntityType("evoker",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Conjurer::new, MobCategory.MONSTER, builder -> builder.sized(0.6f, 1.95f).passengerAttachments(2.0f).ridingOffset(-0.6f)
                    .clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Trickster>> ILLUSIONER = ENTITIES.registerEntityType("illusioner",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Trickster::new, MobCategory.MONSTER, builder -> builder.sized(0.6f, 1.95f).passengerAttachments(2.0f).ridingOffset(-0.6f)
                    .clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Tormentor>> VEX = ENTITIES.registerEntityType("vex",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Tormentor::new, MobCategory.MONSTER, builder -> builder.fireImmune().sized(0.4f, 0.8f).eyeHeight(0.51875f)
                    .passengerAttachments(0.7375f).ridingOffset(0.04f).clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Devastator>> RAVAGER = ENTITIES.registerEntityType("ravager",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Devastator::new, MobCategory.MONSTER, builder -> builder.sized(1.95f, 2.2f)
                    .passengerAttachments(new net.minecraft.world.phys.Vec3(0.0, 2.2625, -0.0625)).clientTrackingRange(10).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.ReefGuardian>> GUARDIAN = ENTITIES.registerEntityType("guardian",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.ReefGuardian::new, MobCategory.MONSTER, builder -> builder.sized(0.85f, 0.85f).eyeHeight(0.425f).passengerAttachments(0.975f)
                    .clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.ElderReefGuardian>> ELDER_GUARDIAN = ENTITIES.registerEntityType(
            "elder_guardian", io.github.sirschrolli.kingdomsatwar.entity.creature.monster.ElderReefGuardian::new, MobCategory.MONSTER, builder -> builder.sized(1.9975f, 1.9975f)
                    .eyeHeight(0.99875f).passengerAttachments(2.350625f).clientTrackingRange(10).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.WindSpirit>> BREEZE = ENTITIES.registerEntityType("breeze",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.WindSpirit::new, MobCategory.MONSTER, builder -> builder.sized(0.6f, 1.77f).eyeHeight(1.3452f).clientTrackingRange(10)
                    .notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Gnarl>> CREAKING = ENTITIES.registerEntityType("creaking",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Gnarl::new, MobCategory.MONSTER, builder -> builder.sized(0.9f, 2.7f).eyeHeight(2.3f).clientTrackingRange(8).notInPeaceful());
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DeepWarden>> WARDEN = ENTITIES.registerEntityType("warden",
            io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DeepWarden::new, MobCategory.MONSTER, builder -> builder.sized(0.9f, 2.9f).passengerAttachments(3.15f)
                    .attach(net.minecraft.world.entity.EntityAttachment.WARDEN_CHEST, 0.0f, 1.6f, 0.0f).clientTrackingRange(16).fireImmune()
                    .notInPeaceful());

    // ---------------------------------------------------------------- Wilde Kreaturen
    public static final DeferredHolder<EntityType<?>, EntityType<WildBoarEntity>> WILD_BOAR = ENTITIES.registerEntityType("wild_boar",
            WildBoarEntity::new, MobCategory.CREATURE, builder -> builder.sized(0.9f, 0.9f).eyeHeight(0.6f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<DeerEntity>> DEER = ENTITIES.registerEntityType("deer",
            DeerEntity::new, MobCategory.CREATURE, builder -> builder.sized(0.9f, 1.4f).eyeHeight(1.3f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<GooseEntity>> GOOSE = ENTITIES.registerEntityType("goose",
            GooseEntity::new, MobCategory.CREATURE, builder -> builder.sized(0.5f, 0.9f).eyeHeight(0.8f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<WillOWispEntity>> WILL_O_WISP = ENTITIES.registerEntityType("will_o_wisp",
            WillOWispEntity::new, MobCategory.AMBIENT,
            builder -> builder.sized(0.45f, 0.45f).eyeHeight(0.22f).fireImmune().clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<CaveTrollEntity>> CAVE_TROLL = ENTITIES.registerEntityType("cave_troll",
            CaveTrollEntity::new, MobCategory.MONSTER, builder -> builder.sized(1.4f, 2.9f).eyeHeight(2.45f).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<LindwormEntity>> LINDWORM = ENTITIES.registerEntityType("lindworm",
            LindwormEntity::new, MobCategory.MONSTER, builder -> builder.sized(1.6f, 1.3f).eyeHeight(1.1f).clientTrackingRange(10));

    // ---------------------------------------------------------------- Kreaturen der Schattenwelt
    /** Schattenpirscher: unsichtbar, solange man ihn nicht ansieht. */
    public static final DeferredHolder<EntityType<?>, EntityType<ShadeStalkerEntity>> SHADE_STALKER = ENTITIES.registerEntityType("shade_stalker",
            ShadeStalkerEntity::new, MobCategory.MONSTER,
            builder -> builder.sized(0.7f, 2.5f).eyeHeight(2.2f).notInPeaceful().clientTrackingRange(10));
    /** Kristallkriecher: klettert an Wänden, Kristallsplitter auf dem Rücken. */
    public static final DeferredHolder<EntityType<?>, EntityType<CrystalCrawlerEntity>> CRYSTAL_CRAWLER = ENTITIES.registerEntityType(
            "crystal_crawler", CrystalCrawlerEntity::new, MobCategory.MONSTER,
            builder -> builder.sized(1.2f, 0.65f).eyeHeight(0.45f).notInPeaceful().clientTrackingRange(8));
    /** Nachtschwinge: Flugjäger über den Gipfeln und Inseln. */
    public static final DeferredHolder<EntityType<?>, EntityType<NightWingEntity>> NIGHT_WING = ENTITIES.registerEntityType("night_wing",
            NightWingEntity::new, MobCategory.MONSTER,
            builder -> builder.sized(1.0f, 0.55f).eyeHeight(0.2f).passengerAttachments(0.35f).ridingOffset(-0.125f).notInPeaceful()
                    .clientTrackingRange(10));
    /** Mondhirsch: friedlich, mit leuchtendem Kristallgeweih. */
    public static final DeferredHolder<EntityType<?>, EntityType<MoonStagEntity>> MOON_STAG = ENTITIES.registerEntityType("moon_stag",
            MoonStagEntity::new, MobCategory.CREATURE, builder -> builder.sized(0.9f, 1.4f).eyeHeight(1.3f).clientTrackingRange(10));
    /** Glimmerfalter: leuchtende Nachtfalter über den Wiesen. */
    public static final DeferredHolder<EntityType<?>, EntityType<GlimmerMothEntity>> GLIMMER_MOTH = ENTITIES.registerEntityType("glimmer_moth",
            GlimmerMothEntity::new, MobCategory.AMBIENT, builder -> builder.sized(0.5f, 0.4f).eyeHeight(0.2f).clientTrackingRange(8));

    // ---------------------------------------------------------------- Die Umbrer (Volk der Schattenwelt)
    /** Umbrer: Bewohner der Dörfer, tauschen gegen Mondsilber (Beruf je Tier). */
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerEntity>> UMBRER =
            ENTITIES.registerEntityType("umbrer", io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.6f, 2.15f).eyeHeight(1.9f).clientTrackingRange(10));
    /** Wächter der Umbrer: schützen die Dörfer. */
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerGuardEntity>> UMBRER_GUARD =
            ENTITIES.registerEntityType("umbrer_guard", io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerGuardEntity::new,
                    MobCategory.MISC, builder -> builder.sized(0.6f, 2.15f).eyeHeight(1.9f).clientTrackingRange(10));

    // ---------------------------------------------------------------- Drachen-Bosse
    /** Pyrrhon, der Aschenfürst (Feuer). */
    public static final DeferredHolder<EntityType<?>, EntityType<PyrrhonEntity>> PYRRHON = ENTITIES.registerEntityType("fire_dragon_boss",
            PyrrhonEntity::new, MobCategory.MONSTER,
            builder -> builder.sized(6.0f, 6.0f).eyeHeight(7.0f).fireImmune().clientTrackingRange(16).updateInterval(2));
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.boss.ice.GlaciaraEntity>> GLACIARA =
            ENTITIES.registerEntityType("ice_dragon_boss", io.github.sirschrolli.kingdomsatwar.entity.boss.ice.GlaciaraEntity::new, MobCategory.MONSTER,
                    builder -> builder.sized(5.0f, 6.0f).eyeHeight(8.0f).fireImmune()
                            .clientTrackingRange(16).updateInterval(2));
    /** Verdantis, die Hüterin des Hains (Natur). */
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.boss.nature.VerdantisEntity>> VERDANTIS =
            ENTITIES.registerEntityType("nature_dragon_boss", io.github.sirschrolli.kingdomsatwar.entity.boss.nature.VerdantisEntity::new,
                    MobCategory.MONSTER, builder -> builder.sized(6.0f, 5.5f).eyeHeight(6.5f).clientTrackingRange(16).updateInterval(2));
    /** Terragon, der Bergbrecher (Erde). */
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.boss.earth.TerragonEntity>> TERRAGON =
            ENTITIES.registerEntityType("earth_dragon_boss", io.github.sirschrolli.kingdomsatwar.entity.boss.earth.TerragonEntity::new,
                    MobCategory.MONSTER, builder -> builder.sized(6.5f, 5.0f).eyeHeight(5.5f).clientTrackingRange(16).updateInterval(2));
    /** Voltarion, der Sturmbringer (Blitz). */
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.boss.lightning.VoltarionEntity>> VOLTARION =
            ENTITIES.registerEntityType("lightning_dragon_boss", io.github.sirschrolli.kingdomsatwar.entity.boss.lightning.VoltarionEntity::new,
                    MobCategory.MONSTER, builder -> builder.sized(5.0f, 6.0f).eyeHeight(7.5f).fireImmune().clientTrackingRange(16).updateInterval(2));
    /** Thalassa, die Herrin der Tiefe (Wasser). */
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.boss.water.ThalassaEntity>> THALASSA =
            ENTITIES.registerEntityType("water_dragon_boss", io.github.sirschrolli.kingdomsatwar.entity.boss.water.ThalassaEntity::new,
                    MobCategory.MONSTER, builder -> builder.sized(4.5f, 3.5f).eyeHeight(3.0f).clientTrackingRange(16).updateInterval(2));
    /** Nyxar, der Schattendrache – Endgegner der Schattenwelt. */
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.boss.shadow.NyxarEntity>> NYXAR =
            ENTITIES.registerEntityType("shadow_dragon_boss", io.github.sirschrolli.kingdomsatwar.entity.boss.shadow.NyxarEntity::new,
                    MobCategory.MONSTER, builder -> builder.sized(6.0f, 6.5f).eyeHeight(8.0f).fireImmune().clientTrackingRange(16).updateInterval(2));
    /** Rankling: Diener von Verdantis (wird nicht gespeichert). */
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.boss.nature.VinelingEntity>> VINELING =
            ENTITIES.registerEntityType("vineling", io.github.sirschrolli.kingdomsatwar.entity.boss.nature.VinelingEntity::new,
                    MobCategory.MONSTER, builder -> builder.sized(0.8f, 1.6f).eyeHeight(1.35f).noSave().clientTrackingRange(10));
    /** Geschosse der Bosse (Feuerbälle, Meteore …). */
    public static final DeferredHolder<EntityType<?>, EntityType<BossProjectile>> BOSS_PROJECTILE = ENTITIES.registerEntityType("boss_projectile",
            BossProjectile::new, MobCategory.MISC,
            builder -> builder.sized(1.0f, 1.0f).fireImmune().noLootTable().clientTrackingRange(10).updateInterval(1));

    // ---------------------------------------------------------------- Erzmagier
    /** Ein Erzmagier je Element (wacht in der Spitze seines Turms). */
    public static final Map<DragonType, DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity>>>
            MAGES = new EnumMap<>(DragonType.class);

    static {
        for (DragonType type : DragonType.values()) {
            MAGES.put(type, ENTITIES.registerEntityType(type.id() + "_mage",
                    (entityType, level) -> new io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity(entityType, level, type),
                    MobCategory.MONSTER, builder -> {
                        builder.sized(0.9f, 2.6f).eyeHeight(2.3f).clientTrackingRange(12).updateInterval(2);
                        return type == DragonType.FIRE ? builder.fireImmune() : builder;
                    }));
        }
    }

    /** Spiegelbild eines Erzmagiers (wird nicht gespeichert). */
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageImageEntity>> MAGE_IMAGE =
            ENTITIES.registerEntityType("mage_image", io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageImageEntity::new,
                    MobCategory.MISC, builder -> builder.sized(0.9f, 2.6f).eyeHeight(2.3f).noSave().noLootTable().clientTrackingRange(10));

    /** Magisches Geschoss der Erzmagier. */
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MagicMissile>> MAGIC_MISSILE =
            ENTITIES.registerEntityType("magic_missile", io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MagicMissile::new,
                    MobCategory.MISC, builder -> builder.sized(0.5f, 0.5f).fireImmune().noLootTable().clientTrackingRange(10).updateInterval(1));

    /** Unsichtbarer Träger der Lichteffekte beim Siegelbruch. */
    public static final DeferredHolder<EntityType<?>, EntityType<io.github.sirschrolli.kingdomsatwar.entity.SealBreakEntity>> SEAL_BREAK =
            ENTITIES.registerEntityType("seal_break", io.github.sirschrolli.kingdomsatwar.entity.SealBreakEntity::new, MobCategory.MISC,
                    builder -> builder.sized(0.5f, 0.5f).fireImmune().noLootTable().noSave().clientTrackingRange(16).updateInterval(1));

    /** Der Boss einer Drachenart (null, solange er noch nicht gebaut ist). */
    public static @org.jspecify.annotations.Nullable EntityType<? extends io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity> bossType(
            DragonType element) {
        return switch (element) {
            case FIRE -> PYRRHON.get();
            case ICE -> GLACIARA.get();
            case NATURE -> VERDANTIS.get();
            case EARTH -> TERRAGON.get();
            case LIGHTNING -> VOLTARION.get();
            case WATER -> THALASSA.get();
            default -> null;
        };
    }

    // ---------------------------------------------------------------- Möbel
    /** Unsichtbarer Sitz auf Stühlen, Bänken und dem Thron. */
    public static final DeferredHolder<EntityType<?>, EntityType<SeatEntity>> SEAT = ENTITIES.registerEntityType("seat",
            SeatEntity::new, MobCategory.MISC,
            builder -> builder.sized(0.001f, 0.001f).noLootTable().noSummon().clientTrackingRange(10).updateInterval(20));

    private ModEntities() {}

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        DRAGONS.values().forEach(type -> event.put(type.get(), DragonEntity.createAttributes().build()));
        SOLDIERS.values().forEach(type -> event.put(type.get(), SoldierEntity.createAttributes().build()));
        event.put(BANDIT.get(), BanditEntity.createAttributes().build());
        event.put(CITIZEN.get(), io.github.sirschrolli.kingdomsatwar.economy.citizen.CitizenEntity.createAttributes().build());
        event.put(VILLAGER.get(), io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity.createAttributes().build());
        event.put(BANDIT_ARCHER.get(), BanditEntity.createAttributes().build());
        event.put(CATTLE.get(), Cattle.createAttributes().build());
        event.put(FARM_PIG.get(), FarmPig.createAttributes().build());
        event.put(FARM_SHEEP.get(), FarmSheep.createAttributes().build());
        event.put(FARM_CHICKEN.get(), FarmChicken.createAttributes().build());
        event.put(FARM_HORSE.get(), FarmHorse.createAttributes().build());
        event.put(FARM_DONKEY.get(), FarmDonkey.createAttributes().build());
        event.put(FARM_MULE.get(), FarmMule.createAttributes().build());
        event.put(FARM_GOAT.get(), FarmGoat.createAttributes().build());
        event.put(HARE.get(), Hare.createAttributes().build());
        // Eigene Monster (E4a): Werte wie bei Vanilla
        event.put(ZOMBIE.get(), net.minecraft.world.entity.monster.zombie.Zombie.createAttributes().build());
        event.put(HUSK.get(), net.minecraft.world.entity.monster.zombie.Zombie.createAttributes().build());
        event.put(DROWNED.get(), net.minecraft.world.entity.monster.zombie.Drowned.createAttributes().build());
        event.put(ZOMBIE_VILLAGER.get(), net.minecraft.world.entity.monster.zombie.Zombie.createAttributes().build());
        event.put(SKELETON.get(), net.minecraft.world.entity.monster.skeleton.AbstractSkeleton.createAttributes().build());
        event.put(STRAY.get(), net.minecraft.world.entity.monster.skeleton.AbstractSkeleton.createAttributes().build());
        event.put(BOGGED.get(), net.minecraft.world.entity.monster.skeleton.Bogged.createAttributes().build());
        event.put(PARCHED.get(), net.minecraft.world.entity.monster.skeleton.Parched.createAttributes().build());
        event.put(CREEPER.get(), net.minecraft.world.entity.monster.Creeper.createAttributes().build());
        event.put(SPIDER.get(), net.minecraft.world.entity.monster.spider.Spider.createAttributes().build());
        event.put(CAVE_SPIDER.get(), net.minecraft.world.entity.monster.spider.CaveSpider.createCaveSpider().build());
        event.put(SLIME.get(), net.minecraft.world.entity.monster.Monster.createMonsterAttributes().build());
        event.put(SULFUR_CUBE.get(), net.minecraft.world.entity.monster.cubemob.SulfurCube.createSulfurCubeAttributes().build());
        event.put(WITCH.get(), net.minecraft.world.entity.monster.Witch.createAttributes().build());
        event.put(PHANTOM.get(), net.minecraft.world.entity.monster.Monster.createMonsterAttributes().build());
        event.put(SILVERFISH.get(), net.minecraft.world.entity.monster.Silverfish.createAttributes().build());
        event.put(ZOMBIE_HORSE.get(), net.minecraft.world.entity.animal.equine.ZombieHorse.createAttributes().build());
        event.put(SKELETON_HORSE.get(), net.minecraft.world.entity.animal.equine.SkeletonHorse.createAttributes().build());
        event.put(CAMEL_HUSK.get(), net.minecraft.world.entity.animal.camel.Camel.createAttributes().build());
        event.put(PILLAGER.get(), net.minecraft.world.entity.monster.illager.Pillager.createAttributes().build());
        event.put(VINDICATOR.get(), net.minecraft.world.entity.monster.illager.Vindicator.createAttributes().build());
        event.put(EVOKER.get(), net.minecraft.world.entity.monster.illager.Evoker.createAttributes().build());
        event.put(ILLUSIONER.get(), net.minecraft.world.entity.monster.illager.Illusioner.createAttributes().build());
        event.put(VEX.get(), net.minecraft.world.entity.monster.Vex.createAttributes().build());
        event.put(RAVAGER.get(), net.minecraft.world.entity.monster.Ravager.createAttributes().build());
        event.put(GUARDIAN.get(), net.minecraft.world.entity.monster.Guardian.createAttributes().build());
        event.put(ELDER_GUARDIAN.get(), net.minecraft.world.entity.monster.ElderGuardian.createAttributes().build());
        event.put(BREEZE.get(), net.minecraft.world.entity.monster.breeze.Breeze.createAttributes().build());
        event.put(CREAKING.get(), net.minecraft.world.entity.monster.creaking.Creaking.createAttributes().build());
        event.put(WARDEN.get(), net.minecraft.world.entity.monster.warden.Warden.createAttributes().build());
        event.put(WOLF.get(), WildWolf.createAttributes().build());
        event.put(FOX.get(), WildFox.createAttributes().build());
        event.put(BEAR.get(), Bear.createAttributes().build());
        event.put(PANDA.get(), WildPanda.createAttributes().build());
        event.put(CAT.get(), HouseCat.createAttributes().build());
        event.put(WILDCAT.get(), Wildcat.createAttributes().build());
        event.put(PARROT.get(), WildParrot.createAttributes().build());
        event.put(BEE.get(), WildBee.createAttributes().build());
        event.put(BAT.get(), WildBat.createAttributes().build());
        event.put(ARMADILLO.get(), WildArmadillo.createAttributes().build());
        event.put(CAMEL.get(), WildCamel.createAttributes().build());
        event.put(LLAMA.get(), WildLlama.createAttributes().build());
        event.put(TRADER_LLAMA.get(), WildLlama.createAttributes().build());
        event.put(WILD_BOAR.get(), WildBoarEntity.createAttributes().build());
        event.put(DEER.get(), DeerEntity.createAttributes().build());
        event.put(GOOSE.get(), GooseEntity.createAttributes().build());
        event.put(WILL_O_WISP.get(), WillOWispEntity.createAttributes().build());
        event.put(CAVE_TROLL.get(), CaveTrollEntity.createAttributes().build());
        event.put(LINDWORM.get(), LindwormEntity.createAttributes().build());
        event.put(PYRRHON.get(), PyrrhonEntity.createAttributes().build());
        event.put(GLACIARA.get(), io.github.sirschrolli.kingdomsatwar.entity.boss.ice.GlaciaraEntity.createAttributes().build());
        event.put(VERDANTIS.get(), io.github.sirschrolli.kingdomsatwar.entity.boss.nature.VerdantisEntity.createAttributes().build());
        event.put(TERRAGON.get(), io.github.sirschrolli.kingdomsatwar.entity.boss.earth.TerragonEntity.createAttributes().build());
        event.put(VOLTARION.get(), io.github.sirschrolli.kingdomsatwar.entity.boss.lightning.VoltarionEntity.createAttributes().build());
        event.put(THALASSA.get(), io.github.sirschrolli.kingdomsatwar.entity.boss.water.ThalassaEntity.createAttributes().build());
        event.put(NYXAR.get(), io.github.sirschrolli.kingdomsatwar.entity.boss.shadow.NyxarEntity.createAttributes().build());
        event.put(VINELING.get(), io.github.sirschrolli.kingdomsatwar.entity.boss.nature.VinelingEntity.createAttributes().build());
        MAGES.values().forEach(type -> event.put(type.get(), io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity.createAttributes().build()));
        event.put(MAGE_IMAGE.get(), io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageImageEntity.createAttributes().build());
        event.put(SHADE_STALKER.get(), ShadeStalkerEntity.createAttributes().build());
        event.put(CRYSTAL_CRAWLER.get(), CrystalCrawlerEntity.createAttributes().build());
        event.put(NIGHT_WING.get(), NightWingEntity.createAttributes().build());
        event.put(MOON_STAG.get(), DeerEntity.createAttributes().build());
        event.put(GLIMMER_MOTH.get(), GlimmerMothEntity.createAttributes().build());
        event.put(UMBRER.get(), io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerEntity.createAttributes().build());
        event.put(UMBRER_GUARD.get(), io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerGuardEntity.createAttributes().build());
    }

    /**
     * Wo Lebewesen von selbst erscheinen dürfen: Banditen nachts unter freiem Himmel, Tiere auf Gras,
     * Irrlichter nachts im Dunkeln, Trolle tief unter der Erde, Lindwürmer unter freiem Himmel.
     */
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        var replace = RegisterSpawnPlacementsEvent.Operation.REPLACE;
        var ground = SpawnPlacementTypes.ON_GROUND;
        var surface = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
        for (var bandit : java.util.List.of(BANDIT, BANDIT_ARCHER)) {
            event.register(bandit.get(), ground, surface, BanditEntity::checkBanditSpawnRules, replace);
        }
        // eigene Hof- und Weidetiere: wie die Vanilla-Tiere, die sie ersetzen
        event.register(CATTLE.get(), ground, surface, Cattle::checkCattleSpawnRules, replace);
        for (var animal : java.util.List.<java.util.function.Supplier<? extends EntityType<? extends Animal>>>of(FARM_PIG, FARM_SHEEP,
                FARM_CHICKEN, FARM_HORSE, FARM_DONKEY, FARM_MULE)) {
            registerAnimal(event, animal.get());
        }
        event.register(FARM_GOAT.get(), ground, surface, Goat::checkGoatSpawnRules, replace);
        event.register(HARE.get(), ground, surface, Rabbit::checkRabbitSpawnRules, replace);
        // eigene Wildtiere: wie die Vanilla-Tiere, die sie ersetzen (Fennek und Trampeltier auch auf Sand bzw. Gras)
        var noLeaves = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
        var free = SpawnPlacementTypes.NO_RESTRICTIONS;
        // Eigene Monster (E4a): Regeln wie bei Vanilla
        event.register(ZOMBIE.get(), ground, surface, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, replace);
        event.register(HUSK.get(), ground, surface, net.minecraft.world.entity.monster.Monster::checkSurfaceMonstersSpawnRules, replace);
        event.register(DROWNED.get(), SpawnPlacementTypes.IN_WATER, surface, net.minecraft.world.entity.monster.zombie.Drowned::checkDrownedSpawnRules, replace);
        event.register(ZOMBIE_VILLAGER.get(), ground, surface, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, replace);
        event.register(SKELETON.get(), ground, surface, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, replace);
        event.register(STRAY.get(), ground, surface, net.minecraft.world.entity.monster.skeleton.Stray::checkStraySpawnRules, replace);
        event.register(BOGGED.get(), ground, surface, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, replace);
        event.register(PARCHED.get(), ground, surface, net.minecraft.world.entity.monster.Monster::checkSurfaceMonstersSpawnRules, replace);
        event.register(CREEPER.get(), ground, surface, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, replace);
        event.register(SPIDER.get(), ground, surface, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, replace);
        event.register(CAVE_SPIDER.get(), ground, surface, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, replace);
        event.register(SLIME.get(), ground, surface, net.minecraft.world.entity.monster.cubemob.Slime::checkSlimeSpawnRules, replace);
        event.register(SULFUR_CUBE.get(), ground, surface, net.minecraft.world.entity.monster.cubemob.SulfurCube::checkSulfurCubeSpawnRules, replace);
        event.register(WITCH.get(), ground, surface, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, replace);
        event.register(PHANTOM.get(), SpawnPlacementTypes.NO_RESTRICTIONS, surface, net.minecraft.world.entity.Mob::checkMobSpawnRules, replace);
        event.register(SILVERFISH.get(), ground, surface, net.minecraft.world.entity.monster.Silverfish::checkSilverfishSpawnRules, replace);
        event.register(ZOMBIE_HORSE.get(), ground, surface, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, replace);
        event.register(SKELETON_HORSE.get(), ground, surface, net.minecraft.world.entity.animal.equine.SkeletonHorse::checkSkeletonHorseSpawnRules, replace);
        event.register(CAMEL_HUSK.get(), ground, surface, net.minecraft.world.entity.monster.Monster::checkSurfaceMonstersSpawnRules, replace);
        // übrige Monster (E4b): Regeln wie bei Vanilla
        event.register(PILLAGER.get(), ground, noLeaves, net.minecraft.world.entity.monster.PatrollingMonster::checkPatrollingMonsterSpawnRules, replace);
        event.register(VINDICATOR.get(), free, noLeaves, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, replace);
        event.register(EVOKER.get(), free, noLeaves, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, replace);
        event.register(ILLUSIONER.get(), free, noLeaves, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, replace);
        event.register(VEX.get(), free, noLeaves, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, replace);
        event.register(RAVAGER.get(), ground, noLeaves, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, replace);
        event.register(GUARDIAN.get(), SpawnPlacementTypes.IN_WATER, noLeaves, net.minecraft.world.entity.monster.Guardian::checkGuardianSpawnRules, replace);
        event.register(ELDER_GUARDIAN.get(), SpawnPlacementTypes.IN_WATER, noLeaves, net.minecraft.world.entity.monster.Guardian::checkGuardianSpawnRules,
                replace);
        event.register(BREEZE.get(), ground, noLeaves, net.minecraft.world.entity.monster.Monster::checkAnyLightMonsterSpawnRules, replace);
        event.register(CREAKING.get(), ground, noLeaves, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, replace);
        event.register(WARDEN.get(), free, noLeaves, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, replace);
        event.register(WOLF.get(), ground, noLeaves, Wolf::checkWolfSpawnRules, replace);
        event.register(FOX.get(), free, noLeaves, WildAnimals::checkGrassOrSandSpawnRules, replace);
        event.register(BEAR.get(), ground, noLeaves, PolarBear::checkPolarBearSpawnRules, replace);
        event.register(PANDA.get(), free, noLeaves, Animal::checkAnimalSpawnRules, replace);
        event.register(CAT.get(), ground, noLeaves, Animal::checkAnimalSpawnRules, replace);
        event.register(WILDCAT.get(), ground, Heightmap.Types.MOTION_BLOCKING, Ocelot::checkOcelotSpawnRules, replace);
        event.register(PARROT.get(), ground, Heightmap.Types.MOTION_BLOCKING, Parrot::checkParrotSpawnRules, replace);
        event.register(BEE.get(), ground, noLeaves, Animal::checkAnimalSpawnRules, replace);
        event.register(BAT.get(), ground, noLeaves, Bat::checkBatSpawnRules, replace);
        event.register(ARMADILLO.get(), ground, noLeaves, Armadillo::checkArmadilloSpawnRules, replace);
        event.register(CAMEL.get(), ground, noLeaves, WildCamel::checkWildCamelSpawnRules, replace);
        event.register(LLAMA.get(), ground, noLeaves, Animal::checkAnimalSpawnRules, replace);
        event.register(TRADER_LLAMA.get(), free, noLeaves, Animal::checkAnimalSpawnRules, replace);
        event.register(WILD_BOAR.get(), ground, surface, Animal::checkAnimalSpawnRules, replace);
        event.register(DEER.get(), ground, surface, Animal::checkAnimalSpawnRules, replace);
        event.register(GOOSE.get(), ground, surface, Animal::checkAnimalSpawnRules, replace);
        event.register(WILL_O_WISP.get(), ground, surface, WillOWispEntity::checkWispSpawnRules, replace);
        event.register(CAVE_TROLL.get(), ground, surface, CaveTrollEntity::checkTrollSpawnRules, replace);
        event.register(LINDWORM.get(), ground, surface, LindwormEntity::checkLindwormSpawnRules, replace);
        // Schattenwelt: Pirscher überall im Dunkeln, Kriecher in Höhlen und Schluchten, Schwingen und Falter in der Luft
        event.register(SHADE_STALKER.get(), ground, surface, ShadeStalkerEntity::checkStalkerSpawnRules, replace);
        event.register(CRYSTAL_CRAWLER.get(), ground, surface, CrystalCrawlerEntity::checkCrawlerSpawnRules, replace);
        event.register(NIGHT_WING.get(), SpawnPlacementTypes.NO_RESTRICTIONS, surface, NightWingEntity::checkNightWingSpawnRules, replace);
        event.register(MOON_STAG.get(), ground, surface, Animal::checkAnimalSpawnRules, replace);
        event.register(GLIMMER_MOTH.get(), SpawnPlacementTypes.NO_RESTRICTIONS, surface, GlimmerMothEntity::checkMothSpawnRules, replace);
    }

    private static <T extends Animal> void registerAnimal(RegisterSpawnPlacementsEvent event, EntityType<T> type) {
        event.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
