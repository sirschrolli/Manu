package io.github.sirschrolli.kingdomsatwar.entity.creature.end;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.level.Level;

/**
 * Leermilbe (statt Endermite): ein Panzer aus dunklem Purpurkristall mit glimmenden Fugen, vier Punktaugen, Fühler und
 * Beißzangen, drei Beinpaare, Kristalldornen auf dem Rücken und ein leuchtender Stachel. Sie kriecht aus geworfenen
 * Enderperlen, lebt nur kurz und lockt die Leerwandler an.
 */
public class Leermilbe extends Endermite implements ReplacesVanilla {
    public Leermilbe(EntityType<? extends Endermite> type, Level level) {
        super(type, level);
    }
}
