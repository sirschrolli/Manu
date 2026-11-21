package io.github.sirschrolli.kingdomsatwar.client.renderer.serpent;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Der Kopf Quetzaras: Der Unterkiefer klappt auf, die Giftzähne schwingen dabei vom Gaumen herab, die Zunge züngelt; der
 * Federkamm richtet sich mit der Krause auf, und die dreizehn Federn der Krause stellen sich von angelegt (nach hinten) bis
 * gespreizt (wie die Haube einer Kobra) auf und zittern dabei. Im Schlaf decken Lider die Augen.
 */
public class QuetzaraModel extends EntityModel<QuetzaraRenderState> {
    public static final int RUFF = 13;
    private final ModelPart jaw;
    private final ModelPart fangs;
    private final ModelPart tongue;
    private final ModelPart crest;
    private final ModelPart lids;
    private final ModelPart[] ruff = new ModelPart[RUFF];

    public QuetzaraModel(ModelPart root) {
        super(root);
        ModelPart head = root.getChild("head");
        this.jaw = head.getChild("jaw");
        this.fangs = head.getChild("fangs");
        this.tongue = this.jaw.getChild("tongue");
        this.crest = head.getChild("crest");
        this.lids = head.getChild("lids");
        ModelPart collar = head.getChild("ruff");
        for (int k = 0; k < RUFF; k++) {
            this.ruff[k] = collar.getChild("ruff_" + k + "_base").getChild("ruff_" + k);
        }
    }

    @Override
    public void setupAnim(QuetzaraRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float jaw = Mth.clamp(state.jaw, 0.0f, 1.0f);
        this.jaw.xRot = jaw * 0.95f;
        // die Giftzähne liegen bei geschlossenem Maul zurückgeklappt am Gaumen
        this.fangs.xRot = (1.0f - Mth.clamp(jaw * 1.6f, 0.0f, 1.0f)) * 80.0f * Mth.DEG_TO_RAD;
        this.tongue.visible = state.tongue > 0.02f || jaw > 0.25f;
        this.tongue.z -= state.tongue * 7.5f;
        this.tongue.xRot = -0.08f + Mth.sin(t * 2.2f) * 0.06f * state.tongue;
        float ruff = Mth.clamp(state.ruff, 0.0f, 1.0f);
        this.crest.xRot = Mth.lerp(ruff, 18.0f, -16.0f) * Mth.DEG_TO_RAD + Mth.sin(t * 0.07f) * 0.03f;
        for (int k = 0; k < RUFF; k++) {
            float flutter = Mth.sin(t * 0.45f + k * 1.7f) * (1.5f + 3.5f * ruff);
            this.ruff[k].xRot = (Mth.lerp(ruff, -76.0f, -6.0f) + flutter) * Mth.DEG_TO_RAD;
        }
        this.lids.visible = state.asleep && !state.shed;
    }
}
