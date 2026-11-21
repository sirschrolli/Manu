package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Glutschleim;
import net.minecraft.client.model.monster.slime.MagmaCubeModel;
import net.minecraft.client.renderer.entity.AbstractCubeMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.cubemob.MagmaCube;

/**
 * Glutschleim: eigenes Modell (acht Scheiben und Kern wie beim Vanilla-Magmawürfel, damit er beim Springen
 * auseinandergeht; dazu Schollen und Tropfen), Bild je Gestalt; er leuchtet selbst (wie der Magmawürfel).
 */
public class GlutschleimRenderer extends AbstractCubeMobRenderer<MagmaCube, SlimeRenderState, MagmaCubeModel> {
    private static final String[] LOOKS = {"glut", "basalt"};
    private static final Identifier[] TEXTURES = new Identifier[LOOKS.length];

    static {
        for (int i = 0; i < LOOKS.length; i++) {
            TEXTURES[i] = NetherRenderers.texture("magma_cube", LOOKS[i]);
        }
    }

    public static class State extends SlimeRenderState {
        public int look;
    }

    public GlutschleimRenderer(EntityRendererProvider.Context context) {
        super(context, new MagmaCubeModel(context.bakeLayer(NetherRenderers.GLUTSCHLEIM)));
    }

    @Override
    protected int getBlockLightLevel(MagmaCube entity, BlockPos pos) {
        return 15;
    }

    @Override
    public SlimeRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MagmaCube entity, SlimeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof State s) {
            s.look = entity instanceof Glutschleim slime ? slime.getLook().ordinal() : 0;
        }
    }

    @Override
    public Identifier getTextureLocation(SlimeRenderState state) {
        return TEXTURES[state instanceof State s ? Mth.clamp(s.look, 0, LOOKS.length - 1) : 0];
    }
}
