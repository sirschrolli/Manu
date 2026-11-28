package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Tormentor;
import net.minecraft.client.model.monster.vex.VexModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VexRenderer;
import net.minecraft.client.renderer.entity.state.VexRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Vex;

/**
 * Plagegeister: eigenes Modell (Kapuze, Fetzenleib, Tuchflügel) und Bild in der Farbe ihres Beschwörers; durchscheinend
 * und voll hell wie bei Vanilla, Augen und Seelenlicht leuchten zusätzlich.
 */
public class TormentorRenderer extends VexRenderer {
    private static final String[] NAMES = {"plagegeist_violett", "plagegeist_tuerkis"};
    private static final Identifier[] TEXTURES = MonsterRenderers.textures("vex", NAMES);
    private static final RenderType[] GLOWS = MonsterRenderers.glows("vex", NAMES);

    public TormentorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new VexModel(context.bakeLayer(MonsterRenderers.VEX));
        this.addLayer(new GlowLayer<>(this, state -> state instanceof TormentorRenderState t ? GLOWS[t.look] : null));
    }

    @Override
    public VexRenderState createRenderState() {
        return new TormentorRenderState();
    }

    @Override
    public void extractRenderState(Vex entity, VexRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof TormentorRenderState look) {
            look.look = entity instanceof Tormentor t ? Mth.clamp(t.getLook().ordinal(), 0, NAMES.length - 1) : 0;
        }
    }

    @Override
    public Identifier getTextureLocation(VexRenderState state) {
        return TEXTURES[state instanceof TormentorRenderState look ? look.look : 0];
    }
}
