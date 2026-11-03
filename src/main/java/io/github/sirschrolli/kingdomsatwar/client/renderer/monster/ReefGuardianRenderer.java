package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.client.renderer.entity.state.GuardianRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Riffwächter und Uralter Riffwächter: eigenes Modell und Bild, das große Auge leuchtet; Strahl, Stacheln, Schwanz
 * und Blick erbt er vom Vanilla-Renderer.
 */
public class ReefGuardianRenderer extends GuardianRenderer {
    private final Identifier texture;

    public ReefGuardianRenderer(EntityRendererProvider.Context context, float shadow, ModelLayerLocation layer, String name) {
        super(context, shadow, layer);
        this.texture = KingdomsAtWar.id("textures/entity/guardian/" + name + ".png");
        RenderType glow = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/guardian/" + name + "_glow.png"));
        this.addLayer(new GlowLayer<>(this, state -> glow));
    }

    @Override
    public Identifier getTextureLocation(GuardianRenderState state) {
        return this.texture;
    }
}
