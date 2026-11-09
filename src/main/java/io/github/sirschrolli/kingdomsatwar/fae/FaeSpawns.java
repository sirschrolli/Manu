package io.github.sirschrolli.kingdomsatwar.fae;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.biome.MobSpawnSettings;

/**
 * Wer in den Biomen des Feenreichs von selbst erscheint: Kaninchen auf den Wiesen, Füchse im Glimmerwald, Frösche an den
 * Mondseen – und die Wesen des Feenreichs selbst.
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
        return new Spawns().add(EntityTypes.RABBIT, 8, 2, 3).build();
    }

    static MobSpawnSettings glimmerwood() {
        return new Spawns().add(EntityTypes.FOX, 6, 2, 3).add(EntityTypes.RABBIT, 3, 1, 2).build();
    }

    static MobSpawnSettings mushroomGrove() {
        return new Spawns().add(EntityTypes.RABBIT, 3, 1, 2).build();
    }

    static MobSpawnSettings moonlitLakes() {
        return new Spawns().add(EntityTypes.FROG, 8, 2, 4).build();
    }

    static MobSpawnSettings thornwild() {
        return new Spawns().add(EntityTypes.FOX, 3, 1, 2).build();
    }
}
