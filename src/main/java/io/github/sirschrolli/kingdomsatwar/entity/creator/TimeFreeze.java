package io.github.sirschrolli.kingdomsatwar.entity.creator;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * „Die Zeit steht still“: Schrolli der Schöpfer hält die Zeit seiner Gegner an. Wer erstarrt ist, kann sich weder bewegen
 * noch springen, schwebt, wo er gerade war, und seine Schläge haben keine Kraft – umsehen kann er sich. Die Starre löst
 * sich von selbst (auch wenn der Schöpfer fällt oder das Wesen die Welt verlässt); die Änderungen werden nicht gespeichert.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class TimeFreeze {
    private static final Identifier ID = KingdomsAtWar.id("schrolli_timestop");
    /** Wer bis wann erstarrt ist (Spielzeit der Welt, in der er steht). */
    private static final Map<UUID, Long> FROZEN = new ConcurrentHashMap<>();

    private TimeFreeze() {}

    /** Lässt das Wesen für {@code ticks} Ticks erstarren. */
    public static void freeze(LivingEntity entity, int ticks) {
        FROZEN.put(entity.getUUID(), entity.level().getGameTime() + ticks);
        modify(entity, Attributes.MOVEMENT_SPEED);
        modify(entity, Attributes.JUMP_STRENGTH);
        modify(entity, Attributes.GRAVITY);
        modify(entity, Attributes.FLYING_SPEED);
        modify(entity, Attributes.ATTACK_DAMAGE);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.needsSync = true;
    }

    public static void unfreeze(LivingEntity entity) {
        FROZEN.remove(entity.getUUID());
        for (Holder<Attribute> attribute : java.util.List.of(Attributes.MOVEMENT_SPEED, Attributes.JUMP_STRENGTH, Attributes.GRAVITY,
                Attributes.FLYING_SPEED, Attributes.ATTACK_DAMAGE)) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance != null) {
                instance.removeModifier(ID);
            }
        }
    }

    public static boolean isFrozen(Entity entity) {
        return FROZEN.containsKey(entity.getUUID());
    }

    private static void modify(LivingEntity entity, Holder<Attribute> attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null && !instance.hasModifier(ID)) {
            instance.addTransientModifier(new AttributeModifier(ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @SubscribeEvent
    static void onTick(EntityTickEvent.Pre event) {
        if (FROZEN.isEmpty() || !(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide()) {
            return;
        }
        Long until = FROZEN.get(entity.getUUID());
        if (until == null) {
            return;
        }
        if (entity.level().getGameTime() >= until || !entity.isAlive()) {
            unfreeze(entity);
            return;
        }
        Vec3 v = entity.getDeltaMovement();
        if (v.lengthSqr() > 1.0e-6) {
            entity.setDeltaMovement(Vec3.ZERO);
            entity.needsSync = true;
        }
        entity.fallDistance = 0.0;
    }

    @SubscribeEvent
    static void onLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity entity && isFrozen(entity)) {
            unfreeze(entity);
        }
    }
}
