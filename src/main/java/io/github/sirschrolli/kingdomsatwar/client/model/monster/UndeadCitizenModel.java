package io.github.sirschrolli.kingdomsatwar.client.model.monster;

import io.github.sirschrolli.kingdomsatwar.client.model.CitizenModel;
import io.github.sirschrolli.kingdomsatwar.client.renderer.CitizenRenderState;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.UndeadVillagerRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * Untote Dorfbewohner: das Modell der lebenden Dorfbewohner mit ihrer Berufstracht, aber mit vorgestreckten Armen wie
 * die Wiedergänger (beim Angreifen höher erhoben).
 */
public class UndeadCitizenModel extends CitizenModel {
    public UndeadCitizenModel(ModelPart root) {
        super(root);
    }

    @Override
    protected void setupAttackAnimation(CitizenRenderState state) {
        super.setupAttackAnimation(state);
        if (state instanceof UndeadVillagerRenderState undead) {
            zombieArms(undead);
        }
    }

    /** Wie {@code AnimationUtils.animateZombieArms}. */
    private void zombieArms(UndeadVillagerRenderState state) {
        boolean raiseArms = !state.isBaby || state.getMainHandItemStack() == ItemStack.EMPTY;
        float armDrop = raiseArms ? -Mth.PI / (state.isAggressive ? 1.5f : 2.25f) : 0.0f;
        float attackY = (raiseArms ? 1.0f : -1.0f) * Mth.sin(state.swingAnimation * Mth.PI);
        float attackX = Mth.sin((1.0f - (1.0f - state.swingAnimation) * (1.0f - state.swingAnimation)) * Mth.PI);
        float xRot = armDrop + attackY * 1.2f - attackX * 0.4f;
        float yRot = 0.1f - attackY * 0.6f;
        if (state.rightArmPose != HumanoidModel.ArmPose.THROW_TRIDENT) {
            this.rightArm.xRot = xRot;
            this.rightArm.yRot = raiseArms ? -yRot : yRot;
            this.rightArm.zRot = 0.0f;
        }
        if (state.leftArmPose != HumanoidModel.ArmPose.THROW_TRIDENT) {
            this.leftArm.xRot = xRot;
            this.leftArm.yRot = raiseArms ? yRot : -yRot;
            this.leftArm.zRot = 0.0f;
        }
    }
}
