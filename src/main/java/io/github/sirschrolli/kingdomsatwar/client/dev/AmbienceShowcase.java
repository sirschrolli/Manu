package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import java.util.Deque;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;

/**
 * Nur zur Prüfung der Umgebungsklänge: der Spieler steht nacheinander in mehreren eigenen Biomen, je eine Weile am Tag und
 * in der Nacht. Mit {@code ALSOFT_CONF} (Ausgabe in eine WAV-Datei) lässt sich so aufnehmen, was das Spiel wirklich spielt;
 * die Zeitmarken stehen im Log ("[Ambience]").
 */
final class AmbienceShowcase {
    private AmbienceShowcase() {}

    static void listen(Deque<Step> steps, String only) {
        List<String> biomes = only.isEmpty()
                ? List.of("primeval_forest", "royal_meadows", "mist_moor", "storm_coast", "frost_peaks", "ash_fields")
                : List.of(only.split("\\+"));
        steps.add(Step.once(minecraft -> {
            minecraft.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set(0.0);
            minecraft.options.getSoundSourceOptionInstance(SoundSource.AMBIENT).set(1.0);
            minecraft.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(1.0);
        }));
        int i = 0;
        for (String biome : biomes) {
            int x = 40000 + i * 600;
            steps.add(Showcase.tp(x + 0.5, 2, 0.5, 0, 0));
            steps.add(Step.waitTicks(10));
            steps.add(Step.waitForChunks(400));
            steps.add(Step.server(level -> {
                int g = Showcase.ground;
                Showcase.fill(level, new BlockPos(x - 6, g - 1, -6), new BlockPos(x + 6, g - 1, 6), Blocks.GRASS_BLOCK.defaultBlockState());
                Showcase.fill(level, new BlockPos(x - 6, g, -6), new BlockPos(x + 6, g + 8, 6), Blocks.AIR.defaultBlockState());
            }));
            steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format("fillbiome %d %d %d %d %d %d kingdomsatwar:%s",
                    x - 12, Showcase.ground - 8, -12, x + 12, Showcase.ground + 20, 12, biome))));
            steps.add(Step.waitTicks(40));
            steps.add(Step.command("time set 6000"));
            steps.add(Step.once(minecraft -> KingdomsAtWar.LOGGER.info("[Ambience] {} Tag {} – Biom am Spieler {}", biome, System.currentTimeMillis(),
                    minecraft.level.getBiome(minecraft.player.blockPosition()).unwrapKey().orElse(null))));
            steps.add(Step.waitTicks(320));
            steps.add(Step.command("time set 18000"));
            steps.add(Step.once(minecraft -> KingdomsAtWar.LOGGER.info("[Ambience] {} Nacht {}", biome, System.currentTimeMillis())));
            steps.add(Step.waitTicks(320));
            i++;
        }
        steps.add(Step.once(minecraft -> KingdomsAtWar.LOGGER.info("[Ambience] Ende {}", System.currentTimeMillis())));
    }
}
