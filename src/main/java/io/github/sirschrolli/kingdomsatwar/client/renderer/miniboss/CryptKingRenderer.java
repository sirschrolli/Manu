package io.github.sirschrolli.kingdomsatwar.client.renderer.miniboss;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.BonesRenderState;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.BonesRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.miniboss.CryptKingEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Morvan, der Gruftkönig: das Knochenmodell der Mod in seinem eigenen Bild (dunkle Knochen, Goldkrone, Königsmantel,
 * eisblaue Augen), 1,3-mal so groß. Solange er schläft, sitzt er auf seinem Thron – das Haupt gesenkt, die Arme auf den
 * Knien; erwacht er, steht er auf.
 */
public class CryptKingRenderer extends BonesRenderer<CryptKingEntity> {
    public CryptKingRenderer(EntityRendererProvider.Context context) {
        super(context, new String[] {"gruftkoenig"}, king -> 0);
    }

    @Override
    public void extractRenderState(CryptKingEntity king, BonesRenderState state, float partialTicks) {
        super.extractRenderState(king, state, partialTicks);
        if (!king.isAwake() && king.deathTime <= 0) {
            state.isPassenger = true;                          // die Sitzhaltung der Reiter: Beine nach vorn, Arme auf den Knien
            state.xRot = 28.0f;                                 // das Haupt gesenkt
            state.yRot = 0.0f;
        }
    }

    @Override
    protected void setupRotations(BonesRenderState state, PoseStack poseStack, float bodyRot, float scale) {
        super.setupRotations(state, poseStack, bodyRot, scale);
        if (state.isPassenger) {
            poseStack.translate(0.0f, -0.6f, 0.0f);            // auf die Sitzfläche des Throns
        }
    }
}
