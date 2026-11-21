package io.github.sirschrolli.kingdomsatwar.entity.creature.end;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;

/**
 * Schreinling (statt Shulker): ein bleiches Wesen mit einem einzigen großen Auge und zwei Fühlern, das in einem
 * geschnitzten Reliquienschrein aus Stein haust – Bogennischen mit glimmenden Runen, ein Rautenfries, oben ein
 * achtzackiger Stern mit Medaillon und Seelenstein, Eckbeschläge aus angelaufener Bronze und Zähne unter dem
 * Deckelrand. Er tut, was der Shulker tut: klammert sich an jede Seite, späht, feuert Schreinfunken, die schweben lassen,
 * springt fort, lässt Schalen fallen und lässt sich färben.
 */
public class Schreinling extends Shulker implements ReplacesVanilla {
    public Schreinling(EntityType<? extends Shulker> type, Level level) {
        super(type, level);
    }
}
