package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.client.model.wild.FelineModel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.Wildcat;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.OcelotRenderer;
import net.minecraft.resources.Identifier;

/** Wildkatze: Ozelot, Waldkatze und der etwas größere Luchs. */
public class WildcatRenderer extends WildRenderer<Wildcat, WildcatRenderState, FelineModel<WildcatRenderState>> {
    private static final Identifier[] TEXTURES = textures("feline", "wildcat_", Wildcat.Breed.values(), "");
    private static final float LYNX_SCALE = 1.12f;

    public WildcatRenderer(EntityRendererProvider.Context context) {
        super(context, new FelineModel<>(context.bakeLayer(WildRenderers.WILDCAT), false), 0.4f, new OcelotRenderer(context));
    }

    @Override
    protected void extract(Wildcat cat, WildcatRenderState state, float partialTicks) {
        state.breed = cat.getBreed().ordinal();
        state.lynx = cat.getBreed() == Wildcat.Breed.LUCHS;
    }

    @Override
    protected void scale(WildcatRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        if (state.lynx) {
            poseStack.scale(LYNX_SCALE, LYNX_SCALE, LYNX_SCALE);
        }
    }

    @Override
    public Identifier getTextureLocation(WildcatRenderState state) {
        return pick(TEXTURES, state.breed);
    }

    @Override
    public WildcatRenderState createRenderState() {
        return new WildcatRenderState();
    }
}
