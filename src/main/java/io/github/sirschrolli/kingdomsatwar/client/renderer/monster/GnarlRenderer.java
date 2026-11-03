package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Gnarl;
import net.minecraft.client.model.monster.creaking.CreakingModel;
import net.minecraft.client.renderer.entity.CreakingRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CreakingRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Knorz: eigenes Modell (Astgeweih, Wurzelfüße, Astfinger) und Bild; die Augen glühen, solange er wach ist – wie beim
 * Vanilla-Knarz. Erstarren, Angriff und Zerfallen erbt er vom Vanilla-Renderer.
 */
public class GnarlRenderer extends CreakingRenderer<Gnarl> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/creaking/knorz.png");
    private static final RenderType EYES = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/creaking/knorz_glow.png"));

    public GnarlRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CreakingModel(context.bakeLayer(MonsterRenderers.CREAKING));
        this.layers.clear();                                   // Vanillas Augen-Ebene sitzt auf einem anderen Kopf
        this.addLayer(new GlowLayer<>(this, state -> state.eyesGlowing ? EYES : null));
    }

    @Override
    public Identifier getTextureLocation(CreakingRenderState state) {
        return TEXTURE;
    }
}
