package io.github.sirschrolli.kingdomsatwar.client.model.farm;

import io.github.sirschrolli.kingdomsatwar.client.renderer.farm.FarmEquineRenderState;
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Esel und Maultier mit dem Skelett des Vanilla-Esels (passt zu Sattel und Packtaschen): Eselsohren mit Ohrenspiel und
 * Schweifquaste – beim Maultier kürzere Ohren und ein Pferdeschweif.
 */
public class FarmDonkeyModel extends AbstractEquineModel<FarmEquineRenderState> {
    private final ModelPart leftChest;
    private final ModelPart rightChest;
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart leftEarMule;
    private final ModelPart rightEarMule;
    private final ModelPart tailTuft;
    private final ModelPart tailHair;

    public FarmDonkeyModel(ModelPart root) {
        super(root);
        this.leftChest = this.body.getChild("left_chest");
        this.rightChest = this.body.getChild("right_chest");
        ModelPart head = this.headParts.getChild("head");
        this.leftEar = head.getChild("left_ear");
        this.rightEar = head.getChild("right_ear");
        this.leftEarMule = head.getChild("left_ear_mule");
        this.rightEarMule = head.getChild("right_ear_mule");
        ModelPart tail = this.body.getChild("tail");
        this.tailTuft = tail.getChild("tail_tuft");
        this.tailHair = tail.getChild("tail_hair");
    }

    @Override
    public void setupAnim(FarmEquineRenderState state) {
        super.setupAnim(state);
        this.leftChest.visible = state.hasChest;
        this.rightChest.visible = state.hasChest;
        this.leftEar.visible = !state.mule;
        this.rightEar.visible = !state.mule;
        this.leftEarMule.visible = state.mule;
        this.rightEarMule.visible = state.mule;
        this.tailTuft.visible = !state.mule;
        this.tailHair.visible = state.mule;

        float t = state.ageInTicks;
        ModelPart left = state.mule ? this.leftEarMule : this.leftEar;
        ModelPart right = state.mule ? this.rightEarMule : this.rightEar;
        left.xRot += Math.max(0.0f, Mth.sin(t * 0.05f)) * 0.45f;
        right.xRot += Math.max(0.0f, Mth.sin(t * 0.05f + 2.0f)) * 0.45f;
        left.zRot += Mth.sin(t * 0.03f) * 0.1f;
        right.zRot -= Mth.sin(t * 0.03f + 1.0f) * 0.1f;
        if (state.isBaby) {
            this.headParts.xScale = this.headParts.yScale = this.headParts.zScale = 1.2f;
        }
    }
}
