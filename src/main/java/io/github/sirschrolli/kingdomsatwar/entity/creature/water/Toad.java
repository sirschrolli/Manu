package io.github.sirschrolli.kingdomsatwar.entity.creature.water;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.Level;

/**
 * Frosch (statt Vanilla-Frosch): je nach Klima ein grüner Teichfrosch, eine Feuerkröte mit rotem Bauch oder ein
 * rostbrauner Grasfrosch. Springt, quakt, fängt mit der Zunge Schleime und Magmawürfel und lässt Froschlichter
 * entstehen wie der Vanilla-Frosch.
 */
public class Toad extends Frog {
    public Toad(EntityType<? extends net.minecraft.world.entity.animal.Animal> type, Level level) {
        super(type, level);
    }
}
