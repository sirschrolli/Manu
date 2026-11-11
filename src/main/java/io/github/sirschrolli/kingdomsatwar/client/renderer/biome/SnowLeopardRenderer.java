package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.SnowLeopardEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/** Schneeleopard: zwei Fellfarben; Junge halb so groß mit großem Kopf. */
public class SnowLeopardRenderer extends MobRenderer<SnowLeopardEntity, SnowLeopardRenderState, SnowLeopardModel> {
    private static final Identifier[] TEXTURES = {
            KingdomsAtWar.id("textures/entity/snow_leopard/snow_leopard.png"),
            KingdomsAtWar.id("textures/entity/snow_leopard/snow_leopard_smoky.png")};

    public SnowLeopardRenderer(EntityRendererProvider.Context context) {
        super(context, new SnowLeopardModel(context.bakeLayer(ModModelLayers.SNOW_LEOPARD)), 0.55f);
    }

    @Override
    public Identifier getTextureLocation(SnowLeopardRenderState state) {
        return TEXTURES[Math.floorMod(state.variant, TEXTURES.length)];
    }

    @Override
    protected void scale(SnowLeopardRenderState state, PoseStack poseStack) {
        if (state.isBaby) {
            poseStack.scale(state.ageScale, state.ageScale, state.ageScale);
        }
    }

    @Override
    public SnowLeopardRenderState createRenderState() {
        return new SnowLeopardRenderState();
    }

    @Override
    public void extractRenderState(SnowLeopardEntity entity, SnowLeopardRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant();
        state.tame = entity.isTame();
        float age = state.ageInTicks;
        state.stalk = Anim.follow(entity, 0, entity.isStalking() ? 1.0f : 0.0f, age, 5.0f);
        state.lie = Anim.follow(entity, 1, entity.isLying() ? 1.0f : 0.0f, age, 3.0f);
        // rennen: mehr als knapp vier Blöcke je Sekunde (die Bewegung auf dem Bildschirm, nicht die übertragene Geschwindigkeit)
        double moved = Math.hypot(entity.getX() - entity.xo, entity.getZ() - entity.zo);
        state.run = Anim.follow(entity, 2, moved > 0.19 ? 1.0f : 0.0f, age, 4.0f);
        state.pounce.copyFrom(entity.pounceAnimation);
        state.swipe.copyFrom(entity.swipeAnimation);
        state.eat.copyFrom(entity.eatAnimation);
    }
}
