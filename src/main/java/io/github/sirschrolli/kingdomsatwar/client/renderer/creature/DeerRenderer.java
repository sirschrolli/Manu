package io.github.sirschrolli.kingdomsatwar.client.renderer.creature;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.DeerModel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.DeerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/** Rothirsch (Kitze mit getupftem Fell, Geweih nur bei männlichen Tieren). */
public class DeerRenderer extends MobRenderer<DeerEntity, CreatureRenderState, DeerModel> {
    private static final Identifier ADULT = KingdomsAtWar.id("textures/entity/deer/deer.png");
    private static final Identifier FAWN = KingdomsAtWar.id("textures/entity/deer/deer_fawn.png");

    public DeerRenderer(EntityRendererProvider.Context context) {
        super(context, new DeerModel(context.bakeLayer(ModModelLayers.DEER)), 0.6f);
    }

    @Override
    public Identifier getTextureLocation(CreatureRenderState state) {
        return state.isBaby ? FAWN : ADULT;
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
    public void extractRenderState(DeerEntity entity, CreatureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.stag = entity.isStag();
        state.special.copyFrom(entity.grazeAnimation);
    }
}
