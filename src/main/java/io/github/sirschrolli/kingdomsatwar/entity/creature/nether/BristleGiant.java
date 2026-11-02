package io.github.sirschrolli.kingdomsatwar.entity.creature.nether;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.level.Level;

/**
 * Borstenhüne (statt Piglin-Barbar): ein Wächter des Keilervolks mit gehörntem Eisenhelm, Brustpanzer mit
 * Keilerkopf-Wappen, Schulterstücken und Kettenärmeln. Er bewacht wie der Barbar die Schätze der Bastionen, lässt
 * sich nicht mit Gold besänftigen und greift jeden an, der ihm zu nahe kommt.
 */
public class BristleGiant extends PiglinBrute {
    public BristleGiant(EntityType<? extends PiglinBrute> type, Level level) {
        super(type, level);
    }
}
