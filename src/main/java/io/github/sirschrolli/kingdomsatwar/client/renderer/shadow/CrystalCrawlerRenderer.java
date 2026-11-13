package io.github.sirschrolli.kingdomsatwar.client.renderer.shadow;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.shadow.CrystalCrawlerModel;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.CrystalCrawlerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Kristallkriecher: die Rückenkristalle und die vier Augen leuchten im Dunkeln. */
public class CrystalCrawlerRenderer extends MobRenderer<CrystalCrawlerEntity, ShadowCreatureRenderState, CrystalCrawlerModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/crystal_crawler/crystal_crawler.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/crystal_crawler/crystal_crawler_glow.png"));

    public CrystalCrawlerRenderer(EntityRendererProvider.Context context) {
        super(context, new CrystalCrawlerModel(context.bakeLayer(ModModelLayers.CRYSTAL_CRAWLER)), 0.8f);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public Identifier getTextureLocation(ShadowCreatureRenderState state) {
        return TEXTURE;
    }

    @Override
    public ShadowCreatureRenderState createRenderState() {
        return new ShadowCreatureRenderState();
    }

    @Override
    public void extractRenderState(CrystalCrawlerEntity entity, ShadowCreatureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.climb = Mth.lerp(partialTicks, entity.climbAmountO, entity.climbAmount);
        state.attack.copyFrom(entity.biteAnimation);
    }
}
