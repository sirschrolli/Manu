package io.github.sirschrolli.kingdomsatwar.client.creator;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.creator.CreatorWings;
import io.github.sirschrolli.kingdomsatwar.network.CreatorBoostPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
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
 * Schwingen des Schöpfers im Spiel: Springen im Gleitflug zündet den Schwingenschlag (sofort, dann dem Server gemeldet), und
 * hinter den Schwingen aller Gleitenden ziehen goldene Federn und Lichtfunken her.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class CreatorWingsClient {
    private static boolean wasJumping;
    private static boolean wasFlying;

    private CreatorWingsClient() {}

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
        if (jumping && !wasJumping && flying && wasFlying && CreatorWings.wearing(player)) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!player.getCooldowns().isOnCooldown(chest)) {
                player.getCooldowns().addCooldown(chest, CreatorWings.BOOST_COOLDOWN);
                player.setDeltaMovement(CreatorWings.boostVelocity(player.getDeltaMovement(), player.getLookAngle()));
                player.playSound(ModSounds.SCHROLLI_WINGS.get(), 1.0f, 1.1f);
                burst(level, player);
                ClientPacketDistributor.sendToServer(CreatorBoostPayload.INSTANCE);
            }
        }
        wasJumping = jumping;
        wasFlying = flying;
        for (Player other : level.players()) {
            if (other.isFallFlying() && CreatorWings.wearing(other) && !(other == player && mc.options.getCameraType().isFirstPerson()
                    && player.getDeltaMovement().lengthSqr() < 0.04)) {
                trail(level, other);
            }
        }
    }

    /** Goldene Federn und Lichtfunken hinter den Schwingenspitzen. */
    private static void trail(ClientLevel level, Player player) {
        RandomSource random = level.getRandom();
        float yaw = player.yBodyRot * Mth.DEG_TO_RAD;
        double sideX = -Math.cos(yaw);
        double sideZ = -Math.sin(yaw);
        Vec3 motion = player.getDeltaMovement();
        for (int side = -1; side <= 1; side += 2) {
            double x = player.getX() + sideX * side * 1.1 - motion.x * 0.6;
            double y = player.getY() + 0.3 - motion.y * 0.6;
            double z = player.getZ() + sideZ * side * 1.1 - motion.z * 0.6;
            if (random.nextInt(2) == 0) {
                level.addParticle(ModParticles.DIVINE_SPARK.get(), x, y, z, (random.nextDouble() - 0.5) * 0.02, -0.01, (random.nextDouble() - 0.5) * 0.02);
            }
            if (random.nextInt(6) == 0) {
                level.addParticle(ModParticles.DIVINE_FEATHER.get(), x, y, z, 0.0, -0.02, 0.0);
            }
        }
    }

    /** Ein Kranz aus Federn und Licht hinter dem Gleitenden, wenn die Schwingen schlagen. */
    private static void burst(ClientLevel level, Player player) {
        RandomSource random = level.getRandom();
        Vec3 back = player.position().subtract(player.getLookAngle().scale(1.2)).add(0.0, 0.4, 0.0);
        for (int i = 0; i < 24; i++) {
            Vec3 d = new Vec3(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).normalize();
            level.addParticle(ParticleTypes.END_ROD, back.x, back.y, back.z, d.x * 0.16, d.y * 0.16, d.z * 0.16);
        }
        for (int i = 0; i < 10; i++) {
            level.addParticle(ModParticles.DIVINE_FEATHER.get(), back.x, back.y, back.z, (random.nextDouble() - 0.5) * 0.25,
                    (random.nextDouble() - 0.5) * 0.15, (random.nextDouble() - 0.5) * 0.25);
        }
    }
}
