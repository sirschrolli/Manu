package io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.voidrealm.VoidMawModel;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.VoidMawEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/** Leerenschlund: durchscheinender Saum und Mundarme, leuchtende Augen, Sterne, Fangfäden und Schlund. */
public class VoidMawRenderer extends MobRenderer<VoidMawEntity, VoidCreatureRenderState, VoidMawModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/void_maw/void_maw.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/void_maw/void_maw_glow.png"));
    public static final float SCALE = 1.25f;

    public VoidMawRenderer(EntityRendererProvider.Context context) {
        super(context, new VoidMawModel(context.bakeLayer(ModModelLayers.VOID_MAW)), 0.7f);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public Identifier getTextureLocation(VoidCreatureRenderState state) {
        return TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(VoidCreatureRenderState state, boolean isBodyVisible, boolean forceTransparent,
            boolean appearGlowing) {
        if (!isBodyVisible && !forceTransparent && !appearGlowing) {
            return null;
        }
        return appearGlowing && !isBodyVisible ? RenderTypes.outline(TEXTURE) : RenderTypes.entityTranslucent(TEXTURE);
    }

    @Override
    public VoidCreatureRenderState createRenderState() {
        return new VoidCreatureRenderState();
    }

    @Override
    public void extractRenderState(VoidMawEntity entity, VoidCreatureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.open = Mth.lerp(partialTicks, entity.mouthOpenO, entity.mouthOpen);
        state.attack.copyFrom(entity.biteAnimation);
    }

    @Override
    protected void scale(VoidCreatureRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    protected float getShadowStrength(VoidCreatureRenderState state) {
        return 0.35f;
    }
}
