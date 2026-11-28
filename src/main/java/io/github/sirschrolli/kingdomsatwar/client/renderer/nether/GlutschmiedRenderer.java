package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.nether.GlutschmiedEntity;
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
 * Malleus, der Glutschmied, 1,4-fach vergrößert (knapp vier Blöcke hoch): Augen, Herzesse, Glutadern, Hammerbahnen und
 * die Klinge des Speers glühen und atmen; weißglühend leuchtet alles doppelt so hell. Im Sterben verlischt die Glut. Er
 * kippt nicht um wie andere Wesen – er sinkt auf die Knie.
 */
public class GlutschmiedRenderer extends MobRenderer<GlutschmiedEntity, GlutschmiedRenderState, GlutschmiedModel> {
    public static final float SCALE = 1.4f;
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/glutschmied/glutschmied.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/glutschmied/glutschmied_glow.png"));

    public GlutschmiedRenderer(EntityRendererProvider.Context context) {
        super(context, new GlutschmiedModel(context.bakeLayer(ModModelLayers.GLUTSCHMIED)), 1.5f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, GlutschmiedRenderState state, float yRot,
                    float xRot) {
                if (state.isInvisible) {
                    return;
                }
                float glow = glow(state);
                if (glow > 0.01f) {
                    collector.order(1).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(glow * 255.0f), 0xFFFFFF), null, state.outlineColor);
                }
                if (state.stoked && state.deathTime <= 0.0f) {
                    // weißglühend: die Glut ein zweites Mal, heller und gelblich
                    float heat = 0.55f + Mth.sin(state.ageInTicks * 0.3f) * 0.2f;
                    collector.order(2).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(heat * 255.0f), 0xFFE6B0), null, state.outlineColor);
                }
            }
        });
    }

    /** Wie hell die Glut ist: ein ruhiges Atmen, beim Schmieden heller, im Tod verlöschend. */
    private static float glow(GlutschmiedRenderState state) {
        float t = state.ageInTicks;
        float glow = 0.82f + Mth.sin(t * 0.09f) * 0.1f + Mth.sin(t * 0.37f) * 0.04f;
        if (state.deathTime > 0.0f) {
            glow *= 1.0f - Mth.clamp(state.deathTime / GlutschmiedEntity.DEATH_TICKS, 0.0f, 1.0f) * 0.95f;
        }
        return Mth.clamp(glow, 0.0f, 1.0f);
    }

    @Override
    public Identifier getTextureLocation(GlutschmiedRenderState state) {
        return TEXTURE;
    }

    @Override
    public GlutschmiedRenderState createRenderState() {
        return new GlutschmiedRenderState();
    }

    @Override
    public void extractRenderState(GlutschmiedEntity entity, GlutschmiedRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack = entity.currentAttack().ordinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.stoked = entity.isStoked();
        state.spear = entity.holdsSpear(state.attackTime);
        state.owner = entity;
        // im langen Sterben nicht rot getönt (er verglüht, siehe die Leuchtebene)
        if (entity.deathTime > 0) {
            state.hasRedOverlay = false;
        }
    }

    @Override
    protected void scale(GlutschmiedRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    /** Er kippt im Tod nicht zur Seite, er sinkt auf die Knie (siehe {@link GlutschmiedModel}). */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }

    @Override
    protected int getBlockLightLevel(GlutschmiedEntity entity, BlockPos pos) {
        // er glüht von innen: selbst im Schatten nie ganz dunkel
        return Math.max(11, super.getBlockLightLevel(entity, pos));
    }
}
