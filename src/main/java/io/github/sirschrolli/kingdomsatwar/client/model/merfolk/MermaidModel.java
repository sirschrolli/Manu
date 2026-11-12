package io.github.sirschrolli.kingdomsatwar.client.model.merfolk;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.merfolk.MermaidRenderState;
import io.github.sirschrolli.kingdomsatwar.entity.merfolk.MermaidProfession;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Das Meervolk (seit 0.93). Im Stand tritt die Meerjungfrau Wasser: aufrecht, der Schwanz schlägt ruhig vor und zurück,
 * die Arme rudern seitlich, das Haar treibt; beim Schwimmen kippt sie waagrecht (nach der Neigung der Bahn), der Schwanz
 * schlägt wie bei einem Delfin in einer Welle bis zur Flosse, die Arme liegen am Körper, das Haar strömt nach hinten und
 * der Kopf schaut nach vorn. Beim Handeln zeigt sie die Ware vor; die Tritonwachen tragen den Dreizack aufrecht, senken
 * ihn im Kampf, stoßen zu und werfen. An Land liegt sie und zappelt.
 */
public class MermaidModel extends EntityModel<MermaidRenderState> {
    private final ModelPart swim;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart earL;
    private final ModelPart earR;
    private final ModelPart locksL;
    private final ModelPart locksR;
    private final ModelPart hairBack;
    private final ModelPart hairTip;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart trident;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart fluke;
    private final ModelPart flukeL;
    private final ModelPart flukeR;
    private final ModelPart veilL;
    private final ModelPart veilR;
    private final ModelPart coralCrown;
    private final Map<String, ModelPart> gear = new LinkedHashMap<>();

    public MermaidModel(ModelPart root) {
        super(root);
        this.swim = root.getChild("swim");
        this.torso = this.swim.getChild("torso");
        this.head = this.torso.getChild("head");
        this.earL = this.head.getChild("ear_l");
        this.earR = this.head.getChild("ear_r");
        this.locksL = this.head.getChild("locks_l");
        this.locksR = this.head.getChild("locks_r");
        this.hairBack = this.head.getChild("hair_back");
        this.hairTip = this.hairBack.getChild("hair_tip");
        this.leftArm = this.torso.getChild("left_arm");
        this.rightArm = this.torso.getChild("right_arm");
        this.trident = this.rightArm.getChild("trident");
        this.tail1 = this.swim.getChild("tail1");
        this.tail2 = this.tail1.getChild("tail2");
        this.tail3 = this.tail2.getChild("tail3");
        this.tail4 = this.tail3.getChild("tail4");
        this.fluke = this.tail4.getChild("fluke");
        this.flukeL = this.fluke.getChild("fluke_l");
        this.flukeR = this.fluke.getChild("fluke_r");
        this.veilL = this.tail1.getChild("veil_l");
        this.veilR = this.tail1.getChild("veil_r");
        this.coralCrown = this.head.getChild("coral_crown");
        // Zubehör je Beruf (Knochen-Namen wie in tools/entitygen/models/mermaid.py)
        for (String name : new String[] {"top", "necklace", "armor"}) {
            this.gear.put(name, this.torso.getChild(name));
        }
        for (String name : new String[] {"locks_l", "locks_r", "hair_back", "diver_band", "coral_crown", "flower", "shell_comb", "starfish",
                "helmet"}) {
            this.gear.put(name, this.head.getChild(name));
        }
        this.gear.put("bracelet_l", this.leftArm.getChild("bracelet_l"));
        this.gear.put("pauldron_l", this.leftArm.getChild("pauldron_l"));
        this.gear.put("bracelet_r", this.rightArm.getChild("bracelet_r"));
        this.gear.put("pauldron_r", this.rightArm.getChild("pauldron_r"));
        this.gear.put("trident", this.trident);
        for (String name : new String[] {"pearl_net", "tool_belt", "conch"}) {
            this.gear.put(name, this.tail1.getChild(name));
        }
    }

    @Override
    public void setupAnim(MermaidRenderState state) {
        super.setupAnim(state);
        MermaidProfession profession = state.profession;
        for (Map.Entry<String, ModelPart> entry : this.gear.entrySet()) {
            entry.getValue().visible = profession.wears(entry.getKey());
        }
        float t = state.ageInTicks;
        float sw = state.inWater ? state.swimPose : 0.0f;
        float idle = 1.0f - sw;
        float ph = state.swimPhase;

        // ------------------------------------------------------------ der ganze Körper
        float pitch = Mth.clamp(state.xRot, -60.0f, 60.0f) * DEG;
        this.swim.xRot = sw * (Mth.HALF_PI + pitch * 0.8f);
        this.swim.y += Mth.sin(ph) * 0.45f * idle;                  // wassertretend hebt und senkt sie sich mit jedem Schlag
        this.swim.zRot = Mth.sin(t * 0.045f) * 0.04f * idle;
        this.torso.xRot = -Mth.sin(ph + 0.6f) * (0.02f + 0.03f * sw);

        // ------------------------------------------------------------ Schwanz: eine Welle bis in die Flosse
        float amp = 0.09f + 0.21f * sw;
        this.tail1.xRot += Mth.sin(ph) * amp * 0.35f;
        this.tail2.xRot += Mth.sin(ph - 0.7f) * amp * 0.8f;
        this.tail3.xRot += Mth.sin(ph - 1.4f) * amp;
        this.tail4.xRot += Mth.sin(ph - 2.1f) * amp * 1.15f;
        this.fluke.xRot += Mth.sin(ph - 2.8f) * amp * 1.35f;
        float flex = Mth.sin(ph - 3.2f) * (0.06f + 0.06f * sw);
        this.flukeL.zRot += flex;
        this.flukeR.zRot -= flex;
        // im Stand schwingt der Schwanz auch ein wenig zur Seite
        this.tail2.zRot += Mth.sin(t * 0.06f) * 0.05f * idle;
        this.tail3.zRot += Mth.sin(t * 0.06f - 0.6f) * 0.06f * idle;
        this.tail4.zRot += Mth.sin(t * 0.06f - 1.2f) * 0.07f * idle;
        // die Schleierflossen wehen
        float veil = Mth.sin(ph * 1.1f + 0.5f) * (0.08f + 0.06f * sw);
        this.veilL.zRot += veil;
        this.veilR.zRot -= veil;
        this.veilL.xRot += Mth.sin(t * 0.07f) * 0.06f;
        this.veilR.xRot += Mth.sin(t * 0.07f + 0.8f) * 0.06f;

        // ------------------------------------------------------------ Kopf, Haar, Ohrflossen
        this.head.yRot = state.yRot * DEG * idle;
        this.head.xRot = Mth.lerp(sw, pitch * 0.5f, -this.swim.xRot * 0.78f + 0.08f);
        float drift = Mth.sin(t * 0.05f);
        // beim Schwimmen strömt das Haar nach hinten am Körper entlang (der Kopf ist dann zurückgeneigt)
        this.hairBack.xRot += Mth.lerp(sw, 0.12f + drift * 0.09f, 1.05f + Mth.sin(ph * 0.8f) * 0.07f);
        this.hairTip.xRot += Mth.lerp(sw, Mth.sin(t * 0.05f - 0.9f) * 0.15f, 0.12f + Mth.sin(ph * 0.8f - 1.0f) * 0.1f);
        this.hairBack.zRot += Mth.sin(t * 0.037f) * 0.06f * idle;
        this.locksL.zRot += Mth.sin(t * 0.06f) * 0.05f;
        this.locksR.zRot -= Mth.sin(t * 0.06f + 0.4f) * 0.05f;
        this.locksL.xRot += sw * 1.0f + drift * 0.04f;
        this.locksR.xRot += sw * 1.0f + drift * 0.04f;
        this.earL.yRot += Mth.sin(t * 0.21f) * 0.1f;
        this.earR.yRot -= Mth.sin(t * 0.21f + 0.3f) * 0.1f;
        this.coralCrown.zRot += Mth.sin(t * 0.08f) * 0.02f;

        // ------------------------------------------------------------ Arme
        // im Stand: seitlich ausgestreckt, rudernd; beim Schwimmen am Körper, ab und zu ein Zug
        float scull = Mth.sin(t * 0.11f);
        float lift = Mth.cos(t * 0.11f);
        float stroke = Mth.sin(ph * 0.5f);
        this.leftArm.xRot = Mth.lerp(sw, scull * 0.35f - 0.15f, 0.15f + stroke * 0.12f);
        this.leftArm.zRot = Mth.lerp(sw, -0.55f - lift * 0.12f, -0.12f);
        this.rightArm.xRot = Mth.lerp(sw, -scull * 0.35f - 0.15f, 0.15f - stroke * 0.12f);
        this.rightArm.zRot = Mth.lerp(sw, 0.55f + lift * 0.12f, 0.12f);

        if (profession == MermaidProfession.GUARD) {
            this.guardArms(state, t, sw);
        } else if (state.show > 0.001f) {
            // die Ware vorzeigen (weich hinein und heraus)
            float show = state.show;
            this.rightArm.xRot = Mth.lerp(show, this.rightArm.xRot, -1.2f);
            this.rightArm.zRot = Mth.lerp(show, this.rightArm.zRot, 0.12f);
            this.rightArm.yRot = Mth.lerp(show, this.rightArm.yRot, -0.25f);
            this.head.xRot += 0.15f * show;
        }
        if (state.sulk > 0.001f) {
            // Kopfschütteln
            this.head.zRot = Mth.lerp(state.sulk, this.head.zRot, 0.3f * Mth.sin(0.45f * t));
            this.head.xRot = Mth.lerp(state.sulk, this.head.xRot, 0.3f);
        }
        if (!state.inWater) {
            // an Land: sie liegt auf der Seite des Bauchs und zappelt mit dem Schwanz
            this.swim.xRot = Mth.HALF_PI;
            this.swim.y += 16.0f;
            this.head.xRot = -1.1f;
            float flop = Mth.sin(t * 0.55f);
            this.tail2.xRot += flop * 0.25f;
            this.tail3.xRot += flop * 0.3f;
            this.fluke.xRot += flop * 0.35f;
        }
    }

    /** Dreizack aufrecht – im Kampf gesenkt (waagrecht nach vorn), beim Stoß nach vorn gestreckt, beim Wurf über den Kopf. */
    private void guardArms(MermaidRenderState state, float t, float sw) {
        float stance = state.stance;
        float arm = Mth.lerp(stance, Mth.lerp(sw, -0.25f, 0.0f), -0.85f);
        float thrust = 0.0f;
        float s = Anim.seconds(state.thrust, t);
        if (s >= 0.0f && s < 0.45f) {
            thrust = Anim.bump(s, 0.08f, 0.16f, 0.42f);
        }
        float toss = 0.0f;
        float u = Anim.seconds(state.toss, t);
        if (u >= 0.0f && u < 0.7f) {
            // ausholen (der Arm über den Kopf), dann nach vorn schleudern
            toss = u < 0.35f ? Anim.bump(u, 0.2f, 0.1f, 0.35f) : -Anim.bump(u - 0.35f, 0.06f, 0.05f, 0.35f) * 0.6f;
        }
        arm -= 0.6f * thrust + 2.0f * toss;
        this.rightArm.xRot = arm;
        this.rightArm.zRot = 0.12f;
        this.rightArm.yRot = 0.0f;
        // aufrecht: Arm + Dreizack = 0; gesenkt: Arm + Dreizack = 90° (waagrecht nach vorn)
        this.trident.xRot = Mth.lerp(stance, -arm, Mth.HALF_PI - arm) + toss * 1.4f;
        this.leftArm.xRot = Mth.lerp(stance, this.leftArm.xRot, -0.95f - 0.4f * thrust);
        this.leftArm.zRot = Mth.lerp(stance, this.leftArm.zRot, -0.38f);
        this.torso.yRot = -0.15f * stance;
        this.torso.xRot += 0.12f * thrust;
    }
}
