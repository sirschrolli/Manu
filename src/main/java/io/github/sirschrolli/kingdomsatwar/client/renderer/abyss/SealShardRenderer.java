package io.github.sirschrolli.kingdomsatwar.client.renderer.abyss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.abyss.SealShardBlock;
import io.github.sirschrolli.kingdomsatwar.block.abyss.SealShardBlockEntity;
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
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

/**
 * Die Kronensplitter am Schlund der Welten: über jeder Druse schwebt ein großer, sechskantiger Kristall, der sich langsam
 * dreht und auf und ab wiegt, in einem violetten Schein. Von ihm führt eine Kette aus Licht zum Siegelstern, der hoch über
 * der Mitte des Schlunds hängt – wo alle sieben Ketten zusammenlaufen, glüht er hell.
 */
public class SealShardRenderer implements BlockEntityRenderer<SealShardBlockEntity, SealShardRenderer.State> {
    private static final Identifier CRYSTAL = KingdomsAtWar.id("textures/entity/seal_crystal.png");
    private static final RenderType CRYSTAL_TYPE = RenderTypes.entityCutout(CRYSTAL);
    private static final Identifier BEAM = KingdomsAtWar.id("textures/entity/spell/beam.png");
    private static final Identifier HALO = KingdomsAtWar.id("textures/entity/soft_glow.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final int PALE = 0xF2E6FF;
    /** Höhe des Siegelsterns über der Mitte des Rings. */
    public static final double STAR_HEIGHT = 5.5;
    /** Höhe des schwebenden Kristalls über der Druse. */
    private static final float FLOAT = 2.2f;
    private static final float RADIUS = 0.42f;
    private static final float TOP = 1.15f;
    private static final float BOTTOM = 0.6f;
    private static final int SIDES = 6;

    public static class State extends BlockEntityRenderState {
        float time;
        float phase;
        boolean chained;
        Vec3 star = Vec3.ZERO;
    }

    public SealShardRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SealShardBlockEntity shard, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(shard, state, partialTicks, cameraPosition, breakProgress);
        BlockPos pos = shard.getBlockPos();
        state.time = (shard.getLevel() == null ? 0L : shard.getLevel().getGameTime() % 240_000L) + partialTicks;
        state.phase = (float) ((pos.asLong() * 0x9E3779B97F4A7C15L >>> 40) & 1023) / 1023.0f * Mth.TWO_PI;
        BlockPos home = shard.home();
        state.chained = home != null;
        if (home != null) {
            state.star = new Vec3(home.getX() + 0.5 - pos.getX(), home.getY() + STAR_HEIGHT - pos.getY(), home.getZ() + 0.5 - pos.getZ());
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float t = state.time;
        float bob = Mth.sin(t * 0.05f + state.phase) * 0.14f;
        Vec3 center = new Vec3(0.5, FLOAT + bob, 0.5);
        float pulse = 0.8f + 0.2f * Mth.sin(t * 0.09f + state.phase);

        // der Kristall: sechskantig, oben lang zugespitzt, dreht sich langsam
        pose.pushPose();
        pose.translate(center.x, center.y, center.z);
        pose.rotate(Axis.YP.rotationDegrees(t * 1.4f + state.phase * 57.0f));
        pose.rotate(Axis.ZP.rotationDegrees(Mth.sin(t * 0.03f + state.phase) * 6.0f));
        collector.submitCustomGeometry(pose, CRYSTAL_TYPE, (p, buffer) -> crystal(p, buffer));
        pose.popPose();

        // sein Schein
        RenderType halo = RenderTypes.energySwirl(HALO, 0.0f, 0.0f);
        int glow = scale(SealShardBlock.COLOR, 0.55f * pulse);
        pose.pushPose();
        pose.translate(center.x, center.y + 0.15, center.z);
        pose.rotate(camera.orientation);
        pose.scale(3.2f, 3.2f, 3.2f);
        collector.submitCustomGeometry(pose, halo, (p, buffer) -> billboard(p, buffer, glow));
        pose.popPose();

        if (!state.chained) {
            return;
        }
        // die Kette aus Licht zum Siegelstern
        Vec3 from = center.add(0.0, 0.3, 0.0);
        Vec3 span = state.star.subtract(from);
        double length = span.length();
        if (length > 0.5) {
            float len = (float) length;
            float repeat = len / 1.6f;
            RenderType swirl = RenderTypes.energySwirl(BEAM, 0.0f, -t * 0.05f);
            int chain = scale(SealShardBlock.COLOR, 0.75f * pulse);
            int core = scale(PALE, 0.6f * pulse);
            pose.pushPose();
            pose.translate(from.x, from.y, from.z);
            Vec3 d = span.scale(1.0 / length);
            pose.rotate(new Quaternionf().rotationTo(0.0f, 1.0f, 0.0f, (float) d.x, (float) d.y, (float) d.z));
            pose.rotate(Axis.YP.rotationDegrees(t * 3.0f + state.phase * 40.0f));
            collector.submitCustomGeometry(pose, swirl, (p, buffer) -> cross(p, buffer, len, 0.42f, repeat, chain));
            collector.submitCustomGeometry(pose, swirl, (p, buffer) -> cross(p, buffer, len, 0.14f, repeat, core));
            pose.popPose();
        }
        // der Siegelstern: alle sieben Splitter zeichnen ihn schwach – zusammen glüht er hell
        int star = scale(PALE, 0.22f + 0.06f * Mth.sin(t * 0.12f));
        pose.pushPose();
        pose.translate(state.star.x, state.star.y, state.star.z);
        pose.rotate(camera.orientation);
        pose.rotate(Axis.ZP.rotationDegrees(t * 0.8f));
        pose.scale(4.5f, 4.5f, 4.5f);
        collector.submitCustomGeometry(pose, halo, (p, buffer) -> billboard(p, buffer, star));
        pose.popPose();
    }

    private static int scale(int rgb, float f) {
        float k = Mth.clamp(f, 0.0f, 1.0f);
        return ARGB.color(255, Math.round(ARGB.red(rgb) * k), Math.round(ARGB.green(rgb) * k), Math.round(ARGB.blue(rgb) * k));
    }

    /** Ein sechskantiger Doppelkegel: oben lang, unten kurz; die Textur läuft einmal ringsum, oben hell, unten dunkel. */
    private static void crystal(PoseStack.Pose pose, VertexConsumer buffer) {
        for (int k = 0; k < SIDES; k++) {
            float a0 = k * Mth.TWO_PI / SIDES;
            float a1 = (k + 1) * Mth.TWO_PI / SIDES;
            float x0 = Mth.cos(a0) * RADIUS;
            float z0 = Mth.sin(a0) * RADIUS;
            float x1 = Mth.cos(a1) * RADIUS;
            float z1 = Mth.sin(a1) * RADIUS;
            float u0 = (float) k / SIDES;
            float u1 = (float) (k + 1) / SIDES;
            float um = (u0 + u1) * 0.5f;
            float nx = Mth.cos((a0 + a1) * 0.5f);
            float nz = Mth.sin((a0 + a1) * 0.5f);
            // oben
            vertex(buffer, pose, x0, 0.0f, z0, u0, 0.62f, nx, 0.4f, nz);
            vertex(buffer, pose, 0.0f, TOP, 0.0f, um, 0.0f, nx, 0.4f, nz);
            vertex(buffer, pose, 0.0f, TOP, 0.0f, um, 0.0f, nx, 0.4f, nz);
            vertex(buffer, pose, x1, 0.0f, z1, u1, 0.62f, nx, 0.4f, nz);
            // unten
            vertex(buffer, pose, x1, 0.0f, z1, u1, 0.62f, nx, -0.6f, nz);
            vertex(buffer, pose, 0.0f, -BOTTOM, 0.0f, um, 1.0f, nx, -0.6f, nz);
            vertex(buffer, pose, 0.0f, -BOTTOM, 0.0f, um, 1.0f, nx, -0.6f, nz);
            vertex(buffer, pose, x0, 0.0f, z0, u0, 0.62f, nx, -0.6f, nz);
        }
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, float nx, float ny,
            float nz) {
        buffer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT)
                .setNormal(pose, nx, ny, nz);
    }

    private static void beamVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    private static void cross(PoseStack.Pose pose, VertexConsumer buffer, float length, float width, float repeat, int color) {
        float w = width * 0.5f;
        beamVertex(buffer, pose, -w, 0.0f, 0.0f, 0.0f, repeat, color);
        beamVertex(buffer, pose, w, 0.0f, 0.0f, 1.0f, repeat, color);
        beamVertex(buffer, pose, w, length, 0.0f, 1.0f, 0.0f, color);
        beamVertex(buffer, pose, -w, length, 0.0f, 0.0f, 0.0f, color);
        beamVertex(buffer, pose, 0.0f, 0.0f, -w, 0.0f, repeat, color);
        beamVertex(buffer, pose, 0.0f, 0.0f, w, 1.0f, repeat, color);
        beamVertex(buffer, pose, 0.0f, length, w, 1.0f, 0.0f, color);
        beamVertex(buffer, pose, 0.0f, length, -w, 0.0f, 0.0f, color);
    }

    private static void billboard(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        beamVertex(buffer, pose, -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, color);
        beamVertex(buffer, pose, 0.5f, -0.5f, 0.0f, 1.0f, 1.0f, color);
        beamVertex(buffer, pose, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, color);
        beamVertex(buffer, pose, -0.5f, 0.5f, 0.0f, 0.0f, 0.0f, color);
    }

    @Override
    public AABB getRenderBoundingBox(SealShardBlockEntity shard) {
        BlockPos pos = shard.getBlockPos();
        AABB box = new AABB(pos).inflate(2.0).expandTowards(0.0, 3.0, 0.0);
        BlockPos home = shard.home();
        if (home != null) {
            box = box.minmax(new AABB(home.getX() - 3.0, home.getY() + STAR_HEIGHT - 3.0, home.getZ() - 3.0, home.getX() + 4.0,
                    home.getY() + STAR_HEIGHT + 4.0, home.getZ() + 4.0));
        }
        return box;
    }

    @Override
    public int getViewDistance() {
        return 160;
    }
}
