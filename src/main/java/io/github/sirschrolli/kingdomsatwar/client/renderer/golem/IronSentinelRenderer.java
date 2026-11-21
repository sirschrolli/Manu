package io.github.sirschrolli.kingdomsatwar.client.renderer.golem;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.entity.creature.golem.IronSentinel;
import java.util.Map;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.IronGolemFlowerLayer;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.animal.golem.IronGolem;

/**
 * Eisenwächter: eigenes Modell und Bild je Gestalt. Augen, Ofengitter und die Glut im Rauchabzug leuchten aus dem
 * eigenen Leuchtbild und flackern wie ein Schmiedefeuer; die Risse sind eigene Bilder für das eigene Modell. Gehen,
 * Schlag, Wanken und die Mohnblume in der Hand kommen vom Vanilla-Renderer.
 */
public class IronSentinelRenderer extends IronGolemRenderer {
    private static final String[] LOOKS = {"schmiedeeisen", "mooswaechter", "frostwaechter", "rostwaechter"};
    private static final Identifier[] TEXTURES = GolemRenderers.textures("iron_golem", "", LOOKS);
    private static final Identifier[] GLOW = GolemRenderers.textures("iron_golem", "_glow", LOOKS);
    private static final Map<Crackiness.Level, Identifier> CRACKS = Map.of(
            Crackiness.Level.LOW, GolemRenderers.texture("iron_golem", "risse_low"),
            Crackiness.Level.MEDIUM, GolemRenderers.texture("iron_golem", "risse_medium"),
            Crackiness.Level.HIGH, GolemRenderers.texture("iron_golem", "risse_high"));

    public static class State extends IronGolemRenderState {
        public int look;
    }

    public IronSentinelRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new IronGolemModel(context.bakeLayer(GolemRenderers.IRON_GOLEM));
        this.layers.clear();                                   // Vanillas Risse gehören zu Vanillas Bild
        this.addLayer(new Cracks(this));
        this.addLayer(new IronGolemFlowerLayer(this));
        this.addLayer(new LivingEntityEmissiveLayer<>(this, state -> GLOW[look(state)],
                (state, ageInTicks) -> Mth.clamp(0.78f + 0.14f * Mth.sin(ageInTicks * 0.31f) + 0.08f * Mth.sin(ageInTicks * 1.7f + 1.3f), 0.0f, 1.0f),
                new IronGolemModel(context.bakeLayer(GolemRenderers.IRON_GOLEM)), RenderTypes::entityTranslucentEmissive, false));
    }

    private static int look(IronGolemRenderState state) {
        return state instanceof State s ? Mth.clamp(s.look, 0, LOOKS.length - 1) : 0;
    }

    @Override
    public IronGolemRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(IronGolem entity, IronGolemRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof State s) {
            s.look = entity instanceof IronSentinel sentinel ? sentinel.getLook().ordinal() : 0;
        }
    }

    @Override
    public Identifier getTextureLocation(IronGolemRenderState state) {
        return TEXTURES[look(state)];
    }

    /** Risse je nach Schaden – wie Vanillas IronGolemCrackinessLayer, nur mit den eigenen Bildern. */
    private static final class Cracks extends RenderLayer<IronGolemRenderState, IronGolemModel> {
        Cracks(RenderLayerParent<IronGolemRenderState, IronGolemModel> parent) {
            super(parent);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, IronGolemRenderState state, float yRot, float xRot) {
            if (!state.isInvisible && state.crackiness != Crackiness.Level.NONE) {
                renderColoredCutoutModel(this.getParentModel(), CRACKS.get(state.crackiness), poseStack, collector, lightCoords, state, -1, 1);
            }
        }
    }
}
