package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Glutgeist;
import net.minecraft.client.model.monster.blaze.BlazeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Blaze;

/**
 * Glutgeist: eigenes Modell (Kopf und zwölf Stäbe wie bei der Vanilla-Lohe, damit die Stäbe kreisen; dazu Krone, Bart
 * und Kern), Bild je Gestalt; er leuchtet selbst (wie die Lohe).
 */
public class GlutgeistRenderer extends MobRenderer<Blaze, LivingEntityRenderState, BlazeModel> {
    private static final String[] LOOKS = {"glut", "seele"};
    private static final Identifier[] TEXTURES = new Identifier[LOOKS.length];

    static {
        for (int i = 0; i < LOOKS.length; i++) {
            TEXTURES[i] = NetherRenderers.texture("blaze", LOOKS[i]);
        }
    }

    public static class State extends LivingEntityRenderState {
        public int look;
    }

    public GlutgeistRenderer(EntityRendererProvider.Context context) {
        super(context, new BlazeModel(context.bakeLayer(NetherRenderers.GLUTGEIST)), 0.5f);
    }

    @Override
    protected int getBlockLightLevel(Blaze entity, BlockPos pos) {
        return 15;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(Blaze entity, LivingEntityRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof State s) {
            s.look = entity instanceof Glutgeist geist ? geist.getLook().ordinal() : 0;
        }
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURES[state instanceof State s ? Mth.clamp(s.look, 0, LOOKS.length - 1) : 0];
    }
}
