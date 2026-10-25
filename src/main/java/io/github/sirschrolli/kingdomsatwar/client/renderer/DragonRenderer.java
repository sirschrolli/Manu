package io.github.sirschrolli.kingdomsatwar.client.renderer;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.DragonModel;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Zeichnet die Drachen: Grundtextur je Art plus leuchtende Teile (Augen, Blitzmuster). */
public class DragonRenderer extends MobRenderer<DragonEntity, DragonRenderState, DragonModel> {
    private static final Map<DragonType, Identifier> TEXTURES = new EnumMap<>(DragonType.class);
    private static final Map<DragonType, RenderType> GLOW = new EnumMap<>(DragonType.class);

    static {
        for (DragonType type : DragonType.values()) {
            TEXTURES.put(type, KingdomsAtWar.id("textures/entity/dragon/" + type.id() + ".png"));
            GLOW.put(type, RenderTypes.eyes(KingdomsAtWar.id("textures/entity/dragon/" + type.id() + "_glow.png")));
        }
    }

    public DragonRenderer(EntityRendererProvider.Context context) {
        super(context, new DragonModel(context.bakeLayer(ModModelLayers.DRAGON)), 1.1f);
        this.addLayer(new EyesLayer<>(this) {
            private DragonType current = DragonType.FIRE;

            @Override
            public void submit(com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector collector,
                    int light, DragonRenderState state, float yRot, float xRot) {
                this.current = state.dragonType;
                super.submit(poseStack, collector, light, state, yRot, xRot);
            }

            @Override
            public RenderType renderType() {
                return GLOW.get(this.current);
            }
        });
    }

    @Override
    public Identifier getTextureLocation(DragonRenderState state) {
        return TEXTURES.get(state.dragonType);
    }

    @Override
    public DragonRenderState createRenderState() {
        return new DragonRenderState();
    }

    @Override
    public void extractRenderState(DragonEntity entity, DragonRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.dragonType = entity.getDragonType();
        state.stage = entity.getStage();
        state.flying = entity.isFlying();
        state.gliding = entity.isFlying() && entity.getDeltaMovement().y < -0.05;
        state.sitting = entity.isInSittingPose();
        state.breathing = entity.isBreathing();
        state.ridden = entity.isVehicle();
        state.flapPhase = Mth.lerp(partialTicks, entity.flapPhaseO, entity.flapPhase);
        state.flightPitch = entity.isFlying() ? -entity.getXRot() : 0.0f;
        state.roarAnimation.copyFrom(entity.roarAnimation);
        state.biteAnimation.copyFrom(entity.biteAnimation);
    }
}
