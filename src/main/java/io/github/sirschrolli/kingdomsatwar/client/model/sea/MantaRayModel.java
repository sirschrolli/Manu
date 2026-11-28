package io.github.sirschrolli.kingdomsatwar.client.model.sea;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.sea.SeaRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Mantarochen: die Brustflossen schlagen langsam in einer Welle von innen nach außen (jedes Glied etwas später und
 * weiter), die Hinterkante schwingt nach, der Leib hebt und senkt sich im Takt. Er neigt sich in Kurven und steigt mit
 * erhobenem Kopf; beim Sprung stehen die Schwingen weit ausgebreitet, die Kopfflossen rollen sich ein.
 */
public class MantaRayModel extends EntityModel<SeaRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart[] wingsL = new ModelPart[3];
    private final ModelPart[] wingsR = new ModelPart[3];
    private final ModelPart pelvicL;
    private final ModelPart pelvicR;
    private final ModelPart tail;
    private final ModelPart tailTip;
    private final ModelPart cephalicL;
    private final ModelPart cephalicR;
    private final ModelPart cephalicTipL;
    private final ModelPart cephalicTipR;

    public MantaRayModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.cephalicL = this.head.getChild("left_cephalic");
        this.cephalicR = this.head.getChild("right_cephalic");
        this.cephalicTipL = this.cephalicL.getChild("left_cephalic_tip");
        this.cephalicTipR = this.cephalicR.getChild("right_cephalic_tip");
        for (String side : new String[] {"left", "right"}) {
            ModelPart w1 = this.body.getChild(side + "_wing");
            ModelPart w2 = w1.getChild(side + "_wing_mid");
            ModelPart w3 = w2.getChild(side + "_wing_tip");
            ModelPart[] target = side.equals("left") ? this.wingsL : this.wingsR;
            target[0] = w1;
            target[1] = w2;
            target[2] = w3;
        }
        this.pelvicL = this.body.getChild("left_pelvic");
        this.pelvicR = this.body.getChild("right_pelvic");
        this.tail = this.body.getChild("tail");
        this.tailTip = this.tail.getChild("tail_tip");
    }

    @Override
    public void setupAnim(SeaRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float f = state.flap;
        float air = state.inWater ? 0.0f : 1.0f;
        // Flügelschlag als Welle: inneres Glied zuerst, Spitze zuletzt und am weitesten (in der Luft fast still)
        float[] amp = {0.26f, 0.36f, 0.46f};
        float[] lag = {0.0f, 0.7f, 1.4f};
        float beat = 1.0f - air * 0.8f;
        for (int i = 0; i < 3; i++) {
            float a = (Mth.sin(f - lag[i]) * amp[i] + 0.03f) * beat - air * 0.12f * (i + 1);
            this.wingsL[i].zRot += a;
            this.wingsR[i].zRot -= a;
            float twist = Mth.cos(f - lag[i]) * 0.07f * (i + 1) * beat;
            this.wingsL[i].xRot += twist;
            this.wingsR[i].xRot += twist;
        }
        this.body.y += -Mth.sin(f) * 0.8f * beat;
        // Neigung: Steigen/Sinken aus der Blickrichtung des Wesens, in der Kurve zur Seite
        this.body.xRot += Mth.clamp(state.xRot, -55.0f, 55.0f) * Anim.DEG + Mth.cos(f) * 0.03f * beat;
        this.body.zRot += state.bank * Anim.DEG;
        this.head.xRot += Mth.sin(f - 0.4f) * 0.04f;
        // Kopfflossen: im Wasser locker, beim Sprung eingerollt
        float curl = air * 0.9f;
        this.cephalicL.xRot += Mth.sin(t * 0.08f) * 0.1f - curl * 0.4f;
        this.cephalicR.xRot += Mth.sin(t * 0.08f + 0.6f) * 0.1f - curl * 0.4f;
        this.cephalicTipL.xRot -= curl;
        this.cephalicTipR.xRot -= curl;
        this.pelvicL.zRot += Mth.sin(f - 0.9f) * 0.15f;
        this.pelvicR.zRot -= Mth.sin(f - 0.9f) * 0.15f;
        // Peitschenschwanz: schlängelt nach, in Kurven zur Außenseite
        this.tail.yRot += Mth.sin(t * 0.09f) * 0.12f - state.bank * Anim.DEG * 0.3f;
        this.tail.xRot += Mth.sin(f * 0.5f) * 0.06f + Mth.clamp(-state.xRot * Anim.DEG, -0.4f, 0.4f) * 0.3f;
        this.tailTip.yRot += Mth.sin(t * 0.09f - 0.9f) * 0.2f;
        this.tailTip.xRot += Mth.sin(f * 0.5f - 0.9f) * 0.1f;
    }
}
