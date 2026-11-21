package io.github.sirschrolli.kingdomsatwar.client.renderer.glitch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.MissingZoneEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Ein Feld fehlender Textur: Magenta und Schwarz im Schachbrett, ein Feld je Block, flach über dem Boden. Zuerst blinkt nur
 * der Rand (Zeit zu fliehen), dann ist es scharf und pulsiert; einzelne Felder flackern auf. Zum Ende verblasst es.
 */
public class MissingZoneRenderer extends EntityRenderer<MissingZoneEntity, MissingZoneRenderer.State> {
    private static final RenderType TYPE = RenderTypes.entityTranslucentEmissive(KingdomsAtWar.id("textures/entity/anni_troll/missing_cell.png"));
    private static final int FULL_BRIGHT = 0xF000F0;

    public static class State extends EntityRenderState {
        public int half;
        public float age;
        public int seed;
    }

    public MissingZoneRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MissingZoneEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.half = entity.half();
        state.age = entity.tickCount + partialTicks;
        state.seed = entity.getId();
    }

    @Override
    protected int getBlockLightLevel(MissingZoneEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected boolean affectedByCulling(MissingZoneEntity entity) {
        return false;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int half = state.half;
        boolean armed = state.age >= MissingZoneEntity.WARMUP;
        float out = Mth.clamp((MissingZoneEntity.LIFE - state.age) / 15.0f, 0.0f, 1.0f);
        float in = armed ? 1.0f : 0.0f;
        float pulse = 0.8f + Mth.sin(state.age * 0.5f) * 0.15f;
        int tick = (int) state.age;
        collector.submitCustomGeometry(poseStack, TYPE, (pose, buffer) -> {
            for (int x = -half; x <= half; x++) {
                for (int z = -half; z <= half; z++) {
                    boolean edge = Math.abs(x) == half || Math.abs(z) == half;
                    if (!armed && !(edge && (tick / 3) % 2 == 0)) {
                        continue;
                    }
                    float flicker = AnniTrollRenderer.hash(state.seed + x * 31 + z * 17, tick, 60) < 0.06f ? 0.4f : 1.0f;
                    float alpha = (armed ? pulse * in : 0.8f) * out * flicker;
                    // ein Feld: magenta oder schwarz (die Textur ist ein Feld mit feinem Rand; die Farbe kommt aus der Tönung)
                    boolean magenta = ((x + z) & 1) == 0;
                    int tint = ARGB.color(Math.round(alpha * 230.0f), magenta ? 0xF800F8 : 0x140014);
                    float y = 0.03f + (magenta ? 0.002f : 0.0f);
                    cell(buffer, pose, x - 0.5f, z - 0.5f, x + 0.5f, z + 0.5f, y, tint);
                }
            }
        });
        super.submit(state, poseStack, collector, camera);
    }

    private static void cell(VertexConsumer buffer, PoseStack.Pose pose, float x0, float z0, float x1, float z1, float y, int color) {
        buffer.addVertex(pose, x0, y, z0).setColor(color).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, x0, y, z1).setColor(color).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, x1, y, z1).setColor(color).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, x1, y, z0).setColor(color).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0, 1, 0);
    }
}
