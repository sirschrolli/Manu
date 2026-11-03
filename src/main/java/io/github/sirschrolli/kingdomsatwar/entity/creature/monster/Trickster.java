package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.level.Level;

/**
 * Gaukler (statt Illusionist): ein Narr mit zweizipfliger Schellenkappe, weißer Halbmaske und Harlekin-Rauten.
 * Schießt mit dem Bogen, blendet, macht sich unsichtbar und narrt mit Trugbildern seiner selbst.
 */
public class Trickster extends Illusioner implements ReplacesVanilla {
    public Trickster(EntityType<? extends Trickster> type, Level level) {
        super(type, level);
    }

    /** Hebt die Arme zum Zauber (wie wenn er blendet) – für Vorführungen und Tests. */
    public void showCasting() {
        this.setIsCastingSpell(IllagerSpell.BLINDNESS);
    }
}
