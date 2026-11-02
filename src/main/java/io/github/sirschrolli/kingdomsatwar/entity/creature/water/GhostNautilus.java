package io.github.sirschrolli.kingdomsatwar.entity.creature.water;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilus;
import net.minecraft.world.level.Level;

/**
 * Geisternautilus (statt Zombie-Nautilus): gebleichtes, rissiges Gehäuse mit Seepocken und Tang, faulende
 * graugrüne Fangarme und meergrün leuchtende Augen. Trägt Wasserleichen durch die Tiefe.
 */
public class GhostNautilus extends ZombieNautilus implements ReplacesVanilla {
    public GhostNautilus(EntityType<? extends GhostNautilus> type, Level level) {
        super(type, level);
    }
}
