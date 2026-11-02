package io.github.sirschrolli.kingdomsatwar.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.SoldierModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;

/**
 * Rangabzeichen über der Rüstung: Veteranen tragen eine purpurne Schärpe mit silbernem Saum und einen silbernen
 * Ärmelstreifen, Elitekämpfer die Schärpe mit Goldsaum, zwei goldene Ärmelstreifen, goldene Schulterstücke und eine
 * Ehrenmedaille. Das Modell ist ein wenig größer als der Soldat und macht jede Geste mit.
 */
public class SoldierRankLayer extends RenderLayer<KingdomHumanoidRenderState, SoldierModel> {
    private static final Identifier[] TEXTURES = {
            KingdomsAtWar.id("textures/entity/soldier/rank/veteran.png"),
            KingdomsAtWar.id("textures/entity/soldier/rank/elite.png")};

    private final SoldierModel model;

    public SoldierRankLayer(RenderLayerParent<KingdomHumanoidRenderState, SoldierModel> renderer, EntityModelSet models) {
        super(renderer);
        this.model = new SoldierModel(models.bakeLayer(ModModelLayers.SOLDIER_RANK));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, KingdomHumanoidRenderState state, float yRot, float xRot) {
        if (state.rank <= 0) {
            return;
        }
        coloredCutoutModelCopyLayerRender(this.model, TEXTURES[Math.min(state.rank, TEXTURES.length) - 1], poseStack, collector, light, state, -1, 1);
    }
}
