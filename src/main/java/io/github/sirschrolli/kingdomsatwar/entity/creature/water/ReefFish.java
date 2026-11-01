package io.github.sirschrolli.kingdomsatwar.entity.creature.water;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.level.Level;

/**
 * Rifffisch (statt Tropenfisch): zwei eigene Körperformen mit je sechs eigenen Mustern, gefärbt in den zwei Farben
 * der Vanilla-Variante – so bleiben alle Farbspiele, die benannten Arten und der Fisch im Eimer erhalten.
 */
public class ReefFish extends TropicalFish {
    public ReefFish(EntityType<? extends ReefFish> type, Level level) {
        super(type, level);
    }
}
