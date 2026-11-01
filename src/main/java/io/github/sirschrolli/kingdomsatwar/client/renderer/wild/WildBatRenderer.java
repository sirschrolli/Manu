package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildBat;
import net.minecraft.client.model.ambient.BatModel;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Fledermaus: Abendsegler, Mausohr oder Vampir; Flug und Ruhe macht das Vanilla-Modell. */
public class WildBatRenderer extends WildRenderer<WildBat, WildBatRenderState, BatModel> {
    private static final Identifier[] TEXTURES = textures("bat", "bat_", WildBat.Breed.values(), "");

    public WildBatRenderer(EntityRendererProvider.Context context) {
        super(context, new BatModel(context.bakeLayer(WildRenderers.BAT)), 0.25f, new BatRenderer(context));
    }

    @Override
    protected void extract(WildBat bat, WildBatRenderState state, float partialTicks) {
        state.breed = bat.getBreed().ordinal();
    }

    @Override
    public Identifier getTextureLocation(WildBatRenderState state) {
        return pick(TEXTURES, state.breed);
    }

    @Override
    public WildBatRenderState createRenderState() {
        return new WildBatRenderState();
    }
}
