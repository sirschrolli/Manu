package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Greif: am Boden die Schwingen an den Flanken zusammengelegt (Arm hängt herab, die Hand liegt darunter, die gespreizten
 * Handschwingen ragen nach hinten über die Kruppe), Adlerläufe vorn, Löwenläufe hinten im Kreuzgang. Beim Ruhen sitzt er
 * aufrecht wie ein Wappentier, der Schwanz liegt um die Hinterpranken. Beim Schreien reckt er Hals und Kopf, reißt den
 * Schnabel auf und lüpft die Schwingen. In der Luft breitet er sie weit aus: langsame, kräftige Schläge mit nachfolgender
 * Hand, im Segelflug leicht V-förmig mit aufgebogenen Spitzen; die Läufe angelegt, der Schwanz als Steuer. Im Sturzflug
 * legt er die Schwingen zurück und bringt die Fänge nach vorn, beim Zuschlagen reißt er sie vor und bremst mit den
 * Schwingen. Neigung und Schräglage im Flug dreht der Renderer.
 */
public class GriffinModel extends EntityModel<GriffinRenderState> {
    /** Wie weit der Arm beim Zusammenlegen herabklappt (knapp senkrecht, unten etwas abstehend). */
    private static final float FOLD_Z = Mth.HALF_PI - 0.12f;

    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart tuftL;
    private final ModelPart tuftR;
    private final ModelPart wingL;
    private final ModelPart wingLMid;
    private final ModelPart wingLTip;
    private final ModelPart wingR;
    private final ModelPart wingRMid;
    private final ModelPart wingRTip;
    private final ModelPart tail;
    private final ModelPart tail2;
    private final ModelPart tailTuft;
    private final ModelPart frontL;
    private final ModelPart frontLShin;
    private final ModelPart frontLFoot;
    private final ModelPart frontR;
    private final ModelPart frontRShin;
    private final ModelPart frontRFoot;
    private final ModelPart hindL;
    private final ModelPart hindLShin;
    private final ModelPart hindLPaw;
    private final ModelPart hindR;
    private final ModelPart hindRShin;
    private final ModelPart hindRPaw;

    public GriffinModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.tuftL = this.head.getChild("tuft_l");
        this.tuftR = this.head.getChild("tuft_r");
        this.wingL = this.body.getChild("wing_l");
        this.wingLMid = this.wingL.getChild("wing_l_mid");
        this.wingLTip = this.wingLMid.getChild("wing_l_tip");
        this.wingR = this.body.getChild("wing_r");
        this.wingRMid = this.wingR.getChild("wing_r_mid");
        this.wingRTip = this.wingRMid.getChild("wing_r_tip");
        this.tail = this.body.getChild("tail");
        this.tail2 = this.tail.getChild("tail2");
        this.tailTuft = this.tail2.getChild("tail_tuft");
        this.frontL = root.getChild("left_front_leg");
        this.frontLShin = this.frontL.getChild("left_front_shin");
        this.frontLFoot = this.frontLShin.getChild("left_front_foot");
        this.frontR = root.getChild("right_front_leg");
        this.frontRShin = this.frontR.getChild("right_front_shin");
        this.frontRFoot = this.frontRShin.getChild("right_front_foot");
        this.hindL = root.getChild("left_hind_leg");
        this.hindLShin = this.hindL.getChild("left_hind_shin");
        this.hindLPaw = this.hindLShin.getChild("left_hind_paw");
        this.hindR = root.getChild("right_hind_leg");
        this.hindRShin = this.hindR.getChild("right_hind_shin");
        this.hindRPaw = this.hindRShin.getChild("right_hind_paw");
    }

    @Override
    public void setupAnim(GriffinRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float fly = state.fly;
        float ground = 1.0f - fly;
        float rest = state.rest * ground;
        float dive = state.dive * fly;
        float pos = state.walkAnimationPos;
        float speed = Math.min(1.0f, state.walkAnimationSpeed) * ground * (1.0f - rest);

        float sc = Anim.seconds(state.screech, t);
        float screech = sc >= 0.0f && sc < 2.0f ? Anim.bump(sc, 0.15f, 1.2f, 1.9f) : 0.0f;

        // ------------------------------------------------------------ Schwingen zusammenlegen (am Boden)
        float fold = ground;
        this.foldWing(this.wingL, this.wingLMid, this.wingLTip, fold, 1.0f);
        this.foldWing(this.wingR, this.wingRMid, this.wingRTip, fold, -1.0f);

        // ------------------------------------------------------------ Flug
        if (fly > 0.0f) {
            float beat = t * 0.3f;
            float flap = state.flap * (1.0f - dive);
            float glide = 1.0f - flap;
            // Schlag: der Arm führt, Hand und Spitze folgen später; Segeln: leichtes V, die Spitzen aufgebogen
            float inner = (Mth.sin(beat) * 0.7f * flap - 0.1f * glide) * fly;
            float mid = (Mth.sin(beat - 0.55f) * 0.3f * flap + 0.04f * glide) * fly;
            float tip = (Mth.sin(beat - 1.0f) * 0.38f * flap - 0.12f * glide) * fly;
            this.wingL.zRot += inner;
            this.wingR.zRot -= inner;
            this.wingLMid.zRot += mid;
            this.wingRMid.zRot -= mid;
            this.wingLTip.zRot += tip;
            this.wingRTip.zRot -= tip;
            float sweep = Mth.cos(beat) * 0.08f * flap * fly;
            this.wingL.yRot += sweep;
            this.wingR.yRot -= sweep;
            float lift = Mth.sin(beat + 1.4f) * 0.8f * flap * fly;
            this.body.y += lift;
            this.frontL.y += lift;
            this.frontR.y += lift;
            this.hindL.y += lift;
            this.hindR.y += lift;
            // Sturzflug: Schwingen zurück, Handgelenk vor, Spitzen nach hinten
            this.wingL.yRot += -0.75f * dive;
            this.wingR.yRot += 0.75f * dive;
            this.wingL.zRot += -0.2f * dive;
            this.wingR.zRot += 0.2f * dive;
            this.wingLMid.yRot += 0.55f * dive;
            this.wingRMid.yRot -= 0.55f * dive;
            this.wingLTip.yRot += -0.85f * dive;
            this.wingRTip.yRot += 0.85f * dive;
            // Läufe angelegt (im Sturz die Fänge schon halb vorn), der Schwanz als Steuer, der Hals gestreckt
            float tuck = 1.35f - 1.6f * dive;
            this.frontL.xRot += tuck * fly;
            this.frontR.xRot += tuck * fly;
            this.frontLShin.xRot += 0.6f * fly;
            this.frontRShin.xRot += 0.6f * fly;
            this.frontLFoot.xRot += 0.5f * fly;
            this.frontRFoot.xRot += 0.5f * fly;
            this.hindL.xRot += 1.2f * fly;
            this.hindR.xRot += 1.2f * fly;
            this.hindLShin.xRot += 0.35f * fly;
            this.hindRShin.xRot += 0.35f * fly;
            this.hindLPaw.xRot += 0.5f * fly;
            this.hindRPaw.xRot += 0.5f * fly;
            this.tail.xRot += 0.75f * fly;
            this.tail2.xRot += -0.35f * fly;
            this.tail2.yRot += Mth.sin(t * 0.1f) * 0.1f * fly;
            this.neck.xRot += 0.45f * fly;
            this.head.xRot -= 0.45f * fly;
        }

        // ------------------------------------------------------------ Gehen: Kreuzgang, Kopf nickt, Schwanz pendelt
        float wp = pos * 0.7f;
        this.frontL.xRot += Mth.cos(wp) * 0.55f * speed;
        this.frontR.xRot += Mth.cos(wp + Mth.PI) * 0.55f * speed;
        this.frontLShin.xRot += Math.max(0.0f, -Mth.sin(wp)) * 0.6f * speed;
        this.frontRShin.xRot += Math.max(0.0f, Mth.sin(wp)) * 0.6f * speed;
        this.hindL.xRot += Mth.cos(wp + Mth.PI) * 0.5f * speed;
        this.hindR.xRot += Mth.cos(wp) * 0.5f * speed;
        this.hindLShin.xRot += Math.max(0.0f, Mth.sin(wp)) * 0.5f * speed;
        this.hindRShin.xRot += Math.max(0.0f, -Mth.sin(wp)) * 0.5f * speed;
        this.body.y += -Math.abs(Mth.sin(wp)) * 0.4f * speed;
        this.neck.xRot += Mth.sin(wp * 2.0f) * 0.05f * speed;
        this.tail.yRot += Mth.sin(wp) * 0.15f * speed;

        // ------------------------------------------------------------ Ruhe: Atmen, Schwanzspitze zuckt
        this.body.y += Mth.sin(t * 0.08f) * 0.15f * ground;
        this.tail.yRot += Mth.sin(t * 0.05f) * 0.12f * ground;
        this.tailTuft.xRot += Mth.sin(t * 0.09f) * 0.15f;

        // ------------------------------------------------------------ Kopf: Blick, ruckartiges Kopfneigen wie ein Adler
        float yaw = Mth.clamp(state.yRot, -70.0f, 70.0f) * DEG;
        this.neck.yRot += yaw * 0.35f;
        this.head.yRot += yaw * 0.65f;
        this.head.xRot += Mth.clamp(state.xRot, -45.0f, 45.0f) * DEG * 0.7f;
        this.head.zRot += (Mth.floor(t / 37.0f) % 4 == 1 ? 0.25f : 0.0f) * ground * (1.0f - screech);

        // ------------------------------------------------------------ Sitzen (aufrecht wie ein Wappentier)
        if (rest > 0.0f) {
            this.body.xRot += -0.524f * rest;
            this.body.y += 3.7f * rest;
            this.neck.xRot += 0.21f * rest;
            this.head.xRot += 0.31f * rest;
            this.frontL.xRot += -0.175f * rest;
            this.frontR.xRot += -0.175f * rest;
            this.frontL.z += -0.6f * rest;
            this.frontR.z += -0.6f * rest;
            this.frontLShin.xRot += 0.175f * rest;
            this.frontRShin.xRot += 0.175f * rest;
            this.hindL.xRot += -0.96f * rest;
            this.hindR.xRot += -0.96f * rest;
            this.hindL.y += 6.6f * rest;
            this.hindR.y += 6.6f * rest;
            this.hindL.z += -1.0f * rest;
            this.hindR.z += -1.0f * rest;
            this.hindLShin.xRot += 2.44f * rest;
            this.hindRShin.xRot += 2.44f * rest;
            this.hindLPaw.xRot += -1.48f * rest;
            this.hindRPaw.xRot += -1.48f * rest;
            this.tail2.xRot += 0.73f * rest;
            this.tail2.yRot += 0.96f * rest;
            this.tailTuft.yRot += 0.35f * rest;
        }

        // ------------------------------------------------------------ Schrei: Hals und Kopf hoch, Schnabel auf, Schwingen gelüpft
        if (screech > 0.0f) {
            this.neck.xRot += -0.35f * screech;
            this.head.xRot += -0.5f * screech + Mth.sin(t * 1.8f) * 0.05f * screech;
            this.jaw.xRot += 0.65f * screech;
            this.tuftL.xRot += 0.4f * screech;
            this.tuftR.xRot += 0.4f * screech;
            float mantle = screech * ground;
            this.wingL.zRot += -0.45f * mantle;
            this.wingR.zRot += 0.45f * mantle;
            this.wingL.yRot += 0.15f * mantle;
            this.wingR.yRot -= 0.15f * mantle;
        }

        // ------------------------------------------------------------ Zuschlagen im Sturzflug: Fänge vor, mit den Schwingen bremsen
        float st = Anim.seconds(state.strike, t);
        if (st >= 0.0f && st < 0.8f) {
            float s = Anim.bump(st, 0.08f, 0.25f, 0.7f);
            this.frontL.xRot += -2.4f * s * fly;
            this.frontR.xRot += -2.4f * s * fly;
            this.frontLShin.xRot += -0.4f * s;
            this.frontRShin.xRot += -0.4f * s;
            this.frontLFoot.xRot += -0.6f * s;
            this.frontRFoot.xRot += -0.6f * s;
            this.neck.xRot += -0.3f * s;
            this.head.xRot += 0.3f * s;
            this.wingL.zRot += -0.5f * s * fly;
            this.wingR.zRot += 0.5f * s * fly;
        }

        // ------------------------------------------------------------ Prankenhieb am Boden
        float sw = Anim.seconds(state.swipe, t);
        if (sw >= 0.0f && sw < 0.6f) {
            float s = Anim.bump(sw, 0.1f, 0.18f, 0.5f) * ground;
            this.frontR.xRot += -1.5f * s;
            this.frontRShin.xRot += -0.5f * s;
            this.frontRFoot.xRot += -0.4f * s;
            this.body.xRot += -0.1f * s;
            this.neck.xRot += 0.2f * s;
            this.head.xRot += 0.15f * s;
            this.jaw.xRot += 0.4f * s;
        }

        // ------------------------------------------------------------ Fressen: Kopf tief, reißen
        float e = Anim.seconds(state.eat, t);
        if (e >= 0.0f && e < 3.6f) {
            float eat = Anim.bump(e, 0.3f, 3.0f, 3.5f) * ground;
            this.neck.xRot += 1.0f * eat;
            this.head.xRot += 0.45f * eat + Mth.sin(t * 0.9f) * 0.15f * eat;
            this.head.yRot += Mth.sin(t * 0.45f) * 0.2f * eat;
            this.jaw.xRot += (0.3f + Mth.sin(t * 0.9f) * 0.25f) * eat;
            this.frontL.xRot += -0.2f * eat;
        }
    }

    /**
     * Legt eine Schwinge an der Flanke zusammen: der Arm klappt am Schultergelenk herab (Oberseite nach außen, die
     * Federn zeigen nach hinten), die Hand schlägt nach innen zurück hinauf, die Handschwingen liegen dazwischen und
     * zeigen nach hinten. {@code side}: 1 links, -1 rechts.
     */
    private void foldWing(ModelPart inner, ModelPart mid, ModelPart tip, float fold, float side) {
        if (fold <= 0.0f) {
            return;
        }
        inner.x += 2.25f * side * fold;
        inner.y += -1.0f * fold;
        inner.zRot += FOLD_Z * side * fold;
        mid.zRot += Mth.PI * side * fold;
        mid.y += 2.0f * fold;
        tip.yRot += -Mth.HALF_PI * side * fold;
        tip.y += 1.0f * fold;
    }
}
