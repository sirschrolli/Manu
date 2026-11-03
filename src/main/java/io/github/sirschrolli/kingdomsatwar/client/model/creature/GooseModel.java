package io.github.sirschrolli.kingdomsatwar.client.model.creature;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.CreatureRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Gans: Watschelgang, nickender Hals, Flügelschlag, Schnattern mit hochgerecktem Kopf und Drohhaltung. */
public class GooseModel extends EntityModel<CreatureRenderState> {
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public GooseModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.leftWing = this.body.getChild("left_wing");
        this.rightWing = this.body.getChild("right_wing");
        this.tail = this.body.getChild("tail");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    @Override
    public void setupAnim(CreatureRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);

        this.leftLeg.xRot += Anim.leg(pos, speed, 0.0f, 1.4f);
        this.rightLeg.xRot += Anim.leg(pos, speed, Mth.PI, 1.4f);
        this.body.zRot += Mth.sin(pos * 0.6662f) * 0.14f * speed;          // watscheln
        this.tail.yRot += Mth.sin(pos * 0.6662f) * 0.2f * speed;
        this.neck.xRot += Mth.sin(pos * 1.3324f) * 0.12f * speed + Mth.sin(t * 0.1f) * 0.04f;

        float yaw = Mth.clamp(state.yRot, -60.0f, 60.0f) * DEG;
        this.neck.yRot += yaw * 0.5f;
        this.head.yRot += yaw * 0.5f;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * DEG * 0.6f;

        float flapAngle = (Mth.sin(state.flap) + 1.0f) * state.flapSpeed;
        this.leftWing.zRot -= flapAngle;
        this.rightWing.zRot += flapAngle;

        if (state.angry) {
            // Drohhaltung: Hals nach vorne, Flügel leicht gespreizt
            this.neck.xRot += 0.95f;
            this.head.xRot -= 0.8f;
            this.leftWing.zRot -= 0.5f;
            this.rightWing.zRot += 0.5f;
        }
        float s = Anim.seconds(state.attack, t);
        if (s >= 0.0f) {
            float honk = Anim.bump(s, 0.08f, 0.3f, 0.5f);
            this.neck.xRot -= 0.25f * honk;
            this.head.xRot -= 0.45f * honk;
        }
        if (state.isBaby) {
            this.head.xScale = this.head.yScale = this.head.zScale = 1.4f;
        }
    }
}
