package io.github.sirschrolli.kingdomsatwar.client.renderer.sea;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Ein weicher, leuchtender Hof (immer zur Kamera gedreht) – um Köder und Quallen in der dunklen Tiefe. */
final class SeaGlow {
    private static final Identifier HALO = KingdomsAtWar.id("textures/entity/mage/missile.png");
    private static final int FULL_BRIGHT = 0xF000F0;

    private SeaGlow() {}

    /**
     * Zeichnet einen Hof der Größe {@code size} (Blöcke) an {@code at} (relativ zum Wesen) in der Farbe {@code rgb} mit der
     * Stärke {@code strength} (0..1).
     */
    static void halo(PoseStack poseStack, SubmitNodeCollector collector, Quaternionf camera, Vector3f at, float size, int rgb, float strength) {
        if (strength <= 0.01f) {
            return;
        }
        RenderType type = RenderTypes.energySwirl(HALO, 0.0f, 0.0f);
        float s = Math.min(1.0f, strength);
        int color = ARGB.color(255, Math.round(ARGB.red(rgb) * s), Math.round(ARGB.green(rgb) * s), Math.round(ARGB.blue(rgb) * s));
        poseStack.pushPose();
        poseStack.translate(at.x, at.y, at.z);
        poseStack.rotate(camera);
        poseStack.scale(size, size, size);
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> billboard(pose, buffer, color));
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int color) {
        buffer.addVertex(pose, x, y, 0.0f).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    private static void billboard(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        vertex(buffer, pose, -0.5f, -0.5f, 0.0f, 1.0f, color);
        vertex(buffer, pose, 0.5f, -0.5f, 1.0f, 1.0f, color);
        vertex(buffer, pose, 0.5f, 0.5f, 1.0f, 0.0f, color);
        vertex(buffer, pose, -0.5f, 0.5f, 0.0f, 0.0f, color);
    }
}
