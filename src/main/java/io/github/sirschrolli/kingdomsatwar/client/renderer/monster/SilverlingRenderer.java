package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Silverling;
import net.minecraft.client.model.monster.silverfish.SilverfishModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SilverfishRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Silberfischchen: eigenes Modell mit Beinen, Fühlern und Schwanzfäden; Schlängeln wie bei Vanilla. */
public class SilverlingRenderer extends WildRenderer<Silverling, LookRenderState, SilverfishModel> {
    private static final Identifier[] TEXTURES = MonsterRenderers.textures("silverfish", new String[] {"silberfischchen", "tiefenfischchen"});

    public SilverlingRenderer(EntityRendererProvider.Context context) {
        super(context, new SilverfishModel(context.bakeLayer(MonsterRenderers.SILVERFISH)), 0.3f, new SilverfishRenderer(context));
    }

    @Override
    protected void extract(Silverling silverling, LookRenderState state, float partialTicks) {
        state.look = silverling.getLook().ordinal();
    }

    @Override
    protected float getFlipDegrees() {
        return 180.0f;
    }

    @Override
    public Identifier getTextureLocation(LookRenderState state) {
        return TEXTURES[Mth.clamp(state.look, 0, TEXTURES.length - 1)];
    }

    @Override
    public LookRenderState createRenderState() {
        return new LookRenderState();
    }
}
