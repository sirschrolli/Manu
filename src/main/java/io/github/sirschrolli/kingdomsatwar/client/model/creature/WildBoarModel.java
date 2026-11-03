package io.github.sirschrolli.kingdomsatwar.client.model.creature;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.CreatureRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Wildschwein: Trab, schnüffelnder Rüssel, wackelnder Ringelschwanz, gesenkter Kopf beim Angriff und Hauerstoß. */
public class WildBoarModel extends EntityModel<CreatureRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart earL;
    private final ModelPart earR;
    private final ModelPart tail;
    private final ModelPart frontLeft;
    private final ModelPart frontRight;
    private final ModelPart hindLeft;
    private final ModelPart hindRight;

    public WildBoarModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.earL = this.head.getChild("ear_l");
        this.earR = this.head.getChild("ear_r");
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

        this.frontLeft.xRot += Anim.leg(pos, speed, 0.0f, 1.3f);
        this.frontRight.xRot += Anim.leg(pos, speed, Mth.PI, 1.3f);
        this.hindLeft.xRot += Anim.leg(pos, speed, Mth.PI, 1.3f);
        this.hindRight.xRot += Anim.leg(pos, speed, 0.0f, 1.3f);
        this.body.y -= Math.abs(Mth.sin(pos * 0.6662f)) * 0.6f * speed;

        this.head.yRot += Mth.clamp(state.yRot, -40.0f, 40.0f) * DEG;
        this.head.xRot += Mth.clamp(state.xRot, -25.0f, 25.0f) * DEG * 0.6f;
        // Schnüffeln im Stand, wedelnder Schwanz, zuckende Ohren
        this.head.xRot += Mth.sin(t * 0.25f) * 0.035f * (1.0f - speed);
        this.tail.zRot += Mth.sin(t * 0.35f) * 0.35f;
        this.earL.zRot += Mth.sin(t * 0.09f) * 0.07f;
        this.earR.zRot -= Mth.sin(t * 0.09f + 1.3f) * 0.07f;

        if (state.charging) {
            this.head.xRot += 0.45f;
            this.earL.xRot -= 0.5f;
            this.earR.xRot -= 0.5f;
        }
        float s = Anim.seconds(state.attack, t);
        if (s >= 0.0f) {
            this.head.xRot -= 0.9f * Anim.bump(s, 0.1f, 0.15f, 0.45f);   // Hauerstoß nach oben
        }
        if (state.isBaby) {
            this.head.xScale = this.head.yScale = this.head.zScale = 1.3f;
        }
    }
}
