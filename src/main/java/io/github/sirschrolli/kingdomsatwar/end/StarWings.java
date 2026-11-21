package io.github.sirschrolli.kingdomsatwar.end;

import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.StarBlocks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Die Sternenschwingen (Beute Asterias): Sie gleiten wie Elytren, und im Gleitflug zündet ein Druck auf Springen den
 * Sternenschub – ein Stoß nach vorn wie von einer Feuerwerksrakete, alle drei Sekunden (kostet einen Punkt Haltbarkeit).
 * Hinter den Schwingen zieht eine Spur aus Sternenlicht her. Der Schub wird im Spiel sofort ausgeführt (damit er sich
 * unmittelbar anfühlt) und dem Server gemeldet, der Abklingzeit, Haltbarkeit und Klang für alle anderen übernimmt.
 */
public final class StarWings {
    /** Abklingzeit zwischen zwei Schüben (Ticks). */
    public static final int BOOST_COOLDOWN = 60;
    /** Höchstgeschwindigkeit nach einem Schub (Blöcke je Tick). */
    private static final double MAX_SPEED = 2.8;

    private StarWings() {}

    /** Trägt das Wesen heile Sternenschwingen? */
    public static boolean wearing(LivingEntity entity) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        return chest.is(StarBlocks.STAR_WINGS.get()) && !chest.nextDamageWillBreak();
    }

    /** Geschwindigkeit nach einem Schub: kräftig in Blickrichtung (wie eine Rakete). */
    public static Vec3 boostVelocity(Vec3 motion, Vec3 look) {
        Vec3 next = motion.add(look.scale(1.25));
        return next.length() > MAX_SPEED ? next.normalize().scale(MAX_SPEED) : next;
    }

    /** Auf dem Server: Schub bestätigen (Abklingzeit, Haltbarkeit, Klang und Sternenlicht für alle anderen). */
    public static void boost(ServerPlayer player) {
        if (!player.isFallFlying() || !wearing(player)) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (player.getCooldowns().isOnCooldown(chest)) {
            return;
        }
        player.getCooldowns().addCooldown(chest, BOOST_COOLDOWN);
        chest.hurtAndBreak(1, player, EquipmentSlot.CHEST);
        player.resetFallDistance();
        ServerLevel level = player.level();
        level.playSound(player, player.getX(), player.getY(), player.getZ(), ModSounds.STAR_WINGS_BOOST.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY(0.5), player.getZ(), 24, 0.8, 0.3, 0.8, 0.08);
        level.sendParticles(ParticleTypes.FIREWORK, player.getX(), player.getY(0.5), player.getZ(), 12, 0.5, 0.2, 0.5, 0.05);
    }
}
