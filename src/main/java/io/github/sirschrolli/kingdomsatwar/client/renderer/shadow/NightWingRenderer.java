package io.github.sirschrolli.kingdomsatwar.client.renderer.shadow;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.shadow.NightWingModel;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.NightWingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Nachtschwinge: wie ein Phantom gezeichnet (Größe, Neigung im Flug), mit rot glühenden Augen. */
public class NightWingRenderer extends MobRenderer<NightWingEntity, ShadowCreatureRenderState, NightWingModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/night_wing/night_wing.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/night_wing/night_wing_glow.png"));

    public NightWingRenderer(EntityRendererProvider.Context context) {
        super(context, new NightWingModel(context.bakeLayer(ModModelLayers.NIGHT_WING)), 0.75f);
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
    public void extractRenderState(NightWingEntity entity, ShadowCreatureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.flapTime = entity.getUniqueFlapTickOffset() + state.ageInTicks;
        state.size = entity.getPhantomSize();
    }

    @Override
    protected void scale(ShadowCreatureRenderState state, PoseStack poseStack) {
        float scale = 1.0f + 0.15f * state.size;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0f, 1.3125f, 0.1875f);
    }

    @Override
    protected void setupRotations(ShadowCreatureRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        poseStack.rotateDegrees(Axis.XP, state.xRot);
    }
}
