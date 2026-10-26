package io.github.sirschrolli.kingdomsatwar.client.boss;

import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * Animiertes Modell eines Drachen-Bosses mit Hals- und Schwanzkette, vier (oder zwei) Beinen und Fledermausflügeln.
 *
 * <p>Die Bewegung entsteht in zwei Stufen: {@link DragonMotion} bestimmt die Zielhaltung (Gang, Flug, Angriff, Laune),
 * {@link DragonRig} glättet sie mit Federn (Nachschwingen, Trägheit, Zusammenzucken). Das Modell legt das Ergebnis
 * nur noch auf die Grundhaltung der Geometrie.
 */
public class DragonBossModel extends EntityModel<BossRenderState> {
    private static final float DEG = Mth.DEG_TO_RAD;

    /** Ein Bein aus Ober-, Unterschenkel und Fuß. */
    protected record Leg(ModelPart upper, ModelPart shin, ModelPart foot, boolean front, float side) {}

    /** Ein Flügel: Oberarm, Unterarm, Handgelenk und Finger (sx = +1 links, -1 rechts). */
    protected record Wing(ModelPart arm, ModelPart fore, ModelPart hand, ModelPart[] fingers, float sx) {}

    /** Wie das Flügelpaar in diesem Bild steht. */
    protected static final class WingPose {
        float fold;
        float flapAmp;
        float flap;
        float raise;
        float sweep;
        float spread;
    }

    protected final ModelPart body;
    protected final ModelPart head;
    protected final ModelPart jaw;
    protected final ModelPart[] neck;
    protected final ModelPart[] tail;
    protected final @Nullable Leg frontL;
    protected final @Nullable Leg frontR;
    protected final @Nullable Leg hindL;
    protected final @Nullable Leg hindR;
    protected final @Nullable Wing wingL;
    protected final @Nullable Wing wingR;
    /** Höhe des Rumpfes über dem Boden in Modell-Einheiten (für Hinlegen und Zusammenbrechen). */
    protected final float bodyHeight;

    public DragonBossModel(ModelPart root, int necks, int tails, boolean quadruped, int fingers, float bodyHeight) {
        this(root, necks, tails, quadruped, true, fingers, bodyHeight);
    }

    /** Mit freier Wahl der Glieder: Vorderbeine (oder Flossen) und Hinterbeine (eine Seeschlange hat keine). */
    public DragonBossModel(ModelPart root, int necks, int tails, boolean front, boolean hind, int fingers, float bodyHeight) {
        super(root);
        Function<String, @Nullable ModelPart> part = root.createPartLookup();
        this.body = req(part, "body");
        this.head = req(part, "head");
        this.jaw = req(part, "jaw");
        this.neck = new ModelPart[necks];
        for (int i = 0; i < necks; i++) {
            this.neck[i] = req(part, "neck" + (i + 1));
        }
        this.tail = new ModelPart[tails];
        for (int i = 0; i < tails; i++) {
            this.tail[i] = req(part, "tail" + (i + 1));
        }
        this.frontL = front ? leg(part, "front", "l", 1) : null;
        this.frontR = front ? leg(part, "front", "r", -1) : null;
        this.hindL = hind ? leg(part, "hind", "l", 1) : null;
        this.hindR = hind ? leg(part, "hind", "r", -1) : null;
        this.wingL = fingers > 0 ? wing(part, "l", 1, fingers) : null;
        this.wingR = fingers > 0 ? wing(part, "r", -1, fingers) : null;
        this.bodyHeight = bodyHeight;
    }

    private static ModelPart req(Function<String, @Nullable ModelPart> part, String name) {
        ModelPart p = part.apply(name);
        if (p == null) {
            throw new IllegalStateException("Boss-Modell: Knochen fehlt: " + name);
        }
        return p;
    }

    private static Leg leg(Function<String, @Nullable ModelPart> part, String kind, String side, float sx) {
        return new Leg(req(part, kind + "_leg_" + side), req(part, kind + "_shin_" + side), req(part, kind + "_foot_" + side),
                kind.equals("front"), sx);
    }

    private static Wing wing(Function<String, @Nullable ModelPart> part, String side, float sx, int fingers) {
        ModelPart[] f = new ModelPart[fingers];
        for (int i = 0; i < fingers; i++) {
            f[i] = req(part, "wing_" + side + "_f" + (i + 1));
        }
        return new Wing(req(part, "wing_" + side), req(part, "wing_" + side + "_fore"), req(part, "wing_" + side + "_hand"), f, sx);
    }

    public int tailCount() {
        return this.tail.length;
    }

    /** Die Bewegungen dieses Körperbaus (Zielhaltung je Bild). */
    public DragonMotion createMotion() {
        return new DragonMotion(this.neck.length, this.tail.length, this.frontL != null, this.bodyHeight);
    }

    /** Die Federn für ein Exemplar (jeder Boss hat seine eigenen). */
    public DragonRig createRig() {
        return new DragonRig(this.neck.length, this.tail.length);
    }

    // =====================================================================================
    // Anwenden der geglätteten Haltung
    // =====================================================================================

    @Override
    public void setupAnim(BossRenderState s) {
        super.setupAnim(s);
        // Rumpf (Hals, Schwanz, Beine und Flügel hängen daran)
        this.body.y += s.bodyY;
        this.body.xRot += s.bodyPitch;
        this.body.zRot += s.bodyRoll;
        this.body.yRot += s.bodyYaw;
        this.body.xScale *= s.chest;
        this.body.yScale *= 1.0f + (s.chest - 1.0f) * 0.7f;
        // Hals und Kopf
        for (int i = 0; i < this.neck.length && i < s.neckPitch.length; i++) {
            this.neck[i].xRot += s.neckPitch[i];
            this.neck[i].yRot += s.neckYaw[i];
            this.neck[i].zRot += s.neckRoll[i];
        }
        this.head.xRot += s.headPitch;
        this.head.yRot += s.headYaw;
        this.head.zRot += s.headRoll;
        this.jaw.xRot += s.jaw;
        // Schwanz
        for (int i = 0; i < this.tail.length && i < s.tailYaw.length; i++) {
            this.tail[i].yRot += s.tailYaw[i];
            this.tail[i].xRot += s.tailPitch[i];
        }
        // Beine
        this.applyLeg(this.frontL, s.legs[DragonPose.FRONT_L]);
        this.applyLeg(this.frontR, s.legs[DragonPose.FRONT_R]);
        this.applyLeg(this.hindL, s.legs[DragonPose.HIND_L]);
        this.applyLeg(this.hindR, s.legs[DragonPose.HIND_R]);
        // Flügel
        this.applyWings(s);
    }

    private void applyLeg(@Nullable Leg leg, float[] v) {
        if (leg == null) {
            return;
        }
        leg.upper().xRot += v[DragonPose.UPPER_X];
        leg.upper().zRot += v[DragonPose.UPPER_Z];
        leg.shin().xRot += v[DragonPose.SHIN_X];
        leg.foot().xRot += v[DragonPose.FOOT_X];
    }

    // =====================================================================================
    // Flügel
    // =====================================================================================

    /**
     * Legt die Flügel an bzw. breitet sie aus – Oberarm, Unterarm und Hand jeweils mit eigener Feder, so entfaltet sich
     * der Flügel gestaffelt wie bei einer Fledermaus. Angelegt rollt der Oberarm so, dass die Flughaut senkrecht am
     * Körper hängt, der Unterarm klappt nach vorn zurück, die Hand mit den Fingern wieder nach hinten.
     *
     * <p>Flügelschlag: kräftiger, schneller Abschlag (Vorderkante leicht nach unten gedreht), langsamerer Aufschlag mit
     * eingeknicktem Handgelenk und zusammengelegten Fingern; die Bewegung läuft als Welle vom Arm zu den Fingern.
     * Beim Gleiten zittern die Flügelspitzen im Wind.
     */
    protected void applyWings(BossRenderState s) {
        if (this.wingL == null || this.wingR == null) {
            return;
        }
        Wing[] wings = {this.wingL, this.wingR};
        for (int side = 0; side < 2; side++) {
            this.foldWing(s, wings[side], side);
            this.flapWing(s, wings[side], side);
        }
    }

    /** Anlegen: Standard ist der eng an den Rumpf gelegte Flügel (Flughaut senkrecht am Körper). */
    protected void foldWing(BossRenderState s, Wing w, int side) {
        float sx = w.sx();
        float fa = s.foldArm[side];
        float ff = s.foldFore[side];
        float fh = s.foldHand[side];
        // Anlegen (Flughäute werden dabei quer zu den Knochen zusammengeschoben)
        w.arm().xRot += -1.3f * fa;
        w.arm().yRot += -1.3f * sx * fa;
        w.arm().zRot += -0.35f * sx * fa;
        w.arm().zScale *= 1.0f - 0.4f * fa;
        w.fore().zRot += 2.85f * sx * ff;
        w.fore().zScale *= 1.0f - 0.4f * ff;
        w.hand().xRot += 1.57f * fh;
        w.hand().zRot += -2.97f * sx * fh;
        this.foldFingers(s, w, fh);
    }

    /** Finger zusammenlegen (die Flughaut dazwischen wird schmal) bzw. beim Gleiten spreizen. */
    protected void foldFingers(BossRenderState s, Wing w, float fh) {
        float openHand = 1.0f - fh;
        for (ModelPart finger : w.fingers()) {
            finger.yRot *= 1.0f - 0.92f * fh + 0.18f * s.spread * openHand;
            finger.zScale *= 1.0f - 0.8f * fh;
            // angelegt reichen die Finger nur bis zum Ellbogen zurück (sonst ragen sie wie Stöcke über den Rumpf hinaus)
            finger.xScale *= 1.0f - 0.4f * fh;
        }
    }

    /** Flügelschlag (nur soweit der Flügel ausgebreitet ist). */
    protected void flapWing(BossRenderState s, Wing w, int side) {
        float t = s.ageInTicks / 20.0f;
        float stroke = BossAnim.stroke(s.flap);
        float strokeLag = BossAnim.stroke(s.flap - 0.55f);
        float strokeLag2 = BossAnim.stroke(s.flap - 1.0f);
        float up = BossAnim.upstroke(s.flap);
        float sx = w.sx();
        float open = 1.0f - s.foldArm[side];
        float amp = s.flapAmp * open;
        // Ausschlag: oben etwa 50°, unten nur etwa 23° unter die Waagerechte – Unterarm und Hand schwingen etwas nach,
        // die Spitze endet bei gut 35° (der Flügel hängt nie senkrecht herab)
        float armSwing = stroke > 0 ? stroke * 0.85f : stroke * 0.4f;
        float lag = strokeLag < 0 ? strokeLag * 0.15f : strokeLag * 0.3f;
        float lag2 = strokeLag2 < 0 ? strokeLag2 * 0.1f : strokeLag2 * 0.18f;
        w.arm().zRot += (-armSwing - s.raise * 0.6f) * sx * amp + (-s.raise * 0.6f) * sx * (1.0f - amp) * open;
        w.arm().xRot += stroke * 0.08f * amp;
        w.fore().zRot += (-lag + up * 0.6f) * sx * amp;
        w.hand().zRot += (up * 0.5f - lag2) * sx * amp;
        w.arm().yRot += (s.sweep * 0.6f + up * 0.16f * amp) * -sx;
        for (int i = 0; i < w.fingers().length; i++) {
            ModelPart finger = w.fingers()[i];
            finger.yRot *= 1.0f - 0.28f * up * amp;
            // Gleiten: die Spitzen zittern leicht im Fahrtwind
            finger.zRot += Mth.sin(t * 11.0f + i * 1.7f + side) * 0.025f * open * (1.0f - Math.min(1.0f, s.flapAmp * 3.0f));
        }
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    public static boolean isDying(BossRenderState s) {
        return s.bossState == DragonBossEntity.State.DYING || s.deathSeconds > 0;
    }
}
