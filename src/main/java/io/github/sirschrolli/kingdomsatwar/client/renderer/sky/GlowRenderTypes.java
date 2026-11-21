package io.github.sirschrolli.kingdomsatwar.client.renderer.sky;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

/**
 * Licht obenauf: Bilder, die das, was hinter ihnen liegt, aufhellen statt es zu verdecken (wie Blitze) – selbstleuchtend,
 * mit Textur, in der Farbe und Deckkraft ihrer Eckpunkte. Sie schreiben nicht in die Tiefe, verdecken also nichts. Für
 * Lichthöfe, Schweife und den Schein von Siegeln; die deckende Farbe darunter zeichnet weiterhin {@code RenderTypes.eyes}.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class GlowRenderTypes {
    /** Wie die leuchtenden Augen der Wesen, aber aufhellend (Quelle mal Deckkraft, dazu das Bild dahinter). */
    public static final RenderPipeline GLOW = RenderPipeline.builder(RenderPipelines.ENTITY_NO_LIGHTMAP_SNIPPET, RenderPipelines.EYES_SNIPPET)
            .withLocation(KingdomsAtWar.id("pipeline/glow"))
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .build();

    private static final Function<Identifier, RenderType> TYPES = Util.memoize(texture -> RenderType.create(KingdomsAtWar.MODID + "_glow",
            RenderSetup.builder(GLOW).withTexture("Sampler0", texture).sortOnUpload().createRenderSetup()));

    private GlowRenderTypes() {}

    /** Licht obenauf mit dieser Textur. */
    public static RenderType glow(Identifier texture) {
        return TYPES.apply(texture);
    }

    @SubscribeEvent
    static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(GLOW);
    }
}
