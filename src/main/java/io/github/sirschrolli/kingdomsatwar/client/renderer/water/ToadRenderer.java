package io.github.sirschrolli.kingdomsatwar.client.renderer.water;

import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.Toad;
import net.minecraft.client.model.animal.frog.FrogModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FrogRenderer;
import net.minecraft.client.renderer.entity.state.FrogRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.frog.FrogVariants;

/**
 * Frosch: Teichfrosch (gemäßigt), Feuerkröte (warm) und Grasfrosch (kalt) an Stelle der drei Vanilla-Arten. Springen,
 * Quaken mit Schallblasen und Zungenschlag wie beim Vanilla-Frosch.
 */
public class ToadRenderer extends WildRenderer<Toad, ToadRenderer.State, FrogModel> {
    private static final Identifier TEMPERATE = WaterRenderers.texture("frog", "teichfrosch");
    private static final Identifier WARM = WaterRenderers.texture("frog", "feuerkroete");
    private static final Identifier COLD = WaterRenderers.texture("frog", "grasfrosch");

    public static class State extends FrogRenderState {
        public Identifier own = TEMPERATE;
    }

    public ToadRenderer(EntityRendererProvider.Context context) {
        super(context, new FrogModel(context.bakeLayer(WaterRenderers.FROG)), 0.3f, new FrogRenderer(context));
    }

    @Override
    protected void extract(Toad entity, State state, float partialTicks) {
        var variant = entity.getVariant();
        state.own = variant.is(FrogVariants.WARM) ? WARM : variant.is(FrogVariants.COLD) ? COLD : TEMPERATE;
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return state.own;
    }

    @Override
    public State createRenderState() {
        return new State();
    }
}
