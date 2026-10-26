package io.github.sirschrolli.kingdomsatwar.client.renderer.creature;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.VinelingModel;
import io.github.sirschrolli.kingdomsatwar.entity.boss.nature.VinelingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Rankling mit leuchtenden Augen; beim Herauswachsen steckt er noch halb im Boden. */
public class VinelingRenderer extends MobRenderer<VinelingEntity, VinelingRenderState, VinelingModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/vineling/vineling.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/vineling/vineling_glow.png"));

    public VinelingRenderer(EntityRendererProvider.Context context) {
        super(context, new VinelingModel(context.bakeLayer(ModModelLayers.VINELING)), 0.45f);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public Identifier getTextureLocation(VinelingRenderState state) {
        return TEXTURE;
    }

    @Override
    public VinelingRenderState createRenderState() {
        return new VinelingRenderState();
    }

    @Override
    public void extractRenderState(VinelingEntity entity, VinelingRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack.copyFrom(entity.snapAnimation);
        state.sprout = entity.sprout(partialTicks);
    }

    @Override
    protected void setupRotations(VinelingRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        if (state.sprout < 1.0f) {
            float s = state.sprout;
            poseStack.translate(0.0f, -1.6f * (1.0f - s * s), 0.0f);
        }
    }
}
