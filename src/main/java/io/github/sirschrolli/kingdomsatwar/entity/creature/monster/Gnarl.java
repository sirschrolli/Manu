package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.level.Level;

/**
 * Knorz (statt Knarz): eine hagere, knorrige Gestalt aus rissiger Rinde mit Moos und Flechten, Wurzelfüßen und
 * Astfingern; in den tiefen Augenhöhlen glimmt es glutrot. Erstarrt, sobald man hinsieht, und ist an sein Knarzherz
 * gebunden wie der Vanilla-Knarz.
 */
public class Gnarl extends Creaking implements ReplacesVanilla {
    public Gnarl(EntityType<? extends Gnarl> type, Level level) {
        super(type, level);
    }
}
