package io.github.sirschrolli.kingdomsatwar.client.model.wild;

import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.BearRenderState;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.Bear;
import net.minecraft.client.model.animal.polarbear.PolarBearModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.PolarBearRenderState;

/** Bär auf dem Skelett des Vanilla-Eisbären (Aufrichten zum Angriff); nur der Braunbär trägt den hohen Schulterbuckel. */
public class BearModel extends PolarBearModel {
    private final ModelPart hump;

    public BearModel(ModelPart root) {
        super(root);
        this.hump = root.getChild("body").getChild("hump");
    }

    @Override
    public void setupAnim(PolarBearRenderState state) {
        super.setupAnim(state);
        this.hump.visible = state instanceof BearRenderState bear && bear.breed == Bear.Breed.BRAUN.ordinal();
        if (state.isBaby) {
            this.head.xScale = this.head.yScale = this.head.zScale = 1.35f;
        }
    }
}
