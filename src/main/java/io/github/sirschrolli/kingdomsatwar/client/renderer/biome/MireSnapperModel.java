package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Moorschnapper: schwerfälliges Kriechen mit rudernden Beinen, der Panzer schaukelt; im Stand pendelt der Schwanz, der
 * Kopf hebt und senkt sich langsam. Beim Fauchen reckt er den Kopf und reißt das Maul auf; beim Zuschnappen schnellt der
 * Hals vor und die Kiefer schlagen zu. In den Panzer zurückgezogen liegt er flach, Kopf, Beine und Schwanz eingezogen.
 */
public class MireSnapperModel extends EntityModel<MireSnapperRenderState> {
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart frontL;
    private final ModelPart frontR;
    private final ModelPart hindL;
    private final ModelPart hindR;

    public MireSnapperModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.tail1 = this.body.getChild("tail1");
        this.tail2 = this.tail1.getChild("tail2");
        this.frontL = root.getChild("left_front_leg");
        this.frontR = root.getChild("right_front_leg");
        this.hindL = root.getChild("left_hind_leg");
        this.hindR = root.getChild("right_hind_leg");
    }

    @Override
    public void setupAnim(MireSnapperRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks + state.offset;
        float w = state.withdraw;
        float out = 1.0f - w;
        float speed = Math.min(1.0f, state.walkAnimationSpeed * 2.0f) * out;
        float phase = state.walkAnimationPos * 1.1f;

        // ------------------------------------------------------------ Kriechen: Beine rudern im Kreuzgang, der Panzer schaukelt
        float a = Mth.sin(phase);
        // links: positives yRot schwingt nach vorn, rechts negatives; Kreuzgang (links vorn mit rechts hinten)
        this.frontL.yRot += a * 0.5f * speed;
        this.hindR.yRot -= a * 0.5f * speed;
        this.frontR.yRot += a * 0.5f * speed;
        this.hindL.yRot -= a * 0.5f * speed;
        float lift = Math.max(0.0f, Mth.cos(phase)) * 0.3f * speed;
        float lift2 = Math.max(0.0f, -Mth.cos(phase)) * 0.3f * speed;
        this.frontL.zRot -= lift;
        this.hindR.zRot += lift;
        this.frontR.zRot += lift2;
        this.hindL.zRot -= lift2;
        this.body.zRot += a * 0.05f * speed;
        this.body.y += -Math.abs(a) * 0.3f * speed;
        this.neck.xRot += Mth.sin(phase * 2.0f) * 0.05f * speed;

        // ------------------------------------------------------------ Ruhe: Schwanz pendelt, Kopf hebt und senkt sich
        this.tail1.yRot += Mth.sin(t * 0.05f) * 0.15f * out + a * 0.15f * speed;
        this.tail2.yRot += Mth.sin(t * 0.05f - 0.9f) * 0.2f * out + a * 0.2f * speed;
        this.neck.xRot += Mth.sin(t * 0.03f) * 0.04f * out;
        this.head.yRot += Mth.clamp(state.yRot, -60.0f, 60.0f) * DEG * 0.6f * out;
        this.neck.yRot += Mth.clamp(state.yRot, -60.0f, 60.0f) * DEG * 0.3f * out;
        this.head.xRot += Mth.clamp(state.xRot, -25.0f, 25.0f) * DEG * 0.5f * out;
        if (state.submerged) {
            this.body.y += Mth.sin(t * 0.04f) * 0.15f;
        }

        // ------------------------------------------------------------ Fauchen
        float h = Anim.seconds(state.hiss, t - state.offset);
        if (h >= 0.0f && h < 1.4f) {
            float hiss = Anim.bump(h, 0.12f, 0.9f, 1.3f) * out;
            this.neck.xRot += -0.25f * hiss;
            this.neck.z += -0.8f * hiss;
            this.head.xRot += -0.2f * hiss;
            this.jaw.xRot += 0.75f * hiss;
        }

        // ------------------------------------------------------------ Zuschnappen: Hals schnellt vor, Kiefer zu
        float s = Anim.seconds(state.snap, t - state.offset);
        if (s >= 0.0f && s < 0.6f) {
            float lunge = Anim.bump(s, 0.05f, 0.14f, 0.5f) * out;
            float open = s < 0.07f ? s / 0.07f : Math.max(0.0f, 1.0f - (s - 0.07f) / 0.06f);
            this.neck.z += -2.8f * lunge;
            this.head.z += -0.8f * lunge;
            this.head.xRot += 0.15f * lunge;
            this.jaw.xRot += 0.85f * open * out;
            this.body.z += -0.4f * lunge;
        }

        // ------------------------------------------------------------ Im Panzer: Kopf, Beine und Schwanz eingezogen
        if (w > 0.0f) {
            this.body.y += 1.2f * w;
            this.neck.z += 3.4f * w;
            this.neck.xRot += 0.25f * w;
            this.head.z += 1.2f * w;
            this.tail1.z += -2.4f * w;
            this.tail1.yRot += 0.9f * w;
            this.tail2.yRot += 1.2f * w;
            this.frontL.x -= 2.2f * w;
            this.frontR.x += 2.2f * w;
            this.hindL.x -= 2.2f * w;
            this.hindR.x += 2.2f * w;
            this.frontL.y -= 0.6f * w;
            this.frontR.y -= 0.6f * w;
            this.hindL.y -= 0.6f * w;
            this.hindR.y -= 0.6f * w;
        }
    }
}
