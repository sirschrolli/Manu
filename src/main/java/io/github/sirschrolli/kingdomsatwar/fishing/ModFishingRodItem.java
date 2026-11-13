package io.github.sirschrolli.kingdomsatwar.fishing;

import net.minecraft.world.item.FishingRodItem;

/**
 * Eine Angel der Mod (seit 0.87): wirft aus und holt ein wie die Angel von Minecraft; was sie besser kann, steht in
 * {@link RodType} und wirkt beim Biss und im Drill ({@link Fishing}).
 */
public class ModFishingRodItem extends FishingRodItem {
    private final RodType type;

    public ModFishingRodItem(RodType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public RodType type() {
        return this.type;
    }
}
