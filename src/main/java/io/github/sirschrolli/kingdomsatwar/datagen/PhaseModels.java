package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.PhaseGear;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.world.level.block.Blocks;

/**
 * Modelle zum Phasit (seit 1.4): das Erz (Kristallbüschel über Endstein, die hellsten Pixel leuchten), der Block (das Licht
 * springt in der Leuchtebene von Stein zu Stein), der Kristall (im Inventar das Bild, in der Hand plastisch) und die Rüstung
 * der Leerwandler.
 */
final class PhaseModels {
    private PhaseModels() {}

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        ModModelProvider.ore(blocks, PhaseGear.PHASIT_ORE, Blocks.END_STONE, "phasit");
        ModModelProvider.glowingCube(blocks, PhaseGear.PHASIT_BLOCK.get());
        ModModelProvider.flat(items, PhaseGear.PHASIT.get());
        ModModelProvider.armor(items, PhaseGear.VOIDWALKER_HELMET.get(), PhaseGear.VOIDWALKER_CHESTPLATE.get(),
                PhaseGear.VOIDWALKER_LEGGINGS.get(), PhaseGear.VOIDWALKER_BOOTS.get());
    }
}
