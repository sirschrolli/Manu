package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Pilzling: tapst mit kurzen Schritten und wiegt dabei den Hut, die Ärmchen schlenkern. Duckt er sich, sinkt der Stiel
 * zusammen, der Hut kommt bis zum Boden herab und die Ärmchen verschwinden darunter.
 */
public class MushroomlingModel extends EntityModel<FaeRenderState> {
    private final ModelPart body;
    private final ModelPart cap;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart leftArm;
    private final ModelPart rightArm;

    public MushroomlingModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.cap = this.body.getChild("cap");
        this.leftLeg = this.body.getChild("left_leg");
        this.rightLeg = this.body.getChild("right_leg");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
    }

    @Override
    public void setupAnim(FaeRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed * 1.4f, 1.0f);
        float hide = state.hide;
        this.leftLeg.xRot += Anim.leg(pos * 1.6f, speed, 0.0f, 0.9f);
        this.rightLeg.xRot += Anim.leg(pos * 1.6f, speed, Mth.PI, 0.9f);
        // Watscheln: der ganze Pilz kippt mit jedem Schritt etwas zur Seite, der Hut schwingt nach
        float waddle = Mth.sin(pos * 1.6f * 0.6662f) * 0.16f * speed;
        this.body.zRot += waddle;
        this.cap.zRot -= waddle * 0.6f + Mth.sin(t * 0.07f) * 0.03f;
        this.cap.xRot += Mth.sin(t * 0.05f) * 0.025f;
        this.body.y += Math.abs(Mth.cos(pos * 1.6f * 0.6662f)) * -0.6f * speed;
        float swing = Mth.sin(t * 0.12f) * 0.1f;
        this.leftArm.zRot += -0.25f - swing - Anim.leg(pos * 1.6f, speed, 0.0f, 0.4f);
        this.rightArm.zRot += 0.25f + swing + Anim.leg(pos * 1.6f, speed, Mth.PI, 0.4f);
        this.leftArm.xRot += Anim.leg(pos * 1.6f, speed, Mth.PI, 0.7f);
        this.rightArm.xRot += Anim.leg(pos * 1.6f, speed, 0.0f, 0.7f);
        // Kopf (Hut) folgt ein wenig dem Blick
        this.cap.yRot += Mth.clamp(state.yRot, -30.0f, 30.0f) * Anim.DEG * 0.4f;
        if (hide > 0.0f) {
            this.body.y += 3.2f * hide;
            this.cap.y += 1.4f * hide;
            this.body.zRot *= 1.0f - hide;
            this.leftArm.visible = hide < 0.6f;
            this.rightArm.visible = hide < 0.6f;
            this.leftLeg.xRot = Mth.lerp(hide, this.leftLeg.xRot, -1.2f);
            this.rightLeg.xRot = Mth.lerp(hide, this.rightLeg.xRot, -1.2f);
        } else {
            this.leftArm.visible = true;
            this.rightArm.visible = true;
        }
    }
}
