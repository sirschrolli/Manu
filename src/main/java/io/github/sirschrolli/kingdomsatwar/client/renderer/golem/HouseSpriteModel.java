package io.github.sirschrolli.kingdomsatwar.client.renderer.golem;

import net.minecraft.client.model.animal.allay.AllayModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AllayRenderState;

/** Wichtel: Vanillas Modell und Bewegungen; je Gestalt Zipfelmütze oder Fliegenpilz-Hut. */
public class HouseSpriteModel extends AllayModel {
    private final ModelPart cone;
    private final ModelPart toadstool;

    public HouseSpriteModel(ModelPart root) {
        super(root);
        ModelPart head = root.getChild("root").getChild("head");
        this.cone = head.getChild("cap_cone");
        this.toadstool = head.getChild("toadstool");
    }

    @Override
    public void setupAnim(AllayRenderState state) {
        super.setupAnim(state);
        boolean toadstool = state instanceof HouseSpriteRenderer.State s && s.look == HouseSpriteRenderer.TOADSTOOL;
        this.cone.visible = !toadstool;
        this.toadstool.visible = toadstool;
    }
}
