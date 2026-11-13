package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Dämmerkorn: das Getreide der Umbrer. Wächst auf Schattenackerland (und gewöhnlichem Ackerland) im Zwielicht; reife
 * Ähren schimmern violett-golden und leuchten ein wenig.
 */
public class DuskgrainBlock extends CropBlock {
    public DuskgrainBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ShadowItems.DUSKGRAIN_SEEDS.get();
    }

    /** Licht der reifen Ähren. */
    public static int light(BlockState state) {
        int age = state.getValue(AGE);
        return age >= MAX_AGE ? 5 : age >= 5 ? 2 : 0;
    }
}
