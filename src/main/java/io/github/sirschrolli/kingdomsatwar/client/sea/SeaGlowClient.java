package io.github.sirschrolli.kingdomsatwar.client.sea;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Meeresleuchten: Nachts leuchtet das Meer, wo es bewegt wird. Wer schwimmt, rudert oder taucht, zieht eine Spur aus
 * winzigen, türkis leuchtenden Punkten hinter sich her – Fische, Delfine und Boote ebenso; dazu glimmt die Oberfläche
 * hier und da, wo eine Welle bricht. Nur in Meeren und an Stränden der Oberwelt, nur im Dunkeln.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class SeaGlowClient {
    private SeaGlowClient() {}

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Player player = minecraft.player;
        if (level == null || player == null || minecraft.isPaused() || !level.dimensionType().hasSkyLight()) {
            return;
        }
        if (!dark(level, player.blockPosition())) {
            return;
        }
        BlockPos at = player.blockPosition();
        if (!level.getBiome(at).is(BiomeTags.IS_OCEAN) && !level.getBiome(at).is(BiomeTags.IS_BEACH)) {
            return;
        }
        RandomSource random = level.getRandom();
        // Spuren der Bewegten
        AABB area = player.getBoundingBox().inflate(24.0, 12.0, 24.0);
        for (Entity entity : level.getEntities(player, area, e -> e instanceof LivingEntity || e instanceof AbstractBoat)) {
            trail(level, entity, random);
        }
        trail(level, player, random);
        // die Oberfläche glimmt, wo Wellen brechen
        int sea = level.getSeaLevel();
        if (Math.abs(player.getY() - sea) < 24.0) {
            for (int i = 0; i < 3; i++) {
                double x = player.getX() + (random.nextDouble() - 0.5) * 36.0;
                double z = player.getZ() + (random.nextDouble() - 0.5) * 36.0;
                BlockPos surface = BlockPos.containing(x, sea - 1, z);
                if (level.getFluidState(surface).is(FluidTags.WATER) && level.getBlockState(surface.above()).isAir()
                        && random.nextFloat() < 0.6f) {
                    level.addParticle(ModParticles.SEA_GLOW.get(), x, sea - 0.05, z, (random.nextDouble() - 0.5) * 0.01, 0.0,
                            (random.nextDouble() - 0.5) * 0.01);
                }
            }
        }
    }

    /** Hinter einem Wesen, das sich im Wasser bewegt, leuchten Punkte auf – je schneller, desto mehr. */
    private static void trail(ClientLevel level, Entity entity, RandomSource random) {
        if (!entity.isInWater() && !(entity instanceof AbstractBoat)) {
            return;
        }
        Vec3 motion = entity.position().subtract(entity.xo, entity.yo, entity.zo);
        double speed = motion.length();
        if (speed < 0.03) {
            return;
        }
        int count = Math.min(5, (int) (speed * 14.0) + (random.nextFloat() < (speed * 14.0) % 1.0 ? 1 : 0));
        double width = Math.max(0.3, entity.getBbWidth());
        double height = entity instanceof AbstractBoat ? 0.3 : Math.min(1.2, entity.getBbHeight());
        for (int i = 0; i < count; i++) {
            double x = entity.getX() + (random.nextDouble() - 0.5) * width - motion.x * random.nextDouble() * 2.0;
            double y = entity.getY() + random.nextDouble() * height;
            double z = entity.getZ() + (random.nextDouble() - 0.5) * width - motion.z * random.nextDouble() * 2.0;
            if (level.getFluidState(BlockPos.containing(x, y, z)).is(FluidTags.WATER)) {
                level.addParticle(ModParticles.SEA_GLOW.get(), x, y, z, (random.nextDouble() - 0.5) * 0.02, (random.nextDouble() - 0.5) * 0.02,
                        (random.nextDouble() - 0.5) * 0.02);
            }
        }
    }

    /** Ist es hier dunkel genug (Nacht oder tief unten)? */
    private static boolean dark(ClientLevel level, BlockPos pos) {
        return level.isDarkOutside() || pos.getY() < level.getSeaLevel() - 24;
    }
}
