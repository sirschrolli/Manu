package io.github.sirschrolli.kingdomsatwar.client.model.monster;

import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.HagRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Hexe auf dem Menschen-Skelett: der Spitzhut wippt beim Gehen nach, die Nase zuckt, wenn sie einen Trank bereithält,
 * und beim Trinken führt sie die Flasche zum Mund und legt den Kopf in den Nacken.
 */
public class HagModel extends HumanoidModel<HagRenderState> {
    private final ModelPart nose;
    private final ModelPart hatTier1;
    private final ModelPart hatTier2;
    private final ModelPart hatTip;
    private final ModelPart hair;

    public HagModel(ModelPart root) {
        super(root);
        this.nose = this.head.getChild("nose");
        ModelPart crown = this.head.getChild("hat_brim").getChild("hat_crown");
        this.hatTier1 = crown.getChild("hat_tier1");
        this.hatTier2 = this.hatTier1.getChild("hat_tier2");
        this.hatTip = this.hatTier2.getChild("hat_tip");
        this.hair = this.head.getChild("long_hair");
    }

    @Override
    public void setupAnim(HagRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float walk = state.walkAnimationSpeed;
        // die Hutspitze schwingt beim Gehen und im Wind ein wenig nach
        float sway = Mth.sin(state.walkAnimationPos * 0.6662f) * walk * 0.12f + Mth.sin(t * 0.05f) * 0.03f;
        this.hatTier1.zRot += sway * 0.5f;
        this.hatTier2.zRot += sway;
        this.hatTip.zRot += sway * 1.5f;
        this.hair.xRot = -0.21f - Math.max(0.0f, this.head.xRot) * 0.9f;
        if (state.holdingPotion) {
            // die Nase zuckt, wenn sie einen Trank in der Hand hält
            this.nose.y = this.nose.getInitialPose().y() + Mth.sin(t * 0.8f) * 0.25f;
            this.nose.xRot = this.nose.getInitialPose().xRot() + Mth.sin(t * 0.6f) * 0.06f;
        }
        if (state.drinking) {
            // die Flasche zum Mund, den Kopf in den Nacken
            this.rightArm.xRot = -1.85f + Mth.sin(t * 0.9f) * 0.05f;
            this.rightArm.yRot = -0.55f;
            this.rightArm.zRot = 0.0f;
            this.head.xRot = -0.45f;
        }
    }
}
