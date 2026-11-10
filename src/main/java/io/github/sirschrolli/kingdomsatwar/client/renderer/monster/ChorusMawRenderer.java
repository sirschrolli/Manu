package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.monster.ChorusMawEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Chorusschlund; im offenen Schlund und in den Knospen glüht es magenta. */
public class ChorusMawRenderer extends MobRenderer<ChorusMawEntity, ChorusMawRenderState, ChorusMawModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/chorus_maw/chorus_maw.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/chorus_maw/chorus_maw_glow.png"));

    public ChorusMawRenderer(EntityRendererProvider.Context context) {
        super(context, new ChorusMawModel(context.bakeLayer(ModModelLayers.CHORUS_MAW)), 0.6f);
        this.addLayer(new GlowLayer<>(this, state -> GLOW));
    }

    @Override
    public Identifier getTextureLocation(ChorusMawRenderState state) {
        return TEXTURE;
    }

    @Override
    public ChorusMawRenderState createRenderState() {
        return new ChorusMawRenderState();
    }

    @Override
    public void extractRenderState(ChorusMawEntity entity, ChorusMawRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.lashLeft.copyFrom(entity.lashLeftAnimation);
        state.lashRight.copyFrom(entity.lashRightAnimation);
        state.bite.copyFrom(entity.biteAnimation);
        state.spit.copyFrom(entity.spitAnimation);
        state.open = Anim.follow(entity, 0, entity.isOpen() ? 1.0f : 0.0f, state.ageInTicks, 5.0f);
    }
}
