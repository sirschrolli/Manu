package io.github.sirschrolli.kingdomsatwar.client.boss;

import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.env;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.rise;

import net.minecraft.util.Mth;

/**
 * Bewegungen von Glaciara: Sie erwacht zusammengerollt im Eiskristall (Kopf an der Brust, Schwanz um den Körper
 * gelegt), sprengt ihn und richtet sich mit ausgebreiteten Flügeln auf. Schneesturm: Sie steigt auf die Hinterbeine
 * und fächelt mit weiten Flügelschlägen den Sturm heran. Eisgefängnis: Kopf vorgestreckt, ein zischender Hauch.
 */
public class GlaciaraMotion extends DragonMotion {
    public GlaciaraMotion(int necks, int tails, boolean quadruped, float bodyHeight) {
        super(necks, tails, quadruped, bodyHeight);
    }

    @Override
    protected void awakening(BossRenderState s, DragonPose p, float time) {
        float curl = 1.0f - rise(time, 4.3f, 5.3f);
        float tremble = env(time, 1.0f, 2.0f, 4.2f, 4.5f);
        float rear = env(time, 4.6f, 5.4f, 6.0f, 6.8f);
        float roar = env(time, 6.2f, 6.6f, 7.6f, 8.4f);
        p.bodyY += this.bodyHeight * 0.35f * curl;
        for (int leg : this.legs()) {
            boolean front = leg == DragonPose.FRONT_L || leg == DragonPose.FRONT_R;
            p.leg(leg)[DragonPose.UPPER_X] -= (front ? 0.8f : 1.1f) * curl;
            p.leg(leg)[DragonPose.SHIN_X] += (front ? 1.2f : 1.7f) * curl;
        }
        p.neckBend += 0.95f * curl - 0.3f * rear - 0.3f * roar;
        p.neckTip += 0.2f * rear + 0.2f * roar;
        p.headPitch += 0.55f * curl - 0.2f * rear - 0.5f * roar;
        p.neckYaw += 0.35f * curl;
        for (int i = 0; i < this.tails; i++) {
            p.tailYaw[i] += (0.24f + 0.012f * i) * curl;     // eng um den Körper gelegt
            p.tailPitch[i] -= 0.03f * curl;
        }
        p.bodyRoll += Mth.sin(time * 31.0f) * 0.02f * tremble;
        p.headRoll += Mth.sin(time * 27.0f) * 0.03f * tremble;
        p.bodyPitch -= 0.25f * rear + 0.08f * roar;
        p.jaw += 1.0f * roar + 0.2f * rear;
        p.chest *= 1.0f + 0.05f * roar;
        p.fold = Math.max(0.0f, 1.0f - Math.max(rear, roar) * 1.1f);
        p.raise += 0.9f * roar + 0.4f * rear;
        p.spread += 0.3f * roar;
        p.flapAmp = 0.0f;
    }

    /** Schneesturm: auf die Hinterbeine, Flügel weit offen, langsame, kräftige Schläge. */
    @Override
    protected void special1(BossRenderState s, DragonPose p, float a) {
        float up = env(a, 0.0f, 0.8f, 4.2f, 5.0f);
        float beat = env(a, 0.8f, 1.2f, 4.0f, 4.6f);
        p.bodyPitch -= 0.55f * up;
        p.bodyY -= 3.0f * up;
        for (int leg : new int[] {DragonPose.FRONT_L, DragonPose.FRONT_R}) {
            p.leg(leg)[DragonPose.UPPER_X] -= 1.0f * up;
            p.leg(leg)[DragonPose.SHIN_X] += 0.9f * up;
        }
        p.leg(DragonPose.HIND_L)[DragonPose.UPPER_X] -= 0.4f * up;
        p.leg(DragonPose.HIND_R)[DragonPose.UPPER_X] -= 0.4f * up;
        p.neckBend -= 0.35f * up;
        p.neckTip += 0.25f * up;
        p.headPitch -= 0.45f * up;
        p.jaw += 0.75f * up;
        p.fold *= 1.0f - up;
        p.flapAmp = Math.max(p.flapAmp, 0.75f * beat);
        p.flap = a * 4.2f;
        p.raise += 0.3f * up;
        p.spread += 0.35f * up;
    }

    /** Eisgefängnis: Kopf und Hals vorgestreckt, das Maul öffnet sich zu einem eisigen Hauch. */
    @Override
    protected void special2(BossRenderState s, DragonPose p, float a) {
        float aim = env(a, 0.0f, 0.5f, 1.8f, 2.4f);
        float hiss = env(a, 0.6f, 0.8f, 1.6f, 2.0f);
        p.neckBend += 0.45f * aim;
        p.neckTip -= 0.2f * aim;
        p.headPitch -= 0.2f * aim;
        p.bodyY += 1.5f * aim;
        p.bodyPitch += 0.06f * aim;
        p.jaw += 0.7f * hiss;
        p.chest *= 1.0f + 0.04f * hiss;
        p.headRoll += Mth.sin(a * 21.0f) * 0.03f * hiss;
        p.fold = Math.min(p.fold, 1.0f - 0.3f * aim);
        p.raise += 0.25f * aim;
    }
}
