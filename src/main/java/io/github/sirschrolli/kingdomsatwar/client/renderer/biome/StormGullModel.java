package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Sturmmöwe: am Boden mit angelegten Flügeln (die schwarzen Spitzen über dem Schwanz gekreuzt), watschelnd, mit
 * nickendem Kopf; beim Rufen reckt sie den Hals, öffnet den Schnabel und lüpft die Flügel. In der Luft weit ausgebreitete
 * Flügel in der typischen Möwen-Knickform: beim Steigen kräftige Schläge, beim Segeln still, in der Kurve nach innen
 * geneigt (das macht der Renderer); die Beine liegen angelegt. Auf dem Wasser schwimmt sie, die Flügel angelegt.
 */
public class StormGullModel extends EntityModel<StormGullRenderState> {
    final ModelPart body;
    final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart wingL;
    private final ModelPart wingLTip;
    private final ModelPart wingR;
    private final ModelPart wingRTip;
    private final ModelPart tail;
    private final ModelPart legL;
    private final ModelPart legR;

    public StormGullModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.wingL = this.body.getChild("wing_l");
        this.wingLTip = this.wingL.getChild("wing_l_tip");
        this.wingR = this.body.getChild("wing_r");
        this.wingRTip = this.wingR.getChild("wing_r_tip");
        this.tail = this.body.getChild("tail");
        this.legL = root.getChild("left_leg");
        this.legR = root.getChild("right_leg");
    }

    @Override
    public void setupAnim(StormGullRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float fly = state.fly;
        float ground = 1.0f - fly;
        float pos = state.walkAnimationPos;
        float speed = Math.min(1.0f, state.walkAnimationSpeed) * ground;

        // ------------------------------------------------------------ Flügel: angelegt (Boden) ↔ ausgebreitet (Luft)
        float fold = ground;
        this.wingL.yRot += -1.47f * fold;
        this.wingR.yRot += 1.47f * fold;
        this.wingL.zRot += 0.24f * fold;
        this.wingR.zRot -= 0.24f * fold;
        this.wingLTip.yRot += -0.1f * fold;
        this.wingRTip.yRot += 0.1f * fold;
        this.wingLTip.zRot += 0.04f * fold;
        this.wingRTip.zRot -= 0.04f * fold;

        if (fly > 0.0f) {
            float beat = t * 0.95f;
            float flap = state.flap;
            // Schlag: der Innenflügel führt, die Spitze folgt etwas später
            float inner = Mth.sin(beat) * 0.95f * flap;
            float outer = Mth.sin(beat - 0.7f) * 0.55f * flap;
            // Gleiten: Innenflügel leicht hoch, Spitzen leicht herab (die Knickform der Möwe), ein leises Zittern im Wind
            float gl = 1.0f - flap;
            float tremble = Mth.sin(t * 0.45f) * 0.04f;
            this.wingL.zRot += (inner - 0.16f * gl + tremble) * fly;
            this.wingR.zRot -= (inner - 0.16f * gl + tremble) * fly;
            this.wingLTip.zRot += (outer + 0.24f * gl) * fly;
            this.wingRTip.zRot -= (outer + 0.24f * gl) * fly;
            // beim Abschlag dreht der Flügel leicht nach vorn, beim Aufschlag zurück
            this.wingL.yRot += Mth.cos(beat) * 0.12f * flap * fly;
            this.wingR.yRot -= Mth.cos(beat) * 0.12f * flap * fly;
            this.body.y += Mth.sin(beat + 1.4f) * 0.35f * flap * fly;
            // Beine nach hinten angelegt, Schwanz gefächert
            this.legL.xRot += 1.35f * fly;
            this.legR.xRot += 1.35f * fly;
            this.legL.y -= 1.2f * fly;
            this.legR.y -= 1.2f * fly;
            this.tail.xRot += -0.08f * fly;
            this.tail.xScale = 1.0f + 0.25f * fly;
            this.body.xRot += state.pitch * fly;
            this.head.xRot -= state.pitch * 0.6f * fly;
        }

        // ------------------------------------------------------------ Gehen: watscheln, Kopf nickt
        float phase = pos * 1.1f;
        this.legL.xRot += Mth.cos(phase) * 0.9f * speed;
        this.legR.xRot += Mth.cos(phase + Mth.PI) * 0.9f * speed;
        this.body.zRot += Mth.sin(phase) * 0.12f * speed;
        this.head.z += Mth.sin(phase * 2.0f) * 0.4f * speed;
        this.body.xRot += -0.08f * ground;

        // ------------------------------------------------------------ Kopf
        float headYaw = Mth.clamp(state.yRot, -70.0f, 70.0f) * DEG;
        this.head.yRot += headYaw;
        this.head.xRot += Mth.clamp(state.xRot, -40.0f, 40.0f) * DEG * 0.6f;
        // ruckartiges Umschauen am Boden
        this.head.yRot += (Mth.floor(t / 23.0f) % 3 - 1) * 0.25f * ground;

        // ------------------------------------------------------------ Schwimmen
        if (state.floating) {
            this.body.y += 2.4f;
            this.legL.visible = false;
            this.legR.visible = false;
            this.body.zRot += Mth.sin(t * 0.12f) * 0.06f;
            this.body.xRot += Mth.sin(t * 0.09f) * 0.04f;
        } else {
            this.legL.visible = true;
            this.legR.visible = true;
        }

        // ------------------------------------------------------------ Rufen: Hals recken, Schnabel auf, Flügel lüpfen
        float c = Anim.seconds(state.call, t);
        if (c >= 0.0f && c < 2.0f) {
            float call = Anim.bump(c, 0.12f, 1.4f, 1.8f);
            this.head.xRot += -0.75f * call + Mth.sin(t * 2.6f) * 0.12f * call;
            this.jaw.xRot += (0.45f + Mth.sin(t * 2.6f) * 0.25f) * call;
            this.body.xRot += -0.2f * call * ground;
            this.wingL.zRot += -0.25f * call * ground;
            this.wingR.zRot += 0.25f * call * ground;
        }

        // ------------------------------------------------------------ Picken
        float p = Anim.seconds(state.peck, t);
        if (p >= 0.0f && p < 0.4f) {
            float peck = Anim.bump(p, 0.08f, 0.12f, 0.3f);
            this.head.xRot += 1.1f * peck;
            this.body.xRot += 0.35f * peck;
            this.jaw.xRot += 0.2f * peck;
        }

        // ------------------------------------------------------------ Zuschnappen (Fisch klauen)
        float s = Anim.seconds(state.snatch, t);
        if (s >= 0.0f && s < 0.6f) {
            float snap = Anim.bump(s, 0.06f, 0.12f, 0.4f);
            this.head.xRot += 0.7f * snap;
            this.jaw.xRot += 0.6f * snap;
            this.legL.xRot -= 1.2f * snap;
            this.legR.xRot -= 1.2f * snap;
        }
    }
}
