package io.github.sirschrolli.kingdomsatwar.client.model.creature;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.VinelingRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Rankling: wiegt sich beim Gehen hin und her, die Rankenarme peitschen locker nach, die Knospe nickt. Beim
 * Zuschnappen klappen die Blütenblätter auf und die Arme schlagen zu; beim Herauswachsen entfaltet er sich.
 */
public class VinelingModel extends EntityModel<VinelingRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart[] petals = new ModelPart[4];
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftForearm;
    private final ModelPart rightForearm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public VinelingModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.petals[0] = this.head.getChild("petal_front");
        this.petals[1] = this.head.getChild("petal_back");
        this.petals[2] = this.head.getChild("petal_left");
        this.petals[3] = this.head.getChild("petal_right");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftLeg = this.body.getChild("left_leg");
        this.rightLeg = this.body.getChild("right_leg");
    }

    @Override
    public void setupAnim(VinelingRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        // Gehen: wiegen, Beine schwingen, Arme peitschen nach
        this.leftLeg.xRot += Anim.leg(pos, speed, 0.0f, 1.1f);
        this.rightLeg.xRot += Anim.leg(pos, speed, Mth.PI, 1.1f);
        this.body.zRot += Mth.sin(pos * 0.6662f) * 0.12f * speed + Mth.sin(t * 0.08f) * 0.03f;
        this.body.xRot += 0.12f * speed;
        float sway = Mth.sin(t * 0.11f);
        this.leftArm.zRot += -0.1f * sway - Anim.leg(pos, speed, Mth.PI, 0.3f);
        this.rightArm.zRot += -0.1f * sway + Anim.leg(pos, speed, 0.0f, 0.3f);
        this.leftArm.xRot += Anim.leg(pos, speed, Mth.PI, 0.6f);
        this.rightArm.xRot += Anim.leg(pos, speed, 0.0f, 0.6f);
        this.leftForearm.xRot += -0.25f - 0.2f * Mth.sin(t * 0.13f + 1.0f);
        this.rightForearm.xRot += -0.25f - 0.2f * Mth.sin(t * 0.13f + 2.4f);
        float yaw = Mth.clamp(state.yRot, -50.0f, 50.0f) * DEG;
        this.head.yRot += yaw;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * DEG * 0.6f + Mth.sin(t * 0.09f) * 0.05f;
        // Knospe atmet (Blätter öffnen sich leicht)
        float breathe = 0.06f + 0.05f * Mth.sin(t * 0.1f);
        this.openPetals(breathe);
        // Zuschnappen
        float snap = Anim.seconds(state.attack, t);
        if (snap >= 0.0f && snap < 0.7f) {
            float open = Anim.bump(snap, 0.12f, 0.2f, 0.45f);
            this.openPetals(0.9f * open);
            this.head.xRot += 0.35f * Anim.bump(snap, 0.15f, 0.22f, 0.5f);
            float lash = Anim.bump(snap, 0.1f, 0.2f, 0.6f);
            this.leftArm.xRot -= 1.4f * lash;
            this.rightArm.xRot -= 1.4f * lash;
            this.leftForearm.xRot -= 0.6f * lash;
            this.rightForearm.xRot -= 0.6f * lash;
        }
        // Herauswachsen: zusammengerollt, dann entfaltet
        float curl = 1.0f - state.sprout;
        if (curl > 0.0f) {
            this.body.xRot += 0.5f * curl;
            this.leftArm.zRot -= 1.2f * curl;
            this.rightArm.zRot += 1.2f * curl;
            this.openPetals(-0.1f * curl);
        }
    }

    private void openPetals(float amount) {
        this.petals[0].xRot -= amount;
        this.petals[1].xRot += amount;
        this.petals[2].zRot += amount;
        this.petals[3].zRot -= amount;
    }
}
