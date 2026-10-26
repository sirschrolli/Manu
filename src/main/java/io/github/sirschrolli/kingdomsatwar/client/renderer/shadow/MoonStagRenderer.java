package io.github.sirschrolli.kingdomsatwar.client.renderer.shadow;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.DeerModel;
import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.CreatureRenderState;
import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.DeerRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Mondhirsch: silbriges Fell; das Kristallgeweih und die Augen leuchten. */
public class MoonStagRenderer extends DeerRenderer {
    private static final Identifier ADULT = KingdomsAtWar.id("textures/entity/deer/moon_stag.png");
    private static final Identifier FAWN = KingdomsAtWar.id("textures/entity/deer/moon_stag_fawn.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/deer/moon_stag_glow.png"));
    private static final RenderType GLOW_FAWN = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/deer/moon_stag_fawn_glow.png"));

    public MoonStagRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.addLayer(new RenderLayer<CreatureRenderState, DeerModel>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, CreatureRenderState state, float yRot,
                    float xRot) {
                collector.order(1).submitModel(this.getParentModel(), state, poseStack, state.isBaby ? GLOW_FAWN : GLOW, lightCoords,
                        OverlayTexture.NO_OVERLAY, state.outlineColor);
            }
        });
    }

    @Override
    public Identifier getTextureLocation(CreatureRenderState state) {
        return state.isBaby ? FAWN : ADULT;
    }
}
