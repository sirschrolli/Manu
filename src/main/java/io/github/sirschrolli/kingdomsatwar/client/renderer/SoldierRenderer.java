package io.github.sirschrolli.kingdomsatwar.client.renderer;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.SoldierModel;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

/** Zeichnet die Soldaten – jede Art mit eigener Rüstung, jeder Soldat mit eigenem Gesicht. */
public class SoldierRenderer extends HumanoidMobRenderer<SoldierEntity, KingdomHumanoidRenderState, SoldierModel> {
    public SoldierRenderer(EntityRendererProvider.Context context) {
        super(context, new SoldierModel(context.bakeLayer(ModModelLayers.SOLDIER)), 0.5f);
    }

    public static Identifier texture(SoldierType type, int variant) {
        return KingdomsAtWar.id("textures/entity/soldier/" + type.id() + "_" + (variant + 1) + ".png");
    }

    @Override
    public KingdomHumanoidRenderState createRenderState() {
        return new KingdomHumanoidRenderState();
    }

    @Override
    public void extractRenderState(SoldierEntity soldier, KingdomHumanoidRenderState state, float partialTicks) {
        super.extractRenderState(soldier, state, partialTicks);
        SoldierType type = soldier.getSoldierType();
        state.texture = texture(type, soldier.getVariant());
        state.salute = soldier.saluteProgress(partialTicks);
        state.cheer = soldier.cheerProgress(partialTicks);
        state.cape = type == SoldierType.CAPTAIN || type == SoldierType.KNIGHT;
        state.plume = type == SoldierType.CAPTAIN || type == SoldierType.KNIGHT && soldier.getVariant() == 1;
        state.quiver = type == SoldierType.ARCHER;
        extractCape(soldier, state, partialTicks);
    }

    /** Der Umhang weht beim Laufen nach hinten und bewegt sich leicht im Wind. */
    static void extractCape(net.minecraft.world.entity.LivingEntity entity, KingdomHumanoidRenderState state, float partialTicks) {
        float speed = entity.walkAnimation.speed(partialTicks);
        float pos = entity.walkAnimation.position(partialTicks);
        state.capeLean = Mth.clamp(speed * 55.0f, 0.0f, 60.0f);
        state.capeFlap = Mth.sin(pos * 0.45f) * speed * 6.0f + Mth.sin(state.ageInTicks * 0.09f) * 1.5f;
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(SoldierEntity soldier, HumanoidArm arm) {
        return armPose(soldier, arm, super.getArmPose(soldier, arm));
    }

    /** Bogen spannen, Schild heben oder die Waffe bereit halten. */
    static HumanoidModel.ArmPose armPose(net.minecraft.world.entity.Mob mob, HumanoidArm arm, HumanoidModel.ArmPose fallback) {
        ItemStack held = mob.getItemHeldByArm(arm);
        if (held.isEmpty()) {
            return fallback;
        }
        if (mob.isUsingItem() && mob.getUsedItemHand().asArm(mob.getMainArm()) == arm && held.has(DataComponents.BLOCKS_ATTACKS)) {
            return HumanoidModel.ArmPose.BLOCK;
        }
        if (held.getItem() instanceof BowItem && mob.isAggressive() && mob.getMainArm() == arm) {
            return HumanoidModel.ArmPose.BOW_AND_ARROW;
        }
        if (fallback != HumanoidModel.ArmPose.EMPTY) {
            return fallback;
        }
        return arm == mob.getMainArm() || mob.getItemInHand(InteractionHand.OFF_HAND) == held ? HumanoidModel.ArmPose.ITEM : fallback;
    }

    @Override
    public Identifier getTextureLocation(KingdomHumanoidRenderState state) {
        return state.texture != null ? state.texture : texture(SoldierType.SWORDSMAN, 0);
    }
}
