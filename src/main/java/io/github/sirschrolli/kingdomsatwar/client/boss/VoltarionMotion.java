package io.github.sirschrolli.kingdomsatwar.client.boss;

import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.env;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.rise;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.smooth;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FRONT_L;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FRONT_R;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.SHIN_X;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.UPPER_X;

import net.minecraft.util.Mth;

/**
 * Bewegungen von Voltarion, dem Wyvern: Am Boden geht er im Viertakt auf Hinterbeinen und Handgelenken (die
 * Vorderbein-Kanäle der Haltung steuern hier die Flügelarme: Schwung und Ellbogenknick), wachsam und ruckartig wie ein
 * Raubvogel. Kettenblitz: Er lädt sich mit halb gehobenen Flügeln auf, zittert vor Spannung und stößt den Kopf vor.
 * Blitzfeld: breitet die Flügel aus und hält sie bebend gespreizt. Erwachen: Er fährt mit dem Blitz herab (4,0 s),
 * landet tief geduckt mit ausgebreiteten Flügeln, richtet sich auf und brüllt (6,6 s). Einschlafen: Er steigt in den
 * Sturm auf, statt sich hinzulegen.
 */
public class VoltarionMotion extends DragonMotion {
    public VoltarionMotion(int necks, int tails, boolean quadruped, float bodyHeight) {
        super(necks, tails, quadruped, bodyHeight);
    }

    @Override
    protected void idle(BossRenderState s, DragonPose p, float t, float air) {
        super.idle(s, p, t, air);
        // wachsam: ruckartige Kopfbewegungen wie ein Raubvogel, der Schwanz zuckt
        float calm = 1.0f - air;
        float jerk = Mth.sin(t * 0.9f);
        jerk = jerk * jerk * jerk * jerk * jerk;
        p.headYaw += 0.12f * jerk * calm;
        p.headRoll += 0.05f * jerk * calm;
        if (this.tails > 3) {
            p.tailYaw[this.tails - 1] += Mth.sin(t * 5.0f) * 0.04f * calm;
        }
        p.neckBend -= 0.06f * calm;                 // Hals hoch getragen
        p.neckTip += 0.08f * calm;
    }

    @Override
    protected void walk(BossRenderState s, DragonPose p, float ground) {
        super.walk(s, p, ground);
        float speed = Math.min(s.walkAnimationSpeed * 1.4f, 1.0f) * ground;
        if (speed < 0.01f) {
            return;
        }
        float cycle = s.walkAnimationPos * 0.42f / Mth.TWO_PI;
        wrist(p.leg(FRONT_L), cycle, 0.25f, speed);
        wrist(p.leg(FRONT_R), cycle, 0.75f, speed);
        p.neckBend += 0.05f * speed;                // beim Gehen den Hals etwas vorgestreckt
    }

    /**
     * Ein Flügelarm im Schritt: Standphase (Handgelenk am Boden, der Arm schwingt von vorn nach hinten – vorn knickt
     * der Ellbogen etwas nach außen, damit das Handgelenk am Boden bleibt), dann die Schwungphase (Ellbogen knickt ein,
     * das Handgelenk hebt sich und schwingt nach vorn).
     */
    private static void wrist(float[] leg, float cycle, float offset, float speed) {
        float ph = cycle + offset;
        ph -= Mth.floor(ph);
        float swing;
        float lift;
        if (ph < 0.62f) {
            swing = Mth.lerp(ph / 0.62f, -1.0f, 1.0f);
            lift = 0.0f;
        } else {
            float u = (ph - 0.62f) / 0.38f;
            swing = Mth.lerp(smooth(u), 1.0f, -1.0f);
            lift = Mth.sin(u * Mth.PI);
        }
        float amp = 0.26f * speed;
        leg[UPPER_X] += swing * amp;
        leg[SHIN_X] += lift * 0.42f * speed + Math.min(0.0f, swing) * amp * 0.57f;
    }

    @Override
    protected void flight(BossRenderState s, DragonPose p, float air) {
        super.flight(s, p, air);
        // schlanker Flug: Hals und Peitschenschwanz gestreckt
        p.neckBend += 0.08f * air;
        for (int i = 0; i < this.tails; i++) {
            p.tailYaw[i] *= 1.0f - 0.3f * air;
        }
    }

    /** Kettenblitz: aufladen (Flügel halb gehoben, Zittern vor Spannung), bei 1,1 s den Kopf vorstoßen. */
    @Override
    protected void special1(BossRenderState s, DragonPose p, float a) {
        float charge = env(a, 0.0f, 0.5f, 1.05f, 1.15f);
        float strike = env(a, 1.05f, 1.15f, 1.5f, 2.2f);
        p.bodyPitch -= 0.16f * charge;
        p.bodyY -= 2.0f * charge;
        p.neckBend += -0.4f * charge + 0.55f * strike;
        p.headPitch += -0.35f * charge - 0.2f * strike;
        p.jaw += 0.25f * charge + 1.0f * strike;
        p.chest *= 1.0f + 0.06f * charge;
        p.fold *= 1.0f - 0.7f * charge;
        p.raise += 0.5f * charge;
        p.spread += 0.2f * charge;
        p.headRoll += Mth.sin(a * 31.0f) * 0.035f * charge;
        p.bodyRoll += Mth.sin(a * 27.0f) * 0.02f * charge;
    }

    /** Blitzfeld (und Blitzring): ducken, aufspringen, die Flügel bebend gespreizt halten, solange das Feld steht. */
    @Override
    protected void ring(DragonPose p, float a) {
        float crouch = env(a, 0.0f, 0.85f, 0.95f, 1.1f);
        float burst = env(a, 1.0f, 1.12f, 1.5f, 2.0f);
        float hold = rise(a, 1.0f, 1.4f);
        float open = Math.max(burst, hold);
        p.bodyY += 3.0f * crouch - 1.5f * burst - 1.0f * hold;
        p.bodyPitch -= 0.25f * burst + 0.1f * hold;
        p.neckBend += 0.25f * crouch - 0.45f * burst - 0.15f * hold;
        p.headPitch -= 0.3f * burst + 0.1f * hold;
        p.jaw += 0.3f * crouch + 1.0f * burst + 0.25f * hold;
        p.chest *= 1.0f + 0.06f * crouch;
        p.fold *= 1.0f - open;
        p.raise += 0.8f * burst + 0.45f * hold - 0.3f * crouch;
        p.spread += 0.3f * open;
        p.flapAmp = Math.max(p.flapAmp, 0.1f * hold);
        p.headRoll += Mth.sin(a * 23.0f) * 0.02f * hold;
    }

    /**
     * Erwachen: bis 4,0 s kreist er hoch im Sturm (unsichtbar über dem Gipfel), dann fährt er mit dem Blitz herab und
     * landet tief geduckt mit ausgebreiteten Flügeln; Funken zucken durch den Körper. Er richtet sich auf und brüllt
     * bei 6,6 s mit hoch erhobenen Flügeln.
     */
    @Override
    protected void awakening(BossRenderState s, DragonPose p, float time) {
        float sky = 1.0f - rise(time, 3.9f, 4.05f);
        float impact = env(time, 4.0f, 4.1f, 4.7f, 5.6f);
        float rearUp = env(time, 5.4f, 6.2f, 7.6f, 8.6f);
        float roar = env(time, 6.4f, 6.7f, 7.8f, 8.6f);
        float twitch = env(time, 4.0f, 4.2f, 6.0f, 6.6f);
        p.flapAmp = sky;
        p.fold = sky > 0.5f ? 0.0f : Mth.clamp(1.0f - Math.max(impact * 1.2f, roar * 1.1f), 0.0f, 1.0f);
        p.bodyY += 6.0f * impact - 3.0f * rearUp;
        p.bodyPitch += 0.15f * impact - 0.3f * rearUp;
        p.neckBend += 0.5f * impact - 0.3f * rearUp - 0.2f * roar;
        p.neckTip += 0.2f * roar;
        p.headPitch += 0.3f * impact - 0.5f * roar;
        p.jaw += 1.0f * roar + 0.3f * impact;
        p.raise += 0.9f * roar + 0.2f * impact;
        p.spread += 0.3f * Math.max(roar, impact);
        p.headRoll += Mth.sin(time * 31.0f) * 0.05f * twitch + Mth.sin(time * 20.0f) * 0.03f * roar;
        p.bodyRoll += Mth.sin(time * 27.0f) * 0.03f * twitch;
        p.chest *= 1.0f + 0.05f * roar;
    }

    /** Einschlafen: Er duckt sich, breitet die Flügel aus und steigt mit kräftigen Schlägen in den Sturm auf. */
    @Override
    protected void slumber(BossRenderState s, DragonPose p, float time) {
        float crouch = env(time, 0.0f, 0.5f, 0.8f, 1.0f);
        float fly = rise(time, 0.8f, 1.2f);
        p.bodyY += 4.0f * crouch;
        p.fold = 1.0f - fly;
        p.flapAmp = Math.max(p.flapAmp, 1.3f * fly);
        p.raise += 0.5f * crouch;
        p.neckBend -= 0.2f * fly;
    }

    /** Tod: wie die anderen, aber die Flügel breiten sich am Ende flach über den Boden. */
    @Override
    protected void dying(BossRenderState s, DragonPose p, float time) {
        super.dying(s, p, time);
        float fall = rise(time, 1.8f, 3.4f);
        p.fold = Math.min(p.fold, 1.0f - fall);
    }
}
