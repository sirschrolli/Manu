package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.entity.fae.ThornWightEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Dornwicht mit glühenden Augen und glimmender Brust. */
public class ThornWightRenderer extends MobRenderer<ThornWightEntity, FaeRenderState, ThornWightModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/thorn_wight/thorn_wight.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/thorn_wight/thorn_wight_glow.png"));

    public ThornWightRenderer(EntityRendererProvider.Context context) {
        super(context, new ThornWightModel(context.bakeLayer(ModModelLayers.THORN_WIGHT)), 0.6f);
        this.addLayer(new GlowLayer<>(this, state -> GLOW));
    }

    @Override
    public Identifier getTextureLocation(FaeRenderState state) {
        return TEXTURE;
    }

    @Override
    public FaeRenderState createRenderState() {
        return new FaeRenderState();
    }

    @Override
    public void extractRenderState(ThornWightEntity entity, FaeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.slash.copyFrom(entity.slashAnimation);
        state.volley.copyFrom(entity.volleyAnimation);
    }
}
