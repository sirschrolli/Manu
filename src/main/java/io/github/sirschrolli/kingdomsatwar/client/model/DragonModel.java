package io.github.sirschrolli.kingdomsatwar.client.model;

import io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim;
import io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose;
import io.github.sirschrolli.kingdomsatwar.client.renderer.DragonRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Drachen-Modell.
 *
 * <p>Die Geometrie kommt aus {@code DragonGeometry} (erzeugt von tools/generate_entities.py). Die Haltung in jedem Bild
 * bestimmt {@link DragonEntityMotion} (Atmen, Gang, Flug, Sitzen, Launen, Odem, Biss, Brüllen), die Federn
 * ({@link io.github.sirschrolli.kingdomsatwar.client.boss.DragonRig}) glätten sie; hier wird das Ergebnis auf die Knochen
 * gelegt. Alle Winkel sind Zusätze zur Grundhaltung aus der Geometrie.
 */
public class DragonModel extends EntityModel<DragonRenderState> {
    private static final float DEG = Mth.DEG_TO_RAD;

    private final ModelPart body;
    private final ModelPart neck1;
    private final ModelPart neck2;
    private final ModelPart neck3;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart[] tail = new ModelPart[6];
    private final ModelPart frontLegL;
    private final ModelPart frontLegR;
    private final ModelPart frontShinL;
    private final ModelPart frontShinR;
    private final ModelPart frontFootL;
    private final ModelPart frontFootR;
    private final ModelPart hindLegL;
    private final ModelPart hindLegR;
    private final ModelPart hindShinL;
    private final ModelPart hindShinR;
    private final ModelPart hindFootL;
    private final ModelPart hindFootR;
    private final ModelPart wingL;
    private final ModelPart wingR;
    private final ModelPart wingTipL;
    private final ModelPart wingTipR;

    public DragonModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.neck1 = this.body.getChild("neck1");
        this.neck2 = this.neck1.getChild("neck2");
        this.neck3 = this.neck2.getChild("neck3");
        this.head = this.neck3.getChild("head");
        this.jaw = this.head.getChild("jaw");
        ModelPart parent = this.body;
        for (int i = 0; i < this.tail.length; i++) {
            this.tail[i] = parent.getChild("tail" + (i + 1));
            parent = this.tail[i];
        }
        this.frontLegL = this.body.getChild("front_leg_l");
        this.frontLegR = this.body.getChild("front_leg_r");
        this.frontShinL = this.frontLegL.getChild("front_shin_l");
        this.frontShinR = this.frontLegR.getChild("front_shin_r");
        this.frontFootL = this.frontShinL.getChild("front_foot_l");
        this.frontFootR = this.frontShinR.getChild("front_foot_r");
        this.hindLegL = this.body.getChild("hind_leg_l");
        this.hindLegR = this.body.getChild("hind_leg_r");
        this.hindShinL = this.hindLegL.getChild("hind_shin_l");
        this.hindShinR = this.hindLegR.getChild("hind_shin_r");
        this.hindFootL = this.hindShinL.getChild("hind_foot_l");
        this.hindFootR = this.hindShinR.getChild("hind_foot_r");
        this.wingL = this.body.getChild("wing_l");
        this.wingR = this.body.getChild("wing_r");
        this.wingTipL = this.wingL.getChild("wing_tip_l");
        this.wingTipR = this.wingR.getChild("wing_tip_r");
    }

    @Override
    public void setupAnim(DragonRenderState s) {
        super.setupAnim(s);

        // Jungtiere: größerer Kopf, kürzerer Hals, kleinere Flügel
        float young = Mth.clamp((1.0f - s.stage.scale()) / 0.72f, 0.0f, 1.0f);
        if (young > 0.0f) {
            float headScale = 1.0f + 0.55f * young;
            this.head.xScale = this.head.yScale = this.head.zScale = headScale;
            this.neck1.zScale = this.neck2.zScale = this.neck3.zScale = 1.0f - 0.25f * young;
            float wingScale = 1.0f - 0.35f * young;
            this.wingL.xScale = this.wingL.yScale = this.wingL.zScale = wingScale;
            this.wingR.xScale = this.wingR.yScale = this.wingR.zScale = wingScale;
        }

        // Rumpf (Hals, Schwanz, Beine und Flügel hängen daran): Haltung aus den Federn, dazu Steigen/Sinken und die
        // Schräglage im Kurvenflug
        this.body.y += s.bodyY;
        this.body.xRot += s.bodyPitch - Mth.clamp(s.flightPitch, -40.0f, 35.0f) * DEG * 0.6f * s.airBlend;
        this.body.zRot += s.bodyRoll + s.bank * DEG * 0.75f;
        this.body.yRot += s.bodyYaw;
        this.body.xScale *= 1.0f + (s.chest - 1.0f) * 0.6f;
        this.body.yScale *= 1.0f + (s.chest - 1.0f) * 0.4f;
        // Hals und Kopf
        ModelPart[] neck = {this.neck1, this.neck2, this.neck3};
        for (int i = 0; i < neck.length && i < s.neckPitch.length; i++) {
            neck[i].xRot += s.neckPitch[i];
            neck[i].yRot += s.neckYaw[i];
            neck[i].zRot += s.neckRoll[i];
        }
        this.head.xRot += s.headPitch;
        this.head.yRot += s.headYaw;
        this.head.zRot += s.headRoll;
        this.jaw.xRot += s.jaw;
        // Schwanz
        for (int i = 0; i < this.tail.length && i < s.tailYaw.length; i++) {
            this.tail[i].yRot += s.tailYaw[i];
            this.tail[i].xRot += s.tailPitch[i];
        }
        // Beine
        this.applyLeg(this.frontLegL, this.frontShinL, this.frontFootL, s.legs[DragonPose.FRONT_L]);
        this.applyLeg(this.frontLegR, this.frontShinR, this.frontFootR, s.legs[DragonPose.FRONT_R]);
        this.applyLeg(this.hindLegL, this.hindShinL, this.hindFootL, s.legs[DragonPose.HIND_L]);
        this.applyLeg(this.hindLegR, this.hindShinR, this.hindFootR, s.legs[DragonPose.HIND_R]);
        // Flügel
        this.wing(s, this.wingL, this.wingTipL, 0, 1.0f);
        this.wing(s, this.wingR, this.wingTipR, 1, -1.0f);
    }

    private void applyLeg(ModelPart upper, ModelPart shin, ModelPart foot, float[] v) {
        upper.xRot += v[DragonPose.UPPER_X];
        upper.zRot += v[DragonPose.UPPER_Z];
        shin.xRot += v[DragonPose.SHIN_X];
        foot.xRot += v[DragonPose.FOOT_X];
    }

    // ------------------------------------------------------------------ Flügel

    /**
     * Ein Flügel ({@code sx} = 1 links, -1 rechts): anlegen bzw. ausbreiten – Arm und Spitze mit eigener Feder, so
     * entfaltet sich der Flügel gestaffelt (erst der Arm, dann die Spitze) –, dazu der Flügelschlag: kräftiger, schneller
     * Abschlag, langsamer Aufschlag mit eingeknickter Spitze, die Bewegung läuft als Welle vom Arm zur Spitze.
     */
    private void wing(DragonRenderState s, ModelPart arm, ModelPart tip, int side, float sx) {
        float fa = Mth.clamp(s.foldArm[side], 0.0f, 1.0f);
        // Beim Anlegen klappt zuerst die Spitze ein, beim Ausbreiten öffnet sie sich zuletzt – so hängt ein halb
        // offener Flügel nie bis zum Boden herab
        float ft = 1.0f - (1.0f - fa) * (1.0f - fa);
        // Anlegen: der Arm schwenkt nach hinten und dreht sich zuletzt um die eigene Längsachse (die Flughaut hängt dann
        // senkrecht am Körper); die Spitze klappt über oben nach vorn zurück und wird zusammengeschoben
        float idle = Mth.sin(s.ageInTicks * 0.05f) * 0.025f;
        arm.xRot += -1.42f * fa * fa;
        arm.yRot += -1.32f * sx * fa;
        arm.zRot += (-0.42f + idle) * sx * fa;
        arm.zScale *= 1.0f - 0.5f * fa;
        tip.zRot += -3.333f * sx * ft;
        tip.yRot += 0.12f * sx * ft;
        tip.xScale *= 1.0f - 0.45f * ft;

        // Flügelschlag (nur soweit der Flügel offen ist)
        float open = 1.0f - fa;
        float amp = s.flapAmp * open * open;
        float stroke = BossAnim.stroke(s.flap);
        float strokeLag = BossAnim.stroke(s.flap - 0.6f);
        float up = BossAnim.upstroke(s.flap);
        // oben etwa 50°, unten gut 20° unter die Waagerechte; die Spitze schwingt nach und knickt im Aufschlag ein
        float armSwing = stroke > 0.0f ? stroke * 0.88f : stroke * 0.38f;
        float lag = strokeLag < 0.0f ? strokeLag * 0.16f : strokeLag * 0.3f;
        arm.zRot += (-armSwing - 0.08f - s.raise * 0.6f) * sx * amp + (-s.raise * 0.6f) * sx * (1.0f - amp) * open;
        arm.xRot += stroke * 0.08f * amp;
        arm.yRot += 0.12f * sx * open - (s.sweep * 0.6f + up * 0.16f * amp) * sx;
        tip.zRot += (-lag + up * 0.55f) * sx * amp;
        // Gleiten: die Spitzen zittern leicht im Fahrtwind, gespreizt
        tip.zRot += (Mth.sin(s.ageInTicks * 0.55f + side) * 0.02f - s.spread * 0.15f) * sx * open * (1.0f - Math.min(1.0f, s.flapAmp * 3.0f));
    }
}
