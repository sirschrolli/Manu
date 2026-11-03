package io.github.sirschrolli.kingdomsatwar.client.model.monster;

import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.RevenantRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.util.Mth;

/**
 * Wiedergänger, Mumie und Wasserleiche auf dem Menschen-Skelett: Arme vorgestreckt wie beim Vanilla-Zombie; die
 * Wasserleiche schwimmt wie der Ertrunkene mit über den Kopf gereckten Armen.
 */
public class RevenantModel extends ZombieModel<RevenantRenderState> {
    public RevenantModel(ModelPart root) {
        super(root);
    }

    @Override
    protected void setupSwimAnimation(RevenantRenderState state, float animationPos, float swimAmount) {
        if (!state.drowned) {
            super.setupSwimAnimation(state, animationPos, swimAmount);
            return;
        }
        this.rightArm.xRot = Mth.rotLerpRad(swimAmount, this.rightArm.xRot, -2.5132742f) + swimAmount * 0.35f * Mth.sin(0.1f * state.ageInTicks);
        this.leftArm.xRot = Mth.rotLerpRad(swimAmount, this.leftArm.xRot, -2.5132742f) - swimAmount * 0.35f * Mth.sin(0.1f * state.ageInTicks);
        this.rightArm.zRot = Mth.rotLerpRad(swimAmount, this.rightArm.zRot, -0.15f);
        this.leftArm.zRot = Mth.rotLerpRad(swimAmount, this.leftArm.zRot, 0.15f);
        this.leftLeg.xRot = this.leftLeg.xRot - swimAmount * 0.55f * Mth.sin(0.1f * state.ageInTicks);
        this.rightLeg.xRot = this.rightLeg.xRot + swimAmount * 0.55f * Mth.sin(0.1f * state.ageInTicks);
        this.head.xRot = 0.0f;
    }
}
