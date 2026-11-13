package io.github.sirschrolli.kingdomsatwar.client.model.sea;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.sea.SeaRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Hai: schwimmt mit einer S-Welle, die vom Kopf zur Schwanzflosse immer stärker wird (der Kopf pendelt leicht gegen);
 * die Brustflossen steuern beim Steigen und Sinken. Beim Biss reißt er das Maul weit auf, hebt die Schnauze und schlägt
 * es zu. An Land zappelt er hilflos.
 */
public class SharkModel extends EntityModel<SeaRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart tail;
    private final ModelPart tailEnd;
    private final ModelPart caudal;
    private final ModelPart pectoralL;
    private final ModelPart pectoralR;
    private final ModelPart dorsal;

    public SharkModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.tail = this.body.getChild("tail");
        this.tailEnd = this.tail.getChild("tail_end");
        this.caudal = this.tailEnd.getChild("caudal");
        this.pectoralL = this.body.getChild("left_pectoral");
        this.pectoralR = this.body.getChild("right_pectoral");
        this.dorsal = this.body.getChild("dorsal");
    }

    @Override
    public void setupAnim(SeaRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float s = state.swim;
        float pitch = Mth.clamp(state.xRot, -50.0f, 50.0f) * Anim.DEG;
        if (!state.inWater) {
            // gestrandet: auf der Seite, wildes Zappeln
            this.body.zRot += Mth.HALF_PI;
            this.body.y += 3.0f;
            float flop = Mth.sin(t * 0.9f);
            this.tail.yRot += flop * 0.5f;
            this.tailEnd.yRot += flop * 0.6f;
            this.head.yRot -= flop * 0.15f;
            this.jaw.xRot += 0.25f + Math.max(0.0f, flop) * 0.25f;
            return;
        }
        this.body.xRot += pitch;
        // S-Welle: Kopf leicht gegen, Schwanz kräftig, Schwanzflosse am stärksten (jeweils etwas später)
        this.body.yRot += Mth.sin(s) * 0.04f;
        this.head.yRot -= Mth.sin(s + 0.3f) * 0.08f;
        this.tail.yRot += Mth.sin(s - 0.9f) * 0.26f;
        this.tailEnd.yRot += Mth.sin(s - 1.7f) * 0.36f;
        this.caudal.yRot += Mth.sin(s - 2.4f) * 0.42f;
        // Brustflossen: Tiefenruder
        this.pectoralL.xRot -= pitch * 0.6f;
        this.pectoralR.xRot -= pitch * 0.6f;
        this.pectoralL.zRot += Mth.sin(t * 0.07f) * 0.05f;
        this.pectoralR.zRot -= Mth.sin(t * 0.07f) * 0.05f;
        this.dorsal.zRot += Mth.sin(s - 0.5f) * 0.03f;
        // ruhiges Atmen: das Maul öffnet sich einen Spalt
        this.jaw.xRot += 0.04f + Mth.sin(t * 0.05f) * 0.03f;
        float bite = Anim.seconds(state.attack, t);
        if (bite >= 0.0f && bite < 0.7f) {
            float open = Anim.bump(bite, 0.12f, 0.22f, 0.32f);
            float snap = Anim.bump(bite - 0.28f, 0.05f, 0.12f, 0.42f);
            this.jaw.xRot += open * 0.95f - snap * 0.05f;
            this.head.xRot -= open * 0.35f - snap * 0.1f;
            this.body.xRot -= open * 0.12f;
            this.head.yRot += snap * Mth.sin(bite * 40.0f) * 0.12f;          // das Schütteln nach dem Biss
        }
    }
}
