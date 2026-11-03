package io.github.sirschrolli.kingdomsatwar.client.renderer.creature;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.WildBoarModel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.WildBoarEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/** Wildschwein (Frischlinge mit Streifen-Fell). */
public class WildBoarRenderer extends MobRenderer<WildBoarEntity, CreatureRenderState, WildBoarModel> {
    private static final Identifier ADULT = KingdomsAtWar.id("textures/entity/wild_boar/wild_boar.png");
    private static final Identifier BABY = KingdomsAtWar.id("textures/entity/wild_boar/wild_boar_baby.png");

    public WildBoarRenderer(EntityRendererProvider.Context context) {
        super(context, new WildBoarModel(context.bakeLayer(ModModelLayers.WILD_BOAR)), 0.6f);
    }

    @Override
    public Identifier getTextureLocation(CreatureRenderState state) {
        return state.isBaby ? BABY : ADULT;
    }

    /** Jungtiere halb so groß (der Kopf wird im Modell vergrößert). */
    @Override
    protected void scale(CreatureRenderState state, PoseStack poseStack) {
        if (state.isBaby) {
            poseStack.scale(state.ageScale, state.ageScale, state.ageScale);
        }
    }

    @Override
    public CreatureRenderState createRenderState() {
        return new CreatureRenderState();
    }

    @Override
    public void extractRenderState(WildBoarEntity entity, CreatureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.charging = entity.isCharging();
        state.attack.copyFrom(entity.attackAnimation);
    }
}
