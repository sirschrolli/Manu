package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Devastator;
import net.minecraft.client.model.monster.ravager.RavagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RavagerRenderer;
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Ravager;

/** Verwüster: eigenes Modell und Bild je Gestalt; Rammen, Brüllen und Betäubtsein erbt er vom Vanilla-Renderer. */
public class DevastatorRenderer extends RavagerRenderer {
    private static final String[] NAMES = {"verwuester_auerochse", "verwuester_steppe"};
    private static final Identifier[] TEXTURES = MonsterRenderers.textures("ravager", NAMES);
    private static final RenderType[] GLOWS = MonsterRenderers.glows("ravager", NAMES);

    public DevastatorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new RavagerModel(context.bakeLayer(MonsterRenderers.RAVAGER));
        this.addLayer(new GlowLayer<>(this, state -> state instanceof DevastatorRenderState d ? GLOWS[d.look] : null));
    }

    @Override
    public RavagerRenderState createRenderState() {
        return new DevastatorRenderState();
    }

    @Override
    public void extractRenderState(Ravager entity, RavagerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof DevastatorRenderState look) {
            look.look = entity instanceof Devastator d ? Mth.clamp(d.getLook().ordinal(), 0, NAMES.length - 1) : 0;
        }
    }

    @Override
    public Identifier getTextureLocation(RavagerRenderState state) {
        return TEXTURES[state instanceof DevastatorRenderState look ? look.look : 0];
    }
}
