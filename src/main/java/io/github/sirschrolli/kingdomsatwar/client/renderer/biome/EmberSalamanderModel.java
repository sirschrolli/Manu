package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Glutsalamander: läuft im Kreuzgang mit gespreizten Beinen, der Leib schlängelt dabei im S, der Schwanz zieht die Welle
 * nach; still pendelt nur die Schwanzspitze. Beim Sonnen liegt er flach, die Beine weit gespreizt, den Kopf erhoben.
 */
public class EmberSalamanderModel extends EntityModel<EmberSalamanderRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart body2;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart frontL;
    private final ModelPart frontLFoot;
    private final ModelPart frontR;
    private final ModelPart frontRFoot;
    private final ModelPart hindL;
    private final ModelPart hindLFoot;
    private final ModelPart hindR;
    private final ModelPart hindRFoot;

    public EmberSalamanderModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.body2 = this.body.getChild("body2");
        this.tail1 = this.body2.getChild("tail1");
        this.tail2 = this.tail1.getChild("tail2");
        this.tail3 = this.tail2.getChild("tail3");
        this.frontL = this.body.getChild("left_front_leg");
        this.frontLFoot = this.frontL.getChild("left_front_foot");
        this.frontR = this.body.getChild("right_front_leg");
        this.frontRFoot = this.frontR.getChild("right_front_foot");
        this.hindL = this.body2.getChild("left_hind_leg");
        this.hindLFoot = this.hindL.getChild("left_hind_foot");
        this.hindR = this.body2.getChild("right_hind_leg");
        this.hindRFoot = this.hindR.getChild("right_hind_foot");
    }

    @Override
    public void setupAnim(EmberSalamanderRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks + state.offset;
        float bask = state.bask;
        float speed = Math.min(1.0f, state.walkAnimationSpeed * 1.6f) * (1.0f - bask);
        float phase = state.walkAnimationPos * 1.3f;

        // ------------------------------------------------------------ Kreuzgang: links vorn mit rechts hinten
        float a = Mth.sin(phase);
        // links: positives yRot schwingt nach vorn, rechts negatives
        this.frontL.yRot += a * 0.55f * speed;
        this.hindR.yRot -= a * 0.55f * speed;
        this.frontR.yRot += a * 0.55f * speed;
        this.hindL.yRot -= a * 0.55f * speed;
        // das vorschwingende Bein hebt ab
        float liftA = Math.max(0.0f, Mth.cos(phase)) * 0.45f * speed;
        float liftB = Math.max(0.0f, Mth.cos(phase + Mth.PI)) * 0.45f * speed;
        this.frontL.zRot -= liftA;
        this.hindR.zRot += liftA;
        this.frontR.zRot += liftB;
        this.hindL.zRot -= liftB;
        // der Leib schlängelt, der Schwanz zieht die Welle nach
        this.body.yRot += a * 0.22f * speed;
        this.body2.yRot += -a * 0.36f * speed;
        this.head.yRot += -a * 0.2f * speed;
        float idle = (1.0f - speed) * 0.12f;
        this.tail1.yRot += Mth.sin(phase - 0.8f) * 0.32f * speed + Mth.sin(t * 0.06f) * idle;
        this.tail2.yRot += Mth.sin(phase - 1.6f) * 0.38f * speed + Mth.sin(t * 0.06f - 0.8f) * idle;
        this.tail3.yRot += Mth.sin(phase - 2.4f) * 0.45f * speed + Mth.sin(t * 0.06f - 1.6f) * idle * 1.6f;

        // ------------------------------------------------------------ Kopf: Blick, ab und zu kurz das Maul auf
        this.head.yRot += Mth.clamp(state.yRot, -50.0f, 50.0f) * DEG * 0.7f;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * DEG * 0.6f;
        float gape = Math.max(0.0f, Mth.sin(t * 0.045f) - 0.92f) / 0.08f;
        this.jaw.xRot += gape * 0.4f;

        // ------------------------------------------------------------ Sonnen: flach, Beine weit gespreizt, Kopf erhoben
        if (bask > 0.0f) {
            this.body.y += 0.55f * bask;
            this.frontL.zRot += 0.35f * bask;
            this.frontR.zRot -= 0.35f * bask;
            this.hindL.zRot += 0.35f * bask;
            this.hindR.zRot -= 0.35f * bask;
            this.frontL.yRot += 0.5f * bask;
            this.frontR.yRot -= 0.5f * bask;
            this.hindL.yRot -= 0.5f * bask;
            this.hindR.yRot += 0.5f * bask;
            this.head.xRot += -0.28f * bask;
            this.tail2.yRot += 0.3f * bask;
            this.tail3.yRot += 0.5f * bask;
            this.body.y += Mth.sin(t * 0.08f) * 0.06f * bask;
        }
    }
}
