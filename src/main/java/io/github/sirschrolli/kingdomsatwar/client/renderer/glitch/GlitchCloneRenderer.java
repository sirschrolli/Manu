package io.github.sirschrolli.kingdomsatwar.client.renderer.glitch;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.AnniTrollEntity;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.GlitchCloneEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;

/**
 * Ein Fehler-Klon: Annis Gestalt in nur einer Farbe, leuchtend und durchscheinend, etwas kleiner als sie; er flackert und
 * ruckt wie sie, und manchmal ist er nur fehlende Textur.
 */
public class GlitchCloneRenderer extends MobRenderer<GlitchCloneEntity, AnniTrollRenderState, AnniTrollModel> {
    private static final float SCALE = AnniTrollRenderer.SCALE * 0.8f;

    public GlitchCloneRenderer(EntityRendererProvider.Context context) {
        super(context, new AnniTrollModel(context.bakeLayer(ModModelLayers.ANNI_TROLL)), 0.0f);
    }

    @Override
    public Identifier getTextureLocation(AnniTrollRenderState state) {
        return AnniTrollRenderer.missingFrame(state) ? AnniTrollRenderer.MISSING : AnniTrollRenderer.TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(AnniTrollRenderState state, boolean isBodyVisible, boolean forceTransparent,
            boolean appearGlowing) {
        return AnniTrollRenderer.missingFrame(state) ? net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(AnniTrollRenderer.MISSING)
                : AnniTrollRenderer.SPLIT;
    }

    @Override
    protected int getModelTint(AnniTrollRenderState state) {
        float flicker = AnniTrollRenderer.hash(state.seed, (int) state.ageInTicks, 50) < 0.15f ? 0.35f : 0.7f;
        return ARGB.color(Math.round(flicker * 255.0f), state.tint);
    }

    @Override
    public AnniTrollRenderState createRenderState() {
        return new AnniTrollRenderState();
    }

    @Override
    public void extractRenderState(GlitchCloneEntity entity, AnniTrollRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack = AnniTrollEntity.Attack.NONE.ordinal();
        state.attackTime = 0.0f;
        state.lagging = false;
        state.glitchMode = true;
        state.seed = entity.getId();
        state.clone = true;
        state.tint = GlitchCloneEntity.COLORS[Math.floorMod(entity.colorIndex(), GlitchCloneEntity.COLORS.length)];
        state.alpha = 0.7f;
        state.hasRedOverlay = false;
    }

    @Override
    protected void scale(AnniTrollRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    protected int getBlockLightLevel(GlitchCloneEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(AnniTrollRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        AnniTrollRenderer.jitter(state, poseStack);
        super.submit(state, poseStack, collector, camera);
        poseStack.popPose();
    }
}
