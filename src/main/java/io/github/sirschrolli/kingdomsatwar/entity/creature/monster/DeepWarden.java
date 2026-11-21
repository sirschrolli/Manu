package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.Level;

/**
 * Tiefenhüter (statt Wärter): ein blinder Koloss der tiefen Dunkelheit mit schiefergrauer Steinhaut, einem Käfig aus
 * bleichen Rippen um ein seelenblau leuchtendes Herz und leuchtenden Adern. Taucht aus dem Boden auf, wittert,
 * brüllt und greift mit dem Schallschlag an wie der Vanilla-Wärter.
 */
public class DeepWarden extends Warden implements ReplacesVanilla {
    public DeepWarden(EntityType<? extends DeepWarden> type, Level level) {
        super(type, level);
    }
}
