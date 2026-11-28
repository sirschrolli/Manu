package io.github.sirschrolli.kingdomsatwar.client.renderer.golem;

import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.golem.HouseSprite;
import net.minecraft.client.renderer.entity.AllayRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.AllayRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.allay.Allay;

/** Wichtel: eigenes Modell, Mütze je Gestalt, leuchtende Augen; Schweben, Tanzen und Tragen wie beim Vanilla-Hilfsgeist. */
public class HouseSpriteRenderer extends AllayRenderer {
    private static final String[] LOOKS = {"mooswichtel", "pilzwichtel", "laubwichtel", "sternwichtel"};
    static final int TOADSTOOL = 1;
    private static final Identifier[] TEXTURES = GolemRenderers.textures("allay", "", LOOKS);
    private static final RenderType[] EYES = new RenderType[LOOKS.length];

    static {
        Identifier[] glow = GolemRenderers.textures("allay", "_glow", LOOKS);
        for (int i = 0; i < LOOKS.length; i++) {
            EYES[i] = RenderTypes.eyes(glow[i]);
        }
    }

    public static class State extends AllayRenderState {
        public int look;
    }

    public HouseSpriteRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new HouseSpriteModel(context.bakeLayer(GolemRenderers.ALLAY));
        this.addLayer(new GlowLayer<>(this, state -> EYES[look(state)]));
    }

    private static int look(AllayRenderState state) {
        return state instanceof State s ? Mth.clamp(s.look, 0, LOOKS.length - 1) : 0;
    }

    @Override
    public AllayRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(Allay entity, AllayRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof State s) {
            s.look = entity instanceof HouseSprite sprite ? sprite.getLook().ordinal() : 0;
        }
    }

    @Override
    public Identifier getTextureLocation(AllayRenderState state) {
        return TEXTURES[look(state)];
    }
}
