package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Moorschlurfer: ein schwerer, schwankender Gang – der Buckel wiegt von einer Seite zur anderen, die langen Arme schleifen
 * und pendeln nach, die Rohrkolben und der tote Ast schwanken, aus dem Maul hängen die Wurzeln und wiegen sich. Versunken
 * steckt er bis zum Buckel im Schlamm, beim Aufrichten stemmt er sich mit den Armen heraus. Beim Wurf greift die rechte
 * Hand in den Schlamm, holt über die Schulter aus und schleudert; beim Wurzelgriff schnellen beide Arme vor, die
 * Wurzelfinger schließen sich und drücken zu.
 */
public class MireShamblerModel extends EntityModel<MireShamblerRenderState> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart branch;
    private final ModelPart reeds;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftForearm;
    private final ModelPart rightForearm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public MireShamblerModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.branch = this.body.getChild("branch");
        this.reeds = this.body.getChild("reeds");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    @Override
    public void setupAnim(MireShamblerRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        float stride = pos * 0.5f;

        // ------------------------------------------------------------ schwerer, schwankender Gang
        this.leftLeg.xRot += Mth.cos(stride) * 0.5f * speed;
        this.rightLeg.xRot += Mth.cos(stride + Mth.PI) * 0.5f * speed;
        this.body.zRot += Mth.cos(stride) * 0.12f * speed + Mth.sin(t * 0.04f) * 0.03f;
        this.body.y += Math.abs(Mth.sin(stride)) * 1.2f * speed;
        this.body.xRot += Mth.sin(t * 0.06f) * 0.03f;                                   // schwerer Atem
        this.leftArm.xRot += Mth.cos(stride + Mth.PI) * 0.35f * speed + Mth.sin(t * 0.05f) * 0.04f;
        this.rightArm.xRot += Mth.cos(stride) * 0.35f * speed + Mth.sin(t * 0.05f + 1.0f) * 0.04f;
        this.leftArm.zRot += -0.08f - Mth.cos(stride) * 0.06f * speed;
        this.rightArm.zRot += 0.08f - Mth.cos(stride) * 0.06f * speed;
        this.leftForearm.xRot += -0.1f - Mth.sin(t * 0.07f) * 0.05f;
        this.rightForearm.xRot += -0.1f - Mth.sin(t * 0.07f + 2.0f) * 0.05f;
        this.head.yRot += Mth.clamp(state.yRot, -40.0f, 40.0f) * Anim.DEG * 0.7f;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * Anim.DEG * 0.5f;
        this.jaw.xRot += 0.06f + Mth.sin(t * 0.09f) * 0.06f;
        this.reeds.xRot += Mth.sin(t * 0.07f) * 0.08f + Mth.cos(stride) * 0.1f * speed;
        this.reeds.zRot += Mth.sin(t * 0.05f + 1.0f) * 0.06f;
        this.branch.zRot += Mth.sin(t * 0.04f) * 0.04f;

        // ------------------------------------------------------------ versunken im Schlamm
        float sunk = state.sunk;
        if (sunk > 0.001f) {
            float down = 21.0f * sunk;
            this.body.y += down;
            this.leftLeg.y += down;
            this.rightLeg.y += down;
            this.body.xRot += 0.35f * sunk;
            this.head.xRot += 0.3f * sunk;
            this.leftArm.xRot += 0.4f * sunk;
            this.rightArm.xRot += 0.4f * sunk;
            this.leftArm.zRot -= 0.5f * sunk;
            this.rightArm.zRot += 0.5f * sunk;
        }

        // ------------------------------------------------------------ aufrichten: er stemmt sich aus dem Schlamm
        float rise = Anim.seconds(state.rise, t);
        if (rise >= 0.0f && rise < 1.9f) {
            float push = Anim.bump(rise, 0.3f, 1.0f, 1.6f);
            float roar = Anim.bump(rise - 1.0f, 0.15f, 0.4f, 0.8f);
            this.leftArm.xRot += -1.1f * push;
            this.rightArm.xRot += -1.1f * push;
            this.leftArm.zRot += -0.55f * push;
            this.rightArm.zRot += 0.55f * push;
            this.leftForearm.xRot += 0.6f * push;
            this.rightForearm.xRot += 0.6f * push;
            this.body.xRot += -0.25f * roar;
            this.head.xRot += -0.5f * roar;
            this.jaw.xRot += 0.5f * roar;
        }

        // ------------------------------------------------------------ Schlammwurf (rechte Hand)
        float throwing = Anim.seconds(state.throwing, t);
        if (throwing >= 0.0f && throwing < 1.3f) {
            float scoop = Anim.bump(throwing, 0.2f, 0.3f, 0.45f);
            float back = Anim.bump(throwing - 0.3f, 0.2f, 0.36f, 0.42f);
            float hurl = Anim.bump(throwing - 0.62f, 0.08f, 0.2f, 0.6f);
            this.rightArm.xRot += -0.4f * scoop - 2.5f * back - 1.2f * hurl;
            this.rightArm.zRot += 0.3f * back;
            this.rightForearm.xRot += -0.5f * scoop - 0.8f * back + 0.4f * hurl;
            this.body.xRot += 0.25f * scoop - 0.2f * back + 0.3f * hurl;
            this.body.yRot += 0.3f * back - 0.35f * hurl;
            this.head.xRot += -0.15f * back;
            this.jaw.xRot += 0.35f * hurl;
        }

        // ------------------------------------------------------------ Wurzelgriff (beide Arme)
        float grasp = Anim.seconds(state.grasp, t);
        if (grasp >= 0.0f && grasp < 3.1f) {
            float reach = Anim.bump(grasp, 0.18f, 2.7f, 3.0f);
            float squeeze = reach * (0.5f + 0.5f * Mth.sin(grasp * 9.0f));
            this.leftArm.xRot += -1.35f * reach;
            this.rightArm.xRot += -1.35f * reach;
            this.leftArm.zRot += -0.3f * reach + 0.12f * squeeze;
            this.rightArm.zRot += 0.3f * reach - 0.12f * squeeze;
            this.leftForearm.xRot += -0.5f * reach - 0.2f * squeeze;
            this.rightForearm.xRot += -0.5f * reach - 0.2f * squeeze;
            this.body.xRot += 0.2f * reach;
            this.jaw.xRot += 0.3f * reach;
        }
    }
}
