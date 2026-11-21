package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Conjurer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EvokerRenderer;
import net.minecraft.client.renderer.entity.state.EvokerRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Beschwörer: eigenes Modell und Bild je Gestalt; die Augen in der Kapuze und die Runen leuchten. Zauberhaltung und
 * Jubel kommen vom Vanilla-Renderer.
 */
public class ConjurerRenderer extends EvokerRenderer<Conjurer> {
    private static final String[] NAMES = {"beschwoerer_schatten", "beschwoerer_runen"};
    private static final Identifier[] TEXTURES = MonsterRenderers.textures("illager", NAMES);
    private static final RenderType[] GLOWS = MonsterRenderers.glows("illager", NAMES);

    public ConjurerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = Outlaws.model(context);
        this.addLayer(new GlowLayer<>(this, state -> state instanceof ConjurerRenderState c ? GLOWS[c.look] : null));
    }

    @Override
    public EvokerRenderState createRenderState() {
        return new ConjurerRenderState();
    }

    @Override
    public void extractRenderState(Conjurer entity, EvokerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof ConjurerRenderState look) {
            look.look = Mth.clamp(entity.getLook().ordinal(), 0, NAMES.length - 1);
        }
    }

    @Override
    public Identifier getTextureLocation(EvokerRenderState state) {
        return TEXTURES[state instanceof ConjurerRenderState look ? look.look : 0];
    }
}
