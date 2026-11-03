package io.github.sirschrolli.kingdomsatwar.client.model.farm;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.farm.FarmRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Hausschwein: trippelnde Beine, wippende Schlappohren, kringelnder Schwanz; Sattel nur gesattelt. */
public class FarmPigModel extends EntityModel<FarmRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart earL;
    private final ModelPart earR;
    private final ModelPart tail;
    private final ModelPart saddle;
    private final ModelPart frontLeft;
    private final ModelPart frontRight;
    private final ModelPart hindLeft;
    private final ModelPart hindRight;

    public FarmPigModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.earL = this.head.getChild("ear_l");
        this.earR = this.head.getChild("ear_r");
        this.tail = this.body.getChild("tail");
        this.saddle = this.body.getChild("saddle");
        this.frontLeft = root.getChild("front_left_leg");
        this.frontRight = root.getChild("front_right_leg");
        this.hindLeft = root.getChild("hind_left_leg");
        this.hindRight = root.getChild("hind_right_leg");
    }

    @Override
    public void setupAnim(FarmRenderState state) {
        super.setupAnim(state);
        this.saddle.visible = state.saddled && !state.isBaby;

        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        this.frontLeft.xRot += Anim.leg(pos, speed, 0.0f, 1.2f);
        this.frontRight.xRot += Anim.leg(pos, speed, Mth.PI, 1.2f);
        this.hindLeft.xRot += Anim.leg(pos, speed, Mth.PI, 1.2f);
        this.hindRight.xRot += Anim.leg(pos, speed, 0.0f, 1.2f);
        this.body.y -= Math.abs(Mth.sin(pos * 0.6662f)) * 0.4f * speed;

        this.head.yRot += Mth.clamp(state.yRot, -45.0f, 45.0f) * Anim.DEG * 0.8f;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * Anim.DEG * 0.6f;
        // Schlappohren wippen beim Laufen, schnuppernder Rüssel im Stand
        float flop = Mth.sin(pos * 0.6662f * 2.0f) * 0.25f * speed;
        this.earL.xRot += flop;
        this.earR.xRot -= flop;
        this.head.xRot += Mth.sin(t * 0.25f) * 0.02f * (1.0f - speed);
        this.tail.yRot += Mth.sin(t * 0.35f) * 0.35f;
        this.tail.zRot += Mth.cos(t * 0.35f) * 0.2f;

        if (state.isBaby) {
            this.head.xScale = this.head.yScale = this.head.zScale = 1.4f;
        }
    }
}
