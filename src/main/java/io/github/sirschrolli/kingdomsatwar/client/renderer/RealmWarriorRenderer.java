package io.github.sirschrolli.kingdomsatwar.client.renderer;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.SoldierModel;
import io.github.sirschrolli.kingdomsatwar.entity.realm.RealmWarriorEntity;
import io.github.sirschrolli.kingdomsatwar.war.Realm;
import io.github.sirschrolli.kingdomsatwar.war.WarriorRole;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

/** Zeichnet die Krieger der fremden Reiche (Soldatenmodell, Wappenrock in den Farben ihres Reichs). */
public class RealmWarriorRenderer extends HumanoidMobRenderer<RealmWarriorEntity, KingdomHumanoidRenderState, SoldierModel> {
    public RealmWarriorRenderer(EntityRendererProvider.Context context) {
        super(context, new SoldierModel(context.bakeLayer(ModModelLayers.SOLDIER)), 0.5f);
    }

    public static Identifier texture(Realm realm, WarriorRole role, int variant) {
        return KingdomsAtWar.id("textures/entity/realm/" + realm.id() + "/" + role.id() + "_" + (variant + 1) + ".png");
    }

    @Override
    public KingdomHumanoidRenderState createRenderState() {
        return new KingdomHumanoidRenderState();
    }

    @Override
    public void extractRenderState(RealmWarriorEntity warrior, KingdomHumanoidRenderState state, float partialTicks) {
        super.extractRenderState(warrior, state, partialTicks);
        WarriorRole role = warrior.role();
        state.texture = texture(warrior.realm(), role, warrior.variant());
        state.cape = role == WarriorRole.KNIGHT || role == WarriorRole.CAPTAIN || role == WarriorRole.LORD;
        state.plume = role == WarriorRole.CAPTAIN || role == WarriorRole.KNIGHT && warrior.variant() == 1;
        state.quiver = role.ranged();
        state.rank = 0;
        SoldierRenderer.extractCape(warrior, state, partialTicks);
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(RealmWarriorEntity warrior, HumanoidArm arm) {
        ItemStack held = warrior.getItemHeldByArm(arm);
        if (held.getItem() instanceof CrossbowItem && warrior.getMainArm() == arm) {
            if (warrior.isChargingCrossbow()) {
                return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            }
            if (warrior.isAggressive() && CrossbowItem.isCharged(held)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }
        }
        return SoldierRenderer.armPose(warrior, arm, super.getArmPose(warrior, arm));
    }

    @Override
    public Identifier getTextureLocation(KingdomHumanoidRenderState state) {
        return state.texture != null ? state.texture : texture(Realm.EISENMARK, WarriorRole.FOOTMAN, 0);
    }
}
