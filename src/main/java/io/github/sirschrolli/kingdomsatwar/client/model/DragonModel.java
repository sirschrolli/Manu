package io.github.sirschrolli.kingdomsatwar.client.model;

import io.github.sirschrolli.kingdomsatwar.client.renderer.DragonRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Drachen-Modell mit allen Animationen.
 *
 * <p>Die Geometrie kommt aus {@code DragonGeometry} (erzeugt von tools/generate_entities.py). Die Animationen
 * werden hier berechnet: Atmen, Schwanz, Kopfdrehung, Laufen, Sitzen, Fliegen/Gleiten, Odem, Brüllen, Beißen.
 * Alle Winkel sind Zusätze zur Grundhaltung aus der Geometrie.
 */
public class DragonModel extends EntityModel<DragonRenderState> {
    private static final float DEG = Mth.DEG_TO_RAD;

    private final ModelPart body;
    private final ModelPart neck1;
    private final ModelPart neck2;
    private final ModelPart neck3;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart[] tail = new ModelPart[6];
    private final ModelPart frontLegL;
    private final ModelPart frontLegR;
    private final ModelPart frontShinL;
    private final ModelPart frontShinR;
    private final ModelPart frontFootL;
    private final ModelPart frontFootR;
    private final ModelPart hindLegL;
    private final ModelPart hindLegR;
    private final ModelPart hindShinL;
    private final ModelPart hindShinR;
    private final ModelPart hindFootL;
    private final ModelPart hindFootR;
    private final ModelPart wingL;
    private final ModelPart wingR;
    private final ModelPart wingTipL;
    private final ModelPart wingTipR;

    public DragonModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.neck1 = this.body.getChild("neck1");
        this.neck2 = this.neck1.getChild("neck2");
        this.neck3 = this.neck2.getChild("neck3");
        this.head = this.neck3.getChild("head");
        this.jaw = this.head.getChild("jaw");
        ModelPart parent = this.body;
        for (int i = 0; i < this.tail.length; i++) {
            this.tail[i] = parent.getChild("tail" + (i + 1));
            parent = this.tail[i];
        }
        this.frontLegL = this.body.getChild("front_leg_l");
        this.frontLegR = this.body.getChild("front_leg_r");
        this.frontShinL = this.frontLegL.getChild("front_shin_l");
        this.frontShinR = this.frontLegR.getChild("front_shin_r");
        this.frontFootL = this.frontShinL.getChild("front_foot_l");
        this.frontFootR = this.frontShinR.getChild("front_foot_r");
        this.hindLegL = this.body.getChild("hind_leg_l");
        this.hindLegR = this.body.getChild("hind_leg_r");
        this.hindShinL = this.hindLegL.getChild("hind_shin_l");
        this.hindShinR = this.hindLegR.getChild("hind_shin_r");
        this.hindFootL = this.hindShinL.getChild("hind_foot_l");
        this.hindFootR = this.hindShinR.getChild("hind_foot_r");
        this.wingL = this.body.getChild("wing_l");
        this.wingR = this.body.getChild("wing_r");
        this.wingTipL = this.wingL.getChild("wing_tip_l");
        this.wingTipR = this.wingR.getChild("wing_tip_r");
    }

    @Override
    public void setupAnim(DragonRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;

        // Jungtiere: größerer Kopf, kürzerer Hals, kleinere Flügel
        float young = Mth.clamp((1.0f - state.stage.scale()) / 0.72f, 0.0f, 1.0f);
        if (young > 0.0f) {
            float headScale = 1.0f + 0.55f * young;
            this.head.xScale = this.head.yScale = this.head.zScale = headScale;
            this.neck1.zScale = this.neck2.zScale = this.neck3.zScale = 1.0f - 0.25f * young;
            float wingScale = 1.0f - 0.35f * young;
            this.wingL.xScale = this.wingL.yScale = this.wingL.zScale = wingScale;
            this.wingR.xScale = this.wingR.yScale = this.wingR.zScale = wingScale;
        }

        // Kopf folgt dem Blick, verteilt auf Hals und Kopf
        float yaw = Mth.clamp(state.yRot, -70.0f, 70.0f) * DEG;
        float pitch = Mth.clamp(state.xRot, -45.0f, 45.0f) * DEG;
        this.neck1.yRot += yaw * 0.2f;
        this.neck2.yRot += yaw * 0.25f;
        this.neck3.yRot += yaw * 0.25f;
        this.head.yRot += yaw * 0.3f;
        this.neck2.xRot += pitch * 0.3f;
        this.head.xRot += pitch * 0.5f;

        // Atmen
        float breathe = Mth.sin(t * 0.08f);
        this.body.y += breathe * 0.3f;
        this.neck1.xRot += breathe * 0.025f;
        this.head.xRot -= breathe * 0.02f;

        // Schwanz schwingt sanft (im Flug weniger)
        float sway = state.flying ? 0.45f : 1.0f;
        for (int i = 0; i < this.tail.length; i++) {
            this.tail[i].yRot += Mth.sin(t * 0.09f - i * 0.65f) * (0.05f + i * 0.018f) * sway;
            this.tail[i].xRot += Mth.sin(t * 0.07f - i * 0.5f) * 0.025f;
        }

        if (state.flying) {
            this.flyPose(state, t);
        } else if (state.sitting) {
            this.sitPose(t);
        } else {
            this.walkPose(state);
        }

        if (state.breathing) {
            this.jaw.xRot += 0.75f;
            this.neck1.xRot += 0.35f;
            this.neck2.xRot += 0.1f;
            this.head.xRot -= 0.3f + Mth.sin(t * 0.8f) * 0.04f;
        }
        this.applyRoar(state);
        this.applyBite(state);
    }

    // ------------------------------------------------------------------ Haltungen

    private void walkPose(DragonRenderState state) {
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        float swing = Mth.cos(pos * 0.6662f) * 0.8f * speed;
        float swingOpp = Mth.cos(pos * 0.6662f + Mth.PI) * 0.8f * speed;
        this.frontLegL.xRot += swing;
        this.frontLegR.xRot += swingOpp;
        this.hindLegL.xRot += swingOpp;
        this.hindLegR.xRot += swing;
        this.frontShinL.xRot += Math.max(0.0f, -swing) * 0.7f;
        this.frontShinR.xRot += Math.max(0.0f, -swingOpp) * 0.7f;
        this.hindShinL.xRot += Math.max(0.0f, swingOpp) * 0.6f;
        this.hindShinR.xRot += Math.max(0.0f, swing) * 0.6f;
        this.body.y += Math.abs(Mth.sin(pos * 0.6662f)) * 0.8f * speed;
        this.body.zRot += Mth.cos(pos * 0.6662f) * 0.025f * speed;
        this.neck1.xRot += Mth.sin(pos * 1.3324f) * 0.03f * speed;
        this.foldWings(1.0f, state.ageInTicks);
    }

    private void sitPose(float t) {
        this.body.y += 5.0f;
        this.frontLegL.xRot -= 0.5f;
        this.frontLegR.xRot -= 0.5f;
        this.frontShinL.xRot -= 0.6f;
        this.frontShinR.xRot -= 0.6f;
        this.frontFootL.xRot += 1.0f;
        this.frontFootR.xRot += 1.0f;
        this.hindLegL.xRot -= 1.1f;
        this.hindLegR.xRot -= 1.1f;
        this.hindShinL.xRot += 1.6f;
        this.hindShinR.xRot += 1.6f;
        this.hindFootL.xRot -= 0.4f;
        this.hindFootR.xRot -= 0.4f;
        for (int i = 0; i < this.tail.length; i++) {
            this.tail[i].yRot += 0.22f;
            this.tail[i].xRot += i == 0 ? 0.25f : 0.0f;
        }
        this.foldWings(1.0f, t);
    }

    private void flyPose(DragonRenderState state, float t) {
        float flap = state.flapPhase;
        float amp = state.gliding ? 0.12f : 0.85f;
        float f = Mth.sin(flap);
        float follow = Mth.sin(flap - 0.7f);
        this.wingL.zRot += -f * amp - 0.08f;
        this.wingR.zRot += f * amp + 0.08f;
        this.wingTipL.zRot += -follow * amp * 0.55f;
        this.wingTipR.zRot += follow * amp * 0.55f;
        this.wingL.yRot += 0.12f;
        this.wingR.yRot -= 0.12f;
        // Beine angezogen, Schwanz gestreckt, Hals nach vorn
        this.frontLegL.xRot += 1.0f;
        this.frontLegR.xRot += 1.0f;
        this.frontShinL.xRot -= 0.5f;
        this.frontShinR.xRot -= 0.5f;
        this.hindLegL.xRot += 1.25f;
        this.hindLegR.xRot += 1.25f;
        this.hindShinL.xRot += 0.15f;
        this.hindShinR.xRot += 0.15f;
        this.hindFootL.xRot += 0.6f;
        this.hindFootR.xRot += 0.6f;
        this.tail[0].xRot += 0.22f;
        this.tail[1].xRot += 0.06f;
        this.tail[2].xRot -= 0.04f;
        this.neck1.xRot += 0.45f;
        this.neck3.xRot -= 0.1f;
        this.head.xRot -= 0.35f;
        this.body.xRot += Mth.clamp(state.flightPitch, -40.0f, 40.0f) * DEG * 0.6f;
        this.body.y += f * (state.gliding ? 0.3f : 1.6f);
    }

    /**
     * Flügel an den Körper legen (0 = ausgebreitet, 1 = ganz angelegt): der Flügel wird um seine eigene
     * Längsachse gedreht, damit die Flughaut senkrecht am Körper hängt, nach hinten geschwenkt, und die
     * Hand (mit der äußeren Flughaut) wird nach vorn zurückgeklappt und zusammengeschoben.
     */
    private void foldWings(float amount, float t) {
        float idle = Mth.sin(t * 0.05f) * 0.025f;
        this.wingL.xRot += -1.42f * amount;
        this.wingR.xRot += -1.42f * amount;
        this.wingL.yRot += -1.32f * amount;
        this.wingR.yRot += 1.32f * amount;
        this.wingL.zRot += (-0.42f + idle) * amount;
        this.wingR.zRot += (0.42f - idle) * amount;
        this.wingTipL.zRot += 2.95f * amount;
        this.wingTipR.zRot += -2.95f * amount;
        this.wingTipL.yRot += 0.12f * amount;
        this.wingTipR.yRot += -0.12f * amount;
        float squash = 1.0f - 0.45f * amount;
        this.wingTipL.xScale *= squash;
        this.wingTipR.xScale *= squash;
        // Flughaut im angelegten Zustand gerafft, damit sie nicht bis zum Boden hängt
        float chord = 1.0f - 0.5f * amount;
        this.wingL.zScale *= chord;
        this.wingR.zScale *= chord;
    }

    // ------------------------------------------------------------------ Einmalige Animationen

    private void applyRoar(DragonRenderState state) {
        if (!state.roarAnimation.isStarted()) {
            return;
        }
        float seconds = state.roarAnimation.getTimeInMillis(state.ageInTicks) / 1000.0f;
        if (seconds > 2.2f) {
            return;
        }
        float env = bump(seconds, 0.25f, 1.6f, 2.2f);
        this.neck1.xRot -= 0.45f * env;
        this.neck2.xRot -= 0.1f * env;
        this.head.xRot -= 0.35f * env;
        this.jaw.xRot += 0.85f * env;
        this.head.zRot += Mth.sin(seconds * 18.0f) * 0.03f * env;
        if (!state.flying) {
            this.wingL.zRot += 0.7f * env;
            this.wingR.zRot -= 0.7f * env;
            this.wingTipL.yRot += 1.6f * env;
            this.wingTipR.yRot -= 1.6f * env;
        }
    }

    private void applyBite(DragonRenderState state) {
        if (!state.biteAnimation.isStarted()) {
            return;
        }
        float seconds = state.biteAnimation.getTimeInMillis(state.ageInTicks) / 1000.0f;
        if (seconds > 0.6f) {
            return;
        }
        float lunge = bump(seconds, 0.12f, 0.25f, 0.6f);
        float open = seconds < 0.15f ? seconds / 0.15f : Math.max(0.0f, 1.0f - (seconds - 0.15f) / 0.12f);
        this.neck1.xRot += 0.4f * lunge;
        this.neck2.xRot += 0.15f * lunge;
        this.head.xRot -= 0.2f * lunge;
        this.jaw.xRot += 0.7f * open;
    }

    /** Glatte Hüllkurve: steigt bis {@code in}, hält bis {@code hold}, fällt bis {@code end}. */
    private static float bump(float x, float in, float hold, float end) {
        if (x < in) {
            return Mth.sin(x / in * Mth.HALF_PI);
        }
        if (x < hold) {
            return 1.0f;
        }
        return Math.max(0.0f, Mth.cos((x - hold) / (end - hold) * Mth.HALF_PI));
    }
}
