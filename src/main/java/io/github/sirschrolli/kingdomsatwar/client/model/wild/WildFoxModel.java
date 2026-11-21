package io.github.sirschrolli.kingdomsatwar.client.model.wild;

import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildFoxRenderState;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildFox;
import net.minecraft.client.model.animal.fox.AdultFoxModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.FoxRenderState;

/** Fuchs auf dem Vanilla-Skelett; der Wüstenfuchs trägt statt der normalen Ohren seine riesigen Fennek-Ohren. */
public class WildFoxModel extends AdultFoxModel {
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart bigRightEar;
    private final ModelPart bigLeftEar;

    public WildFoxModel(ModelPart root) {
        super(root);
        ModelPart head = root.getChild("head");
        this.rightEar = head.getChild("right_ear");
        this.leftEar = head.getChild("left_ear");
        this.bigRightEar = head.getChild("ear_big_r");
        this.bigLeftEar = head.getChild("ear_big_l");
    }

    @Override
    public void setupAnim(FoxRenderState state) {
        super.setupAnim(state);
        boolean fennec = state instanceof WildFoxRenderState fox && fox.breed == WildFox.Breed.WUESTE.ordinal();
        this.rightEar.visible = this.leftEar.visible = !fennec;
        this.bigRightEar.visible = this.bigLeftEar.visible = fennec;
        if (state.isBaby) {
            this.head.xScale = this.head.yScale = this.head.zScale = 1.3f;
        }
    }
}
