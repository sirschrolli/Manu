package io.github.sirschrolli.kingdomsatwar.block.sea;

import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.KelpPlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Riesentang (Stängel): die Glieder unter der Spitze. */
public class GiantKelpPlantBlock extends KelpPlantBlock {
    public GiantKelpPlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return SeaBlocks.GIANT_KELP.get();
    }
}
