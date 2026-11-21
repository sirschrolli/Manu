package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Moderkeiler;
import net.minecraft.client.renderer.entity.AbstractHoglinRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Moderkeiler: das Modell der Glutkeiler mit verwestem Bild je Gestalt und einem glimmenden Auge. */
public class ModerkeilerRenderer extends AbstractHoglinRenderer<Moderkeiler> {
    private static final String[] LOOKS = {"moder_karmesin", "moder_aschen"};
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

    public ModerkeilerRenderer(EntityRendererProvider.Context context) {
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
    public void extractRenderState(Moderkeiler entity, HoglinRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof State s) {
            s.look = entity.getLook().ordinal();
        }
    }

    @Override
    public Identifier getTextureLocation(HoglinRenderState state) {
        return state.isBaby ? BABY[look(state)] : ADULT[look(state)];
    }
}
