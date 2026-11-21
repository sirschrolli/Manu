package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.end.PhasmorEntity;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Phasmor, der Leerwandlerkönig: Jeder Angriff ist eine Folge von Haltungen ({@link PhasmorPoses}), zwischen denen weich
 * überblendet wird – beim Wachen kniet er, die Hände auf dem Knauf der Klinge, die im Boden steckt; beim Erwachen steht er
 * auf und reckt die Klinge empor; beim Hieb holt er über die Schulter aus. Dazu kommen der lange Schritt der Leerwandler
 * (die Beine schwingen, die Knie federn), die Krone aus Phasitsplittern, die langsam kreist (gekrönt schneller), die
 * schwebenden Splitter über den Schultern, der Königsmantel und der Wappenrock, die hängen und wehen, und der Kiefer, der sich
 * beim Brüllen zeigt (der Kopf hebt sich wie bei einem zornigen Leerwandler).
 */
public class PhasmorModel extends EntityModel<PhasmorRenderState> {
    /** Je Wesen die zuletzt gezeigte Haltung (für weiche Wechsel zwischen den Angriffen). */
    private static final Map<Object, float[]> LAST = new WeakHashMap<>();

    private final ModelPart[] parts = new ModelPart[PhasmorPoses.BONES];
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart crown;
    private final ModelPart[] spikes = new ModelPart[5];
    private final ModelPart mantle;
    private final ModelPart mantleLow;
    private final ModelPart tabard;
    private final ModelPart[] floats = new ModelPart[4];
    private final float[] pose = new float[PhasmorPoses.BONES * 6];

    public PhasmorModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.jaw = this.body.getChild("jaw");
        this.crown = this.head.getChild("crown");
        for (int i = 0; i < this.spikes.length; i++) {
            this.spikes[i] = this.crown.getChild("crown_" + i);
        }
        this.mantle = this.body.getChild("mantle");
        this.mantleLow = this.mantle.getChild("mantle_low");
        this.tabard = this.body.getChild("tabard");
        ModelPart leftArm = this.body.getChild("left_arm");
        ModelPart rightArm = this.body.getChild("right_arm");
        ModelPart leftForearm = leftArm.getChild("left_forearm");
        ModelPart rightForearm = rightArm.getChild("right_forearm");
        ModelPart leftPauldron = leftArm.getChild("left_pauldron");
        ModelPart rightPauldron = rightArm.getChild("right_pauldron");
        this.floats[0] = leftPauldron.getChild("left_float_0");
        this.floats[1] = leftPauldron.getChild("left_float_1");
        this.floats[2] = rightPauldron.getChild("right_float_0");
        this.floats[3] = rightPauldron.getChild("right_float_1");
        ModelPart leftLeg = this.body.getChild("left_leg");
        ModelPart rightLeg = this.body.getChild("right_leg");
        this.parts[PhasmorPoses.BODY] = this.body;
        this.parts[PhasmorPoses.HEAD] = this.head;
        this.parts[PhasmorPoses.LEFT_ARM] = leftArm;
        this.parts[PhasmorPoses.LEFT_FOREARM] = leftForearm;
        this.parts[PhasmorPoses.RIGHT_ARM] = rightArm;
        this.parts[PhasmorPoses.RIGHT_FOREARM] = rightForearm;
        this.parts[PhasmorPoses.BLADE] = rightForearm.getChild("blade");
        this.parts[PhasmorPoses.LEFT_LEG] = leftLeg;
        this.parts[PhasmorPoses.LEFT_SHIN] = leftLeg.getChild("left_shin");
        this.parts[PhasmorPoses.RIGHT_LEG] = rightLeg;
        this.parts[PhasmorPoses.RIGHT_SHIN] = rightLeg.getChild("right_shin");
    }

    @Override
    public void setupAnim(PhasmorRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        PhasmorEntity.Attack[] attacks = PhasmorEntity.Attack.values();
        PhasmorEntity.Attack attack = attacks[Mth.clamp(state.attack, 0, attacks.length - 1)];
        float a = state.attackTime;
        int[][] timeline = switch (attack) {
            case AWAKEN -> PhasmorPoses.AWAKEN;
            case SLASH -> PhasmorPoses.SLASH;
            case BLINK -> PhasmorPoses.BLINK;
            case ILLUSIONS -> PhasmorPoses.ILLUSIONS;
            case HURL -> PhasmorPoses.HURL;
            case RIFTS -> PhasmorPoses.RIFTS;
            case STORM -> PhasmorPoses.STORM;
            case CROWN -> PhasmorPoses.CROWN;
            case STAGGER -> PhasmorPoses.STAGGER;
            default -> null;
        };
        if (attack == PhasmorEntity.Attack.GUARD) {
            copy(PhasmorPoses.POSES[PhasmorPoses.P_KNEEL], this.pose);
        } else if (timeline == null) {
            copy(PhasmorPoses.POSES[PhasmorPoses.P_READY], this.pose);
        } else {
            sample(timeline, a, this.pose);
        }
        if (state.deathTime > 0.0f) {
            float d = Mth.clamp(state.deathTime / 26.0f, 0.0f, 1.0f);
            mix(this.pose, PhasmorPoses.POSES[PhasmorPoses.P_DEATH], d * d * (3.0f - 2.0f * d), this.pose);
        }
        this.smooth(state, t);
        this.apply();

        boolean free = attack == PhasmorEntity.Attack.NONE && state.deathTime <= 0.0f;
        // der Schritt: lange Beine schwingen weit, die Knie federn, die freie Linke schwingt gegen
        float walk = free ? Mth.clamp(state.walkAnimationSpeed, 0.0f, 1.0f) : 0.0f;
        if (walk > 0.0f) {
            float phase = state.walkAnimationPos * 0.55f;
            float swing = Mth.cos(phase) * 0.75f * walk;
            this.parts[PhasmorPoses.LEFT_LEG].xRot += swing;
            this.parts[PhasmorPoses.RIGHT_LEG].xRot -= swing;
            this.parts[PhasmorPoses.LEFT_SHIN].xRot += Math.max(0.0f, -Mth.sin(phase)) * 0.9f * walk;
            this.parts[PhasmorPoses.RIGHT_SHIN].xRot += Math.max(0.0f, Mth.sin(phase)) * 0.9f * walk;
            this.parts[PhasmorPoses.LEFT_ARM].xRot -= swing * 0.45f;
            this.body.y += Math.abs(Mth.sin(phase)) * -0.8f * walk;
        }
        // ruhiges Atmen
        if (attack != PhasmorEntity.Attack.GUARD) {
            this.body.y += Mth.sin(t * 0.07f) * 0.25f;
        }
        // Blick zum Gegner (beim Wachen und Erwachen lenkt die Haltung den Kopf)
        if (attack != PhasmorEntity.Attack.GUARD && attack != PhasmorEntity.Attack.AWAKEN && attack != PhasmorEntity.Attack.CROWN
                && state.deathTime <= 0.0f) {
            float look = attack == PhasmorEntity.Attack.NONE ? 0.8f : 0.4f;
            this.head.yRot += Mth.clamp(state.yRot, -45.0f, 45.0f) * Anim.DEG * look;
            this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * Anim.DEG * look;
        }
        // der Kiefer: steht still, wenn sich der Kopf zum Brüllen hebt
        this.jaw.xRot = this.head.xRot;
        this.jaw.yRot = this.head.yRot;
        this.jaw.zRot = this.head.zRot;
        float roar = roar(attack, a);
        this.head.y -= roar * 4.5f;
        this.jaw.visible = roar > 0.01f;
        // die Krone kreist (gekrönt schneller), die Splitter schweben auf und ab
        float fade = state.deathTime > 0.0f ? Mth.clamp(1.0f - state.deathTime / PhasmorEntity.DEATH_TICKS, 0.0f, 1.0f) : 1.0f;
        this.crown.yRot = t * (state.crowned ? 0.07f : 0.02f);
        this.crown.y -= (state.crowned ? 1.2f : 0.4f) + Mth.sin(t * 0.09f) * 0.4f;
        for (int i = 0; i < this.spikes.length; i++) {
            this.spikes[i].y += Mth.sin(t * 0.13f + i * 1.3f) * 0.35f;
            float s = fade * (state.crowned ? 1.15f + Mth.sin(t * 0.3f + i) * 0.08f : 1.0f);
            this.spikes[i].xScale = this.spikes[i].zScale = s;
            this.spikes[i].yScale = s * (state.crowned ? 1.25f : 1.0f);
        }
        for (int i = 0; i < this.floats.length; i++) {
            this.floats[i].y += Mth.sin(t * 0.11f + i * 1.7f) * 0.5f;
            this.floats[i].yRot += t * 0.03f * (i % 2 == 0 ? 1 : -1);
        }
        // der Mantel hängt, auch wenn er sich neigt; im Gehen weht er hinter ihm her
        float lean = this.body.xRot;
        float flow = Mth.clamp(state.drift * 5.0f, 0.0f, 0.8f) + walk * 0.25f;
        this.mantle.xRot += -lean * 0.85f + flow * 0.6f + Mth.sin(t * 0.08f) * 0.03f;
        this.mantleLow.xRot += flow * 0.35f + Mth.sin(t * 0.1f + 1.0f) * 0.04f;
        this.tabard.xRot += -lean * 0.9f - Math.max(Math.max(-this.parts[PhasmorPoses.LEFT_LEG].xRot, -this.parts[PhasmorPoses.RIGHT_LEG].xRot),
                0.0f) * 0.75f;
        this.tabard.zRot += Mth.sin(t * 0.09f) * 0.03f;
    }

    /** Wie weit der Kopf zum Brüllen gehoben ist (0–1): beim Erwachen, bei der Krönung, beim Krümmen. */
    private static float roar(PhasmorEntity.Attack attack, float a) {
        return switch (attack) {
            case AWAKEN -> ramp(a, 42.0f, 48.0f, 58.0f, 64.0f);
            case CROWN -> ramp(a, 10.0f, 16.0f, 46.0f, 54.0f);
            case STAGGER -> ramp(a, 0.0f, 4.0f, 30.0f, 40.0f) * 0.6f;
            default -> 0.0f;
        };
    }

    private static float ramp(float a, float in0, float in1, float out0, float out1) {
        if (a <= in0 || a >= out1) {
            return 0.0f;
        }
        if (a < in1) {
            return (a - in0) / (in1 - in0);
        }
        if (a > out0) {
            return 1.0f - (a - out0) / (out1 - out0);
        }
        return 1.0f;
    }

    /** Weicher Wechsel: die Haltung folgt der berechneten rasch (bei jeder Bildrate gleich), so springt nichts. */
    private void smooth(PhasmorRenderState state, float t) {
        Object key = state.owner;
        if (key == null) {
            return;
        }
        float[] last = LAST.computeIfAbsent(key, k -> new float[this.pose.length + 1]);
        float time = t / 20.0f;
        float dt = time - last[this.pose.length];
        if (dt < 0.0f || dt > 0.5f || last[this.pose.length] == 0.0f) {
            System.arraycopy(this.pose, 0, last, 0, this.pose.length);
        } else {
            float k = 1.0f - (float) Math.exp(-20.0f * dt);
            for (int i = 0; i < this.pose.length; i++) {
                last[i] += (this.pose[i] - last[i]) * k;
            }
            System.arraycopy(last, 0, this.pose, 0, this.pose.length);
        }
        last[this.pose.length] = time;
    }

    private void apply() {
        for (int b = 0; b < PhasmorPoses.BONES; b++) {
            ModelPart part = this.parts[b];
            int i = b * 6;
            part.xRot += this.pose[i] * Anim.DEG;
            part.yRot += this.pose[i + 1] * Anim.DEG;
            part.zRot += this.pose[i + 2] * Anim.DEG;
            part.x += this.pose[i + 3];
            part.y += this.pose[i + 4];
            part.z += this.pose[i + 5];
        }
    }

    /** Die Haltung zum Zeitpunkt {@code t} (Ticks) eines Ablaufs. */
    static void sample(int[][] keys, float t, float[] out) {
        if (t <= keys[0][0]) {
            copy(PhasmorPoses.POSES[keys[0][1]], out);
            return;
        }
        for (int k = 0; k < keys.length - 1; k++) {
            int[] from = keys[k];
            int[] to = keys[k + 1];
            if (t < to[0]) {
                float u = (t - from[0]) / Math.max(1.0f, to[0] - from[0]);
                u = switch (to[2]) {
                    case 1 -> u * u;
                    case 2 -> 1.0f - (1.0f - u) * (1.0f - u);
                    default -> u * u * (3.0f - 2.0f * u);
                };
                mix(PhasmorPoses.POSES[from[1]], PhasmorPoses.POSES[to[1]], u, out);
                return;
            }
        }
        copy(PhasmorPoses.POSES[keys[keys.length - 1][1]], out);
    }

    private static void copy(float[] from, float[] out) {
        System.arraycopy(from, 0, out, 0, out.length);
    }

    private static void mix(float[] a, float[] b, float u, float[] out) {
        for (int i = 0; i < out.length; i++) {
            out[i] = a[i] + (b[i] - a[i]) * u;
        }
    }
}
