package io.github.sirschrolli.kingdomsatwar.client.renderer.creature;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.WillOWispModel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.WillOWispEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/** Irrlicht: selbstleuchtend und durchscheinend, wirft keinen Schatten. */
public class WillOWispRenderer extends MobRenderer<WillOWispEntity, CreatureRenderState, WillOWispModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/will_o_wisp/will_o_wisp.png");

    public WillOWispRenderer(EntityRendererProvider.Context context) {
        super(context, new WillOWispModel(context.bakeLayer(ModModelLayers.WILL_O_WISP)), 0.0f);
    }

    @Override
    public Identifier getTextureLocation(CreatureRenderState state) {
        return TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(CreatureRenderState state, boolean isBodyVisible, boolean forceTransparent,
            boolean appearGlowing) {
        return RenderTypes.entityTranslucentEmissive(TEXTURE);
    }

    @Override
    protected int getBlockLightLevel(WillOWispEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public CreatureRenderState createRenderState() {
        return new CreatureRenderState();
    }
}
