package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.monster.RiftHoundEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;

/** Rissjäger mit glühenden Rissen, Sternsprenkeln und schwebenden Splittern; beim Phasensprung versinkt er in einem Riss. */
public class RiftHoundRenderer extends MobRenderer<RiftHoundEntity, RiftHoundRenderState, RiftHoundModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/rift_hound/rift_hound.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/rift_hound/rift_hound_glow.png"));

    public RiftHoundRenderer(EntityRendererProvider.Context context) {
        super(context, new RiftHoundModel(context.bakeLayer(ModModelLayers.RIFT_HOUND)), 0.5f);
        // die Risse erlöschen, sobald er zur Hälfte im Riss versunken ist
        this.addLayer(new GlowLayer<>(this, state -> state.fade > 0.5f ? null : GLOW));
    }

    @Override
    public Identifier getTextureLocation(RiftHoundRenderState state) {
        return TEXTURE;
    }

    @Override
    public RiftHoundRenderState createRenderState() {
        return new RiftHoundRenderState();
    }

    @Override
    public void extractRenderState(RiftHoundEntity entity, RiftHoundRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.bite.copyFrom(entity.biteAnimation);
        state.howl.copyFrom(entity.howlAnimation);
        state.fade = entity.fade(partialTicks);
    }

    @Override
    protected void scale(RiftHoundRenderState state, PoseStack poseStack) {
        // beim Phasensprung sinkt er in einen Riss im Boden: er wird flach und breit, dreht sich ein und verblasst
        if (state.fade > 0.0f) {
            float f = state.fade;
            poseStack.rotate(Axis.YP.rotation(f * f * 2.4f));
            poseStack.scale(1.0f + f * 0.35f, Math.max(0.03f, 1.0f - f * 0.97f), 1.0f + f * 0.2f);
        }
    }

    @Override
    protected @Nullable RenderType getRenderType(RiftHoundRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (state.fade > 0.02f && bodyVisible) {
            return RenderTypes.entityTranslucent(TEXTURE);
        }
        return super.getRenderType(state, bodyVisible, translucent, glowing);
    }

    @Override
    protected int getModelTint(RiftHoundRenderState state) {
        return state.fade > 0.02f ? ARGB.white(Math.max(0.0f, 1.0f - state.fade * 1.1f)) : -1;
    }
}
