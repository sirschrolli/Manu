package io.github.sirschrolli.kingdomsatwar.entity.creature.monster;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.level.Level;

/**
 * Höhlenspinne: klein, dunkel blaugrün mit giftgrün leuchtender Zeichnung – vergiftet mit ihrem Biss wie die
 * Vanilla-Höhlenspinne.
 */
public class WildCaveSpider extends CaveSpider implements ReplacesVanilla {
    public WildCaveSpider(EntityType<? extends WildCaveSpider> type, Level level) {
        super(type, level);
    }
}
