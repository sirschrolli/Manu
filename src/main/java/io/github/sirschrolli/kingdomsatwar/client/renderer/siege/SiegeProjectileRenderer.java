package io.github.sirschrolli.kingdomsatwar.client.renderer.siege;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.siege.SiegeModels;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeProjectile;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Geschosse der Belagerungswaffen: die Steinkugel des Katapults und der schwerere Brocken des Triboks überschlagen sich im
 * Flug, der Ballistenbolzen zeigt in Flugrichtung und bleibt so stecken, wie er eingeschlagen ist.
 */
public class SiegeProjectileRenderer extends EntityRenderer<SiegeProjectile, SiegeProjectileRenderer.State> {
    private static final Identifier STONE = KingdomsAtWar.id("textures/entity/siege/siege_stone/siege_stone.png");
    private static final Identifier BOLT = KingdomsAtWar.id("textures/entity/siege/ballista_bolt/ballista_bolt.png");
    /** Das Steinmodell misst zehn Einheiten; im Löffel des Katapults liegen sechs, in der Schlinge des Triboks 7,6. */
    private static final float CATAPULT_STONE = 0.6f;
    private static final float TREBUCHET_STONE = 0.4f * SiegeModels.TREBUCHET_STONE_SCALE;

    public static class State extends EntityRenderState {
        public SiegeType siege = SiegeType.CATAPULT;
        public float yRot;
        public float xRot;
        public float age;
        public boolean stuck;
        /** Jede Kugel dreht sich etwas anders. */
        public float spin;
    }

    private final SiegeModels.Stone stone;
    private final SiegeModels.Bolt bolt;

    public SiegeProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.stone = new SiegeModels.Stone(context.bakeLayer(ModModelLayers.SIEGE_STONE));
        this.bolt = new SiegeModels.Bolt(context.bakeLayer(ModModelLayers.BALLISTA_BOLT));
        this.shadowRadius = 0.3f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SiegeProjectile entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.siege = entity.siege();
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.age = entity.tickCount + partialTicks;
        state.stuck = entity.stuck();
        state.spin = (entity.getId() * 37 % 360);
    }

    @Override
    public void submit(State s, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        // die Mitte des Geschosses (der Abschusspunkt) liegt in der Mitte seines Kastens
        poseStack.translate(0.0f, 0.3f, 0.0f);
        if (s.siege == SiegeType.BALLISTA) {
            poseStack.rotate(Axis.YP.rotationDegrees(s.yRot + 180.0f));
            poseStack.rotate(Axis.XP.rotationDegrees(s.xRot));
            // im Flug dreht sich der Bolzen langsam um sich selbst (die Federn)
            if (!s.stuck) {
                poseStack.rotate(Axis.ZP.rotationDegrees(s.spin + s.age * 9.0f));
            }
            poseStack.scale(-1.0f, -1.0f, 1.0f);
            collector.submitModel(this.bolt, s, poseStack, this.bolt.renderType(BOLT), s.lightCoords, OverlayTexture.NO_OVERLAY, -1, null,
                    s.outlineColor);
        } else {
            float size = s.siege == SiegeType.TREBUCHET ? TREBUCHET_STONE : CATAPULT_STONE;
            // die Kugel überschlagt sich nach vorn
            poseStack.rotate(Axis.YP.rotationDegrees(s.yRot + 180.0f));
            poseStack.rotate(Axis.XP.rotationDegrees(s.spin - s.age * 23.0f));
            poseStack.rotate(Axis.ZP.rotationDegrees(s.spin * 0.5f));
            poseStack.scale(-size, -size, size);
            collector.submitModel(this.stone, s, poseStack, this.stone.renderType(STONE), s.lightCoords, OverlayTexture.NO_OVERLAY, -1, null,
                    s.outlineColor);
        }
        poseStack.popPose();
        super.submit(s, poseStack, collector, camera);
    }
}
