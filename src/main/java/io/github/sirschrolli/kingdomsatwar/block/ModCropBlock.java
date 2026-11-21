package io.github.sirschrolli.kingdomsatwar.block;

import java.util.function.Supplier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Feldfrucht mit acht Wachstumsstufen (Alter 0–7), wie Weizen.
 *
 * <p>Der Samen wird erst beim Aufruf abgefragt, damit Block und Samen sich gegenseitig
 * kennen können, obwohl sie in unterschiedlichen Registern stehen.
 */
public class ModCropBlock extends CropBlock {
    private final Supplier<? extends ItemLike> seed;

    public ModCropBlock(Supplier<? extends ItemLike> seed, BlockBehaviour.Properties properties) {
        super(properties);
        this.seed = seed;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seed.get();
    }
}
