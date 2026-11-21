package io.github.sirschrolli.kingdomsatwar.client.renderer.serpent;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Quetzaras Schwanz: der Federbusch wiegt sich hin und her, die Spitze des Schwanzes ringelt sich ein wenig. */
public class QuetzaraTailModel extends EntityModel<QuetzaraRenderState.Segment> {
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart plume;

    public QuetzaraTailModel(ModelPart root) {
        super(root);
        ModelPart tail = root.getChild("tail");
        this.tail2 = tail.getChild("tail2");
        this.tail3 = this.tail2.getChild("tail3");
        this.plume = this.tail3.getChild("plume");
    }

    @Override
    public void setupAnim(QuetzaraRenderState.Segment state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        this.tail2.yRot += Mth.sin(t * 0.11f) * 0.12f;
        this.tail3.yRot += Mth.sin(t * 0.11f - 0.8f) * 0.18f;
        this.plume.yRot += Mth.sin(t * 0.11f - 1.6f) * 0.22f;
        this.plume.xRot += Mth.sin(t * 0.08f) * 0.06f;
    }
}
