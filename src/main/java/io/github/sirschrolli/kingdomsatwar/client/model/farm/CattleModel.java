package io.github.sirschrolli.kingdomsatwar.client.model.farm;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.farm.FarmRenderState;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.Cattle;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Hausrind: schwerer Gang, wedelnder Schwanz, zuckende Ohren; Hörner, Stirnschopf und Pilze je Rasse. */
public class CattleModel extends EntityModel<FarmRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart earL;
    private final ModelPart earR;
    private final ModelPart[] shortHorns;
    private final ModelPart[] longHorns;
    private final ModelPart[] lyreHorns;
    private final ModelPart fringe;
    private final ModelPart mushrooms;
    private final ModelPart tail;
    private final ModelPart frontLeft;
    private final ModelPart frontRight;
    private final ModelPart hindLeft;
    private final ModelPart hindRight;

    public CattleModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.earL = this.head.getChild("ear_l");
        this.earR = this.head.getChild("ear_r");
        this.shortHorns = new ModelPart[]{this.head.getChild("horn_short_l"), this.head.getChild("horn_short_r")};
        this.longHorns = new ModelPart[]{this.head.getChild("horn_long_l"), this.head.getChild("horn_long_r")};
        this.lyreHorns = new ModelPart[]{this.head.getChild("horn_lyre_l"), this.head.getChild("horn_lyre_r")};
        this.fringe = this.head.getChild("fringe");
        this.mushrooms = this.body.getChild("mushrooms");
        this.tail = this.body.getChild("tail");
        this.frontLeft = root.getChild("front_left_leg");
        this.frontRight = root.getChild("front_right_leg");
        this.hindLeft = root.getChild("hind_left_leg");
        this.hindRight = root.getChild("hind_right_leg");
    }

    @Override
    public void setupAnim(FarmRenderState state) {
        super.setupAnim(state);
        Cattle.Breed breed = Cattle.Breed.values()[Mth.clamp(state.breed, 0, Cattle.Breed.values().length - 1)];
        boolean adult = !state.isBaby;
        for (ModelPart horn : this.shortHorns) {
            horn.visible = adult && (breed == Cattle.Breed.FLECKVIEH || breed == Cattle.Breed.BRAUNVIEH || breed == Cattle.Breed.PILZ);
        }
        for (ModelPart horn : this.longHorns) {
            horn.visible = adult && breed == Cattle.Breed.HOCHLAND;
        }
        for (ModelPart horn : this.lyreHorns) {
            horn.visible = adult && breed == Cattle.Breed.STEPPE;
        }
        this.fringe.visible = breed == Cattle.Breed.HOCHLAND;
        this.mushrooms.visible = breed == Cattle.Breed.PILZ;

        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        this.frontLeft.xRot += Anim.leg(pos, speed, 0.0f, 1.0f);
        this.frontRight.xRot += Anim.leg(pos, speed, Mth.PI, 1.0f);
        this.hindLeft.xRot += Anim.leg(pos, speed, Mth.PI, 1.0f);
        this.hindRight.xRot += Anim.leg(pos, speed, 0.0f, 1.0f);
        this.body.zRot += Mth.cos(pos * 0.6662f) * 0.025f * speed;               // schweres Wiegen

        this.head.yRot += Mth.clamp(state.yRot, -45.0f, 45.0f) * Anim.DEG * 0.8f;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * Anim.DEG * 0.6f + Mth.sin(t * 0.045f) * 0.04f;
        this.head.xRot += Mth.cos(pos * 0.6662f * 2.0f) * 0.05f * speed;          // Nicken beim Gehen

        this.earL.zRot += Math.max(0.0f, Mth.sin(t * 0.09f)) * 0.3f;
        this.earR.zRot -= Math.max(0.0f, Mth.sin(t * 0.09f + 2.4f)) * 0.3f;
        this.tail.zRot += Mth.sin(t * 0.11f) * 0.12f + Mth.cos(pos * 0.6662f) * 0.1f * speed;
        this.tail.xRot += 0.15f * speed;

        if (state.isBaby) {
            this.head.xScale = this.head.yScale = this.head.zScale = 1.35f;
        }
    }
}
