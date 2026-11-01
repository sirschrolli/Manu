package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.wild.WildPandaModel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildPanda;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.model.animal.panda.PandaModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PandaRenderer;
import net.minecraft.client.renderer.entity.layers.PandaHoldsItemLayer;
import net.minecraft.client.renderer.entity.state.PandaRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.panda.Panda;

/** Panda: ein Fellbild je Wesenszug; Bambus in den Pfoten, Rollen, Sitzen und Rückenlage wie bei Vanilla. */
public class WildPandaRenderer extends WildRenderer<WildPanda, PandaRenderState, PandaModel> {
    private static final Map<Panda.Gene, Identifier> TEXTURES = new EnumMap<>(Panda.Gene.class);

    static {
        for (Panda.Gene gene : Panda.Gene.values()) {
            TEXTURES.put(gene, KingdomsAtWar.id("textures/entity/panda/panda_" + gene.getSerializedName() + ".png"));
        }
    }

    public WildPandaRenderer(EntityRendererProvider.Context context) {
        super(context, new WildPandaModel(context.bakeLayer(WildRenderers.PANDA)), 0.9f, new PandaRenderer(context));
        this.addLayer(new PandaHoldsItemLayer(this));
    }

    @Override
    public Identifier getTextureLocation(PandaRenderState state) {
        return TEXTURES.getOrDefault(state.variant, TEXTURES.get(Panda.Gene.NORMAL));
    }

    @Override
    public PandaRenderState createRenderState() {
        return new PandaRenderState();
    }

    /** Rollen, Sitzen und Rückenlage – genau wie bei Vanilla. */
    @Override
    protected void setupRotations(PandaRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        if (state.rollTime > 0.0f) {
            float transition = Mth.frac(state.rollTime);
            int rollPos = Mth.floor(state.rollTime);
            int nextRollPos = rollPos + 1;
            float y = state.isBaby ? 0.3f : 0.8f;
            if (rollPos < 8.0f) {
                float angle = angle(90.0f * rollPos / 7.0f, 90.0f * nextRollPos / 7.0f, nextRollPos, transition, 8.0f);
                poseStack.translate(0.0f, (y + 0.2f) * (angle / 90.0f), 0.0f);
                poseStack.rotateDegrees(Axis.XP, -angle);
            } else if (rollPos < 16.0f) {
                float counter = (rollPos - 8.0f) / 7.0f;
                float angle = angle(90.0f + 90.0f * counter, 90.0f + 90.0f * (nextRollPos - 8.0f) / 7.0f, nextRollPos, transition, 16.0f);
                poseStack.translate(0.0f, y + 0.2f + (y - 0.2f) * (angle - 90.0f) / 90.0f, 0.0f);
                poseStack.rotateDegrees(Axis.XP, -angle);
            } else if (rollPos < 24.0f) {
                float counter = (rollPos - 16.0f) / 7.0f;
                float angle = angle(180.0f + 90.0f * counter, 180.0f + 90.0f * (nextRollPos - 16.0f) / 7.0f, nextRollPos, transition, 24.0f);
                poseStack.translate(0.0f, y + y * (270.0f - angle) / 90.0f, 0.0f);
                poseStack.rotateDegrees(Axis.XP, -angle);
            } else if (rollPos < 32) {
                float counter = (rollPos - 24.0f) / 7.0f;
                float angle = angle(270.0f + 90.0f * counter, 270.0f + 90.0f * (nextRollPos - 24.0f) / 7.0f, nextRollPos, transition, 32.0f);
                poseStack.translate(0.0f, y * ((360.0f - angle) / 90.0f), 0.0f);
                poseStack.rotateDegrees(Axis.XP, -angle);
            }
        }
        float sitAmount = state.sitAmount;
        if (sitAmount > 0.0f) {
            poseStack.translate(0.0f, 0.8f * sitAmount, 0.0f);
            poseStack.rotateDegrees(Axis.XP, Mth.lerp(sitAmount, state.xRot, state.xRot + 90.0f));
            poseStack.translate(0.0f, -1.0f * sitAmount, 0.0f);
            if (state.isScared) {
                float shake = (float) (Math.cos(state.ageInTicks * 1.25f) * Math.PI * 0.05f);
                poseStack.rotateDegrees(Axis.YP, shake);
                if (state.isBaby) {
                    poseStack.translate(0.0f, 0.8f, 0.55f);
                }
            }
        }
        float lieOnBack = state.lieOnBackAmount;
        if (lieOnBack > 0.0f) {
            float y = state.isBaby ? 0.5f : 1.3f;
            poseStack.translate(0.0f, y * lieOnBack, 0.0f);
            poseStack.rotateDegrees(Axis.XP, Mth.lerp(lieOnBack, state.xRot, state.xRot + 180.0f));
        }
    }

    private static float angle(float from, float to, int next, float transition, float threshold) {
        return next < threshold ? Mth.lerp(transition, from, to) : from;
    }
}
