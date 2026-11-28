package io.github.sirschrolli.kingdomsatwar.client.renderer.rare;

import io.github.sirschrolli.kingdomsatwar.entity.creature.rare.PrimevalDigger;
import net.minecraft.client.model.animal.sniffer.SnifferModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SnifferRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * Urwühler: eigenes Modell (Knochen wie beim Vanilla-Schnüffler, damit Gehen, Schnuppern, Graben, Aufstehen und
 * Freudentanz greifen), Fell und Moosmatte je Gestalt; das Junge hat ein eigenes, flauschiges Modell.
 */
public class PrimevalDiggerRenderer extends AgeableMobRenderer<PrimevalDigger, SnifferRenderState, SnifferModel> {
    private static final String[] LOOKS = {"zottelwuehler", "steppenwuehler", "frostwuehler"};
    private static final Identifier[] ADULT = new Identifier[LOOKS.length];
    private static final Identifier[] BABY = new Identifier[LOOKS.length];

    static {
        for (int i = 0; i < LOOKS.length; i++) {
            ADULT[i] = RareRenderers.texture("sniffer", LOOKS[i]);
            BABY[i] = RareRenderers.texture("sniffer/baby", LOOKS[i]);
        }
    }

    public static class State extends SnifferRenderState {
        public int look;
    }

    public PrimevalDiggerRenderer(EntityRendererProvider.Context context) {
        super(context, new SnifferModel(context.bakeLayer(RareRenderers.SNIFFER)), new SnifferModel(context.bakeLayer(RareRenderers.SNIFFER_BABY)),
                1.1f);
    }

    @Override
    public SnifferRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PrimevalDigger entity, SnifferRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isSearching = entity.isSearching();
        state.diggingAnimationState.copyFrom(entity.diggingAnimationState);
        state.sniffingAnimationState.copyFrom(entity.sniffingAnimationState);
        state.risingAnimationState.copyFrom(entity.risingAnimationState);
        state.feelingHappyAnimationState.copyFrom(entity.feelingHappyAnimationState);
        state.scentingAnimationState.copyFrom(entity.scentingAnimationState);
        if (state instanceof State s) {
            s.look = entity.getLook().ordinal();
        }
    }

    @Override
    protected AABB getBoundingBoxForCulling(PrimevalDigger entity, float partialTicks) {
        return super.getBoundingBoxForCulling(entity, partialTicks).inflate(0.6f);
    }

    @Override
    public Identifier getTextureLocation(SnifferRenderState state) {
        int look = state instanceof State s ? Mth.clamp(s.look, 0, LOOKS.length - 1) : 0;
        return state.isBaby ? BABY[look] : ADULT[look];
    }
}
