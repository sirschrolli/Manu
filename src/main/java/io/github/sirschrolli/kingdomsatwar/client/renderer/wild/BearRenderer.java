package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import io.github.sirschrolli.kingdomsatwar.client.model.wild.BearModel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.Bear;
import net.minecraft.client.model.animal.polarbear.PolarBearModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PolarBearRenderer;
import net.minecraft.resources.Identifier;

/** Bär: Eis-, Braun- oder Schwarzbär. */
public class BearRenderer extends WildRenderer<Bear, BearRenderState, PolarBearModel> {
    private static final Identifier[] TEXTURES = textures("bear", "bear_", Bear.Breed.values(), "");

    public BearRenderer(EntityRendererProvider.Context context) {
        super(context, new BearModel(context.bakeLayer(WildRenderers.BEAR)), 0.9f, new PolarBearRenderer(context));
    }

    @Override
    protected void extract(Bear bear, BearRenderState state, float partialTicks) {
        state.breed = bear.getBreed().ordinal();
    }

    @Override
    public Identifier getTextureLocation(BearRenderState state) {
        return pick(TEXTURES, state.breed);
    }

    @Override
    public BearRenderState createRenderState() {
        return new BearRenderState();
    }
}
