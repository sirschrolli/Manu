package io.github.sirschrolli.kingdomsatwar.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.network.DragonBreathPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

/** Tastenbelegung der Mod (änderbar unter Optionen → Steuerung → Tastenbelegung). */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class ModKeyMappings {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(KingdomsAtWar.id("dragons"));
    /** Drachenodem beim Reiten (Standard: R). */
    public static final KeyMapping DRAGON_BREATH = new KeyMapping("key.kingdomsatwar.dragon_breath", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYBOARD, InputConstants.KEY_R, CATEGORY);

    private ModKeyMappings() {}

    @SubscribeEvent
    static void register(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(DRAGON_BREATH);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (DRAGON_BREATH.consumeClick()) {
            if (minecraft.player != null && minecraft.player.getVehicle() instanceof DragonEntity dragon
                    && dragon.getControllingPassenger() == minecraft.player) {
                ClientPacketDistributor.sendToServer(DragonBreathPayload.INSTANCE);
            }
        }
    }
}
