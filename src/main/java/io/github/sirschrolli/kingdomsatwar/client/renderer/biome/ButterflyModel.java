package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Schmetterling: im Flug schlagen die Flügel schnell auf und ab (die Hinterflügel einen Hauch später), der Leib wippt; im
 * Sitzen sind die Flügel über dem Rücken zusammengeklappt und öffnen sich ab und zu langsam zum Sonnen. Die Fühler zittern.
 */
public class ButterflyModel extends EntityModel<ButterflyRenderState> {
    private final ModelPart body;
    private final ModelPart antennaL;
    private final ModelPart antennaR;
    private final ModelPart wingL;
    private final ModelPart wingR;
    private final ModelPart hindL;
    private final ModelPart hindR;

    public ButterflyModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        ModelPart head = this.body.getChild("head");
        this.antennaL = head.getChild("antenna_l");
        this.antennaR = head.getChild("antenna_r");
        this.wingL = this.body.getChild("wing_l");
        this.wingR = this.body.getChild("wing_r");
        this.hindL = this.body.getChild("hind_l");
        this.hindR = this.body.getChild("hind_r");
    }

    @Override
    public void setupAnim(ButterflyRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks + state.offset;
        float perch = state.perch;
        float fly = 1.0f - perch;
        // im Flug: schnelle Schläge (oben weit geöffnet, unten fast flach)
        float beat = Mth.sin(t * 1.9f);
        float flapUp = (-0.15f - (beat * 0.5f + 0.5f) * 1.25f) * fly;
        float flapHind = (-0.12f - (Mth.sin(t * 1.9f - 0.35f) * 0.5f + 0.5f) * 1.1f) * fly;
        // im Sitzen: zusammengeklappt (senkrecht), ab und zu langsam auf und wieder zu
        float bask = state.openWings >= 0.0f ? state.openWings : Math.max(0.0f, Mth.sin(t * 0.05f) - 0.4f) / 0.6f;
        float closed = (-1.5f + bask * 1.25f + Mth.sin(t * 0.3f) * 0.04f) * perch;
        this.wingL.zRot += flapUp + closed;
        this.wingR.zRot -= flapUp + closed;
        this.hindL.zRot += flapHind + closed;
        this.hindR.zRot -= flapHind + closed;
        // Leib wippt im Flug, leicht schräg nach oben
        this.body.y += Mth.sin(t * 1.9f + 1.2f) * 0.5f * fly - 1.5f * fly;
        this.body.xRot += -0.25f * fly;
        // Fühler zittern
        this.antennaL.xRot += Mth.sin(t * 0.7f) * 0.08f;
        this.antennaR.xRot += Mth.sin(t * 0.7f + 1.3f) * 0.08f;
    }
}
