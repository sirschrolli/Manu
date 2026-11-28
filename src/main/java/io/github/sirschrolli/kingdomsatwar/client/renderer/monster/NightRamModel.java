package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.monster.NightRamEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Nachtwidder: Im Gehen schwingen die Beine im Kreuzgang, das Zottelvlies wogt, der Kopf nickt. Vor dem Ansturm senkt er
 * den Kopf tief, und der rechte Vorderhuf scharrt zweimal nach hinten; im Ansturm galoppiert er mit gesenkten Hörnern.
 * Benommen schwankt er, der Kopf pendelt. Zum Stampfen bäumt er sich auf (Vorderbeine hoch) und schlägt die Hufe nieder;
 * beim Hornstoß fährt der Kopf von unten nach oben.
 */
public class NightRamModel extends EntityModel<NightRamRenderState> {
    private final ModelPart body;
    private final ModelPart shag;
    private final ModelPart tail;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart beard;
    private final ModelPart leftFront;
    private final ModelPart rightFront;
    private final ModelPart leftHind;
    private final ModelPart rightHind;

    public NightRamModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.shag = this.body.getChild("shag");
        this.tail = this.body.getChild("tail");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.beard = this.jaw.getChild("beard");
        this.leftFront = root.getChild("left_front_leg");
        this.rightFront = root.getChild("right_front_leg");
        this.leftHind = root.getChild("left_hind_leg");
        this.rightHind = root.getChild("right_hind_leg");
    }

    @Override
    public void setupAnim(NightRamRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(1.0f, state.walkAnimationSpeed);
        byte ramState = state.ramState;

        // ------------------------------------------------------------ Gehen
        boolean charging = ramState == NightRamEntity.CHARGE;
        if (charging) {
            // Galopp: kräftig und schnell, die Vorderbeine gemeinsam, die Hinterbeine gemeinsam
            float g = t * 0.9f;
            float front = Mth.sin(g) * 0.95f;
            float hind = Mth.sin(g + Mth.PI) * 0.85f;
            this.leftFront.xRot += front;
            this.rightFront.xRot += Mth.sin(g + 0.4f) * 0.95f;
            this.leftHind.xRot += hind;
            this.rightHind.xRot += Mth.sin(g + Mth.PI + 0.4f) * 0.85f;
            this.body.xRot += Mth.sin(g) * 0.06f;
            this.body.y += Mth.abs(Mth.cos(g)) * -1.0f;
            this.shag.xRot += Mth.sin(g - 0.6f) * 0.04f;
        } else {
            this.leftFront.xRot += Anim.leg(pos, speed, 0.0f, 0.75f);
            this.rightFront.xRot += Anim.leg(pos, speed, Mth.PI, 0.75f);
            this.leftHind.xRot += Anim.leg(pos, speed, Mth.PI, 0.75f);
            this.rightHind.xRot += Anim.leg(pos, speed, 0.0f, 0.75f);
            this.body.y += Mth.abs(Mth.sin(pos * 0.6662f)) * -0.5f * speed;
            this.head.xRot += Mth.sin(pos * 0.6662f * 2.0f) * 0.06f * speed;
        }
        this.head.yRot += Mth.clamp(state.yRot, -40.0f, 40.0f) * Anim.DEG * (1.0f - state.lowered);
        this.head.xRot += Mth.clamp(state.xRot, -25.0f, 25.0f) * Anim.DEG * 0.5f * (1.0f - state.lowered);
        this.head.xRot += Mth.sin(t * 0.045f) * 0.03f;
        this.beard.xRot += Mth.sin(t * 0.08f) * 0.08f;
        this.tail.xRot += Mth.sin(t * 0.13f) * 0.12f;
        this.shag.zRot += Mth.sin(t * 0.05f) * 0.012f;

        // ------------------------------------------------------------ Kopf gesenkt (Scharren, Ansturm)
        float low = state.lowered;
        this.head.xRot += 0.6f * low;
        this.head.y += 1.0f * low;
        this.body.xRot += 0.08f * low;

        // ------------------------------------------------------------ Scharren
        float prepare = Anim.seconds(state.prepare, t);
        if (prepare >= 0.0f && prepare < 1.5f && ramState == NightRamEntity.PREPARE) {
            float scrape = Anim.bump(prepare - 0.3f, 0.12f, 0.2f, 0.4f) + Anim.bump(prepare - 0.9f, 0.12f, 0.2f, 0.4f);
            this.rightFront.xRot += 0.9f * scrape;
            this.rightFront.z += 0.6f * scrape;
            this.jaw.xRot += 0.15f * scrape;
        }

        // ------------------------------------------------------------ Benommen
        if (ramState == NightRamEntity.STUNNED) {
            float s = Anim.seconds(state.stun, t);
            float fade = s < 0.0f ? 1.0f : Math.min(1.0f, s * 4.0f);
            this.head.zRot += Mth.sin(t * 0.35f) * 0.3f * fade;
            this.head.xRot += 0.25f * fade;
            this.body.zRot += Mth.sin(t * 0.35f + 0.6f) * 0.06f * fade;
            this.leftFront.zRot -= 0.12f * fade;
            this.rightFront.zRot += 0.12f * fade;
            this.jaw.xRot += 0.3f * fade;
        }

        // ------------------------------------------------------------ Aufbäumen und Stampfen
        float stomp = Anim.seconds(state.stomp, t);
        if (stomp >= 0.0f && stomp < 1.2f) {
            float rear = Anim.bump(stomp, 0.45f, 0.62f, 0.72f);
            float slam = Anim.bump(stomp - 0.68f, 0.04f, 0.18f, 0.5f);
            this.body.xRot += -0.62f * rear + 0.08f * slam;
            this.body.y += -4.0f * rear;
            this.body.z += 2.0f * rear;
            this.leftFront.xRot += -1.15f * rear;
            this.rightFront.xRot += -1.35f * rear;
            this.leftFront.y += -4.5f * rear;
            this.rightFront.y += -4.5f * rear;
            this.leftFront.z += 2.6f * rear;
            this.rightFront.z += 2.6f * rear;
            this.leftHind.xRot += 0.35f * rear;
            this.rightHind.xRot += 0.35f * rear;
            this.head.xRot += -0.45f * rear + 0.5f * slam;
            this.jaw.xRot += 0.45f * rear;
        }

        // ------------------------------------------------------------ Hornstoß
        float butt = Anim.seconds(state.butt, t);
        if (butt >= 0.0f && butt < 0.5f) {
            float down = Anim.bump(butt, 0.1f, 0.14f, 0.22f);
            float up = Anim.bump(butt - 0.16f, 0.06f, 0.12f, 0.32f);
            this.head.xRot += 0.6f * down - 0.55f * up;
            this.body.xRot += 0.06f * down - 0.05f * up;
        }
    }
}
