package io.github.sirschrolli.kingdomsatwar.shadow;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import io.github.sirschrolli.kingdomsatwar.world.ShadowGate;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Besondere Kräfte der Schattenwelt-Ausrüstung: Helme aus Nachtstahl oder Abyssit lassen in der Schattenwelt sehen wie am
 * Tag (Nachtsicht, ohne das Flackern am Ende). Die Abyssit-Stiefel fangen Stürze über ihre Eigenschaften ab.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class ShadowEquipment {
    /** Dauer der Nachtsicht; sie wird erneuert, bevor sie zu flackern beginnt (unter 200 Ticks). */
    private static final int NIGHT_VISION_TICKS = 300;

    private ShadowEquipment() {}

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % 20 != 0) {
            return;
        }
        if (player.level().dimension() != ShadowGate.SHADOW_WORLD || !hasShadowSight(player)) {
            return;
        }
        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
        if (current == null || current.getDuration() < 240) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, NIGHT_VISION_TICKS, 0, true, false, true));
        }
    }

    /** Trägt der Spieler einen Helm, der in der Schattenwelt sehen lässt? */
    public static boolean hasShadowSight(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        return helmet.is(ShadowItems.NIGHTSTEEL_HELMET.get()) || helmet.is(ShadowItems.ABYSSITE_HELMET.get());
    }
}
