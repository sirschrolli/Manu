package io.github.sirschrolli.kingdomsatwar.client.boss;

import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.env;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.rise;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.strike;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FOOT_X;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FRONT_L;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FRONT_R;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.HIND_L;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.HIND_R;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.SHIN_X;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.UPPER_X;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.UPPER_Z;

import net.minecraft.util.Mth;

/**
 * Bewegungen von Verdantis: schwer und erdverbunden. Im Gang schaukelt der breite Rumpf, der Kopf mit dem Geweih
 * pendelt tief. Beim Erwachen liegt sie zusammengekauert unter dem Hügel, bricht hervor, schüttelt Erde und Laub ab
 * und breitet brüllend die Blattflügel aus. Wurzelfessel: aufbäumen und die Pranken in den Boden schlagen.
 * Heilung: breitbeinig verwurzelt, Kopf und Geweih erhoben, die Flügel halb offen und zitternd. Giftsporen:
 * schüttelt sich wie ein nasser Hund.
 */
public class VerdantisMotion extends DragonMotion {
    public VerdantisMotion(int necks, int tails, boolean quadruped, float bodyHeight) {
        super(necks, tails, quadruped, bodyHeight);
    }

    @Override
    protected void walk(BossRenderState s, DragonPose p, float ground) {
        super.walk(s, p, ground);
        float speed = Math.min(s.walkAnimationSpeed * 1.4f, 1.0f) * ground;
        if (speed < 0.01f) {
            return;
        }
        float c = s.walkAnimationPos * 0.42f;
        p.bodyRoll += 0.045f * Mth.sin(c) * speed;              // schweres Schaukeln
        p.neckBend += 0.06f * speed;                            // Kopf tief
        p.headYaw += 0.04f * Mth.sin(c * 0.5f) * speed;
    }

    @Override
    protected void idle(BossRenderState s, DragonPose p, float t, float air) {
        super.idle(s, p, t, air);
        p.neckBend += 0.05f * (1.0f - air);                     // trägt den Kopf etwas tiefer
        p.headPitch += 0.03f * (1.0f - air);
    }

    /** Erwachen: kauert unter dem Hügel, bricht hervor (4,6 s), schüttelt sich (5,0–6,6 s) und brüllt (7,1 s). */
    @Override
    protected void awakening(BossRenderState s, DragonPose p, float time) {
        float crouch = 1.0f - rise(time, 4.4f, 5.4f);
        float strain = env(time, 2.5f, 3.5f, 4.4f, 4.7f);
        float shake = env(time, 5.0f, 5.3f, 6.2f, 6.7f);
        float roar = env(time, 6.9f, 7.3f, 8.4f, 9.2f);
        p.bodyY += this.bodyHeight * 0.4f * crouch;
        for (int leg : this.legs()) {
            boolean front = leg == FRONT_L || leg == FRONT_R;
            p.leg(leg)[UPPER_X] -= (front ? 0.9f : 1.2f) * crouch;
            p.leg(leg)[SHIN_X] += (front ? 1.3f : 1.8f) * crouch;
        }
        p.neckBend += 0.85f * crouch - 0.35f * roar;
        p.headPitch += 0.4f * crouch - 0.55f * roar;
        p.neckTip += 0.2f * roar;
        p.bodyRoll += Mth.sin(time * 26.0f) * 0.02f * strain + Mth.sin(time * 15.0f) * 0.09f * shake;
        p.headRoll += Mth.sin(time * 15.0f + 1.0f) * 0.2f * shake;
        p.neckYaw += Mth.sin(time * 15.0f + 0.5f) * 0.15f * shake;
        for (int i = 0; i < this.tails; i++) {
            p.tailYaw[i] += 0.18f * crouch + Mth.sin(time * 15.0f - i * 0.6f) * 0.06f * shake;
        }
        p.bodyPitch -= 0.1f * roar;
        p.jaw += 1.0f * roar + 0.25f * shake;
        p.chest *= 1.0f + 0.05f * roar + 0.03f * strain;
        p.fold = Math.max(0.0f, 1.0f - roar * 1.1f - 0.3f * shake);
        p.raise += 0.85f * roar;
        p.spread += 0.3f * roar;
        p.flapAmp = 0.0f;
    }

    /** Wurzelfessel: aufbäumen (bis 1,0 s), Pranken in den Boden (1,1 s), festkrallen und beben, bis die Wurzeln kommen. */
    @Override
    protected void special1(BossRenderState s, DragonPose p, float a) {
        float rear = env(a, 0.0f, 0.9f, 1.0f, 1.12f);
        float slam = strike(a, 1.1f, 1.6f);
        float dig = env(a, 1.1f, 1.25f, 1.95f, 2.6f);
        p.bodyPitch -= 0.55f * rear - 0.12f * dig;
        p.bodyY -= 4.0f * rear - 1.5f * dig;
        for (int leg : new int[] {FRONT_L, FRONT_R}) {
            p.leg(leg)[UPPER_X] += -1.1f * rear + 0.35f * dig;
            p.leg(leg)[SHIN_X] += 0.9f * rear + 0.6f * dig;
            p.leg(leg)[FOOT_X] -= 0.4f * dig;
        }
        p.leg(HIND_L)[UPPER_X] -= 0.4f * rear;
        p.leg(HIND_R)[UPPER_X] -= 0.4f * rear;
        p.neckBend += -0.3f * rear + 0.55f * dig + 0.2f * slam;
        p.headPitch += -0.3f * rear + 0.2f * dig;
        p.jaw += 0.5f * rear + 0.8f * dig;
        p.bodyRoll += Mth.sin(a * 30.0f) * 0.025f * dig;
        p.headRoll += Mth.sin(a * 24.0f) * 0.05f * dig;
        p.fold *= 1.0f - 0.5f * rear;
        p.raise += 0.5f * rear;
    }

    /** Heilung: breitbeinig, Kopf und Geweih hoch, Flügel halb offen und zitternd, tiefes, langsames Atmen. */
    @Override
    protected void special2(BossRenderState s, DragonPose p, float a) {
        float e = env(a, 0.0f, 0.8f, 4.8f, 5.6f);
        float roar = env(a, 4.9f, 5.2f, 5.6f, 6.0f);
        float breath = Mth.sin(a * 2.2f);
        p.bodyY += 1.2f * e;
        p.leg(FRONT_L)[UPPER_Z] += 0.18f * e;
        p.leg(FRONT_R)[UPPER_Z] -= 0.18f * e;
        p.leg(HIND_L)[UPPER_Z] += 0.15f * e;
        p.leg(HIND_R)[UPPER_Z] -= 0.15f * e;
        p.neckBend -= 0.45f * e + 0.2f * roar;
        p.neckTip += 0.15f * e;
        p.headPitch -= 0.5f * e + 0.3f * roar;
        p.jaw += 0.2f * e + 0.06f * breath * e + 0.9f * roar;
        p.chest *= 1.0f + (0.05f + 0.04f * breath) * e;
        p.fold = Math.min(p.fold, 1.0f - 0.55f * e);
        p.raise += 0.4f * e;
        p.spread += 0.2f * e;
        p.flapAmp = Math.max(p.flapAmp, 0.05f * e);
        p.flap = a * 30.0f;                                     // Zittern der Flügel
        p.headRoll += Mth.sin(a * 3.0f) * 0.04f * e;
    }

    /** Giftsporen: schüttelt sich wie ein nasser Hund, dann bauschen sich die Flügel – die Sporen platzen auf (1,0 s). */
    @Override
    protected void ring(DragonPose p, float a) {
        float shake = env(a, 0.0f, 0.15f, 0.85f, 1.0f);
        float puff = env(a, 0.95f, 1.1f, 1.5f, 2.2f);
        p.bodyRoll += Mth.sin(a * 22.0f) * 0.1f * shake;
        p.neckYaw += Mth.sin(a * 22.0f + 0.8f) * 0.2f * shake;
        p.headRoll += Mth.sin(a * 22.0f + 1.4f) * 0.25f * shake;
        for (int i = 0; i < this.tails; i++) {
            p.tailYaw[i] += Mth.sin(a * 22.0f - i * 0.7f) * 0.08f * shake;
        }
        p.bodyY += 1.5f * puff;
        p.chest *= 1.0f + 0.08f * puff;
        p.fold *= 1.0f - 0.6f * puff;
        p.raise += 0.5f * puff;
        p.spread += 0.3f * puff;
        p.jaw += 0.5f * puff;
        p.neckBend += 0.15f * puff;
    }
}
