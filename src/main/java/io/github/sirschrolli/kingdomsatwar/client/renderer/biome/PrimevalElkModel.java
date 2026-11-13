package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Urelch: Er geht im Kreuzgang mit hohen Knien, beim Traben weit ausgreifend, der schwere Kopf nickt im Takt. Zum Grasen
 * senkt er Hals und Kopf bis zum Boden und kaut; beim Aufbäumen hebt er die Vorderläufe nur halb und wirft den Kopf.
 * Im Ansturm senkt er die Schaufeln nach vorn; der Stoß reißt den Kopf hoch. Beim Brunftruf reckt er den Hals, hebt den
 * Kopf und öffnet das Maul. Die Ranken an den Schaufeln pendeln mit. Sattel, Zaumzeug und Truhen nur, wenn er sie trägt;
 * Kälber ohne Schaufeln, mit großem Kopf.
 */
public class PrimevalElkModel extends EntityModel<PrimevalElkRenderState> {
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart bell;
    private final ModelPart earL;
    private final ModelPart earR;
    private final ModelPart antlerL;
    private final ModelPart antlerR;
    private final ModelPart palmL;
    private final ModelPart palmR;
    private final ModelPart bridle;
    private final ModelPart tail;
    private final ModelPart saddle;
    private final ModelPart chestL;
    private final ModelPart chestR;
    private final ModelPart frontL;
    private final ModelPart frontLShin;
    private final ModelPart frontR;
    private final ModelPart frontRShin;
    private final ModelPart hindL;
    private final ModelPart hindLShin;
    private final ModelPart hindR;
    private final ModelPart hindRShin;

    public PrimevalElkModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.bell = this.head.getChild("bell");
        this.earL = this.head.getChild("ear_l");
        this.earR = this.head.getChild("ear_r");
        this.antlerL = this.head.getChild("antler_l");
        this.antlerR = this.head.getChild("antler_r");
        this.palmL = this.antlerL.getChild("antler_l_palm");
        this.palmR = this.antlerR.getChild("antler_r_palm");
        this.bridle = this.head.getChild("bridle");
        this.tail = this.body.getChild("tail");
        this.saddle = this.body.getChild("saddle");
        this.chestL = this.body.getChild("chest_l");
        this.chestR = this.body.getChild("chest_r");
        this.frontL = root.getChild("left_front_leg");
        this.frontLShin = this.frontL.getChild("left_front_shin");
        this.frontR = root.getChild("right_front_leg");
        this.frontRShin = this.frontR.getChild("right_front_shin");
        this.hindL = root.getChild("left_hind_leg");
        this.hindLShin = this.hindL.getChild("left_hind_shin");
        this.hindR = root.getChild("right_hind_leg");
        this.hindRShin = this.hindR.getChild("right_hind_shin");
    }

    @Override
    public void setupAnim(PrimevalElkRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(1.0f, state.walkAnimationSpeed);
        boolean antlers = state.bull && !state.isBaby;
        this.antlerL.visible = antlers;
        this.antlerR.visible = antlers;
        this.saddle.visible = state.saddled;
        this.bridle.visible = state.saddled;
        this.chestL.visible = state.chested;
        this.chestR.visible = state.chested;

        float stand = state.stand;
        float upright = 1.0f - stand;
        float trot = state.trot;

        // ------------------------------------------------------------ Gehen und Traben: Kreuzgang, hohe Knie
        float phase = pos * 0.6662f;
        float amp = (0.5f + 0.25f * trot) * speed * upright;
        this.legStep(this.frontL, this.frontLShin, phase, amp, 1.0f);
        this.legStep(this.frontR, this.frontRShin, phase + Mth.PI, amp, 1.0f);
        this.legStep(this.hindL, this.hindLShin, phase + Mth.PI, amp, 0.8f);
        this.legStep(this.hindR, this.hindRShin, phase, amp, 0.8f);
        this.body.y += Mth.abs(Mth.sin(phase)) * -0.6f * speed * (0.6f + trot) * upright;
        this.neck.xRot += Mth.sin(phase * 2.0f) * 0.05f * speed;
        this.head.xRot += Mth.sin(phase * 2.0f + 0.6f) * 0.04f * speed;

        // ------------------------------------------------------------ Kopf
        float headYaw = Mth.clamp(state.yRot, -45.0f, 45.0f) * DEG;
        float headPitch = Mth.clamp(state.xRot, -30.0f, 30.0f) * DEG;
        this.neck.yRot += headYaw * 0.5f;
        this.head.yRot += headYaw * 0.4f;
        this.head.xRot += headPitch * 0.5f;
        this.neck.xRot += Mth.sin(t * 0.04f) * 0.02f;
        this.earL.zRot += Math.max(0.0f, Mth.sin(t * 0.071f)) * 0.3f;
        this.earR.zRot -= Math.max(0.0f, Mth.sin(t * 0.071f + 2.0f)) * 0.3f;
        this.earL.yRot += Mth.sin(t * 0.05f) * 0.15f;
        this.earR.yRot -= Mth.sin(t * 0.05f + 1.3f) * 0.15f;
        this.bell.xRot += Mth.sin(t * 0.09f + pos * 0.3f) * 0.12f;
        this.tail.xRot -= Math.max(0.0f, Mth.sin(t * 0.17f)) * 0.35f;
        this.tail.zRot += Mth.sin(t * 0.11f) * 0.12f;
        // die Schaufeln schwingen mit dem Kopf leicht nach, die Ranken an ihnen hängen
        this.palmL.zRot += Mth.sin(t * 0.06f) * 0.02f;
        this.palmR.zRot -= Mth.sin(t * 0.06f) * 0.02f;

        // ------------------------------------------------------------ Grasen
        float eat = state.eat * upright;
        if (eat > 0.0f) {
            this.neck.xRot += 0.85f * eat;
            this.head.xRot += 0.35f * eat;
            this.jaw.xRot += (0.06f + Mth.sin(t * 0.7f) * 0.06f) * eat;
            this.frontL.xRot -= 0.06f * eat;
            this.frontR.xRot -= 0.06f * eat;
        }

        // ------------------------------------------------------------ Ansturm
        float charge = state.charge * upright;
        if (charge > 0.0f) {
            this.neck.xRot += 0.35f * charge;
            this.head.xRot += 0.5f * charge;
            this.earL.zRot += 0.4f * charge;
            this.earR.zRot -= 0.4f * charge;
            this.body.xRot += 0.04f * charge;
        }

        // ------------------------------------------------------------ Aufbäumen: halb hoch, Kopf werfen
        if (stand > 0.0f) {
            this.body.xRot += -0.55f * stand;
            this.body.y += -2.5f * stand;
            this.body.z += 3.5f * stand;
            this.frontL.y += -7.0f * stand;
            this.frontR.y += -7.0f * stand;
            this.frontL.z += 4.5f * stand;
            this.frontR.z += 4.5f * stand;
            this.frontL.xRot += -0.9f * stand + Mth.sin(t * 0.5f) * 0.3f * stand;
            this.frontR.xRot += -0.7f * stand - Mth.sin(t * 0.5f) * 0.3f * stand;
            this.frontLShin.xRot += 1.1f * stand;
            this.frontRShin.xRot += 1.2f * stand;
            this.hindL.xRot += 0.3f * stand;
            this.hindR.xRot += 0.3f * stand;
            this.neck.xRot += -0.25f * stand;
            this.head.xRot += -0.4f * stand + Mth.sin(t * 0.45f) * 0.25f * stand;
        }
        this.jaw.xRot += state.mouth * 0.5f;

        // ------------------------------------------------------------ Brunftruf
        float b = Anim.seconds(state.bugle, t);
        if (b >= 0.0f && b < 3.0f) {
            float call = Anim.bump(b, 0.4f, 2.1f, 2.8f) * upright;
            this.neck.xRot += -0.45f * call;
            this.head.xRot += -0.75f * call;
            this.jaw.xRot += (0.45f + Mth.sin(t * 1.3f) * 0.08f) * call;
            this.earL.zRot += 0.5f * call;
            this.earR.zRot -= 0.5f * call;
            this.bell.xRot += -0.3f * call;
        }

        // ------------------------------------------------------------ Stoß mit den Schaufeln
        float s = Anim.seconds(state.impact, t);
        if (s >= 0.0f && s < 0.7f) {
            float down = Anim.bump(s, 0.08f, 0.12f, 0.2f);
            float toss = Anim.bump(s - 0.12f, 0.08f, 0.2f, 0.5f);
            this.neck.xRot += 0.35f * down - 0.4f * toss;
            this.head.xRot += 0.3f * down - 0.6f * toss;
            this.body.xRot += -0.05f * toss;
        }

        if (state.isBaby) {
            this.head.xScale = this.head.yScale = this.head.zScale = 1.4f;
        }
    }

    /** Ein Schritt: der Lauf schwingt, das untere Glied knickt beim Vorschwingen ein (hohe Knie). */
    private void legStep(ModelPart upper, ModelPart lower, float phase, float amp, float bend) {
        upper.xRot += Mth.cos(phase) * amp;
        lower.xRot += Math.max(0.0f, Mth.sin(phase)) * amp * bend * 1.6f;
    }
}
