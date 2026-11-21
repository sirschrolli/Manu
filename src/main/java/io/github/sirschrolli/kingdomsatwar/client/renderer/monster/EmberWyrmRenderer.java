package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.monster.EmberWyrmEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Glutwurm (1,6-fach vergrößert) mit glühenden Fugen, Augen und Maul. */
public class EmberWyrmRenderer extends MobRenderer<EmberWyrmEntity, EmberWyrmRenderState, EmberWyrmModel> {
    public static final float SCALE = 1.6f;
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/ember_wyrm/ember_wyrm.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/ember_wyrm/ember_wyrm_glow.png"));

    public EmberWyrmRenderer(EntityRendererProvider.Context context) {
        super(context, new EmberWyrmModel(context.bakeLayer(ModModelLayers.EMBER_WYRM)), 0.9f);
        this.addLayer(new GlowLayer<>(this, state -> GLOW));
    }

    @Override
    public Identifier getTextureLocation(EmberWyrmRenderState state) {
        return TEXTURE;
    }

    @Override
    public EmberWyrmRenderState createRenderState() {
        return new EmberWyrmRenderState();
    }

    @Override
    public void extractRenderState(EmberWyrmEntity entity, EmberWyrmRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.spit.copyFrom(entity.spitAnimation);
        state.bite.copyFrom(entity.biteAnimation);
        state.rear = Anim.follow(entity, 0, entity.isRearing() ? 1.0f : 0.0f, state.ageInTicks, 4.0f);
        state.inLava = entity.isInLava();
    }

    @Override
    protected void scale(EmberWyrmRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    /** In der Lava wirft er keinen Schatten. */
    @Override
    protected float getShadowRadius(EmberWyrmRenderState state) {
        return state.inLava ? 0.0f : super.getShadowRadius(state);
    }
}
