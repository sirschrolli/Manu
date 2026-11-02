package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import net.minecraft.client.model.monster.endermite.EndermiteModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Endermite;

/** Leermilbe: eigenes Modell auf den Segmenten der Endermite (sie schlängelt sich wie diese); Fugen, Augen und Dornen leuchten. */
public class LeermilbeRenderer extends MobRenderer<Endermite, LivingEntityRenderState, EndermiteModel> {
    private static final Identifier TEXTURE = EndRenderers.texture("endermite", "leermilbe");
    private static final RenderType GLOW = RenderTypes.eyes(EndRenderers.texture("endermite", "leermilbe_glow"));

    public LeermilbeRenderer(EntityRendererProvider.Context context) {
        super(context, new EndermiteModel(context.bakeLayer(EndRenderers.LEERMILBE)), 0.3f);
        this.addLayer(new GlowLayer<>(this, state -> GLOW));
    }

    @Override
    protected float getFlipDegrees() {
        return 180.0f;
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}
