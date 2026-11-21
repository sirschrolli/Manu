package io.github.sirschrolli.kingdomsatwar.client.renderer.creature;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.LindwormModel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.LindwormEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Lindwurm mit leuchtenden Augen. */
public class LindwormRenderer extends MobRenderer<LindwormEntity, CreatureRenderState, LindwormModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/lindworm/lindworm.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/lindworm/lindworm_glow.png"));

    public LindwormRenderer(EntityRendererProvider.Context context) {
        super(context, new LindwormModel(context.bakeLayer(ModModelLayers.LINDWORM)), 1.0f);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public Identifier getTextureLocation(CreatureRenderState state) {
        return TEXTURE;
    }

    @Override
    public CreatureRenderState createRenderState() {
        return new CreatureRenderState();
    }

    @Override
    public void extractRenderState(LindwormEntity entity, CreatureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack.copyFrom(entity.biteAnimation);
        state.special.copyFrom(entity.sweepAnimation);
    }
}
