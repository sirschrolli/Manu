package io.github.sirschrolli.kingdomsatwar.client.renderer.water;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.Puffer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.fish.PufferfishBigModel;
import net.minecraft.client.model.animal.fish.PufferfishMidModel;
import net.minecraft.client.model.animal.fish.PufferfishSmallModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PufferfishRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PufferfishRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Kugelfisch in drei Stufen wie bei Vanilla: schlank, halb aufgebläht und als Stachelkugel – jede Stufe mit eigenem
 * Modell und eigener Textur (die Stacheln wachsen beim Aufblähen heraus).
 */
public class PufferRenderer extends WildRenderer<Puffer, PufferfishRenderState, EntityModel<EntityRenderState>> {
    private static final Identifier SMALL_TEXTURE = WaterRenderers.texture("pufferfish", "kugelfisch_klein");
    private static final Identifier MID_TEXTURE = WaterRenderers.texture("pufferfish", "kugelfisch_mittel");
    private static final Identifier BIG_TEXTURE = WaterRenderers.texture("pufferfish", "kugelfisch_gross");
    private final EntityModel<EntityRenderState> small;
    private final EntityModel<EntityRenderState> mid;
    private final EntityModel<EntityRenderState> big;

    public PufferRenderer(EntityRendererProvider.Context context) {
        super(context, new PufferfishBigModel(context.bakeLayer(WaterRenderers.PUFFER_BIG)), 0.2f, new PufferfishRenderer(context));
        this.big = this.getModel();
        this.mid = new PufferfishMidModel(context.bakeLayer(WaterRenderers.PUFFER_MID));
        this.small = new PufferfishSmallModel(context.bakeLayer(WaterRenderers.PUFFER_SMALL));
    }

    @Override
    public Identifier getTextureLocation(PufferfishRenderState state) {
        return switch (state.puffState) {
            case 0 -> SMALL_TEXTURE;
            case 1 -> MID_TEXTURE;
            default -> BIG_TEXTURE;
        };
    }

    @Override
    public PufferfishRenderState createRenderState() {
        return new PufferfishRenderState();
    }

    @Override
    protected float getShadowRadius(PufferfishRenderState state) {
        return 0.1f + 0.1f * state.puffState;
    }

    @Override
    public void submit(PufferfishRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        this.model = switch (state.puffState) {
            case 0 -> this.small;
            case 1 -> this.mid;
            default -> this.big;
        };
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    protected void setupRotations(PufferfishRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        poseStack.translate(0.0f, Mth.cos(state.ageInTicks * 0.05f) * 0.08f, 0.0f);
        super.setupRotations(state, poseStack, bodyRot, entityScale);
    }
}
