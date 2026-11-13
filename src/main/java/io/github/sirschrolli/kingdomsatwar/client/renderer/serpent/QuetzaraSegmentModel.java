package io.github.sirschrolli.kingdomsatwar.client.renderer.serpent;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Ein Glied von Quetzaras Leib: das Federbüschel auf dem Rücken wippt leicht. */
public class QuetzaraSegmentModel extends EntityModel<QuetzaraRenderState.Segment> {
    private final ModelPart tuft;
    private final ModelPart tuft2;

    public QuetzaraSegmentModel(ModelPart root) {
        super(root);
        ModelPart body = root.getChild("body");
        this.tuft = body.getChild("tuft");
        this.tuft2 = body.getChild("tuft2");
    }

    @Override
    public void setupAnim(QuetzaraRenderState.Segment state) {
        super.setupAnim(state);
        float t = state.ageInTicks + state.index * 5.0f;
        this.tuft.xRot += Mth.sin(t * 0.21f) * 0.08f;
        this.tuft2.xRot += Mth.sin(t * 0.21f + 1.3f) * 0.08f;
    }
}
