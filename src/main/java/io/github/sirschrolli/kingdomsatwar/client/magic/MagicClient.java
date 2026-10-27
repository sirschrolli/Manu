package io.github.sirschrolli.kingdomsatwar.client.magic;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.ModKeyMappings;
import io.github.sirschrolli.kingdomsatwar.magic.AetheritStaffItem;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Magie auf dem eigenen Bildschirm: Zauberkreis (Taste halten), Zauberanzeige an der Schnellleiste, Magietisch. */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class MagicClient {
    private MagicClient() {}

    @SubscribeEvent
    static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(io.github.sirschrolli.kingdomsatwar.registry.ModMenus.MAGIC_TABLE.get(), MagicTableScreen::new);
    }

    @SubscribeEvent
    static void registerRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(io.github.sirschrolli.kingdomsatwar.registry.ModBlockEntities.MAGIC_TABLE.get(), MagicTableRenderer::new);
    }

    @SubscribeEvent
    static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, SpellHud.LAYER, SpellHud::render);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        SpellHud.tick();
        while (ModKeyMappings.SPELL_WHEEL.consumeClick()) {
            if (minecraft.player != null && minecraft.gui.screen() == null && !AetheritStaffItem.heldStaff(minecraft.player).isEmpty()) {
                minecraft.gui.setScreen(new SpellWheelScreen());
            }
        }
    }
}
