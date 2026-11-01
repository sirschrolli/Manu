package io.github.sirschrolli.kingdomsatwar.entity.creature.water;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.level.Level;

/**
 * Kaulquappe (statt Vanilla-Kaulquappe): rundlich, olivschwarz mit goldenen Flecken und durchscheinendem Flossensaum.
 * Wird mit der Zeit (oder mit Schleimbällen schneller) zum Frosch der Gegend.
 */
public class Polliwog extends Tadpole {
    public Polliwog(EntityType<? extends net.minecraft.world.entity.animal.fish.AbstractFish> type, Level level) {
        super(type, level);
    }
}
