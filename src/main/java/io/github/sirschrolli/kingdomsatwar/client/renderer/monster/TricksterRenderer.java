package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllusionerRenderer;
import net.minecraft.client.renderer.entity.state.IllusionerRenderState;
import net.minecraft.resources.Identifier;

/** Gaukler: eigenes Modell und Bild; Trugbilder, Unsichtbarkeit und Bogen kommen vom Vanilla-Renderer. */
public class TricksterRenderer extends IllusionerRenderer {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/illager/gaukler.png");

    public TricksterRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = Outlaws.model(context);
    }

    @Override
    public Identifier getTextureLocation(IllusionerRenderState state) {
        return TEXTURE;
    }
}
