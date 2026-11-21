package io.github.sirschrolli.kingdomsatwar.item;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Die Krone des Zerbrochenen Königs trägt ein wenig von der Schwerelosigkeit der Leere in sich: wer sie trägt und fällt,
 * sinkt sanft wie eine Feder (Schleichen lässt ihn normal fallen).
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class ShatteredCrownEffects {
    private ShatteredCrownEffects() {}

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % 5 != 0) {
            return;
        }
        boolean crown = player.getItemBySlot(EquipmentSlot.HEAD).is(VoidBlocks.SHATTERED_CROWN.get());
        boolean falling = crown && !player.onGround() && !player.isInWater() && !player.getAbilities().flying && !player.isFallFlying()
                && !player.isPassenger() && (player.getDeltaMovement().y < -0.35 && player.fallDistance > 2.5
                || player.hasEffect(MobEffects.SLOW_FALLING));
        // still (ohne Symbol); endet beim Aufsetzen, beim Schleichen und wenn die Krone abgelegt wird
        io.github.sirschrolli.kingdomsatwar.item.GearEffects.set(player, MobEffects.SLOW_FALLING, 0, "shattered_crown", falling && !player.isShiftKeyDown());
    }
}
