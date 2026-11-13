package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.entity.end.PhasmorEntity;
import io.github.sirschrolli.kingdomsatwar.entity.end.PhasmorLike;
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
import net.minecraft.world.entity.monster.Monster;
import org.jspecify.annotations.Nullable;

/**
 * Phasmor, der Leerwandlerkönig, 1,35-fach vergrößert (mit der Krone fast fünf Blöcke hoch): Augen, Krone, Risse, Phasite,
 * Fäden des Mantels und die Klinge leuchten und atmen; gekrönt lodert alles heller. Im Sterben erlischt das Licht. Er kippt
 * nicht um wie andere Wesen – er sinkt auf die Knie. Seine Trugbilder sehen aus wie er, nur durchscheinend und ein wenig
 * blasser.
 */
public class PhasmorRenderer<T extends Monster & PhasmorLike> extends MobRenderer<T, PhasmorRenderState, PhasmorModel> {
    public static final float SCALE = 1.35f;
    private static final Identifier TEXTURE = EndRenderers.texture("phasmor", "phasmor");
    private static final RenderType GLOW = RenderTypes.eyes(EndRenderers.texture("phasmor", "phasmor_glow"));

    public PhasmorRenderer(EntityRendererProvider.Context context) {
        super(context, new PhasmorModel(context.bakeLayer(EndRenderers.PHASMOR)), 0.9f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, PhasmorRenderState state, float yRot,
                    float xRot) {
                if (state.isInvisible) {
                    return;
                }
                float glow = glow(state);
                if (glow > 0.01f) {
                    collector.order(1).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(glow * 255.0f), 0xFFFFFF), null, state.outlineColor);
                }
                if (state.crowned && !state.illusion && state.deathTime <= 0.0f) {
                    // gekrönt: das Licht ein zweites Mal, heller
                    float flare = 0.45f + Mth.sin(state.ageInTicks * 0.25f) * 0.2f;
                    collector.order(2).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(flare * 255.0f), 0xD8FFF6), null, state.outlineColor);
                }
            }
        });
    }

    /** Wie hell das Licht ist: ein ruhiges Atmen, beim Wachen gedämpft, im Tod verlöschend, als Trugbild blasser. */
    private static float glow(PhasmorRenderState state) {
        float t = state.ageInTicks;
        float glow = 0.86f + Mth.sin(t * 0.1f) * 0.08f + Mth.sin(t * 0.37f) * 0.04f;
        if (state.attack == PhasmorEntity.Attack.GUARD.ordinal()) {
            glow *= 0.7f;
        }
        if (state.illusion) {
            glow *= 0.6f;
        }
        if (state.deathTime > 0.0f) {
            glow *= 1.0f - Mth.clamp(state.deathTime / PhasmorEntity.DEATH_TICKS, 0.0f, 1.0f) * 0.95f;
        }
        return Mth.clamp(glow, 0.0f, 1.0f);
    }

    @Override
    public Identifier getTextureLocation(PhasmorRenderState state) {
        return TEXTURE;
    }

    @Override
    public PhasmorRenderState createRenderState() {
        return new PhasmorRenderState();
    }

    @Override
    public void extractRenderState(T entity, PhasmorRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack = entity.attackOrdinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.crowned = entity.isCrowned();
        state.illusion = entity.isIllusion();
        double dx = entity.getX() - entity.xo;
        double dz = entity.getZ() - entity.zo;
        state.drift = (float) Math.sqrt(dx * dx + dz * dz);
        state.owner = entity;
        if (entity.deathTime > 0) {
            state.hasRedOverlay = false;
        }
    }

    @Override
    protected void scale(PhasmorRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    /** Er kippt im Tod nicht zur Seite, er sinkt auf die Knie (siehe {@link PhasmorModel}). */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }

    /** Trugbilder: durchscheinend und ein wenig türkis. */
    @Override
    protected @Nullable RenderType getRenderType(PhasmorRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        if (state.illusion && isBodyVisible) {
            return RenderTypes.entityTranslucentCull(this.getTextureLocation(state));
        }
        return super.getRenderType(state, isBodyVisible, forceTransparent, appearGlowing);
    }

    @Override
    protected int getModelTint(PhasmorRenderState state) {
        return state.illusion ? ARGB.color(175, 0xC8FFF2) : -1;
    }

    @Override
    protected int getBlockLightLevel(T entity, BlockPos pos) {
        // die Augen und Risse leuchten: nie ganz dunkel
        return Math.max(9, super.getBlockLightLevel(entity, pos));
    }
}
