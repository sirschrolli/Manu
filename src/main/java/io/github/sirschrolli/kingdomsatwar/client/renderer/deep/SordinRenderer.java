package io.github.sirschrolli.kingdomsatwar.client.renderer.deep;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.entity.deep.SordinEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Sordin, der Kantor der Stille, 1,3-fach vergrößert (mit den Pfeifen über vier Blöcke hoch): der singende Mund, die
 * Fensterrose auf der Brust, die Münder der Pfeifen, die Fühler und die Zinken der Stimmgabel leuchten und atmen; im
 * Fortissimo glüht alles heller, beim Singen pulsiert es. Im Sterben erlischt das Licht. Er kippt nicht um – er sinkt auf die
 * Knie.
 */
public class SordinRenderer extends MobRenderer<SordinEntity, SordinRenderState, SordinModel> {
    public static final float SCALE = 1.3f;
    private static final Identifier TEXTURE = DeepRenderers.texture("sordin", "sordin");
    private static final RenderType GLOW = RenderTypes.eyes(DeepRenderers.texture("sordin", "sordin_glow"));

    public SordinRenderer(EntityRendererProvider.Context context) {
        super(context, new SordinModel(context.bakeLayer(DeepRenderers.SORDIN)), 0.9f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, SordinRenderState state, float yRot, float xRot) {
                if (state.isInvisible) {
                    return;
                }
                float glow = glow(state);
                if (glow > 0.01f) {
                    collector.order(1).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(glow * 255.0f), 0xFFFFFF), null, state.outlineColor);
                }
                if (state.fortissimo && state.deathTime <= 0.0f) {
                    // im Fortissimo: das Licht ein zweites Mal, heller
                    float flare = 0.4f + Mth.sin(state.ageInTicks * 0.3f) * 0.2f;
                    collector.order(2).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(flare * 255.0f), 0xD8FFFF), null, state.outlineColor);
                }
            }
        });
    }

    /** Wie hell das Licht ist: ein ruhiges Atmen, an der Orgel gedämpft, beim Singen pulsierend, im Tod verlöschend. */
    private static float glow(SordinRenderState state) {
        float t = state.ageInTicks;
        float glow = 0.84f + Mth.sin(t * 0.1f) * 0.08f + Mth.sin(t * 0.37f) * 0.04f;
        if (state.attack == SordinEntity.Attack.GUARD.ordinal()) {
            glow *= 0.72f;
        }
        if (state.attack == SordinEntity.Attack.NOTES.ordinal() || state.attack == SordinEntity.Attack.FORTISSIMO.ordinal()) {
            glow = 0.9f + Mth.sin(t * 0.8f) * 0.1f;
        }
        if (state.deathTime > 0.0f) {
            glow *= 1.0f - Mth.clamp(state.deathTime / SordinEntity.DEATH_TICKS, 0.0f, 1.0f) * 0.95f;
        }
        return Mth.clamp(glow, 0.0f, 1.0f);
    }

    @Override
    public Identifier getTextureLocation(SordinRenderState state) {
        return TEXTURE;
    }

    @Override
    public SordinRenderState createRenderState() {
        return new SordinRenderState();
    }

    @Override
    public void extractRenderState(SordinEntity entity, SordinRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack = entity.attackOrdinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.fortissimo = entity.isFortissimo();
        double dx = entity.getX() - entity.xo;
        double dz = entity.getZ() - entity.zo;
        state.drift = (float) Math.sqrt(dx * dx + dz * dz);
        state.owner = entity;
        if (entity.deathTime > 0) {
            state.hasRedOverlay = false;
        }
    }

    @Override
    protected void scale(SordinRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    /** Er kippt im Tod nicht zur Seite, er sinkt auf die Knie (siehe {@link SordinModel}). */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }

    @Override
    protected int getBlockLightLevel(SordinEntity entity, BlockPos pos) {
        // der Mund, die Fensterrose und die Pfeifen leuchten: nie ganz dunkel
        return Math.max(7, super.getBlockLightLevel(entity, pos));
    }
}
