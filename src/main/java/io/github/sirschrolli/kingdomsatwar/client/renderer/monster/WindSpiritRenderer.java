package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.monster.breeze.BreezeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.BreezeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.BreezeRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Windgeist: Steinmaske mit Kapuze und Runensteinen als Körper, darunter der rollende Wirbel aus eigenem, nahtlosem
 * Windbild; Augen, Runen und Kristalle leuchten. Sprünge, Ausweichen und Schießen erbt er vom Vanilla-Renderer.
 */
public class WindSpiritRenderer extends BreezeRenderer {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/breeze/windgeist.png");
    private static final RenderType GLOW = RenderTypes.breezeEyes(KingdomsAtWar.id("textures/entity/breeze/windgeist_glow.png"));

    public WindSpiritRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new BreezeModel(context.bakeLayer(MonsterRenderers.BREEZE));
        this.layers.clear();                                   // Vanillas Wind- und Augen-Ebene passen nicht zum eigenen Bild
        this.addLayer(new Wind(this, context.getModelSet()));
        this.addLayer(new GlowLayer<>(this, state -> GLOW));
    }

    @Override
    public Identifier getTextureLocation(BreezeRenderState state) {
        return TEXTURE;
    }

    /** Der Wirbel unter dem Windgeist: eigenes Modell, das Bild rollt seitwärts wie bei Vanilla. */
    private static final class Wind extends RenderLayer<BreezeRenderState, BreezeModel> {
        private static final Identifier WIND = KingdomsAtWar.id("textures/entity/breeze/wind/windgeist.png");
        private final BreezeModel model;

        Wind(RenderLayerParent<BreezeRenderState, BreezeModel> parent, EntityModelSet models) {
            super(parent);
            this.model = new BreezeModel(models.bakeLayer(MonsterRenderers.BREEZE_WIND));
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, BreezeRenderState state, float yRot, float xRot) {
            RenderType type = RenderTypes.breezeWind(WIND, state.ageInTicks * 0.02f % 1.0f, 0.0f);
            collector.order(1).submitModel(this.model, state, poseStack, type, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        }
    }
}
