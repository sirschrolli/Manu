package io.github.sirschrolli.kingdomsatwar.client.renderer.abyss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.ClockPendulumEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * Das Pendel des Stundenwächters: vom Aufhängepunkt (dem Wesen) hängt die Stange – Stück für Stück, einen Block je Stück –,
 * oben das Lager (ein Zahnrad, das sich mitdreht), unten die Sichel um die Pendelscheibe. Alles schwingt um das Lager entlang
 * der Bahn; es blendet ein und wieder aus. Die Pendelscheibe und ihre Stundenmarken leuchten.
 */
public class ClockPendulumRenderer extends EntityRenderer<ClockPendulumEntity, ClockPendulumRenderer.State> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/clock_pendulum/clock_pendulum.png");
    private static final RenderType BASE = RenderTypes.entityTranslucent(TEXTURE);
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/clock_pendulum/clock_pendulum_glow.png"));
    /** Das Modell der Klinge ist kleiner gebaut; so groß ist sie im Spiel. */
    private static final float BLADE_SCALE = 2.4f;

    /** Was das Pendel zum Zeichnen braucht. */
    public static class State extends EntityRenderState {
        public float direction;
        public float swing;
        public float alpha;
    }

    /** Ein Teil des Modells (rod, cap oder blade, siehe {@code ClockPendulumGeometry}); die anderen bleiben unsichtbar. */
    static final class PartModel extends EntityModel<State> {
        private final ModelPart part;
        /** Das Lager dreht sich mit dem Schwung (doppelt so weit). */
        private final boolean spins;

        PartModel(ModelPart root, String name) {
            super(root);
            for (String other : new String[] {"rod", "cap", "blade"}) {
                root.getChild(other).visible = other.equals(name);
            }
            this.part = root.getChild(name);
            this.spins = "cap".equals(name);
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            if (this.spins) {
                this.part.xRot = state.swing * Mth.DEG_TO_RAD * 2.0f;
            }
        }
    }

    private final PartModel rod;
    private final PartModel cap;
    private final PartModel blade;

    public ClockPendulumRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.rod = new PartModel(context.bakeLayer(ModModelLayers.CLOCK_PENDULUM), "rod");
        this.cap = new PartModel(context.bakeLayer(ModModelLayers.CLOCK_PENDULUM), "cap");
        this.blade = new PartModel(context.bakeLayer(ModModelLayers.CLOCK_PENDULUM), "blade");
        this.shadowRadius = 0.0f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ClockPendulumEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.direction = entity.direction();
        state.swing = entity.swingAngle(partialTicks);
        state.alpha = entity.alpha(partialTicks);
    }

    @Override
    protected AABB getBoundingBoxForCulling(ClockPendulumEntity entity, float partialTicks) {
        return super.getBoundingBoxForCulling(entity, partialTicks).inflate(16.0, 18.0, 16.0).move(0.0, -8.0, 0.0);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.alpha <= 0.01f) {
            return;
        }
        int color = ARGB.color(Math.round(state.alpha * 255.0f), 0xFFFFFF);
        poseStack.pushPose();
        poseStack.rotate(Axis.YP.rotationDegrees(180.0f - state.direction));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        // um das Lager schwingen (positiv: entlang der Bahn nach vorn, also nach -z im Modell)
        poseStack.rotate(Axis.XP.rotationDegrees(-state.swing));
        this.part(this.cap, collector, state, poseStack, color);
        int segments = Math.round(ClockPendulumEntity.LENGTH - 1.2f);
        for (int i = 0; i < segments; i++) {
            poseStack.pushPose();
            poseStack.translate(0.0f, i, 0.0f);
            this.part(this.rod, collector, state, poseStack, color);
            poseStack.popPose();
        }
        poseStack.pushPose();
        poseStack.translate(0.0f, ClockPendulumEntity.LENGTH - 1.3f, 0.0f);
        poseStack.scale(BLADE_SCALE / 1.6f, BLADE_SCALE / 1.6f, BLADE_SCALE / 1.6f);
        this.part(this.blade, collector, state, poseStack, color);
        poseStack.popPose();
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private void part(PartModel model, SubmitNodeCollector collector, State state, PoseStack poseStack, int color) {
        collector.submitModel(model, state, poseStack, BASE, state.lightCoords, OverlayTexture.NO_OVERLAY, color, null, state.outlineColor);
        collector.order(1).submitModel(model, state, poseStack, GLOW, state.lightCoords, OverlayTexture.NO_OVERLAY, color, null,
                state.outlineColor);
    }
}
