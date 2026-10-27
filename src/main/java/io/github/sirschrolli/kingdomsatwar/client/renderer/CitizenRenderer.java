package io.github.sirschrolli.kingdomsatwar.client.renderer;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.CitizenModel;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.CitizenEntity;
import io.github.sirschrolli.kingdomsatwar.economy.town.Profession;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

/** Zeichnet die Bürger – jeder Beruf mit eigener Kleidung, jeder Bürger mit eigenem Gesicht. */
public class CitizenRenderer extends HumanoidMobRenderer<CitizenEntity, CitizenRenderState, CitizenModel> {
    public CitizenRenderer(EntityRendererProvider.Context context) {
        super(context, new CitizenModel(context.bakeLayer(ModModelLayers.CITIZEN)), 0.5f);
    }

    public static Identifier texture(Profession profession, int variant) {
        return KingdomsAtWar.id("textures/entity/citizen/" + profession.id() + "_" + (variant + 1) + ".png");
    }

    /** Was ein Bürger dieses Berufs trägt. */
    public static int parts(Profession profession) {
        return switch (profession) {
            case BUILDER -> CitizenModel.CAP | CitizenModel.POUCH;
            case FARMER -> CitizenModel.BRIM | CitizenModel.CROWN;
            case LUMBERJACK -> CitizenModel.CAP;
            case MINER -> CitizenModel.CROWN | CitizenModel.LAMP;
            case SMITH, INNKEEPER -> CitizenModel.APRON;
            case MERCHANT -> CitizenModel.CAP | CitizenModel.FEATHER | CitizenModel.POUCH;
            case CARTOGRAPHER -> CitizenModel.CAP | CitizenModel.QUIVER;
        };
    }

    @Override
    public CitizenRenderState createRenderState() {
        return new CitizenRenderState();
    }

    @Override
    public void extractRenderState(CitizenEntity citizen, CitizenRenderState state, float partialTicks) {
        super.extractRenderState(citizen, state, partialTicks);
        Profession profession = citizen.getProfession();
        int variant = Math.min(citizen.getVariant(), profession.variants() - 1);
        state.texture = texture(profession, variant);
        state.parts = parts(profession);
        state.feminine = citizen.isFeminine();
        state.working = citizen.isWorking();
        state.hammer = profession == Profession.SMITH;
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(CitizenEntity citizen, HumanoidArm arm) {
        ItemStack held = citizen.getItemHeldByArm(arm);
        if (held.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        }
        return arm == citizen.getMainArm() ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.EMPTY;
    }

    @Override
    public Identifier getTextureLocation(CitizenRenderState state) {
        return state.texture != null ? state.texture : texture(Profession.BUILDER, 0);
    }
}
