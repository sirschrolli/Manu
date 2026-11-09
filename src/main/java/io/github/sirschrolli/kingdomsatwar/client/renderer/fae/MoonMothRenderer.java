package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.shadow.GlimmerMothModel;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.client.renderer.shadow.ShadowCreatureRenderState;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MoonMothEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/** Mondfalter: der Körper des Glimmerfalters in Flieder, die Augenflecken glühen rosa; etwas größer. */
public class MoonMothRenderer extends MobRenderer<MoonMothEntity, ShadowCreatureRenderState, GlimmerMothModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/glimmer_moth/moon_moth.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/glimmer_moth/moon_moth_glow.png"));

    public MoonMothRenderer(EntityRendererProvider.Context context) {
        super(context, new GlimmerMothModel(context.bakeLayer(ModModelLayers.GLIMMER_MOTH)), 0.2f);
        this.addLayer(new GlowLayer<>(this, state -> GLOW));
    }

    @Override
    public Identifier getTextureLocation(ShadowCreatureRenderState state) {
        return TEXTURE;
    }

    @Override
    public ShadowCreatureRenderState createRenderState() {
        return new ShadowCreatureRenderState();
    }

    @Override
    protected void scale(ShadowCreatureRenderState state, PoseStack poseStack) {
        poseStack.scale(1.6f, 1.6f, 1.6f);
    }

    @Override
    protected int getBlockLightLevel(MoonMothEntity entity, BlockPos pos) {
        return Math.max(10, super.getBlockLightLevel(entity, pos));
    }
}
