package io.github.sirschrolli.kingdomsatwar.client.model.sea;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.sea.SeaRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Leuchtqualle: bei jedem Pumpstoß zieht sich die Glocke zusammen (schmaler und höher), der Saum schlägt nach unten und
 * die Fäden werden nach hinten gerissen; danach weitet sie sich langsam wieder, die Fäden schweben in sanften Wellen nach.
 * Zwischen den Stößen atmet die Glocke ganz leicht.
 */
public class GlowJellyfishModel extends EntityModel<SeaRenderState> {
    private final ModelPart bell;
    private final ModelPart rim;
    private final ModelPart[] arms = new ModelPart[4];
    private final ModelPart[] armsLow = new ModelPart[4];
    private final ModelPart[][] tentacles = new ModelPart[8][3];

    public GlowJellyfishModel(ModelPart root) {
        super(root);
        this.bell = root.getChild("bell");
        this.rim = this.bell.getChild("rim");
        for (int k = 0; k < 4; k++) {
            this.arms[k] = this.bell.getChild("arm_" + k);
            this.armsLow[k] = this.arms[k].getChild("arm_" + k + "_low");
        }
        for (int i = 0; i < 8; i++) {
            ModelPart base = this.bell.getChild("tentacle_" + i);
            ModelPart mid = base.getChild("tentacle_" + i + "_mid");
            this.tentacles[i][0] = base;
            this.tentacles[i][1] = mid;
            this.tentacles[i][2] = mid.getChild("tentacle_" + i + "_tip");
        }
    }

    @Override
    public void setupAnim(SeaRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        // Pumpstoß: schnelles Zusammenziehen (4 Ticks), langsames Weiten (bis 30 Ticks)
        float p = state.sincePulse;
        float squeeze = p < 4.0f ? Mth.sin(p / 4.0f * Mth.HALF_PI) : p < 30.0f ? Mth.cos((p - 4.0f) / 26.0f * Mth.HALF_PI) : 0.0f;
        float breathe = Mth.sin(t * 0.12f) * 0.025f;
        this.bell.xScale = this.bell.zScale = 1.0f - squeeze * 0.16f + breathe;
        this.bell.yScale = 1.0f + squeeze * 0.12f - breathe;
        this.bell.y += -squeeze * 0.8f + Mth.sin(t * 0.05f) * 0.6f;
        this.bell.xRot += Mth.sin(t * 0.031f) * 0.06f;
        this.bell.zRot += Mth.sin(t * 0.027f + 1.0f) * 0.06f;
        // der Saum schlägt beim Zusammenziehen nach innen
        this.rim.xScale = this.rim.zScale = 1.0f - squeeze * 0.12f;
        this.rim.y += squeeze * 0.4f;
        // Mundarme: träge, gegeneinander versetzt
        for (int k = 0; k < 4; k++) {
            float a = (45.0f + 90.0f * k) * Anim.DEG;
            float wave = Mth.sin(t * 0.09f + k * 1.6f);
            float out = 0.06f + squeeze * 0.1f;
            this.arms[k].xRot += Mth.sin(a) * out + wave * 0.06f;
            this.arms[k].zRot -= Mth.cos(a) * out;
            this.arms[k].yRot += Mth.sin(t * 0.05f + k) * 0.15f;
            this.armsLow[k].xRot += wave * 0.12f + squeeze * 0.15f;
        }
        // Fangfäden: Wellen von der Glocke zur Spitze; beim Stoß nach außen und hinten gerissen
        for (int i = 0; i < 8; i++) {
            float a = (22.5f + 45.0f * i) * Anim.DEG;
            float phase = i * 0.8f;
            for (int seg = 0; seg < 3; seg++) {
                ModelPart part = this.tentacles[i][seg];
                float amp = 0.06f + seg * 0.06f;
                float w = t * 0.1f - seg * 0.75f + phase;
                float spread = seg == 0 ? 0.08f + squeeze * 0.28f : -squeeze * 0.12f * seg;
                part.xRot += Mth.sin(a) * spread + Mth.sin(w) * amp;
                part.zRot -= Mth.cos(a) * spread - Mth.cos(w * 0.8f + 1.3f) * amp * 0.8f;
            }
        }
    }
}
