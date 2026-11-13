package io.github.sirschrolli.kingdomsatwar.voidrealm;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.world.level.biome.MobSpawnSettings;

/**
 * Wer in den Biomen der Leere von selbst erscheint: Leerenschlünde treiben überall (am meisten zwischen den
 * Bruchstücken und an den Splitterklippen), Zerrlinge streifen über die Bruchstücke, Splitterwächter wachen in den
 * Ruinen (die meisten stehen schon seit dem Erzeugen der Welt dort), und Sternenrochen gleiten um die Archipele.
 */
final class VoidSpawns {
    private VoidSpawns() {}

    private static final class Spawns {
        private final MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();

        Spawns maws(int weight) {
            this.builder.addSpawn(ModEntities.VOID_MAW.get(), weight, 1, 1);
            return this;
        }

        Spawns distortlings(int weight) {
            this.builder.addSpawn(ModEntities.DISTORTLING.get(), weight, 1, 2);
            return this;
        }

        Spawns wardens(int weight) {
            this.builder.addSpawn(ModEntities.SHARD_WARDEN.get(), weight, 1, 1);
            return this;
        }

        Spawns rays(int weight) {
            this.builder.addSpawn(ModEntities.STAR_RAY.get(), weight, 1, 2);
            return this;
        }

        MobSpawnSettings build() {
            return this.builder.build();
        }
    }

    static MobSpawnSettings expanse() {
        return new Spawns().maws(30).rays(12).build();
    }

    static MobSpawnSettings crystalForest() {
        return new Spawns().distortlings(40).maws(20).rays(10).build();
    }

    static MobSpawnSettings ruins() {
        return new Spawns().wardens(6).distortlings(30).maws(10).rays(6).build();
    }

    static MobSpawnSettings stardust() {
        return new Spawns().distortlings(30).maws(15).rays(14).build();
    }

    static MobSpawnSettings shardCliffs() {
        return new Spawns().maws(30).distortlings(20).wardens(3).rays(4).build();
    }

    static MobSpawnSettings remnants() {
        return new Spawns().distortlings(45).maws(10).rays(6).build();
    }
}
