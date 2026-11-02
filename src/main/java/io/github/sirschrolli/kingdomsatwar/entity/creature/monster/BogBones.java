package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.level.Level;

/**
 * Moorgebein (statt Sumpfskelett): bemoost, schlammig, von Ranken umwunden, mit roten und braunen Pilzen auf dem
 * Schädel (die man ihm abscheren kann). Schießt Giftpfeile wie das Vanilla-Sumpfskelett.
 */
public class BogBones extends Bogged implements ReplacesVanilla {
    public BogBones(EntityType<? extends BogBones> type, Level level) {
        super(type, level);
    }
}
