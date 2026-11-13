package io.github.sirschrolli.kingdomsatwar.client.renderer.castle;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.castle.RobberKnightEntity;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Kuno von Rabenfels, der Raubritter, und sein Rappe: Jeder Angriff ist eine Folge von Haltungen ({@link RobberKnightPoses}),
 * zwischen denen weich überblendet wird. Zu Ross sitzt der Reiter im Sattel und folgt dem Ross (auch wenn es steigt); das Ross
 * geht und galoppiert (beim Lanzengang mit langen Sätzen), wirft den Kopf, schlägt mit dem Schweif; die Schabracke schwingt,
 * der Wimpel flattert. Zu Fuß blendet das Modell das Ross aus – der Reiter steht auf dem Boden und führt den Morgenstern.
 */
public class RobberKnightModel extends EntityModel<RobberKnightRenderState> {
    /** Je Wesen die zuletzt gezeigte Haltung (für weiche Wechsel zwischen den Angriffen). */
    private static final Map<Object, float[]> LAST = new WeakHashMap<>();
    /** Je Haltung: sitzt er im Sattel? (Als Zahl, damit sich der Sitz im Sattel überblenden lässt.) */
    private static final int WEIGHT = RobberKnightPoses.BONES * 6;

    private final ModelPart[] parts = new ModelPart[RobberKnightPoses.BONES];
    private final ModelPart horse;
    private final ModelPart knight;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart tailLow;
    private final ModelPart maneDrape;
    private final ModelPart[] ears = new ModelPart[2];
    private final ModelPart[] legs = new ModelPart[4];
    private final ModelPart[] shins = new ModelPart[4];
    private final ModelPart trapperL;
    private final ModelPart trapperR;
    private final ModelPart trapperFront;
    private final ModelPart trapperBack;
    private final ModelPart torso;
    private final ModelPart helm;
    private final ModelPart lance;
    private final ModelPart pennant;
    private final ModelPart mace;
    private final ModelPart hornBelt;
    private final ModelPart hornHand;
    private final ModelPart cape;
    private final ModelPart[] wings = new ModelPart[2];
    private final float[] pose = new float[WEIGHT + 1];

    public RobberKnightModel(ModelPart root) {
        super(root);
        this.horse = root.getChild("horse");
        this.knight = root.getChild("knight");
        this.neck = this.horse.getChild("neck");
        this.head = this.neck.getChild("head");
        this.maneDrape = this.neck.getChild("mane_drape");
        this.ears[0] = this.head.getChild("ear_l");
        this.ears[1] = this.head.getChild("ear_r");
        this.tail = this.horse.getChild("tail");
        this.tailLow = this.tail.getChild("tail_low");
        String[] legNames = {"fl", "fr", "hl", "hr"};
        for (int i = 0; i < 4; i++) {
            this.legs[i] = this.horse.getChild("leg_" + legNames[i]);
            this.shins[i] = this.legs[i].getChild("shin_" + legNames[i]);
        }
        this.trapperL = this.horse.getChild("trapper_l");
        this.trapperR = this.horse.getChild("trapper_r");
        this.trapperFront = this.horse.getChild("trapper_front");
        this.trapperBack = this.horse.getChild("trapper_back");
        this.torso = this.knight.getChild("torso");
        this.helm = this.torso.getChild("helm");
        ModelPart crest = this.helm.getChild("crest");
        this.wings[0] = crest.getChild("wing_l");
        this.wings[1] = crest.getChild("wing_r");
        ModelPart armR = this.torso.getChild("arm_r");
        ModelPart forearmR = armR.getChild("forearm_r");
        ModelPart handR = forearmR.getChild("hand_r");
        ModelPart armL = this.torso.getChild("arm_l");
        ModelPart forearmL = armL.getChild("forearm_l");
        ModelPart handL = forearmL.getChild("hand_l");
        this.lance = handR.getChild("lance");
        this.pennant = this.lance.getChild("pennant");
        this.mace = handR.getChild("mace");
        this.hornBelt = this.torso.getChild("horn");
        this.hornHand = handL.getChild("horn_hand");
        this.cape = this.torso.getChild("cape");
        ModelPart thighR = this.knight.getChild("thigh_r");
        ModelPart thighL = this.knight.getChild("thigh_l");

        this.parts[RobberKnightPoses.KNIGHT] = this.knight;
        this.parts[RobberKnightPoses.TORSO] = this.torso;
        this.parts[RobberKnightPoses.HELM] = this.helm;
        this.parts[RobberKnightPoses.ARM_R] = armR;
        this.parts[RobberKnightPoses.FOREARM_R] = forearmR;
        this.parts[RobberKnightPoses.HAND_R] = handR;
        this.parts[RobberKnightPoses.ARM_L] = armL;
        this.parts[RobberKnightPoses.FOREARM_L] = forearmL;
        this.parts[RobberKnightPoses.HAND_L] = handL;
        this.parts[RobberKnightPoses.THIGH_R] = thighR;
        this.parts[RobberKnightPoses.SHIN_R] = thighR.getChild("shin_r");
        this.parts[RobberKnightPoses.THIGH_L] = thighL;
        this.parts[RobberKnightPoses.SHIN_L] = thighL.getChild("shin_l");
        this.parts[RobberKnightPoses.LANCE] = this.lance;
        this.parts[RobberKnightPoses.MACE] = this.mace;
        this.parts[RobberKnightPoses.SHIELD] = forearmL.getChild("shield");
        this.parts[RobberKnightPoses.TABARD] = this.torso.getChild("tabard");
        this.parts[RobberKnightPoses.TABARD_BACK] = this.torso.getChild("tabard_back");
        this.parts[RobberKnightPoses.CAPE] = this.cape;
        this.parts[RobberKnightPoses.HORSE] = this.horse;
        this.parts[RobberKnightPoses.NECK] = this.neck;
        this.parts[RobberKnightPoses.HEAD] = this.head;
        this.parts[RobberKnightPoses.LEG_FL] = this.legs[0];
        this.parts[RobberKnightPoses.SHIN_FL] = this.shins[0];
        this.parts[RobberKnightPoses.LEG_FR] = this.legs[1];
        this.parts[RobberKnightPoses.SHIN_FR] = this.shins[1];
        this.parts[RobberKnightPoses.LEG_HL] = this.legs[2];
        this.parts[RobberKnightPoses.SHIN_HL] = this.shins[2];
        this.parts[RobberKnightPoses.LEG_HR] = this.legs[3];
        this.parts[RobberKnightPoses.SHIN_HR] = this.shins[3];
        this.parts[RobberKnightPoses.TAIL] = this.tail;
    }

    @Override
    public void setupAnim(RobberKnightRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        RobberKnightEntity.Attack[] attacks = RobberKnightEntity.Attack.values();
        RobberKnightEntity.Attack attack = attacks[Mth.clamp(state.attack, 0, attacks.length - 1)];
        float a = state.attackTime;
        boolean mounted = state.mounted;
        int[][] timeline = switch (attack) {
            case AWAKEN -> RobberKnightPoses.AWAKEN;
            case CHARGE -> RobberKnightPoses.CHARGE;
            case HALT -> RobberKnightPoses.HALT;
            case THRUST -> RobberKnightPoses.THRUST;
            case TRAMPLE -> RobberKnightPoses.TRAMPLE;
            case HORN -> mounted ? RobberKnightPoses.HORN_RIDE : RobberKnightPoses.HORN_FOOT;
            case VOLLEY -> mounted ? RobberKnightPoses.VOLLEY_RIDE : RobberKnightPoses.VOLLEY_FOOT;
            case STAGGER -> RobberKnightPoses.STAGGER;
            case UNHORSE -> RobberKnightPoses.UNHORSE;
            case SMASH -> RobberKnightPoses.SMASH;
            case WHIRL -> RobberKnightPoses.WHIRL;
            case BASH -> RobberKnightPoses.BASH;
            case WALL -> RobberKnightPoses.WALL;
            default -> null;
        };
        if (attack == RobberKnightEntity.Attack.GUARD) {
            copy(RobberKnightPoses.P_RIDE_GUARD, this.pose);
        } else if (timeline == null) {
            copy(mounted ? RobberKnightPoses.P_RIDE_READY : RobberKnightPoses.P_FOOT_READY, this.pose);
        } else {
            sample(timeline, a, this.pose);
        }
        if (state.deathTime > 0.0f) {
            float d = Mth.clamp(state.deathTime / 40.0f, 0.0f, 1.0f);
            float[] target = new float[this.pose.length];
            copy(mounted ? RobberKnightPoses.P_RIDE_STAGGER : RobberKnightPoses.P_DEATH, target);
            mix(this.pose, target, d * d * (3.0f - 2.0f * d), this.pose);
        }
        this.smooth(state, t);
        this.apply();

        // ---- das Ross: Schritt und Galopp
        boolean charging = attack == RobberKnightEntity.Attack.CHARGE && a >= RobberKnightEntity.CHARGE_AIM;
        boolean rearing = attack == RobberKnightEntity.Attack.AWAKEN || attack == RobberKnightEntity.Attack.TRAMPLE
                || attack == RobberKnightEntity.Attack.UNHORSE || attack == RobberKnightEntity.Attack.HALT;
        float speed = mounted ? Mth.clamp(state.walkAnimationSpeed * 1.2f, 0.0f, 1.0f) : 0.0f;
        if (charging) {
            speed = Math.max(speed, 1.0f);
        }
        if (rearing) {
            speed *= 0.3f;
        }
        if (speed > 0.01f) {
            float p = state.walkAnimationPos * (charging ? 0.42f : 0.55f);
            float amp = (charging ? 1.0f : 0.7f) * speed;
            float[] phase = {0.0f, 0.45f, Mth.PI, Mth.PI + 0.45f};
            for (int i = 0; i < 4; i++) {
                float ph = p + phase[i];
                this.legs[i].xRot += Mth.cos(ph) * amp;
                this.shins[i].xRot += Math.max(0.0f, -Mth.sin(ph)) * amp * 1.3f;
            }
            this.horse.y -= Math.abs(Mth.sin(p)) * (charging ? 1.2f : 0.6f) * speed;
            this.horse.xRot += Mth.sin(p) * 0.06f * speed;
            this.neck.xRot += Mth.sin(p + 0.6f) * 0.12f * speed;
            this.tail.xRot += 0.45f * speed;
            this.tailLow.xRot += Mth.sin(p * 2.0f) * 0.1f * speed;
        }
        // in Ruhe: Kopfnicken, ein Ohr zuckt, der Schweif pendelt
        this.neck.xRot += Mth.sin(t * 0.05f) * 0.035f;
        this.head.xRot += Mth.sin(t * 0.07f + 1.0f) * 0.03f;
        this.ears[0].zRot += Mth.sin(t * 0.21f) > 0.95f ? 0.25f : 0.0f;
        this.ears[1].zRot -= Mth.sin(t * 0.17f + 2.0f) > 0.95f ? 0.25f : 0.0f;
        this.tail.zRot += Mth.sin(t * 0.06f) * 0.1f;
        this.tailLow.zRot += Mth.sin(t * 0.06f - 0.6f) * 0.08f;
        this.maneDrape.zRot += Mth.sin(t * 0.08f) * 0.03f + speed * 0.12f;
        // die Schabracke schwingt (im Galopp weht sie nach hinten)
        float sway = Mth.sin(t * 0.09f) * 0.012f + speed * 0.06f;
        this.trapperL.zRot -= sway;
        this.trapperR.zRot += sway;
        this.trapperFront.xRot -= speed * 0.18f;
        this.trapperBack.xRot += speed * 0.14f + Mth.sin(t * 0.08f) * 0.01f;

        // ---- der Reiter folgt dem Sattel (überblendet, wenn er stürzt)
        float w = this.pose[WEIGHT];
        if (w > 0.001f) {
            float ax = this.horse.xRot;
            float sy = RobberKnightPoses.SEAT_Y - RobberKnightPoses.HORSE_PIVOT_Y;
            float sz = RobberKnightPoses.SEAT_Z;
            float ny = sy * Mth.cos(ax) - sz * Mth.sin(ax);
            float nz = sy * Mth.sin(ax) + sz * Mth.cos(ax);
            this.knight.x += w * this.horse.x;
            this.knight.y += w * ((this.horse.y - RobberKnightPoses.HORSE_PIVOT_Y) + (ny - sy));
            this.knight.z += w * (this.horse.z + (nz - sz));
            this.knight.xRot += w * ax;
        }

        // ---- der Reiter: Atmen, Blick, Mantel, Wimpel
        boolean free = attack == RobberKnightEntity.Attack.NONE && state.deathTime <= 0.0f;
        this.torso.y += Mth.sin(t * 0.07f) * 0.15f;
        if (free || attack == RobberKnightEntity.Attack.GUARD) {
            float look = free ? 0.7f : 0.3f;
            this.helm.yRot += Mth.clamp(state.yRot, -45.0f, 45.0f) * Anim.DEG * look;
            this.helm.xRot += Mth.clamp(state.xRot, -25.0f, 25.0f) * Anim.DEG * look;
        }
        float wind = Mth.clamp(state.drift * 2.2f, 0.0f, 1.0f);
        this.cape.xRot += wind * 0.6f + Mth.sin(t * 0.08f) * 0.02f;
        this.pennant.yRot += Mth.sin(t * (0.25f + wind * 0.5f)) * (0.12f + wind * 0.25f);
        this.pennant.zRot += Mth.sin(t * 0.31f + 1.0f) * 0.06f;
        for (int i = 0; i < 2; i++) {
            this.wings[i].zRot += (i == 0 ? -1.0f : 1.0f) * Mth.sin(t * 0.05f) * 0.03f;
        }

        // ---- was zu sehen ist
        this.horse.visible = mounted;
        this.lance.visible = mounted && state.lance && !(attack == RobberKnightEntity.Attack.UNHORSE && a >= 16.0f);
        this.mace.visible = !mounted && !(attack == RobberKnightEntity.Attack.UNHORSE && a < RobberKnightEntity.UNHORSE_MACE);
        boolean blowing = attack == RobberKnightEntity.Attack.HORN && a >= 6.0f && a < 38.0f;
        this.hornHand.visible = blowing;
        this.hornBelt.visible = !blowing;
    }

    /** Weicher Wechsel: die Haltung folgt der berechneten rasch (bei jeder Bildrate gleich), so springt nichts. */
    private void smooth(RobberKnightRenderState state, float t) {
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
            float k = 1.0f - (float) Math.exp(-18.0f * dt);
            for (int i = 0; i < this.pose.length; i++) {
                last[i] += (this.pose[i] - last[i]) * k;
            }
            System.arraycopy(last, 0, this.pose, 0, this.pose.length);
        }
        last[this.pose.length] = time;
    }

    private void apply() {
        for (int b = 0; b < RobberKnightPoses.BONES; b++) {
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

    /** Die Haltung (und ob er im Sattel sitzt) zum Zeitpunkt {@code t} (Ticks) eines Ablaufs. */
    static void sample(int[][] keys, float t, float[] out) {
        if (t <= keys[0][0]) {
            copy(keys[0][1], out);
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
                float[] a = new float[out.length];
                float[] b = new float[out.length];
                copy(from[1], a);
                copy(to[1], b);
                mix(a, b, u, out);
                return;
            }
        }
        copy(keys[keys.length - 1][1], out);
    }

    /** Haltung {@code index} samt Sitz im Sattel (1 oder 0) nach {@code out}. */
    private static void copy(int index, float[] out) {
        System.arraycopy(RobberKnightPoses.POSES[index], 0, out, 0, WEIGHT);
        out[WEIGHT] = RobberKnightPoses.MOUNTED[index] ? 1.0f : 0.0f;
    }

    private static void mix(float[] a, float[] b, float u, float[] out) {
        for (int i = 0; i < out.length; i++) {
            out[i] = a[i] + (b[i] - a[i]) * u;
        }
    }
}
