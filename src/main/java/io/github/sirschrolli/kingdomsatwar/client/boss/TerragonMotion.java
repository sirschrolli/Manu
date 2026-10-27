package io.github.sirschrolli.kingdomsatwar.client.boss;

import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.env;
import static io.github.sirschrolli.kingdomsatwar.client.boss.BossAnim.rise;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FOOT_X;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FRONT_L;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.FRONT_R;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.HIND_L;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.HIND_R;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.SHIN_X;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.UPPER_X;
import static io.github.sirschrolli.kingdomsatwar.client.boss.DragonPose.UPPER_Z;

import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import net.minecraft.util.Mth;

/**
 * Bewegungen von Terragon: ein wandelnder Berg. Jeder Schritt lässt den Rumpf schwer einsinken, der Kopf pendelt tief,
 * der Keulenschwanz schwingt träge. Beim Eingraben wühlen die Vorderbeine abwechselnd den Boden auf; unter der Erde
 * sind die Beine angelegt. Kristallpanzer: breitbeinig, bebend, mit erhobenem Kopf. Erwachen: bricht nach dem Beben
 * aus dem Fels (4,2 s), schüttelt Gestein ab und brüllt (6,9 s).
 */
public class TerragonMotion extends DragonMotion {
    public TerragonMotion(int necks, int tails, boolean quadruped, float bodyHeight) {
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
        float bob = 0.5f - 0.5f * Mth.cos(c * 2.0f);
        p.bodyY += 0.8f * bob * speed;                       // schweres Einsinken bei jedem Schritt
        p.bodyRoll += 0.04f * Mth.sin(c) * speed;
        p.neckBend += 0.08f * speed + 0.04f * bob * speed;
    }

    @Override
    protected void idle(BossRenderState s, DragonPose p, float t, float air) {
        super.idle(s, p, t, air);
        float breath = Mth.sin(t * 1.4f);
        p.chest *= 1.0f + 0.02f * breath;                     // tiefes, langsames Atmen
        p.neckBend += 0.06f;
    }

    @Override
    protected void awakening(BossRenderState s, DragonPose p, float time) {
        float crouch = 1.0f - rise(time, 4.1f, 5.0f);
        float strain = env(time, 1.5f, 2.5f, 4.1f, 4.4f);
        float shake = env(time, 5.0f, 5.3f, 6.2f, 6.6f);
        float roar = env(time, 6.8f, 7.1f, 8.2f, 9.0f);
        p.bodyY += this.bodyHeight * 0.35f * crouch;
        for (int leg : this.legs()) {
            boolean front = leg == FRONT_L || leg == FRONT_R;
            p.leg(leg)[UPPER_X] -= (front ? 0.7f : 1.0f) * crouch;
            p.leg(leg)[SHIN_X] += (front ? 1.1f : 1.5f) * crouch;
        }
        p.neckBend += 0.6f * crouch - 0.35f * roar;
        p.headPitch += 0.3f * crouch - 0.55f * roar;
        p.bodyRoll += Mth.sin(time * 28.0f) * 0.02f * strain + Mth.sin(time * 12.0f) * 0.08f * shake;
        p.headRoll += Mth.sin(time * 12.0f + 1.0f) * 0.18f * shake;
        p.neckYaw += Mth.sin(time * 12.0f + 0.5f) * 0.12f * shake;
        p.bodyPitch -= 0.12f * roar;
        p.jaw += 1.0f * roar + 0.3f * shake;
        p.chest *= 1.0f + 0.06f * roar + 0.03f * strain;
        for (int i = 0; i < this.tails; i++) {
            p.tailYaw[i] += Mth.sin(time * 12.0f - i * 0.6f) * 0.07f * shake;
        }
    }

    /** Eingraben: Kopf tief, die Vorderbeine graben abwechselnd; unter der Erde angelegt; nach dem Auftauchen brüllen. */
    @Override
    protected void special1(BossRenderState s, DragonPose p, float a) {
        float dig = env(a, 0.0f, 0.2f, 1.3f, 1.5f);
        boolean under = s.moveMode == DragonBossEntity.MoveMode.BURROW;
        float tuck = under ? 1.0f : 0.0f;
        p.neckBend += 0.55f * dig + 0.35f * tuck;
        p.headPitch += 0.35f * dig;
        p.bodyPitch += 0.18f * dig + 0.1f * tuck;
        p.jaw += 0.4f * dig;
        float paddle = Mth.sin(a * 14.0f);
        p.leg(FRONT_L)[UPPER_X] += (-0.6f + 0.6f * paddle) * dig - 0.6f * tuck;
        p.leg(FRONT_R)[UPPER_X] += (-0.6f - 0.6f * paddle) * dig - 0.6f * tuck;
        p.leg(FRONT_L)[SHIN_X] += (0.5f + 0.4f * paddle) * dig + 0.8f * tuck;
        p.leg(FRONT_R)[SHIN_X] += (0.5f - 0.4f * paddle) * dig + 0.8f * tuck;
        p.leg(HIND_L)[UPPER_X] += 0.8f * tuck;
        p.leg(HIND_R)[UPPER_X] += 0.8f * tuck;
        p.bodyRoll += Mth.sin(a * 14.0f) * 0.04f * dig;
        if (!under && a > 2.0f) {
            // aufgetaucht: Kopf hoch, Maul auf
            p.neckBend -= 0.35f;
            p.headPitch -= 0.45f;
            p.jaw += 0.9f;
            p.chest *= 1.05f;
        }
    }

    /** Kristallpanzer: breitbeinig, der ganze Leib bebt, der Kopf hoch erhoben. */
    @Override
    protected void special2(BossRenderState s, DragonPose p, float a) {
        float e = env(a, 0.0f, 0.4f, 2.0f, 2.5f);
        float roar = env(a, 0.1f, 0.4f, 1.4f, 1.9f);
        p.leg(FRONT_L)[UPPER_Z] += 0.2f * e;
        p.leg(FRONT_R)[UPPER_Z] -= 0.2f * e;
        p.leg(HIND_L)[UPPER_Z] += 0.15f * e;
        p.leg(HIND_R)[UPPER_Z] -= 0.15f * e;
        p.bodyY += 1.5f * e;
        p.bodyRoll += Mth.sin(a * 32.0f) * 0.03f * e;
        p.neckBend -= 0.4f * e;
        p.headPitch -= 0.5f * roar;
        p.jaw += 1.0f * roar;
        p.chest *= 1.0f + 0.07f * e;
        for (int leg : this.legs()) {
            p.leg(leg)[FOOT_X] += 0.1f * e;
        }
    }
}
