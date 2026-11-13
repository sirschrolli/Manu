package io.github.sirschrolli.kingdomsatwar.client.renderer.abyss;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Der Kopf des Weltenfressers: Die acht Lippen öffnen sich wie eine Blüte (weit beim Biss, beim Ansaugen und beim
 * Rissatem) und zittern dabei; geschlossen greifen sie ineinander. Die Hauer arbeiten mit, die Kronensplitter stehen fest.
 */
public class WorldEaterModel extends EntityModel<WorldEaterRenderState> {
    private final ModelPart[] lips = new ModelPart[8];
    private final ModelPart tuskLeft;
    private final ModelPart tuskRight;

    public WorldEaterModel(ModelPart root) {
        super(root);
        ModelPart head = root.getChild("head");
        for (int k = 0; k < this.lips.length; k++) {
            this.lips[k] = head.getChild("lip" + k + "_base").getChild("lip" + k);
        }
        this.tuskLeft = head.getChild("tusk_left");
        this.tuskRight = head.getChild("tusk_right");
    }

    @Override
    public void setupAnim(WorldEaterRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float open = Mth.clamp(state.maw, 0.0f, 1.0f);
        for (int k = 0; k < this.lips.length; k++) {
            float tremble = open > 0.5f ? Mth.sin(t * 1.7f + k * 1.3f) * 3.0f * open : Mth.sin(t * 0.12f + k) * 1.2f;
            this.lips[k].xRot = (Mth.lerp(open, 6.0f, -62.0f) + tremble) * Mth.DEG_TO_RAD;
        }
        float jaw = (open * 16.0f + Mth.sin(t * 0.1f) * 2.0f) * Mth.DEG_TO_RAD;
        this.tuskLeft.yRot += jaw;
        this.tuskRight.yRot -= jaw;
    }
}
