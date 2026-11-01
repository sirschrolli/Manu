package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.level.Level;

/**
 * Windgeist (statt Brise): eine verwitterte Steinmaske in einer Kapuze aus Wind, umkreist von drei Runensteinen,
 * unter sich ein Wirbel aus Luft und Blättern. Springt, weicht aus und schießt Windkugeln wie die Vanilla-Brise.
 */
public class WindSpirit extends Breeze {
    public WindSpirit(EntityType<? extends WindSpirit> type, Level level) {
        super(type, level);
    }
}
