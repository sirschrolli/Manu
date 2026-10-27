package io.github.sirschrolli.kingdomsatwar.magic;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import net.minecraft.world.item.Item;

/**
 * Rune eines der sechs Drachen-Elemente – aus der Zeit, bevor die Erzmagier ihre Zaubertafeln schrieben. Runen werden
 * nicht mehr hergestellt; wer noch welche besitzt, kann sie am Magietisch wie eine Zaubertafel ihres Elements
 * einschreiben.
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

    /** Der Zauber, den die Rune am Magietisch lehrt. */
    public Spell spell() {
        return Spell.of(this.element);
    }
}
