package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.ButterflyEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.EmberSalamanderEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.FlamingoEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.GriffinEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.MireSnapperEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.PrimevalElkEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.SnowLeopardEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.StormGullEntity;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Die Tiere der eigenen Biome: Schneeleopard (Frostgipfel), Urelch (Urhain), Greif (Felsschluchten) und Sturmmöwe
 * (Sturmküste) seit 0.85, Glutsalamander (Aschefelder), Moorschnapper (Nebelmoor), Flamingo (Korallenküste) und
 * Schmetterlinge (Königsauen) seit 0.86 – Wesenarten, Spawn-Eier, Werte und Regeln für ihr Auftreten.
 */
public final class BiomeBeasts {
    private BiomeBeasts() {}

    public static void init() {}

    /** Schneeleopard: scheu, jagt Ziegen und Hasen im Sprung, zähmbar mit rohem Fleisch. */
    public static final DeferredHolder<EntityType<?>, EntityType<SnowLeopardEntity>> SNOW_LEOPARD = ModEntities.ENTITIES.registerEntityType(
            "snow_leopard", SnowLeopardEntity::new, MobCategory.CREATURE,
            builder -> builder.sized(0.9f, 0.95f).eyeHeight(0.8f).clientTrackingRange(10));

    /** Urelch: riesig und ruhig, Bullen mit bemoosten Schaufeln stürmen, wenn man sie angreift; zähmbar, reitbar, mit Truhe. */
    public static final DeferredHolder<EntityType<?>, EntityType<PrimevalElkEntity>> PRIMEVAL_ELK = ModEntities.ENTITIES.registerEntityType(
            "primeval_elk", PrimevalElkEntity::new, MobCategory.CREATURE,
            builder -> builder.sized(1.6f, 2.25f).eyeHeight(1.95f).passengerAttachments(1.82f).clientTrackingRange(10));

    /** Sturmmöwe: Schwärme über der Küste; schnappt sich gewöhnliche Fische aus der Hand. */
    public static final DeferredHolder<EntityType<?>, EntityType<StormGullEntity>> STORM_GULL = ModEntities.ENTITIES.registerEntityType(
            "storm_gull", StormGullEntity::new, MobCategory.CREATURE,
            builder -> builder.sized(0.55f, 0.6f).eyeHeight(0.5f).clientTrackingRange(10));

    /** Greif: kreist über den Felsschluchten, sitzt auf den höchsten Felsnadeln, jagt im Sturzflug. */
    public static final DeferredHolder<EntityType<?>, EntityType<GriffinEntity>> GRIFFIN = ModEntities.ENTITIES.registerEntityType(
            "griffin", GriffinEntity::new, MobCategory.CREATURE,
            builder -> builder.sized(1.5f, 1.9f).eyeHeight(1.75f).clientTrackingRange(12));

    /** Glutsalamander: feuerfest, läuft über Lava, sonnt sich an der Glut, verbrennt den, der ihn schlägt. */
    public static final DeferredHolder<EntityType<?>, EntityType<EmberSalamanderEntity>> EMBER_SALAMANDER = ModEntities.ENTITIES.registerEntityType(
            "ember_salamander", EmberSalamanderEntity::new, MobCategory.CREATURE,
            builder -> builder.sized(0.7f, 0.35f).eyeHeight(0.25f).fireImmune().clientTrackingRange(8));

    /** Moorschnapper: lauert im flachen Wasser, schnappt nach allem vor seiner Nase, zieht sich in den Panzer zurück. */
    public static final DeferredHolder<EntityType<?>, EntityType<MireSnapperEntity>> MIRE_SNAPPER = ModEntities.ENTITIES.registerEntityType(
            "mire_snapper", MireSnapperEntity::new, MobCategory.CREATURE,
            builder -> builder.sized(1.0f, 0.6f).eyeHeight(0.45f).clientTrackingRange(8));

    /** Flamingo: Scharen im flachen Wasser, seiht, ruht auf einem Bein, flattert auf. */
    public static final DeferredHolder<EntityType<?>, EntityType<FlamingoEntity>> FLAMINGO = ModEntities.ENTITIES.registerEntityType(
            "flamingo", FlamingoEntity::new, MobCategory.CREATURE,
            builder -> builder.sized(0.6f, 1.5f).eyeHeight(1.35f).clientTrackingRange(10));

    /** Schmetterling: gaukelt über die Wiesen und setzt sich auf Blumen (sechs Arten). */
    public static final DeferredHolder<EntityType<?>, EntityType<ButterflyEntity>> BUTTERFLY = ModEntities.ENTITIES.registerEntityType(
            "butterfly", ButterflyEntity::new, MobCategory.AMBIENT,
            builder -> builder.sized(0.35f, 0.3f).eyeHeight(0.15f).clientTrackingRange(8));

    public static final DeferredItem<SpawnEggItem> SNOW_LEOPARD_SPAWN_EGG = egg("snow_leopard", () -> SNOW_LEOPARD.get());
    public static final DeferredItem<SpawnEggItem> PRIMEVAL_ELK_SPAWN_EGG = egg("primeval_elk", () -> PRIMEVAL_ELK.get());
    public static final DeferredItem<SpawnEggItem> STORM_GULL_SPAWN_EGG = egg("storm_gull", () -> STORM_GULL.get());
    public static final DeferredItem<SpawnEggItem> GRIFFIN_SPAWN_EGG = egg("griffin", () -> GRIFFIN.get());
    public static final DeferredItem<SpawnEggItem> EMBER_SALAMANDER_SPAWN_EGG = egg("ember_salamander", () -> EMBER_SALAMANDER.get());
    public static final DeferredItem<SpawnEggItem> MIRE_SNAPPER_SPAWN_EGG = egg("mire_snapper", () -> MIRE_SNAPPER.get());
    public static final DeferredItem<SpawnEggItem> FLAMINGO_SPAWN_EGG = egg("flamingo", () -> FLAMINGO.get());
    public static final DeferredItem<SpawnEggItem> BUTTERFLY_SPAWN_EGG = egg("butterfly", () -> BUTTERFLY.get());

    public static List<DeferredItem<SpawnEggItem>> spawnEggs() {
        return List.of(SNOW_LEOPARD_SPAWN_EGG, PRIMEVAL_ELK_SPAWN_EGG, STORM_GULL_SPAWN_EGG, GRIFFIN_SPAWN_EGG, EMBER_SALAMANDER_SPAWN_EGG,
                MIRE_SNAPPER_SPAWN_EGG, FLAMINGO_SPAWN_EGG, BUTTERFLY_SPAWN_EGG);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SNOW_LEOPARD.get(), SnowLeopardEntity.createAttributes().build());
        event.put(PRIMEVAL_ELK.get(), PrimevalElkEntity.createAttributes().build());
        event.put(STORM_GULL.get(), StormGullEntity.createAttributes().build());
        event.put(GRIFFIN.get(), GriffinEntity.createAttributes().build());
        event.put(EMBER_SALAMANDER.get(), EmberSalamanderEntity.createAttributes().build());
        event.put(MIRE_SNAPPER.get(), MireSnapperEntity.createAttributes().build());
        event.put(FLAMINGO.get(), FlamingoEntity.createAttributes().build());
        event.put(BUTTERFLY.get(), ButterflyEntity.createAttributes().build());
    }

    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        var replace = RegisterSpawnPlacementsEvent.Operation.REPLACE;
        event.register(SNOW_LEOPARD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SnowLeopardEntity::checkLeopardSpawnRules, replace);
        event.register(PRIMEVAL_ELK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                PrimevalElkEntity::checkElkSpawnRules, replace);
        event.register(STORM_GULL.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                StormGullEntity::checkGullSpawnRules, replace);
        event.register(GRIFFIN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GriffinEntity::checkGriffinSpawnRules, replace);
        event.register(EMBER_SALAMANDER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EmberSalamanderEntity::checkSalamanderSpawnRules, replace);
        event.register(MIRE_SNAPPER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                MireSnapperEntity::checkSnapperSpawnRules, replace);
        event.register(FLAMINGO.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                FlamingoEntity::checkFlamingoSpawnRules, replace);
        event.register(BUTTERFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ButterflyEntity::checkButterflySpawnRules, replace);
    }

    private static DeferredItem<SpawnEggItem> egg(String name, Supplier<EntityType<? extends Mob>> type) {
        DeferredItem<SpawnEggItem> item = ModItems.ITEMS.registerItem(name + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(type.get()));
        ModItems.DRAGON_TAB.add(item);
        return item;
    }
}
