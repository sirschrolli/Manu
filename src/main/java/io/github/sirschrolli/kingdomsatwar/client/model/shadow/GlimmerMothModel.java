package io.github.sirschrolli.kingdomsatwar.client.model.shadow;

import io.github.sirschrolli.kingdomsatwar.client.renderer.shadow.ShadowCreatureRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Glimmerfalter: schneller, weicher Flügelschlag (Hinterflügel leicht versetzt), zitternde Fühler, Gaukeln. */
public class GlimmerMothModel extends EntityModel<ShadowCreatureRenderState> {
    private final ModelPart body;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftHind;
    private final ModelPart rightHind;
    private final ModelPart antennaL;
    private final ModelPart antennaR;

    public GlimmerMothModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.leftWing = this.body.getChild("left_wing");
        this.rightWing = this.body.getChild("right_wing");
        this.leftHind = this.body.getChild("left_hind_wing");
        this.rightHind = this.body.getChild("right_hind_wing");
        this.antennaL = this.body.getChild("antenna_l");
        this.antennaR = this.body.getChild("antenna_r");
    }

    @Override
    public void setupAnim(ShadowCreatureRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float flap = Mth.sin(t * 1.3f);
        float hind = Mth.sin(t * 1.3f - 0.5f);
        this.leftWing.zRot = -0.15f + flap * 0.9f;
        this.rightWing.zRot = -this.leftWing.zRot;
        this.leftHind.zRot = -0.1f + hind * 0.7f;
        this.rightHind.zRot = -this.leftHind.zRot;
        this.body.y += Mth.sin(t * 0.25f) * 1.2f;                       // Gaukeln
        this.body.xRot = -0.15f + Mth.sin(t * 0.25f + 1.0f) * 0.08f;
        this.antennaL.xRot += Mth.sin(t * 0.6f) * 0.08f;
        this.antennaR.xRot += Mth.sin(t * 0.6f + 0.5f) * 0.08f;
    }
}
