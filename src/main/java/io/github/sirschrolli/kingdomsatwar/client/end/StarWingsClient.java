package io.github.sirschrolli.kingdomsatwar.client.end;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.end.StarWings;
import io.github.sirschrolli.kingdomsatwar.network.StarBoostPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Sternenschwingen im Spiel: Springen im Gleitflug zündet den Sternenschub (sofort, dann dem Server gemeldet), und hinter
 * den Schwingen aller Gleitenden zieht eine Spur aus Sternenlicht her.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class StarWingsClient {
    private static boolean wasJumping;
    private static boolean wasFlying;

    private StarWingsClient() {}

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null || mc.isPaused()) {
            wasJumping = false;
            wasFlying = false;
            return;
        }
        boolean jumping = player.input.keyPresses.jump();
        boolean flying = player.isFallFlying();
        // nur ein neuer Druck, und erst wenn man schon gleitet (der Druck, der das Gleiten startet, zählt nicht)
        if (jumping && !wasJumping && flying && wasFlying && StarWings.wearing(player)) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!player.getCooldowns().isOnCooldown(chest)) {
                player.getCooldowns().addCooldown(chest, StarWings.BOOST_COOLDOWN);
                player.setDeltaMovement(StarWings.boostVelocity(player.getDeltaMovement(), player.getLookAngle()));
                player.playSound(ModSounds.STAR_WINGS_BOOST.get(), 1.0f, 1.0f);
                burst(level, player);
                ClientPacketDistributor.sendToServer(StarBoostPayload.INSTANCE);
            }
        }
        wasJumping = jumping;
        wasFlying = flying;
        for (Player other : level.players()) {
            if (other.isFallFlying() && StarWings.wearing(other) && !(other == player && mc.options.getCameraType().isFirstPerson()
                    && player.getDeltaMovement().lengthSqr() < 0.04)) {
                trail(level, other);
            }
        }
    }

    /** Sternenlicht hinter den Schwingenspitzen, dazwischen hier und da ein Funke. */
    private static void trail(ClientLevel level, Player player) {
        RandomSource random = level.getRandom();
        float yaw = player.yBodyRot * Mth.DEG_TO_RAD;
        double sideX = -Math.cos(yaw);
        double sideZ = -Math.sin(yaw);
        Vec3 motion = player.getDeltaMovement();
        for (int side = -1; side <= 1; side += 2) {
            if (random.nextInt(2) == 0) {
                continue;
            }
            double x = player.getX() + sideX * side * 1.0 - motion.x * 0.6;
            double y = player.getY() + 0.25 - motion.y * 0.6;
            double z = player.getZ() + sideZ * side * 1.0 - motion.z * 0.6;
            level.addParticle(ParticleTypes.END_ROD, x, y, z, (random.nextDouble() - 0.5) * 0.02, -0.01, (random.nextDouble() - 0.5) * 0.02);
        }
        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.FIREWORK, player.getX() - motion.x, player.getY() + 0.3 - motion.y, player.getZ() - motion.z,
                    0.0, 0.0, 0.0);
        }
    }

    /** Ein Stern zerspringt hinter dem Gleitenden, wenn der Schub zündet. */
    private static void burst(ClientLevel level, Player player) {
        RandomSource random = level.getRandom();
        Vec3 back = player.position().subtract(player.getLookAngle().scale(1.2)).add(0.0, 0.4, 0.0);
        for (int i = 0; i < 26; i++) {
            Vec3 d = new Vec3(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).normalize();
            level.addParticle(ParticleTypes.END_ROD, back.x, back.y, back.z, d.x * 0.18, d.y * 0.18, d.z * 0.18);
        }
        for (int i = 0; i < 8; i++) {
            level.addParticle(ParticleTypes.FIREWORK, back.x, back.y, back.z, (random.nextDouble() - 0.5) * 0.2, (random.nextDouble() - 0.5) * 0.2,
                    (random.nextDouble() - 0.5) * 0.2);
        }
    }
}
