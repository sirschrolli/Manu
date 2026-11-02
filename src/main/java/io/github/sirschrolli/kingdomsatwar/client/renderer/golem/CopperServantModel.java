package io.github.sirschrolli.kingdomsatwar.client.renderer.golem;

import net.minecraft.client.model.animal.golem.CopperGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.CopperGolemRenderState;

/** Kupferknecht: Vanillas Modell und Animationen; dazu dreht sich der Aufziehschlüssel auf dem Rücken – beim Gehen schneller. */
public class CopperServantModel extends CopperGolemModel {
    private final ModelPart key;

    public CopperServantModel(ModelPart root) {
        super(root);
        this.key = root.getChild("body").getChild("key");
    }

    @Override
    public void setupAnim(CopperGolemRenderState state) {
        super.setupAnim(state);
        this.key.zRot = state.ageInTicks * (0.04f + Math.min(1.0f, state.walkAnimationSpeed) * 0.22f);
    }
}
