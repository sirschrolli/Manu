package io.github.sirschrolli.kingdomsatwar.client.model.farm;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.farm.FarmRenderState;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmGoat;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Ziege: Hörner je Art (einzeln abbrechbar), Zottelfell der Schneeziege, Rammen mit gesenktem Kopf. */
public class FarmGoatModel extends EntityModel<FarmRenderState> {
    private static final String[] KINDS = {"ibex", "chamois", "snow", "domestic"};

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart skirt;
    private final ModelPart beard;
    private final ModelPart beardLong;
    private final ModelPart earL;
    private final ModelPart earR;
    private final ModelPart[] hornsL = new ModelPart[KINDS.length];
    private final ModelPart[] hornsR = new ModelPart[KINDS.length];
    private final ModelPart tail;
    private final ModelPart[] legs;
    private final ModelPart[] pants;

    public FarmGoatModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.skirt = this.body.getChild("skirt");
        this.beard = this.head.getChild("beard");
        this.beardLong = this.head.getChild("beard_long");
        this.earL = this.head.getChild("ear_l");
        this.earR = this.head.getChild("ear_r");
        for (int i = 0; i < KINDS.length; i++) {
            this.hornsL[i] = this.head.getChild("horn_" + KINDS[i] + "_l");
            this.hornsR[i] = this.head.getChild("horn_" + KINDS[i] + "_r");
        }
        this.tail = this.body.getChild("tail");
        String[] names = {"front_left_leg", "front_right_leg", "hind_left_leg", "hind_right_leg"};
        this.legs = new ModelPart[names.length];
        this.pants = new ModelPart[names.length];
        for (int i = 0; i < names.length; i++) {
            this.legs[i] = root.getChild(names[i]);
            this.pants[i] = this.legs[i].getChild(names[i] + "_pants");
        }
    }

    @Override
    public void setupAnim(FarmRenderState state) {
        super.setupAnim(state);
        FarmGoat.Breed breed = FarmGoat.Breed.values()[Mth.clamp(state.breed, 0, FarmGoat.Breed.values().length - 1)];
        boolean adult = !state.isBaby;
        for (int i = 0; i < KINDS.length; i++) {
            boolean kind = i == breed.ordinal() && adult;
            this.hornsL[i].visible = kind && state.leftHorn;
            this.hornsR[i].visible = kind && state.rightHorn;
        }
        boolean shaggy = breed == FarmGoat.Breed.SCHNEE;
        this.skirt.visible = shaggy;
        this.beardLong.visible = shaggy && adult;
        this.beard.visible = !shaggy && breed != FarmGoat.Breed.GAEMSE && adult;
        for (ModelPart p : this.pants) {
            p.visible = shaggy;
        }

        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        this.legs[0].xRot += Anim.leg(pos, speed, 0.0f, 1.2f);
        this.legs[1].xRot += Anim.leg(pos, speed, Mth.PI, 1.2f);
        this.legs[2].xRot += Anim.leg(pos, speed, Mth.PI, 1.2f);
        this.legs[3].xRot += Anim.leg(pos, speed, 0.0f, 1.2f);

        this.head.yRot += Mth.clamp(state.yRot, -45.0f, 45.0f) * Anim.DEG * 0.8f;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * Anim.DEG * 0.6f + state.rammingXHeadRot * 1.6f;
        // Kauen und Ohrenspiel
        this.head.xRot += Mth.sin(t * 0.7f) * 0.015f * (1.0f - speed);
        this.earL.xRot += Math.max(0.0f, Mth.sin(t * 0.07f)) * 0.3f;
        this.earR.xRot += Math.max(0.0f, Mth.sin(t * 0.07f + 2.1f)) * 0.3f;
        this.tail.xRot -= Math.max(0.0f, Mth.sin(t * 0.25f)) * 0.35f;

        if (state.isBaby) {
            this.head.xScale = this.head.yScale = this.head.zScale = 1.35f;
        }
    }
}
