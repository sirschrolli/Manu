package io.github.sirschrolli.kingdomsatwar.client.renderer.deep;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.deep.SordinEntity;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Sordin, der Kantor der Stille: Jeder Angriff ist eine Folge von Haltungen ({@link SordinPoses}), zwischen denen weich
 * überblendet wird – an der Orgel spielt er (die Finger laufen über die Tasten, der Kopf wiegt sich), beim Erwachen lauscht
 * er, reckt die Stimmgabel empor und schlägt sie an; beim Akkord dirigiert er. Dazu kommen kleine, gleitende Schritte (die
 * Kutte schwingt mit), die Pfeifen auf seinem Rücken, die beim Singen beben, und die Fühler aus Sculk, die jeder
 * Erschütterung nachtasten.
 */
public class SordinModel extends EntityModel<SordinRenderState> {
    /** Je Wesen die zuletzt gezeigte Haltung (für weiche Wechsel zwischen den Angriffen). */
    private static final Map<Object, float[]> LAST = new WeakHashMap<>();
    private static final int PIPES = 7;

    private final ModelPart[] parts = new ModelPart[SordinPoses.BONES];
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart fork;
    private final ModelPart skirt;
    private final ModelPart skirtLow;
    private final ModelPart pipes;
    private final ModelPart[] pipe = new ModelPart[PIPES];
    private final ModelPart[] feelers = new ModelPart[6];
    private final float[] pose = new float[SordinPoses.BONES * 6];

    public SordinModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.skirt = this.body.getChild("skirt");
        this.skirtLow = this.skirt.getChild("skirt_low");
        this.pipes = this.body.getChild("pipes");
        for (int i = 0; i < PIPES; i++) {
            this.pipe[i] = this.pipes.getChild("pipe_" + i);
        }
        int k = 0;
        for (String side : new String[] {"l", "r"}) {
            ModelPart a = this.head.getChild("feeler_" + side + "_0");
            ModelPart b = a.getChild("feeler_" + side + "_1");
            ModelPart c = b.getChild("feeler_" + side + "_2");
            this.feelers[k++] = a;
            this.feelers[k++] = b;
            this.feelers[k++] = c;
        }
        ModelPart leftArm = this.body.getChild("left_arm");
        ModelPart rightArm = this.body.getChild("right_arm");
        ModelPart leftForearm = leftArm.getChild("left_forearm");
        ModelPart rightForearm = rightArm.getChild("right_forearm");
        ModelPart leftHand = leftForearm.getChild("left_hand");
        ModelPart rightHand = rightForearm.getChild("right_hand");
        this.fork = rightHand.getChild("fork");
        ModelPart leftLeg = this.body.getChild("left_leg");
        ModelPart rightLeg = this.body.getChild("right_leg");
        this.parts[SordinPoses.BODY] = this.body;
        this.parts[SordinPoses.HEAD] = this.head;
        this.parts[SordinPoses.LEFT_ARM] = leftArm;
        this.parts[SordinPoses.LEFT_FOREARM] = leftForearm;
        this.parts[SordinPoses.LEFT_HAND] = leftHand;
        this.parts[SordinPoses.RIGHT_ARM] = rightArm;
        this.parts[SordinPoses.RIGHT_FOREARM] = rightForearm;
        this.parts[SordinPoses.RIGHT_HAND] = rightHand;
        this.parts[SordinPoses.FORK] = this.fork;
        this.parts[SordinPoses.LEFT_LEG] = leftLeg;
        this.parts[SordinPoses.LEFT_SHIN] = leftLeg.getChild("left_shin");
        this.parts[SordinPoses.RIGHT_LEG] = rightLeg;
        this.parts[SordinPoses.RIGHT_SHIN] = rightLeg.getChild("right_shin");
        this.parts[SordinPoses.SKIRT] = this.skirt;
        this.parts[SordinPoses.SKIRT_LOW] = this.skirtLow;
        this.parts[SordinPoses.PIPES] = this.pipes;
    }

    @Override
    public void setupAnim(SordinRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        SordinEntity.Attack[] attacks = SordinEntity.Attack.values();
        SordinEntity.Attack attack = attacks[Mth.clamp(state.attack, 0, attacks.length - 1)];
        float a = state.attackTime;
        int[][] timeline = switch (attack) {
            case AWAKEN -> SordinPoses.AWAKEN;
            case SWING -> SordinPoses.SWING;
            case SONIC -> SordinPoses.SONIC;
            case CHORD -> SordinPoses.CHORD;
            case NOTES -> SordinPoses.NOTES;
            case SILENCE -> SordinPoses.SILENCE;
            case FORTISSIMO -> SordinPoses.FORTISSIMO;
            case STAGGER -> SordinPoses.STAGGER;
            default -> null;
        };
        boolean playing = attack == SordinEntity.Attack.GUARD;
        if (playing) {
            copy(SordinPoses.POSES[SordinPoses.P_PLAY], this.pose);
        } else if (timeline == null) {
            copy(SordinPoses.POSES[SordinPoses.P_READY], this.pose);
        } else {
            sample(timeline, a, this.pose);
        }
        if (state.deathTime > 0.0f) {
            float d = Mth.clamp(state.deathTime / 30.0f, 0.0f, 1.0f);
            mix(this.pose, SordinPoses.POSES[SordinPoses.P_DEATH], d * d * (3.0f - 2.0f * d), this.pose);
        }
        this.smooth(state, t);
        this.apply();

        // die Stimmgabel: an der Orgel liegt sie nicht in der Hand; beim Erwachen greift er sie, wenn er sie emporreckt
        this.fork.visible = !playing && !(attack == SordinEntity.Attack.AWAKEN && a < 42.0f);

        boolean free = attack == SordinEntity.Attack.NONE && state.deathTime <= 0.0f;
        // an der Orgel: die Finger laufen über die Tasten, der Kopf wiegt sich zur Musik, die niemand hört
        if (playing) {
            this.parts[SordinPoses.LEFT_HAND].xRot += Mth.sin(t * 0.55f) * 0.18f;
            this.parts[SordinPoses.RIGHT_HAND].xRot += Mth.sin(t * 0.55f + 2.1f) * 0.18f;
            this.parts[SordinPoses.LEFT_FOREARM].yRot += Mth.sin(t * 0.13f) * 0.12f;
            this.parts[SordinPoses.RIGHT_FOREARM].yRot += Mth.sin(t * 0.13f + 1.3f) * 0.12f;
            this.head.zRot += Mth.sin(t * 0.06f) * 0.08f;
            this.head.xRot += Mth.sin(t * 0.09f) * 0.05f;
            this.body.zRot += Mth.sin(t * 0.06f + 0.5f) * 0.02f;
        }
        // kleine, gleitende Schritte; die Kutte schwingt mit
        float walk = free ? Mth.clamp(state.walkAnimationSpeed, 0.0f, 1.0f) : 0.0f;
        if (walk > 0.0f) {
            float phase = state.walkAnimationPos * 0.5f;
            float swing = Mth.cos(phase) * 0.38f * walk;
            this.parts[SordinPoses.LEFT_LEG].xRot += swing;
            this.parts[SordinPoses.RIGHT_LEG].xRot -= swing;
            this.parts[SordinPoses.LEFT_SHIN].xRot += Math.max(0.0f, -Mth.sin(phase)) * 0.5f * walk;
            this.parts[SordinPoses.RIGHT_SHIN].xRot += Math.max(0.0f, Mth.sin(phase)) * 0.5f * walk;
            this.parts[SordinPoses.LEFT_ARM].xRot -= swing * 0.35f;
            this.body.y += Math.abs(Mth.sin(phase)) * -0.4f * walk;
        }
        // ruhiges Atmen
        if (!playing) {
            this.body.y += Mth.sin(t * 0.07f) * 0.25f;
        }
        // die Kutte hängt, auch wenn er sich neigt; im Gehen schwingt sie
        float lean = this.body.xRot;
        float flow = Mth.clamp(state.drift * 4.0f, 0.0f, 0.6f) + walk * 0.2f;
        this.skirt.xRot += -lean * 0.6f + flow * 0.3f;
        this.skirtLow.xRot += -lean * 0.2f + flow * 0.25f + Mth.sin(t * 0.08f) * 0.03f;
        this.skirtLow.zRot += Mth.sin(t * 0.07f + 1.0f) * 0.025f;
        // Blick zum Gegner
        if (free || attack == SordinEntity.Attack.SWING || attack == SordinEntity.Attack.SONIC) {
            float look = free ? 0.8f : 0.4f;
            this.head.yRot += Mth.clamp(state.yRot, -45.0f, 45.0f) * Anim.DEG * look;
            this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * Anim.DEG * look;
        }
        // die Pfeifen: beim Singen, im Fortissimo und beim Taumeln beben sie, sonst atmen sie kaum merklich
        float tremble = switch (attack) {
            case NOTES, FORTISSIMO -> 1.0f;
            case CHORD, STAGGER -> 0.6f;
            default -> state.fortissimo ? 0.3f : 0.08f;
        };
        for (int i = 0; i < PIPES; i++) {
            this.pipe[i].zRot += Mth.sin(t * 1.7f + i * 1.3f) * 0.035f * tremble;
            float s = 1.0f + Mth.sin(t * 0.9f + i * 0.8f) * 0.05f * tremble;
            this.pipe[i].yScale = s;
        }
        // die Fühler tasten nach jeder Erschütterung
        for (int i = 0; i < this.feelers.length; i++) {
            int seg = i % 3;
            if (seg > 0) {
                this.feelers[i].xRot += Mth.sin(t * 0.12f + i * 0.9f) * 0.09f * seg;
                this.feelers[i].zRot += Mth.sin(t * 0.09f + i * 1.7f) * 0.06f;
            }
        }
        // beim Taumeln zittert er
        if (attack == SordinEntity.Attack.STAGGER) {
            this.body.zRot += Mth.sin(t * 2.3f) * 0.05f;
            this.head.yRot += Mth.sin(t * 1.9f) * 0.12f;
        }
    }

    /** Weicher Wechsel: die Haltung folgt der berechneten rasch (bei jeder Bildrate gleich), so springt nichts. */
    private void smooth(SordinRenderState state, float t) {
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
        for (int b = 0; b < SordinPoses.BONES; b++) {
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
            copy(SordinPoses.POSES[keys[0][1]], out);
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
                mix(SordinPoses.POSES[from[1]], SordinPoses.POSES[to[1]], u, out);
                return;
            }
        }
        copy(SordinPoses.POSES[keys[keys.length - 1][1]], out);
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
