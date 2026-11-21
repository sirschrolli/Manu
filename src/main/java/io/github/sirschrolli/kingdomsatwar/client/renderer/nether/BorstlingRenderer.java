package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Borstling;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.client.renderer.entity.state.PiglinRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;

/**
 * Borstlinge und Borstenhünen: eigenes Modell (Knochen wie beim Vanilla-Piglin), Tracht je Gestalt; die Vanilla-
 * Rüstungen und gehaltenen Waffen sitzen wie gewohnt.
 */
public class BorstlingRenderer extends PiglinRenderer {
    private static final String[] LOOKS = {"karmesin", "aschen", "gold"};
    private static final Identifier[] ADULT = new Identifier[LOOKS.length];
    private static final Identifier[] BABY = new Identifier[LOOKS.length];
    private static final Identifier BRUTE = NetherRenderers.texture("piglin", "borstenhuene");

    static {
        for (int i = 0; i < LOOKS.length; i++) {
            ADULT[i] = NetherRenderers.texture("piglin", LOOKS[i]);
            BABY[i] = NetherRenderers.texture("piglin/baby", LOOKS[i]);
        }
    }

    public static class State extends PiglinRenderState {
        public int look;
    }

    public BorstlingRenderer(EntityRendererProvider.Context context, ModelLayerLocation baby, ArmorModelSet<ModelLayerLocation> armor,
            ArmorModelSet<ModelLayerLocation> babyArmor) {
        super(context, NetherRenderers.BORSTLING, baby, armor, babyArmor);
    }

    @Override
    public PiglinRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AbstractPiglin entity, PiglinRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isBrute = entity instanceof PiglinBrute;
        if (state instanceof State s) {
            s.look = entity instanceof Borstling borstling ? borstling.getLook().ordinal() : 0;
        }
    }

    @Override
    public Identifier getTextureLocation(PiglinRenderState state) {
        if (state.isBrute) {
            return BRUTE;
        }
        int look = state instanceof State s ? Mth.clamp(s.look, 0, LOOKS.length - 1) : 0;
        return state.isBaby ? BABY[look] : ADULT[look];
    }
}
