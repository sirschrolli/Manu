package io.github.sirschrolli.kingdomsatwar.entity.creature.water;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.fish.Pufferfish;
import net.minecraft.world.level.Level;

/**
 * Kugelfisch (statt Vanilla-Kugelfisch): ockergelb mit braunen Sattelflecken und weißem Bauch; bläht sich in zwei
 * Stufen auf, die Stacheln stellen sich dabei immer weiter auf. Vergiftet, wer ihm zu nahe kommt.
 */
public class Puffer extends Pufferfish {
    public Puffer(EntityType<? extends Puffer> type, Level level) {
        super(type, level);
    }
}
