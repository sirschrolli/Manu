package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildBee;
import net.minecraft.client.model.animal.bee.AdultBeeModel;
import net.minecraft.client.model.animal.bee.BeeModel;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Biene: Honigbiene oder Hummel, je ruhig oder wütend, mit oder ohne Pollen an den Beinen. */
public class WildBeeRenderer extends WildRenderer<WildBee, WildBeeRenderState, BeeModel> {
    private static final Identifier[] CALM = textures("bee", "bee_", WildBee.Breed.values(), "");
    private static final Identifier[] ANGRY = textures("bee", "bee_", WildBee.Breed.values(), "_angry");
    private static final Identifier[] NECTAR = textures("bee", "bee_", WildBee.Breed.values(), "_nectar");
    private static final Identifier[] ANGRY_NECTAR = textures("bee", "bee_", WildBee.Breed.values(), "_angry_nectar");

    public WildBeeRenderer(EntityRendererProvider.Context context) {
        super(context, new AdultBeeModel(context.bakeLayer(WildRenderers.BEE)), 0.4f, new BeeRenderer(context));
    }

    @Override
    protected void extract(WildBee bee, WildBeeRenderState state, float partialTicks) {
        state.breed = bee.getBreed().ordinal();
    }

    @Override
    public Identifier getTextureLocation(WildBeeRenderState state) {
        Identifier[] set = state.isAngry ? (state.hasNectar ? ANGRY_NECTAR : ANGRY) : (state.hasNectar ? NECTAR : CALM);
        return pick(set, state.breed);
    }

    @Override
    public WildBeeRenderState createRenderState() {
        return new WildBeeRenderState();
    }
}
