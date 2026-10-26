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
        event.put(BANDIT_ARCHER.get(), BanditEntity.createAttributes().build());
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
}
