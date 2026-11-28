package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Nightmare;
import net.minecraft.client.model.monster.phantom.PhantomModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PhantomRenderer;
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Phantom;

/**
 * Nachtmahre: eigenes Modell (Fingerknochen, zerfranste Flughäute, Wirbelkamm, Hörner) und eigene Bilder; Flügelschlag,
 * Größe und Neigung erbt er vom Vanilla-Renderer, die leuchtenden Augen kommen aus dem eigenen Leuchtbild.
 */
public class NightmareRenderer extends PhantomRenderer {
    private static final String[] NAMES = {"nachtmahr", "nachtmahr_bleich"};
    private static final Identifier[] TEXTURES = MonsterRenderers.textures("phantom", NAMES);
    private static final RenderType[] GLOWS = MonsterRenderers.glows("phantom", NAMES);

    public NightmareRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PhantomModel(context.bakeLayer(MonsterRenderers.PHANTOM));
        this.layers.clear();                                   // ohne Vanillas Augen-Ebene (die passt nicht auf das eigene Bild)
        this.addLayer(new GlowLayer<>(this, state -> state instanceof NightmareRenderState nightmare ? GLOWS[nightmare.look] : null));
    }

    @Override
    public PhantomRenderState createRenderState() {
        return new NightmareRenderState();
    }

    @Override
    public void extractRenderState(Phantom entity, PhantomRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof NightmareRenderState nightmare) {
            nightmare.look = entity instanceof Nightmare n ? Mth.clamp(n.getLook().ordinal(), 0, NAMES.length - 1) : 0;
        }
    }

    @Override
    public Identifier getTextureLocation(PhantomRenderState state) {
        return TEXTURES[state instanceof NightmareRenderState nightmare ? nightmare.look : 0];
    }
}
