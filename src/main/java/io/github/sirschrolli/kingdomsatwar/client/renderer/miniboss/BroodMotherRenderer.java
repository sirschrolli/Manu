package io.github.sirschrolli.kingdomsatwar.client.renderer.miniboss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.LookRenderState;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.MonsterRenderers;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.WildSpiderRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.miniboss.BroodMotherEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;

/**
 * Die Brutmutter: das Spinnenmodell der Mod in ihrem eigenen Bild (schwarzviolettes Chitin, der Totenschädel auf dem
 * Hinterleib), 2,3-mal so groß; vor der Netzsalve und dem Sprung bäumt sie sich auf (der Vorderleib hebt sich um die
 * hinteren Beine). Im Arachnophobie-Modus wird auch sie zum Moosling.
 */
public class BroodMotherRenderer extends WildSpiderRenderer<BroodMotherEntity> {
    /** So weit bäumt sie sich auf (Grad). */
    private static final float REAR = 34.0f;

    public static class State extends LookRenderState {
        public float rear;
    }

    public BroodMotherRenderer(EntityRendererProvider.Context context) {
        super(context, MonsterRenderers.SPIDER, 0.8f, new String[] {"brutmutter"}, spider -> 0, new SpiderRenderer<BroodMotherEntity>(context), false);
    }

    @Override
    public LookRenderState createRenderState() {
        return new State();
    }

    @Override
    protected void extract(BroodMotherEntity spider, LookRenderState state, float partialTicks) {
        super.extract(spider, state, partialTicks);
        if (state instanceof State s) {
            s.rear = spider.rear(partialTicks);
        }
    }

    @Override
    protected void setupRotations(LookRenderState state, PoseStack poseStack, float bodyRot, float scale) {
        super.setupRotations(state, poseStack, bodyRot, scale);
        if (state instanceof State s && s.rear > 0.001f && state.deathTime <= 0.0f) {
            // um die hinteren Beine kippen: der Vorderleib (lokal -Z) hebt sich
            poseStack.translate(0.0f, 0.0f, 0.55f);
            poseStack.rotate(Axis.XP.rotationDegrees(REAR * s.rear));
            poseStack.translate(0.0f, 0.0f, -0.55f);
        }
    }
}
