package io.github.sirschrolli.kingdomsatwar.client.model.voidrealm;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm.VoidCreatureRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Leerenschlund: die Glocke pumpt (zieht sich zusammen und weitet sich, der Saum schlägt aus), die Fangfäden wellen
 * nach; beim Ansaugen klappen die vier Lippen weit auf und flattern, beim Zuschnappen schlagen sie zu.
 */
public class VoidMawModel extends EntityModel<VoidCreatureRenderState> {
    private static final float CLOSED = 0.5f;
    private static final float OPEN = -0.8f;

    private final ModelPart bell;
    private final ModelPart[] skirt = new ModelPart[4];
    private final ModelPart lipN;
    private final ModelPart lipS;
    private final ModelPart lipW;
    private final ModelPart lipE;
    private final ModelPart[] arms = new ModelPart[4];
    private final ModelPart[] armTips = new ModelPart[4];
    private final ModelPart[][] tentacles = new ModelPart[8][3];

    public VoidMawModel(ModelPart root) {
        super(root);
        this.bell = root.getChild("bell");
        String[] skirts = {"skirt_n", "skirt_e", "skirt_s", "skirt_w"};
        for (int i = 0; i < 4; i++) {
            this.skirt[i] = this.bell.getChild(skirts[i]);
        }
        ModelPart maw = this.bell.getChild("maw");
        this.lipN = maw.getChild("lip_n");
        this.lipS = maw.getChild("lip_s");
        this.lipW = maw.getChild("lip_w");
        this.lipE = maw.getChild("lip_e");
        for (int k = 0; k < 4; k++) {
            this.arms[k] = maw.getChild("arm_" + k);
            this.armTips[k] = this.arms[k].getChild("arm_" + k + "_tip");
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
    public void setupAnim(VoidCreatureRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float open = state.open;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        // Pumpen im Takt der Bewegung (siehe VoidMawEntity.DriftMoveControl): beim Ansaugen schneller und flacher
        float rate = 0.16f + open * 0.1f;
        float pulse = Mth.sin(t * rate);
        float squeeze = Math.max(0.0f, pulse);
        this.bell.xScale = this.bell.zScale = 1.0f - squeeze * 0.07f + (1.0f - squeeze) * 0.02f;
        this.bell.yScale = 1.0f + squeeze * 0.06f;
        this.bell.y += -squeeze * 0.6f + Mth.sin(t * 0.05f) * 0.8f;
        // im Flug leicht in Fahrtrichtung geneigt, beim Ansaugen der Beute zugewandt
        this.bell.xRot += speed * 0.22f + open * Mth.clamp(state.xRot, -40.0f, 40.0f) * Anim.DEG * 0.5f;
        this.bell.zRot += Mth.sin(t * 0.031f) * 0.05f;
        // der Saum schlägt beim Zusammenziehen nach unten und weitet sich danach wieder (beim Ansaugen weit offen)
        float droop = 0.3f + squeeze * 0.45f - (1.0f - squeeze) * 0.1f - open * 0.25f;
        this.skirt[0].xRot += droop;
        this.skirt[2].xRot -= droop;
        this.skirt[1].zRot += droop;
        this.skirt[3].zRot -= droop;

        // Lippen: geschlossen schräg nach innen, beim Ansaugen weit offen und flatternd, beim Biss zugeschlagen
        float lip = Mth.lerp(open, CLOSED, OPEN) + Mth.sin(t * 0.9f) * 0.09f * open;
        float bite = Anim.seconds(state.attack, t);
        if (bite >= 0.0f && bite < 0.6f) {
            float snap = Anim.bump(bite, 0.06f, 0.16f, 0.55f);
            lip = Mth.lerp(snap, lip, CLOSED + 0.25f);
            this.bell.y += snap * 1.8f;
            this.bell.xRot += snap * 0.25f;
        }
        this.lipN.xRot += lip;
        this.lipS.xRot -= lip;
        this.lipW.zRot -= lip;
        this.lipE.zRot += lip;
        for (int k = 0; k < 4; k++) {
            float wave = Mth.sin(t * 0.12f + k * 1.7f);
            this.arms[k].xRot += 0.12f + wave * 0.12f - open * 0.55f;
            this.armTips[k].xRot += wave * 0.2f - open * 0.3f + speed * 0.25f;
            this.arms[k].zRot += Mth.sin(t * 0.07f + k) * 0.08f;
        }

        // Fangfäden: Wellen von der Glocke zur Spitze, im Flug nach hinten geweht, beim Ansaugen nach außen gespreizt
        for (int i = 0; i < 8; i++) {
            float phase = i * 0.85f;
            float swingX = speed * 0.35f;
            for (int seg = 0; seg < 3; seg++) {
                ModelPart part = this.tentacles[i][seg];
                float amp = 0.08f + seg * 0.07f;
                float w = t * 0.11f - seg * 0.7f + phase;
                part.xRot += Mth.sin(w) * amp + swingX * (seg == 0 ? 1.0f : 0.5f) + squeeze * 0.06f * (seg + 1);
                if (seg > 0) {
                    // die unteren Glieder biegen sich wieder nach innen (beim Ansaugen eingerollt)
                    float a = (22.5f + 45.0f * i) * Anim.DEG;
                    float curl = 0.08f * seg + open * 0.18f * seg;
                    part.zRot += Mth.cos(a) * curl;
                    part.xRot -= Mth.sin(a) * curl;
                }
                part.zRot += Mth.cos(w * 0.8f + 1.3f) * amp * 0.8f;
                if (seg == 0) {
                    // nach außen gespreizt: in Richtung des Ansatzes
                    float a = (22.5f + 45.0f * i) * Anim.DEG;
                    float spread = 0.05f + open * 0.16f + (1.0f - squeeze) * 0.04f;
                    part.zRot -= Mth.cos(a) * spread;
                    part.xRot += Mth.sin(a) * spread;
                }
            }
        }
    }
}
