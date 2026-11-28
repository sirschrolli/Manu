package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Aschengeist: Er schwebt auf und ab, die zerfetzte Kutte weht in Wellen nach, die dürren Arme treiben locker, die Krallen
 * zucken, die Aschebrocken kreisen. Im Flug neigt er sich vor, die Kutte weht zurück. Beim Seelenfeuer holt die rechte
 * Hand aus und stößt vor; bei der Aschewolke breitet er beide Arme weit aus und fegt sie dann nach vorn; beim Krallenhieb
 * reißt der rechte Arm quer herab.
 */
public class AshWraithModel extends EntityModel<AshWraithRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart hoodTip;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftForearm;
    private final ModelPart rightForearm;
    private final ModelPart skirt;
    private final ModelPart skirtLow;
    private final ModelPart skirtTip;
    private final ModelPart orbit;

    public AshWraithModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.hoodTip = this.head.getChild("hood_tip");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.skirt = this.body.getChild("skirt");
        this.skirtLow = this.skirt.getChild("skirt_low");
        this.skirtTip = this.skirtLow.getChild("skirt_tip");
        this.orbit = root.getChild("orbit");
    }

    @Override
    public void setupAnim(AshWraithRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float drift = state.drift;

        // ------------------------------------------------------------ Schweben
        float bob = Mth.sin(t * 0.08f);
        this.body.y += bob * 1.2f;
        this.orbit.y += bob * 1.2f;
        this.body.xRot += 0.25f * drift + Mth.sin(t * 0.05f) * 0.03f;
        this.body.zRot += Mth.sin(t * 0.04f) * 0.04f;
        this.skirt.xRot += Mth.sin(t * 0.11f) * 0.08f + 0.2f * drift;
        this.skirtLow.xRot += Mth.sin(t * 0.11f - 0.9f) * 0.12f + 0.25f * drift;
        this.skirtTip.xRot += Mth.sin(t * 0.11f - 1.8f) * 0.18f + 0.35f * drift;
        this.skirtLow.zRot += Mth.sin(t * 0.07f - 0.5f) * 0.06f;
        this.skirtTip.zRot += Mth.sin(t * 0.07f - 1.2f) * 0.1f;
        this.leftArm.xRot += Mth.sin(t * 0.06f) * 0.08f + 0.15f * drift;
        this.rightArm.xRot += Mth.sin(t * 0.06f + 1.4f) * 0.08f + 0.15f * drift;
        this.leftForearm.xRot += -0.1f + Mth.sin(t * 0.09f) * 0.08f;
        this.rightForearm.xRot += -0.1f + Mth.sin(t * 0.09f + 2.1f) * 0.08f;
        this.head.yRot += Mth.clamp(state.yRot, -50.0f, 50.0f) * Anim.DEG * 0.8f;
        this.head.xRot += Mth.clamp(state.xRot, -40.0f, 40.0f) * Anim.DEG * 0.6f - 0.15f * drift;
        this.hoodTip.xRot += Mth.sin(t * 0.1f) * 0.1f + 0.3f * drift;
        this.orbit.yRot += t * 0.06f;

        // ------------------------------------------------------------ Seelenfeuer
        float bolt = Anim.seconds(state.bolt, t);
        if (bolt >= 0.0f && bolt < 1.0f) {
            float wind = Anim.bump(bolt, 0.35f, 0.42f, 0.5f);
            float thrust = Anim.bump(bolt - 0.45f, 0.06f, 0.18f, 0.5f);
            this.rightArm.xRot += -1.2f * wind - 1.5f * thrust;
            this.rightArm.zRot += 0.6f * wind - 0.2f * thrust;
            this.rightForearm.xRot += -0.8f * wind + 0.5f * thrust;
            this.body.yRot += 0.3f * wind - 0.25f * thrust;
            this.head.xRot += -0.15f * wind;
        }

        // ------------------------------------------------------------ Aschewolke
        float cloud = Anim.seconds(state.cloud, t);
        if (cloud >= 0.0f && cloud < 1.5f) {
            float spread = Anim.bump(cloud, 0.5f, 0.65f, 0.75f);
            float sweep = Anim.bump(cloud - 0.66f, 0.12f, 0.3f, 0.75f);
            this.leftArm.xRot += -2.2f * spread - 1.2f * sweep;
            this.rightArm.xRot += -2.2f * spread - 1.2f * sweep;
            this.leftArm.zRot += -0.7f * spread + 0.3f * sweep;
            this.rightArm.zRot += 0.7f * spread - 0.3f * sweep;
            this.body.xRot += -0.3f * spread + 0.35f * sweep;
            this.head.xRot += -0.4f * spread + 0.2f * sweep;
            this.skirt.xRot += -0.2f * spread;
        }

        // ------------------------------------------------------------ Krallenhieb
        float slash = Anim.seconds(state.slash, t);
        if (slash >= 0.0f && slash < 0.55f) {
            float raise = Anim.bump(slash, 0.12f, 0.15f, 0.22f);
            float strike = Anim.bump(slash - 0.14f, 0.06f, 0.12f, 0.38f);
            this.rightArm.xRot += -2.3f * raise + 1.0f * strike;
            this.rightArm.zRot += 0.5f * raise - 0.6f * strike;
            this.body.yRot += -0.3f * raise + 0.4f * strike;
        }
    }
}
