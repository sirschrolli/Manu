package io.github.sirschrolli.kingdomsatwar.block.sea;

import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Riesentang (Spitze): goldbrauner Tang mit breiten Wedeln an runden Schwimmblasen, der in kühlen Meeren ganze Wälder
 * bis zur Oberfläche bildet. Wächst wie Seetang (auch mit Knochenmehl).
 */
public class GiantKelpBlock extends KelpBlock {
    public GiantKelpBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected Block getBodyBlock() {
        return SeaBlocks.GIANT_KELP_PLANT.get();
    }
}
