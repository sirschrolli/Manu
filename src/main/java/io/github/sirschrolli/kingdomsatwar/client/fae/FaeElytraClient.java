package io.github.sirschrolli.kingdomsatwar.client.fae;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.fae.FaeElytra;
import io.github.sirschrolli.kingdomsatwar.network.FaeFlapPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
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
 * Feen-Elytren im Spiel: Springen im Gleitflug löst den Flügelschlag aus (sofort, dann dem Server gemeldet), und hinter
 * den Flügelspitzen aller Gleitenden zieht eine Spur aus Feenfunken her.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class FaeElytraClient {
    private static boolean wasJumping;
    private static boolean wasFlying;

    private FaeElytraClient() {}

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
        if (jumping && !wasJumping && flying && wasFlying && FaeElytra.wearing(player)) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!player.getCooldowns().isOnCooldown(chest)) {
                player.getCooldowns().addCooldown(chest, FaeElytra.FLAP_COOLDOWN);
                player.setDeltaMovement(FaeElytra.flapVelocity(player.getDeltaMovement(), player.getLookAngle()));
                player.playSound(ModSounds.FAE_ELYTRA_FLAP.get(), 1.0f, 1.0f);
                burst(level, player);
                ClientPacketDistributor.sendToServer(FaeFlapPayload.INSTANCE);
            }
        }
        wasJumping = jumping;
        wasFlying = flying;
        for (Player other : level.players()) {
            if (other.isFallFlying() && FaeElytra.wearing(other) && !(other == player && mc.options.getCameraType().isFirstPerson()
                    && player.getDeltaMovement().lengthSqr() < 0.04)) {
                trail(level, other);
            }
        }
    }

    /** Funkenspur hinter den Flügelspitzen. */
    private static void trail(ClientLevel level, Player player) {
        RandomSource random = level.getRandom();
        float yaw = player.yBodyRot * Mth.DEG_TO_RAD;
        double sideX = -Math.cos(yaw);
        double sideZ = -Math.sin(yaw);
        Vec3 motion = player.getDeltaMovement();
        for (int side = -1; side <= 1; side += 2) {
            if (random.nextInt(3) == 0) {
                continue;
            }
            double x = player.getX() + sideX * side * 0.95 - motion.x * 0.6;
            double y = player.getY() + 0.25 - motion.y * 0.6;
            double z = player.getZ() + sideZ * side * 0.95 - motion.z * 0.6;
            level.addParticle(ModParticles.FAE_SPARK.get(), x, y, z, (random.nextDouble() - 0.5) * 0.02, -0.01, (random.nextDouble() - 0.5) * 0.02);
        }
    }

    /** Ein Wirbel aus Funken beim Flügelschlag. */
    private static void burst(ClientLevel level, Player player) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < 18; i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double r = 0.6 + random.nextDouble() * 0.7;
            level.addParticle(ModParticles.FAE_SPARK.get(), player.getX() + Math.cos(a) * r, player.getY() + 0.3, player.getZ() + Math.sin(a) * r,
                    Math.cos(a) * 0.06, -0.03, Math.sin(a) * 0.06);
        }
    }
}
