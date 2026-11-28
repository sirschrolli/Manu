package io.github.sirschrolli.kingdomsatwar.client.model.sea;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.sea.SeaRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Riesenkrabbe: die sechs Beine trippeln in versetzten Paaren (die Krabbe selbst wird im Renderer zum Seitwärtslaufen
 * gedreht), der Panzer wippt mit jedem Schritt, die Stielaugen schauen umher. Ab und zu klappert sie mit den Scheren;
 * beim Kneifen stößt die große Knackschere vor und schnappt zu.
 */
public class GiantCrabModel extends EntityModel<SeaRenderState> {
    private final ModelPart body;
    private final ModelPart eyeL;
    private final ModelPart eyeR;
    private final ModelPart armL;
    private final ModelPart armR;
    private final ModelPart clawL;
    private final ModelPart clawR;
    private final ModelPart fingerL;
    private final ModelPart fingerR;
    private final ModelPart[] legsL = new ModelPart[3];
    private final ModelPart[] legsR = new ModelPart[3];
    private final ModelPart[] lowL = new ModelPart[3];
    private final ModelPart[] lowR = new ModelPart[3];

    public GiantCrabModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.eyeL = this.body.getChild("left_eye");
        this.eyeR = this.body.getChild("right_eye");
        this.armL = this.body.getChild("left_arm");
        this.armR = this.body.getChild("right_arm");
        this.clawL = this.armL.getChild("left_forearm").getChild("left_claw");
        this.clawR = this.armR.getChild("right_forearm").getChild("right_claw");
        this.fingerL = this.clawL.getChild("left_claw_finger");
        this.fingerR = this.clawR.getChild("right_claw_finger");
        for (int k = 0; k < 3; k++) {
            this.legsL[k] = this.body.getChild("left_leg_" + k);
            this.legsR[k] = this.body.getChild("right_leg_" + k);
            this.lowL[k] = this.legsL[k].getChild("left_leg_" + k + "_low");
            this.lowR[k] = this.legsR[k].getChild("right_leg_" + k + "_low");
        }
    }

    @Override
    public void setupAnim(SeaRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        // Trippeln: je Seite drei Beine, abwechselnd versetzt; die Beine heben sich (zRot) und greifen (yRot)
        for (int k = 0; k < 3; k++) {
            float phase = k * 2.1f;
            float liftL = Math.max(0.0f, Mth.sin(pos * 1.4f + phase)) * 0.45f * speed;
            float liftR = Math.max(0.0f, Mth.sin(pos * 1.4f + phase + Mth.PI)) * 0.45f * speed;
            float reachL = Mth.cos(pos * 1.4f + phase) * 0.22f * speed;
            float reachR = Mth.cos(pos * 1.4f + phase + Mth.PI) * 0.22f * speed;
            this.legsL[k].zRot -= liftL;
            this.legsR[k].zRot += liftR;
            this.legsL[k].yRot += reachL;
            this.legsR[k].yRot -= reachR;
            this.lowL[k].zRot += liftL * 0.5f;
            this.lowR[k].zRot -= liftR * 0.5f;
            // im Stand ein feines Zucken
            this.legsL[k].zRot += Mth.sin(t * 0.13f + k) * 0.02f;
            this.legsR[k].zRot -= Mth.sin(t * 0.11f + k) * 0.02f;
        }
        this.body.y += -Math.abs(Mth.sin(pos * 1.4f)) * 0.6f * speed + Mth.sin(t * 0.08f) * 0.15f;
        this.body.zRot += Mth.sin(pos * 1.4f) * 0.04f * speed;
        // Stielaugen schauen umher (und dem Spieler nach)
        float look = Mth.clamp(state.yRot, -50.0f, 50.0f) * Anim.DEG * 0.5f;
        this.eyeL.yRot += look + Mth.sin(t * 0.05f) * 0.25f;
        this.eyeR.yRot += look + Mth.sin(t * 0.05f + 1.7f) * 0.25f;
        this.eyeL.xRot += Mth.sin(t * 0.07f) * 0.1f;
        this.eyeR.xRot += Mth.sin(t * 0.07f + 0.9f) * 0.1f;
        // Scheren: in Ruhe leicht gehoben, beim Laufen im Takt
        this.armL.xRot += -0.1f + Mth.sin(t * 0.06f) * 0.04f + Mth.sin(pos * 1.4f) * 0.06f * speed;
        this.armR.xRot += -0.1f + Mth.sin(t * 0.06f + 1.2f) * 0.04f - Mth.sin(pos * 1.4f) * 0.06f * speed;
        this.fingerL.yRot += 0.1f + Mth.sin(t * 0.04f) * 0.05f;
        this.fingerR.yRot -= 0.1f + Mth.sin(t * 0.045f) * 0.05f;
        // Klappern: beide Scheren schnappen schnell hintereinander
        float clack = Anim.seconds(state.special, t);
        if (clack >= 0.0f && clack < 1.4f) {
            float env = Anim.bump(clack, 0.15f, 1.0f, 1.4f);
            float snap = Math.max(0.0f, Mth.sin(clack * 28.0f));
            this.armL.xRot -= env * 0.5f;
            this.armR.xRot -= env * 0.5f;
            this.fingerL.yRot += env * snap * 0.5f;
            this.fingerR.yRot -= env * Math.max(0.0f, Mth.sin(clack * 28.0f + 1.6f)) * 0.5f;
        }
        // Kneifen: die große Schere stößt vor, öffnet sich und schnappt zu
        float pinch = Anim.seconds(state.attack, t);
        if (pinch >= 0.0f && pinch < 0.6f) {
            float reach = Anim.bump(pinch, 0.12f, 0.25f, 0.55f);
            float shut = Anim.bump(pinch - 0.18f, 0.05f, 0.15f, 0.35f);
            this.armL.yRot -= reach * 0.5f;
            this.armL.xRot -= reach * 0.4f;
            this.fingerL.yRot += reach * 0.6f - shut * 0.7f;
            this.body.xRot -= reach * 0.08f;
        }
    }
}
