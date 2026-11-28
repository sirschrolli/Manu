package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.level.Level;

/**
 * Dörrgebein (statt Vertrocknetes Skelett): sonnengebleichte Knochen in sandfarbenen Wüstenbinden mit vertrockneten
 * Sehnen. Verhält sich wie das Vanilla-Gegenstück.
 */
public class DryBones extends Parched implements ReplacesVanilla {
    public DryBones(EntityType<? extends DryBones> type, Level level) {
        super(type, level);
    }
}
