package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.PlantCreeper;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Creeper;

/**
 * Kriecher: eigenes Modell (Blätter, Stacheln, Reif, Pilze, Blüten) und eigene Bilder je Art; Anschwellen, weißes
 * Blinken und die Blitz-Aura erbt er vom Vanilla-Renderer (die Hauptquader sind so groß wie beim Creeper).
 */
public class PlantCreeperRenderer extends CreeperRenderer {
    static final String[] NAMES = {"kriecher_wald", "kriecher_wueste", "kriecher_schnee", "kriecher_hoehle", "kriecher_dschungel"};
    private static final Identifier[] TEXTURES = MonsterRenderers.textures("creeper", NAMES);
    private static final RenderType[] GLOWS = MonsterRenderers.glows("creeper", NAMES);

    public PlantCreeperRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CreeperModel(context.bakeLayer(MonsterRenderers.CREEPER));
        this.addLayer(new GlowLayer<>(this, state -> state instanceof PlantCreeperRenderState plant ? GLOWS[plant.look] : null));
    }

    @Override
    public CreeperRenderState createRenderState() {
        return new PlantCreeperRenderState();
    }

    @Override
    public void extractRenderState(Creeper entity, CreeperRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof PlantCreeperRenderState plant) {
            plant.look = entity instanceof PlantCreeper creeper ? Mth.clamp(creeper.getLook().ordinal(), 0, NAMES.length - 1) : 0;
        }
    }

    @Override
    public Identifier getTextureLocation(CreeperRenderState state) {
        return TEXTURES[state instanceof PlantCreeperRenderState plant ? plant.look : 0];
    }
}
