package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MissMystikEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MystikImageEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * Ein Spiegelbild MissMystiks: dasselbe Modell, nur ein wenig durchscheinend und mit einem kühlen Schimmer, der langsam
 * pulsiert – wer genau hinsieht, erkennt es. Wirft es eine Karte, fächert es dieselben Karten auf wie sie.
 */
public class MystikImageRenderer extends MobRenderer<MystikImageEntity, MissMystikRenderState, MissMystikModel> {
    public MystikImageRenderer(EntityRendererProvider.Context context) {
        super(context, new MissMystikModel(context.bakeLayer(ModModelLayers.MISS_MYSTIK)), 0.4f);
        this.addLayer(new MissMystikRenderer.GlowLayer(this));
    }

    @Override
    public Identifier getTextureLocation(MissMystikRenderState state) {
        return MissMystikRenderer.TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(MissMystikRenderState state, boolean isBodyVisible, boolean forceTransparent,
            boolean appearGlowing) {
        if (!isBodyVisible && !forceTransparent && !appearGlowing) {
            return null;
        }
        return RenderTypes.entityTranslucent(MissMystikRenderer.TEXTURE);
    }

    @Override
    protected int getModelTint(MissMystikRenderState state) {
        float pulse = 0.72f + Mth.sin(state.ageInTicks * 0.2f) * 0.08f;
        return ARGB.color(Math.round(pulse * 255.0f), 0xE8E0FF);
    }

    @Override
    public MissMystikRenderState createRenderState() {
        return new MissMystikRenderState();
    }

    @Override
    public void extractRenderState(MystikImageEntity entity, MissMystikRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        float since = entity.sinceThrow(partialTicks);
        boolean throwing = since >= 0.0f && since < MissMystikEntity.Attack.CARDS.duration;
        state.attack = throwing ? MissMystikEntity.Attack.CARDS.ordinal() : MissMystikEntity.Attack.NONE.ordinal();
        state.attackTime = throwing ? since : 0.0f;
        state.encore = false;
        state.hat = true;
        state.flap = Mth.lerp(partialTicks, entity.flapO, entity.flap);
        state.image = true;
        state.speed = (float) entity.getDeltaMovement().length();
        state.hasRedOverlay = false;
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }

    @Override
    protected int getBlockLightLevel(MystikImageEntity entity, BlockPos pos) {
        return Math.max(9, super.getBlockLightLevel(entity, pos));
    }
}
