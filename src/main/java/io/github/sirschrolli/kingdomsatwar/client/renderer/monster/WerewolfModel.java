package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Werwolf: aufrecht ein schwerer, gebeugter Gang – die langen Arme pendeln, die Krallen hängen, der Kopf wiegt sich, der
 * Schwanz schwingt mit. Jagt er, fällt er auf alle viere und rennt in weiten Sätzen (Arme und Beine greifen paarweise aus,
 * der Rücken streckt und krümmt sich, der Kopf bleibt nach vorn gerichtet). Beim Krallenhieb holt der rechte Arm weit aus
 * und reißt quer herab; beim Heulen legt er den Kopf in den Nacken, reißt das Maul auf und breitet die Arme aus; beim
 * Ansprung duckt er sich tief und streckt sich dann im Flug, die Krallen voran.
 */
public class WerewolfModel extends EntityModel<WerewolfRenderState> {
    private final ModelPart hips;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftForearm;
    private final ModelPart rightForearm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart leftShin;
    private final ModelPart rightShin;
    private final ModelPart leftFoot;
    private final ModelPart rightFoot;
    private final ModelPart tail;
    private final ModelPart tailTip;

    public WerewolfModel(ModelPart root) {
        super(root);
        this.hips = root.getChild("hips");
        this.body = this.hips.getChild("body");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.leftEar = this.head.getChild("left_ear");
        this.rightEar = this.head.getChild("right_ear");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftLeg = this.hips.getChild("left_leg");
        this.rightLeg = this.hips.getChild("right_leg");
        this.leftShin = this.leftLeg.getChild("left_shin");
        this.rightShin = this.rightLeg.getChild("right_shin");
        this.leftFoot = this.leftShin.getChild("left_foot");
        this.rightFoot = this.rightShin.getChild("right_foot");
        this.tail = this.hips.getChild("tail");
        this.tailTip = this.tail.getChild("tail_tip");
    }

    @Override
    public void setupAnim(WerewolfRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        float run = state.run;
        float walk = 1.0f - run;

        // ------------------------------------------------------------ aufrechter Gang
        float stride = pos * 0.6662f;
        this.leftLeg.xRot += Mth.cos(stride) * 0.75f * speed * walk;
        this.rightLeg.xRot += Mth.cos(stride + Mth.PI) * 0.75f * speed * walk;
        // das Knie beugt sich, wenn das Bein nach vorn schwingt
        this.leftShin.xRot += Math.max(0.0f, Mth.sin(stride)) * 0.6f * speed * walk;
        this.rightShin.xRot += Math.max(0.0f, Mth.sin(stride + Mth.PI)) * 0.6f * speed * walk;
        this.hips.y += Math.abs(Mth.cos(stride)) * 0.9f * speed * walk;
        this.hips.yRot += Mth.cos(stride) * 0.08f * speed * walk;
        this.body.zRot += Mth.cos(stride) * 0.06f * speed * walk + Mth.sin(t * 0.045f) * 0.02f;
        this.body.xRot += Mth.sin(t * 0.08f) * 0.025f;                       // er atmet schwer
        // die langen Arme pendeln locker gegengleich, die Krallen hängen
        this.leftArm.xRot += Mth.cos(stride + Mth.PI) * 0.55f * speed * walk + Mth.sin(t * 0.07f) * 0.04f;
        this.rightArm.xRot += Mth.cos(stride) * 0.55f * speed * walk + Mth.sin(t * 0.07f + 1.2f) * 0.04f;
        this.leftForearm.xRot += -0.15f * speed * walk;
        this.rightForearm.xRot += -0.15f * speed * walk;

        // ------------------------------------------------------------ auf allen vieren
        if (run > 0.001f) {
            float bound = pos * 0.6662f * 1.15f;
            float swing = Mth.sin(bound);
            // der Rücken neigt sich weit vor und streckt und krümmt sich im Takt
            float lean = 48.0f * DEG + swing * 0.12f * speed;
            this.body.xRot += lean * run;
            this.hips.y += 2.6f * run;
            this.hips.xRot += -swing * 0.1f * speed * run;
            // die Arme werden zu Vorderläufen: senkrecht zum Boden, dann greifen sie paarweise aus
            float front = -54.0f * DEG - swing * 0.75f * speed;
            this.leftArm.xRot += (front - 0.05f) * run;
            this.rightArm.xRot += (front + 0.05f) * run;
            this.leftArm.zRot += 6.0f * DEG * run;
            this.rightArm.zRot -= 6.0f * DEG * run;
            this.leftForearm.xRot += (25.0f * DEG + Math.max(0.0f, swing) * 0.5f) * run;
            this.rightForearm.xRot += (25.0f * DEG + Math.max(0.0f, swing) * 0.5f) * run;
            // die Hinterläufe stoßen gemeinsam ab
            float hind = swing * 0.85f * speed;
            this.leftLeg.xRot += (hind + 0.05f) * run;
            this.rightLeg.xRot += (hind - 0.05f) * run;
            this.leftShin.xRot += Math.max(0.0f, -swing) * 0.5f * run;
            this.rightShin.xRot += Math.max(0.0f, -swing) * 0.5f * run;
            // der Kopf bleibt nach vorn gerichtet, die Ohren legen sich an
            this.head.xRot -= lean * run * 0.9f;
            this.leftEar.xRot += 0.5f * run;
            this.rightEar.xRot += 0.5f * run;
            this.tail.xRot += (-0.6f + swing * 0.25f) * run;
        }

        // ------------------------------------------------------------ Kopf, Maul, Schwanz
        this.head.yRot += Mth.clamp(state.yRot, -50.0f, 50.0f) * DEG * 0.8f;
        this.head.xRot += Mth.clamp(state.xRot, -35.0f, 35.0f) * DEG * 0.6f;
        this.jaw.xRot += (0.06f + 0.06f * Mth.sin(t * 0.08f)) + 0.2f * run;           // hechelt
        this.leftEar.zRot += Mth.sin(t * 0.13f) > 0.96f ? 0.25f : 0.0f;                   // ein Ohr zuckt
        this.tail.yRot += Mth.sin(t * 0.09f) * 0.15f + Mth.cos(stride) * 0.2f * speed * walk;
        this.tailTip.yRot += Mth.sin(t * 0.09f - 0.8f) * 0.2f;
        this.tailTip.xRot += Mth.sin(t * 0.06f) * 0.06f;

        // ------------------------------------------------------------ Krallenhieb
        float swipe = Anim.seconds(state.swipe, t);
        if (swipe >= 0.0f && swipe < 0.6f) {
            float raise = Anim.bump(swipe, 0.14f, 0.18f, 0.28f);
            float strike = Anim.bump(swipe - 0.17f, 0.07f, 0.14f, 0.4f);
            this.rightArm.xRot += -2.4f * raise + 1.2f * strike;
            this.rightArm.zRot += 0.5f * raise - 0.7f * strike;
            this.rightForearm.xRot += -0.5f * raise + 0.3f * strike;
            this.leftArm.xRot += -0.6f * raise;
            this.body.yRot += -0.35f * raise + 0.45f * strike;
            this.body.xRot += 0.15f * strike;
            this.jaw.xRot += 0.35f * Math.max(raise, strike);
            this.head.xRot += 0.1f * strike;
        }

        // ------------------------------------------------------------ Heulen
        float howl = Anim.seconds(state.howl, t);
        if (howl >= 0.0f && howl < 1.85f) {
            float k = Anim.bump(howl, 0.3f, 1.4f, 1.8f);
            this.body.xRot += -0.45f * k;
            this.head.xRot += -0.95f * k;
            this.jaw.xRot += 0.55f * k * (0.9f + 0.1f * Mth.sin(t * 1.3f));
            this.leftArm.xRot += 0.35f * k;
            this.rightArm.xRot += 0.35f * k;
            this.leftArm.zRot += 0.55f * k;
            this.rightArm.zRot -= 0.55f * k;
            this.leftForearm.xRot += -0.4f * k;
            this.rightForearm.xRot += -0.4f * k;
            this.leftEar.xRot += 0.4f * k;
            this.rightEar.xRot += 0.4f * k;
            this.tail.xRot += -0.3f * k;
            this.hips.y += 0.6f * k;
        }

        // ------------------------------------------------------------ Ansprung
        float pounce = Anim.seconds(state.pounce, t);
        if (pounce >= 0.0f && pounce < 0.9f) {
            float crouch = Anim.bump(pounce, 0.06f, 0.1f, 0.18f);
            float leap = Anim.bump(pounce - 0.12f, 0.08f, 0.45f, 0.75f);
            this.hips.y += 3.0f * crouch;
            this.leftLeg.xRot += -0.6f * crouch + 0.9f * leap;
            this.rightLeg.xRot += -0.6f * crouch + 0.9f * leap;
            this.leftShin.xRot += 0.8f * crouch - 0.4f * leap;
            this.rightShin.xRot += 0.8f * crouch - 0.4f * leap;
            this.body.xRot += 0.3f * crouch + 0.35f * leap;
            this.head.xRot += -0.35f * leap;
            this.leftArm.xRot += -2.2f * leap + 0.3f * crouch;
            this.rightArm.xRot += -2.2f * leap + 0.3f * crouch;
            this.leftArm.zRot += 0.2f * leap;
            this.rightArm.zRot -= 0.2f * leap;
            this.leftForearm.xRot += 0.4f * leap;
            this.rightForearm.xRot += 0.4f * leap;
            this.jaw.xRot += 0.5f * leap;
            this.tail.xRot += -0.5f * leap;
        }
    }
}
