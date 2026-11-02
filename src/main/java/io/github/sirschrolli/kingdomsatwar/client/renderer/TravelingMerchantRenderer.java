package io.github.sirschrolli.kingdomsatwar.client.renderer;

import io.github.sirschrolli.kingdomsatwar.client.model.CitizenModel;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.villager.TravelingMerchant;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

/**
 * Zeichnet den fahrenden Händler mit dem Menschen-Modell der Bürger: breitkrempiger Hut, langer Reisemantel,
 * Geldbeutel und die Kiepe auf dem Rücken (Weidenkorb mit Deckenrolle, Kupferkessel und Laterne). Trinkt er nachts
 * seinen Unsichtbarkeitstrank, sieht man die Flasche in seiner Hand.
 */
public class TravelingMerchantRenderer extends HumanoidMobRenderer<TravelingMerchant, CitizenRenderState, CitizenModel> {
    /** Was der Händler trägt. */
    public static final int PARTS = CitizenModel.BRIM | CitizenModel.CROWN | CitizenModel.ROBE | CitizenModel.POUCH | CitizenModel.PACK;

    public TravelingMerchantRenderer(EntityRendererProvider.Context context) {
        super(context, new CitizenModel(context.bakeLayer(ModModelLayers.CITIZEN)), 0.5f);
    }

    @Override
    public CitizenRenderState createRenderState() {
        return new CitizenRenderState();
    }

    @Override
    public void extractRenderState(TravelingMerchant merchant, CitizenRenderState state, float partialTicks) {
        super.extractRenderState(merchant, state, partialTicks);
        state.texture = TravelingMerchant.texture(merchant.getVariant());
        state.parts = PARTS;
        state.feminine = merchant.isFeminine();
        state.working = false;
        state.hammer = false;
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(TravelingMerchant merchant, HumanoidArm arm) {
        ItemStack held = merchant.getItemHeldByArm(arm);
        if (held.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        }
        return arm == merchant.getMainArm() ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.EMPTY;
    }

    @Override
    public Identifier getTextureLocation(CitizenRenderState state) {
        return state.texture != null ? state.texture : TravelingMerchant.texture(0);
    }
}
