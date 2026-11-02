package io.github.sirschrolli.kingdomsatwar.entity.creature.golem;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.level.Level;

/**
 * Kupferknecht (statt Kupfergolem): ein kleiner Diener aus getriebenem Kupferblech mit Kesselkopf, Glasaugen,
 * Zeigerwerk auf der Brust und einem Aufziehschlüssel auf dem Rücken. Gebaut wird er wie der Vanilla-Golem (ein
 * Kupferblock und ein geschnitzter Kürbis, der Kupferblock wird zur Kupfertruhe); er trägt Gegenstände aus
 * Kupfertruhen in die Truhen ringsum, läuft mit der Zeit an und wird grün, lässt sich mit Wachs schützen und mit der
 * Axt abschleifen, ein Blitz macht ihn wieder blank. Ganz oxidiert erstarrt er irgendwann zur Statue.
 */
public class CopperServant extends CopperGolem {
    public CopperServant(EntityType<? extends CopperServant> type, Level level) {
        super(type, level);
    }
}
