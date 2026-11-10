package io.github.sirschrolli.kingdomsatwar.entity.abyss;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Der Weltenfresser hat mehr Lebenskraft, als Minecraft einem Wesen erlaubt (höchstens 1024): Jeder Treffer zählt nach
 * Rüstung und Härte entsprechend weniger – so hält er so viel aus wie mit {@value WorldEaterEntity#DESIGNED_HEALTH}
 * Lebenspunkten, und die Bossleiste zeigt trotzdem den richtigen Anteil.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
final class WorldEaterDamage {
    private WorldEaterDamage() {}

    @SubscribeEvent
    static void onDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof WorldEaterEntity eater) {
            event.setNewDamage(event.getNewDamage() * eater.healthScale());
        }
    }
}
