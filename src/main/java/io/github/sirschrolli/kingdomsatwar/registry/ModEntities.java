package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.BanditEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
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

    private ModEntities() {}

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        DRAGONS.values().forEach(type -> event.put(type.get(), DragonEntity.createAttributes().build()));
        SOLDIERS.values().forEach(type -> event.put(type.get(), SoldierEntity.createAttributes().build()));
        event.put(BANDIT.get(), BanditEntity.createAttributes().build());
        event.put(BANDIT_ARCHER.get(), BanditEntity.createAttributes().build());
    }

    /** Wo Banditen von selbst erscheinen dürfen: nachts unter freiem Himmel auf festem Boden. */
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        for (var bandit : java.util.List.of(BANDIT, BANDIT_ARCHER)) {
            event.register(bandit.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    BanditEntity::checkBanditSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }
}
