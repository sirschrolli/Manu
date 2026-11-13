package io.github.sirschrolli.kingdomsatwar.item.glitch;

import io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Die Fehlerflügel (Beute Annis): Sie gleiten wie Elytren, und im Gleitflug lässt ein Druck auf Springen den Gleitenden
 * blinzeln – er springt acht Blöcke in Blickrichtung, ohne den Raum dazwischen zu durchqueren (nicht durch Wände), und
 * fliegt mit seinem Schwung weiter. Alle zwei Sekunden, kostet einen Punkt Haltbarkeit. Hinter den Flügeln ziehen
 * Farbsäume her. Der Sprung geschieht auf dem Server (der Spieler meldet nur den Druck).
 */
public final class GlitchWings {
    public static final int BLINK_COOLDOWN = 40;
    public static final double BLINK_DISTANCE = 8.0;

    private GlitchWings() {}

    /** Trägt das Wesen heile Fehlerflügel? */
    public static boolean wearing(LivingEntity entity) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        return chest.is(AnniBlocks.GLITCH_WINGS.get()) && !chest.nextDamageWillBreak();
    }

    /** Wohin der Sprung führt: acht Blöcke in Blickrichtung, vor einer Wand endet er davor. */
    public static Vec3 blinkTarget(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 to = eye.add(player.getLookAngle().scale(BLINK_DISTANCE));
        HitResult hit = player.level().clip(new ClipContext(eye, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 end = hit.getType() == HitResult.Type.MISS ? to : hit.getLocation().subtract(player.getLookAngle().scale(0.8));
        return end.subtract(0.0, player.getEyeHeight(), 0.0);
    }

    /** Auf dem Server: Blinzeln (Abklingzeit, Haltbarkeit, Klang und Pixel an beiden Enden). */
    public static void blink(ServerPlayer player) {
        if (!player.isFallFlying() || !wearing(player)) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (player.getCooldowns().isOnCooldown(chest)) {
            return;
        }
        ServerLevel level = player.level();
        Vec3 from = player.position();
        Vec3 to = blinkTarget(player);
        if (to.distanceToSqr(from) < 4.0 || !level.noCollision(player, player.getBoundingBox().move(to.subtract(from)))) {
            return;
        }
        player.getCooldowns().addCooldown(chest, BLINK_COOLDOWN);
        chest.hurtAndBreak(1, player, EquipmentSlot.CHEST);
        player.resetFallDistance();
        level.sendParticles(ModParticles.GLITCH_PIXEL.get(), from.x, from.y + 0.9, from.z, 30, 0.4, 0.6, 0.4, 0.06);
        player.teleportTo(level, to.x, to.y, to.z, Set.of(Relative.DELTA_X, Relative.DELTA_Y, Relative.DELTA_Z, Relative.X_ROT, Relative.Y_ROT),
                0.0f, 0.0f, false);
        level.sendParticles(ModParticles.GLITCH_PIXEL.get(), to.x, to.y + 0.9, to.z, 30, 0.4, 0.6, 0.4, 0.06);
        level.playSound(null, to.x, to.y, to.z, ModSounds.GLITCH_WINGS_BLINK.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
