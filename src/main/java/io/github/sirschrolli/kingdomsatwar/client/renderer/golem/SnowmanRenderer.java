package io.github.sirschrolli.kingdomsatwar.client.renderer.golem;

import io.github.sirschrolli.kingdomsatwar.entity.creature.golem.Snowman;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SnowGolemRenderer;
import net.minecraft.client.renderer.entity.state.SnowGolemRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.golem.SnowGolem;

/** Schneemann: eigenes Modell, Schal und Mütze je Gestalt; den Kürbis auf dem Kopf zeichnet Vanillas Kopf-Ebene. */
public class SnowmanRenderer extends SnowGolemRenderer {
    private static final String[] LOOKS = {"rot", "gruen", "blau", "gelb"};
    private static final Identifier[] TEXTURES = GolemRenderers.textures("snow_golem", "", LOOKS);

    public static class State extends SnowGolemRenderState {
        public int look;
    }

    public SnowmanRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SnowmanModel(context.bakeLayer(GolemRenderers.SNOW_GOLEM));
    }

    @Override
    public SnowGolemRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SnowGolem entity, SnowGolemRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof State s) {
            s.look = entity instanceof Snowman snowman ? snowman.getLook().ordinal() : 0;
        }
    }

    @Override
    public Identifier getTextureLocation(SnowGolemRenderState state) {
        return TEXTURES[state instanceof State s ? Mth.clamp(s.look, 0, LOOKS.length - 1) : 0];
    }
}
