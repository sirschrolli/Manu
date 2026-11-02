package io.github.sirschrolli.kingdomsatwar.entity.creature.water;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.squid.GlowSquid;
import net.minecraft.world.level.Level;

/**
 * Tiefseekrake (statt Leuchttintenfisch): tief indigo und blaugrün mit leuchtenden türkisen Punkten und Ringen; lebt
 * in dunklen Unterwasserhöhlen und lässt Leuchttintenbeutel fallen.
 */
public class DeepKraken extends GlowSquid implements ReplacesVanilla {
    public DeepKraken(EntityType<? extends DeepKraken> type, Level level) {
        super(type, level);
    }
}
