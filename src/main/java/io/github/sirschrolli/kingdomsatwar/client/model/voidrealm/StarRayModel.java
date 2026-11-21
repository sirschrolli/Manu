package io.github.sirschrolli.kingdomsatwar.client.model.voidrealm;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm.VoidCreatureRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Sternenrochen: die Schwingen schlagen in einer Welle von innen nach außen (jedes Glied etwas später und weiter), der
 * Leib hebt und senkt sich im Takt, der Peitschenschwanz schlängelt nach. In Kurven neigt er sich, beim Steigen hebt er
 * den Kopf.
 */
public class StarRayModel extends EntityModel<VoidCreatureRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart[] wingsL = new ModelPart[3];
    private final ModelPart[] wingsR = new ModelPart[3];
    private final ModelPart pelvicL;
    private final ModelPart pelvicR;
    private final ModelPart[] tail = new ModelPart[3];
    private final ModelPart cephalicL;
    private final ModelPart cephalicR;

    public StarRayModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.cephalicL = this.head.getChild("left_cephalic");
        this.cephalicR = this.head.getChild("right_cephalic");
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
        this.tail[0] = this.body.getChild("tail");
        this.tail[1] = this.tail[0].getChild("tail_mid");
        this.tail[2] = this.tail[1].getChild("tail_tip");
    }

    @Override
    public void setupAnim(VoidCreatureRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float f = state.flap;
        // Flügelschlag als Welle: inneres Glied zuerst, Spitze zuletzt und am weitesten
        float[] amp = {0.32f, 0.42f, 0.5f};
        float[] lag = {0.0f, 0.65f, 1.3f};
        for (int i = 0; i < 3; i++) {
            float a = Mth.sin(f - lag[i]) * amp[i] + (i == 0 ? 0.04f : 0.02f);
            this.wingsL[i].zRot += a;
            this.wingsR[i].zRot -= a;
            // die Hinterkante schwingt etwas nach
            float twist = Mth.cos(f - lag[i]) * 0.08f * (i + 1);
            this.wingsL[i].xRot += twist;
            this.wingsR[i].xRot += twist;
        }
        this.body.y += -Mth.sin(f) * 0.7f;
        this.body.xRot += Mth.clamp(-state.climb * 2.2f, -0.45f, 0.45f) + Mth.cos(f) * 0.03f;
        this.body.zRot += state.bank * Anim.DEG;
        this.head.xRot += Mth.sin(f - 0.4f) * 0.04f;
        this.cephalicL.xRot += Mth.sin(t * 0.09f) * 0.12f;
        this.cephalicR.xRot += Mth.sin(t * 0.09f + 0.5f) * 0.12f;
        this.pelvicL.zRot += Mth.sin(f - 0.9f) * 0.15f;
        this.pelvicR.zRot -= Mth.sin(f - 0.9f) * 0.15f;
        // Peitschenschwanz: schlängelt nach, in Kurven zur Außenseite
        for (int i = 0; i < 3; i++) {
            float w = t * 0.09f - i * 0.8f;
            this.tail[i].yRot += Mth.sin(w) * (0.1f + i * 0.08f) - state.bank * Anim.DEG * 0.3f;
            this.tail[i].xRot += Mth.sin(f * 0.5f - i * 0.9f) * (0.05f + i * 0.04f) + Mth.clamp(state.climb, -0.3f, 0.3f) * 0.4f;
        }
    }
}
