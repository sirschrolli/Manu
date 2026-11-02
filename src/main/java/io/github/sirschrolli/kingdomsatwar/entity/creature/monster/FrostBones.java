package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.level.Level;

/**
 * Frostgebein (statt Eiswanderer): bereifte Knochen mit Eiskrusten, ein zerfetzter Pelzumhang und eisblau leuchtende
 * Augen. Schießt Pfeile der Langsamkeit, ist kältefest und entsteht aus Gerippen im Pulverschnee.
 */
public class FrostBones extends Stray implements ReplacesVanilla {
    public FrostBones(EntityType<? extends FrostBones> type, Level level) {
        super(type, level);
    }
}
