package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.fae.StoneKeeperEntity;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Der Steinhüter: Jeder Angriff ist eine Folge von Haltungen ({@link StoneKeeperPoses}), zwischen denen weich überblendet
 * wird – im Schlaf kniend, beim Erwachen das Aufstemmen und Brüllen, beim Faustschlag das Ausholen über den Kopf, beim
 * Felswurf das Graben, Stemmen und Schleudern des Brockens (nur dann ist er zu sehen). Dazu kommen der schwere Gang mit
 * pendelnden Armen, der Blick, das Atmen, das pulsierende Hüterherz, die wiegenden Leuchtranken – und der Kristallpanzer,
 * der unter halber Kraft über ihn wächst (und zerspringt).
 */
public class StoneKeeperModel extends EntityModel<StoneKeeperRenderState> {
    /** Je Wesen die zuletzt gezeigte Haltung (für weiche Wechsel zwischen den Angriffen). */
    private static final Map<Object, float[]> LAST = new WeakHashMap<>();

    private final ModelPart[] parts = new ModelPart[StoneKeeperPoses.BONES];
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart core;
    private final ModelPart boulder;
    private final ModelPart[] armor;
    private final ModelPart[] vines;
    private final float[] pose = new float[StoneKeeperPoses.BONES * 6];

    public StoneKeeperModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.core = this.body.getChild("core");
        this.boulder = this.body.getChild("boulder");
        ModelPart leftArm = this.body.getChild("left_arm");
        ModelPart rightArm = this.body.getChild("right_arm");
        ModelPart leftForearm = leftArm.getChild("left_forearm");
        ModelPart rightForearm = rightArm.getChild("right_forearm");
        ModelPart leftLeg = root.getChild("left_leg");
        ModelPart rightLeg = root.getChild("right_leg");
        this.parts[StoneKeeperPoses.BODY] = this.body;
        this.parts[StoneKeeperPoses.HEAD] = this.head;
        this.parts[StoneKeeperPoses.JAW] = this.head.getChild("jaw");
        this.parts[StoneKeeperPoses.LEFT_ARM] = leftArm;
        this.parts[StoneKeeperPoses.LEFT_FOREARM] = leftForearm;
        this.parts[StoneKeeperPoses.RIGHT_ARM] = rightArm;
        this.parts[StoneKeeperPoses.RIGHT_FOREARM] = rightForearm;
        this.parts[StoneKeeperPoses.LEFT_LEG] = leftLeg;
        this.parts[StoneKeeperPoses.LEFT_SHIN] = leftLeg.getChild("left_shin");
        this.parts[StoneKeeperPoses.RIGHT_LEG] = rightLeg;
        this.parts[StoneKeeperPoses.RIGHT_SHIN] = rightLeg.getChild("right_shin");
        this.parts[StoneKeeperPoses.BOULDER] = this.boulder;
        this.armor = new ModelPart[] {this.body.getChild("armor_chest"), this.head.getChild("armor_head"), leftForearm.getChild("armor_left_arm"),
                rightForearm.getChild("armor_right_arm"), leftLeg.getChild("armor_left_leg"), rightLeg.getChild("armor_right_leg")};
        this.vines = new ModelPart[] {this.body.getChild("vine_back_0"), this.body.getChild("vine_back_1"), this.body.getChild("vine_back_2"),
                leftArm.getChild("vine_left"), rightArm.getChild("vine_right")};
    }

    @Override
    public void setupAnim(StoneKeeperRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        StoneKeeperEntity.Attack attack = StoneKeeperEntity.Attack.values()[Mth.clamp(state.attack, 0, StoneKeeperEntity.Attack.values().length - 1)];
        float a = state.attackTime;
        int[][] timeline = switch (attack) {
            case DORMANT -> StoneKeeperPoses.DORMANT;
            case AWAKEN -> StoneKeeperPoses.AWAKEN;
            case SMASH -> StoneKeeperPoses.SMASH;
            case STOMP -> StoneKeeperPoses.STOMP;
            case BOULDER -> StoneKeeperPoses.HURL;
            case CRYSTALS -> StoneKeeperPoses.CRYSTALS;
            case BEAM -> StoneKeeperPoses.BEAM;
            case LEAP -> StoneKeeperPoses.LEAP;
            case LAND -> StoneKeeperPoses.LAND;
            case ARMOR -> StoneKeeperPoses.ARMOR;
            case STAGGER -> StoneKeeperPoses.STAGGER;
            default -> null;
        };
        if (timeline == null) {
            copy(StoneKeeperPoses.POSES[StoneKeeperPoses.P_READY], this.pose);
        } else {
            sample(timeline, a, this.pose);
        }
        if (state.deathTime > 0.0f) {
            float d = Mth.clamp(state.deathTime / 22.0f, 0.0f, 1.0f);
            mix(this.pose, StoneKeeperPoses.POSES[StoneKeeperPoses.P_DEATH], d * d * (3.0f - 2.0f * d), this.pose);
        }
        this.smooth(state, t);
        this.apply();

        boolean asleep = attack == StoneKeeperEntity.Attack.DORMANT;
        // der schwere Gang (nur ohne Angriff): kurze, wuchtige Schritte, die Arme pendeln mit
        float speed = attack == StoneKeeperEntity.Attack.NONE ? Math.min(1.0f, state.walkAnimationSpeed * 1.5f) : 0.0f;
        float pos = state.walkAnimationPos;
        if (speed > 0.01f) {
            float swing = Mth.cos(pos * 0.5f);
            this.parts[StoneKeeperPoses.LEFT_LEG].xRot += swing * 0.55f * speed;
            this.parts[StoneKeeperPoses.RIGHT_LEG].xRot -= swing * 0.55f * speed;
            this.parts[StoneKeeperPoses.LEFT_SHIN].xRot += Math.max(0.0f, -swing) * 0.4f * speed;
            this.parts[StoneKeeperPoses.RIGHT_SHIN].xRot += Math.max(0.0f, swing) * 0.4f * speed;
            this.parts[StoneKeeperPoses.LEFT_ARM].xRot -= swing * 0.3f * speed;
            this.parts[StoneKeeperPoses.RIGHT_ARM].xRot += swing * 0.3f * speed;
            this.body.y += Math.abs(Mth.sin(pos * 0.5f)) * 1.4f * speed;
            this.body.zRot += Mth.sin(pos * 0.25f) * 0.06f * speed;
            this.body.yRot += Mth.sin(pos * 0.25f) * 0.05f * speed;
        }
        // Blick zum Gegner
        if (!asleep && state.deathTime <= 0.0f && attack != StoneKeeperEntity.Attack.STAGGER) {
            float look = attack == StoneKeeperEntity.Attack.NONE ? 0.8f : 0.4f;
            this.head.yRot += Mth.clamp(state.yRot, -40.0f, 40.0f) * Anim.DEG * look;
            this.head.xRot += Mth.clamp(state.xRot, -25.0f, 25.0f) * Anim.DEG * look;
        }
        // Atmen (im Schlaf langsam und tief), benommen schwankt er
        float breath = asleep ? Mth.sin(t * 0.035f) : Mth.sin(t * 0.07f);
        this.body.xRot += breath * (asleep ? 0.02f : 0.015f);
        this.body.y += breath * (asleep ? 0.35f : 0.15f);
        if (attack == StoneKeeperEntity.Attack.STAGGER) {
            this.body.zRot += Mth.sin(t * 0.18f) * 0.08f;
            this.head.zRot += Mth.sin(t * 0.18f + 1.0f) * 0.12f;
        }
        // das Hüterherz pulsiert (beim Mondstrahl schwillt es an)
        float pulse = 1.0f + Mth.sin(t * (asleep ? 0.06f : 0.16f)) * 0.06f;
        if (attack == StoneKeeperEntity.Attack.BEAM) {
            pulse += Mth.clamp(a / 30.0f, 0.0f, 1.0f) * 0.3f + (a > 34.0f ? Mth.sin(t * 0.9f) * 0.08f : 0.0f);
        }
        this.core.xScale = pulse;
        this.core.yScale = pulse;
        this.core.zScale = pulse;
        // die Leuchtranken wiegen sich (und schwingen den Bewegungen nach)
        for (int i = 0; i < this.vines.length; i++) {
            ModelPart vine = this.vines[i];
            vine.xRot = -this.body.xRot * 0.6f + Mth.sin(t * 0.08f + i * 1.3f) * 0.08f - speed * 0.15f;
            vine.zRot = Mth.sin(t * 0.06f + i * 0.7f) * 0.08f;
        }
        // der Felsbrocken: nur beim Felswurf, vom Graben bis zum Wurf
        boolean holding = attack == StoneKeeperEntity.Attack.BOULDER && a >= 15.0f && a < 36.0f;
        this.boulder.visible = holding;
        if (holding) {
            float grow = Mth.clamp((a - 15.0f) / 3.0f, 0.0f, 1.0f);
            this.boulder.xScale = grow;
            this.boulder.yScale = grow;
            this.boulder.zScale = grow;
            this.boulder.yRot += Mth.sin(t * 0.3f) * 0.05f;
        }
        // der Kristallpanzer wächst beim Brüllen (Tick 14–26) und bleibt, bis er zerspringt
        float grown = 0.0f;
        if (state.armored) {
            grown = 1.0f;
        }
        if (attack == StoneKeeperEntity.Attack.ARMOR) {
            grown = Math.max(grown, Mth.clamp((a - 14.0f) / 12.0f, 0.0f, 1.0f));
        }
        for (ModelPart part : this.armor) {
            part.visible = grown > 0.01f;
            float k = grown * grown * (3.0f - 2.0f * grown);
            part.xScale = k;
            part.yScale = k;
            part.zScale = k;
        }
    }

    /** Weicher Wechsel: die Haltung folgt der berechneten rasch (bei jeder Bildrate gleich), so springt nichts. */
    private void smooth(StoneKeeperRenderState state, float t) {
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
        for (int b = 0; b < StoneKeeperPoses.BONES; b++) {
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
            copy(StoneKeeperPoses.POSES[keys[0][1]], out);
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
                mix(StoneKeeperPoses.POSES[from[1]], StoneKeeperPoses.POSES[to[1]], u, out);
                return;
            }
        }
        copy(StoneKeeperPoses.POSES[keys[keys.length - 1][1]], out);
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
