package io.github.sirschrolli.kingdomsatwar.client.model.wild;

import net.minecraft.client.model.animal.panda.PandaModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.PandaRenderState;

/** Panda auf dem Vanilla-Skelett (Sitzen, Rollen, Rückenlage, Niesen); Junge mit großem Kopf. */
public class WildPandaModel extends PandaModel {
    public WildPandaModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(PandaRenderState state) {
        super.setupAnim(state);
        if (state.isBaby) {
            this.head.xScale = this.head.yScale = this.head.zScale = 1.35f;
        }
    }
}
