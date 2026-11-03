package io.github.sirschrolli.kingdomsatwar.client.model.shadow;

import io.github.sirschrolli.kingdomsatwar.client.renderer.shadow.ShadowCreatureRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Nachtschwinge: weite, schwere Flügelschläge (Spitzen schwingen nach), pendelnder Schwanz, bewegliche Ohren. */
public class NightWingModel extends EntityModel<ShadowCreatureRenderState> {
    private final ModelPart leftWing;
    private final ModelPart leftTip;
    private final ModelPart rightWing;
    private final ModelPart rightTip;
    private final ModelPart tail;
    private final ModelPart tailTip;
    private final ModelPart head;
    private final ModelPart earL;
    private final ModelPart earR;

    public NightWingModel(ModelPart root) {
        super(root);
        ModelPart body = root.getChild("body");
        this.leftWing = body.getChild("left_wing_base");
        this.leftTip = this.leftWing.getChild("left_wing_tip");
        this.rightWing = body.getChild("right_wing_base");
        this.rightTip = this.rightWing.getChild("right_wing_tip");
        this.tail = body.getChild("tail_base");
        this.tailTip = this.tail.getChild("tail_tip");
        this.head = body.getChild("head");
        this.earL = this.head.getChild("ear_l");
        this.earR = this.head.getChild("ear_r");
    }

    @Override
    public void setupAnim(ShadowCreatureRenderState state) {
        super.setupAnim(state);
        float a = state.flapTime * 7.448451f * Mth.DEG_TO_RAD;
        float flap = Mth.cos(a);
        float lag = Mth.cos(a - 0.7f);
        this.leftWing.zRot = flap * 22.0f * Mth.DEG_TO_RAD;
        this.leftTip.zRot = lag * 24.0f * Mth.DEG_TO_RAD;
        this.rightWing.zRot = -this.leftWing.zRot;
        this.rightTip.zRot = -this.leftTip.zRot;
        this.tail.xRot = -(4.0f + Mth.cos(a * 2.0f) * 6.0f) * Mth.DEG_TO_RAD;
        this.tailTip.xRot = -(4.0f + Mth.cos(a * 2.0f - 0.8f) * 8.0f) * Mth.DEG_TO_RAD;
        this.head.xRot += Mth.sin(a * 2.0f) * 0.04f;
        this.earL.zRot += flap * 0.12f;
        this.earR.zRot -= flap * 0.12f;
    }
}
