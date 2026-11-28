package io.github.sirschrolli.kingdomsatwar.client.renderer.water;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.SeaTurtle;
import net.minecraft.client.model.animal.turtle.AdultTurtleModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TurtleRenderer;
import net.minecraft.client.renderer.entity.state.TurtleRenderState;
import net.minecraft.resources.Identifier;

/**
 * Suppen- und Karettschildkröte: eigener Panzer je Art, Kriechen an Land, Schwimmen und Eiablage wie bei Vanilla (mit
 * gewölbtem Eierbauch, solange sie Eier trägt). Schlüpflinge sind die kleine Ausgabe des erwachsenen Tiers.
 */
public class SeaTurtleRenderer extends WildRenderer<SeaTurtle, SeaTurtleRenderer.State, AdultTurtleModel> {
    private static final Identifier[] TEXTURES = {WaterRenderers.texture("turtle", "suppenschildkroete"),
            WaterRenderers.texture("turtle", "karettschildkroete")};
    /** Wie die Vanilla-Schlüpflinge: knapp ein Drittel so groß. */
    private static final float BABY_SCALE = 0.35f;

    public static class State extends TurtleRenderState {
        public int look;
    }

    public SeaTurtleRenderer(EntityRendererProvider.Context context) {
        super(context, new AdultTurtleModel(context.bakeLayer(WaterRenderers.TURTLE)), 0.7f, new TurtleRenderer(context));
    }

    @Override
    protected void extract(SeaTurtle entity, State state, float partialTicks) {
        state.look = entity.getLook().ordinal();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return pick(TEXTURES, state.look);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected void scale(State state, PoseStack poseStack) {
        if (state.isBaby) {
            poseStack.scale(BABY_SCALE, BABY_SCALE, BABY_SCALE);
        }
    }

    @Override
    protected float getShadowRadius(State state) {
        return 0.7f * (state.isBaby ? BABY_SCALE * 1.4f : 1.0f);
    }
}
