package io.github.sirschrolli.kingdomsatwar.client.fae;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Wer im Feenportal steht, sieht die Welt in einem rosagoldenen Schimmer verschwimmen (statt im violetten Wirbel des
 * Netherportals), hört das gläserne Wirbeln der Feenreise, und um ihn tanzen Funken.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class FaePortalOverlay {
    public static final Identifier LAYER = KingdomsAtWar.id("fae_portal");

    private static float intensity;
    private static float lastIntensity;

    private FaePortalOverlay() {}

    @SubscribeEvent
    static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, LAYER, FaePortalOverlay::render);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        lastIntensity = intensity;
        if (player == null || minecraft.level == null) {
            intensity = 0.0f;
            return;
        }
        boolean inside = minecraft.level.getBlockState(player.blockPosition()).is(FaeBlocks.FAE_PORTAL.get())
                || minecraft.level.getBlockState(player.blockPosition().above()).is(FaeBlocks.FAE_PORTAL.get());
        if (inside) {
            if (intensity == 0.0f) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(ModSounds.FAE_PORTAL_TRAVEL.get(),
                        0.9f + player.getRandom().nextFloat() * 0.2f, 0.6f));
            }
            intensity = Math.min(1.0f, intensity + 0.02f);
            if (player.getRandom().nextInt(2) == 0) {
                double a = player.getRandom().nextDouble() * Math.PI * 2.0;
                minecraft.level.addParticle(ModParticles.FAE_SPARK.get(), player.getX() + Math.cos(a) * 0.8, player.getY() + 0.2
                        + player.getRandom().nextDouble() * 1.8, player.getZ() + Math.sin(a) * 0.8, 0.0, 0.03, 0.0);
            }
        } else {
            intensity = Math.max(0.0f, intensity - 0.06f);
        }
    }

    static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        float alpha = Mth.lerp(delta.getGameTimeDeltaPartialTick(false), lastIntensity, intensity);
        if (alpha <= 0.0f) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        float shown = alpha < 1.0f ? alpha * alpha * 0.75f + 0.1f : 0.85f;
        TextureAtlasSprite sprite = minecraft.getModelManager().getBlockStateModelSet()
                .getParticleMaterial(FaeBlocks.FAE_PORTAL.get().defaultBlockState()).sprite();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 0, 0, width, height, ARGB.white(shown * 0.8f));
        // ein warmer Schleier vom Rand her
        int edge = ARGB.color((int) (alpha * 120), 255, 190, 220);
        int clear = ARGB.color(0, 255, 190, 220);
        graphics.fillGradient(0, 0, width, height / 3, edge, clear);
        graphics.fillGradient(0, height * 2 / 3, width, height, clear, edge);
    }
}
