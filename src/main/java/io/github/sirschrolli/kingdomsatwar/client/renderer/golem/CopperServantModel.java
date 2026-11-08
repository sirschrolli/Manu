package io.github.sirschrolli.kingdomsatwar.client.renderer.golem;

import net.minecraft.client.model.animal.golem.CopperGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.CopperGolemRenderState;

/** Kupferknecht: Vanillas Modell und Animationen; dazu dreht sich der Aufziehschlüssel auf dem Rücken – beim Gehen schneller. */
public class CopperServantModel extends CopperGolemModel {
    private final ModelPart key;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public CopperServantModel(ModelPart root) {
        super(root);
        ModelPart body = root.getChild("body");
        this.key = body.getChild("key");
        this.rightArm = body.getChild("right_arm");
        this.leftArm = body.getChild("left_arm");
    }

    @Override
    public void setupAnim(CopperGolemRenderState state) {
        super.setupAnim(state);
        // Vanillas Laufschwung ist für die langen Arme des Kupferknechts zu weit (die Fäuste schlagen quer über die
        // Beine): mit leeren Händen schwingen die Arme beim Gehen ruhiger
        float walk = Math.min(1.0f, state.walkAnimationSpeed * 2.5f);
        if (walk > 0.0f && state.rightHandItemState.isEmpty() && state.leftHandItemState.isEmpty()) {
            float calm = 1.0f - 0.45f * walk;
            this.rightArm.xRot *= calm;
            this.leftArm.xRot *= calm;
            this.rightArm.zRot *= calm;
            this.leftArm.zRot *= calm;
        }
        this.key.zRot = state.ageInTicks * (0.04f + Math.min(1.0f, state.walkAnimationSpeed) * 0.22f);
    }
}
