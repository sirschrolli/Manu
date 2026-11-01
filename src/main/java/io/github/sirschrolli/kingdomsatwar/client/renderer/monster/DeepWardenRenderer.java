package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.client.model.monster.warden.WardenModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WardenRenderer;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Tiefenhüter: eigenes Modell (Hörner, Rippenkäfig, Klauen) und Bild. Herz, Adern, Flecken und Hornspitzen leuchten
 * aus dem eigenen Leuchtbild und pochen mit dem Herzschlag (der schneller schlägt, je wütender er ist); die Hörner
 * zucken wie Vanillas Fühler.
 */
public class DeepWardenRenderer extends WardenRenderer {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/warden/tiefenhueter.png");
    private static final Identifier GLOW = KingdomsAtWar.id("textures/entity/warden/tiefenhueter_glow.png");

    public DeepWardenRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new WardenModel(context.bakeLayer(MonsterRenderers.WARDEN));
        this.layers.clear();                                   // Vanillas Leucht-Ebenen gehören zu Vanillas Bild
        this.addLayer(new LivingEntityEmissiveLayer<>(this, state -> GLOW,
                (state, ageInTicks) -> Mth.clamp(0.45f + 0.55f * state.heartAnimation + Math.max(0.0f, Mth.cos(ageInTicks * 0.045f)) * 0.15f,
                        0.0f, 1.0f),
                new WardenModel(context.bakeLayer(MonsterRenderers.WARDEN)), RenderTypes::entityTranslucentEmissive, false));
    }

    @Override
    public Identifier getTextureLocation(WardenRenderState state) {
        return TEXTURE;
    }
}
