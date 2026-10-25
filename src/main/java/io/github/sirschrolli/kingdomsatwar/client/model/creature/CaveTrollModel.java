package io.github.sirschrolli.kingdomsatwar.client.model.creature;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.CreatureRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Höhlentroll: schwerfälliger Gang mit Schaukeln, schwingende Arme, Atmen, Keulenhieb von oben, Zittern im Sonnenlicht. */
public class CaveTrollModel extends EntityModel<CreatureRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public CaveTrollModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.leftEar = this.head.getChild("left_ear");
        this.rightEar = this.head.getChild("right_ear");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    @Override
    public void setupAnim(CreatureRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        float stride = pos * 0.5f;

        this.leftLeg.xRot += Mth.cos(stride) * 0.9f * speed;
        this.rightLeg.xRot += Mth.cos(stride + Mth.PI) * 0.9f * speed;
        this.leftArm.xRot += Mth.cos(stride + Mth.PI) * 0.6f * speed;
        this.rightArm.xRot += Mth.cos(stride) * 0.35f * speed;
        this.body.zRot += Mth.sin(stride) * 0.07f * speed;                 // schaukeln
        this.body.y += Math.abs(Mth.cos(stride)) * 1.0f * speed;

        // Atmen und hängende Arme
        float breathe = Mth.sin(t * 0.06f);
        this.body.xRot += breathe * 0.025f;
        this.head.xRot -= breathe * 0.02f;
        this.leftArm.zRot -= 0.05f + breathe * 0.03f;
        this.rightArm.zRot += 0.05f + breathe * 0.03f;
        this.leftEar.yRot += Mth.sin(t * 0.13f) * 0.06f;
        this.rightEar.yRot -= Mth.sin(t * 0.11f) * 0.06f;

        this.head.yRot += Mth.clamp(state.yRot, -45.0f, 45.0f) * DEG * 0.8f;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * DEG * 0.5f;

        float s = Anim.seconds(state.attack, t);
        if (s >= 0.0f && s < 0.9f) {
            // Ausholen (bis 0,25 s), Zuschlagen (bis 0,38 s), Erholen
            float raise = s < 0.25f ? Mth.sin(s / 0.25f * Mth.HALF_PI) : s < 0.38f ? 1.0f - (s - 0.25f) / 0.13f : 0.0f;
            float slam = s < 0.25f ? 0.0f : s < 0.38f ? (s - 0.25f) / 0.13f : Math.max(0.0f, 1.0f - (s - 0.38f) / 0.5f);
            this.rightArm.xRot -= 2.7f * raise - 0.55f * slam;
            this.rightArm.zRot -= 0.25f * raise;
            this.body.xRot -= 0.15f * raise - 0.3f * slam;
            this.head.xRot += 0.2f * slam;
            this.leftArm.xRot -= 0.5f * raise;
        }
        if (state.petrifying) {
            this.body.zRot += Mth.sin(t * 2.7f) * 0.025f;                    // Zittern
            this.head.yRot += Mth.sin(t * 3.1f) * 0.04f;
        }
    }
}
