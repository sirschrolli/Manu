package io.github.sirschrolli.kingdomsatwar.client.renderer.water;

import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import net.minecraft.client.model.animal.squid.SquidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GlowSquidRenderer;
import net.minecraft.client.renderer.entity.state.SquidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.squid.GlowSquid;

/**
 * Tiefseekrake: tiefblauer Leib, voll hell wie der Vanilla-Leuchttintenfisch; dazu leuchten Lichtflecken, Flossensaum, Augen
 * und Armringe in eigenem Türkis. Nach einem Treffer wird sie wie bei Vanilla für ein paar Sekunden dunkel.
 */
public class DeepKrakenRenderer extends GlowSquidRenderer {
    private static final Identifier TEXTURE = WaterRenderers.texture("squid", "tiefseekrake");
    private static final RenderType GLOW = RenderTypes.eyes(WaterRenderers.texture("squid", "tiefseekrake_glow"));

    public DeepKrakenRenderer(EntityRendererProvider.Context context) {
        super(context, new SquidModel(context.bakeLayer(WaterRenderers.SQUID)), new SquidModel(context.bakeLayer(WaterRenderers.SQUID_BABY)));
        this.addLayer(new GlowLayer<>(this, state -> state instanceof State own && own.glowing ? GLOW : null));
    }

    /** Wie beim Vanilla-Leuchttintenfisch: nach einem Treffer bleibt die Krake ein paar Sekunden dunkel. */
    public static class State extends SquidRenderState {
        public boolean glowing = true;
    }

    @Override
    public SquidRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GlowSquid entity, SquidRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof State own) {
            own.glowing = entity.getDarkTicksRemaining() <= 0;
        }
    }

    @Override
    public Identifier getTextureLocation(SquidRenderState state) {
        return TEXTURE;
    }
}
