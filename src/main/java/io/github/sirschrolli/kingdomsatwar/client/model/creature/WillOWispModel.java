package io.github.sirschrolli.kingdomsatwar.client.model.creature;

import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.CreatureRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Irrlicht: schwebt auf und ab, pulsiert, und drei Funken umkreisen den Kern. */
public class WillOWispModel extends EntityModel<CreatureRenderState> {
    private final ModelPart core;
    private final ModelPart halo;
    private final ModelPart[] sparks = new ModelPart[3];

    public WillOWispModel(ModelPart root) {
        super(root);
        this.core = root.getChild("core");
        this.halo = this.core.getChild("halo");
        for (int i = 0; i < this.sparks.length; i++) {
            this.sparks[i] = this.core.getChild("spark" + (i + 1));
        }
    }

    @Override
    public void setupAnim(CreatureRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        this.core.y += Mth.sin(t * 0.12f) * 1.5f;
        this.core.yRot = t * 0.04f;
        this.core.xRot = Mth.sin(t * 0.05f) * 0.2f;
        float pulse = 1.0f + Mth.sin(t * 0.2f) * 0.1f;
        this.halo.xScale = this.halo.yScale = this.halo.zScale = pulse;
        for (int i = 0; i < this.sparks.length; i++) {
            ModelPart spark = this.sparks[i];
            spark.yRot = t * 0.16f + i * Mth.TWO_PI / 3.0f;
            spark.zRot = Mth.sin(t * 0.09f + i * 2.0f) * 0.5f;
            float twinkle = 0.75f + Mth.sin(t * 0.5f + i) * 0.25f;
            spark.xScale = spark.yScale = spark.zScale = twinkle;
        }
    }
}
