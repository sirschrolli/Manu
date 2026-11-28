package io.github.sirschrolli.kingdomsatwar.client.model.sea;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.sea.SeaRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Anglerfisch: schwebt fast reglos, nur die Brustflossen fächeln und das Maul atmet träge; der Köder an der Angel
 * pendelt sacht und lockend hin und her. Beim Zuschnappen reißt er das riesige Maul auf, die Angel schnellt zurück, und
 * das Maul schlägt zu.
 */
public class AnglerfishModel extends EntityModel<SeaRenderState> {
    private final ModelPart body;
    private final ModelPart jaw;
    private final ModelPart rod;
    private final ModelPart rodTip;
    private final ModelPart lure;
    private final ModelPart tail;
    private final ModelPart tailFin;
    private final ModelPart pectoralL;
    private final ModelPart pectoralR;
    private final ModelPart dorsal;

    public AnglerfishModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.jaw = this.body.getChild("jaw");
        this.rod = this.body.getChild("rod");
        this.rodTip = this.rod.getChild("rod_tip");
        this.lure = this.rodTip.getChild("lure");
        this.tail = this.body.getChild("tail");
        this.tailFin = this.tail.getChild("tail_fin");
        this.pectoralL = this.body.getChild("left_pectoral");
        this.pectoralR = this.body.getChild("right_pectoral");
        this.dorsal = this.body.getChild("dorsal");
    }

    /** Für den leuchtenden Hof um den Köder: die Kette der Knochen bis zum Köder. */
    public ModelPart[] lureChain() {
        return new ModelPart[] {this.root(), this.body, this.rod, this.rodTip, this.lure};
    }

    @Override
    public void setupAnim(SeaRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float s = state.swim;
        if (!state.inWater) {
            this.body.zRot += Mth.HALF_PI;
            this.body.y += 3.0f;
            float flop = Mth.sin(t * 0.8f);
            this.tail.yRot += flop * 0.5f;
            this.jaw.xRot += 0.3f + Math.max(0.0f, flop) * 0.3f;
            return;
        }
        this.body.xRot += Mth.clamp(state.xRot, -40.0f, 40.0f) * Anim.DEG;
        this.body.y += Mth.sin(t * 0.06f) * 0.6f;
        this.tail.yRot += Mth.sin(s) * 0.22f;
        this.tailFin.yRot += Mth.sin(s - 0.9f) * 0.32f;
        this.pectoralL.zRot += Mth.sin(t * 0.21f) * 0.25f;
        this.pectoralR.zRot -= Mth.sin(t * 0.21f + 0.5f) * 0.25f;
        this.dorsal.xRot += Mth.sin(t * 0.07f) * 0.06f;
        // das Maul atmet träge
        this.jaw.xRot += 0.08f + (Mth.sin(t * 0.045f) * 0.5f + 0.5f) * 0.12f;
        // der Köder pendelt lockend
        this.rod.xRot += Mth.sin(t * 0.05f) * 0.06f;
        this.rod.zRot += Mth.sin(t * 0.037f) * 0.1f;
        this.rodTip.xRot += Mth.sin(t * 0.05f - 0.6f) * 0.12f;
        this.lure.xRot += Mth.sin(t * 0.08f) * 0.3f;
        this.lure.zRot += Mth.sin(t * 0.063f + 0.4f) * 0.3f;
        float snap = Anim.seconds(state.attack, t);
        if (snap >= 0.0f && snap < 0.9f) {
            float open = Anim.bump(snap, 0.1f, 0.2f, 0.3f);
            float shut = Anim.bump(snap - 0.26f, 0.04f, 0.2f, 0.6f);
            this.jaw.xRot += open * 1.1f - shut * 0.15f;
            this.body.xRot -= open * 0.25f;
            this.rod.xRot -= open * 0.45f;
            this.rodTip.xRot -= open * 0.4f;
            this.tail.yRot += Mth.sin(snap * 30.0f) * open * 0.4f;
        }
    }
}
