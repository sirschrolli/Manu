package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.monster.WerewolfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Werwolf mit glühenden Bernsteinaugen. */
public class WerewolfRenderer extends MobRenderer<WerewolfEntity, WerewolfRenderState, WerewolfModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/werewolf/werewolf.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/werewolf/werewolf_glow.png"));

    public WerewolfRenderer(EntityRendererProvider.Context context) {
        super(context, new WerewolfModel(context.bakeLayer(ModModelLayers.WEREWOLF)), 0.75f);
        this.addLayer(new GlowLayer<>(this, state -> GLOW));
    }

    @Override
    public Identifier getTextureLocation(WerewolfRenderState state) {
        return TEXTURE;
    }

    @Override
    public WerewolfRenderState createRenderState() {
        return new WerewolfRenderState();
    }

    @Override
    public void extractRenderState(WerewolfEntity entity, WerewolfRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.swipe.copyFrom(entity.swipeAnimation);
        state.howl.copyFrom(entity.howlAnimation);
        state.pounce.copyFrom(entity.pounceAnimation);
        state.run = Anim.follow(entity, 0, entity.isRunning() ? 1.0f : 0.0f, state.ageInTicks, 5.0f);
    }
}
