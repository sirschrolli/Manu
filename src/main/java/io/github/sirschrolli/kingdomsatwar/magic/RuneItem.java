package io.github.sirschrolli.kingdomsatwar.magic;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import net.minecraft.world.item.Item;

/**
 * Rune eines der sechs Drachen-Elemente. Hält man sie in der zweiten Hand, wirkt der Aetherit-Stab ihren Zauber;
 * die Rune selbst verbraucht sich dabei nicht.
 */
public class RuneItem extends Item {
    private final DragonType element;

    public RuneItem(DragonType element, Properties properties) {
        super(properties);
        this.element = element;
    }

    public DragonType element() {
        return this.element;
    }
}
