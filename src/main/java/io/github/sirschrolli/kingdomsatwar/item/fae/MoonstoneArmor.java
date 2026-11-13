package io.github.sirschrolli.kingdomsatwar.item.fae;

import io.github.sirschrolli.kingdomsatwar.registry.FaeGear;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Die Mondsteinrüstung speichert Mondlicht (seit 0.96): Jeder getragene Teil fasst {@link #MANA_PER_PIECE} Mana mehr und lässt
 * es um {@link #REGEN_PER_PIECE} schneller zurückkehren; der volle Satz noch einmal {@link #SET_MANA} Mana und
 * {@link #SET_REGEN} mehr – zusammen 80 Mana und die Hälfte schnellere Erholung. Wirkt mit {@link
 * io.github.sirschrolli.kingdomsatwar.magic.Mana} (auch im Spiel des Spielers, für die Anzeige).
 */
public final class MoonstoneArmor {
    public static final float MANA_PER_PIECE = 15.0f;
    public static final float REGEN_PER_PIECE = 0.10f;
    public static final float SET_MANA = 20.0f;
    public static final float SET_REGEN = 0.10f;
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private MoonstoneArmor() {}

    /** Wie viele Teile der Mondsteinrüstung das Wesen trägt (0–4). */
    public static int pieces(LivingEntity entity) {
        List<DeferredItem<net.minecraft.world.item.Item>> set = FaeGear.moonstoneSet();
        int count = 0;
        for (int i = 0; i < SLOTS.length; i++) {
            if (entity.getItemBySlot(SLOTS[i]).is(set.get(i).get())) {
                count++;
            }
        }
        return count;
    }

    /** So viel Mana fasst man mehr. */
    public static float bonusMana(LivingEntity entity) {
        int pieces = pieces(entity);
        return pieces * MANA_PER_PIECE + (pieces == SLOTS.length ? SET_MANA : 0.0f);
    }

    /** So viel schneller kehrt das Mana zurück (1 = wie ohne). */
    public static float regenFactor(LivingEntity entity) {
        int pieces = pieces(entity);
        return 1.0f + pieces * REGEN_PER_PIECE + (pieces == SLOTS.length ? SET_REGEN : 0.0f);
    }
}
