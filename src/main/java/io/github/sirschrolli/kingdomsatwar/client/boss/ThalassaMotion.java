package io.github.sirschrolli.kingdomsatwar.client.boss;

import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.env;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.rise;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.strike;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FOOT_X;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FRONT_L;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FRONT_R;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.SHIN_X;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.UPPER_X;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.UPPER_Z;

import net.minecraft.util.Mth;

/**
 * Bewegungen von Thalassa, der Seeschlange: Eine Welle läuft ständig vom Hals bis zur Schwanzflosse (beim Schwimmen
 * kräftiger und so schnell, wie sie vorankommt – wie bei einem Aal), die Brustflossen rudern versetzt, der Hals
 * windet sich in einer sanften S-Kurve. Der Kragen liegt in Ruhe angelegt und stellt sich beim Drohen auf.
 *
 * <p>Angriffe: Biss aus dem Wasser, Schwanzpeitsche, Wasserstrahl (Kragen auf), Flutwelle (aufbäumen und
 * niederschlagen), Strudel (eingerollt, kreisend), Blasenkäfig (Fauchen mit gespreiztem Kragen), Sprung aus der Tiefe
 * (gestreckt, Flossen angelegt, Schwanz schlägt wild). Erwachen: steigt eingerollt aus der Tiefe, streckt sich und
 * brüllt mit aufgestelltem Kragen (6,6 s). Tod: windet sich, erschlafft und sinkt.
 */
public class ThalassaMotion extends DragonMotion {
    public ThalassaMotion(int necks, int tails, boolean quadruped, float bodyHeight) {
        super(necks, tails, quadruped, bodyHeight);
    }

    /** Welle entlang des Leibes: Phase je Glied, Stärke wächst zur Schwanzflosse hin. */
    private void wave(DragonPose p, float phase, float amp, float pitchAmp) {
        for (int i = 0; i < this.tails; i++) {
            float w = 0.45f + 0.55f * i / (this.tails - 1.0f);
            p.tailYaw[i] += Mth.sin(phase - i * 0.62f) * amp * w;
            p.tailPitch[i] += Mth.sin(phase * 0.5f - i * 0.4f + 1.0f) * pitchAmp * w;
        }
    }

    /** Brustflossen rudern (die Kanäle der Vorderbeine): versetzt, mit Welle vom Ansatz zum Flossenblatt. */
    private static void paddle(DragonPose p, float phase, float amp) {
        for (int k = 0; k < 2; k++) {
            float[] leg = p.leg(k == 0 ? FRONT_L : FRONT_R);
            float ph = phase + k * Mth.PI * 0.15f;
            leg[UPPER_X] += Mth.sin(ph) * 0.35f * amp;
            leg[UPPER_Z] += Mth.cos(ph) * 0.18f * amp * (k == 0 ? 1.0f : -1.0f);
            leg[SHIN_X] += Mth.sin(ph - 0.7f) * 0.28f * amp;
            leg[FOOT_X] += Mth.sin(ph - 1.4f) * 0.4f * amp;
        }
    }

    @Override
    protected void idle(BossRenderState s, DragonPose p, float t, float air) {
        super.idle(s, p, t, air);
        float rate = s.phase == 3 ? 1.6f : s.phase == 2 ? 1.3f : 1.0f;
        this.wave(p, t * 2.2f * rate, 0.06f, 0.02f);
        paddle(p, t * 2.6f, 0.35f);
        // Hals in sanfter S-Kurve, wiegt sich wie eine Schlange
        p.neckYaw += Mth.sin(t * 0.7f) * 0.12f;
        p.neckRoll += Mth.sin(t * 0.7f + 1.2f) * 0.05f;
        p.headYaw -= Mth.sin(t * 0.7f) * 0.1f;
        p.bodyY += Mth.sin(t * 1.1f) * 0.5f;          // treibt im Wasser auf und ab
        p.bodyRoll += Mth.sin(t * 0.8f + 0.5f) * 0.03f;
        p.spread = 0.12f + 0.06f * Mth.sin(t * 0.9f);  // Kragen fast angelegt, atmet mit
    }

    @Override
    protected void walk(BossRenderState s, DragonPose p, float ground) {
        // Schwimmen: die Welle läuft so schnell nach hinten, wie sie vorankommt
        float speed = Math.min(s.walkAnimationSpeed * 1.6f, 1.0f);
        if (speed < 0.01f) {
            return;
        }
        float phase = s.walkAnimationPos * 0.55f;
        this.wave(p, phase, 0.13f * speed, 0.03f * speed);
        paddle(p, phase * 0.9f, speed);
        p.bodyYaw += Mth.sin(phase + 0.8f) * 0.06f * speed;
        p.neckYaw -= Mth.sin(phase + 0.3f) * 0.07f * speed;
        p.headYaw += Mth.sin(phase) * 0.05f * speed;   // der Kopf bleibt ruhig auf Kurs
        p.neckBend += 0.12f * speed;                   // Hals beim Schwimmen vorgestreckt
    }

    @Override
    protected void bite(DragonPose p, float a) {
        super.bite(p, a);
        p.spread += 0.8f * env(a, 0.1f, 0.4f, 0.7f, 1.0f);
    }

    @Override
    protected void tailSweep(DragonPose p, float a) {
        float coil = env(a, 0.0f, 0.4f, 0.5f, 0.6f);
        float whip = env(a, 0.5f, 0.6f, 1.2f, 1.7f);
        for (int i = 0; i < this.tails; i++) {
            float w = (i + 1.0f) / this.tails;
            p.tailYaw[i] += 0.16f * coil * w - Mth.sin(a * 14.0f - i * 0.5f) * 0.2f * whip * w;
        }
        p.neckBend += 0.12f * whip;
        p.jaw += 0.35f * whip;
        p.spread += 0.5f * whip;
    }

    @Override
    protected void breath(DragonPose p, float a) {
        super.breath(p, a);
        float charge = env(a, 0.0f, 0.8f, 0.9f, 1.05f);
        float spew = env(a, 0.9f, 1.05f, 3.0f, 3.4f);
        p.spread += 0.9f * Math.max(charge, spew) + 0.2f * spew;
        p.chest *= 1.0f + 0.05f * charge;
    }

    /** Flutwelle: bäumt sich hoch aus dem Wasser auf und schlägt mit dem Vorderleib nieder. */
    @Override
    protected void ring(DragonPose p, float a) {
        float rear = env(a, 0.0f, 0.85f, 0.95f, 1.05f);
        float slam = strike(a, 1.0f, 1.8f);
        p.bodyPitch -= 0.5f * rear - 0.15f * slam;
        p.bodyY -= 6.0f * rear - 2.0f * slam;
        p.neckBend += -0.35f * rear + 0.45f * slam;
        p.headPitch -= 0.3f * rear;
        p.jaw += 0.6f * rear + 0.8f * slam;
        p.spread += 1.0f * rear + 0.4f * slam;
        paddle(p, a * 9.0f, rear);
        for (int i = 0; i < this.tails; i++) {
            p.tailPitch[i] += (0.05f * rear - 0.04f * slam) * (i < 4 ? 1.0f : 0.3f);
        }
    }

    /** Strudel: eingerollt und kreisend (die Drehung selbst macht der Angriff), Kopf zur Mitte geneigt. */
    @Override
    protected void erupt(DragonPose p, float a) {
        float coil = env(a, 0.0f, 0.6f, 4.0f, 4.8f);
        for (int i = 0; i < this.tails; i++) {
            p.tailYaw[i] += 0.17f * coil + Mth.sin(a * 6.0f - i * 0.6f) * 0.05f * coil;
        }
        p.neckYaw -= 0.35f * coil;
        p.headYaw -= 0.2f * coil;
        p.bodyRoll -= 0.15f * coil;
        p.spread += 0.6f * coil;
        p.jaw += 0.3f * coil;
    }

    /** Blasenkäfig: Fauchen mit voll gespreiztem Kragen, der Kopf zittert vor, dann schnappt das Maul zu. */
    @Override
    protected void special1(BossRenderState s, DragonPose p, float a) {
        float hiss = env(a, 0.0f, 0.4f, 1.1f, 1.3f);
        float snap = env(a, 1.1f, 1.2f, 1.5f, 2.0f);
        p.spread += 1.2f * hiss + 0.6f * snap;
        p.jaw += 0.9f * hiss - 0.2f * snap;
        p.neckBend += -0.25f * hiss + 0.4f * snap;
        p.headPitch -= 0.2f * hiss;
        p.headRoll += Mth.sin(a * 32.0f) * 0.04f * hiss;
        p.chest *= 1.0f + 0.06f * hiss;
    }

    /** Sprung aus der Tiefe: gestreckt wie ein Speer, Flossen angelegt, der Schwanz schlägt wild. */
    @Override
    protected void special2(BossRenderState s, DragonPose p, float a) {
        float dive = env(a, 0.0f, 0.4f, 3.6f, 4.2f);
        this.wave(p, a * 16.0f, 0.12f * dive, 0.0f);
        for (int k = 0; k < 2; k++) {
            float[] leg = p.leg(k == 0 ? FRONT_L : FRONT_R);
            leg[UPPER_X] += 0.9f * dive;
            leg[UPPER_Z] += 0.4f * dive * (k == 0 ? 1.0f : -1.0f);
        }
        p.neckBend += 0.3f * dive;
        p.headPitch -= 0.2f * dive;
        p.spread = Math.min(p.spread, 0.0f);
        p.jaw += 0.5f * env(a, 1.6f, 1.9f, 2.4f, 2.8f);
    }

    @Override
    protected void roar(DragonPose p, float a) {
        super.roar(p, a);
        float e = env(a, 0.15f, 0.6f, 1.6f, 2.2f);
        p.spread += 1.2f * e;
        p.bodyY -= 3.0f * e;
    }

    @Override
    protected void volley(DragonPose p, float a) {
        super.volley(p, a);
        p.spread += 0.7f;
    }

    /** Erwachen: steigt eingerollt aus der Tiefe, streckt sich und brüllt mit aufgestelltem Kragen. */
    @Override
    protected void awakening(BossRenderState s, DragonPose p, float time) {
        float coiled = 1.0f - rise(time, 2.5f, 5.0f);
        float rearUp = env(time, 4.6f, 5.8f, 8.0f, 9.0f);
        float roar = env(time, 6.3f, 6.7f, 7.8f, 8.6f);
        for (int i = 0; i < this.tails; i++) {
            p.tailYaw[i] += 0.2f * coiled;
        }
        this.wave(p, time * 5.0f, 0.1f * (1.0f - coiled), 0.0f);
        p.neckYaw += 0.5f * coiled;
        p.neckBend += 0.4f * coiled - 0.45f * rearUp;
        p.neckTip += 0.25f * rearUp;
        p.headPitch += 0.3f * coiled - 0.5f * roar;
        p.bodyPitch -= 0.35f * rearUp;
        p.bodyY -= 5.0f * rearUp;
        p.jaw += 1.0f * roar;
        p.spread = 0.05f + 1.15f * Math.max(roar, rearUp * 0.6f);
        p.headRoll += Mth.sin(time * 20.0f) * 0.03f * roar;
        paddle(p, time * 7.0f, 0.4f + 0.6f * rearUp);
    }

    @Override
    protected void slumber(BossRenderState s, DragonPose p, float time) {
        float down = rise(time, 0.0f, 2.2f);
        for (int i = 0; i < this.tails; i++) {
            p.tailYaw[i] += 0.18f * down;
        }
        p.neckBend += 0.5f * down;
        p.headPitch += 0.2f * down;
        p.spread = 0.0f;
    }

    /** Tod: windet sich, erschlafft, der Kragen fällt in sich zusammen. */
    @Override
    protected void dying(BossRenderState s, DragonPose p, float time) {
        float thrash = env(time, 0.0f, 0.3f, 2.0f, 2.6f);
        float limp = rise(time, 2.2f, 4.0f);
        this.wave(p, time * 14.0f, 0.22f * thrash, 0.05f * thrash);
        for (int i = 0; i < this.tails; i++) {
            p.tailYaw[i] += 0.08f * limp * Mth.sin(i * 0.7f);
        }
        p.neckBend += -0.4f * thrash * Mth.abs(Mth.sin(time * 3.0f)) + 0.7f * limp;
        p.neckYaw += Mth.sin(time * 9.0f) * 0.25f * thrash;
        p.headRoll += 0.4f * limp;
        p.jaw += 0.8f * thrash + 0.3f * limp;
        p.spread = 0.6f * thrash;
        paddle(p, time * 12.0f, thrash);
    }
}
