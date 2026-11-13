package io.github.sirschrolli.kingdomsatwar.client.model.farm;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.farm.FarmRenderState;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.Hare;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Hase: hockt geduckt, beim Sprung strecken sich die Hinterläufe nach hinten und die Vorderläufe nach vorn, die
 * Löffel legen sich an; im Sitzen spielen die Löffel und die Nase schnuppert. Lange oder kurze Löffel je Rasse.
 */
public class HareModel extends EntityModel<FarmRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart[] longEars;
    private final ModelPart[] shortEars;
    private final ModelPart frontL;
    private final ModelPart frontR;
    private final ModelPart haunchL;
    private final ModelPart haunchR;
    private final ModelPart footL;
    private final ModelPart footR;
    private final ModelPart tail;

    public HareModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.tail = this.body.getChild("tail");
        this.head = root.getChild("head");
        this.longEars = new ModelPart[]{this.head.getChild("ear_long_l"), this.head.getChild("ear_long_r")};
        this.shortEars = new ModelPart[]{this.head.getChild("ear_short_l"), this.head.getChild("ear_short_r")};
        this.frontL = root.getChild("front_leg_l");
        this.frontR = root.getChild("front_leg_r");
        this.haunchL = root.getChild("haunch_l");
        this.haunchR = root.getChild("haunch_r");
        this.footL = this.haunchL.getChild("hind_foot_l");
        this.footR = this.haunchR.getChild("hind_foot_r");
    }

    @Override
    public void setupAnim(FarmRenderState state) {
        super.setupAnim(state);
        Hare.Breed breed = Hare.Breed.values()[Mth.clamp(state.breed, 0, Hare.Breed.values().length - 1)];
        boolean longEars = breed == Hare.Breed.FELDHASE || breed == Hare.Breed.SCHNEEHASE || breed == Hare.Breed.WUESTENHASE;
        for (ModelPart ear : this.longEars) {
            ear.visible = longEars;
        }
        for (ModelPart ear : this.shortEars) {
            ear.visible = !longEars;
        }
        ModelPart[] ears = longEars ? this.longEars : this.shortEars;

        float t = state.ageInTicks;
        float jump = Mth.sin(state.jumpCompletion * Mth.PI);
        // Sprung: Körper streckt sich, Hinterläufe schnellen nach hinten, Vorderläufe greifen nach vorn
        this.body.xRot += jump * 0.35f;
        this.head.xRot += jump * 0.25f;
        this.head.y += jump * 0.6f;
        this.haunchL.xRot += jump * 0.9f;
        this.haunchR.xRot += jump * 0.9f;
        this.footL.xRot += jump * 0.6f;
        this.footR.xRot += jump * 0.6f;
        this.frontL.xRot -= jump * 1.1f;
        this.frontR.xRot -= jump * 1.1f;
        for (ModelPart ear : ears) {
            ear.xRot -= jump * 0.6f;
        }

        this.head.yRot += Mth.clamp(state.yRot, -50.0f, 50.0f) * Anim.DEG;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * Anim.DEG * 0.6f;
        // Löffelspiel und Schnuppern im Sitzen
        float rest = 1.0f - Math.min(1.0f, jump * 3.0f);
        ears[0].xRot += Mth.sin(t * 0.09f) * 0.12f * rest;
        ears[1].xRot += Mth.sin(t * 0.09f + 1.3f) * 0.12f * rest;
        ears[0].zRot += Math.max(0.0f, Mth.sin(t * 0.05f)) * 0.25f * rest;
        this.head.xRot += Mth.sin(t * 1.6f) * 0.02f * rest;
        this.tail.xRot += Mth.sin(t * 0.4f) * 0.1f;

        if (state.isBaby) {
            this.head.xScale = this.head.yScale = this.head.zScale = 1.4f;
        }
    }
}
