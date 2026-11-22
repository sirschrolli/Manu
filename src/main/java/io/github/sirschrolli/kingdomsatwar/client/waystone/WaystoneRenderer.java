package io.github.sirschrolli.kingdomsatwar.client.waystone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.waystone.WaystoneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.waystone.WaystoneCompass;
import io.github.sirschrolli.kingdomsatwar.waystone.WaystoneCompassItem;
import io.github.sirschrolli.kingdomsatwar.waystone.Waypoints;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * Über dem Wegstein schwebt ein türkiser Kristall (seit 1.12): ein Achtflächner, der sich langsam dreht, auf und ab
 * schwingt und sanft pulsiert. Ist der Wegstein das Ziel des Wegkompasses in der Hand, dreht er sich schneller und
 * leuchtet heller. Steht ein Block auf dem Wegstein, sinkt der Kristall in seine Fassung.
 */
public class WaystoneRenderer implements BlockEntityRenderer<WaystoneBlockEntity, WaystoneRenderer.State> {
    private static final Identifier CRYSTAL = KingdomsAtWar.id("textures/block/waystone_crystal.png");
    private static final RenderType CRYSTAL_TYPE = RenderTypes.entityTranslucentEmissive(CRYSTAL);
    private static final int FULL_BRIGHT = 0xF000F0;

    public static class State extends BlockEntityRenderState {
        float time;
        boolean blocked;
        /** 0..1: wie sehr er gerade das Ziel des Kompasses in der Hand ist (blendet weich über). */
        float targeted;
        /** Der Name über dem Stein – nur, wenn man ihn gerade ansieht. */
        @Nullable Component name;
    }

    public WaystoneRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WaystoneBlockEntity stone, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(stone, state, partialTicks, cameraPosition, breakProgress);
        Level level = stone.getLevel();
        BlockPos pos = stone.getBlockPos();
        long seed = pos.asLong();
        state.time = level == null ? 0.0f : (float) (level.getGameTime() % 72000L) + partialTicks + (seed & 255L);
        state.blocked = level != null && !level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
        boolean targeted = level != null && isTarget(GlobalPos.of(level.dimension(), pos));
        stone.glow = Mth.approach(stone.glow, targeted ? 1.0f : 0.0f, 0.02f);
        state.targeted = stone.glow;
        HitResult hit = Minecraft.getInstance().hitResult;
        boolean looking = hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK && block.getBlockPos().equals(pos);
        state.name = looking ? stone.getName().copy().withStyle(ChatFormatting.AQUA) : null;
    }

    /** Zeigt der Wegkompass in einer Hand des eigenen Spielers gerade hierher? */
    private static boolean isTarget(GlobalPos pos) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof WaystoneCompassItem) {
                Waypoints.Waypoint target = WaystoneCompass.of(stack).target();
                if (target != null && target.pos().equals(pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float t = state.time;
        float lit = state.targeted;
        float height = state.blocked ? 0.985f : 1.17f + Mth.sin(t * 0.055f) * 0.045f;
        float size = state.blocked ? 0.085f : 0.12f;
        float pulse = 0.5f + 0.5f * Mth.sin(t * 0.11f);

        // Kristall: Achtflächner, oben spitzer als unten
        pose.pushPose();
        pose.translate(0.5f, height, 0.5f);
        pose.rotateDegrees(Axis.YP, t * (1.3f + 3.2f * lit));
        pose.rotateDegrees(Axis.XP, Mth.sin(t * 0.03f) * 6.0f);
        pose.scale(size, size, size);
        int brightness = Math.round(200 + 40 * pulse * (0.4f + lit) + 15 * lit);
        int color = ARGB.color(Math.min(255, brightness), ARGB.srgbLerp(lit * 0.35f, 0xFFFFFFFF, 0xFFFFF4C8) & 0xFFFFFF);
        collector.submitCustomGeometry(pose, CRYSTAL_TYPE, (p, buffer) -> octahedron(p, buffer, color));
        pose.popPose();

        // der Name, wenn man den Stein ansieht
        if (state.name != null) {
            collector.submitNameTag(pose, new Vec3(0.5, height - 0.05 + (state.blocked ? 0.9 : 0.0), 0.5), 0, state.name, false, FULL_BRIGHT, camera);
        }
    }

    private static void octahedron(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        float top = 1.75f;
        float bottom = -1.25f;
        Vector3f[] ring = {new Vector3f(1, 0, 0), new Vector3f(0, 0, 1), new Vector3f(-1, 0, 0), new Vector3f(0, 0, -1)};
        Vector3f up = new Vector3f(0, top, 0);
        Vector3f down = new Vector3f(0, bottom, 0);
        for (int i = 0; i < 4; i++) {
            Vector3f a = ring[i];
            Vector3f b = ring[(i + 1) % 4];
            triangle(pose, buffer, up, b, a, color, 0.0f);
            triangle(pose, buffer, down, a, b, color, 0.45f);
        }
    }

    /** Ein Dreieck als Viereck mit doppelter Ecke; {@code v0}: wie weit unten im Bild die Spitze liegt (unten dunkler). */
    private static void triangle(PoseStack.Pose pose, VertexConsumer buffer, Vector3f apex, Vector3f a, Vector3f b, int color, float v0) {
        Vector3f normal = new Vector3f(a).sub(apex).cross(new Vector3f(b).sub(apex)).normalize();
        vertex(buffer, pose, apex, 0.5f, v0, color, normal);
        vertex(buffer, pose, a, 0.0f, 1.0f, color, normal);
        vertex(buffer, pose, b, 1.0f, 1.0f, color, normal);
        vertex(buffer, pose, apex, 0.5f, v0, color, normal);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, Vector3f p, float u, float v, int color, Vector3f normal) {
        buffer.addVertex(pose, p.x(), p.y(), p.z()).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT)
                .setNormal(pose, normal.x(), normal.y(), normal.z());
    }

    @Override
    public AABB getRenderBoundingBox(WaystoneBlockEntity stone) {
        BlockPos pos = stone.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 2.4, pos.getZ() + 1.0);
    }
}
