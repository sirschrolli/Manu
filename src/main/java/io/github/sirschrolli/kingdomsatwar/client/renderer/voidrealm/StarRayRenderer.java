package io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.voidrealm.StarRayModel;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarRayEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/** Sternenrochen: Sternbilder und Augen leuchten, die Spitzen der Schwingen sind durchscheinend. */
public class StarRayRenderer extends MobRenderer<StarRayEntity, VoidCreatureRenderState, StarRayModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/star_ray/star_ray.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/star_ray/star_ray_glow.png"));

    public StarRayRenderer(EntityRendererProvider.Context context) {
        super(context, new StarRayModel(context.bakeLayer(ModModelLayers.STAR_RAY)), 1.4f);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public Identifier getTextureLocation(VoidCreatureRenderState state) {
        return TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(VoidCreatureRenderState state, boolean isBodyVisible, boolean forceTransparent,
            boolean appearGlowing) {
        if (!isBodyVisible && !forceTransparent && !appearGlowing) {
            return null;
        }
        return appearGlowing && !isBodyVisible ? RenderTypes.outline(TEXTURE) : RenderTypes.entityTranslucent(TEXTURE);
    }

    @Override
    public VoidCreatureRenderState createRenderState() {
        return new VoidCreatureRenderState();
    }

    @Override
    public void extractRenderState(StarRayEntity entity, VoidCreatureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.flap = Mth.lerp(partialTicks, entity.flapO, entity.flap);
        state.bank = Mth.lerp(partialTicks, entity.bankO, entity.bank);
        state.climb = (float) entity.getDeltaMovement().y;
    }

    @Override
    protected float getShadowStrength(VoidCreatureRenderState state) {
        return 0.3f;
    }
}
