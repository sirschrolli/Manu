package io.github.sirschrolli.kingdomsatwar.client.model.creature;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.CreatureRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Hirsch: leichter Gang, Lauschen mit den Ohren, Grasen und – nur bei männlichen Tieren – das Geweih. */
public class DeerModel extends EntityModel<CreatureRenderState> {
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart earL;
    private final ModelPart earR;
    private final ModelPart antlerL;
    private final ModelPart antlerR;
    private final ModelPart tail;
    private final ModelPart frontLeft;
    private final ModelPart frontRight;
    private final ModelPart hindLeft;
    private final ModelPart hindRight;

    public DeerModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.earL = this.head.getChild("ear_l");
        this.earR = this.head.getChild("ear_r");
        this.antlerL = this.head.getChild("antler_l");
        this.antlerR = this.head.getChild("antler_r");
        this.tail = this.body.getChild("tail");
        this.frontLeft = root.getChild("front_left_leg");
        this.frontRight = root.getChild("front_right_leg");
        this.hindLeft = root.getChild("hind_left_leg");
        this.hindRight = root.getChild("hind_right_leg");
    }

    @Override
    public void setupAnim(CreatureRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);

        boolean antlers = state.stag && !state.isBaby;
        this.antlerL.visible = antlers;
        this.antlerR.visible = antlers;

        this.frontLeft.xRot += Anim.leg(pos, speed, 0.0f, 1.15f);
        this.frontRight.xRot += Anim.leg(pos, speed, Mth.PI, 1.15f);
        this.hindLeft.xRot += Anim.leg(pos, speed, Mth.PI, 1.15f);
        this.hindRight.xRot += Anim.leg(pos, speed, 0.0f, 1.15f);
        this.body.y -= Math.abs(Mth.sin(pos * 0.6662f)) * 0.5f * speed;
        // Beim Rennen den Hals nach vorne strecken
        this.neck.xRot += 0.35f * speed * speed;
        this.head.xRot -= 0.25f * speed * speed;

        float yaw = Mth.clamp(state.yRot, -50.0f, 50.0f) * DEG;
        this.neck.yRot += yaw * 0.5f;
        this.head.yRot += yaw * 0.5f;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * DEG * 0.5f;

        // Ohren lauschen abwechselnd, Schwanz zuckt
        this.earL.xRot += Math.max(0.0f, Mth.sin(t * 0.07f)) * 0.35f;
        this.earR.xRot += Math.max(0.0f, Mth.sin(t * 0.07f + 2.0f)) * 0.35f;
        this.tail.xRot -= Math.max(0.0f, Mth.sin(t * 0.21f)) * 0.4f;

        float s = Anim.seconds(state.special, t);
        if (s >= 0.0f) {
            float graze = Anim.bump(s, 0.35f, 1.65f, 2.0f);
            this.neck.xRot += 1.25f * graze;
            this.head.xRot += 0.35f * graze + Mth.sin(t * 1.1f) * 0.06f * graze;   // kauen
        }
        if (state.isBaby) {
            this.head.xScale = this.head.yScale = this.head.zScale = 1.35f;
        }
    }
}
