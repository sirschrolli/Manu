package io.github.sirschrolli.kingdomsatwar.client.renderer.mage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.model.mage.MageModel;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageLike;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import org.jspecify.annotations.Nullable;

/**
 * Erzmagier und ihre Spiegelbilder: 1,3-fache Größe, leuchtende Augen, Runen und Kristalle (eigene Leuchtebene), unter
 * ihnen kreist ein Siegel aus Licht. Beim Blinzeln blenden sie aus und ein, Spiegelbilder sind durchscheinend und
 * flimmern. Ein aktiver Schild erscheint als zwei gegenläufig kreisende Runenringe in einem Schleier aus Licht.
 */
public class MageRenderer<T extends Mob & MageLike> extends MobRenderer<T, MageRenderState, MageModel> {
    private static final Map<DragonType, Identifier> TEXTURES = new EnumMap<>(DragonType.class);
    private static final Map<DragonType, RenderType> GLOW = new EnumMap<>(DragonType.class);
    private static final Identifier SIGIL = KingdomsAtWar.id("textures/entity/mage/sigil.png");
    private static final Identifier GLYPHS = KingdomsAtWar.id("textures/block/magic_table_glyphs.png");
    private static final Identifier VEIL = KingdomsAtWar.id("textures/block/magic_table_beam.png");
    private static final RenderType SIGIL_TYPE = RenderTypes.entityTranslucentEmissive(SIGIL);
    private static final RenderType GLYPH_TYPE = RenderTypes.entityTranslucentEmissive(GLYPHS);
    private static final RenderType VEIL_TYPE = RenderTypes.entityTranslucentEmissive(VEIL);
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final float SCALE = 1.3f;

    static {
        for (DragonType type : DragonType.values()) {
            TEXTURES.put(type, KingdomsAtWar.id("textures/entity/mage/" + type.id() + ".png"));
            GLOW.put(type, RenderTypes.eyes(KingdomsAtWar.id("textures/entity/mage/" + type.id() + "_glow.png")));
        }
    }

    public MageRenderer(EntityRendererProvider.Context context) {
        super(context, new MageModel(context.bakeLayer(ModModelLayers.MAGE)), 0.6f);
        this.addLayer(new RenderLayer<MageRenderState, MageModel>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, MageRenderState state, float yRot,
                    float xRot) {
                int alpha = Math.round(255 * state.visibility);
                if (alpha < 8) {
                    return;
                }
                collector.order(1).submitModel(this.getParentModel(), state, poseStack, GLOW.get(state.element), lightCoords,
                        OverlayTexture.NO_OVERLAY, ARGB.color(255, alpha, alpha, alpha), null, state.outlineColor);
            }
        });
    }

    @Override
    public Identifier getTextureLocation(MageRenderState state) {
        return TEXTURES.get(state.element);
    }

    @Override
    public MageRenderState createRenderState() {
        return new MageRenderState();
    }

    @Override
    public void extractRenderState(T entity, MageRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.element = entity.element();
        state.gesture = entity.gesture();
        state.gestureTime = Anim.seconds(entity.gestureAnimation(), state.ageInTicks);
        state.visibility = entity.visibility(partialTicks);
        state.shield = entity.shield(partialTicks);
        state.dormant = entity.dormant();
        state.dying = entity.dyingTicks() > 0 ? entity.dyingTicks() + partialTicks : 0.0f;
        state.image = entity.isImage();
        // sterbend kein rotes Aufleuchten und kein Umkippen: der Magier löst sich schwebend in Funken auf
        state.hasRedOverlay = entity.hurtTime > 0 && state.dying <= 0.0f;
        double y = Mth.lerp(partialTicks, entity.yo, entity.getY());
        net.minecraft.world.phys.Vec3 from = new net.minecraft.world.phys.Vec3(entity.getX(), y + 0.3, entity.getZ());
        var hit = entity.level().clip(new net.minecraft.world.level.ClipContext(from, from.add(0, -3.0, 0),
                net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, entity));
        state.floorOffset = hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS ? 0.4f : (float) (y - hit.getLocation().y);
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }

    @Override
    protected void scale(MageRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
        if (state.dying > 0.0f) {
            poseStack.translate(0.0f, -state.dying * 0.012f, 0.0f);    // steigt sterbend auf
        }
    }

    @Override
    protected @Nullable RenderType getRenderType(MageRenderState state, boolean isBodyVisible, boolean forceTransparent,
            boolean appearGlowing) {
        if (state.visibility < 0.99f || state.image) {
            return RenderTypes.entityTranslucent(this.getTextureLocation(state));
        }
        return super.getRenderType(state, isBodyVisible, forceTransparent, appearGlowing);
    }

    @Override
    protected int getModelTint(MageRenderState state) {
        int alpha = Math.round(255 * state.visibility);
        if (state.image) {
            int rgb = ARGB.srgbLerp(0.35f, 0xFFFFFF, state.element.color());
            return ARGB.color(alpha, rgb);
        }
        return ARGB.color(alpha, 0xFFFFFF);
    }

    @Override
    protected int getBlockLightLevel(T entity, BlockPos pos) {
        return Math.max(super.getBlockLightLevel(entity, pos), 9);
    }

    @Override
    public void submit(MageRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (!state.image && state.dying <= 0.0f) {
            this.submitSigil(state, poseStack, collector);
        }
        if (state.shield > 0.01f) {
            this.submitShield(state, poseStack, collector);
        }
    }

    /** Siegel aus Licht unter dem Magier (in der Meditation hell, im Kampf schwächer). */
    private void submitSigil(MageRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        float alpha = (state.dormant ? 0.85f : 0.45f) * state.visibility;
        int color = ARGB.color(Math.round(255 * alpha), state.element.color());
        poseStack.pushPose();
        poseStack.translate(0.0f, -state.floorOffset + 0.03f, 0.0f);
        poseStack.rotateDegrees(Axis.YP, state.ageInTicks * (state.dormant ? 0.6f : 2.0f));
        float size = 2.8f;
        poseStack.scale(size, 1.0f, size);
        collector.submitCustomGeometry(poseStack, SIGIL_TYPE, (pose, buffer) -> flatQuad(pose, buffer, color));
        poseStack.popPose();
    }

    /** Schild: zwei gegenläufige Runenringe und ein Schleier aus Licht. */
    private void submitShield(MageRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        float s = state.shield;
        int rgb = state.element.color();
        for (int ring = 0; ring < 2; ring++) {
            float height = ring == 0 ? 0.7f : 2.0f;
            float spin = state.ageInTicks * (ring == 0 ? 3.0f : -2.2f);
            int color = ARGB.color(Math.round(230 * s), ARGB.srgbLerp(0.4f, rgb, 0xFFFFFF));
            for (int i = 0; i < 10; i++) {
                int glyph = (i + ring * 3) % 8;
                float angle = spin + i * 36.0f;
                poseStack.pushPose();
                poseStack.rotateDegrees(Axis.YP, angle);
                poseStack.translate(0.0f, height + Mth.sin(state.ageInTicks * 0.1f + i) * 0.05f, 1.15f);
                poseStack.scale(0.42f, 0.42f, 0.42f);
                collector.submitCustomGeometry(poseStack, GLYPH_TYPE, (pose, buffer) -> glyphQuad(pose, buffer, glyph, color));
                poseStack.popPose();
            }
        }
        int veil = ARGB.color(Math.round(110 * s), rgb);
        for (int i = 0; i < 12; i++) {
            poseStack.pushPose();
            poseStack.rotateDegrees(Axis.YP, i * 30.0f + state.ageInTicks * 0.8f);
            poseStack.translate(0.0f, -0.2f, 1.2f);
            poseStack.scale(0.66f, 3.2f, 1.0f);
            collector.submitCustomGeometry(poseStack, VEIL_TYPE, (pose, buffer) -> veilQuad(pose, buffer, veil));
            poseStack.popPose();
        }
    }

    private static void flatQuad(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        vertex(buffer, pose, -0.5f, 0.0f, -0.5f, 0.0f, 0.0f, color);
        vertex(buffer, pose, -0.5f, 0.0f, 0.5f, 0.0f, 1.0f, color);
        vertex(buffer, pose, 0.5f, 0.0f, 0.5f, 1.0f, 1.0f, color);
        vertex(buffer, pose, 0.5f, 0.0f, -0.5f, 1.0f, 0.0f, color);
        vertex(buffer, pose, 0.5f, 0.0f, -0.5f, 1.0f, 0.0f, color);
        vertex(buffer, pose, 0.5f, 0.0f, 0.5f, 1.0f, 1.0f, color);
        vertex(buffer, pose, -0.5f, 0.0f, 0.5f, 0.0f, 1.0f, color);
        vertex(buffer, pose, -0.5f, 0.0f, -0.5f, 0.0f, 0.0f, color);
    }

    private static void glyphQuad(PoseStack.Pose pose, VertexConsumer buffer, int glyph, int color) {
        float u0 = glyph / 8.0f;
        float u1 = (glyph + 1) / 8.0f;
        vertex(buffer, pose, -0.5f, -0.5f, 0.0f, u0, 1.0f, color);
        vertex(buffer, pose, 0.5f, -0.5f, 0.0f, u1, 1.0f, color);
        vertex(buffer, pose, 0.5f, 0.5f, 0.0f, u1, 0.0f, color);
        vertex(buffer, pose, -0.5f, 0.5f, 0.0f, u0, 0.0f, color);
        vertex(buffer, pose, -0.5f, 0.5f, 0.0f, u0, 0.0f, color);
        vertex(buffer, pose, 0.5f, 0.5f, 0.0f, u1, 0.0f, color);
        vertex(buffer, pose, 0.5f, -0.5f, 0.0f, u1, 1.0f, color);
        vertex(buffer, pose, -0.5f, -0.5f, 0.0f, u0, 1.0f, color);
    }

    private static void veilQuad(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        vertex(buffer, pose, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, color);
        vertex(buffer, pose, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, color);
        vertex(buffer, pose, 0.5f, 1.0f, 0.0f, 1.0f, 0.0f, color);
        vertex(buffer, pose, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, color);
        vertex(buffer, pose, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, color);
        vertex(buffer, pose, 0.5f, 1.0f, 0.0f, 1.0f, 0.0f, color);
        vertex(buffer, pose, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, color);
        vertex(buffer, pose, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, color);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}
