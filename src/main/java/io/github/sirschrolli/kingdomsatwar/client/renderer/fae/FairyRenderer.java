package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.entity.fae.FairyEntity;
import net.minecraft.client.model.animal.allay.AllayModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.AllayRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Fee: auf dem Skelett des Hilfsgeists (Schweben und Flügelschlag wie Vanilla), in vier Gestalten, mit leuchtenden
 * Augen, Blüten und Flügelflecken; nach einem Geschenk wirbelt sie im Freudentanz.
 */
public class FairyRenderer extends MobRenderer<FairyEntity, AllayRenderState, AllayModel> {
    private static final String[] LOOKS = {"rose", "azure", "gold", "violet"};
    private static final Identifier[] TEXTURES = new Identifier[LOOKS.length];
    private static final RenderType[] GLOW = new RenderType[LOOKS.length];

    static {
        for (int i = 0; i < LOOKS.length; i++) {
            TEXTURES[i] = KingdomsAtWar.id("textures/entity/fairy/" + LOOKS[i] + ".png");
            GLOW[i] = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/fairy/" + LOOKS[i] + "_glow.png"));
        }
    }

    /** Zeichenzustand mit der Gestalt. */
    public static class State extends AllayRenderState {
        public int look;
    }

    public FairyRenderer(EntityRendererProvider.Context context) {
        super(context, new AllayModel(context.bakeLayer(ModModelLayers.FAIRY)), 0.25f);
        this.addLayer(new GlowLayer<>(this, state -> GLOW[look(state)]));
    }

    private static int look(AllayRenderState state) {
        return state instanceof State s ? Mth.clamp(s.look, 0, LOOKS.length - 1) : 0;
    }

    @Override
    public Identifier getTextureLocation(AllayRenderState state) {
        return TEXTURES[look(state)];
    }

    @Override
    public AllayRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FairyEntity entity, AllayRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof State s) {
            s.look = entity.getVariant();
        }
        int dance = entity.getDance();
        state.isDancing = dance > 0;
        state.isSpinning = dance > 0;
        state.spinningProgress = dance > 0 ? Mth.clamp((FairyEntity.DANCE_TICKS - dance + partialTicks) / FairyEntity.DANCE_TICKS, 0.0f, 1.0f)
                : 0.0f;
        state.holdingAnimationProgress = 0.0f;
    }

    @Override
    protected void scale(AllayRenderState state, com.mojang.blaze3d.vertex.PoseStack poseStack) {
        poseStack.scale(1.15f, 1.15f, 1.15f);
    }

    @Override
    protected int getBlockLightLevel(FairyEntity entity, BlockPos pos) {
        return Math.max(10, super.getBlockLightLevel(entity, pos));
    }
}
