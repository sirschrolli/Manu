package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.monster.SoulHeronEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Seelenreiher mit glimmenden Federspitzen, Schopf und Schnabelnaht. */
public class SoulHeronRenderer extends MobRenderer<SoulHeronEntity, SoulHeronRenderState, SoulHeronModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/soul_heron/soul_heron.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/soul_heron/soul_heron_glow.png"));

    public SoulHeronRenderer(EntityRendererProvider.Context context) {
        super(context, new SoulHeronModel(context.bakeLayer(ModModelLayers.SOUL_HERON)), 0.55f);
        this.addLayer(new GlowLayer<>(this, state -> GLOW));
    }

    @Override
    public Identifier getTextureLocation(SoulHeronRenderState state) {
        return TEXTURE;
    }

    @Override
    public SoulHeronRenderState createRenderState() {
        return new SoulHeronRenderState();
    }

    @Override
    public void extractRenderState(SoulHeronEntity entity, SoulHeronRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.stab.copyFrom(entity.stabAnimation);
        state.buffet.copyFrom(entity.buffetAnimation);
        state.dive.copyFrom(entity.diveAnimation);
        state.flying = Anim.follow(entity, 0, entity.isFlying() ? 1.0f : 0.0f, state.ageInTicks, 6.0f);
    }
}
