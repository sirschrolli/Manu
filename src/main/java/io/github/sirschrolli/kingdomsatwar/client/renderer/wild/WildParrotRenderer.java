package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildParrot;
import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.parrot.Parrot;

/** Papagei: ein Fellbild je Art (= Vanilla-Farbe); Fliegen, Sitzen und Tanzen macht das Vanilla-Modell. */
public class WildParrotRenderer extends WildRenderer<WildParrot, ParrotRenderState, ParrotModel> {
    private static final Identifier[] TEXTURES = textures("parrot", "parrot_", WildParrot.Breed.values(), "");

    public WildParrotRenderer(EntityRendererProvider.Context context) {
        super(context, new ParrotModel(context.bakeLayer(WildRenderers.PARROT)), 0.3f, new ParrotRenderer(context));
    }

    /** Fellbild einer Vanilla-Farbe – auch für den Papagei auf der Schulter. */
    public static Identifier texture(Parrot.Variant variant) {
        return pick(TEXTURES, WildParrot.breedOf(variant).ordinal());
    }

    @Override
    public Identifier getTextureLocation(ParrotRenderState state) {
        return texture(state.variant);
    }

    @Override
    public ParrotRenderState createRenderState() {
        return new ParrotRenderState();
    }
}
