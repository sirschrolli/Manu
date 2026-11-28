package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.EchoGear;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.world.level.block.Blocks;

/**
 * Modelle des Hallkristalls (seit 1.6): das Erz im Tiefenschiefer (die Kristalle in eigener Leuchtebene), der Block mit
 * wandernden Wellen, der Kristall (in der Hand plastisch) und die Rüstung der Lauscher.
 */
final class EchoModels {
    private EchoModels() {}

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        ModModelProvider.ore(blocks, EchoGear.ECHO_CRYSTAL_ORE, Blocks.DEEPSLATE, "echo_crystal");
        ModModelProvider.glowingCube(blocks, EchoGear.ECHO_CRYSTAL_BLOCK.get());
        ModModelProvider.flat(items, EchoGear.ECHO_CRYSTAL.get());
        ModModelProvider.armor(items, EchoGear.LISTENER_HELMET.get(), EchoGear.LISTENER_CHESTPLATE.get(), EchoGear.LISTENER_LEGGINGS.get(),
                EchoGear.LISTENER_BOOTS.get());
    }
}
