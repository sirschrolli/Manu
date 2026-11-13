package io.github.sirschrolli.kingdomsatwar.client.urmeer;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.urmeer.TideGate;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Wer im Gezeitentor schwimmt (seit 0.89), sieht die Welt in einem türkisen Strudel verschwimmen – der Schleier füllt
 * langsam das Bild, vom Rand her drängt tiefes Blau herein –, hört das Rauschen der Strömung, und um ihn wirbeln die
 * Funken des Meeres.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class TideGateOverlay {
    public static final Identifier LAYER = KingdomsAtWar.id("tide_gate");

    private static float intensity;
    private static float lastIntensity;

    private TideGateOverlay() {}

    @SubscribeEvent
    static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, LAYER, TideGateOverlay::render);
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
        boolean inside = minecraft.level.getBlockState(player.blockPosition()).is(SeaBlocks.TIDE_GATE.get())
                || minecraft.level.getBlockState(player.blockPosition().above()).is(SeaBlocks.TIDE_GATE.get());
        if (inside) {
            if (intensity == 0.0f) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(SoundEvents.CONDUIT_ACTIVATE,
                        0.85f + player.getRandom().nextFloat() * 0.2f, 0.7f));
            }
            intensity = Math.min(1.0f, intensity + 1.0f / TideGate.TRAVEL_TICKS);
            double a = player.getRandom().nextDouble() * Math.PI * 2.0;
            double r = 0.7 + player.getRandom().nextDouble() * 0.5;
            minecraft.level.addParticle(ModParticles.SEA_GLOW.get(), player.getX() + Math.cos(a) * r, player.getY() + 0.2
                    + player.getRandom().nextDouble() * 1.8, player.getZ() + Math.sin(a) * r, -Math.sin(a) * 0.05, 0.02, Math.cos(a) * 0.05);
            if (player.getRandom().nextInt(2) == 0) {
                minecraft.level.addParticle(ParticleTypes.NAUTILUS, player.getX() + Math.cos(a) * 1.6, player.getY() + 1.0, player.getZ()
                        + Math.sin(a) * 1.6, -Math.cos(a) * 1.4, -0.2, -Math.sin(a) * 1.4);
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
        float shown = alpha < 1.0f ? alpha * alpha * 0.7f + 0.1f : 0.8f;
        TextureAtlasSprite sprite = minecraft.getModelManager().getBlockStateModelSet()
                .getParticleMaterial(SeaBlocks.TIDE_GATE.get().defaultBlockState()).sprite();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 0, 0, width, height, ARGB.white(shown * 0.75f));
        // tiefes Blau vom Rand her
        int edge = ARGB.color((int) (alpha * 150), 10, 60, 110);
        int clear = ARGB.color(0, 10, 60, 110);
        graphics.fillGradient(0, 0, width, height / 3, edge, clear);
        graphics.fillGradient(0, height * 2 / 3, width, height, clear, edge);
    }
}
