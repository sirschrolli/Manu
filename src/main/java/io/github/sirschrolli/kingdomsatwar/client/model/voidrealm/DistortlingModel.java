package io.github.sirschrolli.kingdomsatwar.client.model.voidrealm;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm.VoidCreatureRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Zerrling: gebeugte, lauernde Haltung; ein ruckhafter, stockender Gang; immer wieder Bildfehler – Scheiben des Leibs
 * verrutschen, der Kopf zuckt, die losen Splitter springen. Nach einem Sprung ist er kurz in die Länge gezerrt. Beim
 * Angriff reißen beide Krallenarme über Kreuz herab.
 */
public class DistortlingModel extends EntityModel<VoidCreatureRenderState> {
    private final ModelPart body;
    private final ModelPart chest;
    private final ModelPart chestSlice;
    private final ModelPart chestSlice2;
    private final ModelPart head;
    private final ModelPart headSlice;
    private final ModelPart jaw;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightShin;
    private final ModelPart leftShin;
    private final ModelPart[] bits = new ModelPart[6];

    public DistortlingModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        ModelPart belly = this.body.getChild("belly");
        this.chest = belly.getChild("chest");
        this.chestSlice = this.chest.getChild("chest_slice");
        this.chestSlice2 = this.chest.getChild("chest_slice2");
        this.head = this.chest.getChild("head");
        this.headSlice = this.head.getChild("head_slice");
        this.jaw = this.head.getChild("jaw");
        this.rightArm = this.chest.getChild("right_arm");
        this.leftArm = this.chest.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightShin = this.rightLeg.getChild("right_shin");
        this.leftShin = this.leftLeg.getChild("left_shin");
        ModelPart glitch = this.body.getChild("glitch");
        for (int k = 0; k < 6; k++) {
            this.bits[k] = glitch.getChild("bit_" + k);
        }
    }

    /** Fester Zufall 0..1 je Wesen und Zeitabschnitt. */
    private static float noise(int seed, int bucket, int salt) {
        int h = seed * 73856093 ^ bucket * 19349663 ^ salt * 83492791;
        h = (h ^ (h >>> 13)) * 0x5bd1e995;
        h ^= h >>> 15;
        return (h & 0xFFFF) / 65535.0f;
    }

    @Override
    public void setupAnim(VoidCreatureRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        // stockender Gang: die Phase springt in kleinen Stufen statt gleichmäßig zu laufen
        float pos = Mth.floor(state.walkAnimationPos * 2.5f) / 2.5f;
        float stride = pos * 0.62f;

        // gebeugte, lauernde Haltung
        this.chest.xRot += 0.3f + speed * 0.15f;
        this.head.xRot -= 0.32f + speed * 0.1f;
        this.rightArm.xRot -= 0.22f;
        this.leftArm.xRot -= 0.22f;
        this.rightForearm.xRot -= 0.35f;
        this.leftForearm.xRot -= 0.35f;
        this.rightArm.zRot += 0.1f;
        this.leftArm.zRot -= 0.1f;

        this.rightLeg.xRot += Mth.cos(stride) * 0.8f * speed;
        this.leftLeg.xRot += Mth.cos(stride + Mth.PI) * 0.8f * speed;
        this.rightShin.xRot += Math.max(0.0f, Mth.sin(stride)) * 0.7f * speed;
        this.leftShin.xRot += Math.max(0.0f, Mth.sin(stride + Mth.PI)) * 0.7f * speed;
        this.rightArm.xRot += Mth.cos(stride + Mth.PI) * 0.5f * speed;
        this.leftArm.xRot += Mth.cos(stride) * 0.5f * speed;
        this.body.y += Math.abs(Mth.sin(stride)) * 0.8f * speed;

        this.head.yRot += Mth.clamp(state.yRot, -60.0f, 60.0f) * DEG;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * DEG * 0.6f;
        // langsames, unruhiges Wiegen
        this.chest.zRot += Mth.sin(t * 0.07f) * 0.05f;
        this.head.zRot += Mth.sin(t * 0.11f + 1.0f) * 0.07f;

        // Bildfehler: in kurzen Abständen verrutschen Scheiben, der Kopf zuckt, der Kiefer klappt
        int seed = state.seed;
        int bucket = (int) (t / 2.0f);
        float roll = noise(seed, bucket, 1);
        float after = Math.max(0.0f, 1.0f - state.sinceBlink / 12.0f);
        float strength = roll > 0.82f ? 1.0f : roll > 0.65f ? 0.35f : 0.0f;
        strength = Math.max(strength, after);
        this.chestSlice.x += (noise(seed, bucket, 2) - 0.5f) * 4.0f * strength;
        this.chestSlice2.x += (noise(seed, bucket, 3) - 0.5f) * 3.0f * strength;
        this.headSlice.x += (noise(seed, bucket, 4) - 0.5f) * 3.5f * strength;
        if (roll > 0.9f) {
            this.head.zRot += (noise(seed, bucket, 5) - 0.5f) * 0.9f;
            this.head.yRot += (noise(seed, bucket, 6) - 0.5f) * 0.6f;
        }
        this.jaw.xRot += noise(seed, bucket / 3, 7) > 0.7f ? 0.45f : 0.05f;
        this.jaw.y += noise(seed, bucket / 3, 8) > 0.7f ? 0.6f : 0.0f;
        for (int k = 0; k < 6; k++) {
            int b = (int) ((t + k * 3.0f) / 3.0f);
            this.bits[k].x += (noise(seed, b, 10 + k) - 0.5f) * 2.4f;
            this.bits[k].y += (noise(seed, b, 20 + k) - 0.5f) * 2.4f + Mth.sin(t * 0.1f + k) * 0.6f;
            this.bits[k].z += (noise(seed, b, 30 + k) - 0.5f) * 2.4f;
            float vis = noise(seed, b, 40 + k) > 0.15f || after > 0.0f ? 1.0f : 0.0f;
            this.bits[k].visible = vis > 0.5f;
        }
        // nach einem Sprung: kurz in die Länge gezerrt
        if (after > 0.0f) {
            this.body.yScale = 1.0f + after * 0.35f;
            this.body.xScale = this.body.zScale = 1.0f - after * 0.2f;
            this.chest.zRot += (noise(seed, bucket, 9) - 0.5f) * after;
        }

        // Angriff: beide Krallenarme hoch und über Kreuz herab
        float s = Anim.seconds(state.attack, t);
        if (s >= 0.0f && s < 0.55f) {
            float raise = Anim.bump(s, 0.12f, 0.16f, 0.26f);
            float strike = Anim.bump(s - 0.14f, 0.06f, 0.12f, 0.4f);
            this.rightArm.xRot -= 2.3f * raise - 0.7f * strike;
            this.leftArm.xRot -= 2.0f * raise - 0.5f * strike;
            this.rightArm.zRot -= 0.35f * raise - 0.5f * strike;
            this.leftArm.zRot += 0.35f * raise - 0.5f * strike;
            this.chest.xRot += -0.25f * raise + 0.4f * strike;
            this.jaw.xRot += 0.5f * raise;
        }
    }
}
