package io.github.sirschrolli.kingdomsatwar.client.model.umbrer;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.umbrer.UmbrerRenderState;
import io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerProfession;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Die Umbrer: ruhiger, gemessener Gang (die Füße schauen unter der Robe hervor, die Robe schwingt), im Stehen die Hände
 * vor dem Körper gefaltet, beim Handeln die Ware vorgezeigt, ab und zu zuckende Ohren. Die Wächter tragen den Speer
 * aufrecht, senken ihn im Kampf und stoßen zu.
 */
public class UmbrerModel extends EntityModel<UmbrerRenderState> {
    private final ModelPart body;
    private final ModelPart skirt;
    private final ModelPart head;
    private final ModelPart earL;
    private final ModelPart earR;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart spear;
    private final ModelPart cape;
    private final ModelPart seerHat;
    private final Map<String, ModelPart> gear = new LinkedHashMap<>();

    public UmbrerModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.skirt = this.body.getChild("skirt");
        this.head = this.body.getChild("head");
        this.earL = this.head.getChild("ear_l");
        this.earR = this.head.getChild("ear_r");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        ModelPart hood = this.head.getChild("hood");
        this.spear = this.rightArm.getChild("spear");
        this.cape = this.body.getChild("cape");
        this.seerHat = hood.getChild("seer_hat");
        this.gear.put("hood", hood);
        this.gear.put("sprig", hood.getChild("sprig"));
        this.gear.put("seer_hat", this.seerHat);
        this.gear.put("goggles", this.head.getChild("goggles"));
        this.gear.put("straw_hat", this.head.getChild("straw_hat"));
        this.gear.put("helmet", this.head.getChild("helmet"));
        this.gear.put("pauldron_l", this.leftArm.getChild("pauldron_l"));
        this.gear.put("pauldron_r", this.rightArm.getChild("pauldron_r"));
        this.gear.put("spear", this.spear);
        for (String name : new String[] {"apron", "satchel", "pendant", "amulet", "armor", "cape"}) {
            this.gear.put(name, this.body.getChild(name));
        }
    }

    @Override
    public void setupAnim(UmbrerRenderState state) {
        super.setupAnim(state);
        UmbrerProfession profession = state.profession;
        for (Map.Entry<String, ModelPart> entry : this.gear.entrySet()) {
            entry.getValue().visible = profession.wears(entry.getKey());
        }
        float t = state.ageInTicks;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        float stride = state.walkAnimationPos * 0.6662f;

        // Beine unter der Robe, die Robe schwingt leicht mit
        this.leftLeg.xRot = Mth.cos(stride) * 0.85f * speed;
        this.rightLeg.xRot = Mth.cos(stride + Mth.PI) * 0.85f * speed;
        this.skirt.zRot = Mth.sin(stride) * 0.035f * speed;
        this.skirt.xRot = -Math.abs(Mth.sin(stride)) * 0.06f * speed;
        this.cape.xRot += speed * 0.35f + Mth.sin(t * 0.08f) * 0.03f;
        // Atem und gelassene Kopfbewegung
        this.body.xRot = Mth.sin(t * 0.06f) * 0.012f;
        this.head.yRot = state.yRot * DEG;
        this.head.xRot = state.xRot * DEG;
        // die Ohren zucken ab und zu
        float twitch = (float) Math.pow(Math.max(0.0f, Mth.sin(t * 0.11f)), 12.0);
        this.earL.zRot -= twitch * 0.3f;
        this.earR.zRot += twitch * 0.3f;
        this.seerHat.zRot += Mth.sin(t * 0.05f) * 0.04f + Mth.sin(stride) * 0.06f * speed;

        if (profession == UmbrerProfession.GUARD) {
            this.guardArms(state, t, speed, stride);
        } else {
            this.civilianArms(state, speed, stride);
        }
        if (state.unhappy) {
            this.head.zRot = 0.3f * Mth.sin(0.45f * t);
            this.head.xRot = 0.35f;
        }
    }

    /** Im Stehen gefaltete Hände, beim Gehen schwingende Ärmel, beim Handeln die vorgezeigte Ware. */
    private void civilianArms(UmbrerRenderState state, float speed, float stride) {
        float fold = Mth.clamp(1.0f - speed * 2.5f, 0.0f, 1.0f);
        this.leftArm.xRot = Mth.lerp(fold, Mth.cos(stride + Mth.PI) * 0.55f * speed, -0.62f);
        this.rightArm.xRot = Mth.lerp(fold, Mth.cos(stride) * 0.55f * speed, -0.62f);
        this.leftArm.zRot = Mth.lerp(fold, 0.0f, 0.34f);
        this.rightArm.zRot = Mth.lerp(fold, 0.0f, -0.34f);
        if (state.trading) {
            this.rightArm.xRot = -1.05f;
            this.rightArm.zRot = -0.12f;
            this.rightArm.yRot = 0.2f;
            this.head.xRot += 0.18f;
        }
    }

    /** Speer aufrecht – im Kampf gesenkt (waagrecht nach vorn), beim Stoß nach vorn gestreckt. */
    private void guardArms(UmbrerRenderState state, float t, float speed, float stride) {
        float stance = state.stance;
        float rest = -0.22f;
        float combat = -0.8f;
        float arm = Mth.lerp(stance, rest, combat);
        float swing = Mth.cos(stride + Mth.PI) * 0.5f * speed;
        float thrust = 0.0f;
        float s = Anim.seconds(state.thrust, t);
        if (s >= 0.0f && s < 0.45f) {
            thrust = Anim.bump(s, 0.08f, 0.16f, 0.42f);
        }
        arm -= 0.6f * thrust;
        this.rightArm.xRot = arm;
        // aufrecht: Arm + Speer = 0; gesenkt: Arm + Speer = 90° (waagrecht nach vorn)
        this.spear.xRot = Mth.lerp(stance, -arm, Mth.HALF_PI - arm);
        this.leftArm.xRot = Mth.lerp(stance, swing, -0.95f - 0.4f * thrust);
        this.leftArm.zRot = Mth.lerp(stance, 0.0f, 0.38f);
        this.body.xRot += 0.12f * thrust + 0.05f * stance;
        this.body.yRot = -0.15f * stance;
    }
}
