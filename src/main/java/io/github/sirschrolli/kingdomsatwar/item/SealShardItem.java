package io.github.sirschrolli.kingdomsatwar.item;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Siegelsplitter: Jeder der sechs Drachen trägt ein Sechstel des Siegels in seinem Herzen – nach seinem Sieg bleibt es
 * als Splitter zurück. Er gehört in den Sockel seines Monolithen am Siegelkreis. Splitter verbrennen nicht und
 * verschwinden nicht, wenn sie liegen bleiben.
 */
public class SealShardItem extends Item {
    private final DragonType element;

    public SealShardItem(DragonType element, Item.Properties properties) {
        super(properties);
        this.element = element;
    }

    public DragonType element() {
        return this.element;
    }

    @Override
    public int getEntityLifespan(ItemStack stack, Level level) {
        return Integer.MAX_VALUE;
    }
}
