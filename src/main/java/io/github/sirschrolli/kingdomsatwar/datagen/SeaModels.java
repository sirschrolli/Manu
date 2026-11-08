package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;

/** Modelle des Meeres: die Tiefseelaterne (wie eine Laterne, stehend und hängend), die Beute und die Spawn-Eier. */
final class SeaModels {
    private SeaModels() {}

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        blocks.createLantern(SeaBlocks.DEEP_SEA_LANTERN.get());
        for (var item : SeaBlocks.flatItems()) {
            ModModelProvider.flat(items, item.get());
        }
        for (var egg : SeaBlocks.spawnEggs()) {
            ModModelProvider.flat(items, egg.get());
        }
    }
}
