package io.github.sirschrolli.kingdomsatwar.client.boss;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Federn für weiche, lebendige Bewegungen eines Drachen-Bosses.
 *
 * <p>Jeder bewegliche Teil folgt seiner Zielhaltung ({@link DragonPose}) wie an einer gedämpften Feder:
 * <ul>
 *     <li>Halsglieder federn einzeln – die oberen weicher als die unteren, so läuft jede Bewegung als Welle zum Kopf
 *     hoch und schwingt ein wenig über;</li>
 *     <li>der Schwanz ist eine Kette mit eigener Trägheit: dreht sich der Körper, bleibt er zurück und schwingt nach,
 *     die Spitze am weitesten; wippt der Körper (Schritte, Flügelschläge), wippt der Schwanz mit;</li>
 *     <li>Kopf, Kiefer und Rumpf federn schnell, aber mit leichtem Nachschwingen;</li>
 *     <li>die Flügel entfalten sich gestaffelt: erst der Oberarm, dann Unterarm, dann Hand und Finger;</li>
 *     <li>Treffer lassen den Drachen zusammenzucken.</li>
 * </ul>
 * Gerechnet wird in kleinen Zeitschritten – unabhängig von der Bildrate.
 */
public final class DragonRig {
    private static final float DEG = Mth.DEG_TO_RAD;
    private static final float MAX_STEP = 1.0f / 90.0f;
    private static final float TAIL_LIMIT = 0.42f;

    /** Gedämpfte Feder: Wert, Geschwindigkeit, Steifigkeit und Dämpfung. */
    static final class Spring {
        float x;
        float v;
        final float k;
        final float c;

        Spring(float k, float zeta) {
            this.k = k;
            this.c = 2.0f * zeta * Mth.sqrt(k);
        }

        void step(float target, float h) {
            this.v += (this.k * (target - this.x) - this.c * this.v) * h;
            this.x += this.v * h;
        }

        void snap(float target) {
            this.x = target;
            this.v = 0.0f;
        }
    }

    private final int necks;
    private final int tails;
    private final float[] bendShare;
    private final float[] yawShare;
    private final Spring[] neckPitch;
    private final Spring[] neckYaw;
    private final Spring[] neckRoll;
    private final float[] tailAbs;
    private final float[] tailAbsV;
    private final float[] tailK;
    private final float[] tailC;
    private final Spring[] tailPitch;
    private final Spring headPitch = new Spring(150, 0.55f);
    private final Spring headYaw = new Spring(120, 0.55f);
    private final Spring headRoll = new Spring(100, 0.5f);
    private final Spring jaw = new Spring(230, 0.5f);
    private final Spring bodyY = new Spring(70, 0.55f);
    private final Spring bodyPitch = new Spring(55, 0.6f);
    private final Spring bodyRoll = new Spring(40, 0.55f);
    private final Spring bodyYaw = new Spring(45, 0.65f);
    private final Spring chest = new Spring(40, 0.9f);
    private final Spring[] foldArm = {new Spring(42, 0.85f), new Spring(42, 0.85f)};
    private final Spring[] foldFore = {new Spring(26, 0.85f), new Spring(26, 0.85f)};
    private final Spring[] foldHand = {new Spring(17, 0.85f), new Spring(17, 0.85f)};
    private final Spring flapAmp = new Spring(22, 1.0f);
    private final Spring raise = new Spring(38, 0.75f);
    private final Spring sweep = new Spring(38, 0.75f);
    private final Spring spread = new Spring(38, 0.8f);
    private final RandomSource random = RandomSource.create();

    private float lastTime = Float.NaN;
    private float lastBodyYawDeg;
    private float bodyYawAbs;
    private int lastHurt;
    private float lastHurtTime = -10.0f;

    public DragonRig(int necks, int tails) {
        this.necks = necks;
        this.tails = tails;
        this.neckPitch = springs(necks, 150, 75, 0.62f);
        this.neckYaw = springs(necks, 130, 65, 0.62f);
        this.neckRoll = springs(necks, 90, 60, 0.6f);
        this.tailPitch = springs(tails, 60, 16, 0.42f);
        this.tailAbs = new float[tails];
        this.tailAbsV = new float[tails];
        this.tailK = new float[tails];
        this.tailC = new float[tails];
        for (int i = 0; i < tails; i++) {
            float u = tails > 1 ? (float) i / (tails - 1) : 0.0f;
            this.tailK[i] = Mth.lerp(u, 70.0f, 16.0f);
            this.tailC[i] = 2.0f * 0.42f * Mth.sqrt(this.tailK[i]);
        }
        // Beugen: unten am meisten; Drehen: in der Halsmitte am meisten
        this.bendShare = new float[necks];
        this.yawShare = new float[necks];
        float bendSum = 0;
        float yawSum = 0;
        for (int i = 0; i < necks; i++) {
            this.bendShare[i] = necks - i * 0.6f;
            this.yawShare[i] = 0.55f + Mth.sin((i + 0.5f) / necks * Mth.PI);
            bendSum += this.bendShare[i];
            yawSum += this.yawShare[i];
        }
        for (int i = 0; i < necks; i++) {
            this.bendShare[i] /= bendSum;
            this.yawShare[i] /= yawSum;
        }
    }

    private static Spring[] springs(int n, float kBase, float kTip, float zeta) {
        Spring[] out = new Spring[n];
        for (int i = 0; i < n; i++) {
            float u = n > 1 ? (float) i / (n - 1) : 0.0f;
            out[i] = new Spring(Mth.lerp(u, kBase, kTip), zeta);
        }
        return out;
    }

    /**
     * Rechnet die Federn bis zum aktuellen Zeitpunkt weiter und schreibt das Ergebnis in den Zeichenzustand.
     *
     * @param time      Sekunden (mit Bruchteil des Ticks)
     * @param bodyYaw   Körperdrehung in Grad (geglättet)
     * @param hurtTime  Treffer-Zähler des Wesens (10 = gerade getroffen)
     */
    public void update(BossRenderState s, DragonPose p, float time, float bodyYaw, int hurtTime) {
        float dt = Float.isNaN(this.lastTime) ? -1.0f : time - this.lastTime;
        this.lastTime = time;
        float dYaw = Mth.wrapDegrees(bodyYaw - this.lastBodyYawDeg) * DEG;
        this.lastBodyYawDeg = bodyYaw;
        float yawFrom = this.bodyYawAbs;
        this.bodyYawAbs += dYaw;

        if (dt <= 0.0f || dt > 0.3f) {
            this.snap(p);
        } else {
            int steps = Math.max(1, Mth.ceil(dt / MAX_STEP));
            float h = dt / steps;
            for (int k = 1; k <= steps; k++) {
                float bodyAngle = Mth.lerp((float) k / steps, yawFrom, this.bodyYawAbs);
                this.step(p, h, bodyAngle);
            }
        }
        if (hurtTime > this.lastHurt && hurtTime >= 8) {
            this.flinch(time);
        }
        this.lastHurt = hurtTime;
        this.write(s, p);
    }

    private void snap(DragonPose p) {
        for (int i = 0; i < this.necks; i++) {
            this.neckPitch[i].snap(this.neckPitchTarget(p, i));
            this.neckYaw[i].snap(p.neckYaw * this.yawShare[i]);
            this.neckRoll[i].snap(p.neckRoll * this.yawShare[i]);
        }
        float parent = this.bodyYawAbs;
        for (int i = 0; i < this.tails; i++) {
            this.tailAbs[i] = parent + p.tailYaw[i];
            this.tailAbsV[i] = 0;
            parent = this.tailAbs[i];
            this.tailPitch[i].snap(p.tailPitch[i]);
        }
        this.headPitch.snap(p.headPitch);
        this.headYaw.snap(p.headYaw);
        this.headRoll.snap(p.headRoll);
        this.jaw.snap(p.jaw);
        this.bodyY.snap(p.bodyY);
        this.bodyPitch.snap(p.bodyPitch);
        this.bodyRoll.snap(p.bodyRoll);
        this.bodyYaw.snap(p.bodyYaw);
        this.chest.snap(p.chest);
        for (int side = 0; side < 2; side++) {
            this.foldArm[side].snap(p.fold);
            this.foldFore[side].snap(p.fold);
            this.foldHand[side].snap(p.fold);
        }
        this.flapAmp.snap(p.flapAmp);
        this.raise.snap(p.raise);
        this.sweep.snap(p.sweep);
        this.spread.snap(p.spread);
    }

    private float neckPitchTarget(DragonPose p, int i) {
        float target = p.neckBend * this.bendShare[i];
        if (i == this.necks - 1) {
            target += p.neckTip;
        }
        return target;
    }

    private void step(DragonPose p, float h, float bodyAngle) {
        for (int i = 0; i < this.necks; i++) {
            this.neckPitch[i].step(this.neckPitchTarget(p, i), h);
            this.neckYaw[i].step(p.neckYaw * this.yawShare[i], h);
            this.neckRoll[i].step(p.neckRoll * this.yawShare[i], h);
        }
        this.headPitch.step(p.headPitch, h);
        this.headYaw.step(p.headYaw, h);
        this.headRoll.step(p.headRoll, h);
        this.jaw.step(p.jaw, h);
        float vyBefore = this.bodyY.v;
        this.bodyY.step(p.bodyY, h);
        float bodyAccel = (this.bodyY.v - vyBefore) / h;
        this.bodyPitch.step(p.bodyPitch, h);
        this.bodyRoll.step(p.bodyRoll, h);
        this.bodyYaw.step(p.bodyYaw, h);
        this.chest.step(p.chest, h);
        // Schwanz: Gier als träge Kette, Nicken mit der Trägheit des wippenden Körpers
        float parent = bodyAngle + this.bodyYaw.x;
        for (int i = 0; i < this.tails; i++) {
            float target = parent + p.tailYaw[i];
            this.tailAbsV[i] += (this.tailK[i] * (target - this.tailAbs[i]) - this.tailC[i] * this.tailAbsV[i]) * h;
            this.tailAbs[i] += this.tailAbsV[i] * h;
            float local = this.tailAbs[i] - parent;
            if (local > TAIL_LIMIT || local < -TAIL_LIMIT) {
                this.tailAbs[i] = parent + Mth.clamp(local, -TAIL_LIMIT, TAIL_LIMIT);
                this.tailAbsV[i] *= 0.5f;
            }
            parent = this.tailAbs[i];
            float weight = (i + 1.0f) / this.tails;
            this.tailPitch[i].v += bodyAccel * 0.0045f * weight * h;
            this.tailPitch[i].step(p.tailPitch[i], h);
        }
        for (int side = 0; side < 2; side++) {
            this.foldArm[side].step(p.fold, h);
            this.foldFore[side].step(p.fold, h);
            this.foldHand[side].step(p.fold, h);
        }
        this.flapAmp.step(p.flapAmp, h);
        this.raise.step(p.raise, h);
        this.sweep.step(p.sweep, h);
        this.spread.step(p.spread, h);
    }

    /** Zusammenzucken bei einem Treffer: Kopf ruckt, Kiefer schnappt auf, Rumpf und Schwanz zucken. */
    private void flinch(float time) {
        float strength = time - this.lastHurtTime < 0.6f ? 0.35f : 1.0f;   // ein ganzes Heer trifft ständig
        this.lastHurtTime = time;
        float side = this.random.nextBoolean() ? 1.0f : -1.0f;
        this.headYaw.v += 2.4f * side * strength;
        this.headPitch.v -= 1.8f * strength;
        this.jaw.v += 5.0f * strength;
        this.bodyRoll.v += 0.7f * side * strength;
        this.bodyY.v += 7.0f * strength;
        for (int i = 0; i < this.necks; i++) {
            this.neckPitch[i].v -= 1.4f * strength * (i + 1.0f) / this.necks;
            this.neckYaw[i].v += 0.9f * side * strength;
        }
        for (int i = 0; i < this.tails; i++) {
            this.tailAbsV[i] += 1.6f * side * strength * (i + 1.0f) / this.tails;
        }
    }

    private void write(BossRenderState s, DragonPose p) {
        s.ensureRig(this.necks, this.tails);
        for (int i = 0; i < this.necks; i++) {
            s.neckPitch[i] = this.neckPitch[i].x;
            s.neckYaw[i] = this.neckYaw[i].x;
            s.neckRoll[i] = this.neckRoll[i].x;
        }
        float parent = this.bodyYawAbs + this.bodyYaw.x;
        for (int i = 0; i < this.tails; i++) {
            s.tailYaw[i] = Mth.clamp(this.tailAbs[i] - parent, -TAIL_LIMIT, TAIL_LIMIT);
            parent = this.tailAbs[i];
            s.tailPitch[i] = this.tailPitch[i].x;
        }
        s.headPitch = this.headPitch.x;
        s.headYaw = this.headYaw.x;
        s.headRoll = this.headRoll.x;
        s.jaw = Math.max(-0.05f, this.jaw.x);
        s.bodyY = this.bodyY.x;
        s.bodyPitch = this.bodyPitch.x;
        s.bodyRoll = this.bodyRoll.x;
        s.bodyYaw = this.bodyYaw.x;
        s.chest = this.chest.x;
        for (int side = 0; side < 2; side++) {
            s.foldArm[side] = Mth.clamp(this.foldArm[side].x, 0.0f, 1.0f);
            s.foldFore[side] = Mth.clamp(this.foldFore[side].x, 0.0f, 1.0f);
            s.foldHand[side] = Mth.clamp(this.foldHand[side].x, 0.0f, 1.0f);
        }
        s.flapAmp = Math.max(0.0f, this.flapAmp.x);
        s.flap = p.flap;
        s.raise = this.raise.x;
        s.sweep = this.sweep.x;
        s.spread = this.spread.x;
        for (int leg = 0; leg < 4; leg++) {
            System.arraycopy(p.legs[leg], 0, s.legs[leg], 0, 4);
        }
    }
}
