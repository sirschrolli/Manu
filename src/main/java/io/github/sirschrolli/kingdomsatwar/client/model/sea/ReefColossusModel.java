package io.github.sirschrolli.kingdomsatwar.client.model.sea;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.sea.ReefColossusRenderState;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Karkinos, der Riffkoloss: Er trippelt seitwärts auf sechs langen Beinen, die Stielaugen schauen umher, Fühler und
 * Tangsträhnen wiegen sich, die Mundplatten zittern. Seine Angriffe:
 * <ul>
 *     <li><b>Scherenschlag:</b> die Brecherschere steigt hoch über ihn, verharrt – und schmettert in den Sand; dort steckt
 *     sie fest, er zerrt an ihr (Benommenheit), bis er sie herausreißt.</li>
 *     <li><b>Zangengriff:</b> die Greifschere schießt vor, öffnet sich, schnappt zu, hebt die Beute und schleudert sie mit
 *     einem Peitschenhieb zur Seite.</li>
 *     <li><b>Blasenstrahl:</b> er bäumt sich auf, die Scheren spreizen sich, die Mundplatten klappen weit auf.</li>
 *     <li><b>Eingraben und Ausbrechen:</b> die Beine schaufeln wild (das Einsinken übernimmt der Renderer), beim Ausbrechen
 *     stoßen beide Scheren nach oben.</li>
 *     <li><b>Panzerwirbel:</b> Beine und Scheren falten sich unter die Kuppel, die Augen ziehen sich ein (das Kreiseln
 *     übernimmt der Renderer).</li>
 *     <li><b>Ruf der Brut:</b> beide Scheren hoch, die Finger klappern schnell.</li>
 *     <li><b>Gezeitenzorn:</b> er bäumt sich auf und reckt die Scheren weit auseinander, der ganze Körper bebt.</li>
 *     <li><b>Flutlanzen:</b> die Brecherschere zeigt zum Himmel und schlägt im Augenblick der Lanzen nieder.</li>
 * </ul>
 * Im Tod rollen sich die Beine ein, die Scheren sinken, er sackt zusammen.
 */
public class ReefColossusModel extends EntityModel<ReefColossusRenderState> {
    private final ModelPart body;
    private final ModelPart shell;
    private final ModelPart spear;
    private final ModelPart eyeL;
    private final ModelPart eyeR;
    private final ModelPart antL;
    private final ModelPart antR;
    private final ModelPart antTipL;
    private final ModelPart antTipR;
    private final ModelPart mouthL;
    private final ModelPart mouthR;
    private final ModelPart armL;
    private final ModelPart armR;
    private final ModelPart foreL;
    private final ModelPart foreR;
    private final ModelPart clawL;
    private final ModelPart clawR;
    private final ModelPart fingerL;
    private final ModelPart fingerR;
    private final ModelPart[] legsL = new ModelPart[3];
    private final ModelPart[] legsR = new ModelPart[3];
    private final ModelPart[] lowL = new ModelPart[3];
    private final ModelPart[] lowR = new ModelPart[3];
    private final ModelPart[] kelp = new ModelPart[4];

    public ReefColossusModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.shell = this.body.getChild("shell");
        this.spear = this.shell.getChild("spear");
        for (int k = 0; k < this.kelp.length; k++) {
            this.kelp[k] = this.shell.getChild("kelp_" + k);
        }
        this.eyeL = this.body.getChild("left_eye");
        this.eyeR = this.body.getChild("right_eye");
        this.antL = this.body.getChild("left_antenna");
        this.antR = this.body.getChild("right_antenna");
        this.antTipL = this.antL.getChild("left_antenna_tip");
        this.antTipR = this.antR.getChild("right_antenna_tip");
        this.mouthL = this.body.getChild("mouth_left");
        this.mouthR = this.body.getChild("mouth_right");
        this.armL = this.body.getChild("left_arm");
        this.armR = this.body.getChild("right_arm");
        this.foreL = this.armL.getChild("left_forearm");
        this.foreR = this.armR.getChild("right_forearm");
        this.clawL = this.foreL.getChild("left_claw");
        this.clawR = this.foreR.getChild("right_claw");
        this.fingerL = this.clawL.getChild("left_claw_finger");
        this.fingerR = this.clawR.getChild("right_claw_finger");
        for (int k = 0; k < 3; k++) {
            this.legsL[k] = this.body.getChild("left_leg_" + k);
            this.legsR[k] = this.body.getChild("right_leg_" + k);
            this.lowL[k] = this.legsL[k].getChild("left_leg_" + k + "_low");
            this.lowR[k] = this.legsR[k].getChild("right_leg_" + k + "_low");
        }
    }

    /** Die Glieder vom Körper bis zur Spitze des Speers (für den Leuchthof im Renderer). */
    public List<ModelPart> spearChain() {
        return List.of(this.root(), this.body, this.shell, this.spear);
    }

    @Override
    public void setupAnim(ReefColossusRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float pos = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed * 1.4f, 1.0f);
        this.spear.visible = state.spear;

        // Ruhehaltung: die Scheren etwas erhoben vor dem Körper
        this.foreL.xRot -= 0.35f;
        this.foreR.xRot -= 0.25f;
        this.fingerL.yRot += 0.12f + Mth.sin(t * 0.05f) * 0.08f;
        this.fingerR.yRot -= 0.12f + Mth.sin(t * 0.055f + 1.0f) * 0.08f;

        // Atmen, Wippen beim Gehen, Augen, Fühler, Mund, Tang
        this.body.y += Mth.sin(t * 0.07f) * 0.3f - Math.abs(Mth.sin(pos * 0.9f)) * 0.8f * speed;
        this.body.zRot += Mth.sin(pos * 0.9f) * 0.03f * speed;
        float look = Mth.clamp(state.yRot, -50.0f, 50.0f) * Anim.DEG * 0.5f;
        this.eyeL.yRot += look + Mth.sin(t * 0.045f) * 0.3f;
        this.eyeR.yRot += look + Mth.sin(t * 0.04f + 1.9f) * 0.3f;
        this.eyeL.xRot += Mth.sin(t * 0.06f) * 0.12f;
        this.eyeR.xRot += Mth.sin(t * 0.06f + 1.1f) * 0.12f;
        this.antL.yRot += Mth.sin(t * 0.08f) * 0.18f;
        this.antR.yRot += Mth.sin(t * 0.075f + 2.0f) * 0.18f;
        this.antL.xRot += Mth.sin(t * 0.05f + 0.5f) * 0.1f;
        this.antR.xRot += Mth.sin(t * 0.05f + 1.5f) * 0.1f;
        this.antTipL.xRot += Mth.sin(t * 0.11f) * 0.15f;
        this.antTipR.xRot += Mth.sin(t * 0.1f + 1.2f) * 0.15f;
        float chew = Mth.sin(t * 0.35f) * 0.08f;
        this.mouthL.yRot += chew;
        this.mouthR.yRot -= chew;
        for (int k = 0; k < this.kelp.length; k++) {
            this.kelp[k].xRot += Mth.sin(t * 0.06f + k * 1.3f) * 0.12f;
            this.kelp[k].zRot += Mth.sin(t * 0.05f + k * 0.7f) * 0.1f;
        }

        // Laufbeine: je Seite drei, versetzt; heben (zRot) und greifen (yRot)
        for (int k = 0; k < 3; k++) {
            float phase = k * 2.1f;
            float liftL = Math.max(0.0f, Mth.sin(pos * 0.9f + phase)) * 0.4f * speed;
            float liftR = Math.max(0.0f, Mth.sin(pos * 0.9f + phase + Mth.PI)) * 0.4f * speed;
            float reachL = Mth.cos(pos * 0.9f + phase) * 0.22f * speed;
            float reachR = Mth.cos(pos * 0.9f + phase + Mth.PI) * 0.22f * speed;
            this.legsL[k].zRot -= liftL;
            this.legsR[k].zRot += liftR;
            this.legsL[k].yRot += reachL;
            this.legsR[k].yRot -= reachR;
            this.lowL[k].zRot += liftL * 0.5f;
            this.lowR[k].zRot -= liftR * 0.5f;
            this.legsL[k].zRot += Mth.sin(t * 0.09f + k) * 0.015f;
            this.legsR[k].zRot -= Mth.sin(t * 0.08f + k) * 0.015f;
        }

        this.awaken(state, t);
        this.slam(state, t);
        this.grab(state, t);
        this.jet(state, t);
        this.burrow(state, t);
        this.roll(state, t);
        this.summon(state, t);
        this.tide(state, t);
        this.lance(state, t);
        this.stun(state, t);
        this.death(state);
    }

    /** Erwachen: die Beine strampeln sich frei, am Ende reckt er beide Scheren zum Brüllen. */
    private void awaken(ReefColossusRenderState state, float t) {
        float s = Anim.seconds(state.awaken, t);
        if (s < 0.0f || s >= 4.4f) {
            return;
        }
        float flail = Anim.bump(s, 0.3f, 2.6f, 3.0f);
        for (int k = 0; k < 3; k++) {
            this.legsL[k].zRot -= Mth.sin(s * 9.0f + k) * 0.35f * flail;
            this.legsR[k].zRot += Mth.sin(s * 9.0f + k + 1.5f) * 0.35f * flail;
        }
        float roar = Anim.bump(s - 2.7f, 0.25f, 0.9f, 1.6f);
        this.body.xRot -= roar * 0.3f;
        this.foreL.xRot -= roar * 1.1f;
        this.foreR.xRot -= roar * 1.1f;
        this.armL.yRot -= roar * 0.35f;
        this.armR.yRot += roar * 0.35f;
        this.fingerL.yRot += roar * 0.5f;
        this.fingerR.yRot -= roar * 0.5f;
        this.mouthL.yRot -= roar * 0.6f;
        this.mouthR.yRot += roar * 0.6f;
    }

    /** Scherenschlag: hoch – verharren – niederschmettern – stecken bleiben (bis zur Benommenheit). */
    private void slam(ReefColossusRenderState state, float t) {
        float s = Anim.seconds(state.slam, t);
        if (s < 0.0f || s >= 2.9f) {
            return;
        }
        float raise = Anim.bump(s, 0.55f, 0.8f, 0.98f);
        float down = s < 0.82f ? 0.0f : Anim.bump(s - 0.82f, 0.16f, 1.6f, 2.05f);
        this.armL.zRot -= raise * 0.7f;
        this.foreL.xRot -= raise * 1.25f;
        this.clawL.xRot -= raise * 0.3f;
        this.fingerL.yRot += raise * 0.4f;
        this.body.xRot -= raise * 0.12f;
        this.armL.zRot += down * 0.35f;
        this.foreL.xRot += down * 0.75f;
        this.clawL.xRot += down * 0.35f;
        this.body.xRot += down * 0.14f;
        this.body.y += down * 1.2f;
    }

    /** Zangengriff: vorschießen, zuschnappen, hochheben, zur Seite schleudern. */
    private void grab(ReefColossusRenderState state, float t) {
        float s = Anim.seconds(state.grab, t);
        if (s >= 0.0f && s < 2.3f) {
            float reach = Anim.bump(s, 0.45f, 0.6f, 0.8f);
            float shut = s < 0.6f ? 0.0f : 1.0f;
            float lift = Anim.bump(s - 0.6f, 0.35f, 1.0f, 1.7f);
            this.armR.yRot -= reach * 0.45f;
            this.foreR.xRot += reach * 0.25f;
            this.fingerR.yRot -= reach * 0.7f * (1.0f - shut);
            this.fingerR.yRot += shut * 0.15f;
            this.body.xRot += reach * 0.1f;
            this.foreR.xRot -= lift * 0.9f;
            this.armR.zRot += lift * 0.4f;
        }
        float f = Anim.seconds(state.fling, t);
        if (f >= 0.0f && f < 0.7f) {
            float whip = Anim.bump(f, 0.12f, 0.2f, 0.65f);
            this.armR.yRot += whip * 0.9f;
            this.foreR.xRot -= whip * 0.4f;
            this.fingerR.yRot -= whip * 0.6f;
        }
    }

    /** Blasenstrahl: aufbäumen, Scheren spreizen, Mund weit auf (solange der Strahl läuft, zittert er). */
    private void jet(ReefColossusRenderState state, float t) {
        float s = Anim.seconds(state.jet, t);
        if (s < 0.0f || s >= 3.9f) {
            return;
        }
        float rear = Anim.bump(s, 0.7f, 3.3f, 3.8f);
        float shake = state.jetting ? Mth.sin(t * 2.7f) * 0.02f : 0.0f;
        this.body.xRot -= rear * 0.2f + shake;
        this.armL.yRot -= rear * 0.4f;
        this.armR.yRot += rear * 0.4f;
        this.foreL.xRot -= rear * 0.35f;
        this.foreR.xRot -= rear * 0.35f;
        this.mouthL.yRot -= rear * 0.75f;
        this.mouthR.yRot += rear * 0.75f;
        this.eyeL.zRot -= rear * 0.2f;
        this.eyeR.zRot += rear * 0.2f;
    }

    /** Eingraben (Beine schaufeln) und Ausbrechen (beide Scheren stoßen nach oben). */
    private void burrow(ReefColossusRenderState state, float t) {
        float s = Anim.seconds(state.burrow, t);
        if (s >= 0.0f && s < 1.2f) {
            float dig = Anim.bump(s, 0.15f, 0.9f, 1.15f);
            for (int k = 0; k < 3; k++) {
                this.legsL[k].yRot += Mth.sin(s * 22.0f + k * 2.0f) * 0.4f * dig;
                this.legsR[k].yRot -= Mth.sin(s * 22.0f + k * 2.0f + 1.0f) * 0.4f * dig;
            }
            this.body.xRot += dig * 0.15f;
            this.foreL.xRot += dig * 0.5f;
            this.foreR.xRot += dig * 0.5f;
        }
        float e = Anim.seconds(state.erupt, t);
        if (e >= 0.0f && e < 1.2f) {
            float up = Anim.bump(e, 0.12f, 0.5f, 1.15f);
            this.foreL.xRot -= up * 1.3f;
            this.foreR.xRot -= up * 1.3f;
            this.armL.yRot -= up * 0.3f;
            this.armR.yRot += up * 0.3f;
            this.body.xRot -= up * 0.2f;
            for (int k = 0; k < 3; k++) {
                this.legsL[k].zRot -= up * 0.3f;
                this.legsR[k].zRot += up * 0.3f;
            }
        }
    }

    /** Panzerwirbel: alles unter die Kuppel falten (das Kreiseln dreht der Renderer). */
    private void roll(ReefColossusRenderState state, float t) {
        float s = Anim.seconds(state.roll, t);
        if (s < 0.0f || s >= 5.3f) {
            return;
        }
        float tuck = Anim.bump(s, 0.6f, 4.9f, 5.25f);
        for (int k = 0; k < 3; k++) {
            this.legsL[k].zRot += tuck * 0.55f;
            this.legsR[k].zRot -= tuck * 0.55f;
            this.lowL[k].zRot += tuck * 0.8f;
            this.lowR[k].zRot -= tuck * 0.8f;
        }
        this.armL.yRot += tuck * 0.55f;
        this.armR.yRot -= tuck * 0.55f;
        this.foreL.xRot += tuck * 0.3f;
        this.foreR.xRot += tuck * 0.3f;
        this.eyeL.y += tuck * 4.0f;
        this.eyeR.y += tuck * 4.0f;
        this.antL.xRot += tuck * 0.9f;
        this.antR.xRot += tuck * 0.9f;
        this.body.y += tuck * 2.5f;
    }

    /** Ruf der Brut: beide Scheren hoch, die Finger klappern. */
    private void summon(ReefColossusRenderState state, float t) {
        float s = Anim.seconds(state.summon, t);
        if (s < 0.0f || s >= 2.4f) {
            return;
        }
        float up = Anim.bump(s, 0.35f, 1.9f, 2.35f);
        float clack = Math.max(0.0f, Mth.sin(s * 26.0f));
        this.foreL.xRot -= up * 1.0f;
        this.foreR.xRot -= up * 1.0f;
        this.armL.zRot -= up * 0.35f;
        this.armR.zRot += up * 0.35f;
        this.fingerL.yRot += up * clack * 0.6f;
        this.fingerR.yRot -= up * Math.max(0.0f, Mth.sin(s * 26.0f + 1.6f)) * 0.6f;
        this.body.xRot -= up * 0.1f;
    }

    /** Gezeitenzorn: aufbäumen, die Scheren weit auseinander, der ganze Körper bebt. */
    private void tide(ReefColossusRenderState state, float t) {
        float s = Anim.seconds(state.tide, t);
        if (s < 0.0f || s >= 2.6f) {
            return;
        }
        float rear = Anim.bump(s, 0.6f, 1.9f, 2.5f);
        float quake = Mth.sin(t * 2.3f) * 0.03f * rear;
        this.body.xRot -= rear * 0.35f + quake;
        this.armL.zRot -= rear * 0.6f;
        this.armR.zRot += rear * 0.6f;
        this.armL.yRot -= rear * 0.5f;
        this.armR.yRot += rear * 0.5f;
        this.foreL.xRot -= rear * 0.9f;
        this.foreR.xRot -= rear * 0.9f;
        this.fingerL.yRot += rear * 0.6f;
        this.fingerR.yRot -= rear * 0.6f;
        this.mouthL.yRot -= rear * 0.7f;
        this.mouthR.yRot += rear * 0.7f;
    }

    /** Flutlanzen: die Brecherschere zeigt zum Himmel und schlägt nieder, wenn die Lanzen fallen. */
    private void lance(ReefColossusRenderState state, float t) {
        float s = Anim.seconds(state.lance, t);
        if (s < 0.0f || s >= 3.0f) {
            return;
        }
        float point = Anim.bump(s, 0.5f, 1.75f, 1.9f);
        float strike = Anim.bump(s - 1.8f, 0.1f, 0.5f, 1.1f);
        this.armL.zRot -= point * 0.9f;
        this.foreL.xRot -= point * 1.5f;
        this.fingerL.yRot += point * 0.5f;
        this.foreL.xRot += strike * 0.6f;
        this.body.xRot += strike * 0.1f;
    }

    /** Benommen (Schere steckt fest oder nach dem Wirbel): er sackt ein, die Augen hängen, er zerrt und wankt. */
    private void stun(ReefColossusRenderState state, float t) {
        if (!state.stunned) {
            return;
        }
        float s = Math.max(0.0f, Anim.seconds(state.stun, t));
        float in = Mth.clamp(s / 0.3f, 0.0f, 1.0f);
        this.body.y += in * 1.0f;
        this.body.zRot += Mth.sin(t * 0.5f) * 0.05f * in;
        this.eyeL.xRot += in * 0.6f;
        this.eyeR.xRot += in * 0.6f;
        this.eyeL.zRot -= in * 0.3f;
        this.eyeR.zRot += in * 0.3f;
        this.armL.yRot += Mth.sin(t * 0.9f) * 0.12f * in;
        for (int k = 0; k < 3; k++) {
            this.legsL[k].zRot += Mth.sin(t * 0.6f + k) * 0.08f * in;
            this.legsR[k].zRot -= Mth.sin(t * 0.6f + k + 1.0f) * 0.08f * in;
        }
    }

    /** Im Tod: die Beine rollen sich ein, die Scheren sinken, die Augen hängen. */
    private void death(ReefColossusRenderState state) {
        if (state.deathTime <= 0.0f) {
            return;
        }
        float d = Mth.clamp(state.deathTime / 40.0f, 0.0f, 1.0f);
        for (int k = 0; k < 3; k++) {
            this.legsL[k].zRot += d * 0.5f;
            this.legsR[k].zRot -= d * 0.5f;
            this.lowL[k].zRot += d * 0.9f;
            this.lowR[k].zRot -= d * 0.9f;
        }
        this.foreL.xRot += d * 0.6f;
        this.foreR.xRot += d * 0.6f;
        this.armL.zRot += d * 0.3f;
        this.armR.zRot -= d * 0.3f;
        this.eyeL.xRot += d * 0.9f;
        this.eyeR.xRot += d * 0.9f;
        this.body.y += d * 2.5f;
        this.body.xRot += d * 0.12f;
        this.fingerL.yRot += d * 0.5f;
        this.fingerR.yRot -= d * 0.5f;
    }
}
