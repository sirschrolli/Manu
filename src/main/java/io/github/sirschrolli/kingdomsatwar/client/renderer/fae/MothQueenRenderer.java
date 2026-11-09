package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MothQueenEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/** Mottenkönigin: 2,5-fach vergrößert, mit leuchtenden Augen, Krone und Augenflecken. */
public class MothQueenRenderer extends MobRenderer<MothQueenEntity, FaeRenderState, MothQueenModel> {
    public static final float SCALE = 2.5f;
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/moth_queen/moth_queen.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/moth_queen/moth_queen_glow.png"));

    public MothQueenRenderer(EntityRendererProvider.Context context) {
        super(context, new MothQueenModel(context.bakeLayer(ModModelLayers.MOTH_QUEEN)), 2.2f);
        this.addLayer(new GlowLayer<>(this, state -> GLOW));
    }

    @Override
    public Identifier getTextureLocation(FaeRenderState state) {
        return TEXTURE;
    }

    @Override
    public FaeRenderState createRenderState() {
        return new FaeRenderState();
    }

    @Override
    public void extractRenderState(MothQueenEntity entity, FaeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        MothQueenEntity.Attack attack = entity.currentAttack();
        state.attack = attack.ordinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.enraged = entity.isEnraged();
    }

    @Override
    protected void scale(FaeRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    protected int getBlockLightLevel(MothQueenEntity entity, BlockPos pos) {
        // sie schimmert selbst im Schatten der Bäume (sonst wirkt sie von unten fast schwarz)
        return Math.max(13, super.getBlockLightLevel(entity, pos));
    }
}
