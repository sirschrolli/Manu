package io.github.sirschrolli.kingdomsatwar.client.boss;

import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.env;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.rise;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.strike;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.wobble;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * Animiertes Modell eines Drachen-Bosses mit Hals- und Schwanzkette, vier (oder zwei) Beinen und Fledermausflügeln.
 *
 * <p>Alles wird prozedural berechnet und als Zusatz auf die Grundhaltung der Geometrie gelegt:
 * <ul>
 *     <li>Atmen, Umsehen, Hals führt in Kurven, Schwanz schwingt verzögert nach (aus der Drehung der letzten Ticks)</li>
 *     <li>Gang mit diagonalen Beinpaaren, Körper wippt und rollt, Kopf bleibt ruhig</li>
 *     <li>Flügelschlag: kräftiger Abschlag, eingeklappter Aufschlag, Gleiten im Sinkflug; Flügel an- und ablegen</li>
 *     <li>jeder Angriff mit Ausholen, Ausführen und Nachschwingen; Erwachen, Einschlafen und Tod</li>
 * </ul>
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
    protected final Leg hindL;
    protected final Leg hindR;
    protected final @Nullable Wing wingL;
    protected final @Nullable Wing wingR;
    /** Höhe des Rumpfes über dem Boden in Modell-Einheiten (für Hinlegen und Zusammenbrechen). */
    protected final float bodyHeight;

    public DragonBossModel(ModelPart root, int necks, int tails, boolean quadruped, int fingers, float bodyHeight) {
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
        this.frontL = quadruped ? leg(part, "front", "l", 1) : null;
        this.frontR = quadruped ? leg(part, "front", "r", -1) : null;
        this.hindL = leg(part, "hind", "l", 1);
        this.hindR = leg(part, "hind", "r", -1);
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

    // =====================================================================================
    // Hauptablauf
    // =====================================================================================

    @Override
    public void setupAnim(BossRenderState s) {
        super.setupAnim(s);
        float t = s.ageInTicks / 20.0f;
        float air = s.airBlend;
        float ground = 1.0f - air;
        WingPose wp = new WingPose();
        wp.fold = ground;
        wp.flapAmp = air;
        wp.flap = s.flapPhase;

        this.idle(s, t, air);
        this.lookAround(s);
        if (air > 0.01f) {
            this.flight(s, t, air, wp);
        }
        if (ground > 0.01f) {
            this.walk(s, ground);
        }
        this.tailFollow(s, air);
        this.attack(s, t, wp);
        switch (s.bossState) {
            case AWAKENING -> this.awakening(s, s.stateTime, wp);
            case SLUMBER -> this.slumber(s, s.stateTime, wp);
            default -> {
            }
        }
        if (s.deathSeconds > 0) {
            this.dying(s, s.deathSeconds, wp);
        }
        this.applyWings(wp);
    }

    // =====================================================================================
    // Grundbewegungen
    // =====================================================================================

    protected void idle(BossRenderState s, float t, float air) {
        float breath = Mth.sin(t * 1.6f);
        float chest = 1.0f + 0.014f * breath;
        this.body.xScale *= chest;
        this.body.yScale *= 1.0f + 0.01f * breath;
        this.neck[0].xRot += breath * 0.025f;
        this.head.xRot -= breath * 0.02f;
        this.jaw.xRot += Math.max(0.0f, Mth.sin(t * 0.35f)) * 0.04f;     // ab und zu ein Spalt im Maul
        // Schwanz pendelt träge; am Ende ein wenig angehoben
        for (int i = 0; i < this.tail.length; i++) {
            float w = (1.0f - 0.6f * air);
            this.tail[i].yRot += Mth.sin(t * 0.9f - i * 0.55f) * (0.018f + i * 0.007f) * w;
            this.tail[i].xRot += Mth.sin(t * 0.7f - i * 0.4f) * 0.012f * w;
        }
    }

    protected void lookAround(BossRenderState s) {
        float yaw = Mth.clamp(s.yRot, -75.0f, 75.0f) * DEG;
        float pitch = Mth.clamp(s.xRot, -50.0f, 60.0f) * DEG;
        float[] share = this.neck.length >= 4 ? new float[] {0.14f, 0.2f, 0.24f, 0.2f} : new float[] {0.25f, 0.3f, 0.25f};
        for (int i = 0; i < this.neck.length; i++) {
            float sh = i < share.length ? share[i] : 0.15f;
            this.neck[i].yRot += yaw * sh;
            this.neck[i].zRot -= yaw * sh * 0.15f;         // Hals legt sich leicht in die Drehung
        }
        this.head.yRot += yaw * 0.2f;
        if (this.neck.length > 2) {
            this.neck[1].xRot += pitch * 0.18f;
            this.neck[2].xRot += pitch * 0.22f;
        }
        this.head.xRot += pitch * 0.4f;
    }

    /** Gang: diagonale Beinpaare, Körper wippt und rollt, Kopf und Hals gleichen aus. */
    protected void walk(BossRenderState s, float ground) {
        float speed = Math.min(s.walkAnimationSpeed * 1.4f, 1.0f) * ground;
        if (speed < 0.01f) {
            return;
        }
        float pos = s.walkAnimationPos * 0.42f;
        if (this.frontL != null && this.frontR != null) {
            this.stride(this.frontL, pos, 0.0f, speed);
            this.stride(this.hindR, pos, 0.25f, speed);
            this.stride(this.frontR, pos, 0.5f, speed);
            this.stride(this.hindL, pos, 0.75f, speed);
        } else {
            this.stride(this.hindL, pos, 0.0f, speed * 1.2f);
            this.stride(this.hindR, pos, 0.5f, speed * 1.2f);
        }
        float bob = Mth.abs(Mth.sin(pos * 2.0f));
        this.body.y += bob * 0.9f * speed;
        this.body.zRot += Mth.sin(pos) * 0.035f * speed;
        this.body.xRot += Mth.sin(pos * 2.0f) * 0.012f * speed;
        this.neck[0].xRot -= bob * 0.04f * speed;
        this.head.xRot += bob * 0.03f * speed;
        for (int i = 0; i < this.tail.length; i++) {
            this.tail[i].yRot += Mth.sin(pos - i * 0.6f) * (0.03f + i * 0.012f) * speed;
        }
    }

    private void stride(Leg leg, float pos, float phase, float speed) {
        float a = pos + phase * Mth.TWO_PI;
        float swing = Mth.cos(a);
        float lift = Math.max(0.0f, Mth.sin(a));
        leg.upper().xRot += swing * 0.42f * speed;
        if (leg.front()) {
            leg.shin().xRot += lift * 0.75f * speed;
            leg.foot().xRot -= lift * 0.55f * speed - swing * 0.12f * speed;
        } else {
            leg.shin().xRot -= lift * 0.55f * speed;
            leg.foot().xRot += lift * 0.7f * speed;
        }
    }

    /** Flug: Beine angezogen, Hals gestreckt, Körper hebt und senkt sich mit dem Flügelschlag. */
    protected void flight(BossRenderState s, float t, float air, WingPose wp) {
        boolean gliding = s.flightPitch < -8.0f;
        wp.flapAmp = air * (gliding ? 0.18f : 1.0f);
        float f = Mth.sin(s.flapPhase);
        this.body.y += f * 1.4f * wp.flapAmp;
        if (this.frontL != null && this.frontR != null) {
            for (Leg leg : new Leg[] {this.frontL, this.frontR}) {
                leg.upper().xRot -= 0.85f * air;
                leg.shin().xRot += 1.5f * air;
                leg.foot().xRot += 0.4f * air;
            }
        }
        for (Leg leg : new Leg[] {this.hindL, this.hindR}) {
            leg.upper().xRot += 1.15f * air;
            leg.shin().xRot += 0.25f * air;
            leg.foot().xRot += 0.7f * air;
        }
        this.neck[0].xRot += 0.55f * air;
        if (this.neck.length > 1) {
            this.neck[1].xRot += 0.1f * air;
        }
        this.head.xRot -= 0.45f * air;
        this.tail[0].xRot += 0.18f * air;
        this.tail[1].xRot += 0.08f * air;
        for (int i = 0; i < this.tail.length; i++) {
            this.tail[i].xRot += Mth.sin(s.flapPhase - 1.2f - i * 0.5f) * 0.03f * wp.flapAmp;
        }
    }

    /** Schwanz (und ein wenig der Hals) folgen der Drehung der letzten Ticks – wie ein echter, schwerer Schwanz. */
    protected void tailFollow(BossRenderState s, float air) {
        float prev = 0.0f;
        for (int i = 0; i < this.tail.length; i++) {
            int k = Math.min(s.yawLag.length - 1, (i + 1) * 3);
            float cumulative = Mth.clamp(s.yawLag[k] * DEG * 0.85f, -1.6f, 1.6f);
            float local = Mth.clamp(cumulative - prev, -0.32f, 0.32f);
            this.tail[i].yRot += local;
            prev += local;
        }
        // Der Hals führt in die Drehung hinein (kleiner Vorgriff)
        float lead = Mth.clamp(-s.yawLag[4] * DEG * 0.5f, -0.4f, 0.4f);
        for (ModelPart n : this.neck) {
            n.yRot += lead / this.neck.length;
        }
    }

    // =====================================================================================
    // Angriffe
    // =====================================================================================

    protected void attack(BossRenderState s, float t, WingPose wp) {
        float a = s.attackTime;
        switch (s.attack) {
            case BossAnims.BITE -> this.bite(a);
            case BossAnims.CLAW -> this.claw(a);
            case BossAnims.TAIL_SWEEP -> this.tailSweep(a);
            case BossAnims.WING_BUFFET -> this.wingBuffet(a, wp);
            case BossAnims.ROAR -> this.roar(a, wp, 0.0f);
            case BossAnims.STOMP -> this.stomp(a, wp);
            case BossAnims.TAKE_OFF -> this.takeOff(a, wp);
            case BossAnims.LAND -> this.land(s, a, wp);
            case BossAnims.BREATH -> this.breath(a);
            case BossAnims.AIR_BREATH -> this.airBreath(a, wp);
            case BossAnims.VOLLEY -> this.volley(a);
            case BossAnims.RAIN -> this.rain(s, a, wp);
            case BossAnims.RING -> this.ring(a, wp);
            case BossAnims.ERUPT -> this.erupt(a);
            default -> {
            }
        }
    }

    protected void bite(float a) {
        float back = env(a, 0.0f, 0.35f, 0.45f, 0.6f);
        float lunge = env(a, 0.5f, 0.62f, 0.7f, 1.15f);
        float open = env(a, 0.25f, 0.5f, 0.55f, 0.63f);
        this.neckBend(-0.32f * back + 0.55f * lunge);
        this.head.xRot += -0.25f * back - 0.25f * lunge;
        this.jaw.xRot += 0.95f * open;
        this.body.xRot += 0.06f * lunge;
        this.head.zRot += wobble(a, 0.62f, 3.0f, 6.0f) * 0.05f;
    }

    protected void claw(float a) {
        float raise = env(a, 0.0f, 0.45f, 0.5f, 0.62f);
        float swipe = env(a, 0.5f, 0.66f, 0.75f, 1.25f);
        this.body.xRot -= 0.12f * raise;
        this.body.y -= 1.5f * raise;
        if (this.frontR != null) {
            this.frontR.upper().xRot += -1.5f * raise + 0.45f * swipe;
            this.frontR.upper().zRot += -0.35f * raise + 0.55f * swipe;
            this.frontR.shin().xRot += 0.9f * raise - 0.3f * swipe;
            this.frontR.foot().xRot -= 0.5f * raise;
        }
        this.neck[0].yRot -= 0.15f * swipe;
        this.head.xRot += 0.15f * swipe;
        this.jaw.xRot += 0.4f * env(a, 0.4f, 0.6f, 0.7f, 1.0f);
    }

    protected void tailSweep(float a) {
        float coil = env(a, 0.0f, 0.4f, 0.5f, 0.6f);
        float whip = env(a, 0.5f, 0.65f, 1.15f, 1.6f);
        this.body.y += 1.5f * coil;
        for (int i = 0; i < this.tail.length; i++) {
            this.tail[i].xRot -= 0.06f * coil;
            this.tail[i].yRot += (0.1f * coil - 0.05f * whip) * (i + 1) * 0.3f;
        }
        this.neckBend(0.12f * whip);
        this.jaw.xRot += 0.35f * whip;
    }

    protected void wingBuffet(float a, WingPose wp) {
        float open = env(a, 0.0f, 0.55f, 0.95f, 1.5f);
        float beat = env(a, 0.65f, 0.8f, 0.85f, 1.1f);
        float up = env(a, 0.3f, 0.6f, 0.65f, 0.8f);
        wp.fold *= 1.0f - open;
        wp.raise += 0.9f * up - 0.55f * beat;
        wp.sweep -= 0.5f * beat;
        this.body.xRot -= 0.18f * open;
        this.body.y -= 1.2f * open;
        this.neckBend(-0.2f * open);
        this.jaw.xRot += 0.5f * beat;
    }

    protected void roar(float a, WingPose wp, float extra) {
        float e = env(a, 0.15f, 0.6f, 1.6f, 2.2f);
        this.neckBend(-0.3f * e);
        if (this.neck.length > 2) {
            this.neck[this.neck.length - 1].xRot += 0.18f * e;
        }
        this.head.xRot -= 0.5f * e;
        this.jaw.xRot += 1.0f * e;
        this.head.zRot += Mth.sin(a * 20.0f) * 0.035f * e;
        this.body.xRot -= 0.1f * e;
        wp.fold *= 1.0f - 0.65f * e;
        wp.raise += 0.35f * e;
        wp.spread += 0.2f * e;
    }

    protected void stomp(float a, WingPose wp) {
        float rear = env(a, 0.0f, 0.75f, 0.85f, 1.02f);
        float slam = strike(a, 1.0f, 1.6f);
        this.body.xRot -= 0.55f * rear - 0.08f * slam;
        this.body.y -= 4.0f * rear;
        if (this.frontL != null && this.frontR != null) {
            for (Leg leg : new Leg[] {this.frontL, this.frontR}) {
                leg.upper().xRot -= 0.9f * rear;
                leg.shin().xRot += 0.8f * rear;
            }
        }
        this.hindL.upper().xRot -= 0.35f * rear;
        this.hindR.upper().xRot -= 0.35f * rear;
        this.neckBend(-0.25f * rear + 0.3f * slam);
        this.jaw.xRot += 0.6f * env(a, 0.5f, 0.9f, 1.1f, 1.5f);
        wp.fold *= 1.0f - 0.6f * rear;
        wp.raise += 0.6f * rear;
    }

    protected void takeOff(float a, WingPose wp) {
        float crouch = env(a, 0.0f, 0.4f, 0.48f, 0.6f);
        float leap = env(a, 0.48f, 0.6f, 0.8f, 1.4f);
        this.body.y += 4.0f * crouch;
        for (Leg leg : this.legs()) {
            leg.upper().xRot -= 0.25f * crouch;
            leg.shin().xRot += (leg.front() ? 0.6f : -0.6f) * crouch;
            leg.foot().xRot += 0.3f * crouch;
        }
        this.body.xRot -= 0.25f * leap;
        wp.fold *= 1.0f - rise(a, 0.2f, 0.6f);
        wp.raise += 0.6f * crouch;
        wp.flapAmp = Math.max(wp.flapAmp, 1.2f * leap);
    }

    protected void land(BossRenderState s, float a, WingPose wp) {
        float near = 1.0f - s.airBlend;
        this.body.xRot -= 0.35f * s.airBlend;
        if (this.frontL != null && this.frontR != null) {
            this.frontL.upper().xRot -= 0.5f * s.airBlend;
            this.frontR.upper().xRot -= 0.5f * s.airBlend;
        }
        wp.sweep -= 0.35f * s.airBlend;
        wp.flapAmp = Math.max(wp.flapAmp, 0.9f * s.airBlend);
        this.body.y += 2.0f * near * env(a, 0.0f, 0.1f, 0.4f, 1.0f);
    }

    protected void breath(float a) {
        float charge = env(a, 0.0f, 0.8f, 0.9f, 1.05f);
        float spew = env(a, 0.9f, 1.05f, 3.0f, 3.4f);
        this.neckBend(-0.35f * charge + 0.35f * spew);
        this.head.xRot += -0.3f * charge - 0.25f * spew;
        this.jaw.xRot += 0.35f * charge + 0.95f * spew;
        this.body.xScale *= 1.0f + 0.05f * charge;
        this.body.yScale *= 1.0f + 0.04f * charge;
        this.body.y -= 1.0f * charge;
        this.head.zRot += Mth.sin(a * 23.0f) * 0.02f * spew;
    }

    protected void airBreath(float a, WingPose wp) {
        float spew = env(a, 1.4f, 1.6f, 3.4f, 3.7f);
        this.neckBend(0.45f * spew);
        this.head.xRot += 0.15f * spew;
        this.jaw.xRot += 0.9f * spew;
        wp.flapAmp *= 1.0f - 0.7f * spew;     // gleitet beim Speien
        wp.sweep -= 0.15f * spew;
    }

    protected void volley(float a) {
        float kick = 0.0f;
        for (int i = 0; i < 6; i++) {
            kick = Math.max(kick, strike(a, 0.7f + i * 0.4f, 1.0f + i * 0.4f));
        }
        this.neckBend(-0.15f + 0.3f * kick);
        this.head.xRot -= 0.15f * kick;
        this.jaw.xRot += 0.2f + 0.7f * kick;
    }

    protected void rain(BossRenderState s, float a, WingPose wp) {
        float up = env(a, 0.0f, 0.8f, 4.6f, 5.4f);
        float roar = env(a, 0.3f, 0.6f, 1.4f, 2.0f);
        this.body.xRot -= 0.5f * up;
        this.neckBend(-0.5f * roar + 0.2f * up * (1 - roar));
        this.head.xRot -= 0.6f * roar;
        this.jaw.xRot += 1.0f * roar;
        wp.flapAmp = Math.max(wp.flapAmp, 1.1f * up);
        wp.sweep -= 0.25f * up;
        for (Leg leg : this.legs()) {
            leg.upper().xRot -= 0.3f * up;
        }
    }

    protected void ring(float a, WingPose wp) {
        float crouch = env(a, 0.0f, 0.85f, 0.95f, 1.05f);
        float burst = env(a, 1.0f, 1.12f, 1.6f, 2.3f);
        this.body.y += 3.0f * crouch - 2.0f * burst;
        this.body.xRot -= 0.3f * burst;
        this.neckBend(0.25f * crouch - 0.45f * burst);
        this.head.xRot -= 0.3f * burst;
        this.jaw.xRot += 0.3f * crouch + 1.0f * burst;
        wp.fold *= 1.0f - burst;
        wp.raise += 0.8f * burst - 0.3f * crouch;
        wp.spread += 0.3f * burst;
    }

    protected void erupt(float a) {
        float rear = env(a, 0.0f, 0.45f, 0.5f, 0.62f);
        float slam = strike(a, 0.6f, 1.3f);
        this.body.xRot -= 0.35f * rear - 0.08f * slam;
        this.body.y -= 2.5f * rear;
        if (this.frontL != null && this.frontR != null) {
            this.frontL.upper().xRot -= 0.7f * rear;
            this.frontR.upper().xRot -= 0.7f * rear;
        }
        this.neckBend(-0.2f * rear + 0.45f * slam);
        this.jaw.xRot += 0.5f * slam;
    }

    // =====================================================================================
    // Zustände
    // =====================================================================================

    /** Erwachen: Kopf hoch, Flügel eng – dann breitet der Drache beim Brüllen die Flügel aus (Standard: Brüllen bei 6,6 s). */
    protected void awakening(BossRenderState s, float time, WingPose wp) {
        float emerge = env(time, 1.5f, 3.0f, 5.6f, 6.6f);
        float roar = env(time, 6.3f, 6.7f, 7.6f, 8.4f);
        this.body.xRot -= 0.14f * emerge + 0.08f * roar;
        this.neckBend(-0.16f * emerge - 0.26f * roar);
        if (this.neck.length > 2) {
            this.neck[this.neck.length - 1].xRot += 0.25f * emerge + 0.2f * roar;   // S-Kurve: oben wieder nach vorn
        }
        this.head.xRot -= 0.1f * emerge + 0.5f * roar;
        this.jaw.xRot += 1.0f * roar + 0.15f * emerge;
        this.head.zRot += Mth.sin(time * 20.0f) * 0.03f * roar;
        wp.fold = Math.max(0.0f, 1.0f - roar * 1.1f);
        wp.raise += 0.8f * roar;
        wp.spread += 0.25f * roar;
        wp.flapAmp = 0.0f;
    }

    protected void slumber(BossRenderState s, float time, WingPose wp) {
        float down = rise(time, 0.0f, 2.2f);
        this.body.y += this.bodyHeight * 0.45f * down;
        for (Leg leg : this.legs()) {
            leg.upper().xRot -= (leg.front() ? 0.9f : 1.2f) * down;
            leg.shin().xRot += (leg.front() ? 1.4f : 1.9f) * down;
            leg.foot().xRot -= 0.4f * down;
        }
        this.neckBend(0.5f * down);
        this.head.xRot += 0.2f * down;
        for (int i = 0; i < this.tail.length; i++) {
            this.tail[i].yRot += 0.12f * down;
        }
        wp.fold = 1.0f;
    }

    /** Tod: Aufbäumen und Um-sich-Schlagen, dann knicken die Beine ein, Kopf und Flügel sinken zu Boden. */
    protected void dying(BossRenderState s, float time, WingPose wp) {
        float thrash = env(time, 0.0f, 0.3f, 1.6f, 2.2f);
        float fall = rise(time, 1.8f, 3.4f);
        float shake = Mth.sin(time * 17.0f) * thrash;
        this.neckBend(-0.5f * thrash * Mth.abs(Mth.sin(time * 3.0f)) + 0.65f * fall);
        this.neck[0].yRot += shake * 0.25f;
        this.head.xRot += 0.3f * fall - 0.3f * thrash;
        this.head.zRot += shake * 0.15f + 0.35f * fall;
        this.jaw.xRot += 0.8f * thrash + 0.35f * fall;
        this.body.y += this.bodyHeight * 0.55f * fall;
        this.body.zRot += 0.12f * fall;
        for (Leg leg : this.legs()) {
            leg.upper().xRot -= (leg.front() ? 1.0f : 1.3f) * fall;
            leg.upper().zRot += leg.side() * 0.35f * fall;
            leg.shin().xRot += (leg.front() ? 1.5f : 2.0f) * fall;
            leg.upper().xRot += shake * 0.2f * (1 - fall);
        }
        for (int i = 0; i < this.tail.length; i++) {
            this.tail[i].yRot += Mth.sin(time * 6.0f - i * 0.7f) * 0.18f * thrash + 0.08f * fall;
            this.tail[i].xRot += 0.06f * fall;
        }
        float twitch = Mth.sin(time * 9.0f) * env(time, 4.0f, 4.2f, 4.6f, 5.0f) * 0.1f;
        this.head.xRot += twitch;
        // Flügel schlagen wild, dann liegen sie halb ausgebreitet am Boden
        wp.fold = Math.min(wp.fold, 1.0f - 0.6f * Math.max(thrash, fall));
        wp.flapAmp = 0.7f * thrash;
        wp.flap = time * 9.0f;
        wp.raise -= 0.45f * fall;
        wp.spread += 0.15f * fall;
    }

    // =====================================================================================
    // Flügel
    // =====================================================================================

    /**
     * Legt die Flügel an (fold = 1) bzw. breitet sie aus (fold = 0) und lässt sie schlagen. Angelegt rollt der Oberarm
     * so, dass die Flughaut senkrecht am Körper hängt, der Unterarm klappt nach vorn zurück, die Hand mit den Fingern
     * wieder nach hinten – wie bei einer Fledermaus.
     */
    protected void applyWings(WingPose wp) {
        if (this.wingL == null || this.wingR == null) {
            return;
        }
        float fold = Mth.clamp(wp.fold, 0.0f, 1.0f);
        float open = 1.0f - fold;
        float f = Mth.sin(wp.flap);
        float lag = Mth.sin(wp.flap - 0.7f);
        float up = Math.max(0.0f, Mth.cos(wp.flap));            // Aufschlag: Flügel leicht einziehen
        for (Wing w : new Wing[] {this.wingL, this.wingR}) {
            float sx = w.sx();
            // Angelegt: Oberarm nach hinten (Flughaut hängt senkrecht), Unterarm nach vorn, Finger wieder nach hinten;
            // die Flughäute werden dabei zusammengeschoben (Skalierung quer zu den Knochen)
            w.arm().xRot += -1.3f * fold;
            w.arm().yRot += -1.3f * sx * fold;
            w.arm().zRot += -0.35f * sx * fold;
            w.arm().zScale *= 1.0f - 0.4f * fold;
            w.fore().zRot += 2.85f * sx * fold;
            w.fore().zScale *= 1.0f - 0.4f * fold;
            w.hand().xRot += 1.57f * fold;
            w.hand().zRot += -2.97f * sx * fold;
            for (ModelPart finger : w.fingers()) {
                finger.yRot *= 1.0f - 0.92f * fold + 0.18f * wp.spread * open;
                finger.zScale *= 1.0f - 0.8f * fold;
            }
            // Flügelschlag (nur wenn ausgebreitet)
            float amp = wp.flapAmp * open;
            w.arm().zRot += (-f * 0.78f - wp.raise * 0.6f) * sx * amp + (-wp.raise * 0.6f) * sx * (1 - amp) * open;
            w.fore().zRot += (-lag * 0.5f + up * 0.35f) * sx * amp;
            w.hand().zRot += (up * 0.3f) * sx * amp;
            w.arm().yRot += (wp.sweep * 0.6f + up * 0.12f * amp) * -sx;
            for (int i = 0; i < w.fingers().length; i++) {
                w.fingers()[i].yRot *= 1.0f - 0.2f * up * amp;
            }
        }
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Beugt den Hals (positiv = nach vorn/unten, negativ = hoch/zurück), verteilt auf die Glieder. */
    protected void neckBend(float amount) {
        float[] share = this.neck.length >= 4 ? new float[] {0.45f, 0.25f, 0.18f, 0.12f} : new float[] {0.5f, 0.3f, 0.2f};
        for (int i = 0; i < this.neck.length; i++) {
            this.neck[i].xRot += amount * (i < share.length ? share[i] : 0.1f);
        }
    }

    protected Leg[] legs() {
        return this.frontL != null && this.frontR != null
                ? new Leg[] {this.frontL, this.frontR, this.hindL, this.hindR}
                : new Leg[] {this.hindL, this.hindR};
    }

    public static boolean isDying(BossRenderState s) {
        return s.bossState == DragonBossEntity.State.DYING || s.deathSeconds > 0;
    }
}
