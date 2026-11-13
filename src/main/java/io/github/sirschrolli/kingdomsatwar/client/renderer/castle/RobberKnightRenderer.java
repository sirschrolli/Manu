package io.github.sirschrolli.kingdomsatwar.client.renderer.castle;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.entity.castle.RobberKnightEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/**
 * Kuno von Rabenfels, der Raubritter, mit seinem Rappen – 1,1-fach vergrößert (zu Ross mit dem Raben auf dem Helm gut
 * dreieinhalb Blöcke hoch). Er kippt im Tod nicht zur Seite: Er sinkt in die Knie (siehe {@link RobberKnightModel}).
 */
public class RobberKnightRenderer extends MobRenderer<RobberKnightEntity, RobberKnightRenderState, RobberKnightModel> {
    public static final float SCALE = 1.1f;
    private static final Identifier TEXTURE = CastleRenderers.texture("robber_knight", "robber_knight");

    public RobberKnightRenderer(EntityRendererProvider.Context context) {
        super(context, new RobberKnightModel(context.bakeLayer(CastleRenderers.ROBBER_KNIGHT)), 1.1f);
    }

    @Override
    public Identifier getTextureLocation(RobberKnightRenderState state) {
        return TEXTURE;
    }

    @Override
    public RobberKnightRenderState createRenderState() {
        return new RobberKnightRenderState();
    }

    @Override
    public void extractRenderState(RobberKnightEntity entity, RobberKnightRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack = entity.attackOrdinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.mounted = entity.isMounted();
        state.lance = entity.hasLance();
        double dx = entity.getX() - entity.xo;
        double dz = entity.getZ() - entity.zo;
        state.drift = (float) Math.sqrt(dx * dx + dz * dz);
        state.owner = entity;
        this.shadowRadius = entity.isMounted() ? 1.1f : 0.6f;
        if (entity.deathTime > 0) {
            state.hasRedOverlay = false;
        }
    }

    @Override
    protected void scale(RobberKnightRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    /** Er kippt im Tod nicht zur Seite, er sinkt in die Knie. */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }
}
