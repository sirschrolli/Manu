package io.github.sirschrolli.kingdomsatwar.entity.boss;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.AnniTrollEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Lebenskraft über der Grenze von Minecraft: Ein Wesen darf höchstens 1.024 Lebenspunkte haben – die großen Gegner der Mod
 * haben mehr (der Zerbrochene König 1.800, Nyxar 1.400, Terragon 1.150, der Weltenfresser 2.600, auf „Schwer“ und mit
 * mehreren Spielern noch mehr). Ihre Leiste fasst nur 1.024; dafür zählt jeder Treffer – nach Rüstung und Härte –
 * entsprechend weniger. So halten sie genau so viel aus, wie für sie vorgesehen ist, und die Bossleiste zeigt trotzdem
 * den richtigen Anteil. Gilt für alle Wesen der Mod (Anni der Troll rechnet das schon selbst).
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class BossHealth {
    private BossHealth() {}

    /** Die Lebenskraft, die das Wesen ohne die Grenze von Minecraft hätte (Grundwert mit allen Änderungen). */
    public static double designed(LivingEntity entity) {
        AttributeInstance health = entity.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) {
            return entity.getMaxHealth();
        }
        double add = health.getBaseValue();
        double base = 0.0;
        double total = 1.0;
        for (AttributeModifier modifier : health.getModifiers()) {
            switch (modifier.operation()) {
                case ADD_VALUE -> add += modifier.amount();
                case ADD_MULTIPLIED_BASE -> base += modifier.amount();
                case ADD_MULTIPLIED_TOTAL -> total *= 1.0 + modifier.amount();
            }
        }
        return add * (1.0 + base) * total;
    }

    /** Wie stark Treffer zählen: 1, solange die volle Lebenskraft in die Leiste passt, sonst entsprechend weniger. */
    public static float scale(LivingEntity entity) {
        double designed = designed(entity);
        return designed <= entity.getMaxHealth() + 0.01 ? 1.0f : (float) (entity.getMaxHealth() / designed);
    }

    private static boolean ours(LivingEntity entity) {
        return KingdomsAtWar.MODID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace());
    }

    @SubscribeEvent
    static void onDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof AnniTrollEntity || entity.getMaxHealth() < 1000.0f || !ours(entity)) {
            return;
        }
        float scale = scale(entity);
        if (scale < 1.0f) {
            event.setNewDamage(event.getNewDamage() * scale);
        }
    }
}
