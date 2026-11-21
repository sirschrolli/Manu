package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import java.util.Objects;
import net.minecraft.client.model.monster.shulker.ShulkerModel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ShulkerRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Schreinling: eigenes Modell auf dem Shulker-Gerüst (Deckel, Sockel, Kopf; Öffnen, Spähen und Anheften wie bei Vanilla),
 * je Farbe ein eigenes Bild; Runen, Seelenstein, Auge und Fühler leuchten.
 */
public class SchreinlingRenderer extends MobRenderer<Shulker, ShulkerRenderState, ShulkerModel> {
    private static final Identifier DEFAULT = EndRenderers.texture("shulker", "schreinling");
    private static final RenderType DEFAULT_GLOW = RenderTypes.eyes(EndRenderers.texture("shulker", "schreinling_glow"));
    private static final Identifier[] TEXTURES = new Identifier[DyeColor.values().length];
    private static final RenderType[] GLOWS = new RenderType[DyeColor.values().length];

    static {
        for (DyeColor color : DyeColor.values()) {
            TEXTURES[color.getId()] = EndRenderers.texture("shulker", "schreinling_" + color.getName());
            GLOWS[color.getId()] = RenderTypes.eyes(EndRenderers.texture("shulker", "schreinling_" + color.getName() + "_glow"));
        }
    }

    public SchreinlingRenderer(EntityRendererProvider.Context context) {
        super(context, new ShulkerModel(context.bakeLayer(EndRenderers.SCHREINLING)), 0.0f);
        this.addLayer(new GlowLayer<>(this, state -> state.color == null ? DEFAULT_GLOW : GLOWS[state.color.getId()]));
    }

    @Override
    public Vec3 getRenderOffset(ShulkerRenderState state) {
        return state.renderOffset;
    }

    @Override
    public boolean shouldRender(Shulker entity, Frustum culler, double camX, double camY, double camZ, float partialTicks) {
        if (super.shouldRender(entity, culler, camX, camY, camZ, partialTicks)) {
            return true;
        }
        Vec3 start = entity.getRenderPosition(0.0f);
        if (start == null) {
            return false;
        }
        EntityType<?> type = entity.getType();
        float halfHeight = type.getHeight() / 2.0f;
        float halfWidth = type.getWidth() / 2.0f;
        Vec3 target = Vec3.atBottomCenterOf(entity.blockPosition());
        return culler.isVisible(new AABB(start.x, start.y + halfHeight, start.z, target.x, target.y + halfHeight, target.z)
                .inflate(halfWidth, halfHeight, halfWidth));
    }

    @Override
    public Identifier getTextureLocation(ShulkerRenderState state) {
        return texture(state.color);
    }

    private static Identifier texture(@Nullable DyeColor color) {
        return color == null ? DEFAULT : TEXTURES[color.getId()];
    }

    @Override
    public ShulkerRenderState createRenderState() {
        return new ShulkerRenderState();
    }

    @Override
    public void extractRenderState(Shulker entity, ShulkerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.renderOffset = Objects.requireNonNullElse(entity.getRenderPosition(partialTicks), Vec3.ZERO);
        state.color = entity.getColor();
        state.peekAmount = entity.getClientPeekAmount(partialTicks);
        state.yHeadRot = entity.yHeadRot;
        state.yBodyRot = entity.yBodyRot;
        state.attachFace = entity.getAttachFace();
    }

    @Override
    protected void setupRotations(ShulkerRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot + 180.0f, entityScale);
        poseStack.rotateAround(state.attachFace.getOpposite().getRotation(), 0.0f, 0.5f, 0.0f);
    }
}
