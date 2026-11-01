package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.client.model.wild.WildCamelModel;
import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildCamelRenderState;
import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DryCamel;
import net.minecraft.client.model.animal.camel.CamelModel;
import net.minecraft.client.model.animal.camel.CamelSaddleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.CamelHuskRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Dörrkamel und Mumienkamel: Kamel-Modell der Mod mit einem Höcker, eigene Bilder, glimmende Augen, Sattel des Kamel-Husks. */
public class DryCamelRenderer extends WildRenderer<DryCamel, DryCamelRenderer.State, CamelModel> {
    private static final String[] NAMES = {"doerrkamel", "mumienkamel"};
    private static final Identifier[] TEXTURES = MonsterRenderers.textures("camel_husk", NAMES);
    private static final RenderType[] GLOWS = MonsterRenderers.glows("camel_husk", NAMES);

    /** Zeichenzustand: Kamel-Zustand (ein Höcker wie beim Dromedar) und Art. */
    public static class State extends WildCamelRenderState {
        public int look;
    }

    public DryCamelRenderer(EntityRendererProvider.Context context) {
        super(context, new WildCamelModel(context.bakeLayer(MonsterRenderers.CAMEL_HUSK)), 0.7f, new CamelHuskRenderer(context));
        this.addLayer(new SimpleEquipmentLayer<>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.CAMEL_HUSK_SADDLE,
                state -> state.saddle, new CamelSaddleModel(context.bakeLayer(ModelLayers.CAMEL_HUSK_SADDLE)), null));
        this.addLayer(new GlowLayer<>(this, state -> GLOWS[state.look]));
    }

    @Override
    protected void extract(DryCamel camel, State state, float partialTicks) {
        state.breed = 0;
        state.look = Mth.clamp(camel.getLook().ordinal(), 0, NAMES.length - 1);
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURES[state.look];
    }

    @Override
    public State createRenderState() {
        return new State();
    }
}
