package io.github.sirschrolli.kingdomsatwar.client.boss;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.network.ScreenShakePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Bildschirmbeben (Boss erwacht, stampft, stürzt): leichtes Zittern von Blickrichtung und Neigung. */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class ScreenShake {
    private static float strength;
    private static int ticksLeft;
    private static int totalTicks;

    private ScreenShake() {}

    @SubscribeEvent
    static void registerHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(ScreenShakePayload.TYPE, ScreenShake::onShake);
    }

    private static void onShake(ScreenShakePayload payload, IPayloadContext context) {
        add(payload.strength(), payload.ticks());
    }

    public static void add(float s, int ticks) {
        if (s >= strength * ((float) ticksLeft / Math.max(1, totalTicks))) {
            strength = Mth.clamp(s, 0.0f, 1.5f);
            ticksLeft = ticks;
            totalTicks = Math.max(1, ticks);
        }
    }

    @SubscribeEvent
    static void onTick(ClientTickEvent.Post event) {
        if (ticksLeft > 0) {
            ticksLeft--;
        }
    }

    @SubscribeEvent
    static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        if (ticksLeft <= 0 || Minecraft.getInstance().isPaused()) {
            return;
        }
        float fade = (float) ticksLeft / totalTicks;
        float s = strength * fade * 2.2f;
        double t = (Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0) + event.getPartialTick();
        event.setYaw(event.getYaw() + (float) (Math.sin(t * 2.9) * s * 0.6));
        event.setPitch(event.getPitch() + (float) (Math.sin(t * 3.7 + 1.3) * s));
        event.setRoll(event.getRoll() + (float) (Math.sin(t * 2.3 + 0.7) * s * 0.8));
    }
}
