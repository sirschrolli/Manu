package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.block.nether.LavaCrustBlock;
import io.github.sirschrolli.kingdomsatwar.registry.FlameGear;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

/**
 * Modelle zum Feueropal (seit 1.1.6): die Erze (Knollen über Netherrack und Schwarzstein, die hellsten Pixel leuchten), der
 * Block (mit wogendem Feuer in der Leuchtebene), der Stein (im Inventar das Bild, in der Hand plastisch), die Rüstung der
 * Glutwandler und die Glutkruste in vier Stufen (je älter, desto weiter aufgebrochen und heller glühend).
 */
final class FlameModels {
    private FlameModels() {}

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        ModModelProvider.ore(blocks, FlameGear.FIRE_OPAL_ORE, Blocks.NETHERRACK, "fire_opal");
        ModModelProvider.ore(blocks, FlameGear.BLACKSTONE_FIRE_OPAL_ORE, Blocks.BLACKSTONE, "fire_opal");
        ModModelProvider.glowingCube(blocks, FlameGear.FIRE_OPAL_BLOCK.get());
        ModModelProvider.flat(items, FlameGear.FIRE_OPAL.get());
        ModModelProvider.armor(items, FlameGear.EMBERWALKER_HELMET.get(), FlameGear.EMBERWALKER_CHESTPLATE.get(),
                FlameGear.EMBERWALKER_LEGGINGS.get(), FlameGear.EMBERWALKER_BOOTS.get());

        var crust = FlameGear.LAVA_CRUST.get();
        Identifier[] ages = new Identifier[LavaCrustBlock.MAX_AGE + 1];
        for (int age = 0; age <= LavaCrustBlock.MAX_AGE; age++) {
            ages[age] = ModModelProvider.glowingCubeModel(blocks, crust, "_" + age);
        }
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(crust).with(PropertyDispatch.initial(LavaCrustBlock.AGE)
                .generate(age -> BlockModelGenerators.plainVariant(ages[age]))));
    }
}
