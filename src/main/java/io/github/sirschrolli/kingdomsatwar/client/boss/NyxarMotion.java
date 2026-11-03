package io.github.sirschrolli.kingdomsatwar.client.boss;

import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.env;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.rise;

import net.minecraft.util.Mth;

/**
 * Bewegungen von Nyxar: Beim Erwachen kauert er als Schatten zusammengesunken auf dem Thronhof, zittert, nimmt mit
 * einem Ruck Gestalt an, reißt die Schwingen auf und brüllt. Schattensprung: Er duckt sich, zerfließt – und schlägt nach
 * dem Wiederauftauchen mit der Pranke zu. Finsternis: Er bäumt sich auf, breitet die Flügel weit über die Arena und
 * brüllt das Licht aus.
 */
public class NyxarMotion extends DragonMotion {
    public NyxarMotion(int necks, int tails, boolean quadruped, float bodyHeight) {
        super(necks, tails, quadruped, bodyHeight);
    }

    @Override
    protected void awakening(BossRenderState s, DragonPose p, float time) {
        float crouch = 1.0f - rise(time, 4.4f, 5.0f);
        float tremble = env(time, 1.0f, 2.0f, 4.2f, 4.5f);
        float rear = env(time, 4.5f, 5.0f, 5.8f, 6.6f);
        float roar = env(time, 7.4f, 8.0f, 9.2f, 9.9f);
        p.bodyY += this.bodyHeight * 0.4f * crouch;
        for (int leg : this.legs()) {
            boolean front = leg == DragonPose.FRONT_L || leg == DragonPose.FRONT_R;
            p.leg(leg)[DragonPose.UPPER_X] -= (front ? 0.85f : 1.15f) * crouch;
            p.leg(leg)[DragonPose.SHIN_X] += (front ? 1.3f : 1.8f) * crouch;
        }
        p.neckBend += 0.7f * crouch - 0.35f * rear - 0.3f * roar;
        p.neckTip += 0.25f * rear + 0.2f * roar;
        p.headPitch += 0.45f * crouch - 0.25f * rear - 0.5f * roar;
        p.bodyRoll += Mth.sin(time * 29.0f) * 0.025f * tremble;
        p.headRoll += Mth.sin(time * 23.0f) * 0.04f * tremble + Mth.sin(time * 20.0f) * 0.035f * roar;
        p.bodyPitch -= 0.3f * rear + 0.1f * roar;
        p.jaw += 1.0f * roar + 0.5f * rear;
        p.chest *= 1.0f + 0.06f * roar;
        p.fold = Math.max(0.0f, 1.0f - Math.max(rear, roar) * 1.2f);
        p.raise += 1.0f * rear + 0.8f * roar;
        p.spread += 0.35f * rear + 0.3f * roar;
        p.flapAmp = 0.0f;
    }

    /** Schattensprung: ducken und zerfließen, nach dem Auftauchen (1,5 s) ein Prankenhieb aus der Dunkelheit. */
    @Override
    protected void special1(BossRenderState s, DragonPose p, float a) {
        float duck = env(a, 0.0f, 0.5f, 1.5f, 1.7f);
        p.bodyY += this.bodyHeight * 0.25f * duck;
        p.neckBend += 0.4f * duck;
        p.headPitch += 0.3f * duck;
        p.fold = Math.min(p.fold + duck, 1.0f);
        if (a > 1.45f) {
            this.claw(p, a - 1.45f);
            float snarl = env(a, 1.5f, 1.7f, 2.2f, 2.6f);
            p.jaw += 0.6f * snarl;
        }
    }

    /** Finsternis: aufbäumen, Flügel weit gespreizt und erhoben, langes Brüllen. */
    @Override
    protected void special2(BossRenderState s, DragonPose p, float a) {
        float rear = env(a, 0.0f, 0.9f, 6.5f, 7.6f);
        float roar = env(a, 0.6f, 1.2f, 2.4f, 3.2f);
        p.bodyPitch -= 0.32f * rear;
        p.bodyY -= 3.0f * rear;
        if (this.quadruped) {
            for (int leg : new int[] {DragonPose.FRONT_L, DragonPose.FRONT_R}) {
                p.leg(leg)[DragonPose.UPPER_X] -= 0.8f * rear;
                p.leg(leg)[DragonPose.SHIN_X] += 0.7f * rear;
            }
        }
        p.neckBend -= 0.35f * rear;
        p.neckTip += 0.2f * rear;
        p.headPitch -= 0.35f * rear + 0.3f * roar;
        p.jaw += 1.0f * roar + 0.25f * rear;
        p.headRoll += Mth.sin(a * 20.0f) * 0.035f * roar;
        p.fold = Math.max(0.0f, p.fold - 1.2f * rear);
        p.raise += 1.1f * rear;
        p.spread += 0.45f * rear;
        p.flapAmp = 0.0f;
    }
}
