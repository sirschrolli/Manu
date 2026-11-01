package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.minecraft.world.level.Level;

/**
 * Schwefelwürfel (statt Vanilla-Schwefelwürfel): bernsteinfarbenes Gelee mit Schwefelkristallen und Bläschen. Schluckt
 * Blöcke, lässt sich mit dem Eimer fangen und scheren – wie das Vanilla-Gegenstück.
 */
public class BrimstoneCube extends SulfurCube {
    public BrimstoneCube(EntityType<? extends BrimstoneCube> type, Level level) {
        super(type, level);
    }
}
