package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Glutwurm: Der Leib schlängelt sich in Wellen, die vom Kopf zum Schwanz laufen (in der Lava ruhig und weit, an Land
 * mühsam im Takt seiner Bewegung). Aufgerichtet steht der Hals wie bei einer Kobra senkrecht über der Lava, der Rest des
 * Leibs liegt flach dahinter, der Kopf wiegt sich. Beim Spucken zuckt der Kopf zurück und reißt das glühende Maul auf; beim
 * Biss holt er aus und schnellt vor.
 */
public class EmberWyrmModel extends EntityModel<EmberWyrmRenderState> {
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart leftHorn;
    private final ModelPart rightHorn;
    private final ModelPart[] segments = new ModelPart[6];
    private final ModelPart tail;

    public EmberWyrmModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.leftHorn = this.head.getChild("left_horn");
        this.rightHorn = this.head.getChild("right_horn");
        ModelPart parent = this.head;
        for (int i = 0; i < 6; i++) {
            this.segments[i] = parent.getChild("segment" + (i + 1));
            parent = this.segments[i];
        }
        this.tail = parent.getChild("tail");
    }

    @Override
    public void setupAnim(EmberWyrmRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        float rear = state.rear;
        float lie = 1.0f - rear;

        // ------------------------------------------------------------ Schlängeln
        float phase = state.inLava ? t * 0.16f : state.walkAnimationPos * 0.9f + t * 0.03f;
        float amp = state.inLava ? 1.0f : 0.4f + 0.6f * speed;
        for (int i = 0; i < 6; i++) {
            float a = (0.1f + 0.045f * i) * amp;
            // aufgerichtet schlängelt nur noch der hintere Leib
            float weight = i < 3 ? lie : 1.0f;
            this.segments[i].yRot += Mth.sin(phase - i * 0.9f) * a * weight;
            this.segments[i].xRot += Mth.sin(phase * 0.6f - i * 0.7f) * 0.03f * lie;
        }
        this.tail.yRot += Mth.sin(phase - 6 * 0.9f) * 0.4f * amp;
        this.head.yRot += -Mth.sin(phase) * 0.08f * amp * lie + Mth.clamp(state.yRot, -30.0f, 30.0f) * Anim.DEG * 0.4f * rear;
        this.jaw.xRot += 0.05f + 0.05f * Mth.sin(t * 0.11f);
        this.leftHorn.yRot += Mth.sin(t * 0.07f) * 0.02f;
        this.rightHorn.yRot -= Mth.sin(t * 0.07f) * 0.02f;

        // ------------------------------------------------------------ aufgerichtet wie eine Kobra
        if (rear > 0.001f) {
            float sway = Mth.sin(t * 0.09f) * 0.6f;
            this.head.y -= 26.0f * rear;
            this.head.z += 2.0f * rear;
            this.head.x += sway * rear;
            // der Hals senkrecht hinab, dann liegt der Leib flach auf der Lava
            this.segments[0].xRot += -Mth.HALF_PI * rear;
            this.segments[1].xRot += Mth.sin(t * 0.09f + 0.5f) * 0.06f * rear;
            this.segments[2].xRot += 0.05f * rear;
            this.segments[3].xRot += Mth.HALF_PI * rear - 0.05f * rear;
            this.segments[0].zRot += -sway * 0.04f * rear;
            // der Kopf blickt leicht hinab auf seine Beute
            this.head.xRot += 0.18f * rear;
            this.segments[0].xRot -= 0.18f * rear;
            this.jaw.xRot += 0.12f * rear;
        }

        // ------------------------------------------------------------ Spucken
        float spit = Anim.seconds(state.spit, t);
        if (spit >= 0.0f && spit < 0.8f) {
            float draw = Anim.bump(spit, 0.25f, 0.3f, 0.36f);
            float out = Anim.bump(spit - 0.28f, 0.05f, 0.15f, 0.45f);
            this.head.z += 3.0f * draw - 3.0f * out;
            this.head.y -= 2.0f * draw;
            this.jaw.xRot += 0.35f * draw + 0.9f * out;
        }

        // ------------------------------------------------------------ Biss
        float bite = Anim.seconds(state.bite, t);
        if (bite >= 0.0f && bite < 1.2f) {
            float coil = Anim.bump(bite, 0.4f, 0.48f, 0.55f);
            float strike = Anim.bump(bite - 0.5f, 0.08f, 0.2f, 0.65f);
            this.head.z += 5.0f * coil - 16.0f * strike;
            this.head.y += -3.0f * coil + 12.0f * strike * rear;
            this.jaw.xRot += 0.4f * coil + 1.0f * strike * (strike > 0.6f ? 1.0f : 0.6f);
            this.segments[0].xRot += 0.5f * strike * rear;
            this.head.xRot += 0.25f * strike;
        }
    }
}
