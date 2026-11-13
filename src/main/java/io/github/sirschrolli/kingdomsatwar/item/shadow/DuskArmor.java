package io.github.sirschrolli.kingdomsatwar.item.shadow;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowGear;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Die Dämmerrüstung (seit 0.98), die Rüstung der Schleicher: Jeder Teil lässt Monster einen erst aus geringerer Entfernung
 * bemerken ({@link #PIECE_VISIBILITY}); wer den vollen Satz trägt und schleicht, wird fast nur noch aus nächster Nähe
 * entdeckt ({@link #FULL_SNEAK_VISIBILITY}). Beinlinge und Stiefel lassen schneller schleichen (über ihre Attribute), die
 * Stiefel fangen Stürze ab; die Kapuze schenkt beim Schleichen Nachtsicht ({@link #SIGHT}), die endet, sobald man aufsteht.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class DuskArmor {
    /** Je Teil sehen Monster einen nur auf so viel ihrer Reichweite (vier Teile: gut die Hälfte). */
    public static final double PIECE_VISIBILITY = 0.85;
    /** Mit dem vollen Satz beim Schleichen noch einmal so viel (zusammen mit dem Schleichen selbst gut ein Sechstel). */
    public static final double FULL_SNEAK_VISIBILITY = 0.4;
    /** Nachtsicht der Kapuze (Ticks) – beim Schleichen ständig erneuert, länger als zehn Sekunden, damit sie nicht flackert. */
    public static final int SIGHT = io.github.sirschrolli.kingdomsatwar.item.GearEffects.HOLD;
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private DuskArmor() {}

    /** Wie viele Teile der Dämmerrüstung das Wesen trägt (0–4). */
    public static int pieces(LivingEntity entity) {
        List<DeferredItem<Item>> set = ShadowGear.duskSet();
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

    /** Wie gut das Wesen wegen der Rüstung zu sehen ist (1 = wie sonst). */
    public static double visibility(LivingEntity entity) {
        int n = pieces(entity);
        double v = Math.pow(PIECE_VISIBILITY, n);
        if (n == SLOTS.length && entity.isShiftKeyDown()) {
            v *= FULL_SNEAK_VISIBILITY;
        }
        return v;
    }

    @SubscribeEvent
    static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        double v = visibility(event.getEntity());
        if (v < 1.0) {
            event.modifyVisibility(v);
        }
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % 5 != 2) {
            return;
        }
        refresh(player);
    }

    /**
     * Kapuze und Schleichen: Nachtsicht (ohne Teilchen). Steht das Wesen auf oder legt die Kapuze ab, endet sie – aber nur
     * die eigene, kurze Nachtsicht der Kapuze; ein Trank bleibt. Gibt zurück, ob die Kapuze gerade Nachtsicht schenkt.
     */
    public static boolean refresh(LivingEntity entity) {
        boolean sight = entity.getItemBySlot(EquipmentSlot.HEAD).is(ShadowGear.DUSK_HOOD.get()) && entity.isShiftKeyDown();
        io.github.sirschrolli.kingdomsatwar.item.GearEffects.set(entity, MobEffects.NIGHT_VISION, 0, "dusk_hood", sight);
        return sight;
    }
}
