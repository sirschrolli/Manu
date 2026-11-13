package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.level.Level;

/**
 * Riffwächter (statt Wächter): ein gepanzerter Rifffisch mit Knochenplatten, Korallenbewuchs, gebänderten Stacheln
 * und einem großen Bernsteinauge. Bewacht den Korallenpalast und schießt seinen Strahl wie der Vanilla-Wächter.
 */
public class ReefGuardian extends Guardian implements ReplacesVanilla {
    public ReefGuardian(EntityType<? extends ReefGuardian> type, Level level) {
        super(type, level);
    }
}
