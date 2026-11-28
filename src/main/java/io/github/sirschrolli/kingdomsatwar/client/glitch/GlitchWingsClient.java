package io.github.sirschrolli.kingdomsatwar.client.glitch;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.item.glitch.GlitchWings;
import io.github.sirschrolli.kingdomsatwar.network.GlitchBlinkPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.DustParticleOptions;
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
 * Fehlerflügel im Spiel: Springen im Gleitflug meldet dem Server das Blinzeln (der springt), und hinter den Flügeln aller
 * Gleitenden ziehen Farbsäume (Magenta und Cyan) und hier und da ein Pixel her.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class GlitchWingsClient {
    private static final DustParticleOptions MAGENTA = new DustParticleOptions(0xF800F8, 0.9f);
    private static final DustParticleOptions CYAN = new DustParticleOptions(0x20E8FF, 0.9f);
    private static boolean wasJumping;
    private static boolean wasFlying;

    private GlitchWingsClient() {}

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
        if (jumping && !wasJumping && flying && wasFlying && GlitchWings.wearing(player)) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!player.getCooldowns().isOnCooldown(chest)) {
                ClientPacketDistributor.sendToServer(GlitchBlinkPayload.INSTANCE);
            }
        }
        wasJumping = jumping;
        wasFlying = flying;
        for (Player other : level.players()) {
            if (other.isFallFlying() && GlitchWings.wearing(other) && !(other == player && mc.options.getCameraType().isFirstPerson()
                    && player.getDeltaMovement().lengthSqr() < 0.04)) {
                trail(level, other);
            }
        }
    }

    /** Farbsäume hinter den Flügelspitzen: links Magenta, rechts Cyan; dazwischen hier und da ein Pixel. */
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
            level.addParticle(side < 0 ? MAGENTA : CYAN, x, y, z, 0.0, 0.0, 0.0);
        }
        if (random.nextInt(4) == 0) {
            level.addParticle(ModParticles.GLITCH_PIXEL.get(), player.getX() - motion.x, player.getY() + 0.3 - motion.y, player.getZ() - motion.z,
                    0.0, 0.0, 0.0);
        }
    }
}
