package io.github.sirschrolli.kingdomsatwar.client.renderer.water;

import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.Polliwog;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TadpoleRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Kaulquappe: golden gesprenkelter Kopfleib und ein durchscheinender Flossensaum, der wie bei Vanilla hin und her schlägt. */
public class PolliwogRenderer extends WildRenderer<Polliwog, LivingEntityRenderState, PolliwogRenderer.Model> {
    private static final Identifier TEXTURE = WaterRenderers.texture("tadpole", "kaulquappe");

    /** Wie das Vanilla-Modell, aber durchscheinend gezeichnet (der Flossensaum ist halb durchsichtig). */
    public static final class Model extends EntityModel<LivingEntityRenderState> {
        private final ModelPart tail;

        Model(ModelPart root) {
            super(root, RenderTypes::entityTranslucent);
            this.tail = root.getChild("tail");
        }

        @Override
        public void setupAnim(LivingEntityRenderState state) {
            super.setupAnim(state);
            float amplitude = state.isInWater ? 1.0f : 1.5f;
            this.tail.yRot = -amplitude * 0.25f * Mth.sin(0.3f * state.ageInTicks);
        }
    }

    public PolliwogRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(WaterRenderers.TADPOLE)), 0.14f, new TadpoleRenderer(context));
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}
