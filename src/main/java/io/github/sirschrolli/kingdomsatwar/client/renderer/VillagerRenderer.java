package io.github.sirschrolli.kingdomsatwar.client.renderer;

import io.github.sirschrolli.kingdomsatwar.client.model.CitizenModel;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

/**
 * Zeichnet die Dorfbewohner mit dem Menschen-Modell der Bürger – jeder Beruf mit eigener Tracht und eigenen Zusatzteilen
 * (Strohhut, Filzhut, Strickmütze, Bäckermütze, Gugel, Köcher, Umhängetasche, Schürze, Kutte).
 */
public class VillagerRenderer extends HumanoidMobRenderer<VillagerEntity, CitizenRenderState, CitizenModel> {
    public VillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new CitizenModel(context.bakeLayer(ModModelLayers.CITIZEN)), 0.5f);
    }

    /** Was ein Bewohner dieses Berufs trägt. */
    public static int parts(VillageProfession profession) {
        return switch (profession) {
            case PEASANT -> CitizenModel.HOOD;
            case FARMER -> CitizenModel.BRIM | CitizenModel.CROWN;
            case FISHER -> CitizenModel.CAP | CitizenModel.SATCHEL;
            case SHEPHERD -> CitizenModel.BRIM | CitizenModel.CROWN | CitizenModel.SATCHEL;
            case FLETCHER -> CitizenModel.CAP | CitizenModel.FEATHER | CitizenModel.QUIVER;
            case SCRIBE -> CitizenModel.ROBE | CitizenModel.SATCHEL;
            case CARTOGRAPHER -> CitizenModel.CAP | CitizenModel.QUIVER;
            case HERBALIST -> CitizenModel.HOOD | CitizenModel.ROBE | CitizenModel.SATCHEL;
            case ARMORER, WEAPONSMITH, TANNER, INNKEEPER -> CitizenModel.APRON;
            case TOOLSMITH, BUTCHER -> CitizenModel.CAP | CitizenModel.APRON;
            case MASON -> CitizenModel.CAP | CitizenModel.POUCH;
            case BAKER -> CitizenModel.TALL_HAT | CitizenModel.APRON;
            case MERCHANT -> CitizenModel.CAP | CitizenModel.FEATHER | CitizenModel.POUCH;
        };
    }

    @Override
    public CitizenRenderState createRenderState() {
        return new CitizenRenderState();
    }

    @Override
    public void extractRenderState(VillagerEntity villager, CitizenRenderState state, float partialTicks) {
        super.extractRenderState(villager, state, partialTicks);
        VillageProfession profession = villager.getProfession();
        state.texture = VillagerEntity.texture(profession, villager.getVariant());
        state.parts = parts(profession);
        state.feminine = villager.isFeminine();
        state.working = villager.isWorking();
        state.hammer = profession.hammer();
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(VillagerEntity villager, HumanoidArm arm) {
        ItemStack held = villager.getItemHeldByArm(arm);
        if (held.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        }
        return arm == villager.getMainArm() ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.EMPTY;
    }

    @Override
    public Identifier getTextureLocation(CitizenRenderState state) {
        return state.texture != null ? state.texture : VillagerEntity.texture(VillageProfession.PEASANT, 0);
    }
}
