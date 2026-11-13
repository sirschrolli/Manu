package io.github.sirschrolli.kingdomsatwar.client.renderer.sea;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.sea.SharkModel;
import io.github.sirschrolli.kingdomsatwar.entity.sea.SharkEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Hai. */
public class SharkRenderer extends MobRenderer<SharkEntity, SeaRenderState, SharkModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/shark/shark.png");

    public SharkRenderer(EntityRendererProvider.Context context) {
        super(context, new SharkModel(context.bakeLayer(ModModelLayers.SHARK)), 0.8f);
    }

    @Override
    public Identifier getTextureLocation(SeaRenderState state) {
        return TEXTURE;
    }

    @Override
    public SeaRenderState createRenderState() {
        return new SeaRenderState();
    }

    @Override
    public void extractRenderState(SharkEntity entity, SeaRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.swim = Mth.lerp(partialTicks, entity.swimO, entity.swim);
        state.inWater = entity.isInWater();
        state.attack.copyFrom(entity.biteAnimation);
    }

    @Override
    protected float getShadowStrength(SeaRenderState state) {
        return 0.35f;
    }
}
