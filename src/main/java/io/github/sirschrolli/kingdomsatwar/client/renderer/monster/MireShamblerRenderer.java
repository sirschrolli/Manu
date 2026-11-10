package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.monster.MireShamblerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Moorschlurfer mit glimmenden Augen und leuchtenden Baumpilzen; versunken ragt nur der Buckel aus dem Schlamm. */
public class MireShamblerRenderer extends MobRenderer<MireShamblerEntity, MireShamblerRenderState, MireShamblerModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/mire_shambler/mire_shambler.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/mire_shambler/mire_shambler_glow.png"));

    public MireShamblerRenderer(EntityRendererProvider.Context context) {
        super(context, new MireShamblerModel(context.bakeLayer(ModModelLayers.MIRE_SHAMBLER)), 0.85f);
        this.addLayer(new GlowLayer<>(this, state -> GLOW));
    }

    @Override
    public Identifier getTextureLocation(MireShamblerRenderState state) {
        return TEXTURE;
    }

    @Override
    public MireShamblerRenderState createRenderState() {
        return new MireShamblerRenderState();
    }

    @Override
    public void extractRenderState(MireShamblerEntity entity, MireShamblerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.throwing.copyFrom(entity.throwAnimation);
        state.grasp.copyFrom(entity.graspAnimation);
        state.rise.copyFrom(entity.riseAnimation);
        state.sunk = Anim.follow(entity, 0, entity.isSubmerged() ? 1.0f : 0.0f, state.ageInTicks, entity.isSubmerged() ? 1.5f : 6.0f);
    }

    /** Versunken wirft er kaum Schatten. */
    @Override
    protected float getShadowRadius(MireShamblerRenderState state) {
        return super.getShadowRadius(state) * (1.0f - 0.7f * state.sunk);
    }
}
