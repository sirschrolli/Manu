package io.github.sirschrolli.kingdomsatwar.client.renderer.castle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.castle.RavenBannerBlock;
import io.github.sirschrolli.kingdomsatwar.block.castle.RavenBannerBlockEntity;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Das Rabenbanner über dem Pfahl (seit 1.8): das obere Stück des Pfahls mit einem Eisenband und dem goldenen Knauf, das
 * Querholz mit goldenen Spitzen und eisernem Beschlag – und daran das Banner derer von Rabenfels, das im Wind weht: Wellen
 * laufen von oben nach unten durch das Tuch, die Zipfel schwingen. Ist der Raubritter besiegt, hängen nur noch Fetzen da.
 *
 * <p>Gezeichnet in Modelleinheiten (1/16 Block) über der Mitte des Blocks, vorn (das Bild) nach Norden; gedreht wie der Block.
 */
public class RavenBannerRenderer implements BlockEntityRenderer<RavenBannerBlockEntity, RavenBannerRenderer.State> {
    static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/raven_banner/raven_banner.png");
    private static final RenderType TYPE = RenderTypes.entityCutout(TEXTURE);
    private static final float TEX = 128.0f;
    /** Das Tuch: 24 x 36 Einheiten, oben unter dem Querholz, ein Stück vor dem Pfahl. */
    private static final float CLOTH_W = 24.0f;
    private static final float CLOTH_H = 36.0f;
    private static final float CLOTH_TOP = 44.0f;
    private static final float CLOTH_Z = -2.7f;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 12;

    public static class State extends BlockEntityRenderState {
        float yaw;
        float time;
        boolean torn;
    }

    public RavenBannerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(RavenBannerBlockEntity banner, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(banner, state, partialTicks, cameraPosition, breakProgress);
        var blockState = banner.getBlockState();
        Direction facing = blockState.hasProperty(RavenBannerBlock.FACING) ? blockState.getValue(RavenBannerBlock.FACING) : Direction.SOUTH;
        state.yaw = facing.toYRot();
        state.torn = blockState.hasProperty(RavenBannerBlock.PHASE) && blockState.getValue(RavenBannerBlock.PHASE) == RavenBannerBlock.Phase.FALLEN;
        BlockPos pos = banner.getBlockPos();
        long seed = pos.asLong() * 0x9E3779B97F4A7C15L >>> 52;
        state.time = (banner.getLevel() == null ? 0L : banner.getLevel().getGameTime() % 240_000L) + partialTicks + seed;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.5f, 0.0f, 0.5f);
        pose.rotate(Axis.YP.rotationDegrees(180.0f - state.yaw));
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        int light = state.lightCoords;
        float time = state.time;
        boolean torn = state.torn;
        collector.submitCustomGeometry(pose, TYPE, (p, buffer) -> {
            // das obere Stück des Pfahls, ein Eisenband am Übergang, der Knauf (Eiche, Eisen und Gold: je 2 Pixel je Einheit)
            box(buffer, p, light, -1.0f, 31.0f, -1.0f, 1.0f, 47.0f, 1.0f, 96, 0, 100, 32);
            box(buffer, p, light, -1.25f, 31.0f, -1.25f, 1.25f, 33.0f, 1.25f, 112, 18, 117, 22);
            box(buffer, p, light, -1.6f, 47.0f, -1.6f, 1.6f, 49.4f, 1.6f, 113, 1, 119, 6);
            box(buffer, p, light, -0.8f, 49.4f, -0.8f, 0.8f, 50.6f, 0.8f, 114, 2, 117, 4);
            // das Querholz vor dem Pfahl, mit eisernem Beschlag und goldenen Spitzen
            box(buffer, p, light, -13.0f, 44.0f, -3.4f, 13.0f, 45.6f, -1.8f, 100, 0, 103, 52);
            box(buffer, p, light, -1.6f, 43.6f, -3.7f, 1.6f, 46.0f, 1.2f, 113, 17, 123, 22);
            box(buffer, p, light, -14.2f, 43.7f, -3.7f, -13.0f, 45.9f, -1.5f, 113, 1, 117, 5);
            box(buffer, p, light, 13.0f, 43.7f, -3.7f, 14.2f, 45.9f, -1.5f, 113, 1, 117, 5);
            cloth(buffer, p, light, time, torn);
        });
        pose.popPose();
    }

    /**
     * Das Tuch: ein Gitter, dessen Punkte nach vorn wehen (nie nach hinten, durch den Pfahl): oben fest am Querholz, nach unten
     * immer weiter; Wellen laufen hinab, die Zipfel schwingen ein wenig zur Seite. Vorn und hinten je eine Fläche (von hinten
     * erscheint das Bild gespiegelt, wie durch das Tuch hindurch).
     */
    private static void cloth(VertexConsumer buffer, PoseStack.Pose pose, int light, float time, boolean torn) {
        float[][] px = new float[GRID_X + 1][GRID_Y + 1];
        float[][] py = new float[GRID_X + 1][GRID_Y + 1];
        float[][] pz = new float[GRID_X + 1][GRID_Y + 1];
        for (int i = 0; i <= GRID_X; i++) {
            for (int j = 0; j <= GRID_Y; j++) {
                float s = (float) j / GRID_Y;
                float x = -CLOTH_W / 2.0f + CLOTH_W * i / GRID_X;
                float amp = 1.5f * (float) Math.pow(s, 1.2);
                float phase = time * 0.085f - s * 3.4f + x * 0.11f;
                float wave = 0.55f + 0.3f * Mth.sin(phase) + 0.15f * Mth.sin(phase * 1.9f + x * 0.23f);
                px[i][j] = x + 0.6f * s * s * Mth.sin(time * 0.037f + 0.7f);
                py[i][j] = CLOTH_TOP - CLOTH_H * s;
                pz[i][j] = CLOTH_Z - amp * wave;
            }
        }
        float u0 = (torn ? 48.0f : 0.0f) / TEX;
        float du = 48.0f / TEX / GRID_X;
        float dv = 72.0f / TEX / GRID_Y;
        for (int i = 0; i < GRID_X; i++) {
            for (int j = 0; j < GRID_Y; j++) {
                // die Normale der Zelle (aus ihren Kanten), damit die Wellen Licht und Schatten werfen
                float ax = px[i + 1][j] - px[i][j];
                float ay = py[i + 1][j] - py[i][j];
                float az = pz[i + 1][j] - pz[i][j];
                float bx = px[i][j + 1] - px[i][j];
                float by = py[i][j + 1] - py[i][j];
                float bz = pz[i][j + 1] - pz[i][j];
                float nx = ay * bz - az * by;
                float ny = az * bx - ax * bz;
                float nz = ax * by - ay * bx;
                float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
                if (len < 1.0e-5f) {
                    nx = 0.0f;
                    ny = 0.0f;
                    nz = -1.0f;
                } else {
                    nx /= len;
                    ny /= len;
                    nz /= len;
                }
                // (das Kreuzprodukt der Kanten nach rechts und nach unten zeigt nach vorn, nach -z)
                float ua = u0 + du * i;
                float ub = u0 + du * (i + 1);
                float va = dv * j;
                float vb = dv * (j + 1);
                // vorn (nach -z): von vorn gesehen gegen den Uhrzeigersinn
                vertex(buffer, pose, px[i + 1][j], py[i + 1][j], pz[i + 1][j], ub, va, light, nx, ny, nz);
                vertex(buffer, pose, px[i + 1][j + 1], py[i + 1][j + 1], pz[i + 1][j + 1], ub, vb, light, nx, ny, nz);
                vertex(buffer, pose, px[i][j + 1], py[i][j + 1], pz[i][j + 1], ua, vb, light, nx, ny, nz);
                vertex(buffer, pose, px[i][j], py[i][j], pz[i][j], ua, va, light, nx, ny, nz);
                // hinten
                vertex(buffer, pose, px[i][j], py[i][j], pz[i][j], ua, va, light, -nx, -ny, -nz);
                vertex(buffer, pose, px[i][j + 1], py[i][j + 1], pz[i][j + 1], ua, vb, light, -nx, -ny, -nz);
                vertex(buffer, pose, px[i + 1][j + 1], py[i + 1][j + 1], pz[i + 1][j + 1], ub, vb, light, -nx, -ny, -nz);
                vertex(buffer, pose, px[i + 1][j], py[i + 1][j], pz[i + 1][j], ub, va, light, -nx, -ny, -nz);
            }
        }
    }

    /** Ein Quader (Einheiten), alle Seiten mit demselben Ausschnitt der Textur (Pixel u0, v0 bis u1, v1). */
    private static void box(VertexConsumer buffer, PoseStack.Pose pose, int light, float x0, float y0, float z0, float x1, float y1, float z1,
            int pu0, int pv0, int pu1, int pv1) {
        float u0 = pu0 / TEX;
        float v0 = pv0 / TEX;
        float u1 = pu1 / TEX;
        float v1 = pv1 / TEX;
        // die Maserung läuft der Länge nach: bei liegenden Quadern (Querholz) quer über die Textur
        boolean lying = x1 - x0 > y1 - y0;
        // vorn (-z) und hinten (+z)
        face(buffer, pose, light, lying, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0, u0, v0, u1, v1, 0, 0, -1);
        face(buffer, pose, light, lying, x1, y0, z1, x1, y1, z1, x0, y1, z1, x0, y0, z1, u0, v0, u1, v1, 0, 0, 1);
        // links (-x) und rechts (+x)
        face(buffer, pose, light, lying, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0, u0, v0, u1, v1, -1, 0, 0);
        face(buffer, pose, light, lying, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1, u0, v0, u1, v1, 1, 0, 0);
        // oben und unten
        face(buffer, pose, light, lying, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, u0, v0, u1, v1, 0, 1, 0);
        face(buffer, pose, light, lying, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, u0, v0, u1, v1, 0, -1, 0);
    }

    /** Eine Fläche aus vier Punkten (a unten links, b oben links, c oben rechts, d unten rechts, von außen gesehen). */
    private static void face(VertexConsumer buffer, PoseStack.Pose pose, int light, boolean lying, float ax, float ay, float az, float bx, float by,
            float bz, float cx, float cy, float cz, float dx, float dy, float dz, float u0, float v0, float u1, float v1, float nx, float ny,
            float nz) {
        if (lying) {
            // quer: die Länge (a → d) entlang v
            vertex(buffer, pose, ax, ay, az, u1, v0, light, nx, ny, nz);
            vertex(buffer, pose, bx, by, bz, u0, v0, light, nx, ny, nz);
            vertex(buffer, pose, cx, cy, cz, u0, v1, light, nx, ny, nz);
            vertex(buffer, pose, dx, dy, dz, u1, v1, light, nx, ny, nz);
        } else {
            vertex(buffer, pose, ax, ay, az, u0, v1, light, nx, ny, nz);
            vertex(buffer, pose, bx, by, bz, u0, v0, light, nx, ny, nz);
            vertex(buffer, pose, cx, cy, cz, u1, v0, light, nx, ny, nz);
            vertex(buffer, pose, dx, dy, dz, u1, v1, light, nx, ny, nz);
        }
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light, float nx,
            float ny, float nz) {
        buffer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }

    @Override
    public AABB getRenderBoundingBox(RavenBannerBlockEntity banner) {
        BlockPos pos = banner.getBlockPos();
        return new AABB(pos.getX() - 0.6, pos.getY(), pos.getZ() - 0.6, pos.getX() + 1.6, pos.getY() + 3.3, pos.getZ() + 1.6);
    }
}
