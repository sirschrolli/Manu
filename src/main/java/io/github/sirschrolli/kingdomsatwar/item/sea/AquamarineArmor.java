package io.github.sirschrolli.kingdomsatwar.item.sea;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.SeaGear;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Die Taucherrüstung aus Aquamarin (seit 0.97): Jeder Teil hilft unter Wasser (über seine Attribute – der Helm lässt länger
 * atmen und schneller abbauen, der Harnisch mit den Luftflaschen lässt noch länger atmen, Beinschutz und Flossen lassen
 * schneller schwimmen). Wer den vollen Satz trägt, atmet unter Wasser (seit 0.99) und sieht dort klar (Nachtsicht, solange der
 * Kopf unter Wasser ist) – beides still, ohne Symbol.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class AquamarineArmor {
    /** So lange (Ticks) gibt die Rüstung die klare Sicht auf einmal (sie wird erneuert, solange der Kopf unter Wasser ist). */
    public static final int CLEAR_SIGHT = io.github.sirschrolli.kingdomsatwar.item.GearEffects.HOLD;
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private AquamarineArmor() {}

    /** Wie viele Teile der Taucherrüstung das Wesen trägt (0–4). */
    public static int pieces(LivingEntity entity) {
        List<DeferredItem<Item>> set = SeaGear.aquamarineSet();
        int count = 0;
        for (int i = 0; i < SLOTS.length; i++) {
            if (entity.getItemBySlot(SLOTS[i]).is(set.get(i).get())) {
                count++;
            }
        }
        return count;
    }

    /** Trägt das Wesen den vollen Satz? */
    public static boolean fullSet(LivingEntity entity) {
        return pieces(entity) == SLOTS.length;
    }

    /** Klare Sicht: mit dem vollen Satz unter Wasser Nachtsicht (ohne Teilchen; erneuert, bevor sie zu flackern beginnt). */
    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % 10 != 3) {
            return;
        }
        refresh(player);
    }

    /** Hat das Wesen die Augen unter Wasser? */
    public static boolean eyesInWater(LivingEntity entity) {
        return entity.level().getFluidState(BlockPos.containing(entity.getEyePosition())).is(FluidTags.WATER);
    }

    /**
     * Der volle Satz: Wasseratmung (seit 0.99) und, solange die Augen unter Wasser sind, klare Sicht (Nachtsicht) – beides still
     * (ohne Symbol) und sofort fort, wenn der Satz abgelegt wird oder der Kopf auftaucht (sie läuft nicht flackernd aus); ein
     * Trank bleibt unberührt. Gibt zurück, ob es klare Sicht hat.
     */
    public static boolean refresh(LivingEntity entity) {
        boolean full = fullSet(entity);
        boolean sight = full && eyesInWater(entity);
        io.github.sirschrolli.kingdomsatwar.item.GearEffects.set(entity, MobEffects.WATER_BREATHING, 0, "aquamarine", full);
        io.github.sirschrolli.kingdomsatwar.item.GearEffects.set(entity, MobEffects.NIGHT_VISION, 0, "aquamarine", sight);
        return sight;
    }
}
