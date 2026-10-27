package io.github.sirschrolli.kingdomsatwar.client.renderer;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.SoldierModel;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.BanditEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;

/** Zeichnet die Banditen (gleiches Modell wie die Soldaten, eigene Kleidung). */
public class BanditRenderer extends HumanoidMobRenderer<BanditEntity, KingdomHumanoidRenderState, SoldierModel> {
    public BanditRenderer(EntityRendererProvider.Context context) {
        super(context, new SoldierModel(context.bakeLayer(ModModelLayers.SOLDIER)), 0.5f);
    }

    public static Identifier texture(boolean archer, int variant) {
        return KingdomsAtWar.id("textures/entity/bandit/" + (archer ? "bandit_archer_" : "bandit_") + (variant + 1) + ".png");
    }

    @Override
    public KingdomHumanoidRenderState createRenderState() {
        return new KingdomHumanoidRenderState();
    }

    @Override
    public void extractRenderState(BanditEntity bandit, KingdomHumanoidRenderState state, float partialTicks) {
        super.extractRenderState(bandit, state, partialTicks);
        state.texture = texture(bandit.isArcher(), bandit.getVariant());
        state.quiver = bandit.isArcher();
        state.cape = false;
        state.plume = false;
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(BanditEntity bandit, HumanoidArm arm) {
        return SoldierRenderer.armPose(bandit, arm, super.getArmPose(bandit, arm));
    }

    @Override
    public Identifier getTextureLocation(KingdomHumanoidRenderState state) {
        return state.texture != null ? state.texture : texture(false, 0);
    }
}
