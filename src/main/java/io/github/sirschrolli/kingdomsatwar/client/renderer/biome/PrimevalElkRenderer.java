package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.PrimevalElkEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Urelch: Bullen mit Schaufeln, Sattel und Truhen sichtbar, wenn er sie trägt; leuchtende Beeren an den Ranken. */
public class PrimevalElkRenderer extends MobRenderer<PrimevalElkEntity, PrimevalElkRenderState, PrimevalElkModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/primeval_elk/primeval_elk.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/primeval_elk/primeval_elk_glow.png"));

    public PrimevalElkRenderer(EntityRendererProvider.Context context) {
        super(context, new PrimevalElkModel(context.bakeLayer(ModModelLayers.PRIMEVAL_ELK)), 1.1f);
        this.addLayer(new GlowLayer<>(this, state -> state.bull && !state.isBaby ? GLOW : null));
    }

    @Override
    public Identifier getTextureLocation(PrimevalElkRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(PrimevalElkRenderState state, PoseStack poseStack) {
        if (state.isBaby) {
            poseStack.scale(state.ageScale, state.ageScale, state.ageScale);
        }
    }

    @Override
    public PrimevalElkRenderState createRenderState() {
        return new PrimevalElkRenderState();
    }

    @Override
    public void extractRenderState(PrimevalElkEntity entity, PrimevalElkRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.bull = entity.isBull();
        state.saddled = entity.isSaddled();
        state.chested = entity.hasChest();
        state.eat = entity.getEatAnim(partialTicks);
        state.stand = entity.getStandAnim(partialTicks);
        state.mouth = entity.getMouthAnim(partialTicks);
        float age = state.ageInTicks;
        state.charge = Anim.follow(entity, 0, entity.isCharging() ? 1.0f : 0.0f, age, 4.0f);
        double moved = Math.hypot(entity.getX() - entity.xo, entity.getZ() - entity.zo);
        state.trot = Anim.follow(entity, 1, moved > 0.16 ? 1.0f : 0.0f, age, 3.0f);
        state.bugle.copyFrom(entity.bugleAnimation);
        state.impact.copyFrom(entity.impactAnimation);
    }
}
