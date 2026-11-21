package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.EmberSalamanderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Glutsalamander: zwei Arten, die Glut in Rissen, Flecken und Kamm leuchtet im Dunkeln. */
public class EmberSalamanderRenderer extends MobRenderer<EmberSalamanderEntity, EmberSalamanderRenderState, EmberSalamanderModel> {
    private static final String[] NAMES = {"ember_salamander", "ember_salamander_gold"};
    private static final Identifier[] TEXTURES = new Identifier[NAMES.length];
    private static final RenderType[] GLOW = new RenderType[NAMES.length];

    static {
        for (int i = 0; i < NAMES.length; i++) {
            TEXTURES[i] = KingdomsAtWar.id("textures/entity/ember_salamander/" + NAMES[i] + ".png");
            GLOW[i] = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/ember_salamander/" + NAMES[i] + "_glow.png"));
        }
    }

    public EmberSalamanderRenderer(EntityRendererProvider.Context context) {
        super(context, new EmberSalamanderModel(context.bakeLayer(ModModelLayers.EMBER_SALAMANDER)), 0.35f);
        this.addLayer(new GlowLayer<>(this, state -> GLOW[Math.floorMod(state.variant, GLOW.length)]));
    }

    @Override
    public Identifier getTextureLocation(EmberSalamanderRenderState state) {
        return TEXTURES[Math.floorMod(state.variant, TEXTURES.length)];
    }

    @Override
    public EmberSalamanderRenderState createRenderState() {
        return new EmberSalamanderRenderState();
    }

    @Override
    public void extractRenderState(EmberSalamanderEntity entity, EmberSalamanderRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant();
        state.bask = Anim.follow(entity, 0, entity.isBasking() ? 1.0f : 0.0f, state.ageInTicks, 3.0f);
        state.offset = (entity.getId() * 13.7f) % 100.0f;
    }
}
