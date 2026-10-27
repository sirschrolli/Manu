package io.github.sirschrolli.kingdomsatwar.client.renderer.shadow;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.shadow.ShadeStalkerModel;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.ShadeStalkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * Schattenpirscher: durchscheinend – je nachdem, ob sein Opfer ihn ansieht, kaum zu erkennen oder deutlich. Die
 * violetten Augen glühen immer.
 */
public class ShadeStalkerRenderer extends MobRenderer<ShadeStalkerEntity, ShadowCreatureRenderState, ShadeStalkerModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/shade_stalker/shade_stalker.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/shade_stalker/shade_stalker_glow.png"));

    public ShadeStalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new ShadeStalkerModel(context.bakeLayer(ModModelLayers.SHADE_STALKER)), 0.5f);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public Identifier getTextureLocation(ShadowCreatureRenderState state) {
        return TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(ShadowCreatureRenderState state, boolean isBodyVisible, boolean forceTransparent,
            boolean appearGlowing) {
        if (!isBodyVisible && !forceTransparent && !appearGlowing) {
            return null;
        }
        return appearGlowing && !isBodyVisible ? RenderTypes.outline(TEXTURE) : RenderTypes.entityTranslucent(TEXTURE);
    }

    @Override
    protected int getModelTint(ShadowCreatureRenderState state) {
        return ARGB.color(Math.round(Mth.clamp(state.visibility, 0.0f, 1.0f) * 255.0f), 0xFFFFFF);
    }

    @Override
    public ShadowCreatureRenderState createRenderState() {
        return new ShadowCreatureRenderState();
    }

    @Override
    public void extractRenderState(ShadeStalkerEntity entity, ShadowCreatureRenderState state, float partialTicks) {
        // vor dem Rest, damit auch der Schatten am Boden schon die Sichtbarkeit kennt
        state.visibility = Mth.lerp(partialTicks, entity.visibilityO, entity.visibility);
        super.extractRenderState(entity, state, partialTicks);
        state.attack.copyFrom(entity.attackAnimation);
    }

    @Override
    protected float getShadowStrength(ShadowCreatureRenderState state) {
        return 0.25f + 0.75f * state.visibility;
    }
}
