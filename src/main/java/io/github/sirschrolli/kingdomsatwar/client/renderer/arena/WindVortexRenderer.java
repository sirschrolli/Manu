package io.github.sirschrolli.kingdomsatwar.client.renderer.arena;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.entity.arena.WindVortexEntity;
import java.util.function.Function;
import net.minecraft.client.model.Model;
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
import net.minecraft.util.Mth;

/**
 * Eine Windhose: derselbe Wirbel wie unter Ventors Leib (seine Windfäden und der rollende Dunst), nur gut doppelt so groß und
 * ohne den Leib darauf – sie wächst aus dem Boden, dreht sich rasend und fällt am Ende in sich zusammen. Staub und Sand am
 * Fuß zeichnen die Teilchen ({@link WindVortexEntity}).
 */
public class WindVortexRenderer extends EntityRenderer<WindVortexEntity, WindVortexRenderer.State> {
    private static final float SCALE = 2.2f;
    private static final Identifier TEXTURE = ArenaRenderers.texture("ventor", "ventor");
    private static final RenderType GLOW = RenderTypes.eyes(ArenaRenderers.texture("ventor", "ventor_glow"));
    private static final Identifier WIND = ArenaRenderers.texture("ventor", "wind");
    private final Funnel shell;
    private final Funnel haze;

    public static class State extends EntityRenderState {
        public float age;
        public float seed;
    }

    /** Nur der Wirbel aus Ventors Modell; seine Ringe drehen sich je nach Alter der Windhose (beim Zeichnen gesetzt). */
    private static final class Funnel extends Model<State> {
        private final ModelPart[] rings = new ModelPart[4];

        Funnel(ModelPart wind, Function<Identifier, RenderType> type) {
            super(wind, type);
            ModelPart ring = wind;
            for (int i = 0; i < this.rings.length; i++) {
                ring = ring.getChild("ring_" + i);
                this.rings[i] = ring;
            }
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            float t = state.age + state.seed;
            for (int i = 0; i < this.rings.length; i++) {
                ModelPart ring = this.rings[i];
                ring.yRot = t * (0.3f + i * 0.07f);
                if (i > 0) {
                    ring.x += Mth.sin(t * 0.17f + i * 0.9f) * 0.5f * i;
                    ring.z += Mth.cos(t * 0.15f + i * 1.3f) * 0.4f * i;
                }
            }
            this.root().xRot = Mth.sin(t * 0.08f) * 0.06f;
            this.root().zRot = Mth.cos(t * 0.07f) * 0.06f;
        }
    }

    public WindVortexRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shell = new Funnel(context.bakeLayer(ArenaRenderers.VENTOR).getChild("wind"), RenderTypes::entityCutout);
        this.haze = new Funnel(context.bakeLayer(ArenaRenderers.VENTOR_WIND).getChild("wind"), RenderTypes::entityCutout);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WindVortexEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.age = entity.tickCount + partialTicks;
        state.seed = (entity.getId() * 37) % 100;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float grow = Mth.clamp(state.age / 10.0f, 0.0f, 1.0f);
        float fade = Mth.clamp((WindVortexEntity.LIFETIME + 2.0f - state.age) / 10.0f, 0.0f, 1.0f);
        float s = SCALE * Math.max(0.05f, Math.min(grow, fade));
        poseStack.pushPose();
        poseStack.scale(s, s, s);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(0.0f, -1.501f, 0.0f);
        collector.submitModel(this.shell, state, poseStack, RenderTypes.entityCutout(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY,
                -1, null, 0);
        RenderType wind = RenderTypes.breezeWind(WIND, state.age * 0.04f % 1.0f, 0.0f);
        collector.order(1).submitModel(this.haze, state, poseStack, wind, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, 0);
        collector.order(2).submitModel(this.shell, state, poseStack, GLOW, 0xF000F0, OverlayTexture.NO_OVERLAY, -1, null, 0);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
