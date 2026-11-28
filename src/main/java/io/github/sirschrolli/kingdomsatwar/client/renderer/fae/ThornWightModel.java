package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Dornwicht: ein schleppender, ungleichmäßiger Gang mit vorgebeugtem Rumpf, die langen Arme pendeln locker, die Klauen
 * zucken. Beim Hieb holt ein Arm weit über den Kopf aus und reißt quer nach unten; bei der Salve reißt er beide Arme
 * zurück und schleudert sie nach vorn, der Kopf ruckt mit.
 */
public class ThornWightModel extends EntityModel<FaeRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftForearm;
    private final ModelPart rightForearm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public ThornWightModel(ModelPart root) {
        super(root);
        ModelPart wight = root.getChild("wight");
        this.body = wight.getChild("body");
        this.head = this.body.getChild("head");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftLeg = wight.getChild("left_leg");
        this.rightLeg = wight.getChild("right_leg");
    }

    @Override
    public void setupAnim(FaeRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        // Gang: Beine schwingen, der Rumpf ist vorgebeugt und wiegt sich, die Arme pendeln gegengleich
        this.leftLeg.xRot += Anim.leg(pos, speed, 0.0f, 0.9f);
        this.rightLeg.xRot += Anim.leg(pos, speed, Mth.PI, 0.9f);
        this.body.xRot += 0.22f + 0.08f * speed;
        this.body.zRot += Mth.sin(pos * 0.6662f) * 0.08f * speed + Mth.sin(t * 0.05f) * 0.03f;
        this.leftArm.xRot += Anim.leg(pos, speed, Mth.PI, 0.7f) - 0.2f;
        this.rightArm.xRot += Anim.leg(pos, speed, 0.0f, 0.7f) - 0.2f;
        this.leftArm.zRot += -0.12f - Mth.sin(t * 0.07f) * 0.05f;
        this.rightArm.zRot += 0.12f + Mth.sin(t * 0.07f + 1.3f) * 0.05f;
        this.leftForearm.xRot += -0.35f - 0.12f * Mth.sin(t * 0.09f);
        this.rightForearm.xRot += -0.35f - 0.12f * Mth.sin(t * 0.09f + 2.0f);
        // Kopf: Blick, dazu ein unheimliches, gelegentliches Zucken
        this.head.yRot += Mth.clamp(state.yRot, -60.0f, 60.0f) * Anim.DEG;
        this.head.xRot += Mth.clamp(state.xRot, -40.0f, 40.0f) * Anim.DEG - 0.15f;
        float twitch = Mth.sin(t * 0.31f) > 0.97f ? 0.25f : 0.0f;
        this.head.zRot += twitch;
        // Klauenhieb
        float slash = Anim.seconds(state.slash, t);
        if (slash >= 0.0f && slash < 0.6f) {
            float raise = Anim.bump(slash, 0.15f, 0.2f, 0.32f);
            float strike = Anim.bump(slash - 0.2f, 0.08f, 0.15f, 0.4f);
            this.rightArm.xRot += -2.6f * raise + 1.4f * strike;
            this.rightArm.zRot += 0.4f * raise - 0.6f * strike;
            this.rightForearm.xRot += -0.6f * raise;
            this.body.yRot += -0.3f * raise + 0.4f * strike;
        }
        // Dornensalve
        float volley = Anim.seconds(state.volley, t);
        if (volley >= 0.0f && volley < 1.0f) {
            float back = Anim.bump(volley, 0.35f, 0.6f, 0.72f);
            float throwing = Anim.bump(volley - 0.62f, 0.06f, 0.12f, 0.36f);
            float arms = -0.6f * back - 1.9f * throwing;
            this.leftArm.xRot += arms;
            this.rightArm.xRot += arms;
            this.leftArm.zRot -= 0.5f * back;
            this.rightArm.zRot += 0.5f * back;
            this.body.xRot += -0.25f * back + 0.35f * throwing;
            this.head.xRot += 0.3f * throwing;
        }
    }
}
