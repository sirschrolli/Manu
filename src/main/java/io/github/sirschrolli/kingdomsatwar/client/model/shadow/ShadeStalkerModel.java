package io.github.sirschrolli.kingdomsatwar.client.model.shadow;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.shadow.ShadowCreatureRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Schattenpirscher: lauernder, schleichender Gang, pendelnde Krallenarme, wehender Umhang, Krallenhieb. */
public class ShadeStalkerModel extends EntityModel<ShadowCreatureRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart cloak;
    private final ModelPart wispL;
    private final ModelPart wispR;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftForearm;
    private final ModelPart rightForearm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public ShadeStalkerModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.cloak = this.body.getChild("cloak");
        this.wispL = this.head.getChild("wisp_l");
        this.wispR = this.head.getChild("wisp_r");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    @Override
    public void setupAnim(ShadowCreatureRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        float stride = pos * 0.55f;

        // langer, schleichender Schritt
        this.leftLeg.xRot += Mth.cos(stride) * 0.75f * speed;
        this.rightLeg.xRot += Mth.cos(stride + Mth.PI) * 0.75f * speed;
        this.leftArm.xRot += Mth.cos(stride + Mth.PI) * 0.45f * speed;
        this.rightArm.xRot += Mth.cos(stride) * 0.45f * speed;
        this.body.y += Math.abs(Mth.sin(stride)) * 0.8f * speed;
        this.body.xRot += speed * 0.12f;                                 // im Lauf tiefer gebeugt

        // Lauern: langsames Wiegen, pendelnde Krallen, wehender Umhang und Schattenfäden
        float sway = Mth.sin(t * 0.05f);
        this.body.zRot += sway * 0.04f;
        this.head.zRot -= sway * 0.06f;
        this.leftForearm.xRot -= 0.15f + Mth.sin(t * 0.07f) * 0.08f;
        this.rightForearm.xRot -= 0.15f + Mth.sin(t * 0.07f + 1.3f) * 0.08f;
        this.cloak.xRot += 0.08f + speed * 0.4f + Mth.sin(t * 0.09f) * 0.06f;
        this.wispL.xRot += Mth.sin(t * 0.11f) * 0.15f - speed * 0.3f;
        this.wispR.xRot += Mth.sin(t * 0.11f + 0.8f) * 0.15f - speed * 0.3f;

        this.head.yRot += Mth.clamp(state.yRot, -50.0f, 50.0f) * DEG;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * DEG * 0.7f;

        // Krallenhieb: beide Arme holen aus und reißen über Kreuz nach unten
        float s = Anim.seconds(state.attack, t);
        if (s >= 0.0f && s < 0.6f) {
            float raise = Anim.bump(s, 0.15f, 0.18f, 0.3f);
            float strike = Anim.bump(s - 0.18f, 0.08f, 0.12f, 0.42f);
            this.rightArm.xRot -= 2.4f * raise - 0.6f * strike;
            this.leftArm.xRot -= 1.8f * raise - 0.4f * strike;
            this.rightArm.zRot -= 0.4f * raise;
            this.leftArm.zRot += 0.4f * raise;
            this.body.xRot -= 0.2f * raise - 0.35f * strike;
            this.head.xRot += 0.25f * strike;
        }
    }
}
