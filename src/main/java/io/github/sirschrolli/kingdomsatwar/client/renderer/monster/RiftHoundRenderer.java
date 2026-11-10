package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.monster.RiftHoundEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.PoseStack;

/** Rissjäger mit glühenden Rissen, Sternsprenkeln und schwebenden Splittern; beim Phasensprung fällt er in sich zusammen. */
public class RiftHoundRenderer extends MobRenderer<RiftHoundEntity, RiftHoundRenderState, RiftHoundModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/rift_hound/rift_hound.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/rift_hound/rift_hound_glow.png"));

    public RiftHoundRenderer(EntityRendererProvider.Context context) {
        super(context, new RiftHoundModel(context.bakeLayer(ModModelLayers.RIFT_HOUND)), 0.5f);
        this.addLayer(new GlowLayer<>(this, state -> GLOW));
    }

    @Override
    public Identifier getTextureLocation(RiftHoundRenderState state) {
        return TEXTURE;
    }

    @Override
    public RiftHoundRenderState createRenderState() {
        return new RiftHoundRenderState();
    }

    @Override
    public void extractRenderState(RiftHoundEntity entity, RiftHoundRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.bite.copyFrom(entity.biteAnimation);
        state.howl.copyFrom(entity.howlAnimation);
        state.fade = entity.fade(partialTicks);
    }

    @Override
    protected void scale(RiftHoundRenderState state, PoseStack poseStack) {
        // beim Phasensprung: schmal und lang gezogen, bis nur ein Flimmern bleibt
        if (state.fade > 0.0f) {
            float f = state.fade;
            poseStack.scale(Math.max(0.04f, 1.0f - f), 1.0f + f * 0.6f, Math.max(0.04f, 1.0f - f * 0.8f));
        }
    }
}
