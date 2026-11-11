package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Flamingo: steht mit dem Hals im S und angelegten Flügeln (die schwarzen Schwungfedern über dem Schwanz), schreitet mit
 * nach hinten knickenden Fersen und nickendem Kopf; beim Ruhen auf einem Bein (das andere angezogen), nachts mit dem Kopf
 * im Rückengefieder; beim Seihen den Hals tief gebogen und den Kopf verkehrt herum im Wasser, schwenkend. Beim Rufen
 * reckt er den Hals. In der Luft: Hals gerade vor, Beine gerade nach hinten, rote und schwarze Flügel in kräftigen Schlägen.
 */
public class FlamingoModel extends EntityModel<FlamingoRenderState> {
    private final ModelPart body;
    private final ModelPart neck1;
    private final ModelPart neck2;
    private final ModelPart neck3;
    private final ModelPart head;
    private final ModelPart wingL;
    private final ModelPart wingLTip;
    private final ModelPart wingR;
    private final ModelPart wingRTip;
    private final ModelPart tail;
    private final ModelPart legL;
    private final ModelPart shinL;
    private final ModelPart footL;
    private final ModelPart legR;
    private final ModelPart shinR;
    private final ModelPart footR;

    public FlamingoModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.neck1 = this.body.getChild("neck1");
        this.neck2 = this.neck1.getChild("neck2");
        this.neck3 = this.neck2.getChild("neck3");
        this.head = this.neck3.getChild("head");
        this.wingL = this.body.getChild("wing_l");
        this.wingLTip = this.wingL.getChild("wing_l_tip");
        this.wingR = this.body.getChild("wing_r");
        this.wingRTip = this.wingR.getChild("wing_r_tip");
        this.tail = this.body.getChild("tail");
        this.legL = root.getChild("left_leg");
        this.shinL = this.legL.getChild("left_shin");
        this.footL = this.shinL.getChild("left_foot");
        this.legR = root.getChild("right_leg");
        this.shinR = this.legR.getChild("right_shin");
        this.footR = this.shinR.getChild("right_foot");
    }

    @Override
    public void setupAnim(FlamingoRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float fly = state.fly;
        float ground = 1.0f - fly;
        float feed = state.feed * ground;
        float rest = state.rest * ground * (1.0f - feed);
        float tuck = state.tuck * rest;
        float pos = state.walkAnimationPos;
        float speed = Math.min(1.0f, state.walkAnimationSpeed) * ground * (1.0f - rest);

        // ------------------------------------------------------------ Flügel anlegen (auf dem Rücken, Spitzen über dem Schwanz)
        this.wingL.yRot += -1.47f * ground;
        this.wingR.yRot += 1.47f * ground;
        this.wingL.zRot += 0.24f * ground;
        this.wingR.zRot -= 0.24f * ground;
        this.wingLTip.yRot += -0.1f * ground;
        this.wingRTip.yRot += 0.1f * ground;

        // ------------------------------------------------------------ Flug
        if (fly > 0.0f) {
            float beat = t * 0.55f;
            float flap = state.flap;
            float inner = (Mth.sin(beat) * 0.85f * flap - 0.08f * (1.0f - flap)) * fly;
            float outer = (Mth.sin(beat - 0.7f) * 0.5f * flap + 0.12f * (1.0f - flap)) * fly;
            this.wingL.zRot += inner;
            this.wingR.zRot -= inner;
            this.wingLTip.zRot += outer;
            this.wingRTip.zRot -= outer;
            this.body.y += Mth.sin(beat + 1.4f) * 0.4f * flap * fly;
            // Hals gerade nach vorn (der S-Bogen streckt sich), Kopf waagerecht
            this.neck1.xRot += 1.6f * fly;
            this.neck2.xRot += 0.38f * fly;
            this.neck3.xRot += -0.84f * fly;
            this.head.xRot += 0.24f * fly;
            // Beine gerade nach hinten
            this.legL.xRot += 1.45f * fly;
            this.legR.xRot += 1.45f * fly;
            this.legL.y -= 2.0f * fly;
            this.legR.y -= 2.0f * fly;
            this.legL.z += 2.4f * fly;
            this.legR.z += 2.4f * fly;
            this.footL.xRot += 0.4f * fly;
            this.footR.xRot += 0.4f * fly;
        }

        // ------------------------------------------------------------ Schreiten: Fersen knicken nach hinten, Kopf nickt
        float phase = pos * 0.75f;
        float swingL = Mth.cos(phase);
        float swingR = Mth.cos(phase + Mth.PI);
        this.legL.xRot += swingL * 0.45f * speed;
        this.legR.xRot += swingR * 0.45f * speed;
        this.shinL.xRot += Math.max(0.0f, Mth.sin(phase)) * 0.9f * speed;
        this.shinR.xRot += Math.max(0.0f, -Mth.sin(phase)) * 0.9f * speed;
        this.footL.xRot -= Math.max(0.0f, Mth.sin(phase)) * 0.6f * speed;
        this.footR.xRot -= Math.max(0.0f, -Mth.sin(phase)) * 0.6f * speed;
        this.neck1.xRot += Mth.sin(phase * 2.0f) * 0.07f * speed;
        this.head.z += Mth.sin(phase * 2.0f) * 0.4f * speed;

        // ------------------------------------------------------------ Kopf und Hals folgen dem Blick
        float yaw = Mth.clamp(state.yRot, -80.0f, 80.0f) * DEG;
        this.neck2.yRot += yaw * 0.4f * (1.0f - feed);
        this.head.yRot += yaw * 0.5f * (1.0f - feed);
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * DEG * 0.5f;

        // ------------------------------------------------------------ Ruhen auf einem Bein, nachts den Kopf ins Gefieder
        if (rest > 0.0f) {
            this.legR.xRot += -0.55f * rest;
            this.shinR.xRot += 2.6f * rest;
            this.footR.xRot += -1.2f * rest;
            this.body.xRot += 0.06f * rest;
            this.neck1.xRot += -0.2f * tuck;
            this.neck2.xRot += -1.4f * tuck;
            this.neck3.xRot += -1.0f * tuck;
            this.head.xRot += 0.9f * tuck;
            this.head.yRot += 0.5f * tuck;
        }

        // ------------------------------------------------------------ Seihen: Hals tief, Kopf verkehrt herum, schwenkend
        if (feed > 0.0f) {
            float sweep = Mth.sin(t * 0.18f) * 0.35f;
            this.body.xRot += 0.21f * feed;
            this.neck1.xRot += 2.36f * feed;
            this.neck2.xRot += 0.87f * feed;
            this.neck3.xRot += -0.17f * feed;
            this.head.xRot += 2.62f * feed + Mth.sin(t * 0.9f) * 0.08f * feed;
            this.neck2.yRot += sweep * feed;
            this.head.yRot += sweep * 0.5f * feed;
        }

        // ------------------------------------------------------------ Im Wasser: der Leib wiegt sich ein wenig
        if (state.wading) {
            this.body.zRot += Mth.sin(t * 0.07f) * 0.03f * ground;
        }

        // ------------------------------------------------------------ Rufen: Hals recken, Schnabel hoch
        float c = Anim.seconds(state.call, t);
        if (c >= 0.0f && c < 1.6f) {
            float call = Anim.bump(c, 0.15f, 1.0f, 1.5f) * ground * (1.0f - feed);
            this.neck1.xRot += -0.25f * call;
            this.neck3.xRot += -0.4f * call;
            this.head.xRot += -0.5f * call + Mth.sin(t * 2.4f) * 0.08f * call;
            this.tail.xRot += 0.2f * call;
        }
    }
}
