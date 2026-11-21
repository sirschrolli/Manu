package io.github.sirschrolli.kingdomsatwar.entity.creature.water;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.level.Level;

/**
 * Molch (statt Axolotl): fünf eigene Arten an Stelle der fünf Vanilla-Farben – blassrosa Grottenolm, gefleckter
 * Wildmolch, Goldmolch, Eismolch und der seltene Blaumolch mit leuchtenden Kiemen. Jagt mit dem Spieler unter Wasser,
 * stellt sich tot und kommt in den Eimer wie das Axolotl.
 */
public class Newt extends Axolotl implements ReplacesVanilla {
    public Newt(EntityType<? extends Newt> type, Level level) {
        super(type, level);
    }
}
