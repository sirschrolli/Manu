package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.nether.IgnaraEntity;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Ignara, die Flammenpriesterin: Jeder Angriff ist eine Folge von Haltungen ({@link IgnaraPoses}), zwischen denen weich
 * überblendet wird – im Gebet das gesenkte Haupt, beim Peitschenhieb Ausholen und Zuschlagen, bei der Lavawoge die Arme
 * empor und hinab. Dazu kommen das Schweben (sacht auf und ab, der Umhang weht hinterher), das flackernde Flammenhaar und der
 * Saum aus Flammen, das kreisende Rad aus Opalsplittern, die pulsierende Feuerkugel, die Peitschenschnur, die als Welle
 * schwingt (beim Hieb schnalzt sie, bei der Flammenschlinge kreist sie), und in der Phönixgestalt die schlagenden Schwingen.
 */
public class IgnaraModel extends EntityModel<IgnaraRenderState> {
    /** Je Wesen die zuletzt gezeigte Haltung (für weiche Wechsel zwischen den Angriffen). */
    private static final Map<Object, float[]> LAST = new WeakHashMap<>();

    private final ModelPart[] parts = new ModelPart[IgnaraPoses.BONES];
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart hair;
    private final ModelPart[] hairPlanes = new ModelPart[4];
    private final ModelPart hem;
    private final ModelPart cape;
    private final ModelPart capeLow;
    private final ModelPart wheel;
    private final ModelPart orb;
    private final ModelPart[] lash = new ModelPart[6];
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftWingTip;
    private final ModelPart rightWingTip;
    private final float[] pose = new float[IgnaraPoses.BONES * 6];

    public IgnaraModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        ModelPart skirt = this.body.getChild("skirt");
        this.hem = skirt.getChild("hem");
        ModelPart torso = this.body.getChild("torso");
        this.head = torso.getChild("head");
        this.hair = this.head.getChild("hair");
        for (int i = 0; i < 4; i++) {
            this.hairPlanes[i] = this.hair.getChild("hair_" + (i + 1));
        }
        this.cape = torso.getChild("cape");
        this.capeLow = this.cape.getChild("cape_low");
        this.wheel = torso.getChild("wheel");
        ModelPart leftArm = torso.getChild("left_arm");
        ModelPart leftForearm = leftArm.getChild("left_forearm");
        ModelPart rightArm = torso.getChild("right_arm");
        ModelPart rightForearm = rightArm.getChild("right_forearm");
        ModelPart whip = rightForearm.getChild("whip");
        this.orb = leftForearm.getChild("orb");
        ModelPart seg = whip;
        for (int i = 0; i < this.lash.length; i++) {
            seg = seg.getChild("whip_" + (i + 1));
            this.lash[i] = seg;
        }
        this.leftWing = torso.getChild("left_wing");
        this.rightWing = torso.getChild("right_wing");
        this.leftWingTip = this.leftWing.getChild("left_wing_tip");
        this.rightWingTip = this.rightWing.getChild("right_wing_tip");
        this.parts[IgnaraPoses.BODY] = this.body;
        this.parts[IgnaraPoses.HEAD] = this.head;
        this.parts[IgnaraPoses.LEFT_ARM] = leftArm;
        this.parts[IgnaraPoses.LEFT_FOREARM] = leftForearm;
        this.parts[IgnaraPoses.RIGHT_ARM] = rightArm;
        this.parts[IgnaraPoses.RIGHT_FOREARM] = rightForearm;
        this.parts[IgnaraPoses.WHIP] = whip;
    }

    @Override
    public void setupAnim(IgnaraRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        IgnaraEntity.Attack attack = IgnaraEntity.Attack.values()[Mth.clamp(state.attack, 0, IgnaraEntity.Attack.values().length - 1)];
        float a = state.attackTime;
        int[][] timeline = switch (attack) {
            case AWAKEN -> IgnaraPoses.AWAKEN;
            case LASH -> IgnaraPoses.LASH;
            case SNARE -> IgnaraPoses.SNARE;
            case WAVE -> IgnaraPoses.WAVE;
            case PILLARS -> IgnaraPoses.PILLARS;
            case OPAL_RAIN -> IgnaraPoses.OPAL_RAIN;
            case CALL -> IgnaraPoses.CALL;
            case PHOENIX -> IgnaraPoses.PHOENIX;
            case DIVE -> IgnaraPoses.DIVE;
            default -> null;
        };
        if (attack == IgnaraEntity.Attack.PRAY) {
            copy(IgnaraPoses.POSES[IgnaraPoses.P_PRAY], this.pose);
        } else if (timeline == null) {
            copy(IgnaraPoses.POSES[IgnaraPoses.P_READY], this.pose);
        } else {
            sample(timeline, a, this.pose);
        }
        if (state.deathTime > 0.0f) {
            float d = Mth.clamp(state.deathTime / 24.0f, 0.0f, 1.0f);
            mix(this.pose, IgnaraPoses.POSES[IgnaraPoses.P_DEATH], d * d * (3.0f - 2.0f * d), this.pose);
        }
        this.smooth(state, t);
        this.apply();

        // das Schweben: sacht auf und ab, ein wenig in die Fahrt geneigt
        this.body.y += Mth.sin(t * 0.08f) * 0.6f;
        this.body.xRot += Mth.clamp(state.drift * 2.2f, 0.0f, 0.35f);
        // Blick zum Gegner (im Gebet und beim Erwachen lenkt die Haltung den Kopf)
        if (attack != IgnaraEntity.Attack.PRAY && attack != IgnaraEntity.Attack.AWAKEN && state.deathTime <= 0.0f) {
            float look = attack == IgnaraEntity.Attack.NONE ? 0.8f : 0.4f;
            this.head.yRot += Mth.clamp(state.yRot, -45.0f, 45.0f) * Anim.DEG * look;
            this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * Anim.DEG * look;
        }
        // das Flammenhaar und der Saum flackern; im Tod verlöschen sie
        float fade = state.deathTime > 0.0f ? Mth.clamp(1.0f - state.deathTime / IgnaraEntity.DEATH_TICKS, 0.05f, 1.0f) : 1.0f;
        float blaze = (state.phoenix ? 1.3f : 1.0f) * fade;
        this.hair.yScale = blaze * (1.0f + Mth.sin(t * 0.5f) * 0.1f + Mth.sin(t * 1.3f) * 0.05f);
        this.hair.xScale = 1.0f + Mth.sin(t * 0.37f + 1.0f) * 0.06f;
        for (int i = 0; i < this.hairPlanes.length; i++) {
            this.hairPlanes[i].yRot += Mth.sin(t * (0.3f + i * 0.05f) + i * 1.7f) * 0.12f;
        }
        this.hem.yScale = fade * (1.0f + Mth.sin(t * 0.6f) * 0.15f + Mth.sin(t * 1.7f + 2.0f) * 0.06f);
        // der Umhang weht hinter ihr her
        float flow = Mth.clamp(state.drift * 6.0f, 0.0f, 0.9f);
        this.cape.xRot += flow * 0.7f + Mth.sin(t * 0.09f) * 0.04f;
        this.capeLow.xRot += flow * 0.4f + Mth.sin(t * 0.11f + 1.0f) * 0.05f;
        // das Rad aus Opalsplittern kreist (beim Opalregen und als Phönix schneller)
        float spin = attack == IgnaraEntity.Attack.OPAL_RAIN ? 0.35f : state.phoenix ? 0.12f : 0.04f;
        this.wheel.zRot = t * spin;
        // die Feuerkugel pulsiert
        float pulse = 1.0f + Mth.sin(t * 0.35f) * 0.12f;
        this.orb.xScale = this.orb.yScale = this.orb.zScale = pulse * fade;
        this.orb.yRot = t * 0.15f;
        // die Peitschenschnur: eine Welle, die zur Spitze läuft; beim Hieb schnalzt sie, bei der Flammenschlinge kreist sie
        this.animateLash(attack, a, t);
        // die Schwingen der Phönixgestalt
        boolean wings = state.phoenix || attack == IgnaraEntity.Attack.PHOENIX && a >= 24.0f;
        this.leftWing.visible = wings;
        this.rightWing.visible = wings;
        if (wings) {
            float open = attack == IgnaraEntity.Attack.PHOENIX ? Mth.clamp((a - 24.0f) / 8.0f, 0.0f, 1.0f) : 1.0f;
            float beat = Mth.sin(t * (attack == IgnaraEntity.Attack.DIVE ? 0.6f : 0.25f));
            float sweep = attack == IgnaraEntity.Attack.DIVE && a > 12.0f ? -0.5f : 0.0f;
            this.leftWing.yRot += (-0.3f + beat * 0.25f + sweep) * open;
            this.rightWing.yRot -= (-0.3f + beat * 0.25f + sweep) * open;
            this.leftWingTip.yRot += beat * 0.2f;
            this.rightWingTip.yRot -= beat * 0.2f;
            this.leftWing.xScale = this.leftWing.yScale = this.leftWing.zScale = 0.2f + 0.8f * open;
            this.rightWing.xScale = this.rightWing.yScale = this.rightWing.zScale = 0.2f + 0.8f * open;
        }
    }

    /** Die Glieder der Schnur biegen sich als Welle, die zur Spitze läuft. */
    private void animateLash(IgnaraEntity.Attack attack, float a, float t) {
        float amp;
        float speed;
        float curl = 0.0f;
        switch (attack) {
            case LASH -> {
                // beim Ausholen hängt die Schnur zurück, beim Hieb schnalzt sie nach vorn
                float k = a < 12.0f ? a / 12.0f : Mth.clamp(1.0f - (a - 12.0f) / 10.0f, 0.0f, 1.0f);
                amp = 0.25f + 0.35f * k;
                speed = 0.9f;
                curl = a < 12.0f ? 0.25f * k : -0.18f * k;
            }
            case SNARE, AWAKEN -> {
                amp = 0.3f;
                speed = 1.2f;
                curl = attack == IgnaraEntity.Attack.SNARE && a > IgnaraEntity.SNARE_THROW && a < IgnaraEntity.SNARE_STRIKE ? -0.12f : 0.1f;
            }
            case PRAY -> {
                amp = 0.05f;
                speed = 0.1f;
                curl = 0.32f;
            }
            default -> {
                amp = 0.12f;
                speed = 0.25f;
                curl = 0.12f;
            }
        }
        for (int i = 0; i < this.lash.length; i++) {
            float phase = t * speed - i * 0.9f;
            this.lash[i].xRot += curl + Mth.sin(phase) * amp * (0.6f + i * 0.12f);
            this.lash[i].zRot += Mth.cos(phase * 0.7f) * amp * 0.5f;
        }
        if (attack == IgnaraEntity.Attack.SNARE && a < IgnaraEntity.SNARE_THROW) {
            // über dem Kopf kreisen
            this.parts[IgnaraPoses.WHIP].yRot += a * 0.9f;
        }
    }

    /** Weicher Wechsel: die Haltung folgt der berechneten rasch (bei jeder Bildrate gleich), so springt nichts. */
    private void smooth(IgnaraRenderState state, float t) {
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
        for (int b = 0; b < IgnaraPoses.BONES; b++) {
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
            copy(IgnaraPoses.POSES[keys[0][1]], out);
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
                mix(IgnaraPoses.POSES[from[1]], IgnaraPoses.POSES[to[1]], u, out);
                return;
            }
        }
        copy(IgnaraPoses.POSES[keys[keys.length - 1][1]], out);
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
