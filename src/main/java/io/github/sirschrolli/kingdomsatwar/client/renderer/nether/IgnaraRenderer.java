package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.nether.IgnaraEntity;
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
 * Ignara, die Flammenpriesterin, 1,2-fach vergrößert (mit dem Flammenhaar gut drei Blöcke hoch): Haar, Saum, Feuerkugel,
 * Peitschenschnur, Opale, Glutzeichen und die Sehschlitze der Maske glühen und atmen; als Phönix lodert alles heller. Im
 * Sterben verlischt das Feuer. Sie kippt nicht um wie andere Wesen – sie sinkt in sich zusammen.
 */
public class IgnaraRenderer extends MobRenderer<IgnaraEntity, IgnaraRenderState, IgnaraModel> {
    public static final float SCALE = 1.2f;
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/ignara/ignara.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/ignara/ignara_glow.png"));

    public IgnaraRenderer(EntityRendererProvider.Context context) {
        super(context, new IgnaraModel(context.bakeLayer(ModModelLayers.IGNARA)), 1.0f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, IgnaraRenderState state, float yRot,
                    float xRot) {
                if (state.isInvisible) {
                    return;
                }
                float glow = glow(state);
                if (glow > 0.01f) {
                    collector.order(1).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(glow * 255.0f), 0xFFFFFF), null, state.outlineColor);
                }
                if (state.phoenix && state.deathTime <= 0.0f) {
                    // als Phönix: das Feuer ein zweites Mal, heller und golden
                    float heat = 0.5f + Mth.sin(state.ageInTicks * 0.3f) * 0.2f;
                    collector.order(2).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(heat * 255.0f), 0xFFE6A0), null, state.outlineColor);
                }
            }
        });
    }

    /** Wie hell das Feuer ist: ein ruhiges Atmen, im Gebet gedämpfter, im Tod verlöschend. */
    private static float glow(IgnaraRenderState state) {
        float t = state.ageInTicks;
        float glow = 0.86f + Mth.sin(t * 0.11f) * 0.08f + Mth.sin(t * 0.43f) * 0.04f;
        if (state.attack == IgnaraEntity.Attack.PRAY.ordinal()) {
            glow *= 0.85f;
        }
        if (state.deathTime > 0.0f) {
            glow *= 1.0f - Mth.clamp(state.deathTime / IgnaraEntity.DEATH_TICKS, 0.0f, 1.0f) * 0.95f;
        }
        return Mth.clamp(glow, 0.0f, 1.0f);
    }

    @Override
    public Identifier getTextureLocation(IgnaraRenderState state) {
        return TEXTURE;
    }

    @Override
    public IgnaraRenderState createRenderState() {
        return new IgnaraRenderState();
    }

    @Override
    public void extractRenderState(IgnaraEntity entity, IgnaraRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack = entity.currentAttack().ordinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.phoenix = entity.isPhoenix();
        double dx = entity.getX() - entity.xo;
        double dz = entity.getZ() - entity.zo;
        state.drift = (float) Math.sqrt(dx * dx + dz * dz);
        state.owner = entity;
        if (entity.deathTime > 0) {
            state.hasRedOverlay = false;
        }
    }

    @Override
    protected void scale(IgnaraRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    /** Sie kippt im Tod nicht zur Seite, sie sinkt in sich zusammen (siehe {@link IgnaraModel}). */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }

    @Override
    protected int getBlockLightLevel(IgnaraEntity entity, BlockPos pos) {
        // sie brennt von innen: nie ganz dunkel
        return Math.max(12, super.getBlockLightLevel(entity, pos));
    }
}
