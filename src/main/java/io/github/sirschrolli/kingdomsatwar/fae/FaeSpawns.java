package io.github.sirschrolli.kingdomsatwar.fae;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.biome.MobSpawnSettings;

/**
 * Wer in den Biomen des Feenreichs von selbst erscheint: Feen überall (am meisten auf den Wiesen und im Glimmerwald),
 * Feen-Irrlichter, Pilzlinge im Pilzhain, Dornwichte nur in der Dornenwildnis – dazu Kaninchen, Füchse und Frösche.
 */
final class FaeSpawns {
    private FaeSpawns() {}

    private static final class Spawns {
        private final MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();

        Spawns add(EntityType<?> type, int weight, int min, int max) {
            this.builder.addSpawn(type, weight, min, max);
            return this;
        }

        MobSpawnSettings build() {
            return this.builder.build();
        }
    }

    static MobSpawnSettings meadows() {
        return new Spawns().add(ModEntities.FAIRY.get(), 14, 2, 4).add(ModEntities.FAE_WISP.get(), 4, 1, 1)
                .add(EntityTypes.RABBIT, 6, 2, 3).build();
    }

    static MobSpawnSettings glimmerwood() {
        return new Spawns().add(ModEntities.FAIRY.get(), 12, 2, 3).add(ModEntities.FAE_WISP.get(), 5, 1, 1)
                .add(EntityTypes.FOX, 5, 2, 3).add(EntityTypes.RABBIT, 2, 1, 2).build();
    }

    static MobSpawnSettings mushroomGrove() {
        return new Spawns().add(ModEntities.MUSHROOMLING.get(), 16, 2, 4).add(ModEntities.FAIRY.get(), 6, 1, 2)
                .add(ModEntities.FAE_WISP.get(), 3, 1, 1).build();
    }

    static MobSpawnSettings moonlitLakes() {
        return new Spawns().add(ModEntities.FAIRY.get(), 8, 1, 3).add(ModEntities.FAE_WISP.get(), 4, 1, 1)
                .add(EntityTypes.FROG, 8, 2, 4).build();
    }

    static MobSpawnSettings thornwild() {
        return new Spawns().add(ModEntities.THORN_WIGHT.get(), 30, 1, 2).add(ModEntities.FAE_WISP.get(), 3, 1, 1)
                .add(EntityTypes.FOX, 2, 1, 2).build();
    }
}
