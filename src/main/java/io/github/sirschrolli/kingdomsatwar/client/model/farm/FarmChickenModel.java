package io.github.sirschrolli.kingdomsatwar.client.model.farm;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.farm.FarmRenderState;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmChicken;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Huhn: nickender Kopf beim Gehen, Picken, Flügelschlag; der Hahn mit großem Kamm und Sichelfedern. */
public class FarmChickenModel extends EntityModel<FarmRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart comb;
    private final ModelPart combBig;
    private final ModelPart wingL;
    private final ModelPart wingR;
    private final ModelPart tail;
    private final ModelPart sickle;
    private final ModelPart legL;
    private final ModelPart legR;

    public FarmChickenModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.comb = this.head.getChild("comb");
        this.combBig = this.head.getChild("comb_big");
        this.wingL = this.body.getChild("wing_l");
        this.wingR = this.body.getChild("wing_r");
        this.tail = this.body.getChild("tail");
        this.sickle = this.tail.getChild("sickle");
        this.legL = root.getChild("leg_l");
        this.legR = root.getChild("leg_r");
    }

    @Override
    public void setupAnim(FarmRenderState state) {
        super.setupAnim(state);
        boolean rooster = state.breed == FarmChicken.Breed.ROOSTER.ordinal() && !state.isBaby;
        this.comb.visible = !rooster && !state.isBaby;
        this.combBig.visible = rooster;
        this.sickle.visible = rooster;

        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        this.legL.xRot += Anim.leg(pos, speed, 0.0f, 1.4f);
        this.legR.xRot += Anim.leg(pos, speed, Mth.PI, 1.4f);
        // typisches Kopfnicken im Gehen, Picken im Stand
        this.head.z += Mth.sin(pos * 0.6662f * 2.0f) * 0.8f * speed;
        float peck = Math.max(0.0f, Mth.sin(t * 0.13f) - 0.85f) / 0.15f * (1.0f - speed);
        this.head.xRot += peck * 0.9f;
        this.head.yRot += Mth.clamp(state.yRot, -60.0f, 60.0f) * Anim.DEG;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * Anim.DEG * 0.7f;

        float flap = (Mth.sin(state.flap) + 1.0f) * state.flapSpeed;
        this.wingL.zRot -= flap;
        this.wingR.zRot += flap;
        this.tail.xRot += Mth.sin(t * 0.2f) * 0.05f;
        this.body.y -= Math.abs(Mth.sin(pos * 0.6662f)) * 0.3f * speed;

        if (state.isBaby) {
            this.head.xScale = this.head.yScale = this.head.zScale = 1.45f;
        }
    }
}
