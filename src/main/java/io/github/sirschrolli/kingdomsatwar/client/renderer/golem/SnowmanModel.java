package io.github.sirschrolli.kingdomsatwar.client.renderer.golem;

import net.minecraft.client.model.animal.golem.SnowGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SnowGolemRenderState;

/** Schneemann: wie Vanillas Modell; Möhrennase und Strickmütze nur ohne Kürbis auf dem Kopf. */
public class SnowmanModel extends SnowGolemModel {
    private final ModelPart nose;
    private final ModelPart cap;

    public SnowmanModel(ModelPart root) {
        super(root);
        ModelPart head = root.getChild("head");
        this.nose = head.getChild("nose");
        this.cap = head.getChild("cap");
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        boolean pumpkin = state instanceof SnowGolemRenderState s && !s.headBlock.isEmpty();
        this.nose.visible = !pumpkin;
        this.cap.visible = !pumpkin;
    }
}
