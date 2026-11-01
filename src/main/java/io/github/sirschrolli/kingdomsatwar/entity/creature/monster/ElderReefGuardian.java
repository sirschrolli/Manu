package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.level.Level;

/**
 * Uralter Riffwächter (statt Großer Wächter): riesig, elfenbeinbleich und von Seepocken und Tang überwachsen, mit
 * türkis leuchtendem Auge. Hütet die Halle des Korallenpalasts, belegt Eindringlinge mit Abbaulähmung und lässt
 * nasse Schwämme fallen.
 */
public class ElderReefGuardian extends ElderGuardian {
    public ElderReefGuardian(EntityType<? extends ElderReefGuardian> type, Level level) {
        super(type, level);
    }
}
