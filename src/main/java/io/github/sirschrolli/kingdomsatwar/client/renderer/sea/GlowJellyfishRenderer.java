package io.github.sirschrolli.kingdomsatwar.client.renderer.sea;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.sea.GlowJellyfishModel;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.entity.sea.GlowJellyfishEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/** Leuchtqualle: durchscheinend, mit leuchtenden Kanälen, Saum und Fäden und einem sanften Schein um die Glocke. */
public class GlowJellyfishRenderer extends MobRenderer<GlowJellyfishEntity, SeaRenderState, GlowJellyfishModel> {
    private static final String[] NAMES = {"blue", "pink", "gold"};
    private static final int[] HALO_COLORS = {0x46B8FF, 0xFF6AD8, 0xFFC04A};
    private static final Identifier[] TEXTURES = new Identifier[3];
    private static final RenderType[] GLOWS = new RenderType[3];

    static {
        for (int i = 0; i < 3; i++) {
            TEXTURES[i] = KingdomsAtWar.id("textures/entity/glow_jellyfish/glow_jellyfish_" + NAMES[i] + ".png");
            GLOWS[i] = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/glow_jellyfish/glow_jellyfish_" + NAMES[i] + "_glow.png"));
        }
    }

    public GlowJellyfishRenderer(EntityRendererProvider.Context context) {
        super(context, new GlowJellyfishModel(context.bakeLayer(ModModelLayers.GLOW_JELLYFISH)), 0.3f);
        this.addLayer(new GlowLayer<>(this, state -> GLOWS[Mth.clamp(state.variant, 0, 2)]));
    }

    @Override
    public Identifier getTextureLocation(SeaRenderState state) {
        return TEXTURES[Mth.clamp(state.variant, 0, 2)];
    }

    @Override
    protected @Nullable RenderType getRenderType(SeaRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        Identifier texture = this.getTextureLocation(state);
        if (!isBodyVisible && !forceTransparent && !appearGlowing) {
            return null;
        }
        return appearGlowing && !isBodyVisible ? RenderTypes.outline(texture) : RenderTypes.entityTranslucent(texture);
    }

    @Override
    public SeaRenderState createRenderState() {
        return new SeaRenderState();
    }

    @Override
    public void extractRenderState(GlowJellyfishEntity entity, SeaRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant();
        state.sincePulse = entity.tickCount - entity.pulseTick + partialTicks;
        state.inWater = entity.isInWater();
    }

    @Override
    public void submit(SeaRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (!state.isInvisible && state.deathTime <= 0.0f) {
            // ein weicher Schein um die Glocke, der beim Pumpen kurz aufflackert
            float pulse = state.sincePulse < 30.0f ? (1.0f - state.sincePulse / 30.0f) * 0.25f : 0.0f;
            float strength = 0.32f + 0.06f * Mth.sin(state.ageInTicks * 0.1f) + pulse;
            SeaGlow.halo(poseStack, collector, camera.orientation, new Vector3f(0.0f, 0.82f, 0.0f), 1.5f,
                    HALO_COLORS[Mth.clamp(state.variant, 0, 2)], strength);
        }
    }

    @Override
    protected float getShadowStrength(SeaRenderState state) {
        return 0.2f;
    }
}
