package io.github.sirschrolli.kingdomsatwar.client.renderer.arena;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.entity.arena.VentorEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Ventor, der Gladiator der Winde, 1,1-fach vergrößert (mit dem Helmbusch gut dreieinhalb Blöcke hoch): der Leib aus Bronze,
 * darunter der Wirbel – seine Windfäden auf dem Leib-Bild, dazu der durchscheinende Dunst als eigene Ebene, deren Bild rollt
 * wie beim Windgeist. Augen, Windfäden, die Spitzen des Dreizacks, die Windrune und die Risse im Erz leuchten (im entfesselten
 * Sturm heller); beim Aufsteigen wächst er aus dem Wirbel, beim Zurücksinken vergeht er darin. Er kippt im Tod nicht um – der
 * Wind erlahmt, und das Standbild stürzt vornüber.
 */
public class VentorRenderer extends MobRenderer<VentorEntity, VentorRenderState, VentorModel> {
    public static final float SCALE = 1.1f;
    private static final Identifier TEXTURE = ArenaRenderers.texture("ventor", "ventor");
    private static final RenderType GLOW = RenderTypes.eyes(ArenaRenderers.texture("ventor", "ventor_glow"));
    private static final Identifier WIND = ArenaRenderers.texture("ventor", "wind");

    public VentorRenderer(EntityRendererProvider.Context context) {
        super(context, new VentorModel(context.bakeLayer(ArenaRenderers.VENTOR)), 0.9f);
        this.addLayer(new Haze(this, new VentorModel(context.bakeLayer(ArenaRenderers.VENTOR_WIND))));
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, VentorRenderState state, float yRot, float xRot) {
                if (state.isInvisible) {
                    return;
                }
                float glow = glow(state);
                if (glow > 0.01f) {
                    collector.order(2).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(glow * 255.0f), 0xFFFFFF), null, state.outlineColor);
                }
            }
        });
    }

    /** Der durchscheinende Dunst im Wirbel: dasselbe Modell (gleich bewegt), nur die Kästen der Ringe, das Bild rollt. */
    private static final class Haze extends RenderLayer<VentorRenderState, VentorModel> {
        private final VentorModel model;

        Haze(RenderLayerParent<VentorRenderState, VentorModel> parent, VentorModel model) {
            super(parent);
            this.model = model;
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, VentorRenderState state, float yRot, float xRot) {
            if (state.isInvisible) {
                return;
            }
            float speed = state.enraged ? 0.035f : 0.025f;
            RenderType type = RenderTypes.breezeWind(WIND, state.ageInTicks * speed % 1.0f, 0.0f);
            collector.order(1).submitModel(this.model, state, poseStack, type, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        }
    }

    /** Wie hell er leuchtet: ein ruhiges Atmen, im entfesselten Sturm heller und unruhiger, im Tod verlöschend. */
    private static float glow(VentorRenderState state) {
        float t = state.ageInTicks;
        float glow = 0.82f + Mth.sin(t * 0.12f) * 0.08f + Mth.sin(t * 0.41f) * 0.04f;
        if (state.enraged) {
            glow = 0.92f + Mth.sin(t * 0.6f) * 0.08f;
        }
        if (state.deathTime > 0.0f) {
            glow *= 1.0f - Mth.clamp(state.deathTime / 50.0f, 0.0f, 1.0f);
        }
        return Mth.clamp(glow, 0.0f, 1.0f);
    }

    @Override
    public Identifier getTextureLocation(VentorRenderState state) {
        return TEXTURE;
    }

    @Override
    public VentorRenderState createRenderState() {
        return new VentorRenderState();
    }

    @Override
    public void extractRenderState(VentorEntity entity, VentorRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack = entity.attackOrdinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.trident = entity.hasTrident();
        state.net = entity.hasNet();
        state.enraged = entity.isEnraged();
        double dx = entity.getX() - entity.xo;
        double dz = entity.getZ() - entity.zo;
        state.drift = (float) Math.sqrt(dx * dx + dz * dz);
        state.owner = entity;
        if (entity.deathTime > 0) {
            state.hasRedOverlay = false;
        }
    }

    /** Er wächst beim Aufsteigen aus dem Wirbel und vergeht beim Zurücksinken darin. */
    @Override
    protected void scale(VentorRenderState state, PoseStack poseStack) {
        float s = SCALE;
        if (state.attack == VentorEntity.Attack.AWAKEN.ordinal()) {
            float f = Mth.clamp(state.attackTime / VentorEntity.FORM_TICKS, 0.0f, 1.0f);
            s *= Math.max(0.02f, f * f * (3.0f - 2.0f * f));
        } else if (state.attack == VentorEntity.Attack.RETREAT.ordinal()) {
            float f = 1.0f - Mth.clamp((state.attackTime - 10.0f) / 30.0f, 0.0f, 1.0f);
            s *= Math.max(0.02f, f * f * (3.0f - 2.0f * f));
        }
        poseStack.scale(s, s, s);
    }

    /** Er kippt im Tod nicht zur Seite (siehe {@link VentorModel}). */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }

    @Override
    protected int getBlockLightLevel(VentorEntity entity, BlockPos pos) {
        // die Augen, die Windfäden und die Zinken leuchten: nie ganz dunkel
        return Math.max(6, super.getBlockLightLevel(entity, pos));
    }
}
