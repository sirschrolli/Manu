package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Ooze;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractCubeMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Schleime: der Kern mit Gesicht und Einschlüssen deckend, darüber die durchscheinende Hülle (beides aus einem Bild je
 * Art); Größe und Wackeln wie bei Vanilla, die Kristallsplitter des Höhlenschleims leuchten.
 */
public class OozeRenderer extends AbstractCubeMobRenderer<Ooze, OozeRenderState, SlimeModel> {
    private static final String[] NAMES = {"schleim_sumpf", "schleim_hoehle"};
    private static final Identifier[] TEXTURES = MonsterRenderers.textures("slime", NAMES);
    private static final RenderType[] GLOWS = MonsterRenderers.glows("slime", NAMES);

    public OozeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel(context.bakeLayer(MonsterRenderers.SLIME)));
        this.addLayer(new Shell(this, context.getModelSet()));
        this.addLayer(new GlowLayer<>(this, state -> GLOWS[state.look]));
    }

    @Override
    protected void scale(OozeRenderState state, PoseStack poseStack) {
        this.downscaleSlightly(poseStack);
        super.scale(state, poseStack);
    }

    @Override
    public OozeRenderState createRenderState() {
        return new OozeRenderState();
    }

    @Override
    public void extractRenderState(Ooze entity, OozeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.look = Mth.clamp(entity.getLook().ordinal(), 0, NAMES.length - 1);
    }

    @Override
    public Identifier getTextureLocation(OozeRenderState state) {
        return TEXTURES[state.look];
    }

    /** Die durchscheinende Hülle (wie Vanillas SlimeOuterLayer, mit dem eigenen Bild). */
    private static final class Shell extends RenderLayer<OozeRenderState, SlimeModel> {
        private final SlimeModel model;

        Shell(RenderLayerParent<OozeRenderState, SlimeModel> parent, EntityModelSet models) {
            super(parent);
            this.model = new SlimeModel(models.bakeLayer(MonsterRenderers.SLIME_OUTER));
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, OozeRenderState state, float yRot,
                float xRot) {
            boolean outlineOnly = state.appearsGlowing() && state.isInvisible;
            if (state.isInvisible && !outlineOnly) {
                return;
            }
            Identifier texture = TEXTURES[state.look];
            int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0f);
            collector.order(1).submitModel(this.model, state, poseStack,
                    outlineOnly ? RenderTypes.outline(texture) : RenderTypes.entityTranslucent(texture), lightCoords, overlay,
                    state.outlineColor);
        }
    }
}
