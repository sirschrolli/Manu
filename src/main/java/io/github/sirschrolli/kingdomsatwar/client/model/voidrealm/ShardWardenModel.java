package io.github.sirschrolli.kingdomsatwar.client.model.voidrealm;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm.VoidCreatureRenderState;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShardWardenEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Splitterwächter: schwerer, stampfender Gang; ruhend gesenkter Kopf, hängende Arme und am Boden liegende Trümmer;
 * beim Erwachen richtet er sich auf, die Trümmer steigen und kreisen. Dazu Faustschlag, Erdstoß mit beiden Fäusten und
 * die Splittersalve aus der erhobenen Hand. Das Herz in der Brust dreht sich unablässig.
 */
public class ShardWardenModel extends EntityModel<VoidCreatureRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart core;
    private final ModelPart coreInner;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart orbit;
    private final ModelPart[] fragments = new ModelPart[3];

    public ShardWardenModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.core = this.body.getChild("core");
        this.coreInner = this.core.getChild("core_inner");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.orbit = root.getChild("orbit");
        for (int k = 0; k < 3; k++) {
            this.fragments[k] = this.orbit.getChild("fragment_" + k);
        }
    }

    @Override
    public void setupAnim(VoidCreatureRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float awake = state.open;
        float sleep = 1.0f - awake;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);

        // schwerer, stampfender Gang
        float stride = pos * 0.5f;
        this.rightLeg.xRot += Mth.cos(stride) * 0.7f * speed;
        this.leftLeg.xRot += Mth.cos(stride + Mth.PI) * 0.7f * speed;
        this.rightArm.xRot += Mth.cos(stride + Mth.PI) * 0.35f * speed;
        this.leftArm.xRot += Mth.cos(stride) * 0.35f * speed;
        this.body.zRot += Mth.cos(stride) * 0.06f * speed;
        this.body.y += Math.abs(Mth.sin(stride)) * 1.2f * speed;
        // Atmen (wach) – ruhend steht er still wie eine Statue
        this.body.y += Mth.sin(t * 0.06f) * 0.35f * awake;
        this.rightArm.zRot += 0.08f + Mth.sin(t * 0.06f) * 0.03f * awake;
        this.leftArm.zRot -= 0.08f + Mth.sin(t * 0.06f) * 0.03f * awake;
        this.rightForearm.xRot -= 0.18f * awake;
        this.leftForearm.xRot -= 0.18f * awake;

        // Blick
        this.head.yRot += Mth.clamp(state.yRot, -50.0f, 50.0f) * DEG * awake;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * DEG * 0.6f * awake;
        // ruhend: Kopf gesenkt, Rumpf leicht vornüber, Arme schwer herabhängend
        this.head.xRot += 0.55f * sleep;
        this.body.xRot += 0.16f * sleep;
        this.rightArm.xRot -= 0.1f * sleep;
        this.leftArm.xRot -= 0.1f * sleep;

        // das Herz dreht sich und schwebt auf und ab
        this.core.yRot += t * (0.02f + 0.06f * awake);
        this.core.xRot += Mth.sin(t * 0.04f) * 0.3f;
        this.coreInner.yRot -= t * 0.09f * (0.3f + awake);
        this.core.y += Mth.sin(t * 0.1f) * 0.4f * awake;

        // Trümmer: wach kreisen sie um ihn, ruhend liegen sie auf dem Boden
        this.orbit.yRot += t * 0.018f * awake + 0.4f;
        for (int k = 0; k < 3; k++) {
            ModelPart f = this.fragments[k];
            f.y += Mth.sin(t * 0.07f + k * 2.1f) * 1.4f * awake + sleep * (27.8f - f.y);
            f.xRot += t * 0.01f * (k + 1) * awake;
            f.yRot += t * 0.013f * (2 - k) * awake;
        }

        this.awaken(state, t);
        this.punch(state, t);
        this.slam(state, t);
        this.volley(state, t);
    }

    /** Erwachen: er richtet sich auf, breitet die Arme und wirft den Kopf in den Nacken. */
    private void awaken(VoidCreatureRenderState state, float t) {
        float s = Anim.seconds(state.awaken, t);
        if (s < 0.0f || s > ShardWardenEntity.AWAKEN_TICKS / 20.0f) {
            return;
        }
        float rise = Anim.bump(s, 0.5f, 1.0f, ShardWardenEntity.AWAKEN_TICKS / 20.0f);
        this.rightArm.zRot += 0.7f * rise;
        this.leftArm.zRot -= 0.7f * rise;
        this.rightArm.xRot -= 0.5f * rise;
        this.leftArm.xRot -= 0.5f * rise;
        this.head.xRot -= 0.45f * rise;
        this.body.xRot -= 0.15f * rise;
    }

    /** Faustschlag: der rechte Arm holt aus und stößt vor. */
    private void punch(VoidCreatureRenderState state, float t) {
        float s = Anim.seconds(state.attack, t);
        if (s < 0.0f || s > 0.6f) {
            return;
        }
        float back = Anim.bump(s, 0.12f, 0.16f, 0.26f);
        float strike = Anim.bump(s - 0.16f, 0.07f, 0.14f, 0.42f);
        this.rightArm.xRot += 0.8f * back - 1.7f * strike;
        this.rightForearm.xRot -= 0.6f * back - 0.3f * strike;
        this.body.yRot += 0.3f * back - 0.25f * strike;
    }

    /** Erdstoß: beide Fäuste über den Kopf, dann mit vorgebeugtem Rumpf auf den Boden. */
    private void slam(VoidCreatureRenderState state, float t) {
        float s = Anim.seconds(state.slam, t);
        float end = ShardWardenEntity.SLAM_TICKS / 20.0f;
        if (s < 0.0f || s > end) {
            return;
        }
        float impact = ShardWardenEntity.SLAM_IMPACT / 20.0f;
        float raise = Anim.bump(s, 0.45f, impact - 0.12f, impact);
        float down = Anim.bump(s - (impact - 0.12f), 0.1f, 0.35f, end - impact + 0.12f);
        for (ModelPart arm : new ModelPart[] {this.rightArm, this.leftArm}) {
            arm.xRot += -2.7f * raise - 0.9f * down;
        }
        this.rightArm.zRot -= 0.25f * raise;
        this.leftArm.zRot += 0.25f * raise;
        this.rightForearm.xRot -= 0.5f * raise;
        this.leftForearm.xRot -= 0.5f * raise;
        this.body.xRot += -0.22f * raise + 0.5f * down;
        this.head.xRot += -0.3f * raise + 0.2f * down;
        this.body.y += 2.0f * down;
    }

    /** Splittersalve: die rechte Hand hebt sich, die Kristalle laden sich auf, dann drei schnelle Würfe. */
    private void volley(VoidCreatureRenderState state, float t) {
        float s = Anim.seconds(state.volley, t);
        float end = ShardWardenEntity.VOLLEY_TICKS / 20.0f;
        if (s < 0.0f || s > end) {
            return;
        }
        float raise = Anim.bump(s, 0.7f, 1.6f, end);
        this.rightArm.xRot -= 2.4f * raise;
        this.rightArm.zRot -= 0.35f * raise;
        this.rightForearm.xRot -= 0.4f * raise;
        this.leftArm.zRot += 0.3f * raise;
        this.body.yRot -= 0.2f * raise;
        for (float shot : new float[] {1.0f, 1.25f, 1.5f}) {
            float flick = Anim.bump(s - shot, 0.04f, 0.06f, 0.18f);
            this.rightArm.xRot += 0.9f * flick;
            this.body.yRot += 0.12f * flick;
        }
    }
}
