package io.github.sirschrolli.kingdomsatwar.client.renderer.water;

import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.Newt;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.model.animal.axolotl.AdultAxolotlModel;
import net.minecraft.client.renderer.entity.AxolotlRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.AxolotlRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

/**
 * Molch: eine eigene Art an Stelle jeder Vanilla-Farbe (Grottenolm, Wildmolch, Goldmolch, Eismolch, Blaumolch mit
 * leuchtenden Kiemenspitzen). Schwimmen, Laufen und Totstellen wie beim Vanilla-Axolotl; Jungtiere sind halb so groß.
 */
public class NewtRenderer extends WildRenderer<Newt, AxolotlRenderState, AdultAxolotlModel> {
    private static final Map<Axolotl.Variant, Identifier> TEXTURES = new EnumMap<>(Axolotl.Variant.class);
    private static final RenderType BLUE_GLOW = RenderTypes.eyes(WaterRenderers.texture("axolotl", "blaumolch_glow"));

    static {
        TEXTURES.put(Axolotl.Variant.LUCY, WaterRenderers.texture("axolotl", "grottenolm"));
        TEXTURES.put(Axolotl.Variant.WILD, WaterRenderers.texture("axolotl", "wildmolch"));
        TEXTURES.put(Axolotl.Variant.GOLD, WaterRenderers.texture("axolotl", "goldmolch"));
        TEXTURES.put(Axolotl.Variant.CYAN, WaterRenderers.texture("axolotl", "eismolch"));
        TEXTURES.put(Axolotl.Variant.BLUE, WaterRenderers.texture("axolotl", "blaumolch"));
    }

    public NewtRenderer(EntityRendererProvider.Context context) {
        super(context, new AdultAxolotlModel(context.bakeLayer(WaterRenderers.AXOLOTL)), 0.5f, new AxolotlRenderer(context));
        this.addLayer(new GlowLayer<>(this, state -> state.variant == Axolotl.Variant.BLUE ? BLUE_GLOW : null));
    }

    @Override
    public Identifier getTextureLocation(AxolotlRenderState state) {
        return TEXTURES.getOrDefault(state.variant, TEXTURES.get(Axolotl.Variant.LUCY));
    }

    @Override
    public AxolotlRenderState createRenderState() {
        return new AxolotlRenderState();
    }
}
