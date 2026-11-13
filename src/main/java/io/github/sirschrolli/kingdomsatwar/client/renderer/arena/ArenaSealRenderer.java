package io.github.sirschrolli.kingdomsatwar.client.renderer.arena;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.arena.ArenaSealBlock;
import io.github.sirschrolli.kingdomsatwar.block.arena.ArenaSealBlockEntity;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Die Windsiegel über dem Arenasiegel: rings um den Spawner der Böen schwebt je Käfig ein kupfernes Siegel mit einer
 * Windspirale, zu seinem Käfig gewandt. Noch nicht bezwungen ist es matt; ist der Käfig bezwungen, leuchtet es türkis. Sind
 * alle bezwungen (der Meister ist bereit), kreisen sie um den Spawner und pulsieren; solange er kämpft, sind sie fort.
 */
public class ArenaSealRenderer implements BlockEntityRenderer<ArenaSealBlockEntity, ArenaSealRenderer.State> {
    private static final RenderType TYPE = RenderTypes.entityCutout(KingdomsAtWar.id("textures/entity/ventor/sigil.png"));
    /**
     * So weit von der Mitte schweben die Siegel (Blöcke), so groß sind sie, so hoch über dem Arenasiegel: als Kranz über dem
     * Spawner der Böen (er steht auf dem Siegel und reicht bis 2 Blöcke darüber), leicht nach oben geneigt, damit man sie
     * von den Rängen und vom Sand aus sieht.
     */
    private static final float RADIUS = 1.2f;
    private static final float SIZE = 0.72f;
    private static final float HEIGHT = 2.85f;
    private static final float TILT = 0.35f;
    /** Ein mattes Siegel ist halbhell (Block- und Himmelslicht 7). */
    private static final int DIM = 7 << 4 | 7 << 20;

    public static class State extends BlockEntityRenderState {
        float time;
        float[] angles = new float[0];
        int mask;
        ArenaSealBlock.@Nullable Phase phase;
    }

    public ArenaSealRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ArenaSealBlockEntity seal, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(seal, state, partialTicks, cameraPosition, breakProgress);
        BlockPos pos = seal.getBlockPos();
        List<BlockPos> cages = seal.cages();
        state.angles = new float[cages.size()];
        for (int i = 0; i < cages.size(); i++) {
            BlockPos c = cages.get(i);
            state.angles[i] = (float) Math.atan2(c.getZ() - pos.getZ(), c.getX() - pos.getX());
        }
        state.mask = seal.conqueredMask();
        state.phase = seal.phase();
        state.time = (seal.getLevel() == null ? 0L : seal.getLevel().getGameTime() % 240_000L) + partialTicks;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.phase == ArenaSealBlock.Phase.AWAKE || state.angles.length == 0) {
            return;
        }
        boolean ready = state.phase == ArenaSealBlock.Phase.READY;
        for (int i = 0; i < state.angles.length; i++) {
            boolean lit = ready || (state.mask & (1 << i)) != 0;
            float angle = state.angles[i] + (ready ? state.time * 0.06f : 0.0f);
            float bob = Mth.sin(state.time * 0.08f + i * 1.7f) * 0.06f;
            float pulse = ready ? 1.0f + Mth.sin(state.time * 0.25f + i) * 0.08f : 1.0f;
            pose.pushPose();
            pose.translate(0.5f, HEIGHT + bob, 0.5f);
            // die Fläche (in x-y, Normale +z) zum Käfig drehen: +z zeigt dann in Richtung (cos θ, sin θ)
            pose.rotate(Axis.YP.rotation(Mth.HALF_PI - angle));
            pose.translate(0.0f, 0.0f, RADIUS);
            pose.rotate(Axis.XP.rotation(-TILT));
            pose.rotate(Axis.ZP.rotation(state.time * (lit ? 0.04f : 0.01f) + i));
            pose.scale(pulse, pulse, pulse);
            int light = lit ? 0xF000F0 : DIM;
            float u0 = lit ? 0.5f : 0.0f;
            collector.submitCustomGeometry(pose, TYPE, (p, buffer) -> {
                float h = SIZE / 2.0f;
                vertex(buffer, p, -h, -h, u0 + 0.5f, 1.0f, light);
                vertex(buffer, p, h, -h, u0, 1.0f, light);
                vertex(buffer, p, h, h, u0, 0.0f, light);
                vertex(buffer, p, -h, h, u0 + 0.5f, 0.0f, light);
            });
            pose.popPose();
        }
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int light) {
        buffer.addVertex(pose, x, y, 0.0f).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public AABB getRenderBoundingBox(ArenaSealBlockEntity seal) {
        BlockPos pos = seal.getBlockPos();
        return new AABB(pos.getX() - 1.5, pos.getY() + 0.5, pos.getZ() - 1.5, pos.getX() + 2.5, pos.getY() + HEIGHT + 1.0, pos.getZ() + 2.5);
    }
}
