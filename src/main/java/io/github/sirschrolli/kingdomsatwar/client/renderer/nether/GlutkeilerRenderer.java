package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Glutkeiler;
import net.minecraft.client.renderer.entity.AbstractHoglinRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Glutkeiler: eigenes Modell (Knochen wie beim Vanilla-Hoglin, damit Laufen, Ohrenschlagen und Kopfstoß passen), Bild je
 * Gestalt; Augen, glimmende Borstenspitzen und die Magmakruste leuchten. Verwandelt er sich gerade, zittert er.
 */
public class GlutkeilerRenderer extends AbstractHoglinRenderer<Glutkeiler> {
    private static final String[] LOOKS = {"karmesin", "aschen", "schlacke"};
    private static final Identifier[] ADULT = new Identifier[LOOKS.length];
    private static final Identifier[] BABY = new Identifier[LOOKS.length];
    private static final RenderType[] GLOW = new RenderType[LOOKS.length];
    private static final RenderType[] BABY_GLOW = new RenderType[LOOKS.length];

    static {
        for (int i = 0; i < LOOKS.length; i++) {
            ADULT[i] = NetherRenderers.texture("hoglin", LOOKS[i]);
            BABY[i] = NetherRenderers.texture("hoglin/baby", LOOKS[i]);
            GLOW[i] = RenderTypes.eyes(NetherRenderers.texture("hoglin", LOOKS[i] + "_glow"));
            BABY_GLOW[i] = RenderTypes.eyes(NetherRenderers.texture("hoglin/baby", LOOKS[i] + "_glow"));
        }
    }

    public static class State extends HoglinRenderState {
        public int look;
    }

    public GlutkeilerRenderer(EntityRendererProvider.Context context) {
        super(context, NetherRenderers.GLUTKEILER, NetherRenderers.GLUTKEILER_BABY, 0.7f);
        this.addLayer(new GlowLayer<>(this, state -> state.isBaby ? BABY_GLOW[look(state)] : GLOW[look(state)]));
    }

    private static int look(HoglinRenderState state) {
        return state instanceof State s ? Mth.clamp(s.look, 0, LOOKS.length - 1) : 0;
    }

    @Override
    public HoglinRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(Glutkeiler entity, HoglinRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isConverting = entity.isConverting();
        if (state instanceof State s) {
            s.look = entity.getLook().ordinal();
        }
    }

    @Override
    protected boolean isShaking(HoglinRenderState state) {
        return super.isShaking(state) || state.isConverting;
    }

    @Override
    public Identifier getTextureLocation(HoglinRenderState state) {
        return state.isBaby ? BABY[look(state)] : ADULT[look(state)];
    }
}
