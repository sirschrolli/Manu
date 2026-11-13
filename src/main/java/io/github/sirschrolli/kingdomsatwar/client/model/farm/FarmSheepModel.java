package io.github.sirschrolli.kingdomsatwar.client.model.farm;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.farm.FarmRenderState;
import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmSheep;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Schaf. Dasselbe Modell gibt es zweimal: einmal für Haut, Kopf und Beine (Wolle unsichtbar) und einmal nur für die
 * Wolle, die die Wollebene in der Farbe des Schafs einfärbt – so passen beide immer genau übereinander.
 */
public class FarmSheepModel extends EntityModel<FarmRenderState> {
    private final boolean woolOnly;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart earL;
    private final ModelPart earR;
    private final ModelPart hornL;
    private final ModelPart hornR;
    private final ModelPart tail;
    private final ModelPart woolBody;
    private final ModelPart woolHead;
    private final ModelPart frontLeft;
    private final ModelPart frontRight;
    private final ModelPart hindLeft;
    private final ModelPart hindRight;
    private final List<ModelPart> woolLegs;

    public FarmSheepModel(ModelPart root, boolean woolOnly) {
        super(root);
        this.woolOnly = woolOnly;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.earL = this.head.getChild("ear_l");
        this.earR = this.head.getChild("ear_r");
        this.hornL = this.head.getChild("horn_l");
        this.hornR = this.head.getChild("horn_r");
        this.tail = this.body.getChild("tail");
        this.woolBody = this.body.getChild("wool_body");
        this.woolHead = this.head.getChild("wool_head");
        this.frontLeft = root.getChild("front_left_leg");
        this.frontRight = root.getChild("front_right_leg");
        this.hindLeft = root.getChild("hind_left_leg");
        this.hindRight = root.getChild("hind_right_leg");
        this.woolLegs = List.of(this.frontLeft.getChild("wool_front_left_leg"), this.frontRight.getChild("wool_front_right_leg"),
                this.hindLeft.getChild("wool_hind_left_leg"), this.hindRight.getChild("wool_hind_right_leg"));
    }

    @Override
    public void setupAnim(FarmRenderState state) {
        super.setupAnim(state);
        FarmSheep.Breed breed = FarmSheep.Breed.values()[Mth.clamp(state.breed, 0, FarmSheep.Breed.values().length - 1)];
        boolean horns = breed == FarmSheep.Breed.HEIDSCHNUCKE && !state.isBaby;
        // Haut-Modell: alles außer Wolle; Woll-Modell: nur die Wolle (die Eltern-Teile zeichnen sich dann nicht selbst)
        for (ModelPart part : List.of(this.body, this.head, this.earL, this.earR, this.tail, this.frontLeft, this.frontRight,
                this.hindLeft, this.hindRight)) {
            part.skipDraw = this.woolOnly;
        }
        this.hornL.visible = horns && !this.woolOnly;
        this.hornR.visible = horns && !this.woolOnly;
        this.earL.visible = true;
        this.woolBody.visible = this.woolOnly;
        this.woolHead.visible = this.woolOnly && breed == FarmSheep.Breed.MERINO;
        for (ModelPart wool : this.woolLegs) {
            wool.visible = this.woolOnly;
        }

        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        this.frontLeft.xRot += Anim.leg(pos, speed, 0.0f, 1.1f);
        this.frontRight.xRot += Anim.leg(pos, speed, Mth.PI, 1.1f);
        this.hindLeft.xRot += Anim.leg(pos, speed, Mth.PI, 1.1f);
        this.hindRight.xRot += Anim.leg(pos, speed, 0.0f, 1.1f);

        if (state.headEatPosition > 0.0f) {
            // Grasen: Kopf tief ins Gras, kauen
            this.head.y += state.headEatPosition * 7.0f;
            this.head.xRot = state.headEatAngle + 0.2f;
        } else {
            this.head.yRot += Mth.clamp(state.yRot, -45.0f, 45.0f) * Anim.DEG * 0.8f;
            this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * Anim.DEG * 0.6f;
        }
        this.earL.zRot += Math.max(0.0f, Mth.sin(t * 0.08f)) * 0.25f;
        this.earR.zRot -= Math.max(0.0f, Mth.sin(t * 0.08f + 1.7f)) * 0.25f;
        this.tail.zRot += Mth.sin(t * 0.3f) * 0.08f * (0.3f + speed);

        if (state.isBaby) {
            this.head.xScale = this.head.yScale = this.head.zScale = 1.35f;
        }
    }
}
