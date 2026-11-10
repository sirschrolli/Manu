package io.github.sirschrolli.kingdomsatwar.client.renderer.abyss;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Der Schwanz des Weltenfressers: Die Spitze schlägt langsam hin und her, die Kristallflosse folgt. */
public class WorldEaterTailModel extends EntityModel<WorldEaterRenderState.Segment> {
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart fin;

    public WorldEaterTailModel(ModelPart root) {
        super(root);
        ModelPart tail = root.getChild("tail");
        this.tail2 = tail.getChild("tail2");
        this.tail3 = this.tail2.getChild("tail3");
        this.fin = this.tail3.getChild("fin");
    }

    @Override
    public void setupAnim(WorldEaterRenderState.Segment state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        this.tail2.yRot += Mth.sin(t * 0.11f) * 0.22f;
        this.tail3.yRot += Mth.sin(t * 0.11f - 0.9f) * 0.32f;
        this.fin.yRot += Mth.sin(t * 0.11f - 1.8f) * 0.25f;
    }
}
