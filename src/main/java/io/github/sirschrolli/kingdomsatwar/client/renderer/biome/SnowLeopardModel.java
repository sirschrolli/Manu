package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Schneeleopard: Im Schritt setzt er die Läufe über Kreuz und knickt Ellbogen und Sprunggelenk ein, im Lauf springt er
 * mit gebeugtem und gestrecktem Rücken; der lange Schwanz pendelt in Wellen hinterher. Zum Anschleichen duckt er sich
 * tief, Kopf und Schwanz flach, die Spitze zuckt. Beim Sprung kauert er kurz, streckt sich dann ganz – Vorderpranken weit
 * voraus, Hinterläufe nach hinten – und fängt sich ab. Liegend (Sphinx) ruhen die Vorderläufe ausgestreckt, die
 * Hinterläufe unter dem Leib, der Schwanz um die Seite gelegt. Beim Prankenhieb holt die rechte Pranke aus, das Maul
 * öffnet sich; beim Fressen senkt er den Kopf und kaut.
 */
public class SnowLeopardModel extends EntityModel<SnowLeopardRenderState> {
    /** So weit sinkt der Leib, wenn er liegt (Modell-Einheiten). */
    private static final float LIE_DROP = 6.4f;
    /** So weit beim Anschleichen. */
    private static final float STALK_DROP = 2.6f;

    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart earL;
    private final ModelPart earR;
    private final ModelPart[] tail = new ModelPart[4];
    private final ModelPart frontL;
    private final ModelPart frontLShin;
    private final ModelPart frontR;
    private final ModelPart frontRShin;
    private final ModelPart hindL;
    private final ModelPart hindLShin;
    private final ModelPart hindR;
    private final ModelPart hindRShin;
    private final ModelPart frontLPaw;
    private final ModelPart frontRPaw;
    private final ModelPart hindLPaw;
    private final ModelPart hindRPaw;

    public SnowLeopardModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.earL = this.head.getChild("ear_l");
        this.earR = this.head.getChild("ear_r");
        this.tail[0] = this.body.getChild("tail1");
        this.tail[1] = this.tail[0].getChild("tail2");
        this.tail[2] = this.tail[1].getChild("tail3");
        this.tail[3] = this.tail[2].getChild("tail4");
        this.frontL = root.getChild("left_front_leg");
        this.frontLShin = this.frontL.getChild("left_front_shin");
        this.frontR = root.getChild("right_front_leg");
        this.frontRShin = this.frontR.getChild("right_front_shin");
        this.hindL = root.getChild("left_hind_leg");
        this.hindLShin = this.hindL.getChild("left_hind_shin");
        this.hindR = root.getChild("right_hind_leg");
        this.hindRShin = this.hindR.getChild("right_hind_shin");
        this.frontLPaw = this.frontLShin.getChild("left_front_paw");
        this.frontRPaw = this.frontRShin.getChild("right_front_paw");
        this.hindLPaw = this.hindLShin.getChild("left_hind_paw");
        this.hindRPaw = this.hindRShin.getChild("right_hind_paw");
    }

    @Override
    public void setupAnim(SnowLeopardRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(1.0f, state.walkAnimationSpeed);
        float lie = state.lie;
        float stand = 1.0f - lie;
        float stalk = state.stalk * stand;
        float run = state.run * stand * (1.0f - stalk);
        float walk = speed * stand;

        // ------------------------------------------------------------ Gehen und Laufen
        float phase = pos * 0.6662f;
        // Schritt: Kreuzgang, Ellbogen und Sprunggelenk knicken beim Vorschwingen ein
        float stepAmp = 0.62f * (1.0f - run) * (1.0f - stalk * 0.45f);
        this.legStep(this.frontL, this.frontLShin, phase, walk * stepAmp, 0.9f);
        this.legStep(this.frontR, this.frontRShin, phase + Mth.PI, walk * stepAmp, 0.9f);
        this.legStep(this.hindL, this.hindLShin, phase + Mth.PI, walk * stepAmp, 0.7f);
        this.legStep(this.hindR, this.hindRShin, phase, walk * stepAmp, 0.7f);
        this.body.y += Mth.abs(Mth.sin(phase)) * -0.4f * walk * (1.0f - run);
        // Lauf: Vorderläufe und Hinterläufe paarweise, der Rücken beugt und streckt sich
        if (run > 0.01f) {
            float g = pos * 0.42f;
            float bound = Mth.sin(g) * walk * run;
            this.frontL.xRot += Mth.sin(g + 0.25f) * 1.05f * walk * run;
            this.frontR.xRot += Mth.sin(g) * 1.05f * walk * run;
            this.hindL.xRot += Mth.sin(g + Mth.PI + 0.25f) * 0.95f * walk * run;
            this.hindR.xRot += Mth.sin(g + Mth.PI) * 0.95f * walk * run;
            this.frontLShin.xRot += Math.max(0.0f, Mth.cos(g + 0.25f)) * 0.9f * walk * run;
            this.frontRShin.xRot += Math.max(0.0f, Mth.cos(g)) * 0.9f * walk * run;
            this.hindLShin.xRot += Math.max(0.0f, -Mth.cos(g + 0.25f)) * 0.8f * walk * run;
            this.hindRShin.xRot += Math.max(0.0f, -Mth.cos(g)) * 0.8f * walk * run;
            this.body.xRot += bound * 0.09f;
            this.body.y -= Mth.abs(Mth.cos(g)) * 1.1f * walk * run;
            this.neck.xRot -= bound * 0.12f;
            this.tail[0].xRot += 0.35f * walk * run;
        }

        // ------------------------------------------------------------ Kopf
        float headYaw = Mth.clamp(state.yRot, -55.0f, 55.0f) * DEG;
        float headPitch = Mth.clamp(state.xRot, -35.0f, 35.0f) * DEG;
        this.neck.yRot += headYaw * 0.45f;
        this.head.yRot += headYaw * 0.55f;
        this.head.xRot += headPitch * 0.6f;
        this.neck.xRot += Mth.sin(t * 0.05f) * 0.02f;                                     // Atmen
        // Ohren: abwechselnd lauschen
        this.earL.zRot += Math.max(0.0f, Mth.sin(t * 0.083f)) * 0.22f;
        this.earR.zRot -= Math.max(0.0f, Mth.sin(t * 0.083f + 2.4f)) * 0.22f;

        // ------------------------------------------------------------ Schwanz: Wellen, die nach hinten laufen
        float sway = 0.12f + walk * 0.08f;
        for (int i = 0; i < 4; i++) {
            this.tail[i].yRot += Mth.sin(t * 0.09f - i * 0.75f + pos * 0.2f) * sway * (0.6f + i * 0.25f);
            this.tail[i].xRot += Mth.sin(t * 0.06f - i * 0.6f) * 0.04f;
        }
        this.tail[3].xRot += Math.max(0.0f, Mth.sin(t * 0.21f)) * 0.18f;                 // die Spitze zuckt

        // ------------------------------------------------------------ Anschleichen
        if (stalk > 0.0f) {
            this.body.y += STALK_DROP * stalk;
            this.body.xRot += 0.04f * stalk;
            for (ModelPart leg : new ModelPart[] {this.frontL, this.frontR, this.hindL, this.hindR}) {
                leg.y += STALK_DROP * 0.55f * stalk;
            }
            this.frontL.xRot -= 0.35f * stalk;
            this.frontR.xRot -= 0.35f * stalk;
            this.frontLShin.xRot += 0.75f * stalk;
            this.frontRShin.xRot += 0.75f * stalk;
            this.hindL.xRot -= 0.45f * stalk;
            this.hindR.xRot -= 0.45f * stalk;
            this.hindLShin.xRot += 0.8f * stalk;
            this.hindRShin.xRot += 0.8f * stalk;
            this.neck.xRot += 0.38f * stalk;
            this.head.xRot -= 0.3f * stalk;
            this.earL.zRot += 0.35f * stalk;                                                 // Ohren flach
            this.earR.zRot -= 0.35f * stalk;
            this.tail[0].xRot += 0.45f * stalk;                                              // Schwanz flach nach hinten
            this.tail[1].xRot -= 0.12f * stalk;
            this.tail[3].yRot += Mth.sin(t * 0.6f) * 0.35f * stalk;                         // nur die Spitze zuckt
        }

        // ------------------------------------------------------------ Liegen (Sphinx)
        if (lie > 0.0f) {
            this.body.y += LIE_DROP * lie;
            this.frontL.y += LIE_DROP * lie;
            this.frontR.y += LIE_DROP * lie;
            this.hindL.y += LIE_DROP * lie;
            this.hindR.y += LIE_DROP * lie;
            this.frontL.xRot += -0.85f * lie;
            this.frontR.xRot += -0.85f * lie;
            this.frontLShin.xRot += -0.72f * lie;
            this.frontRShin.xRot += -0.72f * lie;
            this.hindL.xRot += -0.95f * lie;
            this.hindR.xRot += -0.95f * lie;
            this.hindL.zRot += -0.18f * lie;
            this.hindR.zRot += 0.18f * lie;
            this.hindLShin.xRot += 2.57f * lie;
            this.hindRShin.xRot += 2.57f * lie;
            // die Pranken liegen flach auf dem Boden
            this.frontLPaw.xRot += 1.57f * lie;
            this.frontRPaw.xRot += 1.57f * lie;
            this.hindLPaw.xRot += -1.62f * lie;
            this.hindRPaw.xRot += -1.62f * lie;
            this.neck.xRot -= 0.22f * lie;
            this.head.xRot += 0.12f * lie + Mth.sin(t * 0.03f) * 0.04f * lie;
            // der Schwanz liegt flach und schlingt sich um die rechte Seite
            this.tail[0].xRot += 0.6f * lie;
            this.tail[1].yRot -= 0.45f * lie;
            this.tail[2].yRot -= 0.55f * lie;
            this.tail[3].yRot -= 0.6f * lie;
            this.tail[1].xRot -= 0.15f * lie;
            this.tail[2].xRot -= 0.35f * lie;
            this.tail[3].xRot -= 0.62f * lie;
        }

        // ------------------------------------------------------------ Sprung
        float p = Anim.seconds(state.pounce, t);
        if (p >= 0.0f && p < 1.1f) {
            float crouch = Anim.bump(p, 0.08f, 0.12f, 0.22f);
            float stretch = Anim.bump(p - 0.12f, 0.12f, 0.42f, 0.75f);
            float land = Anim.bump(p - 0.6f, 0.08f, 0.15f, 0.45f);
            this.body.y += 2.2f * crouch - 0.5f * stretch + 1.2f * land;
            this.body.xRot += -0.25f * stretch + 0.12f * land;
            this.frontL.xRot += 0.3f * crouch - 1.35f * stretch + 0.4f * land;
            this.frontR.xRot += 0.3f * crouch - 1.25f * stretch + 0.4f * land;
            this.frontLShin.xRot += 0.6f * crouch - 0.2f * stretch;
            this.frontRShin.xRot += 0.6f * crouch - 0.2f * stretch;
            this.hindL.xRot += -0.5f * crouch + 1.1f * stretch - 0.3f * land;
            this.hindR.xRot += -0.5f * crouch + 1.0f * stretch - 0.3f * land;
            this.hindLShin.xRot += 0.9f * crouch + 0.3f * stretch;
            this.hindRShin.xRot += 0.9f * crouch + 0.3f * stretch;
            this.neck.xRot += 0.2f * stretch;
            this.head.xRot -= 0.35f * stretch;
            this.jaw.xRot += 0.55f * stretch;
            this.tail[0].xRot += 0.75f * stretch;
            this.tail[1].xRot -= 0.15f * stretch;
            this.tail[2].xRot -= 0.15f * stretch;
            this.tail[3].xRot -= 0.25f * stretch;
        }

        // ------------------------------------------------------------ Prankenhieb
        float s = Anim.seconds(state.swipe, t);
        if (s >= 0.0f && s < 0.5f) {
            float raise = Anim.bump(s, 0.1f, 0.14f, 0.24f);
            float strike = Anim.bump(s - 0.14f, 0.06f, 0.1f, 0.34f);
            this.frontR.xRot += -1.5f * raise - 0.4f * strike;
            this.frontR.zRot += 0.25f * raise - 0.35f * strike;
            this.frontRShin.xRot += 0.7f * raise - 0.5f * strike;
            this.frontR.y += -1.5f * raise;
            this.neck.yRot += 0.25f * strike;
            this.jaw.xRot += 0.6f * (raise + strike);
            this.earL.zRot += 0.4f * raise;
            this.earR.zRot -= 0.4f * raise;
            this.body.xRot -= 0.06f * raise;
        }

        // ------------------------------------------------------------ Fressen
        float e = Anim.seconds(state.eat, t);
        if (e >= 0.0f && e < 3.6f) {
            float down = Anim.bump(e, 0.4f, 3.1f, 3.5f) * stand;
            this.neck.xRot += 0.95f * down;
            this.head.xRot += 0.45f * down;
            this.jaw.xRot += (0.15f + Mth.sin(t * 0.9f) * 0.15f) * down;
            this.frontL.xRot -= 0.15f * down;
            this.frontR.xRot -= 0.15f * down;
        }

        // ------------------------------------------------------------ Junge: großer Kopf, große Pranken
        if (state.isBaby) {
            this.head.xScale = this.head.yScale = this.head.zScale = 1.45f;
        }
    }

    /** Ein Schritt: der Lauf schwingt, das untere Glied knickt beim Vorschwingen ein. */
    private void legStep(ModelPart upper, ModelPart lower, float phase, float amp, float bend) {
        upper.xRot += Mth.cos(phase) * amp;
        lower.xRot += Math.max(0.0f, Mth.sin(phase)) * amp * bend * 1.4f;
    }
}
