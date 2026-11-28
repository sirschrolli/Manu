package io.github.sirschrolli.kingdomsatwar.client.renderer.arena;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.arena.VentorEntity;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

/**
 * Ventor, der Gladiator der Winde: Jeder Angriff ist eine Folge von Haltungen ({@link VentorPoses}), zwischen denen weich
 * überblendet wird. Darunter dreht sich der Wirbel (jeder Ring schneller als der über ihm), wankt hin und her und neigt sich,
 * wenn er gleitet; der rote Schurz und der Schweif des Helmbuschs wehen, das Netz pendelt. Im Zyklon dreht er sich um sich
 * selbst, im Sterben erlahmt der Wind (die Ringe schrumpfen, das Standbild stürzt).
 *
 * <p>Dasselbe Modell bewegt auch die Wind-Ebene (gleiche Knochen, nur der Dunst der Ringe) – so passt der Dunst immer genau.
 */
public class VentorModel extends EntityModel<VentorRenderState> {
    private static final int RINGS = 4;

    /** Je Wesen die zuletzt gezeigte Haltung (für weiche Wechsel zwischen den Angriffen) – je Modell eigens. */
    private final Map<Object, float[]> last = new WeakHashMap<>();
    private final ModelPart[] parts = new ModelPart[VentorPoses.BONES];
    private final ModelPart wind;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart[] rings = new ModelPart[RINGS];
    private final ModelPart trident;
    private final ModelPart net;
    private final ModelPart netA;
    private final ModelPart netB;
    private final ModelPart loinFront;
    private final ModelPart loinBack;
    private final ModelPart crestTail;
    private final float[] pose = new float[VentorPoses.BONES * 6];

    public VentorModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.wind = root.getChild("wind");
        this.body = root.getChild("body");
        ModelPart ring = this.wind;
        for (int i = 0; i < RINGS; i++) {
            ring = ring.getChild("ring_" + i);
            this.rings[i] = ring;
        }
        this.head = this.body.getChild("head");
        ModelPart armR = this.body.getChild("arm_r");
        ModelPart forearmR = armR.getChild("forearm_r");
        ModelPart handR = forearmR.getChild("hand_r");
        ModelPart armL = this.body.getChild("arm_l");
        ModelPart forearmL = armL.getChild("forearm_l");
        ModelPart handL = forearmL.getChild("hand_l");
        this.trident = handR.getChild("trident");
        this.net = handL.getChild("net");
        this.netA = this.net.getChild("net_a");
        this.netB = this.net.getChild("net_b");
        this.loinFront = this.body.getChild("loin_front");
        this.loinBack = this.body.getChild("loin_back");
        this.crestTail = this.head.getChild("crest_tail");

        this.parts[VentorPoses.WIND] = this.wind;
        this.parts[VentorPoses.BODY] = this.body;
        this.parts[VentorPoses.HEAD] = this.head;
        this.parts[VentorPoses.ARM_R] = armR;
        this.parts[VentorPoses.FOREARM_R] = forearmR;
        this.parts[VentorPoses.HAND_R] = handR;
        this.parts[VentorPoses.ARM_L] = armL;
        this.parts[VentorPoses.FOREARM_L] = forearmL;
        this.parts[VentorPoses.HAND_L] = handL;
        this.parts[VentorPoses.TRIDENT] = this.trident;
        this.parts[VentorPoses.NET] = this.net;
        this.parts[VentorPoses.LOIN_FRONT] = this.loinFront;
        this.parts[VentorPoses.LOIN_BACK] = this.loinBack;
        this.parts[VentorPoses.CREST_TAIL] = this.crestTail;
    }

    @Override
    public void setupAnim(VentorRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        VentorEntity.Attack[] attacks = VentorEntity.Attack.values();
        VentorEntity.Attack attack = attacks[Mth.clamp(state.attack, 0, attacks.length - 1)];
        float a = state.attackTime;
        int[][] timeline = switch (attack) {
            case AWAKEN -> VentorPoses.AWAKEN;
            case RAGE -> VentorPoses.RAGE;
            case RETREAT -> VentorPoses.RETREAT;
            case GUST -> VentorPoses.GUST;
            case THRUST -> VentorPoses.THRUST;
            case NET -> VentorPoses.NET_THROW;
            case THROW -> VentorPoses.THROW;
            case CHARGES -> VentorPoses.CHARGES;
            case VORTEX -> VentorPoses.VORTEX;
            case DIVE -> VentorPoses.DIVE;
            case CYCLONE -> VentorPoses.CYCLONE;
            default -> null;
        };
        if (state.deathTime > 0.0f) {
            sample(VentorPoses.DEATH, state.deathTime, this.pose);
        } else if (timeline == null) {
            copy(VentorPoses.P_IDLE, this.pose);
        } else {
            sample(timeline, a, this.pose);
        }
        this.smooth(state, t);
        this.apply();

        // ---- der Wirbel: jeder Ring dreht sich schneller als der über ihm, wankt, und neigt sich, wenn er gleitet
        float storm = state.enraged ? 1.35f : 1.0f;
        float drift = Mth.clamp(state.drift * 2.5f, 0.0f, 1.0f);
        for (int i = 0; i < RINGS; i++) {
            ModelPart ring = this.rings[i];
            ring.yRot += t * (0.16f + i * 0.05f) * storm;
            if (i > 0) {
                ring.x += Mth.sin(t * 0.13f + i * 0.9f) * 0.45f * i;
                ring.z += Mth.cos(t * 0.11f + i * 1.3f) * 0.35f * i;
            }
        }
        this.wind.xRot += drift * 0.45f + Mth.sin(t * 0.07f) * 0.03f;
        this.wind.zRot += Mth.sin(t * 0.09f + 1.0f) * 0.035f;
        // er schwebt: ein ruhiges Auf und Ab
        float bob = Mth.sin(t * 0.1f) * 0.5f;
        this.body.y += bob;
        this.wind.y += bob * 0.5f;

        // ---- der Zyklon: er dreht sich um sich selbst (anfangs langsam, dann rasend, zum Schluss auslaufend)
        if (attack == VentorEntity.Attack.CYCLONE && state.deathTime <= 0.0f) {
            float spin = spinAngle(a);
            this.body.yRot += spin;
            this.wind.yRot += spin * 1.3f;
        }

        // ---- Blick, Schurz, Helmbusch, Netz
        boolean free = attack == VentorEntity.Attack.NONE && state.deathTime <= 0.0f;
        if (free) {
            this.head.yRot += Mth.clamp(state.yRot, -45.0f, 45.0f) * Anim.DEG * 0.7f;
            this.head.xRot += Mth.clamp(state.xRot, -25.0f, 25.0f) * Anim.DEG * 0.7f;
        }
        float flutter = 0.08f + drift * 0.25f + (state.enraged ? 0.06f : 0.0f);
        this.loinFront.xRot += Mth.sin(t * 0.45f) * flutter - drift * 0.5f;
        this.loinBack.xRot += Mth.sin(t * 0.41f + 1.2f) * flutter + drift * 0.35f;
        this.crestTail.xRot += Mth.sin(t * 0.33f) * flutter * 0.8f + drift * 0.3f;
        this.crestTail.zRot += Mth.sin(t * 0.27f + 0.5f) * 0.06f;
        this.netA.zRot += Mth.sin(t * 0.21f) * 0.07f;
        this.netA.xRot += Mth.sin(t * 0.17f + 0.8f) * 0.05f + drift * 0.3f;
        this.netB.zRot += Mth.sin(t * 0.19f + 2.0f) * 0.07f;
        this.netB.xRot += Mth.sin(t * 0.23f + 1.6f) * 0.05f + drift * 0.3f;

        // ---- im Sterben erlahmt der Wind: die Ringe schrumpfen und vergehen
        if (state.deathTime > 0.0f) {
            float d = Mth.clamp((state.deathTime - 6.0f) / 40.0f, 0.0f, 1.0f);
            float s = Math.max(0.001f, 1.0f - d * d);
            this.wind.xScale = s;
            this.wind.zScale = s;
            this.wind.yScale = Math.max(0.001f, 1.0f - d);
        }

        // ---- was zu sehen ist: Dreizack und Netz, solange er sie in der Hand hat
        boolean netOut = attack == VentorEntity.Attack.NET && a >= VentorEntity.NET_RELEASE;
        boolean tridentOut = attack == VentorEntity.Attack.THROW && a >= VentorEntity.THROW_RELEASE;
        this.trident.visible = state.trident && !tridentOut;
        this.net.visible = state.net && !netOut;
    }

    /** Die Drehung im Zyklon (Bogenmaß) zum Zeitpunkt {@code a} des Angriffs. */
    static float spinAngle(float a) {
        float from = VentorEntity.CYCLONE_FROM;
        float to = VentorEntity.CYCLONE_TO + 6.0f;
        float u = Mth.clamp((a - from) / (to - from), 0.0f, 1.0f);
        // Winkel = Integral einer Geschwindigkeit, die an- und ausläuft (bis zu ~0,7 rad je Tick)
        float ease = u * u * u * (u * (u * 6.0f - 15.0f) + 10.0f);
        return ease * (to - from) * 0.55f * Mth.TWO_PI / 6.0f;
    }

    /** Weicher Wechsel: die Haltung folgt der berechneten rasch (bei jeder Bildrate gleich), so springt nichts. */
    private void smooth(VentorRenderState state, float t) {
        Object key = state.owner;
        if (key == null) {
            return;
        }
        float[] last = this.last.computeIfAbsent(key, k -> new float[this.pose.length + 1]);
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
        for (int b = 0; b < VentorPoses.BONES; b++) {
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
                for (int i = 0; i < out.length; i++) {
                    out[i] = a[i] + (b[i] - a[i]) * u;
                }
                return;
            }
        }
        copy(keys[keys.length - 1][1], out);
    }

    private static void copy(int index, float[] out) {
        System.arraycopy(VentorPoses.POSES[index], 0, out, 0, out.length);
    }
}
