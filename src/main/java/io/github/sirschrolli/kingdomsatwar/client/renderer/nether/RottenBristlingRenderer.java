package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.RottenBristling;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombifiedPiglinRenderer;
import net.minecraft.client.renderer.entity.state.ZombifiedPiglinRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;

/** Moderborstlinge: das Modell der Borstlinge mit fauligem Bild je Gestalt und einem glimmenden Auge. */
public class RottenBristlingRenderer extends ZombifiedPiglinRenderer {
    private static final String[] LOOKS = {"moder_karmesin", "moder_aschen"};
    private static final Identifier[] ADULT = new Identifier[LOOKS.length];
    private static final Identifier[] BABY = new Identifier[LOOKS.length];
    private static final RenderType[] EYES = new RenderType[LOOKS.length];
    private static final RenderType[] BABY_EYES = new RenderType[LOOKS.length];

    static {
        for (int i = 0; i < LOOKS.length; i++) {
            ADULT[i] = NetherRenderers.texture("piglin", LOOKS[i]);
            BABY[i] = NetherRenderers.texture("piglin/baby", LOOKS[i]);
            EYES[i] = RenderTypes.eyes(NetherRenderers.texture("piglin", LOOKS[i] + "_glow"));
            BABY_EYES[i] = RenderTypes.eyes(NetherRenderers.texture("piglin/baby", LOOKS[i] + "_glow"));
        }
    }

    public static class State extends ZombifiedPiglinRenderState {
        public int look;
    }

    public RottenBristlingRenderer(EntityRendererProvider.Context context) {
        super(context, NetherRenderers.BORSTLING, NetherRenderers.BORSTLING_BABY, ModelLayers.ZOMBIFIED_PIGLIN_ARMOR,
                ModelLayers.ZOMBIFIED_PIGLIN_BABY_ARMOR);
        this.addLayer(new GlowLayer<>(this, state -> state.isBaby ? BABY_EYES[look(state)] : EYES[look(state)]));
    }

    private static int look(ZombifiedPiglinRenderState state) {
        return state instanceof State s ? Mth.clamp(s.look, 0, LOOKS.length - 1) : 0;
    }

    @Override
    public ZombifiedPiglinRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ZombifiedPiglin entity, ZombifiedPiglinRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof State s) {
            s.look = entity instanceof RottenBristling rotten ? rotten.getLook().ordinal() : 0;
        }
    }

    @Override
    public Identifier getTextureLocation(ZombifiedPiglinRenderState state) {
        return state.isBaby ? BABY[look(state)] : ADULT[look(state)];
    }
}
