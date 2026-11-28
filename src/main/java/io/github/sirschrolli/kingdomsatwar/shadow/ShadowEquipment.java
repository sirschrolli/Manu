package io.github.sirschrolli.kingdomsatwar.shadow;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import io.github.sirschrolli.kingdomsatwar.world.ShadowGate;
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
    private ShadowEquipment() {}

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % 20 != 0) {
            return;
        }
        // still (ohne Symbol) und sofort fort, wenn der Helm abgelegt wird oder man die Schattenwelt verlässt
        io.github.sirschrolli.kingdomsatwar.item.GearEffects.set(player, MobEffects.NIGHT_VISION, 0, "shadow_helmet",
                player.level().dimension() == ShadowGate.SHADOW_WORLD && hasShadowSight(player));
    }

    /** Trägt der Spieler einen Helm, der in der Schattenwelt sehen lässt? */
    public static boolean hasShadowSight(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        return helmet.is(ShadowItems.NIGHTSTEEL_HELMET.get()) || helmet.is(ShadowItems.ABYSSITE_HELMET.get());
    }
}
