package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.renderer.FlightAim;
import io.github.sirschrolli.kingdomsatwar.entity.monster.MonsterShot;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

/**
 * Geschosse der Monster (seit 1.12.1): Der Schlamm des Moorschlurfers fliegt als nasser, klumpiger Batzen mit Kieseln und
 * Gras darin, der nachtropft und sich im Flug langsam um sich selbst dreht (vorher ein glatter Schlammblock aus Vanilla). Magmabatzen
 * und Chorusfrucht bleiben Bilder wie bei Vanilla; das Seelenfeuer ist nur Partikel.
 */
public class MonsterShotRenderer extends ThrownItemRenderer<MonsterShot> {
    private static final RenderType MUD = RenderTypes.entityCutout(KingdomsAtWar.id("textures/entity/mire_shambler/mud_ball.png"));

    public static class State extends ThrownItemRenderState {
        public boolean mud;
        public float yRot;
        public float xRot;
        public float age;
    }

    private final float[] aim = new float[2];
    private final ModelPart ball;

    public MonsterShotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.ball = context.bakeLayer(ModModelLayers.MUD_BALL).getChild("ball");
    }

    @Override
    public ThrownItemRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MonsterShot entity, ThrownItemRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof State shot) {
            shot.mud = entity.kind() == MonsterShot.Kind.MUD;
            if (shot.mud) {
                // statt des Schlammblocks der eigene Batzen
                state.item.clear();
                FlightAim.aim(entity, partialTicks, false, false, this.aim);
                shot.yRot = this.aim[0];
                shot.xRot = this.aim[1];
                shot.age = entity.tickCount + partialTicks;
            }
        }
    }

    @Override
    public void submit(ThrownItemRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state instanceof State shot && shot.mud) {
            pose.pushPose();
            pose.translate(0.0f, 0.12f, 0.0f);
            pose.rotate(Axis.YP.rotationDegrees(shot.yRot + 180.0f));
            pose.rotate(Axis.XP.rotationDegrees(shot.xRot));
            // der Batzen dreht sich langsam um die Flugachse und wabert (die Tropfen bleiben hinten)
            pose.rotate(Axis.ZP.rotationDegrees(shot.age * 9.0f));
            float wobble = 1.0f + Mth.sin(shot.age * 0.8f) * 0.06f;
            pose.scale(-0.9f * wobble, -0.9f / wobble, 0.9f);
            this.ball.resetPose();
            collector.submitModelPart(this.ball, pose, MUD, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
            pose.popPose();
        }
        super.submit(state, pose, collector, camera);
    }
}
