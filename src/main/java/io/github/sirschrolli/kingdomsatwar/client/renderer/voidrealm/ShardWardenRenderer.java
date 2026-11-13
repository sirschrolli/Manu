package io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.voidrealm.ShardWardenModel;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShardWardenEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Splitterwächter: Kristalle, Herz und Sehschlitz leuchten – ruhend nur schwach wie eine Glut, wach hell; beim Aufladen
 * der Splittersalve und vor dem Erdstoß flammen sie auf.
 */
public class ShardWardenRenderer extends MobRenderer<ShardWardenEntity, VoidCreatureRenderState, ShardWardenModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/shard_warden/shard_warden.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/shard_warden/shard_warden_glow.png"));

    public ShardWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new ShardWardenModel(context.bakeLayer(ModModelLayers.SHARD_WARDEN)), 1.0f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, VoidCreatureRenderState state, float yRot,
                    float xRot) {
                float charge = Math.max(charge(state.volley, state.ageInTicks, ShardWardenEntity.VOLLEY_TICKS),
                        charge(state.slam, state.ageInTicks, ShardWardenEntity.SLAM_TICKS));
                float glow = Mth.clamp(0.22f + 0.6f * state.open + 0.18f * charge + Mth.sin(state.ageInTicks * 0.08f) * 0.05f, 0.0f, 1.0f);
                collector.order(1).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                        ARGB.color(Math.round(glow * 255.0f), 0xFFFFFF), null, state.outlineColor);
            }
        });
    }

    /** 0..1, solange die Animation läuft (aufsteigend bis zur Mitte). */
    private static float charge(net.minecraft.world.entity.AnimationState animation, float ageInTicks, int ticks) {
        if (!animation.isStarted()) {
            return 0.0f;
        }
        float s = animation.getTimeInMillis(ageInTicks) / 1000.0f;
        float end = ticks / 20.0f;
        return s < 0.0f || s > end ? 0.0f : Mth.sin(s / end * Mth.PI);
    }

    @Override
    public Identifier getTextureLocation(VoidCreatureRenderState state) {
        return TEXTURE;
    }

    @Override
    public VoidCreatureRenderState createRenderState() {
        return new VoidCreatureRenderState();
    }

    @Override
    public void extractRenderState(ShardWardenEntity entity, VoidCreatureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.open = Mth.lerp(partialTicks, entity.awakeO, entity.awake);
        state.attack.copyFrom(entity.punchAnimation);
        state.slam.copyFrom(entity.slamAnimation);
        state.volley.copyFrom(entity.volleyAnimation);
        state.awaken.copyFrom(entity.awakenAnimation);
    }
}
