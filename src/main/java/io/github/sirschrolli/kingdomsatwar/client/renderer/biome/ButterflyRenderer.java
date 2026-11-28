package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.ButterflyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/** Schmetterlinge: sechs Arten, eine Textur je Art (die Flügelform steckt in der Textur). */
public class ButterflyRenderer extends MobRenderer<ButterflyEntity, ButterflyRenderState, ButterflyModel> {
    private static final String[] SPECIES = {"peacock", "brimstone", "swallowtail", "blue", "admiral", "monarch"};
    private static final Identifier[] TEXTURES = new Identifier[SPECIES.length];

    static {
        for (int i = 0; i < SPECIES.length; i++) {
            TEXTURES[i] = KingdomsAtWar.id("textures/entity/butterfly/" + SPECIES[i] + ".png");
        }
    }

    public ButterflyRenderer(EntityRendererProvider.Context context) {
        super(context, new ButterflyModel(context.bakeLayer(ModModelLayers.BUTTERFLY)), 0.12f);
    }

    @Override
    public Identifier getTextureLocation(ButterflyRenderState state) {
        return TEXTURES[Math.floorMod(state.variant, TEXTURES.length)];
    }

    @Override
    public ButterflyRenderState createRenderState() {
        return new ButterflyRenderState();
    }

    @Override
    public void extractRenderState(ButterflyEntity entity, ButterflyRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant();
        state.perch = Anim.follow(entity, 0, entity.isPerched() ? 1.0f : 0.0f, state.ageInTicks, 8.0f);
        state.offset = (entity.getId() * 7.31f) % 20.0f;
        state.openWings = entity.isNoAi() ? 1.0f : -1.0f;
    }
}
