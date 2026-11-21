package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.MireSnapperEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/** Moorschnapper. */
public class MireSnapperRenderer extends MobRenderer<MireSnapperEntity, MireSnapperRenderState, MireSnapperModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/mire_snapper/mire_snapper.png");

    public MireSnapperRenderer(EntityRendererProvider.Context context) {
        super(context, new MireSnapperModel(context.bakeLayer(ModModelLayers.MIRE_SNAPPER)), 0.8f);
    }

    @Override
    public Identifier getTextureLocation(MireSnapperRenderState state) {
        return TEXTURE;
    }

    @Override
    public MireSnapperRenderState createRenderState() {
        return new MireSnapperRenderState();
    }

    @Override
    public void extractRenderState(MireSnapperEntity entity, MireSnapperRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.withdraw = Anim.follow(entity, 0, entity.isWithdrawn() ? 1.0f : 0.0f, state.ageInTicks, 7.0f);
        state.submerged = entity.isInWater();
        state.offset = (entity.getId() * 11.3f) % 100.0f;
        state.snap.copyFrom(entity.snapAnimation);
        state.hiss.copyFrom(entity.hissAnimation);
    }
}
