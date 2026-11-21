package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Chorusschlund: Der Stamm wiegt sich langsam, die Seitenäste schwanken mit, die Ranken rollen sich ein und aus. Wacht er
 * auf, blüht die Knospe auf: die vier Blütenblätter klappen weit auseinander und zittern, innen glüht der Schlund. Beim
 * Biss reißt er sie noch weiter auf, schnellt vor und lässt sie zuschnappen; beim Rankenhieb holt eine Ranke aus und
 * peitscht nach vorn (die Glieder folgen nacheinander wie eine Peitsche); beim Spucken bäumt sich die Blüte zurück und
 * stößt dann vor.
 */
public class ChorusMawModel extends EntityModel<ChorusMawRenderState> {
    private final ModelPart stalk2;
    private final ModelPart stalk3;
    private final ModelPart head;
    private final ModelPart petalLeft;
    private final ModelPart petalRight;
    private final ModelPart petalFront;
    private final ModelPart petalBack;
    private final ModelPart branchLeft;
    private final ModelPart branchRight;
    private final ModelPart vineLeft;
    private final ModelPart vineLeftMid;
    private final ModelPart vineLeftTip;
    private final ModelPart vineRight;
    private final ModelPart vineRightMid;
    private final ModelPart vineRightTip;

    public ChorusMawModel(ModelPart root) {
        super(root);
        ModelPart stalk1 = root.getChild("stalk1");
        this.stalk2 = stalk1.getChild("stalk2");
        this.stalk3 = this.stalk2.getChild("stalk3");
        this.head = this.stalk3.getChild("head");
        this.petalLeft = this.head.getChild("petal_left");
        this.petalRight = this.head.getChild("petal_right");
        this.petalFront = this.head.getChild("petal_front");
        this.petalBack = this.head.getChild("petal_back");
        this.branchLeft = stalk1.getChild("branch_left");
        this.branchRight = this.stalk2.getChild("branch_right");
        this.vineLeft = this.stalk3.getChild("vine_left");
        this.vineLeftMid = this.vineLeft.getChild("vine_left_mid");
        this.vineLeftTip = this.vineLeftMid.getChild("vine_left_tip");
        this.vineRight = this.stalk3.getChild("vine_right");
        this.vineRightMid = this.vineRight.getChild("vine_right_mid");
        this.vineRightTip = this.vineRightMid.getChild("vine_right_tip");
    }

    @Override
    public void setupAnim(ChorusMawRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float open = state.open;

        // ------------------------------------------------------------ Wiegen
        this.stalk2.zRot += Mth.sin(t * 0.03f) * 0.04f;
        this.stalk2.xRot += Mth.sin(t * 0.025f + 1.0f) * 0.03f;
        this.stalk3.zRot += Mth.sin(t * 0.03f - 0.7f) * 0.05f;
        this.stalk3.xRot += Mth.sin(t * 0.025f + 0.3f) * 0.04f;
        this.branchLeft.zRot += Mth.sin(t * 0.04f) * 0.05f;
        this.branchRight.zRot += Mth.sin(t * 0.04f + 2.0f) * 0.05f;
        // die Ranken rollen sich langsam ein und aus
        float curl = Mth.sin(t * 0.05f) * 0.12f;
        this.vineLeftMid.zRot += curl;
        this.vineLeftTip.zRot += curl * 1.5f;
        this.vineRightMid.zRot -= Mth.sin(t * 0.05f + 1.3f) * 0.12f;
        this.vineRightTip.zRot -= Mth.sin(t * 0.05f + 1.3f) * 0.18f;
        this.head.yRot += Mth.clamp(state.yRot, -35.0f, 35.0f) * Anim.DEG * 0.6f;

        // ------------------------------------------------------------ die Blüte geht auf
        float quiver = Mth.sin(t * 0.9f) * 0.04f * open;
        float bloom = 1.25f * open;
        // Biss: noch weiter auf, dann schnappen die Blätter zu (und ein wenig darüber hinaus)
        float bite = Anim.seconds(state.bite, t);
        if (bite >= 0.0f && bite < 0.6f) {
            float gape = Anim.bump(bite, 0.12f, 0.18f, 0.25f);
            float snap = Anim.bump(bite - 0.22f, 0.03f, 0.15f, 0.35f);
            bloom += 0.3f * gape - (bloom + 0.12f) * snap;
            this.head.xRot += 0.2f * gape + 0.25f * snap;
            this.head.z += -1.5f * snap;
            this.stalk3.xRot += 0.15f * snap;
        }
        // Spucken: weit auf, zurückbäumen, vorstoßen
        float spit = Anim.seconds(state.spit, t);
        if (spit >= 0.0f && spit < 0.8f) {
            float back = Anim.bump(spit, 0.3f, 0.36f, 0.42f);
            float thrust = Anim.bump(spit - 0.38f, 0.05f, 0.14f, 0.35f);
            bloom += 0.25f * thrust;
            this.stalk3.xRot += -0.3f * back + 0.35f * thrust;
            this.head.xRot += -0.35f * back + 0.4f * thrust;
        }
        this.petalFront.xRot += bloom + quiver;
        this.petalBack.xRot -= bloom + Mth.sin(t * 0.9f + 1.1f) * 0.04f * open;
        this.petalLeft.zRot += bloom + Mth.sin(t * 0.9f + 2.3f) * 0.04f * open;
        this.petalRight.zRot -= bloom + Mth.sin(t * 0.9f + 3.4f) * 0.04f * open;
        this.head.xRot += 0.08f * open;
        // offen recken sich die Ranken der Beute entgegen
        this.vineLeft.zRot += 0.25f * open;
        this.vineRight.zRot -= 0.25f * open;
        this.vineLeft.yRot += 0.35f * open;
        this.vineRight.yRot -= 0.35f * open;

        // ------------------------------------------------------------ Rankenhiebe
        this.lash(Anim.seconds(state.lashLeft, t), this.vineLeft, this.vineLeftMid, this.vineLeftTip, 1.0f);
        this.lash(Anim.seconds(state.lashRight, t), this.vineRight, this.vineRightMid, this.vineRightTip, -1.0f);
    }

    /** Ein Rankenhieb: ausholen (nach hinten oben), dann peitschen die Glieder nacheinander nach vorn. */
    private void lash(float s, ModelPart vine, ModelPart mid, ModelPart tip, float side) {
        if (s < 0.0f || s >= 0.7f) {
            return;
        }
        float wind = Anim.bump(s, 0.18f, 0.24f, 0.3f);
        float whip = Anim.bump(s - 0.26f, 0.05f, 0.14f, 0.4f);
        float whip2 = Anim.bump(s - 0.3f, 0.05f, 0.14f, 0.38f);
        vine.yRot += side * (-0.7f * wind + 1.35f * whip);
        vine.zRot += side * (-0.5f * wind + 0.35f * whip);
        mid.yRot += side * (-0.3f * wind + 0.6f * whip2);
        mid.zRot += side * (-0.2f * wind - 0.5f * whip2);
        tip.yRot += side * 0.7f * whip2;
        tip.zRot += side * -0.6f * whip2;
    }
}
