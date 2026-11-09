package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.nether.GlutschmiedEntity;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Malleus, der Glutschmied: Jeder Angriff ist eine Folge von Haltungen ({@link GlutschmiedPoses}), zwischen denen weich
 * überblendet wird – am Amboss der Hammerschlag im Takt, beim Erwachen das Aufrichten und das Greifen nach dem Speer,
 * beim Speerstoß Ausholen und Zustoßen, beim Hammerschlag das Ausholen über den Kopf. Dazu kommen der schwere Gang, der
 * Blick, das Atmen, die flackernden Schädelflammen und die pendelnde Schürze.
 */
public class GlutschmiedModel extends EntityModel<GlutschmiedRenderState> {
    /** Je Wesen die zuletzt gezeigte Haltung (für weiche Wechsel zwischen den Angriffen). */
    private static final Map<Object, float[]> LAST = new WeakHashMap<>();

    private final ModelPart[] parts = new ModelPart[GlutschmiedPoses.BONES];
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart apron;
    private final ModelPart flames;
    private final ModelPart flame1;
    private final ModelPart flame2;
    private final ModelPart spear;
    private final float[] pose = new float[GlutschmiedPoses.BONES * 6];

    public GlutschmiedModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.apron = this.body.getChild("apron");
        this.flames = this.head.getChild("flames");
        this.flame1 = this.flames.getChild("flame_1");
        this.flame2 = this.flames.getChild("flame_2");
        ModelPart leftArm = this.body.getChild("left_arm");
        ModelPart leftForearm = leftArm.getChild("left_forearm");
        ModelPart rightArm = this.body.getChild("right_arm");
        ModelPart rightForearm = rightArm.getChild("right_forearm");
        this.spear = rightForearm.getChild("spear");
        this.parts[GlutschmiedPoses.BODY] = this.body;
        this.parts[GlutschmiedPoses.HEAD] = this.head;
        this.parts[GlutschmiedPoses.JAW] = this.head.getChild("jaw");
        this.parts[GlutschmiedPoses.LEFT_ARM] = leftArm;
        this.parts[GlutschmiedPoses.LEFT_FOREARM] = leftForearm;
        this.parts[GlutschmiedPoses.HAMMER] = leftForearm.getChild("hammer");
        this.parts[GlutschmiedPoses.RIGHT_ARM] = rightArm;
        this.parts[GlutschmiedPoses.RIGHT_FOREARM] = rightForearm;
        this.parts[GlutschmiedPoses.SPEAR] = this.spear;
        this.parts[GlutschmiedPoses.LEFT_LEG] = root.getChild("left_leg");
        this.parts[GlutschmiedPoses.RIGHT_LEG] = root.getChild("right_leg");
    }

    @Override
    public void setupAnim(GlutschmiedRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        GlutschmiedEntity.Attack attack = GlutschmiedEntity.Attack.values()[Mth.clamp(state.attack, 0,
                GlutschmiedEntity.Attack.values().length - 1)];
        float a = state.attackTime;
        int[][] timeline = switch (attack) {
            case FORGE -> GlutschmiedPoses.FORGE;
            case AWAKEN -> GlutschmiedPoses.AWAKEN;
            case THRUST -> GlutschmiedPoses.THRUST;
            case SLAM -> GlutschmiedPoses.SLAM;
            case SPARKS -> GlutschmiedPoses.SPARKS;
            case GLOBS -> GlutschmiedPoses.GLOBS;
            case CALL -> GlutschmiedPoses.CALL;
            case STOKE -> GlutschmiedPoses.STOKE;
            default -> null;
        };
        if (timeline == null) {
            copy(GlutschmiedPoses.POSES[GlutschmiedPoses.P_READY], this.pose);
        } else {
            sample(timeline, attack == GlutschmiedEntity.Attack.FORGE ? a % GlutschmiedEntity.FORGE_BEAT : a, this.pose);
        }
        if (state.deathTime > 0.0f) {
            float d = Mth.clamp(state.deathTime / 18.0f, 0.0f, 1.0f);
            mix(this.pose, GlutschmiedPoses.POSES[GlutschmiedPoses.P_DEATH], d * d * (3.0f - 2.0f * d), this.pose);
        }
        this.smooth(state, t);
        this.apply();

        // der schwere Gang (nur ohne Angriff)
        float speed = attack == GlutschmiedEntity.Attack.NONE ? Math.min(1.0f, state.walkAnimationSpeed * 1.4f) : 0.0f;
        float pos = state.walkAnimationPos;
        if (speed > 0.01f) {
            float swing = Mth.cos(pos * 0.55f);
            this.parts[GlutschmiedPoses.LEFT_LEG].xRot += swing * 0.7f * speed;
            this.parts[GlutschmiedPoses.RIGHT_LEG].xRot -= swing * 0.7f * speed;
            this.parts[GlutschmiedPoses.LEFT_ARM].xRot -= swing * 0.22f * speed;
            this.parts[GlutschmiedPoses.RIGHT_ARM].xRot += swing * 0.12f * speed;
            this.body.y += Math.abs(Mth.sin(pos * 0.55f)) * 1.3f * speed;
            this.body.zRot += Mth.sin(pos * 0.275f) * 0.05f * speed;
            this.apron.xRot -= Math.abs(swing) * 0.25f * speed;
        }
        // Blick zum Gegner (am Amboss und beim Erwachen gelenkt die Haltung den Kopf)
        if (attack != GlutschmiedEntity.Attack.FORGE && state.deathTime <= 0.0f) {
            float look = attack == GlutschmiedEntity.Attack.NONE ? 0.8f : 0.45f;
            this.head.yRot += Mth.clamp(state.yRot, -45.0f, 45.0f) * Anim.DEG * look;
            this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * Anim.DEG * look;
        }
        // Atmen, die Schürze hängt senkrecht, die Schädelflammen flackern
        this.body.xRot += Mth.sin(t * 0.07f) * 0.015f;
        this.parts[GlutschmiedPoses.JAW].xRot += Math.max(0.0f, Mth.sin(t * 0.045f)) * 0.06f;
        this.apron.xRot -= (this.body.xRot - 0.1047f) * 0.85f;
        float flicker = state.stoked ? 1.35f : 1.0f;
        this.flames.yScale = flicker * (1.0f + Mth.sin(t * 0.55f) * 0.12f + Mth.sin(t * 1.3f) * 0.05f);
        this.flames.xScale = 1.0f + Mth.sin(t * 0.4f + 1.0f) * 0.06f;
        this.flame1.yRot += Mth.sin(t * 0.33f) * 0.12f;
        this.flame2.yRot += Mth.sin(t * 0.29f + 2.0f) * 0.12f;
        this.spear.visible = state.spear;
    }

    /** Weicher Wechsel: die Haltung folgt der berechneten rasch (bei jeder Bildrate gleich), so springt nichts. */
    private void smooth(GlutschmiedRenderState state, float t) {
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
            float k = 1.0f - (float) Math.exp(-22.0f * dt);
            for (int i = 0; i < this.pose.length; i++) {
                last[i] += (this.pose[i] - last[i]) * k;
            }
            System.arraycopy(last, 0, this.pose, 0, this.pose.length);
        }
        last[this.pose.length] = time;
    }

    private void apply() {
        for (int b = 0; b < GlutschmiedPoses.BONES; b++) {
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
            copy(GlutschmiedPoses.POSES[keys[0][1]], out);
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
                mix(GlutschmiedPoses.POSES[from[1]], GlutschmiedPoses.POSES[to[1]], u, out);
                return;
            }
        }
        copy(GlutschmiedPoses.POSES[keys[keys.length - 1][1]], out);
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
