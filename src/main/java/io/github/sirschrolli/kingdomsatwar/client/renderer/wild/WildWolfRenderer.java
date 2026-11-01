package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import io.github.sirschrolli.kingdomsatwar.client.model.wild.WildWolfModel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildWolf;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.layers.WolfArmorLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/** Wolf: Fellbild je Art, wütend mit roten Augen; Wolfspanzer von Vanilla, eigenes eingefärbtes Halsband. */
public class WildWolfRenderer extends WildRenderer<WildWolf, WolfRenderState, WolfModel> {
    private static final Identifier[] CALM = textures("wolf", "wolf_", WildWolf.Breed.values(), "");
    private static final Identifier[] ANGRY = textures("wolf", "wolf_", WildWolf.Breed.values(), "_angry");

    public WildWolfRenderer(EntityRendererProvider.Context context) {
        super(context, new WildWolfModel(context.bakeLayer(WildRenderers.WOLF), false), 0.5f, new WolfRenderer(context));
        this.addLayer(new WolfArmorLayer(this, context.getModelSet(), context.getEquipmentRenderer()));
        this.addLayer(new TintedCollarLayer<>(this, new WildWolfModel(context.bakeLayer(WildRenderers.WOLF), true),
                state -> state.collarColor, state -> state.texture));
    }

    @Override
    protected void extract(WildWolf wolf, WolfRenderState state, float partialTicks) {
        state.texture = pick(state.isAngry ? ANGRY : CALM, wolf.getBreed().ordinal());
    }

    /** Nasses Fell wird dunkler (wie bei Vanilla). */
    @Override
    protected int getModelTint(WolfRenderState state) {
        float shade = state.wetShade;
        return shade == 1.0f ? -1 : ARGB.colorFromFloat(1.0f, shade, shade, shade);
    }

    @Override
    public Identifier getTextureLocation(WolfRenderState state) {
        return state.texture;
    }

    @Override
    public WolfRenderState createRenderState() {
        return new WolfRenderState();
    }
}
