package io.github.sirschrolli.kingdomsatwar.creator;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Die Schwingen des Schöpfers (Beute Schrollis): die mächtigsten aller Schwingen. Sie gleiten wie Elytren und zerbrechen
 * nie; im Gleitflug zündet ein Druck auf Springen den <b>Schwingenschlag</b> – ein Stoß nach vorn, stärker als jede Rakete,
 * alle anderthalb Sekunden (er kostet nichts). Hinter den Schwingen ziehen goldene Federn und Lichtfunken her. Wer sie
 * trägt, nimmt weder Fall- noch Aufprallschaden – die Schwingen fangen ihn auf. Der Schlag wird im Spiel sofort ausgeführt
 * und dem Server gemeldet, der Abklingzeit, Klang und Licht für alle anderen übernimmt.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class CreatorWings {
    /** Abklingzeit zwischen zwei Schwingenschlägen (Ticks). */
    public static final int BOOST_COOLDOWN = 30;
    /** Höchstgeschwindigkeit nach einem Schlag (Blöcke je Tick). */
    private static final double MAX_SPEED = 3.2;

    private CreatorWings() {}

    /** Trägt das Wesen die Schwingen des Schöpfers? */
    public static boolean wearing(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST).is(CreatorBlocks.CREATOR_WINGS.get());
    }

    /** Abklingzeit des Schwingenschlags: als Ebenbild des Schöpfers (alle drei Teile) nur halb so lang. */
    public static int cooldown(LivingEntity entity) {
        return CreatorGear.image(entity) ? BOOST_COOLDOWN / 2 : BOOST_COOLDOWN;
    }

    /** Geschwindigkeit nach einem Schlag: kräftig in Blickrichtung, dazu ein wenig Auftrieb. */
    public static Vec3 boostVelocity(Vec3 motion, Vec3 look) {
        Vec3 next = motion.add(look.scale(1.55)).add(0.0, 0.12, 0.0);
        return next.length() > MAX_SPEED ? next.normalize().scale(MAX_SPEED) : next;
    }

    /** Auf dem Server: Schlag bestätigen (Abklingzeit, Klang, Federn und Licht für alle anderen). */
    public static boolean boost(ServerPlayer player) {
        if (!player.isFallFlying() || !wearing(player)) {
            return false;
        }
        var chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (player.getCooldowns().isOnCooldown(chest)) {
            return false;
        }
        player.getCooldowns().addCooldown(chest, cooldown(player));
        player.resetFallDistance();
        ServerLevel level = player.level();
        level.playSound(player, player.getX(), player.getY(), player.getZ(), ModSounds.SCHROLLI_WINGS.get(), SoundSource.PLAYERS, 1.2f, 1.1f);
        level.sendParticles(ModParticles.DIVINE_FEATHER.get(), player.getX(), player.getY(0.5), player.getZ(), 18, 0.9, 0.3, 0.9, 0.06);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY(0.5), player.getZ(), 20, 0.8, 0.3, 0.8, 0.08);
        return true;
    }

    /** Die Schwingen fangen ihren Träger auf: kein Fall- und kein Aufprallschaden. */
    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        var source = event.getSource();
        if ((source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypes.FLY_INTO_WALL)) && wearing(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}
