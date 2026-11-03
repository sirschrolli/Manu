package io.github.sirschrolli.kingdomsatwar.entity.creature.water;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.level.Level;

/**
 * Tintenfisch (statt Vanilla-Tintenfisch): ziegelrot gemusterter Kalmar mit Mantelflossen, großen goldenen Augen und
 * Saugnäpfen an den Armen. Stößt Tinte aus und lässt Tintenbeutel fallen.
 */
public class Inkfish extends Squid implements ReplacesVanilla {
    public Inkfish(EntityType<? extends Inkfish> type, Level level) {
        super(type, level);
    }
}
