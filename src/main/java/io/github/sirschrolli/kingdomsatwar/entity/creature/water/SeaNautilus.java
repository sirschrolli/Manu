package io.github.sirschrolli.kingdomsatwar.entity.creature.water;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.nautilus.Nautilus;
import net.minecraft.world.level.Level;

/**
 * Nautilus (statt Vanilla-Nautilus): cremefarbenes Gehäuse mit rostroten Tigerstreifen und perlmuttner Mündung,
 * zarte rosagraue Fangarme. Lässt sich zähmen, satteln, panzern und unter Wasser reiten.
 */
public class SeaNautilus extends Nautilus implements ReplacesVanilla {
    public SeaNautilus(EntityType<? extends SeaNautilus> type, Level level) {
        super(type, level);
    }
}
