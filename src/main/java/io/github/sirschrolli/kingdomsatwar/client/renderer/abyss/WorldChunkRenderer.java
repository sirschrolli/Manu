package io.github.sirschrolli.kingdomsatwar.client.renderer.abyss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.WorldChunkEntity;
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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Ein ausgespiener Weltbrocken: ein kantiger Fels seiner Welt (Glut, Frost, End, Schatten), der sich im Flug überschlägt;
 * seine Adern glühen.
 */
public class WorldChunkRenderer extends EntityRenderer<WorldChunkEntity, WorldChunkRenderer.State> {
    private static final Identifier[] TEXTURES = new Identifier[WorldChunkEntity.Kind.values().length];
    private static final RenderType[] GLOW = new RenderType[WorldChunkEntity.Kind.values().length];

    static {
        for (WorldChunkEntity.Kind kind : WorldChunkEntity.Kind.values()) {
            String name = kind.name().toLowerCase(java.util.Locale.ROOT);
            TEXTURES[kind.ordinal()] = KingdomsAtWar.id("textures/entity/world_chunk/" + name + ".png");
            GLOW[kind.ordinal()] = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/world_chunk/" + name + "_glow.png"));
        }
    }

    public static class State extends EntityRenderState {
        public int kind;
        public float spin;
        public long seed;
    }

    public static class Model extends EntityModel<State> {
        private final ModelPart chunk;

        public Model(ModelPart root) {
            super(root);
            this.chunk = root.getChild("chunk");
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            float s = state.spin;
            this.chunk.xRot = s * 0.21f + (state.seed & 7);
            this.chunk.yRot = s * 0.13f + ((state.seed >> 3) & 7);
            this.chunk.zRot = s * 0.08f;
        }
    }

    private final Model model;

    public WorldChunkRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new Model(context.bakeLayer(ModModelLayers.WORLD_CHUNK));
        this.shadowRadius = 0.6f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WorldChunkEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.kind = entity.kind().ordinal();
        state.spin = entity.tickCount + partialTicks;
        state.seed = entity.getId() * 2_654_435_761L;
    }

    @Override
    protected int getBlockLightLevel(WorldChunkEntity entity, BlockPos pos) {
        return Math.max(super.getBlockLightLevel(entity, pos), 9);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.0f, 0.5f, 0.0f);
        pose.rotate(Axis.YP.rotationDegrees(state.seed % 360));
        float scale = 1.55f;
        pose.scale(-scale, -scale, scale);
        collector.submitModel(this.model, state, pose, TEXTURES[state.kind], state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        int glow = ARGB.color(Math.round((0.8f + 0.2f * Mth.sin(state.spin * 0.6f)) * 255.0f), 0xFFFFFF);
        collector.order(1).submitModel(this.model, state, pose, GLOW[state.kind], state.lightCoords, OverlayTexture.NO_OVERLAY, glow, null,
                state.outlineColor);
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }
}
