package io.github.sirschrolli.kingdomsatwar.client.boss;

import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.downstroke;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.env;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.rise;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.smooth;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.strike;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.wobble;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FOOT_X;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FRONT_L;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FRONT_R;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.HIND_L;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.HIND_R;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.SHIN_X;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.UPPER_X;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.UPPER_Z;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import net.minecraft.util.Mth;

/**
 * Was ein Drachen-Boss in jedem Augenblick tun will – die Zielhaltung ({@link DragonPose}) aus Zustand, Bewegung,
 * Angriff und Laune. Die Federn ({@link DragonRig}) machen daraus weiche Bewegungen.
 *
 * <ul>
 *     <li>Ruhe: sichtbares Atmen (schneller, je wütender), Gewicht verlagern, Kopf nie ganz still, Schwanz als Welle</li>
 *     <li>Launen: Schnauben, Kopfschütteln, Schwanzschlagen, Flügel lockern, Umsehen, Gähnen</li>
 *     <li>Gang: Standphase und Schwungphase je Bein (Knie und Fuß beugen sich), Körper senkt sich bei jedem Aufsetzen,
 *     Gewicht pendelt, Wirbelsäule schlängelt, Kopf bleibt ruhig, Schwanz schwingt gegen</li>
 *     <li>Flug: kräftiger Abschlag hebt den Körper, Kopf bleibt ruhig, Schwanz folgt Steig- und Sinkflug und steuert in
 *     Kurven, Beine angezogen</li>
 *     <li>Angriffe mit Ausholen, Ausführen und Nachschwingen; Erwachen, Einschlafen und Tod</li>
 * </ul>
 */
public class DragonMotion {
    private static final float DEG = Mth.DEG_TO_RAD;

    protected final int necks;
    protected final int tails;
    protected final boolean quadruped;
    protected final float bodyHeight;

    public DragonMotion(int necks, int tails, boolean quadruped, float bodyHeight) {
        this.necks = necks;
        this.tails = tails;
        this.quadruped = quadruped;
        this.bodyHeight = bodyHeight;
    }

    public void pose(BossRenderState s, DragonPose p) {
        p.reset();
        float t = s.ageInTicks / 20.0f;
        float air = s.airBlend;
        float ground = 1.0f - air;
        p.fold = ground;
        p.flapAmp = air;
        p.flap = s.flapPhase;
        this.idle(s, p, t, air);
        this.look(s, p);
        if (air > 0.01f) {
            this.flight(s, p, air);
        }
        if (ground > 0.01f) {
            this.walk(s, p, ground);
        }
        this.mood(s, p);
        this.attack(s, p);
        switch (s.bossState) {
            case AWAKENING -> this.awakening(s, p, s.stateTime);
            case SLUMBER -> this.slumber(s, p, s.stateTime);
            default -> {
            }
        }
        if (s.deathSeconds > 0) {
            this.dying(s, p, s.deathSeconds);
        }
    }

    // =====================================================================================
    // Ruhe, Blick, Gang, Flug
    // =====================================================================================

    protected void idle(BossRenderState s, DragonPose p, float t, float air) {
        float rate = s.phase == 3 ? 2.8f : s.phase == 2 ? 2.2f : 1.75f;
        float breath = Mth.sin(t * rate);
        p.chest *= 1.0f + 0.032f * breath;
        p.bodyY -= 0.35f * breath * (1.0f - air);
        p.neckBend -= 0.035f * breath;
        p.headPitch += 0.025f * breath;
        float gape = Mth.sin(t * 0.37f);
        if (gape > 0) {
            p.jaw += gape * gape * gape * 0.08f;          // ab und zu ein Spalt im Maul
        }
        float calm = 1.0f - air;
        p.bodyRoll += 0.032f * Mth.sin(t * 0.21f + 1.3f) * calm;     // Gewicht verlagern
        p.bodyYaw += 0.025f * Mth.sin(t * 0.16f) * calm;
        p.headYaw += 0.05f * Mth.sin(t * 0.47f) + 0.02f * Mth.sin(t * 1.31f + 2.0f);
        p.headPitch += 0.03f * Mth.sin(t * 0.63f + 0.4f);
        p.headRoll += 0.025f * Mth.sin(t * 0.39f + 1.1f);
        float w = 1.0f - 0.6f * air;
        for (int i = 0; i < this.tails; i++) {
            p.tailYaw[i] += Mth.sin(t * 0.85f - i * 0.5f) * (0.03f + i * 0.01f) * w;
            p.tailPitch[i] += Mth.sin(t * 0.62f - i * 0.45f) * 0.018f * w;
        }
        if (this.tails > 2) {
            p.tailPitch[this.tails - 1] += 0.08f * w;
            p.tailPitch[this.tails - 2] += 0.04f * w;
        }
    }

    protected void look(BossRenderState s, DragonPose p) {
        float yaw = Mth.clamp(s.yRot, -75.0f, 75.0f) * DEG;
        float pitch = Mth.clamp(s.xRot, -50.0f, 60.0f) * DEG;
        p.neckYaw += yaw * 0.8f;
        p.neckRoll -= yaw * 0.12f;
        p.headYaw += yaw * 0.2f;
        p.neckBend += pitch * 0.4f;
        p.headPitch += pitch * 0.4f;
    }

    protected void walk(BossRenderState s, DragonPose p, float ground) {
        float speed = Math.min(s.walkAnimationSpeed * 1.4f, 1.0f) * ground;
        if (speed < 0.01f) {
            return;
        }
        float cycle = s.walkAnimationPos * 0.42f / Mth.TWO_PI;
        if (this.quadruped) {
            this.gait(p.leg(HIND_L), cycle, 0.0f, speed, false);
            this.gait(p.leg(FRONT_L), cycle, 0.25f, speed, true);
            this.gait(p.leg(HIND_R), cycle, 0.5f, speed, false);
            this.gait(p.leg(FRONT_R), cycle, 0.75f, speed, true);
        } else {
            this.gait(p.leg(HIND_L), cycle, 0.0f, speed * 1.2f, false);
            this.gait(p.leg(HIND_R), cycle, 0.5f, speed * 1.2f, false);
        }
        float c = cycle * Mth.TWO_PI;
        float bob = 0.5f - 0.5f * Mth.cos(c * 2.0f);
        p.bodyY += 0.9f * bob * speed;
        p.bodyRoll += 0.05f * Mth.sin(c) * speed;
        p.bodyYaw += 0.035f * Mth.sin(c + 0.6f) * speed;
        p.bodyPitch += 0.015f * Mth.sin(c * 2.0f + 0.8f) * speed;
        p.neckBend -= 0.05f * bob * speed;
        p.headPitch += 0.04f * bob * speed;
        p.headYaw -= 0.03f * Mth.sin(c + 0.6f) * speed;
        for (int i = 0; i < this.tails; i++) {
            p.tailYaw[i] += Mth.sin(c - 0.8f - i * 0.55f) * (0.035f + i * 0.012f) * speed;
        }
    }

    /**
     * Ein Bein im Schritt: 62 % Standphase (Fuß am Boden, das Bein wandert von vorn nach hinten), dann die Schwungphase
     * (Bein vorschwingen, Knie und Fuß gebeugt). Positiver Winkel am Oberschenkel = Fuß nach hinten.
     */
    private void gait(float[] leg, float cycle, float offset, float speed, boolean front) {
        float ph = cycle + offset;
        ph -= Mth.floor(ph);
        float amp = (front ? 0.36f : 0.42f) * speed;
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
        leg[UPPER_X] += swing * amp;
        if (front) {
            leg[SHIN_X] += lift * 0.95f * speed;
            leg[FOOT_X] += -lift * 0.6f * speed + swing * 0.1f * speed;
        } else {
            leg[SHIN_X] -= lift * 0.75f * speed;
            leg[FOOT_X] += lift * 0.85f * speed - swing * 0.08f * speed;
        }
    }

    protected void flight(BossRenderState s, DragonPose p, float air) {
        boolean gliding = s.flightPitch < -8.0f;
        p.flapAmp = air * (gliding ? 0.12f : 1.0f);
        float down = downstroke(s.flapPhase) - 0.36f;      // > 0 im Abschlag
        p.bodyY -= 1.4f * down * p.flapAmp;
        p.bodyPitch += 0.05f * down * p.flapAmp;
        // Beine angezogen, baumeln ein wenig mit
        float dangle = Mth.sin(s.flapPhase - 1.6f) * 0.05f * p.flapAmp;
        if (this.quadruped) {
            for (int leg : new int[] {FRONT_L, FRONT_R}) {
                p.leg(leg)[UPPER_X] -= 0.85f * air;
                p.leg(leg)[SHIN_X] += 1.5f * air;
                p.leg(leg)[FOOT_X] += 0.4f * air;
            }
        }
        for (int leg : new int[] {HIND_L, HIND_R}) {
            p.leg(leg)[UPPER_X] += 1.15f * air + dangle;
            p.leg(leg)[SHIN_X] += 0.25f * air;
            p.leg(leg)[FOOT_X] += 0.7f * air + dangle;
        }
        // Hals gestreckt, Kopf ruhig gegen das Auf und Ab
        p.neckBend += 0.5f * air + 0.06f * down * p.flapAmp;
        p.neckTip -= 0.08f * air;
        p.headPitch -= 0.42f * air + 0.06f * down * p.flapAmp;
        // Schwanz: gestreckt, Welle mit dem Schlag, folgt Steig- und Sinkflug, steuert in Kurven
        if (this.tails > 1) {
            p.tailPitch[0] += 0.16f * air;
            p.tailPitch[1] += 0.06f * air;
        }
        float bank = s.bank * DEG;
        for (int i = 0; i < this.tails; i++) {
            p.tailPitch[i] += Mth.sin(s.flapPhase - 1.3f - i * 0.55f) * 0.04f * p.flapAmp;
            p.tailPitch[i] -= s.flightPitch * DEG * 0.05f * air;
            p.tailYaw[i] -= bank * 0.05f * air;
        }
        if (gliding) {
            p.spread += 0.15f * air;
        }
    }

    // =====================================================================================
    // Launen (nur in ruhigen Momenten)
    // =====================================================================================

    protected void mood(BossRenderState s, DragonPose p) {
        float a = s.idleTime;
        switch (s.idleEvent) {
            case BossAnims.MOOD_SNORT -> {
                float up = env(a, 0.0f, 0.12f, 0.2f, 0.4f);
                float down = env(a, 0.3f, 0.42f, 0.55f, 0.95f);
                p.headPitch += -0.28f * up + 0.16f * down;
                p.neckBend -= 0.08f * up;
                p.jaw += 0.22f * env(a, 0.1f, 0.18f, 0.3f, 0.45f);
                p.headRoll += wobble(a, 0.35f, 4.0f, 5.0f) * 0.06f;
            }
            case BossAnims.MOOD_SHAKE -> {
                float e = env(a, 0.0f, 0.15f, 0.8f, 1.2f);
                p.headRoll += Mth.sin(a * 26.0f) * 0.14f * e;
                p.neckYaw += Mth.sin(a * 26.0f + 1.2f) * 0.07f * e;
                p.jaw += 0.1f * e;
            }
            case BossAnims.MOOD_TAIL_FLICK -> {
                float e = env(a, 0.0f, 0.1f, 0.6f, 1.0f);
                int half = this.tails / 2;
                for (int i = half; i < this.tails; i++) {
                    p.tailYaw[i] += Mth.sin(a * 9.0f - i * 0.7f) * 0.13f * e * (i - half + 1.0f) / Math.max(1, this.tails - half);
                }
            }
            case BossAnims.MOOD_SHRUG -> {
                float e = env(a, 0.0f, 0.5f, 0.9f, 1.6f);
                p.fold = Math.min(p.fold, 1.0f - 0.45f * e);
                p.raise += 0.35f * e;
                p.bodyPitch -= 0.04f * e;
            }
            case BossAnims.MOOD_GLANCE -> {
                float e = env(a, 0.0f, 0.35f, 1.8f, 2.5f);
                p.headYaw += 0.4f * s.idleSide * e;
                p.neckYaw += 0.35f * s.idleSide * e;
                p.headRoll += 0.08f * s.idleSide * e;
            }
            case BossAnims.MOOD_YAWN -> {
                float e = env(a, 0.0f, 0.6f, 1.4f, 2.2f);
                p.neckBend -= 0.3f * e;
                p.neckTip += 0.15f * e;
                p.headPitch -= 0.35f * e;
                p.jaw += 1.0f * env(a, 0.3f, 0.8f, 1.3f, 1.9f);
                p.chest *= 1.0f + 0.05f * e;
                p.fold = Math.min(p.fold, 1.0f - 0.25f * e);
            }
            default -> {
            }
        }
    }

    // =====================================================================================
    // Angriffe
    // =====================================================================================

    protected void attack(BossRenderState s, DragonPose p) {
        float a = s.attackTime;
        switch (s.attack) {
            case BossAnims.BITE -> this.bite(p, a);
            case BossAnims.CLAW -> this.claw(p, a);
            case BossAnims.TAIL_SWEEP -> this.tailSweep(p, a);
            case BossAnims.WING_BUFFET -> this.wingBuffet(p, a);
            case BossAnims.ROAR -> this.roar(p, a);
            case BossAnims.STOMP -> this.stomp(p, a);
            case BossAnims.TAKE_OFF -> this.takeOff(p, a);
            case BossAnims.LAND -> this.land(s, p, a);
            case BossAnims.BREATH -> this.breath(p, a);
            case BossAnims.AIR_BREATH -> this.airBreath(p, a);
            case BossAnims.VOLLEY -> this.volley(p, a);
            case BossAnims.RAIN -> this.rain(p, a);
            case BossAnims.RING -> this.ring(p, a);
            case BossAnims.ERUPT -> this.erupt(p, a);
            default -> {
            }
        }
    }

    protected void bite(DragonPose p, float a) {
        float back = env(a, 0.0f, 0.35f, 0.45f, 0.6f);
        float lunge = env(a, 0.5f, 0.6f, 0.7f, 1.15f);
        float open = env(a, 0.25f, 0.5f, 0.55f, 0.62f);
        p.neckBend += -0.34f * back + 0.6f * lunge;
        p.headPitch += -0.25f * back - 0.25f * lunge;
        p.jaw += 1.0f * open;
        p.bodyPitch += 0.07f * lunge - 0.03f * back;
        p.bodyY -= 0.8f * back;
        p.headRoll += wobble(a, 0.62f, 3.0f, 6.0f) * 0.06f;
    }

    protected void claw(DragonPose p, float a) {
        float raise = env(a, 0.0f, 0.45f, 0.5f, 0.62f);
        float swipe = env(a, 0.5f, 0.66f, 0.75f, 1.25f);
        p.bodyPitch -= 0.12f * raise;
        p.bodyY -= 1.5f * raise;
        p.bodyRoll -= 0.06f * raise - 0.08f * swipe;
        if (this.quadruped) {
            float[] leg = p.leg(FRONT_R);
            leg[UPPER_X] += -1.5f * raise + 0.45f * swipe;
            leg[UPPER_Z] += -0.35f * raise + 0.55f * swipe;
            leg[SHIN_X] += 0.9f * raise - 0.3f * swipe;
            leg[FOOT_X] -= 0.5f * raise;
        }
        p.neckYaw -= 0.15f * swipe;
        p.headPitch += 0.15f * swipe;
        p.jaw += 0.4f * env(a, 0.4f, 0.6f, 0.7f, 1.0f);
    }

    protected void tailSweep(DragonPose p, float a) {
        float coil = env(a, 0.0f, 0.4f, 0.5f, 0.6f);
        float whip = env(a, 0.5f, 0.65f, 1.15f, 1.6f);
        p.bodyY += 1.5f * coil;
        for (int i = 0; i < this.tails; i++) {
            p.tailPitch[i] -= 0.06f * coil;
            p.tailYaw[i] += (0.1f * coil - 0.05f * whip) * (i + 1) * 0.3f;
        }
        p.neckBend += 0.12f * whip;
        p.jaw += 0.35f * whip;
    }

    protected void wingBuffet(DragonPose p, float a) {
        float open = env(a, 0.0f, 0.55f, 0.95f, 1.5f);
        float beat = env(a, 0.65f, 0.8f, 0.85f, 1.1f);
        float up = env(a, 0.3f, 0.6f, 0.65f, 0.8f);
        p.fold *= 1.0f - open;
        p.raise += 0.9f * up - 0.55f * beat;
        p.sweep -= 0.5f * beat;
        p.bodyPitch -= 0.18f * open;
        p.bodyY -= 1.2f * open;
        p.neckBend -= 0.2f * open;
        p.jaw += 0.5f * beat;
    }

    protected void roar(DragonPose p, float a) {
        float e = env(a, 0.15f, 0.6f, 1.6f, 2.2f);
        p.neckBend -= 0.3f * e;
        p.neckTip += 0.18f * e;
        p.headPitch -= 0.5f * e;
        p.jaw += 1.0f * e;
        p.headRoll += Mth.sin(a * 20.0f) * 0.035f * e;
        p.bodyPitch -= 0.1f * e;
        p.chest *= 1.0f + 0.04f * e;
        p.fold *= 1.0f - 0.65f * e;
        p.raise += 0.35f * e;
        p.spread += 0.2f * e;
    }

    protected void stomp(DragonPose p, float a) {
        float rear = env(a, 0.0f, 0.75f, 0.85f, 1.02f);
        float slam = strike(a, 1.0f, 1.6f);
        p.bodyPitch -= 0.55f * rear - 0.08f * slam;
        p.bodyY -= 4.0f * rear;
        if (this.quadruped) {
            for (int leg : new int[] {FRONT_L, FRONT_R}) {
                p.leg(leg)[UPPER_X] -= 0.9f * rear;
                p.leg(leg)[SHIN_X] += 0.8f * rear;
            }
        }
        p.leg(HIND_L)[UPPER_X] -= 0.35f * rear;
        p.leg(HIND_R)[UPPER_X] -= 0.35f * rear;
        p.neckBend += -0.25f * rear + 0.3f * slam;
        p.jaw += 0.6f * env(a, 0.5f, 0.9f, 1.1f, 1.5f);
        p.fold *= 1.0f - 0.6f * rear;
        p.raise += 0.6f * rear;
    }

    protected void takeOff(DragonPose p, float a) {
        float crouch = env(a, 0.0f, 0.4f, 0.48f, 0.6f);
        float leap = env(a, 0.48f, 0.6f, 0.8f, 1.4f);
        p.bodyY += 4.0f * crouch;
        for (int leg : this.legs()) {
            boolean front = leg == FRONT_L || leg == FRONT_R;
            p.leg(leg)[UPPER_X] -= 0.25f * crouch;
            p.leg(leg)[SHIN_X] += (front ? 0.6f : -0.6f) * crouch;
            p.leg(leg)[FOOT_X] += 0.3f * crouch;
        }
        p.bodyPitch -= 0.25f * leap;
        p.neckBend -= 0.15f * crouch - 0.2f * leap;
        p.fold *= 1.0f - rise(a, 0.2f, 0.6f);
        p.raise += 0.6f * crouch;
        p.flapAmp = Math.max(p.flapAmp, 1.2f * leap);
    }

    protected void land(BossRenderState s, DragonPose p, float a) {
        float near = 1.0f - s.airBlend;
        p.bodyPitch -= 0.35f * s.airBlend;
        if (this.quadruped) {
            p.leg(FRONT_L)[UPPER_X] -= 0.5f * s.airBlend;
            p.leg(FRONT_R)[UPPER_X] -= 0.5f * s.airBlend;
        }
        p.sweep -= 0.35f * s.airBlend;
        p.flapAmp = Math.max(p.flapAmp, 0.9f * s.airBlend);
        float touch = env(a, 0.0f, 0.1f, 0.4f, 1.0f);
        p.bodyY += 2.0f * near * touch;
        p.neckBend += 0.15f * near * touch;
    }

    protected void breath(DragonPose p, float a) {
        float charge = env(a, 0.0f, 0.8f, 0.9f, 1.05f);
        float spew = env(a, 0.9f, 1.05f, 3.0f, 3.4f);
        p.neckBend += -0.35f * charge + 0.35f * spew;
        p.headPitch += -0.3f * charge - 0.25f * spew;
        p.jaw += 0.35f * charge + 0.95f * spew;
        p.chest *= 1.0f + 0.06f * charge;
        p.bodyY -= 1.0f * charge;
        p.headRoll += Mth.sin(a * 23.0f) * 0.02f * spew;
        p.neckYaw += Mth.sin(a * 1.7f) * 0.12f * spew;     // schwenkt den Strahl
    }

    protected void airBreath(DragonPose p, float a) {
        float spew = env(a, 1.4f, 1.6f, 3.4f, 3.7f);
        p.neckBend += 0.45f * spew;
        p.headPitch += 0.15f * spew;
        p.jaw += 0.9f * spew;
        p.flapAmp *= 1.0f - 0.7f * spew;
        p.sweep -= 0.15f * spew;
    }

    protected void volley(DragonPose p, float a) {
        float kick = 0.0f;
        for (int i = 0; i < 6; i++) {
            kick = Math.max(kick, strike(a, 0.7f + i * 0.4f, 1.0f + i * 0.4f));
        }
        p.neckBend += -0.15f + 0.3f * kick;
        p.headPitch -= 0.15f * kick;
        p.jaw += 0.2f + 0.7f * kick;
        p.chest *= 1.0f + 0.03f * kick;
    }

    protected void rain(DragonPose p, float a) {
        float up = env(a, 0.0f, 0.8f, 4.6f, 5.4f);
        float roar = env(a, 0.3f, 0.6f, 1.4f, 2.0f);
        p.bodyPitch -= 0.5f * up;
        p.neckBend += -0.5f * roar + 0.2f * up * (1 - roar);
        p.headPitch -= 0.6f * roar;
        p.jaw += 1.0f * roar;
        p.flapAmp = Math.max(p.flapAmp, 1.1f * up);
        p.sweep -= 0.25f * up;
        for (int leg : this.legs()) {
            p.leg(leg)[UPPER_X] -= 0.3f * up;
        }
    }

    protected void ring(DragonPose p, float a) {
        float crouch = env(a, 0.0f, 0.85f, 0.95f, 1.05f);
        float burst = env(a, 1.0f, 1.12f, 1.6f, 2.3f);
        p.bodyY += 3.0f * crouch - 2.0f * burst;
        p.bodyPitch -= 0.3f * burst;
        p.neckBend += 0.25f * crouch - 0.45f * burst;
        p.headPitch -= 0.3f * burst;
        p.jaw += 0.3f * crouch + 1.0f * burst;
        p.chest *= 1.0f + 0.06f * crouch;
        p.fold *= 1.0f - burst;
        p.raise += 0.8f * burst - 0.3f * crouch;
        p.spread += 0.3f * burst;
    }

    protected void erupt(DragonPose p, float a) {
        float rear = env(a, 0.0f, 0.45f, 0.5f, 0.62f);
        float slam = strike(a, 0.6f, 1.3f);
        p.bodyPitch -= 0.35f * rear - 0.08f * slam;
        p.bodyY -= 2.5f * rear;
        if (this.quadruped) {
            p.leg(FRONT_L)[UPPER_X] -= 0.7f * rear;
            p.leg(FRONT_R)[UPPER_X] -= 0.7f * rear;
        }
        p.neckBend += -0.2f * rear + 0.45f * slam;
        p.jaw += 0.5f * slam;
    }

    // =====================================================================================
    // Zustände
    // =====================================================================================

    /** Erwachen: Kopf hoch, Flügel eng – dann breitet der Drache beim Brüllen die Flügel aus (Brüllen bei 6,6 s). */
    protected void awakening(BossRenderState s, DragonPose p, float time) {
        float emerge = env(time, 1.5f, 3.0f, 5.6f, 6.6f);
        float roar = env(time, 6.3f, 6.7f, 7.6f, 8.4f);
        float shake = env(time, 0.4f, 1.0f, 2.6f, 3.4f);     // schüttelt Lava und Gestein ab
        p.bodyPitch -= 0.14f * emerge + 0.08f * roar;
        p.bodyRoll += Mth.sin(time * 7.0f) * 0.05f * shake;
        p.neckBend -= 0.16f * emerge + 0.26f * roar;
        p.neckTip += 0.25f * emerge + 0.2f * roar;
        p.headPitch -= 0.1f * emerge + 0.5f * roar;
        p.headRoll += Mth.sin(time * 20.0f) * 0.03f * roar + Mth.sin(time * 9.0f) * 0.08f * shake;
        p.jaw += 1.0f * roar + 0.15f * emerge;
        p.chest *= 1.0f + 0.05f * roar;
        p.fold = Math.max(0.0f, 1.0f - roar * 1.1f);
        p.raise += 0.8f * roar;
        p.spread += 0.25f * roar;
        p.flapAmp = 0.0f;
    }

    protected void slumber(BossRenderState s, DragonPose p, float time) {
        float down = rise(time, 0.0f, 2.2f);
        p.bodyY += this.bodyHeight * 0.45f * down;
        for (int leg : this.legs()) {
            boolean front = leg == FRONT_L || leg == FRONT_R;
            p.leg(leg)[UPPER_X] -= (front ? 0.9f : 1.2f) * down;
            p.leg(leg)[SHIN_X] += (front ? 1.4f : 1.9f) * down;
            p.leg(leg)[FOOT_X] -= 0.4f * down;
        }
        p.neckBend += 0.5f * down;
        p.headPitch += 0.2f * down;
        for (int i = 0; i < this.tails; i++) {
            p.tailYaw[i] += 0.12f * down;
        }
        p.fold = 1.0f;
    }

    /** Tod: Aufbäumen und Um-sich-Schlagen, dann knicken die Beine ein, Kopf und Flügel sinken zu Boden. */
    protected void dying(BossRenderState s, DragonPose p, float time) {
        float thrash = env(time, 0.0f, 0.3f, 1.6f, 2.2f);
        float fall = rise(time, 1.8f, 3.4f);
        float shake = Mth.sin(time * 17.0f) * thrash;
        p.neckBend += -0.5f * thrash * Mth.abs(Mth.sin(time * 3.0f)) + 0.65f * fall;
        p.neckYaw += shake * 0.25f;
        p.headPitch += 0.3f * fall - 0.3f * thrash;
        p.headRoll += shake * 0.15f + 0.35f * fall;
        p.jaw += 0.8f * thrash + 0.35f * fall;
        p.bodyY += this.bodyHeight * 0.55f * fall;
        p.bodyRoll += 0.12f * fall;
        for (int leg : this.legs()) {
            boolean front = leg == FRONT_L || leg == FRONT_R;
            float side = leg == FRONT_L || leg == HIND_L ? 1.0f : -1.0f;
            p.leg(leg)[UPPER_X] -= (front ? 1.0f : 1.3f) * fall - shake * 0.2f * (1 - fall);
            p.leg(leg)[UPPER_Z] += side * 0.35f * fall;
            p.leg(leg)[SHIN_X] += (front ? 1.5f : 2.0f) * fall;
        }
        for (int i = 0; i < this.tails; i++) {
            p.tailYaw[i] += Mth.sin(time * 6.0f - i * 0.7f) * 0.18f * thrash + 0.08f * fall;
            p.tailPitch[i] += 0.06f * fall;
        }
        p.headPitch += Mth.sin(time * 9.0f) * env(time, 4.0f, 4.2f, 4.6f, 5.0f) * 0.1f;
        p.fold = Math.min(p.fold, 1.0f - 0.6f * Math.max(thrash, fall));
        p.flapAmp = 0.7f * thrash;
        p.flap = time * 9.0f;
        p.raise -= 0.45f * fall;
        p.spread += 0.15f * fall;
    }

    protected int[] legs() {
        return this.quadruped ? new int[] {FRONT_L, FRONT_R, HIND_L, HIND_R} : new int[] {HIND_L, HIND_R};
    }
}
