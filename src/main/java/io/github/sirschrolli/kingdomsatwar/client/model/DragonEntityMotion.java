package io.github.sirschrolli.kingdomsatwar.client.model;

import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.env;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.smooth;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.wobble;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FOOT_X;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FRONT_L;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FRONT_R;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.HIND_L;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.HIND_R;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.SHIN_X;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.UPPER_X;

import io.github.sirschrolli.kingdomsatwar.client.boss.BossRenderState;
import io.github.sirschrolli.kingdomsatwar.client.boss.DragonMotion;
import io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.DragonRenderState;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonGait;
import net.minecraft.util.Mth;

/**
 * Bewegungen der Drachen (wild und gezähmt) – dieselbe Bewegungslehre wie bei den Drachen-Bossen ({@link DragonMotion}):
 * sichtbares Atmen, Gewicht verlagern, Kopf nie ganz still, kräftiger Flügelschlag mit Auf und Ab, der Schwanz steuert in
 * Kurven, kleine Launen in ruhigen Momenten. Die Federn ({@link io.github.sirschrolli.kingdomsatwar.client.boss.DragonRig})
 * machen daraus fließende Übergänge – Abheben und Landen, Hinsetzen und Aufstehen, Kopf und Schwanz schwingen nach.
 *
 * <p>Eigen sind:
 * <ul>
 *     <li>der Gang nach {@link DragonGait}: ein ruhiger Schritt, der mit der Geschwindigkeit fließend in einen Galopp mit
 *     schaukelndem Rumpf übergeht; die Füße bleiben in der Standphase stehen, statt über den Boden zu rutschen;</li>
 *     <li>das Sitzen (Hinterbeine untergeschlagen, Brust aufgerichtet, der Schwanz um den Körper gelegt);</li>
 *     <li>Odem, Biss und Brüllen der Reitdrachen.</li>
 * </ul>
 */
public final class DragonEntityMotion extends DragonMotion {
    /** Die Vorderbeine sind kürzer: sie schwingen weiter aus, damit vorn und hinten gleich weit geschoben wird. */
    private static final float FRONT_SWING = 0.47f;

    public DragonEntityMotion() {
        super(3, 6, true, 17.0f);
    }

    @Override
    public void pose(BossRenderState s, DragonPose p) {
        super.pose(s, p);
        DragonRenderState d = (DragonRenderState) s;
        // Abheben und Landen: die Flügel gehen dabei hoch (sie entfalten sich nach oben, beim Landen bremsen sie), die
        // Brust richtet sich auf, die Beine greifen nach dem Boden
        float lift = 4.0f * d.airBlend * (1.0f - d.airBlend);
        if (lift > 0.001f) {
            p.raise += 0.85f * lift;
            p.bodyPitch -= 0.12f * lift;
            p.neckBend -= 0.08f * lift;
            for (int leg : new int[] {FRONT_L, FRONT_R}) {
                p.leg(leg)[UPPER_X] -= 0.35f * lift;
                p.leg(leg)[SHIN_X] -= 0.3f * lift;
            }
        }
        this.sit(p, d.sit * (1.0f - d.airBlend));
        if (d.breathTime >= 0.0f) {
            this.spew(d, p, d.breathTime);
        }
        float roar = Anim.seconds(d.roarAnimation, d.ageInTicks);
        if (roar >= 0.0f && roar < 2.2f) {
            this.roar(p, roar);
        }
        float bite = Anim.seconds(d.biteAnimation, d.ageInTicks);
        if (bite >= 0.0f && bite < 0.7f) {
            this.snapBite(p, bite, d.biteDip);
        }
    }

    // =====================================================================================
    // Gang
    // =====================================================================================

    @Override
    protected void walk(BossRenderState s, DragonPose p, float ground) {
        DragonRenderState d = (DragonRenderState) s;
        float speed = d.strideSpeed;
        float fade = Math.min(1.0f, speed * 4.0f) * ground;
        if (fade < 0.01f) {
            return;
        }
        float run = DragonGait.run(speed);
        float stance = DragonGait.stance(run);
        float reach = DragonGait.reach(speed, run) * fade;
        float cycle = d.stride;
        // Schritt: links hinten, links vorn, rechts hinten, rechts vorn – Galopp: beide hinten, dann beide vorn
        this.leg(p.leg(HIND_L), cycle, 0.0f, stance, reach, false);
        this.leg(p.leg(HIND_R), cycle, Mth.lerp(run, 0.5f, 0.1f), stance, reach, false);
        this.leg(p.leg(FRONT_L), cycle, Mth.lerp(run, 0.25f, 0.5f), stance, reach, true);
        this.leg(p.leg(FRONT_R), cycle, Mth.lerp(run, 0.75f, 0.6f), stance, reach, true);

        float c = cycle * Mth.TWO_PI;
        float walk = (1.0f - run) * fade;
        float gallop = run * fade;
        // Schritt: der Rumpf senkt sich bei jedem Aufsetzen, das Gewicht pendelt, der Kopf bleibt ruhig
        float bob = 0.5f - 0.5f * Mth.cos(c * 2.0f);
        p.bodyY += 0.7f * bob * walk;
        p.bodyRoll += 0.045f * Mth.sin(c) * walk;
        p.bodyYaw += 0.03f * Mth.sin(c + 0.6f) * walk;
        p.bodyPitch += 0.015f * Mth.sin(c * 2.0f + 0.8f) * walk;
        p.neckBend -= 0.05f * bob * walk;
        p.headPitch += 0.04f * bob * walk;
        p.headYaw -= 0.03f * Mth.sin(c + 0.6f) * walk;
        // Galopp: die Hinterbeine schieben die Brust hoch, die Vorderbeine fangen sie ab – der Rumpf schaukelt, in der
        // Flugphase hebt er ab; Hals und Kopf gleichen aus
        p.bodyPitch -= 0.075f * Mth.sin(c) * gallop;
        p.bodyY += 1.3f * Mth.cos(c * 2.0f - 0.8f * Mth.PI) * gallop;
        p.neckBend += 0.08f * Mth.sin(c) * gallop;
        p.headPitch += 0.05f * Mth.sin(c) * gallop;
        for (int i = 0; i < this.tails; i++) {
            p.tailYaw[i] += Mth.sin(c - 0.8f - i * 0.55f) * (0.04f + i * 0.014f) * walk;
            p.tailPitch[i] += Mth.sin(c - 1.0f - i * 0.5f) * 0.05f * gallop;
        }
    }

    /**
     * Ein Bein: in der Standphase steht der Fuß, das Bein wandert von vorn nach hinten; in der Schwungphase schwingt es
     * vor, Knie und Fuß beugen sich. Positiver Winkel am Oberschenkel = Fuß nach hinten.
     */
    private void leg(float[] leg, float cycle, float offset, float stance, float reach, boolean front) {
        float ph = cycle + offset;
        ph -= Mth.floor(ph);
        float amp = (front ? FRONT_SWING : DragonGait.SWING) * reach;
        float swing;
        float lift;
        if (ph < stance) {
            swing = Mth.lerp(ph / stance, -1.0f, 1.0f);
            lift = 0.0f;
        } else {
            float u = (ph - stance) / (1.0f - stance);
            swing = Mth.lerp(smooth(u), 1.0f, -1.0f);
            lift = Mth.sin(u * Mth.PI);
        }
        float bend = Math.min(reach, 1.2f);
        leg[UPPER_X] += swing * amp;
        if (front) {
            leg[SHIN_X] += lift * 0.95f * bend;
            leg[FOOT_X] += -lift * 0.6f * bend + swing * 0.1f * bend;
        } else {
            leg[SHIN_X] -= lift * 0.75f * bend;
            leg[FOOT_X] += lift * 0.85f * bend - swing * 0.08f * bend;
        }
    }

    // =====================================================================================
    // Sitzen, Odem, Biss
    // =====================================================================================

    /**
     * Sitzen wie eine Katze: die Hinterbeine untergeschlagen, das Hinterteil am Boden, die Vorderbeine aufgestützt, die
     * Brust aufgerichtet, der Hals stolz erhoben; der Schwanz liegt am Boden und um den Körper gelegt.
     */
    private void sit(DragonPose p, float w) {
        if (w <= 0.001f) {
            return;
        }
        p.bodyY += 4.0f * w;
        p.bodyPitch -= 0.3f * w;
        for (int leg : new int[] {FRONT_L, FRONT_R}) {
            p.leg(leg)[UPPER_X] += 0.33f * w;
            p.leg(leg)[SHIN_X] -= 0.35f * w;
            p.leg(leg)[FOOT_X] += 0.32f * w;
        }
        for (int leg : new int[] {HIND_L, HIND_R}) {
            p.leg(leg)[UPPER_X] -= 0.65f * w;
            p.leg(leg)[SHIN_X] += 1.82f * w;
            p.leg(leg)[FOOT_X] -= 0.87f * w;
        }
        // Schwanz: an der Wurzel hinab zum Boden, dann flach und seitlich um den Körper
        p.tailPitch[0] -= 0.45f * w;
        p.tailPitch[1] += 0.2f * w;
        for (int i = 1; i < this.tails; i++) {
            p.tailYaw[i] += 0.3f * w;
        }
        p.neckBend -= 0.1f * w;
        p.headPitch += 0.35f * w;
    }

    /** Odem: das Maul weit auf, der Hals streckt sich zum Ziel, der Kopf zittert vom Strahl. */
    private void spew(DragonRenderState d, DragonPose p, float a) {
        float e = env(a, 0.0f, 0.2f, 1.55f, 1.95f);
        float air = d.airBlend;
        p.neckBend += (0.3f + 0.15f * air) * e;
        p.headPitch -= 0.3f * e;
        p.jaw += 1.0f * e;
        p.headRoll += Mth.sin(a * 23.0f) * 0.025f * e;
        p.chest *= 1.0f + 0.03f * e;
        p.bodyPitch += 0.04f * e * (1.0f - air);
        p.bodyY += 0.5f * e * (1.0f - air);
        p.flapAmp *= 1.0f - 0.4f * e * air;
    }

    /**
     * Biss: kurz zurück, dann schnellt der Kopf mit offenem Maul vor und schnappt zu – hinab zu einem kleinen Gegner vor
     * den Füßen ({@code dip} -1: der Hals beugt sich tief, die Brust senkt sich), hinauf zu einem fliegenden (+1).
     */
    private void snapBite(DragonPose p, float a, float dip) {
        float back = env(a, 0.0f, 0.07f, 0.09f, 0.16f);
        float lunge = env(a, 0.07f, 0.16f, 0.26f, 0.6f);
        float open = env(a, 0.0f, 0.1f, 0.13f, 0.22f);
        float down = Math.max(0.0f, -dip);
        float up = Math.max(0.0f, dip);
        p.neckBend += -0.15f * back + (0.6f + 0.55f * down - 0.5f * up) * lunge;
        p.headPitch += -0.12f * back + (-0.25f + 0.3f * down - 0.2f * up) * lunge;
        p.jaw += 1.1f * open;
        p.bodyPitch += (0.05f + 0.1f * down - 0.12f * up) * lunge;
        p.bodyY += 1.2f * down * lunge;
        p.headRoll += wobble(a, 0.22f, 3.0f, 6.0f) * 0.06f;
    }
}
