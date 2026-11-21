package io.github.sirschrolli.kingdomsatwar.client.renderer.creature;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.CaveTrollModel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.CaveTrollEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Höhlentroll mit glühenden Augen; im Sonnenlicht wird er grau wie Stein. */
public class CaveTrollRenderer extends MobRenderer<CaveTrollEntity, CreatureRenderState, CaveTrollModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/cave_troll/cave_troll.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/cave_troll/cave_troll_glow.png"));

    public CaveTrollRenderer(EntityRendererProvider.Context context) {
        super(context, new CaveTrollModel(context.bakeLayer(ModModelLayers.CAVE_TROLL)), 1.0f);
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
    protected int getModelTint(CreatureRenderState state) {
        return state.petrifying ? 0xFFB8BCC0 : -1;
    }

    @Override
    public CreatureRenderState createRenderState() {
        return new CreatureRenderState();
    }

    @Override
    public void extractRenderState(CaveTrollEntity entity, CreatureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.petrifying = entity.isPetrifying();
        state.attack.copyFrom(entity.smashAnimation);
    }
}
