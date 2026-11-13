package io.github.sirschrolli.kingdomsatwar.fae;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Die Feen-Elytren (Beute der Mottenkönigin): Sie gleiten wie Elytren, und im Gleitflug lässt ein Druck auf Springen sie
 * kräftig schlagen – ein Schub nach vorn und oben, alle zwei Sekunden (kostet einen Punkt Haltbarkeit). Prallt man im
 * Flug gegen eine Wand, fangen sie den Stoß ab. Der Flügelschlag wird im Spiel sofort ausgeführt (damit er sich
 * unmittelbar anfühlt) und dem Server gemeldet, der Abklingzeit, Haltbarkeit und Klang für alle anderen übernimmt.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class FaeElytra {
    /** Abklingzeit zwischen zwei Flügelschlägen (Ticks). */
    public static final int FLAP_COOLDOWN = 40;
    /** Höchstgeschwindigkeit nach einem Flügelschlag (Blöcke je Tick). */
    private static final double MAX_SPEED = 2.2;

    private FaeElytra() {}

    /** Trägt das Wesen heile Feen-Elytren? */
    public static boolean wearing(LivingEntity entity) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        return chest.is(FaeBlocks.FAE_ELYTRA.get()) && !chest.nextDamageWillBreak();
    }

    /** Geschwindigkeit nach einem Flügelschlag: kräftig in Blickrichtung und ein gutes Stück nach oben. */
    public static Vec3 flapVelocity(Vec3 motion, Vec3 look) {
        Vec3 next = motion.add(look.scale(0.75)).add(0.0, 0.42, 0.0);
        return next.length() > MAX_SPEED ? next.normalize().scale(MAX_SPEED) : next;
    }

    /** Auf dem Server: Flügelschlag bestätigen (Abklingzeit, Haltbarkeit, Klang und Funken für alle anderen). */
    public static void flap(ServerPlayer player) {
        if (!player.isFallFlying() || !wearing(player)) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (player.getCooldowns().isOnCooldown(chest)) {
            return;
        }
        player.getCooldowns().addCooldown(chest, FLAP_COOLDOWN);
        chest.hurtAndBreak(1, player, EquipmentSlot.CHEST);
        player.resetFallDistance();
        ServerLevel level = player.level();
        level.playSound(player, player.getX(), player.getY(), player.getZ(), ModSounds.FAE_ELYTRA_FLAP.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        level.sendParticles(ModParticles.FAE_SPARK.get(), player.getX(), player.getY(0.5), player.getZ(), 26, 0.9, 0.25, 0.9, 0.05);
    }

    /** Wer Feen-Elytren trägt, nimmt beim Aufprall im Flug keinen Schaden. */
    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().is(DamageTypes.FLY_INTO_WALL) && wearing(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}
